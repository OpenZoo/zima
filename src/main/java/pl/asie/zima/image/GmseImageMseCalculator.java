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
package pl.asie.zima.image;

import pl.asie.libzzt.TextVisualData;
import pl.asie.zima.util.ColorUtils;
import pl.asie.zima.util.Gaussian2DKernel;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GmseImageMseCalculator implements ImageMseCalculator {
	public static class ColorMixCache {
		private final TextVisualData visual;
		private final int accuracy;
		private final List<int[]> cache;

		public ColorMixCache(TextVisualData visual, int accuracy) {
			this.visual = visual;
			this.accuracy = accuracy;
			this.cache = IntStream.range(0, 256).parallel().mapToObj(col -> {
				int bgColor = visual.getPalette()[col >> 4];
				int fgColor = visual.getPalette()[col & 0x0F];
				float accAsFloat = (float) accuracy;

				return IntStream.rangeClosed(0, accuracy).map(mixFactor -> {
					return ColorUtils.mix(bgColor, fgColor, mixFactor / accAsFloat);
				}).toArray();
			}).collect(Collectors.toList());
		}

		public int mix(int col, float mix) {
			return cache.get(col)[Math.round(mix * accuracy)];
		}
	}

	public static class GaussianCharCache {
		private final TextVisualData visual;
		private final List<float[]> cache;
		private final Gaussian2DKernel kernel;

		private static int getCharPixelAt(TextVisualData visual, int chr, int cx, int cy) {
			if (cx < 0) cx = 0;
			else if (cx >= visual.getCharWidth()) cx = visual.getCharWidth() - 1;
			if (cy < 0) cy = 0;
			else if (cy >= visual.getCharHeight()) cy = visual.getCharHeight() - 1;
			return (visual.getCharData()[(chr * visual.getCharHeight()) + cy] >> (7 - cx)) & 1;


			/* if (cx >= 0 && cy >= 0 && cx < visual.getCharWidth() && cy < visual.getCharHeight()) {
				return (visual.getCharData()[(chr * visual.getCharHeight()) + cy] >> (7 - cx)) & 1;
			} else {
				return 0;
			} */
		}

		public GaussianCharCache(TextVisualData visual, int radius, float sigma) {
			this.visual = visual;
			this.kernel = new Gaussian2DKernel(sigma, radius);
			this.cache = IntStream.range(0, 256).parallel().mapToObj(chr -> {
				float[] data = new float[visual.getCharWidth() * visual.getCharHeight()];
				int ci = 0;

				for (int cy = 0; cy < visual.getCharHeight(); cy++) {
					for (int cx = 0; cx < visual.getCharWidth(); cx++) {
						float cv = 0.0f;
						for (int ky = -radius; ky <= radius; ky++) {
							for (int kx = -radius; kx <= radius; kx++) {
								int v = getCharPixelAt(visual, chr, cx + kx, cy + ky);
								float k = kernel.at(kx, ky);
								cv += k * v;
							}
						}
						data[ci++] = cv;
					}
				}

				return data;
			}).collect(Collectors.toCollection(ArrayList::new));

			for (Map.Entry<Integer, Float> entries : Map.of(
					176, 0.25f,
					177, 0.5f,
					178, 0.75f
			).entrySet()) {
				float[] data = this.cache.get(entries.getKey());
				Arrays.fill(data, entries.getValue());
			}
		}

		public float[] getGaussian(int chr) {
			return this.cache.get(chr & 0xFF);
		}
	}

	private final TextVisualData visual;
	private final GaussianCharCache gaussCache;
	private final ColorMixCache mixCache;
	private final float contrastReduction;
	private final boolean blinkingDisabled;

	public GmseImageMseCalculator(TextVisualData visual, boolean blinkingDisabled, float contrastReduction, float accurateApproximate) {
		this.visual = visual;
		this.contrastReduction = contrastReduction;
		this.blinkingDisabled = blinkingDisabled;

		float size = 0.05f + (accurateApproximate * 1.45f);
		this.gaussCache = new GaussianCharCache(visual, 3, size);
		this.mixCache = new ColorMixCache(visual, 256);
	}
	
	@Override
	public Applier applyMse(BufferedImage image, int px, int py) {
		final int[] imgColorLut = new int[visual.getCharWidth() * visual.getCharHeight()];
		{
			int ci = 0;
			for (int cy = 0; cy < visual.getCharHeight(); cy++) {
				for (int cx = 0; cx < visual.getCharWidth(); cx++, ci++) {
					int ix = px + cx;
					int iy = py + cy;
					imgColorLut[ci] = image.getRGB(ix, iy);
				}
			}
		}

		float maxDistanceTmp = 0.0f;
		for (int i = 0; i < imgColorLut.length; i++) {
			for (int j = i + 1; j < imgColorLut.length; j++) {
				maxDistanceTmp = Math.max(maxDistanceTmp, ColorUtils.distance(imgColorLut[i], imgColorLut[j]));
			}
		}
		float maxDistance = maxDistanceTmp;

		int colorMask = blinkingDisabled ? 0xFF : 0x7F;

		return (proposed, maxMse) -> {
			float mse = 0.0f;

			int color = proposed.getColor() & colorMask;
			int bgColor = visual.getPalette()[(proposed.getColor() >> 4) & (blinkingDisabled ? 0x0F : 0x07)];
			int fgColor = visual.getPalette()[proposed.getColor() & 0x0F];

			float contrastDiff = maxDistance - ColorUtils.distance(bgColor, fgColor);
			float mseContrastReduction = contrastReduction * contrastDiff * contrastDiff;
			mse += mseContrastReduction * imgColorLut.length;
			if (mse > maxMse) {
				return mse;
			}

			float[] charBlurred = gaussCache.getGaussian(proposed.getCharacter());
			for (int ci = 0; ci < imgColorLut.length; ci++) {
				int imgColor = imgColorLut[ci];
				//int localCharColor = ColorUtils.mix(bgColor, fgColor, charBlurred[ci]);
				int localCharColor = this.mixCache.mix(color, charBlurred[ci]);
				mse += ColorUtils.distance(imgColor, localCharColor);
				if (mse > maxMse) {
					break;
				}
			}

			return mse;
		};
	}
}
