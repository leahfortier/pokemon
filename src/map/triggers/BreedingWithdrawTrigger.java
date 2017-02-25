package map.triggers;

import main.Game;
import message.MessageUpdate;
import message.Messages;
import pokemon.breeding.DayCareCenter;
import util.StringUtils;

public class BreedingWithdrawTrigger extends Trigger {
    private final Boolean isFirstPokemon;

    public BreedingWithdrawTrigger(String contents, String condition) {
        super(TriggerType.BREEDING_WITHDRAW, contents, condition);

        if (StringUtils.isNullOrEmpty(contents)) {
            this.isFirstPokemon = null;
        } else {
            this.isFirstPokemon = Boolean.parseBoolean(contents);
        }
    }

    @Override
    protected void executeTrigger() {
        DayCareCenter dayCareCenter = Game.getPlayer().getDayCareCenter();
        if (this.isFirstPokemon == null) {
            Messages.add(new MessageUpdate().withTrigger(dayCareCenter.getWithdrawTrigger().getName()));
        }
        else {
            dayCareCenter.withdraw(isFirstPokemon);
        }
    }
}
