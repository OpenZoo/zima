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

import java.io.IOException;
import java.io.InputStream;

public final class PaletteLoaderUtils {
	private static final int[] PAL_TO_PLD = new int[] {
			0, 1, 2, 3, 4, 5, 20, 7,
			56, 57, 58, 59, 60, 61, 62, 63
	};

	private static int[] readRgb3(InputStream stream, int count, int max) throws IOException {
		int[] colors = new int[count];
		for (int i = 0; i < count; i++) {
			int r = stream.read() & 0xFF;
			int g = stream.read() & 0xFF;
			int b = stream.read() & 0xFF;
			if (r > max) r = max;
			if (g > max) g = max;
			if (b > max) b = max;
			r = (r * 255 / max);
			g = (g * 255 / max);
			b = (b * 255 / max);
			colors[i] = (r << 16) | (g << 8) | b;
		}
		return colors;
	}

	public static int[] readPalFile(InputStream stream) throws IOException {
		return readRgb3(stream, 16, 63);
	}

	public static int[] readPldFile(InputStream stream) throws IOException {
		int[] pldData = readRgb3(stream, 64, 63);
		int[] palData = new int[16];
		for (int i = 0; i < 16; i++) {
			palData[i] = pldData[PAL_TO_PLD[i]];
		}
		return palData;
	}
}
