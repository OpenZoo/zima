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

import lombok.Builder;
import lombok.Singular;
import pl.asie.libzzt.Element;
import pl.asie.libzzt.oop.commands.OopCommand;
import pl.asie.libzzt.oop.commands.OopCommandBecome;
import pl.asie.libzzt.oop.commands.OopCommandBind;
import pl.asie.libzzt.oop.commands.OopCommandChange;
import pl.asie.libzzt.oop.commands.OopCommandChar;
import pl.asie.libzzt.oop.commands.OopCommandClear;
import pl.asie.libzzt.oop.commands.OopCommandCycle;
import pl.asie.libzzt.oop.commands.OopCommandDie;
import pl.asie.libzzt.oop.commands.OopCommandEnd;
import pl.asie.libzzt.oop.commands.OopCommandEndgame;
import pl.asie.libzzt.oop.commands.OopCommandGive;
import pl.asie.libzzt.oop.commands.OopCommandGo;
import pl.asie.libzzt.oop.commands.OopCommandIdle;
import pl.asie.libzzt.oop.commands.OopCommandIf;
import pl.asie.libzzt.oop.commands.OopCommandLock;
import pl.asie.libzzt.oop.commands.OopCommandPlay;
import pl.asie.libzzt.oop.commands.OopCommandPut;
import pl.asie.libzzt.oop.commands.OopCommandRestart;
import pl.asie.libzzt.oop.commands.OopCommandRestore;
import pl.asie.libzzt.oop.commands.OopCommandSend;
import pl.asie.libzzt.oop.commands.OopCommandSet;
import pl.asie.libzzt.oop.commands.OopCommandShoot;
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
import pl.asie.libzzt.oop.directions.OopDirectionRnd;
import pl.asie.libzzt.oop.directions.OopDirectionRndne;
import pl.asie.libzzt.oop.directions.OopDirectionRndns;
import pl.asie.libzzt.oop.directions.OopDirectionRndp;
import pl.asie.libzzt.oop.directions.OopDirectionSeek;
import pl.asie.libzzt.oop.directions.OopDirectionSouth;
import pl.asie.libzzt.oop.directions.OopDirectionWest;

import java.util.Map;

@Builder(toBuilder = true)
public class OopParserConfiguration {
	@Singular
	private final Map<Class<?>, OopTokenParser<?>> classParsers;
	@Singular
	private final Map<String, Integer> colors;

	@SuppressWarnings("unchecked")
	public <T> OopTokenParser<T> getClassParser(Class<T> cl) {
		return (OopTokenParser<T>) this.classParsers.get(cl);
	}

	private static OopTokenParser<Object> parseGiveOrTake(boolean isTake) {
		return context -> {
			OopCounterType type = context.parseType(OopCounterType.class);
			if (type == null) {
				throw new OopParseException("Invalid type: " + context.getWord());
			}
			context.readValue();
			if (isTake) {
				return new OopCommandGive(type, -context.getValue(), context.parseCommand());
			} else {
				return new OopCommandGive(type, context.getValue(), context.parseCommand());
			}
		};
	}

