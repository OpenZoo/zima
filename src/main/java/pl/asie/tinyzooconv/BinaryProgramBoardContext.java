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
package pl.asie.tinyzooconv;

import lombok.Getter;
import pl.asie.tinyzooconv.exceptions.IdNotFoundException;
import pl.asie.tinyzooconv.exceptions.TooManyIdsException;
import pl.asie.libzzt.Stat;
import pl.asie.libzzt.oop.OopProgram;
import pl.asie.libzzt.oop.OopUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BinaryProgramBoardContext {
	private static final Map<String, Integer> SPECIAL_LABELS = Map.of(
			"RESTART", 254,
			"SHOT", 253,
			"ENERGIZE", 252,
			"THUD", 251,
			"TOUCH", 250,
			"BOMBED", 249
	);
	private static final Map<String, Integer> SPECIAL_NAMES = Map.of(
			"ALL", 254,
			"OTHERS", 253,
			"SELF", 252,
			"", 251
	);
	private static final int MAX_LABELS = 255 - SPECIAL_LABELS.size();
	private static final int MAX_NAMES = 255 - SPECIAL_NAMES.size();

	@Getter
	private final BinaryBoard parent;
	private final List<String> allLabels = new ArrayList<>();
	private final List<String> allNames = new ArrayList<>();

	public BinaryProgramBoardContext(BinaryBoard parent) throws TooManyIdsException {
		this.parent = parent;

		for (Stat s : this.parent.board.getStats()) {
			OopProgram program = s.getCode(this.parent.board.getEngineDefinition());
			if (program != null) {
				if (program.getName() != null && !program.getName().isEmpty() && !allNames.contains(program.getName())) {
					allNames.add(program.getName());
				}

				OopUtils.allChildren(program.getCommands().stream()).flatMap(c -> c.getLabels().stream()).map(OopUtils::asToken)
						.filter(label -> !SPECIAL_LABELS.containsKey(label) && !allLabels.contains(label)).forEach(allLabels::add);
			}
		}

		BinaryUtil.validateIdCount("program name", MAX_NAMES, allNames.size());
		BinaryUtil.validateIdCount("label", MAX_LABELS, allLabels.size());
	}

	public int getFlagId(String name) throws IdNotFoundException {
		return parent.parent.getFlagId(name);
	}

	public int getNameId(String name) throws IdNotFoundException {
		return BinaryUtil.getId("program name", name, allNames, SPECIAL_NAMES, 255);
	}

	public int getLabelId(String label) throws IdNotFoundException {
		return BinaryUtil.getId("label", label, allLabels, SPECIAL_LABELS, 255);
	}
}
