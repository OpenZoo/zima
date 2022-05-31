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

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class OopParserConfiguration {
	private final Map<Class<?>, OopTokenParser<?>> classParsers = new HashMap<>();
	private final Map<String, Integer> colors = new HashMap<>();

	public OopParserConfiguration addColor(String name, int value) {
		this.colors.put(name, value);
		return this;
	}

	public OopParserConfiguration setColors(Map<String, Integer> colors, boolean replace) {
		if (replace) this.colors.clear();
		this.colors.putAll(colors);
		return this;
	}

	public <T> OopParserConfiguration addParser(Class<T> type, OopTokenParser<T> parser) {
		if (this.classParsers.containsKey(type)) {
			this.classParsers.put(type, OopTokenParser.and(parser, (OopTokenParser<T>) this.classParsers.get(type)));
		} else {
			this.classParsers.put(type, parser);
		}
		return this;
	}

	public <T> OopParserConfiguration addParser(Class<T> type, String word, OopTokenParser<T> parser) {
		OopTokenParser<T> parentParser = (OopTokenParser<T>) this.classParsers.get(type);
		if (!(parentParser instanceof OopTokenWordDiscriminator<T>)) {
			throw new RuntimeException("Invalid parentParser for " + type.getName());
		}
		((OopTokenWordDiscriminator<T>) parentParser).addWord(word, parser);
		return this;
	}

	@SuppressWarnings("unchecked")
	public <T> OopTokenParser<T> getParser(Class<T> type) {
		return (OopTokenParser<T>) this.classParsers.get(type);
	}

	private static OopTokenParser<OopCommand> parseGiveOrTake(boolean isTake) {
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

	public static OopParserConfiguration buildZztParser() {
		OopParserConfiguration config = new OopParserConfiguration();
		config.addColor("BLUE", 9);
		config.addColor("GREEN", 10);
		config.addColor("CYAN", 11);
		config.addColor("RED", 12);
		config.addColor("PURPLE", 13);
		config.addColor("YELLOW", 14);
		config.addColor("WHITE", 15);
		config.addParser(OopCounterType.class, context -> {
			context.readWord();
			try {
				return OopCounterType.valueOf(context.getWord());
			} catch (IllegalArgumentException e) {
				return null;
			}
		});
		config.addParser(OopLabelTarget.class, context ->  {
			context.readWord();
			return new OopLabelTarget(context.getWord());
		});
		config.addParser(OopTile.class, context -> {
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
		});
		config.addParser(OopCondition.class, new OopTokenWordDiscriminator<OopCondition>()
				.setDefaultParser(context -> new OopConditionFlag(context.getWord()))
				.addWord("NOT", context -> new OopConditionNot(context.parseType(OopCondition.class)))
				.addWord("ALLIGNED", context -> new OopConditionAlligned())
				.addWord("CONTACT", context -> new OopConditionContact())
				.addWord("BLOCKED", context -> new OopConditionBlocked(context.parseType(OopDirection.class)))
				.addWord("ENERGIZED", context -> new OopConditionEnergized())
				.addWord("ANY", context -> new OopConditionAny(context.parseType(OopTile.class)))
		);
		config.addParser(OopDirection.class, new OopTokenWordDiscriminator<OopDirection>()
				.addWord("N", context -> new OopDirectionNorth())
				.addWord("NORTH", context -> new OopDirectionNorth())
				.addWord("S", context -> new OopDirectionSouth())
				.addWord("SOUTH", context -> new OopDirectionSouth())
				.addWord("W", context -> new OopDirectionWest())
				.addWord("WEST", context -> new OopDirectionWest())
				.addWord("E", context -> new OopDirectionEast())
				.addWord("EAST", context -> new OopDirectionEast())
				.addWord("I", context -> new OopDirectionIdle())
				.addWord("IDLE", context -> new OopDirectionIdle())
				.addWord("SEEK", context -> new OopDirectionSeek())
				.addWord("FLOW", context -> new OopDirectionFlow())
				.addWord("RND", context -> new OopDirectionRnd())
				.addWord("RNDNS", context -> new OopDirectionRndns())
				.addWord("RNDNE", context -> new OopDirectionRndne())
				.addWord("CW", context -> new OopDirectionCw(context.parseType(OopDirection.class)))
				.addWord("CCW", context -> new OopDirectionCcw(context.parseType(OopDirection.class)))
				.addWord("RNDP", context -> new OopDirectionRndp(context.parseType(OopDirection.class)))
				.addWord("OPP", context -> new OopDirectionOpp(context.parseType(OopDirection.class)))
		);
		config.addParser(OopCommand.class, new OopTokenWordDiscriminator<OopCommand>()
				.setDefaultParser(context -> {
					// TODO: This doesn't handle #TEXT right if TEXT doesn't lead to a valid target.
					return new OopCommandSend(new OopLabelTarget(context.getWord()));
				})
				.addWord("GO", context -> new OopCommandGo(context.parseType(OopDirection.class)))
				.addWord("TRY", context -> new OopCommandTry(context.parseType(OopDirection.class), context.parseCommand()))
				.addWord("WALK", context -> new OopCommandWalk(context.parseType(OopDirection.class)))
				.addWord("SET", context -> {
					context.readWord();
					return new OopCommandSet(context.getWord());
				})
				.addWord("CLEAR", context -> {
					context.readWord();
					return new OopCommandClear(context.getWord());
				})
				.addWord("IF", context -> {
					OopCondition cond = context.parseType(OopCondition.class);
					return new OopCommandIf(cond, context.parseCommand());
				})
				.addWord("SHOOT", context -> new OopCommandShoot(context.parseType(OopDirection.class)))
				.addWord("THROWSTAR", context -> new OopCommandThrowstar(context.parseType(OopDirection.class)))
				.addWord("GIVE", parseGiveOrTake(false))
				.addWord("TAKE", parseGiveOrTake(true))
				.addWord("END", context -> new OopCommandEnd())
				.addWord("ENDGAME", context -> new OopCommandEndgame())
				.addWord("IDLE", context -> new OopCommandIdle())
				.addWord("RESTART", context -> new OopCommandRestart())
				.addWord("ZAP", context -> new OopCommandZap(context.parseType(OopLabelTarget.class)))
				.addWord("RESTORE", context -> new OopCommandRestore(context.parseType(OopLabelTarget.class)))
				.addWord("LOCK", context -> new OopCommandLock())
				.addWord("UNLOCK", context -> new OopCommandUnlock())
				.addWord("SEND", context -> new OopCommandSend(context.parseType(OopLabelTarget.class)))
				.addWord("BECOME", context -> new OopCommandBecome(context.parseType(OopTile.class)))
				.addWord("PUT", context -> {
					OopDirection dir = context.parseType(OopDirection.class);
					OopTile tile = context.parseType(OopTile.class);
					return new OopCommandPut(dir, tile);
				})
				.addWord("CHANGE", context -> {
					OopTile tileFrom = context.parseType(OopTile.class);
					OopTile tileTo = context.parseType(OopTile.class);
					return new OopCommandChange(tileFrom, tileTo);
				})
				.addWord("PLAY", context -> {
					context.getState().lineFinished = false;
					return new OopCommandPlay(new OopSound(context, context.parseLineToEnd()));
				})
				.addWord("CYCLE", context -> {
					context.readValue();
					return new OopCommandCycle(context.getValue());
				})
				.addWord("CHAR", context -> {
					context.readValue();
					return new OopCommandChar(context.getValue());
				})
				.addWord("DIE", context -> new OopCommandDie())
				.addWord("BIND", context -> {
					context.readWord();
					return new OopCommandBind(context.getWord());
				})
		);
		return config;
	}
}
