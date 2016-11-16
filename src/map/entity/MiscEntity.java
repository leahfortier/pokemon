package map.entity;

import gui.TileSet;
import gui.view.MapView;
import main.Game;
import map.Direction;
import map.MapData;
import util.InputControl;
import util.Point;

import java.awt.image.BufferedImage;
import java.util.List;

public class MiscEntity extends Entity {
	private final List<EntityAction> actions;

	private boolean dataCreated;

	public MiscEntity(String name, Point location, String condition, List<EntityAction> actions) {
		super(location, name, condition);
		this.actions = actions;
		this.dataCreated = false;
	}

	public void update(int dt, Entity[][] entity, MapData map, InputControl input, MapView view) {}

	protected BufferedImage getFrame() {
		return Game.getData().getTrainerTiles().getTile(TileSet.EMPTY_IMAGE);
	}

	public void getAttention(Direction direction) {}
	
	public void addData() {
		if (dataCreated) {
			return;
		}

		EntityAction.addActionGroupTrigger(this.getEntityName(), this.getTriggerSuffix(), this.actions);
		dataCreated = true;
	}

	public void reset() {}
}