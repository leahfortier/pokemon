package gui.view.map;

import draw.Alignment;
import draw.DrawUtils;
import draw.layout.DrawLayout;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.PanelList;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

class MessageState extends VisualStateHandler {
    private final int circleRadius = 10;

    // Message used to create the spacing for the choices panel and labels
    // If the current choice message is not the same, then the panels need to be reset
    private MessageUpdate spacingChoiceMessage;
    private PanelList choicePanels;
    private DrawPanel[] choiceLabels;
    private int circleX;

    private int choiceIndex;

    // Creates choices panel and choice label panels for a new choice message
    private void setChoicePanels(MessageUpdate currentMessage) {
        // Panels are already using the spacing for this message
        if (currentMessage == spacingChoiceMessage) {
            return;
        }

        // Panel width is chosen based on the longest choice
        ChoiceMatcher[] choices = currentMessage.getChoices();
        int longestLength = Arrays.stream(choices)
                                  .sorted(Comparator.comparingInt(choice -> -choice.getText().length()))
                                  .collect(Collectors.toList())
                                  .get(0).getText().length();

        int fontSize = 30;
        int spacing = 15;
        int borderSize = 15;

        int fullTextWidth = FontMetrics.getTextWidth(fontSize, longestLength);
        int textHeight = FontMetrics.getTextHeight(fontSize);

        int width = fullTextWidth + 2*spacing + spacing/2 + 2*circleRadius + 2*borderSize;
        int height = (textHeight + spacing)*choices.length + spacing + 2*borderSize;

        DrawPanel choicesPanel = new DrawPanel(
                Global.GAME_SIZE.width - width,
                BasicPanels.getMessagePanelY() - height + DrawUtils.OUTLINE_SIZE,
                width,
                height
        ).withBlackOutline()
         .withBorderSize(borderSize);

        // Where the selected circle should begin
        circleX = choicesPanel.x + borderSize + spacing;

        // Fake panels are mostly correct but need to change their start x to account for the choice circle
        DrawPanel[] fakePanels = new DrawLayout(choicesPanel, choices.length, 1, spacing).getPanels();
        int newX = circleX + 2*circleRadius + spacing/2;

        // Spacing factor is 0 so label will start exactly where this panel begins
        choiceLabels = new DrawPanel[fakePanels.length];
        for (int i = 0; i < fakePanels.length; i++) {
            DrawPanel panel = fakePanels[i];
            choiceLabels[i] = new DrawPanel(newX, panel.y, panel.width - (newX - panel.x), panel.height)
                    .withLabel(choices[i].getText(), 30, Alignment.LEFT)
                    .withLabelSpacingFactor(0)
                    .withNoBackground();
        }

        choicePanels = new PanelList(choicesPanel).add(choiceLabels);
        spacingChoiceMessage = currentMessage;
    }

    @Override
    public void draw(Graphics g) {
        MessageUpdate currentMessage = view.getCurrentMessage();

        BasicPanels.drawFullMessagePanel(g, currentMessage.getMessage());
        if (currentMessage.isChoice()) {
            this.setChoicePanels(currentMessage);
            choicePanels.drawAll(g);

            // Draw selected circle
            g.setColor(Color.BLACK);
            DrawUtils.drawCenteredHeightCircle(g, circleX, choiceLabels[choiceIndex].centerY(), circleRadius);
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
