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
package pl.asie.zima.binconv.cli;

import com.beust.jcommander.JCommander;
import pl.asie.libzxt.ZxtFlag;
import pl.asie.libzxt.zzt.ZxtReader;
import pl.asie.libzxt.zzt.ZxtWorld;
import pl.asie.libzzt.EngineDefinition;
import pl.asie.tinyzooconv.BinarySerializer;
import pl.asie.tinyzooconv.BinaryWorldPack;
import pl.asie.zima.CliPlugin;
import pl.asie.zima.binconv.BinconvArgs;
import pl.asie.zima.binconv.BinconvGlobalConfig;
import pl.asie.zima.binconv.BinconvPlatform;
import pl.asie.zima.binconv.BinconvPlatformGb;

import java.io.File;
import java.io.FileOutputStream;

public class BinconvCliPlugin extends CliPlugin {

	@Override
	public String getName() {
		return "binconv";
	}

	@Override
	public void run(String[] argsStr) {
		BinconvArgs args = new BinconvArgs();
		JCommander.newBuilder()
				.addObject(args)
				.build()
				.parse(argsStr);

		try {
			BinconvPlatform platform = null;
			if ("gb".equals(args.getPlatform())) {
				platform = new BinconvPlatformGb(args);
			} else {
				throw new RuntimeException("Unknown platform: " + args.getPlatform());
			}

			BinconvGlobalConfig.apply(args);

			long timeStart = System.currentTimeMillis();

			ZxtReader reader = platform.createZxtReader();
			BinaryWorldPack worlds = new BinaryWorldPack();
			for (String fn : args.getFiles()) {
				File file = new File(fn);
				System.err.println("Adding " + file.getName());
				ZxtWorld zw = reader.loadWorldWithExtensions(
						EngineDefinition.zzt(), file,
						ZxtFlag.READING_MUST | ZxtFlag.PLAYING_MUST
				);
				worlds.addWorld(zw.getWorld());
			}

			long timeRead = System.currentTimeMillis();
			System.err.println("Worlds added successfully! [" + (timeRead - timeStart) + " ms]");

			BinarySerializer serializer = platform.createBinarySerializer();
			serializer.serialize(worlds);
			serializer.pack();
			try (FileOutputStream fos = new FileOutputStream(args.getOutput())) {
				platform.write(fos, serializer);
			}

			long timeWrite = System.currentTimeMillis();
			System.err.println("File saved successfully! [" + (timeWrite - timeRead) + " ms]");
		} catch (Exception e) {
			throw new RuntimeException("Conversion error: " + e.getMessage(), e);
		}
	}
}
