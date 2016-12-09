package gui.view.map;

import battle.Battle;
import gui.GameData;
import gui.view.ViewMode;
import gui.view.map.VisualState.VisualStateHandler;
import main.Game;
import main.Global;
import map.TerrainType;
import pokemon.ActivePokemon;
import sound.SoundPlayer;
import sound.SoundTitle;
import util.DrawUtils;
import util.FileIO;
import util.Folder;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

class BattleState implements VisualStateHandler {
    private static final BufferedImage UPPER_POKEBALL_IMAGE = FileIO.readImage(Folder.IMAGES + "PokeBallBattleIntroTop.png");
    private static final BufferedImage LOWER_POKEBALL_IMAGE = FileIO.readImage(Folder.IMAGES + "PokeBallBattleIntroBottom.png");

    private static final int BATTLE_INTRO_ANIMATION_LIFESPAN = 1000;

    private Battle battle;
    private boolean seenWild;

    private int battleAnimationTime;

    private BufferedImage battleImageSlideRight;
    private BufferedImage battleImageSlideLeft;

    @Override
    public void draw(Graphics g, MapView mapView) {
        if (battleImageSlideRight == null || battleImageSlideLeft == null) {
            return;
        }

        int drawWidth = Global.GAME_SIZE.width/2;
        int drawHeightLeft;
        int drawHeightRight;
        float moveInAnimationPercentage;
        float fadeOutPercentage = 0.2f;

        if (battle.isWildBattle()) {
            drawHeightRight = drawHeightLeft = Global.GAME_SIZE.height * 5 / 8;
            drawHeightLeft -= battleImageSlideLeft.getHeight()/2;
            drawHeightRight -= battleImageSlideRight.getHeight();

            moveInAnimationPercentage = 0.5f;
        }
        else {
            drawHeightRight = drawHeightLeft = Global.GAME_SIZE.height/2;
            drawHeightLeft -= battleImageSlideLeft.getHeight();
            //drawHeightRight -= battleImageSlideRight.getHeight();

            moveInAnimationPercentage = 0.4f;
        }


        // Images slide in from sides
        if (battleAnimationTime > BATTLE_INTRO_ANIMATION_LIFESPAN*(1-moveInAnimationPercentage)) {
            float normalizedTime = (battleAnimationTime - BATTLE_INTRO_ANIMATION_LIFESPAN*(1-moveInAnimationPercentage))/ (BATTLE_INTRO_ANIMATION_LIFESPAN*moveInAnimationPercentage);

            int dist = -battleImageSlideRight.getWidth()/2 -drawWidth;
            dist = (int)(dist * normalizedTime);

            g.drawImage(battleImageSlideLeft, drawWidth - battleImageSlideLeft.getWidth()/2 + dist, drawHeightLeft, null);

            dist = Global.GAME_SIZE.width;
            dist = (int)(dist * normalizedTime);

            g.drawImage(battleImageSlideRight, drawWidth - battleImageSlideRight.getWidth()/2 + dist, drawHeightRight, null);
        }
        // Hold images
        else {
            g.drawImage(battleImageSlideLeft, drawWidth - battleImageSlideLeft.getWidth()/2, drawHeightLeft, null);
            g.drawImage(battleImageSlideRight, drawWidth - battleImageSlideRight.getWidth()/2, drawHeightRight, null);

            //Fade to black before battle appears.
            if (battleAnimationTime < BATTLE_INTRO_ANIMATION_LIFESPAN*fadeOutPercentage)
            {
                int f = Math.min(255, (int)((BATTLE_INTRO_ANIMATION_LIFESPAN*fadeOutPercentage - battleAnimationTime)/ (BATTLE_INTRO_ANIMATION_LIFESPAN*fadeOutPercentage) *255));

                g.setColor(new Color(0, 0, 0, f));
                g.fillRect(0, 0, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
            }
        }
    }

    void setBattle(Battle battle, boolean seenWild, TerrainType terrain) {
        this.battle = battle;
        this.seenWild = seenWild;
        this.battle.setTerrainType(terrain, true);

        battleAnimationTime = BATTLE_INTRO_ANIMATION_LIFESPAN;
        battleImageSlideLeft = null;
        battleImageSlideRight = null;

        if (battle.isWildBattle()) {
            SoundPlayer.soundPlayer.playMusic(SoundTitle.WILD_POKEMON_BATTLE);
        }
        else {
            SoundPlayer.soundPlayer.playMusic(SoundTitle.TRAINER_BATTLE);
        }
    }

    boolean hasBattle() {
        return this.battle != null;
    }

    @Override
    public void update(int dt, MapView mapView) {
        if (battleImageSlideLeft == null || battleImageSlideRight == null) {
            loadBattleImages(mapView);
        }

        if (battleAnimationTime < 0) {
            this.battle = null;
            Game.setViewMode(ViewMode.BATTLE_VIEW);
            mapView.setState(VisualState.MAP);
        }

        battleAnimationTime -= dt;
    }

    private void loadBattleImages(MapView mapView) {
        GameData data = Game.getData();

        if (battle.isWildBattle()) {
            battleImageSlideLeft = mapView.getTerrain().getOpponentCircleImage();

            ActivePokemon p = battle.getOpponent().front();
            battleImageSlideRight = data.getPokemonTilesLarge().getTile(p.getImageIndex());

            if (seenWild) {
                battleImageSlideRight = DrawUtils.colorImage(battleImageSlideRight, new float[] { 0, 0, 0, 1 }, new float[] { 0, 0, 0, 0 });
            }
        }
        else {
            battleImageSlideRight = LOWER_POKEBALL_IMAGE;
            battleImageSlideLeft = UPPER_POKEBALL_IMAGE;
        }
    }
}
