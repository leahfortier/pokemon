package gui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import javax.imageio.ImageIO;

import main.Global;

public class TileSet
{
	private static BufferedImage imageNotFound;
	private static String imageNotFoundPath = "rec" + Global.FILE_SLASH + "imageNotFound.png";
	public String name;
	private HashMap<Integer, BufferedImage> map;
	private HashMap<Integer, String> indexMap;
	private float scale;

	public TileSet(String name, float scale) throws IOException
	{
		this.name = name;
		this.scale = scale;
		map = new HashMap<>();
		indexMap = new HashMap<>();
		File indexFile = new File("rec" + Global.FILE_SLASH + "tiles" + Global.FILE_SLASH + name + Global.FILE_SLASH + "index.txt");
		if (!indexFile.exists())
		{
			System.err.println("Failed to find index file for tiles: " + name);
			return;
		}
		Scanner in = new Scanner(indexFile);
		while (in.hasNext())
		{
			String fileName = in.next();
			int mapping = (int) Long.parseLong(in.next(), 16);
			indexMap.put(mapping, fileName);
		}
		in.close();
	}

	private BufferedImage scale(BufferedImage img, float s)
	{
		if (s == 1.0f) return img;
		Image tmp = img.getScaledInstance((int) (img.getWidth() * s), (int) (img.getHeight() * s), /*
																									 * BufferedImage
																									 * .
																									 * SCALE_FAST
																									 */BufferedImage.SCALE_SMOOTH);
		BufferedImage buffer = new BufferedImage((int) (img.getWidth() * s), (int) (img.getHeight() * s), BufferedImage.TYPE_INT_ARGB);
		buffer.getGraphics().drawImage(tmp, 0, 0, null);
		return buffer;
	}

	private void load(int val)
	{
		String fileName = indexMap.get(val);
		// System.out.println(fileName +" -> " + val);
		File file = new File("rec" + Global.FILE_SLASH + "tiles" + Global.FILE_SLASH + name + Global.FILE_SLASH + fileName);
		if (!file.exists()) return;
		BufferedImage img;
		try
		{
			img = ImageIO.read(file);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return;
		}
		img = scale(img, scale);
		map.put(val, img);
	}

	public BufferedImage getTile(int val)
	{
		if (indexMap.containsKey(val))
		{
			load(val);
			indexMap.remove(val); // so it doesn't try to reload it
		}
		if (map.containsKey(val)) return map.get(val);
		return imageNotFound();
	}

	private static BufferedImage imageNotFound()
	{
		if (imageNotFound == null)
		{
			try
			{
				imageNotFound = ImageIO.read(new File(imageNotFoundPath));
			}
			catch (IOException e)
			{
				e.printStackTrace();
				System.exit(1);
			}
		}
		return imageNotFound;
	}
}
