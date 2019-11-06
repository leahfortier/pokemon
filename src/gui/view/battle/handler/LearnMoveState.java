package gui.view.battle.handler;

import draw.button.ButtonList;
import draw.handler.LearnMoveHandler;
import gui.view.battle.BattleView;

import java.awt.Graphics;

public class LearnMoveState implements VisualStateHandler {
    private LearnMoveHandler learnMoveHandler;

    @Override
    public void set(BattleView view) {
        this.learnMoveHandler = new LearnMoveHandler(view.getLearnedPokemon(), view.getLearnedMove());
    }

    @Override
    public void draw(BattleView view, Graphics g) {
        this.learnMoveHandler.draw(g);
    }

    @Override
    public void update(BattleView view) {
        this.learnMoveHandler.update();
        if (this.learnMoveHandler.isFinished()) {
            view.cycleMessage();
        }
    }

    @Override
    public ButtonList getButtons() {
        return new ButtonList(0);
    }
}
