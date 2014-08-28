package item;

import item.Bag.BagCategory;
import item.Bag.BattleBagCategory;
import item.berry.Berry;
import item.berry.GainableEffectBerry;
import item.berry.HealthTriggeredBerry;
import item.berry.StatusBerry;
import item.hold.ConsumedItem;
import item.hold.DriveItem;
import item.hold.HoldItem;
import item.hold.PlateItem;
import item.use.BallItem;
import item.use.BattleUseItem;
import item.use.MoveUseItem;
import item.use.PokemonUseItem;
import item.use.TrainerUseItem;
import item.use.TypeItem;
import item.use.UseItem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import main.Global;
import main.Type;
import pokemon.ActivePokemon;
import pokemon.BaseEvolution;
import pokemon.Evolution;
import pokemon.Evolution.EvolutionCheck;
import pokemon.Stat;
import trainer.CharacterData;
import trainer.Team;
import trainer.Trainer;
import trainer.Trainer.Action;
import trainer.WildPokemon;
import battle.Attack.Category;
import battle.Battle;
import battle.Move;
import battle.effect.ApplyDamageEffect;
import battle.effect.AttackSelectionEffect;
import battle.effect.BracingEffect;
import battle.effect.CritStageEffect;
import battle.effect.DefiniteEscape;
import battle.effect.Effect.CastSource;
import battle.effect.EffectBlockerEffect;
import battle.effect.EndTurnEffect;
import battle.effect.EntryEffect;
import battle.effect.GroundedEffect;
import battle.effect.ItemCondition;
import battle.effect.LevitationEffect;
import battle.effect.OpponentPowerChangeEffect;
import battle.effect.PhysicalContactEffect;
import battle.effect.PokemonEffect;
import battle.effect.PowerChangeEffect;
import battle.effect.RepellingEffect;
import battle.effect.StallingEffect;
import battle.effect.StatChangingEffect;
import battle.effect.StatProtectingEffect;
import battle.effect.Status;
import battle.effect.Status.StatusCondition;
import battle.effect.TakeDamageEffect;
import battle.effect.TeamEffect;
import battle.effect.Weather.WeatherType;
import battle.effect.WeatherBlockerEffect;
import battle.effect.WeatherExtendingEffect;

public abstract class Item implements Comparable<Item>, Serializable {
	private static final long serialVersionUID = 1L;

	private static HashMap<String, Item> map;

	protected String name, desc;
	protected BagCategory cat;
	protected BattleBagCategory[] bcat;
	protected int price, index;

	public static boolean exists(String s) {
		if (map == null)
			loadItems();

		if (!map.containsKey(s))
			return false;
		return true;
	}

	private void readObject(ObjectInputStream ois) throws IOException,
			ClassNotFoundException {
		ois.defaultReadObject();
		synchronized (Item.class) {
			if (map == null)
				loadItems();
			map.put(this.getName(), this);
		}
	}

	public Item() {
		this.name = this.desc = "UNDEFINED";
		this.cat = BagCategory.MISC;
		bcat = new BattleBagCategory[0];
		this.price = -1;
		this.index = 0;
	}

	public int compareTo(Item o) {
		return this.name.compareTo(o.name);
	}

	public boolean isUsable() {
		return this instanceof UseItem;
	}

	public boolean isHoldable() {
		return this instanceof HoldItem;
	}

	public String getName() {
		return this.name;
	}

	public String getDesc() {
		return this.desc;
	}

	public int getPrice() {
		return this.price;
	}

	public int getIndex() {
		return index;
	}

	public static Item noneItem() {
		return getItem("None");
	}

	public static Item getItem(String m) {
		if (map == null)
			loadItems();
		if (map.containsKey(m))
			return map.get(m);

		Global.error("No such Item " + m);
		return null;
	}

	public static boolean isItem(String m) {
		if (map == null)
			loadItems();
		if (map.containsKey(m))
			return true;
		return false;
	}

	public boolean equals(Object o) {
		if (!(o instanceof Item))
			return false;
		return (((Item) o).getName().equals(name));
	}

	public int hashCode() {
		return name.hashCode();
	}

	/* Helper abstract classes */

	private static abstract class StageIncreaseItem extends Item implements
			BattleUseItem {
		private static final long serialVersionUID = 1L;

		public abstract Stat toIncrease();

		public boolean use(ActivePokemon p, Battle b) {
			return p.getAttributes().modifyStage(p, p, 1, toIncrease(), b,
					CastSource.USE_ITEM);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toIncrease().getName()
					+ " was raised!";
		}
	}

	private static abstract class TypeEnhancingItem extends Item implements
			PowerChangeEffect, TypeItem {
		private static final long serialVersionUID = 1L;

		public abstract double getMultiplier();

		public double getMultiplier(Battle b, ActivePokemon me, ActivePokemon o) {
			if (me.getAttack().isType(b, me, getType())) {
				if (this instanceof ConsumedItem) {
					b.addMessage(me.getName() + "'s " + this.getName()
							+ " enhanced " + me.getAttack().getName()
							+ "'s power!");
					me.consumeItem(b);
				}

				return getMultiplier();
			}

			return 1;
		}
	}

	private static abstract class StatusConditionRemoveItem extends Item
			implements PokemonUseItem, BattleUseItem {
		private static final long serialVersionUID = 1L;

		public abstract StatusCondition toRemove();

		public boolean use(ActivePokemon p) {
			if (!p.hasStatus(toRemove()))
				return false;

			p.removeStatus();
			return true;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(p);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " was cured of its status condition!";
		}
	}

	private static abstract class EVIncreaseItem extends Item implements
			PokemonUseItem {
		private static final long serialVersionUID = 1L;

		public abstract Stat toIncrease();

		public abstract int increaseAmt();

		public boolean use(ActivePokemon p) {
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += 10;

			return p.addEVs(toAdd);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toIncrease().getName()
					+ " was raised!";
		}
	}

	private static abstract class HealItem extends Item implements
			PokemonUseItem, BattleUseItem {
		private static final long serialVersionUID = 1L;

		public abstract int healAmt();

		public boolean use(ActivePokemon p) {
			return p.heal(healAmt()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(p);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s health was restored!";
		}
	}

	private static abstract class EvolutionItem extends Item implements
			PokemonUseItem {
		private static final long serialVersionUID = 1L;

		private String message;

		public boolean use(ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(
					EvolutionCheck.ITEM, p, this.getName());
			if (base == null)
				return false;

			message = p.evolve(null, base);
			return true;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return message;
		}
	}

	public static abstract class PowerItem extends Item implements
			StatChangingEffect {
		private static final long serialVersionUID = 1L;

		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s,
				Battle b) {
			if (s.equals(Stat.SPEED))
				return stat / 2;
			return stat;
		}

		public int[] getEVs(int[] vals) {
			vals[toIncrease().index()] += 4;
			return vals;
		}

		public abstract Stat toIncrease();
	}

	private static abstract class ChoiceItem extends Item implements HoldItem,
			StatChangingEffect, AttackSelectionEffect {
		private static final long serialVersionUID = 1L;

		public abstract Stat toIncrease();

		public int flingDamage() {
			return 10;
		}

		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s,
				Battle b) {
			if (s == toIncrease())
				stat *= 1.5;
			return stat;
		}

		public boolean usable(ActivePokemon p, Move m) {
			Move last = p.getAttributes().getLastMoveUsed();
			if (last == null || m == last)
				return true;
			return false;
		}

