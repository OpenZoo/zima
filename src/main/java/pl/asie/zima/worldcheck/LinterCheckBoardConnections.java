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

import pl.asie.libzzt.Board;
import pl.asie.libzzt.Element;
import pl.asie.libzzt.Stat;
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

public class LinterCheckBoardConnections {
	private final List<LinterMessage> messages = new ArrayList<>();
	private final ElementLocation worldLoc;
	private final int[] oppositeBoardId = new int[] { 1, 0, 3, 2 };
	private final String[] neighborNameById = new String[] { "north", "south", "west", "east" };

	public LinterCheckBoardConnections(World world) {
		worldLoc = ElementLocation.world(world);

		// check board edge reciprocation
		ElementLocationStream.boards(worldLoc).forEach(el -> {
			Board b = el.getBoard();
			for (int neighborIdx = 0; neighborIdx < 4; neighborIdx++) {
				if (b.getNeighborBoards()[neighborIdx] > 0) {
					int neighborId = b.getNeighborBoards()[neighborIdx];
					if (neighborId >= world.getBoards().size()) {
						messages.add(new LinterMessage(el, LinterMessageType.BOARD_CONNECTION_INVALID_ID,
								"Invalid board ID for " + neighborNameById[neighborIdx] + " neighbor: " + neighborId));
					} else {
						Board b2 = world.getBoards().get(neighborId);
						if (neighborId >= el.getBoardId()) {
							if (b2.getNeighborBoards()[oppositeBoardId[neighborIdx]] != el.getBoardId()) {
								// TODO: Do not send this if the board edge is fully blocked.
								messages.add(new LinterMessage(el, LinterMessageType.BOARD_CONNECTION_NOT_RECIPROCATED,
										"Connection to " + neighborNameById[neighborIdx] + " neighbor is not reciprocated: "
												+ ElementLocation.board(world, neighborId)));
							}

							// TODO: Check board edge blockades?

							int xMin = 1;
							int yMin = 1;
							int xMax = Math.min(b.getWidth(), b2.getWidth());
							int yMax = Math.min(b.getHeight(), b2.getHeight());
						}
					}
				}
			}
		});

		// check passage reciprocation

		Element passageElement = world.getEngineDefinition().getElements().byInternalName("PASSAGE");
		ElementLocationStream.stats(worldLoc).forEach(el -> {
			Board b = el.getBoard();
			Stat st = el.getStat();
			if (el.getElement() == passageElement && st != null) {
				int neighborId = st.getP3();
				if (neighborId >= world.getBoards().size()) {
					messages.add(new LinterMessage(el, LinterMessageType.BOARD_PASSAGE_INVALID_ID,
							"Invalid board ID for passage: " + neighborId));
				} else {
					// TODO: Check reciprocation?
				}
			}
		});
	}

	public void emitMessages(Consumer<LinterMessage> consumer) {
		messages.forEach(consumer);
	}
}
