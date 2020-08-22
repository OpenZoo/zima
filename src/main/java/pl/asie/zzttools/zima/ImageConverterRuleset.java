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
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class ImageConverterRuleset {
	@Getter
	private final List<ElementRule> rules;
	private transient int[] allowedObjectIndices;
	private transient int[] allowedTextCharIndices;
	private transient Set<Integer> globallyCoveredChars;
	private transient Set<Integer> globallyCoveredColors;

	public int[] getAllowedTextCharIndices() {
		if (allowedTextCharIndices == null) {
			allowedTextCharIndices = IntStream.range(0, 256).filter(i -> {
				if (i == 0 || i == 255) return false;
				return !getGloballyCoveredChars().contains(i);
			}).toArray();
		}
		return allowedTextCharIndices;
	}

	@SuppressWarnings("RedundantIfStatement")
	public int[] getAllowedObjectIndices() {
		if (allowedObjectIndices == null) {
			allowedObjectIndices = IntStream.range(0, 256 * 256)
					.filter(i -> {
						// reduce the number of cases
						int chr = (i & 0xFF);
						int col = ((i >> 8) & 0xFF);
						int fg = (i >> 8) & 0x0F;
						int bg = (i >> 12) & 0x0F;
						if (chr == 0 || chr == 32 || chr == 255) return false;
						if (fg == bg) return false; // solids
						if (getGloballyCoveredChars().contains(chr)) return false;
						if (getGloballyCoveredColors().contains(col)) return false;
						return true;
					}).toArray();
		}
		return allowedObjectIndices;
	}

	public Set<Integer> getGloballyCoveredChars() {
		if (globallyCoveredChars == null) {
			globallyCoveredChars = rules.stream().filter(rule -> rule.getStrategy() == ElementRule.Strategy.ELEMENT).map(ElementRule::getChr).collect(Collectors.toSet());
		}
		return globallyCoveredChars;
	}

	public Set<Integer> getGloballyCoveredColors() {
		if (globallyCoveredColors == null) {
			globallyCoveredColors = rules.stream().filter(rule -> rule.getStrategy() == ElementRule.Strategy.TEXT).map(ElementRule::getColor).collect(Collectors.toSet());
		}
		return globallyCoveredColors;
	}
}
