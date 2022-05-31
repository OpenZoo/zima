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

import pl.asie.tinyzooconv.exceptions.BinarySerializerException;

import java.io.IOException;

public abstract class BaseBinarySerializerOutput implements BinarySerializerOutput {
	protected final SeekableByteArrayOutputStream bytes = new SeekableByteArrayOutputStream();

	@Override
	public void writeByte(int v) throws IOException {
		bytes.write(v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		bytes.write(v & 0xFF);
		bytes.write(v >> 8);
	}

	public int size() {
		return bytes.size();
	}

	public byte[] toByteArray() {
		return bytes.toByteArray();
	}

	@Override
	public int getNearPointerSize() {
		return getFarPointerSize();
	}

	@Override
	public void writeNearPointerTo(BinarySerializable object) throws IOException, BinarySerializerException {
		writeFarPointerTo(object);
	}
}
