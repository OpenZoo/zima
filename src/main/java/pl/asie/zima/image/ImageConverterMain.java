/**
 * Copyright (c) 2020, 2021 Adrian Siekierka
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
package pl.asie.zima.image;

import pl.asie.zima.Constants;
import pl.asie.zima.Version;
import pl.asie.zima.util.FileUtils;
import pl.asie.zima.image.gui.ZimaChangelogWindow;
import pl.asie.zima.image.gui.ZimaFrontendSwing;

import java.util.List;
import java.util.Objects;

public class ImageConverterMain {
	public static void main(String[] args) throws Exception {
		ZimaFrontendSwing frontend = new ZimaFrontendSwing(
				FileUtils.readAll(Objects.requireNonNull(ImageConverterMain.class.getClassLoader().getResourceAsStream("8x14.bin"))),
				Constants.EGA_PALETTE,
				Version.getCurrent()
		);
	}
}
