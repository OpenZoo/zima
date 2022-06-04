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
package pl.asie.tinyzooconv.oop;

import pl.asie.libzxt.zzt.ZxtWorld;
import pl.asie.libzzt.EngineDefinition;
import pl.asie.libzzt.oop.OopProgram;
import pl.asie.zima.binconv.BinconvGlobalConfig;
import pl.asie.zima.binconv.BinconvPlatform;

import java.util.ArrayList;
import java.util.List;

public class OopTransformers implements OopTransformer {
	public static final OopTransformers INSTANCE = new OopTransformers();

	private final List<OopTransformer> transformers = new ArrayList<>();

	public OopTransformers() {
		transformers.add(OopRemoveNoOps.builder().build());
		transformers.add(OopInlineIfExts.builder().build());
		transformers.add(OopTZTextWrapper.builder().wordWrapWidth(BinconvGlobalConfig.PLATFORM.getTextWindowWidth()).build());
	}


	@Override
	public void apply(EngineDefinition definition, ZxtWorld world, OopProgram program) {
		for (OopTransformer t : this.transformers) {
			t.apply(definition, world, program);
		}
	}
}
