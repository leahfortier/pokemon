package pattern.action;

public class BattleMatcher {
    public String name;
    public int cashMoney;
    public String[] pokemon;
    public String update;

    public BattleMatcher(String name, int cashMoney, String[] pokemon, String update) {
        this.name = name;
        this.cashMoney = cashMoney;
        this.pokemon = pokemon;
        this.update = update;
    }
}
