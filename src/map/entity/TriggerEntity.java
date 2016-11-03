package map.entity;

import gui.TileSet;
import gui.view.MapView;
import main.Game;
import map.Direction;
import map.MapData;
import util.InputControl;

import java.awt.image.BufferedImage;

public class TriggerEntity extends Entity {
	private final String trigger;

	public TriggerEntity(int x, int y, String trigger) {
		super(x, y);
		this.trigger = trigger;

	}

	public void update(int dt, Entity[][] entity, MapData map, InputControl input, MapView view) {}

	protected BufferedImage getFrame() {
		return Game.getData().getTrainerTiles().getTile(TileSet.EMPTY_IMAGE);
	}

	public String getTrigger() {
		return trigger;
	}

	public void getAttention(Direction direction) {}
	
	public void addData() {}
	public void reset() {}
}
