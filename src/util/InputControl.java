package util;

import main.Global;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;

/*
 * To use the textCapture feature, first acquire the lock (and hold onto the key it returns), then
 * startTextCapture and wait until isDown(Control.ENTER, YOUR_KEY_HERE) or however you want to end input.
 * Then release the lock. Easy peasy.
 */
public class InputControl implements MouseListener, KeyListener, MouseMotionListener {

	// TODO: How is this being used? Should the Key class somehow map to this enum or vice versa?
	public enum Control {
		UP,
		DOWN,
		LEFT,
		RIGHT,
		ESC,
		SPACE,
		BACK,
		CONSOLE,
		ENTER,
		L,
	}
		
	private class Key {
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

		public boolean isKey(int keyCode) {
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
		
		public boolean isTyped() {
			return isTyped;
		}
	}

	public int mouseX, mouseY;
	private boolean mouseDown;
	private Key[] keyList;
	private StringBuilder capturedText;
	private boolean isCaptureText;
	private boolean isMouseInput;
	
	public static final int INVALID_LOCK = -1;
	private int lock;
	
	private char[] ignoredInputCharacters;
	private Map<Control, Key> keyMap;

	public InputControl() {
		Key downKey = new Key(KeyEvent.VK_S, KeyEvent.VK_DOWN);
		Key upKey = new Key(KeyEvent.VK_W, KeyEvent.VK_UP);
		Key leftKey = new Key(KeyEvent.VK_A, KeyEvent.VK_LEFT);
		Key rightKey = new Key(KeyEvent.VK_D, KeyEvent.VK_RIGHT);
		Key escKey = new Key(KeyEvent.VK_ESCAPE);
		Key spaceKey = new Key(KeyEvent.VK_SPACE, KeyEvent.VK_ENTER); // TODO: Change name to something like nextKey
		Key backKey = new Key(KeyEvent.VK_BACK_SPACE);
		Key consoleKey = new Key(KeyEvent.VK_BACK_QUOTE);
		Key enterKey = new Key(KeyEvent.VK_ENTER);
		Key lKey = new Key(KeyEvent.VK_L);
		
		keyList = new Key[] { downKey, upKey, leftKey, rightKey, escKey, spaceKey, backKey, consoleKey, enterKey, lKey };
		keyMap = new HashMap<>(); // TODO: enum map
		keyMap.put(Control.UP, upKey);
		keyMap.put(Control.DOWN, downKey);
		keyMap.put(Control.LEFT, leftKey);
		keyMap.put(Control.RIGHT, rightKey);
		keyMap.put(Control.ESC, escKey);
		keyMap.put(Control.SPACE, spaceKey);
		keyMap.put(Control.BACK, backKey);
		keyMap.put(Control.CONSOLE, consoleKey);
		keyMap.put(Control.ENTER, enterKey);
		keyMap.put(Control.L, lKey);
		
		// Ignored during text capture (not added to captureText)
		ignoredInputCharacters = new char[]{ KeyEvent.VK_BACK_SPACE, KeyEvent.VK_ENTER, KeyEvent.VK_ESCAPE };
		
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
	
	// Releases the lock if you have it.
	public boolean releaseLock(int key) {
		if (lock != key) {
			return false;
		}
		
		lock = INVALID_LOCK;
		return true;
	}

	public boolean consumeIfDown(Control control) {
		if (this.isDown(control)) {
			this.consumeKey(control);
			return true;
		}

		return false;
	}
	
	// If there is no lock currently, return the correct value, else return false
	private boolean isDown(Control c) {
		if (c == null) {
			return false;
		}

		if (lock != INVALID_LOCK) {
			return false;
		}

		return keyMap.get(c).isDown();
	}

	public boolean consumeIfDown(Control control, int key) {
		if (this.isDown(control, key)) {
			this.consumeKey(control, key);
			return true;
		}

		return false;
	}
	
	// If there actually isn't a lock, or we have the lock, return the correct value, else
	// return false
	private boolean isDown(Control c, int key) {
		if (key == INVALID_LOCK || lock == key) {
			return keyMap.get(c).isDown;
		}

		return false;
	}
	
	private void consumeKey(Control c) {
		consumeKey(c, INVALID_LOCK);
	}
	
	private void consumeKey(Control c, int key) {
		if (lock == key) {
			keyMap.get(c).consume();
		}
	}

	public void resetKeys() {
		for (Key k : keyList) {
			k.reset();
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

	public void mouseClicked(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {
		isMouseInput = true;
		mouseDown = true;
	}

	public void mouseReleased(MouseEvent e) {
		mouseDown = false;
	}

	public void keyPressed(KeyEvent e) {
		isMouseInput = false;
		for (Key k : keyList) {
			if (k.isKey(e.getKeyCode())) {
				k.isDown = true;
			}
		}
	}

	public void keyReleased(KeyEvent e) {
		for (Key k : keyList) {
			if (k.isKey(e.getKeyCode())) {
				k.isDown = false;
			}
		}
	}

	public void keyTyped(KeyEvent e) {
		for (Key k : keyList) {
			if (k.isKey(e.getKeyCode())) {
				k.isTyped = true;
			}
		}

		// Append the character if it's not ignored
		if (isCaptureText && Character.isDefined(e.getKeyChar()) && !isIgnored(e.getKeyChar())) {
			capturedText.append(e.getKeyChar());
		}

		// Delete the last character if backspace was typed
		if (isCaptureText && e.getKeyChar() == KeyEvent.VK_BACK_SPACE && capturedText.length() > 0) {
			capturedText.setLength(capturedText.length() - 1);
		}
	}

	// TODO: Okay these are some god awful variable names
	private boolean isIgnored(char c) {
		for (char cc : ignoredInputCharacters) {
			if (c == cc) {
				return true;
			}
		}
		
		return false;
	}

	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	public boolean isMouseInput() {
		return isMouseInput;
	}
}
