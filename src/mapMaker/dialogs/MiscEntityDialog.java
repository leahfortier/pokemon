package mapMaker.dialogs;

import main.Global;
import mapMaker.MapMaker;
import pattern.action.MiscEntityInteractionMatcher;
import pattern.map.MiscEntityMatcher;
import util.GuiUtils;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.ArrayList;
import java.util.List;

public class MiscEntityDialog extends TriggerDialog<MiscEntityMatcher> {

    private final JPanel topComponent;

    private final JTextField nameTextField;
    private final ConditionPanel conditionPanel;

    private final List<MiscEntityInteractionMatcher> interactions;
    private final JButton addInteractionButton;

    private final MapMaker mapMaker;

    public MiscEntityDialog(MiscEntityMatcher matcher, MapMaker givenMapMaker) {
        super("Misc Trigger Editor");

        mapMaker = givenMapMaker;

        this.nameTextField = GuiUtils.createTextField();
        this.conditionPanel = new ConditionPanel();

        interactions = new ArrayList<>();
        addInteractionButton = GuiUtils.createButton(
                "Add Interaction",
                event -> {
                    interactions.add(null);
                    render();
                }
        );

        this.topComponent = GuiUtils.createVerticalLayoutComponent(
                GuiUtils.createTextFieldComponent("Name", nameTextField),
                conditionPanel
        );

        this.load(matcher);
    }

    @Override
    public void renderDialog() {
        removeAll();

        List<JComponent> interactionComponents = new ArrayList<>();
        for (int i = 0; i < interactions.size(); i++) {
            final int index = i;
            MiscEntityInteractionMatcher matcher = interactions.get(index);

            JButton interactionButton =
                    GuiUtils.createButton(
                            matcher == null ? "Empty" : matcher.getName(),
                            event -> {
                                MiscEntityInteractionMatcher newMatcher = new MiscEntityInteractionDialog(matcher, index).getMatcher(mapMaker);
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

    @Override
    protected MiscEntityMatcher getMatcher() {
        if (interactions.size() == 0) {
            Global.info("Need at least one interaction for a valid misc entity.");
            return null;
        }

        return new MiscEntityMatcher(
                this.getNameField(nameTextField),
                conditionPanel.getConditionName(),
                conditionPanel.getConditionSet(),
                interactions
        );
    }

    private void load(MiscEntityMatcher matcher) {
        if (matcher == null) {
            return;
        }

        nameTextField.setText(matcher.getBasicName());
        conditionPanel.load(matcher);
        interactions.addAll(matcher.getInteractionMatcherList());
    }
}
