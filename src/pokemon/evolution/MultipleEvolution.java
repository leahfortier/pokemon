package pokemon.evolution;

import item.ItemNamesies;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import util.RandomUtils;

import java.util.ArrayList;
import java.util.List;

class MultipleEvolution extends Evolution {
    private static final long serialVersionUID = 1L;

    private Evolution[] evolutions;

    MultipleEvolution(Evolution[] list) {
        evolutions = list;
    }

    @Override
    public Evolution getEvolution(EvolutionMethod type, ActivePokemon p, ItemNamesies use) {
        List<Evolution> list = new ArrayList<>();
        for (Evolution ev : evolutions) {
            Evolution lev = ev.getEvolution(type, p, use);
            if (lev != null) {
                list.add(lev);
            }
        }

        if (!list.isEmpty()) {
            // This is pretty much for Wurmple even though he's not even going in the game
            return RandomUtils.getRandomValue(list);
        }

        return null;
    }

    @Override
    public PokemonNamesies[] getEvolutions() {
        PokemonNamesies[] namesies = new PokemonNamesies[evolutions.length];
        for (int i = 0; i < evolutions.length; i++) {
            namesies[i] = evolutions[i].getEvolutions()[0];
        }

        return namesies;
    }

    @Override
    public String toString() {
        String toString = EvolutionType.MULTI + " " + this.evolutions.length;
        for (Evolution evolution : this.evolutions) {
            toString += "\n" + evolution.toString();
        }

        return toString;
    }
}
