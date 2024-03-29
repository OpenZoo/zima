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
package pl.asie.libzzt;


import java.awt.image.BufferedImage;

public class TextVisualRenderer {
	@FunctionalInterface
	public interface ByteGetter {
		int get(int x, int y);
	}

	private final TextVisualData visual;
	private final boolean doubleWide;

	public TextVisualRenderer(TextVisualData visual, boolean doubleWide) {
		this.visual = visual;
		this.doubleWide = doubleWide;
	}

	private ByteGetter applyPreviewToColor(ByteGetter getter, boolean preview) {
		if (preview) {
			return (x, y) -> {
				int c = getter.get(x, y);
				return c == 0x00 ? 0x80 : c;
			};
		} else {
			return getter;
		}
	}

	public BufferedImage render(Board board, boolean preview) {
		return render(board, preview, 1, 1, board.getWidth(), board.getHeight());
	}

	public BufferedImage render(Board board, boolean preview, int xOfs, int yOfs, int width, int height) {
		return render(width, height, (x, y) -> {
			Element element = board.getElement(x + xOfs, y + yOfs);
			if (element.getDrawFunction() != null) {
				ElementDrawFunction.Result result = element.getDrawFunction().draw(board, x + xOfs, y + yOfs);
				if (result != null && result.getCharacter() >= 0) {
					return result.getCharacter();
				}
			}
			if (element.isText()) {
				return board.getColor(x + xOfs, y + yOfs);
			} else {
				return element.getCharacter();
			}
		}, applyPreviewToColor((x, y) -> {
			Element element = board.getElement(x + xOfs, y + yOfs);
			if (element.getDrawFunction() != null) {
				ElementDrawFunction.Result result = element.getDrawFunction().draw(board, x + xOfs, y + yOfs);
				if (result != null && result.getColor() >= 0) {
					return result.getColor();
				}
			}
			if (element.isText()) {
				return element.getColor();
			} else {
				return board.getColor(x + xOfs, y + yOfs);
			}
		}, preview));
	}

	/* public BufferedImage render(BoardAnimation animation, int frame) {
		return render(animation.getWidth(), animation.getHeight(), (x, y) -> animation.get(x, y, frame), (x, y) -> 0x0F);
	}

	public BufferedImage render(ExColorBoardAnimation animation, int frame) {
		return render(animation.getWidth(), animation.getHeight(), (x, y) -> ExColorBoardAnimationRenderer.elementToChar(animation.get(x, y, frame).getElement()), (x, y) -> animation.get(x, y, frame).getColorIndex());
	} */

	public BufferedImage render(int width, int height, ByteGetter charGetter, ByteGetter colorGetter) {
		int charXInc = (isDoubleWide() ? 2 : 1);
		int charWidth = visual.getCharWidth() * charXInc;
		int charHeight = visual.getCharHeight();
		BufferedImage image = new BufferedImage(width * charWidth, height * charHeight, BufferedImage.TYPE_INT_RGB);
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
					int i = 7;
					for (int cx = 0; cx < charWidth; cx += charXInc, i--) {
						int ccol = ((charData & (1 << i)) != 0) ? fgCol : bgCol;
						image.setRGB(x * charWidth + cx, y * charHeight + cy, ccol);
						if (charXInc > 1) {
							image.setRGB(x * charWidth + cx + 1, y * charHeight + cy, ccol);
						}
					}
				}
			}
		}
		return image;
	}

	private boolean isDoubleWide() {
		return this.doubleWide && visual.getCharHeight() >= (visual.getCharWidth() * 3 / 2);
	}
}
