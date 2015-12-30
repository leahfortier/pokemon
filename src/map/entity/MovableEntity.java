package map.entity;

import gui.GameData;
import gui.TileSet;
import gui.view.MapView;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

import util.InputControl;
import util.InputControl.Control;
import main.Global;
import map.MapData;

public abstract class MovableEntity extends Entity
{
	public enum Direction
	{
		RIGHT('r', 1, 0, Control.RIGHT),
		UP('u', 0, -1, Control.UP),
		LEFT('l', -1, 0, Control.LEFT),
		DOWN('d', 0, 1, Control.DOWN);
		
		public final char character;
		public final int dx;
		public final int dy;
		public final Control key;
		public Direction opposite; // Really this should be final but it won't let me include this in the constructor
		
		private Direction(char character, int dx, int dy, Control key)
		{
			this.character = character;
			
			this.dx = dx;
			this.dy = dy;
			
			this.key = key;
		}
		
		public static char WAIT_CHARACTER = 'w';
		
		static
		{
			RIGHT.opposite = LEFT;
			UP.opposite = DOWN;
			LEFT.opposite = RIGHT;
			DOWN.opposite = UP;
		}
	}
	
	protected Direction transitionDirection;
	protected int transitionTime;
	protected int runFrame;
	
	protected int spriteIndex;
	
	public MovableEntity(int x, int y, int spriteIndex, Direction startDirection)
	{
		super(x, y);
		
		this.transitionDirection = startDirection;
		
		this.transitionTime = 0;
		this.runFrame = 0;
		
		this.spriteIndex = spriteIndex;
	}
	
	public void draw(Graphics g, GameData data, float drawX, float drawY, boolean drawOnlyInTransition)
	{
		if (drawOnlyInTransition && transitionTime == 0)
			return;
		
		Point canvasCoordinates = getCanvasCoordinates(drawX, drawY);
		
		if (transitionTime != 0)
		{
			int len = Global.TILESIZE * (getTransitionTime() - transitionTime) / getTransitionTime();
			
			canvasCoordinates.x -= transitionDirection.dx * len;
			canvasCoordinates.y -= transitionDirection.dy * len;
			
			// System.out.println(transitionTime + " " +len + " " +cx + " " + cy);
		}
		
		super.draw(g, data, canvasCoordinates);
	}
	
	public void update(int dt, Entity[][] entity, MapData map, InputControl input, MapView view)
	{
		if (transitionTime != 0) 
		{
			transitionTime += dt;	
		}
		
		if (transitionTime > getTransitionTime())
		{
			transitionTime = 0;
			runFrame = (runFrame + 1)%2;
		}
	}
	
	public Direction getDirection()
	{
		return transitionDirection;
	}

	public void setDirection(Direction direction)
	{
		transitionDirection = direction;
	}

	public boolean isFacing(int x, int y)
	{
		if (x != charX && y != charY)
			return false;
		
		int dx = (int) Math.signum(x - charX);
		int dy = (int) Math.signum(y - charY);

		return transitionDirection.dx == dx && transitionDirection.dy == dy;
	}
	
	protected BufferedImage getFrame(GameData data)
	{
		TileSet trainerTiles = data.getTrainerTiles();
		if (transitionTime > 0)
		{
			return trainerTiles.getTile(12 * spriteIndex + 1 + transitionDirection.ordinal() + 4 * (1 + runFrame));
		}

		return trainerTiles.getTile(12 * spriteIndex + 1 + transitionDirection.ordinal());
	}
	
	public abstract int getTransitionTime();
}
