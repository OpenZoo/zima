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
package pl.asie.zima.image.gui;

import pl.asie.zima.util.FileUtils;
import pl.asie.zima.util.Triplet;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

public class ZimaLicenseWindow {
    private static final List<Triplet<String, String, String>> LICENSE_TEXTS = List.of(
            new Triplet<>("zima", "licenses/COPYING", "licenses/COPYING.GPL"),
            new Triplet<>("gson", "licenses/3rdparty/LICENSE-GSON", "licenses/3rdparty/LICENSE-GSON.long"),
            new Triplet<>("Reconstruction of ZZT", "licenses/3rdparty/LICENSE-ROZ", "licenses/3rdparty/LICENSE-ROZ")
    );

    public ZimaLicenseWindow(JFrame parent, String zimaVersion) throws IOException {
        JDialog licenseDialog = new JDialog(parent, "About zima", true);
        licenseDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();

        {
            StringBuilder shortText = new StringBuilder("zima ").append(zimaVersion);
            shortText.append("\n\n");
            shortText.append(FileUtils.readAllTextFromClasspath(LICENSE_TEXTS.get(0).getSecond()));
            shortText.append("\n");
            shortText.append("zima contains the following third-party software under the following license terms:");
            for (int i = 1; i < LICENSE_TEXTS.size(); i++) {
                shortText.append("\n\n-- ").append(LICENSE_TEXTS.get(i).getFirst()).append(" --\n\n");
                shortText.append(FileUtils.readAllTextFromClasspath(LICENSE_TEXTS.get(i).getSecond()));
            }
            addLicenseTab(tabbedPane, "About", shortText.toString());
        }

        for (int i = 0; i < LICENSE_TEXTS.size(); i++) {
            String longText = FileUtils.readAllTextFromClasspath(LICENSE_TEXTS.get(i).getThird());
            addLicenseTab(tabbedPane, LICENSE_TEXTS.get(i).getFirst(), longText);
        }

        licenseDialog.add(tabbedPane);
        licenseDialog.pack();
        licenseDialog.setVisible(true);
    }

    private void addLicenseTab(JTabbedPane tabbedPane, String name, String text) {
        JTextArea area = new JTextArea(text);
        area.setRows(30);
        JScrollPane pane = new JScrollPane(area);

        tabbedPane.addTab(name, pane);
    }
}
