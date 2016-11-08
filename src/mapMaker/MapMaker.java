package mapMaker;

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
import java.awt.Point;
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
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import main.Global;
import util.FileIO;
import map.TerrainType;
import map.AreaData.WeatherState;
import map.entity.npc.NPCEntityData;
import mapMaker.data.MapMakerTriggerData;
import mapMaker.data.PlaceableTrigger;
import util.FileName;
import util.Folder;
import util.StringUtils;

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
	private JList<ImageIcon> tileList;
	private JList<Tool> toolList;
	private Canvas canvas;
	private JMenuItem mntmNew, mntmLoad, mntmSetRoot;
	private JMenuItem mntmCut, mntmCopy, mntmPaste;
	private JLabel lblMapName;
	private JMenuItem mntmSave;
	private JLabel lblRoot;
	private JComboBox<EditType> editTypeComboBox;

	private DefaultListModel<ImageIcon> moveListModel;
	private DefaultListModel<ImageIcon> tileListModel;
	private DefaultListModel<ImageIcon> areaListModel;
	private DefaultListModel<ImageIcon> triggerListModel;

	private Map<Integer, String> indexMap;
	private Map<Integer, BufferedImage> tileMap;
	
	private Map<Integer, String> areaIndexMap;
	private Set<Integer> areasOnMap;

	private Map<Integer, BufferedImage> trainerTileMap;

	private Map<Integer, BufferedImage> itemTileMap;
	
	private MapMakerTriggerData triggerData;

	private Map<Integer, BufferedImage> mapMakerTileMap;
	
	private File root;
	private String currentMapName;
	private BufferedImage currentMapFg, currentMapBg, currentMapMove, currentMapArea;
	private Dimension currentMapSize;
	private int mapX, mapY;
	private int mouseHoverX, mouseHoverY;
	private boolean saved, savedIndex;
		
	private SelectTool selectTool;
	
	private Composite alphaComposite;
	private Composite defaultComposite;

	private int previousToolListIndex = -1;
	private boolean controlKeyDown = false;
	
	public enum EditType {
		BACKGROUND, FOREGROUND, MOVE_MAP, AREA_MAP, TRIGGERS
	}
	
	private EditType editType;
	
	public MapMaker() {
		root = null;
		saved = savedIndex = true;
		mapX = mapY = 0;
		editType = EditType.BACKGROUND;
		
		this.setLayout(new BorderLayout());

		JPanel tilePanel = new JPanel();
		tilePanel.setBorder(new LineBorder(new Color(0, 0, 0), 4));
		add(tilePanel, BorderLayout.WEST);
		tilePanel.setLayout(new BorderLayout());
		
		newTileButton = new JButton("New Tile");
		newTileButton.addActionListener(this);
		tilePanel.add(newTileButton, BorderLayout.NORTH);
		
		moveListModel = new DefaultListModel<>();
		moveListModel.addElement(new ImageIcon(textWithColor("Immovable", Color.black), Color.black.getRGB() +""));
		moveListModel.addElement(new ImageIcon(textWithColor("Movable", Color.white), Color.white.getRGB() +""));
		moveListModel.addElement(new ImageIcon(textWithColor("Water", Color.blue), Color.blue.getRGB() +""));
		moveListModel.addElement(new ImageIcon(textWithColor("Right Ledge", Color.cyan), Color.cyan.getRGB() +""));
		moveListModel.addElement(new ImageIcon(textWithColor("Down Ledge", Color.green), Color.green.getRGB() +""));
		moveListModel.addElement(new ImageIcon(textWithColor("Left Ledge", Color.yellow), Color.yellow.getRGB() +""));
		moveListModel.addElement(new ImageIcon(textWithColor("Up Ledge", Color.red), Color.red.getRGB() +""));
		moveListModel.addElement(new ImageIcon(textWithColor("Stairs Up Right", Color.magenta), Color.magenta.getRGB() + ""));
		moveListModel.addElement(new ImageIcon(textWithColor("Stairs Up Left	", Color.ORANGE), Color.orange.getRGB() + ""));
		
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
		
		// System shortcut key. Control for windows, command for mac.
		int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		
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
		lblRoot.setForeground(Color.RED);
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
		toolListModel.addElement(new MoveTool());
		toolListModel.addElement(new SingleClickTool());
		toolListModel.addElement(new RectangleTool());
		toolListModel.addElement(new TriggerTool());
		toolListModel.addElement(selectTool = new SelectTool());
		
		toolList = new JList<>();
		toolList.setModel(toolListModel);
		toolList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		toolList.setCellRenderer(new ToolRenderer());
		toolList.setFont(new Font("Arial", Font.PLAIN, 24)); // TODO: Boooooo Arial
		toolList.setSelectedIndex(0);
		toolList.addListSelectionListener(this);
		
		JScrollPane toolListScroller = new JScrollPane(toolList);
		add(toolListScroller, BorderLayout.EAST);
		
		///////////////////////////////
		
		//setRoot(new File("..\\Pokemon"));
		setRoot(new File("."));

		/////////////////////////////////
	}
	
	//private JFrame toolFrame;

	// TODO: Move these to the draw util class
	private BufferedImage filledImage(Color color) {
		BufferedImage bi = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();
		g.setColor(color);
		g.fillRect(0, 0, tileSize, tileSize);
		g.dispose();
		return bi;
	}
	
	private BufferedImage textWithColor(String text, Color color) {
		int fontSize = 14;
		int extra = text.length() * (fontSize / 2 + 1);
		
		BufferedImage bi = new BufferedImage(tileSize + extra, tileSize, BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();
		g.setColor(color);
		g.fillRect(0, 0, tileSize, tileSize);
		
		g.setColor(Color.black);
		g.drawString(text, tileSize + 3, tileSize*2/3);
		
		g.dispose();
		return bi;
	}
	
	private BufferedImage imageWithText(BufferedImage image, String text) {
		int fontSize = 14;
		int extra = text.length() * (fontSize / 2 + 1);

		if (image == null) {
			Global.error("Image is null :(");
		}

		BufferedImage bi = new BufferedImage(image.getWidth() + extra, image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();
		
		g.drawImage(image, 0, 0, null);
		
		g.setColor(Color.black);
		g.drawString(text, tileSize + 3, tileSize*2/3);
		
		g.dispose();
		return bi;	
	}

	public void actionPerformed(ActionEvent event) {
		if (root != null) {
			if (event.getSource() == newTileButton) {

				// Adding new tile to the list of tiles
				if (editType == EditType.BACKGROUND || editType == EditType.FOREGROUND) {
					final File mapTilesFolder = new File(getPathWithRoot(Folder.MAP_TILES));

					JFileChooser fc = new JFileChooser(mapTilesFolder);
					fc.setAcceptAllFileFilterUsed(false);
					fc.setMultiSelectionEnabled(true);
					fc.setFileFilter(new FileFilter() {
						public boolean accept(File f) {
							return f.getName().toLowerCase().endsWith("png");
						}

						public String getDescription() {
							return "PNG";
						}
					});
					
					int val = fc.showOpenDialog(this);
					if (val == JFileChooser.APPROVE_OPTION) {
						File[] files = fc.getSelectedFiles();
						try {
							for (File f: files) {
								Color color = JColorChooser.showDialog(this, "Choose a preferred Color for tile: " + f.getName(), Color.WHITE);
								if (color == null) {
									continue;
								}
							
								color = permuteColor(color, indexMap);
								BufferedImage img = ImageIO.read(f);
								tileMap.put(color.getRGB(), img);
								indexMap.put(color.getRGB(), f.getName());
								tileListModel.addElement(new ImageIcon(img, color.getRGB() + ""));
							}
							
							savedIndex = false;
						}
						catch(IOException ex) {
							Global.error("IOException caught potentially with the new tile button in the map maker or something maybe");
						}
					}
				}
				
				// Adding new Area to list of areas
				else if (editType == EditType.AREA_MAP) {

					//Get a name for the new area from the user.
					String newAreaName = JOptionPane.showInputDialog(this, "Please specify a new area:");
					
					//Keep getting a name until something unused is found.
//					while (!StringUtils.isNullOrEmpty(newAreaName)) {// && areaIndexMap.containsValue(newAreaName)
//						newAreaName = JOptionPane.showInputDialog(this, "The area \"" + newAreaName +"\" is already in use.\nPlease specify a new area:");
//					} 
					
					// Have a valid area name
					if (!StringUtils.isNullOrEmpty(newAreaName)) {

						// Area does not already exist.
						if (!areaIndexMap.containsValue(newAreaName)) {

							// Get a color from the user for the new area.
							Color color = JColorChooser.showDialog(this, "Choose a preferred Color for area " + newAreaName, Color.white);
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
										areaListModel.addElement(new ImageIcon(textWithColor(newAreaName, color), color.getRGB() + ""));
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
							
							areaListModel.addElement(new ImageIcon(textWithColor(newAreaName, new Color(color, true)), color + ""));
						}
					}
				}
				
			}
			else if (event.getSource() == mntmSave) {
				saveMap();
			}
			else if (event.getSource() == mntmNew || event.getSource() == mntmLoad) {
				if (!saved || (triggerData != null && !triggerData.isSaved())) {
					int val = JOptionPane.showConfirmDialog(this, "Save current file first?", "Unsaved changes", JOptionPane.YES_NO_CANCEL_OPTION);
					if (val == JOptionPane.CANCEL_OPTION) {
						return;
					}
					else if (val == JOptionPane.YES_OPTION) {
						saveMap();
					}
					else if (val == JOptionPane.NO_OPTION) {
						//Reload items
						triggerData.reload();
					}
				}
				
				toolList.setSelectedIndex(0);
				
				if (event.getSource() == mntmNew) {
					String name = JOptionPane.showInputDialog(this, "Name the map");
					if (name == null) {
						return;
					}
				
					currentMapName = name;
					newMap();
					saveMap();
				}
				else {
					String[] mapList = getAvailableMaps();
					
					String val = (String)JOptionPane.showInputDialog(this, "Select a map", "Load", JOptionPane.PLAIN_MESSAGE, null, mapList, mapList[0]);
					if (val == null) {
						return;
					}

					currentMapName = val;
					loadMap();
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

	// TODO: This is likely not working anymore since I put the maps into subfolders
	// TODO: Also need to be able to choose which subfolder this should go in and which to choose from
	public String[] getAvailableMaps() {
		File mapFolder = new File(getPathWithRoot(Folder.MAPS));
		return mapFolder.list((dir, name) -> !dir.isHidden() && !name.contains("."));
	}

	// TODO: I still never figured out what root is doing
	public String getPathWithRoot(final String path) {
//		return root.getPath() + path;
		return path;
	}

	private String getMapFolderName(final String mapName) {
		return getPathWithRoot(FileIO.makeFolderPath(Folder.MAPS, mapName));
	}

	public String getMapTextFileName(final String mapName) {
		return getMapFolderName(mapName) + mapName + ".txt";
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
		
		loadTiles();
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

	private void loadTiles() {
		File indexFile = new File(getPathWithRoot(FileName.MAP_TILES_INDEX));
	
		tileMap = new HashMap<>();
		indexMap = new HashMap<>();
		tileListModel.clear();
		tileListModel.addElement(new ImageIcon(filledImage(Color.MAGENTA), "0"));
		
		if (indexFile.exists()) {
			try {
				Scanner in = new Scanner(indexFile);
				while (in.hasNext()) {
					String name = in.next();
					int val = (int)Long.parseLong(in.next(), 16);
					
					BufferedImage img = ImageIO.read(new File(getPathWithRoot(Folder.MAP_TILES) + name));
					BufferedImage resizedImg = img.getSubimage(0, 0, Math.min(img.getWidth(), tileSize*3), Math.min(img.getHeight(), tileSize*3));
				
					tileListModel.addElement(new ImageIcon(resizedImg, val + ""));
					tileMap.put(val, img);
					indexMap.put(val, name);
				}
				
				in.close();
			} 
			catch (IOException exception) {
				Global.error("IOException caught while reading map maker images or something");
			}
		}
	}

	}
	
	private void loadTrainerTiles() {
		File trainerIndexFile = new File(getPathWithRoot(FileName.TRAINER_TILES_INDEX));
		trainerTileMap = new HashMap<>();
		Map<Integer, String> trainerIndexMap = new HashMap<>();
		
		if (trainerIndexFile.exists()) {
			try {
				Scanner in = new Scanner(trainerIndexFile);
				
				while (in.hasNext()) {
					String name = in.next();
					int val = (int)Long.parseLong(in.next(), 16);

					File imageFile = new File(getPathWithRoot(Folder.TRAINER_TILES) + name);
					if (!imageFile.exists()) {
						continue;
					}
					
					BufferedImage img = ImageIO.read(imageFile);
					
					trainerTileMap.put(val, img);
					trainerIndexMap.put(val, name);
				}
				
				in.close();
			} 
			catch (IOException exception) {
				Global.error("IOException caught while trying to read trainer tile images or something in the map maker or something maybe");
			}
		}
	}
	
	private void loadMapMakerTiles() {
		File mapMakerIndexFile = new File(getPathWithRoot(FileName.MAP_MAKER_TILES_INDEX));
		
		mapMakerTileMap = new HashMap<>();
		Map<Integer, String> mapMakerTileIndexMap = new HashMap<>();
		
		if (mapMakerIndexFile.exists()) {
			try {
				Scanner in = new Scanner(mapMakerIndexFile);
				while (in.hasNext()) {
					String name = in.next();
					int val = (int)Long.parseLong(in.next(), 16);
					
					File imageFile = new File(getPathWithRoot(Folder.MAP_MAKER_TILES) + name);
					if (!imageFile.exists()) {
						continue;
					}
					
					BufferedImage img = ImageIO.read(imageFile);
					
					mapMakerTileMap.put(val, img);
					mapMakerTileIndexMap.put(val, name);
				}
				
				in.close();
			} 
			catch (IOException e) {
				e.printStackTrace(); // TODO: Global.error
			}
		}
	}

	// TODO: Holy shit these are all the fucking same I can't wait to combine this shit
	private void loadItemTiles() {
		File itemIndexFile = new File(getPathWithRoot(FileName.ITEM_TILES_INDEX));
		
		itemTileMap = new HashMap<>();
		Map<Integer, String> itemIndexMap = new HashMap<>();
		
		if (itemIndexFile.exists()) {
			try {
				Scanner in = new Scanner(itemIndexFile);
				while (in.hasNext()) {
					String name = in.next();
					int val = (int)Long.parseLong(in.next(), 16);
					
					File imageFile = new File(getPathWithRoot(Folder.ITEM_TILES) + name);
					if (!imageFile.exists()) {
						continue;
					}
					
					BufferedImage img = ImageIO.read(imageFile);
					
					itemTileMap.put(val, img);
					itemIndexMap.put(val, name);
				}
				
				in.close();
			} 
			catch (IOException e) {
				e.printStackTrace(); // TODO: Global.error
			}
		}
	}
	
	private void loadTriggerModel() {
		triggerListModel.clear();
		
		//NPC, map transition triggers, wild battle triggers
		// TODO: ahhhhhh what is going on I have no idea I changed everything and I don't know how this shit works
		// TODO: Pokecenter transition, transition buildings
		// TODO: I have a feeling some sort of enum will be extremely useful here
		triggerListModel.addElement(new ImageIcon(imageWithText(trainerTileMap.get(0x0), "Item"), "0")); 				//Item
		triggerListModel.addElement(new ImageIcon(imageWithText(trainerTileMap.get(0x00000040), "NPC"), "1"));			//Trainer
		triggerListModel.addElement(new ImageIcon(imageWithText(mapMakerTileMap.get(0x4), "Trigger Entity"), "2"));		//Trigger Entity
		triggerListModel.addElement(new ImageIcon(imageWithText(mapMakerTileMap.get(0x3), "Wild Battle"), "3"));		//Wild battle
		triggerListModel.addElement(new ImageIcon(imageWithText(mapMakerTileMap.get(0x2), "Map Exit"), "4")); 			//transition exit trigger
		triggerListModel.addElement(new ImageIcon(imageWithText(mapMakerTileMap.get(0x1), "Map Entrance"), "5")); 		//transition enter trigger
		triggerListModel.addElement(new ImageIcon(imageWithText(mapMakerTileMap.get(0x5), "PokeCenter"), "6")); 		//PokeCenter transition trigger
		triggerListModel.addElement(new ImageIcon(imageWithText(mapMakerTileMap.get(0x6), "Transition Building"), "7")); 	//Transition Building transition trigger
		triggerListModel.addElement(new ImageIcon(imageWithText(mapMakerTileMap.get(0xc), "Dialogue"), "8")); 				//Dialogue trigger
		//triggerListModel.addElement(new ImageIcon(imageWithText(mapMakerTileMap.get(0xd), "Group"), "9")); 				//Group trigger
		
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

		FileIO.overwriteFile(getPathWithRoot(FileName.MAP_TILES_INDEX), indexFile);
	}
	
	private void saveTriggers() {
		String mapTextFile = getMapTextFileName(currentMapName);
		
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
		String mapTextFileName = mapFolderPath + currentMapName + ".txt";

		areaListModel.clear();
		areasOnMap = new HashSet<>();
		areasOnMap.add(0);
//		areaListModel.addElement(new ImageIcon(textWithColor(areaIndexMap.get(0), new Color(0, true)), 0 + ""));
		
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
							areaListModel.addElement(new ImageIcon(textWithColor(areaIndexMap.get(rgb), new Color(rgb, true)), rgb + ""));
						}
					}
				}
			}
			// If file doesn't exist, create it instead of crashing.
			else {
				currentMapArea = new BufferedImage(currentMapBg.getWidth(), currentMapBg.getHeight(), BufferedImage.TYPE_INT_ARGB);
			}
			
			currentMapSize = new Dimension(currentMapBg.getWidth(), currentMapBg.getHeight());
			mapX = 0;
			mapY = 0;
			
			triggerData = new MapMakerTriggerData(currentMapName, currentMapSize, this, mapTextFileName);
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
		mapX = 0;
		mapY = 0;
		
		Graphics g = currentMapMove.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, currentMapSize.width, currentMapSize.height);
		g.dispose();
		
		// Create empty trigger data structure
		triggerData = new MapMakerTriggerData(currentMapName, currentMapSize, this);
		
		areaListModel.clear();
		areasOnMap = new HashSet<>();
		areasOnMap.add(0);
		areaListModel.addElement(new ImageIcon(textWithColor(areaIndexMap.get(0), new Color(0, true)), 0 + ""));
	}
	
	private Point setTile(int x, int y, int val) {
		Point returnPoint = new Point(0,0);
		
		if (x < 0 || y < 0 || x >= currentMapSize.width || y >= currentMapSize.height) {
			int newWidth = Math.max(currentMapSize.width, Math.max(x + 1, currentMapSize.width - x));
			int newHeight = Math.max(currentMapSize.height, Math.max(y + 1, currentMapSize.height - y));

			int startX = Math.max(0, -x);
			int startY = Math.max(0, -y);

			System.out.println("New " + newWidth + " " + newHeight);
			System.out.println("Start " + startX + " " + startY);
			
			BufferedImage tmpBg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
			BufferedImage tmpFg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
			BufferedImage tmpMove = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
			BufferedImage tmpArea = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
			
			Graphics g = tmpMove.getGraphics();
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, newWidth, newHeight);
			g.dispose();

			// TODO: omg ahhh
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
			
			// Update trigger data type
			triggerData.moveTriggerData(startX, startY, new Dimension(newWidth, newHeight));
			returnPoint = new Point(startX, startY);
			
			currentMapSize = new Dimension(newWidth, newHeight);
			
			x += startX;
			y += startY;
		}
		
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
		
		return returnPoint;
	}

	public boolean inBounds(int x, int y) {
		return x >= 0 && x < currentMapSize.width && y >= 0 && y < currentMapSize.height;
	}
	
	public int getTile(int x, int y, EditType type) {
		if (!inBounds(x, y)) {
			return 0;
		}
		
		switch (type) {
			case BACKGROUND: 
				return currentMapBg.getRGB(x, y);
			case FOREGROUND: 
				return currentMapFg.getRGB(x, y);
			case MOVE_MAP: 
				return currentMapMove.getRGB(x, y);
			case AREA_MAP: 
				return currentMapArea.getRGB(x, y);
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
		triggerData.drawTriggers(g2d, mapX, mapY);
		
		g2d.setComposite(defaultComposite);
		
		if (!toolList.isSelectionEmpty()) {
			toolList.getSelectedValue().draw(g);
		}
		
		// Draw area label on mouse hover
		if (editType == EditType.AREA_MAP) {
			int x = (mouseHoverX - mapX)/tileSize;
			int y = (mouseHoverY - mapY)/tileSize;
			int tileColor = getTile(x, y, EditType.AREA_MAP);
		
			String areaName = areaIndexMap.get(tileColor);
			if (areaName == null) {
				areaName = areaIndexMap.get(0);
			}

			g2d.setColor(Color.BLACK);
			g2d.setFont(getFont().deriveFont(16).deriveFont(Font.BOLD));
			g2d.drawString(areaName, mouseHoverX, mouseHoverY);
		}
		
		g.dispose();
		
		g = canvas.getGraphics();
		g.drawImage(buffer, 0, 0, null);
		g.dispose();
	}
	
	private void drawImg(Graphics g, EditType type) {
		for (int dy = mapY, y = 0; y < currentMapSize.height; y++, dy += tileSize) {
			for (int dx = mapX, x = 0; x < currentMapSize.width; x++, dx += tileSize) {
				int val = getTile(x, y, type);
				
				if (type == EditType.MOVE_MAP || type == EditType.AREA_MAP) {
					g.setColor(new Color(val, true));
					g.fillRect(dx, dy, tileSize, tileSize);
				}
				else if (type != EditType.TRIGGERS && tileMap.containsKey(val)) {
					BufferedImage img = tileMap.get(val);
					g.drawImage(img, dx - img.getWidth() + tileSize,dy - img.getHeight() + tileSize, null);
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
		mouseHoverX = event.getX();
		mouseHoverY = event.getY();
	
		if (!toolList.isSelectionEmpty()) {
			toolList.getSelectedValue().drag(event.getX(), event.getY());
		}
	
		draw();
	}

	public void mouseMoved(MouseEvent event) {
		mouseHoverX = event.getX();
		mouseHoverY = event.getY();
	
		draw();
	}
	
	public void keyTyped(KeyEvent event) { }

	public void keyPressed(KeyEvent event) {

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

		// TODO: What is this?
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

			// When a trigger item selected
			if (tileList.getModel().equals(triggerListModel) && !tileList.isSelectionEmpty() && !event.getValueIsAdjusting()) {
				if (currentMapBg == null) {
					tileList.clearSelection();
				}
				else {
					// Already something placeable, ignore trying to create something new.
					if (!triggerData.hasPlaceableTrigger()) {
						// Trigger was not created, deselect item
						if (!triggerData.createTrigger(tileList.getSelectedValue().getDescription())) {
							tileList.clearSelection();
						}
						// Trigger was created, move to single selection
						else {
							// TODO: Stop hard coding things
							if (tileList.getSelectedIndex() == 3) { //If wild battle trigger, rectangle tool.
								toolList.setSelectedIndex(2);
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

	public BufferedImage getMapMakerTile(int index) {
		return mapMakerTileMap.get(index);
	}

	public BufferedImage getTrainerTile(int index) {
		return trainerTileMap.get(index);
	}

	public BufferedImage getItemTile(int index) {
		return itemTileMap.get(index);
	}

	public BufferedImage getMapTile(int index) {
		return tileMap.get(index);
	}
	
	public String getAreaForIndex(int index) {
		return areaIndexMap.get(index);
	}
	
	private class ToolRenderer extends JLabel implements ListCellRenderer<Tool> {
		private static final long serialVersionUID = 6750963470094004328L;

		public Component getListCellRendererComponent(JList<? extends Tool> list,
													  Tool value,
													  int index,
													  boolean isSelected,
													  boolean hasFocus) {
			String s = value.toString();
			setText(s);
			//setIcon((s.length() > 10) ? longIcon : shortIcon);
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			}
			else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			
			return this;
		}
	}

	// TODO: Maybe move all these to their own file srsly map maker is getting out of control
	private interface Tool {
		void click(int x, int y);
		void released(int x, int y);
		void pressed(int x, int y);
		void drag(int x, int y);
		void draw(Graphics g);
		void reset();
	}
	
	private class MoveTool implements Tool {
		private int prevX, prevY;

		public void click(int x, int y) { }
		public void drag(int x, int y) {
			mapX -= prevX - x;
			mapY -= prevY - y;
			prevX = x;
			prevY = y;
		}

		public String toString() {
			return "Move";
		}

		public void draw(Graphics g) { }
		public void released(int x, int y) { }

		public void pressed(int x, int y) {
			prevX = x;
			prevY = y;
		}

		public void reset() {
			prevX = mouseHoverX;
			prevY = mouseHoverY;
		}
	}
	
	private class SingleClickTool implements Tool {
		public void click(int x, int y) {
			if (tileList.isSelectionEmpty())
				return;
		
			x = (int)Math.floor((x - mapX)*1.0/tileSize);
			y = (int)Math.floor((y - mapY)*1.0/tileSize);
			
			System.out.println("click: " + x+" " + y);
			
			int val = Integer.parseInt(tileList.getSelectedValue().getDescription());
			setTile(x,y,val);
			
			if (editType == EditType.TRIGGERS) {
				triggerData.clearPlaceableTrigger();
				toolList.setSelectedIndex(3);
			}
		}

		public String toString() {
			return "Single";
		}
		
		public void draw(Graphics g) {
			int mhx = (int)Math.floor((mouseHoverX - mapX)*1.0/tileSize);
			int mhy = (int)Math.floor((mouseHoverY - mapY)*1.0/tileSize);

			g.setColor(Color.red);
			g.drawRect(mhx*tileSize + mapX, mhy*tileSize + mapY, tileSize, tileSize);
			
			if (tileList.isSelectionEmpty()) {
				return;
			}
			
			//Show preview image for normal map tiles
			if (editType == EditType.BACKGROUND || editType == EditType.FOREGROUND) {
				int val = Integer.parseInt(tileList.getSelectedValue().getDescription());
				if (!tileMap.containsKey(val)) {
					return;
				}
			
				BufferedImage img = tileMap.get(val);
				g.drawImage(tileMap.get(val), mhx*tileSize + mapX - img.getWidth() + tileSize, mhy*tileSize + mapY - img.getHeight() + tileSize, null);
			}
			//Show preview image for current trigger
			else if (editType == EditType.TRIGGERS) {
				BufferedImage img = null;

				switch (tileList.getSelectedValue().getDescription()) {
					case "0": 
						img = trainerTileMap.get(0);
						break; 
					case "1": 
						NPCEntityData npc = (NPCEntityData) triggerData.getPlaceableTrigger().entity;
						img = trainerTileMap.get(12*npc.spriteIndex + 1 + npc.defaultDirection.ordinal()); // TODO: This should call a function
						break;
					case "2": 
						img = mapMakerTileMap.get(4);
						break;
					case "3": 
						img = mapMakerTileMap.get(3);
						break;
					case "4": 
						img = mapMakerTileMap.get(2);
						break;
					case "5": 
						img = mapMakerTileMap.get(1);
						break;
					case "6": 
						img = mapMakerTileMap.get(5);
						break;
					case "8": 
						img = mapMakerTileMap.get(0xc);
						break;
				}
				
				if (img != null) {
					// TODO: I think there's a method in draw metrics that does something like this
					g.drawImage(img,mhx*tileSize + mapX - img.getWidth()/2 + tileSize/2,
							mhy*tileSize + mapY - img.getHeight() + tileSize,
							null);
				}
				else if (tileList.getSelectedValue().getDescription().equals("7")) {
					int direction = triggerData.getPlaceableTriggerTransitionBuildingDirection();
					
					img = mapMakerTileMap.get(8 + direction);
					
					int[] dx = {1,0,1,1};
					int[] dy = {1,1,0,1};
					
					g.drawImage(img,mhx*tileSize + mapX - (dx[direction]*tileSize), mhy*tileSize + mapY - (dy[direction]*tileSize), null);
				}
			}
			//Show preview color for other edit modes
//			else if (editType == EditType.AREA_MAP || editType == EditType.MOVE_MAP) {
//				int val = Integer.parseInt(tileList.getSelectedValue().getDescription());
//				BufferedImage img = filledImage(new Color(val, true));
//				g.drawImage(img, mhx*tileSize + mapX - img.getWidth() + tileSize, mhy*tileSize + mapY - img.getHeight() + tileSize, null);
//			}
			
		}
		
		public void drag(int x, int y) { }
		public void released(int x, int y) { }
		public void pressed(int x, int y) { }
		public void reset() { }
	}
	
	/*class FillTool implements Tool{
		
	}*/
	private class RectangleTool implements Tool {
		private int startX, startY;
		private boolean pressed = false;

		public void click(int x, int y) { }

		public void released(int x, int y) {
			if (tileList.isSelectionEmpty() || !pressed) {
				return;
			}
		
			int mhx = (int)Math.floor((mouseHoverX - mapX)*1.0/tileSize);
			int mhy = (int)Math.floor((mouseHoverY - mapY)*1.0/tileSize);

			pressed = false;
			
			int tx = Math.min(startX, mhx);
			int ty = Math.min(startY, mhy);
			int bx = Math.max(startX, mhx);
			int by = Math.max(startY, mhy);
			
			int width = bx - tx;
			int height = by - ty;
			
			int val = Integer.parseInt(tileList.getSelectedValue().getDescription());
			for (int i = 0; i <= width; i++) {
				for (int j = 0; j <= height; j++) {
					Point delta = setTile(tx + i, ty + j, val);
				
					if (delta.x != 0) {
						tx += delta.x;
					}
					
					if (delta.y != 0) {
						ty += delta.y;
					}
				}
			}
			
			if (editType == EditType.TRIGGERS) {
				triggerData.clearPlaceableTrigger();
				toolList.setSelectedIndex(3);
			}
		}

		public void pressed(int x, int y) {
			if (tileList.isSelectionEmpty()) {
				return;
			}
			
			startX = (int)Math.floor((x - mapX)*1.0/tileSize);
			startY = (int)Math.floor((y - mapY)*1.0/tileSize);
		
			pressed = true;
		}

		public void drag(int x, int y) { }

		public void draw(Graphics g) {
			int mhx = (int)Math.floor((mouseHoverX - mapX)*1.0/tileSize);
			int mhy = (int)Math.floor((mouseHoverY - mapY)*1.0/tileSize);
			
			g.setColor(Color.red);
			if (!pressed) {
				g.drawRect(mhx*tileSize + mapX, mhy*tileSize + mapY, tileSize, tileSize);
			}
			else {
				int tx = Math.min(startX, mhx);
				int ty = Math.min(startY, mhy);
				int bx = Math.max(startX, mhx);
				int by = Math.max(startY, mhy);
				
				g.drawRect(tx*tileSize + mapX, ty*tileSize + mapY, tileSize*(bx - tx + 1), tileSize*(by - ty + 1));
			}
		}

		public String toString() {
			return "Rectangle";
		}
		
		public void reset() {
			pressed = false;
		}
	}

	private boolean triggerToolMoveSelected = false;
	
	private class TriggerTool implements Tool {
		private JPopupMenu triggerListPopup;
		private JPopupMenu triggerOptionsPopup;
		
		private int mouseX;
		private int mouseY;
		
		private PlaceableTrigger[] triggers;
		private PlaceableTrigger selectedTrigger;
		
		TriggerTool() {
			selectedTrigger = null;
			triggerListPopup = new JPopupMenu();
			triggerOptionsPopup  = new JPopupMenu();
			
			JMenuItem editItem = new JMenuItem("Edit");
			triggerOptionsPopup.add(editItem);
			
			editItem.addActionListener(event -> triggerData.editTrigger(selectedTrigger));
			
			JMenuItem moveItem = new JMenuItem("Move");
			triggerOptionsPopup.add(moveItem);
			moveItem.addActionListener(event -> {
                toolList.setSelectedIndex(1);
                int index = triggerData.getTriggerTypeIndex(selectedTrigger);
                if (index != -1) {
                    editTypeComboBox.setSelectedIndex(4);

                    triggerData.moveTrigger(selectedTrigger);
                     triggerToolMoveSelected = true;

                     tileList.setSelectedIndex(index);
                }
            });
			
			JMenuItem removeItem = new JMenuItem("Remove");
			triggerOptionsPopup.add(removeItem);
			removeItem.addActionListener(event -> triggerData.removeTrigger(selectedTrigger));
		}
		
		public String toString() {
			return "Trigger";
		}
		
		public void click(int x, int y) {
			if (currentMapName == null) {
				return;
			}
			
			mouseX = x;
			mouseY = y;
			
			x = (int)Math.floor((x - mapX)*1.0/tileSize);
			y = (int)Math.floor((y - mapY)*1.0/tileSize);
			System.out.println("Trigger click: " + x + " " + y);
			
			triggers = triggerData.getTrigger(x, y);
			triggerListPopup.removeAll();
			
			for (PlaceableTrigger trigger: triggers) {
			    JMenuItem menuItem = new JMenuItem(trigger.name + " (" + trigger.triggerType + ")");
			    triggerListPopup.add(menuItem);
			    menuItem.addActionListener(event -> {
                    Component[] components = triggerListPopup.getComponents();
                    // TODO: If someone reads this, please suggest a better way to find the index of the selected item...
                    for (Component component: components) {
                        if (((JMenuItem)component).getText().equals(event.getActionCommand())) {
                            for (PlaceableTrigger trigger1 : triggers) {
                                if (event.getActionCommand().equals(trigger1.name + " (" + trigger1.triggerType + ")")) {
                                    //System.out.println("Clicked " + e.getActionCommand());
                                    selectedTrigger = trigger1;
                                    break;
                                }
                            }
                        }
                    }

                  //triggerListPopup.removeAll();
                  triggerOptionsPopup.show(canvas, mouseX, mouseY);
                });
			}
			
			triggerListPopup.show(canvas, mouseX, mouseY);
		}
		
		public void draw(Graphics g) {
			int mhx = (int)Math.floor((mouseHoverX - mapX)*1.0/tileSize);
			int mhy = (int)Math.floor((mouseHoverY - mapY)*1.0/tileSize);
			
			g.setColor(Color.blue);
			g.drawRect(mhx*tileSize + mapX, mhy*tileSize + mapY, tileSize, tileSize);
		}
		
		public void released(int x, int y) { }
		public void pressed(int x, int y) { }
		public void drag(int x, int y) { }
		public void reset() { }
	}

	// Select tool so you can copy/cut/paste
	private class SelectTool implements Tool {
		private boolean paste;
		private boolean selected;
		private boolean controlClick;
		
		private BufferedImage copiedTiles = null;
		private EditType copiedEditType;
		
		private int startX, startY;
		private int tx, ty, bx, by;
		
		private boolean pressed = false;

		public void click(int x, int y) {
			if (!paste || controlClick) {
				return;
			}
			
			saved = false;
			
			x = (int)Math.floor((x - mapX)*1.0/tileSize);
			y = (int)Math.floor((y - mapY)*1.0/tileSize);
			
			for (int currX = 0; currX < copiedTiles.getWidth(); ++currX) {
				for (int currY = 0; currY < copiedTiles.getHeight(); ++currY) {
					int val = copiedTiles.getRGB(currX, currY);
					Point delta = setTile(x + currX, y + currY, val);

					if (delta.x != 0) {
						x += delta.x;
					}
					
					if (delta.y != 0) {
						y += delta.y;
					}
				}
			}
			
			paste = false;
		}

		public void released(int x, int y) {
			if (editType == EditType.TRIGGERS || paste || !pressed) {
				return;
			}
			
			pressed = false;
			select();

			int mhx = (int)Math.floor((mouseHoverX - mapX)*1.0/tileSize);
			int mhy = (int)Math.floor((mouseHoverY - mapY)*1.0/tileSize);
			
			tx = Math.max(Math.min(startX, mhx), 0);
			ty = Math.max(Math.min(startY, mhy), 0);
			bx = Math.min(Math.max(startX, mhx), currentMapSize.width - 1);
			by = Math.min(Math.max(startY, mhy), currentMapSize.height - 1);
			
			if (tx > bx || ty > by) {
				deselect();
			}
		}

		public void pressed(int x, int y) {
			if (editType == EditType.TRIGGERS || paste) {
				return;
			}
			
//			if (controlKeyDown && selected) {
//				cut();
//				controlClick = true;
//				return;
//			}
			
			startX = (int)Math.floor((x - mapX)*1.0/tileSize);
			startY = (int)Math.floor((y - mapY)*1.0/tileSize);
			
			pressed = true;
			deselect();
		}
		
		public void drag(int x, int y) {
//			if (controlClick) {
//				controlClick = false;
//				paste();
//				//return;
//			}
//			
//			click(x, y);
		}

		public void draw(Graphics g) {
			if (!pressed && !selected && !paste) {
				return;
			}
			
			int mhx = (int)Math.floor((mouseHoverX - mapX)*1.0/tileSize);
			int mhy = (int)Math.floor((mouseHoverY - mapY)*1.0/tileSize);
			
			//int tx, ty, bx, by;
			if (!selected) {
				tx = Math.max(Math.min(startX, mhx), 0);
				ty = Math.max(Math.min(startY, mhy), 0);
				bx = Math.min(Math.max(startX, mhx), currentMapSize.width - 1);
				by = Math.min(Math.max(startY, mhy), currentMapSize.height - 1);
			}
			
			if (!paste) {
				g.setColor(Color.RED);
				g.drawRect(tx*tileSize + mapX, ty*tileSize + mapY, tileSize*(bx - tx + 1), tileSize*(by - ty + 1));
			}
			else {
				// Show preview image for all pasting tiles.
				for (int currX = 0; currX < copiedTiles.getWidth(); ++currX) {
					for (int currY = 0; currY < copiedTiles.getHeight(); ++currY) {
						int val = copiedTiles.getRGB(currX, currY);
						if (editType == EditType.BACKGROUND || editType == EditType.FOREGROUND) {
							if (!tileMap.containsKey(val)) {
								continue;
							}
						
							BufferedImage img = tileMap.get(val);
							g.drawImage(tileMap.get(val), (mhx + currX)*tileSize + mapX - img.getWidth() + tileSize, (mhy + currY)*tileSize + mapY - img.getHeight() + tileSize, null);
						}
						else if (editType == EditType.MOVE_MAP || editType == EditType.AREA_MAP) {
							g.setColor(new Color(val));
							g.fillRect((mhx + currX)*tileSize + mapX, (mhy + currY)*tileSize + mapY, tileSize, tileSize);
						}
					}
				}
				
				g.setColor(Color.red);
				g.drawRect(mhx*tileSize + mapX, mhy*tileSize + mapY, tileSize*copiedTiles.getWidth(), tileSize*copiedTiles.getHeight());
			}
		}
		
		public void reset() {
			pressed = false;
		}

		public String toString() {
			return "Select";
		}
		
		boolean hasSelection() {
			return selected;
		}
		
		boolean canPaste() {
			return copiedEditType == editType && copiedTiles != null;
		}
		
		void select() {
			selected = true;
			mntmCopy.setEnabled(true);
			mntmCut.setEnabled(true);
		}
		
		void deselect() {
			selected = false;
			mntmCopy.setEnabled(false);
			mntmCut.setEnabled(false);
		}
		
		void copy() {
			copiedEditType = editType;
			
			BufferedImage currentMapImage = null;
			if (editType == EditType.FOREGROUND) {
				currentMapImage = currentMapFg;
			}
			else if (editType == EditType.BACKGROUND) {
				currentMapImage = currentMapBg;
			}
			else if (editType == EditType.MOVE_MAP) {
				currentMapImage = currentMapMove;
			}
			else if (editType == EditType.AREA_MAP) {
				currentMapImage = currentMapArea;
			}
			
			int width = bx - tx + 1;
			int height = by - ty + 1;
			copiedTiles = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			
			copiedTiles.setRGB(0, 0, width, height, 
					currentMapImage.getRGB(tx, ty, width, height, null, 0, width), 
					0, width);
			
			if (!mntmPaste.isEnabled()) {
				mntmPaste.setEnabled(true);
			}
		}
		
		public void cut() {
			copy();
			
			int val = Integer.parseInt(tileList.getModel().getElementAt(0).getDescription());
			for (int i = tx; i <= bx; i++) {
				for (int j = ty; j <= by; j++) {
					setTile(i, j, val);
				}
			}
		}
		
		void paste() {
			paste = true;
			deselect();
		}
	}
}
