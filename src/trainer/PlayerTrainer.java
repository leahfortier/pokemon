package trainer;

import battle.ActivePokemon;

public abstract class PlayerTrainer extends Trainer {
    protected PlayerTrainer(String name, int cashMoney) {
        super(name, cashMoney);
    }

    @Override
    public String getEnterBattleMessage(ActivePokemon enterer) {
        return "Go! " + enterer.getName() + "!";
    }
}
