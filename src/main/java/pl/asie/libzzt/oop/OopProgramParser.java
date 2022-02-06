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

import pl.asie.libzzt.Platform;
import pl.asie.libzzt.oop.commands.OopCommand;
import pl.asie.libzzt.oop.commands.OopCommandBecome;
import pl.asie.libzzt.oop.commands.OopCommandBind;
import pl.asie.libzzt.oop.commands.OopCommandChange;
import pl.asie.libzzt.oop.commands.OopCommandChar;
import pl.asie.libzzt.oop.commands.OopCommandClear;
import pl.asie.libzzt.oop.commands.OopCommandComment;
import pl.asie.libzzt.oop.commands.OopCommandCycle;
import pl.asie.libzzt.oop.commands.OopCommandDie;
import pl.asie.libzzt.oop.commands.OopCommandDirection;
import pl.asie.libzzt.oop.commands.OopCommandDirectionTry;
import pl.asie.libzzt.oop.commands.OopCommandEnd;
import pl.asie.libzzt.oop.commands.OopCommandEndgame;
import pl.asie.libzzt.oop.commands.OopCommandGive;
import pl.asie.libzzt.oop.commands.OopCommandGo;
import pl.asie.libzzt.oop.commands.OopCommandIdle;
import pl.asie.libzzt.oop.commands.OopCommandIf;
import pl.asie.libzzt.oop.commands.OopCommandLabel;
import pl.asie.libzzt.oop.commands.OopCommandLock;
import pl.asie.libzzt.oop.commands.OopCommandNull;
import pl.asie.libzzt.oop.commands.OopCommandPlay;
import pl.asie.libzzt.oop.commands.OopCommandPut;
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
import pl.asie.libzzt.oop.conditions.OopCondition;
import pl.asie.libzzt.oop.conditions.OopConditionAlligned;
import pl.asie.libzzt.oop.conditions.OopConditionAny;
import pl.asie.libzzt.oop.conditions.OopConditionBlocked;
import pl.asie.libzzt.oop.conditions.OopConditionContact;
import pl.asie.libzzt.oop.conditions.OopConditionEnergized;
import pl.asie.libzzt.oop.conditions.OopConditionFlag;
import pl.asie.libzzt.oop.conditions.OopConditionNot;
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

import java.util.Map;
import java.util.regex.Pattern;

public class OopProgramParser {

	private static final Pattern WORD_PATTERN = Pattern.compile("^[0-9A-Za-z:_]+$");

	// For parsing.
	private String data;
	private int position;
	private int oopChar;
	private String oopWord;
	private int oopValue;
	private boolean lineFinished;
	private int lastPosition;

	private void readChar() {
		if (position >= 0 && position < data.length()) {
			oopChar = data.charAt(position++);
		} else {
			oopChar = 0;
		}
	}

	private void readWord() {
		StringBuilder s = new StringBuilder();
		readChar();
		while (oopChar == ' ') {
			readChar();
		}
		oopChar = OopUtils.upCase(oopChar);
		if (oopChar < '0' || oopChar > '9') {
			while ((oopChar >= 'A' && oopChar <= 'Z') || oopChar == ':' || (oopChar >= '0' && oopChar <= '9') || oopChar == '_') {
				s.appendCodePoint(oopChar);
				readChar();
				oopChar = OopUtils.upCase(oopChar);
			}
		}
		oopWord = s.toString();
		if (position > 0) {
			position--;
		}
	}

	private void readValue() {
		StringBuilder s = new StringBuilder();
		readChar();
		while (oopChar == ' ') {
			readChar();
		}

		oopChar = OopUtils.upCase(oopChar);
		while (oopChar >= '0' && oopChar <= '9') {
			s.appendCodePoint(oopChar);
			readChar();
			oopChar = OopUtils.upCase(oopChar);
		}

		if (position > 0) {
			position--;
		}

		if (s.length() != 0) {
			oopValue = Integer.parseInt(s.toString());
		} else {
			oopValue = -1;
		}
	}

	private void skipLine() {
		readChar();
		while (oopChar != 0 && oopChar != 13) {
			readChar();
		}
	}

	private String readLineToEnd() {
		StringBuilder s = new StringBuilder();
		readChar();
		while (oopChar != 0 && oopChar != 13) {
			s.appendCodePoint(oopChar);
			readChar();
		}
		return s.toString();
	}

	private static final Map<String, Integer> COLOR_NAME_TO_COLOR = Map.of(
			"BLUE", 9,
			"GREEN", 10,
			"CYAN", 11,
			"RED", 12,
			"PURPLE", 13,
			"YELLOW", 14,
			"WHITE", 15
	);

