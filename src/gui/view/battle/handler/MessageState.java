package gui.view.battle.handler;

import draw.button.ButtonList;
import draw.panel.StatGainPanel;
import gui.view.battle.VisualState;
import input.ControlKey;
import input.InputControl;
import message.MessageUpdate;

import java.awt.Graphics;

public class MessageState extends VisualStateHandler {
    private final StatGainPanel statsPanel;

    // Stat gains and corresponding new stat upgrades for leveling up/evolving
    private MessageUpdate statGainMessage;

    public MessageState() {
        this.statsPanel = new StatGainPanel();
    }

    @Override
    public void draw(Graphics g) {
        view.drawFullMessagePanel(g);
        if (view.isState(VisualState.STAT_GAIN)) {
            statsPanel.drawBackground(g);
            statsPanel.drawStatGain(g, statGainMessage);
        }
    }

    @Override
    public void update() {
        InputControl input = InputControl.instance();

        // Consume input for mouse clicks and spacebars
        // Don't go to the next message if an animation is playing
        if (input.consumeIfMouseDown(ControlKey.SPACE) && view.hasMessage() && !view.isPlayingAnimation()) {
            if (view.isState(VisualState.STAT_GAIN)) {
                view.setVisualState(VisualState.MESSAGE);
            }

            view.cycleMessage();
        }
    }

    @Override
    public void checkMessage(MessageUpdate newMessage) {
        if (newMessage.gainUpdate()) {
            statGainMessage = newMessage;
        }
    }

    @Override
    public ButtonList getButtons() {
        return new ButtonList(0);
    }
}
