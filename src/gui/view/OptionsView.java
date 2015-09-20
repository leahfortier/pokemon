package gui.view;

import gui.DrawMetrics;
import gui.GameData;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import main.Game;
import main.Game.ViewMode;
import main.Global;
import main.InputControl;
import main.InputControl.Control;

public class OptionsView extends View
{
	boolean musicOn;
	
	int cr, cb, cg;
	
	public OptionsView()
	{
		musicOn = Global.soundPlayer.isMuted();
		cr = cb = cg = 0;
	}

	public void update(int dt, InputControl input, Game game)
	{
		if (input.mouseDown)
		{
			input.consumeMousePress();
			
			musicOn = !musicOn;
			Global.soundPlayer.toggleMusic();
		}
		
		if (input.isDown(Control.ESC))
		{
			input.consumeKey(Control.ESC);
			game.setViewMode(ViewMode.MAP_VIEW);
		}
	}

	public void draw(Graphics g, GameData data)
	{
		Dimension d = Global.GAME_SIZE;
		
//		g.setColor(Color.BLACK);
		g.setColor(new Color(cr, cb, cg));
		cr += 13; cr %= 255;
		cb += 21; cb %= 255;
		cg += 34; cg %= 255;
		g.fillRect(0, 0, d.width, d.height);
		
		g.setColor(Color.WHITE);
		DrawMetrics.setFont(g, 150);
		DrawMetrics.drawCenteredWidthString(g, "VOLUME", d.width/2, d.height/4);
		
		if (musicOn)
		{
			g.setColor(Color.GREEN);
			g.fillRect(d.width/2, d.height/2 - 50, 200, 100);
			
			g.setColor(Color.DARK_GRAY);
			g.fillRect(d.width/2 - 200, d.height/2 - 50, 200, 100);
			
			g.setColor(Color.BLACK);
			DrawMetrics.setFont(g, 100);
			g.drawString("OFF", d.width/2 - 180, d.height/2 + 32);
		}
		else
		{
			g.setColor(Color.DARK_GRAY);
			g.fillRect(d.width/2, d.height/2 - 50, 200, 100);
			
			g.setColor(Color.RED);
			g.fillRect(d.width/2 - 200, d.height/2 - 50, 200, 100);
			
			g.setColor(Color.BLACK);
			DrawMetrics.setFont(g, 100);
			g.drawString("ON", d.width/2 + 40, d.height/2 + 32);
		}
		

	}

	public ViewMode getViewModel()
	{
		return ViewMode.OPTIONS_VIEW;
	}

	public void movedToFront(Game game){}

}
