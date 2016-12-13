package pattern;

import pattern.generic.TriggerMatcher;

public class MoveNPCTriggerMatcher extends TriggerMatcher {
    private String npcEntityName;
    private String endEntranceName;
    private boolean endLocationIsPlayer;

    public String getNpcEntityName() {
        return this.npcEntityName;
    }

    public String getEndEntranceName() {
        return this.endEntranceName;
    }

    public boolean endLocationIsPlayer() {
        return this.endLocationIsPlayer;
    }
}
