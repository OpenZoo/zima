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

import pl.asie.libzxt.zzt.ZxtReader;
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

public class BinconvPlatformGb extends BinconvPlatformTinyzooGbBased {
	private final byte[] baseImage;
	private final boolean isTpp1;
	private final Integer sramSize;
	private final Map<Integer, Integer> mbc5SramKbToValue = Map.of(
			8, 0x02,
			32, 0x03,
			128, 0x04,
			64, 0x05
	);
	private final Map<Integer, Integer> tpp1SramKbToValue = Map.of(
			8, 0x01,
			16, 0x02,
			32, 0x03,
			64, 0x04,
			128, 0x05,
			256, 0x06,
			512, 0x07,
			1024, 0x08,
			2048, 0x09
	);

	public BinconvPlatformGb(BinconvArgs args) throws IOException {
		try (FileInputStream fis = new FileInputStream(args.getEngineFile())) {
			this.baseImage = FileUtils.readAll(fis);
		}

		if (this.baseImage.length < 32768 || (this.baseImage.length % 16384) != 0) {
			throw new RuntimeException("Invalid base image size!");
		}
		this.isTpp1 = this.baseImage[0x147] == (byte)0xBC && this.baseImage[0x149] == (byte)0xC1 && this.baseImage[0x14A] == (byte)0x65
				&& this.baseImage[0x150] == 1 && this.baseImage[0x151] == 0;
		this.sramSize = args.getSramSize();
	}

	@Override
	public int getViewportWidth() {
		return 20;
	}

	@Override
	public int getViewportHeight() {
		return 18;
	}

	@Override
	public int getTextWindowWidth() {
		return 19;
	}

	@Override
	public BinarySerializer createBinarySerializer() {
		return BankingBinarySerializer.builder()
				.firstBankIndex(baseImage.length / 16384)
				.maxBanks(256)
				.bankRegionOffset(0x4000)
				.bankSizeBytes(16384)
				.padToPowerOfTwo(true)
				.build();
	}

	@Override
	public void write(OutputStream stream, BinarySerializer serializer) throws IOException, BinarySerializerException {
		// fix ROM size
		int bankSize = serializer.getBanksUsed() * 16384;
		int bankSizeShift = 32768;
		int bankShift = 0;
		while (bankSizeShift < bankSize) {
			bankSizeShift *= 2;
			bankShift++;
		}
		this.baseImage[0x148] = (byte) bankShift;

		// fix SRAM size
		if (this.sramSize != null) {
			Map<Integer, Integer> sramKbToValue = this.isTpp1 ? tpp1SramKbToValue : mbc5SramKbToValue;

			Integer sramByte = sramKbToValue.get(this.sramSize);
			if (sramByte == null) {
				throw new BinarySerializerException("Invalid SRAM size: " + this.sramSize + " KB. Supported values: "
						+ sramKbToValue.keySet().stream().sorted().map(Object::toString).collect(Collectors.joining(", ")));
			}
			this.baseImage[this.isTpp1 ? 0x152 : 0x149] = (byte) sramByte.intValue();
		}

		// calculate header checksum
		int headerChecksum = 0;
		for (int i = 0x134; i < 0x14D; i++) {
			headerChecksum = (headerChecksum - (((int) this.baseImage[i]) & 0xFF) - 1) & 0xFF;
		}
		this.baseImage[0x14D] = (byte) headerChecksum;

		// create ROM
		ByteArrayOutputStream baos = new ByteArrayOutputStream(bankSize);
		baos.write(this.baseImage);
		serializer.writeBankData(baos);
		byte[] fullImage = baos.toByteArray();

		// calculate global checksum
		int globalChecksum = 0;
		for (int i = 0; i < 0x14E; i++) {
			globalChecksum += ((int) fullImage[i]) & 0xFF;
		}
		for (int i = 0x150; i < fullImage.length; i++) {
			globalChecksum += ((int) fullImage[i]) & 0xFF;
		}
		fullImage[0x14E] = (byte) ((globalChecksum >> 8) & 0xFF);
		fullImage[0x14F] = (byte) (globalChecksum & 0xFF);

		// write final ROM
		stream.write(fullImage);
	}
}
