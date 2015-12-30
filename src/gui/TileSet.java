package gui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Scanner;

import util.FileIO;

public class TileSet
{
	public static final int EMPTY_IMAGE = -1;
	private static final String IMAGE_NOT_FOUND_LOCATION = FileIO.makePath("rec") + "imageNotFound.png";
	private static BufferedImage IMAGE_NOT_FOUND = null;
	
	public String name;
	private HashMap<Integer, BufferedImage> map;
	private HashMap<Integer, String> indexMap;
	private float scale;
	private String folderPath;

	public TileSet(String name, float scale)
	{
		this.name = name;
		this.scale = scale;
		
		this.map = new HashMap<>();
		this.indexMap = new HashMap<>();
		
		this.folderPath = FileIO.makePath("rec", "tiles", this.name);
		
		File indexFile = new File(this.folderPath + "index.txt");
		Scanner in = FileIO.openFile(indexFile);
		
		while (in.hasNext())
		{
			String fileName = in.next();
			int mapping = (int) Long.parseLong(in.next(), 16);
			indexMap.put(mapping, fileName);
		}
		
		in.close();
	}

	private BufferedImage scaleImage(BufferedImage img, float s)
	{
		if (s == 1.0f) 
		{
			return img;	
		}
		
		Image tmp = img.getScaledInstance((int) (img.getWidth()*s), (int) (img.getHeight()*s), /* BufferedImage.SCALE_FAST*/ BufferedImage.SCALE_SMOOTH);
		BufferedImage buffer = new BufferedImage((int) (img.getWidth() * s), (int) (img.getHeight() * s), BufferedImage.TYPE_INT_ARGB);
		
		buffer.getGraphics().drawImage(tmp, 0, 0, null);
		
		return buffer;
	}

	private void loadImage(int val)
	{
		String fileName = indexMap.get(val);
		// System.out.println(fileName + " -> " + val);
		
		BufferedImage image = FileIO.readImage(this.folderPath + fileName);
		
		image = scaleImage(image, scale);
		map.put(val, image);
	}

	public BufferedImage getTile(int val)
	{
		if (indexMap.containsKey(val))
		{
			loadImage(val);
			indexMap.remove(val); // so it doesn't try to reload it
		}
		
		if (map.containsKey(val)) 
			return map.get(val);
		
		return imageNotFound();
	}

	private static BufferedImage imageNotFound()
	{
		if (IMAGE_NOT_FOUND == null) 
		{
			IMAGE_NOT_FOUND = FileIO.readImage(IMAGE_NOT_FOUND_LOCATION);
		}
		
		return IMAGE_NOT_FOUND;
	}
}
