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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import pl.asie.libzzt.TextVisualData;
import pl.asie.libzzt.TextVisualRenderer;
import pl.asie.zima.worldcheck.ElementLocation;
import pl.asie.zima.worldcheck.ElementLocationHolder;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class WorldCheckTreeCellRenderer extends DefaultTreeCellRenderer {
	private final TextVisualData visualData;
	private final Cache<ElementLocation, Optional<ImageIcon>> iconCache = CacheBuilder.newBuilder()
			.maximumSize(4096)
			.weakValues()
			.build();

	public void clear() {
		iconCache.invalidateAll();
	}

	private Optional<ImageIcon> create(ElementLocation location) {
		TextVisualRenderer renderer = new TextVisualRenderer(visualData, location.getWorld().getPlatform());
		if (location.getXPos() != null) {
			return Optional.of(new ImageIcon(renderer.render(location.getBoard(), false, location.getXPos(), location.getYPos(), 1, 1)));
		} else if (location.getBoardId() != null) {
			// board icon
			return Optional.of(new ImageIcon(renderer.render(1, 1, (x, y) -> 178, (x, y) -> 0x0E)));
		} else {
			// nothing
			return Optional.empty();
		}
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
		if (userObject instanceof ElementLocationHolder h) {
			ElementLocation location = h.getLocation();
			if (location != null) {
				try {
					Optional<ImageIcon> icon = iconCache.get(location, () -> create(location));
					icon.ifPresent(label::setIcon);
				} catch (Exception e) {
					// pass
				}
			}
		} else if (userObject instanceof ElementLocation location) {
			try {
				Optional<ImageIcon> icon = iconCache.get(location, () -> create(location));
				icon.ifPresent(label::setIcon);
			} catch (Exception e) {
				// pass
			}
		}
		return label;
	}
}
