package gui.view.map;

import battle.Battle;
import input.ControlKey;
import input.InputControl;
import map.weather.WeatherState;
import map.overworld.TerrainType;

import java.awt.Graphics;

enum VisualState {
    BATTLE(new BattleState()),
    MENU(new MenuState()),
    MESSAGE(new MessageState()),
    MAP(new VisualStateHandler() {
        @Override public void draw(Graphics g, MapView mapView) {}

        @Override
        public void update(int dt, MapView mapView) {
            InputControl input = InputControl.instance();
            if (input.consumeIfDown(ControlKey.ESC)) {
                mapView.setState(MENU);
            }
        }
    });

    private final VisualStateHandler handler;

    VisualState(VisualStateHandler handler) {
        this.handler = handler;
    }

    interface VisualStateHandler {
        void draw(Graphics g, MapView mapView);
        void update(int dt, MapView mapView);
    }

    public void draw(Graphics g, MapView mapView) {
        this.handler.draw(g, mapView);
    }

    public void update(int dt, MapView mapView) {
        this.handler.update(dt, mapView);
    }

    public static void setBattle(Battle battle, boolean seenWild, WeatherState weather, TerrainType terrain) {
        ((BattleState)BATTLE.handler).setBattle(battle, seenWild, weather, terrain);
    }

    public static boolean hasBattle() {
        return ((BattleState)BATTLE.handler).hasBattle();
    }
}
