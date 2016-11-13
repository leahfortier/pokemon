package mapMaker.dialogs;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class NPCInteractionDialog extends TriggerDialog {
    private JTextField interactionNameTextField;
    private JCheckBox walkToPlayerCheckBox;
    private ActionListPanel actionListPanel;

    public NPCInteractionDialog() {
        interactionNameTextField = new JTextField();
        walkToPlayerCheckBox = new JCheckBox("Walk to playa");
        this.actionListPanel = new ActionListPanel();

        render();
    }

    private void render() {
        removeAll();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(interactionNameTextField);
        panel.add(walkToPlayerCheckBox);
        panel.add(actionListPanel);
        add(panel);

        this.setPanelSize();
        revalidate();
    }
}
