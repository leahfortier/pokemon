package battle.effect;

import battle.ActivePokemon;
import battle.Battle;
import util.serialization.Serializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EffectList<NamesiesType extends EffectNamesies, EffectType extends Effect<? extends NamesiesType>>
        implements Iterable<EffectType>, Serializable {
    private static final long serialVersionUID = 1L;

    private final List<EffectType> effects;

    public EffectList() {
        this.effects = new ArrayList<>();
    }

    public List<EffectType> asList() {
        return new ArrayList<>(this.effects);
    }

    public void reset() {
        effects.clear();
    }

    public void add(EffectType effect) {
        this.effects.add(effect);
    }

    // Returns the effect if it is in the list, otherwise returns null
    public EffectType get(NamesiesType effectNamesies) {
        for (EffectType effect : this) {
            if (effect.namesies() == effectNamesies) {
                return effect;
            }
        }

        return null;
    }

    public boolean hasEffect(NamesiesType effect) {
        return get(effect) != null;
    }

    // Private and should really only be called from the decrement method
    // Whenever possible, effect.deactivate() is preferable
    protected void remove(EffectType effect) {
        this.effects.remove(effect);
    }

    public boolean remove(NamesiesType effectToRemove) {
        return this.effects.removeIf(effect -> effect.namesies() == effectToRemove);
    }

    public void decrement(Battle b, ActivePokemon p) {
        for (EffectType effect : this) {
            boolean active = effect.isActive();
            if (active) {
                effect.decrement(b, p);
                if (!effect.isActive()) {
                    // Naturally subside from decrement
                    effect.subside(b, p);
                    active = false;
                }
            }

            if (!active) {
                this.remove(effect);

                // I think this is pretty much just for Future Sight...
                if (p != null && p.isFainted(b)) {
                    break;
                }
            }
        }
    }

    @Override
    public Iterator<EffectType> iterator() {
        // Important to use this.asList() to avoid ConcurrentModificationExceptions since decrement/subside can remove effects
        return this.asList().iterator();
    }
}
