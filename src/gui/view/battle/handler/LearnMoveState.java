package gui.view.battle.handler;

import draw.panel.LearnMovePanel;
import gui.view.battle.BattleView;

import java.awt.Graphics;

public class LearnMoveState implements VisualStateHandler {
    private LearnMovePanel learnMovePanel;

    @Override
    public void set(BattleView view) {
        this.learnMovePanel = new LearnMovePanel(view.getLearnedPokemon(), view.getLearnedMove());
    }

    @Override
    public void draw(BattleView view, Graphics g) {
        this.learnMovePanel.draw(g);
    }

    @Override
    public void update(BattleView view) {
        this.learnMovePanel.update();
        if (this.learnMovePanel.isFinished()) {
            view.cycleMessage();
        }
    }
}
