package input;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class Key {
    private final Set<Integer> ids;

    private boolean isDown;

    public Key(int... keyIds) {
        ids = IntStream.of(keyIds).boxed().collect(Collectors.toSet());
        isDown = false;
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
        return ids.contains(keyCode);
    }

    public boolean isDown() {
        return isDown;
    }

    void setDown(boolean isDown) {
        this.isDown = isDown;
    }
}
