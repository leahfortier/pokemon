package battle;

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
    private static final int[] CRITSICLES = { 16, 8, 4, 3, 2 };

    public static class DamageCalculation implements Serializable {
        private static final long serialVersionUID = 1L;

        private final int damageCalculated;
        private final double advantage;
        private final boolean critical;

        private int damageDealt;

        private DamageCalculation(int damage, double advantage, boolean critical) {
            this.damageCalculated = damage;
            this.advantage = advantage;
            this.critical = critical;
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

        public void setDamageDealt(int damage) {
            this.damageDealt = damage;
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

        int level = me.getLevel();
        int random = RandomUtils.getRandomInt(16) + 85;

        int power = me.getAttack().getPower(b, me, o);
        power *= getDamageModifier(b, me, o);

        int attackStat = Stat.getStat(attacking, me, b);
        int defenseStat = Stat.getStat(defending, o, b);

        double stab = TypeAdvantage.getSTAB(b, me);
        double adv = TypeAdvantage.getAdvantage(me, o, b);

        int damage = (int)Math.ceil(((((2*level/5.0 + 2)*attackStat*power/defenseStat)/50.0) + 2)*stab*adv*random/100.0);

        boolean critYoPants = this.criticalHit(b, me, o);
        if (critYoPants) {
            damage *= me.hasAbility(AbilityNamesies.SNIPER) ? 3 : 2;
        }

        return new DamageCalculation(damage, adv, critYoPants);
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

        // Increase crit stage and such
        int stage = getCritStage(b, me);

        return RandomUtils.chanceTest(1, CRITSICLES[stage - 1]);
    }

    public int getCritStage(Battle b, ActivePokemon me) {
        int stage = 1 + CritStageEffect.getModifier(b, me);
        stage = Math.min(stage, CRITSICLES.length); // Max it out, yo
        return stage;
    }
}
