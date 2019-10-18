package gui.view;

import battle.attack.Attack;
import draw.DrawUtils;
import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.button.ButtonInfo;
import draw.button.ButtonList;
import draw.button.ButtonPanel;
import draw.button.ButtonPressAction;
import draw.button.ButtonTransitions;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.PanelList;
import input.InputControl;
import main.Game;
import map.Direction;
import pokemon.active.MoveList;
import pokemon.active.PartyPokemon;
import pokemon.breeding.Eggy;
import pokemon.stat.Stat;
import trainer.Trainer;
import trainer.player.PC;
import trainer.player.Player;
import type.PokeType;
import util.FontMetrics;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

class PCView extends View {
    private static final int NUM_BUTTONS = PC.BOX_HEIGHT*PC.BOX_WIDTH + Trainer.MAX_POKEMON + 6;
    private static final int RETURN = NUM_BUTTONS - 1;
    private static final int RELEASE = NUM_BUTTONS - 2;
    private static final int DEPOSIT_WITHDRAW = NUM_BUTTONS - 3;
    private static final int SWITCH = NUM_BUTTONS - 4;
    private static final int RIGHT_ARROW = NUM_BUTTONS - 5;
    private static final int LEFT_ARROW = NUM_BUTTONS - 6;
    private static final int PARTY = PC.BOX_HEIGHT*PC.BOX_WIDTH;

    private final DrawPanel boxPanel;
    private final DrawPanel boxNamePanel;
    private final DrawPanel infoPanel;
    private final DrawPanel imagePanel;
    private final PanelList panels;

    private final ButtonList buttons;
    private final Button[][] boxButtons;
    private final Button[] partyButtons;
    private final Button leftButton;
    private final Button rightButton;
    private final Button switchButton;
    private final Button depositWithdrawButton;
    private final Button releaseButton;

    private final PC pc;

    private PartyPokemon selected;
    private boolean party;
    private boolean depositClicked;
    private boolean switchClicked;

