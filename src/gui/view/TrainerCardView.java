package gui.view;

import input.ControlKey;
import input.InputControl;
import main.Game;
import trainer.CharacterData;
import util.DrawUtils;

import java.awt.Color;
import java.awt.Graphics;

class TrainerCardView extends View {

	@Override
	public void update(int dt) {
		if (InputControl.instance().consumeIfDown(ControlKey.ESC)) {
			Game.setViewMode(ViewMode.MAP_VIEW);
		}
	}

	@Override
	public void draw(Graphics g) {
		CharacterData player = Game.getPlayer();

		DrawUtils.fillCanvas(g, Color.BLACK);

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

	@Override
	public ViewMode getViewModel() {
		return ViewMode.TRAINER_CARD_VIEW;
	}

	@Override
	public void movedToFront() {}
}
