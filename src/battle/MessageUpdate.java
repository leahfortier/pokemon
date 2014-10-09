package battle;

import main.Type;
import pokemon.ActivePokemon;
import pokemon.Gender;
import pokemon.PokemonInfo;
import pokemon.Stat;
import battle.effect.Status.StatusCondition;

public class MessageUpdate 
{
	private String message;
	private int hp;
	private int maxHP;
	private int[] statGains;
	private int[] newStats;
	private StatusCondition status;
	private PokemonInfo pokemon;
	private boolean shiny;
	private boolean animation;
	private Type[] type;
	private boolean playerTarget; // SO YOU KNOW WHO TO GIVE THE HP/STATUS UPDATE TO
	private boolean switchPokemon;
	private float expRatio;
	private Update updateType;
	private int level;
	private String name;
	private Gender gender;
	private ActivePokemon active;
	private Move move;
	private int duration;
	
	public static enum Update 
	{
		NONE, 
		EXIT_BATTLE, 
		PROMPT_SWITCH, 
		FORCE_SWITCH, 
		LEARN_MOVE, 
		STAT_GAIN, 
		ENTER_NAME, 
		APPEND_TO_NAME, 
		SHOW_POKEMON,
		WIN_BATTLE;
	}
	
	public MessageUpdate(String m)
	{
		message = m;
		status = null;
		hp = -1;
		maxHP = -1;
		pokemon = null;
		type = null;
		switchPokemon = false;
		updateType = Update.NONE;
		expRatio = -1;
		level = -1;
		name = "";
		gender = null;
		shiny = false;
		animation = true;
		statGains = null;
		newStats = null;
		duration = -2; // BECAUSE NEG ONE IS TOTES VALID
	}
	
	// YEAH THAT'S RIGHT HEALTH UPDATE
	public MessageUpdate(String m, int h, boolean t)
	{
		this(m);
		hp = h;
		playerTarget = t;
	}
	
	// Update to maximum HP
	public MessageUpdate(String m, int h, int max, boolean t)
	{
		this(m);
		hp = h;
		maxHP = max;
		playerTarget = t;
	}
	
	// Show stat gains
	public MessageUpdate(String m, int h, int[] gains, int[] stats, boolean t)
	{
		this(m);
		hp = h;
		maxHP = stats[Stat.HP.index()];
		statGains = gains;
		newStats = stats;
		updateType = Update.STAT_GAIN;
		playerTarget = t;
	}
	
	// OOOOHH SOMEONE'S GOT DAT STATUS CONDITION
	public MessageUpdate(String m, StatusCondition s, boolean t)
	{
		this(m);
		status = s;
		playerTarget = t;
	}
	
	// Pokemon Update!
	public MessageUpdate(String m, PokemonInfo p, boolean s, boolean a, boolean t)
	{
		this(m);
		pokemon = p;
		playerTarget = t;
		shiny = s;
		animation = a;
	}
	
	// Type Update!
	public MessageUpdate(String m, Type[] typesies, boolean t)
	{
		this(m);
		type = typesies;
		playerTarget = t;
	}
	
	// Switch update!
	public MessageUpdate(String m, ActivePokemon p, Battle b)
	{
		this(m);
		playerTarget = p.user();
		switchPokemon = true;
		hp = p.getHP();
		status = p.getStatus().getType();
		type = p.getType(b);
		shiny = p.isShiny();
		pokemon = p.getPokemonInfo();
		name = p.getName();
		maxHP = p.getStat(Stat.HP);
		level = p.getLevel();
		gender = p.getGender();
		expRatio = p.expRatio();
		animation = false;
	}
	
	// Special type of update
	public MessageUpdate(String m, Update update)
	{
		this(m);
		updateType = update;	
	}
	
	// EXP Gain update
	public MessageUpdate(String m, float ratio)
	{
		this(m);
		playerTarget = true;
		expRatio = ratio;
	}
	
	// Level up update
	public MessageUpdate(String m, int lev, float ratio)
	{
		this(m);
		playerTarget = true;
		level = lev;
		expRatio = ratio;
	}
	
	// Name change update
	public MessageUpdate(String m, String n, boolean t)
	{
		this(m);
		name = n;
		playerTarget = t;
	}
	
	// Gender change update
	public MessageUpdate(String m, Gender g, boolean t)
	{
		this(m);
		gender = g;
		playerTarget = t;
	}
	
	// Learn new move update
	public MessageUpdate(String m, ActivePokemon p, Move newMove)
	{
		this(m);
		active = p;
		move = newMove;
		updateType = Update.LEARN_MOVE;
	}
	
	// Catching a Pokemon
	public MessageUpdate(String m, int d)
	{
		this(m);
		duration = d;
	}	
	
	public String getMessage()
	{
		return message;
	}
	
	public boolean target()
	{
		return playerTarget;
	}
	
	public boolean healthUpdate()
	{
		return hp != -1;
	}
	
	public int getHP()
	{
		return hp;
	}
	
	public boolean maxHealthUpdate()
	{
		return maxHP != -1;
	}
	
	public int getMaxHP()
	{
		return maxHP;
	}
	
	public boolean gainUpdate()
	{
		return statGains != null;
	}
	
	public int[] getGain()
	{
		return statGains;
	}
	
	public int[] getNewStats()
	{
		return newStats;
	}
	
	public boolean statusUpdate()
	{
		return status != null;
	}
	
	public StatusCondition getStatus()
	{
		return status;
	}
	
	public boolean pokemonUpdate()
	{
		return pokemon != null;
	}
	
	public PokemonInfo getPokemon()
	{
		return pokemon;
	}
	
	public boolean getShiny()
	{
		return shiny;
	}
	
	public boolean isAnimate()
	{
		return animation;
	}
	
	public boolean typeUpdate()
	{
		return type != null;
	}
	
	public Type[] getType()
	{
		return type;
	}
	
	public boolean switchUpdate()
	{
		return switchPokemon;
	}
	
	public boolean expUpdate()
	{
		return expRatio >= 0;
	}
	
	public float getEXPRatio()
	{
		return expRatio;
	}
	
	public boolean levelUpdate()
	{
		return level != -1;
	}
	
	public int getLevel()
	{
		return level;
	}
	
	public boolean nameUpdate()
	{
		return name.length() > 0;
	}
	
	public String getName()
	{
		return name;
	}
	
	public boolean genderUpdate()
	{
		return gender != null;
	}
	
	public Gender getGender()
	{
		return gender;
	}
	
	public boolean catchUpdate()
	{
		return duration != -2;
	}
	
	public int getDuration()
	{
		return duration;
	}
	
	public ActivePokemon getActivePokemon()
	{
		return active;
	}
	
	public Move getMove()
	{
		return move;
	}
	
	public boolean hasUpdateType()
	{
		return updateType != Update.NONE;
	}
	
	public Update getUpdateType()
	{
		return updateType;
	}
	
	public boolean endBattle()
	{
		return updateType == Update.EXIT_BATTLE;
	}
	
	public boolean promptSwitch()
	{
		return updateType == Update.PROMPT_SWITCH;
	}
	
	public boolean forceSwitch()
	{
		return updateType == Update.FORCE_SWITCH;
	}
	
	public boolean learnMove()
	{
		return updateType == Update.LEARN_MOVE;
	}
}
