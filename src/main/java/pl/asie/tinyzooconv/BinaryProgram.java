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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import pl.asie.libzxt.zzt.oop.commands.OopCommandZxtDieItem;
import pl.asie.libzxt.zzt.oop.commands.OopCommandZxtViewport;
import pl.asie.libzxt.zzt.oop.conditions.OopConditionZxtRnd;
import pl.asie.tinyzooconv.exceptions.BinarySerializerException;
import pl.asie.tinyzooconv.exceptions.IdNotFoundException;
import pl.asie.libzzt.oop.OopLabelTarget;
import pl.asie.libzzt.oop.OopProgram;
import pl.asie.libzzt.oop.OopSound;
import pl.asie.libzzt.oop.OopTile;
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
import pl.asie.libzzt.oop.directions.OopDirectionRnd;
import pl.asie.libzzt.oop.directions.OopDirectionRndne;
import pl.asie.libzzt.oop.directions.OopDirectionRndns;
import pl.asie.libzzt.oop.directions.OopDirectionRndp;
import pl.asie.libzzt.oop.directions.OopDirectionSeek;
import pl.asie.libzzt.oop.directions.OopDirectionSouth;
import pl.asie.libzzt.oop.directions.OopDirectionWest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Getter
@EqualsAndHashCode
public class BinaryProgram implements BinarySerializable {
	private final BinaryProgramBoardContext context;
	private final OopProgram program;
	private final Map<Integer, Integer> positionMap = new HashMap<>();
	private static final int CODE_OFFSET = 5;
	private static final int WORD_WRAP_WIDTH = 19;

	public BinaryProgram(BinaryProgramBoardContext context, OopProgram program) {
		this.context = context;
		this.program = program;
	}

	// TODO: This is bad code from the prototype converter. Clean it up.
	public void warnOrError(String s) {
		/* if (failFast) {
			throw new RuntimeException(s);
		} else { */
		System.err.println("WARNING: " + s);
		// }
	}

	private void serializeTile(OopTile tile, List<Integer> code) {
		code.add(tile.getElement().getId());
		code.add(tile.getColor());
	}

	private void serializeLabelTarget(OopLabelTarget target, List<Integer> code) throws IdNotFoundException {
		code.add(context.getNameId(target.getTarget()));
		code.add(context.getLabelId(target.getLabel()));
	}

	private String isValidLabelTarget(OopLabelTarget target) {
		List<String> errors = new ArrayList<>();
		try {
			context.getNameId(target.getTarget());
		} catch (IdNotFoundException e) {
			errors.add(e.getMessage());
		}
		try {
			context.getLabelId(target.getLabel());
		} catch (IdNotFoundException e) {
			errors.add(e.getMessage());
		}
		if (errors.isEmpty()) {
			return null;
		} else {
			return String.join(", ", errors);
		}
	}

	private void serializeCondition(OopCondition condition, List<Integer> code) {
		if (condition instanceof OopConditionNot cond) {
			code.add(0x00);
			serializeCondition(cond.getCond(), code);
		} else if (condition instanceof OopConditionAlligned) {
			code.add(0x01);
		} else if (condition instanceof OopConditionContact) {
			code.add(0x02);
		} else if (condition instanceof OopConditionBlocked cond) {
			code.add(0x03);
			serializeDirection(cond.getDirection(), code);
		} else if (condition instanceof OopConditionEnergized) {
			code.add(0x04);
		} else if (condition instanceof OopConditionAny cond) {
			code.add(0x05);
			serializeTile(cond.getTile(), code);
		} else if (condition instanceof OopConditionFlag cond) {
			code.add(0x06);
			try {
				int flagIdx = context.getFlagId(cond.getFlag());
				code.add(flagIdx);
			} catch (IdNotFoundException e) {
				// TODO: Emit warning
				code.add(255);
			}
		} else if (condition instanceof OopConditionZxtRnd cond) {
			code.add(0x07);
		} else {
			throw new RuntimeException("Unsupported condition: " + condition);
		}
	}

