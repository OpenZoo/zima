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
package pl.asie.libzzt.oop;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import pl.asie.libzzt.EngineDefinition;
import pl.asie.libzzt.oop.commands.OopCommand;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode
@ToString
public class OopProgram {
	@Getter
	String windowName;
	@Getter
	String name;
	@Getter
	List<OopCommand> commands = new ArrayList<>();

	public OopProgram(EngineDefinition engineDefinition, String data) throws OopParseException {
		OopProgramParser parser = new OopProgramParser(engineDefinition);
		parser.parse(this, data);
	}
}
