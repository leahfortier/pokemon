package map.entity.movable;

import gui.view.map.MapView;
import main.Game;
import main.Global;
import map.Direction;
import map.MapData;
import map.PathDirection;
import map.WalkType;
import map.entity.Entity;
import util.Point;
import util.StringUtils;

import java.awt.image.BufferedImage;

public abstract class MovableEntity extends Entity {
	protected final int spriteIndex;

	private int runFrame;
	protected int transitionTime;
	private int waitTime;

	private String tempPath;
	private int pathIndex;
	private EndPathListener endPathListener;

	MovableEntity(Point location, String triggerName, String condition, int spriteIndex) {
		super(location, triggerName, condition);

		this.transitionTime = 0;
		this.runFrame = 0;

		this.spriteIndex = spriteIndex;
	}

	public abstract int getTransitionTime();

	protected abstract boolean hasAttention();
	protected abstract String getPath();
	protected abstract void endPath();

	public abstract Direction getDirection();
	protected abstract void setDirection(Direction direction);

	public void setTempPath(String path, EndPathListener listener) {
		this.tempPath = path;
		this.pathIndex = 0;
		this.endPathListener = listener;
	}

	protected boolean hasTempPath() {
		return !StringUtils.isNullOrEmpty(this.tempPath);
	}

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

		// Decrease wait time
		waitTime = Math.max(0, waitTime - dt);

		// Not transitioning, not waiting, and does not have attention
		if (!this.isTransitioning() && waitTime == 0 && !hasAttention()) {

			String path = this.tempPath;
			if (tempPath == null) {
				path = this.getPath();
			} else if (endedTempPath) {
				if (this.endPathListener != null) {
					this.endPathListener.endPathCallback();
				}

				tempPath = null;
				endedTempPath = false;
				endPath();
				return;
			}

			if (!StringUtils.isNullOrEmpty(path)) {

				// Find the direction that corresponds to the character
				PathDirection direction = PathDirection.getDirection(path.charAt(pathIndex));
				if (direction == PathDirection.WAIT) {
					waitTime = getTransitionTime();
					pathIndex++;
				}
				else {
					Point newLocation = Point.add(this.getLocation(), direction.getDeltaPoint());
					if (currentMap.getPassValue(newLocation).isPassable(direction.getDirection()) && !currentMap.hasEntity(newLocation)) {
						setLocation(newLocation);

						transitionTime = 1;
						waitTime = 5*Global.TIME_BETWEEN_TILES/4; // TODO: Why 5/4
						pathIndex++;
					}

					this.setDirection(direction.getDirection());
				}

				pathIndex %= path.length();
				if (pathIndex == 0 && tempPath != null) {
					endedTempPath = true;
					/*if (this.endPathListener != null) {
						this.endPathListener.endPathCallback();
					}

					tempPath = null;
					endPath();*/
				}
			}
		}
	}

	boolean endedTempPath;

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

	@Override
	public void reset() {
		waitTime = 0;
		pathIndex = 0;
		tempPath = null;
	}

	public static int getTrainerSpriteIndex(int spriteIndex, Direction direction) {
		return 12*spriteIndex + 1 + direction.ordinal();
	}

	public Point getNewLocation(Point location, Direction direction, MapData currentMap) {
		Point newLocation = Point.add(location, direction.getDeltaPoint());

		WalkType passValue = currentMap.getPassValue(newLocation);
		if (passValue.isPassable(direction) && !currentMap.hasEntity(newLocation)) {
			return newLocation;
		}

		return null;
	}
}
