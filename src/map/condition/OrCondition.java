package map.condition;

import java.util.ArrayList;
import java.util.List;

public class OrCondition implements Condition {
    private final List<Condition> conditions;

    public OrCondition(Condition... conditions) {
        this.conditions = new ArrayList<>();
        for (Condition condition : conditions) {
            this.or(condition);
        }
    }

    public OrCondition or(Condition condition) {
        if (condition != null) {
            this.conditions.add(condition);
        }

        return this;
    }

    @Override
    public boolean evaluate() {
        if (this.conditions.isEmpty()) {
            return true;
        }

        for (Condition condition : conditions) {
            if (condition.evaluate()) {
                return true;
            }
        }

        return false;
    }
}
