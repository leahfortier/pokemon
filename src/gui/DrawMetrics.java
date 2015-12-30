package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;

import util.FileIO;
import main.Global;

public class DrawMetrics
{
	public static final Color EXP_BAR_COLOR = new Color(51, 102, 204);
	
	private static final String FONT_METRICS_LOCATION = "fontMetrics.txt";
	
	// For wrapped text, the amount in between each letter
	private static final float VERTICAL_WRAP_FACTOR = 2f; 
	
	// The font the game interface uses
	private static HashMap<Integer, Font> fontMap;
	private static HashMap<Integer, Metrics> fontMetricsMap;
	
	public static void loadFontMetricsMap()
	{
		if (fontMetricsMap != null)
		{
			return;
		}
		
		fontMetricsMap = new HashMap<>();
		
		Scanner in = FileIO.openFile(FONT_METRICS_LOCATION);
		while (in.hasNext())
		{
			int fontSize = in.nextInt();
			int horizontal = in.nextInt();
			int height = in.nextInt();
			
			Metrics fontMetrics = new Metrics(fontSize, horizontal, height);
			fontMetricsMap.put(fontSize, fontMetrics);
		}
	}
	
	public static void setFont(Graphics g, int fontSize)
	{
		g.setFont(getFont(fontSize));
	}
	
	private static Font getFont(int size)
	{
		if (fontMap == null)
			fontMap = new HashMap<>();
			
		if (!fontMap.containsKey(size))
		{
			fontMap.put(size, new Font("Consolas", Font.BOLD, size));
		}
		
		return fontMap.get(size);
	}
	
	private static Metrics getFontMetrics(int fontSize)
	{
		if (fontMetricsMap == null)
		{
			loadFontMetricsMap();
		}
		
		if (!fontMetricsMap.containsKey(fontSize))
		{
			Global.error("No metrics for the font size " + fontSize);
		}
		
		return fontMetricsMap.get(fontSize);
	}

	public static int getSuggestedWidth(String text, int fontSize)
	{
		Metrics fontMetrics = getFontMetrics(fontSize);
		return (text.length() + 2)*fontMetrics.horizontalSpacing; 
	}
	
	public static int getSuggestedHeight(int fontSize)
	{
		Metrics fontMetrics = getFontMetrics(fontSize);
		return (int)(fontMetrics.letterHeight*VERTICAL_WRAP_FACTOR*1.5);
	}
	
	public static Color getHPColor(double ratio)
	{
		if (ratio < 0.25) return Color.RED;
		else if (ratio < 0.5) return Color.YELLOW;
		return Color.GREEN;
	}
	
	public static void drawCenteredImage(Graphics g, BufferedImage image, int x, int y)
	{
		g.drawImage(image, x - image.getWidth()/2, y - image.getHeight()/2, null);
	}
	
	public static BufferedImage colorImage(BufferedImage image, float[] scale, float[] offset) 
	{
		ColorModel colorModel = image.getColorModel();
		boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
		WritableRaster raster = image.copyData(null);
		
		image = new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
		
		int width = image.getWidth();
        int height = image.getHeight();
        
        for (int x = 0; x < width; ++x) 
        {
            for (int y = 0; y < height; ++y) 
            {
                int[] pixels = raster.getPixel(x, y, (int[]) null);
                
                for (int currComponent = 0; currComponent < pixels.length; ++currComponent)
                {
                	pixels[currComponent] = (int)Math.round(pixels[currComponent] * scale[currComponent] + offset[currComponent]);
                	pixels[currComponent] = Math.min(Math.max(pixels[currComponent], 0), 255);
                }
                if (pixels[3] == 0)
                {
                	pixels[0] = pixels[1] = pixels[2] = 0;
                }
                raster.setPixel(x, y, pixels);
            }
        }
        return image;
    }
	
	public static int drawWrappedText(Graphics g, String str, int x, int y, int width)
	{
		int fontSize = g.getFont().getSize();
		Metrics fontMetrics = getFontMetrics(fontSize);
		
		String[] words = str.split("[ ]+");
		StringBuilder build = new StringBuilder();
		
		int height = y;
		int distanceBetweenRows = (int)(fontMetrics.letterHeight*VERTICAL_WRAP_FACTOR);

		for (int i = 0; i < words.length; i++)
		{
			if ((words[i].length() + build.length() + 1)*fontMetrics.horizontalSpacing > width)
			{
				g.drawString(build.toString(), x, height);
				
				height += distanceBetweenRows;
				build = new StringBuilder();
			}

			build.append((build.length() == 0 ? "" : " ") + words[i]);
		}
		
		g.drawString(build.toString(), x, height);
		
		return height + distanceBetweenRows;
	}
	
