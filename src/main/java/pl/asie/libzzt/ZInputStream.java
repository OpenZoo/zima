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

import lombok.Getter;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ZInputStream extends FilterInputStream {

	@Getter
	private final Platform platform;
	@Getter
	private int position = 0;

	public ZInputStream(InputStream in, Platform platform) {
		super(in);
		this.platform = platform;
	}

	@Override
	public int read() throws IOException {
		int v = super.read();
		this.position++;
		return v;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int l = super.read(b, off, len);
		this.position += l;
		return l;
	}

	@Override
	public long skip(long n) throws IOException {
		long l = super.skip(n);
		position += (int) l;
		return l;
	}

	public long skipTo(long position) throws IOException {
		if (this.position > position) {
			throw new IOException("Requested skipping to " + position + ", but cursor already at " + this.position);
		}
		return skip(position - this.position);
	}

	public int readPByte() throws IOException {
		return read() & 0xFF;
	}

	public boolean readPBoolean() throws IOException {
		return readPByte() != 0;
	}

	public int readPShort() throws IOException {
		int lsb = read() & 0xFF;
		int value = lsb | ((read() & 0xFF) << 8);
		if (value >= 32768) {
			return value - 65536;
		} else {
			return value;
		}
	}

	public int readPWord() throws IOException {
		int lsb = read() & 0xFF;
		return lsb | ((read() & 0xFF) << 8);
	}

	public String readPString(int length) throws IOException {
		int textLength = Math.min(length, readPByte());
		int remLength = length - textLength;
		byte[] data = new byte[textLength];
		if (read(data) < textLength) {
			throw new IOException("Not enough data in buffer!");
		}
		if (skip(remLength) < remLength) {
			throw new IOException("Not enough data in buffer!");
		}
		return new String(data, StandardCharsets.ISO_8859_1);
	}
}
