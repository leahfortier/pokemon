package battle;

import item.Item;
import item.berry.Berry;
import item.berry.GainableEffectBerry;
import item.hold.DriveItem;
import item.hold.HoldItem;
import item.hold.PlateItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import main.Global;
import main.Type;
import pokemon.Ability;
import pokemon.ActivePokemon;
import pokemon.Gender;
import pokemon.Stat;
import trainer.Team;
import trainer.Trainer;
import trainer.Trainer.Action;
import trainer.WildPokemon;
import battle.effect.ApplyDamageEffect;
import battle.effect.ChangeAbilityMove;
import battle.effect.ChangeTypeMove;
import battle.effect.CrashDamageMove;
import battle.effect.DefogRelease;
import battle.effect.Effect;
import battle.effect.Effect.CastSource;
import battle.effect.Effect.EffectType;
import battle.effect.EffectBlockerEffect;
import battle.effect.ItemCondition;
import battle.effect.MultiTurnMove;
import battle.effect.PassableEffect;
import battle.effect.PhysicalContactEffect;
import battle.effect.PokemonEffect;
import battle.effect.RapidSpinRelease;
import battle.effect.RecoilMove;
import battle.effect.SelfHealingMove;
import battle.effect.StageChangingEffect;
import battle.effect.StatSwitchingEffect;
import battle.effect.Status;
import battle.effect.Status.StatusCondition;
import battle.effect.TakeDamageEffect;
import battle.effect.TeamEffect;
import battle.effect.Weather;

public abstract class Attack implements Serializable 
{
	private static final long serialVersionUID = 1L;
	
	private static HashMap<String, Attack> map; // Mappity map
	private static List<String> moveNames;
	private static HashSet<String> validMoveTypes;

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
	protected List<String> moveTypes;
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
			name = name().charAt(0)+name().substring(1).toLowerCase();
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

	public Attack(String s, String d, int p, Type t, Category cat) 
	{
		name = s;
		description = d;
		pp = p;
		type = t;
		category = cat;
		effects = new ArrayList<Effect>();
		moveTypes = new ArrayList<String>();
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
		if (category == Category.STATUS) return false;
		if (effectChance < 100) return true;
		if (status != StatusCondition.NONE) return true;
		for (Effect e : effects)
		{
			if (e.getName().equals("Confusion")) return true;
			if (e.getName().equals("Flinch")) return true;
		}
		for (int val : statChanges)
		{
			if (val < 0)
			{
				if (selfTarget) continue;
				return true;
			}
			if (val > 0) return true;
		}
		return false;
	}
	
