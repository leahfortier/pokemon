package gui.view.battle.handler;

import battle.Battle;
import battle.attack.Move;
import gui.Button;
import gui.ButtonHoverAction;
import gui.TileSet;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import main.Game;
import main.Type;
import pokemon.ActivePokemon;
import pokemon.Stat;
import trainer.CharacterData;
import trainer.Trainer;
import trainer.Trainer.Action;
import util.DrawUtils;
import util.FontMetrics;
import util.PokeString;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

public class PokemonState implements VisualStateHandler {

    // Switch Button in Pokemon View Button Index
    private static final int POKEMON_SWITCH_BUTTON = Trainer.MAX_POKEMON;

    // Polygons for Type Colors in the Pokemon View
    private static final int[] pkmnPrimaryColorx = { 0, 349, 5, 0 };
    private static final int[] pkmnPrimaryColory = { 0, 0, 344, 344 };
    private static final int[] pkmnSecondaryColorx = { 344, 349, 349, 0 };
    private static final int[] pkmnSecondaryColory = { 0, 0, 344, 344 };

    private final Button[] pokemonButtons;
    private final Button[] pokemonTabButtons;
    private final Button pokemonSwitchButton;

    // Current selected tab in Pokemon view and whether or not a switch is forced
    private int selectedPokemonTab;
    private boolean switchForced;

    public PokemonState() {
        // Pokemon Switch View Buttons
        pokemonButtons = new Button[Trainer.MAX_POKEMON + 1];

        pokemonTabButtons = new Button[Trainer.MAX_POKEMON];
        for (int i = 0; i < Trainer.MAX_POKEMON; i++) {
            pokemonButtons[i] = pokemonTabButtons[i] = new Button(32 + i*59, 192, 59, 34, ButtonHoverAction.BOX,
                    new int[] {
                            (i + 1)%Trainer.MAX_POKEMON, // Right
                            POKEMON_SWITCH_BUTTON, // Up
                            (i - 1 + Trainer.MAX_POKEMON)%Trainer.MAX_POKEMON, // Left
                            POKEMON_SWITCH_BUTTON // Down
                    }
            );
        }

        pokemonButtons[POKEMON_SWITCH_BUTTON] = pokemonSwitchButton = new Button(55, 509, 141, 36, ButtonHoverAction.BOX, new int[] { -1, 0, -1, -1 });
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

        if (view.isState(VisualState.USE_ITEM)) {
            pokemonSwitchButton.setActive(list.get(selectedPokemonTab).canFight());
        }

        for (Button button: pokemonButtons) {
            button.setForceHover(false);
        }
    }

