package gui.view;

import gui.Button;

import java.awt.Graphics;

public abstract class View {

	public abstract void update(int dt);
	public abstract void draw(Graphics g);
	public abstract ViewMode getViewModel();
	public abstract void movedToFront();
	
	private static final int[] rightArrowx = { 0, 16, 16, 32, 16, 16, 0 };
	private static final int[] rightArrowy = { 5, 5, 0, 10, 20, 15, 15 };
	private static final int[] leftArrowx = { 35, 19, 19, 3, 19, 19, 35 };
	private static final int[] leftArrowy = { 5, 5, 0, 10, 20, 15, 15 };
	
	protected static void drawArrows(Graphics g, Button leftButton, Button rightButton) {
		View.drawArrows(g, leftButton, rightButton, 0, 0);
	}
	
	protected static void drawArrows(Graphics g, Button leftButton, Button rightButton, int xOffset, int yOffset) {
		if (leftButton != null) {
			int x = leftButton.x + xOffset;
			int y = leftButton.y + yOffset;
			
			g.translate(x, y);
			g.fillPolygon(leftArrowx, leftArrowy, leftArrowx.length);
			g.translate(-x, -y);
		}
		
		if (rightButton != null) {
			int x = rightButton.x + xOffset;
			int y = rightButton.y + yOffset;
			
			g.translate(x, y);
			g.fillPolygon(rightArrowx, rightArrowy, rightArrowx.length);
			g.translate(-x, -y);
		}
	}
}
