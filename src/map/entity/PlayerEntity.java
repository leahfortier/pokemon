package map.entity;

import gui.GameData;
import gui.view.MapView;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import main.Game;
import main.Game.ViewMode;
import main.Global;
import map.MapData;
import map.MapData.WalkType;
import map.triggers.Trigger;
import trainer.CharacterData;
import util.InputControl;
import util.InputControl.Control;

public class PlayerEntity extends MovableEntity
{
	private CharacterData charData;
	private boolean justMoved;
	private String npcTrigger;
	private String trainerTrigger;
	private boolean stalled;

	private boolean justCreated;

	public PlayerEntity(CharacterData data)
	{
		super(data.locationX, data.locationY, 0, data.direction);
		
		charData = data;
		justMoved = true;
		stalled = false;
		justCreated = true;
	}

	public void draw(Graphics g, GameData data, float drawX, float drawY, boolean drawOnlyInTransition)
	{
		if (drawOnlyInTransition && transitionTime == 0)
			return;

		Dimension d = Global.GAME_SIZE;

		BufferedImage img = getFrame(data);
		g.drawImage(img, d.width / 2 - img.getWidth() / 2 + Global.TILESIZE / 2, (d.height / 2) + (Global.TILESIZE - img.getHeight()) - (Global.TILESIZE / 2), null);
	}

	public void update(int dt, Entity[][] entity, MapData map, InputControl input, MapView view)
	{
		super.update(dt, entity, map, input, view);

		if (charX != charData.locationX || charY != charData.locationY)
		{
			entity[charX][charY] = null;
			entity[charData.locationX][charData.locationY] = this;
			transitionTime = 0;
		}

		if (charData.direction != transitionDirection)
		{
			transitionDirection = charData.direction;
		}
		
		npcTrigger = null;
		boolean spacePressed = false;
		if (transitionTime == 0 && !justMoved)
		{
			if (input.isDown(Control.SPACE))
			{
				input.consumeKey(Control.SPACE);
				spacePressed = true;
			}
			else
			{
				for (Direction direction : Direction.values())
				{
					if (input.isDown(direction.key) && transitionTime == 0 && !stalled && trainerTrigger == null)
					{
						if (transitionDirection != direction)
						{
							transitionDirection = direction;
							continue;
						}

						int dx = charData.locationX + direction.dx;
						int dy = charData.locationY + direction.dy;
						
						WalkType curPassValue = map.getPassValue(charData.locationX, charData.locationY);
						WalkType passValue = map.getPassValue(dx, dy);
						
						if (isPassable(passValue, direction) && entity[dx][dy] == null)
						{
							// dx += getWalkTypeAdditionalMove(passValue, i);
							dy += getWalkTypeAdditionalMove(curPassValue, passValue, direction);

							entity[dx][dy] = this;
							entity[charData.locationX][charData.locationY] = null;

							charData.setLocation(dx, dy);
							charData.step();

							transitionTime = 1;
							break;
							// TODO: Add support for multiple pressed keys.
							// Weird things happen when you hold one key and
							// press another.
						}
					}
				}
			}

			charX = charData.locationX;
			charY = charData.locationY;
			charData.direction = transitionDirection;

			if (spacePressed)
			{
				int x = transitionDirection.dx + charX;
				int y = transitionDirection.dy + charY;
				
				if (!(x < 0 || y < 0 || x >= entity.length || y >= entity[0].length) && (entity[x][y] != null))
				{
					npcTrigger = entity[x][y].getTrigger();
					entity[x][y].getAttention(transitionDirection.opposite);
					
					if (entity[x][y] instanceof NPCEntity && ((NPCEntity) entity[x][y]).isTrainer())
						trainerTrigger = entity[x][y].getTrigger();
				}
			}
			
			if (stalled)
			{
				for (Direction direction : Direction.values())
				{
					int x = charX - direction.dx;
					int y = charY - direction.dy;
					
					if (!(x < 0 || y < 0 || x >= entity.length || y >= entity[0].length) && (entity[x][y] != null))
					{
						npcTrigger = entity[x][y].getTrigger();
						if (entity[x][y] instanceof NPCEntity && ((NPCEntity) entity[x][y]).isTrainer())
							trainerTrigger = entity[x][y].getTrigger();
						
						entity[x][y].getAttention(direction);
						charData.direction = transitionDirection = direction.opposite;
						stalled = false;
					}
				}
			}
		}

		justMoved = transitionTime == 1 || justCreated;
		justCreated = false;
	}

	public int getWalkTypeAdditionalMove(WalkType prev, WalkType next, Direction direction)
	{
		if (direction == Direction.UP || direction == Direction.DOWN)
		{
			return 0;
		}
		
		if (direction == Direction.LEFT)
		{
			if (next == WalkType.STAIRS_UP_LEFT)
			{
				return Direction.UP.dy;
			}
			else if (next == WalkType.STAIRS_UP_RIGHT)
			{
				return Direction.DOWN.dy;
			}
		}
		
		if (direction == Direction.RIGHT)
		{
			if (prev == WalkType.STAIRS_UP_LEFT)
			{
				return Direction.DOWN.dy;
			}
			else if (prev == WalkType.STAIRS_UP_RIGHT)
			{
				return Direction.UP.dy;
			}
		}
		
		return 0;
	}

	public void triggerCheck(Game game, MapData map)
	{
		String triggerName = null;

		if (npcTrigger != null)
		{
			triggerName = npcTrigger;
			npcTrigger = null;
		}
		else if (justMoved)
		{
			triggerName = map.trigger(charData);
			justMoved = false;
		}
		else if (!stalled && trainerTrigger != null && game.getCurrentViewMode() == ViewMode.MAP_VIEW)
		{
			triggerName = trainerTrigger;
			trainerTrigger = null;
		}

		if (triggerName != null)
		{
			Trigger trigger = game.data.getTrigger(triggerName);
			if (trigger != null && trigger.isTriggered(game.charData))
			{
				trigger.execute(game);
			}
		}
	}

	public float[] getDrawLocation(Dimension d)
	{
		float[] res = new float[2];
		if (transitionTime > 0)
		{
			float len = Math.max(0f, (Global.TIME_BETWEEN_TILES - (float) transitionTime/*-dt*/) / Global.TIME_BETWEEN_TILES);
			res[0] = d.width / 2 - (charData.locationX - transitionDirection.dx * len) * Global.TILESIZE;
			res[1] = d.height / 2 - (charData.locationY - transitionDirection.dy * len) * Global.TILESIZE;
		}
		else
		{
			res[0] = d.width / 2 - charData.locationX * Global.TILESIZE;
			res[1] = d.height / 2 - charData.locationY * Global.TILESIZE;
		}

		return res;
	}

	public String getTrigger()
	{
		return null;
	}

	public int getTransitionTime()
	{
		return Global.TIME_BETWEEN_TILES;
	}

	public void getAttention(Direction direction)
	{
		transitionDirection = direction;
		stalled = true;
	}

	public void stall()
	{
		stalled = true;
	}

	public boolean isStalled()
	{
		return stalled;
	}

	public void addData(GameData gameData) {}

	public void reset() {}
}
