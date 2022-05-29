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

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import pl.asie.libzzt.Board;
import pl.asie.libzzt.Element;
import pl.asie.libzzt.Stat;
import pl.asie.libzzt.World;
import pl.asie.libzzt.oop.OopProgram;
import pl.asie.libzzt.oop.commands.OopCommand;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ElementLocation implements Comparable<ElementLocation> {
	private final World world;
	private final Integer boardId;
	private final Integer xPos;
	private final Integer yPos;
	private final Integer statId;
	private final Integer statLine;
	private final Integer statPosition;
	private final OopCommand oopCommand;

	public Board getBoard() {
		return boardId != null ? world.getBoards().get(boardId) : null;
	}

	public Element getElement() {
		return xPos != null && yPos != null ? getBoard().getElement(xPos, yPos) : null;
	}

	public Stat getStat() {
		return statId != null ? getBoard().getStat(statId) : null;
	}

	public OopProgram getProgram() {
		Stat stat = getStat();
		return stat != null ? stat.getCode(world.getEngineDefinition()) : null;
	}

	private int compare(Integer a, Integer b) {
		if (a != null && b != null) {
			return a - b;
		} else if (a == null && b == null) {
			return 0;
		} else if (a == null) {
			return 1;
		} else {
			return -1;
		}
	}

	@Override
	public int compareTo(ElementLocation o) {
		int result;
		result = compare(boardId, o.boardId);
		if (result != 0) return result;
		if (statId == null && o.statId == null) {
			result = compare(yPos, o.yPos);
			if (result != 0) return result;
			result = compare(xPos, o.xPos);
			if (result != 0) return result;
		} else {
			result = compare(statId, o.statId);
			if (result != 0) return result;
			result = compare(statPosition, statPosition);
			if (result != 0) return result;
		}
		return 0;
	}

	// true if this.includes(o)
	public boolean includes(ElementLocation o) {
		if (boardId == null) return true;
		if (!Objects.equals(boardId, o.boardId)) return false;
		if (xPos == null && yPos == null) return true;
		if (!Objects.equals(xPos, o.xPos) || !Objects.equals(yPos, o.yPos)) return false;
		if (statId == null) return true;
		if (!Objects.equals(statId, o.statId)) return false;
		if (statPosition == null) return true;
		if (!Objects.equals(statPosition, o.statPosition)) return false;
		return true;
	}

	// true if this.isIncludedIn(o)
	public boolean isIncludedIn(ElementLocation o) {
		return o.includes(this);
	}

	@Override
	public String toString() {
		if (getBoardId() != null) {
			if (getStatId() != null) {
				Board board = world.getBoards().get(getBoardId());
				Stat stat = board.getStat(getStatId());
				Element element = null;
				if (stat.getX() >= 0 && stat.getY() >= 0 && stat.getX() <= board.getWidth() && stat.getY() <= board.getHeight()) {
					element = board.getElement(stat.getX(), stat.getY());
				}
				OopProgram program = stat.getCode(board.getEngineDefinition());
				StringBuilder sb = new StringBuilder();
				sb.append(getStatId()).append(" (").append(stat.getX()).append(", ").append(stat.getY()).append(") ");
				if (element != null) {
					sb.append(element.getName());
				} else {
					sb.append("?");
				}
				if (program != null) {
					if (program.getWindowName() != null && !program.getWindowName().isBlank()) {
						sb.append(": ");
						sb.append(program.getWindowName());
					}

					if (getStatLine() != null) {
						sb.append(", line ").append(getStatLine());
					} else if (getStatPosition() != null) {
						sb.append(", char ").append(getStatPosition());
					} else if (stat.getData() != null) {
						sb.append(" (").append(stat.getData().length()).append(" b.)");
					}
				}
				return sb.toString();
			} else {
				Board board = world.getBoards().get(getBoardId());
				StringBuilder sb = new StringBuilder();
				if (getBoardId() == world.getCurrentBoard()) {
					sb.append("*");
				}
				sb.append(getBoardId()).append(": ").append(board.getName());
				return sb.toString();
			}
		} else {
			if (world.getName() != null && !world.getName().isBlank()) {
				return world.getName();
			} else {
				return "World";
			}
		}
	}

	public static ElementLocation world(World world) {
		return new ElementLocation(world, null, null, null, null, null, null, null);
	}

	public static ElementLocation board(World world, int idx) {
		return new ElementLocation(world, idx, null, null, null, null, null, null);
	}

	public static ElementLocation element(World world, int boardId, int x, int y) {
		Board b = world.getBoards().get(boardId);
		OptionalInt statId = b.getStatId(b.getStatAt(x, y));
		return new ElementLocation(world, boardId, x, y, statId.isPresent() ? statId.orElse(0) : null, null, null, null);
	}

	public static ElementLocation stat(World world, int boardId, int statIdx) {
		Board board = world.getBoards().get(boardId);
		Stat stat = board.getStat(statIdx);
		return new ElementLocation(world, boardId, stat.getX(), stat.getY(), statIdx, null, null, null);
	}

	public ElementLocation withCommand(OopCommand command) {
		return new ElementLocation(getWorld(), getBoardId(), getXPos(), getYPos(), getStatId(), null,
				command == null ? null : command.getPosition(), command);
	}

	public static ElementLocation command(World world, int boardId, int statIdx, OopCommand command) {
		Board board = world.getBoards().get(boardId);
		Stat stat = board.getStat(statIdx);
		return new ElementLocation(world, boardId, stat.getX(), stat.getY(), statIdx, null, command.getPosition(), command);
	}
}
