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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;

public class MiscUtilsTest {
    @SuppressWarnings("StringBufferReplaceableByString")
    @Test
    public void zztToUtfTest() {
        StringBuilder s = new StringBuilder();
        s.appendCodePoint(0xDB);
        s.appendCodePoint(0xAA);
        String converted = MiscUtils.zztToUtf(s.toString());
        Assertions.assertEquals("█¬", converted);
    }
}