	private void serializeDirection(OopDirection direction, List<Integer> code) {
		if (direction instanceof OopDirectionIdle) {
			code.add(0x00);
		} else if (direction instanceof OopDirectionNorth) {
			code.add(0x01);
		} else if (direction instanceof OopDirectionSouth) {
			code.add(0x02);
		} else if (direction instanceof OopDirectionEast) {
			code.add(0x03);
		} else if (direction instanceof OopDirectionWest) {
			code.add(0x04);
		} else if (direction instanceof OopDirectionSeek) {
			code.add(0x05);
		} else if (direction instanceof OopDirectionFlow) {
			code.add(0x06);
		} else if (direction instanceof OopDirectionRnd) {
			code.add(0x07);
		} else if (direction instanceof OopDirectionRndns) {
			code.add(0x08);
		} else if (direction instanceof OopDirectionRndne) {
			code.add(0x09);
		} else if (direction instanceof OopDirectionCw dir) {
			code.add(0x0A);
			serializeDirection(dir.getChild(), code);
		} else if (direction instanceof OopDirectionCcw dir) {
			code.add(0x0B);
			serializeDirection(dir.getChild(), code);
		} else if (direction instanceof OopDirectionRndp dir) {
			code.add(0x0C);
			serializeDirection(dir.getChild(), code);
		} else if (direction instanceof OopDirectionOpp dir) {
			code.add(0x0D);
			serializeDirection(dir.getChild(), code);
		} else {
			throw new RuntimeException("Unsupported direction: " + direction);
		}
	}

	private void serializeSkippableCommand(BinarySerializerOutput output, OopCommand command, List<Integer> code, int codeOffset, Map<Integer, BinarySerializable> ptrRequests) throws BinarySerializerException {
		if (command == null) {
			code.add(0);
			return;
		}

		List<Integer> cmdCode = new ArrayList<>();
		serializeCommand(output, command, cmdCode, codeOffset + code.size() + 1, null, ptrRequests);
		code.add(cmdCode.size());
		code.addAll(cmdCode);
	}

	private void addPtrRequest(BinarySerializerOutput output, Map<Integer, BinarySerializable> ptrRequests, List<Integer> code, int codeOffset, BinarySerializable target) {
		ptrRequests.put(code.size() + codeOffset, target);
		for (int i = 0; i < output.getFarPointerSize(); i++) {
			code.add(0);
		}
	}

