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

import java.io.IOException;

public final class BinconvPlatformFactory {
	private BinconvPlatformFactory() {

	}

	public static BinconvPlatform create(BinconvArgs args) throws IOException {
		if ("gb".equals(args.getPlatform())) {
			return new BinconvPlatformGb(args);
		} else if ("gg".equals(args.getPlatform())) {
			return new BinconvPlatformGg(args);
		} else if ("ws".equals(args.getPlatform())) {
			return new BinconvPlatformWs(args);
		} else {
			throw new RuntimeException("Unknown platform: " + args.getPlatform());
		}
	}
}
