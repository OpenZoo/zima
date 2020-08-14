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
import java.util.function.Function;

public class NaiveImageMseCalculator implements ImageMseCalculator {
	private final TextVisualData visual;

	public NaiveImageMseCalculator(TextVisualData visual) {
		this.visual = visual;
	}
	
	@Override
	public Applier applyMse(BufferedImage image, int px, int py) {
		final int[] imgColorLut = new int[visual.getCharWidth() * visual.getCharHeight()];
		for (int cy = 0; cy < visual.getCharHeight(); cy++) {
			for (int cx = 0; cx < visual.getCharWidth(); cx++) {
				imgColorLut[cy * visual.getCharWidth() + cx] = image.getRGB(px + cx, py + cy);
			}
		}

		return (proposed, maxMse) -> {
			float mse = 0.0f;

			int bgColor = visual.getPalette()[proposed.getColor() >> 4];
			int fgColor = visual.getPalette()[proposed.getColor() & 0x0F];
			int charColor = 0;
			boolean forcedCharColor = true;
			switch (proposed.getCharacter()) {
				case 176:
					charColor = ColorUtils.mix(fgColor, bgColor, 0.75f);
					break;
				case 177:
					charColor = ColorUtils.mix(fgColor, bgColor, 0.5f);
					break;
				case 178:
					charColor = ColorUtils.mix(fgColor, bgColor, 0.25f);
					break;
				default:
					forcedCharColor = false;
					break;
			}

			int coff = proposed.getCharacter() * visual.getCharHeight();
			int ci = 0;
			for (int cy = 0; cy < visual.getCharHeight(); cy++) {
				int charLine = (int) visual.getCharData()[coff + cy] & 0xFF;
				for (int cx = 0; cx < visual.getCharWidth(); cx++) {
					int imgColor = imgColorLut[ci++];
					if (!forcedCharColor) {
						charColor = (charLine & (1 << (7 - cx))) != 0 ? fgColor : bgColor;
					}
					mse += ColorUtils.distance(imgColor, charColor);
					if (mse > maxMse) {
						break;
					}
				}
			}

			return proposed.withMse(mse);
		};
	}
}
