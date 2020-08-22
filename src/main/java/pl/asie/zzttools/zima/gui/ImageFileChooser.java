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
import pl.asie.zzttools.util.ImageUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageFileChooser extends JFileChooser {
	private static final BufferedImage LOADING_ICON = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
	private static final BufferedImage MISSING_IMAGE = LOADING_ICON;
	private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();
	private final Map<File, SoftReference<ImageCache>> thumbnailMap = new HashMap<>();

	public ImageFileChooser() {
		super();
		setFileView(new ThumbnailFileView());
		setFileFilter(new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes()));

		ThumbnailAccessory thumbnailAccessory = new ThumbnailAccessory();
		thumbnailAccessory.setPreferredSize(new Dimension(160, 120));
		setAccessory(thumbnailAccessory);
		addPropertyChangeListener((c) -> {
			thumbnailAccessory.repaint();
		});
	}

	private ImageCache getImageCache(File f) {
		SoftReference<ImageCache> ref = thumbnailMap.get(f);
		if (ref == null || ref.get() == null) {
			ImageCache cache = new ImageCache(f);
			ref = new SoftReference<>(cache);
			THREAD_POOL.submit(cache);
			thumbnailMap.put(f, ref);
		}
		return Objects.requireNonNull(ref.get());
	}

	private class ThumbnailAccessory extends JComponent {
		@Override
		public void paintComponent(Graphics graphics) {
			File file = ImageFileChooser.this.getSelectedFile();
			if (file != null) {
				BufferedImage image = getImageCache(file).image;
				if (image != null) {
					Dimension size = this.getSize();
					Graphics2D g2d = (Graphics2D) graphics.create();
					int xPos = (size.width - image.getWidth()) / 2;
					int yPos = (size.height - image.getHeight()) / 2;
					if ((xPos >= 0) && (yPos >= 0)) {
						// scrollable
						g2d.drawImage(image, xPos, yPos, null);
					} else {
						g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
						ImageUtils.drawScaled(image, (int) size.getWidth(), (int) size.getHeight(), g2d, true);
					}
					g2d.dispose();
				}
			}
		}

	}

	private class ImageCache implements Runnable {
		private final File imageFile;
		@Getter
		private final ImageIcon icon;
		@Getter
		private BufferedImage image;

		public ImageCache(File imageFile) {
			this.imageFile = imageFile;
			this.icon = new ImageIcon(LOADING_ICON);
		}

		@Override
		public void run() {
			try {
				this.image = ImageIO.read(imageFile);
			} catch (IOException e) {
				e.printStackTrace();
				this.image = MISSING_IMAGE;
			}

			BufferedImage scaledIconImage = ImageUtils.scale(this.image, 16, 16, true, null);
			this.icon.setImage(scaledIconImage);
			ImageFileChooser.this.repaint();
		}
	}

	private class ThumbnailFileView extends FileView {
		@Override
		public Icon getIcon(File f) {
			if (f.isDirectory()) {
				return null;
			}
			return getImageCache(f).icon;
		}
	}
}
