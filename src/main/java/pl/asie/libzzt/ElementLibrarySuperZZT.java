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
package pl.asie.libzzt;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ElementLibrarySuperZZT {
    private static final Element EMPTY = Element.builder().character(' ').color(0x70).pushable(true).walkable(true).name("Empty").id(0).build();
    private static final Element BOARD_EDGE = Element.builder().id(1).build();
    private static final Element MESSAGE_TIMER = Element.builder().id(2).build();
    private static final Element MONITOR = Element.builder().character(2).color(0x1F).cycle(1).pushable(true).name("Monitor").id(3).build();
    private static final Element PLAYER = Element.builder().character(2).color(0x1F).destructible(true).pushable(true).cycle(1).name("Player").id(4).build();
    private static final Element AMMO = Element.builder().character(132).color(0x03).pushable(true).name("Ammo").id(5).build();
    // 6
    private static final Element GEM = Element.builder().character(4).pushable(true).destructible(true).name("Gem").id(7).build();
    private static final Element KEY = Element.builder().character(12).pushable(true).name("Key").id(8).build();
    private static final Element DOOR = Element.builder().character(10).color(Element.COLOR_WHITE_ON_CHOICE).name("Door").id(9).build();
    private static final Element SCROLL = Element.builder().character(232).color(0x0F).pushable(true).cycle(1).name("Scroll").id(10).build();
    private static final Element PASSAGE = Element.builder().character(240).color(Element.COLOR_WHITE_ON_CHOICE).cycle(0).name("Passage").id(11).build();
    private static final Element DUPLICATOR = Element.builder().character(250).color(0x0F).cycle(2).hasDrawProc(true).name("Duplicator").id(12).build();
    private static final Element BOMB = Element.builder().character(11).hasDrawProc(true).pushable(true).cycle(6).name("Bomb").id(13).build();
    private static final Element ENERGIZER = Element.builder().character(127).color(0x05).name("Energizer").id(14).build();
    // 15
    private static final Element CONVEYOR_CW = Element.builder().character('/').cycle(3).hasDrawProc(true).name("Clockwise").id(16).build();
    private static final Element CONVEYOR_CCW = Element.builder().character('\\').cycle(2).hasDrawProc(true).name("Counter").id(17).build();
    // 18
    private static final Element LAVA = Element.builder().character('o').color(0x4E).placeableOnTop(true).name("Lava").id(19).build();
    private static final Element FOREST = Element.builder().character(176).color(0x20).walkable(false).name("Forest").id(20).build();
    private static final Element SOLID = Element.builder().character(219).name("Solid").id(21).build();
    private static final Element NORMAL = Element.builder().character(178).name("Normal").id(22).build();
    private static final Element BREAKABLE = Element.builder().character(177).name("Breakable").id(23).build();
    private static final Element BOULDER = Element.builder().character(254).pushable(true).name("Boulder").id(24).build();
    private static final Element SLIDER_NS = Element.builder().character(18).name("Slider (NS)").id(25).build();
    private static final Element SLIDER_EW = Element.builder().character(29).name("Slider (EW)").id(26).build();
    private static final Element FAKE = Element.builder().character(178).name("Fake").id(27).build();
    private static final Element INVISIBLE = Element.builder().character(' ').name("Invisible").id(28).build();
    private static final Element BLINK_WALL = Element.builder().character(206).cycle(1).hasDrawProc(true).name("Blink wall").id(29).build();
    private static final Element TRANSPORTER = Element.builder().character(197).hasDrawProc(true).cycle(2).name("Transporter").id(30).build();
    private static final Element LINE = Element.builder().character(206).hasDrawProc(true).name("Line").id(31).build();
    private static final Element RICOCHET = Element.builder().character('*').color(0x0A).name("Ricochet").id(32).build();
    // 33
    private static final Element BEAR = Element.builder().character(235).color(0x02).destructible(true).pushable(true).cycle(3).name("Bear").scoreValue(1).id(34).build();
    private static final Element RUFFIAN = Element.builder().character(5).color(0x0D).destructible(true).pushable(true).cycle(1).name("Ruffian").scoreValue(2).id(35).build();
    private static final Element OBJECT = Element.builder().character(2).cycle(3).hasDrawProc(true).name("Object").id(36).build();
    private static final Element SLIME = Element.builder().character('*').color(Element.COLOR_CHOICE_ON_BLACK).destructible(false).cycle(3).name("Slime").id(37).build();
    // 38
    private static final Element SPINNING_GUN = Element.builder().character(24).cycle(2).hasDrawProc(true).name("Spinning gun").id(39).build();
    private static final Element PUSHER = Element.builder().character(16).color(Element.COLOR_CHOICE_ON_BLACK).hasDrawProc(true).cycle(4).name("Pusher").id(40).build();
    private static final Element LION = Element.builder().character(234).color(0x0C).destructible(true).pushable(true).cycle(2).name("Lion").scoreValue(1).id(41).build();
    private static final Element TIGER = Element.builder().character(227).color(0x0B).destructible(true).pushable(true).cycle(2).name("Lion").scoreValue(2).id(42).build();
    // 43
    private static final Element CENTIPEDE_HEAD = Element.builder().character(233).destructible(true).cycle(2).name("Head").scoreValue(1).id(44).build();
    private static final Element CENTIPEDE_SEGMENT = Element.builder().character('O').destructible(true).cycle(2).name("Segment").scoreValue(3).id(45).build();
    private static final Element UNKNOWN_46 = Element.builder().id(46).build();
    private static final Element FLOOR = Element.builder().character(176).placeableOnTop(true).walkable(true).name("Floor").id(47).build();
    private static final Element WATER_N = Element.builder().character(30).color(0x19).placeableOnTop(true).walkable(true).name("Water N").id(48).build();
    private static final Element WATER_S = Element.builder().character(31).color(0x19).placeableOnTop(true).walkable(true).name("Water S").id(49).build();
    private static final Element WATER_W = Element.builder().character(17).color(0x19).placeableOnTop(true).walkable(true).name("Water W").id(50).build();
    private static final Element WATER_E = Element.builder().character(16).color(0x19).placeableOnTop(true).walkable(true).name("Water E").id(51).build();
    private static final Element ROTON = Element.builder().character(148).color(0x0D).destructible(true).pushable(true).cycle(1).name("Roton").scoreValue(2).id(59).build();
    private static final Element DRAGON_PUP = Element.builder().character(237).color(0x04).destructible(true).pushable(true).cycle(2).hasDrawProc(true).name("Dragon Pup").scoreValue(1).id(60).build();
    private static final Element PAIRER = Element.builder().character(229).color(0x01).destructible(true).pushable(true).cycle(2).name("Pairer").scoreValue(2).id(61).build();
    private static final Element SPIDER = Element.builder().character(15).color(0xFF).destructible(true).pushable(false).cycle(1).name("Spider").scoreValue(3).id(62).build();
    private static final Element WEB = Element.builder().character(197).color(Element.COLOR_CHOICE_ON_BLACK).placeableOnTop(true).walkable(true).hasDrawProc(true).name("Web").id(63).build();
    private static final Element STONE = Element.builder().character('Z').color(0x0F).pushable(false).cycle(1).hasDrawProc(true).name("Stone").id(64).build();
    private static final Element BULLET = Element.builder().character(248).color(0x0F).destructible(true).cycle(1).name("Bullet").id(69).build();
    private static final Element BLINK_RAY_EW = Element.builder().character(205).id(70).build();
    private static final Element BLINK_RAY_NS = Element.builder().character(186).id(71).build();
    private static final Element STAR = Element.builder().character('S').color(0x0F).destructible(false).cycle(1).hasDrawProc(true).name("Star").id(72).build();
    private static final Element TEXT_BLUE = Element.builder().id(73).textBackgroundColor(1).build();
    private static final Element TEXT_GREEN = Element.builder().id(74).textBackgroundColor(2).build();
    private static final Element TEXT_CYAN = Element.builder().id(75).textBackgroundColor(3).build();
    private static final Element TEXT_RED = Element.builder().id(76).textBackgroundColor(4).build();
    private static final Element TEXT_PURPLE = Element.builder().id(77).textBackgroundColor(5).build();
    private static final Element TEXT_YELLOW = Element.builder().id(78).textBackgroundColor(6).build();
    private static final Element TEXT_WHITE = Element.builder().id(79).textBackgroundColor(0).build();
    
    public static final ElementLibrary INSTANCE;
    
    static {
        List<Field> fields = Stream.of(ElementLibrarySuperZZT.class.getDeclaredFields())
                .filter(f -> f.getType() == Element.class).collect(Collectors.toList());
        List<String> names = fields.stream().map(Field::getName).collect(Collectors.toList());
        List<Element> elements = fields.stream().map(f -> {
            try {
                f.setAccessible(true);
                return (Element) f.get(null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
        INSTANCE = new ElementLibrary(names, elements);
    }
}
