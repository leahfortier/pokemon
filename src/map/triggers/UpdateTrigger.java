package map.triggers;

import main.Game;
import map.condition.Condition;
import pattern.action.UpdateMatcher;
import util.SerializationUtils;

public class UpdateTrigger extends Trigger {
    private final String npcEntityName;
    private final String newInteractionName;

    UpdateTrigger(String matcherJson, Condition condition) {
        this(SerializationUtils.deserializeJson(matcherJson, UpdateMatcher.class), condition);
    }

    public UpdateTrigger(UpdateMatcher matcher, Condition condition) {
        super(matcher.getJson(), condition);

        this.npcEntityName = matcher.getNpcEntityName();
        this.newInteractionName = matcher.getInteractionName();
    }

    @Override
    protected void executeTrigger() {
        Game.getPlayer().setNpcInteraction(npcEntityName, newInteractionName);
    }
}
