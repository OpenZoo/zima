/**
 * Copyright (c) 2020 Adrian Siekierka
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
package pl.asie.zzttools.zzt;

public enum Element {
	EMPTY("empty"),
	BOARD_EDGE,
	MESSAGE_TIMER,
	MONITOR,
	PLAYER("player"),
	AMMO("ammo"),
	TORCH("torch"),
	GEM("gem"),
	KEY("key"),
	DOOR("door"),
	SCROLL("scroll"),
	PASSAGE("passage"),
	DUPLICATOR("duplicator"),
	BOMB("bomb"),
	ENERGIZER("energizer"),
	STAR("star"),
	CONVEYOR_CW("clockwise"),
	CONVEYOR_CCW("counter"),
	BULLET("bullet"),
	WATER("water"),
	FOREST("forest"),
	SOLID("solid"),
	NORMAL("normal"),
	BREAKABLE("breakable"),
	BOULDER("boulder"),
	SLIDER_NS("sliderns"),
	SLIDER_EW("sliderew"),
	FAKE("fake"),
	INVISIBLE("invisible"),
	BLINK_WALL("blinkwall"),
	TRANSPORTER("transporter"),
	LINE("line"),
	RICOCHET("ricochet"),
	BLINK_RAY_EW,
	BEAR("bear"),
	RUFFIAN("ruffian"),
	OBJECT("object"),
	SLIME("slime"),
	SHARK("shark"),
	SPINNING_GUN("spinninggun"),
	PUSHER("pusher"),
	LION("lion"),
	TIGER("tiger"),
	BLINK_RAY_NS,
	CENTIPEDE_HEAD("head"),
	CENTIPEDE_SEGMENT("segment"),
	UNKNOWN_46,
	TEXT_BLUE,
	TEXT_GREEN,
	TEXT_CYAN,
	TEXT_RED,
	TEXT_PURPLE,
	TEXT_YELLOW,
	TEXT_WHITE;

	private final String oopName;

	Element() {
		this.oopName = null;
	}

	Element(String oopName) {
		this.oopName = oopName;
	}

	public String getOopName() {
		return oopName;
	}

	public static Element fromOrdinal(int v) {
		Element[] values = Element.values();
		return (v < values.length) ? values[v] : Element.EMPTY;
	}
}
