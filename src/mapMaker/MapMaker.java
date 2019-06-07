package mapMaker;

import draw.TileUtils;
import main.Global;
import map.MapDataType;
import map.MapName;
import mapMaker.model.MapMakerModel;
import mapMaker.model.TileModel;
import mapMaker.model.TileModel.TileType;
import mapMaker.model.TriggerModel.TriggerModelType;
import mapMaker.tools.SelectTool;
import mapMaker.tools.Tool;
import mapMaker.tools.Tool.ToolType;
import mapMaker.tools.ToolRenderer;
import pattern.generic.LocationTriggerMatcher;
import util.FontMetrics;
import util.GuiUtils;
import util.Point;
import util.file.FileIO;
import util.file.Folder;
import util.string.StringUtils;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MapMaker extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
    public final Canvas canvas;

    private JButton newTileButton;
    private JComboBox<TileCategory> tileCategoriesComboBox;
    private JList<ImageIcon> tileList;
    private JList<Tool> toolList;

    public JMenuItem cutMenuItem;
    public JMenuItem copyMenuItem;
    public JMenuItem pasteMenuItem;

    private JLabel mapNameLabel;
    private JComboBox<EditType> editTypeComboBox;

    private EditType editType;
    private EditMapMetadata mapData;

    private LocationTriggerMatcher placeableTrigger;

    private Point location;
    private Point mouseHoverLocation;

    private SelectTool selectTool;
    private ToolType previousToolType;
    public boolean triggerToolMoveSelected;

    public static void main(String[] args) {
        MapMaker mapMaker = new MapMaker();

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(mapMaker);
        frame.setSize(Global.GAME_SIZE);
        frame.setVisible(true);
    }

    private MapMaker() {
        this.location = new Point();
        this.mouseHoverLocation = new Point();

        this.mapData = new EditMapMetadata();
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

        MapMakerModel.reloadModels(this);

        this.mapData.loadPreviousMap(this, new MapName("Depth First Search Town", "PlayersHouseUp"));
    }

    private JScrollPane createToolList() {
        toolList = new JList<>();
        toolList.setModel(Tool.getToolListModel(this));
        toolList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        toolList.setCellRenderer(new ToolRenderer());
        toolList.setFont(FontMetrics.getFont(18));

        toolList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                this.getTool().reset();
                if (this.getTool() != selectTool) {
                    copyMenuItem.setEnabled(false);
                    cutMenuItem.setEnabled(false);
                } else if (selectTool.hasSelection()) {
                    copyMenuItem.setEnabled(true);
                    cutMenuItem.setEnabled(true);
                }
            }
        });

        this.setTool(ToolType.MOVE);
        this.selectTool = (SelectTool)this.getTool(ToolType.SELECT);

        return new JScrollPane(toolList);
    }

    private JMenu createFileMenu() {
        JMenuItem newMenuItem = GuiUtils.createMenuItem("New", KeyEvent.VK_N, event -> showMap(this::createMapDialog));
        JMenuItem loadMenuItem = GuiUtils.createMenuItem("Load", KeyEvent.VK_L, event -> showMap(this::loadMapDialog));
        JMenuItem saveMenuItem = GuiUtils.createMenuItem("Save", KeyEvent.VK_S, event -> saveMap());

        return GuiUtils.createMenu("File", newMenuItem, loadMenuItem, saveMenuItem);
    }

    private JMenu createEditMenu() {
        // Cut and copy are only enabled when select tool is active
        this.cutMenuItem = GuiUtils.createMenuItem("Cut", KeyEvent.VK_X, event -> selectTool.cut());
        this.cutMenuItem.setEnabled(false);

        this.copyMenuItem = GuiUtils.createMenuItem("Copy", KeyEvent.VK_C, event -> selectTool.copy());
        this.copyMenuItem.setEnabled(false);

        // Paste is enabled once something is cut/copied
        this.pasteMenuItem = GuiUtils.createMenuItem("Paste", KeyEvent.VK_V, event -> {
            this.setTool(ToolType.SELECT);
            selectTool.paste();
        });
        this.pasteMenuItem.setEnabled(false);

        JMenuItem undoMenuItem = GuiUtils.createMenuItem("Undo", KeyEvent.VK_Z, event -> {
            Tool.undoLastTool();
            draw();
        });

        return GuiUtils.createMenu("Edit", cutMenuItem, copyMenuItem, pasteMenuItem, undoMenuItem);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        GuiUtils.setStyle(menuBar);

        mapNameLabel = GuiUtils.createLabel("Map Maker");

        menuBar.add(this.createFileMenu());
        menuBar.add(this.createEditMenu());
        menuBar.add(Box.createHorizontalStrut(53));
        menuBar.add(mapNameLabel);
        menuBar.add(Box.createHorizontalStrut(53));
        menuBar.add(this.createEditTypeComboBox());

        return menuBar;
    }

    private JComboBox<EditType> createEditTypeComboBox() {
        editTypeComboBox = GuiUtils.createComboBox(
                EditType.values(),
                event -> {
                    this.editType = (EditType)editTypeComboBox.getSelectedItem();

                    MapMakerModel model = this.getModel();
                    tileList.setModel(model.getListModel());
                    newTileButton.setEnabled(model.newTileButtonEnabled());
                    tileCategoriesComboBox.setEnabled(model instanceof TileModel);

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

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        newTileButton = GuiUtils.createButton("New Tile", event -> this.getModel().newTileButtonPressed(this));
        panel.add(newTileButton, BorderLayout.NORTH);

        tileCategoriesComboBox = GuiUtils.createComboBox(
                TileCategory.values(),
                event -> {
                    if (!tileCategoriesComboBox.isEnabled()) {
                        return;
                    }
                    TileCategory tileCategory = (TileCategory)tileCategoriesComboBox.getSelectedItem();
                    TileModel tileModel = (TileModel)this.getModel();
                    tileModel.setSelectedTileCategory(tileCategory);
                    tileList.setModel(tileModel.getListModel());
                    draw();
                }
        );
        panel.add(tileCategoriesComboBox, BorderLayout.SOUTH);
        tilePanel.add(panel, BorderLayout.NORTH);

        tileList = new JList<>();
        tileList.setModel(this.getModel().getListModel());
        tileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tileList.addKeyListener(this);
        tileList.addListSelectionListener(event -> {
            // When a trigger item selected
            if (this.isEditType(EditType.TRIGGERS) && this.hasSelectedTile() && !event.getValueIsAdjusting()) {
                if (!this.hasMap()) {
                    tileList.clearSelection();
                } else {
                    TriggerModelType type = TriggerModelType.getModelTypeFromIndex(this.getSelectedTileIndex());

                    // Already something placeable, ignore trying to create something new
                    if (!this.hasPlaceableTrigger()) {

                        // Trigger was not created, deselect item
                        if (!this.getTriggerData().createTrigger(type)) {
                            tileList.clearSelection();
                        } else {
                            // Trigger was created, set appropriate tool
                            this.setTool(type.getDefaultTool());
                        }
                    } else if (!triggerToolMoveSelected) {
                        this.clearPlaceableTrigger();
                        tileList.clearSelection();
                    }

                    triggerToolMoveSelected = false;
                }
            }
        });

        JScrollPane listScroller = new JScrollPane(tileList);
        tilePanel.add(listScroller, BorderLayout.CENTER);

        return tilePanel;
    }

    public Tool getTool() {
        return toolList.getSelectedValue();
    }

    private Tool getTool(ToolType toolType) {
        return toolList.getModel().getElementAt(toolType.ordinal());
    }

    public void setTool(ToolType toolType) {
        this.toolList.setSelectedIndex(toolType.ordinal());
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
        // No changes to save -- just exit
        if (!this.hasUnsavedChanges()) {
            return true;
        }

        int val = JOptionPane.showConfirmDialog(
                this,
                "Save current file first?",
                "Unsaved changes",
                JOptionPane.YES_NO_CANCEL_OPTION
        );

        if (val == JOptionPane.YES_OPTION) {
            saveMap();
        }

        return val != JOptionPane.CANCEL_OPTION;
    }

    // Opens an input dialog and with the selection choices and returns the user's selection
    private String getInputDialogChoice(String message, String title, String[] selectionValues) {
        return (String)JOptionPane.showInputDialog(
                this,
                message,
                title,
                JOptionPane.PLAIN_MESSAGE,
                null,
                selectionValues,
                selectionValues[0]
        );
    }

    // Used when switching maps (creating or loading) -- dialog will determine which map to pick
    // showDialog should return false if a map was not selected
    // if true, all relevant map information should already be stored
    private void showMap(Supplier<Boolean> showDialog) {
        if (this.mapData.hasMap()) {
            boolean exit = checkSaveOnExit();
            if (!exit) {
                return;
            }
        }

        // Default to move for new maps (so you can find the best place!)
        this.setTool(ToolType.MOVE);

        if (!showDialog.get()) {
            return;
        }

        mapNameLabel.setText(this.getCurrentMapName().toString());
        draw();
    }

    private boolean createMapDialog() {
        String[] regionList = getAvailableRegions();
        String region = this.getInputDialogChoice("Select a region", "Create Map", regionList);
        if (StringUtils.isNullOrEmpty(region)) {
            return false;
        }

        Set<String> availableMaps = Arrays.stream(getAvailableMaps(region)).map(MapName::getMapName).collect(Collectors.toSet());

        // Name the map (ask until empty or unique)
        String mapName;
        while (true) {
            mapName = JOptionPane.showInputDialog(this, "Name the map");
            if (StringUtils.isNullOrEmpty(mapName)) {
                return false;
            }

            if (availableMaps.contains(mapName)) {
                Global.info("Map with the name " + mapName + " already exists. Please choose another name.");
            } else {
                break;
            }
        }

        this.mapData.createNewMap(this, new MapName(region, mapName));
        return true;
    }

    private boolean loadMapDialog() {
        String[] regionList = getAvailableRegions();
        String region = this.getInputDialogChoice("Select a region", "Load Map", regionList);
        if (StringUtils.isNullOrEmpty(region)) {
            return false;
        }

        String[] availableMaps = Arrays.stream(getAvailableMaps(region)).map(MapName::getMapName).toArray(String[]::new);
        String map = this.getInputDialogChoice("Select a map", "Load Map", availableMaps);
        if (StringUtils.isNullOrEmpty(map)) {
            return false;
        }

        this.mapData.loadPreviousMap(this, new MapName(region, map));
        return true;
    }

    public String[] getAvailableRegions() {
        File mapsFolder = FileIO.newFile(getPath(Folder.MAPS));
        return FileIO.listDirectories(mapsFolder)
                     .stream()
                     .map(File::getName)
                     .toArray(String[]::new);
    }

    public MapName[] getAvailableMaps(String region) {
        File regionFolder = FileIO.newFile(FileIO.makeFolderPath(Folder.MAPS, region));
        return FileIO.listDirectories(regionFolder)
                     .stream()
                     .map(file -> new MapName(region, file.getName()))
                     .toArray(MapName[]::new);
    }

    public String getPath(final String path) {
        return path;
    }

    public String getMapFolderPath(final MapName mapName) {
        return getPath(FileIO.makeFolderPath(Folder.MAPS, mapName.getRegionName(), mapName.getMapName()));
    }

    public String getMapTextFileName(final MapName mapName) {
        return getMapFolderPath(mapName) + mapName.getMapName() + ".txt";
    }

    private boolean hasUnsavedChanges() {
        return this.mapData.hasUnsavedChanges(this) || MapMakerModel.getTileModel().hasUnsavedChanges();
    }

    private void saveMap() {
        this.mapData.save(this);
        MapMakerModel.getTileModel().save(this);
    }

    public Point setTile(Point location, int val) {
        return setTile(location, val, editType);
    }

    public Point setTile(Point location, int val, EditType editType) {
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

    private void draw() {
        if (!this.hasMap()) {
            return;
        }

        BufferedImage buffer = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D)buffer.getGraphics();

        for (int x = 0; x < canvas.getWidth(); x += Global.TILE_SIZE) {
            for (int y = 0; y < canvas.getHeight(); y += Global.TILE_SIZE) {
                TileUtils.fillBlankTile(g2d, new Point(x, y));
            }
        }

        this.mapData.drawMap(g2d, this.location, this.getEditType());

        this.getTool().draw(g2d);
        this.getModel().draw(g2d, this);

        g2d.dispose();

        Graphics g = canvas.getGraphics();
        g.drawImage(buffer, 0, 0, null);
        g.dispose();
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        this.getTool().click(getMouseLocation(event));
        draw();
    }

    @Override
    public void mouseEntered(MouseEvent event) {}

    @Override
    public void mouseExited(MouseEvent event) {}

    @Override
    public void mousePressed(MouseEvent event) {
        this.getTool().pressed(getMouseLocation(event));
        draw();
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        this.getTool().released(getMouseLocation(event));
        draw();
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        this.mouseHoverLocation = getMouseLocation(event);

        this.getTool().drag(getMouseLocation(event));
        draw();
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        this.mouseHoverLocation = getMouseLocation(event);

        draw();
    }

    @Override
    public void keyTyped(KeyEvent event) {}

    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_SPACE && previousToolType == null) {
            previousToolType = this.getTool().getToolType();
            this.setTool(ToolType.MOVE);
        } else if (event.getKeyCode() == KeyEvent.VK_ESCAPE && this.isEditType(EditType.TRIGGERS) && this.hasPlaceableTrigger()) {
            this.clearPlaceableTrigger();
            this.setTool(ToolType.MOVE);
        } else {
            // Check if corresponds to a tool
            for (ToolType toolType : ToolType.values()) {
                if (event.getKeyCode() == toolType.getKeyEvent()) {
                    this.setTool(toolType);
                    break;
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_SPACE && previousToolType != null) {
            this.setTool(previousToolType);
            this.previousToolType = null;
        }
    }

    public boolean hasMap() {
        return this.mapData.hasMap();
    }

    public MapName getCurrentMapName() {
        return this.mapData.getMapName();
    }

    public Dimension getCurrentMapSize() {
        return this.mapData.getDimension();
    }

    public MapMakerTriggerData getTriggerData() {
        return this.mapData.getTriggerData();
    }

    public boolean hasSelectedTile() {
        return !this.tileList.isSelectionEmpty();
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

    public void setEditType(EditType editType) {
        this.editTypeComboBox.setSelectedIndex(editType.ordinal());
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

    public void setPlaceableTrigger(LocationTriggerMatcher trigger) {
        this.placeableTrigger = trigger;
    }

    private boolean hasPlaceableTrigger() {
        return this.placeableTrigger != null;
    }

    public void clearPlaceableTrigger() {
        this.placeableTrigger = null;
    }

    private static Point getMouseLocation(MouseEvent event) {
        return new Point(event.getX(), event.getY());
    }
}
