package mapMaker.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Matcher;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.text.NumberFormatter;

import map.triggers.TrainerBattleTrigger;

public class TrainerDataDialog extends JPanel 
{	
	private static final long serialVersionUID = 4995985841899035558L;
	
	private JTextField nameTextField;
	private JFormattedTextField cashFormattedTextField;
	private JButton addPokemonButton;
	
	private ArrayList<PokemonDataPanel> pokemonPanels;
	//boolean[] selected = new boolean[6];
	private HashSet<Integer> selected;
	private JPanel PokemonPanel;
	private JButton removeSelectedButton;
	private JButton moveDownButton;
	private JButton moveUpButton;
	
	public TrainerDataDialog() 
	{	
		pokemonPanels = new ArrayList<>();
		selected = new HashSet<>();
		
		JLabel lblTrainerName = new JLabel("Trainer Name");
		
		nameTextField = new JTextField();
		nameTextField.setColumns(10);
		
		JLabel lblCashReceivedOn = new JLabel("Cash");
		
		NumberFormat format = NumberFormat.getNumberInstance();
		NumberFormatter formatter = new NumberFormatter(format);
	    formatter.setValueClass(Integer.class);
	    formatter.setMinimum(0);
	    formatter.setMaximum(Integer.MAX_VALUE);
		cashFormattedTextField = new JFormattedTextField(formatter);
		cashFormattedTextField.setColumns(10);
		cashFormattedTextField.setValue(100);
		
		JLabel lblPokemon = new JLabel("Pokemon");
		JLabel lblShiny = new JLabel("Shiny");
		JLabel lblCustomMoves = new JLabel("Custom Moves");
		JLabel lblMove = new JLabel("Move");
		JLabel lblLevel = new JLabel("Level");
		//PokemonPanel.setLayout(new BoxLayout(PokemonPanel, BoxLayout.X_AXIS));
		
		JSeparator separator = new JSeparator();
		
		addPokemonButton = new JButton("Add Pokemon");
		addPokemonButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				addPokemonPanel();
			}
		});
		
		JLabel lblSelect = new JLabel("Select");
		
		removeSelectedButton = new JButton("Remove Selected");
		removeSelectedButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				
				Integer[] values = new Integer[selected.size()];
				selected.toArray(values);
				for (int currPanel = values.length - 1; currPanel >= 0; --currPanel) 
				{
					PokemonPanel.remove(values[currPanel].intValue());
					pokemonPanels.remove(values[currPanel].intValue());
				}
				
				for (int currPanel = 0; currPanel < pokemonPanels.size(); ++currPanel) 
				{
					pokemonPanels.get(currPanel).index = currPanel;
				}
				
				selected.clear();
				removeSelectedButton.setEnabled(false);
				moveDownButton.setEnabled(false);
				moveUpButton.setEnabled(false);
				
				PokemonPanel.validate();
				PokemonPanel.repaint(50L);
			}
		});
		
		removeSelectedButton.setEnabled(false);
		
		moveUpButton = new JButton("Move Up");
		moveUpButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				if (selected.contains(0))
					return;
				
				PokemonPanel.removeAll();
				
				Integer[] values = new Integer[selected.size()];
				selected.toArray(values);
				Arrays.sort(values);
				
				for (int currPanel = 0; currPanel < values.length; ++currPanel) 
				{
					selected.add(values[currPanel] - 1);
					selected.remove(values[currPanel]);
					
					Collections.swap(pokemonPanels, values[currPanel], values[currPanel] - 1);
				}
				
				for (int currPanel = 0; currPanel < pokemonPanels.size(); ++currPanel) 
				{
					pokemonPanels.get(currPanel).index = currPanel;
					PokemonPanel.add(pokemonPanels.get(currPanel));
				}
				
				PokemonPanel.validate();
				PokemonPanel.repaint(50L);
			}
		});
		
		moveUpButton.setEnabled(false);
		
		moveDownButton = new JButton("Move Down");
		moveDownButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				if (selected.contains(pokemonPanels.size() - 1))
					return;
				
				PokemonPanel.removeAll();

				Integer[] values = new Integer[selected.size()];
				selected.toArray(values);
				Arrays.sort(values);
				
				for (int currPanel = values.length - 1; currPanel >=0 ; --currPanel) 
				{
					selected.add(values[currPanel] + 1);
					selected.remove(values[currPanel]);
					
					Collections.swap(pokemonPanels, values[currPanel], values[currPanel] + 1);
				}
				
				for (int currPanel = 0; currPanel < pokemonPanels.size(); ++currPanel) 
				{
					pokemonPanels.get(currPanel).index = currPanel;
					PokemonPanel.add(pokemonPanels.get(currPanel));
				}
				
				PokemonPanel.validate();
				PokemonPanel.repaint(50L);
			}
		});
		
		moveDownButton.setEnabled(false);
		
		PokemonPanel = new JPanel();
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblTrainerName, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
							.addGap(10)
							.addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, 206, GroupLayout.PREFERRED_SIZE)
							.addGap(12)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblCashReceivedOn, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(73)
									.addComponent(cashFormattedTextField, GroupLayout.PREFERRED_SIZE, 206, GroupLayout.PREFERRED_SIZE))))
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(46)
									.addComponent(lblPokemon, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE))
								.addComponent(lblSelect, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE))
							.addGap(82)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblLevel, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(105)
									.addComponent(lblCustomMoves, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE))
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(55)
									.addComponent(lblShiny, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)))
							.addGap(8)
							.addComponent(lblMove, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE))
						.addComponent(PokemonPanel, GroupLayout.PREFERRED_SIZE, 596, GroupLayout.PREFERRED_SIZE)
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
							.addComponent(lblTrainerName))
						.addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(6)
							.addComponent(lblCashReceivedOn))
						.addComponent(cashFormattedTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(lblPokemon)
						.addComponent(lblSelect)
						.addComponent(lblLevel)
						.addComponent(lblCustomMoves)
						.addComponent(lblShiny)
						.addComponent(lblMove))
					.addGap(6)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(PokemonPanel, GroupLayout.PREFERRED_SIZE, 256, GroupLayout.PREFERRED_SIZE)
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
	
	public void setSelected(int index) 
	{
		if (selected.contains(index)) 
		{
			selected.remove(index);
			if (selected.size() == 0) 
			{
				removeSelectedButton.setEnabled(false);
				moveDownButton.setEnabled(false);
				moveUpButton.setEnabled(false);
			}
		}
		else 
		{
			selected.add(index);
			if (selected.size() == 1) 
			{
				removeSelectedButton.setEnabled(true);
				moveDownButton.setEnabled(true);
				moveUpButton.setEnabled(true);
			}
		}
	}
	
	private PokemonDataPanel addPokemonPanel() 
	{
		PokemonDataPanel panel = new PokemonDataPanel(TrainerDataDialog.this, pokemonPanels.size());
		pokemonPanels.add(panel);
		PokemonPanel.add(panel);
		PokemonPanel.validate();
		
		if (pokemonPanels.size() == 6) 
		{
			addPokemonButton.setEnabled(false);
		}
		
		return panel;
	}
	
	public void setTrainerData(String contents) 
	{
		
		Matcher m = TrainerBattleTrigger.eventTriggerPattern.matcher(contents);
		while (m.find())
		{
			if (m.group(1) != null)
			{
				PokemonDataPanel panel = addPokemonPanel();
				panel.nameTextField.setText(m.group(2));
				int level = Integer.parseInt(m.group(3));
				panel.levelFormattedTextField.setValue(level);
				
				Matcher params = TrainerBattleTrigger.parameterPattern.matcher(m.group(4));

				while (params.find())
				{
					if (params.group(1) != null) panel.shinyCheckBox.setSelected(true);
					if (params.group(2) != null)
					{
						panel.moveCheckBox.setSelected(true);
						panel.moveComboBox.setEnabled(true);
						panel.moveTextField.setEnabled(true);
						for (int i = 0; i < 4; ++i)
						{
							if (!params.group(3 + i).equals("None"))
							{
								panel.customMoves[i] = params.group(3 + i);
							}
						}
					}
				}
			}
	
			if (m.group(5) != null)
			{
				nameTextField.setText(m.group(6));
			}
			
			if (m.group(9) != null)
			{
				try 
				{
					cashFormattedTextField.setValue(Integer.parseInt(m.group(10)));
				}
				catch (NumberFormatException e) 
				{
					cashFormattedTextField.setValue(100);
				}
			}
		}
		
	}
	
	public String getTrainerData() 
	{	
		StringBuilder data = new StringBuilder();
		
		data.append("name: " + nameTextField.getText().trim() +"\n");
		data.append("cash: " + ("" + cashFormattedTextField.getValue()).trim() +"\n");
		
		for (PokemonDataPanel panel: pokemonPanels) 
		{
			String pokemonData = panel.getPokemonData();
			if (pokemonData != null) 
			{
				data.append(pokemonData +"\n");
			}
		}
		
		return data.toString().trim();
	}
}
