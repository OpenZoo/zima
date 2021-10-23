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

import lombok.Getter;
import pl.asie.libzzt.Platform;
import pl.asie.libzzt.TextVisualData;
import pl.asie.libzzt.TextVisualRenderer;
import pl.asie.zima.util.*;
import pl.asie.zima.image.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Set;

public class ZimaConversionProfile {
    private static final PropertyAffect SCALED_IMAGE = new PropertyAffect();
    private static final PropertyAffect FILTERED_IMAGE = new PropertyAffect();
    private static final PropertyAffect MSE_CALCULATOR = new PropertyAffect();
    private static final PropertyAffect TEXT_VISUAL_RENDERER = new PropertyAffect();
    private static final PropertyAffect IMAGE_CONVERTER = new PropertyAffect();

    public static final Property<TextVisualData> VISUAL_DATA = Property.createTransient(SCALED_IMAGE, MSE_CALCULATOR, TEXT_VISUAL_RENDERER, IMAGE_CONVERTER);
    public static final Property<Platform> PLATFORM = Property.createTransient(SCALED_IMAGE, TEXT_VISUAL_RENDERER, IMAGE_CONVERTER);

    public static final Property<Integer> CHARS_WIDTH = Property.create("output.widthChars", 60, SCALED_IMAGE);
    public static final Property<Integer> CHARS_HEIGHT = Property.create("output.heightChars", 25, SCALED_IMAGE);
    public static final Property<Integer> BOARD_X = Property.create("output.boardX", 1);
    public static final Property<Integer> BOARD_Y = Property.create("output.boardY", 1);
    public static final Property<Integer> PLAYER_X = Property.create("output.playerX", 60);
    public static final Property<Integer> PLAYER_Y = Property.create("output.playerY", 25);
    public static final Property<Integer> STAT_CYCLE = Property.create("output.statCycle", 0);
    public static final Property<Integer> MAX_STAT_COUNT = Property.create("converter.maxStatCount", 150);
    public static final Property<Integer> MAX_BOARD_SIZE = Property.create("converter.maxBoardSize", 20002);
    public static final Property<Float> TRIX_CONTRAST_REDUCTION = Property.create("converter.trix.contrastReduction", 0.0035f, MSE_CALCULATOR);
    public static final Property<Float> TRIX_ACCURATE_APPROXIMATE = Property.create("converter.trix.accurateApproximate", 0.45f, MSE_CALCULATOR);

    public static final Property<Float> BRIGHTNESS = Property.create("image.colorFilter.brightness", 0.0f, FILTERED_IMAGE);
    public static final Property<Float> CONTRAST = Property.create("image.colorFilter.contrast", 0.0f, FILTERED_IMAGE);
    public static final Property<Float> SATURATION = Property.create("image.colorFilter.saturation", 0.0f, FILTERED_IMAGE);
    public static final Property<AspectRatioPreservationMode> ASPECT_RATIO_PRESERVATION_MODE = Property.create("image.preserveAspectRatio", AspectRatioPreservationMode.SNAP_CHAR, SCALED_IMAGE);

    public static final Property<Integer> CROP_LEFT = Property.create("image.crop.left", 0, SCALED_IMAGE);
    public static final Property<Integer> CROP_RIGHT = Property.create("image.crop.right", 0, SCALED_IMAGE);
    public static final Property<Integer> CROP_TOP = Property.create("image.crop.top", 0, SCALED_IMAGE);
    public static final Property<Integer> CROP_BOTTOM = Property.create("image-crop.bottom", 0, SCALED_IMAGE);

    public static final Property<ImageConverterRuleset> RULESET = Property.createTransient();
    public static final Property<ImageConverterRuleset> FAST_RULESET = Property.createTransient();
    public static final Property<Set<Integer>> ALLOWED_CHARACTERS = Property.create("converter.allowedCharacters");
    public static final Property<Set<Integer>> ALLOWED_COLORS = Property.create("converter.allowedColorPairs");
    public static final Property<Boolean> BLINKING_DISABLED = Property.create("converter.blinkingDisabled", false, MSE_CALCULATOR, IMAGE_CONVERTER);

    public static final Property<Float> COARSE_DITHER_STRENGTH = Property.create("converter.coarseDither.strength", 0.0f);
    public static final Property<DitherMatrix> COARSE_DITHER_MATRIX = Property.create("converter.coarseDither.matrix", DitherMatrix.FLOYD_STEINBERG);

    @Getter
    private final PropertyHolder properties = new PropertyHolder();

    private transient BufferedImage inputImage;
    private transient BufferedImage scaledImage;
    @Getter
    private transient BufferedImage filteredImage;
    private transient ImageMseCalculator mseCalculator;
    private transient TextVisualRenderer renderer;
    private transient ImageConverter converter;

    public ZimaConversionProfile() {
        invalidate();
    }

    public void invalidate() {
        properties.affectAll(SCALED_IMAGE, FILTERED_IMAGE, MSE_CALCULATOR, TEXT_VISUAL_RENDERER, IMAGE_CONVERTER);
    }
    
