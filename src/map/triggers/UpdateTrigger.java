package map.triggers;

import main.Game;
import pattern.action.UpdateMatcher;

public class UpdateTrigger extends Trigger {
    private final String npcEntityName;
    private final String newInteractionName;

    public UpdateTrigger(UpdateMatcher matcher) {
        super(matcher.getJson());

        this.npcEntityName = matcher.getNpcEntityName();
        this.newInteractionName = matcher.getInteractionName();
    }

    @Override
    protected void executeTrigger() {
        Game.getPlayer().setNpcInteraction(npcEntityName, newInteractionName);
    }
}
