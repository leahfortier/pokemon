package battle.effect;

import main.Global;

public abstract class ApplyStatus {
    public static ApplyStatus success() {
        return new SuccessStatus();
    }

    public static ApplyStatus failure() {
        return failure(Effect.DEFAULT_FAIL_MESSAGE);
    }

    public static ApplyStatus failure(String failMessage) {
        return new FailureStatus(failMessage);
    }

    public abstract boolean isSuccess();
    public abstract String getMessage();

    private static class SuccessStatus extends ApplyStatus {

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public String getMessage() {
            Global.error("Error message is only for failure statuses");
            return "";
        }
    }

    private static class FailureStatus extends ApplyStatus {
        private final String failMessage;

        public FailureStatus(String failMessage) {
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
