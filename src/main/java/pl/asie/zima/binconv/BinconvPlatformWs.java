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
package pl.asie.zima.binconv;

import pl.asie.tinyzooconv.BankingBinarySerializer;
import pl.asie.tinyzooconv.BinarySerializer;
import pl.asie.tinyzooconv.exceptions.BinarySerializerException;
import pl.asie.zima.util.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.stream.Collectors;

public class BinconvPlatformWs extends BinconvPlatformTinyzooGbBased {
	private final byte[] baseImage;
	private final Integer sramSize;
	private final Map<Integer, Integer> romBanksToValue = Map.of(
			2, 0x00,
			4, 0x01,
			8, 0x02,
			16, 0x03,
			32, 0x04,
			48, 0x05,
			64, 0x06,
			96, 0x07,
			128, 0x08,
			256, 0x09
	);
	private final Map<Integer, Integer> sramKbToValue = Map.of(
			32, 0x02,
			128, 0x03,
			256, 0x04,
			512, 0x05
	);

	public BinconvPlatformWs(BinconvArgs args) throws IOException {
		try (FileInputStream fis = new FileInputStream(args.getEngineFile())) {
			this.baseImage = FileUtils.readAll(fis);
		}
		this.sramSize = args.getSramSize();
	}

	@Override
	public int getViewportWidth() {
		return 28;
	}

	@Override
	public int getViewportHeight() {
		return 18;
	}

	@Override
	public int getTextWindowWidth() {
		return 27;
	}

	@Override
	public BinarySerializer createBinarySerializer() {
		return BankingBinarySerializer.builder()
				.firstBankIndex(0)
				.maxBanks(256)
				.bankRegionOffset(0x0000)
				.bankSizeBytes(65536)
				.padToPowerOfTwo(false)
				.trim(true)
				.padByte(0xFF)
				.calculateChecksum(true)
				.build();
	}

	@Override
	public void write(OutputStream stream, BinarySerializer serializer) throws IOException, BinarySerializerException {
		// write ROM
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		serializer.writeBankData(baos);
		int bankSize = baos.size() + this.baseImage.length;
		int bankSizeShift = 131072;
		while (bankSizeShift < bankSize) {
			bankSizeShift *= 2;
		}
		while (bankSize < bankSizeShift) {
			baos.write(0xFF);
			bankSize++;
		}
		baos.write(baseImage);
		byte[] fullImage = baos.toByteArray();
		int headerOffset = fullImage.length - 16;

		// fix ROM size
		if (this.sramSize != null) {
			Integer romByte = this.romBanksToValue.get(bankSizeShift >> 16);
			if (romByte == null) {
				throw new BinarySerializerException("Invalid ROM size: " + bankSizeShift + " bytes. Supported values: "
						+ this.romBanksToValue.keySet().stream().sorted().map(Object::toString).collect(Collectors.joining(", ")));
			}
			fullImage[headerOffset + 10] = (byte) romByte.intValue();
		}

		// fix SRAM size
		if (this.sramSize != null) {
			Integer sramByte = this.sramKbToValue.get(this.sramSize);
			if (sramByte == null) {
				throw new BinarySerializerException("Invalid SRAM size: " + this.sramSize + " KB. Supported values: "
						+ this.sramKbToValue.keySet().stream().sorted().map(Object::toString).collect(Collectors.joining(", ")));
			}
			fullImage[headerOffset + 11] = (byte) sramByte.intValue();
		}

		// calculate global checksum
		int globalChecksum = 0;
		for (byte b : fullImage) {
			globalChecksum += ((int) b) & 0xFF;
		}
		fullImage[fullImage.length - 2] = (byte) ((globalChecksum >> 8) & 0xFF);
		fullImage[fullImage.length - 1] = (byte) (globalChecksum & 0xFF);

		// write final ROM
		stream.write(fullImage);
	}
}
