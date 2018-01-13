package gui.view;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.LearnMovePanel;
import gui.GameData;
import gui.TileSet;
import input.InputControl;
import item.ItemNamesies;
import item.bag.Bag;
import main.Game;
import main.Global;
import map.Direction;
import battle.ActivePokemon;
import pokemon.PartyPokemon;
import trainer.Trainer;
import trainer.player.Player;
import type.Type;
import util.GeneralUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Iterator;
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

    private TileSet partyTiles;

    private List<PartyPokemon> team;
    private List<AttackNamesies> learnableMoves;
    private Bag bag;

    private int selectedButton;
    private int selectedPokemon;
    private Attack selectedMove;
    private int pageNum;
    private LearnMovePanel learnMovePanel;

    MoveRelearnerView() {
        int spacing = 20;
        int buttonHeight = 38;

        movesPanel = new DrawPanel(
                spacing,
                spacing,
                (Global.GAME_SIZE.width - 3*spacing)/2,
                Global.GAME_SIZE.height - 3*spacing - buttonHeight
        )
                .withBlackOutline()
                .withTransparentCount(2)
                .withBorderPercentage(0);

        heartScalePanel = new DrawPanel(
                movesPanel.x,
                movesPanel.bottomY() + spacing,
                movesPanel.width,
                buttonHeight
        )
                .withBlackOutline()
                .withBackgroundColor(new Color(248, 179, 249))
                .withTransparentBackground()
                .withBorderPercentage(0);

        descriptionPanel = new DrawPanel(
                movesPanel.rightX() + spacing,
                movesPanel.y,
                movesPanel.width,
                (Global.GAME_SIZE.height - 4*spacing - buttonHeight)/3
        )
                .withBlackOutline()
                .withTransparentCount(2)
                .withBorderPercentage(0);

        partyPanel = new DrawPanel(
                descriptionPanel.x,
                descriptionPanel.bottomY() + spacing,
                descriptionPanel.width,
                Global.GAME_SIZE.height - 4*spacing - buttonHeight - descriptionPanel.height
        )
                .withBlackOutline()
                .withTransparentCount(2)
                .withBorderPercentage(0);

        moveButtons = movesPanel.getButtons(
                10,
                MOVES_PER_PAGE + 1, 1,
                MOVES_PER_PAGE, 1,
                0,
                new int[] { MOVES_PER_PAGE, MOVES_LEFT_ARROW, MOVES_PER_PAGE, MOVES_RIGHT_ARROW },
                index -> selectedMove = learnableMoves.isEmpty() ? null : GeneralUtils.getPageValue(learnableMoves, pageNum, MOVES_PER_PAGE, index).getAttack()
        );

        pokemonButtons = partyPanel.getButtons(
                15,
                Trainer.MAX_POKEMON,
                1,
                MOVES_PER_PAGE,
                new int[] { 0, RETURN, 0, RETURN },
                this::setSelectedPokemon
        );

        learnMoveButton = new Button(
                partyPanel.x,
                partyPanel.bottomY() + spacing,
                (partyPanel.width - spacing)/2,
                buttonHeight,
                ButtonHoverAction.BOX,
                new int[] { RETURN, MOVES_PER_PAGE + Trainer.MAX_POKEMON - 1, RETURN, MOVES_PER_PAGE },
                () -> learnMovePanel = new LearnMovePanel((ActivePokemon)team.get(selectedPokemon), new Move(selectedMove))
        );

        returnButton = Button.createExitButton(
                learnMoveButton.rightX() + spacing,
                learnMoveButton.y,
                learnMoveButton.width,
                learnMoveButton.height,
                ButtonHoverAction.BOX,
                new int[] { LEARN_MOVE, MOVES_PER_PAGE + Trainer.MAX_POKEMON - 1, LEARN_MOVE, MOVES_PER_PAGE }
        );

        int arrowWidth = 35;
        int arrowHeight = 20;

        movesLeftButton = new Button(
                movesPanel.centerX() - arrowWidth*3,
                (moveButtons[MOVES_PER_PAGE - 1].bottomY() + movesPanel.bottomY())/2 - arrowHeight/2,
                arrowWidth,
                arrowHeight,
                ButtonHoverAction.BOX,
                new int[] { MOVES_RIGHT_ARROW, MOVES_PER_PAGE - 1, MOVES_PER_PAGE, RETURN },
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, -1, totalPages())
        );

        movesRightButton = new Button(
                movesPanel.centerX() + arrowWidth*2,
                movesLeftButton.y,
                arrowWidth,
                arrowHeight,
                ButtonHoverAction.BOX,
                new int[] { MOVES_PER_PAGE, MOVES_PER_PAGE - 1, MOVES_LEFT_ARROW, RETURN },
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, 1, totalPages())
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
        if (learnMovePanel != null) {
            learnMovePanel.update();
            if (learnMovePanel.isFinished()) {
                // If a move was actually learned, remove a heart scale
                if (learnMovePanel.learnedMove()) {
                    bag.removeItem(ItemNamesies.HEART_SCALE);
                }

                learnMovePanel = null;
                setSelectedPokemon(selectedPokemon);
                updateActiveButtons();
                return;
            }
        }

        selectedButton = Button.update(buttons, selectedButton);
        if (buttons[selectedButton].checkConsumePress()) {
            updateActiveButtons();
        }

        InputControl.instance().popViewIfEscaped();
    }

    @Override
    public void draw(Graphics g) {
        BasicPanels.drawCanvasPanel(g);

        if (selectedMove == null) {
            movesPanel.withBackgroundColor(Type.NORMAL.getColor());
            descriptionPanel.withBackgroundColor(Type.NORMAL.getColor()).drawBackground(g);
        } else {
            movesPanel.withBackgroundColor(selectedMove.getActualType().getColor());
            descriptionPanel.drawMovePanel(g, selectedMove);
        }

        movesPanel.drawBackground(g);

        heartScalePanel.drawBackground(g);
        heartScalePanel.label(g, 22, "Heart Scales: " + numHeartScales());

        partyPanel.withTypeColors(team.get(selectedPokemon)).drawBackground(g);

        for (int i = 0; i < team.size(); i++) {
            PartyPokemon pokemon = team.get(i);
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
                        buttonPanel.height + 2*spacing
                )
                        .withBlackOutline()
                        .withFullTransparency()
                        .drawBackground(g);
            }

            buttonPanel.drawBackground(g);
            buttonPanel.imageLabel(g, 22, partyTiles.getTile(pokemon.getTinyImageName()), pokemon.getActualName());
        }

        Iterator<AttackNamesies> iterator = GeneralUtils.pageIterator(this.learnableMoves, pageNum, MOVES_PER_PAGE);
        for (int i = 0; i < MOVES_PER_PAGE && iterator.hasNext(); i++) {
            Attack attack = iterator.next().getAttack();
            Button moveButton = moveButtons[i];
            moveButton.blackOutline(g);
            new DrawPanel(moveButton).drawLeftLabel(g, 18, attack.getName());

            int moveImageSpacing = 20;
            BufferedImage typeImage = attack.getActualType().getImage();
            int imageY = moveButton.centerY() - typeImage.getHeight()/2;
            int imageX = moveButton.rightX() - moveImageSpacing - typeImage.getWidth();
            g.drawImage(typeImage, imageX, imageY, null);

            BufferedImage categoryImage = attack.getCategory().getImage();
            imageX -= categoryImage.getWidth() + moveImageSpacing;
            g.drawImage(categoryImage, imageX, imageY, null);
        }

        learnMoveButton.fillBorderLabel(g, new Color(123, 213, 74), 22, "Learn!");
        if (!learnMoveButton.isActive()) {
            learnMoveButton.greyOut(g);
        }

        returnButton.fillBorderLabel(g, Color.YELLOW, 22, "Return");

        movesLeftButton.drawArrow(g, Direction.LEFT);
        movesRightButton.drawArrow(g, Direction.RIGHT);

        int totalPages = totalPages();
        TextUtils.drawCenteredString(g, (totalPages == 0 ? 0 : pageNum + 1) + "/" + totalPages, movesPanel.centerX(), movesLeftButton.centerY());

        if (learnMovePanel != null) {
            learnMovePanel.draw(g);
        }

        for (Button button : buttons) {
            button.draw(g);
        }
    }

    private void updateActiveButtons() {
        if (learnMovePanel != null) {
            for (Button button : buttons) {
                button.setActive(false);
            }
        } else {
            for (int i = 0; i < pokemonButtons.length; i++) {
                pokemonButtons[i].setActive(i < team.size());
            }

            Iterator<AttackNamesies> iterator = getIterator();
            for (int i = 0; i < MOVES_PER_PAGE; i++) {
                if (iterator.hasNext()) {
                    iterator.next();
                    moveButtons[i].setActive(true);
                } else {
                    moveButtons[i].setActive(false);
                }
            }

            this.learnMoveButton.setActive(selectedMove != null && numHeartScales() > 0);

            boolean activeArrows = totalPages() > 0;
            this.movesLeftButton.setActive(activeArrows);
            this.movesRightButton.setActive(activeArrows);

            returnButton.setActive(true);
        }
    }

    private int totalPages() {
        return (int)Math.ceil(1.0*this.learnableMoves.size()/MOVES_PER_PAGE);
    }

    private int numHeartScales() {
        return bag.getQuantity(ItemNamesies.HEART_SCALE);
    }

    private Iterator<AttackNamesies> getIterator() {
        return GeneralUtils.pageIterator(this.learnableMoves, pageNum, MOVES_PER_PAGE);
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.MOVE_RELEARNER_VIEW;
    }

    @Override
    public void movedToFront() {
        Player player = Game.getPlayer();
        GameData data = Game.getData();

        this.partyTiles = data.getPartyTiles();

        this.team = player.getTeam();
        this.bag = player.getBag();
        this.setSelectedPokemon(0);
    }

    private void setSelectedPokemon(int index) {
        this.selectedPokemon = index;
        this.learnableMoves = this.team.get(index).getLearnableMoves();
        this.selectedMove = this.learnableMoves.isEmpty() ? null : this.learnableMoves.get(0).getAttack();
        this.pageNum = 0;
    }
}
