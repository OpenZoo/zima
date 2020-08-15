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
import pl.asie.zzttools.util.FileUtils;
import pl.asie.zzttools.util.Pair;
import pl.asie.zzttools.zima.ImageConverterRules;
import pl.asie.zzttools.zima.ImageConverterRuleset;
import pl.asie.zzttools.zima.ImageMseCalculator;
import pl.asie.zzttools.zima.TrixImageMseCalculator;
import pl.asie.zzttools.zzt.Board;
import pl.asie.zzttools.zzt.PaletteLoaderUtils;
import pl.asie.zzttools.zzt.TextVisualData;
import pl.asie.zzttools.zzt.ZOutputStream;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;

public class ZimaFrontendSwing {
	private final JFrame window;
	private final JPanel mainPanel;
	private final JMenuBar menuBar;
	private final JMenu fileMenu, editMenu, helpMenu;
	private final JMenuItem openItem, saveBrdItem, savePngItem;
	private final JMenuItem copyItem ,pasteItem;
	private final JMenuItem aboutItem;
	private final JTabbedPane optionsPane;
	private final JPanel optionsBoardPanel;
	private final JPanel optionsImagePanel;
	private final JPanel optionsCharsetPanel;
	private final JPanel optionsPalettePanel;
	private final JPanel optionsAdvancedPanel;
	private final SimpleCanvas previewCanvas;
	@Getter
	private final JLabel statusLabel;
	@Getter
	private JProgressBar renderProgress;
	@Getter
	private CharacterSelector characterSelector;
	@Getter
	private PaletteSelector paletteSelector;

	// "Board" tab
	private JSpinner boardXEdit;
	private JSpinner boardYEdit;
	private JSpinner playerXEdit;
	private JSpinner playerYEdit;
	private JSpinner charWidthEdit;
	private JSpinner charHeightEdit;
	private JSpinner maxStatCountEdit;
	private JCheckBox blinkCharsEdit;
	private JComboBox<String> rulesetEdit;
	private final List<Pair<String, ImageConverterRuleset>> rulesetOptions = List.of(
			new Pair<>("Default", ImageConverterRules.RULES_UNSAFE_STATLESS),
			new Pair<>("Default (Clone-safe)", ImageConverterRules.RULES_SAFE_STATLESS),
			new Pair<>("Default (Elements only)", ImageConverterRules.RULES_SAFE),
			new Pair<>("Blocks", ImageConverterRules.RULES_BLOCKS),
			new Pair<>("Walkable", ImageConverterRules.RULES_WALKABLE)
	);
	private JSlider contrastReductionEdit;
	private JButton contrastReductionReset;
	private JSlider accurateApproximateEdit;
	private JButton accurateApproximateReset;

	// "Image" tab
	private JCheckBox showInputImageEdit;
	private JSlider brightnessEdit;
	private JButton brightnessReset;
	private JSlider contrastEdit;
	private JButton contrastReset;
	private JSlider saturationEdit;
	private JButton saturationReset;
	private JSpinner cropLeftEdit;
	private JSpinner cropRightEdit;
	private JSpinner cropTopEdit;
	private JSpinner cropBottomEdit;

	// "Advanced" tab
	private JCheckBox fastPreviewEdit;
	private JCheckBox allowFacesEdit;
	private JComboBox<String> mseConverterEdit;
	private final List<Pair<String, Function<ZimaConversionProfile, ImageMseCalculator>>> mseConverterOptions = List.of(
			new Pair<>("Trix", p -> new TrixImageMseCalculator(p.getVisual(), p.getContrastReduction(), p.getAccurateApproximate()))
	);

	private final byte[] defaultCharset;
	private final int[] defaultPalette;
	private byte[] charset;
	private int[] palette;
	private TextVisualData visual;

	private final String zimaVersion;

	@Getter
	private BufferedImage inputImage;
	@Getter
	private final ZimaConversionProfile profile;
	private final ZimaAsynchronousRenderer asyncRenderer;

