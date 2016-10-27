package battle.effect.status;

import battle.Battle;
import battle.effect.StatChangingEffect;
import battle.effect.generic.EffectInterfaces.BeforeTurnEffect;
import main.Type;
import namesies.AbilityNamesies;
import pokemon.ActivePokemon;
import pokemon.Stat;

class Paralyzed extends Status implements BeforeTurnEffect, StatChangingEffect {
    private static final long serialVersionUID = 1L;

    public Paralyzed() {
        super(StatusCondition.PARALYZED);
    }

    // Electric-type Pokemon cannot be paralyzed
    public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return super.applies(b, caster, victim) && !victim.isType(b, Type.ELECTRIC);
    }

    public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
        if (Math.random()*100 < 25) {
            b.addMessage(p.getName() + " is fully paralyzed!");
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

    public int modify(Integer stat, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
        return (int)(stat*(s == Stat.SPEED && !p.hasAbility(AbilityNamesies.QUICK_FEET) ? .25 : 1));
    }

    public String getRemoveMessage(ActivePokemon victim) {
        return victim.getName() + " is no longer paralyzed!";
    }

    public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
        return victim.getName() + "'s " + sourceName + " cured it of its paralysis!";
    }
}