    PCView() {
        pc = Game.getPlayer().getPC();
        selected = Game.getPlayer().front();
        party = true;

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
        DrawPanel statsPanel = new DrawPanel(
                infoPanel.x,
                infoPanel.bottomY() - buttonHeight - statsPanelHeight,
                infoPanel.width,
                statsPanelHeight + DrawUtils.OUTLINE_SIZE
        ).withFullTransparency()
         .withBlackOutline();

        DrawPanel movesPanel = new DrawPanel(
                infoPanel.x,
                basicInfoPanel.y + basicInfoPanel.height - DrawUtils.OUTLINE_SIZE,
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

        Button[] buttons = new Button[NUM_BUTTONS];
        boxButtons = new Button[PC.BOX_HEIGHT][PC.BOX_WIDTH];
        for (int i = 0, k = 0; i < PC.BOX_HEIGHT; i++) {
            for (int j = 0; j < PC.BOX_WIDTH; j++, k++) {
                final int row = i;
                final int col = j;

                buttons[k] = boxButtons[i][j] = new Button(
                        60 + 54*j, 96 + 54*i, 40, 40, ButtonHoverAction.BOX,
                        new ButtonTransitions()
                                .right(j == PC.BOX_WIDTH - 1 ? SWITCH : k + 1)
                                .up(i == 0 ? PARTY + j : k - PC.BOX_WIDTH)
                                .left(j == 0 ? RELEASE : k - 1)
                                .down(i == PC.BOX_HEIGHT - 1 ? (j < PC.BOX_WIDTH/2 ? LEFT_ARROW : RIGHT_ARROW) : k + PC.BOX_WIDTH),
                        () -> {
                            if (party && depositClicked) {
                                pc.depositPokemonFromPlayer(selected, row, col);
                                depositClicked = false;
                            } else if (switchClicked) {
                                pc.switchPokemon(selected, row, col);
                                switchClicked = false;
                            } else {
                                selected = pc.getBoxPokemon()[row][col];
                                party = false;
                            }
                        }
                );
            }
        }

        partyButtons = new Button[Trainer.MAX_POKEMON];
        for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
            final int index = i;
            buttons[PARTY + i] = partyButtons[i] = new Button(
                    60 + 54*i, 499, 40, 40, ButtonHoverAction.BOX,
                    new ButtonTransitions()
                            .right(i == Trainer.MAX_POKEMON - 1 ? RETURN : PARTY + i + 1)
                            .up(i < PC.BOX_WIDTH/2 ? LEFT_ARROW : RIGHT_ARROW)
                            .left(i == 0 ? RETURN : PARTY + i - 1)
                            .down(i),
                    () -> {
                        if (party && depositClicked) {
                            depositClicked = false;
                        } else if (switchClicked) {
                            pc.switchPokemon(selected, index);
                            switchClicked = false;
                        } else {
                            selected = Game.getPlayer().getTeam().get(index);
                            party = true;
                        }
                    }
            );
        }

        buttons[LEFT_ARROW] = leftButton = new Button(
                140, 418, 35, 20,
                arrowSetup(Direction.LEFT).transition(
                        new ButtonTransitions()
                                .right(RIGHT_ARROW)
                                .up(PC.BOX_WIDTH*(PC.BOX_HEIGHT - 1) + PC.BOX_WIDTH/2 - 1)
                                .down(PARTY)
                )
        );

        buttons[RIGHT_ARROW] = rightButton = new Button(
                255, 418, 35, 20,
                arrowSetup(Direction.RIGHT).transition(
                        new ButtonTransitions()
                                .right(SWITCH)
                                .up(PC.BOX_WIDTH*(PC.BOX_HEIGHT - 1) + PC.BOX_WIDTH/2)
                                .left(LEFT_ARROW)
                                .down(PARTY)
                )
        );

        buttons[SWITCH] = switchButton = new Button(
                410, 464, 118, 38,
                textButtonSetup("Switch")
                        .transition(new ButtonTransitions().right(DEPOSIT_WITHDRAW).left(RIGHT_ARROW).down(RETURN))
                        .press(() -> switchClicked = !switchClicked)
        );

        buttons[DEPOSIT_WITHDRAW] = depositWithdrawButton = new Button(
                526, 464, 118, 38,
                // Deposit/Withdraw text set depending on state
                textButtonSetup("")
                        .transition(new ButtonTransitions().right(RELEASE).left(SWITCH).down(RETURN))
                        .press(() -> {
                            if (party) { // Deposit
                                if (depositClicked) {
                                    pc.depositPokemonFromPlayer(selected);
                                }

                                depositClicked = !depositClicked;
                            } else { // Withdraw
                                pc.withdrawPokemon(selected);
                            }
                        })
                        .setup(ButtonPanel::greyInactive)

        );

        buttons[RELEASE] = releaseButton = new Button(
                642, 464, 118, 38,
                textButtonSetup("Release")
                        .transition(new ButtonTransitions().right(0).left(DEPOSIT_WITHDRAW).down(RETURN))
                        .press(() -> {
                            pc.releasePokemon(selected);
                            movedToFront();
                        })
                        .setup(ButtonPanel::greyInactive)
        );

        buttons[RETURN] = new Button(
                410, 522, 350, 38,
                textButtonSetup("Return")
                        .transition(new ButtonTransitions()
                                            .right(0)
                                            .up(SWITCH)
                                            .left(PARTY + Trainer.MAX_POKEMON - 1))
                        .press(ButtonPressAction.getExitAction())
                        .setup(panel -> panel.withBackgroundColor(Color.YELLOW)
                                             .withTransparentCount(2))
        );

        this.buttons = new ButtonList(buttons);
        this.buttons.setSelected(PARTY);

        this.panels = new PanelList(
                boxPanel, boxNamePanel, partyPanel,
                infoPanel, basicInfoPanel, imagePanel,
                movesPanel, statsPanel
        );
    }

    private ButtonInfo textButtonSetup(String text) {
        return new ButtonInfo().setup(panel -> panel.withLabel(text, 20)
                                                    .withTransparentBackground()
                                                    .withBorderPercentage(0)
                                                    .withBlackOutline());
    }

    private ButtonInfo arrowSetup(Direction arrowDirection) {
        int delta = arrowDirection.getDeltaPoint().x;
        return new ButtonInfo()
                .press(() -> {
                    pc.incrementBox(delta);
                    movedToFront();
                })
                .setup(panel -> panel.asArrow(arrowDirection));
    }

    @Override
    public void update(int dt) {
        buttons.update();
        if (buttons.consumeSelectedPress()) {
            updateActiveButtons();
        }

        InputControl.instance().popViewIfEscaped();
    }

    private void setupPokemonButton(ButtonPanel panel, PartyPokemon pokemon) {
        if (pokemon == null) {
            panel.skipDraw();
            return;
        }

        // Draw the pokemon image and outline if selected
        panel.withImageLabel(Game.getData().getPartyTiles().getTile(pokemon.getTinyImageName()))
             .withConditionalOutline(pokemon == selected);
    }

