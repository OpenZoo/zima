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

	public static float lumaDistance(int a, int b) {
		if (a == b) {
			return 0.0f;
		}

		float ar = sRtoR((a >> 16) & 0xFF);
		float ag = sRtoR((a >> 8) & 0xFF);
		float ab = sRtoR(a & 0xFF);
		float br = sRtoR((b >> 16) & 0xFF);
		float bg = sRtoR((b >> 8) & 0xFF);
		float bb = sRtoR(b & 0xFF);

		float ay = 0.299f * ar + 0.587f * ag + 0.114f * ab;
		float by = 0.299f * br + 0.587f * bg + 0.114f * bb;
		float yd = ay - by;

		return yd * yd;
	}

	// https://www.compuphase.com/cmetric.htm
	public static float distance(int a, int b) {
		if (a == b) {
			return 0.0f;
		}

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

		float xr = (sRtoR(ar) + sRtoR(br) + sRtoR(cr) + sRtoR(dr)) / 4.0f;
		float xg = (sRtoR(ag) + sRtoR(bg) + sRtoR(cg) + sRtoR(dg)) / 4.0f;
		float xb = (sRtoR(ab) + sRtoR(bb) + sRtoR(cb) + sRtoR(db)) / 4.0f;

		return (RtosR(xr) << 16) | (RtosR(xg) << 8) | (RtosR(xb));
		
		/* int xr = (ar + br + cr + dr) >> 2;
		int xg = (ag + bg + cg + dg) >> 2;
		int xb = (ab + bb + cb + db) >> 2;

		return ((xr & 0xFF) << 16) | ((xg & 0xFF) << 8) | (xb & 0xFF); */
	}

	public static float sRtoR(int v) {
		float r;
		if (v < 0.04045f) {
			r = (v / 255.0f) / 12.92f;
		} else {
			r = (float) Math.pow(((v / 255.0f) + 0.055f) / 1.055f, 2.4f);
		}
		return r;
	}

	public static int RtosR(float v) {
		float r;
		if (v < 0.0031308f) {
			r =  12.92f * v;
		} else {
			r = (float) (1.055f * Math.pow(v, 1/2.4f)) - 0.055f;
		}
		if (r < 0.0f) return 0;
		if (r > 1.0f) return 255;
		return (int) (r * 255.0f) & 0xFF;
	}

	public static int mix(int a, int b, float amount) {
		float ar = sRtoR((a >> 16) & 0xFF);
		float ag = sRtoR((a >> 8) & 0xFF);
		float ab = sRtoR(a & 0xFF);
		float br = sRtoR((b >> 16) & 0xFF);
		float bg = sRtoR((b >> 8) & 0xFF);
		float bb = sRtoR(b & 0xFF);

		float cr = ((ar * (1 - amount)) + (br * amount));
		float cg = ((ag * (1 - amount)) + (bg * amount));
		float cb = ((ab * (1 - amount)) + (bb * amount));

		return (RtosR(cr) << 16) | (RtosR(cg) << 8) | (RtosR(cb));
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
