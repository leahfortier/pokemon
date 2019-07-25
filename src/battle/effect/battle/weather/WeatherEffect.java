package battle.effect.battle.weather;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.ApplyResult;
import battle.effect.EffectInterfaces.SimpleStatModifyingEffect;
import battle.effect.InvokeInterfaces.BattleEndTurnEffect;
import battle.effect.InvokeInterfaces.PowerChangeEffect;
import battle.effect.InvokeInterfaces.StatusPreventionEffect;
import battle.effect.InvokeInterfaces.WeatherBlockerEffect;
import battle.effect.battle.BattleEffect;
import battle.effect.source.CastSource;
import battle.effect.status.StatusNamesies;
import pokemon.Stat;
import type.Type;

public abstract class WeatherEffect extends BattleEffect<WeatherNamesies> implements BattleEndTurnEffect {
    private static final long serialVersionUID = 1L;

    private final Type weatherElement;
    private final String imageName;

    public WeatherEffect(WeatherNamesies namesies, Type weatherElement, boolean canHave) {
        super(namesies, -1, -1, canHave, false);
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
        // Weather needs to set its turns in the cast method because base weather has infinite turns
        b.getWeather().setTurns(5);
    }

    @Override
    public boolean endTurnSubsider() {
        return true;
    }

    // EVERYTHING BELOW IS GENERATED ###

    /**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

    static class ClearSkies extends WeatherEffect {
        private static final long serialVersionUID = 1L;

        ClearSkies() {
            super(WeatherNamesies.CLEAR_SKIES, Type.NORMAL, true);
        }
    }

    static class Raining extends WeatherEffect implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Raining() {
            super(WeatherNamesies.RAINING, Type.WATER, false);
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
            super(WeatherNamesies.SUNNY, Type.FIRE, false);
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
        public ApplyResult preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusNamesies status) {
            // Can't freeze in the sunlight
            if (status == StatusNamesies.FROZEN) {
                return ApplyResult.failure("Too sunny to freeze!!");
            }

            return ApplyResult.success();
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
            super(WeatherNamesies.SANDSTORM, Type.ROCK, false);
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
            victim.reduceHealthFraction(b, 1/16.0, victim.getName() + " is buffeted by the sandstorm!");
        }

        @Override
        public boolean isModifyStat(Stat s) {
            return s == Stat.SP_DEFENSE;
        }

        @Override
        public boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
            return p.isType(b, Type.ROCK);
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
            super(WeatherNamesies.HAILING, Type.ICE, false);
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
        public String getEndTurnMessage(Battle b) {
            return "The hail continues to fall.";
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
            victim.reduceHealthFraction(b, 1/16.0, victim.getName() + " is buffeted by the hail!");
        }
    }
}
