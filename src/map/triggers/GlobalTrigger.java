package map.triggers;

import java.util.Collections;

class GlobalTrigger extends Trigger {
    GlobalTrigger(String global, String condition) {
        super(TriggerType.GLOBAL, global, condition, Collections.singletonList(global));
    }
    
    // Only purpose is to add a global, which is passed into the constructor and handled by the superclass
    protected void executeTrigger() {}
}
