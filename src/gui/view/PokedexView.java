package gui.view;

import battle.attack.Attack;
import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.button.ButtonList;
import draw.button.ButtonPanel;
import draw.button.ButtonPressAction;
import draw.button.ButtonTransitions;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.MovePanel;
import draw.panel.PanelList;
import draw.panel.WrapPanel;
import draw.panel.WrapPanel.WrapMetrics;
import gui.TileSet;
import input.InputControl;
import main.Game;
import map.Direction;
import pokemon.active.Gender;
import pokemon.evolution.Evolution;
import pokemon.evolution.MultipleEvolution;
import pokemon.evolution.NoEvolution;
import pokemon.species.LevelUpMove;
import pokemon.species.PokemonInfo;
import pokemon.species.PokemonList;
import pokemon.stat.Stat;
import trainer.player.pokedex.Pokedex;
import util.FontMetrics;
import util.GeneralUtils;
import util.string.PokeString;
import util.string.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;

public class PokedexView extends View {
    private static final int NUM_COLS = 6;
    private static final int NUM_ROWS = 6;

    private static final int PER_PAGE = NUM_ROWS*NUM_COLS;
    private static final int NUM_PAGES = (int)Math.ceil((double)PokemonInfo.NUM_POKEMON/PER_PAGE);
    private static final int NUM_TAB_BUTTONS = TabInfo.values().length;
    private static final int MOVES_PER_PAGE = 4;

    private static final int NUM_BUTTONS = PER_PAGE + NUM_TAB_BUTTONS + MOVES_PER_PAGE + 5;

    private static final int RETURN = NUM_BUTTONS - 1;
    private static final int RIGHT_ARROW = NUM_BUTTONS - 2;
    private static final int LEFT_ARROW = NUM_BUTTONS - 3;
    private static final int MOVES_RIGHT_ARROW = NUM_BUTTONS - 4;
    private static final int MOVES_LEFT_ARROW = NUM_BUTTONS - 5;

    private static final int TAB_START = PER_PAGE;
    private static final int MOVE_START = TAB_START + NUM_TAB_BUTTONS;

    private PanelList panels;
    private List<DrawPanel> alwaysPanels;

    private final DrawPanel countPanel;
    private final DrawPanel infoPanel;
    private final DrawPanel imagePanel;
    private final DrawPanel basicInfoPanel;
    private final WrapPanel flavorTextPanel;
    private final MovePanel moveDescriptionPanel;

    private final ButtonList buttons;
    private final Button[][] pokemonButtons;
    private final Button leftButton;
    private final Button rightButton;
    private final Button[] tabButtons;
    private final Button[] moveButtons;
    private final Button movesLeftButton;
    private final Button movesRightButton;

    private final Pokedex pokedex;

    private PokemonInfo selected;
    private TabInfo selectedTab;

    private int pageNum;
    private int movePageNum;

