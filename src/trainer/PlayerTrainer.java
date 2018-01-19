package trainer;

import battle.Battle.EnterBattleMessageGetter;

public abstract class PlayerTrainer extends Trainer {
    protected PlayerTrainer(String name, int cashMoney) {
        super(name, cashMoney);
    }

    @Override
    public EnterBattleMessageGetter getEnterBattleMessage() {
        return enterer -> "Go! " + enterer.getName() + "!";
    }
}
