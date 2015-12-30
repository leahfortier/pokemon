package map.entity;

import gui.GameData;
import gui.TileSet;
import gui.view.MapView;

import java.awt.image.BufferedImage;

import util.InputControl;
import map.MapData;
import map.entity.MovableEntity.Direction;

public class TriggerEntity extends Entity
{
	private final String trigger;

	public TriggerEntity(int x, int y, String trigger) 
	{
		super(x, y);
		this.trigger = trigger;

	}

	public void update(int dt, Entity[][] entity, MapData map, InputControl input, MapView view) {}

	protected BufferedImage getFrame(GameData data) 
	{
		return data.getTrainerTiles().getTile(TileSet.EMPTY_IMAGE);
	}

	public String getTrigger() 
	{
		return trigger;
	}

	public void getAttention(Direction direction) {}
	
	public void addData(GameData data) {}
	public void reset() {}
}
