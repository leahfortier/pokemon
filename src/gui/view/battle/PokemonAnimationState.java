package gui.view.battle;

import battle.Battle;
import battle.effect.status.StatusCondition;
import draw.Alignment;
import draw.DrawUtils;
import draw.ImageUtils;
import draw.TextUtils;
import draw.panel.DrawPanel;
import gui.GameData;
import gui.TileSet;
import main.Game;
import main.Global;
import message.MessageUpdate;
import pokemon.ActivePokemon;
import pokemon.Gender;
import pokemon.PokemonInfo;
import pokemon.Stat;
import sound.SoundPlayer;
import trainer.Trainer;
import trainer.player.Player;
import type.Type;
import util.FontMetrics;
import util.Point;
import util.StringUtils;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

// Handles animation and keeps track of the current state
class PokemonAnimationState {
    private static final int STATUS_BOX_SPACING = 25;

    // Loss Constants <-- Super Meaningful Comment
    private static final int FRAMES_PER_HP_LOSS = 20;
    private static final float HP_LOSS_RATIO = 0.1f;
    private static final float EXP_LOSS_RATIO = 30f;

    // Evolution and Catch Lifespans
    private static final int EVOLVE_ANIMATION_LIFESPAN = 3000;
    private static final int CATCH_SHAKE_ANIMATION_LIFESPAN = 1000;
    private static final int CATCH_TRANSFORM_ANIMATION_LIFESPAN = 2000;
    private static final int CATCH_ANIMATION_LIFESPAN = CATCH_SHAKE_ANIMATION_LIFESPAN*Player.CATCH_SHAKES + CATCH_TRANSFORM_ANIMATION_LIFESPAN;

    private final BattleView battleView;
    private final boolean isPlayer;

    private final Point pokemonDrawLocation;

    private final DrawPanel statusBox;
    private final DrawPanel hpBar;
    private final DrawPanel expBar;

    // Previous and current state
    private PokemonState oldState;
    private PokemonState state;

    // Animation values
    private int animationHP;
    private int animationEvolve;
    private int animationExp;
    private int animationCatch;
    private int animationCatchDuration;

    PokemonAnimationState(BattleView battleView, boolean isPlayer) {
        this.battleView = battleView;
        this.isPlayer = isPlayer;

        if (isPlayer) {
            this.statusBox = new DrawPanel(463, 284, 295, 123);
            this.pokemonDrawLocation = new Point(190, 412);
        } else {
            this.statusBox = new DrawPanel(42, 32, 295, 110);
            this.pokemonDrawLocation = new Point(607, 237);
        }

        this.statusBox
                .withBorderPercentage(13)
                .withTransparentCount(2)
                .withBlackOutline();

        int hpBarWidth = 200;
        this.hpBar = new DrawPanel(
                statusBox.rightX() - STATUS_BOX_SPACING - hpBarWidth,
                statusBox.y + 68,
                hpBarWidth,
                19)
                .withBlackOutline();

        this.expBar = new DrawPanel(
                statusBox.x,
                statusBox.bottomY() - DrawUtils.OUTLINE_SIZE,
                statusBox.width,
                12)
                .withBlackOutline();
    }

    void resetBattle(ActivePokemon p) {
        this.oldState = new PokemonState();
        this.state = new PokemonState();

        resetVals(p);
        state.imageName = null;
    }

    private void resetVals(ActivePokemon p) {
        Battle b = battleView.getCurrentBattle();
        resetVals(
                p.getHP(),
                p.getDisplayType(b),
                p.isShiny(),
                p.getPokemonInfo(),
                p.getName(),
                p.getGender(),
                p.expRatio(),
                p,
                b.getTrainer(p).getTeamIndex(p)
        );
    }

    // Resets all the values in a state
    private void resetVals(
            int hp,
            Type[] type,
            boolean shiny,
            PokemonInfo pokemon,
            String name,
            Gender gender,
            float expRatio,
            ActivePokemon frontPokemon,
            int teamIndex
    ) {
        animationHP = 0;
        animationExp = 0;
        animationCatchDuration = 0;

        state.hp = oldState.hp = hp;
        state.type = type;
        state.imageName = pokemon.getImageName(shiny, !isPlayer);
        state.caught = battleView.getCurrentBattle().isWildBattle() && Game.getPlayer().getPokedex().isCaught(pokemon.namesies());
        state.name = name;
        state.gender = gender;
        state.expRatio = oldState.expRatio = expRatio;
        state.frontPokemon = frontPokemon;
        state.teamIndex = teamIndex;
    }

