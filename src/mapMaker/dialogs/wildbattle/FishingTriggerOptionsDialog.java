package mapMaker.dialogs.wildbattle;

import main.Global;
import mapMaker.dialogs.TriggerDialog;
import pattern.map.FishingMatcher;
import util.GuiUtils;

import javax.swing.JButton;
import javax.swing.JComboBox;
import java.util.ArrayList;
import java.util.List;

public class FishingTriggerOptionsDialog extends TriggerDialog<FishingMatcher> {
    private final JComboBox<String> comboBox;

    private final JButton createButton;
    private final JButton editButton;
    private final List<FishingMatcher> fishingTriggers;

    public FishingTriggerOptionsDialog(List<FishingMatcher> fishingMatchers) {
        super("Fishing Trigger Options");

        this.fishingTriggers = new ArrayList<>();

        comboBox = GuiUtils.createComboBox(new String[0], null);

        createButton = GuiUtils.createButton(
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

        editButton = GuiUtils.createButton(
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

        GuiUtils.setVerticalLayout(
                this,
                comboBox,
                GuiUtils.createHorizontalLayoutComponent(createButton, editButton)
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
