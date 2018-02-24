package pattern.action;

import map.condition.Condition;
import map.triggers.GroupTrigger;
import map.triggers.Trigger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ActionList implements Iterable<ActionMatcher> {
    private final List<ActionMatcher> actions;

    public ActionList(ActionMatcher... actions) {
        this.actions = Arrays.asList(actions);
    }

    @NotNull
    @Override
    public Iterator<ActionMatcher> iterator() {
        return this.actions.iterator();
    }

    public List<ActionMatcher> asList() {
        return new ArrayList<>(actions);
    }

    public Trigger getGroupTrigger(String entityName, Condition condition) {
        final List<Trigger> actionTriggers = actions
                .stream()
                .map(action -> {
                    if (action instanceof EntityActionMatcher) {
                        ((EntityActionMatcher)action).setEntity(entityName);
                    }
                    return action.createNewTrigger();
                })
                .collect(Collectors.toList());

        return new GroupTrigger(condition, actionTriggers);
    }
}