	private OopTile parseTile() throws OopParseException {
		int color = 0;

		readWord();
		if (COLOR_NAME_TO_COLOR.containsKey(oopWord)) {
			color = COLOR_NAME_TO_COLOR.get(oopWord);
			readWord();
		}

		return new OopTile(Platform.ZZT.getLibrary().byInternalName(oopWord), color);
	}

	private OopCondition parseCondition() throws OopParseException {
		readWord();
		if ("NOT".equals(oopWord)) {
			return new OopConditionNot(parseCondition());
		} else if ("ALLIGNED".equals(oopWord)) {
			return new OopConditionAlligned();
		} else if ("CONTACT".equals(oopWord)) {
			return new OopConditionContact();
		} else if ("BLOCKED".equals(oopWord)) {
			return new OopConditionBlocked(parseDirection());
		} else if ("ENERGIZED".equals(oopWord)) {
			return new OopConditionEnergized();
		} else if ("ANY".equals(oopWord)) {
			return new OopConditionAny(parseTile());
		} else {
			return new OopConditionFlag(oopWord);
		}
	}

	private OopDirection parseDirection() throws OopParseException {
		readWord();
		if ("N".equals(oopWord) || "NORTH".equals(oopWord)) {
			return new OopDirectionNorth();
		} else if ("S".equals(oopWord) || "SOUTH".equals(oopWord)) {
			return new OopDirectionSouth();
		} else if ("E".equals(oopWord) || "EAST".equals(oopWord)) {
			return new OopDirectionEast();
		} else if ("W".equals(oopWord) || "WEST".equals(oopWord)) {
			return new OopDirectionWest();
		} else if ("I".equals(oopWord) || "IDLE".equals(oopWord)) {
			return new OopDirectionIdle();
		} else if ("SEEK".equals(oopWord)) {
			return new OopDirectionSeek();
		} else if ("FLOW".equals(oopWord)) {
			return new OopDirectionFlow();
		} else if ("RND".equals(oopWord)) {
			return new OopDirectionFlow();
		} else if ("RNDNS".equals(oopWord)) {
			return new OopDirectionFlow();
		} else if ("RNDNE".equals(oopWord)) {
			return new OopDirectionFlow();
		} else if ("CW".equals(oopWord)) {
			return new OopDirectionCw(parseDirection());
		} else if ("CCW".equals(oopWord)) {
			return new OopDirectionCcw(parseDirection());
		} else if ("RNDP".equals(oopWord)) {
			return new OopDirectionRndp(parseDirection());
		} else if ("OPP".equals(oopWord)) {
			return new OopDirectionOpp(parseDirection());
		} else {
			throw new OopParseException(this, "Invalid direction " + oopWord);
		}
	}

	private OopCommand readCommand() throws OopParseException {
		readWord();
		if ("THEN".equals(oopWord)) {
			readWord();
		}
		if (oopWord.isEmpty()) {
			return null;
		} else if ("GO".equals(oopWord)) {
			return new OopCommandGo(parseDirection());
		} else if ("TRY".equals(oopWord)) {
			return new OopCommandTry(parseDirection(), readCommand());
		} else if ("WALK".equals(oopWord)) {
			return new OopCommandWalk(parseDirection());
		} else if ("SET".equals(oopWord)) {
			readWord();
			return new OopCommandSet(oopWord);
		} else if ("CLEAR".equals(oopWord)) {
			readWord();
			return new OopCommandClear(oopWord);
		} else if ("IF".equals(oopWord)) {
			OopCondition cond = parseCondition();
			return new OopCommandIf(cond, readCommand());
		} else if ("SHOOT".equals(oopWord)) {
			return new OopCommandShoot(parseDirection());
		} else if ("THROWSTAR".equals(oopWord)) {
			return new OopCommandThrowstar(parseDirection());
		} else if ("GIVE".equals(oopWord) || "TAKE".equals(oopWord)) {
			readWord();
			try {
				OopCounterType type = OopCounterType.valueOf(oopWord);
				readValue();
				if ("TAKE".equals(oopWord)) {
					return new OopCommandGive(type, -oopValue, readCommand());
				} else {
					return new OopCommandGive(type, oopValue, readCommand());
				}
			} catch (IllegalArgumentException e) {
				throw new OopParseException(this, "Invalid type: " + oopWord);
			}
		} else if ("END".equals(oopWord)) {
			return new OopCommandEnd();
		} else if ("ENDGAME".equals(oopWord)) {
			return new OopCommandEndgame();
		} else if ("IDLE".equals(oopWord)) {
			return new OopCommandIdle();
		} else if ("RESTART".equals(oopWord)) {
			return new OopCommandRestart();
		} else if ("ZAP".equals(oopWord)) {
			readWord();
			return new OopCommandZap(new OopLabelTarget(oopWord));
		} else if ("RESTORE".equals(oopWord)) {
			readWord();
			return new OopCommandRestore(new OopLabelTarget(oopWord));
		} else if ("LOCK".equals(oopWord)) {
			return new OopCommandLock();
		} else if ("UNLOCK".equals(oopWord)) {
			return new OopCommandUnlock();
		} else if ("SEND".equals(oopWord)) {
			readWord();
			return new OopCommandSend(new OopLabelTarget(oopWord));
		} else if ("BECOME".equals(oopWord)) {
			return new OopCommandBecome(parseTile());
		} else if ("PUT".equals(oopWord)) {
			OopDirection dir = parseDirection();
			OopTile tile = parseTile();
			return new OopCommandPut(dir, tile);
		} else if ("CHANGE".equals(oopWord)) {
			OopTile tileFrom = parseTile();
			OopTile tileTo = parseTile();
			return new OopCommandChange(tileFrom, tileTo);
		} else if ("PLAY".equals(oopWord)) {
			lineFinished = false;
			return new OopCommandPlay(new OopSound(readLineToEnd()));
		} else if ("CYCLE".equals(oopWord)) {
			readValue();
			return new OopCommandCycle(oopValue);
		} else if ("CHAR".equals(oopWord)) {
			readValue();
			return new OopCommandChar(oopValue);
		} else if ("DIE".equals(oopWord)) {
			return new OopCommandDie();
		} else if ("BIND".equals(oopWord)) {
			readWord();
			return new OopCommandBind(oopWord);
		} else {
			// TODO: This doesn't handle #TEXT right if TEXT doesn't lead to a valid target.
			return new OopCommandSend(new OopLabelTarget(oopWord));
		}
	}

