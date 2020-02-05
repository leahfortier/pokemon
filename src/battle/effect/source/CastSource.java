package battle.effect.source;

import battle.ActivePokemon;
import item.Item;
import main.Global;
import pokemon.ability.Ability;

public enum
CastSource {
    ATTACK(ActivePokemon::getAttack),
    ABILITY(ActivePokemon::getAbility, source -> ((Ability)source).getName()),
    HELD_ITEM(ActivePokemon::getHeldItem, source -> ((Item)source).getName()),
    CAST_SOURCE(ActivePokemon::getCastSource),
    USE_ITEM,
    EFFECT;

    private final SourceGetter sourceGetter;
    private final SourceNameGetter sourceNameGetter;

    CastSource() {
        this(null);
    }

    CastSource(SourceGetter sourceGetter) {
        this(sourceGetter, null);
    }

    CastSource(SourceGetter sourceGetter, SourceNameGetter sourceNameGetter) {
        this.sourceGetter = sourceGetter;
        this.sourceNameGetter = sourceNameGetter;
    }

    public Object getSource(ActivePokemon caster) {
        if (this.sourceGetter == null) {
            Global.error("Cannot get source for CastSource." + this.name() + ".");
            return caster;
        }

        return this.sourceGetter.getSource(caster);
    }

    public boolean hasSourceName() {
        return this.sourceNameGetter != null;
    }

    public String getSourceName(ActivePokemon caster) {
        if (!this.hasSourceName()) {
            return null;
        }

        Object source = this.getSource(caster);
        return this.sourceNameGetter.getSourceName(source);
    }

    @FunctionalInterface
    private interface SourceGetter {
        Object getSource(ActivePokemon caster);
    }

    @FunctionalInterface
    private interface SourceNameGetter {
        String getSourceName(Object source);
    }
}
