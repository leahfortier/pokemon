package pokemon;

import item.Item;
import item.berry.Berry;
import item.berry.HealthTriggeredBerry;
import item.hold.EVItem;
import item.hold.HoldItem;
import item.hold.PowerItem;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.Global;
import main.Namesies;
import main.Namesies.NamesiesType;
import main.Type;
import pokemon.Evolution.EvolutionCheck;
import pokemon.PokemonInfo.WildHoldItem;
import trainer.Pokedex.PokedexStatus;
import battle.Attack;
import battle.Battle;
import battle.BattleAttributes;
import battle.Move;
import battle.effect.AbilityCondition;
import battle.effect.BracingEffect;
import battle.effect.Effect.CastSource;
import battle.effect.FaintEffect;
import battle.effect.GroundedEffect;
import battle.effect.HalfWeightEffect;
import battle.effect.IntegerCondition;
import battle.effect.ItemCondition;
import battle.effect.LevitationEffect;
import battle.effect.MoveListCondition;
import battle.effect.MultiTurnMove;
import battle.effect.NameChanger;
import battle.effect.OpponentTrappingEffect;
import battle.effect.PokemonEffect;
import battle.effect.StallingEffect;
import battle.effect.StatsCondition;
import battle.effect.Status;
import battle.effect.Status.StatusCondition;
import battle.effect.TeamEffect;
import battle.effect.TrappingEffect;
import battle.effect.TypeCondition;

