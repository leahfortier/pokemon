package gui.view.battle.handler;

import battle.Battle;
import battle.attack.Move;
import draw.DrawUtils;
import draw.TextUtils;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.button.ButtonList;
import draw.button.ButtonTransitions;
import draw.panel.DrawPanel;
import gui.TileSet;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import main.Game;
import map.Direction;
import pokemon.PartyPokemon;
import pokemon.Stat;
import pokemon.breeding.Eggy;
import trainer.Trainer;
import trainer.TrainerAction;
import trainer.player.Player;
import type.PokeType;
import util.FontMetrics;
import util.PokeString;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.List;

public class PokemonState implements VisualStateHandler {

    // Switch Button in Pokemon View Button Index
    private static final int POKEMON_SWITCH_BUTTON = Trainer.MAX_POKEMON;

    private final DrawPanel pokemonPanel;
    private final DrawPanel basicInformationPanel;
    private final DrawPanel movesPanel;
    private final DrawPanel statsPanel;

    private final DrawPanel expBar;
    private final DrawPanel hpBar;

    private final ButtonList pokemonButtons;
    private final Button[] pokemonTabButtons;
    private final Button pokemonSwitchButton;

    // Shh they're fake don't tell anyone
    private final Button[] fakeMoveButtons;

    // Current selected tab in Pokemon view and whether or not a switch is forced
    private int selectedPokemonTab;
    private boolean switchForced;

    public PokemonState() {
        pokemonPanel = new DrawPanel(30, 224, 357, 346)
                .withTransparentBackground()
                .withBorderPercentage(0)
                .withBlackOutline(EnumSet.complementOf(EnumSet.of(Direction.UP)));

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

        fakeMoveButtons = movesPanel.getButtons(125, 40, Move.MAX_MOVES, 1);

        int switchButtonHeight = 36;
        pokemonSwitchButton = new Button(
                basicInformationPanel.x,
                pokemonPanel.y + pokemonPanel.height - spacing - switchButtonHeight,
                sidePanelWidth,
                switchButtonHeight,
                ButtonHoverAction.BOX,
                new ButtonTransitions().right(POKEMON_SWITCH_BUTTON).up(0).left(POKEMON_SWITCH_BUTTON).down(0)
        );

        int barHeight = 15;
        int statsPanelHeight = 134;
        int statsPanelSpacing = (pokemonSwitchButton.y - (basicInformationPanel.y + basicInformationPanel.height) - statsPanelHeight - barHeight)/2;
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

        // Pokemon Switch View Buttons
        Button[] pokemonButtons = new Button[Trainer.MAX_POKEMON + 1];

        pokemonTabButtons = new Button[Trainer.MAX_POKEMON];
        for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
            pokemonButtons[i] = pokemonTabButtons[i] = Button.createTabButton(
                    i, pokemonPanel.x, pokemonPanel.y, pokemonPanel.width, 34, pokemonTabButtons.length,
                    new ButtonTransitions()
                            .up(POKEMON_SWITCH_BUTTON)
                            .down(POKEMON_SWITCH_BUTTON)
                            .basic(Direction.RIGHT, i, 1, pokemonTabButtons.length)
                            .basic(Direction.LEFT, i, 1, pokemonTabButtons.length)
            );
        }

