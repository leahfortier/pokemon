package gui.view;

import battle.ActivePokemon;
import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.button.ButtonList;
import draw.button.ButtonPressAction;
import draw.button.ButtonTransitions;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.LearnMovePanel;
import draw.panel.MovePanel;
import draw.panel.WrapPanel.WrapMetrics;
import gui.GameData;
import gui.TileSet;
import input.InputControl;
import item.ItemNamesies;
import item.bag.Bag;
import main.Game;
import main.Global;
import map.Direction;
import pokemon.active.PartyPokemon;
import trainer.Trainer;
import trainer.player.Player;
import type.Type;
import util.GeneralUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
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
    private final MovePanel descriptionPanel;
    private final DrawPanel partyPanel;

    private final ButtonList buttons;
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

        descriptionPanel = new MovePanel(
                movesPanel.rightX() + spacing,
                movesPanel.y,
                movesPanel.width,
                (Global.GAME_SIZE.height - 4*spacing - buttonHeight)/3,
                24, 19, 16
        )
                .withTransparentCount(2)
                .withBorderPercentage(0)
                .withMinDescFontSize(15);

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
                new ButtonTransitions().right(MOVES_PER_PAGE).up(MOVES_LEFT_ARROW).left(MOVES_PER_PAGE).down(MOVES_RIGHT_ARROW),
                index -> selectedMove = learnableMoves.isEmpty() ? null : GeneralUtils.getPageValue(learnableMoves, pageNum, MOVES_PER_PAGE, index).getNewAttack()
        );

        pokemonButtons = partyPanel.getButtons(
                15,
                Trainer.MAX_POKEMON,
                1,
                MOVES_PER_PAGE,
                new ButtonTransitions().right(0).up(RETURN).left(0).down(RETURN),
                this::setSelectedPokemon
        );

        learnMoveButton = new Button(
                partyPanel.x,
                partyPanel.bottomY() + spacing,
                (partyPanel.width - spacing)/2,
                buttonHeight,
                ButtonHoverAction.BOX,
                new ButtonTransitions().right(RETURN).up(MOVES_PER_PAGE + Trainer.MAX_POKEMON - 1).left(RETURN).down(MOVES_PER_PAGE),
                () -> learnMovePanel = new LearnMovePanel((ActivePokemon)team.get(selectedPokemon), new Move(selectedMove))
        );

        returnButton = new Button(
                learnMoveButton.rightX() + spacing,
                learnMoveButton.y,
                learnMoveButton.width,
                learnMoveButton.height,
                ButtonHoverAction.BOX,
                new ButtonTransitions().right(LEARN_MOVE).up(MOVES_PER_PAGE + Trainer.MAX_POKEMON - 1).left(LEARN_MOVE).down(MOVES_PER_PAGE),
                ButtonPressAction.getExitAction()
        );

        int arrowWidth = 35;
        int arrowHeight = 20;

        movesLeftButton = new Button(
                movesPanel.centerX() - arrowWidth*3,
                (moveButtons[MOVES_PER_PAGE - 1].bottomY() + movesPanel.bottomY())/2 - arrowHeight/2,
                arrowWidth,
                arrowHeight,
                ButtonHoverAction.BOX,
                new ButtonTransitions().right(MOVES_RIGHT_ARROW).up(MOVES_PER_PAGE - 1).left(MOVES_PER_PAGE).down(RETURN),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, -1, totalPages())
        );

        movesRightButton = new Button(
                movesPanel.centerX() + arrowWidth*2,
                movesLeftButton.y,
                arrowWidth,
                arrowHeight,
                ButtonHoverAction.BOX,
                new ButtonTransitions().right(MOVES_PER_PAGE).up(MOVES_PER_PAGE - 1).left(MOVES_LEFT_ARROW).down(RETURN),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, 1, totalPages())
        );

        Button[] buttons = new Button[NUM_BUTTONS];
        System.arraycopy(moveButtons, 0, buttons, 0, moveButtons.length);
        System.arraycopy(pokemonButtons, 0, buttons, MOVES_PER_PAGE, pokemonButtons.length);
        buttons[LEARN_MOVE] = learnMoveButton;
        buttons[RETURN] = returnButton;
        buttons[MOVES_LEFT_ARROW] = movesLeftButton;
        buttons[MOVES_RIGHT_ARROW] = movesRightButton;
        this.buttons = new ButtonList(buttons);
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

        buttons.update();
        if (buttons.consumeSelectedPress()) {
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
            drawMoveDetails(g, selectedMove);
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

        Iterator<AttackNamesies> iterator = this.getIterator();
        for (int i = 0; i < MOVES_PER_PAGE && iterator.hasNext(); i++) {
            Attack attack = iterator.next().getNewAttack();
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

        buttons.draw(g);
    }

    public WrapMetrics drawMoveDetails(Graphics g, Attack attack) {
        return descriptionPanel.draw(g, attack);
    }

    private void updateActiveButtons() {
        if (learnMovePanel != null) {
            buttons.setInactive();
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
        return GeneralUtils.getTotalPages(this.learnableMoves.size(), MOVES_PER_PAGE);
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

        PartyPokemon selected = this.team.get(index);
        this.learnableMoves = selected.isEgg() ? new ArrayList<>() : selected.getLearnableMoves();
        this.selectedMove = this.learnableMoves.isEmpty() ? null : this.learnableMoves.get(0).getNewAttack();
        this.pageNum = 0;

        this.updateActiveButtons();
    }
}
