package gui.view.map;

import battle.Battle;

import java.awt.Graphics;

public enum VisualState {
    BATTLE(new BattleState()),
    FLY(new FlyState()),
    MAP(new MapState()),
    MENU(new MenuState()),
    MESSAGE(new MessageState()),
    POKEFINDER(new PokeFinderState());

    private final VisualStateHandler handler;

    VisualState(VisualStateHandler handler) {
        this.handler = handler;
    }

    interface VisualStateHandler {
        void draw(Graphics g, MapView mapView);
        void update(int dt, MapView mapView);
        default void set(MapView mapView) {}
    }

    public void draw(Graphics g, MapView mapView) {
        this.handler.draw(g, mapView);
    }

    public void update(int dt, MapView mapView) {
        this.handler.update(dt, mapView);
    }

    public void set(MapView mapView) {
        this.handler.set(mapView);
    }

    public static void setBattle(Battle battle, boolean seenWild) {
        ((BattleState)BATTLE.handler).setBattle(battle, seenWild);
    }

    public static boolean hasBattle() {
        return ((BattleState)BATTLE.handler).hasBattle();
    }
}
