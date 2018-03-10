package message;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Move;
import battle.effect.generic.battle.weather.WeatherEffect;
import gui.view.ViewMode;
import main.Game;
import map.overworld.TerrainType;
import map.triggers.Trigger;
import pattern.action.ChoiceMatcher;
import pokemon.Gender;
import pokemon.PokemonNamesies;
import sound.SoundTitle;
import type.PokeType;
import util.serialization.Serializable;
import util.string.StringUtils;

public class MessageUpdate {
    private static final String PLAYER_NAME = "{playerName}";

    private final String message;
    private int[] statGains;
    private int[] newStats;
    private PokemonNamesies pokemon;
    private boolean shiny;
    private boolean animation;
    private PokeType type;
    private boolean isPlayer; // SO YOU KNOW WHO TO GIVE THE HP/STATUS UPDATE TO
    private boolean switchPokemon;
    private Float expRatio;
    private MessageUpdateType updateType;
    private boolean levelUp;
    private String name;
    private Gender gender;
    private ActivePokemon moveLearner;
    private String frontPokemonSerialized;
    private Integer teamIndex;
    private Move move;
    private Integer duration;
    private Trigger trigger;
    private ChoiceMatcher[] choices;
    private ViewMode viewMode;
    private WeatherEffect weather;
    private TerrainType terrain;
    private SoundTitle soundEffect;
    private String imageName;

    public MessageUpdate(String message) {
        this.message = message.replace(PLAYER_NAME, Game.getPlayer().getName());
        this.updateType = MessageUpdateType.NO_UPDATE;
    }

    public MessageUpdate() {
        this(StringUtils.empty());
    }

    // Show stat gains
    public MessageUpdate withStatGains(int[] gains, int[] stats) {
        statGains = gains;
        newStats = stats;
        updateType = MessageUpdateType.STAT_GAIN;
        isPlayer = true;
        return this;
    }

    public MessageUpdate withPokemon(ActivePokemon frontPokemon) {
        this.frontPokemonSerialized = frontPokemon.serialize();
        this.isPlayer = frontPokemon.isPlayer();
        return this;
    }

    public MessageUpdate withFrontPokemon(Battle b, ActivePokemon frontPokemon) {
        this.teamIndex = b.getTrainer(frontPokemon).getTeamIndex(frontPokemon);
        return this.withPokemon(frontPokemon);
    }

    public MessageUpdate withSoundEffect(SoundTitle soundEffect) {
        this.soundEffect = soundEffect;
        return this;
    }

    public MessageUpdate withImageName(String imageName, boolean isPlayer) {
        this.imageName = imageName;
        this.animation = true;
        this.isPlayer = isPlayer;
        return this;
    }

    // Updates all current display information of the given pokemon
    // Hp, status condition, type, name, and gender
    public MessageUpdate updatePokemon(Battle b, ActivePokemon pokemon) {
        if (b == null) {
            return this;
        }

        boolean isPlayer = pokemon.isPlayer();
        return this.withType(pokemon.getDisplayType(b), isPlayer)
                   .withNameChange(pokemon.getName(), isPlayer)
                   .withGender(pokemon.getGender(), isPlayer)
                   .withFrontPokemon(b, pokemon);
    }

    // Pokemon Image Update!
    public MessageUpdate withNewPokemon(PokemonNamesies pokemon, boolean shiny, boolean animation, boolean isPlayer) {
        this.pokemon = pokemon;
        this.isPlayer = isPlayer;
        this.shiny = shiny;
        this.animation = animation;
        return this;
    }

    // Type Update!
    public MessageUpdate withType(PokeType typesies, boolean isPlayer) {
        this.type = typesies;
        this.isPlayer = isPlayer;
        return this;
    }

    // Switch update!
    public MessageUpdate withSwitch(Battle battle, ActivePokemon active) {
        this.switchPokemon = true;
        this.type = active.getDisplayType(battle);
        this.shiny = active.isShiny();
        this.pokemon = active.namesies();
        this.name = active.getName();
        this.gender = active.getGender();
        this.animation = false;
        return this.withFrontPokemon(battle, active);
    }

    public MessageUpdate withWeather(WeatherEffect weather) {
        this.weather = weather;
        return this;
    }

    public MessageUpdate withTerrain(TerrainType terrain) {
        this.terrain = terrain;
        return this;
    }

    // Special type of update
    public MessageUpdate withUpdate(MessageUpdateType update) {
        this.updateType = update;
        return this;
    }

