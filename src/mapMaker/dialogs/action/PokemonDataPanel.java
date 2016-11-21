package mapMaker.dialogs.action;

import battle.attack.Attack;
import battle.attack.Move;
import main.Global;
import pattern.PokemonMatcher;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import util.ColorDocumentListener.ColorCondition;
import util.GUIUtils;
import util.StringUtils;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.List;

class PokemonDataPanel extends JPanel {
	private static final long serialVersionUID = 2679616277402077123L;

	private final JTextField nameTextField;
	private final JTextField moveTextField;
	private final JCheckBox shinyCheckBox;
	private JComboBox<String> moveComboBox;
	private final JCheckBox moveCheckBox;
	private final JFormattedTextField levelFormattedTextField;

	private final JCheckBox selectedCheckBox;

	private final String[] customMoves = new String[Move.MAX_MOVES]; // TODO: I think this should just be a list
	
	PokemonDataPanel(String pokemonDescription) {
		
		for (int currMove = 0; currMove < customMoves.length; ++currMove) {
			customMoves[currMove] = StringUtils.empty();
		}

		selectedCheckBox = GUIUtils.createCheckBox();
		nameTextField = GUIUtils.createColorConditionTextField(new ColorCondition() {
			@Override
			public boolean greenCondition() {
				System.out.println(nameTextField.getText().trim());
				return PokemonNamesies.tryValueOf(nameTextField.getText().trim()) != null;
			}
		});
		levelFormattedTextField = GUIUtils.createIntegerTextField(1, 1, ActivePokemon.MAX_LEVEL);
		shinyCheckBox = GUIUtils.createCheckBox();
		moveCheckBox = GUIUtils.createCheckBox();
		moveTextField = GUIUtils.createColorConditionTextField(new ColorCondition() {
			@Override
			public boolean greenCondition() {
				return Attack.isAttack(customMoves[moveComboBox.getSelectedIndex()]);
			}

			@Override
			public void additionalValueChanged() {
				customMoves[moveComboBox.getSelectedIndex()] = moveTextField.getText().trim();
			}
		});

		moveTextField.setEnabled(false);

		moveComboBox = GUIUtils.createComboBox(
				new String[] { "Move 1", "Move 2", "Move 3", "Move 4" }, // TODO: Fuck this shit
				event -> moveTextField.setText(customMoves[moveComboBox.getSelectedIndex()])
		);
		moveComboBox.setEnabled(false);

		moveCheckBox.addActionListener(event -> {
            moveComboBox.setEnabled(moveCheckBox.isSelected());
            moveTextField.setEnabled(moveCheckBox.isSelected());
        });

		GUIUtils.setHorizontalLayout(
				this,
				selectedCheckBox,
				nameTextField,
				levelFormattedTextField,
				shinyCheckBox,
				moveCheckBox,
				moveComboBox,
				moveTextField
		);

		load(pokemonDescription);
	}

	String getPokemonData() {
		String pokemonName = nameTextField.getText().trim();
		if (pokemonName.length() < 2) { // TODO: ??????? what
			return null;
		}
		
		// TODO: I don't think this will work always -- try with something like Mr. Mime
		pokemonName = Character.toUpperCase(pokemonName.charAt(0)) + pokemonName.substring(1).toLowerCase();
		PokemonNamesies namesies = PokemonNamesies.tryValueOf(pokemonName);
		if (namesies == null) {
			return null;
		}
		
		String data = "pokemon: " + pokemonName + " " +
				levelFormattedTextField.getText() + " " +
				(shinyCheckBox.isSelected() ? "Shiny" : StringUtils.empty());
		
		if (moveCheckBox.isSelected()) {
			// TODO: Figure out what this is supposed to be doing because right now this variable does absolutely nothing
			boolean allValidMoves = true;
			String moves = "";
			
			for (int currMove = 0; currMove < customMoves.length; ++currMove) {
				String move = customMoves[currMove].isEmpty() ? "None" : customMoves[currMove];
				if (Attack.isAttack(move)) {
					allValidMoves = false;
					break;
				}

				moves += move + (currMove + 1 == customMoves.length ? "" : ", ");
			}
			
			if (allValidMoves) {
				data += " Moves: " + moves;
			}
		}
		
		return data;
	}

	private void setMoves(final List<String> moves) {
		if (moves.size() > Move.MAX_MOVES) {
			Global.error("Cannot set more than " + Move.MAX_MOVES + " moves.");
		}

		this.moveCheckBox.setSelected(true);
		this.moveComboBox.setEnabled(true);
		this.moveTextField.setEnabled(true);
		for (int i = 0; i < moves.size(); i++) {
			this.customMoves[i] = moves.get(i);
		}

		this.moveTextField.setText(this.customMoves[0]);
	}

	private void load(String pokemonDescription) {
		if (StringUtils.isNullOrEmpty(pokemonDescription)) {
			return;
		}

		PokemonMatcher pokemonMatcher = PokemonMatcher.matchPokemonDescription(pokemonDescription);
		this.nameTextField.setText(pokemonMatcher.getNamesies().getName());
		this.levelFormattedTextField.setValue(Integer.parseInt(pokemonMatcher.getLevel() + ""));

		if (pokemonMatcher.isShiny()) {
			this.shinyCheckBox.setSelected(true);
		}

		if (pokemonMatcher.hasMoves()) {
			this.setMoves(pokemonMatcher.getMoveNames());
		}
	}

	public boolean isSelected() {
		return this.selectedCheckBox.isSelected();
	}
}
