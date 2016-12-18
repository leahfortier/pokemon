package battle.effect.status;

import battle.Battle;
import battle.attack.MoveType;
import battle.effect.generic.CastSource;
import battle.effect.generic.EffectInterfaces.BeforeTurnEffect;
import battle.effect.generic.EffectNamesies;
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

    // TODO: This shouldn't be hard-coded
    // No one can be asleep while Uproar is in effect by either Pokemon
    @Override
    protected boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return !caster.hasEffect(EffectNamesies.UPROAR) && !victim.hasEffect(EffectNamesies.UPROAR);
    }

    public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
        if (user.hasEffect(EffectNamesies.UPROAR) || victim.hasEffect(EffectNamesies.UPROAR)) {
            return "The uproar prevents sleep!";
        }

        return super.getFailMessage(b, user, victim);
    }

    public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
        if (numTurns == 0) {
            Status.removeStatus(b, p, CastSource.EFFECT);
            return true;
        }

        numTurns--;
        Messages.add(new MessageUpdate(p.getName() + " is fast asleep..."));
        return p.getAttack().isMoveType(MoveType.ASLEEP_USER);
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

    public String getRemoveMessage(ActivePokemon victim) {
        return victim.getName() + " woke up!";
    }

    public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
        return victim.getName() + "'s " + sourceName + " caused it to wake up!";
    }
}
