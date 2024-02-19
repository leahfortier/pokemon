package gui.view;

import draw.button.Button;
import draw.button.ButtonList;
import draw.button.ButtonPressAction;
import draw.button.ButtonTransitions;
import draw.layout.ButtonLayout;
import draw.layout.DrawLayout;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.PanelList;
import gui.view.item.BagLayout;
import input.InputControl;
import item.bag.BagCategory;
import main.Game;
import trainer.player.Options;

import java.awt.Color;
import java.awt.Graphics;
import java.util.function.Consumer;
import java.util.function.Function;

class OptionsView extends View {
    private enum OptionType {
        MUTED("Muted??", "Off", "On", BagCategory.MEDICINE.getColor(), Options::isMuted, Options::toggleMuted),
        BATTLE_MUSIC("Battle Music", "Off", "On", BagCategory.BALL.getColor(), Options::shouldPlayBattleMusic, Options::toggleBattleMusic),
        ANIMATION_SPEED("Animation Speed", "Default", "Fast", BagCategory.TM.getColor(), Options::isFastAnimationSpeed, Options::toggleAnimationSpeed);

        private final String name;
        private final String on;
        private final String off;
        private final Color color;

        private final Function<Options, Boolean> isOn;
        private final Consumer<Options> toggle;
        OptionType(String name, String off, String on, Color color, Function<Options, Boolean> isOn, Consumer<Options> toggle) {
            this.name = name;
            this.off = off;
            this.on = on;
            this.color = color;

            this.isOn = isOn;
            this.toggle = toggle;
        }
    }

    private static final int NUM_OPTIONS = OptionType.values().length;
    private static final int NUM_BUTTONS = NUM_OPTIONS + 1;
    private static final int RETURN = NUM_OPTIONS;

    private static final Color BACKGROUND_COLOR = BagCategory.MISC.getColor();

    private final PanelList panels;
    private final ButtonList buttons;

    private final DrawPanel[] subPanels;

    private final Options options;

    OptionsView() {
        this.options = Game.getPlayer().getOptions();

        BagLayout layout = new BagLayout(true);
        layout.bagPanel.withBackgroundColor(BACKGROUND_COLOR);

        int spacing = layout.spacing;

        DrawPanel tabPanel = layout.getTabPanel(BagCategory.MISC.ordinal(), BACKGROUND_COLOR, "Options");

        DrawPanel mainPanel = new DrawPanel(
                layout.leftPanel.x,
                layout.leftPanel.y,
                layout.bagPanel.width - 2*spacing,
                layout.leftPanel.height*5/6
        )
                .withFullTransparency()
                .withBlackOutline();

        Button returnButton = new Button(
                mainPanel.x,
                mainPanel.bottomY() + spacing,
                mainPanel.width,
                layout.bagPanel.height - mainPanel.height - 3*spacing,
                new ButtonTransitions().up(NUM_OPTIONS - 1).down(0),
                ButtonPressAction.getExitAction(),
                panel -> panel.withFullTransparency()
                        .withBackgroundColor(Color.YELLOW)
                        .withTransparentCount(2)
                        .withBlackOutline()
                        .withLabel("Return", 20)
        );

        DrawPanel[] optionPanels = new DrawLayout(mainPanel, NUM_OPTIONS, 1, spacing)
                .withMissingRightCols(1)
                .getPanels();

        Button[] toggleButtons = new ButtonLayout(mainPanel, NUM_OPTIONS, 1, spacing)
                .withMissingLeftCols(1)
                .withPressIndex((row, col) -> OptionType.values()[row].toggle.accept(options))
                .withDefaultTransitions(new ButtonTransitions().up(RETURN).down(RETURN))
                .getButtons();

        subPanels = new DrawPanel[NUM_OPTIONS*2];
        for (OptionType option : OptionType.values()) {
            int i = option.ordinal();
            optionPanels[i].withLabel(option.name, 26)
                    .withBlackOutline()
                    .withBorderColor(option.color)
                    .withBorderPercentage(15)
                    .withBackgroundColor(Color.WHITE);

            DrawPanel[] buttonPanels = new DrawLayout(toggleButtons[i].panel(), 1, 2, 10)
                    .withDrawSetup(panel -> panel.withBlackOutline()
                            .withTransparentBackground()
                            .withTransparentCount(2)
                            .withBorderPercentage(10)
                            .withLabelSize(20))
                    .getPanels();

            subPanels[i*2] = buttonPanels[0].withLabel(option.off).withBackgroundColor(new Color(255, 215, 0));
            subPanels[i*2 + 1] = buttonPanels[1].withLabel(option.on).withBackgroundColor(new Color(35, 120, 220));
        }

        panels = new PanelList(
                layout.bagPanel, tabPanel, mainPanel
        ).add(optionPanels).add(subPanels);

        buttons = new ButtonList(NUM_BUTTONS);
        buttons.set(0, toggleButtons);
        buttons.set(RETURN, returnButton);
    }

    @Override
    public void update(int dt) {
        InputControl input = InputControl.instance();

        buttons.update();
        if (buttons.consumeSelectedPress()) {
            updateActiveButtons();
        }

        input.popViewIfEscaped();
    }

    private void updateActiveButtons() {

    }

    @Override
    public void draw(Graphics g) {
        BasicPanels.drawCanvasPanel(g);
        panels.drawAll(g);

        for (OptionType option : OptionType.values()) {
            DrawPanel off = subPanels[option.ordinal()*2];
            DrawPanel on = subPanels[option.ordinal()*2 + 1];

            boolean isOn = option.isOn.apply(options);
            off.withGreyOut(isOn);
            on.withGreyOut(!isOn);
        }

        buttons.draw(g);
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.OPTIONS_VIEW;
    }

    @Override
    public void movedToFront() {}
}
