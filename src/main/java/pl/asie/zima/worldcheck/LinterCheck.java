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

import lombok.Getter;
import pl.asie.libzzt.Board;
import pl.asie.libzzt.Stat;
import pl.asie.libzzt.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class LinterCheck {
	@Getter
	private final World world;
	private final List<LinterMessage> messages;
	private final Map<LinterMessage, LinterMessage> messagesOnePerCommand;
	@Getter
	private LinterCheckFlags flags;
	@Getter
	private LinterCheckLabels labels;

	public LinterCheck(World world) {
		this.world = world;
		this.messages = new ArrayList<>();
		this.messagesOnePerCommand = new TreeMap<>();
		generateResults();
	}

	private void addMessage(LinterMessage lm) {
		LinterMessage lmKey = lm;
		if (lmKey.getLocation().getOopCommand() != null) {
			lmKey = lmKey.withLocation(lmKey.getLocation().withCommand(null));
		}
		messagesOnePerCommand.put(lmKey, lmKey);
		if (lm.getLocation() != null && lm.getLocation().getStatPosition() != null) {
			List<Integer> pos = messagesOnePerCommand.get(lmKey).getRelevantPositions();
			if (!pos.contains(lm.getLocation().getStatPosition())) {
				pos.add(lm.getLocation().getStatPosition());
			}
		}
		messages.add(lm);
	}

	private void generateResults() {
		this.messages.clear();
		this.messagesOnePerCommand.clear();

		// MUST GO FIRST: catch all the OopProgram exceptions
		ElementLocationStream.stats(ElementLocation.world(world))
				.forEach(el -> {
					Stat s = el.getStat();
					try {
						s.getCode(el.getWorld().getEngineDefinition());
					} catch (Exception e) {
						e.printStackTrace();
						addMessage(new LinterMessage(
								el, LinterMessageType.OOP_PARSE_ERROR,
								"Could not parse OOP: " + e.getMessage()
						));
					}
				});

		// Flag checks
		this.flags = new LinterCheckFlags(this.world);
		this.flags.emitMessages(this::addMessage);

		// Invalid element checks
		ElementLocationStream.elements(ElementLocation.world(world))
				.forEach(el -> {
					Board board = el.getBoard();
					int x = el.getXPos();
					int y = el.getYPos();
					if (!board.isValidElement(x, y)) {
						addMessage(new LinterMessage(
								el, LinterMessageType.INVALID_ELEMENT,
								"Invalid element ID: " + board.getElementId(x, y)
						));
					}
				});

		// OOP checks
		new LinterCheckBusyLoop(this.world).emitMessages(this::addMessage);
		new LinterCheckNoOp(this.world).emitMessages(this::addMessage);

		// Label/target checks
		this.labels = new LinterCheckLabels(this.world);
		this.labels.emitMessages(this::addMessage);

		// Other checks
		new LinterCheckBoardConnections(this.world).emitMessages(this::addMessage);
	}

	public List<LinterMessage> getMessages() {
		return Collections.unmodifiableList(messages);
	}

	public Collection<LinterMessage> getMessagesOnePerCommand() {
		return messagesOnePerCommand.values();
	}
}
