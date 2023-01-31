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

import java.util.*;

public class ElementLibrary {
    @Getter
    private final List<Element> elements = new ArrayList<>();
    private final Map<Integer, Element> elementsById = new HashMap<>();
    private final Map<Element, String> elementInternalNames = new HashMap<>();
    @Getter
    private final Map<String, Element> elementsByInternalNames = new HashMap<>();
    private Element empty;

    private ElementLibrary() {

    }

    private void finishBuilding() {
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

    static class Builder {
        private final ElementLibrary library = new ElementLibrary();

        private Builder() {

        }

        public Builder addElements(ElementLibrary other) {
            addElements(other.elements, other.elementInternalNames);
            return this;
        }

        public Builder addElements(List<Element> elements, Map<Element, String> internalNames) {
            for (Element element : elements) {
                String internalName = internalNames.get(element);
                addElement(element, internalName);
            }
            return this;
        }

        public Builder addElement(Element element, String internalName) {
            library.elements.add(element);
            library.elementsById.put(element.getId(), element);
            library.elementInternalNames.put(element, internalName);
            library.elementsByInternalNames.putIfAbsent(internalName, element);
            return this;
        }

        public ElementLibrary build() {
            this.library.finishBuilding();
            return this.library;
        }
    }

    static Builder builder() {
        return new Builder();
    }
}
