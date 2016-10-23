package map.entity;

import gui.GameData;
import gui.view.MapView;

import java.awt.image.BufferedImage;

import map.DialogueSequence;
import map.MapData;
import map.entity.MovableEntity.Direction;
import map.triggers.Trigger;
import util.InputControl;
import util.PokeString;

class ItemEntity extends Entity {
	private String trigger;
	private boolean hasTriggered;
	private String name;
	private String item;
	private boolean dataCreated;
	
	public ItemEntity(int x, int y, String trigger) {
		super(x, y);
		this.trigger = trigger;
		hasTriggered = false;
	}

	public ItemEntity(String name, int x, int y, String item) {
		super(x,y);
		this.name = name;
		this.trigger = name;
		hasTriggered = false;
		this.item = item;
		dataCreated = false;
	}

	public void update(int dt, Entity[][] entity, MapData map, InputControl input, MapView view) {
		if (hasTriggered) {
			view.removeEntity(this);
		}
	}

	protected BufferedImage getFrame(GameData data) {
		return data.getTrainerTiles().getTile(0);
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
	
	public void addData(GameData data) {
		if (dataCreated) {
			return;
		}
		
		// TODO: Add support for multiple items.
		// TODO: Add support for multiple placements of the same item on the same map
		//		Add numbers to the end of entity name and condition?		
		Trigger eventTrigger = data.getTrigger(name);
		if (eventTrigger == null) {
			data.addTrigger("Event", name, "condition: !has" + name +" \n" +
					"global: has" + name + " \n" +
					"dialogue: " + name
			);
		}
		
		String itemTriggerName = "Item_" + PokeString.removeSpecialSymbols(item);
		String itemName = item.replace("_", " ");
		boolean vowelStart = ("" + item.charAt(0)).matches("[AEIOU]");
		DialogueSequence d = data.getDialogue(name);
		
		if (d == null) {
			data.addDialogue(name, "text: \"You found a" + (vowelStart ? "n" : "") + " " + itemName + "!\" \n" +
									"trigger[0]: " + itemTriggerName
									);
		}
		
		Trigger itemTrigger = data.getTrigger(itemTriggerName);
		
		if (itemTrigger == null) {
			data.addTrigger("Give", itemTriggerName, "item: " + itemName);
		}
		
//		System.out.println(name);
//		System.out.println(itemTriggerName);
//		System.out.println(data.getDialogue(name).text);
//		System.out.println(data.getDialogue(name).choices[0]);
//		System.out.println(data.getTrigger(name));
//		System.out.println(data.getTrigger(itemTriggerName));
		dataCreated = true;
	}
	
	public String toString() {
		return "Name: " + name + " Item:" + item;
	}
}
