package mapMaker.dialogs.action;

import pattern.PokemonMatcher;
import pattern.action.ActionMatcher;
import pattern.action.ActionMatcher.BattleActionMatcher;
import trainer.Trainer;
import util.GUIUtils;
import util.PokeString;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class BattleActionPanel extends ActionPanel {
    private static final long serialVersionUID = 4995985841899035558L;

    private final JPanel topComponent;
    private final JPanel bottomComponent;

    private final JTextField nameTextField;
    private final JFormattedTextField cashFormattedTextField;
    private final JCheckBox maxPokemonLimitCheckBox;
    private final JTextField updateInteractionTextField;
    private final JButton addPokemonButton;

    private final List<PokemonDataPanel> pokemonPanels;

    private final ActionDialog parentDialog;

    BattleActionPanel(ActionDialog actionDialog) {
        this.parentDialog = actionDialog;

        pokemonPanels = new ArrayList<>();

        nameTextField = new JTextField();
        cashFormattedTextField = GUIUtils.createIntegerTextField(100, 0, Integer.MAX_VALUE);
        maxPokemonLimitCheckBox = GUIUtils.createCheckBox("Limit Max " + PokeString.POKEMON);
        updateInteractionTextField = new JTextField("won");
        addPokemonButton = GUIUtils.createButton("Add Pokemon", event -> addPokemonPanel(null));

        JButton removeSelectedButton = GUIUtils.createButton(
                "Remove Selected",
                event -> {
                    pokemonPanels.removeIf(PokemonDataPanel::isSelected);
                    render();
                }
        );

        JButton moveUpButton = GUIUtils.createButton(
                "Move Up",
                event -> {
                    for (int i = 1; i < pokemonPanels.size(); i++) {
                        if (pokemonPanels.get(i).isSelected() && !pokemonPanels.get(i - 1).isSelected()) {
                            Collections.swap(pokemonPanels, i, i - 1);
                        }
                    }

                    render();
                }
        );

        JButton moveDownButton = new JButton("Move Down");
        moveDownButton.addActionListener(event -> {
            for (int i = pokemonPanels.size() - 2; i >= 0; i--) {
                if (pokemonPanels.get(i).isSelected() && !pokemonPanels.get(i + 1).isSelected()) {
                    Collections.swap(pokemonPanels, i, i + 1);
                }
            }

            render();
        });

        JPanel tippityTop = GUIUtils.createHorizontalLayoutComponent(
                GUIUtils.createTextFieldComponent("Trainer Name", nameTextField),
                GUIUtils.createTextFieldComponent("Cash Money", cashFormattedTextField),
                maxPokemonLimitCheckBox,
                GUIUtils.createTextFieldComponent("Update Interaction", updateInteractionTextField)
        );

        this.topComponent = GUIUtils.createVerticalLayoutComponent(
                tippityTop,
                GUIUtils.createLabel(
                        "            " +
                                "Pokemon Name                      " +
                                "Nickname                      " +
                                "Level             " +
                                "Shiny  " +
                                "Custom Moves                                       " +
                                "Move Name"
                )
        );

        this.bottomComponent = GUIUtils.createHorizontalLayoutComponent(
                addPokemonButton,
                removeSelectedButton,
                moveUpButton,
                moveDownButton
        );

        render();
    }

    @Override
    public void render() {
        removeAll();

        addPokemonButton.setEnabled(pokemonPanels.size() < Trainer.MAX_POKEMON);

        List<JComponent> components = new ArrayList<>();
        components.add(topComponent);
        components.addAll(pokemonPanels);
        components.add(bottomComponent);

        GUIUtils.setVerticalLayout(this, components.toArray(new JComponent[0]));

        parentDialog.render();
    }

    private void addPokemonPanel(PokemonMatcher pokemonMatcher) {
        pokemonPanels.add(new PokemonDataPanel(pokemonMatcher));
        render();
    }

    @Override
    protected void load(ActionMatcher matcher) {
        if (matcher == null) {
            return;
        }

        BattleActionMatcher battleMatcher = (BattleActionMatcher)matcher;

        nameTextField.setText(battleMatcher.getName());
        cashFormattedTextField.setValue(battleMatcher.getDatCashMoney());
        maxPokemonLimitCheckBox.setSelected(battleMatcher.isMaxPokemonLimit());
        updateInteractionTextField.setText(battleMatcher.getUpdateInteraction());

        pokemonPanels.clear();
        for (PokemonMatcher pokemonMatcher : battleMatcher.getPokemon()) {
            addPokemonPanel(pokemonMatcher);
        }
    }

    @Override
    public ActionMatcher getActionMatcher(ActionType actionType) {
        String name = nameTextField.getText().trim();
        int cashMoney = Integer.parseInt(cashFormattedTextField.getValue().toString());
        boolean isMaxLimit = maxPokemonLimitCheckBox.isSelected();
        PokemonMatcher[] pokemon = new PokemonMatcher[pokemonPanels.size()];
        for (int i = 0; i < pokemon.length; i++) {
            PokemonMatcher pokemonData = pokemonPanels.get(i).getMatcher();
            if (pokemonData != null) {
                pokemon[i] = pokemonData;
            }
        }

        String update = updateInteractionTextField.getText().trim();

        return new BattleActionMatcher(name, cashMoney, isMaxLimit, pokemon, update);
    }
}
