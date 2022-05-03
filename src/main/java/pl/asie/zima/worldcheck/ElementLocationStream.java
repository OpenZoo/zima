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
import pl.asie.libzzt.World;
import pl.asie.libzzt.oop.OopProgram;
import pl.asie.libzzt.oop.OopUtils;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class ElementLocationStream {
	private ElementLocationStream() {

	}
	public static Stream<ElementLocation> boards(ElementLocation worldLocation) {
		World world = worldLocation.getWorld();
		return IntStream.range(0, world.getBoards().size())
				.mapToObj(i -> ElementLocation.board(world, i));
	}

	public static Stream<ElementLocation> elements(ElementLocation boardLocation) {
		Board board = boardLocation.getBoard();
		if (board == null) {
			return boards(boardLocation).flatMap(ElementLocationStream::stats);
		} else {
			return IntStream.range(0, board.getWidth() * board.getHeight())
					.mapToObj(i -> ElementLocation.element(boardLocation.getWorld(), boardLocation.getBoardId(),
							(i % board.getWidth()) + 1,
							(i / board.getWidth()) + 1));
		}
	}

	public static Stream<ElementLocation> stats(ElementLocation boardLocation) {
		Board board = boardLocation.getBoard();
		if (board == null) {
			return boards(boardLocation).flatMap(ElementLocationStream::stats);
		} else {
			return IntStream.range(0, board.getStats().size())
					.mapToObj(i -> ElementLocation.stat(boardLocation.getWorld(), boardLocation.getBoardId(), i));
		}
	}

	public static Stream<ElementLocation> commands(ElementLocation statLocation, boolean withChildren) {
		if (statLocation.getStatId() == null) {
			return stats(statLocation).flatMap(c -> ElementLocationStream.commands(c, withChildren));
		} else {
			OopProgram program = statLocation.getProgram();
			if (program == null) {
				return Stream.empty();
			}
			if (withChildren) {
				return OopUtils.allChildren(program.getCommands().stream()).map(statLocation::withCommand);
			} else {
				return program.getCommands().stream().map(statLocation::withCommand);
			}
		}
	}
}
