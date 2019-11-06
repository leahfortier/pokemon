package mapMaker.dialogs.interaction;

import mapMaker.dialogs.TriggerDialog;
import pattern.interaction.InteractionMatcher;
import util.GuiUtils;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class InteractionListPanel<InteractionType extends InteractionMatcher> extends JPanel {
    private final TriggerDialog parent;

    private final List<InteractionType> interactions;
    private final JButton addInteractionButton;

    protected InteractionListPanel(TriggerDialog parent) {
        this.parent = parent;

        interactions = new ArrayList<>();
        addInteractionButton = GuiUtils.createButton(
                "Add Interaction",
                event -> {
                    interactions.add(null);
                    render();
                }
        );

        render();
    }

    protected abstract InteractionDialog<InteractionType> getInteractionDialog(InteractionType matcher, int index);

    private void render() {
        removeAll();

        List<JComponent> components = new ArrayList<>();
        for (int i = 0; i < interactions.size(); i++) {
            final int index = i;
            final InteractionType matcher = interactions.get(index);

            JButton interactionButton = GuiUtils.createButton(
                    matcher == null ? "Empty" : matcher.getName(),
                    event -> {
                        InteractionType newMatcher = this.getInteractionDialog(matcher, index).getMatcher(parent);
                        if (newMatcher != null) {
                            interactions.set(index, newMatcher);
                            render();
                        }
                    }
            );

            JButton deleteButton = GuiUtils.createButton(
                    "Delete",
                    event -> {
                        interactions.remove(index);
                        render();
                    }
            );

            components.add(GuiUtils.createHorizontalLayoutComponent(interactionButton, deleteButton));
        }

        components.add(addInteractionButton);
        GuiUtils.setVerticalLayout(this, components.toArray(new JComponent[0]));

        parent.render();
    }

    public void load(List<InteractionType> interactions) {
        this.interactions.addAll(interactions);
        render();
    }

    public List<InteractionType> getInteractions() {
        this.interactions.removeIf(Objects::isNull);
        return this.interactions;
    }
}
