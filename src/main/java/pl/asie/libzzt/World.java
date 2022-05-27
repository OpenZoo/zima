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
package pl.asie.libzzt;

import lombok.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
public class World {
	private static final int VERSION = -1;

	private int ammo;
	private int gems;
	private boolean[] keys = new boolean[7];
	private int health = 100;
	private int currentBoard;
	private int torches;
	private int torchTicks;
	private int energizerTicks;
	private int score;
	private String name = "";
	private List<String> flags = new ArrayList<>();
	private int boardTimeSec;
	private int boardTimeHsec;
	private boolean isSave;
	private int stonesOfPower; // Super ZZT
	private List<Board> boards = new ArrayList<>();

	private final EngineDefinition engineDefinition;

	public World(EngineDefinition engineDefinition) {
		this.engineDefinition = engineDefinition;
		Board titleBoard = new Board(engineDefinition);
		titleBoard.setName("Title screen");
		boards.add(titleBoard);
	}

	public void readZ(ZInputStream stream) throws IOException {
		int position = stream.getPosition();
		int version = stream.readPShort();
		if (version != VERSION) {
			throw new RuntimeException("Invalid version: " + version);
		}
		int boardCount = stream.readPShort();

		// world info
		this.ammo = stream.readPShort();
		this.gems = stream.readPShort();
		for (int i = 0; i < 7; i++)
			this.keys[i] = stream.readPBoolean();
		this.health = stream.readPShort();
		this.currentBoard = stream.readPShort();
		if (engineDefinition.getBaseKind().isSuperZZTLike()) {
			stream.readPShort(); // unk1
		} else {
			this.torches = stream.readPShort();
			this.torchTicks = stream.readPShort();
		}
		this.energizerTicks = stream.readPShort();
		stream.readPShort();
		this.score = stream.readPShort();
		this.name = stream.readPString(20);
		this.flags.clear();
		for (int i = 0; i < (engineDefinition.getBaseKind().isSuperZZTLike() ? 16 : 10); i++) {
			String flag = stream.readPString(20);
			if (!flag.isBlank()) {
				this.flags.add(flag);
			}
		}
		this.boardTimeSec = stream.readPShort();
		this.boardTimeHsec = stream.readPShort();
		this.isSave = stream.readPBoolean();
		if (engineDefinition.getBaseKind().isSuperZZTLike()) {
			this.stonesOfPower = stream.readPShort();
			if (stream.skip(12) != 12) {
				throw new IOException("World info error!");
			}
		} else {
			if (stream.skip(14) != 14) {
				throw new IOException("World info error!");
			}
		}
		int newPosition = position + (engineDefinition.getBaseKind().isSuperZZTLike() ? 1024 : 512);
		stream.skipTo(newPosition);
		if (stream.getPosition() != newPosition) {
			throw new IOException("World padding error!");
		}

		// boards
		boards.clear();
		for (int i = 0; i <= boardCount; i++) {
			Board board = new Board(engineDefinition);
			board.readZ(stream);
			boards.add(board);
		}
	}

	public void writeZ(ZOutputStream stream) throws IOException {
		int pos = stream.getPosition();
		stream.writePShort(VERSION);
		stream.writePShort(boards.size() - 1);

		// world info
		stream.writePShort(ammo);
		stream.writePShort(gems);
		for (int i = 0; i < 7; i++)
			stream.writePBoolean(keys[i]);
		stream.writePShort(health);
		stream.writePShort(currentBoard);
		if (engineDefinition.getBaseKind().isSuperZZTLike()) {
			stream.writePShort(0); // unk1
		} else {
			stream.writePShort(torches);
			stream.writePShort(torchTicks);
		}
		stream.writePShort(energizerTicks);
		stream.writePShort(0); // unk2
		stream.writePShort(score);
		stream.writePString(name, 20);
		for (int i = 0; i < (engineDefinition.getBaseKind().isSuperZZTLike() ? 16 : 10); i++)
			stream.writePString(flags.size() > i ? flags.get(i) : "", 20);
		stream.writePShort(boardTimeSec);
		stream.writePShort(boardTimeHsec);
		stream.writePBoolean(isSave);
		if (engineDefinition.getBaseKind().isSuperZZTLike()) {
			stream.writePShort(stonesOfPower);
			stream.pad(12); // world info
		} else {
			stream.pad(14); // world info
		}
		stream.padTo(pos + (engineDefinition.getBaseKind().isSuperZZTLike() ? 1024 : 512)); // world padding

		// boards
		for (Board board : boards) {
			board.writeZ(stream);
		}
	}
}
