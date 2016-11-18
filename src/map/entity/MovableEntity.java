package map.entity;

import gui.TileSet;
import gui.view.MapView;
import main.Game;
import main.Global;
import map.Direction;
import map.MapData;
import util.InputControl;
import util.Point;

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

	@Override
	protected Point getCanvasCoordinates(Point drawLocation) {
		Point canvasCoordinates = super.getCanvasCoordinates(drawLocation);

		if (this.isTransitioning()) {
			// TODO: Should this be a method?
			int length = Global.TILE_SIZE*(getTransitionTime() - transitionTime)/getTransitionTime();

			canvasCoordinates = Point.subtract(
					canvasCoordinates,
					Point.scale(transitionDirection.getDeltaPoint(), length)
			);
		}

		return canvasCoordinates;
	}

	@Override
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

	@Override
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

	@Override
	protected boolean isTransitioning() {
		return this.transitionTime > 0;
	}
}
