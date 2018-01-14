package trainer;

import battle.ActivePokemon;
import main.Global;
import pokemon.PartyPokemon;

public class EnemyTrainer extends Trainer implements Opponent {
    private static final long serialVersionUID = 1L;

    private final int maxPokemonAllowed;

    public EnemyTrainer(String name, int cashMoney, int maxPokemonAllowed, ActivePokemon... team) {
        super(name, cashMoney);
        this.maxPokemonAllowed = maxPokemonAllowed;
        for (ActivePokemon pokemon : team) {
            addPokemon(pokemon);
        }
    }

    @Override
    public void addPokemon(PartyPokemon p) {
        if (p.isPlayer()) {
            Global.error("Enemy trainer pokemon cannot be player's...");
        }

        if (team.size() < MAX_POKEMON) {
            team.add(p);
        } else {
            Global.error("Trainers cannot have more than six Pokemon!");
        }
    }

    @Override
    public int maxPokemonAllowed() {
        return this.maxPokemonAllowed;
    }
}
