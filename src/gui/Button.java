package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import main.InputControl;
import main.InputControl.Control;
import map.entity.MovableEntity.Direction;

public class Button
{
	public static final int NO_TRANSITON = -1;
	
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

	public Button(int x, int y, int w, int h, HoverAction hoverAction)
	{
		this(x, y, w, h, hoverAction, new int[] { NO_TRANSITON, NO_TRANSITON, NO_TRANSITON, NO_TRANSITON });
	}

	public Button(int x, int y, int width, int height, HoverAction hoverAction, int[] transition)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		
		this.hoverAction = hoverAction == null ? null : hoverAction.hoverAction;
		
		this.transition = transition;
		
		this.hover = false;
		this.press = false;
		this.forceHover = false;
		this.active = true;
	}
	
	public void draw(Graphics g)
	{
		if ((hover || forceHover) && active && hoverAction != null)
			hoverAction.draw(g, this);
	}
	
	public static int basicLeft(int currentIndex, int numCols)
	{
		return currentIndex == 0 ? numCols - 1 : currentIndex - 1;
	}
	
	public static int basicRight(int currentIndex, int numCols)
	{
		return currentIndex == numCols - 1 ? 0 : currentIndex + 1;
	}
	
	public static int basicUp(int currentIndex, int numRows)
	{
		return currentIndex == 0 ? numRows - 1 : currentIndex - 1;
	}
	
	public static int basicDown(int currentIndex, int numRows)
	{
		return currentIndex == numRows - 1 ? 0 : currentIndex + 1;
	}

	public enum HoverAction
	{
		BOX(new ButtonHoverAction()
		{
			private int time = 0;
			private Stroke lineStroke = new BasicStroke(5f);

			public void draw(Graphics g, Button button)
			{
				time = (time + 1) % 80;

				g.setColor(new Color(0, 0, 0, 55 + 150 * (Math.abs(time - 40)) / 40));
				Graphics2D g2d = (Graphics2D) g;
				Stroke oldStroke = g2d.getStroke();
				g2d.setStroke(lineStroke);
				g.drawRect(button.x - 2, button.y - 2, button.width + 3, button.height + 4);
				g2d.setStroke(oldStroke);
			}
		}), 
		ARROW(new ButtonHoverAction()
		{
			private final int[] tx = {0, 11, 0};
			private final int[] ty = {0, 12, 23};
			private int time = 0;

			public void draw(Graphics g, Button button)
			{
				time = (time + 1) % 80;

				int x = button.x - 10;
				int y = button.y + button.height / 2 - 12;

				g.translate(x, y);

				g.setColor(new Color(0, 0, 0, 55 + 200 * (Math.abs(time - 40)) / 40));
				g.fillPolygon(tx, ty, 3);

				g.translate(-x, -y);
			}
		});

		private ButtonHoverAction hoverAction;

		private HoverAction(ButtonHoverAction hoverAction)
		{
			this.hoverAction = hoverAction;
		}
	}

	public static int update(Button[] buttons, int selected, InputControl input)
	{
		if (!buttons[selected].forceHover)
			buttons[selected].setForceHover(true);
		
		for (Direction direction : Direction.values())
		{
			if (input.isDown(direction.key))
			{
				input.consumeKey(direction.key);
				selected = Button.transition(buttons, selected, direction);
			}
		}

		for (int i = 0; i < buttons.length; i++)
		{
			buttons[i].update(input, i == selected);
			
			if (buttons[i].isHover())
			{
				buttons[selected].setForceHover(false);
				
				selected = i;
				buttons[selected].setForceHover(true);
			}
		}
		
		return selected;
	}

	public static int transition(Button[] buttons, int index, Direction direction)
	{
		int next = buttons[index].transition[direction.ordinal()];
		
		while (next != NO_TRANSITON && !buttons[next].isActive())
			next = buttons[next].transition[direction.ordinal()];
		
		if (next == NO_TRANSITON)
			return index;
		
		buttons[index].setForceHover(false);
		buttons[next].setForceHover(true);
		
		return next;
	}

	public void update(InputControl input, boolean isSelected, Control... optionalKeys)
	{
		if (!active)
			return;
		
		hover = false;
		press = false;

		if (input.isMouseInput())
		{
			int mx = input.mouseX;
			int my = input.mouseY;
			
			if (mx >= x && my >= y && mx <= x + width && my <= y + height)
			{
				hover = true;
				if (input.mouseDown)
				{
					input.consumeMousePress();
					press = true;
				}
			}
		}

		if (isSelected && input.isDown(Control.SPACE))
		{
			input.consumeKey(Control.SPACE);
			press = true;
		}

		for (Control c : optionalKeys)
		{
			if (input.isDown(c))
			{
				input.consumeKey(c);
				press = true;
			}
		}
	}

	public void update(InputControl input)
	{
		update(input, false);
	}

	public boolean isHover()
	{
		return hover;
	}

	public boolean isPress()
	{
		return press;
	}

	public boolean checkConsumePress()
	{
		if (isPress())
		{
			press = false;
			return true;
		}

		return false;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setForceHover(boolean set)
	{
		forceHover = set;
	}

	public void setActive(boolean set)
	{
		active = set;
	}
	
	public void greyOut(Graphics g, boolean totesBlacks)
	{
		Color temp = g.getColor();
		g.setColor(totesBlacks ? Color.BLACK : temp.darker());
		g.fillRect(x, y, width, height);
		g.setColor(temp);
	}
}
