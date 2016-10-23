package map;

import main.Global;
import main.Namesies;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import trainer.WildPokemon;

// TODO: Not exactly sure if this is the best location for this class
public class WildEncounter {
    private Namesies pokemon;

    private int minLevel;
    private int maxLevel;

    private int probability;

    public WildEncounter(String pokemon, String minLevel, String maxLevel, String probability) {
        this.pokemon = Namesies.getValueOf(pokemon, Namesies.NamesiesType.POKEMON);

        this.minLevel = Integer.parseInt(minLevel);
        this.maxLevel = Integer.parseInt(maxLevel);

        this.probability = Integer.parseInt(probability);
    }

    public String getPokemonName() {
        return this.pokemon.getName();
    }

    public int getMinLevel() {
        return this.minLevel;
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }

    public int getProbability() {
        return this.probability;
    }

    public WildPokemon getWildPokemon() {
        int level = Global.RANDOM.nextInt((this.maxLevel - this.minLevel + 1) + this.minLevel);
        return new WildPokemon(new ActivePokemon(PokemonInfo.getPokemonInfo(this.pokemon), level, true, false));
    }

    public String toString() {
        return String.format("\tpokemon: %s %d-%d %d%%n", this.pokemon.getName(), this.minLevel, this.maxLevel, this.probability);
    }
}
