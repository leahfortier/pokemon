package battle;

import battle.attack.Move;
import battle.attack.MoveType;
import battle.effect.generic.Effect;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.PokemonEffect;
import pokemon.ability.AbilityNamesies;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BattleAttributes implements Serializable {
    private static final long serialVersionUID = 1L;

    private transient ActivePokemon attributesHolder;

    private List<PokemonEffect> effects;
    private Stages stages;
    private Move selected;
    private Move lastMoveUsed;
    private Serializable castSource;
    private int counter;
    private int damageTaken;
    private double successionDecayRate;
    private boolean firstTurn;
    private boolean attacking;
    private boolean reducePP;
    private boolean used;
    private boolean battleUsed;
    private boolean lastMoveSucceeded;

    public BattleAttributes(ActivePokemon attributesHolder) {
        this.attributesHolder = attributesHolder;

        effects = new ArrayList<>();
        stages = new Stages(attributesHolder);

        selected = null;
        lastMoveUsed = null;
        castSource = null;

        this.resetCount();
        this.resetDamageTaken();
        this.resetDecay();

        used = false;
        battleUsed = false;
        firstTurn = true;
        attacking = false;
        reducePP = false;
        lastMoveSucceeded = true;
    }

    public void setAttributesHolder(ActivePokemon attributesHolder) {
        this.attributesHolder = attributesHolder;
        this.stages.setAttributesHolder(attributesHolder);
    }

    public void setReducePP(boolean reduce) {
        reducePP = reduce;
    }

    public Object getCastSource() {
        return this.castSource;
    }

    public void setCastSource(Serializable castSource) {
        this.castSource = castSource;
    }

    public boolean isAttacking() {
        return attacking;
    }

    private void setAttacking(boolean isAttacking) {
        attacking = isAttacking;
    }

    void setLastMoveSucceeded(boolean lastMoveSucceeded) {
        this.lastMoveSucceeded = lastMoveSucceeded;
    }

    public boolean lastMoveSucceeded() {
        return this.lastMoveSucceeded;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean u) {
        used = u;
        if (used) {
            battleUsed = true;
        }
    }

    public boolean isBattleUsed() {
        return this.battleUsed;
    }

    public boolean isFirstTurn() {
        return firstTurn;
    }

    void setFirstTurn(boolean isFirstTurn) {
        firstTurn = isFirstTurn;
    }

    public void takeDamage(int damage) {
        damageTaken = damage;
    }

    public int getDamageTaken() {
        return damageTaken;
    }

    public boolean hasTakenDamage() {
        return damageTaken > 0;
    }

    public void resetTurn() {
        resetDamageTaken();
        setReducePP(false);
    }

    private void resetDamageTaken() {
        damageTaken = 0;
    }

    public void setLastMoveUsed() {
        lastMoveUsed = selected;
    }

    public Move getLastMoveUsed() {
        return lastMoveUsed;
    }

    // Increment count if the pokemon uses the same move twice in a row
    public void count() {
        if (lastMoveUsed == null || selected.getAttack().namesies() != lastMoveUsed.getAttack().namesies()) {
            resetCount();
        } else {
            counter++;
        }
    }

    private void resetCount() {
        counter = 1;
    }

    public int getCount() {
        return counter;
    }

    public List<PokemonEffect> getEffects() {
        return effects;
    }

    public double getSuccessionDecayRate() {
        return successionDecayRate;
    }

    public void decay() {
        if (selected.getAttack().isMoveType(MoveType.SUCCESSIVE_DECAY)) {
            successionDecayRate *= .5;
        } else {
            this.resetDecay();
        }
    }

    private void resetDecay() {
        successionDecayRate = 1;
    }

    public Move getMove() {
        return selected;
    }

    public void setMove(Battle b, Move move) {
        this.selected = move;
        move.setAttributes(b, this.attributesHolder);
    }

    public void addEffect(PokemonEffect e) {
        effects.add(e);
    }

    public boolean removeEffect(PokemonEffect effect) {
        return effects.remove(effect);
    }

    public boolean removeEffect(EffectNamesies effect) {
        return Effect.removeEffect(effects, effect);
    }

    // Returns null if the Pokemon is not under the effects of the input effect, otherwise returns the Effect
    public PokemonEffect getEffect(EffectNamesies effect) {
        return (PokemonEffect)(Effect.getEffect(effects, effect));
    }

    public boolean hasEffect(EffectNamesies effect) {
        return Effect.hasEffect(effects, effect);
    }

    public Stages getStages() {
        return this.stages;
    }

    public void startAttack(Battle b) {
        this.setAttacking(true);
        this.getMove().switchReady(b, this.attributesHolder); // TODO: I don't think this works right because this is happening before you check if they're able to attack and honestly they shouldn't really switch until the end of the turn
        this.getMove().setAttributes(b, this.attributesHolder);
    }

    public void endAttack(ActivePokemon opp, boolean success) {
        if (!success) {
            this.removeEffect(EffectNamesies.SELF_CONFUSION);
            this.resetCount();
        }

        this.setLastMoveUsed();

        if (this.reducePP) {
            this.getMove().reducePP(opp.hasAbility(AbilityNamesies.PRESSURE) ? 2 : 1);
        }

        this.setAttacking(false);
    }
}
