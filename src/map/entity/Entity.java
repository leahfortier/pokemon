package map.entity;

import gui.view.MapView;
import map.Condition;
import map.Direction;
import map.MapData;
import map.triggers.TriggerType;
import util.DrawUtils;
import util.Point;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public abstract class Entity {

	private final String entityName;
	private final Condition condition;

	private Point location;

	protected Entity(Point location, String entityName, String condition) {
		this.location = location;

		this.entityName = entityName;
		this.condition = new Condition(condition);
	}

	public Point getLocation() {
		return this.location;
	}

	protected void setLocation(Point newLocation) {
		this.location = newLocation;
	}

	public boolean isPresent() {
		return this.condition.isTrue();
	}

	public final void draw(Graphics g, Point drawLocation, boolean drawOnlyInTransition) {
		if (drawOnlyInTransition && !this.isTransitioning()) {
			return;
		}

		DrawUtils.drawEntityTileImage(g, this.getFrame(), this.getCanvasCoordinates(drawLocation));
	}

	protected Point getCanvasCoordinates(Point drawLocation) {
		return DrawUtils.getDrawLocation(this.location, drawLocation);
	}

	public abstract void update(int dt, MapData currentMap, MapView view);

	protected abstract BufferedImage getFrame();
	protected abstract boolean isTransitioning();

	public abstract void getAttention(Direction direction);
	public abstract void addData();
	public abstract void reset();

	protected String getEntityName() {
		return this.entityName;
	}

	public String getTriggerSuffix() {
		return this.getEntityName();
	}

	public String getTriggerName() {
		return TriggerType.GROUP.getTriggerNameFromSuffix(this.getTriggerSuffix());
	}
}
