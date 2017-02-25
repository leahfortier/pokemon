package map.triggers;

import main.Game;
import message.MessageUpdate;
import message.Messages;
import pokemon.breeding.DayCareCenter;
import trainer.Player;
import util.StringUtils;

public class BreedingDepositTrigger extends Trigger {
    private final Integer teamIndex;

    public BreedingDepositTrigger(String contents, String condition) {
        super(TriggerType.BREEDING_DEPOSIT, contents, condition);

        if (StringUtils.isNullOrEmpty(contents)) {
            teamIndex = null;
        } else {
            teamIndex = Integer.parseInt(contents);
        }
    }

    @Override
    protected void executeTrigger() {
        Player player = Game.getPlayer();
        DayCareCenter dayCareCenter = player.getDayCareCenter();
        if (teamIndex == null) {
            Messages.add(new MessageUpdate().withTrigger(dayCareCenter.getDepositTrigger().getName()));
            return;
        }

        dayCareCenter.deposit(player.getTeam().get(teamIndex));
    }
}