    PokedexView() {
        DrawPanel pokedexPanel = new DrawPanel(40, 40, 350, 418)
                .withBackgroundColor(Color.BLUE)
                .withTransparentCount(2)
                .withBorderPercentage(0)
                .withBlackOutline();

        DrawPanel titlePanel = new DrawPanel(pokedexPanel.x, pokedexPanel.y, pokedexPanel.width, 37)
                .withNoBackground()
                .withBlackOutline()
                .withLabel(PokeString.POKEDEX, 20);

        countPanel = new DrawPanel(pokedexPanel.x, 478, pokedexPanel.width, 82)
                .withBackgroundColor(Color.RED)
                .withTransparentCount(2)
                .withBorderPercentage(0)
                .withBlackOutline()
                .withLabelSize(20);

        infoPanel = new DrawPanel(410, pokedexPanel.y, pokedexPanel.width, 462)
                .withTransparentCount(2)
                .withBorderPercentage(0)
                .withBlackOutline();

        imagePanel = new DrawPanel(
                infoPanel.x + 18,
                infoPanel.y + 18,
                104,
                104
        )
                .withFullTransparency()
                .withBlackOutline()
                .withLabelSize(80);

        basicInfoPanel = new DrawPanel(infoPanel.x, infoPanel.y, infoPanel.width, 230)
                .withNoBackground()
                .withBlackOutline();

        flavorTextPanel = new WrapPanel(
                infoPanel.x,
                imagePanel.bottomY(),
                infoPanel.width,
                basicInfoPanel.bottomY() - imagePanel.bottomY(),
                16
        )
                .withBorderPercentage(0);

        int spacing = 20;
        int moveButtonHeight = 38;
        moveDescriptionPanel = new MovePanel(
                infoPanel.x + spacing,
                infoPanel.y + spacing,
                infoPanel.width - 2*spacing,
                moveButtonHeight*3,
                17, 15, 12
        )
                .withMinDescFontSize(10);

        this.pokedex = Game.getPlayer().getPokedex();
        pageNum = 0;

        Button[] buttons = new Button[NUM_BUTTONS];
        pokemonButtons = new Button[NUM_ROWS][NUM_COLS];
        for (int i = 0, k = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLS; j++, k++) {
                final int row = i;
                final int col = j;

                buttons[k] = pokemonButtons[i][j] = new Button(
                        60 + 54*j,
                        96 + 54*i,
                        40,
                        40,
                        ButtonHoverAction.BOX,
                        ButtonTransitions.getBasicTransitions(
                                k, NUM_ROWS, NUM_COLS, 0,
                                new ButtonTransitions().right(RETURN).up(RIGHT_ARROW).down(RIGHT_ARROW)
                        ),
                        () -> selected = PokemonList.get(getPokeNum(row, col)),
                        panel -> panel.withLabelSize(20)
                                      .withLabelColor(new Color(0, 0, 0, 64))
                );
            }
        }

        int buttonHeight = 38;
        tabButtons = new Button[NUM_TAB_BUTTONS];
        for (int i = 0; i < tabButtons.length; i++) {
            TabInfo tabInfo = TabInfo.values()[i];
            buttons[TAB_START + i] = tabButtons[i] = new Button(
                    infoPanel.createBottomInsetTab(i, buttonHeight, NUM_TAB_BUTTONS),
                    ButtonTransitions.getBasicTransitions(
                            i, 1, tabButtons.length, TAB_START,
                            new ButtonTransitions().right(LEFT_ARROW).up(MOVES_RIGHT_ARROW).left(RIGHT_ARROW).down(RETURN)
                    ),
                    () -> changeTab(tabInfo),
                    panel -> panel.withLabel(tabInfo.label, 12)
            );
        }

        moveButtons = new Button[MOVES_PER_PAGE];
        for (int i = 0; i < MOVES_PER_PAGE; i++) {
            buttons[MOVE_START + i] = moveButtons[i] = new Button(
                    moveDescriptionPanel.x,
                    moveDescriptionPanel.bottomY() + spacing + i*(spacing + moveButtonHeight),
                    moveDescriptionPanel.width,
                    moveButtonHeight,
                    ButtonTransitions.getBasicTransitions(
                            i, MOVES_PER_PAGE, 1, MOVE_START,
                            new ButtonTransitions()
                                    .right(MOVES_RIGHT_ARROW)
                                    .up(RETURN)
                                    .left(MOVES_LEFT_ARROW)
                                    .down(MOVES_RIGHT_ARROW)
                    ),
                    () -> {}, // Pressing does nothing, only care if button is selected
                    panel -> panel.skipInactive()
                                  .withBlackOutline()
            );
        }

        int arrowWidth = 35;
        int arrowHeight = 20;

