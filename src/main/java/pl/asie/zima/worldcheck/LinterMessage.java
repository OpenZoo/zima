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

import lombok.Data;
import lombok.With;

import java.util.ArrayList;
import java.util.List;

@Data
@With
public class LinterMessage implements Comparable<LinterMessage>, ElementLocationHolder {
	public enum Severity {
		ERROR, // fatal parsing/code errors
		WARNING, // these are probably wrong
		HINT, // these may be wrong
		INFO, // these are fine
		NONE
	}

	private final ElementLocation location;
	private final LinterMessageType type;
	private final String text;
	private final List<Integer> relevantPositions = new ArrayList<>();

	public LinterMessage(ElementLocation location, LinterMessageType type, String text) {
		this.location = location;
		this.type = type;
		this.text = text;
		if (this.location != null && this.location.getStatPosition() != null) {
			this.relevantPositions.add(this.location.getStatPosition());
		}
	}

	public Severity getSeverity() {
		return type != null ? type.getSeverity() : Severity.NONE;
	}

	@Override
	public int compareTo(LinterMessage o) {
		int result;
		result = getSeverity().ordinal() - o.getSeverity().ordinal();
		if (result != 0) return result;
		if (location != null && o.getLocation() != null) {
			result = location.compareTo(o.getLocation());
			if (result != 0) return result;
		} else if (location != null && o.getLocation() == null) {
			return -1;
		} else if (o.getLocation() != null) {
			return 1;
		}
		return text.compareTo(o.text);
	}

	@Override
	public String toString() {
		return toString(false);
	}

	public String toString(boolean showType) {
		if (getSeverity() == Severity.NONE) {
			return text;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(getSeverity().name());
		if (showType) {
			sb.append(" ").append(getType().name());
		}
		if (location != null) {
			sb.append(" [");
			if (location.getBoardId() != null) {
				sb.append("Board ").append(location.getBoardId());
				if (location.getStatId() != null) {
					sb.append(", stat ").append(location.getStatId());
				}
				if (location.getXPos() != null) {
					sb.append(" at ").append(location.getXPos()).append(", ").append(location.getYPos());
				}
			} else {
				sb.append("global");
			}
			sb.append("]");
		}
		return sb.append(": ").append(text).toString();
	}

}
