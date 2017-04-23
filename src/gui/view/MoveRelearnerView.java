package gui.view;

import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import gui.GameData;
import gui.TileSet;
import item.ItemNamesies;
import item.bag.Bag;
import main.Game;
import main.Global;
import map.Direction;
import pokemon.ActivePokemon;
import trainer.Trainer;
import trainer.player.Player;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

public class MoveRelearnerView extends View {
    private static final int MOVES_PER_PAGE = 8;
    private static final int NUM_BUTTONS = MOVES_PER_PAGE + Trainer.MAX_POKEMON + 4;
    private static final int LEARN_MOVE = NUM_BUTTONS - 1;
    private static final int RETURN = NUM_BUTTONS - 2;
    private static final int MOVES_RIGHT_ARROW = NUM_BUTTONS - 3;
    private static final int MOVES_LEFT_ARROW = NUM_BUTTONS - 4;

    private final DrawPanel movesPanel;
    private final DrawPanel heartScalePanel;
    private final DrawPanel descriptionPanel;
    private final DrawPanel partyPanel;

    private final Button[] buttons;
    private final Button[] moveButtons;
    private final Button[] pokemonButtons;
    private final Button learnMoveButton;
    private final Button returnButton;
    private final Button movesRightButton;
    private final Button movesLeftButton;

    private TileSet itemTiles;
    private TileSet partyTiles;

    private List<ActivePokemon> team;
    private Bag bag;

    private int selectedIndex;
    private int selectedPokemon;

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
                .withBackgroundColor(new Color(248, 179, 249))
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

        moveButtons = movesPanel.getButtons(10, MOVES_PER_PAGE + 1, 1, MOVES_PER_PAGE, 1, 0, new int[] {MOVES_PER_PAGE, RETURN, MOVES_PER_PAGE, RETURN});
        pokemonButtons = partyPanel.getButtons(15, Trainer.MAX_POKEMON, 1, MOVES_PER_PAGE, new int[] { 0, RETURN, 0, RETURN });

        learnMoveButton = new Button(
                partyPanel.x,
                partyPanel.bottomY() + spacing,
                (partyPanel.width - spacing)/2,
                buttonHeight,
                ButtonHoverAction.BOX,
                new int[] { RETURN, MOVES_PER_PAGE + Trainer.MAX_POKEMON - 1, RETURN, MOVES_PER_PAGE}
        );

        returnButton = new Button(
                learnMoveButton.rightX() + spacing,
                learnMoveButton.y,
                learnMoveButton.width,
                learnMoveButton.height,
                ButtonHoverAction.BOX,
                new int[] { LEARN_MOVE, MOVES_PER_PAGE + Trainer.MAX_POKEMON - 1, LEARN_MOVE, MOVES_PER_PAGE}
        );

        int arrowWidth = 35;
        int arrowHeight = 20;

        movesLeftButton = new Button(
                movesPanel.centerX() - arrowWidth*3,
                (moveButtons[MOVES_PER_PAGE - 1].bottomY() + movesPanel.bottomY())/2 - arrowHeight/2,
                arrowWidth,
                arrowHeight,
                ButtonHoverAction.BOX,
                new int[] { MOVES_RIGHT_ARROW, MOVES_PER_PAGE - 1, MOVES_PER_PAGE, RETURN }
        );

        movesRightButton = new Button(
                movesPanel.centerX() + arrowWidth*2,
                movesLeftButton.y,
                arrowWidth,
                arrowHeight,
                ButtonHoverAction.BOX,
                new int[] { MOVES_PER_PAGE, MOVES_PER_PAGE - 1, MOVES_LEFT_ARROW, RETURN }
        );

        buttons = new Button[NUM_BUTTONS];
        System.arraycopy(moveButtons, 0, buttons, 0, moveButtons.length);
        System.arraycopy(pokemonButtons, 0, buttons, MOVES_PER_PAGE, pokemonButtons.length);
        buttons[LEARN_MOVE] = learnMoveButton;
        buttons[RETURN] = returnButton;
        buttons[MOVES_LEFT_ARROW] = movesLeftButton;
        buttons[MOVES_RIGHT_ARROW] = movesRightButton;
    }

    @Override
    public void update(int dt) {
        selectedIndex = Button.update(buttons, selectedIndex);

        for (int i = 0; i < pokemonButtons.length; i++) {
            Button pokemonButton = pokemonButtons[i];
            if (pokemonButton.checkConsumePress()) {
                selectedPokemon = i;
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        BasicPanels.drawCanvasPanel(g);

        movesPanel.drawBackground(g);
        heartScalePanel.drawBackground(g);
        heartScalePanel.label(g, 24, "Heart Scales: " + bag.getQuantity(ItemNamesies.HEART_SCALE));

        descriptionPanel.drawBackground(g);
        partyPanel.drawBackground(g);

        for (Button button : moveButtons) {
            button.blackOutline(g);
        }

        for (int i = 0; i < team.size(); i++) {
            ActivePokemon pokemon = team.get(i);
            Button pokemonButton = pokemonButtons[i];

            DrawPanel buttonPanel = new DrawPanel(pokemonButton)
                    .withTypeColors(pokemon)
                    .withBlackOutline()
                    .withTransparentCount(2)
                    .withBorderPercentage(15);

            // Highlight selected
            if (i == selectedPokemon) {
                int spacing = 5;
                new DrawPanel(
                        buttonPanel.x - spacing,
                        buttonPanel.y - spacing,
                        buttonPanel.width + 2*spacing,
                        buttonPanel.height + 2*spacing)
                        .withBlackOutline()
                        .withFullTransparency()
                        .drawBackground(g);
            }

            buttonPanel.drawBackground(g);
            buttonPanel.imageLabel(g, 22, partyTiles.getTile(pokemon.getTinyImageName()), pokemon.getName());
        }

        learnMoveButton.blackOutline(g);
        returnButton.blackOutline(g);

        movesLeftButton.drawArrow(g, Direction.LEFT);
        movesRightButton.drawArrow(g, Direction.RIGHT);

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
        Player player = Game.getPlayer();
        GameData data = Game.getData();

        this.itemTiles = data.getItemTiles();
        this.partyTiles = data.getPartyTiles();

        this.team = player.getTeam();
        this.bag = player.getBag();
    }
}
