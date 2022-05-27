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

import lombok.Getter;
import pl.asie.libzzt.oop.OopUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ElementLibrary {
    @Getter
    private final List<Element> elements;
    private final Map<Integer, Element> elementsById = new HashMap<>();
    private final Map<Element, String> elementInternalNames = new HashMap<>();
    private final Map<String, Element> elementsByInternalNames = new HashMap<>();
    private final Element empty;

    ElementLibrary(List<Element> elements, Map<Element, String> internalNames) {
        this.elements = List.copyOf(elements);
        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            String internalName = internalNames.get(element);

            elementsById.put(element.getId(), element);
            elementInternalNames.put(element, internalName);
            elementsByInternalNames.putIfAbsent(internalName, element);
        }

        empty = elementsById.get(0);
        if (empty == null) {
            throw new RuntimeException();
        }
    }

    public Element byId(int id) {
        return elementsById.getOrDefault(id, empty);
    }

    public boolean isIdValid(int elementId) {
        return elementsById.containsKey(elementId);
    }

    public Element byInternalName(String name) {
        return elementsByInternalNames.getOrDefault(name, empty);
    }

    public Element byInternalNameOrNull(String name) {
        return elementsByInternalNames.get(name);
    }

    public Element byOopTokenName(String name) {
        for (Element element : elements) {
            if (Objects.equals(OopUtils.stripChars(name), element.getOopName())) {
                return element;
            }
        }
        return null;
    }

    public String getInternalName(Element element) {
        return elementInternalNames.getOrDefault(element, "(unknown)");
    }

    public Element getEmpty() {
        return empty;
    }
}
