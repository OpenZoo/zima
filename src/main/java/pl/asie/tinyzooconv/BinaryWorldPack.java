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
package pl.asie.tinyzooconv;

import pl.asie.tinyzooconv.exceptions.BinarySerializerException;
import pl.asie.libzzt.World;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BinaryWorldPack implements BinarySerializable {
	private final List<BinaryWorld> worldList = new ArrayList<>();

	public List<BinaryWorld> getWorldList() {
		return Collections.unmodifiableList(this.worldList);
	}

	public void addWorld(World world) throws BinarySerializerException {
		int worldId = worldList.size();
		try {
			BinaryWorld binaryWorld = new BinaryWorld(world);
			worldList.add(binaryWorld);
		} catch (BinarySerializerException e) {
			throw new BinarySerializerException("world " + worldId, e);
		}
	}

	@Override
	public void serialize(BinarySerializerOutput output) throws IOException, BinarySerializerException {
		for (BinaryWorld world : worldList) {
			output.writeFarPointerTo(world);
		}
	}
}
