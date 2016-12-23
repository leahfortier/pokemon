package item.hold;

import battle.effect.generic.EffectInterfaces.PowerChangeEffect;
import main.Type;

public interface SpecialTypeItem extends HoldItem {
    Type getType();

    interface DriveItem extends SpecialTypeItem {}
    interface MemoryItem extends SpecialTypeItem {}
    interface PlateItem extends SpecialTypeItem, PowerChangeEffect {}
    interface GemItem extends SpecialTypeItem, ConsumableItem, PowerChangeEffect {}
}
