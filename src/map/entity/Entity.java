package map.entity;

import gui.GameData;
import gui.view.MapView;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import main.Global;
import main.InputControl;
import map.MapData;
import map.MapData.WalkType;

public abstract class Entity
{
	// Transition directions
	public static final int[] tdx = {-1, 0, 1, 0};
	public static final int[] tdy = {0, 1, 0, -1};

	// facing directions
	public static final int[] fdx = {1, 0, -1, 0};
	public static final int[] fdy = {0, -1, 0, 1};

	public static final char[] pathDir = {'r', 'u', 'l', 'd'};

	protected int transitionTime, transitionDirection;
	protected int runFrame;
	public int charX, charY;

	public Entity(int x, int y)
	{
		transitionTime = 0;
		transitionDirection = 3;
		runFrame = 0;
		charX = x;
		charY = y;
	}

	public void draw(Graphics g, GameData data, float drawX, float drawY, boolean drawOnlyInTransition)
	{
		if (drawOnlyInTransition && transitionTime == 0)
			return;
		
		int cx = (int) drawX + Global.TILESIZE * charX;
		int cy = (int) drawY + Global.TILESIZE * charY;
		
		if (transitionTime != 0)
		{
			int len = Global.TILESIZE * (getTransitionTime() - transitionTime) / getTransitionTime();
			cx += tdx[transitionDirection] * len;
			cy += tdy[transitionDirection] * len;
			// System.out.println(transitionTime +" " +len +" " +cx +" " + cy);
		}
		
		BufferedImage img = getFrame(data);
		g.drawImage(img, cx - img.getWidth() / 2 + Global.TILESIZE / 2, cy + (Global.TILESIZE - img.getHeight()) - (Global.TILESIZE / 2), null);
	}

	public void update(int dt, Entity[][] entity, MapData map, InputControl input, MapView view)
	{
		if (transitionTime != 0)
			transitionTime += dt;
		
		if (transitionTime > getTransitionTime())
		{
			transitionTime = 0;
			runFrame = (runFrame + 1) % 2;
		}

	}

	protected boolean isPassable(WalkType type)
	{
		return type == WalkType.WALKABLE;
	}

	protected boolean isPassable(WalkType type, int direction)
	{
		switch (type)
		{
			case HOP_RIGHT:
				if (direction == 0)
					return true;
				return false;
			case HOP_UP:
				if (direction == 1)
					return true;
				return false;
			case HOP_LEFT:
				if (direction == 2)
					return true;
				return false;
			case HOP_DOWN:
				if (direction == 3)
					return true;
				return false;
			case NOT_WALKABLE:
				return false;
			case STAIRS_UP_RIGHT:
			case STAIRS_UP_LEFT:
			case WALKABLE:
				return true;
			case WATER:
				return false;// TODO
		}
		return false;
	}

	public int getDirection()
	{
		return transitionDirection;
	}

	public void setDirection(int direction)
	{
		transitionDirection = direction;
	}

	public boolean isFacing(int x, int y)
	{
		if (x != charX && y != charY)
			return false;
		
		int dx = (int) Math.signum(charX - x);
		int dy = (int) Math.signum(charY - y);

		return tdx[transitionDirection] == dx && tdy[transitionDirection] == dy;
	}

	protected abstract BufferedImage getFrame(GameData data);

	public abstract String getTrigger();

	public abstract int getTransitionTime();

	public abstract void getAttention(int d);

	public abstract void addData(GameData gameData);

	public abstract void reset();
}