    private void setupDraw() {
        // Box color
        boxPanel.withBackgroundColor(pc.getBoxColor());

        // Box name
        boxNamePanel.withLabel("Box " + (pc.getBoxNum() + 1));

        // Box Pokemon buttons
        PartyPokemon[][] box = pc.getBoxPokemon();
        for (int i = 0; i < PC.BOX_HEIGHT; i++) {
            for (int j = 0; j < PC.BOX_WIDTH; j++) {
                setupPokemonButton(boxButtons[j][i].panel(), box[j][i]);
            }
        }

        // Pokemon party buttons
        List<PartyPokemon> team = Game.getPlayer().getTeam();
        for (int i = 0; i < partyButtons.length; i++) {
            PartyPokemon partyPokemon = i < team.size() ? team.get(i) : null;
            setupPokemonButton(partyButtons[i].panel(), partyPokemon);
        }

        // Highlight if selected
        // Secondary color is the color of the buttons
        Color buttonColor = PokeType.getColors(selected)[1];
        switchButton.panel().withHighlight(switchClicked, buttonColor);
        depositWithdrawButton.panel()
                             .withHighlight(party && depositClicked, buttonColor)
                             .withLabel(party ? "Deposit" : "Withdraw");

        // Pokemon panel type colors
        infoPanel.withBackgroundColors(PokeType.getColors(selected));

        // Pokemon panel image
        imagePanel.withImageLabel(Game.getData().getPokemonTilesSmall().getTile(selected.getImageName()));
    }

    @Override
    public void draw(Graphics g) {
        setupDraw();

        // Background
        BasicPanels.drawCanvasPanel(g);
        this.panels.drawAll(g);

        // Draw page numbers
        TextUtils.drawPageNumbers(g, 16, leftButton, rightButton, pc.getBoxNum(), pc.getNumBoxes());

        // Info panel
        drawSelectedPokemon(g);

        // Draw buttons
        buttons.drawEverything(g);
    }

    private void drawSelectedPokemon(Graphics g) {
        FontMetrics.setFont(g, 20);
        g.setColor(Color.BLACK);
        g.drawString(selected.getActualName() + " " + selected.getGenderString(), 541, 82);

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

            MoveList moves = selected.getActualMoves();
            for (int i = 0; i < moves.size(); i++) {
                int x = i%2 == 0 ? 421 : 590;
                int y = i/2 == 0 ? 238 : 277;

                Attack attack = moves.get(i).getAttack();

                DrawPanel movePanel = new DrawPanel(x, y, 159, 31)
                        .withBackgroundColor(attack.getActualType().getColor())
                        .withTransparentCount(2)
                        .withBorderPercentage(20)
                        .withBlackOutline();

                movePanel.drawBackground(g);
                movePanel.label(g, 16, attack.getName());
            }

            TextUtils.drawRightAlignedString(g, "Stat", 635, 340);
            TextUtils.drawRightAlignedString(g, "IV", 681, 340);
            TextUtils.drawRightAlignedString(g, "EV", 735, 340);

            for (int i = 0; i < Stat.NUM_STATS; i++) {
                FontMetrics.setFont(g, 16);
                g.setColor(selected.getNature().getColor(i));
                g.drawString(Stat.getStat(i, false).getName(), 427, 360 + i*18 + i/2); // TODO: srsly what's with the i/2

                g.setColor(Color.BLACK);
                FontMetrics.setFont(g, 14);

                // TODO: What's up with the + i/2 in the y????
                TextUtils.drawRightAlignedString(g, selected.getStat(i) + "", 635, 360 + i*18 + i/2);
                TextUtils.drawRightAlignedString(g, selected.getIVs().get(i) + "", 681, 360 + i*18 + i/2);
                TextUtils.drawRightAlignedString(g, selected.getEVs().get(i) + "", 735, 360 + i*18 + i/2);
            }
        }
    }

    @Override
    public ViewMode getViewModel() {
        return ViewMode.PC_VIEW;
    }

    @Override
    public void movedToFront() {
        party = true;
        selected = Game.getPlayer().front();
        updateActiveButtons();
    }

    private void updateActiveButtons() {
        PartyPokemon[][] box = pc.getBoxPokemon();
        for (int i = 0; i < PC.BOX_HEIGHT; i++) {
            for (int j = 0; j < PC.BOX_WIDTH; j++) {
                boxButtons[i][j].setActive((party && depositClicked) || switchClicked || box[i][j] != null);
            }
        }

        Player player = Game.getPlayer();
        List<PartyPokemon> team = player.getTeam();

        party = false;
        for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
            partyButtons[i].setActive(i < team.size());
            if (i < team.size() && team.get(i) == selected) {
                party = true;
            }
        }

        if (party) {
            depositWithdrawButton.setActive(player.canDeposit(selected));
        } else {
            depositWithdrawButton.setActive(team.size() < Trainer.MAX_POKEMON);
        }

        releaseButton.setActive(!party || player.canDeposit(selected));
    }
}
