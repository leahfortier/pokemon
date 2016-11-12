package util;

import gui.Button;
import main.Global;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class DrawUtils {
	public static final Color EXP_BAR_COLOR = new Color(51, 102, 204);

	// Dimension of a single tile
	private static final Dimension SINGLE_TILE_DIMENSION = new Dimension(1, 1);

	// For wrapped text, the amount in between each letter
	private static final float VERTICAL_WRAP_FACTOR = 2f;
	
	// The font the game interface uses
	private static Map<Integer, Font> fontMap;
	private static Map<Integer, Metrics> fontMetricsMap;
	
	public static void loadFontMetricsMap() {
		if (fontMetricsMap != null) {
			return;
		}
		
		fontMetricsMap = new HashMap<>();
		
		Scanner in = FileIO.openFile(FileName.FONT_METRICS);
		while (in.hasNext()) {
			int fontSize = in.nextInt();
			int horizontal = in.nextInt();
			int height = in.nextInt();
			
			Metrics fontMetrics = new Metrics(fontSize, horizontal, height);
			fontMetricsMap.put(fontSize, fontMetrics);
		}
	}
	
	public static void setFont(Graphics g, int fontSize) {
		g.setFont(getFont(fontSize));
	}
	
	public static Font getFont(int size) {
		if (fontMap == null) {
			fontMap = new HashMap<>();
		}
			
		if (!fontMap.containsKey(size)) {
			fontMap.put(size, new Font("Consolas", Font.BOLD, size));
		}
		
		return fontMap.get(size);
	}
	
	private static Metrics getFontMetrics(int fontSize) {
		if (fontMetricsMap == null) {
			loadFontMetricsMap();
		}
		
		if (!fontMetricsMap.containsKey(fontSize)) {
			Global.error("No metrics for the font size " + fontSize);
		}
		
		return fontMetricsMap.get(fontSize);
	}

	public static int getSuggestedWidth(String text, int fontSize) {
		Metrics fontMetrics = getFontMetrics(fontSize);
		return (text.length() + 2)*fontMetrics.horizontalSpacing; 
	}
	
	public static int getSuggestedHeight(int fontSize) {
		Metrics fontMetrics = getFontMetrics(fontSize);
		return (int)(fontMetrics.letterHeight*VERTICAL_WRAP_FACTOR*1.5);
	}
	
	public static Color getHPColor(double ratio) {
		if (ratio < 0.25) {
			return Color.RED;
		}
		else if (ratio < 0.5) {
			return Color.YELLOW;
		}
		else {
			return Color.GREEN;
		}
	}
	
	public static void drawCenteredImage(Graphics g, BufferedImage image, int x, int y) {
		g.drawImage(image, x - image.getWidth()/2, y - image.getHeight()/2, null);
	}

	public static BufferedImage colorImage(BufferedImage image, float[] scale, float[] offset) {
		ColorModel colorModel = image.getColorModel();
		boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
		WritableRaster raster = image.copyData(null);
		
		image = new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
		
		int width = image.getWidth();
        int height = image.getHeight();
        
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                int[] pixels = raster.getPixel(x, y, (int[]) null);
                
                for (int currComponent = 0; currComponent < pixels.length; ++currComponent) {
                	pixels[currComponent] = Math.round(pixels[currComponent] * scale[currComponent] + offset[currComponent]);
                	pixels[currComponent] = Math.min(Math.max(pixels[currComponent], 0), 255);
                }

                if (pixels[3] == 0) {
                	pixels[0] = pixels[1] = pixels[2] = 0;
                }

                raster.setPixel(x, y, pixels);
            }
        }
        return image;
    }

    public static int getTextHeight(Graphics g) {
		int fontSize = g.getFont().getSize();
		Metrics fontMetrics = getFontMetrics(fontSize);
		return fontMetrics.letterHeight;
	}

    public static int getDistanceBetweenRows(Graphics g) {
		int fontSize = g.getFont().getSize();
		Metrics fontMetrics = getFontMetrics(fontSize);
		return (int)(fontMetrics.letterHeight*VERTICAL_WRAP_FACTOR);
	}

	public static int drawWrappedText(Graphics g, String str, int x, int y, int width) {
		int fontSize = g.getFont().getSize();
		Metrics fontMetrics = getFontMetrics(fontSize);
		
		String[] words = str.split("[ ]+");
		StringBuilder build = new StringBuilder();
		
		int height = y;
		int distanceBetweenRows = getDistanceBetweenRows(g);

		for (String word : words) {
			if ((word.length() + build.length() + 1) * fontMetrics.horizontalSpacing > width) {
				g.drawString(build.toString(), x, height);

				height += distanceBetweenRows;
				build = new StringBuilder();
			}

			// TODO: Should there be something for this first part here?
			build.append(build.length() == 0 ? "" : " ")
					.append(word);
		}
		
		g.drawString(build.toString(), x, height);
		
		return height + distanceBetweenRows;
	}
	
	public static void drawCenteredString(Graphics g, String s, Button b) {
		drawCenteredString(g, s, b.x, b.y, b.width, b.height);
	}
	
	public static void drawCenteredString(Graphics g, String s, int x, int y, int width, int height) {
		int centerX = x + width/2;
		int centerY = y + height/2;
		
		drawCenteredString(g, s, centerX, centerY);
	}
	
	public static void drawCenteredString(Graphics g, String s, int centerX, int centerY) {
		int fontSize = g.getFont().getSize();
		Metrics fontMetrics = getFontMetrics(fontSize);
		
		int leftX = centerX(centerX, s, fontMetrics);
		int bottomY = centerY(centerY, fontMetrics);
		
		g.drawString(s, leftX, bottomY);
	}
	
	public static void drawCenteredWidthString(Graphics g, String s, int centerX, int y) {
		int fontSize = g.getFont().getSize();
		Metrics fontMetrics = getFontMetrics(fontSize);
		
		int leftX = centerX(centerX, s, fontMetrics);
		g.drawString(s, leftX, y);
	}
	
	private static int centerY(int centerY, Metrics fontMetrics) {
		return centerY + fontMetrics.letterHeight/2;
	}
	
	private static int centerX(int centerX, String s, Metrics fontMetrics) {
		return centerX - s.length()*fontMetrics.horizontalSpacing/2;
	}
	
	private static int rightX(int rightX, String s, Metrics fontMetrics) {
		return rightX - s.length()*fontMetrics.horizontalSpacing;
	}
	
	public static void drawCenteredHeightString(Graphics g, String s, int x, int centerY) {
		int fontSize = g.getFont().getSize();
		Metrics fontMetrics = getFontMetrics(fontSize);
		
		int bottomY = centerY(centerY, fontMetrics);
		g.drawString(s, x, bottomY);
	}

	public static void drawRightAlignedString(Graphics g, String s, int rightX, int y) {
		int fontSize = g.getFont().getSize();
		Metrics fontMetrics = getFontMetrics(fontSize);
		
		int leftX = rightX(rightX, s, fontMetrics);
		
		g.drawString(s, leftX, y);
	}
	
	// Draws a string with a shadow behind it the specified location
	public static void drawShadowText(Graphics g, String text, int x, int y, boolean rightAligned) {
		g.setColor(new Color(128, 128, 128, 128));
		
		if (rightAligned) {
			DrawUtils.drawRightAlignedString(g, text, x, y);
		}
		else {
			g.drawString(text, x, y);
		}
		
		g.setColor(Color.DARK_GRAY);
		
		if (rightAligned) {
			DrawUtils.drawRightAlignedString(g, text, x - 2, y - 2);
		}
		else {
			g.drawString(text, x - 2, y - 2);
		}
	}

	private static int getTextWidth(final String text, final int fontSize) {
		return text.length()*getFontMetrics(fontSize).getHorizontalSpacing();
	}

	public static Point getLocation(Point drawLocation, Point mapLocation) {
		return new Point(
				(drawLocation.x - mapLocation.x)/Global.TILE_SIZE,
				(drawLocation.y - mapLocation.y)/Global.TILE_SIZE
		);
	}

	public static Point getDrawLocation(Point location, Point mapLocation) {
		return new Point(
				location.x*Global.TILE_SIZE + mapLocation.x,
				location.y*Global.TILE_SIZE + mapLocation.y
		);
	}

	public static Point getImageDrawLocation(BufferedImage image, Point location, Point mapLocation) {
		Point drawLocation = getDrawLocation(location, mapLocation);
		drawLocation.x += Global.TILE_SIZE/2 - image.getWidth()/2;
		drawLocation.y += Global.TILE_SIZE/2 -  image.getHeight()/2;

		return drawLocation;
	}

	public static void drawTileImage(Graphics g, BufferedImage image, Point location, Point mapLocation) {
		Point drawLocation = getImageDrawLocation(image, location, mapLocation);

		g.drawImage(image, drawLocation.x, drawLocation.y, null);
	}

	public static void outlineTileRed(Graphics g, Point location, Point mapLocation) {
		outlineTile(g, location, mapLocation, Color.RED);
	}

	public static void outlineTile(Graphics g, Point location, Point mapLocation, Color color) {
		outlineTiles(g, location, mapLocation, color, SINGLE_TILE_DIMENSION);
	}

	public static void outlineTiles(Graphics g, Point location, Point mapLocation, Color color, Dimension rectangle) {
		Point drawLocation = getDrawLocation(location, mapLocation);

		g.setColor(color);
		g.drawRect(drawLocation.x, drawLocation.y, Global.TILE_SIZE*rectangle.width, Global.TILE_SIZE*rectangle.height);
	}

	public static void fillTile(Graphics g, Point location, Point mapLocation, Color color) {
		Point drawLocation = getDrawLocation(location, mapLocation);

		g.setColor(color);
		g.fillRect(drawLocation.x, drawLocation.y, Global.TILE_SIZE, Global.TILE_SIZE);
	}

	public static void fillBlankTile(Graphics g, Point drawLocation) {
		int halfTile = Global.TILE_SIZE/2;
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				g.setColor(i == j ? Color.GRAY : Color.LIGHT_GRAY);
				g.fillRect(drawLocation.x + i*halfTile, drawLocation.y + j*halfTile, halfTile, halfTile);
			}
		}
	}

	public static BufferedImage createBlankTile() {
		BufferedImage image = createNewTileImage();
		Graphics g = image.getGraphics();
		fillBlankTile(g, new Point());
		g.dispose();

		return image;
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

	public static void fillImage(BufferedImage image, Color color) {
		Graphics g = image.getGraphics();
		g.setColor(color);
		g.fillRect(0, 0, Global.TILE_SIZE, Global.TILE_SIZE);
		g.dispose();
	}

	public static BufferedImage createNewImage(Dimension dimension) {
		return new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_ARGB);
	}

	public static BufferedImage createNewTileImage() {
		return new BufferedImage(Global.TILE_SIZE, Global.TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
	}

	public static BufferedImage fillImage(Color color) {
		BufferedImage image = createNewTileImage();
		fillImage(image, color);

		return image;
	}

	public static BufferedImage blankImageWithText(String text) {
		int extra = DrawUtils.getTextWidth(text + " ", 14);

		BufferedImage image = new BufferedImage(Global.TILE_SIZE + extra, Global.TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		g.setColor(Color.BLACK);
		setFont(g, 14);

		// TODO: If we're starting the string 3 pixels after the image, then shouldn't we add three to the extra?
		// TODO: I think the y value here is a guess - can maybe use draw metrics to figure out where to start
		g.drawString(text, Global.TILE_SIZE + 3, Global.TILE_SIZE*2/3);

		g.dispose();

		return image;
	}

	public static BufferedImage colorWithText(String text, Color color) {
		BufferedImage image = blankImageWithText(text);
		fillImage(image, color);
		return image;
	}

	public static BufferedImage imageWithText(BufferedImage image, String text) {
		if (image == null) {
			Global.error("Image is null :(");
		}

		BufferedImage bufferedImage = blankImageWithText(text);
		Graphics g = bufferedImage.getGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return bufferedImage;
	}
	
	static class Metrics {
    	private final int fontSize;
    	private final int horizontalSpacing;
    	private final int letterHeight;
    	
    	Metrics(int fontSize, int horizontalSpacing, int letterHeight) {
    		this.fontSize = fontSize;
    		this.horizontalSpacing = horizontalSpacing;
    		this.letterHeight = letterHeight;
    	}

    	int getHorizontalSpacing() {
			return this.horizontalSpacing;
		}

    	public String toString() {
    		return fontSize + " " + horizontalSpacing + " " + letterHeight;
    	}
    }
}
