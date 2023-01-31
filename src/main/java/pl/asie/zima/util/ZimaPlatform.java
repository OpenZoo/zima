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
package pl.asie.zima.util;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import pl.asie.libzzt.ElementLibrary;
import pl.asie.libzzt.ElementLibraryNull;
import pl.asie.libzzt.EngineDefinition;
import pl.asie.libzzt.TextVisualData;
import pl.asie.libzzt.WeaveZZTPlatformData;

import java.io.IOException;

@SuppressWarnings("ConstantConditions")
@Getter
@Setter
@ToString
@With
@Builder
public final class ZimaPlatform {
    @Builder.Default
    private final EngineDefinition zztEngineDefinition = null;
    @Builder.Default
    private final int defaultBoardWidth = -1;
    @Builder.Default
    private final int defaultBoardHeight = -1;
    @Builder.Default
    private final boolean doubleWide = false;
    @Builder.Default
    private final boolean supportsBlinking = true;

    public static final ZimaPlatform ZZT;
    public static final ZimaPlatform SUPER_ZZT;
    public static final ZimaPlatform SUPER_CLASSICZOO;
    public static final ZimaPlatform WEAVE_ZZT_25;
    public static final ZimaPlatform WEAVE_ZZT_30;
    public static final ZimaPlatform MEGAZEUX;

    public int getDefaultBoardWidth() {
        return defaultBoardWidth < 0 ? zztEngineDefinition.getBoardWidth() : defaultBoardWidth;
    }

    public int getDefaultBoardHeight() {
        return defaultBoardHeight < 0 ? zztEngineDefinition.getBoardHeight() : defaultBoardHeight;
    }

    public boolean isDoubleWide(TextVisualData visual) {
        return isDoubleWide() && visual.getCharHeight() >= (visual.getCharWidth() * 3 / 2);
    }

    public boolean isUsesBoard() {
        return this.zztEngineDefinition != null;
    }

    public int getBoardWidth() {
        return this.zztEngineDefinition != null ? this.zztEngineDefinition.getBoardWidth() : 65535;
    }

    public int getBoardHeight() {
        return this.zztEngineDefinition != null ? this.zztEngineDefinition.getBoardHeight() : 65535;
    }

    public ElementLibrary getLibrary() {
        return this.zztEngineDefinition != null ? this.zztEngineDefinition.getElements() : ElementLibraryNull.INSTANCE;
    }

    static {
        ZZT = ZimaPlatform.builder().zztEngineDefinition(EngineDefinition.zzt()).build();
        SUPER_ZZT = ZimaPlatform.builder().zztEngineDefinition(EngineDefinition.superZzt()).doubleWide(true).build();
        SUPER_CLASSICZOO = ZimaPlatform.builder().zztEngineDefinition(EngineDefinition.superClassicZoo()).doubleWide(true).build();
        MEGAZEUX = ZimaPlatform.builder().defaultBoardWidth(80).defaultBoardHeight(25).supportsBlinking(false).build();

        try {
            WEAVE_ZZT_25 = ZimaPlatform.builder().zztEngineDefinition(WeaveZZTPlatformData.apply(EngineDefinition.zzt(), null, WeaveZZTPlatformData.Version.V2_5)).build();
            WEAVE_ZZT_30 = ZimaPlatform.builder().zztEngineDefinition(WeaveZZTPlatformData.apply(EngineDefinition.zzt(), null, WeaveZZTPlatformData.Version.V3_0)).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
