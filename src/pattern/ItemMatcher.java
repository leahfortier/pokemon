package pattern;

import mapMaker.model.TriggerModel.TriggerModelType;
import namesies.ItemNamesies;
import util.Point;

import java.util.ArrayList;
import java.util.List;

public class ItemMatcher extends MapMakerEntityMatcher {
    private int x;
    private int y;
    private String item;

    private transient ItemNamesies itemNamesies;

    private transient List<Point> location;

    public ItemMatcher(ItemNamesies itemName) {
        this.item = itemName.getName();
        this.itemNamesies = itemName;
    }

    @Override
    public List<Point> getLocation() {
        if (this.location != null) {
            return this.location;
        }

        this.location = new ArrayList<>();
        this.location.add(new Point(x, y));
        return this.location;
    }

    @Override
    public void addPoint(Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    @Override
    public TriggerModelType getTriggerModelType() {
        return TriggerModelType.ITEM;
    }

    @Override
    public String getBasicName() {
        return item;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public ItemNamesies getItem() {
        if (this.itemNamesies != null) {
            return this.itemNamesies;
        }

        return this.itemNamesies = ItemNamesies.getValueOf(this.item);
    }
}
