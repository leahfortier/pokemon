package gui.view;

import battle.attack.Attack;
import draw.DrawUtils;
import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonList;
import draw.button.ButtonPanel;
import draw.button.ButtonPanel.ButtonPanelSetup;
import draw.button.ButtonPressAction;
import draw.button.ButtonTransitions;
import draw.layout.ArrowLayout;
import draw.layout.ButtonLayout;
import draw.layout.TabLayout;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.MovePanel;
import draw.panel.PanelList;
import draw.panel.StatPanel;
import draw.panel.WrapPanel.WrapMetrics;
import gui.GameData;
import gui.TileSet;
import input.InputControl;
import main.Game;
import map.Direction;
import pokemon.active.MoveList;
import pokemon.active.PartyPokemon;
import pokemon.breeding.Eggy;
import trainer.Trainer;
import trainer.player.PC;
import trainer.player.Player;
import type.PokeType;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

public class PCView extends View {
    private static final int NUM_BUTTONS = PC.BOX_HEIGHT*PC.BOX_WIDTH + Trainer.MAX_POKEMON + MoveList.MAX_MOVES + 6;
    private static final int BOX = 0;
    private static final int BOTTOM_MIDDLE_BOX = PC.BOX_HEIGHT*PC.BOX_WIDTH - PC.BOX_WIDTH/2;
    private static final int PARTY = BOX + PC.BOX_HEIGHT*PC.BOX_WIDTH;
    private static final int LAST_PARTY = PARTY + Trainer.MAX_POKEMON - 1;
    private static final int MOVES = PARTY + Trainer.MAX_POKEMON;
    private static final int LAST_MOVE = MOVES + MoveList.MAX_MOVES - 1;
    private static final int RETURN = NUM_BUTTONS - 1;
    private static final int RELEASE = NUM_BUTTONS - 2;
    private static final int DEPOSIT_WITHDRAW = NUM_BUTTONS - 3;
    private static final int SWITCH = NUM_BUTTONS - 4;
    private static final int RIGHT_ARROW = NUM_BUTTONS - 5;
    private static final int LEFT_ARROW = NUM_BUTTONS - 6;

    private final PanelList panels;
    private final DrawPanel boxPanel;
    private final DrawPanel boxNamePanel;
    private final DrawPanel infoPanel;
    private final DrawPanel imagePanel;
    private final StatPanel statsPanel;
    private final MovePanel moveDetailsPanel;

    private final ButtonList buttons;
    private final Button[][] boxButtons;
    private final Button[] partyButtons;
    private final Button[] moveButtons;
    private final Button leftButton;
    private final Button rightButton;
    private final Button switchButton;
    private final Button depositWithdrawButton;
    private final Button releaseButton;

    private PC pc;
    private TileSet partyTiles;
    private TileSet spriteTiles;

    private PartyPokemon selected;
    private boolean party;
    private boolean depositClicked;
    private boolean switchClicked;
    private Color highlightColor;

