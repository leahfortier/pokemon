package gui.view.map;

import battle.Battle;

import java.awt.Graphics;

public enum VisualState {
    BATTLE(new BattleState()),
    FLY(new FlyState()),
    MAP(new MapState()),
    MEDAL_CASE(new MedalCaseState()),
    MENU(new MenuState()),
    MESSAGE(new MessageState()),
    POKE_FINDER(new PokeFinderState());

    private final VisualStateHandler handler;

    VisualState(VisualStateHandler handler) {
        this.handler = handler;
    }

    public VisualStateHandler handler() {
        return this.handler;
    }

    public static void setBattle(Battle battle, boolean seenWild) {
        ((BattleState)BATTLE.handler()).setBattle(battle, seenWild);
    }

    public static boolean hasBattle() {
        return ((BattleState)BATTLE.handler()).hasBattle();
    }

    interface VisualStateHandler {
        void draw(Graphics g, MapView mapView);
        void update(int dt, MapView mapView);
        default void set(MapView mapView) {}
    }
}
