package battle.effect.generic;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.CastSource;
import battle.effect.generic.EffectInterfaces.EndTurnEffect;
import battle.effect.generic.EffectInterfaces.PowerChangeEffect;
import battle.effect.generic.EffectInterfaces.SimpleStatModifyingEffect;
import battle.effect.generic.EffectInterfaces.StatusPreventionEffect;
import battle.effect.generic.EffectInterfaces.WeatherBlockerEffect;
import battle.effect.generic.EffectInterfaces.WeatherExtendingEffect;
import battle.effect.status.StatusCondition;
import message.Messages;
import pokemon.Stat;
import type.Type;

public abstract class Weather extends BattleEffect implements EndTurnEffect {
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_TURNS = 5;

    private final Type weatherElement;
    private final String imageName;

    public Weather(EffectNamesies namesies, Type weatherElement) {
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
        return DEFAULT_TURNS + WeatherExtendingEffect.getModifier(b, caster, this.namesies);
    }

    // EVERYTHING BELOW IS GENERATED ###

    /**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

    static class ClearSkies extends Weather {
        private static final long serialVersionUID = 1L;

        ClearSkies() {
            super(EffectNamesies.CLEAR_SKIES, Type.NORMAL);
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
        }
    }

    static class Raining extends Weather implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Raining() {
            super(EffectNamesies.RAINING, Type.WATER);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getWeather().namesies() == this.namesies);
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            Messages.add("The rain continues to pour.");
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

    static class Sunny extends Weather implements StatusPreventionEffect, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Sunny() {
            super(EffectNamesies.SUNNY, Type.FIRE);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getWeather().namesies() == this.namesies);
        }

        @Override
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            Messages.add("The sunlight is strong.");
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
        public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
            return status == StatusCondition.FROZEN;
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

    static class Sandstorm extends Weather implements SimpleStatModifyingEffect {
        private static final long serialVersionUID = 1L;

        private static final Type[] immunees = new Type[] { Type.ROCK, Type.GROUND, Type.STEEL };

        private void buffet(Battle b, ActivePokemon p) {
            // Don't buffet the immune!
            for (Type type : immunees) {
                if (p.isType(b, type)) {
                    return;
                }
            }

            // Srsly don't buffet the immune!!
            if (WeatherBlockerEffect.checkBlocked(b, p, this.namesies)) {
                return;
            }

            // Buffety buffety buffet
            Messages.add(p.getName() + " is buffeted by the sandstorm!");
            p.reduceHealthFraction(b, 1/16.0);
        }

        Sandstorm() {
            super(EffectNamesies.SANDSTORM, Type.ROCK);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getWeather().namesies() == this.namesies);
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
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            Messages.add("The sandstorm rages.");

            ActivePokemon other = b.getOtherPokemon(victim);
            buffet(b, victim);
            buffet(b, other);
        }

        @Override
        public double getModifier() {
            return 1.5;
        }
    }

    static class Hailing extends Weather {
        private static final long serialVersionUID = 1L;

        private static final Type[] immunees = new Type[] { Type.ICE };

        private void buffet(Battle b, ActivePokemon p) {
            // Don't buffet the immune!
            for (Type type : immunees) {
                if (p.isType(b, type)) {
                    return;
                }
            }

            // Srsly don't buffet the immune!!
            if (WeatherBlockerEffect.checkBlocked(b, p, this.namesies)) {
                return;
            }

            // Buffety buffety buffet
            Messages.add(p.getName() + " is buffeted by the hail!");
            p.reduceHealthFraction(b, 1/16.0);
        }

        Hailing() {
            super(EffectNamesies.HAILING, Type.ICE);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
            return !(b.getWeather().namesies() == this.namesies);
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
        public void applyEndTurn(ActivePokemon victim, Battle b) {
            Messages.add("The hail continues to fall.");

            ActivePokemon other = b.getOtherPokemon(victim);
            buffet(b, victim);
            buffet(b, other);
        }
    }
}