	public static final OopParserConfiguration ZZT = OopParserConfiguration.builder()
			.color("BLUE", 9)
			.color("GREEN", 10)
			.color("CYAN", 11)
			.color("RED", 12)
			.color("PURPLE", 13)
			.color("YELLOW", 14)
			.color("WHITE", 15)
			.classParser(OopCounterType.class, context -> {
				context.readWord();
				try {
					return OopCounterType.valueOf(context.getWord());
				} catch (IllegalArgumentException e) {
					return null;
				}
			})
			.classParser(OopLabelTarget.class, context ->  {
				context.readWord();
				return new OopLabelTarget(context.getWord());
			})
			.classParser(OopCondition.class, OopTokenWordDiscriminator.builder()
					.defaultParser(context -> new OopConditionFlag(context.getWord()))
					.word("NOT", context -> new OopConditionNot(context.parseType(OopCondition.class)))
					.word("ALLIGNED", context -> new OopConditionAlligned())
					.word("CONTACT", context -> new OopConditionContact())
					.word("BLOCKED", context -> new OopConditionBlocked(context.parseType(OopDirection.class)))
					.word("ENERGIZED", context -> new OopConditionEnergized())
					.word("ANY", context -> new OopConditionAny(context.parseType(OopTile.class)))
					.build()
			)
			.classParser(OopDirection.class, OopTokenWordDiscriminator.builder()
					.word("N", context -> new OopDirectionNorth())
					.word("NORTH", context -> new OopDirectionNorth())
					.word("S", context -> new OopDirectionSouth())
					.word("SOUTH", context -> new OopDirectionSouth())
					.word("W", context -> new OopDirectionWest())
					.word("WEST", context -> new OopDirectionWest())
					.word("E", context -> new OopDirectionEast())
					.word("EAST", context -> new OopDirectionEast())
					.word("I", context -> new OopDirectionIdle())
					.word("IDLE", context -> new OopDirectionIdle())
					.word("SEEK", context -> new OopDirectionSeek())
					.word("FLOW", context -> new OopDirectionFlow())
					.word("RND", context -> new OopDirectionRnd())
					.word("RNDNS", context -> new OopDirectionRndns())
					.word("RNDNE", context -> new OopDirectionRndne())
					.word("CW", context -> new OopDirectionCw(context.parseType(OopDirection.class)))
					.word("CCW", context -> new OopDirectionCcw(context.parseType(OopDirection.class)))
					.word("RNDP", context -> new OopDirectionRndp(context.parseType(OopDirection.class)))
					.word("OPP", context -> new OopDirectionOpp(context.parseType(OopDirection.class)))
					.build()
			)
			.classParser(OopCommand.class, OopTokenWordDiscriminator.builder()
					.defaultParser(context -> {
						// TODO: This doesn't handle #TEXT right if TEXT doesn't lead to a valid target.
						return new OopCommandSend(new OopLabelTarget(context.getWord()));
					})
					.word("GO", context -> new OopCommandGo(context.parseType(OopDirection.class)))
					.word("TRY", context -> new OopCommandTry(context.parseType(OopDirection.class), context.parseCommand()))
					.word("WALK", context -> new OopCommandWalk(context.parseType(OopDirection.class)))
					.word("SET", context -> {
						context.readWord();
						return new OopCommandSet(context.getWord());
					})
					.word("CLEAR", context -> {
						context.readWord();
						return new OopCommandClear(context.getWord());
					})
					.word("IF", context -> {
						OopCondition cond = context.parseType(OopCondition.class);
						return new OopCommandIf(cond, context.parseCommand());
					})
					.word("SHOOT", context -> new OopCommandShoot(context.parseType(OopDirection.class)))
					.word("THROWSTAR", context -> new OopCommandThrowstar(context.parseType(OopDirection.class)))
					.word("GIVE", parseGiveOrTake(false))
					.word("TAKE", parseGiveOrTake(true))
					.word("END", context -> new OopCommandEnd())
					.word("ENDGAME", context -> new OopCommandEndgame())
					.word("IDLE", context -> new OopCommandIdle())
					.word("RESTART", context -> new OopCommandRestart())
					.word("ZAP", context -> new OopCommandZap(context.parseType(OopLabelTarget.class)))
					.word("RESTORE", context -> new OopCommandRestore(context.parseType(OopLabelTarget.class)))
					.word("LOCK", context -> new OopCommandLock())
					.word("UNLOCK", context -> new OopCommandUnlock())
					.word("SEND", context -> new OopCommandSend(context.parseType(OopLabelTarget.class)))
					.word("BECOME", context -> new OopCommandBecome(context.parseType(OopTile.class)))
					.word("PUT", context -> {
						OopDirection dir = context.parseType(OopDirection.class);
						OopTile tile = context.parseType(OopTile.class);
						return new OopCommandPut(dir, tile);
					})
					.word("CHANGE", context -> {
						OopTile tileFrom = context.parseType(OopTile.class);
						OopTile tileTo = context.parseType(OopTile.class);
						return new OopCommandChange(tileFrom, tileTo);
					})
					.word("PLAY", context -> {
						context.getState().lineFinished = false;
						return new OopCommandPlay(new OopSound(context, context.parseLineToEnd()));
					})
					.word("CYCLE", context -> {
						context.readValue();
						return new OopCommandCycle(context.getValue());
					})
					.word("CHAR", context -> {
						context.readValue();
						return new OopCommandChar(context.getValue());
					})
					.word("DIE", context -> new OopCommandDie())
					.word("BIND", context -> {
						context.readWord();
						return new OopCommandBind(context.getWord());
					})
					.build()
			)
			.classParser(OopTile.class, context -> {
				int color = 0;

				context.readWord();
				if (context.getConfig().colors.containsKey(context.getWord())) {
					color = context.getConfig().colors.get(context.getWord());
					context.readWord();
				}

				Element element = context.getEngine().getElements().byOopTokenName(context.getWord());
				if (element == null) {
					throw new OopParseException("Bad object kind: " + context.getWord());
				}
				return new OopTile(element, color);
			})
			.build();
}
