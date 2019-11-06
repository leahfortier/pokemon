package gui.view;

import battle.ActivePokemon;
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
import draw.layout.ButtonLayout;
import draw.layout.DrawLayout;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.PanelList;
import draw.panel.StatPanel;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import pokemon.active.MoveList;
import pokemon.active.PartyPokemon;
import pokemon.breeding.DayCareCenter;
import pokemon.breeding.Eggy;
import trainer.Trainer;
import trainer.player.Player;
import type.PokeType;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

class DayCareView extends View {
    private static final int NUM_BUTTONS = Trainer.MAX_POKEMON + 4;
    private static final int PARTY = 0;
    private static final int BOTTOM_PARTY = PARTY + Trainer.MAX_POKEMON - 1;
    private static final int RETURN = NUM_BUTTONS - 1;
    private static final int DEPOSIT_WITHDRAW = NUM_BUTTONS - 2;
    private static final int FIRST_DAY_CARE_POKEMON = NUM_BUTTONS - 3;
    private static final int SECOND_DAY_CARE_POKEMON = NUM_BUTTONS - 4;

    private final PanelList panels;
    private final DrawPanel infoPanel;
    private final DrawPanel imagePanel;
    private final DrawPanel[] movePanels;
    private final StatPanel statsPanel;

    private final ButtonList buttons;
    private final Button firstDayCarePokemonButton;
    private final Button secondDayCarePokemonButton;
    private final Button[] partyButtons;
    private final Button depositWithdrawButton;

    private DayCareCenter dayCareCenter;
    private List<PartyPokemon> team;

    private PartyPokemon selected;
    private boolean party;
    private String message;

