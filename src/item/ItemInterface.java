package item;

import battle.effect.InvokeEffect;

public interface ItemInterface extends InvokeEffect {
    String getName();
    ItemNamesies namesies();

    default String getImageName() {
        return this.getClass().getSimpleName().toLowerCase();
    }

    @Override
    default InvokeSource getSource() {
        return InvokeSource.ITEM;
    }
}