    @Override
    public void draw(BattleView view, Graphics g, TileSet tiles) {
        // Draw Background
        g.drawImage(tiles.getTile(0x10), 0, 160, null);

        // Get current Pokemon
        List<ActivePokemon> list = Game.getPlayer().getTeam();
        ActivePokemon selectedPkm = list.get(selectedPokemonTab);

        // Draw type color polygons
        Type[] type = selectedPkm.getActualType();
        Color[] typeColors = Type.getColors(selectedPkm);

        g.translate(31, 224);
        g.setColor(typeColors[0]);
        g.fillPolygon(pkmnPrimaryColorx, pkmnPrimaryColory, 4);
        g.translate(-31, -224);

        g.translate(36, 224);
        g.setColor(typeColors[1]);
        g.fillPolygon(pkmnSecondaryColorx, pkmnSecondaryColory, 4);
        g.translate(-36, -224);

        // Draw Messages Box
        g.drawImage(tiles.getTile(0x20), 415, 440, null);

        // Draw Box Outlines for Pokemon Info
        if (!selectedPkm.canFight()) { // Fainted Pokemon and Eggs
            g.drawImage(tiles.getTile(0x35), 30, 224, null);
            g.drawImage(tiles.getTile(0x31), 55, 249, null);
        }
        else {
            g.drawImage(tiles.getTile(0x34), 30, 224, null);
            g.drawImage(tiles.getTile(0x30), 55, 249, null);
        }

        if (selectedPkm.isEgg()) {
            // Name
            FontMetrics.setFont(g, 16);
            g.setColor(Color.BLACK);
            String nameStr = selectedPkm.getActualName();
            g.drawString(nameStr, 62, 269);

            // Description
            FontMetrics.setFont(g, 14);
            DrawUtils.drawWrappedText(g, selectedPkm.getEggMessage(), 62, 288, 306);
        }
        else {
            // Name and Gender
            FontMetrics.setFont(g, 16);
            g.setColor(Color.BLACK);
            String nameStr = selectedPkm.getActualName() + " " + selectedPkm.getGender().getCharacter();
            g.drawString(nameStr, 62, 269);

            // Status Condition
            String statusStr = selectedPkm.getStatus().getType().getName();
            g.drawString(statusStr, 179, 269);

            // Level
            String levelStr = "Lv" + selectedPkm.getLevel();
            g.drawString(levelStr, 220, 269);

            // Draw type tiles
            if (type[1] == Type.NO_TYPE) {
                g.drawImage(tiles.getTile(type[0].getImageIndex()), 322, 255, null);
            }
            else {
                g.drawImage(tiles.getTile(type[0].getImageIndex()), 285, 255, null);
                g.drawImage(tiles.getTile(type[1].getImageIndex()), 322, 255, null);
            }

            // Ability
            FontMetrics.setFont(g, 14);
            g.drawString(selectedPkm.getActualAbility().getName(), 62, 288);

            // Experience
            g.drawString("EXP", 220, 288);
            DrawUtils.drawRightAlignedString(g, "" + selectedPkm.getTotalEXP(), 352, 288);

            g.drawString(selectedPkm.getActualHeldItem().getName(), 62, 307);

            g.drawString("To Next Lv", 220, 307);
            DrawUtils.drawRightAlignedString(g, "" + selectedPkm.expToNextLevel(), 352, 307);

            // Experience Bar
            float expRatio = selectedPkm.expRatio();
            g.setColor(DrawUtils.EXP_BAR_COLOR);
            g.fillRect(222, 315, (int)(137*expRatio), 10);

            // HP Bar
            g.setColor(selectedPkm.getHPColor());
            g.fillRect(57, 341, (int)(137*selectedPkm.getHPRatio()), 10);

            // Write stat names
            FontMetrics.setFont(g, 16);
            for (int i = 0; i < Stat.NUM_STATS; i++) {
                g.setColor(selectedPkm.getNature().getColor(i));
                g.drawString(Stat.getStat(i, false).getShortName(), 62, 21*i + 372);
            }

            // Write stat values
            g.setColor(Color.BLACK);

            int[] statsVal = selectedPkm.getStats();
            for (int i = 0; i < Stat.NUM_STATS; i++) {
                String valStr = i == Stat.HP.index() ? selectedPkm.getHP() + "/" + statsVal[i] : "" + statsVal[i];
                DrawUtils.drawRightAlignedString(g, valStr, 188, 21*i + 372);
            }

            // Draw Move List
            List<Move> movesList = selectedPkm.getActualMoves();
            for (int i = 0; i < movesList.size(); i++) {
                int dx = 228, dy = 359 + i*46;
                g.translate(dx, dy);

                // Draw Color background
                Move move = movesList.get(i);
                g.setColor(move.getAttack().getActualType().getColor());
                g.fillRect(0, 0, 125, 40);
                g.drawImage(tiles.getTile(0x32), 0, 0, null);

                // Draw attack name
                g.setColor(Color.BLACK);
                g.drawString(move.getAttack().getName(), 7, 17);

                // Draw PP amount
                DrawUtils.drawRightAlignedString(g, "PP: " + move.getPP() + "/" + move.getMaxPP(), 118, 33);

                BufferedImage categoryImage = tiles.getTile(move.getAttack().getCategory().getImageNumber());
                g.drawImage(categoryImage, 7, 21, null);

                g.translate(-dx, -dy);
            }
        }

        // Draw Switch/Use text
        FontMetrics.setFont(g, 20);
        if (view.isState(VisualState.USE_ITEM)) {
            g.drawString("Use!", 103, 533);
        }
        else {
            g.drawString("Switch!", 93, 533);
        }

        // Draw tabs
        TileSet partyTiles = Game.getData().getPartyTiles();
        for (int i = 0; i < list.size(); i++) {
            ActivePokemon pkm = list.get(i);

            // Draw tab
            if(pkm.isEgg()) {
                g.setColor(Type.getColors(selectedPkm)[0]);
            }
            else {
                g.setColor(pkm.getActualType()[0].getColor());
            }

            g.fillRect(32 + i*59, 192, 59, 34);
            if (i == selectedPokemonTab) {
                g.drawImage(tiles.getTile(0x36), 30 + i*59, 190, null);
            }
            else {
                g.drawImage(tiles.getTile(0x33), 30 + i*59, 190, null);
            }

            // Draw Pokemon Image
            BufferedImage img = partyTiles.getTile(pkm.getTinyImageIndex());
            DrawUtils.drawCenteredImage(g, img, 60 + i*59, 205);

            // Fade out fainted Pokemon
            if (!pkm.canFight()) {
                g.setColor(new Color(0, 0, 0, 128));
                g.fillRect(32 + i*59, 192, 59, 34);
            }
        }

        // Draw Messages
        g.setColor(Color.BLACK);
        FontMetrics.setFont(g, 30);
        String msgLine = view.getMessage(VisualState.INVALID_POKEMON, "Select a " + PokeString.POKEMON + "!");
        DrawUtils.drawWrappedText(g, msgLine, 440, 485, 350);

        // Draw back arrow when applicable
        view.drawBackButton(g, !switchForced);

        for (int i = 0; i < list.size(); i++) {
            pokemonTabButtons[i].draw(g);
        }

        if (view.isState(VisualState.USE_ITEM) || selectedPkm.canFight()) {
            pokemonSwitchButton.draw(g);
        }
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

                    if (switchForced) {
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
