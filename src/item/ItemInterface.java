package item;

import battle.effect.holder.ItemHolder;

public interface ItemInterface extends ItemHolder {
    String getName();

    default String getImageName() {
        return this.getClass().getSimpleName().toLowerCase();
    }
}
