package input;

import java.awt.event.KeyEvent;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum ControlKey {
    UP(KeyEvent.VK_W, KeyEvent.VK_UP),
    DOWN(KeyEvent.VK_S, KeyEvent.VK_DOWN),
    LEFT(KeyEvent.VK_A, KeyEvent.VK_LEFT),
    RIGHT(KeyEvent.VK_D, KeyEvent.VK_RIGHT),
    ESC(KeyEvent.VK_ESCAPE),
    SPACE(KeyEvent.VK_SPACE, KeyEvent.VK_ENTER), // TODO: Change name to something like nextKey
    BACK(KeyEvent.VK_BACK_SPACE),
    CONSOLE(KeyEvent.VK_BACK_QUOTE),
    ENTER(KeyEvent.VK_ENTER),
    LOG(KeyEvent.VK_L),
    FLY(KeyEvent.VK_F),
    POKEFINDER(KeyEvent.VK_P);

    private static final Set<ControlKey> CONTROL_KEYS = EnumSet.allOf(ControlKey.class);

    private final Key key;

    ControlKey(int... keyIds) {
        this.key = new Key(keyIds);
    }

    Key getKey() {
        return this.key;
    }

    static List<Key> getKeys(KeyEvent keyEvent) {
        return getKeys(CONTROL_KEYS, keyEvent);
    }

    static List<Key> getKeys(Set<ControlKey> controlKeys, KeyEvent keyEvent) {
        return controlKeys.stream()
                .map(ControlKey::getKey)
                .filter(key -> key.isKey(keyEvent.getKeyCode()))
                .collect(Collectors.toList());
    }

    static void resetAll() {
        CONTROL_KEYS.forEach(controlKey -> controlKey.getKey().reset());
    }

    static void consumeAll() {
        CONTROL_KEYS.forEach(controlKey -> controlKey.getKey().consume());
    }
}
