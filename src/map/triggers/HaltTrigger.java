package map.triggers;

import message.MessageUpdate;
import message.MessageUpdateType;
import message.Messages;

public class HaltTrigger extends Trigger {
    private static boolean halted = false;

    private HaltTrigger() {}

    @Override
    public void execute() {
        if (halted) {
            addHaltTrigger();
        } else {
            Messages.addToFront(new MessageUpdate().withUpdate(MessageUpdateType.RESET_STATE));
        }
    }

    public static void addHaltTrigger() {
        halted = true;
        Messages.addToFront(new MessageUpdate().withTrigger(new HaltTrigger()));
    }

    public static void resume() {
        halted = false;
    }

    public static boolean isHalted() {
        return halted;
    }
}
