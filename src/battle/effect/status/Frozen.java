package battle.effect.status;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.MoveType;
import battle.effect.CastSource;
import battle.effect.generic.EffectInterfaces.BeforeTurnEffect;
import battle.effect.generic.EffectInterfaces.TakeDamageEffect;
import message.Messages;
import type.Type;
import util.RandomUtils;

class Frozen extends Status implements BeforeTurnEffect, TakeDamageEffect {
    private static final long serialVersionUID = 1L;

    public Frozen() {
        super(StatusCondition.FROZEN);
    }

    // Ice-type Pokemon cannot be frozen
    @Override
    protected boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return !victim.isType(b, Type.ICE);
    }

    @Override
    public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
        // 20% chance to thaw out each turn
        if (RandomUtils.chanceTest(20) || p.getAttack().isMoveType(MoveType.DEFROST)) {
            Status.removeStatus(b, p, CastSource.EFFECT);

            return true;
        }

        Messages.add(p.getName() + " is frozen solid!");
        return false;
    }

    @Override
    public String getGenericCastMessage(ActivePokemon p) {
        return p.getName() + " was frozen!";
    }

    @Override
    public String getSourceCastMessage(ActivePokemon sourcerer, ActivePokemon victim, String sourceName) {
        return sourcerer.getName() + "'s " + sourceName + " froze " + victim.getName() + "!";
    }

    @Override
    public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
        // Fire-type moves defrost the user
        if (user.isAttackType(Type.FIRE)) {
            Status.removeStatus(b, victim, CastSource.EFFECT);
        }
    }

    @Override
    public String getGenericRemoveMessage(ActivePokemon victim) {
        return victim.getName() + " thawed out!";
    }

    @Override
    public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
        return victim.getName() + "'s " + sourceName + " thawed it out!";
    }
}
