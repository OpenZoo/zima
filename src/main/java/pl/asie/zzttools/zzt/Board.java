/**
 * Copyright (c) 2020 Adrian Siekierka
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
package pl.asie.zzttools.zzt;

import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class Board {
	public static final int WIDTH = 60;
	public static final int HEIGHT = 25;
	public static final int OUTER_WIDTH = WIDTH + 2;
	public static final int OUTER_HEIGHT = HEIGHT + 2;

	private String name = "";
	private int maxShots = 255;
	private boolean dark;
	private int[] neighborBoards = new int[4];
	private boolean reenterWhenZapped;
	private String message = "";
	private int startPlayerX;
	private int startPlayerY;
	private int timeLimitSec;

	private Element[] elements = new Element[OUTER_WIDTH * OUTER_HEIGHT];
	private byte[] colors = new byte[OUTER_WIDTH * OUTER_HEIGHT];

	private List<Stat> stats = new ArrayList<>();

	public Board() {
		this(WIDTH >> 1, HEIGHT >> 1);
	}

	public Board(int playerX, int playerY) {
		Arrays.fill(elements, Element.EMPTY);

		// set board edges
		for (int i = 0; i < OUTER_WIDTH; i++) {
			setElement(i, 0, Element.BOARD_EDGE);
			setElement(i, OUTER_HEIGHT - 1, Element.BOARD_EDGE);
		}
		for (int i = 0; i < OUTER_HEIGHT; i++) {
			setElement(0, i, Element.BOARD_EDGE);
			setElement(OUTER_WIDTH - 1, i, Element.BOARD_EDGE);
		}

		setElement(playerX, playerY, Element.PLAYER);
		setColor(playerX, playerY, 0x1F);

		Stat playerStat = new Stat();
		playerStat.setX(playerX);
		playerStat.setY(playerY);
		playerStat.setCycle(1);
		stats.add(playerStat);
	}

	public Element getElement(int x, int y) {
		return elements[y * OUTER_WIDTH + x];
	}

	public int getColor(int x, int y) {
		return (int) colors[y * OUTER_WIDTH + x] & 0xFF;
	}

	public void setElement(int x, int y, Element element) {
		elements[y * OUTER_WIDTH + x] = element;
	}

	public void setColor(int x, int y, int color) {
		colors[y * OUTER_WIDTH + x] = (byte) color;
	}

	public int addStat(Stat stat) {
		stats.add(stat);
		return stats.size() - 1;
	}

	public Stat getStat(int id) {
		return stats.get(id);
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

	public void readZ(ZInputStream stream) throws IOException {
		// TODO: take board size into account
		stream.readPShort();

		this.name = stream.readPString(50);

		int ix = 1;
		int iy = 1;
		int rleCount = 0;
		Element rleElement = Element.EMPTY;
		int rleColor = 0;
		do {
			if (rleCount <= 0) {
				rleCount = stream.readPByte();
				if (rleCount == 0) rleCount = 256;
				rleElement = Element.fromOrdinal(stream.readPByte());
				rleColor = stream.readPByte();
			}
			setElement(ix, iy, rleElement);
			setColor(ix, iy, rleColor);
			ix++;
			if (ix > WIDTH) {
				ix = 1;
				iy++;
			}
			rleCount--;
		} while (iy <= HEIGHT);

		this.maxShots = stream.readPByte();
		this.dark = stream.readPBoolean();
		for (int i = 0; i < 4; i++)
			this.neighborBoards[i] = stream.readPByte();
		this.reenterWhenZapped = stream.readPBoolean();
		this.message = stream.readPString(58);
		this.startPlayerX = stream.readPByte();
		this.startPlayerY = stream.readPByte();
		this.timeLimitSec = stream.readPShort();
		if (stream.skip(16) != 16) {
			throw new IOException();
		}

		int statCount = stream.readPShort();
		stats = new ArrayList<>();
		for (int i = 0; i <= statCount; i++) {
			Stat stat = new Stat();
			stat.readZ(stream);
			stats.add(stat);
		}
	}

	public void writeZ(ZOutputStream outStream) throws IOException {
		try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(); ZOutputStream stream = new ZOutputStream(byteStream, outStream.isSuperZzt())) {
			stream.writePString(this.name, 50);

			// fancy RLE logic
			int ix = 1;
			int iy = 1;
			int rleCount = 1;
			Element rleElement = getElement(ix, iy);
			int rleColor = getColor(ix, iy);
			do {
				ix++;
				if (ix > WIDTH) {
					ix = 1;
					iy++;
				}

				if (getColor(ix, iy) == rleColor && getElement(ix, iy) == rleElement && rleCount < 255 && iy <= HEIGHT) {
					rleCount++;
				} else {
					stream.writePByte(rleCount);
					stream.writePByte(rleElement.ordinal());
					stream.writePByte(rleColor);
					rleElement = getElement(ix, iy);
					rleColor = getColor(ix, iy);
					rleCount = 1;
				}
			} while (iy <= HEIGHT);

			stream.writePByte(this.maxShots);
			stream.writePBoolean(this.dark);
			for (int i = 0; i < 4; i++)
				stream.writePByte(this.neighborBoards[i]);
			stream.writePBoolean(this.reenterWhenZapped);
			stream.writePString(this.message, 58);
			stream.writePByte(this.startPlayerX);
			stream.writePByte(this.startPlayerY);
			stream.writePShort(this.timeLimitSec);
			stream.pad(16);

			stream.writePShort(stats.size() - 1);
			for (Stat stat : stats) {
				stat.writeZ(stream);
			}

			byte[] result = byteStream.toByteArray();
			outStream.writePShort(result.length);
			outStream.write(result);
		}
	}
}
