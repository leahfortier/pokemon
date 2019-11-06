package gui.view.battle.handler;

import battle.Battle;
import battle.attack.Attack;
import battle.attack.Move;
import draw.DrawUtils;
import draw.ImageUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonList;
import draw.button.ButtonPanel;
import draw.button.ButtonTransitions;
import draw.layout.ButtonLayout;
import draw.layout.TabLayout;
import draw.panel.DrawPanel;
import draw.panel.VerticalMovePanel;
import draw.panel.WrapPanel.WrapMetrics;
import gui.TileSet;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import main.Game;
import pokemon.active.MoveList;
import pokemon.active.PartyPokemon;
import pokemon.breeding.Eggy;
import pokemon.stat.Stat;
import trainer.Trainer;
import trainer.TrainerAction;
import trainer.player.Player;
import type.PokeType;
import util.FontMetrics;
import util.string.PokeString;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;

public class PokemonState implements VisualStateHandler {
    private static final int NUM_BUTTONS = Trainer.MAX_POKEMON + MoveList.MAX_MOVES + 1;
    private static final int TABS = 0;
    private static final int MOVES = TABS + Trainer.MAX_POKEMON;
    private static final int SWITCH = NUM_BUTTONS - 1;

    private final DrawPanel pokemonPanel;
    private final DrawPanel basicInformationPanel;
    private final DrawPanel statsPanel;
    private final VerticalMovePanel moveDetailsPanel;
    private final DrawPanel movesPanel;

    private final DrawPanel expBar;
    private final DrawPanel hpBar;

    private final ButtonList buttons;
    private final Button[] tabButtons;
    private final Button[] moveButtons;
    private final Button switchButton;

    // Current selected tab in Pokemon view and whether or not a switch is forced
    private int selectedPokemonTab;
    private boolean switchForced;

    public PokemonState() {
        pokemonPanel = new DrawPanel(30, 224, 357, 346)
                .withTransparentBackground()
                .withBorderPercentage(0)
                .withBlackOutline();

        int sidePanelWidth = 141;
        int spacing = (pokemonPanel.width - 2*sidePanelWidth)/3;

        basicInformationPanel = new DrawPanel(pokemonPanel.x + spacing, pokemonPanel.y + spacing, pokemonPanel.width - 2*spacing, 64)
                .withFullTransparency()
                .withBlackOutline();

        int movesPanelHeight = 193;
        movesPanel = new DrawPanel(
                pokemonPanel.rightX() - spacing - sidePanelWidth,
                pokemonPanel.bottomY() - spacing - movesPanelHeight,
                sidePanelWidth,
                movesPanelHeight
        )
                .withFullTransparency()
                .withBlackOutline();

        moveButtons = new ButtonLayout(movesPanel, MoveList.MAX_MOVES, 1, 8)
                .withStartIndex(MOVES)
                .withDefaultTransitions(new ButtonTransitions().right(SWITCH).up(TABS).left(SWITCH).down(TABS))
                .withButtonSetup(panel -> panel.asMovePanel(14, 12)
                                               .withTransparentCount(2)
                                               .withBorderPercentage(15)
                                               .withBlackOutline())
                .getButtons();

        int switchButtonHeight = 36;
        switchButton = new Button(
                basicInformationPanel.x,
                pokemonPanel.y + pokemonPanel.height - spacing - switchButtonHeight,
                sidePanelWidth,
                switchButtonHeight,
                new ButtonTransitions().right(MOVES).up(TABS).left(MOVES).down(TABS),
                () -> {}, // Handled in update
                panel -> panel.greyInactive()
                              .withLabelSize(20)
                              .withBorderlessTransparentBackground()
                              .withBlackOutline()
        );

        int barHeight = 15;
        int statsPanelHeight = 134;
        int statsPanelSpacing = (switchButton.y - (basicInformationPanel.y + basicInformationPanel.height) - statsPanelHeight - barHeight)/2;
        statsPanel = new DrawPanel(
                basicInformationPanel.x,
                basicInformationPanel.y + basicInformationPanel.height + statsPanelSpacing + barHeight,
                sidePanelWidth,
                statsPanelHeight
        )
                .withFullTransparency()
                .withBlackOutline();

        expBar = new DrawPanel(
                basicInformationPanel.rightX() - sidePanelWidth,
                basicInformationPanel.bottomY() - DrawUtils.OUTLINE_SIZE,
                sidePanelWidth,
                barHeight
        )
                .withBlackOutline();

        hpBar = new DrawPanel(statsPanel.x, statsPanel.y - barHeight + DrawUtils.OUTLINE_SIZE, statsPanel.width, barHeight)
                .withBlackOutline();

        // HP Bar and stats panel
        moveDetailsPanel = new VerticalMovePanel(
                statsPanel.x,
                hpBar.y,
                statsPanel.width,
                statsPanel.bottomY() - hpBar.y,
                15, 13, 11
        )
                .withMinDescFontSize(9)
                .withFullTransparency();

        tabButtons = new TabLayout(pokemonPanel, Trainer.MAX_POKEMON, 34)
                .withStartIndex(TABS)
                .withDefaultTransitions(new ButtonTransitions().down(SWITCH).up(SWITCH))
                .withButtonSetup(panel -> panel.skipInactive()
                                               .withBorderlessTransparentBackground())
                .getTabs();

        // Pokemon Switch View Buttons
        this.buttons = new ButtonList(NUM_BUTTONS);
        buttons.set(TABS, tabButtons);
        buttons.set(MOVES, moveButtons);
        buttons.set(SWITCH, switchButton);
    }

