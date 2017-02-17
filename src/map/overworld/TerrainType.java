package map.overworld;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.effect.generic.EffectNamesies;
import battle.effect.status.StatusCondition;
import main.Game;
import pokemon.Stat;
import type.Type;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public enum TerrainType {
    GRASS(Type.GRASS, AttackNamesies.ENERGY_BALL, StatusCondition.ASLEEP),
    BUILDING(Type.NORMAL, AttackNamesies.TRI_ATTACK, StatusCondition.PARALYZED),
    CAVE(Type.ROCK, AttackNamesies.POWER_GEM, EffectNamesies.FLINCH),
    SAND(Type.GROUND, AttackNamesies.EARTH_POWER, Stat.ACCURACY),
    WATER(Type.WATER, AttackNamesies.HYDRO_PUMP, Stat.ATTACK),
    SNOW(Type.ICE, AttackNamesies.FROST_BREATH, StatusCondition.FROZEN),
    ICE(Type.ICE, AttackNamesies.ICE_BEAM, StatusCondition.FROZEN),
    MISTY(Type.FAIRY, AttackNamesies.MOONBLAST, Stat.SP_ATTACK),
    ELECTRIC(Type.ELECTRIC, AttackNamesies.THUNDERBOLT, StatusCondition.PARALYZED),
    PSYCHIC(Type.PSYCHIC, AttackNamesies.PSYCHIC, Stat.SP_DEFENSE); // TODO: Don't have this information yet so I made this up

    private final Type type;
    private final Attack attack;

    private final StatusCondition status;
    private final int[] statChanges;
    private final List<EffectNamesies> effects;

    TerrainType(Type type, AttackNamesies attack, StatusCondition statusCondition) {
        this(type, attack, statusCondition, null, null);
    }

    TerrainType(Type type, AttackNamesies attack, Stat toLower) {
        this(type, attack, StatusCondition.NO_STATUS, toLower, null);
    }

    TerrainType(Type type, AttackNamesies attack, EffectNamesies effect) {
        this(type, attack, StatusCondition.NO_STATUS, null, effect);
    }

    TerrainType(Type type, AttackNamesies attack, StatusCondition statusCondition, Stat toLower, EffectNamesies effect) {
        this.type = type;
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

    private BufferedImage getImage(int baseIndex) {
        return Game.getData().getTerrainTiles().getTile(baseIndex + this.ordinal());
    }

    public BufferedImage getBackgroundImage() {
        return this.getImage(0x100);
    }

    public BufferedImage getPlayerCircleImage() {
        return this.getImage(0x200);
    }

    public BufferedImage getOpponentCircleImage() {
        return this.getImage(0x300);
    }
}
