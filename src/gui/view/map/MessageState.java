package gui.view.map;

import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import input.ControlKey;
import input.InputControl;
import main.Global;
import map.triggers.HaltTrigger;
import map.triggers.Trigger;
import message.MessageUpdate;
import message.Messages;
import pattern.action.ChoiceMatcher;
import sound.SoundPlayer;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;

class MessageState extends VisualStateHandler {
    private int choiceIndex;

    @Override
    public void draw(Graphics g) {
        MessageUpdate currentMessage = view.getCurrentMessage();

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
                if (i == choiceIndex) {
                    g.fillOval(choicesPanel.x + spacing, y - textHeight/2 - circleRadius/2, circleRadius, circleRadius);
                }

                g.drawString(choices[i].getText(), choicesPanel.x + 2*spacing + 2*circleRadius, y);
            }
        }
    }

    @Override
    public void update(int dt) {
        if (BasicPanels.isAnimatingMessage()) {
            return;
        }

        InputControl input = InputControl.instance();
        MessageUpdate currentMessage = view.getCurrentMessage();

        if (currentMessage.isChoice()) {
            if (input.consumeIfDown(ControlKey.DOWN)) {
                choiceIndex++;
            } else if (input.consumeIfDown(ControlKey.UP)) {
                choiceIndex--;
            }

            choiceIndex += currentMessage.getChoices().length;
            choiceIndex %= currentMessage.getChoices().length;
        }

        if (!SoundPlayer.instance().soundEffectIsPlaying() && input.consumeIfMouseDown(ControlKey.SPACE)) {
            if (currentMessage.isChoice()) {
                ChoiceMatcher choice = currentMessage.getChoices()[choiceIndex];
                Trigger trigger = choice.getActions().getGroupTrigger(null, null);
                Messages.addToFront(new MessageUpdate().withTrigger(trigger));
                choiceIndex = 0;
            }

            boolean newMessage = false;
            while (!HaltTrigger.isHalted() && Messages.hasMessages()) {
                view.cycleMessage();
                if (!view.isState(VisualState.MESSAGE) || !view.emptyMessage()) {
                    newMessage = true;
                    break;
                }
            }

            if (!newMessage && !Messages.hasMessages()) {
                view.resetMessageState();
            }
        }
    }
}
