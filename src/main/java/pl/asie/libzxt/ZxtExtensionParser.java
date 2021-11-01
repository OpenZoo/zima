/**
 * Copyright (c) 2020, 2021 Adrian Siekierka
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
package pl.asie.libzxt;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;

import java.io.*;

public class ZxtExtensionParser {
    public ZxtExtensionHeader readHeader(InputStream inputStream) throws IOException, ZxtCannotParseException {
        LittleEndianDataInputStream input = new LittleEndianDataInputStream(inputStream);
        int magicShort = input.readUnsignedShort();
        ZxtHeaderType type = ZxtHeaderType.byMagic(magicShort);
        if (type == null) {
            return null;
        }

        ZxtExtensionHeader header = new ZxtExtensionHeader(type);
        int blockCount = input.readInt();
        for (int i = 0; i < blockCount; i++) {
            short flags = input.readShort();
            if ((flags & ~ZxtFlag.ALL) != 0) {
                throw new ZxtCannotParseException(String.format("Unknown flags for block #%d: %04X", i, flags));
            }
            if ((flags & ZxtFlag.PARSING_MUST) != 0) {
                throw new ZxtCannotParseException(String.format("Cannot parse block #%d due to PARSING_MUST flag being set", i));
            }

            int owner = input.readInt();
            short selector = input.readShort();
            ZxtExtensionId id = new ZxtExtensionId(owner, selector);

            byte reserved0 = input.readByte();
            int fieldLength = input.readUnsignedShort();
            if (fieldLength == 65535) {
                fieldLength = input.readInt();
                if (fieldLength < 0) {
                    throw new ZxtCannotParseException(String.format("Field for block #%d (%s) has invalid size: %d bytes", i, id, fieldLength));
                }
            }
            byte[] fieldData = fieldLength == 0 ? null : input.readNBytes(fieldLength);

            ZxtExtensionBlock block = new ZxtExtensionBlock(flags, id, reserved0, fieldData);
            header.addBlock(block);
        }

        return header;
    }

    public void writeHeader(ZxtExtensionHeader header, OutputStream outputStream) throws IOException {
        LittleEndianDataOutputStream output = new LittleEndianDataOutputStream(outputStream);
        output.writeShort(header.getType().getMagic());
        output.writeInt(header.getBlockCount());

        for (int i = 0; i < header.getBlockCount(); i++) {
            ZxtExtensionBlock block = header.getBlockAt(i);
            output.writeShort(block.getFlags());
            output.writeInt(block.getId().getOwner());
            output.writeShort(block.getId().getSelector());
            output.writeByte(block.getReserved0());

            byte[] fieldData = block.getData();
            if (fieldData == null || fieldData.length == 0) {
                output.writeShort(0);
            } else if (fieldData.length < 65535) {
                output.writeShort(fieldData.length);
                output.write(fieldData);
            } else {
                output.writeShort(65535);
                output.writeInt(fieldData.length);
                output.write(fieldData);
            }
        }
    }
}
