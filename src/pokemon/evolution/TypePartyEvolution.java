package pokemon.evolution;

import battle.ActivePokemon;
import main.Game;
import pokemon.species.PokemonNamesies;
import type.Type;

import java.util.List;

public class TypePartyEvolution extends ConditionEvolution {
    private static final long serialVersionUID = 1L;

    private final Type type;

    public TypePartyEvolution(String type, BaseEvolution evolution) {
        super(evolution);
        this.type = Type.valueOf(type);
    }

    @Override
    protected boolean meetsCondition(ActivePokemon pokemon) {
        List<ActivePokemon> team = Game.getPlayer().getActiveTeam();
        for (ActivePokemon member : team) {
            if (member.getPokemonInfo().isType(this.type)) {
                return true;
            }
        }

        return false;
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
        return EvolutionType.TYPE_PARTY + " " + type.name() + " " + evolution;
    }
}
