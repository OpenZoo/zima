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
package pl.asie.zima.image.gui;

import lombok.Data;
import pl.asie.zima.image.ElementRule;
import pl.asie.zima.image.ImageConverterType;
import pl.asie.zima.util.AspectRatioPreservationMode;

import java.util.List;

@Data
public class ZimaProfileSettings {
    private int[] allowedCharacters;
    private int[] allowedColors;
    private int[] allowedColorPairs;
    private List<ElementRule> allowedElements;

    private byte[] customCharset;
    private int[] customPalette;

    private Integer maxStatCount;
    private Boolean colorsBlink;
    private Float contrastReduction;
    private Float accurateApproximate;

    private AspectRatioPreservationMode aspectRatioPreservationMode;
    private ImageConverterType imageConverterType;
}
