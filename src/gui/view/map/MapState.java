package gui.view.map;

import gui.view.map.VisualState.VisualStateHandler;
import input.ControlKey;
import input.InputControl;
import main.Game;
import map.overworld.OverworldTool;
import trainer.Player;

import java.awt.Graphics;

class MapState implements VisualStateHandler {
    @Override
    public void draw(Graphics g, MapView mapView) {}

    @Override
    public void update(int dt, MapView mapView) {
        InputControl input = InputControl.instance();
        Player player = Game.getPlayer();

        if (input.consumeIfDown(ControlKey.ESC)) {
            mapView.setState(VisualState.MENU);
        }
        else if (input.consumeIfDown(ControlKey.FLY) && player.hasTool(OverworldTool.FLY)) {
            mapView.setState(VisualState.FLY);
        }
        else if (input.consumeIfDown(ControlKey.POKEFINDER) && player.hasTool(OverworldTool.POKEFINDER)) {
            mapView.setState(VisualState.POKEFINDER);
        }
        else if (input.consumeIfDown(ControlKey.BIKE)) {
            player.toggleBicycle();
        }
    }
}
