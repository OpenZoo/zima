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

import lombok.Getter;

public class Gaussian2DKernel {
    @Getter private final int radius, width;
    @Getter private final float sigma;
    @Getter private final float[][] kernel;

    public Gaussian2DKernel(float sigma, int radius) {
        this.sigma = sigma;
        this.radius = radius;
        this.width = (radius * 2) + 1;

        this.kernel = new float[this.width][this.width];
        float kernelSum = 0.0f;

        for (int ky = -this.radius; ky <= this.radius; ky++) {
            for (int kx = -this.radius; kx <= this.radius; kx++) {
                double v = (1.0 / (2 * Math.PI * sigma * sigma)) * Math.pow(Math.E, -((kx*kx + ky*ky) / (2*sigma*sigma)));
                this.kernel[kx + this.radius][ky + this.radius] = (float) v;
                kernelSum += (float) v;
            }
        }

        for (int ky = 0; ky < this.width; ky++) {
            for (int kx = 0; kx < this.width; kx++) {
                kernel[kx][ky] /= kernelSum;
            }
        }
    }

    public float at(int x, int y) {
        return this.kernel[x + this.radius][y + this.radius];
    }

    void print() {
        for (int ky = 0; ky < this.width; ky++) {
            for (int kx = 0; kx < this.width; kx++) {
                System.out.printf("%.4f\t", this.kernel[kx][ky]);
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        new Gaussian2DKernel(1.5f, 3).print();
    }
}
