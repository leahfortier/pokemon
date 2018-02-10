package battle.effect;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.generic.Effect;
import battle.effect.generic.EffectNamesies;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class EffectList<EffectType extends Effect> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<EffectType> effects;

    public EffectList() {
        this.effects = new ArrayList<>();
    }

    public List<EffectType> listEffects() {
        return new ArrayList<>(this.effects);
    }

    public void reset() {
        effects.clear();
    }

    public boolean isEmpty() {
        return this.effects.isEmpty();
    }

    public void add(EffectType effect) {
        this.effects.add(effect);
    }

    // Returns the effect if it is in the list, otherwise returns null
    public EffectType get(EffectNamesies effectNamesies) {
        for (EffectType effect : effects) {
            if (effect.namesies() == effectNamesies) {
                return effect;
            }
        }

        return null;
    }

    public boolean hasEffect(EffectNamesies effect) {
        return get(effect) != null;
    }

    public void remove(EffectType effect) {
        this.effects.remove(effect);
    }

    public boolean remove(EffectNamesies effectToRemove) {
        return this.removeIf(effect -> effect.namesies() == effectToRemove);
    }

    public boolean removeIf(Predicate<EffectType> filter) {
        return this.effects.removeIf(filter);
    }

    public void decrement(Battle b, ActivePokemon p) {
        for (EffectType effect : this.listEffects()) {
            boolean inactive = !effect.isActive();
            if (!inactive) {
                effect.decrement(b, p);
                inactive = !effect.isActive() && !effect.nextTurnSubside();
            }

            if (inactive) {
                this.remove(effect);
                effect.subside(b, p);

                // I think this is pretty much just for Future Sight...
                if (p != null && p.isFainted(b)) {
                    return;
                }
            }
        }
    }
}
