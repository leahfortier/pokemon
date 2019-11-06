package map.entity.interaction;

import main.Game;
import map.condition.Condition;
import map.triggers.Trigger;
import trainer.player.Player;

import java.util.HashMap;
import java.util.Map;

public class InteractionMap<InteractionType extends Interaction> {
    private final String entityName;

    private final String startKey;
    private final Map<String, InteractionType> interactions;

    private Map<String, Trigger> triggerInteractionMap;

    public InteractionMap(String entityName, String startKey, Map<String, InteractionType> interactions) {
        this.entityName = entityName;
        this.startKey = startKey;
        this.interactions = interactions;

        this.triggerInteractionMap = new HashMap<>();
    }

    public InteractionType getCurrentInteraction() {
        return this.interactions.get(this.getCurrentInteractionKey());
    }

    private String getCurrentInteractionKey() {
        Player player = Game.getPlayer();
        if (player.hasEntityInteraction(this.entityName)) {
            return player.getEntityInteractionName(this.entityName);
        }

        return this.startKey;
    }

    public Trigger getTrigger(Condition condition) {
        String currentInteraction = this.getCurrentInteractionKey();
        if (!this.triggerInteractionMap.containsKey(currentInteraction)) {
            Interaction interaction = this.interactions.get(currentInteraction);
            Trigger trigger = interaction.getActions().getGroupTrigger(this.entityName, condition);
            this.triggerInteractionMap.put(currentInteraction, trigger);
        }

        return this.triggerInteractionMap.get(currentInteraction);
    }
}
