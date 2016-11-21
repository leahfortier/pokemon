package map.triggers;

import main.Game;
import pattern.action.UpdateMatcher;
import util.JsonUtils;

class UpdateTrigger extends Trigger {

    private final String npcEntityName;
    private final String newInteractionName;

    UpdateTrigger(String matcherJson, String condition) {
        super(TriggerType.UPDATE, matcherJson, condition);

        UpdateMatcher matcher = JsonUtils.deserialize(matcherJson, UpdateMatcher.class);
        this.npcEntityName = matcher.npcEntityName;
        this.newInteractionName = matcher.interactionName;
    }

    protected void executeTrigger() {
        Game.getPlayer().setNpcInteraction(npcEntityName, newInteractionName);
    }
}
