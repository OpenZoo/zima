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
package pl.asie.zzttools.zima;

import lombok.Data;
import pl.asie.zzttools.zzt.Element;

@Data
public class ElementResult {
	private final Element element;
	private final boolean hasStat;
	private final boolean text;
	private final int character;
	private final int color;
	private float mse;

	public ElementResult withMse(float mse) {
		ElementResult result = new ElementResult(element, hasStat, text, character, color);
		result.setMse(mse);
		return result;
	}
}
