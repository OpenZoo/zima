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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pl.asie.libzzt.Element;
import pl.asie.zima.Version;
import pl.asie.zima.image.ImageConverterMain;
import pl.asie.zima.image.gui.ZimaFrontendSwing;
import pl.asie.zima.util.gui.ImageFileChooser;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class BaseFrontendSwing {
    protected final Gson gson = new GsonBuilder().registerTypeAdapter(Element.class, ElementJsonSerdes.INSTANCE).create();
    protected final JFrame window;
    protected final JPanel mainPanel;
    protected final JMenuBar menuBar;

    protected BaseFrontendSwing(String name) {
        this.window = new JFrame(Version.getCurrentWindowName(name));
        // TODO: change if toolsMenu is removed
        this.window.setDefaultCloseOperation((this instanceof ZimaFrontendSwing) ? JFrame.EXIT_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE);

        this.mainPanel = new JPanel(new GridBagLayout());

        this.window.setJMenuBar(this.menuBar = new JMenuBar());
    }

    //

    protected void addGridBag(JPanel panel, Component c, Consumer<GridBagConstraints> gbcConsumer) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbcConsumer.accept(gbc);
        panel.add(c, gbc);
    }

    //

    private final Map<String, SoftReference<JFileChooser>> chooserCache = new HashMap<>();

    protected JFileChooser getOrCreateFileChooser(String ctx) {
        SoftReference<JFileChooser> ref = chooserCache.get(ctx);
        if (ref == null || ref.get() == null) {
            if (ctx.startsWith("image")) {
                JFileChooser nfc = ImageFileChooser.image();
                nfc.setCurrentDirectory(new File(System.getProperty("user.dir")));
                ref = new SoftReference<>(nfc);
            } else {
                JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
                ref = new SoftReference<>(fc);
            }
            chooserCache.put(ctx, ref);
        }
        return ref.get();
    }

    protected void setFileFilters(JFileChooser fc, FileNameExtensionFilter... filters) {
        fc.resetChoosableFileFilters();
        fc.setFileFilter(null);
        if (filters.length >= 1) {
            if (filters.length >= 2) {
                for (FileNameExtensionFilter f : filters) {
                    fc.addChoosableFileFilter(f);
                }
            } else {
                fc.setFileFilter(filters[0]);
            }
        }
    }

    protected File showLoadDialog(String context, FileNameExtensionFilter... filters) {
        JFileChooser fc = getOrCreateFileChooser(context);
        if (!(fc instanceof ImageFileChooser)) {
            setFileFilters(fc, filters);
        }
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnVal = fc.showOpenDialog(this.window);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile();
        } else {
            return null;
        }
    }

    protected File showSaveDialog(String context, FileNameExtensionFilter... filters) {
        JFileChooser fc = getOrCreateFileChooser(context);
        if (!(fc instanceof ImageFileChooser)) {
            setFileFilters(fc, filters);
        }
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        while (true) {
            int returnVal = fc.showSaveDialog(this.window);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                if (!(fc instanceof ImageFileChooser)) {
                    boolean endsWithValidExtension = false;
                    String defExtension = null;
                    String lcName = file.getName().toLowerCase(Locale.ROOT);
                    for (FileNameExtensionFilter f : filters) {
                        for (String extension : f.getExtensions()) {
                            if (defExtension == null) {
                                defExtension = extension;
                            }
                            if (lcName.endsWith("." + extension)) {
                                endsWithValidExtension = true;
                                break;
                            }
                        }
                        if (endsWithValidExtension) break;
                    }
                    if (!endsWithValidExtension && defExtension != null) {
                        file = new File(file.toString() + "." + defExtension);
                    }
                }
                if (file.exists()) {
                    int result = JOptionPane.showConfirmDialog(this.window,
                            "The file already exists, do you want to overwrite it?", "File exists!",
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            return file;
                        case JOptionPane.NO_OPTION:
                            break;
                        default:
                            return null;
                    }
                } else {
                    return file;
                }
            } else {
                return null;
            }
        }
    }

    protected JMenu addHelpMenu() {
        JMenu helpMenu;
        JMenuItem changelogItem, aboutItem;

        this.menuBar.add(helpMenu = new JMenu("Help"));
        helpMenu.add(changelogItem = new JMenuItem("Changelog"));
        helpMenu.add(aboutItem = new JMenuItem("About"));

        changelogItem.addActionListener(this::onChangelog);
        aboutItem.addActionListener(this::onAbout);

        return helpMenu;
    }

    protected void finishWindowInit() {
        this.window.add(this.mainPanel);
        this.window.pack();
        this.window.setMinimumSize(this.window.getSize());
        this.window.setVisible(true);
    }

    //

    public void onChangelog(ActionEvent event) {
        new ZimaChangelogWindow(window);
    }

    public void onAbout(ActionEvent event) {
        try {
            new ZimaLicenseWindow(window, Version.getCurrent());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this.window, "zima " + Version.getCurrent() + " - copyright (c) 2020, 2021 asie", "About", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
