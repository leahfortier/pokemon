package map.overworld;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.effect.generic.EffectNamesies;
import battle.effect.status.StatusCondition;
import main.Game;
import pokemon.Stat;
import type.Type;
import util.StringUtils;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public enum TerrainType {
    GRASS(Type.GRASS, new Color(224, 247, 224), AttackNamesies.ENERGY_BALL, StatusCondition.ASLEEP),
    BUILDING(Type.NORMAL, new Color(232, 243, 248), AttackNamesies.TRI_ATTACK, StatusCondition.PARALYZED),
    CAVE(Type.ROCK, new Color(192, 169, 104), AttackNamesies.POWER_GEM, EffectNamesies.FLINCH),
    SAND(Type.GROUND, new Color(248, 234, 204), AttackNamesies.EARTH_POWER, Stat.ACCURACY),
    WATER(Type.WATER, new Color(221, 240, 248), AttackNamesies.HYDRO_PUMP, Stat.ATTACK),
    SNOW(Type.ICE, new Color(245, 239, 246), AttackNamesies.FROST_BREATH, StatusCondition.FROZEN),
    ICE(Type.ICE, new Color(228, 249, 240), AttackNamesies.ICE_BEAM, StatusCondition.FROZEN),
    MISTY(Type.FAIRY, new Color(255, 231, 233), AttackNamesies.MOONBLAST, Stat.SP_ATTACK),
    ELECTRIC(Type.ELECTRIC, new Color(250, 250, 210), AttackNamesies.THUNDERBOLT, StatusCondition.PARALYZED),
    PSYCHIC(Type.PSYCHIC, new Color(216, 191, 216), AttackNamesies.PSYCHIC, Stat.SP_DEFENSE); // TODO: Don't have this information yet so I made this up

    private final Type type;
    private final Color color;
    private final String imageName;

    private final Attack attack;
    private final StatusCondition status;
    private final int[] statChanges;
    private final List<EffectNamesies> effects;

    TerrainType(Type type, Color color, AttackNamesies attack, StatusCondition statusCondition) {
        this(type, color, attack, statusCondition, null, null);
    }

    TerrainType(Type type, Color color, AttackNamesies attack, Stat toLower) {
        this(type, color, attack, StatusCondition.NO_STATUS, toLower, null);
    }

    TerrainType(Type type, Color color, AttackNamesies attack, EffectNamesies effect) {
        this(type, color, attack, StatusCondition.NO_STATUS, null, effect);
    }

    TerrainType(Type type, Color color, AttackNamesies attack, StatusCondition statusCondition, Stat toLower, EffectNamesies effect) {
        this.type = type;
        this.color = color;
        this.imageName = StringUtils.properCase(this.name().toLowerCase()) + "Circle";

        this.attack = attack.getAttack();

        this.status = statusCondition;
        this.statChanges = new int[Stat.NUM_BATTLE_STATS];
        this.effects = new ArrayList<>();

        if (toLower != null) {
            this.statChanges[toLower.index()] = -1;
        }

        if (effect != null) {
            this.effects.add(effect);
        }
    }

    public Type getType() {
        return type;
    }

    public Color getColor() {
        return color;
    }

    public Attack getAttack() {
        return attack;
    }

    public StatusCondition getStatusCondition() {
        return status;
    }

    public int[] getStatChanges() {
        return statChanges;
    }

    public List<EffectNamesies> getEffects() {
        return effects;
    }

    public BufferedImage getPlayerCircleImage() {
        return Game.getData().getPlayerTerrainTiles().getTile(this.imageName);
    }

    public BufferedImage getOpponentCircleImage() {
        return Game.getData().getOpponentTerrainTiles().getTile(this.imageName);
    }

    public boolean isInterior() {
        // Make this a field
        return this == BUILDING;
    }
}
