package pl.asie.libzzt.oop;

import lombok.Builder;
import lombok.Singular;

import java.util.HashMap;
import java.util.Map;

@Builder(toBuilder = true)
public class OopTokenWordDiscriminator<T> implements OopTokenParser<T> {
	@Builder.Default
	private final OopTokenParser<T> defaultParser = context -> {
		throw new RuntimeException("Invalid token: " + context.getWord());
	};
	@Singular
	private final Map<String, OopTokenParser<T>> words;

	@Override
	public T parse(OopParserContext context) {
		context.readWord();
		OopTokenParser<T> parser = words.get(context.getWord());
		if (parser != null) {
			return parser.parse(context);
		} else {
			return defaultParser.parse(context);
		}
	}
}
