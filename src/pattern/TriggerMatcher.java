package pattern;

import map.entity.EntityAction;
import map.entity.EntityAction.TriggerAction;
import map.triggers.TriggerType;
import mapMaker.model.TriggerModel.TriggerModelType;
import util.JsonUtils;
import util.Point;

import java.util.ArrayList;
import java.util.List;

public class TriggerMatcher extends MapMakerEntityMatcher {
    private String name;
    private int[] location;

    private ActionMatcher[] actions;

    private transient boolean isEntity;
    private transient List<Point> points;
    private transient List<EntityAction> entityActions;
    private transient WildBattleTriggerMatcher wildBattleContents;

    public TriggerMatcher(String name, String condition, ActionMatcher[] actions) {
        this.name = name;
        this.actions = actions;

        super.setCondition(condition);
    }

    public List<Point> getLocation() {
        if (points != null) {
            return points;
        }

        this.points = new ArrayList<>();
        if (this.location != null) {
            for (int i = 0; i < this.location.length; i += 2) {
                int x = this.location[i];
                int y = this.location[i + 1];

                this.points.add(new Point(x, y));
            }
        }

        return this.points;
    }

    @Override
    public void addPoint(Point point) {
        MapMakerEntityMatcher.addPoint(point, this.getLocation(), location);
    }

    @Override
    public TriggerModelType getTriggerModelType() {
        if (this.isWildBattleTrigger()) {
            return TriggerModelType.WILD_BATTLE;
        } else if (isEntity) {
            // TODO: isEntity is never set right now, but really this has to be a separate class this shit it way too confusing and I've been meaning to do that for a while now
            return TriggerModelType.TRIGGER_ENTITY;
        } else {
            return TriggerModelType.EVENT;
        }
    }

    @Override
    public String getBasicName() {
        return name;
    }

    public ActionMatcher[] getActionMatchers() {
        return this.actions;
    }

    public List<EntityAction> getActions() {
        if (this.entityActions != null) {
            return this.entityActions;
        }

        this.entityActions = new ArrayList<>();
        for (ActionMatcher matcher : this.actions) {
            this.entityActions.add(matcher.getAction(this.getCondition()));
        }

        return this.entityActions;
    }

    public boolean isWildBattleTrigger() {
        return this.getWildBattleTriggerContents() != null;
    }

    public WildBattleTriggerMatcher getWildBattleTriggerContents() {
        if (this.wildBattleContents != null) {
            return this.wildBattleContents;
        }

        for (EntityAction action : this.getActions()) {
            if (action instanceof TriggerAction) {
                TriggerAction triggerAction = (TriggerAction)action;
                if (triggerAction.getTriggerType() == TriggerType.WILD_BATTLE) {
                    String contents = triggerAction.getTriggerContents(this.name);
                    this.wildBattleContents = JsonUtils.deserialize(contents, WildBattleTriggerMatcher.class);
                    break;
                }
            }
        }

        return this.wildBattleContents;
    }
}