    private void startHpAnimation(int newHp) {
        if (newHp == state.hp) {
            return;
        }

        oldState.hp = state.hp;
        state.hp = newHp;
        animationHP = Math.abs(oldState.hp - state.hp)*FRAMES_PER_HP_LOSS;
    }

    public void setType(Type[] newType) {
        state.type = newType;
    }

    private void startPokemonUpdateAnimation(String imageName, boolean animate) {
        if (!StringUtils.isNullOrEmpty(state.imageName)) {
            oldState.imageName = state.imageName;
            if (animate) {
                animationEvolve = EVOLVE_ANIMATION_LIFESPAN;
            }
        }

        state.imageName = imageName;
        animationCatchDuration = 0;
    }

    private void startPokemonUpdateAnimation(PokemonInfo newPokemon, boolean newShiny, boolean animate) {
        startPokemonUpdateAnimation(newPokemon.getImageName(newShiny, !isPlayer), animate);
    }

    private void startCatchAnimation(int duration) {
        if (duration == -1) { // TODO: There should be a constant for this
            animationCatch = CATCH_ANIMATION_LIFESPAN;
            animationCatchDuration = -1;
        }
        else {
            animationCatch = duration*CATCH_SHAKE_ANIMATION_LIFESPAN + 2*CATCH_TRANSFORM_ANIMATION_LIFESPAN;
            animationCatchDuration = animationCatch;
        }
    }

    private void startExpAnimation(float newExpRatio, boolean levelUp) {
        oldState.expRatio = levelUp ? 0 : state.expRatio;
        state.expRatio = newExpRatio;
        animationExp = (int)(100*Math.abs(oldState.expRatio - state.expRatio)*FRAMES_PER_HP_LOSS);
    }

    public void setName(String newName) {
        state.name = newName;
    }

    public void setGender(Gender newGender) {
        state.gender = newGender;
    }

    public boolean isEmpty() {
        return StringUtils.isNullOrEmpty(state.imageName);
    }

    boolean isAnimationPlaying() {
        return animationHP != 0 || animationEvolve != 0 || animationCatch != 0 || animationExp != 0;
    }

