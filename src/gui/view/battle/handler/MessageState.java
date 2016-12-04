package gui.view.battle.handler;

import gui.TileSet;
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

    // Stat gains and corresponding new stat upgrades for leveling up/evolving
    private int[] statGains;
    private int[] newStats;

    @Override
    public void set(BattleView view) {

    }

    @Override
    public void draw(BattleView view, Graphics g, TileSet tiles) {
        g.drawImage(tiles.getTile(0x3), 0, 439, null);

        g.setColor(Color.BLACK);
        FontMetrics.setFont(g, 30);

        DrawUtils.drawWrappedText(g, view.getMessage(), 30, 490, 720);

        if (view.isState(VisualState.STAT_GAIN)) {
            g.drawImage(tiles.getTile(0x5), 0, 280, null);
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
