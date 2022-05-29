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
package pl.asie.gbzooconv2;

import lombok.Builder;
import pl.asie.gbzooconv2.exceptions.BinarySerializerException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Builder
public class BankingBinarySerializer {
	private static class Collector extends BaseBinarySerializerOutput {
		private final Map<Integer, BinarySerializable> farPointerLocations = new HashMap<>();

		@Override
		public byte[] toByteArray() {
			if (!farPointerLocations.isEmpty()) {
				throw new RuntimeException("Uncommitted far pointers (" + farPointerLocations.size() + ")!");
			}
			return super.toByteArray();
		}

		@Override
		public int getFarPointerSize() {
			return 3;
		}

		@Override
		public void writeFarPointerTo(BinarySerializable object) throws IOException, BinarySerializerException {
			farPointerLocations.put(bytes.size(), object);
			bytes.write(0);
			bytes.write(0);
			bytes.write(0);
		}

		private void commitFarPointer(BinarySerializable object, int bank, int position) {
			var it = farPointerLocations.entrySet().iterator();
			while (it.hasNext()) {
				var entry = it.next();
				if (Objects.equals(entry.getValue(), object)) {
					bytes.writeAt(entry.getKey(), position & 0xFF);
					bytes.writeAt(entry.getKey() + 1, position >> 8);
					bytes.writeAt(entry.getKey() + 2, bank);
					it.remove();
				}
			}
		}
	}

	private final int firstBankIndex;
	private final int maxBanks;
	private final int bankRegionOffset;
	private final int bankSizeBytes;
	private final boolean padToPowerOfTwo;
	private final Map<BinarySerializable, Collector> collectorMap = new HashMap<>();
	private BinarySerializable firstObject;
	private final Map<Integer, List<BinarySerializable>> objectsPerBank = new HashMap<>();

	private int getObjectSize(BinarySerializable object) {
		return collectorMap.get(object).size();
	}

	private int getUsedSpace(List<BinarySerializable> list) {
		return list != null ? list.stream().mapToInt(this::getObjectSize).sum() : 0;
	}

	private int getUsedSpace(int bank) {
		return getUsedSpace(objectsPerBank.get(bank));
	}

	private int getFreeSpace(int bank) {
		return bankSizeBytes - getUsedSpace(bank);
	}

	private int addToBank(int bank, BinarySerializable object) {
		if (getFreeSpace(bank) < getObjectSize(object)) {
			throw new RuntimeException("Not enough free space in bank " + bank + " for object! (" + getFreeSpace(bank) + " < " + getObjectSize(object) + ")");
		}
		int location = getUsedSpace(bank);
		objectsPerBank.computeIfAbsent(bank, k -> new ArrayList<>()).add(object);
		for (Collector c : collectorMap.values()) {
			c.commitFarPointer(object, bank, this.bankRegionOffset + location);
		}
		return location;
	}

	public void serialize(BinarySerializable object) throws IOException, BinarySerializerException {
		if (!objectsPerBank.isEmpty()) {
			throw new RuntimeException("Already packed!");
		}

		if (!collectorMap.containsKey(object)) {
			System.out.println("serializing " + object.toString());
			Collector collector = new Collector();
			object.serialize(collector);
			if (this.firstObject == null) {
				this.firstObject = object;
			}
			collectorMap.put(object, collector);
			for (BinarySerializable child : collector.farPointerLocations.values()) {
				serialize(child);
			}
		}
	}

	public void pack() {
		if (!objectsPerBank.isEmpty()) {
			return;
		}

		List<BinarySerializable> objectsToAdd = new ArrayList<>();
		for (BinarySerializable object : this.collectorMap.keySet()) {
			if (!Objects.equals(object, this.firstObject)) {
				objectsToAdd.add(object);
			}
		}
		objectsToAdd.sort(Comparator.comparingInt(a -> -getObjectSize(a)));

		// serialize first object
		addToBank(this.firstBankIndex, this.firstObject);

		// serialize remaining objects
		for (BinarySerializable object : objectsToAdd) {
			int objectSize = getObjectSize(object);

			for (int i = this.firstBankIndex; i < maxBanks; i++) {
				if (getFreeSpace(i) >= objectSize) {
					addToBank(i, object);
					break;
				}
			}
		}
	}

	public void writeBankData(OutputStream stream) throws IOException, BinarySerializerException {
		int maximumBank = this.objectsPerBank.keySet().stream().mapToInt(a -> a).max().orElse(this.firstBankIndex - 1);
		if (padToPowerOfTwo) {
			maximumBank |= (maximumBank >> 1);
			maximumBank |= (maximumBank >> 2);
			maximumBank |= (maximumBank >> 4);
			maximumBank |= (maximumBank >> 8);
		}
		for (int i = firstBankIndex; i <= maximumBank; i++) {
			List<BinarySerializable> list = this.objectsPerBank.get(i);
			int pos = 0;
			if (list != null) {
				for (BinarySerializable obj : list) {
					byte[] data = this.collectorMap.get(obj).toByteArray();
					stream.write(data);
					pos += data.length;
				}
			}
			while (pos < bankSizeBytes) {
				stream.write(0);
				pos++;
			}
		}
	}
}
