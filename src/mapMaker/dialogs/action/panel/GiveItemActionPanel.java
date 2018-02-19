package mapMaker.dialogs.action.panel;

import item.ItemNamesies;
import mapMaker.dialogs.action.ActionPanel;
import mapMaker.dialogs.action.ActionType;
import pattern.action.ActionMatcher;
import pattern.action.ActionMatcher.GiveItemActionMatcher;
import util.ColorDocumentListener;
import util.ColorDocumentListener.ColorCondition;
import util.GUIUtils;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;

public class GiveItemActionPanel extends ActionPanel {
    private final JTextField itemField;
    private final JFormattedTextField quantityTextField;

    public GiveItemActionPanel() {
        super();

        itemField = new JTextField();
        ColorCondition colorCondition = () -> ItemNamesies.tryValueOf(itemField.getText().trim()) != null;
        itemField.getDocument().addDocumentListener(new ColorDocumentListener(colorCondition) {
            @Override
            protected JComponent colorComponent() {
                return itemField;
            }
        });

        quantityTextField = GUIUtils.createIntegerTextField(1, 1, 99);

        GUIUtils.setVerticalLayout(
                this,
                GUIUtils.createTextFieldComponent("Item Name", itemField),
                GUIUtils.createTextFieldComponent("Quantity", quantityTextField)
        );
    }

    @Override
    protected void load(ActionMatcher matcher) {
        GiveItemActionMatcher itemMatcher = (GiveItemActionMatcher)matcher;

        itemField.setText(itemMatcher.getItem().getName());
        quantityTextField.setValue(itemMatcher.getQuantity());
    }

    @Override
    public ActionMatcher getActionMatcher(ActionType actionType) {
        ItemNamesies item = ItemNamesies.getValueOf(itemField.getText().trim());
        int quantity = Integer.parseInt(quantityTextField.getValue().toString());

        return new GiveItemActionMatcher(item, quantity);
    }
}
