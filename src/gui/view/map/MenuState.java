package gui.view.map;

import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.panel.DrawPanel;
import gui.view.map.VisualState.VisualStateHandler;
import input.ControlKey;
import input.InputControl;
import main.Global;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;

class MenuState implements VisualStateHandler {
    private final DrawPanel menuPanel;
    
    private final Button[] menuButtons;
    
    private int selectedButton;
    
    MenuState() {
        int width = 273;
        menuPanel = new DrawPanel(Global.GAME_SIZE.width - width, 0, width, Global.GAME_SIZE.height)
                .withBorderColor(new Color(53, 53, 129))
                .withBorderPercentage(5);
                
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
        menuPanel.drawBackground(g);
        
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
