package message;

import battle.Battle;
import battle.Move;
import battle.effect.status.StatusCondition;
import gui.view.BattleView;
import gui.view.BattleView.VisualState;
import main.Game;
import gui.view.ViewMode;
import main.Global;
import main.Type;
import map.entity.PlayerEntity;
import pattern.action.ChoiceActionMatcher.ChoiceMatcher;
import pokemon.ActivePokemon;
import pokemon.Gender;
import pokemon.PokemonInfo;
import pokemon.Stat;
import sound.SoundTitle;
import util.StringUtils;

public class MessageUpdate {
	private String message;
	private Integer hp;
	private Integer maxHP;
	private int[] statGains;
	private int[] newStats;
	private StatusCondition status;
	private PokemonInfo pokemon;
	private boolean shiny;
	private boolean animation;
	private Type[] type;
	private boolean playerTarget; // SO YOU KNOW WHO TO GIVE THE HP/STATUS UPDATE TO
	private boolean switchPokemon;
	private Float expRatio;
	private Update updateType;
	private Integer level;
	private String name;
	private Gender gender;
	private ActivePokemon active;
	private Move move;
	private Integer duration;
	private String triggerName;
	private ChoiceMatcher[] choices;
	
	public enum Update {
		NO_UPDATE,
		TRIGGER,
		ENTER_BATTLE,
		ENTER_NAME,
		APPEND_TO_NAME,
		SHOW_POKEMON,
		PROMPT_SWITCH(VisualState.POKEMON),
		LEARN_MOVE(VisualState.LEARN_MOVE_QUESTION),
		STAT_GAIN(VisualState.STAT_GAIN),
		EXIT_BATTLE(battleView -> {
            Game.setViewMode(ViewMode.MAP_VIEW);
            battleView.clearUpdate();
			Messages.clearBattleMessages();
			Messages.mappityMap();
			PlayerEntity.currentInteractionEntity = null;
        }),
		FORCE_SWITCH(battleView -> {
            battleView.setVisualState(VisualState.POKEMON);
            battleView.setSwitchForced();
            battleView.clearUpdate();
        }),
		WIN_BATTLE(battleView -> {
            if (battleView.getCurrentBattle().isWildBattle()) {
                Global.soundPlayer.playMusic(SoundTitle.WILD_POKEMON_DEFEATED);
            }
            else {
                // TODO: Get trainer win music
                Global.soundPlayer.playMusic(SoundTitle.TRAINER_DEFEATED);
            }
        });
		
		private final PerformUpdate performUpdate;
		
		Update() {
			this(battleView -> {});
		}
		
		Update(final BattleView.VisualState visualState) {
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
	}

	// TODO: Yeah pretty much all of these constructors should be rewritten to be more obvious what's going on and not just guessing based off on parameters
	public MessageUpdate(String message) {
		this.message = message;
		this.updateType = Update.NO_UPDATE;
	}

	public MessageUpdate() {
		this(StringUtils.empty());
	}

	// YEAH THAT'S RIGHT HEALTH UPDATE
	public MessageUpdate(int hp, boolean target) {
		this();
		this.hp = hp;
		this.playerTarget = target; // TODO: This shouldn't be stored as a boolean but as an enum
	}

	// Update to maximum HP
	public MessageUpdate(int hp, int maxHp, boolean target) {
		this(hp, target);
		this.maxHP = maxHp;
	}
	
	// Show stat gains
	public MessageUpdate(int[] gains, int[] stats) {
		this();
		maxHP = stats[Stat.HP.index()];
		statGains = gains;
		newStats = stats;
		updateType = Update.STAT_GAIN;
		playerTarget = true;
	}
	
	// OOOOHH SOMEONE'S GOT DAT STATUS CONDITION
	public MessageUpdate(StatusCondition status, boolean target) {
		this();
		this.status = status;
		this.playerTarget = target;
	}
	
	// Pokemon Update!
	public MessageUpdate(String message, PokemonInfo pokemon, boolean shiny, boolean animation, boolean target) {
		this(message);
		this.pokemon = pokemon;
		this.playerTarget = target;
		this.shiny = shiny;
		this.animation = animation;
	}
	
	// Type Update!
	public MessageUpdate(Type[] typesies, boolean target) {
		this();
		this.type = typesies;
		this.playerTarget = target;
	}
	
	// Switch update!
	public MessageUpdate(String message, ActivePokemon active, Battle battle) {
		this(message);
		this.playerTarget = active.user();
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
		this.animation = false;
	}
	
	// Special type of update
	public MessageUpdate(String m, Update update) {
		this(m);
		this.updateType = update;
	}

	public MessageUpdate(String m, String triggerName, Update update) {
		this(m);
		this.updateType = update;

		if (update == Update.TRIGGER) {
			this.triggerName = triggerName;
		}
	}

	public MessageUpdate(String m, ChoiceMatcher[] choices) {
		this(m);
		this.choices = choices;
	}
	
	// EXP Gain update
	public MessageUpdate(int lvl, float ratio, boolean levelUp) {
		this();
		
		this.playerTarget = true;
		this.expRatio = ratio;
		
		if (levelUp) {
			level = lvl;
		}
	}
	
	// Name change update
	public MessageUpdate(String name, boolean target) {
		this();
		this.name = name;
		this.playerTarget = target;
	}
	
	// Gender change update
	public MessageUpdate(Gender gender, boolean target) {
		this();
		this.gender = gender;
		this.playerTarget = target;
	}
	
	// Learn new move update
	public MessageUpdate(String message, ActivePokemon active, Move newMove) {
		this(message);
		this.active = active;
		this.move = newMove;
		this.updateType = Update.LEARN_MOVE;
	}
	
	// Catching a Pokemon
	public MessageUpdate(String message, int duration) {
		this(message);
		this.duration = duration;
	}	
	
	public String getMessage() {
		return message;
	}
	
	public boolean target() {
		return playerTarget;
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
	
	public ActivePokemon getActivePokemon() {
		return active;
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
	
	public boolean endBattle() {
		return updateType == Update.EXIT_BATTLE;
	}
	
	public boolean promptSwitch() {
		return updateType == Update.PROMPT_SWITCH;
	}
	
	public boolean forceSwitch() {
		return updateType == Update.FORCE_SWITCH;
	}
	
	public boolean learnMove() {
		return updateType == Update.LEARN_MOVE;
	}

	public boolean trigger() {
		return updateType == Update.TRIGGER;
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
}
