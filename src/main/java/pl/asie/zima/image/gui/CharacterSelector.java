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

import pl.asie.libzzt.Platform;
import pl.asie.libzzt.TextVisualData;
import pl.asie.libzzt.TextVisualRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CharacterSelector extends JComponent implements MouseListener, MouseMotionListener {
    private TextVisualData visual;
    private TextVisualRenderer renderer;
    private final boolean[] allowedChars = new boolean[256];
    private Boolean settingMode = null;
    private final Runnable changeListener;

    public CharacterSelector(Runnable changeListener) {
        Arrays.fill(allowedChars, true);
        addMouseListener(this);
        addMouseMotionListener(this);
        this.changeListener = changeListener;
    }

    @Override
    public void paintComponent(Graphics graphics) {
        BufferedImage image = renderer.render(32, 8,
                (x, y) -> (y << 5) | x,
                (x, y) -> allowedChars[(y << 5) | x] ? 0x2F : 0x04
        );
        graphics.drawImage(image, 0, 0, null);
    }

    public TextVisualData getVisual() {
        return visual;
    }

    public void setVisual(TextVisualData visual) {
        this.visual = visual;
        this.renderer = new TextVisualRenderer(visual, Platform.ZZT);
        Dimension dims = new Dimension(32 * visual.getCharWidth(), 8 * visual.getCharHeight());
        setMinimumSize(dims);
        setMaximumSize(dims);
        setPreferredSize(dims);
        repaint();
    }

    public boolean isCharAllowed(int c) {
        return allowedChars[c];
    }

    public Set<Integer> toSet() {
        return IntStream.range(0, 256).filter(this::isCharAllowed).boxed().collect(Collectors.toSet());
    }

    public void change() {
        changeListener.run();
    }

    public void setCharAllowed(int c, boolean v) {
        if (allowedChars[c] != v) {
            allowedChars[c] = v;
            repaint();
            changeListener.run();
        }
    }

    public void toggleCharAllowed(int... ranges) {
        List<Integer> valuesInRanges = new ArrayList<>();
        for (int i = 0; i < ranges.length; i += 2) {
            for (int j = ranges[i]; j <= ranges[i+1]; j++) {
                valuesInRanges.add(j);
            }
        }

        boolean isAllSet = valuesInRanges.stream().allMatch(i -> allowedChars[i]);
        boolean settingMode = !isAllSet;
        valuesInRanges.forEach(i -> setCharAllowed(i, settingMode));
    }

    // input

    private int mcToChar(int x, int y) {
        int p = ((y / visual.getCharHeight()) << 5) | (x / visual.getCharWidth());
        if (p < 0 || p >= 256) return -1;
        else return p;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
            int c = mcToChar(mouseEvent.getX(), mouseEvent.getY());
            settingMode = (c >= 0) ? !allowedChars[c] : null;
            if (c >= 0) {
                setCharAllowed(c, settingMode);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        settingMode = null;
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        int c = mcToChar(mouseEvent.getX(), mouseEvent.getY());
        if (c >= 0 && settingMode != null) {
            setCharAllowed(c, settingMode);
        }
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {

    }
}
