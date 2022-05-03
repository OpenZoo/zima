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
import lombok.RequiredArgsConstructor;
import pl.asie.zima.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

@Getter
@RequiredArgsConstructor
public class LinterLabel implements LinterTrackable {
	@Getter
	public static class ActionStore {
		private final SortedSet<ElementLocation> at = new TreeSet<>();
		private final SortedSet<ElementLocation> to = new TreeSet<>();
		private final Map<ElementLocation, String> missingTargets = new HashMap<>();

		public void add(ElementLocation location, ElementLocation target, String targetName) {
			at.add(location);
			if (target != null) {
				to.add(location);
			} else if (targetName != null) {
				missingTargets.put(location, targetName);
			}
		}
	}

	public enum ActionType {
		ZAP, RESTORE, SEND, EXIST
	}

	private final String name;
	private final ActionStore zappedAt = new ActionStore();
	private final ActionStore restoredAt = new ActionStore();
	private final ActionStore sentAt = new ActionStore();
	private final SortedSet<ElementLocation> presentAt = new TreeSet<>();

	public void mark(ElementLocation location, ElementLocation target, String targetName, ActionType type) {
		switch (type) {
		case ZAP:
			zappedAt.add(location, target, targetName);
			break;
		case RESTORE:
			restoredAt.add(location, target, targetName);
			break;
		case SEND:
			sentAt.add(location, target, targetName);
			break;
		case EXIST:
			presentAt.add(location);
			break;
		}
	}

	@Override
	public List<Pair<String, SortedSet<ElementLocation>>> getTrackingLocations() {
		return List.of(
				Pair.of("Zapped at", zappedAt.at),
				Pair.of("Restored at", restoredAt.at),
				Pair.of("Sent at", sentAt.at),
				Pair.of("Present at", presentAt)
		);
	}
}
