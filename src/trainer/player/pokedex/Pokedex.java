package trainer.player.pokedex;

import battle.ActivePokemon;
import main.Game;
import pokemon.species.PokemonInfo;
import pokemon.species.PokemonList;
import pokemon.species.PokemonNamesies;
import util.serialization.Serializable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Pokedex implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<PokemonNamesies, PokedexInfo> pokedex;

    public Pokedex() {
        pokedex = new EnumMap<>(PokemonNamesies.class);
        for (PokemonInfo pokemonInfo : PokemonList.instance()) {
            pokedex.put(pokemonInfo.namesies(), new PokedexInfo());
        }
    }

    public boolean isNotSeen(ActivePokemon pokemon) {
        return isNotSeen(pokemon.getPokemonInfo());
    }

    public boolean isNotSeen(PokemonInfo pokemon) {
        return isNotSeen(pokemon.namesies());
    }

    public boolean isNotSeen(PokemonNamesies namesies) {
        return pokedex.get(namesies).isStatus(PokedexStatus.NOT_SEEN);
    }

    public boolean isCaught(ActivePokemon pokemon) {
        return isCaught(pokemon.getPokemonInfo());
    }

    public boolean isCaught(PokemonInfo pokemon) {
        return isCaught(pokemon.namesies());
    }

    public boolean isCaught(PokemonNamesies namesies) {
        return pokedex.get(namesies).isStatus(PokedexStatus.CAUGHT);
    }

    public void setCaught(ActivePokemon p) {
        this.setCaught(p.getPokemonInfo());
    }

    public void setCaught(PokemonInfo pokemonInfo) {
        setStatus(pokemonInfo.namesies(), PokedexStatus.CAUGHT, null);
    }

    public void setSeen(ActivePokemon pokemon, boolean isWildBattle) {
        this.setStatus(
                pokemon.namesies(),
                PokedexStatus.SEEN,
                isWildBattle ? Game.getPlayer().getAreaName() : ""
        );
    }

    private void setStatus(PokemonNamesies pokemon, PokedexStatus status, String wildLocation) {
        PokedexInfo info = pokedex.get(pokemon);

        info.addLocation(wildLocation);
        info.setStatus(status);
        pokedex.put(pokemon, info);

        Game.getPlayer().getMedalCase().updatePokedex(this, pokemon);
    }

    // Num seen includes both caught and seen
    public int numSeen() {
        return (int)pokedex.entrySet()
                           .stream()
                           .filter(pair -> !pair.getValue().isStatus(PokedexStatus.NOT_SEEN))
                           .count();
    }

    public int numCaught() {
        return (int)pokedex.entrySet()
                           .stream()
                           .filter(pair -> pair.getValue().isStatus(PokedexStatus.CAUGHT))
                           .count();
    }

    public List<String> getLocations(PokemonNamesies pokemon) {
        return pokedex.get(pokemon).getLocations();
    }
}
