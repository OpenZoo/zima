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

import pl.asie.zzttools.util.ColorUtils;
import pl.asie.zzttools.zzt.TextVisualData;

import java.awt.image.BufferedImage;

public class TrixImageMseCalculator implements ImageMseCalculator {
	private final TextVisualData visual;
	private final float contrastReduction;
	private final int[] charLutPrecalc;
	private final int[][] charLutIndexPrecalc;
	private final float[] colDistPrecalc;

	private static class ImageLutHolder {
		private final int[] lutData;
		private float maxDistance;

		public ImageLutHolder(BufferedImage image, int px, int py, int width, int height) {
			lutData = new int[(width >> 1) * (height >> 1)];
			for (int cy = 0; cy < height; cy+=2) {
				for (int cx = 0; cx < width; cx+=2) {
					lutData[(cy >> 1) * (width >> 1) + (cx >> 1)] = ColorUtils.mix4equal(
							image.getRGB(px + cx, py + cy),
							image.getRGB(px + cx + 1, py + cy),
							image.getRGB(px + cx, py + cy + 1),
							image.getRGB(px + cx + 1, py + cy + 1)
					);
				}
			}

			maxDistance = 0.0f;
			for (int i = 0; i < lutData.length; i++) {
				for (int j = i + 1; j < lutData.length; j++) {
					float distance = ColorUtils.distance(lutData[i], lutData[j]);
					if (distance > maxDistance) {
						maxDistance = distance;
					}
				}
			}
		}
	}

	public TrixImageMseCalculator(TextVisualData visual, float contrastReduction) {
		this.visual = visual;
		this.contrastReduction = contrastReduction;

		charLutPrecalc = new int[256 * 16];
		for (int i = 0; i < 256 * 16; i++) {
			int bg = visual.getPalette()[(i >> 8) & 0x0F];
			int fg = visual.getPalette()[(i >> 4) & 0x0F];
			int fgColored = (i & 1) + ((i >> 1) & 1) + ((i >> 2) & 1) + ((i >> 3) & 1);
			charLutPrecalc[i] = ColorUtils.mix(bg, fg, fgColored / 4.0f);
		}

		charLutIndexPrecalc = new int[256][(visual.getCharWidth() >> 1) * (visual.getCharHeight() >> 1)];
		for (int c = 0; c < 256; c++) {
			int coff = c * visual.getCharHeight();
			for (int cy = 0; cy < visual.getCharHeight(); cy += 2) {
				int charLine1 = (int) visual.getCharData()[coff + cy] & 0xFF;
				int charLine2 = (int) visual.getCharData()[coff + cy + 1] & 0xFF;

				for (int cx = 0; cx < visual.getCharWidth(); cx += 2) {
					int charLutIdx = ((charLine1 >> (6 - cx)) & 3) | (((charLine2 >> (6 - cx)) & 3) << 2);
					charLutIndexPrecalc[c][(cy >> 1) * (visual.getCharWidth() >> 1) + (cx >> 1)] = charLutIdx;
				}
			}
		}

		colDistPrecalc = new float[256];
		for (int i = 0; i < 256; i++) {
			int bg = visual.getPalette()[(i >> 4) & 0x0F];
			int fg = visual.getPalette()[i & 0x0F];
			colDistPrecalc[i] = ColorUtils.distance(bg, fg);
		}
	}
	
	@Override
	public Applier applyMse(BufferedImage image, int px, int py) {
		final ImageLutHolder holder = new ImageLutHolder(image, px, py, visual.getCharWidth(), visual.getCharHeight());
		return (proposed, maxMse) -> {
			int chr = proposed.getCharacter();
			int col = proposed.getColor();

			float mse = 0.0f;
			int[] imageLutData = holder.lutData;
			int[] charLutData = charLutIndexPrecalc[chr];

			for (int i = 0; i < imageLutData.length; i++) {
				int charLutIdx = charLutData[i];
				int charHalfLut = charLutPrecalc[col << 4 | charLutIdx];
				float dist = ColorUtils.distance(charHalfLut, imageLutData[i]);
				mse += dist;
				if (mse > maxMse) {
					break;
				}
			}

			int bg = visual.getPalette()[(col >> 4) & 0x0F];
			int fg = visual.getPalette()[col & 0x0F];
			float imgContrast = holder.maxDistance;
			float chrContrast = colDistPrecalc[col];

			mse += imageLutData.length * contrastReduction * Math.abs(imgContrast - chrContrast);

			proposed.setMse(mse);
			return proposed;
		};
	}
}
