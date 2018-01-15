package gui.view;

import battle.ActivePokemon;
import battle.attack.Attack;
import battle.attack.Move;
import draw.DrawUtils;
import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import gui.GameData;
import gui.TileSet;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import pokemon.PartyPokemon;
import pokemon.Stat;
import pokemon.breeding.DayCareCenter;
import pokemon.breeding.Eggy;
import trainer.Trainer;
import trainer.player.Player;
import type.Type;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

class DayCareView extends View {
    private static final int NUM_BUTTONS = Trainer.MAX_POKEMON + 4;
    private static final int RETURN = NUM_BUTTONS - 1;
    private static final int DEPOSIT_WITHDRAW = NUM_BUTTONS - 2;
    private static final int FIRST_DAY_CARE_POKEMON_BUTTON = NUM_BUTTONS - 3;
    private static final int SECOND_DAY_CARE_POKEMON_BUTTON = NUM_BUTTONS - 4;

    private final DrawPanel dayCarePanel;
    private final DrawPanel partyPanel;
    private final DrawPanel infoPanel;
    private final DrawPanel imagePanel;
    private final DrawPanel basicInfoPanel;
    private final DrawPanel movesPanel;
    private final DrawPanel[] movePanels;
    private final DrawPanel statsPanel;

    private final Button[] buttons;
    private final Button firstDayCarePokemonButton;
    private final Button secondDayCarePokemonButton;
    private final Button[] partyButtons;
    private final Button depositWithdrawButton;
    private final Button returnButton;

    private DayCareCenter dayCareCenter;
    private List<PartyPokemon> team;

    private PartyPokemon selected;
    private boolean party;
    private int selectedButton;
    private String message;

