package gui.view.map;

import gui.GameData;
import gui.view.map.VisualState.VisualStateHandler;
import input.ControlKey;
import input.InputControl;
import main.Game;
import map.entity.EntityAction;
import map.entity.movable.PlayerEntity;
import map.triggers.Trigger;
import message.MessageUpdate;
import message.MessageUpdate.Update;
import message.Messages;
import pattern.action.ChoiceActionMatcher.ChoiceMatcher;
import sound.SoundPlayer;
import util.DrawUtils;
import util.FontMetrics;
import util.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

class MessageState implements VisualStateHandler {
    private int dialogueSelection;

    @Override
    public void draw(Graphics g, MapView mapView) {
        GameData data = Game.getData();
        MessageUpdate currentMessage = mapView.getCurrentMessage();

        BufferedImage bg = data.getBattleTiles().getTile(3);
        g.drawImage(bg, 0, 439, null);

        FontMetrics.setFont(g, 30);
        g.setColor(Color.BLACK);

        int height = DrawUtils.drawWrappedText(g, currentMessage.getMessage(), 30, 490, 720);
        if (currentMessage.isChoice()) {
            ChoiceMatcher[] choices = currentMessage.getChoices();
            for (int i = 0; i < choices.length; i++) {
                int y = height + i* FontMetrics.getDistanceBetweenRows(g);
                if (i == dialogueSelection) {
                    g.fillOval(50, y - FontMetrics.getTextHeight(g)/2 - 5, 10, 10);
                }

                g.drawString(choices[i].text, 80, y);
            }
        }
    }

    @Override
    public void update(int dt, MapView mapView) {
        InputControl input = InputControl.instance();
        MessageUpdate currentMessage = mapView.getCurrentMessage();
        PlayerEntity playerEntity = Game.getPlayer().getEntity();

        if (currentMessage.isChoice()) {
            if (input.consumeIfDown(ControlKey.DOWN)) {
                dialogueSelection++;
            } else if (input.consumeIfDown(ControlKey.UP)) {
                dialogueSelection--;
            }

            dialogueSelection += currentMessage.getChoices().length;
            dialogueSelection %= currentMessage.getChoices().length;
        }

        if (!SoundPlayer.soundPlayer.soundEffectIsPlaying() && input.consumeIfDown(ControlKey.SPACE)) {
            if (currentMessage.isChoice()) {
                ChoiceMatcher choice = currentMessage.getChoices()[dialogueSelection];
                Trigger trigger = EntityAction.addActionGroupTrigger(null, null, null, choice.getActions());
                Messages.addMessageToFront(new MessageUpdate("", trigger.getName(), Update.TRIGGER));
            }

            boolean newMessage = false;
            while (Messages.hasMessages()) {
                mapView.cycleMessage();

                if (!mapView.isState(VisualState.MESSAGE) || !StringUtils.isNullOrEmpty(mapView.getCurrentMessage().getMessage())) {
                    newMessage = true;
                    break;
                }
            }

            if (!newMessage && !Messages.hasMessages()) {
                playerEntity.resetCurrentInteractionEntity();
                mapView.resetCurrentMessage();
                if (!VisualState.hasBattle()) {
                    mapView.setState(VisualState.MAP);
                }
            }
        }
    }
}
