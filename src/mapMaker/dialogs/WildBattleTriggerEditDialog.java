package mapMaker.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;

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

import map.triggers.TriggerData;
import map.triggers.WildBattleTrigger;

public class WildBattleTriggerEditDialog extends JPanel {
	
	private static final long serialVersionUID = -3454589908432207758L;

	private JTextField nameTextField;
	public JComboBox<String> rateComboBox;
	
	private static String[] encounterRates = new String[] {"Very Common","Common","Semi-Rare","Rare","Very Rare"};
	private JScrollPane pokemonScrollPane;
	private JPanel pokemonCollectionPanel;
	private JButton addPokemonButton;
	private JButton removeSelectedButton;
	
	
	private ArrayList<WildPokemonDataPanel> wildPokemonPanels;
	HashSet<Integer> selected;
	private JLabel lblSelect;
	private JLabel lblPokemon;
	private JLabel lblProbabilty;
	private JLabel lblLowLevel;
	private JLabel lblHighLevel;
	
	public WildBattleTriggerEditDialog() {
		
		wildPokemonPanels = new ArrayList<>();
		selected = new HashSet<>();
		
		JLabel nameLabel = new JLabel("Name");
		
		nameTextField = new JTextField();
		nameTextField.setColumns(10);
		
		rateComboBox = new JComboBox<String>();
		rateComboBox.setModel(new DefaultComboBoxModel<String>(encounterRates));
		
		pokemonScrollPane = new JScrollPane();
		pokemonScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		pokemonCollectionPanel = new JPanel();
		pokemonScrollPane.setViewportView(pokemonCollectionPanel);
		pokemonCollectionPanel.setLayout(new BoxLayout(pokemonCollectionPanel, BoxLayout.Y_AXIS));
		
		addPokemonButton = new JButton("Add Pokemon");
		addPokemonButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				addPokemonPanel();
			}
		});
		
		removeSelectedButton = new JButton("Remove Selected");
		removeSelectedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				Integer[] values = new Integer[selected.size()];
				selected.toArray(values);
				for (int currPanel = values.length-1; currPanel >= 0; --currPanel) {
					wildPokemonPanels.remove(values[currPanel].intValue());
					pokemonCollectionPanel.remove(values[currPanel].intValue());
				}
				
				for (int currPanel = 0; currPanel < wildPokemonPanels.size(); ++currPanel) {
					wildPokemonPanels.get(currPanel).index = currPanel;
				}
				
				selected.clear();
				removeSelectedButton.setEnabled(false);
				
				pokemonCollectionPanel.validate();
				pokemonCollectionPanel.repaint(50L);
				pokemonScrollPane.validate();
				
			}
		});
		removeSelectedButton.setEnabled(false);
		
		lblSelect = new JLabel("Select");
		
		lblPokemon = new JLabel("Pokemon");
		
		lblProbabilty = new JLabel("Probabilty");
		
		lblLowLevel = new JLabel("Low Level");
		
		lblHighLevel = new JLabel("High Level");
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
					.addComponent(lblSelect, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
					.addGap(7)
					.addComponent(lblPokemon, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
					.addGap(58)
					.addComponent(lblProbabilty, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
					.addGap(13)
					.addComponent(lblLowLevel)
					.addGap(24)
					.addComponent(lblHighLevel, GroupLayout.PREFERRED_SIZE, 77, GroupLayout.PREFERRED_SIZE))
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
						.addComponent(lblSelect)
						.addComponent(lblPokemon)
						.addComponent(lblProbabilty)
						.addComponent(lblLowLevel)
						.addComponent(lblHighLevel))
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
	
	
	
	public void initialize(WildBattleTrigger trigger) {
		
		nameTextField.setText(trigger.getName());
		
		for (int currRate = 0; currRate < encounterRates.length; ++currRate) {
			if (encounterRates[currRate].toLowerCase().replace(" ","").equals(trigger.encounterRateString.toLowerCase())) {
				rateComboBox.setSelectedIndex(currRate);
				break;
			}
		}
		
		for (int currPokemon = 0; currPokemon < trigger.pokemon.length; ++currPokemon){
			WildPokemonDataPanel panel = addPokemonPanel();
			panel.pokemonTextField.setText(trigger.pokemon[currPokemon]);
			panel.probabilityFormattedTextField.setValue(trigger.probability[currPokemon]);
			panel.lowLevelFormattedTextField.setValue(trigger.lowLevel[currPokemon]);
			panel.highLevelFormattedTextField.setValue(trigger.highLevel[currPokemon]);
		}
	}
	
	public WildBattleTrigger getTrigger() {
		
		String name = nameTextField.getText();
		String encounterRate = ((String)rateComboBox.getSelectedItem()).replace(" ","");
		
		int size = wildPokemonPanels.size();
		
		if (name.length() == 0 || size == 0)
			return null;
		
		String[] pokemon = new String[size];
		int[] probability = new int[size];
		int[] low = new int[size];
		int[] high = new int[size];
		
		for (int currRow = 0; currRow < size; ++currRow) {

			WildPokemonDataPanel panel = wildPokemonPanels.get(currRow);
			pokemon[currRow] = panel.pokemonTextField.getText();
			System.out.println(pokemon[currRow]);
			probability[currRow] = ((Integer)panel.probabilityFormattedTextField.getValue()).intValue();
			low[currRow] = ((Integer)panel.lowLevelFormattedTextField.getValue()).intValue();
			high[currRow] = ((Integer)panel.highLevelFormattedTextField.getValue()).intValue();
		}
		
		return new WildBattleTrigger(name, probability, low, high, pokemon, encounterRate);
	}
	
	public TriggerData getTriggerData() {
		
		WildBattleTrigger wbt = getTrigger();
		
		if (wbt == null)
			return null;
		
		return new TriggerData(wbt.getName(), "WildBattle\n"+wbt.triggerDataAsString());
	}
}
	
