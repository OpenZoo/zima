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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;

@Data
public class Board {
	private String name = "";
	private int maxShots = 255;
	private boolean dark;
	private int[] neighborBoards = new int[4];
	private boolean reenterWhenZapped;
	private String message = "";
	private int startPlayerX;
	private int startPlayerY;
	private int drawOffsetX; // Super ZZT
	private int drawOffsetY; // Super ZZT
	private int timeLimitSec;

	private final EngineDefinition engineDefinition;
	private final int width, height, outerWidth, outerHeight;
	private final byte[] elements;
	private final byte[] colors;

	private List<Stat> stats = new ArrayList<>();

	public Board(EngineDefinition engineDefinition) {
		this(engineDefinition, engineDefinition.getBoardWidth() >> 1, engineDefinition.getBoardHeight() >> 1);
	}

	public Board(EngineDefinition engineDefinition, int playerX, int playerY) {
		this.engineDefinition = engineDefinition;
		this.width = engineDefinition.getBoardWidth();
		this.height = engineDefinition.getBoardHeight();
		this.outerWidth = this.width + 2;
		this.outerHeight = this.height + 2;
		this.elements = new byte[this.outerWidth * this.outerHeight];
		this.colors = new byte[this.outerWidth * this.outerHeight];

		Arrays.fill(elements, (byte) 0);
		Element boardEdge = engineDefinition.getElements().byInternalName("BOARD_EDGE");
		Element player = engineDefinition.getElements().byInternalName("PLAYER");

		// set board edges
		for (int i = 0; i < this.outerWidth; i++) {
			setElement(i, 0, boardEdge);
			setElement(i, this.outerHeight - 1, boardEdge);
		}
		for (int i = 1; i < this.outerHeight - 1; i++) {
			setElement(0, i, boardEdge);
			setElement(this.outerWidth - 1, i, boardEdge);
		}

		if (playerX >= 1 && playerX <= this.width && playerY >= 1 && playerY <= this.height) {
			setElement(playerX, playerY, player);
			setColor(playerX, playerY, 0x1F);
		}

		Stat playerStat = new Stat();
		playerStat.setX(playerX);
		playerStat.setY(playerY);
		playerStat.setCycle(1);
		stats.add(playerStat);
	}

	public Element getElement(int x, int y) {
		return this.engineDefinition.getElements().byId(getElementId(x, y));
	}

	public boolean isValidElement(int x, int y) {
		return this.engineDefinition.getElements().isIdValid(getElementId(x, y));
	}

	public int getElementId(int x, int y) {
		return (int) (elements[y * this.outerWidth + x]) & 0xFF;
	}

	public int getColor(int x, int y) {
		return (int) colors[y * this.outerWidth + x] & 0xFF;
	}

	public void setElement(int x, int y, Element element) {
		elements[y * this.outerWidth + x] = (byte) element.getId();
	}

	public void setColor(int x, int y, int color) {
		colors[y * this.outerWidth + x] = (byte) color;
	}

	public int addStat(Stat stat) {
		stats.add(stat);
		return stats.size() - 1;
	}

	public Stat getStat(int id) {
		return stats.get(id);
	}

	public OptionalInt getStatId(Stat stat) {
		int pos = stats.indexOf(stat);
		return pos >= 0 ? OptionalInt.of(pos) : OptionalInt.empty();
	}

	public int getStatCount() {
		return stats.size() - 1;
	}

	public void removeStat(int statId) {
		stats.remove(statId);
	}

	public void removeStat(Stat stat) {
		stats.remove(stat);
	}

	public Stat getStatAt(int x, int y) {
		for (Stat stat : stats) {
			if (stat.getX() == x && stat.getY() == y) {
				return stat;
			}
		}
		return null;
	}

