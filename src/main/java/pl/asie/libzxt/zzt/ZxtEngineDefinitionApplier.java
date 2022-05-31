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
package pl.asie.libzxt.zzt;

import pl.asie.libzxt.ZxtExtensionBlock;
import pl.asie.libzzt.EngineDefinition;
import pl.asie.libzzt.oop.OopTokenParser;
import pl.asie.libzzt.oop.commands.OopCommand;

public interface ZxtEngineDefinitionApplier {
	boolean apply(EngineDefinition definition, ZxtExtensionBlock block);

	static ZxtEngineDefinitionApplier quirk(String name) {
		return (definition, block) -> {
			definition.addQuirk(name);
			return true;
		};
	}
}
