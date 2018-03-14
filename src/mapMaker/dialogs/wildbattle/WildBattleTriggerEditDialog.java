package mapMaker.dialogs.wildbattle;

import map.overworld.EncounterRate;
import map.overworld.WildEncounterInfo;
import mapMaker.dialogs.ConditionPanel;
import mapMaker.dialogs.TriggerDialog;
import pattern.map.WildBattleMatcher;
import pokemon.active.PartyPokemon;
import util.GeneralUtils;
import util.GuiUtils;

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

    private final ConditionPanel conditionPanel;

    private final JLabel pokemonProbabilitySumLabel;
    private final List<WildPokemonDataPanel> wildPokemonPanels;

    private final JLabel pageNumLabel;
    private final int index;

    private int pageNum;
    private int probabilitySum;

    public WildBattleTriggerEditDialog(WildBattleMatcher wildBattleMatcher, int index) {
        super("Wild Battle Trigger Editor");

        this.index = index;
        this.probabilitySum = 0;

        wildPokemonPanels = new ArrayList<>();

        nameTextField = GuiUtils.createTextField(this.getDefaultName());
        encounterRateComboBox = GuiUtils.createComboBox(EncounterRate.values());

        lowLevelFormattedTextField = GuiUtils.createIntegerTextField(1, 1, PartyPokemon.MAX_LEVEL);
        highLevelFormattedTextField = GuiUtils.createIntegerTextField(PartyPokemon.MAX_LEVEL, 1, PartyPokemon.MAX_LEVEL);

        conditionPanel = new ConditionPanel();

        JButton addPokemonButton = GuiUtils.createButton("Add Pokemon", event -> addPokemonPanel(null));
        JButton removeSelectedButton = GuiUtils.createButton(
                "Remove Selected",
                event -> {
                    wildPokemonPanels.removeIf(WildPokemonDataPanel::isSelected);
                    render();
                }
        );

        JButton lefty = GuiUtils.createButton("<", event -> {
            pageNum = GeneralUtils.wrapIncrement(pageNum, -1, getTotalPokemonPanelPages());
            renderDialog();
        });

        JButton righty = GuiUtils.createButton(">", event -> {
            pageNum = GeneralUtils.wrapIncrement(pageNum, 1, getTotalPokemonPanelPages());
            renderDialog();
        });

        pokemonProbabilitySumLabel = GuiUtils.createLabel("0");

        this.topComponent = GuiUtils.createVerticalLayoutComponent(
                GuiUtils.createHorizontalLayoutComponent(
                        GuiUtils.createTextFieldComponent("Name", nameTextField),
                        GuiUtils.createComboBoxComponent("Encounter Rate", encounterRateComboBox),
                        lowLevelFormattedTextField,
                        highLevelFormattedTextField
                ),
                GuiUtils.createHorizontalLayoutComponent(
                        conditionPanel
                ),
                GuiUtils.createHorizontalLayoutComponent(
                        GuiUtils.createLabel("Probability:"),
                        pokemonProbabilitySumLabel
                )
        );

        pageNumLabel = GuiUtils.createLabel("");
        this.bottomComponent = GuiUtils.createHorizontalLayoutComponent(
                addPokemonButton,
                removeSelectedButton,
                lefty,
                pageNumLabel,
                righty
        );

        this.load(wildBattleMatcher);
    }

    private int getTotalPokemonPanelPages() {
        return GeneralUtils.getTotalPages(this.wildPokemonPanels.size(), POKES_PER_PAGE);
    }

    private void addPokemonPanel(WildEncounterInfo wildEncounter) {
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
            components.add(GuiUtils.createLabel("     Pokemon Name                       Probability"));
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

        GuiUtils.setVerticalLayout(this, components.toArray(new JComponent[0]));
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
        List<WildEncounterInfo> wildEncounters = wildPokemonPanels
                .stream()
                .map(panel -> panel.getWildEncounter(minLevel, maxLevel))
                .collect(Collectors.toList());

        return new WildBattleMatcher(
                name,
                conditionPanel.getConditionName(),
                conditionPanel.getConditionSet(),
                encounterRate,
                wildEncounters
        );
    }

    private void load(WildBattleMatcher matcher) {
        if (matcher == null) {
            return;
        }

        nameTextField.setText(matcher.getName());
        encounterRateComboBox.setSelectedItem(matcher.getEncounterRate());

        conditionPanel.load(matcher);

        int minLevel = PartyPokemon.MAX_LEVEL;
        int maxLevel = 1;
        for (WildEncounterInfo wildEncounter : matcher.getWildEncounters()) {
            probabilitySum += wildEncounter.getProbability();
            addPokemonPanel(wildEncounter);

            minLevel = Math.min(minLevel, wildEncounter.getMinLevel());
            maxLevel = Math.max(maxLevel, wildEncounter.getMaxLevel());
        }

        lowLevelFormattedTextField.setValue(minLevel);
        highLevelFormattedTextField.setValue(maxLevel);

        pokemonProbabilitySumLabel.setText(String.valueOf(probabilitySum));
    }

    @Override
    public void updatePokemonProbability(int oldProbability, int newProbability) {
        probabilitySum += (newProbability - oldProbability);
        this.pokemonProbabilitySumLabel.setText(String.valueOf(probabilitySum));
    }
}
