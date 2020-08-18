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

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ElementsZZT {
    public static final Element EMPTY = Element.builder().character(' ').color(0x70).pushable(true).walkable(true).name("Empty").id(0).build();
    public static final Element BOARD_EDGE = Element.builder().id(1).build();
    public static final Element MESSAGE_TIMER = Element.builder().id(2).build();
    public static final Element MONITOR = Element.builder().character(' ').color(0x07).cycle(1).name("Monitor").id(3).build();
    public static final Element PLAYER = Element.builder().character(2).color(0x1F).destructible(true).pushable(true).visibleInDark(true).cycle(1).name("Player").id(4).build();
    public static final Element AMMO = Element.builder().character(132).color(0x03).pushable(true).name("Ammo").id(5).build();
    public static final Element TORCH = Element.builder().character(157).color(0x06).visibleInDark(true).name("Torch").id(6).build();
    public static final Element GEM = Element.builder().character(4).pushable(true).destructible(true).name("Gem").id(7).build();
    public static final Element KEY = Element.builder().character(12).pushable(true).name("Key").id(8).build();
    public static final Element DOOR = Element.builder().character(10).color(Element.COLOR_WHITE_ON_CHOICE).name("Door").id(9).build();
    public static final Element SCROLL = Element.builder().character(232).color(0x0F).pushable(true).cycle(1).name("Scroll").id(10).build();
    public static final Element PASSAGE = Element.builder().character(240).color(Element.COLOR_WHITE_ON_CHOICE).cycle(0).visibleInDark(true).name("Passage").id(11).build();
    public static final Element DUPLICATOR = Element.builder().character(250).color(0x0F).cycle(2).hasDrawProc(true).name("Duplicator").id(12).build();
    public static final Element BOMB = Element.builder().character(11).hasDrawProc(true).pushable(true).cycle(6).name("Bomb").id(13).build();
    public static final Element ENERGIZER = Element.builder().character(127).color(0x05).name("Energizer").id(14).build();
    public static final Element STAR = Element.builder().character('S').color(0x0F).destructible(false).cycle(1).hasDrawProc(true).name("Star").id(15).build();
    public static final Element CONVEYOR_CW = Element.builder().character('/').cycle(3).hasDrawProc(true).name("Clockwise").id(16).build();
    public static final Element CONVEYOR_CCW = Element.builder().character('\\').cycle(2).hasDrawProc(true).name("Counter").id(17).build();
    public static final Element BULLET = Element.builder().character(248).color(0x0F).destructible(true).cycle(1).name("Bullet").id(18).build();
    public static final Element WATER = Element.builder().character(176).color(0xF9).placeableOnTop(true).name("Water").id(19).build();
    public static final Element FOREST = Element.builder().character(176).color(0x20).walkable(false).name("Forest").id(20).build();
    public static final Element SOLID = Element.builder().character(219).name("Solid").id(21).build();
    public static final Element NORMAL = Element.builder().character(178).name("Normal").id(22).build();
    public static final Element BREAKABLE = Element.builder().character(177).name("Breakable").id(23).build();
    public static final Element BOULDER = Element.builder().character(254).pushable(true).name("Boulder").id(24).build();
    public static final Element SLIDER_NS = Element.builder().character(18).name("Slider (NS)").id(25).build();
    public static final Element SLIDER_EW = Element.builder().character(29).name("Slider (EW)").id(26).build();
    public static final Element FAKE = Element.builder().character(178).name("Fake").id(27).build();
    public static final Element INVISIBLE = Element.builder().character(' ').name("Invisible").id(28).build();
    public static final Element BLINK_WALL = Element.builder().character(206).cycle(1).hasDrawProc(true).name("Blink wall").id(29).build();
    public static final Element TRANSPORTER = Element.builder().character(197).hasDrawProc(true).cycle(2).name("Transporter").id(30).build();
    public static final Element LINE = Element.builder().character(206).hasDrawProc(true).name("Line").id(31).build();
    public static final Element RICOCHET = Element.builder().character('*').color(0x0A).name("Ricochet").id(32).build();
    public static final Element BLINK_RAY_EW = Element.builder().character(205).id(33).build();
    public static final Element BEAR = Element.builder().character(153).color(0x06).destructible(true).pushable(true).cycle(3).name("Bear").scoreValue(1).id(34).build();
    public static final Element RUFFIAN = Element.builder().character(5).color(0x0D).destructible(true).pushable(true).cycle(1).name("Ruffian").scoreValue(2).id(35).build();
    public static final Element OBJECT = Element.builder().character(2).cycle(3).hasDrawProc(true).name("Object").id(36).build();
    public static final Element SLIME = Element.builder().character('*').color(Element.COLOR_CHOICE_ON_BLACK).destructible(false).cycle(3).name("Slime").id(37).build();
    public static final Element SHARK = Element.builder().character('^').color(0x07).destructible(false).cycle(3).name("Shark").id(38).build();
    public static final Element SPINNING_GUN = Element.builder().character(24).cycle(2).hasDrawProc(true).name("Spinning gun").id(39).build();
    public static final Element PUSHER = Element.builder().character(16).color(Element.COLOR_CHOICE_ON_BLACK).hasDrawProc(true).cycle(4).name("Pusher").id(40).build();
    public static final Element LION = Element.builder().character(234).color(0x0C).destructible(true).pushable(true).cycle(2).name("Lion").scoreValue(1).id(41).build();
    public static final Element TIGER = Element.builder().character(227).color(0x0B).destructible(true).pushable(true).cycle(2).name("Lion").scoreValue(2).id(42).build();
    public static final Element BLINK_RAY_NS = Element.builder().character(186).id(43).build();
    public static final Element CENTIPEDE_HEAD = Element.builder().character(233).destructible(true).cycle(2).name("Head").scoreValue(1).id(44).build();
    public static final Element CENTIPEDE_SEGMENT = Element.builder().character('O').destructible(true).cycle(2).name("Segment").scoreValue(3).id(45).build();
    public static final Element UNKNOWN_46 = Element.builder().id(46).build();
    public static final Element TEXT_BLUE = Element.builder().id(47).build();
    public static final Element TEXT_GREEN = Element.builder().id(48).build();
    public static final Element TEXT_CYAN = Element.builder().id(49).build();
    public static final Element TEXT_RED = Element.builder().id(50).build();
    public static final Element TEXT_PURPLE = Element.builder().id(51).build();
    public static final Element TEXT_YELLOW = Element.builder().id(52).build();
    public static final Element TEXT_WHITE = Element.builder().id(53).build();

    private static final Map<Integer, Element> elementsById;
    private static final Map<Element, String> elementInternalNames;

    public static Element byId(int id) {
        return elementsById.getOrDefault(id, ElementsZZT.EMPTY);
    }

    public static String internalName(Element element) {
        return elementInternalNames.getOrDefault(element, "(unknown)");
    }

    static {
        List<Element> elements = Stream.of(ElementsZZT.class.getFields()).map(f -> {
            try {
                return (Element) f.get(null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        elementsById = elements.stream().collect(Collectors.toMap(Element::getId, a -> a));
        elementInternalNames = Stream.of(ElementsZZT.class.getFields()).collect(Collectors.toMap(f -> {
            try {
                return (Element) f.get(null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, Field::getName));
    }
}
