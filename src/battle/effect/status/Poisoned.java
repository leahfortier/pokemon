package battle.effect.status;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.generic.EffectInterfaces.EndTurnEffect;
import battle.effect.generic.EffectNamesies;
import message.MessageUpdate;
import message.Messages;
import pokemon.ability.AbilityNamesies;
import type.Type;

class Poisoned extends Status implements EndTurnEffect {
    private static final long serialVersionUID = 1L;

    public Poisoned() {
        super(StatusCondition.POISONED);
    }

    @Override
    public boolean isType(StatusCondition statusCondition) {
        return statusCondition == StatusCondition.POISONED || statusCondition == StatusCondition.BADLY_POISONED;
    }

    @Override
    public void applyEndTurn(ActivePokemon victim, Battle b) {
        if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
            return;
        }

        if (victim.hasAbility(AbilityNamesies.POISON_HEAL)) {
            if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
                return;
            }

            victim.healHealthFraction(1/8.0);
            Messages.add(new MessageUpdate(victim.getName() + "'s " + AbilityNamesies.POISON_HEAL + " restored its health!").updatePokemon(b, victim));
            return;
        }

        Messages.add(victim.getName() + " was hurt by its poison!");
        victim.reduceHealthFraction(b, this.getTurns()/16.0);
    }

    // Regular poison reduces 2/16 = 1/8 hp
    protected int getTurns() {
        return 2;
    }

    // Poison-type and Steel-type Pokemon cannot be poisoned unless the caster has the Corrosion ability
    @Override
    protected boolean statusApplies(Battle b, ActivePokemon caster, ActivePokemon victim) {
        return (!victim.isType(b, Type.POISON) && !victim.isType(b, Type.STEEL) || caster.hasAbility(AbilityNamesies.CORROSION));
    }

    @Override
    public String getGenericCastMessage(ActivePokemon p) {
        return p.getName() + " was poisoned!";
    }

    @Override
    public String getSourceCastMessage(ActivePokemon sourcerer, ActivePokemon victim, String sourceName) {
        return sourcerer.getName() + "'s " + sourceName + " poisoned " + victim.getName() + "!";
    }

    @Override
    public String getGenericRemoveMessage(ActivePokemon victim) {
        return victim.getName() + " is no longer poisoned!";
    }

    @Override
    public String getSourceRemoveMessage(ActivePokemon victim, String sourceName) {
        return victim.getName() + "'s " + sourceName + " cured it of its poison!";
    }
}
