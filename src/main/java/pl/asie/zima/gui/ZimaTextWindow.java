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
package pl.asie.zima.gui;

import pl.asie.zima.Version;
import pl.asie.zima.util.FileUtils;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

public class ZimaTextWindow {
    public ZimaTextWindow(JFrame parent, String title, String text) {
        JDialog dialog = new JDialog(parent, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JTextArea area = new JTextArea(text);
        area.setRows(30);
        JScrollPane pane = new JScrollPane(area);

        dialog.add(pane);
        dialog.pack();
        dialog.setVisible(true);
    }
}
