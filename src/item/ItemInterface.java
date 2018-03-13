package item;

import battle.effect.InvokeEffect;
import util.serialization.Serializable;

public interface ItemInterface extends InvokeEffect, Serializable {
    String getName();
    ItemNamesies namesies();

    default String getImageName() {
        return this.getClass().getSimpleName().toLowerCase();
    }
}
