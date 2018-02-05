package map.triggers;

import map.condition.Condition;

import java.util.Collections;

class GlobalTrigger extends Trigger {
    GlobalTrigger(String global, Condition condition) {
        super(TriggerType.GLOBAL, global, condition, Collections.singletonList(global));
    }

    // Only purpose is to add a global, which is passed into the constructor and handled by the superclass
    @Override
    protected void executeTrigger() {}
}
