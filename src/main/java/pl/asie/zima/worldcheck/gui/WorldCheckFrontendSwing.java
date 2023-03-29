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

import pl.asie.libzxt.ZxtExtensionParser;
import pl.asie.libzzt.Board;
import pl.asie.libzzt.Stat;
import pl.asie.libzzt.TextVisualData;
import pl.asie.zima.Constants;
import pl.asie.zima.gui.BaseFrontendSwing;
import pl.asie.zima.image.ImageConverterMain;
import pl.asie.zima.util.FileUtils;
import pl.asie.zima.util.ZimaPlatform;
import pl.asie.zima.worldcheck.ElementLocation;
import pl.asie.zima.worldcheck.LinterCheck;
import pl.asie.zima.worldcheck.LinterLabel;
import pl.asie.zima.worldcheck.LinterMessage;
import pl.asie.zima.worldcheck.LinterWorldHolder;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

public class WorldCheckFrontendSwing extends BaseFrontendSwing {
	private static final boolean DEBUG = false;
	private static final TextVisualData VISUAL_DATA;
	private static final Toolkit TOOLKIT = Toolkit.getDefaultToolkit();

	static {
		try {
			VISUAL_DATA = new TextVisualData(
					8, 14,
					FileUtils.readAll(Objects.requireNonNull(ImageConverterMain.class.getClassLoader().getResourceAsStream("8x14.bin"))),
					Constants.EGA_PALETTE
			);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static class WorldHolder extends LinterWorldHolder {
		private final WorldCheckTreeCellRenderer worldTreeCellRenderer;
		private final JTree uiLocationTree;
		private final JScrollPane uiLocationTreePane;
		private final JTree uiInformationTree;
		private final JScrollPane uiInformationTreePane;
		private final JList<LinterMessage> uiLinterList;
		private final LinterMessageListRenderer uiLinterListRenderer;
		private final JScrollPane uiLinterListPane;
		private final LinterTrackableTreeHolder uiFlagsTree;
		private final LinterTrackableTreeHolder uiLabelsTree;
		private final OopCodePane uiOopCodePane;
		private final JTabbedPane uiTabListPane;
		private final BoardCanvas uiCanvas;
		private ElementLocation currentLocation;

		WorldHolder() {
			this.worldTreeCellRenderer = new WorldCheckTreeCellRenderer(VISUAL_DATA, ZimaPlatform.ZZT);
			this.uiCanvas = new BoardCanvas(false, VISUAL_DATA, ZimaPlatform.ZZT);

			this.uiCanvas.setMinimumSize(new Dimension(480, 350));
			this.uiCanvas.setPreferredSize(new Dimension(480, 350));
			this.uiLocationTree = new JTree();
			this.uiLocationTree.setCellRenderer(this.worldTreeCellRenderer);
			this.uiLocationTreePane = new JScrollPane(this.uiLocationTree);
			this.uiLocationTreePane.setMinimumSize(new Dimension(240, 350));
			this.uiLocationTreePane.setPreferredSize(new Dimension(240, 350));
			this.uiInformationTree = new JTree();
			this.uiInformationTree.setCellRenderer(this.worldTreeCellRenderer);
			this.uiInformationTreePane = new JScrollPane(this.uiInformationTree);
			this.uiInformationTreePane.setMinimumSize(new Dimension(240, 225));
			this.uiInformationTreePane.setPreferredSize(new Dimension(240, 225));
			this.uiLinterList = new JList<>();
			this.uiLinterListRenderer = new LinterMessageListRenderer();
			this.uiLinterList.setCellRenderer(this.uiLinterListRenderer);
			this.uiLinterListPane = new JScrollPane(this.uiLinterList);
			this.uiTabListPane = new JTabbedPane();
			this.uiTabListPane.setMinimumSize(new Dimension(480 + 240, 225));
			this.uiTabListPane.setPreferredSize(new Dimension(480 + 240, 225));
			this.uiOopCodePane = new OopCodePane();

			this.uiLocationTree.addTreeSelectionListener(tse -> {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) this.uiLocationTree.getLastSelectedPathComponent();
				if (node != null) {
					changeLocationTo((ElementLocation) node.getUserObject());
				}
 			});

			this.uiLinterList.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					int index = uiLinterList.locationToIndex(e.getPoint());
					if (index >= 0 && index < uiLinterList.getModel().getSize()) {
						LinterMessage message = uiLinterList.getModel().getElementAt(index);
						if (message.getLocation() != null) {
							changeLocationTo(message.getLocation());
							if (!message.getRelevantPositions().isEmpty()) {
								message.getRelevantPositions().forEach(uiOopCodePane::highlight);
								openCodePane();
							}
						}
					}
				}
			});

