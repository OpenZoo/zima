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

import com.google.gson.*;
import pl.asie.libzzt.Element;
import pl.asie.libzzt.Platform;

import java.lang.reflect.Type;

public final class ElementJsonSerdes implements JsonSerializer<Element>, JsonDeserializer<Element> {
    public static final ElementJsonSerdes INSTANCE = new ElementJsonSerdes();

    private ElementJsonSerdes() {

    }

    @Override
    public Element deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive()) {
            JsonPrimitive jp = json.getAsJsonPrimitive();
            if (jp.isNumber()) {
                return Platform.ZZT.getLibrary().byId(jp.getAsInt());
            } else if (jp.isString()) {
                return Platform.ZZT.getLibrary().byInternalName(jp.getAsString());
            }
        }
        return Platform.ZZT.getLibrary().getEmpty();
    }

    @Override
    public JsonElement serialize(Element src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getId());
    }
}
