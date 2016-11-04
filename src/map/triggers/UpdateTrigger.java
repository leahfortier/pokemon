package map.triggers;

import main.Game;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.UpdateMatcher;

public class UpdateTrigger extends Trigger {

    private final String npcEntityName;
    private final String newInteractionName;

    UpdateTrigger(String matcherJson) {
        super(TriggerType.UPDATE, matcherJson);

        UpdateMatcher matcher = AreaDataMatcher.deserialize(matcherJson, UpdateMatcher.class);
        this.npcEntityName = matcher.npcEntityName;
        this.newInteractionName = matcher.interactionName;
    }

    protected void executeTrigger() {
        Game.getPlayer().setNpcInteraction(npcEntityName, newInteractionName);
    }
}
