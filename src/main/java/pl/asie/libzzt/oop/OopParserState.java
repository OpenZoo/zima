package pl.asie.libzzt.oop;

import lombok.Data;

import java.io.Serializable;

@Data
public final class OopParserState implements Serializable, Cloneable {
	int position = 0;
	int oopChar;
	int oopValue;
	String oopWord;
	boolean lineFinished;

	@Override
	public OopParserState clone() {
		try {
			OopParserState clone = (OopParserState) super.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError();
		}
	}
}
