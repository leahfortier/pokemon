package util;

import main.Global;
import main.Type;
import map.Direction;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Map;

public class DrawUtils {
	public static final Color EXP_BAR_COLOR = new Color(51, 102, 204);
	public static final int OUTLINE_SIZE = 2;

	// Dimension of a single tile
	private static final Dimension SINGLE_TILE_DIMENSION = new Dimension(1, 1);

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

	public static void drawCenteredArrow(Graphics g, int centerX, int centerY, int width, int height, Direction direction) {
		drawArrow(g, centerX - width/2, centerY - height/2, width, height, direction);
	}

	public static void drawArrow(Graphics g, int x, int y, int width, int height, Direction direction) {
		int yMax = height;
		int xMax = width;

		boolean yAxis = direction.getDeltaPoint().x == 0;
		if (yAxis) {
			yMax = width;
			xMax = height;
		}

		int arrowLineTop = yMax/4;
		int arrowLineBottom = yMax - arrowLineTop;

		int arrowMidpoint = xMax/2;

		g.translate(x, y);

		int[] xValues = new int[] { 0, arrowMidpoint, arrowMidpoint, xMax, xMax, arrowMidpoint, arrowMidpoint };
		int[] yValues = new int[] { yMax/2, 0, arrowLineTop, arrowLineTop, arrowLineBottom, arrowLineBottom, yMax};

		if (yAxis) {
			GeneralUtils.swapArrays(xValues, yValues);
		}

		if (direction == Direction.RIGHT) {
			for (int i = 0; i < xValues.length; i++) {
				xValues[i] = xMax - xValues[i];
			}
		} else if (direction == Direction.DOWN) {
			for (int i = 0; i < yValues.length; i++) {
				yValues[i] = yMax - yValues[i];
			}
		}

		g.setColor(Color.BLACK);
		g.fillPolygon(xValues, yValues, xValues.length);

		g.translate(-x, -y);
	}

	public static void drawTypeTiles(Graphics g, Type[] type, int rightX, int textY) {
		BufferedImage firstType = type[0].getImage();

		int drawX = rightX - firstType.getWidth();
		int drawY = textY - firstType.getHeight();

		if (type[1] == Type.NO_TYPE) {
			g.drawImage(firstType, drawX, drawY, null);
		}
		else {
			BufferedImage secondType = type[1].getImage();
			int leftDrawX = drawX - firstType.getWidth() - 8;

			g.drawImage(firstType, leftDrawX, drawY, null);
			g.drawImage(secondType, drawX, drawY, null);
		}
	}

	public static void drawBottomCenteredImage(Graphics g, BufferedImage image, Point center) {
		g.drawImage(
				image,
				center.x - image.getWidth()/2,
				center.y - image.getHeight(),
				null);
	}
	
