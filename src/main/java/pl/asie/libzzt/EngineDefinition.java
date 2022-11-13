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
package pl.asie.libzzt;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pl.asie.libzzt.oop.OopParserConfiguration;
import pl.asie.zima.Constants;

import java.util.*;

@Getter
@Setter
@RequiredArgsConstructor
public class EngineDefinition {
	private final EngineBaseKind baseKind;
	private int boardWidth;
	private int boardHeight;
	private int maxBoardSize;
	/**
	 * This is the "actual" maximum stat count - for a ZZT fork codebase,
	 * this would be MAX_STAT + 1.
	 */
	private int maxStatCount;
	private ElementLibrary elements;
	private OopParserConfiguration oopParserConfiguration;
	private boolean blinkingDisabled;
	private int[] customPalette;
	private final Map<String, Object> quirks = new HashMap<>();

	public void setQuirk(String name, Object value) {
		quirks.put(name, value);
	}

	public void addQuirk(String name) {
		quirks.put(name, true);
	}

	public Set<String> getQuirkSet() {
		return quirks.keySet();
	}

	public boolean hasQuirk(String name) {
		return quirks.containsKey(name);
	}

	public Object getQuirkValue(String name) {
		return quirks.get(name);
	}

	public static EngineDefinition zzt() {
		EngineDefinition def = new EngineDefinition(EngineBaseKind.ZZT);
		def.setBoardWidth(60);
		def.setBoardHeight(25);
		def.setMaxBoardSize(20000);
		def.setMaxStatCount(150 + 1);
		def.setElements(ElementLibraryZZT.INSTANCE);
		def.setOopParserConfiguration(OopParserConfiguration.buildZztParser());
		return def;
	}

	public static EngineDefinition superZzt() {
		EngineDefinition def = new EngineDefinition(EngineBaseKind.SUPER_ZZT);
		def.setBoardWidth(96);
		def.setBoardHeight(80);
		def.setMaxBoardSize(20000);
		def.setMaxStatCount(128 + 1);
		def.setElements(ElementLibrarySuperZZT.INSTANCE);
		def.setOopParserConfiguration(OopParserConfiguration.buildZztParser()); // TODO: Add Super ZZT features
		return def;
	}

	public static EngineDefinition classicZoo() {
		EngineDefinition def = zzt();
		def.setMaxBoardSize(65500);
		return def;
	}

	public static EngineDefinition superClassicZoo() {
		EngineDefinition def = superZzt();
		def.setMaxBoardSize(65500);
		return def;
	}
}
