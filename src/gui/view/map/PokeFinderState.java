package gui.view.map;

import draw.ImageUtils;
import draw.TextUtils;
import draw.button.panel.DrawPanel;
import gui.GameData;
import gui.TileSet;
import gui.view.map.VisualState.VisualStateHandler;
import input.ControlKey;
import input.InputControl;
import main.Game;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;
import trainer.CharacterData;
import trainer.pokedex.Pokedex;
import util.FontMetrics;
import util.Point;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

class PokeFinderState implements VisualStateHandler {
    private static final int NUM_ROWS = 4;
    private static final int NUM_COLUMNS = 6;
    private static final int MAX_RENDER = NUM_ROWS*NUM_COLUMNS;

    private final DrawPanel pokeFinderPanel;

    PokeFinderState() {
        pokeFinderPanel = DrawPanel.fullGamePanel().withBorderColor(new Color(219, 9, 46)).withBorderPercentage(5);
    }


    @Override
    public void draw(Graphics g, MapView mapView) {
        List<PokemonNamesies> availablePokemon = mapView.getCurrentMap().getAvailableWildPokemon();
        GameData data = Game.getData();
        CharacterData player = Game.getPlayer();
        Pokedex pokedex = player.getPokedex();

        pokeFinderPanel.drawBackground(g);
        TileSet pokedexTiles = data.getPokedexTilesSmall();
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

        Iterator<PokemonNamesies> iter = toRender.iterator();
        for (int i = 0; i < toRender.size(); i++) {
            Point point = Point.getPointAtIndex(i, NUM_COLUMNS);
            PokemonNamesies namesies = iter.next();
            if (pokedex.isNotSeen(namesies)) {
                continue;
            }

            PokemonInfo pokemonInfo = PokemonInfo.getPokemonInfo(namesies);
            BufferedImage image = pokedexTiles.getTile(pokemonInfo.getImageName());
            if (!pokedex.isCaught(namesies)) {
                image = ImageUtils.silhouette(image);
            }

            int spacing = (pokeFinderPanel.width - pokeFinderPanel.getBorderSize())/NUM_COLUMNS;
            ImageUtils.drawCenteredImage(g,
                    image,
                    pokeFinderPanel.x + pokeFinderPanel.getBorderSize() + spacing*point.x + spacing/2,
                    72 + 124*point.y
            );
        }

        FontMetrics.setFont(g, 24);
        int numCaught = (int) availablePokemon.stream().filter(p -> player.getPokedex().isCaught(p)).count();
        TextUtils.drawCenteredString(g, numCaught + "/" + availablePokemon.size() + " Caught", pokeFinderPanel.centerX(), pokeFinderPanel.bottomY() - 52);
    }

    @Override
    public void update(int dt, MapView mapView) {
        InputControl input = InputControl.instance();
        if (input.consumeIfDown(ControlKey.ESC) || input.consumeIfDown(ControlKey.POKEFINDER)) {
            mapView.setState(VisualState.MAP);
        }
    }
}
