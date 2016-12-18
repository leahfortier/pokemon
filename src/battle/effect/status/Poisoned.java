package battle.effect.status;

import battle.Battle;
import battle.effect.generic.EffectInterfaces.EndTurnEffect;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.PokemonEffect;
import main.Type;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.ability.AbilityNamesies;

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
            Messages.add(new MessageUpdate(victim.getName() + "'s " + AbilityNamesies.POISON_HEAL + " restored its health!").updatePokemon(b, victim));
            return;
        }

        PokemonEffect badPoison = victim.getEffect(EffectNamesies.BAD_POISON);
        Messages.add(new MessageUpdate(victim.getName() + " was hurt by its poison!"));
        victim.reduceHealthFraction(b, badPoison == null ? 1/8.0 : badPoison.getTurns()/16.0);
    }

    // Poison-type and Steel-type Pokemon cannot be poisoned
    @Override
    protected boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return !victim.isType(b, Type.POISON) && !victim.isType(b, Type.STEEL);
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