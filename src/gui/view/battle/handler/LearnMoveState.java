package gui.view.battle.handler;

import draw.button.ButtonList;
import draw.handler.LearnMoveHandler;
import message.MessageUpdate;

import java.awt.Graphics;

public class LearnMoveState extends VisualStateHandler {
    private LearnMoveHandler learnMoveHandler;

    @Override
    public void draw(Graphics g) {
        this.learnMoveHandler.draw(g);
    }

    @Override
    public void update() {
        this.learnMoveHandler.update();
        if (this.learnMoveHandler.isFinished()) {
            view.cycleMessage();
        }
    }

    @Override
    public ButtonList getButtons() {
        return new ButtonList(0);
    }

    @Override
    public boolean updateBackButton() {
        return false;
    }

    @Override
    public void checkMessage(MessageUpdate newMessage) {
        if (newMessage.learnMove()) {
            this.learnMoveHandler = new LearnMoveHandler(newMessage.getMoveLearner(), newMessage.getMove());
        }
    }
}
