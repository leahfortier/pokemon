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
import util.InputControl;
import util.InputControl.Control;
import util.Point;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class PlayerEntity extends MovableEntity {

	private boolean justMoved;
	private String npcTriggerSuffix;
	private boolean stalled;

	private boolean justCreated;

	public PlayerEntity(CharacterData data) {
		super(data.location, null, null, 0, data.direction);

		justMoved = true;
		stalled = false;
		justCreated = true;
	}

	public void draw(Graphics g, float drawX, float drawY, boolean drawOnlyInTransition) {
		if (drawOnlyInTransition && transitionTime == 0) {
			return;
		}

		Dimension d = Global.GAME_SIZE;

		BufferedImage img = getFrame();
		g.drawImage(img,
				d.width / 2 - img.getWidth() / 2 + Global.TILE_SIZE / 2,
				(d.height / 2) + (Global.TILE_SIZE - img.getHeight()) - (Global.TILE_SIZE / 2),
				null); // TODO: draw metrics?
	}

	// TODO: Don't pass the entity array around goddamnit
	public void update(int dt, Entity[][] entity, MapData map, InputControl input, MapView view) {
		super.update(dt, entity, map, input, view);

		CharacterData player = Game.getPlayer();

		if (!this.location.equals(player.location)) {
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
			if (input.isDown(Control.SPACE)) {
				input.consumeKey(Control.SPACE);
				spacePressed = true;
			}
			else {
				for (Direction direction : Direction.values()) {
					if (input.isDown(direction.key) && transitionTime == 0 && !stalled) {
						if (transitionDirection != direction) {
							transitionDirection = direction;
							continue;
						}

						Point delta = Point.add(player.location, direction.getDeltaPoint());
						
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

			this.location = player.location;
			player.direction = transitionDirection;

			if (spacePressed) {
				Point newPoint = Point.add(this.location, transitionDirection.getDeltaPoint());
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
					Point newLocation = Point.add(this.location, Point.negate(direction.getDeltaPoint()));
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

	// TODO: should hold return value in an object instead of an arbitrary array
	public float[] getDrawLocation(Dimension dimension) {

		Point playerLocation = Game.getPlayer().location;

		float[] res = new float[2];
		if (transitionTime > 0) {
			float len = Math.max(0f, (Global.TIME_BETWEEN_TILES - (float) transitionTime/*-dt*/) / Global.TIME_BETWEEN_TILES);
			res[0] = dimension.width/2 - (playerLocation.x - transitionDirection.dx*len)*Global.TILE_SIZE;
			res[1] = dimension.height/2 - (playerLocation.y - transitionDirection.dy*len)*Global.TILE_SIZE;
		}
		else {
			res[0] = dimension.width/2 - playerLocation.x*Global.TILE_SIZE;
			res[1] = dimension.height/2 - playerLocation.y*Global.TILE_SIZE;
		}

		return res;
	}

	public String getTriggerSuffix() {
		return null;
	}

	public int getTransitionTime() {
		return Global.TIME_BETWEEN_TILES;
	}

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

	public void addData() {}

	public void reset() {}
}
