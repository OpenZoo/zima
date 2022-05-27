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

import lombok.RequiredArgsConstructor;

// TODO: Properly support Super ZZT quirks.
@RequiredArgsConstructor
public class ElementDrawFunctionConnected implements ElementDrawFunction {
	public static final ElementDrawFunctionConnected LINE = new ElementDrawFunctionConnected(
			new int[] { 249, 208, 210, 186, 181, 188, 187, 185, 198, 200, 201, 204, 205, 202, 203, 206 }
	);

	private final int[] chars;

	private boolean matches(Board board, int x, int y) {
		Element LINE = board.getEngineDefinition().getElements().byInternalName("LINE");
		Element BOARD_EDGE = board.getEngineDefinition().getElements().byInternalName("BOARD_EDGE");
		Element element = board.getElement(x, y);
		if (element == LINE || element == BOARD_EDGE) {
			return true;
		} else {
			return false;
		}
	}
	@Override
	public Result draw(Board board, int x, int y) {
		return Result.character(chars[
				(matches(board, x, y - 1) ? 1 : 0)
				| (matches(board, x, y + 1) ? 2 : 0)
				| (matches(board, x - 1, y) ? 4 : 0)
				| (matches(board, x + 1, y) ? 8 : 0)
		]);
	}
}
