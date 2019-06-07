package mapMaker.model;

import draw.DrawUtils;
import draw.TileUtils;
import map.MapDataType;
import mapMaker.MapMaker;
import mapMaker.MapMakerTriggerData;
import mapMaker.dialogs.AreaDialog;
import pattern.map.AreaMatcher;
import util.FontMetrics;
import util.Point;
import util.string.StringUtils;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AreaModel extends MapMakerModel {
    private static final int VOID_INDEX = 0;
    private static final Color DEFAULT_AREA_COLOR = Color.GREEN;

    private final DefaultListModel<ImageIcon> areaListModel;
    private final Map<Integer, String> areaIndexMap;

    AreaModel() {
        super(VOID_INDEX);

        this.areaListModel = new DefaultListModel<>();
        this.areaIndexMap = new HashMap<>();
    }

    private void resetMap() {
        areaIndexMap.clear();
        areaListModel.clear();

        this.addArea(Color.BLACK, "Void");
    }

    public void loadMap(MapMakerTriggerData triggerData) {
        this.resetMap();

        if (triggerData != null) {
            Set<AreaMatcher> areaData = triggerData.getAreaData();
            for (AreaMatcher area : areaData) {
                Color areaColor = area.hasColor() ? area.getColor() : DEFAULT_AREA_COLOR;
                String areaName = StringUtils.isNullOrEmpty(area.getDisplayName()) ? "Nameless" : area.getDisplayName();
                this.addArea(areaColor, areaName);
            }
        }
    }

    private void addArea(Color color, String name) {
        int rgb = color.getRGB();
        areaIndexMap.put(rgb, name);
        areaListModel.addElement(new ImageIcon(TileUtils.colorWithText(name, color), rgb + ""));
    }

    @Override
    public void reload(MapMaker mapMaker) {
        this.loadMap(mapMaker.getTriggerData());
    }

    @Override
    public DefaultListModel<ImageIcon> getListModel() {
        return this.areaListModel;
    }

    @Override
    public boolean newTileButtonEnabled() {
        return true;
    }

    @Override
    public void newTileButtonPressed(MapMaker mapMaker) {
        // Get a color from the user for the new area.
        Color color = JColorChooser.showDialog(mapMaker, "Choose a preferred color for the new area", Color.WHITE);
        if (color != null) {
            color = DrawUtils.permuteColor(color, areaIndexMap);
            AreaMatcher newArea = new AreaDialog(null).getMatcher(mapMaker);
            newArea.setColor(color);

            mapMaker.getTriggerData().addArea(newArea);

            // Add area to the list.
            areaListModel.addElement(new ImageIcon(TileUtils.colorWithText(newArea.getDisplayName(), color), color.getRGB() + ""));
            areaIndexMap.put(color.getRGB(), newArea.getDisplayName());

            AreaMatcher defaultArea = mapMaker.getTriggerData().getDefaultArea();
            if (!defaultArea.hasColor()) {
                defaultArea.setColor(DEFAULT_AREA_COLOR);
            }
        }
    }

    // Draw area label on mouse hover
    @Override
    public void draw(Graphics2D g2d, MapMaker mapMaker) {
        Point mouseHoverLocation = mapMaker.getMouseHoverLocation();
        Point location = TileUtils.getLocation(mouseHoverLocation, mapMaker.getMapLocation());
        int tileColor = mapMaker.getTile(location, MapDataType.AREA);

        String areaName = areaIndexMap.get(tileColor);
        if (areaName == null) {
            areaName = areaIndexMap.get(Color.BLACK.getRGB());
        }

        g2d.setColor(Color.BLACK);
        FontMetrics.setFont(g2d, 16);
        g2d.drawString(areaName, mouseHoverLocation.x, mouseHoverLocation.y);
    }
}
