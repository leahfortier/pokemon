package gui;

import util.FileIO;
import util.FileName;
import util.Folder;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TileSet {
	public static final int EMPTY_IMAGE = -1;
	public static final int INVALID_RGB = -1000;

	public static final BufferedImage POKEBALL = FileIO.readImage(Folder.IMAGES + "Pokeball.png");
	public static final BufferedImage TINY_POKEBALL = FileIO.readImage(Folder.IMAGES + "TinyPokeball.png");

	private static final BufferedImage IMAGE_NOT_FOUND = FileIO.readImage(Folder.IMAGES + "imageNotFound.png");

	private Map<Integer, BufferedImage> map;
	private Map<Integer, String> indexMap;
	private float scale;
	private String folderPath;

	public TileSet(String folderPath) {
		this(folderPath, 1.0f);
	}

	public TileSet(String folderPath, float scale) {
		this.folderPath = folderPath;
		this.scale = scale;

		this.map = new HashMap<>();
		this.indexMap = new HashMap<>();

		String indexFileName = FileName.getIndexFileName(this.folderPath);
		Scanner in = FileIO.openFile(indexFileName);
		
		while (in.hasNext()) {
			String fileName = in.next();
			int mapping = (int) Long.parseLong(in.next(), 16);
			indexMap.put(mapping, fileName);
		}
		
		in.close();
	}

	private BufferedImage scaleImage(BufferedImage img, float scale) {
		if (scale == 1.0f) {
			return img;	
		}
		
		Image tmp = img.getScaledInstance((int) (img.getWidth()*scale), (int) (img.getHeight()*scale), BufferedImage.SCALE_SMOOTH);
		BufferedImage buffer = new BufferedImage((int) (img.getWidth()*scale), (int) (img.getHeight()*scale), BufferedImage.TYPE_INT_ARGB);
		
		buffer.getGraphics().drawImage(tmp, 0, 0, null);
		
		return buffer;
	}

	private void loadImage(int val) {
		String fileName = indexMap.get(val);
		BufferedImage image = FileIO.readImage(this.folderPath + fileName);
		
		image = scaleImage(image, scale);
		map.put(val, image);
	}

	// TODO: I still have no idea what the >> 24 shit means
	public static boolean isValidMapTile(int val) {
		return val != INVALID_RGB && (val >> 24) != 0;
	}

	public BufferedImage getTile(int val) {
		if (indexMap.containsKey(val)) {
			loadImage(val);
			indexMap.remove(val); // so it doesn't try to reload it
		}

		if (map.containsKey(val)) {
			return map.get(val);
		}

		return IMAGE_NOT_FOUND;
	}
}
