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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ColorSelector extends JComponent implements MouseListener {
    private static final int BOX_SIZE = 14;
    @Getter private TextVisualData visual;
    private boolean[] allowedColors = new boolean[16];
    private final Runnable changeListener;

    public ColorSelector(Runnable changeListener) {
        addMouseListener(this);
        this.changeListener = changeListener;
    }

    @Override
    public void paintComponent(Graphics graphics) {
        Color[] awtPalette = new Color[allowedColors.length];
        for (int i = 0; i < allowedColors.length; i++) {
            awtPalette[i] = ColorUtils.toAwtColor(visual.getPalette()[i]);
        }
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, allowedColors.length * BOX_SIZE, BOX_SIZE);
        // draw palette
        for (int ix = 0; ix < allowedColors.length; ix++) {
            boolean enabled = allowedColors[ix];
            Color fgColor = awtPalette[ix];
            int xPos = ix * BOX_SIZE;
            int yPos = 0;

            if (enabled) {
                graphics.setColor(Color.GREEN);
                graphics.fillRect(xPos + 1, yPos + 1, 12, 12);
            }
            graphics.setColor(Color.BLACK);
            graphics.fillRect(xPos + 2, yPos + 2, 10, 10);
            graphics.setColor(fgColor);
            graphics.fillRect(xPos + 3, yPos + 3, 8, 8);
        }
    }

    public void setVisual(TextVisualData visual) {
        this.visual = visual;
        Dimension dims = new Dimension(this.visual.getPalette().length * BOX_SIZE, BOX_SIZE);
        if (this.visual.getPalette().length != allowedColors.length) {
            allowedColors = new boolean[this.visual.getPalette().length];
        }
        setMinimumSize(dims);
        setMaximumSize(dims);
        setPreferredSize(dims);
        repaint();
    }

    public boolean isColorAllowed(int c) {
        return c >= 0 && c < allowedColors.length && allowedColors[c];
    }

    public Set<Integer> toSet() {
        return IntStream.range(0, allowedColors.length).filter(this::isColorAllowed).boxed().collect(Collectors.toSet());
    }

    public void change() {
        changeListener.run();
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

    // input

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
            final int mx = mouseEvent.getX() / BOX_SIZE;
            setColorAllowed(mx, !allowedColors[mx]);
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
