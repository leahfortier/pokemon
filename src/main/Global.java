package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JOptionPane;

import javazoom.jl.player.Player;
import pokemon.Ability;
import pokemon.PokemonInfo;
import sound.SoundPlayer;
import battle.Attack;
import battle.effect.PokemonEffect;

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

	// The font the game interface uses
	// private static final String FONT_PATH = "resources/DejaVuSansMono.ttf";
	private static HashMap<Integer, Font> font;

	// The size of each tile in the map
	public static final int TILESIZE = 32;

	// The time(ms) it takes for the character to move from one tile on the map
	// to another
	public static final int TIME_BETWEEN_TILES = 128;

	public static final String FILE_SLASH = File.separator;

	public static final String MONEY_SYMBOL = "\u00A5";
	
	public static final Color EXP_BAR_COLOR = new Color(51, 102, 204);
	
	// Load all game data that doesn't need to be initialized in the OpenGL
	// context
	public static void init()
	{
		Attack.loadMoves();
		Ability.loadAbilities();
		PokemonEffect.loadEffects();
		PokemonInfo.loadPokemonInfo();
	}

	/**
	 * Reads the whole file ignoring commented lines starting with #
	 * 
	 * @param file
	 * @param ignoreComments
	 * @return
	 */
	public static String readEntireFile(File file, boolean ignoreComments)
	{
		BufferedReader in = openFileBuffered(file);
		StringBuilder build = new StringBuilder();
		String line = null;
		try
		{
			while ((line = in.readLine()) != null)
				if (line.length() > 0 && (line.charAt(0) != '#' || ignoreComments))
				{
					build.append(line);
					build.append("\n");
				}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		return build.toString().replaceAll("\\\\u00e9", "\u00e9").replaceAll("\\\\u2640", "\u2640").replaceAll("\\\\u2642", "\u2642");
	}
	
	public static String readEntireFileWithoutReplacements (File file, boolean ignoreComments)
	{
		BufferedReader in = openFileBuffered(file);
		StringBuilder build = new StringBuilder();
		String line = null;
		try
		{
			while ((line = in.readLine()) != null)
				if (line.length() > 0 && (line.charAt(0) != '#' || ignoreComments))
				{
					build.append(line);
					build.append("\n");
				}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		return build.toString();
	}
	
	

	public static BufferedReader openFileBuffered(String file)
	{
		return openFileBuffered(new File(file));
	}

	public static BufferedReader openFileBuffered(File file)
	{
		try
		{
			return new BufferedReader(new FileReader(file));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	public static Scanner openFile(String file)
	{
		return openFile(new File(file));
	}

	public static Scanner openFile(File file)
	{
		try
		{
			Scanner in = new Scanner(new FileReader(file));
			return in;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

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

	public static Font getFont(int size)
	{
		if (font == null) font = new HashMap<>();
		if (!font.containsKey(size))
		{
			font.put(size, new Font("Consolas", Font.BOLD, size));
		}
		return font.get(size);
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

	public static int drawWrappedText(Graphics g, String str, int x, int y, int width)
	{
		return drawWrappedText(g, str, x, y, width, 17, 36);
	}

	public static int drawWrappedText(Graphics g, String str, int x, int y, int width, int widthFactor, int heightFactor)
	{
		String[] words = str.split("[ ]+");
		StringBuilder build = new StringBuilder();
		int h = y;

		for (int i = 0; i < words.length; i++)
		{
			if ((words[i].length() + build.length() + 1)*widthFactor > width)
			{
				g.drawString(build.toString(), x, h);
				h += heightFactor;
				build = new StringBuilder();
			}

			build.append((build.length() == 0 ? "" : " ") + words[i]);
		}
		g.drawString(build.toString(), x, h);
		return h + 36;
	}

	public static SoundPlayer soundPlayer = new SoundPlayer();
	
	public static int centerX(String s, int x, int fontSize)
	{
		return x - s.length() * (fontSize / 2 + 1) / 2;
	}
	
	public static void drawStringCenterX(String s, int x, int y, Graphics g)
	{
		TextLayout layout = new TextLayout(s, g.getFont(), g.getFontMetrics(g.getFont()).getFontRenderContext());
		float location = (float) layout.getBounds().getWidth()/2;  
        layout.draw(((Graphics2D)g), (float)x-location, (float)y);
	}

	public static int rightX(String s, int x, int fontSize)
	{
		return x - s.length() * (fontSize / 2 + 1);
	}
	
	public static void drawStringRightX(String s, int x, int y, Graphics g)
	{
		TextLayout layout = new TextLayout(s, g.getFont(), g.getFontMetrics(g.getFont()).getFontRenderContext());
		float location = (float) layout.getBounds().getWidth();  
        layout.draw(((Graphics2D)g), (float)x-location, (float)y);
	}
	
	public static Color getHPColor(double ratio)
	{
		if (ratio < 0.25) return Color.RED;
		else if (ratio < 0.5) return Color.YELLOW;
		return Color.GREEN;
	}
}
