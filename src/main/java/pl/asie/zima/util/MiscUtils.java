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
package pl.asie.zima.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.function.IntFunction;

public final class MiscUtils {
    private MiscUtils() {

    }

    public static String zztToUtf(String s) {
        try {
            return new String(s.getBytes("Latin1"), "Cp437");
        } catch (UnsupportedEncodingException e) {
            return s;
        }
    }

    public static int clamp(int i, int min, int max) {
        return (i < min) ? min : Math.min(i, max);
    }

    public static <T> T[] shift(T[] input, int count, IntFunction<T[]> function) {
        if (count >= input.length) {
            return function.apply(0);
        } else {
            T[] output = function.apply(input.length - count);
            System.arraycopy(input, count, output, 0, output.length);
            return output;
        }
    }
}
