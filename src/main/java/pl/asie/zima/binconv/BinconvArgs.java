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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Parameters(commandDescription = "Convert ZZT worlds to the TinyZoo format.")
public class BinconvArgs {
	@Parameter(names = {"--verbose"}, description = "Verbose output")
	private boolean verbose = false;

	@Parameter(names = {"-p", "--platform"}, description = "Platform name", required = true)
	private String platform;

	@Parameter(names = {"-e", "--engine"}, description = "Engine file", required = true)
	private String engineFile;

	@Parameter(names = {"-o", "--output"}, description = "Output file", required = true)
	private String output;

	@Parameter(description = "Input files", required = true)
	private List<String> files = new ArrayList<>();
}
