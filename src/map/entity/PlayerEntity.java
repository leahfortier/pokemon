package map.entity;

import gui.view.MapView;
import input.ControlKey;
import input.InputControl;
import main.Game;
import main.Global;
import map.Direction;
import map.MapData;
import map.WalkType;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import trainer.CharacterData;
import util.FloatPoint;
import util.Point;

import java.util.ArrayList;
import java.util.List;

public class PlayerEntity extends MovableEntity {

	private boolean justCreated;
	private boolean justMoved;
	private boolean stalled;

	private String entityTriggerSuffix;
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
	public void update(int dt, MapData map, MapView view) {
		super.update(dt, map, view);

		CharacterData player = Game.getPlayer();
		InputControl input = InputControl.instance();
		
		entityTriggerSuffix = null;
		boolean spacePressed = false;

		if (!this.isTransitioning() && !justMoved) {
			if (input.consumeIfDown(ControlKey.SPACE)) {
				spacePressed = true;
			}
			else {
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

						WalkType curPassValue = map.getPassValue(player.getLocation());
						WalkType passValue = map.getPassValue(newLocation);

						if (isPassable(passValue, inputDirection) && !map.hasEntity(newLocation)) {
							newLocation = Point.add(newLocation, getWalkTypeAdditionalMove(curPassValue, passValue, inputDirection));

							player.setLocation(newLocation);
							player.step();

							// TODO: This seems to be a common default value -- should be in a method or something
							transitionTime = 1;
						}
					}
				}
			}

			if (spacePressed) {
				Point newLocation = Point.add(this.getLocation(), this.getDirection().getDeltaPoint());
				Entity entity = map.getEntity(newLocation);
				
				if (map.inBounds(newLocation) && entity != null && entity != currentInteractionEntity) {
					entityTriggerSuffix = entity.getTriggerSuffix();
					entity.getAttention(this.getDirection().getOpposite());
					currentInteractionEntity = entity;
				}
			}
			
			if (stalled) {
				for (Direction direction : Direction.values()) {
					Point newLocation = Point.add(this.getLocation(), direction.getDeltaPoint());
					Entity entity = map.getEntity(newLocation);

					// TODO: Should have a method for this
					if (map.inBounds(newLocation) && entity != null && entity != currentInteractionEntity) {
						entityTriggerSuffix = entity.getTriggerSuffix();
						currentInteractionEntity = entity;

						entity.getAttention(direction.getOpposite());
						this.setDirection(direction);
					}
				}
			}
		}

		justMoved = transitionTime == 1 || justCreated;
		justCreated = false;

		triggerCheck(map);
	}

	private Point getWalkTypeAdditionalMove(WalkType prev, WalkType next, Direction direction) {
		if (direction == Direction.LEFT) {
			if (next == WalkType.STAIRS_UP_LEFT) {
				return Direction.UP.getDeltaPoint();
			}
			else if (next == WalkType.STAIRS_UP_RIGHT) {
				return Direction.DOWN.getDeltaPoint();
			}
		}
		
		if (direction == Direction.RIGHT) {
			if (prev == WalkType.STAIRS_UP_LEFT) {
				return Direction.DOWN.getDeltaPoint();
			}
			else if (prev == WalkType.STAIRS_UP_RIGHT) {
				return Direction.UP.getDeltaPoint();
			}
		}
		
		return new Point();
	}

	private void triggerCheck(MapData map) {
		List<String> triggerNames = new ArrayList<>();

		if (entityTriggerSuffix != null) {
			triggerNames.add(TriggerType.GROUP.getTriggerNameFromSuffix(entityTriggerSuffix));
			entityTriggerSuffix = null;
		}
		else if (justMoved) {
			List<String> currentTriggerNames = map.getCurrentTriggers();
			if (currentTriggerNames != null) {
				triggerNames.addAll(currentTriggerNames);
			}

			justMoved = false;
		}

		// Execute all valid triggers
		for (String triggerName : triggerNames) {
			Trigger trigger = Game.getData().getTrigger(triggerName);
			if (trigger != null && trigger.isTriggered()) {
				trigger.execute();
			}
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
	public void getAttention(Direction direction) {
		this.setDirection(direction);
		stalled = true;
	}

	public void stall() {
		stalled = true;
	}

	public boolean isStalled() {
		return stalled;
	}

	public void resetCurrentInteractionEntity() {
		this.currentInteractionEntity = null;
		stalled = false;
	}

	@Override
	public void addData() {}

	@Override
	public void reset() {}
}
