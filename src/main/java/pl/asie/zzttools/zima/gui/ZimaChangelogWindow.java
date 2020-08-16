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

import pl.asie.zzttools.util.FileUtils;
import pl.asie.zzttools.util.Triplet;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ZimaChangelogWindow {
    private static final List<String> versions;

    public static List<String> getVersions() {
        return versions;
    }

    static {
        List<String> versionsTmp;
        try {
            versionsTmp = Stream.of(ZimaLicenseWindow.readAllText("changelog/versions.txt")
                    .split("\n")).filter(s -> !s.isBlank()).collect(Collectors.toList());
        } catch (IOException e) {
            versionsTmp = List.of("unknown");
        }
        versions = versionsTmp;
    }

    public ZimaChangelogWindow(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Changelog", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        StringBuilder text = new StringBuilder();

        for (int i = versions.size() - 1; i >= 0; i--) {
            String version = versions.get(i);
            String versionText = "Unknown changes (file read error).";
            try {
                versionText = ZimaLicenseWindow.readAllText("changelog/" + version + ".txt").trim();
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
