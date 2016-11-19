package mapMaker.dialogs;

import map.Direction;
import mapMaker.MapMaker;
import mapMaker.model.TileModel.TileType;
import pattern.action.NPCInteractionMatcher;
import pattern.map.NPCMatcher;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class NPCEntityDialog extends TriggerDialog<NPCMatcher> {
	private static final long serialVersionUID = -8061888140387296525L;

	private JLabel label;
	
	private JTextField nameTextField;
	
	private JComboBox<ImageIcon> spriteComboBox;
	private JComboBox<Direction> directionComboBox;

    private JTextField pathTextField;
	private JTextField conditionTextField;

	private List<NPCInteractionMatcher> interactions;
	private JButton addInteractionButton;
	
	private MapMaker mapMaker;
	
	public NPCEntityDialog(NPCMatcher npcMatcher, MapMaker givenMapMaker) {
		super("NPC Editor");

		this.mapMaker = givenMapMaker;
		interactions = new ArrayList<>();
		addInteractionButton = new JButton("Add Interaction");
		addInteractionButton.addActionListener(event -> {
			interactions.add(null);
            render();
        });

		label = new JLabel();
		label.setBackground(Color.WHITE);

		final ImageIcon[] trainerSprites = getTrainerSprites();

		spriteComboBox = new JComboBox<>();
		spriteComboBox.setModel(new DefaultComboBoxModel<>(trainerSprites));
		spriteComboBox.setSelectedIndex(1);

		// TODO: 12 + 4?
		label.setIcon(new ImageIcon(mapMaker.getTileFromSet(TileType.TRAINER, 12 + 4)));

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

		nameTextField = new JTextField();
		nameTextField.setColumns(10);

		pathTextField = new JTextField("w");
		pathTextField.setColumns(10);

		conditionTextField = new JTextField();
		conditionTextField.setColumns(10);

		this.load(npcMatcher);
	}

	@Override
	protected void renderDialog() {
		removeAll();
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(label);
		panel.add(spriteComboBox);
		panel.add(directionComboBox);
		panel.add(nameTextField);
		panel.add(conditionTextField);
		panel.add(pathTextField);

		for (int i = 0; i < interactions.size(); i++) {
			final int index = i;
			JButton interactionButton = new JButton("Interaction");
			interactionButton.addActionListener(event -> {
				NPCInteractionMatcher matcher = new NPCInteractionDialog(interactions.get(index)).getMatcher();
				if (matcher != null) {
					interactions.set(index, matcher);
				}
            });
			panel.add(interactionButton);
		}

		panel.add(addInteractionButton);
		add(panel);

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

	@Override
	protected NPCMatcher getMatcher() {
		return new NPCMatcher(
				nameTextField.getText(),
				conditionTextField.getText(),
				pathTextField.getText(),
				spriteComboBox.getSelectedIndex(),
				(Direction)directionComboBox.getSelectedItem(),
				interactions
		);
	}

	private void load(NPCMatcher matcher) {
		if (matcher == null) {
			return;
		}

		nameTextField.setText(matcher.getBasicName());
		conditionTextField.setText(matcher.getCondition());
		pathTextField.setText(matcher.getPath());
		spriteComboBox.setSelectedIndex(matcher.getSpriteIndex());
		directionComboBox.setSelectedItem(matcher.getDirection());
		interactions.addAll(matcher.getInteractionMatcherList());
	}
}
