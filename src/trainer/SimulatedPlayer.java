package trainer;

import battle.ActivePokemon;
import pokemon.PartyPokemon;
import util.SerializationUtils;

public class SimulatedPlayer extends PlayerTrainer {
    public SimulatedPlayer(PlayerTrainer player) {
        super(player.getName(), player.getDatCashMoney());

        for (PartyPokemon poke : player.getTeam()) {
            this.addPokemon((ActivePokemon)SerializationUtils.getSerializedCopy(poke));
        }

        this.setFront(player.getFrontIndex());
    }

    @Override
    public void addPokemon(PartyPokemon p) {
        team.add(p);
    }
}
