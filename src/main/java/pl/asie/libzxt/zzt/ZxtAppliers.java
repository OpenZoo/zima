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

import pl.asie.libzxt.ZxtExtensionId;
import pl.asie.libzxt.zzt.oop.ZxtOopHelpers;
import pl.asie.libzxt.zzt.oop.commands.OopCommandZxtDieItem;
import pl.asie.libzxt.zzt.oop.commands.OopCommandZxtIfExt;
import pl.asie.libzxt.zzt.oop.commands.OopCommandZxtViewport;
import pl.asie.libzxt.zzt.oop.conditions.OopConditionZxtRnd;
import pl.asie.libzzt.oop.OopParseException;
import pl.asie.libzzt.oop.commands.OopCommand;
import pl.asie.libzzt.oop.conditions.OopCondition;
import pl.asie.libzzt.oop.directions.OopDirection;

import java.util.HashMap;
import java.util.Map;

final class ZxtAppliers {
	static final Map<ZxtExtensionId, ZxtEngineDefinitionApplier> APPLIER_MAP;

	private static void add(long owner, long selector, ZxtEngineDefinitionApplier applier) {
		APPLIER_MAP.put(new ZxtExtensionId((int) owner, (short) selector), applier);
	}

	static {
		APPLIER_MAP = new HashMap<>();

		add(0x00000000, 0x0001, (definition, block) -> {
			definition.getOopParserConfiguration().addParser(OopCommand.class, "IFEXT", context -> {
				long owner = ZxtOopHelpers.readHex(context, 32);
				long selector = ZxtOopHelpers.readHex(context, 16);
				return new OopCommandZxtIfExt(new ZxtExtensionId((int) owner, (short) selector), context.parseCommand());
			});
			return true;
		});
		add(0x0000A51E, 0x0001, (definition, block) -> {
			definition.getOopParserConfiguration().addParser(OopCommand.class, "DIE", context -> {
				context.readWord();
				String word = context.getWord();
				if ("ITEM".equals(word)) {
					return new OopCommandZxtDieItem();
				}
				return null;
			});
			return true;
		});
		add(0x0000A51E, 0x0004, (definition, block) -> {
			definition.getOopParserConfiguration().addParser(OopCondition.class, "RND", context -> new OopConditionZxtRnd());
			return true;
		});
		add(0x0000A51E, 0x0007, (definition, block) -> {
			definition.getOopParserConfiguration().addParser(OopCommand.class, "VIEWPORT", context -> {
				context.readWord();
				String word = context.getWord();
				if ("LOCK".equals(word)) {
					return new OopCommandZxtViewport(OopCommandZxtViewport.Type.LOCK);
				} else if ("UNLOCK".equals(word)) {
					return new OopCommandZxtViewport(OopCommandZxtViewport.Type.UNLOCK);
				} else if ("MOVE".equals(word)) {
					return new OopCommandZxtViewport(OopCommandZxtViewport.Type.MOVE, context.parseType(OopDirection.class));
				} else if ("FOCUS".equals(word)) {
					context.readWord();
					return new OopCommandZxtViewport(OopCommandZxtViewport.Type.FOCUS, context.getWord());
				} else {
					throw new OopParseException("Unrecognized VIEWPORT sub-command: " + word);
				}
			});
			return true;
		});
	}
}
