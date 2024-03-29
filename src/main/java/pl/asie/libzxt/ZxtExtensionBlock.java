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

import lombok.Getter;
import lombok.With;

@Getter
@With
public class ZxtExtensionBlock {
    private final short flags;
    private final ZxtExtensionId id;
    private final byte reserved0;
    private final byte[] data;

    public ZxtExtensionBlock(short flags, ZxtExtensionId id, byte reserved0, byte[] data) {
        this.flags = flags;
        this.id = id;
        this.reserved0 = reserved0;
        this.data = data;
    }

    public ZxtExtensionBlock(short flags, ZxtExtensionId id, byte[] data) {
        this(flags, id, (byte) 0, data);
    }

    public ZxtExtensionBlock(short flags, ZxtExtensionId id) {
        this(flags, id, null);
    }

}
