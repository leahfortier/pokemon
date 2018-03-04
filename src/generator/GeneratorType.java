package generator;

import battle.attack.Attack;
import battle.effect.generic.BattleEffect;
import battle.effect.generic.PokemonEffect;
import battle.effect.generic.TeamEffect;
import battle.effect.generic.Terrain;
import battle.effect.generic.Weather;
import item.Item;
import pokemon.ability.Ability;
import util.Folder;

public enum GeneratorType {
    ATTACK_GEN("Moves.txt", Folder.ATTACK, Attack.class, NamesiesType.ATTACK_NAMESIES),
    POKEMON_EFFECT_GEN("PokemonEffects.txt", Folder.GENERIC_EFFECT, PokemonEffect.class, NamesiesType.EFFECT_NAMESIES),
    TEAM_EFFECT_GEN("TeamEffects.txt", Folder.GENERIC_EFFECT, TeamEffect.class, NamesiesType.EFFECT_NAMESIES),
    BATTLE_EFFECT_GEN("BattleEffects.txt", Folder.GENERIC_EFFECT, BattleEffect.class, NamesiesType.EFFECT_NAMESIES),
    WEATHER_GEN("Weather.txt", Folder.GENERIC_EFFECT, Weather.class, NamesiesType.EFFECT_NAMESIES),
    TERRAIN_GEN("Terrain.txt", Folder.GENERIC_EFFECT, Terrain.class, NamesiesType.EFFECT_NAMESIES),
    ABILITY_GEN("Abilities.txt", Folder.ABILITY, Ability.class, NamesiesType.ABILITY_NAMESIES),
    ITEM_GEN("Items.txt", Folder.ITEMS, Item.class, NamesiesType.ITEM_NAMESIES);

    private final String inputPath;
    private final String outputPath;
    private final String outputFolder;
    private final String superClassName;
    private final NamesiesType namesiesType;

    GeneratorType(String inputFileName, String outputFolder, Class<?> superClass, NamesiesType namesiesType) {
        this.inputPath = Folder.GENERATOR + inputFileName;
        this.outputPath = outputFolder + superClass.getSimpleName() + ".java";
        this.outputFolder = outputFolder;
        this.superClassName = superClass.getSimpleName();
        this.namesiesType = namesiesType;
    }

    public String getInputPath() {
        return this.inputPath;
    }

    public String getOutputPath() {
        return this.outputPath;
    }

    public String getOutputFolder() {
        return this.outputFolder;
    }

    public String getSuperClassName() {
        return this.superClassName;
    }

    public NamesiesType getNamesiesType() {
        return this.namesiesType;
    }
}
