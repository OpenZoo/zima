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

import pl.asie.libzxt.ZxtExtensionBlock;
import pl.asie.libzxt.ZxtExtensionId;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ZxtHeaderTableModel extends AbstractTableModel {
    private static final String[] columnNames = new String[] {
            "ID",
            "Name",
            "PaM",
            "ReM",
            "WrM",
            "PlS",
            "PlM",
            "EdS",
            "PrS",
            "VaB",
            "Data length"
    };

    private static final String[] columnTooltips = new String[] {
            null,
            null,
            "Required for parsing (parsing_must)",
            "Required for reading (reading_must)",
            "Required for writing (writing_must)",
            "Recommended for playing (playing_should)",
            "Required for playing (playing_must)",
            "Recommended for editing (editing_should)",
            "Recommended to preserve on resave (preserve_should)",
            "Supported by unmodified ZZT (vanilla_behavior)",
            null
    };

    private static final Class[] columnClasses = new Class[] {
            String.class,
            String.class,
            Boolean.class,
            Boolean.class,
            Boolean.class,
            Boolean.class,
            Boolean.class,
            Boolean.class,
            Boolean.class,
            Boolean.class,
            Integer.class
    };

    private List<ZxtExtensionBlock> blocks;

    ZxtHeaderTableModel() {
        this.blocks = new ArrayList<>();
    }

    public List<ZxtExtensionBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<ZxtExtensionBlock> blocks) {
        this.blocks = blocks;
    }

    @Override
    public int getRowCount() {
        return blocks.size();
    }

    @Override
    public int getColumnCount() {
        return 11;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ZxtExtensionBlock block = blocks.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return block.getId().toString();
            case 1:
                return "";
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                int flagOffset = 1 << (columnIndex - 2);
                return (block.getFlags() & flagOffset) != 0;
            case 10:
                return block.getData() == null ? 0 : block.getData().length;
            default:
                return "?";
        }
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public String getColumnToolTip(int column) {
        return columnTooltips[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return columnClasses[column];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0 || (columnIndex >= 2 && columnIndex <= 9);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (aValue == null) return;

        ZxtExtensionBlock block = blocks.get(rowIndex);

        switch (columnIndex) {
            case 0: {
                try {
                    String[] parts = ((String) aValue).split(":");
                    if (parts.length == 2) {
                        int owner = Integer.parseInt(parts[0], 16);
                        int selector = Integer.parseInt(parts[1], 16);
                        blocks.set(rowIndex, block.withId(new ZxtExtensionId(owner, (short) selector)));
                    }
                } catch (Exception e) {
                    // pass
                }
            } break;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9: {
                int flagOffset = 1 << (columnIndex - 2);
                int newFlags = block.getFlags();
                if ((Boolean) aValue) {
                    newFlags |= flagOffset;
                } else {
                    newFlags &= ~flagOffset;
                }
                blocks.set(rowIndex, block.withFlags((short) newFlags));
            } break;
        }

        fireTableCellUpdated(rowIndex, columnIndex);
    }
}
