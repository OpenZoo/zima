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
import pl.asie.libzzt.oop.OopLabelTarget;
import pl.asie.libzzt.oop.OopProgram;
import pl.asie.libzzt.oop.OopUtils;
import pl.asie.libzzt.oop.commands.OopCommand;
import pl.asie.libzzt.oop.commands.OopCommandIf;
import pl.asie.libzzt.oop.commands.OopCommandLabel;
import pl.asie.libzzt.oop.commands.OopCommandSend;
import pl.asie.libzzt.oop.conditions.OopConditionAlligned;
import pl.asie.libzzt.oop.conditions.OopConditionAny;
import pl.asie.libzzt.oop.conditions.OopConditionBlocked;
import pl.asie.libzzt.oop.conditions.OopConditionContact;
import pl.asie.libzzt.oop.conditions.OopConditionEnergized;
import pl.asie.libzzt.oop.conditions.OopConditionNot;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class LinterCheckBusyLoop {
	private final List<LinterMessage> messages = new ArrayList<>();
	private final ElementLocation worldLoc;

	public boolean hasBusyLoop(String label, List<OopCommand> commands, int fromIndex) {
		// TODO: Handle anything but the most trivial case

		int i = fromIndex;
		LinkedList<OopCommand> childrenQueue = new LinkedList<>();
		while (i < commands.size() || !childrenQueue.isEmpty()) {
			OopCommand next;
			if (!childrenQueue.isEmpty()) {
				next = childrenQueue.removeFirst();
			} else {
				next = commands.get(i++);
			}

			if (next instanceof OopCommandIf cmd) {
				boolean condKeepsBusyLoop = OopUtils.allChildren(cmd.getCondition())
						.allMatch(c -> c instanceof OopConditionAlligned
								|| c instanceof OopConditionAny
								|| c instanceof OopConditionBlocked
								|| c instanceof OopConditionContact
								|| c instanceof OopConditionEnergized
								|| c instanceof OopConditionNot);
				if (!condKeepsBusyLoop) return false;
				childrenQueue.add(cmd.getTrueCommand());
			} else if (next instanceof OopCommandSend cmd) {
				OopLabelTarget target = cmd.getTarget();
				if (target.getTarget().equals("") || target.getTarget().equals("SELF")) {
					if (target.getLabel().equals(label)) {
						return true;
					}
				}
			} else {
				return false;
			}
		}

		return false;
	}

	public LinterCheckBusyLoop(World world) {
		worldLoc = ElementLocation.world(world);
		ElementLocationStream.stats(worldLoc).forEach(statLoc -> {
			OopProgram program = statLoc.getProgram();
			if (program != null) {
				for (int i = 0; i < program.getCommands().size(); i++) {
					OopCommand cmd = program.getCommands().get(i);
					if (cmd instanceof OopCommandLabel label) {
						if (hasBusyLoop(label.getLabel().toUpperCase(Locale.ROOT), program.getCommands(), i + 1)) {
							messages.add(new LinterMessage(
									statLoc.withCommand(cmd), LinterMessageType.PERFORMANCE_BUSYLOOP_FOUND,
									"Unperformant busy loop found on label " + label.getLabel().toUpperCase(Locale.ROOT)
							));
						}
					}
				}
			}
		});
	}

	public void emitMessages(Consumer<LinterMessage> consumer) {
		messages.forEach(consumer);
	}
}