    @Override
    public void reset() {
        selectedPokemonTab = 0;
        switchForced = false;
    }

    @Override
    public void set(BattleView view) {
        List<PartyPokemon> team = Game.getPlayer().getTeam();
        PartyPokemon selected = team.get(selectedPokemonTab);

        for (int i = 0; i < tabButtons.length; i++) {
            tabButtons[i].setActive(i < team.size());
        }

        int numMoves = selected.getActualMoves().size();
        for (int i = 0; i < moveButtons.length; i++) {
            moveButtons[i].setActive(!selected.isEgg() && i < numMoves);
        }

        switchButton.setActive(view.isState(VisualState.USE_ITEM) || selected.canFight());

        buttons.setFalseHover();
    }

    @Override
    public void draw(BattleView view, Graphics g) {
        // Draw Background
        view.drawLargeMenuPanel(g);

        // Draw Messages Box
        String message = view.getMessage(VisualState.INVALID_POKEMON, "Select a " + PokeString.POKEMON + "!");
        view.drawMenuMessagePanel(g, message);

        // Draw back arrow when applicable
        view.drawBackButton(g, !switchForced);

        // Selected Pokemon Info
        // Note: Important to draw selected before the tabs because of how outlines work
        drawSelectedPokemon(g, view);

        // Draw tabs
        List<PartyPokemon> list = Game.getPlayer().getTeam();
        TileSet partyTiles = Game.getData().getPartyTiles();
        for (int i = 0; i < list.size(); i++) {
            PartyPokemon pkm = list.get(i);
            ButtonPanel tabPanel = tabButtons[i].panel();
            tabPanel.withTabOutlines(i, selectedPokemonTab)
                    .withBackgroundColor(PokeType.getColors(pkm)[0])
                    .withImageLabel(partyTiles.getTile(pkm.getTinyImageName()))
                    .draw(g);

            // Fade out fainted Pokemon
            tabPanel.faintOut(g, list.get(i));
        }

        // Hover action
        buttons.drawHover(g);
    }

