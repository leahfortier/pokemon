package draw.panel;

import battle.ActivePokemon;
import battle.attack.Attack;
import battle.attack.Move;
import draw.button.Button;
import draw.button.ButtonList;
import input.ControlKey;
import input.InputControl;
import message.MessageUpdate;
import message.MessageUpdateType;
import util.Point;
import util.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayDeque;
import java.util.List;

public class LearnMovePanel {
    private static final int NUM_COLS = 4;

    private final DrawPanel moveDetailsPanel;
    private final ButtonList buttons;
    private final Button yesButton;
    private final Button noButton;
    private final Button newMoveButton;

    private final ArrayDeque<MessageUpdate> messages;

    private final ActivePokemon learning;
    private final Move toLearn;

    private boolean learnedMove;

    private State state;
    private int selectedButton;

    public LearnMovePanel(ActivePokemon learning, Move toLearn) {
        this.learning = learning;
        this.toLearn = toLearn;

        moveDetailsPanel = new DrawPanel(0, 440 - 161, 385, 161).withBorderPercentage(8).withBlackOutline().withTransparentCount(2);

        // Create a button for each known move and then one for the new move and one for not learning
        buttons = new ButtonList(BasicPanels.getFullMessagePanelButtons(183, 55, 2, NUM_COLS));

        // Bottom center left
        yesButton = buttons.get(NUM_COLS + 1);

        // Bottom center right
        noButton = buttons.get(NUM_COLS + 2);

        newMoveButton = this.buttons.get(buttons.size() - 2);

        learnedMove = false;
        state = State.MESSAGE;

        messages = new ArrayDeque<>();
        messages.add(new MessageUpdate(learning.getName() + " is trying to learn " + toLearn.getAttack().getName() + "...").withUpdate(MessageUpdateType.LEARN_MOVE));
        messages.add(new MessageUpdate("Delete a move in order to learn " + toLearn.getAttack().getName() + "?"));

        updateActiveButtons();
    }

    public void update() {
        if (BasicPanels.isAnimatingMessage()) {
            return;
        }

        InputControl input = InputControl.instance();
        selectedButton = buttons.update(selectedButton);

        if (state == State.QUESTION) {
            if (noButton.checkConsumePress()) {
                messages.pop();
                messages.add(new MessageUpdate(learning.getActualName() + " did not learn " + toLearn.getAttack().getName() + "."));
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
                for (int x = 0; x < Move.MAX_MOVES/2; x++, moveIndex++) {
                    int index = Point.getIndex(x, y, NUM_COLS);
                    if (buttons.get(index).checkConsumePress()) {
                        state = State.END;

                        String learnerName = learning.getActualName();
                        String learnMoveName = toLearn.getAttack().getName();
                        String deleteMoveName = learning.getActualMoves().get(moveIndex).getAttack().getName();

                        learnedMove = true;
                        learning.addMove(toLearn, moveIndex, true);

                        messages.addFirst(new MessageUpdate("...and " + learnerName + " learned " + learnMoveName + "!"));
                        messages.addFirst(new MessageUpdate(learnerName + " forgot how to use " + deleteMoveName + "..."));

                        updateActiveButtons();
                    }
                }
            }

            if (newMoveButton.checkConsumePress()) {
                state = State.END;

                messages.addFirst(new MessageUpdate(learning.getActualName() + " did not learn " + toLearn.getAttack().getName() + "."));
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
            for (int i = 0; i < buttons.size(); i++) {
                Button button = buttons.get(i);
                button.setActive(button == yesButton || button == noButton);
                if (button == yesButton) {
                    selectedButton = i;
                }
            }
        } else if (state == State.DELETE) {
            for (int y = 0; y < 2; y++) {
                for (int x = 0; x < NUM_COLS; x++) {
                    int index = Point.getIndex(x, y, NUM_COLS);
                    Button button = buttons.get(index);
                    button.setActive(x < Move.MAX_MOVES/2 || button == newMoveButton);
                    if (button == newMoveButton) {
                        selectedButton = index;
                    }
                }
            }
        }
    }

    public void draw(Graphics g) {
        BasicPanels.drawFullMessagePanel(
                g,
                messages.isEmpty() || state == State.DELETE
                        ? StringUtils.empty()
                        : messages.peek().getMessage()
        );

        if (state == State.QUESTION) {
            drawButton(g, yesButton, new Color(120, 200, 80), "Yes");
            drawButton(g, noButton, new Color(220, 20, 20), "No");
        } else if (state == State.DELETE) {
            List<Move> moves = learning.getActualMoves();
            Attack selected = null;
            for (int y = 0, moveIndex = 0; y < 2; y++) {
                for (int x = 0; x < Move.MAX_MOVES/2; x++, moveIndex++) {
                    int index = Point.getIndex(x, y, NUM_COLS);
                    Move move = moves.get(moveIndex);

                    buttons.get(index).drawMoveButton(g, move);
                    if (index == selectedButton) {
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
