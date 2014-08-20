package mapMaker.dialogs;

import item.Item;

import java.awt.Color;

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

import map.entity.ItemEntityData;
import mapMaker.MapMaker;

public class ItemEntityDialog extends JPanel {

	private static final long serialVersionUID = 7469923865936465388L;

	private JTextField itemTextField;
	private JLabel itemImageLabel;
	private MapMaker mapMaker;
	private JScrollPane scrollPane;
	private JTextArea conditionTextArea;
	
	public ItemEntityDialog (MapMaker givenMapMaker) {
		
		mapMaker = givenMapMaker;
		
		JLabel itemLabel = new JLabel("Item");
		
		itemTextField = new JTextField();
		itemTextField.setColumns(10);
		
		itemTextField.getDocument().addDocumentListener(new DocumentListener() {
			  public void changedUpdate(DocumentEvent e) { checkItem(); }
			  public void removeUpdate(DocumentEvent e) { checkItem(); }
			  public void insertUpdate(DocumentEvent e) { checkItem(); }
			  public void checkItem() {
				  String itemName = itemTextField.getText().replaceAll("\\\\u00e9", "\u00e9").replaceAll("\\\\u2640", "\u2640").replaceAll("\\\\u2642", "\u2642");
				  itemName = convertToProperCase(itemName);
				  if(!Item.exists(itemName)) {
					  itemImageLabel.setIcon(null);
					  itemTextField.setBackground(new Color(0xFF9494));
					  return;
				  }
				  int index = Item.getItem(itemName).getIndex();
				  itemImageLabel.setIcon(new ImageIcon(mapMaker.getTileFromSet("Item", index)));
				  itemTextField.setBackground(new Color(0x90EE90));
			  }
		});
		
		itemImageLabel = new JLabel();
		Border border = BorderFactory.createLineBorder(Color.darkGray, 1);
		itemImageLabel.setBorder(border);
		itemImageLabel.setHorizontalAlignment(JLabel.CENTER);
		itemImageLabel.setVerticalAlignment(JLabel.CENTER);
        

		JLabel conditionLabel = new JLabel("Condition");
		
		scrollPane = new JScrollPane();
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
	
	public void setItem(ItemEntityData item) {
		
		itemTextField.setText(item.getItem().replace('_', ' '));
		conditionTextArea.setText(item.placedCondition.replace("&"," & ").replace("|"," | "));
		
		int index = Item.getItem(itemTextField.getText().replaceAll("\\\\u00e9", "\u00e9").replaceAll("\\\\u2640", "\u2640").replaceAll("\\\\u2642", "\u2642")).getIndex();
		itemImageLabel.setIcon(new ImageIcon(mapMaker.getTileFromSet("Item", index)));
	}
	
	public String getItemName() {
		String itemName = itemTextField.getText().replaceAll("\\\\u00e9", "\u00e9").replaceAll("\\\\u2640", "\u2640").replaceAll("\\\\u2642", "\u2642");
		itemName = convertToProperCase(itemName);
		return itemName;
	}
	
	public ItemEntityData getItem(String name) {
		
		String item = getItemName();
		
		if(!Item.exists(item))
			return null;
		
		return new ItemEntityData(
				name, 
				"condition: "+conditionTextArea.getText().trim().replace(" ", ""), 
				item.replace(' ', '_'), 
				-1, 
				-1);
	}
	
	public String convertToProperCase(String string) {
		StringBuilder s = new StringBuilder();
		string = string.trim();
		
		while(string.length() != 0) {
			s.append(string.substring(0, 1).toUpperCase());
			string = string.substring(1, string.length());
			
			if(string.length() == 0)
				break;
			int index = string.indexOf(' ');
			
			if(index == -1) {
				s.append(string.substring(0,string.length()));
				string = "";
			}
			else {
				s.append(string.substring(0,index) +" ");
				string = string.substring(index+1, string.length());
			}
			
		}
		return s.toString();
	}
}
