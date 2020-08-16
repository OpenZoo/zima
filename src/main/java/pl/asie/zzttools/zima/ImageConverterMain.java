/**
 * Copyright (c) 2020 Adrian Siekierka
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
package pl.asie.zzttools.zima;

import pl.asie.zzttools.Constants;
import pl.asie.zzttools.util.FileUtils;
import pl.asie.zzttools.zima.gui.ZimaChangelogWindow;
import pl.asie.zzttools.zima.gui.ZimaFrontendSwing;

import java.util.List;
import java.util.Objects;

public class ImageConverterMain {
	public static void main(String[] args) throws Exception {
		List<String> versions = ZimaChangelogWindow.getVersions();
		ZimaFrontendSwing frontend = new ZimaFrontendSwing(
				FileUtils.readAll(Objects.requireNonNull(ImageConverterMain.class.getClassLoader().getResourceAsStream("8x14.bin"))),
				Constants.EGA_PALETTE,
				versions.get(versions.size() - 1)
		);
	}
}
