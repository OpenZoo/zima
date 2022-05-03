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
package pl.asie.zima.worldcheck;

import lombok.Getter;
import pl.asie.libzzt.Board;
import pl.asie.libzzt.World;
import pl.asie.libzzt.oop.OopLabelTarget;
import pl.asie.libzzt.oop.OopProgram;
import pl.asie.libzzt.oop.commands.OopCommand;
import pl.asie.libzzt.oop.commands.OopCommandLabel;
import pl.asie.libzzt.oop.commands.OopCommandRestore;
import pl.asie.libzzt.oop.commands.OopCommandSend;
import pl.asie.libzzt.oop.commands.OopCommandTextLine;
import pl.asie.libzzt.oop.commands.OopCommandZap;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class LinterCheckLabels {
	private static final Set<String> PRESET_TARGETS = Set.of("", "SELF", "OTHERS", "ALL");
	private static final Set<String> PRESET_LABELS = Set.of("RESTART", "SHOT", "ENERGIZE", "THUD", "TOUCH", "BOMBED");
	@Getter
	private final Map<Integer, SortedMap<String, LinterLabel>> labels = new HashMap<>();
	private final ElementLocation worldLoc;

	public Collection<LinterLabel> getLabels(ElementLocation boardLoc) {
		if (boardLoc.getBoardId() == null) {
			return List.of();
		} else {
			var labelMap = labels.computeIfAbsent(boardLoc.getBoardId(), a -> new TreeMap<>());
			return labelMap.values();
		}
	}

	private LinterLabel getLabelFor(ElementLocation location, String labelName) {
		labelName = labelName.toUpperCase(Locale.ROOT);
		var labelMap = labels.computeIfAbsent(location.getBoardId(), a -> new TreeMap<>());
		return labelMap.computeIfAbsent(labelName, LinterLabel::new);
	}

	private Stream<ElementLocation> potentialLocationsForTarget(ElementLocation source, String target) {
		switch (target) {
			case "":
			case "SELF":
				return Stream.of(source.withCommand(null));
			case "ALL":
				return ElementLocationStream.stats(ElementLocation.board(source.getWorld(), source.getBoardId()));
			case "OTHERS":
				// TODO: Code using this method is sometimes written with X sending it to others #BIND X in mind.
				// We can't detect this for now, so pretend we're sending to everyone.
				// return ElementLocationStream.stats(ElementLocation.board(source.getWorld(), source.getBoardId()))
				//		.filter(el -> !Objects.equals(el.getStatId(), source.getStatId()));
				return ElementLocationStream.stats(ElementLocation.board(source.getWorld(), source.getBoardId()));
			default:
				return ElementLocationStream.stats(ElementLocation.board(source.getWorld(), source.getBoardId()))
						.filter(el -> {
							OopProgram program = el.getProgram();
							if (program != null && program.getName() != null) {
								return Objects.equals(program.getName(), target.toUpperCase(Locale.ROOT));
							} else {
								return false;
							}
						});
		}
	}

	private Stream<ElementLocation> locationsForTarget(ElementLocation source, OopLabelTarget labelTarget) {
		return potentialLocationsForTarget(source, labelTarget.getTarget())
				.filter(el -> getLabelFor(el, labelTarget.getLabel()).getPresentAt().stream().anyMatch(
						el2 -> el2.getBoardId().equals(el.getBoardId()) && el2.getStatId().equals(el.getStatId())
				));
	}

	private boolean actionAt(ElementLocation location, OopLabelTarget labelTarget, LinterLabel.ActionType action) {
		AtomicBoolean marked = new AtomicBoolean(false);
		locationsForTarget(location, labelTarget).forEach(el -> {
			getLabelFor(location, labelTarget.getLabel()).mark(location, el, labelTarget.getTarget(), action);
			marked.set(true);
		});
		if (!marked.get()) {
			getLabelFor(location, labelTarget.getLabel()).mark(location, null, labelTarget.getTarget(), action);
		}
		return false;
	}

	private void labelAt(ElementLocation location, String labelName) {
		getLabelFor(location, labelName).mark(location, null, null, LinterLabel.ActionType.EXIST);
	}

	public LinterCheckLabels(World world) {
		this.worldLoc = ElementLocation.world(world);
		for (int bid = 0; bid < world.getBoards().size(); bid++) {
			ElementLocation boardLoc = ElementLocation.board(world, bid);
			Board board = boardLoc.getBoard();
			// phase 1: detect label *presence*
			ElementLocationStream.commands(boardLoc, true)
					.forEach(el -> {
						OopCommand c = el.getOopCommand();
						if (c instanceof OopCommandLabel label) {
							labelAt(el, label.getLabel());
						}
					});
			// phase 2: scan for zap/restore/send
			ElementLocationStream.commands(boardLoc, true)
					.forEach(el -> {
						OopCommand c = el.getOopCommand();
						if (c instanceof OopCommandZap cmd) {
							actionAt(el, cmd.getTarget(), LinterLabel.ActionType.ZAP);
						} else if (c instanceof OopCommandRestore cmd) {
							actionAt(el, cmd.getTarget(), LinterLabel.ActionType.RESTORE);
						} else if (c instanceof OopCommandSend cmd) {
							actionAt(el, cmd.getTarget(), LinterLabel.ActionType.SEND);
						} else if (c instanceof OopCommandTextLine cmd) {
							if (cmd.getDestination() != null) {
								actionAt(el, cmd.getDestination(), LinterLabel.ActionType.SEND);
							}
						}
					});
		}
	}

	private void emitMessageForEmptyButNotEmpty(Consumer<LinterMessage> consumer,
	                                            Function<LinterLabel, Collection<ElementLocation>> funcNotEmpty,
	                                            Predicate<LinterLabel> funcEmpty,
	                                            LinterMessageType type, String prefix, boolean skipPresetLabels) {
		labels.values().stream().flatMap(m -> m.values().stream())
				.filter(f -> !Objects.equals(f.getName(), "RESTART"))
				.filter(f -> !skipPresetLabels || !PRESET_LABELS.contains(f.getName()))
				.filter(funcEmpty::test)
				.forEach(f -> {
					for (ElementLocation el : funcNotEmpty.apply(f)) {
						consumer.accept(new LinterMessage(
								el, type,
								prefix + ": " + f.getName()
						));
					}
				});
	}

	private void emitMessageForMissingTargets(Consumer<LinterMessage> consumer,
	                                            Function<LinterLabel, LinterLabel.ActionStore> funcActionStore,
	                                            LinterMessageType type, String prefix, boolean skipPresetLabels) {
		labels.values().stream().flatMap(m -> m.values().stream())
				.filter(f -> !Objects.equals(f.getName(), "RESTART"))
				.filter(f -> !skipPresetLabels || !PRESET_LABELS.contains(f.getName()))
				.filter(f -> !f.getPresentAt().isEmpty())
				.forEach(f -> {
					for (var entry : funcActionStore.apply(f).getMissingTargets().entrySet()) {
						consumer.accept(new LinterMessage(
								entry.getKey(), type,
								prefix + ": " + (entry.getValue().isEmpty() ? "" : (entry.getValue() + ":")) + f.getName()
						));
					}
				});
	}


	public void emitMessages(Consumer<LinterMessage> consumer) {
		labels.values().stream().map(m -> m.get("RESTART"))
						.filter(Objects::nonNull)
						.forEach(f -> {
							for (ElementLocation el : f.getZappedAt().getAt()) {
								consumer.accept(new LinterMessage(el, LinterMessageType.LABEL_ZAP_RESTORE_RESTART,
										"#ZAP RESTART used"));
							}
							for (ElementLocation el : f.getRestoredAt().getAt()) {
								consumer.accept(new LinterMessage(el, LinterMessageType.LABEL_ZAP_RESTORE_RESTART,
										"#RESTORE RESTART used"));
							}
						});

		emitMessageForEmptyButNotEmpty(consumer, l -> l.getSentAt().getAt(),
				f -> f.getPresentAt().isEmpty(),
				LinterMessageType.LABEL_SENT_BUT_NOT_PRESENT, "Label sent but not on board", false);
		emitMessageForEmptyButNotEmpty(consumer, l -> l.getZappedAt().getAt(),
				f -> f.getPresentAt().isEmpty(),
				LinterMessageType.LABEL_ZAPPED_BUT_NOT_PRESENT, "Label zapped but not on board", false);
		emitMessageForEmptyButNotEmpty(consumer,  l -> l.getRestoredAt().getAt(),
				f -> f.getPresentAt().isEmpty(),
				LinterMessageType.LABEL_RESTORED_BUT_NOT_PRESENT, "Label restored but not on board", false);
		emitMessageForMissingTargets(consumer, LinterLabel::getSentAt,
				LinterMessageType.LABEL_SENT_TO_MISSING_TARGET, "Label sent but nobody received", false);
		emitMessageForMissingTargets(consumer, LinterLabel::getZappedAt,
				LinterMessageType.LABEL_ZAPPED_ON_MISSING_TARGET, "Label zapped but nobody received", false);
		emitMessageForMissingTargets(consumer, LinterLabel::getRestoredAt,
				LinterMessageType.LABEL_RESTORED_ON_MISSING_TARGET, "Label restored but nobody received", false);
		/* emitMessageForEmptyButNotEmpty(consumer,  l -> l.getSentAt().getAt(),
				f -> !f.getPresentAt().isEmpty() && f.getSentAt().getTo().isEmpty(),
				LinterMessageType.LABEL_SENT_BUT_NOT_RECEIVED, "Label sent but not received", false); */
		emitMessageForEmptyButNotEmpty(consumer, LinterLabel::getPresentAt,
				f -> f.getSentAt().getAt().isEmpty(),
				LinterMessageType.LABEL_PRESENT_BUT_NOT_SENT, "Label present but never sent", true);
	}
}
