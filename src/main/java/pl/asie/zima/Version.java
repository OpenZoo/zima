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
package pl.asie.zima;

import pl.asie.zima.util.FileUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Version {
    private static final List<String> versions;

    public static String getCurrent() {
        return versions != null && !versions.isEmpty() ? versions.get(versions.size() - 1) : "[unknown version]";
    }

    public static List<String> getAll() {
        return versions;
    }

    private Version() {

    }

    static {
        List<String> versionsTmp;
        try {
            versionsTmp = Stream.of(FileUtils.readAllTextFromClasspath("changelog/versions.txt")
                    .split("\n")).filter(s -> !s.isBlank()).collect(Collectors.toList());
        } catch (IOException e) {
            versionsTmp = List.of("unknown");
        }
        versions = versionsTmp;
    }
}
