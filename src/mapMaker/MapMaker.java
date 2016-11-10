package mapMaker;

import main.Global;
import map.AreaData.TerrainType;
import map.AreaData.WeatherState;
import util.Point;
import mapMaker.data.MapMakerTriggerData;
import mapMaker.tools.MoveTool;
import mapMaker.tools.RectangleTool;
import mapMaker.tools.SelectTool;
import mapMaker.tools.SingleClickTool;
import mapMaker.tools.Tool;
import mapMaker.tools.ToolRenderer;
import mapMaker.tools.TriggerTool;
import util.DrawMetrics;
import util.FileIO;
import util.FileName;
import util.Folder;
import util.StringUtils;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapMaker extends JPanel implements ActionListener, MouseListener, MouseMotionListener, KeyListener, ListSelectionListener {
	private static final long serialVersionUID = -1323397946555510794L;

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.getContentPane().add(new MapMaker());
		frame.setSize(800, 600);
		frame.setVisible(true);
	}

	private static final Pattern mapAreaPattern = Pattern.compile("\"([^\"]*)\"\\s*(\\p{XDigit}+)");

	public static final int tileSize = 32;

	private JButton newTileButton;
	public JList<ImageIcon> tileList;
	public JList<Tool> toolList;
	public Canvas canvas;
	private JMenuItem mntmNew, mntmLoad, mntmSetRoot;
	public JMenuItem mntmCut, mntmCopy, mntmPaste;
	private JLabel lblMapName;
	private JMenuItem mntmSave;
	private JLabel lblRoot;
	public JComboBox<EditType> editTypeComboBox;

	private DefaultListModel<ImageIcon> moveListModel;
	private DefaultListModel<ImageIcon> tileListModel;
	private DefaultListModel<ImageIcon> areaListModel;
	private DefaultListModel<ImageIcon> triggerListModel;

	private Map<Integer, String> indexMap;
	private Map<Integer, String> areaIndexMap;

	private Set<Integer> areasOnMap;

	private Map<Integer, BufferedImage> mapMakerTileMap;
	private Map<Integer, BufferedImage> tileMap;
	private Map<Integer, BufferedImage> trainerTileMap;
	private Map<Integer, BufferedImage> itemTileMap;

	public MapMakerTriggerData triggerData;

	private File root;
	public String currentMapName;
	public Dimension currentMapSize;

	public BufferedImage currentMapFg;
	public BufferedImage currentMapBg;
	public BufferedImage currentMapMove;
	public BufferedImage currentMapArea;

	private Point location;
	private Point mouseHoverLocation;

	public boolean saved;
	private boolean savedIndex;
		
	private SelectTool selectTool;
	public boolean triggerToolMoveSelected = false;
	
	private Composite alphaComposite;
	private Composite defaultComposite;

	private int previousToolListIndex = -1;
	private boolean controlKeyDown = false;
	
	public enum EditType {
		BACKGROUND, FOREGROUND, MOVE_MAP, AREA_MAP, TRIGGERS
	}
	
	public EditType editType;

	public MapMaker() {
		root = null;
		saved = true;
		savedIndex = true;
		this.location = new Point(0, 0);
		editType = EditType.BACKGROUND;
		
		this.setLayout(new BorderLayout());
		setLayout(new BorderLayout(0, 0));

		JPanel tilePanel = new JPanel();
		tilePanel.setBorder(new LineBorder(new Color(0, 0, 0), 4));
		add(tilePanel, BorderLayout.WEST);
		tilePanel.setLayout(new BorderLayout(0, 0));
		
		newTileButton = new JButton("New Tile");
		newTileButton.addActionListener(this);
		tilePanel.add(newTileButton, BorderLayout.NORTH);
		
		moveListModel = new DefaultListModel<>();
		moveListModel.addElement(new ImageIcon(colorWithText("Immovable", Color.black), Color.black.getRGB() +""));
		moveListModel.addElement(new ImageIcon(colorWithText("Movable", Color.white), Color.white.getRGB() +""));
		moveListModel.addElement(new ImageIcon(colorWithText("Water", Color.blue), Color.blue.getRGB() +""));
		moveListModel.addElement(new ImageIcon(colorWithText("Right Ledge", Color.cyan), Color.cyan.getRGB() +""));
		moveListModel.addElement(new ImageIcon(colorWithText("Down Ledge", Color.green), Color.green.getRGB() +""));
		moveListModel.addElement(new ImageIcon(colorWithText("Left Ledge", Color.yellow), Color.yellow.getRGB() +""));
		moveListModel.addElement(new ImageIcon(colorWithText("Up Ledge", Color.red), Color.red.getRGB() +""));
		moveListModel.addElement(new ImageIcon(colorWithText("Stairs Up Right", Color.magenta), Color.magenta.getRGB() + ""));
		moveListModel.addElement(new ImageIcon(colorWithText("Stairs Up Left	", Color.ORANGE), Color.orange.getRGB() + ""));
		
		tileListModel = new DefaultListModel<>();
		areaListModel = new DefaultListModel<>();
		triggerListModel = new DefaultListModel<>();
		
		tileList = new JList<>();
		tileList.setModel(tileListModel);
		tileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		tileList.addListSelectionListener(this);
		tileList.addKeyListener(this);
		
		JScrollPane listScroller = new JScrollPane(tileList);
		tilePanel.add(listScroller, BorderLayout.CENTER);
		
		canvas = new Canvas();
		canvas.addMouseListener(this);
		canvas.addMouseMotionListener(this);
		canvas.addKeyListener(this);
		add(canvas, BorderLayout.CENTER);

		JMenuBar menuBar = new JMenuBar();
		add(menuBar, BorderLayout.NORTH);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		//System shortcut key. Control for windows, command for mac.
		int shortcut = Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask();
		
		mntmNew = new JMenuItem("New");
		mntmNew.addActionListener(this);
		mntmNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcut));//InputEvent.CTRL_MASK
		mnFile.add(mntmNew);
		
		mntmSave = new JMenuItem("Save");
		mntmSave.addActionListener(this);
		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcut));
		mnFile.add(mntmSave);
		
		mntmLoad = new JMenuItem("Load");
		mntmLoad.addActionListener(this);
		mntmLoad.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, shortcut));
		mnFile.add(mntmLoad);
		
		mntmSetRoot = new JMenuItem("Set Root");
		mntmSetRoot.addActionListener(this);
		mnFile.add(mntmSetRoot);

		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);
		
		mntmCut = new JMenuItem("Cut");
		mntmCut.addActionListener(this);
		mntmCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, shortcut));
		mntmCut.setEnabled(false);
		mnEdit.add(mntmCut);
		
		mntmCopy = new JMenuItem("Copy");
		mntmCopy.addActionListener(this);
		mntmCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, shortcut));
		mntmCopy.setEnabled(false);
		mnEdit.add(mntmCopy);
		
		mntmPaste = new JMenuItem("Paste");
		mntmPaste.addActionListener(this);
		mntmPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, shortcut));
		mntmPaste.setEnabled(false);
		mnEdit.add(mntmPaste);

		Component horizontalStrut = Box.createHorizontalStrut(53);
		menuBar.add(horizontalStrut);
		
		lblMapName = new JLabel("MapMaker");
		menuBar.add(lblMapName);

		Component horizontalStrut_1 = Box.createHorizontalStrut(53);
		menuBar.add(horizontalStrut_1);
		
		lblRoot = new JLabel("Root Location:");
		lblRoot.setForeground(Color.red);
		menuBar.add(lblRoot);

		Component horizontalStrut_2 = Box.createHorizontalStrut(50);
		menuBar.add(horizontalStrut_2);
		
		editTypeComboBox = new JComboBox<>();
		editTypeComboBox.addActionListener(event -> {
            editType = (EditType) editTypeComboBox.getSelectedItem();

            switch (editType) {
                case BACKGROUND:
                case FOREGROUND:
                    tileList.setModel(tileListModel);
                    newTileButton.setEnabled(true);
                    break;
                case MOVE_MAP:
                    tileList.setModel(moveListModel);
                    newTileButton.setEnabled(false);
                    break;
                case AREA_MAP:
                    tileList.setModel(areaListModel);
                    newTileButton.setEnabled(true);
                    break;
                case TRIGGERS:
                    tileList.setModel(triggerListModel);
                    newTileButton.setEnabled(false);
                    break;
            }

            if (mntmPaste != null && selectTool != null) {
				mntmPaste.setEnabled(selectTool.canPaste());
			}

            draw();
        });
		
		editTypeComboBox.setModel(new DefaultComboBoxModel<>(EditType.values()));
		editTypeComboBox.setSelectedIndex(0);
		menuBar.add(editTypeComboBox);

		DefaultListModel<Tool> toolListModel = new DefaultListModel<>();
		toolListModel.addElement(new MoveTool(this));
		toolListModel.addElement(new SingleClickTool(this));
		toolListModel.addElement(new RectangleTool(this));
		toolListModel.addElement(new TriggerTool(this));
		toolListModel.addElement(selectTool = new SelectTool(this));
		
		toolList = new JList<>();
		toolList.setModel(toolListModel);
		toolList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		toolList.setCellRenderer(new ToolRenderer());
		toolList.setFont(new Font("Arial", Font.PLAIN, 24));
		toolList.setSelectedIndex(0);
		toolList.addListSelectionListener(this);
		
		JScrollPane toolListScroller = new JScrollPane(toolList);
		add(toolListScroller, BorderLayout.EAST);

		setRoot(new File("."));
	}

	public Point getMapLocation() {
		return this.location;
	}

	public Point getMouseHoverLocation() {
		return this.mouseHoverLocation;
	}

	private void fillImage(BufferedImage image, Color color) {
		Graphics g = image.getGraphics();
		g.setColor(color);
		g.fillRect(0, 0, tileSize, tileSize);
		g.dispose();
	}

	private BufferedImage filledImage(Color color) {
		BufferedImage image = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
		fillImage(image, color);

		return image;
	}

	private BufferedImage blankImageWithText(String text) {
		int extra = DrawMetrics.getTextWidth(text + " ", 14);

		BufferedImage image = new BufferedImage(tileSize + extra, tileSize, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		g.setColor(Color.BLACK);

		// TODO: If we're starting the string 3 pixels after the image, then shoudn't we add three to the extra?
		// TODO: I think the y value here is a guess - can maybe use draw metrics to figure out where to start
		g.drawString(text, tileSize + 3, tileSize*2/3);

		g.dispose();

		return image;
	}

	private BufferedImage colorWithText(String text, Color color) {
		BufferedImage image = blankImageWithText(text);
		fillImage(image, color);
		return image;
	}

	BufferedImage imageWithText(BufferedImage image, String text) {
		if (image == null) {
			Global.error("Image is null :(");
		}

		BufferedImage bufferedImage = blankImageWithText(text);
		Graphics g = bufferedImage.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return bufferedImage;
	}

	private JFileChooser getImageFileChooser(final String folderName) {
		final File folder = new File(getPathWithRoot(folderName));
		JFileChooser fileChooser = new JFileChooser(folder);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setMultiSelectionEnabled(true);

		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.getName().toLowerCase().endsWith("png");
			}

			public String getDescription() {
				return "PNG";
			}
		});

		return fileChooser;
	}

	private void addNewTile() {
		JFileChooser fc = getImageFileChooser(Folder.MAP_TILES);

		int val = fc.showOpenDialog(this);
		if (val == JFileChooser.APPROVE_OPTION) {
			File[] files = fc.getSelectedFiles();
			try {
				for (File imageFile: files) {
					Color color = JColorChooser.showDialog(this, "Choose a preferred color for tile: " + imageFile.getName(), Color.WHITE);
					if (color == null) {
						continue;
					}

					color = permuteColor(color, indexMap);
					BufferedImage img = ImageIO.read(imageFile);
					tileMap.put(color.getRGB(), img);
					indexMap.put(color.getRGB(), imageFile.getName());

					// TODO: In the other branch with the new tile shit and shit yeah do this in there
					tileListModel.addElement(new ImageIcon(img, color.getRGB() + ""));
				}

				savedIndex = false;
			}
			catch(IOException ex) {
				Global.error("IOException caught potentially with the new tile button in the map maker or something maybe");
			}
		}
	}

	private void addNewArea() {
		// Get a name for the new area from the user.
		String newAreaName = JOptionPane.showInputDialog(this, "Please specify a new area:");

		// Keep getting a name until something unused is found.
		while (!StringUtils.isNullOrEmpty(newAreaName)) {// && areaIndexMap.containsValue(newAreaName)
			newAreaName = JOptionPane.showInputDialog(this, "The area \"" + newAreaName +"\" is already in use.\nPlease specify a new area:");
		}

		// Have a valid area name
		if (!StringUtils.isNullOrEmpty(newAreaName)) {

			// Area does not already exist.
			if (!areaIndexMap.containsValue(newAreaName)) {

				// Get a color from the user for the new area.
				Color color = JColorChooser.showDialog(this, "Choose a preferred color for area " + newAreaName, Color.WHITE);
				if (color != null) {
					color = permuteColor(color, areaIndexMap);

					// Get terrain type
					TerrainType terrainType = (TerrainType)JOptionPane.showInputDialog(this, "Terrain Type", "Please specify the terrain type:", JOptionPane.PLAIN_MESSAGE, null, TerrainType.values(), TerrainType.GRASS);

					if (terrainType != null) {
						// Get weather type
						WeatherState weatherState = (WeatherState)JOptionPane.showInputDialog(this, "Weather State", "Please specify the weather state:", JOptionPane.PLAIN_MESSAGE, null, WeatherState.values(), WeatherState.NORMAL);

						if (weatherState != null) {
							// Save index file with new area
							File areaIndexFile = new File(getPathWithRoot(FileName.MAP_AREA_INDEX));

							// TODO: Use FileIO for this
							try {
								PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(areaIndexFile, true)));
								out.println("\"" + newAreaName + "\"\t\t" + (Long.toString(color.getRGB() & 0xFFFFFFFFL, 16) ) + "\t\t" +weatherState+"\t\t" +terrainType);
							}
							catch (IOException ex) {
								ex.printStackTrace();
							}

							// Add area to the list.
							areaListModel.addElement(new ImageIcon(colorWithText(newAreaName, color), color.getRGB() + ""));
							areaIndexMap.put(color.getRGB(), newAreaName);
						}
					}
				}
			}
			// Area exists, add to model.
			else {
				int color = 0;

				for (Entry<Integer, String> es: areaIndexMap.entrySet()) {
					if (es.getValue().equals(newAreaName)) {
						color = es.getKey();
						break;
					}
				}

				areaListModel.addElement(new ImageIcon(colorWithText(newAreaName, new Color(color, true)), color + ""));
			}
		}
	}

	// Called when trying to exit, shows a confirm dialog asking to save first if there are any unsaved changes
	// Returns whether or not the exit will actually occur
	private boolean checkSaveOnExit() {
		if (!saved || (triggerData != null && !triggerData.isSaved())) {
			int val = JOptionPane.showConfirmDialog(this, "Save current file first?", "Unsaved changes", JOptionPane.YES_NO_CANCEL_OPTION);
			if (val == JOptionPane.YES_OPTION) {
				saveMap();
			}

			return val != JOptionPane.CANCEL_OPTION;
		}

		// Nothing to save, just exit
		return true;
	}

	private void createNewMapDialog() {
		String name = JOptionPane.showInputDialog(this, "Name the map");
		if (name != null) {
			currentMapName = name;
			newMap();
			saveMap();
		}
	}

	private void loadPreviousMapDialog() {
		String[] mapList = getAvailableMaps();
		String val = (String)JOptionPane.showInputDialog(this, "Select a map", "Load", JOptionPane.PLAIN_MESSAGE, null, mapList, mapList[0]);
		if (val != null) {
			currentMapName = val;
			loadMap();
		}
	}

	public void actionPerformed(ActionEvent event) {
		if (root != null) {
			if (event.getSource() == newTileButton) {

				// Adding new tile to the list of tiles
				if (editType == EditType.BACKGROUND || editType == EditType.FOREGROUND) {
					addNewTile();
				}
				// Adding new Area to list of areas
				else if (editType == EditType.AREA_MAP) {
					addNewArea();
				}
				
			}
			else if (event.getSource() == mntmSave) {
				saveMap();
			}
			else if (event.getSource() == mntmNew || event.getSource() == mntmLoad) {
				boolean cancel = checkSaveOnExit();
				if (!cancel) {
					return;
				}
				
				toolList.setSelectedIndex(0);
				
				if (event.getSource() == mntmNew) {
					createNewMapDialog();
				}
				else {
					loadPreviousMapDialog();
				}

				lblMapName.setText(currentMapName);
				draw();
			}
			else if (event.getSource() == mntmCut) {
				selectTool.cut();
			}
			else if (event.getSource() == mntmCopy) {
				selectTool.copy();
			}
			else if (event.getSource() == mntmPaste) {
				toolList.setSelectedIndex(4);
				selectTool.paste();
			}
		}
		else if (event.getSource() == mntmSetRoot) {
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
			int val = fc.showOpenDialog(this);
			if (val == JFileChooser.APPROVE_OPTION) {
				File newRoot = fc.getSelectedFile();
				setRoot(newRoot);
			}
		}
	}
	
	public String[] getAvailableMaps() {
		File mapFolder = new File(getPathWithRoot(Folder.MAPS));
		return mapFolder.list((dir, name) -> !dir.isHidden() && !name.contains("."));
	}

	public String getPathWithRoot(final String path) {
//		return root.getPath() + path;
		return path;
	}

	private String getMapFolderName(final String mapName) {
		return getPathWithRoot(FileIO.makeFolderPath(Folder.MAPS, mapName));
	}

	public File getMapTextFile(final String mapName) {
		return new File(getMapFolderName(mapName) + mapName + ".txt");
	}

	// TODO: Srsly what is going on with setting the root what the fuck
	private void setRoot(File newRoot) {
		System.out.println("root set to: " + newRoot);

		root = newRoot;
		lblRoot.setText(root.getPath());
		lblRoot.setForeground(Color.BLACK);

		FileIO.createFolder(getPathWithRoot(Folder.REC));
		FileIO.createFolder(getPathWithRoot(Folder.TILES));
		FileIO.createFolder(getPathWithRoot(Folder.MAP_TILES));
		FileIO.createFolder(getPathWithRoot(Folder.MAPS));
		
		loadMapTiles();
		loadAreas();
		loadMapMakerTiles();
		loadTrainerTiles();
		loadItemTiles();
		
		loadTriggerModel();
	}
	
	private Color permuteColor(Color color, Map<Integer,String> index) {
		int dr = color.getRed() < 128 ? 1 : -1;
		int dg = color.getGreen() < 128 ? 1 : -1;
		int db = color.getBlue() < 128 ? 1 : -1;
	
		while (index.containsKey(color.getRGB())) {
			int r = color.getRed() + dr;
			int g = color.getGreen() + dg;
			int b = color.getBlue() + db;

			color = new Color(r, g, b);
		}
		
		return color;
	}

	private void loadAreas() {
		File areaIndexFile = new File(getPathWithRoot(FileName.MAP_AREA_INDEX));
		areaIndexMap = new HashMap<>();
		areaListModel.clear();

		if (areaIndexFile.exists()) {
			String fileText = FileIO.readEntireFileWithReplacements(areaIndexFile, false);

			Matcher m = mapAreaPattern.matcher(fileText);
			while (m.find()) {
				String name = m.group(1);
				int val = (int) Long.parseLong(m.group(2), 16);

				// areaListModel.addElement(new ImageIcon(colorWithText(name, new Color(val, true)), val + ""));
				areaIndexMap.put(val, name);
			}
		}
	}

	private void loadMapTiles() {
		this.indexMap = new HashMap<>();
		this.tileMap = loadTiles(Folder.MAP_TILES, FileName.MAP_TILES_INDEX, this.tileListModel, this.indexMap, true);
		this.tileListModel.add(0, new ImageIcon(filledImage(Color.MAGENTA), "0")); // TODO: Use that other thingy when this is merged
	}

	private void loadTrainerTiles() {
		this.trainerTileMap = loadTiles(Folder.TRAINER_TILES, FileName.TRAINER_TILES_INDEX, null, null, false);
	}

	private void loadMapMakerTiles() {
		this.mapMakerTileMap = loadTiles(Folder.MAP_MAKER_TILES, FileName.MAP_MAKER_TILES_INDEX, null, null, false);
	}

	private void loadItemTiles() {
		this.itemTileMap = loadTiles(Folder.ITEM_TILES, FileName.ITEM_TILES_INDEX, null, null, false);
	}

	private Map<Integer, BufferedImage> loadTiles(
			String tileFolderName,
			String indexFileName,
			DefaultListModel<ImageIcon> listModel,
			Map<Integer, String> indexMap,
			boolean resize) {
		File indexFile = new File(getPathWithRoot(indexFileName));

		Map<Integer, BufferedImage> tileMap = new HashMap<>();

		if (listModel != null) {
			listModel.clear();
		}

		if (indexFile.exists()) {
			try {
				Scanner in = new Scanner(indexFile);
				while (in.hasNext()) {
					String name = in.next();
					int val = (int)Long.parseLong(in.next(), 16);

					File imageFile = new File(tileFolderName + name);
					if (!imageFile.exists()) {
						// TODO: Should this be throwing some sort of error?
						continue;
					}

					BufferedImage image = ImageIO.read(imageFile);
					if (resize) {
						// TODO: this may potentially be able to be combined with some of those of method thingies at the top
						image = image.getSubimage(0, 0, Math.min(image.getWidth(), tileSize*3), Math.min(image.getHeight(), tileSize*3));
					}

					tileMap.put(val, image);

					if (indexMap != null) {
						indexMap.put(val, name);
					}

					if (listModel != null) {
						listModel.addElement(new ImageIcon(image, val + ""));
					}
				}

				in.close();
			}
			catch (IOException exception) {
				Global.error("IOException caught while reading map maker images or something");
			}
		}

		return tileMap;
	}

	public enum TileType {
		ITEM,
		MAP,
		MAP_MAKER,
		TRAINER
	}

	private void loadTriggerModel() {
		triggerListModel.clear();

		for (TriggerModelType type : TriggerModelType.values()) {
			triggerListModel.addElement(type.getImageIcon(this));
		}
	}
	
	private void saveMap() {
		if (root == null) {
			return;
		}
		
		saveIndex();
		
		if (currentMapBg == null) {
			return;
		}
		
		saveTriggers();

		final String mapFolderPath = getMapFolderName(currentMapName);
		FileIO.createFolder(mapFolderPath);
		
		File mapBgFile = new File(mapFolderPath + currentMapName + "_bg.png");
		File mapFgFile = new File(mapFolderPath + currentMapName + "_fg.png");
		File mapMoveFile = new File(mapFolderPath + currentMapName + "_move.png");
		File mapAreaFile = new File(mapFolderPath + currentMapName + "_area.png");
		
		try {
			ImageIO.write(currentMapBg, "png", mapBgFile);
			ImageIO.write(currentMapFg, "png", mapFgFile);
			ImageIO.write(currentMapMove, "png", mapMoveFile);
			ImageIO.write(currentMapArea, "png", mapAreaFile);
		}
		catch (IOException e) {
			Global.error("IOException caught while trying to write map images in the map maker or something");
		}
		
		saved = true;
	}
	
	private void saveIndex() {
		if (savedIndex) {
			return;
		}
	
		savedIndex = true;

		final StringBuilder indexFile = new StringBuilder();
		for (final Entry<Integer, String> entry : indexMap.entrySet()) {
			final String imageIndex = Integer.toString(entry.getKey(), 16);
			final String imageName = entry.getValue();

			indexFile.append(imageName)
					.append(" ")
					.append(imageIndex)
					.append("\n");
		}

		FileIO.writeToFile(getPathWithRoot(FileName.MAP_TILES_INDEX), indexFile);
	}
	
	private void saveTriggers() {
		File mapTextFile = getMapTextFile(currentMapName);
		
		// Save all triggers
		triggerData.saveTriggers(mapTextFile);
	}
	
	private void loadMap() {
		final String mapFolderPath = getMapFolderName(currentMapName);

		// TODO: Will likely want an object to hold these
		File mapBgImageFile = new File(mapFolderPath + currentMapName + "_bg.png");
		File mapFgImageFile = new File(mapFolderPath + currentMapName + "_fg.png");
		File mapMoveImageFile = new File(mapFolderPath + currentMapName + "_move.png");
		File mapAreaImageFile = new File(mapFolderPath + currentMapName + "_area.png");

		// TODO: Can use that other function but not doing it because this will likely all get rewritten anyways
		File mapTextFile = new File(mapFolderPath + currentMapName + ".txt");

		areaListModel.clear();
		areasOnMap = new HashSet<>();
		areasOnMap.add(0);
		areaListModel.addElement(new ImageIcon(colorWithText(areaIndexMap.get(0), new Color(0, true)), 0 + ""));
		
		try {
			currentMapBg = ImageIO.read(mapBgImageFile);
			currentMapFg = ImageIO.read(mapFgImageFile);
			currentMapMove = ImageIO.read(mapMoveImageFile);
			
			//Check to see if area file exists
			if (mapAreaImageFile.exists()) {
				currentMapArea = ImageIO.read(mapAreaImageFile);
				
				for (int x = 0; x < currentMapBg.getWidth(); ++x) {
					for (int y = 0; y < currentMapBg.getHeight(); ++y) {
						int rgb = currentMapArea.getRGB(x, y);
						if (!areasOnMap.contains(rgb) && areaIndexMap.containsKey(rgb)) {
							areasOnMap.add(rgb);
							areaListModel.addElement(new ImageIcon(colorWithText(areaIndexMap.get(rgb), new Color(rgb, true)), rgb + ""));
						}
					}
				}
			}
			// If file doesn't exist, create it instead of crashing.
			else {
				currentMapArea = new BufferedImage(currentMapBg.getWidth(), currentMapBg.getHeight(), BufferedImage.TYPE_INT_ARGB);
			}
			
			currentMapSize = new Dimension(currentMapBg.getWidth(), currentMapBg.getHeight());
			this.location = new Point(0, 0);
			
			triggerData = new MapMakerTriggerData(currentMapName, currentMapSize, this, mapTextFile);
		}
		catch (IOException e) {
			Global.error("IOException was caught while trying to load " + currentMapName + " in the map maker or something maybe");
		}
		
		saved = true;
	}
	
	private void newMap() {
		currentMapBg = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		currentMapFg = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		currentMapMove = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		currentMapArea = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);

		// TODO: Maybe this should be a constant for the default size?
		currentMapSize = new Dimension(10, 10);
		this.location = new Point(0, 0);
		
		Graphics g = currentMapMove.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, currentMapSize.width, currentMapSize.height);
		g.dispose();
		
		//Create empty trigger data structure
		triggerData = new MapMakerTriggerData(currentMapName, currentMapSize, this);
		
		areaListModel.clear();
		areasOnMap = new HashSet<>();
		areasOnMap.add(0);
		areaListModel.addElement(new ImageIcon(colorWithText(areaIndexMap.get(0), new Color(0, true)), 0 + ""));
	}

	private Point setNewDimension(Point location) {
		Dimension newDimension = location.maximizeDimension(currentMapSize);
		int newWidth = newDimension.width;
		int newHeight = newDimension.height;

		Point delta = Point.negate(location).lowerBound();
		int startX = delta.x;
		int startY = delta.y;

		System.out.println("New " + newWidth + " " + newHeight);
		System.out.println("Start " + delta);

		BufferedImage tmpBg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
		BufferedImage tmpFg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
		BufferedImage tmpMove = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
		BufferedImage tmpArea = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

		Graphics g = tmpMove.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, newWidth, newHeight);
		g.dispose();

		tmpBg.setRGB(startX, startY, currentMapSize.width, currentMapSize.height,
				currentMapBg.getRGB(0, 0, currentMapSize.width, currentMapSize.height, null, 0, currentMapSize.width), 0, currentMapSize.width);
		tmpFg.setRGB(startX, startY, currentMapSize.width, currentMapSize.height,
				currentMapFg.getRGB(0, 0, currentMapSize.width, currentMapSize.height, null, 0, currentMapSize.width), 0, currentMapSize.width);
		tmpMove.setRGB(startX, startY, currentMapSize.width, currentMapSize.height,
				currentMapMove.getRGB(0, 0, currentMapSize.width, currentMapSize.height, null, 0, currentMapSize.width), 0, currentMapSize.width);
		tmpArea.setRGB(startX, startY, currentMapSize.width, currentMapSize.height,
				currentMapArea.getRGB(0, 0, currentMapSize.width, currentMapSize.height, null, 0, currentMapSize.width), 0, currentMapSize.width);

		currentMapBg = tmpBg;
		currentMapFg = tmpFg;
		currentMapMove = tmpMove;
		currentMapArea = tmpArea;

		//Update trigger data type
		triggerData.moveTriggerData(startX, startY, newDimension);
		currentMapSize = newDimension;

		return delta;
	}

	public Point setTile(Point location, int val) {
		Point delta = new Point(0,0);
		if (!location.inBounds(currentMapSize)) {
			delta = this.setNewDimension(location);
		}

		Point start = Point.add(delta, location);
		int x = start.x;
		int y = start.y;

		saved = false;
		switch (editType) {
			case BACKGROUND: 
				currentMapBg.setRGB(x, y, val);
				break;
			case FOREGROUND: 
				currentMapFg.setRGB(x, y, val); 
				break;
			case MOVE_MAP: 
				currentMapMove.setRGB(x, y, val);
				break;
			case AREA_MAP: 
				currentMapArea.setRGB(x, y, val); 
				break;
			case TRIGGERS: 	
				triggerData.placeTrigger(x, y);
				tileList.clearSelection();
				break;
		}

		return delta;
	}
	
	public int getTile(Point location, EditType type) {
		if (!location.inBounds(this.currentMapSize)) {
			return 0;
		}
		
		switch (type) {
			case BACKGROUND: 
				return currentMapBg.getRGB(location.x, location.y);
			case FOREGROUND: 
				return currentMapFg.getRGB(location.x, location.y);
			case MOVE_MAP: 
				return currentMapMove.getRGB(location.x, location.y);
			case AREA_MAP: 
				return currentMapArea.getRGB(location.x, location.y);
			case TRIGGERS:
			default:
				return 0; 
		}
	}
	
	public void draw() {
		if (currentMapBg == null) {
			return;
		}
		
		BufferedImage buffer = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = buffer.getGraphics();

		// TODO: What is 16 here? Should it be a constant?
		for (int x = 0; x <= canvas.getWidth()/16; x++) {
			for (int y = 0; y <= canvas.getHeight()/16; y++) {
				g.setColor(((x^y) & 1) == 0 ? Color.GRAY : Color.LIGHT_GRAY);
				g.fillRect(x*16, y*16, 16, 16);
			}
		}
		
		Graphics2D g2d = (Graphics2D)g;
		if (alphaComposite == null) {
			defaultComposite = g2d.getComposite();
			alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f);
		}
		
		switch (editType) {
			case AREA_MAP:
				// Drawing of area map handled in MOVE_MAP case.
			case MOVE_MAP:
				drawImg(g, editType);
				g2d.setComposite(alphaComposite); // TODO: Should this have a break here? (I don't know how this works at all this just sort of looks like it might be missing)
			case TRIGGERS:
			case BACKGROUND:
				drawImg(g, EditType.BACKGROUND);
				g2d.setComposite(alphaComposite);
				drawImg(g, EditType.FOREGROUND);
				break;
			case FOREGROUND:
				g2d.setComposite(alphaComposite);
				drawImg(g, EditType.BACKGROUND);
				g2d.setComposite(defaultComposite);
				drawImg(g, EditType.FOREGROUND);
				break;
		}
		
		
		if (editType != EditType.TRIGGERS) {
			// Draw all trigger items at half transparency.
			g2d.setComposite(alphaComposite);
		}
		else {
			g2d.setComposite(defaultComposite);
		}
		
		// Draw all trigger items.
		triggerData.drawTriggers(g2d, this.location);
		
		g2d.setComposite(defaultComposite);
		
		if (!toolList.isSelectionEmpty()) {
			toolList.getSelectedValue().draw(g);
		}
		
		// Draw area label on mouse hover
		if (editType == EditType.AREA_MAP) {
			Point location = DrawMetrics.getLocation(this.mouseHoverLocation, this.location);
			int tileColor = getTile(location, EditType.AREA_MAP);
		
			String areaName = areaIndexMap.get(tileColor);
			if (areaName == null) {
				areaName = areaIndexMap.get(0);
			}

			g2d.setColor(Color.BLACK);
			g2d.setFont(getFont().deriveFont(16).deriveFont(Font.BOLD));
			g2d.drawString(areaName, mouseHoverLocation.x, mouseHoverLocation.y);
		}
		
		g.dispose();
		
		g = canvas.getGraphics();
		g.drawImage(buffer, 0, 0, null);
		g.dispose();
	}
	
	private void drawImg(Graphics g, EditType type) {
		for (int y = 0; y < currentMapSize.height; y++) {
			for (int x = 0; x < currentMapSize.width; x++) {
				Point location = new Point(x, y);
				int val = getTile(location, type);
				
				if (type == EditType.MOVE_MAP || type == EditType.AREA_MAP) {
					DrawMetrics.fillTile(g, location, this.location, new Color(val, true));
				}
				else if (type != EditType.TRIGGERS && tileMap.containsKey(val)) {
					BufferedImage image = tileMap.get(val);
					DrawMetrics.drawTileImage(g, image, location, this.location);
				}
			}
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (!toolList.isSelectionEmpty()) {
			toolList.getSelectedValue().click(e.getX(), e.getY());
		}
	
		draw();
	}

	public void mouseEntered(MouseEvent event) { }
	public void mouseExited(MouseEvent event) { }

	public void mousePressed(MouseEvent event) {
		if (!toolList.isSelectionEmpty()) {
			toolList.getSelectedValue().pressed(event.getX(), event.getY());
		}
	
		draw();
	}

	public void mouseReleased(MouseEvent event) {
		if (!toolList.isSelectionEmpty()) {
			toolList.getSelectedValue().released(event.getX(), event.getY());
		}
	
		draw();
	}

	public void mouseDragged(MouseEvent event) {
		int mouseHoverX = event.getX();
		int mouseHoverY = event.getY();
		this.mouseHoverLocation = new Point(mouseHoverX, mouseHoverY);
	
		if (!toolList.isSelectionEmpty()) {
			toolList.getSelectedValue().drag(event.getX(), event.getY());
		}
	
		draw();
	}

	public void mouseMoved(MouseEvent event) {
		int mouseHoverX = event.getX();
		int mouseHoverY = event.getY();
		this.mouseHoverLocation = new Point(mouseHoverX, mouseHoverY);
	
		draw();
	}
	
	public void keyTyped(KeyEvent event) { }

	public void keyPressed(KeyEvent event)
	{		
		// TODO: e for eraser, s for single, r for rect, t for trigger, ? for select?
		if (event.getKeyCode() == KeyEvent.VK_SPACE && previousToolListIndex == -1 && !toolList.isSelectionEmpty()) {
			previousToolListIndex = toolList.getSelectedIndex();
			toolList.setSelectedIndex(0);
		}
		else if (event.getKeyCode() == KeyEvent.VK_1) {
			toolList.setSelectedIndex(0);
		}
		else if (event.getKeyCode() == KeyEvent.VK_2) {
			toolList.setSelectedIndex(1);
		}
		else if (event.getKeyCode() == KeyEvent.VK_3) {
			toolList.setSelectedIndex(2);
		}
		else if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
			if (editType == EditType.TRIGGERS && triggerData.hasPlaceableTrigger()) {
				triggerData.clearPlaceableTrigger();
				toolList.clearSelection();
			}
		}
		
		if (event.getModifiers() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) {
			controlKeyDown = true;
		}
	}

	public void keyReleased(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.VK_SPACE && previousToolListIndex != -1) {
			toolList.setSelectedIndex(previousToolListIndex);
			previousToolListIndex = -1; 
		}
		
		if (event.getModifiers() != Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) {
			controlKeyDown = false;
		}
	}

	public void valueChanged(ListSelectionEvent event) {
		if (event.getSource() == tileList) {
			//When a trigger item selected
			if (tileList.getModel().equals(triggerListModel) && !tileList.isSelectionEmpty() && !event.getValueIsAdjusting()) {
				if (currentMapBg == null) {
					tileList.clearSelection();
				}
				else {
					TriggerModelType type = TriggerModelType.getModelTypeFromIndex(tileList.getSelectedIndex());

					// Already something placeable, ignore trying to create something new.
					if (!triggerData.hasPlaceableTrigger()) {

						// Trigger was not created, deselect item
						if (!triggerData.createTrigger(type)) {
							tileList.clearSelection();
						}
						// Trigger was created, move to single selection
						else {
							if(type == TriggerModelType.WILD_BATTLE) { //If wild battle trigger, rectangle tool.
								toolList.setSelectedIndex(2); // TODO: ENUM
							}
							else {
								toolList.setSelectedIndex(1);								
							}
						}
					}
					else if (!triggerToolMoveSelected) {
						triggerData.clearPlaceableTrigger();
						tileList.clearSelection();
					}
					
					triggerToolMoveSelected = false;
				}
			}
		}
		else if (event.getSource() == toolList) {
			if (!toolList.isSelectionEmpty() && !event.getValueIsAdjusting()) {
				toolList.getSelectedValue().reset();
				if (toolList.getSelectedValue() != selectTool) {
					mntmCopy.setEnabled(false);
					mntmCut.setEnabled(false);
				}
				else if (selectTool.hasSelection()) {
					mntmCopy.setEnabled(true);
					mntmCut.setEnabled(true);
				}
			}
		}
	}
	
	public BufferedImage getTileFromSet(TileType tileType, int index) {
		BufferedImage img = null;
		
		switch (tileType) {
			case MAP_MAKER:
				img = mapMakerTileMap.get(index); 
				break;
			case TRAINER:
				img = trainerTileMap.get(index); 
			break;
			case ITEM:
				img = itemTileMap.get(index); 
				break;
			case MAP:
				img = tileMap.get(index); 
				break;
		}
		
		return img;
	}
	
	public String getAreaForIndex(int index) {
		return areaIndexMap.get(index);
	}

}
