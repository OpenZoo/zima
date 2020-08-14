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
	private final int[] charLutPrecalc;
	private final int[][] charLutIndexPrecalc;

	private static class ImageLutHolder {
		private final int[] lutData;

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
		}
	}

	public TrixImageMseCalculator(TextVisualData visual) {
		this.visual = visual;

		charLutPrecalc = new int[256 * 16];
		for (int i = 0; i < 256 * 16; i++) {
			int bg = visual.getPalette()[(i >> 8) & 0x0F];
			int fg = visual.getPalette()[(i >> 4) & 0x0F];
			charLutPrecalc[i] = ColorUtils.mix4equal(
					(i & 1) != 0 ? fg : bg,
					(i & 2) != 0 ? fg : bg,
					(i & 4) != 0 ? fg : bg,
					(i & 8) != 0 ? fg : bg
			);
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
	}
	
	@Override
	public Applier applyMse(BufferedImage image, int px, int py) {
		final ImageLutHolder holder = new ImageLutHolder(image, px, py, visual.getCharWidth(), visual.getCharHeight());
		return (proposed, maxMse) -> {
			float mse = 0.0f;
			int[] imageLutData = holder.lutData;
			int[] charLutData = charLutIndexPrecalc[proposed.getCharacter()];

			for (int i = 0; i < imageLutData.length; i++) {
				int charLutIdx = charLutData[i];
				int charHalfLut = charLutPrecalc[proposed.getColor() << 4 | charLutIdx];
				float dist = ColorUtils.distance(charHalfLut, imageLutData[i]);
				mse += dist;
				if (mse > maxMse) {
					break;
				}
			}

			proposed.setMse(mse);
			return proposed;
		};
	}
}
