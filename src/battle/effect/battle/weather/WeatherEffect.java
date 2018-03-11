package battle.effect.battle.weather;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.CastSource;
import battle.effect.EffectInterfaces.BattleEndTurnEffect;
import battle.effect.EffectInterfaces.PowerChangeEffect;
import battle.effect.EffectInterfaces.SimpleStatModifyingEffect;
import battle.effect.EffectInterfaces.StatusPreventionEffect;
import battle.effect.EffectInterfaces.WeatherBlockerEffect;
import battle.effect.EffectInterfaces.WeatherExtendingEffect;
import battle.effect.battle.BattleEffect;
import battle.effect.status.StatusNamesies;
import message.Messages;
import pokemon.Stat;
import type.Type;

public abstract class WeatherEffect extends BattleEffect<WeatherNamesies> implements BattleEndTurnEffect {
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_TURNS = 5;

    private final Type weatherElement;
    private final String imageName;

    public WeatherEffect(WeatherNamesies namesies, Type weatherElement) {
        super(namesies, -1, -1, true, false);
        this.weatherElement = weatherElement;
        this.imageName = this.getClass().getSimpleName().toLowerCase();
    }

    public String getImageName() {
        return this.imageName;
    }

    public Type getElement() {
        return weatherElement;
    }

    @Override
    protected void afterCast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
        b.getWeather().setTurns(getTurns(b, caster));
    }

    private int getTurns(Battle b, ActivePokemon caster) {
        return DEFAULT_TURNS + WeatherExtendingEffect.getModifier(b, caster, this.namesies());
    }

    // EVERYTHING BELOW IS GENERATED ###

    /**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

    static class ClearSkies extends WeatherEffect {
        private static final long serialVersionUID = 1L;

        ClearSkies() {
            super(WeatherNamesies.CLEAR_SKIES, Type.NORMAL);
        }
    }

    static class Raining extends WeatherEffect implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Raining() {
            super(WeatherNamesies.RAINING, Type.WATER);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.hasEffect(this.namesies));
        }

        @Override
        public String getEndTurnMessage(Battle b) {
            return "The rain continues to pour.";
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "It started to rain!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The rain stopped.";
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.isAttackType(Type.WATER)) {
                // Water is fiddy percent stronger in tha weathz
                return 1.5;
            } else if (user.isAttackType(Type.FIRE)) {
                // Fire is fiddy percent weaker in tha weathz
                return .5;
            } else {
                return 1;
            }
        }
    }

    static class Sunny extends WeatherEffect implements StatusPreventionEffect, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Sunny() {
            super(WeatherNamesies.SUNNY, Type.FIRE);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.hasEffect(this.namesies));
        }

        @Override
        public String getEndTurnMessage(Battle b) {
            return "The sunlight is strong.";
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "The sunlight turned harsh!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The sunlight faded.";
        }

        @Override
        public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies status) {
            return status == StatusNamesies.FROZEN;
        }

        @Override
        public String statusPreventionMessage(ActivePokemon victim) {
            return "Too sunny to freeze!!";
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.isAttackType(Type.FIRE)) {
                // Fire is fiddy percent stronger in tha weathz
                return 1.5;
            } else if (user.isAttackType(Type.WATER)) {
                // Water is fiddy percent weaker in tha weathz
                return .5;
            } else {
                return 1;
            }
        }
    }

    static class Sandstorm extends WeatherEffect implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        private static final Type[] immunees = new Type[] { Type.ROCK, Type.GROUND, Type.STEEL };

        Sandstorm() {
            super(WeatherNamesies.SANDSTORM, Type.ROCK);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.hasEffect(this.namesies));
        }

        @Override
        public String getEndTurnMessage(Battle b) {
            return "The sandstorm rages.";
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "A sandstorm kicked up!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The sandstorm subsided.";
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.isType(b, Type.ROCK);
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SP_DEFENSE;
        }

        @Override
        public void singleEndTurnEffect(Battle b, ActivePokemon victim) {
            // Don't buffet the immune!
            for (Type type : immunees) {
                if (victim.isType(b, type)) {
                    return;
                }
            }

            // Srsly don't buffet the immune!!
            if (WeatherBlockerEffect.checkBlocked(b, victim, this.namesies())) {
                return;
            }

            // Buffety buffety buffet
            Messages.add(victim.getName() + " is buffeted by the sandstorm!");
            victim.reduceHealthFraction(b, 1/16.0);
        }

        @Override
        public double getModifier() {
            return 1.5;
        }
    }

    static class Hailing extends WeatherEffect {
        private static final long serialVersionUID = 1L;

        private static final Type[] immunees = new Type[] { Type.ICE };

        Hailing() {
            super(WeatherNamesies.HAILING, Type.ICE);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.hasEffect(this.namesies));
        }

        @Override
        public String getEndTurnMessage(Battle b) {
            return "The hail continues to fall.";
        }

        @Override
        public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim, CastSource source) {
            return "It started to hail!";
        }

        @Override
        public String getSubsideMessage(ActivePokemon victim) {
            return "The hail stopped.";
        }

        @Override
        public void singleEndTurnEffect(Battle b, ActivePokemon victim) {
            // Don't buffet the immune!
            for (Type type : immunees) {
                if (victim.isType(b, type)) {
                    return;
                }
            }

            // Srsly don't buffet the immune!!
            if (WeatherBlockerEffect.checkBlocked(b, victim, this.namesies())) {
                return;
            }

            // Buffety buffety buffet
            Messages.add(victim.getName() + " is buffeted by the hail!");
            victim.reduceHealthFraction(b, 1/16.0);
        }
    }
}
