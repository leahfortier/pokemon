package trainer;

import main.Global;
import pokemon.ActivePokemon;

public class EnemyTrainer extends Trainer implements Opponent {
	private static final long serialVersionUID = 1L;

	private final int maxPokemonAllowed;

	public EnemyTrainer(String name, int cashMoney, int maxPokemonAllowed) {
		super(name, cashMoney);
		this.maxPokemonAllowed = maxPokemonAllowed;
	}

	public EnemyTrainer(String name, int cashMoney, int maxPokemonAllowed, ActivePokemon... team) {
		this(name, cashMoney, maxPokemonAllowed);
		for (ActivePokemon pokemon : team) {
			addPokemon(pokemon);
		}
	}

	@Override
	public void addPokemon(ActivePokemon p) {
		if (team.size() < MAX_POKEMON) {
			team.add(p);
		}
		else {
			Global.error("Trainers cannot have more than six Pokemon");
		}
	}

	@Override
	public int maxPokemonAllowed() {
		return this.maxPokemonAllowed;
	}
}
