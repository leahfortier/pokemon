package mapMaker.dialogs;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import map.entity.NPCEntityData;
import mapMaker.MapMaker;

public class NPCEntityDialog extends JPanel {
	
	private static final long serialVersionUID = -8061888140387296525L;

	private JLabel label;
	
	private JTextField nameTextField;
	
	private JComboBox<ImageIcon> spriteComboBox;
	private JComboBox<String> directionComboBox;
	
	private JTextArea firstDialogueTextArea;
	private JTextArea secondDialogueTextArea;
	
	private JCheckBox walkToPlayerCheckBox;
	private JTextField pathTextField;
	
	private JTextArea firstTriggersTextArea;
	private JTextArea secondTriggersTextArea;
	
	private JTextArea trainerDataTextArea;
	private JTextArea giveItemsTextArea;
	
	private JTextField conditionTextField;
	
	// TODO: simplify adding data to NPC
		//item list
		//first and second trigger parsing (remove "trigger: " when reading and add it when saving)
	
		//remove give items and instead improve first and second triggers
			//Make JList and allow to edit selected or add new triggers. New triggers can be a name or from a new dialog.
	
	private MapMaker mapMaker;
	private String[] facingDirections = new String[] {"Right", "Up", "Left", "Down"};
	
	public NPCEntityDialog(MapMaker givenMapMaker) {
		this.mapMaker = givenMapMaker;
		
		label = new JLabel();
		label.setBackground(Color.WHITE);
		
		final ImageIcon[] trainerSprites = getTrainerSprites();
		
		//spriteComboBox = new JComboBox<ImageIcon>(trainerSprites);
		spriteComboBox = new JComboBox<ImageIcon>();
		spriteComboBox.setModel(new DefaultComboBoxModel<ImageIcon>(trainerSprites));
		spriteComboBox.setSelectedIndex(1);
		
		label.setIcon(new ImageIcon(mapMaker.getTileFromSet("Trainer", 12 + 4)));
		
		JLabel lblSprite = new JLabel("Sprite");
		
		JLabel lblDirection = new JLabel("Direction");
		
		directionComboBox = new JComboBox<String>();
		directionComboBox.setModel(new DefaultComboBoxModel<String>(facingDirections));
		directionComboBox.setSelectedIndex(3);
		
		spriteComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int index = Integer.parseInt(((ImageIcon) spriteComboBox.getSelectedItem()).getDescription());
				int direction = directionComboBox.getSelectedIndex();
				BufferedImage img = mapMaker.getTileFromSet("Trainer", 12*index + 1 + direction);
				img = img.getSubimage(0, 0, Math.min(img.getWidth(), 50), Math.min(img.getHeight(), 50));
				ImageIcon icon = new ImageIcon(img);
				
				label.setIcon(icon);
			}
		});
		
		directionComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int index = Integer.parseInt(((ImageIcon) spriteComboBox.getSelectedItem()).getDescription());
				int direction = directionComboBox.getSelectedIndex();
				BufferedImage img = mapMaker.getTileFromSet("Trainer", 12*index + 1 + direction);
				img = img.getSubimage(0, 0, Math.min(img.getWidth(), 50), Math.min(img.getHeight(), 50));
				ImageIcon icon = new ImageIcon(img);
				
				label.setIcon(icon);
			}
		});
		
		JLabel nameLabel = new JLabel("Name");
		
		nameTextField = new JTextField();
		nameTextField.setColumns(10);
		
		JLabel firstDialoguelabel = new JLabel("First Dialogue");
		
		JLabel secondDialoguelabel = new JLabel("Second Dialogue");
		
		walkToPlayerCheckBox = new JCheckBox("Walk to player");
		
		JLabel pathLabel = new JLabel("Path");
		
		pathTextField = new JTextField("w");
		pathTextField.setColumns(10);
		
		JLabel firstTriggersLabel = new JLabel("First Triggers");
		JLabel secondTriggersLabel = new JLabel("Second Triggers");
		
		JLabel trainerDataLabel = new JLabel("Trainer Data");
		
		JLabel giveItemsLabel = new JLabel("Give Items and Pokemon");
		
		JScrollPane scrollPane_1 = new JScrollPane();
		
		firstTriggersTextArea = new JTextArea();
		scrollPane_1.setViewportView(firstTriggersTextArea);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		
		secondDialogueTextArea = new JTextArea();
		scrollPane_2.setViewportView(secondDialogueTextArea);
		
		JScrollPane scrollPane_3 = new JScrollPane();
		secondTriggersTextArea = new JTextArea();
		scrollPane_3.setViewportView(secondTriggersTextArea);
		
		JScrollPane scrollPane_4 = new JScrollPane();
		
		trainerDataTextArea = new JTextArea();
		trainerDataTextArea.setEnabled(false);
		scrollPane_4.setViewportView(trainerDataTextArea);
		
		JScrollPane scrollPane_5 = new JScrollPane();
		
		giveItemsTextArea = new JTextArea();
		scrollPane_5.setViewportView(giveItemsTextArea);
		
		JScrollPane scrollPane = new JScrollPane();
		
		firstDialogueTextArea = new JTextArea();
		firstDialogueTextArea.setText("I have no dialogue yet.");
		scrollPane.setViewportView(firstDialogueTextArea);
		
		JLabel conditionLabel = new JLabel("Condition");
		
		conditionTextField = new JTextField();
		conditionTextField.setColumns(10);
		
		JButton btnEditTrainer = new JButton("Edit Trainer");
		btnEditTrainer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				TrainerDataDialog dialog = new TrainerDataDialog();
				
				if (trainerDataTextArea.getText().trim().length() != 0)
					dialog.setTrainerData(trainerDataTextArea.getText().trim());

				Object[] options = {"Done", "Cancel"};
				
				int results = JOptionPane.showOptionDialog(mapMaker, dialog, "Trainer Data Editor", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				
				if (results == JOptionPane.CLOSED_OPTION || results == 1)
					return;
				
				trainerDataTextArea.setText(dialog.getTrainerData());
			}
		});
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(label, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblSprite, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblDirection, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE))
							.addGap(1)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(spriteComboBox, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE)
								.addComponent(directionComboBox, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE))
							.addGap(12)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(walkToPlayerCheckBox, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE)
								.addComponent(pathLabel, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(69)
									.addComponent(pathTextField, GroupLayout.PREFERRED_SIZE, 154, GroupLayout.PREFERRED_SIZE))))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(nameLabel, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
							.addGap(1)
							.addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, 161, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(69)
									.addComponent(conditionTextField, GroupLayout.PREFERRED_SIZE, 154, GroupLayout.PREFERRED_SIZE))
								.addComponent(conditionLabel, GroupLayout.PREFERRED_SIZE, 71, GroupLayout.PREFERRED_SIZE)))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(firstDialoguelabel, GroupLayout.PREFERRED_SIZE, 123, GroupLayout.PREFERRED_SIZE)
							.addGap(112)
							.addComponent(firstTriggersLabel, GroupLayout.PREFERRED_SIZE, 123, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 223, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addComponent(scrollPane_1, GroupLayout.PREFERRED_SIZE, 223, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(secondDialoguelabel, GroupLayout.PREFERRED_SIZE, 123, GroupLayout.PREFERRED_SIZE)
							.addGap(112)
							.addComponent(secondTriggersLabel, GroupLayout.PREFERRED_SIZE, 123, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(scrollPane_2, GroupLayout.PREFERRED_SIZE, 223, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addComponent(scrollPane_3, GroupLayout.PREFERRED_SIZE, 223, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(scrollPane_4, GroupLayout.PREFERRED_SIZE, 223, GroupLayout.PREFERRED_SIZE)
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(106)
									.addComponent(btnEditTrainer, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE))
								.addComponent(trainerDataLabel, GroupLayout.PREFERRED_SIZE, 123, GroupLayout.PREFERRED_SIZE))
							.addGap(12)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(giveItemsLabel, GroupLayout.PREFERRED_SIZE, 123, GroupLayout.PREFERRED_SIZE)
								.addComponent(scrollPane_5, GroupLayout.PREFERRED_SIZE, 223, GroupLayout.PREFERRED_SIZE)))))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(label, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblSprite)
							.addGap(12)
							.addComponent(lblDirection))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(spriteComboBox, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
							.addComponent(directionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(walkToPlayerCheckBox)
							.addGap(5)
							.addComponent(pathLabel))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(22)
							.addComponent(pathTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addGap(3)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(6)
							.addComponent(nameLabel))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(1)
							.addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(conditionTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(6)
							.addComponent(conditionLabel)))
					.addGap(2)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(firstDialoguelabel)
						.addComponent(firstTriggersLabel))
					.addGap(4)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
						.addComponent(scrollPane_1, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE))
					.addGap(10)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(secondDialoguelabel)
						.addComponent(secondTriggersLabel))
					.addGap(4)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane_2, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
						.addComponent(scrollPane_3, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE))
					.addGap(7)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(25)
							.addComponent(scrollPane_4, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE))
						.addComponent(btnEditTrainer)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(5)
							.addComponent(trainerDataLabel))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(5)
							.addComponent(giveItemsLabel)
							.addGap(4)
							.addComponent(scrollPane_5, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE))))
		);
		setLayout(groupLayout);
	}
	
	private ImageIcon[] getTrainerSprites() {
		ArrayList<ImageIcon> icons = new ArrayList<>();
		
		for (int curr = 0; mapMaker.getTileFromSet("Trainer", 12*curr + 4) != null; ++curr) {
			icons.add(new ImageIcon(mapMaker.getTileFromSet("Trainer", 12*curr + 4), "" + curr));
		}
		
		ImageIcon[] imageIcons = new ImageIcon[icons.size()];
		return icons.toArray(imageIcons);
	}
	
	public void setNPCData(NPCEntityData npc, String name) {
		nameTextField.setText(name);
		directionComboBox.setSelectedIndex(npc.defaultDirection);
		spriteComboBox.setSelectedIndex(npc.spriteIndex);
		
		conditionTextField.setText(npc.condition.getOriginalConditionString().replace("&"," & ").replace("|"," | "));
		
		StringBuilder dialogue = new StringBuilder();
		for (String currentDialogue: npc.firstDialogue) {
			dialogue.append(currentDialogue +"\n");
		}
		firstDialogueTextArea.setText(dialogue.toString().replaceAll("\\\\u00e9", "\u00e9").replaceAll("\\\\u2640", "\u2640").replaceAll("\\\\u2642", "\u2642"));
		if (firstDialogueTextArea.getText().trim().length() == 0) {
			firstDialogueTextArea.setText("I have no dialogue yet.");
		}
		
		
		dialogue.delete(0, dialogue.length());
		
		if (npc.secondDialogue != null){
			for (String currentDialogue: npc.secondDialogue) {
				dialogue.append(currentDialogue +"\n");
			}
			secondDialogueTextArea.setText(dialogue.toString().replaceAll("\\\\u00e9", "\u00e9").replaceAll("\\\\u2640", "\u2640").replaceAll("\\\\u2642", "\u2642"));
		}
		
		pathTextField.setText(npc.path);
		walkToPlayerCheckBox.setSelected(npc.walkToPlayer == 1);
		
		giveItemsTextArea.setText(npc.itemInfo != null?npc.itemInfo.replace("\t", ""):null);
		trainerDataTextArea.setText(npc.trainerInfo != null?npc.trainerInfo.replace("\t", ""):null);
		
		firstTriggersTextArea.setText(npc.firstTriggers != null?npc.firstTriggers.replace("\t", ""):null);
		secondTriggersTextArea.setText(npc.secondTriggers != null?npc.secondTriggers.replace("\t", ""):null);
	}
	
	public NPCEntityData getNPC() {
		return new NPCEntityData(nameTextField.getText(),
				"condition: " + conditionTextField.getText().trim().replaceAll("\\s + ", ""),
				-1, 
				-1, 
				null, 
				pathTextField.getText().trim().length() == 0? "w": pathTextField.getText().trim(), 
				directionComboBox.getSelectedIndex(), 
				spriteComboBox.getSelectedIndex(), 
				firstDialogueTextArea.getText().replaceAll("\u00e9", "\\\\u00e9").replaceAll("\u2640", "\\\\u2640").replaceAll("\u2642", "\\\\u2642").trim().split("\n"), 
				secondDialogueTextArea.getText().trim().length() == 0? (walkToPlayerCheckBox.isSelected()? new String[]{""}: null): secondDialogueTextArea.getText().replaceAll("\u00e9", "\\\\u00e9").replaceAll("\u2640", "\\\\u2640").replaceAll("\u2642", "\\\\u2642").trim().split("\n"), 
				trainerDataTextArea.getText().trim().length() == 0? null: trainerDataTextArea.getText().trim().replace("\n", "\n\t\t"), 
				giveItemsTextArea.getText().trim().length() == 0? null: giveItemsTextArea.getText().trim().replace("\n", "\n\t\t"), 
				firstTriggersTextArea.getText().trim().length() == 0? null: firstTriggersTextArea.getText().trim().replace("\n", "\n\t\t"),
				secondTriggersTextArea.getText().trim().length() == 0? null: secondTriggersTextArea.getText().trim().replace("\n", "\n\t\t"),
				walkToPlayerCheckBox.isSelected());
	}
}
