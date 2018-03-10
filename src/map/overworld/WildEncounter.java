package map.overworld;

import battle.ActivePokemon;
import main.Game;
import pattern.PokemonMatcher;
import pokemon.Gender;
import pokemon.Nature;
import pokemon.PartyPokemon;
import pokemon.PokemonNamesies;
import trainer.WildPokemon;
import util.RandomUtils;
import util.serialization.JsonMatcher;

public class WildEncounter implements JsonMatcher {
    private PokemonMatcher pokemonMatcher;

    public WildEncounter(WildEncounterInfo wildEncounterInfo) {
        this(
                wildEncounterInfo.getPokemonName(),
                RandomUtils.getRandomInt(wildEncounterInfo.getMinLevel(), wildEncounterInfo.getMaxLevel())
        );
    }

    public WildEncounter(PokemonNamesies pokemon, int level) {
        this.pokemonMatcher = new PokemonMatcher(pokemon, level);
    }

    public PokemonNamesies getPokemon() {
        return this.pokemonMatcher.getNamesies();
    }

    public int getLevel() {
        return this.pokemonMatcher.getLevel();
    }

    public void setLevel(int level) {
        this.pokemonMatcher.setLevel(level);
    }

    public void setGender(Gender gender) {
        this.pokemonMatcher.setGender(gender);
    }

    public void setNature(Nature nature) {
        this.pokemonMatcher.setNature(nature);
    }

    public WildPokemon getWildPokemon() {
        ActivePokemon attacking = Game.getPlayer().front();

        this.pokemonMatcher.setHoldItem(WildHoldItem.getWildHoldItem(pokemonMatcher.getNamesies(), attacking));

        ActivePokemon wildPokemon = (ActivePokemon)PartyPokemon.createActivePokemon(this.pokemonMatcher, false);
        return new WildPokemon(wildPokemon);
    }
}
