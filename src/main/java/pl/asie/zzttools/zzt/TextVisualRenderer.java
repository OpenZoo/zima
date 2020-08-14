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
package pl.asie.zzttools.zzt;

import java.awt.image.BufferedImage;

public class TextVisualRenderer {
	@FunctionalInterface
	public interface ByteGetter {
		int get(int x, int y);
	}

	private final TextVisualData visual;

	public TextVisualRenderer(TextVisualData visual) {
		this.visual = visual;
	}

	/* public BufferedImage render(BoardAnimation animation, int frame) {
		return render(animation.getWidth(), animation.getHeight(), (x, y) -> animation.get(x, y, frame), (x, y) -> 0x0F);
	}

	public BufferedImage render(ExColorBoardAnimation animation, int frame) {
		return render(animation.getWidth(), animation.getHeight(), (x, y) -> ExColorBoardAnimationRenderer.elementToChar(animation.get(x, y, frame).getElement()), (x, y) -> animation.get(x, y, frame).getColorIndex());
	} */

	public BufferedImage render(int width, int height, ByteGetter charGetter, ByteGetter colorGetter) {
		BufferedImage image = new BufferedImage(width * visual.getCharWidth(), height * visual.getCharHeight(), BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int chr = charGetter.get(x, y);
				int col = colorGetter.get(x, y);
				int bgCol = visual.getPalette()[(col >> 4) & 0x0F];
				int fgCol = visual.getPalette()[col & 0x0F];
				byte[] charDataArray = visual.getCharData();
				int charDataOffset = chr * visual.getCharHeight();

				for (int cy = 0; cy < visual.getCharHeight(); cy++) {
					int charData = (int) charDataArray[charDataOffset + cy] & 0xFF;
					for (int cx = 0; cx < visual.getCharWidth(); cx++) {
						image.setRGB(x * visual.getCharWidth() + cx, y * visual.getCharHeight() + cy, ((charData & (1 << (7 - cx))) != 0) ? fgCol : bgCol);
					}
				}
			}
		}
		return image;
	}
}
