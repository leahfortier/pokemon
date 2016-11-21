package util;

import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;

public abstract class ColorDocumentListener implements DocumentListener {
    private ColorCondition colorCondition;

    ColorDocumentListener(ColorCondition colorCondition) {
        this.colorCondition = colorCondition;
    }

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

    protected abstract JComponent colorComponent();

    private void valueChanged() {
        colorCondition.additionalValueChanged();

        if (colorCondition.greenCondition()) {
            colorComponent().setBackground(new Color(0x90EE90));
        }
        else {
            colorComponent().setBackground(new Color(0xFF9494));
        }
    }

    public interface ColorCondition {
        boolean greenCondition();
        default void additionalValueChanged() {}
    }
}
