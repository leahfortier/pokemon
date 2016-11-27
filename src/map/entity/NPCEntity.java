package map.entity;

import gui.view.MapView;
import main.Game;
import main.Global;
import map.Direction;
import map.MapData;
import map.PathDirection;
import map.entity.EntityAction.BattleAction;
import trainer.CharacterData;
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
	public void update(int dt, Entity[][] entity, MapData map, MapView view) {
		super.update(dt, entity, map, view);

		// Decrease wait time
		waitTime = Math.max(0, waitTime - dt);

		// Not transitioning, not waiting, and does not have attention
		if (!this.isTransitioning() && waitTime == 0 && !hasAttention) {
			String path = this.path;
			if (tempPath != null) {
				path = tempPath;
			}

			// Find the direction that corresponds to the character
			PathDirection direction = PathDirection.getDirection(path.charAt(pathIndex));
			if (direction == PathDirection.WAIT) {
				waitTime = getTransitionTime();
				pathIndex++;
			}
			else {
				Point newPoint = Point.add(this.getLocation(), direction.getDeltaPoint());
				int x = newPoint.x; // TODO
				int y = newPoint.y;

				// TODO: Shouldn't the isPassable method check if an entity doesn't exist in it as well?
				if (isPassable(map.getPassValue(x, y)) && entity[x][y] == null) {
					entity[getX()][getY()] = null;
					super.setLocation(newPoint);
					entity[getX()][getY()] = this;

					transitionTime = 1;
					waitTime = 5*Global.TIME_BETWEEN_TILES/4; // TODO: Why 5/4
					pathIndex++;
				}

				transitionDirection = direction.getDirection();
			}

			pathIndex %= path.length();
			if (pathIndex == 0 && tempPath != null) {
				tempPath = null;
			}
		}
	}

	public void walkTowards(int steps, PathDirection direction) {
		tempPath = direction.getTempPath(steps);

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
		return Global.TIME_BETWEEN_TILES*2;
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

	// TODO: rename
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
		super.setLocation(defaultLocation);

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
