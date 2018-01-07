package map.triggers;

import main.Game;
import pattern.action.UpdateMatcher;
import util.SerializationUtils;

class UpdateTrigger extends Trigger {

    private final String npcEntityName;
    private final String newInteractionName;

    UpdateTrigger(String matcherJson, String condition) {
        super(TriggerType.UPDATE, matcherJson, condition);

        UpdateMatcher matcher = SerializationUtils.deserializeJson(matcherJson, UpdateMatcher.class);
        this.npcEntityName = matcher.getNpcEntityName();
        this.newInteractionName = matcher.getInteractionName();
    }

    protected void executeTrigger() {
        Game.getPlayer().setNpcInteraction(npcEntityName, newInteractionName);
    }
}
