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
package pl.asie.zzttools.util;

import lombok.Data;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public class Property<T> {
    @Getter
    private String name;
    @Getter
    private final T defaultValue;
    @Getter
    private final List<PropertyAffect> affects;
    
    private Property(String name, T defaultValue, PropertyAffect... affects) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.affects = Arrays.asList(affects);
    }

    public boolean isTransient() {
        return this.name == null;
    }

    public static <T> Property<T> createTransient(PropertyAffect... affects) {
        return new Property<T>(null, null, affects);
    }

    public static <T> Property<T> createTransient(T defaultValue, PropertyAffect... affects) {
        return new Property<T>(null, defaultValue, affects);
    }

    public static <T> Property<T> create(String name, PropertyAffect... affects) {
        return new Property<T>(name, null, affects);
    }

    public static <T> Property<T> create(String name, T defaultValue, PropertyAffect... affects) {
        return new Property<T>(name, defaultValue, affects);
    }
}
