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
package pl.asie.tinyzooconv.oop;

import lombok.Builder;
import pl.asie.libzxt.zzt.ZxtWorld;
import pl.asie.libzxt.zzt.oop.commands.OopCommandZxtIfExt;
import pl.asie.libzzt.EngineDefinition;
import pl.asie.libzzt.oop.OopProgram;
import pl.asie.libzzt.oop.commands.OopCommand;
import pl.asie.libzzt.oop.commands.OopCommandChar;
import pl.asie.libzzt.oop.commands.OopCommandComment;
import pl.asie.libzzt.oop.commands.OopCommandCycle;
import pl.asie.tinyzooconv.oop.OopTransformer;
import pl.asie.zima.binconv.BinconvPlatform;

@Builder
public class OopInlineIfExts implements OopTransformer {
	@Override
	public OopCommand transform(EngineDefinition definition, ZxtWorld world, OopProgram program, OopCommand command) {
		return command;
	}

	@Override
	public void apply(EngineDefinition definition, ZxtWorld world, OopProgram program) {
		var it = program.getCommands().listIterator();

		while (it.hasNext()) {
			OopCommand cmd = it.next();

			if (cmd instanceof OopCommandZxtIfExt c) {
				if (world.getZxtActiveIds().contains(c.getId())) {
					it.set(((OopCommandZxtIfExt) cmd).getTrueCommand());
					it.previous();
				} else {
					it.remove();
				}
			}
		}
	}
}
