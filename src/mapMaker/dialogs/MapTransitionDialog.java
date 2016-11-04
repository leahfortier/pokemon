package mapMaker.dialogs;

import map.triggers.MapTransitionTrigger;
import map.triggers.TriggerData;
import mapMaker.MapMaker;
import mapMaker.data.MapMakerTriggerData;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MapTransitionDialog extends JPanel {
	private static final long serialVersionUID = 6937677302812347311L;

	private MapMakerTriggerData triggerData;

	private JComboBox<String> destinationComboBox;
	private JComboBox<String> entranceComboBox;
	private JComboBox<String> directionComboBox;

	// TODO: move direction to map entrance since it makes more sense to put it there.

	private static final String[] DIRECTIONS = { "Auto","Right", "Up", "Left", "Down" };

	public MapTransitionDialog(MapMaker givenMapMaker, MapMakerTriggerData givenTriggerData) {
		triggerData = givenTriggerData;
		
		JLabel destinationLabel = new JLabel("Destination");
		JLabel entranceLabel = new JLabel("Entrance");
		JLabel directionLabel = new JLabel("Direction");
		
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

                String[] mapEntrances = triggerData.getMapEntrancesForMap((String)destinationComboBox.getSelectedItem());
                for (String entrance: mapEntrances) {
                    entranceComboBox.addItem(entrance); // TODO: lambda?
                }
            }
        });
		
		entranceComboBox = new JComboBox<>();
		entranceComboBox.setEnabled(false);
		
		directionComboBox = new JComboBox<>(DIRECTIONS);
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
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
							.addComponent(directionComboBox, GroupLayout.PREFERRED_SIZE, 271, GroupLayout.PREFERRED_SIZE))))
		);

		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(2)
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
						.addComponent(directionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
		);
		setLayout(groupLayout);
	}

	public String getDestination() {
		return (String)destinationComboBox.getSelectedItem();
	}
	
	public String getMapEntrance() {
		return (String)entranceComboBox.getSelectedItem();
	}
	
	public void setMapTransition(MapTransitionTrigger mapTransition) {
		destinationComboBox.setSelectedItem(mapTransition.getNextMap());
		entranceComboBox.setSelectedItem(mapTransition.getMapEntranceName());
		directionComboBox.setSelectedIndex(mapTransition.getDirection().ordinal() + 1); // TODO: Not sure what's going on here but it should probably be in a direction method instead of using the ordinal
	}
	
	public MapTransitionTrigger getMapTransition(String name) {
		String destination = getDestination();
		String entrance = getMapEntrance();
		
		if (destination.isEmpty() || entrance.isEmpty()) {
			return null;
		}
		
//		return new MapTransitionTrigger(name, "", destination, entrance, directionComboBox.getSelectedIndex() - 1);
		return null;
	}
	
	public TriggerData getTriggerData(String name) {
		String destination = getDestination();
		String entrance = getMapEntrance();
		
		if (destination.isEmpty() || entrance.isEmpty()) {
			return null;
		}

		return null;
//		return new TriggerData(name,
//				"MapTransition\n"
//						+ "\tnextMap: " + destination + "\n"
//						+ "\tmapEntrance: " +entrance + "\n"
//						+ (directionComboBox.getSelectedIndex() == 0 ? "" : "\tdirection: " + (directionComboBox.getSelectedIndex() - 1)) + "\n"
//				);
	}
}
