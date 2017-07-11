package map.entity.movable;

import gui.view.map.MapView;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import map.Direction;
import map.MapData;
import map.entity.Entity;
import map.overworld.WalkType;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import sound.SoundPlayer;
import sound.SoundTitle;
import trainer.player.Player;
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
		super(location, null, null);

		justMoved = true;
		justCreated = true;

        this.unstall();
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
	public boolean isPassable() {
		return false;
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

		if (!this.isStalled()) {
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
			
			if (this.isStalled()) {
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
				if (newEntity instanceof NPCEntity && newEntity != currentInteractionEntity) {
					NPCEntity npc = (NPCEntity) newEntity;
					if (npc.canWalkToPlayer()) {
						this.stall();
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
		Player player = Game.getPlayer();
		Direction inputDirection = Direction.checkInputDirection();
		if (inputDirection != null && !isTransitioning() && !this.isStalled()) {

			// If not facing the input direction, transition this way
			if (this.getDirection() != inputDirection) {
				this.setDirection(inputDirection);
			}
			// Otherwise, advance in the input direction
			else {
				Point newLocation = this.getNewLocation(player.getLocation(), inputDirection, currentMap);
				if (newLocation != null) {
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

		if (entity instanceof NPCEntity && ((NPCEntity)entity).hasTempPath()) {
			return false;
		}

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
			transitionLength = Math.max(0f, (this.getTimeBetweenTiles() - (float) transitionTime/*-dt*/) / this.getTimeBetweenTiles());
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
	protected int getSpriteIndex() {
		Player player = Game.getPlayer();

        if (Game.getData().getMap(player.getMapName()).getPassValue(this.getLocation()) == WalkType.WATER) {
            // Surfing
            return 21;
        }
        else if (player.isBiking()) {
            // Biking
            return 22;
        }
		else {
			// Walking
			return 0;
		}
	}

	@Override
	public int getTransitionTime() {
		return this.getTimeBetweenTiles();
	}

	@Override
	public String getPath() {
		return null;
	}

	public void setPath(String path, EndPathListener listener) {
		super.setTempPath(path, listener);
        this.stall();
	}

	private boolean isStalled() {
        return this.stalled;
    }

	public void stall() {
		stalled = true;
	}

	public void unstall() {
        stalled = false;
	}

	@Override
	protected void endPath() {
        this.unstall();
	}

	@Override
	public boolean hasAttention() {
		return false;
	}

	@Override
	public void getAttention(Direction direction) {
		this.setDirection(direction);
		this.stall();
	}

	public void resetCurrentInteractionEntity() {
		currentInteractionEntity = null;
		this.unstall();
	}

	// Double speed while biking
	protected int getTimeBetweenTiles() {
		return super.getTimeBetweenTiles()/(Game.getPlayer().isBiking() ? 2 : 1);
	}
}