	public ZimaFrontendSwing(byte[] defaultCharset, int[] defaultPalette, String zimaVersion) {
		this.zimaVersion = zimaVersion;
		this.defaultCharset = defaultCharset;
		this.defaultPalette = defaultPalette;

		this.profile = new ZimaConversionProfile();
		this.asyncRenderer = new ZimaAsynchronousRenderer(this);

		this.window = new JFrame("zima");
		this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.previewCanvas = new SimpleCanvas(true);
		this.mainPanel = new JPanel(new GridBagLayout());
		this.statusLabel = new JLabel("Ready.");
		addGridBag(this.mainPanel, this.previewCanvas, (c) -> { c.gridx = 0; c.gridy = 0; });
		addGridBag(this.mainPanel, this.optionsPane = new JTabbedPane(), (c) -> { c.gridx = 1; c.gridy = 0; c.gridheight = 2; });
		addGridBag(this.mainPanel, this.renderProgress = new JProgressBar(), (c) -> { c.gridx = 0; c.gridy = 1; });
		addGridBag(this.mainPanel, this.statusLabel, (c) -> { c.gridx = 0; c.gridy = 2; c.gridwidth = GridBagConstraints.REMAINDER; c.fill = GridBagConstraints.BOTH; c.anchor = GridBagConstraints.WEST; });

		this.optionsBoardPanel = new JPanel(new GridBagLayout());
		this.optionsPane.addTab("Board", new JScrollPane(this.optionsBoardPanel));
		this.optionsImagePanel = new JPanel(new GridBagLayout());
		this.optionsPane.addTab("Image", new JScrollPane(this.optionsImagePanel));
		this.optionsCharsetPanel = new JPanel(new GridBagLayout());
		this.optionsPane.addTab("Charset", new JScrollPane(this.optionsCharsetPanel));
		this.optionsPalettePanel = new JPanel(new GridBagLayout());
		this.optionsPane.addTab("Palette", new JScrollPane(this.optionsPalettePanel));
		this.optionsAdvancedPanel = new JPanel(new GridBagLayout());
		this.optionsPane.addTab("Advanced", new JScrollPane(this.optionsAdvancedPanel));

		this.optionsPane.addChangeListener((e) -> updateCanvas());

		this.window.setJMenuBar(this.menuBar = new JMenuBar());
		this.menuBar.add(this.fileMenu = new JMenu("File"));
		this.fileMenu.add(this.openItem = new JMenuItem("Open"));
		this.fileMenu.add(this.saveBrdItem = new JMenuItem("Save (.brd)"));
		this.fileMenu.add(this.savePngItem = new JMenuItem("Save (.png)"));

		this.menuBar.add(this.editMenu = new JMenu("Edit"));
		this.editMenu.add(this.copyItem = new JMenuItem("Copy preview"));
		this.editMenu.add(this.pasteItem = new JMenuItem("Paste"));

		this.menuBar.add(this.helpMenu = new JMenu("Help"));
		this.helpMenu.add(this.aboutItem = new JMenuItem("About"));

		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;

			appendTabRow(this.optionsBoardPanel, gbc, "Board X", this.boardXEdit = new JSpinner(boardCoordsModel(this.profile.getBoardX(), false)));
			rerenderAndSet(this.boardXEdit, this.profile::setBoardX);

			appendTabRow(this.optionsBoardPanel, gbc, "Board Y", this.boardYEdit = new JSpinner(boardCoordsModel(this.profile.getBoardY(), true)));
			rerenderAndSet(this.boardYEdit, this.profile::setBoardY);

			appendTabRow(this.optionsBoardPanel, gbc, "Width (chars)", this.charWidthEdit = new JSpinner(boardCoordsModel(this.profile.getCharsWidth(), false)));
			rerenderAndSet(this.charWidthEdit, this.profile::setCharsWidth);

			appendTabRow(this.optionsBoardPanel, gbc, "Height (chars)", this.charHeightEdit = new JSpinner(boardCoordsModel(this.profile.getCharsHeight(), true)));
			rerenderAndSet(this.charHeightEdit, this.profile::setCharsHeight);

			appendTabRow(this.optionsBoardPanel, gbc, "Player X", this.playerXEdit = new JSpinner(boardCoordsModel(this.profile.getPlayerX(), false)));
			rerenderAndSet(this.playerXEdit, this.profile::setPlayerX);

			appendTabRow(this.optionsBoardPanel, gbc, "Player Y", this.playerYEdit = new JSpinner(boardCoordsModel(this.profile.getPlayerY(), true)));
			rerenderAndSet(this.playerYEdit, this.profile::setPlayerY);

			appendTabRow(this.optionsBoardPanel, gbc, "Max. stats", this.maxStatCountEdit = new JSpinner(new SpinnerNumberModel(this.profile.getMaxStatCount(), 0, 150, 1)));
			rerenderAndSet(this.maxStatCountEdit, this.profile::setMaxStatCount);

			appendTabRow(this.optionsBoardPanel, gbc, "Skip blinkable", this.blinkCharsEdit = new JCheckBox());
			this.blinkCharsEdit.setSelected(this.profile.isColorsBlink());
			rerenderAndSet(this.blinkCharsEdit, this.profile::setColorsBlink);

			appendTabRow(this.optionsBoardPanel, gbc, "Elements", this.rulesetEdit = new JComboBox<>(this.rulesetOptions.stream().map(Pair::getFirst).toArray(String[]::new)));
			this.rulesetEdit.setSelectedIndex(0);
			this.profile.setRuleset(this.rulesetOptions.get(this.rulesetEdit.getSelectedIndex()).getSecond());
			this.rulesetEdit.addActionListener(rerenderAndCallA(() -> this.profile.setRuleset(this.rulesetOptions.get(this.rulesetEdit.getSelectedIndex()).getSecond())));

			float defAccurateApproximateValue = this.profile.getAccurateApproximate();
			appendTabRow(this.optionsBoardPanel, gbc, "Accurate/Approximate",
					this.accurateApproximateEdit = new JSlider(JSlider.HORIZONTAL, 0, 1000, (int) (defAccurateApproximateValue * 1000.0f)),
					this.accurateApproximateReset = new JButton("Reset"));
			this.accurateApproximateEdit.addChangeListener(rerenderAndCall(() -> this.profile.setAccurateApproximate(this.accurateApproximateEdit.getValue() / 1000.0f)));
			this.accurateApproximateReset.addActionListener((e) -> { this.profile.setAccurateApproximate(defAccurateApproximateValue); this.accurateApproximateEdit.setValue((int) (defAccurateApproximateValue * 1000.0f)); rerender(); });
		}

		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;

