package mapMaker.dialogs.wildbattle;

import main.Global;
import mapMaker.dialogs.TriggerDialog;
import pattern.map.WildBattleAreaMatcher;
import pattern.map.WildBattleMatcher;
import util.GuiUtils;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import java.util.ArrayList;
import java.util.List;

public class WildBattleAreaDialog extends TriggerDialog<WildBattleAreaMatcher> {
    private final JComponent panel;
    private final JTextField nameTextField;

    private JComboBox<String> encountersComboBox;
    private List<WildBattleMatcher> wildBattleTriggers;

    public WildBattleAreaDialog(WildBattleAreaMatcher wildBattleAreaMatcher) {
        super("Edit Wild Battle Area Dialog");

        wildBattleTriggers = new ArrayList<>();

        nameTextField = GuiUtils.createTextField();
        encountersComboBox = GuiUtils.createComboBox(new String[0]);

        JButton createButton = GuiUtils.createButton(
                "Create New",
                event -> {
                    WildBattleMatcher oldMatcher = null;
                    if (!this.wildBattleTriggers.isEmpty()) {
                        oldMatcher = wildBattleTriggers.get(0).getJsonCopy(WildBattleMatcher.class);
                        oldMatcher.setName("Wild Trigger Matcher " + wildBattleTriggers.size());
                        oldMatcher.setCondition(null, null);
                    }

                    WildBattleMatcher matcher = editWildBattleTrigger(oldMatcher);
                    if (matcher == null) {
                        return;
                    }

                    this.addWildBattleTrigger(matcher);
                    encountersComboBox.setSelectedItem(matcher.getName());
                }
        );

        JButton editButton = GuiUtils.createButton(
                "Edit",
                event -> {
                    WildBattleMatcher oldMatcher = this.getSelectedTriggerMatcher();
                    WildBattleMatcher newMatcher = editWildBattleTrigger(oldMatcher);
                    if (newMatcher == null) {
                        return;
                    }

                    if (oldMatcher != null) {
                        wildBattleTriggers.remove(oldMatcher);
                    }

                    addWildBattleTrigger(newMatcher);
                }
        );

        JComponent encountersPanel = GuiUtils.createHorizontalLayoutComponent(
                GuiUtils.createComboBoxComponent("Encounters", encountersComboBox),
                editButton
        );

        panel = GuiUtils.createVerticalLayoutComponent(
                GuiUtils.createTextFieldComponent("Name", nameTextField),
                encountersPanel,
                createButton
        );

        load(wildBattleAreaMatcher);
    }

    private WildBattleMatcher getSelectedTriggerMatcher() {
        String wildBattleName = (String)encountersComboBox.getSelectedItem();
        for (WildBattleMatcher matcher : wildBattleTriggers) {
            if (matcher.getName().equals(wildBattleName)) {
                return matcher;
            }
        }

        Global.error("No wild battle trigger found with name " + wildBattleName);
        return null;
    }

    private WildBattleMatcher editWildBattleTrigger(WildBattleMatcher wildBattleMatcher) {
        return new WildBattleTriggerEditDialog(wildBattleMatcher, wildBattleTriggers.size()).getMatcher(this);
    }

    private void addWildBattleTrigger(WildBattleMatcher newMatcher) {
        wildBattleTriggers.add(newMatcher);

        String[] encounterNames = wildBattleTriggers
                .stream()
                .map(WildBattleMatcher::getName)
                .toArray(String[]::new);

        encountersComboBox.setModel(new DefaultComboBoxModel<>(encounterNames));

        render();
    }

    @Override
    protected void renderDialog() {
        GuiUtils.setVerticalLayout(this, panel);
    }

    @Override
    protected WildBattleAreaMatcher getMatcher() {
        return new WildBattleAreaMatcher(
                nameTextField.getText(),
                wildBattleTriggers
        );
    }

    private void load(WildBattleAreaMatcher matcher) {
        if (matcher == null) {
            return;
        }

        nameTextField.setText(matcher.getBasicName());
        matcher.getWildBattles().forEach(this::addWildBattleTrigger);
    }
}
