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

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@FunctionalInterface
public interface ElementDrawFunction {
	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	class Result {
		public static final Result NONE = new Result(-1, -1);

		private final int character, color;

		public static Result character(int ch) {
			return new Result(ch, -1);
		}

		public static Result color(int co) {
			return new Result(-1, co);
		}

		public static Result create(int ch, int co) {
			return new Result(ch, co);
		}
	}

	Result draw(Board board, int x, int y);
}
