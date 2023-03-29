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
package pl.asie.zima.worldcheck.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import pl.asie.libzzt.World;
import pl.asie.zima.CliPlugin;
import pl.asie.zima.worldcheck.LinterCheck;
import pl.asie.zima.worldcheck.LinterMessage;
import pl.asie.zima.worldcheck.LinterMessageType;
import pl.asie.zima.worldcheck.LinterWorldHolder;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class LinterCliPlugin extends CliPlugin {
	@Parameters(commandDescription = "Check files using the linter.")
	public static class CheckArgs {

		@Parameter(names = "--exclude-type", description = "Exclude the given check types.")
		private List<String> excludeType = new ArrayList<>();

		@Parameter(names = "--include-type", description = "Include only the given check type.")
		private List<String> includeType = new ArrayList<>();

		@Parameter(description = "Input files", required = true)
		private List<String> files = new ArrayList<>();
	}

	@Parameters(commandDescription = "List linter check types.")
	public static class ListTypesArgs {
	}

	@Override
	public String getName() {
		return "linter";
	}

	@Override
	public void run(String[] args) {
		CheckArgs checkArgs = new CheckArgs();
		ListTypesArgs listTypesArgs = new ListTypesArgs();

		JCommander jc = JCommander.newBuilder()
				.addCommand("check", checkArgs)
				.addCommand("list-types", listTypesArgs)
				.build();
		try {
			jc.parse(args);
			if (jc.getParsedCommand() == null || jc.getParsedCommand().isEmpty()) {
				throw new ParameterException("");
			}
		} catch (ParameterException p) {
			if (jc.getParsedCommand() == null || jc.getParsedCommand().isEmpty()) {
				System.err.println("Available commands:");
				System.err.println("\t- check: Check files using the linter. ");
				System.err.println("\t- list-types: List available linter check types. ");
			} else {
				jc.getCommands().get(jc.getParsedCommand()).usage();
			}
			return;
		}

		switch (jc.getParsedCommand()) {
			case "check" -> {
				try {
					for (String file : checkArgs.files) {
						LinterWorldHolder holder = new LinterWorldHolder();
						holder.read(new File(file));
						Stream<LinterMessage> stream = holder.getLinterCheck().getMessagesOnePerCommand().stream();
						if (!checkArgs.includeType.isEmpty()) {
							stream = stream.filter(f -> checkArgs.includeType.contains(f.getType().name()));
						}
						if (!checkArgs.excludeType.isEmpty()) {
							stream = stream.filter(f -> !checkArgs.excludeType.contains(f.getType().name()));
						}
						stream.forEach(message -> {
							System.out.println(message.toString(true));
						});
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			case "list-types" -> {
				for (LinterMessageType type : LinterMessageType.values()) {
					System.out.println("- " + type.name() + " (severity " + type.getSeverity().name() + ")");
				}
			}
			default -> {
				System.err.println("Unknown command: " + jc.getParsedCommand());
			}
		}
	}
}
