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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Scanner;
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
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import main.Global;
import map.entity.NPCEntityData;
import mapMaker.data.MapMakerTriggerData;
import mapMaker.data.PlaceableTrigger;


public class MapMaker extends JPanel implements ActionListener, MouseListener, MouseMotionListener, KeyListener, ListSelectionListener
{
	private static final long serialVersionUID = -1323397946555510794L;

	public static void main(String[] args) 
	{
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(new MapMaker());
		frame.setSize(800, 600);
		frame.setVisible(true);
	}
	
	public static final String FILE_SLASH = File.separator;
	
	public static final int tileSize = 32;
	public static final String recFolderNme = "rec";
	public static final String mapFolderName = recFolderNme + FILE_SLASH + "maps";
	public static final String tileFolderName = recFolderNme + FILE_SLASH + "tiles";
	public static final String mapTileFolderName = tileFolderName + FILE_SLASH + "mapTiles";
	public static final String mapTileIndexFileName = mapTileFolderName + FILE_SLASH + "index.txt";
	
	public static final String mapAreaIndexFileName = mapFolderName + FILE_SLASH + "areaIndex.txt";
	
	public static final String trainerTileFolderName = tileFolderName + FILE_SLASH + "trainerTiles";
	public static final String trainerTileIndexFileName = trainerTileFolderName + FILE_SLASH + "index.txt";
	
	public static final String mapMakerTileFolderName = tileFolderName + FILE_SLASH + "mapMakerTiles";
	public static final String mapMakerTileIndexFileName = mapMakerTileFolderName + FILE_SLASH + "index.txt";
	
	public static final String itemTileFolderName = tileFolderName + FILE_SLASH + "itemTiles";
	public static final String itemTileIndexFileName = itemTileFolderName + FILE_SLASH + "index.txt";
	
	private JPanel tilePanel;
	private JButton newTileButton;
	private JList<ImageIcon> tileList;
	private JList<Tool> toolList;
	private Canvas canvas;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmNew, mntmLoad, mntmSetRoot;
	private JMenu mnEdit;
	private JMenuItem mntmCut, mntmCopy, mntmPaste;
	private JLabel lblMapName;
	private JMenuItem mntmSave;
	private Component horizontalStrut, horizontalStrut_1, horizontalStrut_2;
	private JLabel lblRoot;
	private JComboBox<EditType> editTypeComboBox;

	private DefaultListModel<ImageIcon> moveListModel;
	private DefaultListModel<ImageIcon> tileListModel;
	private DefaultListModel<ImageIcon> areaListModel;
	private DefaultListModel<ImageIcon> triggerListModel;
	private DefaultListModel<Tool> toolListModel;
	
	private HashMap<Integer, String> indexMap;
	private HashMap<Integer, BufferedImage> tileMap;
	
	private HashMap<Integer, String> areaIndexMap;
	private HashSet<Integer> areasOnMap;
	
	private HashMap<Integer, String> trainerIndexMap;
	private HashMap<Integer, BufferedImage> trainerTileMap;
	
	private HashMap<Integer, String> itemIndexMap;
	private HashMap<Integer, BufferedImage> itemTileMap;
	
	private MapMakerTriggerData triggerData;
	
	private HashMap<Integer, String> mapMakerTileIndexMap;
	private HashMap<Integer, BufferedImage> mapMakerTileMap;
	
	public File root;
	private String currentMapName;
	private BufferedImage currentMapFg, currentMapBg, currentMapMove, currentMapArea;
	private Dimension currentMapSize;
	private int mapX, mapY;
	private int mouseHoverX, mouseHoverY;
	private boolean saved, savedIndex;
		
	private SelectTool selectTool;
	
	Composite alphaComposite = null;
	Composite defaultComposite = null;
	
	public static enum EditType
	{
		BACKGROUND, FOREGROUND, MOVE_MAP, AREA_MAP, TRIGGERS
	};
	
	private EditType editType;
	