    private void drawSelectedPokemon(Graphics g, BattleView view) {
        PartyPokemon selectedPkm = Game.getPlayer().getTeam().get(selectedPokemonTab);

        // Type color polygons
        pokemonPanel.withBackgroundColors(PokeType.getColors(selectedPkm), true)
                    .drawBackground(g);

        // Top Box with basic information
        basicInformationPanel.drawBackground(g);

        // Name and Gender
        FontMetrics.setBlackFont(g, 16);
        g.drawString(selectedPkm.getNameAndGender(), 62, 269);

        if (selectedPkm.isEgg()) {
            // Description
            FontMetrics.setFont(g, 14);
            TextUtils.drawWrappedText(g, ((Eggy)selectedPkm).getEggMessage(), 62, 288, 306);
        } else {
            // Status Condition
            String statusStr = selectedPkm.getStatus().getShortName();
            g.drawString(statusStr, 179, 269);

            // Level
            String levelStr = "Lv" + selectedPkm.getLevel();
            g.drawString(levelStr, 220, 269);

            // Draw type tiles
            PokeType type = selectedPkm.getActualType();
            ImageUtils.drawTypeTiles(g, type, 352, 269);

            // Ability
            FontMetrics.setFont(g, 14);
            g.drawString(selectedPkm.getActualAbility().getName(), 62, 288);

            // Held Item
            g.drawString(selectedPkm.getActualHeldItem().getName(), 62, 307);

            // Experience
            g.drawString("EXP", 220, 288);
            TextUtils.drawRightAlignedString(g, "" + selectedPkm.getTotalEXP(), 352, 288);

            g.drawString("To Next Lv", 220, 307);
            TextUtils.drawRightAlignedString(g, "" + selectedPkm.expToNextLevel(), 352, 307);

            // Experience Bar
            float expRatio = selectedPkm.expRatio();
            expBar.fillBar(g, DrawUtils.EXP_BAR_COLOR, expRatio);

            MoveList moves = selectedPkm.getActualMoves();
            int selectedButton = buttons.getSelected();
            if (selectedButton >= MOVES && selectedButton < MOVES + MoveList.MAX_MOVES) {
                drawMoveDetails(g, moves.get(selectedButton - MOVES).getAttack());
            } else {
                drawStatBox(g, selectedPkm);
            }

            // Draw Move List
            movesPanel.drawBackground(g);
            for (int i = 0; i < moves.size(); i++) {
                Move move = moves.get(i);
                ButtonPanel movePanel = this.moveButtons[i].panel();

                movePanel.withMove(move).draw(g);
            }
        }

        // Draw switch button
        switchButton.panel()
                    .withLabel(view.isState(VisualState.USE_ITEM) ? "Use!" : "Switch!")
                    .draw(g);

        // If ya dead ya red
        pokemonPanel.faintOut(g, selectedPkm);
    }

    public WrapMetrics drawMoveDetails(Graphics g, Attack move) {
        return moveDetailsPanel.draw(g, move);
    }

    private void drawStatBox(Graphics g, PartyPokemon selectedPkm) {
        // HP Bar
        hpBar.fillBar(g, selectedPkm.getHPColor(), selectedPkm.getHPRatio());

        // Write stat names and values
        FontMetrics.setFont(g, 16);
        statsPanel.drawBackground(g);
        for (int i = 0; i < Stat.NUM_STATS; i++) {
            g.setColor(selectedPkm.getNature().getColor(i));
            g.drawString(Stat.getStat(i, false).getShortName(), 62, 21*i + 372);

            g.setColor(Color.BLACK);
            int statVal = selectedPkm.getStat(i);
            String valStr = i == Stat.HP.index() ? selectedPkm.getHP() + "/" + statVal : "" + statVal;
            TextUtils.drawRightAlignedString(g, valStr, 188, 21*i + 372);
        }
    }

    @Override
    public void update(BattleView view) {
        // Update the buttons
        buttons.update();

        Battle currentBattle = view.getCurrentBattle();
        Player player = Game.getPlayer();
        List<PartyPokemon> list = player.getTeam();
        for (int i = 0; i < list.size(); i++) {
            if (tabButtons[i].checkConsumePress()) {
                selectedPokemonTab = i;
                view.setVisualState(); // To update active buttons
            }
        }

        // Switch Switch Switcheroo
        if (switchButton.checkConsumePress()) {
            PartyPokemon selectedPkm = list.get(selectedPokemonTab);

            // Use an item on this Pokemon instead of switching
            if (view.isState(VisualState.USE_ITEM)) {
                // Valid item
                if (player.getBag().battleUseItem(VisualState.getSelectedItem(), selectedPkm, currentBattle)) {
                    player.performAction(currentBattle, TrainerAction.ITEM);
                    view.setVisualState(VisualState.MENU);
                    view.cycleMessage();
                }
                // Invalid item
                else {
                    view.cycleMessage();
                    view.setVisualState(VisualState.INVALID_BAG);
                }
            }
            // Actual switcheroo
            else {
                if (player.canSwitch(currentBattle, selectedPokemonTab)) {
                    player.setSwitchIndex(selectedPokemonTab);

                    if (switchForced) {
                        player.performSwitch(currentBattle);
                    } else {
                        player.performAction(currentBattle, TrainerAction.SWITCH);
                    }

                    view.cycleMessage();
                    switchForced = false;
                    VisualState.resetLastMoveUsed();
                } else {
                    view.cycleMessage();
                    view.setVisualState(VisualState.INVALID_POKEMON);
                }
            }
        }

        // Return to main menu if applicable
        view.updateBackButton(!switchForced);
    }

    public void setSwitchForced() {
        this.switchForced = true;
    }

    @Override
    public ButtonList getButtons() {
        return this.buttons;
    }
}
