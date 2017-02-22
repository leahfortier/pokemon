package message;

import battle.Battle;
import battle.attack.Move;
import battle.effect.generic.Weather;
import battle.effect.status.StatusCondition;
import gui.view.ViewMode;
import gui.view.battle.BattleView;
import gui.view.battle.VisualState;
import main.Game;
import map.overworld.TerrainType;
import message.Messages.MessageState;
import pattern.action.ChoiceActionMatcher.ChoiceMatcher;
import pokemon.ActivePokemon;
import pokemon.Gender;
import pokemon.PokemonInfo;
import sound.SoundPlayer;
import sound.SoundTitle;
import type.Type;
import util.StringUtils;

public class MessageUpdate {
	private String message;
	private Integer hp;
	private Integer maxHP;
	private int[] statGains;
	private int[] newStats;
	private int[] stages;
	private StatusCondition status;
	private PokemonInfo pokemon;
	private boolean shiny;
	private boolean animation;
	private Type[] type;
	private boolean isPlayer; // SO YOU KNOW WHO TO GIVE THE HP/STATUS UPDATE TO
	private boolean switchPokemon;
	private Float expRatio;
	private Update updateType;
	private Integer level;
	private String name;
	private Gender gender;
	private ActivePokemon moveLearner;
	private ActivePokemon frontPokemon;
	private Move move;
	private Integer duration;
	private String triggerName;
	private ChoiceMatcher[] choices;
	private ViewMode viewMode;
	private Boolean showImage;
	private Weather weather;
	private TerrainType terrain;

	public enum Update {
		NO_UPDATE,
		TRIGGER,
		RESET_STATE,
		ENTER_BATTLE,
		ENTER_NAME,
		APPEND_TO_NAME,
		SHOW_POKEMON,
		PROMPT_SWITCH(VisualState.POKEMON),
		LEARN_MOVE(VisualState.LEARN_MOVE_QUESTION),
		STAT_GAIN(VisualState.STAT_GAIN),
		EXIT_BATTLE(battleView -> exitBattle(battleView, ViewMode.MAP_VIEW)),
		CATCH_POKEMON(battleView -> exitBattle(battleView, ViewMode.NEW_POKEMON_VIEW)),
		FORCE_SWITCH(battleView -> {
            battleView.setVisualState(VisualState.POKEMON);
            battleView.setSwitchForced();
            battleView.clearUpdate();
        }),
		WIN_BATTLE(battleView -> {
            if (battleView.getCurrentBattle().isWildBattle()) {
                SoundPlayer.soundPlayer.playMusic(SoundTitle.WILD_POKEMON_DEFEATED);
            }
            else {
                // TODO: Get trainer win music
                SoundPlayer.soundPlayer.playMusic(SoundTitle.TRAINER_DEFEATED);
            }
        });

		private final PerformUpdate performUpdate;
		
		Update() {
			this(battleView -> {});
		}
		
		Update(final VisualState visualState) {
			this(battleView -> {
                battleView.setVisualState(visualState);
                battleView.clearUpdate();
            });
		}
		
		Update(PerformUpdate performUpdate) {
			this.performUpdate = performUpdate;
		}
		
		public void performUpdate(BattleView battleView) {
			this.performUpdate.performUpdate(battleView);
		}
		
		private interface PerformUpdate {
			void performUpdate(BattleView battleView);
		}

		private static void exitBattle(BattleView battleView, ViewMode viewMode) {
			Game.instance().setViewMode(viewMode);
			battleView.clearUpdate();
			Messages.clearMessages(MessageState.FIGHTY_FIGHT);
			Messages.setMessageState(MessageState.MAPPITY_MAP);
			Game.getPlayer().getEntity().resetCurrentInteractionEntity();

			Game.getPlayer().checkEvolution();
		}
	}

	public MessageUpdate(String message) {
		this.message = message;
		this.updateType = Update.NO_UPDATE;
	}

	public MessageUpdate() {
		this(StringUtils.empty());
	}

	// YEAH THAT'S RIGHT HEALTH UPDATE
	public MessageUpdate withHp(int hp, boolean isPlayer) {
		this.hp = hp;
		this.isPlayer = isPlayer;
		return this;
	}

	// Update to maximum HP
	public MessageUpdate withMaxHp(int hp, int maxHp, boolean isPlayer) {
		this.maxHP = maxHp;
		return this.withHp(hp, isPlayer);
	}
	
	// Show stat gains
	public MessageUpdate withStatGains(int[] gains, int[] stats) {
		statGains = gains;
		newStats = stats;
		updateType = Update.STAT_GAIN;
		isPlayer = true;
		return this;
	}

	public MessageUpdate withStages(int[] stages, boolean isPlayer) {
		this.stages = stages;
		this.isPlayer = isPlayer;
		return this;
	}