    DayCareView() {
        int spacing = 40;
        int buttonHeight = 38;

        dayCarePanel = new DrawPanel(
                spacing,
                spacing,
                (Global.GAME_SIZE.width - 2*spacing - spacing/2)/2,
                3*(Global.GAME_SIZE.height - 2*spacing - spacing/2)/(Trainer.MAX_POKEMON + 1 + 3)
        )
                .withBackgroundColor(Color.BLUE)
                .withTransparentCount(2)
                .withBorderPercentage(0)
                .withBlackOutline();

        partyPanel = new DrawPanel(
                dayCarePanel.x,
                dayCarePanel.bottomY() + spacing/2,
                dayCarePanel.width,
                Global.GAME_SIZE.height - dayCarePanel.height - 2*spacing - spacing/2
        )
                .withBackgroundColor(Color.RED)
                .withTransparentCount(2)
                .withBorderPercentage(0)
                .withBlackOutline();

        infoPanel = new DrawPanel(
                dayCarePanel.rightX() + spacing/2,
                dayCarePanel.y,
                dayCarePanel.width,
                Global.GAME_SIZE.height - buttonHeight - 2*spacing - spacing/2
        )
                .withTransparentBackground()
                .withBorderPercentage(0)
                .withBlackOutline();

        basicInfoPanel = new DrawPanel(infoPanel.x, infoPanel.y, infoPanel.width, 190)
                .withFullTransparency()
                .withBlackOutline();

        int statsPanelHeight = 148;
        statsPanel = new DrawPanel(
                infoPanel.x,
                infoPanel.bottomY() - statsPanelHeight,
                infoPanel.width,
                statsPanelHeight
        )
                .withFullTransparency()
                .withBlackOutline();

        movesPanel = new DrawPanel(
                infoPanel.x,
                basicInfoPanel.bottomY() - DrawUtils.OUTLINE_SIZE,
                infoPanel.width,
                infoPanel.height - basicInfoPanel.height - statsPanel.height + 2*DrawUtils.OUTLINE_SIZE
        )
                .withFullTransparency()
                .withBlackOutline();

        int moveSpacing = 10;
        int movePanelWidth = (movesPanel.width - 3*moveSpacing)/2;
        int movePanelHeight = (movesPanel.height - 3*moveSpacing)/2;
        movePanels = new DrawPanel[Move.MAX_MOVES];
        for (int i = 0; i < movePanels.length; i++) {
            movePanels[i] = new DrawPanel(
                    movesPanel.x + (i%2 + 1)*moveSpacing + (i%2)*movePanelWidth,
                    movesPanel.y + (i/2 + 1)*moveSpacing + (i/2)*movePanelHeight,
                    movePanelWidth,
                    movePanelHeight
            )
                    .withTransparentCount(2)
                    .withBorderPercentage(20)
                    .withBlackOutline();
        }

        imagePanel = new DrawPanel(
                infoPanel.x + 18,
                infoPanel.y + 18,
                104,
                104
        )
                .withFullTransparency()
                .withBlackOutline();

        selectedButton = DEPOSIT_WITHDRAW;

        buttons = new Button[NUM_BUTTONS];

        int buttonSpacing = 10;
        int pokemonButtonHeight = (dayCarePanel.height - 4*buttonSpacing)/3;
        firstDayCarePokemonButton = buttons[FIRST_DAY_CARE_POKEMON_BUTTON] = new Button(
                dayCarePanel.x + buttonSpacing,
                dayCarePanel.y + pokemonButtonHeight + 2*buttonSpacing,
                dayCarePanel.width - 2*buttonSpacing,
                pokemonButtonHeight,
                ButtonHoverAction.BOX,
                new int[] {
                        DEPOSIT_WITHDRAW,
                        Trainer.MAX_POKEMON - 1,
                        DEPOSIT_WITHDRAW,
                        SECOND_DAY_CARE_POKEMON_BUTTON
                },
                () -> selected = dayCareCenter.getFirstPokemon()
        );

        secondDayCarePokemonButton = buttons[SECOND_DAY_CARE_POKEMON_BUTTON] = new Button(
                firstDayCarePokemonButton.x,
                firstDayCarePokemonButton.bottomY() + buttonSpacing,
                firstDayCarePokemonButton.width,
                firstDayCarePokemonButton.height,
                ButtonHoverAction.BOX,
                new int[] { DEPOSIT_WITHDRAW, FIRST_DAY_CARE_POKEMON_BUTTON, DEPOSIT_WITHDRAW, 0 },
                () -> selected = dayCareCenter.getSecondPokemon()
        );

        pokemonButtonHeight = (partyPanel.height - (Trainer.MAX_POKEMON + 2)*buttonSpacing)/(Trainer.MAX_POKEMON + 1);
        partyButtons = new Button[Trainer.MAX_POKEMON];
        for (int i = 0; i < partyButtons.length; i++) {
            final int index = i; // Silly Java, Trix are for kids
            partyButtons[i] = buttons[i] = new Button(
                    firstDayCarePokemonButton.x,
                    partyPanel.y + (i + 1)*pokemonButtonHeight + (i + 2)*buttonSpacing,
                    firstDayCarePokemonButton.width,
                    pokemonButtonHeight,
                    ButtonHoverAction.BOX,
                    Button.getBasicTransitions(i, Trainer.MAX_POKEMON, 1, 0,
                                               new int[] {
                                                       DEPOSIT_WITHDRAW,
                                                       SECOND_DAY_CARE_POKEMON_BUTTON,
                                                       DEPOSIT_WITHDRAW,
                                                       FIRST_DAY_CARE_POKEMON_BUTTON
                                               }
                    ),
                    () -> selected = team.get(index)
            );
        }

        depositWithdrawButton = buttons[DEPOSIT_WITHDRAW] = new Button(
                infoPanel.x,
                infoPanel.bottomY() + spacing/2,
                (infoPanel.width - spacing/2)/2,
                buttonHeight,
                ButtonHoverAction.BOX,
                new int[] { RETURN, -1, 0, -1 },
                () -> {
                    if (party) {
                        message = dayCareCenter.deposit((ActivePokemon)selected);
                    } else {
                        message = dayCareCenter.withdraw((ActivePokemon)selected);
                    }
                }
        );

        returnButton = buttons[RETURN] = Button.createExitButton(
                infoPanel.rightX() - depositWithdrawButton.width,
                depositWithdrawButton.y,
                depositWithdrawButton.width,
                buttonHeight,
                ButtonHoverAction.BOX,
                new int[] { 0, -1, DEPOSIT_WITHDRAW, -1 }
        );
    }

    @Override
    public void update(int dt) {
        if (BasicPanels.isAnimatingMessage()) {
            return;
        }

        InputControl input = InputControl.instance();
        if (message != null) {
            if (input.consumeIfMouseDown(ControlKey.SPACE)) {
                message = null;
            }

            return;
        }

        selectedButton = Button.update(buttons, selectedButton);
        if (buttons[selectedButton].checkConsumePress()) {
            updateActiveButtons();
        }

        input.popViewIfEscaped();
    }

    private void drawPokemonButton(Graphics g, Button button, PartyPokemon pokemon) {
        if (pokemon != null) {
            button.fillTransparent(g);
            button.blackOutline(g);

            TileSet partyTiles = Game.getData().getPartyTiles();
            BufferedImage image = partyTiles.getTile(pokemon.getTinyImageName());
            FontMetrics.setFont(g, 20);
            ImageUtils.drawCenteredImageLabel(
                    g,
                    image,
                    pokemon.getActualName() + " " + pokemon.getGenderString(),
                    button.centerX(),
                    button.centerY()
            );
        }
    }

