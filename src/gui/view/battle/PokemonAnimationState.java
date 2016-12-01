package gui.view.battle;

import battle.effect.status.StatusCondition;
import gui.TileSet;
import main.Global;
import main.Type;
import pokemon.ActivePokemon;
import pokemon.Gender;
import pokemon.PokemonInfo;
import util.DrawUtils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

// Handles animation and keeps track of the current state
public class PokemonAnimationState {
    private BattleView battleView; // todo
    public PokemonState oldState, state;
    private int animationHP, animationEvolve, animationExp, animationCatch;
    private int animationCatchDuration;

    PokemonAnimationState(BattleView battleView) {
        this.battleView = battleView;
        oldState = new PokemonState();
        state = new PokemonState();
    }

    // todo
    public void resetVals(ActivePokemon p) {
        resetVals(
                p.getHP(),
                p.getStatus().getType(),
                p.getDisplayType(battleView.currentBattle),
                p.isShiny(),
                p.getPokemonInfo(),
                p.getName(),
                p.getMaxHP(),
                p.getLevel(),
                p.getGender(),
                p.expRatio()
        );
    }

    // todo
    // Resets all the values in a state
    public void resetVals(
            int hp,
            StatusCondition status,
            Type[] type,
            boolean shiny,
            PokemonInfo pokemon,
            String name,
            int maxHP,
            int level,
            Gender gender,
            float expRatio
    ) {
        animationHP = 0;
        animationExp = 0;
        animationCatchDuration = 0;

        state.hp = oldState.hp = hp;
        state.status = oldState.status = status;
        state.type = type;
        state.shiny = shiny;
        state.imageNumber = pokemon.getImageNumber(state.shiny);
        state.caught = battleView.currentBattle.isWildBattle() && battleView.currentBattle.getPlayer().getPokedex().isCaught(pokemon.namesies());
        state.name = name;
        state.maxHp = oldState.maxHp = maxHP;
        state.level = level;
        state.gender = gender;
        state.expRatio = oldState.expRatio = expRatio;
    }

    public void startHpAnimation(int newHp) {
        if (newHp == state.hp) {
            return;
        }

        oldState.hp = state.hp;
        state.hp = newHp;
        animationHP = Math.abs(oldState.hp - state.hp)* BattleView.FRAMES_PER_HP_LOSS;
    }

    public void setMaxHP(int newMax) {
        state.maxHp = newMax;
    }

    public void setStatus(StatusCondition newStatus) {
        oldState.status = state.status;
        state.status = newStatus;
    }

    public void setType(Type[] newType)
    {
        state.type = newType;
    }

    public void startPokemonUpdateAnimation(PokemonInfo newPokemon, boolean newShiny, boolean animate) {
        state.shiny = newShiny;
        if (state.imageNumber != 0) {
            oldState.imageNumber = state.imageNumber;
            if (animate) {
                animationEvolve = BattleView.EVOLVE_ANIMATION_LIFESPAN;
            }
        }

        state.imageNumber = newPokemon.getImageNumber(state.shiny);
        animationCatchDuration = 0;
    }

    public void startCatchAnimation(int duration) {
        if (duration == -1) { // TODO: There should be a constant for this
            animationCatch = BattleView.CATCH_ANIMATION_LIFESPAN;
            animationCatchDuration = -1;
        }
        else {
            // TODO: uggy
            animationCatch = animationCatchDuration = duration* BattleView.CATCH_SHAKE_ANIMATION_LIFESPAN + 2* BattleView.CATCH_TRANSFORM_ANIMATION_LIFESPAN;
        }
    }

    public void startExpAnimation(float newExpRatio, boolean levelUp) {
        oldState.expRatio = levelUp ? 0 : state.expRatio;
        state.expRatio = newExpRatio;
        animationExp = (int)(100*Math.abs(oldState.expRatio - state.expRatio)* BattleView.FRAMES_PER_HP_LOSS);
    }

    public void setLevel(int newLevel) {
        state.level = newLevel;
    }

    public void setName(String newName) {
        state.name = newName;
    }

    public void setGender(Gender newGender) {
        state.gender = newGender;
    }

    public boolean isEmpty() {
        return state.imageNumber == 0;
    }

    public boolean isAnimationPlaying() {
        return animationHP != 0 || animationEvolve != 0 || animationCatch != 0 || animationExp != 0;
    }

