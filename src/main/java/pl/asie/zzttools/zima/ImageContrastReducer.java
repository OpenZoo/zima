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
package pl.asie.zzttools.zima;

import pl.asie.zzttools.util.ColorUtils;
import pl.asie.zzttools.zzt.TextVisualData;

public class ImageContrastReducer {
    private final TextVisualData visual;
    private final float value;
    private final float[] colDistPrecalc;

    public ImageContrastReducer(TextVisualData visual, float value) {
        this.visual = visual;
        this.value = value;

        colDistPrecalc = new float[256];
        for (int i = 0; i < 256; i++) {
            int bg = visual.getPalette()[(i >> 4) & 0x0F];
            int fg = visual.getPalette()[i & 0x0F];
            colDistPrecalc[i] = ColorUtils.distance(bg, fg);
        }

    }

    public ElementResult apply(ElementResult result) {
        // hardcoded patch: blending characters need to be tweaked to penalize high-contrast variants
        float crDiff = (visual.getCharWidth() >> 1) * (visual.getCharHeight() >> 1) * value * colDistPrecalc[result.getColor()];
        result.setMse(result.getMse() + crDiff);
        return result;
    }
}