			this.uiFlagsTree = new LinterTrackableTreeHolder("Flags", this::changeLocationTo);
			this.uiFlagsTree.getTree().setCellRenderer(this.worldTreeCellRenderer);
			this.uiLabelsTree = new LinterTrackableTreeHolder("Labels", this::changeLocationTo);
			this.uiLabelsTree.getTree().setCellRenderer(this.worldTreeCellRenderer);

			this.uiTabListPane.addTab("Linter", this.uiLinterListPane);
			this.uiTabListPane.addTab("Flags", this.uiFlagsTree.getPane());
			this.uiTabListPane.addTab("Labels", this.uiLabelsTree.getPane());
			this.uiTabListPane.addTab("Code", this.uiOopCodePane.getPane());

			this.uiCanvas.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					Board board = currentLocation.getBoard();
					if (board != null) {
						Point p = e.getPoint();
						int x = (p.x / VISUAL_DATA.getCharWidth()) + 1;
						int y = (p.y / VISUAL_DATA.getCharHeight()) + 1;
						if (x >= 1 && y >= 1 && x <= board.getWidth() && y <= board.getHeight()) {
							ElementLocation newLocation = ElementLocation.element(
									currentLocation.getWorld(),
									currentLocation.getBoardId(),
									x, y
							);
							if (!newLocation.includes(currentLocation)) {
								changeLocationTo(newLocation);
							}
						}
					}
				}
			});
		}

		void openCodePane() {
			uiTabListPane.setSelectedIndex(uiTabListPane.indexOfTab("Code"));
			// uiOopCodePane.getPane().getVerticalScrollBar().setValue(0);
		}

		TreeModel buildLocationTreeModel() {
			if (this.world == null) {
				return new DefaultTreeModel(null);
			}

			DefaultMutableTreeNode root = new DefaultMutableTreeNode(ElementLocation.world(this.world));
			for (int i = 0; i < this.world.getBoards().size(); i++) {
				Board b = this.world.getBoards().get(i);
				DefaultMutableTreeNode rootBoard = new DefaultMutableTreeNode(ElementLocation.board(this.world, i));
				for (int j = 0; j < b.getStats().size(); j++) {
					Stat s = b.getStat(j);
					DefaultMutableTreeNode rootBoardStat = new DefaultMutableTreeNode(ElementLocation.stat(this.world, i, j));
					rootBoard.add(rootBoardStat);
				}
				root.add(rootBoard);
			}
			return new DefaultTreeModel(root);
		}

		TreeModel buildInformationTreeModel() {
			if (this.world == null) {
				return new DefaultTreeModel(null);
			}

			DefaultMutableTreeNode root = new DefaultMutableTreeNode("Information");
			// TODO
			return new DefaultTreeModel(root);
		}

		ListModel<LinterMessage> buildLinterMessageListModel() {
			var model = new DefaultListModel<LinterMessage>();
			if (this.linterCheck != null) {
				this.linterCheck.getMessagesOnePerCommand().stream()
						.filter(lm -> this.currentLocation == null
								|| this.currentLocation.includes(lm.getLocation())
								|| lm.getLocation().getBoardId() == null)
						.forEach(model::addElement);
				List<LinterMessage> otherMessages =
						this.linterCheck.getMessagesOnePerCommand().stream()
								.filter(m -> !model.contains(m))
								.toList();
				if (!otherMessages.isEmpty()) {
					this.uiLinterListRenderer.setIgnoreFrom(model.getSize());
					model.addElement(new LinterMessage(
							null, null, "***"
					));
					model.addAll(otherMessages);
				} else {
					this.uiLinterListRenderer.setIgnoreFrom(-1);
				}
			}
			return model;
		}

		void updateBoardPreview() {
			synchronized (uiCanvas) {
				Board board = null;
				if (this.world != null) {
					if (this.currentLocation != null) {
						if (this.currentLocation.getBoardId() != null) {
							board = world.getBoards().get(this.currentLocation.getBoardId());
						} else {
							board = world.getBoards().get(world.getCurrentBoard());
						}
					}
				}
				this.uiCanvas.setBoard(board);
			}
		}

		void onWorldLocationChange() {
			this.uiInformationTree.setModel(buildInformationTreeModel());

			SortedMap<Integer, Stream<LinterLabel>> labelsMap = new TreeMap<>();
			if (this.linterCheck != null) {
				for (var entry : this.linterCheck.getLabels().getLabels().entrySet()) {
					labelsMap.put(entry.getKey(), entry.getValue().values().stream());
				}
			}
			this.uiLabelsTree.updatePerBoard(this.currentLocation, labelsMap);
		}

		void onBoardLocationChange() {
			updateBoardPreview();
			this.uiLinterList.setModel(buildLinterMessageListModel());
			this.uiLinterListPane.getVerticalScrollBar().setValue(0);
			this.worldTreeCellRenderer.setBoardLocation(this.currentLocation);
		}

		void onLocationChange(ElementLocation fullLocation) {
			this.uiOopCodePane.update(fullLocation);
			if (fullLocation.getStatPosition() != null) {
				openCodePane();
			}
		}

		void changeLocationTo(ElementLocation location) {
			if (location != null) {
				ElementLocation boardLocation = location.getBoardId() != null
						? ElementLocation.board(location.getWorld(), location.getBoardId())
						: ElementLocation.world(location.getWorld());
				if (!boardLocation.equals(this.currentLocation)) {
					boolean worldChanged = this.currentLocation == null || !Objects.equals(boardLocation.getWorld(), this.currentLocation.getWorld());
					this.currentLocation = boardLocation;
					if (worldChanged) {
						onWorldLocationChange();
					}
					onBoardLocationChange();
				}
				onLocationChange(location);
				if (location.getXPos() != null && location.getYPos() != null) {
					synchronized (uiCanvas) {
						this.uiCanvas.setHighlights(List.of(location));
						this.uiCanvas.repaint();
					}
				}
				if (DEBUG) {
					if (location.getProgram() != null) {
						System.out.println(location.getProgram());
					}
				}
			} else {
				onWorldLocationChange();
				onBoardLocationChange();
				this.currentLocation = null;
			}
		}

		void reload() {
			if (this.world != null) {
				if (this.linterCheck == null || this.linterCheck.getWorld() != this.world) {
					this.linterCheck = new LinterCheck(this.world);
				}
			} else {
				this.linterCheck = null;
			}

			this.uiLocationTree.setModel(buildLocationTreeModel());
			Object root = this.uiLocationTree.getModel().getRoot();
			if (root != null) {
				for (int i = 0; i < this.uiLocationTree.getModel().getChildCount(root); i++) {
					Object child = this.uiLocationTree.getModel().getChild(root, i);
					/* if (i == this.world.getCurrentBoard()) {
						this.uiLocationTree.expandPath(new TreePath(child));
					} else { */
						this.uiLocationTree.collapsePath(new TreePath(child));
					// }
				}
				// changeLocationTo(ElementLocation.board(this.world, this.world.getCurrentBoard()));
				changeLocationTo(ElementLocation.world(this.world));
			} else {
				changeLocationTo(null);
			}
			this.uiFlagsTree.update(this.linterCheck == null ? null : this.linterCheck.getFlags().getFlags().stream());
		}

		public boolean redraw() {
			synchronized (uiCanvas) {
				if (uiCanvas.getHighlights() != null && !uiCanvas.getHighlights().isEmpty()) {
					uiCanvas.repaint();
					return true;
				}
			}
			return false;
		}
	}

	private final ZxtExtensionParser zxtParser = new ZxtExtensionParser();
	private final JMenu fileMenu;
	private final JMenuItem openWorldItem, compareWorldItem;

	private final WorldHolder world, world2;
	private boolean appendedCompareView;

	public WorldCheckFrontendSwing() {
		super("world checker");

		this.world = new WorldHolder();
		this.world2 = new WorldHolder();

		this.menuBar.add(this.fileMenu = new JMenu("File"));
		this.fileMenu.add(this.openWorldItem = new JMenuItem("Open"));
		this.fileMenu.add(this.compareWorldItem = new JMenuItem("Compare with..."));

		this.openWorldItem.addActionListener(this::onOpenWorld);
		this.openWorldItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));

		this.compareWorldItem.addActionListener(this::onOpenSecondWorld);

		addHelpMenu();

		{
			world.uiTabListPane.setTabPlacement(JTabbedPane.LEFT);
			GridBagConstraints gbc = new GridBagConstraints();

			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.fill = GridBagConstraints.BOTH;

			gbc.gridy = 0;
			gbc.gridx = 0;
			gbc.weightx = 0.4f;
			this.mainPanel.add(this.world.uiLocationTreePane, gbc);
			gbc.gridx = 1;
			gbc.weightx = 0.6f;
			this.mainPanel.add(this.world.uiCanvas, gbc);

			gbc.gridy = 1;
			gbc.gridheight = GridBagConstraints.REMAINDER;
			gbc.weighty = 1.0;
			gbc.gridx = 0;
/*			gbc.weightx = 0.4f;
			this.mainPanel.add(this.world.uiInformationTreePane, gbc);
			gbc.gridx = 1;
			gbc.weightx = 0.6f; */
			gbc.weightx = 1.0f;
			gbc.gridwidth = 2;
			this.mainPanel.add(this.world.uiTabListPane, gbc);
		}

		reloadFields();

		finishWindowInit();

		new Timer(100, e -> {
			boolean syncNeeded = world.redraw();
			if (appendedCompareView) syncNeeded |= world2.redraw();
			if (syncNeeded) {
				TOOLKIT.sync();
			}
		}).start();
	}

	protected void appendCompareView() {
		if (!appendedCompareView) {
			world.uiTabListPane.setTabPlacement(JTabbedPane.LEFT);
			world2.uiTabListPane.setTabPlacement(JTabbedPane.RIGHT);
			GridBagConstraints gbc = new GridBagConstraints();

			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.fill = GridBagConstraints.BOTH;

			gbc.gridy = 0;
			gbc.gridx = 3;
			gbc.weightx = 0.4f;
			this.mainPanel.add(this.world2.uiLocationTreePane, gbc);
			gbc.gridx = 2;
			gbc.weightx = 0.6f;
			this.mainPanel.add(this.world2.uiCanvas, gbc);

			gbc.gridy = 1;
			gbc.gridheight = GridBagConstraints.REMAINDER;
			gbc.weighty = 1.0;
/* 			gbc.gridx = 3;
			gbc.weightx = 0.4f;
			this.mainPanel.add(this.world2.uiInformationTreePane, gbc);
			gbc.gridx = 2;
			gbc.weightx = 0.6f; */
			gbc.weightx = 1.0f;
			gbc.gridwidth = 2;
			gbc.gridx = 2;
			this.mainPanel.add(this.world2.uiTabListPane, gbc);

			this.window.pack();
			this.window.setMinimumSize(this.window.getSize());

			appendedCompareView = true;
		}
	}

	protected void reloadFields() {
		this.compareWorldItem.setEnabled(this.world.getWorld() != null);
		if (this.world2.getWorld() != null) {
			appendCompareView();
		}

		this.world.reload();
		if (appendedCompareView) {
			this.world2.reload();
		}
	}

	public void onOpenWorld(ActionEvent event) {
		world.clearWorld();
		world2.clearWorld();

		File file = showLoadDialog("world",
				new FileNameExtensionFilter("ZZT World", "zzt"),
				/* new FileNameExtensionFilter("Super ZZT World", "szt"), TODO */
				new FileNameExtensionFilter("ZXT World", "zxt")
		);
		if (file != null) {
			try {
				world.read(file);
				if (world.isShowZxtWarning()) {
					JOptionPane.showMessageDialog(this.window, "Warning - unsupported ZXT extensions recommended in editor!");
				}
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this.window, "Error opening file: " + e.getMessage());
			}
		}

		reloadFields();
	}

	public void onOpenSecondWorld(ActionEvent event) {
		world2.clearWorld();

		File file = showLoadDialog("world",
				new FileNameExtensionFilter("ZZT World", "zzt"),
				/* new FileNameExtensionFilter("Super ZZT World", "szt"), TODO */
				new FileNameExtensionFilter("ZXT World", "zxt")
		);
		if (file != null) {
			try {
				world2.read(file);
				if (world2.isShowZxtWarning()) {
					JOptionPane.showMessageDialog(this.window, "Warning - unsupported ZXT extensions recommended in editor!");
				}
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this.window, "Error opening file: " + e.getMessage());
			}
		}

		reloadFields();
	}
}
