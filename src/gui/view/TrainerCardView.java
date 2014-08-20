package gui.view;

import gui.GameData;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import main.Game;
import main.Game.ViewMode;
import main.InputControl.Control;
import main.Global;
import main.InputControl;
import trainer.CharacterData;

public class TrainerCardView extends View
{
	CharacterData charData;
	
	public TrainerCardView(CharacterData data)
	{
		charData = data;
	}

	public void update(int dt, InputControl input, Game game)
	{
		if (input.isDown(Control.ESC))
		{
			input.consumeKey(Control.ESC);
			game.setViewMode(ViewMode.MAP_VIEW);
		}
	}

	public void draw(Graphics g, GameData data)
	{
		Dimension d = Global.GAME_SIZE;

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, d.width, d.height);

		g.setColor(Color.WHITE);
		
		int x = 50, y = 50;
		g.setFont(Global.getFont(50));
		g.drawString("TRAINER " + charData.getName(), x, y);
		
		y += 100;
		
		g.drawString("$$$" + charData.getDatCashMoney()+" money in da bank.", x, y);
		
		y += 100;
		g.setFont(Global.getFont(40));
		g.drawString("Played " + formatTime(charData.getTimePlayed()), x, y);
	}
	
	private String formatTime(long l)
	{
		return (l/(3600) + " hours " + ((l%3600)/60) + " minutes");
	}

	public ViewMode getViewModel()
	{
		return ViewMode.TRAINER_CARD_VIEW;
	}

	public void movedToFront() {}
	
}
