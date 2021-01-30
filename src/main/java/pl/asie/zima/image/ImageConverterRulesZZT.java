/**
 * Copyright (c) 2020, 2021 Adrian Siekierka
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

import pl.asie.libzzt.Platform;

import java.util.*;

public class ImageConverterRulesZZT {
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
		outputList.sort(Comparator.comparing(e -> e.getElement().getId()));
		return new ImageConverterRuleset(outputList);
	}

	static {
		List<ElementRule> unusedRules = new ArrayList<>();

		List<ElementRule> alwaysRules = new ArrayList<>();
		alwaysRules.add(ElementRule.element(Platform.ZZT, "EMPTY"));

		List<ElementRule> blockyRules = new ArrayList<>();
		blockyRules.add(ElementRule.element(Platform.ZZT, "SOLID"));
		blockyRules.add(ElementRule.element(Platform.ZZT, "NORMAL"));
		blockyRules.add(ElementRule.element(Platform.ZZT, "BREAKABLE"));
		blockyRules.add(ElementRule.element(Platform.ZZT, "WATER"));
		unusedRules.add(ElementRule.element(Platform.ZZT, "FOREST")); // (water)
		unusedRules.add(ElementRule.element(Platform.ZZT, "FAKE")); // (normal)

		// DrawProc-safe
		List<ElementRule> rules = new ArrayList<>();
		List<ElementRule> statlessRules = new ArrayList<>();

		statlessRules.add(ElementRule.element(Platform.ZZT, "LION"));
		statlessRules.add(ElementRule.element(Platform.ZZT, "TIGER"));
		statlessRules.add(ElementRule.element(Platform.ZZT, "CENTIPEDE_HEAD"));
		statlessRules.add(ElementRule.element(Platform.ZZT, "CENTIPEDE_SEGMENT"));
		rules.add(ElementRule.element(Platform.ZZT, "BULLET"));
		rules.add(ElementRule.element(Platform.ZZT, "KEY"));
		rules.add(ElementRule.element(Platform.ZZT, "AMMO"));
		rules.add(ElementRule.element(Platform.ZZT, "GEM"));
		statlessRules.add(ElementRule.element(Platform.ZZT, "PASSAGE"));
		rules.add(ElementRule.element(Platform.ZZT, "DOOR"));
		statlessRules.add(ElementRule.element(Platform.ZZT, "SCROLL"));
		rules.add(ElementRule.element(Platform.ZZT, "TORCH"));
		statlessRules.add(ElementRule.element(Platform.ZZT, "RUFFIAN"));
		statlessRules.add(ElementRule.element(Platform.ZZT, "BEAR"));
		unusedRules.add(ElementRule.element(Platform.ZZT, "SLIME")); // ricochet
		statlessRules.add(ElementRule.element(Platform.ZZT, "SHARK"));
		rules.add(ElementRule.element(Platform.ZZT, "BLINK_RAY_NS"));
		rules.add(ElementRule.element(Platform.ZZT, "BLINK_RAY_EW"));
		rules.add(ElementRule.element(Platform.ZZT, "RICOCHET"));
		rules.add(ElementRule.element(Platform.ZZT, "BOULDER"));
		rules.add(ElementRule.element(Platform.ZZT, "SLIDER_NS"));
		rules.add(ElementRule.element(Platform.ZZT, "SLIDER_EW"));
		rules.add(ElementRule.element(Platform.ZZT, "ENERGIZER"));
		rules.add(ElementRule.text(Platform.ZZT, "TEXT_BLUE", 0x1F));
		rules.add(ElementRule.text(Platform.ZZT, "TEXT_GREEN", 0x2F));
		rules.add(ElementRule.text(Platform.ZZT, "TEXT_CYAN", 0x3F));
		rules.add(ElementRule.text(Platform.ZZT, "TEXT_RED", 0x4F));
		rules.add(ElementRule.text(Platform.ZZT, "TEXT_PURPLE", 0x5F));
		rules.add(ElementRule.text(Platform.ZZT, "TEXT_YELLOW", 0x6F));
		rules.add(ElementRule.text(Platform.ZZT, "TEXT_WHITE", 0x0F));
		statlessRules.add(ElementRule.statP1(Platform.ZZT, "OBJECT"));
		// uses DrawProc, but with a constant variable
		statlessRules.add(ElementRule.element(Platform.ZZT, "BLINK_WALL"));

		// statless; rely on how ZZT does things very specifically
		List<ElementRule> unsafeStatlessRules = new ArrayList<>();
		unsafeStatlessRules.add(ElementRule.element(Platform.ZZT, "OBJECT", 2));
		unsafeStatlessRules.add(ElementRule.element(Platform.ZZT, "DUPLICATOR", 250));
		unsafeStatlessRules.add(ElementRule.element(Platform.ZZT, "BOMB", 11));
		unsafeStatlessRules.add(ElementRule.element(Platform.ZZT, "PUSHER", 31));

		// for custom rulesets
		ALL_RULES = rulesetSorted(alwaysRules, blockyRules, rules, statlessRules, unsafeStatlessRules, unusedRules);

		RULES_BLOCKS = ruleset(alwaysRules, blockyRules);
		RULES_SAFE = ruleset(alwaysRules, blockyRules, rules);
		RULES_SAFE_STATLESS = ruleset(alwaysRules, blockyRules, rules, statlessRules);
		RULES_UNSAFE_STATLESS = ruleset(alwaysRules, blockyRules, rules, statlessRules, unsafeStatlessRules);

		// extra rulesets
		RULES_WALKABLE = ruleset(alwaysRules, List.of(
				ElementRule.element(Platform.ZZT, "FAKE"),
				ElementRule.element(Platform.ZZT, "FOREST")
		));
	}
}
