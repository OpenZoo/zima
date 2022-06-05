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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import pl.asie.tinyzooconv.exceptions.BinarySerializerException;
import pl.asie.libzzt.oop.commands.OopCommandTextLine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class BinaryTextLine implements BinarySerializable {
	private final OopCommandTextLine line;
	private final int targetId;
	private final int labelId;

	@Override
	public void serialize(BinarySerializerOutput output) throws IOException, BinarySerializerException {
		switch (line.getType()) {
			case REGULAR -> { output.writeByte(0); }
			case CENTERED -> { output.writeByte(1); }
			case HYPERLINK -> { output.writeByte(2); }
			default -> throw new RuntimeException("Unsupported type: " + line.getType());
		}
		byte[] lineText = line.getMessage().getBytes(StandardCharsets.ISO_8859_1);
		output.writeByte(lineText.length);
		for (int i = 0; i < lineText.length; i++) {
			output.writeByte((int) lineText[i] & 0xFF);
		}
		switch (line.getType()) {
			case HYPERLINK -> { output.writeByte(targetId); output.writeByte(labelId); }
		}
	}
}
