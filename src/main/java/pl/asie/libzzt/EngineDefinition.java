package pl.asie.libzzt;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class EngineDefinition {
	public static final EngineDefinition ZZT = EngineDefinition.builder()
			.baseKind(EngineBaseKind.ZZT)
			.boardWidth(60).boardHeight(25)
			.maxBoardSize(20000)
			.maxStatCount(150 + 1)
			.elements(ElementLibraryZZT.INSTANCE)
			.build();
	public static final EngineDefinition SUPER_ZZT = EngineDefinition.builder()
			.baseKind(EngineBaseKind.SUPER_ZZT)
			.boardWidth(96).boardHeight(80)
			.maxBoardSize(20000)
			.maxStatCount(128 + 1)
			.elements(ElementLibrarySuperZZT.INSTANCE)
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
}
