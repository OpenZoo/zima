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

@Getter
@RequiredArgsConstructor
public enum LinterMessageType {
	OOP_PARSE_ERROR(LinterMessage.Severity.ERROR),
	INVALID_ELEMENT(LinterMessage.Severity.ERROR),
	FLAG_SET_BUT_NOT_USED(LinterMessage.Severity.WARNING),
	FLAG_SET_BUT_NOT_CHECKED(LinterMessage.Severity.WARNING),
	FLAG_SET_BUT_NOT_CLEARED(LinterMessage.Severity.INFO),
	FLAG_USED_BUT_NOT_SET(LinterMessage.Severity.HINT),
	FLAG_CHECKED_BUT_NOT_SET(LinterMessage.Severity.WARNING),
	FLAG_CLEARED_BUT_NOT_SET(LinterMessage.Severity.HINT),
	FLAG_TOO_MANY_MIGHT_BE_SET(LinterMessage.Severity.HINT),
	LABEL_SENT_BUT_NOT_PRESENT(LinterMessage.Severity.WARNING),
	LABEL_ZAPPED_BUT_NOT_PRESENT(LinterMessage.Severity.WARNING),
	LABEL_RESTORED_BUT_NOT_PRESENT(LinterMessage.Severity.WARNING),
	LABEL_SENT_TO_MISSING_TARGET(LinterMessage.Severity.WARNING),
	LABEL_ZAPPED_ON_MISSING_TARGET(LinterMessage.Severity.WARNING),
	LABEL_RESTORED_ON_MISSING_TARGET(LinterMessage.Severity.WARNING),
	LABEL_PRESENT_BUT_NOT_SENT(LinterMessage.Severity.INFO),
	LABEL_SENT_BUT_NOT_RECEIVED(LinterMessage.Severity.WARNING),
	LABEL_ZAP_RESTORE_RESTART(LinterMessage.Severity.INFO),
	PERFORMANCE_BUSYLOOP_FOUND(LinterMessage.Severity.WARNING),
	PERFORMANCE_NOOP_FOUND(LinterMessage.Severity.WARNING);

	private final LinterMessage.Severity severity;
}
