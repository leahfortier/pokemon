package map.entity;

import gui.GameData;
import gui.TileSet;
import gui.view.MapView;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import main.Game;
import main.Global;
import main.InputControl;
import main.Game.ViewMode;
import main.InputControl.Control;
import map.MapData;
import map.triggers.Trigger;
import trainer.CharacterData;

public class PlayerEntity extends Entity
{
	private static Control[] directionKeys = new Control[] {Control.RIGHT, Control.UP, Control.LEFT, Control.DOWN};
	
	private CharacterData charData;
	private boolean justMoved;
	private String npcTrigger;
	private String trainerTrigger;
	private boolean stalled;
	
	public PlayerEntity(CharacterData data)
	{
		super(data.locationX, data.locationY);
		charData = data;
		justMoved = false;
		stalled = false;
	}

	public void draw(Graphics g, GameData data, float drawX, float drawY, boolean drawOnlyInTransition)
	{
		if(drawOnlyInTransition && transitionTime == 0) return;
		
		Dimension d = Global.GAME_SIZE;
		
		BufferedImage img = getFrame(data);
		g.drawImage(img, d.width/2 - img.getWidth()/2 + Global.TILESIZE/2, (d.height/2) + (Global.TILESIZE - img.getHeight()) - (Global.TILESIZE/2), null);
	}
	
	public void update(int dt, Entity[][] entity, MapData map, InputControl input, MapView view) 
	{
		super.update(dt, entity, map, input, view);
		
		if (charX != charData.locationX || charY != charData.locationY)
		{
			entity[charX][charY]= null;
			entity[charData.locationX][charData.locationY] = this;
			transitionTime = 0;
		}
		
		if (charData.direction != transitionDirection)
		{
			transitionDirection = charData.direction;
		}
		npcTrigger = null;
		boolean spacePressed = false;
		if (transitionTime == 0)
		{
			if (input.isDown(Control.SPACE))
			{
				input.consumeKey(Control.SPACE);
				spacePressed = true;
			}
			else
			{
				for (int i = 0; i < directionKeys.length; i++)
				{
					if (input.isDown(directionKeys[i]) && transitionTime == 0 && !stalled && trainerTrigger == null)
					{
						if (transitionDirection != i)
						{
							transitionDirection = i;
							continue;
						}
						
						int dx = charData.locationX + fdx[i];
						int dy = charData.locationY + fdy[i];
						if (isPassable(map.getPassValue(dx, dy), i) && entity[dx][dy] == null)
						{
							entity[dx][dy] = this;
							entity[charData.locationX][charData.locationY] = null;
							
							charData.setLocation(dx, dy);
							charData.step();
							
							transitionTime = 1;
							break;
							//TODO: Add support for multiple pressed keys. Weird things happen when you hold one key and press another. 
						}
					}
				}
			}
			
			charX = charData.locationX;
			charY = charData.locationY;
			charData.direction = transitionDirection;
			
			if (spacePressed)
			{
				int x = fdx[transitionDirection] + charX;
				int y = fdy[transitionDirection] + charY;
				if (!(x < 0 || y < 0 || x >= entity.length || y >= entity[0].length) && (entity[x][y] != null))
				{
					npcTrigger = entity[x][y].getTrigger();
					entity[x][y].getAttention((transitionDirection + 2)%4);
					if (entity[x][y] instanceof NPCEntity && ((NPCEntity)entity[x][y]).isTrainer())
						trainerTrigger = entity[x][y].getTrigger();
				}
			}
			if (stalled)
			{
				for (int dir = 0; dir < tdx.length; ++dir)
				{
					int x = tdx[dir] + charX;
					int y = tdy[dir] + charY;
					if (!(x < 0 || y < 0 || x >= entity.length || y >= entity[0].length) && (entity[x][y] != null))
					{
						npcTrigger = entity[x][y].getTrigger();
						if (entity[x][y] instanceof NPCEntity && ((NPCEntity)entity[x][y]).isTrainer())
							trainerTrigger = entity[x][y].getTrigger();
						entity[x][y].getAttention(dir);
						charData.direction = transitionDirection = (dir+2)%tdx.length;
						stalled = false;
					}
				}
			}
		}
		
		justMoved = transitionTime == 1;
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
	
	protected BufferedImage getFrame(GameData data) 
	{
		TileSet trainerTiles = data.getTrainerTiles();
		if (transitionTime > 0)
			return trainerTiles.getTile(1+transitionDirection+4*(1+runFrame));
		return trainerTiles.getTile(1+transitionDirection);
	}

	public float[] getDrawLocation(Dimension d) 
	{
		float[] res = new float[2];
		if (transitionTime > 0)
		{
			float len = Math.max(0f, (Global.TIME_BETWEEN_TILES - (float)transitionTime/*-dt*/)/Global.TIME_BETWEEN_TILES);
			res[0] = d.width/2 - (charData.locationX+tdx[transitionDirection]*len)*Global.TILESIZE;
			res[1] = d.height/2 - (charData.locationY+tdy[transitionDirection]*len)*Global.TILESIZE;
		}
		else
		{
			res[0] = d.width/2 - charData.locationX*Global.TILESIZE;
			res[1] = d.height/2 - charData.locationY*Global.TILESIZE;
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

	public void getAttention(int d) 
	{
		transitionDirection = d;
		stalled = true;
	}
	
	public void stall(){
		stalled = true;
	}
	
	public boolean isStalled() {
		return stalled;
	}
	
	public void addData(GameData gameData) {}
}
