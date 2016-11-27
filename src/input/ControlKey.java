package input;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
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
    L(KeyEvent.VK_L); // TODO: Is L for the log view in battle? should this be renamed appropriately?

    private final Key key;

    ControlKey(int... keyIds) {
        this.key = new Key(keyIds);
    }

    Key getKey() {
        return this.key;
    }

    static List<Key> getKeys(KeyEvent keyEvent) {
        return Arrays.stream(values())
                .map(ControlKey::getKey)
                .filter(key -> key.isKey(keyEvent.getKeyCode()))
                .collect(Collectors.toList());
    }
}
