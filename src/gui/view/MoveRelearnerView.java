package gui.view;

import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import main.Global;
import trainer.Trainer;

import java.awt.Graphics;

public class MoveRelearnerView extends View {
    private final static int NUM_MOVE_BUTTONS = 8;
    private final static int NUM_BUTTONS = NUM_MOVE_BUTTONS + Trainer.MAX_POKEMON + 2;
    private final static int LEARN_MOVE = NUM_BUTTONS - 1;
    private final static int RETURN = NUM_BUTTONS - 2;

    private final DrawPanel movesPanel;
    private final DrawPanel heartScalePanel;
    private final DrawPanel descriptionPanel;
    private final DrawPanel partyPanel;

    private final Button[] buttons;
    private final Button[] moveButtons;
    private final Button[] pokemonButtons;
    private final Button learnMoveButton;
    private final Button returnButton;

    private int selectedIndex;

    MoveRelearnerView() {
        int spacing = 20;
        int buttonHeight = 38;

        movesPanel = new DrawPanel(
                spacing,
                spacing,
                (Global.GAME_SIZE.width - 3*spacing)/2,
                Global.GAME_SIZE.height - 3*spacing - buttonHeight)
                .withBlackOutline()
                .withFullTransparency();

        heartScalePanel = new DrawPanel(
                movesPanel.x,
                movesPanel.bottomY() + spacing,
                movesPanel.width,
                buttonHeight)
                .withBlackOutline()
                .withFullTransparency();

        descriptionPanel = new DrawPanel(
                movesPanel.rightX() + spacing,
                movesPanel.y,
                movesPanel.width,
                (Global.GAME_SIZE.height - 4*spacing - buttonHeight)/3)
                .withBlackOutline()
                .withFullTransparency();

        partyPanel = new DrawPanel(
                descriptionPanel.x,
                descriptionPanel.bottomY() + spacing,
                descriptionPanel.width,
                Global.GAME_SIZE.height - 4*spacing - buttonHeight - descriptionPanel.height)
                .withBlackOutline()
                .withFullTransparency();

        moveButtons = movesPanel.getButtons(10, NUM_MOVE_BUTTONS, 1, 0, new int[] { NUM_MOVE_BUTTONS, RETURN, NUM_MOVE_BUTTONS, RETURN});
        pokemonButtons = partyPanel.getButtons(10, Trainer.MAX_POKEMON, 1, NUM_MOVE_BUTTONS, new int[] { 0, RETURN, 0, RETURN });

        learnMoveButton = new Button(
                partyPanel.x,
                partyPanel.bottomY() + spacing,
                (partyPanel.width - spacing)/2,
                buttonHeight,
                ButtonHoverAction.BOX,
                new int[] { RETURN, NUM_MOVE_BUTTONS + Trainer.MAX_POKEMON - 1, RETURN, NUM_MOVE_BUTTONS }
        );

        returnButton = new Button(
                learnMoveButton.rightX() + spacing,
                learnMoveButton.y,
                learnMoveButton.width,
                learnMoveButton.height,
                ButtonHoverAction.BOX,
                new int[] { LEARN_MOVE, NUM_MOVE_BUTTONS + Trainer.MAX_POKEMON - 1, LEARN_MOVE, NUM_MOVE_BUTTONS }
        );

        buttons = new Button[NUM_BUTTONS];
        System.arraycopy(moveButtons, 0, buttons, 0, moveButtons.length);
        System.arraycopy(pokemonButtons, 0, buttons, NUM_MOVE_BUTTONS, pokemonButtons.length);
        buttons[LEARN_MOVE] = learnMoveButton;
        buttons[RETURN] = returnButton;
    }

    @Override
    public void update(int dt) {
        selectedIndex = Button.update(buttons, selectedIndex);
    }

    @Override
    public void draw(Graphics g) {
        BasicPanels.drawCanvasPanel(g);

        movesPanel.drawBackground(g);
        heartScalePanel.drawBackground(g);
        descriptionPanel.drawBackground(g);
        partyPanel.drawBackground(g);

        for (Button button : moveButtons) {
            button.blackOutline(g);
        }

        for (Button button : pokemonButtons) {
            button.blackOutline(g);
        }

        learnMoveButton.blackOutline(g);
        returnButton.blackOutline(g);

        for (Button button : buttons) {
            button.draw(g);
        }
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.POKEDEX_VIEW;
    }

    @Override
    public void movedToFront() {

    }
}
