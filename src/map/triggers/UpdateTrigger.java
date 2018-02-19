package map.triggers;

import main.Game;
import map.condition.Condition;
import pattern.action.UpdateMatcher;

public class UpdateTrigger extends Trigger {
    private final String npcEntityName;
    private final String newInteractionName;

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
