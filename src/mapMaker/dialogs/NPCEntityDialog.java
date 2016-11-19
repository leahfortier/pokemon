package mapMaker.dialogs;

import map.Direction;
import mapMaker.MapMaker;
import mapMaker.model.TileModel.TileType;
import pattern.action.NPCInteractionMatcher;
import pattern.map.NPCMatcher;
import util.GUIUtils;
import util.StringUtils;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class NPCEntityDialog extends TriggerDialog<NPCMatcher> {
	private static final long serialVersionUID = -8061888140387296525L;

	private final JPanel topComponent;

	private final JLabel trainerIcon;

	private final JTextField nameTextField;

	private final JComboBox<ImageIcon> spriteComboBox;
	private final JComboBox<Direction> directionComboBox;

    private final JTextField pathTextField;
	private final JTextArea conditionTextField;

	private final List<NPCInteractionMatcher> interactions;
	private final JButton addInteractionButton;
	
	private final MapMaker mapMaker;

	public NPCEntityDialog(NPCMatcher npcMatcher, MapMaker givenMapMaker) {
		super("NPC Editor");

		ActionListener spriteActionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int index = Integer.parseInt(((ImageIcon) spriteComboBox.getSelectedItem()).getDescription());
				int direction = directionComboBox.getSelectedIndex();

				BufferedImage image = mapMaker.getTileFromSet(TileType.TRAINER, 12 * index + 1 + direction);
				image = image.getSubimage(0, 0, Math.min(image.getWidth(), 50), Math.min(image.getHeight(), 50));

				ImageIcon icon = new ImageIcon(image);
				trainerIcon.setIcon(icon);
			}
		};

		mapMaker = givenMapMaker;

		trainerIcon = GUIUtils.createLabel(StringUtils.empty());
		spriteComboBox = GUIUtils.createComboBox(getTrainerSprites(), spriteActionListener);
		directionComboBox = GUIUtils.createComboBox(Direction.values(), spriteActionListener);

		nameTextField = new JTextField();
		pathTextField = new JTextField("w");
		conditionTextField = new JTextArea();

		interactions = new ArrayList<>();
		addInteractionButton = GUIUtils.createButton(
				"Add Interaction",
				event -> {
					interactions.add(null);
					render();
				}
		);

		spriteComboBox.setSelectedIndex(1);
		directionComboBox.setSelectedItem(Direction.DOWN);

		JPanel tippityTopComponent =
				GUIUtils.createHorizontalLayoutComponent(
						trainerIcon,
						spriteComboBox,
						directionComboBox
				);

		this.topComponent =
				GUIUtils.createVerticalLayoutComponent(
						tippityTopComponent,
						GUIUtils.createTextFieldComponent("Name", nameTextField),
						GUIUtils.createTextFieldComponent("Path", pathTextField),
						GUIUtils.createTextAreaComponent("Condition", conditionTextField)
				);

		this.load(npcMatcher);
	}

	@Override
	protected void renderDialog() {
		removeAll();

		List<JComponent> interactionComponents = new ArrayList<>();
		for (int i = 0; i < interactions.size(); i++) {
			final int index = i;
			JButton interactionButton =
					GUIUtils.createButton(
							"Interaction",
							event -> {
								NPCInteractionMatcher matcher = new NPCInteractionDialog(interactions.get(index)).getMatcher(mapMaker);
								if (matcher != null) {
									interactions.set(index, matcher);
								}
							}
					);

			interactionComponents.add(interactionButton);
		}

		JPanel interactionComponent = GUIUtils.createHorizontalLayoutComponent(interactionComponents.toArray(new JComponent[0]));
		GUIUtils.setVerticalLayout(this, topComponent, interactionComponent, addInteractionButton);
	}
	
	private ImageIcon[] getTrainerSprites() {
		List<ImageIcon> icons = new ArrayList<>();

		for (int curr = 0; mapMaker.getTileFromSet(TileType.TRAINER, 12*curr + 4) != null; curr++) {
			icons.add(new ImageIcon(mapMaker.getTileFromSet(TileType.TRAINER, 12*curr + 4), "" + curr));
		}

		return icons.toArray(new ImageIcon[0]);
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
