package mapMaker.dialogs;

import map.entity.npc.NPCEntityData;
import mapMaker.MapMaker;
import mapMaker.model.TileModel.TileType;
import util.PokeString;
import util.StringUtils;

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
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class NPCEntityDialog extends JPanel {
	private static final long serialVersionUID = -8061888140387296525L;

	// TODO: There's shit like this all over the fucking place
	private static final String[] facingDirections = new String[] {"Right", "Up", "Left", "Down"};

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
	
	// TODO: Simplify adding data to NPC
		// Item list
		// First and second trigger parsing (remove "trigger: " when reading and add it when saving)
	
		// Remove give items and instead improve first and second triggers
			// Make JList and allow to edit selected or add new triggers. New triggers can be a name or from a new dialog.
	
	private MapMaker mapMaker;
	
	public NPCEntityDialog(MapMaker givenMapMaker) {
		this.mapMaker = givenMapMaker;
		
		label = new JLabel();
		label.setBackground(Color.WHITE);
		
		final ImageIcon[] trainerSprites = getTrainerSprites();
		
		//spriteComboBox = new JComboBox<ImageIcon>(trainerSprites);
		spriteComboBox = new JComboBox<>();
		spriteComboBox.setModel(new DefaultComboBoxModel<>(trainerSprites));
		spriteComboBox.setSelectedIndex(1);
		
		label.setIcon(new ImageIcon(mapMaker.getTileFromSet(TileType.TRAINER, 12 + 4)));
		
		JLabel lblSprite = new JLabel("Sprite");
		
		JLabel lblDirection = new JLabel("Direction");
		
		directionComboBox = new JComboBox<>();
		directionComboBox.setModel(new DefaultComboBoxModel<>(facingDirections));
		directionComboBox.setSelectedIndex(3);
		
		spriteComboBox.addActionListener(event -> {
            int index = Integer.parseInt(((ImageIcon) spriteComboBox.getSelectedItem()).getDescription());
            int direction = directionComboBox.getSelectedIndex();

            BufferedImage img = mapMaker.getTileFromSet(TileType.TRAINER, 12*index + 1 + direction);
            img = img.getSubimage(0, 0, Math.min(img.getWidth(), 50), Math.min(img.getHeight(), 50));

			ImageIcon icon = new ImageIcon(img);
            label.setIcon(icon);
        });

		// TODO: UMMM IS THIS EXACTLY THE SAME AS THE ONE ABOVE?? IF SO SRSLY WHAT THE FUCK
		directionComboBox.addActionListener(event -> {
            int index = Integer.parseInt(((ImageIcon) spriteComboBox.getSelectedItem()).getDescription());
            int direction = directionComboBox.getSelectedIndex();

            BufferedImage img = mapMaker.getTileFromSet(TileType.TRAINER, 12*index + 1 + direction);
            img = img.getSubimage(0, 0, Math.min(img.getWidth(), 50), Math.min(img.getHeight(), 50));

			ImageIcon icon = new ImageIcon(img);
            label.setIcon(icon);
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
		btnEditTrainer.addActionListener(event -> {

            TrainerDataDialog dialog = new TrainerDataDialog();

            if (!trainerDataTextArea.getText().trim().isEmpty()) {
				dialog.setTrainerData(trainerDataTextArea.getText().trim());
			}

            Object[] options = {"Done", "Cancel"};

            int results = JOptionPane.showOptionDialog(
            		mapMaker,
					dialog,
					"Trainer Data Editor",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.PLAIN_MESSAGE,
					null,
					options,
					options[0]
			);

            if (results == JOptionPane.CLOSED_OPTION || results == 1) {
				return;
			}

            trainerDataTextArea.setText(dialog.getTrainerData());
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
		List<ImageIcon> icons = new ArrayList<>();
		
		for (int curr = 0; mapMaker.getTileFromSet(TileType.TRAINER, 12*curr + 4) != null; curr++) {
			icons.add(new ImageIcon(mapMaker.getTileFromSet(TileType.TRAINER, 12*curr + 4), "" + curr));
		}
		
		ImageIcon[] imageIcons = new ImageIcon[icons.size()];
		return icons.toArray(imageIcons);
	}
	
	public void setNPCData(NPCEntityData npc, String name) {
		nameTextField.setText(name);
		directionComboBox.setSelectedIndex(npc.defaultDirection.ordinal());
		spriteComboBox.setSelectedIndex(npc.spriteIndex);
		
		conditionTextField.setText(npc.condition.getOriginalConditionString().replace("&"," & ").replace("|"," | "));
		
		StringBuilder dialogue = new StringBuilder();
		for (String currentDialogue: npc.firstDialogue) {
			StringUtils.appendLine(dialogue, currentDialogue);
		}

		firstDialogueTextArea.setText(PokeString.restoreSpecialFromUnicode(dialogue.toString()));
		if (firstDialogueTextArea.getText().trim().isEmpty()) {
			firstDialogueTextArea.setText("I have no dialogue yet."); // TODO: There should be a constant for the default expression and it should be more interesting than this
		}
		
		dialogue.delete(0, dialogue.length());
		
		if (npc.secondDialogue != null){
			for (String currentDialogue: npc.secondDialogue) {
				StringUtils.appendLine(dialogue, currentDialogue);
			}

			secondDialogueTextArea.setText(PokeString.restoreSpecialFromUnicode(dialogue.toString()));
		}
		
		pathTextField.setText(npc.path);
		walkToPlayerCheckBox.setSelected(npc.walkToPlayer == 1);

		// TODO: make a method for this and fix the uggyness too fucking tired for that shit right now
		giveItemsTextArea.setText(npc.itemInfo != null?npc.itemInfo.replace("\t", ""):null);
		trainerDataTextArea.setText(npc.trainerInfo != null?npc.trainerInfo.replace("\t", ""):null);
		
		firstTriggersTextArea.setText(npc.firstTriggers != null?npc.firstTriggers.replace("\t", ""):null);
		secondTriggersTextArea.setText(npc.secondTriggers != null?npc.secondTriggers.replace("\t", ""):null);
	}
	
	private static String getTrimmedAreaText(JTextArea textArea) {
		String text = textArea.getText().trim();
		if (text.isEmpty()) {
			return null;
		}

		// TODO: Maybe make a method in the string util class that takes in the number of tabs to insert
		return text.replace("\n", "\n\t\t");
	}
	
	public NPCEntityData getNPC() {
		return new NPCEntityData(nameTextField.getText(),
				"condition: " + conditionTextField.getText().trim().replaceAll("\\s + ", ""),
				-1, 
				-1, 
				null, 
				pathTextField.getText().trim().isEmpty() ? "w" : pathTextField.getText().trim(), // TODO: What is w -- should it be a constant?
				directionComboBox.getSelectedIndex(), 
				spriteComboBox.getSelectedIndex(), 
				PokeString.convertSpecialToUnicode(firstDialogueTextArea.getText()).trim().split("\n"), 
				secondDialogueTextArea.getText().trim().isEmpty()
						? (walkToPlayerCheckBox.isSelected() ? new String[]{""} : null) // TODO: ??
						: PokeString.convertSpecialToUnicode(secondDialogueTextArea.getText()).trim().split("\n"),
				getTrimmedAreaText(trainerDataTextArea),
				getTrimmedAreaText(giveItemsTextArea),
				getTrimmedAreaText(firstTriggersTextArea),
				getTrimmedAreaText(secondTriggersTextArea),
				walkToPlayerCheckBox.isSelected());
	}
}