    PCView() {
        boxPanel = new DrawPanel(40, 40, 350, 418)
                .withTransparentCount(2)
                .withBorderPercentage(0)
                .withBlackOutline();

        boxNamePanel = new DrawPanel(boxPanel.x, boxPanel.y, boxPanel.width, 37)
                .withBackgroundColor(null)
                .withBlackOutline()
                .withLabelSize(20);

        DrawPanel partyPanel = new DrawPanel(boxPanel.x, 478, boxPanel.width, 82)
                .withBackgroundColor(Color.RED)
                .withTransparentCount(2)
                .withBorderPercentage(0)
                .withBlackOutline();

        infoPanel = new DrawPanel(410, boxPanel.y, boxPanel.width, 462)
                .withTransparentBackground()
                .withBorderPercentage(0)
                .withBlackOutline();

        DrawPanel basicInfoPanel = new DrawPanel(infoPanel.x, infoPanel.y, infoPanel.width, 190)
                .withFullTransparency()
                .withBlackOutline();

        int statsPanelHeight = 148;
        int buttonHeight = 38;
        statsPanel = new StatPanel(
                infoPanel.x,
                infoPanel.bottomY() - buttonHeight - statsPanelHeight,
                infoPanel.width,
                statsPanelHeight + DrawUtils.OUTLINE_SIZE,
                16, 14
        ).withFullTransparency()
         .withBlackOutline();

        moveDetailsPanel = new MovePanel(statsPanel, 20, 18, 16)
                .withFullTransparency()
                .withMinDescFontSize(14);

        DrawPanel movesPanel = new DrawPanel(
                infoPanel.x,
                basicInfoPanel.bottomY() - DrawUtils.OUTLINE_SIZE,
                infoPanel.width,
                infoPanel.height - basicInfoPanel.height - statsPanel.height - buttonHeight + 3*DrawUtils.OUTLINE_SIZE
        ).withFullTransparency()
         .withBlackOutline();

        imagePanel = new DrawPanel(
                infoPanel.x + 18,
                infoPanel.y + 18,
                104,
                104
        ).withFullTransparency()
         .withBlackOutline();

        int pokemonButtonSize = 40;

        // PC panel without the box name panel
        DrawPanel pokemonPanel = new DrawPanel(
                boxPanel.x,
                boxNamePanel.bottomY(),
                boxPanel.width,
                boxPanel.bottomY() - boxNamePanel.bottomY()
        ).withNoBackground();

        ButtonLayout pokemonLayout = new ButtonLayout(pokemonPanel, PC.BOX_WIDTH, PC.BOX_HEIGHT, pokemonButtonSize, pokemonButtonSize)
                .withMissingBottomRow()
                .withStartIndex(BOX)
                .withDefaultTransitions(new ButtonTransitions().right(MOVES).down(RIGHT_ARROW).up(PARTY))
                .withPressIndex(this::pressBoxPokemon);

        boxButtons = pokemonLayout.getGridButtons();

        partyButtons = new ButtonLayout(partyPanel, 1, Trainer.MAX_POKEMON, pokemonButtonSize, pokemonButtonSize)
                .withStartIndex(PARTY)
                .withDefaultTransitions(new ButtonTransitions().right(RETURN).up(RIGHT_ARROW).left(RETURN).down(BOX))
                .withPressIndex(this::pressPartyPokemon)
                .withButtonSetup(ButtonPanel::skipInactive)
                .getButtons();

        moveButtons = new ButtonLayout(movesPanel, 2, MoveList.MAX_MOVES/2, 8)
                .withDrawSetup(panel -> panel.withTransparentCount(2)
                                             .withBorderPercentage(20)
                                             .withBlackOutline()
                                             .withLabelSize(16))
                .withStartIndex(MOVES)
                .withDefaultTransitions(new ButtonTransitions().up(RETURN).down(DEPOSIT_WITHDRAW).right(BOX).left(RIGHT_ARROW))
                .getButtons();

        ArrowLayout arrowPanels = pokemonLayout.getArrowLayout();
        leftButton = new Button(
                arrowPanels.getLeftPanel(),
                new ButtonTransitions().right(RIGHT_ARROW).up(BOTTOM_MIDDLE_BOX - 1).down(PARTY).left(RELEASE),
                () -> {
                    pc.incrementBox(-1);
                    movedToFront();
                }
        ).asArrow(Direction.LEFT);

        rightButton = new Button(
                arrowPanels.getRightPanel(),
                new ButtonTransitions().right(SWITCH).up(BOTTOM_MIDDLE_BOX).left(LEFT_ARROW).down(PARTY),
                () -> {
                    pc.incrementBox(1);
                    movedToFront();
                }
        ).asArrow(Direction.RIGHT);

        Button[] fakeTabs = new TabLayout(infoPanel, 3, 38).asBottomTabs().asInsetTabs().getTabs();

        switchButton = new Button(
                fakeTabs[0].panel(),
                new ButtonTransitions().right(DEPOSIT_WITHDRAW).left(RIGHT_ARROW).down(RETURN).up(LAST_MOVE),
                () -> switchClicked = !switchClicked,
                textButtonSetup("Switch")
        );

        depositWithdrawButton = new Button(
                fakeTabs[1].panel(),
                new ButtonTransitions().right(RELEASE).left(SWITCH).down(RETURN).up(LAST_MOVE),
                this::pressDepositWithdraw,
                textButtonSetup("") // Deposit/Withdraw text set depending on state
        ).setup(ButtonPanel::greyInactive);

        releaseButton = new Button(
                fakeTabs[2].panel(),
                new ButtonTransitions().right(PARTY).left(DEPOSIT_WITHDRAW).down(RETURN).up(LAST_MOVE),
                () -> {
                    pc.releasePokemon(selected);
                    movedToFront();
                },
                textButtonSetup("Release")
        ).setup(ButtonPanel::greyInactive);

        int spacing = infoPanel.x - boxPanel.rightX();
        int returnY = infoPanel.bottomY() + spacing;
        Button returnButton = new Button(
                infoPanel.x,
                returnY,
                infoPanel.width,
                partyPanel.bottomY() - returnY,
                new ButtonTransitions().right(PARTY).up(DEPOSIT_WITHDRAW).down(MOVES).left(LAST_PARTY),
                ButtonPressAction.getExitAction(),
                textButtonSetup("Return")
        ).setup(panel -> panel.withBackgroundColor(Color.YELLOW)
                              .withTransparentCount(2));

        this.buttons = new ButtonList(NUM_BUTTONS);
        this.buttons.set(BOX, boxButtons);
        this.buttons.set(PARTY, partyButtons);
        this.buttons.set(MOVES, moveButtons);
        this.buttons.set(LEFT_ARROW, leftButton);
        this.buttons.set(RIGHT_ARROW, rightButton);
        this.buttons.set(SWITCH, switchButton);
        this.buttons.set(DEPOSIT_WITHDRAW, depositWithdrawButton);
        this.buttons.set(RELEASE, releaseButton);
        this.buttons.set(RETURN, returnButton);

        this.buttons.setSelected(PARTY);

        this.panels = new PanelList(
                boxPanel, boxNamePanel, partyPanel, infoPanel,
                basicInfoPanel, imagePanel, movesPanel, statsPanel
        );
    }

