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

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.davidmoten.text.utils.WordWrap;
import pl.asie.libzzt.oop.OopLabelTarget;
import pl.asie.libzzt.oop.commands.OopCommand;
import pl.asie.libzzt.oop.commands.OopCommandTextLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopCommandTZWrappedTextLines extends OopCommand {
	private List<OopCommandTextLine> lines;
	private int lineCount;

	public OopCommandTZWrappedTextLines(List<OopCommandTextLine> originalLines, int wordWrapWidth) {
		this.lines = new ArrayList<>();
		this.lineCount = originalLines.size();
		if (originalLines.size() == 1 && originalLines.get(0).getMessage().isEmpty() && originalLines.get(0).getType() == OopCommandTextLine.Type.REGULAR) {
			this.lineCount = 0;
		}

		List<OopCommandTextLine> lineBuffer = new ArrayList<>();
		for (OopCommandTextLine line : originalLines) {
			// If the line is empty, make it stand alone.
			if (line.getType() != OopCommandTextLine.Type.HYPERLINK && line.getMessage().isBlank()) {
				addLineBuffer(lineBuffer, wordWrapWidth);
				lineBuffer.clear();
				lineBuffer.add(line);
				addLineBuffer(lineBuffer, wordWrapWidth);
				lineBuffer.clear();
				continue;
			}

			boolean lineCompatible = false;
			if (lineBuffer.isEmpty()) {
				lineCompatible = true;
			} else {
				OopCommandTextLine currLine = lineBuffer.get(0);
				if (Objects.equals(currLine.getType(), line.getType()) && Objects.equals(currLine.getDestination(), line.getDestination())) {
					lineCompatible = true;
				}
			}
			if (!lineCompatible) {
				addLineBuffer(lineBuffer, wordWrapWidth);
				lineBuffer.clear();
			}
			lineBuffer.add(line);
		}

		if (!lineBuffer.isEmpty()) {
			addLineBuffer(lineBuffer, wordWrapWidth);
		}
	}

	private void addLineBuffer(List<OopCommandTextLine> buffer, int wordWrapWidth) {
		if (buffer.isEmpty()) {
			return;
		}

		OopCommandTextLine currLine = buffer.get(0);
		String fullText = buffer.stream().map(OopCommandTextLine::getMessage).collect(Collectors.joining(" "));
		if (currLine.getType() == OopCommandTextLine.Type.EXTERNAL_HYPERLINK) {
			throw new RuntimeException("Unsupported text line type: " + currLine.getType());
		} else if (currLine.getType() == OopCommandTextLine.Type.HYPERLINK) {
			wordWrapWidth -= 3;
		}

		if (fullText.isBlank()) {
			lines.add(createTextLine(currLine.getType(), currLine.getDestination(), currLine.getExternalDestination(), "", wordWrapWidth));
		} else {
			try {
				for (String s : wrap(fullText, wordWrapWidth)) {
					lines.add(createTextLine(currLine.getType(), currLine.getDestination(), currLine.getExternalDestination(), s, wordWrapWidth));
				}
			} catch (IllegalArgumentException e) {
				fullText = buffer.stream().map(OopCommandTextLine::getMessage).map(String::strip).collect(Collectors.joining(" "));
				try {
					for (String s : wrap(fullText, wordWrapWidth)) {
						lines.add(createTextLine(currLine.getType(), currLine.getDestination(), currLine.getExternalDestination(), s, wordWrapWidth));
					}
				} catch (IllegalArgumentException ee) {
					throw new RuntimeException(fullText, ee);
				}
			}
		}
	}

	private List<String> wrap(String fullText, int wordWrapWidth) {
		List<String> list = new ArrayList<>(WordWrap.from(fullText).maxWidth(wordWrapWidth).wrapToList());
		for (int i = 0; i < list.size(); i++) {
			String s = list.get(i);
			if (s.length() > wordWrapWidth) {
				String rest = String.join(" ", list.subList(i + 1, list.size()));
				while (list.size() > i) {
					list.remove(i);
				}
				while (s.length() > wordWrapWidth) {
					list.add(s.substring(0, wordWrapWidth));
					s = s.substring(wordWrapWidth);
				}
				list.addAll(WordWrap.from(s + " " + rest).maxWidth(wordWrapWidth).wrapToList());
			}
		}
		return list;
	}

	private OopCommandTextLine createTextLine(OopCommandTextLine.Type type, OopLabelTarget destination, String externalDestination, String s, int wordWrapWidth) {
		if (s.length() > wordWrapWidth) {
			throw new RuntimeException("Line too long: " + s.length() + " > " + wordWrapWidth);
		}
		return new OopCommandTextLine(type, destination, externalDestination, s);
	}
}
