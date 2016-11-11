package mapMaker;

import map.MapMetaData.MapDataType;
import mapMaker.data.MapMakerTriggerData;
import mapMaker.data.PlaceableTrigger;
import mapMaker.model.EditMode;
import mapMaker.model.EditMode.EditType;
import mapMaker.model.MapMakerModel;
import mapMaker.model.TileModel.TileType;
import mapMaker.model.TriggerModel.TriggerModelType;
import mapMaker.tools.MoveTool;
import mapMaker.tools.RectangleTool;
import mapMaker.tools.SelectTool;
import mapMaker.tools.SingleClickTool;
import mapMaker.tools.Tool;
import mapMaker.tools.ToolRenderer;
import mapMaker.tools.TriggerTool;
import util.FileIO;
import util.Folder;
import util.Point;
import util.StringUtils;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
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
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
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
import java.io.File;

public class MapMaker extends JPanel implements ActionListener, MouseListener, MouseMotionListener, KeyListener, ListSelectionListener {
	private static final long serialVersionUID = -1323397946555510794L;

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.getContentPane().add(new MapMaker());
		frame.setSize(800, 600);
		frame.setVisible(true);
	}

	public static final int tileSize = 32;

	private JButton newTileButton;
	private JList<ImageIcon> tileList;
	public JList<Tool> toolList;
	public Canvas canvas;
	private JMenuItem mntmNew, mntmLoad, mntmSetRoot;
	public JMenuItem mntmCut, mntmCopy, mntmPaste;
	private JLabel lblMapName;
	private JMenuItem mntmSave;
	private JLabel lblRoot;
	public JComboBox<EditType> editTypeComboBox;

    private EditMode editMode;
    private EditMapMetaData mapData;

    private PlaceableTrigger placeableTrigger;

	private File root;

	private Point location;
	private Point mouseHoverLocation;
		
	private SelectTool selectTool;
	public boolean triggerToolMoveSelected = false;

	private int previousToolListIndex = -1;
	private boolean controlKeyDown = false;

	public MapMaker() {
		this.root = null;
		this.location = new Point();
        this.mapData = new EditMapMetaData();
        this.editMode = new EditMode(this);

		this.setLayout(new BorderLayout());

		JPanel tilePanel = new JPanel();
		tilePanel.setBorder(new LineBorder(new Color(0, 0, 0), 4));
		add(tilePanel, BorderLayout.WEST);
		tilePanel.setLayout(new BorderLayout(0, 0));

		newTileButton = new JButton("New Tile");
		newTileButton.addActionListener(this);
		tilePanel.add(newTileButton, BorderLayout.NORTH);
		
		tileList = new JList<>();
		tileList.setModel(this.getModel().getListModel());
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
		lblRoot.setForeground(Color.RED);
		menuBar.add(lblRoot);

		Component horizontalStrut_2 = Box.createHorizontalStrut(50);
		menuBar.add(horizontalStrut_2);
		
		editTypeComboBox = new JComboBox<>();
		editTypeComboBox.addActionListener(event -> {
            editMode.setEditType((EditType) editTypeComboBox.getSelectedItem());

            MapMakerModel model = this.getModel();
            tileList.setModel(model.getListModel());
            newTileButton.setEnabled(model.newTileButtonEnabled());

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

    public BufferedImage getTileFromSet(TileType tileType, int index) {
        return this.editMode.getTileModel().getTile(tileType, index);
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
        this.editMode.getAreaModel().resetMap();
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
			this.mapData.loadPreviousMap(this, name, this.editMode.getAreaModel());
            this.resetMap();
		}
	}

	public void actionPerformed(ActionEvent event) {
		if (root != null) {
			if (event.getSource() == newTileButton) {
                this.getModel().newTileButtonPressed(this);
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

				lblMapName.setText(this.getCurrentMapName());
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
            // TODO: Move to FileIO
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

	public String getMapFolderName(final String mapName) {
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

        this.editMode.reload(this);
	}
	
	private void saveMap() {
		if (root == null) {
			return;
		}

        this.mapData.save(this);
        this.editMode.getTileModel().save(this);
	}

	public Point setTile(Point location, int val) {
		Point delta = this.mapData.checkNewDimension(location);

		Point start = Point.add(delta, location);
		boolean clearSelection = this.editMode.setTile(mapData, start, val);
        if (clearSelection) {
            tileList.clearSelection();;
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

		// TODO: What is 16 here? Should it be a constant? -- looks like this is half the tile size to draw the checker grey pattern
		for (int x = 0; x <= canvas.getWidth()/16; x++) {
			for (int y = 0; y <= canvas.getHeight()/16; y++) {
				g2d.setColor(((x^y) & 1) == 0 ? Color.GRAY : Color.LIGHT_GRAY);
				g2d.fillRect(x*16, y*16, 16, 16);
			}
		}

		this.mapData.drawMap(g2d, this.location, this.getEditType(), this.editMode.getTileModel());
		
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
		if (event.getKeyCode() == KeyEvent.VK_SPACE && previousToolListIndex != -1) {
			toolList.setSelectedIndex(previousToolListIndex);
			previousToolListIndex = -1; 
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
        return this.editMode.getEditType();
    }

    public boolean isEditType(EditType editType) {
        return this.getEditType() == editType;
    }

    public BufferedImage getCurrentMapImage(MapDataType dataType) {
        return this.mapData.getMapImage(dataType);
    }

    public MapMakerModel getModel() {
        return this.editMode.getModel();
    }

    public PlaceableTrigger getPlaceableTrigger() {
        return this.placeableTrigger;
    }

    public boolean hasPlaceableTrigger() {
        return this.placeableTrigger != null;
    }

    public void setPlaceableTrigger(PlaceableTrigger trigger) {
        this.placeableTrigger = trigger;
    }

    public void clearPlaceableTrigger() {
        this.placeableTrigger = null;
    }

    public boolean isPlaceableTriggerType(PlaceableTrigger.TriggerType triggerType) {
        return this.placeableTrigger != null && this.placeableTrigger.triggerType == triggerType;
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
							if(type == TriggerModelType.WILD_BATTLE) { //If wild battle trigger, rectangle tool.
								toolList.setSelectedIndex(2); // TODO: ENUM
							}
							else {
								toolList.setSelectedIndex(1);								
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
}
