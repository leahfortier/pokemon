package battle;

import battle.attack.Move;
import battle.effect.InvokeInterfaces.AlwaysCritEffect;
import battle.effect.InvokeInterfaces.CritBlockerEffect;
import battle.effect.InvokeInterfaces.CritStageEffect;
import battle.effect.InvokeInterfaces.OpponentPowerChangeEffect;
import battle.effect.InvokeInterfaces.PowerChangeEffect;
import main.Global;
import pokemon.ability.AbilityNamesies;
import pokemon.stat.Stat;
import type.TypeAdvantage;
import util.RandomUtils;
import util.serialization.Serializable;

public class DamageCalculator {
    // Crit yo pants
    private static final int[] CRITSICLES = { 24, 8, 2, 1 };

    public static class DamageCalculation implements Serializable {
        private static final long serialVersionUID = 1L;

        private int damageCalculated;
        private double advantage;
        private boolean critical;
        private int damageDealt;

        public DamageCalculation() {
            this.reset();
        }

        public void reset() {
            this.damageCalculated = 0;
            this.advantage = 1;
            this.critical = false;
            this.damageDealt = 0;
        }

        private void setDamageCalculated(int damageCalculated) {
            this.damageCalculated = damageCalculated;
        }

        private void setAdvantage(double advantage) {
            this.advantage = advantage;
        }

        private void setCritical(boolean critical) {
            this.critical = critical;
        }

        public void setDamageDealt(int damage) {
            this.damageDealt = damage;
        }

        public int getCalculatedDamage() {
            return this.damageCalculated;
        }

        public double getAdvantage() {
            return this.advantage;
        }

        public boolean isCritical() {
            return this.critical;
        }

        public int getDamageDealt() {
            return this.damageDealt;
        }
    }

    public DamageCalculation calculateDamage(Battle b, ActivePokemon me, ActivePokemon o) {
        final Stat attacking;
        final Stat defending;
        switch (me.getAttack().getCategory()) {
            case PHYSICAL:
                attacking = Stat.ATTACK;
                defending = Stat.DEFENSE;
                break;
            case SPECIAL:
                attacking = Stat.SP_ATTACK;
                defending = Stat.SP_DEFENSE;
                break;
            default:
                Global.error("Invalid category " + me.getAttack().getCategory() + " for calculating damage");
                return null;
        }

        Move move = me.getMove();
        DamageCalculation calculatedDamage = move.getCalculatedDamage();

        int level = me.getLevel();
        int random = RandomUtils.getRandomInt(16) + 85;

        boolean critYoPants = this.criticalHit(b, me, o);
        calculatedDamage.setCritical(critYoPants);

        int power = move.getAttack().getPower(b, me, o);
        power *= getDamageModifier(b, me, o);

        int attackStat = Stat.getStat(attacking, me, b);
        int defenseStat = Stat.getStat(defending, o, b);

        double stab = TypeAdvantage.getSTAB(b, me);
        double adv = TypeAdvantage.getAdvantage(me, o, b);

        int damage = (int)Math.ceil(((((2*level/5.0 + 2)*attackStat*power/defenseStat)/50.0) + 2)*stab*adv*random/100.0);
        if (critYoPants) {
            damage *= me.hasAbility(AbilityNamesies.SNIPER) ? 2 : 1.5;
        }

        calculatedDamage.setDamageCalculated(damage);
        calculatedDamage.setAdvantage(adv);

        return calculatedDamage;
    }

    protected double getDamageModifier(Battle b, ActivePokemon me, ActivePokemon o) {
        return PowerChangeEffect.getModifier(b, me, o)*OpponentPowerChangeEffect.getModifier(b, me, o);
    }

    private boolean criticalHit(Battle b, ActivePokemon me, ActivePokemon o) {
        if (CritBlockerEffect.checkBlocked(b, me, o)) {
            return false;
        }

        if (AlwaysCritEffect.defCritsies(b, me, o)) {
            return true;
        }

        return checkRandomCrit(b, me);
    }

    // Returns true if the crit stage random check results in a critical hit
    // Does not include effects that always or never result in critical hits, ONLY the stage check
    protected boolean checkRandomCrit(Battle b, ActivePokemon me) {
        int stage = getCritStage(b, me);
        stage = Math.min(stage, CRITSICLES.length - 1); // Max it out, yo
        return RandomUtils.chanceTest(1, CRITSICLES[stage]);
    }

    public int getCritStage(Battle b, ActivePokemon me) {
        return CritStageEffect.getModifier(b, me);
    }
}
