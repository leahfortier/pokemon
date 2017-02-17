package map.entity;

import gui.GameData;
import item.ItemNamesies;
import main.Game;
import map.Direction;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import pattern.GroupTriggerMatcher;
import util.JsonUtils;
import util.Point;
import util.StringUtils;

import java.awt.image.BufferedImage;

public class ItemEntity extends Entity {
	private final ItemNamesies itemName;

	private boolean hasTriggered;
	private boolean dataCreated;

	public ItemEntity(String name, Point location, String condition, ItemNamesies item) {
		super(location, name, condition);
		this.itemName = item;

		this.hasTriggered = false;
		this.dataCreated = false;
	}

	@Override
	public boolean isVisible() {
		return super.isVisible() && !hasTriggered;
	}

	@Override
	protected BufferedImage getFrame() {
		// TODO: Needs constant
		return Game.getData().getTrainerTiles().getTile(0);
	}

	@Override
	public void getAttention(Direction direction) {
		hasTriggered = true;
	}

	@Override
	public void reset() {
		hasTriggered = false;
	}

	@Override
	public void addData() {
		if (dataCreated) {
			return;
		}

		GameData data = Game.getData();
		final String itemTriggerSuffix = "ItemEntity_" + TriggerType.GIVE_ITEM.getTriggerName(this.itemName.getName());
		final String itemTriggerName = TriggerType.GROUP.getTriggerNameFromSuffix(itemTriggerSuffix);

		// Create a universal trigger for this item
		if (!data.hasTrigger(itemTriggerName)) {
			String itemDialogue = "You found " + StringUtils.articleString(this.itemName.getName()) + "!";

			Trigger dialogue = TriggerType.DIALOGUE.createTrigger(itemDialogue, null);
			Trigger giveItem = TriggerType.GIVE_ITEM.createTrigger(this.itemName.getName(), null);

			GroupTriggerMatcher groupTriggerMatcher = new GroupTriggerMatcher(itemTriggerSuffix, dialogue.getName(), giveItem.getName());
			TriggerType.GROUP.createTrigger(JsonUtils.getJson(groupTriggerMatcher), null);
		}

		// This trigger will only call the item trigger when the conditions apply
		GroupTriggerMatcher matcher = new GroupTriggerMatcher(this.getTriggerSuffix(), itemTriggerName);
		matcher.addGlobals("has" + this.getEntityName());

		TriggerType.GROUP.createTrigger(JsonUtils.getJson(matcher), null);

		dataCreated = true;
	}
}
