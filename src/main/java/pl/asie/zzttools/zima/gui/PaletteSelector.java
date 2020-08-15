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

import pl.asie.zzttools.zzt.TextVisualData;
import pl.asie.zzttools.zzt.TextVisualRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PaletteSelector extends JComponent implements MouseListener {
    private static final int PALETTE_WIDTH = 16;
    private static final int PALETTE_HEIGHT = 64;

    private TextVisualData visual;
    private final boolean[] allowedColors = new boolean[16];
    private final Runnable changeListener;

    public PaletteSelector(Runnable changeListener) {
        Arrays.fill(allowedColors, true);
        addMouseListener(this);
        this.changeListener = changeListener;
    }

    @Override
    public void paintComponent(Graphics graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, PALETTE_WIDTH * 16, PALETTE_HEIGHT);
        for (int i = 0; i < 16; i++) {
            int rgb = visual.getPalette()[i];
            graphics.setColor(new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
            graphics.fillRect(i * PALETTE_WIDTH, 0, PALETTE_WIDTH, PALETTE_HEIGHT - 16);
            graphics.setColor(Color.GRAY);
            graphics.fillRect(i * PALETTE_WIDTH, PALETTE_HEIGHT - 2, PALETTE_WIDTH, 2);
            graphics.fillRect(i * PALETTE_WIDTH + (PALETTE_WIDTH - 2), PALETTE_HEIGHT - 16, 2, 16);
            graphics.setColor(Color.LIGHT_GRAY);
            graphics.fillRect(i * PALETTE_WIDTH, PALETTE_HEIGHT - 16, PALETTE_WIDTH, 2);
            graphics.fillRect(i * PALETTE_WIDTH, PALETTE_HEIGHT - 16, 2, 16);
            graphics.setColor(allowedColors[i] ? Color.GREEN : Color.BLACK);
            graphics.fillRect(i * PALETTE_WIDTH + 2, PALETTE_HEIGHT - 16 + 2, PALETTE_WIDTH - 4, 16 - 4);
        }
    }

    public TextVisualData getVisual() {
        return visual;
    }

    public void setVisual(TextVisualData visual) {
        this.visual = visual;
        Dimension dims = new Dimension(16 * PALETTE_WIDTH, PALETTE_HEIGHT);
        setMinimumSize(dims);
        setMaximumSize(dims);
        setPreferredSize(dims);
        repaint();
    }

    public boolean isTwoColorAllowed(int c) {
        return allowedColors[c >> 4] && allowedColors[c & 0xF];
    }

    public boolean isColorAllowed(int c) {
        return allowedColors[c];
    }

    public void setColorAllowed(int c, boolean v) {
        allowedColors[c] = v;
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

        boolean isAllSet = valuesInRanges.stream().allMatch(i -> allowedColors[i]);
        boolean settingMode = !isAllSet;
        valuesInRanges.forEach(i -> setColorAllowed(i, settingMode));
    }


    // input

    private int mcToColor(int x, int y) {
        int p = x / PALETTE_WIDTH;
        if (p < 0 || p >= 16) return -1;
        else return p;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
            int c = mcToColor(mouseEvent.getX(), mouseEvent.getY());
            if (c >= 0) {
                setColorAllowed(c, !allowedColors[c]);
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
