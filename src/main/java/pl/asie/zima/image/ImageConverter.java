/**
 * Copyright (c) 2020, 2021, 2022 Adrian Siekierka
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
package pl.asie.zima.image;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import pl.asie.libzzt.Board;
import pl.asie.libzzt.Element;
import pl.asie.zima.util.ZimaPlatform;
import pl.asie.libzzt.Stat;
import pl.asie.libzzt.TextVisualData;
import pl.asie.libzzt.TextVisualRenderer;
import pl.asie.libzzt.ZOutputStream;
import pl.asie.zima.util.ColorUtils;
import pl.asie.zima.util.Coord2D;
import pl.asie.zima.util.LengthMeasuringOutputStream;
import pl.asie.zima.util.DitherMatrix;
import pl.asie.zima.util.ImageUtils;
import pl.asie.zima.util.Pair;
import pl.asie.zima.util.Triplet;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ImageConverter {
	private final TextVisualData visual;
	private final ZimaPlatform platform;
	private final ElementResult emptyResult;
	private final ImageMseCalculator mseCalculator;

	public ImageConverter(TextVisualData visual, ZimaPlatform platform, ImageMseCalculator mseCalculator) {
		this.visual = visual;
		this.platform = platform;
		this.emptyResult = new ElementResult(platform.getLibrary().getEmpty(), false, false, 0, 0x0F);
		this.mseCalculator = mseCalculator;
	}

	@FunctionalInterface
	private interface ZOutputStreamConsumer {
		void accept(ZOutputStream stream) throws IOException;
	}

	private int count(ZOutputStreamConsumer streamConsumer) {
		try (LengthMeasuringOutputStream stream = new LengthMeasuringOutputStream(); ZOutputStream zStream = new ZOutputStream(stream, this.platform.getZztEngineDefinition())) {
			streamConsumer.accept(zStream);
			return stream.getDataLength();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@AllArgsConstructor
	public static class Result {
		@Getter
		private final Board board;
		@Getter
		private final int width;
		@Getter
		private final int height;
		private final ElementResult[] previewResults;

		public int getCharacter(int ix, int iy) {
			if (ix >= 0 && iy >= 0 && ix < width && iy < height) {
				return previewResults[iy * width + ix].getCharacter();
			} else {
				return 0;
			}
		}

		public int getColor(int ix, int iy) {
			if (ix >= 0 && iy >= 0 && ix < width && iy < height) {
				return previewResults[iy * width + ix].getColor();
			} else {
				return 0;
			}
		}
	}

	private Pair<Result, BufferedImage> convertBoardless(BufferedImage inputImage, int width, int height, boolean blinkingDisabled,
	                                                     IntPredicate charCheck, IntPredicate colorCheck,
	                                                     float coarseDitherStrength, DitherMatrix coarseDitherMatrixEnum,
	                                                     TextVisualRenderer previewRenderer,
	                                                     ProgressCallback progressCallback, boolean fast) {
		List<ElementResult> rules = new ArrayList<>(256 * 256);
		final int progressSize = width * height;
		ElementResult[] previewResults = new ElementResult[width * height];
		ElementResult emptyResult = null;
		BufferedImage preview = null;

		int acceptedChars = 0;
		for (int ich = 0; ich < 256; ich++) {
			if (fast && (ich != 32 && ich != 176 && ich != 177 && ich != 178 && ich != 219)) continue;
			if (charCheck != null && !charCheck.test(ich)) continue;
			for (int ico = 0; ico < 256; ico++) {
				if (colorCheck != null && !colorCheck.test(ico)) continue;

				// if BG == FG, we only need one char
				if (((ico >> 4) == (ico & 15)) && acceptedChars >= 1) {
					continue;
				}

				ElementResult result = new ElementResult(null, false, false, ich, ico);
				if (emptyResult == null) {
					emptyResult = result;
				}
				rules.add(result);
			}
			acceptedChars++;
		}

		if (emptyResult == null) {
			emptyResult = new ElementResult(null, false, false, 0, 0);
		}
		final ElementResult emptyResultFinal = emptyResult;

		float[] ditherMatrix = coarseDitherMatrixEnum != null ? coarseDitherMatrixEnum.getMatrix() : null;
		int ditherMatrixSize = coarseDitherMatrixEnum != null ? coarseDitherMatrixEnum.getDimSize() : 0;
		int ditherMatrixOffset = coarseDitherMatrixEnum != null ? coarseDitherMatrixEnum.getDimOffset() : 0;
		List<IntStream> blockIndexes = getBlockIndexes(width, height, coarseDitherStrength, coarseDitherMatrixEnum);

		final BufferedImage image = coarseDitherStrength > 0.0f ? ImageUtils.cloneRgb(inputImage) : inputImage;
		final Object ditherApplicationSync = new Object();

		// find lowest-MSE results for each tile, in parallel
		blockIndexes.forEach(idxs -> {
			idxs.parallel().forEach(pos -> {
				synchronized (progressCallback) {
					progressCallback.step(progressSize);
				}

				int ix = pos % width;
				int iy = pos / width;

				ElementResult minResult = emptyResultFinal;
				float minMse = Float.MAX_VALUE;
				int px = ix * visual.getCharWidth();
				int py = iy * visual.getCharHeight();
				ImageMseCalculator.Applier applyMseFunc = mseCalculator.applyMse(image, px, py);

				for (ElementResult result : rules) {
					float localMse = applyMseFunc.apply(result, minMse);
					if (localMse < minMse) {
						minMse = localMse;
						minResult = result;
					}
				}

				previewResults[pos] = minResult;

				if (coarseDitherStrength > 0.0f) {
					synchronized (ditherApplicationSync) {
						applyCoarseDither(coarseDitherStrength, ditherMatrix, ditherMatrixSize, ditherMatrixOffset, image, minResult, px, py);
					}
				}
			});
		});

		// result
		Result result = new Result(null, width, height, previewResults);

		// preview
		if (previewRenderer != null) {
			preview = previewRenderer.render(width, height, (ix, iy) -> {
				if (ix >= 0 && iy >= 0 && ix < width && iy < height) {
					return result.getCharacter(ix, iy);
				} else {
					return 0;
				}
			}, (ix, iy) -> {
				if (ix >= 0 && iy >= 0 && ix < width && iy < height) {
					int color = result.getColor(ix, iy);
					return blinkingDisabled ? color : (color & 0x7F);
				} else {
					return 0;
				}
			});
		}

		return new Pair<>(result, preview);
	}

	@Data
	private static class ElementRuleResult {
		private final ElementRule rule;
		private final List<ElementResult> result;
	}

	public Pair<Result, BufferedImage> convert(BufferedImage inputImage, ImageConverterRuleset ruleset,
											  int x, int y, int width, int height, int playerX, int playerY, int maxStatCount, boolean blinkingDisabled,
											  int maxBoardSize, float coarseDitherStrength, DitherMatrix coarseDitherMatrixEnum,
											  IntPredicate charCheck, IntPredicate colorCheck, int defStatCycle,
											  TextVisualRenderer previewRenderer,
											  ProgressCallback progressCallback, boolean fast) {
		if (!platform.isSupportsBlinking()) {
			blinkingDisabled = true;
		}
		final boolean blinkingDisabledFinal = blinkingDisabled;

		if (!platform.isUsesBoard()) {
			return convertBoardless(inputImage, width, height, blinkingDisabled, charCheck, colorCheck, coarseDitherStrength, coarseDitherMatrixEnum, previewRenderer, progressCallback, fast);
		}

		Board board = new Board(platform.getZztEngineDefinition(), playerX, playerY);
		BufferedImage preview = null;

		int pixelWidth = width * visual.getCharWidth();
		int pixelHeight = height * visual.getCharHeight();

		List<Triplet<Coord2D, ElementResult, Float>> statfulStrategies = new ArrayList<>();
		List<ElementRuleResult> ruleResultList = new ArrayList<>(ruleset.getRules().size());
		Set<Element> allowedElements = new HashSet<>();
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
			allowedElements.add(rule.getElement());
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

			ruleResultList.add(new ElementRuleResult(rule, proposals.collect(Collectors.toList())));
		}

		float[] ditherMatrix = coarseDitherMatrixEnum != null ? coarseDitherMatrixEnum.getMatrix() : null;
		int ditherMatrixSize = coarseDitherMatrixEnum != null ? coarseDitherMatrixEnum.getDimSize() : 0;
		int ditherMatrixOffset = coarseDitherMatrixEnum != null ? coarseDitherMatrixEnum.getDimOffset() : 0;
		List<IntStream> blockIndexes = getBlockIndexes(width, height, coarseDitherStrength, coarseDitherMatrixEnum);

		final BufferedImage image = coarseDitherStrength > 0.0f ? ImageUtils.cloneRgb(inputImage) : inputImage;
		final Object ditherApplicationSync = new Object();

		// find lowest-MSE results for each tile, in parallel
		blockIndexes.forEach(idxs -> {
			idxs.parallel().forEach(pos -> {
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
				int px = ix * visual.getCharWidth();
				int py = iy * visual.getCharHeight();
				ImageMseCalculator.Applier applyMseFunc = mseCalculator.applyMse(image, px, py);

				for (ElementRuleResult ruleResult : ruleResultList) {
					List<ElementResult> proposals = ruleResult.getResult();

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

				if (coarseDitherStrength > 0.0f) {
					ElementResult ditherResult = statfulResult != null ? statfulResult : statlessResult;
					synchronized (ditherApplicationSync) {
						applyCoarseDither(coarseDitherStrength, ditherMatrix, ditherMatrixSize, ditherMatrixOffset, image, ditherResult, px, py);
					}
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
		});

		// apply statful strategies - lowest to highest MSE
		statfulStrategies.sort(Comparator.comparing(c -> {
			float pastMse = previewMse[c.getFirst().getY() * width + c.getFirst().getX()];
		    float proposedMse = c.getThird();
			return proposedMse - pastMse;
		}));

		int realMaxStatCount = Math.min(maxStatCount, platform.getZztEngineDefinition().getMaxStatCount());
		int realMaxBoardSize = Math.min(maxBoardSize, platform.getZztEngineDefinition().getMaxBoardSize());
		int boardSerializationSize = count(board::writeZ);
		int addedStats = 0;

		Stat stat = new Stat();
		for (int i = 0; i < statfulStrategies.size(); i++) {
			Triplet<Coord2D, ElementResult, Float> strategyData = statfulStrategies.get(i);
			Coord2D coords = strategyData.getFirst();
			ElementResult result = strategyData.getSecond();

			ElementResult prevResult = previewResults[coords.getY() * width + coords.getX()];
			Element prevElement = board.getElement(x + coords.getX(), y + coords.getY());
			int prevColor = board.getColor(x + coords.getX(), y + coords.getY());

			// only one mode - set stat P1
			previewResults[coords.getY() * width + coords.getX()] = result;
			board.setElement(x + coords.getX(), y + coords.getY(), result.getElement());
			board.setColor(x + coords.getX(), y + coords.getY(), result.getColor());

			stat.setX(x + coords.getX());
			stat.setY(y + coords.getY());
			stat.setCycle(defStatCycle); // maybe we can reduce this to save CPU cycles?
			stat.setP1(result.getCharacter());

			if (boardSerializationSize < (realMaxBoardSize - 128)) {
				// optimization: don't recalc full board size if only RLE could be impacted
				boardSerializationSize += stat.lengthZ(platform.getZztEngineDefinition());
			} else {
				boardSerializationSize = count(board::writeZ) + stat.lengthZ(platform.getZztEngineDefinition());
			}
			if (boardSerializationSize > realMaxBoardSize) {
				previewResults[coords.getY() * width + coords.getX()] = prevResult;
				board.setElement(x + coords.getX(), y + coords.getY(), prevElement);
				board.setColor(x + coords.getX(), y + coords.getY(), prevColor);
			} else {
				board.addStat(stat);
				if ((++addedStats) >= realMaxStatCount) {
					break;
				}
				stat = new Stat();
			}
		}

		if (!fast) {
			// compression pass
			boolean canTrustSolids = visual.isCharFull(219);
			Element solidElement = platform.getLibrary().byInternalName("SOLID");
			if (!allowedElements.contains(solidElement)) solidElement = null;

			for (int iy = 0; iy < height; iy++) {
				for (int ix = 0; ix < width; ix++) {
					boolean spaceForbidden = ((x + ix) < 1 || (y + iy) < 1 || (x + ix) > platform.getBoardWidth() || (y + iy) > platform.getBoardHeight());

					if (!spaceForbidden) {
						Element element = board.getElement(x + ix, y + iy);
						int color = board.getColor(x + ix, y + iy);

						if (!element.isText() && (!element.isStat() || board.getStatAt(x + ix, y + iy) == null)) {
							if (!blinkingDisabled && color >= 0x80) {
								continue;
							}

							if (color == 0x00) {
								board.setElement(x + ix, y + iy, emptyResult.getElement());
								board.setColor(x + ix, y + iy, 0x0F);
							} else if ((element.getCharacter() == 219 || ((color >> 4) == (color & 0x0F))) && canTrustSolids) {
								if ((color & 0x0F) == 0x00) {
									board.setElement(x + ix, y + iy, emptyResult.getElement());
									board.setColor(x + ix, y + iy, 0x0F);
								} else {
									if (solidElement != null && (colorCheck == null || colorCheck.test(color & 0x0F))) {
										board.setElement(x + ix, y + iy, solidElement);
										board.setColor(x + ix, y + iy, color & 0x0F);
									}
								}
							}
						}
					}
				}
			}
		}

		// result
		Result result = new Result(board, width, height, previewResults);

		// preview
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
					return blinkingDisabledFinal ? color : (color & 0x7F);
				} else {
					return 0;
				}
			});
		}

		return new Pair<>(result, preview);
	}

	private List<IntStream> getBlockIndexes(int width, int height, float coarseDitherStrength, DitherMatrix coarseDitherMatrixEnum) {
		float[] ditherMatrix = coarseDitherMatrixEnum != null ? coarseDitherMatrixEnum.getMatrix() : null;
		int ditherMatrixSize = coarseDitherMatrixEnum != null ? coarseDitherMatrixEnum.getDimSize() : 0;
		int ditherMatrixOffset = coarseDitherMatrixEnum != null ? coarseDitherMatrixEnum.getDimOffset() : 0;

		List<IntStream> blockIndexes;
		if (coarseDitherStrength > 0.0f && ditherMatrix != null) {
			blockIndexes = new ArrayList<>();
			boolean[] addedIntegers = new boolean[width * height];
			int addedIntCount = 0;

			while (addedIntCount < addedIntegers.length) {
				List<Integer> integers = new ArrayList<>();

				for (int pos = 0; pos < width * height; pos++) {
					if (addedIntegers[pos]) continue;
					int ix = pos % width;
					int iy = pos / width;

					boolean ditherDepsFound = true;
					for (int dmp = 0; dmp < ditherMatrix.length; dmp++) {
						if (ditherMatrix[dmp] > 0.0f) {
							int dmx = (dmp % ditherMatrixSize) - ditherMatrixOffset;
							int dmy = (dmp / ditherMatrixSize) - ditherMatrixOffset;
							int dmix = ix - dmx;
							int dmiy = iy - dmy;
							if (dmix >= 0 && dmiy >= 0 && dmix < width && dmiy < height) {
								int dmipos = (dmiy * width) + dmix;
								if (!addedIntegers[dmipos]) {
									ditherDepsFound = false;
									break;
								}
							}
						}
					}

					if (ditherDepsFound) {
						integers.add(pos);
						addedIntCount++;
					}
				}

				if (integers.isEmpty()) {
					throw new RuntimeException("Unsatisfiable constraints for dither matrix!");
				} else {
					for (Integer pos : integers) {
						addedIntegers[pos] = true;
					}
					blockIndexes.add(integers.stream().mapToInt(i -> i));
				}
			}
		} else {
			blockIndexes = List.of(IntStream.range(0, width * height));
		}
		return blockIndexes;
	}

	private void applyCoarseDither(float coarseDitherStrength, float[] ditherMatrix, int ditherMatrixSize, int ditherMatrixOffset, BufferedImage image, ElementResult ditherResult, int px, int py) {
		float errorR = 0;
		float errorG = 0;
		float errorB = 0;
		int errorDiv = visual.getCharWidth() * visual.getCharHeight();
		int ebg = visual.getPalette()[ditherResult.getColor() >> 4];
		int efg = visual.getPalette()[ditherResult.getColor() & 0xF];
		for (int ey = 0; ey < visual.getCharHeight(); ey++) {
			int epy = py + ey;
			byte ech = visual.getCharData()[ditherResult.getCharacter() * visual.getCharHeight() + ey];
			for (int ex = 0; ex < visual.getCharWidth(); ex++) {
				int epx = px + ex;
				int imagePx = image.getRGB(epx, epy);

				int imageR = (imagePx >> 16) & 0xFF;
				int imageG = (imagePx >> 8) & 0xFF;
				int imageB = imagePx & 0xFF;

				int stratPx = (((ech >> (7 - ex)) & 0x1) != 0) ? efg : ebg;
				int stratR = (stratPx >> 16) & 0xFF;
				int stratG = (stratPx >> 8) & 0xFF;
				int stratB = stratPx & 0xFF;

				errorR += (ColorUtils.sRtoR(imageR) - ColorUtils.sRtoR(stratR));
				errorG += (ColorUtils.sRtoR(imageG) - ColorUtils.sRtoR(stratG));
				errorB += (ColorUtils.sRtoR(imageB) - ColorUtils.sRtoR(stratB));
			}
		}
		errorR /= errorDiv;
		errorG /= errorDiv;
		errorB /= errorDiv;
		for (int ey = 0; ey < visual.getCharHeight(); ey++) {
			int epy = py + ey;
			for (int ex = 0; ex < visual.getCharWidth(); ex++) {
				int epx = px + ex;
				for (int dmp = 0; dmp < ditherMatrix.length; dmp++) {
					if (ditherMatrix[dmp] > 0.0f) {
						int dmx = (dmp % ditherMatrixSize) - ditherMatrixOffset;
						int dmy = (dmp / ditherMatrixSize) - ditherMatrixOffset;
						int epmx = epx + (visual.getCharWidth() * dmx);
						int epmy = epy + (visual.getCharHeight() * dmy);
						if (epmx >= 0 && epmy >= 0 && epmx < image.getWidth() && epmy < image.getHeight()) {
							image.setRGB(epmx, epmy, ColorUtils.add(image.getRGB(epmx, epmy), errorR, errorG, errorB, ditherMatrix[dmp] * coarseDitherStrength));
						}
					}
				}
			}
		}
	}
}
