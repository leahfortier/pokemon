package gui.view.map;

import draw.DrawUtils;
import draw.ImageUtils;
import gui.GameData;
import gui.view.ViewMode;
import gui.view.map.VisualState.VisualStateHandler;
import main.Game;
import main.Global;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

class TradeState implements VisualStateHandler {
    private static final int TRADE_ANIMATION_LIFESPAN = 1000;

    private int tradeAnimationTime;

    private BufferedImage pokemonImageSlideRight;
    private BufferedImage pokemonImageSlideLeft;

    private ActivePokemon myPokes;
    private ActivePokemon theirPokes;

    @Override
    public void draw(Graphics g, MapView mapView) {
        if (pokemonImageSlideRight == null || pokemonImageSlideLeft == null) {
            return;
        }

        int drawWidth = Global.GAME_SIZE.width/2;
        int drawHeightLeft;
        int drawHeightRight;
        float moveInAnimationPercentage;
        float fadeOutPercentage = 0.2f;

        // HEY YO THIS PROBABLY IS NOT RIGHT PLZ FIX THIS SO IT LOOKS GOOD THANKS YO
        drawHeightRight = drawHeightLeft = Global.GAME_SIZE.height * 5 / 8;
        drawHeightLeft -= pokemonImageSlideLeft.getHeight()/2;
        drawHeightRight -= pokemonImageSlideRight.getHeight();

        moveInAnimationPercentage = 0.5f;


        // Images slide in from sides
        if (tradeAnimationTime > TRADE_ANIMATION_LIFESPAN *(1 - moveInAnimationPercentage)) {
            float normalizedTime = (tradeAnimationTime - TRADE_ANIMATION_LIFESPAN *(1 - moveInAnimationPercentage))/(TRADE_ANIMATION_LIFESPAN *moveInAnimationPercentage);

            int dist = -pokemonImageSlideRight.getWidth()/2 - drawWidth;
            dist = (int)(dist * normalizedTime);

            g.drawImage(pokemonImageSlideLeft, drawWidth - pokemonImageSlideLeft.getWidth()/2 + dist, drawHeightLeft, null);

            dist = Global.GAME_SIZE.width;
            dist = (int)(dist * normalizedTime);

            g.drawImage(pokemonImageSlideRight, drawWidth - pokemonImageSlideRight.getWidth()/2 + dist, drawHeightRight, null);
        }
        // Hold images
        else {
            g.drawImage(pokemonImageSlideLeft, drawWidth - pokemonImageSlideLeft.getWidth()/2, drawHeightLeft, null);
            g.drawImage(pokemonImageSlideRight, drawWidth - pokemonImageSlideRight.getWidth()/2, drawHeightRight, null);

            // Fade to black before battle appears.
            float fadeOutLifespan = TRADE_ANIMATION_LIFESPAN *fadeOutPercentage;
            if (tradeAnimationTime < fadeOutLifespan) {
                int f = Math.min(255, (int)((fadeOutLifespan - tradeAnimationTime)/(fadeOutLifespan)*255));
                DrawUtils.fillCanvas(g, new Color(0, 0, 0, f));
            }
        }
    }

    void setTrade(ActivePokemon myPokes, ActivePokemon theirPokes) {
        this.myPokes = myPokes;
        this.theirPokes = theirPokes;

        tradeAnimationTime = TRADE_ANIMATION_LIFESPAN;
    }

    @Override
    public void update(int dt, MapView mapView) {
        if (pokemonImageSlideLeft == null || pokemonImageSlideRight == null) {
            loadTradeImages(mapView);
        }

        if (tradeAnimationTime < 0) {
            this.myPokes = null;
            this.theirPokes = null;
            mapView.setState(VisualState.MAP);
        }

        tradeAnimationTime -= dt;
    }

    private void loadTradeImages(MapView mapView) {
        GameData data = Game.getData();
        this.pokemonImageSlideLeft = data.getPokemonTilesLarge().getTile(this.myPokes.getImageName());
        this.pokemonImageSlideRight = data.getPokemonTilesLarge().getTile(this.theirPokes.getImageName());
    }
}
