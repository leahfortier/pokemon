package item.medicine;

import battle.ActivePokemon;
import battle.attack.Move;
import battle.effect.source.CastSource;
import item.hold.HoldItem;
import item.use.MoveUseItem;
import main.Global;
import message.Messages;

public interface PPHealer extends MoveUseItem, HoldItem {
    int restoreAmount(ActivePokemon restorer, Move toRestore);

    // TODO: Need to be able to call these from the battle! (BattleMoveUse? yuck) -- Test messages once completed
    @Override
    default boolean use(ActivePokemon p, Move m) {
        return this.use(p, m, CastSource.USE_ITEM);
    }

    default boolean use(ActivePokemon p, Move m, CastSource source) {
        if (m.increasePP(this.restoreAmount(p, m))) {
            switch (source) {
                case USE_ITEM:
                    Messages.add(p.getName() + "'s PP for " + m.getAttack().getName() + " was restored!");
                    break;
                case HELD_ITEM:
                    Messages.add(p.getName() + "'s " + this.getName() + " restored " + m.getAttack().getName() + "'s PP!");
                    break;
                default:
                    Global.error("Invalid source " + source);
                    break;
            }

            return true;
        }

        return false;
    }
}