    // Draws all of the text inside the status box
    public void drawStatusBoxText(Graphics g, int isEnemy, TileSet tiles, ActivePokemon pokemon) {
        // Name, Gender, Level, Status Condition
        DrawUtils.setFont(g, 27);
        DrawUtils.drawShadowText(g, state.name + " " + state.gender.getCharacter(), 20, 40, false);
        DrawUtils.drawShadowText(g, "Lv" + state.level, 272, 40, true);

        DrawUtils.setFont(g, 24);
        DrawUtils.drawShadowText(g, state.status.getName(), 20, 71, false);

        // Only the player shows the HP Text
        if (isEnemy == 0) {
            // HP Text Animation
            int originalTime = Math.abs(state.hp - oldState.hp)* BattleView.FRAMES_PER_HP_LOSS;
            String hpStr = state.hp + "/" + state.maxHp;
            if (animationHP > 0) {
                hpStr = (int)(state.hp + (oldState.hp - state.hp)*(animationHP/(float)originalTime)) + "/" + state.maxHp;
            }

            DrawUtils.setFont(g, 24);
            DrawUtils.drawShadowText(g, hpStr, 273, 95, true);
        }
        // Show whether or not the wild Pokemon has already been caught
        else if (state.caught) {
            g.drawImage(tiles.getTile(0x4), 296, 40, null);
        }
    }

    // TODO: Is this code duplicated in other places? Like the evolution view by any chance
    // Might want to include a helper class that contains a generic method for different types of animations
    private void catchAnimation(Graphics g, BufferedImage plyrImg, int isEnemy, TileSet pkmTiles, int px, int py) {
        Graphics2D g2d = (Graphics2D)g;
        float[] pokeyScales = { 1f, 1f, 1f, 1f };
        float[] pokeyOffsets = { 255f, 255f, 255f, 0f };
        float[] ballScales = { 1f, 1f, 1f, 1f };
        float[] ballOffsets = { 255f, 255f, 255f, 0f };

        int xOffset = 0;

        int lifespan = animationCatchDuration == -1 ? BattleView.CATCH_ANIMATION_LIFESPAN : animationCatchDuration;

        // Turn white
        if (animationCatch > lifespan - BattleView.CATCH_TRANSFORM_ANIMATION_LIFESPAN*.3) {
            pokeyOffsets[0] = pokeyOffsets[1] = pokeyOffsets[2] = 255*(1 - (animationCatch - (lifespan - BattleView.CATCH_TRANSFORM_ANIMATION_LIFESPAN*.3f))/(BattleView.CATCH_TRANSFORM_ANIMATION_LIFESPAN*(1 - .7f)));
            ballScales[3] = 0;
        }
        // Transform into Pokeball
        else if (animationCatch > lifespan - BattleView.CATCH_TRANSFORM_ANIMATION_LIFESPAN*.7) {
           pokeyOffsets[0] = pokeyOffsets[1] = pokeyOffsets[2] = 255;
           pokeyScales[3] = ((animationCatch - (lifespan - BattleView.CATCH_TRANSFORM_ANIMATION_LIFESPAN*0.7f))/(BattleView.CATCH_TRANSFORM_ANIMATION_LIFESPAN*(.7f - .3f)));
           ballOffsets[0] = ballOffsets[1] = ballOffsets[2] = 255;
           ballScales[3] = (1 - (animationCatch - (lifespan - BattleView.CATCH_TRANSFORM_ANIMATION_LIFESPAN*0.7f))/(BattleView.CATCH_TRANSFORM_ANIMATION_LIFESPAN*(.7f - .3f)));
        }
        // Restore color
        else if (animationCatch > lifespan - BattleView.CATCH_TRANSFORM_ANIMATION_LIFESPAN) {
            pokeyScales[3] = 0;
            ballOffsets[0] = ballOffsets[1] = ballOffsets[2] = 255*(animationCatch - (lifespan - BattleView.CATCH_TRANSFORM_ANIMATION_LIFESPAN))/(BattleView.CATCH_TRANSFORM_ANIMATION_LIFESPAN*(.3f));
        }
        // Shake
        else if (animationCatchDuration == -1 || animationCatch > BattleView.CATCH_TRANSFORM_ANIMATION_LIFESPAN) {
            pokeyScales[3] = 0;
            ballOffsets[0] = ballOffsets[1] = ballOffsets[2] = 0;
            xOffset = (int)(10*Math.sin(animationCatch/200.0));
        }
        // Turn white -- didn't catch
        else if (animationCatch > BattleView.CATCH_TRANSFORM_ANIMATION_LIFESPAN*.7) {
            ballOffsets[0] = ballOffsets[1] = ballOffsets[2] = 255*(1f - (animationCatch - BattleView.CATCH_TRANSFORM_ANIMATION_LIFESPAN*.7f)/(BattleView.CATCH_TRANSFORM_ANIMATION_LIFESPAN*(1 - 0.7f)));
            pokeyScales[3] = 0;
        }
        // Transform into Pokemon
        else if (animationCatch > BattleView.CATCH_TRANSFORM_ANIMATION_LIFESPAN*.3) {
            pokeyOffsets[0] = pokeyOffsets[1] = pokeyOffsets[2] = 255;
            pokeyScales[3] = (1 - (animationCatch - BattleView.CATCH_TRANSFORM_ANIMATION_LIFESPAN*0.3f)/(BattleView.CATCH_TRANSFORM_ANIMATION_LIFESPAN*(.7f - .3f)));
            ballOffsets[0] = ballOffsets[1] = ballOffsets[2] = 255;
            ballScales[3] = ((animationCatch - BattleView.CATCH_TRANSFORM_ANIMATION_LIFESPAN*0.3f)/(BattleView.CATCH_TRANSFORM_ANIMATION_LIFESPAN*(.7f - .3f)));
        }
        // Restore color
        else {
            ballScales[3] = 0;
            pokeyOffsets[0] = pokeyOffsets[1] = pokeyOffsets[2] = 255*(animationCatch)/(BattleView.CATCH_TRANSFORM_ANIMATION_LIFESPAN*(1.0f - .7f));
        }

        animationCatch -= Global.MS_BETWEEN_FRAMES;

        BufferedImage pkBall = pkmTiles.getTile(0x11111);

        g2d.drawImage(DrawUtils.colorImage(pkBall, ballScales, ballOffsets), px - pkBall.getWidth()/2 + xOffset, py - pkBall.getHeight(), null);
        g2d.drawImage(DrawUtils.colorImage(plyrImg, pokeyScales, pokeyOffsets), px - plyrImg.getWidth()/2, py - plyrImg.getHeight(), null);
    }

