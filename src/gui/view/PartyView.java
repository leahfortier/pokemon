package gui.view;

import battle.attack.Attack;
import battle.attack.Move;
import draw.Alignment;
import draw.DrawUtils;
import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.button.ButtonList;
import draw.button.ButtonPressAction;
import draw.button.ButtonTransitions;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.MovePanel;
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
import pokemon.stat.Stat;
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
    private final DrawPanel statsPanel;
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
                .withTransparentBackground()
                .withBorderPercentage(0)
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
                14
        )
                .withFullTransparency()
                .withBlackOutline()
                .withMinimumSpacing(0);

        statsPanel = new DrawPanel(
                abilityPanel.x,
                abilityPanel.y + abilityPanel.height + spacing,
                halfPanelWidth,
                statsPanelHeight
        )
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
                ButtonHoverAction.BOX,
                new ButtonTransitions().right(SWITCH).up(TABS).left(RETURN).down(TABS),
                () -> nicknameView = true
        );

        switchButton = new Button(
                nicknameButton.rightX() + spacing,
                nicknameButton.y,
                buttonWidth,
                buttonHeight,
                ButtonHoverAction.BOX,
                new ButtonTransitions().right(RETURN).up(TABS).left(NICKNAME).down(TABS),
                () -> switchTabIndex = switchTabIndex == -1 ? selectedTab : -1
        );

        returnButton = new Button(
                switchButton.rightX() + spacing,
                switchButton.y,
                buttonWidth,
                buttonHeight,
                ButtonHoverAction.BOX,
                new ButtonTransitions().right(NICKNAME).up(MOVES + MoveList.MAX_MOVES - 1).left(SWITCH).down(TABS),
                ButtonPressAction.getExitAction()
        );

        tabButtons = new Button[Trainer.MAX_POKEMON];
        for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
            final int index = i;
            tabButtons[i] = new Button(
                    pokemonPanel.createTab(i, tabHeight, tabButtons.length),
                    ButtonTransitions.getBasicTransitions(
                            i, 1, Trainer.MAX_POKEMON, TABS,
                            new ButtonTransitions().up(RETURN).down(MOVES)
                    ),
                    () -> {
                        if (switchTabIndex != -1) {
                            Game.getPlayer().swapPokemon(index, switchTabIndex);
                            switchTabIndex = -1;
                        }

                        selectedTab = index;
                    }
            );
        }

        nicknamePanel = new DrawPanel(
                pokemonPanel.x,
                tabButtons[0].y,
                pokemonPanel.width,
                tabButtons[0].height + pokemonPanel.height
        )
                .withTransparentBackground()
                .withBorderPercentage(0)
                .withBlackOutline();

        moveButtons = movesPanel.getButtons(
                10, MoveList.MAX_MOVES, 1, MOVES,
                new ButtonTransitions().up(0).down(RETURN)
        );

        Button[] buttons = new Button[NUM_BUTTONS];
        System.arraycopy(tabButtons, 0, buttons, TABS, tabButtons.length);
        System.arraycopy(moveButtons, 0, buttons, MOVES, moveButtons.length);
        buttons[NICKNAME] = nicknameButton;
        buttons[SWITCH] = switchButton;
        buttons[RETURN] = returnButton;
        this.buttons = new ButtonList(buttons);

        updateActiveButtons();
    }

    @Override
    public void update(int dt) {
        Player player = Game.getPlayer();
        InputControl input = InputControl.instance();

        buttons.update();

        if (nicknameView) {
            if (!input.isCapturingText()) {
                input.startTextCapture();
            }

            if (input.consumeIfDown(ControlKey.ENTER)) {
                String nickname = input.stopAndResetCapturedText();
                player.getTeam().get(selectedTab).setNickname(nickname);

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

        // Background
        BasicPanels.drawCanvasPanel(g);

        List<PartyPokemon> list = player.getTeam();
        PartyPokemon selectedPkm = list.get(selectedTab);

        TileSet pkmTiles = data.getPokemonTilesSmall();
        BufferedImage pkmImg = pkmTiles.getTile(selectedPkm.getImageName());

        if (nicknameView) {
            drawNicknameView(g, pkmImg, selectedPkm);
        } else {
            // Pokemon info
            drawPokemonInfo(g, pkmImg, selectedPkm);

            // Tabs
            drawTabs(g, data, list);

            // Nickname, Switch, and Return buttons
            drawButtons(g, selectedPkm);
        }

        buttons.draw(g);
    }

    private Color[] getBackgroundColors(PartyPokemon selectedPkm) {
        return PokeType.getColors(selectedPkm);
    }

    private void drawNicknameView(Graphics g, BufferedImage pkmImg, PartyPokemon selectedPkm) {
        nicknamePanel.withBackgroundColors(PokeType.getColors(selectedPkm), true);
        nicknamePanel.drawBackground(g);

        FontMetrics.setFont(g, 30);

        String nickname = InputControl.instance().getInputCaptureString(PartyPokemon.MAX_NAME_LENGTH);
        ImageUtils.drawCenteredImageLabel(g, pkmImg, nickname, Global.GAME_SIZE.width/2, Global.GAME_SIZE.height/2);
    }

    private void drawTabs(Graphics g, GameData data, List<PartyPokemon> list) {
        TileSet partyTiles = data.getPartyTiles();
        FontMetrics.setFont(g, 14);

        for (int i = 0; i < list.size(); i++) {
            PartyPokemon pkm = list.get(i);
            Button tabButton = tabButtons[i];

            // Color tab
            tabButton.fill(g, PokeType.getColors(pkm)[0]);

            // Fade out fainted Pokemon
            if (!pkm.canFight()) {
                tabButton.greyOut(g);
            }

            // Transparenty
            tabButton.fillTransparent(g);

            // Outline in black
            tabButton.outlineTab(g, i, selectedTab);

            g.translate(tabButton.x, tabButton.y);

            g.setColor(Color.BLACK);
            g.drawString(pkm.getActualName(), 40, 34);

            BufferedImage pkmImg = partyTiles.getTile(pkm.getTinyImageName());
            ImageUtils.drawCenteredImage(g, pkmImg, 19, 26);

            g.translate(-tabButton.x, -tabButton.y);
        }
    }

    private void drawPokemonInfo(Graphics g, BufferedImage pkmImg, PartyPokemon selectedPkm) {
        // Draw type color polygons
        pokemonPanel.withBackgroundColors(this.getBackgroundColors(selectedPkm), true);
        if (!selectedPkm.canFight()) {
            pokemonPanel.greyOut();
        }

        pokemonPanel.drawBackground(g);

        // Draw Pokemon Image
        imagePanel.drawBackground(g);
        imagePanel.imageLabel(g, pkmImg);

        // Draw basic information panel
        basicInformationPanel.drawBackground(g);

        FontMetrics.setFont(g, 20);
        g.setColor(Color.BLACK);

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

            FontMetrics.setFont(g, 16);
            g.setColor(Color.BLACK);

            MoveList moves = selectedPkm.getActualMoves();

            // Stats Box or Move description
            int selectedButton = buttons.getSelected();
            if (selectedButton >= MOVES && selectedButton < MOVES + MoveList.MAX_MOVES) {
                drawMoveDescriptionPanel(g, moves.get(selectedButton - MOVES).getAttack());
            } else {
                drawStatBox(g, selectedPkm);
            }

            // Move Box
            movesPanel.drawBackground(g);
            for (int i = 0; i < moves.size(); i++) {
                Move move = moves.get(i);
                Attack attack = move.getAttack();
                Button moveButton = moveButtons[i];

                DrawPanel movePanel = new DrawPanel(moveButton)
                        .withTransparentBackground(attack.getActualType().getColor())
                        .withTransparentCount(2)
                        .withBorderPercentage(20)
                        .withBlackOutline();
                movePanel.drawBackground(g);

                g.setColor(Color.BLACK);
                FontMetrics.setFont(g, 18);

                int moveInset = movePanel.getBorderSize() + 10;
                TextUtils.drawCenteredHeightString(g, attack.getName(), movePanel.x + moveInset, movePanel.centerY());
                TextUtils.drawCenteredHeightString(g, String.format("PP: %d/%d", move.getPP(), move.getMaxPP()), movePanel.rightX() - moveInset, movePanel.centerY(), Alignment.RIGHT);
            }
        }
    }

    private void drawStatBox(Graphics g, PartyPokemon selectedPkm) {
        statsPanel.drawBackground(g);

        int spacing = statsPanel.height/(Stat.NUM_STATS + 1);
        int firstRowY = statsPanel.y + spacing - 2;

        g.drawString("Stat", 250, firstRowY);
        g.drawString("IV", 310, firstRowY);
        g.drawString("EV", 355, firstRowY);

        for (int i = 0; i < Stat.NUM_STATS; i++) {
            g.setColor(selectedPkm.getNature().getColor(i));
            g.drawString(Stat.getStat(i, false).getName(), statsPanel.x + 10, firstRowY + (i + 1)*spacing);
        }

        FontMetrics.setFont(g, 14);
        g.setColor(Color.BLACK);

        for (int i = 0; i < Stat.NUM_STATS; i++) {
            final String statString;
            if (i == Stat.HP.index()) {
                statString = selectedPkm.getHP() + "/" + selectedPkm.getStat(i);
            } else {
                statString = "" + selectedPkm.getStat(i);
            }

            int drawY = firstRowY + (i + 1)*spacing;
            TextUtils.drawRightAlignedString(g, statString, 285, drawY);
            TextUtils.drawRightAlignedString(g, "" + selectedPkm.getIVs().get(i), 327, drawY);
            TextUtils.drawRightAlignedString(g, "" + selectedPkm.getEVs().get(i), 371, drawY);
        }

        // HP Bar
        hpBar.fillBar(g, selectedPkm.getHPColor(), selectedPkm.getHPRatio());
    }

    public WrapMetrics drawAbility(Graphics g, Ability ability) {
        abilityPanel.drawBackground(g);
        return abilityPanel.drawMessage(g, ability.getName() + " - " + ability.getDescription());
    }

    public WrapMetrics drawMoveDescriptionPanel(Graphics g, Attack move) {
        return moveDetailsPanel.draw(g, move);
    }

    private void drawButtons(Graphics g, PartyPokemon selectedPkm) {
        // Nickname button
        if (!nicknameButton.isActive()) {
            nicknameButton.greyOut(g);
        }

        nicknameButton.fillTransparent(g);
        nicknameButton.blackOutline(g);
        nicknameButton.label(g, 20, "Nickname!!");

        // Switch Box
        if (!switchButton.isActive()) {
            switchButton.greyOut(g);
        } else if (switchTabIndex != -1) {
            switchButton.highlight(g, this.getBackgroundColors(selectedPkm)[1]);
        }

        switchButton.fillTransparent(g);
        switchButton.blackOutline(g);
        switchButton.label(g, 20, "Switch!");

        // Return Box
        returnButton.fillTransparent(g);
        returnButton.blackOutline(g);
        returnButton.label(g, 20, "Return");
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
        selectedTab = 0;
        switchTabIndex = -1;
        nicknameView = false;
        buttons.setSelected(0);
        updateActiveButtons();
    }
}
