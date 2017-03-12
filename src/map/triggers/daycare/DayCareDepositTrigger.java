package map.triggers.daycare;

import main.Game;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import message.MessageUpdate;
import message.Messages;
import pokemon.breeding.DayCareCenter;
import trainer.player.Player;
import util.StringUtils;

public class DayCareDepositTrigger extends Trigger {
    private final Integer teamIndex;

    public DayCareDepositTrigger(String contents, String condition) {
        super(TriggerType.DAY_CARE_DEPOSIT, contents, condition);

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
