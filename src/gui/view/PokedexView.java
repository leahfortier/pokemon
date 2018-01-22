package gui.view;

import battle.attack.Attack;
import draw.DrawUtils;
import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import gui.GameData;
import gui.TileSet;
import input.InputControl;
import main.Game;
import map.Direction;
import pokemon.Gender;
import pokemon.LevelUpMove;
import pokemon.PokemonInfo;
import pokemon.Stat;
import pokemon.evolution.Evolution;
import pokemon.evolution.MultipleEvolution;
import pokemon.evolution.NoEvolution;
import trainer.player.pokedex.Pokedex;
import type.Type;
import util.FontMetrics;
import util.GeneralUtils;
import util.PokeString;
import util.StringUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class PokedexView extends View {
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

    private final DrawPanel pokedexPanel;
    private final DrawPanel titlePanel;
    private final DrawPanel countPanel;
    private final DrawPanel infoPanel;
    private final DrawPanel imagePanel;
    private final DrawPanel basicInfoPanel;
    private final DrawPanel moveDescriptionPanel;

    private final Button[] buttons;
    private final Button[][] pokemonButtons;
    private final Button leftButton;
    private final Button rightButton;
    private final Button[] tabButtons;
    private final Button[] moveButtons;
    private final Button movesLeftButton;
    private final Button movesRightButton;
    private final Button returnButton;

    private final Pokedex pokedex;

    private int selectedButton;
    private PokemonInfo selected;
    private TabInfo selectedTab;

    private int pageNum;
    private int movePageNum;

    private int numSeen;
    private int numCaught;

    PokedexView() {
        pokedexPanel = new DrawPanel(40, 40, 350, 418)
                .withBackgroundColor(Color.BLUE)
                .withTransparentCount(2)
                .withBorderPercentage(0)
                .withBlackOutline();

        titlePanel = new DrawPanel(pokedexPanel.x, pokedexPanel.y, pokedexPanel.width, 37)
                .withBackgroundColor(null)
                .withBlackOutline();

        countPanel = new DrawPanel(pokedexPanel.x, 478, pokedexPanel.width, 82)
                .withBackgroundColor(Color.RED)
                .withTransparentCount(2)
                .withBorderPercentage(0)
                .withBlackOutline();

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
                .withBlackOutline();

        basicInfoPanel = new DrawPanel(infoPanel.x, infoPanel.y, infoPanel.width, 230)
                .withBackgroundColor(null)
                .withBorderPercentage(0)
                .withBlackOutline();

        int spacing = 20;
        int moveButtonHeight = 38;
        moveDescriptionPanel = new DrawPanel(
                infoPanel.x + spacing,
                infoPanel.y + spacing,
                infoPanel.width - 2*spacing,
                moveButtonHeight*3
        )
                .withBlackOutline();

        this.pokedex = Game.getPlayer().getPokedex();
        selectedButton = 0;
        pageNum = 0;

        buttons = new Button[NUM_BUTTONS];
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
                        Button.getBasicTransitions(
                                k, NUM_ROWS, NUM_COLS, 0,
                                new int[] { RETURN, RIGHT_ARROW, -1, RIGHT_ARROW }
                        ),
                        () -> selected = PokemonInfo.getPokemonInfo(getIndex(row, col) + 1)
                );
            }
        }

        int buttonHeight = 38;
        tabButtons = new Button[NUM_TAB_BUTTONS];
        for (int i = 0; i < tabButtons.length; i++) {
            final int index = i;
            buttons[TAB_START + i] = tabButtons[i] = Button.createTabButton(
                    i,
                    infoPanel.x,
                    infoPanel.bottomY() - DrawUtils.OUTLINE_SIZE,
                    infoPanel.width,
                    buttonHeight,
                    NUM_TAB_BUTTONS,
                    Button.getBasicTransitions(
                            i, 1, tabButtons.length, TAB_START,
                            new int[] { LEFT_ARROW, MOVES_RIGHT_ARROW, RIGHT_ARROW, RETURN }
                    ),
                    () -> changeTab(TabInfo.values()[index])
            );
        }

        moveButtons = new Button[MOVES_PER_PAGE];
        for (int i = 0; i < MOVES_PER_PAGE; i++) {
            buttons[MOVE_START + i] = moveButtons[i] = new Button(
                    moveDescriptionPanel.x,
                    moveDescriptionPanel.bottomY() + spacing + i*(spacing + moveButtonHeight),
                    moveDescriptionPanel.width,
                    moveButtonHeight,
                    ButtonHoverAction.BOX,
                    Button.getBasicTransitions(
                            i, MOVES_PER_PAGE, 1, MOVE_START,
                            new int[] { MOVES_RIGHT_ARROW, RETURN, MOVES_LEFT_ARROW, MOVES_RIGHT_ARROW }
                    )
            );
        }

        int arrowWidth = 35;
        int arrowHeight = 20;

        buttons[LEFT_ARROW] = leftButton = new Button(
                140, 418, arrowWidth, arrowHeight,
                ButtonHoverAction.BOX,
                new int[] { RIGHT_ARROW, NUM_COLS*(NUM_ROWS - 1) + NUM_COLS/2 - 1, TAB_START + NUM_TAB_BUTTONS - 1, 0 },
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, -1, NUM_PAGES)
        );

        buttons[RIGHT_ARROW] = rightButton = new Button(
                255, 418, arrowWidth, arrowHeight,
                ButtonHoverAction.BOX,
                new int[] { TAB_START, NUM_COLS*(NUM_ROWS - 1) + NUM_COLS/2, LEFT_ARROW, 0 },
                () -> pageNum = GeneralUtils.wrapIncrement(pageNum, 1, NUM_PAGES)
        );

        buttons[MOVES_LEFT_ARROW] = movesLeftButton = new Button(
                infoPanel.centerX() - arrowWidth*3,
                (moveButtons[MOVES_PER_PAGE - 1].bottomY() + tabButtons[0].y)/2 - arrowHeight/2,
                arrowWidth,
                arrowHeight,
                ButtonHoverAction.BOX,
                new int[] { MOVES_RIGHT_ARROW, MOVE_START + MOVES_PER_PAGE - 1, RIGHT_ARROW, RETURN },
                () -> movePageNum = GeneralUtils.wrapIncrement(movePageNum, -1, maxMovePages())
        );

        buttons[MOVES_RIGHT_ARROW] = movesRightButton = new Button(
                infoPanel.centerX() + arrowWidth*2,
                buttons[MOVES_LEFT_ARROW].y,
                arrowWidth,
                arrowHeight,
                ButtonHoverAction.BOX,
                new int[] { LEFT_ARROW, MOVE_START + MOVES_PER_PAGE - 1, MOVES_LEFT_ARROW, RETURN },
                () -> movePageNum = GeneralUtils.wrapIncrement(movePageNum, 1, maxMovePages())
        );

        buttons[RETURN] = returnButton = Button.createExitButton(
                410, 522, 350, 38, ButtonHoverAction.BOX,
                new int[] { 0, PER_PAGE, RIGHT_ARROW, PER_PAGE }
        );

        selected = PokemonInfo.getPokemonInfo(1);
        changeTab(TabInfo.MAIN);
    }

    @Override
    public void update(int dt) {
        selectedButton = Button.update(buttons, selectedButton);
        if (buttons[selectedButton].checkConsumePress()) {
            updateActiveButtons();
        }

        InputControl.instance().popViewIfEscaped();
    }

    private int maxMovePages() {
        return (int)Math.ceil(1.0*selected.getLevelUpMoves().size()/MOVES_PER_PAGE);
    }

    @Override
    public void draw(Graphics g) {
        GameData data = Game.getData();

        TileSet partyTiles = data.getPartyTiles();
        TileSet pokedexTiles = data.getPokedexTilesSmall();

        BasicPanels.drawCanvasPanel(g);

        pokedexPanel.drawBackground(g);
        titlePanel.drawBackground(g);
        titlePanel.label(g, 20, PokeString.POKEDEX);

        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLS; j++) {
                int number = getIndex(i, j) + 1;
                if (number > PokemonInfo.NUM_POKEMON) {
                    continue;
                }

                PokemonInfo pokemonInfo = PokemonInfo.getPokemonInfo(number);
                Button pokemonButton = pokemonButtons[i][j];

                if (pokedex.isNotSeen(pokemonInfo)) {
                    pokemonButton.label(g, 20, new Color(0, 0, 0, 64), String.format("%03d", number));
                } else {
                    if (pokemonInfo == selected) {
                        pokemonButton.blackOutline(g);
                    }

                    pokemonButton.imageLabel(g, partyTiles.getTile(pokemonInfo.getTinyImageName()));

                    if (pokedex.isCaught(pokemonInfo)) {
                        BufferedImage pokeball = TileSet.TINY_POKEBALL;
                        g.drawImage(
                                pokeball,
                                pokemonButton.x + pokemonButton.width - 3*pokeball.getWidth()/2,
                                pokemonButton.y + pokemonButton.height - 3*pokeball.getHeight()/2,
                                null
                        );
                    }
                }
            }
        }

        // Draw page numbers and arrows
        g.setColor(Color.BLACK);
        FontMetrics.setFont(g, 16);
        TextUtils.drawCenteredWidthString(g, (pageNum + 1) + "/" + NUM_PAGES, pokedexPanel.centerX(), 433);

        leftButton.drawArrow(g, Direction.LEFT);
        rightButton.drawArrow(g, Direction.RIGHT);

        // Seen/Caught
        countPanel.drawBackground(g);
        countPanel.label(g, 20, "Seen: " + numSeen + "     Caught: " + numCaught);

        // Description
        Type[] type = selected.getType();
        Color[] typeColors = Type.getColors(type);

        boolean notSeen = pokedex.isNotSeen(selected);
        boolean caught = pokedex.isCaught(selected);

        if (notSeen) {
            typeColors = new Color[] { Color.BLACK, Color.BLACK };
        }

        infoPanel.withBackgroundColors(typeColors)
                 .drawBackground(g);

        for (int i = 0; i < tabButtons.length; i++) {
            List<Direction> toOutline = new ArrayList<>();
            toOutline.add(Direction.RIGHT);

            if (i == 0) {
                toOutline.add(Direction.LEFT);
            }

            if (i != selectedTab.ordinal()) {
                toOutline.add(Direction.UP);
            }

            Button tab = tabButtons[i];
            DrawUtils.blackOutline(g, tab.x, tab.y, tab.width, tab.height, toOutline.toArray(new Direction[0]));
            tab.label(g, 12, TabInfo.values()[i].label);
        }

        int spacing = 15;
        int leftX, textY;

        if (selectedTab.shouldDrawInformationPanel(caught)) {
            basicInfoPanel.drawBackground(g);

            // Image
            imagePanel.drawBackground(g);
            if (notSeen) {
                imagePanel.label(g, 80, "?");
            } else {
                BufferedImage pkmImg = pokedexTiles.getTile(selected.getBaseImageName());
                pkmImg.setRGB(0, 0, 0);

                imagePanel.imageLabel(g, pkmImg);
            }

            g.setColor(Color.BLACK);

            // Name
            FontMetrics.setFont(g, 20);
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
                ImageUtils.drawTypeTiles(g, type, infoPanel.rightX() - spacing, textY);

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
                    textY = imagePanel.bottomY() + FontMetrics.getTextHeight(g) + spacing;
                    infoPanel.drawMessage(g, selected.getFlavorText(), textY);
                }
            }
        }

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
                TextUtils.drawRightAlignedString(g, selected.getStat(i) + "", baseStatRightX, y);
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

                    if (i == 0 || selectedButton == MOVE_START + i) {
                        moveDescriptionPanel.withTransparentBackground(attack.getActualType().getColor())
                                            .drawBackground(g);

                        FontMetrics.setFont(g, 18);
                        int moveSpacing = 15;
                        int moveX = moveDescriptionPanel.x + moveSpacing;
                        int rightX = moveDescriptionPanel.rightX() - moveSpacing;
                        int y = moveDescriptionPanel.y + moveSpacing + FontMetrics.getTextHeight(g);

                        g.drawString(attack.getName(), moveX, y);

                        BufferedImage typeImage = attack.getActualType().getImage();
                        int imageY = y - typeImage.getHeight();
                        int imageX = rightX - typeImage.getWidth();
                        g.drawImage(typeImage, imageX, imageY, null);

                        BufferedImage categoryImage = attack.getCategory().getImage();
                        imageX -= categoryImage.getWidth() + moveSpacing;
                        g.drawImage(categoryImage, imageX, imageY, null);

                        y += FontMetrics.getDistanceBetweenRows(g);

                        FontMetrics.setFont(g, 16);
                        g.drawString("Power: " + attack.getPowerString(), moveX, y);
                        TextUtils.drawRightAlignedString(g, "Acc: " + attack.getAccuracyString(), rightX, y);

                        y += FontMetrics.getDistanceBetweenRows(g) + 2;

                        FontMetrics.setFont(g, 12);
                        TextUtils.drawWrappedText(
                                g,
                                attack.getDescription(),
                                moveX,
                                y,
                                moveDescriptionPanel.width - 2*moveSpacing
                        );
                    }

                    Button moveButton = moveButtons[i];
                    moveButton.blackOutline(g);
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

                movesLeftButton.drawArrow(g, Direction.LEFT);
                movesRightButton.drawArrow(g, Direction.RIGHT);

                TextUtils.drawCenteredString(g, (movePageNum + 1) + "/" + maxMovePages(), infoPanel.centerX(), movesLeftButton.centerY());
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
                        textY = TextUtils.drawWrappedText(
                                g,
                                eachEvolution.getEvolutions()[0].getName() + ": " + eachEvolution.getString(),
                                leftX,
                                textY,
                                infoPanel.width - 3*spacing
                        );
                    }
                } else if (evolution instanceof NoEvolution) {
                    g.drawString(selected.getName() + " does not evolve", leftX, textY);
                } else {
                    TextUtils.drawWrappedText(
                            g,
                            evolution.getEvolutions()[0].getName() + ": " + evolution.getString(),
                            leftX,
                            textY,
                            infoPanel.width - 3*spacing
                    );
                }
            }
        }

        // Return button
        returnButton.fillTransparent(g, Color.YELLOW);
        returnButton.fillTransparent(g);
        returnButton.blackOutline(g);
        returnButton.label(g, 20, "Return");

        for (Button button : buttons) {
            button.draw(g);
        }
    }

    private int getIndex(int i, int j) {
        return PER_PAGE*pageNum + i*NUM_COLS + j;
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
        selected = PokemonInfo.getPokemonInfo(1);
        numSeen = pokedex.numSeen();
        numCaught = pokedex.numCaught();
        changeTab(TabInfo.MAIN);
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

        public boolean shouldDrawInformationPanel(boolean caught) {
            return this != MOVES || !caught;
        }
    }
}
