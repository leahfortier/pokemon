package map.triggers;

public class GlobalTrigger extends Trigger {
    GlobalTrigger(String global, String condition) {
        super(TriggerType.GLOBAL, global, condition, global);
    }

    protected void executeTrigger() {}
}
