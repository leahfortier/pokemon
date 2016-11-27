package gui.view;

import main.Game;
import main.Global;
import trainer.CharacterData;
import util.DrawUtils;
import input.InputControl;
import input.ControlKey;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

public class TrainerCardView extends View {

	public void update(int dt, InputControl input) {
		if (input.consumeIfDown(ControlKey.ESC)) {
			Game.setViewMode(ViewMode.MAP_VIEW);
		}
	}

	public void draw(Graphics g) {
		CharacterData player = Game.getPlayer();
		Dimension dimension = Global.GAME_SIZE;

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, dimension.width, dimension.height);

		g.setColor(Color.WHITE);
		
		int x = 50, y = 50;
		DrawUtils.setFont(g, 50);
		g.drawString("TRAINER " + player.getName(), x, y);
		
		y += 100;
		
		g.drawString("$$$" + player.getDatCashMoney() + " money in da bank.", x, y);
		
		y += 100;
		DrawUtils.setFont(g, 40);
		g.drawString("Played " + formatTime(player.getTimePlayed()), x, y);
	}
	
	private String formatTime(long l) {
		return (l/(3600) + " hours " + ((l%3600)/60) + " minutes");
	}

	public ViewMode getViewModel() {
		return ViewMode.TRAINER_CARD_VIEW;
	}

	public void movedToFront() {}
}
