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
package pl.asie.zima.worldcheck;

import pl.asie.libzzt.World;
import pl.asie.libzzt.oop.OopUtils;
import pl.asie.libzzt.oop.commands.OopCommand;
import pl.asie.libzzt.oop.commands.OopCommandClear;
import pl.asie.libzzt.oop.commands.OopCommandIf;
import pl.asie.libzzt.oop.commands.OopCommandSet;
import pl.asie.libzzt.oop.conditions.OopCondition;
import pl.asie.libzzt.oop.conditions.OopConditionFlag;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class LinterCheckFlags {
	private static final List<String> PRESET_FLAGS = List.of("SECRET");
	private final SortedMap<String, LinterFlag> flags = new TreeMap<>();
	private final ElementLocation worldLoc;

	private void addFlagAt(ElementLocation location, LinterFlag.ActionType type, String flag) {
		LinterFlag linterFlag = flags.computeIfAbsent(flag, LinterFlag::new);
		linterFlag.mark(location, type);
	}

	public Collection<LinterFlag> getFlags() {
		return flags.values();
	}

	public LinterCheckFlags(World world) {
		worldLoc = ElementLocation.world(world);
		for (String s : world.getFlags()) {
			addFlagAt(worldLoc, LinterFlag.ActionType.SET, s);
			ElementLocationStream.commands(worldLoc, true).forEach(el -> {
				OopCommand c = el.getOopCommand();
				if (c instanceof OopCommandSet cmd) {
					addFlagAt(el, LinterFlag.ActionType.SET, cmd.getFlag());
				} else if (c instanceof OopCommandClear cmd) {
					addFlagAt(el, LinterFlag.ActionType.CLEAR, cmd.getFlag());
				} else if (c instanceof OopCommandIf ifCmd) {
					OopUtils.allChildren(ifCmd.getCondition()).forEach(cond -> {
						if (cond instanceof OopConditionFlag ifCond) {
							addFlagAt(el, LinterFlag.ActionType.CHECK, ifCond.getFlag());
						}
					});
				} else if (!c.getFlags().isEmpty()) {
					new Exception("libzzt/worldcheck mismatch").printStackTrace();
				}
			});
		}
	}

	private void emitMessageForEmptyButNotEmpty(Consumer<LinterMessage> consumer, Consumer<LinterFlag> flagConsumer,
	                                Function<LinterFlag, Collection<ElementLocation>> funcNotEmpty,
	                                Predicate<LinterFlag> funcEmpty,
	                                LinterMessageType type, String prefix, boolean checkPresetFlags) {

		flags.values().stream()
				.filter(f -> !checkPresetFlags || !PRESET_FLAGS.contains(f.getName()))
				.filter(funcEmpty::test)
				.forEach(f -> {
					flagConsumer.accept(f);
					for (ElementLocation el : funcNotEmpty.apply(f)) {
						consumer.accept(new LinterMessage(
								el, type,
								prefix + ": " + f.getName()
						));
					}
				});
	}

	public void emitMessages(Consumer<LinterMessage> consumer) {
		Set<String> flagsSetButNotCleared = new HashSet<>();

		emitMessageForEmptyButNotEmpty(consumer, f -> flagsSetButNotCleared.add(f.getName()), LinterFlag::getSetAt,
				f -> f.getCheckedAt().isEmpty() && f.getClearedAt().isEmpty(),
				LinterMessageType.FLAG_SET_BUT_NOT_USED, "Flag set but not used", true);
		emitMessageForEmptyButNotEmpty(consumer, f -> {}, LinterFlag::getSetAt,
				f -> f.getCheckedAt().isEmpty() && !f.getClearedAt().isEmpty(),
				LinterMessageType.FLAG_SET_BUT_NOT_CHECKED, "Flag set but not checked", true);
		emitMessageForEmptyButNotEmpty(consumer, f -> flagsSetButNotCleared.add(f.getName()), LinterFlag::getSetAt,
				f -> f.getClearedAt().isEmpty() && !f.getCheckedAt().isEmpty(),
				LinterMessageType.FLAG_SET_BUT_NOT_CLEARED, "Flag set but not cleared", true);
		emitMessageForEmptyButNotEmpty(consumer, f -> {}, f -> Stream.concat(f.getCheckedAt().stream(), f.getClearedAt().stream()).sorted().distinct().toList(),
				f -> f.getSetAt().isEmpty(),
				LinterMessageType.FLAG_USED_BUT_NOT_SET, "Flag used but not set", false);
		emitMessageForEmptyButNotEmpty(consumer, f -> {}, f -> f.getCheckedAt().stream().filter(a -> !f.getClearedAt().contains(a)).toList(),
				f -> f.getSetAt().isEmpty(),
				LinterMessageType.FLAG_CHECKED_BUT_NOT_SET, "Flag checked but not set", false);
		emitMessageForEmptyButNotEmpty(consumer, f -> {}, f -> f.getClearedAt().stream().filter(a -> !f.getCheckedAt().contains(a)).toList(),
				f -> f.getSetAt().isEmpty(),
				LinterMessageType.FLAG_CLEARED_BUT_NOT_SET, "Flag cleared but not set", false);

		// TODO: Configurable?
		if (flagsSetButNotCleared.size() > 10) {
			consumer.accept(new LinterMessage(
					worldLoc, LinterMessageType.FLAG_TOO_MANY_MIGHT_BE_SET,
					"Too many flags might be set but not cleared (" + flagsSetButNotCleared.size() + " > 10): [" + String.join(", ", flagsSetButNotCleared) + "]"
			));
		}
	}
}