	public static void drawCenteredString(Graphics g, String s, Button b)
	{
		drawCenteredString(g, s, b.x, b.y, b.width, b.height);
	}
	
	public static void drawCenteredString(Graphics g, String s, int x, int y, int width, int height)
	{
		int centerX = x + width/2;
		int centerY = y + height/2;
		
		drawCenteredString(g, s, centerX, centerY);
	}
	
	public static void drawCenteredString(Graphics g, String s, int centerX, int centerY)
	{
		int fontSize = g.getFont().getSize();
		Metrics fontMetrics = getFontMetrics(fontSize);
		
		int leftX = centerX(centerX, s, fontMetrics);
		int bottomY = centerY(centerY, fontMetrics);
		
		g.drawString(s, leftX, bottomY);
	}
	
	public static void drawCenteredWidthString(Graphics g, String s, int centerX, int y)
	{
		int fontSize = g.getFont().getSize();
		Metrics fontMetrics = getFontMetrics(fontSize);
		
		int leftX = centerX(centerX, s, fontMetrics);
		g.drawString(s, leftX, y);
	}
	
	private static int centerY(int centerY, Metrics fontMetrics)
	{
		return centerY + fontMetrics.letterHeight/2;
	}
	
	private static int centerX(int centerX, String s, Metrics fontMetrics)
	{
		return centerX - s.length()*fontMetrics.horizontalSpacing/2;
	}
	
	private static int rightX(int rightX, String s, Metrics fontMetrics)
	{
		return rightX - s.length()*fontMetrics.horizontalSpacing;
	}
	
	public static void drawCenteredHeightString(Graphics g, String s, int x, int centerY)
	{
		int fontSize = g.getFont().getSize();
		Metrics fontMetrics = getFontMetrics(fontSize);
		
		int bottomY = centerY(centerY, fontMetrics);
		g.drawString(s, x, bottomY);
	}

	public static void drawRightAlignedString(Graphics g, String s, int rightX, int y)
	{
		int fontSize = g.getFont().getSize();
		Metrics fontMetrics = getFontMetrics(fontSize);
		
		int leftX = rightX(rightX, s, fontMetrics);
		
		g.drawString(s, leftX, y);
	}
	
	// Draws a string with a shadow behind it the specified location
	public static void drawShadowText(Graphics g, String text, int x, int y, boolean rightAligned)
	{
		g.setColor(new Color(128, 128, 128, 128));
		
		if (rightAligned) 
		{
			DrawMetrics.drawRightAlignedString(g, text, x, y);
		}
		else 
		{
			g.drawString(text, x, y);
		}
		
		g.setColor(Color.DARK_GRAY);
		
		if (rightAligned) 
		{
			DrawMetrics.drawRightAlignedString(g, text, x - 2, y - 2);
		}
		else 
		{
			g.drawString(text, x - 2, y - 2);
		}
	}
	
	private static class Metrics
    {
    	private final int fontSize;
    	private final int horizontalSpacing;
    	private final int letterHeight;
    	
    	public Metrics(int fontSize, int horizontalSpacing, int letterHeight)
    	{
    		this.fontSize = fontSize;
    		this.horizontalSpacing = horizontalSpacing;
    		this.letterHeight = letterHeight;
    	}
    	
    	public String toString()
    	{
    		return fontSize + " " + horizontalSpacing + " " + letterHeight;
    	}
    }
	
	public static class FindMetrics extends JPanel
	{
		private static final long serialVersionUID = 1L;
		
		private static final String s = "WWWWWWWWWWWWWWWWWWWWWW";
		private static final int SMALLEST_FONT_SIZE = 1;
		private static final int LARGEST_FONT_SIZE = 150;
		
		private final int startX;
		private final int startY;
		
		private final BufferedImage canvas;
		private final Graphics2D g;
		private final int[][] colors;
		
