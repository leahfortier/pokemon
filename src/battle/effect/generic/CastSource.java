package battle.effect.generic;

import battle.Battle;
import main.Global;
import pokemon.ActivePokemon;

public enum CastSource {
    ATTACK,
    ABILITY,
    HELD_ITEM,
    USE_ITEM,
    EFFECT;

    public Object getSource(Battle b, ActivePokemon caster) {
        switch (this) {
            case ATTACK:
                return caster.getAttack();
            case ABILITY:
                return caster.getAbility();
            case HELD_ITEM:
                return caster.getHeldItem(b);
            default:
                Global.error("Cannot get source for CastSource." + this.name() + ".");
                return null;
        }
    }
}
