package input;

class Key {
    private boolean isDown;
    private boolean isTyped;
    private int[] id;

    public Key(int... keyId) {
        isDown = isTyped = false;
        id = keyId;
    }

    void consume() {
        isTyped = false;
        isDown = false;
    }

    public void reset() {
        isDown = false;
        isTyped = false;
    }

    boolean isKey(int keyCode) {
        for (int i : id) {
            if (i == keyCode) {
                return true;
            }
        }

        return false;
    }

    public boolean isDown() {
        return isDown;
    }

    void setDown(boolean isDown) {
        this.isDown = isDown;
    }

    boolean isTyped() {
        return isTyped;
    }

    void setTyped(boolean isTyped) {
        this.isTyped = isTyped;
    }
}