	    private FindMetrics(int width, int height)
	    {
	        canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	        colors = new int[canvas.getWidth()][canvas.getHeight()];
	        
	        startX = 1;
	        startY = canvas.getHeight()/2;
	        
	        g = canvas.createGraphics();
	        
	        StringBuilder sb = new StringBuilder();
	        
	        for (int fontSize = SMALLEST_FONT_SIZE; fontSize <= LARGEST_FONT_SIZE; fontSize++)
	        {
	        	reset(fontSize);
	            
	            Metrics fontMetrics = getMetrics(fontSize);
	            checkMatch(fontSize, fontMetrics.horizontalSpacing);
	            
	            sb.append(fontMetrics.toString() + "\n");
	        }
	        
	        FileIO.writeToFile(FONT_METRICS_LOCATION, sb);
	    }
	    
	    private void reset(int fontSize)
	    {
	    	fillCanvas(Color.WHITE);
	    	
	    	drawString(fontSize, startX, startY);
	        setColors();
	    }
	    
	    private Metrics getMetrics(int fontSize)
	    {
	    	int leftMost = canvas.getWidth() + 1;
	    	int rightMost = -1;
	    	int highest = canvas.getHeight() + 1;
	    	int lowest = -1;
	    	
	    	for (int x = 0; x < canvas.getWidth(); x++)
	    	{
	    		for (int y = 0; y < canvas.getHeight(); y++)
	    		{
	    			if (canvas.getRGB(x, y) == Color.BLACK.getRGB())
	    			{
	    				leftMost = Math.min(leftMost, x);
	    				rightMost = Math.max(rightMost, x);
	    				highest = Math.min(highest, y);
	    				lowest = Math.max(lowest, y);
	    			}
	    		}
	    	}
	    	
	    	double horizontalSpacing = (double)rightMost/s.length();
	    	int horizontalGuess = (int)(horizontalSpacing + .5);
	    	
	    	int letterHeight = lowest - highest;
	    	
	    	return new Metrics(fontSize, horizontalGuess, letterHeight);
	    }
	    
	    private void setColors()
	    {
	    	for (int x = 0; x < canvas.getWidth(); x++)
	    	{
	    		for (int y = 0; y < canvas.getHeight(); y++)
	    		{
	    			colors[x][y] = canvas.getRGB(x, y);
	    		}
	    	}
	    }
	    
	    private void checkMatch(int fontSize, int spacing)
	    {
	    	char[] charArray = s.toCharArray();
	    	for (int i = 0; i < charArray.length; i++)
	    	{
	    		g.drawString(charArray[i] + "", startX + spacing*i, startY);
	    	}
	    	
	    	boolean match = true;
	    	for (int x = 0; x < canvas.getWidth(); x++)
	    	{
	    		for (int y = 0; y < canvas.getHeight(); y++)
	    		{
	    			if (colors[x][y] != canvas.getRGB(x, y))
	    			{
	    				match = false;
	    				break;
	    			}
	    		}
	    	}
	    	
	    	if (!match)
	    	{
	    		Global.error("Could not find font metrics for font size " + fontSize + ".");
	    	}
	    }
	    
	    private void drawString(int fontSize, int x, int y)
	    {
	    	DrawMetrics.setFont(g, fontSize);
	    	g.setColor(Color.BLACK);
	    	
	    	g.drawString(s, x, y);
	    }

	    public Dimension getPreferredSize() 
	    {
	        return new Dimension(canvas.getWidth(), canvas.getHeight());
	    }

	    public void paintComponent(Graphics g) 
	    {
	        super.paintComponent(g);
	        
	        Graphics2D g2 = (Graphics2D) g;
	        g2.drawImage(canvas, null, null);
	    }

	    private void fillCanvas(Color c) 
	    {
	        int color = c.getRGB();
	        
	        for (int x = 0; x < canvas.getWidth(); x++) 
	        {
	            for (int y = 0; y < canvas.getHeight(); y++) 
	            {
	                canvas.setRGB(x, y, color);
	            }
	        }
	        
	        super.repaint();
	    }

	    public static void writeFontMetrics() {
	        
	    	int width = 1850;
	        int height = 480;
	        
	        JFrame frame = new JFrame("Direct draw demo");

	        FindMetrics panel = new FindMetrics(width, height);

	        frame.add(panel);
	        frame.pack();
	        
//	        frame.setVisible(true);
	        frame.setResizable(false);
	        
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    }	
	}
}
