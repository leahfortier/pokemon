package map.entity;

import gui.GameData;
import gui.view.MapView;
import main.Game;
import map.Direction;
import map.MapData;
import map.triggers.EventTrigger;
import map.triggers.GiveTrigger;
import map.triggers.GroupTrigger;
import map.triggers.Trigger;
import namesies.ItemNamesies;
import pattern.AreaDataMatcher.GroupTriggerMatcher;
import util.InputControl;
import util.StringUtils;

import java.awt.image.BufferedImage;

class ItemEntity extends Entity {
	private String trigger;
	private boolean hasTriggered;
	private String name;
	private ItemNamesies itemName;

	private boolean dataCreated;

	public ItemEntity(String name, int x, int y, String item) {
		super(x,y);
		this.name = name;
		this.trigger = name;
		hasTriggered = false;
		this.itemName = ItemNamesies.getValueOf(item);
		dataCreated = false;
	}

	public void update(int dt, Entity[][] entity, MapData map, InputControl input, MapView view) {
		if (hasTriggered) {
			view.removeEntity(this);
		}
	}

	protected BufferedImage getFrame() {
		return Game.getData().getTrainerTiles().getTile(0);
	}

	public String getTrigger() {
		return trigger;
	}

	public void getAttention(Direction direction) {
		hasTriggered = true;
	}
	
	public void reset() {
		hasTriggered = false;
	}
	
	public void addData() {
		if (dataCreated) {
			return;
		}

		GameData data = Game.getData();
		final String itemTriggerName = "item_" + this.itemName.name().toLowerCase();

		// Create a universal trigger for this item
		if (!data.hasTrigger(itemTriggerName)) {
			String itemDialogue = "You found " + StringUtils.articleString(itemName.getName()) + "!";

			Trigger dialogue = new EventTrigger(itemTriggerName + "_event", itemDialogue);
			Trigger giveItem = new GiveTrigger(itemTriggerName + "_item", this.itemName);
			Trigger groupTrigger = new GroupTrigger(
					itemTriggerName,
					new GroupTriggerMatcher(new String[] {
							dialogue.getName(),
							giveItem.getName()
					}));

			data.addTrigger(dialogue);
			data.addTrigger(giveItem);
			data.addTrigger(groupTrigger);
		}

		// This trigger will only call the item trigger when the conditions apply
		GroupTriggerMatcher matcher = new GroupTriggerMatcher(new String[] { itemTriggerName });
		matcher.condition = "!has" + name;
		matcher.globals.add("has" + name);

		data.addTrigger(new GroupTrigger(name, matcher));

		dataCreated = true;
	}
	
	public String toString() {
		return "Name: " + name + " Item:" + itemName.getName();
	}
}
