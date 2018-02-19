package gui.view.map;

import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import gui.view.map.VisualState.VisualStateHandler;
import input.ControlKey;
import input.InputControl;
import main.Global;
import map.triggers.HaltTrigger;
import map.triggers.Trigger;
import message.MessageUpdate;
import message.Messages;
import pattern.action.ActionMatcher;
import pattern.action.ChoiceMatcher;
import sound.SoundPlayer;
import util.FontMetrics;
import util.StringUtils;

import java.awt.Color;
import java.awt.Graphics;

class MessageState implements VisualStateHandler {
    private int dialogueSelection;

    @Override
    public void draw(Graphics g, MapView mapView) {
        MessageUpdate currentMessage = mapView.getCurrentMessage();

        BasicPanels.drawFullMessagePanel(g, currentMessage.getMessage());
        if (currentMessage.isChoice()) {
            ChoiceMatcher[] choices = currentMessage.getChoices();
            String longestChoice = choices[0].getText();
            for (int i = 1; i < choices.length; i++) {
                if (choices[i].getText().length() > longestChoice.length()) {
                    longestChoice = choices[i].getText();
                }
            }

            int spacing = 20;
            int circleRadius = 10;

            int distanceBetweenRows = FontMetrics.getDistanceBetweenRows(g);
            int textHeight = FontMetrics.getTextHeight(g);

            int width = FontMetrics.getTextWidth(g, longestChoice) + spacing*3 + 2*circleRadius;
            int height = (distanceBetweenRows + 1)*choices.length - textHeight + 2*spacing;
            DrawPanel choicesPanel = new DrawPanel(
                    Global.GAME_SIZE.width - width,
                    BasicPanels.getMessagePanelY() - height,
                    width,
                    height
            ).withBlackOutline();
            choicesPanel.drawBackground(g);

            g.setColor(Color.BLACK);
            for (int i = 0; i < choices.length; i++) {
                int y = choicesPanel.y + spacing + i*distanceBetweenRows + textHeight;
                if (i == dialogueSelection) {
                    g.fillOval(choicesPanel.x + spacing, y - textHeight/2 - circleRadius/2, circleRadius, circleRadius);
                }

                g.drawString(choices[i].getText(), choicesPanel.x + 2*spacing + 2*circleRadius, y);
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

        if (!SoundPlayer.instance().soundEffectIsPlaying() && input.consumeIfMouseDown(ControlKey.SPACE)) {
            if (currentMessage.isChoice()) {
                ChoiceMatcher choice = currentMessage.getChoices()[dialogueSelection];
                Trigger trigger = ActionMatcher.addActionGroupTrigger(null, null, null, choice.getActions());
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
