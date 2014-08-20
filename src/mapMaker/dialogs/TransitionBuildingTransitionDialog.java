package mapMaker.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import mapMaker.MapMaker;
import mapMaker.data.MapMakerTriggerData;
import mapMaker.data.TransitionBuildingPair;

public class TransitionBuildingTransitionDialog extends JPanel {
	
	private static final long serialVersionUID = -8645769582436950833L;
	
	private JLabel LeftDirectionLabel;
	private JLabel RightDirectionLabel;
	private JComboBox<String> RightMapComboBox;
	private JComboBox<String> LeftMapComboBox;
	private JComboBox<String> DirectionComboBox;

	public static final String[] Directions = {"Horizontal", "Vertical"};
	
	public MapMaker mapMaker;
	public MapMakerTriggerData triggerData;
	
	public String currentMapName;
	public boolean currentOnLeft;
	
	
	public TransitionBuildingTransitionDialog(MapMaker givenMapMaker, MapMakerTriggerData givenTriggerData, String givenCurrentMapName) {
		
		mapMaker = givenMapMaker;
		triggerData = givenTriggerData;
		currentMapName = givenCurrentMapName;
		currentOnLeft = true;
		
		
		DirectionComboBox = new JComboBox<String>(Directions);
		//DirectionComboBox = new JComboBox<String>();
		
		DirectionComboBox.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (DirectionComboBox.getSelectedIndex() == 0) {
					LeftDirectionLabel.setText("West Map");
					RightDirectionLabel.setText("East Map");
				}
				else {
					LeftDirectionLabel.setText("South Map");
					RightDirectionLabel.setText("North Map");
				}
			}
		});
		
		
		LeftDirectionLabel = new JLabel("West Map");
		RightDirectionLabel = new JLabel("East Map");
		
		JButton SwapButton = new JButton("Swap");
		SwapButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				int temp = LeftMapComboBox.getSelectedIndex();
				LeftMapComboBox.setSelectedIndex(RightMapComboBox.getSelectedIndex());
				RightMapComboBox.setSelectedIndex(temp);
				
				currentOnLeft = !currentOnLeft;
				if (currentOnLeft){
					LeftMapComboBox.setSelectedItem(currentMapName);
					LeftMapComboBox.setEnabled(false);
					RightMapComboBox.setEnabled(true);
				}
				else {
					RightMapComboBox.setSelectedItem(currentMapName);
					RightMapComboBox.setEnabled(false);
					LeftMapComboBox.setEnabled(true);
				}
			}
		});
		
		//Fill combo boxes with available maps.
		String[] mapList = mapMaker.getAvailableMaps();
		String[] updatedMapList = new String[mapList.length+1];
		updatedMapList[0] = "";
		for (int currMap = 0; currMap < mapList.length; ++currMap) {
			updatedMapList[currMap+1] = mapList[currMap];
		}
		
		LeftMapComboBox = new JComboBox<String>(updatedMapList);
		RightMapComboBox = new JComboBox<String>(updatedMapList);
		
		//LeftMapComboBox = new JComboBox<String>();
		//RightMapComboBox = new JComboBox<String>();
		
		LeftMapComboBox.setSelectedItem(currentMapName);
		LeftMapComboBox.setEnabled(false);
		
		
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addComponent(DirectionComboBox, GroupLayout.PREFERRED_SIZE, 151, GroupLayout.PREFERRED_SIZE))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(16)
					.addComponent(LeftDirectionLabel, GroupLayout.PREFERRED_SIZE, 141, GroupLayout.PREFERRED_SIZE)
					.addGap(114)
					.addComponent(RightDirectionLabel, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addComponent(LeftMapComboBox, GroupLayout.PREFERRED_SIZE, 151, GroupLayout.PREFERRED_SIZE)
					.addGap(19)
					.addComponent(SwapButton, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
					.addGap(23)
					.addComponent(RightMapComboBox, GroupLayout.PREFERRED_SIZE, 151, GroupLayout.PREFERRED_SIZE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addComponent(DirectionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(LeftDirectionLabel)
						.addComponent(RightDirectionLabel))
					.addGap(11)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(1)
							.addComponent(LeftMapComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(SwapButton)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(1)
							.addComponent(RightMapComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
		);
		setLayout(groupLayout);
	}
	
	public TransitionBuildingPair getTransitionPair() {
		
		if (LeftMapComboBox.getSelectedIndex() == 0 || RightMapComboBox.getSelectedIndex() == 0)
			return null;
		
		return new TransitionBuildingPair(DirectionComboBox.getSelectedIndex() == 0, (String)RightMapComboBox.getSelectedItem(), (String)LeftMapComboBox.getSelectedItem(), -1);
	}
}
