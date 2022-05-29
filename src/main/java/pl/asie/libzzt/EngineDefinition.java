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
import pl.asie.libzzt.oop.OopParserConfiguration;
import pl.asie.zima.Constants;

@Getter
@Builder(toBuilder = true)
public class EngineDefinition {
	public static final EngineDefinition ZZT = EngineDefinition.builder()
			.baseKind(EngineBaseKind.ZZT)
			.boardWidth(60).boardHeight(25)
			.maxBoardSize(20000)
			.maxStatCount(150 + 1)
			.elements(ElementLibraryZZT.INSTANCE)
			.oopParserConfiguration(OopParserConfiguration.ZZT)
			.build();
	public static final EngineDefinition SUPER_ZZT = EngineDefinition.builder()
			.baseKind(EngineBaseKind.SUPER_ZZT)
			.boardWidth(96).boardHeight(80)
			.maxBoardSize(20000)
			.maxStatCount(128 + 1)
			.elements(ElementLibrarySuperZZT.INSTANCE)
			.oopParserConfiguration(OopParserConfiguration.ZZT)
			.build();

	public static final EngineDefinition CLASSICZOO = ZZT.toBuilder()
			.maxBoardSize(65500)
			.build();
	public static final EngineDefinition SUPER_CLASSICZOO = SUPER_ZZT.toBuilder()
			.maxBoardSize(65500)
			.build();

	private final EngineBaseKind baseKind;
	private final int boardWidth;
	private final int boardHeight;
	private final int maxBoardSize;
	/**
	 * This is the "actual" maximum stat count - for a ZZT fork codebase,
	 * this would be MAX_STAT + 1.
	 */
	private final int maxStatCount;
	private final ElementLibrary elements;
	private final OopParserConfiguration oopParserConfiguration;
	private final boolean blinkingDisabled;
	private final int[] customPalette;
}