public class ActivePokemon implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final Pattern pokemonPattern = Pattern.compile("(?:(\\w+)\\s*(\\d+)([A-Za-z \\t0-9,:.\\-'*]*)|(RandomEgg))");
	public static final Pattern pokemonParameterPattern = Pattern.compile("(?:(Shiny)|(Moves:)\\s*([A-Za-z0-9 ]+),\\s*([A-Za-z0-9 ]+),\\s*([A-Za-z0-9 ]+),\\s*([A-Za-z0-9 ]+)\\s*[*]|(Egg)|(Item:)\\s*([\\w \\-'.]+)[*])", Pattern.UNICODE_CHARACTER_CLASS);
	
	public static final int MAX_LEVEL = 100;
	public static final int MAX_IV = 31;
	
	private static final String[][] characteristics = 
		{{"Loves to eat",            "Proud of its power",      "Sturdy body",            "Highly curious",        "Strong willed",     "Likes to run"},
		 {"Takes plenty of siestas", "Likes to thrash about",   "Capable of taking hits", "Mischievous",           "Somewhat vain",     "Alert to sounds"},
		 {"Nods off a lot",          "A little quick tempered", "Highly persistent",      "Thoroughly cunning",    "Strongly defiant",  "Impetuous and silly"},
		 {"Scatters things often",   "Likes to fight",          "Good endurance",         "Often lost in thought", "Hates to lose",     "Somewhat of a clown"},
		 {"Likes to relax",          "Quick tempered",          "Good perseverance",      "Very finicky",          "Somewhat stubborn", "Quick to flee"}};
	
	private PokemonInfo pokemon;
	private String nickname;
	private int[] stats;
	private int[] IVs;
	private List<Move> moves;
	private int hp;
	private int level;
	private boolean playerPokemon;
	private Status status;
	private int totalEXP;
	private int[] EVs;
	private HoldItem heldItem;
	private Ability ability;
	private Gender gender;
	private Nature nature;
	private String characteristic;
	private boolean shiny;
	private BattleAttributes attributes;
	private Type hiddenPowerType;
	private boolean isEgg;
	private int eggSteps;

	// General constructor for an active Pokemon (user is true if it is the player's pokemon and false if it is wild, enemy trainer, etc.)
	public ActivePokemon(PokemonInfo pokemonSpecies, int level, boolean isWild, boolean user)
	{
		this.pokemon = pokemonSpecies;
		this.nickname = pokemonSpecies.getName();
		this.level = level;
		
		setIVs();
		
		this.nature = new Nature();
		setCharacteristic();
		
		this.EVs = new int[Stat.NUM_STATS];
		this.stats = new int[Stat.NUM_STATS];
		setStats();
		
		this.hp = stats[Stat.HP.index()];
		this.playerPokemon = user;
		this.attributes = new BattleAttributes();
		
		removeStatus();
		
		this.totalEXP = GrowthRate.getEXP(pokemon.getGrowthRate(), this.level);
		this.totalEXP += (int)(Math.random()*expToNextLevel());
		this.gender = Gender.getGender(pokemon.getMaleRatio());
		this.shiny = user || isWild ? (int)(Math.random()*8192) == 13 : false;
		
		setMoves();
		
		this.ability = Ability.assign(this.pokemon);
		this.hiddenPowerType = computeHiddenPowerType();
		
		this.heldItem = isWild ? WildHoldItem.getWildHoldItem(this.pokemon.getWildItems()) : (HoldItem)Item.noneItem();
		
		this.isEgg = false;
		this.eggSteps = 0;
	}
	
	// Constructor for Eggs
	public ActivePokemon(PokemonInfo p)
	{
		this(p, 1, false, true);
		isEgg = true;
		eggSteps = p.getEggSteps();
		nickname = "Egg";
	}
	
	public ActivePokemon(ActivePokemon daddy, ActivePokemon mommy, PokemonInfo babyInfo)
	{
		this(babyInfo, 1, false, true);
		
		moves = Breeding.getBabyMoves(daddy, mommy, babyInfo);
		IVs = Breeding.getBabyIVs(daddy, mommy);
		nature = Breeding.getBabyNature(daddy, mommy);
		hiddenPowerType = computeHiddenPowerType();
		setStats();
		setCharacteristic();
	}
	
	/*
	 * Format: Name Level Parameters
	 * Possible parameters:
	 * 		Moves: Move1, Move2, Move3, Move4*
	 * 		Shiny
	 * 		Egg
	 * 		Item: item name*
	 */
	// Constructor for triggers
	public static ActivePokemon createActivePokemon(String pokemonDescription, boolean user)
	{
		Matcher m = pokemonPattern.matcher(pokemonDescription);
		m.find();
		
		//Random Egg
		if (m.group(4) != null) 
		{
			if(!user)
			{
				Global.error("Trainers cannot have eggs.");
			}
			return new ActivePokemon(PokemonInfo.getRandomBaseEvolution());
		}
		
		Namesies namesies = Namesies.getValueOf(m.group(1), NamesiesType.POKEMON);
		PokemonInfo pinfo = PokemonInfo.getPokemonInfo(namesies);
		
		int level = Integer.parseInt(m.group(2));
		
		Matcher params = pokemonParameterPattern.matcher(m.group(3));

		boolean shiny = false;
		boolean setMoves = false;
		ArrayList<Move> moves = null;
		HoldItem holdItem = null;
		
		boolean isEgg = false;
		
		while (params.find())
		{
			if (params.group(1) != null) 
				shiny = true;
			
			if (params.group(2) != null)
			{
				setMoves = true;
				moves = new ArrayList<>();
				for (int i = 0; i < 4; ++i)
				{
					String attackName = params.group(3 + i);
					if (!attackName.equals("None"))
					{	
						if(!Attack.isAttack(attackName))
						{
							Global.error(attackName +" is not an attack. Pokemon: "+pinfo.getName());
						}
						
						moves.add(new Move(Attack.getAttackFromName(attackName)));
					}
				}
			}
			
			if (params.group(7) != null) 
				isEgg = true;
			
			if (params.group(8) != null)
			{
				String itemName = params.group(9);
				if (Item.isItem(itemName))
				{
					Item i = Item.getItemFromName(itemName);
					if (i.isHoldable())
					{
						holdItem = (HoldItem)i;
					}
					else
					{
						Global.error(itemName +" is not a hold item. Pokemon: "+pinfo.getName());
					}
				}
				else
				{
					Global.error(itemName +" is not an item. Pokemon: "+pinfo.getName());
				}
			}
		}
		
		ActivePokemon p;
		if (isEgg) 
		{	
			if(!user)
			{
				Global.error("Trainers cannot have eggs.");
			}
			
			p = new ActivePokemon(pinfo);
		}
		else 
		{
			p = new ActivePokemon(pinfo, level, false, user);
		}
		
		if (shiny) 
		{
			p.setShiny();
		}
		
		if (setMoves) 
		{
			p.setMoves(moves);
		}
		
		if (holdItem != null)
		{
			p.giveItem(holdItem);
		}

		return p;
	}
	
	public void setGender(Gender gender)
	{
		this.gender = gender;
	}
	
	// Does not include shiny -- this is for the small party tiles
	public int getTinyImageIndex()
	{
		return this.isEgg ? PokemonInfo.EGG_IMAGE : pokemon.getNumber();
	}
	
	// Larger image index
	public int getImageIndex()
	{
		return this.isEgg() ? PokemonInfo.EGG_IMAGE : PokemonInfo.getImageNumber(this.getPokemonInfo().getNumber(), this.isShiny());
	}
	
	public boolean isEgg()
	{
		return isEgg;
	}
	
	public boolean hatch()
	{	
		if (!isEgg())
		{
			Global.error("Only eggs can hatch!");
		}
		
		eggSteps--;
		
		if (eggSteps > 0)
		{
			return false;
		}
		
		this.isEgg = false;
		this.nickname = pokemon.getName();
		
		return true;
	}
	
	public String getEggMessage()
	{
		if (!isEgg()) 
		{
			Global.error("Only Eggs can have egg messages.");
		}
		
		if (eggSteps > 10*255) return "Wonder what's inside? It needs more time though.";
		else if (eggSteps > 5*255) return "It moves around inside sometimes. It must be close to hatching.";
		return "It's making sounds inside! It's going to hatch soon!";
	}
	
	private void setMoves()
	{
		moves = new ArrayList<Move>();
		TreeMap<Integer, List<Namesies>> map = pokemon.getLevelUpMoves();
		for (Integer i : map.keySet())
		{
			if (i > level) 
			{
				continue;
			}
			
			for (Namesies s : map.get(i))
			{
				if (hasActualMove(s)) 
					continue;
				
				moves.add(new Move(Attack.getAttack(s)));
				
				// This can be an 'if' statement, but just to be safe...
				while (moves.size() > Move.MAX_MOVES) 
					moves.remove(0);
			}
		}
	}
	
	public void setMoves(List<Move> list)
	{
		moves = list;
	}
	
	public void setShiny()
	{
		shiny = true;
	}
	
	// Random value between 0 and 31
	private void setIVs()
	{
		IVs = new int[Stat.NUM_STATS];
		for (int i = 0; i < IVs.length; i++) 
			IVs[i] = (int)(Math.random() * (MAX_IV + 1));
	}
	
	private void setIVs(ActivePokemon daddy, ActivePokemon mommy)
	{
		Item daddysItem = daddy.getActualHeldItem();
		Item mommysItem = mommy.getActualHeldItem();
		
		ArrayList<PowerItem> powerItems = new ArrayList<>();
		if (daddysItem instanceof PowerItem)
			powerItems.add((PowerItem)daddysItem);
		if (mommysItem instanceof PowerItem)
			powerItems.add((PowerItem)mommysItem);
		
		ArrayList<Stat> remainingStats = new ArrayList<>();
		for (Stat s : Stat.STATS)
		{
			remainingStats.add(s);
		}
		
		int remainingIVsToInherit = daddysItem.namesies() == Namesies.DESTINY_KNOT_ITEM || mommysItem.namesies() == Namesies.DESTINY_KNOT_ITEM ? 5 : 3;
		IVs = new int[Stat.NUM_STATS];
		Arrays.fill(IVs, -1);
		
		if (powerItems.size() > 0)
		{
			PowerItem randomItem = powerItems.get((int)(Math.random() * powerItems.size()));
			Stat stat = randomItem.powerStat();
			remainingStats.remove(stat);
			
			ActivePokemon parentToInheritFrom = (int)(Math.random() * 2) == 0 ? daddy : mommy;
			IVs[stat.index()] = parentToInheritFrom.getIV(stat.index());
			
			remainingIVsToInherit--;
		}
		
		while (remainingIVsToInherit --> 0)
		{
			Stat stat = remainingStats.get((int)(Math.random() * remainingStats.size()));
			remainingStats.remove(stat);
			
			ActivePokemon parentToInheritFrom = (int)(Math.random() * 2) == 0 ? daddy : mommy;
			IVs[stat.index()] = parentToInheritFrom.getIV(stat.index());
		}
		
		for (int i = 0; i < IVs.length; i++)
			if (IVs[i] == -1)
				IVs[i] = (int)(Math.random() * (MAX_IV + 1));
	}
	
	private void setCharacteristic()
	{
		int maxIndex = 0;
		for (int i = 1; i < IVs.length; i++)
		{
			if (IVs[i] > IVs[maxIndex])
			{
				maxIndex = i;
			}
		}
		
		characteristic = characteristics[IVs[maxIndex]%5][maxIndex];
	}
	
	private void setStats()
	{
		int prevHP = stats[Stat.HP.index()];
		
		stats = new int[Stat.NUM_STATS];
		for (int i = 0; i < stats.length; i++)
		{
			stats[i] = Stat.getStat(i, level, pokemon.getStat(i), IVs[i], EVs[i], nature.getNatureVal(i));
		}
		
		hp += stats[Stat.HP.index()] - prevHP;
	}
	
	private Type computeHiddenPowerType()
	{
		return Type.getHiddenType(((IVs[Stat.HP.index()]%2 + 2*(IVs[Stat.ATTACK.index()]%2) 
				+ 4*(IVs[Stat.DEFENSE.index()]%2) + 8*(IVs[Stat.SPEED.index()]%2) 
				+ 16*(IVs[Stat.SP_ATTACK.index()]%2) + 32*(IVs[Stat.SP_DEFENSE.index()]%2))*15)/63);
	}
	
	public Type getHiddenPowerType()
	{
		return hiddenPowerType;
	}
	
	public String getCharacteristic()
	{
		return characteristic;
	}
	
	public int[] getStats()
	{
		return stats;
	}
	
	public int[] getIVs()
	{
		return IVs;
	}
	
	public int[] getEVs()
	{
		return EVs;
	}
	
	public int getIV(int index)
	{
		return IVs[index];
	}
	
	public int getEV(int index)
	{
		return EVs[index];
	}
	
	public Nature getNature()
	{
		return nature;
	}
	
	public void assignAbility(Ability newAbility)
	{
		ability = newAbility;
	}
	
	public Ability getActualAbility()
	{
		return ability;
	}
	
	public Ability getAbility()
	{
		// Check if the Pokemon has had its ability changed during the battle
		PokemonEffect e = getEffect(Namesies.CHANGE_ABILITY_EFFECT);
		if (e != null) return ((AbilityCondition)e).getAbility();
		
		return ability;
	}
	
	public int getStage(int index)
	{
		return attributes.getStage(index);
	}
	
	public Move getMove(Battle b, int index)
	{
		return getMoves(b).get(index);
	}
	
	public int getMaxHP()
	{
		return stats[Stat.HP.index()];
	}
	
	public int getStat(Battle b, Stat s)
	{
		Object stat = Battle.getInvoke(b.getEffectsList(this), StatsCondition.class, "getStat", this, s);
		if (stat != null)
		{
			return (int)stat;
		}
		
		return stats[s.index()];
	}
	
	public List<Move> getMoves(Battle b)
	{
		Move[] moveArray = this.moves.toArray(new Move[0]);		
		moveArray = (Move[])Battle.updateInvoke(1, b.getEffectsList(this), MoveListCondition.class, "getMoveList", this, moveArray);
		List<Move> moveList = Arrays.asList(moveArray); 
		
		return moveList;
	}
	
	public List<Move> getActualMoves()
	{
		return moves;
	}
	
	public int getTotalEXP()
	{
		return totalEXP;
	}
	
	public int expToNextLevel()
	{
		if (level == MAX_LEVEL) return 0;
		
		return GrowthRate.getEXP(pokemon.getGrowthRate(), level + 1) - totalEXP;
	}
	
	public float expRatio()
	{
		return 1.0f - (float)expToNextLevel()/(GrowthRate.getEXP(pokemon.getGrowthRate(), level + 1) - GrowthRate.getEXP(pokemon.getGrowthRate(), level));
	}
	
	public void gainEXP(Battle b, int gain, ActivePokemon dead)
	{
		boolean front = b.getPlayer().front() == this;
		
		// Add EXP
		totalEXP += gain;
		b.addMessage(getActualName() + " gained " + gain + " EXP points!");
		if (front) b.addMessage("", this, Math.min(1, expRatio()), false);
		
		// Add EVs
		Item i = getHeldItem(b);
		int[] vals = dead.getPokemonInfo().getGivenEVs();
		if (i instanceof EVItem) 
		{
			vals = ((EVItem)i).getEVs(vals);
		}
		
		addEVs(vals);
		
		// Level up if applicable
		while (totalEXP >= GrowthRate.getEXP(pokemon.getGrowthRate(), level + 1))
		{
			levelUp(b);
		}
	}

	public boolean levelUp(Battle b)
	{
		if (level == MAX_LEVEL) 
		{
			return false;
		}
		
		boolean print = b != null;
		boolean front = print && b.getPlayer().front() == this;
		
		// Grow to the next level
		level++;
		if (print) b.addMessage(getActualName() + " grew to level " + level + "!");
		if (print && front) b.addMessage("", this, Math.min(1, expRatio()), true);
		
		// Change stats -- keep track of the gains
		int[] prevStats = stats.clone();
		int[] gain = new int[Stat.NUM_STATS];
		setStats();
		for (int i = 0; i < Stat.NUM_STATS; i++) 
		{
			gain[i] = stats[i] - prevStats[i];
		}
		
		// TODO: Show gain update for other Pokemon in the party
		if (print && front) b.addMessage("", this, gain, stats);
		
		// Learn new moves
		for (Namesies s : pokemon.getMoves(level)) 
		{
			learnMove(b, s);
		}
		
		// Maybe you'll evolve?!
		BaseEvolution ev = (BaseEvolution)pokemon.getEvolution().getEvolution(EvolutionCheck.LEVEL, this, null);
		if (ev != null) evolve(b, ev);
		
		return true;
	}
	
	public void evolve(Battle b, BaseEvolution ev)
	{
		if (getActualHeldItem() == Item.getItem(Namesies.EVERSTONE_ITEM)) 
		{
			return;
		}
		
		boolean print = b != null, front = print && b.getPlayer().front() == this;
		boolean sameName = nickname.equals(pokemon.getName());
		
		ability = Ability.evolutionAssign(this, ev.getEvolution());
		
		String name = nickname;
		if (print) b.addMessage(getActualName() + " is evolving!");
		
		pokemon = ev.getEvolution();
		if (print) b.getPlayer().getPokedex().setStatus(this.getPokemonInfo(), PokedexStatus.CAUGHT);
		
		// Set name if it was not given a nickname
		if (sameName) nickname = pokemon.getName();
		
		// Change stats
		int[] prevStats = stats.clone(), gain = new int[Stat.NUM_STATS];
		setStats();
		for (int i = 0; i < Stat.NUM_STATS; i++) gain[i] = stats[i] - prevStats[i];
		
		if (print && front) b.addMessage("", pokemon, shiny, true, playerPokemon);
		
		String message = name + " evolved into " + pokemon.getName() + "!";
		
		if (print) b.addMessage(message);
		if (print && front) b.addMessage("", this, gain, stats);
		
		// Learn new moves
		List<Namesies> levelMoves = pokemon.getMoves(level);
		for (Namesies s : levelMoves) 
		{
			learnMove(b, s);
		}
	}
	
	private void learnMove(Battle b, Namesies attackName)
	{
		// Don't want to learn a move you already know!
		if (hasActualMove(attackName)) 
		{
			return;
		}
		
		Move m = new Move(Attack.getAttack(attackName));
		if (moves.size() < Move.MAX_MOVES)
		{
			if (b != null) 
			{
				b.addMessage(getActualName() + " learned " + m.getAttack().getName() + "!");
			}
			
			addMove(b, m, moves.size() - 1);
			return;
		}
		
		// Only add messagy things whilst in battle TODO: But really we need to be able to do messagy things outside of battle too...
		if (b == null) 
		{
			return;
		}
		
		b.addMessage(" ", this, m);
		b.addMessage(getActualName() + " did not learn " + m.getAttack().getName() + ".");
		
		// Wait I think this is in a motherfucking for loop because this is really poorly and hackily implemented...
		for (int i = 0; i < moves.size(); i++)
		{
			b.addMessage(getActualName() + " forgot how to use " + moves.get(i).getAttack().getName() + "...");	
		}
		
		b.addMessage("...and " + getActualName() + " learned " + m.getAttack().getName() + "!");
	}
	
	public void addMove(Battle b, Move m, int index)
	{
		if (moves.size() < Move.MAX_MOVES) moves.add(m);
		else moves.set(index, m);
		
		BaseEvolution ev = (BaseEvolution)pokemon.getEvolution().getEvolution(EvolutionCheck.MOVE, this, null);
		if (ev != null) evolve(b, ev);
	}
	
	public int getLevel()
	{
		return level;
	}
	
	public void callNewMove(Battle b, ActivePokemon opp, Move m)
	{
		Move temp = getMove();
		m.setAttributes(b, this, opp);
		setMove(m);
		b.printAttacking(this);
		getAttack().apply(this, opp, b);
		setMove(temp);
	}
	
	// Pangoro breaks the mold!
	public boolean breaksTheMold()
	{
		switch (getAbility().namesies())
		{
			case MOLD_BREAKER_ABILITY:
			case TURBOBLAZE_ABILITY:
			case TERAVOLT_ABILITY:
				return true;
			default:
				return false;
		}
	}
	
	public boolean canFight()
	{
		return !hasStatus(StatusCondition.FAINTED) && !isEgg();
	}
	
	// Returns if the Pokemon is stalling -- that is that it will move last within its priority bracket
	public boolean isStalling(Battle b)
	{
		return getAbility() instanceof StallingEffect || getHeldItem(b) instanceof StallingEffect;
	}
	
	public boolean hasAbility(Namesies a)
	{
		return getAbility().namesies() == a;
	}
	
	public void setStatus(Status s)
	{
		status = s;
	}
	
	public void addEffect(PokemonEffect e)
	{
		attributes.addEffect(e);
	}
	
	public void setMove(Move m)
	{
		attributes.setMove(m);
	}
	
	public Move getMove()
	{
		return attributes.getMove();
	}
	
	public Attack getAttack()
	{
		Move m = attributes.getMove();
		if (m == null)
		{
			return null;
		}
		
		return m.getAttack();
	}
	
	public boolean isAttackType(Type t)
	{
		return getAttackType() == t;
	}
	
	public Type getAttackType()
	{
		return getMove().getType();
	}
	
	public int getAttackPower()
	{
		return getMove().getPower();
	}
	
	// Returns whether or not this Pokemon knows this move already
	public boolean hasActualMove(Namesies name)
	{
		return hasMove(getActualMoves(), name);
	}
	
	public boolean hasMove(Battle b, Namesies name)
	{
		return hasMove(getMoves(b), name);
	}
	
	private boolean hasMove(List<Move> moveList, Namesies name)
	{
		for (Move m : moveList)
			if (m.getAttack().namesies() == name) 
				return true;

		return false;
	}
	
	public Gender getGender()
	{
		return gender;
	}
	
	public boolean isSemiInvulnerable()
	{
		if (getMove() == null) 
		{
			return false;
		}
		
		return !getMove().isReady() && ((MultiTurnMove)getAttack()).semiInvulnerability();
	}
	
	public boolean isSemiInvulnerableFlying()
	{
		return isSemiInvulnerable() && (getAttack().namesies() == Namesies.FLY_ATTACK || getAttack().namesies() == Namesies.BOUNCE_ATTACK); 
	}
	
	public boolean isSemiInvulnerableDigging()
	{
		return isSemiInvulnerable() && getAttack().namesies() == Namesies.DIG_ATTACK;
	}
	
	public boolean isSemiInvulnerableDiving()
	{
		return isSemiInvulnerable() && getAttack().namesies() == Namesies.DIVE_ATTACK;
	}
	
	private int totalEVs()
	{
		int sum = 0;
		for (int i = 0; i < EVs.length; i++) sum += EVs[i];
		return sum;
	}
	
	// Adds Effort Values to a Pokemon, returns true if they were successfully added
	public boolean addEVs(int[] vals)
	{
		if (totalEVs() == Stat.MAX_EVS) 
			return false;
		
		boolean added = false;
		for (int i = 0; i < EVs.length; i++)
		{
			if (vals[i] > 0 && EVs[i] < Stat.MAX_STAT_EVS)
			{
				added = true;
				EVs[i] = Math.min(Stat.MAX_STAT_EVS, EVs[i] + vals[i]); // Don't exceed stat EV amount
				
				// Don't exceed total EV amount
				if (totalEVs() > Stat.MAX_EVS) 
				{
					EVs[i] -= (Stat.MAX_EVS - totalEVs());
					break;
				}
			}
			else if (vals[i] < 0 && EVs[i] > 0)
			{
				added = true;
				EVs[i] = Math.max(0, EVs[i] + vals[i]); // Don't drop below zero
			}
		}
		
		setStats();
		return added;
	}
	
	public Type[] getActualType()
	{
		return pokemon.getType();
	}
	
	public Type[] getDisplayType(Battle b)
	{
		return getType(b, true);
	}
	
	public Type[] getType(Battle b) 
	{	
		return getType(b, false);
	}
	
	private Type[] getType(Battle b, boolean displayOnly)
	{
		// Guarantee the change-type effect to be first
		Object[] invokees = b.getEffectsList(this, this.getEffect(Namesies.CHANGE_TYPE_EFFECT));
		
		Object changeType = Battle.getInvoke(invokees, TypeCondition.class, "getType", b, this, displayOnly);
		if (changeType != null)
		{
			return (Type[])changeType;
		}
		
		return getActualType();
	}
	
	public boolean isType(Battle b, Type type)
	{
		Type[] types = getType(b);
		return types[0] == type || types[1] == type; 
	}
	
	public int getHP()
	{
		return hp;
	}
	
	public void setHP(int amount)
	{
		hp = Math.min(getMaxHP(), Math.max(0, amount));
	}
	
	public boolean fullHealth()
	{
		return hp == getMaxHP();
	}
	
	public double getHPRatio()
	{
		return (double)hp/getMaxHP();
	}
	
	public Color getHPColor()
	{
		return Global.getHPColor(getHPRatio());
	}
	
	public String getActualName()
	{
		return nickname;
	}
	
	public String getName()
	{
		Object[] invokees = { this.getAbility() };
		Object changedName = Battle.getInvoke(invokees, NameChanger.class, "getNameChange");
		if (changedName != null)
		{
			return (String)changedName;
		}
		
		return getActualName();
	}
	
	public BattleAttributes getAttributes()
	{
		return attributes;
	}
	
	public boolean user()
	{
		return playerPokemon;
	}
	
	public void resetAttributes()
	{
		attributes = new BattleAttributes();
		
		for (Move m : moves)
		{
			m.resetReady();
		}
	}
	
	public void setCaught()
	{
		playerPokemon = true;
	}
	
	public boolean isFainted(Battle b)
	{
		// We have already checked that this Pokemon is fainted -- don't print/apply effects more than once
		if (hasStatus(StatusCondition.FAINTED))
		{
			if (hp == 0) return true;
			Global.error("Pokemon should only have the Fainted Status Condition when HP is zero.");
		}
		
		// Deady
		if (hp == 0)
		{
			b.addMessage("", this);
			
			Status.die(this);
			b.addMessage(getName() + " fainted!", this);
			
			ActivePokemon murderer = b.getOtherPokemon(user());

			// Apply effects which occur when the user faints
			Battle.invoke(getEffects().toArray(), FaintEffect.class, "deathwish", b, this, murderer);
			
			// If the pokemon fainted by direct result of an attack -- apply ability and attack deathwishes 
			if (murderer.getAttributes().isAttacking())
			{
				Object[] invokees = new Object[] {murderer.getAttack(), murderer.getAbility()};
				Battle.invoke(invokees, FaintEffect.class, "deathwish", b, this, murderer);
			}
			
			b.getEffects(playerPokemon).add(TeamEffect.getEffect(Namesies.DEAD_ALLY_EFFECT).newInstance());
			
			return true;	
		}
		
		// Still kickin' it
		return false;
	}
	
	// Returns the empty string if the Pokemon can switch, and the appropriate fail message if they cannot
	public boolean canEscape(Battle b)
	{
		// Shed Shell always allows escape
		if (isHoldingItem(b, Namesies.SHED_SHELL_ITEM)) 
		{
			return true;
		}
		
		// Check if the user is under an effect that prevents escape
		Object[] invokees = b.getEffectsList(this);
		Object trapped = Battle.checkInvoke(true, invokees, TrappingEffect.class, "isTrapped", b, this);
		if (trapped != null)
		{
			b.addMessage(((TrappingEffect)trapped).trappingMessage(this));
			return false;
		}
		
		// The opponent has an effect that prevents escape
		ActivePokemon other = b.getOtherPokemon(user());
		invokees = b.getEffectsList(other);
		trapped = Battle.checkInvoke(true, invokees, OpponentTrappingEffect.class, "trapOpponent", b, this);
		if (trapped != null)
		{
			b.addMessage(((OpponentTrappingEffect)trapped).opponentTrappingMessage(this, other));
			return false;
		}
		
		// Safe and sound
		return true;
	}
	
	public boolean hasEffect(Namesies effect)
	{
		return attributes.hasEffect(effect);
	}
	
	// Returns null if the Pokemon is not under the effects of the input effect, otherwise returns the Condition
	public PokemonEffect getEffect(Namesies effect)
	{
		return attributes.getEffect(effect);
	}
	
	public List<PokemonEffect> getEffects()
	{
		return attributes.getEffects();
	}
	
	public void modifyStages(Battle b, ActivePokemon modifier, int[] mod, CastSource source)
	{
		for (int i = 0; i < mod.length; i++)
		{
			if (mod[i] == 0) continue;
			attributes.modifyStage(modifier, this, mod[i], Stat.getStat(i, true), b, source);
		}
	}
	
	public Status getStatus()
	{
		return status;
	}
	
	// Returns whether or not the Pokemon is afflicted with a status condition
	public boolean hasStatus()
	{
		return status.getType() != StatusCondition.NONE;
	}
	
	public boolean hasStatus(StatusCondition type)
	{
		return status.getType() == type;
	}
	
	// Sets the Pokemon's status condition to be None
	public void removeStatus()
	{
		Status.removeStatus(this);
		attributes.removeEffect(Namesies.NIGHTMARE_EFFECT);
		attributes.removeEffect(Namesies.BAD_POISON_EFFECT);
	}
	
	// Returns null if the Pokemon is not bracing, and the associated effect if it is
	private BracingEffect bracing(Battle b, boolean fullHealth)
	{
		BracingEffect bracingEffect = (BracingEffect)getEffect(Namesies.BRACING_EFFECT);
		if (bracingEffect != null) 
		{
			return bracingEffect;
		}
		
		Object[] invokees = b.getEffectsList(this);
		bracingEffect = (BracingEffect)Battle.checkInvoke(true, b.getOtherPokemon(user()), invokees, BracingEffect.class, "isBracing", b, this, fullHealth);
		
		return bracingEffect;
	}
	
	// Reduces hp by amount, returns the actual amount of hp that was reduced
	public int reduceHealth(Battle b, int amount)
	{
		// Not actually reducing health...
		if (amount == 0) 
		{
			return 0;
		}
		
		// Substitute absorbs the damage instead of the Pokemon
		IntegerCondition e = (IntegerCondition)getEffect(Namesies.SUBSTITUTE_EFFECT);
		if (e != null)
		{
			e.decrease(amount);
			if (e.getAmount() <= 0)
			{
				b.addMessage("The substitute broke!");
				attributes.removeEffect(Namesies.SUBSTITUTE_EFFECT);
			}
			else
			{
				b.addMessage("The substitute absorbed the hit!");
			}
			
			return 0;
		}
		
		boolean fullHealth = fullHealth();
		
		// Reduce HP, record damage, and check if fainted
		int prev = hp, taken = prev - (hp = Math.max(0, hp - amount));
		attributes.takeDamage(taken);
		
		// Enduring the hit
		if (hp == 0)
		{
			BracingEffect brace = bracing(b, fullHealth);
			if (brace != null)
			{
				taken -= heal(1);
				
				b.addMessage("", this);
				b.addMessage(brace.braceMessage(this));				
			}
		}
		
		if (isFainted(b)) 
		{
			return taken;
		}
		
		b.addMessage("", this);
		
		// Check if the Pokemon fainted and also handle Focus Punch
		if (hasEffect(Namesies.FOCUSING_EFFECT))
		{
			b.addMessage(getName() + " lost its focus and couldn't move!");
			attributes.removeEffect(Namesies.FOCUSING_EFFECT);
			addEffect(PokemonEffect.getEffect(Namesies.FLINCH_EFFECT));
		}
		
		// Health Triggered Berries
		Item item = getHeldItem(b);
		if (item instanceof HealthTriggeredBerry)
		{
			HealthTriggeredBerry berry  = (HealthTriggeredBerry)item;
			double healthRatio = getHPRatio();
			if ((healthRatio <= berry.healthTriggerRatio() || (healthRatio <= .5 && hasAbility(Namesies.GLUTTONY_ABILITY))))
			{
				if (berry.gainBerryEffect(b, this, CastSource.HELD_ITEM))
				{
					consumeItem(b);
				}
			}
		}
		
		return taken;
	}
	
	// Reduces the amount of health that corresponds to fraction of the pokemon's total health and returns this amount
	public int reduceHealthFraction(Battle b, double fraction)
	{
		return reduceHealth(b, (int)Math.max(stats[Stat.HP.index()]*fraction, 1));
	}
	
	// Restores hp by amount, returns the actual amount of hp that was restored
	public int heal(int amount)
	{
		// Dead Pokemon can't heal
		if (hasStatus(StatusCondition.FAINTED)) return 0;
		
		int prev = hp;
		hp = Math.min(getMaxHP(), hp + amount);
		return hp - prev;
	}
	
	// Restores the amount of health that corresponds to fraction of the pokemon's total health and returns this amount
	public int healHealthFraction(double fraction)
	{
		return heal((int)Math.max(getMaxHP()*fraction, 1));
	}
	
	// Removes status, restores PP for all moves, restores to full health
	public void fullyHeal()
	{
		removeStatus();
		
		for (Move m : this.getActualMoves())
		{
			m.resetPP();
		}
		
		healHealthFraction(1);
	}
	
	// Heals the Pokemon by damage amount. It is assume damage has already been dealt to the victim
	public void sapHealth(ActivePokemon victim, int amount, Battle b, boolean print, boolean dreamEater)
	{
		if (victim.hasAbility(Namesies.LIQUID_OOZE_ABILITY))
		{
			b.addMessage(victim.getName() + "'s " + Namesies.LIQUID_OOZE_ABILITY.getName() + " caused " + getName() + " to lose health instead!");
			reduceHealth(b, amount);
			return;
		}
		
		// Big Root heals an additional 30%
		if (isHoldingItem(b, Namesies.BIG_ROOT_ITEM)) 
		{
			amount *= 1.3;
		}
		
		// Sap message (different for Dream Eater)
		if (print) 
		{
			String message = dreamEater ? victim.getName() + "'s dream was eaten!" : victim.getName() + "'s health was sapped!"; 
			b.addMessage(message);
		}
		
		// Healers gon' heal
		if (!hasEffect(Namesies.HEAL_BLOCK_EFFECT)) 
		{
			heal(amount);
		}
		
		b.addMessage("", victim);
		b.addMessage("", this);
	}
	
	public boolean isGrounded(Battle b)
	{
		return Battle.hasInvoke(b.getEffectsList(this), GroundedEffect.class);
	}
	
	// Returns true if the Pokemon is currently levitating for any reason
	public boolean isLevitating(Battle b)
	{
		// Grounded effect take precedence over levitation effects
		if (isGrounded(b))
		{
			return false;
		}
		
		// Obvs levitating if you have a levitation effect
		// Stupid motherfucking Mold Breaker not allowing me to make Levitate a Levitation effect, fuck you Mold Breaker. -- NOT ANYMORE NOW WE HAVE Battle.hasInvoke FUCK YES YOU GO GLENN COCO
		if (Battle.hasInvoke(b.getEffectsList(this), LevitationEffect.class))
		{
			return true;
		}
		
		// Flyahs gon' Fly
		return isType(b, Type.FLYING);
	}

	public void giveItem(HoldItem i)
	{
		heldItem = i;
	}
	
	public void removeItem()
	{
		heldItem = (HoldItem)Item.noneItem();
	}
	
	public void consumeItem(Battle b)
	{
		Item consumed = getHeldItem(b);
		PokemonEffect.getEffect(Namesies.CONSUMED_ITEM_EFFECT).cast(b, this, this, CastSource.HELD_ITEM, false);
		
		ActivePokemon other = b.getOtherPokemon(playerPokemon); 
		if (other.hasAbility(Namesies.PICKUP_ABILITY) && !other.isHoldingItem(b))
		{
			other.giveItem((HoldItem)consumed);
			b.addMessage(other.getName() + " picked up " + getName() + "'s " + consumed.getName() + "!");
		}
	}
	
	public Item getActualHeldItem()
	{
		return (Item)heldItem;
	}
	
	public Item getHeldItem(Battle b)
	{
		if (b == null)
		{
			return getActualHeldItem();
		}
		
		if (hasAbility(Namesies.KLUTZ_ABILITY) || b.hasEffect(Namesies.MAGIC_ROOM_EFFECT) || hasEffect(Namesies.EMBARGO_EFFECT)) 
		{
			return Item.noneItem();
		}
		
		// Check if the Pokemon has had its item changed during the battle
		PokemonEffect changeItem = getEffect(Namesies.CHANGE_ITEM_EFFECT);
		Item item = changeItem == null ? getActualHeldItem() : ((ItemCondition)changeItem).getItem();
		
		if (item instanceof Berry && b.getOtherPokemon(user()).hasAbility(Namesies.UNNERVE_ABILITY))
		{
			return Item.noneItem();
		}
		
		return item;
	}
	
	public boolean isHoldingItem(Battle b, Namesies itemName)
	{
		return getHeldItem(b) == Item.getItem(itemName);
	}
	
	public boolean isHoldingItem(Battle b)
	{
		return getHeldItem(b) != Item.noneItem();
	}
	
	public boolean isShiny()
	{
		return shiny;
	}
	
	public PokemonInfo getPokemonInfo()
	{
		return pokemon;
	}
	
	public boolean isPokemon(Namesies name)
	{
		return pokemon.namesies() == name;
	}
	
	public double getWeight(Battle b)
	{
		Object[] invokees = b.getEffectsList(this);
		ActivePokemon moldBreaker = b.getOtherPokemon(user());
		
		int halfAmount = (int)Battle.updateInvoke(0, moldBreaker, invokees, HalfWeightEffect.class, "getHalfAmount", 0);
		
		return pokemon.getWeight()/Math.pow(2, halfAmount);
	}
	
	public void startAttack(Battle b, ActivePokemon opp)
	{
		this.getAttributes().setAttacking(true);
		this.getMove().switchReady(b, this); // TODO: I don't think this works right because this is happening before you check if they're able to attack and honestly they shouldn't really switch until the end of the turn
		this.getMove().setAttributes(b, this, opp);
	}
	
	public void endAttack(Battle b, ActivePokemon opp, boolean success, boolean reduce)
	{
		if (!success)
		{
			this.getAttributes().removeEffect(Namesies.SELF_CONFUSION_EFFECT);
			this.getAttributes().resetCount();
		}
		
		this.getAttributes().setLastMoveUsed();
		
		if (reduce) 
		{
			this.getMove().reducePP(opp.hasAbility(Namesies.PRESSURE_ABILITY) ? 2 : 1);
		}
		
		this.getAttributes().setAttacking(false);
	}
	
	public boolean canBreed()
	{
		return !isEgg && pokemon.canBreed();
	}
}
