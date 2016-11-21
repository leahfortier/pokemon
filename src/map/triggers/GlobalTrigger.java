package map.triggers;

import java.util.Collections;

public class GlobalTrigger extends Trigger {
    GlobalTrigger(String global, String condition) {
        super(TriggerType.GLOBAL, global, condition, Collections.singletonList(global));
    }

    protected void executeTrigger() {}
}
