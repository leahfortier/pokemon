package mapMaker.dialogs.wildbattle;

import main.Global;
import mapMaker.dialogs.TriggerDialog;
import pattern.map.WildBattleAreaMatcher;
import util.GUIUtils;

import javax.swing.JButton;
import javax.swing.JComboBox;
import java.util.ArrayList;
import java.util.List;

public class WildBattleTriggerOptionsDialog extends TriggerDialog<WildBattleAreaMatcher> {
    private final JComboBox<String> comboBox;

    private final JButton createButton;
    private final JButton editButton;
    private final List<WildBattleAreaMatcher> wildBattleAreas;

    public WildBattleTriggerOptionsDialog(List<WildBattleAreaMatcher> wildBattleMatchers) {
        super("Wild Battle Trigger Options");

        this.wildBattleAreas = new ArrayList<>();

        comboBox = GUIUtils.createComboBox(new String[0], null);

        createButton = GUIUtils.createButton(
                "Create New",
                event -> {
                    WildBattleAreaMatcher matcher = editWildBattleArea(null);
                    if (matcher == null) {
                        return;
                    }

                    this.addWildBattleArea(matcher);
                    comboBox.setSelectedItem(matcher.getBasicName());
                }
        );

        editButton = GUIUtils.createButton(
                "Edit",
                event -> {
                    WildBattleAreaMatcher oldMatcher = this.getSelectedTriggerMatcher();
                    WildBattleAreaMatcher newMatcher = editWildBattleArea(oldMatcher);
                    if (newMatcher == null) {
                        return;
                    }

                    if (oldMatcher != null) {
                        wildBattleAreas.remove(oldMatcher);
                    }

                    addWildBattleArea(newMatcher);
                }
        );
        editButton.setEnabled(false);

        this.load(wildBattleMatchers);
    }

    private WildBattleAreaMatcher  getSelectedTriggerMatcher() {
        String wildBattleName = (String)comboBox.getSelectedItem();
        for (WildBattleAreaMatcher matcher : wildBattleAreas) {
            if (wildBattleName.equals(matcher.getBasicName())) {
                return matcher;
            }
        }

        Global.error("No wild battle trigger found with name " + wildBattleName);
        return null;
    }

    private WildBattleAreaMatcher editWildBattleArea(WildBattleAreaMatcher wildBattleMatcher) {
        return new WildBattleAreaDialog(wildBattleMatcher).getMatcher(this);
    }

    @Override
    protected void renderDialog() {
        comboBox.removeAllItems();
        this.wildBattleAreas.forEach(matcher -> comboBox.addItem(matcher.getBasicName()));

        editButton.setEnabled(!wildBattleAreas.isEmpty());

        GUIUtils.setVerticalLayout(
                this,
                comboBox,
                GUIUtils.createHorizontalLayoutComponent(createButton, editButton)
        );
    }


    private void addWildBattleArea(WildBattleAreaMatcher newMatcher) {
        wildBattleAreas.add(newMatcher);
        render();
    }

    @Override
    protected WildBattleAreaMatcher getMatcher() {
        if (this.wildBattleAreas.isEmpty()) {
            return null;
        }

        return wildBattleAreas.get(this.comboBox.getSelectedIndex());
    }

    private void load(List<WildBattleAreaMatcher> matchers) {
        if (matchers == null) {
            return;
        }

        matchers.forEach(this::addWildBattleArea);
    }
}
