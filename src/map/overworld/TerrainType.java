package map.overworld;

import battle.attack.AttackNamesies;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.status.StatusNamesies;
import main.Game;
import pokemon.stat.Stat;
import type.Type;
import util.string.StringUtils;

import java.awt.Color;
import java.awt.image.BufferedImage;

public enum TerrainType {
    BUILDING(Type.NORMAL, new Color(232, 243, 248), AttackNamesies.TRI_ATTACK, StatusNamesies.PARALYZED),
    CAVE(Type.ROCK, new Color(192, 169, 104), AttackNamesies.POWER_GEM, PokemonEffectNamesies.FLINCH),
    SAND(Type.GROUND, new Color(248, 234, 204), AttackNamesies.EARTH_POWER, Stat.ACCURACY),
    WATER(Type.WATER, new Color(221, 240, 248), AttackNamesies.HYDRO_PUMP, Stat.ATTACK),
    SNOW(Type.ICE, new Color(245, 239, 246), AttackNamesies.FROST_BREATH, StatusNamesies.FROZEN),
    ICE(Type.ICE, new Color(228, 249, 240), AttackNamesies.ICE_BEAM, StatusNamesies.FROZEN),
    GRASS(Type.GRASS, new Color(224, 247, 224), AttackNamesies.ENERGY_BALL, StatusNamesies.ASLEEP),
    MISTY(Type.FAIRY, new Color(255, 231, 233), AttackNamesies.MOONBLAST, Stat.SP_ATTACK),
    ELECTRIC(Type.ELECTRIC, new Color(250, 250, 210), AttackNamesies.THUNDERBOLT, StatusNamesies.PARALYZED),
    PSYCHIC(Type.PSYCHIC, new Color(216, 191, 216), AttackNamesies.PSYCHIC, Stat.SPEED);

    private final Type type;
    private final Color color;
    private final String imageName;

    private final AttackNamesies attack;
    private final StatusNamesies status;
    private final int[] statChanges;
    private final PokemonEffectNamesies effect;

    TerrainType(Type type, Color color, AttackNamesies attack, StatusNamesies statusCondition) {
        this(type, color, attack, statusCondition, null, null);
    }

    TerrainType(Type type, Color color, AttackNamesies attack, Stat toLower) {
        this(type, color, attack, StatusNamesies.NO_STATUS, toLower, null);
    }

    TerrainType(Type type, Color color, AttackNamesies attack, PokemonEffectNamesies effect) {
        this(type, color, attack, StatusNamesies.NO_STATUS, null, effect);
    }

    TerrainType(Type type, Color color, AttackNamesies attack, StatusNamesies statusCondition, Stat toLower, PokemonEffectNamesies effect) {
        this.type = type;
        this.color = color;
        this.imageName = StringUtils.properCase(this.name().toLowerCase()) + "Circle";

        this.attack = attack;

        this.status = statusCondition;
        this.statChanges = new int[Stat.NUM_BATTLE_STATS];
        this.effect = effect;

        if (toLower != null) {
            this.statChanges[toLower.index()] = -1;
        }
    }

    public Type getType() {
        return type;
    }

    public Color getColor() {
        return color;
    }

    public AttackNamesies getAttack() {
        return attack;
    }

    public StatusNamesies getStatusCondition() {
        return status;
    }

    public int[] getStatChanges() {
        return statChanges;
    }

    public PokemonEffectNamesies getEffect() {
        return effect;
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
