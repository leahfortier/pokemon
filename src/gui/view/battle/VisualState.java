package gui.view.battle;

import draw.button.ButtonList;
import gui.view.battle.handler.BagState;
import gui.view.battle.handler.FightState;
import gui.view.battle.handler.LearnMoveState;
import gui.view.battle.handler.LogState;
import gui.view.battle.handler.MenuState;
import gui.view.battle.handler.MessageState;
import gui.view.battle.handler.PokemonState;
import gui.view.battle.handler.VisualStateHandler;
import item.ItemNamesies;
import message.MessageUpdate;

import java.awt.Graphics;

// Contains the different types of states a battle can be in
public enum VisualState {
    MESSAGE(new MessageState()),
    BAG(new BagState()),
    INVALID_BAG(BAG.visualStateHandler),
    FIGHT(new FightState()),
    INVALID_FIGHT(FIGHT.visualStateHandler),
    POKEMON(new PokemonState()),
    INVALID_POKEMON(POKEMON.visualStateHandler),
    MENU(new MenuState()),
    LEARN_MOVE(new LearnMoveState()),
    USE_ITEM(POKEMON.visualStateHandler),
    STAT_GAIN(MESSAGE.visualStateHandler),
    LOG_VIEW(new LogState());

    private final VisualStateHandler visualStateHandler;

    VisualState(VisualStateHandler visualStateHandler) {
        this.visualStateHandler = visualStateHandler;
    }

    public void update() {
        this.visualStateHandler.update();
    }

    public void set() {
        this.visualStateHandler.set();
    }

    public void draw(Graphics g) {
        this.visualStateHandler.draw(g);
    }

    public void reset(BattleView battleView) {
        this.visualStateHandler.reset(battleView);
    }

    public void checkMessage(MessageUpdate newMessage) {
        this.visualStateHandler.checkMessage(newMessage);
    }

    public ButtonList getButtons() {
        return this.visualStateHandler.getButtons();
    }

    public static ItemNamesies getSelectedItem() {
        return ((BagState)BAG.visualStateHandler).getSelectedItem();
    }

    public static void setSwitchForced() {
        ((PokemonState)POKEMON.visualStateHandler).setSwitchForced();
    }

    public static void resetLastMoveUsed() {
        ((FightState)FIGHT.visualStateHandler).resetLastMoveUsed();
    }
}
