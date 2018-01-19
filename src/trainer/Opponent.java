package trainer;

public interface Opponent extends Team {
    String getStartBattleMessage();
    int maxPokemonAllowed();
}
