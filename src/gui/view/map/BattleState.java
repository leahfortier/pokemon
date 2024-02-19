package gui.view.map;

import battle.ActivePokemon;
import battle.Battle;
import draw.DrawUtils;
import draw.ImageUtils;
import gui.GameData;
import gui.view.ViewMode;
import main.Game;
import main.Global;
import sound.SoundPlayer;
import sound.SoundTitle;
import util.file.FileIO;
import util.file.Folder;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

class BattleState extends VisualStateHandler {
    private static final BufferedImage UPPER_POKEBALL_IMAGE = FileIO.readImage(Folder.IMAGES + "PokeBallBattleIntroTop.png");
    private static final BufferedImage LOWER_POKEBALL_IMAGE = FileIO.readImage(Folder.IMAGES + "PokeBallBattleIntroBottom.png");

    private static final int BATTLE_INTRO_ANIMATION_LIFESPAN = 1000;

    private Battle battle;
    private boolean seenWild;

    private int battleAnimationTime;

    private BufferedImage battleImageSlideRight;
    private BufferedImage battleImageSlideLeft;

    @Override
    public void draw(Graphics g) {
        int drawWidth = Global.GAME_SIZE.width/2;
        int drawHeightLeft;
        int drawHeightRight;
        float moveInAnimationPercentage;
        float fadeOutPercentage = 0.2f;

        if (battle.isWildBattle()) {
            drawHeightRight = drawHeightLeft = Global.GAME_SIZE.height*5/8;
            drawHeightLeft -= battleImageSlideLeft.getHeight()/2;
            drawHeightRight -= battleImageSlideRight.getHeight();

            moveInAnimationPercentage = 0.5f;
        } else {
            drawHeightRight = drawHeightLeft = Global.GAME_SIZE.height/2;
            drawHeightLeft -= battleImageSlideLeft.getHeight();

            moveInAnimationPercentage = 0.4f;
        }

        // Images slide in from sides
        if (battleAnimationTime > BATTLE_INTRO_ANIMATION_LIFESPAN*(1 - moveInAnimationPercentage)) {
            float normalizedTime = (battleAnimationTime - BATTLE_INTRO_ANIMATION_LIFESPAN*(1 - moveInAnimationPercentage))/(BATTLE_INTRO_ANIMATION_LIFESPAN*moveInAnimationPercentage);

            int dist = -battleImageSlideRight.getWidth()/2 - drawWidth;
            dist = (int)(dist*normalizedTime);

            g.drawImage(battleImageSlideLeft, drawWidth - battleImageSlideLeft.getWidth()/2 + dist, drawHeightLeft, null);

            dist = Global.GAME_SIZE.width;
            dist = (int)(dist*normalizedTime);

            g.drawImage(battleImageSlideRight, drawWidth - battleImageSlideRight.getWidth()/2 + dist, drawHeightRight, null);
        }
        // Hold images
        else {
            g.drawImage(battleImageSlideLeft, drawWidth - battleImageSlideLeft.getWidth()/2, drawHeightLeft, null);
            g.drawImage(battleImageSlideRight, drawWidth - battleImageSlideRight.getWidth()/2, drawHeightRight, null);

            // Fade to black before battle appears.
            float fadeOutLifespan = BATTLE_INTRO_ANIMATION_LIFESPAN*fadeOutPercentage;
            if (battleAnimationTime < fadeOutLifespan) {
                int f = Math.min(255, (int)((fadeOutLifespan - battleAnimationTime)/(fadeOutLifespan)*255));
                DrawUtils.fillCanvas(g, new Color(0, 0, 0, f));
            }
        }
    }

    void setBattle(Battle battle, boolean seenWild) {
        this.battle = battle;
        this.seenWild = seenWild;

        battleAnimationTime = BATTLE_INTRO_ANIMATION_LIFESPAN;
        this.loadBattleImages(view);

        if (Game.getPlayer().getOptions().shouldPlayBattleMusic()) {
            SoundTitle music = battle.isWildBattle()
                    ? SoundTitle.WILD_POKEMON_BATTLE
                    : SoundTitle.TRAINER_BATTLE;
            SoundPlayer.instance().playMusic(music);
        }
    }

    boolean hasBattle() {
        return this.battle != null;
    }

    @Override
    public void update(int dt) {
        if (battleAnimationTime < 0) {
            this.battle = null;
            Game.instance().setViewMode(ViewMode.BATTLE_VIEW);
            view.setState(VisualState.MAP);
        }

        battleAnimationTime -= dt;
    }

    private void loadBattleImages(MapView mapView) {
        if (battle.isWildBattle()) {
            GameData data = Game.getData();
            ActivePokemon p = battle.getOpponent().front();

            battleImageSlideRight = data.getPokemonTilesLarge().getTile(p.getImageName());
            battleImageSlideLeft = mapView.getCurrentArea().getBattleTerrain().getOpponentCircleImage();

            if (seenWild) {
                battleImageSlideRight = ImageUtils.silhouette(battleImageSlideRight);
            }
        } else {
            battleImageSlideRight = LOWER_POKEBALL_IMAGE;
            battleImageSlideLeft = UPPER_POKEBALL_IMAGE;
        }
    }
}