    DayCareView() {
        int spacing = 40;
        int buttonHeight = 38;

        DrawPanel dayCarePanel = new DrawPanel(
                spacing,
                spacing,
                (Global.GAME_SIZE.width - 2*spacing - spacing/2)/2,
                3*(Global.GAME_SIZE.height - 2*spacing - spacing/2)/(Trainer.MAX_POKEMON + 1 + 3)
        )
                .withBackgroundColor(Color.BLUE)
                .withTransparentCount(2)
                .withBorderPercentage(0)
                .withBlackOutline();

        DrawPanel partyPanel = new DrawPanel(
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
                .withBorderlessTransparentBackground()
                .withBlackOutline();

        DrawPanel basicInfoPanel = new DrawPanel(infoPanel.x, infoPanel.y, infoPanel.width, 190)
                .withFullTransparency()
                .withBlackOutline();

        int statsPanelHeight = 148;
        statsPanel = new StatPanel(
                infoPanel.x,
                infoPanel.bottomY() - statsPanelHeight,
                infoPanel.width,
                statsPanelHeight,
                16, 14
        )
                .withFullTransparency()
                .withBlackOutline();

        DrawPanel movesPanel = new DrawPanel(
                infoPanel.x,
                basicInfoPanel.bottomY() - DrawUtils.OUTLINE_SIZE,
                infoPanel.width,
                infoPanel.height - basicInfoPanel.height - statsPanel.height + 2*DrawUtils.OUTLINE_SIZE
        )
                .withFullTransparency()
                .withBlackOutline();

        movePanels = new DrawLayout(movesPanel, 2, MoveList.MAX_MOVES/2, 10)
                .withDrawSetup(panel -> panel.withTransparentCount(2)
                                             .withBorderPercentage(15)
                                             .withBlackOutline()
                                             .withLabelSize(16))
                .getPanels();

        imagePanel = new DrawPanel(
                infoPanel.x + 18,
                infoPanel.y + 18,
                104,
                104
        ).withFullTransparency().withBlackOutline();

        // Fake panels with three rows (label + each day care pokemon), and one column
        int buttonSpacing = 10;
        DrawPanel[] fakeDayCarePanels = new DrawLayout(dayCarePanel, 3, 1, buttonSpacing).getPanels();

        // Label isn't a button but still uses the spacing
        DrawPanel dayCareLabelPanel = labelPanelSetup("Day Care", fakeDayCarePanels[0]);

        firstDayCarePokemonButton = new Button(
                fakeDayCarePanels[1],
                new ButtonTransitions()
                        .right(DEPOSIT_WITHDRAW)
                        .up(BOTTOM_PARTY)
                        .left(RETURN)
                        .down(SECOND_DAY_CARE_POKEMON),
                () -> selected = dayCareCenter.getFirstPokemon(),
                pokemonButtonSetup()
        );

        secondDayCarePokemonButton = new Button(
                fakeDayCarePanels[2],
                new ButtonTransitions()
                        .right(DEPOSIT_WITHDRAW)
                        .up(FIRST_DAY_CARE_POKEMON)
                        .left(RETURN)
                        .down(PARTY),
                () -> selected = dayCareCenter.getSecondPokemon(),
                pokemonButtonSetup()
        );

        ButtonLayout partyLayout = new ButtonLayout(partyPanel, Trainer.MAX_POKEMON, 1, buttonSpacing)
                .withMissingTopRow();

        // Get the missing top row panel as the label
        DrawPanel partyLabelPanel = labelPanelSetup("Party", partyLayout.getPanel(0, 0));

        partyButtons = partyLayout.withStartIndex(PARTY)
                                  .withDefaultTransitions(new ButtonTransitions()
                                                                  .right(DEPOSIT_WITHDRAW)
                                                                  .up(SECOND_DAY_CARE_POKEMON)
                                                                  .left(RETURN)
                                                                  .down(FIRST_DAY_CARE_POKEMON))
                                  .withPressIndex(index -> selected = team.get(index))
                                  .withButtonSetup(pokemonButtonSetup())
                                  .getButtons();

        depositWithdrawButton = new Button(
                infoPanel.x,
                infoPanel.bottomY() + spacing/2,
                (infoPanel.width - spacing/2)/2,
                buttonHeight,
                new ButtonTransitions().right(RETURN).left(PARTY),
                () -> {
                    if (party) {
                        message = dayCareCenter.deposit((ActivePokemon)selected);
                    } else {
                        message = dayCareCenter.withdraw((ActivePokemon)selected);
                    }
                },
                // Deposit/Withdraw text set depending on state
                textButtonSetup("", new Color(123, 213, 74))
        );

        Button returnButton = new Button(
                infoPanel.rightX() - depositWithdrawButton.width,
                depositWithdrawButton.y,
                depositWithdrawButton.width,
                buttonHeight,
                new ButtonTransitions().right(PARTY).left(DEPOSIT_WITHDRAW),
                ButtonPressAction.getExitAction(),
                textButtonSetup("Return", Color.YELLOW)
        );

        this.buttons = new ButtonList(NUM_BUTTONS);
        buttons.set(PARTY, partyButtons);
        buttons.set(FIRST_DAY_CARE_POKEMON, firstDayCarePokemonButton);
        buttons.set(SECOND_DAY_CARE_POKEMON, secondDayCarePokemonButton);
        buttons.set(DEPOSIT_WITHDRAW, depositWithdrawButton);
        buttons.set(RETURN, returnButton);

        this.panels = new PanelList(
                dayCarePanel, dayCareLabelPanel, partyPanel, partyLabelPanel,
                infoPanel, basicInfoPanel, movesPanel, statsPanel, imagePanel
        ).add(movePanels);
    }

    private DrawPanel labelPanelSetup(String label, DrawPanel panel) {
        return panel.withNoBackground()
                    .withLabel(label, 24);
    }

    private ButtonPanelSetup textButtonSetup(String text, Color color) {
        return panel -> panel.greyInactive()
                             .withBorderlessTransparentBackground()
                             .withBackgroundColor(color)
                             .withLabel(text, 20);
    }

    private ButtonPanelSetup pokemonButtonSetup() {
        return panel -> panel.skipInactive()
                             .withBorderlessTransparentBackground()
                             .withBlackOutline()
                             .withLabelSize(20);
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

        buttons.update();
        if (buttons.consumeSelectedPress()) {
            updateActiveButtons();
        }

        input.popViewIfEscaped();
    }

    private void setupPokemonButton(ButtonPanel panel, PartyPokemon pokemon) {
        if (pokemon == null) {
            panel.skipDraw();
            return;
        }

        // Centered label with party tile, name, and gender
        panel.withImageLabel(
                Game.getData().getPartyTiles().getTile(pokemon.getTinyImageName()),
                pokemon.getNameAndGender()
        );
    }

    private void drawSetup() {
        // Selected Pokemon
        infoPanel.withBackgroundColors(PokeType.getColors(selected));
        imagePanel.withImageLabel(Game.getData().getPokemonTilesSmall().getTile(selected.getImageName()));

        MoveList moves = selected.getActualMoves();
        for (int i = 0; i < movePanels.length; i++) {
            DrawPanel movePanel = movePanels[i];
            if (!selected.isEgg() && i < moves.size()) {
                Attack attack = moves.get(i).getAttack();
                movePanel.withBackgroundColor(attack.getActualType().getColor())
                         .withLabel(attack.getName());
            } else {
                movePanel.skipDraw();
            }
        }

        // Day Care Pokemon
        setupPokemonButton(firstDayCarePokemonButton.panel(), dayCareCenter.getFirstPokemon());
        setupPokemonButton(secondDayCarePokemonButton.panel(), dayCareCenter.getSecondPokemon());

        // Party Pokemon
        for (int i = 0; i < team.size(); i++) {
            setupPokemonButton(partyButtons[i].panel(), team.get(i));
        }

        // Either deposit or withdraw
        depositWithdrawButton.panel().withLabel(party ? "Deposit" : "Withdraw");
    }

    @Override
    public void draw(Graphics g) {
        drawSetup();

        // Background
        BasicPanels.drawCanvasPanel(g);
        panels.drawAll(g);
        buttons.drawPanels(g);

        // Selected pokemon
        drawSelectedPokemon(g);

        // Draw message or button hovers
        if (message != null) {
            BasicPanels.drawFullMessagePanel(g, message);
        } else {
            buttons.drawHover(g);
        }
    }

    private void drawSelectedPokemon(Graphics g) {
        FontMetrics.setBlackFont(g, 20);
        g.drawString(selected.getNameAndGender(), 541, 82);

        if (selected.isEgg()) {
            FontMetrics.setFont(g, 16);
            TextUtils.drawWrappedText(g, ((Eggy)selected).getEggMessage(), 427, 179, 740 - 427);
        } else {
            TextUtils.drawRightAlignedString(g, "Lv" + selected.getLevel(), 740, 82);
            g.drawString("#" + String.format("%03d", selected.getPokemonInfo().getNumber()), 540, 110);

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

            // Stats
            statsPanel.drawStats(g, selected);
        }
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.DAY_CARE_VIEW;
    }

    @Override
    public void movedToFront() {
        Player player = Game.getPlayer();
        dayCareCenter = player.getDayCareCenter();
        selected = null;
        buttons.setSelected(PARTY);

        updateActiveButtons();
    }

    private void updateActiveButtons() {
        Player player = Game.getPlayer();
        team = player.getTeam();

        ActivePokemon first = dayCareCenter.getFirstPokemon();
        ActivePokemon second = dayCareCenter.getSecondPokemon();

        firstDayCarePokemonButton.setActive(first != null);
        secondDayCarePokemonButton.setActive(second != null);

        party = false;
        for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
            partyButtons[i].setActive(i < team.size());
            if (i < team.size() && team.get(i) == selected) {
                party = true;
            }
        }

        // Reset selected Pokemon if unassigned or if pointing to a Pokemon not in the current view
        if (selected == null || (!party && selected != first && selected != second)) {
            selected = team.get(0);
            party = true;
        }

        depositWithdrawButton.setActive(!party || dayCareCenter.canDeposit(selected));
    }
}
