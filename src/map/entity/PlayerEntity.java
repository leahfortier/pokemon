package map.entity;

import gui.view.MapView;
import main.Game;
import main.Global;
import map.Direction;
import map.MapData;
import map.WalkType;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import trainer.CharacterData;
import util.FloatPoint;
import input.InputControl;
import input.ControlKey;
import util.Point;

public class PlayerEntity extends MovableEntity {

	private boolean justCreated;
	private boolean justMoved;
	private boolean stalled;

	private String entityTriggerSuffix;

	public PlayerEntity() {
		this(Game.getPlayer());
	}

	private PlayerEntity(CharacterData player) {
		super(player.getLocation(), null, null, 0, player.direction);

		justMoved = true;
		stalled = false;
		justCreated = true;
	}

	// Player is drawn in the center of the canvas
	@Override
	public Point getCanvasCoordinates(Point drawLocation) {
		return Point.scaleDown(Global.GAME_SIZE, 2);
	}

	@Override
	public void update(int dt, Entity[][] entities, MapData map, MapView view) {
		super.update(dt, entities, map, view);

		CharacterData player = Game.getPlayer();
		InputControl input = InputControl.instance();

		if (!this.getLocation().equals(player.getLocation())) {
			entities[getX()][getY()] = null;
			entities[player.getX()][player.getY()] = this;
			transitionTime = 0;
		}

		if (player.direction != transitionDirection) {
			transitionDirection = player.direction;
		}
		
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
					if (transitionDirection != inputDirection) {
						transitionDirection = inputDirection;
					}
					// Otherwise, advance in the input direction
					else {
						Point newLocation = Point.add(player.getLocation(), inputDirection.getDeltaPoint());

						WalkType curPassValue = map.getPassValue(player.getX(), player.getY());
						WalkType passValue = map.getPassValue(newLocation.x, newLocation.y);

						if (isPassable(passValue, inputDirection) && entities[newLocation.x][newLocation.y] == null) {
							newLocation = Point.add(newLocation, getWalkTypeAdditionalMove(curPassValue, passValue, inputDirection));

							entities[newLocation.x][newLocation.y] = this;
							entities[player.getX()][player.getY()] = null;

							player.setLocation(newLocation);
							player.step();

							// TODO: This seems to be a common default value -- should be in a method or something
							transitionTime = 1;
						}
					}
				}
			}

			super.setLocation(player.getLocation());
			player.direction = transitionDirection;

			if (spacePressed) {
				Point newLocation = Point.add(this.getLocation(), transitionDirection.getDeltaPoint());
				Entity entity = entities[newLocation.x][newLocation.y];
				
				if (map.inBounds(newLocation) && entity != null && entity != currentInteractionEntity) {
					entityTriggerSuffix = entity.getTriggerSuffix();
					entity.getAttention(transitionDirection.getOpposite());
					currentInteractionEntity = entity;
				}
			}
			
			if (stalled) {
				for (Direction direction : Direction.values()) {
					Point newLocation = Point.add(this.getLocation(), Point.negate(direction.getDeltaPoint()));
					Entity entity = entities[newLocation.x][newLocation.y];

					// TODO: Should have a method for this
					if (map.inBounds(newLocation) && entity != null && entity != currentInteractionEntity) {
						entityTriggerSuffix = entity.getTriggerSuffix();
						currentInteractionEntity = entity;

						entity.getAttention(direction);
						player.direction = transitionDirection = direction.getOpposite();
						stalled = false;
					}
				}
			}
		}

		justMoved = transitionTime == 1 || justCreated;
		justCreated = false;
	}

	public static Entity currentInteractionEntity;

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

	public void triggerCheck(MapData map) {
		String triggerName = null;

		if (entityTriggerSuffix != null) {
			triggerName = TriggerType.GROUP.getTriggerNameFromSuffix(entityTriggerSuffix);
			entityTriggerSuffix = null;
		}
		else if (justMoved) {
			triggerName = map.getCurrentTrigger();
			justMoved = false;
		}

		if (triggerName != null) {
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
		FloatPoint transitionDelta = FloatPoint.scale(transitionDirection.getDeltaPoint(), transitionLength);

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
		transitionDirection = direction;
		stalled = true;
	}

	public void stall() {
		stalled = true;
	}

	public boolean isStalled() {
		return stalled;
	}

	@Override
	public void addData() {}

	@Override
	public void reset() {}
}
