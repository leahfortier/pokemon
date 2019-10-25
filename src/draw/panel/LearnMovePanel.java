package draw.panel;

import battle.ActivePokemon;
import battle.attack.Attack;
import battle.attack.Move;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonList;
import draw.button.ButtonPanel;
import draw.panel.WrapPanel.WrapMetrics;
import input.ControlKey;
import input.InputControl;
import map.Direction;
import message.MessageQueue;
import message.MessageUpdate;
import message.MessageUpdateType;
import pokemon.active.MoveList;
import util.FontMetrics;
import util.Point;

import java.awt.Color;
import java.awt.Graphics;

public class LearnMovePanel {
    private static final int NUM_COLS = 4;

    private static final int YES_BUTTON = NUM_COLS + 1; // Bottom center left
    private static final int NO_BUTTON = NUM_COLS + 2; // Bottom center right

    private final MovePanel moveDetailsPanel;
    private final ButtonList buttons;
    private final Button yesButton;
    private final Button noButton;
    private final Button newMoveButton;

    private final MessageQueue messages;

    private final ActivePokemon learning;
    private final Move toLearn;

    private boolean learnedMove;

    private State state;

    public LearnMovePanel(ActivePokemon learning, Move toLearn) {
        this.learning = learning;
        this.toLearn = toLearn;

        int height = 161;
        int y = BasicPanels.getMessagePanelY() - height;
        this.moveDetailsPanel = new MovePanel(0, y, 385, height, 22, 18, 16)
                .withMissingBlackOutline(Direction.DOWN)
                .withBorderPercentage(8)
                .withTransparentCount(2)
                .withMinDescFontSize(13);

        // Create a button for each known move and then one for the new move and one for not learning
        buttons = new ButtonList(BasicPanels.getFullMessagePanelButtons(8, 2, NUM_COLS));
        buttons.forEach(button -> button.panel()
                                        .withTransparentCount(2)
                                        .withBorderPercentage(15)
                                        .withBlackOutline()
                                        .withLabelSize(30));
        
        yesButton = buttons.get(YES_BUTTON);
        noButton = buttons.get(NO_BUTTON);
        newMoveButton = buttons.get(buttons.size() - 2);

        learnedMove = false;
        state = State.MESSAGE;

        messages = new MessageQueue();
        messages.add(new MessageUpdate(learning.getName() + " is trying to learn " + toLearn.getAttack().getName() + "...").withUpdate(MessageUpdateType.LEARN_MOVE));
        messages.add("Delete a move in order to learn " + toLearn.getAttack().getName() + "?");

        updateActiveButtons();
    }

    public void update() {
        if (BasicPanels.isAnimatingMessage()) {
            return;
        }

        InputControl input = InputControl.instance();
        buttons.update();

        if (state == State.QUESTION) {
            if (noButton.checkConsumePress()) {
                messages.pop();
                messages.add(learning.getActualName() + " did not learn " + toLearn.getAttack().getName() + ".");
                state = State.END;
                updateActiveButtons();
            }

            if (yesButton.checkConsumePress()) {
                messages.pop();
                state = State.DELETE;
                updateActiveButtons();
            }
        } else if (state == State.DELETE) {
            for (int y = 0, moveIndex = 0; y < 2; y++) {
                for (int x = 0; x < MoveList.MAX_MOVES/2; x++, moveIndex++) {
                    int index = Point.getIndex(x, y, NUM_COLS);
                    if (buttons.get(index).checkConsumePress()) {
                        state = State.END;

                        String learnerName = learning.getActualName();
                        String learnMoveName = toLearn.getAttack().getName();
                        String deleteMoveName = learning.getActualMoves().get(moveIndex).getAttack().getName();

                        learnedMove = true;
                        learning.addMove(toLearn, moveIndex, true);

                        messages.addFirst("...and " + learnerName + " learned " + learnMoveName + "!");
                        messages.addFirst(learnerName + " forgot how to use " + deleteMoveName + "...");

                        updateActiveButtons();
                    }
                }
            }

            if (newMoveButton.checkConsumePress()) {
                state = State.END;

                messages.addFirst(learning.getActualName() + " did not learn " + toLearn.getAttack().getName() + ".");
                updateActiveButtons();
            }
        } else {
            if (!messages.isEmpty() && input.consumeIfMouseDown(ControlKey.SPACE)) {
                MessageUpdate message = messages.pop();
                if (message.learnMove()) {
                    state = State.QUESTION;
                    updateActiveButtons();
                }
            }
        }
    }