    private ButtonPanelSetup textButtonSetup(String text) {
        return panel -> panel.withBlackOutline()
                             .withLabel(text, 20)
                             .withFullTransparency();
    }

    @Override
    public void update(int dt) {
        buttons.update();
        if (buttons.consumeSelectedPress()) {
            updateActiveButtons();
        }

        InputControl.instance().popViewIfEscaped();
    }

    private void pressBoxPokemon(int row, int col) {
        if (party && depositClicked) {
            pc.depositPokemonFromPlayer(selected, row, col);
            depositClicked = false;
        } else if (switchClicked) {
            pc.switchPokemon(selected, row, col);
            switchClicked = false;
        } else {
            this.setSelected(pc.getBoxPokemon()[row][col]);
        }
    }

    private void pressPartyPokemon(int index) {
        if (party && depositClicked) {
            depositClicked = false;
        } else if (switchClicked) {
            pc.switchPokemon(selected, index);
            switchClicked = false;
        } else {
            this.setSelected(index);
        }
    }

    private void setSelected(int partyIndex) {
        this.setSelected(Game.getPlayer().getTeam().get(partyIndex));
    }

    private void setSelected(PartyPokemon selected) {
        this.selected = selected;

        // Pokemon panel type colors
        Color[] typeColors = PokeType.getColors(selected);
        infoPanel.withBackgroundColors(typeColors);

        // Secondary color is the color of the buttons
        highlightColor = typeColors[1];

        // Pokemon panel image
        BufferedImage pokemonImage = spriteTiles.getTile(selected.getImageName());
        imagePanel.withImageLabel(pokemonImage);

        // Handles setting party
        this.updateActiveButtons();
    }

    private void pressDepositWithdraw() {
        if (party) { // Deposit
            if (depositClicked) {
                pc.depositPokemonFromPlayer(selected);
            }

            depositClicked = !depositClicked;
        } else { // Withdraw
            pc.withdrawPokemon(selected);
        }
    }

    // Sets up activeness of button and background colors and images and labels
    private void setupPokemonButton(Button button, PartyPokemon pokemon) {
        boolean active = pokemon != null;
        button.setActiveSkip(active);

        // Draw the pokemon image and outline if selected
        if (active) {
            button.panel()
                  .withConditionalOutline(pokemon == selected)
                  .withImageLabel(partyTiles.getTile(pokemon.getTinyImageName()));
        }
    }

    @Override
    public void draw(Graphics g) {
        // Highlight if selected
        switchButton.panel().withHighlight(switchClicked, highlightColor);
        depositWithdrawButton.panel().withHighlight(party && depositClicked, highlightColor);

        // Background
        BasicPanels.drawCanvasPanel(g);
        this.panels.drawAll(g);

        // Draw page numbers
        TextUtils.drawPageNumbers(g, 16, leftButton, rightButton, pc.getBoxNum(), pc.getNumBoxes());

        // Info panel
        drawSelectedPokemon(g);

        // Draw buttons
        buttons.draw(g);
    }

