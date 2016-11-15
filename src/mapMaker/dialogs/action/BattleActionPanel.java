package mapMaker.dialogs.action;

import pattern.action.ActionMatcher;
import pattern.action.BattleMatcher;
import trainer.Trainer;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class BattleActionPanel extends ActionPanel {
	private static final long serialVersionUID = 4995985841899035558L;

	private JTextField nameTextField;
	private JFormattedTextField cashFormattedTextField;
	private JTextField updateTriggerTextField;
	private JButton addPokemonButton;

	private List<PokemonDataPanel> pokemonPanels;
	private Set<Integer> selected;
	private JPanel pokemonPanel;
	private JButton removeSelectedButton;
	private JButton moveDownButton;
	private JButton moveUpButton;
	
	BattleActionPanel() {
		pokemonPanels = new ArrayList<>();
		selected = new HashSet<>();

		JLabel trainerNameLabel = new JLabel("Trainer Name");
		
		nameTextField = new JTextField();
		nameTextField.setColumns(10);

		JLabel cashMoney = new JLabel("Cash Money");
		
		NumberFormat format = NumberFormat.getNumberInstance();
		NumberFormatter formatter = new NumberFormatter(format);
	    formatter.setValueClass(Integer.class);
	    formatter.setMinimum(0);
	    formatter.setMaximum(Integer.MAX_VALUE);

		cashFormattedTextField = new JFormattedTextField(formatter);
		cashFormattedTextField.setColumns(10);
		cashFormattedTextField.setValue(100);

		JLabel updateTriggerLabel = new JLabel("Update Trigger");
		updateTriggerTextField = new JTextField("won");
		updateTriggerTextField.setColumns(10);
		
		JLabel pokemonLabel = new JLabel("Pokemon");
		JLabel shinyLabel = new JLabel("Shiny");
		JLabel customMovesLabel = new JLabel("Custom Moves");
		JLabel moveLabel = new JLabel("Move");
		JLabel levelLabel = new JLabel("Level");
		//pokemonPanel.setLayout(new BoxLayout(pokemonPanel, BoxLayout.X_AXIS));
		
		JSeparator separator = new JSeparator();
		
		addPokemonButton = new JButton("Add Pokemon");
		addPokemonButton.addActionListener(event -> addPokemonPanel());
		
		JLabel selectLabel = new JLabel("Select");
		
		removeSelectedButton = new JButton("Remove Selected");
		removeSelectedButton.addActionListener(event -> {

            Integer[] values = new Integer[selected.size()];
            selected.toArray(values);
            for (int currPanel = values.length - 1; currPanel >= 0; --currPanel) {
                pokemonPanel.remove(values[currPanel]);
                pokemonPanels.remove(values[currPanel].intValue());
            }

            for (int currPanel = 0; currPanel < pokemonPanels.size(); ++currPanel) {
                pokemonPanels.get(currPanel).index = currPanel;
            }

            selected.clear();
            removeSelectedButton.setEnabled(false);
            moveDownButton.setEnabled(false);
            moveUpButton.setEnabled(false);

            pokemonPanel.validate();
            pokemonPanel.repaint(50L);
        });
		
		removeSelectedButton.setEnabled(false);
		
		moveUpButton = new JButton("Move Up");
		moveUpButton.addActionListener(event -> {
            if (selected.contains(0)) {
				return;
			}

            pokemonPanel.removeAll();

            Integer[] values = new Integer[selected.size()];
            selected.toArray(values);
            Arrays.sort(values);

			for (Integer value : values) {
				selected.add(value - 1);
				selected.remove(value);

				Collections.swap(pokemonPanels, value, value - 1);
			}

			// TODO: Pretty sure this can be a for each
            for (int currPanel = 0; currPanel < pokemonPanels.size(); currPanel++) {
                pokemonPanels.get(currPanel).index = currPanel;
                pokemonPanel.add(pokemonPanels.get(currPanel));
            }

            pokemonPanel.validate();
            pokemonPanel.repaint(50L);
        });
		
		moveUpButton.setEnabled(false);
		
		moveDownButton = new JButton("Move Down");
		moveDownButton.addActionListener(event -> {
            if (selected.contains(pokemonPanels.size() - 1)) {
				return;
			}

            pokemonPanel.removeAll();

            Integer[] values = selected.toArray(new Integer[0]);
            Arrays.sort(values);

            for (int currPanel = values.length - 1; currPanel >=0 ; currPanel--) {
                selected.add(values[currPanel] + 1);
                selected.remove(values[currPanel]);

                Collections.swap(pokemonPanels, values[currPanel], values[currPanel] + 1);
            }

            // TODO: for each and above too
            for (int currPanel = 0; currPanel < pokemonPanels.size(); currPanel++) {
                pokemonPanels.get(currPanel).index = currPanel;
                pokemonPanel.add(pokemonPanels.get(currPanel));
            }

            pokemonPanel.validate();
            pokemonPanel.repaint(50L);
        });
		
		moveDownButton.setEnabled(false);
		
		pokemonPanel = new JPanel();
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(trainerNameLabel, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
							.addGap(10)
							.addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, 206, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(cashMoney, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(73)
									.addComponent(cashFormattedTextField, GroupLayout.PREFERRED_SIZE, 206, GroupLayout.PREFERRED_SIZE))))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(updateTriggerLabel, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
							.addGap(10)
							.addComponent(updateTriggerTextField, GroupLayout.PREFERRED_SIZE, 206, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(46)
									.addComponent(pokemonLabel, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE))
								.addComponent(selectLabel, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE))
							.addGap(82)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(levelLabel, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(105)
									.addComponent(customMovesLabel, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE))
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(55)
									.addComponent(shinyLabel, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)))
							.addGap(8)
							.addComponent(moveLabel, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE))
						.addComponent(pokemonPanel, GroupLayout.PREFERRED_SIZE, 596, GroupLayout.PREFERRED_SIZE)
						.addComponent(separator, GroupLayout.PREFERRED_SIZE, 602, GroupLayout.PREFERRED_SIZE)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(addPokemonButton, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(149)
									.addComponent(removeSelectedButton, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)))
							.addGap(2)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(149)
									.addComponent(moveDownButton, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE))
								.addComponent(moveUpButton, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)))))
		);
		
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(6)
							.addComponent(trainerNameLabel))
						.addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(6)
							.addComponent(cashMoney))
						.addComponent(cashFormattedTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(6)
							.addComponent(updateTriggerLabel))
						.addComponent(updateTriggerTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(pokemonLabel)
						.addComponent(selectLabel)
						.addComponent(levelLabel)
						.addComponent(customMovesLabel)
						.addComponent(shinyLabel)
						.addComponent(moveLabel))
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(pokemonPanel, GroupLayout.PREFERRED_SIZE, 256, GroupLayout.PREFERRED_SIZE)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(26)
							.addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(addPokemonButton)
						.addComponent(removeSelectedButton)
						.addComponent(moveDownButton)
						.addComponent(moveUpButton)))
		);
		
		this.setLayout(groupLayout);
	}
	
	public void setSelected(int index) {
		if (selected.contains(index)) {
			selected.remove(index);
			if (selected.size() == 0) {
				removeSelectedButton.setEnabled(false);
				moveDownButton.setEnabled(false);
				moveUpButton.setEnabled(false);
			}
		}
		else {
			selected.add(index);
			if (selected.size() == 1) {
				removeSelectedButton.setEnabled(true);
				moveDownButton.setEnabled(true);
				moveUpButton.setEnabled(true);
			}
		}
	}
	
	private PokemonDataPanel addPokemonPanel() {
		PokemonDataPanel panel = new PokemonDataPanel(BattleActionPanel.this, pokemonPanels.size());
		pokemonPanels.add(panel);
		pokemonPanel.add(panel);
		pokemonPanel.validate();
		
		if (pokemonPanels.size() == Trainer.MAX_POKEMON) {
			addPokemonButton.setEnabled(false);
		}
		
		return panel;
	}

	@Override
	protected void load(ActionMatcher matcher) {
		BattleMatcher battleMatcher = matcher.getBattle();

		nameTextField.setText(battleMatcher.name);
		cashFormattedTextField.setValue(battleMatcher.cashMoney);
		updateTriggerTextField.setText(battleMatcher.update);

		pokemonPanels.clear();
		for (String pokemonString : battleMatcher.pokemon) {
			PokemonDataPanel panel = addPokemonPanel();
			panel.load(pokemonString);
		}
	}

	@Override
	public ActionMatcher getActionMatcher(ActionType actionType) {
		String name = nameTextField.getText().trim();
		int cashMoney = Integer.parseInt(cashFormattedTextField.getValue().toString());
		String[] pokemon = new String[pokemonPanels.size()];
		for (int i = 0; i < pokemon.length; i++) {
			String pokemonData = pokemonPanels.get(i).getPokemonData();
			if (pokemonData != null) {
				pokemon[i] = pokemonData;
			}
		}

		String update = updateTriggerTextField.getText().trim();

		BattleMatcher battleMatcher = new BattleMatcher(name, cashMoney, pokemon, update);
		ActionMatcher actionMatcher = new ActionMatcher();
		actionMatcher.setBattle(battleMatcher);

		return actionMatcher;
	}
}
