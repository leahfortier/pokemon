package gui.view;

import battle.ActivePokemon;
import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.button.ButtonList;
import draw.button.ButtonPanel;
import draw.button.ButtonPanel.ButtonPanelSetup;
import draw.button.ButtonPressAction;
import draw.button.ButtonTransitions;
import draw.layout.ButtonLayout;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.LearnMovePanel;
import draw.panel.MovePanel;
import draw.panel.PanelList;
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
import java.util.List;

public class MoveRelearnerView extends View {
    private static final int MOVES_PER_PAGE = 8;
    private static final int NUM_BUTTONS = MOVES_PER_PAGE + Trainer.MAX_POKEMON + 4;
    private static final int MOVES = 0;
    private static final int LAST_MOVE = MOVES + MOVES_PER_PAGE - 1;
    private static final int PARTY = MOVES_PER_PAGE;
    private static final int LAST_PARTY = PARTY + Trainer.MAX_POKEMON - 1;
    private static final int LEARN_MOVE = NUM_BUTTONS - 1;
    private static final int RETURN = NUM_BUTTONS - 2;
    private static final int MOVES_RIGHT_ARROW = NUM_BUTTONS - 3;
    private static final int MOVES_LEFT_ARROW = NUM_BUTTONS - 4;

    private final PanelList panels;
    private final DrawPanel movesPanel;
    private final DrawPanel heartScalePanel;
    private final MovePanel descriptionPanel;
    private final DrawPanel partyPanel;
    private final DrawPanel[] selectedPokemonPanels;

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
                .withBorderPercentage(0)
                .withLabelSize(22);

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

        moveButtons = new ButtonLayout(movesPanel, MOVES_PER_PAGE, 1, 10)
                .withMissingBottomRow()
                .withStartIndex(MOVES)
                .withDefaultTransitions(new ButtonTransitions().right(PARTY).up(MOVES_LEFT_ARROW).left(PARTY).down(MOVES_RIGHT_ARROW))
                .withPressIndex(index -> selectedMove = learnableMoves.isEmpty() ? null : GeneralUtils.getPageValue(learnableMoves, pageNum, MOVES_PER_PAGE, index).getNewAttack())
                .withDrawSetup(panel -> panel.withBlackOutline().withFullTransparency())
                .getButtons();

        pokemonButtons = new ButtonLayout(partyPanel, Trainer.MAX_POKEMON, 1, 15)
                .withStartIndex(PARTY)
                .withDefaultTransitions(new ButtonTransitions().right(MOVES).up(RETURN).left(MOVES).down(RETURN))
                .withPressIndex(this::setSelectedPokemon)
                .withButtonSetup(panel -> panel.withBlackOutline()
                                               .withTransparentCount(2)
                                               .withBorderPercentage(15)
                                               .withLabelSize(22))
                .getButtons();

        // Panels that highlight the currently selected Pokemon with an outline
        int selectedSpacing = 5;
        selectedPokemonPanels = new DrawPanel[pokemonButtons.length];
        for (int i = 0; i < selectedPokemonPanels.length; i++) {
            ButtonPanel buttonPanel = pokemonButtons[i].panel();
            selectedPokemonPanels[i] = new DrawPanel(
                    buttonPanel.x - selectedSpacing,
                    buttonPanel.y - selectedSpacing,
                    buttonPanel.width + 2*selectedSpacing,
                    buttonPanel.height + 2*selectedSpacing
            ).withBlackOutline().withFullTransparency();
        }

        learnMoveButton = new Button(
                partyPanel.x,
                partyPanel.bottomY() + spacing,
                (partyPanel.width - spacing)/2,
                buttonHeight,
                new ButtonTransitions().right(RETURN).up(LAST_PARTY).left(RETURN).down(PARTY),
                () -> learnMovePanel = new LearnMovePanel((ActivePokemon)team.get(selectedPokemon), new Move(selectedMove)),
                textButtonSetup("Learn!", new Color(123, 213, 74))
        );

        returnButton = new Button(
                learnMoveButton.rightX() + spacing,
                learnMoveButton.y,
                learnMoveButton.width,
                learnMoveButton.height,
                new ButtonTransitions().right(LEARN_MOVE).up(LAST_PARTY).left(LEARN_MOVE).down(PARTY),
                ButtonPressAction.getExitAction(),
                textButtonSetup("Return", Color.YELLOW)
        );

        int arrowWidth = 35;
        int arrowHeight = 20;

