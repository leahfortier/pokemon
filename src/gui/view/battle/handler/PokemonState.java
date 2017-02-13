package gui.view.battle.handler;

import battle.Battle;
import battle.attack.Move;
import gui.TileSet;
import draw.button.Button;
import draw.button.ButtonHoverAction;
import draw.button.panel.DrawPanel;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import main.Game;
import map.Direction;
import pokemon.ActivePokemon;
import pokemon.Stat;
import trainer.CharacterData;
import trainer.Trainer;
import trainer.Trainer.Action;
import type.Type;
import draw.DrawUtils;
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

    private final Button[] pokemonButtons;
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
                pokemonPanel.x + pokemonPanel.width - spacing - sidePanelWidth,
                pokemonPanel.y + pokemonPanel.height - spacing - movesPanelHeight,
                sidePanelWidth,
                movesPanelHeight)
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
                new int[] { POKEMON_SWITCH_BUTTON, 0, POKEMON_SWITCH_BUTTON, 0 }
        );

        int barHeight = 15;
        int statsPanelHeight = 134;
        int statsPanelSpacing = (pokemonSwitchButton.y - (basicInformationPanel.y + basicInformationPanel.height) - statsPanelHeight - barHeight)/2;
        statsPanel = new DrawPanel(
                basicInformationPanel.x,
                basicInformationPanel.y + basicInformationPanel.height + statsPanelSpacing + barHeight,
                sidePanelWidth,
                statsPanelHeight)
                .withFullTransparency()
                .withBlackOutline();

        expBar = new DrawPanel(
                basicInformationPanel.x + basicInformationPanel.width - sidePanelWidth,
                basicInformationPanel.y + basicInformationPanel.height - DrawUtils.OUTLINE_SIZE,
                sidePanelWidth,
                barHeight)
                .withBlackOutline();

        hpBar = new DrawPanel(statsPanel.x, statsPanel.y - barHeight + DrawUtils.OUTLINE_SIZE, statsPanel.width, barHeight)
                .withBlackOutline();

        // Pokemon Switch View Buttons
        pokemonButtons = new Button[Trainer.MAX_POKEMON + 1];

        pokemonTabButtons = new Button[Trainer.MAX_POKEMON];
        for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
            pokemonButtons[i] = pokemonTabButtons[i] = Button.createTabButton(
                    i, pokemonPanel.x, pokemonPanel.y, pokemonPanel.width, 34, pokemonTabButtons.length,
                    new int[] {
                            Button.basicTransition(i, 1, pokemonTabButtons.length, Direction.RIGHT),
                            POKEMON_SWITCH_BUTTON, // Up
                            Button.basicTransition(i, 1, pokemonTabButtons.length, Direction.LEFT),
                            POKEMON_SWITCH_BUTTON // Down
                    });
        }

        pokemonButtons[POKEMON_SWITCH_BUTTON] = pokemonSwitchButton;
    }

    @Override
    public void reset() {
        selectedPokemonTab = 0;
        switchForced = false;
    }

    @Override
    public void set(BattleView view) {
        List<ActivePokemon> list = Game.getPlayer().getTeam();
        for (int i = 0; i < pokemonTabButtons.length; i++) {
            pokemonTabButtons[i].setActive(i < list.size());
        }

        pokemonSwitchButton.setActive(view.isState(VisualState.USE_ITEM) || list.get(selectedPokemonTab).canFight());

        for (Button button: pokemonButtons) {
            button.setForceHover(false);
        }

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
        List<ActivePokemon> list = Game.getPlayer().getTeam();
        ActivePokemon selectedPkm = list.get(selectedPokemonTab);

        // Draw type color polygons
        pokemonPanel.withBackgroundColors(Type.getColors(selectedPkm));

        if (!selectedPkm.canFight()) {
            pokemonPanel.greyOut();
        }

        pokemonPanel.drawBackground(g);

        // Draw tabs
        TileSet partyTiles = Game.getData().getPartyTiles();
        for (int i = 0; i < list.size(); i++) {
            ActivePokemon pkm = list.get(i);
            Button tabButton = pokemonTabButtons[i];

            // Color tab
            tabButton.fill(g, Type.getColors(pkm)[0]);

            // Fade out fainted Pokemon
            if (!pkm.canFight()) {
                tabButton.greyOut(g);
            }

            // Transparenty
            tabButton.fillTransparent(g);

            // Outline in black
            tabButton.outlineTab(g, i, selectedPokemonTab);

            // Draw Pokemon Image
            BufferedImage img = partyTiles.getTile(pkm.getTinyImageIndex());
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
            DrawUtils.drawWrappedText(g, selectedPkm.getEggMessage(), 62, 288, 306);
        }
        else {
            // Status Condition
            String statusStr = selectedPkm.getStatus().getType().getName();
            g.drawString(statusStr, 179, 269);

            // Level
            String levelStr = "Lv" + selectedPkm.getLevel();
            g.drawString(levelStr, 220, 269);

            // Draw type tiles
            Type[] type = selectedPkm.getActualType();
            if (type[1] == Type.NO_TYPE) {
                g.drawImage(type[0].getImage(), 322, 255, null);
            }
            else {
                g.drawImage(type[0].getImage(), 285, 255, null);
                g.drawImage(type[1].getImage(), 322, 255, null);
            }

            // Ability
            FontMetrics.setFont(g, 14);
            g.drawString(selectedPkm.getActualAbility().getName(), 62, 288);

            // Held Item
            g.drawString(selectedPkm.getActualHeldItem().getName(), 62, 307);

            // Experience
            g.drawString("EXP", 220, 288);
            DrawUtils.drawRightAlignedString(g, "" + selectedPkm.getTotalEXP(), 352, 288);

            g.drawString("To Next Lv", 220, 307);
            DrawUtils.drawRightAlignedString(g, "" + selectedPkm.expToNextLevel(), 352, 307);

            // Experience Bar
            float expRatio = selectedPkm.expRatio();
            expBar.fillBar(g, DrawUtils.EXP_BAR_COLOR, expRatio);

            // HP Bar
            hpBar.fillBar(g, selectedPkm.getHPColor(), selectedPkm.getHPRatio());

            // Write stat names and values
            statsPanel.drawBackground(g);
            FontMetrics.setFont(g, 16);
            int[] statsVal = selectedPkm.getStats();
            for (int i = 0; i < Stat.NUM_STATS; i++) {
                g.setColor(selectedPkm.getNature().getColor(i));
                g.drawString(Stat.getStat(i, false).getShortName(), 62, 21*i + 372);

                g.setColor(Color.BLACK);
                String valStr = i == Stat.HP.index() ? selectedPkm.getHP() + "/" + statsVal[i] : "" + statsVal[i];
                DrawUtils.drawRightAlignedString(g, valStr, 188, 21*i + 372);
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
                DrawUtils.drawRightAlignedString(g, "PP: " + move.getPP() + "/" + move.getMaxPP(), 118, 33);

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

        for (Button button : pokemonButtons) {
            button.draw(g);
        }

        // Draw back arrow when applicable
        view.drawBackButton(g, !switchForced);
    }

    @Override
    public void update(BattleView view) {
        // Update the buttons
        view.setSelectedButton(pokemonButtons);

        Battle currentBattle = view.getCurrentBattle();
        CharacterData player = Game.getPlayer();
        List<ActivePokemon> list = player.getTeam();
        for (int i = 0; i < list.size(); i++) {
            if (pokemonTabButtons[i].checkConsumePress()) {
                selectedPokemonTab = i;
                view.setVisualState(); //to update active buttons
            }
        }

        // Switch Switch Switcheroo
        if (pokemonSwitchButton.checkConsumePress()) {
            ActivePokemon selectedPkm = list.get(selectedPokemonTab);

            // Use an item on this Pokemon instead of switching
            if (view.isState(VisualState.USE_ITEM)) {
                // Valid item
                if (player.getBag().battleUseItem(VisualState.getSelectedItem(), selectedPkm, currentBattle)) {
                    player.performAction(currentBattle, Action.ITEM);
                    view.setVisualState(VisualState.MENU);
                    view.cycleMessage(false);
                }
                // Invalid item
                else {
                    view.cycleMessage(false);
                    view.setVisualState(VisualState.INVALID_BAG);
                }
            }
            // Actual switcheroo
            else {
                if (player.canSwitch(currentBattle, selectedPokemonTab)) {
                    player.setFront(selectedPokemonTab);
                    currentBattle.enterBattle(player.front());

                    if (!switchForced) {
                        player.performAction(currentBattle, Action.SWITCH);
                    }

                    view.cycleMessage(false);
                    switchForced = false;
                    VisualState.resetLastMoveUsed();
                }
                else {
                    view.cycleMessage(false);
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
}
