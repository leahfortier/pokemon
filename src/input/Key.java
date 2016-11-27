package input;

class Key {
    private boolean isDown;
    private int[] id; // TODO: Change to set

    public Key(int... keyId) {
        isDown = false;
        id = keyId;
    }

    void consume() {
        isDown = false;
    }

    public void reset() {
        isDown = false;
    }

    boolean isKey(char keyChar) {
        return isKey((int)keyChar);
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
}
