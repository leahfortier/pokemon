package mapMaker.model;

import map.AreaData.WeatherState;
import map.MapMetaData.MapDataType;
import map.TerrainType;
import mapMaker.MapMaker;
import util.DrawUtils;
import util.Point;
import util.StringUtils;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class AreaModel extends MapMakerModel {
    private static final int VOID_INDEX = 0;

    private final DefaultListModel<ImageIcon> areaListModel;
    private final Map<Integer, String> areaIndexMap;
    private final Set<Integer> areasOnMap;

    AreaModel() {
        super(VOID_INDEX);

        this.areaListModel = new DefaultListModel<>();
        this.areaIndexMap = new HashMap<>();
        this.areasOnMap = new HashSet<>();
    }

    public void resetMap() {
        areaListModel.clear();
        areasOnMap.clear();
        this.addArea(VOID_INDEX, "Void");
    }

    public void updateExistingAreas(int rgb) {
        if (!areasOnMap.contains(rgb) && areaIndexMap.containsKey(rgb)) {
            this.addArea(rgb, areaIndexMap.get(rgb));
        }
    }

    public void addArea(int rgb, String name) {
        areaIndexMap.put(rgb, name);
        areasOnMap.add(rgb);
        areaListModel.addElement(new ImageIcon(DrawUtils.colorWithText(name, new Color(rgb, true)), rgb + ""));
    }

    // TODO: Redo all of this
    @Override
    public void reload(MapMaker mapMaker) {
//        File areaIndexFile = new File(mapMaker.getPathWithRoot(FileName.MAP_AREA_INDEX));
        areaIndexMap.clear();
        areasOnMap.clear();
        areaListModel.clear();

//        if (areaIndexFile.exists()) {
//            String fileText = FileIO.readEntireFileWithReplacements(areaIndexFile, false);
//
//            Matcher m = mapAreaPattern.matcher(fileText);
//            while (m.find()) {
//                String name = m.group(1);
//                int val = (int) Long.parseLong(m.group(2), 16);
//
//                this.addArea(val, name);
//            }
//        }
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
                Color color = JColorChooser.showDialog(mapMaker, "Choose a preferred color for area " + newAreaName, Color.WHITE);
                if (color != null) {
                    color = DrawUtils.permuteColor(color, areaIndexMap);

                    // Get terrain type
                    TerrainType terrainType = (TerrainType)JOptionPane.showInputDialog(mapMaker, "Terrain Type", "Please specify the terrain type:", JOptionPane.PLAIN_MESSAGE, null, TerrainType.values(), TerrainType.GRASS);

                    if (terrainType != null) {
                        // Get weather type
                        WeatherState weatherState = (WeatherState)JOptionPane.showInputDialog(mapMaker, "Weather State", "Please specify the weather state:", JOptionPane.PLAIN_MESSAGE, null, WeatherState.values(), WeatherState.NORMAL);

                        if (weatherState != null) {
                            // Save index file with new area
//                            File areaIndexFile = new File(mapMaker.getPathWithRoot(FileName.MAP_AREA_INDEX));
//
//                            // TODO: Use FileIO for this -- yeah not doing that because this is getting deleted succkkkaaaaahhhh
//                            try {
//                                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(areaIndexFile, true)));
//                                out.println("\"" + newAreaName + "\"\t\t" + (Long.toString(color.getRGB() & 0xFFFFFFFFL, 16) ) + "\t\t" +weatherState+"\t\t" +terrainType);
//                            }
//                            catch (IOException ex) {
//                                ex.printStackTrace();
//                            }

                            // Add area to the list.
                            areaListModel.addElement(new ImageIcon(DrawUtils.colorWithText(newAreaName, color), color.getRGB() + ""));
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

                areaListModel.addElement(new ImageIcon(DrawUtils.colorWithText(newAreaName, new Color(color, true)), color + ""));
            }
        }
    }

    // Draw area label on mouse hover
    @Override
    public void draw(Graphics2D g2d, MapMaker mapMaker) {
        Point mouseHoverLocation = mapMaker.getMouseHoverLocation();
        Point location = DrawUtils.getLocation(mouseHoverLocation, mapMaker.getMapLocation());
        int tileColor = mapMaker.getTile(location, MapDataType.AREA);

        String areaName = areaIndexMap.get(tileColor);
        if (areaName == null) {
            areaName = areaIndexMap.get(0);
        }

        g2d.setColor(Color.BLACK);
        DrawUtils.setFont(g2d, 16);
        g2d.drawString(areaName, mouseHoverLocation.x, mouseHoverLocation.y);
    }
}
