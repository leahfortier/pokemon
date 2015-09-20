package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import javax.swing.JOptionPane;

import sound.SoundPlayer;

// Loads and maintains game data.
public class Global
{
	// Title of the window
	public static final String TITLE = "Pok\u00e9mon++";

	// Size of the game window
	public static final Dimension GAME_SIZE = new Dimension(800, 600);

	// Frame rate the game runs at
	public static final int FRAMERATE = 30;

	// The time(ms) between each frame.
	public static long MS_BETWEEN_FRAMES = 1000 / FRAMERATE;

	// The size of each tile in the map
	public static final int TILESIZE = 32;

	// The time(ms) it takes for the character to move from one tile on the map to another
	public static final int TIME_BETWEEN_TILES = 128;

	public static final String MONEY_SYMBOL = "\u00A5";
	
	public static final Color EXP_BAR_COLOR = new Color(51, 102, 204);

	public static <T> void swap(T[] arr)
	{
		T temp = arr[0];
		arr[0] = arr[1];
		arr[1] = temp;
	}

	public static void error(String errorMessage)
	{
		JOptionPane.showMessageDialog(null, "Eggs aren't supposed to be green.", "ERROR", JOptionPane.ERROR_MESSAGE);
		Thread.dumpStack();
		System.err.println(errorMessage);
		System.exit(1);
	}

	public static int getPercentageIndex(int[] chances)
	{
		int sum = 0, random = (int) (Math.random() * 100);
		for (int i = 0; i < chances.length; i++)
		{
			sum += chances[i];
			if (random < sum) return i;
		}
		
		Global.error("Chances array is improperly formatted.");
		return -1;
	}

	public static SoundPlayer soundPlayer = new SoundPlayer();
	
	public static Color getHPColor(double ratio)
	{
		if (ratio < 0.25) return Color.RED;
		else if (ratio < 0.5) return Color.YELLOW;
		return Color.GREEN;
	} 
	
	public static BufferedImage colorImage(BufferedImage image, float[] scale, float[] offset) 
	{
		ColorModel cm = image.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = image.copyData(null);
		image = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
		
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
}
