/**
 * Copyright (c) 2020 Adrian Siekierka
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
package pl.asie.zzttools.zzt;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@SuppressWarnings("ConstantConditions")
@Getter
@Builder
public final class Element {
    @Builder.Default
    private final int character = ' ';
    @Builder.Default
    private final int color = COLOR_CHOICE_ON_BLACK;
    @Builder.Default
    private final boolean destructible = false;
    @Builder.Default
    private final boolean pushable = false;
    @Builder.Default
    private final boolean visibleInDark = false;
    @Builder.Default
    private final boolean placeableOnTop = false;
    @Builder.Default
    private final boolean walkable = false;
    @Builder.Default
    private final boolean hasDrawProc = false;
    @Builder.Default
    private final int cycle = -1;
    @Builder.Default
    private final int scoreValue = 0;
    @Builder.Default
    private final String name = "";
    @Builder.Default
    private final int id = -1;

    public String getOopName() {
        // TODO
        return null;
    }

    public boolean isStat() {
        return cycle >= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Element element = (Element) o;
        return id == element.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Element{" +
                "name='" + name + '\'' +
                ", id=" + id +
                '}';
    }

    public static final int COLOR_SPECIAL_MIN = 0xF0;
    public static final int COLOR_CHOICE_ON_BLACK = 0xFF;
    public static final int COLOR_WHITE_ON_CHOICE = 0xFE;
    public static final int COLOR_CHOICE_ON_CHOICE = 0xFD;
}
