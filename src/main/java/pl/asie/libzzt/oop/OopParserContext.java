package pl.asie.libzzt.oop;

import pl.asie.libzzt.EngineDefinition;
import pl.asie.libzzt.oop.commands.OopCommand;

public interface OopParserContext {
	EngineDefinition getEngine();

	default OopParserConfiguration getConfig() {
		return getEngine().getOopParserConfiguration();
	}

	OopParserState getState();

	int getChar();

	void readChar();

	int getValue();

	void readValue();

	String getWord();

	void readWord();

	void skipLine();

	String parseLineToEnd();

	<T> T parseType(Class<T> cl);

	OopCommand parseInstruction();

	OopCommand parseCommand();

	OopParserState pushState();

	void popState(OopParserState state);
}