	private void serializeCommand(BinarySerializerOutput output, OopCommand command, List<Integer> code, int codeOffset, List<Integer> labels, Map<Integer, BinarySerializable> ptrRequests) throws BinarySerializerException {
		if (command instanceof OopCommandTextLine cmd) {
			command = new OopCommandTZWrappedTextLines(List.of(cmd), WORD_WRAP_WIDTH);
		}

		boolean isInner = labels == null;
		if (command instanceof OopCommandLabel label) {
			if (isInner) throw new RuntimeException("Not allowed inside a command!");
			labels.add(context.getLabelId(label.getLabel().toUpperCase(Locale.ROOT)));
			int pos = code.size() | (label.isRestoreFindStringVisible() ? 0x8000 : 0);
			labels.add(pos & 0xFF);
			labels.add(pos >> 8);
		} else if (command instanceof OopCommandEnd) {
			code.add(0x00);
		} else if (command instanceof OopCommandDirection cmd) {
			code.add(0x01);
			serializeDirection(cmd.getDirection(), code);
		} else if (command instanceof OopCommandDirectionTry cmd) {
			code.add(0x02);
			serializeDirection(cmd.getDirection(), code);
		} else if (command instanceof OopCommandGo cmd) {
			code.add(0x03);
			serializeDirection(cmd.getDirection(), code);
		} else if (command instanceof OopCommandTry cmd) {
			code.add(0x04);
			serializeDirection(cmd.getDirection(), code);
			serializeSkippableCommand(output, cmd.getElseCommand(), code, codeOffset, ptrRequests);
		} else if (command instanceof OopCommandWalk cmd) {
			code.add(0x05);
			serializeDirection(cmd.getDirection(), code);
		} else if (command instanceof OopCommandSet cmd) {
			code.add(0x06);
			code.add(context.getFlagId(cmd.getFlag()));
		} else if (command instanceof OopCommandClear cmd) {
			code.add(0x07);
			code.add(context.getFlagId(cmd.getFlag()));
		} else if (command instanceof OopCommandIf cmd) {
			code.add(0x08);
			serializeCondition(cmd.getCondition(), code);
			serializeSkippableCommand(output, cmd.getTrueCommand(), code, codeOffset, ptrRequests);
		} else if (command instanceof OopCommandShoot cmd) {
			code.add(0x09);
			serializeDirection(cmd.getDirection(), code);
		} else if (command instanceof OopCommandThrowstar cmd) {
			code.add(0x0A);
			serializeDirection(cmd.getDirection(), code);
		} else if (command instanceof OopCommandGive cmd) {
			code.add(0x0B);
			switch (cmd.getCounterType()) {
				case HEALTH -> code.add(0x00);
				case AMMO -> code.add(0x01);
				case GEMS -> code.add(0x02);
				case TORCHES -> code.add(0x03);
				case SCORE -> code.add(0x04);
				case TIME -> code.add(0x05);
				default -> throw new RuntimeException("Unsupported counter type: " + cmd.getCounterType());
			}
			code.add(cmd.getAmount() & 0xFF);
			code.add((cmd.getAmount() >> 8) & 0xFF);
			serializeSkippableCommand(output, cmd.getElseCommand(), code, codeOffset, ptrRequests);
		} else if (command instanceof OopCommandEndgame) {
			code.add(0x0D);
		} else if (command instanceof OopCommandIdle) {
			code.add(0x0E);
		} else if (command instanceof OopCommandRestart) {
			code.add(0x0F);
		} else if (command instanceof OopCommandZap cmd) {
			String err = isValidLabelTarget(cmd.getTarget());
			if (err != null) {
				warnOrError("#ZAP: " + err);
				code.add(0x0C);
			} else {
				code.add(0x10);
				serializeLabelTarget(cmd.getTarget(), code);
			}
		} else if (command instanceof OopCommandRestore cmd) {
			String err = isValidLabelTarget(cmd.getTarget());
			if (err != null) {
				warnOrError("#RESTORE: " + err);
				code.add(0x0C);
			} else {
				code.add(0x11);
				serializeLabelTarget(cmd.getTarget(), code);
			}
		} else if (command instanceof OopCommandLock) {
			code.add(0x12);
		} else if (command instanceof OopCommandUnlock) {
			code.add(0x13);
		} else if (command instanceof OopCommandSend cmd) {
			String err = isValidLabelTarget(cmd.getTarget());
			if (err != null) {
				warnOrError("#SEND: " + err);
				code.add(0x0C);
			} else {
				code.add(0x14);
				serializeLabelTarget(cmd.getTarget(), code);
			}
		} else if (command instanceof OopCommandBecome cmd) {
			code.add(0x15);
			serializeTile(cmd.getTile(), code);
		} else if (command instanceof OopCommandPut cmd) {
			code.add(0x16);
			serializeDirection(cmd.getDirection(), code);
			serializeTile(cmd.getTile(), code);
		} else if (command instanceof OopCommandChange cmd) {
			code.add(0x17);
			serializeTile(cmd.getTileFrom(), code);
			serializeTile(cmd.getTileTo(), code);
		} else if (command instanceof OopCommandPlay cmd) {
			code.add(0x18);
			List<OopSound.Note> notes = cmd.getSound().getNotes();
			code.add(notes.size() * 2);
			for (OopSound.Note note : notes) {
				code.add(note.getNote());
				code.add(note.getDuration());
			}
		} else if (command instanceof OopCommandCycle cmd) {
			code.add(0x19);
			code.add(cmd.getValue());
		} else if (command instanceof OopCommandChar cmd) {
			code.add(0x1A);
			code.add(cmd.getValue());
		} else if (command instanceof OopCommandDie) {
			code.add(0x1B);
			code.add(0x00);
		} else if (command instanceof OopCommandBind cmd) {
			try {
				int bindId = context.getNameId(cmd.getTargetName());
				code.add(0x1C);
				code.add(bindId);
			} catch (IdNotFoundException e) {
				warnOrError("#BIND: " + e.getMessage());
			}
		} else if (command instanceof OopCommandTZWrappedTextLines cmd) {
			code.add(0x1D);
			code.add(cmd.getLineCount());
			code.add(cmd.getLines().size());

			for (OopCommandTextLine line : cmd.getLines()) {
				int targetId = 255;
				int labelId = 255;
				if (line.getType() == OopCommandTextLine.Type.HYPERLINK) {
					try {
						targetId = context.getNameId(line.getDestination().getTarget().toUpperCase(Locale.ROOT));
					} catch (IdNotFoundException e) {
						warnOrError("T " + e.getMessage());
					}
					try {
						labelId = context.getLabelId(line.getDestination().getLabel().toUpperCase(Locale.ROOT));
					} catch (IdNotFoundException e) {
						warnOrError("text line: " + e.getMessage());
					}
				}
				addPtrRequest(output, ptrRequests, code, codeOffset, new BinaryTextLine(line, targetId, labelId));
			}
		} else if (command instanceof OopCommandZxtDieItem cmd) {
			code.add(0x1B);
			code.add(0x02);
		} else if (command instanceof OopCommandZxtViewport cmd) {
			code.add(0x1F);
			switch (cmd.getType()) {
				case LOCK -> code.add(0x00);
				case UNLOCK -> code.add(0x01);
				case FOCUS -> {
					code.add(0x02);
					if ("PLAYER".equals(cmd.getTarget())) {
						code.add(251);
					} else {
						code.add(context.getNameId(cmd.getTarget()));
					}
				}
				case MOVE -> {
					code.add(0x03);
					serializeDirection(cmd.getDirection(), code);
				}
			}
		} else {
			throw new RuntimeException("Unsupported command: " + command);
		}
	}

