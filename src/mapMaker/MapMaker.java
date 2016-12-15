package mapMaker;

import main.Global;
import map.MapDataType;
import mapMaker.model.MapMakerModel;
import mapMaker.model.TileModel.TileType;
import mapMaker.model.TriggerModel.TriggerModelType;
import mapMaker.tools.SelectTool;
import mapMaker.tools.Tool;
import mapMaker.tools.Tool.ToolType;
import mapMaker.tools.ToolRenderer;
import pattern.generic.LocationTriggerMatcher;
import util.DrawUtils;
import util.FileIO;
import util.Folder;
import util.FontMetrics;
import util.GUIUtils;
import util.Point;
import util.StringUtils;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
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
import java.io.File;

public class MapMaker extends JPanel implements ActionListener, MouseListener, MouseMotionListener, KeyListener, ListSelectionListener {
	private static final long serialVersionUID = -1323397946555510794L;

	public static void main(String[] args) {
		MapMaker mapMaker = new MapMaker();

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.getContentPane().add(mapMaker);
		frame.setSize(Global.GAME_SIZE);
		frame.setVisible(true);
	}

	private JButton newTileButton;
	private JList<ImageIcon> tileList;
	private JList<Tool> toolList;
	public Canvas canvas;
	private JMenuItem newMenuItem, loadMenuItem, setRootMenuItem;
	public JMenuItem cutMenuItem, copyMenuItem, pasteMenuItem;
	private JLabel mapNameLabel;
	private JMenuItem saveMenuItem;
	private JLabel rootLabel;
	private JComboBox<EditType> editTypeComboBox;

    private EditType editType;
    private EditMapMetaData mapData;

    private LocationTriggerMatcher placeableTrigger;

	private File root;

	private Point location;
	private Point mouseHoverLocation;
		
	private SelectTool selectTool;
    private ToolType previousToolType;
	public boolean triggerToolMoveSelected;

	private boolean controlKeyDown;

	public MapMaker() {
		this.root = null;

        this.location = new Point();
        this.mouseHoverLocation = new Point();

        this.mapData = new EditMapMetaData();
		this.editType = EditType.BACKGROUND;

        this.canvas = new Canvas();
        this.canvas.addMouseListener(this);
        this.canvas.addMouseMotionListener(this);
        this.canvas.addKeyListener(this);

        this.setLayout(new BorderLayout());
        this.add(this.createTilePanel(), BorderLayout.WEST);
        this.add(this.createMenuBar(), BorderLayout.NORTH);
		this.add(this.createToolList(), BorderLayout.EAST);
        this.add(this.canvas, BorderLayout.CENTER);

		this.setRoot(new File("."));
	}

	private JScrollPane createToolList() {
        toolList = new JList<>();
        toolList.setModel(Tool.getToolListModel(this));
        toolList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        toolList.setCellRenderer(new ToolRenderer());
        toolList.setFont(FontMetrics.getFont(18));
        toolList.addListSelectionListener(this);

        this.setTool(ToolType.MOVE);
        this.selectTool = (SelectTool)this.getTool(ToolType.SELECT);

        return new JScrollPane(toolList);
    }

	private JMenuItem createEditMenuItem(String text, int keyEvent) {
		JMenuItem menuItem = GUIUtils.createMenuItem(text, keyEvent, this);
		menuItem.setEnabled(false);

		return menuItem;
	}

    private JMenu createFileMenu() {
        this.newMenuItem = GUIUtils.createMenuItem("New", KeyEvent.VK_N, this);
        this.saveMenuItem = GUIUtils.createMenuItem("Save", KeyEvent.VK_S, this);
        this.loadMenuItem = GUIUtils.createMenuItem("Load", KeyEvent.VK_L, this);
        this.setRootMenuItem = GUIUtils.createMenuItem("Set Root", this);

        return GUIUtils.createMenu("File", newMenuItem, saveMenuItem, loadMenuItem, setRootMenuItem);
    }

    private JMenu createEditMenu() {
        this.cutMenuItem = this.createEditMenuItem("Cut", KeyEvent.VK_X);
        this.copyMenuItem = this.createEditMenuItem("Copy", KeyEvent.VK_C);
        this.pasteMenuItem = this.createEditMenuItem("Paste", KeyEvent.VK_V);

        return GUIUtils.createMenu("Edit", cutMenuItem, copyMenuItem, pasteMenuItem);
    }

	private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
		GUIUtils.setStyle(menuBar);

