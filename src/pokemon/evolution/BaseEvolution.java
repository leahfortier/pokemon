package pokemon.evolution;

import item.ItemNamesies;
import pokemon.ActivePokemon;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;

public abstract class BaseEvolution extends Evolution {
	private final EvolutionMethod evolutionMethod;
	private final PokemonNamesies evolutionNamesies;

	protected BaseEvolution(EvolutionMethod evolutionMethod, String namesies) {
		this.evolutionMethod = evolutionMethod;
		this.evolutionNamesies = PokemonNamesies.valueOf(namesies);
	}

	protected abstract Evolution getEvolution(ActivePokemon toEvolve, ItemNamesies useItem);

	public final PokemonInfo getEvolution() {
		return PokemonInfo.getPokemonInfo(evolutionNamesies);
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
