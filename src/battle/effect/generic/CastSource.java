package battle.effect.generic;

import battle.ActivePokemon;
import battle.Battle;
import item.Item;
import main.Global;
import pokemon.ability.Ability;

public enum CastSource {
    ATTACK(false, (b, caster) -> caster.getAttack()),
    ABILITY(true, (b, caster) -> caster.getAbility()),
    HELD_ITEM(true, (b, caster) -> caster.getHeldItem(b)),
    CAST_SOURCE(false, (b, caster) -> caster.getCastSource()),
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

    public Object getSource(Battle b, ActivePokemon caster) {
        if (this.sourceGetter == null) {
            Global.error("Cannot get source for CastSource." + this.name() + ".");
            return caster;
        }

        return this.sourceGetter.getSource(b, caster);
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
            return ((Item)source).getName();
        } else if (source instanceof Ability) {
            return ((Ability)source).getName();
        } else {
            return null;
        }
    }

    @FunctionalInterface
    private interface SourceGetter {
        Object getSource(Battle b, ActivePokemon caster);
    }
}
