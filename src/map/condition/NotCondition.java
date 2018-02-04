package map.condition;

public class NotCondition implements Condition {
    private final Condition condition;

    public NotCondition(Condition condition) {
        this.condition = condition;
    }

    @Override
    public boolean evaluate() {
        return !this.condition.evaluate();
    }
}
