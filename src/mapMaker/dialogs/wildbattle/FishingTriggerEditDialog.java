package mapMaker.dialogs.wildbattle;

import map.overworld.WildEncounter;
import mapMaker.dialogs.TriggerDialog;
import pattern.map.FishingMatcher;
import util.GUIUtils;

import javax.swing.JButton;
import javax.swing.JComponent;
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
    private final List<WildPokemonDataPanel> wildPokemonPanels;

    private final int index;

    public FishingTriggerEditDialog(FishingMatcher fishingMatcher, int index) {
        super("Fishing Trigger Editor");

        this.index = index;

        wildPokemonPanels = new ArrayList<>();

        nameTextField = GUIUtils.createTextField(this.getDefaultName());

        JButton addPokemonButton = GUIUtils.createButton("Add Pokemon", event -> addPokemonPanel(null));
        JButton removeSelectedButton = GUIUtils.createButton(
                "Remove Selected",
                event -> {
                    wildPokemonPanels.removeIf(WildPokemonDataPanel::isSelected);
                    render();
                }
        );

        this.topComponent = GUIUtils.createHorizontalLayoutComponent(
                GUIUtils.createTextFieldComponent("Name", nameTextField)
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
            components.add(GUIUtils.createLabel("     Pokemon Name     Probability       Min Level        Max Level"));
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
        List<WildEncounter> wildEncounters = wildPokemonPanels
                .stream()
                .map(WildPokemonDataPanel::getWildEncounter)
                .collect(Collectors.toList());

        return new FishingMatcher(name, wildEncounters);
    }

    private void load(FishingMatcher matcher) {
        if (matcher == null) {
            return;
        }

        nameTextField.setText(matcher.getBasicName());
        for (WildEncounter wildEncounter : matcher.getWildEncounters()) {
            addPokemonPanel(wildEncounter);
        }
    }
}
