package map;

import battle.attack.Attack;
import battle.effect.generic.Effect;
import battle.effect.generic.PokemonEffect;
import battle.effect.status.StatusCondition;
import main.Global;
import main.Type;
import battle.attack.AttackNamesies;
import battle.effect.generic.EffectNamesies;
import pokemon.Stat;

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
    ELECTRIC(Type.ELECTRIC, AttackNamesies.THUNDERBOLT, StatusCondition.PARALYZED);

    private final Type type;
    private final Attack attack;

    private final StatusCondition status;
    private final int[] statChanges;
    private final List<Effect> effects;

    private int backgroundIndex;
    private int playerCircleIndex;
    private int opponentCircleIndex;

    TerrainType(Type type, AttackNamesies attack, Object effect) {
        this.type = type;
        this.attack = Attack.getAttack(attack);

        this.statChanges = new int[Stat.NUM_BATTLE_STATS];
        this.effects = new ArrayList<>();

        if (effect instanceof StatusCondition) {
            this.status = (StatusCondition)effect;
        }
        else {
            this.status = StatusCondition.NO_STATUS;

            if (effect instanceof Stat) {
                this.statChanges[((Stat)effect).index()] = -1;
            }
            else if (effect instanceof EffectNamesies) {
                this.effects.add(PokemonEffect.getEffect((EffectNamesies)effect));
            }
            else {
                Global.error("Invalid effect for terrain type " + this.name());
            }
        }

        this.backgroundIndex = 0x100 + this.ordinal();
        this.playerCircleIndex = 0x200 + this.ordinal();
        this.opponentCircleIndex = 0x300 + this.ordinal();
    }

    static {
        // TODO: Need Terrain images for misty and electric terrain -- use snow and sand for now (for no particular reason)
        // TODO: Once that's finished need to include final tags on all these variables
        MISTY.backgroundIndex = SNOW.backgroundIndex;
        ELECTRIC.backgroundIndex = SAND.backgroundIndex;

        MISTY.playerCircleIndex = SNOW.playerCircleIndex;
        ELECTRIC.playerCircleIndex = SAND.playerCircleIndex;

        MISTY.opponentCircleIndex = SNOW.opponentCircleIndex;
        ELECTRIC.opponentCircleIndex = SAND.opponentCircleIndex;
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

    public List<Effect> getEffects() {
        return effects;
    }

    public int getBackgroundIndex() {
        return backgroundIndex;
    }

    public int getPlayerCircleIndex() {
        return playerCircleIndex;
    }

    public int getOpponentCircleIndex() {
        return opponentCircleIndex;
    }
}
