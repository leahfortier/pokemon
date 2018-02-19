package map.triggers;

import java.util.Collections;

public class GlobalTrigger extends Trigger {
    public GlobalTrigger(String global) {
        super(global, null, Collections.singletonList(global));
    }

    // Only purpose is to add a global, which is passed into the constructor and handled by the superclass
    @Override
    protected void executeTrigger() {}
}
