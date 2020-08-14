/**
 * Copyright (c) 2020 Adrian Siekierka
 *
 * This file is part of zima.
 *
 * zima is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * zima is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with zima.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.asie.zzttools.zima;

import pl.asie.zzttools.util.Coord2D;
import pl.asie.zzttools.util.ImageUtils;
import pl.asie.zzttools.util.Pair;
import pl.asie.zzttools.zzt.Board;
import pl.asie.zzttools.zzt.Element;
import pl.asie.zzttools.zzt.Stat;
import pl.asie.zzttools.zzt.TextVisualData;
import pl.asie.zzttools.zzt.TextVisualRenderer;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ImageConverter {
	private final TextVisualData visual;
	private final ImageMseCalculator mseCalculator;
	private final boolean allowFaces;

	public ImageConverter(TextVisualData visual, ImageMseCalculator mseCalculator, boolean allowFaces) {
		this.visual = visual;
		this.allowFaces = allowFaces;
		this.mseCalculator = mseCalculator;
	}

	private ElementResult min(ElementResult existing, ElementResult newResult) {
		if (existing == null) {
			return newResult;
		}
		return newResult.getMse() < existing.getMse() ? newResult : existing;
	}

	public Pair<Board, BufferedImage> convert(BufferedImage inputImage, ImageConverterRuleset ruleset,
	                                          int x, int y, int width, int height, int playerX, int playerY, int maxStatCount, boolean noBlinking,
	                                          float contrastReduction,
	                                          TextVisualRenderer previewRenderer,
	                                          ProgressCallback progressCallback) {
		Board board = new Board(playerX, playerY);
		BufferedImage preview = null;

		int pixelWidth = width * visual.getCharWidth();
		int pixelHeight = height * visual.getCharHeight();

		if (inputImage.getWidth() != pixelWidth || inputImage.getHeight() != pixelHeight) {
			inputImage = ImageUtils.scale(inputImage, pixelWidth, pixelHeight, AffineTransformOp.TYPE_BICUBIC);
		}
		final BufferedImage image = inputImage;

		List<Pair<Coord2D, ElementResult>> statfulStrategies = new ArrayList<>();
		List<ElementRule> rules = ruleset.getRules();
		ElementResult[] previewResults = new ElementResult[width * height];
		ImageContrastReducer contrastReducer = new ImageContrastReducer(visual, contrastReduction);

		IntStream.range(0, width * height).parallel().forEach(pos -> {
			synchronized (progressCallback) {
				progressCallback.step(width * height);
			}

			int ix = pos % width;
			int iy = pos / width;
			boolean spaceForbidden = ((x + ix) < 1 || (y + iy) < 1 || (x + ix) > Board.WIDTH || (y + iy) > Board.HEIGHT);

			if ((x + ix) == playerX && (y + iy) == playerY) {
				spaceForbidden = true;
			}

			if (spaceForbidden) {
				previewResults[iy * width + ix] = new ElementResult(Element.EMPTY, false, false, 32, 0);
				return;
			}

			ElementResult statlessResult = null;
			ElementResult statfulResult = null;
			ImageMseCalculator.Applier applyMseFunc = mseCalculator.applyMse(image, ix * visual.getCharWidth(), iy * visual.getCharHeight());
			float lowestGlobalMse = Float.MAX_VALUE;

			for (ElementRule rule : rules) {
				if (rule.getStrategy().isRequiresStat() && maxStatCount <= 0) {
					// no stats - no stat strategies!
					continue;
				}

				Stream<ElementResult> proposals = null;
				switch (rule.getStrategy()) {
					case EMPTY:
						proposals = Stream.of(new ElementResult(Element.EMPTY, false, false, 32, 0));
						break;
					case ELEMENT:
						if (!allowFaces && rule.getChr() == 2) continue;
						proposals = IntStream.range(0, noBlinking ? 128 : 256).mapToObj(i -> new ElementResult(rule.getElement(), false, false, rule.getChr(), i));
						break;
					case TEXT:
						proposals = IntStream.of(ruleset.getAllowedTextCharIndices()).mapToObj(i -> new ElementResult(rule.getElement(), false, true, i, rule.getColor()));
						break;
					case USE_STAT_P1:
						proposals = IntStream.of(ruleset.getAllowedObjectIndices(noBlinking)).mapToObj(i -> new ElementResult(rule.getElement(), true, false, i & 0xFF, i >> 8));
						break;
				}

				float lowestMse = lowestGlobalMse;
				ElementResult lowestResult = null;
				float weight = 1.0f;

				Iterator<ElementResult> it = proposals.iterator();
				while (it.hasNext()) {
					ElementResult result = it.next();

					if (!allowFaces && (result.getCharacter() == 1 || result.getCharacter() == 2)) {
						// block faces for now...
						continue;
					}

					result = applyMseFunc.apply(result, lowestMse);
					result = contrastReducer.apply(result);

					float localMse = (result.getMse() * weight);
					if (localMse < lowestMse) {
						lowestMse = localMse;
						lowestResult = result;
					}
				}

				if (lowestResult != null) {
					if (!lowestResult.isHasStat()) {
						statlessResult = min(statlessResult, lowestResult);
						lowestGlobalMse = lowestMse;
					}
					statfulResult = min(statfulResult, lowestResult);
				}
			}

			// apply statless result to board
			if (statlessResult == null) {
				throw new RuntimeException();
			}

			previewResults[iy * width + ix] = statlessResult;
			board.setElement(x + ix, y + iy, statlessResult.getElement());
			board.setColor(x + ix, y + iy, statlessResult.isText() ? statlessResult.getCharacter() : statlessResult.getColor());

			if (statfulResult.isHasStat() && statfulResult.getMse() < statlessResult.getMse()) {
				// lowest result has stat, add to statfulStrategies
				statfulStrategies.add(new Pair<>(new Coord2D(ix, iy), statfulResult));
			}
		});

		// apply statful strategies - lowest to highest MSE
		statfulStrategies.sort(Comparator.comparing(c -> {
			ElementResult past = previewResults[c.getFirst().getY() * width + c.getFirst().getX()];
			ElementResult proposed = c.getSecond();
			return proposed.getMse() - past.getMse();
		}));

		for (int i = 0; i < maxStatCount; i++) {
			if (i >= statfulStrategies.size()) {
				break;
			}
			Pair<Coord2D, ElementResult> pair = statfulStrategies.get(i);
			Coord2D coords = pair.getFirst();
			ElementResult result = pair.getSecond();

			// only one mode - set stat P1
			previewResults[coords.getY() * width + coords.getX()] = result;
			board.setElement(x + coords.getX(), y + coords.getY(), result.getElement());
			board.setColor(x + coords.getX(), y + coords.getY(), result.getColor());
			Stat stat = new Stat();
			stat.setX(x + coords.getX());
			stat.setY(y + coords.getY());
			stat.setCycle(1); // maybe we can reduce this to save CPU cycles?
			stat.setP1(result.getCharacter());
			board.addStat(stat);
		}

		if (previewRenderer != null) {
			preview = previewRenderer.render(60, 25, (px, py) -> {
				if ((px + 1) == playerX && (py + 1) == playerY) {
					return 2;
				}
				int ix = (px + 1) - x;
				int iy = (py + 1) - y;
				if (ix >= 0 && iy >= 0 && ix < width && iy < height) {
					return previewResults[iy * width + ix].getCharacter();
				} else {
					return 0;
				}
			}, (px, py) -> {
				if ((px + 1) == playerX && (py + 1) == playerY) {
					return 0x1F;
				}
				int ix = (px + 1) - x;
				int iy = (py + 1) - y;
				if (ix >= 0 && iy >= 0 && ix < width && iy < height) {
					return previewResults[iy * width + ix].getColor();
				} else {
					return 0;
				}
			});
		}

		return new Pair<>(board, preview);
	}
}
