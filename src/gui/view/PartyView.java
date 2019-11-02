package gui.view;

import battle.attack.Attack;
import battle.attack.Move;
import draw.Alignment;
import draw.DrawUtils;
import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonList;
import draw.button.ButtonPanel;
import draw.button.ButtonPanel.ButtonPanelSetup;
import draw.button.ButtonPressAction;
import draw.button.ButtonTransitions;
import draw.layout.ButtonLayout;
import draw.layout.TabLayout;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.MovePanel;
import draw.panel.StatPanel;
import draw.panel.WrapPanel;
import draw.panel.WrapPanel.WrapMetrics;
import gui.GameData;
import gui.TileSet;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import pokemon.ability.Ability;
import pokemon.active.MoveList;
import pokemon.active.PartyPokemon;
import pokemon.breeding.Eggy;
import pokemon.species.PokemonInfo;
import trainer.Trainer;
import trainer.player.Player;
import type.PokeType;
import util.FontMetrics;
import util.Point;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

public class PartyView extends View {
    private static final int NUM_BOTTOM_BUTTONS = 3;
    private static final int NUM_BUTTONS = Trainer.MAX_POKEMON + MoveList.MAX_MOVES + NUM_BOTTOM_BUTTONS;
    private static final int TABS = 0;
    private static final int MOVES = Trainer.MAX_POKEMON;
    private static final int RETURN = NUM_BUTTONS - 1;
    private static final int SWITCH = NUM_BUTTONS - 2;
    private static final int NICKNAME = NUM_BUTTONS - 3;

    private final DrawPanel pokemonPanel;
    private final DrawPanel imagePanel;
    private final DrawPanel basicInformationPanel;
    private final WrapPanel abilityPanel;
    private final StatPanel statsPanel;
    private final MovePanel moveDetailsPanel;
    private final DrawPanel movesPanel;
    private final DrawPanel nicknamePanel;

    private final DrawPanel hpBar;
    private final DrawPanel expBar;

    private final ButtonList buttons;
    private final Button[] tabButtons;
    private final Button[] moveButtons;

    private final Button nicknameButton;
    private final Button switchButton;
    private final Button returnButton;

    private int selectedTab;
    private int switchTabIndex;
    private boolean nicknameView;

