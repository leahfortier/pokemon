package map.condition;

public class ConditionSet {
    private Condition condition;

    public ConditionSet(Condition condition) {
        this.condition = condition == null ? new AndCondition() : condition;
    }

    public boolean evaluate() {
        return this.condition.evaluate();
    }

    public Condition getCondition() {
        return this.condition;
    }
}
