package map.entity;

import gui.GameData;
import gui.view.MapView;

import java.awt.image.BufferedImage;

import main.InputControl;
import map.MapData;
import map.entity.MovableEntity.Direction;

public class TriggerEntity extends Entity
{
	private String trigger;

	public TriggerEntity(int x, int y, String trigger) 
	{
		super(x, y);
		this.trigger = trigger;

	}

	public void update(int dt, Entity[][] entity, MapData map, InputControl input, MapView view) {}

	protected BufferedImage getFrame(GameData data) 
	{
		// TODO: Make constant for empty image because this looks weird
		return data.getTrainerTiles().getTile(-1);
	}

	public String getTrigger() 
	{
		return trigger;
	}

	public void getAttention(Direction direction) {}
	
	public void addData(GameData data) {}
	public void reset() {}
}
