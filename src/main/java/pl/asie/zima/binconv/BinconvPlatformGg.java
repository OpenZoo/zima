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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BinconvPlatformGg extends BinconvPlatformTinyzooGbBased {
	private final byte[] baseImage;
	public BinconvPlatformGg(BinconvArgs args) throws IOException {
		try (FileInputStream fis = new FileInputStream(args.getEngineFile())) {
			this.baseImage = FileUtils.readAll(fis);
		}

		if (this.baseImage.length < 32768 || (this.baseImage.length % 16384) != 0) {
			throw new RuntimeException("Invalid base image size!");
		}
	}

	@Override
	public int getTextWindowWidth() {
		return 19; // TODO
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
		// write data
		stream.write(this.baseImage);
		serializer.writeBankData(stream);
	}
}
