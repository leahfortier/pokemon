package battle.effect;

import main.Global;

public abstract class ApplyResult {
    public static ApplyResult newResult(boolean success) {
        return success ? success() : failure();
    }

    public static ApplyResult success() {
        return new SuccessResult();
    }

    public static ApplyResult failure() {
        return failure(Effect.DEFAULT_FAIL_MESSAGE);
    }

    public static ApplyResult failure(String failMessage) {
        return new FailureResult(failMessage);
    }

    public abstract boolean isSuccess();
    public abstract String getMessage();

    private static class SuccessResult extends ApplyResult {

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public String getMessage() {
            Global.error("Error message is only for failure results");
            return "";
        }
    }

    private static class FailureResult extends ApplyResult {
        private final String failMessage;

        public FailureResult(String failMessage) {
            this.failMessage = failMessage;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String getMessage() {
            return this.failMessage;
        }
    }
}
