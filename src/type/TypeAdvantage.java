package type;

import battle.Battle;
import battle.effect.generic.EffectInterfaces.AdvantageMultiplierMove;
import battle.effect.generic.EffectInterfaces.NoAdvantageChanger;
import pokemon.ActivePokemon;
import pokemon.ability.AbilityNamesies;

import java.util.EnumMap;
import java.util.Map;

public enum TypeAdvantage {
    NORMAL(new Builder()
            .weak(Type.ROCK, Type.STEEL)
            .ineffective(Type.GHOST)),
    FIRE(new Builder()
            .strong(Type.GRASS, Type.ICE, Type.BUG, Type.STEEL)
            .weak(Type.FIRE, Type.WATER, Type.ROCK, Type.DRAGON)),
    WATER(new Builder()
            .strong(Type.FIRE, Type.GROUND, Type.ROCK)
            .weak(Type.WATER, Type.GRASS, Type.DRAGON)),
    ELECTRIC(new Builder()
            .strong(Type.WATER, Type.FLYING)
            .weak(Type.ELECTRIC, Type.GRASS, Type.DRAGON)
            .ineffective(Type.GROUND)),
    GRASS(new Builder()
            .strong(Type.WATER, Type.GROUND, Type.ROCK)
            .weak(Type.FIRE, Type.GRASS, Type.POISON, Type.FLYING, Type.BUG, Type.DRAGON, Type.STEEL)),
    ICE(new Builder()
            .strong(Type.GRASS, Type.GROUND, Type.FLYING, Type.DRAGON)
            .weak(Type.FIRE, Type.WATER, Type.ICE, Type.STEEL)),
    FIGHTING(new Builder()
            .strong(Type.NORMAL, Type.ICE, Type.ROCK, Type.DARK, Type.STEEL)
            .weak(Type.POISON, Type.FLYING, Type.PSYCHIC, Type.BUG, Type.FAIRY)
            .ineffective(Type.GHOST)),
    POISON(new Builder()
            .strong(Type.GRASS, Type.FAIRY)
            .weak(Type.POISON, Type.GROUND, Type.ROCK, Type.GHOST)
            .ineffective(Type.STEEL)),
    GROUND(new Builder()
            .strong(Type.FIRE, Type.ELECTRIC, Type.POISON, Type.ROCK, Type.STEEL)
            .weak(Type.GRASS, Type.BUG)
            .ineffective(Type.FLYING)),
    FLYING(new Builder()
            .strong(Type.GRASS, Type.FIGHTING, Type.BUG)
            .weak(Type.ELECTRIC, Type.ROCK, Type.STEEL)),
    PSYCHIC(new Builder()
            .strong(Type.FIGHTING, Type.POISON)
            .weak(Type.PSYCHIC, Type.STEEL)
            .ineffective(Type.DARK)),
    BUG(new Builder()
            .strong(Type.GRASS, Type.PSYCHIC, Type.DARK)
            .weak(Type.FIRE, Type.FIGHTING, Type.POISON, Type.FLYING, Type.GHOST, Type.STEEL, Type.FAIRY)),
    ROCK(new Builder()
            .strong(Type.FIRE, Type.ICE, Type.FLYING, Type.BUG)
            .weak(Type.FIGHTING, Type.GROUND, Type.STEEL)),
    GHOST(new Builder()
            .strong(Type.PSYCHIC, Type.GHOST)
            .weak(Type.DARK)
            .ineffective(Type.NORMAL)),
    DRAGON(new Builder()
            .strong(Type.DRAGON)
            .weak(Type.STEEL)
            .ineffective(Type.FAIRY)),
    DARK(new Builder()
            .strong(Type.PSYCHIC, Type.GHOST)
            .weak(Type.FIGHTING, Type.DARK, Type.FAIRY)),
    STEEL(new Builder()
            .strong(Type.ICE, Type.ROCK, Type.FAIRY)
            .weak(Type.FIRE, Type.WATER, Type.ELECTRIC, Type.STEEL)),
    FAIRY(new Builder()
            .strong(Type.FIGHTING, Type.DRAGON, Type.DARK)
            .weak(Type.FIRE, Type.POISON, Type.STEEL)),
    NO_TYPE(new Builder());

