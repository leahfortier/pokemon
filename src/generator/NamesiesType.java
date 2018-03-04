package generator;

import battle.attack.AttackNamesies;
import battle.effect.generic.BattleEffectNamesies;
import battle.effect.generic.PokemonEffectNamesies;
import battle.effect.generic.TeamEffectNamesies;
import battle.effect.generic.TerrainNamesies;
import battle.effect.generic.WeatherNamesies;
import item.ItemNamesies;
import pokemon.PokemonNamesies;
import pokemon.ability.AbilityNamesies;
import util.Folder;

public enum NamesiesType {
    ATTACK_NAMESIES(AttackNamesies.class, Folder.ATTACK, true),
    POKEMON_EFFECT_NAMESIES(PokemonEffectNamesies.class, Folder.GENERIC_EFFECT, false),
    TEAM_EFFECT_NAMESIES(TeamEffectNamesies.class, Folder.GENERIC_EFFECT, false),
    BATTLE_EFFECT_NAMESIES(BattleEffectNamesies.class, Folder.GENERIC_EFFECT, false),
    WEATHER_NAMESIES(WeatherNamesies.class, Folder.GENERIC_EFFECT, false),
    TERRAIN_NAMESIES(TerrainNamesies.class, Folder.GENERIC_EFFECT, false),
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
