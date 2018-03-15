package pokemon.active;

import main.Game;
import pokemon.Stat;
import pokemon.species.PokemonInfo;
import trainer.player.medal.Medal;
import trainer.player.medal.MedalCase;
import util.serialization.Serializable;

public class StatValues implements Serializable {
    private static final long serialVersionUID = 1L;

    private int[] stats;
    private Nature nature;
    private IndividualValues IVs;
    private EffortValues EVs;

    private final transient PartyPokemon statsHolder;

    StatValues(PartyPokemon statsHolder) {
        this.statsHolder = statsHolder;

        this.stats = new int[Stat.NUM_STATS];
        this.nature = new Nature();
        this.IVs = new IndividualValues();
        this.EVs = new EffortValues();
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

        PokemonInfo pokemon = statsHolder.getPokemonInfo();

        stats = new int[Stat.NUM_STATS];
        int[] gain = new int[Stat.NUM_STATS];
        for (int i = 0; i < stats.length; i++) {
            stats[i] = Stat.getStat(
                    i,
                    statsHolder.getLevel(),
                    pokemon.getStat(i),
                    IVs.get(i),
                    EVs.get(i),
                    nature.getNatureVal(i)
            );

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
}
