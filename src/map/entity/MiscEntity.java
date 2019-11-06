package map.entity;

import main.Game;
import map.condition.Condition;
import map.entity.interaction.Interaction;
import map.triggers.Trigger;
import trainer.player.Player;
import util.Point;

import java.util.HashMap;
import java.util.Map;

public class MiscEntity extends Entity {
    private final String startKey;
    private final Map<String, Interaction> interactions;

    private Map<String, Trigger> triggerInteractionMap;

    public MiscEntity(String name, Point location, Condition condition, String startKey,
                      Map<String, Interaction> interactions) {
        super(location, name, condition);
        this.startKey = startKey;
        this.interactions = interactions;

        this.triggerInteractionMap = new HashMap<>();
    }

    private String getCurrentInteractionKey() {
        Player player = Game.getPlayer();
        if (player.hasEntityInteraction(this.getEntityName())) {
            return player.getEntityInteractionName(this.getEntityName());
        }

        return this.startKey;
    }

    @Override
    public Trigger getTrigger() {
        String currentInteraction = this.getCurrentInteractionKey();
        if (!this.triggerInteractionMap.containsKey(currentInteraction)) {
            Interaction interaction = this.interactions.get(currentInteraction);
            Trigger trigger = interaction.getActions().getGroupTrigger(
                    this.getEntityName(),
                    this.getCondition()
            );

            this.triggerInteractionMap.put(currentInteraction, trigger);
        }

        return this.triggerInteractionMap.get(currentInteraction);
    }
}