			appendTabRow(this.optionsImagePanel, gbc, "Show input image", this.showInputImageEdit = new JCheckBox());
			this.showInputImageEdit.setSelected(true);
			rerenderAndSet(this.showInputImageEdit, (a) -> {});

			appendTabRow(this.optionsImagePanel, gbc, "Brightness",
					this.brightnessEdit = new JSlider(JSlider.HORIZONTAL, -160, 160, 0),
					this.brightnessReset = new JButton("Reset"));
			this.brightnessEdit.addChangeListener(rerenderAndCall(() -> this.profile.setBrightness(this.brightnessEdit.getValue() / 255.0f)));
			this.brightnessReset.addActionListener((e) -> { this.profile.setBrightness(0.0f); this.brightnessEdit.setValue(0); rerender(); });

			appendTabRow(this.optionsImagePanel, gbc, "Contrast",
					this.contrastEdit = new JSlider(JSlider.HORIZONTAL, -240, 240, 0),
					this.contrastReset = new JButton("Reset"));
			this.contrastEdit.addChangeListener(rerenderAndCall(() -> this.profile.setContrast(this.contrastEdit.getValue() / 255.0f)));
			this.contrastReset.addActionListener((e) -> { this.profile.setContrast(0.0f); this.contrastEdit.setValue(0); rerender(); });

			appendTabRow(this.optionsImagePanel, gbc, "Saturation",
					this.saturationEdit = new JSlider(JSlider.HORIZONTAL, -240, 240, 0),
					this.saturationReset = new JButton("Reset"));
			this.saturationEdit.addChangeListener(rerenderAndCall(() -> this.profile.setSaturation(this.saturationEdit.getValue() / 240.0f)));
			this.saturationReset.addActionListener((e) -> { this.profile.setSaturation(0.0f); this.saturationEdit.setValue(0); rerender(); });

			appendTabRow(this.optionsImagePanel, gbc, "Crop left", this.cropLeftEdit = new JSpinner(new SpinnerNumberModel(0, 0, null, 1)));
			rerenderAndSet(this.cropLeftEdit, this.profile::setCropLeft);

			appendTabRow(this.optionsImagePanel, gbc, "Crop right", this.cropRightEdit = new JSpinner(new SpinnerNumberModel(0, 0, null, 1)));
			rerenderAndSet(this.cropRightEdit, this.profile::setCropRight);

			appendTabRow(this.optionsImagePanel, gbc, "Crop top", this.cropTopEdit = new JSpinner(new SpinnerNumberModel(0, 0, null, 1)));
			rerenderAndSet(this.cropTopEdit, this.profile::setCropTop);

