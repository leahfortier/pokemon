package map.condition;

import java.util.ArrayList;
import java.util.List;

public class AndCondition implements Condition {
    private List<Condition> conditions;

    public AndCondition(Condition... conditions) {
        this.conditions = new ArrayList<>();
        for (Condition condition : conditions) {
            this.and(condition);
        }
    }

    public AndCondition and(Condition condition) {
        if (condition != null) {
            this.conditions.add(condition);
        }

        return this;
    }

    @Override
    public boolean evaluate() {
        for (Condition condition : conditions) {
            if (!condition.evaluate()) {
                return false;
            }
        }

        return true;
    }
}
