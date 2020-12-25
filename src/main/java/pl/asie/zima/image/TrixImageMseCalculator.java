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
package pl.asie.zima.image;

import pl.asie.zima.util.ColorUtils;
import pl.asie.libzzt.TextVisualData;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TrixImageMseCalculator implements ImageMseCalculator {
	private final TextVisualData visual;
	private final float contrastReduction;
	private final float accurateApproximate;
	private final int[] charLutPrecalc;
	private final int[][] charLut2x2Precalc;
	private final boolean[][] charLut1x1Precalc;
	private final float[] colDistPrecalc;
	private final boolean blinkingDisabled;
	private final Set<Integer> blendingChars = new HashSet<>();

	private static class ImageLutHolder {
		private final float[][] dataMacro1x1;
		private final int[] dataMacro2x2;
		private float maxDistance;

		public ImageLutHolder(TextVisualData visual, BufferedImage image, int px, int py, int width, int height) {
			dataMacro1x1 = new float[width * height][16];
			int[] rgbCache = new int[width * height];
			int offs = 0;
			for (int cy = 0; cy < height; cy++) {
				for (int cx = 0; cx < width; cx++, offs++) {
					int col = image.getRGB(px + cx, py + cy);
					float[] lut = dataMacro1x1[offs];
					rgbCache[offs] = col;
					for (int cc = 0; cc < 16; cc++) {
						lut[cc] = ColorUtils.distance(col, visual.getPalette()[cc]);
					}
				}
			}

			dataMacro2x2 = new int[(width >> 1) * (height >> 1)];
			offs = 0;
			for (int cy = 0; cy < height; cy+=2) {
				for (int cx = 0; cx < width; cx+=2, offs++) {
					int offs2 = cy * width + cx;
					dataMacro2x2[offs] = ColorUtils.mix4equal(
							rgbCache[offs2],
							rgbCache[offs2 + 1],
							rgbCache[offs2 + width],
							rgbCache[offs2 + width + 1]
					);
				}
			}

			maxDistance = 0.0f;
			for (int i = 0; i < dataMacro2x2.length; i++) {
				for (int j = i + 1; j < dataMacro2x2.length; j++) {
					maxDistance = Math.max(maxDistance, ColorUtils.distance(dataMacro2x2[i], dataMacro2x2[j]));
				}
			}
		}
	}

	public TrixImageMseCalculator(TextVisualData visual, boolean blinkingDisabled, float contrastReduction, float accurateApproximate) {
		this.visual = visual;
		this.contrastReduction = contrastReduction;
		this.accurateApproximate = accurateApproximate;
		this.blinkingDisabled = blinkingDisabled;

		charLutPrecalc = new int[256 * 16];
		for (int i = 0; i < 256 * 16; i++) {
			int bg = visual.getPalette()[(i >> 8) & 0x0F];
			int fg = visual.getPalette()[(i >> 4) & 0x0F];
			int fgColored = (i & 1) + ((i >> 1) & 1) + ((i >> 2) & 1) + ((i >> 3) & 1);
			charLutPrecalc[i] = ColorUtils.mix(bg, fg, fgColored / 4.0f);
		}

		int charLut2x2Count = (visual.getCharWidth() >> 1) * (visual.getCharHeight() >> 1);
		charLut2x2Precalc = new int[256][charLut2x2Count];
		for (int c = 0; c < 256; c++) {
			int coff = c * visual.getCharHeight();
			int[] charLut2x2PrecalcLocal = charLut2x2Precalc[c];

			for (int cy = 0; cy < visual.getCharHeight(); cy += 2) {
				int charLine1 = (int) visual.getCharData()[coff + cy] & 0xFF;
				int charLine2 = (int) visual.getCharData()[coff + cy + 1] & 0xFF;

				for (int cx = 0; cx < visual.getCharWidth(); cx += 2) {
					int charLutIdx = ((charLine1 >> (6 - cx)) & 3) | (((charLine2 >> (6 - cx)) & 3) << 2);
					charLut2x2PrecalcLocal[(cy >> 1) * (visual.getCharWidth() >> 1) + (cx >> 1)] = charLutIdx;
				}
			}
		}

		charLut1x1Precalc = new boolean[256][visual.getCharWidth() * visual.getCharHeight()];
		for (int c = 0; c < 256; c++) {
			int coff = c * visual.getCharHeight();
			boolean[] charLut1x1PrecalcLocal = charLut1x1Precalc[c];

			for (int cy = 0; cy < visual.getCharHeight(); cy++) {
				int charLine = (int) visual.getCharData()[coff + cy] & 0xFF;
				for (int cx = 0; cx < visual.getCharWidth(); cx++) {
					charLut1x1PrecalcLocal[cy * visual.getCharWidth() + cx] = (charLine & (1 << (7 - cx))) != 0;
				}
			}
		}

		colDistPrecalc = new float[256];
		for (int i = 0; i < 256; i++) {
			int bg = visual.getPalette()[i >> 4];
			int fg = visual.getPalette()[i & 0x0F];
			colDistPrecalc[i] = ColorUtils.distance(bg, fg);
		}

		blendingChars.add(176);
		blendingChars.add(177);
		blendingChars.add(178);
		for (int i = 0; i < 256; i++) {
			if (visual.isCharEmpty(i) || visual.isCharFull(i)) {
				blendingChars.add(i);
			}
		}
	}
	
	@Override
	public Applier applyMse(BufferedImage image, int px, int py) {
		final ImageLutHolder holder = new ImageLutHolder(visual, image, px, py, visual.getCharWidth(), visual.getCharHeight());
		float[] mseContrastPrecalc = new float[256];
		float[] macroRatioPrecalc = new float[256];
		for (int i = 0; i < 256; i++) {
			float imgContrast = holder.maxDistance;
			float chrContrast = colDistPrecalc[i];
			float contrastDiff = (imgContrast - chrContrast);
			mseContrastPrecalc[i] = contrastReduction * contrastDiff * contrastDiff;

			macroRatioPrecalc[i] = blendingChars.contains(i) ? 1.0f : accurateApproximate;
		}
		final int colorMask = blinkingDisabled ? 0xFF : 0x7F;
		return (proposed, maxMse) -> {
			int chr = proposed.getCharacter();
			int col = proposed.getColor() & colorMask;

			float mse = 0.0f;
			int[] dataMacro2x2 = holder.dataMacro2x2;

			float mseContrastReduction = mseContrastPrecalc[col];
			float macroRatio = macroRatioPrecalc[chr];

			mse += dataMacro2x2.length * mseContrastReduction;
			if (mse <= maxMse) {
				if (macroRatio < 1.0f) {
					float invMacroRatio = ((1 - macroRatio) * 0.25f);
					float[][] dataMacro1x1 = holder.dataMacro1x1;
					boolean[] charData = charLut1x1Precalc[chr];
					int bg = (col >> 4);
					int fg = (col & 0x0F);

					for (int dm1p = 0; dm1p < charData.length; dm1p++) {
						int charColor = charData[dm1p] ? fg : bg;
						mse += dataMacro1x1[dm1p][charColor] * invMacroRatio;
						if (mse > maxMse) {
							return mse;
						}
					}
				}

				if (macroRatio > 0.0f) {
					int[] charLutData = charLut2x2Precalc[chr];

					for (int i = 0; i < dataMacro2x2.length; i++) {
						int charLutIdx = charLutData[i];
						int char2x2Lut = charLutPrecalc[col << 4 | charLutIdx];
						float dist2x2 = ColorUtils.distance(char2x2Lut, dataMacro2x2[i]);
						mse += dist2x2 * macroRatio;
						if (mse > maxMse) {
							return mse;
						}
					}
				}
			}

			return mse;
		};
	}
}
