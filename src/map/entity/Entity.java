package map.entity;

import gui.view.MapView;
import main.Global;
import map.Direction;
import map.MapData;
import map.MapData.WalkType;
import map.triggers.TriggerType;
import util.InputControl;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

public abstract class Entity {
	protected int charX;
	protected int charY;

	public Entity(int x, int y) {
		charX = x;
		charY = y;
	}
	
	public int getX() {
		return this.charX;
	}
	
	public int getY() {
		return this.charY;
	}
	
	// Takes in the draw coordinates and returns the location of the entity where to draw it relative to the canvas
	public Point getCanvasCoordinates(float drawX, float drawY) {
		int cx = (int) drawX + Global.TILE_SIZE * charX;
		int cy = (int) drawY + Global.TILE_SIZE * charY;
		
		return new Point(cx, cy);
	}

	public void draw(Graphics g, float drawX, float drawY, boolean drawOnlyInTransition) {
		draw(g, getCanvasCoordinates(drawX, drawY));
	}
	
	public void draw(Graphics g, Point canvasCoordinates) {
		int cx = canvasCoordinates.x;
		int cy = canvasCoordinates.y;
		
		BufferedImage img = getFrame();
		//TODO: metrics class?
		g.drawImage(img, cx - img.getWidth() / 2 + Global.TILE_SIZE / 2, cy + (Global.TILE_SIZE - img.getHeight()) - (Global.TILE_SIZE / 2), null);
	}

	public abstract void update(int dt, Entity[][] entity, MapData map, InputControl input, MapView view);

	protected boolean isPassable(WalkType type) {
		return type == WalkType.WALKABLE;
	}

	protected boolean isPassable(WalkType type, Direction direction) {
		// TODO: I'm probobbly gonna want to generalize this at some point...
		switch (type) {
			case HOP_RIGHT:
				return direction == Direction.RIGHT;
			case HOP_UP:
				return direction == Direction.UP;
			case HOP_LEFT:
				return direction == Direction.LEFT;
			case HOP_DOWN:
				return direction == Direction.DOWN;
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

	protected abstract BufferedImage getFrame();

	public abstract void getAttention(Direction direction);
	public abstract void addData();
	public abstract void reset();

	public abstract String getTriggerSuffix();

	public String getTriggerName() {
		return TriggerType.GROUP.getTriggerNameFromSuffix(this.getTriggerSuffix());
	}
}
