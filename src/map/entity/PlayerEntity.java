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
		// TODO: point shit
		super(new Point(data.locationX, data.locationY), null, null, 0, data.direction);

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

		// TODO: have a method to check if locations are equal
		if (charX != player.locationX || charY != player.locationY) {
			entity[charX][charY] = null;
			entity[player.locationX][player.locationY] = this;
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

						int dx = player.locationX + direction.dx;
						int dy = player.locationY + direction.dy;
						
						WalkType curPassValue = map.getPassValue(player.locationX, player.locationY);
						WalkType passValue = map.getPassValue(dx, dy);
						
						if (isPassable(passValue, direction) && entity[dx][dy] == null) {
							// dx += getWalkTypeAdditionalMove(passValue, i);
							dy += getWalkTypeAdditionalMove(curPassValue, passValue, direction);

							entity[dx][dy] = this;
							entity[player.locationX][player.locationY] = null;

							player.setLocation(dx, dy);
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

			charX = player.locationX;
			charY = player.locationY;
			player.direction = transitionDirection;

			if (spacePressed) {
				int x = transitionDirection.dx + charX;
				int y = transitionDirection.dy + charY;
				
				if (map.inBounds(x, y) && entity[x][y] != null && entity[x][y] != currentInteractionEntity) {
					npcTriggerSuffix = entity[x][y].getTriggerSuffix();
					entity[x][y].getAttention(transitionDirection.opposite);
					currentInteractionEntity = entity[x][y];
				}
			}
			
			if (stalled) {
				for (Direction direction : Direction.values()) {
					int x = charX - direction.dx;
					int y = charY - direction.dy;

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

	private int getWalkTypeAdditionalMove(WalkType prev, WalkType next, Direction direction) {
		if (direction == Direction.LEFT) {
			if (next == WalkType.STAIRS_UP_LEFT) {
				return Direction.UP.dy;
			}
			else if (next == WalkType.STAIRS_UP_RIGHT) {
				return Direction.DOWN.dy;
			}
		}
		
		if (direction == Direction.RIGHT) {
			if (prev == WalkType.STAIRS_UP_LEFT) {
				return Direction.DOWN.dy;
			}
			else if (prev == WalkType.STAIRS_UP_RIGHT) {
				return Direction.UP.dy;
			}
		}
		
		return 0;
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

		CharacterData player = Game.getPlayer();
		int playerX = player.locationX;
		int playerY = player.locationY;

		float[] res = new float[2];
		if (transitionTime > 0) {
			float len = Math.max(0f, (Global.TIME_BETWEEN_TILES - (float) transitionTime/*-dt*/) / Global.TIME_BETWEEN_TILES);
			res[0] = dimension.width/2 - (playerX - transitionDirection.dx*len)*Global.TILE_SIZE;
			res[1] = dimension.height/2 - (playerY - transitionDirection.dy*len)*Global.TILE_SIZE;
		}
		else {
			res[0] = dimension.width/2 - playerX*Global.TILE_SIZE;
			res[1] = dimension.height/2 - playerY*Global.TILE_SIZE;
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