	private OopCommandTextLine parseTextLine(String s) {
		if (s.startsWith("$")) {
			return new OopCommandTextLine(OopCommandTextLine.Type.CENTERED, null, s.substring(1));
		} else if (s.startsWith("!")) {
			String[] split = s.substring(1).split(";", 2);
			if (split[0].startsWith("-")) {
				return new OopCommandTextLine(OopCommandTextLine.Type.EXTERNAL_HYPERLINK, split[0].substring(1), split[1]);
			} else {
				return new OopCommandTextLine(OopCommandTextLine.Type.HYPERLINK, split[0], split[1]);
			}
		} else {
			return new OopCommandTextLine(OopCommandTextLine.Type.REGULAR, null, s);
		}
	}

	public void parse(OopProgram program, String data) throws OopParseException {
		this.data = data;
		this.position = 0;

		while (position < data.length()) {
			lineFinished = true;
			lastPosition = position;

			readChar();
			if (oopChar == '@') {
				if (position <= 1) {
					readWord();
					program.name = this.oopWord;
					position = 1;
					program.windowName = readLineToEnd();
				}
			} else if (oopChar == '\'' || oopChar == ':') {
				boolean zapped = oopChar == '\'';
				String s = readLineToEnd();
				OopCommand cmd = null;
				if (WORD_PATTERN.matcher(s).find() && !s.startsWith(":")) {
					boolean restoreFindStringVisible = false;
					int lastPosition = position;
					readChar();
					oopChar = OopUtils.upCase(oopChar);
					restoreFindStringVisible = !((oopChar >= 'A' && oopChar <= 'Z') || (oopChar == '_'));
					position = lastPosition;
					cmd = new OopCommandLabel(s, zapped, restoreFindStringVisible);
				} else if (zapped) {
					cmd = new OopCommandComment(s);
				}
				if (cmd != null) {
					cmd.setPosition(lastPosition - 1); // Label jumps are off by one
					program.commands.add(cmd);
				}
			} else if (oopChar == '/' || oopChar == '?') {
				OopDirection direction = parseDirection();
				OopCommand cmd = oopChar == '?' ? new OopCommandDirectionTry(direction) : new OopCommandDirection(direction);
				cmd.setPosition(lastPosition);
				program.commands.add(cmd);
			} else if (oopChar == '#') {
				OopCommand cmd = readCommand();
				if (cmd != null) {
					cmd.setPosition(lastPosition);
					program.commands.add(cmd);
				}

				if (lineFinished) {
					skipLine();
				}
			} else if (oopChar == 13) {
				OopCommand cmd = parseTextLine("");
				cmd.setPosition(lastPosition);
				program.commands.add(cmd);
			} else if (oopChar == 0) {
				OopCommand cmd = new OopCommandNull();
				cmd.setPosition(lastPosition);
				program.commands.add(cmd);
			} else {
				OopCommand cmd = parseTextLine(Character.toString(oopChar) + readLineToEnd());
				cmd.setPosition(lastPosition);
				program.commands.add(cmd);
			}
		}
	}
}
