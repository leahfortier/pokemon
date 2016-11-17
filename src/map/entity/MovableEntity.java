package map.entity;

import gui.TileSet;
import gui.view.MapView;
import main.Game;
import main.Global;
import map.Direction;
import map.MapData;
import util.InputControl;
import util.Point;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public abstract class MovableEntity extends Entity {
	protected Direction transitionDirection;
	protected int transitionTime;
	protected int runFrame;
	
	protected int spriteIndex;
	
	public MovableEntity(Point location, String triggerName, String condition, int spriteIndex, Direction startDirection) {
		super(location, triggerName, condition);
		
		this.transitionDirection = startDirection;
		
		this.transitionTime = 0;
		this.runFrame = 0;
		
		this.spriteIndex = spriteIndex;
	}
	
	public void draw(Graphics g, float drawX, float drawY, boolean drawOnlyInTransition) {
		if (drawOnlyInTransition && transitionTime == 0) {
			return;
		}
		
		Point canvasCoordinates = getCanvasCoordinates(drawX, drawY);
		
		if (transitionTime != 0) {
			// TODO: util
			int len = Global.TILE_SIZE*(getTransitionTime() - transitionTime)/getTransitionTime();
			
			canvasCoordinates.x -= transitionDirection.dx * len;
			canvasCoordinates.y -= transitionDirection.dy * len;
			
			// System.out.println(transitionTime + " " +len + " " +cx + " " + cy);
		}
		
		super.draw(g, canvasCoordinates);
	}
	
	public void update(int dt, Entity[][] entity, MapData map, InputControl input, MapView view) {
		if (transitionTime != 0) {
			transitionTime += dt;	
		}
		
		if (transitionTime > getTransitionTime()) {
			transitionTime = 0;
			runFrame = (runFrame + 1)%2;
		}
	}
	
	public Direction getDirection() {
		return transitionDirection;
	}

	public void setDirection(Direction direction) {
		transitionDirection = direction;
	}

	public boolean isFacing(int x, int y) {
		if (x != getX() && y != getY()) {
			return false;
		}

		// TODO: Make point method for this
		int dx = (int) Math.signum(x - getX());
		int dy = (int) Math.signum(y - getY());

		return transitionDirection.dx == dx && transitionDirection.dy == dy;
	}
	
	protected BufferedImage getFrame() {
		TileSet trainerTiles = Game.getData().getTrainerTiles();
		if (transitionTime > 0) {
			// TODO: method
			return trainerTiles.getTile(12 * spriteIndex + 1 + transitionDirection.ordinal() + 4 * (1 + runFrame));
		}

		// TODO: These two should be combined
		return trainerTiles.getTile(12 * spriteIndex + 1 + transitionDirection.ordinal());
	}
	
	public abstract int getTransitionTime();
}
