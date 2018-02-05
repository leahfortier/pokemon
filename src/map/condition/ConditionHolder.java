package map.condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// For conditions that hold other conditions
public interface ConditionHolder extends Condition {
    List<Condition> getConditions();

    class AndCondition implements ConditionHolder {
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
        public List<Condition> getConditions() {
            return this.conditions;
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

    class OrCondition implements ConditionHolder {
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
        public List<Condition> getConditions() {
            return this.conditions;
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

    class NotCondition implements ConditionHolder {
        private final Condition condition;

        public NotCondition(Condition condition) {
            this.condition = condition;
        }

        @Override
        public List<Condition> getConditions() {
            return Collections.singletonList(this.condition);
        }

        @Override
        public boolean evaluate() {
            return !this.condition.evaluate();
        }
    }
}
