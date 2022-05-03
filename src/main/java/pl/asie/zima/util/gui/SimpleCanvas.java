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
package pl.asie.zima.util.gui;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SimpleCanvas extends JComponent {
	@Getter
	private BufferedImage image;
	@Getter
	private boolean allowScaling;
	@Getter
	private boolean doubleWide;
	@Getter
	private boolean scrollable;

	public SimpleCanvas() {
		this.allowScaling = false;
	}

	private int imageWidth() {
		return this.image == null ? 0 : (this.image.getWidth() * (doubleWide ? 2 : 1));
	}

	private int imageHeight() {
		return this.image == null ? 0 : this.image.getHeight();
	}

	protected void paintOverlay(Graphics2D g2d, int xPos, int yPos, float xScale, float yScale) {

	}

	@Override
	public void paintComponent(Graphics graphics) {
		if (image != null) {
			Dimension size = allowScaling ? this.getParent().getSize() : this.getSize();
			Graphics2D g2d = (Graphics2D) graphics.create();
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			if (image.getWidth() == size.width && image.getHeight() == size.height) {
				g2d.drawImage(image, 0, 0, size.width, size.height, null);
				paintOverlay(g2d, 0, 0, 1.0f, 1.0f);
			} else {
				int xPos = (size.width - imageWidth()) / 2;
				int yPos = (size.height - imageHeight()) / 2;
				if (scrollable) {
					if (xPos < 0) xPos = 0;
					if (yPos < 0) yPos = 0;
				}
				if (!allowScaling || ((xPos >= 0) && (yPos >= 0))) {
					// centered
					g2d.drawImage(image, xPos, yPos, xPos + imageWidth(), yPos + imageHeight(), 0, 0, image.getWidth(), image.getHeight(), null);
					paintOverlay(g2d, xPos, yPos, imageWidth() / (float)this.image.getWidth(), imageHeight() / (float)this.image.getHeight());
				} else {
					// scaled
					// TODO: preserve aspect ratio
					g2d.drawImage(image, 0, 0, size.width, size.height, 0, 0, image.getWidth(), image.getHeight(), null);
					paintOverlay(g2d, 0, 0, size.width / (float)this.image.getWidth(), size.height / (float)this.image.getHeight());
				}
			}
			g2d.dispose();
		}
	}

	public void updateDimensions() {
		Dimension parentDim = allowScaling ? this.getParent().getSize() : this.getSize();
		Dimension dimension = (this.image == null || allowScaling) ? parentDim : new Dimension(imageWidth(), imageHeight());
		if (dimension.width < parentDim.width && dimension.height < parentDim.height) {
			dimension = parentDim;
		}
		this.setMinimumSize(dimension);
		this.setMaximumSize(dimension);
		this.setPreferredSize(dimension);
		this.getParent().revalidate();
		this.getParent().repaint();
	}

	public void setScrollable(boolean scrollable) {
		this.scrollable = scrollable;
	}

	public void setDoubleWide(boolean doubleWide) {
		this.doubleWide = doubleWide;
		updateDimensions();
	}

	public void setAllowScaling(boolean allowScaling) {
		this.allowScaling = allowScaling;
		updateDimensions();
	}

	public void setImage(BufferedImage image) {
		this.image = image;
		updateDimensions();
	}
}
