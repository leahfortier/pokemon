package mapMaker.dialogs;

import item.Item;
import mapMaker.MapMaker;
import mapMaker.model.TileModel.TileType;
import namesies.ItemNamesies;
import pattern.AreaDataMatcher.ItemMatcher;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;

public class ItemEntityDialog extends TriggerDialog {
	private static final long serialVersionUID = 7469923865936465388L;

	private JTextField itemTextField;
	private JLabel itemImageLabel;
	private MapMaker mapMaker;
	private JTextArea conditionTextArea;
	
	public ItemEntityDialog (MapMaker givenMapMaker) {
		mapMaker = givenMapMaker;
		
		JLabel itemLabel = new JLabel("Item");
		
		itemTextField = new JTextField();
		itemTextField.setColumns(10);
		
		itemTextField.getDocument().addDocumentListener(new DocumentListener() {
			  public void changedUpdate(DocumentEvent e) {
				  checkItem();
			  }

			  public void removeUpdate(DocumentEvent e) {
				  checkItem();
			  }

			  public void insertUpdate(DocumentEvent e) {
				  checkItem();
			  }

			  private void checkItem() {
				  ItemNamesies itemName = getItemName();
				  if (itemName == null) {
					  itemImageLabel.setIcon(null);
					  itemTextField.setBackground(new Color(0xFF9494));
				  } else {
					  int index = Item.getItem(itemName).getImageIndex();
					  itemImageLabel.setIcon(new ImageIcon(mapMaker.getTileFromSet(TileType.ITEM, index)));
					  itemTextField.setBackground(new Color(0x90EE90));
				  }
			  }
		});
		
		itemImageLabel = new JLabel();
		Border border = BorderFactory.createLineBorder(Color.DARK_GRAY, 1);
		itemImageLabel.setBorder(border);
		itemImageLabel.setHorizontalAlignment(JLabel.CENTER);
		itemImageLabel.setVerticalAlignment(JLabel.CENTER);
        
		JLabel conditionLabel = new JLabel("Condition");

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		conditionTextArea = new JTextArea();
		scrollPane.setViewportView(conditionTextArea);

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(itemImageLabel, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(itemLabel)
									.addGap(12)
									.addComponent(itemTextField, GroupLayout.PREFERRED_SIZE, 143, GroupLayout.PREFERRED_SIZE))
								.addComponent(conditionLabel)))
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 259, GroupLayout.PREFERRED_SIZE)))
		);
		
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(itemImageLabel, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(6)
									.addComponent(itemLabel))
								.addComponent(itemTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(20)
							.addComponent(conditionLabel)))
					.addGap(12)
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE))
		);
		
		setLayout(groupLayout);
	}

	public void setItem(ItemMatcher item) {
		ItemNamesies itemName = item.getItem();
		itemTextField.setText(itemName.getName());
//		conditionTextArea.setText(item.placedCondition.replace("&"," & ").replace("|", " | "));
		
		int index = Item.getItem(itemName).getImageIndex();
		itemImageLabel.setIcon(new ImageIcon(mapMaker.getTileFromSet(TileType.ITEM, index)));
	}

	public ItemNamesies getItemName() {
		return ItemNamesies.tryValueOf(itemTextField.getText());
	}
	
	public ItemMatcher getItem(String name) {
		ItemNamesies itemName = this.getItemName();
		if (itemName == null) {
			return null;
		}

		// TODO: Condition: "condition: " + conditionTextArea.getText().trim().replace(" ", ""),
		return new ItemMatcher(name, itemName);
	}
}
