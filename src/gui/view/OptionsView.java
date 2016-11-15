package gui.view;

import main.Game;
import main.Global;
import util.DrawUtils;
import util.InputControl;
import util.InputControl.Control;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

public class OptionsView extends View {
	private boolean musicOn;
	private Color color;
	
	public OptionsView() {
		musicOn = Global.soundPlayer.isMuted();
		color = new Color(0, 0, 0);
	}

	public void update(int dt, InputControl input) {
		if (input.mouseDown) {
			input.consumeMousePress();
			
			musicOn = !musicOn;
			Global.soundPlayer.toggleMusic();
		}
		
		if (input.isDown(Control.ESC)) {
			input.consumeKey(Control.ESC);
			Game.setViewMode(ViewMode.MAP_VIEW);
		}
	}

	private void setNextColor() {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();

		r += 13;
		g += 34;
		b += 21;

		r %= 255;
		g %= 255;
		b %= 255;

		this.color = new Color(r, g, b);
	}

	public void draw(Graphics g) {
		Dimension d = Global.GAME_SIZE;
		
//		g.setColor(Color.BLACK);
		g.setColor(color);
		setNextColor();

		g.fillRect(0, 0, d.width, d.height);
		
		g.setColor(Color.WHITE);
		DrawUtils.setFont(g, 150);
		DrawUtils.drawCenteredWidthString(g, "VOLUME", d.width/2, d.height/4);
		
		if (musicOn) {
			g.setColor(Color.GREEN);
			g.fillRect(d.width/2, d.height/2 - 50, 200, 100);
			
			g.setColor(Color.DARK_GRAY);
			g.fillRect(d.width/2 - 200, d.height/2 - 50, 200, 100);
			
			g.setColor(Color.BLACK);
			DrawUtils.setFont(g, 100);
			g.drawString("OFF", d.width/2 - 180, d.height/2 + 32);
		}
		else {
			g.setColor(Color.DARK_GRAY);
			g.fillRect(d.width/2, d.height/2 - 50, 200, 100);
			
			g.setColor(Color.RED);
			g.fillRect(d.width/2 - 200, d.height/2 - 50, 200, 100);
			
			g.setColor(Color.BLACK);
			DrawUtils.setFont(g, 100);
			g.drawString("ON", d.width/2 + 40, d.height/2 + 32);
		}
		

	}

	public ViewMode getViewModel() {
		return ViewMode.OPTIONS_VIEW;
	}

	public void movedToFront() {}
}
