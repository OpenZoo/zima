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
import pl.asie.libzzt.EngineDefinition;
import pl.asie.libzzt.oop.OopProgram;
import pl.asie.libzzt.oop.commands.OopCommand;
import pl.asie.libzzt.oop.commands.OopCommandTextLine;
import pl.asie.zima.binconv.BinconvPlatform;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public class OopTZTextWrapper implements OopTransformer {
	private final int wordWrapWidth;

	private boolean isNonEmptyText(OopCommand cmd) {
		return cmd instanceof OopCommandTextLine && !((OopCommandTextLine) cmd).getMessage().isEmpty();
	}

	private boolean isMergeableWith(OopCommandTextLine a, OopCommandTextLine b) {
		if (a.getType() != b.getType()) {
			return false;
		}
		if (a.getType() == OopCommandTextLine.Type.HYPERLINK) {
			if (!Objects.equals(a.getDestination(), b.getDestination())) {
				return false;
			}
			return false; // TODO: Cyber Purge needs this, but other programs might not like it
		} else if (a.getType() == OopCommandTextLine.Type.EXTERNAL_HYPERLINK) {
			if (!Objects.equals(a.getExternalDestination(), b.getExternalDestination())) {
				return false;
			}
			return false; // TODO: Cyber Purge needs this, but other programs might not like it
		}
		if (a.getType() == OopCommandTextLine.Type.REGULAR) {
			if ("".equals(a.getMessage())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void apply(EngineDefinition definition, ZxtWorld world, OopProgram program) {
		List<OopCommandTextLine> textLines = new ArrayList<>();
		List<OopCommand> commands = new ArrayList<>();
		var it = program.getCommands().listIterator();

		while (it.hasNext()) {
			OopCommand cmd = it.next();

			if (isNonEmptyText(cmd) && cmd instanceof OopCommandTextLine tl) {
				if (!textLines.isEmpty()) {
					OopCommandTextLine prev = textLines.get(0);
					if (!isMergeableWith(tl, prev)) {
						commands.add(new OopCommandTZWrappedTextLines(textLines, wordWrapWidth));
						textLines.clear();
					}
				}
				if (tl.getType() != OopCommandTextLine.Type.EXTERNAL_HYPERLINK) {
					textLines.add(tl);
				} else {
					System.err.println("WARNING: External hyperlinks not supported!");
				}
			} else {
				if (!textLines.isEmpty()) {
					commands.add(new OopCommandTZWrappedTextLines(textLines, wordWrapWidth));
					textLines.clear();
				}
				if (cmd instanceof OopCommandTextLine tl) {
					commands.add(new OopCommandTZWrappedTextLines(List.of(tl), wordWrapWidth));
				} else {
					commands.add(cmd);
				}
			}
		}

		program.setCommands(commands);
		if (!textLines.isEmpty()) {
			program.getCommands().add(new OopCommandTZWrappedTextLines(textLines, wordWrapWidth));
			textLines.clear();
		}
	}
}
