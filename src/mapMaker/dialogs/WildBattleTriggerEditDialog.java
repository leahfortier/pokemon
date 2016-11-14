package mapMaker.dialogs;

import map.EncounterRate;
import map.WildEncounter;
import pattern.WildBattleMatcher;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WildBattleTriggerEditDialog extends TriggerDialog<WildBattleMatcher> {
	private static final long serialVersionUID = -3454589908432207758L;

	private JTextField nameTextField;
	private JComboBox<EncounterRate> rateComboBox;
	
	private JScrollPane pokemonScrollPane;
	private JPanel pokemonCollectionPanel;
	private JButton removeSelectedButton;
	
	private List<WildPokemonDataPanel> wildPokemonPanels;
	private Set<Integer> selected;
	
	public WildBattleTriggerEditDialog() {
		wildPokemonPanels = new ArrayList<>();
		selected = new HashSet<>();
		
		JLabel nameLabel = new JLabel("Name");
		
		nameTextField = new JTextField();
		nameTextField.setColumns(10);
		
		rateComboBox = new JComboBox<>();
		rateComboBox.setModel(new DefaultComboBoxModel<>(EncounterRate.values()));
		
		pokemonScrollPane = new JScrollPane();
		pokemonScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		pokemonCollectionPanel = new JPanel();
		pokemonScrollPane.setViewportView(pokemonCollectionPanel);
		pokemonCollectionPanel.setLayout(new BoxLayout(pokemonCollectionPanel, BoxLayout.Y_AXIS));

		JButton addPokemonButton = new JButton("Add Pokemon");
		addPokemonButton.addActionListener(actionEvent -> addPokemonPanel());
		
		removeSelectedButton = new JButton("Remove Selected");
		removeSelectedButton.addActionListener(actionEvent -> {

			// Convert selected to an int array
			int[] indexesToRemove = Arrays
					.stream(selected.toArray(new Integer[0]))
					.mapToInt(Integer::intValue)
					.sorted()
					.toArray();

            for (int index = indexesToRemove.length - 1; index >= 0; index--) {
                wildPokemonPanels.remove(indexesToRemove[index]);
                pokemonCollectionPanel.remove(indexesToRemove[index]);
            }

            for (int currPanel = 0; currPanel < wildPokemonPanels.size(); currPanel++) {
                wildPokemonPanels.get(currPanel).index = currPanel;
            }

            selected.clear();
            removeSelectedButton.setEnabled(false);

            pokemonCollectionPanel.validate();
            pokemonCollectionPanel.repaint(50L);
            pokemonScrollPane.validate();
        });
		
		removeSelectedButton.setEnabled(false);

		JLabel selectLabel = new JLabel("Select");
		JLabel pokemonLabel = new JLabel("Pokemon");
		JLabel probabilityLabel = new JLabel("Probability");
		JLabel minLevelLabel = new JLabel("Min Level");
		JLabel maxLevelLabel = new JLabel("Max Level");
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addComponent(nameLabel, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
					.addGap(1)
					.addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, 214, GroupLayout.PREFERRED_SIZE)
					.addGap(12)
					.addComponent(rateComboBox, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(7)
					.addComponent(selectLabel, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
					.addGap(7)
					.addComponent(pokemonLabel, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
					.addGap(58)
					.addComponent(probabilityLabel, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
					.addGap(13)
					.addComponent(minLevelLabel)
					.addGap(24)
					.addComponent(maxLevelLabel, GroupLayout.PREFERRED_SIZE, 77, GroupLayout.PREFERRED_SIZE))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addComponent(pokemonScrollPane, GroupLayout.PREFERRED_SIZE, 448, GroupLayout.PREFERRED_SIZE))
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addComponent(addPokemonButton, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
					.addGap(12)
					.addComponent(removeSelectedButton, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE))
		);
		
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(6)
							.addComponent(nameLabel))
						.addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(2)
							.addComponent(rateComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addGap(5)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(selectLabel)
						.addComponent(pokemonLabel)
						.addComponent(probabilityLabel)
						.addComponent(minLevelLabel)
						.addComponent(maxLevelLabel))
					.addGap(5)
					.addComponent(pokemonScrollPane, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
					.addGap(4)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(addPokemonButton)
						.addComponent(removeSelectedButton)))
		);

		setLayout(groupLayout);
	}

	private WildPokemonDataPanel addPokemonPanel() {
		WildPokemonDataPanel panel = new WildPokemonDataPanel(this, wildPokemonPanels.size());
		wildPokemonPanels.add(panel);
		pokemonCollectionPanel.add(panel);
		pokemonCollectionPanel.validate();
		pokemonScrollPane.validate();
		
		return panel;
	}
	
	public void setSelected(int index) {
		if (selected.contains(index)) {
			selected.remove(index);
			if (selected.size() == 0) {
				removeSelectedButton.setEnabled(false);
			}
		}
		else {
			selected.add(index);
			if (selected.size() == 1) {
				removeSelectedButton.setEnabled(true);
			}
		}
	}

	@Override
	public WildBattleMatcher getMatcher() {
		String name = nameTextField.getText();
		EncounterRate encounterRate = (EncounterRate)rateComboBox.getSelectedItem();

		if (name.isEmpty() || wildPokemonPanels.isEmpty()) {
			return null;
		}

		WildEncounter[] wildEncounters = new WildEncounter[wildPokemonPanels.size()];
		for (int currRow = 0; currRow < wildEncounters.length; currRow++) {
			WildPokemonDataPanel panel = wildPokemonPanels.get(currRow);

			String pokemon = panel.pokemonTextField.getText();
			String minLevel = panel.lowLevelFormattedTextField.getText();
			String maxLevel = panel.highLevelFormattedTextField.getText();
			String probability = panel.probabilityFormattedTextField.getText();

			wildEncounters[currRow] = new WildEncounter(pokemon, minLevel, maxLevel, probability);
			System.out.println(wildEncounters[currRow].getPokemonName());
		}

		return new WildBattleMatcher(name, encounterRate, wildEncounters);
	}

	@Override
	public void load(WildBattleMatcher matcher) {
		nameTextField.setText(matcher.getBasicName());
		rateComboBox.setSelectedItem(matcher.getEncounterRate());

		for (WildEncounter wildEncounter : matcher.getWildEncounters()) {
			WildPokemonDataPanel panel = addPokemonPanel();

			panel.pokemonTextField.setText(wildEncounter.getPokemonName());
			panel.probabilityFormattedTextField.setValue(wildEncounter.getProbability());
			panel.lowLevelFormattedTextField.setValue(wildEncounter.getMinLevel());
			panel.highLevelFormattedTextField.setValue(wildEncounter.getMaxLevel());

			System.out.println(wildEncounter.getPokemonName() + " " + wildEncounter.getMinLevel() + " " + wildEncounter.getMaxLevel() + " " + wildEncounter.getProbability());
		}
	}
}
