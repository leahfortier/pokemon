package trainer;

import pokemon.PartyPokemon;

public class SimulatedPlayer extends PlayerTrainer {
    public SimulatedPlayer(PlayerTrainer player) {
        super(player.getName(), player.getDatCashMoney());

        for (PartyPokemon poke : player.getTeam()) {
            this.addPokemon(poke.getSerializedCopy(PartyPokemon.class));
        }

        this.setFront(player.getFrontIndex());
        this.setInBattle();
    }

    @Override
    public void addPokemon(PartyPokemon p) {
        team.add(p);
    }
}
