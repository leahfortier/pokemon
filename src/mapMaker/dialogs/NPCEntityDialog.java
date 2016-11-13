package mapMaker.dialogs;

import map.Direction;
import mapMaker.MapMaker;
import mapMaker.model.TileModel.TileType;
import pattern.AreaDataMatcher.NPCInteractionMatcher;
import pattern.AreaDataMatcher.NPCMatcher;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class NPCEntityDialog extends TriggerDialog {
	private static final long serialVersionUID = -8061888140387296525L;

	private JLabel label;
	
	private JTextField nameTextField;
	
	private JComboBox<ImageIcon> spriteComboBox;
	private JComboBox<Direction> directionComboBox;

    private JTextField pathTextField;
	private JTextArea trainerDataTextArea;
	private JTextField conditionTextField;

	private List<NPCInteractionMatcher> interactions;
	private JButton addInteractionButton;

	// TODO: Simplify adding data to NPC
		// Item list
		// First and second trigger parsing (remove "trigger: " when reading and add it when saving)
	
		// Remove give items and instead improve first and second triggers
			// Make JList and allow to edit selected or add new triggers. New triggers can be a name or from a new dialog.
	
	private MapMaker mapMaker;
	
	public NPCEntityDialog(MapMaker givenMapMaker) {
		this.mapMaker = givenMapMaker;
		interactions = new ArrayList<>();
		addInteractionButton = new JButton("Add Interaction");
		addInteractionButton.addActionListener(event -> {
			interactions.add(new NPCInteractionMatcher());
            render();
        });

		render();

//		JButton btnEditTrainer = new JButton("Edit Trainer");
//		btnEditTrainer.addActionListener(event -> {
//
//            TrainerDataDialog dialog = new TrainerDataDialog();
//
//            if (!trainerDataTextArea.getText().trim().isEmpty()) {
//				dialog.setTrainerData(trainerDataTextArea.getText().trim());
//			}
//
//            Object[] options = {"Done", "Cancel"};
//
//            int results = JOptionPane.showOptionDialog(
//            		mapMaker,
//					dialog,
//					"Trainer Data Editor",
//					JOptionPane.YES_NO_OPTION,
//					JOptionPane.PLAIN_MESSAGE,
//					null,
//					options,
//					options[0]
//			);
//
//            if (results == JOptionPane.CLOSED_OPTION || results == 1) {
//				return;
//			}
//
//            trainerDataTextArea.setText(dialog.getTrainerData());
//        });
	}

	private void render() {
		label = new JLabel();
		label.setBackground(Color.WHITE);

		final ImageIcon[] trainerSprites = getTrainerSprites();

		spriteComboBox = new JComboBox<>();
		spriteComboBox.setModel(new DefaultComboBoxModel<>(trainerSprites));
		spriteComboBox.setSelectedIndex(1);

		// TODO: 12 + 4?
		label.setIcon(new ImageIcon(mapMaker.getTileFromSet(TileType.TRAINER, 12 + 4)));

		JLabel lblSprite = new JLabel("Sprite");
		JLabel lblDirection = new JLabel("Direction");

		directionComboBox = new JComboBox<>();
		directionComboBox.setModel(new DefaultComboBoxModel<>(Direction.values()));
		directionComboBox.setSelectedItem(Direction.DOWN);

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

		JLabel pathLabel = new JLabel("Path");

		pathTextField = new JTextField("w");
		pathTextField.setColumns(10);

		JLabel conditionLabel = new JLabel("Condition");

		conditionTextField = new JTextField();
		conditionTextField.setColumns(10);

		removeAll();
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(label);
		panel.add(spriteComboBox);
		panel.add(directionComboBox);
		panel.add(pathTextField);
		panel.add(nameTextField);
		panel.add(conditionTextField);

		for (NPCInteractionMatcher interaction: interactions) {
			JButton interactionButton = new JButton("Interaction");
			interactionButton.addActionListener(event -> {
				new NPCInteractionDialog().giveOption("New NPC Interaction Dialog", this);
            });
			panel.add(interactionButton);
		}

		panel.add(addInteractionButton);
		add(panel);

		this.setPanelSize();

		revalidate();
/*
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
														.addComponent(pathLabel, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
														.addGroup(groupLayout.createSequentialGroup()
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
												.addComponent(addInteractionButton, GroupLayout.PREFERRED_SIZE, 71, GroupLayout.PREFERRED_SIZE))
								)));

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
												.addComponent(conditionLabel))))
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addComponent(addInteractionButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
		);*/

		//setLayout(groupLayout);
	}
	
	private ImageIcon[] getTrainerSprites() {
		List<ImageIcon> icons = new ArrayList<>();

		for (int curr = 0; mapMaker.getTileFromSet(TileType.TRAINER, 12*curr + 4) != null; curr++) {
			icons.add(new ImageIcon(mapMaker.getTileFromSet(TileType.TRAINER, 12*curr + 4), "" + curr));
		}
		
		ImageIcon[] imageIcons = new ImageIcon[icons.size()];
		return icons.toArray(imageIcons);
	}
	
	public void setNPCData(NPCMatcher npc, String name) {
		nameTextField.setText(name);
		directionComboBox.setSelectedIndex(npc.direction.ordinal());
		spriteComboBox.setSelectedIndex(npc.spriteIndex);

		// TODO: This is def gonna npe
		conditionTextField.setText(npc.condition.replace("&"," & ").replace("|"," | "));

		// TODO: Need to build map maker to handle interactions
//		StringBuilder dialogue = new StringBuilder();
//		for (String currentDialogue: npc.firstDialogue) {
//			StringUtils.appendLine(dialogue, currentDialogue);
//		}

//		firstDialogueTextArea.setText(PokeString.restoreSpecialFromUnicode(dialogue.toString()));
//		if (firstDialogueTextArea.getText().trim().isEmpty()) {
//			firstDialogueTextArea.setText("I have no dialogue yet."); // TODO: There should be a constant for the default expression and it should be more interesting than this
//		}

//		dialogue.delete(0, dialogue.length());
		
//		if (npc.secondDialogue != null){
//			for (String currentDialogue: npc.secondDialogue) {
//				StringUtils.appendLine(dialogue, currentDialogue);
//			}
//
//			secondDialogueTextArea.setText(PokeString.restoreSpecialFromUnicode(dialogue.toString()));
//		}

		pathTextField.setText(npc.getPath());

		// TODO: Move checkbox inside interaction
//		walkToPlayerCheckBox.setSelected(npc.walkToPlayer == 1);

		// TODO: make a method for this and fix the uggyness too fucking tired for that shit right now
//		giveItemsTextArea.setText(npc.itemInfo != null?npc.itemInfo.replace("\t", ""):null);
//		trainerDataTextArea.setText(npc.trainerInfo != null?npc.trainerInfo.replace("\t", ""):null);

//		firstTriggersTextArea.setText(npc.firstTriggers != null?npc.firstTriggers.replace("\t", ""):null);
//		secondTriggersTextArea.setText(npc.secondTriggers != null?npc.secondTriggers.replace("\t", ""):null);
	}

	public NPCMatcher getNPC() {
		return new NPCMatcher(
				nameTextField.getText(),
				conditionTextField.getText().trim().replaceAll("\\s + ", ""), // TODO: Make method for this
				pathTextField.getText(),
				spriteComboBox.getSelectedIndex(),
				Direction.values()[directionComboBox.getSelectedIndex()]
		);
	}
}
