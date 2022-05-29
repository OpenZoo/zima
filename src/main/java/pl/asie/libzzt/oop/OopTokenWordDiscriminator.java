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

import lombok.Builder;
import lombok.Singular;

import java.util.HashMap;
import java.util.Map;

@Builder(toBuilder = true)
public class OopTokenWordDiscriminator<T> implements OopTokenParser<T> {
	@Builder.Default
	private final OopTokenParser<T> defaultParser = context -> {
		throw new RuntimeException("Invalid token: " + context.getWord());
	};
	@Singular
	private final Map<String, OopTokenParser<T>> words;

	@Override
	public T parse(OopParserContext context) {
		context.readWord();
		OopTokenParser<T> parser = words.get(context.getWord());
		if (parser != null) {
			return parser.parse(context);
		} else {
			return defaultParser.parse(context);
		}
	}
}
