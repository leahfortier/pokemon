package map.entity;

import gui.view.MapView;
import main.Game;
import main.Global;
import map.Direction;
import map.MapData;
import map.MapData.WalkType;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import trainer.CharacterData;
import util.FloatPoint;
import util.InputControl;
import util.InputControl.Control;
import util.Point;

public class PlayerEntity extends MovableEntity {

	private boolean justMoved;
	private String npcTriggerSuffix;
	private boolean stalled;

	private boolean justCreated;

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
	public void update(int dt, Entity[][] entity, MapData map, InputControl input, MapView view) {
		super.update(dt, entity, map, input, view);

		CharacterData player = Game.getPlayer();

		if (!this.getLocation().equals(player.getLocation())) {
			entity[getX()][getY()] = null;
			entity[player.getX()][player.getY()] = this;
			transitionTime = 0;
		}

		if (player.direction != transitionDirection) {
			transitionDirection = player.direction;
		}
		
		npcTriggerSuffix = null;
		boolean spacePressed = false;
		if (transitionTime == 0 && !justMoved) {
			if (input.consumeIfDown(Control.SPACE)) {
				spacePressed = true;
			}
			else {
				for (Direction direction : Direction.values()) {
					// TODO: Check if this should be consumed
					if (input.isDown(direction.getKey()) && transitionTime == 0 && !stalled) {
						if (transitionDirection != direction) {
							transitionDirection = direction;
							continue;
						}

						Point delta = Point.add(player.getLocation(), direction.getDeltaPoint());
						
						WalkType curPassValue = map.getPassValue(player.getX(), player.getY());
						WalkType passValue = map.getPassValue(delta.x, delta.y);
						
						if (isPassable(passValue, direction) && entity[delta.x][delta.y] == null) {
							delta = Point.add(delta, getWalkTypeAdditionalMove(curPassValue, passValue, direction));

							entity[delta.x][delta.y] = this;
							entity[player.getX()][player.getY()] = null;

							player.setLocation(delta);
							player.step();

							transitionTime = 1;
							break;
							// TODO: Add support for multiple pressed keys.
							// Weird things happen when you hold one key and
							// press another.
						}
					}
				}
			}

			super.setLocation(player.getLocation());
			player.direction = transitionDirection;

			if (spacePressed) {
				Point newPoint = Point.add(this.getLocation(), transitionDirection.getDeltaPoint());
				int x = newPoint.x; // TODO
				int y = newPoint.y;
				
				if (map.inBounds(x, y) && entity[x][y] != null && entity[x][y] != currentInteractionEntity) {
					npcTriggerSuffix = entity[x][y].getTriggerSuffix();
					entity[x][y].getAttention(transitionDirection.opposite);
					currentInteractionEntity = entity[x][y];
				}
			}
			
			if (stalled) {
				for (Direction direction : Direction.values()) {
					Point newLocation = Point.add(this.getLocation(), Point.negate(direction.getDeltaPoint()));
					int x = newLocation.x; // TODO
					int y = newLocation.y;

					// TODO: Should have a method for this
					if (map.inBounds(x, y) && entity[x][y] != null && entity[x][y] != currentInteractionEntity) {
						npcTriggerSuffix = entity[x][y].getTriggerSuffix();
						currentInteractionEntity = entity[x][y];

						entity[x][y].getAttention(direction);
						player.direction = transitionDirection = direction.opposite;
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

		if (npcTriggerSuffix != null) {
			triggerName = TriggerType.GROUP.getTriggerNameFromSuffix(npcTriggerSuffix);
			npcTriggerSuffix = null;
		}
		else if (justMoved) {
			triggerName = map.trigger();
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