    private void updateActiveButtons() {
        buttons.setInactive();

        if (state == State.QUESTION) {
            yesButton.setActive(true);
            noButton.setActive(true);
            buttons.setSelected(YES_BUTTON);
        } else if (state == State.DELETE) {
            for (int y = 0; y < 2; y++) {
                for (int x = 0; x < NUM_COLS; x++) {
                    int index = Point.getIndex(x, y, NUM_COLS);
                    Button button = buttons.get(index);
                    button.setActive(x < MoveList.MAX_MOVES/2 || button == newMoveButton);
                    if (button == newMoveButton) {
                        buttons.setSelected(index);
                    }
                }
            }
        }
    }

    public void draw(Graphics g) {
        BasicPanels.drawFullMessagePanel(
                g,
                messages.isEmptyMessage() || state == State.DELETE ? "" : messages.peek().getMessage()
        );

        if (state == State.QUESTION) {
            drawLabelButton(g, yesButton, new Color(120, 200, 80), "Yes");
            drawLabelButton(g, noButton, new Color(220, 20, 20), "No");
        } else if (state == State.DELETE) {
            MoveList moves = learning.getActualMoves();
            Attack selected = null;
            for (int col = 0, moveIndex = 0; col < NUM_COLS/2; col++) {
                for (int row = 0; row < MoveList.MAX_MOVES/2; row++, moveIndex++) {
                    int buttonIndex = Point.getIndex(row, col, NUM_COLS);
                    Move move = moves.get(moveIndex);
                    drawMoveButton(g, buttons.get(buttonIndex), move);
                    if (buttonIndex == buttons.getSelected()) {
                        selected = move.getAttack();
                    }
                }
            }

            drawMoveDetails(g, selected == null ? toLearn.getAttack() : selected);
            drawMoveButton(g, newMoveButton, toLearn);
        }

        buttons.drawHover(g);
    }

    public WrapMetrics drawMoveDetails(Graphics g, Attack attack) {
        return moveDetailsPanel.draw(g, attack);
    }

    private void drawMoveButton(Graphics g, Button button, Move move) {
        ButtonPanel panel = button.panel();

        // Attack type color background
        panel.withBackgroundColor(move.getAttack().getActualType().getColor())
             .drawBackground(g);

        FontMetrics.setBlackFont(g, 19);
        int spacing = FontMetrics.getTextWidth(g)/2;
        int borderSize = panel.getBorderSize();
        int fullSpacing = spacing + borderSize;

        // Attack name as left label on the top
        g.drawString(move.getAttack().getName(), panel.x + fullSpacing, panel.y + fullSpacing + FontMetrics.getTextHeight(g));

        // PP amount as right label on the bottom
        FontMetrics.setBlackFont(g, 16);
        String ppString = "PP: " + move.getPP() + "/" + move.getMaxPP();
        TextUtils.drawRightAlignedString(g, ppString, panel.rightX() - fullSpacing, panel.bottomY() - fullSpacing);
    }

    private void drawLabelButton(Graphics g, Button button, Color color, String label) {
        button.panel()
              .withLabel(label)
              .withBackgroundColor(color)
              .draw(g);
    }

    public boolean learnedMove() {
        return this.learnedMove;
    }

    public boolean isFinished() {
        return state == State.END && messages.isEmpty();
    }

    private enum State {
        MESSAGE,
        QUESTION,
        DELETE,
        END
    }
}
