package util;

import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;

public abstract class ColorDocumentListener implements DocumentListener {
    @Override
    public final void insertUpdate(DocumentEvent event) {
        valueChanged();
    }

    @Override
    public final void removeUpdate(DocumentEvent event) {
        valueChanged();
    }

    @Override
    public final void changedUpdate(DocumentEvent event) {
        valueChanged();
    }

    protected abstract boolean greenCondition();
    protected abstract JComponent colorComponent();

    protected void additionalValueChanged() {}

    private void valueChanged() {
        additionalValueChanged();

        if (greenCondition()) {
            colorComponent().setBackground(new Color(0x90EE90));
        }
        else {
            colorComponent().setBackground(new Color(0xFF9494));
        }
    }
}
