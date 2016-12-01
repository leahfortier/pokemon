package gui;

import input.ControlKey;
import input.InputControl;
import map.Direction;
import util.Point;

import java.awt.Color;
import java.awt.Graphics;

public class Button {
	public static final int NO_TRANSITION = -1;
	
	public final int x;
	public final int y;
	public final int width;
	public final int height;
	
	private final ButtonHoverAction hoverAction;
	private final int[] transition;
	
	private boolean hover;
	private boolean press;
	private boolean forceHover;
	private boolean active;

	public Button(int x, int y, int w, int h, ButtonHoverAction hoverAction) {
		this(x, y, w, h, hoverAction, null);
	}

	public Button(int x, int y, int width, int height, ButtonHoverAction hoverAction, int[] transition) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
		this.hoverAction = hoverAction;

		if (transition == null) {
			this.transition = new int[] { NO_TRANSITION, NO_TRANSITION, NO_TRANSITION, NO_TRANSITION };
		} else {
			this.transition = transition;
		}

		this.hover = false;
		this.press = false;
		this.forceHover = false;
		this.active = true;
	}
	
	public void draw(Graphics g) {
		if ((hover || forceHover) && active && hoverAction != null) {
			hoverAction.draw(g, this);
		}
	}

	// Works for all grid buttons
	public static int[] getBasicTransitions(int currentIndex, int numRows, int numCols) {
		return new int[] {
				basicTransition(currentIndex, numRows, numCols, Direction.RIGHT),
				basicTransition(currentIndex, numRows, numCols, Direction.UP),
				basicTransition(currentIndex, numRows, numCols, Direction.LEFT),
				basicTransition(currentIndex, numRows, numCols, Direction.DOWN)
		};
	}

	public static int basicTransition(int currentIndex, int numRows, int numCols, Direction direction) {
		// Get the corresponding grid index
		Point location = Point.getPointAtIndex(currentIndex, numCols);

		// Move in the given direction
		location = Point.move(location, direction);

		// Keep in bounds of the grid
		location = Point.modInBounds(location, numRows, numCols);

		// Convert back to single dimension index
		return location.getIndex(numCols);
	}

	public static int update(Button[] buttons, int selected) {
		if (!buttons[selected].forceHover) {
			buttons[selected].setForceHover(true);
		}

		Direction inputDirection = Direction.consumeInputDirection();
		if (inputDirection != null) {
			selected = Button.transition(buttons, selected, inputDirection);
		}

		for (int i = 0; i < buttons.length; i++) {
			buttons[i].update(i == selected);
			
			if (buttons[i].isHover()) {
				buttons[selected].setForceHover(false);
				
				selected = i;
				buttons[selected].setForceHover(true);
			}
		}
		
		return selected;
	}

	private static int transition(Button[] buttons, int index, Direction direction) {
		int next = buttons[index].transition[direction.ordinal()];
		
		while (next != NO_TRANSITION && !buttons[next].isActive()) {
			next = buttons[next].transition[direction.ordinal()];
		}
		
		if (next == NO_TRANSITION) {
			return index;
		}
		
		buttons[index].setForceHover(false);
		buttons[next].setForceHover(true);
		
		return next;
	}

	public void update(boolean isSelected, ControlKey... optionalKeys) {
		if (!active) {
			return;
		}
		
		hover = false;
		press = false;

		InputControl input = InputControl.instance();
		if (input.isMouseInput()) {
			Point mouseLocation = input.getMouseLocation();

			int mx = mouseLocation.x;
			int my = mouseLocation.y;
			
			if (mx >= x && my >= y && mx <= x + width && my <= y + height) {
				hover = true;
				if (input.consumeIfMouseDown()) {
					press = true;
				}
			}
		}

		if (isSelected && input.consumeIfDown(ControlKey.SPACE)) {
			press = true;
		}

		for (ControlKey c : optionalKeys) {
			if (input.consumeIfDown(c)) {
				press = true;
			}
		}
	}

	public void update() {
		update(false);
	}

	public boolean isHover() {
		return hover;
	}

	public boolean isPress() {
		return press;
	}

	public boolean checkConsumePress() {
		if (isPress()) {
			press = false;
			return true;
		}

		return false;
	}

	public boolean isActive() {
		return active;
	}

	public void setForceHover(boolean set) {
		forceHover = set;
	}

	public void setActive(boolean set) {
		active = set;
	}
	
	public void greyOut(Graphics g, boolean totesBlacks) {
		Color temp = g.getColor();
		g.setColor(totesBlacks ? Color.BLACK : temp.darker());
		g.fillRect(x, y, width, height);
		g.setColor(temp);
	}
}
