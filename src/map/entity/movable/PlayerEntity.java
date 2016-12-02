package map.entity.movable;

import gui.view.map.MapView;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import map.Direction;
import map.MapData;
import map.WalkType;
import map.entity.Entity;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import sound.SoundPlayer;
import sound.SoundTitle;
import trainer.CharacterData;
import util.FloatPoint;
import util.Point;

import java.util.List;

public class PlayerEntity extends MovableEntity {

	private boolean justCreated;
	private boolean justMoved;
	private boolean stalled;

	private Direction entityDirection;
	private Entity currentInteractionEntity;

	public PlayerEntity(Point location) {
		super(location, null, null, 0);

		justMoved = true;
		stalled = false;
		justCreated = true;
	}

	@Override
	public Point getLocation() {
		return Game.getPlayer().getLocation();
	}

	@Override
	public void setLocation(Point newLocation) {
		Game.getPlayer().setLocation(newLocation);
	}

	@Override
	public Direction getDirection() {
		return Game.getPlayer().getDirection();
	}

	@Override
	protected void setDirection(Direction direction) {
		Game.getPlayer().setDirection(direction);
	}

	// Player is drawn in the center of the canvas
	@Override
	public Point getCanvasCoordinates(Point drawLocation) {
		return Point.scaleDown(Global.GAME_SIZE, 2);
	}

	@Override
	public void update(int dt, MapData currentMap, MapView view) {
		super.update(dt, currentMap, view);

		if (this.hasTempPath()) {
			return;
		}

		InputControl input = InputControl.instance();

		entityDirection = null;

		boolean spacePressed = false;

		if (!stalled) {
			checkNPCs(currentMap);
		}

		if (!this.isTransitioning() && !justMoved) {
			if (input.consumeIfDown(ControlKey.SPACE)) {
				spacePressed = true;
			}
			else {
				checkMovement(currentMap);
			}

			if (spacePressed) {
				entityInteraction(this.getDirection(), currentMap);
			}
			
			if (stalled) {
				for (Direction direction : Direction.values()) {
					if (entityInteraction(direction, currentMap)) {
						this.setDirection(direction);
					}
				}
			}
		}

		justMoved = transitionTime == 1 || justCreated;
		justCreated = false;

		triggerCheck(currentMap);
	}

	// Check for any NPCs facing the player
	private void checkNPCs(MapData currentMap) {
		for (Direction direction : Direction.values()) {
			for (int dist = 1; dist <= NPCEntity.NPC_SIGHT_DISTANCE; dist++) {
				Point newLocation = Point.add(this.getLocation(), Point.scale(direction.getDeltaPoint(), dist));
				if (!currentMap.getPassValue(newLocation).isPassable(direction)) {
					break;
				}

				Entity newEntity = currentMap.getEntity(newLocation);
				if (newEntity instanceof NPCEntity) {
					NPCEntity npc = (NPCEntity) newEntity;
					if (npc.canWalkToPlayer(this.getLocation())) {
						stalled = true;
						npc.walkTowards(dist - 1, direction.getOpposite().getPathDirection());

						if (npc.isTrainer()) {
							SoundPlayer.soundPlayer.playMusic(SoundTitle.TRAINER_SPOTTED);
						}
					}
				}
			}
		}
	}

	private void checkMovement(MapData currentMap) {
		CharacterData player = Game.getPlayer();

		// TODO: Add support for multiple pressed keys. Weird things happen when you hold one key and press another.
		Direction inputDirection = Direction.checkInputDirection();
		if (inputDirection != null && !isTransitioning() && !stalled) {

			// If not facing the input direction, transition this way
			if (this.getDirection() != inputDirection) {
				this.setDirection(inputDirection);
			}
			// Otherwise, advance in the input direction
			else {
				Point newLocation = Point.add(player.getLocation(), inputDirection.getDeltaPoint());

				WalkType curPassValue = currentMap.getPassValue(player.getLocation());
				WalkType passValue = currentMap.getPassValue(newLocation);

				if (passValue.isPassable(inputDirection) && !currentMap.hasEntity(newLocation)) {
					newLocation = Point.add(newLocation, WalkType.getAdditionalMove(curPassValue, passValue, inputDirection).getDeltaPoint());

					player.setLocation(newLocation);
					player.step();

					// TODO: This seems to be a common default value -- should be in a method or something
					transitionTime = 1;
				}
			}
		}
	}

	// Check if there is an entity next to the player in the specified direction
	// If so, set this entity as the current interaction entity
	private boolean entityInteraction(Direction direction, MapData currentMap) {
		Point newLocation = Point.add(this.getLocation(), direction.getDeltaPoint());
		Entity entity = currentMap.getEntity(newLocation);

		if (entity != null && entity != currentInteractionEntity) {
			entityDirection = direction;
			currentInteractionEntity = entity;
			return true;
		}

		return false;
	}

	private void triggerCheck(MapData map) {

		// Entity
		if (entityDirection != null) {
			if (currentInteractionEntity.isVisible()) {
				currentInteractionEntity.getAttention(entityDirection.getOpposite());

				Trigger trigger = Game.getData().getTrigger(TriggerType.GROUP.getTriggerNameFromSuffix(currentInteractionEntity.getTriggerSuffix()));
				if (trigger != null) {
					trigger.execute();
				}
			}
			entityDirection = null;
		}
		// Trigger
		else if (justMoved) {
			List<String> currentTriggerNames = map.getCurrentTriggers();
			if (currentTriggerNames != null) {
				// Execute all valid triggers
				for (String triggerName : currentTriggerNames) {
					Trigger trigger = Game.getData().getTrigger(triggerName);
					if (trigger != null && trigger.isTriggered()) {
						trigger.execute();
					}
				}
			}

			justMoved = false;
		}
	}

	public Point getDrawLocation() {
		float transitionLength = 0;
		if (transitionTime > 0) {
			transitionLength = Math.max(0f, (Global.TIME_BETWEEN_TILES - (float) transitionTime/*-dt*/) / Global.TIME_BETWEEN_TILES);
		}

		// Scale by the length of the transition in the current direction
		FloatPoint transitionDelta = FloatPoint.scale(this.getDirection().getDeltaPoint(), transitionLength);

		// Get the location relative to the player
		FloatPoint transitionLocationUnscaled = FloatPoint.subtract(Game.getPlayer().getLocation(), transitionDelta);

		// Scale by the tile size
		Point transitionLocation = FloatPoint.scale(transitionLocationUnscaled, Global.TILE_SIZE).getPoint();

		// The location to draw should be the center of the window scaled in the direction of the transition by the delta amount
		Point windowCenter = Point.scaleDown(Global.GAME_SIZE, 2);
		return Point.subtract(windowCenter, transitionLocation);
	}

	@Override
	public String getTriggerSuffix() {
		return null;
	}

	@Override
	public int getTransitionTime() {
		return Global.TIME_BETWEEN_TILES;
	}

	@Override
	public String getPath() {
		return null;
	}

	public void setPath(String path) {
		super.setTempPath(path);
		stalled = true;
	}

	@Override
	protected void endPath() {
		stalled = false;
	}

	@Override
	public boolean hasAttention() {
		return false;
	}

	@Override
	public void getAttention(Direction direction) {
		this.setDirection(direction);
		stalled = true;
	}

	public void resetCurrentInteractionEntity() {
		currentInteractionEntity = null;
		stalled = false;
	}

	@Override
	public void addData() {}

	@Override
	public void reset() {}
}