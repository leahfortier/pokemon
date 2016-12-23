package battle.effect.generic;

import battle.Battle;
import item.Item;
import main.Global;
import pokemon.ActivePokemon;
import pokemon.ability.Ability;

public enum CastSource {
    ATTACK(false, (b, caster) -> caster.getAttack()),
    ABILITY(true, (b, caster) -> caster.getAbility()),
    HELD_ITEM(true, (b, caster) -> caster.getHeldItem(b)),
    USE_ITEM,
    EFFECT;

    private final boolean hasSourceName;
    private final SourceGetter sourceGetter;

    CastSource() {
        this(false, null);
    }

    CastSource(boolean hasSourceName, SourceGetter sourceGetter) {
        this.hasSourceName = hasSourceName;
        this.sourceGetter = sourceGetter;
    }

    private interface SourceGetter {
        Object getSource(Battle b, ActivePokemon caster);
    }

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

    public boolean hasSourceName() {
        return this.hasSourceName;
    }

    public String getSourceName(Battle b, ActivePokemon caster) {
        if (!this.hasSourceName()) {
            return null;
        }

        Object source = this.sourceGetter.getSource(b, caster);
        if (source instanceof Item) {
            return ((Item) source).getName();
        } else if (source instanceof Ability) {
            return ((Ability) source).getName();
        } else {
            return null;
        }
    }
}