	private boolean isNonEmptyText(OopCommand cmd) {
		return cmd instanceof OopCommandTextLine && !((OopCommandTextLine) cmd).getMessage().isEmpty();
	}

	private boolean isMergeableWith(OopCommandTextLine a, OopCommandTextLine other) {
		if (a.getType() != other.getType()) {
			return false;
		}
		if (a.getType() == OopCommandTextLine.Type.HYPERLINK) {
			if (!Objects.equals(a.getDestination(), other.getDestination())) {
				return false;
			}
			return false; // TODO: Cyber Purge needs this, but other programs might not like it
		} else if (a.getType() == OopCommandTextLine.Type.EXTERNAL_HYPERLINK) {
			if (!Objects.equals(a.getExternalDestination(), other.getExternalDestination())) {
				return false;
			}
			return false; // TODO: Cyber Purge needs this, but other programs might not like it
		}
		if (a.getType() == OopCommandTextLine.Type.REGULAR) {
			if ("".equals(a.getMessage())) {
				return false;
			}
		}
		return true;
	}

	public Integer serializeProgramPosition(int position) {
		if (position == -1) {
			return -1;
		} else if (position == 0) {
			return 0;
		} else {
			return positionMap.get(position);
		}
	}

	public void prepare(BinarySerializerOutput output) throws IOException, BinarySerializerException {
		output = new CountingBinarySerializerOutput(output);
		positionMap.clear();

		List<Integer> code = new ArrayList<>();
		List<Integer> labels = new ArrayList<>();
		Map<Integer, BinarySerializable> ptrRequests = new HashMap<>();
		// byte[] windowName = program.getWindowName() != null ? program.getWindowName().getBytes(StandardCharsets.ISO_8859_1) : new byte[0];
		byte[] windowName = new byte[0];

		List<OopCommand> commands = new ArrayList<>(program.getCommands().size());
		List<OopCommandTextLine> textLines = new ArrayList<>();
		for (OopCommand cmd : program.getCommands()) {
			if (cmd instanceof OopCommandComment) {
				continue;
			}

			if (cmd instanceof OopCommandCycle c) {
				if (c.getValue() <= 0) {
					continue;
				}
			} else if (cmd instanceof OopCommandChar c) {
				if (c.getValue() <= 0 || c.getValue() > 255) {
					continue;
				}
			}

			if (isNonEmptyText(cmd) && cmd instanceof OopCommandTextLine tl) {
				if (!textLines.isEmpty()) {
					OopCommandTextLine prev = textLines.get(0);
					if (!isMergeableWith(tl, prev)) {
						commands.add(new OopCommandTZWrappedTextLines(textLines, WORD_WRAP_WIDTH));
						/* if (prev.getType() == OopCommandTextLine.Type.HYPERLINK) {
							System.out.println(textLines);
						} */
						textLines.clear();
					}
				}
				if (tl.getType() != OopCommandTextLine.Type.EXTERNAL_HYPERLINK) {
					textLines.add(tl);
				} else {
					warnOrError("External hyperlinks not supported!");
				}
			} else {
				if (!textLines.isEmpty()) {
					commands.add(new OopCommandTZWrappedTextLines(textLines, WORD_WRAP_WIDTH));
					textLines.clear();
				}
				if (cmd instanceof OopCommandTextLine tl) {
					commands.add(new OopCommandTZWrappedTextLines(List.of(tl), WORD_WRAP_WIDTH));
				} else {
					commands.add(cmd);
				}
			}
		}
		if (!textLines.isEmpty()) {
			commands.add(new OopCommandTZWrappedTextLines(textLines, WORD_WRAP_WIDTH));
			textLines.clear();
		}

		// Serialization
		OopCommand lastCmd = null;
		for (OopCommand cmd : commands) {
			if (cmd.getPosition() != null) {
				positionMap.put(cmd.getPosition(), code.size());
			}
			serializeCommand(output, cmd, code, 0, labels, ptrRequests);
		}
	}

