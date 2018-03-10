package pattern.action;

import util.serialization.JsonMatcher;

public class UpdateMatcher implements JsonMatcher {
    private String npcEntityName;
    private String interactionName;

    public UpdateMatcher(final String npcEntityName, final String interactionName) {
        this.npcEntityName = npcEntityName;
        this.interactionName = interactionName;
    }

    public String getNpcEntityName() {
        return this.npcEntityName;
    }

    public String getInteractionName() {
        return this.interactionName;
    }
}
