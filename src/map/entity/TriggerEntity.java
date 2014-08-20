package map.entity;

import gui.GameData;
import gui.view.MapView;

import java.awt.image.BufferedImage;

import main.InputControl;
import map.MapData;

public class TriggerEntity extends Entity{
	private String trigger;

	public TriggerEntity(int x, int y, String trigger) {
		super(x, y);
		this.trigger = trigger;

	}

	@Override
	public void update(int dt, Entity[][] entity, MapData map, InputControl input, MapView view) {
		super.update(dt, entity, map, input, view);
		
	}

	@Override
	protected BufferedImage getFrame(GameData data) {
		return data.getTrainerTiles().getTile(-1);
	}

	@Override
	public String getTrigger() {
		return trigger;
	}

	@Override
	public int getTransitionTime() {
		return 0;
	}

	@Override
	public void getAttention(int d) {
		transitionDirection = d;
	}
	
	@Override
	public void addData(GameData data){
		
	}
}
