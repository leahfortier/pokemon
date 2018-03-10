package mapMaker.dialogs.action.panel;

import mapMaker.dialogs.action.ActionDialog;
import mapMaker.dialogs.action.ActionPanel;
import mapMaker.dialogs.action.PokemonDataPanel;
import pattern.PokemonMatcher;
import pattern.action.EntityActionMatcher.BattleActionMatcher;
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

public class BattleActionPanel extends ActionPanel<BattleActionMatcher> {
    private final JPanel topComponent;
    private final JPanel bottomComponent;

    private final JTextField nameTextField;
    private final JFormattedTextField cashFormattedTextField;
    private final JCheckBox maxPokemonLimitCheckBox;
    private final JTextField updateInteractionTextField;
    private final JButton addPokemonButton;

    private final List<PokemonDataPanel> pokemonPanels;

    private final ActionDialog parentDialog;

    public BattleActionPanel(ActionDialog actionDialog) {
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
    protected void load(BattleActionMatcher matcher) {
        if (matcher == null) {
            return;
        }

        nameTextField.setText(matcher.getName());
        cashFormattedTextField.setValue(matcher.getDatCashMoney());
        maxPokemonLimitCheckBox.setSelected(matcher.isMaxPokemonLimit());
        updateInteractionTextField.setText(matcher.getUpdateInteraction());

        pokemonPanels.clear();
        for (PokemonMatcher pokemonMatcher : matcher.getPokemon()) {
            addPokemonPanel(pokemonMatcher);
        }
    }

    @Override
    public BattleActionMatcher getActionMatcher() {
        String name = nameTextField.getText().trim();
        int cashMoney = Integer.parseInt(cashFormattedTextField.getValue().toString());
        boolean isMaxLimit = maxPokemonLimitCheckBox.isSelected();
        String update = updateInteractionTextField.getText().trim();

        PokemonMatcher[] pokemon = new PokemonMatcher[pokemonPanels.size()];
        for (int i = 0; i < pokemon.length; i++) {
            PokemonMatcher pokemonData = pokemonPanels.get(i).getMatcher();
            if (pokemonData != null) {
                pokemon[i] = pokemonData;
            }
        }

        return new BattleActionMatcher(name, cashMoney, isMaxLimit, pokemon, update);
    }
}
