package generator;

import battle.attack.Attack;
import battle.effect.generic.battle.BattleEffect;
import battle.effect.generic.battle.StandardBattleEffectNamesies;
import battle.effect.generic.battle.terrain.TerrainEffect;
import battle.effect.generic.battle.weather.WeatherEffect;
import battle.effect.generic.pokemon.PokemonEffect;
import battle.effect.generic.team.TeamEffect;
import item.Item;
import pokemon.ability.Ability;
import util.Folder;

public enum GeneratorType {
    ATTACK_GEN("Moves.txt", Folder.ATTACK, Attack.class, NamesiesType.ATTACK_NAMESIES),
    POKEMON_EFFECT_GEN("PokemonEffects.txt", Folder.POKEMON_EFFECT, PokemonEffect.class, NamesiesType.POKEMON_EFFECT_NAMESIES),
    TEAM_EFFECT_GEN("TeamEffects.txt", Folder.TEAM_EFFECT, TeamEffect.class, NamesiesType.TEAM_EFFECT_NAMESIES),
    BATTLE_EFFECT_GEN("BattleEffects.txt", Folder.BATTLE_EFFECT, new ClassNameCreator(BattleEffect.class, StandardBattleEffectNamesies.class), NamesiesType.BATTLE_EFFECT_NAMESIES),
    WEATHER_GEN("Weather.txt", Folder.WEATHER_EFFECT, WeatherEffect.class, NamesiesType.WEATHER_NAMESIES),
    TERRAIN_GEN("Terrain.txt", Folder.TERRAIN_EFFECT, TerrainEffect.class, NamesiesType.TERRAIN_NAMESIES),
    ABILITY_GEN("Abilities.txt", Folder.ABILITY, Ability.class, NamesiesType.ABILITY_NAMESIES),
    ITEM_GEN("Items.txt", Folder.ITEMS, Item.class, NamesiesType.ITEM_NAMESIES);

    private final String inputPath;
    private final String outputPath;
    private final String superClassName;
    private final NamesiesType namesiesType;

    GeneratorType(String inputFileName, String outputFolder, Class<?> superClass, NamesiesType namesiesType) {
        this(inputFileName, outputFolder, new ClassNameCreator(superClass), namesiesType);
    }

    GeneratorType(String inputFileName, String outputFolder, ClassNameCreator classNameCreator, NamesiesType namesiesType) {
        this.inputPath = Folder.GENERATOR + inputFileName;
        this.outputPath = outputFolder + classNameCreator.getBaseClassName() + ".java";
        this.superClassName = classNameCreator.getFullClassName();
        this.namesiesType = namesiesType;
    }

    public String getInputPath() {
        return this.inputPath;
    }

    public String getOutputPath() {
        return this.outputPath;
    }

    public String getSuperClassName() {
        return this.superClassName;
    }

    public NamesiesType getNamesiesType() {
        return this.namesiesType;
    }
}
