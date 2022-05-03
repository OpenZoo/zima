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
import pl.asie.libzzt.oop.commands.OopCommandChar;
import pl.asie.libzzt.oop.commands.OopCommandCycle;
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

public class LinterCheckNoOp {
	private final List<LinterMessage> messages = new ArrayList<>();
	private final ElementLocation worldLoc;

	public LinterCheckNoOp(World world) {
		worldLoc = ElementLocation.world(world);
		ElementLocationStream.commands(worldLoc, true).forEach(cmdLoc -> {
			OopCommand c = cmdLoc.getOopCommand();;
			if (c instanceof OopCommandChar cmd) {
				if (!(cmd.getValue() > 0 && (cmd.getValue() <= 255))) {
					messages.add(new LinterMessage(cmdLoc, LinterMessageType.PERFORMANCE_NOOP_FOUND,
							"No-op found: #CHAR " + cmd.getValue()));
				}
			} else if (c instanceof OopCommandCycle cmd) {
				if (cmd.getValue() <= 0) {
					messages.add(new LinterMessage(cmdLoc, LinterMessageType.PERFORMANCE_NOOP_FOUND,
							"No-op found: #CYCLE " + cmd.getValue()));
				}
			}
		});
	}

	public void emitMessages(Consumer<LinterMessage> consumer) {
		messages.forEach(consumer);
	}
}
