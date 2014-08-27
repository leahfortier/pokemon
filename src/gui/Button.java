package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import main.InputControl;
import main.InputControl.Control;

public class Button
{	
	public int x, y, width, height;
	private boolean hover, press, forceHover, active;
	private ButtonHoverAction hoverAction;
	private int[] transition;

	public Button(int xx, int yy, int ww, int hh, HoverAction ha)
	{
		this(xx, yy, ww, hh, ha, new int[] { -1, -1, -1, -1 });
	}

	public Button(int xx, int yy, int ww, int hh, HoverAction ha, int[] trans)
	{
		x = xx;
		y = yy;
		width = ww;
		height = hh;
		hoverAction = ha == null ? null : ha.hoverAction;
		hover = press = forceHover = false;
		active = true;
		transition = trans;
	}
	
	public enum HoverAction
	{
		BOX(new ButtonHoverAction()
		{
			private int time = 0;
			private Stroke lineStroke = new BasicStroke(5f);
			
			public void draw(Graphics g, Button button) 
			{
				time = (time + 1)%80;
				
				g.setColor(new Color(0, 0, 0, 55 + 150*(Math.abs(time - 40))/40));
				Graphics2D g2d = (Graphics2D)g;
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
				time = (time + 1)%80;
				
				int x = button.x - 10;
				int y = button.y + button.height/2 - 12;
				
				g.translate(x, y);
				
				g.setColor(new Color(0, 0, 0, 55 + 200*(Math.abs(time - 40))/40));
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
	
	public static int update(Button[] ary, int selected, InputControl input)
	{
		if (!ary[selected].forceHover) ary[selected].setForceHover(true);
		if (input.isDown(Control.RIGHT))
		{
			input.consumeKey(Control.RIGHT);
			selected = Button.transition(ary, selected, 0);
		}
		else if (input.isDown(Control.UP))
		{
			input.consumeKey(Control.UP);
			selected = Button.transition(ary, selected, 1);
		}
		else if (input.isDown(Control.LEFT))
		{
			input.consumeKey(Control.LEFT);
			selected = Button.transition(ary, selected, 2);
		}
		else if (input.isDown(Control.DOWN))
		{
			input.consumeKey(Control.DOWN);
			selected = Button.transition(ary, selected, 3);
		}

		for (int i = 0; i < ary.length; i++)
		{
			ary[i].update(input, i == selected);
			if (ary[i].isHover())
			{
				ary[selected].setForceHover(false);
				selected = i;
				ary[selected].setForceHover(true);
			}
		}
		return selected;
	}

	public static int transition(Button[] ary, int index, int direction)
	{
		int next = ary[index].transition[direction];
		while (next != -1 && !ary[next].isActive())
			next = ary[next].transition[direction];
		if (next == -1) return index;
		ary[index].setForceHover(false);
		ary[next].setForceHover(true);
		return next;
	}
	
	public void update(InputControl input, boolean isSelected, Control... optionalKeys)
	{
		if (!active) return;
		int mx = input.mouseX;
		int my = input.mouseY;
		hover = press = false;

		if (input.isMouseInput())
		{
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
	
	public void draw(Graphics g)
	{
		if ((hover || forceHover) && active && hoverAction != null) hoverAction.draw(g, this);
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
}