    private final Type type;
    private final Map<Type, Double> advantageMap;

    TypeAdvantage(Builder builder) {
        this.type = Type.valueOf(this.name());
        this.advantageMap = builder.advantageMap;
    }

    public double getAdvantage(Type defending) {
        return advantageMap.get(defending);
    }

    public double getAdvantage(Battle b, ActivePokemon attacking, ActivePokemon defending, Type defendingType) {
        if (doesNotEffect(defendingType)
                && NoAdvantageChanger.checkNoAdvantageChanger(b, attacking, defending, this.type, defendingType)) {
            return 1;
        }

        return getAdvantage(defendingType);
    }

    public double getAdvantage(Type[] defending) {
        return this.getAdvantage(defending[0], defending[1]);
    }

    public double getAdvantage(Type firstType, Type secondType) {
        return getAdvantage(firstType)*getAdvantage(secondType);
    }

    public double getAdvantage(ActivePokemon defending, Battle b) {
        return getAdvantage(defending.getType(b));
    }

    public boolean isSuperEffective(ActivePokemon defending, Battle b) {
        return this.getAdvantage(defending, b) > 1;
    }

    // Also includes moves that do not effect at all
    public boolean isNotVeryEffective(Type defending) {
        return this.getAdvantage(defending) < 1;
    }

    public boolean doesNotEffect(Type defending) {
        return this.getAdvantage(defending) == 0;
    }

    public static double getAdvantage(ActivePokemon attacking, ActivePokemon defending, Battle b) {
        Type attackingType = attacking.getAttackType();
        TypeAdvantage typeAdvantage = attackingType.getAdvantage();

        Type[] defendingTypes = defending.getType(b);

        // Do special case check stupid things for fucking levitation which fucks everything up
        // Pokemon that are levitating cannot be hit by ground type moves
        if (attackingType == Type.GROUND && defending.isLevitatingWithoutTypeCheck(b, attacking)) {
            return 0;
        }

        double advantage = 1;

        // Go through each defending type and multiply its advantage
        for (Type defendingType : defendingTypes) {
            // For moves that are completely uneffective, check if there is an effect which negates this and don't multiply
            if (typeAdvantage.doesNotEffect(defendingType)
                    && NoAdvantageChanger.checkNoAdvantageChanger(b, attacking, defending, attackingType, defendingType)) {
                continue;
            }

            advantage *= typeAdvantage.getAdvantage(defendingType);
        }

        // TODO: Change this too
        // Get the advantage and apply any multiplier that may come from the attack
        advantage = AdvantageMultiplierMove.updateModifier(advantage, attacking, attackingType, defendingTypes);

        return advantage;
    }

    public static double getSTAB(Battle b, ActivePokemon p) {
        // Same type -- STAB
        if (p.isType(b, p.getAttackType())) {
            // The adaptability ability increases stab
            return p.hasAbility(AbilityNamesies.ADAPTABILITY) ? 2 : 1.5;
        }

        return 1;
    }

    private static class Builder {
        private final Map<Type, Double> advantageMap;

        Builder() {
            this.advantageMap = new EnumMap<>(Type.class);
            setAdvantage(1, Type.values());
        }

        Builder strong(Type... values) {
            return setAdvantage(2, values);
        }

        Builder weak(Type... values) {
            return setAdvantage(.5, values);
        }

        Builder ineffective(Type... values) {
            return setAdvantage(0, values);
        }

        private Builder setAdvantage(double advantage, Type... values) {
            for (Type type : values) {
                this.advantageMap.put(type, advantage);
            }

            return this;
        }
    }
}
