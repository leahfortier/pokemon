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

    private final ButtonList buttons;
    private final Button[] moveButtons;

    private BattleView view;

    private ActivePokemon selected;
    private MoveList selectedMoves;
    private int lastMoveUsed;

    public FightState() {
        view = Game.instance().getBattleView();

        moveButtons = view.createPanelLayout(MoveList.MAX_MOVES)
                          .withButtonSetup(panel -> panel.asMovePanel(19, 16)
                                                         .withTransparentCount(2)
                                                         .withBorderPercentage(15)
                                                         .withBlackOutline())
                          .withPressIndex(this::pressMove)
                          .getButtons();
        buttons = new ButtonList(moveButtons);

        moveDetailsPanel = new MovePanel(view.getMenuPanelSizing(), 22, 18, 16)
                .withBorderPercentage(8)
                .withTransparentCount(2)
                .withMinDescFontSize(13);
    }

    @Override
    public void reset(BattleView view) {
        this.view = view;
        this.selected = null;
        this.selectedMoves = null;
        this.resetLastMoveUsed();
    }

    @Override
    public void set() {
        ActivePokemon front = Game.getPlayer().front();
        selectedMoves = front.getMoves(view.getCurrentBattle());
        if (front != selected || lastMoveUsed >= selectedMoves.size()) {
            selected = front;
            this.resetLastMoveUsed();
        }

        for (int i = 0; i < moveButtons.length; i++) {
            Button button = moveButtons[i];
            button.setActiveSkip(i < selectedMoves.size());
            if (button.isActive()) {
                button.panel().withMove(selectedMoves.get(i));
            }
        }

        buttons.setSelected(lastMoveUsed);
        buttons.setFalseHover();
    }

    @Override
    public void draw(Graphics g) {
        view.drawButtonsPanel(g);

        String message = view.getMessage(VisualState.INVALID_FIGHT, null);
        if (StringUtils.isNullOrEmpty(message)) {
            // Draw move details
            drawMoveDetails(g, selectedMoves.get(buttons.getSelected()).getAttack());
        } else {
            // Show unusable move message
            view.drawMenuMessagePanel(g, message);
        }

        buttons.draw(g);
    }

    public WrapMetrics drawMoveDetails(Graphics g, Attack attack) {
        return moveDetailsPanel.draw(g, attack);
    }

    private void pressMove(int index) {
        Player player = Game.getPlayer();
        Battle currentBattle = view.getCurrentBattle();
        Move move = selectedMoves.get(index);

        lastMoveUsed = index;

        // Execute the move if valid
        if (Move.validMove(currentBattle, selected, move, true)) {
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

    @Override
    public void update() {
        // Update move buttons and the back button
        buttons.update();
        if (buttons.consumeSelectedPress()) {
            view.setVisualState();
        }

        // Return to main battle menu
        view.updateBackButton();
    }

    private void resetLastMoveUsed() {
        this.lastMoveUsed = 0;
    }

    @Override
    public ButtonList getButtons() {
        return this.buttons;
    }
}
