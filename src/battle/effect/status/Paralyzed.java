package battle.effect.status;

import battle.Battle;
import battle.effect.generic.EffectInterfaces.BeforeTurnEffect;
import battle.effect.generic.EffectInterfaces.StatChangingEffect;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.Stat;
import pokemon.ability.AbilityNamesies;
import type.Type;
import util.RandomUtils;

class Paralyzed extends Status implements BeforeTurnEffect, StatChangingEffect {
    private static final long serialVersionUID = 1L;

    public Paralyzed() {
        super(StatusCondition.PARALYZED);
    }

    // Electric-type Pokemon cannot be paralyzed
    @Override
    protected boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return !victim.isType(b, Type.ELECTRIC);
    }

    public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
        if (RandomUtils.chanceTest(25)) {
            Messages.add(p.getName() + " is fully paralyzed!");
            return false;
        }

        return true;
    }

    public String getCastMessage(ActivePokemon p) {
        return p.getName() + " was paralyzed!";
    }

    public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim) {
        return abilify.getName() + "'s " + abilify.getAbility().getName() + " paralyzed " + victim.getName() + "!";
    }

    public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
        return (int)(stat*(s == Stat.SPEED && !p.hasAbility(AbilityNamesies.QUICK_FEET) ? .25 : 1));
    }

    public String getGenericRemoveMessage(ActivePokemon victim) {
        return victim.getName() + " is no longer paralyzed!";
    }

    public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
        return victim.getName() + "'s " + sourceName + " cured it of its paralysis!";
    }
}
