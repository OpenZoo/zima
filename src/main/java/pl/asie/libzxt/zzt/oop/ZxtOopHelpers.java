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
package pl.asie.libzxt.zzt.oop;

import pl.asie.libzzt.oop.OopParserContext;
import pl.asie.libzzt.oop.OopUtils;

public final class ZxtOopHelpers {
	private ZxtOopHelpers() {

	}

	public static long readHex(OopParserContext context, int bits) {
		long result = 0;

		context.readChar();
		while (context.getChar() == ' ') {
			context.readChar();
		}

		while (true) {
			int c = OopUtils.upCase(context.getChar());

			if (c >= '0' && c <= '9') {
				result = (result << 4) | (c - 48);
			} else if (c >= 'A' && c <= 'F') {
				result = (result << 4) | (c - 55);
			} else {
				break;
			}

			context.readChar();
		}

		return result & ((1L << bits) - 1);
	}
}
