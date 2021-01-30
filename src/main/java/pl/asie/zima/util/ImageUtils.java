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
package pl.asie.zima.util;

import java.awt.*;
import java.awt.image.BufferedImage;

public final class ImageUtils {
    private ImageUtils() {

    }

    public static float calculateScaleFactor(BufferedImage image, int width, int height) {
        float aspectRatioSrc = (float) image.getWidth() / image.getHeight();
        float aspectRatioDst = (float) width / height;
        return aspectRatioSrc > aspectRatioDst ? ((float) width / image.getWidth()) : ((float) height / image.getHeight());
    }

    public static void drawScaled(BufferedImage inputImage, int width, int height, Graphics2D scaledGraphics, boolean preserveAspectRatio) {
        drawScaled(inputImage, width, height, scaledGraphics, preserveAspectRatio, false);
    }

    public static void drawScaled(BufferedImage inputImage, int width, int height, Graphics2D scaledGraphics, boolean preserveAspectRatio, boolean doubleWide) {
        if (preserveAspectRatio) {
            float factor = calculateScaleFactor(inputImage, width * (doubleWide ? 2 : 1), height);
            int drawWidth = Math.round(inputImage.getWidth() * factor);
            int drawHeight = Math.round(inputImage.getHeight() * factor);
            int xOffset = (width - (drawWidth / (doubleWide ? 2 : 1))) / 2;
            int yOffset = (height - drawHeight) / 2;
            scaledGraphics.drawImage(inputImage, xOffset, yOffset, (drawWidth / (doubleWide ? 2 : 1)) + xOffset, drawHeight + yOffset, 0, 0, inputImage.getWidth(), inputImage.getHeight(), null);
        } else {
            scaledGraphics.drawImage(inputImage, 0, 0, width, height, 0, 0, inputImage.getWidth(), inputImage.getHeight(), null);
        }
    }

    public static BufferedImage scale(BufferedImage inputImage, int width, int height, boolean preserveAspectRatio, Color fillColor) {
        return scale(inputImage, width, height, preserveAspectRatio, false, fillColor);
    }

    public static BufferedImage scale(BufferedImage inputImage, int width, int height, boolean preserveAspectRatio, boolean doubleWide, Color fillColor) {
        if (inputImage.getWidth() == width && inputImage.getHeight() == height) {
            return inputImage;
        }
        BufferedImage scaledImage = new BufferedImage(width, height, fillColor != null ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) scaledImage.getGraphics();
        if (fillColor != null) {
            graphics.setColor(fillColor);
            graphics.fillRect(0, 0, width, height);
        }
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        drawScaled(inputImage, width, height, graphics, preserveAspectRatio, doubleWide);
        graphics.dispose();
        return scaledImage;
    }

}
