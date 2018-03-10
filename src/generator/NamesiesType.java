package generator;

import battle.attack.AttackNamesies;
import battle.effect.generic.battle.StandardBattleEffectNamesies;
import battle.effect.generic.battle.terrain.TerrainNamesies;
import battle.effect.generic.battle.weather.WeatherNamesies;
import battle.effect.generic.pokemon.PokemonEffectNamesies;
import battle.effect.generic.team.TeamEffectNamesies;
import item.ItemNamesies;
import pokemon.PokemonNamesies;
import pokemon.ability.AbilityNamesies;
import util.file.Folder;

public enum NamesiesType {
    ATTACK_NAMESIES(AttackNamesies.class, Folder.ATTACK, true),
    POKEMON_EFFECT_NAMESIES(PokemonEffectNamesies.class, Folder.POKEMON_EFFECT, false),
    TEAM_EFFECT_NAMESIES(TeamEffectNamesies.class, Folder.TEAM_EFFECT, false),
    BATTLE_EFFECT_NAMESIES(StandardBattleEffectNamesies.class, Folder.BATTLE_EFFECT, false),
    WEATHER_NAMESIES(WeatherNamesies.class, Folder.WEATHER_EFFECT, false),
    TERRAIN_NAMESIES(TerrainNamesies.class, Folder.TERRAIN_EFFECT, false),
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
