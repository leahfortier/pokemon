package mapMaker.dialogs;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

public class TransitionBuildingMainSelectDialog extends JPanel {
	
	private static final long serialVersionUID = -967308261145427863L;
	private JComboBox<String> unplacedComboBox;

	public TransitionBuildingMainSelectDialog(String[] comboBoxItems) {
		
		unplacedComboBox = new JComboBox<String>();
		
		if (comboBoxItems != null && comboBoxItems.length > 0) {
			for (String item: comboBoxItems) {
				unplacedComboBox.addItem(item);
			}
		}
		else {
			unplacedComboBox.setEnabled(false);
		}
		
		JLabel lblOnCurrentMap = new JLabel("Unplaced");
		
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addComponent(lblOnCurrentMap, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
					.addGap(12)
					.addComponent(unplacedComboBox, GroupLayout.PREFERRED_SIZE, 220, GroupLayout.PREFERRED_SIZE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(10)
					.addComponent(lblOnCurrentMap))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addComponent(unplacedComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
		);
		setLayout(groupLayout);
	}
	
	public int getSelectedIndex() {
		return unplacedComboBox.getSelectedIndex();
	}
}
