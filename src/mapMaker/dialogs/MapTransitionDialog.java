package mapMaker.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import map.triggers.MapTransitionTrigger;
import map.triggers.TriggerData;
import mapMaker.MapMaker;
import mapMaker.data.MapMakerTriggerData;

public class MapTransitionDialog extends JPanel {

	private static final long serialVersionUID = 6937677302812347311L;
	
	private JComboBox<String> destinationComboBox;
	private JComboBox<String> entranceComboBox;
	private JComboBox<String> directionComboBox;
	
	//TODO: move direction to map entrance since it makes more sense to put it there.

	public MapMaker mapMaker;
	public MapMakerTriggerData triggerData;
	
	public static String[] directions = {"Auto","Right", "Up", "Left", "Down"};
	
	public MapTransitionDialog(MapMaker givenMapMaker, MapMakerTriggerData givenTriggerData) {
		
		mapMaker = givenMapMaker;
		triggerData = givenTriggerData;
		
		JLabel destinationLabel = new JLabel("Destination");
		JLabel entranceLabel = new JLabel("Entrance");
		JLabel directionLabel = new JLabel("Direction");
		
		//Fill combo boxes with available maps.
		String[] mapList = mapMaker.getAvailableMaps();
		String[] updatedMapList = new String[mapList.length+1];
		updatedMapList[0] = "";
		for(int currMap = 0; currMap < mapList.length; ++currMap) {
			updatedMapList[currMap+1] = mapList[currMap];
		}
		
		destinationComboBox = new JComboBox<String>(updatedMapList);
		
		destinationComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Fill entranceComboBox with available entrances.
				if(destinationComboBox.getSelectedIndex() == 0) {
					entranceComboBox.setEnabled(false);
					entranceComboBox.removeAllItems();
				}
				else {
					entranceComboBox.setEnabled(true);
					entranceComboBox.removeAllItems();
					
					String[] mapEntrances = triggerData.getMapEntrancesForMap((String)destinationComboBox.getSelectedItem());
					
					for(String entrance: mapEntrances) {
						entranceComboBox.addItem(entrance);
					}
				}
			}
		});
		
		entranceComboBox = new JComboBox<String>();
		entranceComboBox.setEnabled(false);
		
		directionComboBox = new JComboBox<String>(directions);
		
		
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
		destinationComboBox.setSelectedItem(mapTransition.mapName);
		entranceComboBox.setSelectedItem(mapTransition.mapEntranceName);
		directionComboBox.setSelectedIndex(mapTransition.direction +1);
	}
	
	public MapTransitionTrigger getMapTransition(String name) {
		String destination = getDestination();
		String entrance = getMapEntrance();
		
		if(destination.equals("") || entrance.equals(""))
			return null;
		
		return new MapTransitionTrigger(name, "", destination, entrance, directionComboBox.getSelectedIndex()-1);
	}
	
	public TriggerData getTriggerData(String name) {
		String destination = getDestination();
		String entrance = getMapEntrance();
		
		if(destination.equals("") || entrance.equals(""))
			return null;
		
		return new TriggerData(name,
				"MapTransition\n" +
				"\tnextMap: " +destination +"\n"+
				"\tmapEntrance: " +entrance +"\n"+
				(directionComboBox.getSelectedIndex() == 0? "": "\tdirection: " +(directionComboBox.getSelectedIndex()-1)) +"\n"
				);
	}
}
