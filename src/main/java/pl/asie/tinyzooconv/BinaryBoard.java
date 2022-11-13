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

import lombok.Getter;
import pl.asie.tinyzooconv.exceptions.BinarySerializerException;
import pl.asie.tinyzooconv.exceptions.TooManyIdsException;
import pl.asie.libzzt.Board;
import pl.asie.libzzt.Element;
import pl.asie.libzzt.Stat;
import pl.asie.libzzt.oop.OopProgram;
import pl.asie.libzzt.oop.commands.OopCommand;
import pl.asie.libzzt.oop.commands.OopCommandLabel;
import pl.asie.zima.binconv.BinconvGlobalConfig;
import pl.asie.zima.binconv.BinconvPlatformGb;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class BinaryBoard implements BinarySerializable {
	final BinaryWorld parent;
	final Board board;
	private final BinaryProgramBoardContext programHelper;

	public BinaryBoard(BinaryWorld parent, Board board) throws TooManyIdsException {
		this.parent = parent;
		this.board = board;
		this.programHelper = new BinaryProgramBoardContext(this);
	}

	private int fixCentipedeStatId(int v) {
		if (v > board.getEngineDefinition().getMaxStatCount()) {
			return -1;
		} else if (v < -2) {
			return -2;
		} else {
			return v;
		}
	}

	@Override
	public void serialize(BinarySerializerOutput output) throws IOException, BinarySerializerException {
		BinaryProgramBoardContext programContext = new BinaryProgramBoardContext(this);

		// fancy RLE logic
		int ix = 1;
		int iy = 1;
		int rleCount = 1;
		Element rleElement = this.board.getElement(ix, iy);
		int rleColor = this.board.getColor(ix, iy);
		do {
			ix++;
			if (ix > this.board.getWidth()) {
				ix = 1;
				iy++;
			}

			if (this.board.getColor(ix, iy) == rleColor && this.board.getElement(ix, iy) == rleElement && rleCount < 255 && iy <= this.board.getHeight()) {
				rleCount++;
			} else {
				if (rleCount == 1) {
					output.writeByte(rleElement.getId() | 0x80);
					output.writeByte(rleColor);
				} else {
					output.writeByte(rleElement.getId());
					output.writeByte(rleColor);
					output.writeByte(rleCount);
				}
				rleElement = this.board.getElement(ix, iy);
				rleColor = this.board.getColor(ix, iy);
				rleCount = 1;
			}
		} while (iy <= this.board.getHeight());

		/* if (BinconvGlobalConfig.PLATFORM instanceof BinconvPlatformGb) {
			// zoo_gb_static_colors
			BoardColorPairCounter pairCounter = new BoardColorPairCounter(
					BinconvGlobalConfig.PLATFORM.getViewportWidth(),
					BinconvGlobalConfig.PLATFORM.getViewportHeight(),
					0x7F
			);
			pairCounter.addBoard(this.board);
			List<Integer> colors = pairCounter.getMostCommonColors();
			// game transition color must always be allocated dynamically
			colors.removeIf(f -> Objects.equals(f, 0x05));
			for (int i = 0; i < 3; i++) {
				if (i < colors.size()) {
					output.writeByte(colors.get(i));
				} else {
					output.writeByte(0xFF);
				}
			}
		} */

		// zoo_board_info_t
		output.writeByte(this.board.getMaxShots());
		output.writeByte((this.board.isDark() ? 1 : 0) | (this.board.isReenterWhenZapped() ? 2 : 0));
		for (int i = 0; i < 4; i++)
			output.writeByte(this.board.getNeighborBoards()[i]);
		output.writeByte(this.board.getStartPlayerX());
		output.writeByte(this.board.getStartPlayerY());
		output.writeShort(this.board.getTimeLimitSec());

		output.writeByte(this.board.getStats().size() - 1);

		for (Stat stat : this.board.getStats()) {
			stat.copyStatToStatId(this.board);
		}

		Map<Stat, Integer> statToDataOfs = new IdentityHashMap<>();
		List<Integer> dataOffsets = new ArrayList<>();
		Map<Integer, BinarySerializable> dataObjectPointers = new HashMap<>();

		for (Stat stat : this.board.getStats()) {
			OopProgram oopProgram = stat.getCode(this.board.getEngineDefinition());
			BinaryProgram program = oopProgram != null ? new BinaryProgram(programContext, oopProgram) : null;

			output.writeByte(stat.getX());
			output.writeByte(stat.getY());
			output.writeByte(stat.getStepX());
			output.writeByte(stat.getStepY());
			output.writeByte(stat.getCycle());
			output.writeByte(stat.getP1());
			output.writeByte(stat.getP2());
			output.writeByte(stat.getP3());
			output.writeByte(fixCentipedeStatId(stat.getFollower()));
			output.writeByte(fixCentipedeStatId(stat.getLeader()));
			output.writeByte(stat.getUnderElement() != null ? stat.getUnderElement().getId() : 0);
			output.writeByte(stat.getUnderColor());

			int dataOfs = 0xFFFF;
			boolean emitNewDataOfs = false;
			if (program != null) {
				if (stat.getBoundStat() != null) {
					Stat boundStat = stat.getBoundStat();
					while (boundStat.getBoundStat() != null) {
						boundStat = boundStat.getBoundStat();
					}
					dataOfs = statToDataOfs.get(boundStat);
				} else {
					dataOfs = dataOffsets.size();
					emitNewDataOfs = true;
				}
				program.prepare(output);
			}
			output.writeShort(dataOfs);
			int dataPos;
			if (stat.getDataPos() < 0) {
				dataPos = -1;
			} else if (stat.getDataPos() == 0) {
				dataPos = 0;
			}  else if (program != null) {
				Integer newDataPos = program.serializeProgramPosition(stat.getDataPos());
				if (newDataPos != null) {
					dataPos = newDataPos;
				} else {
						throw new RuntimeException("Unsupported data position: " + stat.getDataPos() + " (stat index " + this.board.getStatId(stat) + ")");
				}
			} else {
				dataPos = stat.getDataPos(); // ditto
			}
			output.writeShort(dataPos);

			if (emitNewDataOfs) {
				int dataOffsetsPos = dataOffsets.size();
				dataObjectPointers.put(dataOffsetsPos, program);
				for (int i = 0; i < output.getFarPointerSize(); i++) {
					dataOffsets.add(0);
				}
				dataOffsets.add(0); // obj flags/size
				// zapped flags
				int flagVals = 0;
				int i = 0;
				for (OopCommand command : program.getProgram().getCommands()) {
					if (command instanceof OopCommandLabel lbl) {
						if (lbl.isZapped()) {
							flagVals |= 1 << (i & 7);
						}
						i++;
						if ((i & 7) == 0) {
							dataOffsets.add(flagVals);
							flagVals = 0;
						}
					}
				}
				if ((i & 7) != 0) {
					dataOffsets.add(flagVals);
				}
				int dataOffsetsSize = dataOffsets.size() - dataOffsetsPos;
				if (dataOffsetsSize >= 128) {
					throw new RuntimeException("Too big data offset size: " + dataOffsetsSize);
				}
				dataOffsets.set(dataOffsetsPos + 3, dataOffsetsSize);
				statToDataOfs.put(stat, dataOffsetsPos);
			}
		}

		output.writeShort(dataOffsets.size());

		for (int i = 0; i < dataOffsets.size(); i++) {
			BinarySerializable farPointedObj = dataObjectPointers.get(i);
			if (farPointedObj != null) {
				output.writeFarPointerTo(farPointedObj);
				i += output.getFarPointerSize() - 1;
				continue;
			}
			output.writeByte(dataOffsets.get(i));
		}
	}
}
