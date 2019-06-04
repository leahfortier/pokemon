package mapMaker.dialogs;

import util.GuiUtils;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Frame;

class DialogOptions extends JDialog {

    private final JPanel panel;
    private final JPanel buttonsComponent;

    private boolean saved;

    DialogOptions(String name, JComponent parent) {
        super((Frame)null, true);

        panel = new JPanel();
        GuiUtils.setStyle(panel);

        JButton saveButton = GuiUtils.createButton("Save or Whatever", event -> {
            this.saved = true;
            finish();
        });

        JButton cancelButton = GuiUtils.createButton("Cancel", event -> finish());

        this.buttonsComponent = GuiUtils.createHorizontalLayoutComponent(saveButton, cancelButton);

        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setLocationRelativeTo(parent);
        this.setTitle(name);
        this.setBackground(Color.WHITE);
    }

    public void render(JPanel dialogPanel) {
        this.remove(this.panel);
        GuiUtils.setVerticalLayout(this.panel, dialogPanel, this.buttonsComponent);
        this.add(this.panel);
        this.pack();
    }

    private void finish() {
        this.setVisible(false);
        this.dispose();
    }

    boolean isSaved() {
        return this.saved;
    }
}
