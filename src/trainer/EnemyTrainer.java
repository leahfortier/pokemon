package trainer;

import main.Global;
import pokemon.ActivePokemon;

public class EnemyTrainer extends Trainer implements Opponent {
	private static final long serialVersionUID = 1L;
	
	public EnemyTrainer(String name, int cashMoney) {
		super(name, cashMoney);
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
}
