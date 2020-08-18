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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Data
public class Stat {
	private int x;
	private int y;
	private int stepX;
	private int stepY;
	private int cycle;
	private int p1;
	private int p2;
	private int p3;
	private int follower = -1;
	private int leader = -1;
	private Element underElement = ElementsZZT.EMPTY;
	private int underColor = 0;
	private String data;
	private int dataPos;
	private int boundStatId;

	public void readZ(ZInputStream stream) throws IOException {
		this.x = stream.readPByte();
		this.y = stream.readPByte();
		this.stepX = stream.readPShort();
		this.stepY = stream.readPShort();
		this.cycle = stream.readPShort();
		this.p1 = stream.readPByte();
		this.p2 = stream.readPByte();
		this.p3 = stream.readPByte();
		this.follower = stream.readPShort();
		this.leader = stream.readPShort();
		this.underElement = ElementsZZT.byId(stream.readPByte());
		this.underColor = stream.readPByte();
		if (stream.skip(4) != 4) {
			throw new IOException("Could not skip data^!");
		}
		this.dataPos = stream.readPShort();
		int dataLen = stream.readPShort();
		if (!stream.isSuperZzt()) {
			if (stream.skip(8) != 8) {
				throw new IOException("Could not skip unk!");
			}
		}
		if (dataLen > 0) {
			byte[] dataBytes = new byte[dataLen];
			if (stream.read(dataBytes) != dataBytes.length) {
				throw new IOException("Could not read stat data!");
			}
			data = new String(dataBytes, StandardCharsets.ISO_8859_1);
		}
		boundStatId = (dataLen < 0) ? -dataLen : 0;
	}

	public void writeZ(ZOutputStream stream) throws IOException {
		byte[] dataBytes = data != null ? data.getBytes(StandardCharsets.ISO_8859_1) : null;
		stream.writePByte(this.x);
		stream.writePByte(this.y);
		stream.writePShort(this.stepX);
		stream.writePShort(this.stepY);
		stream.writePShort(this.cycle);
		stream.writePByte(this.p1);
		stream.writePByte(this.p2);
		stream.writePByte(this.p3);
		stream.writePShort(this.follower);
		stream.writePShort(this.leader);
		stream.writePByte(this.underElement.getId());
		stream.writePByte(this.underColor);
		stream.pad(4); // data^
		stream.writePShort(this.dataPos);
		stream.writePShort(boundStatId > 0 ? (-boundStatId) : ((dataBytes != null) ? dataBytes.length : 0));
		if (!stream.isSuperZzt()) {
			stream.pad(8); // unk
		}
		if (boundStatId == 0 && dataBytes != null) {
			stream.write(dataBytes);
		}
	}
}
