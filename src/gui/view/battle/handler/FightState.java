package gui.view.battle.handler;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Attack;
import battle.attack.Move;
import draw.button.Button;
import draw.button.ButtonList;
import draw.panel.MovePanel;
import draw.panel.WrapPanel.WrapMetrics;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import main.Game;
import pokemon.active.MoveList;
import trainer.TrainerAction;
import trainer.player.Player;
import util.string.StringUtils;

import java.awt.Graphics;

public class FightState implements VisualStateHandler {
    private final MovePanel moveDetailsPanel;
    private ButtonList moveButtons;

    private BattleView view;
    private MoveList selectedMoveList;

    // The last move that a Pokemon used
    private int lastMoveUsed;

    public FightState() {
        // TODO: 440 is message panel y, 161 is height, x + width = game size
        moveDetailsPanel = new MovePanel(
                415, 440, 385, 161,
                22, 18, 16
        )
                .withBorderPercentage(8)
                .withTransparentCount(2)
                .withMinDescFontSize(13);
    }

    @Override
    public void reset(BattleView view) {
        this.view = view;

        moveButtons = new ButtonList(
                view.createPanelLayout(MoveList.MAX_MOVES)
                    .withButtonSetup(panel -> panel.asMovePanel(19, 16)
                                                   .withTransparentCount(2)
                                                   .withBorderPercentage(15)
                                                   .withBlackOutline())
                    .getButtons()
        );

        this.resetLastMoveUsed();
    }

    @Override
    public void set() {
        selectedMoveList = Game.getPlayer().front().getMoves(view.getCurrentBattle());
        if (lastMoveUsed >= selectedMoveList.size()) {
            this.resetLastMoveUsed();
        }

        moveButtons.setSelected(lastMoveUsed);

        for (int i = 0; i < MoveList.MAX_MOVES; i++) {
            moveButtons.get(i).setActive(i < selectedMoveList.size());
        }

        moveButtons.setFalseHover();
    }

    @Override
    public void draw(Graphics g) {
        view.drawButtonsPanel(g);

        ActivePokemon playerPokemon = Game.getPlayer().front();
        MoveList moves = playerPokemon.getMoves(view.getCurrentBattle());
        for (int i = 0; i < moves.size(); i++) {
            drawMoveButton(g, moveButtons.get(i), moves.get(i));
        }

        String message = view.getMessage(VisualState.INVALID_FIGHT, null);
        if (StringUtils.isNullOrEmpty(message)) {
            // Draw move details
            drawMoveDetails(g, moves.get(moveButtons.getSelected()).getAttack());
        } else {
            // Show unusable move message
            view.drawMenuMessagePanel(g, message);
        }

        moveButtons.drawHover(g);
    }

    public WrapMetrics drawMoveDetails(Graphics g, Attack attack) {
        return moveDetailsPanel.draw(g, attack);
    }

    private void drawMoveButton(Graphics g, Button button, Move move) {
        button.panel().withMove(move).draw(g);
    }

    @Override
    public void update() {
        // Update move buttons and the back button
        moveButtons.update();

        Player player = Game.getPlayer();
        Battle currentBattle = view.getCurrentBattle();

        // Get the Pokemon that is attacking and their corresponding move list
        ActivePokemon front = player.front();

        for (int i = 0; i < selectedMoveList.size(); i++) {
            if (moveButtons.get(i).checkConsumePress()) {
                lastMoveUsed = i;

                // Execute the move if valid
                if (Move.validMove(currentBattle, front, selectedMoveList.get(i), true)) {
                    player.performAction(currentBattle, TrainerAction.FIGHT);
                    view.setVisualState(VisualState.MESSAGE);
                    view.cycleMessage();
                }
                // An invalid move -- Don't let them select it
                else {
                    view.cycleMessage();
                    view.setVisualState(VisualState.INVALID_FIGHT);
                }
            }
        }

        // Return to main battle menu
        view.updateBackButton();
    }

    public void resetLastMoveUsed() {
        this.lastMoveUsed = 0;
    }

    @Override
    public ButtonList getButtons() {
        return this.moveButtons;
    }
}
