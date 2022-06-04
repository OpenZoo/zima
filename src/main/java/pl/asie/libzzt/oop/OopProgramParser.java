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

import lombok.RequiredArgsConstructor;
import pl.asie.libzzt.EngineDefinition;
import pl.asie.libzzt.oop.commands.OopCommand;
import pl.asie.libzzt.oop.commands.OopCommandComment;
import pl.asie.libzzt.oop.commands.OopCommandDirection;
import pl.asie.libzzt.oop.commands.OopCommandDirectionTry;
import pl.asie.libzzt.oop.commands.OopCommandEnd;
import pl.asie.libzzt.oop.commands.OopCommandLabel;
import pl.asie.libzzt.oop.commands.OopCommandTextLine;
import pl.asie.libzzt.oop.directions.OopDirection;

import java.util.regex.Pattern;

@RequiredArgsConstructor
public class OopProgramParser implements OopParserContext {
	private static final Pattern WORD_PATTERN = Pattern.compile("^[0-9A-Za-z:_]+$");
	private final EngineDefinition engineDefinition;
	private OopProgram program;
	private String data;
	private OopParserState state;

	@Override
	public int getChar() {
		return this.state.oopChar;
	}

	@Override
	public int getValue() {
		return this.state.oopValue;
	}

	@Override
	public String getWord() {
		return this.state.oopWord;
	}

	@Override
	public void readChar() {
		if (this.state.position >= 0 && this.state.position < data.length()) {
			this.state.oopChar = data.charAt(this.state.position++);
		} else {
			this.state.oopChar = 0;
		}
	}

	@Override
	public void readWord() {
		StringBuilder s = new StringBuilder();
		readChar();
		while (this.state.oopChar == ' ') {
			readChar();
		}
		this.state.oopChar = OopUtils.upCase(this.state.oopChar);
		if (this.state.oopChar < '0' || this.state.oopChar > '9') {
			while ((this.state.oopChar >= 'A' && this.state.oopChar <= 'Z') || this.state.oopChar == ':' || (this.state.oopChar >= '0' && this.state.oopChar <= '9') || this.state.oopChar == '_') {
				s.appendCodePoint(this.state.oopChar);
				readChar();
				this.state.oopChar = OopUtils.upCase(this.state.oopChar);
			}
		}
		this.state.oopWord = s.toString();
		if (this.state.position > 0) {
			this.state.position--;
		}
	}

	private void readValueInner(boolean signed) {
		StringBuilder s = new StringBuilder();
		readChar();
		while (this.state.oopChar == ' ') {
			readChar();
		}

		this.state.oopChar = OopUtils.upCase(this.state.oopChar);
		if (signed && this.state.oopChar == '-') {
			s.appendCodePoint('-');
			readChar();
			this.state.oopChar = OopUtils.upCase(this.state.oopChar);
		}

		while (this.state.oopChar >= '0' && this.state.oopChar <= '9') {
			s.appendCodePoint(this.state.oopChar);
			readChar();
			this.state.oopChar = OopUtils.upCase(this.state.oopChar);
		}

		if (this.state.position > 0) {
			this.state.position--;
		}

		if (s.length() != 0) {
			// TODO: Val() has weird behaviour in Turbo Pascal 5.5 when significantly out of range.
			this.state.oopValue = Integer.parseInt(s.toString());
		} else {
			this.state.oopValue = -1;
		}
	}

	@Override
	public void readValue() {
		readValueInner(false);
	}

	@Override
	public void readSignedValue() {
		readValueInner(true);
	}

	@Override
	public void skipLine() {
		readChar();
		while (this.state.oopChar != 0 && this.state.oopChar != 13) {
			readChar();
		}
	}

	@Override
	public String parseLineToEnd() {
		StringBuilder s = new StringBuilder();
		readChar();
		while (this.state.oopChar != 0 && this.state.oopChar != 13) {
			s.appendCodePoint(this.state.oopChar);
			readChar();
		}
		return s.toString();
	}

	@Override
	public <T> T parseType(Class<T> cl) {
		return getConfig().getParser(cl).parse(this);
	}

