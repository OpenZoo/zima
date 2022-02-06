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
package pl.asie.zima.image;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
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

	// Order:
	// - things covered by empties/elements
	// - things covered by text (don't include empty/element chars)
	// - things covered by objects (don't include text *or* empty/element chars)
	private transient Set<Integer> elementCoveredChars;
	private transient Set<Integer> elementCoveredColors;
	private transient Set<Integer> textCoveredColors;

	public int[] getAllowedTextCharIndices() {
		if (allowedTextCharIndices == null) {
			allowedTextCharIndices = IntStream.range(0, 256).filter(i -> {
				return !getElementCoveredChars().contains(i);
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
						if (fg == bg) return false; // solids
						if (getElementCoveredChars().contains(chr)) return false;
						if (getElementCoveredColors().contains(col)) return false;
						if (getTextCoveredColors().contains(col)) return false;
						return true;
					}).toArray();
		}
		return allowedObjectIndices;
	}

	public Set<Integer> getElementCoveredChars() {
		if (elementCoveredChars == null) {
			elementCoveredChars = rules.stream().filter(rule -> rule.getStrategy() == ElementRule.Strategy.ELEMENT).map(ElementRule::getChr).collect(Collectors.toSet());
		}
		return elementCoveredChars;
	}

	public Set<Integer> getElementCoveredColors() {
		if (elementCoveredColors == null) {
			elementCoveredColors = new HashSet<>();

			// Empties cover black-on-black areas.
			if (rules.stream().anyMatch(rule -> rule.getStrategy() == ElementRule.Strategy.EMPTY)) {
				elementCoveredColors.add(0);
			}

			// Solids cover X-on-X areas.
			if (rules.stream().anyMatch(rule -> rule.getStrategy() == ElementRule.Strategy.ELEMENT && rule.getChr() == 219)) {
				for (int i = 0; i < 16; i++) {
					elementCoveredColors.add((i << 4) | i);
				}
			}
		}
		return elementCoveredColors;
	}

	public Set<Integer> getTextCoveredColors() {
		if (textCoveredColors == null) {
			textCoveredColors = rules.stream().filter(rule -> rule.getStrategy() == ElementRule.Strategy.TEXT).map(ElementRule::getColor).collect(Collectors.toSet());
		}
		return textCoveredColors;
	}
}
