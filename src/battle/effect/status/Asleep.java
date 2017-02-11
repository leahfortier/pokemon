package battle.effect.status;

import battle.Battle;
import battle.effect.generic.CastSource;
import battle.effect.generic.EffectInterfaces.BeforeTurnEffect;
import battle.effect.generic.EffectInterfaces.SleepyFightsterEffect;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.ability.AbilityNamesies;
import util.RandomUtils;

class Asleep extends Status implements BeforeTurnEffect {
    private static final long serialVersionUID = 1L;
    private int numTurns;

    public Asleep() {
        super(StatusCondition.ASLEEP);
        this.numTurns = RandomUtils.getRandomInt(1, 3);
    }

    protected void postCreateEffect(ActivePokemon victim) {
        if (victim.hasAbility(AbilityNamesies.EARLY_BIRD)) {
            this.numTurns /= 2;
        }
    }

    // All Pokemon can get sleepy
    @Override
    protected boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return true;
    }

    public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
        if (numTurns == 0) {
            Status.removeStatus(b, p, CastSource.EFFECT);
            return true;
        }

        if (!p.hasAbility(AbilityNamesies.COMATOSE)) {
            numTurns--;
        }

        Messages.add(new MessageUpdate(p.getName() + " is fast asleep..."));
        return SleepyFightsterEffect.containsSleepyFightsterEffect(b, p);
    }

    public String getCastMessage(ActivePokemon p) {
        return p.getName() + " fell asleep!";
    }

    public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim) {
        return abilify.getName() + "'s " + abilify.getAbility().getName() + " caused " + victim.getName() + " to fall asleep!";
    }

    public void setTurns(int turns) {
        this.numTurns = turns;
    }

    public String getGenericRemoveMessage(ActivePokemon victim) {
        return victim.getName() + " woke up!";
    }

    public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
        return victim.getName() + "'s " + sourceName + " caused it to wake up!";
    }
}
