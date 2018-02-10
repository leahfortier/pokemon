package generator;

import battle.attack.AttackNamesies;
import battle.effect.generic.EffectNamesies;
import item.ItemNamesies;
import pokemon.PokemonNamesies;
import pokemon.ability.AbilityNamesies;
import util.Folder;

public enum NamesiesType {
    ATTACK_NAMESIES(AttackNamesies.class, Folder.ATTACK, true),
    EFFECT_NAMESIES(EffectNamesies.class, Folder.GENERIC_EFFECT, false),
    ABILITY_NAMESIES(AbilityNamesies.class, Folder.ABILITY, true),
    ITEM_NAMESIES(ItemNamesies.class, Folder.ITEMS, true),
    POKEMON_NAMESIES(PokemonNamesies.class, Folder.POKEMON, true);

    private final Class<? extends Enum> namesiesClass;
    private final String folder;
    private final boolean includeName;

    NamesiesType(Class<? extends Enum> namesiesClass, String folder, boolean includeName) {
        this.namesiesClass = namesiesClass;
        this.includeName = includeName;
        this.folder = folder;
    }

    public String getFileName() {
        return this.folder + this.namesiesClass.getSimpleName() + ".java";
    }

    public boolean includeName() {
        return this.includeName;
    }
}
