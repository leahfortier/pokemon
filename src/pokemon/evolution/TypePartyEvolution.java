package pokemon.evolution;

import item.ItemNamesies;
import main.Game;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import type.Type;

import java.util.List;

public class TypePartyEvolution extends Evolution {
    private final BaseEvolution evolution;
    private final Type type;

    public TypePartyEvolution(String type, BaseEvolution evolution) {
        this.type = Type.valueOf(type);
        this.evolution = evolution;
    }

    @Override
    public BaseEvolution getEvolution(EvolutionMethod type, ActivePokemon p, ItemNamesies use) {
        List<ActivePokemon> team = Game.getPlayer().getTeam();
        for (ActivePokemon pokemon : team) {
            if (pokemon.getPokemonInfo().isType(this.type)) {
                return this.evolution.getEvolution(type, p, use);
            }
        }

        return null;
    }

    @Override
    public PokemonNamesies[] getEvolutions() {
        return this.evolution.getEvolutions();
    }

    @Override
    public String getString() {
        return this.evolution.getString() + ", with " + this.type.getName() + "-type in party";
    }

    @Override
    public String toString() {
        return EvolutionType.TYPE_PARTY + " " + type.name() + " " + evolution.toString();
    }
}
