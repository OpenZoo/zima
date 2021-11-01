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
package pl.asie.zima.gui;

import pl.asie.zima.Version;
import pl.asie.zima.util.FileUtils;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

public class ZimaChangelogWindow {
    public ZimaChangelogWindow(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Changelog", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        StringBuilder text = new StringBuilder();
        List<String> versions = Version.getAll();

        for (int i = versions.size() - 1; i >= 0; i--) {
            String version = versions.get(i);
            String versionText = "Unknown changes (file read error).";
            try {
                versionText = FileUtils.readAllTextFromClasspath("changelog/" + version + ".txt").trim();
            } catch (IOException e) {
                // pass
            }
            text.append("== ").append(version).append(" ==\n\n").append(versionText);
            if (i > 0) {
                text.append("\n\n");
            }
        }

        JTextArea area = new JTextArea(text.toString());
        area.setRows(30);
        JScrollPane pane = new JScrollPane(area);

        dialog.add(pane);
        dialog.pack();
        dialog.setVisible(true);
    }
}
