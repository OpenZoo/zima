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
import lombok.RequiredArgsConstructor;
import pl.asie.tinyzooconv.exceptions.BinarySerializerException;

import java.io.IOException;

@Getter
@RequiredArgsConstructor
public class CountingBinarySerializerOutput implements BinarySerializerOutput {
	private final BinarySerializerOutput parent;
	private int size;

	@Override
	public void writeByte(int v) throws IOException {
		size += 1;
	}

	@Override
	public void writeShort(int v) throws IOException {
		size += 2;
	}

	@Override
	public int getNearPointerSize() {
		return parent.getNearPointerSize();
	}

	@Override
	public void writeNearPointerTo(BinarySerializable object) throws IOException, BinarySerializerException {
		size += getNearPointerSize();
	}

	@Override
	public int getFarPointerSize() {
		return parent.getFarPointerSize();
	}

	@Override
	public void writeFarPointerTo(BinarySerializable object) throws IOException, BinarySerializerException {
		size += getFarPointerSize();
	}
}