    public MessageUpdate withTrigger(Trigger trigger) {
        this.trigger = trigger;
        this.updateType = MessageUpdateType.TRIGGER;

        return this;
    }

    public MessageUpdate withChoices(ChoiceMatcher[] choices) {
        this.choices = choices;
        return this;
    }

    // EXP Gain update
    public MessageUpdate withExpGain(Battle battle, ActivePokemon gainer, float ratio, boolean levelUp) {
        this.updatePokemon(battle, gainer);

        this.isPlayer = true;
        this.expRatio = ratio;
        this.levelUp = levelUp;

        return this;
    }

    // Name change update
    public MessageUpdate withNameChange(String name, boolean isPlayer) {
        this.name = name;
        this.isPlayer = isPlayer;

        return this;
    }

    // Gender change update
    public MessageUpdate withGender(Gender gender, boolean isPlayer) {
        this.gender = gender;
        this.isPlayer = isPlayer;

        return this;
    }

    // Learn new move update
    public MessageUpdate withLearnMove(ActivePokemon active, Move newMove) {
        this.moveLearner = active;
        this.move = newMove;
        this.updateType = MessageUpdateType.LEARN_MOVE;

        return this;
    }

    // Catching a Pokemon
    public MessageUpdate withCatchPokemon(int duration) {
        this.duration = duration;
        return this;
    }

    public MessageUpdate withViewChange(ViewMode viewMode) {
        this.viewMode = viewMode;
        return this;
    }

    public boolean isViewChange() {
        return this.viewMode != null;
    }

    public ViewMode getViewMode() {
        return this.viewMode;
    }

    public String getMessage() {
        return message;
    }

    public boolean isPlayer() {
        return isPlayer;
    }

    public boolean gainUpdate() {
        return statGains != null;
    }

    public int[] getGain() {
        return statGains;
    }

    public int[] getNewStats() {
        return newStats;
    }

    public boolean pokemonUpdate() {
        return pokemon != null;
    }

    public PokemonNamesies getPokemon() {
        return pokemon;
    }

    public boolean getShiny() {
        return shiny;
    }

    public boolean isAnimate() {
        return animation;
    }

    public boolean typeUpdate() {
        return type != null;
    }

    public PokeType getType() {
        return type;
    }

    public boolean switchUpdate() {
        return switchPokemon;
    }

    public boolean expUpdate() {
        return expRatio != null;
    }

    public float getEXPRatio() {
        return expRatio;
    }

    public boolean levelUpdate() {
        return levelUp;
    }

    public boolean nameUpdate() {
        return name != null;
    }

    public String getName() {
        return name;
    }

    public boolean genderUpdate() {
        return gender != null;
    }

    public Gender getGender() {
        return gender;
    }

    public boolean catchUpdate() {
        return duration != null;
    }

    public int getDuration() {
        return duration;
    }

    public ActivePokemon getMoveLearner() {
        return moveLearner;
    }

    public Move getMove() {
        return move;
    }

    public boolean hasUpdateType() {
        return updateType != MessageUpdateType.NO_UPDATE;
    }

    public MessageUpdateType getUpdateType() {
        return updateType;
    }

    public boolean learnMove() {
        return updateType == MessageUpdateType.LEARN_MOVE;
    }

    public boolean trigger() {
        return updateType == MessageUpdateType.TRIGGER;
    }

    public boolean resetState() {
        return updateType == MessageUpdateType.RESET_STATE;
    }

    public Trigger getTrigger() {
        return this.trigger;
    }

    public boolean isChoice() {
        return this.choices != null;
    }

    public ChoiceMatcher[] getChoices() {
        return this.choices;
    }

    public boolean frontPokemonUpdate() {
        return !StringUtils.isNullOrEmpty(this.frontPokemonSerialized);
    }

    public ActivePokemon getFrontPokemon() {
        return Serializable.deserialize(this.frontPokemonSerialized, ActivePokemon.class);
    }

    public int getTeamIndex() {
        return this.teamIndex;
    }

    public boolean weatherUpdate() {
        return this.weather != null;
    }

    public WeatherEffect getWeather() {
        return this.weather;
    }

    public boolean terrainUpdate() {
        return this.terrain != null;
    }

    public TerrainType getTerrain() {
        return this.terrain;
    }

    public boolean soundEffectUpdate() {
        return this.soundEffect != null;
    }

    public SoundTitle getSoundEffect() {
        return this.soundEffect;
    }

    public boolean imageUpdate() {
        return !StringUtils.isNullOrEmpty(this.imageName);
    }

    public String getImageName() {
        return this.imageName;
    }
}
