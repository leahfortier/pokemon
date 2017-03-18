package gui.view.map;

import gui.TileSet;
import gui.view.map.VisualState.VisualStateHandler;
import main.Game;
import main.Global;
import pokemon.ActivePokemon;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

class TradeState implements VisualStateHandler {
    private static final int TRADE_ANIMATION_LIFESPAN = 6000;

    private int tradeAnimationTime;

    private BufferedImage theirPokesBackImage;
    private BufferedImage theirPokesFrontImage;
    private BufferedImage myPokesBackImage;
    private BufferedImage myPokesFrontImage;

    private ActivePokemon myPokes;
    private ActivePokemon theirPokes;

    @Override
    public void draw(Graphics g, MapView mapView) {
        if (
                myPokesFrontImage == null ||
                theirPokesFrontImage == null ||
                myPokesBackImage == null ||
                theirPokesBackImage == null
        ) {
            return;
        }

       //int pokesWidth = //100;// myPokesFrontImage.getWidth() + theirPokesFrontImage.getWidth();
        int drawWidth = Global.GAME_SIZE.width;// + pokesWidth;
        int drawHeightLeft;
        int drawHeightRight;

        drawHeightRight = drawHeightLeft = Global.GAME_SIZE.height / 2;
        drawHeightRight -= myPokesFrontImage.getHeight();

        BufferedImage a = myPokesBackImage;
        BufferedImage b = theirPokesFrontImage;
        float timesies = tradeAnimationTime;
        boolean swap = false;

        // Images slide in from sides
        if (tradeAnimationTime < TRADE_ANIMATION_LIFESPAN / 2.0f) {
            a = theirPokesBackImage;
            b = myPokesFrontImage;
            timesies = TRADE_ANIMATION_LIFESPAN - timesies;
            swap = true;
        }

        float yomama = TRADE_ANIMATION_LIFESPAN / 2.0f;
        float normalizedTime = 1 - (timesies - yomama)/(yomama);
        int drawWidthA = drawWidth + a.getWidth();
        int drawWidthB = drawWidth + b.getWidth();
        int distA = (int)(drawWidthA*normalizedTime);
        int distB = (int)(drawWidthB*normalizedTime);
        if (swap) {
            distA = drawWidthA - distA;
            distB = drawWidthB - distB;
        }

        g.drawImage(a, distA - a.getWidth(), drawHeightLeft, null);
        g.drawImage(b, Global.GAME_SIZE.width - distB, drawHeightRight, null);
    }

    void setTrade(ActivePokemon myPokes, ActivePokemon theirPokes) {
        this.myPokes = myPokes;
        this.theirPokes = theirPokes;

        tradeAnimationTime = TRADE_ANIMATION_LIFESPAN;
    }

    @Override
    public void update(int dt, MapView mapView) {
        if (
                myPokesFrontImage == null ||
                theirPokesFrontImage == null ||
                myPokesBackImage == null ||
                theirPokesBackImage == null
        ) {
            loadTradeImages();
        }

        if (tradeAnimationTime < 0) {
            this.myPokes = null;
            this.theirPokes = null;
            mapView.setState(VisualState.MAP);
        }

        tradeAnimationTime -= dt;
    }

    private void loadTradeImages() {
        TileSet largeTiles = Game.getData().getPokemonTilesLarge();

        this.myPokesFrontImage = largeTiles.getTile(this.myPokes.getImageName());
        this.theirPokesFrontImage = largeTiles.getTile(this.theirPokes.getImageName());

        this.myPokesBackImage = largeTiles.getTile(this.myPokes.getImageName(false));
        this.theirPokesBackImage = largeTiles.getTile(this.theirPokes.getImageName(false));
    }
}
