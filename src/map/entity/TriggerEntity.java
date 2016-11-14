package map.entity;

import gui.TileSet;
import gui.view.MapView;
import main.Game;
import map.Direction;
import map.MapData;
import util.InputControl;

import java.awt.image.BufferedImage;
import java.util.List;

public class TriggerEntity extends Entity {
	private final String name;
	private final List<EntityAction> actions;

	private boolean dataCreated;

	public TriggerEntity(String name, int x, int y, List<EntityAction> actions) {
		super(x, y);
		this.name = name;
		this.actions = actions;
		this.dataCreated = false;
	}

	public void update(int dt, Entity[][] entity, MapData map, InputControl input, MapView view) {}

	protected BufferedImage getFrame() {
		return Game.getData().getTrainerTiles().getTile(TileSet.EMPTY_IMAGE);
	}

	public String getTriggerSuffix() {
		return this.name;
	}

	public void getAttention(Direction direction) {}
	
	public void addData() {
		if (dataCreated) {
			return;
		}

		EntityAction.addActionGroupTrigger(this.name, this.getTriggerSuffix(), this.actions);
		dataCreated = true;
	}

	public void reset() {}
}
