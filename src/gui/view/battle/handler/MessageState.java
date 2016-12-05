package gui.view.battle.handler;

import gui.panel.DrawPanel;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import input.ControlKey;
import input.InputControl;
import message.MessageUpdate;
import pokemon.Stat;
import util.DrawUtils;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;

public class MessageState implements VisualStateHandler {

    private final DrawPanel statsPanel;

    public MessageState() {
        this.statsPanel = new DrawPanel(0, 280, 273, 161).withBlackOutline();
    }

    // Stat gains and corresponding new stat upgrades for leveling up/evolving
    private int[] statGains;
    private int[] newStats;

    @Override
    public void set(BattleView view) {}

    @Override
    public void draw(BattleView view, Graphics g) {
        view.drawFullMessagePanel(g);

        if (view.isState(VisualState.STAT_GAIN)) {
            statsPanel.drawBackground(g);
            g.setColor(Color.BLACK);
            for (int i = 0; i < Stat.NUM_STATS; i++) {
                FontMetrics.setFont(g, 16);
                g.drawString(Stat.getStat(i, false).getName(), 25, 314 + i*21);

                DrawUtils.drawRightAlignedString(g, (statGains[i] < 0 ? "" : " + ") + statGains[i], 206, 314 + i*21);
                DrawUtils.drawRightAlignedString(g, newStats[i] + "", 247, 314 + i*21);
            }
        }
    }

    @Override
    public void update(BattleView view) {
        boolean pressed = false;
        InputControl input = InputControl.instance();

        // Consume input for mouse clicks and spacebars
        if (input.consumeIfMouseDown()) {
            pressed = true;
        }

        if (input.consumeIfDown(ControlKey.SPACE)) {
            pressed = true;
        }

        // Don't go to the next message if an animation is playing
        if (pressed && view.hasMessage() && !view.isPlayingAnimation()) {
            if (view.isState(VisualState.STAT_GAIN)) view.setVisualState(VisualState.MESSAGE);
            view.cycleMessage(false);
        }
    }

    @Override
    public void checkMessage(MessageUpdate newMessage) {
        if (newMessage.gainUpdate()) {
            newStats = newMessage.getNewStats();
            statGains = newMessage.getGain();
        }
    }
}
