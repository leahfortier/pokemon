package mapMaker.dialogs;

import map.MapName;
import map.PathDirection;
import mapMaker.MapMaker;
import pattern.map.MapDataMatcher;
import pattern.map.MapTransitionMatcher;
import util.GUIUtils;
import util.StringUtils;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MapTransitionDialog extends TriggerDialog<MapTransitionMatcher> {
    private static final long serialVersionUID = 6937677302812347311L;

    private static final MapName EMPTY_MAP = new MapName("No Destination", StringUtils.empty());

    private final JComboBox<String> entranceComboBox;
    private final JComboBox<PathDirection> directionComboBox;
    private final JCheckBox deathPortalCheckBox;
    private final JTextField entranceNameTextField;
    private JComboBox<MapName> destinationComboBox;

    public MapTransitionDialog(MapTransitionMatcher mapTransitionMatcher, MapMaker givenMapMaker) {
        super("Map Transition Editor");

        deathPortalCheckBox = GUIUtils.createCheckBox("Death Portal");
        entranceNameTextField = GUIUtils.createTextField();

        // Fill combo boxes with available maps.
        String[] regionList = givenMapMaker.getAvailableRegions();
        List<MapName> mapList = new ArrayList<>();
        for (String region : regionList) {
            MapName[] maps = givenMapMaker.getAvailableMaps(region);
            Collections.addAll(mapList, maps);
        }

        MapName[] updatedMapList = new MapName[mapList.size() + 1];
        updatedMapList[0] = EMPTY_MAP;
        for (int i = 0; i < mapList.size(); i++) {
            updatedMapList[i + 1] = mapList.get(i);
        }

        entranceComboBox = GUIUtils.createComboBox(new String[0]);
        entranceComboBox.setEnabled(false);

        destinationComboBox = GUIUtils.createComboBox(
                updatedMapList,
                actionEvent -> {
                    entranceComboBox.setEnabled(destinationComboBox.getSelectedIndex() != 0);
                    entranceComboBox.removeAllItems();

                    // Fill entranceComboBox with available entrances.
                    if (entranceComboBox.isEnabled()) {
                        MapName destinationMap = (MapName)destinationComboBox.getSelectedItem();
                        getMapEntrancesForMap(givenMapMaker, destinationMap)
                                .forEach(entranceComboBox::addItem);
                    }
                }
        );

        directionComboBox = GUIUtils.createComboBox(PathDirection.values());

        GUIUtils.setVerticalLayout(
                this,
                GUIUtils.createTextFieldComponent("Entrance Name", entranceNameTextField),
                GUIUtils.createComboBoxComponent("Destination", destinationComboBox),
                GUIUtils.createComboBoxComponent("Destination Entrance", entranceComboBox),
                GUIUtils.createComboBoxComponent("Direction", directionComboBox),
                deathPortalCheckBox
        );

        this.load(mapTransitionMatcher);
    }

    private MapName getDestination() {
        return (MapName)destinationComboBox.getSelectedItem();
    }

    private String getMapEntrance() {
        return (String)entranceComboBox.getSelectedItem();
    }

    @Override
    protected MapTransitionMatcher getMatcher() {
        return new MapTransitionMatcher(
                this.getNameField(entranceNameTextField),
                getDestination(),
                getMapEntrance(),
                (PathDirection)directionComboBox.getSelectedItem(),
                this.deathPortalCheckBox.isSelected()
        );
    }

    private void load(MapTransitionMatcher matcher) {
        if (matcher == null) {
            return;
        }

        MapName destination = matcher.getNextMap();
        if (destination == null) {
            destination = EMPTY_MAP;
        }

        entranceNameTextField.setText(matcher.getExitName());
        destinationComboBox.setSelectedItem(destination);
        entranceComboBox.setSelectedItem(matcher.getNextEntranceName());
        directionComboBox.setSelectedIndex(matcher.getDirection().ordinal());
        deathPortalCheckBox.setSelected(matcher.isDeathPortal());
    }

    private static Set<String> getMapEntrancesForMap(MapMaker mapMaker, MapName mapName) {
        String mapFileName = mapMaker.getMapTextFileName(mapName);
        MapDataMatcher mapDataMatcher = MapDataMatcher.matchArea(mapFileName);

        return mapDataMatcher.getMapTransitions()
                             .stream()
                             .map(MapTransitionMatcher::getExitName)
                             .collect(Collectors.toSet());
    }
}
