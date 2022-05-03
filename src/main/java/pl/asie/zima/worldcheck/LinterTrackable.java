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

import pl.asie.zima.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.stream.Stream;

public interface LinterTrackable {
	List<Pair<String, SortedSet<ElementLocation>>> getTrackingLocations();

	default Stream<ElementLocation> getAllLocations() {
		return getTrackingLocations().stream()
				.flatMap(p -> p.getSecond().stream())
				.sorted().distinct();
	}
}
