package trainer;

import battle.Battle;
import pokemon.active.PartyPokemon;

public class SimulatedPlayer extends PlayerTrainer {
    private static final long serialVersionUID = 1L;

    public SimulatedPlayer(PlayerTrainer player) {
        super(player.getName(), player.getDatCashMoney());

        for (PartyPokemon poke : player.getTeam()) {
            this.addPokemon(poke.getSerializedCopy(PartyPokemon.class));
        }

        this.setFront(player.getFrontIndex());
        this.setBattle(player.getBattle().getSerializedCopy(Battle.class));
    }

    @Override
    public void addPokemon(PartyPokemon p) {
        team.add(p);
    }
}
