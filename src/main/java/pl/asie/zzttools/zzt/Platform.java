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

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Platform {
    private final int boardWidth;
    private final int boardHeight;
    private final int maxBoardSize; // excl. size byte
    private final int maxStatCount;
    @Builder.Default
    private final boolean doubleWide = false;
    private final ElementLibrary library;

    public static final Platform ZZT;
    public static final Platform SUPER_ZZT;

    static {
        ZZT = Platform.builder().boardWidth(60).boardHeight(25).maxBoardSize(20000).maxStatCount(150).library(ElementLibraryZZT.INSTANCE).build();
        SUPER_ZZT = Platform.builder().boardWidth(96).boardHeight(80).maxBoardSize(20000).maxStatCount(128).library(ElementLibrarySuperZZT.INSTANCE).doubleWide(true).build();
    }
}
