package map.triggers.daycare;

import main.Game;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import message.MessageUpdate;
import message.Messages;
import pokemon.breeding.DayCareCenter;
import util.StringUtils;

public class DayCareWithdrawTrigger extends Trigger {
    private final Boolean isFirstPokemon;

    public DayCareWithdrawTrigger(String contents, String condition) {
        super(TriggerType.DAY_CARE_WITHDRAW, contents, condition);

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