    // hi :)
    // TODO: is there any way to combine these?
    private void evolveAnimation(Graphics g, BufferedImage plyrImg, int isEnemy, TileSet pkmTiles, int px, int py) {
        Graphics2D g2d = (Graphics2D)g;

        float[] prevEvolutionScales = { 1f, 1f, 1f, 1f };
        float[] prevEvolutionOffsets = { 255f, 255f, 255f, 0f };
        float[] evolutionScales = { 1f, 1f, 1f, 1f };
        float[] evolutionOffsets = { 255f, 255f, 255f, 0f };

        // Turn white
        if (animationEvolve > BattleView.EVOLVE_ANIMATION_LIFESPAN*0.7) {
            prevEvolutionOffsets[0] = prevEvolutionOffsets[1] = prevEvolutionOffsets[2] = 255*(1 - (animationEvolve - BattleView.EVOLVE_ANIMATION_LIFESPAN*0.7f)/(BattleView.EVOLVE_ANIMATION_LIFESPAN*(1 - 0.7f)));
            evolutionScales[3] = 0;
        }
        // Change form
        else if (animationEvolve > BattleView.EVOLVE_ANIMATION_LIFESPAN*0.3) {
            prevEvolutionOffsets[0] = prevEvolutionOffsets[1] = prevEvolutionOffsets[2] = 255;
            prevEvolutionScales[3] = ((animationEvolve - BattleView.EVOLVE_ANIMATION_LIFESPAN*0.3f)/(BattleView.EVOLVE_ANIMATION_LIFESPAN*(0.7f - 0.3f)));
            evolutionOffsets[0] = evolutionOffsets[1] = evolutionOffsets[2] = 255;
            evolutionScales[3] = (1 - (animationEvolve - BattleView.EVOLVE_ANIMATION_LIFESPAN*0.3f)/(BattleView.EVOLVE_ANIMATION_LIFESPAN*(0.7f - 0.3f)));
        }
        // Restore color
        else {
            prevEvolutionScales[3] = 0;
            evolutionOffsets[0] = evolutionOffsets[1] = evolutionOffsets[2] = 255*(animationEvolve)/(BattleView.EVOLVE_ANIMATION_LIFESPAN*(1-0.7f));
        }

        animationEvolve -= Global.MS_BETWEEN_FRAMES;

        BufferedImage prevEvo = pkmTiles.getTile(oldState.imageNumber + (isEnemy^1));

        g2d.drawImage(DrawUtils.colorImage(plyrImg, evolutionScales, evolutionOffsets), px-plyrImg.getWidth()/2, py-plyrImg.getHeight(), null);
        g2d.drawImage(DrawUtils.colorImage(prevEvo, prevEvolutionScales, prevEvolutionOffsets), px-prevEvo.getWidth()/2, py-prevEvo.getHeight(), null);
    }

