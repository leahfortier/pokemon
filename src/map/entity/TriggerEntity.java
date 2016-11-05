package map.entity;

import gui.GameData;
import gui.TileSet;
import gui.view.MapView;
import main.Game;
import map.Direction;
import map.MapData;
import map.entity.npc.EntityAction;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.GroupTriggerMatcher;
import util.InputControl;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map.Entry;

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

		GameData data = Game.getData();

		// TODO: Create a common method for this and merge with npc entity
		final String[] actionTriggerNames = new String[actions.size()];
		for (int i = 0; i < actions.size(); i++) {
			Trigger actionTrigger = actions.get(i).getTrigger(this.name);
			data.addTrigger(actionTrigger);
			actionTriggerNames[i] = actionTrigger.getName();
		}

		GroupTriggerMatcher matcher = new GroupTriggerMatcher(actionTriggerNames);
		matcher.suffix = this.getTriggerSuffix();
		final String groupContents = AreaDataMatcher.getJson(matcher);

		data.addTrigger(TriggerType.GROUP, groupContents);

		dataCreated = true;
	}

	public void reset() {}
}
