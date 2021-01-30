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
package pl.asie.zima.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

public class ImageUtilsAspectRatioTest {
    private BufferedImage image(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
    }

    @Test
    public void calculateSizeIgnoreTest() {
        Assertions.assertArrayEquals(new int[] { 640, 350 },
                ImageUtils.calculateSize(
                        image(100, 100),
                        640, 350,
                        AspectRatioPreservationMode.IGNORE,
                        8, 14, false
                )
        );
    }

    @Test
    public void calculateSizePreserveTest() {
        Assertions.assertArrayEquals(new int[] { 350, 350 },
                ImageUtils.calculateSize(
                        image(100, 100),
                        640, 350,
                        AspectRatioPreservationMode.PRESERVE,
                        8, 14, false
                )
        );
    }

    @Test
    public void calculateSizeSnapCharTest() {
        Assertions.assertArrayEquals(new int[] { 352, 350 },
                ImageUtils.calculateSize(
                        image(100, 100),
                        640, 350,
                        AspectRatioPreservationMode.SNAP_CHAR,
                        8, 14, false
                )
        );

        Assertions.assertArrayEquals(new int[] { 352, 336 },
                ImageUtils.calculateSize(
                        image(100, 97),
                        352, 630,
                        AspectRatioPreservationMode.SNAP_CHAR,
                        8, 14, false
                )
        );
    }

    @Test
    public void calculateSizeSnapCenterTest() {
        Assertions.assertArrayEquals(new int[] { 344, 350 },
                ImageUtils.calculateSize(
                        image(100, 100),
                        632, 350,
                        AspectRatioPreservationMode.SNAP_CENTER,
                        8, 14, false
                )
        );

        Assertions.assertArrayEquals(new int[] { 352, 350 },
                ImageUtils.calculateSize(
                        image(100, 97),
                        352, 630,
                        AspectRatioPreservationMode.SNAP_CENTER,
                        8, 14, false
                )
        );
    }
}
