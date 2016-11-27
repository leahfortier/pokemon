package mapMaker.dialogs;

import map.Direction;
import map.PathDirection;
import mapMaker.MapMaker;
import pattern.map.MapDataMatcher;
import pattern.map.MapTransitionMatcher;
import util.FileIO;
import util.GUIUtils;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.util.Set;
import java.util.stream.Collectors;

public class MapTransitionDialog extends TriggerDialog<MapTransitionMatcher> {
	private static final long serialVersionUID = 6937677302812347311L;

	private JComboBox<String> destinationComboBox;
	private final JComboBox<String> entranceComboBox;
	private final JComboBox<PathDirection> directionComboBox;
	private final JCheckBox deathPortalCheckBox;
	private final JTextField entranceNameTextField;

	private static Set<String> getMapEntrancesForMap(MapMaker mapMaker, String mapName) {
		String mapFileName = mapMaker.getMapTextFileName(mapName);
		String fileText = FileIO.readEntireFileWithReplacements(mapFileName, false);
		MapDataMatcher mapDataMatcher = MapDataMatcher.matchArea(mapFileName, fileText);

		return mapDataMatcher.getMapTransitions()
				.stream()
				.map(MapTransitionMatcher::getExitName)
				.collect(Collectors.toSet());
	}

	public MapTransitionDialog(MapTransitionMatcher mapTransitionMatcher, MapMaker givenMapMaker) {
		super("Map Transition Editor");

		deathPortalCheckBox = GUIUtils.createCheckBox("Death Portal");
		entranceNameTextField = new JTextField();

		// Fill combo boxes with available maps.
		String[] mapList = givenMapMaker.getAvailableMaps();
		String[] updatedMapList = new String[mapList.length + 1];
		updatedMapList[0] = "";
		System.arraycopy(mapList, 0, updatedMapList, 1, mapList.length);

		entranceComboBox = GUIUtils.createComboBox(new String[0], null);
		entranceComboBox.setEnabled(false);

		destinationComboBox = GUIUtils.createComboBox(
				updatedMapList,
				actionEvent -> {
					// Fill entranceComboBox with available entrances.
					if (destinationComboBox.getSelectedIndex() == 0) {
						entranceComboBox.setEnabled(false);
						entranceComboBox.removeAllItems();
					}
					else {
						entranceComboBox.setEnabled(true);
						entranceComboBox.removeAllItems();

						String destinationMap = (String)destinationComboBox.getSelectedItem();
						getMapEntrancesForMap(givenMapMaker, destinationMap)
								.forEach(entranceComboBox::addItem);
					}
				}
		);

		directionComboBox = GUIUtils.createComboBox(PathDirection.values(), null);

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

	private String getDestination() {
		return (String)destinationComboBox.getSelectedItem();
	}
	
	private String getMapEntrance() {
		return (String)entranceComboBox.getSelectedItem();
	}

	@Override
	protected MapTransitionMatcher getMatcher() {
		String destination = getDestination();
		String entrance = getMapEntrance();

		if (destination.isEmpty() || entrance.isEmpty()) {
			return null;
		}

		return new MapTransitionMatcher(
				this.entranceNameTextField.getText(),
				destination,
				entrance,
				(PathDirection)directionComboBox.getSelectedItem(),
				this.deathPortalCheckBox.isSelected()
		);
	}

	private void load(MapTransitionMatcher matcher) {
		if (matcher == null) {
			return;
		}

		entranceNameTextField.setText(matcher.getExitName());
		destinationComboBox.setSelectedItem(matcher.getNextMap());
		entranceComboBox.setSelectedItem(matcher.getNextEntranceName());
		directionComboBox.setSelectedIndex(matcher.getDirection().ordinal() + 1); // TODO: Not sure what's going on here but it should probably be in a direction method instead of using the ordinal
		deathPortalCheckBox.setSelected(matcher.isDeathPortal());
	}
}
