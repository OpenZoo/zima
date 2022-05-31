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
package pl.asie.tinyzooconv;

import lombok.Getter;
import pl.asie.tinyzooconv.exceptions.BinarySerializerException;
import pl.asie.tinyzooconv.exceptions.IdNotFoundException;
import pl.asie.libzzt.Board;
import pl.asie.libzzt.Stat;
import pl.asie.libzzt.World;
import pl.asie.libzzt.oop.OopUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class BinaryWorld implements BinarySerializable {
	private static final int MAX_FLAGS = 255;

	private final World parent;
	private final List<BinaryBoard> boards;
	private final List<String> allFlags;

	public BinaryWorld(World parent) throws BinarySerializerException {
		this.parent = parent;
		this.boards = new ArrayList<>(this.parent.getBoards().size());
		for (int i = 0; i < this.parent.getBoards().size(); i++) {
			try {
				this.boards.add(new BinaryBoard(this, this.parent.getBoards().get(i)));
			} catch (BinarySerializerException e) {
				throw new BinarySerializerException("board " + i);
			}
		}

		this.allFlags = new ArrayList<>();
		this.parent.getFlags().stream()
				.filter(f -> !allFlags.contains(f))
				.forEach(allFlags::add);
		for (Board b : this.parent.getBoards()) {
			for (Stat stat : b.getStats()) {
				if (stat.getData() != null) {
					OopUtils.allChildren(stat.getCode(this.parent.getEngineDefinition()).getCommands().stream())
							.flatMap(c -> c.getFlags().stream())
							.filter(f -> !allFlags.contains(f))
							.forEach(allFlags::add);
				}
			}
		}

		BinaryUtil.validateIdCount("flag", MAX_FLAGS, this.allFlags.size());
	}

	public int getFlagId(String flag) throws IdNotFoundException {
		return BinaryUtil.getId("flag", flag, this.allFlags);
	}

	@Override
	public void serialize(BinarySerializerOutput output) throws IOException, BinarySerializerException {
		output.writeShort(this.parent.getAmmo());
		output.writeShort(this.parent.getGems());
		int keys = 0;
		for (int i = 0; i < 7; i++) {
			if (this.parent.getKeys()[i]) {
				keys |= 1 << (i + 1);
			}
		}
		output.writeByte(keys);
		output.writeShort(this.parent.getHealth());
		output.writeShort(this.parent.getTorches());
		output.writeShort(this.parent.getScore());
		output.writeByte(this.parent.getCurrentBoard());
		output.writeByte(this.parent.getTorchTicks());
		output.writeByte(this.parent.getEnergizerTicks());
		for (int i = 0; i < 10; i++) {
			if (i >= this.parent.getFlags().size()) {
				output.writeByte(255);
			} else {
				String flag = this.parent.getFlags().get(i);
				output.writeByte(getFlagId(flag));
			}
		}
		output.writeShort(this.parent.getBoardTimeSec());
		output.writeShort(this.parent.getBoardTimeHsec());
		output.writeByte(this.parent.getBoards().size() - 1);
		for (BinaryBoard board : this.boards) {
			output.writeFarPointerTo(board);
		}
	}
}
