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
package pl.asie.zima.worldcheck.gui;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.asie.zima.worldcheck.ElementLocation;
import pl.asie.zima.worldcheck.ElementLocationHolder;

@Getter
@EqualsAndHashCode
public class ElementLocationTextNode implements ElementLocationHolder {
	private final ElementLocation location;
	private final String text;

	public ElementLocationTextNode(String text) {
		this.location = null;
		this.text = text;
	}

	public ElementLocationTextNode(ElementLocation location, String text) {
		this.location = location;
		this.text = text;
	}

	@Override
	public String toString() {
		if (this.location == null) {
			return this.text;
		} else if (this.text == null) {
			return this.location.toString();
		} else {
			return "[" + this.location.toString() + "] " + this.text;
		}
	}
}
