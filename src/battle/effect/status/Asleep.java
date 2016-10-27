package battle.effect.status;

import battle.Battle;
import battle.MoveType;
import battle.effect.generic.Effect;
import battle.effect.generic.EffectInterfaces.BeforeTurnEffect;
import namesies.AbilityNamesies;
import namesies.EffectNamesies;
import pokemon.ActivePokemon;

class Asleep extends Status implements BeforeTurnEffect {
    private static final long serialVersionUID = 1L;
    private int numTurns;

    public Asleep() {
        super(StatusCondition.ASLEEP);
        this.numTurns = (int)(Math.random()*3) + 1;
    }

    protected void postCreateEffect(ActivePokemon victim) {
        if (victim.hasAbility(AbilityNamesies.EARLY_BIRD)) {
            this.numTurns /= 2;
        }
    }

    // No one can be asleep while Uproar is in effect by either Pokemon
    public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return super.applies(b, caster, victim) && !caster.hasEffect(EffectNamesies.UPROAR) && !victim.hasEffect(EffectNamesies.UPROAR);
    }

    public String getFailMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
        if (user.hasEffect(EffectNamesies.UPROAR) || victim.hasEffect(EffectNamesies.UPROAR)) {
            return "The uproar prevents sleep!";
        }

        return super.getFailMessage(b, user, victim);
    }

    public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
        if (numTurns == 0) {
            Status.removeStatus(b, p, Effect.CastSource.EFFECT);
            return true;
        }

        numTurns--;
        b.addMessage(p.getName() + " is fast asleep...");
        return p.getAttack().isMoveType(MoveType.ASLEEP_USER);
    }

    public String getCastMessage(ActivePokemon p)
    {
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
