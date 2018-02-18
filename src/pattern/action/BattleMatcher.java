package pattern.action;

import pattern.PokemonMatcher;
import trainer.Trainer;

public class BattleMatcher {
    public String name;
    public int cashMoney;
    public boolean maxPokemonLimit;
    public PokemonMatcher[] pokemon;
    public String update;

    public BattleMatcher(String name, int cashMoney, boolean maxPokemonLimit, PokemonMatcher[] pokemon, String update) {
        this.name = name;
        this.cashMoney = cashMoney;
        this.maxPokemonLimit = maxPokemonLimit;
        this.pokemon = pokemon;
        this.update = update;
    }

    public String getName() {
        return this.name;
    }

    public int getDatCashMoney() {
        return this.cashMoney;
    }

    public PokemonMatcher[] getPokemon() {
        return this.pokemon;
    }

    public String getUpdateInteraction() {
        return this.update;
    }

    public boolean isMaxPokemonLimit() {
        return this.maxPokemonLimit;
    }

    public int getMaxPokemonAllowed() {
        return this.maxPokemonLimit ? this.pokemon.length : Trainer.MAX_POKEMON;
    }
}
