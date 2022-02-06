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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ZOutputStream extends FilterOutputStream {
	@Getter
	private final Platform platform;

	public ZOutputStream(OutputStream out, Platform platform) {
		super(out);
		this.platform = platform;
	}

	public void pad(int amount) throws IOException {
		for (int i = 0; i < amount; i++) {
			write(0);
		}
	}

	public void writePByte(int v) throws IOException {
		write(v & 0xFF);
	}

	public void writePBoolean(boolean v) throws IOException {
		writePByte(v ? 1 : 0);
	}

	public void writePShort(int v) throws IOException {
		write(v & 0xFF);
		write((v >> 8) & 0xFF);
	}

	public void writePString(String text, int length) throws IOException {
		byte[] textBytes = text.getBytes(StandardCharsets.ISO_8859_1);
		if (textBytes.length > length) {
			throw new IOException("String too long: '" + text + "' (" + text.length() + " > " + length + ")");
		}
		write(textBytes.length);
		for (int i = 0; i < length; i++) {
			if (i < textBytes.length) {
				write(textBytes[i]);
			} else {
				write(0);
			}
		}
	}
}
