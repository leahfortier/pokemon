package gui.view.map;

import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.button.ButtonList;
import draw.button.ButtonTransitions;
import draw.panel.DrawPanel;
import input.ControlKey;
import input.InputControl;
import main.Global;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;

class MenuState extends VisualStateHandler {
    private final DrawPanel menuPanel;
    private final ButtonList menuButtons;

    MenuState() {
        int width = 273;
        menuPanel = new DrawPanel(Global.GAME_SIZE.width - width, 0, width, Global.GAME_SIZE.height)
                .withBorderColor(new Color(53, 53, 129))
                .withBorderPercentage(5);

        Button[] menuButtons = new Button[MenuChoice.values().length];
        for (int i = 0; i < menuButtons.length; i++) {
            menuButtons[i] = new Button(
                    558, 72*i + 10,
                    240, 70,
                    ButtonHoverAction.ARROW,
                    ButtonTransitions.getBasicTransitions(i, menuButtons.length, 1)
            );
        }

        this.menuButtons = new ButtonList(menuButtons);
    }

    @Override
    public void draw(Graphics g) {
        menuPanel.drawBackground(g);

        FontMetrics.setBlackFont(g, 40);
        for (MenuChoice menuChoice : MenuChoice.values()) {
            g.drawString(menuChoice.getDisplayName(), 558, 59 + 72*menuChoice.ordinal());
        }

        menuButtons.drawHover(g);
    }

    @Override
    public void update(int dt) {
        InputControl input = InputControl.instance();
        menuButtons.update();

        for (int i = 0; i < menuButtons.size(); i++) {
            if (menuButtons.get(i).checkConsumePress()) {
                MenuChoice menuChoice = MenuChoice.values()[i];
                menuChoice.execute(view);
            }
        }

        if (input.consumeIfDown(ControlKey.ESC)) {
            view.setState(VisualState.MAP);
        }
    }
}
