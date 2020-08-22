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
import pl.asie.zzttools.util.Triplet;
import pl.asie.zzttools.zzt.*;

import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ImageConverter {
	private final TextVisualData visual;
	private final Platform platform;
	private final ElementResult emptyResult;
	private final ImageMseCalculator mseCalculator;

	public ImageConverter(TextVisualData visual, Platform platform, ImageMseCalculator mseCalculator) {
		this.visual = visual;
		this.platform = platform;
		this.emptyResult = new ElementResult(platform.getLibrary().getEmpty(), false, false, 0, 0);
		this.mseCalculator = mseCalculator;
	}

	public Pair<Board, BufferedImage> convert(BufferedImage inputImage, ImageConverterRuleset ruleset,
	                                          int x, int y, int width, int height, int playerX, int playerY, int maxStatCount, boolean blinkingDisabled,
	                                          IntPredicate charCheck, IntPredicate colorCheck,
	                                          TextVisualRenderer previewRenderer,
	                                          ProgressCallback progressCallback) {
		Board board = new Board(platform, playerX, playerY);
		BufferedImage preview = null;

		int pixelWidth = width * visual.getCharWidth();
		int pixelHeight = height * visual.getCharHeight();

		if (inputImage.getWidth() != pixelWidth || inputImage.getHeight() != pixelHeight) {
			inputImage = ImageUtils.scale(inputImage, pixelWidth, pixelHeight, AffineTransformOp.TYPE_BICUBIC);
		}
		final BufferedImage image = inputImage;

		List<Triplet<Coord2D, ElementResult, Float>> statfulStrategies = new ArrayList<>();
		Map<ElementRule, List<ElementResult>> ruleResultMap = new LinkedHashMap<>();
		ElementResult[] previewResults = new ElementResult[width * height];
		float[] previewMse = new float[width * height];
		final int progressSize = width * height;

		// generate ruleResultMap
		for (ElementRule rule : ruleset.getRules()) {
			if (rule.getStrategy().isRequiresStat() && maxStatCount <= 0) {
				// no stats - no stat strategies!
				continue;
			}

			Stream<ElementResult> proposals = null;
			switch (rule.getStrategy()) {
				case EMPTY:
					proposals = Stream.of(emptyResult);
					break;
				case ELEMENT:
					if (charCheck != null && !charCheck.test(rule.getChr())) {
						continue;
					}
					proposals = IntStream.range(0, 256).filter(i -> {
						if (colorCheck != null && !colorCheck.test(i)) {
							return false;
						}

						return true;
					}).mapToObj(i -> new ElementResult(rule.getElement(), false, false, rule.getChr(), i));
					break;
				case TEXT:
					if (colorCheck != null && !colorCheck.test(rule.getColor())) {
						continue;
					}
					proposals = IntStream.of(ruleset.getAllowedTextCharIndices()).filter(i -> {
						if (charCheck != null && !charCheck.test(i)) {
							return false;
						}

						return true;
					}).mapToObj(i -> new ElementResult(rule.getElement(), false, true, i, rule.getColor()));
					break;
				case USE_STAT_P1:
					proposals = IntStream.of(ruleset.getAllowedObjectIndices()).filter(i -> {
						if (charCheck != null && !charCheck.test(i & 0xFF)) {
							return false;
						}

						if (colorCheck != null && !colorCheck.test(i >> 8)) {
							return false;
						}

						return true;
					}).mapToObj(i -> new ElementResult(rule.getElement(), true, false, i & 0xFF, i >> 8));
					break;
			}

			ruleResultMap.put(rule, proposals.collect(Collectors.toList()));
		}

		IntStream.range(0, width * height).parallel().forEach(pos -> {
			synchronized (progressCallback) {
				progressCallback.step(progressSize);
			}

			int ix = pos % width;
			int iy = pos / width;
			boolean spaceForbidden = ((x + ix) < 1 || (y + iy) < 1 || (x + ix) > platform.getBoardWidth() || (y + iy) > platform.getBoardHeight());

			if ((x + ix) == playerX && (y + iy) == playerY) {
				spaceForbidden = true;
			}

			if (spaceForbidden) {
				previewResults[iy * width + ix] = emptyResult;
				return;
			}

			ElementResult statlessResult = null;
			float statlessMse = Float.MAX_VALUE;
			ElementResult statfulResult = null;
			float statfulMse = Float.MAX_VALUE;
			ImageMseCalculator.Applier applyMseFunc = mseCalculator.applyMse(image, ix * visual.getCharWidth(), iy * visual.getCharHeight());

			for (Map.Entry<ElementRule, List<ElementResult>> ruleResult : ruleResultMap.entrySet()) {
				List<ElementResult> proposals = ruleResult.getValue();

				float lowestLocalMse = statlessMse;
				ElementResult lowestLocalResult = null;
				float weight = 1.0f;

				for (ElementResult result : proposals) {
					float localMse = applyMseFunc.apply(result, lowestLocalMse) * weight;
					if (localMse < lowestLocalMse) {
						lowestLocalMse = localMse;
						lowestLocalResult = result;
					}
				}

				if (lowestLocalResult != null) {
					if (!lowestLocalResult.isHasStat()) {
						if (lowestLocalMse < statlessMse) {
							statlessResult = lowestLocalResult;
							statlessMse = lowestLocalMse;
						}
					}
					if (lowestLocalMse < statfulMse) {
						statfulResult = lowestLocalResult;
						statfulMse = lowestLocalMse;
					}
				}
			}

			// apply statless result to board
			if (statlessResult == null) {
				throw new RuntimeException();
			}

			int idx = iy * width + ix;
			previewResults[idx] = statlessResult;
			previewMse[idx] = statlessMse;
			board.setElement(x + ix, y + iy, statlessResult.getElement());
			board.setColor(x + ix, y + iy, statlessResult.isText() ? statlessResult.getCharacter() : statlessResult.getColor());

			if (statfulResult.isHasStat() && statfulMse < statlessMse) {
				synchronized (statfulStrategies) {
					// lowest result has stat, add to statfulStrategies
					statfulStrategies.add(new Triplet<>(new Coord2D(ix, iy), statfulResult, statfulMse));
				}
			}
		});

		// apply statful strategies - lowest to highest MSE
		statfulStrategies.sort(Comparator.comparing(c -> {
			float pastMse = previewMse[c.getFirst().getY() * width + c.getFirst().getX()];
		    float proposedMse = c.getThird();
			return proposedMse - pastMse;
		}));

		int realMaxStatCount = Math.min(maxStatCount, platform.getMaxStatCount());
		for (int i = 0; i < realMaxStatCount; i++) {
			if (i >= statfulStrategies.size()) {
				break;
			}
			Triplet<Coord2D, ElementResult, Float> strategyData = statfulStrategies.get(i);
			Coord2D coords = strategyData.getFirst();
			ElementResult result = strategyData.getSecond();

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
			preview = previewRenderer.render(platform.getBoardWidth(), platform.getBoardHeight(), (px, py) -> {
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
					int color = previewResults[iy * width + ix].getColor();
					return blinkingDisabled ? color : (color & 0x7F);
				} else {
					return 0;
				}
			});
		}

		return new Pair<>(board, preview);
	}
}
