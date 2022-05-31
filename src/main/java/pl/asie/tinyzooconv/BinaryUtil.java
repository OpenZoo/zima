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
package pl.asie.tinyzooconv;

import pl.asie.tinyzooconv.exceptions.IdNotFoundException;
import pl.asie.tinyzooconv.exceptions.TooManyIdsException;

import java.util.List;
import java.util.Map;

public class BinaryUtil {
	private BinaryUtil() {

	}

	public static <T> int getId(String type, T token, List<T> tokens) throws IdNotFoundException {
		int index = tokens.indexOf(token);
		if (index < 0) {
			throw new IdNotFoundException(type, token, tokens);
		}
		return index;
	}

	public static <T> int getId(String type, T token, List<T> tokens, Map<T, Integer> specialTokens, Integer emptyValue) throws IdNotFoundException {
		//noinspection SuspiciousMethodCalls
		if (token == null || ((token instanceof String) && ((String) token).isEmpty() && !specialTokens.containsKey(""))) {
			if (emptyValue != null) {
				return emptyValue;
			}
		}
		if (specialTokens.containsKey(token)) {
			return specialTokens.get(token);
		}
		return getId(type, token, tokens);
	}

	public static void validateIdCount(String type, int expected, int actual) throws TooManyIdsException {
		if (actual > expected) {
			throw new TooManyIdsException(type, expected, actual);
		}
	}
}