	public void readZ(ZInputStream inStream) throws IOException {
		int boardSize = inStream.readPWord();
		byte[] data = new byte[boardSize];
		int dataRead = inStream.read(data, 0, boardSize);
		if (dataRead != boardSize) {
			throw new IOException("Could not read all board bytes: read " + dataRead + " bytes, expected " + boardSize + " bytes");
		}

		try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data); ZInputStream stream = new ZInputStream(byteStream, inStream.getEngineDefinition())) {
			this.name = stream.readPString(engineDefinition.getBaseKind().isSuperZZTLike() ? 60 : 50);

			int ix = 1;
			int iy = 1;
			int rleCount = 0;
			Element rleElement = engineDefinition.getElements().getEmpty();
			int rleColor = 0;
			do {
				if (rleCount <= 0) {
					rleCount = stream.readPByte();
					if (rleCount == 0) rleCount = 256;
					rleElement = engineDefinition.getElements().byId(stream.readPByte());
					rleColor = stream.readPByte();
				}
				setElement(ix, iy, rleElement);
				setColor(ix, iy, rleColor);
				ix++;
				if (ix > this.width) {
					ix = 1;
					iy++;
				}
				rleCount--;
			} while (iy <= this.height);

			this.maxShots = stream.readPByte();
			if (engineDefinition.getBaseKind().isZZTLike()) {
				this.dark = stream.readPBoolean();
			}
			for (int i = 0; i < 4; i++)
				this.neighborBoards[i] = stream.readPByte();
			this.reenterWhenZapped = stream.readPBoolean();
			if (engineDefinition.getBaseKind().isZZTLike()) {
				this.message = stream.readPString(58);
			}
			this.startPlayerX = stream.readPByte();
			this.startPlayerY = stream.readPByte();
			if (engineDefinition.getBaseKind().isSuperZZTLike()) {
				this.drawOffsetX = stream.readPShort();
				this.drawOffsetY = stream.readPShort();
			}
			this.timeLimitSec = stream.readPShort();
			int skipCount = engineDefinition.getBaseKind().isSuperZZTLike() ? 14 : 16;
			if (stream.skip(skipCount) != skipCount) {
				throw new IOException();
			}

			int statCount = stream.readPShort();
			stats = new ArrayList<>();
			for (int i = 0; i <= statCount; i++) {
				Stat stat = new Stat();
				stat.readZ(stream);
				stats.add(stat);
			}
			for (Stat stat : stats) {
				stat.copyStatIdToStat(this);
			}
		}
	}

	public void writeZ(ZOutputStream outStream) throws IOException {
		try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(); ZOutputStream stream = new ZOutputStream(byteStream, outStream.getEngineDefinition())) {
			stream.writePString(this.name, engineDefinition.getBaseKind().isSuperZZTLike() ? 60 : 50);

			// fancy RLE logic
			int ix = 1;
			int iy = 1;
			int rleCount = 1;
			Element rleElement = getElement(ix, iy);
			int rleColor = getColor(ix, iy);
			do {
				ix++;
				if (ix > this.width) {
					ix = 1;
					iy++;
				}

				if (getColor(ix, iy) == rleColor && getElement(ix, iy) == rleElement && rleCount < 255 && iy <= this.height) {
					rleCount++;
				} else {
					stream.writePByte(rleCount);
					stream.writePByte(rleElement.getId());
					stream.writePByte(rleColor);
					rleElement = getElement(ix, iy);
					rleColor = getColor(ix, iy);
					rleCount = 1;
				}
			} while (iy <= this.height);

			stream.writePByte(this.maxShots);
			if (engineDefinition.getBaseKind().isZZTLike()) {
				stream.writePBoolean(this.dark);
			}
			for (int i = 0; i < 4; i++)
				stream.writePByte(this.neighborBoards[i]);
			stream.writePBoolean(this.reenterWhenZapped);
			if (engineDefinition.getBaseKind().isZZTLike()) {
				stream.writePString(this.message, 58);
			}
			stream.writePByte(this.startPlayerX);
			stream.writePByte(this.startPlayerY);
			if (engineDefinition.getBaseKind().isSuperZZTLike()) {
				stream.writePShort(this.drawOffsetX);
				stream.writePShort(this.drawOffsetY);
			}
			stream.writePShort(this.timeLimitSec);
			stream.pad(engineDefinition.getBaseKind().isSuperZZTLike() ? 14 : 16);

			stream.writePShort(stats.size() - 1);

			for (Stat stat : stats) {
				stat.copyStatToStatId(this);
			}
			for (Stat stat : stats) {
				stat.writeZ(stream);
			}

			byte[] result = byteStream.toByteArray();
			outStream.writePShort(result.length);
			outStream.write(result);
		}
	}

	@Override
	public String toString() {
		return "Board{" + this.getName() + "}";
	}
}
