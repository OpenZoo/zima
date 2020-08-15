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
package pl.asie.zzttools.zima.gui;

import lombok.Getter;
import lombok.Setter;
import pl.asie.zzttools.util.ColorUtils;
import pl.asie.zzttools.util.ImageUtils;
import pl.asie.zzttools.util.Pair;
import pl.asie.zzttools.zima.ImageConverter;
import pl.asie.zzttools.zima.ImageConverterRules;
import pl.asie.zzttools.zima.ImageConverterRuleset;
import pl.asie.zzttools.zima.ImageMseCalculator;
import pl.asie.zzttools.zima.ProgressCallback;
import pl.asie.zzttools.zzt.Board;
import pl.asie.zzttools.zzt.TextVisualData;
import pl.asie.zzttools.zzt.TextVisualRenderer;

import java.awt.*;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Supplier;

@Getter
public class ZimaConversionProfile implements Cloneable {
	// provided
	private TextVisualData visual;
	private ImageConverterRuleset ruleset;
	private Function<ZimaConversionProfile, ImageMseCalculator> mseCalculatorFunction;
	private int charsWidth = 60, charsHeight = 25;
	@Setter
	private int boardX = 1, boardY = 1, playerX = 60, playerY = 25;
	@Setter
	private int maxStatCount = 149;
	@Setter
	private boolean colorsBlink = true;
	private float contrastReduction = 0.0035f;

	private float brightness = 0.0f;
	private float contrast = 0.0f;
	private float saturation = 0.0f;

	private int cropLeft = 0;
	private int cropRight = 0;
	private int cropTop = 0;
	private int cropBottom = 0;

	// generated
	private BufferedImage scaledImage;
	private BufferedImage filteredImage;
	private ImageMseCalculator mseCalculator;
	private TextVisualRenderer renderer;
	private ImageConverter converter;

	// misc
	private BufferedImage inputImage;

	// setters - provided

	public void setVisual(TextVisualData visual) {
		this.visual = visual;
		this.mseCalculator = null;
		this.renderer = null;
		this.converter = null;
	}

	public void setRuleset(ImageConverterRuleset ruleset) {
		this.ruleset = ruleset;
	}

	public void setMseCalculatorFunction(Function<ZimaConversionProfile, ImageMseCalculator> mseCalculatorFunction) {
		this.mseCalculatorFunction = mseCalculatorFunction;
		this.mseCalculator = null;
	}

	public void setCharsWidth(int charsWidth) {
		this.charsWidth = charsWidth;
		this.scaledImage = null;
	}

	public void setCharsHeight(int charsHeight) {
		this.charsHeight = charsHeight;
		this.scaledImage = null;
	}

	public void setContrastReduction(float contrastReduction) {
		this.contrastReduction = contrastReduction;
		this.mseCalculator = null;
	}

	public void setBrightness(float brightness) {
		this.brightness = brightness;
		this.filteredImage = null;
	}

	public void setContrast(float contrast) {
		this.contrast = contrast;
		this.filteredImage = null;
	}

	public void setSaturation(float saturation) {
		this.saturation = saturation;
		this.filteredImage = null;
	}

	public void setCropLeft(int cropLeft) {
		this.cropLeft = cropLeft;
		this.scaledImage = null;
	}

	public void setCropRight(int cropRight) {
		this.cropRight = cropRight;
		this.scaledImage = null;
	}

	public void setCropTop(int cropTop) {
		this.cropTop = cropTop;
		this.scaledImage = null;
	}

	public void setCropBottom(int cropBottom) {
		this.cropBottom = cropBottom;
		this.scaledImage = null;
	}

	// setters - generated

	private void setRenderer(TextVisualRenderer renderer) {
		this.renderer = renderer;
	}

	private void setConverter(ImageConverter converter) {
		this.converter = converter;
	}

	private void setMseCalculator(ImageMseCalculator calculator) {
		this.mseCalculator = calculator;
		this.converter = null;
	}

