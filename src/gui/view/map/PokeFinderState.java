package gui.view.map;

import draw.ImageUtils;
import draw.layout.DrawLayout;
import draw.panel.BasicPanels;
import draw.panel.DrawPanel;
import draw.panel.PanelList;
import gui.TileSet;
import input.ControlKey;
import input.InputControl;
import main.Game;
import pokemon.species.PokemonInfo;
import pokemon.species.PokemonNamesies;
import trainer.player.Player;
import trainer.player.pokedex.Pokedex;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

class PokeFinderState extends VisualStateHandler {
    private static final int NUM_ROWS = 3;
    private static final int NUM_COLS = 5;
    private static final int MAX_RENDER = NUM_ROWS*NUM_COLS;

    private final PanelList panels;
    private final DrawPanel[] pokemonPanels;
    private final DrawPanel labelPanel;

    PokeFinderState() {
        DrawPanel pokeFinderPanel = BasicPanels.newFullGamePanel()
                                               .withBorderColor(new Color(219, 9, 46))
                                               .withBorderPercentage(5);

        DrawLayout layout = new DrawLayout(pokeFinderPanel, NUM_ROWS, NUM_COLS, PokemonInfo.MAX_POKEDEX_IMAGE_SIZE)
                .withMissingBottomRow();

        pokemonPanels = layout.getPanels();
        labelPanel = layout.getFullBottomPanel().withNoBackground().withLabelSize(24);

        panels = new PanelList(pokeFinderPanel, labelPanel).add(pokemonPanels);
    }

    @Override
    public void draw(Graphics g) {
        panels.drawAll(g);
    }

    @Override
    public void update(int dt) {
        InputControl input = InputControl.instance();
        if (input.consumeIfDown(ControlKey.ESC, ControlKey.POKEFINDER)) {
            view.setState(VisualState.MAP);
        }
    }

    @Override
    public void set() {
        Player player = Game.getPlayer();

        List<PokemonNamesies> availablePokemon = view.getCurrentMap().getArea(player.getLocation()).getAvailableWildPokemon();
        if (availablePokemon.isEmpty()) {
            view.setState(VisualState.MAP);
            return;
        }

        Pokedex pokedex = player.getPokedex();
        TileSet pokedexTiles = Game.getData().getPokedexTilesSmall();

        Set<PokemonNamesies> toRender = new TreeSet<>();
        for (PokemonNamesies namesies : availablePokemon) {
            if (toRender.size() == MAX_RENDER) {
                break;
            }

            if (!pokedex.isNotSeen(namesies) && !pokedex.isCaught(namesies)) {
                toRender.add(namesies);
            }
        }

        // Caught pokemon have a lower priority to be shown if too many Pokemon to render
        for (PokemonNamesies namesies : availablePokemon) {
            if (toRender.size() == MAX_RENDER) {
                break;
            }

            if (pokedex.isCaught(namesies)) {
                toRender.add(namesies);
            }
        }

        // Set pokedex image label for each pokemon being rendered
        // If Pokemon has only been seen (not caught), set image to silhouette
        Iterator<PokemonNamesies> iter = toRender.iterator();
        for (DrawPanel panel : pokemonPanels) {
            panel.setSkip(!iter.hasNext());
            if (!panel.isSkipping()) {
                PokemonNamesies namesies = iter.next();
                PokemonInfo pokemonInfo = namesies.getInfo();

                BufferedImage image = pokedexTiles.getTile(pokemonInfo.getImageName());
                if (!pokedex.isCaught(namesies)) {
                    image = ImageUtils.silhouette(image);
                }
                panel.withImageLabel(image);
            }
        }

        // Display total number of Pokemon available and how many are caught
        int numCaught = (int)availablePokemon.stream().filter(pokedex::isCaught).count();
        labelPanel.withLabel(numCaught + "/" + availablePokemon.size() + " Caught");
    }
}
