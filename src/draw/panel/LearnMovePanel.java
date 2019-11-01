package draw.panel;

import battle.ActivePokemon;
import battle.attack.Attack;
import battle.attack.Move;
import draw.button.Button;
import draw.button.ButtonList;
import draw.button.ButtonTransitions;
import draw.layout.ButtonLayout;
import draw.panel.WrapPanel.WrapMetrics;
import input.ControlKey;
import input.InputControl;
import map.Direction;
import message.MessageQueue;
import message.MessageUpdate;
import message.MessageUpdateType;
import pokemon.active.MoveList;

import java.awt.Color;
import java.awt.Graphics;

public class LearnMovePanel {
    private static final int NUM_BUTTONS = MoveList.MAX_MOVES + 3;
    private static final int CURRENT_MOVES = 0;
    private static final int LAST_CURRENT_MOVE = CURRENT_MOVES + MoveList.MAX_MOVES - 1;
    private static final int YES_BUTTON = NUM_BUTTONS - 1;
    private static final int NO_BUTTON = NUM_BUTTONS - 2;
    private static final int NEW_MOVE_BUTTON = NUM_BUTTONS - 3;

    private final MovePanel moveDetailsPanel;
    private final ButtonList buttons;
    private final Button yesButton;
    private final Button noButton;
    private final Button[] currentMoveButtons;
    private final Button newMoveButton;

    private final MessageQueue messages;

    private final ActivePokemon learning;
    private final Move toLearn;

    private boolean learnedMove;

    private State state;

    public LearnMovePanel(ActivePokemon learning, Move toLearn) {
        this.learning = learning;
        this.toLearn = toLearn;

        messages = new MessageQueue();
        messages.add(new MessageUpdate(learning.getName() + " is trying to learn " + toLearn.getAttack().getName() + "...").withUpdate(MessageUpdateType.LEARN_MOVE));
        messages.add("Delete a move in order to learn " + toLearn.getAttack().getName() + "?");

        int height = 161;
        int y = BasicPanels.getMessagePanelY() - height;
        this.moveDetailsPanel = new MovePanel(0, y, 385, height, 22, 18, 16)
                .withMissingBlackOutline(Direction.DOWN)
                .withBorderPercentage(8)
                .withTransparentCount(2)
                .withMinDescFontSize(13);

        ButtonLayout moveLayout = BasicPanels.getFullMessagePanelLayout(2, MoveList.MAX_MOVES/2, 8)
                                             .withMissingRightCols(2)
                                             .withButtonSetup(panel -> panel.asMovePanel(19, 16)
                                                                            .skipInactive()
                                                                            .withTransparentCount(2)
                                                                            .withBorderPercentage(15)
                                                                            .withBlackOutline());

        // Bottom right of the move buttons
        newMoveButton = moveLayout.getButton(
                1, MoveList.MAX_MOVES/2,
                new ButtonTransitions().left(LAST_CURRENT_MOVE).right(LAST_CURRENT_MOVE - 1),
                () -> {
                    state = State.END;
                    messages.addFirst(learning.getActualName() + " did not learn " + toLearn.getAttack().getName() + ".");
                }
        ).setup(panel -> panel.withMove(toLearn));

        MoveList moves = learning.getActualMoves();
        currentMoveButtons = moveLayout.withStartIndex(CURRENT_MOVES)
                                       .withDefaultTransitions(new ButtonTransitions().right(NEW_MOVE_BUTTON)
                                                                                      .left(NEW_MOVE_BUTTON))
                                       .withPressIndex(this::pressLearnNewMove)
                                       .withButtonSetup((panel, index) -> panel.withMove(moves.get(index)))
                                       .getButtons();

        ButtonLayout questionLayout = BasicPanels.getFullMessagePanelLayout(2, 4, newMoveButton)
                                                 .withButtonSetup(panel -> panel.skipInactive()
                                                                                .withTransparentCount(2)
                                                                                .withBorderPercentage(15)
                                                                                .withLabelSize(30)
                                                                                .withBlackOutline());
        // Bottom middle left
        yesButton = questionLayout.getButton(
                1, 1,
                new ButtonTransitions().left(NO_BUTTON).right(NO_BUTTON),
                () -> {
                    messages.pop();
                    state = State.DELETE;
                }
        ).setup(panel -> panel.withBackgroundColor(new Color(120, 200, 80)).withLabel("Yes"));

        // Bottom middle right
        noButton = questionLayout.getButton(
                1, 2,
                new ButtonTransitions().left(YES_BUTTON).right(YES_BUTTON),
                () -> {
                    messages.pop();
                    messages.add(learning.getActualName() + " did not learn " + toLearn.getAttack().getName() + ".");
                    state = State.END;
                }
        ).setup(panel -> panel.withBackgroundColor(new Color(220, 20, 20)).withLabel("No"));

        // Create a button for each known move and then one for the new move and one for not learning
        buttons = new ButtonList(NUM_BUTTONS);
        buttons.set(CURRENT_MOVES, currentMoveButtons);
        buttons.set(NEW_MOVE_BUTTON, newMoveButton);
        buttons.set(YES_BUTTON, yesButton);
        buttons.set(NO_BUTTON, noButton);

        learnedMove = false;
        state = State.MESSAGE;

        updateActiveButtons();
    }

    private void pressLearnNewMove(int index) {
        state = State.END;

        String learnerName = learning.getActualName();
        String learnMoveName = toLearn.getAttack().getName();
        String deleteMoveName = learning.getActualMoves().get(index).getAttack().getName();

        learnedMove = true;
        learning.addMove(toLearn, index, true);

        messages.addFirst("...and " + learnerName + " learned " + learnMoveName + "!");
        messages.addFirst(learnerName + " forgot how to use " + deleteMoveName + "...");
    }

    public void update() {
        if (BasicPanels.isAnimatingMessage()) {
            return;
        }

        InputControl input = InputControl.instance();
        buttons.update();
        if (buttons.consumeSelectedPress()) {
            updateActiveButtons();
        }

        if ((state == State.MESSAGE || state == State.END)
                && !messages.isEmpty() && input.consumeIfMouseDown(ControlKey.SPACE)) {
            MessageUpdate message = messages.pop();
            if (message.learnMove()) {
                state = State.QUESTION;
                updateActiveButtons();
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
            for (Button currentMoveButton : currentMoveButtons) {
                currentMoveButton.setActive(true);
            }
            newMoveButton.setActive(true);
            buttons.setSelected(NEW_MOVE_BUTTON);
        }
    }

    public void draw(Graphics g) {
        BasicPanels.drawFullMessagePanel(
                g,
                messages.isEmptyMessage() || state == State.DELETE ? "" : messages.peek().getMessage()
        );

        buttons.drawPanels(g);

        if (state == State.DELETE) {
            // Draw details of highlighted move
            drawMoveDetails(g, getHighlightedMove().getAttack());
        }

        buttons.drawHover(g);
    }

    private Move getHighlightedMove() {
        int selectedButton = buttons.getSelected();

        // If the currently selected button is a current move, then highlight this move
        if (selectedButton >= CURRENT_MOVES && selectedButton < CURRENT_MOVES + MoveList.MAX_MOVES) {
            MoveList moves = learning.getActualMoves();
            return moves.get(selectedButton - CURRENT_MOVES);
        }

        // Highlight new move by default
        return toLearn;
    }

    public WrapMetrics drawMoveDetails(Graphics g, Attack attack) {
        return moveDetailsPanel.draw(g, attack);
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
