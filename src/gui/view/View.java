package gui.view;
import gui.GameData;

import java.awt.Graphics;

import main.Game;
import main.InputControl;

public abstract class View {
	public abstract void update(int dt, InputControl input, Game game);
	public abstract void draw(Graphics g, GameData data);
	public abstract Game.ViewMode getViewModel();
	public abstract void movedToFront();
}
