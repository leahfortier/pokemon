package gui.view;

import gui.button.Button;

import java.awt.Graphics;

public abstract class View {

	public abstract void update(int dt);
	public abstract void draw(Graphics g);
	public abstract ViewMode getViewModel();
	public abstract void movedToFront();
	
	private static final int[] rightArrowX = { 0, 16, 16, 32, 16, 16, 0 };
	private static final int[] rightArrowY = { 5, 5, 0, 10, 20, 15, 15 };
	private static final int[] leftArrowX = { 35, 19, 19, 3, 19, 19, 35 };
	private static final int[] leftArrowY = { 5, 5, 0, 10, 20, 15, 15 };

	public static void drawArrows(Graphics g, Button leftButton, Button rightButton) {
		if (leftButton != null) {
			int x = leftButton.x;
			int y = leftButton.y;
			
			g.translate(x, y);
			g.fillPolygon(leftArrowX, leftArrowY, leftArrowX.length);
			g.translate(-x, -y);
		}
		
		if (rightButton != null) {
			int x = rightButton.x;
			int y = rightButton.y;
			
			g.translate(x, y);
			g.fillPolygon(rightArrowX, rightArrowY, rightArrowX.length);
			g.translate(-x, -y);
		}
	}
}
