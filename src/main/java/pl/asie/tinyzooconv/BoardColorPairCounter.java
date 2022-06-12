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

import lombok.Data;
import lombok.RequiredArgsConstructor;
import pl.asie.libzzt.Board;
import pl.asie.libzzt.Element;
import pl.asie.zima.util.Pair;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BoardColorPairCounter {
	private static final boolean DEBUG = false;
	private final int viewportWidth;
	private final int viewportHeight;
	private final int colorMask; // can be 0x7F or 0xFF
	private final long[] occurences;

	public BoardColorPairCounter(int viewportWidth, int viewportHeight, int colorMask) {
		this.viewportWidth = viewportWidth;
		this.viewportHeight = viewportHeight;
		this.colorMask = colorMask;
		this.occurences = new long[colorMask + 1];
	}

	@SuppressWarnings("UnnecessaryLocalVariable")
	private int calculateWeight(int x, int width, int viewportWidth) {
		int xLeft = x;
		int xRight = (width - x + 1);
		return Math.min(Math.min(viewportWidth, width - viewportWidth + 1), Math.min(xLeft, xRight));
	}

	public void addBoard(Board board) {
		for (int y = 1; y <= board.getHeight(); y++) {
			for (int x = 1; x <= board.getWidth(); x++) {
				Element e = board.getElement(x, y);
				int color = e.isText() ? e.getTextColor() : board.getColor(x, y);
				int xWeight = calculateWeight(x, board.getWidth(), viewportWidth);
				int yWeight = calculateWeight(y, board.getHeight(), viewportHeight);
				int weight = xWeight * yWeight;
				occurences[color & colorMask] += weight;
				if (DEBUG) {
					if (color != 0x0F) {
						System.out.println("adding " + weight + " (" + xWeight + " " + yWeight + ") to " + Integer.toString(color, 4) + " " + x + " " + y);
					}
				}
			}
		}
	}

	@Data
	private static class OccurencePair {
		private final int color;
		private final long count;
	}

	public List<Integer> getMostCommonColors() {
		return IntStream.range(0, occurences.length)
				.mapToObj(i -> new OccurencePair(i, occurences[i]))
				.filter(i -> i.count > 0)
				.sorted(Comparator.comparingLong(a -> -a.count))
				.map(i -> i.color)
				.collect(Collectors.toList());
	}
}
