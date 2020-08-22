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
package pl.asie.zzttools.zima.gui;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SimpleCanvas extends JComponent {
	@Getter
	@Setter
	private boolean centered;
	private BufferedImage image;

	public SimpleCanvas(boolean centered) {
		this.centered = centered;
	}

	@Override
	public void paintComponent(Graphics graphics) {
		if (image != null) {
			Dimension size = getSize();
			if (image.getWidth() == size.width && image.getHeight() == size.height) {
				graphics.drawImage(image, 0, 0, null);
			} else {
				Graphics2D g2d = (Graphics2D) graphics.create();
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				if (centered && image.getWidth() <= size.width && image.getHeight() <= size.height) {
					g2d.drawImage(image, (size.width - image.getWidth()) / 2, (size.height - image.getHeight()) / 2,null);
				} else {
					// scaled
					// TODO: preserve aspect ratio
					g2d.drawImage(image, 0, 0, size.width, size.height, 0, 0, image.getWidth(), image.getHeight(), null);
				}
				g2d.dispose();
			}
		}
	}

	public void setImage(BufferedImage image) {
		this.image = image;
		repaint();
	}

	public BufferedImage getImage() {
		return this.image;
	}
}
