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
package pl.asie.zima.worldcheck;

import lombok.Getter;
import pl.asie.libzxt.ZxtCannotParseException;
import pl.asie.libzxt.ZxtExtensionHeader;
import pl.asie.libzxt.ZxtExtensionParser;
import pl.asie.libzxt.ZxtFlag;
import pl.asie.zima.util.ZimaPlatform;
import pl.asie.libzzt.World;
import pl.asie.libzzt.ZInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class LinterWorldHolder {
	protected final ZxtExtensionParser zxtParser = new ZxtExtensionParser();
	@Getter
	protected ZxtExtensionHeader zxtHeader;
	@Getter
	protected World world;
	@Getter
	protected LinterCheck linterCheck;
	protected boolean showZxtWarning;

	public boolean isShowZxtWarning() {
		if (showZxtWarning) {
			showZxtWarning = false;
			return true;
		} else {
			return false;
		}
	}

	public void read(File file) throws IOException, ZxtCannotParseException {
		try (FileInputStream fis = new FileInputStream(file); BufferedInputStream bis = new BufferedInputStream(fis)) {
			showZxtWarning = false;
			zxtHeader = zxtParser.readHeader(bis);
			if (zxtHeader != null) {
				if (zxtHeader.getBlocks().stream().anyMatch(block -> (block.getFlags() & (ZxtFlag.PARSING_MUST | ZxtFlag.READING_MUST | ZxtFlag.WRITING_MUST | ZxtFlag.PLAYING_MUST)) != 0)) {
					throw new IOException("Unsupported ZXT extensions!");
				} else if (zxtHeader.getBlocks().stream().anyMatch(block -> (block.getFlags() & (ZxtFlag.EDITING_SHOULD)) != 0)) {
					showZxtWarning = true;
				}
			}

			try (ZInputStream zis = new ZInputStream(bis, ZimaPlatform.ZZT.getZztEngineDefinition())) {
				world = new World(ZimaPlatform.ZZT.getZztEngineDefinition());
				world.readZ(zis);
			}

			if (this.linterCheck == null || this.linterCheck.getWorld() != this.world) {
				this.linterCheck = new LinterCheck(this.world);
			}
		}
	}
}
