package mapMaker.dialogs.action.panel;

import item.ItemNamesies;
import mapMaker.dialogs.action.ActionPanel;
import pattern.action.ActionMatcher.GiveItemActionMatcher;
import util.ColorDocumentListener;
import util.ColorDocumentListener.ColorCondition;
import util.GUIUtils;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;

public class GiveItemActionPanel extends ActionPanel<GiveItemActionMatcher> {
    private final JTextField itemField;
    private final JFormattedTextField quantityTextField;

    public GiveItemActionPanel() {
        itemField = GUIUtils.createTextField();
        quantityTextField = GUIUtils.createIntegerTextField(1, 1, 99);

        ColorCondition colorCondition = () -> ItemNamesies.tryValueOf(itemField.getText().trim()) != null;
        itemField.getDocument().addDocumentListener(new ColorDocumentListener(colorCondition) {
            @Override
            protected JComponent colorComponent() {
                return itemField;
            }
        });

        GUIUtils.setVerticalLayout(
                this,
                GUIUtils.createTextFieldComponent("Item Name", itemField),
                GUIUtils.createTextFieldComponent("Quantity", quantityTextField)
        );
    }

    @Override
    protected void load(GiveItemActionMatcher matcher) {
        itemField.setText(matcher.getItem().getName());
        quantityTextField.setValue(matcher.getQuantity());
    }

    @Override
    public GiveItemActionMatcher getActionMatcher() {
        ItemNamesies item = ItemNamesies.getValueOf(itemField.getText().trim());
        int quantity = Integer.parseInt(quantityTextField.getValue().toString());

        return new GiveItemActionMatcher(item, quantity);
    }
}
