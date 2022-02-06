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
package pl.asie.zima.image.gui;

import lombok.Getter;
import pl.asie.libzzt.TextVisualData;
import pl.asie.zima.util.ColorUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PaletteSelector extends JComponent implements MouseListener {
    private static final int BOX_SIZE = 14;
    @Getter private TextVisualData visual;
    private boolean blinkingDisabled;
    private boolean selectBlinking;
    private final boolean[] allowedColors = new boolean[256];
    private final Runnable changeListener;

    public PaletteSelector(Runnable changeListener) {
        addMouseListener(this);
        this.changeListener = changeListener;
        this.blinkingDisabled = false;
        this.selectBlinking = false;
        for (int i = 0; i < 256; i++) {
            allowedColors[i] = i < 128;
        }
    }

    private void drawB(Graphics graphics, int xPos, int yPos) {
        graphics.fillRect(xPos, yPos, 2, 5);
        graphics.fillRect(xPos, yPos, 5, 1);
        graphics.fillRect(xPos, yPos + 2, 5, 1);
        graphics.fillRect(xPos, yPos + 5, 5, 1);
        graphics.fillRect(xPos + 5, yPos + 1, 1, 1);
        graphics.fillRect(xPos + 5, yPos + 3, 1, 2);
    }

    @Override
    public void paintComponent(Graphics graphics) {
        Color[] awtPalette = new Color[16];
        for (int i = 0; i < 16; i++) {
            awtPalette[i] = ColorUtils.toAwtColor(visual.getPalette()[i]);
        }
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, 17 * BOX_SIZE, 18 * BOX_SIZE);
        // draw palette
        for (int iy = 0; iy < 16; iy++) {
            Color bgColor = awtPalette[!blinkingDisabled ? (iy & 7) : iy];
            for (int ix = 0; ix < 16; ix++) {
                boolean enabled = allowedColors[(iy * 16) + ix];
                Color fgColor = awtPalette[ix];
                int xPos = (ix + 1) * BOX_SIZE;
                int yPos = (iy + 1) * BOX_SIZE;

                if (enabled) {
                    graphics.setColor(Color.GREEN);
                    graphics.fillRect(xPos + 1, yPos + 1, 12, 12);
                }
                graphics.setColor(bgColor);
                graphics.fillRect(xPos + 2, yPos + 2, 10, 10);
                graphics.setColor(fgColor);
                if (!blinkingDisabled && iy >= 8) {
                    drawB(graphics, xPos + 4, yPos + 4);
                } else {
                    graphics.fillRect(xPos + 4, yPos + 4, 6, 6);
                }
            }
        }
        // draw toggle buttons
        for (int i = 0; i < 32; i++) {
            int xPos = (i >= 16) ? (BOX_SIZE * (i - 15)) : 0;
            int yPos = (i >= 16) ? 0 : (BOX_SIZE * (i + 1));

            graphics.setColor(Color.GRAY);
            graphics.fillRect(xPos + 2, yPos + 2, BOX_SIZE - 4, BOX_SIZE - 4);
            graphics.setColor(Color.LIGHT_GRAY);
            graphics.fillRect(xPos + 1, yPos + 1, 1, BOX_SIZE - 2);
            graphics.fillRect(xPos + 1, yPos + 1, BOX_SIZE - 2, 1);
        }
        // draw global buttons
        for (int i = 0; i < 17; i++) {
            int xPos = BOX_SIZE * i;
            int yPos = BOX_SIZE * 17;

            graphics.setColor((i == 0 && selectBlinking) ? Color.WHITE : Color.GRAY);
            graphics.fillRect(xPos + 2, yPos + 2, BOX_SIZE - 4, BOX_SIZE - 4);
            graphics.setColor(Color.LIGHT_GRAY);
            graphics.fillRect(xPos + 1, yPos + 1, 1, BOX_SIZE - 2);
            graphics.fillRect(xPos + 1, yPos + 1, BOX_SIZE - 2, 1);
            if (i > 0) {
                graphics.setColor(awtPalette[i - 1]);
                graphics.fillRect(xPos + 4, yPos + 4, BOX_SIZE - 8, BOX_SIZE - 8);
            } else {
                // draw [B]
                graphics.setColor(Color.BLACK);
                drawB(graphics, xPos + 4, yPos + 4);
            }
        }
    }

    public void setVisual(TextVisualData visual) {
        this.visual = visual;
        Dimension dims = new Dimension(17 * BOX_SIZE, 18 * BOX_SIZE);
        setMinimumSize(dims);
        setMaximumSize(dims);
        setPreferredSize(dims);
        repaint();
    }

    public boolean isTwoColorAllowed(int c) {
        return allowedColors[c];
    }

    public boolean isTwoColorAllowed(int bg, int fg) {
        return allowedColors[((bg & 0x0F) << 4) | (fg & 0x0F)];
    }

    @Deprecated
    public boolean isColorAllowed(int c) {
        for (int i = 0; i < 16; i++) {
            if (allowedColors[(c << 4) | i] || allowedColors[c | (i << 4)]) {
                return true;
            }
        }
        return false;
    }

    public Set<Integer> toSet() {
        return IntStream.range(0, 256).filter(this::isTwoColorAllowed).boxed().collect(Collectors.toSet());
    }

    public void change() {
        changeListener.run();
    }

    public void setBlinkingDisabled(boolean blinkingDisabled) {
        this.blinkingDisabled = blinkingDisabled;
        IntStream.range(0, 256).forEach(i -> setColorAllowed(i, allowedColors[i]));
        repaint();
        changeListener.run();
    }

    public void setColorAllowed(int c, boolean v) {
        if (blinkingDisabled || (selectBlinking != (c < 128))) {
            allowedColors[c] = v;
        } else {
            allowedColors[c] = false;
        }
        repaint();
        changeListener.run();
    }

    public void toggleColorAllowed(int... ranges) {
        List<Integer> valuesInRanges = new ArrayList<>();
        for (int i = 0; i < ranges.length; i += 2) {
            for (int j = ranges[i]; j <= ranges[i+1]; j++) {
                valuesInRanges.add(j);
            }
        }
        toggleColorAllowed(valuesInRanges);
    }

    public void toggleColorAllowed(IntStream ranges) {
        toggleColorAllowed(ranges.boxed().collect(Collectors.toList()));
    }

    private void toggleColorAllowed(List<Integer> valuesInRanges) {
        boolean isAnySet = valuesInRanges.stream().anyMatch(i -> allowedColors[i]);
        boolean settingMode = !isAnySet;
        valuesInRanges.forEach(i -> setColorAllowed(i, settingMode));
    }

    private IntStream streamColorBg(int color, boolean accountForBlinking) {
        if (accountForBlinking && !blinkingDisabled) {
            if (color >= 8) {
                return IntStream.empty();
            } else {
                return IntStream.concat(IntStream.range((color) << 4, (color + 1) << 4), IntStream.range((color + 8) << 4, (color + 9) << 4));
            }
        } else {
            return IntStream.range((color) << 4, (color + 1) << 4);
        }
    }

    private IntStream streamColorFg(int color) {
        return IntStream.range(0, 16).map(i -> (i << 4) | color);
    }

    private IntStream streamColorContained(int color) {
        return IntStream.concat(streamColorBg(color, true), streamColorFg(color));
    }

    public void setColorContainingAllowed(int color, boolean v) {
        streamColorContained(color).forEach(i -> setColorAllowed(i, v));
    }

    // input

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
            final int my = mouseEvent.getY() / BOX_SIZE;
            final int mx = mouseEvent.getX() / BOX_SIZE;
            if (my == 0 && mx >= 1 && mx <= 16) {
                // toggle all FOREGROUND colors
                toggleColorAllowed(streamColorFg(mx - 1));
            } else if (mx == 0 && my >= 1 && my <= 16) {
                // toggle all BACKGROUND colors
                toggleColorAllowed(streamColorBg(my - 1, false));
            } else if (my == 17 && mx >= 1 && mx <= 16) {
                // toggle all colors containing color
                toggleColorAllowed(streamColorContained(mx - 1));
            } else if (mx >= 1 && mx <= 16 && my >= 1 && my <= 16) {
                // toggle specific color
                int c = (mx - 1) | ((my - 1) << 4);
                setColorAllowed(c, !allowedColors[c]);
            } else if (mx == 0 && my == 17) {
                // toggle [B] selection allowed
                if (selectBlinking) {
                    selectBlinking = false;
                    IntStream.range(0, 128).forEach(c -> setColorAllowed(c, allowedColors[c + 128]));
                    IntStream.range(128, 256).forEach(c -> setColorAllowed(c, false));
                } else {
                    selectBlinking = true;
                    IntStream.range(128, 256).forEach(c -> setColorAllowed(c, allowedColors[c - 128]));
                    IntStream.range(0, 128).forEach(c -> setColorAllowed(c, false));
                }
                repaint();
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }
}
