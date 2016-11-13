package mapMaker.dialogs;

import map.Direction;
import mapMaker.MapMaker;
import mapMaker.data.MapMakerTriggerData;
import pattern.MapDataMatcher;
import pattern.MapTransitionMatcher;
import util.FileIO;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.util.Set;
import java.util.stream.Collectors;

public class MapTransitionDialog extends TriggerDialog<MapTransitionMatcher> {
	private static final long serialVersionUID = 6937677302812347311L;

	private MapMakerTriggerData triggerData;

	private JComboBox<String> destinationComboBox;
	private JComboBox<String> entranceComboBox;
	private JComboBox<DirectionType> directionComboBox;
	private JCheckBox deathPortalCheckBox;
	private JTextField entranceNameTextField;

	// TODO: Combine this with the direction one when I can add the auto thingy -- just don't want to deal with that right now
	private enum DirectionType {
		AUTO(null),
		RIGHT(Direction.RIGHT),
		UP(Direction.UP),
		LEFT(Direction.LEFT),
		DOWN(Direction.DOWN);

		private final Direction direction;

		DirectionType(Direction direction) {
			this.direction = direction;
		}
	}

	private static Set<String> getMapEntrancesForMap(MapMaker mapMaker, String mapName) {
		String mapFileName = mapMaker.getMapTextFileName(mapName);
		String fileText = FileIO.readEntireFileWithReplacements(mapFileName, false);
		MapDataMatcher mapDataMatcher = MapDataMatcher.matchArea(mapFileName, fileText);

		return mapDataMatcher.getMapExits()
				.stream()
				.map(MapTransitionMatcher::getExitName)
				.collect(Collectors.toSet());
	}

	public MapTransitionDialog(MapMaker givenMapMaker, MapMakerTriggerData givenTriggerData) {
		triggerData = givenTriggerData;

		JLabel destinationLabel = new JLabel("Destination");
		JLabel entranceLabel = new JLabel("Destination Entrance");
		JLabel directionLabel = new JLabel("Direction");
		JLabel entranceNameLabel = new JLabel("Entrance Name");

		deathPortalCheckBox = new JCheckBox("Death Portal");
		entranceNameTextField = new JTextField();
		entranceNameTextField.setColumns(10);
		
		// Fill combo boxes with available maps.
		String[] mapList = givenMapMaker.getAvailableMaps();
		String[] updatedMapList = new String[mapList.length + 1];
		updatedMapList[0] = "";
		System.arraycopy(mapList, 0, updatedMapList, 1, mapList.length);
		
		destinationComboBox = new JComboBox<>(updatedMapList);
		
		destinationComboBox.addActionListener(actionEvent -> {
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
        });
		
		entranceComboBox = new JComboBox<>();
		entranceComboBox.setEnabled(false);
		
		directionComboBox = new JComboBox<>(DirectionType.values());
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(entranceNameLabel, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addComponent(entranceNameTextField, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(destinationLabel, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addComponent(destinationComboBox, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(entranceLabel, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addComponent(entranceComboBox, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(directionLabel, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addComponent(directionComboBox, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(deathPortalCheckBox, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE))))
		);

		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(2)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup())
							.addGap(4)
							.addComponent(entranceNameLabel)
						.addComponent(entranceNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(4)
							.addComponent(destinationLabel))
						.addComponent(destinationComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(1)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(4)
							.addComponent(entranceLabel))
						.addComponent(entranceComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(1)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(4)
							.addComponent(directionLabel))
						.addComponent(directionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGroup(groupLayout.createSequentialGroup())
						.addComponent(deathPortalCheckBox))
		);
		setLayout(groupLayout);
	}

	public String getDestination() {
		return (String)destinationComboBox.getSelectedItem();
	}
	
	public String getMapEntrance() {
		return (String)entranceComboBox.getSelectedItem();
	}
	
	public void setMapTransition(MapTransitionMatcher mapTransition) {
		destinationComboBox.setSelectedItem(mapTransition.getNextMap());
		entranceComboBox.setSelectedItem(mapTransition.getNextEntranceName());
		directionComboBox.setSelectedIndex(mapTransition.getDirection().ordinal() + 1); // TODO: Not sure what's going on here but it should probably be in a direction method instead of using the ordinal
	}

	@Override
	public MapTransitionMatcher getMatcher() {
		String destination = getDestination();
		String entrance = getMapEntrance();

		if (destination.isEmpty() || entrance.isEmpty()) {
			return null;
		}

		return new MapTransitionMatcher(
				this.entranceNameTextField.getText(),
				destination,
				entrance,
				((DirectionType)directionComboBox.getSelectedItem()).direction,
				this.deathPortalCheckBox.isSelected()
		);
	}

	@Override
	protected void load(MapTransitionMatcher matcher) {
		this.entranceNameTextField.setText(matcher.getExitName());

	}
}
