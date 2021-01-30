/**
 * Copyright (c) 2020, 2021 Adrian Siekierka
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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class TextVisualData {
	private final int charWidth;
	private final int charHeight;
	private final byte[] charData;
	private final int[] palette;

	public boolean isCharEmpty(int c) {
		for (int i = c * charHeight; i < (c + 1) * charHeight; i++) {
			if (charData[i] != 0) {
				return false;
			}
		}
		return true;
	}

	public boolean isCharFull(int c) {
		for (int i = c * charHeight; i < (c + 1) * charHeight; i++) {
			if (charData[i] != ((byte) 0xFF)) {
				return false;
			}
		}
		return true;
	}
}
