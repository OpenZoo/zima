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
package pl.asie.libzxt;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class ZxtFlag {
    public static final short PARSING_MUST = (short) 0x1;
    public static final short READING_MUST = (short) 0x2;
    public static final short WRITING_MUST = (short) 0x4;
    public static final short PLAYING_SHOULD = (short) 0x8;
    public static final short PLAYING_MUST = (short) 0x10;
    public static final short EDITING_SHOULD = (short) 0x20;
    public static final short PRESERVE_SHOULD = (short) 0x40;
    public static final short VANILLA_BEHAVIOR = (short) 0x80;
    public static final short ALL = (short) 0xFF;

    private ZxtFlag() {

    }

    public static String toString(int flags) {
        Map<Short, String> strings = new TreeMap<>();

        try {
            for (Field f : ZxtFlag.class.getFields()) {
                if (f.getType() == short.class) {
                    short value = f.getShort(null);
                    if ((flags & value) != 0) {
                        strings.put(value, f.getName());
                    }
                }
            }

            return "[" + String.join(", ", strings.values()) + "]";
        } catch (Exception e) {
            e.printStackTrace();
            return "???";
        }
    }
}
