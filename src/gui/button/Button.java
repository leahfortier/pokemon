package gui.button;

import gui.panel.DrawPanel;
import input.ControlKey;
import input.InputControl;
import map.Direction;
import util.DrawUtils;
import util.FontMetrics;
import util.Point;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Button {
	private static final int NO_TRANSITION = -1;

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

	public Button(int x, int y, int width, int height, ButtonHoverAction hoverAction) {
		this(x, y, width, height, hoverAction, null);
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

	public static Button createTabButton(int tabIndex, int panelX, int panelY, int panelWidth, int tabHeight, int numButtons, int[] transitions) {
		int tabWidth = panelWidth/numButtons;
		int remainder = panelWidth%numButtons;

		return new Button(
				panelX + tabIndex*tabWidth + Math.min(tabIndex, remainder),
				panelY - tabHeight + DrawUtils.OUTLINE_SIZE,
				tabWidth + (tabIndex < remainder ? 1 : 0),
				tabHeight,
				ButtonHoverAction.BOX,
				transitions
		);
	}

	public void draw(Graphics g) {
		if ((hover || forceHover) && active && hoverAction != null) {
			hoverAction.draw(g, this);
		}
	}

	// Works for all grid buttons
	public static int[] getBasicTransitions(int currentIndex, int numRows, int numCols) {
		int[] transitions = new int[Direction.values().length];
		for (int i = 0; i < transitions.length; i++) {
			Direction direction = Direction.values()[i];
			transitions[i] = basicTransition(currentIndex, numRows, numCols, direction);
		}

		return transitions;
	}

	// Works for all grid buttons
	public static int[] getBasicTransitions(int currentIndex, int numRows, int numCols, int startValue, int[] defaultTransitions) {
		// Get the corresponding grid index
		Point location = Point.getPointAtIndex(currentIndex, numCols);

		int[] transitions = new int[Direction.values().length];
		for (int i = 0; i < transitions.length; i++) {
			Direction direction = Direction.values()[i];
			Point newLocation = Point.add(location, direction.getDeltaPoint());
			boolean inBounds = newLocation.inBounds(numCols, numRows);

			// Default value specified and out of bounds -- use default value instead of wrapping
			if (defaultTransitions != null
					&& i < defaultTransitions.length
					&& defaultTransitions[i] != -1
					&& !inBounds) {
				transitions[i] = defaultTransitions[i];
			} else {
				transitions[i] = basicTransition(currentIndex, numRows, numCols, direction) + startValue;
			}
		}

		return transitions;
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
			
			if (buttons[i].hover) {
				buttons[selected].setForceHover(false);
				
				selected = i;
				buttons[selected].setForceHover(true);
			}
		}
		
		return selected;
	}

	private static int transition(Button[] buttons, int index, Direction direction) {
		int next = index;
		do {
			next = buttons[next].transition[direction.ordinal()];
		} while (next != NO_TRANSITION && !buttons[next].isActive());

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

	public boolean checkConsumePress() {
		if (press) {
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
		fill(g, totesBlacks ? Color.BLACK : temp.darker());
		g.setColor(temp);
	}

	public void greyOut(Graphics g) {
		DrawUtils.greyOut(g, x, y, width, height);
	}

	public void fillTranslated(Graphics g, Color color) {
		fill(g, color, 0, 0);
	}

	public void fill(Graphics g, Color color) {
		fill(g, color, x, y);
	}

	private void fill(Graphics g, Color color, int x, int y) {
		g.setColor(color);
		g.fillRect(x, y, width, height);
	}

	public void fillBordered(Graphics g, Color color) {
		new DrawPanel(x, y, width, height)
				.withTransparentBackground(color)
				.withBorderPercentage(15)
				.withBlackOutline()
				.drawBackground(g);
	}

	public void fillTransparent(Graphics g, Color color) {
		fill(g, color);
		fillTransparent(g);
	}

	public void fillTransparent(Graphics g) {
		DrawUtils.fillTransparent(g, x, y, width, height);
	}

	public void blackOutline(Graphics g) {
		DrawUtils.blackOutline(g, x, y, width, height);
	}

	public void outlineTab(Graphics g, int index, int selectedIndex) {
		List<Direction> toOutline = new ArrayList<>();
		toOutline.add(Direction.UP);
		toOutline.add(Direction.RIGHT);

		if (index == 0) {
			toOutline.add(Direction.LEFT);
		}

		if (index != selectedIndex) {
			toOutline.add(Direction.DOWN);
		}

		DrawUtils.blackOutline(g, x, y, width, height, toOutline.toArray(new Direction[0]));
	}

	public void label(Graphics g, int fontSize, String text) {
		label(g, fontSize, Color.BLACK, text);
	}

	public void label(Graphics g, int fontSize, Color color, String text) {
		g.setColor(color);
		FontMetrics.setFont(g, fontSize);
		DrawUtils.drawCenteredString(g, text, x, y, width, height);
	}

	public void imageLabel(Graphics g, BufferedImage image) {
		DrawUtils.drawCenteredImage(g, image, centerX(), centerY());
	}

	public int centerX() {
		return x + width/2;
	}

	public int centerY() {
		return y + height/2;
	}

	public void drawArrow(Graphics g, Direction direction) {
		DrawUtils.drawArrow(g, x, y, width, height, direction);
	}
}
