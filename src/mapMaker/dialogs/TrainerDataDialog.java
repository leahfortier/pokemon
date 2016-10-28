package mapMaker.dialogs;

import map.triggers.TrainerBattleTrigger;
import pattern.PokemonMatcher;
import util.StringUtils;

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
import java.util.regex.Matcher;

class TrainerDataDialog extends JPanel {
	private static final long serialVersionUID = 4995985841899035558L;
	
	private JTextField nameTextField;
	private JFormattedTextField cashFormattedTextField;
	private JButton addPokemonButton;
	
	private List<PokemonDataPanel> pokemonPanels;
	//boolean[] selected = new boolean[6];
	private Set<Integer> selected;
	private JPanel pokemonPanel;
	private JButton removeSelectedButton;
	private JButton moveDownButton;
	private JButton moveUpButton;
	
	TrainerDataDialog() {
		pokemonPanels = new ArrayList<>();
		selected = new HashSet<>();
		
		JLabel trainerNameLabel = new JLabel("Trainer Name");
		
		nameTextField = new JTextField();
		nameTextField.setColumns(10);

		// TODO: Cash received on? what?
		JLabel cashReceivedOnLabel = new JLabel("Cash");
		
		NumberFormat format = NumberFormat.getNumberInstance();
		NumberFormatter formatter = new NumberFormatter(format);
	    formatter.setValueClass(Integer.class);
	    formatter.setMinimum(0);
	    formatter.setMaximum(Integer.MAX_VALUE);

		cashFormattedTextField = new JFormattedTextField(formatter);
		cashFormattedTextField.setColumns(10);
		cashFormattedTextField.setValue(100);
		
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

            Integer[] values = new Integer[selected.size()];
            selected.toArray(values);
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
								.addComponent(cashReceivedOnLabel, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(73)
									.addComponent(cashFormattedTextField, GroupLayout.PREFERRED_SIZE, 206, GroupLayout.PREFERRED_SIZE))))
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
							.addComponent(cashReceivedOnLabel))
						.addComponent(cashFormattedTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
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
		
		setLayout(groupLayout);
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
		PokemonDataPanel panel = new PokemonDataPanel(TrainerDataDialog.this, pokemonPanels.size());
		pokemonPanels.add(panel);
		pokemonPanel.add(panel);
		pokemonPanel.validate();
		
		if (pokemonPanels.size() == 6) {
			addPokemonButton.setEnabled(false);
		}
		
		return panel;
	}
	
	void setTrainerData(String contents) {
		Matcher m = TrainerBattleTrigger.trainerBattleTriggerPattern.matcher(contents);
		while (m.find()) {
			if (m.group(1) != null) {
				final String pokemonName = m.group(2);
				final String level = m.group(3);
				final String parameters = m.group(4);

				PokemonDataPanel panel = addPokemonPanel();
				panel.setName(pokemonName);
				panel.setLevel(level);

				final PokemonMatcher paramsMatcher = PokemonMatcher.matchPokemonParameters(pokemonName, level, parameters);
				if (paramsMatcher.isShiny()) {
					panel.setShiny();
				}

				if (paramsMatcher.hasMoves()) {
					panel.setMoves(paramsMatcher.getMoveNames());
				}
			}
	
			if (m.group(5) != null) {
				this.nameTextField.setText(m.group(6));
			}
			
			if (m.group(9) != null) {
				try {
					cashFormattedTextField.setValue(Integer.parseInt(m.group(10)));
				}
				catch (NumberFormatException e) {
					cashFormattedTextField.setValue(100);
				}
			}
		}
		
	}
	
	String getTrainerData() {
		StringBuilder data = new StringBuilder();

		StringUtils.appendLine(data, "name: " + nameTextField.getText().trim());
		StringUtils.appendLine(data, "cash: " + ("" + cashFormattedTextField.getValue()).trim());

		for (PokemonDataPanel panel: pokemonPanels) {
			String pokemonData = panel.getPokemonData();
			if (pokemonData != null) {
				StringUtils.appendLine(data, pokemonData);
			}
		}
		
		return data.toString().trim();
	}
}
