package gui.view.map;

import draw.Alignment;
import draw.button.Button;
import draw.button.ButtonList;
import draw.layout.ButtonLayout;
import draw.panel.DrawPanel;
import input.ControlKey;
import input.InputControl;
import main.Global;

import java.awt.Color;
import java.awt.Graphics;

class MenuState extends VisualStateHandler {
    private static final MenuChoice[] MENU_CHOICES = MenuChoice.values();

    private final DrawPanel menuPanel;
    private final ButtonList buttons;

    MenuState() {
        int width = 273;
        menuPanel = new DrawPanel(Global.GAME_SIZE.width - width, 0, width, Global.GAME_SIZE.height)
                .withBorderColor(new Color(53, 53, 129))
                .withBorderPercentage(5);

        Button[] menuButtons = new ButtonLayout(menuPanel, MENU_CHOICES.length, 1, 20)
                .withArrowHover()
                .withPressIndex(index -> MENU_CHOICES[index].execute(view))
                .withDrawSetup((panel, index) -> panel.withNoBackground()
                                                      .withLabel(MENU_CHOICES[index].getDisplayName(), 40, Alignment.LEFT)
                                                      .withLabelSpacingFactor(.5f))
                .getButtons();

        this.buttons = new ButtonList(menuButtons);
    }

    @Override
    public void draw(Graphics g) {
        menuPanel.drawBackground(g);
        buttons.draw(g);
    }

    @Override
    public void update(int dt) {
        buttons.update();
        buttons.consumeSelectedPress();

        InputControl input = InputControl.instance();
        if (input.consumeIfDown(ControlKey.ESC)) {
            view.setState(VisualState.MAP);
        }
    }
}
