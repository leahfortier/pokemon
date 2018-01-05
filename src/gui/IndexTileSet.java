package gui;

import util.FileIO;
import util.FileName;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class IndexTileSet extends TileSet {
    private final Map<Integer, String> indexMap;
    
    public IndexTileSet(String folderPath) {
        super(folderPath);
        
        this.indexMap = new HashMap<>();
        String indexFileName = FileName.getIndexFileName(this.folderPath);
        Scanner in = FileIO.openFile(indexFileName);
        
        while (in.hasNext()) {
            String fileName = in.next();
            int mapping = (int)Long.parseLong(in.next(), 16);
            indexMap.put(mapping, fileName);
            
            // Ignore the rest of the line -- only interested in the index value
            if (in.hasNext()) {
                in.nextLine();
            }
        }
        
        in.close();
    }
    
    public BufferedImage getTile(int index) {
        if (!indexMap.containsKey(index)) {
            return TileSet.IMAGE_NOT_FOUND;
        }
        
        return super.getTile(indexMap.get(index));
    }
}
