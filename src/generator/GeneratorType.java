package generator;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.effect.generic.BattleEffect;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.PokemonEffect;
import battle.effect.generic.TeamEffect;
import battle.effect.generic.Weather;
import item.Item;
import item.ItemNamesies;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import util.Folder;

public enum GeneratorType {
    ATTACK_GEN("Moves.txt", Folder.ATTACK, Attack.class, AttackNamesies.class),
    POKEMON_EFFECT_GEN("PokemonEffects.txt", Folder.GENERIC_EFFECT, PokemonEffect.class, EffectNamesies.class),
    TEAM_EFFECT_GEN("TeamEffects.txt", Folder.GENERIC_EFFECT, TeamEffect.class, EffectNamesies.class),
    BATTLE_EFFECT_GEN("BattleEffects.txt", Folder.GENERIC_EFFECT, BattleEffect.class, EffectNamesies.class),
    WEATHER_GEN("Weather.txt", Folder.GENERIC_EFFECT, Weather.class, EffectNamesies.class),
    ABILITY_GEN("Abilities.txt", Folder.ABILITY, Ability.class, AbilityNamesies.class),
    ITEM_GEN("Items.txt", Folder.ITEMS, Item.class, ItemNamesies.class);

    private final String inputPath;
    private final String outputPath;
    private final String outputFolder;
    private final String superClassName;
    private final Class namesiesClass;

    GeneratorType(String inputFileName, String outputFolder, Class superClass, Class namesiesClass) {
        this.inputPath = Folder.GENERATOR + inputFileName;
        this.outputPath = outputFolder + superClass.getSimpleName() + ".java";
        this.outputFolder = outputFolder;
        this.superClassName = superClass.getSimpleName();
        this.namesiesClass = namesiesClass;
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

    public Class getNamesiesClass() {
        return this.namesiesClass;
    }
}
