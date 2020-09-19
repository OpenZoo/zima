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
package pl.asie.zima;

import pl.asie.zima.image.ImageConverterMain;
import pl.asie.zima.util.MiscUtils;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length <= 0) {
            // GUI
            // TODO: expose more than image converter
            ImageConverterMain.main(args);
        } else {
            System.err.println("zima " + Version.getCurrent());
            System.err.println();

            // CLI
            List<CliPlugin> plugins = List.of();
            for (CliPlugin plugin : plugins) {
                if (args[0].equals(plugin.getName())) {
                    plugin.run(MiscUtils.shift(args, 1, String[]::new));
                    return;
                }
            }
            // no plugin
            System.err.println("Usage: <.jar> <plugin-name> <arguments...>");
            System.err.println();
            System.err.println("Available plugins:");
            for (CliPlugin plugin : plugins) {
                System.err.println("\t- " + plugin.getName());
            }
        }
    }
}
