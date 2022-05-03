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
package pl.asie.zima.worldcheck.gui;

import lombok.Data;
import lombok.Getter;
import pl.asie.libzzt.Stat;
import pl.asie.zima.worldcheck.ElementLocation;

import javax.swing.*;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class OopCodePane {
	@Data
	private static class HighlightLocation {
		private final int start, end;
	}

	@Getter
	private final JTextPane textPane;
	@Getter
	private final JScrollPane pane;
	private final TreeMap<Integer, HighlightLocation> positionMapping = new TreeMap<>();

	public OopCodePane() {
		this.textPane = new JTextPane();
		this.textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		this.textPane.setEditable(false);
		this.pane = new JScrollPane(this.textPane);
	}

	public void highlight(Integer codePosition) {
		if (codePosition == null) {
			return;
		}

		HighlightLocation hl = positionMapping.ceilingEntry(codePosition).getValue();
		if (hl != null) {
			DefaultHighlighter.DefaultHighlightPainter highlightPainter =
					new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
			try {
				this.textPane.getHighlighter()
						.addHighlight(hl.start, hl.end, highlightPainter);
				if (hl.end < this.textPane.getCaretPosition()) {
					this.textPane.setCaretPosition(hl.end);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void update(ElementLocation location) {
		this.textPane.setText("");
		this.textPane.getHighlighter().removeAllHighlights();
		this.positionMapping.clear();
		if (location.getStat() != null && location.getStat().getData() != null) {
			Stat stat = location.getStat();
			String[] lines = stat.getData().split("\r");
			StringBuilder text = new StringBuilder();

			int posStart = 0;
			int posEnd = -1;
			int textPosStart = 0;
			int textPosEnd = 0;
			boolean lastLineWasEmpty = false;
			for (int i = 0; i < lines.length; i++) {
				if (!lastLineWasEmpty) posStart = posEnd + 1;
				String line = lines[i];
				posEnd = posEnd + 1 + line.length();
				if (!lastLineWasEmpty) textPosStart = text.length();
				if (!line.isEmpty()) {
					for (int k = 0; k < line.length(); k++) {
						char c = line.charAt(k);
						if (c >= 32 && c < 127) {
							text.appendCodePoint(c);
						} else {
							text.append("?");
						}
					}
					textPosEnd = text.length();
					positionMapping.put(posEnd - 1, new HighlightLocation(textPosStart, textPosEnd));
				}
				text.append('\n');
				lastLineWasEmpty = line.isEmpty();
			}

			this.textPane.setText(text.toString());
			this.textPane.setCaretPosition(textPosEnd);

			highlight(location.getStatPosition());
		}
	}
}
