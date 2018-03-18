package gui;

import battle.attack.AttackNamesies;
import input.ControlKey;
import input.InputControl;
import item.ItemNamesies;
import main.Game;
import main.Global;
import map.area.AreaData;
import map.overworld.OverworldTool;
import pattern.PokemonMatcher;
import pokemon.active.MoveList;
import pokemon.active.PartyPokemon;
import pokemon.species.PokemonNamesies;
import test.maps.TestMap;
import trainer.player.Player;
import util.FontMetrics;
import util.file.FileIO;
import util.file.Folder;

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class DevConsole {
    private int key;
    private String currText;
    private boolean show;

    DevConsole() {
        key = InputControl.INVALID_LOCK;
        currText = "";
        show = false;
    }

    // Try to initialize the console, but if you can't just don't do anything
    boolean init() {
        key = InputControl.instance().getLock();
        if (key == InputControl.INVALID_LOCK) {
            return false;
        }

        currText = "";
        show = true;
        return true;
    }

    public void update() {
        if (key == InputControl.INVALID_LOCK) {
            return; // Shouldn't even ever be here!
        }

        InputControl input = InputControl.instance();
        if (!input.isCapturingText()) {
            input.startTextCapture();
        }

        if (input.isCapturingText()) {
            currText = input.getCapturedText();
        }

        if (input.consumeIfDown(ControlKey.ENTER, key)) {
            execute(input.stopTextCapture());
        }

        if (input.consumeIfDown(ControlKey.ESC, key)) {
            tearDown();
        }
    }

    // Tear down, release locks, etc... This needs to be the only way to get out of here, or bad things can happen!
    private void tearDown() {
        InputControl input = InputControl.instance();

        show = false;
        input.stopTextCapture();
        input.releaseLock(key);
        currText = "";
    }

    boolean isShown() {
        return show;
    }

    public void draw(Graphics g) {
        if (!isShown()) {
            return; // Fixes a minor graphical stutter when tearing down
        }

        g.translate(0, Global.GAME_SIZE.height - 20);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, Global.GAME_SIZE.width, 20);

        g.setColor(Color.DARK_GRAY);
        g.drawLine(0, 0, Global.GAME_SIZE.width, 0);

        g.setColor(Color.WHITE);
        FontMetrics.setFont(g, 14);
        g.drawString(currText, 2, 16);

        g.translate(0, -Global.GAME_SIZE.height + 20);
    }

    private void execute(String command) {
        Scanner in = new Scanner(command);
        in.useDelimiter("\\s+");

        if (!in.hasNext()) {
            in.close();
            return;
        }

        String curr = in.next();
        switch (curr.toLowerCase()) {
            case "give":
                give(in);
                break;
            case "global":
                global(in);
                break;
            default:
                Global.info("Invalid command " + curr);
                break;
        }

        if (in.hasNext()) {
            Global.info("Unused end of command " + in.nextLine());
        }

        in.close();
    }

    private void global(Scanner in) {
        if (!in.hasNext()) {
            Global.info("Add what global????");
            return;
        }

        Player player = Game.getPlayer();
        if (player == null) {
            Global.info("Can't give before loading a player!");
            return;
        }

        String global = in.next();
        System.out.println("Adding global \"" + global + "\".");
        player.addGlobal(global);
    }

    private void give(Scanner in) {
        if (!in.hasNext()) {
            Global.info("Give what???");
            return;
        }

        Player player = Game.getPlayer();
        if (player == null) {
            Global.info("Can't give before loading a player!");
            return;
        }

        String curr = in.next();
        switch (curr.toLowerCase()) {
            case "pokemon":
                givePokemon(in, player);
                break;
            case "item":
                giveItem(in, player);
                break;
            case "fly":
                giveFlyLocations(player);
                break;
            case "tools":
                giveTools(player);
                break;
            default:
                Global.info("Invalid token " + curr);
                break;
        }
    }

    private void givePokemon(Scanner in, Player player) {
        String pokemonName = in.next();
        PokemonNamesies namesies = PokemonNamesies.tryValueOf(pokemonName);
        if (namesies == null) {
            Global.info("Invalid Pokemon name " + pokemonName);
            return;
        }

        // Default values
        int level = PartyPokemon.MAX_LEVEL;
        List<AttackNamesies> moves = null;
        boolean shiny = false;

        while (in.hasNext()) {
            String token = in.next();
            switch (token.toLowerCase()) {
                case "level":
                    String levelInput = in.next();
                    try {
                        level = Integer.parseInt(levelInput);
                    } catch (NumberFormatException exception) {
                        Global.info("Invalid level " + levelInput);
                        return;
                    }
                    break;
                case "shiny":
                    shiny = true;
                    break;
                case "moves":
                    moves = new ArrayList<>();
                    Pattern oldDelimiter = in.delimiter();
                    in.useDelimiter(",");
                    for (int i = 0; i < MoveList.MAX_MOVES; i++) {
                        String s = in.next().trim();
                        AttackNamesies attack = AttackNamesies.tryValueOf(s);
                        if (attack == null) {
                            Global.info("Invalid move: " + s);
                            return;
                        } else {
                            moves.add(attack);
                        }
                    }
                    in.useDelimiter(oldDelimiter);
                    break;
                default:
                    Global.info("Invalid pokemon token " + token);
                    return;
            }
        }

        System.out.println("adding " + namesies.getName());

        PokemonMatcher pokemonMatcher = new PokemonMatcher(namesies, null, level, shiny, moves, null);
        player.addPokemon(pokemonMatcher.createPokemon(false, true));
    }

    private void giveItem(Scanner in, Player player) {
        String itemName = in.next();
        int amount = 1;
        if (in.hasNext()) {
            String amountToken = in.next();
            try {
                amount = Integer.parseInt(amountToken);
            } catch (NumberFormatException exception) {
                Global.info("Invalid amount " + amountToken);
                return;
            }
        }

        ItemNamesies namesies = ItemNamesies.tryValueOf(itemName);
        if (namesies == null) {
            Global.info("Invalid item name " + itemName);
            return;
        }

        player.getBag().addItem(namesies, amount);
    }

    public static void giveFlyLocations(Player player) {
        File mapsDirectory = new File(Folder.MAPS);
        for (File mapFolder : FileIO.listSubdirectories(mapsDirectory)) {
            TestMap map = new TestMap(mapFolder);
            for (AreaData area : map.getAreas()) {
                if (area.isFlyLocation()) {
                    player.setArea(map.getName(), area);
                }
            }
        }
    }

    public static void giveTools(Player player) {
        for (OverworldTool tool : OverworldTool.values()) {
            player.addGlobal(tool.getGlobalName());
        }
        player.getBag().addItem(ItemNamesies.BICYCLE);
        player.getBag().addItem(ItemNamesies.FISHING_ROD);
        player.getBag().addItem(ItemNamesies.SURFBOARD);
    }
}
