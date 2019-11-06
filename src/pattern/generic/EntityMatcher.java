package pattern.generic;

import map.entity.Entity;
import util.Point;

import java.util.List;
import java.util.stream.Collectors;

public interface EntityMatcher {
    List<Entity> createEntities();

    interface SingleEntityMatcher extends EntityMatcher {
        Entity createEntity();

        @Override
        default List<Entity> createEntities() {
            return List.of(this.createEntity());
        }
    }

    interface MultiEntityMatcher extends EntityMatcher {
        List<Point> getLocation();
        Entity createEntity(Point location);

        @Override
        default List<Entity> createEntities() {
            return this.getLocation()
                       .stream()
                       .map(this::createEntity)
                       .collect(Collectors.toList());
        }
    }
}