			appendTabRow(this.optionsImagePanel, gbc, "Crop bottom", this.cropBottomEdit = new JSpinner(new SpinnerNumberModel(0, 0, null, 1)));
			rerenderAndSet(this.cropBottomEdit, this.profile::setCropBottom);
		}

		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;

			characterSelector = new CharacterSelector(this::rerender);

			// block faces by default
			characterSelector.setCharAllowed(1, false);
			characterSelector.setCharAllowed(2, false);

			gbc.insets = new Insets(2, 4, 2, 4);
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.fill = GridBagConstraints.BOTH;
			optionsCharsetPanel.add(characterSelector, gbc);

			// create toggle buttons
			gbc.gridy = 1;
			gbc.gridwidth = 1;
			gbc.gridx = GridBagConstraints.RELATIVE;

			appendCharSelToggle(optionsCharsetPanel, "All", gbc, 0, 255);
			appendCharSelToggle(optionsCharsetPanel, "Faces", gbc, 1, 2);
			gbc.gridy = 2;
			appendCharSelToggle(optionsCharsetPanel, "Blocks", gbc, 176, 178, 219, 219);
			appendCharSelToggle(optionsCharsetPanel, "Half-Blocks", gbc, 219, 223);
			gbc.gridy = 3;
			appendCharSelToggle(optionsCharsetPanel, "Lines", gbc, 179, 218);
			gbc.gridy = 4;
			appendCharSelButton(optionsCharsetPanel, "Load default", gbc, this::onLoadDefaultCharset);
			appendCharSelButton(optionsCharsetPanel, "Load custom", gbc, this::onLoadCustomCharset);
		}

		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;

			paletteSelector = new PaletteSelector(this::rerender);

			gbc.insets = new Insets(2, 4, 2, 4);
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.fill = GridBagConstraints.BOTH;
			optionsPalettePanel.add(paletteSelector, gbc);

			// create toggle buttons
			gbc.gridy = 1;
			gbc.gridwidth = 1;
			gbc.gridx = GridBagConstraints.RELATIVE;

			appendPalSelToggle(optionsPalettePanel, "All", gbc, 0, 15);
			gbc.gridy = 2;
			appendCharSelButton(optionsPalettePanel, "Load default", gbc, this::onLoadDefaultPalette);
			appendCharSelButton(optionsPalettePanel, "Load custom", gbc, this::onLoadCustomPalette);
		}

		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;

			appendTabRow(this.optionsAdvancedPanel, gbc, "Fast preview", this.fastPreviewEdit = new JCheckBox());
			this.fastPreviewEdit.setSelected(true);
			this.asyncRenderer.setUseFastPreview(this.fastPreviewEdit.isSelected());
			rerenderAndSet(this.fastPreviewEdit, this.asyncRenderer::setUseFastPreview);

			this.profile.setMseCalculatorFunction(this.mseConverterOptions.get(0).getSecond());
