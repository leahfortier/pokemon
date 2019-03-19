package draw.panel;

import battle.ActivePokemon;
import battle.attack.Attack;
import battle.attack.Move;
import draw.button.Button;
import draw.button.ButtonList;
import input.ControlKey;
import input.InputControl;
import message.MessageQueue;
import message.MessageUpdate;
import message.MessageUpdateType;
import pokemon.active.MoveList;
import util.Point;

import java.awt.Color;
import java.awt.Graphics;

public class LearnMovePanel {
    private static final int NUM_COLS = 4;

    private static final int YES_BUTTON = NUM_COLS + 1; // Bottom center left
    private static final int NO_BUTTON = NUM_COLS + 2; // Bottom center right

    private final DrawPanel moveDetailsPanel;
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

        moveDetailsPanel = new DrawPanel(0, 440 - 161, 385, 161).withBorderPercentage(8).withBlackOutline().withTransparentCount(2);

        // Create a button for each known move and then one for the new move and one for not learning
        buttons = new ButtonList(BasicPanels.getFullMessagePanelButtons(183, 55, 2, NUM_COLS));

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
            drawButton(g, yesButton, new Color(120, 200, 80), "Yes");
            drawButton(g, noButton, new Color(220, 20, 20), "No");
        } else if (state == State.DELETE) {
            MoveList moves = learning.getActualMoves();
            Attack selected = null;
            for (int y = 0, moveIndex = 0; y < 2; y++) {
                for (int x = 0; x < MoveList.MAX_MOVES/2; x++, moveIndex++) {
                    int index = Point.getIndex(x, y, NUM_COLS);
                    Move move = moves.get(moveIndex);

                    buttons.get(index).drawMoveButton(g, move);
                    if (index == buttons.getSelected()) {
                        selected = move.getAttack();
                    }
                }
            }

            moveDetailsPanel.drawMovePanel(g, selected == null ? toLearn.getAttack() : selected);
            newMoveButton.drawMoveButton(g, toLearn);
        }

        buttons.draw(g);
    }

    private void drawButton(Graphics g, Button button, Color color, String label) {
        button.fillBordered(g, color);
        button.blackOutline(g);
        button.label(g, 30, label);
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
