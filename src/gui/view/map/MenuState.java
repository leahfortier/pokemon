package gui.view.map;

import draw.Alignment;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.button.ButtonList;
import draw.layout.ButtonLayout;
import draw.panel.DrawPanel;
import input.ControlKey;
import input.InputControl;
import main.Global;
import trainer.player.Player;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;

class MenuState extends VisualStateHandler {
    private static final MenuChoice[] MENU_CHOICES = MenuChoice.values();

    private final DrawPanel menuPanel;
    private final ButtonList buttons;
    private final Button[] menuButtons;

    MenuState() {
        int fontSize = 40;
        int borderSize = 13;

        // Spacing factor is set to that there is equal spacing on either side of the arrow
        int arrowSpacing = 10;
        float spacingFactor = (float)arrowSpacing/FontMetrics.getTextWidth(fontSize);
        int buttonSpacing = arrowSpacing + ButtonHoverAction.ARROW_WIDTH;
        int fullSpacing = buttonSpacing + arrowSpacing;

        // Equal spacing between the text on both sides for max characters (arrow is inside this space)
        int textWidth = FontMetrics.getTextWidth(fontSize, Player.MAX_NAME_LENGTH);
        int width = 2*borderSize + 2*fullSpacing + textWidth;

        menuPanel = new DrawPanel(Global.GAME_SIZE.width - width, 0, width, Global.GAME_SIZE.height)
                .withBorderColor(new Color(53, 53, 129))
                .withBorderSize(borderSize);

        menuButtons = new ButtonLayout(menuPanel, MENU_CHOICES.length, 1, buttonSpacing)
                .withArrowHover()
                .withPressIndex(index -> MENU_CHOICES[index].execute(view))
                .withDrawSetup((panel, index) -> panel.withNoBackground()
                                                      .withLabelSize(fontSize, Alignment.LEFT)
                                                      .withLabelSpacingFactor(spacingFactor))
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

    @Override
    public void set() {
        for (int i = 0; i < menuButtons.length; i++) {
            menuButtons[i].panel().withLabel(MENU_CHOICES[i].getDisplayName());
        }
    }
}
