package pattern.action;

import pattern.PokemonMatcher;
import trainer.Trainer;

public class BattleMatcher {
    private String name;
    private int cashMoney;
    private Integer maxPokemonAllowed;
    private PokemonMatcher[] pokemon;
    private String update;

    public BattleMatcher(String name, int cashMoney, PokemonMatcher[] pokemon, String update) {
        this.name = name;
        this.cashMoney = cashMoney;
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

    public int getMaxPokemonAllowed() {
        return this.maxPokemonAllowed == null ? Trainer.MAX_POKEMON : this.maxPokemonAllowed;
    }
}