    private BufferedImage filterImage(BufferedImage input) {
        float brightness = properties.get(BRIGHTNESS);
        float contrast = properties.get(CONTRAST);
        float saturation = properties.get(SATURATION);
        
        BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);
        float contrastMul = (1.05f * (1.0f + contrast)) / (1.0f * (1.05f - contrast));
        float saturationMul = (saturation + 1.0f);
        boolean useYuv = Math.abs(saturation) > 1e-5f;

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
        PropertyHolder localHolder = this.properties.clone(SCALED_IMAGE, FILTERED_IMAGE);

        if (input == null) {
            this.scaledImage = null;
            this.filteredImage = null;
            return;
        }

        if (this.inputImage == null || this.scaledImage == null || this.inputImage != input) {
            this.inputImage = input;
            localHolder.affect(SCALED_IMAGE);
            localHolder.affect(FILTERED_IMAGE);
        }

        if (localHolder.isAffected(SCALED_IMAGE)) {
            BufferedImage img = this.inputImage;
            int cropLeft = properties.get(CROP_LEFT);
            int cropRight = properties.get(CROP_RIGHT);
            int cropTop = properties.get(CROP_TOP);
            int cropBottom = properties.get(CROP_BOTTOM);

            if (cropTop != 0 || cropLeft != 0 || cropRight != 0 || cropBottom != 0) {
                int croppedWidth = Math.max(1, img.getWidth() - cropLeft - cropRight);
                int croppedHeight = Math.max(1, img.getHeight() - cropTop - cropBottom);

                BufferedImage croppedImage = new BufferedImage(croppedWidth, croppedHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D gfx = (Graphics2D) croppedImage.getGraphics();
                gfx.drawImage(img, -cropLeft, -cropTop, null);
                gfx.dispose();
                img = croppedImage;
            }

            int width = properties.get(VISUAL_DATA).getCharWidth() * properties.get(CHARS_WIDTH);
            int height = properties.get(VISUAL_DATA).getCharHeight() * properties.get(CHARS_HEIGHT);

            this.scaledImage = ImageUtils.scale(img, width, height, properties.get(ASPECT_RATIO_PRESERVATION_MODE), properties.get(PLATFORM).isDoubleWide(), Color.BLACK);
            localHolder.affect(FILTERED_IMAGE);
        }

        if (localHolder.isAffected(FILTERED_IMAGE) || this.filteredImage == null) {
            this.filteredImage = filterImage(this.scaledImage);
        }
    }

    public Pair<ImageConverter.Result, BufferedImage> convert(BufferedImage input, ProgressCallback progressCallback, boolean fast) {
        updateImage(input);

        PropertyHolder localHolder = this.properties.clone(TEXT_VISUAL_RENDERER, MSE_CALCULATOR, IMAGE_CONVERTER);

        if (localHolder.isAffected(TEXT_VISUAL_RENDERER) || this.renderer == null) {
            this.renderer = new TextVisualRenderer(properties.get(VISUAL_DATA), properties.get(PLATFORM));
        }
        if (localHolder.isAffected(MSE_CALCULATOR) || this.mseCalculator == null) {
           this.mseCalculator = new TrixImageMseCalculator(properties.get(VISUAL_DATA), properties.get(BLINKING_DISABLED), properties.get(TRIX_CONTRAST_REDUCTION), properties.get(TRIX_ACCURATE_APPROXIMATE));
           //this.mseCalculator = new GmseImageMseCalculator(properties.get(VISUAL_DATA), properties.get(BLINKING_DISABLED), properties.get(TRIX_CONTRAST_REDUCTION));
           localHolder.affect(IMAGE_CONVERTER);
        }
        if (localHolder.isAffected(IMAGE_CONVERTER) || this.converter == null) {
            this.converter = new ImageConverter(properties.get(VISUAL_DATA), properties.get(PLATFORM), mseCalculator);
        }
        return converter.convert(this.filteredImage,
                properties.get((fast && properties.has(FAST_RULESET)) ? FAST_RULESET : RULESET),
                properties.get(BOARD_X), properties.get(BOARD_Y),
                properties.get(CHARS_WIDTH), properties.get(CHARS_HEIGHT),
                properties.get(PLAYER_X), properties.get(PLAYER_Y),
                properties.get(MAX_STAT_COUNT),
                properties.get(BLINKING_DISABLED),
                properties.get(MAX_BOARD_SIZE),
                properties.get(COARSE_DITHER_STRENGTH),
                properties.get(COARSE_DITHER_MATRIX),
                properties.has(ALLOWED_CHARACTERS) ? properties.get(ALLOWED_CHARACTERS)::contains : null,
                properties.has(ALLOWED_COLORS) ? properties.get(ALLOWED_COLORS)::contains : null,
                properties.get(STAT_CYCLE),
                this.renderer, progressCallback, fast);
    }
}
