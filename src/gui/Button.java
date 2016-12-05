package gui;

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
			
			if (buttons[i].hover) {
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

	public void blackOutline(Graphics g, List<Direction> directions) {
		DrawUtils.blackOutline(g, x, y, width, height, directions.toArray(new Direction[0]));
	}

	public void label(Graphics g, int fontSize, String text) {
		g.setColor(Color.BLACK);
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
}
