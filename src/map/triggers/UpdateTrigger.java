package map.triggers;

import main.Game;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.UpdateMatcher;

public class UpdateTrigger extends Trigger {

    private final String npcEntityName;
    private final String newInteractionName;

    public UpdateTrigger(String name, String contents) {
        super(name, contents);

        UpdateMatcher matcher = AreaDataMatcher.deserialize(contents, UpdateMatcher.class);
        this.npcEntityName = matcher.npcEntityName;
        this.newInteractionName = matcher.interactionName;
    }

    public void execute() {
        super.execute();
        Game.getPlayer().setNpcInteraction(npcEntityName, newInteractionName);
    }
}