	public static void drawCenteredImage(Graphics g, BufferedImage image, int x, int y) {
		g.drawImage(
				image,
				x - image.getWidth()/2,
				y - image.getHeight()/2,
				null);
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

    public static int transformAnimation(
    		Graphics g,
			int animationValue,
			int animationLifespan,
			BufferedImage first,
			BufferedImage second,
			Point drawLocation) {

		float[] firstScales = { 1f, 1f, 1f, 1f };
		float[] firstOffsets = { 255f, 255f, 255f, 0f };
		float[] secondScales = { 1f, 1f, 1f, 1f };
		float[] secondOffsets = { 255f, 255f, 255f, 0f };

		// Turn white
		if (animationValue > animationLifespan*0.7) {
			firstOffsets[0] = firstOffsets[1] = firstOffsets[2] = 255*(1 - (animationValue - animationLifespan*0.7f)/(animationLifespan*(1 - 0.7f)));
			secondScales[3] = 0;
		}
		// Change form
		else if (animationValue > animationLifespan*0.3) {
			firstOffsets[0] = firstOffsets[1] = firstOffsets[2] = 255;
			firstScales[3] = ((animationValue - animationLifespan*0.3f)/(animationLifespan*(0.7f - 0.3f)));
			secondOffsets[0] = secondOffsets[1] = secondOffsets[2] = 255;
			secondScales[3] = (1 - (animationValue - animationLifespan*0.3f)/(animationLifespan*(0.7f - 0.3f)));
		}
		// Restore color
		else {
			firstScales[3] = 0;
			secondOffsets[0] = secondOffsets[1] = secondOffsets[2] = 255*(animationValue)/(animationLifespan*(1-0.7f));
		}

		animationValue -= Global.MS_BETWEEN_FRAMES;

		DrawUtils.drawBottomCenteredImage(g, DrawUtils.colorImage(first, secondScales, secondOffsets), drawLocation);
		DrawUtils.drawBottomCenteredImage(g, DrawUtils.colorImage(second, firstScales, firstOffsets), drawLocation);

		return animationValue;
	}
	
	public static void drawCenteredString(Graphics g, String s, int x, int y, int width, int height) {
		int centerX = x + width/2;
		int centerY = y + height/2;
		
		drawCenteredString(g, s, centerX, centerY);
	}
	
	public static void drawCenteredString(Graphics g, String s, int centerX, int centerY) {
		int fontSize = g.getFont().getSize();
		FontMetrics fontMetrics = FontMetrics.getFontMetrics(fontSize);
		
		int leftX = centerX(centerX, s, fontMetrics);
		int bottomY = centerY(centerY, fontMetrics);
		
		g.drawString(s, leftX, bottomY);
	}
	
	public static void drawCenteredWidthString(Graphics g, String s, int centerX, int y) {
		int fontSize = g.getFont().getSize();
		FontMetrics fontMetrics = FontMetrics.getFontMetrics(fontSize);
		
		int leftX = centerX(centerX, s, fontMetrics);
		g.drawString(s, leftX, y);
	}
	
	private static int centerY(int centerY, FontMetrics fontMetrics) {
		return centerY + fontMetrics.getLetterHeight()/2;
	}
	
	private static int centerX(int centerX, String s, FontMetrics fontMetrics) {
		return centerX - fontMetrics.getLength(s)/2;
	}
	
	private static int rightX(int rightX, String s, FontMetrics fontMetrics) {
		return rightX - fontMetrics.getLength(s);
	}
	
	public static void drawCenteredHeightString(Graphics g, String s, int x, int centerY) {
		int fontSize = g.getFont().getSize();
		FontMetrics fontMetrics = FontMetrics.getFontMetrics(fontSize);
		
		int bottomY = centerY(centerY, fontMetrics);
		g.drawString(s, x, bottomY);
	}

	public static void drawRightAlignedString(Graphics g, String s, int rightX, int y) {
		int fontSize = g.getFont().getSize();
		FontMetrics fontMetrics = FontMetrics.getFontMetrics(fontSize);
		
		int leftX = rightX(rightX, s, fontMetrics);
		
		g.drawString(s, leftX, y);
	}

	// Draws a string with a shadow behind it the specified location
	public static void drawShadowText(Graphics g, String text, int x, int y, Alignment alignment) {
		g.setColor(new Color(128, 128, 128, 128));
		alignment.drawString(g, text, x + 2, y + 2);

		g.setColor(Color.BLACK);
		alignment.drawString(g, text, x, y);
	}

	private static int getTextWidth(final String text, final int fontSize) {
		return text.length()*FontMetrics.getFontMetrics(fontSize).getHorizontalSpacing();
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

	public static void drawBorder(Graphics g, Color color, int x, int y, int width, int height, int borderSize) {
		drawBorder(g, color, x, y, width, height, borderSize, Direction.values());
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

	public static Point getLocation(Point drawLocation, Point mapLocation) {
		return new Point(
				(drawLocation.x - mapLocation.x)/Global.TILE_SIZE,
				(drawLocation.y - mapLocation.y)/Global.TILE_SIZE
		);
	}

	public static Point getDrawLocation(int x, int y, Point mapLocation) {
		return new Point(
				x*Global.TILE_SIZE + mapLocation.x,
				y*Global.TILE_SIZE + mapLocation.y
		);
	}

	// Takes in the draw coordinates and returns the location of the entity where to draw it relative to the canvas
	public static Point getDrawLocation(Point location, Point mapLocation) {
		return getDrawLocation(location.x, location.y, mapLocation);
	}

	public static void drawEntityTileImage(Graphics g, BufferedImage image, Point drawLocation) {
		Point imageDrawLocation = Point.add(
				drawLocation,
				Global.TILE_SIZE/2 - image.getWidth()/2,
				Global.TILE_SIZE/2 - image.getHeight()
		);

		g.drawImage(image, imageDrawLocation.x, imageDrawLocation.y, null);
	}

	public static void drawTileImage(Graphics g, BufferedImage image, Point drawLocation) {
		Point imageDrawLocation = Point.add(
				drawLocation,
				Global.TILE_SIZE - image.getWidth(),
				Global.TILE_SIZE - image.getHeight()
		);

		g.drawImage(image, imageDrawLocation.x, imageDrawLocation.y, null);
	}

	public static void drawTileImage(Graphics g, BufferedImage image, int x, int y, Point mapLocation) {
		drawTileImage(g, image, getDrawLocation(x, y, mapLocation));
	}

	public static void drawTileImage(Graphics g, BufferedImage image, Point location, Point mapLocation) {
		drawTileImage(g, image, location.x, location.y, mapLocation);
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
		FontMetrics.setFont(g, 14);

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

	public static int drawWrappedText(Graphics g, String str, int x, int y, int width) {
        int fontSize = g.getFont().getSize();
        FontMetrics fontMetrics = FontMetrics.getFontMetrics(fontSize);

        String[] words = str.split("[ ]+");
        StringBuilder build = new StringBuilder();

        int height = y;
        int distanceBetweenRows = FontMetrics.getDistanceBetweenRows(g);

        for (String word : words) {
            if ((word.length() + build.length() + 1) * fontMetrics.getHorizontalSpacing() > width) {
                g.drawString(build.toString(), x, height);

                height += distanceBetweenRows;
                build = new StringBuilder();
            }

            // TODO: StringUtil method
            build.append(build.length() == 0 ? "" : " ")
                    .append(word);
        }

        g.drawString(build.toString(), x, height);

        return height + distanceBetweenRows;
    }
}
