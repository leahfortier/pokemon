package trainer;

import pokemon.ActivePokemon;
import util.SerializationUtils;

public class SimulatedPlayer extends PlayerTrainer {
    public SimulatedPlayer(PlayerTrainer player) {
        super(player.getName(), player.getDatCashMoney());
        
        for (ActivePokemon poke : player.getTeam()) {
            this.addPokemon((ActivePokemon)SerializationUtils.getSerializedCopy(poke));
        }
        
        this.setFront(player.getFrontIndex());
    }
    
    @Override
    public void addPokemon(ActivePokemon p) {
        team.add(p);
    }
}
