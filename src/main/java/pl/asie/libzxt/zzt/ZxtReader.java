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
package pl.asie.libzxt.zzt;

import pl.asie.libzxt.ZxtCannotParseException;
import pl.asie.libzxt.ZxtExtensionBlock;
import pl.asie.libzxt.ZxtExtensionHeader;
import pl.asie.libzxt.ZxtExtensionParser;
import pl.asie.libzxt.ZxtFlag;
import pl.asie.libzzt.EngineDefinition;
import pl.asie.libzzt.World;
import pl.asie.libzzt.ZInputStream;
import pl.asie.zima.util.FileUtils;
import pl.asie.zima.util.ZimaPlatform;
import pl.asie.zima.worldcheck.LinterCheck;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public final class ZxtReader {
	private ZxtReader() {

	}

	/**
	 * Apply the given ZXT extensions to a given engine definition.
	 *
	 * @param src The engine definition to apply extensions to.
	 * @param zxt The extension header
	 * @param requiredFlags Flags without which the function will throw.
	 * @return A mask of unsatisfied optional flags.
	 * @throws ZxtCannotApplyException An extension block with a required flag could not be applied.
	 */
	public static int applyExtensions(EngineDefinition src, ZxtExtensionHeader zxt, int requiredFlags) throws ZxtCannotApplyException {
		int unsatisfiedFlags = 0;

		for (ZxtExtensionBlock zxtBlock : zxt.getBlocks()) {
			ZxtEngineDefinitionApplier applier = ZxtAppliers.APPLIER_MAP.get(zxtBlock.getId());
			//noinspection StatementWithEmptyBody
			if (applier != null && applier.apply(src, zxtBlock)) {
				// success!
			} else {
				if ((zxtBlock.getFlags() & requiredFlags) != 0) {
					throw new ZxtCannotApplyException(zxtBlock.getId());
				} else {
					unsatisfiedFlags |= zxtBlock.getFlags();
				}
			}
		}

		return unsatisfiedFlags;
	}

	public static ZxtWorld loadWorldWithExtensions(EngineDefinition base, File file, int requiredFlags) throws IOException, ZxtCannotParseException, ZxtCannotApplyException {
		File zaxFile = FileUtils.firstExists(
				FileUtils.withExtension(file, "zax"),
				FileUtils.withExtension(file, "ZAX")
		).orElse(null);

		EngineDefinition def = base;
		ZxtExtensionParser zxtParser = new ZxtExtensionParser();
		ZxtExtensionHeader zxtHeader = null;
		World world = null;
		int zxtUnsatisfiedFlags = 0;

		if (zaxFile != null) {
			try (FileInputStream fis = new FileInputStream(zaxFile); BufferedInputStream bis = new BufferedInputStream(fis)) {
				zxtHeader = zxtParser.readHeader(bis);
			}
		}

		try (FileInputStream fis = new FileInputStream(file); BufferedInputStream bis = new BufferedInputStream(fis)) {
			if (zxtHeader == null) {
				zxtHeader = zxtParser.readHeader(bis);
			}

			if (zxtHeader != null) {
				zxtUnsatisfiedFlags = applyExtensions(def, zxtHeader, requiredFlags);
			}

			try (ZInputStream zis = new ZInputStream(bis, def)) {
				world = new World(def);
				world.readZ(zis);
			}
		}

		return new ZxtWorld(zxtHeader, zxtUnsatisfiedFlags, world);
	}
}
