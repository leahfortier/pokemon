package mapMaker.dialogs.wildbattle;

import map.overworld.WildEncounter;
import mapMaker.dialogs.TriggerDialog;
import pattern.map.FishingMatcher;
import pokemon.ActivePokemon;
import util.GUIUtils;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FishingTriggerEditDialog extends TriggerDialog<FishingMatcher> {
    private static final long serialVersionUID = -3454589908432207758L;
    
    private final JPanel topComponent;
    private final JPanel bottomComponent;
    
    private final JTextField nameTextField;
    private final JFormattedTextField lowLevelFormattedTextField;
    private final JFormattedTextField highLevelFormattedTextField;
    private final List<WildPokemonDataPanel> wildPokemonPanels;
    
    private final int index;
    
    public FishingTriggerEditDialog(FishingMatcher fishingMatcher, int index) {
        super("Fishing Trigger Editor");
        
        this.index = index;
        
        wildPokemonPanels = new ArrayList<>();
        
        nameTextField = GUIUtils.createTextField(this.getDefaultName());
        
        lowLevelFormattedTextField = GUIUtils.createIntegerTextField(1, 1, ActivePokemon.MAX_LEVEL);
        highLevelFormattedTextField = GUIUtils.createIntegerTextField(ActivePokemon.MAX_LEVEL, 1, ActivePokemon.MAX_LEVEL);
        
        JButton addPokemonButton = GUIUtils.createButton("Add Pokemon", event -> addPokemonPanel(null));
        JButton removeSelectedButton = GUIUtils.createButton(
                "Remove Selected",
                event -> {
                    wildPokemonPanels.removeIf(WildPokemonDataPanel::isSelected);
                    render();
                }
        );
        
        this.topComponent = GUIUtils.createHorizontalLayoutComponent(
                GUIUtils.createTextFieldComponent("Name", nameTextField),
                lowLevelFormattedTextField,
                highLevelFormattedTextField
        );
        
        this.bottomComponent = GUIUtils.createHorizontalLayoutComponent(
                addPokemonButton,
                removeSelectedButton
        );
        
        this.load(fishingMatcher);
    }
    
    private void addPokemonPanel(WildEncounter wildEncounter) {
        wildPokemonPanels.add(new WildPokemonDataPanel(wildEncounter));
        render();
    }
    
    @Override
    protected void renderDialog() {
        removeAll();
        
        List<JComponent> components = new ArrayList<>();
        components.add(topComponent);
        if (!wildPokemonPanels.isEmpty()) {
            components.add(GUIUtils.createLabel("     Pokemon Name                   Probability"));
        }
        components.addAll(wildPokemonPanels);
        components.add(bottomComponent);
        
        GUIUtils.setVerticalLayout(this, components.toArray(new JComponent[0]));
    }
    
    private String getDefaultName() {
        return "Fishing Spot " + index;
    }
    
    @Override
    protected FishingMatcher getMatcher() {
        if (wildPokemonPanels.isEmpty()) {
            return null;
        }
        
        String name = this.getNameField(nameTextField, this.getDefaultName());
        int minLevel = Integer.parseInt(lowLevelFormattedTextField.getText());
        int maxLevel = Integer.parseInt(highLevelFormattedTextField.getText());
        this.updatePokemonPanelsWithLevels(minLevel, maxLevel);
        List<WildEncounter> wildEncounters = wildPokemonPanels
                .stream()
                .map(WildPokemonDataPanel::getWildEncounter)
                .collect(Collectors.toList());
        
        return new FishingMatcher(name, minLevel, maxLevel, wildEncounters);
    }
    
    private void load(FishingMatcher matcher) {
        if (matcher == null) {
            return;
        }
        
        nameTextField.setText(matcher.getBasicName());
        lowLevelFormattedTextField.setValue(matcher.getMinLevel());
        highLevelFormattedTextField.setValue(matcher.getMaxLevel());
        for (WildEncounter wildEncounter : matcher.getWildEncounters()) {
            addPokemonPanel(wildEncounter);
        }
    }
    
    private void updatePokemonPanelsWithLevels(int minLevel, int maxLevel) {
        for (WildPokemonDataPanel wildPokemonDataPanel : this.wildPokemonPanels) {
            wildPokemonDataPanel.setMinAndMaxLevel(minLevel, maxLevel);
        }
    }
}
