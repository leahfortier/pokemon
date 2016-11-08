package mapMaker.tools;

import map.entity.npc.NPCEntityData;
import mapMaker.MapMaker;
import mapMaker.MapMaker.EditType;
import pattern.AreaDataMatcher.NPCMatcher;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class SingleClickTool extends Tool {
    public SingleClickTool(final MapMaker mapMaker) {
        super(mapMaker);
    }

    public void click(int x, int y) {
        if (mapMaker.tileList.isSelectionEmpty()) {
            return;
        }

        x = (int) Math.floor((x - mapMaker.mapX) * 1.0 / MapMaker.tileSize);
        y = (int) Math.floor((y - mapMaker.mapY) * 1.0 / MapMaker.tileSize);

        System.out.println("click: " + x + " " + y);

        int val = Integer.parseInt(mapMaker.tileList.getSelectedValue().getDescription());
        mapMaker.setTile(x, y, val);

        if (mapMaker.editType == EditType.TRIGGERS) {
            mapMaker.triggerData.clearPlaceableTrigger();
            mapMaker.toolList.setSelectedIndex(3);
        }
    }

    public String toString() {
        return "Single";
    }

    public void draw(Graphics g) {
        int mhx = (int) Math.floor((mapMaker.mouseHoverX - mapMaker.mapX) * 1.0 / MapMaker.tileSize);
        int mhy = (int) Math.floor((mapMaker.mouseHoverY - mapMaker.mapY) * 1.0 / MapMaker.tileSize);

        g.setColor(Color.red);
        g.drawRect(mhx * MapMaker.tileSize + mapMaker.mapX, mhy * MapMaker.tileSize + mapMaker.mapY, MapMaker.tileSize, MapMaker.tileSize);

        if (mapMaker.tileList.isSelectionEmpty()) {
            return;
        }

        //Show preview image for normal map tiles
        if (mapMaker.editType == EditType.BACKGROUND || mapMaker.editType == EditType.FOREGROUND) {
            int val = Integer.parseInt(mapMaker.tileList.getSelectedValue().getDescription());
            if (!mapMaker.tileMap.containsKey(val)) {
                return;
            }

            BufferedImage img = mapMaker.tileMap.get(val);
            g.drawImage(mapMaker.tileMap.get(val), mhx * MapMaker.tileSize + mapMaker.mapX - img.getWidth() + MapMaker.tileSize, mhy * MapMaker.tileSize + mapMaker.mapY - img.getHeight() + MapMaker.tileSize, null);
        }
        // Show preview image for current trigger
        else if (mapMaker.editType == EditType.TRIGGERS) {
            BufferedImage img = null;

            switch (mapMaker.tileList.getSelectedValue().getDescription()) {
                case "0":
                    img = mapMaker.trainerTileMap.get(0);
                    break;
                case "1":
                    NPCEntityData npc = (NPCEntityData) mapMaker.triggerData.getPlaceableTrigger().entity;
                    img = mapMaker.trainerTileMap.get(12 * npc.spriteIndex + 1 + npc.defaultDirection.ordinal()); // TODO: This should call a function
                    break;
                case "2":
                    img = mapMaker.mapMakerTileMap.get(4);
                    break;
                case "3":
                    img = mapMaker.mapMakerTileMap.get(3);
                    break;
                case "4":
                    img = mapMaker.mapMakerTileMap.get(2);
                    break;
                case "5":
                    img = mapMaker.mapMakerTileMap.get(1);
                    break;
                case "6":
                    img = mapMaker.mapMakerTileMap.get(5);
                    break;
                case "8":
                    img = mapMaker.mapMakerTileMap.get(0xc);
                    break;
            }

            if (img != null) {
                // TODO: I think there's a method in draw metrics that does something like this
                g.drawImage(img, mhx * MapMaker.tileSize + mapMaker.mapX - img.getWidth() / 2 + MapMaker.tileSize / 2,
                        mhy * MapMaker.tileSize + mapMaker.mapY - img.getHeight() + MapMaker.tileSize,
                        null);
            }
        }
        //Show preview color for other edit modes
//			else if (editType == EditType.AREA_MAP || editType == EditType.MOVE_MAP) {
//				int val = Integer.parseInt(tileList.getSelectedValue().getDescription());
//				BufferedImage img = filledImage(new Color(val, true));
//				g.drawImage(img, mhx*tileSize + mapX - img.getWidth() + tileSize, mhy*tileSize + mapY - img.getHeight() + tileSize, null);
//			}

    }
}
