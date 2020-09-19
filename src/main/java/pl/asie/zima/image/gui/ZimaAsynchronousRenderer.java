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
package pl.asie.zima.image.gui;

import lombok.Getter;
import lombok.Setter;
import pl.asie.zima.util.CountOutputStream;
import pl.asie.zima.util.Pair;
import pl.asie.libzzt.Board;
import pl.asie.libzzt.ZOutputStream;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class ZimaAsynchronousRenderer {
	private final ZimaFrontendSwing parent;
	private Board outputBoard;
	private BufferedImage outputImage;
	private BufferedImage outputPreviewImage;

	private final Object stateLock = new Object();
	private final Object outputWriteLock = new Object();
	private Thread workThread = null;
	@SuppressWarnings("FieldCanBeLocal")
	private Thread workThreadFast;
	private boolean queued = false;
	private boolean queuedFast = false;

	@Getter
	@Setter
	private boolean useFastPreview;

	public ZimaAsynchronousRenderer(ZimaFrontendSwing parent) {
		this.parent = parent;

		this.workThreadFast = new Thread(this::rerenderFastSync);
		this.workThreadFast.start();
	}

	private void rerenderOnce(ZimaConversionProfile profile, boolean fast) {
		BufferedImage image = this.parent.getInputImage();
		if (image != null) {
			if (!fast) {
				this.parent.getRenderProgress().setValue(0);
			}

			Pair<Board, BufferedImage> output = profile.convert(image, !fast ? ((max) -> {
				this.parent.getRenderProgress().setMaximum(max);
				this.parent.getRenderProgress().setValue(this.parent.getRenderProgress().getValue() + 1);
			}) : ((max) -> {}), fast);

			synchronized (outputWriteLock) {
				if (fast) {
					outputPreviewImage = output.getSecond();
				} else {
					if (!this.queued || !useFastPreview) {
						outputBoard = output.getFirst();
						outputImage = output.getSecond();
					}
				}
				this.parent.updateCanvas();
			}

			if (!fast) {
				// calculate board data
				int statCount = output.getFirst().getStats().size() - 1;
				int boardSize = -1;

				try (CountOutputStream cos = new CountOutputStream(); ZOutputStream stream = new ZOutputStream(cos, output.getFirst().getPlatform())) {
					output.getFirst().writeZ(stream);
					boardSize = cos.getCount();
				} catch (IOException e) {
					// pass
				}

				this.parent.getStatusLabel().setText(String.format("%d bytes, %d stats", boardSize, statCount));
			}
		} else {
			if (fast) {
				outputPreviewImage = null;
			} else {
				outputBoard = null;
				outputImage = null;
			}
			this.parent.updateCanvas();
			this.parent.getStatusLabel().setText("Ready.");
		}
	}

	private void rerenderSync() {
		synchronized (outputWriteLock) {
			outputBoard = null;
			outputImage = null;
		}

		while (true) {
			synchronized (this.stateLock) {
				if (this.queued) {
					this.queued = false;
				} else {
					// work completed
					return;
				}
			}

			// debounce
			try {
				Thread.sleep(75);
			} catch (InterruptedException e) {
				// pass
			}

			if (this.queued) {
				// parameters changed
				continue;
			}

			try {
				rerenderOnce(parent.getProfile(), false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void rerenderFastSync() {
		while (true) {
			if (!useFastPreview) {
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					// pass
				}
				continue;
			}

			boolean queued = false;
			synchronized (this.stateLock) {
				if (this.queuedFast) {
					queued = true;
					this.queuedFast = false;
				}
			}

			if (queued) {
				try {
					parent.getProfile().getProperties().copyTo(parent.getProfileFast().getProperties());
					rerenderOnce(parent.getProfileFast(), true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					Thread.sleep(32);
				} catch (InterruptedException e) {
					// pass
				}
			}
		}
	}

	public void popQueue() {
		synchronized (this.stateLock) {
			if (queued) {
				queuedFast = true;

				if (workThread == null || !workThread.isAlive()) {
					workThread = new Thread(this::rerenderSync);
					workThread.start();
				}
			}
		}
	}

	public void rerender() {
		synchronized (this.stateLock) {
			if (!queued) {
				queued = true;
			}
		}
	}

	public Board getOutputBoard() {
		synchronized (outputWriteLock) {
			return outputBoard;
		}
	}

	public BufferedImage getOutputImage() {
		synchronized (outputWriteLock) {
			return (outputImage == null && useFastPreview) ? outputPreviewImage : outputImage;
		}
	}
}
