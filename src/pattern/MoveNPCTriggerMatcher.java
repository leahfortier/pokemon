package pattern;

import pattern.generic.TriggerMatcher;

public class MoveNPCTriggerMatcher extends TriggerMatcher {
    private String npcEntityName;
    private String endEntranceName;
    private boolean endLocationIsPlayer;

    public MoveNPCTriggerMatcher(String npcEntityName, String endEntranceName, boolean endLocationIsPlayer) {
        this.npcEntityName = npcEntityName;
        this.endLocationIsPlayer = endLocationIsPlayer;

        // Ending at the player and another entrance are mutually exclusive
        if (!endLocationIsPlayer) {
            this.endEntranceName = endEntranceName;
        }
    }

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
