package map.triggers;

import map.condition.Condition;

import java.util.Collections;

public class GlobalTrigger extends Trigger {
    public GlobalTrigger(String global, Condition condition) {
        super(global, condition, Collections.singletonList(global));
    }

    // Only purpose is to add a global, which is passed into the constructor and handled by the superclass
    @Override
    protected void executeTrigger() {}
}
