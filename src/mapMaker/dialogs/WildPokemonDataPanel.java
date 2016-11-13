package mapMaker.dialogs;

import namesies.PokemonNamesies;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;
import java.awt.Color;
import java.text.NumberFormat;

public class WildPokemonDataPanel extends TriggerDialog {
	
	private static final long serialVersionUID = -7408589859784929623L;

	public JTextField pokemonTextField;
	public JFormattedTextField probabilityFormattedTextField;
	public JFormattedTextField lowLevelFormattedTextField;
	public JFormattedTextField highLevelFormattedTextField;
	
	private WildBattleTriggerEditDialog wildBattleEditDialog;
	int index;
	
	WildPokemonDataPanel(WildBattleTriggerEditDialog givenWildBattleEditDialog, int givenIndex) {
		
		wildBattleEditDialog = givenWildBattleEditDialog;
		index = givenIndex;

		JCheckBox selectedCheckBox = new JCheckBox("");
		selectedCheckBox.addActionListener(event -> wildBattleEditDialog.setSelected(index));
		
		pokemonTextField = new JTextField();
		pokemonTextField.setColumns(10);
		pokemonTextField.getDocument().addDocumentListener(new DocumentListener() {
			public void removeUpdate(DocumentEvent event) { valueChanged(); }
			public void insertUpdate(DocumentEvent event) { valueChanged(); }
			public void changedUpdate(DocumentEvent event) {}
			public void valueChanged() {
				PokemonNamesies namesies = PokemonNamesies.tryValueOf(pokemonTextField.getText().trim());
				if (namesies == null) {
					pokemonTextField.setBackground(new Color(0xFF9494));
				}
				else {
					pokemonTextField.setBackground(new Color(0x90EE90));
				}
			}
		});
		
		NumberFormat format = NumberFormat.getNumberInstance();
		NumberFormatter formatter = new NumberFormatter(format);
	    formatter.setValueClass(Integer.class);
	    formatter.setMinimum(1);
	    formatter.setMaximum(100);
		probabilityFormattedTextField = new JFormattedTextField(formatter);
		probabilityFormattedTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		probabilityFormattedTextField.setValue(100);

		lowLevelFormattedTextField = new JFormattedTextField(formatter);
		lowLevelFormattedTextField.setValue(1);
		lowLevelFormattedTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		
		highLevelFormattedTextField = new JFormattedTextField(formatter);
		highLevelFormattedTextField.setValue(100);
		highLevelFormattedTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addComponent(selectedCheckBox)
					.addComponent(pokemonTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(12)
					.addComponent(probabilityFormattedTextField, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
					.addGap(12)
					.addComponent(lowLevelFormattedTextField, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
					.addGap(12)
					.addComponent(highLevelFormattedTextField, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(9)
					.addComponent(selectedCheckBox))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addComponent(pokemonTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addComponent(probabilityFormattedTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addComponent(lowLevelFormattedTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addComponent(highLevelFormattedTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
		);
		setLayout(groupLayout);
	}
}
