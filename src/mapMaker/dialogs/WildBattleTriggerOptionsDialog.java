package mapMaker.dialogs;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import map.triggers.TriggerData;
import map.triggers.WildBattleTrigger;
import mapMaker.data.MapMakerTriggerData;

public class WildBattleTriggerOptionsDialog extends JPanel {
	
	private static final long serialVersionUID = -7378035463487486331L;
	
	public JComboBox<String> comboBox;
	private JButton btnEdit;
	HashMap<String, TriggerData> wildBattleTriggers;
	MapMakerTriggerData mapMakerTriggerData;
	
	
	
	public WildBattleTriggerOptionsDialog(HashMap<String, TriggerData> givenWildBattleTriggers, MapMakerTriggerData givenMapMakerTriggerData) {
		
		this.wildBattleTriggers = givenWildBattleTriggers;
		this.mapMakerTriggerData = givenMapMakerTriggerData;
		
		JButton btnCreate = new JButton("Create New");
		btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				TriggerData td = editWildBattleTrigger(null, (JButton)e.getSource());
				
				if(td == null)
					return;
				
				wildBattleTriggers.put(td.name, td);
				comboBox.addItem(td.name);
				
				if(wildBattleTriggers.size() == 1) {
					btnEdit.setEnabled(true);
				}
			}
		});
		
		String[] items = new String[givenWildBattleTriggers.size()];
		comboBox = new JComboBox<String>(givenWildBattleTriggers.keySet().toArray(items));
		
		
		btnEdit = new JButton("Edit");
		btnEdit.setEnabled(items.length != 0);
		btnEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				TriggerData trigger = wildBattleTriggers.get((String)comboBox.getSelectedItem());
				
				TriggerData td = editWildBattleTrigger(trigger, (JButton)e.getSource());
				
				if(td == null)
					return;
				
				mapMakerTriggerData.renameTriggerData(trigger, td);
				
				comboBox.removeAllItems();
				String[] items = new String[wildBattleTriggers.size()];
				for(String item: wildBattleTriggers.keySet().toArray(items)) {
					comboBox.addItem(item);
				}
			}
		});
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, 246, GroupLayout.PREFERRED_SIZE)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(btnCreate, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addComponent(btnEdit, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE))))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(7)
					.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(btnCreate)
						.addComponent(btnEdit)))
		);
		setLayout(groupLayout);
	}
	
	public TriggerData editWildBattleTrigger(TriggerData trigger, JButton button) {
		

		WildBattleTriggerEditDialog dialog = new WildBattleTriggerEditDialog();
		
		if(trigger != null) {
			dialog.initialize(new WildBattleTrigger(trigger.name, trigger.triggerContents));
		}
		
		Object[] options = {"Done", "Cancel"};
		
		int results = JOptionPane.showOptionDialog(button, dialog, "Wild Battle Trigger Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		if(results == JOptionPane.CLOSED_OPTION || results == 1)
			return null;
		
		TriggerData td = dialog.getTriggerData();
		
		return td;
	}
}
