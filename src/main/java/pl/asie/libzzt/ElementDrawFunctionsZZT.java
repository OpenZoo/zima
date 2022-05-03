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

public class ElementDrawFunctionsZZT {
	protected static ElementDrawFunction.Result drawObject(Board board, int x, int y) {
		Stat stat = board.getStatAt(x, y);
		if (stat != null) {
			return ElementDrawFunction.Result.character(stat.getP1());
		} else {
			return ElementDrawFunction.Result.character(0);
		}
	}

	protected static ElementDrawFunction.Result drawTransporter(Board board, int x, int y) {
		Stat stat = board.getStatAt(x, y);
		if (stat != null) {
			if (stat.getStepX() == 0) {
				if (stat.getStepY() == 0) {
					return ElementDrawFunction.Result.character('-');
				} else {
					return ElementDrawFunction.Result.character(stat.getStepY() < 0 ? '^' : 'v');
				}
			} else {
				return ElementDrawFunction.Result.character(stat.getStepX() < 0 ? '<' : '>');
			}
		}
		return ElementDrawFunction.Result.NONE;
	}

}