	public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o)
	{
		return accuracy;
	}
	
	public String getAccuracyString()
	{
		if (accuracy > 100) return "--";
		
		return accuracy+"";
	}
	
	public Category getCategory()
	{
		return category;
	}
	
	public int getPP()
	{
		return pp;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public boolean isType(Battle b, ActivePokemon user, Type t)
	{
		return getType(b, user) == type;
	}
	
	public boolean isMoveType(String type)
	{
		validMoveType(type);
		for (String s : moveTypes)
		{
			if (s.equals(type)) return true;
		}
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
	
	public Type getType(Battle b, ActivePokemon user)	
	{
		if (user.hasAbility("Normalize")) return Type.NORMAL;
		return type;
	}
	
	public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
	{
		return power;
	}
	
	public String getPowerString()
	{
		if (power == 0) return "--";
		
		return power+"";
	}
	
	public void apply(ActivePokemon me, ActivePokemon o, Battle b)
	{
		boolean applyEffects = canApplyEffects(b, me, o);
		ActivePokemon target = getTarget(b, me, o);
		
		if (category != Category.STATUS) applyEffects = applyDamage(me, o, b) != -1 && applyEffects;
		if (applyEffects) applyEffects(b, me, target);
	}
	
	private ActivePokemon getTarget(Battle b, ActivePokemon me, ActivePokemon o)
	{
		if (selfTarget)
		{
			// Snatch steals self-target moves
			if (category == Category.STATUS && !isMoveType("NonSnatchable") && o.hasEffect("Snatch"))
			{
				b.addMessage(o.getName()+" snatched "+me.getName()+"'s move!");
				return o;
			}
			return me;
		}
		
		// Magic Coat and Magic Bounce reflects effects back to the user
		if (category == Category.STATUS && !isMoveType("Field") && !isMoveType("NoMagicCoat"))
		{
			String bouncy = "";
			if (o.hasEffect("MagicCoat")) bouncy = "Coat";
			else if (o.hasAbility("Magic Bounce") && !me.breaksTheMold()) bouncy = "Bounce";
			if (bouncy.length() > 0)
			{
				b.addMessage(o.getName()+"'s Magic "+bouncy+" reflected "+me.getName()+"'s move!");
				return me;
			}
		}
		
		return o;
	}
	
	private boolean canApplyEffects(Battle b, ActivePokemon me, ActivePokemon o)
	{
		int chance = effectChance*(me.hasAbility("Serene Grace") ? 2 : 1); 
		if (Math.random()*100 >= chance) return false;
		
		// Check the opponents effects and see if it will prevent effects from occuring
		List<Object> list = b.getEffectsList(o);
		for (Object obj : list)
		{
			if (obj instanceof Effect && !((Effect)obj).isActive()) continue;
			if (obj instanceof EffectBlockerEffect && !((EffectBlockerEffect)obj).validMove(b, me, o)) return false;
		}
		
		// Sheer Force prevents the user from having secondary effects for its moves
		if (me.hasAbility("Sheer Force") && me.getAttack().hasSecondaryEffects()) return false;
		
		return true;
	}
	
	private boolean zeroAdvantage(Battle b, ActivePokemon p, ActivePokemon opp)
	{
		if (Type.getAdvantage(p.getAttack().getType(b, p), opp, b) > 0) return false;
		
		b.addMessage("It doesn't affect "+opp.getName()+"!");
		if (p.getAttack() instanceof CrashDamageMove) ((CrashDamageMove)p.getAttack()).crash(b, p);
		return true;
	}
	
	// Physical and Special moves -- do dat damage!
	// Returns false if the move was unsuccessful due to zero type advantage
	public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
	{
		if (zeroAdvantage(b, me, o)) return -1;
		
		// Print Advantage
		double adv = Type.getAdvantage(me.getAttack().getType(b, me), o, b);
		if (adv < 1) b.addMessage("It's not very effective...");
		else if (adv > 1) b.addMessage("It's super effective!");
		
		// Deal damage
		int damage = b.applyDamage(o, b.damageCalc(me, o));
		
		// Check if target is fainted
		o.isFainted(b);
		
		Ability userAbility = me.getAbility(), oppAbility = o.getAbility();
		Item userItem = me.getHeldItem(b), oppItem = o.getHeldItem(b);
		
		// Apply a damage effect
		if (userAbility instanceof ApplyDamageEffect) ((ApplyDamageEffect)userAbility).applyEffect(b, me, o, damage);
		if (userItem instanceof ApplyDamageEffect) ((ApplyDamageEffect)userItem).applyEffect(b, me, o, damage);
		
		// Take Recoil Damage
		if (this instanceof RecoilMove) ((RecoilMove)this).applyRecoil(b, me, damage);
	
		if (me.isFainted(b)) return damage;
		
		// Sap the Health
		if (isMoveType("SapHealth")) me.sapHealth(o, (int)Math.ceil(damage/2.0), b, true);
		
		// Abilities and items that apply when a Pokemon makes physical contact with them
		if (isMoveType("PhysicalContact"))
		{
			if (oppAbility instanceof PhysicalContactEffect) ((PhysicalContactEffect)oppAbility).contact(b, me, o);
			if (oppItem instanceof PhysicalContactEffect) ((PhysicalContactEffect)oppItem).contact(b, me, o);
		}
		
		// Effects that apply to the opponent when they take damage
		if (oppAbility instanceof TakeDamageEffect) ((TakeDamageEffect)oppAbility).takeDamage(b, me, o);
		if (oppItem instanceof TakeDamageEffect) ((TakeDamageEffect)oppItem).takeDamage(b, me, o);
		
		return damage;
	}
	
	public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
	{
		// Kill yourself!!
		if (isMoveType("UserFaints")) user.reduceHealthFraction(b, 1);
		
		// Don't apply effects to a fainted Pokemon
		if (victim.isFainted(b)) return;
		
		// Give Status Condition
		if (status != StatusCondition.NONE)
		{
			boolean success = Status.giveStatus(b, user, victim, status);
			if (!success && canPrintFail()) b.addMessage(Status.getFailMessage(b, user, victim, status));
		}
		
		// Give Stat Changes
		victim.modifyStages(b, user, statChanges, CastSource.ATTACK);
		
		// Give additional effects
		for (Effect e : effects)
		{
			if (e.applies(b, user, victim, CastSource.ATTACK)) e.cast(b, user, victim, CastSource.ATTACK, canPrintCast());
			else if (canPrintFail()) b.addMessage(e.getFailMessage(user, victim, victim.user()));
		}
		
		// Heal yourself!!
		if (this instanceof SelfHealingMove) ((SelfHealingMove)this).heal(user, victim, b);
	}
	
	public boolean canPrintFail()
	{
		return effectChance == 100 && category == Category.STATUS;
	}
	
	public boolean canPrintCast()
	{
		return printCast;
	}
	
	public void startTurn(Battle b, ActivePokemon me) {}

	public static Attack getAttack(String m)
	{
		if (map == null) loadMoves();
		if (map.containsKey(m)) return map.get(m);

		Global.error("No such Move "+m);
		return null;
	}
	
	public static boolean isAttack(String m)
	{
		if (map == null) loadMoves();
		if (map.containsKey(m)) return true;

		return false;
	}
	
	// Throws an error if the moveType is not valid
	public static void validMoveType(String moveType)
	{
		if (validMoveTypes == null)
		{
			validMoveTypes = new HashSet<>();
			Scanner in = Global.openFile("MoveTypes.txt");
			while (in.hasNext()) validMoveTypes.add(in.next());
			in.close();
		}
		if (!validMoveTypes.contains(moveType)) Global.error("Invalid MoveType "+moveType);
	}

	// Create and load the Moves map if it doesn't already exist
	public static void loadMoves() 
	{
		if (map != null) return;
		map = new HashMap<>();
		moveNames = new ArrayList<>();

		// EVERYTHING BELOW IS GENERATED +++

		// List all of the moves we are loading
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
		map.put("Sonicboom", new Sonicboom());
		map.put("Smokescreen", new Smokescreen());
		map.put("Take Down", new TakeDown());
		map.put("Struggle", new Struggle());
		map.put("Razor Leaf", new RazorLeaf());
		map.put("Sweet Scent", new SweetScent());
		map.put("Growth", new Growth());
		map.put("Double-edge", new DoubleEdge());
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
		map.put("Solarbeam", new Solarbeam());
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
		map.put("Rain Dance", new RainDance());
		map.put("Hydro Pump", new HydroPump());
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
		map.put("Sand-attack", new SandAttack());
		map.put("Quick Attack", new QuickAttack());
		map.put("Twister", new Twister());
		map.put("Featherdance", new Featherdance());
		map.put("Roost", new Roost());
		map.put("Thundershock", new Thundershock());
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
		map.put("Doubleslap", new Doubleslap());
		map.put("Wish", new Wish());
		map.put("Minimize", new Minimize());
		map.put("Wake-up Slap", new WakeUpSlap());
		map.put("Cosmic Power", new CosmicPower());
		map.put("Lucky Chant", new LuckyChant());
		map.put("Metronome", new Metronome());
		map.put("Gravity", new Gravity());
		map.put("Moonlight", new Moonlight());
		map.put("Stored Power", new StoredPower());
		map.put("Mimic", new Mimic());
		map.put("Meteor Mash", new MeteorMash());
		map.put("Imprison", new Imprison());
		map.put("Will-o-wisp", new WillOWisp());
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
		map.put("X-scissor", new XScissor());
		map.put("Foresight", new Foresight());
		map.put("Odor Sleuth", new OdorSleuth());
		map.put("Miracle Eye", new MiracleEye());
		map.put("Howl", new Howl());
		map.put("Signal Beam", new SignalBeam());
		map.put("Zen Headbutt", new ZenHeadbutt());
		map.put("Psychic", new Psychic());
		map.put("Mud-slap", new MudSlap());
		map.put("Magnitude", new Magnitude());
		map.put("Bulldoze", new Bulldoze());
		map.put("Dig", new Dig());
		map.put("Earthquake", new Earthquake());
		map.put("Fissure", new Fissure());
		map.put("Night Slash", new NightSlash());
		map.put("Tri Attack", new TriAttack());
		map.put("Fake Out", new FakeOut());
		map.put("Faint Attack", new FaintAttack());
		map.put("Taunt", new Taunt());
		map.put("Pay Day", new PayDay());
		map.put("Power Gem", new PowerGem());
		map.put("Water Sport", new WaterSport());
		map.put("Soak", new Soak());
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
		map.put("Extremespeed", new Extremespeed());
		map.put("Hypnosis", new Hypnosis());
		map.put("Bubblebeam", new Bubblebeam());
		map.put("Mud Shot", new MudShot());
		map.put("Belly Drum", new BellyDrum());
		map.put("Submission", new Submission());
		map.put("Dynamicpunch", new Dynamicpunch());
		map.put("Mind Reader", new MindReader());
		map.put("Lock-on", new LockOn());
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
		map.put("Dragonbreath", new Dragonbreath());
		map.put("Iron Tail", new IronTail());
		map.put("Meditate", new Meditate());
		map.put("Synchronoise", new Synchronoise());
		map.put("Psyshock", new Psyshock());
		map.put("Vicegrip", new Vicegrip());
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
		map.put("Wood Hammer", new WoodHamer());
		map.put("Bone Club", new BoneClub());
		map.put("Bonemerang", new Bonemerang());
		map.put("Bone Rush", new BoneRush());
		map.put("Rolling Kick", new RollingKick());
		map.put("Jump Kick", new JumpKick());
		map.put("Brick Break", new BrickBreak());
		map.put("Hi Jump Kick", new HiJumpKick());
		map.put("Blaze Kick", new BlazeKick());
		map.put("Mega Kick", new MegaKick());
		map.put("Comet Punch", new CometPunch());
		map.put("Mach Punch", new MachPunch());
		map.put("Bullet Punch", new BulletPunch());
		map.put("Vacuum Wave", new VacuumWave());
		map.put("Thunderpunch", new Thunderpunch());
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
		map.put("Softboiled", new Softboiled());
		map.put("Ancientpower", new Ancientpower());
		map.put("Tickle", new Tickle());
		map.put("Dizzy Punch", new DizzyPunch());
		map.put("Outrage", new Outrage());
		map.put("Dragon Dance", new DragonDance());
		map.put("Dragon Pulse", new DragonPulse());
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
		map.put("Grasswhistle", new Grasswhistle());
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
		map.put("Smellingsalt", new Smellingsalt());
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
		map.put("Selfdestruct", new Selfdestruct());
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
		map.put("U-turn", new UTurn());
		map.put("Baton Pass", new BatonPass());
		map.put("Perish Song", new PerishSong());
		map.put("Dragon Tail", new DragonTail());
		map.put("Foul Play", new FoulPlay());
		map.put("Embargo", new Embargo());
		map.put("Nature Power", new NaturePower());
		map.put("Entrainment", new Entrainment());
		map.put("Magic Room", new MagicRoom());
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

		for (String s : map.keySet()) moveNames.add(s);
	}

	/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

	private static class Tackle extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Tackle()
		{
			super("Tackle", "A physical attack in which the user charges and slams into the target with its whole body.", 35, Type.NORMAL, Category.PHYSICAL);
			super.power = 35;
			super.accuracy = 95;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class LeechSeed extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public LeechSeed()
		{
			super("Leech Seed", "A seed is planted on the target. It steals some HP from the target every turn.", 10, Type.GRASS, Category.STATUS);
			super.accuracy = 90;
			super.effects.add(Effect.getEffect("LeechSeed", EffectType.POKEMON));
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (Ability.blockDamage(b, user, victim)) return;
			else super.applyEffects(b, user, victim);
		}
	}

	private static class ThunderWave extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public ThunderWave()
		{
			super("Thunder Wave", "A weak electric charge is launched at the target. It causes paralysis if it hits.", 20, Type.ELECTRIC, Category.STATUS);
			super.accuracy = 100;
			super.status = StatusCondition.PARALYZED;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (super.zeroAdvantage(b, user, victim)) b.addMessage("It doesn't affect "+victim.getName()+"!");
			else if (Ability.blockDamage(b, user, victim)) return;
			else super.applyEffects(b, user, victim);
		}
	}

	private static class PoisonPowder extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public PoisonPowder()
		{
			super("Poison Powder", "The user scatters a cloud of poisonous dust on the target. It may poison the target.", 35, Type.POISON, Category.STATUS);
			super.accuracy = 75;
			super.status = StatusCondition.POISONED;
			super.moveTypes.add("Powder");
		}
	}

	private static class SleepPowder extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SleepPowder()
		{
			super("Sleep Powder", "The user scatters a big cloud of sleep-inducing dust around the target.", 15, Type.GRASS, Category.STATUS);
			super.accuracy = 75;
			super.status = StatusCondition.ASLEEP;
			super.moveTypes.add("Powder");
		}
	}

	private static class Toxic extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Toxic()
		{
			super("Toxic", "A move that leaves the target badly poisoned. Its poison damage worsens every turn.", 10, Type.POISON, Category.STATUS);
			super.effects.add(Effect.getEffect("BadPoison", EffectType.POKEMON));
			super.accuracy = 90;
		}
	}

	private static class Ember extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Ember()
		{
			super("Ember", "The target is attacked with small flames. It may also leave the target with a burn.", 25, Type.FIRE, Category.SPECIAL);
			super.accuracy = 100;
			super.status = StatusCondition.BURNED;
			super.effectChance = 10;
			super.power = 40;
		}
	}

	private static class Growl extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Growl()
		{
			super("Growl", "The user growls in an endearing way, making the opposing team less wary. The foes' Attack stats are lowered.", 40, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.ATTACK.index()] = -1;
			super.moveTypes.add("SoundBased");
		}
	}

	private static class Scratch extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Scratch()
		{
			super("Scratch", "Hard, pointed, and sharp claws rake the target to inflict damage.", 35, Type.NORMAL, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class VineWhip extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public VineWhip()
		{
			super("Vine Whip", "The target is struck with slender, whiplike vines to inflict damage.", 15, Type.GRASS, Category.PHYSICAL);
			super.power = 35;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Sonicboom extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Sonicboom()
		{
			super("Sonicboom", "The target is hit with a destructive shock wave that always inflicts 20 HP damage.", 20, Type.NORMAL, Category.SPECIAL);
			super.accuracy = 90;
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			return b.applyDamage(o, 20);
		}
	}

	private static class Smokescreen extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Smokescreen()
		{
			super("Smokescreen", "The user releases an obscuring cloud of smoke or ink. It reduces the target's accuracy.", 20, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	private static class TakeDown extends Attack implements RecoilMove
	{
		private static final long serialVersionUID = 1L;
		private int recoilDamage(ActivePokemon user, int damage)
		{
			return (int)Math.ceil(damage/4.0);
		}

		public TakeDown()
		{
			super("Take Down", "The user releases an obscuring cloud of smoke or ink. It reduces the target's accuracy.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 90;
			super.accuracy = 85;
			super.moveTypes.add("PhysicalContact");
		}

		public void applyRecoil(Battle b, ActivePokemon user, int damage)
		{
			if (user.hasAbility("Rock Head") || user.hasAbility("Magic Guard")) return;
			b.addMessage(user.getName()+" was hurt by recoil!");
			b.applyDamage(user, recoilDamage(user, damage));
		}
	}

	private static class Struggle extends Attack implements RecoilMove
	{
		private static final long serialVersionUID = 1L;
		private int recoilDamage(ActivePokemon user, int damage)
		{
			return user.getStat(Stat.HP)/4;
		}

		public Struggle()
		{
			super("Struggle", "An attack that is used in desperation only if the user has no PP. It also hurts the user slightly.", 1, Type.NONE, Category.PHYSICAL);
			super.power = 50;
			super.moveTypes.add("Encoreless");
			super.moveTypes.add("Mimicless");
			super.moveTypes.add("Assistless");
			super.moveTypes.add("Metronomeless");
			super.moveTypes.add("PhysicalContact");
		}

		public void applyRecoil(Battle b, ActivePokemon user, int damage)
		{
			b.addMessage(user.getName()+" was hurt by recoil!");
			b.applyDamage(user, recoilDamage(user, damage));
		}
	}

	private static class RazorLeaf extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public RazorLeaf()
		{
			super("Razor Leaf", "Sharp-edged leaves are launched to slash at the opposing team. Critical hits land more easily.", 25, Type.GRASS, Category.PHYSICAL);
			super.power = 55;
			super.accuracy = 95;
			super.moveTypes.add("HighCritRatio");
		}
	}

	private static class SweetScent extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SweetScent()
		{
			super("Sweet Scent", "A sweet scent that lowers the opposing team's evasiveness. It also lures wild Pok\u00e9mon if used in grass, etc.", 20, Type.NORMAL, Category.STATUS);
			super.statChanges[Stat.EVASION.index()] = -1;
			super.accuracy = 100;
		}
	}

	private static class Growth extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Growth()
		{
			super("Growth", "The user's body grows all at once, raising the Attack and Sp. Atk stats.", 40, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
		}
	}

	private static class DoubleEdge extends Attack implements RecoilMove
	{
		private static final long serialVersionUID = 1L;
		private int recoilDamage(ActivePokemon user, int damage)
		{
			return (int)Math.ceil(damage/3.0);
		}

		public DoubleEdge()
		{
			super("Double-edge", "A reckless, life-risking tackle. It also damages the user by a fairly large amount, however.", 15, Type.NORMAL, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public void applyRecoil(Battle b, ActivePokemon user, int damage)
		{
			if (user.hasAbility("Rock Head") || user.hasAbility("Magic Guard")) return;
			b.addMessage(user.getName()+" was hurt by recoil!");
			b.applyDamage(user, recoilDamage(user, damage));
		}
	}

	private static class SeedBomb extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SeedBomb()
		{
			super("Seed Bomb", "The user slams a barrage of hard-shelled seeds down on the target from above.", 15, Type.GRASS, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
		}
	}

	private static class Synthesis extends Attack implements SelfHealingMove
	{
		private static final long serialVersionUID = 1L;
		public Synthesis()
		{
			super("Synthesis", "The user restores its own HP. The amount of HP regained varies with the weather.", 5, Type.GRASS, Category.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect("HealBlock"))
			{
				b.addMessage("...but it failed!");
				return;
			}

			switch (b.getWeather().getType())
			{
				case CLEAR_SKIES:
				victim.healHealthFraction(1/2.0);
				break;
				case SUNNY:
				victim.healHealthFraction(2/3.0);
				break;
				case HAILING:
				case RAINING:
				case SANDSTORM:
				victim.healHealthFraction(1/4.0);
				break;
				default:
				Global.error("Funky weather problems!!!!");
				break;
			}
			b.addMessage(victim.getName()+"'s health was restored!", victim.getHP(), victim.user());
		}
	}

	private static class Recover extends Attack implements SelfHealingMove
	{
		private static final long serialVersionUID = 1L;
		public Recover()
		{
			super("Recover", "Restoring its own cells, the user restores its own HP by half of its max HP.", 10, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect("HealBlock"))
			{
				b.addMessage("...but it failed!");
				return;
			}

			victim.healHealthFraction(1/2.0);
			b.addMessage(victim.getName()+"'s health was restored!", victim.getHP(), victim.user());
		}
	}

	private static class DragonRage extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public DragonRage()
		{
			super("Dragon Rage", "This attack hits the target with a shock wave of pure rage. This attack always inflicts 40 HP damage.", 10, Type.DRAGON, Category.SPECIAL);
			super.accuracy = 100;
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			return b.applyDamage(o, 40);
		}
	}

	private static class ScaryFace extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public ScaryFace()
		{
			super("Scary Face", "The user frightens the target with a scary face to harshly reduce its Speed stat.", 10, Type.NORMAL, Category.STATUS);
			super.statChanges[Stat.SPEED.index()] = -2;
			super.accuracy = 100;
		}
	}

	private static class FireFang extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FireFang()
		{
			super("Fire Fang", "The user bites with flame-cloaked fangs. It may also make the target flinch or leave it burned.", 15, Type.FIRE, Category.PHYSICAL);
			super.power = 65;
			super.status = StatusCondition.BURNED;
			super.effectChance = 10;
			super.accuracy = 95;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.moveTypes.add("PhysicalContact");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (Math.random()*100 < 50) Status.giveStatus(b, user, victim, status);
			else if (effects.get(0).applies(b, user, victim, CastSource.ATTACK)) effects.get(0).cast(b, user, victim, CastSource.ATTACK, super.printCast);
		}
	}

	private static class FlameBurst extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FlameBurst()
		{
			super("Flame Burst", "The user attacks the target with a bursting flame.", 15, Type.FIRE, Category.SPECIAL);
			super.power = 70;
			super.accuracy = 100;
		}
	}

	private static class Bite extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Bite()
		{
			super("Bite", "The target is bitten with viciously sharp fangs. It may make the target flinch.", 25, Type.DARK, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.effectChance = 30;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Slash extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Slash()
		{
			super("Slash", "The target is attacked with a slash of claws or blades. Critical hits land more easily.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add("HighCritRatio");
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class TailWhip extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public TailWhip()
		{
			super("Tail Whip", "The user wags its tail cutely, making opposing Pok\u00e9mon less wary and lowering their Defense stat.", 30, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.DEFENSE.index()] = -1;
		}
	}

	private static class Solarbeam extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;
		public Solarbeam()
		{
			super("Solarbeam", "A two-turn attack. The user gathers light, then blasts a bundled beam on the second turn.", 10, Type.GRASS, Category.SPECIAL);
			super.power = 120;
			super.moveTypes.add("SleepTalkFail");
			super.accuracy = 100;
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			switch (b.getWeather().getType())
			{
				case SUNNY:
				case CLEAR_SKIES:
				return super.power;
				case HAILING:
				case RAINING:
				case SANDSTORM:
				return super.power/2;
				default:
				Global.error("Funky weather problems!");
				return -1;
			}
		}

		public boolean isMultiTurn(Battle b)
		{
			return b.getWeather().getType() != Weather.WeatherType.SUNNY;
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
			b.addMessage(user.getName()+" began taking in sunlight!");
		}
	}

	private static class Flamethrower extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Flamethrower()
		{
			super("Flamethrower", "The target is scorched with an intense blast of fire. It may also leave the target with a burn.", 15, Type.FIRE, Category.SPECIAL);
			super.power = 95;
			super.accuracy = 100;
			super.status = StatusCondition.BURNED;
			super.effectChance = 10;
		}
	}

	private static class Fly extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;
		public Fly()
		{
			super("Fly", "The user soars, then strikes its target on the second turn. It can also be used for flying to any familiar town.", 15, Type.FLYING, Category.PHYSICAL);
			super.power = 90;
			super.accuracy = 95;
			super.moveTypes.add("SleepTalkFail");
			super.moveTypes.add("Airborne");
			super.moveTypes.add("PhysicalContact");
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
			b.addMessage(user.getName()+" flew up high!");
		}
	}

	private static class FireSpin extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FireSpin()
		{
			super("Fire Spin", "The target becomes trapped within a fierce vortex of fire that rages for four to five turns.", 15, Type.FIRE, Category.SPECIAL);
			super.power = 35;
			super.accuracy = 85;
			super.effects.add(Effect.getEffect("FireSpin", EffectType.POKEMON));
		}
	}

	private static class Inferno extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Inferno()
		{
			super("Inferno", "The user attacks by engulfing the target in an intense fire. It leaves the target with a burn.", 5, Type.FIRE, Category.SPECIAL);
			super.accuracy = 50;
			super.power = 100;
			super.status = StatusCondition.BURNED;
		}
	}

	private static class DragonClaw extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public DragonClaw()
		{
			super("Dragon Claw", "The user slashes the target with huge, sharp claws.", 15, Type.DRAGON, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class ShadowClaw extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public ShadowClaw()
		{
			super("Shadow Claw", "The user slashes with a sharp claw made from shadows. Critical hits land more easily.", 15, Type.GHOST, Category.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add("HighCritRatio");
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class AirSlash extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public AirSlash()
		{
			super("Air Slash", "The user attacks with a blade of air that slices even the sky. It may also make the target flinch.", 20, Type.FLYING, Category.SPECIAL);
			super.power = 75;
			super.accuracy = 95;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.effectChance = 30;
		}
	}

	private static class WingAttack extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public WingAttack()
		{
			super("Wing Attack", "The target is struck with large, imposing wings spread wide to inflict damage.", 35, Type.FLYING, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class HeatWave extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public HeatWave()
		{
			super("Heat Wave", "The user attacks by exhaling hot breath on the opposing team. It may also leave targets with a burn.", 10, Type.FIRE, Category.SPECIAL);
			super.power = 100;
			super.accuracy = 90;
			super.status = StatusCondition.BURNED;
			super.effectChance = 10;
		}
	}

	private static class FlareBlitz extends Attack implements RecoilMove
	{
		private static final long serialVersionUID = 1L;
		private int recoilDamage(ActivePokemon user, int damage)
		{
			return (int)Math.ceil(damage/3.0);
		}

		public FlareBlitz()
		{
			super("Flare Blitz", "The user cloaks itself in fire and charges at the target. The user sustains serious damage and may leave the target burned.", 15, Type.FIRE, Category.PHYSICAL);
			super.status = StatusCondition.BURNED;
			super.effectChance = 10;
			super.power = 120;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public void applyRecoil(Battle b, ActivePokemon user, int damage)
		{
			if (user.hasAbility("Rock Head") || user.hasAbility("Magic Guard")) return;
			b.addMessage(user.getName()+" was hurt by recoil!");
			b.applyDamage(user, recoilDamage(user, damage));
		}
	}

	private static class FlashCannon extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FlashCannon()
		{
			super("Flash Cannon", "The user gathers all its light energy and releases it at once. It may also lower the target's Sp. Def stat.", 10, Type.STEEL, Category.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
			super.effectChance = 10;
		}
	}

	private static class Bubble extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Bubble()
		{
			super("Bubble", "A spray of countless bubbles is jetted at the opposing team. It may also lower the targets' Speed stats.", 30, Type.WATER, Category.SPECIAL);
			super.power = 20;
			super.accuracy = 100;
			super.statChanges[Stat.SPEED.index()] = -1;
			super.effectChance = 10;
		}
	}

	private static class Withdraw extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Withdraw()
		{
			super("Withdraw", "The user withdraws its body into its hard shell, raising its Defense stat.", 40, Type.WATER, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 1;
		}
	}

	private static class WaterGun extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public WaterGun()
		{
			super("Water Gun", "The target is blasted with a forceful shot of water.", 25, Type.WATER, Category.SPECIAL);
			super.accuracy = 100;
			super.power = 40;
		}
	}

	private static class RapidSpin extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public RapidSpin()
		{
			super("Rapid Spin", "A spin attack that can also eliminate such moves as Bind, Wrap, Leech Seed, and Spikes.", 40, Type.NORMAL, Category.PHYSICAL);
			super.power = 20;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			for (int i = 0; i < user.getEffects().size(); i++)
			{
				PokemonEffect e = user.getEffects().get(i);
				if (e.isActive() && e instanceof RapidSpinRelease)
				{
					b.addMessage(((RapidSpinRelease)e).getReleaseMessage(user));
					user.getEffects().remove(i--);
				}
			}
			for (int i = 0; i < b.getEffects(user.user()).size(); i++)
			{
				TeamEffect e = b.getEffects(user.user()).get(i);
				if (e.isActive() && e instanceof RapidSpinRelease)
				{
					b.addMessage(((RapidSpinRelease)e).getReleaseMessage(user));
					b.getEffects(user.user()).remove(i--);
				}
			}
		}
	}

	private static class Reflect extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Reflect()
		{
			super("Reflect", "A wondrous wall of light is put up to suppress damage from physical attacks for five turns.", 20, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect("Reflect", EffectType.TEAM));
			super.selfTarget = true;
		}
	}

	private static class Protect extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Protect()
		{
			super("Protect", "It enables the user to evade all attacks. Its chance of failing rises if it is used in succession.", 10, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect("Protecting", EffectType.POKEMON));
			super.priority = 4;
			super.moveTypes.add("SuccessiveDecay");
			super.selfTarget = true;
			super.moveTypes.add("Assistless");
			super.moveTypes.add("Metronomeless");
		}
	}

	private static class Detect extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Detect()
		{
			super("Detect", "It enables the user to evade all attacks. Its chance of failing rises if it is used in succession.", 5, Type.FIGHTING, Category.STATUS);
			super.effects.add(Effect.getEffect("Protecting", EffectType.POKEMON));
			super.priority = 4;
			super.moveTypes.add("SuccessiveDecay");
			super.selfTarget = true;
			super.moveTypes.add("Assistless");
			super.moveTypes.add("Metronomeless");
		}
	}

	private static class QuickGuard extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public QuickGuard()
		{
			super("Quick Guard", "The user protects itself and its allies from priority moves. If used in succession, its chance of failing rises.", 15, Type.FIGHTING, Category.STATUS);
			super.effects.add(Effect.getEffect("QuickGuard", EffectType.POKEMON));
			super.priority = 3;
			super.moveTypes.add("SuccessiveDecay");
			super.selfTarget = true;
			super.moveTypes.add("Metronomeless");
		}
	}

	private static class Endure extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Endure()
		{
			super("Endure", "The user endures any attack with at least 1 HP. Its chance of failing rises if it is used in succession.", 10, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect("Bracing", EffectType.POKEMON));
			super.selfTarget = true;
			super.moveTypes.add("Assistless");
			super.moveTypes.add("SuccessiveDecay");
			super.priority = 4;
			super.moveTypes.add("NonSnatchable");
			super.moveTypes.add("Metronomeless");
		}
	}

	private static class WaterPulse extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public WaterPulse()
		{
			super("Water Pulse", "The user attacks the target with a pulsing blast of water. It may also confuse the target.", 20, Type.WATER, Category.SPECIAL);
			super.effects.add(Effect.getEffect("Confusion", EffectType.POKEMON));
			super.accuracy = 100;
			super.power = 60;
			super.effectChance = 20;
		}
	}

	private static class ConfusionDamage extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public ConfusionDamage()
		{
			super("ConfusionDamage", "None", 1, Type.NONE, Category.PHYSICAL);
			super.power = 40;
			super.moveTypes.add("CannotCrit");
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class ConfuseRay extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public ConfuseRay()
		{
			super("Confuse Ray", "The target is exposed to a sinister ray that triggers confusion.", 10, Type.GHOST, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("Confusion", EffectType.POKEMON));
		}
	}

	private static class AquaTail extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public AquaTail()
		{
			super("Aqua Tail", "The user attacks by swinging its tail as if it were a vicious wave in a raging storm.", 10, Type.WATER, Category.PHYSICAL);
			super.accuracy = 90;
			super.power = 90;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class SkullBash extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;
		public SkullBash()
		{
			super("Skull Bash", "The user tucks in its head to raise its Defense in the first turn, then rams the target on the next turn.", 15, Type.NORMAL, Category.PHYSICAL);
			super.moveTypes.add("SleepTalkFail");
			super.power = 100;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
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
			b.addMessage(user.getName()+" lowered its head!");
			user.getAttributes().modifyStage(user, user, 1, Stat.DEFENSE, b, CastSource.ATTACK);
		}
	}

	private static class IronDefense extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public IronDefense()
		{
			super("Iron Defense", "The user hardens its body's surface like iron, sharply raising its Defense stat.", 15, Type.STEEL, Category.STATUS);
			super.statChanges[Stat.DEFENSE.index()] = 2;
			super.selfTarget = true;
		}
	}

	private static class RainDance extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public RainDance()
		{
			super("Rain Dance", "The user summons a heavy rain that falls for five turns, powering up Water-type moves.", 5, Type.WATER, Category.STATUS);
			super.effects.add(Effect.getEffect("Raining", EffectType.BATTLE));
			super.moveTypes.add("Field");
		}
	}

	private static class HydroPump extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public HydroPump()
		{
			super("Hydro Pump", "The target is blasted by a huge volume of water launched under great pressure.", 5, Type.WATER, Category.SPECIAL);
			super.power = 120;
			super.accuracy = 80;
		}
	}

	private static class SunnyDay extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SunnyDay()
		{
			super("Sunny Day", "The user intensifies the sun for five turns, powering up Fire-type moves.", 5, Type.FIRE, Category.STATUS);
			super.moveTypes.add("Field");
			super.effects.add(Effect.getEffect("Sunny", EffectType.BATTLE));
		}
	}

	private static class Sandstorm extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Sandstorm()
		{
			super("Sandstorm", "A five-turn sandstorm is summoned to hurt all combatants except the Rock, Ground, and Steel types.", 10, Type.ROCK, Category.STATUS);
			super.moveTypes.add("Field");
			super.effects.add(Effect.getEffect("Sandstorm", EffectType.BATTLE));
		}
	}

	private static class Hail extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Hail()
		{
			super("Hail", "The user summons a hailstorm lasting five turns. It damages all Pok\u00e9mon except the Ice type.", 10, Type.ICE, Category.STATUS);
			super.moveTypes.add("Field");
			super.effects.add(Effect.getEffect("Hailing", EffectType.BATTLE));
		}
	}

	private static class PetalDance extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public PetalDance()
		{
			super("Petal Dance", "The user attacks the target by scattering petals for two to three turns. The user then becomes confused.", 10, Type.GRASS, Category.SPECIAL);
			super.accuracy = 100;
			super.power = 120;
			super.effects.add(Effect.getEffect("SelfConfusion", EffectType.POKEMON));
			super.selfTarget = true;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Thrash extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Thrash()
		{
			super("Thrash", "The user rampages and attacks for two to three turns. It then becomes confused, however.", 10, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 100;
			super.power = 120;
			super.effects.add(Effect.getEffect("SelfConfusion", EffectType.POKEMON));
			super.selfTarget = true;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class HyperBeam extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;
		public HyperBeam()
		{
			super("Hyper Beam", "The target is attacked with a powerful beam. The user must rest on the next turn to regain its energy.", 5, Type.NORMAL, Category.SPECIAL);
			super.accuracy = 90;
			super.power = 150;
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
			b.addMessage(user.getName()+" must recharge!");
		}
	}

	private static class StringShot extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public StringShot()
		{
			super("String Shot", "The targets are bound with silk blown from the user's mouth. This silk reduces the targets' Speed stat.", 40, Type.BUG, Category.STATUS);
			super.statChanges[Stat.SPEED.index()] = -1;
			super.accuracy = 95;
		}
	}

	private static class BugBite extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public BugBite()
		{
			super("Bug Bite", "The user bites the target. If the target is holding a Berry, the user eats it and gains its effect.", 20, Type.BUG, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Item i = victim.getHeldItem(b);
			if (i instanceof Berry)
			{
				b.addMessage(user.getName()+" ate "+victim.getName()+"'s "+i.getName()+"!");
				victim.consumeItem(b);
				
				if (i instanceof GainableEffectBerry)
				{
					Item temp = user.getActualHeldItem();
					user.giveItem((HoldItem)i);
					
					((GainableEffectBerry)i).useBerry(b, user, victim);
					user.giveItem((HoldItem)temp);
				}
			}
		}
	}

	private static class Harden extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Harden()
		{
			super("Harden", "The user stiffens all the muscles in its body to raise its Defense stat.", 30, Type.NORMAL, Category.STATUS);
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.selfTarget = true;
		}
	}

	private static class Confusion extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Confusion()
		{
			super("Confusion", "The target is hit by a weak telekinetic force. It may also leave the target confused.", 25, Type.PSYCHIC, Category.SPECIAL);
			super.effects.add(Effect.getEffect("Confusion", EffectType.POKEMON));
			super.effectChance = 10;
			super.power = 50;
			super.accuracy = 100;
		}
	}

	private static class StunSpore extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public StunSpore()
		{
			super("Stun Spore", "The user scatters a cloud of paralyzing powder. It may leave the target with paralysis.", 30, Type.GRASS, Category.STATUS);
			super.accuracy = 75;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add("Powder");
		}
	}

	private static class Gust extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Gust()
		{
			super("Gust", "A gust of wind is whipped up by wings and launched at the target to inflict damage.", 35, Type.FLYING, Category.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add("HitFly");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			if (o.isSemiInvulnerable() && o.getAttack().getName().equals("Fly")) return super.power*2;
			return super.power;
		}
	}

	private static class Supersonic extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Supersonic()
		{
			super("Supersonic", "The user generates odd sound waves from its body. It may confuse the target.", 20, Type.NORMAL, Category.STATUS);
			super.accuracy = 55;
			super.moveTypes.add("SoundBased");
			super.effects.add(Effect.getEffect("Confusion", EffectType.POKEMON));
		}
	}

	private static class Psybeam extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Psybeam()
		{
			super("Psybeam", "The target is attacked with a peculiar ray. It may also cause confusion.", 20, Type.PSYCHIC, Category.SPECIAL);
			super.power = 65;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("Confusion", EffectType.POKEMON));
			super.effectChance = 10;
		}
	}

	private static class SilverWind extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SilverWind()
		{
			super("Silver Wind", "The target is attacked with powdery scales blown by wind. It may also raise all the user's stats.", 5, Type.BUG, Category.SPECIAL);
			super.power = 60;
			super.accuracy = 100;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
			super.statChanges[Stat.SPEED.index()] = 1;
			super.statChanges[Stat.ACCURACY.index()] = 1;
			super.statChanges[Stat.EVASION.index()] = 1;
			super.selfTarget = true;
			super.effectChance = 10;
		}
	}

	private static class Tailwind extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Tailwind()
		{
			super("Tailwind", "The user whips up a turbulent whirlwind that ups the Speed of all party Pok\u00e9mon for four turns.", 30, Type.FLYING, Category.STATUS);
			super.effects.add(Effect.getEffect("Tailwind", EffectType.TEAM));
			super.selfTarget = true;
		}
	}

	private static class MorningSun extends Attack implements SelfHealingMove
	{
		private static final long serialVersionUID = 1L;
		public MorningSun()
		{
			super("Morning Sun", "The user restores its own HP. The amount of HP regained varies with the weather.", 5, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect("HealBlock"))
			{
				b.addMessage("...but it failed!");
				return;
			}

			switch (b.getWeather().getType())
			{
				case CLEAR_SKIES:
				victim.healHealthFraction(1/2.0);
				break;
				case SUNNY:
				victim.healHealthFraction(2/3.0);
				break;
				case HAILING:
				case RAINING:
				case SANDSTORM:
				victim.healHealthFraction(1/4.0);
				break;
				default:
				Global.error("Funky weather problems!!!!");
				break;
			}
			b.addMessage(victim.getName()+"'s health was restored!", victim.getHP(), victim.user());
		}
	}

	private static class Safeguard extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Safeguard()
		{
			super("Safeguard", "The user creates a protective field that prevents status problems for five turns.", 25, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect("Safeguard", EffectType.POKEMON));
			super.selfTarget = true;
		}
	}

	private static class Captivate extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Captivate()
		{
			super("Captivate", "If it is the opposite gender of the user, the target is charmed into harshly lowering its Sp. Atk stat.", 20, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.SP_ATTACK.index()] = -2;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (Gender.oppositeGenders(user, victim)) super.applyEffects(b, user, victim);
			else b.addMessage("...but it failed!");
		}
	}

	private static class BugBuzz extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public BugBuzz()
		{
			super("Bug Buzz", "The user vibrates its wings to generate a damaging sound wave. It may also lower the target's Sp. Def stat.", 10, Type.BUG, Category.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
			super.effectChance = 10;
			super.moveTypes.add("SoundBased");
		}
	}

	private static class QuiverDance extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public QuiverDance()
		{
			super("Quiver Dance", "The user lightly performs a beautiful, mystic dance. It boosts the user's Sp. Atk, Sp. Def, and Speed stats.", 20, Type.BUG, Category.STATUS);
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
			super("Encore", "The user compels the target to keep using only the move it last used for three turns.", 5, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("Encore", EffectType.POKEMON));
			super.moveTypes.add("Encoreless");
			super.moveTypes.add("SubstitutePiercing");
		}
	}

	private static class PoisonSting extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public PoisonSting()
		{
			super("Poison Sting", "The user stabs the target with a poisonous stinger. This may also poison the target.", 35, Type.POISON, Category.PHYSICAL);
			super.status = StatusCondition.POISONED;
			super.effectChance = 30;
			super.accuracy = 100;
			super.power = 15;
		}
	}

	private static class FuryAttack extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FuryAttack()
		{
			super("Fury Attack", "The target is jabbed repeatedly with a horn or beak two to five times in a row.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 15;
			super.accuracy = 85;
			super.moveTypes.add("PhysicalContact");
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			int damage = 0, hits = (int)(Math.random()*(5-2+1))+2;
			if (me.hasAbility("Skill Link")) hits = 5;
			for (int i = 1; i <= hits; i++)
			{
				b.addMessage("Hit "+i+"!");
				damage += super.applyDamage(me, o, b);
			}
			b.addMessage("Hit "+hits+" times!");
			return damage;
		}
	}

	private static class FalseSwipe extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FalseSwipe()
		{
			super("False Swipe", "A restrained attack that prevents the target from fainting. The target is left with at least 1 HP.", 40, Type.NORMAL, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			o.addEffect(PokemonEffect.getEffect("Bracing"));
			super.applyDamage(me, o, b);
			o.getAttributes().removeEffect("Bracing");
		}
	}

	private static class Disable extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Disable()
		{
			super("Disable", "For four turns, this move prevents the target from using the move it last used.", 20, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("Disable", EffectType.POKEMON));
			super.moveTypes.add("SubstitutePiercing");
		}
	}

	private static class FocusEnergy extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FocusEnergy()
		{
			super("Focus Energy", "The user takes a deep breath and focuses so that critical hits land more easily.", 30, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
			super.effects.add(Effect.getEffect("RaiseCrits", EffectType.POKEMON));
		}
	}

	private static class Twineedle extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Twineedle()
		{
			super("Twineedle", "The user damages the target twice in succession by jabbing it with two spikes. It may also poison the target.", 20, Type.BUG, Category.PHYSICAL);
			super.status = StatusCondition.POISONED;
			super.effectChance = 20;
			super.power = 25;
			super.accuracy = 100;
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			int damage = 0, hits = (int)(Math.random()*(2-2+1))+2;
			for (int i = 1; i <= hits; i++)
			{
				b.addMessage("Hit "+i+"!");
				damage += super.applyDamage(me, o, b);
			}
			b.addMessage("Hit "+hits+" times!");
			return damage;
		}
	}

	private static class Rage extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Rage()
		{
			super("Rage", "As long as this move is in use, the power of rage raises the Attack stat each time the user is hit in battle.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 20;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*me.getAttributes().getCount();
		}
	}

	private static class Pursuit extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Pursuit()
		{
			super("Pursuit", "An attack move that inflicts double damage if used on a target that is switching out of battle.", 20, Type.DARK, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			Team trainer = b.getTrainer(o.user());
			if (trainer instanceof Trainer && ((Trainer)trainer).getAction() == Action.SWITCH) return super.power*2;
			return super.power;
		}

		public int getPriority(Battle b, ActivePokemon me)
		{
			Team trainer = b.getTrainer(!me.user()); // TODO: Make switching occur at its priority
			if (trainer instanceof Trainer && ((Trainer)trainer).getAction() == Action.SWITCH) return 7;
			return super.priority;
		}
	}

	private static class ToxicSpikes extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public ToxicSpikes()
		{
			super("Toxic Spikes", "The user lays a trap of poison spikes at the opponent's feet. They poison opponents that switch into battle.", 20, Type.POISON, Category.STATUS);
			super.moveTypes.add("Field");
			super.effects.add(Effect.getEffect("ToxicSpikes", EffectType.TEAM));
		}
	}

	private static class PinMissile extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public PinMissile()
		{
			super("Pin Missile", "Sharp spikes are shot at the target in rapid succession. They hit two to five times in a row.", 20, Type.BUG, Category.PHYSICAL);
			super.power = 14;
			super.accuracy = 85;
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			int damage = 0, hits = (int)(Math.random()*(5-2+1))+2;
			if (me.hasAbility("Skill Link")) hits = 5;
			for (int i = 1; i <= hits; i++)
			{
				b.addMessage("Hit "+i+"!");
				damage += super.applyDamage(me, o, b);
			}
			b.addMessage("Hit "+hits+" times!");
			return damage;
		}
	}

	private static class Agility extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Agility()
		{
			super("Agility", "The user relaxes and lightens its body to move faster. It sharply boosts the Speed stat.", 30, Type.PSYCHIC, Category.STATUS);
			super.statChanges[Stat.SPEED.index()] = 2;
			super.selfTarget = true;
		}
	}

	private static class Assurance extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Assurance()
		{
			super("Assurance", "If the target has already taken some damage in the same turn, this attack's power is doubled.", 10, Type.DARK, Category.PHYSICAL);
			super.power = 50;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			if (me.getAttributes().hasTakenDamage()) return super.power*2;
			return super.power;
		}
	}

	private static class PoisonJab extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public PoisonJab()
		{
			super("Poison Jab", "The target is stabbed with a tentacle or arm steeped in poison. It may also poison the target.", 20, Type.POISON, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.status = StatusCondition.POISONED;
			super.effectChance = 30;
			super.moveTypes.add("Punching");
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Endeavor extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Endeavor()
		{
			super("Endeavor", "An attack move that cuts down the target's HP to equal the user's HP.", 5, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return;
			if (me.getHP() >= o.getHP())
			{
				b.addMessage("...but it failed!");
				return;
			}
			b.applyDamage(o, o.getHP()-me.getHP());
		}
	}

	private static class SandAttack extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SandAttack()
		{
			super("Sand-attack", "Sand is hurled in the target's face, reducing its accuracy.", 15, Type.GROUND, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	private static class QuickAttack extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public QuickAttack()
		{
			super("Quick Attack", "The user lunges at the target at a speed that makes it almost invisible. It is sure to strike first.", 30, Type.NORMAL, Category.PHYSICAL);
			super.power = 40;
			super.priority = 1;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Twister extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Twister()
		{
			super("Twister", "The user whips up a vicious tornado to tear at the opposing team. It may also make targets flinch.", 20, Type.DRAGON, Category.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.effectChance = 20;
			super.moveTypes.add("HitFly");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			if (o.isSemiInvulnerable() && o.getAttack().getName().equals("Fly")) return super.power*2;
			return super.power;
		}
	}

	private static class Featherdance extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Featherdance()
		{
			super("Featherdance", "The user covers the target's body with a mass of down that harshly lowers its Attack stat.", 15, Type.FLYING, Category.STATUS);
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
			super("Roost", "The user lands and rests its body. It restores the user's HP by up to half of its max HP.", 10, Type.FLYING, Category.STATUS);
			super.selfTarget = true;
			super.printCast = false;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			healFail = true;
			super.applyEffects(b, user, victim);
			if (!healFail && getType(b, user, victim) != null)
			{
				PokemonEffect.getEffect("ChangeType").cast(b, user, victim, CastSource.ATTACK, super.printCast);
				user.getEffect("ChangeType").setTurns(1);
			}
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect("HealBlock"))
			{
				b.addMessage("...but it failed!");
				return;
			}

			healFail = false;
			victim.healHealthFraction(1/2.0);
			b.addMessage(victim.getName()+"'s health was restored!", victim.getHP(), victim.user());
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			if (victim.getType()[0].equals(Type.FLYING)) return new Type[] {victim.getType()[1], Type.NONE};
			if (victim.getType()[1].equals(Type.FLYING)) return new Type[] {victim.getType()[0], Type.NONE};
			return null;
		}
	}

	private static class Thundershock extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Thundershock()
		{
			super("Thundershock", "A jolt of electricity is hurled at the target to inflict damage. It may also leave the target with paralysis.", 30, Type.ELECTRIC, Category.SPECIAL);
			super.accuracy = 100;
			super.power = 40;
			super.status = StatusCondition.PARALYZED;
			super.effectChance = 10;
		}
	}

	private static class MirrorMove extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MirrorMove()
		{
			super("Mirror Move", "The user counters the target by mimicking the target's last move.", 20, Type.FLYING, Category.STATUS);
			super.moveTypes.add("SleepTalkFail");
			super.moveTypes.add("Encoreless");
			super.moveTypes.add("Metronomeless");
			super.moveTypes.add("Assistless");
			super.moveTypes.add("NoMagicCoat");
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			Move mirror = o.getAttributes().getLastMoveUsed();
			if (mirror == null || mirror.getAttack().getName().equals("Mirror Move") ||	mirror.getAttack().getName().equals("Copycat"))
			{
				b.addMessage("...but it failed!");
				return;
			}
			me.callNewMove(b, o, new Move(mirror.getAttack()));
		}
	}

	private static class Hurricane extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Hurricane()
		{
			super("Hurricane", "The user attacks by wrapping its opponent in a fierce wind that flies up into the sky. It may also confuse the target.", 10, Type.FLYING, Category.SPECIAL);
			super.power = 120;
			super.accuracy = 70;
			super.moveTypes.add("HitFly");
			super.effects.add(Effect.getEffect("Confusion", EffectType.POKEMON));
			super.effectChance = 30;
		}

		public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o)
		{
			switch (b.getWeather().getType())
			{
				case RAINING:
				return 100;
				case SUNNY:
				return 50;
				default:
				return super.accuracy;
			}
		}
	}

	private static class HyperFang extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public HyperFang()
		{
			super("Hyper Fang", "The user bites hard on the target with its sharp front fangs. It may also make the target flinch.", 15, Type.NORMAL, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 90;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.effectChance = 10;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class SuckerPunch extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SuckerPunch()
		{
			super("Sucker Punch", "This move enables the user to attack first. It fails if the foe is not readying an attack, however.", 5, Type.DARK, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.priority = 1;
			super.moveTypes.add("PhysicalContact");
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (o.getMove().getAttack().getCategory() == Category.STATUS)
			{
				b.addMessage("...but it failed!");
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
			super("Crunch", "The user crunches up the foe with sharp fangs. It may also lower the target's Defense stat.", 15, Type.DARK, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.effectChance = 20;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class SuperFang extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SuperFang()
		{
			super("Super Fang", "The user chomps hard on the foe with its sharp front fangs. It cuts the target's HP to half.", 10, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 90;
			super.moveTypes.add("PhysicalContact");
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			return b.applyDamage(o, (int)Math.ceil(o.getHP()/2.0));
		}
	}

	private static class SwordsDance extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SwordsDance()
		{
			super("Swords Dance", "A frenetic dance to uplift the fighting spirit. It sharply raises the user's Attack stat.", 30, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 2;
		}
	}

	private static class Peck extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Peck()
		{
			super("Peck", "The foe is jabbed with a sharply pointed beak or horn.", 35, Type.FLYING, Category.PHYSICAL);
			super.power = 35;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Leer extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Leer()
		{
			super("Leer", "The foe is given an intimidating leer with sharp eyes. The target's Defense stat is reduced.", 30, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.DEFENSE.index()] = -1;
		}
	}

	private static class AerialAce extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public AerialAce()
		{
			super("Aerial Ace", "The user confounds the foe with speed, then slashes. The attack lands without fail.", 20, Type.FLYING, Category.PHYSICAL);
			super.power = 60;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class DrillPeck extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public DrillPeck()
		{
			super("Drill Peck", "A corkscrewing attack with the sharp beak acting as a drill.", 20, Type.FLYING, Category.PHYSICAL);
			super.accuracy = 100;
			super.power = 80;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Pluck extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Pluck()
		{
			super("Pluck", "The user pecks the foe. If the foe is holding a Berry, the user plucks it and gains its effect.", 20, Type.FLYING, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Item i = victim.getHeldItem(b);
			if (i instanceof Berry)
			{
				b.addMessage(user.getName()+" ate "+victim.getName()+"'s "+i.getName()+"!");
				victim.consumeItem(b);
				
				if (i instanceof GainableEffectBerry)
				{
					Item temp = user.getActualHeldItem();
					user.giveItem((HoldItem)i);
					
					((GainableEffectBerry)i).useBerry(b, user, victim);
					user.giveItem((HoldItem)temp);
				}
			}
		}
	}

	private static class DrillRun extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public DrillRun()
		{
			super("Drill Run", "The user crashes into its target while rotating its body like a drill. Critical hits land more easily.", 10, Type.GROUND, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 95;
			super.moveTypes.add("HighCritRatio");
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Wrap extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Wrap()
		{
			super("Wrap", "A long body or vines are used to wrap and squeeze the target for four to five turns.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 15;
			super.accuracy = 90;
			super.effects.add(Effect.getEffect("Wrapped", EffectType.POKEMON));
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Glare extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Glare()
		{
			super("Glare", "The user intimidates the target with the pattern on its belly to cause paralysis.", 30, Type.NORMAL, Category.STATUS);
			super.accuracy = 90;
			super.status = StatusCondition.PARALYZED;
		}
	}

	private static class Screech extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Screech()
		{
			super("Screech", "An earsplitting screech harshly reduces the target's Defense stat.", 40, Type.NORMAL, Category.STATUS);
			super.accuracy = 85;
			super.statChanges[Stat.DEFENSE.index()] = -2;
			super.moveTypes.add("SoundBased");
		}
	}

	private static class Acid extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Acid()
		{
			super("Acid", "The opposing team is attacked with a spray of harsh acid. The acid may also lower the targets' Sp. Def stats.", 30, Type.POISON, Category.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
			super.effectChance = 10;
		}
	}

	private static class Stockpile extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Stockpile()
		{
			super("Stockpile", "The user charges up power and raises both its Defense and Sp. Def. The move can be used three times.", 20, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
			super.effects.add(Effect.getEffect("Stockpile", EffectType.POKEMON));
		}
	}

	private static class SpitUp extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SpitUp()
		{
			super("Spit Up", "The power stored using the move Stockpile is released at once in an attack. The more power is stored, the greater the damage.", 10, Type.NORMAL, Category.SPECIAL);
			super.accuracy = 100;
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			int turns = me.getEffect("Stockpile").getTurns();
			if (turns <= 0) Global.error("Stockpile turns should never be nonpositive");
			return (int)Math.min(turns*100, 300);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (!me.hasEffect("Stockpile"))
			{
				b.addMessage("...but it failed!");
				return;
			}
			super.apply(me, o, b);
		}
	}

	private static class Swallow extends Attack implements SelfHealingMove
	{
		private static final long serialVersionUID = 1L;
		public Swallow()
		{
			super("Swallow", "The power stored using the move Stockpile is absorbed by the user to heal its HP. Storing more power heals more HP.", 10, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect("HealBlock"))
			{
				b.addMessage("...but it failed!");
				return;
			}

			PokemonEffect e = user.getEffect("Stockpile");
			if (e  == null)
			{
				b.addMessage("...but it failed!");
				return;
			}
			if (e.getTurns() <= 0) Global.error("Stockpile turns should never be nonpositive");
			switch (e.getTurns())
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
			b.addMessage(victim.getName()+"'s health was restored!", victim.getHP(), victim.user());
		}
	}

	private static class AcidSpray extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public AcidSpray()
		{
			super("Acid Spray", "The user spits fluid that works to melt the target. This harshly reduces the target's Sp. Def stat.", 20, Type.POISON, Category.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.statChanges[Stat.SP_DEFENSE.index()] = -2;
		}
	}

	private static class MudBomb extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MudBomb()
		{
			super("Mud Bomb", "The user launches a hard-packed mud ball to attack. It may also lower the target's accuracy.", 10, Type.GROUND, Category.SPECIAL);
			super.power = 65;
			super.accuracy = 85;
			super.statChanges[Stat.ACCURACY.index()] = -1;
			super.effectChance = 30;
		}
	}

	private static class Haze extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Haze()
		{
			super("Haze", "The user creates a haze that eliminates every stat change among all the Pok\u00e9mon engaged in battle.", 30, Type.ICE, Category.STATUS);
			super.moveTypes.add("Field");
			super.moveTypes.add("SubstitutePiercing");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			user.getAttributes().resetStages();
			victim.getAttributes().resetStages();
			b.addMessage("All stat changes were eliminated!");
		}
	}

	private static class Coil extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Coil()
		{
			super("Coil", "The user coils up and concentrates. This raises its Attack and Defense stats as well as its accuracy.", 20, Type.POISON, Category.STATUS);
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
			super("Gunk Shot", "The user shoots filthy garbage at the target to attack. It may also poison the target.", 5, Type.POISON, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 70;
			super.status = StatusCondition.POISONED;
			super.effectChance = 30;
		}
	}

	private static class IceFang extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public IceFang()
		{
			super("Ice Fang", "The user bites with cold-infused fangs. It may also make the target flinch or leave it frozen.", 15, Type.ICE, Category.PHYSICAL);
			super.power = 65;
			super.status = StatusCondition.FROZEN;
			super.effectChance = 10;
			super.accuracy = 95;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.moveTypes.add("PhysicalContact");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (Math.random()*100 < 50) Status.giveStatus(b, user, victim, status);
			else if (effects.get(0).applies(b, user, victim, CastSource.ATTACK)) effects.get(0).cast(b, user, victim, CastSource.ATTACK, super.printCast);
		}
	}

	private static class ThunderFang extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public ThunderFang()
		{
			super("Thunder Fang", "The user bites with electrified fangs. It may also make the target flinch or leave it with paralysis.", 15, Type.ELECTRIC, Category.PHYSICAL);
			super.power = 65;
			super.status = StatusCondition.PARALYZED;
			super.effectChance = 10;
			super.accuracy = 95;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.moveTypes.add("PhysicalContact");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (Math.random()*100 < 50) Status.giveStatus(b, user, victim, status);
			else if (effects.get(0).applies(b, user, victim, CastSource.ATTACK)) effects.get(0).cast(b, user, victim, CastSource.ATTACK, super.printCast);
		}
	}

	private static class ElectroBall extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public ElectroBall()
		{
			super("Electro Ball", "The user hurls an electric orb at the target. The faster the user is than the target, the greater the damage.", 10, Type.ELECTRIC, Category.SPECIAL);
			super.accuracy = 100;
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
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
			super("Double Team", "By moving rapidly, the user makes illusory copies of itself to raise its evasiveness.", 15, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.EVASION.index()] = 1;
		}
	}

	private static class Slam extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Slam()
		{
			super("Slam", "The target is slammed with a long tail, vines, etc., to inflict damage.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 75;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Thunderbolt extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Thunderbolt()
		{
			super("Thunderbolt", "A strong electric blast is loosed at the target. It may also leave the target with paralysis.", 15, Type.ELECTRIC, Category.SPECIAL);
			super.power = 95;
			super.accuracy = 100;
			super.status = StatusCondition.PARALYZED;
			super.effectChance = 10;
		}
	}

	private static class Feint extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Feint()
		{
			super("Feint", "An attack that hits a target using Protect or Detect. It also lifts the effects of those moves.", 10, Type.NORMAL, Category.PHYSICAL);
			super.power = 30;
			super.accuracy = 100;
			super.moveTypes.add("Assistless");
			super.priority = 2;
			super.moveTypes.add("ProtectPiercing");
			super.moveTypes.add("Metronomeless");
		}
	}

	private static class Discharge extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Discharge()
		{
			super("Discharge", "A flare of electricity is loosed to strike the area around the user. It may also cause paralysis.", 15, Type.ELECTRIC, Category.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.status = StatusCondition.PARALYZED;
			super.effectChance = 30;
		}
	}

	private static class LightScreen extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public LightScreen()
		{
			super("Light Screen", "A wondrous wall of light is put up to suppress damage from special attacks for five turns.", 30, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect("LightScreen", EffectType.TEAM));
			super.selfTarget = true;
		}
	}

	private static class Thunder extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Thunder()
		{
			super("Thunder", "A wicked thunderbolt is dropped on the target to inflict damage. It may also leave the target with paralysis.", 10, Type.ELECTRIC, Category.SPECIAL);
			super.power = 120;
			super.accuracy = 70;
			super.status = StatusCondition.PARALYZED;
			super.effectChance = 30;
		}

		public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o)
		{
			switch (b.getWeather().getType())
			{
				case RAINING:
				return 100;
				case SUNNY:
				return 50;
				default:
				return super.accuracy;
			}
		}
	}

	private static class DefenseCurl extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public DefenseCurl()
		{
			super("Defense Curl", "The user curls up to conceal weak spots and raise its Defense stat.", 40, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
			super.effects.add(Effect.getEffect("UsedDefenseCurl", EffectType.POKEMON));
			super.statChanges[Stat.DEFENSE.index()] = 1;
		}
	}

	private static class Swift extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Swift()
		{
			super("Swift", "Star-shaped rays are shot at the opposing team. This attack never misses.", 20, Type.NORMAL, Category.SPECIAL);
			super.power = 60;
		}
	}

	private static class FurySwipes extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FurySwipes()
		{
			super("Fury Swipes", "The target is raked with sharp claws or scythes for two to five times in quick succession.", 15, Type.NORMAL, Category.PHYSICAL);
			super.power = 18;
			super.accuracy = 80;
			super.moveTypes.add("PhysicalContact");
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			int damage = 0, hits = (int)(Math.random()*(5-2+1))+2;
			if (me.hasAbility("Skill Link")) hits = 5;
			for (int i = 1; i <= hits; i++)
			{
				b.addMessage("Hit "+i+"!");
				damage += super.applyDamage(me, o, b);
			}
			b.addMessage("Hit "+hits+" times!");
			return damage;
		}
	}

	private static class Rollout extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Rollout()
		{
			super("Rollout", "The user continually rolls into the target over five turns. It becomes stronger each time it hits.", 20, Type.ROCK, Category.PHYSICAL);
			super.power = 30;
			super.accuracy = 90;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(int)Math.min(me.getAttributes().getCount(), 5)*(me.hasEffect("UsedDefenseCurl") ? 2 : 1);
		}
	}

	private static class FuryCutter extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FuryCutter()
		{
			super("Fury Cutter", "The target is slashed with scythes or claws. Its power increases if it hits in succession.", 20, Type.BUG, Category.PHYSICAL);
			super.accuracy = 95;
			super.power = 40;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return (int)Math.min(160, super.power*me.getAttributes().getCount());
		}
	}

	private static class SandTomb extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SandTomb()
		{
			super("Sand Tomb", "The user traps the target inside a harshly raging sandstorm for four to five turns.", 15, Type.GROUND, Category.PHYSICAL);
			super.power = 35;
			super.accuracy = 85;
			super.effects.add(Effect.getEffect("SandTomb", EffectType.POKEMON));
		}
	}

	private static class GyroBall extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public GyroBall()
		{
			super("Gyro Ball", "The user tackles the target with a high-speed spin. The slower the user, the greater the damage.", 5, Type.STEEL, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return (int)Math.min(150, 25.0*Stat.getStat(Stat.SPEED, o, me, b)/Stat.getStat(Stat.SPEED, me, o, b));
		}
	}

	private static class CrushClaw extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public CrushClaw()
		{
			super("Crush Claw", "The user slashes the target with hard and sharp claws. It may also lower the target's Defense.", 10, Type.NORMAL, Category.PHYSICAL);
			super.power = 75;
			super.accuracy = 95;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.effectChance = 50;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class DoubleKick extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public DoubleKick()
		{
			super("Double Kick", "The target is quickly kicked twice in succession using both feet.", 30, Type.FIGHTING, Category.PHYSICAL);
			super.power = 30;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			int damage = 0, hits = (int)(Math.random()*(2-2+1))+2;
			for (int i = 1; i <= hits; i++)
			{
				b.addMessage("Hit "+i+"!");
				damage += super.applyDamage(me, o, b);
			}
			b.addMessage("Hit "+hits+" times!");
			return damage;
		}
	}

	private static class PoisonTail extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public PoisonTail()
		{
			super("Poison Tail", "The user hits the target with its tail. It may also poison the target. Critical hits land more easily.", 25, Type.POISON, Category.PHYSICAL);
			super.power = 50;
			super.moveTypes.add("HighCritRatio");
			super.accuracy = 100;
			super.status = StatusCondition.POISONED;
			super.effectChance = 10;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Flatter extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Flatter()
		{
			super("Flatter", "Flattery is used to confuse the target. However, it also raises the target's Sp. Atk stat.", 15, Type.DARK, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("Confusion", EffectType.POKEMON));
			super.statChanges[Stat.SP_ATTACK.index()] = 2;
		}
	}

	private static class PoisonFang extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public PoisonFang()
		{
			super("Poison Fang", "The user bites the target with toxic fangs. It may also leave the target badly poisoned.", 15, Type.POISON, Category.PHYSICAL);
			super.power = 50;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("BadPoison", EffectType.POKEMON));
			super.effectChance = 30;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class ChipAway extends Attack implements StageChangingEffect
	{
		private static final long serialVersionUID = 1L;
		public ChipAway()
		{
			super("Chip Away", "Looking for an opening, the user strikes continually. The target's stat changes don't affect this attack's damage.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public int adjustStage(int stage, Stat s, ActivePokemon p, ActivePokemon opp, Battle b, boolean user)
		{
			return !user ? 0 : stage;
		}
	}

	private static class BodySlam extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public BodySlam()
		{
			super("Body Slam", "The user drops onto the target with its full body weight. It may also leave the target with paralysis.", 15, Type.NORMAL, Category.PHYSICAL);
			super.power = 85;
			super.accuracy = 100;
			super.status = StatusCondition.PARALYZED;
			super.effectChance = 30;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class EarthPower extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public EarthPower()
		{
			super("Earth Power", "The user makes the ground under the target erupt with power. It may also lower the target's Sp. Def.", 10, Type.GROUND, Category.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
			super.effectChance = 10;
		}
	}

	private static class Superpower extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Superpower()
		{
			super("Superpower", "The user attacks the target with great power. However, it also lowers the user's Attack and Defense.", 5, Type.FIGHTING, Category.PHYSICAL);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = -1;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.power = 120;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class HornAttack extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public HornAttack()
		{
			super("Horn Attack", "The target is jabbed with a sharply pointed horn to inflict damage.", 25, Type.NORMAL, Category.PHYSICAL);
			super.power = 65;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class HornDrill extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public HornDrill()
		{
			super("Horn Drill", "The user stabs the target with a horn that rotates like a drill. If it hits, the target faints instantly.", 5, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 30;
			super.moveTypes.add("OneHitKO");
			super.moveTypes.add("PhysicalContact");
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (me.getLevel() < o.getLevel())
			{
				b.addMessage("...but it failed!");
				return -1;
			}

			if (o.hasAbility("Sturdy") && !me.breaksTheMold())
			{
				b.addMessage(o.getName()+"'s Sturdy prevents OHKO moves!");
				return -1;
			}

			if (super.zeroAdvantage(b, me, o)) return -1;
			b.addMessage("It's a One-Hit KO!");
			return b.applyDamage(o, o.getHP());
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
			super("Megahorn", "Using its tough and impressive horn, the user rams into the target with no letup.", 10, Type.BUG, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 85;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Pound extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Pound()
		{
			super("Pound", "The target is physically pounded with a long tail or a foreleg, etc.", 35, Type.NORMAL, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Sing extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Sing()
		{
			super("Sing", "A soothing lullaby is sung in a calming voice that puts the target into a deep slumber.", 15, Type.NORMAL, Category.STATUS);
			super.accuracy = 55;
			super.status = StatusCondition.ASLEEP;
			super.moveTypes.add("SoundBased");
		}
	}

	private static class Doubleslap extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Doubleslap()
		{
			super("Doubleslap", "The target is slapped repeatedly, back and forth, two to five times in a row.", 10, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 85;
			super.power = 15;
			super.moveTypes.add("PhysicalContact");
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			int damage = 0, hits = (int)(Math.random()*(5-2+1))+2;
			if (me.hasAbility("Skill Link")) hits = 5;
			for (int i = 1; i <= hits; i++)
			{
				b.addMessage("Hit "+i+"!");
				damage += super.applyDamage(me, o, b);
			}
			b.addMessage("Hit "+hits+" times!");
			return damage;
		}
	}

	private static class Wish extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Wish()
		{
			super("Wish", "One turn after this move is used, the target's HP is restored by half the user's maximum HP.", 10, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect("Wish", EffectType.TEAM));
			super.selfTarget = true;
		}
	}

	private static class Minimize extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Minimize()
		{
			super("Minimize", "The user compresses its body to make itself look smaller, which sharply raises its evasiveness.", 20, Type.NORMAL, Category.STATUS);
			super.statChanges[Stat.EVASION.index()] = 2;
			super.selfTarget = true;
			super.effects.add(Effect.getEffect("UsedMinimize", EffectType.POKEMON));
		}
	}

	private static class WakeUpSlap extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public WakeUpSlap()
		{
			super("Wake-up Slap", "This attack inflicts big damage on a sleeping target. It also wakes the target up, however.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (victim.hasStatus(StatusCondition.ASLEEP))
			{
				b.addMessage(victim.getName()+" woke up!", StatusCondition.NONE, victim.user());
				victim.removeStatus();
			}
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(o.hasStatus(StatusCondition.ASLEEP) ? 2 : 1);
		}
	}

	private static class CosmicPower extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public CosmicPower()
		{
			super("Cosmic Power", "The user absorbs a mystical power from space to raise its Defense and Sp. Def stats.", 20, Type.PSYCHIC, Category.STATUS);
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
			super("Lucky Chant", "The user chants an incantation toward the sky, preventing opposing Pok\u00e9mon from landing critical hits.", 30, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect("LuckyChant", EffectType.TEAM));
			super.selfTarget = true;
		}
	}

	private static class Metronome extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Metronome()
		{
			super("Metronome", "The user waggles a finger and stimulates its brain into randomly using nearly any move.", 10, Type.NORMAL, Category.STATUS);
			super.moveTypes.add("SleepTalkFail");
			super.moveTypes.add("Mimicless");
			super.moveTypes.add("Assistless");
			super.moveTypes.add("Metronomeless");
			super.moveTypes.add("NoMagicCoat");
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int index = (int)(Math.random()*moveNames.size());
			while (map.get(moveNames.get(index)).isMoveType("Metronomeless")) index = (int)(Math.random()*moveNames.size());
			me.callNewMove(b, o, new Move(map.get(moveNames.get(index))));
		}
	}

	private static class Gravity extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Gravity()
		{
			super("Gravity", "Gravity is intensified for five turns, making moves involving flying unusable and negating Levitate.", 5, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect("Gravity", EffectType.BATTLE));
			super.moveTypes.add("Field");
		}
	}

	private static class Moonlight extends Attack implements SelfHealingMove
	{
		private static final long serialVersionUID = 1L;
		public Moonlight()
		{
			super("Moonlight", "The user restores its own HP. The amount of HP regained varies with the weather.", 5, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect("HealBlock"))
			{
				b.addMessage("...but it failed!");
				return;
			}

			switch (b.getWeather().getType())
			{
				case CLEAR_SKIES:
				victim.healHealthFraction(1/2.0);
				break;
				case SUNNY:
				victim.healHealthFraction(2/3.0);
				break;
				case HAILING:
				case RAINING:
				case SANDSTORM:
				victim.healHealthFraction(1/4.0);
				break;
				default:
				Global.error("Funky weather problems!!!!");
				break;
			}
			b.addMessage(victim.getName()+"'s health was restored!", victim.getHP(), victim.user());
		}
	}

	private static class StoredPower extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public StoredPower()
		{
			super("Stored Power", "The user attacks the target with stored power. The more the user's stats are raised, the greater the damage.", 10, Type.PSYCHIC, Category.SPECIAL);
			super.power = 20;
			super.accuracy = 100;
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*me.getAttributes().totalStatIncreases();
		}
	}

	private static class Mimic extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Mimic()
		{
			super("Mimic", "The user copies the target's last move. The move can be used during battle until the Pok\u00e9mon is switched out.", 10, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect("Mimic", EffectType.POKEMON));
			super.moveTypes.add("NoMagicCoat");
			super.moveTypes.add("Encoreless");
			super.moveTypes.add("Assistless");
			super.moveTypes.add("Metronomeless");
		}
	}

	private static class MeteorMash extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MeteorMash()
		{
			super("Meteor Mash", "The target is hit with a hard punch fired like a meteor. It may also raise the user's Attack.", 10, Type.STEEL, Category.PHYSICAL);
			super.power = 100;
			super.accuracy = 85;
			super.moveTypes.add("Punching");
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.effectChance = 20;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Imprison extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Imprison()
		{
			super("Imprison", "If the opponents know any move also known by the user, the opponents are prevented from using it.", 10, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect("Imprison", EffectType.POKEMON));
			super.moveTypes.add("NoMagicCoat");
			super.moveTypes.add("ProtectPiercing");
			super.moveTypes.add("SubstitutePiercing");
		}
	}

	private static class WillOWisp extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public WillOWisp()
		{
			super("Will-o-wisp", "The user shoots a sinister, bluish-white flame at the target to inflict a burn.", 15, Type.FIRE, Category.STATUS);
			super.status = StatusCondition.BURNED;
			super.accuracy = 75;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (Ability.blockDamage(b, user, victim)) return;
			super.applyEffects(b, user, victim);
		}
	}

	private static class Payback extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Payback()
		{
			super("Payback", "If the user moves after the target, this attack's power will be doubled.", 10, Type.DARK, Category.PHYSICAL);
			super.accuracy = 100;
			super.power = 50;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return b.isFirstAttack() ? super.power : super.power*2;
		}
	}

	private static class Extrasensory extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Extrasensory()
		{
			super("Extrasensory", "The user attacks with an odd, unseeable power. It may also make the target flinch.", 30, Type.PSYCHIC, Category.SPECIAL);
			super.accuracy = 100;
			super.power = 80;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.effectChance = 10;
		}
	}

	private static class FireBlast extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FireBlast()
		{
			super("Fire Blast", "The target is attacked with an intense blast of all-consuming fire. It may also leave the target with a burn.", 5, Type.FIRE, Category.SPECIAL);
			super.power = 120;
			super.accuracy = 85;
			super.status = StatusCondition.BURNED;
			super.effectChance = 10;
		}
	}

	private static class NastyPlot extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public NastyPlot()
		{
			super("Nasty Plot", "The user stimulates its brain by thinking bad thoughts. It sharply raises the user's Sp. Atk.", 20, Type.DARK, Category.STATUS);
			super.statChanges[Stat.SP_ATTACK.index()] = 2;
			super.selfTarget = true;
		}
	}

	private static class Round extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Round()
		{
			super("Round", "The user attacks the target with a song.", 15, Type.NORMAL, Category.SPECIAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add("SoundBased");
		}
	}

	private static class Rest extends Attack implements SelfHealingMove
	{
		private static final long serialVersionUID = 1L;
		public Rest()
		{
			super("Rest", "The user goes to sleep for two turns. It fully restores the user's HP and heals any status problem.", 10, Type.PSYCHIC, Category.STATUS);
			super.selfTarget = true;
			super.selfTarget = true;
			super.status = StatusCondition.ASLEEP;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (victim.fullHealth() || victim.hasAbility("Insomnia") || victim.hasAbility("Vital Spirit") || victim.hasStatus(StatusCondition.ASLEEP))
			{
				b.addMessage("...but it failed!");
				return;
			}
			victim.removeStatus();
			super.applyEffects(b, user, victim);
			victim.getStatus().setTurns(3);
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect("HealBlock"))
			{
				b.addMessage("...but it failed!");
				return;
			}

			victim.healHealthFraction(1/1.0);
			b.addMessage(victim.getName()+"'s health was restored!", victim.getHP(), victim.user());
		}
	}

	private static class HyperVoice extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public HyperVoice()
		{
			super("Hyper Voice", "The user lets loose a horribly echoing shout with the power to inflict damage.", 10, Type.NORMAL, Category.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
			super.moveTypes.add("SoundBased");
		}
	}

	private static class LeechLife extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public LeechLife()
		{
			super("Leech Life", "The user drains the target's blood. The user's HP is restored by half the damage taken by the target.", 15, Type.BUG, Category.PHYSICAL);
			super.moveTypes.add("SapHealth");
			super.accuracy = 100;
			super.power = 20;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Astonish extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Astonish()
		{
			super("Astonish", "The user attacks the target while shouting in a startling fashion. It may also make the target flinch.", 15, Type.GHOST, Category.PHYSICAL);
			super.power = 30;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.effectChance = 30;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class AirCutter extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public AirCutter()
		{
			super("Air Cutter", "The user launches razor-like wind to slash the opposing team. Critical hits land more easily.", 25, Type.FLYING, Category.SPECIAL);
			super.power = 55;
			super.accuracy = 95;
			super.moveTypes.add("HighCritRatio");
		}
	}

	private static class MeanLook extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MeanLook()
		{
			super("Mean Look", "The user pins the target with a dark, arresting look. The target becomes unable to flee.", 5, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect("Trapped", EffectType.POKEMON));
		}
	}

	private static class Acrobatics extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Acrobatics()
		{
			super("Acrobatics", "The user nimbly strikes the target. If the user is not holding an item, this attack inflicts massive damage.", 15, Type.FLYING, Category.PHYSICAL);
			super.power = 55;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(me.isHoldingItem(b) ? 1 : 2);
		}
	}

	private static class Absorb extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Absorb()
		{
			super("Absorb", "A nutrient-draining attack. The user's HP is restored by half the damage taken by the target.", 25, Type.GRASS, Category.SPECIAL);
			super.power = 20;
			super.accuracy = 100;
			super.moveTypes.add("SapHealth");
		}
	}

	private static class MegaDrain extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MegaDrain()
		{
			super("Mega Drain", "A nutrient-draining attack. The user's HP is restored by half the damage taken by the target.", 15, Type.GRASS, Category.SPECIAL);
			super.moveTypes.add("SapHealth");
			super.power = 40;
			super.accuracy = 100;
		}
	}

	private static class NaturalGift extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public NaturalGift()
		{
			super("Natural Gift", "The user draws power to attack by using its held Berry. The Berry determines its type and power.", 15, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 100;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getHeldItem(b) instanceof Berry) user.consumeItem(b);
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return ((Berry)me.getHeldItem(b)).naturalGiftPower();
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (me.getHeldItem(b) instanceof Berry) super.apply(me, o, b);
			else b.addMessage("...but it failed!");
		}

		public Type getType(Battle b, ActivePokemon user)
		{
			if (user.hasAbility("Normalize")) return Type.NORMAL;
			Item i = user.getHeldItem(b);
			if (i instanceof Berry) return ((Berry)i).naturalGiftType();
			return super.type;
		}
	}

	private static class GigaDrain extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public GigaDrain()
		{
			super("Giga Drain", "A nutrient-draining attack. The user's HP is restored by half the damage taken by the target.", 10, Type.GRASS, Category.SPECIAL);
			super.moveTypes.add("SapHealth");
			super.power = 75;
			super.accuracy = 100;
		}
	}

	private static class Aromatherapy extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Aromatherapy()
		{
			super("Aromatherapy", "The user releases a soothing scent that heals all status problems affecting the user's party.", 5, Type.GRASS, Category.STATUS);
			super.selfTarget = true;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			for (ActivePokemon p : b.getTrainer(me.user()).getTeam())
			{
				if (!p.hasStatus(StatusCondition.FAINTED)) p.removeStatus();
			}
			b.addMessage("All status problems were cured!");
		}
	}

	private static class Spore extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Spore()
		{
			super("Spore", "The user scatters bursts of spores that induce sleep.", 15, Type.GRASS, Category.STATUS);
			super.accuracy = 100;
			super.status = StatusCondition.ASLEEP;
		}
	}

	private static class CrossPoison extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public CrossPoison()
		{
			super("Cross Poison", "A slashing attack with a poisonous blade that may also leave the target poisoned. Critical hits land more easily.", 20, Type.POISON, Category.PHYSICAL);
			super.moveTypes.add("HighCritRatio");
			super.status = StatusCondition.POISONED;
			super.effectChance = 10;
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class XScissor extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public XScissor()
		{
			super("X-scissor", "The user slashes at the target by crossing its scythes or claws as if they were a pair of scissors.", 15, Type.BUG, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Foresight extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Foresight()
		{
			super("Foresight", "Enables a Ghost-type target to be hit by Normal and Fighting type attacks. It also enables an evasive target to be hit.", 40, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect("Foresight", EffectType.POKEMON));
			super.moveTypes.add("SubstitutePiercing");
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
			super("Odor Sleuth", "Enables a Ghost-type target to be hit with Normal- and Fighting-type attacks. It also enables an evasive target to be hit.", 40, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect("Foresight", EffectType.POKEMON));
			super.moveTypes.add("SubstitutePiercing");
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
			super("Miracle Eye", "Enables a Dark-type target to be hit by Psychic-type attacks. It also enables an evasive target to be hit.", 40, Type.PSYCHIC, Category.STATUS);
			super.moveTypes.add("SubstitutePiercing");
			super.effects.add(Effect.getEffect("MiracleEye", EffectType.POKEMON));
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
			super("Howl", "The user howls loudly to raise its spirit, boosting its Attack stat.", 40, Type.NORMAL, Category.STATUS);
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.selfTarget = true;
		}
	}

	private static class SignalBeam extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SignalBeam()
		{
			super("Signal Beam", "The user attacks with a sinister beam of light. It may also confuse the target.", 15, Type.BUG, Category.SPECIAL);
			super.accuracy = 100;
			super.power = 75;
			super.effects.add(Effect.getEffect("Confusion", EffectType.POKEMON));
			super.effectChance = 10;
		}
	}

	private static class ZenHeadbutt extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public ZenHeadbutt()
		{
			super("Zen Headbutt", "The user focuses its willpower to its head and attacks the target. It may also make the target flinch.", 15, Type.PSYCHIC, Category.PHYSICAL);
			super.power = 80;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.effectChance = 20;
			super.accuracy = 90;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Psychic extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Psychic()
		{
			super("Psychic", "The target is hit by a strong telekinetic force. It may also reduce the target's Sp. Def stat.", 10, Type.PSYCHIC, Category.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
			super.effectChance = 10;
		}
	}

	private static class MudSlap extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MudSlap()
		{
			super("Mud-slap", "The user hurls mud in the target's face to inflict damage and lower its accuracy.", 10, Type.GROUND, Category.SPECIAL);
			super.power = 20;
			super.accuracy = 100;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	private static class Magnitude extends Attack 
	{
		private static final long serialVersionUID = 1L;
		private static int[] chances = {5, 10, 20, 30, 20, 10, 5};
		private static int[] powers = {10, 30, 50, 70, 90, 110, 150};
		private int index;

		public Magnitude()
		{
			super("Magnitude", "The user looses a ground-shaking quake affecting everyone around the user. Its power varies.", 30, Type.GROUND, Category.PHYSICAL);
			super.accuracy = 100;
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return powers[index];
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			b.addMessage("Magnitude "+(index+4)+"!");
			super.apply(me, o, b);
		}

		public void startTurn(Battle b, ActivePokemon me)
		{
			// TODO: There is a small bug that if two Pokemon both use Magnitude in the same turn, it will always be the same index -- We can probably overlook this because its super situational and no one really cares that much but still
			index = Global.getPercentageIndex(chances);
		}
	}

	private static class Bulldoze extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Bulldoze()
		{
			super("Bulldoze", "The user stomps down on the ground and attacks everything in the area. Hit Pok\u00e9mon's Speed stat is reduced.", 20, Type.GROUND, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.statChanges[Stat.SPEED.index()] = -1;
		}
	}

	private static class Dig extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;
		public Dig()
		{
			super("Dig", "The user burrows, then attacks on the second turn. It can also be used to exit dungeons.", 10, Type.GROUND, Category.PHYSICAL);
			super.accuracy = 100;
			super.power = 80;
			super.moveTypes.add("SleepTalkFail");
			super.moveTypes.add("PhysicalContact");
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
			b.addMessage(user.getName()+" went underground!");
		}
	}

	private static class Earthquake extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Earthquake()
		{
			super("Earthquake", "The user sets off an earthquake that strikes those around it.", 10, Type.GROUND, Category.PHYSICAL);
			super.power = 100;
			super.accuracy = 100;
			super.moveTypes.add("HitDig");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			if (o.isSemiInvulnerable() && o.getAttack().getName().equals("Dig")) return super.power*2;
			return super.power;
		}
	}

	private static class Fissure extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Fissure()
		{
			super("Fissure", "The user opens up a fissure in the ground and drops the target in. The target instantly faints if it hits.", 5, Type.GROUND, Category.PHYSICAL);
			super.moveTypes.add("OneHitKO");
			super.accuracy = 30;
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (me.getLevel() < o.getLevel())
			{
				b.addMessage("...but it failed!");
				return -1;
			}

			if (o.hasAbility("Sturdy") && !me.breaksTheMold())
			{
				b.addMessage(o.getName()+"'s Sturdy prevents OHKO moves!");
				return -1;
			}

			if (super.zeroAdvantage(b, me, o)) return -1;
			b.addMessage("It's a One-Hit KO!");
			return b.applyDamage(o, o.getHP());
		}

		public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.accuracy + (me.getLevel() - o.getLevel());
		}
	}

	private static class NightSlash extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public NightSlash()
		{
			super("Night Slash", "The user slashes the target the instant an opportunity arises. Critical hits land more easily.", 15, Type.DARK, Category.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add("HighCritRatio");
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class TriAttack extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public TriAttack()
		{
			super("Tri Attack", "The user strikes with a simultaneous three-beam attack. May also burn, freeze, or leave the target with paralysis.", 10, Type.NORMAL, Category.SPECIAL);
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
			super("Fake Out", "An attack that hits first and makes the target flinch. It only works the first turn the user is in battle.", 10, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 100;
			super.priority = 3;
			super.power = 40;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.moveTypes.add("PhysicalContact");
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (!me.getAttributes().isFirstTurn()) b.addMessage("...but it failed!");
			else super.apply(me, o, b);
		}
	}

	private static class FaintAttack extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FaintAttack()
		{
			super("Faint Attack", "The user approaches the target disarmingly, then throws a sucker punch. It hits without fail.", 20, Type.DARK, Category.PHYSICAL);
			super.power = 60;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Taunt extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Taunt()
		{
			super("Taunt", "The target is taunted into a rage that allows it to use only attack moves for three turns.", 20, Type.DARK, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("Taunt", EffectType.POKEMON));
			super.moveTypes.add("SubstitutePiercing");
		}
	}

	private static class PayDay extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public PayDay()
		{
			super("Pay Day", "Numerous coins are hurled at the target to inflict damage. Money is earned after the battle.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("PayDay", EffectType.TEAM));
			super.selfTarget = true;
		}
	}

	private static class PowerGem extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public PowerGem()
		{
			super("Power Gem", "The user attacks with a ray of light that sparkles as if it were made of gemstones.", 20, Type.ROCK, Category.SPECIAL);
			super.power = 70;
			super.accuracy = 100;
		}
	}

	private static class WaterSport extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public WaterSport()
		{
			super("Water Sport", "The user soaks itself with water. The move weakens Fire-type moves while the user is in the battle.", 15, Type.WATER, Category.STATUS);
			super.moveTypes.add("Field");
			super.effects.add(Effect.getEffect("WaterSport", EffectType.BATTLE));
		}
	}

	private static class Soak extends Attack implements ChangeTypeMove
	{
		private static final long serialVersionUID = 1L;
		public Soak()
		{
			super("Soak", "The user shoots a torrent of water at the target and changes the target's type to Water.", 20, Type.WATER, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("ChangeType", EffectType.POKEMON));
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (Ability.blockDamage(b, user, victim)) return;
			else super.applyEffects(b, user, victim);
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return new Type[] {Type.WATER, Type.NONE};
		}
	}

	private static class PsychUp extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public PsychUp()
		{
			super("Psych Up", "The user hypnotizes itself into copying any stat change made by the target.", 10, Type.NORMAL, Category.STATUS);
			super.moveTypes.add("NoMagicCoat");
			super.moveTypes.add("ProtectPiercing");
			super.moveTypes.add("SubstitutePiercing");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			for (int i = 0; i < Stat.NUM_BATTLE_STATS; i++) user.getAttributes().setStage(i, victim.getStage(i));
			b.addMessage(user.getName()+" copied "+victim.getName()+"'s stat changes!");
		}
	}

	private static class Amnesia extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Amnesia()
		{
			super("Amnesia", "The user temporarily empties its mind to forget its concerns. It sharply raises the user's Sp. Def stat.", 20, Type.PSYCHIC, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.SP_DEFENSE.index()] = 2;
		}
	}

	private static class WonderRoom extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public WonderRoom()
		{
			super("Wonder Room", "The user creates a bizarre area in which Pok\u00e9mon's Defense and Sp. Def stats are swapped for five turns.", 10, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect("WonderRoom", EffectType.BATTLE));
			super.moveTypes.add("Field");
			super.priority = -7;
		}
	}

	private static class AquaJet extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public AquaJet()
		{
			super("Aqua Jet", "The user lunges at the target at a speed that makes it almost invisible. It is sure to strike first.", 20, Type.WATER, Category.PHYSICAL);
			super.priority = 1;
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Covet extends Attack implements ItemCondition
	{
		private static final long serialVersionUID = 1L;
		private Item item;

		public Covet()
		{
			super("Covet", "The user endearingly approaches the target, then steals the target's held item.", 40, Type.NORMAL, Category.PHYSICAL);
			super.power = 60;
			super.moveTypes.add("Metronomeless");
			super.accuracy = 100;
			super.moveTypes.add("Assistless");
			super.effects.add(Effect.getEffect("ChangeItem", EffectType.POKEMON));
			super.moveTypes.add("PhysicalContact");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isHoldingItem(b) || !victim.isHoldingItem(b) || b.getTrainer(user.user()) instanceof WildPokemon || victim.hasAbility("Sticky Hold"))
			{
				if (super.category == Category.STATUS) b.addMessage("...but it failed!");
				return;
			}

			Item userItem = user.getHeldItem(b), victimItem = victim.getHeldItem(b);
			b.addMessage(user.getName()+" stole "+victim.getName()+"'s "+victimItem.getName()+"!");

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
			super("Low Kick", "A powerful low kick that makes the target fall over. It inflicts greater damage on heavier targets.", 20, Type.FIGHTING, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
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

	private static class KarateChop extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public KarateChop()
		{
			super("Karate Chop", "The target is attacked with a sharp chop. Critical hits land more easily.", 25, Type.FIGHTING, Category.PHYSICAL);
			super.moveTypes.add("HighCritRatio");
			super.accuracy = 100;
			super.power = 50;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class SeismicToss extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SeismicToss()
		{
			super("Seismic Toss", "The target is thrown using the power of gravity. It inflicts damage equal to the user's level.", 20, Type.FIGHTING, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			return b.applyDamage(o, me.getLevel());
		}
	}

	private static class Swagger extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Swagger()
		{
			super("Swagger", "The user enrages and confuses the target. However, it also sharply raises the target's Attack stat.", 15, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect("Confusion", EffectType.POKEMON));
			super.statChanges[Stat.ATTACK.index()] = 2;
			super.accuracy = 90;
		}
	}

	private static class CrossChop extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public CrossChop()
		{
			super("Cross Chop", "The user delivers a double chop with its forearms crossed. Critical hits land more easily.", 5, Type.FIGHTING, Category.PHYSICAL);
			super.power = 100;
			super.accuracy = 80;
			super.moveTypes.add("HighCritRatio");
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Punishment extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Punishment()
		{
			super("Punishment", "This attack's power increases the more the target has powered up with stat changes.", 5, Type.DARK, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return (int)Math.min(super.power + 20*me.getAttributes().totalStatIncreases(), 200);
		}
	}

	private static class CloseCombat extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public CloseCombat()
		{
			super("Close Combat", "The user fights the target up close without guarding itself. It also cuts the user's Defense and Sp. Def.", 5, Type.FIGHTING, Category.PHYSICAL);
			super.accuracy = 100;
			super.power = 120;
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class FlameWheel extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FlameWheel()
		{
			super("Flame Wheel", "The user cloaks itself in fire and charges at the target. It may also leave the target with a burn.", 25, Type.FIRE, Category.PHYSICAL);
			super.moveTypes.add("Defrost");
			super.power = 60;
			super.accuracy = 100;
			super.status = StatusCondition.BURNED;
			super.effectChance = 10;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Reversal extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Reversal()
		{
			super("Reversal", "An all-out attack that becomes more powerful the less HP the user has.", 15, Type.FIGHTING, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
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

	private static class Extremespeed extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Extremespeed()
		{
			super("Extremespeed", "The user charges the target at blinding speed. This attack always goes before any other move.", 5, Type.NORMAL, Category.PHYSICAL);
			super.priority = 2;
			super.power = 80;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Hypnosis extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Hypnosis()
		{
			super("Hypnosis", "The user employs hypnotic suggestion to make the target fall into a deep sleep.", 20, Type.PSYCHIC, Category.STATUS);
			super.accuracy = 60;
			super.status = StatusCondition.ASLEEP;
		}
	}

	private static class Bubblebeam extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Bubblebeam()
		{
			super("Bubblebeam", "A spray of bubbles is forcefully ejected at the opposing team. It may also lower their Speed stats.", 20, Type.WATER, Category.SPECIAL);
			super.statChanges[Stat.SPEED.index()] = -1;
			super.effectChance = 10;
			super.power = 65;
			super.accuracy = 100;
		}
	}

	private static class MudShot extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MudShot()
		{
			super("Mud Shot", "The user attacks by hurling a blob of mud at the target. It also reduces the target's Speed.", 15, Type.GROUND, Category.SPECIAL);
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
			super("Belly Drum", "The user maximizes its Attack stat in exchange for HP equal to half its max HP.", 10, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.getStage(Stat.ATTACK.index()) == Stat.MAX_STAT_CHANGES || user.getHP() < user.getStat(Stat.HP)/2.0)
			{
				b.addMessage("...but it failed!");
				return;
			}
			b.addMessage(user.getName()+" cut its own HP and maximized its attack!");
			user.reduceHealthFraction(b, 1/2.0);
			user.getAttributes().setStage(Stat.ATTACK.index(), Stat.MAX_STAT_CHANGES);
		}
	}

	private static class Submission extends Attack implements RecoilMove
	{
		private static final long serialVersionUID = 1L;
		private int recoilDamage(ActivePokemon user, int damage)
		{
			return (int)Math.ceil(damage/4.0);
		}

		public Submission()
		{
			super("Submission", "The user grabs the target and recklessly dives for the ground. It also hurts the user slightly.", 25, Type.FIGHTING, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 80;
			super.moveTypes.add("PhysicalContact");
		}

		public void applyRecoil(Battle b, ActivePokemon user, int damage)
		{
			if (user.hasAbility("Rock Head") || user.hasAbility("Magic Guard")) return;
			b.addMessage(user.getName()+" was hurt by recoil!");
			b.applyDamage(user, recoilDamage(user, damage));
		}
	}

	private static class Dynamicpunch extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Dynamicpunch()
		{
			super("Dynamicpunch", "The user punches the target with full, concentrated power. It confuses the target if it hits.", 5, Type.FIGHTING, Category.PHYSICAL);
			super.power = 100;
			super.accuracy = 50;
			super.effects.add(Effect.getEffect("Confusion", EffectType.POKEMON));
			super.moveTypes.add("Punching");
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class MindReader extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MindReader()
		{
			super("Mind Reader", "The user senses the target's movements with its mind to ensure its next attack does not miss the target.", 5, Type.NORMAL, Category.STATUS);
			super.moveTypes.add("NonSnatchable");
			super.selfTarget = true;
			super.effects.add(Effect.getEffect("LockOn", EffectType.POKEMON));
		}
	}

	private static class LockOn extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public LockOn()
		{
			super("Lock-on", "The user takes sure aim at the target. It ensures the next attack does not fail to hit the target.", 5, Type.NORMAL, Category.STATUS);
			super.moveTypes.add("NonSnatchable");
			super.selfTarget = true;
			super.effects.add(Effect.getEffect("LockOn", EffectType.POKEMON));
		}
	}

	private static class Kinesis extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Kinesis()
		{
			super("Kinesis", "The user distracts the target by bending a spoon. It lowers the target's accuracy.", 15, Type.PSYCHIC, Category.STATUS);
			super.accuracy = 80;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	private static class Barrier extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Barrier()
		{
			super("Barrier", "The user throws up a sturdy wall that sharply raises its Defense stat.", 30, Type.PSYCHIC, Category.STATUS);
			super.statChanges[Stat.DEFENSE.index()] = 2;
			super.selfTarget = true;
		}
	}

	private static class Telekinesis extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Telekinesis()
		{
			super("Telekinesis", "The user makes the target float with its psychic power. The target is easier to hit for three turns.", 15, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect("Telekinesis", EffectType.POKEMON));
			super.moveTypes.add("Airborne");
		}
	}

	private static class Ingrain extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Ingrain()
		{
			super("Ingrain", "The user lays roots that restore its HP on every turn. Because it is rooted, it can't switch out.", 20, Type.GRASS, Category.STATUS);
			super.selfTarget = true;
			super.effects.add(Effect.getEffect("Ingrain", EffectType.POKEMON));
		}
	}

	private static class PsychoCut extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public PsychoCut()
		{
			super("Psycho Cut", "The user tears at the target with blades formed by psychic power. Critical hits land more easily.", 20, Type.PSYCHIC, Category.PHYSICAL);
			super.moveTypes.add("HighCritRatio");
			super.accuracy = 100;
			super.power = 70;
		}
	}

	private static class FutureSight extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FutureSight()
		{
			super("Future Sight", "Two turns after this move is used, a hunk of psychic energy attacks the target.", 10, Type.PSYCHIC, Category.SPECIAL);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("FutureSight", EffectType.TEAM));
			super.power = 100;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			super.applyEffects(b, me, o); // Don't apply damage just yet!!
		}
	}

	private static class DoomDesire extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public DoomDesire()
		{
			super("Doom Desire", "Two turns after this move is used, the user blasts the target with a concentrated bundle of light.", 5, Type.STEEL, Category.SPECIAL);
			super.power = 140;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("DoomDesire", EffectType.TEAM));
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			super.applyEffects(b, me, o); // Don't apply damage just yet!!
		}
	}

	private static class CalmMind extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public CalmMind()
		{
			super("Calm Mind", "The user quietly focuses its mind and calms its spirit to raise its Sp. Atk and Sp. Def stats.", 20, Type.PSYCHIC, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
		}
	}

	private static class LowSweep extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public LowSweep()
		{
			super("Low Sweep", "The user attacks the target's legs swiftly, reducing the target's Speed stat.", 20, Type.FIGHTING, Category.PHYSICAL);
			super.accuracy = 100;
			super.power = 60;
			super.statChanges[Stat.SPEED.index()] = -1;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Revenge extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Revenge()
		{
			super("Revenge", "An attack move that inflicts double the damage if the user has been hurt by the opponent in the same turn.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.priority = -4;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			if (me.getAttributes().hasTakenDamage()) return super.power*2;
			return super.power;
		}
	}

	private static class VitalThrow extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public VitalThrow()
		{
			super("Vital Throw", "The user attacks last. In return, this throw move is guaranteed not to miss.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.priority = -1;
			super.power = 70;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class WringOut extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public WringOut()
		{
			super("Wring Out", "The user powerfully wrings the target. The more HP the target has, the greater this attack's power.", 5, Type.NORMAL, Category.SPECIAL);
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return (int)(120*o.getHPRatio());
		}
	}

	private static class LeafTornado extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public LeafTornado()
		{
			super("Leaf Tornado", "The user attacks its target by encircling it in sharp leaves. This attack may also lower the target's accuracy.", 10, Type.GRASS, Category.SPECIAL);
			super.accuracy = 90;
			super.power = 65;
			super.statChanges[Stat.ACCURACY.index()] = -1;
			super.effectChance = 30;
		}
	}

	private static class LeafStorm extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public LeafStorm()
		{
			super("Leaf Storm", "The user whips up a storm of leaves around the target. The attack's recoil harshly reduces the user's Sp. Atk stat.", 5, Type.GRASS, Category.SPECIAL);
			super.power = 140;
			super.accuracy = 90;
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = -2;
		}
	}

	private static class LeafBlade extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public LeafBlade()
		{
			super("Leaf Blade", "The user handles a sharp leaf like a sword and attacks by cutting its target. Critical hits land more easily.", 15, Type.GRASS, Category.PHYSICAL);
			super.power = 90;
			super.accuracy = 100;
			super.moveTypes.add("HighCritRatio");
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Constrict extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Constrict()
		{
			super("Constrict", "The target is attacked with long, creeping tentacles or vines. It may also lower the target's Speed stat.", 35, Type.NORMAL, Category.PHYSICAL);
			super.power = 10;
			super.accuracy = 100;
			super.statChanges[Stat.SPEED.index()] = -1;
			super.effectChance = 10;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Hex extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Hex()
		{
			super("Hex", "This relentless attack does massive damage to a target affected by status problems.", 10, Type.GHOST, Category.SPECIAL);
			super.power = 50;
			super.accuracy = 100;
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return o.hasStatus() ? super.power*2 : super.power;
		}
	}

	private static class SludgeWave extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SludgeWave()
		{
			super("Sludge Wave", "It swamps the area around the user with a giant sludge wave. It may also poison those hit.", 10, Type.POISON, Category.SPECIAL);
			super.accuracy = 100;
			super.power = 95;
			super.status = StatusCondition.POISONED;
			super.effectChance = 10;
		}
	}

	private static class MudSport extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MudSport()
		{
			super("Mud Sport", "The user covers itself with mud. It weakens Electric-type moves while the user is in the battle.", 15, Type.GROUND, Category.STATUS);
			super.effects.add(Effect.getEffect("MudSport", EffectType.BATTLE));
			super.moveTypes.add("Field");
		}
	}

	private static class RockPolish extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public RockPolish()
		{
			super("Rock Polish", "The user polishes its body to reduce drag. It can sharply raise the Speed stat.", 20, Type.ROCK, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.SPEED.index()] = 2;
		}
	}

	private static class RockThrow extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public RockThrow()
		{
			super("Rock Throw", "The user picks up and throws a small rock at the target to attack.", 15, Type.ROCK, Category.PHYSICAL);
			super.power = 50;
			super.accuracy = 90;
		}
	}

	private static class RockBlast extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public RockBlast()
		{
			super("Rock Blast", "The user hurls hard rocks at the target. Two to five rocks are launched in quick succession.", 10, Type.ROCK, Category.PHYSICAL);
			super.power = 25;
			super.accuracy = 90;
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			int damage = 0, hits = (int)(Math.random()*(5-2+1))+2;
			if (me.hasAbility("Skill Link")) hits = 5;
			for (int i = 1; i <= hits; i++)
			{
				b.addMessage("Hit "+i+"!");
				damage += super.applyDamage(me, o, b);
			}
			b.addMessage("Hit "+hits+" times!");
			return damage;
		}
	}

	private static class SmackDown extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SmackDown()
		{
			super("Smack Down", "The user throws a stone or projectile to attack an opponent. A flying Pok\u00e9mon will fall to the ground when hit.", 15, Type.ROCK, Category.PHYSICAL);
			super.effects.add(Effect.getEffect("Grounded", EffectType.POKEMON));
			super.moveTypes.add("HitFly");
			super.power = 50;
			super.accuracy = 100;
		}
	}

	private static class StealthRock extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public StealthRock()
		{
			super("Stealth Rock", "The user lays a trap of levitating stones around the opponent's team. The trap hurts opponents that switch into battle.", 20, Type.ROCK, Category.STATUS);
			super.effects.add(Effect.getEffect("StealthRock", EffectType.TEAM));
			super.moveTypes.add("Field");
		}
	}

	private static class StoneEdge extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public StoneEdge()
		{
			super("Stone Edge", "The user stabs the foe with sharpened stones from below. It has a high critical-hit ratio.", 5, Type.ROCK, Category.PHYSICAL);
			super.accuracy = 80;
			super.power = 100;
			super.moveTypes.add("HighCritRatio");
		}
	}

	private static class Steamroller extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Steamroller()
		{
			super("Steamroller", "The user crushes its targets by rolling over them with its rolled-up body. This attack may make the target flinch.", 20, Type.BUG, Category.PHYSICAL);
			super.accuracy = 100;
			super.power = 65;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.effectChance = 30;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return o.hasEffect("UsedMinimize") ? super.power*2 : super.power;
		}
	}

	private static class HeavySlam extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public HeavySlam()
		{
			super("Heavy Slam", "The user slams into the target with its heavy body. The more the user outweighs the target, the greater its damage.", 10, Type.STEEL, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			double ratio = o.getWeight(b)/me.getWeight(b);
			if (ratio > .5) return 40;
			if (ratio > .33) return 60;
			if (ratio > .25) return 80;
			if (ratio > .2) return 100;
			return 120;
		}
	}

	private static class Stomp extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Stomp()
		{
			super("Stomp", "The target is stomped with a big foot. It may also make the target flinch.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 65;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.effectChance = 30;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class FlameCharge extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FlameCharge()
		{
			super("Flame Charge", "The user cloaks itself with flame and attacks. Building up more power, it raises the user's Speed stat.", 20, Type.FIRE, Category.PHYSICAL);
			super.power = 50;
			super.accuracy = 100;
			super.statChanges[Stat.SPEED.index()] = 1;
			super.selfTarget = true;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Bounce extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;
		public Bounce()
		{
			super("Bounce", "The user bounces up high, then drops on the target on the second turn. It may also leave the target with paralysis.", 5, Type.FLYING, Category.PHYSICAL);
			super.power = 85;
			super.moveTypes.add("SleepTalkFail");
			super.accuracy = 85;
			super.status = StatusCondition.PARALYZED;
			super.effectChance = 30;
			super.moveTypes.add("Airborne");
			super.moveTypes.add("PhysicalContact");
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
			b.addMessage(user.getName()+" sprang up!");
		}
	}

	private static class Curse extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Curse()
		{
			super("Curse", "A move that works differently for the Ghost type than for all other types.", 10, Type.GHOST, Category.STATUS);
			super.effects.add(Effect.getEffect("Curse", EffectType.POKEMON));
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.statChanges[Stat.SPEED.index()] = -1;
			super.moveTypes.add("ProtectPiercing");
			super.moveTypes.add("SubstitutePiercing");
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (me.isType(Type.GHOST))
			{
				if (effects.get(0).applies(b, me, o, CastSource.ATTACK)) effects.get(0).cast(b, me, o, CastSource.ATTACK, super.printCast);
				else b.addMessage("...but it failed!");
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
			super("Yawn", "The user lets loose a huge yawn that lulls the target into falling asleep on the next turn.", 10, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect("Yawn", EffectType.POKEMON));
		}
	}

	private static class Headbutt extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Headbutt()
		{
			super("Headbutt", "The user sticks out its head and attacks by charging straight into the target. It may also make the target flinch.", 15, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 100;
			super.power = 70;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.effectChance = 30;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class SlackOff extends Attack implements SelfHealingMove
	{
		private static final long serialVersionUID = 1L;
		public SlackOff()
		{
			super("Slack Off", "The user slacks off, restoring its own HP by up to half of its maximum HP.", 10, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect("HealBlock"))
			{
				b.addMessage("...but it failed!");
				return;
			}

			victim.healHealthFraction(1/2.0);
			b.addMessage(victim.getName()+"'s health was restored!", victim.getHP(), victim.user());
		}
	}

	private static class HealPulse extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public HealPulse()
		{
			super("Heal Pulse", "The user emits a healing pulse which restores the target's HP by up to half of its max HP.", 10, Type.PSYCHIC, Category.STATUS);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (victim.fullHealth() || victim.hasEffect("HealPulse"))
			{
				b.addMessage("...but it failed!");
				return;
			}
			victim.healHealthFraction(1/2.0);
			b.addMessage(victim.getName()+"'s health was restored!", victim.getHP(), victim.user());
		}
	}

	private static class MetalSound extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MetalSound()
		{
			super("Metal Sound", "A horrible sound like scraping metal harshly reduces the target's Sp. Def stat.", 40, Type.STEEL, Category.STATUS);
			super.accuracy = 85;
			super.statChanges[Stat.SP_DEFENSE.index()] = -2;
			super.moveTypes.add("SoundBased");
		}
	}

	private static class Spark extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Spark()
		{
			super("Spark", "The user throws an electrically charged tackle at the target. It may also leave the target with paralysis.", 20, Type.ELECTRIC, Category.PHYSICAL);
			super.accuracy = 100;
			super.power = 65;
			super.status = StatusCondition.PARALYZED;
			super.effectChance = 30;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class MagnetBomb extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MagnetBomb()
		{
			super("Magnet Bomb", "The user launches steel bombs that stick to the target. This attack will not miss.", 20, Type.STEEL, Category.PHYSICAL);
			super.power = 60;
		}
	}

	private static class MirrorShot extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MirrorShot()
		{
			super("Mirror Shot", "The user looses a flash of energy at the target from its polished body. It may also lower the target's accuracy.", 10, Type.STEEL, Category.SPECIAL);
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
			super("Magnet Rise", "The user levitates using electrically generated magnetism for five turns.", 10, Type.ELECTRIC, Category.STATUS);
			super.effects.add(Effect.getEffect("MagnetRise", EffectType.POKEMON));
			super.selfTarget = true;
			super.moveTypes.add("Airborne");
		}
	}

	private static class ZapCannon extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public ZapCannon()
		{
			super("Zap Cannon", "The user fires an electric blast like a cannon to inflict damage and cause paralysis.", 5, Type.ELECTRIC, Category.SPECIAL);
			super.power = 120;
			super.accuracy = 50;
			super.status = StatusCondition.PARALYZED;
		}
	}

	private static class BraveBird extends Attack implements RecoilMove
	{
		private static final long serialVersionUID = 1L;
		private int recoilDamage(ActivePokemon user, int damage)
		{
			return (int)Math.ceil(damage/3.0);
		}

		public BraveBird()
		{
			super("Brave Bird", "The user tucks in its wings and charges from a low altitude. The user also takes serious damage.", 15, Type.FLYING, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public void applyRecoil(Battle b, ActivePokemon user, int damage)
		{
			if (user.hasAbility("Rock Head") || user.hasAbility("Magic Guard")) return;
			b.addMessage(user.getName()+" was hurt by recoil!");
			b.applyDamage(user, recoilDamage(user, damage));
		}
	}

	private static class Uproar extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Uproar()
		{
			super("Uproar", "The user attacks in an uproar for three turns. Over that time, no one can fall asleep.", 10, Type.NORMAL, Category.SPECIAL);
			super.moveTypes.add("SoundBased");
			super.moveTypes.add("SleepTalkFail");
			super.power = 90;
			super.accuracy = 100;
			super.selfTarget = true;
			super.effects.add(Effect.getEffect("Uproar", EffectType.POKEMON));
		}
	}

	private static class Acupressure extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Acupressure()
		{
			super("Acupressure", "The user applies pressure to stress points, sharply boosting one of its stats.", 30, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			super.statChanges = new int[Stat.NUM_BATTLE_STATS];
			super.statChanges[(int)(Math.random()*Stat.NUM_BATTLE_STATS)] = 2;
			super.apply(me, o, b);
		}
	}

	private static class DoubleHit extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public DoubleHit()
		{
			super("Double Hit", "The user slams the target with a long tail, vines, or tentacle. The target is hit twice in a row.", 10, Type.NORMAL, Category.PHYSICAL);
			super.power = 35;
			super.accuracy = 90;
			super.moveTypes.add("PhysicalContact");
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			int damage = 0, hits = (int)(Math.random()*(2-2+1))+2;
			for (int i = 1; i <= hits; i++)
			{
				b.addMessage("Hit "+i+"!");
				damage += super.applyDamage(me, o, b);
			}
			b.addMessage("Hit "+hits+" times!");
			return damage;
		}
	}

	private static class IcyWind extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public IcyWind()
		{
			super("Icy Wind", "The user attacks with a gust of chilled air. It also reduces the targets' Speed stat.", 15, Type.ICE, Category.SPECIAL);
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
			super("Ice Shard", "The user flash freezes chunks of ice and hurls them at the target. This move always goes first.", 30, Type.ICE, Category.PHYSICAL);
			super.power = 40;
			super.priority = 1;
			super.accuracy = 100;
		}
	}

	private static class AquaRing extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public AquaRing()
		{
			super("Aqua Ring", "The user envelops itself in a veil made of water. It regains some HP on every turn.", 20, Type.WATER, Category.STATUS);
			super.selfTarget = true;
			super.effects.add(Effect.getEffect("AquaRing", EffectType.POKEMON));
		}
	}

	private static class AuroraBeam extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public AuroraBeam()
		{
			super("Aurora Beam", "The target is hit with a rainbow-colored beam. This may also lower the target's Attack stat.", 20, Type.ICE, Category.SPECIAL);
			super.power = 65;
			super.accuracy = 100;
			super.statChanges[Stat.ATTACK.index()] = -1;
			super.effectChance = 10;
		}
	}

	private static class Brine extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Brine()
		{
			super("Brine", "If the target's HP is down to about half, this attack will hit with double the power.", 10, Type.WATER, Category.SPECIAL);
			super.accuracy = 100;
			super.power = 65;
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return me.getHPRatio() < .5 ? 2*super.power : super.power;
		}
	}

	private static class Dive extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;
		public Dive()
		{
			super("Dive", "Diving on the first turn, the user floats up and attacks on the second turn. It can be used to dive deep in the ocean.", 10, Type.WATER, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.moveTypes.add("SleepTalkFail");
			super.moveTypes.add("PhysicalContact");
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
			b.addMessage(user.getName()+" hid underwater!");
		}
	}

	private static class IceBeam extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public IceBeam()
		{
			super("Ice Beam", "The target is struck with an icy-cold beam of energy. It may also freeze the target solid.", 10, Type.ICE, Category.SPECIAL);
			super.accuracy = 100;
			super.power = 95;
			super.status = StatusCondition.FROZEN;
			super.effectChance = 10;
		}
	}

	private static class SheerCold extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SheerCold()
		{
			super("Sheer Cold", "The target is attacked with a blast of absolute-zero cold. The target instantly faints if it hits.", 5, Type.ICE, Category.SPECIAL);
			super.moveTypes.add("OneHitKO");
			super.accuracy = 30;
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (me.getLevel() < o.getLevel())
			{
				b.addMessage("...but it failed!");
				return -1;
			}

			if (o.hasAbility("Sturdy") && !me.breaksTheMold())
			{
				b.addMessage(o.getName()+"'s Sturdy prevents OHKO moves!");
				return -1;
			}

			if (super.zeroAdvantage(b, me, o)) return -1;
			b.addMessage("It's a One-Hit KO!");
			return b.applyDamage(o, o.getHP());
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
			super("Poison Gas", "A cloud of poison gas is sprayed in the face of opposing Pok\u00e9mon. It may poison those hit.", 40, Type.POISON, Category.STATUS);
			super.accuracy = 80;
			super.status = StatusCondition.POISONED;
		}
	}

	private static class Sludge extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Sludge()
		{
			super("Sludge", "Unsanitary sludge is hurled at the target. It may also poison the target.", 20, Type.POISON, Category.SPECIAL);
			super.power = 65;
			super.accuracy = 100;
			super.power = 65;
			super.status = StatusCondition.POISONED;
			super.effectChance = 30;
		}
	}

	private static class SludgeBomb extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SludgeBomb()
		{
			super("Sludge Bomb", "Unsanitary sludge is hurled at the target. It may also poison the target.", 10, Type.POISON, Category.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
			super.status = StatusCondition.POISONED;
			super.effectChance = 30;
		}
	}

	private static class AcidArmor extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public AcidArmor()
		{
			super("Acid Armor", "The user alters its cellular structure to liquefy itself, sharply raising its Defense stat.", 40, Type.POISON, Category.STATUS);
			super.statChanges[Stat.DEFENSE.index()] = 2;
			super.selfTarget = true;
		}
	}

	private static class IcicleSpear extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public IcicleSpear()
		{
			super("Icicle Spear", "The user launches sharp icicles at the target. It strikes two to five times in a row.", 30, Type.ICE, Category.PHYSICAL);
			super.power = 25;
			super.accuracy = 100;
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			int damage = 0, hits = (int)(Math.random()*(5-2+1))+2;
			if (me.hasAbility("Skill Link")) hits = 5;
			for (int i = 1; i <= hits; i++)
			{
				b.addMessage("Hit "+i+"!");
				damage += super.applyDamage(me, o, b);
			}
			b.addMessage("Hit "+hits+" times!");
			return damage;
		}
	}

	private static class Clamp extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Clamp()
		{
			super("Clamp", "The target is clamped and squeezed by the user's very thick and sturdy shell for four to five turns.", 15, Type.WATER, Category.PHYSICAL);
			super.accuracy = 85;
			super.power = 35;
			super.effects.add(Effect.getEffect("Clamped", EffectType.POKEMON));
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class RazorShell extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public RazorShell()
		{
			super("Razor Shell", "The user cuts its target with sharp shells. This attack may also lower the target's Defense stat.", 10, Type.WATER, Category.PHYSICAL);
			super.accuracy = 95;
			super.power = 75;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.effectChance = 50;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Whirlpool extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Whirlpool()
		{
			super("Whirlpool", "Traps foes in a violent swirling whirlpool for four to five turns.", 15, Type.WATER, Category.SPECIAL);
			super.power = 35;
			super.accuracy = 85;
			super.effects.add(Effect.getEffect("Whirlpool", EffectType.POKEMON));
		}
	}

	private static class ShellSmash extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public ShellSmash()
		{
			super("Shell Smash", "The user breaks its shell, lowering its Defense and Sp. Def stats but sharply raising Attack, Sp. Atk, and Speed stats.", 15, Type.NORMAL, Category.STATUS);
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
			super("Spike Cannon", "Sharp spikes are shot at the target in rapid succession. They hit two to five times in a row.", 15, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 100;
			super.power = 20;
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			int damage = 0, hits = (int)(Math.random()*(5-2+1))+2;
			if (me.hasAbility("Skill Link")) hits = 5;
			for (int i = 1; i <= hits; i++)
			{
				b.addMessage("Hit "+i+"!");
				damage += super.applyDamage(me, o, b);
			}
			b.addMessage("Hit "+hits+" times!");
			return damage;
		}
	}

	private static class Spikes extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Spikes()
		{
			super("Spikes", "The user lays a trap of spikes at the opposing team's feet. The trap hurts Pok\u00e9mon that switch into battle.", 20, Type.GROUND, Category.STATUS);
			super.moveTypes.add("Field");
			super.effects.add(Effect.getEffect("Spikes", EffectType.TEAM));
		}
	}

	private static class IcicleCrash extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public IcicleCrash()
		{
			super("Icicle Crash", "The user attacks by harshly dropping an icicle onto the target. It may also make the target flinch.", 10, Type.ICE, Category.PHYSICAL);
			super.power = 85;
			super.accuracy = 90;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.effectChance = 30;
		}
	}

	private static class Lick extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Lick()
		{
			super("Lick", "The target is licked with a long tongue, causing damage. It may also leave the target with paralysis.", 30, Type.GHOST, Category.PHYSICAL);
			super.accuracy = 100;
			super.power = 20;
			super.status = StatusCondition.PARALYZED;
			super.effectChance = 30;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Spite extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Spite()
		{
			super("Spite", "The user unleashes its grudge on the move last used by the target by cutting 4 PP from it.", 10, Type.GHOST, Category.STATUS);
			super.accuracy = 100;
			super.moveTypes.add("SubstitutePiercing");
			super.moveTypes.add("SubstitutePiercing");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Move last = victim.getAttributes().getLastMoveUsed();
			if (last == null || last.getPP() == 0 || !victim.hasMove(last.getAttack().getName()))
			{
				b.addMessage("...but it failed!");
				return;
			}
			b.addMessage(victim.getName()+"'s "+last.getAttack().getName()+"'s PP was reduced by "+last.reducePP(4)+"!");
		}
	}

	private static class NightShade extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public NightShade()
		{
			super("Night Shade", "The user makes the target see a frightening mirage. It inflicts damage matching the user's level.", 15, Type.GHOST, Category.SPECIAL);
			super.accuracy = 100;
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			return b.applyDamage(o, me.getLevel());
		}
	}

	private static class ShadowBall extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public ShadowBall()
		{
			super("Shadow Ball", "The user hurls a shadowy blob at the target. It may also lower the target's Sp. Def stat.", 15, Type.GHOST, Category.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
			super.effectChance = 20;
		}
	}

	private static class DreamEater extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public DreamEater()
		{
			super("Dream Eater", "The user eats the dreams of a sleeping target. It absorbs half the damage caused to heal the user's HP.", 15, Type.PSYCHIC, Category.SPECIAL);
			super.power = 100;
			super.accuracy = 100;
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int damage = super.applyDamage(me, o, b);
			if (!me.hasEffect("HealBlock")) me.heal((int)(damage*.5*(me.isHoldingItem(b, "Big Root") ? 1.3 : 1)));
			b.addMessage(o.getName()+"'s dream was eaten!");
			b.addMessage("", o.getHP(), o.user());
			b.addMessage("", me.getHP(), me.user());
			return damage;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (!o.hasStatus(StatusCondition.ASLEEP))
			{
				b.addMessage("...but it failed!");
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
			super("Dark Pulse", "The user releases a horrible aura imbued with dark thoughts. It may also make the target flinch.", 15, Type.DARK, Category.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.effectChance = 20;
		}
	}

	private static class Nightmare extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Nightmare()
		{
			super("Nightmare", "A sleeping target sees a nightmare that inflicts some damage every turn.", 15, Type.GHOST, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("Nightmare", EffectType.POKEMON));
		}
	}

	private static class ShadowPunch extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public ShadowPunch()
		{
			super("Shadow Punch", "The user throws a punch from the shadows. The punch lands without fail.", 20, Type.GHOST, Category.PHYSICAL);
			super.power = 60;
			super.moveTypes.add("Punching");
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Bind extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Bind()
		{
			super("Bind", "Things such as long bodies or tentacles are used to bind and squeeze the target for four to five turns.", 20, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 85;
			super.power = 15;
			super.effects.add(Effect.getEffect("Binded", EffectType.POKEMON));
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class RockTomb extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public RockTomb()
		{
			super("Rock Tomb", "Boulders are hurled at the target. It also lowers the target's Speed by preventing its movement.", 10, Type.ROCK, Category.PHYSICAL);
			super.accuracy = 80;
			super.power = 50;
			super.statChanges[Stat.SPEED.index()] = -1;
		}
	}

	private static class Dragonbreath extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Dragonbreath()
		{
			super("Dragonbreath", "The user exhales a mighty gust that inflicts damage. It may also leave the target with paralysis.", 20, Type.DRAGON, Category.SPECIAL);
			super.accuracy = 100;
			super.power = 60;
			super.status = StatusCondition.PARALYZED;
			super.effectChance = 30;
		}
	}

	private static class IronTail extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public IronTail()
		{
			super("Iron Tail", "The target is slammed with a steel-hard tail. It may also lower the target's Defense stat.", 15, Type.STEEL, Category.PHYSICAL);
			super.accuracy = 75;
			super.power = 100;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.effectChance = 30;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Meditate extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Meditate()
		{
			super("Meditate", "The user meditates to awaken the power deep within its body and raise its Attack stat.", 40, Type.PSYCHIC, Category.STATUS);
			super.statChanges[Stat.ATTACK.index()] = -1;
			super.selfTarget = true;
		}
	}

	private static class Synchronoise extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Synchronoise()
		{
			super("Synchronoise", "Using an odd shock wave, the user inflicts damage on any Pok\u00e9mon of the same type in the area around it.", 15, Type.PSYCHIC, Category.SPECIAL);
			super.accuracy = 100;
			super.power = 70;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			Type[] type = me.getType();
			if (o.isType(type[0]) || (type[1] != Type.NONE && o.isType(type[1]))) super.apply(me, o, b);
			else b.addMessage("...but it failed!");
		}
	}

	private static class Psyshock extends Attack implements StatSwitchingEffect
	{
		private static final long serialVersionUID = 1L;
		public Psyshock()
		{
			super("Psyshock", "The user materializes an odd psychic wave to attack the target. This attack does physical damage.", 10, Type.PSYCHIC, Category.SPECIAL);
			super.accuracy = 100;
			super.power = 80;
		}

		public Stat switchStat(Stat s)
		{
			return s == Stat.SP_DEFENSE ? Stat.DEFENSE : s;
		}
	}

	private static class Vicegrip extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Vicegrip()
		{
			super("Vicegrip", "The target is gripped and squeezed from both sides to inflict damage.", 30, Type.NORMAL, Category.PHYSICAL);
			super.power = 55;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class MetalClaw extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MetalClaw()
		{
			super("Metal Claw", "The target is raked with steel claws. It may also raise the user's Attack stat.", 35, Type.STEEL, Category.PHYSICAL);
			super.power = 50;
			super.accuracy = 95;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.selfTarget = true;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Guillotine extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Guillotine()
		{
			super("Guillotine", "A vicious, tearing attack with big pincers. The target will faint instantly if this attack hits.", 5, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 30;
			super.moveTypes.add("OneHitKO");
			super.moveTypes.add("PhysicalContact");
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (me.getLevel() < o.getLevel())
			{
				b.addMessage("...but it failed!");
				return -1;
			}

			if (o.hasAbility("Sturdy") && !me.breaksTheMold())
			{
				b.addMessage(o.getName()+"'s Sturdy prevents OHKO moves!");
				return -1;
			}

			if (super.zeroAdvantage(b, me, o)) return -1;
			b.addMessage("It's a One-Hit KO!");
			return b.applyDamage(o, o.getHP());
		}

		public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.accuracy + (me.getLevel() - o.getLevel());
		}
	}

	private static class Crabhammer extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Crabhammer()
		{
			super("Crabhammer", "The target is hammered with a large pincer. Critical hits land more easily.", 10, Type.WATER, Category.PHYSICAL);
			super.accuracy = 90;
			super.power = 90;
			super.moveTypes.add("HighCritRatio");
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Flail extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Flail()
		{
			super("Flail", "The user flails about aimlessly to attack. It becomes more powerful the less HP the user has.", 15, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
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
			super("Charge", "The user boosts the power of the Electric move it uses on the next turn. It also raises the user's Sp. Def stat.", 20, Type.ELECTRIC, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
			super.effects.add(Effect.getEffect("Charge", EffectType.POKEMON));
		}
	}

	private static class ChargeBeam extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public ChargeBeam()
		{
			super("Charge Beam", "The user attacks with an electric charge. The user may use any remaining electricity to raise its Sp. Atk stat.", 10, Type.ELECTRIC, Category.SPECIAL);
			super.power = 50;
			super.accuracy = 90;
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
			super.effectChance = 70;
		}
	}

	private static class MirrorCoat extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MirrorCoat()
		{
			super("Mirror Coat", "A retaliation move that counters any special attack, inflicting double the damage taken.", 20, Type.PSYCHIC, Category.SPECIAL);
			super.priority = -5;
			super.accuracy = 100;
			super.moveTypes.add("Assistless");
			super.moveTypes.add("Metronomeless");
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int damageTaken = me.getAttributes().getDamageTaken();
			if (damageTaken == 0 || o.getMove().getAttack().getCategory() != Category.SPECIAL)
			{
				b.addMessage("...but it failed!");
				return;
			}
			if (super.zeroAdvantage(b, me, o)) return;
			b.applyDamage(o, damageTaken*2);
		}
	}

	private static class Counter extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Counter()
		{
			super("Counter", "A retaliation move that counters any physical attack, inflicting double the damage taken.", 20, Type.FIGHTING, Category.PHYSICAL);
			super.moveTypes.add("Metronomeless");
			super.accuracy = 100;
			super.priority = -5;
			super.moveTypes.add("Assistless");
			super.moveTypes.add("PhysicalContact");
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int damageTaken = me.getAttributes().getDamageTaken();
			if (damageTaken == 0 || o.getMove().getAttack().getCategory() != Category.PHYSICAL)
			{
				b.addMessage("...but it failed!");
				return;
			}
			if (super.zeroAdvantage(b, me, o)) return;
			b.applyDamage(o, damageTaken*2);
		}
	}

	private static class Barrage extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Barrage()
		{
			super("Barrage", "Round objects are hurled at the target to strike two to five times in a row.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 15;
			super.accuracy = 85;
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			int damage = 0, hits = (int)(Math.random()*(5-2+1))+2;
			if (me.hasAbility("Skill Link")) hits = 5;
			for (int i = 1; i <= hits; i++)
			{
				b.addMessage("Hit "+i+"!");
				damage += super.applyDamage(me, o, b);
			}
			b.addMessage("Hit "+hits+" times!");
			return damage;
		}
	}

	private static class BulletSeed extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public BulletSeed()
		{
			super("Bullet Seed", "The user forcefully shoots seeds at the target. Two to five seeds are shot in rapid succession.", 30, Type.GRASS, Category.PHYSICAL);
			super.power = 25;
			super.accuracy = 100;
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			int damage = 0, hits = (int)(Math.random()*(5-2+1))+2;
			if (me.hasAbility("Skill Link")) hits = 5;
			for (int i = 1; i <= hits; i++)
			{
				b.addMessage("Hit "+i+"!");
				damage += super.applyDamage(me, o, b);
			}
			b.addMessage("Hit "+hits+" times!");
			return damage;
		}
	}

	private static class EggBomb extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public EggBomb()
		{
			super("Egg Bomb", "A large egg is hurled at the target with maximum force to inflict damage.", 10, Type.NORMAL, Category.PHYSICAL);
			super.power = 100;
			super.accuracy = 75;
		}
	}

	private static class WoodHamer extends Attack implements RecoilMove
	{
		private static final long serialVersionUID = 1L;
		private int recoilDamage(ActivePokemon user, int damage)
		{
			return (int)Math.ceil(damage/3.0);
		}

		public WoodHamer()
		{
			super("Wood Hammer", "The user slams its rugged body into the target to attack. The user also sustains serious damage.", 15, Type.GRASS, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public void applyRecoil(Battle b, ActivePokemon user, int damage)
		{
			if (user.hasAbility("Rock Head") || user.hasAbility("Magic Guard")) return;
			b.addMessage(user.getName()+" was hurt by recoil!");
			b.applyDamage(user, recoilDamage(user, damage));
		}
	}

	private static class BoneClub extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public BoneClub()
		{
			super("Bone Club", "The user clubs the target with a bone. It may also make the target flinch.", 20, Type.GROUND, Category.PHYSICAL);
			super.power = 65;
			super.accuracy = 85;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.effectChance = 10;
		}
	}

	private static class Bonemerang extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Bonemerang()
		{
			super("Bonemerang", "The user throws the bone it holds. The bone loops to hit the target twice, coming and going.", 10, Type.GROUND, Category.PHYSICAL);
			super.accuracy = 90;
			super.power = 50;
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			int damage = 0, hits = (int)(Math.random()*(2-2+1))+2;
			for (int i = 1; i <= hits; i++)
			{
				b.addMessage("Hit "+i+"!");
				damage += super.applyDamage(me, o, b);
			}
			b.addMessage("Hit "+hits+" times!");
			return damage;
		}
	}

	private static class BoneRush extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public BoneRush()
		{
			super("Bone Rush", "The user strikes the target with a hard bone two to five times in a row.", 10, Type.GROUND, Category.PHYSICAL);
			super.accuracy = 90;
			super.power = 25;
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			int damage = 0, hits = (int)(Math.random()*(5-2+1))+2;
			if (me.hasAbility("Skill Link")) hits = 5;
			for (int i = 1; i <= hits; i++)
			{
				b.addMessage("Hit "+i+"!");
				damage += super.applyDamage(me, o, b);
			}
			b.addMessage("Hit "+hits+" times!");
			return damage;
		}
	}

	private static class RollingKick extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public RollingKick()
		{
			super("Rolling Kick", "The user lashes out with a quick, spinning kick. It may also make the target flinch.", 15, Type.FIGHTING, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 85;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.effectChance = 30;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class JumpKick extends Attack implements CrashDamageMove
	{
		private static final long serialVersionUID = 1L;
		public JumpKick()
		{
			super("Jump Kick", "The user jumps up high, then strikes with a kick. If the kick misses, the user hurts itself.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.power = 100;
			super.accuracy = 95;
			super.moveTypes.add("Airborne");
			super.moveTypes.add("PhysicalContact");
		}

		public void crash(Battle b, ActivePokemon user)
		{
			b.addMessage(user.getName()+" kept going and crashed!");
			b.applyDamage(user, user.getStat(Stat.HP)/3);
		}
	}

	private static class BrickBreak extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public BrickBreak()
		{
			super("Brick Break", "The user attacks with a swift chop. It can also break any barrier such as Light Screen and Reflect.", 15, Type.FIGHTING, Category.PHYSICAL);
			super.power = 75;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (Effect.hasEffect(b.getTrainer(!user.user()).getEffects(), "Reflect"))
			{
				b.addMessage(user.getName()+" broke the reflect barrier!");
				Effect.removeEffect(b.getTrainer(!user.user()).getEffects(), "Reflect");
			}
			if (Effect.hasEffect(b.getTrainer(!user.user()).getEffects(), "LightScreen"))
			{
				b.addMessage(user.getName()+" broke the light screen barrier!");
				Effect.removeEffect(b.getTrainer(!user.user()).getEffects(), "LightScreen");
			}
		}
	}

	private static class HiJumpKick extends Attack implements CrashDamageMove
	{
		private static final long serialVersionUID = 1L;
		public HiJumpKick()
		{
			super("Hi Jump Kick", "The target is attacked with a knee kick from a jump. If it misses, the user is hurt instead.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.accuracy = 90;
			super.power = 130;
			super.moveTypes.add("Airborne");
			super.moveTypes.add("PhysicalContact");
		}

		public void crash(Battle b, ActivePokemon user)
		{
			b.addMessage(user.getName()+" kept going and crashed!");
			b.applyDamage(user, user.getStat(Stat.HP)/2);
		}
	}

	private static class BlazeKick extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public BlazeKick()
		{
			super("Blaze Kick", "The user launches a kick that lands a critical hit more easily. It may also leave the target with a burn.", 10, Type.FIRE, Category.PHYSICAL);
			super.accuracy = 90;
			super.power = 85;
			super.moveTypes.add("HighCritRatio");
			super.status = StatusCondition.BURNED;
			super.effectChance = 10;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class MegaKick extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MegaKick()
		{
			super("Mega Kick", "The target is attacked by a kick launched with muscle-packed power.", 5, Type.NORMAL, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 75;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class CometPunch extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public CometPunch()
		{
			super("Comet Punch", "The target is hit with a flurry of punches that strike two to five times in a row.", 15, Type.NORMAL, Category.PHYSICAL);
			super.power = 18;
			super.accuracy = 85;
			super.moveTypes.add("PhysicalContact");
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			int damage = 0, hits = (int)(Math.random()*(5-2+1))+2;
			if (me.hasAbility("Skill Link")) hits = 5;
			for (int i = 1; i <= hits; i++)
			{
				b.addMessage("Hit "+i+"!");
				damage += super.applyDamage(me, o, b);
			}
			b.addMessage("Hit "+hits+" times!");
			return damage;
		}
	}

	private static class MachPunch extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MachPunch()
		{
			super("Mach Punch", "The user throws a punch at blinding speed. It is certain to strike first.", 30, Type.FIGHTING, Category.PHYSICAL);
			super.priority = 1;
			super.power = 40;
			super.moveTypes.add("Punching");
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class BulletPunch extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public BulletPunch()
		{
			super("Bullet Punch", "The user strikes the target with tough punches as fast as bullets. This move always goes first.", 30, Type.STEEL, Category.PHYSICAL);
			super.priority = 1;
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add("Punching");
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class VacuumWave extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public VacuumWave()
		{
			super("Vacuum Wave", "The user whirls its fists to send a wave of pure vacuum at the target. This move always goes first.", 30, Type.FIGHTING, Category.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.priority = 1;
		}
	}

	private static class Thunderpunch extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Thunderpunch()
		{
			super("Thunderpunch", "The target is punched with an electrified fist. It may also leave the target with paralysis.", 15, Type.ELECTRIC, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add("Punching");
			super.power = 75;
			super.status = StatusCondition.PARALYZED;
			super.effectChance = 10;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class IcePunch extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public IcePunch()
		{
			super("Ice Punch", "The target is punched with an icy fist. It may also leave the target frozen.", 15, Type.ICE, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add("Punching");
			super.power = 75;
			super.status = StatusCondition.FROZEN;
			super.effectChance = 10;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class FirePunch extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FirePunch()
		{
			super("Fire Punch", "The target is punched with a fiery fist. It may also leave the target with a burn.", 15, Type.FIRE, Category.PHYSICAL);
			super.moveTypes.add("Punching");
			super.accuracy = 100;
			super.power = 75;
			super.status = StatusCondition.BURNED;
			super.effectChance = 10;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class SkyUppercut extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SkyUppercut()
		{
			super("Sky Uppercut", "The user attacks the target with an uppercut thrown skyward with force.", 15, Type.FIGHTING, Category.PHYSICAL);
			super.power = 85;
			super.moveTypes.add("Punching");
			super.accuracy = 90;
			super.moveTypes.add("HitFly");
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class MegaPunch extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MegaPunch()
		{
			super("Mega Punch", "The target is slugged by a punch thrown with muscle-packed power.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 85;
			super.moveTypes.add("Punching");
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class FocusPunch extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FocusPunch()
		{
			super("Focus Punch", "The user focuses its mind before launching a punch. It will fail if the user is hit before it is used.", 20, Type.FIGHTING, Category.PHYSICAL);
			super.power = 150;
			super.accuracy = 100;
			super.moveTypes.add("Assistless");
			super.moveTypes.add("SleepTalkFail");
			super.moveTypes.add("Metronomeless");
			super.moveTypes.add("Punching");
			super.priority = -3;
			super.effects.add(Effect.getEffect("Focusing", EffectType.POKEMON));
			super.selfTarget = true;
			super.moveTypes.add("PhysicalContact");
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (me.hasEffect("Focusing")) super.applyDamage(me, o, b);
		}

		public void startTurn(Battle b, ActivePokemon me)
		{
			super.applyEffects(b, me, me);
		}
	}

	private static class MeFirst extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MeFirst()
		{
			super("Me First", "The user tries to cut ahead of the target to steal and use the target's intended move with greater power.", 20, Type.NORMAL, Category.STATUS);
			super.moveTypes.add("SleepTalkFail");
			super.moveTypes.add("Assistless");
			super.moveTypes.add("NoMagicCoat");
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (!b.isFirstAttack() || o.getAttack().getCategory() == Category.STATUS)
			{
				b.addMessage("...but it failed!");
				return;
			}
			me.addEffect(PokemonEffect.getEffect("FiddyPercentStronger"));
			me.callNewMove(b, o, new Move(o.getAttack()));
			me.getAttributes().removeEffect("FiddyPercentStronger");
		}
	}

	private static class Refresh extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Refresh()
		{
			super("Refresh", "The user rests to cure itself of a poisoning, burn, or paralysis.", 20, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.hasStatus())
			{
				user.removeStatus();
				b.addMessage(user.getName()+" cured itself of its status condition!", StatusCondition.NONE, user.user());
				return;
			}
			b.addMessage("...but it failed!");
		}
	}

	private static class PowerWhip extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public PowerWhip()
		{
			super("Power Whip", "The user violently whirls its vines or tentacles to harshly lash the target.", 10, Type.GRASS, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 85;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Smog extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Smog()
		{
			super("Smog", "The target is attacked with a discharge of filthy gases. It may also poison the target.", 20, Type.POISON, Category.SPECIAL);
			super.power = 20;
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
			super("Clear Smog", "The user attacks by throwing a clump of special mud. All status changes are returned to normal.", 15, Type.POISON, Category.SPECIAL);
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
			super("Hammer Arm", "The user swings and hits with its strong and heavy fist. It lowers the user's Speed, however.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.power = 100;
			super.accuracy = 90;
			super.moveTypes.add("Punching");
			super.statChanges[Stat.SPEED.index()] = -1;
			super.selfTarget = true;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Softboiled extends Attack implements SelfHealingMove
	{
		private static final long serialVersionUID = 1L;
		public Softboiled()
		{
			super("Softboiled", "The user restores its own HP by up to half of its maximum HP. May also be used in the field to heal HP.", 10, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect("HealBlock"))
			{
				b.addMessage("...but it failed!");
				return;
			}

			victim.healHealthFraction(1/2.0);
			b.addMessage(victim.getName()+"'s health was restored!", victim.getHP(), victim.user());
		}
	}

	private static class Ancientpower extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Ancientpower()
		{
			super("Ancientpower", "The user attacks with a prehistoric power. It may also raise all the user's stats at once.", 5, Type.ROCK, Category.SPECIAL);
			super.power = 60;
			super.accuracy = 100;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
			super.statChanges[Stat.SPEED.index()] = 1;
			super.statChanges[Stat.ACCURACY.index()] = 1;
			super.statChanges[Stat.EVASION.index()] = 1;
			super.selfTarget = true;
			super.effectChance = 10;
		}
	}

	private static class Tickle extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Tickle()
		{
			super("Tickle", "The user tickles the target into laughing, reducing its Attack and Defense stats.", 20, Type.NORMAL, Category.STATUS);
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
			super("Dizzy Punch", "The target is hit with rhythmically launched punches that may also leave it confused.", 10, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add("Punching");
			super.power = 70;
			super.effects.add(Effect.getEffect("Confusion", EffectType.POKEMON));
			super.effectChance = 20;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Outrage extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Outrage()
		{
			super("Outrage", "The user rampages and attacks for two to three turns. It then becomes confused, however.", 10, Type.DRAGON, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("SelfConfusion", EffectType.POKEMON));
			super.selfTarget = true;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class DragonDance extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public DragonDance()
		{
			super("Dragon Dance", "The user vigorously performs a mystic, powerful dance that boosts its Attack and Speed stats.", 20, Type.DRAGON, Category.STATUS);
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.SPEED.index()] = 1;
			super.selfTarget = true;
		}
	}

	private static class DragonPulse extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public DragonPulse()
		{
			super("Dragon Pulse", "The target is attacked with a shock wave generated by the user's gaping mouth.", 10, Type.DRAGON, Category.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
		}
	}

	private static class Waterfall extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Waterfall()
		{
			super("Waterfall", "The user charges at the target and may make it flinch. It can also be used to climb a waterfall.", 15, Type.WATER, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.effectChance = 20;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class ReflectType extends Attack implements ChangeTypeMove
	{
		private static final long serialVersionUID = 1L;
		public ReflectType()
		{
			super("Reflect Type", "The user reflects the target's type, making it the same type as the target.", 15, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
			super.effects.add(Effect.getEffect("ChangeType", EffectType.POKEMON));
			super.moveTypes.add("NonSnatchable");
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return b.getOtherPokemon(caster.user()).getType().clone();
		}
	}

	private static class MagicalLeaf extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MagicalLeaf()
		{
			super("Magical Leaf", "The user scatters curious leaves that chase the target. This attack will not miss.", 20, Type.GRASS, Category.SPECIAL);
			super.power = 60;
		}
	}

	private static class PowerSwap extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public PowerSwap()
		{
			super("Power Swap", "The user employs its psychic power to switch changes to its Attack and Sp. Atk with the target.", 10, Type.PSYCHIC, Category.STATUS);
			super.moveTypes.add("NoMagicCoat");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			int temp = user.getAttributes().getStage(Stat.ATTACK.index());
			user.getAttributes().setStage(Stat.ATTACK.index(), victim.getAttributes().getStage(Stat.ATTACK.index()));
			victim.getAttributes().setStage(Stat.ATTACK.index(), temp);
			temp = user.getAttributes().getStage(Stat.SP_ATTACK.index());
			user.getAttributes().setStage(Stat.SP_ATTACK.index(), victim.getAttributes().getStage(Stat.SP_ATTACK.index()));
			victim.getAttributes().setStage(Stat.SP_ATTACK.index(), temp);
			b.addMessage(user.getName()+" swapped its stats with "+victim.getName()+"!");
		}
	}

	private static class GuardSwap extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public GuardSwap()
		{
			super("Guard Swap", "The user employs its psychic power to switch changes to its Defense and Sp. Def with the target.", 10, Type.PSYCHIC, Category.STATUS);
			super.moveTypes.add("SubstitutePiercing");
			super.moveTypes.add("NoMagicCoat");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			int temp = user.getAttributes().getStage(Stat.DEFENSE.index());
			user.getAttributes().setStage(Stat.DEFENSE.index(), victim.getAttributes().getStage(Stat.DEFENSE.index()));
			victim.getAttributes().setStage(Stat.DEFENSE.index(), temp);
			temp = user.getAttributes().getStage(Stat.SP_DEFENSE.index());
			user.getAttributes().setStage(Stat.SP_DEFENSE.index(), victim.getAttributes().getStage(Stat.SP_DEFENSE.index()));
			victim.getAttributes().setStage(Stat.SP_DEFENSE.index(), temp);
			b.addMessage(user.getName()+" swapped its stats with "+victim.getName()+"!");
		}
	}

	private static class Copycat extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Copycat()
		{
			super("Copycat", "The user mimics the move used immediately before it. The move fails if no other move has been used yet.", 20, Type.NORMAL, Category.STATUS);
			super.moveTypes.add("Metronomeless");
			super.moveTypes.add("NoMagicCoat");
			super.moveTypes.add("SleepTalkFail");
			super.moveTypes.add("Assistless");
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			Move mirror = o.getAttributes().getLastMoveUsed();
			if (mirror == null || mirror.getAttack().getName().equals("Mirror Move") || mirror.getAttack().getName().equals("Copycat"))
			{
				b.addMessage("...but it failed!");
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
			super("Transform", "The user transforms into a copy of the target right down to having the same move set.", 10, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect("Transformed", EffectType.POKEMON));
			super.selfTarget = true;
			super.moveTypes.add("NonSnatchable");
			super.moveTypes.add("ProtectPiercing");
			super.moveTypes.add("Encoreless");
			super.moveTypes.add("Assistless");
			super.moveTypes.add("Metronomeless");
		}
	}

	private static class Substitute extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Substitute()
		{
			super("Substitute", "The user makes a copy of itself using some of its HP. The copy serves as the user's decoy.", 10, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
			super.effects.add(Effect.getEffect("Substitute", EffectType.POKEMON));
		}
	}

	private static class RazorWind extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;
		public RazorWind()
		{
			super("Razor Wind", "A two-turn attack. Blades of wind hit opposing Pok\u00e9mon on the second turn. Critical hits land more easily.", 10, Type.NORMAL, Category.SPECIAL);
			super.moveTypes.add("SleepTalkFail");
			super.accuracy = 100;
			super.moveTypes.add("HighCritRatio");
			super.power = 80;
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
			b.addMessage(user.getName()+" whipped up a whirlwind!");
		}
	}

	private static class LovelyKiss extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public LovelyKiss()
		{
			super("Lovely Kiss", "With a scary face, the user tries to force a kiss on the target. If it succeeds, the target falls asleep.", 10, Type.NORMAL, Category.STATUS);
			super.accuracy = 75;
			super.status = StatusCondition.ASLEEP;
		}
	}

	private static class PowderSnow extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public PowderSnow()
		{
			super("Powder Snow", "The user attacks with a chilling gust of powdery snow. It may also freeze the targets.", 25, Type.ICE, Category.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.status = StatusCondition.FROZEN;
			super.effectChance = 10;
		}
	}

	private static class HeartStamp extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public HeartStamp()
		{
			super("Heart Stamp", "The user unleashes a vicious blow after its cute act makes the target less wary. It may also make the target flinch.", 25, Type.PSYCHIC, Category.PHYSICAL);
			super.accuracy = 100;
			super.power = 60;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.effectChance = 30;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class FakeTears extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FakeTears()
		{
			super("Fake Tears", "The user feigns crying to fluster the target, harshly lowering its Sp. Def stat.", 20, Type.DARK, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.SP_DEFENSE.index()] = -2;
		}
	}

	private static class Avalanche extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Avalanche()
		{
			super("Avalanche", "An attack move that inflicts double the damage if the user has been hurt by the target in the same turn.", 10, Type.ICE, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.priority = -4;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			if (me.getAttributes().hasTakenDamage()) return super.power*2;
			return super.power;
		}
	}

	private static class Blizzard extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Blizzard()
		{
			super("Blizzard", "A howling blizzard is summoned to strike the opposing team. It may also freeze them solid.", 5, Type.ICE, Category.SPECIAL);
			super.power = 120;
			super.accuracy = 70;
			super.status = StatusCondition.FROZEN;
			super.effectChance = 10;
		}

		public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o)
		{
			if (b.getWeather().getType() == Weather.WeatherType.HAILING) return 100;
			return super.accuracy;
		}
	}

	private static class ShockWave extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public ShockWave()
		{
			super("Shock Wave", "The user strikes the target with a quick jolt of electricity. This attack cannot be evaded.", 20, Type.ELECTRIC, Category.SPECIAL);
			super.power = 60;
		}
	}

	private static class LavaPlume extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public LavaPlume()
		{
			super("Lava Plume", "An inferno of scarlet flames torches everything around the user. It may leave targets with a burn.", 15, Type.FIRE, Category.SPECIAL);
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
			super("Work Up", "The user is roused, and its Attack and Sp. Atk stats increase.", 30, Type.NORMAL, Category.STATUS);
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
			super("Giga Impact", "The user charges at the target using every bit of its power. The user must rest on the next turn.", 5, Type.NORMAL, Category.PHYSICAL);
			super.power = 150;
			super.accuracy = 90;
			super.moveTypes.add("PhysicalContact");
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
			b.addMessage(user.getName()+" must recharge!");
		}
	}

	private static class Splash extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Splash()
		{
			super("Splash", "The user just flops and splashes around to no effect at all...", 40, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
			super.moveTypes.add("Airborne");
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			b.addMessage("But nothing happened...");
		}
	}

	private static class Mist extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Mist()
		{
			super("Mist", "The user cloaks its body with a white mist that prevents any of its stats from being cut for five turns.", 30, Type.ICE, Category.STATUS);
			super.selfTarget = true;
			super.effects.add(Effect.getEffect("Mist", EffectType.POKEMON));
		}
	}

	private static class LastResort extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public LastResort()
		{
			super("Last Resort", "This move can be used only after the user has used all the other moves it knows in the battle.", 5, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 100;
			super.power = 140;
			super.moveTypes.add("PhysicalContact");
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			for (Move m : me.getMoves())
			{
				if (m.getAttack().getName().equals("Last Resort")) continue;
				if (!m.used())
				{
					b.addMessage("...but it failed!");
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
			super("Trump Card", "The fewer PP this move has, the greater its attack power.", 5, Type.NORMAL, Category.SPECIAL);
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			int pp = me.getMove().getPP();
			if (pp == 1) return 190;
			if (pp == 2) return 75;
			if (pp == 3) return 60;
			if (pp == 4) return 50;
			return 40;
		}
	}

	private static class MuddyWater extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MuddyWater()
		{
			super("Muddy Water", "The user attacks by shooting muddy water at the opposing team. It may also lower the targets' accuracy.", 10, Type.WATER, Category.SPECIAL);
			super.accuracy = 85;
			super.power = 95;
			super.statChanges[Stat.ACCURACY.index()] = -1;
			super.effectChance = 30;
		}
	}

	private static class Conversion extends Attack implements ChangeTypeMove
	{
		private static final long serialVersionUID = 1L;
		public Conversion()
		{
			super("Conversion", "The user changes its type to become the same type as one of its moves.", 30, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
			super.effects.add(Effect.getEffect("ChangeType", EffectType.POKEMON));
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			for (Move m : me.getMoves())
			{
				if (!me.isType(m.getAttack().getType(b, me)))
				{
					super.apply(me, o, b);
					return;
				}
			}
			b.addMessage("...but it failed!");
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			List<Type> types = new ArrayList<>();
			for (Move m : victim.getMoves())
			{
				Type t = m.getAttack().getType(b, victim);
				if (!victim.isType(t)) types.add(t);
			}
			return new Type[] {types.get((int)(Math.random()*types.size())), Type.NONE};
		}
	}

	private static class Conversion2 extends Attack implements ChangeTypeMove
	{
		private static final long serialVersionUID = 1L;
		private List<Type> getResistances(ActivePokemon victim, Type attacking)
		{
			List<Type> types = new ArrayList<>();
			for (Type t : Type.values())
			{
				if (Type.getAdvantage(attacking, t) < 1 && !victim.isType(t)) types.add(t);
			}
			return types;
		}

		public Conversion2()
		{
			super("Conversion 2", "The user changes its type to make itself resistant to the type of the attack the opponent used last.", 30, Type.NORMAL, Category.STATUS);
			super.moveTypes.add("NonSnatchable");
			super.selfTarget = true;
			super.effects.add(Effect.getEffect("ChangeType", EffectType.POKEMON));
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			Move m = o.getAttributes().getLastMoveUsed();
			if (m == null || getResistances(me, m.getAttack().getType(b, o)).size() == 0)
			{
				b.addMessage("...but it failed!");
				return;
			}
			super.apply(me, o, b);
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			ActivePokemon other = b.getOtherPokemon(victim.user());
			List<Type> types = getResistances(victim, other.getAttributes().getLastMoveUsed().getAttack().getType(b, other));
			return new Type[] {types.get((int)(Math.random()*types.size())), Type.NONE};
		}
	}

	private static class Sharpen extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Sharpen()
		{
			super("Sharpen", "The user reduces its polygon count to make itself more jagged, raising the Attack stat.", 30, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
		}
	}

	private static class MagicCoat extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MagicCoat()
		{
			super("Magic Coat", "A barrier reflects back to the target moves like Leech Seed and moves that damage status.", 15, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect("MagicCoat", EffectType.POKEMON));
			super.selfTarget = true;
			super.priority = 4;
			super.moveTypes.add("NonSnatchable");
		}
	}

	private static class SkyDrop extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SkyDrop()
		{
			super("Sky Drop", "The user takes the target into the sky, then slams it into the ground.", 10, Type.FLYING, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add("Airborne");
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class IronHead extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public IronHead()
		{
			super("Iron Head", "The user slams the target with its steel-hard head. It may also make the target flinch.", 15, Type.STEEL, Category.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.effectChance = 30;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class RockSlide extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public RockSlide()
		{
			super("Rock Slide", "Large boulders are hurled at the opposing team to inflict damage. It may also make the targets flinch.", 10, Type.ROCK, Category.PHYSICAL);
			super.power = 75;
			super.accuracy = 90;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.effectChance = 30;
		}
	}

	private static class Snore extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Snore()
		{
			super("Snore", "An attack that can be used only if the user is asleep. The harsh noise may also make the target flinch.", 15, Type.NORMAL, Category.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add("SoundBased");
			super.moveTypes.add("Metronomeless");
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.effectChance = 30;
			super.moveTypes.add("AsleepUser");
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (!me.hasStatus(StatusCondition.ASLEEP))
			{
				b.addMessage("...but it failed!");
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
			super("Sleep Talk", "While it is asleep, the user randomly uses one of the moves it knows.", 10, Type.NORMAL, Category.STATUS);
			super.moveTypes.add("NoMagicCoat");
			super.moveTypes.add("SleepTalkFail");
			super.moveTypes.add("AsleepUser");
			super.moveTypes.add("Assistless");
			super.moveTypes.add("Metronomeless");
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (!me.hasStatus(StatusCondition.ASLEEP))
			{
				b.addMessage("...but it failed!");
				return;
			}
			List<Move> moves = new ArrayList<>();
			for (Move m : me.getMoves())
			{
				if (!m.getAttack().isMoveType("SleepTalkFail")) moves.add(m);
			}
			if (moves.size() == 0)
			{
				b.addMessage("...but it failed!");
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
			super("Block", "The user blocks the target's way with arms spread wide to prevent escape.", 5, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect("Trapped", EffectType.POKEMON));
		}
	}

	private static class SkyAttack extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;
		public SkyAttack()
		{
			super("Sky Attack", "A second-turn attack move where critical hits land more easily. It may also make the target flinch.", 5, Type.FLYING, Category.PHYSICAL);
			super.moveTypes.add("SleepTalkFail");
			super.power = 140;
			super.accuracy = 90;
			super.moveTypes.add("HighCritRatio");
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.effectChance = 30;
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
			b.addMessage(user.getName()+" started glowing!");
		}
	}

	private static class DragonRush extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public DragonRush()
		{
			super("Dragon Rush", "The user tackles the target while exhibiting overwhelming menace. It may also make the target flinch.", 10, Type.DRAGON, Category.PHYSICAL);
			super.accuracy = 75;
			super.power = 100;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.effectChance = 20;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class AuraSphere extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public AuraSphere()
		{
			super("Aura Sphere", "The user looses a blast of aura power from deep within its body at the target. This move is certain to hit.", 20, Type.FIGHTING, Category.SPECIAL);
			super.power = 90;
		}
	}

	private static class Psystrike extends Attack implements StatSwitchingEffect
	{
		private static final long serialVersionUID = 1L;
		public Psystrike()
		{
			super("Psystrike", "The user materializes an odd psychic wave to attack the target. This attack does physical damage.", 10, Type.PSYCHIC, Category.SPECIAL);
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
			super("Eruption", "The user attacks the opposing team with explosive fury. The lower the user's HP, the less powerful this attack becomes.", 5, Type.FIRE, Category.SPECIAL);
			super.accuracy = 100;
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return (int)(me.getHPRatio()*150);
		}
	}

	private static class Charm extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Charm()
		{
			super("Charm", "The user gazes at the target rather charmingly, making it less wary. The target's Attack is harshly lowered.", 20, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.ATTACK.index()] = -2;
		}
	}

	private static class EchoedVoice extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public EchoedVoice()
		{
			super("Echoed Voice", "The user attacks the target with an echoing voice. If this move is used every turn, it does greater damage.", 15, Type.NORMAL, Category.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add("SoundBased");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return Math.min(200, super.power*me.getAttributes().getCount());
		}
	}

	private static class PsychoShift extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public PsychoShift()
		{
			super("Psycho Shift", "Using its psychic power of suggestion, the user transfers its status problems to the target.", 10, Type.PSYCHIC, Category.STATUS);
			super.accuracy = 90;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (!user.hasStatus() || !Status.giveStatus(b, user, victim, user.getStatus().getType()))
			{
				b.addMessage("...but it failed!");
				return;
			}
			user.removeStatus();
		}
	}

	private static class ShadowSneak extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public ShadowSneak()
		{
			super("Shadow Sneak", "The user extends its shadow and attacks the target from behind. This move always goes first.", 30, Type.GHOST, Category.PHYSICAL);
			super.power = 40;
			super.priority = 1;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class SpiderWeb extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SpiderWeb()
		{
			super("Spider Web", "The user ensnares the target with thin, gooey silk so it can't flee from battle.", 10, Type.BUG, Category.STATUS);
			super.effects.add(Effect.getEffect("Trapped", EffectType.POKEMON));
		}
	}

	private static class SweetKiss extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SweetKiss()
		{
			super("Sweet Kiss", "The user kisses the target with a sweet, angelic cuteness that causes confusion.", 10, Type.NORMAL, Category.STATUS);
			super.accuracy = 75;
			super.effects.add(Effect.getEffect("Confusion", EffectType.POKEMON));
		}
	}

	private static class OminousWind extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public OminousWind()
		{
			super("Ominous Wind", "The user blasts the target with a gust of repulsive wind. It may also raise all the user's stats at once.", 5, Type.GHOST, Category.SPECIAL);
			super.accuracy = 100;
			super.power = 60;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
			super.statChanges[Stat.SPEED.index()] = 1;
			super.statChanges[Stat.ACCURACY.index()] = 1;
			super.statChanges[Stat.EVASION.index()] = 1;
			super.selfTarget = true;
			super.effectChance = 10;
		}
	}

	private static class CottonSpore extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public CottonSpore()
		{
			super("Cotton Spore", "The user releases cotton-like spores that cling to the target, harshly reducing its Speed stat.", 40, Type.GRASS, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.SPEED.index()] = -2;
		}
	}

	private static class CottonGuard extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public CottonGuard()
		{
			super("Cotton Guard", "The user protects itself by wrapping its body in soft cotton, drastically raising the user's Defense stat.", 10, Type.GRASS, Category.STATUS);
			super.statChanges[Stat.DEFENSE.index()] = 3;
			super.selfTarget = true;
		}
	}

	private static class Grasswhistle extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Grasswhistle()
		{
			super("Grasswhistle", "The user plays a pleasant melody that lulls the target into a deep sleep.", 15, Type.GRASS, Category.STATUS);
			super.accuracy = 55;
			super.status = StatusCondition.ASLEEP;
			super.moveTypes.add("SoundBased");
		}
	}

	private static class Torment extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Torment()
		{
			super("Torment", "The user torments and enrages the target, making it incapable of using the same move twice in a row.", 15, Type.DARK, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("Torment", EffectType.POKEMON));
			super.moveTypes.add("SubstitutePiercing");
		}
	}

	private static class HiddenPower extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public HiddenPower()
		{
			super("Hidden Power", "A unique attack that varies in type and intensity depending on the Pok\u00e9mon using it.", 15, Type.NORMAL, Category.SPECIAL);
			super.accuracy = 100;
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return me.getHiddenPower();
		}

		public Type getType(Battle b, ActivePokemon user)
		{
			if (user.hasAbility("Normalize")) return Type.NORMAL;
			return user.getHiddenType();
		}
	}

	private static class Psywave extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Psywave()
		{
			super("Psywave", "The target is attacked with an odd psychic wave. The attack varies in intensity.", 15, Type.PSYCHIC, Category.SPECIAL);
			super.accuracy = 80;
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			return b.applyDamage(o, (int)Math.max(1, ((int)(Math.random()*11)+5)*me.getLevel()/10.0));
		}
	}

	private static class PainSplit extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public PainSplit()
		{
			super("Pain Split", "The user adds its HP to the target's HP, then equally shares the combined HP with the target.", 20, Type.NORMAL, Category.STATUS);
			super.moveTypes.add("NoMagicCoat");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			int share = (user.getHP()+victim.getHP())/2;
			user.setHP(share);
			victim.setHP(share);
			b.addMessage(user.getName()+" and "+victim.getName()+" split their pain!", user.getHP(), user.user());
			b.addMessage("", victim.getHP(), victim.user());
		}
	}

	private static class Bide extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Bide()
		{
			super("Bide", "The user endures attacks for two turns, then strikes back to cause double the damage taken.", 10, Type.NORMAL, Category.PHYSICAL);
			super.effects.add(Effect.getEffect("Bide", EffectType.POKEMON));
			super.selfTarget = true;
			super.priority = 1;
			super.moveTypes.add("SleepTalkFail");
			super.moveTypes.add("PhysicalContact");
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
			super("Autotomize", "The user sheds part of its body to make itself lighter and sharply raise its Speed stat.", 15, Type.STEEL, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.SPEED.index()] = 2;
			super.effects.add(Effect.getEffect("HalfWeight", EffectType.POKEMON));
		}
	}

	private static class StruggleBug extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public StruggleBug()
		{
			super("Struggle Bug", "While resisting, the user attacks the opposing Pok\u00e9mon. The targets' Sp. Atk stat is reduced.", 20, Type.BUG, Category.SPECIAL);
			super.accuracy = 100;
			super.statChanges[Stat.SP_ATTACK.index()] = -1;
			super.power = 30;
		}
	}

	private static class PowerTrick extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public PowerTrick()
		{
			super("Power Trick", "The user employs its psychic power to switch its Attack with its Defense stat.", 10, Type.PSYCHIC, Category.STATUS);
			super.selfTarget = true;
			super.effects.add(Effect.getEffect("PowerTrick", EffectType.POKEMON));
		}
	}

	private static class PowerSplit extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public PowerSplit()
		{
			super("Power Split", "The user employs its psychic power to average its Attack and Sp. Atk stats with those of the target's.", 10, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect("PowerSplit", EffectType.POKEMON));
			super.selfTarget = true;
			super.moveTypes.add("NonSnatchable");
		}
	}

	private static class GuardSplit extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public GuardSplit()
		{
			super("Guard Split", "The user employs its psychic power to average its Defense and Sp. Def stats with those of its target's.", 10, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect("GuardSplit", EffectType.POKEMON));
			super.selfTarget = true;
			super.moveTypes.add("NonSnatchable");
		}
	}

	private static class HoneClaws extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public HoneClaws()
		{
			super("Hone Claws", "The user sharpens its claws to boost its Attack stat and accuracy.", 15, Type.DARK, Category.STATUS);
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.ACCURACY.index()] = 1;
			super.selfTarget = true;
		}
	}

	private static class BeatUp extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public BeatUp()
		{
			super("Beat Up", "The user gets all party Pok\u00e9mon to attack the target. The more party Pok\u00e9mon, the greater the number of attacks.", 10, Type.DARK, Category.PHYSICAL);
			super.power = 10;
			super.accuracy = 100;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			for (ActivePokemon p : b.getTrainer(me.user()).getTeam())
			{
				if (p.hasStatus()) continue;
				Move temp = p.getMove();
				p.setMove(new Move(Attack.getAttack("Beat Up")));
				b.addMessage(p.getName()+"'s attack!");
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
			super("Octazooka", "The user attacks by spraying ink in the target's face or eyes. It may also lower the target's accuracy.", 10, Type.WATER, Category.SPECIAL);
			super.power = 65;
			super.accuracy = 85;
			super.statChanges[Stat.ACCURACY.index()] = -1;
			super.effectChance = 50;
		}
	}

	private static class Present extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Present()
		{
			super("Present", "The user attacks by giving the target a gift with a hidden trap. It restores HP sometimes, however.", 15, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 90;
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			double random = Math.random()*80;
			if (random < 40) return 40;
			if (random < 70) return 80;
			return 120;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (Math.random()*100 < 80)
			{
				super.applyDamage(me, o, b);
				return;
			}
			if (o.fullHealth() || o.hasEffect("HealBlock"))
			{
				b.addMessage("...but it failed!");
				return;
			}
			o.healHealthFraction(1/4.0);
			b.addMessage(o.getName()+"'s health was restored!", o.getHP(), o.user());
		}
	}

	private static class SteelWing extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SteelWing()
		{
			super("Steel Wing", "The target is hit with wings of steel. It may also raise the user's Defense stat.", 25, Type.STEEL, Category.PHYSICAL);
			super.accuracy = 90;
			super.power = 70;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.selfTarget = true;
			super.effectChance = 10;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Sketch extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Sketch()
		{
			super("Sketch", "It enables the user to permanently learn the move last used by the target. Once used, Sketch disappears.", 1, Type.NORMAL, Category.STATUS);
			super.moveTypes.add("SleepTalkFail");
			super.moveTypes.add("Encoreless");
			super.moveTypes.add("Mimicless");
			super.moveTypes.add("Assistless");
			super.moveTypes.add("Metronomeless");
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			Move copy = o.getAttributes().getLastMoveUsed();
			if (copy == null || copy.getAttack().getName().equals("Struggle") || me.hasEffect("Transformed"))
			{
				b.addMessage("...but it failed!");
				return;
			}
			List<Move> moves = me.getMoves();
			for (int i = 0; i < moves.size(); i++)
			{
				if (moves.get(i).getAttack().getName().equals("Sketch"))
				{
					moves.add(i, new Move(copy.getAttack()));
					moves.remove(i+1);
					b.addMessage(me.getName()+" learned "+moves.get(i).getAttack().getName()+"!");
				}
			}
		}
	}

	private static class TripleKick extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public TripleKick()
		{
			super("Triple Kick", "A consecutive three-kick attack that becomes more powerful with each successive hit.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.power = 10;
			super.accuracy = 90;
			super.moveTypes.add("PhysicalContact");
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			int pow = super.power, hits, damage = 0;
			for (hits = 1; hits <= 3; hits++)
			{
				super.power = pow*hits;
				b.addMessage("Hit "+hits+"!");
				damage += super.applyDamage(me, o, b);
				if (hits < 3 && !b.accuracyCheck(me, o)) break;
			}
			if (hits > 3) hits = 3;
			b.addMessage("Hit "+hits+" times!");
			super.power = pow;
			return damage;
		}
	}

	private static class MilkDrink extends Attack implements SelfHealingMove
	{
		private static final long serialVersionUID = 1L;
		public MilkDrink()
		{
			super("Milk Drink", "The user restores its own HP by up to half of its maximum HP. May also be used in the field to heal HP.", 10, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect("HealBlock"))
			{
				b.addMessage("...but it failed!");
				return;
			}

			victim.healHealthFraction(1/2.0);
			b.addMessage(victim.getName()+"'s health was restored!", victim.getHP(), victim.user());
		}
	}

	private static class HealBell extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public HealBell()
		{
			super("Heal Bell", "The user makes a soothing bell chime to heal the status problems of all the party Pok\u00e9mon.", 5, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			for (ActivePokemon p : b.getTrainer(me.user()).getTeam())
			{
				if (!p.hasStatus(StatusCondition.FAINTED)) p.removeStatus();
			}
			b.addMessage("All status problems were cured!");
		}
	}

	private static class WeatherBall extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public WeatherBall()
		{
			super("Weather Ball", "An attack move that varies in power and type depending on the weather.", 10, Type.NORMAL, Category.SPECIAL);
			super.power = 50;
			super.accuracy = 100;
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(b.getWeather().getType() == Weather.WeatherType.CLEAR_SKIES ? 1 : 2);
		}

		public Type getType(Battle b, ActivePokemon user)
		{
			if (user.hasAbility("Normalize")) return Type.NORMAL;
			switch (b.getWeather().getType())
			{
				case SUNNY:
				return Type.FIRE;
				case RAINING:
				return Type.WATER;
				case HAILING:
				return Type.ICE;
				case SANDSTORM:
				return Type.ROCK;
				default:
				return Type.NORMAL;
			}
		}
	}

	private static class Aeroblast extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Aeroblast()
		{
			super("Aeroblast", "A vortex of air is shot at the target to inflict damage. Critical hits land more easily.", 5, Type.FLYING, Category.SPECIAL);
			super.accuracy = 95;
			super.power = 100;
			super.moveTypes.add("HighCritRatio");
		}
	}

	private static class SacredFire extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SacredFire()
		{
			super("Sacred Fire", "The target is razed with a mystical fire of great intensity. It may also leave the target with a burn.", 5, Type.FIRE, Category.PHYSICAL);
			super.power = 100;
			super.accuracy = 95;
			super.status = StatusCondition.BURNED;
			super.effectChance = 50;
		}
	}

	private static class HealBlock extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public HealBlock()
		{
			super("Heal Block", "For five turns, the user prevents the opposing team from using any moves, Abilities, or held items that recover HP.", 15, Type.PSYCHIC, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("HealBlock", EffectType.POKEMON));
		}
	}

	private static class EnergyBall extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public EnergyBall()
		{
			super("Energy Ball", "The user draws power from nature and fires it at the target. It may also lower the target's Sp. Def.", 10, Type.GRASS, Category.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
			super.effectChance = 10;
		}
	}

	private static class BulkUp extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public BulkUp()
		{
			super("Bulk Up", "The user tenses its muscles to bulk up its body, boosting both its Attack and Defense stats.", 20, Type.FIGHTING, Category.STATUS);
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
			super("Thief", "The user attacks and steals the target's held item simultaneously. It can't steal if the user holds an item.", 25, Type.DARK, Category.PHYSICAL);
			super.moveTypes.add("Metronomeless");
			super.power = 60;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("ChangeItem", EffectType.POKEMON));
			super.moveTypes.add("Assistless");
			super.moveTypes.add("PhysicalContact");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (user.isHoldingItem(b) || !victim.isHoldingItem(b) || b.getTrainer(user.user()) instanceof WildPokemon || victim.hasAbility("Sticky Hold"))
			{
				if (super.category == Category.STATUS) b.addMessage("...but it failed!");
				return;
			}

			Item userItem = user.getHeldItem(b), victimItem = victim.getHeldItem(b);
			b.addMessage(user.getName()+" stole "+victim.getName()+"'s "+victimItem.getName()+"!");

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
			super("Attract", "If it is the opposite gender of the user, the target becomes infatuated and less likely to attack.", 15, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("Infatuated", EffectType.POKEMON));
			super.moveTypes.add("SubstitutePiercing");
		}
	}

	private static class ForcePalm extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public ForcePalm()
		{
			super("Force Palm", "The target is attacked with a shock wave. It may also leave the target with paralysis.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.accuracy = 100;
			super.power = 60;
			super.status = StatusCondition.PARALYZED;
			super.effectChance = 30;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class ArmThrust extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public ArmThrust()
		{
			super("Arm Thrust", "The user looses a flurry of open-palmed arm thrusts that hit two to five times in a row.", 20, Type.FIGHTING, Category.PHYSICAL);
			super.accuracy = 100;
			super.power = 15;
			super.moveTypes.add("PhysicalContact");
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			int damage = 0, hits = (int)(Math.random()*(5-2+1))+2;
			if (me.hasAbility("Skill Link")) hits = 5;
			for (int i = 1; i <= hits; i++)
			{
				b.addMessage("Hit "+i+"!");
				damage += super.applyDamage(me, o, b);
			}
			b.addMessage("Hit "+hits+" times!");
			return damage;
		}
	}

	private static class Smellingsalt extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Smellingsalt()
		{
			super("Smellingsalt", "This attack inflicts double damage on a target with paralysis. It also cures the target's paralysis, however.", 10, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 100;
			super.power = 60;
			super.moveTypes.add("PhysicalContact");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (victim.hasStatus(StatusCondition.PARALYZED))
			{
				b.addMessage(victim.getName()+" was cured from its paralysis!", StatusCondition.NONE, victim.user());
				victim.removeStatus();
			}
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(o.hasStatus(StatusCondition.PARALYZED) ? 2 : 1);
		}
	}

	private static class Assist extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Assist()
		{
			super("Assist", "The user hurriedly and randomly uses a move among those known by other Pok\u00e9mon in the party.", 20, Type.NORMAL, Category.STATUS);
			super.moveTypes.add("NoMagicCoat");
			super.moveTypes.add("SleepTalkFail");
			super.moveTypes.add("Metronomeless");
			super.moveTypes.add("Assistless");
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			List<Attack> attacks = new ArrayList<>();
			for (ActivePokemon p : b.getTrainer(me.user()).getTeam())
			{
				if (p == b.getTrainer(me.user()).front()) continue;
				for (Move m : p.getMoves())
				{
					if (!m.getAttack().isMoveType("Assistless")) attacks.add(m.getAttack());
				}
			}
			if (attacks.size() == 0)
			{
				b.addMessage("...but it failed!");
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
			super("Metal Burst", "The user retaliates with much greater power against the target that last inflicted damage on it.", 10, Type.STEEL, Category.PHYSICAL);
			super.accuracy = 100;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			int damageTaken = me.getAttributes().getDamageTaken();
			if (damageTaken == 0 || b.isFirstAttack())
			{
				b.addMessage("...but it failed!");
				return;
			}
			if (super.zeroAdvantage(b, me, o)) return;
			b.applyDamage(o, (int)(damageTaken*1.5));
		}
	}

	private static class WildCharge extends Attack implements RecoilMove
	{
		private static final long serialVersionUID = 1L;
		private int recoilDamage(ActivePokemon user, int damage)
		{
			return (int)Math.ceil(damage/4.0);
		}

		public WildCharge()
		{
			super("Wild Charge", "The user shrouds itself in electricity and smashes into its target. It also damages the user a little.", 15, Type.ELECTRIC, Category.PHYSICAL);
			super.accuracy = 100;
			super.power = 90;
			super.moveTypes.add("PhysicalContact");
		}

		public void applyRecoil(Battle b, ActivePokemon user, int damage)
		{
			if (user.hasAbility("Rock Head") || user.hasAbility("Magic Guard")) return;
			b.addMessage(user.getName()+" was hurt by recoil!");
			b.applyDamage(user, recoilDamage(user, damage));
		}
	}

	private static class Flash extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Flash()
		{
			super("Flash", "The user flashes a bright light that cuts the target's accuracy. It can also be used to illuminate caves.", 20, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	private static class TailGlow extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public TailGlow()
		{
			super("Tail Glow", "The user stares at flashing lights to focus its mind, drastically raising its Sp. Atk stat.", 20, Type.BUG, Category.STATUS);
			super.statChanges[Stat.SP_ATTACK.index()] = 3;
			super.selfTarget = true;
		}
	}

	private static class WaterSpout extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public WaterSpout()
		{
			super("Water Spout", "The user spouts water to damage the opposing team. The lower the user's HP, the less powerful it becomes.", 5, Type.WATER, Category.SPECIAL);
			super.accuracy = 100;
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return (int)(me.getHPRatio()*150);
		}
	}

	private static class TeeterDance extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public TeeterDance()
		{
			super("Teeter Dance", "The user performs a wobbly dance that confuses the Pok\u00e9mon around it.", 20, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("Confusion", EffectType.POKEMON));
		}
	}

	private static class NeedleArm extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public NeedleArm()
		{
			super("Needle Arm", "The user attacks by wildly swinging its thorny arms. It may also make the target flinch.", 15, Type.GRASS, Category.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("Flinch", EffectType.POKEMON));
			super.effectChance = 30;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Venoshock extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Venoshock()
		{
			super("Venoshock", "The user drenches the target in a special poisonous liquid. Its power is doubled if the target is poisoned.", 10, Type.POISON, Category.SPECIAL);
			super.accuracy = 100;
			super.power = 65;
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(o.hasStatus(StatusCondition.POISONED) ? 2 : 1);
		}
	}

	private static class Snatch extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Snatch()
		{
			super("Snatch", "The user steals the effects of any healing or stat-changing move the opponent attempts to use.", 10, Type.DARK, Category.STATUS);
			super.priority = 4;
			super.effects.add(Effect.getEffect("Snatch", EffectType.POKEMON));
			super.selfTarget = true;
			super.moveTypes.add("Assistless");
			super.moveTypes.add("Metronomeless");
		}
	}

	private static class IceBall extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public IceBall()
		{
			super("Ice Ball", "The user continually rolls into the target over five turns. It becomes stronger each time it hits.", 20, Type.ICE, Category.PHYSICAL);
			super.power = 30;
			super.accuracy = 90;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(int)Math.min(me.getAttributes().getCount(), 5)*(me.hasEffect("UsedDefenseCurl") ? 2 : 1);
		}
	}

	private static class HeadSmash extends Attack implements RecoilMove
	{
		private static final long serialVersionUID = 1L;
		private int recoilDamage(ActivePokemon user, int damage)
		{
			return (int)Math.ceil(damage/2.0);
		}

		public HeadSmash()
		{
			super("Head Smash", "The user attacks the target with a hazardous, full-power headbutt. The user also takes terrible damage.", 5, Type.ROCK, Category.PHYSICAL);
			super.power = 150;
			super.accuracy = 80;
			super.power = 150;
			super.moveTypes.add("PhysicalContact");
		}

		public void applyRecoil(Battle b, ActivePokemon user, int damage)
		{
			if (user.hasAbility("Rock Head") || user.hasAbility("Magic Guard")) return;
			b.addMessage(user.getName()+" was hurt by recoil!");
			b.applyDamage(user, recoilDamage(user, damage));
		}
	}

	private static class MistBall extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MistBall()
		{
			super("Mist Ball", "A mistlike flurry of down envelops and damages the target. It may also lower the target's Sp. Atk.", 5, Type.PSYCHIC, Category.SPECIAL);
			super.accuracy = 100;
			super.power = 70;
			super.statChanges[Stat.SP_ATTACK.index()] = -1;
			super.effectChance = 50;
		}
	}

	private static class LusterPurge extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public LusterPurge()
		{
			super("Luster Purge", "The user lets loose a damaging burst of light. It may also reduce the target's Sp. Def stat.", 5, Type.PSYCHIC, Category.SPECIAL);
			super.power = 70;
			super.accuracy = 100;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
			super.effectChance = 50;
		}
	}

	private static class PsychoBoost extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public PsychoBoost()
		{
			super("Psycho Boost", "The user attacks the target at full power. The attack's recoil harshly reduces the user's Sp. Atk stat.", 5, Type.PSYCHIC, Category.SPECIAL);
			super.power = 140;
			super.accuracy = 90;
			super.statChanges[Stat.SP_ATTACK.index()] = -2;
			super.selfTarget = true;
		}
	}

	private static class Facade extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Facade()
		{
			super("Facade", "An attack move that doubles its power if the user is poisoned, burned, or has paralysis.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(me.hasStatus() ? 2 : 1);
		}
	}

	private static class DefendOrder extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public DefendOrder()
		{
			super("Defend Order", "The user calls out its underlings to shield its body, raising its Defense and Sp. Def stats.", 10, Type.BUG, Category.STATUS);
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
			super("Heal Order", "The user calls out its underlings to heal it. The user regains up to half of its max HP.", 10, Type.BUG, Category.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b)
		{
			if (victim.fullHealth() || victim.hasEffect("HealBlock"))
			{
				b.addMessage("...but it failed!");
				return;
			}

			victim.healHealthFraction(1/2.0);
			b.addMessage(victim.getName()+"'s health was restored!", victim.getHP(), victim.user());
		}
	}

	private static class AttackOrder extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public AttackOrder()
		{
			super("Attack Order", "The user calls out its underlings to pummel the target. Critical hits land more easily.", 15, Type.BUG, Category.PHYSICAL);
			super.power = 90;
			super.moveTypes.add("HighCritRatio");
			super.accuracy = 100;
		}
	}

	private static class Chatter extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Chatter()
		{
			super("Chatter", "The user attacks using a sound wave based on words it has learned. It may also confuse the target.", 20, Type.FLYING, Category.SPECIAL);
			super.power = 60;
			super.effects.add(Effect.getEffect("Confusion", EffectType.POKEMON));
			super.moveTypes.add("SleepTalkFail");
			super.effectChance = 15;
			super.accuracy = 100;
			super.moveTypes.add("SoundBased");
			super.moveTypes.add("Mimicless");
			super.moveTypes.add("Assistless");
			super.moveTypes.add("Metronomeless");
		}
	}

	private static class DualChop extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public DualChop()
		{
			super("Dual Chop", "The user attacks its target by hitting it with brutal strikes. The target is hit twice in a row.", 15, Type.DRAGON, Category.PHYSICAL);
			super.accuracy = 90;
			super.power = 40;
			super.moveTypes.add("PhysicalContact");
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			int damage = 0, hits = (int)(Math.random()*(2-2+1))+2;
			for (int i = 1; i <= hits; i++)
			{
				b.addMessage("Hit "+i+"!");
				damage += super.applyDamage(me, o, b);
			}
			b.addMessage("Hit "+hits+" times!");
			return damage;
		}
	}

	private static class RockWrecker extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;
		public RockWrecker()
		{
			super("Rock Wrecker", "The user launches a huge boulder at the target to attack. It must rest on the next turn, however.", 5, Type.ROCK, Category.PHYSICAL);
			super.accuracy = 90;
			super.power = 150;
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
			b.addMessage(user.getName()+" must recharge!");
		}
	}

	private static class TrickRoom extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public TrickRoom()
		{
			super("Trick Room", "The user creates a bizarre area in which slower Pok\u00e9mon get to move first for five turns.", 5, Type.PSYCHIC, Category.STATUS);
			super.priority = -7;
			super.effects.add(Effect.getEffect("TrickRoom", EffectType.BATTLE));
			super.moveTypes.add("Field");
		}
	}

	private static class RoarOfTime extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;
		public RoarOfTime()
		{
			super("Roar Of Time", "The user blasts the target with power that distorts even time. The user must rest on the next turn.", 5, Type.DRAGON, Category.SPECIAL);
			super.accuracy = 90;
			super.power = 150;
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
			b.addMessage(user.getName()+" must recharge!");
		}
	}

	private static class SpacialRend extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SpacialRend()
		{
			super("Spacial Rend", "The user tears the target along with the space around it. Critical hits land more easily.", 5, Type.DRAGON, Category.SPECIAL);
			super.power = 100;
			super.accuracy = 95;
			super.moveTypes.add("HighCritRatio");
		}
	}

	private static class MagmaStorm extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MagmaStorm()
		{
			super("Magma Storm", "The target becomes trapped within a maelstrom of fire that rages for four to five turns.", 5, Type.FIRE, Category.SPECIAL);
			super.power = 120;
			super.accuracy = 75;
			super.effects.add(Effect.getEffect("MagmaStorm", EffectType.POKEMON));
		}
	}

	private static class CrushGrip extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public CrushGrip()
		{
			super("Crush Grip", "The target is crushed with great force. The attack is more powerful the more HP the target has left.", 5, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return (int)Math.min(1, (120*o.getHPRatio()));
		}
	}

	private static class ShadowForce extends Attack implements MultiTurnMove
	{
		private static final long serialVersionUID = 1L;
		public ShadowForce()
		{
			super("Shadow Force", "The user disappears, then strikes the target on the second turn. It hits even if the target protects itself.", 5, Type.GHOST, Category.PHYSICAL);
			super.accuracy = 100;
			super.power = 120;
			super.moveTypes.add("ProtectPiercing");
			super.moveTypes.add("SleepTalkFail");
			super.moveTypes.add("PhysicalContact");
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
			b.addMessage(user.getName()+" disappeared!");
		}
	}

	private static class HeartSwap extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public HeartSwap()
		{
			super("Heart Swap", "The user employs its psychic power to switch stat changes with the target.", 10, Type.PSYCHIC, Category.STATUS);
			super.moveTypes.add("SubstitutePiercing");
			super.moveTypes.add("NoMagicCoat");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			for (int i = 0; i < Stat.NUM_BATTLE_STATS; i++)
			{
				int temp = user.getAttributes().getStage(i);
				user.getAttributes().setStage(i, victim.getAttributes().getStage(i));
				victim.getAttributes().setStage(i, temp);
			}
			b.addMessage(user.getName()+" swapped its stats with "+victim.getName()+"!");
		}
	}

	private static class DarkVoid extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public DarkVoid()
		{
			super("Dark Void", "Opposing Pok\u00e9mon are dragged into a world of total darkness that makes them sleep.", 10, Type.DARK, Category.STATUS);
			super.status = StatusCondition.ASLEEP;
			super.accuracy = 80;
		}
	}

	private static class SeedFlare extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SeedFlare()
		{
			super("Seed Flare", "The user emits a shock wave from its body to attack its target. It may harshly lower the target's Sp. Def.", 5, Type.GRASS, Category.SPECIAL);
			super.power = 120;
			super.accuracy = 85;
			super.statChanges[Stat.SP_DEFENSE.index()] = -2;
			super.effectChance = 40;
		}
	}

	private static class Judgment extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Judgment()
		{
			super("Judgment", "The user releases countless shots of light at the target. Its type varies with the kind of Plate the user is holding.", 10, Type.NORMAL, Category.SPECIAL);
			super.power = 100;
			super.accuracy = 100;
		}

		public Type getType(Battle b, ActivePokemon user)
		{
			if (user.hasAbility("Normalize")) return Type.NORMAL;
			Item i = user.getHeldItem(b);
			if (i instanceof PlateItem) return ((PlateItem)i).getType();
			return super.type;
		}
	}

	private static class SearingShot extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SearingShot()
		{
			super("Searing Shot", "An inferno of scarlet flames torches everything around the user. It may leave targets with a burn.", 5, Type.FIRE, Category.SPECIAL);
			super.power = 100;
			super.accuracy = 100;
			super.status = StatusCondition.BURNED;
			super.effectChance = 30;
		}
	}

	private static class Incinerate extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Incinerate()
		{
			super("Incinerate", "The user attacks the target with fire. If the target is holding a Berry, the Berry becomes burnt up and unusable.", 15, Type.FIRE, Category.SPECIAL);
			super.accuracy = 100;
			super.power = 60;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Item heldItem = victim.getHeldItem(b);
			if (heldItem instanceof Berry)
			{
				b.addMessage(victim.getName()+"'s "+heldItem.getName()+" was burned!");
				victim.consumeItem(b);
			}
		}
	}

	private static class Overheat extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Overheat()
		{
			super("Overheat", "The user attacks the target at full power. The attack's recoil harshly reduces the user's Sp. Atk stat.", 5, Type.FIRE, Category.SPECIAL);
			super.power = 140;
			super.accuracy = 90;
			super.statChanges[Stat.SP_ATTACK.index()] = -2;
			super.selfTarget = true;
		}
	}

	private static class HeatCrash extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public HeatCrash()
		{
			super("Heat Crash", "The user slams its target with its flame- covered body. The more the user outweighs the target, the greater the damage.", 10, Type.FIRE, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
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
			super("Grass Knot", "The user snares the target with grass and trips it. The heavier the target, the greater the damage.", 20, Type.GRASS, Category.SPECIAL);
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
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
			super("Scald", "The user shoots boiling hot water at its target. It may also leave the target with a burn.", 15, Type.WATER, Category.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.status = StatusCondition.BURNED;
			super.effectChance = 30;
		}
	}

	private static class DrainPunch extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public DrainPunch()
		{
			super("Drain Punch", "An energy-draining punch. The user's HP is restored by half the damage taken by the target.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.power = 75;
			super.moveTypes.add("Punching");
			super.accuracy = 100;
			super.moveTypes.add("SapHealth");
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class StormThrow extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public StormThrow()
		{
			super("Storm Throw", "The user strikes the target with a fierce blow. This attack always results in a critical hit.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add("AlwaysCrit");
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class RockSmash extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public RockSmash()
		{
			super("Rock Smash", "The user attacks with a punch that can shatter a rock. It may also lower the target's Defense stat.", 15, Type.FIGHTING, Category.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.effectChance = 50;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class RockClimb extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public RockClimb()
		{
			super("Rock Climb", "The user attacks the target by smashing into it with incredible force. It may also confuse the target.", 20, Type.NORMAL, Category.PHYSICAL);
			super.power = 90;
			super.accuracy = 85;
			super.effects.add(Effect.getEffect("Confusion", EffectType.POKEMON));
			super.effectChance = 20;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class NightDaze extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public NightDaze()
		{
			super("Night Daze", "The user lets loose a pitch-black shock wave at its target. It may also lower the target's accuracy.", 10, Type.DARK, Category.SPECIAL);
			super.power = 85;
			super.accuracy = 95;
			super.statChanges[Stat.ACCURACY.index()] = -1;
			super.effectChance = 40;
		}
	}

	private static class TailSlap extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public TailSlap()
		{
			super("Tail Slap", "The user attacks by striking the target with its hard tail. It hits the target two to five times in a row.", 10, Type.NORMAL, Category.PHYSICAL);
			super.power = 25;
			super.accuracy = 85;
			super.moveTypes.add("PhysicalContact");
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			int damage = 0, hits = (int)(Math.random()*(5-2+1))+2;
			if (me.hasAbility("Skill Link")) hits = 5;
			for (int i = 1; i <= hits; i++)
			{
				b.addMessage("Hit "+i+"!");
				damage += super.applyDamage(me, o, b);
			}
			b.addMessage("Hit "+hits+" times!");
			return damage;
		}
	}

	private static class Defog extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Defog()
		{
			super("Defog", "A strong wind blows away the target's obstacles such as Reflect or Light Screen. It also lowers the target's evasiveness.", 15, Type.FLYING, Category.STATUS);
			super.statChanges[Stat.EVASION.index()] = -1;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			for (int i = 0; i < victim.getEffects().size(); i++)
			{
				PokemonEffect e = victim.getEffects().get(i);
				if (e.isActive() && e instanceof DefogRelease)
				{
					b.addMessage(((DefogRelease)e).getDefogReleaseMessage(victim));
					victim.getEffects().remove(i--);
				}
			}
			for (int i = 0; i < b.getEffects(victim.user()).size(); i++)
			{
				TeamEffect e = b.getEffects(victim.user()).get(i);
				if (e.isActive() && e instanceof DefogRelease)
				{
					b.addMessage(((DefogRelease)e).getDefogReleaseMessage(victim));
					b.getEffects(victim.user()).remove(i--);
				}
			}
		}
	}

	private static class HornLeech extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public HornLeech()
		{
			super("Horn Leech", "The user drains the target's energy with its horns. The user's HP is restored by half the damage taken by the target.", 10, Type.GRASS, Category.PHYSICAL);
			super.accuracy = 100;
			super.power = 75;
			super.moveTypes.add("SapHealth");
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Electroweb extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Electroweb()
		{
			super("Electroweb", "The user captures and attacks opposing Pok\u00e9mon by using an electric net. It reduces the targets' Speed stat.", 15, Type.ELECTRIC, Category.SPECIAL);
			super.statChanges[Stat.SPEED.index()] = -1;
			super.power = 55;
			super.accuracy = 95;
		}
	}

	private static class GearGrind extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public GearGrind()
		{
			super("Gear Grind", "The user attacks by throwing two steel gears at its target.", 15, Type.STEEL, Category.PHYSICAL);
			super.accuracy = 85;
			super.power = 50;
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			int damage = 0, hits = (int)(Math.random()*(2-2+1))+2;
			for (int i = 1; i <= hits; i++)
			{
				b.addMessage("Hit "+i+"!");
				damage += super.applyDamage(me, o, b);
			}
			b.addMessage("Hit "+hits+" times!");
			return damage;
		}
	}

	private static class ShiftGear extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public ShiftGear()
		{
			super("Shift Gear", "The user rotates its gears, raising its Attack and sharply raising its Speed.", 10, Type.STEEL, Category.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.SPEED.index()] = 2;
		}
	}

	private static class HeadCharge extends Attack implements RecoilMove
	{
		private static final long serialVersionUID = 1L;
		private int recoilDamage(ActivePokemon user, int damage)
		{
			return (int)Math.ceil(damage/4.0);
		}

		public HeadCharge()
		{
			super("Head Charge", "The user charges its head into its target, using its powerful guard hair. It also damages the user a little.", 15, Type.NORMAL, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public void applyRecoil(Battle b, ActivePokemon user, int damage)
		{
			if (user.hasAbility("Rock Head") || user.hasAbility("Magic Guard")) return;
			b.addMessage(user.getName()+" was hurt by recoil!");
			b.applyDamage(user, recoilDamage(user, damage));
		}
	}

	private static class FieryDance extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FieryDance()
		{
			super("Fiery Dance", "Cloaked in flames, the user dances and flaps its wings. It may also raise the user's Sp. Atk stat.", 10, Type.FIRE, Category.SPECIAL);
			super.accuracy = 100;
			super.power = 80;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
			super.selfTarget = true;
			super.effectChance = 50;
		}
	}

	private static class SacredSword extends Attack implements StageChangingEffect
	{
		private static final long serialVersionUID = 1L;
		public SacredSword()
		{
			super("Sacred Sword", "The user attacks by slicing with its long horns. The target's stat changes don't affect this attack's damage.", 20, Type.FIGHTING, Category.PHYSICAL);
			super.power = 90;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public int adjustStage(int stage, Stat s, ActivePokemon p, ActivePokemon opp, Battle b, boolean user)
		{
			return !user ? 0 : stage;
		}
	}

	private static class SecretSword extends Attack implements StageChangingEffect
	{
		private static final long serialVersionUID = 1L;
		public SecretSword()
		{
			super("Secret Sword", "The user cuts with its long horn. The odd power contained in the horn does physical damage to the target.", 10, Type.FIGHTING, Category.SPECIAL);
			super.moveTypes.add("Metronomeless");
			super.accuracy = 100;
			super.power = 85;
		}

		public int adjustStage(int stage, Stat s, ActivePokemon p, ActivePokemon opp, Battle b, boolean user)
		{
			return !user ? 0 : stage;
		}
	}

	private static class FusionFlare extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FusionFlare()
		{
			super("Fusion Flare", "The user brings down a giant flame. This attack does greater damage when influenced by an enormous thunderbolt.", 5, Type.FIRE, Category.SPECIAL);
			super.power = 100;
			super.accuracy = 100;
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(!b.isFirstAttack() && o.getAttack().getName().equals("Fusion Bolt") ? 2 : 1);
		}
	}

	private static class FusionBolt extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FusionBolt()
		{
			super("Fusion Bolt", "The user throws down a giant thunderbolt. This attack does greater damage when influenced by an enormous flame.", 5, Type.ELECTRIC, Category.PHYSICAL);
			super.power = 100;
			super.accuracy = 100;
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(!b.isFirstAttack() && o.getAttack().getName().equals("Fusion Flare") ? 2 : 1);
		}
	}

	private static class BlueFlare extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public BlueFlare()
		{
			super("Blue Flare", "The user attacks by engulfing the target in an intense, yet beautiful, blue flame. It may leave the target with a burn.", 5, Type.FIRE, Category.SPECIAL);
			super.accuracy = 85;
			super.power = 130;
			super.status = StatusCondition.BURNED;
			super.effectChance = 20;
		}
	}

	private static class BoltStrike extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public BoltStrike()
		{
			super("Bolt Strike", "The user charges its target, surrounding itself with a great amount of electricity. It may leave the target with paralysis.", 5, Type.ELECTRIC, Category.PHYSICAL);
			super.accuracy = 85;
			super.power = 130;
			super.status = StatusCondition.PARALYZED;
			super.effectChance = 20;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Glaciate extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Glaciate()
		{
			super("Glaciate", "The user attacks by blowing freezing cold air at opposing Pok\u00e9mon. This attack reduces the targets' Speed stat.", 10, Type.ICE, Category.SPECIAL);
			super.accuracy = 95;
			super.power = 65;
			super.statChanges[Stat.SPEED.index()] = -1;
		}
	}

	private static class TechnoBlast extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public TechnoBlast()
		{
			super("Techno Blast", "The user fires a beam of light at its target. The type changes depending on the Drive the user holds.", 5, Type.NORMAL, Category.SPECIAL);
			super.power = 85;
			super.accuracy = 100;
			super.moveTypes.add("Metronomeless");
		}

		public Type getType(Battle b, ActivePokemon user)
		{
			if (user.hasAbility("Normalize")) return Type.NORMAL;
			Item i = user.getHeldItem(b);
			if (i instanceof DriveItem) return ((DriveItem)i).getType();
			return super.type;
		}
	}

	private static class Explosion extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Explosion()
		{
			super("Explosion", "The user explodes to inflict damage on those around it. The user faints upon using this move.", 5, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 100;
			super.power = 250;
			super.moveTypes.add("UserFaints");
		}
	}

	private static class Selfdestruct extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Selfdestruct()
		{
			super("Selfdestruct", "The user attacks everything around it by causing an explosion. The user faints upon using this move.", 5, Type.NORMAL, Category.PHYSICAL);
			super.power = 200;
			super.accuracy = 100;
			super.moveTypes.add("UserFaints");
		}
	}

	private static class Fling extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Fling()
		{
			super("Fling", "The user flings its held item at the target to attack. Its power and effects depend on the item.", 10, Type.DARK, Category.PHYSICAL);
			super.accuracy = 100;
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return 1; // TODO: FLING DAMAGE
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (me.hasEffect("Embargo"))
			{
				b.addMessage("...but it failed!");
				return;
			}
			super.apply(me, o, b);
		}
	}

	private static class FreezeShock extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FreezeShock()
		{
			super("Freeze Shock", "On the second turn, the user hits the target with electrically charged ice. It may leave the target with paralysis.", 5, Type.ICE, Category.PHYSICAL);
			super.moveTypes.add("Metronomeless");
			super.power = 140;
			super.accuracy = 90;
			super.status = StatusCondition.PARALYZED;
			super.effectChance = 30;
		}
	}

	private static class SecretPower extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public SecretPower()
		{
			super("Secret Power", "The user attacks the target with a secret power. Its added effects vary depending on the user's environment.", 20, Type.NORMAL, Category.PHYSICAL);
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
			super("Final Gambit", "The user risks everything to attack its target. The user faints but does damage equal to the user's HP.", 5, Type.FIGHTING, Category.SPECIAL);
			super.accuracy = 100;
			super.moveTypes.add("UserFaints");
			super.moveTypes.add("PhysicalContact");
		}

		public int applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
		{
			if (super.zeroAdvantage(b, me, o)) return -1;
			return b.applyDamage(o, me.getHP());
		}
	}

	private static class GastroAcid extends Attack implements ChangeAbilityMove
	{
		private static final long serialVersionUID = 1L;
		public GastroAcid()
		{
			super("Gastro Acid", "The user hurls up its stomach acids on the target. The fluid eliminates the effect of the target's Ability.", 10, Type.POISON, Category.STATUS);
			super.effects.add(Effect.getEffect("ChangeAbility", EffectType.POKEMON));
			super.accuracy = 100;
		}

		public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return Ability.getAbility("None").newInstance();
		}

		public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return caster.getName()+" suppressed "+victim.getName()+"'s ability!";
		}
	}

	private static class HealingWish extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public HealingWish()
		{
			super("Healing Wish", "The user faints. In return, the Pok\u00e9mon taking its place will have its HP restored and status cured.", 10, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect("HealSwitch", EffectType.TEAM));
			super.selfTarget = true;
			super.moveTypes.add("UserFaints");
			super.moveTypes.add("NonSnatchable");
		}
	}

	private static class LunarDance extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public LunarDance()
		{
			super("Lunar Dance", "The user faints. In return, the Pok\u00e9mon taking its place will have its status and HP fully restored.", 10, Type.PSYCHIC, Category.STATUS);
			super.effects.add(Effect.getEffect("HealSwitch", EffectType.TEAM));
			super.selfTarget = true;
			super.moveTypes.add("UserFaints");
			super.moveTypes.add("NonSnatchable");
		}
	}

	private static class Roar extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Roar()
		{
			super("Roar", "The target is scared off and replaced by another Pok\u00e9mon in its party. In the wild, the battle ends.", 20, Type.NORMAL, Category.STATUS);
			super.moveTypes.add("SubstitutePiercing");
			super.accuracy = 100;
			super.priority = -6;
			super.moveTypes.add("SoundBased");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Trainer trainer = b.isWildBattle() ? null : (Trainer)b.getTrainer(victim.user());
			if (trainer == null || !trainer.hasRemainingPokemon() || b.isFirstAttack() || victim.hasEffect("Ingrain"))
			{
				if (super.category == Category.STATUS) b.addMessage("...but it failed!");
				return;
			}

			if (victim.hasAbility("Suction Cups") && !user.breaksTheMold())
			{
				b.addMessage(victim.getName()+"'s Suction Cups prevents it from switching!");
				return;
			}

			b.addMessage(victim.getName()+" fled in fear!");
			trainer.switchToRandom();
			victim = trainer.front();
			b.enterBattle(victim, "...and "+victim.getName()+" was dragged out!");
		}
	}

	private static class Grudge extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Grudge()
		{
			super("Grudge", "If the user faints, the user's grudge fully depletes the PP of the opponent's move that knocked it out.", 5, Type.GHOST, Category.STATUS);
			super.effects.add(Effect.getEffect("Grudge", EffectType.POKEMON));
			super.selfTarget = true;
			super.moveTypes.add("SubstitutePiercing");
		}
	}

	private static class Retaliate extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Retaliate()
		{
			super("Retaliate", "The user gets revenge for a fainted ally. If an ally fainted in the previous turn, this attack's damage increases.", 5, Type.NORMAL, Category.PHYSICAL);
			super.accuracy = 100;
			super.accuracy = 100;
			super.power = 70;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return super.power*(Effect.hasEffect(b.getEffects(me.user()), "DeadAlly") ? 2 : 1);
		}
	}

	private static class CircleThrow extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public CircleThrow()
		{
			super("Circle Throw", "The user throws the target and drags out another Pok\u00e9mon in its party. In the wild, the battle ends.", 10, Type.FIGHTING, Category.PHYSICAL);
			super.accuracy = 90;
			super.power = 60;
			super.priority = -6;
			super.moveTypes.add("Assistless");
			super.moveTypes.add("PhysicalContact");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Trainer trainer = b.isWildBattle() ? null : (Trainer)b.getTrainer(victim.user());
			if (trainer == null || !trainer.hasRemainingPokemon() || b.isFirstAttack() || victim.hasEffect("Ingrain"))
			{
				if (super.category == Category.STATUS) b.addMessage("...but it failed!");
				return;
			}

			if (victim.hasAbility("Suction Cups") && !user.breaksTheMold())
			{
				b.addMessage(victim.getName()+"'s Suction Cups prevents it from switching!");
				return;
			}

			b.addMessage(victim.getName()+" was thrown away!");
			trainer.switchToRandom();
			victim = trainer.front();
			b.enterBattle(victim, "...and "+victim.getName()+" was dragged out!");
		}
	}

	private static class Teleport extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Teleport()
		{
			super("Teleport", "Use it to flee from any wild Pok\u00e9mon. It can also warp to the last Pok\u00e9mon Center visited.", 20, Type.PSYCHIC, Category.STATUS);
			super.moveTypes.add("Field");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (!b.isWildBattle())
			{
				b.addMessage("...but it failed!");
				return;
			}
			b.addMessage(user.getName()+" teleported out of battle!");
			b.addMessage(" ", MessageUpdate.Update.EXIT_BATTLE);
		}
	}

	private static class RolePlay extends Attack implements ChangeAbilityMove
	{
		private static final long serialVersionUID = 1L;
		public RolePlay()
		{
			super("Role Play", "The user mimics the target completely, copying the target's natural Ability.", 10, Type.PSYCHIC, Category.STATUS);
			super.moveTypes.add("SubstitutePiercing");
			super.selfTarget = true;
			super.effects.add(Effect.getEffect("ChangeAbility", EffectType.POKEMON));
			super.moveTypes.add("ProtectPiercing");
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			switch (o.getAbility().getName())
			{
				case "Wonder Guard":
				case "Multitype":
				b.addMessage("...but it failed!");
				break;
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
			return victim.getName()+" copied "+other.getName()+"'s "+other.getAbility().getName()+"!";
		}
	}

	private static class KnockOff extends Attack implements ItemCondition
	{
		private static final long serialVersionUID = 1L;
		public KnockOff()
		{
			super("Knock Off", "The user slaps down the target's held item, preventing that item from being used in the battle.", 20, Type.DARK, Category.PHYSICAL);
			super.accuracy = 100;
			super.power = 65;
			super.effects.add(Effect.getEffect("ChangeItem", EffectType.POKEMON));
			super.moveTypes.add("PhysicalContact");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (!victim.isHoldingItem(b) || victim.hasAbility("Sticky Hold")) return;
			b.addMessage(user.getName()+" knocked off "+victim.getName()+"'s "+victim.getHeldItem(b).getName()+"!");
			super.applyEffects(b, user, victim);
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return (int)(super.power*(o.isHoldingItem(b) ? 1.5 : 1));
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
			super("Whirlwind", "The target is blown away, to be replaced by another Pok\u00e9mon in its party. In the wild, the battle ends.", 20, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.moveTypes.add("SubstitutePiercing");
			super.priority = -6;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Trainer trainer = b.isWildBattle() ? null : (Trainer)b.getTrainer(victim.user());
			if (trainer == null || !trainer.hasRemainingPokemon() || b.isFirstAttack() || victim.hasEffect("Ingrain"))
			{
				if (super.category == Category.STATUS) b.addMessage("...but it failed!");
				return;
			}

			if (victim.hasAbility("Suction Cups") && !user.breaksTheMold())
			{
				b.addMessage(victim.getName()+"'s Suction Cups prevents it from switching!");
				return;
			}

			b.addMessage(victim.getName()+" blew away!");
			trainer.switchToRandom();
			victim = trainer.front();
			b.enterBattle(victim, "...and "+victim.getName()+" was dragged out!");
		}
	}

	private static class Bestow extends Attack implements ItemCondition
	{
		private static final long serialVersionUID = 1L;
		private Item item;

		public Bestow()
		{
			super("Bestow", "The user passes its held item to the target when the target isn't holding an item.", 15, Type.NORMAL, Category.STATUS);
			super.effects.add(Effect.getEffect("ChangeItem", EffectType.POKEMON));
			super.moveTypes.add("Metronomeless");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (!user.isHoldingItem(b) || victim.isHoldingItem(b))
			{
				if (super.category == Category.STATUS) b.addMessage("...but it failed!");
				return;
			}

			Item userItem = user.getHeldItem(b), victimItem = victim.getHeldItem(b);
			b.addMessage(user.getName()+" gave "+victim.getName()+" its "+userItem.getName()+"!");

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
			super("Switcheroo", "The user passes its held item to the target when the target isn't holding an item.", 10, Type.DARK, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("ChangeItem", EffectType.POKEMON));
			super.moveTypes.add("Assistless");
			super.moveTypes.add("Metronomeless");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if ((!user.isHoldingItem(b) && !victim.isHoldingItem(b)) || user.hasAbility("Sticky Hold") || victim.hasAbility("Sticky Hold"))
			{
				if (super.category == Category.STATUS) b.addMessage("...but it failed!");
				return;
			}

			Item userItem = user.getHeldItem(b), victimItem = victim.getHeldItem(b);
			b.addMessage(user.getName()+" switched items with "+victim.getName()+"!");

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
			super("Trick", "The user catches the target off guard and swaps its held item with its own.", 10, Type.PSYCHIC, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("ChangeItem", EffectType.POKEMON));
			super.moveTypes.add("Assistless");
			super.moveTypes.add("Metronomeless");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if ((!user.isHoldingItem(b) && !victim.isHoldingItem(b)) || user.hasAbility("Sticky Hold") || victim.hasAbility("Sticky Hold"))
			{
				if (super.category == Category.STATUS) b.addMessage("...but it failed!");
				return;
			}

			Item userItem = user.getHeldItem(b), victimItem = victim.getHeldItem(b);
			b.addMessage(user.getName()+" switched items with "+victim.getName()+"!");

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
			super("Memento", "The user faints when using this move. In return, it harshly lowers the target's Attack and Sp. Atk.", 10, Type.DARK, Category.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.ATTACK.index()] = -2;
			super.statChanges[Stat.SP_ATTACK.index()] = -2;
			super.moveTypes.add("UserFaints");
		}
	}

	private static class DestinyBond extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public DestinyBond()
		{
			super("Destiny Bond", "When this move is used, if the user faints, the Pok\u00e9mon that landed the knockout hit also faints.", 5, Type.GHOST, Category.STATUS);
			super.selfTarget = true;
			super.effects.add(Effect.getEffect("DestinyBond", EffectType.POKEMON));
			super.moveTypes.add("SubstitutePiercing");
			super.moveTypes.add("Assistless");
			super.moveTypes.add("Metronomeless");
		}
	}

	private static class Camouflage extends Attack implements ChangeTypeMove
	{
		private static final long serialVersionUID = 1L;
		public Camouflage()
		{
			super("Camouflage", "The user's type is changed depending on its environment, such as at water's edge, in grass, or in a cave.", 20, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
			super.effects.add(Effect.getEffect("ChangeType", EffectType.POKEMON));
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
			super("Recycle", "The user recycles a held item that has been used in battle so it can be used again.", 10, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
			super.selfTarget = true;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			PokemonEffect consumed = victim.getEffect("ConsumedItem");
			if (consumed == null || victim.isHoldingItem(b))
			{
				b.addMessage("...but it failed!");
				return;
			}
			Item restored = ((ItemCondition)consumed).getItem();
			victim.giveItem((HoldItem)restored);
			b.addMessage(victim.getName()+"'s "+restored.getName()+" was restored!");
		}
	}

	private static class UTurn extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public UTurn()
		{
			super("U-turn", "After making its attack, the user rushes back to switch places with a party Pok\u00e9mon in waiting.", 20, Type.BUG, Category.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Team t = b.getTrainer(user.user());
			if (t instanceof WildPokemon)
			{
				b.addMessage(user.getName()+" left the battle!");
				b.addMessage(" ", MessageUpdate.Update.EXIT_BATTLE);
				return;
			}

			Trainer trainer = (Trainer)t;
			if (!trainer.hasRemainingPokemon()) return;

			b.addMessage(user.getName()+" went back to "+trainer.getName()+"!");
			trainer.switchToRandom(); // TODO: Prompt a legit switch fo user
			user = trainer.front();
			b.enterBattle(user, trainer.getName()+" sent out "+user.getName()+"!");
		}
	}

	private static class BatonPass extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public BatonPass()
		{
			super("Baton Pass", "The user switches places with a party Pok\u00e9mon in waiting, passing along any stat changes.", 40, Type.NORMAL, Category.STATUS);
			super.selfTarget = true;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Team t = b.getTrainer(user.user());
			if (t instanceof WildPokemon)
			{
				b.addMessage(user.getName()+" left the battle!");
				b.addMessage(" ", MessageUpdate.Update.EXIT_BATTLE);
				return;
			}
			
			Trainer trainer = (Trainer)t;
			if (!trainer.hasRemainingPokemon())
			{
				b.addMessage("...but it failed!");
				return;
			}
			
			b.addMessage(user.getName()+" went back to "+trainer.getName()+"!");
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
			b.enterBattle(user, trainer.getName()+" sent out "+user.getName()+"!", false);
		}
	}

	private static class PerishSong extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public PerishSong()
		{
			super("Perish Song", "Any Pok\u00e9mon that hears this song faints in three turns, unless it switches out of battle.", 5, Type.NORMAL, Category.STATUS);
			super.moveTypes.add("ProtectPiercing");
			super.effects.add(Effect.getEffect("PerishSong", EffectType.POKEMON));
			super.moveTypes.add("SubstitutePiercing");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			super.applyEffects(b, user, victim);
			super.applyEffects(b, user, user);
		}
	}

	private static class DragonTail extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public DragonTail()
		{
			super("Dragon Tail", "The user knocks away the target and drags out another Pok\u00e9mon in its party. In the wild, the battle ends.", 10, Type.DRAGON, Category.PHYSICAL);
			super.accuracy = 90;
			super.power = 60;
			super.priority = -6;
			super.moveTypes.add("Assistless");
			super.moveTypes.add("PhysicalContact");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Trainer trainer = b.isWildBattle() ? null : (Trainer)b.getTrainer(victim.user());
			if (trainer == null || !trainer.hasRemainingPokemon() || b.isFirstAttack() || victim.hasEffect("Ingrain"))
			{
				if (super.category == Category.STATUS) b.addMessage("...but it failed!");
				return;
			}

			if (victim.hasAbility("Suction Cups") && !user.breaksTheMold())
			{
				b.addMessage(victim.getName()+"'s Suction Cups prevents it from switching!");
				return;
			}

			b.addMessage(victim.getName()+" was slapped away!");
			trainer.switchToRandom();
			victim = trainer.front();
			b.enterBattle(victim, "...and "+victim.getName()+" was dragged out!");
		}
	}

	private static class FoulPlay extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FoulPlay()
		{
			super("Foul Play", "The user turns the target's power against it. The higher the target's Attack stat, the greater the damage.", 15, Type.DARK, Category.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add("PhysicalContact");
		}

		public int getPower(Battle b, ActivePokemon me, ActivePokemon o)
		{
			double ratio = (double)Stat.getStat(Stat.ATTACK, me, o, b)/Stat.getStat(Stat.ATTACK, o, me, b);
			if (ratio > .5) return 40;
			if (ratio > .33) return 60;
			if (ratio > .25) return 80;
			if (ratio > .2) return 100;
			return 120;
		}
	}

	private static class Embargo extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Embargo()
		{
			super("Embargo", "It prevents the target from using its held item. Its Trainer is also prevented from using items on it.", 15, Type.DARK, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("Embargo", EffectType.POKEMON));
		}
	}

	private static class NaturePower extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public NaturePower()
		{
			super("Nature Power", "An attack that makes use of nature's power. Its effects vary depending on the user's environment.", 20, Type.NORMAL, Category.STATUS);
			super.moveTypes.add("Assistless");
			super.moveTypes.add("Metronomeless");
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			// TODO: Battle environments
			me.callNewMove(b, o, new Move(Attack.getAttack("Tri Attack")));
		}

		public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o)
		{
			return Attack.getAttack("Tri Attack").getAccuracy(b, me, o);
		}
	}

	private static class Entrainment extends Attack implements ChangeAbilityMove
	{
		private static final long serialVersionUID = 1L;
		public Entrainment()
		{
			super("Entrainment", "The user dances with an odd rhythm that compels the target to mimic it, making the target's Ability the same as the user's.", 15, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("ChangeAbility", EffectType.POKEMON));
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			switch (o.getAbility().getName())
			{
				case "Truant":
				case "Multitype":
				b.addMessage("...but it failed!");
				break;
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
			return victim.getName()+" copied "+caster.getName()+"'s "+caster.getAbility().getName()+"!";
		}
	}

	private static class MagicRoom extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public MagicRoom()
		{
			super("Magic Room", "The user creates a bizarre area in which Pok\u00e9mon's held items lose their effects for five turns.", 10, Type.PSYCHIC, Category.STATUS);
			super.priority = -7;
			super.effects.add(Effect.getEffect("MagicRoom", EffectType.BATTLE));
			super.moveTypes.add("Field");
		}
	}

	private static class SimpleBeam extends Attack implements ChangeAbilityMove
	{
		private static final long serialVersionUID = 1L;
		public SimpleBeam()
		{
			super("Simple Beam", "The user's mysterious psychic wave changes the target's Ability to Simple.", 15, Type.NORMAL, Category.STATUS);
			super.accuracy = 100;
			super.effects.add(Effect.getEffect("ChangeAbility", EffectType.POKEMON));
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b)
		{
			switch (o.getAbility().getName())
			{
				case "Truant":
				case "Multitype":
				b.addMessage("...but it failed!");
				break;
				default:
				super.apply(me, o, b);
			}
		}

		public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return Ability.getAbility("Simple").newInstance();
		}

		public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim)
		{
			return victim.getName()+"'s ability was changed to Simple!";
		}
	}

	private static class SkillSwap extends Attack implements ChangeAbilityMove
	{
		private static final long serialVersionUID = 1L;
		private Ability ability, temp;

		public SkillSwap()
		{
			super("Skill Swap", "The user employs its psychic power to exchange Abilities with the target.", 10, Type.PSYCHIC, Category.STATUS);
			super.moveTypes.add("Field");
			super.moveTypes.add("SubstitutePiercing");
			super.effects.add(Effect.getEffect("ChangeAbility", EffectType.POKEMON));
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			if (victim.hasAbility("Wonder Guard") || victim.hasAbility("Multitype"))
			{
				b.addMessage("...but it failed!");
				return;
			}
			
			ability = user.getAbility();
			temp = victim.getAbility();
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
			return victim.getName()+"'s ability was changed to "+ability.getName()+"!";
		}
	}

	private static class VoltSwitch extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public VoltSwitch()
		{
			super("Volt Switch", "After making its attack, the user rushes back to switch places with a party Pok\u00e9mon in waiting.", 20, Type.ELECTRIC, Category.SPECIAL);
			super.power = 70;
			super.accuracy = 100;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim)
		{
			Team t = b.getTrainer(user.user());
			if (t instanceof WildPokemon)
			{
				b.addMessage(user.getName()+" left the battle!");
				b.addMessage(" ", MessageUpdate.Update.EXIT_BATTLE);
				return;
			}

			Trainer trainer = (Trainer)t;
			if (!trainer.hasRemainingPokemon()) return;

			b.addMessage(user.getName()+" went back to "+trainer.getName()+"!");
			trainer.switchToRandom(); // TODO: Prompt a legit switch fo user
			user = trainer.front();
			b.enterBattle(user, trainer.getName()+" sent out "+user.getName()+"!");
		}
	}

	private static class RelicSong extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public RelicSong()
		{
			super("Relic Song", "The user sings an ancient song and attacks by appealing to the hearts of those listening. It may also induce sleep.", 10, Type.NORMAL, Category.SPECIAL);
			super.accuracy = 100;
			super.power = 75;
			super.status = StatusCondition.ASLEEP;
			super.moveTypes.add("Metronomeless");
			super.effectChance = 10;
			super.moveTypes.add("SoundBased");
		}
	}

	private static class Snarl extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Snarl()
		{
			super("Snarl", "The user yells as if it is ranting about something, making the target's Sp. Atk stat decrease.", 15, Type.DARK, Category.SPECIAL);
			super.moveTypes.add("SoundBased");
			super.moveTypes.add("Metronomeless");
			super.accuracy = 95;
			super.power = 55;
			super.statChanges[Stat.SP_ATTACK.index()] = -1;
		}
	}

	private static class IceBurn extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public IceBurn()
		{
			super("Ice Burn", "On the second turn, an ultracold, freezing wind surrounds the target. This may leave the target with a burn.", 5, Type.ICE, Category.SPECIAL);
			super.moveTypes.add("Metronomeless");
			super.power = 140;
			super.accuracy = 90;
			super.effectChance = 30;
			super.status = StatusCondition.BURNED;
		}
	}

	private static class VCreate extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public VCreate()
		{
			super("V-create", "With a hot flame on its forehead, the user hurls itself at its target. It lowers the user's Defense, Sp. Def, and Speed stats.", 5, Type.FIRE, Category.PHYSICAL);
			super.accuracy = 95;
			super.power = 180;
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
			super.statChanges[Stat.SPEED.index()] = -1;
			super.moveTypes.add("PhysicalContact");
		}
	}

	private static class Surf extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public Surf()
		{
			super("Surf", "It swamps the area around the user with a giant wave. It can also be used for crossing water.", 15, Type.WATER, Category.SPECIAL);
			super.power = 95;
			super.accuracy = 100;
		}
	}

	private static class VoltTackle extends Attack implements RecoilMove
	{
		private static final long serialVersionUID = 1L;
		private int recoilDamage(ActivePokemon user, int damage)
		{
			return (int)Math.ceil(damage/3.0);
		}

		public VoltTackle()
		{
			super("Volt Tackle", "The user electrifies itself, then charges. It causes considerable damage to the user and may leave the target with paralysis.", 15, Type.ELECTRIC, Category.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.status = StatusCondition.PARALYZED;
			super.effectChance = 10;
			super.moveTypes.add("PhysicalContact");
		}

		public void applyRecoil(Battle b, ActivePokemon user, int damage)
		{
			if (user.hasAbility("Rock Head") || user.hasAbility("Magic Guard")) return;
			b.addMessage(user.getName()+" was hurt by recoil!");
			b.applyDamage(user, recoilDamage(user, damage));
		}
	}

	private static class FocusBlast extends Attack 
	{
		private static final long serialVersionUID = 1L;
		public FocusBlast()
		{
			super("Focus Blast", "The user heightens its mental focus and unleashes its power. It may also lower the target's Sp. Def.", 5, Type.FIGHTING, Category.SPECIAL);
			super.power = 120;
			super.accuracy = 70;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
			super.effectChance = 10;
		}
	}

}
