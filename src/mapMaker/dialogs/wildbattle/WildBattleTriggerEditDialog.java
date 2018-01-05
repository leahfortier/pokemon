package mapMaker.dialogs.wildbattle;

import map.condition.Condition;
import map.overworld.EncounterRate;
import map.overworld.WildEncounter;
import mapMaker.dialogs.TimeOfDayPanel;
import mapMaker.dialogs.TriggerDialog;
import pattern.map.WildBattleMatcher;
import pokemon.ActivePokemon;
import util.GUIUtils;
import util.GeneralUtils;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class WildBattleTriggerEditDialog extends TriggerDialog<WildBattleMatcher> implements PokemonProbabilityListener {
    private static final int POKES_PER_PAGE = 5;
    
    private final JPanel topComponent;
    private final JPanel bottomComponent;
    
    private final JTextField nameTextField;
    private final JComboBox<EncounterRate> encounterRateComboBox;
    
    private final JFormattedTextField lowLevelFormattedTextField;
    private final JFormattedTextField highLevelFormattedTextField;
    
    private final TimeOfDayPanel timeOfDayPanel;
    private final JTextField conditionTextField;
    
    private final JLabel pokemonProbabilitySumLabel;
    private final List<WildPokemonDataPanel> wildPokemonPanels;
    
    private final JLabel pageNumLabel;
    private int pageNum;
    private final int index;
    private int probabilitySum;
    
    public WildBattleTriggerEditDialog(WildBattleMatcher wildBattleMatcher, int index) {
        super("Wild Battle Trigger Editor");
        
        this.index = index;
        this.probabilitySum = 0;
        
        wildPokemonPanels = new ArrayList<>();
        
        nameTextField = GUIUtils.createTextField(this.getDefaultName());
        encounterRateComboBox = GUIUtils.createComboBox(EncounterRate.values());
        
        lowLevelFormattedTextField = GUIUtils.createIntegerTextField(1, 1, ActivePokemon.MAX_LEVEL);
        highLevelFormattedTextField = GUIUtils.createIntegerTextField(ActivePokemon.MAX_LEVEL, 1, ActivePokemon.MAX_LEVEL);
        
        timeOfDayPanel = new TimeOfDayPanel();
        conditionTextField = GUIUtils.createTextField();
        
        JButton addPokemonButton = GUIUtils.createButton("Add Pokemon", event -> addPokemonPanel(null));
        JButton removeSelectedButton = GUIUtils.createButton(
                "Remove Selected",
                event -> {
                    wildPokemonPanels.removeIf(WildPokemonDataPanel::isSelected);
                    render();
                }
        );
        
        JButton lefty = GUIUtils.createButton("<", event -> {
            pageNum = GeneralUtils.wrapIncrement(pageNum, -1, getTotalPokemonPanelPages());
            renderDialog();
        });
        
        JButton righty = GUIUtils.createButton(">", event -> {
            pageNum = GeneralUtils.wrapIncrement(pageNum, 1, getTotalPokemonPanelPages());
            renderDialog();
        });
        
        pokemonProbabilitySumLabel = GUIUtils.createLabel("0");
        
        this.topComponent = GUIUtils.createVerticalLayoutComponent(
                GUIUtils.createHorizontalLayoutComponent(
                        GUIUtils.createTextFieldComponent("Name", nameTextField),
                        GUIUtils.createComboBoxComponent("Encounter Rate", encounterRateComboBox),
                        lowLevelFormattedTextField,
                        highLevelFormattedTextField
                ),
                GUIUtils.createHorizontalLayoutComponent(
                        timeOfDayPanel,
                        GUIUtils.createTextFieldComponent("Condition", conditionTextField)
                ),
                GUIUtils.createHorizontalLayoutComponent(
                        GUIUtils.createLabel("Probability:"),
                        pokemonProbabilitySumLabel
                )
        );
        
        
        pageNumLabel = GUIUtils.createLabel("");
        this.bottomComponent = GUIUtils.createHorizontalLayoutComponent(
                addPokemonButton,
                removeSelectedButton,
                lefty,
                pageNumLabel,
                righty
        );
        
        this.load(wildBattleMatcher);
    }
    
    private int getTotalPokemonPanelPages() {
        return (int)(Math.ceil(this.wildPokemonPanels.size()/(double)POKES_PER_PAGE));
    }
    
    private void addPokemonPanel(WildEncounter wildEncounter) {
        WildPokemonDataPanel panel = new WildPokemonDataPanel(wildEncounter);
        panel.setProbabilityListener(this);
        wildPokemonPanels.add(panel);
        render();
    }
    
    @Override
    protected void renderDialog() {
        removeAll();
        
        List<JComponent> components = new ArrayList<>();
        components.add(topComponent);
        
        if (!wildPokemonPanels.isEmpty()) {
            components.add(GUIUtils.createLabel("     Pokemon Name                       Probability"));
        }
        
        Iterator<WildPokemonDataPanel> displayPokes = GeneralUtils.pageIterator(
                wildPokemonPanels,
                pageNum,
                POKES_PER_PAGE
        );
        for (int i = 0; i < POKES_PER_PAGE && displayPokes.hasNext(); i++) {
            components.add(displayPokes.next());
        }
        components.add(bottomComponent);
        
        this.pageNumLabel.setText((pageNum + 1) + "/" + getTotalPokemonPanelPages());
        
        GUIUtils.setVerticalLayout(this, components.toArray(new JComponent[0]));
    }
    
    private String getDefaultName() {
        return "Wild Battle " + index;
    }
    
    @Override
    protected WildBattleMatcher getMatcher() {
        if (wildPokemonPanels.isEmpty()) {
            return null;
        }
        
        String name = this.getNameField(nameTextField, this.getDefaultName());
        EncounterRate encounterRate = (EncounterRate)encounterRateComboBox.getSelectedItem();
        int minLevel = Integer.parseInt(lowLevelFormattedTextField.getText());
        int maxLevel = Integer.parseInt(highLevelFormattedTextField.getText());
        this.updatePokemonPanelsWithLevels(minLevel, maxLevel);
        List<WildEncounter> wildEncounters = wildPokemonPanels
                .stream()
                .map(WildPokemonDataPanel::getWildEncounter)
                .collect(Collectors.toList());
        String condition = Condition.and(timeOfDayPanel.getCondition(), conditionTextField.getText());
        
        WildBattleMatcher matcher = new WildBattleMatcher(
                name,
                encounterRate,
                minLevel,
                maxLevel,
                wildEncounters
        );
        matcher.setCondition(condition);
        
        return matcher;
    }
    
    private void load(WildBattleMatcher matcher) {
        if (matcher == null) {
            return;
        }
        
        nameTextField.setText(matcher.getName());
        encounterRateComboBox.setSelectedItem(matcher.getEncounterRate());
        conditionTextField.setText(matcher.getCondition());
        lowLevelFormattedTextField.setValue(matcher.getMinLevel());
        highLevelFormattedTextField.setValue(matcher.getMaxLevel());
        
        for (WildEncounter wildEncounter : matcher.getWildEncounters()) {
            probabilitySum += wildEncounter.getProbability();
            addPokemonPanel(wildEncounter);
        }
        
        this.pokemonProbabilitySumLabel.setText(String.valueOf(probabilitySum));
    }
    
    private void updatePokemonPanelsWithLevels(int minLevel, int maxLevel) {
        for (WildPokemonDataPanel wildPokemonDataPanel : this.wildPokemonPanels) {
            wildPokemonDataPanel.setMinAndMaxLevel(minLevel, maxLevel);
        }
    }
    
    public void updatePokemonProbability(int oldProbability, int newProbability) {
        probabilitySum += (newProbability - oldProbability);
        this.pokemonProbabilitySumLabel.setText(String.valueOf(probabilitySum));
    }
}