	private OopCommandTextLine parseTextLine(String s) {
		if (s.startsWith("$")) {
			return new OopCommandTextLine(OopCommandTextLine.Type.CENTERED, null, null, s.substring(1));
		} else if (s.startsWith("!")) {
			String[] split = s.substring(1).split(";", 2);
			if (split[0].startsWith("-")) {
				return new OopCommandTextLine(OopCommandTextLine.Type.EXTERNAL_HYPERLINK, null, split[0].substring(1), split[1]);
			} else {
				return new OopCommandTextLine(OopCommandTextLine.Type.HYPERLINK, new OopLabelTarget(split[0]), null, split[1]);
			}
		} else {
			return new OopCommandTextLine(OopCommandTextLine.Type.REGULAR, null, null, s);
		}
	}

	@Override
	public OopCommand parseCommand() {
		OopParserState lastState = pushState();
		readWord();
		while ("THEN".equals(getWord())) {
			lastState = pushState();
			readWord();
		}
		if (getWord().isEmpty()) {
			return parseInstruction();
		} else {
			popState(lastState);
			return getConfig().getParser(OopCommand.class).parse(this);
		}
	}

	@Override
	public OopCommand parseInstruction() {
		this.state.lineFinished = true;
		int lastPosition = this.state.position;

		readChar();
		if (getChar() == '@') {
			if (this.state.position <= 1) {
				readWord();
				program.name = getWord();
				this.state.position = 1;
				program.windowName = parseLineToEnd();
			} else {
				skipLine();
			}
			return null;
		} else if (getChar() == '\'' || getChar() == ':') {
			boolean zapped = getChar() == '\'';
			String s = parseLineToEnd();
			OopCommand cmd = null;
			boolean restoreFindStringVisible = !s.endsWith(" ");
			while (s.endsWith(" ")) {
				s = s.substring(0, s.length() - 1);
			}
			if (WORD_PATTERN.matcher(s).find() && !s.startsWith(":")) {
				int lastPosition2 = this.state.position;
				readChar();
				this.state.oopChar = OopUtils.upCase(getChar());
				restoreFindStringVisible &= !((getChar() >= 'A' && getChar() <= 'Z') || (getChar() == '_'));
				this.state.position = lastPosition2;
				cmd = new OopCommandLabel(s, zapped, restoreFindStringVisible);
			} else if (zapped) {
				cmd = new OopCommandComment(s);
			}
			if (cmd != null) {
				cmd.setPosition(lastPosition - 1); // Label jumps are off by one
				return cmd;
			} else {
				return null;
			}
		} else if (getChar() == '/' || getChar() == '?') {
			OopDirection direction = parseType(OopDirection.class);
			OopCommand cmd = getChar() == '?' ? new OopCommandDirectionTry(direction) : new OopCommandDirection(direction);
			cmd.setPosition(lastPosition);
			return cmd;
		} else if (getChar() == '#') {
			OopCommand cmd = parseCommand();
			if (this.state.lineFinished) {
				skipLine();
			}

			if (cmd != null) {
				cmd.setPosition(lastPosition);
				return cmd;
			}
			return null;
		} else if (getChar() == 13) {
			OopCommand cmd = parseTextLine("");
			cmd.setPosition(lastPosition);
			return cmd;
		} else if (getChar() == 0) {
			// TODO: Formally different from #END (doesn't change position), but does it matter?
			OopCommand cmd = new OopCommandEnd();
			cmd.setPosition(lastPosition);
			return cmd;
		} else {
			OopCommand cmd = parseTextLine(Character.toString(getChar()) + parseLineToEnd());
			cmd.setPosition(lastPosition);
			return cmd;
		}
	}

	@Override
	public OopParserState pushState() {
		return this.state.clone();
	}

	@Override
	public void popState(OopParserState state) {
		this.state = state;
	}

	@Override
	public EngineDefinition getEngine() {
		return this.engineDefinition;
	}

	@Override
	public OopParserState getState() {
		return this.state;
	}

	public void parse(OopProgram program, String data) throws OopParseException {
		this.data = data;
		this.state = new OopParserState();
		this.program = program;

		while (this.state.position < data.length()) {
			OopCommand command = parseInstruction();
			if (command != null) {
				program.commands.add(command);
			}
		}
	}
}