    private void drawSelectedPokemon(Graphics g) {
        FontMetrics.setBlackFont(g, 20);
        g.drawString(selected.getNameAndGender(), 541, 82);

        // Eggs don't know shit
        if (selected.isEgg()) {
            FontMetrics.setFont(g, 16);
            TextUtils.drawWrappedText(g, ((Eggy)selected).getEggMessage(), 427, 179, 740 - 427);
        } else {
            // Level and number
            TextUtils.drawRightAlignedString(g, "Lv" + selected.getLevel(), 740, 82);
            g.drawString("#" + String.format("%03d", selected.getPokemonInfo().getNumber()), 540, 110);

            // Type
            ImageUtils.drawTypeTiles(g, selected.getActualType(), 740, 110);

            FontMetrics.setFont(g, 16);

            // Total EXP
            g.drawString("EXP:", 540, 135);
            TextUtils.drawRightAlignedString(g, selected.getTotalEXP() + "", 740, 135);

            // EXP to the next level
            g.drawString("To Next Lv:", 540, 156);
            TextUtils.drawRightAlignedString(g, selected.expToNextLevel() + "", 740, 156);

            // Ability
            g.drawString(selected.getActualAbility().getName(), 427, 179);

            // Held Item
            TextUtils.drawRightAlignedString(g, selected.getActualHeldItem().getName(), 740, 179);

            // Nature
            g.drawString(selected.getNature().getName() + " Nature", 427, 198);

            // Characteristic
            g.drawString(selected.getCharacteristic(), 427, 217);

            // Stats Box or Move description
            int selectedButton = buttons.getSelected();
            if (selectedButton >= MOVES && selectedButton < MOVES + MoveList.MAX_MOVES) {
                MoveList moves = selected.getActualMoves();
                drawMoveDetails(g, moves.get(selectedButton - MOVES).getAttack());
            } else {
                statsPanel.drawStats(g, selected);
            }
        }
    }

    public WrapMetrics drawMoveDetails(Graphics g, Attack move) {
        return moveDetailsPanel.drawMove(g, move);
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.PC_VIEW;
    }

    @Override
    public void movedToFront() {
        Player player = Game.getPlayer();
        GameData data = Game.getData();

        pc = player.getPC();
        partyTiles = data.getPartyTiles();
        spriteTiles = data.getPokemonTilesSmall();

        this.setSelected(0);
        updateActiveButtons();
    }

    private void updateActiveButtons() {
        Player player = Game.getPlayer();
        List<PartyPokemon> team = player.getTeam();

        // Party Pokemon buttons -- check if selected is part of the team
        party = false;
        for (int i = 0; i < partyButtons.length; i++) {
            PartyPokemon partyPokemon = i < team.size() ? team.get(i) : null;
            setupPokemonButton(partyButtons[i], partyPokemon);
            if (selected != null && selected == partyPokemon) {
                party = true;
            }
        }

        // Box Pokemon buttons
        PartyPokemon[][] box = pc.getBoxPokemon();
        for (int i = 0; i < PC.BOX_HEIGHT; i++) {
            for (int j = 0; j < PC.BOX_WIDTH; j++) {
                Button button = boxButtons[i][j];
                setupPokemonButton(button, box[i][j]);

                // Even if slot is empty, can still be active (like if choosing where to place the selected)
                if ((party && depositClicked) || switchClicked) {
                    button.setActive(true);
                }
            }
        }

        // Selected Pokemon moves
        MoveList moves = selected.getActualMoves();
        for (int i = 0; i < moveButtons.length; i++) {
            Button button = moveButtons[i];
            button.setActiveSkip(!selected.isEgg() && i < moves.size());
            if (button.isActive()) {
                Attack attack = moves.get(i).getAttack();
                button.panel()
                      .withBackgroundColor(attack.getActualType().getColor())
                     .withLabel(attack.getName());
            }
        }

        // Deposit/Withdraw
        if (party) {
            depositWithdrawButton.setActive(player.canDeposit(selected));
            depositWithdrawButton.panel().withLabel("Deposit");
        } else {
            depositWithdrawButton.setActive(team.size() < Trainer.MAX_POKEMON);
            depositWithdrawButton.panel().withLabel("Withdraw");
        }

        // Release
        releaseButton.setActive(!party || player.canDeposit(selected));

        // Box color and name
        boxPanel.withBackgroundColor(pc.getBoxColor());
        boxNamePanel.withLabel("Box " + (pc.getBoxNum() + 1));
    }
}
