package draw.layout;

import draw.DrawUtils;
import draw.button.Button;
import draw.button.ButtonPanel.ButtonPanelSetup;
import draw.button.ButtonTransitions;
import draw.layout.ButtonLayout.ButtonIndexAction;
import draw.panel.DrawPanel;

public class TabLayout {
    private final DrawPanel panel;
    private final int numTabs;
    private final int tabHeight;

    private boolean isBottomTab;
    private boolean isInsetTab;

    private int startIndex;
    private ButtonTransitions defaultTransitions;
    private ButtonIndexAction indexAction;
    private ButtonPanelSetup buttonSetup;

    public TabLayout(DrawPanel panel, int numTabs, int tabHeight) {
        this.panel = panel;
        this.numTabs = numTabs;
        this.tabHeight = tabHeight;

        // Default values
        this.isBottomTab = false;
        this.isInsetTab = false;
        this.startIndex = 0;
        this.defaultTransitions = null;
        this.indexAction = index -> {};
        this.buttonSetup = buttonPanel -> {};
    }

    public TabLayout asBottomTabs() {
        this.isBottomTab = true;
        return this;
    }

    // Inset is true if the button should overlap with the panel
    public TabLayout asInsetTabs() {
        this.isInsetTab = true;
        return this;
    }

    public TabLayout withStartIndex(int startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    public TabLayout withDefaultTransitions(ButtonTransitions defaultTransitions) {
        this.defaultTransitions = defaultTransitions;
        return this;
    }

    public TabLayout withPressIndex(ButtonIndexAction indexAction) {
        this.indexAction = indexAction;
        return this;
    }

    public TabLayout withButtonSetup(ButtonPanelSetup buttonSetup) {
        this.buttonSetup = this.buttonSetup.add(buttonSetup);
        return this;
    }

    public Button[] getTabs() {
        int tabWidth = panel.width/numTabs;
        int remainder = panel.width%numTabs;

        int offset = isInsetTab ? tabHeight - DrawUtils.OUTLINE_SIZE : 0;
        int y = isBottomTab ? panel.bottomY() - DrawUtils.OUTLINE_SIZE - offset
                            : panel.y - tabHeight + DrawUtils.OUTLINE_SIZE + offset;

        Button[] tabs = new Button[this.numTabs];
        for (int i = 0; i < tabs.length; i++) {
            final int index = i;
            tabs[i] = new Button(
                    panel.x + i*tabWidth + Math.min(i, remainder),
                    y,
                    tabWidth + (i < remainder ? 1 : 0),
                    tabHeight,
                    ButtonTransitions.getBasicTransitions(i, 1, numTabs, startIndex, defaultTransitions),
                    () -> indexAction.pressButton(index),
                    buttonSetup
            );
        }

        return tabs;
    }
}
