package mapMaker.dialogs;

import item.ItemNamesies;
import main.Global;
import pattern.map.ItemMatcher;
import util.ColorDocumentListener.ColorCondition;
import util.GUIUtils;
import util.file.FileIO;
import util.file.Folder;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;

public class ItemEntityDialog extends TriggerDialog<ItemMatcher> {
    private JTextField itemTextField;
    private JLabel itemImageLabel;

    public ItemEntityDialog(ItemMatcher itemMatcher) {
        super("Item Editor");

        itemTextField = GUIUtils.createColorConditionTextField(new ColorCondition() {
            @Override
            public boolean greenCondition() {
                return getItemName() != null;
            }

            @Override
            public void additionalValueChanged() {
                if (greenCondition()) {
                    itemImageLabel.setIcon(new ImageIcon(FileIO.readImage(Folder.ITEM_TILES + getItemName().getItem().getImageName())));
                } else {
                    itemImageLabel.setIcon(null);
                }
            }
        });

        itemImageLabel = new JLabel();
        Border border = BorderFactory.createLineBorder(Color.DARK_GRAY, 1);
        itemImageLabel.setBorder(border);
        itemImageLabel.setHorizontalAlignment(JLabel.CENTER);
        itemImageLabel.setVerticalAlignment(JLabel.CENTER);
        itemImageLabel.setMinimumSize(new Dimension(3*Global.TILE_SIZE/2, 3*Global.TILE_SIZE/2));

        JTextArea conditionTextArea = new JTextArea();
        JPanel itemImageAndNameComponent = GUIUtils.createHorizontalLayoutComponent(
                itemImageLabel,
                GUIUtils.createTextFieldComponent("Item Name", itemTextField)
        );

        // TODO: Not currently saving the condition I believe
        GUIUtils.setVerticalLayout(
                this,
                itemImageAndNameComponent,
                GUIUtils.createTextAreaComponent("Condition", conditionTextArea)
        );

        this.load(itemMatcher);
    }

    private ItemNamesies getItemName() {
        return ItemNamesies.tryValueOf(itemTextField.getText().trim());
    }

    @Override
    protected ItemMatcher getMatcher() {
        final ItemNamesies itemNamesies = this.getItemName();
        if (itemNamesies == null) {
            return null;
        }

        return new ItemMatcher(itemNamesies);
    }

    private void load(ItemMatcher matcher) {
        if (matcher == null) {
            return;
        }

        ItemNamesies itemName = matcher.getItem();
        itemTextField.setText(itemName.getName());

        itemImageLabel.setIcon(new ImageIcon(FileIO.readImage(Folder.ITEM_TILES + itemName.getItem().getImageName())));
    }
}
