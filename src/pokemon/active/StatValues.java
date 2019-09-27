package pokemon.active;

import main.Game;
import pokemon.species.BaseStats;
import pokemon.stat.Stat;
import trainer.player.medal.Medal;
import trainer.player.medal.MedalCase;
import util.serialization.Serializable;

public class StatValues implements Serializable {
    private static final long serialVersionUID = 1L;

    private int[] stats;
    private Nature nature;
    private IndividualValues IVs;
    private EffortValues EVs;

    private transient PartyPokemon statsHolder;

    StatValues(PartyPokemon statsHolder) {
        this.statsHolder = statsHolder;

        this.stats = new int[Stat.NUM_STATS];
        this.nature = new Nature();
        this.IVs = new IndividualValues();
        this.EVs = new EffortValues();
    }

    public void setStatsHolder(PartyPokemon statsHolder) {
        this.statsHolder = statsHolder;
    }

    public int get(int index) {
        return stats[index];
    }

    public int get(Stat stat) {
        return this.get(stat.index());
    }

    public Nature getNature() {
        return nature;
    }

    public IndividualValues getIVs() {
        return this.IVs;
    }

    public EffortValues getEVs() {
        return this.EVs;
    }

    public int[] getClonedStats() {
        return this.stats.clone();
    }

    void setNature(Nature nature) {
        this.nature = nature;
        this.setStats();
    }

    void setIVs(IndividualValues IVs) {
        this.IVs.setIVs(IVs);
        this.setStats();
    }

    int[] setStats() {
        int[] prevStats = stats.clone();

        stats = new int[Stat.NUM_STATS];
        int[] gain = new int[Stat.NUM_STATS];
        for (int i = 0; i < stats.length; i++) {
            stats[i] = this.calculate(i, statsHolder.getPokemonInfo().getStats());
            gain[i] = stats[i] - prevStats[i];
        }

        statsHolder.setHP(statsHolder.getHP() + stats[Stat.HP.index()] - prevStats[Stat.HP.index()]);

        return gain;
    }

    // Adds Effort Values to a Pokemon, returns true if they were successfully added
    public boolean addEVs(int[] vals) {
        if (!statsHolder.canFight()) {
            return false;
        }

        boolean added = this.EVs.addEVs(vals);

        if (added) {
            this.setStats();
            if (this.EVs.totalEVs() == EffortValues.MAX_EVS) {
                MedalCase medalCase = Game.getPlayer().getMedalCase();
                medalCase.earnMedal(Medal.TRAINED_TO_MAX_POTENTIAL);
            }
        }

        return added;
    }

    public int calculate(Stat stat, BaseStats baseStats) {
        return this.calculate(stat.index(), baseStats);
    }

    // Generates a new stat
    public int calculate(int statIndex, BaseStats baseStats) {
        int level = statsHolder.getLevel();
        int baseStat = baseStats.get(statIndex);
        int IV = IVs.get(statIndex);
        int EV = EVs.get(statIndex);
        double natureVal = nature.getNatureVal(statIndex);

        if (statIndex == Stat.HP.index()) {
            // Shedinja...
            if (baseStat == 1) {
                return 1;
            } else {
                return (int)(((IV + 2*baseStat + (EV/4.0))*level/100.0) + 10 + level);
            }
        }

        return (int)((((IV + 2*baseStat + (EV/4.0))*level/100.0) + 5)*natureVal);
    }
}
