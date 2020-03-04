package pokemon.active;

import pokemon.stat.Stat;
import util.RandomUtils;
import util.string.StringUtils;

import java.awt.Color;

public enum Nature {
    HARDY(Stat.ATTACK, Stat.ATTACK),
    LONELY(Stat.ATTACK, Stat.DEFENSE),
    ADAMANT(Stat.ATTACK, Stat.SP_ATTACK),
    NAUGHTY(Stat.ATTACK, Stat.SP_DEFENSE),
    BRAVE(Stat.ATTACK, Stat.SPEED),
    BOLD(Stat.DEFENSE, Stat.ATTACK),
    DOCILE(Stat.DEFENSE, Stat.DEFENSE),
    IMPISH(Stat.DEFENSE, Stat.SP_ATTACK),
    LAX(Stat.DEFENSE, Stat.SP_DEFENSE),
    RELAXED(Stat.DEFENSE, Stat.SPEED),
    MODEST(Stat.SP_ATTACK, Stat.ATTACK),
    MILD(Stat.SP_ATTACK, Stat.DEFENSE),
    BASHFUL(Stat.SP_ATTACK, Stat.SP_ATTACK),
    RASH(Stat.SP_ATTACK, Stat.SP_DEFENSE),
    QUIET(Stat.SP_ATTACK, Stat.SPEED),
    CALM(Stat.SP_DEFENSE, Stat.ATTACK),
    GENTLE(Stat.SP_DEFENSE, Stat.DEFENSE),
    CAREFUL(Stat.SP_DEFENSE, Stat.SP_ATTACK),
    QUIRKY(Stat.SP_DEFENSE, Stat.SP_DEFENSE),
    SASSY(Stat.SP_DEFENSE, Stat.SPEED),
    TIMID(Stat.SPEED, Stat.ATTACK),
    HASTY(Stat.SPEED, Stat.DEFENSE),
    JOLLY(Stat.SPEED, Stat.SP_ATTACK),
    NAIVE(Stat.SPEED, Stat.SP_DEFENSE),
    SERIOUS(Stat.SPEED, Stat.SPEED);

    private final Stat beneficial;
    private final Stat hindering;
    private final String name;

    Nature(Stat beneficial, Stat hindering) {
        this.beneficial = beneficial;
        this.hindering = hindering;
        this.name = StringUtils.properCase(this.name().toLowerCase());
    }

    public String getName() {
        return name;
    }

    public double getNatureVal(int statIndex) {
        Stat stat = Stat.getStat(statIndex, false);
        if (this.isNeutral()) {
            return 1;
        } else if (beneficial == stat) {
            return 1.1;
        } else if (hindering == stat) {
            return .9;
        } else {
            return 1;
        }
    }

    public Color getColor(Stat stat) {
        if (this.isNeutral()) {
            return Color.BLACK;
        } else if (beneficial == stat) {
            return new Color(0, 190, 0);
        } else if (hindering == stat) {
            return new Color(200, 0, 0);
        } else {
            return Color.BLACK;
        }
    }

    public boolean isNeutral() {
        return beneficial == hindering;
    }

    // Returns stat which is boosted by the nature
    // Note: Should check for neutral nature before checking this
    public Stat getBeneficial() {
        return beneficial;
    }

    // Returns stat which is hindered by the nature
    // Note: Should check for neutral nature before checking this
    public Stat getHindering() {
        return hindering;
    }

    // Returns a randomly assigned nature
    public static Nature random() {
        return RandomUtils.getRandomValue(Nature.values());
    }
}
