package map.entity;

import map.condition.Condition;
import map.entity.interaction.Interaction;
import map.entity.interaction.InteractionMap;
import map.triggers.Trigger;
import util.Point;

import java.util.Map;

public class MiscEntity extends Entity {
    private final InteractionMap<Interaction> interactions;

    public MiscEntity(String name, Point location, Condition condition, String startKey,
                      Map<String, Interaction> interactions) {
        super(location, name, condition);
        this.interactions = new InteractionMap<>(this.getEntityName(), startKey, interactions);
    }

    @Override
    public Trigger getTrigger() {
        return this.interactions.getTrigger(this.getCondition());
    }
}
