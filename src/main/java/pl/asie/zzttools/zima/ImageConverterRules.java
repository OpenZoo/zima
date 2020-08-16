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

import pl.asie.zzttools.zzt.Element;

import java.util.*;

public class ImageConverterRules {
	public static final ImageConverterRuleset RULES_BLOCKS;
	public static final ImageConverterRuleset RULES_WALKABLE;
	public static final ImageConverterRuleset RULES_SAFE;
	public static final ImageConverterRuleset RULES_SAFE_STATLESS;
	public static final ImageConverterRuleset RULES_UNSAFE_STATLESS;
	public static final ImageConverterRuleset ALL_RULES;

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static ImageConverterRuleset ruleset(List... lists) {
		Set<ElementRule> combinedList = new LinkedHashSet<>();
		for (List list : lists) {
			combinedList.addAll(list);
		}
		return new ImageConverterRuleset(List.copyOf(combinedList));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static ImageConverterRuleset rulesetSorted(List... lists) {
		Set<ElementRule> combinedList = new LinkedHashSet<>();
		for (List list : lists) {
			combinedList.addAll(list);
		}
		List<ElementRule> outputList = new ArrayList<>(combinedList);
		outputList.sort(Comparator.comparing(e -> e.getElement().ordinal()));
		return new ImageConverterRuleset(outputList);
	}

	static {
		List<ElementRule> unusedRules = new ArrayList<>();

		List<ElementRule> alwaysRules = new ArrayList<>();
		alwaysRules.add(ElementRule.element(Element.EMPTY, ' '));

		List<ElementRule> blockyRules = new ArrayList<>();
		blockyRules.add(ElementRule.element(Element.SOLID, 219));
		blockyRules.add(ElementRule.element(Element.NORMAL, 178));
		blockyRules.add(ElementRule.element(Element.BREAKABLE, 177));
		blockyRules.add(ElementRule.element(Element.WATER, 176));
		unusedRules.add(ElementRule.element(Element.FOREST, 176)); // (water)
		unusedRules.add(ElementRule.element(Element.FAKE, 178)); // (normal)

		// DrawProc-safe
		List<ElementRule> rules = new ArrayList<>();
		List<ElementRule> statlessRules = new ArrayList<>();

		statlessRules.add(ElementRule.element(Element.LION, 234));
		statlessRules.add(ElementRule.element(Element.TIGER, 227));
		statlessRules.add(ElementRule.element(Element.CENTIPEDE_HEAD, 233));
		statlessRules.add(ElementRule.element(Element.CENTIPEDE_SEGMENT, 'O'));
		rules.add(ElementRule.element(Element.BULLET, 248));
		rules.add(ElementRule.element(Element.KEY, 12));
		rules.add(ElementRule.element(Element.AMMO, 132));
		rules.add(ElementRule.element(Element.GEM, 4));
		statlessRules.add(ElementRule.element(Element.PASSAGE, 240));
		rules.add(ElementRule.element(Element.DOOR, 10));
		statlessRules.add(ElementRule.element(Element.SCROLL, 232));
		rules.add(ElementRule.element(Element.TORCH, 157));
		statlessRules.add(ElementRule.element(Element.RUFFIAN, 5));
		statlessRules.add(ElementRule.element(Element.BEAR, 153));
		unusedRules.add(ElementRule.element(Element.SLIME, '*')); // ricochet
		statlessRules.add(ElementRule.element(Element.SHARK, '^'));
		rules.add(ElementRule.element(Element.BLINK_RAY_NS, 186));
		rules.add(ElementRule.element(Element.BLINK_RAY_EW, 205));
		rules.add(ElementRule.element(Element.RICOCHET, '*'));
		rules.add(ElementRule.element(Element.BOULDER, 254));
		rules.add(ElementRule.element(Element.SLIDER_NS, 18));
		rules.add(ElementRule.element(Element.SLIDER_EW, 29));
		rules.add(ElementRule.element(Element.ENERGIZER, 127));
		rules.add(ElementRule.text(Element.TEXT_BLUE, 0x1F));
		rules.add(ElementRule.text(Element.TEXT_GREEN, 0x2F));
		rules.add(ElementRule.text(Element.TEXT_CYAN, 0x3F));
		rules.add(ElementRule.text(Element.TEXT_RED, 0x4F));
		rules.add(ElementRule.text(Element.TEXT_PURPLE, 0x5F));
		rules.add(ElementRule.text(Element.TEXT_YELLOW, 0x6F));
		rules.add(ElementRule.text(Element.TEXT_WHITE, 0x0F));
		statlessRules.add(ElementRule.statP1(Element.OBJECT));
		// uses DrawProc, but with a constant variable
		statlessRules.add(ElementRule.element(Element.BLINK_WALL, 206));

		// statless; rely on how ZZT does things very specifically
		List<ElementRule> unsafeStatlessRules = new ArrayList<>();
		unsafeStatlessRules.add(ElementRule.element(Element.OBJECT, 2));
		unsafeStatlessRules.add(ElementRule.element(Element.DUPLICATOR, 250));
		unsafeStatlessRules.add(ElementRule.element(Element.BOMB, 11));
		unsafeStatlessRules.add(ElementRule.element(Element.PUSHER, 31));

		// for custom rulesets
		ALL_RULES = rulesetSorted(alwaysRules, blockyRules, rules, statlessRules, unsafeStatlessRules, unusedRules);

		RULES_BLOCKS = ruleset(alwaysRules, blockyRules);
		RULES_SAFE = ruleset(alwaysRules, blockyRules, rules);
		RULES_SAFE_STATLESS = ruleset(alwaysRules, blockyRules, rules, statlessRules);
		RULES_UNSAFE_STATLESS = ruleset(alwaysRules, blockyRules, rules, statlessRules, unsafeStatlessRules);

		// extra rulesets
		RULES_WALKABLE = ruleset(alwaysRules, List.of(
				ElementRule.element(Element.FAKE, 178),
				ElementRule.element(Element.FOREST, 176)
		));
	}
}
