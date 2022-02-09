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

public enum DitherMatrix {
	FLOYD_STEINBERG("Floyd-Steinberg", new float[] {
			0, 0, 0,
			0, 0, 7/16f,
			3/16f, 5/16f, 1/16f
	}),
	JJN("Jarvis-Judice-Ninke", new float[] {
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0,
			0, 0, 0, 7/48f, 5/48f,
			3/48f, 5/48f, 7/48f, 5/48f, 3/48f,
			1/48f, 3/48f, 5/48f, 3/48f, 5/48f
	}),
	ATKINSON("Atkinson", new float[] {
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0,
			0, 0, 0, 1/8f, 1/8f,
			0, 1/8f, 1/8f, 1/8f, 0,
			0, 0, 1/8f, 0, 0
	}),
	SIERRA("Sierra", new float[] {
			0, 0, 0, 0, 0,
			0, 0, 0, 0, 0,
			0, 0, 0, 5/32f, 3/32f,
			2/32f, 4/32f, 5/32f, 4/32f, 2/32f,
			0, 2/32f, 3/32f, 2/32f, 0
	});

	private final String name;
	private final float[] matrix;
	private final int dimSize;

	DitherMatrix(String name, float[] matrix) {
		this.name = name;
		this.matrix = matrix;
		this.dimSize = (int) Math.sqrt(matrix.length);
	}

	public float[] getMatrix() {
		return matrix;
	}

	public int getDimSize() {
		return this.dimSize;
	}

	public int getDimOffset() {
		return (this.dimSize - 1) / 2;
	}

	@Override
	public String toString() {
		return name;
	}
}
