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
package pl.asie.libzzt.oop;

import pl.asie.libzzt.Board;
import pl.asie.libzzt.Stat;

import java.util.stream.Stream;

public final class OopUtils {
	private OopUtils() {

	}

	public static <T extends ChildrenIterable<T>> Stream<T> allChildren(Stream<T> commandStream) {
		return commandStream.flatMap(c -> Stream.concat(Stream.of(c), allChildren(c.getChildren().stream())));
	}

	public static String stripChars(String value) {
		StringBuilder newValue = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			int codePoint = value.codePointAt(i);
			if (codePoint >= 'A' && codePoint <= 'Z') {
				newValue.appendCodePoint(codePoint);
			} else if (codePoint >= '0' && codePoint <= '9') {
				newValue.appendCodePoint(codePoint);
			} else if (codePoint >= 'a' && codePoint <= 'z') {
				newValue.appendCodePoint(codePoint + 'A' - 'a');
			}
		}
		return newValue.toString();
	}
}
