package mapMaker.dialogs.action;

import battle.attack.AttackNamesies;
import battle.attack.Move;
import main.Global;
import pattern.PokemonMatcher;
import pokemon.PartyPokemon;
import pokemon.PokemonNamesies;
import util.ColorDocumentListener.ColorCondition;
import util.GUIUtils;
import util.string.StringUtils;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.List;

public class PokemonDataPanel extends JPanel {
    private final JTextField nameTextField;
    private final JTextField nicknameTextField;
    private final JTextField moveTextField;
    private final JCheckBox shinyCheckBox;
    private final JCheckBox moveCheckBox;
    private final JFormattedTextField levelFormattedTextField;
    private final JCheckBox selectedCheckBox;

    private final AttackNamesies[] customMoves = new AttackNamesies[Move.MAX_MOVES];

    // Fucking Java won't let this be final
    private JComboBox<String> moveComboBox;

    public PokemonDataPanel(PokemonMatcher pokemonMatcher) {

        selectedCheckBox = GUIUtils.createCheckBox();
        nameTextField = GUIUtils.createColorConditionTextField(new ColorCondition() {
            @Override
            public boolean greenCondition() {
                return PokemonNamesies.tryValueOf(nameTextField.getText().trim()) != null;
            }
        });
        nicknameTextField = GUIUtils.createTextField(); // Restrict to max length characters
        levelFormattedTextField = GUIUtils.createIntegerTextField(1, 1, PartyPokemon.MAX_LEVEL);
        shinyCheckBox = GUIUtils.createCheckBox();
        moveCheckBox = GUIUtils.createCheckBox();
        moveTextField = GUIUtils.createColorConditionTextField(new ColorCondition() {
            @Override
            public boolean greenCondition() {
                return customMoves[moveComboBox.getSelectedIndex()] != null;
            }

            @Override
            public void additionalValueChanged() {
                customMoves[moveComboBox.getSelectedIndex()] = AttackNamesies.tryValueOf(moveTextField.getText().trim());
            }
        });

        moveTextField.setEnabled(false);

        moveComboBox = GUIUtils.createComboBox(
                new String[] { "Move 1", "Move 2", "Move 3", "Move 4" }, // TODO: Fuck this shit
                event -> {
                    AttackNamesies selectedMove = customMoves[moveComboBox.getSelectedIndex()];
                    String moveText = selectedMove == null ? StringUtils.empty() : selectedMove.getName();
                    moveTextField.setText(moveText);
                }
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
                nicknameTextField,
                levelFormattedTextField,
                shinyCheckBox,
                moveCheckBox,
                moveComboBox,
                moveTextField
        );

        load(pokemonMatcher);
    }

    public PokemonMatcher getMatcher() {
        PokemonNamesies namesies = PokemonNamesies.tryValueOf(nameTextField.getText().trim());
        if (namesies == null) {
            return null;
        }

        return new PokemonMatcher(
                namesies,
                nicknameTextField.getText(),
                Integer.parseInt(levelFormattedTextField.getText().trim()),
                shinyCheckBox.isSelected(),
                customMoves,
                null // TODO: Should be able to give item
        );
    }

    private void setMoves(final List<AttackNamesies> moves) {
        if (moves.size() > Move.MAX_MOVES) {
            Global.error("Cannot set more than " + Move.MAX_MOVES + " moves.");
        }

        this.moveCheckBox.setSelected(true);
        this.moveComboBox.setEnabled(true);
        this.moveTextField.setEnabled(true);
        for (int i = 0; i < moves.size(); i++) {
            this.customMoves[i] = moves.get(i);
        }

        this.moveTextField.setText(this.customMoves[0].getName());
    }

    private void load(PokemonMatcher matcher) {
        if (matcher == null) {
            return;
        }

        this.nameTextField.setText(matcher.getNamesies().getName());
        this.nicknameTextField.setText(matcher.getNickname());
        this.levelFormattedTextField.setValue(Integer.parseInt(matcher.getLevel() + ""));

        if (matcher.isShiny()) {
            this.shinyCheckBox.setSelected(true);
        }

        if (matcher.hasMoves()) {
            this.setMoves(matcher.getMoveNames());
        }
    }

    public boolean isSelected() {
        return this.selectedCheckBox.isSelected();
    }
}