/*			appendTabRow(this.optionsAdvancedPanel, gbc, "Error calculator", this.mseConverterEdit = new JComboBox<>(this.mseConverterOptions.stream().map(Pair::getFirst).toArray(String[]::new)));
			this.mseConverterEdit.setSelectedIndex(0);
			this.mseConverterEdit.addActionListener(rerenderAndCallA(() -> this.profile.setMseCalculatorFunction(this.mseConverterOptions.get(this.mseConverterEdit.getSelectedIndex()).getSecond()))); */

			float defContrastReductionValue = this.profile.getContrastReduction();
			appendTabRow(this.optionsAdvancedPanel, gbc, "Tile contrast reduction",
					this.contrastReductionEdit = new JSlider(JSlider.HORIZONTAL, 0, 1000, (int) Math.sqrt(defContrastReductionValue * 10000000.0f)),
					this.contrastReductionReset = new JButton("Reset"));
			this.contrastReductionEdit.addChangeListener(rerenderAndCall(() -> this.profile.setContrastReduction((this.contrastReductionEdit.getValue() * this.contrastReductionEdit.getValue()) / 10000000.0f)));
			this.contrastReductionReset.addActionListener((e) -> { this.profile.setContrastReduction(defContrastReductionValue); this.contrastReductionEdit.setValue((int) Math.sqrt(defContrastReductionValue * 10000000.0f)); rerender(); });
		}

		updateVisual();

		this.openItem.addActionListener(this::onOpen);
		this.saveBrdItem.addActionListener((e) -> this.onSave(e, false));
		this.savePngItem.addActionListener((e) -> this.onSave(e, true));
		this.copyItem.addActionListener(this::onCopy);
		this.pasteItem.addActionListener(this::onPaste);
		this.aboutItem.addActionListener(this::onAbout);

		this.openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		this.saveBrdItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		this.savePngItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		this.copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
		this.pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));

		this.previewCanvas.setPreferredSize(new Dimension(480, 350));
		this.previewCanvas.setMinimumSize(this.previewCanvas.getPreferredSize());
		this.renderProgress.setPreferredSize(new Dimension(480, 20));
		this.renderProgress.setMinimumSize(this.renderProgress.getPreferredSize());
		this.optionsPane.setPreferredSize(new Dimension(480, 378));
		this.optionsPane.setMinimumSize(this.optionsPane.getPreferredSize());

		this.window.add(this.mainPanel);
		this.window.pack();
		this.window.setMinimumSize(this.window.getSize());
		this.window.setMaximumSize(this.window.getSize());
		this.window.setResizable(false);
		this.window.setVisible(true);
	}

	public void updateVisual() {
		if (this.charset == null) {
			this.charset = this.defaultCharset;
		}
		if (this.palette == null) {
			this.palette = this.defaultPalette;
		}
		this.visual = new TextVisualData(8, charset.length / 256, charset, palette);
		this.profile.setVisual(this.visual);
		this.characterSelector.setVisual(this.visual);
		this.paletteSelector.setVisual(this.visual);
		rerender();
	}

	public boolean isShowInputImage() {
		return this.optionsPane.getSelectedIndex() == 1 && this.showInputImageEdit.isSelected();
	}

	public void rerender() {
		this.asyncRenderer.rerender();
		if (!isShowInputImage()) {
			this.asyncRenderer.popQueue();
		} else {
			updateCanvas();
		}
	}

	public void updateCanvas() {
		if (isShowInputImage()) {
			this.profile.updateImage(inputImage);
			this.previewCanvas.setCentered(false);
			this.previewCanvas.setImage(this.profile.getFilteredImage());
		} else {
			this.previewCanvas.setCentered(true);
			this.previewCanvas.setImage(this.asyncRenderer.getOutputImage());
			this.asyncRenderer.popQueue();
		}
	}

	public void setInputImage(Image image) {
		if (!(image instanceof BufferedImage) || ((BufferedImage) image).getType() != BufferedImage.TYPE_INT_RGB) {
			BufferedImage inputImageFixed = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
			Graphics2D gfx = (Graphics2D) inputImageFixed.getGraphics();
			gfx.setColor(Color.BLACK);
			gfx.fillRect(0, 0, image.getWidth(null), image.getHeight(null));
			gfx.drawImage(image, 0, 0, null);
			gfx.dispose();
			inputImage = inputImageFixed;
		} else {
			inputImage = (BufferedImage) image;
		}
		rerender();
	}

	// Menu options

	public void onLoadDefaultCharset(ActionEvent event) {
		this.charset = null;
		updateVisual();
	}

	public void onLoadCustomCharset(ActionEvent event) {
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setFileFilter(new FileNameExtensionFilter("Character set file", "chr", "bin"));
		int returnVal = fc.showOpenDialog(this.window);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try (FileInputStream fis = new FileInputStream(fc.getSelectedFile())) {
				this.charset = FileUtils.readAll(fis);
				updateVisual();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this.window, "Error loading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void onLoadDefaultPalette(ActionEvent event) {
		this.palette = null;
		updateVisual();
	}

	public void onLoadCustomPalette(ActionEvent event) {
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.addChoosableFileFilter(new FileNameExtensionFilter("MegaZeux palette file", "pal"));
		fc.addChoosableFileFilter(new FileNameExtensionFilter("PLD palette file", "pld"));
		int returnVal = fc.showOpenDialog(this.window);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try (FileInputStream fis = new FileInputStream(fc.getSelectedFile())) {
				String name = fc.getSelectedFile().getName().toLowerCase(Locale.ROOT);
				int[] colors = null;
				if (name.endsWith(".pld")) {
					colors = PaletteLoaderUtils.readPldFile(fis);
				} else {
					// default - .pal
					colors = PaletteLoaderUtils.readPalFile(fis);
				}
				//noinspection ConstantConditions
				if (colors != null && colors.length == 16) {
					this.palette = colors;
				} else {
					JOptionPane.showMessageDialog(this.window, "Error loading file: invalid palette data", "Error", JOptionPane.ERROR_MESSAGE);
					this.palette = null;
				}
				updateVisual();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this.window, "Error loading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void onCopy(ActionEvent event) {
		Image outputImage = asyncRenderer.getOutputImage();
		if (outputImage != null) {
			try {
				TransferableImage transferableImage = new TransferableImage(outputImage);
				window.getToolkit().getSystemClipboard().setContents(transferableImage, (clipboard, contents) -> {
					// pass
				});
			} catch (Exception e) {
				// pass
			}
		}
	}

	public void onPaste(ActionEvent event) {
		try {
			Transferable clipboardContents = window.getToolkit().getSystemClipboard().getContents(null);
			if (clipboardContents != null && clipboardContents.isDataFlavorSupported(DataFlavor.imageFlavor)) {
				setInputImage((Image) clipboardContents.getTransferData(DataFlavor.imageFlavor));
			}
		} catch (Exception e) {
			// pass
		}
	}

	public void onAbout(ActionEvent event) {
		JOptionPane.showMessageDialog(this.window, "zima " + zimaVersion + " - Copyright (c) 2020 asie", "About", JOptionPane.INFORMATION_MESSAGE);
	}

	public void onOpen(ActionEvent event) {
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnVal = fc.showOpenDialog(this.window);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				setInputImage(ImageIO.read(fc.getSelectedFile()));
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this.window, "Error loading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void onSave(ActionEvent event, boolean png) {
		if (this.asyncRenderer.getOutputBoard() == null || this.asyncRenderer.getOutputImage() == null) {
			return;
		}

		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setFileFilter(png ? new FileNameExtensionFilter("PNG image file", "png") : new FileNameExtensionFilter("ZZT board file", ".brd"));
		int returnVal = fc.showSaveDialog(this.window);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String extension = png ? ".png" : ".brd";
			File file = fc.getSelectedFile();
			if (!file.getName().toLowerCase(Locale.ROOT).endsWith(extension)) {
				file = new File(file.toString() + extension);
			}

			try {
				if (png) {
					ImageIO.write(this.asyncRenderer.getOutputImage(), "png", file);
				} else {
					try (FileOutputStream fos = new FileOutputStream(file); ZOutputStream zos = new ZOutputStream(fos, false)) {
						this.asyncRenderer.getOutputBoard().writeZ(zos);
					}
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this.window, "Error saving file: " + e.getMessage());
			}
		}
	}

	// Re-render call listeners

	public ActionListener rerenderAndCallA(Runnable callFunc) {
		return (e) -> {
			callFunc.run();
			rerender();
		};
	}

	public ChangeListener rerenderAndCall(Runnable callFunc) {
		return (e) -> {
			callFunc.run();
			rerender();
		};
	}

	public void rerenderAndSet(JSpinner spinner, IntConsumer valueConsumer) {
		spinner.addChangeListener(rerenderAndCall(() -> valueConsumer.accept(((Number) spinner.getValue()).intValue())));
	}

	public void rerenderAndSet(JCheckBox box, Consumer<Boolean> valueConsumer) {
		box.addActionListener(rerenderAndCallA(() -> valueConsumer.accept(box.isSelected())));
	}

	// Swing layout utils

	private void appendCharSelToggle(JPanel panel, String name, GridBagConstraints gbc, int... ranges) {
		appendCharSelButton(panel, name, gbc, (e) -> this.characterSelector.toggleCharAllowed(ranges));
	}

	private void appendPalSelToggle(JPanel panel, String name, GridBagConstraints gbc, int... ranges) {
		appendCharSelButton(panel, name, gbc, (e) -> this.paletteSelector.toggleColorAllowed(ranges));
	}

	private void appendCharSelButton(JPanel panel, String name, GridBagConstraints gbc, ActionListener al) {
		JButton toggle = new JButton(name);
		toggle.addActionListener(al);
		panel.add(toggle, gbc);
	}

	private void appendTabRow(JPanel panel, GridBagConstraints gbc, String label, Component... c) {
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(2, 4, 2, 4);
		panel.add(new JLabel(label), gbc);
		for (int i = 0; i < c.length; i++) {
			gbc.gridx++;
			if (i == c.length - 1) {
				gbc.gridwidth = GridBagConstraints.REMAINDER;
				gbc.fill = GridBagConstraints.BOTH;
			}
			panel.add(c[i], gbc);
		}
		gbc.gridx = 0;
		gbc.gridy += 1;
	}

	private void addGridBag(JPanel panel, Component c, Consumer<GridBagConstraints> gbcConsumer) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbcConsumer.accept(gbc);
		panel.add(c, gbc);
	}

	private static SpinnerNumberModel boardCoordsModel(int value, boolean height) {
		return new SpinnerNumberModel(value, 1, height ? Board.HEIGHT : Board.WIDTH, 1);
	}
}
