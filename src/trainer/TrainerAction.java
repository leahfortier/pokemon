package trainer;

import java.io.Serializable;

public enum TrainerAction implements Serializable {
    FIGHT(0),
    SWITCH(6),
    ITEM(6),
    RUN(6);
    
    private final int priority;
    
    TrainerAction(int p) {
        priority = p;
    }
    
    public int getPriority() {
        return priority;
    }
}
