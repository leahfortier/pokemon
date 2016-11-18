package map.entity;

import gui.view.MapView;
import main.Game;
import main.Global;
import map.Direction;
import map.MapData;
import map.entity.EntityAction.BattleAction;
import trainer.CharacterData;
import util.InputControl;
import util.Point;
import util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class NPCEntity extends MovableEntity {
	public static final int NPC_SIGHT_DISTANCE = 5;

	private final String path;
	private final Point defaultLocation;
	private final Direction defaultDirection;

	private final Map<String, NPCInteraction> interactions;
	private final String startKey;

	private String tempPath;
	private int pathIndex;
	private int waitTime;
	private boolean hasAttention;
	private boolean walkingToPlayer;

	private boolean dataCreated;

	public NPCEntity(
			String name,
			Point location,
			String condition,
			String path,
			Direction direction,
			int spriteIndex,
			Map<String, NPCInteraction> interactions,
			String startKey) {
		super(location, name, condition, spriteIndex, direction);

		this.path = path;
		this.spriteIndex = spriteIndex;

		this.defaultLocation =  location;
		this.defaultDirection = direction;

		this.interactions = interactions;
		this.startKey = startKey;
	}

	@Override
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

					Point newPoint = Point.add(this.location, direction.getDeltaPoint());
					int x = newPoint.x; // TODO
					int y = newPoint.y;
					
					// TODO: Shouldn't the isPassable method check if an entity doesn't exist in it as well? 
					if (isPassable(map.getPassValue(x, y)) && entity[x][y] == null) {
						entity[getX()][getY()] = null;
						
						this.location = newPoint;
						
						entity[getX()][getY()] = this;
						
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

	private String getCurrentInteractionKey() {
		CharacterData player = Game.getPlayer();
		if (player.hasNpcInteraction(this.getEntityName())) {
			return player.getNpcInteractionName(this.getEntityName());
		}

		return this.startKey;
	}

	@Override
	public String getTriggerSuffix() {
		return this.getTriggerSuffix(this.getCurrentInteractionKey());
	}

	private String getTriggerSuffix(final String interactionName) {
		return super.getTriggerSuffix() + "_" + interactionName;
	}

	@Override
	public int getTransitionTime() {
		return Global.TIME_BETWEEN_TILES * 2;
	}

	@Override
	public void getAttention(Direction direction) {
		transitionDirection = direction;
		hasAttention = true;
	}

	public boolean shouldWalkToPlayer() {
		final String interaction = this.getCurrentInteractionKey();
		return this.interactions.get(interaction).shouldWalkToPlayer();
	}

	public boolean getWalkingToPlayer() {
		return walkingToPlayer;
	}

	public String getWalkTrigger() {
		return shouldWalkToPlayer() ? this.getTriggerName() : StringUtils.empty();
	}

	public boolean isTrainer() {
		NPCInteraction interaction = interactions.get(this.getCurrentInteractionKey());
		List<EntityAction> actions = interaction.getActions();
		for (EntityAction action : actions) {
			if (action instanceof BattleAction) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void reset() {
		location = defaultLocation;
		waitTime = 0;
		pathIndex = 0;
		hasAttention = false;
		transitionDirection = defaultDirection;
		walkingToPlayer = false;
		tempPath = null;
	}

	@Override
	public void addData() {
		if (dataCreated) {
			return;
		}

		for (Entry<String, NPCInteraction> interaction : this.interactions.entrySet()) {
			final String interactionName = interaction.getKey();
			final List<EntityAction> actions = interaction.getValue().getActions();

			EntityAction.addActionGroupTrigger(this.getEntityName(), this.getTriggerSuffix(interactionName), actions);
		}
		
		dataCreated = true;
	}
}
