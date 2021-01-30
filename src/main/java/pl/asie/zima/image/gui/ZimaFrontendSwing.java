/**
 * Copyright (c) 2020, 2021 Adrian Siekierka
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import pl.asie.libzzt.Board;
import pl.asie.libzzt.Element;
import pl.asie.libzzt.PaletteLoaderUtils;
import pl.asie.libzzt.Platform;
import pl.asie.libzzt.TextVisualData;
import pl.asie.libzzt.ZOutputStream;
import pl.asie.zima.Version;
import pl.asie.zima.util.*;
import pl.asie.zima.image.*;
import pl.asie.zima.util.gui.ImageFileChooser;
import pl.asie.zima.util.gui.SimpleCanvas;
import pl.asie.zima.util.gui.TransferableImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.ref.SoftReference;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

public class ZimaFrontendSwing {
	private final Gson gson = new GsonBuilder().registerTypeAdapter(Element.class, ElementJsonSerdes.INSTANCE).create();
	private final JFrame window;
	private final JPanel mainPanel;
	private final JMenuBar menuBar;
	private final JMenu fileMenu, editMenu, profileMenu, helpMenu;
	private final JMenuItem openItem, saveBrdItem, saveMzmItem, savePngItem, closeItem;
	private final JMenuItem copyItem ,pasteItem;
	private final JMenuItem profileLoadItem, profileSaveItem;
	private final JMenuItem changelogItem, aboutItem;
	private final JTabbedPane optionsPane;
	private final JPanel optionsBoardPanel;
	private final JPanel optionsImagePanel;
	private final JPanel optionsElementsPanel;
	private final JPanel optionsCharsetPanel;
	private final JPanel optionsPalettePanel;
	private final JPanel optionsAdvancedPanel;
	private final SimpleCanvas previewCanvas;
	private final JScrollPane previewCanvasPane;
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
	private JLabel charRatioLabel;
	private JSpinner maxStatCountEdit;
	private JSpinner maxBoardSizeEdit;
	private JCheckBox blinkingDisabledEdit;
	private JComboBox<String> platformEdit;
	private final List<Pair<String, Platform>> platforms = List.of(
			new Pair<>("ZZT", Platform.ZZT),
			new Pair<>("Super ZZT", Platform.SUPER_ZZT),
			new Pair<>("MegaZeux", Platform.MEGAZEUX)
	);
	private JComboBox<String> rulesetEdit;
	private final Map<Platform, ImageConverterRuleset> allRulesRuleset = Map.of(
			Platform.ZZT, ImageConverterRulesZZT.ALL_RULES,
			Platform.SUPER_ZZT, ImageConverterRulesSuperZZT.ALL_RULES,
			Platform.MEGAZEUX, new ImageConverterRuleset(List.of())
	);
	private final Map<Platform, List<Pair<String, ImageConverterRuleset>>> rulesetOptions = Map.of(
			Platform.ZZT, List.of(
					new Pair<>("Default", ImageConverterRulesZZT.RULES_UNSAFE_STATLESS),
					new Pair<>("Default (Clone-safe)", ImageConverterRulesZZT.RULES_SAFE_STATLESS),
					new Pair<>("Default (Elements only)", ImageConverterRulesZZT.RULES_SAFE),
					new Pair<>("Blocks", ImageConverterRulesZZT.RULES_BLOCKS),
					new Pair<>("Walkable", ImageConverterRulesZZT.RULES_WALKABLE),
					new Pair<>("Custom", null)
			),
			Platform.SUPER_ZZT, List.of(
					new Pair<>("Default", ImageConverterRulesSuperZZT.RULES_UNSAFE_STATLESS),
					new Pair<>("Default (Clone-safe)", ImageConverterRulesSuperZZT.RULES_SAFE_STATLESS),
					new Pair<>("Default (Elements only)", ImageConverterRulesSuperZZT.RULES_SAFE),
					new Pair<>("Blocks", ImageConverterRulesSuperZZT.RULES_BLOCKS),
					new Pair<>("Walkable", ImageConverterRulesSuperZZT.RULES_WALKABLE),
					new Pair<>("Custom", null)
			),
			Platform.MEGAZEUX, List.of(
					new Pair<>("N/A", null)
			)
	);
	private Map<ElementRule, JCheckBox> rulesetBoxEdit = new HashMap<>();
	private ImageConverterRuleset customRuleset;
	private JSlider contrastReductionEdit;
	private JButton contrastReductionReset;
	private JSlider accurateApproximateEdit;
	private JButton accurateApproximateReset;

	// "Image" tab
	private JLabel imageDataLabel;
	private JCheckBox showInputImageEdit;
	private JComboBox<AspectRatioPreservationMode> aspectRatioEdit;
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
	@Getter
	private final ZimaConversionProfile profileFast;
	private final ZimaAsynchronousRenderer asyncRenderer;

	private boolean uiReady;

	public ZimaFrontendSwing(byte[] defaultCharset, int[] defaultPalette, String zimaVersion) {
		this.zimaVersion = zimaVersion;
		this.defaultCharset = defaultCharset;
		this.defaultPalette = defaultPalette;

		this.asyncRenderer = new ZimaAsynchronousRenderer(this);

		this.profile = new ZimaConversionProfile();
		this.profileFast = new ZimaConversionProfile();
		this.profile.getProperties().addGlobalChangeListener((k, v) -> rerender());
		// TODO: move both
		this.profile.getProperties().set(ZimaConversionProfile.PLATFORM, Platform.ZZT);
		this.profile.getProperties().set(ZimaConversionProfile.FAST_RULESET, ImageConverterRulesZZT.RULES_BLOCKS);

		this.window = new JFrame(Version.getCurrentWindowName("image converter"));
		this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.previewCanvas = new SimpleCanvas();
		this.previewCanvas.setScrollable(true);
		this.mainPanel = new JPanel(new GridBagLayout());
		this.statusLabel = new JLabel("Ready.");
		addGridBag(this.mainPanel, this.previewCanvasPane = new JScrollPane(this.previewCanvas), (c) -> { c.gridx = 0; c.gridy = 0; c.fill = GridBagConstraints.BOTH; c.weightx = 0.8; c.weighty = 1.0; });
		addGridBag(this.mainPanel, this.optionsPane = new JTabbedPane(), (c) -> { c.gridx = 1; c.gridy = 0; c.gridheight = 2; c.fill = GridBagConstraints.VERTICAL; });
		addGridBag(this.mainPanel, this.renderProgress = new JProgressBar(), (c) -> { c.gridx = 0; c.gridy = 1; c.fill = GridBagConstraints.HORIZONTAL; });
		addGridBag(this.mainPanel, this.statusLabel, (c) -> { c.gridx = 0; c.gridy = 2; c.gridwidth = GridBagConstraints.REMAINDER; c.fill = GridBagConstraints.HORIZONTAL; c.anchor = GridBagConstraints.WEST; });

		this.optionsBoardPanel = new JPanel(new GridBagLayout());
		this.optionsPane.addTab("Board", new JScrollPane(this.optionsBoardPanel));
		this.optionsImagePanel = new JPanel(new GridBagLayout());
		this.optionsPane.addTab("Image", new JScrollPane(this.optionsImagePanel));
		this.optionsElementsPanel = new JPanel(new GridBagLayout());
		this.optionsPane.addTab("Elements", new JScrollPane(this.optionsElementsPanel));
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
		this.fileMenu.add(this.saveMzmItem = new JMenuItem("Save (.mzm)"));
		this.fileMenu.add(this.closeItem = new JMenuItem("Close"));

		this.menuBar.add(this.editMenu = new JMenu("Edit"));
		this.editMenu.add(this.copyItem = new JMenuItem("Copy preview"));
		this.editMenu.add(this.pasteItem = new JMenuItem("Paste"));

		this.menuBar.add(this.profileMenu = new JMenu("Profile"));
		this.profileMenu.add(this.profileLoadItem = new JMenuItem("Load"));
		this.profileMenu.add(this.profileSaveItem = new JMenuItem("Save"));

		this.menuBar.add(this.helpMenu = new JMenu("Help"));
		this.helpMenu.add(this.changelogItem = new JMenuItem("Changelog"));
		this.helpMenu.add(this.aboutItem = new JMenuItem("About"));

		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;

			appendTabRow(this.optionsBoardPanel, gbc, "Platform",
					this.platformEdit = new JComboBox<>(this.platforms.stream().map(Pair::getFirst).toArray(String[]::new)));
			this.platformEdit.addActionListener((e) -> {
				Platform newPlatform = this.platforms.get(this.platformEdit.getSelectedIndex()).getSecond();
				this.profile.getProperties().set(ZimaConversionProfile.PLATFORM, newPlatform);
				this.profile.getProperties().set(ZimaConversionProfile.CHARS_WIDTH, newPlatform.getDefaultBoardWidth());
				this.profile.getProperties().set(ZimaConversionProfile.CHARS_HEIGHT, newPlatform.getDefaultBoardHeight());

				this.boardXEdit.setEnabled(newPlatform.isUsesBoard());
				this.boardYEdit.setEnabled(newPlatform.isUsesBoard());
				this.playerXEdit.setEnabled(newPlatform.isUsesBoard());
				this.playerYEdit.setEnabled(newPlatform.isUsesBoard());
				this.saveBrdItem.setEnabled(newPlatform.isUsesBoard());
				this.maxBoardSizeEdit.setEnabled(newPlatform.getMaxBoardSize() > 0);
				this.maxStatCountEdit.setEnabled(newPlatform.getMaxStatCount() > 0);
			});

			appendTabRow(this.optionsBoardPanel, gbc, "Board X", this.boardXEdit = new JSpinner(boardCoordsModel(1, false)));
			bindPropertyInt(this.profile.getProperties(), ZimaConversionProfile.BOARD_X, this.boardXEdit);
			this.profile.getProperties().addChangeListener(ZimaConversionProfile.PLATFORM, (k, v) -> this.boardXEdit.setModel(boardCoordsModel(((Number) this.boardXEdit.getValue()).intValue(), false)));

			appendTabRow(this.optionsBoardPanel, gbc, "Board Y", this.boardYEdit = new JSpinner(boardCoordsModel(1, true)));
			bindPropertyInt(this.profile.getProperties(), ZimaConversionProfile.BOARD_Y, this.boardYEdit);
			this.profile.getProperties().addChangeListener(ZimaConversionProfile.PLATFORM, (k, v) -> this.boardYEdit.setModel(boardCoordsModel(((Number) this.boardYEdit.getValue()).intValue(), true)));

			appendTabRow(this.optionsBoardPanel, gbc, "Width (chars)", this.charWidthEdit = new JSpinner(boardCoordsModel(60, false)));
			bindPropertyInt(this.profile.getProperties(), ZimaConversionProfile.CHARS_WIDTH, this.charWidthEdit);
			this.profile.getProperties().addChangeListener(ZimaConversionProfile.PLATFORM, (k, v) -> this.charWidthEdit.setModel(boardCoordsModel(((Number) this.charWidthEdit.getValue()).intValue(), false)));

			appendTabRow(this.optionsBoardPanel, gbc, "Height (chars)", this.charHeightEdit = new JSpinner(boardCoordsModel(25, true)));
			bindPropertyInt(this.profile.getProperties(), ZimaConversionProfile.CHARS_HEIGHT, this.charHeightEdit);
			this.profile.getProperties().addChangeListener(ZimaConversionProfile.PLATFORM, (k, v) -> this.charHeightEdit.setModel(boardCoordsModel(((Number) this.charHeightEdit.getValue()).intValue(), true)));

			appendTabRow(this.optionsBoardPanel, gbc, "Aspect ratio", this.charRatioLabel = new JLabel(""));
			this.profile.getProperties().addChangeListener(ZimaConversionProfile.CHARS_WIDTH, (k, v) -> updateCharRatioLabel());
			this.profile.getProperties().addChangeListener(ZimaConversionProfile.CHARS_HEIGHT, (k, v) -> updateCharRatioLabel());
			this.profile.getProperties().addChangeListener(ZimaConversionProfile.PLATFORM, (k, v) -> updateCharRatioLabel());
			this.profile.getProperties().addChangeListener(ZimaConversionProfile.VISUAL_DATA, (k, v) -> updateCharRatioLabel());

			appendTabRow(this.optionsBoardPanel, gbc, "Player X", this.playerXEdit = new JSpinner(boardCoordsModel(1, false)));
			bindPropertyInt(this.profile.getProperties(), ZimaConversionProfile.PLAYER_X, this.playerXEdit);
			this.profile.getProperties().addChangeListener(ZimaConversionProfile.PLATFORM, (k, v) -> this.playerXEdit.setModel(boardCoordsModel(((Number) this.playerXEdit.getValue()).intValue(), false)));

			appendTabRow(this.optionsBoardPanel, gbc, "Player Y", this.playerYEdit = new JSpinner(boardCoordsModel(1, true)));
			bindPropertyInt(this.profile.getProperties(), ZimaConversionProfile.PLAYER_Y, this.playerYEdit);
			this.profile.getProperties().addChangeListener(ZimaConversionProfile.PLATFORM, (k, v) -> this.playerYEdit.setModel(boardCoordsModel(((Number) this.playerYEdit.getValue()).intValue(), true)));

			appendTabRow(this.optionsBoardPanel, gbc, "Max. stats", this.maxStatCountEdit = new JSpinner(statCountModel(Platform.ZZT.getMaxStatCount())));
			bindPropertyInt(this.profile.getProperties(), ZimaConversionProfile.MAX_STAT_COUNT, this.maxStatCountEdit);
			this.profile.getProperties().addChangeListener(ZimaConversionProfile.PLATFORM, (k, v) -> this.maxStatCountEdit.setModel(statCountModel(((Number) this.maxStatCountEdit.getValue()).intValue())));

			appendTabRow(this.optionsBoardPanel, gbc, "Max. board size", this.maxBoardSizeEdit = new JSpinner(boardSizeModel(Platform.ZZT.getMaxBoardSize())));
			bindPropertyInt(this.profile.getProperties(), ZimaConversionProfile.MAX_BOARD_SIZE, this.maxBoardSizeEdit);
			this.profile.getProperties().addChangeListener(ZimaConversionProfile.PLATFORM, (k, v) -> this.maxBoardSizeEdit.setModel(boardSizeModel(((Number) this.maxBoardSizeEdit.getValue()).intValue())));

			appendTabRow(this.optionsBoardPanel, gbc, "Accurate/Approximate",
					this.accurateApproximateEdit = new JSlider(JSlider.HORIZONTAL, 0, 1000, 0),
					this.accurateApproximateReset = new JButton("Reset"));
			bindPropertyFloatScaled(this.profile.getProperties(), ZimaConversionProfile.TRIX_ACCURATE_APPROXIMATE, this.accurateApproximateEdit, 1000.0f);
			this.accurateApproximateReset.addActionListener((e) -> { this.profile.getProperties().reset(ZimaConversionProfile.TRIX_ACCURATE_APPROXIMATE); });
		}

		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;

			appendTabRow(this.optionsImagePanel, gbc, "Show input image", this.showInputImageEdit = new JCheckBox());
			this.showInputImageEdit.setSelected(true);
			this.showInputImageEdit.addItemListener((e) -> rerender());

			appendTabRow(this.optionsImagePanel, gbc, "Image info", this.imageDataLabel = new JLabel(""));

			appendTabRow(this.optionsImagePanel, gbc, "Aspect ratio", this.aspectRatioEdit = createEnumComboBox(AspectRatioPreservationMode.class));
			bindPropertyEnum(this.profile.getProperties(), ZimaConversionProfile.ASPECT_RATIO_PRESERVATION_MODE, this.aspectRatioEdit);

			appendTabRow(this.optionsImagePanel, gbc, "Brightness",
					this.brightnessEdit = new JSlider(JSlider.HORIZONTAL, -160, 160, 0),
					this.brightnessReset = new JButton("Reset"));
			bindPropertyFloatScaled(this.profile.getProperties(), ZimaConversionProfile.BRIGHTNESS, this.brightnessEdit, 255.0f);
			this.brightnessReset.addActionListener((e) -> { this.profile.getProperties().reset(ZimaConversionProfile.BRIGHTNESS); });

			appendTabRow(this.optionsImagePanel, gbc, "Contrast",
					this.contrastEdit = new JSlider(JSlider.HORIZONTAL, -240, 240, 0),
					this.contrastReset = new JButton("Reset"));
			bindPropertyFloatScaled(this.profile.getProperties(), ZimaConversionProfile.CONTRAST, this.contrastEdit, 255.0f);
			this.contrastReset.addActionListener((e) -> { this.profile.getProperties().reset(ZimaConversionProfile.CONTRAST); });

			appendTabRow(this.optionsImagePanel, gbc, "Saturation",
					this.saturationEdit = new JSlider(JSlider.HORIZONTAL, -240, 240, 0),
					this.saturationReset = new JButton("Reset"));
			bindPropertyFloatScaled(this.profile.getProperties(), ZimaConversionProfile.SATURATION, this.saturationEdit, 240.0f);
			this.saturationReset.addActionListener((e) -> { this.profile.getProperties().reset(ZimaConversionProfile.SATURATION); });

			appendTabRow(this.optionsImagePanel, gbc, "Crop left", this.cropLeftEdit = new JSpinner(new SpinnerNumberModel(0, 0, null, 1)));
			bindPropertyInt(this.profile.getProperties(), ZimaConversionProfile.CROP_LEFT, this.cropLeftEdit);

			appendTabRow(this.optionsImagePanel, gbc, "Crop right", this.cropRightEdit = new JSpinner(new SpinnerNumberModel(0, 0, null, 1)));
			bindPropertyInt(this.profile.getProperties(), ZimaConversionProfile.CROP_RIGHT, this.cropRightEdit);

			appendTabRow(this.optionsImagePanel, gbc, "Crop top", this.cropTopEdit = new JSpinner(new SpinnerNumberModel(0, 0, null, 1)));
			bindPropertyInt(this.profile.getProperties(), ZimaConversionProfile.CROP_TOP, this.cropTopEdit);

			appendTabRow(this.optionsImagePanel, gbc, "Crop bottom", this.cropBottomEdit = new JSpinner(new SpinnerNumberModel(0, 0, null, 1)));
			bindPropertyInt(this.profile.getProperties(), ZimaConversionProfile.CROP_BOTTOM, this.cropBottomEdit);
		}

		{
			rebuildElementsPanel();
			this.profile.getProperties().addChangeListener(ZimaConversionProfile.PLATFORM, (k, v) -> rebuildElementsPanel());
		}

		{
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;

			characterSelector = new CharacterSelector(() -> this.profile.getProperties().set(ZimaConversionProfile.ALLOWED_CHARACTERS, characterSelector.toSet()));
			characterSelector.change();

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

			paletteSelector = new PaletteSelector(() -> this.profile.getProperties().set(ZimaConversionProfile.ALLOWED_COLORS, paletteSelector.toSet()));
			paletteSelector.change();

			gbc.insets = new Insets(2, 4, 2, 4);
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.fill = GridBagConstraints.BOTH;
			optionsPalettePanel.add(paletteSelector, gbc);

			// create toggle buttons
			gbc.gridy = 1;
			gbc.gridwidth = 2;
			gbc.gridx = GridBagConstraints.RELATIVE;
			gbc.anchor = GridBagConstraints.CENTER;

			appendPalSelToggle(optionsPalettePanel, "All", gbc, 0, 255);

			gbc.gridwidth = 1;
			gbc.anchor = GridBagConstraints.WEST;

			optionsPalettePanel.add(this.blinkingDisabledEdit = new JCheckBox(), gbc);
			bindPropertyBoolean(this.profile.getProperties(), ZimaConversionProfile.BLINKING_DISABLED, this.blinkingDisabledEdit);
			this.profile.getProperties().addChangeListener(ZimaConversionProfile.BLINKING_DISABLED, (k, v) -> {
				this.paletteSelector.setBlinkingDisabled(Objects.equals(v, Boolean.TRUE));
			});
			optionsPalettePanel.add(new JLabel("High colors"), gbc);

			gbc.gridy = 2;
			gbc.gridwidth = 2;
			gbc.anchor = GridBagConstraints.CENTER;
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
			this.fastPreviewEdit.addItemListener((e) -> { this.asyncRenderer.setUseFastPreview(this.fastPreviewEdit.isSelected()); rerender(); });

/*			this.profile.setMseCalculatorFunction(this.mseConverterOptions.get(0).getSecond());
			appendTabRow(this.optionsAdvancedPanel, gbc, "Error calculator", this.mseConverterEdit = new JComboBox<>(this.mseConverterOptions.stream().map(Pair::getFirst).toArray(String[]::new)));
			this.mseConverterEdit.setSelectedIndex(0);
			this.mseConverterEdit.addActionListener(rerenderAndCallA(() -> this.profile.setMseCalculatorFunction(this.mseConverterOptions.get(this.mseConverterEdit.getSelectedIndex()).getSecond()))); */

			appendTabRow(this.optionsAdvancedPanel, gbc, "Tile contrast reduce",
					this.contrastReductionEdit = new JSlider(JSlider.HORIZONTAL, 0, 1000, 0),
					this.contrastReductionReset = new JButton("Reset"));
			bindPropertyFloat(this.profile.getProperties(), ZimaConversionProfile.TRIX_CONTRAST_REDUCTION, this.contrastReductionEdit, (f) -> (int) Math.sqrt(f * 10000000.0f), (i) -> (i * i) / 10000000.0f);
			this.contrastReductionReset.addActionListener((e) -> { this.profile.getProperties().reset(ZimaConversionProfile.TRIX_CONTRAST_REDUCTION); });
		}

		for (JPanel panel : List.of(this.optionsBoardPanel, this.optionsImagePanel, this.optionsCharsetPanel, this.optionsPalettePanel, this.optionsElementsPanel, this.optionsAdvancedPanel)) {
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = GridBagConstraints.RELATIVE;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.weighty = 1.0;
			gbc.fill = GridBagConstraints.VERTICAL;
			panel.add(new JLabel(), gbc);
		}

		updateVisual();

		this.openItem.addActionListener(this::onOpen);
		this.saveBrdItem.addActionListener(this::onSaveBoard);
		this.savePngItem.addActionListener(this::onSavePng);
		this.saveMzmItem.addActionListener(this::onSaveMzm);
		this.closeItem.addActionListener(this::onClose);
		this.copyItem.addActionListener(this::onCopy);
		this.pasteItem.addActionListener(this::onPaste);
		this.profileLoadItem.addActionListener(this::onLoadSettings);
		this.profileSaveItem.addActionListener(this::onSaveSettings);
		this.changelogItem.addActionListener(this::onChangelog);
		this.aboutItem.addActionListener(this::onAbout);

		this.openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		this.saveBrdItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		this.savePngItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		this.copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
		this.pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));

		this.previewCanvasPane.setBorder(null);
		this.previewCanvas.setMinimumSize(new Dimension(480, 350));
		this.previewCanvasPane.setMinimumSize(this.previewCanvas.getMinimumSize());
		this.previewCanvas.setPreferredSize(this.previewCanvas.getMinimumSize());
		this.previewCanvasPane.setPreferredSize(this.previewCanvas.getMinimumSize());
		this.renderProgress.setMinimumSize(new Dimension(480, 20));
		this.optionsPane.setMinimumSize(new Dimension(470, 378));

		this.previewCanvasPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
				previewCanvas.updateDimensions();
			}
		});

		uiReady = true;
		rerender();

		this.window.add(this.mainPanel);
		this.window.pack();
		this.window.setMinimumSize(this.window.getSize());
		this.window.setVisible(true);
	}

	public void rebuildElementsPanel() {
		optionsElementsPanel.removeAll();
		Platform platform = this.profile.getProperties().get(ZimaConversionProfile.PLATFORM);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = GridBagConstraints.RELATIVE;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0, 1, 1, 1);
		gbc.anchor = GridBagConstraints.WEST;

		int i = 0;
		for (ElementRule rule : allRulesRuleset.get(platform).getRules()) {
			StringBuilder labelStr = new StringBuilder(platform.getLibrary().getInternalName(rule.getElement()));
			if (rule.getStrategy().isRequiresStat()) {
				labelStr.append(" (Stat)");
			}
			JCheckBox box = new JCheckBox();
			JLabel label = new JLabel(labelStr.toString());
			box.addActionListener((e) -> onChangeRulesetCheckbox());

			optionsElementsPanel.add(box, gbc);
			optionsElementsPanel.add(label, gbc);
			rulesetBoxEdit.put(rule, box);

			i++;
			if ((i % 3) == 0) gbc.gridy++;
		}

		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		this.rulesetEdit = new JComboBox<>(this.rulesetOptions.get(platform).stream().map(Pair::getFirst).toArray(String[]::new));
		this.rulesetEdit.addActionListener((e) -> setRuleset(this.rulesetEdit.getSelectedIndex()));
		optionsElementsPanel.add(this.rulesetEdit, gbc);
		setRuleset(0); // default
	}

	public void onChangeRulesetCheckbox() {
		Platform platform = this.profile.getProperties().get(ZimaConversionProfile.PLATFORM);

		int index = this.rulesetEdit.getSelectedIndex();
		ImageConverterRuleset ruleset = this.rulesetOptions.get(platform).get(index).getSecond();
		if (ruleset == null) {
			setRuleset(index);
		}
	}

	public void setRuleset(int index) {
		Platform platform = this.profile.getProperties().get(ZimaConversionProfile.PLATFORM);

		if (index >= this.rulesetOptions.get(platform).size()) {
			index = 0;
		}
		ImageConverterRuleset ruleset = this.rulesetOptions.get(platform).get(index).getSecond();

		List<ElementRule> customRules = new ArrayList<>();
		// if default ruleset, disable checkboxes + copy to boxes
		// if custom ruleset, enable checkboxes + copy from boxes
		for (ElementRule rule : allRulesRuleset.get(platform).getRules()) {
			JCheckBox box = rulesetBoxEdit.get(rule);
			box.setEnabled(rule.getElement().getId() != 0 && ruleset == null);
			if (ruleset != null) {
				box.setSelected(ruleset.getRules().contains(rule));
			} else {
				if (box.isSelected()) {
					customRules.add(rule);
				}
			}
		}
		if (ruleset == null) {
			customRuleset = new ImageConverterRuleset(customRules);
			ruleset = customRuleset;
		}
		this.profile.getProperties().set(ZimaConversionProfile.RULESET, ruleset);
		rerender();
	}

	private void updateCharRatioLabel() {
		Platform platform = this.profile.getProperties().get(ZimaConversionProfile.PLATFORM);
		int width = this.profile.getProperties().get(ZimaConversionProfile.CHARS_WIDTH) * this.visual.getCharWidth() * (platform.isDoubleWide() ? 2 : 1);
		int height = this.profile.getProperties().get(ZimaConversionProfile.CHARS_HEIGHT) * this.visual.getCharHeight();
		if (width > 0 && height > 0) {
			this.charRatioLabel.setText(String.format("%.2f (%dx%d pixels)", (float) width / height, width, height));
		} else {
			this.charRatioLabel.setText("");
		}
	}

	public void updateVisual() {
		if (this.charset == null) {
			this.charset = this.defaultCharset;
		}
		if (this.palette == null) {
			this.palette = this.defaultPalette;
		}
		this.visual = new TextVisualData(8, charset.length >> 8, charset, palette);
		this.characterSelector.setVisual(this.visual);
		this.paletteSelector.setVisual(this.visual);
		this.profile.getProperties().set(ZimaConversionProfile.VISUAL_DATA, this.visual);
	}

	public boolean isShowInputImage() {
		return this.optionsPane.getSelectedIndex() == 1 && this.showInputImageEdit.isSelected();
	}

	public void rerender() {
		if (uiReady) {
			this.asyncRenderer.rerender();
			if (!isShowInputImage()) {
				this.asyncRenderer.popQueue();
			} else {
				updateCanvas();
			}
		}
	}

	public void updateCanvas() {
		if (isShowInputImage()) {
			Platform platform = this.profile.getProperties().get(ZimaConversionProfile.PLATFORM);
			this.profile.updateImage(inputImage);
			this.previewCanvas.setAllowScaling(true);
			this.previewCanvas.setDoubleWide(platform.isDoubleWide());
			this.previewCanvas.setImage(this.profile.getFilteredImage());
		} else {
			this.previewCanvas.setAllowScaling(false);
			this.previewCanvas.setDoubleWide(false);
			this.previewCanvas.setImage(this.asyncRenderer.getOutputImage());
			this.asyncRenderer.popQueue();
		}
	}

	public void setInputImage(Image image) {
		if (image == null) {
			inputImage = null;
		} else if (!(image instanceof BufferedImage) || ((BufferedImage) image).getType() != BufferedImage.TYPE_INT_RGB) {
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
		if (inputImage != null) {
			this.imageDataLabel.setText(String.format("%dx%d pixels, aspect = %.2f", inputImage.getWidth(), inputImage.getHeight(), (float) inputImage.getWidth() / inputImage.getHeight()));
		} else {
			this.imageDataLabel.setText("");
		}
		rerender();
	}

	// Menu options

	private final Map<String, SoftReference<JFileChooser>> chooserCache = new HashMap<>();

	private JFileChooser getOrCreateFileChooser(String ctx) {
		SoftReference<JFileChooser> ref = chooserCache.get(ctx);
		if (ref == null || ref.get() == null) {
			if (ctx.startsWith("image")) {
				JFileChooser nfc = ImageFileChooser.image();
				nfc.setCurrentDirectory(new File(System.getProperty("user.dir")));
				ref = new SoftReference<>(nfc);
			} else {
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
				ref = new SoftReference<>(fc);
			}
			chooserCache.put(ctx, ref);
		}
		return ref.get();
	}

	private void setFileFilters(JFileChooser fc, FileNameExtensionFilter... filters) {
		fc.resetChoosableFileFilters();
		fc.setFileFilter(null);
		if (filters.length >= 1) {
			if (filters.length >= 2) {
				for (FileNameExtensionFilter f : filters) {
					fc.addChoosableFileFilter(f);
				}
			} else {
				fc.setFileFilter(filters[0]);
			}
		}
	}

	private File showLoadDialog(String context, FileNameExtensionFilter... filters) {
		JFileChooser fc = getOrCreateFileChooser(context);
		if (!(fc instanceof ImageFileChooser)) {
			setFileFilters(fc, filters);
		}
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnVal = fc.showOpenDialog(this.window);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile();
		} else {
			return null;
		}
	}

	private File showSaveDialog(String context, FileNameExtensionFilter... filters) {
		JFileChooser fc = getOrCreateFileChooser(context);
		if (!(fc instanceof ImageFileChooser)) {
			setFileFilters(fc, filters);
		}
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnVal = fc.showSaveDialog(this.window);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (!(fc instanceof ImageFileChooser)) {
				boolean endsWithValidExtension = false;
				String defExtension = null;
				String lcName = file.getName().toLowerCase(Locale.ROOT);
				for (FileNameExtensionFilter f : filters) {
					for (String extension : f.getExtensions()) {
						if (defExtension == null) {
							defExtension = extension;
						}
						if (lcName.endsWith("." + extension)) {
							endsWithValidExtension = true;
							break;
						}
					}
					if (endsWithValidExtension) break;
				}
				if (!endsWithValidExtension && defExtension != null) {
					file = new File(file.toString() + "." + defExtension);
				}
			}
			return file;
		} else {
			return null;
		}
	}

	public void onLoadSettings(ActionEvent event) {
		File file = showLoadDialog("profile", new FileNameExtensionFilter("JSON profile", "json"));
		if (file != null) {
			try (FileReader reader = new FileReader(file)) {
				setSettings(gson.fromJson(reader, ZimaProfileSettings.class));
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this.window, "Error loading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void onSaveSettings(ActionEvent event) {
		File file = showSaveDialog("profile", new FileNameExtensionFilter("JSON profile", "json"));
		if (file != null) {
			try (FileWriter writer = new FileWriter(file)) {
				writer.write(gson.toJson(getSettings()));
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this.window, "Error saving file: " + e.getMessage());
			}
		}
	}

	public void onLoadDefaultCharset(ActionEvent event) {
		this.charset = null;
		updateVisual();
	}

	public void onLoadCustomCharset(ActionEvent event) {
		File file = showLoadDialog("assets", new FileNameExtensionFilter("Character set file", "chr", "bin"));
		if (file != null) {
			try (FileInputStream fis = new FileInputStream(file)) {
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
		File file = showLoadDialog("assets",
				new FileNameExtensionFilter("MegaZeux palette file", "pal"),
				new FileNameExtensionFilter("PLD palette file", "pld"));
		if (file != null) {
			try (FileInputStream fis = new FileInputStream(file)) {
				String name = file.getName().toLowerCase(Locale.ROOT);
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

	public void onChangelog(ActionEvent event) {
		new ZimaChangelogWindow(window);
	}

	public void onAbout(ActionEvent event) {
		try {
			new ZimaLicenseWindow(window, this.zimaVersion);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this.window, "zima " + zimaVersion + " - Copyright (c) 2020 asie", "About", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public void onOpen(ActionEvent event) {
		File file = showLoadDialog("imageInput");
		if (file != null) {
			try {
				setInputImage(ImageIO.read(file));
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this.window, "Error loading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void onClose(ActionEvent event) {
		setInputImage(null);
	}

	public void onSaveMzm(ActionEvent event) {
		if (this.asyncRenderer.getOutputResult() == null) {
			return;
		}

		File file = showSaveDialog("outputMzm", new FileNameExtensionFilter("MegaZeux MZM file", "mzm"));
		if (file != null) {
			try (FileOutputStream fos = new FileOutputStream(file)) {
				ImageConverter.Result result = this.asyncRenderer.getOutputResult();
				MZMWriter.write(fos, result.getWidth(), result.getHeight(), result::getCharacter, result::getColor);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this.window, "Error saving file: " + e.getMessage());
			}
		}
	}

	public void onSaveBoard(ActionEvent event) {
		if (this.asyncRenderer.getOutputBoard() == null) {
			return;
		}

		File file = showSaveDialog("outputBrd", new FileNameExtensionFilter("ZZT board file", "brd"));
		if (file != null) {
			try {
				Board board = this.asyncRenderer.getOutputBoard();
				Platform platform = this.profile.getProperties().get(ZimaConversionProfile.PLATFORM);

				try (FileOutputStream fos = new FileOutputStream(file); ZOutputStream zos = new ZOutputStream(fos, platform)) {
					String basename = file.getName();
					int extIndex = basename.lastIndexOf('.');
					if (extIndex > 0) {
						basename = basename.substring(0, extIndex);
					}
					board.setName(basename.replaceAll("[^\\x20-\\x7E]", "?"));
					this.asyncRenderer.getOutputBoard().writeZ(zos);
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this.window, "Error saving file: " + e.getMessage());
			}
		}
	}

	public void onSavePng(ActionEvent event) {
		if (this.asyncRenderer.getOutputImage() == null) {
			return;
		}

		File file = showSaveDialog("outputPng", new FileNameExtensionFilter("PNG image file", "png"));
		if (file != null) {
			try {
				ImageIO.write(this.asyncRenderer.getOutputImage(), "png", file);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this.window, "Error saving file: " + e.getMessage());
			}
		}
	}

	// Profile serialization

	public ZimaProfileSettings getSettings() {
		ZimaProfileSettings settings = new ZimaProfileSettings();

		settings.setMaxStatCount(this.profile.getProperties().get(ZimaConversionProfile.MAX_STAT_COUNT));
		settings.setColorsBlink(!this.profile.getProperties().get(ZimaConversionProfile.BLINKING_DISABLED));

		settings.setAllowedCharacters(IntStream.range(0, 256).filter(this.characterSelector::isCharAllowed).toArray());
		settings.setAllowedColorPairs(IntStream.range(0, 256).filter(this.paletteSelector::isTwoColorAllowed).toArray());
		settings.setAllowedElements(this.profile.getProperties().get(ZimaConversionProfile.RULESET).getRules());

		if (!Arrays.equals(this.defaultCharset, this.charset)) {
			settings.setCustomCharset(this.charset);
		}
		if (!Arrays.equals(this.defaultPalette, this.palette)) {
			settings.setCustomPalette(this.palette);
		}

		settings.setContrastReduction(this.profile.getProperties().get(ZimaConversionProfile.TRIX_CONTRAST_REDUCTION));
		settings.setAccurateApproximate(this.profile.getProperties().get(ZimaConversionProfile.TRIX_ACCURATE_APPROXIMATE));

		settings.setAspectRatioPreservationMode(this.profile.getProperties().get(ZimaConversionProfile.ASPECT_RATIO_PRESERVATION_MODE));

		return settings;
	}

	private Set<Integer> toIntSet(int[] array) {
		Set<Integer> set = new HashSet<>();
		for (int i = 0; i < array.length; i++) {
			set.add(array[i]);
		}
		return set;
	}

	public void setSettings(ZimaProfileSettings settings) {
		if (settings.getMaxStatCount() != null) {
			this.profile.getProperties().set(ZimaConversionProfile.MAX_STAT_COUNT, settings.getMaxStatCount());
		}

		if (settings.getColorsBlink() != null) {
			this.profile.getProperties().set(ZimaConversionProfile.BLINKING_DISABLED, !settings.getColorsBlink());
		}

		if (settings.getAllowedCharacters() != null) {
			Set<Integer> allowedCharactersSet = toIntSet(settings.getAllowedCharacters());
			IntStream.range(0, 256).forEach(i -> this.characterSelector.setCharAllowed(i, allowedCharactersSet.contains(i)));
		}

		if (settings.getAllowedColors() != null) {
			Set<Integer> allowedColorsSet = toIntSet(settings.getAllowedColors());
			IntStream.range(0, 256).forEach(i -> this.paletteSelector.setColorAllowed(i, true));
			IntStream.range(0, 16).forEach(i -> { if (!allowedColorsSet.contains(i)) this.paletteSelector.setColorContainingAllowed(i, false); });
		}

		if (settings.getAllowedColorPairs() != null) {
			Set<Integer> allowedColorsSet = toIntSet(settings.getAllowedColorPairs());
			IntStream.range(0, 256).forEach(i -> this.paletteSelector.setColorAllowed(i, allowedColorsSet.contains(i)));
		}

		if (settings.getAllowedElements() != null) {
			Platform platform = this.profile.getProperties().get(ZimaConversionProfile.PLATFORM);
			boolean found = false;
			int emptyIndex = -1;
			Set<ElementRule> settingsElementSet = new HashSet<>(settings.getAllowedElements());
			for (int i = 0; i < rulesetOptions.get(platform).size(); i++) {
				ImageConverterRuleset ruleset = rulesetOptions.get(platform).get(i).getSecond();
				if (ruleset != null) {
					Set<ElementRule> rulesElementSet = new HashSet<>(ruleset.getRules());
					if (rulesElementSet.equals(settingsElementSet)) {
						rulesetEdit.setSelectedIndex(i);
						setRuleset(i);
						found = true;
						break;
					}
				} else {
					emptyIndex = i;
				}
			}

			if (!found && emptyIndex >= 0) {
				for (Map.Entry<ElementRule, JCheckBox> box : rulesetBoxEdit.entrySet()) {
					box.getValue().setSelected(settings.getAllowedElements().contains(box.getKey()));
				}
				setRuleset(emptyIndex);
			}
		}

		this.charset = settings.getCustomCharset();
		this.palette = settings.getCustomPalette();
		updateVisual();

		if (settings.getContrastReduction() != null) {
			this.profile.getProperties().set(ZimaConversionProfile.TRIX_CONTRAST_REDUCTION, settings.getContrastReduction());
		}

		if (settings.getAccurateApproximate() != null) {
			this.profile.getProperties().set(ZimaConversionProfile.TRIX_ACCURATE_APPROXIMATE, settings.getAccurateApproximate());
		}

		if (settings.getAspectRatioPreservationMode() != null) {
			this.profile.getProperties().set(ZimaConversionProfile.ASPECT_RATIO_PRESERVATION_MODE, settings.getAspectRatioPreservationMode());
		}

		rerender();
	}

	// Re-render call listeners

	public <T extends Enum<?>> JComboBox<T> createEnumComboBox(Class<T> enumClass) {
		JComboBox<T> comboBox = new JComboBox<>();
		for (T value : enumClass.getEnumConstants()) {
			comboBox.addItem(value);
		}
		return comboBox;
	}

	public <T extends Enum<?>> void bindPropertyEnum(PropertyHolder holder, Property<T> property, JComboBox<T> comboBox) {
		comboBox.setSelectedItem(property.getDefaultValue());
		comboBox.addItemListener((e) -> holder.set(property, (T) comboBox.getSelectedItem()));
		holder.addChangeListener(property, (k, v) -> comboBox.setSelectedItem(v));
	}

	public void bindPropertyBoolean(PropertyHolder holder, Property<Boolean> property, JCheckBox checkBox) {
		Boolean defValue = property.getDefaultValue();
		checkBox.setSelected(Objects.equals(defValue, Boolean.TRUE));
		checkBox.addItemListener((e) -> holder.set(property, checkBox.isSelected()));
		holder.addChangeListener(property, (k, v) -> checkBox.setSelected(Objects.equals(v, Boolean.TRUE)));
	}

	public void bindPropertyInt(PropertyHolder holder, Property<Integer> property, JSpinner spinner) {
		spinner.setValue(this.profile.getProperties().get(property));
		spinner.addChangeListener((e) -> holder.set(property, ((Number) spinner.getValue()).intValue()));
		holder.addChangeListener(property, (k, v) -> spinner.setValue(holder.get(k)));
	}

	public void bindPropertyFloatScaled(PropertyHolder holder, Property<Float> property, JSlider slider, float scale) {
		bindPropertyFloat(holder, property, slider, (f) -> (int) (f * scale), (i) -> (float) i / scale);
	}

	public void bindPropertyFloat(PropertyHolder holder, Property<Float> property, JSlider slider, Function<Float, Integer> toSpinner, Function<Integer, Float> fromSpinner) {
		slider.setValue(toSpinner.apply(this.profile.getProperties().get(property)));
		slider.addChangeListener((e) -> holder.set(property, fromSpinner.apply(((Number) slider.getValue()).intValue())));
		holder.addChangeListener(property, (k, v) -> slider.setValue(toSpinner.apply(holder.get(k))));
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

	private SpinnerNumberModel boardCoordsModel(int cval, boolean height) {
		Platform platform = this.profile.getProperties().get(ZimaConversionProfile.PLATFORM);
		int cmax = height ? platform.getBoardHeight() : platform.getBoardWidth();
		if (cval > cmax) cval = cmax;
		return new SpinnerNumberModel(cval, 1, cmax, 1);
	}

	private SpinnerNumberModel statCountModel(int cval) {
		Platform platform = this.profile.getProperties().get(ZimaConversionProfile.PLATFORM);
		if (platform.getMaxStatCount() < 0) return new SpinnerNumberModel(0, 0, 0, 1);
		if (cval > platform.getActualMaxStatCount()) cval = platform.getActualMaxStatCount();
		return new SpinnerNumberModel(cval, 0, platform.getActualMaxStatCount(), 1);
	}

	private SpinnerNumberModel boardSizeModel(int cval) {
		Platform platform = this.profile.getProperties().get(ZimaConversionProfile.PLATFORM);
		if (platform.getMaxBoardSize() < 0) return new SpinnerNumberModel(0, 0, 0, 1);
		if (cval > platform.getMaxBoardSize()) cval = platform.getMaxBoardSize();
		return new SpinnerNumberModel(cval, 0, platform.getMaxBoardSize(), 1);
	}
}
