package trainer;

import pokemon.PartyPokemon;
import util.SerializationUtils;

public class SimulatedPlayer extends PlayerTrainer {
    public SimulatedPlayer(PlayerTrainer player) {
        super(player.getName(), player.getDatCashMoney());

        for (PartyPokemon poke : player.getTeam()) {
            this.addPokemon((PartyPokemon)SerializationUtils.getSerializedCopy(poke));
        }

        this.setFront(player.getFrontIndex());
        this.setInBattle();
    }

    @Override
    public void addPokemon(PartyPokemon p) {
        team.add(p);
    }
}
