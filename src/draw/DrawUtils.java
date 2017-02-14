package draw;

import main.Global;
import map.Direction;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Map;

public final class DrawUtils {
	public static final Color EXP_BAR_COLOR = new Color(51, 102, 204);
	public static final int OUTLINE_SIZE = 2;

	public static Color getHPColor(double ratio) {
		if (ratio < 0.25) {
			return new Color(220, 20, 20);
		}
		else if (ratio < 0.5) {
			return new Color(255, 227, 85);
		}
		else {
			return new Color(35, 238, 91);
		}
	}

	public static void fillCanvas(Graphics g, Color color) {
		g.setColor(color);
		g.fillRect(0, 0, Global.GAME_SIZE.width, Global.GAME_SIZE.height);
	}

	public static void blackOutline(Graphics g, int x, int y, int width, int height) {
		blackOutline(g, x, y, width, height, Direction.values());
	}

	public static void blackOutline(Graphics g, int x, int y, int width, int height, Direction... directions) {
		drawBorder(g, Color.BLACK, x, y, width, height, OUTLINE_SIZE, directions);
	}

	// Doesn't draw over corners so works for transparent backgrounds
	public static void drawBorder(Graphics g, Color color, int x, int y, int width, int height, int borderSize) {
		g.setColor(color);

		g.fillRect(x, y, width - borderSize, borderSize);
		g.fillRect(x, y + borderSize, borderSize, height - borderSize);
		g.fillRect(x + borderSize, y + height - borderSize, width - borderSize, borderSize);
		g.fillRect(x + width - borderSize, y, borderSize, height - borderSize);
	}

	public static void drawBorder(Graphics g, Color color, int x, int y, int width, int height, int borderSize, Direction[] directions) {
		g.setColor(color);

		for (Direction direction : directions) {
			switch (direction) {
				case UP:
					g.fillRect(x, y, width, borderSize);
					break;
				case LEFT:
					g.fillRect(x, y, borderSize, height);
					break;
				case DOWN:
					g.fillRect(x, y + height - borderSize, width, borderSize);
					break;
				case RIGHT:
					g.fillRect(x + width - borderSize, y, borderSize, height);
					break;
			}
		}
	}

	public static void greyOut(Graphics g, int x, int y, int width, int height) {
		g.setColor(new Color(0, 0, 0, 128));
		g.fillRect(x, y, width, height);
	}

	public static void fillTransparent(Graphics g, int x, int y, int width, int height) {
		g.setColor(new Color(255, 255, 255, 128));
		g.fillRect(x, y, width, height);
	}

	public static Color permuteColor(Color color, Map<Integer,String> indexMap) {
		int dr = color.getRed() < 128 ? 1 : -1;
		int dg = color.getGreen() < 128 ? 1 : -1;
		int db = color.getBlue() < 128 ? 1 : -1;

		while (indexMap.containsKey(color.getRGB())) {
			int r = color.getRed() + dr;
			int g = color.getGreen() + dg;
			int b = color.getBlue() + db;

			color = new Color(r, g, b);
		}

		return color;
	}

}
