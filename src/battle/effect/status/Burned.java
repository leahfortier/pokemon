package battle.effect.status;

import battle.Battle;
import battle.effect.EndTurnEffect;
import battle.effect.StatChangingEffect;
import namesies.Namesies;
import main.Type;
import pokemon.ActivePokemon;
import pokemon.Stat;

class Burned extends Status implements EndTurnEffect, StatChangingEffect {
    private static final long serialVersionUID = 1L;

    public Burned() {
        super(StatusCondition.BURNED);
    }

    public void applyEndTurn(ActivePokemon victim, Battle b) {
        if (victim.hasAbility(Namesies.MAGIC_GUARD_ABILITY)) {
            return;
        }

        b.addMessage(victim.getName() + " was hurt by its burn!");
        victim.reduceHealthFraction(b, victim.hasAbility(Namesies.HEATPROOF_ABILITY) ? 1/16.0 : 1/8.0);
    }

    // Fire-type Pokemon cannot be burned
    public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return super.applies(b, caster, victim) && !victim.isType(b, Type.FIRE);
    }

    public String getCastMessage(ActivePokemon p) {
        return p.getName() + " was burned!";
    }

    public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim) {
        return abilify.getName() + "'s " + abilify.getAbility().getName() + " burned " + victim.getName() + "!";
    }

    public int modify(Integer stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
        return (int)(stat*(s == Stat.ATTACK && !p.hasAbility(Namesies.GUTS_ABILITY) ? .5 : 1));
    }

    public String getRemoveMessage(ActivePokemon victim) {
        return victim.getName() + " is no longer burned!";
    }

    public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
        return victim.getName() + "'s " + sourceName + " cured it of its burn!";
    }
}