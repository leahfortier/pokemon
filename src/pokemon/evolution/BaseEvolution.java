package pokemon.evolution;

import item.ItemNamesies;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;

public abstract class BaseEvolution extends Evolution {
	private final EvolutionMethod evolutionMethod;
	protected final int evolutionNumber;

	protected BaseEvolution(EvolutionMethod evolutionMethod, int evolutionNumber) {
		this.evolutionMethod = evolutionMethod;
		this.evolutionNumber = evolutionNumber;
	}

	protected abstract Evolution getEvolution(ActivePokemon toEvolve, ItemNamesies useItem);

	public final PokemonInfo getEvolution() {
		return PokemonInfo.getPokemonInfo(evolutionNumber);
	}

	@Override
	public final PokemonNamesies[] getEvolutions() {
		return new PokemonNamesies[] { getEvolution().namesies() };
	}

	@Override
	public final Evolution getEvolution(EvolutionMethod type, ActivePokemon p, ItemNamesies use) {
		if (type != this.evolutionMethod) {
			return null;
		}

		return this.getEvolution(p, use);
	}
}
