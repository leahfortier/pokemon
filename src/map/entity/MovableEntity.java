package map.entity;

import gui.view.map.MapView;
import main.Game;
import main.Global;
import map.Direction;
import map.MapData;
import util.Point;

import java.awt.image.BufferedImage;

public abstract class MovableEntity extends Entity {
	protected int transitionTime;
	private int runFrame;
	
	protected int spriteIndex;
	
	MovableEntity(Point location, String triggerName, String condition, int spriteIndex) {
		super(location, triggerName, condition);
		
		this.transitionTime = 0;
		this.runFrame = 0;
		
		this.spriteIndex = spriteIndex;
	}

	public abstract int getTransitionTime();
	public abstract Direction getDirection();
	protected abstract void setDirection(Direction direction);

	@Override
	protected Point getCanvasCoordinates(Point drawLocation) {
		Point canvasCoordinates = super.getCanvasCoordinates(drawLocation);

		if (this.isTransitioning()) {
			// TODO: Should this be a method?
			int length = Global.TILE_SIZE*(getTransitionTime() - transitionTime)/getTransitionTime();

			canvasCoordinates = Point.subtract(
					canvasCoordinates,
					Point.scale(getDirection().getDeltaPoint(), length)
			);
		}

		return canvasCoordinates;
	}

	@Override
	public void update(int dt, MapData currentMap, MapView view) {
		if (transitionTime != 0) {
			transitionTime += dt;	
		}
		
		if (transitionTime > getTransitionTime()) {
			transitionTime = 0;
			runFrame = (runFrame + 1)%2;
		}
	}

	public boolean isFacing(Point otherLocation) {

		// Not in the same row or the same column
		if (!this.getLocation().partiallyEquals(otherLocation)) {
			return false;
		}

		// Get the direction that would be facing the other location
		Point deltaDirection = Point.getDeltaDirection(otherLocation, this.getLocation());

		// Check if these are the same direction
		return this.getDirection().getDeltaPoint().equals(deltaDirection);
	}

	@Override
	protected BufferedImage getFrame() {
		int trainerSpriteIndex = getTrainerSpriteIndex(spriteIndex, this.getDirection());
		if (transitionTime > 0) {
			trainerSpriteIndex += 4*(1 + runFrame);
		}

		return Game.getData().getTrainerTiles().getTile(trainerSpriteIndex);
	}

	@Override
	protected boolean isTransitioning() {
		return this.transitionTime > 0;
	}

	public static int getTrainerSpriteIndex(int spriteIndex, Direction direction) {
		return 12*spriteIndex + 1 + direction.ordinal();
	}
}
