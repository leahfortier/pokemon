package battle;

import item.Item;
import item.berry.Berry;
import item.berry.GainableEffectBerry;
import item.hold.DriveItem;
import item.hold.GemItem;
import item.hold.HoldItem;
import item.hold.PlateItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import main.Global;
import main.Namesies;
import main.Type;
import pokemon.Ability;
import pokemon.ActivePokemon;
import pokemon.Gender;
import pokemon.Stat;
import trainer.Team;
import trainer.Trainer;
import trainer.Trainer.Action;
import trainer.WildPokemon;
import battle.MessageUpdate.Update;
import battle.effect.AccuracyBypassEffect;
import battle.effect.AdvantageMultiplier;
import battle.effect.ApplyDamageEffect;
import battle.effect.BarrierEffect;
import battle.effect.ChangeAbilityMove;
import battle.effect.ChangeTypeMove;
import battle.effect.CrashDamageMove;
import battle.effect.CritBlockerEffect;
import battle.effect.CritStageEffect;
import battle.effect.DefogRelease;
import battle.effect.Effect;
import battle.effect.Effect.CastSource;
import battle.effect.Effect.EffectType;
import battle.effect.EffectBlockerEffect;
import battle.effect.FaintEffect;
import battle.effect.IgnoreStageEffect;
import battle.effect.ItemCondition;
import battle.effect.MultiTurnMove;
import battle.effect.PassableEffect;
import battle.effect.PhysicalContactEffect;
import battle.effect.PokemonEffect;
import battle.effect.RapidSpinRelease;
import battle.effect.RecoilMove;
import battle.effect.SelfHealingMove;
import battle.effect.StatSwitchingEffect;
import battle.effect.Status;
import battle.effect.Status.StatusCondition;
import battle.effect.TakeDamageEffect;
import battle.effect.TargetSwapperEffect;

public abstract class Attack implements Serializable 
{
	private static final long serialVersionUID = 1L;
	
	private static HashMap<String, Attack> map; // Mappity map
	private static List<String> moveNames;

	protected Namesies namesies;
	protected String name;
	protected String description;
	protected int power;
	protected int accuracy;
	protected int pp;
	protected Type type;
	protected Category category;
	protected List<Effect> effects;
	protected int effectChance;
	protected StatusCondition status;
	protected List<MoveType> moveTypes;
	protected boolean selfTarget;
	protected int priority;
	protected int[] statChanges;
	protected boolean printCast;

	public static enum Category implements Serializable
	{
		PHYSICAL(0x23), 
		SPECIAL(0x24), 
		STATUS(0x25);

		private String name;
		private int imageNumber;

		private Category(int imgNum) 
		{
			imageNumber = imgNum;
			name = name().charAt(0) + name().substring(1).toLowerCase();
		}

		public String toString() 
		{
			return name;
		}

		public int getImageNumber() 
		{
			return imageNumber;
		}
	}
	
	public enum MoveType
	{
		AIRBORNE,
		ALWAYS_CRIT,
		ASLEEP_USER,
		ASSISTLESS,
		AURA_PULSE,
		BITING,
		BOMB_BALL,
		DEFROST,
		ENCORELESS,
		FIELD,
		METRONOMELESS,
		MIMICLESS,
		MIRRORLESS,
		NO_MAGIC_COAT,
		NON_SNATCHABLE,
		ONE_HIT_KO,
		PHYSICAL_CONTACT,
		POWDER,
		PROTECT_PIERCING,
		PUNCHING,
		SAP_75,
		SAP_HEALTH,
		SLEEP_TALK_FAIL,
		SOUND_BASED,
		SUCCESSIVE_DECAY,
		SUBSTITUTE_PIERCING,
		USER_FAINTS
	}

	public Attack(Namesies s, String d, int p, Type t, Category cat) 
	{
		namesies = s;
		name = s.getName();
		description = d;
		pp = p;
		type = t;
		category = cat;
		effects = new ArrayList<>();
		moveTypes = new ArrayList<>();
		power = 0;
		accuracy = 10000;
		selfTarget = false;
		priority = 0;
		status = StatusCondition.NONE;
		statChanges = new int[Stat.NUM_BATTLE_STATS];
		effectChance = 100;
		printCast = true;
	}

	public int getPriority(Battle b, ActivePokemon me) 
	{
		return priority;
	}
	
	public boolean isSelfTarget()
	{
		return selfTarget;
	}
	
	// Returns true if an attack has secondary effects -- this only applies to physical and special moves
	// Secondary effects include status conditions, confusing, flinching, and stat changes (unless the stat changes are negative for the user)
	public boolean hasSecondaryEffects()
	{
		// Effects are primary for status moves
		if (category == Category.STATUS) 
			return false;
		
		// If the effect may not necessarily occur, then it is secondary
		if (effectChance < 100) 
			return true;
		
		// Giving the target a status condition is a secondary effect
		if (status != StatusCondition.NONE) 
			return true;
		
		// Confusion and flinching count as secondary effects -- but I don't think anything else does?
		for (Effect e : effects)
		{
			if (e.namesies() == Namesies.CONFUSION_EFFECT || e.namesies() == Namesies.FLINCH_EFFECT)
			{
				return true;
			}
		}
		
		// Stat changes are considered to be secondary effects unless they are negative for the user
		for (int val : statChanges)
		{
			if (val < 0)
			{
				if (selfTarget)
				{
					continue;
				}
				
				return true;
			}
			
			if (val > 0)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o)
	{
		return accuracy;
	}
	
	public String getAccuracyString()
	{
		if (accuracy > 100) 
		{
			return "--";
		}
		
		return accuracy + "";
	}
	
	public Category getCategory()
	{
		return category;
	}
	
	public int getPP()
	{
		return pp;
	}
	
	public Namesies namesies()
	{
		return namesies;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public boolean isMoveType(MoveType moveType)
	{
		for (MoveType t : moveTypes)
			if (moveType == t) 
				return true;
		
		return false;
	}
	
	public boolean isMultiTurn(Battle b)
	{
		return this instanceof MultiTurnMove;
	}
	
	public Type getActualType()
	{
		return type;
	}
	
	public Type setType(Battle b, ActivePokemon user)	
	{
		return type;
	}
	
	public int getPower()
	{
		return power;	
	}
	
	public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
	{
		return power;
	}
	
	public String getPowerString()
	{
		return power == 0 ? "--" : power + "";
	}
	
	public void apply(ActivePokemon me, ActivePokemon o, Battle b)
	{
		ActivePokemon target = getTarget(b, me, o);
		
		// Don't do anything for moves that are uneffective
		if (!effective(b, me, target))
		{
			return;
		}
		
		// Physical and special attacks -- apply dat damage
		if (category != Category.STATUS) 
		{
			applyDamage(me, o, b);
		}
		
		// If you got it, flaunt it
		if (canApplyEffects(b, me, target))
		{
			applyEffects(b, me, target);
		}
	}
	
	private ActivePokemon getTarget(Battle b, ActivePokemon user, ActivePokemon opponent)
	{
		Object[] invokees = b.getEffectsList(opponent);
		Object swapTarget = Global.checkInvoke(true, user, invokees, TargetSwapperEffect.class, "swapTarget", b, user, opponent);
		if (swapTarget != null)
		{
			return selfTarget ? opponent : user;
		}
		
		return selfTarget ? user : opponent;
	}
	
	private boolean canApplyEffects(Battle b, ActivePokemon me, ActivePokemon o)
	{
		int chance = effectChance*(me.hasAbility(Namesies.SERENE_GRACE_ABILITY) ? 2 : 1); 
		if (Math.random()*100 >= chance) 
		{
			return false;
		}
		
		// Check the opponents effects and see if it will prevent effects from occurring
		Object[] list = b.getEffectsList(o);
		Object checkeroo = Global.checkInvoke(false, me, list, EffectBlockerEffect.class, "validMove", b, me, o);
		if (checkeroo != null)
		{
			return false;
		}
		
		// Sheer Force prevents the user from having secondary effects for its moves
		if (me.hasAbility(Namesies.SHEER_FORCE_ABILITY) && me.getAttack().hasSecondaryEffects()) 
		{
			return false;
		}
		
		return true;
	}
	
	private boolean zeroAdvantage(Battle b, ActivePokemon p, ActivePokemon opp)
	{
		if (Type.getAdvantage(p, opp, b) > 0) 
		{
			return false;
		}
		
		b.addMessage("It doesn't affect " + opp.getName() + "!");
		Global.invoke(new Object[] { p.getAttack() }, CrashDamageMove.class, "crash", b, p);
		
		return true;
	}
	
	// Takes type advantage, victim ability, and victim type into account to determine if the attack is effective
	public boolean effective(Battle b, ActivePokemon me, ActivePokemon o)
	{
		// Self-target moves and field moves don't need to take type advantage always work
		if (this.isSelfTarget() || this.isMoveType(MoveType.FIELD))
		{
			return true;
		}
		
		// Non-status moves (AND FUCKING THUNDER WAVE) -- need to check the type chart
		if ((this.category != Category.STATUS || this.namesies == Namesies.THUNDER_WAVE_ATTACK) && this.zeroAdvantage(b, me, o))
		{
			return false;
		}
		
		// Check if type or ability will block the attack
		if (Ability.blockAttack(b, me, o) || Type.blockAttack(b, me, o)) 
		{
			return false;
		}
		
		// You passed!!
		return true;
	}
	
	// Physical and Special moves -- do dat damage!
	public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
	{	
		// Print Advantage
		double adv = Type.getAdvantage(me, o, b);
		if (adv < 1) b.addMessage("It's not very effective...");
		else if (adv > 1) b.addMessage("It's super effective!");
		
		// Deal damage
		int damage = o.reduceHealth(b, b.damageCalc(me, o));
		
		// Check if target is fainted
		o.isFainted(b);
		
		if (me.isFainted(b))
		{
			return;
		}
		
		Object[] invokees = b.getEffectsList(me);
		
		// Apply a damage effect
		Global.invoke(invokees, ApplyDamageEffect.class, "applyDamageEffect", b, me, o, damage);
		
		if (me.isFainted(b))
		{
			return;
		}
		
		// Take Recoil Damage
		Global.invoke(new Object[] {this}, RecoilMove.class, "applyRecoil", b, me, damage);
	
		if (me.isFainted(b))
		{
			return;
		}
		
		// Sap the Health
		if (isMoveType(MoveType.SAP_HEALTH)) 
		{
			int sapAmount = (int)Math.ceil(damage*(this.isMoveType(MoveType.SAP_75) ? .75 : .5));
			me.sapHealth(o, sapAmount, b, true, this.namesies() == Namesies.DREAM_EATER_ATTACK);
		}
		
		invokees = b.getEffectsList(o);
		
		// Effects that apply when a Pokemon makes physical contact with them
		if (isMoveType(MoveType.PHYSICAL_CONTACT))
		{
			Global.invoke(invokees, PhysicalContactEffect.class, "contact", b, me, o);
		}
		
		// Effects that apply to the opponent when they take damage
		Global.invoke(invokees, TakeDamageEffect.class, "takeDamage", b, me, o);
		
		return;
	}
	
	public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
	{
		// Kill yourself!!
		if (isMoveType(MoveType.USER_FAINTS)) 
		{
			user.reduceHealthFraction(b, 1);
		}
		
		// Don't apply effects to a fainted Pokemon
		if (victim.isFainted(b)) 
		{
			return;
		}
		
		// Give Status Condition
		if (status != StatusCondition.NONE)
		{
			boolean success = Status.giveStatus(b, user, victim, status);
			if (!success && canPrintFail()) 
			{
				b.addMessage(Status.getFailMessage(b, user, victim, status));
			}
		}
		
		// Give Stat Changes
		victim.modifyStages(b, user, statChanges, CastSource.ATTACK);
		
		// Give additional effects
		for (Effect e : effects)
		{
			if (e.applies(b, user, victim, CastSource.ATTACK)) 
			{
				e.cast(b, user, victim, CastSource.ATTACK, canPrintCast());
			}
			else if (canPrintFail()) 
			{
				b.addMessage(e.getFailMessage(b, user, victim));
			}
		}
		
		// Heal yourself!!
		if (this instanceof SelfHealingMove) 
		{
			((SelfHealingMove)this).heal(user, victim, b);
		}
	}
	
	public boolean canPrintFail()
	{
		return effectChance == 100 && category == Category.STATUS;
	}
	
	public boolean canPrintCast()
	{
		return printCast;
	}
	
	// To be overridden if necessary
	public void startTurn(Battle b, ActivePokemon me) {}

	// ONLY CALL THIS FUNCTION IF YOU SRSLY LIKE NEED TO LIKE YOU'RE READING FROM A FILE OR SOMETHING OTHERWISE JUST FUCKING USE THE FUCKING NAMESIES I FUCKING MEAN IT AND EVEN IN THE SITUATION I SAID IT COULD BE USED IT BETTER FUCKING BE PRECEDED BY THE ISATTACK FUNCTION SRSRLY SRSLY SRSLY
	public static Attack getAttackFromName(String m)
	{
		if (isAttack(m)) 
		{
			return map.get(m);
		}

		Global.error("No such Move " + m);
		return null;
	}
	
	public static Attack getAttack(Namesies name)
	{
		return getAttackFromName(name.getName());
	}
	
	public static boolean isAttack(String m)
	{
		if (map == null)
		{
			loadMoves();
		}
		
		return map.containsKey(m);
	}

	// Create and load the Moves map if it doesn't already exist
	public static void loadMoves() 
	{
		if (map != null) 
		{
			return;
		}
		
		map = new HashMap<>();
		moveNames = new ArrayList<>();

		// EVERYTHING BELOW IS GENERATED ###

		// List all of the classes we are loading
		map.put("Tackle", new Tackle());
		map.put("Leech Seed", new LeechSeed());
		map.put("Thunder Wave", new ThunderWave());
		map.put("Poison Powder", new PoisonPowder());
		map.put("Sleep Powder", new SleepPowder());
		map.put("Toxic", new Toxic());
		map.put("Ember", new Ember());
		map.put("Growl", new Growl());
		map.put("Scratch", new Scratch());
		map.put("Vine Whip", new VineWhip());
		map.put("Sonic Boom", new SonicBoom());
		map.put("Smokescreen", new Smokescreen());
		map.put("Take Down", new TakeDown());
		map.put("Struggle", new Struggle());
		map.put("Razor Leaf", new RazorLeaf());
		map.put("Sweet Scent", new SweetScent());
		map.put("Growth", new Growth());
		map.put("Double-Edge", new DoubleEdge());
		map.put("Seed Bomb", new SeedBomb());
		map.put("Synthesis", new Synthesis());
		map.put("Recover", new Recover());
		map.put("Dragon Rage", new DragonRage());
		map.put("Scary Face", new ScaryFace());
		map.put("Fire Fang", new FireFang());
		map.put("Flame Burst", new FlameBurst());
		map.put("Bite", new Bite());
		map.put("Slash", new Slash());
		map.put("Tail Whip", new TailWhip());
		map.put("Solar Beam", new SolarBeam());
		map.put("Flamethrower", new Flamethrower());
		map.put("Fly", new Fly());
		map.put("Fire Spin", new FireSpin());
		map.put("Inferno", new Inferno());
		map.put("Dragon Claw", new DragonClaw());
		map.put("Shadow Claw", new ShadowClaw());
		map.put("Air Slash", new AirSlash());
		map.put("Wing Attack", new WingAttack());
		map.put("Heat Wave", new HeatWave());
		map.put("Flare Blitz", new FlareBlitz());
		map.put("Flash Cannon", new FlashCannon());
		map.put("Bubble", new Bubble());
		map.put("Withdraw", new Withdraw());
		map.put("Water Gun", new WaterGun());
		map.put("Rapid Spin", new RapidSpin());
		map.put("Reflect", new Reflect());
		map.put("Spiky Shield", new SpikyShield());
		map.put("King's Shield", new KingsShield());
		map.put("Protect", new Protect());
		map.put("Detect", new Detect());
		map.put("Quick Guard", new QuickGuard());
		map.put("Endure", new Endure());
		map.put("Water Pulse", new WaterPulse());
		map.put("ConfusionDamage", new ConfusionDamage());
		map.put("Confuse Ray", new ConfuseRay());
		map.put("Aqua Tail", new AquaTail());
		map.put("Skull Bash", new SkullBash());
		map.put("Iron Defense", new IronDefense());
		map.put("Hydro Pump", new HydroPump());
		map.put("Rain Dance", new RainDance());
		map.put("Sunny Day", new SunnyDay());
		map.put("Sandstorm", new Sandstorm());
		map.put("Hail", new Hail());
		map.put("Petal Dance", new PetalDance());
		map.put("Thrash", new Thrash());
		map.put("Hyper Beam", new HyperBeam());
		map.put("String Shot", new StringShot());
		map.put("Bug Bite", new BugBite());
		map.put("Harden", new Harden());
		map.put("Confusion", new Confusion());
		map.put("Stun Spore", new StunSpore());
		map.put("Gust", new Gust());
		map.put("Supersonic", new Supersonic());
		map.put("Psybeam", new Psybeam());
		map.put("Silver Wind", new SilverWind());
		map.put("Tailwind", new Tailwind());
		map.put("Morning Sun", new MorningSun());
		map.put("Safeguard", new Safeguard());
		map.put("Captivate", new Captivate());
		map.put("Bug Buzz", new BugBuzz());
		map.put("Quiver Dance", new QuiverDance());
		map.put("Encore", new Encore());
		map.put("Poison Sting", new PoisonSting());
		map.put("Fury Attack", new FuryAttack());
		map.put("False Swipe", new FalseSwipe());
		map.put("Disable", new Disable());
		map.put("Focus Energy", new FocusEnergy());
		map.put("Twineedle", new Twineedle());
		map.put("Rage", new Rage());
		map.put("Pursuit", new Pursuit());
		map.put("Toxic Spikes", new ToxicSpikes());
		map.put("Pin Missile", new PinMissile());
		map.put("Agility", new Agility());
		map.put("Assurance", new Assurance());
		map.put("Poison Jab", new PoisonJab());
		map.put("Endeavor", new Endeavor());
		map.put("Sand Attack", new SandAttack());
		map.put("Quick Attack", new QuickAttack());
		map.put("Twister", new Twister());
		map.put("Feather Dance", new FeatherDance());
		map.put("Roost", new Roost());
		map.put("Thunder Shock", new ThunderShock());
		map.put("Mirror Move", new MirrorMove());
		map.put("Hurricane", new Hurricane());
		map.put("Hyper Fang", new HyperFang());
		map.put("Sucker Punch", new SuckerPunch());
		map.put("Crunch", new Crunch());
		map.put("Super Fang", new SuperFang());
		map.put("Swords Dance", new SwordsDance());
		map.put("Peck", new Peck());
		map.put("Leer", new Leer());
		map.put("Aerial Ace", new AerialAce());
		map.put("Drill Peck", new DrillPeck());
		map.put("Pluck", new Pluck());
		map.put("Drill Run", new DrillRun());
		map.put("Wrap", new Wrap());
		map.put("Glare", new Glare());
		map.put("Screech", new Screech());
		map.put("Acid", new Acid());
		map.put("Stockpile", new Stockpile());
		map.put("Spit Up", new SpitUp());
		map.put("Swallow", new Swallow());
		map.put("Acid Spray", new AcidSpray());
		map.put("Mud Bomb", new MudBomb());
		map.put("Haze", new Haze());
		map.put("Coil", new Coil());
		map.put("Gunk Shot", new GunkShot());
		map.put("Ice Fang", new IceFang());
		map.put("Thunder Fang", new ThunderFang());
		map.put("Electro Ball", new ElectroBall());
		map.put("Double Team", new DoubleTeam());
		map.put("Slam", new Slam());
		map.put("Thunderbolt", new Thunderbolt());
		map.put("Feint", new Feint());
		map.put("Discharge", new Discharge());
		map.put("Light Screen", new LightScreen());
		map.put("Thunder", new Thunder());
		map.put("Defense Curl", new DefenseCurl());
		map.put("Swift", new Swift());
		map.put("Fury Swipes", new FurySwipes());
		map.put("Rollout", new Rollout());
		map.put("Fury Cutter", new FuryCutter());
		map.put("Sand Tomb", new SandTomb());
		map.put("Gyro Ball", new GyroBall());
		map.put("Crush Claw", new CrushClaw());
		map.put("Double Kick", new DoubleKick());
		map.put("Poison Tail", new PoisonTail());
		map.put("Flatter", new Flatter());
		map.put("Poison Fang", new PoisonFang());
		map.put("Chip Away", new ChipAway());
		map.put("Body Slam", new BodySlam());
		map.put("Earth Power", new EarthPower());
		map.put("Superpower", new Superpower());
		map.put("Horn Attack", new HornAttack());
		map.put("Horn Drill", new HornDrill());
		map.put("Megahorn", new Megahorn());
		map.put("Pound", new Pound());
		map.put("Sing", new Sing());
		map.put("Double Slap", new DoubleSlap());
		map.put("Wish", new Wish());
		map.put("Minimize", new Minimize());
		map.put("Wake-Up Slap", new WakeUpSlap());
		map.put("Cosmic Power", new CosmicPower());
		map.put("Lucky Chant", new LuckyChant());
		map.put("Metronome", new Metronome());
		map.put("Gravity", new Gravity());
		map.put("Moonlight", new Moonlight());
		map.put("Stored Power", new StoredPower());
		map.put("Mimic", new Mimic());
		map.put("Meteor Mash", new MeteorMash());
		map.put("Imprison", new Imprison());
		map.put("Will-O-Wisp", new WillOWisp());
		map.put("Payback", new Payback());
		map.put("Extrasensory", new Extrasensory());
		map.put("Fire Blast", new FireBlast());
		map.put("Nasty Plot", new NastyPlot());
		map.put("Round", new Round());
		map.put("Rest", new Rest());
		map.put("Hyper Voice", new HyperVoice());
		map.put("Leech Life", new LeechLife());
		map.put("Astonish", new Astonish());
		map.put("Air Cutter", new AirCutter());
		map.put("Mean Look", new MeanLook());
		map.put("Acrobatics", new Acrobatics());
		map.put("Absorb", new Absorb());
		map.put("Mega Drain", new MegaDrain());
		map.put("Natural Gift", new NaturalGift());
		map.put("Giga Drain", new GigaDrain());
		map.put("Aromatherapy", new Aromatherapy());
		map.put("Spore", new Spore());
		map.put("Cross Poison", new CrossPoison());
		map.put("X-Scissor", new XScissor());
		map.put("Foresight", new Foresight());
		map.put("Odor Sleuth", new OdorSleuth());
		map.put("Miracle Eye", new MiracleEye());
		map.put("Howl", new Howl());
		map.put("Signal Beam", new SignalBeam());
		map.put("Zen Headbutt", new ZenHeadbutt());
		map.put("Psychic", new Psychic());
		map.put("Mud-Slap", new MudSlap());
		map.put("Magnitude", new Magnitude());
		map.put("Bulldoze", new Bulldoze());
		map.put("Dig", new Dig());
		map.put("Earthquake", new Earthquake());
		map.put("Fissure", new Fissure());
		map.put("Night Slash", new NightSlash());
		map.put("Tri Attack", new TriAttack());
		map.put("Fake Out", new FakeOut());
		map.put("Feint Attack", new FeintAttack());
		map.put("Taunt", new Taunt());
		map.put("Pay Day", new PayDay());
		map.put("Power Gem", new PowerGem());
		map.put("Water Sport", new WaterSport());
		map.put("Soak", new Soak());
		map.put("Trick-or-Treat", new TrickOrTreat());
		map.put("Forest's Curse", new ForestsCurse());
		map.put("Psych Up", new PsychUp());
		map.put("Amnesia", new Amnesia());
		map.put("Wonder Room", new WonderRoom());
		map.put("Aqua Jet", new AquaJet());
		map.put("Covet", new Covet());
		map.put("Low Kick", new LowKick());
		map.put("Karate Chop", new KarateChop());
		map.put("Seismic Toss", new SeismicToss());
		map.put("Swagger", new Swagger());
		map.put("Cross Chop", new CrossChop());
		map.put("Punishment", new Punishment());
		map.put("Close Combat", new CloseCombat());
		map.put("Flame Wheel", new FlameWheel());
		map.put("Reversal", new Reversal());
		map.put("Extreme Speed", new ExtremeSpeed());
		map.put("Hypnosis", new Hypnosis());
		map.put("Bubble Beam", new BubbleBeam());
		map.put("Mud Shot", new MudShot());
		map.put("Belly Drum", new BellyDrum());
		map.put("Submission", new Submission());
		map.put("Dynamic Punch", new DynamicPunch());
		map.put("Mind Reader", new MindReader());
		map.put("Lock-On", new LockOn());
		map.put("Kinesis", new Kinesis());
		map.put("Barrier", new Barrier());
		map.put("Telekinesis", new Telekinesis());
		map.put("Ingrain", new Ingrain());
		map.put("Psycho Cut", new PsychoCut());
		map.put("Future Sight", new FutureSight());
		map.put("Doom Desire", new DoomDesire());
		map.put("Calm Mind", new CalmMind());
		map.put("Low Sweep", new LowSweep());
		map.put("Revenge", new Revenge());
		map.put("Vital Throw", new VitalThrow());
		map.put("Wring Out", new WringOut());
		map.put("Leaf Tornado", new LeafTornado());
		map.put("Leaf Storm", new LeafStorm());
		map.put("Leaf Blade", new LeafBlade());
		map.put("Constrict", new Constrict());
		map.put("Hex", new Hex());
		map.put("Sludge Wave", new SludgeWave());
		map.put("Mud Sport", new MudSport());
		map.put("Rock Polish", new RockPolish());
		map.put("Rock Throw", new RockThrow());
		map.put("Rock Blast", new RockBlast());
		map.put("Smack Down", new SmackDown());
		map.put("Stealth Rock", new StealthRock());
		map.put("Stone Edge", new StoneEdge());
		map.put("Steamroller", new Steamroller());
		map.put("Heavy Slam", new HeavySlam());
		map.put("Stomp", new Stomp());
		map.put("Flame Charge", new FlameCharge());
		map.put("Bounce", new Bounce());
		map.put("Curse", new Curse());
		map.put("Yawn", new Yawn());
		map.put("Headbutt", new Headbutt());
		map.put("Slack Off", new SlackOff());
		map.put("Heal Pulse", new HealPulse());
		map.put("Metal Sound", new MetalSound());
		map.put("Spark", new Spark());
		map.put("Magnet Bomb", new MagnetBomb());
		map.put("Mirror Shot", new MirrorShot());
		map.put("Magnet Rise", new MagnetRise());
		map.put("Zap Cannon", new ZapCannon());
		map.put("Brave Bird", new BraveBird());
		map.put("Uproar", new Uproar());
		map.put("Acupressure", new Acupressure());
		map.put("Double Hit", new DoubleHit());
		map.put("Icy Wind", new IcyWind());
		map.put("Ice Shard", new IceShard());
		map.put("Aqua Ring", new AquaRing());
		map.put("Aurora Beam", new AuroraBeam());
		map.put("Brine", new Brine());
		map.put("Dive", new Dive());
		map.put("Ice Beam", new IceBeam());
		map.put("Sheer Cold", new SheerCold());
		map.put("Poison Gas", new PoisonGas());
		map.put("Sludge", new Sludge());
		map.put("Sludge Bomb", new SludgeBomb());
		map.put("Acid Armor", new AcidArmor());
		map.put("Icicle Spear", new IcicleSpear());
		map.put("Clamp", new Clamp());
		map.put("Razor Shell", new RazorShell());
		map.put("Whirlpool", new Whirlpool());
		map.put("Shell Smash", new ShellSmash());
		map.put("Spike Cannon", new SpikeCannon());
		map.put("Spikes", new Spikes());
		map.put("Icicle Crash", new IcicleCrash());
		map.put("Lick", new Lick());
		map.put("Spite", new Spite());
		map.put("Night Shade", new NightShade());
		map.put("Shadow Ball", new ShadowBall());
		map.put("Dream Eater", new DreamEater());
		map.put("Dark Pulse", new DarkPulse());
		map.put("Nightmare", new Nightmare());
		map.put("Shadow Punch", new ShadowPunch());
		map.put("Bind", new Bind());
		map.put("Rock Tomb", new RockTomb());
		map.put("Dragon Breath", new DragonBreath());
		map.put("Iron Tail", new IronTail());
		map.put("Meditate", new Meditate());
		map.put("Synchronoise", new Synchronoise());
		map.put("Psyshock", new Psyshock());
		map.put("Vice Grip", new ViceGrip());
		map.put("Metal Claw", new MetalClaw());
		map.put("Guillotine", new Guillotine());
		map.put("Crabhammer", new Crabhammer());
		map.put("Flail", new Flail());
		map.put("Charge", new Charge());
		map.put("Charge Beam", new ChargeBeam());
		map.put("Mirror Coat", new MirrorCoat());
		map.put("Counter", new Counter());
		map.put("Barrage", new Barrage());
		map.put("Bullet Seed", new BulletSeed());
		map.put("Egg Bomb", new EggBomb());
		map.put("Wood Hammer", new WoodHammer());
		map.put("Bone Club", new BoneClub());
		map.put("Bonemerang", new Bonemerang());
		map.put("Bone Rush", new BoneRush());
		map.put("Rolling Kick", new RollingKick());
		map.put("Jump Kick", new JumpKick());
		map.put("Brick Break", new BrickBreak());
		map.put("High Jump Kick", new HighJumpKick());
		map.put("Blaze Kick", new BlazeKick());
		map.put("Mega Kick", new MegaKick());
		map.put("Comet Punch", new CometPunch());
		map.put("Mach Punch", new MachPunch());
		map.put("Bullet Punch", new BulletPunch());
		map.put("Vacuum Wave", new VacuumWave());
		map.put("Thunder Punch", new ThunderPunch());
		map.put("Ice Punch", new IcePunch());
		map.put("Fire Punch", new FirePunch());
		map.put("Sky Uppercut", new SkyUppercut());
		map.put("Mega Punch", new MegaPunch());
		map.put("Focus Punch", new FocusPunch());
		map.put("Me First", new MeFirst());
		map.put("Refresh", new Refresh());
		map.put("Power Whip", new PowerWhip());
		map.put("Smog", new Smog());
		map.put("Clear Smog", new ClearSmog());
		map.put("Hammer Arm", new HammerArm());
		map.put("Soft-Boiled", new SoftBoiled());
		map.put("Ancient Power", new AncientPower());
		map.put("Tickle", new Tickle());
		map.put("Dizzy Punch", new DizzyPunch());
		map.put("Outrage", new Outrage());
		map.put("Dragon Dance", new DragonDance());
		map.put("Dragon Pulse", new DragonPulse());
		map.put("Draco Meteor", new DracoMeteor());
		map.put("Waterfall", new Waterfall());
		map.put("Reflect Type", new ReflectType());
		map.put("Magical Leaf", new MagicalLeaf());
		map.put("Power Swap", new PowerSwap());
		map.put("Guard Swap", new GuardSwap());
		map.put("Copycat", new Copycat());
		map.put("Transform", new Transform());
		map.put("Substitute", new Substitute());
		map.put("Razor Wind", new RazorWind());
		map.put("Lovely Kiss", new LovelyKiss());
		map.put("Powder Snow", new PowderSnow());
		map.put("Heart Stamp", new HeartStamp());
		map.put("Fake Tears", new FakeTears());
		map.put("Avalanche", new Avalanche());
		map.put("Blizzard", new Blizzard());
		map.put("Shock Wave", new ShockWave());
		map.put("Lava Plume", new LavaPlume());
		map.put("Work Up", new WorkUp());
		map.put("Giga Impact", new GigaImpact());
		map.put("Splash", new Splash());
		map.put("Mist", new Mist());
		map.put("Last Resort", new LastResort());
		map.put("Trump Card", new TrumpCard());
		map.put("Muddy Water", new MuddyWater());
		map.put("Conversion", new Conversion());
		map.put("Conversion 2", new Conversion2());
		map.put("Sharpen", new Sharpen());
		map.put("Magic Coat", new MagicCoat());
		map.put("Sky Drop", new SkyDrop());
		map.put("Iron Head", new IronHead());
		map.put("Rock Slide", new RockSlide());
		map.put("Snore", new Snore());
		map.put("Sleep Talk", new SleepTalk());
		map.put("Block", new Block());
		map.put("Sky Attack", new SkyAttack());
		map.put("Dragon Rush", new DragonRush());
		map.put("Aura Sphere", new AuraSphere());
		map.put("Psystrike", new Psystrike());
		map.put("Eruption", new Eruption());
		map.put("Charm", new Charm());
		map.put("Echoed Voice", new EchoedVoice());
		map.put("Psycho Shift", new PsychoShift());
		map.put("Shadow Sneak", new ShadowSneak());
		map.put("Spider Web", new SpiderWeb());
		map.put("Sweet Kiss", new SweetKiss());
		map.put("Ominous Wind", new OminousWind());
		map.put("Cotton Spore", new CottonSpore());
		map.put("Cotton Guard", new CottonGuard());
		map.put("Grass Whistle", new GrassWhistle());
		map.put("Torment", new Torment());
		map.put("Hidden Power", new HiddenPower());
		map.put("Psywave", new Psywave());
		map.put("Pain Split", new PainSplit());
		map.put("Bide", new Bide());
		map.put("Autotomize", new Autotomize());
		map.put("Struggle Bug", new StruggleBug());
		map.put("Power Trick", new PowerTrick());
		map.put("Power Split", new PowerSplit());
		map.put("Guard Split", new GuardSplit());
		map.put("Hone Claws", new HoneClaws());
		map.put("Beat Up", new BeatUp());
		map.put("Octazooka", new Octazooka());
		map.put("Present", new Present());
		map.put("Steel Wing", new SteelWing());
		map.put("Sketch", new Sketch());
		map.put("Triple Kick", new TripleKick());
		map.put("Milk Drink", new MilkDrink());
		map.put("Heal Bell", new HealBell());
		map.put("Weather Ball", new WeatherBall());
		map.put("Aeroblast", new Aeroblast());
		map.put("Sacred Fire", new SacredFire());
		map.put("Heal Block", new HealBlock());
		map.put("Energy Ball", new EnergyBall());
		map.put("Bulk Up", new BulkUp());
		map.put("Thief", new Thief());
		map.put("Attract", new Attract());
		map.put("Force Palm", new ForcePalm());
		map.put("Arm Thrust", new ArmThrust());
		map.put("Smelling Salts", new SmellingSalts());
		map.put("Assist", new Assist());
		map.put("Metal Burst", new MetalBurst());
		map.put("Wild Charge", new WildCharge());
		map.put("Flash", new Flash());
		map.put("Tail Glow", new TailGlow());
		map.put("Water Spout", new WaterSpout());
		map.put("Teeter Dance", new TeeterDance());
		map.put("Needle Arm", new NeedleArm());
		map.put("Venoshock", new Venoshock());
		map.put("Snatch", new Snatch());
		map.put("Ice Ball", new IceBall());
		map.put("Head Smash", new HeadSmash());
		map.put("Mist Ball", new MistBall());
		map.put("Luster Purge", new LusterPurge());
		map.put("Psycho Boost", new PsychoBoost());
		map.put("Facade", new Facade());
		map.put("Defend Order", new DefendOrder());
		map.put("Heal Order", new HealOrder());
		map.put("Attack Order", new AttackOrder());
		map.put("Chatter", new Chatter());
		map.put("Dual Chop", new DualChop());
		map.put("Rock Wrecker", new RockWrecker());
		map.put("Trick Room", new TrickRoom());
		map.put("Roar Of Time", new RoarOfTime());
		map.put("Spacial Rend", new SpacialRend());
		map.put("Magma Storm", new MagmaStorm());
		map.put("Crush Grip", new CrushGrip());
		map.put("Shadow Force", new ShadowForce());
		map.put("Heart Swap", new HeartSwap());
		map.put("Dark Void", new DarkVoid());
		map.put("Seed Flare", new SeedFlare());
		map.put("Judgment", new Judgment());
		map.put("Searing Shot", new SearingShot());
		map.put("Incinerate", new Incinerate());
		map.put("Overheat", new Overheat());
		map.put("Heat Crash", new HeatCrash());
		map.put("Grass Knot", new GrassKnot());
		map.put("Scald", new Scald());
		map.put("Drain Punch", new DrainPunch());
		map.put("Storm Throw", new StormThrow());
		map.put("Frost Breath", new FrostBreath());
		map.put("Rock Smash", new RockSmash());
		map.put("Rock Climb", new RockClimb());
		map.put("Night Daze", new NightDaze());
		map.put("Tail Slap", new TailSlap());
		map.put("Defog", new Defog());
		map.put("Horn Leech", new HornLeech());
		map.put("Electroweb", new Electroweb());
		map.put("Gear Grind", new GearGrind());
		map.put("Shift Gear", new ShiftGear());
		map.put("Head Charge", new HeadCharge());
		map.put("Fiery Dance", new FieryDance());
		map.put("Sacred Sword", new SacredSword());
		map.put("Secret Sword", new SecretSword());
		map.put("Fusion Flare", new FusionFlare());
		map.put("Fusion Bolt", new FusionBolt());
		map.put("Blue Flare", new BlueFlare());
		map.put("Bolt Strike", new BoltStrike());
		map.put("Glaciate", new Glaciate());
		map.put("Techno Blast", new TechnoBlast());
		map.put("Explosion", new Explosion());
		map.put("Self-Destruct", new SelfDestruct());
		map.put("Fling", new Fling());
		map.put("Freeze Shock", new FreezeShock());
		map.put("Secret Power", new SecretPower());
		map.put("Final Gambit", new FinalGambit());
		map.put("Gastro Acid", new GastroAcid());
		map.put("Healing Wish", new HealingWish());
		map.put("Lunar Dance", new LunarDance());
		map.put("Roar", new Roar());
		map.put("Grudge", new Grudge());
		map.put("Retaliate", new Retaliate());
		map.put("Circle Throw", new CircleThrow());
		map.put("Teleport", new Teleport());
		map.put("Role Play", new RolePlay());
		map.put("Knock Off", new KnockOff());
		map.put("Whirlwind", new Whirlwind());
		map.put("Bestow", new Bestow());
		map.put("Switcheroo", new Switcheroo());
		map.put("Trick", new Trick());
		map.put("Memento", new Memento());
		map.put("Destiny Bond", new DestinyBond());
		map.put("Camouflage", new Camouflage());
		map.put("Recycle", new Recycle());
		map.put("Parting Shot", new PartingShot());
		map.put("U-turn", new UTurn());
		map.put("Baton Pass", new BatonPass());
		map.put("Perish Song", new PerishSong());
		map.put("Dragon Tail", new DragonTail());
		map.put("Foul Play", new FoulPlay());
		map.put("Embargo", new Embargo());
		map.put("Nature Power", new NaturePower());
		map.put("Entrainment", new Entrainment());
		map.put("Magic Room", new MagicRoom());
		map.put("Worry Seed", new WorrySeed());
		map.put("Simple Beam", new SimpleBeam());
		map.put("Skill Swap", new SkillSwap());
		map.put("Volt Switch", new VoltSwitch());
		map.put("Relic Song", new RelicSong());
		map.put("Snarl", new Snarl());
		map.put("Ice Burn", new IceBurn());
		map.put("V-create", new VCreate());
		map.put("Surf", new Surf());
		map.put("Volt Tackle", new VoltTackle());
		map.put("Focus Blast", new FocusBlast());
		map.put("Diamond Storm", new DiamondStorm());
		map.put("Moonblast", new Moonblast());
		map.put("Land's Wrath", new LandsWrath());
		map.put("Phantom Force", new PhantomForce());
		map.put("Oblivion Wing", new OblivionWing());
		map.put("Geomancy", new Geomancy());
		map.put("Boomburst", new Boomburst());
		map.put("Play Rough", new PlayRough());
		map.put("Crafty Shield", new CraftyShield());
		map.put("Nuzzle", new Nuzzle());
		map.put("Draining Kiss", new DrainingKiss());
		map.put("Fairy Wind", new FairyWind());
		map.put("Parabolic Charge", new ParabolicCharge());
		map.put("Disarming Voice", new DisarmingVoice());
		map.put("Freeze-Dry", new FreezeDry());
		map.put("Flying Press", new FlyingPress());
		map.put("Topsy-Turvy", new TopsyTurvy());
		map.put("Play Nice", new PlayNice());
		map.put("Eerie Impulse", new EerieImpulse());
		map.put("Misty Terrain", new MistyTerrain());
		map.put("Fairy Lock", new FairyLock());
		map.put("Aromatic Mist", new AromaticMist());
		map.put("Baby-Doll Eyes", new BabyDollEyes());
		map.put("Petal Blizzard", new PetalBlizzard());
		map.put("Grassy Terrain", new GrassyTerrain());
		map.put("Flower Shield", new FlowerShield());
		map.put("Noble Roar", new NobleRoar());
		map.put("Powder", new Powder());
		map.put("Rototiller", new Rototiller());
		map.put("Water Shuriken", new WaterShuriken());
		map.put("Mat Block", new MatBlock());
		map.put("Mystical Fire", new MysticalFire());
		map.put("Infestation", new Infestation());
		map.put("Electrify", new Electrify());
		map.put("Fell Stinger", new FellStinger());
		map.put("Magnetic Flux", new MagneticFlux());
		map.put("Sticky Web", new StickyWeb());
		map.put("Belch", new Belch());
		map.put("Venom Drench", new VenomDrench());
		map.put("Electric Terrain", new ElectricTerrain());
		map.put("Power-Up Punch", new PowerUpPunch());
		map.put("Confide", new Confide());
		map.put("Cut", new Cut());
		map.put("Dazzling Gleam", new DazzlingGleam());
		map.put("Strength", new Strength());

		for (String s : map.keySet())
		{
			moveNames.add(s);
		}
	}

	/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

	private static class Tackle extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Tackle()
		{
			super(Namesies.TACKLE_ATTACK, "A physical attack in which the user charges and slams into the target with its whole body.", 35, Type.NORMAL, Category.PHYSICAL);
			super.power = 35;
			super.accuracy = 95;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class LeechSeed extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public LeechSeed()
		{
			super(Namesies.LEECH_SEED_ATTACK, "A seed is planted on the target. It steals some HP from the target every turn.", 10, Type.GRASS, Category.STATUS);
			super.accuracy = 90;
			super.effects.add(Effect.getEffect(Namesies.LEECH_SEED_EFFECT, EffectType.POKEMON));
		}
	}

	private static class ThunderWave extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public ThunderWave()
		{
			super(Namesies.THUNDER_WAVE_ATTACK, "A weak electric charge is launched at the target. It causes paralysis if it hits.", 20, Type.ELECTRIC, Category.STATUS);
			super.accuracy = 100;
			super.status = StatusCondition.PARALYZED;
		}
	}

	private static class PoisonPowder extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PoisonPowder()
		{
			super(Namesies.POISON_POWDER_ATTACK, "The user scatters a cloud of poisonous dust on the target. It may poison the target.", 35, Type.POISON, Category.STATUS);
			super.accuracy = 75;
			super.status = StatusCondition.POISONED;
			super.moveTypes.add(MoveType.POWDER);
		}
	}

