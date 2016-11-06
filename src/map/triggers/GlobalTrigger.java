package map.triggers;

public class GlobalTrigger extends Trigger {
    GlobalTrigger(String global) {
        super(TriggerType.GLOBAL, global, null, global);
    }

    protected void executeTrigger() {}
}
