package pokemon.evolution;

import battle.ActivePokemon;
import item.ItemNamesies;
import main.Game;
import main.Global;
import pokemon.active.PartyPokemon;

public enum EvolutionMethod {
    LEVEL,
    ITEM,
    MOVE;

    public boolean checkEvolution(PartyPokemon p) {
        return this.checkEvolution(p, null);
    }

    public boolean checkEvolution(PartyPokemon p, ItemNamesies itemNamesies) {
        if ((this == ITEM) == (itemNamesies == null)) {
            Global.error("Item can be specified if and only if Item Evolution: " + this + ", " + itemNamesies);
        }

        if (!p.isPlayer()) {
            Global.error("Only player Pokemon can evolve.");
        }

        // Eggys and deadies can't evolve
        if (!p.canFight()) {
            return false;
        }

        // Everstone prevents evolution
        if (p.getActualHeldItem().namesies() == ItemNamesies.EVERSTONE) {
            return false;
        }

        ActivePokemon evolver = (ActivePokemon)p;
        BaseEvolution evolution = p.getPokemonInfo().getEvolution().getEvolution(this, evolver, itemNamesies);
        if (evolution != null) {
            Game.getPlayer().getEvolutionInfo().setEvolution(evolver, evolution);
            return true;
        }

        return false;
    }
}
