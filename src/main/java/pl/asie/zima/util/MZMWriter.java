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
package pl.asie.zima.util;

import pl.asie.libzzt.TextVisualRenderer;

import java.io.IOException;
import java.io.OutputStream;

public class MZMWriter {
	private static final byte[] MAGIC = {'M', 'Z', 'M', '3'};

	public static void write(OutputStream s, int width, int height, TextVisualRenderer.ByteGetter charGetter, TextVisualRenderer.ByteGetter colorGetter) throws IOException {
		s.write(MAGIC);
		s.write(width & 0xFF); s.write(width >> 8);
		s.write(height & 0xFF); s.write(height >> 8);
		s.write(0); s.write(0); s.write(0); s.write(0); // No robot data in file
		s.write(0); // No robots in file
		s.write(1); // Layer storage mode
		s.write(0); // Not a savegame
		s.write(84); s.write(2); // MegaZeux 2.84 introduced MZM3
		s.write(0); s.write(0); s.write(0); // reserved

		for (int iy = 0; iy < height; iy++) {
			for (int ix = 0; ix < width; ix++) {
				s.write(charGetter.get(ix, iy));
				s.write(colorGetter.get(ix, iy));
			}
		}
	}
}
