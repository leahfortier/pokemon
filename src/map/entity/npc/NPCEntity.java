package map.entity.npc;

import gui.GameData;
import gui.view.MapView;
import main.Global;
import map.Direction;
import map.MapData;
import map.entity.Entity;
import map.entity.MovableEntity;
import map.entity.npc.NPCAction.BattleAction;
import map.triggers.Trigger.TriggerType;
import pattern.AreaDataMatcher;
import pattern.AreaDataMatcher.GroupTriggerMatcher;
import util.InputControl;
import util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class NPCEntity extends MovableEntity {
	public static final int NPC_SIGHT_DISTANCE = 5;

	private String name;
	private String path;
	private String tempPath;
	private int pathIndex;
	private int waitTime;
	private boolean hasAttention;

	private boolean walkToPlayer;
	private boolean walkingToPlayer;

	private Map<String, List<NPCAction>> interactions;
	private String currentInteractionKey;
	private int currentInteractionIndex;

	private int defaultX;
	private int defaultY;
	private Direction defaultDirection;

	private boolean dataCreated;

	NPCEntity(
			String name,
			int x,
			int y,
			String path,
			Direction direction,
			int index,
			boolean walkToPlayer,
			Map<String, List<NPCAction>> interactions,
			String startKey) {
		super(x, y, index, direction);
		
		this.name = name;
		this.path = path;
		
		tempPath = null;
		waitTime = 0;
		hasAttention = false;
		spriteIndex = index;

		this.walkToPlayer = walkToPlayer;
		this.walkingToPlayer = false;

		defaultX = x;
		defaultY = y;
		defaultDirection = direction;

		this.interactions = interactions;
		this.currentInteractionKey = startKey;
		this.currentInteractionIndex = 0;

//		dataCreated = firstDialogue.length == 0;
	}

	public void update(int dt, Entity[][] entity, MapData map, InputControl input, MapView view) {
		super.update(dt, entity, map, input, view);

		if (waitTime != 0) {
			waitTime -= dt;
		}

		if (waitTime < 0) {
			waitTime = 0;
		}

		if (transitionTime == 0 && waitTime == 0 && !hasAttention) {
			String path = this.path;
			if (tempPath != null) {
				path = tempPath;
				// System.out.println(path);
			}
			
			char pathChar = path.charAt(pathIndex);
			
			if (pathChar == Direction.WAIT_CHARACTER) {
				waitTime = getTransitionTime();
				pathIndex++;	
			}
			else {
				// Find the direction that corresponds to the character
				for (Direction direction: Direction.values()) {
					if (pathChar != direction.character) {
						continue;
					}
					
					int x = charX + direction.dx;
					int y = charY + direction.dy;
					
					// TODO: Shouldn't the isPassable method check if an entity doesn't exist in it as well? 
					if (isPassable(map.getPassValue(x, y)) && entity[x][y] == null) {
						entity[charX][charY] = null;
						
						charX = x;
						charY = y;
						
						entity[charX][charY] = this;
						
						transitionTime = 1;
						waitTime = 5*Global.TIME_BETWEEN_TILES/4;
						pathIndex++;
					}
					
					transitionDirection = direction;
				}
			}

			pathIndex %= path.length();
			if (pathIndex == 0 && tempPath != null) {
				tempPath = null;
			}
		}
	}

	public void walkTowards(int steps, Direction direction) {
		tempPath = Direction.WAIT_CHARACTER + "";
		for (int i = 0; i < steps; ++i) {
			tempPath += direction.character;
		}

		pathIndex = 0;
		walkingToPlayer = true;
	}

	public String getTrigger() {
		return this.getTriggerName(this.currentInteractionKey);
	}

	private String getTriggerName(final String interactionName) {
		return this.name + "_" + interactionName;
	}

	public int getTransitionTime() {
		return Global.TIME_BETWEEN_TILES * 2;
	}

	public void getAttention(Direction direction) {
		transitionDirection = direction;
		hasAttention = true;
	}

	public boolean getWalkToPlayer() {
		return walkToPlayer;
	}

	public boolean getWalkingToPlayer() {
		return walkingToPlayer;
	}

	// TODO: Yeah I still don't know what this wants
	// TODO: create NPCTrainerEntity
	public String getWalkTrigger() {
		return walkToPlayer ? this.getTrigger() : StringUtils.empty();
	}

	public boolean isTrainer() {
		List<NPCAction> actions = interactions.get(currentInteractionKey);
		for (NPCAction action : actions) {
			if (action instanceof BattleAction) {
				return true;
			}
		}

		return false;
	}

	public void reset() {
		charX = defaultX;
		charY = defaultY;
		waitTime = 0;
		pathIndex = 0;
		hasAttention = false;
		transitionDirection = defaultDirection;
		walkingToPlayer = false;
		tempPath = null;
	}
	
	public void addData(GameData data) {
		if (dataCreated) {
			return;
		}

		for (Entry<String, List<NPCAction>> interaction : this.interactions.entrySet()) {
			final String interactionName = interaction.getKey();
			final List<NPCAction> actions = interaction.getValue();

			final String triggerName = this.getTriggerName(interactionName);
			final String[] actionTriggerNames = new String[actions.size()];
			for (int i = 0; i < actions.size(); i++) {
				final String actionTriggerNameSuffix = "_action" + i;
				actionTriggerNames[i] = triggerName + actionTriggerNameSuffix;

				data.addTrigger(actions.get(i).getTrigger(triggerName, actionTriggerNameSuffix));
			}

			GroupTriggerMatcher matcher = new GroupTriggerMatcher(actionTriggerNames);
			final String groupContents = AreaDataMatcher.getJson(matcher);

			data.addTrigger(TriggerType.GROUP, triggerName, groupContents);
		}

//		// Create group trigger for initial encounter.
//		data.addTrigger("Group", name + "_GT1",
//
//			// Add all additional triggers
//			firstTriggers + "\n" +
//
//			// If trainer, battle at the end of first dialogue.
//			(trainer?
//				"trigger: " + name + "_Battle":
//				""
//			) +
//
//			// If not a trainer and is giving items, add to end of dialogue.
//			(!trainer && itemInfo != null?
//				"trigger: " + name + "_items":
//				""
//			)
//		);
//
//		// Create group trigger for additional encounters.
//		data.addTrigger("Group", name + "_GT2",
//
//			// Add all additional triggers
//			secondTriggers +"\n"+
//
//			// If trainer and giving items, add to end of dialogue.
//			(trainer && itemInfo != null?
//				"trigger: " + name + "_items":
//				""
//			)
//		);
//
//		// Create event trigger to bring up first dialogue.
//		trigger = name + "_T1";
//		data.addTrigger("Event", trigger,
//			"condition: !triggered_" + name +" \n" +
//			(trainer || secondDialogue.length == 0?"":("global: triggered_" + name + " \n")) +
//			"dialogue: " + trigger + "_D1"
//		);
//
//		// Add all first dialogue sequences. Call group trigger on last dialogue.
//		for (int i = 0; i < firstDialogue.length; ++i) {
//			data.addDialogue(name + "_T1_D" + (i + 1),
//				"text: \""+ firstDialogue[i] + "\" \n" +
//
//				// If not the last dialogue, transition to the next dialogue in the sequence
//				// Else, add the triggers
//				(i + 1 != firstDialogue.length?
//
//					// Move to each dialogue
//					"next[0]: " + name + "_T1_D" + (i + 2):
//
//					// End of dialogue, add triggers
//					"trigger[0]: " + name + "_GT1"
//				)
//			);
//		}
//
//		// If different dialogue for first and second encounter, create group trigger and second dialogue trigger
//		if (secondDialogue.length > 0) {
//			// Create and set group trigger to initiate multiple encounters.
//			trigger = name + "_GT";
//			data.addTrigger("Group", name + "_GT",
//				"trigger: " +name + "_T2\n"+
//				"trigger: " +name + "_T1"
//			);
//
//			// Create event trigger to bring up second dialogue.
//			data.addTrigger("Event", name + "_T2",
//				"condition: triggered_" + name +" \n" +
//				"dialogue: " + name +"_T2_D1"
//			);
//
//			// Add all second dialogue sequences. Call group trigger on last dialogue.
//			for (int i = 0; i < secondDialogue.length; ++i) {
//				data.addDialogue(name + "_T2_D" + (i + 1),
//					"text: \""+ secondDialogue[i] + "\" \n" +
//
//					// If not the last dialogue, transition to the next dialogue in the sequence
//					// Else, add the triggers
//					(i + 1 != secondDialogue.length ?
//
//						// Move to each dialogue
//						"next[0]: " + name + "_T2_D" + (i + 2):
//
//						// End of dialogue, add triggers
//						"trigger[0]: " +name + "_GT2"
//					)
//				);
//			}
//		}
		
		dataCreated = true;
	}
}
