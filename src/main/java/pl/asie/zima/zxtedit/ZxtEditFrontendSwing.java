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
package pl.asie.zima.zxtedit;

import com.google.common.collect.Lists;
import pl.asie.libzxt.*;
import pl.asie.zima.gui.BaseFrontendSwing;
import pl.asie.zima.util.FileUtils;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class ZxtEditFrontendSwing extends BaseFrontendSwing {
	private final ZxtExtensionParser zxtParser = new ZxtExtensionParser();

	private final JMenu fileMenu;
	private final JMenuItem newItem, newSuperItem, openItem, saveWorldItem, saveHeaderItem;

	private ZxtHeaderTableModel headerTableModel;
	private JTable headerTable;
	private JScrollPane headerTableScrollPane;

	private JButton addButton;
	private final JLabel statusLabel;

	private byte[] remainingData = null;
	private ZxtExtensionHeader currentHeader;
	private boolean currentHeaderDirty = false;

	public ZxtEditFrontendSwing() {
		super("zxt editor (wip)");

		this.menuBar.add(this.fileMenu = new JMenu("File"));
		this.fileMenu.add(this.newItem = new JMenuItem("New ZZT header"));
		this.fileMenu.add(this.newSuperItem = new JMenuItem("New Super ZZT header"));
		this.fileMenu.add(this.openItem = new JMenuItem("Open file"));
		this.fileMenu.add(this.saveWorldItem = new JMenuItem("Save world"));
		this.fileMenu.add(this.saveHeaderItem = new JMenuItem("Save .ZAX header"));

		addHelpMenu();

		this.newItem.addActionListener(ev -> onNew(ev, ZxtHeaderType.ZZT_WORLD));
		this.newSuperItem.addActionListener(ev -> onNew(ev, ZxtHeaderType.SUPER_ZZT_WORLD));
		this.openItem.addActionListener(this::onOpen);
		this.saveWorldItem.addActionListener(this::onSaveWorld);
		this.saveHeaderItem.addActionListener(this::onSaveHeader);

		this.openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		this.saveWorldItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		this.saveHeaderItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));

		this.headerTableModel = new ZxtHeaderTableModel();
		this.headerTableModel.addTableModelListener(e -> currentHeaderDirty = true);
		this.headerTable = new JTable(this.headerTableModel) {
			@Override
			protected JTableHeader createDefaultTableHeader() {
				return new JTableHeader(columnModel) {
					@Override
					public String getToolTipText(MouseEvent e) {
						int colIdx = columnModel.getColumnIndexAtX(e.getPoint().x);
						int modelColIdx = columnModel.getColumn(colIdx).getModelIndex();
						return headerTableModel.getColumnToolTip(modelColIdx);
					}
				};
			}
		};
		for (int i = 0; i < this.headerTable.getColumnCount(); i++) {
			Class<?> c = this.headerTable.getColumnClass(i);
			if (c == Boolean.class) {
				this.headerTable.getColumnModel().getColumn(i).setMinWidth(30);
				this.headerTable.getColumnModel().getColumn(i).setPreferredWidth(50);
				this.headerTable.getColumnModel().getColumn(i).setMaxWidth(50);
			} else if (c == Integer.class) {
				this.headerTable.getColumnModel().getColumn(i).setMinWidth(60);
				this.headerTable.getColumnModel().getColumn(i).setPreferredWidth(80);
				this.headerTable.getColumnModel().getColumn(i).setMaxWidth(80);
			}
		}
		this.headerTable.setVisible(false);

		this.headerTableScrollPane = new JScrollPane(this.headerTable);
		this.headerTable.setFillsViewportHeight(true);
		addGridBag(this.mainPanel, this.headerTableScrollPane, (c) -> { c.gridx = 0; c.gridy = 0; c.fill = GridBagConstraints.BOTH; c.weightx = 1.0; c.weighty = 1.0; });

		this.addButton = new JButton("Add");
		this.addButton.addActionListener(e -> onAddBlock());
		addGridBag(this.mainPanel, this.addButton, (c) -> { c.gridx = 0; c.gridy = 1; });

		this.statusLabel = new JLabel("Ready.");
		addGridBag(this.mainPanel, this.statusLabel, (c) -> { c.gridx = 0; c.gridy = 2; c.gridwidth = GridBagConstraints.REMAINDER; c.fill = GridBagConstraints.HORIZONTAL; c.anchor = GridBagConstraints.WEST; });

		copyHeaderToUi();

		finishWindowInit();
	}

	public void updateUiStatusBar() {
		if (this.currentHeader == null) {
			this.statusLabel.setText("Ready.");
			this.statusLabel.repaint();
			return;
		}

		this.statusLabel.setText(
				(this.currentHeader.getType().isSuperZzt() ? "Super ZZT" : "ZZT")
				+ (this.remainingData == null ? " Header" : " World (" + this.remainingData.length + " bytes)")
				+ "; " + this.headerTableModel.getBlocks().size() + " extension" + (this.headerTableModel.getBlocks().size() == 1 ? "" : "s") + "."
		);
		this.statusLabel.repaint();
	}

	public void onAddBlock() {
		int rowIdx = this.headerTableModel.getRowCount();
		this.headerTableModel.getBlocks().add(new ZxtExtensionBlock((short) 0, new ZxtExtensionId(0, (short) 0)));
		this.headerTableModel.fireTableRowsInserted(rowIdx, rowIdx);
		updateUiStatusBar();
	}

	public void copyHeaderToUi() {
		this.saveWorldItem.setEnabled(remainingData != null);
		this.headerTableModel.setBlocks(currentHeader != null ? Lists.newArrayList(currentHeader.getBlocks()) : new ArrayList<>());
		this.headerTableModel.fireTableStructureChanged();
		updateUiStatusBar();
	}

	public void copyUiToHeader() {
		currentHeader.clearBlocks();
		this.headerTableModel.getBlocks().forEach(b -> currentHeader.addBlock(b));
	}

	public boolean onOpen(ActionEvent event) {
		File file = showLoadDialog("world",
				new FileNameExtensionFilter("ZZT World", "zzt"),
				new FileNameExtensionFilter("Super ZZT World", "szt"),
				new FileNameExtensionFilter("ZXT World", "zxt"),
				new FileNameExtensionFilter("ZXT Header", "zax")
		);
		if (file != null) {
			try (FileInputStream fis = new FileInputStream(file); BufferedInputStream bis = new BufferedInputStream(fis)) {
				currentHeader = zxtParser.readHeader(bis);
				if (currentHeader == null) {
					fis.getChannel().position(0);
					currentHeader = new ZxtExtensionHeader(FileUtils.getExtension(file).equalsIgnoreCase("szt")
							? ZxtHeaderType.SUPER_ZZT_WORLD : ZxtHeaderType.ZZT_WORLD);
				}
				currentHeaderDirty = false;

				remainingData = bis.readAllBytes();
				if (remainingData != null && remainingData.length == 0) {
					remainingData = null;
				}

				copyHeaderToUi();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this.window, "Error opening file: " + e.getMessage());
				return false;
			}
		}
		return false;
	}

	public boolean onSaveWorld(ActionEvent event) {
		copyUiToHeader();

		File file = showSaveDialog("world", new FileNameExtensionFilter("ZXT World", "zxt"));
		if (file != null) {
			try (FileOutputStream fos = new FileOutputStream(file)) {
				zxtParser.writeHeader(currentHeader, fos);

				// fix magic byte
				// TODO: move elsewhere
				if (remainingData.length >= 2) {
					int newMagicByte;
					if (currentHeader.getBlocks().stream().anyMatch(b -> (b.getFlags() & ZxtFlag.READING_MUST) != 0)) {
						newMagicByte = 0xE227;
					} else {
						newMagicByte = currentHeader.getType().isSuperZzt() ? -2 : -1;
					}
					remainingData[0] = (byte) (newMagicByte & 0xFF);
					remainingData[1] = (byte) ((newMagicByte >> 8) & 0xFF);
				}

				fos.write(remainingData);
				currentHeaderDirty = false;
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this.window, "Error saving file: " + e.getMessage());
			}
		}
		return false;
	}

	public boolean onSaveHeader(ActionEvent event) {
		copyUiToHeader();

		File file = showSaveDialog("header", new FileNameExtensionFilter("ZXT Header", "zax"));
		if (file != null) {
			try (FileOutputStream fos = new FileOutputStream(file)) {
				zxtParser.writeHeader(currentHeader, fos);
				currentHeaderDirty = false;
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this.window, "Error saving file: " + e.getMessage());
			}
		}
		return false;
	}

	public boolean verifySave() {
		if (!currentHeaderDirty || (currentHeader == null)) return true;

		int result = JOptionPane.showConfirmDialog(this.window,
				"You have unsaved changes! Would you like to save them first?",
				"zxt editor", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		switch (result) {
			case JOptionPane.YES_OPTION:
				if (remainingData == null) {
					return onSaveHeader(null);
				} else {
					return onSaveWorld(null);
				}
			case JOptionPane.NO_OPTION:
				return true;
			default:
				return false;
		}
	}

	public void onNew(ActionEvent event, ZxtHeaderType headerType) {
		if (verifySave()) {
			currentHeader = new ZxtExtensionHeader(headerType);
			currentHeaderDirty = false;
			copyHeaderToUi();
			this.headerTable.setVisible(true);
			this.headerTable.repaint();
		}
	}
}
