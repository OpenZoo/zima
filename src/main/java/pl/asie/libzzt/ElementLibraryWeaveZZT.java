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
package pl.asie.libzzt;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ElementLibraryWeaveZZT {
    private static final BiMap<String, String> INTERNAL_TO_WEAVE = HashBiMap.create(Map.ofEntries(
            Map.entry("BOARD_EDGE", "EDGE"),
            Map.entry("MESSAGE_TIMER", "DARKNESS"),
            Map.entry("BLINK_RAY_EW", "BLINKEW"),
            Map.entry("BLINK_RAY_NS", "BLINKNS"),
            Map.entry("UNKNOWN_46", "CUSTOMTEXT"),
            Map.entry("TEXT_BLUE", "BLUETEXT"),
            Map.entry("TEXT_GREEN", "GREENTEXT"),
            Map.entry("TEXT_CYAN", "CYANTEXT"),
            Map.entry("TEXT_RED", "REDTEXT"),
            Map.entry("TEXT_PURPLE", "PURPLETEXT"),
            Map.entry("TEXT_YELLOW", "YELLOWTEXT"),
            Map.entry("TEXT_WHITE", "WHITETEXT")
    ));

    private static final Map<String, Integer> COLOR_NUMBERS = Map.ofEntries(
            Map.entry("BLACK", 0),
            Map.entry("DKBLUE", 1),
            Map.entry("DKGREEN", 2),
            Map.entry("DKCYAN", 3),
            Map.entry("DKRED", 4),
            Map.entry("DKPURPLE", 5),
            Map.entry("BROWN", 6),
            Map.entry("GRAY", 7),
            Map.entry("GREY", 7),
            Map.entry("DKGRAY", 8),
            Map.entry("DKGREY", 8),
            Map.entry("BLUE", 9),
            Map.entry("GREEN", 10),
            Map.entry("CYAN", 11),
            Map.entry("RED", 12),
            Map.entry("PURPLE", 13),
            Map.entry("YELLOW", 14),
            Map.entry("WHITE", 15)
    );

    private ElementLibraryWeaveZZT() {

    }

    public static ElementLibrary create(ElementLibrary base, InputStream is) throws IOException {
        List<Element> elements = new ArrayList<>(base.getElements());
        List<String> names = elements.stream().map(base::getInternalName).collect(Collectors.toList());
        List<String> weaveNames = names.stream().map(e -> INTERNAL_TO_WEAVE.getOrDefault(e, e)).collect(Collectors.toList());

        // Weave ZZT default patches
        {
            int customTextIdx = weaveNames.indexOf("CUSTOMTEXT");
            if (customTextIdx >= 0) {
                elements.set(customTextIdx, elements.get(customTextIdx).withTextColor(0));
            }
        }

        // File patches
        if (is != null) try (InputStreamReader isr = new InputStreamReader(is); BufferedReader reader = new BufferedReader(isr)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String s = line.strip();
                if (s.length() > 1 && s.charAt(0) != '#') {
                    String[] keyValue = s.split("=", 2);
                    if (keyValue.length == 2) {
                        String[] key = keyValue[0].replaceAll("[^.A-Za-z]", "").split("\\.");
                        String value = keyValue[1].replaceAll("[^-.A-Za-z0-9]", "");
                        if (key.length == 2) {
                            int elementIdx = weaveNames.indexOf(key[0].toUpperCase(Locale.ROOT));
                            if (elementIdx >= 0) {
                                try {
                                    Integer valueNum = COLOR_NUMBERS.get(value.toUpperCase(Locale.ROOT));
                                    if (valueNum == null) {
                                        valueNum = Integer.parseInt(value);
                                    }
                                    System.out.println("Modifying " + names.get(elementIdx) + " -> " + key[1]);
                                    Element element = elements.get(elementIdx);
                                    if ("CHAR".equalsIgnoreCase(key[1])) {
                                        element = element.withCharacter(valueNum & 0xFF);
                                    } else if ("FG".equalsIgnoreCase(key[1])) {
                                        if (element.isText()) {
                                            element = element.withTextColor((element.getTextColor() & 0xF0) | (valueNum & 0x0F));
                                        } else {
                                            element = element.withColor((element.getColor() & 0xF0) | (valueNum & 0x0F));
                                        }
                                    } else if ("BG".equalsIgnoreCase(key[1])) {
                                        if (element.isText()) {
                                            element = element.withTextColor((element.getTextColor() & 0x0F) | ((valueNum & 0x0F) << 4));
                                        } else {
                                            element = element.withColor((element.getColor() & 0x0F) | ((valueNum & 0x0F) << 4));
                                        }
                                    } else if ("DESTRUCTIBLE".equalsIgnoreCase(key[1])) {
                                        element = element.withDestructible(value.equalsIgnoreCase("TRUE"));
                                    } else if ("PUSHABLE".equalsIgnoreCase(key[1])) {
                                        element = element.withPushable(value.equalsIgnoreCase("TRUE"));
                                    } else if ("PLACEABLEONTOP".equalsIgnoreCase(key[1])) {
                                        element = element.withPlaceableOnTop(value.equalsIgnoreCase("TRUE"));
                                    } else if ("WALKABLE".equalsIgnoreCase(key[1])) {
                                        element = element.withWalkable(value.equalsIgnoreCase("TRUE"));
                                    } else if ("CYCLE".equalsIgnoreCase(key[1])) {
                                        element = element.withCycle(valueNum);
                                    } else if ("SCOREVALUE".equalsIgnoreCase(key[1])) {
                                        element = element.withScoreValue(valueNum);
                                    }
                                    // Missing: PARAM1, PARAM2, PARAM3
                                    elements.set(elementIdx, element);
                                } catch (NumberFormatException e) {
                                    // skip
                                }
                            }
                        }
                    }
                }
            }
        }

        return new ElementLibrary(names, elements);
    }
}