	private BufferedImage filterImage(BufferedImage input) {
		BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);
		float contrastMul = (1.05f * (1.0f + contrast)) / (1.0f * (1.05f - contrast));
		float saturationMul = (saturation + 1.0f);
		boolean useYuv = Math.abs(this.saturation) > 1e-5f;

		for (int y = 0; y < input.getHeight(); y++) {
			for (int x = 0; x < input.getWidth(); x++) {
				int rgbIn = input.getRGB(x, y);
				float rIn = ColorUtils.sRtoR((rgbIn >> 16) & 0xFF);
				float gIn = ColorUtils.sRtoR((rgbIn >> 8) & 0xFF);
				float bIn = ColorUtils.sRtoR(rgbIn & 0xFF);

				rIn = rIn + brightness;
				gIn = gIn + brightness;
				bIn = bIn + brightness;

				rIn = (rIn - 0.5f) * contrastMul + 0.5f;
				gIn = (gIn - 0.5f) * contrastMul + 0.5f;
				bIn = (bIn - 0.5f) * contrastMul + 0.5f;

				if (useYuv) {
					float yIn = 0.299f * rIn + 0.587f * gIn + 0.114f * bIn;
					float uIn = -0.14713f * rIn + -0.28886f * gIn + 0.436f * bIn;
					float vIn = 0.615f * rIn + -0.51499f * gIn + -0.10001f * bIn;

					uIn *= saturationMul;
					vIn *= saturationMul;

					rIn = yIn + 1.13983f * vIn;
					gIn = yIn + -0.39465f * uIn + -0.58060f * vIn;
					bIn = yIn + 2.03211f * uIn;
				}

				int rgbOut = (ColorUtils.RtosR(rIn) << 16) | (ColorUtils.RtosR(gIn) << 8) | (ColorUtils.RtosR(bIn));
				output.setRGB(x, y, rgbOut);
			}
		}
		return output;
	}

	public void updateImage(BufferedImage input) {
		if (input == null) {
			return;
		}

		if (this.inputImage == null || this.inputImage != input) {
			this.inputImage = input;
			this.scaledImage = null;
		}

		if (this.scaledImage == null) {
			BufferedImage img = this.inputImage;
			if (this.cropTop != 0 || this.cropLeft != 0 || this.cropRight != 0 || this.cropBottom != 0) {
				int croppedWidth = Math.max(1, img.getWidth() - this.cropLeft - this.cropRight);
				int croppedHeight = Math.max(1, img.getHeight() - this.cropTop - this.cropBottom);

				BufferedImage croppedImage = new BufferedImage(croppedWidth, croppedHeight, BufferedImage.TYPE_INT_RGB);
				Graphics2D gfx = (Graphics2D) croppedImage.getGraphics();
				gfx.drawImage(img, -this.cropLeft, -this.cropTop, null);
				gfx.dispose();
				img = croppedImage;
			}

			int width = visual.getCharWidth() * charsWidth;
			int height = visual.getCharHeight() * charsHeight;

			this.scaledImage = ImageUtils.scale(img, width, height, AffineTransformOp.TYPE_BICUBIC);
			this.filteredImage = null;
		}

		if (this.filteredImage == null) {
			this.filteredImage = filterImage(this.scaledImage);
		}
	}

	public Pair<Board, BufferedImage> convert(BufferedImage input, ProgressCallback progressCallback, boolean fast,
											  IntPredicate charCheck, IntPredicate colorCheck) {
		updateImage(input);

		if (this.renderer == null) {
			setRenderer(new TextVisualRenderer(visual));
		}
		if (this.mseCalculator == null) {
			setMseCalculator(this.mseCalculatorFunction.apply(this));
		}
		if (this.converter == null) {
			setConverter(new ImageConverter(visual, mseCalculator));
		}
		return converter.convert(this.filteredImage, fast ? ImageConverterRules.RULES_BLOCKS : this.ruleset,
				boardX, boardY, charsWidth, charsHeight, playerX, playerY, maxStatCount, colorsBlink,
				charCheck, colorCheck,
				this.renderer, progressCallback);
	}
}