    private void drawHealthBar(Graphics g) {
        // Draw the white background of the health bar
        g.setColor(Color.WHITE);
        g.fillRect(108, 53, 315 - 150, 124 - 105);

        // Get the ratio based off of the possible animation
        float ratio = state.hp/(float)state.maxHp;
        if (animationHP > 0) {
            animationHP -= BattleView.HP_LOSS_RATIO*state.maxHp + 1;
            int originalTime = Math.abs(state.hp - oldState.hp)* BattleView.FRAMES_PER_HP_LOSS;
            ratio = (state.hp + (oldState.hp - state.hp)*(animationHP/(float)originalTime))/(float)state.maxHp;
        }
        else {
            animationHP = 0;
        }

        // Set the proper color for the ratio and fill in the health bar as appropriate
        g.setColor(DrawUtils.getHPColor(ratio));
        if (animationHP > 0 && (animationHP/10)%2 == 0) {
            g.setColor(g.getColor().darker());
        }

        g.fillRoundRect(113, 57, (int)((312 - 155)*ratio), 119 - 109, 5, 5);
    }

    private void drawExpBar(Graphics g) {
        // Show the animation
        float expRatio = state.expRatio;
        if (animationExp > 0) {
            animationExp -= BattleView.EXP_LOSS_RATIO;
            int originalTime = (int)(100*Math.abs(state.expRatio - oldState.expRatio)* BattleView.FRAMES_PER_HP_LOSS);
            expRatio = (state.expRatio + (oldState.expRatio - state.expRatio)*(animationExp/(float)originalTime));
        }
        else {
            animationExp = 0;
        }

        // Experience bar background
        g.setColor(new Color(153, 153, 153));
        g.fillRect(36, 107, 294 - 36, 115 - 107); //463,  304

        // Experience bar foreground
        g.setColor(DrawUtils.EXP_BAR_COLOR);
        g.fillRect(36, 107, (int)((294 - 36)*expRatio), 115 - 107);
    }

    // Draws the status box, not including the text
    public void drawStatusBox(Graphics g, int isEnemy, ActivePokemon pokemon, TileSet pkmTiles, int px, int py) { //-42 -52
        // Draw the colored type polygons
        Color[] typeColors = Type.getColors(state.type);
        g.setColor(typeColors[0]);
        g.fillPolygon(BattleView.primaryColorx[isEnemy], BattleView.primaryColory[isEnemy], 4);
        g.setColor(typeColors[1]);
        g.fillPolygon(BattleView.secondaryColorx[isEnemy], BattleView.secondaryColory[isEnemy], 4);

        // Draw health bar and player's EXP Bar
        drawHealthBar(g);
        if (isEnemy == 0) {
            drawExpBar(g);
        }

        // Draw the Pokemon image if applicable
        if (!isEmpty() && !pokemon.isSemiInvulnerable()) {
            BufferedImage plyrImg = pkmTiles.getTile(state.imageNumber + (isEnemy^1));
            if (plyrImg != null) {
                if (animationEvolve > 0) {
                    evolveAnimation(g, plyrImg, isEnemy, pkmTiles, px, py);
                }
                else if (animationCatch > 0) {
                    catchAnimation(g, plyrImg, isEnemy, pkmTiles, px, py);
                }
                else {
                    if (animationCatchDuration == -1) {
                        plyrImg = pkmTiles.getTile(0x11111);
                    }

                    g.drawImage(plyrImg, px - plyrImg.getWidth()/2, py - plyrImg.getHeight(), null); // TODO: Why is height not /2 -- can this use the centered image function?

                    animationEvolve = 0;
                    animationCatch = 0;
                }
            }
        }
    }
}
