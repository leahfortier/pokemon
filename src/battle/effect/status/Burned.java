package battle.effect.status;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackNamesies;
import battle.effect.generic.EffectInterfaces.EndTurnEffect;
import battle.effect.generic.EffectInterfaces.StatModifyingEffect;
import message.Messages;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import type.Type;

class Burned extends Status implements EndTurnEffect, StatModifyingEffect {
    private static final long serialVersionUID = 1L;

    public Burned() {
        super(StatusCondition.BURNED);
    }

    @Override
    public void applyEndTurn(ActivePokemon victim, Battle b) {
        if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
            return;
        }

        Messages.add(victim.getName() + " was hurt by its burn!");
        victim.reduceHealthFraction(b, victim.hasAbility(AbilityNamesies.HEATPROOF) ? 1/16.0 : 1/8.0);
    }

    // Fire-type Pokemon cannot be burned
    @Override
    protected boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return !victim.isType(b, Type.FIRE);
    }

    @Override
    public String getCastMessage(ActivePokemon p) {
        return p.getName() + " was burned!";
    }

    @Override
    public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim) {
        return abilify.getName() + "'s " + abilify.getAbility().getName() + " burned " + victim.getName() + "!";
    }

    @Override
    public String getGenericRemoveMessage(ActivePokemon victim) {
        return victim.getName() + " is no longer burned!";
    }

    @Override
    public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
        return victim.getName() + "'s " + sourceName + " cured it of its burn!";
    }

    // Burn decreases attack by 50%
    @Override
    public double modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s) {
        return s == Stat.ATTACK && !p.hasAbility(AbilityNamesies.GUTS) && p.getAttack().namesies() != AttackNamesies.FACADE ? .5 : 1;
    }
}
