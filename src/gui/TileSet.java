package gui;

import util.FileIO;
import util.Folder;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TileSet {
	public static final int INVALID_RGB = -1000;

	public static final BufferedImage POKEBALL = FileIO.readImage(Folder.IMAGES + "Pokeball.png");
	public static final BufferedImage TINY_POKEBALL = FileIO.readImage(Folder.IMAGES + "TinyPokeball.png");

	protected static final BufferedImage IMAGE_NOT_FOUND = FileIO.readImage(Folder.IMAGES + "imageNotFound.png");

	private Map<String, BufferedImage> map;
	private float scale;
	protected String folderPath;

	public TileSet(String folderPath) {
		this(folderPath, 1.0f);
	}

	public TileSet(String folderPath, float scale) {
		this.folderPath = folderPath;
		this.scale = scale;

		this.map = new HashMap<>();
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

	// TODO: I still have no idea what the >> 24 shit means
	public static boolean isValidMapTile(int val) {
		return val != INVALID_RGB && (val >> 24) != 0;
	}

	public BufferedImage getTile(String imageName) {
		if (!map.containsKey(imageName)) {
			File file = new File(this.folderPath + imageName + ".png");
			try {
				BufferedImage image = ImageIO.read(file);
				image = scaleImage(image, scale);
				map.put(imageName, image);
			}
			catch (IOException exception) {
				return IMAGE_NOT_FOUND;
			}
		}

		return map.get(imageName);
	}
}
