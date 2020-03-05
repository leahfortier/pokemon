package battle.attack;

import battle.ActivePokemon;
import battle.Battle;
import battle.DamageCalculator.DamageCalculation;
import battle.effect.InvokeInterfaces.PriorityChangeEffect;
import type.Type;
import util.serialization.Serializable;

// Different values calculated for the current turn
public class MoveTurnData implements Serializable {
    private static final long serialVersionUID = 1L;

    private int priority;
    private Type type;

    private Boolean accuracyBypass;
    private boolean attackHit;

    private DamageCalculation calculatedDamage;

    public MoveTurnData(AttackNamesies attackNamesies) {
        Attack attack = attackNamesies.getNewAttack();
        this.priority = attack.getActualPriority();
        this.type = attack.getActualType();
        this.calculatedDamage = new DamageCalculation();
    }

    // Occurs at the beginning of the full turn (not at beginning of attack)
    public void startTurn(Battle b, ActivePokemon p) {
        this.priority = p.getAttack().getPriority(b, p) + PriorityChangeEffect.getModifier(b, p);
    }

    // Occurs at the beginning of using the current move
    public void startMove(Battle b, ActivePokemon p) {
        this.type = p.getAttack().getBattleType(b, p);
        this.calculatedDamage.reset();
    }

    public int getPriority() {
        return this.priority;
    }

    public Type getType() {
        return type;
    }

    public DamageCalculation getCalculatedDamage() {
        return this.calculatedDamage;
    }

    public void setAccuracyCheck(Boolean bypass, boolean attackHit) {
        this.accuracyBypass = bypass;
        this.attackHit = attackHit;
    }

    // Returns true if the attack missed by chance (returns false if forced missed like semi-invulnerable etc)
    public boolean isNaturalMiss() {
        return accuracyBypass == null && !attackHit;
    }
}
