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
import pl.asie.libzzt.Element;
import pl.asie.libzzt.Platform;
import pl.asie.zima.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ImageConverterRules {
	@Getter private final ImageConverterRuleset allRules;
	@Getter private final List<Pair<String, ImageConverterRuleset>> rulesetPresets;

	private static boolean alreadyContains(Collection<ElementRule> rules, ElementRule rule, boolean exactMatches) {
		if (rules.contains(rule)) {
			return true;
		}
		if (!exactMatches) {
			if (rule.getStrategy() == ElementRule.Strategy.ELEMENT) {
				for (ElementRule other : rules) {
					if (other.getStrategy() == ElementRule.Strategy.ELEMENT && other.getChr() == rule.getChr()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static ImageConverterRuleset ruleset(boolean exactMatches, List... lists) {
		Set<ElementRule> combinedList = new LinkedHashSet<>();
		for (List list : lists) {
			for (ElementRule rule : (List<ElementRule>) list) {
				if (!alreadyContains(combinedList, rule, exactMatches)) {
					combinedList.add(rule);
				}
			}
		}
		return new ImageConverterRuleset(List.copyOf(combinedList));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static ImageConverterRuleset rulesetSorted(boolean exactMatches, List... lists) {
		Set<ElementRule> combinedList = new LinkedHashSet<>();
		for (List list : lists) {
			for (ElementRule rule : (List<ElementRule>) list) {
				if (!alreadyContains(combinedList, rule, exactMatches)) {
					combinedList.add(rule);
				}
			}
		}
		List<ElementRule> outputList = new ArrayList<>(combinedList);
		outputList.sort(Comparator.comparing(e -> e.getElement().getId()));
		return new ImageConverterRuleset(outputList);
	}

	public ImageConverterRules() {
		this.allRules = new ImageConverterRuleset(List.of());
		this.rulesetPresets = List.of(
				new Pair<>("N/A", null)
		);
	}

	public ImageConverterRuleset getRuleset(String name) {
		return rulesetPresets.stream().filter(p -> p.getFirst().equalsIgnoreCase(name)).findFirst().map(Pair::getSecond).orElse(null);
	}

	public ImageConverterRules(Platform platform, boolean isSuperZztBased) {
		List<ElementRule> alwaysRules = new ArrayList<>();
		alwaysRules.add(ElementRule.element(platform, "EMPTY"));

		List<ElementRule> blockyRules = new ArrayList<>();
		blockyRules.add(ElementRule.element(platform, "SOLID"));
		blockyRules.add(ElementRule.element(platform, "NORMAL"));
		blockyRules.add(ElementRule.element(platform, "BREAKABLE"));
		blockyRules.add(ElementRule.element(platform, "WATER"));
		blockyRules.add(ElementRule.element(platform, "FOREST")); // (water, lava)
		blockyRules.add(ElementRule.element(platform, "FAKE")); // (normal)

		// DrawProc-safe
		List<ElementRule> rules = new ArrayList<>();
		List<ElementRule> statlessRules = new ArrayList<>();

		if (isSuperZztBased) {
			rules.add(ElementRule.element(Platform.SUPER_ZZT, "LAVA"));
			statlessRules.add(ElementRule.element(Platform.SUPER_ZZT, "ROTON"));
			statlessRules.add(ElementRule.element(Platform.SUPER_ZZT, "SPIDER"));
			statlessRules.add(ElementRule.element(Platform.SUPER_ZZT, "PAIRER"));
		}
		statlessRules.add(ElementRule.element(platform, "LION"));
		statlessRules.add(ElementRule.element(platform, "TIGER"));
		statlessRules.add(ElementRule.element(platform, "CENTIPEDE_HEAD"));
		statlessRules.add(ElementRule.element(platform, "CENTIPEDE_SEGMENT"));
		rules.add(ElementRule.element(platform, "BULLET"));
		rules.add(ElementRule.element(platform, "KEY"));
		rules.add(ElementRule.element(platform, "AMMO"));
		rules.add(ElementRule.element(platform, "GEM"));
		statlessRules.add(ElementRule.element(platform, "PASSAGE"));
		rules.add(ElementRule.element(platform, "DOOR"));
		statlessRules.add(ElementRule.element(platform, "SCROLL"));
		if (!isSuperZztBased) {
			rules.add(ElementRule.element(platform, "TORCH"));
		}
		statlessRules.add(ElementRule.element(platform, "RUFFIAN"));
		statlessRules.add(ElementRule.element(platform, "BEAR"));
		if (!isSuperZztBased) {
			statlessRules.add(ElementRule.element(platform, "SHARK"));
		}
		rules.add(ElementRule.element(platform, "BLINK_RAY_NS"));
		rules.add(ElementRule.element(platform, "BLINK_RAY_EW"));
		rules.add(ElementRule.element(platform, "RICOCHET"));
		rules.add(ElementRule.element(platform, "BOULDER"));
		rules.add(ElementRule.element(platform, "SLIDER_NS"));
		rules.add(ElementRule.element(platform, "SLIDER_EW"));
		rules.add(ElementRule.element(platform, "ENERGIZER"));
		if (isSuperZztBased) {
			rules.add(ElementRule.element(Platform.SUPER_ZZT, "WATER_N"));
			rules.add(ElementRule.element(Platform.SUPER_ZZT, "WATER_S"));
			rules.add(ElementRule.element(Platform.SUPER_ZZT, "WATER_W"));
			rules.add(ElementRule.element(Platform.SUPER_ZZT, "WATER_E"));
		}
		rules.add(ElementRule.text(platform, "TEXT_BLUE"));
		rules.add(ElementRule.text(platform, "TEXT_GREEN"));
		rules.add(ElementRule.text(platform, "TEXT_CYAN"));
		rules.add(ElementRule.text(platform, "TEXT_RED"));
		rules.add(ElementRule.text(platform, "TEXT_PURPLE"));
		rules.add(ElementRule.text(platform, "TEXT_YELLOW"));
		rules.add(ElementRule.text(platform, "TEXT_WHITE"));

		statlessRules.add(ElementRule.statP1(platform, "OBJECT"));
		// uses DrawProc, but with a constant variable
		statlessRules.add(ElementRule.element(platform, "BLINK_WALL"));

		// statless; rely on how ZZT does things very specifically
		List<ElementRule> unsafeStatlessRules = new ArrayList<>();
		unsafeStatlessRules.add(ElementRule.element(platform, "SLIME")); // ricochet
		unsafeStatlessRules.add(ElementRule.element(platform, "OBJECT", 2));
		unsafeStatlessRules.add(ElementRule.element(platform, "DUPLICATOR", 250));
		unsafeStatlessRules.add(ElementRule.element(platform, "BOMB", 11));
		unsafeStatlessRules.add(ElementRule.element(platform, "PUSHER", 31));

		// WeaveZZT patches
		Element unknown46 = platform.getLibrary().byInternalName("UNKNOWN_46");
		if (unknown46.isText()) {
			rules.add(ElementRule.text(platform, "UNKNOWN_46"));
		}
		Element monitor = platform.getLibrary().byInternalName("MONITOR");
		if (monitor.getCharacter() != 32 && monitor.getCharacter() != 0) {
			statlessRules.add(ElementRule.element(platform, "MONITOR"));
		}
		Element edge = platform.getLibrary().byInternalName("BOARD_EDGE");
		if (edge.getCharacter() != 32 && edge.getCharacter() != 0) {
			rules.add(ElementRule.element(platform, "BOARD_EDGE"));
		}
		Element messageTimer = platform.getLibrary().byInternalName("MESSAGE_TIMER");
		if (messageTimer.getCharacter() != 32 && messageTimer.getCharacter() != 0) {
			statlessRules.add(ElementRule.element(platform, "MESSAGE_TIMER"));
		}

		// for custom rulesets
		this.allRules = rulesetSorted(true, alwaysRules, blockyRules, rules, statlessRules, unsafeStatlessRules);

		ImageConverterRuleset RULES_BLOCKS = ruleset(false, alwaysRules, blockyRules);
		ImageConverterRuleset RULES_SAFE = ruleset(false, alwaysRules, blockyRules, rules);
		ImageConverterRuleset RULES_SAFE_STATLESS = ruleset(false, alwaysRules, blockyRules, rules, statlessRules);
		ImageConverterRuleset RULES_UNSAFE_STATLESS = ruleset(false, alwaysRules, blockyRules, rules, statlessRules, unsafeStatlessRules);

		// extra rulesets
		ImageConverterRuleset RULES_WALKABLE = ruleset(false, alwaysRules, allRules.getRules().stream().filter(r -> r.getElement().isWalkable()).collect(Collectors.toList()));

		this.rulesetPresets = List.of(
				new Pair<>("Default", RULES_UNSAFE_STATLESS),
				new Pair<>("Default (Clone-safe)", RULES_SAFE_STATLESS),
				new Pair<>("Default (Elements only)", RULES_SAFE),
				new Pair<>("Blocks", RULES_BLOCKS),
				new Pair<>("Walkable", RULES_WALKABLE),
				new Pair<>("Custom", null)
		);
	}
}
