package mapMaker.dialogs;

import util.GUIUtils;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DialogOptions extends JDialog implements ActionListener {

    private final JPanel panel;

    private final JButton saveButton;
    private final JPanel buttonsComponent;

    private boolean saved;

    public DialogOptions(String name, JComponent parent) {
        super((Frame)null, true);

        panel = new JPanel();

        this.saveButton = GUIUtils.createButton("Save or Whatever", this);
        JButton cancelButton = GUIUtils.createButton("Cancel", this);

        this.buttonsComponent = GUIUtils.getHorizontalLayoutComponents(saveButton, cancelButton);

        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(parent);
        this.setTitle(name);
    }

    public void render(JPanel dialogPanel) {
        this.remove(this.panel);
        GUIUtils.setVerticalLayout(this.panel, dialogPanel, this.buttonsComponent);
        this.add(this.panel);
        this.pack();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == this.saveButton) {
            this.saved = true;
        }

        this.setVisible(false);
        this.dispose();
    }

    public boolean isSaved() {
        return this.saved;
    }
}
