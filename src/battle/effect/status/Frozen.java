package battle.effect.status;

import battle.Battle;
import battle.MoveType;
import battle.effect.generic.Effect;
import battle.effect.generic.EffectInterfaces.BeforeTurnEffect;
import battle.effect.generic.EffectInterfaces.TakeDamageEffect;
import main.Global;
import main.Type;
import namesies.EffectNamesies;
import pokemon.ActivePokemon;

class Frozen extends Status implements BeforeTurnEffect, TakeDamageEffect {
    private static final long serialVersionUID = 1L;

    public Frozen() {
        super(StatusCondition.FROZEN);
    }

    // Ice-type Pokemon cannot be frozen and no one can frozen while sunny
    public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return super.applies(b, caster, victim) && !victim.isType(b, Type.ICE) && b.getWeather().namesies() != EffectNamesies.SUNNY;
    }

    public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
        // 20% chance to thaw out each turn
        if (Global.chanceTest(20) || p.getAttack().isMoveType(MoveType.DEFROST)) {
            Status.removeStatus(b, p, Effect.CastSource.EFFECT);

            return true;
        }

        b.addMessage(p.getName() + " is frozen solid!");
        return false;
    }

    public String getCastMessage(ActivePokemon p) {
        return p.getName() + " was frozen!";
    }

    public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim) {
        return abilify.getName() + "'s " + abilify.getAbility().getName() + " froze " + victim.getName() + "!";
    }

    public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
        // Fire-type moves defrost the user
        if (user.isAttackType(Type.FIRE)) {
            Status.removeStatus(b, victim, Effect.CastSource.EFFECT);
        }
    }

    public String getRemoveMessage(ActivePokemon victim) {
        return victim.getName() + " thawed out!";
    }

    public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
        return victim.getName() + "'s " + sourceName + " thawed it out!";
    }
}
