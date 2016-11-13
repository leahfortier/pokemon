package mapMaker.dialogs;

import map.triggers.TriggerType;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.EnumMap;
import java.util.Map;

public class ActionDialog extends TriggerDialog {

    private Map<ActionType, ActionData> map;
    private JComboBox<ActionType> boringActionComboBox;
    private JPanel actionPanel;

    private static abstract class ActionData {
        protected abstract JPanel getPanel();
    }

    private static class TriggerActionData extends ActionData {
        private String triggerType;
        public String triggerContents;

        protected JPanel getPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

            JComboBox<TriggerType> triggerTypeCombobBox = new JComboBox<>(TriggerType.values());
            panel.add(triggerTypeCombobBox);

            JTextField triggerContentsTextField = new JTextField();
            triggerContentsTextField.setColumns(10);
            panel.add(triggerContentsTextField);

            return panel;
        }
    }

    private static class BattleActionData extends ActionData {
        protected JPanel getPanel() {
            return new TrainerDataDialog();
        }
    }

    private static class ChoiceActionData extends ActionData {
        protected JPanel getPanel() {
            return new JPanel();
        }
    }

    private static class NotYoMamaActionData extends ActionData {
        protected JPanel getPanel() {
            JPanel panel = new JPanel();

            JTextField textField = new JTextField();
            textField.setColumns(10);
            panel.add(textField);

            return panel;
        }
    }

    private enum ActionType {
        TRIGGER(TriggerActionData::new),
        BATTLE(BattleActionData::new),
        CHOICE(ChoiceActionData::new),
        UPDATE(NotYoMamaActionData::new),
        GROUP_TRIGGER(NotYoMamaActionData::new),
        GLOBAL(NotYoMamaActionData::new);

        private final ActionDataCreator actionDataCreator;

        ActionType(ActionDataCreator actionDataCreator) {
            this.actionDataCreator = actionDataCreator;
        }

        private interface ActionDataCreator {
            ActionData createData();
        }
    }

    public ActionDialog() {
        this.map = new EnumMap<>(ActionType.class);
        for (ActionType action : ActionType.values()) {
            this.map.put(action, action.actionDataCreator.createData());
        }

        boringActionComboBox = new JComboBox<>(ActionType.values());
        boringActionComboBox.addActionListener(event -> {
            if (actionPanel != null) {
                panel.remove(actionPanel);
            }

            ActionType selectedAction = (ActionType)boringActionComboBox.getSelectedItem();
            actionPanel = map.get(selectedAction).getPanel();

           render();
        });

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        panel.add(boringActionComboBox);

        add(panel);
        render();
    }

    private JPanel panel;
    private void render() {
        if (actionPanel != null) {
            panel.add(actionPanel);
        }
        this.setPanelSize();
        revalidate();
    }
}
