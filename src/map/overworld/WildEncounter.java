package map.overworld;

import battle.ActivePokemon;
import item.ItemNamesies;
import main.Game;
import pattern.PokemonMatcher;
import pokemon.Gender;
import pokemon.Nature;
import pokemon.PartyPokemon;
import pokemon.PokemonNamesies;
import trainer.WildPokemon;
import util.RandomUtils;

public class WildEncounter {
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

    public int getLevel() {
        return this.pokemonMatcher.getLevel();
    }

    public void setLevel(int level) {
        this.pokemonMatcher.setLevel(level);
    }

    public void setHoldItem(ItemNamesies holdItem) {
        this.pokemonMatcher.setHoldItem(holdItem);
    }

    public void setGender(Gender gender) {
        this.pokemonMatcher.setGender(gender);
    }

    public void setNature(Nature nature) {
        this.pokemonMatcher.setNature(nature);
    }

    public WildPokemon getWildPokemon() {
        ActivePokemon attacking = Game.getPlayer().front();

        if (!this.pokemonMatcher.hasHoldItem()) {
            this.pokemonMatcher.setHoldItem(WildHoldItem.getWildHoldItem(pokemonMatcher.getNamesies(), attacking));
        }

        ActivePokemon wildPokemon = (ActivePokemon)PartyPokemon.createActivePokemon(this.pokemonMatcher, false);
        return new WildPokemon(wildPokemon);
    }
}
