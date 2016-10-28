package mapMaker.dialogs;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;

import battle.Move;
import main.Global;
import pokemon.PokemonInfo;
import battle.Attack;
import util.StringUtils;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

class PokemonDataPanel extends JPanel {
	
	private static final long serialVersionUID = 2679616277402077123L;
	
	private JTextField nameTextField;
	private JTextField moveTextField;
	private JCheckBox shinyCheckBox;
	private JComboBox<String> moveComboBox;
	private JCheckBox moveCheckBox;
	private JFormattedTextField levelFormattedTextField;

	private String[] customMoves = new String[Move.MAX_MOVES]; // TODO: I think this should just be a list

	private TrainerDataDialog trainerDialog;
	public int index;
	
	PokemonDataPanel(TrainerDataDialog givenTrainerDialog, int givenIndex) {
		
		trainerDialog = givenTrainerDialog;
		index = givenIndex;

		for (int currMove = 0; currMove < customMoves.length; ++currMove) {
			customMoves[currMove] = "";
		}
		
		nameTextField = new JTextField();
		nameTextField.getDocument().addDocumentListener(new DocumentListener() {
			public void removeUpdate(DocumentEvent event) {
				valueChanged();
			}

			public void insertUpdate(DocumentEvent event) {
				valueChanged();
			}

			public void changedUpdate(DocumentEvent event) {}
			
			private void valueChanged() {
				String pokemonName = nameTextField.getText().trim();
				if (pokemonName.length() < 2) {
					nameTextField.setBackground(new Color(0xFF9494)); // TODO: What is this color -- it should be a constant if it's being used in multiple locations
					return;
				}

				// TODO: use util method
				pokemonName = Character.toUpperCase(pokemonName.charAt(0)) + pokemonName.substring(1).toLowerCase();
				
				if (!PokemonInfo.isPokemonName(pokemonName)) {
					nameTextField.setBackground(new Color(0xFF9494));
				}
				else {
					nameTextField.setBackground(new Color(0x90EE90));
				}
			}
		});
		
		nameTextField.setColumns(10);
		
		shinyCheckBox = new JCheckBox("");
		
		moveComboBox = new JComboBox<>();
		moveComboBox.setModel(new DefaultComboBoxModel<>(new String[] {"Move 1", "Move 2", "Move 3", "Move 4"}));
		moveComboBox.addActionListener(event -> moveTextField.setText(customMoves[moveComboBox.getSelectedIndex()]));
		moveComboBox.setEnabled(false);
		
		moveCheckBox = new JCheckBox(StringUtils.empty());
		moveCheckBox.addActionListener(event -> {
            moveComboBox.setEnabled(moveCheckBox.isSelected());
            moveTextField.setEnabled(moveCheckBox.isSelected());
        });

		// TODO: try to combine this inner shit with that similar one above and if that doesn't work at least fix its ugly ass formatting that I don't feel like handling right now
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

		JCheckBox selectedCheckBox = new JCheckBox(StringUtils.empty());
		selectedCheckBox.addActionListener(event -> trainerDialog.setSelected(index));
		
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
	
	public void setPokemon() {
		// TODO: What is this?
	}

	String getPokemonData() {
		String pokemonName = nameTextField.getText().trim();
		if (pokemonName.length() < 2) { // TODO: ??????? what
			return null;
		}
		
		// TODO: I don't think this will work always -- try with something like Mr. Mime
		pokemonName = Character.toUpperCase(pokemonName.charAt(0)) + pokemonName.substring(1).toLowerCase();
		if (!PokemonInfo.isPokemonName(pokemonName)) {
			return null;
		}
		
		String data = "pokemon: " + pokemonName + " " +
				levelFormattedTextField.getText() + " " +
				(shinyCheckBox.isSelected() ? "Shiny" : StringUtils.empty());
		
		if (moveCheckBox.isSelected()) {
			// TODO: Figure out what this is supposed to be doing because right now this variable does absolutely nothing
			boolean allValidMoves = true;
			String moves = "";
			
			for (int currMove = 0; currMove < customMoves.length && allValidMoves; ++currMove) {
				String move = customMoves[currMove].isEmpty() ? "None" : customMoves[currMove];
				allValidMoves |= Attack.isAttack(move);
				moves+= move +(currMove + 1 == customMoves.length?"*":", ");
			}
			
			if (allValidMoves) {
				data += " Moves: " + moves;
			}
		}
		
		return data;
	}

	public void setName(final String name) {
		this.nameTextField.setText(name);
	}

	public void setLevel(String levelString) {
		final int level = Integer.parseInt(levelString);
		this.levelFormattedTextField.setValue(level);
	}

	public void setShiny() {
		this.shinyCheckBox.setSelected(true);
	}

	public void setMoves(final String... moves) {
		setMoves(Arrays.asList(moves));
	}

	public void setMoves(final List<String> moves) {
		if (moves.size() > Move.MAX_MOVES) {
			Global.error("Cannot set more than " + Move.MAX_MOVES + " moves.");
		}

		this.moveCheckBox.setSelected(true);
		this.moveComboBox.setEnabled(true);
		this.moveTextField.setEnabled(true);
		for (int i = 0; i < moves.size(); i++) {
			this.customMoves[i] = moves.get(i);
		}
	}
}
