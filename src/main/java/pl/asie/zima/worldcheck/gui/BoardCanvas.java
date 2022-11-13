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
package pl.asie.zima.worldcheck.gui;

import lombok.Getter;
import lombok.Setter;
import pl.asie.libzzt.Board;
import pl.asie.libzzt.TextVisualData;
import pl.asie.libzzt.TextVisualRenderer;
import pl.asie.zima.util.ZimaPlatform;
import pl.asie.zima.util.gui.SimpleCanvas;
import pl.asie.zima.worldcheck.ElementLocation;

import java.awt.*;
import java.util.List;

public class BoardCanvas extends SimpleCanvas {
	@Getter
	private TextVisualData visualData;
	private final ZimaPlatform platform;
	@Getter
	private Board board;
	@Getter
	@Setter
	private List<ElementLocation> highlights;

	public BoardCanvas(boolean isInPane, TextVisualData visualData, ZimaPlatform platform) {
		super(isInPane);
		this.visualData = visualData;
		this.platform = platform;
	}

	@Override
	protected void paintOverlay(Graphics2D g2d, int xPos, int yPos, float xScale, float yScale) {
		if (highlights != null) {
			Color color = Color.getHSBColor((System.currentTimeMillis() % 2600) / 2600.0f, 0.5f, 0.95f);
			g2d.setColor(color);
			for (ElementLocation el : highlights) {
				int xStart = (int) (xPos + (el.getXPos() - 1) * visualData.getCharWidth() * xScale);
				int yStart = (int) (yPos + (el.getYPos() - 1) * visualData.getCharHeight() * yScale);
				int xEnd = (int) (xPos + (el.getXPos()) * visualData.getCharWidth() * xScale);
				int yEnd = (int) (yPos + (el.getYPos()) * visualData.getCharHeight() * yScale);

				g2d.drawRect(xStart, yStart, xEnd - xStart, yEnd - yStart);
			}
		}
	}

	private void redrawBoard() {
		if (board == null) {
			setImage(null);
		} else {
			TextVisualRenderer renderer = new TextVisualRenderer(visualData, platform.isDoubleWide());
			setImage(renderer.render(board, false));
		}
	}

	public void setVisualData(TextVisualData visualData) {
		this.visualData = visualData;
		redrawBoard();
	}

	public void setBoard(Board board) {
		if (this.board != board) {
			this.board = board;
			this.highlights = null;
			redrawBoard();
		}
	}
}
