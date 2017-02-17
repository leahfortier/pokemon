package mapMaker.dialogs;

import main.Global;
import pattern.map.FishingMatcher;
import util.GUIUtils;

import javax.swing.JButton;
import javax.swing.JComboBox;
import java.util.ArrayList;
import java.util.List;

public class FishingTriggerOptionsDialog extends TriggerDialog<FishingMatcher> {
    private static final long serialVersionUID = -7378035463487486331L;

    private JComboBox<String> comboBox;

    private JButton createButton;
    private JButton editButton;
    private List<FishingMatcher> fishingTriggers;

    public FishingTriggerOptionsDialog(List<FishingMatcher> fishingMatchers) {
        super("Fishing Trigger Options");

        this.fishingTriggers = new ArrayList<>();

        comboBox = GUIUtils.createComboBox(new String[0], null);

        createButton = GUIUtils.createButton(
                "Create New",
                event -> {
                    FishingMatcher matcher = editWildBattleTrigger(null);
                    if (matcher == null) {
                        return;
                    }

                    this.addWildBattleTrigger(matcher);
                    comboBox.setSelectedItem(matcher.getBasicName());
                }
        );

        editButton = GUIUtils.createButton(
                "Edit",
                event -> {
                    FishingMatcher oldMatcher = this.getSelectedTriggerMatcher();
                    FishingMatcher newMatcher = editWildBattleTrigger(oldMatcher);
                    if (newMatcher == null) {
                        return;
                    }

                    if (oldMatcher != null) {
                        fishingTriggers.remove(oldMatcher);
                    }

                    addWildBattleTrigger(newMatcher);
                }
        );
        editButton.setEnabled(false);

        this.load(fishingMatchers);
    }

    private FishingMatcher getSelectedTriggerMatcher() {
        String wildBattleName = (String)comboBox.getSelectedItem();
        for (FishingMatcher matcher : fishingTriggers) {
            if (wildBattleName.equals(matcher.getBasicName())) {
                return matcher;
            }
        }

        Global.error("No wild battle trigger found with name " + wildBattleName);
        return null;
    }

    private FishingMatcher editWildBattleTrigger(FishingMatcher wildBattleMatcher) {
        return new FishingTriggerEditDialog(wildBattleMatcher, fishingTriggers.size()).getMatcher(this);
    }

    @Override
    protected void renderDialog() {
        comboBox.removeAllItems();
        this.fishingTriggers.forEach(matcher -> comboBox.addItem(matcher.getBasicName()));

        editButton.setEnabled(!fishingTriggers.isEmpty());

        GUIUtils.setVerticalLayout(
                this,
                comboBox,
                GUIUtils.createHorizontalLayoutComponent(createButton, editButton)
        );
    }


    private void addWildBattleTrigger(FishingMatcher newMatcher) {
        fishingTriggers.add(newMatcher);
        render();
    }

    @Override
    protected FishingMatcher getMatcher() {
        if (this.fishingTriggers.isEmpty()) {
            return null;
        }

        return fishingTriggers.get(this.comboBox.getSelectedIndex());
    }

    private void load(List<FishingMatcher> matchers) {
        if (matchers == null) {
            return;
        }

        matchers.forEach(this::addWildBattleTrigger);
    }
}