    // TODO: lalala generalize this shit again I suck I suck I suck at everything
    // Might want to include a helper class that contains a generic method for different types of animations
    private void catchAnimation(Graphics g, BufferedImage plyrImg) {
        Graphics2D g2d = (Graphics2D)g;
        float[] pokeyScales = { 1f, 1f, 1f, 1f };
        float[] pokeyOffsets = { 255f, 255f, 255f, 0f };
        float[] ballScales = { 1f, 1f, 1f, 1f };
        float[] ballOffsets = { 255f, 255f, 255f, 0f };

        int xOffset = 0;
        int lifespan = animationCatchDuration == -1 ? CATCH_ANIMATION_LIFESPAN : animationCatchDuration;

        // Turn white
        if (animationCatch > lifespan - CATCH_TRANSFORM_ANIMATION_LIFESPAN*.3) {
            pokeyOffsets[0] = pokeyOffsets[1] = pokeyOffsets[2] = 255*(1 - (animationCatch - (lifespan - CATCH_TRANSFORM_ANIMATION_LIFESPAN*.3f))/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(1 - .7f)));
            ballScales[3] = 0;
        }
        // Transform into Pokeball
        else if (animationCatch > lifespan - CATCH_TRANSFORM_ANIMATION_LIFESPAN*.7) {
            pokeyOffsets[0] = pokeyOffsets[1] = pokeyOffsets[2] = 255;
            pokeyScales[3] = ((animationCatch - (lifespan - CATCH_TRANSFORM_ANIMATION_LIFESPAN*0.7f))/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(.7f - .3f)));
            ballOffsets[0] = ballOffsets[1] = ballOffsets[2] = 255;
            ballScales[3] = (1 - (animationCatch - (lifespan - CATCH_TRANSFORM_ANIMATION_LIFESPAN*0.7f))/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(.7f - .3f)));
        }
        // Restore color
        else if (animationCatch > lifespan - CATCH_TRANSFORM_ANIMATION_LIFESPAN) {
            pokeyScales[3] = 0;
            ballOffsets[0] = ballOffsets[1] = ballOffsets[2] = 255*(animationCatch - (lifespan - CATCH_TRANSFORM_ANIMATION_LIFESPAN))/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(.3f));
        }
        // Shake
        else if (animationCatchDuration == -1 || animationCatch > CATCH_TRANSFORM_ANIMATION_LIFESPAN) {
            pokeyScales[3] = 0;
            ballOffsets[0] = ballOffsets[1] = ballOffsets[2] = 0;
            xOffset = (int)(10*Math.sin(animationCatch/200.0));
        }
        // Turn white -- didn't catch
        else if (animationCatch > CATCH_TRANSFORM_ANIMATION_LIFESPAN*.7) {
            ballOffsets[0] = ballOffsets[1] = ballOffsets[2] = 255*(1f - (animationCatch - CATCH_TRANSFORM_ANIMATION_LIFESPAN*.7f)/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(1 - 0.7f)));
            pokeyScales[3] = 0;
        }
        // Transform into Pokemon
        else if (animationCatch > CATCH_TRANSFORM_ANIMATION_LIFESPAN*.3) {
            pokeyOffsets[0] = pokeyOffsets[1] = pokeyOffsets[2] = 255;
            pokeyScales[3] = (1 - (animationCatch - CATCH_TRANSFORM_ANIMATION_LIFESPAN*0.3f)/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(.7f - .3f)));
            ballOffsets[0] = ballOffsets[1] = ballOffsets[2] = 255;
            ballScales[3] = ((animationCatch - CATCH_TRANSFORM_ANIMATION_LIFESPAN*0.3f)/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(.7f - .3f)));
        }
        // Restore color
        else {
            ballScales[3] = 0;
            pokeyOffsets[0] = pokeyOffsets[1] = pokeyOffsets[2] = 255*(animationCatch)/(CATCH_TRANSFORM_ANIMATION_LIFESPAN*(1.0f - .7f));
        }

        animationCatch -= Global.MS_BETWEEN_FRAMES;

        BufferedImage pkBall = Game.getData().getItemTilesLarge().getTile(Game.getPlayer().getPokeball().getImageName());

        int px = pokemonDrawLocation.x;
        int py = pokemonDrawLocation.y;

        g2d.drawImage(ImageUtils.colorImage(pkBall, ballScales, ballOffsets), px - pkBall.getWidth()/2 + xOffset, py - pkBall.getHeight(), null);
        g2d.drawImage(ImageUtils.colorImage(plyrImg, pokeyScales, pokeyOffsets), px - plyrImg.getWidth()/2, py - plyrImg.getHeight(), null);
    }

    // hi :)
    private void evolveAnimation(Graphics g, BufferedImage plyrImg, TileSet pkmTiles) {
        animationEvolve = ImageUtils.transformAnimation(
                g,
                animationEvolve,
                EVOLVE_ANIMATION_LIFESPAN,
                plyrImg,
                pkmTiles.getTile(oldState.imageName),
                pokemonDrawLocation
        );
    }

    private void drawHealthBar(Graphics g) {
        int maxHp = state.frontPokemon.getMaxHP();

        // Get the ratio based off of the possible animation
        float ratio = state.hp/(float)maxHp;
        String hpStr = state.hp + "/" + maxHp;

        if (animationHP > 0) {
            animationHP -= Math.max(0, HP_LOSS_RATIO*maxHp + 1);
            float originalTime = Math.abs(state.hp - oldState.hp)*FRAMES_PER_HP_LOSS;
            float numerator = (state.hp + (oldState.hp - state.hp)*(animationHP/originalTime));

            ratio = numerator/maxHp;
            hpStr = (int)numerator + "/" + maxHp;
        }
        else {
            animationHP = 0;
        }

        // Set the proper color for the ratio and fill in the health bar as appropriate
        g.setColor(DrawUtils.getHPColor(ratio));
        if (animationHP > 0 && (animationHP/10)%2 == 0) {
            g.setColor(g.getColor().darker());
        }

        hpBar.fillBar(g, g.getColor(), ratio);

        if (isPlayer) {
            FontMetrics.setFont(g, 24);
            TextUtils.drawShadowText(
                    g,
                    hpStr,
                    statusBox.rightX() - STATUS_BOX_SPACING,
                    hpBar.bottomY() + FontMetrics.getTextHeight(g) + 5,
                    Alignment.RIGHT);
        }
    }

    private void drawExpBar(Graphics g) {
        // Show the animation
        float expRatio = state.expRatio;
        if (animationExp > 0) {
            animationExp -= EXP_LOSS_RATIO;
            int originalTime = (int)(100*Math.abs(state.expRatio - oldState.expRatio)*FRAMES_PER_HP_LOSS);
            expRatio = (state.expRatio + (oldState.expRatio - state.expRatio)*(animationExp/(float)originalTime));
        }
        else {
            animationExp = 0;
        }

        expBar.fillBar(g, DrawUtils.EXP_BAR_COLOR, expRatio);
    }

    private void drawPokemon(Graphics g) {
        // Draw the Pokemon image if applicable
        if (!isEmpty() && state.showImage()) {
            GameData data = Game.getData();
            TileSet pkmTiles = isPlayer ? data.getPokemonTilesLarge() : data.getPokemonTilesMedium();

            BufferedImage plyrImg = pkmTiles.getTile(state.imageName);
            if (plyrImg != null) {
                if (animationEvolve > 0) {
                    evolveAnimation(g, plyrImg, pkmTiles);
                }
                else if (animationCatch > 0) {
                    catchAnimation(g, plyrImg);
                }
                else {
                    if (animationCatchDuration == -1) {
                        plyrImg = data.getItemTilesLarge().getTile(Game.getPlayer().getPokeball().getImageName());
                    }

                    ImageUtils.drawBottomCenteredImage(g, plyrImg, pokemonDrawLocation);

                    animationEvolve = 0;
                    animationCatch = 0;
                }
            }
        }
    }

    // Draws the status box, not including the text
    void drawStatusBox(Graphics g) {
        drawPokemon(g);

        // Draw the colored type polygons
        this.statusBox.withBackgroundColors(Type.getColors(state.type)).drawBackground(g);

        // Draw health bar and player's EXP Bar
        drawHealthBar(g);
        if (isPlayer) {
            drawExpBar(g);
        }

        // Name and gender in top left
        FontMetrics.setFont(g, 27);
        TextUtils.drawShadowText(
                g,
                state.name + " " + state.gender.getCharacter(),
                statusBox.x + STATUS_BOX_SPACING,
                statusBox.y + STATUS_BOX_SPACING + FontMetrics.getTextHeight(g),
                Alignment.LEFT);

        // Level in top right
        TextUtils.drawShadowText(
                g,
                "Lv" + state.getLevel(),
                statusBox.rightX() - STATUS_BOX_SPACING,
                statusBox.y + STATUS_BOX_SPACING + FontMetrics.getTextHeight(g),
                Alignment.RIGHT);

        // Status to the left of the hp bar
        FontMetrics.setFont(g, 24);
        TextUtils.drawShadowText(
                g,
                state.getStatus().getName(),
                statusBox.x + STATUS_BOX_SPACING,
                hpBar.centerY(),
                Alignment.CENTER_Y);

        // Stat modifiers
        FontMetrics.setFont(g, 12);
        for (int i = 0; i < Stat.NUM_BATTLE_STATS; i++) {
            int stage = state.getStage(i);
            if (stage == 0) {
                continue;
            }

            String message = "";
            if (stage > 0) {
                message += " ";
            }

            message += stage + "  ";
            g.drawString(message, hpBar.x + FontMetrics.getTextWidth(g, message)*i, hpBar.y - 12);
            g.drawString(Stat.getStat(i, true).getShortestName(), hpBar.x + FontMetrics.getTextWidth(g, message)*i + 2, hpBar.y - 4);
        }

        if (isPlayer) {
            drawTrainerPokeballs(g, expBar.rightX(), expBar.bottomY(), -1);
        }
        else if (!battleView.getCurrentBattle().isWildBattle()) {
            drawTrainerPokeballs(g, statusBox.x, statusBox.y, 1);
        }

        // Show whether or not the wild Pokemon has already been caught
        if (!isPlayer && state.caught) {
            ImageUtils.drawCenteredImage(
                    g,
                    TileSet.TINY_POKEBALL,
                    hpBar.rightX() + (STATUS_BOX_SPACING - statusBox.getBorderSize())/2,
                    hpBar.centerY()
            );
        }
    }

    private void drawTrainerPokeballs(Graphics g, int cornerX, int cornerY, int direction) {
        Battle battle = battleView.getCurrentBattle();
        Trainer trainer = (Trainer)battle.getTrainer(isPlayer);
        List<ActivePokemon> team = trainer.getTeam();
        boolean usedMaxPokemon = trainer.maxPokemonUsed(battle);

        for (int i = 0; i < team.size(); i++) {
            int index = isPlayer ? team.size() - i - 1 : i;
            ActivePokemon pokemon = team.get(index);

            boolean silhouette = i == state.teamIndex
                    ? state.getStatus() == StatusCondition.FAINTED
                    : !pokemon.canFight() || (usedMaxPokemon && !pokemon.getAttributes().isBattleUsed());

            BufferedImage pokeball = TileSet.TINY_POKEBALL;
            if (silhouette) {
                pokeball = ImageUtils.silhouette(pokeball);
            }

            ImageUtils.drawCenteredImage(
                    g,
                    pokeball,
                    cornerX + direction*pokeball.getWidth()*(2*i + 1),
                    cornerY - direction*pokeball.getHeight()
            );
        }
    }

    void checkMessage(MessageUpdate newMessage) {
        if (newMessage.switchUpdate()) {
            ActivePokemon front = newMessage.getFrontPokemon();
            resetVals(
                    front.getHP(),
                    newMessage.getType(),
                    newMessage.getShiny(),
                    newMessage.getPokemon(),
                    newMessage.getName(),
                    newMessage.getGender(),
                    front.expRatio(),
                    newMessage.getFrontPokemon(),
                    newMessage.getTeamIndex()
            );
        }
        else {
            if (newMessage.frontPokemonUpdate()) {
                ActivePokemon pokemon = newMessage.getFrontPokemon();
                state.frontPokemon = pokemon;
                startHpAnimation(pokemon.getHP());
            }

            if (newMessage.typeUpdate()) {
                setType(newMessage.getType());
            }

            if (newMessage.catchUpdate()) {
                startCatchAnimation(newMessage.getDuration());
            }

            if (newMessage.imageUpdate()) {
                startPokemonUpdateAnimation(newMessage.getImageName(), newMessage.isAnimate());
            }

            if (newMessage.pokemonUpdate()) {
                startPokemonUpdateAnimation(newMessage.getPokemon(), newMessage.getShiny(), newMessage.isAnimate());
            }

            if (newMessage.expUpdate()) {
                startExpAnimation(newMessage.getEXPRatio(), newMessage.levelUpdate());
            }

            if  (newMessage.soundEffectUpdate()) {
                SoundPlayer.soundPlayer.playSoundEffect(newMessage.getSoundEffect());
            }

            if (newMessage.nameUpdate()) {
                setName(newMessage.getName());
            }

            if (newMessage.genderUpdate()) {
                setGender(newMessage.getGender());
            }
        }
    }

    // A class to hold the state of a Pokemon
    private static class PokemonState {
        private int hp;
        private String imageName;
        private String name;
        private Type[] type;
        private float expRatio;
        private boolean caught;
        private Gender gender;
        private ActivePokemon frontPokemon;
        private int teamIndex;

        PokemonState() {
            type = new Type[2];
        }

        public StatusCondition getStatus() {
            return this.frontPokemon == null ? StatusCondition.NO_STATUS : this.frontPokemon.getStatus().getType();
        }

        public int getStage(int index) {
            return this.frontPokemon == null ? 0 : this.frontPokemon.getAttributes().getStage(Stat.getStat(index, true));
        }

        public int getLevel() {
            return this.frontPokemon.getLevel();
        }

        public boolean showImage() {
            return this.frontPokemon != null && !this.frontPokemon.isSemiInvulnerable();
        }
    }
}