    @Override
    public void draw(Graphics g) {
        GameData data = Game.getData();
        TileSet pokemonTiles = data.getPokemonTilesSmall();

        BasicPanels.drawCanvasPanel(g);

        // Day Care Panel
        dayCarePanel.drawBackground(g);
        drawPokemonButton(g, firstDayCarePokemonButton, dayCareCenter.getFirstPokemon());
        drawPokemonButton(g, secondDayCarePokemonButton, dayCareCenter.getSecondPokemon());

        FontMetrics.setFont(g, 24);
        TextUtils.drawCenteredString(g, "Day Care", dayCarePanel.centerX(), (dayCarePanel.y + firstDayCarePokemonButton.y)/2);

        // Party Panel
        partyPanel.drawBackground(g);
        TextUtils.drawCenteredString(g, "Party", partyPanel.centerX(), (partyPanel.y + partyButtons[0].y)/2);
        for (int i = 0; i < team.size(); i++) {
            drawPokemonButton(g, partyButtons[i], team.get(i));
        }

        // Description
        Type[] type = selected.getActualType();
        infoPanel.withBackgroundColors(Type.getColors(selected))
                 .drawBackground(g);

        basicInfoPanel.drawBackground(g);
        movesPanel.drawBackground(g);
        statsPanel.drawBackground(g);

        BufferedImage pkmImg = pokemonTiles.getTile(selected.getImageName());
        imagePanel.drawBackground(g);
        imagePanel.imageLabel(g, pkmImg);

        g.setColor(Color.BLACK);
        FontMetrics.setFont(g, 20);
        g.drawString(selected.getActualName() + " " + selected.getGenderString(), 541, 82);

        if (selected.isEgg()) {
            FontMetrics.setFont(g, 16);
            TextUtils.drawWrappedText(g, ((Eggy)selected).getEggMessage(), 427, 179, 740 - 427);
        } else {
            TextUtils.drawRightAlignedString(g, "Lv" + selected.getLevel(), 740, 82);
            g.drawString("#" + String.format("%03d", selected.getPokemonInfo().getNumber()), 541, 110);

            int index = 0;
            if (type[1] != Type.NO_TYPE) {
                g.drawImage(type[0].getImage(), 669, 97, null);
                index = 1;
            }

            g.drawImage(type[index].getImage(), 707, 97, null);

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

            List<Move> moves = selected.getActualMoves();
            for (int i = 0; i < moves.size(); i++) {
                Attack attack = moves.get(i).getAttack();

                movePanels[i].withBackgroundColor(attack.getActualType().getColor());
                movePanels[i].drawBackground(g);
                movePanels[i].label(g, 16, attack.getName());
            }

            int statsY = statsPanel.y;
            statsY += FontMetrics.getTextHeight(g) + 10;

            TextUtils.drawRightAlignedString(g, "Stat", 635, statsY);
            TextUtils.drawRightAlignedString(g, "IV", 681, statsY);
            TextUtils.drawRightAlignedString(g, "EV", 735, statsY);

            for (int i = 0; i < Stat.NUM_STATS; i++) {
                statsY += FontMetrics.getTextHeight(g) + 11;

                FontMetrics.setFont(g, 16);
                g.setColor(selected.getNature().getColor(i));
                g.drawString(Stat.getStat(i, false).getName(), 427, statsY);

                g.setColor(Color.BLACK);
                FontMetrics.setFont(g, 14);

                TextUtils.drawRightAlignedString(g, selected.getStat(i) + "", 635, statsY);
                TextUtils.drawRightAlignedString(g, selected.getIV(i) + "", 681, statsY);
                TextUtils.drawRightAlignedString(g, selected.getEV(i) + "", 735, statsY);
            }
        }

        // Buttons
        drawTextButton(g, depositWithdrawButton, party ? "Deposit" : "Withdraw", new Color(123, 213, 74));
        drawTextButton(g, returnButton, "Return", Color.YELLOW);

        for (Button b : buttons) {
            b.draw(g);
        }

        if (message != null) {
            BasicPanels.drawFullMessagePanel(g, message);
        }
    }

    private void drawTextButton(Graphics g, Button button, String text, Color color) {
        button.fillTransparent(g, color);
        if (!button.isActive()) {
            button.greyOut(g);
        }

        button.blackOutline(g);
        button.label(g, 20, text);
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.DAY_CARE_VIEW;
    }

    @Override
    public void movedToFront() {
        Player player = Game.getPlayer();
        selected = player.front();
        dayCareCenter = player.getDayCareCenter();

        party = true;
        updateActiveButtons();
    }

    private void updateActiveButtons() {
        Player player = Game.getPlayer();
        team = player.getTeam();

        firstDayCarePokemonButton.setActive(dayCareCenter.getFirstPokemon() != null);
        secondDayCarePokemonButton.setActive(dayCareCenter.getSecondPokemon() != null);

        party = false;
        for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
            partyButtons[i].setActive(i < team.size());
            if (i < team.size() && team.get(i) == selected) {
                party = true;
            }
        }

        depositWithdrawButton.setActive(!party || dayCareCenter.canDeposit(selected));
    }
}