        mapNameLabel = GUIUtils.createLabel("MapMaker");

        rootLabel = GUIUtils.createLabel("Root Location:");
        rootLabel.setForeground(Color.RED);

        menuBar.add(this.createFileMenu());
        menuBar.add(this.createEditMenu());
        menuBar.add(Box.createHorizontalStrut(53));
        menuBar.add(mapNameLabel);
        menuBar.add(Box.createHorizontalStrut(53));
        menuBar.add(rootLabel);
        menuBar.add(Box.createHorizontalStrut(50));
        menuBar.add(this.createEditTypeComboBox());

        return menuBar;
    }

    private JComboBox<EditType> createEditTypeComboBox() {
		editTypeComboBox = GUIUtils.createComboBox(
				EditType.values(),
				event -> {
					this.editType = (EditType) editTypeComboBox.getSelectedItem();

					MapMakerModel model = this.getModel();
					tileList.setModel(model.getListModel());
					newTileButton.setEnabled(model.newTileButtonEnabled());

					if (pasteMenuItem != null && selectTool != null) {
						pasteMenuItem.setEnabled(selectTool.canPaste());
					}

					draw();
				}
		);

		editTypeComboBox.setLightWeightPopupEnabled(false);
        this.setEditType(EditType.BACKGROUND);

        return editTypeComboBox;
    }

	private JPanel createTilePanel() {
        JPanel tilePanel = new JPanel();
        tilePanel.setBorder(new LineBorder(Color.BLACK));
        tilePanel.setLayout(new BorderLayout());

        newTileButton = GUIUtils.createButton("New Tile", this);
        tilePanel.add(newTileButton, BorderLayout.NORTH);

        tileList = new JList<>();
        tileList.setModel(this.getModel().getListModel());
        tileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tileList.addListSelectionListener(this);
        tileList.addKeyListener(this);

        JScrollPane listScroller = new JScrollPane(tileList);
        tilePanel.add(listScroller, BorderLayout.CENTER);

        return tilePanel;
    }

	public void setEditType(EditType editType) {
        this.editTypeComboBox.setSelectedIndex(editType.ordinal());
    }

	public void setTool(ToolType toolType) {
        this.toolList.setSelectedIndex(toolType.ordinal());
    }

	private Tool getTool(ToolType toolType) {
        return toolList.getModel().getElementAt(toolType.ordinal());
    }

	public Point getMapLocation() {
        return this.location;
	}

	public void offSetLocation(Point delta) {
		this.location = Point.add(this.location, delta);
	}

	public Point getMouseHoverLocation() {
		return this.mouseHoverLocation;
	}

    public BufferedImage getTileFromSet(TileType tileType, int index) {
		return MapMakerModel.getTileModel().getTile(tileType, index);
    }

	// Called when trying to exit, shows a confirm dialog asking to save first if there are any unsaved changes
	// Returns whether or not the exit will actually occur
	private boolean checkSaveOnExit() {
		if (!this.mapData.isSaved()) {
			int val = JOptionPane.showConfirmDialog(this, "Save current file first?", "Unsaved changes", JOptionPane.YES_NO_CANCEL_OPTION);
			if (val == JOptionPane.YES_OPTION) {
				saveMap();
			}

			return val != JOptionPane.CANCEL_OPTION;
		}

		// Nothing to save, just exit
		return true;
	}

	private void resetMap() {
        this.location = new Point();
		MapMakerModel.getAreaModel().resetMap();
    }

	// TODO: Should this be checking if that name is already taken?
	private void createNewMapDialog() {
		String name = JOptionPane.showInputDialog(this, "Name the map");
		if (!StringUtils.isNullOrEmpty(name)) {
            this.mapData.createNewMap(this, name);
            this.resetMap();
		}
	}

	private void loadPreviousMapDialog() {
		String[] mapList = getAvailableMaps();
		String name = (String)JOptionPane.showInputDialog(this, "Select a map", "Load", JOptionPane.PLAIN_MESSAGE, null, mapList, mapList[0]);
        if (!StringUtils.isNullOrEmpty(name)) {
			this.mapData.loadPreviousMap(this, name);
            this.resetMap();
		}
	}

	public void actionPerformed(ActionEvent event) {
		if (root != null) {
			if (event.getSource() == newTileButton) {
                this.getModel().newTileButtonPressed(this);
			}
			else if (event.getSource() == saveMenuItem) {
				saveMap();
			}
			else if (event.getSource() == newMenuItem || event.getSource() == loadMenuItem) {
				boolean exit = checkSaveOnExit();
				if (!exit) {
					return;
				}

				this.setTool(ToolType.MOVE);
				
				if (event.getSource() == newMenuItem) {
					createNewMapDialog();
				}
				else {
					loadPreviousMapDialog();
				}

				mapNameLabel.setText(this.getCurrentMapName());
				draw();
			}
			else if (event.getSource() == cutMenuItem) {
				selectTool.cut();
			}
			else if (event.getSource() == copyMenuItem) {
				selectTool.copy();
			}
			else if (event.getSource() == pasteMenuItem) {
                this.setTool(ToolType.SELECT);
				selectTool.paste();
			}
		}
		else if (event.getSource() == setRootMenuItem) {
            JFileChooser directoryChooser = FileIO.getDirectoryChooser();
		
			int response = directoryChooser.showOpenDialog(this);
			if (response == JFileChooser.APPROVE_OPTION) {
				this.setRoot(directoryChooser.getSelectedFile());
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

	public String getMapFolderPath(final String mapName) {
		return getPathWithRoot(FileIO.makeFolderPath(Folder.MAPS, mapName));
	}

	public String getMapTextFileName(final String mapName) {
		return getMapFolderPath(mapName) + mapName + ".txt";
	}

	// TODO: Srsly what is going on with setting the root what the fuck
	private void setRoot(File newRoot) {
		System.out.println("root set to: " + newRoot);

		root = newRoot;
		rootLabel.setText(root.getPath());
		rootLabel.setForeground(Color.BLACK);

		FileIO.createFolder(getPathWithRoot(Folder.REC));
		FileIO.createFolder(getPathWithRoot(Folder.TILES));
		FileIO.createFolder(getPathWithRoot(Folder.MAP_TILES));
		FileIO.createFolder(getPathWithRoot(Folder.MAPS));

        MapMakerModel.reloadModels(this);
	}
	
	private void saveMap() {
		if (root == null) {
			return;
		}

        this.mapData.save(this);
        MapMakerModel.getTileModel().save(this);
	}

	public Point setTile(Point location, int val) {
		Point delta = this.mapData.checkNewDimension(location);

		Point start = Point.add(delta, location);
		boolean clearSelection = this.mapData.setTile(editType, start, val);
        if (clearSelection) {
            tileList.clearSelection();
        }

		return delta;
	}
	
	public int getTile(Point location, MapDataType dataType) {
        return this.mapData.getTile(location, dataType);
	}
	
	public void draw() {
		if (!this.hasMap()) {
			return;
		}
		
		BufferedImage buffer = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = (Graphics2D)buffer.getGraphics();

		for (int x = 0; x < canvas.getWidth(); x += Global.TILE_SIZE) {
			for (int y = 0; y < canvas.getHeight(); y += Global.TILE_SIZE) {
				DrawUtils.fillBlankTile(g2d, new Point(x, y));
			}
		}

		this.mapData.drawMap(g2d, this.location, this.getEditType());
		
		if (!toolList.isSelectionEmpty()) {
			toolList.getSelectedValue().draw(g2d);
		}

		this.getModel().draw(g2d, this);
		
		g2d.dispose();
		
		Graphics g = canvas.getGraphics();
		g.drawImage(buffer, 0, 0, null);
		g.dispose();
	}

	private static Point getMouseLocation(MouseEvent event) {
        return new Point(event.getX(), event.getY());
    }

	public void mouseClicked(MouseEvent event) {
		if (!toolList.isSelectionEmpty()) {
			toolList.getSelectedValue().click(getMouseLocation(event));
		}
	
		draw();
	}

	public void mouseEntered(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}

	public void mousePressed(MouseEvent event) {
		if (!toolList.isSelectionEmpty()) {
			toolList.getSelectedValue().pressed(getMouseLocation(event));
		}
	
		draw();
	}

	// TODO: Do we not want to be saving the mouse hover location here? as well as above?
	public void mouseReleased(MouseEvent event) {
		if (!toolList.isSelectionEmpty()) {
			toolList.getSelectedValue().released(getMouseLocation(event));
		}
	
		draw();
	}

	public void mouseDragged(MouseEvent event) {
        this.mouseHoverLocation = getMouseLocation(event);

		if (!toolList.isSelectionEmpty()) {
			toolList.getSelectedValue().drag(getMouseLocation(event));
		}
	
		draw();
	}

	public void mouseMoved(MouseEvent event) {
        this.mouseHoverLocation = getMouseLocation(event);
	
		draw();
	}
	
	public void keyTyped(KeyEvent event) {}

	public void keyPressed(KeyEvent event) {
		// TODO: This should be stored in the tool
		// TODO: e for eraser, s for single, r for rect, t for trigger, ? for select?
		if (event.getKeyCode() == KeyEvent.VK_SPACE && previousToolType == null && !toolList.isSelectionEmpty()) {
            previousToolType = ToolType.values()[toolList.getSelectedIndex()];
            this.setTool(ToolType.MOVE);
		}
		else if (event.getKeyCode() == KeyEvent.VK_1) {
            this.setTool(ToolType.MOVE);
		}
		else if (event.getKeyCode() == KeyEvent.VK_2) {
            this.setTool(ToolType.SINGLE_CLICK);
		}
		else if (event.getKeyCode() == KeyEvent.VK_3) {
			this.setTool(ToolType.RECTANGLE);
		}
		else if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
			if (this.isEditType(EditType.TRIGGERS) && this.hasPlaceableTrigger()) {
				this.clearPlaceableTrigger();
				toolList.clearSelection();
			}
		}

		if (event.getModifiers() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) {
			controlKeyDown = true;
		}
	}

	public void keyReleased(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.VK_SPACE && previousToolType != null) {
            this.setTool(previousToolType);
			this.previousToolType = null;
		}
		
		if (event.getModifiers() != Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) {
			controlKeyDown = false;
		}
	}

    public boolean hasMap() {
        return this.mapData.hasMap();
    }

    public String getCurrentMapName() {
        return this.mapData.getMapName();
    }

	public Dimension getCurrentMapSize() {
        return this.mapData.getDimension();
    }

    public MapMakerTriggerData getTriggerData() {
        return this.mapData.getTriggerData();
    }

    public boolean isTileSelectionEmpty() {
        return this.tileList.isSelectionEmpty();
    }

	public int getSelectedTile() {
		return Integer.parseInt(this.tileList.getSelectedValue().getDescription());
    }

    public int getSelectedTileIndex() {
        return this.tileList.getSelectedIndex();
    }

    public void setSelectedTileIndex(int index) {
        this.tileList.setSelectedIndex(index);
    }

    public EditType getEditType() {
        return this.editType;
    }

    public boolean isEditType(EditType editType) {
        return this.getEditType() == editType;
    }

    public BufferedImage getCurrentMapImage(MapDataType dataType) {
        return this.mapData.getMapImage(dataType);
    }

    public MapMakerModel getModel() {
        return this.editType.getModel();
    }

    public LocationTriggerMatcher getPlaceableTrigger() {
        return this.placeableTrigger;
    }

    public boolean hasPlaceableTrigger() {
        return this.placeableTrigger != null;
    }

    public void setPlaceableTrigger(LocationTriggerMatcher trigger) {
        this.placeableTrigger = trigger;
    }

    public void clearPlaceableTrigger() {
        this.placeableTrigger = null;
    }

	public void valueChanged(ListSelectionEvent event) {
		if (event.getSource() == tileList) {

			// When a trigger item selected
			if (this.isEditType(EditType.TRIGGERS) && !this.isTileSelectionEmpty() && !event.getValueIsAdjusting()) {
				if (!this.hasMap()) {
					tileList.clearSelection();
				}
				else {
					TriggerModelType type = TriggerModelType.getModelTypeFromIndex(this.getSelectedTileIndex());

					// Already something placeable, ignore trying to create something new.
					if (!this.hasPlaceableTrigger()) {

						// Trigger was not created, deselect item
						if (!this.getTriggerData().createTrigger(type)) {
							tileList.clearSelection();
						}
						// Trigger was created, move to single selection
						else {
							if (type == TriggerModelType.WILD_BATTLE || type == TriggerModelType.EVENT) {
								this.setTool(ToolType.RECTANGLE);
							}
							else {
                                this.setTool(ToolType.SINGLE_CLICK);
							}
						}
					}
					else if (!triggerToolMoveSelected) {
						this.clearPlaceableTrigger();
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
					copyMenuItem.setEnabled(false);
					cutMenuItem.setEnabled(false);
				}
				else if (selectTool.hasSelection()) {
					copyMenuItem.setEnabled(true);
					cutMenuItem.setEnabled(true);
				}
			}
		}
	}
}
