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

import java.util.Objects;

public class ZxtExtensionId {
    private final int owner; /* 0x00000000 - 0xFFFFFFFF */
    private final short selector; /* 0x0000 - 0xFFFF */

    public ZxtExtensionId(int owner, short selector) {
        this.owner = owner;
        this.selector = selector;
    }

    public int getOwner() {
        return owner;
    }

    public short getSelector() {
        return selector;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZxtExtensionId that = (ZxtExtensionId) o;
        return owner == that.owner && selector == that.selector;
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, selector);
    }

    @Override
    public String toString() {
        return String.format("%08X:%04X", owner, selector);
    }
}