	// OOOOHH SOMEONE'S GOT DAT STATUS CONDITION
	public MessageUpdate withStatusCondition(StatusCondition status, boolean isPlayer) {
		this.status = status;
		this.isPlayer = isPlayer;
		return this;
	}

	public MessageUpdate withShowImage(boolean showImage, boolean isPlayer) {
		this.showImage = showImage;
		this.isPlayer = isPlayer;
		return this;
	}

	public MessageUpdate withFrontPokemon(ActivePokemon frontPokemon, boolean isPlayer) {
		this.frontPokemon = frontPokemon;
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
		return this.withMaxHp(pokemon.getHP(), pokemon.getMaxHP(), isPlayer)
				.withStatusCondition(pokemon.getStatus().getType(), isPlayer)
				.withType(pokemon.getDisplayType(b), isPlayer)
				.withNameChange(pokemon.getName(), isPlayer)
				.withGender(pokemon.getGender(), isPlayer)
				.withStages(pokemon.getAttributes().getStages(), isPlayer)
				.withFrontPokemon(pokemon, isPlayer)
				.withShowImage(!pokemon.isSemiInvulnerable(), isPlayer);
	}
	
	// Pokemon image Update!
	public MessageUpdate withNewPokemon(PokemonInfo pokemon, boolean shiny, boolean animation, boolean isPlayer) {
		this.pokemon = pokemon;
		this.isPlayer = isPlayer;
		this.shiny = shiny;
		this.animation = animation;
		return this;
	}
	
	// Type Update!
	public MessageUpdate withType(Type[] typesies, boolean isPlayer) {
		this.type = typesies;
		this.isPlayer = isPlayer;
		return this;
	}
	
	// Switch update!
	public MessageUpdate withSwitch(Battle battle, ActivePokemon active) {
		this.isPlayer = active.isPlayer();
		this.switchPokemon = true;
		this.hp = active.getHP();
		this.status = active.getStatus().getType();
		this.type = active.getDisplayType(battle);
		this.shiny = active.isShiny();
		this.pokemon = active.getPokemonInfo();
		this.name = active.getName();
		this.maxHP = active.getMaxHP();
		this.level = active.getLevel();
		this.gender = active.getGender();
		this.expRatio = active.expRatio();
		this.stages = active.getAttributes().getStages();
		this.frontPokemon = active;
		this.showImage = true;
		this.animation = false;
		return this;
	}

	public MessageUpdate withWeather(Weather weather) {
		this.weather = weather;
		return this;
	}

	public MessageUpdate withTerrain(TerrainType terrain) {
		this.terrain = terrain;
		return this;
	}

	// Special type of update
	public MessageUpdate withUpdate(Update update) {
		this.updateType = update;
		return this;
	}

	public MessageUpdate withTrigger(String triggerName) {
		this.triggerName = triggerName;
		this.updateType = Update.TRIGGER;

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
		
		if (levelUp) {
			level = gainer.getLevel();
		}

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
		this.updateType = Update.LEARN_MOVE;

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
	
	public boolean healthUpdate() {
		return hp != null;
	}
	
	public int getHP() {
		return hp;
	}
	
	public boolean maxHealthUpdate() {
		return maxHP != null;
	}
	
	public int getMaxHP() {
		return maxHP;
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

	public boolean stageUpdate() {
		return this.stages != null;
	}

	public int[] getStages() {
		return this.stages;
	}

	public boolean statusUpdate() {
		return status != null;
	}
	
	public StatusCondition getStatus() {
		return status;
	}
	
	public boolean pokemonUpdate() {
		return pokemon != null;
	}
	
	public PokemonInfo getPokemon() {
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
	
	public Type[] getType() {
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
		return level != null;
	}
	
	public int getLevel() {
		return level;
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
		return updateType != Update.NO_UPDATE;
	}
	
	public Update getUpdateType() {
		return updateType;
	}
	
	public boolean learnMove() {
		return updateType == Update.LEARN_MOVE;
	}

	public boolean trigger() {
		return updateType == Update.TRIGGER;
	}

	public boolean resetState() {
		return updateType == Update.RESET_STATE;
	}

	public String getTriggerName() {
		return this.triggerName;
	}

	public boolean isChoice() {
		return this.choices != null;
	}

	public ChoiceMatcher[] getChoices() {
		return this.choices;
	}

	public boolean frontPokemonUpdate() {
		return this.frontPokemon != null;
	}

	public ActivePokemon getFrontPokemon() {
		return this.frontPokemon;
	}

	public boolean showImageUpdate() {
		return this.showImage != null;
	}

	public boolean getShowImage() {
		return this.showImage;
	}

	public boolean weatherUpdate() {
		return this.weather != null;
	}

	public Weather getWeather() {
		return this.weather;
	}

	public boolean terrainUpdate() {
		return this.terrain != null;
	}

	public TerrainType getTerrain() {
		return this.terrain;
	}
}
