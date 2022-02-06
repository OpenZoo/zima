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
package pl.asie.libzzt.oop;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import pl.asie.libzzt.oop.commands.OopCommand;
import pl.asie.libzzt.oop.commands.OopCommandBind;
import pl.asie.libzzt.oop.commands.OopCommandChar;
import pl.asie.libzzt.oop.commands.OopCommandClear;
import pl.asie.libzzt.oop.commands.OopCommandComment;
import pl.asie.libzzt.oop.commands.OopCommandCycle;
import pl.asie.libzzt.oop.commands.OopCommandDie;
import pl.asie.libzzt.oop.commands.OopCommandDirection;
import pl.asie.libzzt.oop.commands.OopCommandDirectionTry;
import pl.asie.libzzt.oop.commands.OopCommandEnd;
import pl.asie.libzzt.oop.commands.OopCommandEndgame;
import pl.asie.libzzt.oop.commands.OopCommandGo;
import pl.asie.libzzt.oop.commands.OopCommandIdle;
import pl.asie.libzzt.oop.commands.OopCommandLabel;
import pl.asie.libzzt.oop.commands.OopCommandLock;
import pl.asie.libzzt.oop.commands.OopCommandPlay;
import pl.asie.libzzt.oop.commands.OopCommandRestart;
import pl.asie.libzzt.oop.commands.OopCommandRestore;
import pl.asie.libzzt.oop.commands.OopCommandSend;
import pl.asie.libzzt.oop.commands.OopCommandSet;
import pl.asie.libzzt.oop.commands.OopCommandShoot;
import pl.asie.libzzt.oop.commands.OopCommandTextLine;
import pl.asie.libzzt.oop.commands.OopCommandThrowstar;
import pl.asie.libzzt.oop.commands.OopCommandTry;
import pl.asie.libzzt.oop.commands.OopCommandUnlock;
import pl.asie.libzzt.oop.commands.OopCommandWalk;
import pl.asie.libzzt.oop.commands.OopCommandZap;
import pl.asie.libzzt.oop.directions.OopDirection;
import pl.asie.libzzt.oop.directions.OopDirectionCcw;
import pl.asie.libzzt.oop.directions.OopDirectionCw;
import pl.asie.libzzt.oop.directions.OopDirectionEast;
import pl.asie.libzzt.oop.directions.OopDirectionFlow;
import pl.asie.libzzt.oop.directions.OopDirectionIdle;
import pl.asie.libzzt.oop.directions.OopDirectionNorth;
import pl.asie.libzzt.oop.directions.OopDirectionOpp;
import pl.asie.libzzt.oop.directions.OopDirectionRndp;
import pl.asie.libzzt.oop.directions.OopDirectionSeek;
import pl.asie.libzzt.oop.directions.OopDirectionSouth;
import pl.asie.libzzt.oop.directions.OopDirectionWest;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@EqualsAndHashCode
@ToString
public class OopProgram {
	@Getter
	String windowName;
	@Getter
	String name;
	@Getter
	List<OopCommand> commands = new ArrayList<>();

	public OopProgram(String data) throws OopParseException {
		OopProgramParser parser = new OopProgramParser();
		parser.parse(this, data);
	}
}