	private static class SleepPowder extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SleepPowder()
		{
			super(Namesies.SLEEP_POWDER_ATTACK, "The user scatters a big cloud of sleep-inducing dust around the target.", 15, Type.GRASS, Category.STATUS);
			super.accuracy = 75;
			super.status = StatusCondition.ASLEEP;
			super.moveTypes.add(MoveType.POWDER);
		}
	}

	private static class Toxic extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public Toxic()
		{
			super(Namesies.TOXIC_ATTACK, "A move that leaves the target badly poisoned. Its poison damage worsens every turn.", 10, Type.POISON, Category.STATUS);
			super.accuracy = 90;
			super.effects.add(Effect.getEffect(Namesies.BAD_POISON_EFFECT, EffectType.POKEMON));
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			// Poison-type Pokemon bypass accuracy
			return attacking.isType(b, Type.POISON);
		}
	}

	private static class Ember extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Ember()
		{
			super(Namesies.EMBER_ATTACK, "The target is attacked with small flames. It may also leave the target with a burn.", 25, Type.FIRE, Category.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.BURNED;
		}
	}

	private static class Growl extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Growl()
		{
			super(Namesies.GROWL_ATTACK, "The user growls in an endearing way, making the opposing team less wary. The foes' Attack stats are lowered.", 40, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.statChanges[Stat.ATTACK.index()] = -1;
		}
	}

	private static class Scratch extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Scratch()
		{
			super(Namesies.SCRATCH_ATTACK, "Hard, pointed, and sharp claws rake the target to inflict damage.", 35, Type.NORMAL, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class VineWhip extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public VineWhip()
		{
			super(Namesies.VINE_WHIP_ATTACK, "The target is struck with slender, whiplike vines to inflict damage.", 25, Type.GRASS, Category.PHYSICAL);
			super.power = 45;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class SonicBoom extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SonicBoom()
		{
			super(Namesies.SONIC_BOOM_ATTACK, "The target is hit with a destructive shock wave that always inflicts 20 HP damage.", 20, Type.NORMAL, Category.SPECIAL);
			super.accuracy = 90;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			o.reduceHealth(b, 20);
		}
	}

	private static class Smokescreen extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Smokescreen()
		{
			super(Namesies.SMOKESCREEN_ATTACK, "The user releases an obscuring cloud of smoke or ink. It reduces the target's accuracy.", 20, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	private static class TakeDown extends Attack implements RecoilMove
	{
		private static final long serialVersionUID = 1L;

		public TakeDown()
		{
			super(Namesies.TAKE_DOWN_ATTACK, "A reckless, full-body charge attack for slamming into the target. It also damages the user a little.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 90;
			super.accuracy = 85;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyRecoil(Battle b, ActivePokemon user, Integer damage)
		{
			if (user.hasAbility(Namesies.ROCK_HEAD_ABILITY) || user.hasAbility(Namesies.MAGIC_GUARD_ABILITY))
			{
				return;
			}
			
			b.addMessage(user.getName() + " was hurt by recoil!");
			user.reduceHealth(b, (int)Math.ceil(damage/4.0));
		}
	}

	private static class Struggle extends Attack implements RecoilMove
	{
		private static final long serialVersionUID = 1L;

		public Struggle()
		{
			super(Namesies.STRUGGLE_ATTACK, "An attack that is used in desperation only if the user has no PP. It also hurts the user slightly.", 1, Type.NONE, Category.PHYSICAL);
			super.power = 50;
			super.moveTypes.add(MoveType.ENCORELESS);
			super.moveTypes.add(MoveType.MIMICLESS);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyRecoil(Battle b, ActivePokemon user, Integer damage)
		{
			b.addMessage(user.getName() + " was hurt by recoil!");
			user.reduceHealth(b, user.getMaxHP()/4);
		}
	}

	private static class RazorLeaf extends Attack implements CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public RazorLeaf()
		{
			super(Namesies.RAZOR_LEAF_ATTACK, "Sharp-edged leaves are launched to slash at the opposing team. Critical hits land more easily.", 25, Type.GRASS, Category.PHYSICAL);
			super.power = 55;
			super.accuracy = 95;
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}
	}

	private static class SweetScent extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SweetScent()
		{
			super(Namesies.SWEET_SCENT_ATTACK, "A sweet scent that lowers the opposing team's evasiveness. It also lures wild Pok\u00e9mon if used in grass, etc.", 20, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.EVASION.index()] = -2;
		}
	}

	private static class Growth extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Growth()
		{
			super(Namesies.GROWTH_ATTACK, "The user's body grows all at once, raising the Attack and Sp. Atk stats.", 20, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			// Doubles stat changes in the sunlight
			if (b.getWeather().namesies() == Namesies.SUNNY_EFFECT)
			{
				int[] statChanges = super.statChanges.clone();
				for (int i = 0; i < super.statChanges.length; i++)
				{
					if (super.statChanges[i] != 0)
					{
						super.statChanges[i] *= 2;
					}
				}
				
				super.applyEffects(b, user, victim);
				super.statChanges = statChanges;
				return;
			}
			
			super.applyEffects(b, user, victim);
		}
	}

	private static class DoubleEdge extends Attack implements RecoilMove
	{
		private static final long serialVersionUID = 1L;

		public DoubleEdge()
		{
			super(Namesies.DOUBLE_EDGE_ATTACK, "A reckless, life-risking tackle. It also damages the user by a fairly large amount, however.", 15, Type.NORMAL, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyRecoil(Battle b, ActivePokemon user, Integer damage)
		{
			if (user.hasAbility(Namesies.ROCK_HEAD_ABILITY) || user.hasAbility(Namesies.MAGIC_GUARD_ABILITY))
			{
				return;
			}
			
			b.addMessage(user.getName() + " was hurt by recoil!");
			user.reduceHealth(b, (int)Math.ceil(damage/3.0));
		}
	}

	private static class SeedBomb extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SeedBomb()
		{
			super(Namesies.SEED_BOMB_ATTACK, "The user slams a barrage of hard-shelled seeds down on the target from above.", 15, Type.GRASS, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.BOMB_BALL);
		}
	}

	private static class Synthesis extends Attack implements SelfHealingMove
	{
		private static final long serialVersionUID = 1L;

		public Synthesis()
		{
			super(Namesies.SYNTHESIS_ATTACK, "The user restores its own HP. The amount of HP regained varies with the weather.", 5, Type.GRASS, Category.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect(Namesies.HEAL_BLOCK_EFFECT))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			switch (b.getWeather().namesies())
			{
				case CLEAR_SKIES_EFFECT:
				victim.healHealthFraction(1/2.0);
				break;
				case SUNNY_EFFECT:
				victim.healHealthFraction(2/3.0);
				break;
				case HAILING_EFFECT:
				case RAINING_EFFECT:
				case SANDSTORM_EFFECT:
				victim.healHealthFraction(1/4.0);
				break;
				default:
				Global.error("Funky weather problems!!!!");
				break;
			}
			
			b.addMessage(victim.getName() + "'s health was restored!", victim);
		}
	}

	private static class Recover extends Attack implements SelfHealingMove
	{
		private static final long serialVersionUID = 1L;

		public Recover()
		{
			super(Namesies.RECOVER_ATTACK, "Restoring its own cells, the user restores its own HP by half of its max HP.", 10, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect(Namesies.HEAL_BLOCK_EFFECT))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			victim.healHealthFraction(1/2.0);
			
			b.addMessage(victim.getName() + "'s health was restored!", victim);
		}
	}

	private static class DragonRage extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DragonRage()
		{
			super(Namesies.DRAGON_RAGE_ATTACK, "This attack hits the target with a shock wave of pure rage. This attack always inflicts 40 HP damage.", 10, Type.DRAGON, Category.SPECIAL);
			super.accuracy = 100;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			o.reduceHealth(b, 40);
		}
	}

	private static class ScaryFace extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public ScaryFace()
		{
			super(Namesies.SCARY_FACE_ATTACK, "The user frightens the target with a scary face to harshly reduce its Speed stat.", 10, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.SPEED.index()] = -2;
		}
	}

	private static class FireFang extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FireFang()
		{
			super(Namesies.FIRE_FANG_ATTACK, "The user bites with flame-cloaked fangs. It may also make the target flinch or leave it burned.", 15, Type.FIRE, Category.PHYSICAL);
			super.power = 65;
			super.accuracy = 95;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 20;
			super.moveTypes.add(MoveType.BITING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			// If the effect is being applied, 50/50 chance to give a status condition vs. flinching
			if (Math.random() < .5)
			{
				Status.giveStatus(b, user, victim, StatusCondition.BURNED);
				return;
			}
			
			super.applyEffects(b, user, victim);
		}
	}

	private static class FlameBurst extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FlameBurst()
		{
			super(Namesies.FLAME_BURST_ATTACK, "The user attacks the target with a bursting flame.", 15, Type.FIRE, Category.SPECIAL);
			super.power = 70;
			super.accuracy = 100;
		}
	}

	private static class Bite extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Bite()
		{
			super(Namesies.BITE_ATTACK, "The target is bitten with viciously sharp fangs. It may make the target flinch.", 25, Type.DARK, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 30;
			super.moveTypes.add(MoveType.BITING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Slash extends Attack implements CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public Slash()
		{
			super(Namesies.SLASH_ATTACK, "The target is attacked with a slash of claws or blades. Critical hits land more easily.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}
	}

	private static class TailWhip extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public TailWhip()
		{
			super(Namesies.TAIL_WHIP_ATTACK, "The user wags its tail cutely, making opposing Pok\u00e9mon less wary and lowering their Defense stat.", 30, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.DEFENSE.index()] = -1;
		}
	}

	private static class SolarBeam extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;

		public SolarBeam()
		{
			super(Namesies.SOLAR_BEAM_ATTACK, "A two-turn attack. The user gathers light, then blasts a bundled beam on the second turn.", 10, Type.GRASS, Category.SPECIAL);
			super.power = 120;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
		}

		public boolean isMultiTurn(Battle b)
		{
			return b.getWeather().namesies() != Namesies.SUNNY_EFFECT;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			switch (b.getWeather().namesies())
			{
				case SUNNY_EFFECT:
				case CLEAR_SKIES_EFFECT:
				return super.power;
				case HAILING_EFFECT:
				case RAINING_EFFECT:
				case SANDSTORM_EFFECT:
				return super.power/2;
				default:
				Global.error("Funky weather problems!");
				return -1;
			}
		}

		public boolean chargesFirst()
		{
			return true;
		}

		public boolean semiInvulnerability()
		{
			return false;
		}

		public void charge(ActivePokemon user, Battle b)
		{
			b.addMessage(getChargeMessage(user));
		}

		public String getChargeMessage(ActivePokemon user)
		{
			return user.getName() + " began taking in sunlight!";
		}
	}

	private static class Flamethrower extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Flamethrower()
		{
			super(Namesies.FLAMETHROWER_ATTACK, "The target is scorched with an intense blast of fire. It may also leave the target with a burn.", 15, Type.FIRE, Category.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.BURNED;
		}
	}

	private static class Fly extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;

		public Fly()
		{
			super(Namesies.FLY_ATTACK, "The user soars, then strikes its target on the second turn. It can also be used for flying to any familiar town.", 15, Type.FLYING, Category.PHYSICAL);
			super.power = 90;
			super.accuracy = 95;
			super.moveTypes.add(MoveType.AIRBORNE);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public boolean chargesFirst()
		{
			return true;
		}

		public boolean semiInvulnerability()
		{
			return true;
		}

		public void charge(ActivePokemon user, Battle b)
		{
			b.addMessage(getChargeMessage(user));
		}

		public String getChargeMessage(ActivePokemon user)
		{
			return user.getName() + " flew up high!";
		}
	}

	private static class FireSpin extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FireSpin()
		{
			super(Namesies.FIRE_SPIN_ATTACK, "The target becomes trapped within a fierce vortex of fire that rages for four to five turns.", 15, Type.FIRE, Category.SPECIAL);
			super.power = 35;
			super.accuracy = 85;
			super.effects.add(Effect.getEffect(Namesies.FIRE_SPIN_EFFECT, EffectType.POKEMON));
		}
	}

	private static class Inferno extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Inferno()
		{
			super(Namesies.INFERNO_ATTACK, "The user attacks by engulfing the target in an intense fire. It leaves the target with a burn.", 5, Type.FIRE, Category.SPECIAL);
			super.power = 100;
			super.accuracy = 50;
			super.status = StatusCondition.BURNED;
		}
	}

	private static class DragonClaw extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DragonClaw()
		{
			super(Namesies.DRAGON_CLAW_ATTACK, "The user slashes the target with huge, sharp claws.", 15, Type.DRAGON, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class ShadowClaw extends Attack implements CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public ShadowClaw()
		{
			super(Namesies.SHADOW_CLAW_ATTACK, "The user slashes with a sharp claw made from shadows. Critical hits land more easily.", 15, Type.GHOST, Category.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}
	}

	private static class AirSlash extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public AirSlash()
		{
			super(Namesies.AIR_SLASH_ATTACK, "The user attacks with a blade of air that slices even the sky. It may also make the target flinch.", 15, Type.FLYING, Category.SPECIAL);
			super.power = 75;
			super.accuracy = 95;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 30;
		}
	}

	private static class WingAttack extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public WingAttack()
		{
			super(Namesies.WING_ATTACK_ATTACK, "The target is struck with large, imposing wings spread wide to inflict damage.", 35, Type.FLYING, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class HeatWave extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public HeatWave()
		{
			super(Namesies.HEAT_WAVE_ATTACK, "The user attacks by exhaling hot breath on the opposing team. It may also leave targets with a burn.", 10, Type.FIRE, Category.SPECIAL);
			super.power = 95;
			super.accuracy = 90;
			super.effectChance = 10;
			super.status = StatusCondition.BURNED;
		}
	}

	private static class FlareBlitz extends Attack implements RecoilMove
	{
		private static final long serialVersionUID = 1L;

		public FlareBlitz()
		{
			super(Namesies.FLARE_BLITZ_ATTACK, "The user cloaks itself in fire and charges at the target. The user sustains serious damage and may leave the target burned.", 15, Type.FIRE, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.BURNED;
			super.moveTypes.add(MoveType.DEFROST);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyRecoil(Battle b, ActivePokemon user, Integer damage)
		{
			if (user.hasAbility(Namesies.ROCK_HEAD_ABILITY) || user.hasAbility(Namesies.MAGIC_GUARD_ABILITY))
			{
				return;
			}
			
			b.addMessage(user.getName() + " was hurt by recoil!");
			user.reduceHealth(b, (int)Math.ceil(damage/3.0));
		}
	}

	private static class FlashCannon extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FlashCannon()
		{
			super(Namesies.FLASH_CANNON_ATTACK, "The user gathers all its light energy and releases it at once. It may also lower the target's Sp. Def stat.", 10, Type.STEEL, Category.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.effectChance = 10;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
		}
	}

	private static class Bubble extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Bubble()
		{
			super(Namesies.BUBBLE_ATTACK, "A spray of countless bubbles is jetted at the opposing team. It may also lower the targets' Speed stats.", 30, Type.WATER, Category.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.effectChance = 10;
			super.statChanges[Stat.SPEED.index()] = -1;
		}
	}

	private static class Withdraw extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Withdraw()
		{
			super(Namesies.WITHDRAW_ATTACK, "The user withdraws its body into its hard shell, raising its Defense stat.", 40, Type.WATER, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 1;
		}
	}

	private static class WaterGun extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public WaterGun()
		{
			super(Namesies.WATER_GUN_ATTACK, "The target is blasted with a forceful shot of water.", 25, Type.WATER, Category.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
		}
	}

	private static class RapidSpin extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public RapidSpin()
		{
			super(Namesies.RAPID_SPIN_ATTACK, "A spin attack that can also eliminate such moves as Bind, Wrap, Leech Seed, and Spikes.", 40, Type.NORMAL, Category.PHYSICAL);
			super.power = 20;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Global.invoke(b.getEffectsList(user), RapidSpinRelease.class, "releaseRapidSpin", b, user);
		}
	}

	private static class Reflect extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Reflect()
		{
			super(Namesies.REFLECT_ATTACK, "A wondrous wall of light is put up to suppress damage from physical attacks for five turns.", 20, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.REFLECT_EFFECT, EffectType.TEAM));
			super.selfTarget = true;
		}
	}

	private static class SpikyShield extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SpikyShield()
		{
			super(Namesies.SPIKY_SHIELD_ATTACK, "In addition to protecting the user from attacks, this move also damages any attacker who makes direct contact.", 10, Type.GRASS, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.SPIKY_SHIELD_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.SUCCESSIVE_DECAY);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
			super.priority = 4;
		}
	}

	private static class KingsShield extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public KingsShield()
		{
			super(Namesies.KINGS_SHIELD_ATTACK, "The user takes a defensive stance while it protects itself from damage. It also harshly lowers the Attack stat of any attacker who makes direct contact.", 10, Type.STEEL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.KINGS_SHIELD_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.SUCCESSIVE_DECAY);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
			super.priority = 4;
		}
	}

	private static class Protect extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Protect()
		{
			super(Namesies.PROTECT_ATTACK, "It enables the user to evade all attacks. Its chance of failing rises if it is used in succession.", 10, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.PROTECTING_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.SUCCESSIVE_DECAY);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
			super.priority = 4;
		}
	}

	private static class Detect extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Detect()
		{
			super(Namesies.DETECT_ATTACK, "It enables the user to evade all attacks. Its chance of failing rises if it is used in succession.", 5, Type.FIGHTING, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.PROTECTING_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.SUCCESSIVE_DECAY);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
			super.priority = 4;
		}
	}

	private static class QuickGuard extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public QuickGuard()
		{
			super(Namesies.QUICK_GUARD_ATTACK, "The user protects itself and its allies from priority moves. If used in succession, its chance of failing rises.", 15, Type.FIGHTING, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.QUICK_GUARD_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.SUCCESSIVE_DECAY);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
			super.priority = 4;
		}
	}

	private static class Endure extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Endure()
		{
			super(Namesies.ENDURE_ATTACK, "The user endures any attack with at least 1 HP. Its chance of failing rises if it is used in succession.", 10, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.BRACING_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.SUCCESSIVE_DECAY);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.selfTarget = true;
			super.priority = 4;
		}
	}

	private static class WaterPulse extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public WaterPulse()
		{
			super(Namesies.WATER_PULSE_ATTACK, "The user attacks the target with a pulsing blast of water. It may also confuse the target.", 20, Type.WATER, Category.SPECIAL);
			super.power = 60;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.CONFUSION_EFFECT, EffectType.POKEMON));
			super.effectChance = 20;
			super.moveTypes.add(MoveType.AURA_PULSE);
		}
	}

	private static class ConfusionDamage extends Attack implements CritBlockerEffect
	{
		private static final long serialVersionUID = 1L;

		public ConfusionDamage()
		{
			super(Namesies.CONFUSION_DAMAGE_ATTACK, "None", 1, Type.NONE, Category.PHYSICAL);
			super.power = 40;
		}

		public boolean blockCrits()
		{
			return true;
		}
	}

	private static class ConfuseRay extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public ConfuseRay()
		{
			super(Namesies.CONFUSE_RAY_ATTACK, "The target is exposed to a sinister ray that triggers confusion.", 10, Type.GHOST, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.CONFUSION_EFFECT, EffectType.POKEMON));
		}
	}

	private static class AquaTail extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public AquaTail()
		{
			super(Namesies.AQUA_TAIL_ATTACK, "The user attacks by swinging its tail as if it were a vicious wave in a raging storm.", 10, Type.WATER, Category.PHYSICAL);
			super.power = 90;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class SkullBash extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;

		public SkullBash()
		{
			super(Namesies.SKULL_BASH_ATTACK, "The user tucks in its head to raise its Defense in the first turn, then rams the target on the next turn.", 10, Type.NORMAL, Category.PHYSICAL);
			super.power = 130;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public boolean chargesFirst()
		{
			return true;
		}

		public boolean semiInvulnerability()
		{
			return false;
		}

		public void charge(ActivePokemon user, Battle b)
		{
			b.addMessage(getChargeMessage(user));
			user.getAttributes().modifyStage(user, user, 1, Stat.DEFENSE, b, CastSource.ATTACK);
		}

		public String getChargeMessage(ActivePokemon user)
		{
			return user.getName() + " lowered its head!";
		}
	}

	private static class IronDefense extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public IronDefense()
		{
			super(Namesies.IRON_DEFENSE_ATTACK, "The user hardens its body's surface like iron, sharply raising its Defense stat.", 15, Type.STEEL, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 2;
		}
	}

	private static class HydroPump extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public HydroPump()
		{
			super(Namesies.HYDRO_PUMP_ATTACK, "The target is blasted by a huge volume of water launched under great pressure.", 5, Type.WATER, Category.SPECIAL);
			super.power = 110;
			super.accuracy = 80;
		}
	}

	private static class RainDance extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public RainDance()
		{
			super(Namesies.RAIN_DANCE_ATTACK, "The user summons a heavy rain that falls for five turns, powering up Water-type moves.", 5, Type.WATER, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.RAINING_EFFECT, EffectType.BATTLE));
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return true;
		}
	}

	private static class SunnyDay extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public SunnyDay()
		{
			super(Namesies.SUNNY_DAY_ATTACK, "The user intensifies the sun for five turns, powering up Fire-type moves.", 5, Type.FIRE, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.SUNNY_EFFECT, EffectType.BATTLE));
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return true;
		}
	}

	private static class Sandstorm extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public Sandstorm()
		{
			super(Namesies.SANDSTORM_ATTACK, "A five-turn sandstorm is summoned to hurt all combatants except the Rock, Ground, and Steel types.", 10, Type.ROCK, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.SANDSTORM_EFFECT, EffectType.BATTLE));
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return true;
		}
	}

	private static class Hail extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public Hail()
		{
			super(Namesies.HAIL_ATTACK, "The user summons a hailstorm lasting five turns. It damages all Pok\u00e9mon except the Ice type.", 10, Type.ICE, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.HAILING_EFFECT, EffectType.BATTLE));
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return true;
		}
	}

	private static class PetalDance extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PetalDance()
		{
			super(Namesies.PETAL_DANCE_ATTACK, "The user attacks the target by scattering petals for two to three turns. The user then becomes confused.", 10, Type.GRASS, Category.SPECIAL);
			super.power = 120;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.SELF_CONFUSION_EFFECT, EffectType.POKEMON));
			super.selfTarget = true;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Thrash extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Thrash()
		{
			super(Namesies.THRASH_ATTACK, "The user rampages and attacks for two to three turns. It then becomes confused, however.", 10, Type.NORMAL, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.SELF_CONFUSION_EFFECT, EffectType.POKEMON));
			super.selfTarget = true;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class HyperBeam extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;

		public HyperBeam()
		{
			super(Namesies.HYPER_BEAM_ATTACK, "The target is attacked with a powerful beam. The user must rest on the next turn to regain its energy.", 5, Type.NORMAL, Category.SPECIAL);
			super.power = 150;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
		}

		public boolean chargesFirst()
		{
			return false;
		}

		public boolean semiInvulnerability()
		{
			return false;
		}

		public void charge(ActivePokemon user, Battle b)
		{
			b.addMessage(getChargeMessage(user));
		}

		public String getChargeMessage(ActivePokemon user)
		{
			return user.getName() + " must recharge!";
		}
	}

	private static class StringShot extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public StringShot()
		{
			super(Namesies.STRING_SHOT_ATTACK, "The targets are bound with silk blown from the user's mouth. This silk reduces the targets' Speed stat.", 40, Type.BUG, Category.STATUS);
			super.accuracy = 95;
			super.statChanges[Stat.SPEED.index()] = -2;
		}
	}

	private static class BugBite extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public BugBite()
		{
			super(Namesies.BUG_BITE_ATTACK, "The user bites the target. If the target is holding a Berry, the user eats it and gains its effect.", 20, Type.BUG, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Item i = victim.getHeldItem(b);
			if (i instanceof Berry)
			{
				b.addMessage(user.getName() + " ate " + victim.getName() + "'s " + i.getName() + "!");
				victim.consumeItem(b);
				
				if (i instanceof GainableEffectBerry)
				{
					((GainableEffectBerry)i).gainBerryEffect(b, user, CastSource.USE_ITEM);
				}
			}
		}
	}

	private static class Harden extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Harden()
		{
			super(Namesies.HARDEN_ATTACK, "The user stiffens all the muscles in its body to raise its Defense stat.", 30, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 1;
		}
	}

	private static class Confusion extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Confusion()
		{
			super(Namesies.CONFUSION_ATTACK, "The target is hit by a weak telekinetic force. It may also leave the target confused.", 25, Type.PSYCHIC, Category.SPECIAL);
			super.power = 50;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.CONFUSION_EFFECT, EffectType.POKEMON));
			super.effectChance = 10;
		}
	}

	private static class StunSpore extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public StunSpore()
		{
			super(Namesies.STUN_SPORE_ATTACK, "The user scatters a cloud of paralyzing powder. It may leave the target with paralysis.", 30, Type.GRASS, Category.STATUS);
			super.accuracy = 75;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.POWDER);
		}
	}

	private static class Gust extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public Gust()
		{
			super(Namesies.GUST_ATTACK, "A gust of wind is whipped up by wings and launched at the target to inflict damage.", 35, Type.FLYING, Category.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			// Twice as strong when the opponent is flying
			return super.power*(o.isSemiInvulnerableFlying() ? 2 : 1);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			// Always hit when the opponent is flying
			return defending.isSemiInvulnerableFlying();
		}
	}

	private static class Supersonic extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Supersonic()
		{
			super(Namesies.SUPERSONIC_ATTACK, "The user generates odd sound waves from its body. It may confuse the target.", 20, Type.NORMAL, Category.STATUS);
			super.accuracy = 55;
			super.effects.add(Effect.getEffect(Namesies.CONFUSION_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.SOUND_BASED);
		}
	}

	private static class Psybeam extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Psybeam()
		{
			super(Namesies.PSYBEAM_ATTACK, "The target is attacked with a peculiar ray. It may also cause confusion.", 20, Type.PSYCHIC, Category.SPECIAL);
			super.power = 65;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.CONFUSION_EFFECT, EffectType.POKEMON));
			super.effectChance = 10;
		}
	}

	private static class SilverWind extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SilverWind()
		{
			super(Namesies.SILVER_WIND_ATTACK, "The target is attacked with powdery scales blown by wind. It may also raise all the user's stats.", 5, Type.BUG, Category.SPECIAL);
			super.power = 60;
			super.accuracy = 100;
			super.effectChance = 10;
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
			super.statChanges[Stat.SPEED.index()] = 1;
			super.statChanges[Stat.ACCURACY.index()] = 1;
			super.statChanges[Stat.EVASION.index()] = 1;
		}
	}

	private static class Tailwind extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Tailwind()
		{
			super(Namesies.TAILWIND_ATTACK, "The user whips up a turbulent whirlwind that ups the Speed of all party Pok\u00e9mon for four turns.", 30, Type.FLYING, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.TAILWIND_EFFECT, EffectType.TEAM));
			super.selfTarget = true;
		}
	}

	private static class MorningSun extends Attack implements SelfHealingMove
	{
		private static final long serialVersionUID = 1L;

		public MorningSun()
		{
			super(Namesies.MORNING_SUN_ATTACK, "The user restores its own HP. The amount of HP regained varies with the weather.", 5, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect(Namesies.HEAL_BLOCK_EFFECT))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			switch (b.getWeather().namesies())
			{
				case CLEAR_SKIES_EFFECT:
				victim.healHealthFraction(1/2.0);
				break;
				case SUNNY_EFFECT:
				victim.healHealthFraction(2/3.0);
				break;
				case HAILING_EFFECT:
				case RAINING_EFFECT:
				case SANDSTORM_EFFECT:
				victim.healHealthFraction(1/4.0);
				break;
				default:
				Global.error("Funky weather problems!!!!");
				break;
			}
			
			b.addMessage(victim.getName() + "'s health was restored!", victim);
		}
	}

	private static class Safeguard extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Safeguard()
		{
			super(Namesies.SAFEGUARD_ATTACK, "The user creates a protective field that prevents status problems for five turns.", 25, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.SAFEGUARD_EFFECT, EffectType.POKEMON));
			super.selfTarget = true;
		}
	}

	private static class Captivate extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Captivate()
		{
			super(Namesies.CAPTIVATE_ATTACK, "If it is the opposite gender of the user, the target is charmed into harshly lowering its Sp. Atk stat.", 20, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.SP_ATTACK.index()] = -2;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			boolean oblivious = victim.hasAbility(Namesies.OBLIVIOUS_ABILITY);
			if (Gender.oppositeGenders(user, victim) && !oblivious)
			{
				super.applyEffects(b, user, victim);
			}
			else if (oblivious)
			{
				b.addMessage(victim.getName() + "'s " + victim.getAbility().getName() + " prevents it from being captivated!");
			}
			else
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
			}
		}
	}

	private static class BugBuzz extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public BugBuzz()
		{
			super(Namesies.BUG_BUZZ_ATTACK, "The user vibrates its wings to generate a damaging sound wave. It may also lower the target's Sp. Def stat.", 10, Type.BUG, Category.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
			super.effectChance = 10;
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
		}
	}

	private static class QuiverDance extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public QuiverDance()
		{
			super(Namesies.QUIVER_DANCE_ATTACK, "The user lightly performs a beautiful, mystic dance. It boosts the user's Sp. Atk, Sp. Def, and Speed stats.", 20, Type.BUG, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
			super.statChanges[Stat.SPEED.index()] = 1;
		}
	}

	private static class Encore extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Encore()
		{
			super(Namesies.ENCORE_ATTACK, "The user compels the target to keep using only the move it last used for three turns.", 5, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.ENCORE_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.ENCORELESS);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
		}
	}

	private static class PoisonSting extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PoisonSting()
		{
			super(Namesies.POISON_STING_ATTACK, "The user stabs the target with a poisonous stinger. This may also poison the target.", 35, Type.POISON, Category.PHYSICAL);
			super.power = 15;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.POISONED;
		}
	}

	private static class FuryAttack extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FuryAttack()
		{
			super(Namesies.FURY_ATTACK_ATTACK, "The target is jabbed repeatedly with a horn or beak two to five times in a row.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 15;
			super.accuracy = 85;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int minHits = 2;
			int maxHits = 5;
			
			int hits = (int)(Math.random()*(maxHits - minHits + 1)) + minHits;
			
			if (maxHits == 5 && me.hasAbility(Namesies.SKILL_LINK_ABILITY))
			{
				hits = 5;
			}
			
			int hit = 1;
			for (; hit <= hits; hit++)
			{
				b.addMessage("Hit " + hit + "!");
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b))
				{
					break;
				}
			}
			
			// Print hits and gtfo
			b.addMessage("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!");
		}
	}

	private static class FalseSwipe extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FalseSwipe()
		{
			super(Namesies.FALSE_SWIPE_ATTACK, "A restrained attack that prevents the target from fainting. The target is left with at least 1 HP.", 40, Type.NORMAL, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			o.addEffect(PokemonEffect.getEffect(Namesies.BRACING_EFFECT));
			super.applyDamage(me, o, b);
			o.getAttributes().removeEffect(Namesies.BRACING_EFFECT);
		}
	}

	private static class Disable extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Disable()
		{
			super(Namesies.DISABLE_ATTACK, "For four turns, this move prevents the target from using the move it last used.", 20, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.DISABLE_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
		}
	}

	private static class FocusEnergy extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FocusEnergy()
		{
			super(Namesies.FOCUS_ENERGY_ATTACK, "The user takes a deep breath and focuses so that critical hits land more easily.", 30, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.RAISE_CRITS_EFFECT, EffectType.POKEMON));
			super.selfTarget = true;
		}
	}

	private static class Twineedle extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Twineedle()
		{
			super(Namesies.TWINEEDLE_ATTACK, "The user damages the target twice in succession by jabbing it with two spikes. It may also poison the target.", 20, Type.BUG, Category.PHYSICAL);
			super.power = 25;
			super.accuracy = 100;
			super.effectChance = 20;
			super.status = StatusCondition.POISONED;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int minHits = 2;
			int maxHits = 2;
			
			int hits = (int)(Math.random()*(maxHits - minHits + 1)) + minHits;
			
			if (maxHits == 5 && me.hasAbility(Namesies.SKILL_LINK_ABILITY))
			{
				hits = 5;
			}
			
			int hit = 1;
			for (; hit <= hits; hit++)
			{
				b.addMessage("Hit " + hit + "!");
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b))
				{
					break;
				}
			}
			
			// Print hits and gtfo
			b.addMessage("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!");
		}
	}

	private static class Rage extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Rage()
		{
			super(Namesies.RAGE_ATTACK, "As long as this move is in use, the power of rage raises the Attack stat each time the user is hit in battle.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 20;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*me.getAttributes().getCount();
		}
	}

	private static class Pursuit extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Pursuit()
		{
			super(Namesies.PURSUIT_ATTACK, "An attack move that inflicts double damage if used on a target that is switching out of battle.", 20, Type.DARK, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int getPriority(Battle b, ActivePokemon me)
		{
			Team trainer = b.getTrainer(!me.user()); // TODO: Make switching occur at its priority
			if (trainer instanceof Trainer && ((Trainer)trainer).getAction() == Action.SWITCH) return 7;
			return super.priority;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			Team trainer = b.getTrainer(o.user());
			if (trainer instanceof Trainer && ((Trainer)trainer).getAction() == Action.SWITCH)
			{
				return super.power*2;
			}
			
			return super.power;
		}
	}

	private static class ToxicSpikes extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public ToxicSpikes()
		{
			super(Namesies.TOXIC_SPIKES_ATTACK, "The user lays a trap of poison spikes at the opponent's feet. They poison opponents that switch into battle.", 20, Type.POISON, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.TOXIC_SPIKES_EFFECT, EffectType.TEAM));
			super.moveTypes.add(MoveType.FIELD);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return true;
		}
	}

	private static class PinMissile extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PinMissile()
		{
			super(Namesies.PIN_MISSILE_ATTACK, "Sharp spikes are shot at the target in rapid succession. They hit two to five times in a row.", 20, Type.BUG, Category.PHYSICAL);
			super.power = 25;
			super.accuracy = 95;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int minHits = 2;
			int maxHits = 5;
			
			int hits = (int)(Math.random()*(maxHits - minHits + 1)) + minHits;
			
			if (maxHits == 5 && me.hasAbility(Namesies.SKILL_LINK_ABILITY))
			{
				hits = 5;
			}
			
			int hit = 1;
			for (; hit <= hits; hit++)
			{
				b.addMessage("Hit " + hit + "!");
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b))
				{
					break;
				}
			}
			
			// Print hits and gtfo
			b.addMessage("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!");
		}
	}

	private static class Agility extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Agility()
		{
			super(Namesies.AGILITY_ATTACK, "The user relaxes and lightens its body to move faster. It sharply boosts the Speed stat.", 30, Type.PSYCHIC, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.SPEED.index()] = 2;
		}
	}

	private static class Assurance extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Assurance()
		{
			super(Namesies.ASSURANCE_ATTACK, "If the target has already taken some damage in the same turn, this attack's power is doubled.", 10, Type.DARK, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			if (me.getAttributes().hasTakenDamage())
			{
				return super.power*2;
			}
			
			return super.power;
		}
	}

	private static class PoisonJab extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PoisonJab()
		{
			super(Namesies.POISON_JAB_ATTACK, "The target is stabbed with a tentacle or arm steeped in poison. It may also poison the target.", 20, Type.POISON, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.POISONED;
			super.moveTypes.add(MoveType.PUNCHING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Endeavor extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Endeavor()
		{
			super(Namesies.ENDEAVOR_ATTACK, "An attack move that cuts down the target's HP to equal the user's HP.", 5, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			o.reduceHealth(b, o.getHP() - me.getHP());
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (me.getHP() >= o.getHP())
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			super.apply(me, o, b);
		}
	}

	private static class SandAttack extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SandAttack()
		{
			super(Namesies.SAND_ATTACK_ATTACK, "Sand is hurled in the target's face, reducing its accuracy.", 15, Type.GROUND, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	private static class QuickAttack extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public QuickAttack()
		{
			super(Namesies.QUICK_ATTACK_ATTACK, "The user lunges at the target at a speed that makes it almost invisible. It is sure to strike first.", 30, Type.NORMAL, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.priority = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Twister extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public Twister()
		{
			super(Namesies.TWISTER_ATTACK, "The user whips up a vicious tornado to tear at the opposing team. It may also make targets flinch.", 20, Type.DRAGON, Category.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 20;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			// Twice as strong when the opponent is flying
			return super.power*(o.isSemiInvulnerableFlying() ? 2 : 1);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			// Always hit when the opponent is flying
			return defending.isSemiInvulnerableFlying();
		}
	}

	private static class FeatherDance extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FeatherDance()
		{
			super(Namesies.FEATHER_DANCE_ATTACK, "The user covers the target's body with a mass of down that harshly lowers its Attack stat.", 15, Type.FLYING, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.ATTACK.index()] = -2;
		}
	}

	private static class Roost extends Attack implements SelfHealingMove, ChangeTypeMove
	{
		private static final long serialVersionUID = 1L;
		private boolean healFail;

		public Roost()
		{
			super(Namesies.ROOST_ATTACK, "The user lands and rests its body. It restores the user's HP by up to half of its max HP.", 10, Type.FLYING, Category.STATUS);
			super.selfTarget = true;
			super.printCast = false;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			healFail = true;
			super.applyEffects(b, user, victim);
			if (!healFail && getType(b, user, victim) != null)
			{
				PokemonEffect.getEffect(Namesies.CHANGE_TYPE_EFFECT).cast(b, user, victim, CastSource.ATTACK, super.printCast);
				user.getEffect(Namesies.CHANGE_TYPE_EFFECT).setTurns(1);
			}
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect(Namesies.HEAL_BLOCK_EFFECT))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			healFail = false;
			victim.healHealthFraction(1/2.0);
			
			b.addMessage(victim.getName() + "'s health was restored!", victim);
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			Type[] type = victim.getType(b);
			
			if (type[0] == Type.FLYING)
			{
				return new Type[] {type[1], Type.NONE};
			}
			
			if (type[1] == Type.FLYING)
			{
				return new Type[] {type[0], Type.NONE};
			}
			
			return null;
		}
	}

	private static class ThunderShock extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public ThunderShock()
		{
			super(Namesies.THUNDER_SHOCK_ATTACK, "A jolt of electricity is hurled at the target to inflict damage. It may also leave the target with paralysis.", 30, Type.ELECTRIC, Category.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.PARALYZED;
		}
	}

	private static class MirrorMove extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MirrorMove()
		{
			super(Namesies.MIRROR_MOVE_ATTACK, "The user counters the target by mimicking the target's last move.", 20, Type.FLYING, Category.STATUS);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.ENCORELESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.MIRRORLESS);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			Move mirror = o.getAttributes().getLastMoveUsed();
			if (mirror == null || mirror.getAttack().isMoveType(MoveType.MIRRORLESS))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			me.callNewMove(b, o, new Move(mirror.getAttack()));
		}
	}

	private static class Hurricane extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public Hurricane()
		{
			super(Namesies.HURRICANE_ATTACK, "The user attacks by wrapping its opponent in a fierce wind that flies up into the sky. It may also confuse the target.", 10, Type.FLYING, Category.SPECIAL);
			super.power = 110;
			super.accuracy = 70;
			super.effects.add(Effect.getEffect(Namesies.CONFUSION_EFFECT, EffectType.POKEMON));
			super.effectChance = 30;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			// Twice as strong when the opponent is flying
			return super.power*(o.isSemiInvulnerableFlying() ? 2 : 1);
		}

		public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o)
		{
			// Accuracy is only 50% when sunny
			if (b.getWeather().namesies() == Namesies.SUNNY_EFFECT)
			{
				return 50;
			}
			
			return super.accuracy;
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			// Always hits when the opponent is flying or it is raining (unless they're non-flying semi-invulnerable)
			return defending.isSemiInvulnerableFlying() || (b.getWeather().namesies() == Namesies.RAINING_EFFECT && defending.isSemiInvulnerable());
		}
	}

	private static class HyperFang extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public HyperFang()
		{
			super(Namesies.HYPER_FANG_ATTACK, "The user bites hard on the target with its sharp front fangs. It may also make the target flinch.", 15, Type.NORMAL, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 90;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 10;
			super.moveTypes.add(MoveType.BITING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class SuckerPunch extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SuckerPunch()
		{
			super(Namesies.SUCKER_PUNCH_ATTACK, "This move enables the user to attack first. It fails if the foe is not readying an attack, however.", 5, Type.DARK, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.priority = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (o.getMove().getAttack().getCategory() == Category.STATUS)
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			super.apply(me, o, b);
		}
	}

	private static class Crunch extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Crunch()
		{
			super(Namesies.CRUNCH_ATTACK, "The user crunches up the foe with sharp fangs. It may also lower the target's Defense stat.", 15, Type.DARK, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.effectChance = 20;
			super.moveTypes.add(MoveType.BITING);
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class SuperFang extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SuperFang()
		{
			super(Namesies.SUPER_FANG_ATTACK, "The user chomps hard on the foe with its sharp front fangs. It cuts the target's HP to half.", 10, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 90;
			super.moveTypes.add(MoveType.BITING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			o.reduceHealth(b, (int)Math.ceil(o.getHP()/2.0));
		}
	}

	private static class SwordsDance extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SwordsDance()
		{
			super(Namesies.SWORDS_DANCE_ATTACK, "A frenetic dance to uplift the fighting spirit. It sharply raises the user's Attack stat.", 30, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 2;
		}
	}

	private static class Peck extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Peck()
		{
			super(Namesies.PECK_ATTACK, "The foe is jabbed with a sharply pointed beak or horn.", 35, Type.FLYING, Category.PHYSICAL);
			super.power = 35;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Leer extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Leer()
		{
			super(Namesies.LEER_ATTACK, "The foe is given an intimidating leer with sharp eyes. The target's Defense stat is reduced.", 30, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.DEFENSE.index()] = -1;
		}
	}

	private static class AerialAce extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public AerialAce()
		{
			super(Namesies.AERIAL_ACE_ATTACK, "The user confounds the foe with speed, then slashes. The attack lands without fail.", 20, Type.FLYING, Category.PHYSICAL);
			super.power = 60;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class DrillPeck extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DrillPeck()
		{
			super(Namesies.DRILL_PECK_ATTACK, "A corkscrewing attack with the sharp beak acting as a drill.", 20, Type.FLYING, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Pluck extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Pluck()
		{
			super(Namesies.PLUCK_ATTACK, "The user pecks the foe. If the foe is holding a Berry, the user plucks it and gains its effect.", 20, Type.FLYING, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Item i = victim.getHeldItem(b);
			if (i instanceof Berry)
			{
				b.addMessage(user.getName() + " ate " + victim.getName() + "'s " + i.getName() + "!");
				victim.consumeItem(b);
				
				if (i instanceof GainableEffectBerry)
				{
					((GainableEffectBerry)i).gainBerryEffect(b, user, CastSource.USE_ITEM);
				}
			}
		}
	}

	private static class DrillRun extends Attack implements CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public DrillRun()
		{
			super(Namesies.DRILL_RUN_ATTACK, "The user crashes into its target while rotating its body like a drill. Critical hits land more easily.", 10, Type.GROUND, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 95;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}
	}

	private static class Wrap extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Wrap()
		{
			super(Namesies.WRAP_ATTACK, "A long body or vines are used to wrap and squeeze the target for four to five turns.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 15;
			super.accuracy = 90;
			super.effects.add(Effect.getEffect(Namesies.WRAPPED_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Glare extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Glare()
		{
			super(Namesies.GLARE_ATTACK, "The user intimidates the target with the pattern on its belly to cause paralysis.", 30, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.status = StatusCondition.PARALYZED;
		}
	}

	private static class Screech extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Screech()
		{
			super(Namesies.SCREECH_ATTACK, "An earsplitting screech harshly reduces the target's Defense stat.", 40, Type.NORMAL, Category.STATUS);
			super.accuracy = 85;
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.statChanges[Stat.DEFENSE.index()] = -2;
		}
	}

	private static class Acid extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Acid()
		{
			super(Namesies.ACID_ATTACK, "The opposing team is attacked with a spray of harsh acid. The acid may also lower the targets' Sp. Def stats.", 30, Type.POISON, Category.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.effectChance = 10;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
		}
	}

	private static class Stockpile extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Stockpile()
		{
			super(Namesies.STOCKPILE_ATTACK, "The user charges up power and raises both its Defense and Sp. Def. The move can be used three times.", 20, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.STOCKPILE_EFFECT, EffectType.POKEMON));
			super.selfTarget = true;
		}
	}

	private static class SpitUp extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SpitUp()
		{
			super(Namesies.SPIT_UP_ATTACK, "The power stored using the move Stockpile is released at once in an attack. The more power is stored, the greater the damage.", 10, Type.NORMAL, Category.SPECIAL);
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			PokemonEffect stockpile = me.getEffect(Namesies.STOCKPILE_EFFECT);
			
			// I really don't like this, because there's no way this value should actually be getting used -- but it's getting set now always even if the attack isn't going to work and we don't want a NullPointerException
			if (stockpile == null)
			{
				return super.setPower(b, me, o);
			}
			
			int turns = stockpile.getTurns();
			if (turns <= 0)
			{
				Global.error("Stockpile turns should never be nonpositive");
			}
			
			// Max power is 300
			return (int)Math.min(turns, 3)*100;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			PokemonEffect stockpile = me.getEffect(Namesies.STOCKPILE_EFFECT);
			if (stockpile == null)
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			super.apply(me, o, b);
			
			// Stockpile ends after Spit up is used
			stockpile.deactivate();
		}
	}

	private static class Swallow extends Attack implements SelfHealingMove
	{
		private static final long serialVersionUID = 1L;

		public Swallow()
		{
			super(Namesies.SWALLOW_ATTACK, "The power stored using the move Stockpile is absorbed by the user to heal its HP. Storing more power heals more HP.", 10, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect(Namesies.HEAL_BLOCK_EFFECT))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			PokemonEffect stockpile = user.getEffect(Namesies.STOCKPILE_EFFECT);
			if (stockpile == null)
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			if (stockpile.getTurns() <= 0)
			{
				Global.error("Stockpile turns should never be nonpositive");
			}
			
			// Heals differently based on number of stockpile turns
			switch (stockpile.getTurns())
			{
				case 1:
				victim.healHealthFraction(1/4.0);
				break;
				case 2:
				victim.healHealthFraction(1/2.0);
				break;
				default:
				victim.healHealthFraction(1);
				break;
			}
			
			// Stockpile ends after Swallow is used
			stockpile.deactivate();
			
			b.addMessage(victim.getName() + "'s health was restored!", victim);
		}
	}

	private static class AcidSpray extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public AcidSpray()
		{
			super(Namesies.ACID_SPRAY_ATTACK, "The user spits fluid that works to melt the target. This harshly reduces the target's Sp. Def stat.", 20, Type.POISON, Category.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.BOMB_BALL);
			super.statChanges[Stat.SP_DEFENSE.index()] = -2;
		}
	}

	private static class MudBomb extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MudBomb()
		{
			super(Namesies.MUD_BOMB_ATTACK, "The user launches a hard-packed mud ball to attack. It may also lower the target's accuracy.", 10, Type.GROUND, Category.SPECIAL);
			super.power = 65;
			super.accuracy = 85;
			super.effectChance = 30;
			super.moveTypes.add(MoveType.BOMB_BALL);
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	private static class Haze extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public Haze()
		{
			super(Namesies.HAZE_ATTACK, "The user creates a haze that eliminates every stat change among all the Pok\u00e9mon engaged in battle.", 30, Type.ICE, Category.STATUS);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			user.getAttributes().resetStages();
			victim.getAttributes().resetStages();
			b.addMessage("All stat changes were eliminated!");
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return true;
		}
	}

	private static class Coil extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Coil()
		{
			super(Namesies.COIL_ATTACK, "The user coils up and concentrates. This raises its Attack and Defense stats as well as its accuracy.", 20, Type.POISON, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.statChanges[Stat.ACCURACY.index()] = 1;
		}
	}

	private static class GunkShot extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public GunkShot()
		{
			super(Namesies.GUNK_SHOT_ATTACK, "The user shoots filthy garbage at the target to attack. It may also poison the target.", 5, Type.POISON, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 80;
			super.effectChance = 30;
			super.status = StatusCondition.POISONED;
		}
	}

	private static class IceFang extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public IceFang()
		{
			super(Namesies.ICE_FANG_ATTACK, "The user bites with cold-infused fangs. It may also make the target flinch or leave it frozen.", 15, Type.ICE, Category.PHYSICAL);
			super.power = 65;
			super.accuracy = 95;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 20;
			super.moveTypes.add(MoveType.BITING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			// If the effect is being applied, 50/50 chance to give a status condition vs. flinching
			if (Math.random() < .5)
			{
				Status.giveStatus(b, user, victim, StatusCondition.FROZEN);
				return;
			}
			
			super.applyEffects(b, user, victim);
		}
	}

	private static class ThunderFang extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public ThunderFang()
		{
			super(Namesies.THUNDER_FANG_ATTACK, "The user bites with electrified fangs. It may also make the target flinch or leave it with paralysis.", 15, Type.ELECTRIC, Category.PHYSICAL);
			super.power = 65;
			super.accuracy = 95;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 20;
			super.moveTypes.add(MoveType.BITING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			// If the effect is being applied, 50/50 chance to give a status condition vs. flinching
			if (Math.random() < .5)
			{
				Status.giveStatus(b, user, victim, StatusCondition.PARALYZED);
				return;
			}
			
			super.applyEffects(b, user, victim);
		}
	}

	private static class ElectroBall extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public ElectroBall()
		{
			super(Namesies.ELECTRO_BALL_ATTACK, "The user hurls an electric orb at the target. The faster the user is than the target, the greater the damage.", 10, Type.ELECTRIC, Category.SPECIAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.BOMB_BALL);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			double ratio = (double)Stat.getStat(Stat.SPEED, o, me, b)/Stat.getStat(Stat.SPEED, me, o, b);
			if (ratio > .5) return 60;
			if (ratio > .33) return 80;
			if (ratio > .25) return 120;
			return 150;
		}
	}

	private static class DoubleTeam extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DoubleTeam()
		{
			super(Namesies.DOUBLE_TEAM_ATTACK, "By moving rapidly, the user makes illusory copies of itself to raise its evasiveness.", 15, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.EVASION.index()] = 1;
		}
	}

	private static class Slam extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Slam()
		{
			super(Namesies.SLAM_ATTACK, "The target is slammed with a long tail, vines, etc., to inflict damage.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 75;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Thunderbolt extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Thunderbolt()
		{
			super(Namesies.THUNDERBOLT_ATTACK, "A strong electric blast is loosed at the target. It may also leave the target with paralysis.", 15, Type.ELECTRIC, Category.SPECIAL);
			super.power = 95;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.PARALYZED;
		}
	}

	private static class Feint extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Feint()
		{
			super(Namesies.FEINT_ATTACK, "An attack that hits a target using Protect or Detect. It also lifts the effects of those moves.", 10, Type.NORMAL, Category.PHYSICAL);
			super.power = 30;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.priority = 2;
		}
	}

	private static class Discharge extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Discharge()
		{
			super(Namesies.DISCHARGE_ATTACK, "A flare of electricity is loosed to strike the area around the user. It may also cause paralysis.", 15, Type.ELECTRIC, Category.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.PARALYZED;
		}
	}

	private static class LightScreen extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public LightScreen()
		{
			super(Namesies.LIGHT_SCREEN_ATTACK, "A wondrous wall of light is put up to suppress damage from special attacks for five turns.", 30, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.LIGHT_SCREEN_EFFECT, EffectType.TEAM));
			super.selfTarget = true;
		}
	}

	private static class Thunder extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public Thunder()
		{
			super(Namesies.THUNDER_ATTACK, "A wicked thunderbolt is dropped on the target to inflict damage. It may also leave the target with paralysis.", 10, Type.ELECTRIC, Category.SPECIAL);
			super.power = 110;
			super.accuracy = 70;
			super.effectChance = 30;
			super.status = StatusCondition.PARALYZED;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			// Twice as strong when the opponent is flying
			return super.power*(o.isSemiInvulnerableFlying() ? 2 : 1);
		}

		public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o)
		{
			// Accuracy is only 50% when sunny
			if (b.getWeather().namesies() == Namesies.SUNNY_EFFECT)
			{
				return 50;
			}
			
			return super.accuracy;
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			// Always hits when the opponent is flying or it is raining (unless they're non-flying semi-invulnerable)
			return defending.isSemiInvulnerableFlying() || (b.getWeather().namesies() == Namesies.RAINING_EFFECT && defending.isSemiInvulnerable());
		}
	}

	private static class DefenseCurl extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DefenseCurl()
		{
			super(Namesies.DEFENSE_CURL_ATTACK, "The user curls up to conceal weak spots and raise its Defense stat.", 40, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.USED_DEFENSE_CURL_EFFECT, EffectType.POKEMON));
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 1;
		}
	}

	private static class Swift extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Swift()
		{
			super(Namesies.SWIFT_ATTACK, "Star-shaped rays are shot at the opposing team. This attack never misses.", 20, Type.NORMAL, Category.SPECIAL);
			super.power = 60;
		}
	}

	private static class FurySwipes extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FurySwipes()
		{
			super(Namesies.FURY_SWIPES_ATTACK, "The target is raked with sharp claws or scythes for two to five times in quick succession.", 15, Type.NORMAL, Category.PHYSICAL);
			super.power = 18;
			super.accuracy = 80;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int minHits = 2;
			int maxHits = 5;
			
			int hits = (int)(Math.random()*(maxHits - minHits + 1)) + minHits;
			
			if (maxHits == 5 && me.hasAbility(Namesies.SKILL_LINK_ABILITY))
			{
				hits = 5;
			}
			
			int hit = 1;
			for (; hit <= hits; hit++)
			{
				b.addMessage("Hit " + hit + "!");
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b))
				{
					break;
				}
			}
			
			// Print hits and gtfo
			b.addMessage("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!");
		}
	}

	private static class Rollout extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Rollout()
		{
			super(Namesies.ROLLOUT_ATTACK, "The user continually rolls into the target over five turns. It becomes stronger each time it hits.", 20, Type.ROCK, Category.PHYSICAL);
			super.power = 30;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(int)Math.min(me.getAttributes().getCount(), 5)*(me.hasEffect(Namesies.USED_DEFENSE_CURL_EFFECT) ? 2 : 1);
		}
	}

	private static class FuryCutter extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FuryCutter()
		{
			super(Namesies.FURY_CUTTER_ATTACK, "The target is slashed with scythes or claws. Its power increases if it hits in succession.", 20, Type.BUG, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 95;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(int)Math.min(5, me.getAttributes().getCount());
		}
	}

	private static class SandTomb extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SandTomb()
		{
			super(Namesies.SAND_TOMB_ATTACK, "The user traps the target inside a harshly raging sandstorm for four to five turns.", 15, Type.GROUND, Category.PHYSICAL);
			super.power = 35;
			super.accuracy = 85;
			super.effects.add(Effect.getEffect(Namesies.SAND_TOMB_EFFECT, EffectType.POKEMON));
		}
	}

	private static class GyroBall extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public GyroBall()
		{
			super(Namesies.GYRO_BALL_ATTACK, "The user tackles the target with a high-speed spin. The slower the user, the greater the damage.", 5, Type.STEEL, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.BOMB_BALL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return (int)Math.min(150, 25.0*Stat.getStat(Stat.SPEED, o, me, b)/Stat.getStat(Stat.SPEED, me, o, b));
		}
	}

	private static class CrushClaw extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public CrushClaw()
		{
			super(Namesies.CRUSH_CLAW_ATTACK, "The user slashes the target with hard and sharp claws. It may also lower the target's Defense.", 10, Type.NORMAL, Category.PHYSICAL);
			super.power = 75;
			super.accuracy = 95;
			super.effectChance = 50;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class DoubleKick extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DoubleKick()
		{
			super(Namesies.DOUBLE_KICK_ATTACK, "The target is quickly kicked twice in succession using both feet.", 30, Type.FIGHTING, Category.PHYSICAL);
			super.power = 30;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int minHits = 2;
			int maxHits = 2;
			
			int hits = (int)(Math.random()*(maxHits - minHits + 1)) + minHits;
			
			if (maxHits == 5 && me.hasAbility(Namesies.SKILL_LINK_ABILITY))
			{
				hits = 5;
			}
			
			int hit = 1;
			for (; hit <= hits; hit++)
			{
				b.addMessage("Hit " + hit + "!");
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b))
				{
					break;
				}
			}
			
			// Print hits and gtfo
			b.addMessage("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!");
		}
	}

	private static class PoisonTail extends Attack implements CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public PoisonTail()
		{
			super(Namesies.POISON_TAIL_ATTACK, "The user hits the target with its tail. It may also poison the target. Critical hits land more easily.", 25, Type.POISON, Category.PHYSICAL);
			super.power = 50;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.POISONED;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}
	}

	private static class Flatter extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Flatter()
		{
			super(Namesies.FLATTER_ATTACK, "Flattery is used to confuse the target. However, it also raises the target's Sp. Atk stat.", 15, Type.DARK, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.CONFUSION_EFFECT, EffectType.POKEMON));
			super.statChanges[Stat.SP_ATTACK.index()] = 2;
		}
	}

	private static class PoisonFang extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PoisonFang()
		{
			super(Namesies.POISON_FANG_ATTACK, "The user bites the target with toxic fangs. It may also leave the target badly poisoned.", 15, Type.POISON, Category.PHYSICAL);
			super.power = 50;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.BAD_POISON_EFFECT, EffectType.POKEMON));
			super.effectChance = 50;
			super.moveTypes.add(MoveType.BITING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class ChipAway extends Attack implements IgnoreStageEffect
	{
		private static final long serialVersionUID = 1L;

		public ChipAway()
		{
			super(Namesies.CHIP_AWAY_ATTACK, "Looking for an opening, the user strikes continually. The target's stat changes don't affect this attack's damage.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public boolean ignoreStage(Stat s)
		{
			return !s.user();
		}
	}

	private static class BodySlam extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public BodySlam()
		{
			super(Namesies.BODY_SLAM_ATTACK, "The user drops onto the target with its full body weight. It may also leave the target with paralysis.", 15, Type.NORMAL, Category.PHYSICAL);
			super.power = 85;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(o.hasEffect(Namesies.USED_MINIMIZE_EFFECT) ? 2 : 1);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return !defending.isSemiInvulnerable() && defending.hasEffect(Namesies.USED_MINIMIZE_EFFECT);
		}
	}

	private static class EarthPower extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public EarthPower()
		{
			super(Namesies.EARTH_POWER_ATTACK, "The user makes the ground under the target erupt with power. It may also lower the target's Sp. Def.", 10, Type.GROUND, Category.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
			super.effectChance = 10;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
		}
	}

	private static class Superpower extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Superpower()
		{
			super(Namesies.SUPERPOWER_ATTACK, "The user attacks the target with great power. However, it also lowers the user's Attack and Defense.", 5, Type.FIGHTING, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = -1;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class HornAttack extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public HornAttack()
		{
			super(Namesies.HORN_ATTACK_ATTACK, "The target is jabbed with a sharply pointed horn to inflict damage.", 25, Type.NORMAL, Category.PHYSICAL);
			super.power = 65;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class HornDrill extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public HornDrill()
		{
			super(Namesies.HORN_DRILL_ATTACK, "The user stabs the target with a horn that rotates like a drill. If it hits, the target faints instantly.", 5, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 30;
			super.moveTypes.add(MoveType.ONE_HIT_KO);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			// Fails when the opponent is at a higher level than the user
			if (me.getLevel() < o.getLevel())
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			// Sturdy prevents OHKO moves if the user is not a mold breaker
			if (o.hasAbility(Namesies.STURDY_ABILITY) && !me.breaksTheMold())
			{
				b.addMessage(o.getName() + "'s " + Namesies.STURDY_ABILITY.getName() + " prevents OHKO moves!");
				return;
			}
			
			// Certain death
			o.reduceHealth(b, o.getHP());
			b.addMessage("It's a One-Hit KO!");
		}

		public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.accuracy + (me.getLevel() - o.getLevel());
		}
	}

	private static class Megahorn extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Megahorn()
		{
			super(Namesies.MEGAHORN_ATTACK, "Using its tough and impressive horn, the user rams into the target with no letup.", 10, Type.BUG, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 85;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Pound extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Pound()
		{
			super(Namesies.POUND_ATTACK, "The target is physically pounded with a long tail or a foreleg, etc.", 35, Type.NORMAL, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Sing extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Sing()
		{
			super(Namesies.SING_ATTACK, "A soothing lullaby is sung in a calming voice that puts the target into a deep slumber.", 15, Type.NORMAL, Category.STATUS);
			super.accuracy = 55;
			super.status = StatusCondition.ASLEEP;
			super.moveTypes.add(MoveType.SOUND_BASED);
		}
	}

	private static class DoubleSlap extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DoubleSlap()
		{
			super(Namesies.DOUBLE_SLAP_ATTACK, "The target is slapped repeatedly, back and forth, two to five times in a row.", 10, Type.NORMAL, Category.PHYSICAL);
			super.power = 15;
			super.accuracy = 85;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int minHits = 2;
			int maxHits = 5;
			
			int hits = (int)(Math.random()*(maxHits - minHits + 1)) + minHits;
			
			if (maxHits == 5 && me.hasAbility(Namesies.SKILL_LINK_ABILITY))
			{
				hits = 5;
			}
			
			int hit = 1;
			for (; hit <= hits; hit++)
			{
				b.addMessage("Hit " + hit + "!");
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b))
				{
					break;
				}
			}
			
			// Print hits and gtfo
			b.addMessage("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!");
		}
	}

	private static class Wish extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Wish()
		{
			super(Namesies.WISH_ATTACK, "One turn after this move is used, the target's HP is restored by half the user's maximum HP.", 10, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.WISH_EFFECT, EffectType.TEAM));
			super.selfTarget = true;
		}
	}

	private static class Minimize extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Minimize()
		{
			super(Namesies.MINIMIZE_ATTACK, "The user compresses its body to make itself look smaller, which sharply raises its evasiveness.", 20, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.USED_MINIMIZE_EFFECT, EffectType.POKEMON));
			super.selfTarget = true;
			super.statChanges[Stat.EVASION.index()] = 2;
		}
	}

	private static class WakeUpSlap extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public WakeUpSlap()
		{
			super(Namesies.WAKE_UP_SLAP_ATTACK, "This attack inflicts big damage on a sleeping target. It also wakes the target up, however.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(o.hasStatus(StatusCondition.ASLEEP) ? 2 : 1);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (victim.hasStatus(StatusCondition.ASLEEP))
			{
				Status.removeStatus(b, victim, CastSource.ATTACK);
			}
		}
	}

	private static class CosmicPower extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public CosmicPower()
		{
			super(Namesies.COSMIC_POWER_ATTACK, "The user absorbs a mystical power from space to raise its Defense and Sp. Def stats.", 20, Type.PSYCHIC, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
		}
	}

	private static class LuckyChant extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public LuckyChant()
		{
			super(Namesies.LUCKY_CHANT_ATTACK, "The user chants an incantation toward the sky, preventing opposing Pok\u00e9mon from landing critical hits.", 30, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.LUCKY_CHANT_EFFECT, EffectType.TEAM));
			super.selfTarget = true;
		}
	}

	private static class Metronome extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Metronome()
		{
			super(Namesies.METRONOME_ATTACK, "The user waggles a finger and stimulates its brain into randomly using nearly any move.", 10, Type.NORMAL, Category.STATUS);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.MIMICLESS);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int index = (int)(Math.random()*moveNames.size());
			while (map.get(moveNames.get(index)).isMoveType(MoveType.METRONOMELESS)) index = (int)(Math.random()*moveNames.size());
			me.callNewMove(b, o, new Move(map.get(moveNames.get(index))));
		}
	}

	private static class Gravity extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public Gravity()
		{
			super(Namesies.GRAVITY_ATTACK, "Gravity is intensified for five turns, making moves involving flying unusable and negating Levitate.", 5, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.GRAVITY_EFFECT, EffectType.BATTLE));
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return true;
		}
	}

	private static class Moonlight extends Attack implements SelfHealingMove
	{
		private static final long serialVersionUID = 1L;

		public Moonlight()
		{
			super(Namesies.MOONLIGHT_ATTACK, "The user restores its own HP. The amount of HP regained varies with the weather.", 5, Type.FAIRY, Category.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect(Namesies.HEAL_BLOCK_EFFECT))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			switch (b.getWeather().namesies())
			{
				case CLEAR_SKIES_EFFECT:
				victim.healHealthFraction(1/2.0);
				break;
				case SUNNY_EFFECT:
				victim.healHealthFraction(2/3.0);
				break;
				case HAILING_EFFECT:
				case RAINING_EFFECT:
				case SANDSTORM_EFFECT:
				victim.healHealthFraction(1/4.0);
				break;
				default:
				Global.error("Funky weather problems!!!!");
				break;
			}
			
			b.addMessage(victim.getName() + "'s health was restored!", victim);
		}
	}

	private static class StoredPower extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public StoredPower()
		{
			super(Namesies.STORED_POWER_ATTACK, "The user attacks the target with stored power. The more the user's stats are raised, the greater the damage.", 10, Type.PSYCHIC, Category.SPECIAL);
			super.power = 20;
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*me.getAttributes().totalStatIncreases();
		}
	}

	private static class Mimic extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Mimic()
		{
			super(Namesies.MIMIC_ATTACK, "The user copies the target's last move. The move can be used during battle until the Pok\u00e9mon is switched out.", 10, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.MIMIC_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.ENCORELESS);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}
	}

	private static class MeteorMash extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MeteorMash()
		{
			super(Namesies.METEOR_MASH_ATTACK, "The target is hit with a hard punch fired like a meteor. It may also raise the user's Attack.", 10, Type.STEEL, Category.PHYSICAL);
			super.power = 90;
			super.accuracy = 90;
			super.effectChance = 20;
			super.moveTypes.add(MoveType.PUNCHING);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Imprison extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Imprison()
		{
			super(Namesies.IMPRISON_ATTACK, "If the opponents know any move also known by the user, the opponents are prevented from using it.", 10, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.IMPRISON_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
		}
	}

	private static class WillOWisp extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public WillOWisp()
		{
			super(Namesies.WILL_OWISP_ATTACK, "The user shoots a sinister, bluish-white flame at the target to inflict a burn.", 15, Type.FIRE, Category.STATUS);
			super.accuracy = 85;
			super.status = StatusCondition.BURNED;
		}
	}

	private static class Payback extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Payback()
		{
			super(Namesies.PAYBACK_ATTACK, "If the user moves after the target, this attack's power will be doubled.", 10, Type.DARK, Category.PHYSICAL);
			super.power = 50;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(b.isFirstAttack() ? 1 : 2);
		}
	}

	private static class Extrasensory extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Extrasensory()
		{
			super(Namesies.EXTRASENSORY_ATTACK, "The user attacks with an odd, unseeable power. It may also make the target flinch.", 20, Type.PSYCHIC, Category.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 10;
		}
	}

	private static class FireBlast extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FireBlast()
		{
			super(Namesies.FIRE_BLAST_ATTACK, "The target is attacked with an intense blast of all-consuming fire. It may also leave the target with a burn.", 5, Type.FIRE, Category.SPECIAL);
			super.power = 110;
			super.accuracy = 85;
			super.effectChance = 10;
			super.status = StatusCondition.BURNED;
		}
	}

	private static class NastyPlot extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public NastyPlot()
		{
			super(Namesies.NASTY_PLOT_ATTACK, "The user stimulates its brain by thinking bad thoughts. It sharply raises the user's Sp. Atk.", 20, Type.DARK, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = 2;
		}
	}

	private static class Round extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Round()
		{
			super(Namesies.ROUND_ATTACK, "The user attacks the target with a song.", 15, Type.NORMAL, Category.SPECIAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SOUND_BASED);
		}
	}

	private static class Rest extends Attack implements SelfHealingMove
	{
		private static final long serialVersionUID = 1L;

		public Rest()
		{
			super(Namesies.REST_ATTACK, "The user goes to sleep for two turns. It fully restores the user's HP and heals any status problem.", 10, Type.PSYCHIC, Category.STATUS);
			super.status = StatusCondition.ASLEEP;
			super.selfTarget = true;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (victim.fullHealth() || !Status.applies(StatusCondition.ASLEEP, b, victim, victim))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			victim.removeStatus();
			super.applyEffects(b, user, victim);
			victim.getStatus().setTurns(3);
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect(Namesies.HEAL_BLOCK_EFFECT))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			victim.healHealthFraction(1/1.0);
			
			b.addMessage(victim.getName() + "'s health was restored!", victim);
		}
	}

	private static class HyperVoice extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public HyperVoice()
		{
			super(Namesies.HYPER_VOICE_ATTACK, "The user lets loose a horribly echoing shout with the power to inflict damage.", 10, Type.NORMAL, Category.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SOUND_BASED);
		}
	}

	private static class LeechLife extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public LeechLife()
		{
			super(Namesies.LEECH_LIFE_ATTACK, "The user drains the target's blood. The user's HP is restored by half the damage taken by the target.", 15, Type.BUG, Category.PHYSICAL);
			super.power = 20;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SAP_HEALTH);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Astonish extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Astonish()
		{
			super(Namesies.ASTONISH_ATTACK, "The user attacks the target while shouting in a startling fashion. It may also make the target flinch.", 15, Type.GHOST, Category.PHYSICAL);
			super.power = 30;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 30;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class AirCutter extends Attack implements CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public AirCutter()
		{
			super(Namesies.AIR_CUTTER_ATTACK, "The user launches razor-like wind to slash the opposing team. Critical hits land more easily.", 25, Type.FLYING, Category.SPECIAL);
			super.power = 60;
			super.accuracy = 95;
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}
	}

	private static class MeanLook extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MeanLook()
		{
			super(Namesies.MEAN_LOOK_ATTACK, "The user pins the target with a dark, arresting look. The target becomes unable to flee.", 5, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.TRAPPED_EFFECT, EffectType.POKEMON));
		}
	}

	private static class Acrobatics extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Acrobatics()
		{
			super(Namesies.ACROBATICS_ATTACK, "The user nimbly strikes the target. If the user is not holding an item, this attack inflicts massive damage.", 15, Type.FLYING, Category.PHYSICAL);
			super.power = 55;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(me.isHoldingItem(b) ? 1 : 2);
		}
	}

	private static class Absorb extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Absorb()
		{
			super(Namesies.ABSORB_ATTACK, "A nutrient-draining attack. The user's HP is restored by half the damage taken by the target.", 25, Type.GRASS, Category.SPECIAL);
			super.power = 20;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SAP_HEALTH);
		}
	}

	private static class MegaDrain extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MegaDrain()
		{
			super(Namesies.MEGA_DRAIN_ATTACK, "A nutrient-draining attack. The user's HP is restored by half the damage taken by the target.", 15, Type.GRASS, Category.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SAP_HEALTH);
		}
	}

	private static class NaturalGift extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public NaturalGift()
		{
			super(Namesies.NATURAL_GIFT_ATTACK, "The user draws power to attack by using its held Berry. The Berry determines its type and power.", 15, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 100;
		}

		public Type setType(Battle b, ActivePokemon user)
		{
			Item i = user.getHeldItem(b);
			if (i instanceof Berry)
			{
				return ((Berry)i).naturalGiftType();
			}
			
			return super.type;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return ((Berry)me.getHeldItem(b)).naturalGiftPower();
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (!(me.getHeldItem(b) instanceof Berry))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			super.apply(me, o, b);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			// This is so fucking stupid that it consumes the Berry upon use, like srsly what the fuck is the fucking point of this move
			if (user.getHeldItem(b) instanceof Berry)
			{
				user.consumeItem(b);
			}
		}
	}

	private static class GigaDrain extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public GigaDrain()
		{
			super(Namesies.GIGA_DRAIN_ATTACK, "A nutrient-draining attack. The user's HP is restored by half the damage taken by the target.", 10, Type.GRASS, Category.SPECIAL);
			super.power = 75;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SAP_HEALTH);
		}
	}

	private static class Aromatherapy extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Aromatherapy()
		{
			super(Namesies.AROMATHERAPY_ATTACK, "The user releases a soothing scent that heals all status problems affecting the user's party.", 5, Type.GRASS, Category.STATUS);
			super.selfTarget = true;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			for (ActivePokemon p : b.getTrainer(user.user()).getTeam())
			{
				if (!p.hasStatus(StatusCondition.FAINTED))
				{
					p.removeStatus();
				}
			}
			
			b.addMessage("All status problems were cured!");
		}
	}

	private static class Spore extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Spore()
		{
			super(Namesies.SPORE_ATTACK, "The user scatters bursts of spores that induce sleep.", 15, Type.GRASS, Category.STATUS);
			super.accuracy = 100;
			super.status = StatusCondition.ASLEEP;
			super.moveTypes.add(MoveType.POWDER);
		}
	}

	private static class CrossPoison extends Attack implements CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public CrossPoison()
		{
			super(Namesies.CROSS_POISON_ATTACK, "A slashing attack with a poisonous blade that may also leave the target poisoned. Critical hits land more easily.", 20, Type.POISON, Category.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.POISONED;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}
	}

	private static class XScissor extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public XScissor()
		{
			super(Namesies.XSCISSOR_ATTACK, "The user slashes at the target by crossing its scythes or claws as if they were a pair of scissors.", 15, Type.BUG, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Foresight extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Foresight()
		{
			super(Namesies.FORESIGHT_ATTACK, "Enables a Ghost-type target to be hit by Normal and Fighting type attacks. It also enables an evasive target to be hit.", 40, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.FORESIGHT_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			super.applyEffects(b, user, victim);
			victim.getAttributes().resetStage(Stat.EVASION);
		}
	}

	private static class OdorSleuth extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public OdorSleuth()
		{
			super(Namesies.ODOR_SLEUTH_ATTACK, "Enables a Ghost-type target to be hit with Normal- and Fighting-type attacks. It also enables an evasive target to be hit.", 40, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.FORESIGHT_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			super.applyEffects(b, user, victim);
			victim.getAttributes().resetStage(Stat.EVASION);
		}
	}

	private static class MiracleEye extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MiracleEye()
		{
			super(Namesies.MIRACLE_EYE_ATTACK, "Enables a Dark-type target to be hit by Psychic-type attacks. It also enables an evasive target to be hit.", 40, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.MIRACLE_EYE_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			super.applyEffects(b, user, victim);
			victim.getAttributes().resetStage(Stat.EVASION);
		}
	}

	private static class Howl extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Howl()
		{
			super(Namesies.HOWL_ATTACK, "The user howls loudly to raise its spirit, boosting its Attack stat.", 40, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
		}
	}

	private static class SignalBeam extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SignalBeam()
		{
			super(Namesies.SIGNAL_BEAM_ATTACK, "The user attacks with a sinister beam of light. It may also confuse the target.", 15, Type.BUG, Category.SPECIAL);
			super.power = 75;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.CONFUSION_EFFECT, EffectType.POKEMON));
			super.effectChance = 10;
		}
	}

	private static class ZenHeadbutt extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public ZenHeadbutt()
		{
			super(Namesies.ZEN_HEADBUTT_ATTACK, "The user focuses its willpower to its head and attacks the target. It may also make the target flinch.", 15, Type.PSYCHIC, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 90;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 20;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Psychic extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Psychic()
		{
			super(Namesies.PSYCHIC_ATTACK, "The target is hit by a strong telekinetic force. It may also reduce the target's Sp. Def stat.", 10, Type.PSYCHIC, Category.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
			super.effectChance = 10;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
		}
	}

	private static class MudSlap extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MudSlap()
		{
			super(Namesies.MUD_SLAP_ATTACK, "The user hurls mud in the target's face to inflict damage and lower its accuracy.", 10, Type.GROUND, Category.SPECIAL);
			super.power = 20;
			super.accuracy = 100;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	private static class Magnitude extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;
		private static int[] chances = {5, 10, 20, 30, 20, 10, 5};
		private static int[] powers = {10, 30, 50, 70, 90, 110, 150};
		private int index;

		public Magnitude()
		{
			super(Namesies.MAGNITUDE_ATTACK, "The user looses a ground-shaking quake affecting everyone around the user. Its power varies.", 30, Type.GROUND, Category.PHYSICAL);
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			int power = powers[index = Global.getPercentageIndex(chances)];
			
			// Power is halved during Grassy Terrain
			if (b.hasEffect(Namesies.GRASSY_TERRAIN_EFFECT))
			{
				power *= .5;
			}
			
			// Power is double when the opponent is underground
			if (o.isSemiInvulnerableDigging())
			{
				power *= 2;
			}
			
			return power;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			b.addMessage("Magnitude " + (index + 4) + "!");
			super.apply(me, o, b);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			// Always hit when the opponent is underground
			return defending.isSemiInvulnerableDigging();
		}
	}

	private static class Bulldoze extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Bulldoze()
		{
			super(Namesies.BULLDOZE_ATTACK, "The user stomps down on the ground and attacks everything in the area. Hit Pok\u00e9mon's Speed stat is reduced.", 20, Type.GROUND, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.statChanges[Stat.SPEED.index()] = -1;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			// Power is halved during Grassy Terrain
			return (int)(super.power*(b.hasEffect(Namesies.GRASSY_TERRAIN_EFFECT) ? .5 : 1));
		}
	}

	private static class Dig extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;

		public Dig()
		{
			super(Namesies.DIG_ATTACK, "The user burrows, then attacks on the second turn. It can also be used to exit dungeons.", 10, Type.GROUND, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public boolean chargesFirst()
		{
			return true;
		}

		public boolean semiInvulnerability()
		{
			return true;
		}

		public void charge(ActivePokemon user, Battle b)
		{
			b.addMessage(getChargeMessage(user));
		}

		public String getChargeMessage(ActivePokemon user)
		{
			return user.getName() + " went underground!";
		}
	}

	private static class Earthquake extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public Earthquake()
		{
			super(Namesies.EARTHQUAKE_ATTACK, "The user sets off an earthquake that strikes those around it.", 10, Type.GROUND, Category.PHYSICAL);
			super.power = 100;
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			int power = super.power;
			
			// Power is halved during Grassy Terrain
			if (b.hasEffect(Namesies.GRASSY_TERRAIN_EFFECT))
			{
				power *= .5;
			}
			
			// Power is double when the opponent is underground
			if (o.isSemiInvulnerableDigging())
			{
				power *= 2;
			}
			
			return power;
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			// Always hit when the opponent is underground
			return defending.isSemiInvulnerableDigging();
		}
	}

	private static class Fissure extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Fissure()
		{
			super(Namesies.FISSURE_ATTACK, "The user opens up a fissure in the ground and drops the target in. The target instantly faints if it hits.", 5, Type.GROUND, Category.PHYSICAL);
			super.accuracy = 30;
			super.moveTypes.add(MoveType.ONE_HIT_KO);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			// Fails when the opponent is at a higher level than the user
			if (me.getLevel() < o.getLevel())
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			// Sturdy prevents OHKO moves if the user is not a mold breaker
			if (o.hasAbility(Namesies.STURDY_ABILITY) && !me.breaksTheMold())
			{
				b.addMessage(o.getName() + "'s " + Namesies.STURDY_ABILITY.getName() + " prevents OHKO moves!");
				return;
			}
			
			// Certain death
			o.reduceHealth(b, o.getHP());
			b.addMessage("It's a One-Hit KO!");
		}

		public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.accuracy + (me.getLevel() - o.getLevel());
		}
	}

	private static class NightSlash extends Attack implements CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public NightSlash()
		{
			super(Namesies.NIGHT_SLASH_ATTACK, "The user slashes the target the instant an opportunity arises. Critical hits land more easily.", 15, Type.DARK, Category.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}
	}

	private static class TriAttack extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public TriAttack()
		{
			super(Namesies.TRI_ATTACK_ATTACK, "The user strikes with a simultaneous three-beam attack. May also burn, freeze, or leave the target with paralysis.", 10, Type.NORMAL, Category.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.effectChance = 20;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			int random = (int)(Math.random()*3);
			if (random == 0) super.status = StatusCondition.PARALYZED;
			else if (random == 1) super.status = StatusCondition.BURNED;
			else if (random == 2) super.status = StatusCondition.FROZEN;
			super.applyEffects(b, user, victim);
		}
	}

	private static class FakeOut extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FakeOut()
		{
			super(Namesies.FAKE_OUT_ATTACK, "An attack that hits first and makes the target flinch. It only works the first turn the user is in battle.", 10, Type.NORMAL, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.priority = 3;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (!me.getAttributes().isFirstTurn())
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			super.apply(me, o, b);
		}
	}

	private static class FeintAttack extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FeintAttack()
		{
			super(Namesies.FEINT_ATTACK_ATTACK, "The user approaches the target disarmingly, then throws a sucker punch. It hits without fail.", 20, Type.DARK, Category.PHYSICAL);
			super.power = 60;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Taunt extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Taunt()
		{
			super(Namesies.TAUNT_ATTACK, "The target is taunted into a rage that allows it to use only attack moves for three turns.", 20, Type.DARK, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.TAUNT_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
		}
	}

	private static class PayDay extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PayDay()
		{
			super(Namesies.PAY_DAY_ATTACK, "Numerous coins are hurled at the target to inflict damage. Money is earned after the battle.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.PAY_DAY_EFFECT, EffectType.TEAM));
			super.selfTarget = true;
		}
	}

	private static class PowerGem extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PowerGem()
		{
			super(Namesies.POWER_GEM_ATTACK, "The user attacks with a ray of light that sparkles as if it were made of gemstones.", 20, Type.ROCK, Category.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
		}
	}

	private static class WaterSport extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public WaterSport()
		{
			super(Namesies.WATER_SPORT_ATTACK, "The user soaks itself with water. The move weakens Fire-type moves while the user is in the battle.", 15, Type.WATER, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.WATER_SPORT_EFFECT, EffectType.BATTLE));
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return true;
		}
	}

	private static class Soak extends Attack implements ChangeTypeMove
	{
		private static final long serialVersionUID = 1L;

		public Soak()
		{
			super(Namesies.SOAK_ATTACK, "The user shoots a torrent of water at the target and changes the target's type to Water.", 20, Type.WATER, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.CHANGE_TYPE_EFFECT, EffectType.POKEMON));
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return new Type[] {Type.WATER, Type.NONE};
		}
	}

	private static class TrickOrTreat extends Attack implements ChangeTypeMove
	{
		private static final long serialVersionUID = 1L;

		public TrickOrTreat()
		{
			super(Namesies.TRICK_OR_TREAT_ATTACK, "The user takes the target trick-or-treating. This adds Ghost type to the target's type.", 20, Type.GHOST, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.CHANGE_TYPE_EFFECT, EffectType.POKEMON));
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			Type primary = victim.getType(b)[0];
			
			return new Type[] {primary, primary == Type.GHOST ? Type.NONE : Type.GHOST};
		}
	}

	private static class ForestsCurse extends Attack implements ChangeTypeMove
	{
		private static final long serialVersionUID = 1L;

		public ForestsCurse()
		{
			super(Namesies.FORESTS_CURSE_ATTACK, "The user puts a forest curse on the target. Afflicted targets are now Grass type as well.", 20, Type.GRASS, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.CHANGE_TYPE_EFFECT, EffectType.POKEMON));
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			Type primary = victim.getType(b)[0];
			
			return new Type[] {primary, primary == Type.GRASS ? Type.NONE : Type.GRASS};
		}
	}

	private static class PsychUp extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PsychUp()
		{
			super(Namesies.PSYCH_UP_ATTACK, "The user hypnotizes itself into copying any stat change made by the target.", 10, Type.NORMAL, Category.STATUS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			for (int i = 0; i < Stat.NUM_BATTLE_STATS; i++)
			{
				user.getAttributes().setStage(i, victim.getStage(i));
			}
			
			b.addMessage(user.getName() + " copied " + victim.getName() + "'s stat changes!");
		}
	}

	private static class Amnesia extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Amnesia()
		{
			super(Namesies.AMNESIA_ATTACK, "The user temporarily empties its mind to forget its concerns. It sharply raises the user's Sp. Def stat.", 20, Type.PSYCHIC, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.SP_DEFENSE.index()] = 2;
		}
	}

	private static class WonderRoom extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public WonderRoom()
		{
			super(Namesies.WONDER_ROOM_ATTACK, "The user creates a bizarre area in which Pok\u00e9mon's Defense and Sp. Def stats are swapped for five turns.", 10, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.WONDER_ROOM_EFFECT, EffectType.BATTLE));
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return true;
		}
	}

	private static class AquaJet extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public AquaJet()
		{
			super(Namesies.AQUA_JET_ATTACK, "The user lunges at the target at a speed that makes it almost invisible. It is sure to strike first.", 20, Type.WATER, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.priority = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Covet extends Attack implements ItemCondition
	{
		private static final long serialVersionUID = 1L;
		private Item item;

		public Covet()
		{
			super(Namesies.COVET_ATTACK, "The user endearingly approaches the target, then steals the target's held item.", 25, Type.NORMAL, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.CHANGE_ITEM_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public String getSwitchMessage(ActivePokemon user, Item userItem, ActivePokemon victim, Item victimItem)
		{
			return user.getName() + " stole " + victim.getName() + "'s " + victimItem.getName() + "!";
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isHoldingItem(b) || !victim.isHoldingItem(b) || b.getTrainer(user.user()) instanceof WildPokemon || victim.hasAbility(Namesies.STICKY_HOLD_ABILITY))
			{
				if (super.category == Category.STATUS)
				{
					b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				}
				
				return;
			}
			
			Item userItem = user.getHeldItem(b), victimItem = victim.getHeldItem(b);
			b.addMessage(getSwitchMessage(user, userItem, victim, victimItem));
			
			if (b.isWildBattle())
			{
				user.giveItem((HoldItem)victimItem);
				victim.giveItem((HoldItem)userItem);
				return;
			}
			
			item = userItem;
			super.applyEffects(b, user, victim);
			
			item = victimItem;
			super.applyEffects(b, user, user);
		}

		public Item getItem()
		{
			return item;
		}
	}

	private static class LowKick extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public LowKick()
		{
			super(Namesies.LOW_KICK_ATTACK, "A powerful low kick that makes the target fall over. It inflicts greater damage on heavier targets.", 20, Type.FIGHTING, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			double weight = o.getWeight(b);
			if (weight < 22) return 20;
			if (weight < 55) return 40;
			if (weight < 110) return 60;
			if (weight < 220) return 80;
			if (weight < 440) return 100;
			return 120;
		}
	}

	private static class KarateChop extends Attack implements CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public KarateChop()
		{
			super(Namesies.KARATE_CHOP_ATTACK, "The target is attacked with a sharp chop. Critical hits land more easily.", 25, Type.FIGHTING, Category.PHYSICAL);
			super.power = 50;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}
	}

	private static class SeismicToss extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SeismicToss()
		{
			super(Namesies.SEISMIC_TOSS_ATTACK, "The target is thrown using the power of gravity. It inflicts damage equal to the user's level.", 20, Type.FIGHTING, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			o.reduceHealth(b, me.getLevel());
		}
	}

	private static class Swagger extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Swagger()
		{
			super(Namesies.SWAGGER_ATTACK, "The user enrages and confuses the target. However, it also sharply raises the target's Attack stat.", 15, Type.NORMAL, Category.STATUS);
			super.accuracy = 90;
			super.effects.add(Effect.getEffect(Namesies.CONFUSION_EFFECT, EffectType.POKEMON));
			super.statChanges[Stat.ATTACK.index()] = 2;
		}
	}

	private static class CrossChop extends Attack implements CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public CrossChop()
		{
			super(Namesies.CROSS_CHOP_ATTACK, "The user delivers a double chop with its forearms crossed. Critical hits land more easily.", 5, Type.FIGHTING, Category.PHYSICAL);
			super.power = 100;
			super.accuracy = 80;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}
	}

	private static class Punishment extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Punishment()
		{
			super(Namesies.PUNISHMENT_ATTACK, "This attack's power increases the more the target has powered up with stat changes.", 5, Type.DARK, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return (int)Math.min(super.power + 20*me.getAttributes().totalStatIncreases(), 200);
		}
	}

	private static class CloseCombat extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public CloseCombat()
		{
			super(Namesies.CLOSE_COMBAT_ATTACK, "The user fights the target up close without guarding itself. It also cuts the user's Defense and Sp. Def.", 5, Type.FIGHTING, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class FlameWheel extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FlameWheel()
		{
			super(Namesies.FLAME_WHEEL_ATTACK, "The user cloaks itself in fire and charges at the target. It may also leave the target with a burn.", 25, Type.FIRE, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.BURNED;
			super.moveTypes.add(MoveType.DEFROST);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Reversal extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Reversal()
		{
			super(Namesies.REVERSAL_ATTACK, "An all-out attack that becomes more powerful the less HP the user has.", 15, Type.FIGHTING, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			double ratio = me.getHPRatio();
			if (ratio > .7) return 20;
			if (ratio > .35) return 40;
			if (ratio > .2) return 80;
			if (ratio > .1) return 100;
			if (ratio > .04) return 150;
			return 200;
		}
	}

	private static class ExtremeSpeed extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public ExtremeSpeed()
		{
			super(Namesies.EXTREME_SPEED_ATTACK, "The user charges the target at blinding speed. This attack always goes before any other move.", 5, Type.NORMAL, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.priority = 2;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Hypnosis extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Hypnosis()
		{
			super(Namesies.HYPNOSIS_ATTACK, "The user employs hypnotic suggestion to make the target fall into a deep sleep.", 20, Type.PSYCHIC, Category.STATUS);
			super.accuracy = 60;
			super.status = StatusCondition.ASLEEP;
		}
	}

	private static class BubbleBeam extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public BubbleBeam()
		{
			super(Namesies.BUBBLE_BEAM_ATTACK, "A spray of bubbles is forcefully ejected at the opposing team. It may also lower their Speed stats.", 20, Type.WATER, Category.SPECIAL);
			super.power = 65;
			super.accuracy = 100;
			super.effectChance = 10;
			super.statChanges[Stat.SPEED.index()] = -1;
		}
	}

	private static class MudShot extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MudShot()
		{
			super(Namesies.MUD_SHOT_ATTACK, "The user attacks by hurling a blob of mud at the target. It also reduces the target's Speed.", 15, Type.GROUND, Category.SPECIAL);
			super.power = 55;
			super.accuracy = 95;
			super.statChanges[Stat.SPEED.index()] = -1;
		}
	}

	private static class BellyDrum extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public BellyDrum()
		{
			super(Namesies.BELLY_DRUM_ATTACK, "The user maximizes its Attack stat in exchange for HP equal to half its max HP.", 10, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			// Fails if attack is already maxed or if you have less than half your health to give up
			if (user.getStage(Stat.ATTACK.index()) == Stat.MAX_STAT_CHANGES || user.getHPRatio() < .5)
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			// Maximization station
			b.addMessage(user.getName() + " cut its own HP and maximized its attack!");
			user.reduceHealthFraction(b, 1/2.0);
			user.getAttributes().setStage(Stat.ATTACK.index(), Stat.MAX_STAT_CHANGES);
		}
	}

	private static class Submission extends Attack implements RecoilMove
	{
		private static final long serialVersionUID = 1L;

		public Submission()
		{
			super(Namesies.SUBMISSION_ATTACK, "The user grabs the target and recklessly dives for the ground. It also hurts the user slightly.", 25, Type.FIGHTING, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 80;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyRecoil(Battle b, ActivePokemon user, Integer damage)
		{
			if (user.hasAbility(Namesies.ROCK_HEAD_ABILITY) || user.hasAbility(Namesies.MAGIC_GUARD_ABILITY))
			{
				return;
			}
			
			b.addMessage(user.getName() + " was hurt by recoil!");
			user.reduceHealth(b, (int)Math.ceil(damage/4.0));
		}
	}

	private static class DynamicPunch extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DynamicPunch()
		{
			super(Namesies.DYNAMIC_PUNCH_ATTACK, "The user punches the target with full, concentrated power. It confuses the target if it hits.", 5, Type.FIGHTING, Category.PHYSICAL);
			super.power = 100;
			super.accuracy = 50;
			super.effects.add(Effect.getEffect(Namesies.CONFUSION_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.PUNCHING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class MindReader extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MindReader()
		{
			super(Namesies.MIND_READER_ATTACK, "The user senses the target's movements with its mind to ensure its next attack does not miss the target.", 5, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.LOCK_ON_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}
	}

	private static class LockOn extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public LockOn()
		{
			super(Namesies.LOCK_ON_ATTACK, "The user takes sure aim at the target. It ensures the next attack does not fail to hit the target.", 5, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.LOCK_ON_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}
	}

	private static class Kinesis extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Kinesis()
		{
			super(Namesies.KINESIS_ATTACK, "The user distracts the target by bending a spoon. It lowers the target's accuracy.", 15, Type.PSYCHIC, Category.STATUS);
			super.accuracy = 80;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	private static class Barrier extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Barrier()
		{
			super(Namesies.BARRIER_ATTACK, "The user throws up a sturdy wall that sharply raises its Defense stat.", 20, Type.PSYCHIC, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 2;
		}
	}

	private static class Telekinesis extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Telekinesis()
		{
			super(Namesies.TELEKINESIS_ATTACK, "The user makes the target float with its psychic power. The target is easier to hit for three turns.", 15, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.TELEKINESIS_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.AIRBORNE);
		}
	}

	private static class Ingrain extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Ingrain()
		{
			super(Namesies.INGRAIN_ATTACK, "The user lays roots that restore its HP on every turn. Because it is rooted, it can't switch out.", 20, Type.GRASS, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.INGRAIN_EFFECT, EffectType.POKEMON));
			super.selfTarget = true;
		}
	}

	private static class PsychoCut extends Attack implements CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public PsychoCut()
		{
			super(Namesies.PSYCHO_CUT_ATTACK, "The user tears at the target with blades formed by psychic power. Critical hits land more easily.", 20, Type.PSYCHIC, Category.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}
	}

	private static class FutureSight extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FutureSight()
		{
			super(Namesies.FUTURE_SIGHT_ATTACK, "Two turns after this move is used, a hunk of psychic energy attacks the target.", 10, Type.PSYCHIC, Category.SPECIAL);
			super.power = 120;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.FUTURE_SIGHT_EFFECT, EffectType.TEAM));
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			super.applyEffects(b, me, o); // Don't apply damage just yet!!
		}

		public boolean canPrintFail()
		{
			return true;
		}
	}

	private static class DoomDesire extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DoomDesire()
		{
			super(Namesies.DOOM_DESIRE_ATTACK, "Two turns after this move is used, the user blasts the target with a concentrated bundle of light.", 5, Type.STEEL, Category.SPECIAL);
			super.power = 140;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.DOOM_DESIRE_EFFECT, EffectType.TEAM));
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			super.applyEffects(b, me, o); // Don't apply damage just yet!!
		}

		public boolean canPrintFail()
		{
			return true;
		}
	}

	private static class CalmMind extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public CalmMind()
		{
			super(Namesies.CALM_MIND_ATTACK, "The user quietly focuses its mind and calms its spirit to raise its Sp. Atk and Sp. Def stats.", 20, Type.PSYCHIC, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
		}
	}

	private static class LowSweep extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public LowSweep()
		{
			super(Namesies.LOW_SWEEP_ATTACK, "The user attacks the target's legs swiftly, reducing the target's Speed stat.", 20, Type.FIGHTING, Category.PHYSICAL);
			super.power = 65;
			super.accuracy = 100;
			super.statChanges[Stat.SPEED.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Revenge extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Revenge()
		{
			super(Namesies.REVENGE_ATTACK, "An attack move that inflicts double the damage if the user has been hurt by the opponent in the same turn.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.priority = -4;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(me.getAttributes().hasTakenDamage() ? 2 : 1);
		}
	}

	private static class VitalThrow extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public VitalThrow()
		{
			super(Namesies.VITAL_THROW_ATTACK, "The user attacks last. In return, this throw move is guaranteed not to miss.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.power = 70;
			super.priority = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class WringOut extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public WringOut()
		{
			super(Namesies.WRING_OUT_ATTACK, "The user powerfully wrings the target. The more HP the target has, the greater this attack's power.", 5, Type.NORMAL, Category.SPECIAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return (int)Math.min(1, (120*o.getHPRatio()));
		}
	}

	private static class LeafTornado extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public LeafTornado()
		{
			super(Namesies.LEAF_TORNADO_ATTACK, "The user attacks its target by encircling it in sharp leaves. This attack may also lower the target's accuracy.", 10, Type.GRASS, Category.SPECIAL);
			super.power = 65;
			super.accuracy = 90;
			super.effectChance = 30;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	private static class LeafStorm extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public LeafStorm()
		{
			super(Namesies.LEAF_STORM_ATTACK, "The user whips up a storm of leaves around the target. The attack's recoil harshly reduces the user's Sp. Atk stat.", 5, Type.GRASS, Category.SPECIAL);
			super.power = 130;
			super.accuracy = 90;
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = -2;
		}
	}

	private static class LeafBlade extends Attack implements CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public LeafBlade()
		{
			super(Namesies.LEAF_BLADE_ATTACK, "The user handles a sharp leaf like a sword and attacks by cutting its target. Critical hits land more easily.", 15, Type.GRASS, Category.PHYSICAL);
			super.power = 90;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}
	}

	private static class Constrict extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Constrict()
		{
			super(Namesies.CONSTRICT_ATTACK, "The target is attacked with long, creeping tentacles or vines. It may also lower the target's Speed stat.", 35, Type.NORMAL, Category.PHYSICAL);
			super.power = 10;
			super.accuracy = 100;
			super.effectChance = 10;
			super.statChanges[Stat.SPEED.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Hex extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Hex()
		{
			super(Namesies.HEX_ATTACK, "This relentless attack does massive damage to a target affected by status problems.", 10, Type.GHOST, Category.SPECIAL);
			super.power = 65;
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(o.hasStatus() ? 2 : 1);
		}
	}

	private static class SludgeWave extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SludgeWave()
		{
			super(Namesies.SLUDGE_WAVE_ATTACK, "It swamps the area around the user with a giant sludge wave. It may also poison those hit.", 10, Type.POISON, Category.SPECIAL);
			super.power = 95;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.POISONED;
		}
	}

	private static class MudSport extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public MudSport()
		{
			super(Namesies.MUD_SPORT_ATTACK, "The user covers itself with mud. It weakens Electric-type moves while the user is in the battle.", 15, Type.GROUND, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.MUD_SPORT_EFFECT, EffectType.BATTLE));
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return true;
		}
	}

	private static class RockPolish extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public RockPolish()
		{
			super(Namesies.ROCK_POLISH_ATTACK, "The user polishes its body to reduce drag. It can sharply raise the Speed stat.", 20, Type.ROCK, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.SPEED.index()] = 2;
		}
	}

	private static class RockThrow extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public RockThrow()
		{
			super(Namesies.ROCK_THROW_ATTACK, "The user picks up and throws a small rock at the target to attack.", 15, Type.ROCK, Category.PHYSICAL);
			super.power = 50;
			super.accuracy = 90;
		}
	}

	private static class RockBlast extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public RockBlast()
		{
			super(Namesies.ROCK_BLAST_ATTACK, "The user hurls hard rocks at the target. Two to five rocks are launched in quick succession.", 10, Type.ROCK, Category.PHYSICAL);
			super.power = 25;
			super.accuracy = 90;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int minHits = 2;
			int maxHits = 5;
			
			int hits = (int)(Math.random()*(maxHits - minHits + 1)) + minHits;
			
			if (maxHits == 5 && me.hasAbility(Namesies.SKILL_LINK_ABILITY))
			{
				hits = 5;
			}
			
			int hit = 1;
			for (; hit <= hits; hit++)
			{
				b.addMessage("Hit " + hit + "!");
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b))
				{
					break;
				}
			}
			
			// Print hits and gtfo
			b.addMessage("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!");
		}
	}

	private static class SmackDown extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public SmackDown()
		{
			super(Namesies.SMACK_DOWN_ATTACK, "The user throws a stone or projectile to attack an opponent. A flying Pok\u00e9mon will fall to the ground when hit.", 15, Type.ROCK, Category.PHYSICAL);
			super.power = 50;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.GROUNDED_EFFECT, EffectType.POKEMON));
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			// Twice as strong when the opponent is flying
			return super.power*(o.isSemiInvulnerableFlying() ? 2 : 1);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			// Always hit when the opponent is flying
			return defending.isSemiInvulnerableFlying();
		}
	}

	private static class StealthRock extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public StealthRock()
		{
			super(Namesies.STEALTH_ROCK_ATTACK, "The user lays a trap of levitating stones around the opponent's team. The trap hurts opponents that switch into battle.", 20, Type.ROCK, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.STEALTH_ROCK_EFFECT, EffectType.TEAM));
			super.moveTypes.add(MoveType.FIELD);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return true;
		}
	}

	private static class StoneEdge extends Attack implements CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public StoneEdge()
		{
			super(Namesies.STONE_EDGE_ATTACK, "The user stabs the foe with sharpened stones from below. It has a high critical-hit ratio.", 5, Type.ROCK, Category.PHYSICAL);
			super.power = 100;
			super.accuracy = 80;
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}
	}

	private static class Steamroller extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public Steamroller()
		{
			super(Namesies.STEAMROLLER_ATTACK, "The user crushes its targets by rolling over them with its rolled-up body. This attack may make the target flinch.", 20, Type.BUG, Category.PHYSICAL);
			super.power = 65;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 30;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(o.hasEffect(Namesies.USED_MINIMIZE_EFFECT) ? 2 : 1);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return !defending.isSemiInvulnerable() && defending.hasEffect(Namesies.USED_MINIMIZE_EFFECT);
		}
	}

	private static class HeavySlam extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public HeavySlam()
		{
			super(Namesies.HEAVY_SLAM_ATTACK, "The user slams into the target with its heavy body. The more the user outweighs the target, the greater its damage.", 10, Type.STEEL, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			double ratio = o.getWeight(b)/me.getWeight(b);
			if (ratio > .5) return 40;
			if (ratio > .33) return 60;
			if (ratio > .25) return 80;
			if (ratio > .2) return 100;
			return 120;
		}
	}

	private static class Stomp extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public Stomp()
		{
			super(Namesies.STOMP_ATTACK, "The target is stomped with a big foot. It may also make the target flinch.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 65;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 30;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(o.hasEffect(Namesies.USED_MINIMIZE_EFFECT) ? 2 : 1);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return !defending.isSemiInvulnerable() && defending.hasEffect(Namesies.USED_MINIMIZE_EFFECT);
		}
	}

	private static class FlameCharge extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FlameCharge()
		{
			super(Namesies.FLAME_CHARGE_ATTACK, "The user cloaks itself with flame and attacks. Building up more power, it raises the user's Speed stat.", 20, Type.FIRE, Category.PHYSICAL);
			super.power = 50;
			super.accuracy = 100;
			super.selfTarget = true;
			super.statChanges[Stat.SPEED.index()] = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Bounce extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;

		public Bounce()
		{
			super(Namesies.BOUNCE_ATTACK, "The user bounces up high, then drops on the target on the second turn. It may also leave the target with paralysis.", 5, Type.FLYING, Category.PHYSICAL);
			super.power = 85;
			super.accuracy = 85;
			super.effectChance = 30;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.AIRBORNE);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public boolean chargesFirst()
		{
			return true;
		}

		public boolean semiInvulnerability()
		{
			return true;
		}

		public void charge(ActivePokemon user, Battle b)
		{
			b.addMessage(getChargeMessage(user));
		}

		public String getChargeMessage(ActivePokemon user)
		{
			return user.getName() + " sprang up!";
		}
	}

	private static class Curse extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Curse()
		{
			super(Namesies.CURSE_ATTACK, "A move that works differently for the Ghost type than for all other types.", 10, Type.GHOST, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.CURSE_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.statChanges[Stat.SPEED.index()] = -1;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			// Different effects based on the type of the user
			if (me.isType(b, Type.GHOST))
			{
				// Manually apply the effect if it applies
				if (effects.get(0).applies(b, me, o, CastSource.ATTACK))
				{
					effects.get(0).cast(b, me, o, CastSource.ATTACK, super.printCast);
				}
				else
				{
					b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				}
				return;
			}
			
			me.modifyStages(b, me, statChanges, CastSource.ATTACK);
		}
	}

	private static class Yawn extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Yawn()
		{
			super(Namesies.YAWN_ATTACK, "The user lets loose a huge yawn that lulls the target into falling asleep on the next turn.", 10, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.YAWN_EFFECT, EffectType.POKEMON));
		}
	}

	private static class Headbutt extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Headbutt()
		{
			super(Namesies.HEADBUTT_ATTACK, "The user sticks out its head and attacks by charging straight into the target. It may also make the target flinch.", 15, Type.NORMAL, Category.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 30;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class SlackOff extends Attack implements SelfHealingMove
	{
		private static final long serialVersionUID = 1L;

		public SlackOff()
		{
			super(Namesies.SLACK_OFF_ATTACK, "The user slacks off, restoring its own HP by up to half of its maximum HP.", 10, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect(Namesies.HEAL_BLOCK_EFFECT))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			victim.healHealthFraction(1/2.0);
			
			b.addMessage(victim.getName() + "'s health was restored!", victim);
		}
	}

	private static class HealPulse extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public HealPulse()
		{
			super(Namesies.HEAL_PULSE_ATTACK, "The user emits a healing pulse which restores the target's HP by up to half of its max HP.", 10, Type.PSYCHIC, Category.STATUS);
			super.moveTypes.add(MoveType.AURA_PULSE);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (victim.fullHealth() || victim.hasEffect(Namesies.HEAL_BLOCK_EFFECT))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			// Heal by 50% unless the user has Mega Launcher -- then heal by 75%
			double fraction = user.hasAbility(Namesies.MEGA_LAUNCHER_ABILITY) ? .75 : .5;
			
			victim.healHealthFraction(fraction);
			b.addMessage(victim.getName() + "'s health was restored!", victim);
		}
	}

	private static class MetalSound extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MetalSound()
		{
			super(Namesies.METAL_SOUND_ATTACK, "A horrible sound like scraping metal harshly reduces the target's Sp. Def stat.", 40, Type.STEEL, Category.STATUS);
			super.accuracy = 85;
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.statChanges[Stat.SP_DEFENSE.index()] = -2;
		}
	}

	private static class Spark extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Spark()
		{
			super(Namesies.SPARK_ATTACK, "The user throws an electrically charged tackle at the target. It may also leave the target with paralysis.", 20, Type.ELECTRIC, Category.PHYSICAL);
			super.power = 65;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class MagnetBomb extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MagnetBomb()
		{
			super(Namesies.MAGNET_BOMB_ATTACK, "The user launches steel bombs that stick to the target. This attack will not miss.", 20, Type.STEEL, Category.PHYSICAL);
			super.power = 60;
			super.moveTypes.add(MoveType.BOMB_BALL);
		}
	}

	private static class MirrorShot extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MirrorShot()
		{
			super(Namesies.MIRROR_SHOT_ATTACK, "The user looses a flash of energy at the target from its polished body. It may also lower the target's accuracy.", 10, Type.STEEL, Category.SPECIAL);
			super.power = 65;
			super.accuracy = 85;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	private static class MagnetRise extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MagnetRise()
		{
			super(Namesies.MAGNET_RISE_ATTACK, "The user levitates using electrically generated magnetism for five turns.", 10, Type.ELECTRIC, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.MAGNET_RISE_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.AIRBORNE);
			super.selfTarget = true;
		}
	}

	private static class ZapCannon extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public ZapCannon()
		{
			super(Namesies.ZAP_CANNON_ATTACK, "The user fires an electric blast like a cannon to inflict damage and cause paralysis.", 5, Type.ELECTRIC, Category.SPECIAL);
			super.power = 120;
			super.accuracy = 50;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.BOMB_BALL);
		}
	}

	private static class BraveBird extends Attack implements RecoilMove
	{
		private static final long serialVersionUID = 1L;

		public BraveBird()
		{
			super(Namesies.BRAVE_BIRD_ATTACK, "The user tucks in its wings and charges from a low altitude. The user also takes serious damage.", 15, Type.FLYING, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyRecoil(Battle b, ActivePokemon user, Integer damage)
		{
			if (user.hasAbility(Namesies.ROCK_HEAD_ABILITY) || user.hasAbility(Namesies.MAGIC_GUARD_ABILITY))
			{
				return;
			}
			
			b.addMessage(user.getName() + " was hurt by recoil!");
			user.reduceHealth(b, (int)Math.ceil(damage/3.0));
		}
	}

	private static class Uproar extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Uproar()
		{
			super(Namesies.UPROAR_ATTACK, "The user attacks in an uproar for three turns. Over that time, no one can fall asleep.", 10, Type.NORMAL, Category.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.UPROAR_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.selfTarget = true;
		}
	}

	private static class Acupressure extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Acupressure()
		{
			super(Namesies.ACUPRESSURE_ATTACK, "The user applies pressure to stress points, sharply boosting one of its stats.", 30, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			super.statChanges = new int[Stat.NUM_BATTLE_STATS];
			super.statChanges[(int)(Math.random()*Stat.NUM_BATTLE_STATS)] = 2;
			
			super.applyEffects(b, user, victim);
		}
	}

	private static class DoubleHit extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DoubleHit()
		{
			super(Namesies.DOUBLE_HIT_ATTACK, "The user slams the target with a long tail, vines, or tentacle. The target is hit twice in a row.", 10, Type.NORMAL, Category.PHYSICAL);
			super.power = 35;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int minHits = 2;
			int maxHits = 2;
			
			int hits = (int)(Math.random()*(maxHits - minHits + 1)) + minHits;
			
			if (maxHits == 5 && me.hasAbility(Namesies.SKILL_LINK_ABILITY))
			{
				hits = 5;
			}
			
			int hit = 1;
			for (; hit <= hits; hit++)
			{
				b.addMessage("Hit " + hit + "!");
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b))
				{
					break;
				}
			}
			
			// Print hits and gtfo
			b.addMessage("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!");
		}
	}

	private static class IcyWind extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public IcyWind()
		{
			super(Namesies.ICY_WIND_ATTACK, "The user attacks with a gust of chilled air. It also reduces the targets' Speed stat.", 15, Type.ICE, Category.SPECIAL);
			super.power = 55;
			super.accuracy = 95;
			super.statChanges[Stat.SPEED.index()] = -1;
		}
	}

	private static class IceShard extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public IceShard()
		{
			super(Namesies.ICE_SHARD_ATTACK, "The user flash freezes chunks of ice and hurls them at the target. This move always goes first.", 30, Type.ICE, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.priority = 1;
		}
	}

	private static class AquaRing extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public AquaRing()
		{
			super(Namesies.AQUA_RING_ATTACK, "The user envelops itself in a veil made of water. It regains some HP on every turn.", 20, Type.WATER, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.AQUA_RING_EFFECT, EffectType.POKEMON));
			super.selfTarget = true;
		}
	}

	private static class AuroraBeam extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public AuroraBeam()
		{
			super(Namesies.AURORA_BEAM_ATTACK, "The target is hit with a rainbow-colored beam. This may also lower the target's Attack stat.", 20, Type.ICE, Category.SPECIAL);
			super.power = 65;
			super.accuracy = 100;
			super.effectChance = 10;
			super.statChanges[Stat.ATTACK.index()] = -1;
		}
	}

	private static class Brine extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Brine()
		{
			super(Namesies.BRINE_ATTACK, "If the target's HP is down to about half, this attack will hit with double the power.", 10, Type.WATER, Category.SPECIAL);
			super.power = 65;
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(me.getHPRatio() < .5 ? 2 : 1);
		}
	}

	private static class Dive extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;

		public Dive()
		{
			super(Namesies.DIVE_ATTACK, "Diving on the first turn, the user floats up and attacks on the second turn. It can be used to dive deep in the ocean.", 10, Type.WATER, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public boolean chargesFirst()
		{
			return true;
		}

		public boolean semiInvulnerability()
		{
			return true;
		}

		public void charge(ActivePokemon user, Battle b)
		{
			b.addMessage(getChargeMessage(user));
		}

		public String getChargeMessage(ActivePokemon user)
		{
			return user.getName() + " hid underwater!";
		}
	}

	private static class IceBeam extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public IceBeam()
		{
			super(Namesies.ICE_BEAM_ATTACK, "The target is struck with an icy-cold beam of energy. It may also freeze the target solid.", 10, Type.ICE, Category.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.FROZEN;
		}
	}

	private static class SheerCold extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SheerCold()
		{
			super(Namesies.SHEER_COLD_ATTACK, "The target is attacked with a blast of absolute-zero cold. The target instantly faints if it hits.", 5, Type.ICE, Category.SPECIAL);
			super.accuracy = 30;
			super.moveTypes.add(MoveType.ONE_HIT_KO);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			// Fails when the opponent is at a higher level than the user
			if (me.getLevel() < o.getLevel())
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			// Sturdy prevents OHKO moves if the user is not a mold breaker
			if (o.hasAbility(Namesies.STURDY_ABILITY) && !me.breaksTheMold())
			{
				b.addMessage(o.getName() + "'s " + Namesies.STURDY_ABILITY.getName() + " prevents OHKO moves!");
				return;
			}
			
			// Certain death
			o.reduceHealth(b, o.getHP());
			b.addMessage("It's a One-Hit KO!");
		}

		public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.accuracy + (me.getLevel() - o.getLevel());
		}
	}

	private static class PoisonGas extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PoisonGas()
		{
			super(Namesies.POISON_GAS_ATTACK, "A cloud of poison gas is sprayed in the face of opposing Pok\u00e9mon. It may poison those hit.", 40, Type.POISON, Category.STATUS);
			super.accuracy = 90;
			super.status = StatusCondition.POISONED;
		}
	}

	private static class Sludge extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Sludge()
		{
			super(Namesies.SLUDGE_ATTACK, "Unsanitary sludge is hurled at the target. It may also poison the target.", 20, Type.POISON, Category.SPECIAL);
			super.power = 65;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.POISONED;
		}
	}

	private static class SludgeBomb extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SludgeBomb()
		{
			super(Namesies.SLUDGE_BOMB_ATTACK, "Unsanitary sludge is hurled at the target. It may also poison the target.", 10, Type.POISON, Category.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.POISONED;
			super.moveTypes.add(MoveType.BOMB_BALL);
		}
	}

	private static class AcidArmor extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public AcidArmor()
		{
			super(Namesies.ACID_ARMOR_ATTACK, "The user alters its cellular structure to liquefy itself, sharply raising its Defense stat.", 20, Type.POISON, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 2;
		}
	}

	private static class IcicleSpear extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public IcicleSpear()
		{
			super(Namesies.ICICLE_SPEAR_ATTACK, "The user launches sharp icicles at the target. It strikes two to five times in a row.", 30, Type.ICE, Category.PHYSICAL);
			super.power = 25;
			super.accuracy = 100;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int minHits = 2;
			int maxHits = 5;
			
			int hits = (int)(Math.random()*(maxHits - minHits + 1)) + minHits;
			
			if (maxHits == 5 && me.hasAbility(Namesies.SKILL_LINK_ABILITY))
			{
				hits = 5;
			}
			
			int hit = 1;
			for (; hit <= hits; hit++)
			{
				b.addMessage("Hit " + hit + "!");
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b))
				{
					break;
				}
			}
			
			// Print hits and gtfo
			b.addMessage("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!");
		}
	}

	private static class Clamp extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Clamp()
		{
			super(Namesies.CLAMP_ATTACK, "The target is clamped and squeezed by the user's very thick and sturdy shell for four to five turns.", 15, Type.WATER, Category.PHYSICAL);
			super.power = 35;
			super.accuracy = 85;
			super.effects.add(Effect.getEffect(Namesies.CLAMPED_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class RazorShell extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public RazorShell()
		{
			super(Namesies.RAZOR_SHELL_ATTACK, "The user cuts its target with sharp shells. This attack may also lower the target's Defense stat.", 10, Type.WATER, Category.PHYSICAL);
			super.power = 75;
			super.accuracy = 95;
			super.effectChance = 50;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Whirlpool extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Whirlpool()
		{
			super(Namesies.WHIRLPOOL_ATTACK, "Traps foes in a violent swirling whirlpool for four to five turns.", 15, Type.WATER, Category.SPECIAL);
			super.power = 35;
			super.accuracy = 85;
			super.effects.add(Effect.getEffect(Namesies.WHIRLPOOLED_EFFECT, EffectType.POKEMON));
		}
	}

	private static class ShellSmash extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public ShellSmash()
		{
			super(Namesies.SHELL_SMASH_ATTACK, "The user breaks its shell, lowering its Defense and Sp. Def stats but sharply raising Attack, Sp. Atk, and Speed stats.", 15, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
			super.statChanges[Stat.ATTACK.index()] = 2;
			super.statChanges[Stat.SP_ATTACK.index()] = 2;
			super.statChanges[Stat.SPEED.index()] = 2;
		}
	}

	private static class SpikeCannon extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SpikeCannon()
		{
			super(Namesies.SPIKE_CANNON_ATTACK, "Sharp spikes are shot at the target in rapid succession. They hit two to five times in a row.", 15, Type.NORMAL, Category.PHYSICAL);
			super.power = 20;
			super.accuracy = 100;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int minHits = 2;
			int maxHits = 5;
			
			int hits = (int)(Math.random()*(maxHits - minHits + 1)) + minHits;
			
			if (maxHits == 5 && me.hasAbility(Namesies.SKILL_LINK_ABILITY))
			{
				hits = 5;
			}
			
			int hit = 1;
			for (; hit <= hits; hit++)
			{
				b.addMessage("Hit " + hit + "!");
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b))
				{
					break;
				}
			}
			
			// Print hits and gtfo
			b.addMessage("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!");
		}
	}

	private static class Spikes extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public Spikes()
		{
			super(Namesies.SPIKES_ATTACK, "The user lays a trap of spikes at the opposing team's feet. The trap hurts Pok\u00e9mon that switch into battle.", 20, Type.GROUND, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.SPIKES_EFFECT, EffectType.TEAM));
			super.moveTypes.add(MoveType.FIELD);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return true;
		}
	}

	private static class IcicleCrash extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public IcicleCrash()
		{
			super(Namesies.ICICLE_CRASH_ATTACK, "The user attacks by harshly dropping an icicle onto the target. It may also make the target flinch.", 10, Type.ICE, Category.PHYSICAL);
			super.power = 85;
			super.accuracy = 90;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 30;
		}
	}

	private static class Lick extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Lick()
		{
			super(Namesies.LICK_ATTACK, "The target is licked with a long tongue, causing damage. It may also leave the target with paralysis.", 30, Type.GHOST, Category.PHYSICAL);
			super.power = 30;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Spite extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Spite()
		{
			super(Namesies.SPITE_ATTACK, "The user unleashes its grudge on the move last used by the target by cutting 4 PP from it.", 10, Type.GHOST, Category.STATUS);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Move last = victim.getAttributes().getLastMoveUsed();
			
			// Fails if the victim hasn't attacked yet, their last move already has 0 PP, or they don't actually know the last move they used
			if (last == null || last.getPP() == 0 || !victim.hasMove(b, last.getAttack().namesies()))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			b.addMessage(victim.getName() + "'s " + last.getAttack().getName() + "'s PP was reduced by " + last.reducePP(4) + "!");
		}
	}

	private static class NightShade extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public NightShade()
		{
			super(Namesies.NIGHT_SHADE_ATTACK, "The user makes the target see a frightening mirage. It inflicts damage matching the user's level.", 15, Type.GHOST, Category.SPECIAL);
			super.accuracy = 100;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			o.reduceHealth(b, me.getLevel());
		}
	}

	private static class ShadowBall extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public ShadowBall()
		{
			super(Namesies.SHADOW_BALL_ATTACK, "The user hurls a shadowy blob at the target. It may also lower the target's Sp. Def stat.", 15, Type.GHOST, Category.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.effectChance = 20;
			super.moveTypes.add(MoveType.BOMB_BALL);
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
		}
	}

	private static class DreamEater extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DreamEater()
		{
			super(Namesies.DREAM_EATER_ATTACK, "The user eats the dreams of a sleeping target. It absorbs half the damage caused to heal the user's HP.", 15, Type.PSYCHIC, Category.SPECIAL);
			super.power = 100;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SAP_HEALTH);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (!o.hasStatus(StatusCondition.ASLEEP))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			super.apply(me, o, b);
		}
	}

	private static class DarkPulse extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DarkPulse()
		{
			super(Namesies.DARK_PULSE_ATTACK, "The user releases a horrible aura imbued with dark thoughts. It may also make the target flinch.", 15, Type.DARK, Category.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 20;
			super.moveTypes.add(MoveType.AURA_PULSE);
		}
	}

	private static class Nightmare extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Nightmare()
		{
			super(Namesies.NIGHTMARE_ATTACK, "A sleeping target sees a nightmare that inflicts some damage every turn.", 15, Type.GHOST, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.NIGHTMARE_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}
	}

	private static class ShadowPunch extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public ShadowPunch()
		{
			super(Namesies.SHADOW_PUNCH_ATTACK, "The user throws a punch from the shadows. The punch lands without fail.", 20, Type.GHOST, Category.PHYSICAL);
			super.power = 60;
			super.moveTypes.add(MoveType.PUNCHING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Bind extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Bind()
		{
			super(Namesies.BIND_ATTACK, "Things such as long bodies or tentacles are used to bind and squeeze the target for four to five turns.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 15;
			super.accuracy = 85;
			super.effects.add(Effect.getEffect(Namesies.BINDED_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class RockTomb extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public RockTomb()
		{
			super(Namesies.ROCK_TOMB_ATTACK, "Boulders are hurled at the target. It also lowers the target's Speed by preventing its movement.", 10, Type.ROCK, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 95;
			super.statChanges[Stat.SPEED.index()] = -1;
		}
	}

	private static class DragonBreath extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DragonBreath()
		{
			super(Namesies.DRAGON_BREATH_ATTACK, "The user exhales a mighty gust that inflicts damage. It may also leave the target with paralysis.", 20, Type.DRAGON, Category.SPECIAL);
			super.power = 60;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.PARALYZED;
		}
	}

	private static class IronTail extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public IronTail()
		{
			super(Namesies.IRON_TAIL_ATTACK, "The target is slammed with a steel-hard tail. It may also lower the target's Defense stat.", 15, Type.STEEL, Category.PHYSICAL);
			super.power = 100;
			super.accuracy = 75;
			super.effectChance = 30;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Meditate extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Meditate()
		{
			super(Namesies.MEDITATE_ATTACK, "The user meditates to awaken the power deep within its body and raise its Attack stat.", 40, Type.PSYCHIC, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = -1;
		}
	}

	private static class Synchronoise extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Synchronoise()
		{
			super(Namesies.SYNCHRONOISE_ATTACK, "Using an odd shock wave, the user inflicts damage on any Pok\u00e9mon of the same type in the area around it.", 15, Type.PSYCHIC, Category.SPECIAL);
			super.power = 120;
			super.accuracy = 100;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			Type[] type = me.getType(b);
			
			// Like this is literally the stupidest move ever like srsly what is wrong with the creators
			if (o.isType(b, type[0]) || (type[1] != Type.NONE && o.isType(b, type[1])))
			{
				super.apply(me, o, b);
			}
			else
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
			}
		}
	}

	private static class Psyshock extends Attack implements StatSwitchingEffect
	{
		private static final long serialVersionUID = 1L;

		public Psyshock()
		{
			super(Namesies.PSYSHOCK_ATTACK, "The user materializes an odd psychic wave to attack the target. This attack does physical damage.", 10, Type.PSYCHIC, Category.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
		}

		public Stat switchStat(Stat s)
		{
			return s == Stat.SP_DEFENSE ? Stat.DEFENSE : s;
		}
	}

	private static class ViceGrip extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public ViceGrip()
		{
			super(Namesies.VICE_GRIP_ATTACK, "The target is gripped and squeezed from both sides to inflict damage.", 30, Type.NORMAL, Category.PHYSICAL);
			super.power = 55;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class MetalClaw extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MetalClaw()
		{
			super(Namesies.METAL_CLAW_ATTACK, "The target is raked with steel claws. It may also raise the user's Attack stat.", 35, Type.STEEL, Category.PHYSICAL);
			super.power = 50;
			super.accuracy = 95;
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Guillotine extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Guillotine()
		{
			super(Namesies.GUILLOTINE_ATTACK, "A vicious, tearing attack with big pincers. The target will faint instantly if this attack hits.", 5, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 30;
			super.moveTypes.add(MoveType.ONE_HIT_KO);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			// Fails when the opponent is at a higher level than the user
			if (me.getLevel() < o.getLevel())
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			// Sturdy prevents OHKO moves if the user is not a mold breaker
			if (o.hasAbility(Namesies.STURDY_ABILITY) && !me.breaksTheMold())
			{
				b.addMessage(o.getName() + "'s " + Namesies.STURDY_ABILITY.getName() + " prevents OHKO moves!");
				return;
			}
			
			// Certain death
			o.reduceHealth(b, o.getHP());
			b.addMessage("It's a One-Hit KO!");
		}

		public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.accuracy + (me.getLevel() - o.getLevel());
		}
	}

	private static class Crabhammer extends Attack implements CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public Crabhammer()
		{
			super(Namesies.CRABHAMMER_ATTACK, "The target is hammered with a large pincer. Critical hits land more easily.", 10, Type.WATER, Category.PHYSICAL);
			super.power = 100;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}
	}

	private static class Flail extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Flail()
		{
			super(Namesies.FLAIL_ATTACK, "The user flails about aimlessly to attack. It becomes more powerful the less HP the user has.", 15, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			double ratio = me.getHPRatio();
			if (ratio > .7) return 20;
			if (ratio > .35) return 40;
			if (ratio > .2) return 80;
			if (ratio > .1) return 100;
			if (ratio > .04) return 150;
			return 200;
		}
	}

	private static class Charge extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Charge()
		{
			super(Namesies.CHARGE_ATTACK, "The user boosts the power of the Electric move it uses on the next turn. It also raises the user's Sp. Def stat.", 20, Type.ELECTRIC, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.CHARGE_EFFECT, EffectType.POKEMON));
			super.selfTarget = true;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
		}
	}

	private static class ChargeBeam extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public ChargeBeam()
		{
			super(Namesies.CHARGE_BEAM_ATTACK, "The user attacks with an electric charge. The user may use any remaining electricity to raise its Sp. Atk stat.", 10, Type.ELECTRIC, Category.SPECIAL);
			super.power = 50;
			super.accuracy = 90;
			super.effectChance = 70;
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
		}
	}

	private static class MirrorCoat extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MirrorCoat()
		{
			super(Namesies.MIRROR_COAT_ATTACK, "A retaliation move that counters any special attack, inflicting double the damage taken.", 20, Type.PSYCHIC, Category.SPECIAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.priority = -5;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int damageTaken = me.getAttributes().getDamageTaken();
			
			// Fails if no damage to reflect or if the opponent isn't using an attack of the proper category
			if (damageTaken == 0 || o.getMove() == null || o.getAttack().getCategory() != Category.SPECIAL || b.isFirstAttack())
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			o.reduceHealth(b, damageTaken*2);
		}
	}

	private static class Counter extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Counter()
		{
			super(Namesies.COUNTER_ATTACK, "A retaliation move that counters any physical attack, inflicting double the damage taken.", 20, Type.FIGHTING, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.priority = -5;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int damageTaken = me.getAttributes().getDamageTaken();
			
			// Fails if no damage to reflect or if the opponent isn't using an attack of the proper category
			if (damageTaken == 0 || o.getMove() == null || o.getAttack().getCategory() != Category.PHYSICAL || b.isFirstAttack())
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			o.reduceHealth(b, damageTaken*2);
		}
	}

	private static class Barrage extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Barrage()
		{
			super(Namesies.BARRAGE_ATTACK, "Round objects are hurled at the target to strike two to five times in a row.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 15;
			super.accuracy = 85;
			super.moveTypes.add(MoveType.BOMB_BALL);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int minHits = 2;
			int maxHits = 5;
			
			int hits = (int)(Math.random()*(maxHits - minHits + 1)) + minHits;
			
			if (maxHits == 5 && me.hasAbility(Namesies.SKILL_LINK_ABILITY))
			{
				hits = 5;
			}
			
			int hit = 1;
			for (; hit <= hits; hit++)
			{
				b.addMessage("Hit " + hit + "!");
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b))
				{
					break;
				}
			}
			
			// Print hits and gtfo
			b.addMessage("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!");
		}
	}

	private static class BulletSeed extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public BulletSeed()
		{
			super(Namesies.BULLET_SEED_ATTACK, "The user forcefully shoots seeds at the target. Two to five seeds are shot in rapid succession.", 30, Type.GRASS, Category.PHYSICAL);
			super.power = 25;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.BOMB_BALL);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int minHits = 2;
			int maxHits = 5;
			
			int hits = (int)(Math.random()*(maxHits - minHits + 1)) + minHits;
			
			if (maxHits == 5 && me.hasAbility(Namesies.SKILL_LINK_ABILITY))
			{
				hits = 5;
			}
			
			int hit = 1;
			for (; hit <= hits; hit++)
			{
				b.addMessage("Hit " + hit + "!");
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b))
				{
					break;
				}
			}
			
			// Print hits and gtfo
			b.addMessage("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!");
		}
	}

	private static class EggBomb extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public EggBomb()
		{
			super(Namesies.EGG_BOMB_ATTACK, "A large egg is hurled at the target with maximum force to inflict damage.", 10, Type.NORMAL, Category.PHYSICAL);
			super.power = 100;
			super.accuracy = 75;
			super.moveTypes.add(MoveType.BOMB_BALL);
		}
	}

	private static class WoodHammer extends Attack implements RecoilMove
	{
		private static final long serialVersionUID = 1L;

		public WoodHammer()
		{
			super(Namesies.WOOD_HAMMER_ATTACK, "The user slams its rugged body into the target to attack. The user also sustains serious damage.", 15, Type.GRASS, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyRecoil(Battle b, ActivePokemon user, Integer damage)
		{
			if (user.hasAbility(Namesies.ROCK_HEAD_ABILITY) || user.hasAbility(Namesies.MAGIC_GUARD_ABILITY))
			{
				return;
			}
			
			b.addMessage(user.getName() + " was hurt by recoil!");
			user.reduceHealth(b, (int)Math.ceil(damage/3.0));
		}
	}

	private static class BoneClub extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public BoneClub()
		{
			super(Namesies.BONE_CLUB_ATTACK, "The user clubs the target with a bone. It may also make the target flinch.", 20, Type.GROUND, Category.PHYSICAL);
			super.power = 65;
			super.accuracy = 85;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 10;
		}
	}

	private static class Bonemerang extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Bonemerang()
		{
			super(Namesies.BONEMERANG_ATTACK, "The user throws the bone it holds. The bone loops to hit the target twice, coming and going.", 10, Type.GROUND, Category.PHYSICAL);
			super.power = 50;
			super.accuracy = 90;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int minHits = 2;
			int maxHits = 2;
			
			int hits = (int)(Math.random()*(maxHits - minHits + 1)) + minHits;
			
			if (maxHits == 5 && me.hasAbility(Namesies.SKILL_LINK_ABILITY))
			{
				hits = 5;
			}
			
			int hit = 1;
			for (; hit <= hits; hit++)
			{
				b.addMessage("Hit " + hit + "!");
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b))
				{
					break;
				}
			}
			
			// Print hits and gtfo
			b.addMessage("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!");
		}
	}

	private static class BoneRush extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public BoneRush()
		{
			super(Namesies.BONE_RUSH_ATTACK, "The user strikes the target with a hard bone two to five times in a row.", 10, Type.GROUND, Category.PHYSICAL);
			super.power = 25;
			super.accuracy = 90;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int minHits = 2;
			int maxHits = 5;
			
			int hits = (int)(Math.random()*(maxHits - minHits + 1)) + minHits;
			
			if (maxHits == 5 && me.hasAbility(Namesies.SKILL_LINK_ABILITY))
			{
				hits = 5;
			}
			
			int hit = 1;
			for (; hit <= hits; hit++)
			{
				b.addMessage("Hit " + hit + "!");
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b))
				{
					break;
				}
			}
			
			// Print hits and gtfo
			b.addMessage("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!");
		}
	}

	private static class RollingKick extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public RollingKick()
		{
			super(Namesies.ROLLING_KICK_ATTACK, "The user lashes out with a quick, spinning kick. It may also make the target flinch.", 15, Type.FIGHTING, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 85;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 30;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class JumpKick extends Attack implements CrashDamageMove
	{
		private static final long serialVersionUID = 1L;

		public JumpKick()
		{
			super(Namesies.JUMP_KICK_ATTACK, "The user jumps up high, then strikes with a kick. If the kick misses, the user hurts itself.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.power = 100;
			super.accuracy = 95;
			super.moveTypes.add(MoveType.AIRBORNE);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void crash(Battle b, ActivePokemon user)
		{
			b.addMessage(user.getName() + " kept going and crashed!");
			user.reduceHealth(b, user.getMaxHP()/3);
		}
	}

	private static class BrickBreak extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public BrickBreak()
		{
			super(Namesies.BRICK_BREAK_ATTACK, "The user attacks with a swift chop. It can also break any barrier such as Light Screen and Reflect.", 15, Type.FIGHTING, Category.PHYSICAL);
			super.power = 75;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Global.invoke(b.getEffectsList(b.getOtherPokemon(user.user())), BarrierEffect.class, "breakBarrier", b, user);
		}
	}

	private static class HighJumpKick extends Attack implements CrashDamageMove
	{
		private static final long serialVersionUID = 1L;

		public HighJumpKick()
		{
			super(Namesies.HIGH_JUMP_KICK_ATTACK, "The target is attacked with a knee kick from a jump. If it misses, the user is hurt instead.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.power = 130;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.AIRBORNE);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void crash(Battle b, ActivePokemon user)
		{
			b.addMessage(user.getName() + " kept going and crashed!");
			user.reduceHealth(b, user.getMaxHP()/2);
		}
	}

	private static class BlazeKick extends Attack implements CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public BlazeKick()
		{
			super(Namesies.BLAZE_KICK_ATTACK, "The user launches a kick that lands a critical hit more easily. It may also leave the target with a burn.", 10, Type.FIRE, Category.PHYSICAL);
			super.power = 85;
			super.accuracy = 90;
			super.effectChance = 10;
			super.status = StatusCondition.BURNED;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}
	}

	private static class MegaKick extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MegaKick()
		{
			super(Namesies.MEGA_KICK_ATTACK, "The target is attacked by a kick launched with muscle-packed power.", 5, Type.NORMAL, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 75;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class CometPunch extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public CometPunch()
		{
			super(Namesies.COMET_PUNCH_ATTACK, "The target is hit with a flurry of punches that strike two to five times in a row.", 15, Type.NORMAL, Category.PHYSICAL);
			super.power = 18;
			super.accuracy = 85;
			super.moveTypes.add(MoveType.PUNCHING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int minHits = 2;
			int maxHits = 5;
			
			int hits = (int)(Math.random()*(maxHits - minHits + 1)) + minHits;
			
			if (maxHits == 5 && me.hasAbility(Namesies.SKILL_LINK_ABILITY))
			{
				hits = 5;
			}
			
			int hit = 1;
			for (; hit <= hits; hit++)
			{
				b.addMessage("Hit " + hit + "!");
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b))
				{
					break;
				}
			}
			
			// Print hits and gtfo
			b.addMessage("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!");
		}
	}

	private static class MachPunch extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MachPunch()
		{
			super(Namesies.MACH_PUNCH_ATTACK, "The user throws a punch at blinding speed. It is certain to strike first.", 30, Type.FIGHTING, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PUNCHING);
			super.priority = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class BulletPunch extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public BulletPunch()
		{
			super(Namesies.BULLET_PUNCH_ATTACK, "The user strikes the target with tough punches as fast as bullets. This move always goes first.", 30, Type.STEEL, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PUNCHING);
			super.priority = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class VacuumWave extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public VacuumWave()
		{
			super(Namesies.VACUUM_WAVE_ATTACK, "The user whirls its fists to send a wave of pure vacuum at the target. This move always goes first.", 30, Type.FIGHTING, Category.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.priority = 1;
		}
	}

	private static class ThunderPunch extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public ThunderPunch()
		{
			super(Namesies.THUNDER_PUNCH_ATTACK, "The target is punched with an electrified fist. It may also leave the target with paralysis.", 15, Type.ELECTRIC, Category.PHYSICAL);
			super.power = 75;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.PUNCHING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class IcePunch extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public IcePunch()
		{
			super(Namesies.ICE_PUNCH_ATTACK, "The target is punched with an icy fist. It may also leave the target frozen.", 15, Type.ICE, Category.PHYSICAL);
			super.power = 75;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.FROZEN;
			super.moveTypes.add(MoveType.PUNCHING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class FirePunch extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FirePunch()
		{
			super(Namesies.FIRE_PUNCH_ATTACK, "The target is punched with a fiery fist. It may also leave the target with a burn.", 15, Type.FIRE, Category.PHYSICAL);
			super.power = 75;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.BURNED;
			super.moveTypes.add(MoveType.PUNCHING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class SkyUppercut extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public SkyUppercut()
		{
			super(Namesies.SKY_UPPERCUT_ATTACK, "The user attacks the target with an uppercut thrown skyward with force.", 15, Type.FIGHTING, Category.PHYSICAL);
			super.power = 85;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.PUNCHING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			// Does not deal double damage when opponent is flying
			return super.power;
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			// Always hit when the opponent is flying
			return defending.isSemiInvulnerableFlying();
		}
	}

	private static class MegaPunch extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MegaPunch()
		{
			super(Namesies.MEGA_PUNCH_ATTACK, "The target is slugged by a punch thrown with muscle-packed power.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 85;
			super.moveTypes.add(MoveType.PUNCHING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class FocusPunch extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FocusPunch()
		{
			super(Namesies.FOCUS_PUNCH_ATTACK, "The user focuses its mind before launching a punch. It will fail if the user is hit before it is used.", 20, Type.FIGHTING, Category.PHYSICAL);
			super.power = 150;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.FOCUSING_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.PUNCHING);
			super.selfTarget = true;
			super.priority = -3;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void startTurn(Battle b, ActivePokemon me)
		{
			super.applyEffects(b, me, me);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (me.hasEffect(Namesies.FOCUSING_EFFECT))
			{
				super.applyDamage(me, o, b);
			}
		}
	}

	private static class MeFirst extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MeFirst()
		{
			super(Namesies.ME_FIRST_ATTACK, "The user tries to cut ahead of the target to steal and use the target's intended move with greater power.", 20, Type.NORMAL, Category.STATUS);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.METRONOMELESS);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			// Fails if it is the second turn or the opponent is using a status move
			if (!b.isFirstAttack() || o.getMove() == null || o.getAttack().getCategory() == Category.STATUS)
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			me.addEffect(PokemonEffect.getEffect(Namesies.FIDDY_PERCENT_STRONGER_EFFECT));
			me.callNewMove(b, o, new Move(o.getAttack()));
			me.getAttributes().removeEffect(Namesies.FIDDY_PERCENT_STRONGER_EFFECT);
		}
	}

	private static class Refresh extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Refresh()
		{
			super(Namesies.REFRESH_ATTACK, "The user rests to cure itself of a poisoning, burn, or paralysis.", 20, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (!me.hasStatus())
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			super.apply(me, o, b);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Status.removeStatus(b, user, CastSource.ATTACK);
		}
	}

	private static class PowerWhip extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PowerWhip()
		{
			super(Namesies.POWER_WHIP_ATTACK, "The user violently whirls its vines or tentacles to harshly lash the target.", 10, Type.GRASS, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 85;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Smog extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Smog()
		{
			super(Namesies.SMOG_ATTACK, "The target is attacked with a discharge of filthy gases. It may also poison the target.", 20, Type.POISON, Category.SPECIAL);
			super.power = 30;
			super.accuracy = 70;
			super.effectChance = 40;
			super.status = StatusCondition.POISONED;
		}
	}

	private static class ClearSmog extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public ClearSmog()
		{
			super(Namesies.CLEAR_SMOG_ATTACK, "The user attacks by throwing a clump of special mud. All status changes are returned to normal.", 15, Type.POISON, Category.SPECIAL);
			super.power = 50;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			user.getAttributes().resetStages();
			victim.getAttributes().resetStages();
			b.addMessage("All stat changes were eliminated!");
		}
	}

	private static class HammerArm extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public HammerArm()
		{
			super(Namesies.HAMMER_ARM_ATTACK, "The user swings and hits with its strong and heavy fist. It lowers the user's Speed, however.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.power = 100;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.PUNCHING);
			super.selfTarget = true;
			super.statChanges[Stat.SPEED.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class SoftBoiled extends Attack implements SelfHealingMove
	{
		private static final long serialVersionUID = 1L;

		public SoftBoiled()
		{
			super(Namesies.SOFT_BOILED_ATTACK, "The user restores its own HP by up to half of its maximum HP. May also be used in the field to heal HP.", 10, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect(Namesies.HEAL_BLOCK_EFFECT))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			victim.healHealthFraction(1/2.0);
			
			b.addMessage(victim.getName() + "'s health was restored!", victim);
		}
	}

	private static class AncientPower extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public AncientPower()
		{
			super(Namesies.ANCIENT_POWER_ATTACK, "The user attacks with a prehistoric power. It may also raise all the user's stats at once.", 5, Type.ROCK, Category.SPECIAL);
			super.power = 60;
			super.accuracy = 100;
			super.effectChance = 10;
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
			super.statChanges[Stat.SPEED.index()] = 1;
			super.statChanges[Stat.ACCURACY.index()] = 1;
			super.statChanges[Stat.EVASION.index()] = 1;
		}
	}

	private static class Tickle extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Tickle()
		{
			super(Namesies.TICKLE_ATTACK, "The user tickles the target into laughing, reducing its Attack and Defense stats.", 20, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.ATTACK.index()] = -1;
			super.statChanges[Stat.DEFENSE.index()] = -1;
		}
	}

	private static class DizzyPunch extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DizzyPunch()
		{
			super(Namesies.DIZZY_PUNCH_ATTACK, "The target is hit with rhythmically launched punches that may also leave it confused.", 10, Type.NORMAL, Category.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.CONFUSION_EFFECT, EffectType.POKEMON));
			super.effectChance = 20;
			super.moveTypes.add(MoveType.PUNCHING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Outrage extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Outrage()
		{
			super(Namesies.OUTRAGE_ATTACK, "The user rampages and attacks for two to three turns. It then becomes confused, however.", 10, Type.DRAGON, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.SELF_CONFUSION_EFFECT, EffectType.POKEMON));
			super.selfTarget = true;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class DragonDance extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DragonDance()
		{
			super(Namesies.DRAGON_DANCE_ATTACK, "The user vigorously performs a mystic, powerful dance that boosts its Attack and Speed stats.", 20, Type.DRAGON, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.SPEED.index()] = 1;
		}
	}

	private static class DragonPulse extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DragonPulse()
		{
			super(Namesies.DRAGON_PULSE_ATTACK, "The target is attacked with a shock wave generated by the user's gaping mouth.", 10, Type.DRAGON, Category.SPECIAL);
			super.power = 85;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.AURA_PULSE);
		}
	}

	private static class DracoMeteor extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DracoMeteor()
		{
			super(Namesies.DRACO_METEOR_ATTACK, "Comets are summoned down from the sky onto the target. The attack's recoil harshly reduces the user's Sp. Atk stat.", 5, Type.DRAGON, Category.SPECIAL);
			super.power = 130;
			super.accuracy = 90;
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = -2;
		}
	}

	private static class Waterfall extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Waterfall()
		{
			super(Namesies.WATERFALL_ATTACK, "The user charges at the target and may make it flinch. It can also be used to climb a waterfall.", 15, Type.WATER, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 20;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class ReflectType extends Attack implements ChangeTypeMove
	{
		private static final long serialVersionUID = 1L;

		public ReflectType()
		{
			super(Namesies.REFLECT_TYPE_ATTACK, "The user reflects the target's type, making it the same type as the target.", 15, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.CHANGE_TYPE_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return b.getOtherPokemon(caster.user()).getType(b).clone();
		}
	}

	private static class MagicalLeaf extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MagicalLeaf()
		{
			super(Namesies.MAGICAL_LEAF_ATTACK, "The user scatters curious leaves that chase the target. This attack will not miss.", 20, Type.GRASS, Category.SPECIAL);
			super.power = 60;
		}
	}

	private static class PowerSwap extends Attack 
	{
		private static final long serialVersionUID = 1L;
		private static Stat[] swapStats = { Stat.ATTACK, Stat.SP_ATTACK };

		public PowerSwap()
		{
			super(Namesies.POWER_SWAP_ATTACK, "The user employs its psychic power to switch changes to its Attack and Sp. Atk with the target.", 10, Type.PSYCHIC, Category.STATUS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			for (Stat s : swapStats)
			{
				int statIndex = s.index();
				
				int userStat = user.getAttributes().getStage(statIndex);
				int victimStat = victim.getAttributes().getStage(statIndex);
				
				user.getAttributes().setStage(statIndex, victimStat);
				victim.getAttributes().setStage(statIndex, userStat);
			}
			
			b.addMessage(user.getName() + " swapped its stats with " + victim.getName() + "!");
		}
	}

	private static class GuardSwap extends Attack 
	{
		private static final long serialVersionUID = 1L;
		private static Stat[] swapStats = { Stat.DEFENSE, Stat.SP_DEFENSE };

		public GuardSwap()
		{
			super(Namesies.GUARD_SWAP_ATTACK, "The user employs its psychic power to switch changes to its Defense and Sp. Def with the target.", 10, Type.PSYCHIC, Category.STATUS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			for (Stat s : swapStats)
			{
				int statIndex = s.index();
				
				int userStat = user.getAttributes().getStage(statIndex);
				int victimStat = victim.getAttributes().getStage(statIndex);
				
				user.getAttributes().setStage(statIndex, victimStat);
				victim.getAttributes().setStage(statIndex, userStat);
			}
			
			b.addMessage(user.getName() + " swapped its stats with " + victim.getName() + "!");
		}
	}

	private static class Copycat extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Copycat()
		{
			super(Namesies.COPYCAT_ATTACK, "The user mimics the move used immediately before it. The move fails if no other move has been used yet.", 20, Type.NORMAL, Category.STATUS);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.ENCORELESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.MIRRORLESS);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			Move mirror = o.getAttributes().getLastMoveUsed();
			if (mirror == null || mirror.getAttack().isMoveType(MoveType.MIRRORLESS))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			me.callNewMove(b, o, new Move(mirror.getAttack()));
		}
	}

	private static class Transform extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Transform()
		{
			super(Namesies.TRANSFORM_ATTACK, "The user transforms into a copy of the target right down to having the same move set.", 10, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.TRANSFORMED_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.ENCORELESS);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.selfTarget = true;
		}
	}

	private static class Substitute extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Substitute()
		{
			super(Namesies.SUBSTITUTE_ATTACK, "The user makes a copy of itself using some of its HP. The copy serves as the user's decoy.", 10, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.SUBSTITUTE_EFFECT, EffectType.POKEMON));
			super.selfTarget = true;
		}
	}

	private static class RazorWind extends Attack implements MultiTurnMove, CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public RazorWind()
		{
			super(Namesies.RAZOR_WIND_ATTACK, "A two-turn attack. Blades of wind hit opposing Pok\u00e9mon on the second turn. Critical hits land more easily.", 10, Type.NORMAL, Category.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
		}

		public boolean chargesFirst()
		{
			return true;
		}

		public boolean semiInvulnerability()
		{
			return false;
		}

		public void charge(ActivePokemon user, Battle b)
		{
			b.addMessage(getChargeMessage(user));
		}

		public String getChargeMessage(ActivePokemon user)
		{
			return user.getName() + " whipped up a whirlwind!";
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}
	}

	private static class LovelyKiss extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public LovelyKiss()
		{
			super(Namesies.LOVELY_KISS_ATTACK, "With a scary face, the user tries to force a kiss on the target. If it succeeds, the target falls asleep.", 10, Type.NORMAL, Category.STATUS);
			super.accuracy = 75;
			super.status = StatusCondition.ASLEEP;
		}
	}

	private static class PowderSnow extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PowderSnow()
		{
			super(Namesies.POWDER_SNOW_ATTACK, "The user attacks with a chilling gust of powdery snow. It may also freeze the targets.", 25, Type.ICE, Category.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.FROZEN;
		}
	}

	private static class HeartStamp extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public HeartStamp()
		{
			super(Namesies.HEART_STAMP_ATTACK, "The user unleashes a vicious blow after its cute act makes the target less wary. It may also make the target flinch.", 25, Type.PSYCHIC, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 30;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class FakeTears extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FakeTears()
		{
			super(Namesies.FAKE_TEARS_ATTACK, "The user feigns crying to fluster the target, harshly lowering its Sp. Def stat.", 20, Type.DARK, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.SP_DEFENSE.index()] = -2;
		}
	}

	private static class Avalanche extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Avalanche()
		{
			super(Namesies.AVALANCHE_ATTACK, "An attack move that inflicts double the damage if the user has been hurt by the target in the same turn.", 10, Type.ICE, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.priority = -4;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(me.getAttributes().hasTakenDamage() ? 2 : 1);
		}
	}

	private static class Blizzard extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public Blizzard()
		{
			super(Namesies.BLIZZARD_ATTACK, "A howling blizzard is summoned to strike the opposing team. It may also freeze them solid.", 5, Type.ICE, Category.SPECIAL);
			super.power = 110;
			super.accuracy = 70;
			super.effectChance = 10;
			super.status = StatusCondition.FROZEN;
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			// Always hits when it's hailing unless the opponent is hiding (I think -- the hiding part is not specified on Bulbapedia)
			return b.getWeather().namesies() == Namesies.HAILING_EFFECT && !defending.isSemiInvulnerable();
		}
	}

	private static class ShockWave extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public ShockWave()
		{
			super(Namesies.SHOCK_WAVE_ATTACK, "The user strikes the target with a quick jolt of electricity. This attack cannot be evaded.", 20, Type.ELECTRIC, Category.SPECIAL);
			super.power = 60;
		}
	}

	private static class LavaPlume extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public LavaPlume()
		{
			super(Namesies.LAVA_PLUME_ATTACK, "An inferno of scarlet flames torches everything around the user. It may leave targets with a burn.", 15, Type.FIRE, Category.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.BURNED;
		}
	}

	private static class WorkUp extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public WorkUp()
		{
			super(Namesies.WORK_UP_ATTACK, "The user is roused, and its Attack and Sp. Atk stats increase.", 30, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
		}
	}

	private static class GigaImpact extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;

		public GigaImpact()
		{
			super(Namesies.GIGA_IMPACT_ATTACK, "The user charges at the target using every bit of its power. The user must rest on the next turn.", 5, Type.NORMAL, Category.PHYSICAL);
			super.power = 150;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public boolean chargesFirst()
		{
			return false;
		}

		public boolean semiInvulnerability()
		{
			return false;
		}

		public void charge(ActivePokemon user, Battle b)
		{
			b.addMessage(getChargeMessage(user));
		}

		public String getChargeMessage(ActivePokemon user)
		{
			return user.getName() + " must recharge!";
		}
	}

	private static class Splash extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Splash()
		{
			super(Namesies.SPLASH_ATTACK, "The user just flops and splashes around to no effect at all...", 40, Type.NORMAL, Category.STATUS);
			super.moveTypes.add(MoveType.AIRBORNE);
			super.selfTarget = true;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			b.addMessage("But nothing happened...");
		}
	}

	private static class Mist extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Mist()
		{
			super(Namesies.MIST_ATTACK, "The user cloaks its body with a white mist that prevents any of its stats from being cut for five turns.", 30, Type.ICE, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.MIST_EFFECT, EffectType.POKEMON));
			super.selfTarget = true;
		}
	}

	private static class LastResort extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public LastResort()
		{
			super(Namesies.LAST_RESORT_ATTACK, "This move can be used only after the user has used all the other moves it knows in the battle.", 5, Type.NORMAL, Category.PHYSICAL);
			super.power = 140;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			for (Move m : me.getMoves(b))
			{
				if (m.getAttack().namesies() == this.namesies)
				{
					continue;
				}
				
				if (!m.used())
				{
					b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
					return;
				}
			}
			
			super.apply(me, o, b);
		}
	}

	private static class TrumpCard extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public TrumpCard()
		{
			super(Namesies.TRUMP_CARD_ATTACK, "The fewer PP this move has, the greater its attack power.", 5, Type.NORMAL, Category.SPECIAL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			int pp = me.getMove().getPP();
			
			switch (pp)
			{
				case 1:
				return 190;
				case 2:
				return 75;
				case 3:
				return 60;
				case 4:
				return 50;
				default:
				return 40;
			}
		}
	}

	private static class MuddyWater extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MuddyWater()
		{
			super(Namesies.MUDDY_WATER_ATTACK, "The user attacks by shooting muddy water at the opposing team. It may also lower the targets' accuracy.", 10, Type.WATER, Category.SPECIAL);
			super.power = 90;
			super.accuracy = 85;
			super.effectChance = 30;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	private static class Conversion extends Attack implements ChangeTypeMove
	{
		private static final long serialVersionUID = 1L;

		public Conversion()
		{
			super(Namesies.CONVERSION_ATTACK, "The user changes its type to become the same type as one of its moves.", 30, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.CHANGE_TYPE_EFFECT, EffectType.POKEMON));
			super.selfTarget = true;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			for (Move m : me.getMoves(b))
			{
				if (!me.isType(b, m.getAttack().getActualType()))
				{
					super.apply(me, o, b);
					return;
				}
			}
			
			b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			List<Type> types = new ArrayList<>();
			for (Move m : victim.getMoves(b))
			{
				Type t = m.getAttack().getActualType();
				if (!victim.isType(b, t))
				{
					types.add(t);
				}
			}
			
			return new Type[] {types.get((int)(Math.random()*types.size())), Type.NONE};
		}
	}

	private static class Conversion2 extends Attack implements ChangeTypeMove
	{
		private static final long serialVersionUID = 1L;
		private List<Type> getResistances(ActivePokemon victim, Type attacking, Battle b)
		{
			List<Type> types = new ArrayList<>();
			for (Type t : Type.values())
			{
				if (Type.getBasicAdvantage(attacking, t) < 1 && !victim.isType(b, t))
				{
					types.add(t);
				}
			}
			
			return types;
		}

		public Conversion2()
		{
			super(Namesies.CONVERSION2_ATTACK, "The user changes its type to make itself resistant to the type of the attack the opponent used last.", 30, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.CHANGE_TYPE_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			Move m = o.getAttributes().getLastMoveUsed();
			if (m == null || getResistances(me, m.getType(), b).size() == 0)
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			super.apply(me, o, b);
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			ActivePokemon other = b.getOtherPokemon(victim.user());
			List<Type> types = getResistances(victim, other.getAttributes().getLastMoveUsed().getType(), b);
			return new Type[] {types.get((int)(Math.random()*types.size())), Type.NONE};
		}
	}

	private static class Sharpen extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Sharpen()
		{
			super(Namesies.SHARPEN_ATTACK, "The user reduces its polygon count to make itself more jagged, raising the Attack stat.", 30, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
		}
	}

	private static class MagicCoat extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MagicCoat()
		{
			super(Namesies.MAGIC_COAT_ATTACK, "A barrier reflects back to the target moves like Leech Seed and moves that damage status.", 15, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.MAGIC_COAT_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
			super.priority = 4;
		}
	}

	private static class SkyDrop extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SkyDrop()
		{
			super(Namesies.SKY_DROP_ATTACK, "The user takes the target into the sky, then slams it into the ground.", 10, Type.FLYING, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.AIRBORNE);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (o.getWeight(b) > 440)
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			super.apply(me, o, b);
		}
	}

	private static class IronHead extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public IronHead()
		{
			super(Namesies.IRON_HEAD_ATTACK, "The user slams the target with its steel-hard head. It may also make the target flinch.", 15, Type.STEEL, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 30;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class RockSlide extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public RockSlide()
		{
			super(Namesies.ROCK_SLIDE_ATTACK, "Large boulders are hurled at the opposing team to inflict damage. It may also make the targets flinch.", 10, Type.ROCK, Category.PHYSICAL);
			super.power = 75;
			super.accuracy = 90;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 30;
		}
	}

	private static class Snore extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Snore()
		{
			super(Namesies.SNORE_ATTACK, "An attack that can be used only if the user is asleep. The harsh noise may also make the target flinch.", 15, Type.NORMAL, Category.SPECIAL);
			super.power = 50;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 30;
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.ASLEEP_USER);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (!me.hasStatus(StatusCondition.ASLEEP))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			super.apply(me, o, b);
		}
	}

	private static class SleepTalk extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SleepTalk()
		{
			super(Namesies.SLEEP_TALK_ATTACK, "While it is asleep, the user randomly uses one of the moves it knows.", 10, Type.NORMAL, Category.STATUS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.ASLEEP_USER);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (!me.hasStatus(StatusCondition.ASLEEP))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			List<Move> moves = new ArrayList<>();
			for (Move m : me.getMoves(b))
			{
				if (!m.getAttack().isMoveType(MoveType.SLEEP_TALK_FAIL))
				{
					moves.add(m);
				}
			}
			
			if (moves.size() == 0)
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			me.callNewMove(b, o, moves.get((int)(Math.random()*moves.size())));
		}
	}

	private static class Block extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Block()
		{
			super(Namesies.BLOCK_ATTACK, "The user blocks the target's way with arms spread wide to prevent escape.", 5, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.TRAPPED_EFFECT, EffectType.POKEMON));
		}
	}

	private static class SkyAttack extends Attack implements MultiTurnMove, CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public SkyAttack()
		{
			super(Namesies.SKY_ATTACK_ATTACK, "A second-turn attack move where critical hits land more easily. It may also make the target flinch.", 5, Type.FLYING, Category.PHYSICAL);
			super.power = 140;
			super.accuracy = 90;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 30;
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
		}

		public boolean chargesFirst()
		{
			return true;
		}

		public boolean semiInvulnerability()
		{
			return false;
		}

		public void charge(ActivePokemon user, Battle b)
		{
			b.addMessage(getChargeMessage(user));
		}

		public String getChargeMessage(ActivePokemon user)
		{
			return user.getName() + " started glowing!";
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}
	}

	private static class DragonRush extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public DragonRush()
		{
			super(Namesies.DRAGON_RUSH_ATTACK, "The user tackles the target while exhibiting overwhelming menace. It may also make the target flinch.", 10, Type.DRAGON, Category.PHYSICAL);
			super.power = 100;
			super.accuracy = 75;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 20;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(o.hasEffect(Namesies.USED_MINIMIZE_EFFECT) ? 2 : 1);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return !defending.isSemiInvulnerable() && defending.hasEffect(Namesies.USED_MINIMIZE_EFFECT);
		}
	}

	private static class AuraSphere extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public AuraSphere()
		{
			super(Namesies.AURA_SPHERE_ATTACK, "The user looses a blast of aura power from deep within its body at the target. This move is certain to hit.", 20, Type.FIGHTING, Category.SPECIAL);
			super.power = 80;
			super.moveTypes.add(MoveType.BOMB_BALL);
			super.moveTypes.add(MoveType.AURA_PULSE);
		}
	}

	private static class Psystrike extends Attack implements StatSwitchingEffect
	{
		private static final long serialVersionUID = 1L;

		public Psystrike()
		{
			super(Namesies.PSYSTRIKE_ATTACK, "The user materializes an odd psychic wave to attack the target. This attack does physical damage.", 10, Type.PSYCHIC, Category.SPECIAL);
			super.power = 100;
			super.accuracy = 100;
		}

		public Stat switchStat(Stat s)
		{
			return s == Stat.SP_DEFENSE ? Stat.DEFENSE : s;
		}
	}

	private static class Eruption extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Eruption()
		{
			super(Namesies.ERUPTION_ATTACK, "The user attacks the opposing team with explosive fury. The lower the user's HP, the less powerful this attack becomes.", 5, Type.FIRE, Category.SPECIAL);
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return (int)Math.min(1, (150*me.getHPRatio()));
		}
	}

	private static class Charm extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Charm()
		{
			super(Namesies.CHARM_ATTACK, "The user gazes at the target rather charmingly, making it less wary. The target's Attack is harshly lowered.", 20, Type.FAIRY, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.ATTACK.index()] = -2;
		}
	}

	private static class EchoedVoice extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public EchoedVoice()
		{
			super(Namesies.ECHOED_VOICE_ATTACK, "The user attacks the target with an echoing voice. If this move is used every turn, it does greater damage.", 15, Type.NORMAL, Category.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SOUND_BASED);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*Math.min(5, me.getAttributes().getCount());
		}
	}

	private static class PsychoShift extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PsychoShift()
		{
			super(Namesies.PSYCHO_SHIFT_ATTACK, "Using its psychic power of suggestion, the user transfers its status problems to the target.", 10, Type.PSYCHIC, Category.STATUS);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			String message = user.getName() + " transferred its status condition to " + victim.getName() + "!";
			if (!user.hasStatus() || !Status.giveStatus(b, user, victim, user.getStatus().getType(), message))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			user.removeStatus();
			b.addMessage("", user);
		}
	}

	private static class ShadowSneak extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public ShadowSneak()
		{
			super(Namesies.SHADOW_SNEAK_ATTACK, "The user extends its shadow and attacks the target from behind. This move always goes first.", 30, Type.GHOST, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.priority = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class SpiderWeb extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SpiderWeb()
		{
			super(Namesies.SPIDER_WEB_ATTACK, "The user ensnares the target with thin, gooey silk so it can't flee from battle.", 10, Type.BUG, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.TRAPPED_EFFECT, EffectType.POKEMON));
		}
	}

	private static class SweetKiss extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SweetKiss()
		{
			super(Namesies.SWEET_KISS_ATTACK, "The user kisses the target with a sweet, angelic cuteness that causes confusion.", 10, Type.FAIRY, Category.STATUS);
			super.accuracy = 75;
			super.effects.add(Effect.getEffect(Namesies.CONFUSION_EFFECT, EffectType.POKEMON));
		}
	}

	private static class OminousWind extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public OminousWind()
		{
			super(Namesies.OMINOUS_WIND_ATTACK, "The user blasts the target with a gust of repulsive wind. It may also raise all the user's stats at once.", 5, Type.GHOST, Category.SPECIAL);
			super.power = 60;
			super.accuracy = 100;
			super.effectChance = 10;
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
			super.statChanges[Stat.SPEED.index()] = 1;
			super.statChanges[Stat.ACCURACY.index()] = 1;
			super.statChanges[Stat.EVASION.index()] = 1;
		}
	}

	private static class CottonSpore extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public CottonSpore()
		{
			super(Namesies.COTTON_SPORE_ATTACK, "The user releases cotton-like spores that cling to the target, harshly reducing its Speed stat.", 40, Type.GRASS, Category.STATUS);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.POWDER);
			super.statChanges[Stat.SPEED.index()] = -2;
		}
	}

	private static class CottonGuard extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public CottonGuard()
		{
			super(Namesies.COTTON_GUARD_ATTACK, "The user protects itself by wrapping its body in soft cotton, drastically raising the user's Defense stat.", 10, Type.GRASS, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 3;
		}
	}

	private static class GrassWhistle extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public GrassWhistle()
		{
			super(Namesies.GRASS_WHISTLE_ATTACK, "The user plays a pleasant melody that lulls the target into a deep sleep.", 15, Type.GRASS, Category.STATUS);
			super.accuracy = 55;
			super.status = StatusCondition.ASLEEP;
			super.moveTypes.add(MoveType.SOUND_BASED);
		}
	}

	private static class Torment extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Torment()
		{
			super(Namesies.TORMENT_ATTACK, "The user torments and enrages the target, making it incapable of using the same move twice in a row.", 15, Type.DARK, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.TORMENT_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
		}
	}

	private static class HiddenPower extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public HiddenPower()
		{
			super(Namesies.HIDDEN_POWER_ATTACK, "A unique attack that varies in type and intensity depending on the Pok\u00e9mon using it.", 15, Type.NORMAL, Category.SPECIAL);
			super.power = 60;
			super.accuracy = 100;
		}

		public Type setType(Battle b, ActivePokemon user)
		{
			return user.getHiddenPowerType();
		}
	}

	private static class Psywave extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Psywave()
		{
			super(Namesies.PSYWAVE_ATTACK, "The target is attacked with an odd psychic wave. The attack varies in intensity.", 15, Type.PSYCHIC, Category.SPECIAL);
			super.accuracy = 100;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			o.reduceHealth(b, (int)Math.max(1, ((int)(Math.random()*11) + 5)*me.getLevel()/10.0));
		}
	}

	private static class PainSplit extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PainSplit()
		{
			super(Namesies.PAIN_SPLIT_ATTACK, "The user adds its HP to the target's HP, then equally shares the combined HP with the target.", 20, Type.NORMAL, Category.STATUS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			int share = (user.getHP() + victim.getHP())/2;
			user.setHP(share);
			victim.setHP(share);
			
			b.addMessage(user.getName() + " and " + victim.getName() + " split their pain!", user);
			b.addMessage("", victim);
		}
	}

	private static class Bide extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Bide()
		{
			super(Namesies.BIDE_ATTACK, "The user endures attacks for two turns, then strikes back to cause double the damage taken.", 10, Type.NORMAL, Category.PHYSICAL);
			super.effects.add(Effect.getEffect(Namesies.BIDE_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.selfTarget = true;
			super.priority = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			super.applyEffects(b, me, me);
		}
	}

	private static class Autotomize extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Autotomize()
		{
			super(Namesies.AUTOTOMIZE_ATTACK, "The user sheds part of its body to make itself lighter and sharply raise its Speed stat.", 15, Type.STEEL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.HALF_WEIGHT_EFFECT, EffectType.POKEMON));
			super.selfTarget = true;
			super.statChanges[Stat.SPEED.index()] = 2;
		}
	}

	private static class StruggleBug extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public StruggleBug()
		{
			super(Namesies.STRUGGLE_BUG_ATTACK, "While resisting, the user attacks the opposing Pok\u00e9mon. The targets' Sp. Atk stat is reduced.", 20, Type.BUG, Category.SPECIAL);
			super.power = 50;
			super.accuracy = 100;
			super.statChanges[Stat.SP_ATTACK.index()] = -1;
		}
	}

	private static class PowerTrick extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PowerTrick()
		{
			super(Namesies.POWER_TRICK_ATTACK, "The user employs its psychic power to switch its Attack with its Defense stat.", 10, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.POWER_TRICK_EFFECT, EffectType.POKEMON));
			super.selfTarget = true;
		}
	}

	private static class PowerSplit extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PowerSplit()
		{
			super(Namesies.POWER_SPLIT_ATTACK, "The user employs its psychic power to average its Attack and Sp. Atk stats with those of the target's.", 10, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.POWER_SPLIT_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}
	}

	private static class GuardSplit extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public GuardSplit()
		{
			super(Namesies.GUARD_SPLIT_ATTACK, "The user employs its psychic power to average its Defense and Sp. Def stats with those of its target's.", 10, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.GUARD_SPLIT_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}
	}

	private static class HoneClaws extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public HoneClaws()
		{
			super(Namesies.HONE_CLAWS_ATTACK, "The user sharpens its claws to boost its Attack stat and accuracy.", 15, Type.DARK, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.ACCURACY.index()] = 1;
		}
	}

	private static class BeatUp extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public BeatUp()
		{
			super(Namesies.BEAT_UP_ATTACK, "The user gets all party Pok\u00e9mon to attack the target. The more party Pok\u00e9mon, the greater the number of attacks.", 10, Type.DARK, Category.PHYSICAL);
			super.power = 10;
			super.accuracy = 100;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			for (ActivePokemon p : b.getTrainer(me.user()).getTeam())
			{
				// Only healthy Pokemon get to attack
				if (!p.canFight() || p.hasStatus())
				{
					continue;
				}
				
				// Stop killing the dead
				if (o.isFainted(b))
				{
					break;
				}
				
				Move temp = p.getMove();
				p.setMove(new Move(Attack.getAttack(this.namesies)));
				b.addMessage(p.getName() + "'s attack!");
				super.applyDamage(p, o, b);
				p.setMove(temp);
			}
		}
	}

	private static class Octazooka extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Octazooka()
		{
			super(Namesies.OCTAZOOKA_ATTACK, "The user attacks by spraying ink in the target's face or eyes. It may also lower the target's accuracy.", 10, Type.WATER, Category.SPECIAL);
			super.power = 65;
			super.accuracy = 85;
			super.effectChance = 50;
			super.moveTypes.add(MoveType.BOMB_BALL);
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	private static class Present extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Present()
		{
			super(Namesies.PRESENT_ATTACK, "The user attacks by giving the target a gift with a hidden trap. It restores HP sometimes, however.", 15, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 90;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			double random = Math.random()*80;
			if (random < 40) return 40;
			if (random < 70) return 80;
			return 120;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (!effective(b, me, o))
			{
				return;
			}
			
			if (Math.random()*100 < 80)
			{
				super.applyDamage(me, o, b);
				return;
			}
			
			if (o.fullHealth() || o.hasEffect(Namesies.HEAL_BLOCK_EFFECT))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			o.healHealthFraction(1/4.0);
			b.addMessage(o.getName() + "'s health was restored!", o);
		}
	}

	private static class SteelWing extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SteelWing()
		{
			super(Namesies.STEEL_WING_ATTACK, "The target is hit with wings of steel. It may also raise the user's Defense stat.", 25, Type.STEEL, Category.PHYSICAL);
			super.power = 70;
			super.accuracy = 90;
			super.effectChance = 10;
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Sketch extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Sketch()
		{
			super(Namesies.SKETCH_ATTACK, "It enables the user to permanently learn the move last used by the target. Once used, Sketch disappears.", 1, Type.NORMAL, Category.STATUS);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.ENCORELESS);
			super.moveTypes.add(MoveType.MIMICLESS);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Move copy = victim.getAttributes().getLastMoveUsed();
			if (copy == null || copy.getAttack().namesies() == Namesies.STRUGGLE_ATTACK || user.hasEffect(Namesies.TRANSFORMED_EFFECT))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			List<Move> moves = user.getMoves(b);
			for (int i = 0; i < moves.size(); i++)
			{
				if (moves.get(i).getAttack().namesies() == this.namesies)
				{
					moves.add(i, new Move(copy.getAttack()));
					moves.remove(i + 1);
					b.addMessage(user.getName() + " learned " + moves.get(i).getAttack().getName() + "!");
					break;
				}
			}
		}
	}

	private static class TripleKick extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public TripleKick()
		{
			super(Namesies.TRIPLE_KICK_ATTACK, "A consecutive three-kick attack that becomes more powerful with each successive hit.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.power = 20;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int minHits = 3;
			int maxHits = 3;
			
			int hits = (int)(Math.random()*(maxHits - minHits + 1)) + minHits;
			
			if (maxHits == 5 && me.hasAbility(Namesies.SKILL_LINK_ABILITY))
			{
				hits = 5;
			}
			
			int hit = 1;
			for (; hit <= hits; hit++)
			{
				b.addMessage("Hit " + hit + "!");
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b))
				{
					break;
				}
			}
			
			// Print hits and gtfo
			b.addMessage("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!");
		}
	}

	private static class MilkDrink extends Attack implements SelfHealingMove
	{
		private static final long serialVersionUID = 1L;

		public MilkDrink()
		{
			super(Namesies.MILK_DRINK_ATTACK, "The user restores its own HP by up to half of its maximum HP. May also be used in the field to heal HP.", 10, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect(Namesies.HEAL_BLOCK_EFFECT))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			victim.healHealthFraction(1/2.0);
			
			b.addMessage(victim.getName() + "'s health was restored!", victim);
		}
	}

	private static class HealBell extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public HealBell()
		{
			super(Namesies.HEAL_BELL_ATTACK, "The user makes a soothing bell chime to heal the status problems of all the party Pok\u00e9mon.", 5, Type.NORMAL, Category.STATUS);
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.selfTarget = true;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			for (ActivePokemon p : b.getTrainer(user.user()).getTeam())
			{
				if (!p.hasStatus(StatusCondition.FAINTED))
				{
					p.removeStatus();
				}
			}
			
			b.addMessage("All status problems were cured!");
		}
	}

	private static class WeatherBall extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public WeatherBall()
		{
			super(Namesies.WEATHER_BALL_ATTACK, "An attack move that varies in power and type depending on the weather.", 10, Type.NORMAL, Category.SPECIAL);
			super.power = 50;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.BOMB_BALL);
		}

		public Type setType(Battle b, ActivePokemon user)
		{
			return b.getWeather().getElement();
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(b.getWeather().namesies() == Namesies.CLEAR_SKIES_EFFECT ? 1 : 2);
		}
	}

	private static class Aeroblast extends Attack implements CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public Aeroblast()
		{
			super(Namesies.AEROBLAST_ATTACK, "A vortex of air is shot at the target to inflict damage. Critical hits land more easily.", 5, Type.FLYING, Category.SPECIAL);
			super.power = 100;
			super.accuracy = 95;
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}
	}

	private static class SacredFire extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SacredFire()
		{
			super(Namesies.SACRED_FIRE_ATTACK, "The target is razed with a mystical fire of great intensity. It may also leave the target with a burn.", 5, Type.FIRE, Category.PHYSICAL);
			super.power = 100;
			super.accuracy = 95;
			super.effectChance = 50;
			super.status = StatusCondition.BURNED;
			super.moveTypes.add(MoveType.DEFROST);
		}
	}

	private static class HealBlock extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public HealBlock()
		{
			super(Namesies.HEAL_BLOCK_ATTACK, "For five turns, the user prevents the opposing team from using any moves, Abilities, or held items that recover HP.", 15, Type.PSYCHIC, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.HEAL_BLOCK_EFFECT, EffectType.POKEMON));
		}
	}

	private static class EnergyBall extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public EnergyBall()
		{
			super(Namesies.ENERGY_BALL_ATTACK, "The user draws power from nature and fires it at the target. It may also lower the target's Sp. Def.", 10, Type.GRASS, Category.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
			super.effectChance = 10;
			super.moveTypes.add(MoveType.BOMB_BALL);
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
		}
	}

	private static class BulkUp extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public BulkUp()
		{
			super(Namesies.BULK_UP_ATTACK, "The user tenses its muscles to bulk up its body, boosting both its Attack and Defense stats.", 20, Type.FIGHTING, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.DEFENSE.index()] = 1;
		}
	}

	private static class Thief extends Attack implements ItemCondition
	{
		private static final long serialVersionUID = 1L;
		private Item item;

		public Thief()
		{
			super(Namesies.THIEF_ATTACK, "The user attacks and steals the target's held item simultaneously. It can't steal if the user holds an item.", 25, Type.DARK, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.CHANGE_ITEM_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public String getSwitchMessage(ActivePokemon user, Item userItem, ActivePokemon victim, Item victimItem)
		{
			return user.getName() + " stole " + victim.getName() + "'s " + victimItem.getName() + "!";
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isHoldingItem(b) || !victim.isHoldingItem(b) || b.getTrainer(user.user()) instanceof WildPokemon || victim.hasAbility(Namesies.STICKY_HOLD_ABILITY))
			{
				if (super.category == Category.STATUS)
				{
					b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				}
				
				return;
			}
			
			Item userItem = user.getHeldItem(b), victimItem = victim.getHeldItem(b);
			b.addMessage(getSwitchMessage(user, userItem, victim, victimItem));
			
			if (b.isWildBattle())
			{
				user.giveItem((HoldItem)victimItem);
				victim.giveItem((HoldItem)userItem);
				return;
			}
			
			item = userItem;
			super.applyEffects(b, user, victim);
			
			item = victimItem;
			super.applyEffects(b, user, user);
		}

		public Item getItem()
		{
			return item;
		}
	}

	private static class Attract extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Attract()
		{
			super(Namesies.ATTRACT_ATTACK, "If it is the opposite gender of the user, the target becomes infatuated and less likely to attack.", 15, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.INFATUATED_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
		}
	}

	private static class ForcePalm extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public ForcePalm()
		{
			super(Namesies.FORCE_PALM_ATTACK, "The target is attacked with a shock wave. It may also leave the target with paralysis.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class ArmThrust extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public ArmThrust()
		{
			super(Namesies.ARM_THRUST_ATTACK, "The user looses a flurry of open-palmed arm thrusts that hit two to five times in a row.", 20, Type.FIGHTING, Category.PHYSICAL);
			super.power = 15;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int minHits = 2;
			int maxHits = 5;
			
			int hits = (int)(Math.random()*(maxHits - minHits + 1)) + minHits;
			
			if (maxHits == 5 && me.hasAbility(Namesies.SKILL_LINK_ABILITY))
			{
				hits = 5;
			}
			
			int hit = 1;
			for (; hit <= hits; hit++)
			{
				b.addMessage("Hit " + hit + "!");
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b))
				{
					break;
				}
			}
			
			// Print hits and gtfo
			b.addMessage("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!");
		}
	}

	private static class SmellingSalts extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SmellingSalts()
		{
			super(Namesies.SMELLING_SALTS_ATTACK, "This attack inflicts double damage on a target with paralysis. It also cures the target's paralysis, however.", 10, Type.NORMAL, Category.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(o.hasStatus(StatusCondition.PARALYZED) ? 2 : 1);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (victim.hasStatus(StatusCondition.PARALYZED))
			{
				Status.removeStatus(b, victim, CastSource.ATTACK);
			}
		}
	}

	private static class Assist extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Assist()
		{
			super(Namesies.ASSIST_ATTACK, "The user hurriedly and randomly uses a move among those known by other Pok\u00e9mon in the party.", 20, Type.NORMAL, Category.STATUS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.ASSISTLESS);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			List<Attack> attacks = new ArrayList<>();
			for (ActivePokemon p : b.getTrainer(me.user()).getTeam())
			{
				if (p == b.getTrainer(me.user()).front())
				{
					continue;
				}
				
				for (Move m : p.getMoves(b))
				{
					if (!m.getAttack().isMoveType(MoveType.ASSISTLESS))
					{
						attacks.add(m.getAttack());
					}
				}
			}
			
			if (attacks.size() == 0)
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			me.callNewMove(b, o, new Move(attacks.get((int)(Math.random()*attacks.size()))));
		}
	}

	private static class MetalBurst extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MetalBurst()
		{
			super(Namesies.METAL_BURST_ATTACK, "The user retaliates with much greater power against the target that last inflicted damage on it.", 10, Type.STEEL, Category.PHYSICAL);
			super.accuracy = 100;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int damageTaken = me.getAttributes().getDamageTaken();
			if (damageTaken == 0 || b.isFirstAttack())
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			o.reduceHealth(b, (int)(damageTaken*1.5));
		}
	}

	private static class WildCharge extends Attack implements RecoilMove
	{
		private static final long serialVersionUID = 1L;

		public WildCharge()
		{
			super(Namesies.WILD_CHARGE_ATTACK, "The user shrouds itself in electricity and smashes into its target. It also damages the user a little.", 15, Type.ELECTRIC, Category.PHYSICAL);
			super.power = 90;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyRecoil(Battle b, ActivePokemon user, Integer damage)
		{
			if (user.hasAbility(Namesies.ROCK_HEAD_ABILITY) || user.hasAbility(Namesies.MAGIC_GUARD_ABILITY))
			{
				return;
			}
			
			b.addMessage(user.getName() + " was hurt by recoil!");
			user.reduceHealth(b, (int)Math.ceil(damage/4.0));
		}
	}

	private static class Flash extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Flash()
		{
			super(Namesies.FLASH_ATTACK, "The user flashes a bright light that cuts the target's accuracy. It can also be used to illuminate caves.", 20, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	private static class TailGlow extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public TailGlow()
		{
			super(Namesies.TAIL_GLOW_ATTACK, "The user stares at flashing lights to focus its mind, drastically raising its Sp. Atk stat.", 20, Type.BUG, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = 3;
		}
	}

	private static class WaterSpout extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public WaterSpout()
		{
			super(Namesies.WATER_SPOUT_ATTACK, "The user spouts water to damage the opposing team. The lower the user's HP, the less powerful it becomes.", 5, Type.WATER, Category.SPECIAL);
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return (int)Math.min(1, (150*me.getHPRatio()));
		}
	}

	private static class TeeterDance extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public TeeterDance()
		{
			super(Namesies.TEETER_DANCE_ATTACK, "The user performs a wobbly dance that confuses the Pok\u00e9mon around it.", 20, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.CONFUSION_EFFECT, EffectType.POKEMON));
		}
	}

	private static class NeedleArm extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public NeedleArm()
		{
			super(Namesies.NEEDLE_ARM_ATTACK, "The user attacks by wildly swinging its thorny arms. It may also make the target flinch.", 15, Type.GRASS, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.FLINCH_EFFECT, EffectType.POKEMON));
			super.effectChance = 30;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Venoshock extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Venoshock()
		{
			super(Namesies.VENOSHOCK_ATTACK, "The user drenches the target in a special poisonous liquid. Its power is doubled if the target is poisoned.", 10, Type.POISON, Category.SPECIAL);
			super.power = 65;
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(o.hasStatus(StatusCondition.POISONED) ? 2 : 1);
		}
	}

	private static class Snatch extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Snatch()
		{
			super(Namesies.SNATCH_ATTACK, "The user steals the effects of any healing or stat-changing move the opponent attempts to use.", 10, Type.DARK, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.SNATCH_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
			super.priority = 4;
		}
	}

	private static class IceBall extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public IceBall()
		{
			super(Namesies.ICE_BALL_ATTACK, "The user continually rolls into the target over five turns. It becomes stronger each time it hits.", 20, Type.ICE, Category.PHYSICAL);
			super.power = 30;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.BOMB_BALL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(int)Math.min(me.getAttributes().getCount(), 5)*(me.hasEffect(Namesies.USED_DEFENSE_CURL_EFFECT) ? 2 : 1);
		}
	}

	private static class HeadSmash extends Attack implements RecoilMove
	{
		private static final long serialVersionUID = 1L;

		public HeadSmash()
		{
			super(Namesies.HEAD_SMASH_ATTACK, "The user attacks the target with a hazardous, full-power headbutt. The user also takes terrible damage.", 5, Type.ROCK, Category.PHYSICAL);
			super.power = 150;
			super.accuracy = 80;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyRecoil(Battle b, ActivePokemon user, Integer damage)
		{
			if (user.hasAbility(Namesies.ROCK_HEAD_ABILITY) || user.hasAbility(Namesies.MAGIC_GUARD_ABILITY))
			{
				return;
			}
			
			b.addMessage(user.getName() + " was hurt by recoil!");
			user.reduceHealth(b, (int)Math.ceil(damage/2.0));
		}
	}

	private static class MistBall extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MistBall()
		{
			super(Namesies.MIST_BALL_ATTACK, "A mistlike flurry of down envelops and damages the target. It may also lower the target's Sp. Atk.", 5, Type.PSYCHIC, Category.SPECIAL);
			super.power = 70;
			super.accuracy = 100;
			super.effectChance = 50;
			super.moveTypes.add(MoveType.BOMB_BALL);
			super.statChanges[Stat.SP_ATTACK.index()] = -1;
		}
	}

	private static class LusterPurge extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public LusterPurge()
		{
			super(Namesies.LUSTER_PURGE_ATTACK, "The user lets loose a damaging burst of light. It may also reduce the target's Sp. Def stat.", 5, Type.PSYCHIC, Category.SPECIAL);
			super.power = 70;
			super.accuracy = 100;
			super.effectChance = 50;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
		}
	}

	private static class PsychoBoost extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PsychoBoost()
		{
			super(Namesies.PSYCHO_BOOST_ATTACK, "The user attacks the target at full power. The attack's recoil harshly reduces the user's Sp. Atk stat.", 5, Type.PSYCHIC, Category.SPECIAL);
			super.power = 140;
			super.accuracy = 90;
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = -2;
		}
	}

	private static class Facade extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Facade()
		{
			super(Namesies.FACADE_ATTACK, "An attack move that doubles its power if the user is poisoned, burned, or has paralysis.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			// TODO: Should not take the attack reduction from burn
			return super.power*(me.hasStatus() ? 2 : 1);
		}
	}

	private static class DefendOrder extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DefendOrder()
		{
			super(Namesies.DEFEND_ORDER_ATTACK, "The user calls out its underlings to shield its body, raising its Defense and Sp. Def stats.", 10, Type.BUG, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
		}
	}

	private static class HealOrder extends Attack implements SelfHealingMove
	{
		private static final long serialVersionUID = 1L;

		public HealOrder()
		{
			super(Namesies.HEAL_ORDER_ATTACK, "The user calls out its underlings to heal it. The user regains up to half of its max HP.", 10, Type.BUG, Category.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect(Namesies.HEAL_BLOCK_EFFECT))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			victim.healHealthFraction(1/2.0);
			
			b.addMessage(victim.getName() + "'s health was restored!", victim);
		}
	}

	private static class AttackOrder extends Attack implements CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public AttackOrder()
		{
			super(Namesies.ATTACK_ORDER_ATTACK, "The user calls out its underlings to pummel the target. Critical hits land more easily.", 15, Type.BUG, Category.PHYSICAL);
			super.power = 90;
			super.accuracy = 100;
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}
	}

	private static class Chatter extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Chatter()
		{
			super(Namesies.CHATTER_ATTACK, "The user attacks using a sound wave based on words it has learned. It may also confuse the target.", 20, Type.FLYING, Category.SPECIAL);
			super.power = 65;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.CONFUSION_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.SOUND_BASED);
		}
	}

	private static class DualChop extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DualChop()
		{
			super(Namesies.DUAL_CHOP_ATTACK, "The user attacks its target by hitting it with brutal strikes. The target is hit twice in a row.", 15, Type.DRAGON, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int minHits = 2;
			int maxHits = 2;
			
			int hits = (int)(Math.random()*(maxHits - minHits + 1)) + minHits;
			
			if (maxHits == 5 && me.hasAbility(Namesies.SKILL_LINK_ABILITY))
			{
				hits = 5;
			}
			
			int hit = 1;
			for (; hit <= hits; hit++)
			{
				b.addMessage("Hit " + hit + "!");
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b))
				{
					break;
				}
			}
			
			// Print hits and gtfo
			b.addMessage("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!");
		}
	}

	private static class RockWrecker extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;

		public RockWrecker()
		{
			super(Namesies.ROCK_WRECKER_ATTACK, "The user launches a huge boulder at the target to attack. It must rest on the next turn, however.", 5, Type.ROCK, Category.PHYSICAL);
			super.power = 150;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.BOMB_BALL);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
		}

		public boolean chargesFirst()
		{
			return false;
		}

		public boolean semiInvulnerability()
		{
			return false;
		}

		public void charge(ActivePokemon user, Battle b)
		{
			b.addMessage(getChargeMessage(user));
		}

		public String getChargeMessage(ActivePokemon user)
		{
			return user.getName() + " must recharge!";
		}
	}

	private static class TrickRoom extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public TrickRoom()
		{
			super(Namesies.TRICK_ROOM_ATTACK, "The user creates a bizarre area in which slower Pok\u00e9mon get to move first for five turns.", 5, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.TRICK_ROOM_EFFECT, EffectType.BATTLE));
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
			super.priority = -7;
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return true;
		}
	}

	private static class RoarOfTime extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;

		public RoarOfTime()
		{
			super(Namesies.ROAR_OF_TIME_ATTACK, "The user blasts the target with power that distorts even time. The user must rest on the next turn.", 5, Type.DRAGON, Category.SPECIAL);
			super.power = 150;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
		}

		public boolean chargesFirst()
		{
			return false;
		}

		public boolean semiInvulnerability()
		{
			return false;
		}

		public void charge(ActivePokemon user, Battle b)
		{
			b.addMessage(getChargeMessage(user));
		}

		public String getChargeMessage(ActivePokemon user)
		{
			return user.getName() + " must recharge!";
		}
	}

	private static class SpacialRend extends Attack implements CritStageEffect
	{
		private static final long serialVersionUID = 1L;

		public SpacialRend()
		{
			super(Namesies.SPACIAL_REND_ATTACK, "The user tears the target along with the space around it. Critical hits land more easily.", 5, Type.DRAGON, Category.SPECIAL);
			super.power = 100;
			super.accuracy = 95;
		}

		public int increaseCritStage(Integer stage, ActivePokemon p)
		{
			return stage + 1;
		}
	}

	private static class MagmaStorm extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MagmaStorm()
		{
			super(Namesies.MAGMA_STORM_ATTACK, "The target becomes trapped within a maelstrom of fire that rages for four to five turns.", 5, Type.FIRE, Category.SPECIAL);
			super.power = 100;
			super.accuracy = 75;
			super.effects.add(Effect.getEffect(Namesies.MAGMA_STORM_EFFECT, EffectType.POKEMON));
		}
	}

	private static class CrushGrip extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public CrushGrip()
		{
			super(Namesies.CRUSH_GRIP_ATTACK, "The target is crushed with great force. The attack is more powerful the more HP the target has left.", 5, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return (int)Math.min(1, (120*o.getHPRatio()));
		}
	}

	private static class ShadowForce extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;

		public ShadowForce()
		{
			super(Namesies.SHADOW_FORCE_ATTACK, "The user disappears, then strikes the target on the second turn. It hits even if the target protects itself.", 5, Type.GHOST, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public boolean chargesFirst()
		{
			return true;
		}

		public boolean semiInvulnerability()
		{
			return true;
		}

		public void charge(ActivePokemon user, Battle b)
		{
			b.addMessage(getChargeMessage(user));
		}

		public String getChargeMessage(ActivePokemon user)
		{
			return user.getName() + " disappeared!";
		}
	}

	private static class HeartSwap extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public HeartSwap()
		{
			super(Namesies.HEART_SWAP_ATTACK, "The user employs its psychic power to switch stat changes with the target.", 10, Type.PSYCHIC, Category.STATUS);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			for (int i = 0; i < Stat.NUM_BATTLE_STATS; i++)
			{
				int temp = user.getAttributes().getStage(i);
				user.getAttributes().setStage(i, victim.getAttributes().getStage(i));
				victim.getAttributes().setStage(i, temp);
			}
			
			b.addMessage(user.getName() + " swapped its stats with " + victim.getName() + "!");
		}
	}

	private static class DarkVoid extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DarkVoid()
		{
			super(Namesies.DARK_VOID_ATTACK, "Opposing Pok\u00e9mon are dragged into a world of total darkness that makes them sleep.", 10, Type.DARK, Category.STATUS);
			super.accuracy = 80;
			super.status = StatusCondition.ASLEEP;
		}
	}

	private static class SeedFlare extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SeedFlare()
		{
			super(Namesies.SEED_FLARE_ATTACK, "The user emits a shock wave from its body to attack its target. It may harshly lower the target's Sp. Def.", 5, Type.GRASS, Category.SPECIAL);
			super.power = 120;
			super.accuracy = 85;
			super.effectChance = 40;
			super.statChanges[Stat.SP_DEFENSE.index()] = -2;
		}
	}

	private static class Judgment extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Judgment()
		{
			super(Namesies.JUDGMENT_ATTACK, "The user releases countless shots of light at the target. Its type varies with the kind of Plate the user is holding.", 10, Type.NORMAL, Category.SPECIAL);
			super.power = 100;
			super.accuracy = 100;
		}

		public Type setType(Battle b, ActivePokemon user)
		{
			Item i = user.getHeldItem(b);
			if (i instanceof PlateItem)
			{
				return ((PlateItem)i).getType();
			}
			
			return super.type;
		}
	}

	private static class SearingShot extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SearingShot()
		{
			super(Namesies.SEARING_SHOT_ATTACK, "An inferno of scarlet flames torches everything around the user. It may leave targets with a burn.", 5, Type.FIRE, Category.SPECIAL);
			super.power = 100;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.BURNED;
			super.moveTypes.add(MoveType.BOMB_BALL);
		}
	}

	private static class Incinerate extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Incinerate()
		{
			super(Namesies.INCINERATE_ATTACK, "The user attacks the target with fire. If the target is holding a Berry, the Berry becomes burnt up and unusable.", 15, Type.FIRE, Category.SPECIAL);
			super.power = 60;
			super.accuracy = 100;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Item heldItem = victim.getHeldItem(b);
			
			if (heldItem instanceof Berry || heldItem instanceof GemItem)
			{
				b.addMessage(victim.getName() + "'s " + heldItem.getName() + " was burned!");
				victim.consumeItem(b);
			}
		}
	}

	private static class Overheat extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Overheat()
		{
			super(Namesies.OVERHEAT_ATTACK, "The user attacks the target at full power. The attack's recoil harshly reduces the user's Sp. Atk stat.", 5, Type.FIRE, Category.SPECIAL);
			super.power = 130;
			super.accuracy = 90;
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = -2;
		}
	}

	private static class HeatCrash extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public HeatCrash()
		{
			super(Namesies.HEAT_CRASH_ATTACK, "The user slams its target with its flame- covered body. The more the user outweighs the target, the greater the damage.", 10, Type.FIRE, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			double ratio = o.getWeight(b)/me.getWeight(b);
			if (ratio > .5) return 40;
			if (ratio > .33) return 60;
			if (ratio > .25) return 80;
			if (ratio > .2) return 100;
			return 120;
		}
	}

	private static class GrassKnot extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public GrassKnot()
		{
			super(Namesies.GRASS_KNOT_ATTACK, "The user snares the target with grass and trips it. The heavier the target, the greater the damage.", 20, Type.GRASS, Category.SPECIAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			double weight = o.getWeight(b);
			if (weight < 22) return 20;
			if (weight < 55) return 40;
			if (weight < 110) return 60;
			if (weight < 220) return 80;
			if (weight < 440) return 100;
			return 120;
		}
	}

	private static class Scald extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Scald()
		{
			super(Namesies.SCALD_ATTACK, "The user shoots boiling hot water at its target. It may also leave the target with a burn.", 15, Type.WATER, Category.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.BURNED;
			super.moveTypes.add(MoveType.DEFROST);
		}
	}

	private static class DrainPunch extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DrainPunch()
		{
			super(Namesies.DRAIN_PUNCH_ATTACK, "An energy-draining punch. The user's HP is restored by half the damage taken by the target.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.power = 75;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PUNCHING);
			super.moveTypes.add(MoveType.SAP_HEALTH);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class StormThrow extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public StormThrow()
		{
			super(Namesies.STORM_THROW_ATTACK, "The user strikes the target with a fierce blow. This attack always results in a critical hit.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.ALWAYS_CRIT);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class FrostBreath extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FrostBreath()
		{
			super(Namesies.FROST_BREATH_ATTACK, "The user blows a cold breath on the target. This attack always results in a critical hit.", 10, Type.ICE, Category.SPECIAL);
			super.power = 60;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.ALWAYS_CRIT);
		}
	}

	private static class RockSmash extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public RockSmash()
		{
			super(Namesies.ROCK_SMASH_ATTACK, "The user attacks with a punch that can shatter a rock. It may also lower the target's Defense stat.", 15, Type.FIGHTING, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.effectChance = 50;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class RockClimb extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public RockClimb()
		{
			super(Namesies.ROCK_CLIMB_ATTACK, "The user attacks the target by smashing into it with incredible force. It may also confuse the target.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 90;
			super.accuracy = 85;
			super.effects.add(Effect.getEffect(Namesies.CONFUSION_EFFECT, EffectType.POKEMON));
			super.effectChance = 20;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class NightDaze extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public NightDaze()
		{
			super(Namesies.NIGHT_DAZE_ATTACK, "The user lets loose a pitch-black shock wave at its target. It may also lower the target's accuracy.", 10, Type.DARK, Category.SPECIAL);
			super.power = 85;
			super.accuracy = 95;
			super.effectChance = 40;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	private static class TailSlap extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public TailSlap()
		{
			super(Namesies.TAIL_SLAP_ATTACK, "The user attacks by striking the target with its hard tail. It hits the target two to five times in a row.", 10, Type.NORMAL, Category.PHYSICAL);
			super.power = 25;
			super.accuracy = 85;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int minHits = 2;
			int maxHits = 5;
			
			int hits = (int)(Math.random()*(maxHits - minHits + 1)) + minHits;
			
			if (maxHits == 5 && me.hasAbility(Namesies.SKILL_LINK_ABILITY))
			{
				hits = 5;
			}
			
			int hit = 1;
			for (; hit <= hits; hit++)
			{
				b.addMessage("Hit " + hit + "!");
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b))
				{
					break;
				}
			}
			
			// Print hits and gtfo
			b.addMessage("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!");
		}
	}

	private static class Defog extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Defog()
		{
			super(Namesies.DEFOG_ATTACK, "A strong wind blows away the target's obstacles such as Reflect or Light Screen. It also lowers the target's evasiveness.", 15, Type.FLYING, Category.STATUS);
			super.statChanges[Stat.EVASION.index()] = -1;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			super.applyEffects(b, user, victim);
			
			Global.invoke(b.getEffectsList(victim), DefogRelease.class, "releaseDefog", b, victim);
		}
	}

	private static class HornLeech extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public HornLeech()
		{
			super(Namesies.HORN_LEECH_ATTACK, "The user drains the target's energy with its horns. The user's HP is restored by half the damage taken by the target.", 10, Type.GRASS, Category.PHYSICAL);
			super.power = 75;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SAP_HEALTH);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Electroweb extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Electroweb()
		{
			super(Namesies.ELECTROWEB_ATTACK, "The user captures and attacks opposing Pok\u00e9mon by using an electric net. It reduces the targets' Speed stat.", 15, Type.ELECTRIC, Category.SPECIAL);
			super.power = 55;
			super.accuracy = 95;
			super.statChanges[Stat.SPEED.index()] = -1;
		}
	}

	private static class GearGrind extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public GearGrind()
		{
			super(Namesies.GEAR_GRIND_ATTACK, "The user attacks by throwing two steel gears at its target.", 15, Type.STEEL, Category.PHYSICAL);
			super.power = 50;
			super.accuracy = 85;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int minHits = 2;
			int maxHits = 2;
			
			int hits = (int)(Math.random()*(maxHits - minHits + 1)) + minHits;
			
			if (maxHits == 5 && me.hasAbility(Namesies.SKILL_LINK_ABILITY))
			{
				hits = 5;
			}
			
			int hit = 1;
			for (; hit <= hits; hit++)
			{
				b.addMessage("Hit " + hit + "!");
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b))
				{
					break;
				}
			}
			
			// Print hits and gtfo
			b.addMessage("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!");
		}
	}

	private static class ShiftGear extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public ShiftGear()
		{
			super(Namesies.SHIFT_GEAR_ATTACK, "The user rotates its gears, raising its Attack and sharply raising its Speed.", 10, Type.STEEL, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.SPEED.index()] = 2;
		}
	}

	private static class HeadCharge extends Attack implements RecoilMove
	{
		private static final long serialVersionUID = 1L;

		public HeadCharge()
		{
			super(Namesies.HEAD_CHARGE_ATTACK, "The user charges its head into its target, using its powerful guard hair. It also damages the user a little.", 15, Type.NORMAL, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyRecoil(Battle b, ActivePokemon user, Integer damage)
		{
			if (user.hasAbility(Namesies.ROCK_HEAD_ABILITY) || user.hasAbility(Namesies.MAGIC_GUARD_ABILITY))
			{
				return;
			}
			
			b.addMessage(user.getName() + " was hurt by recoil!");
			user.reduceHealth(b, (int)Math.ceil(damage/4.0));
		}
	}

	private static class FieryDance extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FieryDance()
		{
			super(Namesies.FIERY_DANCE_ATTACK, "Cloaked in flames, the user dances and flaps its wings. It may also raise the user's Sp. Atk stat.", 10, Type.FIRE, Category.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.effectChance = 50;
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
		}
	}

	private static class SacredSword extends Attack implements IgnoreStageEffect
	{
		private static final long serialVersionUID = 1L;

		public SacredSword()
		{
			super(Namesies.SACRED_SWORD_ATTACK, "The user attacks by slicing with its long horns. The target's stat changes don't affect this attack's damage.", 20, Type.FIGHTING, Category.PHYSICAL);
			super.power = 90;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public boolean ignoreStage(Stat s)
		{
			return !s.user();
		}
	}

	private static class SecretSword extends Attack implements StatSwitchingEffect
	{
		private static final long serialVersionUID = 1L;

		public SecretSword()
		{
			super(Namesies.SECRET_SWORD_ATTACK, "The user cuts with its long horn. The odd power contained in the horn does physical damage to the target.", 10, Type.FIGHTING, Category.SPECIAL);
			super.power = 85;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.METRONOMELESS);
		}

		public Stat switchStat(Stat s)
		{
			return s == Stat.SP_DEFENSE ? Stat.DEFENSE : s;
		}
	}

	private static class FusionFlare extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FusionFlare()
		{
			super(Namesies.FUSION_FLARE_ATTACK, "The user brings down a giant flame. This attack does greater damage when influenced by an enormous thunderbolt.", 5, Type.FIRE, Category.SPECIAL);
			super.power = 100;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.DEFROST);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(!b.isFirstAttack() && o.getAttack().namesies() == Namesies.FUSION_BOLT_ATTACK ? 2 : 1);
		}
	}

	private static class FusionBolt extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FusionBolt()
		{
			super(Namesies.FUSION_BOLT_ATTACK, "The user throws down a giant thunderbolt. This attack does greater damage when influenced by an enormous flame.", 5, Type.ELECTRIC, Category.PHYSICAL);
			super.power = 100;
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(!b.isFirstAttack() && o.getAttack().namesies() == Namesies.FUSION_FLARE_ATTACK ? 2 : 1);
		}
	}

	private static class BlueFlare extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public BlueFlare()
		{
			super(Namesies.BLUE_FLARE_ATTACK, "The user attacks by engulfing the target in an intense, yet beautiful, blue flame. It may leave the target with a burn.", 5, Type.FIRE, Category.SPECIAL);
			super.power = 130;
			super.accuracy = 85;
			super.effectChance = 20;
			super.status = StatusCondition.BURNED;
		}
	}

	private static class BoltStrike extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public BoltStrike()
		{
			super(Namesies.BOLT_STRIKE_ATTACK, "The user charges its target, surrounding itself with a great amount of electricity. It may leave the target with paralysis.", 5, Type.ELECTRIC, Category.PHYSICAL);
			super.power = 130;
			super.accuracy = 85;
			super.effectChance = 20;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Glaciate extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Glaciate()
		{
			super(Namesies.GLACIATE_ATTACK, "The user attacks by blowing freezing cold air at opposing Pok\u00e9mon. This attack reduces the targets' Speed stat.", 10, Type.ICE, Category.SPECIAL);
			super.power = 65;
			super.accuracy = 95;
			super.statChanges[Stat.SPEED.index()] = -1;
		}
	}

	private static class TechnoBlast extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public TechnoBlast()
		{
			super(Namesies.TECHNO_BLAST_ATTACK, "The user fires a beam of light at its target. The type changes depending on the Drive the user holds.", 5, Type.NORMAL, Category.SPECIAL);
			super.power = 120;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.METRONOMELESS);
		}

		public Type setType(Battle b, ActivePokemon user)
		{
			Item i = user.getHeldItem(b);
			if (i instanceof DriveItem)
			{
				return ((DriveItem)i).getType();
			}
			
			return super.type;
		}
	}

	private static class Explosion extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Explosion()
		{
			super(Namesies.EXPLOSION_ATTACK, "The user explodes to inflict damage on those around it. The user faints upon using this move.", 5, Type.NORMAL, Category.PHYSICAL);
			super.power = 250;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.USER_FAINTS);
		}
	}

	private static class SelfDestruct extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SelfDestruct()
		{
			super(Namesies.SELF_DESTRUCT_ATTACK, "The user attacks everything around it by causing an explosion. The user faints upon using this move.", 5, Type.NORMAL, Category.PHYSICAL);
			super.power = 200;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.USER_FAINTS);
		}
	}

	private static class Fling extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Fling()
		{
			super(Namesies.FLING_ATTACK, "The user flings its held item at the target to attack. Its power and effects depend on the item.", 10, Type.DARK, Category.PHYSICAL);
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			if (me.isHoldingItem(b))
			{
				return ((HoldItem)me.getHeldItem(b)).flingDamage();
			}
			
			return super.power;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (!me.isHoldingItem(b))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			b.addMessage(me.getName() + " flung its " + me.getHeldItem(b).getName() + "!");
			super.apply(me, o, b);
			me.consumeItem(b);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			((HoldItem)user.getHeldItem(b)).flingEffect(b, victim);
		}
	}

	private static class FreezeShock extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;

		public FreezeShock()
		{
			super(Namesies.FREEZE_SHOCK_ATTACK, "On the second turn, the user hits the target with electrically charged ice. It may leave the target with paralysis.", 5, Type.ICE, Category.PHYSICAL);
			super.power = 140;
			super.accuracy = 90;
			super.effectChance = 30;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
		}

		public boolean chargesFirst()
		{
			return true;
		}

		public boolean semiInvulnerability()
		{
			return false;
		}

		public void charge(ActivePokemon user, Battle b)
		{
			b.addMessage(getChargeMessage(user));
		}

		public String getChargeMessage(ActivePokemon user)
		{
			return user.getName() + " is charging!";
		}
	}

	private static class SecretPower extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public SecretPower()
		{
			super(Namesies.SECRET_POWER_ATTACK, "The user attacks the target with a secret power. Its added effects vary depending on the user's environment.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.effectChance = 30;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			// TODO: BATTLE LOCATION
		}
	}

	private static class FinalGambit extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FinalGambit()
		{
			super(Namesies.FINAL_GAMBIT_ATTACK, "The user risks everything to attack its target. The user faints but does damage equal to the user's HP.", 5, Type.FIGHTING, Category.SPECIAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.USER_FAINTS);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			o.reduceHealth(b, me.getHP());
		}
	}

	private static class GastroAcid extends Attack implements ChangeAbilityMove
	{
		private static final long serialVersionUID = 1L;

		public GastroAcid()
		{
			super(Namesies.GASTRO_ACID_ATTACK, "The user hurls up its stomach acids on the target. The fluid eliminates the effect of the target's Ability.", 10, Type.POISON, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.CHANGE_ABILITY_EFFECT, EffectType.POKEMON));
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			switch (o.getAbility().namesies())
			{
				case MULTITYPE_ABILITY:
				case STANCE_CHANGE_ABILITY:
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
				default:
				super.apply(me, o, b);
			}
		}

		public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return Ability.getAbility(Namesies.NONE_ABILITY).newInstance();
		}

		public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return caster.getName() + " suppressed " + victim.getName() + "'s ability!";
		}
	}

	private static class HealingWish extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public HealingWish()
		{
			super(Namesies.HEALING_WISH_ATTACK, "The user faints. In return, the Pok\u00e9mon taking its place will have its HP restored and status cured.", 10, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.HEAL_SWITCH_EFFECT, EffectType.TEAM));
			super.moveTypes.add(MoveType.USER_FAINTS);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}
	}

	private static class LunarDance extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public LunarDance()
		{
			super(Namesies.LUNAR_DANCE_ATTACK, "The user faints. In return, the Pok\u00e9mon taking its place will have its status and HP fully restored.", 10, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.HEAL_SWITCH_EFFECT, EffectType.TEAM));
			super.moveTypes.add(MoveType.USER_FAINTS);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}
	}

	private static class Roar extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Roar()
		{
			super(Namesies.ROAR_ATTACK, "The target is scared off and replaced by another Pok\u00e9mon in its party. In the wild, the battle ends.", 20, Type.NORMAL, Category.STATUS);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.priority = -6;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			// Fails against the Suction Cups ability
			if (victim.hasAbility(Namesies.SUCTION_CUPS_ABILITY) && !user.breaksTheMold())
			{
				b.addMessage(victim.getName() + "'s " + Namesies.SUCTION_CUPS_ABILITY.getName() + " prevents it from switching!");
				return;
			}
			
			// Fails if this is the first attack of the turn, or if the victim is rooted by Ingrain
			if (b.isFirstAttack() || victim.hasEffect(Namesies.INGRAIN_EFFECT))
			{
				if (super.category == Category.STATUS)
				{
					b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				}
				
				return;
			}
			
			Team opponent = b.getTrainer(victim.user());
			if (opponent instanceof WildPokemon)
			{
				// Fails against wild Pokemon of higher levels
				if (victim.getLevel() > user.getLevel())
				{
					b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
					return;
				}
				
				// End the battle against a wild Pokemon
				b.addMessage(victim.getName() + " fled in fear!");
				b.addMessage(" ", Update.EXIT_BATTLE);
				return;
			}
			
			Trainer trainer = (Trainer)opponent;
			if (!trainer.hasRemainingPokemon())
			{
				// Fails against trainers on their last Pokemon
				if (super.category == Category.STATUS)
				{
					b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				}
				
				return;
			}
			
			// Swap to a random Pokemon!
			b.addMessage(victim.getName() + " fled in fear!");
			trainer.switchToRandom();
			victim = trainer.front();
			b.enterBattle(victim, "...and " + victim.getName() + " was dragged out!");
		}
	}

	private static class Grudge extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Grudge()
		{
			super(Namesies.GRUDGE_ATTACK, "If the user faints, the user's grudge fully depletes the PP of the opponent's move that knocked it out.", 5, Type.GHOST, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.GRUDGE_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}
	}

	private static class Retaliate extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Retaliate()
		{
			super(Namesies.RETALIATE_ATTACK, "The user gets revenge for a fainted ally. If an ally fainted in the previous turn, this attack's damage increases.", 5, Type.NORMAL, Category.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(Effect.hasEffect(b.getEffects(me.user()), Namesies.DEAD_ALLY_EFFECT) ? 2 : 1);
		}
	}

	private static class CircleThrow extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public CircleThrow()
		{
			super(Namesies.CIRCLE_THROW_ATTACK, "The user throws the target and drags out another Pok\u00e9mon in its party. In the wild, the battle ends.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.priority = -6;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			// Fails against the Suction Cups ability
			if (victim.hasAbility(Namesies.SUCTION_CUPS_ABILITY) && !user.breaksTheMold())
			{
				b.addMessage(victim.getName() + "'s " + Namesies.SUCTION_CUPS_ABILITY.getName() + " prevents it from switching!");
				return;
			}
			
			// Fails if this is the first attack of the turn, or if the victim is rooted by Ingrain
			if (b.isFirstAttack() || victim.hasEffect(Namesies.INGRAIN_EFFECT))
			{
				if (super.category == Category.STATUS)
				{
					b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				}
				
				return;
			}
			
			Team opponent = b.getTrainer(victim.user());
			if (opponent instanceof WildPokemon)
			{
				// Fails against wild Pokemon of higher levels
				if (victim.getLevel() > user.getLevel())
				{
					b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
					return;
				}
				
				// End the battle against a wild Pokemon
				b.addMessage(victim.getName() + " was thrown away!");
				b.addMessage(" ", Update.EXIT_BATTLE);
				return;
			}
			
			Trainer trainer = (Trainer)opponent;
			if (!trainer.hasRemainingPokemon())
			{
				// Fails against trainers on their last Pokemon
				if (super.category == Category.STATUS)
				{
					b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				}
				
				return;
			}
			
			// Swap to a random Pokemon!
			b.addMessage(victim.getName() + " was thrown away!");
			trainer.switchToRandom();
			victim = trainer.front();
			b.enterBattle(victim, "...and " + victim.getName() + " was dragged out!");
		}
	}

	private static class Teleport extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Teleport()
		{
			super(Namesies.TELEPORT_ATTACK, "Use it to flee from any wild Pok\u00e9mon. It can also warp to the last Pok\u00e9mon Center visited.", 20, Type.PSYCHIC, Category.STATUS);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (!b.isWildBattle())
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			b.addMessage(user.getName() + " teleported out of battle!");
			b.addMessage(" ", MessageUpdate.Update.EXIT_BATTLE);
		}
	}

	private static class RolePlay extends Attack implements ChangeAbilityMove
	{
		private static final long serialVersionUID = 1L;

		public RolePlay()
		{
			super(Namesies.ROLE_PLAY_ATTACK, "The user mimics the target completely, copying the target's natural Ability.", 10, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.CHANGE_ABILITY_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			switch (o.getAbility().namesies())
			{
				case WONDER_GUARD_ABILITY:
				case MULTITYPE_ABILITY:
				case STANCE_CHANGE_ABILITY:
				case IMPOSTER_ABILITY:
				case ILLUSION_ABILITY:
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
				default:
				super.apply(me, o, b);
			}
		}

		public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return b.getOtherPokemon(victim.user()).getAbility().newInstance();
		}

		public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			ActivePokemon other = b.getOtherPokemon(victim.user());
			return victim.getName() + " copied " + other.getName() + "'s " + other.getAbility().getName() + "!";
		}
	}

	private static class KnockOff extends Attack implements ItemCondition
	{
		private static final long serialVersionUID = 1L;

		public KnockOff()
		{
			super(Namesies.KNOCK_OFF_ATTACK, "The user slaps down the target's held item, preventing that item from being used in the battle.", 25, Type.DARK, Category.PHYSICAL);
			super.power = 65;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.CHANGE_ITEM_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return (int)(super.power*(o.isHoldingItem(b) ? 1.5 : 1));
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (!victim.isHoldingItem(b) || victim.hasAbility(Namesies.STICKY_HOLD_ABILITY))
			{
				return;
			}
			
			b.addMessage(user.getName() + " knocked off " + victim.getName() + "'s " + victim.getHeldItem(b).getName() + "!");
			super.applyEffects(b, user, victim);
		}

		public Item getItem()
		{
			return Item.noneItem();
		}
	}

	private static class Whirlwind extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Whirlwind()
		{
			super(Namesies.WHIRLWIND_ATTACK, "The target is blown away, to be replaced by another Pok\u00e9mon in its party. In the wild, the battle ends.", 20, Type.NORMAL, Category.STATUS);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.priority = -6;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			// Fails against the Suction Cups ability
			if (victim.hasAbility(Namesies.SUCTION_CUPS_ABILITY) && !user.breaksTheMold())
			{
				b.addMessage(victim.getName() + "'s " + Namesies.SUCTION_CUPS_ABILITY.getName() + " prevents it from switching!");
				return;
			}
			
			// Fails if this is the first attack of the turn, or if the victim is rooted by Ingrain
			if (b.isFirstAttack() || victim.hasEffect(Namesies.INGRAIN_EFFECT))
			{
				if (super.category == Category.STATUS)
				{
					b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				}
				
				return;
			}
			
			Team opponent = b.getTrainer(victim.user());
			if (opponent instanceof WildPokemon)
			{
				// Fails against wild Pokemon of higher levels
				if (victim.getLevel() > user.getLevel())
				{
					b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
					return;
				}
				
				// End the battle against a wild Pokemon
				b.addMessage(victim.getName() + " blew away!");
				b.addMessage(" ", Update.EXIT_BATTLE);
				return;
			}
			
			Trainer trainer = (Trainer)opponent;
			if (!trainer.hasRemainingPokemon())
			{
				// Fails against trainers on their last Pokemon
				if (super.category == Category.STATUS)
				{
					b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				}
				
				return;
			}
			
			// Swap to a random Pokemon!
			b.addMessage(victim.getName() + " blew away!");
			trainer.switchToRandom();
			victim = trainer.front();
			b.enterBattle(victim, "...and " + victim.getName() + " was dragged out!");
		}
	}

	private static class Bestow extends Attack implements ItemCondition
	{
		private static final long serialVersionUID = 1L;
		private Item item;

		public Bestow()
		{
			super(Namesies.BESTOW_ATTACK, "The user passes its held item to the target when the target isn't holding an item.", 15, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.CHANGE_ITEM_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public String getSwitchMessage(ActivePokemon user, Item userItem, ActivePokemon victim, Item victimItem)
		{
			return user.getName() + " gave " + victim.getName() + " its " + userItem.getName() + "!";
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (!user.isHoldingItem(b) || victim.isHoldingItem(b))
			{
				if (super.category == Category.STATUS)
				{
					b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				}
				
				return;
			}
			
			Item userItem = user.getHeldItem(b), victimItem = victim.getHeldItem(b);
			b.addMessage(getSwitchMessage(user, userItem, victim, victimItem));
			
			if (b.isWildBattle())
			{
				user.giveItem((HoldItem)victimItem);
				victim.giveItem((HoldItem)userItem);
				return;
			}
			
			item = userItem;
			super.applyEffects(b, user, victim);
			
			item = victimItem;
			super.applyEffects(b, user, user);
		}

		public Item getItem()
		{
			return item;
		}
	}

	private static class Switcheroo extends Attack implements ItemCondition
	{
		private static final long serialVersionUID = 1L;
		private Item item;

		public Switcheroo()
		{
			super(Namesies.SWITCHEROO_ATTACK, "The user passes its held item to the target when the target isn't holding an item.", 10, Type.DARK, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.CHANGE_ITEM_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public String getSwitchMessage(ActivePokemon user, Item userItem, ActivePokemon victim, Item victimItem)
		{
			return user.getName() + " switched its " + userItem.getName() + " with " + victim.getName() + "'s " + victimItem.getName() + "!";
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if ((!user.isHoldingItem(b) && !victim.isHoldingItem(b)) || user.hasAbility(Namesies.STICKY_HOLD_ABILITY) || victim.hasAbility(Namesies.STICKY_HOLD_ABILITY))
			{
				if (super.category == Category.STATUS)
				{
					b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				}
				
				return;
			}
			
			Item userItem = user.getHeldItem(b), victimItem = victim.getHeldItem(b);
			b.addMessage(getSwitchMessage(user, userItem, victim, victimItem));
			
			if (b.isWildBattle())
			{
				user.giveItem((HoldItem)victimItem);
				victim.giveItem((HoldItem)userItem);
				return;
			}
			
			item = userItem;
			super.applyEffects(b, user, victim);
			
			item = victimItem;
			super.applyEffects(b, user, user);
		}

		public Item getItem()
		{
			return item;
		}
	}

	private static class Trick extends Attack implements ItemCondition
	{
		private static final long serialVersionUID = 1L;
		private Item item;

		public Trick()
		{
			super(Namesies.TRICK_ATTACK, "The user catches the target off guard and swaps its held item with its own.", 10, Type.PSYCHIC, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.CHANGE_ITEM_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public String getSwitchMessage(ActivePokemon user, Item userItem, ActivePokemon victim, Item victimItem)
		{
			return user.getName() + " switched its " + userItem.getName() + " with " + victim.getName() + "'s " + victimItem.getName() + "!";
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if ((!user.isHoldingItem(b) && !victim.isHoldingItem(b)) || user.hasAbility(Namesies.STICKY_HOLD_ABILITY) || victim.hasAbility(Namesies.STICKY_HOLD_ABILITY))
			{
				if (super.category == Category.STATUS)
				{
					b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				}
				
				return;
			}
			
			Item userItem = user.getHeldItem(b), victimItem = victim.getHeldItem(b);
			b.addMessage(getSwitchMessage(user, userItem, victim, victimItem));
			
			if (b.isWildBattle())
			{
				user.giveItem((HoldItem)victimItem);
				victim.giveItem((HoldItem)userItem);
				return;
			}
			
			item = userItem;
			super.applyEffects(b, user, victim);
			
			item = victimItem;
			super.applyEffects(b, user, user);
		}

		public Item getItem()
		{
			return item;
		}
	}

	private static class Memento extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Memento()
		{
			super(Namesies.MEMENTO_ATTACK, "The user faints when using this move. In return, it harshly lowers the target's Attack and Sp. Atk.", 10, Type.DARK, Category.STATUS);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.USER_FAINTS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.statChanges[Stat.ATTACK.index()] = -2;
			super.statChanges[Stat.SP_ATTACK.index()] = -2;
		}
	}

	private static class DestinyBond extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DestinyBond()
		{
			super(Namesies.DESTINY_BOND_ATTACK, "When this move is used, if the user faints, the Pok\u00e9mon that landed the knockout hit also faints.", 5, Type.GHOST, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.DESTINY_BOND_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}
	}

	private static class Camouflage extends Attack implements ChangeTypeMove
	{
		private static final long serialVersionUID = 1L;

		public Camouflage()
		{
			super(Namesies.CAMOUFLAGE_ATTACK, "The user's type is changed depending on its environment, such as at water's edge, in grass, or in a cave.", 20, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.CHANGE_TYPE_EFFECT, EffectType.POKEMON));
			super.selfTarget = true;
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return new Type[] {Type.NORMAL, Type.NONE}; // TODO: Battle environments
		}
	}

	private static class Recycle extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Recycle()
		{
			super(Namesies.RECYCLE_ATTACK, "The user recycles a held item that has been used in battle so it can be used again.", 10, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			PokemonEffect consumed = victim.getEffect(Namesies.CONSUMED_ITEM_EFFECT);
			if (consumed == null || victim.isHoldingItem(b))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			Item restored = ((ItemCondition)consumed).getItem();
			victim.giveItem((HoldItem)restored);
			b.addMessage(victim.getName() + "'s " + restored.getName() + " was restored!");
		}
	}

	private static class PartingShot extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PartingShot()
		{
			super(Namesies.PARTING_SHOT_ATTACK, "With a parting threat, the user lowers the target's Attack and Sp. Atk stats. Then it switches with a party Pok\u00e9mon.", 20, Type.DARK, Category.STATUS);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.statChanges[Stat.ATTACK.index()] = -1;
			super.statChanges[Stat.SP_ATTACK.index()] = -1;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			// First execute the move as normal
			super.apply(me, o, b);
			
			Team t = b.getTrainer(me.user());
			if (t instanceof WildPokemon)
			{
				// End the battle against a wild Pokemon
				b.addMessage(me.getName() + " left the battle!");
				b.addMessage(" ", MessageUpdate.Update.EXIT_BATTLE);
				return;
			}
			
			Trainer trainer = (Trainer)t;
			if (!trainer.hasRemainingPokemon())
			{
				// Don't switch if no one to switch to
				return;
			}
			
			// Send this Pokemon back to the trainer and send out the next one
			b.addMessage(me.getName() + " went back to " + trainer.getName() + "!");
			trainer.switchToRandom(); // TODO: Prompt a legit switch fo user
			me = trainer.front();
			b.enterBattle(me, trainer.getName() + " sent out " + me.getName() + "!");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			b.addMessage(user.getName() + " called " + victim.getName() + " a chump!!");
			super.applyEffects(b, user, victim);
		}
	}

	private static class UTurn extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public UTurn()
		{
			super(Namesies.UTURN_ATTACK, "After making its attack, the user rushes back to switch places with a party Pok\u00e9mon in waiting.", 20, Type.BUG, Category.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			// First execute the move as normal
			super.apply(me, o, b);
			
			Team t = b.getTrainer(me.user());
			if (t instanceof WildPokemon)
			{
				// End the battle against a wild Pokemon
				b.addMessage(me.getName() + " left the battle!");
				b.addMessage(" ", MessageUpdate.Update.EXIT_BATTLE);
				return;
			}
			
			Trainer trainer = (Trainer)t;
			if (!trainer.hasRemainingPokemon())
			{
				// Don't switch if no one to switch to
				return;
			}
			
			// Send this Pokemon back to the trainer and send out the next one
			b.addMessage(me.getName() + " went back to " + trainer.getName() + "!");
			trainer.switchToRandom(); // TODO: Prompt a legit switch fo user
			me = trainer.front();
			b.enterBattle(me, trainer.getName() + " sent out " + me.getName() + "!");
		}
	}

	private static class BatonPass extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public BatonPass()
		{
			super(Namesies.BATON_PASS_ATTACK, "The user switches places with a party Pok\u00e9mon in waiting, passing along any stat changes.", 40, Type.NORMAL, Category.STATUS);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Team t = b.getTrainer(user.user());
			if (t instanceof WildPokemon)
			{
				b.addMessage(user.getName() + " left the battle!");
				b.addMessage(" ", MessageUpdate.Update.EXIT_BATTLE);
				return;
			}
			
			Trainer trainer = (Trainer)t;
			if (!trainer.hasRemainingPokemon())
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			b.addMessage(user.getName() + " went back to " + trainer.getName() + "!");
			trainer.switchToRandom(); // TODO: Prompt a legit switch fo user
			
			ActivePokemon next = trainer.front();
			next.resetAttributes();
			for (int i = 0; i < Stat.NUM_BATTLE_STATS; i++)
			{
				next.getAttributes().setStage(i, user.getStage(i));
			}
			for (PokemonEffect e : user.getEffects())
			{
				if (e instanceof PassableEffect) next.addEffect(e);
			}
			
			user = next;
			b.enterBattle(user, trainer.getName() + " sent out " + user.getName() + "!", false);
		}
	}

	private static class PerishSong extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PerishSong()
		{
			super(Namesies.PERISH_SONG_ATTACK, "Any Pok\u00e9mon that hears this song faints in three turns, unless it switches out of battle.", 5, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.PERISH_SONG_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			b.addMessage("All Pokemon hearing this song will faint in three turns!");
			
			if (!victim.hasEffect(Namesies.PERISH_SONG_EFFECT))
			{
				super.applyEffects(b, user, victim);
			}
			
			if (!user.hasEffect(Namesies.PERISH_SONG_EFFECT))
			{
				super.applyEffects(b, user, user);
			}
		}
	}

	private static class DragonTail extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DragonTail()
		{
			super(Namesies.DRAGON_TAIL_ATTACK, "The user knocks away the target and drags out another Pok\u00e9mon in its party. In the wild, the battle ends.", 10, Type.DRAGON, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.priority = -6;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			// Fails against the Suction Cups ability
			if (victim.hasAbility(Namesies.SUCTION_CUPS_ABILITY) && !user.breaksTheMold())
			{
				b.addMessage(victim.getName() + "'s " + Namesies.SUCTION_CUPS_ABILITY.getName() + " prevents it from switching!");
				return;
			}
			
			// Fails if this is the first attack of the turn, or if the victim is rooted by Ingrain
			if (b.isFirstAttack() || victim.hasEffect(Namesies.INGRAIN_EFFECT))
			{
				if (super.category == Category.STATUS)
				{
					b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				}
				
				return;
			}
			
			Team opponent = b.getTrainer(victim.user());
			if (opponent instanceof WildPokemon)
			{
				// Fails against wild Pokemon of higher levels
				if (victim.getLevel() > user.getLevel())
				{
					b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
					return;
				}
				
				// End the battle against a wild Pokemon
				b.addMessage(victim.getName() + " was slapped away!");
				b.addMessage(" ", Update.EXIT_BATTLE);
				return;
			}
			
			Trainer trainer = (Trainer)opponent;
			if (!trainer.hasRemainingPokemon())
			{
				// Fails against trainers on their last Pokemon
				if (super.category == Category.STATUS)
				{
					b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				}
				
				return;
			}
			
			// Swap to a random Pokemon!
			b.addMessage(victim.getName() + " was slapped away!");
			trainer.switchToRandom();
			victim = trainer.front();
			b.enterBattle(victim, "...and " + victim.getName() + " was dragged out!");
		}
	}

	private static class FoulPlay extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FoulPlay()
		{
			super(Namesies.FOUL_PLAY_ATTACK, "The user turns the target's power against it. The higher the target's Attack stat, the greater the damage.", 15, Type.DARK, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			double ratio = (double)Stat.getStat(Stat.ATTACK, me, o, b)/Stat.getStat(Stat.ATTACK, o, me, b);
			if (ratio > .5) return 60;
			if (ratio > .33) return 80;
			if (ratio > .25) return 120;
			return 150;
		}
	}

	private static class Embargo extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Embargo()
		{
			super(Namesies.EMBARGO_ATTACK, "It prevents the target from using its held item. Its Trainer is also prevented from using items on it.", 15, Type.DARK, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.EMBARGO_EFFECT, EffectType.POKEMON));
		}
	}

	private static class NaturePower extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public NaturePower()
		{
			super(Namesies.NATURE_POWER_ATTACK, "An attack that makes use of nature's power. Its effects vary depending on the user's environment.", 20, Type.NORMAL, Category.STATUS);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return Attack.getAttack(Namesies.TRI_ATTACK_ATTACK).getAccuracy(b, me, o);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			// TODO: Battle environments
			me.callNewMove(b, o, new Move(Attack.getAttack(Namesies.TRI_ATTACK_ATTACK)));
		}
	}

	private static class Entrainment extends Attack implements ChangeAbilityMove
	{
		private static final long serialVersionUID = 1L;

		public Entrainment()
		{
			super(Namesies.ENTRAINMENT_ATTACK, "The user dances with an odd rhythm that compels the target to mimic it, making the target's Ability the same as the user's.", 15, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.CHANGE_ABILITY_EFFECT, EffectType.POKEMON));
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			switch (me.getAbility().namesies())
			{
				case TRUANT_ABILITY:
				case MULTITYPE_ABILITY:
				case STANCE_CHANGE_ABILITY:
				case ILLUSION_ABILITY:
				case TRACE_ABILITY:
				case IMPOSTER_ABILITY:
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
				default:
				super.apply(me, o, b);
			}
		}

		public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return caster.getAbility().newInstance();
		}

		public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return victim.getName() + " copied " + caster.getName() + "'s " + caster.getAbility().getName() + "!";
		}
	}

	private static class MagicRoom extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public MagicRoom()
		{
			super(Namesies.MAGIC_ROOM_ATTACK, "The user creates a bizarre area in which Pok\u00e9mon's held items lose their effects for five turns.", 10, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.MAGIC_ROOM_EFFECT, EffectType.BATTLE));
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return true;
		}
	}

	private static class WorrySeed extends Attack implements ChangeAbilityMove
	{
		private static final long serialVersionUID = 1L;

		public WorrySeed()
		{
			super(Namesies.WORRY_SEED_ATTACK, "A seed that causes worry is planted on the target. It prevents sleep by making its Ability Insomnia.", 10, Type.GRASS, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.CHANGE_ABILITY_EFFECT, EffectType.POKEMON));
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			switch (o.getAbility().namesies())
			{
				case TRUANT_ABILITY:
				case MULTITYPE_ABILITY:
				case STANCE_CHANGE_ABILITY:
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
				default:
				super.apply(me, o, b);
			}
		}

		public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return Ability.getAbility(Namesies.INSOMNIA_ABILITY).newInstance();
		}

		public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return victim.getName() + "'s ability was changed to " + Namesies.INSOMNIA_ABILITY.getName() + "!";
		}
	}

	private static class SimpleBeam extends Attack implements ChangeAbilityMove
	{
		private static final long serialVersionUID = 1L;

		public SimpleBeam()
		{
			super(Namesies.SIMPLE_BEAM_ATTACK, "The user's mysterious psychic wave changes the target's Ability to Simple.", 15, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.CHANGE_ABILITY_EFFECT, EffectType.POKEMON));
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			switch (o.getAbility().namesies())
			{
				case TRUANT_ABILITY:
				case MULTITYPE_ABILITY:
				case STANCE_CHANGE_ABILITY:
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
				default:
				super.apply(me, o, b);
			}
		}

		public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return Ability.getAbility(Namesies.SIMPLE_ABILITY).newInstance();
		}

		public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return victim.getName() + "'s ability was changed to " + Namesies.SIMPLE_ABILITY.getName() + "!";
		}
	}

	private static class SkillSwap extends Attack implements ChangeAbilityMove
	{
		private static final long serialVersionUID = 1L;
		private Ability ability;
		
		private static boolean canSkillSwap(ActivePokemon p)
		{
			switch (p.getAbility().namesies())
			{
				case WONDER_GUARD_ABILITY:
				case MULTITYPE_ABILITY:
				case ILLUSION_ABILITY:
				case STANCE_CHANGE_ABILITY:
				return false;
				default:
				return true;
			}
		}

		public SkillSwap()
		{
			super(Namesies.SKILL_SWAP_ATTACK, "The user employs its psychic power to exchange Abilities with the target.", 10, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.CHANGE_ABILITY_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (!canSkillSwap(user) || !canSkillSwap(victim))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			ability = user.getAbility();
			Ability temp = victim.getAbility();
			super.effects.get(0).cast(b, user, victim, CastSource.ATTACK, super.printCast);
			ability = temp;
			super.effects.get(0).cast(b, user, user, CastSource.ATTACK, super.printCast);
		}

		public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return ability;
		}

		public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return victim.getName() + "'s ability was changed to " + ability.getName() + "!";
		}
	}

	private static class VoltSwitch extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public VoltSwitch()
		{
			super(Namesies.VOLT_SWITCH_ATTACK, "After making its attack, the user rushes back to switch places with a party Pok\u00e9mon in waiting.", 20, Type.ELECTRIC, Category.SPECIAL);
			super.power = 70;
			super.accuracy = 100;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			// First execute the move as normal
			super.apply(me, o, b);
			
			Team t = b.getTrainer(me.user());
			if (t instanceof WildPokemon)
			{
				// End the battle against a wild Pokemon
				b.addMessage(me.getName() + " left the battle!");
				b.addMessage(" ", MessageUpdate.Update.EXIT_BATTLE);
				return;
			}
			
			Trainer trainer = (Trainer)t;
			if (!trainer.hasRemainingPokemon())
			{
				// Don't switch if no one to switch to
				return;
			}
			
			// Send this Pokemon back to the trainer and send out the next one
			b.addMessage(me.getName() + " went back to " + trainer.getName() + "!");
			trainer.switchToRandom(); // TODO: Prompt a legit switch fo user
			me = trainer.front();
			b.enterBattle(me, trainer.getName() + " sent out " + me.getName() + "!");
		}
	}

	private static class RelicSong extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public RelicSong()
		{
			super(Namesies.RELIC_SONG_ATTACK, "The user sings an ancient song and attacks by appealing to the hearts of those listening. It may also induce sleep.", 10, Type.NORMAL, Category.SPECIAL);
			super.power = 75;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.ASLEEP;
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.SOUND_BASED);
		}
	}

	private static class Snarl extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Snarl()
		{
			super(Namesies.SNARL_ATTACK, "The user yells as if it is ranting about something, making the target's Sp. Atk stat decrease.", 15, Type.DARK, Category.SPECIAL);
			super.power = 55;
			super.accuracy = 95;
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.statChanges[Stat.SP_ATTACK.index()] = -1;
		}
	}

	private static class IceBurn extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;

		public IceBurn()
		{
			super(Namesies.ICE_BURN_ATTACK, "On the second turn, an ultracold, freezing wind surrounds the target. This may leave the target with a burn.", 5, Type.ICE, Category.SPECIAL);
			super.power = 140;
			super.accuracy = 90;
			super.effectChance = 30;
			super.status = StatusCondition.BURNED;
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
		}

		public boolean chargesFirst()
		{
			return true;
		}

		public boolean semiInvulnerability()
		{
			return false;
		}

		public void charge(ActivePokemon user, Battle b)
		{
			b.addMessage(getChargeMessage(user));
		}

		public String getChargeMessage(ActivePokemon user)
		{
			return user.getName() + " is charging!";
		}
	}

	private static class VCreate extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public VCreate()
		{
			super(Namesies.VCREATE_ATTACK, "With a hot flame on its forehead, the user hurls itself at its target. It lowers the user's Defense, Sp. Def, and Speed stats.", 5, Type.FIRE, Category.PHYSICAL);
			super.power = 180;
			super.accuracy = 95;
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
			super.statChanges[Stat.SPEED.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Surf extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Surf()
		{
			super(Namesies.SURF_ATTACK, "It swamps the area around the user with a giant wave. It can also be used for crossing water.", 15, Type.WATER, Category.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
		}
	}

	private static class VoltTackle extends Attack implements RecoilMove
	{
		private static final long serialVersionUID = 1L;

		public VoltTackle()
		{
			super(Namesies.VOLT_TACKLE_ATTACK, "The user electrifies itself, then charges. It causes considerable damage to the user and may leave the target with paralysis.", 15, Type.ELECTRIC, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyRecoil(Battle b, ActivePokemon user, Integer damage)
		{
			if (user.hasAbility(Namesies.ROCK_HEAD_ABILITY) || user.hasAbility(Namesies.MAGIC_GUARD_ABILITY))
			{
				return;
			}
			
			b.addMessage(user.getName() + " was hurt by recoil!");
			user.reduceHealth(b, (int)Math.ceil(damage/3.0));
		}
	}

	private static class FocusBlast extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FocusBlast()
		{
			super(Namesies.FOCUS_BLAST_ATTACK, "The user heightens its mental focus and unleashes its power. It may also lower the target's Sp. Def.", 5, Type.FIGHTING, Category.SPECIAL);
			super.power = 120;
			super.accuracy = 70;
			super.effectChance = 10;
			super.moveTypes.add(MoveType.BOMB_BALL);
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
		}
	}

	private static class DiamondStorm extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DiamondStorm()
		{
			super(Namesies.DIAMOND_STORM_ATTACK, "The user whips up a storm of diamonds to damage opposing Pok\u00e9mon. This may also raise the user's Defense stat.", 5, Type.ROCK, Category.PHYSICAL);
			super.power = 100;
			super.accuracy = 95;
			super.effectChance = 50;
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Moonblast extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Moonblast()
		{
			super(Namesies.MOONBLAST_ATTACK, "Borrowing the power of the moon, the user attacks the target. This may also lower the target's Sp. Atk stat.", 15, Type.FAIRY, Category.PHYSICAL);
			super.power = 95;
			super.accuracy = 100;
			super.effectChance = 30;
			super.statChanges[Stat.SP_ATTACK.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class LandsWrath extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public LandsWrath()
		{
			super(Namesies.LANDS_WRATH_ATTACK, "The user gathers the energy of the land and focuses that power on opposing Pok\u00e9mon to damage them.", 10, Type.GROUND, Category.PHYSICAL);
			super.power = 90;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class PhantomForce extends Attack implements MultiTurnMove, AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public PhantomForce()
		{
			super(Namesies.PHANTOM_FORCE_ATTACK, "The user vanishes somewhere, then strikes the target on the next turn. This move hits even if the target protects itself.", 10, Type.GHOST, Category.PHYSICAL);
			super.power = 90;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(o.hasEffect(Namesies.USED_MINIMIZE_EFFECT) ? 2 : 1);
		}

		public boolean chargesFirst()
		{
			return true;
		}

		public boolean semiInvulnerability()
		{
			return true;
		}

		public void charge(ActivePokemon user, Battle b)
		{
			b.addMessage(getChargeMessage(user));
		}

		public String getChargeMessage(ActivePokemon user)
		{
			return user.getName() + " vanished suddenly!";
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return !defending.isSemiInvulnerable() && defending.hasEffect(Namesies.USED_MINIMIZE_EFFECT);
		}
	}

	private static class OblivionWing extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public OblivionWing()
		{
			super(Namesies.OBLIVION_WING_ATTACK, "The user absorbs its target's HP. The user's HP is restored by over half of the damage taken by the target.", 10, Type.FLYING, Category.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SAP_HEALTH);
			super.moveTypes.add(MoveType.SAP_75);
		}
	}

	private static class Geomancy extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;

		public Geomancy()
		{
			super(Namesies.GEOMANCY_ATTACK, "The user absorbs energy and sharply raises its Sp. Atk, Sp. Def, and Speed stats on the next turn.", 10, Type.FAIRY, Category.STATUS);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = 2;
			super.statChanges[Stat.SP_DEFENSE.index()] = 2;
			super.statChanges[Stat.SPEED.index()] = 2;
		}

		public boolean chargesFirst()
		{
			return true;
		}

		public boolean semiInvulnerability()
		{
			return false;
		}

		public void charge(ActivePokemon user, Battle b)
		{
			b.addMessage(getChargeMessage(user));
		}

		public String getChargeMessage(ActivePokemon user)
		{
			return user.getName() + " is absorbing power!";
		}
	}

	private static class Boomburst extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Boomburst()
		{
			super(Namesies.BOOMBURST_ATTACK, "The user attacks everything around it with the destructive power of a terrible, explosive sound.", 10, Type.NORMAL, Category.SPECIAL);
			super.power = 140;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SOUND_BASED);
		}
	}

	private static class PlayRough extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PlayRough()
		{
			super(Namesies.PLAY_ROUGH_ATTACK, "The user plays rough with the target and attacks it. This may also lower the target's Attack stat.", 10, Type.FAIRY, Category.PHYSICAL);
			super.power = 90;
			super.accuracy = 90;
			super.effectChance = 10;
			super.statChanges[Stat.ATTACK.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class CraftyShield extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public CraftyShield()
		{
			super(Namesies.CRAFTY_SHIELD_ATTACK, "The user protects itself and its allies from status moves with a mysterious power. This does not stop moves that do damage.", 10, Type.FAIRY, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.CRAFTY_SHIELD_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.SUCCESSIVE_DECAY);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
			super.priority = 4;
		}
	}

	private static class Nuzzle extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Nuzzle()
		{
			super(Namesies.NUZZLE_ATTACK, "The user attacks by nuzzling its electrified cheeks against the target. This also leaves the target with paralysis.", 20, Type.ELECTRIC, Category.PHYSICAL);
			super.power = 20;
			super.accuracy = 100;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class DrainingKiss extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DrainingKiss()
		{
			super(Namesies.DRAINING_KISS_ATTACK, "The user steals the target's energy with a kiss. The user's HP is restored by over half of the damage taken by the target.", 10, Type.FAIRY, Category.SPECIAL);
			super.power = 50;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SAP_HEALTH);
			super.moveTypes.add(MoveType.SAP_75);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class FairyWind extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FairyWind()
		{
			super(Namesies.FAIRY_WIND_ATTACK, "The user stirs up a fairy wind and strikes the target with it.", 30, Type.FAIRY, Category.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
		}
	}

	private static class ParabolicCharge extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public ParabolicCharge()
		{
			super(Namesies.PARABOLIC_CHARGE_ATTACK, "The user attacks everything around it. The user's HP is restored by half the damage taken by those hit.", 20, Type.ELECTRIC, Category.SPECIAL);
			super.power = 50;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SAP_HEALTH);
		}
	}

	private static class DisarmingVoice extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DisarmingVoice()
		{
			super(Namesies.DISARMING_VOICE_ATTACK, "Letting out a charming cry, the user does emotional damage to opposing Pok\u00e9mon. This attack never misses.", 15, Type.FAIRY, Category.SPECIAL);
			super.power = 40;
			super.moveTypes.add(MoveType.SOUND_BASED);
		}
	}

	private static class FreezeDry extends Attack implements AdvantageMultiplier
	{
		private static final long serialVersionUID = 1L;

		public FreezeDry()
		{
			super(Namesies.FREEZE_DRY_ATTACK, "The user rapidly cools the target. This may also leave the target frozen. This move is super effective on Water types.", 20, Type.ICE, Category.SPECIAL);
			super.power = 70;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.FROZEN;
		}

		public double multiplyAdvantage(Type moveType, Type[] defendingType)
		{
			double multiplier = 1;
			for (int i = 0; i < 2; i++)
			{
				if (defendingType[i] == Type.WATER)
				{
					multiplier *= 2/Type.getBasicAdvantage(moveType, defendingType[i]);
				}
			}
			
			return multiplier;
		}
	}

	private static class FlyingPress extends Attack implements AdvantageMultiplier
	{
		private static final long serialVersionUID = 1L;

		public FlyingPress()
		{
			super(Namesies.FLYING_PRESS_ATTACK, "The user dives down onto the target from the sky. This move is Fighting and Flying type simultaneously.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 95;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public double multiplyAdvantage(Type moveType, Type[] defendingType)
		{
			return Type.getBasicAdvantage(Type.FLYING, defendingType[0])*Type.getBasicAdvantage(Type.FLYING, defendingType[1]);
		}
	}

	private static class TopsyTurvy extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public TopsyTurvy()
		{
			super(Namesies.TOPSY_TURVY_ATTACK, "All stat changes affecting the target turn topsy-turvy and become the opposite of what they were.", 20, Type.DARK, Category.STATUS);
			super.accuracy = 100;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			for (int i = 0; i < Stat.NUM_BATTLE_STATS; i++)
			{
				victim.getAttributes().setStage(i, -victim.getStage(i));
			}
			
			b.addMessage(victim.getName() + "'s stat changes were all reversed!");
		}
	}

	private static class PlayNice extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PlayNice()
		{
			super(Namesies.PLAY_NICE_ATTACK, "The user and the target become friends, and the target loses its will to fight. This lowers the target's Attack stat.", 20, Type.NORMAL, Category.STATUS);
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
			super.statChanges[Stat.ATTACK.index()] = -1;
		}
	}

	private static class EerieImpulse extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public EerieImpulse()
		{
			super(Namesies.EERIE_IMPULSE_ATTACK, "The user's body generates an eerie impulse. Exposing the target to it harshly lowers the target's Sp. Atk stat.", 15, Type.ELECTRIC, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.SP_ATTACK.index()] = -2;
		}
	}

	private static class MistyTerrain extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public MistyTerrain()
		{
			super(Namesies.MISTY_TERRAIN_ATTACK, "The user covers the ground under everyone's feet with mist for five turns. This protects Pok\u00e9mon on the ground from status conditions.", 10, Type.FAIRY, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.MISTY_TERRAIN_EFFECT, EffectType.BATTLE));
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return true;
		}
	}

	private static class FairyLock extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FairyLock()
		{
			super(Namesies.FAIRY_LOCK_ATTACK, "By locking down the battlefield, the user keeps all Pok\u00e9mon from fleeing during the next turn.", 10, Type.FAIRY, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.FAIRY_LOCK_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}
	}

	private static class AromaticMist extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public AromaticMist()
		{
			super(Namesies.AROMATIC_MIST_ATTACK, "The user its Sp. Def stat with a mysterious aroma.", 20, Type.FAIRY, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
		}
	}

	private static class BabyDollEyes extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public BabyDollEyes()
		{
			super(Namesies.BABY_DOLL_EYES_ATTACK, "The user stares at the target with its baby-doll eyes, which lowers its Attack stat. This move always goes first.", 30, Type.FAIRY, Category.STATUS);
			super.accuracy = 100;
			super.priority = 1;
			super.statChanges[Stat.ATTACK.index()] = -1;
		}
	}

	private static class PetalBlizzard extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PetalBlizzard()
		{
			super(Namesies.PETAL_BLIZZARD_ATTACK, "The user stirs up a violent petal blizzard and attacks everything around it.", 15, Type.GRASS, Category.PHYSICAL);
			super.power = 90;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class GrassyTerrain extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public GrassyTerrain()
		{
			super(Namesies.GRASSY_TERRAIN_ATTACK, "The user turns the ground under everyone's feet to grass for five turns. This restores the HP of Pok\u00e9mon on the ground a little every turn.", 10, Type.GRASS, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.GRASSY_TERRAIN_EFFECT, EffectType.BATTLE));
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return true;
		}
	}

	private static class FlowerShield extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public FlowerShield()
		{
			super(Namesies.FLOWER_SHIELD_ATTACK, "The user raises its Defense stat with a mysterious power.", 10, Type.FAIRY, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 1;
		}
	}

	private static class NobleRoar extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public NobleRoar()
		{
			super(Namesies.NOBLE_ROAR_ATTACK, "Letting out a noble roar, the user intimidates the target and lowers its Attack and Sp. Atk stats.", 30, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.statChanges[Stat.ATTACK.index()] = -1;
			super.statChanges[Stat.SP_ATTACK.index()] = -1;
		}
	}

	private static class Powder extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Powder()
		{
			super(Namesies.POWDER_ATTACK, "The user covers the target in a powder that explodes and damages the target if it uses a Fire-type move.", 20, Type.BUG, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.POWDER_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.POWDER);
			super.priority = 1;
		}
	}

	private static class Rototiller extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Rototiller()
		{
			super(Namesies.ROTOTILLER_ATTACK, "Tilling the soil, the user makes it easier for plants to grow. This raises its Attack and Sp. Atk stats.", 10, Type.GROUND, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
		}
	}

	private static class WaterShuriken extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public WaterShuriken()
		{
			super(Namesies.WATER_SHURIKEN_ATTACK, "The user hits the target with throwing stars two to five times in a row. This move always goes first.", 20, Type.WATER, Category.PHYSICAL);
			super.power = 15;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int minHits = 2;
			int maxHits = 5;
			
			int hits = (int)(Math.random()*(maxHits - minHits + 1)) + minHits;
			
			if (maxHits == 5 && me.hasAbility(Namesies.SKILL_LINK_ABILITY))
			{
				hits = 5;
			}
			
			int hit = 1;
			for (; hit <= hits; hit++)
			{
				b.addMessage("Hit " + hit + "!");
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b))
				{
					break;
				}
			}
			
			// Print hits and gtfo
			b.addMessage("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!");
		}
	}

	private static class MatBlock extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MatBlock()
		{
			super(Namesies.MAT_BLOCK_ATTACK, "Using a pulled-up mat as a shield, the user protects itself and its allies from damaging moves. This does not stop status moves.", 15, Type.FIGHTING, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.MAT_BLOCK_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.selfTarget = true;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (!me.getAttributes().isFirstTurn())
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			super.apply(me, o, b);
		}
	}

	private static class MysticalFire extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MysticalFire()
		{
			super(Namesies.MYSTICAL_FIRE_ATTACK, "The user attacks by breathing a special, hot fire. This also lowers the target's Sp. Atk stat.", 10, Type.FIRE, Category.SPECIAL);
			super.power = 65;
			super.accuracy = 100;
			super.statChanges[Stat.SP_ATTACK.index()] = -1;
		}
	}

	private static class Infestation extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Infestation()
		{
			super(Namesies.INFESTATION_ATTACK, "The target is infested and attacked for four to five turns. The target can't flee during this time.", 20, Type.BUG, Category.SPECIAL);
			super.power = 20;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect(Namesies.INFESTATION_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Electrify extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Electrify()
		{
			super(Namesies.ELECTRIFY_ATTACK, "If the target is electrified before it uses a move during that turn, the target's move becomes Electric type.", 20, Type.ELECTRIC, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.ELECTRIFIED_EFFECT, EffectType.POKEMON));
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.priority = 1;
		}
	}

	private static class FellStinger extends Attack implements FaintEffect
	{
		private static final long serialVersionUID = 1L;

		public FellStinger()
		{
			super(Namesies.FELL_STINGER_ATTACK, "When the user knocks out a target with this move, the user's Attack stat rises sharply.", 25, Type.BUG, Category.PHYSICAL);
			super.power = 30;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void deathwish(Battle b, ActivePokemon dead, ActivePokemon murderer)
		{
			murderer.getAttributes().modifyStage(murderer, murderer, 2, Stat.ATTACK, b, CastSource.ATTACK);
		}
	}

	private static class MagneticFlux extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public MagneticFlux()
		{
			super(Namesies.MAGNETIC_FLUX_ATTACK, "The user manipulates magnetic fields which raises its Defense and Sp. Def stats.", 20, Type.ELECTRIC, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
		}
	}

	private static class StickyWeb extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public StickyWeb()
		{
			super(Namesies.STICKY_WEB_ATTACK, "The user weaves a sticky net around the opposing team, which lowers their Speed stat upon switching into battle.", 20, Type.BUG, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.STICKY_WEB_EFFECT, EffectType.TEAM));
			super.moveTypes.add(MoveType.FIELD);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return true;
		}
	}

	private static class Belch extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Belch()
		{
			super(Namesies.BELCH_ATTACK, "The user lets out a damaging belch on the target. The user must eat a Berry to use this move.", 10, Type.POISON, Category.SPECIAL);
			super.power = 120;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.ASSISTLESS);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (!me.hasEffect(Namesies.EATEN_BERRY_EFFECT))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			super.apply(me, o, b);
		}
	}

	private static class VenomDrench extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public VenomDrench()
		{
			super(Namesies.VENOM_DRENCH_ATTACK, "Opposing Pok\u00e9mon are drenched in an odd poisonous liquid. This lowers the Attack, Sp. Atk, and Speed stats of a poisoned target.", 20, Type.POISON, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.ATTACK.index()] = -1;
			super.statChanges[Stat.SP_ATTACK.index()] = -1;
			super.statChanges[Stat.SPEED.index()] = -1;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (!o.hasStatus(StatusCondition.POISONED))
			{
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return;
			}
			
			super.apply(me, o, b);
		}
	}

	private static class ElectricTerrain extends Attack implements AccuracyBypassEffect
	{
		private static final long serialVersionUID = 1L;

		public ElectricTerrain()
		{
			super(Namesies.ELECTRIC_TERRAIN_ATTACK, "The user electrifies the ground under everyone's feet for five turns. Pok\u00e9mon on the ground no longer fall asleep.", 10, Type.ELECTRIC, Category.STATUS);
			super.effects.add(Effect.getEffect(Namesies.ELECTRIC_TERRAIN_EFFECT, EffectType.BATTLE));
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending)
		{
			return true;
		}
	}

	private static class PowerUpPunch extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public PowerUpPunch()
		{
			super(Namesies.POWER_UP_PUNCH_ATTACK, "Striking opponents over and over makes the user's fists harder. Hitting a target raises the Attack stat.", 20, Type.FIGHTING, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PUNCHING);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class Confide extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Confide()
		{
			super(Namesies.CONFIDE_ATTACK, "The user tells the target a secret, and the target loses its ability to concentrate. This lowers the target's Sp. Atk stat.", 20, Type.NORMAL, Category.STATUS);
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.statChanges[Stat.SP_ATTACK.index()] = -1;
		}
	}

	private static class Cut extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Cut()
		{
			super(Namesies.CUT_ATTACK, "The target is cut with a scythe or a claw. It can also be used to cut down thin trees.", 30, Type.NORMAL, Category.PHYSICAL);
			super.power = 50;
			super.accuracy = 95;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	private static class DazzlingGleam extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public DazzlingGleam()
		{
			super(Namesies.DAZZLING_GLEAM_ATTACK, "The user damages opposing Pok\u00e9mon by emitting a powerful flash.", 10, Type.FAIRY, Category.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
		}
	}

	private static class Strength extends Attack 
	{
		private static final long serialVersionUID = 1L;

		public Strength()
		{
			super(Namesies.STRENGTH_ATTACK, "The target is slugged with a punch thrown at maximum power. It can also be used to move heavy boulders.", 15, Type.NORMAL, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}
}
