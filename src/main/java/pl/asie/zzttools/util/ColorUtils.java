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

public final class ColorUtils {
	private ColorUtils() {

	}

	// https://www.compuphase.com/cmetric.htm
	public static float distance(int a, int b) {
		int ar = (a >> 16) & 0xFF;
		int ag = (a >> 8) & 0xFF;
		int ab = a & 0xFF;
		int br = (b >> 16) & 0xFF;
		int bg = (b >> 8) & 0xFF;
		int bb = b & 0xFF;

		int rmean = (ar + br) >> 1;
		int rdiff = (ar - br);
		int gdiff = (ag - bg);
		int bdiff = (ab - bb);

		return (float) ((((512+rmean)*rdiff*rdiff)>>8) + 4*gdiff*gdiff + (((767-rmean)*bdiff*bdiff)>>8)) / (768.0f * 768.0f);
	}

	public static int mix4equal(int a, int b, int c, int d) {
		int ar = (a >> 16) & 0xFF;
		int ag = (a >> 8) & 0xFF;
		int ab = a & 0xFF;
		int br = (b >> 16) & 0xFF;
		int bg = (b >> 8) & 0xFF;
		int bb = b & 0xFF;
		int cr = (c >> 16) & 0xFF;
		int cg = (c >> 8) & 0xFF;
		int cb = c & 0xFF;
		int dr = (d >> 16) & 0xFF;
		int dg = (d >> 8) & 0xFF;
		int db = d & 0xFF;

		int xr = (ar + br + cr + dr) >> 2;
		int xg = (ag + bg + cg + dg) >> 2;
		int xb = (ab + bb + cb + db) >> 2;

		return ((xr & 0xFF) << 16) | ((xg & 0xFF) << 8) | (xb & 0xFF);
	}

	public static int mix(int a, int b, float amount) {
		int ar = (a >> 16) & 0xFF;
		int ag = (a >> 8) & 0xFF;
		int ab = a & 0xFF;
		int br = (b >> 16) & 0xFF;
		int bg = (b >> 8) & 0xFF;
		int bb = b & 0xFF;

		int cr = (int) ((ar * (1 - amount)) + (br * amount));
		int cg = (int) ((ag * (1 - amount)) + (bg * amount));
		int cb = (int) ((ab * (1 - amount)) + (bb * amount));

		return ((cr & 0xFF) << 16) | ((cg & 0xFF) << 8) | (cb & 0xFF);
	}

	public static void main(String[] args) {
		System.out.println(distance(0x000000, 0x010101));
		System.out.println(distance(0x000000, 0x020202));
		System.out.println(distance(0x000000, 0x040404));
		System.out.println(distance(0x000000, 0x080808));
		System.out.println(distance(0x000000, 0x101010));
		System.out.println(distance(0x000000, 0x202020));
		System.out.println(distance(0x000000, 0x404040));
		System.out.println(distance(0x000000, 0x808080));
		System.out.println(distance(0x000000, 0xFFFFFF));
	}
}
