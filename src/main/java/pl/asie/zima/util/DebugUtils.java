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

public final class DebugUtils {
    public static final Object PRINT_LOCK = new Object();

    private DebugUtils() {

    }

    public static void print1DArray(float[] arr, int pitch) {
        synchronized (PRINT_LOCK) {
            for (int i = 0; i < arr.length; i++) {
                if (i > 0 && (i % pitch) == 0) {
                    System.out.printf("[%d]", i);
                    System.out.println();
                }
                System.out.printf("%.4f\t", arr[i]);
            }
            System.out.println();
        }
    }
}