	public MapMaker()
	{
		root = null;
		saved = savedIndex = true;
		mapX = mapY = 0;
		editType = EditType.BACKGROUND;
		
		this.setLayout(new BorderLayout());
		setLayout(new BorderLayout(0, 0));
		
		tilePanel = new JPanel();
		tilePanel.setBorder(new LineBorder(new Color(0, 0, 0), 4));
		add(tilePanel, BorderLayout.WEST);
		tilePanel.setLayout(new BorderLayout(0, 0));
		
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
		
		menuBar = new JMenuBar();
		add(menuBar, BorderLayout.NORTH);
		
		mnFile = new JMenu("File");
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
		
		mnEdit = new JMenu("Edit");
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
		
		horizontalStrut = Box.createHorizontalStrut(53);
		menuBar.add(horizontalStrut);
		
		lblMapName = new JLabel("MapMaker");
		menuBar.add(lblMapName);
		
		horizontalStrut_1 = Box.createHorizontalStrut(53);
		menuBar.add(horizontalStrut_1);
		
		lblRoot = new JLabel("Root Location:");
		lblRoot.setForeground(Color.red);
		menuBar.add(lblRoot);

		horizontalStrut_2 = Box.createHorizontalStrut(50);
		menuBar.add(horizontalStrut_2);
		
		editTypeComboBox = new JComboBox<EditType>();
		editTypeComboBox.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				editType = (EditType) editTypeComboBox.getSelectedItem();
				
				switch (editType) 
				{
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
				
				if (mntmPaste != null && selectTool != null)
					mntmPaste.setEnabled(selectTool.canPaste());
				
				draw();
			}
		});
		
		editTypeComboBox.setModel(new DefaultComboBoxModel<EditType>(EditType.values()));
		editTypeComboBox.setSelectedIndex(0);
		menuBar.add(editTypeComboBox);
		
		toolListModel = new DefaultListModel<>();
		toolListModel.addElement(new MoveTool());
		toolListModel.addElement(new SingleClickTool());
		toolListModel.addElement(new RectangleTool());
		toolListModel.addElement(new TriggerTool());
		toolListModel.addElement(selectTool = new SelectTool());
		
		toolList = new JList<>();
		toolList.setModel(toolListModel);
		toolList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		toolList.setCellRenderer(new ToolRenderer());
		toolList.setFont(new Font("Arial", Font.PLAIN, 24));
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
	private BufferedImage filledImage(Color color) 
	{
		BufferedImage bi = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();
		g.setColor(color);
		g.fillRect(0, 0, tileSize, tileSize);
		g.dispose();
		return bi;
	}
	
	private BufferedImage textWithColor(String text, Color color) 
	{	
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
	
	private BufferedImage imageWithText(BufferedImage image, String text) 
	{	
		int fontSize = 14;
		int extra = text.length() * (fontSize / 2 + 1);
		
		BufferedImage bi = new BufferedImage(image.getWidth() + extra, image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();
		
		g.drawImage(image, 0, 0, null);
		
		g.setColor(Color.black);
		g.drawString(text, tileSize + 3, tileSize*2/3);
		
		g.dispose();
		return bi;	
	}

	public void actionPerformed(ActionEvent e) 
	{
		if (root != null)
		{
			if (e.getSource() == newTileButton)
			{
				//Adding new tile to the list of tiles
				if (editType == EditType.BACKGROUND || editType == EditType.FOREGROUND) 
				{
					final File mapTilesFolder = new File(root.getPath() + FILE_SLASH + mapTileFolderName);
					JFileChooser fc = new JFileChooser(mapTilesFolder);
					fc.setAcceptAllFileFilterUsed(false);
					fc.setMultiSelectionEnabled(true);
					fc.setFileFilter(new FileFilter()
					{
						public boolean accept(File f) 
						{
							return f.getName().toLowerCase().endsWith("png");
						}

						public String getDescription() 
						{
							return "PNG";
						}
					});
					
					int val = fc.showOpenDialog(this);
					if (val == JFileChooser.APPROVE_OPTION)
					{
						File[] files = fc.getSelectedFiles();
						try
						{
							for (File f: files)
							{
								Color color = JColorChooser.showDialog(this, "Choose a preffered Color for tile: " + f.getName(), Color.white);
								if (color == null)
									continue;
							
								color = permuteColor(color, indexMap);
								BufferedImage img = ImageIO.read(f);
								tileMap.put(color.getRGB(), img);
								indexMap.put(color.getRGB(), f.getName());
								tileListModel.addElement(new ImageIcon(img, color.getRGB() + ""));
							}
							
							savedIndex = false;
						}
						catch(IOException ex)
						{
							ex.printStackTrace();
						}
					}
				}
				
				//Adding new Area to list of areas
				else if (editType == EditType.AREA_MAP) 
				{
					//Get a name for the new area from the user.
					String newAreaName = JOptionPane.showInputDialog(this, "Please specify a new area:");
					
					//Keep getting a name until something unused is found.
//					while (newAreaName != null && newAreaName.length() != 0){// && areaIndexMap.containsValue(newAreaName)
//						newAreaName = JOptionPane.showInputDialog(this, "The area \"" + newAreaName +"\" is already in use.\nPlease specify a new area:");
//					} 
					
					//Have a valid area name
					if (newAreaName != null && newAreaName.length() != 0) 
					{						
						//Area does not already exist.
						if (!areaIndexMap.containsValue(newAreaName)) 
						{
							//Get a color from the user for the new area.
							Color color = JColorChooser.showDialog(this, "Choose a preffered Color for area " +newAreaName, Color.white);
							if (color != null) 
							{
								color = permuteColor(color, areaIndexMap);
								
								//Save index file with new area
								File areaIndexFile = new File(root.getPath() + FILE_SLASH + mapAreaIndexFileName);  
							
								try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(areaIndexFile, true)))) 
								{
								    out.println("\"" + newAreaName + "\"\t\t" + (Long.toString(color.getRGB() & 0xFFFFFFFFL, 16) ));
								}
								catch (IOException ex) 
								{
									ex.printStackTrace();
								}
								
								//Add area to the list.
								areaListModel.addElement(new ImageIcon(textWithColor(newAreaName, color), color.getRGB() + ""));
								areaIndexMap.put(color.getRGB(), newAreaName);
							}
						}
						//Area exists, add to model.
						else 
						{
							int color = 0;
							
							for (Entry<Integer, String> es: areaIndexMap.entrySet())
							{
								if (es.getValue().equals(newAreaName)) 
								{
									color = es.getKey().intValue();
									break;
								}
							}
							
							areaListModel.addElement(new ImageIcon(textWithColor(newAreaName, new Color(color, true)), color + ""));
						}
					}
				}
				
			}
			else if (e.getSource() == mntmSave)
			{
				saveMap();
			}
			else if (e.getSource() == mntmNew || e.getSource() == mntmLoad)
			{
				if (!saved || (triggerData != null && !triggerData.isSaved()))
				{
					int val = JOptionPane.showConfirmDialog(this, "Save current file first?", "Unsaved changes", JOptionPane.YES_NO_CANCEL_OPTION);
					if (val == JOptionPane.CANCEL_OPTION)
						return;
					else if (val == JOptionPane.YES_OPTION)
					{
						saveMap();
					}
					else if (val == JOptionPane.NO_OPTION) 
					{
						//Reload items
						triggerData.reload();
					}
				}
				
				toolList.setSelectedIndex(0);
				
				if (e.getSource() == mntmNew)
				{
					String name = JOptionPane.showInputDialog(this, "Name the map");
					if (name == null)
						return;
				
					currentMapName = name;
					newMap();
					saveMap();
				}
				else
				{
					String[] mapList = getAvailableMaps();
					
					String val = (String)JOptionPane.showInputDialog(this, "Select a map", "Load", JOptionPane.PLAIN_MESSAGE, null, mapList, mapList[0]);
					if (val == null)
						return;

					currentMapName = val;
					loadMap();
				}
				lblMapName.setText(currentMapName);
				draw();
			}
			else if (e.getSource() == mntmCut) 
			{
				selectTool.cut();
			}
			else if (e.getSource() == mntmCopy) 
			{
				selectTool.copy();
			}
			else if (e.getSource() == mntmPaste) 
			{
				toolList.setSelectedIndex(4);
				selectTool.paste();
			}
		}
		else if (e.getSource() == mntmSetRoot)
		{
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
			int val = fc.showOpenDialog(this);
			if (val == JFileChooser.APPROVE_OPTION)
			{
				File newRoot = fc.getSelectedFile();
				setRoot(newRoot);
			}
		}
	}
	
	public String[] getAvailableMaps() 
	{
		File mapFolder = new File(root.getPath() + FILE_SLASH + mapFolderName);
		String[] mapList = mapFolder.list(new FilenameFilter()
		{
			public boolean accept(File dir, String name) 
			{
				return !dir.isHidden() && !name.contains(".");
			}
			
		});
		
		return mapList;
	}

	void setRoot(File newRoot)
	{
		System.out.println("root set to: " + newRoot);
		File recFolder = new File(newRoot.getPath() + FILE_SLASH + recFolderNme);
		
		if (!recFolder.exists())
		{
			int create = JOptionPane.showConfirmDialog(this, "No rec folder exists! Create one?", "WAT", JOptionPane.OK_CANCEL_OPTION);
			if (create == JOptionPane.OK_OPTION)
			{
				recFolder.mkdir();
			}
			else
			{
				return;
			}
		}
		
		root = newRoot;
		lblRoot.setText(root.getPath());
		lblRoot.setForeground(Color.black);
		
		File tilesFolder = new File(newRoot.getPath() + FILE_SLASH + tileFolderName);
		if (!tilesFolder.exists())
			tilesFolder.mkdir();
		
		File mapTilesFolder = new File(newRoot.getPath() + FILE_SLASH + mapTileFolderName);
		if (!mapTilesFolder.exists())
			mapTilesFolder.mkdir();
		
		File mapsFolder = new File(newRoot.getPath() + FILE_SLASH + mapFolderName);
		if (!mapsFolder.exists())
			mapsFolder.mkdir();
		
		loadTiles();
		loadAreas();
		loadMapMakerTiles();
		loadTrainerTiles();
		loadItemTiles();
		
		loadTriggerModel();
	}
	
	private Color permuteColor(Color color, HashMap<Integer,String> index) 
	{
		int dr = color.getRed() < 128 ? 1 : -1;
		int dg = color.getGreen() < 128 ? 1 : -1;
		int db = color.getBlue() < 128 ? 1 : -1;
	
		while (index.containsKey(color.getRGB()))
		{
			int r = color.getRed() + dr;
			int g = color.getGreen() + dg;
			int b = color.getBlue() + db;

			color = new Color(r, g, b);
		}
		
		return color;
	}

	private void loadTiles() 
	{
		File mapTilesFolder = new File(root.getPath() + FILE_SLASH + mapTileFolderName);
		File indexFile = new File(root.getPath() + FILE_SLASH + mapTileIndexFileName);
	
		tileMap = new HashMap<>();
		indexMap = new HashMap<>();
		tileListModel.clear();
		tileListModel.addElement(new ImageIcon(filledImage(Color.magenta), "0"));
		
		if (indexFile.exists())
		{
			try 
			{
				Scanner in = new Scanner(indexFile);
				while (in.hasNext())
				{
					String name = in.next();
					int val = (int)Long.parseLong(in.next(), 16);
					
					BufferedImage img = ImageIO.read(new File(mapTilesFolder + FILE_SLASH + name));
					BufferedImage resizedImg = img.getSubimage(0, 0, Math.min(img.getWidth(), tileSize*3), Math.min(img.getHeight(), tileSize*3));
				
					tileListModel.addElement(new ImageIcon(resizedImg, val + ""));
					tileMap.put(val, img);
					indexMap.put(val, name);
				}
				
				in.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	private static final Pattern mapAreaPattern = Pattern.compile("\"([^\"]*)\"\\s*(\\p{XDigit}+)");
	private void loadAreas() 
	{
		File areaIndexFile = new File(root.getPath() + FILE_SLASH + mapAreaIndexFileName);  
		areaIndexMap = new HashMap<>();
		areaListModel.clear();
		
		if (areaIndexFile.exists()) 
		{	
			String fileText = Global.readEntireFile(areaIndexFile, false);
			Matcher m = mapAreaPattern.matcher(fileText);
			
			while (m.find())
			{
				String name = m.group(1);
				int val = (int)Long.parseLong(m.group(2), 16);
				
				//areaListModel.addElement(new ImageIcon(textWithColor(name, new Color(val, true)), val + ""));
				areaIndexMap.put(val, name);
			}
		}
	}
	
	private void loadTrainerTiles() 
	{	
		File trainerTilesFolder = new File(root.getPath() + FILE_SLASH + trainerTileFolderName);
		File trainerIndexFile = new File(root.getPath() + FILE_SLASH + trainerTileIndexFileName);
		
		trainerTileMap = new HashMap<>();
		trainerIndexMap = new HashMap<>();
		
		if (trainerIndexFile.exists())
		{
			try 
			{
				Scanner in = new Scanner(trainerIndexFile);
				
				while (in.hasNext())
				{
					String name = in.next();
					int val = (int)Long.parseLong(in.next(), 16);
					
					File imageFile = new File(trainerTilesFolder + FILE_SLASH + name);
					
					if (!imageFile.exists())
						continue;
					
					BufferedImage img = ImageIO.read(imageFile);
					
					trainerTileMap.put(val, img);
					trainerIndexMap.put(val, name);
				}
				
				in.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	private void loadMapMakerTiles() 
	{	
		File mapMakerTilesFolder = new File(root.getPath() + FILE_SLASH + mapMakerTileFolderName);
		File mapMakerIndexFile = new File(root.getPath() + FILE_SLASH + mapMakerTileIndexFileName);
		
		mapMakerTileMap = new HashMap<>();
		mapMakerTileIndexMap = new HashMap<>();
		
		if (mapMakerIndexFile.exists())
		{
			try 
			{
				Scanner in = new Scanner(mapMakerIndexFile);
				
				while (in.hasNext())
				{
					String name = in.next();
					int val = (int)Long.parseLong(in.next(), 16);
					
					File imageFile = new File(mapMakerTilesFolder + FILE_SLASH + name);
					
					if (!imageFile.exists())
						continue;
					
					BufferedImage img = ImageIO.read(imageFile);
					
					mapMakerTileMap.put(val, img);
					mapMakerTileIndexMap.put(val, name);
				}
				
				in.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	private void loadItemTiles() 
	{
		File itemTilesFolder = new File(root.getPath() + FILE_SLASH + itemTileFolderName);
		File itemIndexFile = new File(root.getPath() + FILE_SLASH + itemTileIndexFileName);
		
		itemTileMap = new HashMap<>();
		itemIndexMap = new HashMap<>();
		
		if (itemIndexFile.exists())
		{
			try 
			{
				Scanner in = new Scanner(itemIndexFile);
			
				while (in.hasNext())
				{
					String name = in.next();
					int val = (int)Long.parseLong(in.next(), 16);
					
					File imageFile = new File(itemTilesFolder + FILE_SLASH + name);
					
					if (!imageFile.exists())
						continue;
					
					BufferedImage img = ImageIO.read(imageFile);
					
					itemTileMap.put(val, img);
					itemIndexMap.put(val, name);
				}
				
				in.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	private void loadTriggerModel() 
	{	
		triggerListModel.clear();
		
		//NPC, map transition triggers, wild battle triggers
		//TODO: Pokecenter transition, transition buildings
		triggerListModel.addElement(new ImageIcon(imageWithText(trainerTileMap.get(0x0), "Item"), "0")); 				//Item
		triggerListModel.addElement(new ImageIcon(imageWithText(trainerTileMap.get(0x00000040), "NPC"), "1"));			//Trainer
		triggerListModel.addElement(new ImageIcon(imageWithText(mapMakerTileMap.get(0x4), "Trigger Entity"), "2"));		//Trigger Entity
		triggerListModel.addElement(new ImageIcon(imageWithText(mapMakerTileMap.get(0x3), "Wild Battle"), "3"));		//Wild battle
		triggerListModel.addElement(new ImageIcon(imageWithText(mapMakerTileMap.get(0x2), "Map Exit"), "4")); 			//transition exit trigger
		triggerListModel.addElement(new ImageIcon(imageWithText(mapMakerTileMap.get(0x1), "Map Entrance"), "5")); 		//transition enter trigger
		triggerListModel.addElement(new ImageIcon(imageWithText(mapMakerTileMap.get(0x5), "PokeCenter"), "6")); 		//PokeCenter transition trigger
		triggerListModel.addElement(new ImageIcon(imageWithText(mapMakerTileMap.get(0x6), "Transition Building"), "7")); 	//Transition Building transition trigger
		//triggerListModel.addElement(new ImageIcon(imageWithText(mapMakerTileMap.get(0xd), "Group"), "8")); 				//Group trigger
		triggerListModel.addElement(new ImageIcon(imageWithText(mapMakerTileMap.get(0xc), "Event"), "8")); 				//Event trigger
		
	}
	
	public void saveMap()
	{
		if (root == null)
			return;
		
		saveIndex();
		
		if (currentMapBg == null)
			return;
		
		saveTriggers();
		
		File mapFolder = new File(root.getPath() + FILE_SLASH + mapFolderName + FILE_SLASH + currentMapName);
		
		if (!mapFolder.exists())
			mapFolder.mkdir();
		
		File mapBgFile = new File(mapFolder.getPath() + FILE_SLASH + currentMapName + "_bg.png");
		File mapFgFile = new File(mapFolder.getPath() + FILE_SLASH + currentMapName + "_fg.png");
		File mapMoveFile = new File(mapFolder.getPath() + FILE_SLASH + currentMapName + "_move.png");
		File mapAreaFile = new File(mapFolder.getPath() + FILE_SLASH + currentMapName + "_area.png");
		
		try 
		{
			ImageIO.write(currentMapBg, "png", mapBgFile);
			ImageIO.write(currentMapFg, "png", mapFgFile);
			ImageIO.write(currentMapMove, "png", mapMoveFile);
			ImageIO.write(currentMapArea, "png", mapAreaFile);
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		saved = true;
	}
	
	public void saveIndex()
	{
		if (savedIndex)
			return;
	
		savedIndex = true;
		
		File indexFile = new File(root.getPath() + FILE_SLASH + mapTileIndexFileName);
		FileWriter writer;
		
		try 
		{
			writer = new FileWriter(indexFile);
		
			for (Entry<Integer, String> e: indexMap.entrySet())
				writer.write(e.getValue() + " " + Integer.toString(e.getKey(), 16) + "\n");
			
			writer.close();
		}
		catch (IOException ex) 
		{
			ex.printStackTrace();
		}
	}
	
	public void saveTriggers() 
	{
		File mapTextFile = new File(root.getPath() + FILE_SLASH + mapFolderName + FILE_SLASH + currentMapName + FILE_SLASH + currentMapName + ".txt");
		
		//Save all triggers
		triggerData.saveTriggers(mapTextFile);
	}
	
	public void loadMap()
	{
		File mapBgImageFile = new File(root.getPath() + FILE_SLASH + mapFolderName + FILE_SLASH + currentMapName + FILE_SLASH + currentMapName + "_bg.png");
		File mapFgImageFile = new File(root.getPath() + FILE_SLASH + mapFolderName + FILE_SLASH + currentMapName + FILE_SLASH + currentMapName + "_fg.png");
		File mapMoveImageFile = new File(root.getPath() + FILE_SLASH + mapFolderName + FILE_SLASH + currentMapName + FILE_SLASH + currentMapName + "_move.png");
		File mapAreaImageFile = new File(root.getPath() + FILE_SLASH + mapFolderName + FILE_SLASH + currentMapName + FILE_SLASH + currentMapName + "_area.png");
		
		File mapTextFile = new File(root.getPath() + FILE_SLASH + mapFolderName + FILE_SLASH + currentMapName + FILE_SLASH + currentMapName + ".txt");
		
		areaListModel.clear();
		areasOnMap = new HashSet<>();
		areasOnMap.add(0);
		areaListModel.addElement(new ImageIcon(textWithColor(areaIndexMap.get(0), new Color(0, true)), 0 + ""));
		
		try 
		{
			currentMapBg = ImageIO.read(mapBgImageFile);
			currentMapFg = ImageIO.read(mapFgImageFile);
			currentMapMove = ImageIO.read(mapMoveImageFile);
			
			//Check to see if area file exists
			if (mapAreaImageFile.exists()) 
			{
				currentMapArea = ImageIO.read(mapAreaImageFile);
				
				for (int x = 0; x < currentMapBg.getWidth(); ++x) 
				{
					for (int y = 0; y < currentMapBg.getHeight(); ++y) 
					{
						int rgb = currentMapArea.getRGB(x, y);
						if (!areasOnMap.contains(rgb) && areaIndexMap.containsKey(rgb)) 
						{
							areasOnMap.add(rgb);
							areaListModel.addElement(new ImageIcon(textWithColor(areaIndexMap.get(rgb), new Color(rgb, true)), rgb + ""));
						}
					}
				}
			}
			//If file doesn't exist, create it instead of crashing.
			else
				currentMapArea = new BufferedImage(currentMapBg.getWidth(), currentMapBg.getHeight(), BufferedImage.TYPE_INT_ARGB);
			
			currentMapSize = new Dimension(currentMapBg.getWidth(), currentMapBg.getHeight());
			mapX = mapY = 0;
			
			triggerData = new MapMakerTriggerData(currentMapName, currentMapSize, this, mapTextFile);
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		saved = true;
	}
	
	public void newMap()
	{
		currentMapBg = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		currentMapFg = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		currentMapMove = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		currentMapArea = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		
		currentMapSize = new Dimension(10, 10);
		mapX = mapY = 0;
		
		Graphics g = currentMapMove.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, currentMapSize.width, currentMapSize.height);
		g.dispose();
		
		//Create empty trigger data structure
		triggerData = new MapMakerTriggerData(currentMapName, currentMapSize, this);
		
		areaListModel.clear();
		areasOnMap = new HashSet<>();
		areasOnMap.add(0);
		areaListModel.addElement(new ImageIcon(textWithColor(areaIndexMap.get(0), new Color(0, true)), 0 + ""));
	}
	
	public Point setTile(int x, int y, int val)
	{
		Point returnPoint = new Point(0,0);
		
		if (x < 0 || y < 0 || x >= currentMapSize.width || y >= currentMapSize.height)
		{
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
			g.setColor(Color.black);
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
			triggerData.moveTriggerData(startX, startY, new Dimension(newWidth, newHeight));
			returnPoint = new Point(startX, startY);
			
			currentMapSize = new Dimension(newWidth, newHeight);
			
			x += startX;
			y += startY;
		}
		
		saved = false;
		switch (editType)
		{
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
	
	public int getTile(int x, int y, EditType type)
	{
		if (x < 0 || y < 0 || x >= currentMapSize.width || y >= currentMapSize.height)
			return 0;
		
		switch (type)
		{
			case BACKGROUND: 
				return currentMapBg.getRGB(x, y);
			case FOREGROUND: 
				return currentMapFg.getRGB(x, y);
			case MOVE_MAP: 
				return currentMapMove.getRGB(x, y);
			case AREA_MAP: 
				return currentMapArea.getRGB(x, y);
			case TRIGGERS: 
				return 0; 
		}
		
		return 0;
	}
	
	public void draw()
	{
		if (currentMapBg == null)
			return;
		
		BufferedImage buffer = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = buffer.getGraphics();
		
		for (int x = 0; x <= canvas.getWidth()/16; x++)
		{
			for (int y = 0; y <= canvas.getHeight()/16; y++)
			{
				g.setColor(((x^y)&1) == 0 ? Color.GRAY : Color.LIGHT_GRAY);
				g.fillRect(x*16, y*16, 16, 16);
			}
		}
		
		Graphics2D g2d = (Graphics2D)g;
		if (alphaComposite == null)
		{
			defaultComposite = g2d.getComposite();
			alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.5f);
		}
		
		switch (editType)
		{
			case AREA_MAP:
				//Drawing of area map handled in MOVE_MAP case.
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
		
		
		if (editType != EditType.TRIGGERS) 
		{
			//Draw all trigger items at half transparency. 
			g2d.setComposite(alphaComposite);
		}
		else 
		{
			g2d.setComposite(defaultComposite);
		}
		
		//Draw all trigger items.
		triggerData.drawTriggers(g2d, mapX, mapY);
		
		g2d.setComposite(defaultComposite);
		
		if (!toolList.isSelectionEmpty())
			toolList.getSelectedValue().draw(g);
		
		//Draw area label on mouse hover
		if (editType == EditType.AREA_MAP)
		{
			int x = (mouseHoverX - mapX)/tileSize;
			int y = (mouseHoverY - mapY)/tileSize;
			int tileColor = getTile(x, y, EditType.AREA_MAP);
		
			String areaName = areaIndexMap.get(tileColor);
			
			if (areaName == null) areaName = areaIndexMap.get(0);
			g2d.setColor(Color.black);
			g2d.setFont(getFont().deriveFont(16).deriveFont(Font.BOLD));
			g2d.drawString(areaName, mouseHoverX, mouseHoverY);
		}
		
		g.dispose();
		
		g = canvas.getGraphics();
		g.drawImage(buffer, 0, 0, null);
		g.dispose();
	}
	
	void drawImg(Graphics g, EditType type)
	{
		for (int dy = mapY, y = 0; y < currentMapSize.height; y++, dy+=tileSize)
		{
			for (int dx = mapX, x = 0; x < currentMapSize.width; x++, dx+=tileSize)
			{
				int val = getTile(x, y, type);
				
				if (type == EditType.MOVE_MAP || type == EditType.AREA_MAP)
				{
					g.setColor(new Color(val, true));
					g.fillRect(dx, dy, tileSize, tileSize);
				}
				else if (type != EditType.TRIGGERS && tileMap.containsKey(val))
				{
					BufferedImage img = tileMap.get(val);
					g.drawImage(img, dx - img.getWidth() + tileSize,dy - img.getHeight() + tileSize, null);
				}
			}
		}
	}

	public void mouseClicked(MouseEvent e) 
	{
		if (!toolList.isSelectionEmpty())
			toolList.getSelectedValue().click(e.getX(), e.getY());
	
		draw();
	}

	public void mouseEntered(MouseEvent arg0) { }
	public void mouseExited(MouseEvent arg0) { }

	public void mousePressed(MouseEvent e) 
	{
		if (!toolList.isSelectionEmpty())
			toolList.getSelectedValue().pressed(e.getX(), e.getY());
	
		draw();
	}

	public void mouseReleased(MouseEvent e) 
	{
		if (!toolList.isSelectionEmpty())
			toolList.getSelectedValue().released(e.getX(), e.getY());
	
		draw();
	}

	public void mouseDragged(MouseEvent e) 
	{
		mouseHoverX = e.getX();
		mouseHoverY = e.getY();
	
		if (!toolList.isSelectionEmpty())
			toolList.getSelectedValue().drag(e.getX(), e.getY());
	
		draw();
	}

	public void mouseMoved(MouseEvent e) 
	{
		mouseHoverX = e.getX();
		mouseHoverY = e.getY();
	
		draw();
	}
	
	public void keyTyped(KeyEvent e) { }

	int previousToolListIndex = -1;
	boolean controlKeyDown = false;

	public void keyPressed(KeyEvent e) 
	{		
		// TODO: e for eraser, s for single, r for rect, t for trigger, ? for select?
		if (e.getKeyCode() == KeyEvent.VK_SPACE && previousToolListIndex == -1 && !toolList.isSelectionEmpty()) 
		{
			previousToolListIndex = toolList.getSelectedIndex();
			toolList.setSelectedIndex(0);
		}
		else if (e.getKeyCode() == KeyEvent.VK_1)
		{
			toolList.setSelectedIndex(0);
		}
		else if (e.getKeyCode() == KeyEvent.VK_2)
		{
			toolList.setSelectedIndex(1);
		}
		else if (e.getKeyCode() == KeyEvent.VK_3)
		{
			toolList.setSelectedIndex(2);
		}
		else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			if (editType == EditType.TRIGGERS && triggerData.hasPlaceableTrigger()) 
			{
				triggerData.clearPlaceableTrigger();
				toolList.clearSelection();
			}
		}
		
		if (e.getModifiers() == Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()) 
		{
			controlKeyDown = true;
		}
	}

	public void keyReleased(KeyEvent e) 
	{
		if (e.getKeyCode() == KeyEvent.VK_SPACE && previousToolListIndex != -1)
		{
			toolList.setSelectedIndex(previousToolListIndex);
			previousToolListIndex = -1; 
		}
		
		if (e.getModifiers() != Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()) 
		{
			controlKeyDown = false;
		}
	}

	public void valueChanged(ListSelectionEvent e) 
	{	
		if (e.getSource() == tileList) 
		{
			//When a trigger item selected
			if (tileList.getModel().equals(triggerListModel) && !tileList.isSelectionEmpty() && !e.getValueIsAdjusting()) 
			{	
				if (currentMapBg == null)
					tileList.clearSelection();
				else 
				{
					//Already something placeable, ignore trying to create something new.
					if (!triggerData.hasPlaceableTrigger()) 
					{
						//Trigger was not created, deselect item
						if (!triggerData.createTrigger(tileList.getSelectedValue().getDescription())) 
						{
							tileList.clearSelection();
						}
						//Trigger was created, move to single selection
						else 
						{
							toolList.setSelectedIndex(1);
						}
					}
					else if (!triggerToolMoveSelected)
					{ 
						triggerData.clearPlaceableTrigger();
						tileList.clearSelection();
					}
					
					triggerToolMoveSelected = false;
				}
			}
		}
		else if (e.getSource() == toolList) 
		{
			if (!toolList.isSelectionEmpty() && !e.getValueIsAdjusting()) 
			{
				toolList.getSelectedValue().reset();
				if (toolList.getSelectedValue() != selectTool) 
				{
					mntmCopy.setEnabled(false);
					mntmCut.setEnabled(false);
				}
				else if (selectTool.hasSelection())
				{
					mntmCopy.setEnabled(true);
					mntmCut.setEnabled(true);
				}
			}
		}
	}
	
	public BufferedImage getTileFromSet(String set, int index) 
	{
		BufferedImage img = null;
		
		switch (set) 
		{
			case "MapMaker": 
				img = mapMakerTileMap.get(index); 
				break;
			case "Trainer":
				img = trainerTileMap.get(index); 
			break;
			case "Item": 
				img = itemTileMap.get(index); 
				break;
			case "Map": 
				img = tileMap.get(index); 
				break;
		}
		
		return img;
	}
	
	public String getAreaForIndex(int index) 
	{
		return areaIndexMap.get(index);
	}
	
	private class ToolRenderer extends JLabel implements ListCellRenderer<Tool>
	{
		private static final long serialVersionUID = 6750963470094004328L;

		public Component getListCellRendererComponent(
				JList<? extends Tool> list, Tool value, int index, boolean isSelected,
				boolean hasFocus) 
		{
			String s = value.toString();
			setText(s);
			//setIcon((s.length() > 10) ? longIcon : shortIcon);
			if (isSelected) 
			{
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			}
			else 
			{
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
			
			return this;
		}
	}
	
	private interface Tool
	{
		public void click(int x, int y);
		public void released(int x, int y);
		public void pressed(int x, int y);
		public void drag(int x, int y);
		public void draw(Graphics g);
		public void reset();
	}
	
	private class MoveTool implements Tool
	{
		private int prevX, prevY;

		public void click(int x, int y) { }
		public void drag(int x, int y) 
		{
			mapX -= prevX - x;
			mapY -= prevY - y;
			prevX = x;
			prevY = y;
		}

		public String toString() 
		{
			return "Move";
		}

		public void draw(Graphics g) { }
		public void released(int x, int y) { }

		public void pressed(int x, int y) 
		{
			prevX = x;
			prevY = y;
		}

		public void reset() 
		{
			prevX = mouseHoverX;
			prevY = mouseHoverY;
		}
	}
	
	private class SingleClickTool implements Tool
	{
		public void click(int x, int y) 
		{
			if (tileList.isSelectionEmpty())
				return;
		
			x = (int)Math.floor((x - mapX)*1.0/tileSize);
			y = (int)Math.floor((y - mapY)*1.0/tileSize);
			
			System.out.println("click: " + x+" " + y);
			
			int val = Integer.parseInt(tileList.getSelectedValue().getDescription());
			setTile(x,y,val);
			
			if (editType == EditType.TRIGGERS) 
			{
				triggerData.clearPlaceableTrigger();
				toolList.setSelectedIndex(3);
			}
		}

		public String toString() 
		{
			return "Single";
		}
		
		public void draw(Graphics g) 
		{
			int mhx = (int)Math.floor((mouseHoverX - mapX)*1.0/tileSize);
			int mhy = (int)Math.floor((mouseHoverY - mapY)*1.0/tileSize);

			g.setColor(Color.red);
			g.drawRect(mhx*tileSize + mapX, mhy*tileSize + mapY, tileSize, tileSize);
			
			if (tileList.isSelectionEmpty())
				return;
			
			//Show preview image for normal map tiles
			if (editType == EditType.BACKGROUND || editType == EditType.FOREGROUND) 
			{
				int val = Integer.parseInt(tileList.getSelectedValue().getDescription());
				if (!tileMap.containsKey(val))
					return;
			
				BufferedImage img = tileMap.get(val);
				g.drawImage(tileMap.get(val), mhx*tileSize + mapX - img.getWidth() + tileSize, mhy*tileSize + mapY - img.getHeight() + tileSize, null);
			}
			//Show preview image for current trigger
			else if (editType == EditType.TRIGGERS) 
			{
				BufferedImage img = null;

				switch (tileList.getSelectedValue().getDescription()) 
				{
					case "0": 
						img = trainerTileMap.get(0);
						break; 
					case "1": 
						NPCEntityData npc = (NPCEntityData) triggerData.getPlaceableTrigger().entity;
						img = trainerTileMap.get(12*npc.spriteIndex + 1 +npc.defaultDirection);
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
				
				if (img != null)
					g.drawImage(img,mhx*tileSize + mapX - img.getWidth()/2 + tileSize/2, mhy*tileSize + mapY - img.getHeight() + tileSize, null);
				else if (tileList.getSelectedValue().getDescription() == "7") 
				{
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
	private class RectangleTool implements Tool
	{
		private int startX, startY;
		private boolean pressed = false;

		public void click(int x, int y) { }

		public void released(int x, int y) 
		{
			if (tileList.isSelectionEmpty() || !pressed)
				return;
		
			pressed = false;

			int mhx = (int)Math.floor((mouseHoverX - mapX)*1.0/tileSize);
			int mhy = (int)Math.floor((mouseHoverY - mapY)*1.0/tileSize);
			
			int tx = Math.min(startX, mhx);
			int ty = Math.min(startY, mhy);
			int bx = Math.max(startX, mhx);
			int by = Math.max(startY, mhy);
			
			int width = bx - tx;
			int height = by - ty;
			
			int val = Integer.parseInt(tileList.getSelectedValue().getDescription());
			for (int i = 0; i <= width; i++)
			{
				for (int j = 0; j <= height; j++) 
				{
					Point delta = setTile(tx + i, ty + j, val);
				
					if (delta.x != 0) 
					{
						tx += delta.x;
					}
					
					if (delta.y != 0) 
					{
						ty += delta.y;
					}
				}
			}
			
			if (editType == EditType.TRIGGERS) 
			{
				triggerData.clearPlaceableTrigger();
				toolList.setSelectedIndex(3);
			}
		}

		public void pressed(int x, int y) 
		{
			startX = (int)Math.floor((x - mapX)*1.0/tileSize);
			startY = (int)Math.floor((y - mapY)*1.0/tileSize);
		
			pressed = true;
		}

		public void drag(int x, int y) { }

		public void draw(Graphics g) 
		{
			if (!pressed)
				return;
		
			int mhx = (int)Math.floor((mouseHoverX - mapX)*1.0/tileSize);
			int mhy = (int)Math.floor((mouseHoverY - mapY)*1.0/tileSize);
			
			int tx = Math.min(startX, mhx);
			int ty = Math.min(startY, mhy);
			int bx = Math.max(startX, mhx);
			int by = Math.max(startY, mhy);
			
			g.setColor(Color.red);
			g.drawRect(tx*tileSize + mapX, ty*tileSize + mapY, tileSize*(bx - tx + 1), tileSize*(by - ty + 1));
		}

		public String toString() 
		{
			return "Rectangle";
		}
		
		public void reset() 
		{
			pressed = false;
		}
	}

	private boolean triggerToolMoveSelected = false;
	
	private class TriggerTool implements Tool 
	{
		private JPopupMenu triggerListPopup;
		private JPopupMenu triggerOptionsPopup;
		
		private int mouseX;
		private int mouseY;
		
		private PlaceableTrigger[] triggers;
		private PlaceableTrigger selectedTrigger;
		
		public TriggerTool() 
		{
			selectedTrigger = null;
			triggerListPopup = new JPopupMenu();
			triggerOptionsPopup  = new JPopupMenu();
			
			JMenuItem editItem = new JMenuItem("Edit");
			triggerOptionsPopup.add(editItem);
			
			editItem.addActionListener(new ActionListener() 
			{
		        public void actionPerformed(ActionEvent e) 
		        {
		        	//System.out.println("Edit");
		        	
		          triggerData.editTrigger(selectedTrigger);
		        }
		    });
			
			JMenuItem moveItem = new JMenuItem("Move");
			triggerOptionsPopup.add(moveItem);
			moveItem.addActionListener(new ActionListener() 
			{
				public void actionPerformed(ActionEvent e) 
		        {
		        	toolList.setSelectedIndex(1);
		        	int index = triggerData.getTriggerTypeIndex(selectedTrigger);
		        	if (index != -1) 
		        	{
		        		editTypeComboBox.setSelectedIndex(4);
		        	  
		        		triggerData.moveTrigger(selectedTrigger);
		        	 	triggerToolMoveSelected = true;
		        	  
		        	 	tileList.setSelectedIndex(index);
		        	}
		        }
			});
			
			JMenuItem removeItem = new JMenuItem("Remove");
			triggerOptionsPopup.add(removeItem);
			removeItem.addActionListener(new ActionListener() 
			{
		        public void actionPerformed(ActionEvent e) 
		        {
		        	//System.out.println("Remove");
		        	
		          triggerData.removeTrigger(selectedTrigger);
		        }
			});
		}
		
		public String toString() 
		{
			return "Trigger";
		}
		
		public void click(int x, int y) 
		{
			if (currentMapName == null)
				return;
			
			mouseX = x;
			mouseY = y;
			
			x = (int)Math.floor((x - mapX)*1.0/tileSize);
			y = (int)Math.floor((y - mapY)*1.0/tileSize);
			System.out.println("Trigger click: " + x + " " + y);
			
			triggers = triggerData.getTrigger(x, y);
			triggerListPopup.removeAll();
			
			for (PlaceableTrigger trigger: triggers) 
			{
			    JMenuItem menuItem = new JMenuItem(trigger.name + " (" + trigger.triggerType + ")");
			    triggerListPopup.add(menuItem);
			    menuItem.addActionListener(new ActionListener()
			    {
			        public void actionPerformed(ActionEvent e) 
			        {
			        	Component[] components = triggerListPopup.getComponents();
			        	//If someone reads this, please suggest a better way to find the index of the selected item...
			        	for (Component component: components) 
			        	{
			        		if (((JMenuItem)component).getText().equals(e.getActionCommand())) 
			        		{
			        			for (int currTrigger = 0; currTrigger < triggers.length; ++currTrigger) 
			        			{
			        				if (e.getActionCommand().equals(triggers[currTrigger].name +" (" + triggers[currTrigger].triggerType + ")")) 
			        				{
			        					//System.out.println("Clicked " + e.getActionCommand());
			        					selectedTrigger = triggers[currTrigger];
			        					break;
			        				}
			        			}
			        		}
			        	}
			          
			          //triggerListPopup.removeAll();
			          triggerOptionsPopup.show(canvas, mouseX, mouseY);
			        }
			    });
			}
			
			triggerListPopup.show(canvas, mouseX, mouseY);
		}
		
		public void draw(Graphics g) 
		{
			int mhx = (int)Math.floor((mouseHoverX - mapX)*1.0/tileSize);
			int mhy = (int)Math.floor((mouseHoverY - mapY)*1.0/tileSize);
			
			g.setColor(Color.blue);
			g.drawRect(mhx*tileSize + mapX, mhy*tileSize + mapY, tileSize, tileSize);
			
			if (tileList.isSelectionEmpty())
				return;
			
		}
		
		public void released(int x, int y) { }
		public void pressed(int x, int y) { }
		public void drag(int x, int y) { }
		public void reset() { }
	}

	//Select tool so you can copy/cut/paste
	private class SelectTool implements Tool 
	{	
		private boolean paste;
		private boolean selected;
		private boolean controlClick;
		
		private BufferedImage copiedTiles = null;
		private EditType copiedEditType;
		
		private int startX, startY;
		private int tx, ty, bx, by;
		
		private boolean pressed = false;

		public void click(int x, int y) 
		{ 
			if (!paste || controlClick) 
				return;
			
			saved = false;
			
			x = (int)Math.floor((x - mapX)*1.0/tileSize);
			y = (int)Math.floor((y - mapY)*1.0/tileSize);
			
			for (int currX = 0; currX < copiedTiles.getWidth(); ++currX) 
			{
				for (int currY = 0; currY < copiedTiles.getHeight(); ++currY) 
				{
					int val = copiedTiles.getRGB(currX, currY);
					Point delta = setTile(x + currX, y + currY, val);

					if (delta.x != 0) 
					{
						x += delta.x;
					}
					
					if (delta.y != 0) 
					{
						y += delta.y;
					}
				}
			}
			
			paste = false;
		}

		public void released(int x, int y) 
		{
			if (editType == EditType.TRIGGERS || paste || !pressed)
				return;
			
			pressed = false;
			select();

			int mhx = (int)Math.floor((mouseHoverX - mapX)*1.0/tileSize);
			int mhy = (int)Math.floor((mouseHoverY - mapY)*1.0/tileSize);
			
			tx = Math.max(Math.min(startX, mhx), 0);
			ty = Math.max(Math.min(startY, mhy), 0);
			bx = Math.min(Math.max(startX, mhx), currentMapSize.width - 1);
			by = Math.min(Math.max(startY, mhy), currentMapSize.height - 1);
			
			if (tx > bx || ty > by) 
			{
				deselect();
			}
		}

		public void pressed(int x, int y) 
		{
			if (editType == EditType.TRIGGERS || paste)
				return;
			
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
		
		public void drag(int x, int y) 
		{ 
//			if (controlClick) {
//				controlClick = false;
//				paste();
//				//return;
//			}
//			
//			click(x, y);
		}

		public void draw(Graphics g) 
		{
			if (!pressed && !selected && !paste) 
			{
				return;
			}
			
			int mhx = (int)Math.floor((mouseHoverX - mapX)*1.0/tileSize);
			int mhy = (int)Math.floor((mouseHoverY - mapY)*1.0/tileSize);
			
			//int tx, ty, bx, by;
			if (!selected) 
			{
				tx = Math.max(Math.min(startX, mhx), 0);
				ty = Math.max(Math.min(startY, mhy), 0);
				bx = Math.min(Math.max(startX, mhx), currentMapSize.width - 1);
				by = Math.min(Math.max(startY, mhy), currentMapSize.height - 1);
			}
			
			if (!paste) 
			{
				g.setColor(Color.red);
				g.drawRect(tx*tileSize + mapX, ty*tileSize + mapY, tileSize*(bx - tx + 1), tileSize*(by - ty + 1));
			}
			else 
			{
				//Show preview image for all pasting tiles.
				for (int currX = 0; currX < copiedTiles.getWidth(); ++currX) 
				{
					for (int currY = 0; currY < copiedTiles.getHeight(); ++currY) 
					{
						int val = copiedTiles.getRGB(currX, currY);
						if (editType == EditType.BACKGROUND || editType == EditType.FOREGROUND) 
						{
							if (!tileMap.containsKey(val))
								continue;
						
							BufferedImage img = tileMap.get(val);
							g.drawImage(tileMap.get(val), (mhx + currX)*tileSize + mapX - img.getWidth() + tileSize, (mhy + currY)*tileSize + mapY - img.getHeight() + tileSize, null);
						}
						else if (editType == EditType.MOVE_MAP || editType == EditType.AREA_MAP) 
						{
							g.setColor(new Color(val));
							g.fillRect((mhx + currX)*tileSize + mapX, (mhy + currY)*tileSize + mapY, tileSize, tileSize);
						}
					}
				}
				
				g.setColor(Color.red);
				g.drawRect(mhx*tileSize + mapX, mhy*tileSize + mapY, tileSize*copiedTiles.getWidth(), tileSize*copiedTiles.getHeight());
			}
		}
		
		public void reset() 
		{
			pressed = false;
		}

		public String toString() 
		{
			return "Select";
		}
		
		public boolean hasSelection() 
		{
			return selected;
		}
		
		public boolean canPaste() 
		{
			return copiedEditType == editType && copiedTiles != null;
		}
		
		public void select() 
		{
			selected = true;
			mntmCopy.setEnabled(true);
			mntmCut.setEnabled(true);
		}
		
		public void deselect() 
		{
			selected = false;
			mntmCopy.setEnabled(false);
			mntmCut.setEnabled(false);
		}
		
		public void copy() 
		{
			copiedEditType = editType;
			
			BufferedImage currentMapImage = null;
			if (editType == EditType.FOREGROUND) 
			{
				currentMapImage = currentMapFg;
			}
			else if (editType == EditType.BACKGROUND) 
			{
				currentMapImage = currentMapBg;
			}
			else if (editType == EditType.MOVE_MAP) 
			{
				currentMapImage = currentMapMove;
			}
			else if (editType == EditType.AREA_MAP) 
			{
				currentMapImage = currentMapArea;
			}
			
			int width = bx - tx + 1;
			int height = by - ty + 1;
			copiedTiles = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			
			copiedTiles.setRGB(0, 0, width, height, 
					currentMapImage.getRGB(tx, ty, width, height, null, 0, width), 
					0, width);
			
			if (!mntmPaste.isEnabled()) mntmPaste.setEnabled(true);
		}
		
		public void cut() 
		{
			copy();
			
			int val = Integer.parseInt(tileList.getModel().getElementAt(0).getDescription());
			for (int i = tx; i <= bx; i++)
				for (int j = ty; j <= by; j++)
					setTile(i, j, val);
		}
		
		public void paste() 
		{
			paste = true;
			deselect();
		}
	}
}
