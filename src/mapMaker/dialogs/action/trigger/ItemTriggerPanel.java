package mapMaker.dialogs.action.trigger;

import item.ItemNamesies;
import util.ColorDocumentListener;
import util.ColorDocumentListener.ColorCondition;

import javax.swing.JComponent;

public class ItemTriggerPanel extends StringTriggerPanel {

    ItemTriggerPanel(String label) {
        super(label);

        ColorCondition colorCondition = () -> ItemNamesies.tryValueOf(textField.getText().trim()) != null;
        textField.getDocument().addDocumentListener(new ColorDocumentListener(colorCondition) {
            @Override
            protected JComponent colorComponent() {
                return textField;
            }
        });
    }

    @Override
    protected void load(String triggerContents) {
        this.textField.setText(ItemNamesies.valueOf(triggerContents).getName());
    }

    @Override
    protected String getTriggerContents() {
        return ItemNamesies.getValueOf(this.textField.getText()).name();
    }
}
