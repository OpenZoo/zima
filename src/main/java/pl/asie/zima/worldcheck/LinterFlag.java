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

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Getter
@RequiredArgsConstructor
public class LinterFlag implements LinterTrackable {
	public enum ActionType {
		SET, CLEAR, CHECK
	}

	private final String name;
	private final SortedSet<ElementLocation> setAt = new TreeSet<>();
	private final SortedSet<ElementLocation> clearedAt = new TreeSet<>();
	private final SortedSet<ElementLocation> changedAt = new TreeSet<>();
	private final SortedSet<ElementLocation> checkedAt = new TreeSet<>();

	public void mark(ElementLocation location, ActionType type) {
		switch (type) {
		case SET:
			setAt.add(location);
			changedAt.add(location);
			break;
		case CLEAR:
			clearedAt.add(location);
			changedAt.add(location);
			break;
		case CHECK:
			checkedAt.add(location);
			break;
		}
	}

	@Override
	public List<Pair<String, SortedSet<ElementLocation>>> getTrackingLocations() {
		return List.of(
				Pair.of("Set at", setAt),
				Pair.of("Cleared at", clearedAt),
				Pair.of("Changed at", changedAt),
				Pair.of("Checked at", checkedAt)
		);
	}
}
