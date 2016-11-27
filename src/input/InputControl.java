package input;

import main.Global;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/*
 * To use the textCapture feature, first acquire the lock (and hold onto the key it returns), then
 * startTextCapture and wait until isDown(ControlKey.ENTER, YOUR_KEY_HERE) or however you want to end input.
 * Then release the lock. Easy peasy.
 */
public class InputControl implements MouseListener, KeyListener, MouseMotionListener {

	// Ignored during text capture (not added to captureText)
	private static final Set<Character> IGNORED_INPUT_CHARACTERS = new HashSet<>(Arrays.asList(new Character[] {
					KeyEvent.VK_BACK_SPACE,
					KeyEvent.VK_ENTER,
					KeyEvent.VK_ESCAPE
			}));

	// TODO: Point and private
	public int mouseX;
	public int mouseY;
	private boolean mouseDown;
	private boolean isMouseInput;

	private StringBuilder capturedText;
	private boolean isCaptureText;

	public static final int INVALID_LOCK = -1;
	private int lock;

	public InputControl() {
		mouseX = 0;
		mouseY = 0;
		mouseDown = false;
		isCaptureText = false;
		
		lock = INVALID_LOCK;
	}

	// Gives you the lock. Careful you don't lose that. -1 means unable to acquire lock
	public int getLock() {
		// TODO: Where is this being used? This looks like a typo -- shouldn't it be returning the lock? If so, just remove this whole statement
		if (lock != INVALID_LOCK) {
			return INVALID_LOCK;
		}

		while (lock == INVALID_LOCK) {
			lock = Global.getRandomInt(Integer.MAX_VALUE);
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

	public boolean consumeIfDown(ControlKey controlKey) {
		return consumeIfDown(controlKey, INVALID_LOCK);
	}

	public boolean consumeIfDown(ControlKey controlKey, int key) {
		if (this.isDown(controlKey, key)) {
			this.consumeKey(controlKey, key);
			return true;
		}

		return false;
	}
	
	// If there actually isn't a lock, or we have the lock, return the correct value, else
	// return false
	private boolean isDown(ControlKey controlKey, int key) {
		if (key == INVALID_LOCK || lock == key) {
			return controlKey.getKey().isDown();
		}

		return false;
	}
	
	private void consumeKey(ControlKey controlKey, int key) {
		if (lock == key) {
			controlKey.getKey().consume();
		}
	}

	public void resetKeys() {
		for (ControlKey controlKey : ControlKey.values()) {
			controlKey.getKey().reset();
		}

		mouseDown = false;
	}

	public boolean consumeIfMouseDown() {
		if (mouseDown) {
			mouseDown = false;
			return true;
		}

		return false;
	}

	public void startTextCapture() {
		capturedText = new StringBuilder();
		isCaptureText = true;
	}

	public String stopTextCapture() {
		isCaptureText = false;
		return capturedText.toString();
	}

	public String getCapturedText() {
		return capturedText.toString();
	}

	public boolean isCapturingText() {
		return isCaptureText;
	}

	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		isMouseInput = true;
		mouseDown = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouseDown = false;
	}

	@Override
	public void keyPressed(KeyEvent keyEvent) {
		isMouseInput = false;
		for (Key key : ControlKey.getKeys(keyEvent)) {
			key.setDown(true);
		}
	}

	@Override
	public void keyReleased(KeyEvent keyEvent) {
		for (Key key : ControlKey.getKeys(keyEvent)) {
			key.setDown(false);
		}
	}

	@Override
	public void keyTyped(KeyEvent keyEvent) {
		for (Key key : ControlKey.getKeys(keyEvent)) {
			key.setTyped(true);
		}

		// Append the character if it's not ignored
		if (isCaptureText && Character.isDefined(keyEvent.getKeyChar()) && !isIgnored(keyEvent.getKeyChar())) {
			capturedText.append(keyEvent.getKeyChar());
		}

		// Delete the last character if backspace was typed
		if (isCaptureText && keyEvent.getKeyChar() == KeyEvent.VK_BACK_SPACE && capturedText.length() > 0) {
			capturedText.setLength(capturedText.length() - 1);
		}
	}

	// TODO: Okay these are some god awful variable names
	private boolean isIgnored(char c) {
		return IGNORED_INPUT_CHARACTERS.contains(c);
	}

	@Override
	public void mouseDragged(MouseEvent mouseEvent) {
		mouseMoved(mouseEvent);
	}

	@Override
	public void mouseMoved(MouseEvent mouseEvent) {
		mouseX = mouseEvent.getX();
		mouseY = mouseEvent.getY();
	}

	public boolean isMouseInput() {
		return isMouseInput;
	}
}