    PartyView() {
        selectedTab = 0;
        switchTabIndex = -1;

        int tabHeight = 55;
        int spacing = 28;

        pokemonPanel = new DrawPanel(
                spacing,
                spacing + tabHeight,
                Point.subtract(
                        Global.GAME_SIZE,
                        2*spacing,
                        2*spacing + tabHeight
                )
        )
                .withBorderlessTransparentBackground()
                .withBlackOutline();

        imagePanel = new DrawPanel(
                pokemonPanel.x + spacing,
                pokemonPanel.y + spacing,
                104,
                104
        )
                .withFullTransparency()
                .withBlackOutline();

        basicInformationPanel = new DrawPanel(
                imagePanel.rightX() + spacing,
                imagePanel.y,
                pokemonPanel.width - 3*spacing - imagePanel.width,
                imagePanel.height
        )
                .withFullTransparency()
                .withBlackOutline();

        int barHeight = 15;
        int expBarWidth = basicInformationPanel.width/3;
        expBar = new DrawPanel(
                basicInformationPanel.rightX() - expBarWidth,
                basicInformationPanel.bottomY() - DrawUtils.OUTLINE_SIZE,
                expBarWidth,
                barHeight
        )
                .withBlackOutline();

        int buttonHeight = 38;
        int halfPanelWidth = (pokemonPanel.width - 3*spacing)/2;
        int statsPanelHeight = 138;

        abilityPanel = new WrapPanel(
                imagePanel.x,
                imagePanel.y + imagePanel.height + spacing,
                halfPanelWidth,
                pokemonPanel.height - 5*spacing - imagePanel.height - buttonHeight - statsPanelHeight,
                16
        )
                .withFullTransparency()
                .withBlackOutline()
                .withMinFontSize(12, false);

        statsPanel = new StatPanel(
                abilityPanel.x,
                abilityPanel.y + abilityPanel.height + spacing,
                halfPanelWidth,
                statsPanelHeight,
                16, 14
        )
                .includeCurrentHp()
                .withInsetSpaces(1)
                .withFullTransparency()
                .withBlackOutline();

        hpBar = new DrawPanel(
                statsPanel.x,
                statsPanel.y,
                statsPanel.width/2,
                barHeight
        )
                .withBlackOutline();

        moveDetailsPanel = new MovePanel(statsPanel, 20, 18, 16)
                .withMinDescFontSize(14)
                .withFullTransparency();

        movesPanel = new DrawPanel(
                abilityPanel.rightX() + spacing,
                abilityPanel.y,
                halfPanelWidth,
                statsPanel.bottomY() - abilityPanel.y
        )
                .withFullTransparency()
                .withBlackOutline();

        int buttonWidth = (basicInformationPanel.rightX() - imagePanel.x - (NUM_BOTTOM_BUTTONS - 1)*spacing)/NUM_BOTTOM_BUTTONS;
        nicknameButton = new Button(
                imagePanel.x,
                statsPanel.bottomY() + spacing,
                buttonWidth,
                buttonHeight,
                new ButtonTransitions().right(SWITCH).up(TABS).left(RETURN).down(TABS),
                () -> nicknameView = true,
                textButtonSetup("Nickname!!")
        );

        switchButton = new Button(
                nicknameButton.rightX() + spacing,
                nicknameButton.y,
                buttonWidth,
                buttonHeight,
                new ButtonTransitions().right(RETURN).up(TABS).left(NICKNAME).down(TABS),
                () -> switchTabIndex = switchTabIndex == -1 ? selectedTab : -1,
                textButtonSetup("Switch!")
        );

        returnButton = new Button(
                switchButton.rightX() + spacing,
                switchButton.y,
                buttonWidth,
                buttonHeight,
                new ButtonTransitions().right(NICKNAME).up(MOVES + MoveList.MAX_MOVES - 1).left(SWITCH).down(TABS),
                ButtonPressAction.getExitAction(),
                textButtonSetup("Return")
        );

        tabButtons = new TabLayout(pokemonPanel, Trainer.MAX_POKEMON, tabHeight)
                .withStartIndex(TABS)
                .withDefaultTransitions(new ButtonTransitions().up(RETURN).down(MOVES))
                .withPressIndex(this::switchTab)
                .withButtonSetup(panel -> panel.skipInactive()
                                               .withBorderlessTransparentBackground())
                .getTabs();

        nicknamePanel = new DrawPanel(
                pokemonPanel.x,
                tabButtons[0].y,
                pokemonPanel.width,
                tabButtons[0].height + pokemonPanel.height
        )
                .withBorderlessTransparentBackground()
                .withBlackOutline()
                .withLabelSize(30);

        // Buttons don't actually do anything when pressed, but if hovered updates the move details panel
        moveButtons = new ButtonLayout(movesPanel, MoveList.MAX_MOVES, 1, 10)
                .withStartIndex(MOVES)
                .withDefaultTransitions(new ButtonTransitions().up(0).down(RETURN))
                .withButtonSetup(panel -> panel.skipInactive()
                                               .withTransparentCount(2)
                                               .withBlackOutline()
                                               .withBorderPercentage(20))
                .getButtons();

        this.buttons = new ButtonList(NUM_BUTTONS);
        buttons.set(TABS, tabButtons);
        buttons.set(MOVES, moveButtons);
        buttons.set(NICKNAME, nicknameButton);
        buttons.set(SWITCH, switchButton);
        buttons.set(RETURN, returnButton);

        movedToFront();
    }

    private ButtonPanelSetup textButtonSetup(String label) {
        return panel -> panel.greyInactive()
                             .withBorderlessTransparentBackground()
                             .withBlackOutline()
                             .withLabel(label, 20);
    }

    @Override
    public void update(int dt) {
        InputControl input = InputControl.instance();

        buttons.update();

        if (nicknameView) {
            if (!input.isCapturingText()) {
                input.startTextCapture();
            }

            if (input.consumeIfDown(ControlKey.ENTER)) {
                String nickname = input.stopAndResetCapturedText();
                Game.getPlayer().getTeam().get(selectedTab).setNickname(nickname);

                nicknameView = false;
                updateActiveButtons();
            }
        } else {
            if (buttons.consumeSelectedPress()) {
                updateActiveButtons();
            }

            input.popViewIfEscaped();
        }
    }

