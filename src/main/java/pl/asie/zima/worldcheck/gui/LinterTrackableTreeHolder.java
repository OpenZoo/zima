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
import pl.asie.libzzt.Board;
import pl.asie.libzzt.World;
import pl.asie.zima.worldcheck.ElementLocation;
import pl.asie.zima.worldcheck.LinterTrackable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

class LinterTrackableTreeHolder {
	@Getter
	private final String name;
	@Getter
	private final JTree tree;
	@Getter
	private final JScrollPane pane;
	private final Consumer<ElementLocation> locationConsumer;

	LinterTrackableTreeHolder(String name, Consumer<ElementLocation> locationConsumer) {
		this.name = name;
		this.tree = new JTree();
		this.pane = new JScrollPane(this.tree);
		this.locationConsumer = locationConsumer;
		if (this.locationConsumer != null) {
			this.tree.addTreeSelectionListener(tse -> {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.tree.getLastSelectedPathComponent();
				if (node != null) {
					ElementLocationTextNode eltn = (ElementLocationTextNode) node.getUserObject();
					if (eltn.getLocation() != null) {
						locationConsumer.accept(eltn.getLocation());
					}
				}
			});
		}
	}

	private DefaultMutableTreeNode createNodeForTrackable(LinterTrackable lt) {
		DefaultMutableTreeNode ltNode = new DefaultMutableTreeNode(new ElementLocationTextNode(lt.getName()));
		for (var tl : lt.getTrackingLocations()) {
			if (!tl.getSecond().isEmpty()) {
				DefaultMutableTreeNode tlNode = new DefaultMutableTreeNode(new ElementLocationTextNode(tl.getFirst()));
				for (ElementLocation el : tl.getSecond()) {
					DefaultMutableTreeNode elNode = new DefaultMutableTreeNode(new ElementLocationTextNode(el, null));
					tlNode.add(elNode);
				}
				ltNode.add(tlNode);
			}
		}
		return ltNode;
	}

	<T extends LinterTrackable> void update(Stream<T> trackables) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(new ElementLocationTextNode(this.name));
		if (trackables != null) {
			trackables.forEach(lt -> root.add(createNodeForTrackable(lt)));
		}
		this.tree.setModel(new DefaultTreeModel(root));
	}

	private ElementLocationTextNode getBoardName(World world, int boardIdx) {
		return new ElementLocationTextNode("Board " + boardIdx + ": " + world.getBoards().get(boardIdx).getName());
	}

	<T extends LinterTrackable> void updatePerBoard(ElementLocation currentLocation, Map<Integer, Stream<T>> trackables) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(new ElementLocationTextNode(this.name));
		if (currentLocation != null) {
			for (var entry : trackables.entrySet()) {
				DefaultMutableTreeNode boardNode = new DefaultMutableTreeNode(
						getBoardName(currentLocation.getWorld(), entry.getKey())
				);
				entry.getValue().forEach(lt -> boardNode.add(createNodeForTrackable(lt)));
				root.add(boardNode);
			}
		}
		this.tree.setModel(new DefaultTreeModel(root));
	}
}
