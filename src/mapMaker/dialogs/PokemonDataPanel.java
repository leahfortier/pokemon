package mapMaker.dialogs;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;

import pokemon.PokemonInfo;
import battle.Attack;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

public class PokemonDataPanel extends JPanel {
	
	private static final long serialVersionUID = 2679616277402077123L;
	
	public JTextField nameTextField;
	public JTextField moveTextField;
	public JCheckBox shinyCheckBox;
	public JComboBox<String> moveComboBox;
	public JCheckBox moveCheckBox;
	public JFormattedTextField levelFormattedTextField;
	
	public String[] customMoves = new String[4];
	private JCheckBox selectedCheckBox;
	
	private TrainerDataDialog trainerDialog;
	public int index;
	
	public PokemonDataPanel(TrainerDataDialog givenTrainerDialog, int givenIndex) {
		
		trainerDialog = givenTrainerDialog;
		index = givenIndex;
		
		for (int currMove = 0; currMove < customMoves.length; ++currMove) {
			customMoves[currMove] = "";
		}
		
		nameTextField = new JTextField();
		nameTextField.getDocument().addDocumentListener(new DocumentListener() 
		{
			public void removeUpdate(DocumentEvent e) {valueChanged();}
			public void insertUpdate(DocumentEvent e) {valueChanged();}
			public void changedUpdate(DocumentEvent e) {}
			
			public void valueChanged() 
			{
				String pokemonName = nameTextField.getText().trim();
				if (pokemonName.length() < 2) 
				{
					nameTextField.setBackground(new Color(0xFF9494));
					return;
				}
				
				pokemonName = Character.toUpperCase(pokemonName.charAt(0)) + pokemonName.substring(1).toLowerCase();
				
				if (!PokemonInfo.isPokemonName(pokemonName)) 
				{
					nameTextField.setBackground(new Color(0xFF9494));
				}
				else 
				{
					nameTextField.setBackground(new Color(0x90EE90));
				}
			}
		});
		
		nameTextField.setColumns(10);
		
		shinyCheckBox = new JCheckBox("");
		
		moveComboBox = new JComboBox<String>();
		moveComboBox.setModel(new DefaultComboBoxModel<String>(new String[] {"Move 1", "Move 2", "Move 3", "Move 4"}));
		moveComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				moveTextField.setText(customMoves[moveComboBox.getSelectedIndex()]);
			}
		});
		moveComboBox.setEnabled(false);
		
		moveCheckBox = new JCheckBox("");
		moveCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				moveComboBox.setEnabled(moveCheckBox.isSelected());
				moveTextField.setEnabled(moveCheckBox.isSelected());
			}
		});
		
		moveTextField = new JTextField();
		moveTextField.getDocument().addDocumentListener(new DocumentListener() {
			public void removeUpdate(DocumentEvent e) {valueChanged();}
			public void insertUpdate(DocumentEvent e) {valueChanged();}
			public void changedUpdate(DocumentEvent e) {}
			public void valueChanged() {
				customMoves[moveComboBox.getSelectedIndex()] = moveTextField.getText().trim();
				if (!Attack.isAttack(customMoves[moveComboBox.getSelectedIndex()])) {
					moveTextField.setBackground(new Color(0xFF9494));
				}
				else {
					moveTextField.setBackground(new Color(0x90EE90));
				}
			}
		});
		moveTextField.setColumns(10);
		moveTextField.setEnabled(false);
		
		NumberFormat format = NumberFormat.getNumberInstance();
		NumberFormatter formatter = new NumberFormatter(format);
	    formatter.setValueClass(Integer.class);
	    formatter.setMinimum(1);
	    formatter.setMaximum(100);
		levelFormattedTextField = new JFormattedTextField(formatter);
		levelFormattedTextField.setText("1");
		
		selectedCheckBox = new JCheckBox("");
		
		selectedCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				trainerDialog.setSelected(index);
			}
		});
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(selectedCheckBox)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(26)
							.addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addGap(12)
					.addComponent(levelFormattedTextField, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
					.addGap(12)
					.addComponent(shinyCheckBox)
					.addGap(12)
					.addComponent(moveCheckBox)
					.addComponent(moveComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(moveTextField, GroupLayout.PREFERRED_SIZE, 174, GroupLayout.PREFERRED_SIZE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(7)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(2)
							.addComponent(selectedCheckBox))
						.addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(7)
					.addComponent(levelFormattedTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(9)
					.addComponent(shinyCheckBox))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(9)
					.addComponent(moveCheckBox))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(8)
					.addComponent(moveComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addComponent(moveTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
		);
		
		setLayout(groupLayout);
	}
	
	public void setPokemon() 
	{
		// TODO: What is this?
	}
	
	public String getPokemonData () 
	{
		String pokemonName = nameTextField.getText().trim();
		
		if (pokemonName.length() < 2)
			return null;
		
		// TODO: I don't think this will work always -- try with something like Mr. Mime
		pokemonName = Character.toUpperCase(pokemonName.charAt(0)) + pokemonName.substring(1).toLowerCase();
		
		if (!PokemonInfo.isPokemonName(pokemonName)) 
		{
			return null;
		}
		
		String data = "pokemon: " + pokemonName + " " + (String)levelFormattedTextField.getText() + " " + (shinyCheckBox.isSelected() ? "Shiny" : "");
		
		if (moveCheckBox.isSelected()) 
		{
			boolean allValidMoves = true;
			String moves = "";
			
			for (int currMove = 0; currMove < customMoves.length && allValidMoves; ++currMove) 
			{
				String move = customMoves[currMove].length() == 0? "None": customMoves[currMove];
				allValidMoves |= Attack.isAttack(move);
				moves+= move +(currMove + 1 == customMoves.length?"*":", ");
			}
			
			if (allValidMoves) 
			{
				data += " Moves: " + moves;
			}
		}
		
		return data;
	}
}
