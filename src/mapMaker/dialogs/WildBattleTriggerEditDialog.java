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
import map.triggers.WildBattleTrigger.EncounterRate;
import map.triggers.WildBattleTrigger.WildEncounter;

public class WildBattleTriggerEditDialog extends JPanel 
{	
	private static final long serialVersionUID = -3454589908432207758L;

	private JTextField nameTextField;
	public JComboBox<String> rateComboBox;
	
	private JScrollPane pokemonScrollPane;
	private JPanel pokemonCollectionPanel;
	private JButton addPokemonButton;
	private JButton removeSelectedButton;
	
	private ArrayList<WildPokemonDataPanel> wildPokemonPanels;
	private HashSet<Integer> selected;
	private JLabel lblSelect;
	private JLabel lblPokemon;
	private JLabel lblProbabilty;
	private JLabel lblLowLevel;
	private JLabel lblHighLevel;
	
	public WildBattleTriggerEditDialog() 
	{
		wildPokemonPanels = new ArrayList<>();
		selected = new HashSet<>();
		
		JLabel nameLabel = new JLabel("Name");
		
		nameTextField = new JTextField();
		nameTextField.setColumns(10);
		
		rateComboBox = new JComboBox<String>();
		rateComboBox.setModel(new DefaultComboBoxModel<String>(EncounterRate.ENCOUNTER_RATE_NAMES));
		
		pokemonScrollPane = new JScrollPane();
		pokemonScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		pokemonCollectionPanel = new JPanel();
		pokemonScrollPane.setViewportView(pokemonCollectionPanel);
		pokemonCollectionPanel.setLayout(new BoxLayout(pokemonCollectionPanel, BoxLayout.Y_AXIS));
		
		addPokemonButton = new JButton("Add Pokemon");
		addPokemonButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{	
				addPokemonPanel();
			}
		});
		
		removeSelectedButton = new JButton("Remove Selected");
		removeSelectedButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{	
				Integer[] values = new Integer[selected.size()];
				selected.toArray(values);
				for (int currPanel = values.length - 1; currPanel >= 0; --currPanel) 
				{
					wildPokemonPanels.remove(values[currPanel].intValue());
					pokemonCollectionPanel.remove(values[currPanel].intValue());
				}
				
				for (int currPanel = 0; currPanel < wildPokemonPanels.size(); ++currPanel) 
				{
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

	private WildPokemonDataPanel addPokemonPanel() 
	{
		WildPokemonDataPanel panel = new WildPokemonDataPanel(this, wildPokemonPanels.size());
		wildPokemonPanels.add(panel);
		pokemonCollectionPanel.add(panel);
		pokemonCollectionPanel.validate();
		pokemonScrollPane.validate();
		
		return panel;
	}
	
	public void setSelected(int index) 
	{
		if (selected.contains(index)) 
		{
			selected.remove(index);
			if (selected.size() == 0) 
			{
				removeSelectedButton.setEnabled(false);
			}
		}
		else 
		{
			selected.add(index);
			if (selected.size() == 1) 
			{
				removeSelectedButton.setEnabled(true);
			}
		}
	}
	
	public void initialize(WildBattleTrigger trigger) 
	{	
		nameTextField.setText(trigger.getName());
		
		rateComboBox.setSelectedIndex(trigger.encounterRate.ordinal());
		
		for (WildEncounter wildEncounter : trigger.wildEncounters)
		{
			WildPokemonDataPanel panel = addPokemonPanel();
			
			panel.pokemonTextField.setText(wildEncounter.pokemon.getName());
			panel.probabilityFormattedTextField.setValue(wildEncounter.probability);
			panel.lowLevelFormattedTextField.setValue(wildEncounter.minLevel);
			panel.highLevelFormattedTextField.setValue(wildEncounter.maxLevel);
		}
	}
	
	public WildBattleTrigger getTrigger() 
	{
		String name = nameTextField.getText();
		EncounterRate encounterRate = EncounterRate.valueOf((String)rateComboBox.getSelectedItem());
		
		int size = wildPokemonPanels.size();
		
		if (name.length() == 0 || size == 0)
			return null;
		
		WildEncounter[] wildEncounters = new WildEncounter[size];
		
		for (int currRow = 0; currRow < size; ++currRow) 
		{
			WildPokemonDataPanel panel = wildPokemonPanels.get(currRow);
			
			String pokemon = panel.pokemonTextField.getText();
			String minLevel = panel.lowLevelFormattedTextField.getText();
			String maxLevel = panel.highLevelFormattedTextField.getText();
			String probability = panel.probabilityFormattedTextField.getText();
			
			wildEncounters[currRow] = new WildEncounter(pokemon, minLevel, maxLevel, probability);
			System.out.println(wildEncounters[currRow].pokemon.getName());
		}
		
		return new WildBattleTrigger(name, wildEncounters, encounterRate);
	}
	
	public TriggerData getTriggerData() {
		
		WildBattleTrigger wbt = getTrigger();
		
		if (wbt == null)
			return null;
		
		return new TriggerData(wbt.getName(), "WildBattle\n" + wbt.triggerDataAsString());
	}
}