        pokemonButtons[POKEMON_SWITCH_BUTTON] = pokemonSwitchButton;
        this.pokemonButtons = new ButtonList(pokemonButtons);
    }

    @Override
    public void reset() {
        selectedPokemonTab = 0;
        switchForced = false;
    }

    @Override
    public void set(BattleView view) {
        List<PartyPokemon> list = Game.getPlayer().getTeam();
        for (int i = 0; i < pokemonTabButtons.length; i++) {
            pokemonTabButtons[i].setActive(i < list.size());
        }

        pokemonSwitchButton.setActive(view.isState(VisualState.USE_ITEM) || list.get(selectedPokemonTab).canFight());

        pokemonButtons.setFalseHover();

        for (Button button : fakeMoveButtons) {
            button.setActive(false);
        }
    }

    @Override
    public void draw(BattleView view, Graphics g) {
        // Draw Background
        view.drawLargeMenuPanel(g);

        // Draw Messages Box
        String message = view.getMessage(VisualState.INVALID_POKEMON, "Select a " + PokeString.POKEMON + "!");
        view.drawMenuMessagePanel(g, message);

        // Get current Pokemon
        List<PartyPokemon> list = Game.getPlayer().getTeam();
        PartyPokemon selectedPkm = list.get(selectedPokemonTab);

        // Draw type color polygons
        pokemonPanel.withBackgroundColors(PokeType.getColors(selectedPkm), true);

        if (!selectedPkm.canFight()) {
            pokemonPanel.greyOut();
        }

        pokemonPanel.drawBackground(g);

        // Draw tabs
        TileSet partyTiles = Game.getData().getPartyTiles();
        for (int i = 0; i < list.size(); i++) {
            PartyPokemon pkm = list.get(i);
            Button tabButton = pokemonTabButtons[i];

            // Color tab
            tabButton.fill(g, PokeType.getColors(pkm)[0]);

            // Fade out fainted Pokemon
            if (!pkm.canFight()) {
                tabButton.greyOut(g);
            }

            // Transparenty
            tabButton.fillTransparent(g);

            // Outline in black
            tabButton.outlineTab(g, i, selectedPokemonTab);

            // Draw Pokemon Image
            BufferedImage img = partyTiles.getTile(pkm.getTinyImageName());
            tabButton.imageLabel(g, img);
        }

        // Top Box with basic information
        basicInformationPanel.drawBackground(g);

        // Name and Gender
        FontMetrics.setFont(g, 16);
        g.setColor(Color.BLACK);
        String nameStr = selectedPkm.getActualName() + " " + selectedPkm.getGenderString();
        g.drawString(nameStr, 62, 269);

        if (selectedPkm.isEgg()) {
            // Description
            FontMetrics.setFont(g, 14);
            TextUtils.drawWrappedText(g, ((Eggy)selectedPkm).getEggMessage(), 62, 288, 306);
        } else {
            // Status Condition
            String statusStr = selectedPkm.getStatus().getType().getName();
            g.drawString(statusStr, 179, 269);

            // Level
            String levelStr = "Lv" + selectedPkm.getLevel();
            g.drawString(levelStr, 220, 269);

            // Draw type tiles
            // TODO: Use ImageUtils.drawTypeTiles()
            PokeType type = selectedPkm.getActualType();
            if (type.isSingleTyped()) {
                g.drawImage(type.getFirstType().getImage(), 322, 255, null);
            } else {
                g.drawImage(type.getFirstType().getImage(), 285, 255, null);
                g.drawImage(type.getSecondType().getImage(), 322, 255, null);
            }

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

            // HP Bar
            hpBar.fillBar(g, selectedPkm.getHPColor(), selectedPkm.getHPRatio());

            // Write stat names and values
            statsPanel.drawBackground(g);
            FontMetrics.setFont(g, 16);
            for (int i = 0; i < Stat.NUM_STATS; i++) {
                g.setColor(selectedPkm.getNature().getColor(i));
                g.drawString(Stat.getStat(i, false).getShortName(), 62, 21*i + 372);

                g.setColor(Color.BLACK);
                int statVal = selectedPkm.getStat(i);
                String valStr = i == Stat.HP.index() ? selectedPkm.getHP() + "/" + statVal : "" + statVal;
                TextUtils.drawRightAlignedString(g, valStr, 188, 21*i + 372);
            }

            // Draw Move List
            movesPanel.drawBackground(g);
            List<Move> movesList = selectedPkm.getActualMoves();
            for (int i = 0; i < movesList.size(); i++) {
                Button moveButton = fakeMoveButtons[i];
                Move move = movesList.get(i);

                moveButton.fillBordered(g, move.getAttack().getActualType().getColor());
                moveButton.blackOutline(g);

                int dx = moveButton.x;
                int dy = moveButton.y;

                g.translate(dx, dy);

                // Draw attack name
                g.setColor(Color.BLACK);
                g.drawString(move.getAttack().getName(), 7, 17);

                // Draw PP amount
                TextUtils.drawRightAlignedString(g, "PP: " + move.getPP() + "/" + move.getMaxPP(), 118, 33);

                BufferedImage categoryImage = move.getAttack().getCategory().getImage();
                g.drawImage(categoryImage, 7, 21, null);

                g.translate(-dx, -dy);
            }
        }

        // Switch pokemon button
        pokemonSwitchButton.fillTransparent(g);
        pokemonSwitchButton.blackOutline(g);
        pokemonSwitchButton.label(g, 20, view.isState(VisualState.USE_ITEM) ? "Use!" : "Switch!");

        if (!pokemonSwitchButton.isActive()) {
            pokemonSwitchButton.greyOut(g);
        }

        pokemonButtons.draw(g);

        // Draw back arrow when applicable
        view.drawBackButton(g, !switchForced);
    }

    @Override
    public void update(BattleView view) {
        // Update the buttons
        pokemonButtons.update();

        Battle currentBattle = view.getCurrentBattle();
        Player player = Game.getPlayer();
        List<PartyPokemon> list = player.getTeam();
        for (int i = 0; i < list.size(); i++) {
            if (pokemonTabButtons[i].checkConsumePress()) {
                selectedPokemonTab = i;
                view.setVisualState(); //to update active buttons
            }
        }

        // Switch Switch Switcheroo
        if (pokemonSwitchButton.checkConsumePress()) {
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
        return this.pokemonButtons;
    }
}
