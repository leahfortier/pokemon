package battle.effect.status;

import battle.Battle;
import battle.effect.generic.EffectInterfaces.EndTurnEffect;
import battle.effect.generic.EffectInterfaces.StatChangingEffect;
import main.Type;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;

class Burned extends Status implements EndTurnEffect, StatChangingEffect {
    private static final long serialVersionUID = 1L;

    public Burned() {
        super(StatusCondition.BURNED);
    }

    public void applyEndTurn(ActivePokemon victim, Battle b) {
        if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
            return;
        }

        Messages.add(new MessageUpdate(victim.getName() + " was hurt by its burn!"));
        victim.reduceHealthFraction(b, victim.hasAbility(AbilityNamesies.HEATPROOF) ? 1/16.0 : 1/8.0);
    }

    // Fire-type Pokemon cannot be burned
    @Override
    protected boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return !victim.isType(b, Type.FIRE);
    }

    public String getCastMessage(ActivePokemon p) {
        return p.getName() + " was burned!";
    }

    public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim) {
        return abilify.getName() + "'s " + abilify.getAbility().getName() + " burned " + victim.getName() + "!";
    }

    public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
        return (int)(stat*(s == Stat.ATTACK && !p.hasAbility(AbilityNamesies.GUTS) ? .5 : 1));
    }

    public String getRemoveMessage(ActivePokemon victim) {
        return victim.getName() + " is no longer burned!";
    }

    public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
        return victim.getName() + "'s " + sourceName + " cured it of its burn!";
    }
}