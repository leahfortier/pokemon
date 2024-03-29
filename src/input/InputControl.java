package input;

import main.Game;
import util.Point;
import util.RandomUtils;
import util.string.StringAppender;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * To use the textCapture feature, first acquire the lock (and hold onto the key it returns), then
 * startTextCapture and wait until isDown(ControlKey.ENTER, YOUR_KEY_HERE) or however you want to end input.
 * Then release the lock. Easy peasy.
 */
public class InputControl implements MouseListener, KeyListener, MouseMotionListener {
    private static InputControl instance;

    public static InputControl instance() {
        if (instance == null) {
            instance = new InputControl();
        }
        return instance;
    }

    public static final int INVALID_LOCK = -1;

    // Ignored during text capture (not added to captureText)
    private static final Set<ControlKey> IGNORED_INPUT_KEYS = new HashSet<>(Arrays.asList(
            ControlKey.BACK,
            ControlKey.ENTER,
            ControlKey.ESC
    ));

    private Point mouseLocation;
    private boolean mouseDown;
    private boolean isMouseInput;

    private StringAppender capturedText;
    private boolean isCaptureText;

    private int lock;

    private InputControl() {
        mouseLocation = new Point();
        mouseDown = false;
        isCaptureText = false;

        lock = INVALID_LOCK;
    }

    // Gives you the lock. Careful you don't lose that. -1 means unable to acquire lock
    public int getLock() {
        while (lock == INVALID_LOCK) {
            lock = RandomUtils.getRandomInt(Integer.MAX_VALUE);
        }

        return lock;
    }

    // Releases the lock if you have it
    public boolean releaseLock(int key) {
        if (lock != key) {
            return false;
        }

        lock = INVALID_LOCK;
        return true;
    }

    public void popViewIfEscaped() {
        if (consumeIfDown(ControlKey.ESC)) {
            Game.instance().popView();
        }
    }

    // Returns true if ANY of the input keys has been pressed (and will consume the press)
    public boolean consumeIfDown(ControlKey... controlKeys) {
        for (ControlKey controlKey : controlKeys) {
            if (consumeIfDown(controlKey, INVALID_LOCK)) {
                return true;
            }
        }
        return false;
    }

    public boolean consumeIfDown(ControlKey controlKey, int key) {
        if (this.isDown(controlKey, key)) {
            this.consumeKeys(key);
            return true;
        }

        return false;
    }

    public boolean isDown(ControlKey controlKey, boolean consume) {
        if (consume) {
            return consumeIfDown(controlKey);
        } else {
            return isDown(controlKey, INVALID_LOCK);
        }
    }

    // If there actually isn't a lock, or we have the lock, return the correct value, otherwise return false
    private boolean isDown(ControlKey controlKey, int key) {
        if (controlKey == null) {
            return false;
        }

        if (key == INVALID_LOCK || lock == key) {
            return controlKey.getKey().isDown();
        }

        return false;
    }

    private void consumeKeys(int key) {
        if (lock == key) {
            ControlKey.consumeAll();
        }
    }

    public void resetKeys() {
        ControlKey.resetAll();
        mouseDown = false;
    }

    public boolean consumeIfMouseDown(ControlKey controlKey) {
        return consumeIfDown(controlKey) || consumeIfMouseDown();
    }

    public boolean consumeIfMouseDown() {
        if (mouseDown) {
            mouseDown = false;
            return true;
        }

        return false;
    }

    public void startTextCapture() {
        capturedText = new StringAppender();
        isCaptureText = true;
    }

    public String stopTextCapture() {
        isCaptureText = false;
        return getCapturedText();
    }

    public String stopAndResetCapturedText() {
        String capturedText = this.stopTextCapture();
        this.capturedText = null;
        return capturedText;
    }

    public String getCapturedText() {
        return capturedText == null ? "" : capturedText.toString();
    }

    public String getCapturedText(int maxLength) {
        trimCapturedText(maxLength);
        return this.getCapturedText();
    }

    private void trimCapturedText(int maxLength) {
        if (capturedText != null && capturedText.length() > maxLength) {
            capturedText.setLength(maxLength);
        }
    }

    public boolean isCapturingText() {
        return isCaptureText;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {}

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {}

    @Override
    public void mouseExited(MouseEvent mouseEvent) {}

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        isMouseInput = true;
        mouseDown = true;
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        mouseDown = false;
    }

    private List<Key> getKeys(KeyEvent keyEvent) {
        if (isCaptureText) {
            return ControlKey.getKeys(IGNORED_INPUT_KEYS, keyEvent);
        } else {
            return ControlKey.getKeys(keyEvent);
        }
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        isMouseInput = false;
        for (Key key : this.getKeys(keyEvent)) {
            key.setDown(true);
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        for (Key key : this.getKeys(keyEvent)) {
            key.setDown(false);
        }
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
        if (isCaptureText) {

            // Append the character if it's not ignored
            if (Character.isDefined(keyEvent.getKeyChar()) && !isIgnored(keyEvent)) {
                capturedText.append(keyEvent.getKeyChar());
            }

            // Delete the last character if backspace was typed
            if (keyEvent.getKeyChar() == KeyEvent.VK_BACK_SPACE && !capturedText.isEmpty()) {
                capturedText.setLength(capturedText.length() - 1);
            }
        }
    }

    private boolean isIgnored(KeyEvent keyEvent) {
        return IGNORED_INPUT_KEYS.stream()
                                 .map(ControlKey::getKey)
                                 .anyMatch(key -> key.isKey(keyEvent.getKeyChar()));
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        mouseMoved(mouseEvent);
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        mouseLocation = new Point(
                mouseEvent.getX(),
                mouseEvent.getY()
        );
    }

    public Point getMouseLocation() {
        return this.mouseLocation;
    }

    public boolean isMouseInput() {
        return isMouseInput;
    }

    public String getInputCaptureString(int maxLength) {
        StringAppender display = new StringAppender(this.getCapturedText(maxLength));
        display.appendRepeat("_", maxLength - display.length());
        for (int i = 0; i < maxLength; i++) {
            display.insert(2*i + 1, " ");
        }

        return display.toString();
    }
}