	@Override
	public void serialize(BinarySerializerOutput output) throws IOException, BinarySerializerException {
		Map<Integer, Integer> positionMap = new HashMap<>();
		List<Integer> code = new ArrayList<>();
		List<Integer> labels = new ArrayList<>();
		Map<Integer, BinarySerializable> ptrRequests = new HashMap<>();
		// byte[] windowName = program.getWindowName() != null ? program.getWindowName().getBytes(StandardCharsets.ISO_8859_1) : new byte[0];
		byte[] windowName = new byte[0];

		List<OopCommand> commands = new ArrayList<>(program.getCommands().size());
		List<OopCommandTextLine> textLines = new ArrayList<>();
		for (OopCommand cmd : program.getCommands()) {
			if (cmd instanceof OopCommandComment) {
				continue;
			}

			if (cmd instanceof OopCommandCycle c) {
				if (c.getValue() <= 0) {
					continue;
				}
			} else if (cmd instanceof OopCommandChar c) {
				if (c.getValue() <= 0 || c.getValue() > 255) {
					continue;
				}
			}

			if (isNonEmptyText(cmd) && cmd instanceof OopCommandTextLine tl) {
				if (!textLines.isEmpty()) {
					OopCommandTextLine prev = textLines.get(0);
					if (!isMergeableWith(tl, prev)) {
						commands.add(new OopCommandTZWrappedTextLines(textLines, WORD_WRAP_WIDTH));
						/* if (prev.getType() == OopCommandTextLine.Type.HYPERLINK) {
							System.out.println(textLines);
						} */
						textLines.clear();
					}
				}
				if (tl.getType() != OopCommandTextLine.Type.EXTERNAL_HYPERLINK) {
					textLines.add(tl);
				} else {
					warnOrError("External hyperlinks not supported!");
				}
			} else {
				if (!textLines.isEmpty()) {
					commands.add(new OopCommandTZWrappedTextLines(textLines, WORD_WRAP_WIDTH));
					textLines.clear();
				}
				if (cmd instanceof OopCommandTextLine tl) {
					commands.add(new OopCommandTZWrappedTextLines(List.of(tl), WORD_WRAP_WIDTH));
				} else {
					commands.add(cmd);
				}
			}
		}
		if (!textLines.isEmpty()) {
			commands.add(new OopCommandTZWrappedTextLines(textLines, WORD_WRAP_WIDTH));
			textLines.clear();
		}

		// Serialization
		OopCommand lastCmd = null;
		for (OopCommand cmd : commands) {
			if (cmd.getPosition() != null) {
				positionMap.put(cmd.getPosition(), code.size());
			}
			serializeCommand(output, cmd, code, 0, labels, ptrRequests);
			lastCmd = cmd;
		}
		if (!(lastCmd instanceof OopCommandEnd)) {
			serializeCommand(output, new OopCommandEnd(), code, 0, labels, ptrRequests);
		}

		// Statistics
		/* int linesInProgram = 0;
		for (OopCommand cmd : commands) {
			if (cmd instanceof OopCommandGBZWrappedTextLines tl) {
				linesInProgram += tl.getLines().size();
			}
		}
		this.worldState.setMaxLinesInProgram(linesInProgram); */

		output.writeByte(context.getNameId(program.getName()));
		int offsetToWindowName = 0;
		if (windowName.length > 0) {
			offsetToWindowName = code.size() + 5;
		}
		int offsetToLabelList = 0;
		if (!labels.isEmpty()) {
			if (windowName.length > 0) {
				offsetToLabelList = code.size() + 6 + windowName.length;
			} else {
				offsetToLabelList = code.size() + 5;
			}
		}
		output.writeShort(offsetToWindowName);
		output.writeShort(offsetToLabelList);
		for (int i = 0; i < code.size(); i++) {
			BinarySerializable farPointedObj = ptrRequests.get(i);
			if (farPointedObj != null) {
				output.writeFarPointerTo(farPointedObj);
				i += output.getFarPointerSize() - 1;
				continue;
			}
			output.writeByte(code.get(i));
		}
		if (windowName.length > 0) {
			output.writeByte(windowName.length);
			for (byte c : windowName) {
				output.writeByte((int) c & 0xFF);
			}
		}
		if (!labels.isEmpty()) {
			int labelCount = labels.size() / 3;
			if (labelCount >= 256) {
				throw new RuntimeException("Maximum of 255 labels per stat supported!");
			}
			output.writeByte(labelCount);
			for (int i = 0; i < labels.size(); i++) {
				output.writeByte(labels.get(i));
			}
		}
	}

	@Override
	public String toString() {
		return this.program.toString();
	}
}