    @Override
    public void draw(Graphics g) {
        GameData data = Game.getData();
        Player player = Game.getPlayer();

        List<PartyPokemon> list = player.getTeam();
        PartyPokemon selectedPkm = list.get(selectedTab);

        TileSet pkmTiles = data.getPokemonTilesSmall();
        BufferedImage pkmImg = pkmTiles.getTile(selectedPkm.getImageName());

        // Background
        BasicPanels.drawCanvasPanel(g);

        if (nicknameView) {
            String nickname = InputControl.instance().getInputCaptureString(PartyPokemon.MAX_NAME_LENGTH);
            nicknamePanel.withBackgroundColors(PokeType.getColors(selectedPkm), true)
                         .withImageLabel(pkmImg, nickname)
                         .draw(g);
        } else {
            // Pokemon info
            // Note: Important to draw selected before the tabs because of how outlines work
            drawPokemonInfo(g, selectedPkm, pkmImg);

            // Tabs
            drawTabs(g);

            // Draw button hovers
            buttons.drawHover(g);
        }
    }

    private void drawTabs(Graphics g) {
        GameData data = Game.getData();
        Player player = Game.getPlayer();

        List<PartyPokemon> list = player.getTeam();
        TileSet partyTiles = data.getPartyTiles();

        FontMetrics.setBlackFont(g, 11);

        int spacing = 4;
        int inset = 2*spacing + PokemonInfo.MAX_PARTY_IMAGE_SIZE.width;
        for (int i = 0; i < list.size(); i++) {
            PartyPokemon tabPokemon = list.get(i);
            BufferedImage tabImage = partyTiles.getTile(tabPokemon.getTinyImageName());
            ButtonPanel panel = tabButtons[i].panel();

            // Outline and type color
            panel.withTabOutlines(i, selectedTab)
                 .withBackgroundColor(PokeType.getColors(tabPokemon)[0])
                 .drawBackground(g);

            // Image and name
            int nameX = panel.x + inset;
            int centerY = panel.centerY();
            TextUtils.drawCenteredHeightString(g, tabPokemon.getActualName(), nameX, centerY);
            ImageUtils.drawCenteredImage(g, tabImage, (panel.x + nameX)/2, centerY);

            // Faint out if deadsies
            panel.faintOut(g, tabPokemon);
        }
    }