        movesLeftButton = new Button(
                movesPanel.centerX() - arrowWidth*3,
                (moveButtons[MOVES_PER_PAGE - 1].bottomY() + movesPanel.bottomY())/2 - arrowHeight/2,
                arrowWidth,
                arrowHeight,
                ButtonHoverAction.BOX,
                new ButtonTransitions().right(MOVES_RIGHT_ARROW).up(LAST_MOVE).left(PARTY).down(RETURN),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, -1, totalPages())
        ).asArrow(Direction.LEFT);

        movesRightButton = new Button(
                movesPanel.centerX() + arrowWidth*2,
                movesLeftButton.y,
                arrowWidth,
                arrowHeight,
                ButtonHoverAction.BOX,
                new ButtonTransitions().right(PARTY).up(LAST_MOVE).left(MOVES_LEFT_ARROW).down(RETURN),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, 1, totalPages())
        ).asArrow(Direction.RIGHT);

        this.buttons = new ButtonList(NUM_BUTTONS);
        buttons.set(MOVES, moveButtons);
        buttons.set(PARTY, pokemonButtons);
        buttons.set(LEARN_MOVE, learnMoveButton);
        buttons.set(RETURN, returnButton);
        buttons.set(MOVES_LEFT_ARROW, movesLeftButton);
        buttons.set(MOVES_RIGHT_ARROW, movesRightButton);

        this.panels = new PanelList(
                movesPanel, descriptionPanel, heartScalePanel, partyPanel
        ).add(selectedPokemonPanels);
    }

    private ButtonPanelSetup textButtonSetup(String label, Color color) {
        return panel -> panel.greyInactive()
                             .withLabel(label, 20)
                             .withBorderlessTransparentBackground()
                             .withBackgroundColor(color)
                             .withBlackOutline();
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

    private void drawSetup() {
        // Party panel background is the type colors of the selected Pokemon
        partyPanel.withTypeColors(team.get(selectedPokemon));

        // Moves panel background is the type color of the selected move
        // If no selected move use Normal-type colors
        if (selectedMove == null) {
            movesPanel.withBackgroundColor(Type.NORMAL.getColor());
            descriptionPanel.withBackgroundColor(Type.NORMAL.getColor());
        } else {
            // Don't need to set up description panel as is handled separately in the MovePanel class
            movesPanel.withBackgroundColor(selectedMove.getActualType().getColor());
            descriptionPanel.skipDraw();
        }

        // Number of heart scales
        heartScalePanel.withLabel("Heart Scales: " + numHeartScales());

        // Set up Pokemon buttons
        for (int i = 0; i < pokemonButtons.length; i++) {
            // Highlight selected
            selectedPokemonPanels[i].skipDraw(i != selectedPokemon);

            // Set type color background, image and name labels
            ButtonPanel pokemonPanel = pokemonButtons[i].panel();
            if (i < team.size()) {
                PartyPokemon pokemon = team.get(i);
                pokemonPanel.withTypeColors(pokemon)
                            .withImageLabel(
                                    partyTiles.getTile(pokemon.getTinyImageName()),
                                    pokemon.getActualName()
                            );
            } else {
                pokemonPanel.skipDraw();
            }
        }

        List<AttackNamesies> moves = this.getDisplayMoves();
        for (int i = 0; i < moveButtons.length; i++) {
            moveButtons[i].panel().skipDraw(i >= moves.size());
        }
    }

    private void drawMoveButtons(Graphics g) {
        List<AttackNamesies> moves = this.getDisplayMoves();
        for (int i = 0; i < moves.size(); i++) {
            Attack attack = moves.get(i).getNewAttack();
            ButtonPanel movePanel = moveButtons[i].panel();

            // Left label attack name
            movePanel.drawLeftLabel(g, 18, attack.getName());

            // Draw type and category images
            int moveImageSpacing = 20;
            BufferedImage typeImage = attack.getActualType().getImage();
            int imageY = movePanel.centerY() - typeImage.getHeight()/2;
            int imageX = movePanel.rightX() - moveImageSpacing - typeImage.getWidth();
            g.drawImage(typeImage, imageX, imageY, null);

            BufferedImage categoryImage = attack.getCategory().getImage();
            imageX -= categoryImage.getWidth() + moveImageSpacing;
            g.drawImage(categoryImage, imageX, imageY, null);
        }
    }

    @Override
    public void draw(Graphics g) {
        drawSetup();

        // Background
        BasicPanels.drawCanvasPanel(g);
        panels.drawAll(g);
        buttons.drawPanels(g);

        // Will draw the background for this panel here
        if (selectedMove != null) {
            drawMoveDetails(g, selectedMove);
        }

        // Learnable moves
        drawMoveButtons(g);

        // Page numbers
        TextUtils.drawPageNumbers(g, 22, movesLeftButton, movesRightButton, pageNum, totalPages());

        // Learning movessss
        if (learnMovePanel != null) {
            learnMovePanel.draw(g);
        }

        // Button hover action
        buttons.drawHover(g);
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

            int numMoves = this.getDisplayMoves().size();
            for (int i = 0; i < MOVES_PER_PAGE; i++) {
                moveButtons[i].setActive(i < numMoves);
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

    private List<AttackNamesies> getDisplayMoves() {
        return GeneralUtils.pageValues(this.learnableMoves, pageNum, MOVES_PER_PAGE);
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
