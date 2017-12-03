package gui.view.map;

import draw.panel.DrawPanel;
import gui.view.map.VisualState.VisualStateHandler;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import map.entity.ItemEntity;
import map.overworld.OverworldTool;
import trainer.player.Player;
import util.Point;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

class MapState implements VisualStateHandler {
    private final DrawPanel itemFinderPanel;

    public MapState() {
        int itemFinderLength = 50;
        this.itemFinderPanel = new DrawPanel(Global.GAME_SIZE.width - itemFinderLength, 0, itemFinderLength, itemFinderLength)
                .withBorderColor(Color.WHITE)
                .withBlackOutline();
    }

    @Override
    public void draw(Graphics g, MapView mapView) {
        if (showItemFinder) {
            Point playerLocation = Game.getPlayer().getLocation();
            List<ItemEntity> hiddenItems = mapView.getCurrentMap().getHiddenItems();

            int minDistance = 11;
            for (ItemEntity item : hiddenItems) {
                minDistance = Math.min(Point.distance(playerLocation, item.getLocation()), minDistance);
            }

            final Color finderColor;
            if (minDistance == 0) {
                finderColor = new Color(255, 113, 166);
            }
            else if (minDistance == 1) {
                finderColor = new Color(250, 81, 37);
            }
            else if (minDistance <= 3) {
                finderColor = new Color(255, 199, 8);
            }
            else if (minDistance <= 5) {
                finderColor = new Color(123, 213, 74);
            }
            else if (minDistance <= 10){
                finderColor = new Color(48, 158, 255);
            }
            else {
                finderColor = Color.WHITE;
            }

            this.itemFinderPanel.withBackgroundColor(finderColor).drawBackground(g);
        }
    }

    private boolean showItemFinder;

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
            mapView.setState(VisualState.POKE_FINDER);
        }
        else if (input.consumeIfDown(ControlKey.BIKE)) {
            player.toggleBicycle();
        }
        else if (input.consumeIfDown(ControlKey.ITEM_FINDER) && player.hasTool(OverworldTool.ITEM_FINDER)) {
            showItemFinder = !showItemFinder;
        }
        else if (input.consumeIfDown(ControlKey.MEDAL_CASE)) {
            mapView.setState(VisualState.MEDAL_CASE);
        }
    }
}
