package gui.view.battle.handler;

import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonList;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import input.ControlKey;
import input.InputControl;
import map.Direction;
import message.MessageUpdate;
import pokemon.stat.Stat;
import util.FontMetrics;

import java.awt.Graphics;

public class MessageState implements VisualStateHandler {
    private final DrawPanel statsPanel;

    // Stat gains and corresponding new stat upgrades for leveling up/evolving
    private int[] statGains;
    private int[] newStats;

    public MessageState() {
        int height = 161;
        int y = BasicPanels.getMessagePanelY() - height;
        this.statsPanel = new DrawPanel(0, y, 273, height).withMissingBlackOutline(Direction.DOWN);
    }

    @Override
    public void set(BattleView view) {}

    @Override
    public void draw(BattleView view, Graphics g) {
        view.drawFullMessagePanel(g);

        if (view.isState(VisualState.STAT_GAIN)) {
            statsPanel.drawBackground(g);
            FontMetrics.setBlackFont(g, 16);
            for (int i = 0; i < Stat.NUM_STATS; i++) {
                g.drawString(Stat.getStat(i, false).getName(), 25, 314 + i*21);

                TextUtils.drawRightAlignedString(g, (statGains[i] < 0 ? "" : " + ") + statGains[i], 206, 314 + i*21);
                TextUtils.drawRightAlignedString(g, newStats[i] + "", 247, 314 + i*21);
            }
        }
    }

    @Override
    public void update(BattleView view) {
        boolean pressed = false;
        InputControl input = InputControl.instance();

        // Consume input for mouse clicks and spacebars
        if (input.consumeIfMouseDown(ControlKey.SPACE)) {
            pressed = true;
        }

        // Don't go to the next message if an animation is playing
        if (pressed && view.hasMessage() && !view.isPlayingAnimation()) {
            if (view.isState(VisualState.STAT_GAIN)) {
                view.setVisualState(VisualState.MESSAGE);
            }

            view.cycleMessage();
        }
    }

    @Override
    public void checkMessage(MessageUpdate newMessage) {
        if (newMessage.gainUpdate()) {
            newStats = newMessage.getNewStats();
            statGains = newMessage.getGain();
        }
    }

    @Override
    public ButtonList getButtons() {
        return new ButtonList(new Button[0]);
    }
}
