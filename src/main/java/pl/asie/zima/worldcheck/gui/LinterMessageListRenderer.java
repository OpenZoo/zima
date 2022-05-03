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

import lombok.Getter;
import lombok.Setter;
import pl.asie.zima.util.Pair;
import pl.asie.zima.worldcheck.ElementLocation;
import pl.asie.zima.worldcheck.LinterMessage;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class LinterMessageListRenderer extends DefaultListCellRenderer {
	private static final int FONT_SIZE = 12;
	private static final Map<LinterMessage.Severity, Pair<Font, Color>> SEVERITY_FONT_MAP = Map.of(
			LinterMessage.Severity.NONE, Pair.of(new Font("Default", Font.ITALIC, FONT_SIZE), new Color(0xA0A0A0)),
			LinterMessage.Severity.INFO, Pair.of(new Font("Default", Font.PLAIN, FONT_SIZE), new Color(0x808080)),
			LinterMessage.Severity.HINT, Pair.of(new Font("Default", Font.PLAIN, FONT_SIZE), new Color(0x008000)),
			LinterMessage.Severity.WARNING, Pair.of(new Font("Default", Font.BOLD, FONT_SIZE), new Color(0x685800)),
			LinterMessage.Severity.ERROR, Pair.of(new Font("Default", Font.BOLD, FONT_SIZE), new Color(0x700000))
	);

	@Getter @Setter
	private int ignoreFrom = -1;

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		LinterMessage message = (LinterMessage) value;
		JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		LinterMessage.Severity renderSeverity = message.getSeverity();
		boolean renderIgnored = (ignoreFrom >= 0 && index >= ignoreFrom);
				var style = SEVERITY_FONT_MAP.get(renderSeverity);
		label.setFont(style.getFirst());
		label.setForeground(renderIgnored ? SEVERITY_FONT_MAP.get(LinterMessage.Severity.NONE).getSecond() : style.getSecond());
		return label;
	}
}