		public String getUnusableMessage(ActivePokemon p) {
			return p.getName() + "'s " + super.name + " only allows "
					+ p.getAttributes().getLastMoveUsed().getAttack().getName()
					+ " to be used!";
		}
	}

	private static abstract class TypeDamageStatIncreaseItem extends Item
			implements HoldItem, ConsumedItem, TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		public abstract Type damageType();

		public abstract Stat toIncrease();

		public void takeDamage(Battle b, ActivePokemon user,
				ActivePokemon victim) {
			if (user.getAttack().getType(b, user) == damageType()
					&& victim.getAttributes().modifyStage(victim, victim, 1,
							toIncrease(), b, CastSource.HELD_ITEM)) {
				victim.consumeItem(b);
			}
		}
	}

	private static abstract class RepelItem extends Item implements HoldItem,
			TrainerUseItem {
		private static final long serialVersionUID = 1L;

		public abstract int repelSteps();

		public int flingDamage() {
			return 30;
		}

		public boolean use(Trainer t) {
			if (!(t instanceof CharacterData))
				return false;

			CharacterData player = (CharacterData) t;
			if (player.isUsingRepel())
				return false;

			player.addRepelSteps(repelSteps());
			return true;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "Weak wild Pok\u00e9mon will not appear for " + repelSteps()
					+ " steps!";
		}
	}

	public static abstract class EVDecreaseBerry extends Item implements Berry,
			PokemonUseItem {
		private static final long serialVersionUID = 1L;

		public boolean use(ActivePokemon p) {
			int[] vals = new int[Stat.NUM_STATS];
			if (p.getEV(toDecrease().index()) > 110)
				vals[toDecrease().index()] = 100 - p
						.getEV(toDecrease().index());
			else
				vals[toDecrease().index()] -= 10;

			return p.addEVs(vals);
		}

		abstract Stat toDecrease();

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toDecrease().getName()
					+ " was lowered!";
		}
	}

	public static abstract class SuperEffectivePowerReduceBerry extends Item
			implements Berry, OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		public double getOppMultiplier(Battle b, ActivePokemon user,
				ActivePokemon victim) {
			Type t = user.getAttack().getType(b, user);

			if (t == getType() && Type.getAdvantage(t, victim, b) > 1) {
				b.addMessage(victim.getName() + "'s " + this.name
						+ " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}

			return 1;
		}

		abstract Type getType();
	}

	public static abstract class HealthTriggeredStageIncreaseBerry extends Item
			implements HealthTriggeredBerry {
		private static final long serialVersionUID = 1L;

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user) {
			if (user.getAttributes().modifyStage(user, user, 1, toRaise(), b,
					CastSource.HELD_ITEM)) {
				return true;
			}

			return false;
		}

		public double healthTriggerRatio() {
			return 1 / 4.0;
		}

		public abstract Stat toRaise();

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + this.name + " raised its "
					+ toRaise().getName() + "!";
		}

		public void useBerry(Battle b, ActivePokemon user, ActivePokemon opp) {
			useHealthTriggerBerry(b, user);
		}
	}

	public static void loadItems() {
		if (map != null)
			return;
		map = new HashMap<>();

		// EVERYTHING BELOW IS GENERATED ###
		map.put("None", new None());
		map.put("Syrup", new Syrup());
		map.put("Bicycle", new Bicycle());
		map.put("Surfboard", new Surfboard());
		map.put("Fishing Rod", new FishingRod());
		map.put("Absorb Bulb", new AbsorbBulb());
		map.put("Air Balloon", new AirBalloon());
		map.put("Amulet Coin", new AmuletCoin());
		map.put("Big Root", new BigRoot());
		map.put("Binding Band", new BindingBand());
		map.put("Black Sludge", new BlackSludge());
		map.put("Bright Powder", new BrightPowder());
		map.put("Cell Battery", new CellBattery());
		map.put("Choice Band", new ChoiceBand());
		map.put("Choice Scarf", new ChoiceScarf());
		map.put("Choice Specs", new ChoiceSpecs());
		map.put("Cleanse Tag", new CleanseTag());
		map.put("Damp Rock", new DampRock());
		map.put("Heat Rock", new HeatRock());
		map.put("Icy Rock", new IcyRock());
		map.put("Smooth Rock", new SmoothRock());
		map.put("Eject Button", new EjectButton());
		map.put("Destiny Knot", new DestinyKnot());
		map.put("Expert Belt", new ExpertBelt());
		map.put("Flame Orb", new FlameOrb());
		map.put("Toxic Orb", new ToxicOrb());
		map.put("Float Stone", new FloatStone());
		map.put("Focus Band", new FocusBand());
		map.put("Focus Sash", new FocusSash());
		map.put("Grip Claw", new GripClaw());
		map.put("Griseous Orb", new GriseousOrb());
		map.put("Iron Ball", new IronBall());
		map.put("Lagging Tail", new LaggingTail());
		map.put("Life Orb", new LifeOrb());
		map.put("Light Ball", new LightBall());
		map.put("Light Clay", new LightClay());
		map.put("Lucky Egg", new LuckyEgg());
		map.put("Lucky Punch", new LuckyPunch());
		map.put("Macho Brace", new MachoBrace());
		map.put("Mental Herb", new MentalHerb());
		map.put("Metal Powder", new MetalPowder());
		map.put("Metronome", new Metronome());
		map.put("Muscle Band", new MuscleBand());
		map.put("Power Anklet", new PowerAnklet());
		map.put("Power Band", new PowerBand());
		map.put("Power Belt", new PowerBelt());
		map.put("Power Bracer", new PowerBracer());
		map.put("Power Lens", new PowerLens());
		map.put("Power Weight", new PowerWeight());
		map.put("Quick Claw", new QuickClaw());
		map.put("Quick Powder", new QuickPowder());
		map.put("Red Card", new RedCard());
		map.put("Ring Target", new RingTarget());
		map.put("Rocky Helmet", new RockyHelmet());
		map.put("Safety Goggles", new SafetyGoggles());
		map.put("Scope Lens", new ScopeLens());
		map.put("Shed Shell", new ShedShell());
		map.put("Shell Bell", new ShellBell());
		map.put("Smoke Ball", new SmokeBall());
		map.put("Snowball", new Snowball());
		map.put("Soul Dew", new SoulDew());
		map.put("Stick", new Stick());
		map.put("Sticky Barb", new StickyBarb());
		map.put("Thick Club", new ThickClub());
		map.put("Weakness Policy", new WeaknessPolicy());
		map.put("White Herb", new WhiteHerb());
		map.put("Wide Lens", new WideLens());
		map.put("Wise Glasses", new WiseGlasses());
		map.put("Zoom Lens", new ZoomLens());
		map.put("Full Incense", new FullIncense());
		map.put("Lax Incense", new LaxIncense());
		map.put("Luck Incense", new LuckIncense());
		map.put("Odd Incense", new OddIncense());
		map.put("Pure Incense", new PureIncense());
		map.put("Rock Incense", new RockIncense());
		map.put("Rose Incense", new RoseIncense());
		map.put("Sea Incense", new SeaIncense());
		map.put("Wave Incense", new WaveIncense());
		map.put("Draco Plate", new DracoPlate());
		map.put("Dread Plate", new DreadPlate());
		map.put("Earth Plate", new EarthPlate());
		map.put("Fist Plate", new FistPlate());
		map.put("Flame Plate", new FlamePlate());
		map.put("Icicle Plate", new IciclePlate());
		map.put("Insect Plate", new InsectPlate());
		map.put("Iron Plate", new IronPlate());
		map.put("Meadow Plate", new MeadowPlate());
		map.put("Mind Plate", new MindPlate());
		map.put("Sky Plate", new SkyPlate());
		map.put("Splash Plate", new SplashPlate());
		map.put("Spooky Plate", new SpookyPlate());
		map.put("Stone Plate", new StonePlate());
		map.put("Toxic Plate", new ToxicPlate());
		map.put("Zap Plate", new ZapPlate());
		map.put("Burn Drive", new BurnDrive());
		map.put("Chill Drive", new ChillDrive());
		map.put("Douse Drive", new DouseDrive());
		map.put("Shock Drive", new ShockDrive());
		map.put("Fire Gem", new FireGem());
		map.put("Water Gem", new WaterGem());
		map.put("Electric Gem", new ElectricGem());
		map.put("Grass Gem", new GrassGem());
		map.put("Ice Gem", new IceGem());
		map.put("Fighting Gem", new FightingGem());
		map.put("Poison Gem", new PoisonGem());
		map.put("Ground Gem", new GroundGem());
		map.put("Flying Gem", new FlyingGem());
		map.put("Psychic Gem", new PsychicGem());
		map.put("Bug Gem", new BugGem());
		map.put("Rock Gem", new RockGem());
		map.put("Ghost Gem", new GhostGem());
		map.put("Dragon Gem", new DragonGem());
		map.put("Dark Gem", new DarkGem());
		map.put("Steel Gem", new SteelGem());
		map.put("Normal Gem", new NormalGem());
		map.put("Leftovers", new Leftovers());
		map.put("Black Belt", new BlackBelt());
		map.put("Black Glasses", new BlackGlasses());
		map.put("Charcoal", new Charcoal());
		map.put("Dragon Fang", new DragonFang());
		map.put("Hard Stone", new HardStone());
		map.put("Magnet", new Magnet());
		map.put("Metal Coat", new MetalCoat());
		map.put("Miracle Seed", new MiracleSeed());
		map.put("Mystic Water", new MysticWater());
		map.put("NeverMeltIce", new NeverMeltIce());
		map.put("Poison Barb", new PoisonBarb());
		map.put("Sharp Beak", new SharpBeak());
		map.put("Silk Scarf", new SilkScarf());
		map.put("Silver Powder", new SilverPowder());
		map.put("Soft Sand", new SoftSand());
		map.put("Spell Tag", new SpellTag());
		map.put("Twisted Spoon", new TwistedSpoon());
		map.put("Dawn Stone", new DawnStone());
		map.put("DeepSeaScale", new DeepSeaScale());
		map.put("DeepSeaTooth", new DeepSeaTooth());
		map.put("Dragon Scale", new DragonScale());
		map.put("Dubious Disc", new DubiousDisc());
		map.put("Dusk Stone", new DuskStone());
		map.put("Electirizer", new Electirizer());
		map.put("Fire Stone", new FireStone());
		map.put("King's Rock", new KingsRock());
		map.put("Leaf Stone", new LeafStone());
		map.put("Magmarizer", new Magmarizer());
		map.put("Moon Stone", new MoonStone());
		map.put("Oval Stone", new OvalStone());
		map.put("Everstone", new Everstone());
		map.put("Prism Scale", new PrismScale());
		map.put("Protector", new Protector());
		map.put("Razor Claw", new RazorClaw());
		map.put("Razor Fang", new RazorFang());
		map.put("Reaper Cloth", new ReaperCloth());
		map.put("Shiny Stone", new ShinyStone());
		map.put("Sun Stone", new SunStone());
		map.put("Thunder Stone", new ThunderStone());
		map.put("Up-Grade", new UpGrade());
		map.put("Water Stone", new WaterStone());
		map.put("Antidote", new Antidote());
		map.put("Awakening", new Awakening());
		map.put("Burn Heal", new BurnHeal());
		map.put("Ice Heal", new IceHeal());
		map.put("Paralyze Heal", new ParalyzeHeal());
		map.put("Full Heal", new FullHeal());
		map.put("Full Restore", new FullRestore());
		map.put("Elixir", new Elixir());
		map.put("Max Elixir", new MaxElixir());
		map.put("Ether", new Ether());
		map.put("Max Ether", new MaxEther());
		map.put("Berry Juice", new BerryJuice());
		map.put("Sweet Heart", new SweetHeart());
		map.put("Potion", new Potion());
		map.put("Energy Powder", new EnergyPowder());
		map.put("Fresh Water", new FreshWater());
		map.put("Super Potion", new SuperPotion());
		map.put("Soda Pop", new SodaPop());
		map.put("Lemonade", new Lemonade());
		map.put("Moomoo Milk", new MoomooMilk());
		map.put("Energy Root", new EnergyRoot());
		map.put("Hyper Potion", new HyperPotion());
		map.put("Max Potion", new MaxPotion());
		map.put("Revive", new Revive());
		map.put("Max Revive", new MaxRevive());
		map.put("Revival Herb", new RevivalHerb());
		map.put("Sacred Ash", new SacredAsh());
		map.put("Dire Hit", new DireHit());
		map.put("Guard Spec.", new GuardSpec());
		map.put("X Accuracy", new XAccuracy());
		map.put("X Attack", new XAttack());
		map.put("X Defend", new XDefend());
		map.put("X Sp. Def", new XSpDef());
		map.put("X Special", new XSpecial());
		map.put("X Speed", new XSpeed());
		map.put("Calcium", new Calcium());
		map.put("Carbos", new Carbos());
		map.put("Clever Wing", new CleverWing());
		map.put("Health Wing", new HealthWing());
		map.put("HP Up", new HPUp());
		map.put("Genius Wing", new GeniusWing());
		map.put("Iron", new Iron());
		map.put("Muscle Wing", new MuscleWing());
		map.put("PP Max", new PPMax());
		map.put("PP Up", new PPUp());
		map.put("Protein", new Protein());
		map.put("Rare Candy", new RareCandy());
		map.put("Resist Wing", new ResistWing());
		map.put("Swift Wing", new SwiftWing());
		map.put("Zinc", new Zinc());
		map.put("Cherish Ball", new CherishBall());
		map.put("Dive Ball", new DiveBall());
		map.put("Dusk Ball", new DuskBall());
		map.put("Fast Ball", new FastBall());
		map.put("Great Ball", new GreatBall());
		map.put("Heal Ball", new HealBall());
		map.put("Heavy Ball", new HeavyBall());
		map.put("Level Ball", new LevelBall());
		map.put("Love Ball", new LoveBall());
		map.put("Lure Ball", new LureBall());
		map.put("Luxury Ball", new LuxuryBall());
		map.put("Master Ball", new MasterBall());
		map.put("Moon Ball", new MoonBall());
		map.put("Nest Ball", new NestBall());
		map.put("Net Ball", new NetBall());
		map.put("Pok\u00e9 Ball", new PokeBall());
		map.put("Premier Ball", new PremierBall());
		map.put("Quick Ball", new QuickBall());
		map.put("Repeat Ball", new RepeatBall());
		map.put("Safari Ball", new SafariBall());
		map.put("Timer Ball", new TimerBall());
		map.put("Ultra Ball", new UltraBall());
		map.put("Cheri Berry", new CheriBerry());
		map.put("Chesto Berry", new ChestoBerry());
		map.put("Pecha Berry", new PechaBerry());
		map.put("Rawst Berry", new RawstBerry());
		map.put("Aspear Berry", new AspearBerry());
		map.put("Leppa Berry", new LeppaBerry());
		map.put("Oran Berry", new OranBerry());
		map.put("Persim Berry", new PersimBerry());
		map.put("Lum Berry", new LumBerry());
		map.put("Sitrus Berry", new SitrusBerry());
		map.put("Razz Berry", new RazzBerry());
		map.put("Pomeg Berry", new PomegBerry());
		map.put("Kelpsy Berry", new KelpsyBerry());
		map.put("Qualot Berry", new QualotBerry());
		map.put("Hondew Berry", new HondewBerry());
		map.put("Grepa Berry", new GrepaBerry());
		map.put("Tamato Berry", new TamatoBerry());
		map.put("Occa Berry", new OccaBerry());
		map.put("Passho Berry", new PasshoBerry());
		map.put("Wacan Berry", new WacanBerry());
		map.put("Rindo Berry", new RindoBerry());
		map.put("Yache Berry", new YacheBerry());
		map.put("Chople Berry", new ChopleBerry());
		map.put("Kebia Berry", new KebiaBerry());
		map.put("Shuca Berry", new ShucaBerry());
		map.put("Coba Berry", new CobaBerry());
		map.put("Payapa Berry", new PayapaBerry());
		map.put("Tanga Berry", new TangaBerry());
		map.put("Charti Berry", new ChartiBerry());
		map.put("Kasib Berry", new KasibBerry());
		map.put("Haban Berry", new HabanBerry());
		map.put("Colbur Berry", new ColburBerry());
		map.put("Babiri Berry", new BabiriBerry());
		map.put("Chilan Berry", new ChilanBerry());
		map.put("Liechi Berry", new LiechiBerry());
		map.put("Ganlon Berry", new GanlonBerry());
		map.put("Salac Berry", new SalacBerry());
		map.put("Petaya Berry", new PetayaBerry());
		map.put("Apicot Berry", new ApicotBerry());
		map.put("Lansat Berry", new LansatBerry());
		map.put("Starf Berry", new StarfBerry());
		map.put("Comet Shard", new CometShard());
		map.put("Tiny Mushroom", new TinyMushroom());
		map.put("Big Mushroom", new BigMushroom());
		map.put("Balm Mushroom", new BalmMushroom());
		map.put("Nugget", new Nugget());
		map.put("Big Nugget", new BigNugget());
		map.put("Pearl", new Pearl());
		map.put("Big Pearl", new BigPearl());
		map.put("Stardust", new Stardust());
		map.put("Star Piece", new StarPiece());
		map.put("Rare Bone", new RareBone());
		map.put("Honey", new Honey());
		map.put("Eviolite", new Eviolite());
		map.put("Heart Scale", new HeartScale());
		map.put("Repel", new Repel());
		map.put("Super Repel", new SuperRepel());
		map.put("Max Repel", new MaxRepel());
	}

	private static class None extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public None() {
			super.name = "None";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 0;
			super.desc = "YOU SHUOLDN'T SEE THIS";
			super.price = -1;
		}

		public int flingDamage() {
			return 9001;
		}
	}

	private static class Syrup extends Item implements TrainerUseItem {
		private static final long serialVersionUID = 1L;

		public Syrup() {
			super.name = "Syrup";
			super.cat = BagCategory.KEYITEM;
			super.bcat = new BattleBagCategory[0];
			super.index = 1;
			super.desc = "A mysterious bottle of syrup. Maybe it will be useful some day.";
			super.price = -1;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public boolean use(Trainer t) {
			return false;
		}
	}

	private static class Bicycle extends Item implements TrainerUseItem {
		private static final long serialVersionUID = 1L;

		public Bicycle() {
			super.name = "Bicycle";
			super.cat = BagCategory.KEYITEM;
			super.bcat = new BattleBagCategory[0];
			super.index = 2;
			super.desc = "A folding Bicycle that enables much faster movement than the Running Shoes.";
			super.price = -1;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public boolean use(Trainer t) {// TODO: if (Can ride bike) Set the bike
										// as a 'currentlyUsing' item
		// May need to make this take in info on the route
			return false;
		}
	}

	private static class Surfboard extends Item implements TrainerUseItem {
		private static final long serialVersionUID = 1L;

		public Surfboard() {
			super.name = "Surfboard";
			super.cat = BagCategory.KEYITEM;
			super.bcat = new BattleBagCategory[0];
			super.index = 3;
			super.desc = "A fancy shmancy surfboard that lets you be RADICAL DUDE!";
			super.price = -1;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public boolean use(Trainer t) {
			return false;
		}
	}

	private static class FishingRod extends Item implements TrainerUseItem {
		private static final long serialVersionUID = 1L;

		public FishingRod() {
			super.name = "Fishing Rod";
			super.cat = BagCategory.KEYITEM;
			super.bcat = new BattleBagCategory[0];
			super.index = 4;
			super.desc = "A multi-purpose, do-it-all kind of fishing rod. The kind you can use wherever you want. Except on land.";
			super.price = -1;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "Oh! A bite!";
		}

		public boolean use(Trainer t) {// TODO: if (spot in front of player is a
										// fishing spot) Set as 'currentlyUsing'
		// May need to make this take in info on the route
			return false;
		}
	}

	private static class AbsorbBulb extends TypeDamageStatIncreaseItem {
		private static final long serialVersionUID = 1L;

		public AbsorbBulb() {
			super.name = "Absorb Bulb";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 5;
			super.desc = "A consumable bulb. If the holder is hit by a Water-type move, its Sp. Atk will rise.";
			super.price = 200;
		}

		public Type damageType() {
			return Type.WATER;
		}

		public Stat toIncrease() {
			return Stat.SP_ATTACK;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class AirBalloon extends Item implements HoldItem,
			ConsumedItem, LevitationEffect, TakeDamageEffect, EntryEffect {
		private static final long serialVersionUID = 1L;

		public AirBalloon() {
			super.name = "Air Balloon";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 6;
			super.desc = "When held by a Pok\u00e9mon, the Pok\u00e9mon will float into the air. When the holder is attacked, this item will burst.";
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}

		public void enter(Battle b, ActivePokemon victim) {
			b.addMessage(victim.getName() + " floats with its " + this.name
					+ "!");
		}

		public void takeDamage(Battle b, ActivePokemon user,
				ActivePokemon victim) {
			b.addMessage(victim.getName() + "'s " + this.name + " popped!");
			victim.consumeItem(b);
		}
	}

	private static class AmuletCoin extends Item implements HoldItem,
			EntryEffect {
		private static final long serialVersionUID = 1L;

		public AmuletCoin() {
			super.name = "Amulet Coin";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 7;
			super.desc = "An item to be held by a Pok\u00e9mon. It doubles a battle's prize money if the holding Pok\u00e9mon joins in.";
			super.price = 100;
		}

		public int flingDamage() {
			return 30;
		}

		public void enter(Battle b, ActivePokemon victim) {
			TeamEffect.getEffect("DoubleMoney").cast(b, victim, victim,
					CastSource.HELD_ITEM, false);
		}
	}

	private static class BigRoot extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public BigRoot() {
			super.name = "Big Root";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 8;
			super.desc = "A Pok\u00e9mon held item that boosts the power of HP-stealing moves to let the holder recover more HP.";
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}
	}

	private static class BindingBand extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public BindingBand() {
			super.name = "Binding Band";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 9;
			super.desc = "This item, when attached to a Pok\u00e9mon, increases damage caused by moves that constrict the opponent.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class BlackSludge extends Item implements HoldItem,
			EndTurnEffect {
		private static final long serialVersionUID = 1L;

		public BlackSludge() {
			super.name = "Black Sludge";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 10;
			super.desc = "A held item that gradually restores the HP of Poison-type Pok\u00e9mon. It inflicts damage on all other types.";
			super.price = 200;
		}

		public void apply(ActivePokemon victim, Battle b) {
			if (victim.isType(b, Type.POISON)) {
				if (victim.fullHealth())
					return;
				victim.healHealthFraction(1 / 16.0);
				b.addMessage(victim.getName() + "'s HP was restored by its "
						+ this.name + "!", victim.getHP(), victim.user());
			} else if (!victim.hasAbility("Magic Guard")) {
				b.addMessage(victim.getName()
						+ " lost some of its HP due to its " + this.name + "!");
				victim.reduceHealthFraction(b, 1 / 8.0);
			}
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class BrightPowder extends Item implements HoldItem,
			StatChangingEffect {
		private static final long serialVersionUID = 1L;

		public BrightPowder() {
			super.name = "Bright Powder";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 11;
			super.desc = "An item to be held by a Pok\u00e9mon. It casts a tricky glare that lowers the opponent's accuracy.";
			super.price = 100;
		}

		public int flingDamage() {
			return 30;
		}

		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s,
				Battle b) {
			if (s == Stat.EVASION)
				stat *= 1.1;
			return stat;
		}
	}

	private static class CellBattery extends TypeDamageStatIncreaseItem {
		private static final long serialVersionUID = 1L;

		public CellBattery() {
			super.name = "Cell Battery";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 12;
			super.desc = "A consumable battery. If the holder is hit by an Electric-type move, its Attack will rise.";
			super.price = 200;
		}

		public Type damageType() {
			return Type.ELECTRIC;
		}

		public Stat toIncrease() {
			return Stat.ATTACK;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class ChoiceBand extends ChoiceItem {
		private static final long serialVersionUID = 1L;

		public ChoiceBand() {
			super.name = "Choice Band";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 13;
			super.desc = "An item to be held by a Pok\u00e9mon. This headband ups Attack, but allows the use of only one of its moves.";
			super.price = 100;
		}

		public Stat toIncrease() {
			return Stat.ATTACK;
		}
	}

	private static class ChoiceScarf extends ChoiceItem {
		private static final long serialVersionUID = 1L;

		public ChoiceScarf() {
			super.name = "Choice Scarf";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 14;
			super.desc = "An item to be held by a Pok\u00e9mon. This scarf boosts Speed, but allows the use of only one of its moves.";
			super.price = 200;
		}

		public Stat toIncrease() {
			return Stat.SPEED;
		}
	}

	private static class ChoiceSpecs extends ChoiceItem {
		private static final long serialVersionUID = 1L;

		public ChoiceSpecs() {
			super.name = "Choice Specs";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 15;
			super.desc = "An item to be held by a Pok\u00e9mon. These distinctive glasses boost Sp. Atk but allow the use of only one of its moves.";
			super.price = 200;
		}

		public Stat toIncrease() {
			return Stat.SP_ATTACK;
		}
	}

	private static class CleanseTag extends Item implements HoldItem,
			RepellingEffect {
		private static final long serialVersionUID = 1L;

		public CleanseTag() {
			super.name = "Cleanse Tag";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 16;
			super.desc = "An item to be held by a Pok\u00e9mon. It helps keep wild Pok\u00e9mon away if the holder is the first one in the party.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public double chance() {
			return .33;
		}
	}

	private static class DampRock extends Item implements HoldItem,
			WeatherExtendingEffect {
		private static final long serialVersionUID = 1L;

		public DampRock() {
			super.name = "Damp Rock";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 17;
			super.desc = "A Pok\u00e9mon held item that extends the duration of the move Rain Dance used by the holder.";
			super.price = 200;
		}

		public int flingDamage() {
			return 60;
		}

		public WeatherType getWeatherType() {
			return WeatherType.RAINING;
		}
	}

	private static class HeatRock extends Item implements HoldItem,
			WeatherExtendingEffect {
		private static final long serialVersionUID = 1L;

		public HeatRock() {
			super.name = "Heat Rock";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 18;
			super.desc = "A Pok\u00e9mon held item that extends the duration of the move Sunny Day used by the holder.";
			super.price = 200;
		}

		public int flingDamage() {
			return 60;
		}

		public WeatherType getWeatherType() {
			return WeatherType.SUNNY;
		}
	}

	private static class IcyRock extends Item implements HoldItem,
			WeatherExtendingEffect {
		private static final long serialVersionUID = 1L;

		public IcyRock() {
			super.name = "Icy Rock";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 19;
			super.desc = "A Pok\u00e9mon held item that extends the duration of the move Hail used by the holder.";
			super.price = 200;
		}

		public int flingDamage() {
			return 40;
		}

		public WeatherType getWeatherType() {
			return WeatherType.HAILING;
		}
	}

	private static class SmoothRock extends Item implements HoldItem,
			WeatherExtendingEffect {
		private static final long serialVersionUID = 1L;

		public SmoothRock() {
			super.name = "Smooth Rock";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 20;
			super.desc = "A Pok\u00e9mon held item that extends the duration of the move Sandstorm used by the holder.";
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}

		public WeatherType getWeatherType() {
			return WeatherType.SANDSTORM;
		}
	}

	private static class EjectButton extends Item implements HoldItem,
			TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		public EjectButton() {
			super.name = "Eject Button";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 21;
			super.desc = "If the holder is hit by an attack, it will switch with another Pok\u00e9mon in your party.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void takeDamage(Battle b, ActivePokemon user,
				ActivePokemon victim) {
			Team t = b.getTrainer(victim.user());
			if (t instanceof WildPokemon)
				return;

			Trainer trainer = (Trainer) t;
			if (!trainer.hasRemainingPokemon())
				return;

			b.addMessage(victim.getName() + "'s " + this.name
					+ " sent it back to " + trainer.getName() + "!");
			victim.consumeItem(b);
			trainer.switchToRandom();
			trainer.setAction(Action.SWITCH);
			victim = trainer.front();
			b.enterBattle(victim, victim.getName() + " was sent out!");
		}
	}

	private static class DestinyKnot extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public DestinyKnot() {
			super.name = "Destiny Knot";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 22;
			super.desc = "A long, thin, bright-red string to be held by a Pok\u00e9mon. If the holder becomes infatuated, the foe does too.";
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}
	}

	private static class ExpertBelt extends Item implements HoldItem,
			PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		public ExpertBelt() {
			super.name = "Expert Belt";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 23;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a well-worn belt that slightly boosts the power of supereffective moves.";
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}

		public double getMultiplier(Battle b, ActivePokemon user,
				ActivePokemon victim) {
			return Type.getAdvantage(user.getAttack().getType(b, user), victim,
					b) > 1 ? 1.2 : 1;
		}
	}

	private static class FlameOrb extends Item implements HoldItem,
			EndTurnEffect {
		private static final long serialVersionUID = 1L;

		public FlameOrb() {
			super.name = "Flame Orb";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 24;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a bizarre orb that inflicts a burn on the holder in battle.";
			super.price = 200;
		}

		public void apply(ActivePokemon victim, Battle b) {
			Status.giveStatus(b, victim, victim, StatusCondition.BURNED,
					victim.getName() + " was burned by its " + this.name + "!");
		}

		public int flingDamage() {
			return 10;
		}
	}

	private static class ToxicOrb extends Item implements HoldItem,
			EndTurnEffect {
		private static final long serialVersionUID = 1L;

		public ToxicOrb() {
			super.name = "Toxic Orb";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 25;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a bizarre orb that inflicts a burn on the holder in battle.";
			super.price = 200;
		}

		public void apply(ActivePokemon victim, Battle b) {
			if (Status.applies(StatusCondition.POISONED, b, victim, victim)) {
				victim.addEffect(PokemonEffect.getEffect("BadPoison")
						.newInstance());
				Status.giveStatus(b, victim, victim, StatusCondition.POISONED,
						victim.getName() + " was badly poisoned by its "
								+ this.name + "!");
			}
		}

		public int flingDamage() {
			return 10;
		}
	}

	private static class FloatStone extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public FloatStone() {
			super.name = "Float Stone";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 26;
			super.desc = "This item, when attached to a Pok\u00e9mon, halves the Pok\u00e9mon's weight for use with attacks that deal with weight";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class FocusBand extends Item implements HoldItem,
			BracingEffect {
		private static final long serialVersionUID = 1L;

		public FocusBand() {
			super.name = "Focus Band";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 27;
			super.desc = "An item to be held by a Pok\u00e9mon. The holder may endure a potential KO attack, leaving it with just 1 HP.";
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}

		public boolean isBracing(Battle b, ActivePokemon bracer,
				Boolean fullHealth) {
			return Math.random() * 100 < 10;
		}

		public String braceMessage(ActivePokemon bracer) {
			return bracer.getName() + " held on with its " + this.name + "!";
		}
	}

	private static class FocusSash extends Item implements HoldItem,
			ConsumedItem, BracingEffect {
		private static final long serialVersionUID = 1L;

		public FocusSash() {
			super.name = "Focus Sash";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 28;
			super.desc = "An item to be held by a Pok\u00e9mon. If it has full HP, the holder will endure one potential KO attack, leaving 1 HP.";
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}

		public boolean isBracing(Battle b, ActivePokemon bracer,
				Boolean fullHealth) {
			if (fullHealth) {
				bracer.consumeItem(b);
				return true;
			}
			return false;
		}

		public String braceMessage(ActivePokemon bracer) {
			return bracer.getName() + " held on with its " + this.name + "!";
		}
	}

	private static class GripClaw extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public GripClaw() {
			super.name = "Grip Claw";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 29;
			super.desc = "A Pok\u00e9mon held item that extends the duration of multiturn attacks like Bind and Wrap.";
			super.price = 200;
		}

		public int flingDamage() {
			return 90;
		}
	}

	private static class GriseousOrb extends Item implements HoldItem,
			PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		public GriseousOrb() {
			super.name = "Griseous Orb";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 30;
			super.desc = "A glowing orb to be held by Giratina. It boosts the power of Dragon- and Ghost-type moves.";
			super.price = 200;
		}

		public int flingDamage() {
			return 60;
		}

		public double getMultiplier(Battle b, ActivePokemon user,
				ActivePokemon victim) {
			if (user.isPokemon("Giratina")) {
				Type t = user.getAttack().getType(b, user);
				if (t == Type.DRAGON || t == Type.GHOST)
					return 1.2;
			}
			return 1;
		}
	}

	private static class IronBall extends Item implements HoldItem,
			GroundedEffect, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		public IronBall() {
			super.name = "Iron Ball";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 31;
			super.desc = "A Pok\u00e9mon held item that cuts Speed. It makes Flying-type and levitating holders susceptible to Ground moves.";
			super.price = 200;
		}

		public int flingDamage() {
			return 130;
		}

		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s,
				Battle b) {
			if (s == Stat.SPEED)
				stat *= .5;
			return stat;
		}
	}

	private static class LaggingTail extends Item implements HoldItem,
			StallingEffect {
		private static final long serialVersionUID = 1L;

		public LaggingTail() {
			super.name = "Lagging Tail";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 32;
			super.desc = "An item to be held by a Pok\u00e9mon. It is tremendously heavy and makes the holder move slower than usual.";
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}
	}

	private static class LifeOrb extends Item implements HoldItem,
			PowerChangeEffect, ApplyDamageEffect {
		private static final long serialVersionUID = 1L;

		public LifeOrb() {
			super.name = "Life Orb";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 33;
			super.desc = "An item to be held by a Pok\u00e9mon. It boosts the power of moves, but at the cost of some HP on each hit.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public double getMultiplier(Battle b, ActivePokemon user,
				ActivePokemon victim) {
			return 5324.0 / 4096.0;
		}

		public void applyEffect(Battle b, ActivePokemon user,
				ActivePokemon victim, Integer damage) {
			if (user.hasAbility("Magic Guard"))
				return;
			b.addMessage(user.getName() + " was hurt by its " + this.name + "!");
			user.reduceHealthFraction(b, .1);
		}
	}

	private static class LightBall extends Item implements HoldItem,
			StatChangingEffect {
		private static final long serialVersionUID = 1L;

		public LightBall() {
			super.name = "Light Ball";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 34;
			super.desc = "An item to be held by Pikachu. It is a puzzling orb that raises the Attack and Sp. Atk stat.";
			super.price = 100;
		}

		public int flingDamage() {
			return 30;
		}

		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s,
				Battle b) {
			if ((s == Stat.ATTACK || s == Stat.SP_ATTACK)
					&& p.isPokemon("Pikachu"))
				stat *= 2;
			return stat;
		}
	}

	private static class LightClay extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public LightClay() {
			super.name = "Light Clay";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 35;
			super.desc = "A Pok\u00e9mon held item that extends the duration of barrier moves like Light Screen and Reflect used by the holder.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class LuckyEgg extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public LuckyEgg() {
			super.name = "Lucky Egg";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 36;
			super.desc = "An item to be held by a Pok\u00e9mon. It is an egg filled with happiness that earns extra Exp. Points in battle.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class LuckyPunch extends Item implements HoldItem,
			CritStageEffect {
		private static final long serialVersionUID = 1L;

		public LuckyPunch() {
			super.name = "Lucky Punch";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 37;
			super.desc = "An item to be held by Chansey. It is a pair of gloves that boosts Chansey's critical-hit ratio.";
			super.price = 10;
		}

		public int flingDamage() {
			return 40;
		}

		public int increaseCritStage(ActivePokemon p) {
			if (p.isPokemon("Chansey"))
				return 2;
			return 0;
		}
	}

	private static class MachoBrace extends PowerItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public int[] getEVs(int[] vals) {
			for (int i = 0; i < vals.length; i++)
				vals[i] *= 2;
			return vals;
		}

		public Stat toIncrease() {
			Global.error("toIncrease() method in Macho Brace should never be called.");
			return null;
		}

		public MachoBrace() {
			super.name = "Macho Brace";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 38;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a stiff and heavy brace that promotes strong growth but lowers Speed.";
			super.price = 3000;
		}

		public int flingDamage() {
			return 60;
		}
	}

	private static class MentalHerb extends Item implements HoldItem,
			EndTurnEffect {
		private static final long serialVersionUID = 1L;
		String[] effects = { "Infatuated", "Disable", "Taunt", "Encore",
				"Torment", "Confusion" };
		String[] messages = { "infatuated", "disabled",
				"under the effects of taunt", "under the effects of encore",
				"under the effects of torment", "confused" };

		public MentalHerb() {
			super.name = "Mental Herb";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 39;
			super.desc = "An item to be held by a Pok\u00e9mon. It snaps the holder out of infatuation. It can be used only once.";
			super.price = 100;
		}

		public void apply(ActivePokemon victim, Battle b) {
			boolean used = false;
			for (int i = 0; i < effects.length; i++) {
				String s = effects[i];
				if (victim.hasEffect(s)) {
					used = true;
					victim.getAttributes().removeEffect(s);
					b.addMessage(victim.getName() + " is no longer "
							+ messages[i] + " due to its " + this.name + "!");
				}
			}
			if (used)
				victim.consumeItem(b);
		}

		public int flingDamage() {
			return 10;
		}
	}

	private static class MetalPowder extends Item implements HoldItem,
			StatChangingEffect {
		private static final long serialVersionUID = 1L;

		public MetalPowder() {
			super.name = "Metal Powder";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 40;
			super.desc = "When this item is held by a Ditto, the holder's initial Defence & Special Defence stats are increased by 50%";
			super.price = 10;
		}

		public int flingDamage() {
			return 10;
		}

		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s,
				Battle b) {
			if ((s == Stat.DEFENSE || s == Stat.SP_DEFENSE)
					&& p.isPokemon("Ditto"))
				stat *= 1.5;
			return stat;
		}
	}

	private static class Metronome extends Item implements HoldItem,
			PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		public Metronome() {
			super.name = "Metronome";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 41;
			super.desc = "A Pok\u00e9mon held item that boosts a move used consecutively. Its effect is reset if another move is used.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public double getMultiplier(Battle b, ActivePokemon user,
				ActivePokemon victim) {
			return Math.min(2, 1 + .2 * (user.getAttributes().getCount() - 1));
		}
	}

	private static class MuscleBand extends Item implements HoldItem,
			PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		public MuscleBand() {
			super.name = "Muscle Band";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 42;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a headband that slightly boosts the power of physical moves.";
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}

		public double getMultiplier(Battle b, ActivePokemon user,
				ActivePokemon victim) {
			return user.getAttack().getCategory() == Category.PHYSICAL ? 1.1
					: 1;
		}
	}

	private static class PowerAnklet extends PowerItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public PowerAnklet() {
			super.name = "Power Anklet";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 43;
			super.desc = "A Pok\u00e9mon held item that promotes Speed gain on leveling, but reduces the Speed stat.";
			super.price = 3000;
		}

		public Stat toIncrease() {
			return Stat.SPEED;
		}

		public int flingDamage() {
			return 70;
		}
	}

	private static class PowerBand extends PowerItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public PowerBand() {
			super.name = "Power Band";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 44;
			super.desc = "A Pok\u00e9mon held item that promotes Sp. Def gain on leveling, but reduces the Speed stat.";
			super.price = 3000;
		}

		public Stat toIncrease() {
			return Stat.SP_DEFENSE;
		}

		public int flingDamage() {
			return 70;
		}
	}

	private static class PowerBelt extends PowerItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public PowerBelt() {
			super.name = "Power Belt";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 45;
			super.desc = "A Pok\u00e9mon held item that promotes Def gain on leveling, but reduces the Speed stat.";
			super.price = 3000;
		}

		public Stat toIncrease() {
			return Stat.DEFENSE;
		}

		public int flingDamage() {
			return 70;
		}
	}

	private static class PowerBracer extends PowerItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public PowerBracer() {
			super.name = "Power Bracer";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 46;
			super.desc = "A Pok\u00e9mon held item that promotes Att gain on leveling, but reduces the Speed stat.";
			super.price = 3000;
		}

		public Stat toIncrease() {
			return Stat.ATTACK;
		}

		public int flingDamage() {
			return 70;
		}
	}

	private static class PowerLens extends PowerItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public PowerLens() {
			super.name = "Power Lens";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 47;
			super.desc = "A Pok\u00e9mon held item that promotes Sp. Att gain on leveling, but reduces the Speed stat.";
			super.price = 3000;
		}

		public Stat toIncrease() {
			return Stat.SP_ATTACK;
		}

		public int flingDamage() {
			return 70;
		}
	}

	private static class PowerWeight extends PowerItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public PowerWeight() {
			super.name = "Power Weight";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 48;
			super.desc = "A Pok\u00e9mon held item that promotes HP gain on leveling, but reduces the Speed stat.";
			super.price = 3000;
		}

		public Stat toIncrease() {
			return Stat.HP;
		}

		public int flingDamage() {
			return 70;
		}
	}

	private static class QuickClaw extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public QuickClaw() {
			super.name = "Quick Claw";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 49;
			super.desc = "An item to be held by a Pok\u00e9mon. A light, sharp claw that lets the bearer move first occasionally.";
			super.price = 100;
		}

		public int flingDamage() {
			return 80;
		}
	}

	private static class QuickPowder extends Item implements HoldItem,
			StatChangingEffect {
		private static final long serialVersionUID = 1L;

		public QuickPowder() {
			super.name = "Quick Powder";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 50;
			super.desc = "An item to be held by Ditto. Extremely fine yet hard, this odd powder boosts the Speed stat.";
			super.price = 10;
		}

		public int flingDamage() {
			return 10;
		}

		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s,
				Battle b) {
			if (s == Stat.SPEED && p.isPokemon("Ditto"))
				stat *= 1.5;
			return stat;
		}
	}

	private static class RedCard extends Item implements HoldItem,
			TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		public RedCard() {
			super.name = "Red Card";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 51;
			super.desc = "A card with a mysterious power. When the holder is struck by a foe, the attacker is removed from battle.";
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}

		public void takeDamage(Battle b, ActivePokemon user,
				ActivePokemon victim) {
			Team t = b.getTrainer(user.user());
			if (t instanceof WildPokemon)
				return;

			Trainer trainer = (Trainer) t;
			if (!trainer.hasRemainingPokemon())
				return;

			b.addMessage(victim.getName() + "'s " + this.name + " sent "
					+ user.getName() + " back to " + trainer.getName() + "!");
			victim.consumeItem(b);
			trainer.switchToRandom();
			trainer.setAction(Action.SWITCH);
			user = trainer.front();
			b.enterBattle(user, user.getName() + " was sent out!");
		}
	}

	private static class RingTarget extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public RingTarget() {
			super.name = "Ring Target";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 52;
			super.desc = "Moves that would otherwise have no effect will land on the Pok\u00e9mon that holds it.";
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}
	}

	private static class RockyHelmet extends Item implements HoldItem,
			PhysicalContactEffect {
		private static final long serialVersionUID = 1L;

		public RockyHelmet() {
			super.name = "Rocky Helmet";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 53;
			super.desc = "If the holder of this item takes damage, the attacker will also be damaged upon contact.";
			super.price = 200;
		}

		public int flingDamage() {
			return 60;
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			b.addMessage(user.getName() + " was hurt by " + victim.getName()
					+ "'s " + this.name + "!");
			user.reduceHealthFraction(b, 1 / 8.0);
		}
	}

	private static class SafetyGoggles extends Item implements HoldItem,
			EffectBlockerEffect, WeatherBlockerEffect {
		private static final long serialVersionUID = 1L;

		public SafetyGoggles() {
			super.name = "Safety Goggles";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 54;
			super.desc = "An item to be held by a Pok\u00e9mon. These goggles protect the holder from both weather-related damage and powder.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public boolean validMove(Battle b, ActivePokemon user,
				ActivePokemon victim) {
			if (!user.getAttack().isMoveType("Powder"))
				return true;
			if (user.getAttack().getCategory() == Category.STATUS)
				b.addMessage(victim.getName() + "'s " + this.name
						+ " protects it from powder moves!");
			return false;
		}

		public boolean block(WeatherType weather) {
			return true;
		}
	}

	private static class ScopeLens extends Item implements HoldItem,
			CritStageEffect {
		private static final long serialVersionUID = 1L;

		public ScopeLens() {
			super.name = "Scope Lens";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 55;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a lens that boosts the holder's critical-hit ratio.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public int increaseCritStage(ActivePokemon p) {
			return 1;
		}
	}

	private static class ShedShell extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public ShedShell() {
			super.name = "Shed Shell";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 56;
			super.desc = "A tough, discarded carapace to be held by a Pok\u00e9mon. It enables the holder to switch with a waiting Pok\u00e9mon in battle.";
			super.price = 100;
		}

		public int flingDamage() {
			return 10;
		}
	}

	private static class ShellBell extends Item implements HoldItem,
			ApplyDamageEffect {
		private static final long serialVersionUID = 1L;

		public ShellBell() {
			super.name = "Shell Bell";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 57;
			super.desc = "An item to be held by a Pok\u00e9mon. The holder's HP is restored a little every time it inflicts damage.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void applyEffect(Battle b, ActivePokemon user,
				ActivePokemon victim, Integer damage) {
			if (user.fullHealth())
				return;
			user.heal((int) Math.ceil(damage / 8.0));
			// TODO: This looks really bad when paired with Explosion
			b.addMessage(user.getName() + " restored some HP due to its "
					+ this.name + "!", user.getHP(), user.user());
		}
	}

	private static class SmokeBall extends Item implements HoldItem,
			DefiniteEscape {
		private static final long serialVersionUID = 1L;

		public SmokeBall() {
			super.name = "Smoke Ball";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 58;
			super.desc = "An item to be held by a Pok\u00e9mon. It enables the holder to flee from any wild Pok\u00e9mon without fail.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class Snowball extends TypeDamageStatIncreaseItem {
		private static final long serialVersionUID = 1L;

		public Snowball() {
			super.name = "Snowball";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 59;
			super.desc = "An item to be held by a Pok\u00e9mon. It boosts Attack if hit with an Ice-type attack. It can only be used once.";
			super.price = 200;
		}

		public Type damageType() {
			return Type.ICE;
		}

		public Stat toIncrease() {
			return Stat.ATTACK;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class SoulDew extends Item implements HoldItem,
			StatChangingEffect {
		private static final long serialVersionUID = 1L;

		public SoulDew() {
			super.name = "Soul Dew";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 60;
			super.desc = "If the Soul Dew is attached to Latios or Latias, the holder's Special Attack and Special Defence is increased by 50%.";
			super.price = 10;
		}

		public int flingDamage() {
			return 10;
		}

		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s,
				Battle b) {
			if ((s == Stat.SP_ATTACK || s == Stat.SP_DEFENSE)
					&& (p.isPokemon("Latios") || p.isPokemon("Latias")))
				stat *= 1.5;
			return stat;
		}
	}

	private static class Stick extends Item implements HoldItem,
			CritStageEffect {
		private static final long serialVersionUID = 1L;

		public Stick() {
			super.name = "Stick";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 61;
			super.desc = "An item to be held by Farfetch'd. It is a very long and stiff stalk of leek that boosts the critical-hit ratio.";
			super.price = 200;
		}

		public int flingDamage() {
			return 60;
		}

		public int increaseCritStage(ActivePokemon p) {
			if (p.isPokemon("Farfetch'd"))
				return 2;
			return 0;
		}
	}

	private static class StickyBarb extends Item implements HoldItem,
			EndTurnEffect, PhysicalContactEffect, ItemCondition {
		private static final long serialVersionUID = 1L;
		private Item item;

		public StickyBarb() {
			super.name = "Sticky Barb";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 62;
			super.desc = "A held item that damages the holder on every turn. It may latch on to foes and allies that touch the holder.";
			super.price = 200;
		}

		public void apply(ActivePokemon victim, Battle b) {
			if (victim.hasAbility("Magic Guard"))
				return;
			b.addMessage(victim.getName() + " was hurt by its " + this.name
					+ "!");
			victim.reduceHealthFraction(b, 1 / 8.0);
		}

		public int flingDamage() {
			return 80;
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (!user.hasAbility("Magic Guard")) {
				b.addMessage(user.getName() + " was hurt by "
						+ victim.getName() + "'s " + this.name + "!");
				user.reduceHealthFraction(b, 1 / 8.0);
			}

			if (user.isHoldingItem(b) || user.isFainted(b))
				return;
			b.addMessage(victim.getName() + "s " + this.name + " latched onto "
					+ user.getName() + "!");

			if (b.isWildBattle()) {
				victim.removeItem();
				user.giveItem(this);
				return;
			}

			item = this;
			PokemonEffect.getEffect("ChangeItem").cast(b, victim, user,
					CastSource.HELD_ITEM, false);
			item = Item.noneItem();
			PokemonEffect.getEffect("ChangeItem").cast(b, victim, victim,
					CastSource.HELD_ITEM, false);
		}

		public Item getItem() {
			return item;
		}
	}

	private static class ThickClub extends Item implements HoldItem,
			StatChangingEffect {
		private static final long serialVersionUID = 1L;

		public ThickClub() {
			super.name = "Thick Club";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 63;
			super.desc = "An item to be held by Cubone or Marowak. It is a hard bone of some sort that boosts the Attack stat.";
			super.price = 500;
		}

		public int flingDamage() {
			return 90;
		}

		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s,
				Battle b) {
			if (s == Stat.ATTACK
					&& (p.isPokemon("Cubone") || p.isPokemon("Marowak")))
				stat *= 2;
			return stat;
		}
	}

	private static class WeaknessPolicy extends Item implements HoldItem,
			TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		public WeaknessPolicy() {
			super.name = "Weakness Policy";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 64;
			super.desc = "An item to be held by a Pok\u00e9mon. Attack and Sp. Atk sharply increase if the holder is hit with a move it's weak to.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void takeDamage(Battle b, ActivePokemon user,
				ActivePokemon victim) {
			if (Type.getAdvantage(user.getAttack().getType(b, user), victim, b) > 1) {
				victim.getAttributes().modifyStage(victim, victim, 2,
						Stat.ATTACK, b, CastSource.HELD_ITEM);
				victim.getAttributes().modifyStage(victim, victim, 2,
						Stat.SP_ATTACK, b, CastSource.HELD_ITEM);
			}
		}
	}

	private static class WhiteHerb extends Item implements HoldItem,
			StatProtectingEffect {
		private static final long serialVersionUID = 1L;

		public WhiteHerb() {
			super.name = "White Herb";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 65;
			super.desc = "An item to be held by a Pok\u00e9mon. It restores any lowered stat in battle. It can be used only once.";
			super.price = 100;
		}

		public int flingDamage() {
			return 10;
		}

		public boolean prevent(ActivePokemon caster, Stat stat) {
			return true;
		}

		public String preventionMessage(ActivePokemon p) {
			return p.getName() + "'s " + this.name
					+ " prevented its stats from being lowered!";
		}
	}

	private static class WideLens extends Item implements HoldItem,
			StatChangingEffect {
		private static final long serialVersionUID = 1L;

		public WideLens() {
			super.name = "Wide Lens";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 66;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a magnifying lens that slightly boosts the accuracy of moves.";
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}

		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s,
				Battle b) {
			if (s == Stat.ACCURACY)
				stat *= 1.1;
			return stat;
		}
	}

	private static class WiseGlasses extends Item implements HoldItem,
			StatChangingEffect {
		private static final long serialVersionUID = 1L;

		public WiseGlasses() {
			super.name = "Wise Glasses";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 67;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a thick pair of glasses that slightly boosts the power of special moves.";
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}

		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s,
				Battle b) {
			if (s == Stat.SP_ATTACK)
				stat *= 1.1;
			return stat;
		}
	}

	private static class ZoomLens extends Item implements HoldItem,
			StatChangingEffect {
		private static final long serialVersionUID = 1L;

		public ZoomLens() {
			super.name = "Zoom Lens";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 68;
			super.desc = "An item to be held by a Pok\u00e9mon. If the holder moves after its target, its accuracy will be boosted.";
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}

		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s,
				Battle b) {
			if (s == Stat.ACCURACY && !b.isFirstAttack())
				stat *= 1.2;
			return stat;
		}
	}

	private static class FullIncense extends Item implements HoldItem,
			StallingEffect {
		private static final long serialVersionUID = 1L;

		public FullIncense() {
			super.name = "Full Incense";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 69;
			super.desc = "An item to be held by a Pok\u00e9mon. It is an exotic-smelling incense that makes the holder bloated and slow moving.";
			super.price = 9600;
		}

		public int flingDamage() {
			return 10;
		}
	}

	private static class LaxIncense extends Item implements HoldItem,
			StatChangingEffect {
		private static final long serialVersionUID = 1L;

		public LaxIncense() {
			super.name = "Lax Incense";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 70;
			super.desc = "An item to be held by a Pok\u00e9mon. The tricky aroma of this incense may make attacks miss the holder.";
			super.price = 9600;
		}

		public int flingDamage() {
			return 10;
		}

		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s,
				Battle b) {
			if (s == Stat.EVASION)
				stat *= 1.1;
			return stat;
		}
	}

	private static class LuckIncense extends Item implements HoldItem,
			EntryEffect {
		private static final long serialVersionUID = 1L;

		public LuckIncense() {
			super.name = "Luck Incense";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 71;
			super.desc = "An item to be held by a Pok\u00e9mon. It doubles a battle's prize money if the holding Pok\u00e9mon joins in.";
			super.price = 9600;
		}

		public int flingDamage() {
			return 10;
		}

		public void enter(Battle b, ActivePokemon victim) {
			TeamEffect.getEffect("DoubleMoney").cast(b, victim, victim,
					CastSource.HELD_ITEM, false);
		}
	}

	private static class OddIncense extends TypeEnhancingItem implements
			HoldItem {
		private static final long serialVersionUID = 1L;

		public OddIncense() {
			super.name = "Odd Incense";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 72;
			super.desc = "An item to be held by a Pok\u00e9mon. It is an exotic-smelling incense that boosts the power of Psychic-type moves.";
			super.price = 9600;
		}

		public Type getType() {
			return Type.PSYCHIC;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 10;
		}
	}

	private static class PureIncense extends Item implements HoldItem,
			RepellingEffect {
		private static final long serialVersionUID = 1L;

		public PureIncense() {
			super.name = "Pure Incense";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 73;
			super.desc = "An item to be held by a Pok\u00e9mon. It helps keep wild Pok\u00e9mon away if the holder is the first one in the party.";
			super.price = 9600;
		}

		public int flingDamage() {
			return 10;
		}

		public double chance() {
			return .33;
		}
	}

	private static class RockIncense extends TypeEnhancingItem implements
			HoldItem {
		private static final long serialVersionUID = 1L;

		public RockIncense() {
			super.name = "Rock Incense";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 74;
			super.desc = "An item to be held by a Pok\u00e9mon. It is an exotic-smelling incense that boosts the power of Rock-type moves.";
			super.price = 9600;
		}

		public Type getType() {
			return Type.ROCK;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 10;
		}
	}

	private static class RoseIncense extends TypeEnhancingItem implements
			HoldItem {
		private static final long serialVersionUID = 1L;

		public RoseIncense() {
			super.name = "Rose Incense";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 75;
			super.desc = "An item to be held by a Pok\u00e9mon. It is an exotic-smelling incense that boosts the power of Grass-type moves.";
			super.price = 9600;
		}

		public Type getType() {
			return Type.GRASS;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 10;
		}
	}

	private static class SeaIncense extends TypeEnhancingItem implements
			HoldItem {
		private static final long serialVersionUID = 1L;

		public SeaIncense() {
			super.name = "Sea Incense";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 76;
			super.desc = "An item to be held by a Pok\u00e9mon. It is incense with a curious aroma that boosts the power of Water-type moves.";
			super.price = 9600;
		}

		public Type getType() {
			return Type.WATER;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 10;
		}
	}

	private static class WaveIncense extends TypeEnhancingItem implements
			HoldItem {
		private static final long serialVersionUID = 1L;

		public WaveIncense() {
			super.name = "Wave Incense";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 77;
			super.desc = "An item to be held by a Pok\u00e9mon. It is incense with a curious aroma that boosts the power of Water-type moves.";
			super.price = 9600;
		}

		public Type getType() {
			return Type.WATER;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 10;
		}
	}

	private static class DracoPlate extends TypeEnhancingItem implements
			HoldItem, PlateItem {
		private static final long serialVersionUID = 1L;

		public DracoPlate() {
			super.name = "Draco Plate";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 78;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Dragon-type moves.";
			super.price = 1000;
		}

		public Type getType() {
			return Type.DRAGON;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 90;
		}
	}

	private static class DreadPlate extends TypeEnhancingItem implements
			HoldItem, PlateItem {
		private static final long serialVersionUID = 1L;

		public DreadPlate() {
			super.name = "Dread Plate";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 79;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Dark-type moves.";
			super.price = 1000;
		}

		public Type getType() {
			return Type.DARK;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 90;
		}
	}

	private static class EarthPlate extends TypeEnhancingItem implements
			HoldItem, PlateItem {
		private static final long serialVersionUID = 1L;

		public EarthPlate() {
			super.name = "Earth Plate";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 80;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Ground-type moves.";
			super.price = 1000;
		}

		public Type getType() {
			return Type.GROUND;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 90;
		}
	}

	private static class FistPlate extends TypeEnhancingItem implements
			HoldItem, PlateItem {
		private static final long serialVersionUID = 1L;

		public FistPlate() {
			super.name = "Fist Plate";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 81;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Fighting-type moves.";
			super.price = 1000;
		}

		public Type getType() {
			return Type.FIGHTING;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 90;
		}
	}

	private static class FlamePlate extends TypeEnhancingItem implements
			HoldItem, PlateItem {
		private static final long serialVersionUID = 1L;

		public FlamePlate() {
			super.name = "Flame Plate";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 82;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Fire-type moves.";
			super.price = 1000;
		}

		public Type getType() {
			return Type.FIRE;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 90;
		}
	}

	private static class IciclePlate extends TypeEnhancingItem implements
			HoldItem, PlateItem {
		private static final long serialVersionUID = 1L;

		public IciclePlate() {
			super.name = "Icicle Plate";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 83;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Ice-type moves.";
			super.price = 1000;
		}

		public Type getType() {
			return Type.ICE;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 90;
		}
	}

	private static class InsectPlate extends TypeEnhancingItem implements
			HoldItem, PlateItem {
		private static final long serialVersionUID = 1L;

		public InsectPlate() {
			super.name = "Insect Plate";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 84;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Bug-type moves.";
			super.price = 1000;
		}

		public Type getType() {
			return Type.BUG;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 90;
		}
	}

	private static class IronPlate extends TypeEnhancingItem implements
			HoldItem, PlateItem {
		private static final long serialVersionUID = 1L;

		public IronPlate() {
			super.name = "Iron Plate";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 85;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Steel-type moves.";
			super.price = 1000;
		}

		public Type getType() {
			return Type.STEEL;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 90;
		}
	}

	private static class MeadowPlate extends TypeEnhancingItem implements
			HoldItem, PlateItem {
		private static final long serialVersionUID = 1L;

		public MeadowPlate() {
			super.name = "Meadow Plate";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 86;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Grass-type moves.";
			super.price = 1000;
		}

		public Type getType() {
			return Type.GRASS;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 90;
		}
	}

	private static class MindPlate extends TypeEnhancingItem implements
			HoldItem, PlateItem {
		private static final long serialVersionUID = 1L;

		public MindPlate() {
			super.name = "Mind Plate";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 87;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Psychic-type moves.";
			super.price = 1000;
		}

		public Type getType() {
			return Type.PSYCHIC;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 90;
		}
	}

	private static class SkyPlate extends TypeEnhancingItem implements
			HoldItem, PlateItem {
		private static final long serialVersionUID = 1L;

		public SkyPlate() {
			super.name = "Sky Plate";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 88;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Flying-type moves.";
			super.price = 1000;
		}

		public Type getType() {
			return Type.FLYING;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 90;
		}
	}

	private static class SplashPlate extends TypeEnhancingItem implements
			HoldItem, PlateItem {
		private static final long serialVersionUID = 1L;

		public SplashPlate() {
			super.name = "Splash Plate";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 89;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Water-type moves.";
			super.price = 1000;
		}

		public Type getType() {
			return Type.WATER;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 90;
		}
	}

	private static class SpookyPlate extends TypeEnhancingItem implements
			HoldItem, PlateItem {
		private static final long serialVersionUID = 1L;

		public SpookyPlate() {
			super.name = "Spooky Plate";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 90;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Ghost-type moves.";
			super.price = 1000;
		}

		public Type getType() {
			return Type.GHOST;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 90;
		}
	}

	private static class StonePlate extends TypeEnhancingItem implements
			HoldItem, PlateItem {
		private static final long serialVersionUID = 1L;

		public StonePlate() {
			super.name = "Stone Plate";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 91;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Rock-type moves.";
			super.price = 1000;
		}

		public Type getType() {
			return Type.ROCK;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 90;
		}
	}

	private static class ToxicPlate extends TypeEnhancingItem implements
			HoldItem, PlateItem {
		private static final long serialVersionUID = 1L;

		public ToxicPlate() {
			super.name = "Toxic Plate";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 92;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Poison-type moves.";
			super.price = 1000;
		}

		public Type getType() {
			return Type.POISON;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 90;
		}
	}

	private static class ZapPlate extends TypeEnhancingItem implements
			HoldItem, PlateItem {
		private static final long serialVersionUID = 1L;

		public ZapPlate() {
			super.name = "Zap Plate";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 93;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Electric-type moves.";
			super.price = 1000;
		}

		public Type getType() {
			return Type.ELECTRIC;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 90;
		}
	}

	private static class BurnDrive extends Item implements DriveItem, HoldItem {
		private static final long serialVersionUID = 1L;

		public BurnDrive() {
			super.name = "Burn Drive";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 94;
			super.desc = "A cassette to be held by Genesect. It changes Techno Blast to a Fire-type move.";
			super.price = -1;
		}

		public int flingDamage() {
			return 70;
		}

		public Type getType() {
			return Type.FIRE;
		}
	}

	private static class ChillDrive extends Item implements DriveItem, HoldItem {
		private static final long serialVersionUID = 1L;

		public ChillDrive() {
			super.name = "Chill Drive";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 95;
			super.desc = "A cassette to be held by Genesect. It changes Techno Blast to an Ice-type move.";
			super.price = -1;
		}

		public int flingDamage() {
			return 70;
		}

		public Type getType() {
			return Type.ICE;
		}
	}

	private static class DouseDrive extends Item implements DriveItem, HoldItem {
		private static final long serialVersionUID = 1L;

		public DouseDrive() {
			super.name = "Douse Drive";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 96;
			super.desc = "A cassette to be held by Genesect. It changes Techno Blast to a Water-type move.";
			super.price = -1;
		}

		public int flingDamage() {
			return 70;
		}

		public Type getType() {
			return Type.WATER;
		}
	}

	private static class ShockDrive extends Item implements DriveItem, HoldItem {
		private static final long serialVersionUID = 1L;

		public ShockDrive() {
			super.name = "Shock Drive";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 97;
			super.desc = "A cassette to be held by Genesect. It changes Techno Blast to an Electric-type move.";
			super.price = -1;
		}

		public int flingDamage() {
			return 70;
		}

		public Type getType() {
			return Type.ELECTRIC;
		}
	}

	private static class FireGem extends TypeEnhancingItem implements HoldItem,
			ConsumedItem {
		private static final long serialVersionUID = 1L;

		public FireGem() {
			super.name = "Fire Gem";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 98;
			super.desc = "A gem with an essence of fire. When held, it strengthens the power of a Fire-type move only once.";
			super.price = 100;
		}

		public Type getType() {
			return Type.FIRE;
		}

		public double getMultiplier() {
			return 1.5;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class WaterGem extends TypeEnhancingItem implements
			HoldItem, ConsumedItem {
		private static final long serialVersionUID = 1L;

		public WaterGem() {
			super.name = "Water Gem";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 99;
			super.desc = "A gem with an essence of water. When held, it strengthens the power of a Water-type move only once.";
			super.price = 100;
		}

		public Type getType() {
			return Type.WATER;
		}

		public double getMultiplier() {
			return 1.5;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class ElectricGem extends TypeEnhancingItem implements
			HoldItem, ConsumedItem {
		private static final long serialVersionUID = 1L;

		public ElectricGem() {
			super.name = "Electric Gem";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 100;
			super.desc = "A gem with an essence of electricity. When held, it strengthens the power of an Electric-type move only once.";
			super.price = 100;
		}

		public Type getType() {
			return Type.ELECTRIC;
		}

		public double getMultiplier() {
			return 1.5;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class GrassGem extends TypeEnhancingItem implements
			HoldItem, ConsumedItem {
		private static final long serialVersionUID = 1L;

		public GrassGem() {
			super.name = "Grass Gem";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 101;
			super.desc = "A gem with an essence of nature. When held, it strengthens the power of a Grass-type move only once.";
			super.price = 100;
		}

		public Type getType() {
			return Type.GRASS;
		}

		public double getMultiplier() {
			return 1.5;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class IceGem extends TypeEnhancingItem implements HoldItem,
			ConsumedItem {
		private static final long serialVersionUID = 1L;

		public IceGem() {
			super.name = "Ice Gem";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 102;
			super.desc = "A gem with an essence of ice. When held, it strengthens the power of an Ice-type move only once.";
			super.price = 100;
		}

		public Type getType() {
			return Type.ICE;
		}

		public double getMultiplier() {
			return 1.5;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class FightingGem extends TypeEnhancingItem implements
			HoldItem, ConsumedItem {
		private static final long serialVersionUID = 1L;

		public FightingGem() {
			super.name = "Fighting Gem";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 103;
			super.desc = "A gem with an essence of combat. When held, it strengthens the power of a Fighting-type move only once.";
			super.price = 100;
		}

		public Type getType() {
			return Type.FIGHTING;
		}

		public double getMultiplier() {
			return 1.5;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class PoisonGem extends TypeEnhancingItem implements
			HoldItem, ConsumedItem {
		private static final long serialVersionUID = 1L;

		public PoisonGem() {
			super.name = "Poison Gem";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 104;
			super.desc = "A gem with an essence of poison. When held, it strengthens the power of a Poison-type move only once.";
			super.price = 100;
		}

		public Type getType() {
			return Type.POISON;
		}

		public double getMultiplier() {
			return 1.5;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class GroundGem extends TypeEnhancingItem implements
			HoldItem, ConsumedItem {
		private static final long serialVersionUID = 1L;

		public GroundGem() {
			super.name = "Ground Gem";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 105;
			super.desc = "A gem with an essence of land. When held, it strengthens the power of a Ground-type move only once.";
			super.price = 100;
		}

		public Type getType() {
			return Type.GROUND;
		}

		public double getMultiplier() {
			return 1.5;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class FlyingGem extends TypeEnhancingItem implements
			HoldItem, ConsumedItem {
		private static final long serialVersionUID = 1L;

		public FlyingGem() {
			super.name = "Flying Gem";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 106;
			super.desc = "A gem with an essence of air. When held, it strengthens the power of a Flying-type move only once.";
			super.price = 100;
		}

		public Type getType() {
			return Type.FLYING;
		}

		public double getMultiplier() {
			return 1.5;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class PsychicGem extends TypeEnhancingItem implements
			HoldItem, ConsumedItem {
		private static final long serialVersionUID = 1L;

		public PsychicGem() {
			super.name = "Psychic Gem";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 107;
			super.desc = "A gem with an essence of the mind. When held, it strengthens the power of a Psychic-type move only once.";
			super.price = 100;
		}

		public Type getType() {
			return Type.PSYCHIC;
		}

		public double getMultiplier() {
			return 1.5;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class BugGem extends TypeEnhancingItem implements HoldItem,
			ConsumedItem {
		private static final long serialVersionUID = 1L;

		public BugGem() {
			super.name = "Bug Gem";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 108;
			super.desc = "A gem with an insect-like essence. When held, it strengthens the power of a Bug-type move only once.";
			super.price = 100;
		}

		public Type getType() {
			return Type.BUG;
		}

		public double getMultiplier() {
			return 1.5;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class RockGem extends TypeEnhancingItem implements HoldItem,
			ConsumedItem {
		private static final long serialVersionUID = 1L;

		public RockGem() {
			super.name = "Rock Gem";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 109;
			super.desc = "A gem with an essence of rock. When held, it strengthens the power of a Rock-type move only once.";
			super.price = 100;
		}

		public Type getType() {
			return Type.ROCK;
		}

		public double getMultiplier() {
			return 1.5;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class GhostGem extends TypeEnhancingItem implements
			HoldItem, ConsumedItem {
		private static final long serialVersionUID = 1L;

		public GhostGem() {
			super.name = "Ghost Gem";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 110;
			super.desc = "A gem with a spectral essence. When held, it strengthens the power of a Ghost-type move only once.";
			super.price = 100;
		}

		public Type getType() {
			return Type.GHOST;
		}

		public double getMultiplier() {
			return 1.5;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class DragonGem extends TypeEnhancingItem implements
			HoldItem, ConsumedItem {
		private static final long serialVersionUID = 1L;

		public DragonGem() {
			super.name = "Dragon Gem";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 111;
			super.desc = "A gem with a draconic essence. When held, it strengthens the power of a Dragon-type move only once.";
			super.price = 100;
		}

		public Type getType() {
			return Type.DRAGON;
		}

		public double getMultiplier() {
			return 1.5;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class DarkGem extends TypeEnhancingItem implements HoldItem,
			ConsumedItem {
		private static final long serialVersionUID = 1L;

		public DarkGem() {
			super.name = "Dark Gem";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 112;
			super.desc = "A gem with an essence of darkness. When held, it strengthens the power of a Dark-type move only once.";
			super.price = 100;
		}

		public Type getType() {
			return Type.DARK;
		}

		public double getMultiplier() {
			return 1.5;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class SteelGem extends TypeEnhancingItem implements
			HoldItem, ConsumedItem {
		private static final long serialVersionUID = 1L;

		public SteelGem() {
			super.name = "Steel Gem";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 113;
			super.desc = "A gem with an essence of steel. When held, it strengthens the power of a Steel-type move only once.";
			super.price = 100;
		}

		public Type getType() {
			return Type.STEEL;
		}

		public double getMultiplier() {
			return 1.5;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class NormalGem extends TypeEnhancingItem implements
			HoldItem, ConsumedItem {
		private static final long serialVersionUID = 1L;

		public NormalGem() {
			super.name = "Normal Gem";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 114;
			super.desc = "A gem with an ordinary essence. When held, it strengthens the power of a Normal-type move only once.";
			super.price = 100;
		}

		public Type getType() {
			return Type.NORMAL;
		}

		public double getMultiplier() {
			return 1.5;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class Leftovers extends Item implements HoldItem,
			EndTurnEffect {
		private static final long serialVersionUID = 1L;

		public Leftovers() {
			super.name = "Leftovers";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 115;
			super.desc = "An item to be held by a Pok\u00e9mon. The holder's HP is gradually restored during battle.";
			super.price = 200;
		}

		public void apply(ActivePokemon victim, Battle b) {
			if (victim.fullHealth() || victim.hasEffect("Heal Block"))
				return;
			victim.healHealthFraction(1 / 16.0);
			b.addMessage(victim.getName() + "'s HP was restored by its "
					+ this.name + "!", victim.getHP(), victim.user());
		}

		public int flingDamage() {
			return 10;
		}
	}

	private static class BlackBelt extends TypeEnhancingItem implements
			HoldItem {
		private static final long serialVersionUID = 1L;

		public BlackBelt() {
			super.name = "Black Belt";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 116;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a belt that boosts determination and Fighting-type moves.";
			super.price = 9800;
		}

		public Type getType() {
			return Type.FIGHTING;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class BlackGlasses extends TypeEnhancingItem implements
			HoldItem {
		private static final long serialVersionUID = 1L;

		public BlackGlasses() {
			super.name = "Black Glasses";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 117;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a shady-looking pair of glasses that boosts Dark-type moves.";
			super.price = 9800;
		}

		public Type getType() {
			return Type.FIGHTING;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class Charcoal extends TypeEnhancingItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public Charcoal() {
			super.name = "Charcoal";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 118;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a combustible fuel that boosts the power of Fire-type moves.";
			super.price = 9800;
		}

		public Type getType() {
			return Type.FIRE;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class DragonFang extends TypeEnhancingItem implements
			HoldItem {
		private static final long serialVersionUID = 1L;

		public DragonFang() {
			super.name = "Dragon Fang";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 119;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a hard and sharp fang that ups the power of Dragon-type moves.";
			super.price = 9800;
		}

		public Type getType() {
			return Type.DRAGON;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class HardStone extends TypeEnhancingItem implements
			HoldItem {
		private static final long serialVersionUID = 1L;

		public HardStone() {
			super.name = "Hard Stone";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 120;
			super.desc = "An item to be held by a Pok\u00e9mon. It is an unbreakable stone that ups the power of Rock-type moves.";
			super.price = 9800;
		}

		public Type getType() {
			return Type.ROCK;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class Magnet extends TypeEnhancingItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public Magnet() {
			super.name = "Magnet";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 121;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a powerful magnet that boosts the power of Electric-type moves.";
			super.price = 9800;
		}

		public Type getType() {
			return Type.ELECTRIC;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class MetalCoat extends EvolutionItem implements HoldItem,
			PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		public MetalCoat() {
			super.name = "Metal Coat";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 122;
			super.desc = "A mysterious substance full of a special filmy metal. It allows certain kinds of Pok\u00e9mon to evolve.";
			super.price = 9800;
		}

		public int flingDamage() {
			return 30;
		}

		public double getMultiplier(Battle b, ActivePokemon user,
				ActivePokemon victim) {
			return user.getAttack().isType(b, user, Type.STEEL) ? 1.2 : 1;
		}
	}

	private static class MiracleSeed extends TypeEnhancingItem implements
			HoldItem {
		private static final long serialVersionUID = 1L;

		public MiracleSeed() {
			super.name = "Miracle Seed";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 123;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a seed imbued with life that ups the power of Grass-type moves.";
			super.price = 9800;
		}

		public Type getType() {
			return Type.GRASS;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class MysticWater extends TypeEnhancingItem implements
			HoldItem {
		private static final long serialVersionUID = 1L;

		public MysticWater() {
			super.name = "Mystic Water";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 124;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a teardrop-shaped gem that ups the power of Water-type moves.";
			super.price = 9800;
		}

		public Type getType() {
			return Type.WATER;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class NeverMeltIce extends TypeEnhancingItem implements
			HoldItem {
		private static final long serialVersionUID = 1L;

		public NeverMeltIce() {
			super.name = "NeverMeltIce";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 125;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a piece of ice that repels heat and boosts Ice-type moves.";
			super.price = 9800;
		}

		public Type getType() {
			return Type.ICE;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class PoisonBarb extends TypeEnhancingItem implements
			HoldItem {
		private static final long serialVersionUID = 1L;

		public PoisonBarb() {
			super.name = "Poison Barb";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 126;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a small, poisonous barb that ups the power of Poison-type moves.";
			super.price = 9800;
		}

		public Type getType() {
			return Type.POISON;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class SharpBeak extends TypeEnhancingItem implements
			HoldItem {
		private static final long serialVersionUID = 1L;

		public SharpBeak() {
			super.name = "Sharp Beak";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 127;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a long, sharp beak that boosts the power of Flying-type moves.";
			super.price = 9800;
		}

		public Type getType() {
			return Type.FLYING;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class SilkScarf extends TypeEnhancingItem implements
			HoldItem {
		private static final long serialVersionUID = 1L;

		public SilkScarf() {
			super.name = "Silk Scarf";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 128;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a sumptuous scarf that boosts the power of Normal-type moves.";
			super.price = 9800;
		}

		public Type getType() {
			return Type.NORMAL;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class SilverPowder extends TypeEnhancingItem implements
			HoldItem {
		private static final long serialVersionUID = 1L;

		public SilverPowder() {
			super.name = "Silver Powder";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 129;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a shiny, silver powder that ups the power of Bug-type moves.";
			super.price = 9800;
		}

		public Type getType() {
			return Type.BUG;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class SoftSand extends TypeEnhancingItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public SoftSand() {
			super.name = "Soft Sand";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 130;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a loose, silky sand that boosts the power of Ground-type moves.";
			super.price = 9800;
		}

		public Type getType() {
			return Type.GROUND;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class SpellTag extends TypeEnhancingItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public SpellTag() {
			super.name = "Spell Tag";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 131;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a sinister, eerie tag that boosts the power of Ghost-type moves.";
			super.price = 9800;
		}

		public Type getType() {
			return Type.GHOST;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class TwistedSpoon extends TypeEnhancingItem implements
			HoldItem {
		private static final long serialVersionUID = 1L;

		public TwistedSpoon() {
			super.name = "Twisted Spoon";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 132;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a spoon imbued with telekinetic power that boosts Psychic-type moves.";
			super.price = 9800;
		}

		public Type getType() {
			return Type.PSYCHIC;
		}

		public double getMultiplier() {
			return 1.2;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class DawnStone extends EvolutionItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public DawnStone() {
			super.name = "Dawn Stone";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 133;
			super.desc = "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It sparkles like eyes.";
			super.price = 2100;
		}

		public int flingDamage() {
			return 80;
		}
	}

	private static class DeepSeaScale extends EvolutionItem implements
			HoldItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		public DeepSeaScale() {
			super.name = "DeepSeaScale";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 134;
			super.desc = "An item to be held by Clamperl, Chinchou, or Lanturn. A scale that shines a faint pink, it raises the Sp. Def stat.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s,
				Battle b) {
			if (s == Stat.SP_DEFENSE
					&& (p.isPokemon("Clamperl") || p.isPokemon("Chinchou") || p
							.isPokemon("Lanturn")))
				stat *= 2;
			return stat;
		}
	}

	private static class DeepSeaTooth extends EvolutionItem implements
			HoldItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		public DeepSeaTooth() {
			super.name = "DeepSeaTooth";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 135;
			super.desc = "An item to be held by Clamperl. A fang that gleams a sharp silver, it raises the Sp. Atk stat.";
			super.price = 200;
		}

		public int flingDamage() {
			return 90;
		}

		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s,
				Battle b) {
			if (s == Stat.SP_ATTACK && p.isPokemon("Clamperl"))
				stat *= 2;
			return stat;
		}
	}

	private static class DragonScale extends EvolutionItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public DragonScale() {
			super.name = "Dragon Scale";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 136;
			super.desc = "A thick and tough scale. Dragon-type Pok\u00e9mon may be holding this item when caught.";
			super.price = 2100;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class DubiousDisc extends EvolutionItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public DubiousDisc() {
			super.name = "Dubious Disc";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 137;
			super.desc = "A transparent device overflowing with dubious data. Its producer is unknown.";
			super.price = 2100;
		}

		public int flingDamage() {
			return 50;
		}
	}

	private static class DuskStone extends EvolutionItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public DuskStone() {
			super.name = "Dusk Stone";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 138;
			super.desc = "A peculiar stone that makes certain species of Pokémon evolve. It is as dark as dark can be.";
			super.price = 2100;
		}

		public int flingDamage() {
			return 80;
		}
	}

	private static class Electirizer extends EvolutionItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public Electirizer() {
			super.name = "Electirizer";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 139;
			super.desc = "A box packed with a tremendous amount of electric energy. It is loved by a certain Pok\u00e9mon.";
			super.price = 2100;
		}

		public int flingDamage() {
			return 80;
		}
	}

	private static class FireStone extends EvolutionItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public FireStone() {
			super.name = "Fire Stone";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 140;
			super.desc = "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It is colored orange.";
			super.price = 2100;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class KingsRock extends EvolutionItem implements HoldItem,
			ApplyDamageEffect {
		private static final long serialVersionUID = 1L;

		public KingsRock() {
			super.name = "King's Rock";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 141;
			super.desc = "An item to be held by a Pok\u00e9mon. When the holder inflicts damage, the target may flinch.";
			super.price = 100;
		}

		public int flingDamage() {
			return 30;
		}

		public void applyEffect(Battle b, ActivePokemon user,
				ActivePokemon victim, Integer damage) {
			if (Math.random() * 100 < 10) {
				PokemonEffect flinch = PokemonEffect.getEffect("Flinch");
				if (flinch.applies(b, user, victim, CastSource.HELD_ITEM)) {
					flinch.cast(b, user, victim, CastSource.HELD_ITEM, false);
					b.addMessage(user.getName() + "'s " + this.name
							+ " caused " + victim.getName() + " to flinch!");
				}
			}
		}
	}

	private static class LeafStone extends EvolutionItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public LeafStone() {
			super.name = "Leaf Stone";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 142;
			super.desc = "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It has a leaf pattern.";
			super.price = 2100;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class Magmarizer extends EvolutionItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public Magmarizer() {
			super.name = "Magmarizer";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 143;
			super.desc = "A box packed with a tremendous amount of magma energy. It is loved by a certain Pok\u00e9mon.";
			super.price = 2100;
		}

		public int flingDamage() {
			return 80;
		}
	}

	private static class MoonStone extends EvolutionItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public MoonStone() {
			super.name = "Moon Stone";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 144;
			super.desc = "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It is as black as the night sky.";
			super.price = 2100;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class OvalStone extends EvolutionItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public OvalStone() {
			super.name = "Oval Stone";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 145;
			super.desc = "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It is shaped like an egg.";
			super.price = 2100;
		}

		public int flingDamage() {
			return 80;
		}
	}

	private static class Everstone extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public Everstone() {
			super.name = "Everstone";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 146;
			super.desc = "An item to be held by a Pok\u00e9mon. The Pok\u00e9mon holding this peculiar stone is prevented from evolving.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class PrismScale extends EvolutionItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public PrismScale() {
			super.name = "Prism Scale";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 147;
			super.desc = "A mysterious scale that evolves certain Pok\u00e9mon. It shines in rainbow colors.";
			super.price = 500;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class Protector extends EvolutionItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public Protector() {
			super.name = "Protector";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 148;
			super.desc = "A protective item of some sort. It is extremely stiff and heavy. It is loved by a certain Pok\u00e9mon.";
			super.price = 2100;
		}

		public int flingDamage() {
			return 80;
		}
	}

	private static class RazorClaw extends EvolutionItem implements HoldItem,
			CritStageEffect {
		private static final long serialVersionUID = 1L;

		public RazorClaw() {
			super.name = "Razor Claw";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 149;
			super.desc = "An item to be held by a Pok\u00e9mon. It is a sharply hooked claw that ups the holder's critical-hit ratio.";
			super.price = 2100;
		}

		public int flingDamage() {
			return 80;
		}

		public int increaseCritStage(ActivePokemon p) {
			return 1;
		}
	}

	private static class RazorFang extends EvolutionItem implements HoldItem,
			ApplyDamageEffect {
		private static final long serialVersionUID = 1L;

		public RazorFang() {
			super.name = "Razor Fang";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 150;
			super.desc = "An item to be held by a Pok\u00e9mon. It may make foes and allies flinch when the holder inflicts damage.`";
			super.price = 2100;
		}

		public int flingDamage() {
			return 30;
		}

		public void applyEffect(Battle b, ActivePokemon user,
				ActivePokemon victim, Integer damage) {
			if (Math.random() * 100 < 10) {
				PokemonEffect flinch = PokemonEffect.getEffect("Flinch");
				if (flinch.applies(b, user, victim, CastSource.HELD_ITEM)) {
					flinch.cast(b, user, victim, CastSource.HELD_ITEM, false);
					b.addMessage(user.getName() + "'s " + this.name
							+ " caused " + victim.getName() + " to flinch!");
				}
			}
		}
	}

	private static class ReaperCloth extends EvolutionItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public ReaperCloth() {
			super.name = "Reaper Cloth";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 151;
			super.desc = "A cloth imbued with horrifyingly strong spiritual energy. It is loved by a certain Pok\u00e9mon.";
			super.price = 2100;
		}

		public int flingDamage() {
			return 10;
		}
	}

	private static class ShinyStone extends EvolutionItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public ShinyStone() {
			super.name = "Shiny Stone";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 152;
			super.desc = "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It shines with a dazzling light.";
			super.price = 2100;
		}

		public int flingDamage() {
			return 80;
		}
	}

	private static class SunStone extends EvolutionItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public SunStone() {
			super.name = "Sun Stone";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 153;
			super.desc = "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It is as red as the sun.";
			super.price = 2100;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class ThunderStone extends EvolutionItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public ThunderStone() {
			super.name = "Thunder Stone";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 154;
			super.desc = "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It has a thunderbolt pattern.";
			super.price = 2100;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class UpGrade extends EvolutionItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public UpGrade() {
			super.name = "Up-Grade";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 155;
			super.desc = "A transparent device filled with all sorts of data. It was produced by Silph Co.";
			super.price = 2100;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class WaterStone extends EvolutionItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public WaterStone() {
			super.name = "Water Stone";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 156;
			super.desc = "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It is a clear, light blue.";
			super.price = 2100;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class Antidote extends StatusConditionRemoveItem implements
			HoldItem {
		private static final long serialVersionUID = 1L;

		public Antidote() {
			super.name = "Antidote";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.STATUS };
			super.index = 157;
			super.desc = "A spray-type medicine. It lifts the effect of poison from one Pok\u00e9mon.";
			super.price = 100;
		}

		public StatusCondition toRemove() {
			return StatusCondition.POISONED;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class Awakening extends StatusConditionRemoveItem implements
			HoldItem {
		private static final long serialVersionUID = 1L;

		public Awakening() {
			super.name = "Awakening";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.STATUS };
			super.index = 158;
			super.desc = "A spray-type medicine. It awakens a Pok\u00e9mon from the clutches of sleep.";
			super.price = 250;
		}

		public StatusCondition toRemove() {
			return StatusCondition.ASLEEP;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class BurnHeal extends StatusConditionRemoveItem implements
			HoldItem {
		private static final long serialVersionUID = 1L;

		public BurnHeal() {
			super.name = "Burn Heal";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.STATUS };
			super.index = 159;
			super.desc = "A spray-type medicine. It heals a single Pok\u00e9mon that is suffering from a burn.";
			super.price = 250;
		}

		public StatusCondition toRemove() {
			return StatusCondition.BURNED;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class IceHeal extends StatusConditionRemoveItem implements
			HoldItem {
		private static final long serialVersionUID = 1L;

		public IceHeal() {
			super.name = "Ice Heal";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.STATUS };
			super.index = 160;
			super.desc = "A spray-type medicine. It defrosts a Pok\u00e9mon that has been frozen solid.";
			super.price = 250;
		}

		public StatusCondition toRemove() {
			return StatusCondition.FROZEN;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class ParalyzeHeal extends StatusConditionRemoveItem
			implements HoldItem {
		private static final long serialVersionUID = 1L;

		public ParalyzeHeal() {
			super.name = "Paralyze Heal";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.STATUS };
			super.index = 161;
			super.desc = "A spray-type medicine. It eliminates paralysis from a single Pok\u00e9mon.";
			super.price = 200;
		}

		public StatusCondition toRemove() {
			return StatusCondition.PARALYZED;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class FullHeal extends Item implements PokemonUseItem,
			BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;

		public FullHeal() {
			super.name = "Full Heal";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.STATUS };
			super.index = 162;
			super.desc = "A spray-type medicine. It heals all the status problems of a single Pok\u00e9mon.";
			super.price = 250;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " was cured of its status condition!";
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(p);
		}

		public boolean use(ActivePokemon p) {
			if (!p.hasStatus() || p.hasStatus(StatusCondition.FAINTED))
				return false;

			p.removeStatus();
			return true;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class FullRestore extends Item implements PokemonUseItem,
			HoldItem, BattleUseItem {
		private static final long serialVersionUID = 1L;

		public FullRestore() {
			super.name = "Full Restore";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.HPPP,
					BattleBagCategory.STATUS };
			super.index = 163;
			super.desc = "A medicine that fully restores the HP and heals any status problems of a single Pok\u00e9mon.";
			super.price = 3000;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " was fully healed!";
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(p);
		}

		public boolean use(ActivePokemon p) {
			if (p.hasStatus(StatusCondition.FAINTED))
				return false;
			if (!p.hasStatus() && p.fullHealth())
				return false;

			p.removeStatus();
			p.healHealthFraction(1);
			return true;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class Elixir extends Item implements PokemonUseItem,
			BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;

		public Elixir() {
			super.name = "Elixir";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.HPPP };
			super.index = 164;
			super.desc = "It restores the PP of all the moves learned by the targeted Pok\u00e9mon by 10 points each.";
			super.price = 3000;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s PP was restored!";
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(p);
		}

		public boolean use(ActivePokemon p) {
			boolean changed = false;
			for (Move m : p.getMoves()) {
				changed |= m.increasePP(10);
			}
			return changed;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class MaxElixir extends Item implements PokemonUseItem,
			BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;

		public MaxElixir() {
			super.name = "Max Elixir";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.HPPP };
			super.index = 165;
			super.desc = "It restores the PP of all the moves learned by the targeted Pok\u00e9mon by 10 points each.";
			super.price = 4500;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s PP was restored!";
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(p);
		}

		public boolean use(ActivePokemon p) {
			boolean changed = false;
			for (Move m : p.getMoves()) {
				changed |= m.increasePP(m.getMaxPP());
			}

			return changed;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class Ether extends Item implements MoveUseItem, HoldItem {
		private static final long serialVersionUID = 1L;
		private String restore;

		public Ether() {
			super.name = "Ether";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.HPPP };
			super.index = 166;
			super.desc = "It restores the PP of a Pok\u00e9mon's selected move by a maximum of 10 points.";
			super.price = 1200;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + restore + "'s PP was restored!";
		}

		public boolean use(Move m) {
			restore = m.getAttack().getName();
			return m.increasePP(10);
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class MaxEther extends Item implements MoveUseItem, HoldItem {
		private static final long serialVersionUID = 1L;
		private String restore;

		public MaxEther() {
			super.name = "Max Ether";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.HPPP };
			super.index = 167;
			super.desc = "It fully restores the PP of a single selected move that has been learned by the target Pok\u00e9mon.";
			super.price = 2000;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + restore + "'s PP was restored!";
		}

		public boolean use(Move m) {
			restore = m.getAttack().getName();
			return m.increasePP(m.getMaxPP());
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class BerryJuice extends HealItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public BerryJuice() {
			super.name = "Berry Juice";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.HPPP };
			super.index = 168;
			super.desc = "A 100% pure juice made of Berries. It restores the HP of one Pok\u00e9mon by just 20 points.";
			super.price = 100;
		}

		public int healAmt() {
			return 20;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class SweetHeart extends HealItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public SweetHeart() {
			super.name = "Sweet Heart";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.HPPP };
			super.index = 169;
			super.desc = "Very sweet chocolate. It restores the HP of one Pok\u00e9mon by only 20 points.";
			super.price = 100;
		}

		public int healAmt() {
			return 20;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class Potion extends HealItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public Potion() {
			super.name = "Potion";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.HPPP };
			super.index = 170;
			super.desc = "A spray-type medicine for wounds. It restores the HP of one Pok\u00e9mon by just 20 points.";
			super.price = 100;
		}

		public int healAmt() {
			return 20;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class EnergyPowder extends HealItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public EnergyPowder() {
			super.name = "Energy Powder";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.HPPP };
			super.index = 171;
			super.desc = "A very bitter medicine powder. It restores the HP of one Pok\u00e9mon by 50 points.";
			super.price = 500;
		}

		public int healAmt() {
			return 50;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class FreshWater extends HealItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public FreshWater() {
			super.name = "Fresh Water";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.HPPP };
			super.index = 172;
			super.desc = "Water with a high mineral content. It restores the HP of one Pok\u00e9mon by 50 points.";
			super.price = 200;
		}

		public int healAmt() {
			return 50;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class SuperPotion extends HealItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public SuperPotion() {
			super.name = "Super Potion";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.HPPP };
			super.index = 173;
			super.desc = "A spray-type medicine for wounds. It restores the HP of one Pok\u00e9mon by 50 points.";
			super.price = 700;
		}

		public int healAmt() {
			return 50;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class SodaPop extends HealItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public SodaPop() {
			super.name = "Soda Pop";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.HPPP };
			super.index = 174;
			super.desc = "A fizzy soda drink. It restores the HP of one Pok\u00e9mon by 60 points.";
			super.price = 300;
		}

		public int healAmt() {
			return 60;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class Lemonade extends HealItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public Lemonade() {
			super.name = "Lemonade";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.HPPP };
			super.index = 175;
			super.desc = "A very sweet drink. It restores the HP of one Pok\u00e9mon by 80 points.";
			super.price = 350;
		}

		public int healAmt() {
			return 80;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class MoomooMilk extends HealItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public MoomooMilk() {
			super.name = "Moomoo Milk";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.HPPP };
			super.index = 176;
			super.desc = "Milk with a very high nutrition content. It restores the HP of one Pok\u00e9mon by 100 points.";
			super.price = 500;
		}

		public int healAmt() {
			return 100;
		}

		public int flingDamage() {
			return 1000;
		}
	}

	private static class EnergyRoot extends HealItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public EnergyRoot() {
			super.name = "Energy Root";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.HPPP };
			super.index = 177;
			super.desc = "A very bitter root. It restores the HP of one Pok\u00e9mon by 200 points.";
			super.price = 800;
		}

		public int healAmt() {
			return 200;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class HyperPotion extends HealItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public HyperPotion() {
			super.name = "Hyper Potion";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.HPPP };
			super.index = 178;
			super.desc = "A spray-type medicine for wounds. It restores the HP of one Pok\u00e9mon by 200 points.";
			super.price = 1200;
		}

		public int healAmt() {
			return 200;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class MaxPotion extends Item implements PokemonUseItem,
			BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;

		public MaxPotion() {
			super.name = "Max Potion";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.HPPP };
			super.index = 179;
			super.desc = "A spray-type medicine for wounds. It completely restores the HP of a single Pok\u00e9mon.";
			super.price = 2500;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s health was restored!";
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(p);
		}

		public boolean use(ActivePokemon p) {
			return p.healHealthFraction(1) != 0;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class Revive extends Item implements PokemonUseItem,
			BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;

		public Revive() {
			super.name = "Revive";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.STATUS };
			super.index = 180;
			super.desc = "A medicine that revives a fainted Pok\u00e9mon. It restores half the Pok\u00e9mon's maximum HP.";
			super.price = 1500;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " was revived!";
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(p);
		}

		public boolean use(ActivePokemon p) {
			if (!p.hasStatus(StatusCondition.FAINTED))
				return false;

			p.removeStatus();
			p.healHealthFraction(.5);

			return true;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class MaxRevive extends Item implements PokemonUseItem,
			HoldItem {
		private static final long serialVersionUID = 1L;

		public MaxRevive() {
			super.name = "Max Revive";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.STATUS };
			super.index = 181;
			super.desc = "A medicine that revives a fainted Pok\u00e9mon. It fully restores the Pok\u00e9mon's HP.";
			super.price = 4000;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " was fully revived!";
		}

		public boolean use(ActivePokemon p) {
			if (!p.hasStatus(StatusCondition.FAINTED))
				return false;

			p.removeStatus();
			p.healHealthFraction(1);

			return true;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class RevivalHerb extends Item implements PokemonUseItem,
			HoldItem {
		private static final long serialVersionUID = 1L;

		public RevivalHerb() {
			super.name = "Revival Herb";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.STATUS };
			super.index = 182;
			super.desc = "A very bitter medicinal herb. It revives a fainted Pok\u00e9mon, fully restoring its HP.";
			super.price = 2800;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " was fully revived!";
		}

		public boolean use(ActivePokemon p) {
			if (!p.hasStatus(StatusCondition.FAINTED))
				return false;

			p.removeStatus();
			p.healHealthFraction(1);

			return true;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class SacredAsh extends Item implements TrainerUseItem,
			HoldItem, BattleUseItem {
		private static final long serialVersionUID = 1L;

		public SacredAsh() {
			super.name = "Sacred Ash";
			super.cat = BagCategory.MEDICINE;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.STATUS };
			super.index = 183;
			super.desc = "It revives all fainted Pok\u00e9mon. In doing so, it also fully restores their HP.";
			super.price = 4000;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "All fainted Pok\u00e9mon were fully revived!";
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use((Trainer) b.getTrainer(p.user()));
		}

		public boolean use(Trainer t) {
			boolean changed = false;
			for (ActivePokemon p : t.getTeam()) {
				if (p.hasStatus(StatusCondition.FAINTED)) {
					changed = true;
					p.removeStatus();
					p.healHealthFraction(1);
				}
			}
			return changed;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class DireHit extends Item implements BattleUseItem,
			HoldItem {
		private static final long serialVersionUID = 1L;

		public DireHit() {
			super.name = "Dire Hit";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BATTLE };
			super.index = 184;
			super.desc = "It raises the critical-hit ratio greatly. It can be used only once and wears off if the Pok\u00e9mon is withdrawn.";
			super.price = 650;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " is getting pumped!";
		}

		public boolean use(ActivePokemon p, Battle b) {
			PokemonEffect crits = PokemonEffect.getEffect("RaiseCrits");
			if (!crits.applies(b, p, p, CastSource.USE_ITEM))
				return false;

			crits.cast(b, p, p, CastSource.USE_ITEM, false);
			return true;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class GuardSpec extends Item implements BattleUseItem,
			HoldItem {
		private static final long serialVersionUID = 1L;

		public GuardSpec() {
			super.name = "Guard Spec.";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BATTLE };
			super.index = 185;
			super.desc = "An item that prevents stat reduction among the Trainer's party Pok\u00e9mon for five turns after use.";
			super.price = 700;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " is covered by a veil!";
		}

		public boolean use(ActivePokemon p, Battle b) {
			PokemonEffect gSpesh = PokemonEffect.getEffect("GuardSpecial");
			if (!gSpesh.applies(b, p, p, CastSource.USE_ITEM))
				return false;

			gSpesh.cast(b, p, p, CastSource.USE_ITEM, false);
			return true;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class XAccuracy extends StageIncreaseItem implements
			HoldItem {
		private static final long serialVersionUID = 1L;

		public XAccuracy() {
			super.name = "X Accuracy";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BATTLE };
			super.index = 186;
			super.desc = "An item that raises the accuracy of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.";
			super.price = 950;
		}

		public Stat toIncrease() {
			return Stat.ACCURACY;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class XAttack extends StageIncreaseItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public XAttack() {
			super.name = "X Attack";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BATTLE };
			super.index = 187;
			super.desc = "An item that raises the Attack stat of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.";
			super.price = 500;
		}

		public Stat toIncrease() {
			return Stat.ATTACK;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class XDefend extends StageIncreaseItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public XDefend() {
			super.name = "X Defend";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BATTLE };
			super.index = 188;
			super.desc = "An item that raises the Defense stat of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.";
			super.price = 550;
		}

		public Stat toIncrease() {
			return Stat.SP_DEFENSE;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class XSpDef extends StageIncreaseItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public XSpDef() {
			super.name = "X Sp. Def";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BATTLE };
			super.index = 189;
			super.desc = "An item that raises the Sp. Def stat of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.";
			super.price = 350;
		}

		public Stat toIncrease() {
			return Stat.SP_DEFENSE;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class XSpecial extends StageIncreaseItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public XSpecial() {
			super.name = "X Special";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BATTLE };
			super.index = 190;
			super.desc = "An item that raises the Sp. Atk stat of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.";
			super.price = 350;
		}

		public Stat toIncrease() {
			return Stat.SP_ATTACK;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class XSpeed extends StageIncreaseItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public XSpeed() {
			super.name = "X Speed";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BATTLE };
			super.index = 191;
			super.desc = "An item that raises the Speed stat of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.";
			super.price = 350;
		}

		public Stat toIncrease() {
			return Stat.SPEED;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class Calcium extends EVIncreaseItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public Calcium() {
			super.name = "Calcium";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[0];
			super.index = 192;
			super.desc = "A nutritious drink for Pok\u00e9mon. It raises the base Sp. Atk (Special Attack) stat of a single Pok\u00e9mon.";
			super.price = 9800;
		}

		public Stat toIncrease() {
			return Stat.SP_ATTACK;
		}

		public int increaseAmt() {
			return 1;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class Carbos extends EVIncreaseItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public Carbos() {
			super.name = "Carbos";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[0];
			super.index = 193;
			super.desc = "A nutritious drink for Pok\u00e9mon. It raises the base Speed stat of a single Pok\u00e9mon.";
			super.price = 9800;
		}

		public Stat toIncrease() {
			return Stat.SPEED;
		}

		public int increaseAmt() {
			return 1;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class CleverWing extends EVIncreaseItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public CleverWing() {
			super.name = "Clever Wing";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[0];
			super.index = 194;
			super.desc = "An item for use on a Pok\u00e9mon. It slightly increases the base Sp. Def stat of a single Pok\u00e9mon.";
			super.price = 3000;
		}

		public Stat toIncrease() {
			return Stat.SP_ATTACK;
		}

		public int increaseAmt() {
			return 1;
		}

		public int flingDamage() {
			return 20;
		}
	}

	private static class HealthWing extends EVIncreaseItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public HealthWing() {
			super.name = "Health Wing";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[0];
			super.index = 195;
			super.desc = "An item for use on a Pok\u00e9mon. It slightly increases the base HP of a single Pok\u00e9mon.";
			super.price = 3000;
		}

		public Stat toIncrease() {
			return Stat.HP;
		}

		public int increaseAmt() {
			return 1;
		}

		public int flingDamage() {
			return 20;
		}
	}

	private static class HPUp extends EVIncreaseItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public HPUp() {
			super.name = "HP Up";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[0];
			super.index = 196;
			super.desc = "A nutritious drink for Pok\u00e9mon. It raises the base HP of a single Pok\u00e9mon.";
			super.price = 9800;
		}

		public Stat toIncrease() {
			return Stat.HP;
		}

		public int increaseAmt() {
			return 10;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class GeniusWing extends EVIncreaseItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public GeniusWing() {
			super.name = "Genius Wing";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[0];
			super.index = 197;
			super.desc = "An item for use on a Pok\u00e9mon. It slightly increases the base Sp. Atk stat of a single Pok\u00e9mon.";
			super.price = 3000;
		}

		public Stat toIncrease() {
			return Stat.SP_ATTACK;
		}

		public int increaseAmt() {
			return 1;
		}

		public int flingDamage() {
			return 20;
		}
	}

	private static class Iron extends EVIncreaseItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public Iron() {
			super.name = "Iron";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[0];
			super.index = 198;
			super.desc = "A nutritious drink for Pok\u00e9mon. It raises the base Defense stat of a single Pok\u00e9mon.";
			super.price = 9800;
		}

		public Stat toIncrease() {
			return Stat.DEFENSE;
		}

		public int increaseAmt() {
			return 10;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class MuscleWing extends EVIncreaseItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public MuscleWing() {
			super.name = "Muscle Wing";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[0];
			super.index = 199;
			super.desc = "An item for use on a Pok\u00e9mon. It slightly increases the base Attack stat of a single Pok\u00e9mon.";
			super.price = 3000;
		}

		public Stat toIncrease() {
			return Stat.ATTACK;
		}

		public int increaseAmt() {
			return 1;
		}

		public int flingDamage() {
			return 20;
		}
	}

	private static class PPMax extends Item implements MoveUseItem, HoldItem {
		private static final long serialVersionUID = 1L;
		private String increase;

		public PPMax() {
			super.name = "PP Max";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[0];
			super.index = 200;
			super.desc = "It maximally raises the top PP of a selected move that has been learned by the target Pok\u00e9mon.";
			super.price = 9800;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + increase + "'s Max PP was increased!";
		}

		public boolean use(Move m) {
			increase = m.getAttack().getName();
			return m.increaseMaxPP(3);
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class PPUp extends Item implements MoveUseItem, HoldItem {
		private static final long serialVersionUID = 1L;
		private String increase;

		public PPUp() {
			super.name = "PP Up";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[0];
			super.index = 201;
			super.desc = "It slightly raises the maximum PP of a selected move that has been learned by the target Pok\u00e9mon.";
			super.price = 9800;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + increase + "'s Max PP was increased!";
		}

		public boolean use(Move m) {
			increase = m.getAttack().getName();
			return m.increaseMaxPP(1);
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class Protein extends EVIncreaseItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public Protein() {
			super.name = "Protein";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[0];
			super.index = 202;
			super.desc = "A nutritious drink for Pok\u00e9mon. It raises the base Attack stat of a single Pok\u00e9mon.";
			super.price = 9800;
		}

		public Stat toIncrease() {
			return Stat.ATTACK;
		}

		public int increaseAmt() {
			return 10;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class RareCandy extends EVIncreaseItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public RareCandy() {
			super.name = "Rare Candy";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[0];
			super.index = 203;
			super.desc = "A candy that is packed with energy. It raises the level of a single Pok\u00e9mon by one.";
			super.price = 4800;
		}

		public Stat toIncrease() {
			return null;
		}

		public int increaseAmt() {
			return 1;
		}

		public boolean use(ActivePokemon p) {// TODO: Need Level Up to be
												// implemented
			return false;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class ResistWing extends EVIncreaseItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public ResistWing() {
			super.name = "Resist Wing";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[0];
			super.index = 204;
			super.desc = "An item for use on a Pok\u00e9mon. It slightly increases the base Defense stat of a single Pok\u00e9mon.";
			super.price = 3000;
		}

		public Stat toIncrease() {
			return Stat.DEFENSE;
		}

		public int increaseAmt() {
			return 1;
		}

		public int flingDamage() {
			return 20;
		}
	}

	private static class SwiftWing extends EVIncreaseItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public SwiftWing() {
			super.name = "Swift Wing";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[0];
			super.index = 205;
			super.desc = "An item for use on a Pok\u00e9mon. It slightly increases the base Speed stat of a single Pok\u00e9mon.";
			super.price = 3000;
		}

		public Stat toIncrease() {
			return Stat.SPEED;
		}

		public int increaseAmt() {
			return 1;
		}

		public int flingDamage() {
			return 20;
		}
	}

	private static class Zinc extends EVIncreaseItem implements HoldItem {
		private static final long serialVersionUID = 1L;

		public Zinc() {
			super.name = "Zinc";
			super.cat = BagCategory.STAT;
			super.bcat = new BattleBagCategory[0];
			super.index = 206;
			super.desc = "A nutritious drink for Pok\u00e9mon. It raises the base Sp. Def (Special Defense) stat of a single Pok\u00e9mon.";
			super.price = 9800;
		}

		public Stat toIncrease() {
			return Stat.SP_DEFENSE;
		}

		public int increaseAmt() {
			return 10;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class CherishBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		public CherishBall() {
			super.name = "Cherish Ball";
			super.cat = BagCategory.BALL;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BALL };
			super.index = 207;
			super.desc = "A quite rare Pok\u00e9 Ball that has been specially crafted to commemorate an occasion of some sort.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			return new double[] { 1, 0 };
		}

		public void afterCaught(ActivePokemon p) {
			return;
		}
	}

	private static class DiveBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		public DiveBall() {
			super.name = "Dive Ball";
			super.cat = BagCategory.BALL;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BALL };
			super.index = 208;
			super.desc = "A somewhat different Pok\u00e9 Ball that works especially well on Pok\u00e9mon that live underwater.";
			super.price = 1000;
		}

		public int flingDamage() {
			return 30;
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {// TODO:
																				// How
																				// do
																				// I
																				// see
																				// if
																				// the
																				// user
																				// is
																				// underwater,
																				// surfing,
																				// or
																				// fishing?
			if (false)
				return new double[] { 3.5, 0 };
			else
				return new double[] { 1, 0 };
		}

		public void afterCaught(ActivePokemon p) {
			return;
		}
	}

	private static class DuskBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		public DuskBall() {
			super.name = "Dusk Ball";
			super.cat = BagCategory.BALL;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BALL };
			super.index = 209;
			super.desc = "A somewhat different Pok\u00e9 Ball that makes it easier to catch wild Pok\u00e9mon at night or in dark places like caves.";
			super.price = 1000;
		}

		public int flingDamage() {
			return 30;
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {// TODO:
																				// How
																				// do
																				// I
																				// see
																				// if
																				// the
																				// user
																				// is
																				// in
																				// a
																				// dark
																				// environment?
			if (false)
				return new double[] { 3.5, 0 };
			else
				return new double[] { 1, 0 };
		}

		public void afterCaught(ActivePokemon p) {
			return;
		}
	}

	private static class FastBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		public FastBall() {
			super.name = "Fast Ball";
			super.cat = BagCategory.BALL;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BALL };
			super.index = 210;
			super.desc = "A Pok\u00e9 Ball that makes it easier to catch Pok\u00e9mon which are quick to run away.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			if (o.getStat(Stat.SPEED) >= 100)
				return new double[] { 4, 0 };
			else
				return new double[] { 1, 0 };
		}

		public void afterCaught(ActivePokemon p) {
			return;
		}
	}

	private static class GreatBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		public GreatBall() {
			super.name = "Great Ball";
			super.cat = BagCategory.BALL;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BALL };
			super.index = 211;
			super.desc = "A good, high-performance Ball that provides a higher Pok\u00e9mon catch rate than a standard Pok\u00e9 Ball.";
			super.price = 600;
		}

		public int flingDamage() {
			return 30;
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			return new double[] { 1.5, 0 };
		}

		public void afterCaught(ActivePokemon p) {
			return;
		}
	}

	private static class HealBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		public HealBall() {
			super.name = "Heal Ball";
			super.cat = BagCategory.BALL;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BALL };
			super.index = 212;
			super.desc = "A remedial Pok\u00e9 Ball that restores the caught Pok\u00e9mon's HP and eliminates any status problem.";
			super.price = 300;
		}

		public int flingDamage() {
			return 30;
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			return new double[] { 1, 0 };
		}

		public void afterCaught(ActivePokemon p) {
			p.healHealthFraction(1);
			for (Move m : p.getMoves())
				m.increasePP(m.getAttack().getPP());
		}
	}

	private static class HeavyBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		public HeavyBall() {
			super.name = "Heavy Ball";
			super.cat = BagCategory.BALL;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BALL };
			super.index = 213;
			super.desc = "A Pok\u00e9 Ball for catching very heavy Pok\u00e9mon.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			double weight = o.getWeight(b);

			double[] res = new double[2];
			res[0] = 1;

			if (weight <= 451.5)
				res[1] = -20;
			else if (weight <= 661.5)
				res[1] = 20;
			else if (weight <= 903.0)
				res[1] = 30;
			else
				res[1] = 40;

			return res;
		}

		public void afterCaught(ActivePokemon p) {
			return;
		}
	}

	private static class LevelBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		public LevelBall() {
			super.name = "Level Ball";
			super.cat = BagCategory.BALL;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BALL };
			super.index = 214;
			super.desc = "A Pok\u00e9 Ball for catching Pok\u00e9mon that are a lower level than your own.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			if (me.getLevel() / 4 > o.getLevel())
				return new double[] { 8, 0 };
			else if (me.getLevel() / 2 > o.getLevel())
				return new double[] { 4, 0 };
			else if (me.getLevel() > o.getLevel())
				return new double[] { 2, 0 };
			else
				return new double[] { 1, 0 };
		}

		public void afterCaught(ActivePokemon p) {
			return;
		}
	}

	private static class LoveBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		public LoveBall() {
			super.name = "Love Ball";
			super.cat = BagCategory.BALL;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BALL };
			super.index = 215;
			super.desc = "Pok\u00e9 Ball for catching Pok\u00e9mon that are the opposite gender of your Pok\u00e9mon.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			if (me.getGender() == o.getGender())
				return new double[] { 8, 0 };
			else
				return new double[] { 1, 0 };
		}

		public void afterCaught(ActivePokemon p) {
			return;
		}
	}

	private static class LureBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		public LureBall() {
			super.name = "Lure Ball";
			super.cat = BagCategory.BALL;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BALL };
			super.index = 216;
			super.desc = "A Pok\u00e9 Ball for catching Pok\u00e9mon hooked by a Rod when fishing.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			if (false)
				return new double[] { 3, 0 };
			else
				return new double[] { 1, 0 };
		}

		public void afterCaught(ActivePokemon p) {
			return;
		}
	}

	private static class LuxuryBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		public LuxuryBall() {
			super.name = "Luxury Ball";
			super.cat = BagCategory.BALL;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BALL };
			super.index = 217;
			super.desc = "A comfortable Pok\u00e9 Ball that makes a caught wild Pok\u00e9mon quickly grow friendly.";
			super.price = 1000;
		}

		public int flingDamage() {
			return 30;
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {// :
																				// Booring.
																				// We
																				// should
																				// change
																				// it
																				// up.
			return new double[] { 1, 0 };
		}

		public void afterCaught(ActivePokemon p) {
			return;
		}
	}

	private static class MasterBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		public MasterBall() {
			super.name = "Master Ball";
			super.cat = BagCategory.BALL;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BALL };
			super.index = 218;
			super.desc = "The best Ball with the ultimate level of performance. It will catch any wild Pok\u00e9mon without fail.";
			super.price = 0;
		}

		public int flingDamage() {
			return 30;
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			return new double[] { 255, 0 };
		}

		public void afterCaught(ActivePokemon p) {
			return;
		}
	}

	private static class MoonBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		public MoonBall() {
			super.name = "Moon Ball";
			super.cat = BagCategory.BALL;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BALL };
			super.index = 219;
			super.desc = "A Pok\u00e9 Ball for catching Pok\u00e9mon that evolve using the Moon Stone.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			Evolution ev = o.getPokemonInfo().getEvolution();
			if (ev.getEvolution(EvolutionCheck.ITEM, o, "Moon Stone") != null)
				return new double[] { 4, 0 };
			else
				return new double[] { 1, 0 };
		}

		public void afterCaught(ActivePokemon p) {
			return;
		}
	}

	private static class NestBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		public NestBall() {
			super.name = "Nest Ball";
			super.cat = BagCategory.BALL;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BALL };
			super.index = 220;
			super.desc = "A somewhat different Pok\u00e9 Ball that works especially well on weaker Pok\u00e9mon in the wild.";
			super.price = 1000;
		}

		public int flingDamage() {
			return 30;
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			if (o.getLevel() <= 19)
				return new double[] { 3, 0 };
			else if (o.getLevel() <= 29)
				return new double[] { 2, 0 };
			else
				return new double[] { 1, 0 };
		}

		public void afterCaught(ActivePokemon p) {
			return;
		}
	}

	private static class NetBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		public NetBall() {
			super.name = "Net Ball";
			super.cat = BagCategory.BALL;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BALL };
			super.index = 221;
			super.desc = "A somewhat different Pok\u00e9 Ball that works especially well on Water- and Bug-type Pok\u00e9mon.";
			super.price = 1000;
		}

		public int flingDamage() {
			return 30;
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			if (o.isType(b, Type.WATER) || o.isType(b, Type.BUG))
				return new double[] { 3, 0 };
			else
				return new double[] { 1, 0 };
		}

		public void afterCaught(ActivePokemon p) {
			return;
		}
	}

	private static class PokeBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		public PokeBall() {
			super.name = "Pok\u00e9 Ball";
			super.cat = BagCategory.BALL;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BALL };
			super.index = 222;
			super.desc = "A device for catching wild Pok\u00e9mon. It is thrown like a ball at the target. It is designed as a capsule system.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			return new double[] { 1, 0 };
		}

		public void afterCaught(ActivePokemon p) {
			return;
		}
	}

	private static class PremierBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		public PremierBall() {
			super.name = "Premier Ball";
			super.cat = BagCategory.BALL;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BALL };
			super.index = 223;
			super.desc = "A somewhat rare Pok\u00e9 Ball that has been specially made to commemorate an event of some sort.";
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			return new double[] { 1, 0 };
		}

		public void afterCaught(ActivePokemon p) {
			return;
		}
	}

	private static class QuickBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		public QuickBall() {
			super.name = "Quick Ball";
			super.cat = BagCategory.BALL;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BALL };
			super.index = 224;
			super.desc = "A somewhat different Pok\u00e9 Ball that provides a better catch rate if it is used at the start of a wild encounter.";
			super.price = 1000;
		}

		public int flingDamage() {
			return 30;
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			if (b.getTurn() == 1)
				return new double[] { 3, 0 };
			else
				return new double[] { 1, 0 };
		}

		public void afterCaught(ActivePokemon p) {
			return;
		}
	}

	private static class RepeatBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		public RepeatBall() {
			super.name = "Repeat Ball";
			super.cat = BagCategory.BALL;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BALL };
			super.index = 225;
			super.desc = "A somewhat different Pok\u00e9 Ball that works especially well on Pok\u00e9mon species that were previously caught.";
			super.price = 1000;
		}

		public int flingDamage() {
			return 30;
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			if (b.getPlayer().getPokedex().caught(o.getName()))
				return new double[] { 3, 0 };
			return new double[] { 1, 0 };
		}

		public void afterCaught(ActivePokemon p) {
			return;
		}
	}

	private static class SafariBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		public SafariBall() {
			super.name = "Safari Ball";
			super.cat = BagCategory.BALL;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BALL };
			super.index = 226;
			super.desc = "A special Pok\u00e9 Ball that is used only in the Safari Zone. It is decorated in a camouflage pattern.";
			super.price = 0;
		}

		public int flingDamage() {
			return 0;
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			return new double[] { 1.5, 0 };
		}

		public void afterCaught(ActivePokemon p) {
			return;
		}
	}

	private static class TimerBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		public TimerBall() {
			super.name = "Timer Ball";
			super.cat = BagCategory.BALL;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BALL };
			super.index = 227;
			super.desc = "A somewhat different Ball that becomes progressively better the more turns there are in a battle.";
			super.price = 1000;
		}

		public int flingDamage() {
			return 30;
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			if (b.getTurn() <= 10)
				return new double[] { 1, 0 };
			else if (b.getTurn() <= 20)
				return new double[] { 2, 0 };
			else if (b.getTurn() <= 30)
				return new double[] { 3, 0 };
			else
				return new double[] { 4, 0 };
		}

		public void afterCaught(ActivePokemon p) {
			return;
		}
	}

	private static class UltraBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		public UltraBall() {
			super.name = "Ultra Ball";
			super.cat = BagCategory.BALL;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.BALL };
			super.index = 228;
			super.desc = "An ultra-performance Ball that provides a higher Pok\u00e9mon catch rate than a Great Ball.";
			super.price = 1200;
		}

		public int flingDamage() {
			return 30;
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			return new double[] { 2, 0 };
		}

		public void afterCaught(ActivePokemon p) {
			return;
		}
	}

	private static class CheriBerry extends StatusConditionRemoveItem implements
			StatusBerry {
		private static final long serialVersionUID = 1L;

		public CheriBerry() {
			super.name = "Cheri Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.STATUS };
			super.index = 229;
			super.desc = "If held by a Pok\u00e9mon, it recovers from paralysis.";
			super.price = 20;
		}

		public StatusCondition toRemove() {
			return StatusCondition.PARALYZED;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.FIRE;
		}

		public int naturalGiftPower() {
			return 60;
		}

		public void useBerry(Battle b, ActivePokemon user, ActivePokemon opp) {
			if (use(user, b))
				b.addMessage(getSuccessMessage(user), user.getStatus()
						.getType(), user.user());
		}
	}

	private static class ChestoBerry extends StatusConditionRemoveItem
			implements StatusBerry {
		private static final long serialVersionUID = 1L;

		public ChestoBerry() {
			super.name = "Chesto Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.STATUS };
			super.index = 230;
			super.desc = "If held by a Pok\u00e9mon, it recovers from sleep.";
			super.price = 20;
		}

		public StatusCondition toRemove() {
			return StatusCondition.ASLEEP;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.WATER;
		}

		public int naturalGiftPower() {
			return 60;
		}

		public void useBerry(Battle b, ActivePokemon user, ActivePokemon opp) {
			if (use(user, b))
				b.addMessage(getSuccessMessage(user), user.getStatus()
						.getType(), user.user());
		}
	}

	private static class PechaBerry extends StatusConditionRemoveItem implements
			StatusBerry {
		private static final long serialVersionUID = 1L;

		public PechaBerry() {
			super.name = "Pecha Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.STATUS };
			super.index = 231;
			super.desc = "If held by a Pok\u00e9mon, it recovers from poison.";
			super.price = 20;
		}

		public StatusCondition toRemove() {
			return StatusCondition.POISONED;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.ELECTRIC;
		}

		public int naturalGiftPower() {
			return 60;
		}

		public void useBerry(Battle b, ActivePokemon user, ActivePokemon opp) {
			if (use(user, b))
				b.addMessage(getSuccessMessage(user), user.getStatus()
						.getType(), user.user());
		}
	}

	private static class RawstBerry extends StatusConditionRemoveItem implements
			StatusBerry {
		private static final long serialVersionUID = 1L;

		public RawstBerry() {
			super.name = "Rawst Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.STATUS };
			super.index = 232;
			super.desc = "If held by a Pok\u00e9mon, it recovers from a burn.";
			super.price = 20;
		}

		public StatusCondition toRemove() {
			return StatusCondition.BURNED;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.GRASS;
		}

		public int naturalGiftPower() {
			return 60;
		}

		public void useBerry(Battle b, ActivePokemon user, ActivePokemon opp) {
			if (use(user, b))
				b.addMessage(getSuccessMessage(user), user.getStatus()
						.getType(), user.user());
		}
	}

	private static class AspearBerry extends StatusConditionRemoveItem
			implements StatusBerry {
		private static final long serialVersionUID = 1L;

		public AspearBerry() {
			super.name = "Aspear Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.STATUS };
			super.index = 233;
			super.desc = "If held by a Pok\u00e9mon, it defrosts it.";
			super.price = 20;
		}

		public StatusCondition toRemove() {
			return StatusCondition.FROZEN;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.GRASS;
		}

		public int naturalGiftPower() {
			return 60;
		}

		public void useBerry(Battle b, ActivePokemon user, ActivePokemon opp) {
			if (use(user, b))
				b.addMessage(getSuccessMessage(user), user.getStatus()
						.getType(), user.user());
		}
	}

	private static class LeppaBerry extends Item implements EndTurnEffect,
			MoveUseItem, GainableEffectBerry {
		private static final long serialVersionUID = 1L;
		private String restore;

		public LeppaBerry() {
			super.name = "Leppa Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.HPPP };
			super.index = 234;
			super.desc = "If held by a Pok\u00e9mon, it restores a move's PP by 10.";
			super.price = 20;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + restore + "'s PP was restored!";
		}

		public void apply(ActivePokemon victim, Battle b) {
			for (Move m : victim.getMoves()) {
				if (m.getPP() == 0) {
					b.addMessage(victim.getName() + "'s " + this.name
							+ " restored " + m.getAttack().getName() + "'s PP!");
					m.increasePP(10);
					victim.consumeItem(b);
					break;
				}
			}
		}

		public boolean use(Move m) {
			restore = m.getAttack().getName();
			return m.increasePP(10);
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.FIGHTING;
		}

		public int naturalGiftPower() {
			return 60;
		}

		public void useBerry(Battle b, ActivePokemon user, ActivePokemon opp) {
			List<Move> list = new ArrayList<>();
			for (Move m : user.getMoves()) {
				if (m.getPP() < m.getMaxPP()) {
					list.add(m);
				}
			}

			int size = list.size();
			if (size == 0)
				return;

			Move m = list.get((int) (Math.random() * size));
			m.increasePP(10);
			b.addMessage(user.getName() + "'s " + this.name + " restored "
					+ m.getAttack().getName() + "'s PP!");
		}
	}

	private static class OranBerry extends HealItem implements
			HealthTriggeredBerry {
		private static final long serialVersionUID = 1L;

		public OranBerry() {
			super.name = "Oran Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.HPPP };
			super.index = 235;
			super.desc = "If held by a Pok\u00e9mon, it heals the user by just 10 HP.";
			super.price = 20;
		}

		public int healAmt() {
			return 10;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.POISON;
		}

		public int naturalGiftPower() {
			return 60;
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user) {
			if (user.fullHealth())
				return false;

			user.heal(10);
			b.addMessage(user.getName() + " was healed by its " + this.name
					+ "!", user.getHP(), user.user());
			return true;
		}

		public double healthTriggerRatio() {
			return 1 / 3.0;
		}

		public void useBerry(Battle b, ActivePokemon user, ActivePokemon opp) {
			useHealthTriggerBerry(b, user);
		}
	}

	private static class PersimBerry extends Item implements BattleUseItem,
			GainableEffectBerry {
		private static final long serialVersionUID = 1L;

		public PersimBerry() {
			super.name = "Persim Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.HPPP };
			super.index = 236;
			super.desc = "If held by a Pok\u00e9mon, it recovers from confusion.";
			super.price = 20;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " snapped out of its confusion!";
		}

		public boolean use(ActivePokemon p, Battle b) {
			if (p.hasEffect("Confusion")) {
				p.getAttributes().removeEffect("Confusion");
				return true;
			}

			return false;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.GROUND;
		}

		public int naturalGiftPower() {
			return 60;
		}

		public void useBerry(Battle b, ActivePokemon user, ActivePokemon opp) {
			if (use(user, b)) {
				b.addMessage(getSuccessMessage(user));
			}
		}
	}

	private static class LumBerry extends Item implements PokemonUseItem,
			BattleUseItem, StatusBerry {
		private static final long serialVersionUID = 1L;

		public LumBerry() {
			super.name = "Lum Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.STATUS };
			super.index = 237;
			super.desc = "If held by a Pok\u00e9mon, it recovers from any status problem.";
			super.price = 20;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " was cured of its status condition!";
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(p);
		}

		public boolean use(ActivePokemon p) {
			if (!p.hasStatus() || p.hasStatus(StatusCondition.FAINTED))
				return false;

			p.removeStatus();
			return true;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.FLYING;
		}

		public int naturalGiftPower() {
			return 60;
		}

		public void useBerry(Battle b, ActivePokemon user, ActivePokemon opp) {
			if (use(user, b))
				b.addMessage(getSuccessMessage(user), user.getStatus()
						.getType(), user.user());
		}
	}

	private static class SitrusBerry extends Item implements PokemonUseItem,
			BattleUseItem, HealthTriggeredBerry {
		private static final long serialVersionUID = 1L;

		public SitrusBerry() {
			super.name = "Sitrus Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[] { BattleBagCategory.HPPP };
			super.index = 238;
			super.desc = "If held by a Pok\u00e9mon, it heals the user by a little.";
			super.price = 20;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s health was restored!";
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(p);
		}

		public boolean use(ActivePokemon p) {
			return p.healHealthFraction(1 / 4.0) != 0;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.PSYCHIC;
		}

		public int naturalGiftPower() {
			return 60;
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user) {
			if (user.fullHealth())
				return false;

			user.healHealthFraction(1 / 4.0);
			b.addMessage(user.getName() + " was healed by its " + this.name
					+ "!", user.getHP(), user.user());
			return true;
		}

		public double healthTriggerRatio() {
			return 1 / 2.0;
		}

		public void useBerry(Battle b, ActivePokemon user, ActivePokemon opp) {
			useHealthTriggerBerry(b, user);
		}
	}

	private static class RazzBerry extends Item implements Berry {
		private static final long serialVersionUID = 1L;

		public RazzBerry() {
			super.name = "Razz Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 239;
			super.desc = "A very valuable berry. Useful for aquiring value.";
			super.price = 60000;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.STEEL;
		}

		public int naturalGiftPower() {
			return 60;
		}
	}

	private static class PomegBerry extends EVDecreaseBerry {
		private static final long serialVersionUID = 1L;

		public PomegBerry() {
			super.name = "Pomeg Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 240;
			super.desc = "Using it on a Pok\u00e9mon lowers its base HP.";
			super.price = 20;
		}

		public Stat toDecrease() {
			return Stat.HP;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.ICE;
		}

		public int naturalGiftPower() {
			return 70;
		}
	}

	private static class KelpsyBerry extends EVDecreaseBerry {
		private static final long serialVersionUID = 1L;

		public KelpsyBerry() {
			super.name = "Kelpsy Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 241;
			super.desc = "Using it on a Pok\u00e9mon lowers its base Attack stat.";
			super.price = 20;
		}

		public Stat toDecrease() {
			return Stat.ATTACK;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.FIGHTING;
		}

		public int naturalGiftPower() {
			return 70;
		}
	}

	private static class QualotBerry extends EVDecreaseBerry {
		private static final long serialVersionUID = 1L;

		public QualotBerry() {
			super.name = "Qualot Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 242;
			super.desc = "Using it on a Pok\u00e9mon lowers its base Defense stat.";
			super.price = 20;
		}

		public Stat toDecrease() {
			return Stat.DEFENSE;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.POISON;
		}

		public int naturalGiftPower() {
			return 70;
		}
	}

	private static class HondewBerry extends EVDecreaseBerry {
		private static final long serialVersionUID = 1L;

		public HondewBerry() {
			super.name = "Hondew Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 243;
			super.desc = "Using it on a Pok\u00e9mon lowers its base Sp. Atk stat.";
			super.price = 20;
		}

		public Stat toDecrease() {
			return Stat.SP_ATTACK;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.GROUND;
		}

		public int naturalGiftPower() {
			return 70;
		}
	}

	private static class GrepaBerry extends EVDecreaseBerry {
		private static final long serialVersionUID = 1L;

		public GrepaBerry() {
			super.name = "Grepa Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 244;
			super.desc = "Using it on a Pok\u00e9mon lowers its base Sp. Def stat.";
			super.price = 20;
		}

		public Stat toDecrease() {
			return Stat.SP_DEFENSE;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.FLYING;
		}

		public int naturalGiftPower() {
			return 70;
		}
	}

	private static class TamatoBerry extends EVDecreaseBerry {
		private static final long serialVersionUID = 1L;

		public TamatoBerry() {
			super.name = "Tamato Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 245;
			super.desc = "Using it on a Pok\u00e9mon lowers its base Speed.";
			super.price = 20;
		}

		public Stat toDecrease() {
			return Stat.SPEED;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.PSYCHIC;
		}

		public int naturalGiftPower() {
			return 70;
		}
	}

	private static class OccaBerry extends SuperEffectivePowerReduceBerry {
		private static final long serialVersionUID = 1L;

		public OccaBerry() {
			super.name = "Occa Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 246;
			super.desc = "Weakens a supereffective Fire-type attack against the holding Pok\u00e9mon.";
			super.price = 20;
		}

		public Type getType() {
			return Type.FIRE;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.FIRE;
		}

		public int naturalGiftPower() {
			return 60;
		}
	}

	private static class PasshoBerry extends SuperEffectivePowerReduceBerry {
		private static final long serialVersionUID = 1L;

		public PasshoBerry() {
			super.name = "Passho Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 247;
			super.desc = "Weakens a supereffective Water-type attack against the holding Pok\u00e9mon.";
			super.price = 20;
		}

		public Type getType() {
			return Type.WATER;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.WATER;
		}

		public int naturalGiftPower() {
			return 60;
		}
	}

	private static class WacanBerry extends SuperEffectivePowerReduceBerry {
		private static final long serialVersionUID = 1L;

		public WacanBerry() {
			super.name = "Wacan Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 248;
			super.desc = "Weakens a supereffective Electric-type attack against the holding Pok\u00e9mon.";
			super.price = 20;
		}

		public Type getType() {
			return Type.ELECTRIC;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.ELECTRIC;
		}

		public int naturalGiftPower() {
			return 60;
		}
	}

	private static class RindoBerry extends SuperEffectivePowerReduceBerry {
		private static final long serialVersionUID = 1L;

		public RindoBerry() {
			super.name = "Rindo Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 249;
			super.desc = "Weakens a supereffective Grass-type attack against the holding Pok\u00e9mon.";
			super.price = 20;
		}

		public Type getType() {
			return Type.GRASS;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.GRASS;
		}

		public int naturalGiftPower() {
			return 60;
		}
	}

	private static class YacheBerry extends SuperEffectivePowerReduceBerry {
		private static final long serialVersionUID = 1L;

		public YacheBerry() {
			super.name = "Yache Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 250;
			super.desc = "Weakens a supereffective Ice-type attack against the holding Pok\u00e9mon.";
			super.price = 20;
		}

		public Type getType() {
			return Type.ICE;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.ICE;
		}

		public int naturalGiftPower() {
			return 60;
		}
	}

	private static class ChopleBerry extends SuperEffectivePowerReduceBerry {
		private static final long serialVersionUID = 1L;

		public ChopleBerry() {
			super.name = "Chople Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 251;
			super.desc = "Weakens a supereffective Fighting-type attack against the holding Pok\u00e9mon.";
			super.price = 20;
		}

		public Type getType() {
			return Type.FIGHTING;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.FIGHTING;
		}

		public int naturalGiftPower() {
			return 60;
		}
	}

	private static class KebiaBerry extends SuperEffectivePowerReduceBerry {
		private static final long serialVersionUID = 1L;

		public KebiaBerry() {
			super.name = "Kebia Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 252;
			super.desc = "Weakens a supereffective Poison-type attack against the holding Pok\u00e9mon.";
			super.price = 20;
		}

		public Type getType() {
			return Type.POISON;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.POISON;
		}

		public int naturalGiftPower() {
			return 60;
		}
	}

	private static class ShucaBerry extends SuperEffectivePowerReduceBerry {
		private static final long serialVersionUID = 1L;

		public ShucaBerry() {
			super.name = "Shuca Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 253;
			super.desc = "Weakens a supereffective Ground-type attack against the holding Pok\u00e9mon.";
			super.price = 20;
		}

		public Type getType() {
			return Type.GROUND;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.GROUND;
		}

		public int naturalGiftPower() {
			return 60;
		}
	}

	private static class CobaBerry extends SuperEffectivePowerReduceBerry {
		private static final long serialVersionUID = 1L;

		public CobaBerry() {
			super.name = "Coba Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 254;
			super.desc = "Weakens a supereffective Flying-type attack against the holding Pok\u00e9mon.";
			super.price = 20;
		}

		public Type getType() {
			return Type.FLYING;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.FLYING;
		}

		public int naturalGiftPower() {
			return 60;
		}
	}

	private static class PayapaBerry extends SuperEffectivePowerReduceBerry {
		private static final long serialVersionUID = 1L;

		public PayapaBerry() {
			super.name = "Payapa Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 255;
			super.desc = "Weakens a supereffective Psychic-type attack against the holding Pok\u00e9mon.";
			super.price = 20;
		}

		public Type getType() {
			return Type.PSYCHIC;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.PSYCHIC;
		}

		public int naturalGiftPower() {
			return 60;
		}
	}

	private static class TangaBerry extends SuperEffectivePowerReduceBerry {
		private static final long serialVersionUID = 1L;

		public TangaBerry() {
			super.name = "Tanga Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 256;
			super.desc = "Weakens a supereffective Bug-type attack against the holding Pok\u00e9mon.";
			super.price = 20;
		}

		public Type getType() {
			return Type.BUG;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.BUG;
		}

		public int naturalGiftPower() {
			return 60;
		}
	}

	private static class ChartiBerry extends SuperEffectivePowerReduceBerry {
		private static final long serialVersionUID = 1L;

		public ChartiBerry() {
			super.name = "Charti Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 257;
			super.desc = "Weakens a supereffective Rock-type attack against the holding Pok\u00e9mon.";
			super.price = 20;
		}

		public Type getType() {
			return Type.ROCK;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.ROCK;
		}

		public int naturalGiftPower() {
			return 60;
		}
	}

	private static class KasibBerry extends SuperEffectivePowerReduceBerry {
		private static final long serialVersionUID = 1L;

		public KasibBerry() {
			super.name = "Kasib Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 258;
			super.desc = "Weakens a supereffective Ghost-type attack against the holding Pok\u00e9mon.";
			super.price = 20;
		}

		public Type getType() {
			return Type.GHOST;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.GHOST;
		}

		public int naturalGiftPower() {
			return 60;
		}
	}

	private static class HabanBerry extends SuperEffectivePowerReduceBerry {
		private static final long serialVersionUID = 1L;

		public HabanBerry() {
			super.name = "Haban Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 259;
			super.desc = "Weakens a supereffective Dragon-type attack against the holding Pok\u00e9mon.";
			super.price = 20;
		}

		public Type getType() {
			return Type.DRAGON;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.DRAGON;
		}

		public int naturalGiftPower() {
			return 60;
		}
	}

	private static class ColburBerry extends SuperEffectivePowerReduceBerry {
		private static final long serialVersionUID = 1L;

		public ColburBerry() {
			super.name = "Colbur Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 260;
			super.desc = "Weakens a supereffective Dark-type attack against the holding Pok\u00e9mon.";
			super.price = 20;
		}

		public Type getType() {
			return Type.DARK;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.DARK;
		}

		public int naturalGiftPower() {
			return 60;
		}
	}

	private static class BabiriBerry extends SuperEffectivePowerReduceBerry {
		private static final long serialVersionUID = 1L;

		public BabiriBerry() {
			super.name = "Babiri Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 261;
			super.desc = "Weakens a supereffective Steel-type attack against the holding Pok\u00e9mon.";
			super.price = 20;
		}

		public Type getType() {
			return Type.STEEL;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.STEEL;
		}

		public int naturalGiftPower() {
			return 60;
		}
	}

	private static class ChilanBerry extends SuperEffectivePowerReduceBerry {
		private static final long serialVersionUID = 1L;

		public ChilanBerry() {
			super.name = "Chilan Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 262;
			super.desc = "Weakens a supereffective Normal-type attack against the holding Pok\u00e9mon.";
			super.price = 20;
		}

		public Type getType() {
			return Type.NORMAL;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.NORMAL;
		}

		public int naturalGiftPower() {
			return 60;
		}
	}

	private static class LiechiBerry extends HealthTriggeredStageIncreaseBerry {
		private static final long serialVersionUID = 1L;

		public LiechiBerry() {
			super.name = "Liechi Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 263;
			super.desc = "If held by a Pok\u00e9mon, it raises its Attack stat in a pinch.";
			super.price = 20;
		}

		public Stat toRaise() {
			return Stat.ATTACK;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.GRASS;
		}

		public int naturalGiftPower() {
			return 80;
		}
	}

	private static class GanlonBerry extends HealthTriggeredStageIncreaseBerry {
		private static final long serialVersionUID = 1L;

		public GanlonBerry() {
			super.name = "Ganlon Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 264;
			super.desc = "If held by a Pok\u00e9mon, it raises its Defense stat in a pinch.";
			super.price = 20;
		}

		public Stat toRaise() {
			return Stat.DEFENSE;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.ICE;
		}

		public int naturalGiftPower() {
			return 80;
		}
	}

	private static class SalacBerry extends HealthTriggeredStageIncreaseBerry {
		private static final long serialVersionUID = 1L;

		public SalacBerry() {
			super.name = "Salac Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 265;
			super.desc = "If held by a Pok\u00e9mon, it raises its Speed stat in a pinch.";
			super.price = 20;
		}

		public Stat toRaise() {
			return Stat.SPEED;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.FIGHTING;
		}

		public int naturalGiftPower() {
			return 80;
		}
	}

	private static class PetayaBerry extends HealthTriggeredStageIncreaseBerry {
		private static final long serialVersionUID = 1L;

		public PetayaBerry() {
			super.name = "Petaya Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 266;
			super.desc = "If held by a Pok\u00e9mon, it raises its Speed stat in a pinch.";
			super.price = 20;
		}

		public Stat toRaise() {
			return Stat.SP_ATTACK;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.POISON;
		}

		public int naturalGiftPower() {
			return 80;
		}
	}

	private static class ApicotBerry extends HealthTriggeredStageIncreaseBerry {
		private static final long serialVersionUID = 1L;

		public ApicotBerry() {
			super.name = "Apicot Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 267;
			super.desc = "If held by a Pok\u00e9mon, it raises its Speed stat in a pinch.";
			super.price = 20;
		}

		public Stat toRaise() {
			return Stat.SP_DEFENSE;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.GROUND;
		}

		public int naturalGiftPower() {
			return 80;
		}
	}

	private static class LansatBerry extends Item implements
			HealthTriggeredBerry {
		private static final long serialVersionUID = 1L;

		public LansatBerry() {
			super.name = "Lansat Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 268;
			super.desc = "If held by a Pok\u00e9mon, it raises its critical-hit ratio in a pinch.";
			super.price = 20;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.FLYING;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user) {
			PokemonEffect.getEffect("RaiseCrits").cast(b, user, user,
					CastSource.HELD_ITEM, false);
			b.addMessage(user.getName() + " is getting pumped due to its "
					+ this.name + "!");
			return true;
		}

		public double healthTriggerRatio() {
			return 1 / 4.0;
		}

		public void useBerry(Battle b, ActivePokemon user, ActivePokemon opp) {
			useHealthTriggerBerry(b, user);
		}
	}

	private static class StarfBerry extends Item implements
			HealthTriggeredBerry {
		private static final long serialVersionUID = 1L;

		public StarfBerry() {
			super.name = "Starf Berry";
			super.cat = BagCategory.BERRY;
			super.bcat = new BattleBagCategory[0];
			super.index = 269;
			super.desc = "If held by a Pok\u00e9mon, it raises a random stat in a pinch.";
			super.price = 20;
		}

		public int flingDamage() {
			return 10;
		}

		public Type naturalGiftType() {
			return Type.PSYCHIC;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user) {
			int rand = (int) (Math.random() * (Stat.NUM_BATTLE_STATS + 1));

			// Raise crit
			if (rand == Stat.NUM_BATTLE_STATS) {
				PokemonEffect.getEffect("RaiseCrits").cast(b, user, user,
						CastSource.HELD_ITEM, false);
				b.addMessage(user.getName() + " is getting pumped due to its "
						+ this.name + "!");
				return true;
			}

			// Raise random battle stat
			if (user.getAttributes().modifyStage(user, user, 1,
					Stat.getStat(rand, true), b, CastSource.HELD_ITEM)) {
				return true;
			}

			return false;
		}

		public double healthTriggerRatio() {
			return 1 / 4.0;
		}

		public void useBerry(Battle b, ActivePokemon user, ActivePokemon opp) {
			useHealthTriggerBerry(b, user);
		}
	}

	private static class CometShard extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public CometShard() {
			super.name = "Comet Shard";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 270;
			super.desc = "A shard which fell to the ground when a comet approached. A maniac will buy it for a high price.";
			super.price = 120000;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class TinyMushroom extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public TinyMushroom() {
			super.name = "Tiny Mushroom";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 271;
			super.desc = "A small and rare mushroom. It is sought after by collectors.";
			super.price = 500;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class BigMushroom extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public BigMushroom() {
			super.name = "Big Mushroom";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 272;
			super.desc = "A large and rare mushroom. It is sought after by collectors.";
			super.price = 5000;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class BalmMushroom extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public BalmMushroom() {
			super.name = "Balm Mushroom";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 273;
			super.desc = "A rare mushroom which gives off a nice fragrance. A maniac will buy it for a high price.";
			super.price = 50000;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class Nugget extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public Nugget() {
			super.name = "Nugget";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 274;
			super.desc = "A nugget of pure gold that gives off a lustrous gleam. It can be sold at a high price to shops.";
			super.price = 10000;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class BigNugget extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public BigNugget() {
			super.name = "Big Nugget";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 275;
			super.desc = "A big nugget of pure gold that gives off a lustrous gleam. A maniac will buy it for a high price.";
			super.price = 60000;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class Pearl extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public Pearl() {
			super.name = "Pearl";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 276;
			super.desc = "A somewhat-small pearl that sparkles in a pretty silver color. It can be sold cheaply to shops.";
			super.price = 1400;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class BigPearl extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public BigPearl() {
			super.name = "Big Pearl";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 277;
			super.desc = "A quite-large pearl that sparkles in a pretty silver color. It can be sold at a high price to shops.";
			super.price = 7500;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class Stardust extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public Stardust() {
			super.name = "Stardust";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 278;
			super.desc = "Lovely, red-colored sand with a loose, silky feel. It can be sold at a high price to shops.";
			super.price = 2000;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class StarPiece extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public StarPiece() {
			super.name = "Star Piece";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 279;
			super.desc = "A shard of a pretty gem that sparkles in a red color. It can be sold at a high price to shops.";
			super.price = 9800;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class RareBone extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public RareBone() {
			super.name = "Rare Bone";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 280;
			super.desc = "A bone that is extremely valuable for Pokémon archeology. It can be sold for a high price to shops.";
			super.price = 10000;
		}

		public int flingDamage() {
			return 100;
		}
	}

	private static class Honey extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public Honey() {
			super.name = "Honey";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 281;
			super.desc = "A sweet honey with a lush aroma that attracts wild Pokémon when it is used in grass, caves, or on special trees.";
			super.price = 100;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class Eviolite extends Item implements HoldItem,
			StatChangingEffect {
		private static final long serialVersionUID = 1L;

		public Eviolite() {
			super.name = "Eviolite";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 282;
			super.desc = "A mysterious evolutionary lump. When held, it raises the Defense and Sp. Def of a Pokémon that can still evolve.";
			super.price = 200;
		}

		public int flingDamage() {
			return 40;
		}

		public int modify(int stat, ActivePokemon p, ActivePokemon opp, Stat s,
				Battle b) {
			if ((s == Stat.DEFENSE || s == Stat.SP_DEFENSE)
					&& p.getPokemonInfo().getEvolution().canEvolve())
				return (int) (1.5 * stat);
			return stat;
		}
	}

	private static class HeartScale extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		public HeartScale() {
			super.name = "Heart Scale";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 283;
			super.desc = "A pretty, heart-shaped scale that is extremely rare. It glows faintly in the colors of the rainbow.";
			super.price = 100;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class Repel extends RepelItem {
		private static final long serialVersionUID = 1L;

		public Repel() {
			super.name = "Repel";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 284;
			super.desc = "An item that prevents weak wild Pok\u00e9mon from appearing for 100 steps after its use.";
			super.price = 350;
		}

		public int repelSteps() {
			return 100;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class SuperRepel extends RepelItem {
		private static final long serialVersionUID = 1L;

		public SuperRepel() {
			super.name = "Super Repel";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 285;
			super.desc = "An item that prevents weak wild Pokémon from appearing for 200 steps after its use.";
			super.price = 500;
		}

		public int repelSteps() {
			return 200;
		}

		public int flingDamage() {
			return 30;
		}
	}

	private static class MaxRepel extends RepelItem {
		private static final long serialVersionUID = 1L;

		public MaxRepel() {
			super.name = "Max Repel";
			super.cat = BagCategory.MISC;
			super.bcat = new BattleBagCategory[0];
			super.index = 286;
			super.desc = "An item that prevents weak wild Pokémon from appearing for 250 steps after its use.";
			super.price = 700;
		}

		public int repelSteps() {
			return 250;
		}

		public int flingDamage() {
			return 30;
		}
	}
}
