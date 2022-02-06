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
package pl.asie.libzxt;

import java.util.*;
import java.util.stream.Collectors;

public class ZxtExtensionHeader {
    private final ZxtHeaderType type;
    private final Map<ZxtExtensionId, List<ZxtExtensionBlock>> blocksById;
    private final List<ZxtExtensionBlock> blocks;

    public ZxtExtensionHeader(ZxtHeaderType type) {
        this.type = type;
        this.blocksById = new HashMap<>();
        this.blocks = new ArrayList<>();
    }

    public ZxtHeaderType getType() {
        return type;
    }

    public List<ZxtExtensionBlock> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }

    public ZxtExtensionBlock getBlockAt(int i) {
        return blocks.get(i);
    }

    public int getBlockCount() {
        return blocks.size();
    }

    public int getBlockCountById(ZxtExtensionId id) {
        List<ZxtExtensionBlock> blocksLocal = blocksById.get(id);
        return blocksLocal != null ? blocksLocal.size() : 0;
    }

    public ZxtExtensionBlock getBlockById(ZxtExtensionId id, int i) {
        List<ZxtExtensionBlock> blocksLocal = blocksById.get(id);
        if (blocksLocal == null) {
            throw new IndexOutOfBoundsException(i);
        } else {
            return blocksLocal.get(i);
        }
    }

    private void recalculateBlocksById(ZxtExtensionId id) {
        blocksById.put(id, blocks.stream().filter(b -> id.equals(b.getId())).collect(Collectors.toList()));
    }

    public boolean addBlock(int i, ZxtExtensionBlock block) {
        if (block == null) {
            throw new NullPointerException();
        }
        blocks.add(i, block);
        recalculateBlocksById(block.getId());
        return true;
    }

    public boolean addBlock(ZxtExtensionBlock block) {
        if (block == null) {
            throw new NullPointerException();
        }
        blocks.add(block);
        recalculateBlocksById(block.getId());
        return true;
    }

    public ZxtExtensionBlock removeBlock(int id) {
        ZxtExtensionBlock block = blocks.remove(id);
        if (block != null) {
            recalculateBlocksById(block.getId());
            return block;
        } else {
            return null;
        }
    }

    public boolean removeBlock(ZxtExtensionBlock block) {
        if (blocks.remove(block)) {
            recalculateBlocksById(block.getId());
            return true;
        } else {
            return false;
        }
    }

    public boolean removeBlocksById(ZxtExtensionId id) {
        if (blocks.removeIf(b -> id.equals(b.getId()))) {
            recalculateBlocksById(id);
            return true;
        } else {
            return false;
        }
    }

    public void clearBlocks() {
        blocks.clear();
        blocksById.clear();
    }
}
