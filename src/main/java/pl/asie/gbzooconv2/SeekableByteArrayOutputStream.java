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
package pl.asie.gbzooconv2;

import java.io.ByteArrayOutputStream;

public class SeekableByteArrayOutputStream extends ByteArrayOutputStream {
	public void writeAt(int index, int value) {
		if (index >= buf.length) {
			throw new ArrayIndexOutOfBoundsException(index + " > " + (buf.length - 1));
		}
		buf[index] = (byte) value;
	}
}
