package battle.effect.status;

import battle.Battle;
import battle.effect.EndTurnEffect;
import battle.effect.generic.PokemonEffect;
import namesies.Namesies;
import main.Type;
import pokemon.ActivePokemon;

class Poisoned extends Status implements EndTurnEffect {
    private static final long serialVersionUID = 1L;

    public Poisoned() {
        super(StatusCondition.POISONED);
    }

    public void applyEndTurn(ActivePokemon victim, Battle b) {
        if (victim.hasAbility(Namesies.MAGIC_GUARD_ABILITY)) {
            return;
        }

        if (victim.hasAbility(Namesies.POISON_HEAL_ABILITY)) {
            if  (victim.fullHealth() || victim.hasEffect(Namesies.HEAL_BLOCK_EFFECT)) {
                return;
            }

            victim.healHealthFraction(1/8.0);
            b.addMessage(victim.getName() + "'s " + Namesies.POISON_HEAL_ABILITY + " restored its health!", victim);
            return;
        }

        PokemonEffect badPoison = victim.getEffect(Namesies.BAD_POISON_EFFECT);
        b.addMessage(victim.getName() + " was hurt by its poison!");
        victim.reduceHealthFraction(b, badPoison == null ? 1/8.0 : badPoison.getTurns()/16.0);
    }

    // Poison-type Pokemon cannot be poisoned
    public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return super.applies(b, caster, victim) && !victim.isType(b, Type.POISON);
    }

    public String getCastMessage(ActivePokemon p) {
        return p.getName() + " was " + (p.hasEffect(Namesies.BAD_POISON_EFFECT) ? "badly " : "") + "poisoned!";
    }

    public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim) {
        return abilify.getName() + "'s " + abilify.getAbility().getName() + (victim.hasEffect(Namesies.BAD_POISON_EFFECT) ? " badly " : " ") + "poisoned " + victim.getName() + "!";
    }

    public String getRemoveMessage(ActivePokemon victim) {
        return victim.getName() + " is no longer poisoned!";
    }

    public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
        return victim.getName() + "'s " + sourceName + " cured it of its poison!";
    }
}