package mapMaker.dialogs;

import map.Direction;
import map.entity.movable.MovableEntity;
import mapMaker.MapMaker;
import mapMaker.model.TileModel.TileType;
import pattern.action.NPCInteractionMatcher;
import pattern.map.NPCMatcher;
import util.GuiUtils;
import util.string.StringUtils;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class NPCEntityDialog extends TriggerDialog<NPCMatcher> {
    private final JPanel topComponent;

    private final JLabel trainerIcon;

    private final JTextField nameTextField;

    private final JComboBox<ImageIcon> spriteComboBox;
    private final JComboBox<Direction> directionComboBox;

    private final JTextField pathTextField;
    private final ConditionPanel conditionPanel;

    private final List<NPCInteractionMatcher> interactions;
    private final JButton addInteractionButton;

    private final MapMaker mapMaker;

    public NPCEntityDialog(NPCMatcher npcMatcher, MapMaker givenMapMaker) {
        super("NPC Editor");

        ActionListener spriteActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                int index = Integer.parseInt(((ImageIcon)spriteComboBox.getSelectedItem()).getDescription());
                Direction direction = (Direction)directionComboBox.getSelectedItem();

                BufferedImage image = mapMaker.getTileFromSet(TileType.TRAINER, MovableEntity.getTrainerSpriteIndex(index, direction));
                image = image.getSubimage(0, 0, Math.min(image.getWidth(), 50), Math.min(image.getHeight(), 50));

                ImageIcon icon = new ImageIcon(image);
                trainerIcon.setIcon(icon);
            }
        };

        mapMaker = givenMapMaker;

        trainerIcon = GuiUtils.createLabel(StringUtils.empty());
        spriteComboBox = GuiUtils.createComboBox(getTrainerSprites(), spriteActionListener);
        directionComboBox = GuiUtils.createComboBox(Direction.values(), spriteActionListener);

        nameTextField = GuiUtils.createTextField();
        pathTextField = GuiUtils.createTextField("w");
        conditionPanel = new ConditionPanel();

        interactions = new ArrayList<>();
        addInteractionButton = GuiUtils.createButton(
                "Add Interaction",
                event -> {
                    interactions.add(null);
                    render();
                }
        );

        spriteComboBox.setSelectedIndex(1);
        directionComboBox.setSelectedItem(Direction.DOWN);

        JPanel tippityTopComponent =
                GuiUtils.createHorizontalLayoutComponent(
                        trainerIcon,
                        spriteComboBox,
                        directionComboBox
                );

        this.topComponent =
                GuiUtils.createVerticalLayoutComponent(
                        tippityTopComponent,
                        GuiUtils.createTextFieldComponent("Name", nameTextField),
                        GuiUtils.createTextFieldComponent("Path", pathTextField),
                        conditionPanel
                );

        this.load(npcMatcher);
    }

    @Override
    protected void renderDialog() {
        removeAll();

        List<JComponent> interactionComponents = new ArrayList<>();
        for (int i = 0; i < interactions.size(); i++) {
            final int index = i;
            NPCInteractionMatcher matcher = interactions.get(index);

            JButton interactionButton =
                    GuiUtils.createButton(
                            matcher == null ? "Empty" : matcher.getName(),
                            event -> {
                                NPCInteractionMatcher newMatcher = new NPCInteractionDialog(matcher, index).getMatcher(mapMaker);
                                if (newMatcher != null) {
                                    interactions.set(index, newMatcher);
                                    render();
                                }
                            }
                    );

            JButton deleteButton = GuiUtils.createButton(
                    "Delete",
                    event -> {
                        interactions.remove(index);
                        render();
                    }
            );

            interactionComponents.add(GuiUtils.createHorizontalLayoutComponent(interactionButton, deleteButton));
        }

        JPanel interactionComponent = GuiUtils.createVerticalLayoutComponent(interactionComponents.toArray(new JComponent[0]));
        GuiUtils.setVerticalLayout(this, topComponent, interactionComponent, addInteractionButton);
    }

    private ImageIcon[] getTrainerSprites() {
        List<ImageIcon> icons = new ArrayList<>();

        int spriteIndex = 0;
        while (true) {
            BufferedImage image = mapMaker.getTileFromSet(TileType.TRAINER, MovableEntity.getTrainerSpriteIndex(spriteIndex, Direction.DOWN));
            if (image == null) {
                break;
            }

            icons.add(new ImageIcon(image, spriteIndex + ""));
            spriteIndex++;
        }

        return icons.toArray(new ImageIcon[0]);
    }

    @Override
    protected NPCMatcher getMatcher() {
        return new NPCMatcher(
                getNameField(nameTextField),
                this.conditionPanel.getConditionName(),
                this.conditionPanel.getConditionSet(),
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
        pathTextField.setText(matcher.getPath());
        spriteComboBox.setSelectedIndex(matcher.getSpriteIndex());
        directionComboBox.setSelectedItem(matcher.getDirection());
        interactions.addAll(matcher.getInteractionMatcherList());
        conditionPanel.load(matcher);
    }
}
