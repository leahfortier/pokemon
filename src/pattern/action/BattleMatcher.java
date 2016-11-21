package pattern.action;

import pattern.PokemonMatcher;

public class BattleMatcher {
    public String name;
    public int cashMoney;
    public PokemonMatcher[] pokemon;
    public String update;

    public BattleMatcher(String name, int cashMoney, PokemonMatcher[] pokemon, String update) {
        this.name = name;
        this.cashMoney = cashMoney;
        this.pokemon = pokemon;
        this.update = update;
    }
}
