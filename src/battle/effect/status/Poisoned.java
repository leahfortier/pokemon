package battle.effect.status;

import battle.Battle;
import battle.effect.generic.EffectInterfaces.EndTurnEffect;
import battle.effect.generic.PokemonEffect;
import main.Type;
import namesies.AbilityNamesies;
import namesies.EffectNamesies;
import pokemon.ActivePokemon;

class Poisoned extends Status implements EndTurnEffect {
    private static final long serialVersionUID = 1L;

    public Poisoned() {
        super(StatusCondition.POISONED);
    }

    public void applyEndTurn(ActivePokemon victim, Battle b) {
        if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
            return;
        }

        if (victim.hasAbility(AbilityNamesies.POISON_HEAL)) {
            if  (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
                return;
            }

            victim.healHealthFraction(1/8.0);
            b.addMessage(victim.getName() + "'s " + AbilityNamesies.POISON_HEAL + " restored its health!", victim);
            return;
        }

        PokemonEffect badPoison = victim.getEffect(EffectNamesies.BAD_POISON);
        b.addMessage(victim.getName() + " was hurt by its poison!");
        victim.reduceHealthFraction(b, badPoison == null ? 1/8.0 : badPoison.getTurns()/16.0);
    }

    // Poison-type Pokemon cannot be poisoned
    public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return super.applies(b, caster, victim) && !victim.isType(b, Type.POISON);
    }

    public String getCastMessage(ActivePokemon p) {
        return p.getName() + " was " + (p.hasEffect(EffectNamesies.BAD_POISON) ? "badly " : "") + "poisoned!";
    }

    public String getAbilityCastMessage(ActivePokemon abilify, ActivePokemon victim) {
        return abilify.getName() + "'s " + abilify.getAbility().getName() + (victim.hasEffect(EffectNamesies.BAD_POISON) ? " badly " : " ") + "poisoned " + victim.getName() + "!";
    }

    public String getRemoveMessage(ActivePokemon victim) {
        return victim.getName() + " is no longer poisoned!";
    }

    public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
        return victim.getName() + "'s " + sourceName + " cured it of its poison!";
    }
}