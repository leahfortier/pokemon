package gui.view.map;

import gui.Button;
import gui.GameData;
import gui.ButtonHoverAction;
import gui.TileSet;
import gui.view.map.VisualState.VisualStateHandler;
import input.ControlKey;
import input.InputControl;
import main.Game;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;

class MenuState implements VisualStateHandler {
    private final Button[] menuButtons;

    private int selectedButton;

    MenuState() {
        selectedButton = 0;
        menuButtons = new Button[MenuChoice.values().length];

        for (int i = 0; i < menuButtons.length; i++) {
            menuButtons[i] = new Button(
                    558, 72*i + 10,
                    240, 70,
                    ButtonHoverAction.ARROW,
                    Button.getBasicTransitions(i, menuButtons.length, 1));
        }
    }

    @Override
    public void draw(Graphics g, MapView mapView) {
        GameData data = Game.getData();
        TileSet menuTiles = data.getMenuTiles();

        g.drawImage(menuTiles.getTile(1), 527, 0, null);
        FontMetrics.setFont(g, 40);
        g.setColor(Color.BLACK);

        for (MenuChoice menuChoice : MenuChoice.values()) {
            g.drawString(menuChoice.getDisplayName(), 558, 59 + 72*menuChoice.ordinal());
        }

        for (Button button: menuButtons) {
            button.draw(g);
        }
    }

    @Override
    public void update(int dt, MapView mapView) {
        InputControl input = InputControl.instance();
        selectedButton = Button.update(menuButtons, selectedButton);

        int clicked = -1;
        for (int i = 0; i < menuButtons.length; i++) {
            if (menuButtons[i].checkConsumePress()) {
                clicked = i;
            }
        }

        if (clicked != -1) {
            MenuChoice menuChoice = MenuChoice.values()[clicked];
            menuChoice.execute(mapView);
        }

        if (input.consumeIfDown(ControlKey.ESC)) {
            mapView.setState(VisualState.MAP);
        }
    }
}
