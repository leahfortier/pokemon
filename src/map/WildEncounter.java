package map;

import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import trainer.WildPokemon;
import util.RandomUtils;

// TODO: Not exactly sure if this is the best location for this class
public class WildEncounter {
    private PokemonNamesies pokemon;

    private int minLevel;
    private int maxLevel;

    private int probability;

    public WildEncounter(String pokemon, String minLevel, String maxLevel, String probability) {
        this.pokemon = PokemonNamesies.getValueOf(pokemon);

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
        int level = RandomUtils.getRandomInt(this.minLevel, this.maxLevel);
        return new WildPokemon(new ActivePokemon(this.pokemon, level, true, false));
    }
}
