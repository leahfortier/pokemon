package map.triggers;

import main.Global;
import map.condition.Condition;
import message.MessageUpdate;
import message.MessageUpdateType;
import message.Messages;
import util.StringUtils;

public class HaltTrigger extends Trigger {
    private static boolean halted = false;

    HaltTrigger(String contents, Condition condition) {
        this(condition);
        if (!StringUtils.isNullOrEmpty(contents)) {
            Global.error("Contents should be empty for " + this.getClass().getSimpleName());
        }
    }

    public HaltTrigger(Condition condition) {
        super(null, condition);
    }

    @Override
    protected void executeTrigger() {
        if (halted) {
            addHaltTrigger();
        } else {
            Messages.addToFront(new MessageUpdate().withUpdate(MessageUpdateType.RESET_STATE));
        }
    }

    public static void addHaltTrigger() {
        halted = true;
        Trigger trigger = new HaltTrigger(null);
        Messages.addToFront(new MessageUpdate().withTrigger(trigger.getName()));
    }

    public static void resume() {
        halted = false;
    }

    public static boolean isHalted() {
        return halted;
    }
}
