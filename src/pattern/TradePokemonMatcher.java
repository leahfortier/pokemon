package pattern;

import pokemon.PokemonNamesies;

public class TradePokemonMatcher {
    private PokemonNamesies requested;
    private PokemonNamesies tradePokemon;
    private Integer teamIndex;
    
    public TradePokemonMatcher(PokemonNamesies requested, PokemonNamesies tradePokemon) {
        this.requested = requested;
        this.tradePokemon = tradePokemon;
    }
    
    public PokemonNamesies getRequested() {
        return this.requested;
    }
    
    public PokemonNamesies getTradePokemon() {
        return this.tradePokemon;
    }
    
    public void setTeamIndex(int teamIndex) {
        this.teamIndex = teamIndex;
    }
    
    public boolean isCancelled() {
        return teamIndex == null;
    }
    
    public int getTeamIndex() {
        return this.teamIndex;
    }
}
