package map.entity;

import draw.TileUtils;
import gui.view.map.MapView;
import map.Direction;
import map.MapData;
import map.condition.Condition;
import map.condition.ConditionSet;
import map.triggers.Trigger;
import util.Point;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

public abstract class Entity {
    private final String entityName;
    private final ConditionSet condition;

    private Point location;
    private boolean visible;

    protected Entity(Point location, String entityName, Condition condition) {
        this.location = location;

        this.entityName = entityName;
        this.condition = new ConditionSet(condition);
    }

    public abstract Trigger getTrigger();

    public boolean isHighPriorityEntity() {
        return true;
    }

    public Point getLocation() {
        return this.location;
    }

    protected void setLocation(Point newLocation) {
        this.location = newLocation;
    }

    private boolean isPresent() {
        return this.condition.evaluate();
    }

    public boolean isPassable() {
        return !this.isActive();
    }

    public boolean isActive() {
        return this.visible;
    }

    public boolean setVisible() {
        this.visible = this.isPresent();
        return this.visible;
    }

    protected Condition getCondition() {
        return this.condition.getCondition();
    }

    public final void draw(Graphics g, Point drawLocation, boolean drawOnlyInTransition) {
        if (drawOnlyInTransition && !this.isTransitioning()) {
            return;
        }

        BufferedImage image = this.getFrame();
        if (image != null) {
            TileUtils.drawTileImage(g, image, this.getCanvasCoordinates(drawLocation));
        }
    }

    protected Point getCanvasCoordinates(Point drawLocation) {
        return TileUtils.getDrawLocation(this.location, drawLocation);
    }

    public void update(int dt, MapData currentMap, MapView view) {}

    public void getAttention(Direction direction) {}

    public void reset() {}

    protected BufferedImage getFrame() {
        return null;
    }

    protected boolean isTransitioning() {
        return false;
    }

    public String getEntityName() {
        return this.entityName;
    }
}