        buttons[LEFT_ARROW] = leftButton = new Button(
                140, 418, arrowWidth, arrowHeight,
                ButtonHoverAction.BOX,
                new ButtonTransitions()
                        .right(RIGHT_ARROW)
                        .up(NUM_COLS*(NUM_ROWS - 1) + NUM_COLS/2 - 1)
                        .left(TAB_START + NUM_TAB_BUTTONS - 1)
                        .down(0),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, -1, NUM_PAGES)
        ).asArrow(Direction.LEFT);

        buttons[RIGHT_ARROW] = rightButton = new Button(
                255, 418, arrowWidth, arrowHeight,
                ButtonHoverAction.BOX,
                new ButtonTransitions()
                        .right(TAB_START)
                        .up(NUM_COLS*(NUM_ROWS - 1) + NUM_COLS/2)
                        .left(LEFT_ARROW)
                        .down(0),
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, 1, NUM_PAGES)
        ).asArrow(Direction.RIGHT);

        buttons[MOVES_LEFT_ARROW] = movesLeftButton = new Button(
                infoPanel.centerX() - arrowWidth*3,
                (moveButtons[MOVES_PER_PAGE - 1].bottomY() + tabButtons[0].y)/2 - arrowHeight/2,
                arrowWidth,
                arrowHeight,
                new ButtonTransitions()
                        .right(MOVES_RIGHT_ARROW)
                        .up(MOVE_START + MOVES_PER_PAGE - 1)
                        .left(RIGHT_ARROW)
                        .down(RETURN),
                () -> movePageNum = GeneralUtils.wrapIncrement(movePageNum, -1, maxMovePages()),
                panel -> panel.skipInactive()
                              .asArrow(Direction.LEFT)
        );

        buttons[MOVES_RIGHT_ARROW] = movesRightButton = new Button(
                infoPanel.centerX() + arrowWidth*2,
                buttons[MOVES_LEFT_ARROW].y,
                arrowWidth,
                arrowHeight,
                new ButtonTransitions()
                        .right(LEFT_ARROW)
                        .up(MOVE_START + MOVES_PER_PAGE - 1)
                        .left(MOVES_LEFT_ARROW)
                        .down(RETURN),
                () -> movePageNum = GeneralUtils.wrapIncrement(movePageNum, 1, maxMovePages()),
                panel -> panel.skipInactive()
                              .asArrow(Direction.RIGHT)
        );

        buttons[RETURN] = new Button(
                410, 522, 350, 38, ButtonHoverAction.BOX,
                new ButtonTransitions().right(0).up(PER_PAGE).left(RIGHT_ARROW).down(PER_PAGE),
                ButtonPressAction.getExitAction(),
                panel -> panel.withBackgroundColor(Color.YELLOW)
                              .withBorderlessTransparentBackground()
                              .withTransparentCount(2)
                              .withBlackOutline()
                              .withLabel("Return", 20)
        );

        this.buttons = new ButtonList(buttons);

        this.alwaysPanels = List.of(pokedexPanel, titlePanel, countPanel, infoPanel);
        this.panels = new PanelList(alwaysPanels);

        selected = PokemonList.get(1);
        changeTab(TabInfo.MAIN);
    }

    @Override
    public void update(int dt) {
        buttons.update();
        if (buttons.consumeSelectedPress()) {
            updateActiveButtons();
        }

        InputControl.instance().popViewIfEscaped();
    }

    private int maxMovePages() {
        return (int)Math.ceil(1.0*selected.getLevelUpMoves().size()/MOVES_PER_PAGE);
    }

    private void drawSetup() {
        this.panels = new PanelList(alwaysPanels);

        // Pokemon buttons (image, pokeball, number, etc.)
        setupPokemonButtons();

        // Selected Pokemon
        setupSelectedPokemon();

        // Tab outlines
        for (int i = 0; i < tabButtons.length; i++) {
            tabButtons[i].panel().withBottomTabOutlines(i, selectedTab.ordinal());
        }
    }

    private void setupSelectedPokemon() {
        boolean notSeen = pokedex.isNotSeen(selected);

        // Background type colors (if not seen just use black)
        infoPanel.withBackgroundColors(notSeen
                                       ? new Color[] { Color.BLACK, Color.BLACK }
                                       : selected.getType().getColors());

        if (this.shouldDrawInformationPanel()) {
            this.panels.add(basicInfoPanel, imagePanel);

            // Image
            if (notSeen) {
                imagePanel.withLabel("?");
            } else {
                TileSet pokedexTiles = Game.getData().getPokedexTilesSmall();
                BufferedImage pkmImg = pokedexTiles.getTile(selected.getBaseImageName());
                pkmImg.setRGB(0, 0, 0); // TODO: What is happening here?

                imagePanel.withImageLabel(pkmImg);
            }
        }
    }

    private void setupPokemonButtons() {
        TileSet partyTiles = Game.getData().getPartyTiles();

        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLS; j++) {
                ButtonPanel buttonPanel = pokemonButtons[i][j].panel();

                // Only skip if the Pokemon doesn't even exist (number too high)
                int number = getPokeNum(i, j);
                if (number > PokemonInfo.NUM_POKEMON) {
                    buttonPanel.skipDraw();
                    continue;
                }

                // Outline selected Pokemon in black
                PokemonInfo pokemonInfo = PokemonList.get(number);
                buttonPanel.withConditionalOutline(pokemonInfo == selected);
                buttonPanel.withBottomRightImage(null);

                if (pokedex.isNotSeen(pokemonInfo)) {
                    // Just display number for unseen pokes
                    buttonPanel.withLabel(String.format("%03d", number));
                } else {
                    // If seen or caught, show party tile image
                    buttonPanel.withImageLabel(partyTiles.getTile(pokemonInfo.getTinyImageName()));

                    // Caught pokemon have a little Pokeball in the bottom right corner
                    if (pokedex.isCaught(pokemonInfo)) {
                        buttonPanel.withBottomRightImage(TileSet.TINY_POKEBALL);
                    }
                }
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        drawSetup();

        // Background
        BasicPanels.drawCanvasPanel(g);
        panels.drawAll(g);
        buttons.drawPanels(g);

        // Selected Pokemon information
        drawSelectedPokemon(g);

        // Draw page numbers and arrows
        TextUtils.drawPageNumbers(g, 16, leftButton, rightButton, pageNum, NUM_PAGES);

        buttons.drawHover(g);
    }

    private void drawSelectedPokemon(Graphics g) {
        boolean notSeen = pokedex.isNotSeen(selected);
        boolean caught = pokedex.isCaught(selected);

        int spacing = 15;
        int leftX, textY;
        if (this.shouldDrawInformationPanel()) {
            // Name
            FontMetrics.setBlackFont(g, 20);
            leftX = imagePanel.rightX() + spacing;
            textY = imagePanel.y + FontMetrics.getTextHeight(g) + spacing;
            g.drawString(notSeen ? "?????" : selected.getName(), leftX, textY);

            // Number
            int numberSpacing = 10;
            FontMetrics.setFont(g, 18);
            TextUtils.drawRightAlignedString(
                    g,
                    "#" + String.format("%03d", selected.getNumber()),
                    infoPanel.rightX() - numberSpacing,
                    infoPanel.y + FontMetrics.getTextHeight(g) + numberSpacing
            );

            if (!notSeen) {
                // Type tiles
                ImageUtils.drawTypeTiles(g, selected.getType(), infoPanel.rightX() - spacing, textY);

                textY += FontMetrics.getDistanceBetweenRows(g);

                // Classification
                FontMetrics.setFont(g, 16);
                g.drawString(
                        (!caught ? "???" : selected.getClassification()) + " " + PokeString.POKEMON,
                        leftX,
                        textY
                );

                textY += FontMetrics.getDistanceBetweenRows(g);
                g.drawString("Height: " + (!caught ? "???'??\"" : selected.getHeightString()), leftX, textY);

                textY += FontMetrics.getDistanceBetweenRows(g);
                g.drawString("Weight: " + (!caught ? "???.?" : selected.getWeight()) + " lbs", leftX, textY);

                if (caught) {
                    drawFlavorText(g, selected);
                }
            }
        }

        FontMetrics.setFont(g, 16);

        leftX = imagePanel.x;
        textY = basicInfoPanel.bottomY() + FontMetrics.getTextHeight(g) + spacing;

        if (selectedTab == TabInfo.MAIN && caught) {
            textY = TextUtils.drawWrappedText(
                    g,
                    "Abilities: " + selected.getAbilitiesString(),
                    leftX,
                    textY,
                    infoPanel.width - 2*(leftX - infoPanel.x)
            );

            textY += FontMetrics.getTextHeight(g) + spacing;
            g.drawString("Gender Ratio: " + Gender.getGenderString(selected), leftX, textY);

            textY += FontMetrics.getTextHeight(g) + spacing;
            g.drawString("Capture Rate: " + selected.getCatchRate(), leftX, textY);

            textY += FontMetrics.getTextHeight(g) + spacing;
            g.drawString("Base EXP Yield: " + selected.getBaseEXP(), leftX, textY);

            if (selected.canBreed()) {
                textY += FontMetrics.getTextHeight(g) + spacing;
                g.drawString("Egg Steps: " + selected.getEggSteps(), leftX, textY);
            }
        }

        if (selectedTab == TabInfo.STATS && caught) {
            String label = "Given EVs";
            int evRightX = infoPanel.rightX() - spacing - FontMetrics.getTextWidth(g, label)/2;
            g.drawString(label, evRightX - FontMetrics.getTextWidth(g, label)/2, textY);
            evRightX += FontMetrics.getTextWidth(g, " ");

            int baseStatRightX = evRightX - FontMetrics.getTextWidth(g, label)/2 - 2*spacing;
            label = "Base Stat";
            baseStatRightX -= FontMetrics.getTextWidth(g, label)/2;
            g.drawString(label, baseStatRightX - FontMetrics.getTextWidth(g, label)/2, textY);
            baseStatRightX += FontMetrics.getTextWidth(g, " ");

            for (int i = 0; i < Stat.NUM_STATS; i++) {
                int y = textY + (i + 1)*(FontMetrics.getTextHeight(g) + spacing);
                g.drawString(Stat.getStat(i, false).getName(), leftX, y);
                TextUtils.drawRightAlignedString(g, selected.getStats().get(i) + "", baseStatRightX, y);
                TextUtils.drawRightAlignedString(g, selected.getGivenEV(i) + "", evRightX, y);
            }
        }

        if (selectedTab == TabInfo.LOCATION) {
            g.drawString("Locations:", leftX, textY);
            List<String> locations = pokedex.getLocations(selected.namesies());
            for (int i = 0; i < locations.size(); i++) {
                g.drawString(locations.get(i), leftX + 2*spacing, textY + (i + 1)*FontMetrics.getDistanceBetweenRows(g));
            }
        }

        if (selectedTab == TabInfo.MOVES) {
            if (!caught) {
                g.drawString("Moves:", leftX, textY);
                g.drawString("???", leftX + 2*spacing, textY + FontMetrics.getDistanceBetweenRows(g));
            } else {
                FontMetrics.setFont(g, 14);

                List<LevelUpMove> levelUpMoves = selected.getLevelUpMoves();
                Iterator<LevelUpMove> movesIterator = GeneralUtils.pageIterator(levelUpMoves, movePageNum, MOVES_PER_PAGE);

                for (int i = 0; i < MOVES_PER_PAGE && movesIterator.hasNext(); i++) {
                    LevelUpMove levelUpMove = movesIterator.next();

                    int level = levelUpMove.getLevel();
                    Attack attack = levelUpMove.getMove().getNewAttack();

                    final String levelString;
                    if (level == PokemonInfo.EVOLUTION_LEVEL_LEARNED) {
                        levelString = " Ev";
                    } else if (level <= 1) {
                        levelString = " --";
                    } else {
                        levelString = String.format("%3d", level);
                    }

                    int selectedButton = buttons.getSelected();
                    if (i == 0 || selectedButton == MOVE_START + i) {
                        drawMoveDetails(g, attack);
                    }

                    Button moveButton = moveButtons[i];
                    new DrawPanel(moveButton).drawLeftLabel(g, 18, levelString + " " + attack.getName());

                    int moveImageSpacing = 20;
                    BufferedImage typeImage = attack.getActualType().getImage();
                    int imageY = moveButton.centerY() - typeImage.getHeight()/2;
                    int imageX = moveButton.rightX() - moveImageSpacing - typeImage.getWidth();
                    g.drawImage(typeImage, imageX, imageY, null);

                    BufferedImage categoryImage = attack.getCategory().getImage();
                    imageX -= categoryImage.getWidth() + moveImageSpacing;
                    g.drawImage(categoryImage, imageX, imageY, null);
                }

                TextUtils.drawPageNumbers(g, 18, movesLeftButton, movesRightButton, movePageNum, maxMovePages());
                movesLeftButton.drawArrow(g, Direction.LEFT);
                movesRightButton.drawArrow(g, Direction.RIGHT);
            }
        }

        if (selectedTab == TabInfo.EVOLUTION) {
            g.drawString("Evolutions:", leftX, textY);

            leftX += 2*spacing;
            textY += FontMetrics.getDistanceBetweenRows(g);

            if (!caught) {
                g.drawString("???", leftX, textY);
            } else {
                Evolution evolution = selected.getEvolution();
                if (evolution instanceof MultipleEvolution) {
                    Evolution[] allEvolutions = ((MultipleEvolution)evolution).getFullEvolutions();
                    for (Evolution eachEvolution : allEvolutions) {
                        textY = drawEvolutionText(g, eachEvolution, leftX, textY, spacing);
                    }
                } else if (evolution instanceof NoEvolution) {
                    g.drawString(selected.getName() + " does not evolve", leftX, textY);
                } else {
                    drawEvolutionText(g, evolution, leftX, textY, spacing);
                }
            }
        }
    }

    private int drawEvolutionText(Graphics g, Evolution evolution, int leftX, int textY, int spacing) {
        return TextUtils.drawWrappedText(
                g,
                evolution.getEvolutions()[0].getName() + ": " + evolution.getString(),
                leftX,
                textY,
                infoPanel.width - 3*spacing
        );
    }

    public WrapMetrics drawFlavorText(Graphics g, PokemonInfo pokemonInfo) {
        return flavorTextPanel.drawMessage(g, pokemonInfo.getFlavorText());
    }

    public WrapMetrics drawMoveDetails(Graphics g, Attack attack) {
        return moveDescriptionPanel.draw(g, attack);
    }

    private int getPokeNum(int i, int j) {
        return PER_PAGE*pageNum + i*NUM_COLS + j + 1;
    }

    private void changeTab(TabInfo tab) {
        selectedTab = tab;
        movePageNum = 0;

        this.updateActiveButtons();
    }

    private void updateActiveButtons() {
        int pokemonDisplayed = PokemonInfo.NUM_POKEMON - pageNum*PER_PAGE;
        for (int i = 0, k = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLS; j++, k++) {
                pokemonButtons[i][j].setActive(k < pokemonDisplayed);
            }
        }

        boolean movesView = selectedTab == TabInfo.MOVES && pokedex.isCaught(selected);
        int movesDisplayed = selected.getLevelUpMoves().size() - movePageNum*MOVES_PER_PAGE;
        for (int i = 0; i < MOVES_PER_PAGE; i++) {
            moveButtons[i].setActive(movesView && i < movesDisplayed);
        }

        movesLeftButton.setActive(movesView);
        movesRightButton.setActive(movesView);
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.POKEDEX_VIEW;
    }

    @Override
    public void movedToFront() {
        countPanel.withLabel("Seen: " + pokedex.numSeen() + "     Caught: " + pokedex.numCaught());

        selected = PokemonList.get(1);
        changeTab(TabInfo.MAIN);
    }

    // Not the moves tab or a completely unknown Pokemon which doesn't give a shit about moves
    private boolean shouldDrawInformationPanel() {
        return selectedTab != TabInfo.MOVES || !pokedex.isCaught(selected);
    }

    private enum TabInfo {
        MAIN,
        STATS,
        LOCATION,
        EVOLUTION,
        MOVES;

        private final String label;

        TabInfo() {
            this.label = StringUtils.properCase(this.name().toLowerCase());
        }
    }
}
