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

    public static int[] calculateSize(BufferedImage inputImage, int width, int height, AspectRatioPreservationMode preserveAspectRatio, int charWidth, int charHeight, boolean doubleWide) {
        if (preserveAspectRatio == AspectRatioPreservationMode.IGNORE) {
            return new int[] { width, height };
        } else {
            float factor = calculateScaleFactor(inputImage, width * (doubleWide ? 2 : 1), height);
            int drawWidth = Math.round(inputImage.getWidth() * factor);
            int drawHeight = Math.round(inputImage.getHeight() * factor);
            if (preserveAspectRatio == AspectRatioPreservationMode.SNAP_CHAR) {
                if (drawWidth != width) {
                    drawWidth = Math.round(drawWidth / (float) charWidth) * charWidth;
                } else if (drawHeight != height) {
                    drawHeight = Math.round(drawHeight / (float) charHeight) * charHeight;
                }
            } else if (preserveAspectRatio == AspectRatioPreservationMode.SNAP_CENTER) {
                // one of the axes is guaranteed to be snapped
                if (drawWidth != width) {
                    int oddOffset = (width % (charWidth * 2)) >= charWidth ? charWidth : 0;
                    drawWidth = Math.round((drawWidth - oddOffset) / (float) (charWidth * 2)) * (charWidth * 2) + oddOffset;
                } else if (drawHeight != height) {
                    int oddOffset = (height % (charHeight * 2)) >= charHeight ? charHeight : 0;
                    drawHeight = Math.round((drawHeight - oddOffset) / (float) (charHeight * 2)) * (charHeight * 2) + oddOffset;
                }
            }
            return new int[] { drawWidth, drawHeight };
        }
    }

    public static void drawScaled(BufferedImage inputImage, int width, int height, Graphics2D scaledGraphics, AspectRatioPreservationMode preserveAspectRatio) {
        drawScaled(inputImage, width, height, scaledGraphics, preserveAspectRatio, false);
    }

    public static void drawScaled(BufferedImage inputImage, int width, int height, Graphics2D scaledGraphics, AspectRatioPreservationMode preserveAspectRatio, boolean doubleWide) {
        // TODO: pass char width/height as argument
        int charWidth = doubleWide ? 16 : 8;
        int charHeight = 14;
        int[] drawSize = calculateSize(inputImage, width, height, preserveAspectRatio, charWidth, charHeight, doubleWide);
        int xOffset = (width - (drawSize[0] / (doubleWide ? 2 : 1))) / 2;
        int yOffset = (height - drawSize[1]) / 2;
        if (preserveAspectRatio == AspectRatioPreservationMode.SNAP_CENTER || preserveAspectRatio == AspectRatioPreservationMode.SNAP_CHAR) {
            xOffset = Math.round(xOffset / (float) charWidth) * charWidth;
            yOffset = Math.round(yOffset / (float) charHeight) * charHeight;
        }
        scaledGraphics.drawImage(inputImage, xOffset, yOffset, (drawSize[0] / (doubleWide ? 2 : 1)) + xOffset, drawSize[1] + yOffset, 0, 0, inputImage.getWidth(), inputImage.getHeight(), null);
    }

    public static BufferedImage scale(BufferedImage inputImage, int width, int height, AspectRatioPreservationMode preserveAspectRatio, Color fillColor) {
        return scale(inputImage, width, height, preserveAspectRatio, false, fillColor);
    }

    public static BufferedImage scale(BufferedImage inputImage, int width, int height, AspectRatioPreservationMode preserveAspectRatio, boolean doubleWide, Color fillColor) {
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

	public static BufferedImage cloneRgb(BufferedImage inputImage) {
        BufferedImage image = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.drawImage(inputImage, 0, 0, null);
        g.dispose();
        return image;
	}
}
