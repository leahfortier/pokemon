package item.use;

import battle.ActivePokemon;
import item.hold.HoldItem;
import message.Messages;
import pokemon.active.Nature;

public interface NatureMint extends PokemonUseItem, HoldItem {
    Nature getNature();

    @Override
    default boolean use(ActivePokemon p) {
        // Cannot change to a nature you already have
        Nature nature = this.getNature();
        if (p.getNature() == nature) {
            return false;
        }

        // Brand new attitude!
        p.setNature(nature);
        Messages.add(p.getName() + "'s nature was changed to " + nature.getName() + "!");
        return true;
    }

    @Override
    default int flingDamage() {
        return 10;
    }

    @Override
    default String getImageName() {
        // Ex: neutralmint or attackmint or spdefensemint
        Nature nature = this.getNature();
        return (nature.isNeutral() ? "neutral" : nature.getBeneficial().name().replace("_", "").toLowerCase()) + "mint";
    }
}