    private void drawPokemonInfo(Graphics g, PartyPokemon selectedPkm, BufferedImage pkmImg) {
        Color[] typeColors = PokeType.getColors(selectedPkm);

        // Type color polygons
        pokemonPanel.withBackgroundColors(typeColors, true)
                    .drawBackground(g);

        // Highlight switch button if applicable
        switchButton.panel().withHighlight(switchTabIndex != -1, typeColors[1]);
        nicknameButton.drawPanel(g);
        switchButton.drawPanel(g);
        returnButton.drawPanel(g);

        // Pokemon Image
        imagePanel.withImageLabel(pkmImg)
                  .draw(g);

        // Basic information panel
        basicInformationPanel.drawBackground(g);

        FontMetrics.setBlackFont(g, 20);
        int inset = FontMetrics.getDistanceBetweenRows(g)/2;
        int nameX = basicInformationPanel.x + inset;
        int topLineY = basicInformationPanel.y + inset + FontMetrics.getTextHeight(g);

        // Name and Gender
        g.drawString(selectedPkm.getActualName() + " " + selectedPkm.getGenderString(), nameX, topLineY);

        if (selectedPkm.isEgg()) {
            FontMetrics.setFont(g, 16);

            // Description
            TextUtils.drawWrappedText(
                    g,
                    ((Eggy)selectedPkm).getEggMessage(),
                    basicInformationPanel.x + inset,
                    topLineY + inset + FontMetrics.getTextHeight(g),
                    basicInformationPanel.width - 2*inset
            );
        } else {
            // Number
            int numberX = 378;
            String numberString = String.format("#%03d", selectedPkm.getPokemonInfo().getNumber());
            g.drawString(numberString, numberX, topLineY);

            // Shiny sprite
            if (selectedPkm.isShiny()) {
                int imageX = numberX + FontMetrics.getTextWidth(g, numberString + " ");
                BufferedImage starSprite = TileSet.STAR_SPRITE;
                g.drawImage(starSprite, imageX, topLineY - starSprite.getHeight(), null);
            }

            // Status Condition
            g.drawString(selectedPkm.getStatus().getShortName(), 459, topLineY);

            // Level
            int levelX = 525;
            g.drawString("Lv" + selectedPkm.getLevel(), levelX, topLineY);

            FontMetrics.setFont(g, 16);
            int secondLineY = topLineY + inset + FontMetrics.getTextHeight(g);
            int thirdLineY = secondLineY + inset + FontMetrics.getTextHeight(g);
            int fourthLineY = thirdLineY + inset + FontMetrics.getTextHeight(g);
            int rightAlignedX = basicInformationPanel.rightX() - inset;

            // Type Tiles
            ImageUtils.drawTypeTiles(g, selectedPkm.getActualType(), rightAlignedX, topLineY);

            // Nature
            g.drawString(selectedPkm.getNature().getName() + " Nature", nameX, secondLineY);

            // Total EXP
            g.drawString("EXP:", levelX, secondLineY);
            TextUtils.drawRightAlignedString(g, "" + selectedPkm.getTotalEXP(), rightAlignedX, secondLineY);

            // Characteristic
            g.drawString(selectedPkm.getCharacteristic(), nameX, thirdLineY);

            // EXP To Next Level
            g.drawString("To Next Lv:", levelX, thirdLineY);
            TextUtils.drawRightAlignedString(g, "" + selectedPkm.expToNextLevel(), rightAlignedX, thirdLineY);

            // Held Item
            g.drawString(selectedPkm.getActualHeldItem().getName(), nameX, fourthLineY);

            // Ability with description
            drawAbility(g, selectedPkm.getActualAbility());

            // EXP Bar
            expBar.fillBar(g, DrawUtils.EXP_BAR_COLOR, selectedPkm.expRatio());

            MoveList moves = selectedPkm.getActualMoves();

            // Stats Box or Move description
            int selectedButton = buttons.getSelected();
            if (selectedButton >= MOVES && selectedButton < MOVES + MoveList.MAX_MOVES) {
                drawMoveDetails(g, moves.get(selectedButton - MOVES).getAttack());
            } else {
                drawStatBox(g, selectedPkm);
            }

            // Moves Box
            movesPanel.drawBackground(g);
            FontMetrics.setBlackFont(g, 18);
            for (int i = 0; i < moves.size(); i++) {
                Move move = moves.get(i);
                Attack attack = move.getAttack();
                ButtonPanel movePanel = moveButtons[i].panel();

                movePanel.withBackgroundColor(attack.getActualType().getColor())
                         .drawBackground(g);

                int moveInset = movePanel.getBorderSize() + 10;
                TextUtils.drawCenteredHeightString(g, attack.getName(), movePanel.x + moveInset, movePanel.centerY());
                TextUtils.drawCenteredHeightString(g, "PP: " + move.getPPString(), movePanel.rightX() - moveInset, movePanel.centerY(), Alignment.RIGHT);
            }

            pokemonPanel.faintOut(g, selectedPkm);
        }
    }

    private void drawStatBox(Graphics g, PartyPokemon selectedPkm) {
        // Draw stats
        statsPanel.drawBackground(g);
        statsPanel.drawStats(g, selectedPkm);

        // HP Bar
        hpBar.fillBar(g, selectedPkm.getHPColor(), selectedPkm.getHPRatio());
    }

    public WrapMetrics drawAbility(Graphics g, Ability ability) {
        abilityPanel.drawBackground(g);
        return abilityPanel.drawMessage(g, ability.getName() + " - " + ability.getDescription());
    }

    public WrapMetrics drawMoveDetails(Graphics g, Attack move) {
        moveDetailsPanel.drawBackground(g);
        return moveDetailsPanel.drawMove(g, move);
    }

    private void switchTab(int index) {
        if (switchTabIndex != -1) {
            Game.getPlayer().swapPokemon(index, switchTabIndex);
            switchTabIndex = -1;
        }

        selectedTab = index;
    }

    private void updateActiveButtons() {
        if (nicknameView) {
            buttons.setInactive();
        } else {
            List<PartyPokemon> team = Game.getPlayer().getTeam();
            PartyPokemon pkm = team.get(selectedTab);
            MoveList moves = pkm.getActualMoves();

            for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
                tabButtons[i].setActive(i < team.size());
            }

            for (int i = 0; i < MoveList.MAX_MOVES; i++) {
                moveButtons[i].setActive(!pkm.isEgg() && i < moves.size());
            }

            nicknameButton.setActive(!pkm.isEgg());
            switchButton.setActive(team.size() > 1);
            returnButton.setActive(true);
        }
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.PARTY_VIEW;
    }

    @Override
    public void movedToFront() {
        switchTabIndex = -1;
        nicknameView = false;
        buttons.setSelected(0);
        switchTab(0);
        updateActiveButtons();
    }
}
