package gui.view.map;

import draw.button.panel.BasicPanels;
import gui.view.map.VisualState.VisualStateHandler;
import input.ControlKey;
import input.InputControl;
import main.Game;
import map.entity.EntityAction;
import map.entity.movable.PlayerEntity;
import map.triggers.HaltTrigger;
import map.triggers.Trigger;
import message.MessageUpdate;
import message.Messages;
import pattern.action.ChoiceActionMatcher.ChoiceMatcher;
import sound.SoundPlayer;
import util.FontMetrics;
import util.StringUtils;

import java.awt.Graphics;

class MessageState implements VisualStateHandler {
    private int dialogueSelection;

    @Override
    public void draw(Graphics g, MapView mapView) {
        MessageUpdate currentMessage = mapView.getCurrentMessage();

        int height = BasicPanels.drawFullMessagePanel(g, currentMessage.getMessage());
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
        if (BasicPanels.isAnimatingMessage()) {
            return;
        }

        InputControl input = InputControl.instance();
        MessageUpdate currentMessage = mapView.getCurrentMessage();

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
                Messages.addToFront(new MessageUpdate().withTrigger(trigger.getName()));
            }

            boolean newMessage = false;
            while (!HaltTrigger.isHalted() && Messages.hasMessages()) {
                mapView.cycleMessage();
                if (!mapView.isState(VisualState.MESSAGE) || !StringUtils.isNullOrEmpty(mapView.getCurrentMessage().getMessage())) {
                    newMessage = true;
                    break;
                }
            }

            if (!newMessage && !Messages.hasMessages()) {
                mapView.resetMessageState();
            }
        }
    }
}
