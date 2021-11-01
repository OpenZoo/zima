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

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ZxtHeaderType {
    ZZT_WORLD(0xF227),
    SUPER_ZZT_WORLD(0xF527),
    ZZT_BOARD(0xB227),
    SUPER_ZZT_BOARD(0xB527);

    private static final Map<Integer, ZxtHeaderType> typesByMagic = Arrays.stream(ZxtHeaderType.values())
            .collect(Collectors.toMap(ZxtHeaderType::getMagic, a -> a));
    private final int magic;

    ZxtHeaderType(int magic) {
        this.magic = magic;
    }

    public int getMagic() {
        return magic;
    }

    public boolean isWorld() {
        return this == ZZT_WORLD || this == SUPER_ZZT_WORLD;
    }

    public boolean isBoard() {
        return this == ZZT_BOARD || this == SUPER_ZZT_BOARD;
    }

    public boolean isZzt() {
        return this == ZZT_WORLD || this == ZZT_BOARD;
    }

    public boolean isSuperZzt() {
        return this == SUPER_ZZT_WORLD || this == SUPER_ZZT_BOARD;
    }

    public static ZxtHeaderType byMagic(int magic) {
        return typesByMagic.get(magic);
    }
}
