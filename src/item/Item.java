package item;

import battle.Attack;
import battle.Battle;
import battle.Move;
import battle.MoveCategory;
import battle.MoveType;
import battle.effect.AdvantageChanger;
import battle.effect.AttackSelectionEffect;
import battle.effect.BeforeTurnEffect;
import battle.effect.BracingEffect;
import battle.effect.CritStageEffect;
import battle.effect.DefiniteEscape;
import battle.effect.EffectBlockerEffect;
import battle.effect.EndTurnEffect;
import battle.effect.EntryEffect;
import battle.effect.GroundedEffect;
import battle.effect.HalfWeightEffect;
import battle.effect.LevitationEffect;
import battle.effect.OpponentPowerChangeEffect;
import battle.effect.PhysicalContactEffect;
import battle.effect.PowerChangeEffect;
import battle.effect.PriorityChangeEffect;
import battle.effect.RepellingEffect;
import battle.effect.StallingEffect;
import battle.effect.StatChangingEffect;
import battle.effect.StatProtectingEffect;
import battle.effect.TakeDamageEffect;
import battle.effect.WeatherBlockerEffect;
import battle.effect.WeatherExtendingEffect;
import battle.effect.generic.Effect;
import battle.effect.generic.Effect.CastSource;
import battle.effect.generic.EffectInterfaces;
import battle.effect.generic.EffectInterfaces.ApplyDamageEffect;
import battle.effect.generic.PokemonEffect;
import battle.effect.generic.TeamEffect;
import battle.effect.holder.ItemHolder;
import battle.effect.status.Status;
import battle.effect.status.StatusCondition;
import item.berry.Berry;
import item.berry.GainableEffectBerry;
import item.berry.HealthTriggeredBerry;
import item.berry.StatusBerry;
import item.hold.ConsumableItem;
import item.hold.DriveItem;
import item.hold.EVItem;
import item.hold.GemItem;
import item.hold.HoldItem;
import item.hold.IncenseItem;
import item.hold.PlateItem;
import item.hold.PowerItem;
import item.use.BallItem;
import item.use.BattleUseItem;
import item.use.MoveUseItem;
import item.use.PokemonUseItem;
import item.use.TrainerUseItem;
import item.use.UseItem;
import main.Global;
import main.Type;
import map.AreaData.TerrainType;
import namesies.AbilityNamesies;
import namesies.AttackNamesies;
import namesies.EffectNamesies;
import namesies.ItemNamesies;
import namesies.PokemonNamesies;
import pokemon.Ability;
import pokemon.ActivePokemon;
import pokemon.BaseEvolution;
import pokemon.Evolution;
import pokemon.Evolution.EvolutionCheck;
import pokemon.PokemonInfo;
import pokemon.Stat;
import trainer.CharacterData;
import trainer.Team;
import trainer.Trainer;
import trainer.Trainer.Action;
import trainer.WildPokemon;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Item implements Comparable<Item>, Serializable {
	private static final long serialVersionUID = 1L;

	private static Map<String, Item> map; // TODO: change the key to namesies -- enum map?

	protected ItemNamesies namesies;
	protected String name;
	private String description;
	private BagCategory bagCategory;
	private List<BattleBagCategory> battleBagCategories;
	private int price;
	private int imageIndex;

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		synchronized(Item.class) {
			if (map == null) {
				loadItems();
			}

			map.put(this.getName(), this);
		}
	}

	public Item(ItemNamesies name, String description, BagCategory category, int index) {
		this.namesies = name;
		this.name = name.getName();
		this.description = description;
		this.bagCategory = category;
		this.imageIndex = index;

		this.battleBagCategories = new ArrayList<>();
		this.price = -1;
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

	public boolean hasQuantity() {
		return bagCategory != BagCategory.TM && bagCategory != BagCategory.KEY_ITEM;
	}

	public ItemNamesies namesies() {
		return this.namesies;
	}

	public String getName() {
		return this.name;
	}
	public String getDescription()
	{
		return this.description;
	}

	public int getPrice() {
		return this.price;
	}

	public int getImageIndex() {
		return this.imageIndex;
	}

	public BagCategory getBagCategory() {
		return this.bagCategory;
	}

	public Iterable<BattleBagCategory> getBattleBagCategories() {
		return this.battleBagCategories;
	}

	public static Item noneItem() {
		return getItem(ItemNamesies.NO_ITEM);
	}

	public static Item getItemFromName(String itemName) {
		return getItem(ItemNamesies.getValueOf(itemName));
	}

	public static Item getItem(ItemNamesies itemNamesies) {
		String itemName = itemNamesies.getName();
		if (!isItem(itemName)) {
			Global.error("No such Item " + itemName);
		}

		return map.get(itemName);
	}

	public static boolean isItem(String m) {
		if (map == null) {
			loadItems();
		}

		return map.containsKey(m);
	}

	public int hashCode() {
		return name.hashCode();
	}

	private static void processIncenseItems() {
		Set<String> itemStringKeySet = map.keySet();
		for (String itemString : itemStringKeySet) {
			Item item = map.get(itemString);
			if (item instanceof IncenseItem) {
				IncenseItem incense = (IncenseItem) item;
				PokemonInfo.addIncenseBaby(incense.getBaby());
			}
		}
	}

	public static void loadItems() {
		if (map != null) {
			return;
		}

		map = new HashMap<>();

		// EVERYTHING BELOW IS GENERATED ###

		// List all of the classes we are loading
		map.put("No Item", new NoItem());
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
		map.put("Adamant Orb", new AdamantOrb());
		map.put("Lustrous Orb", new LustrousOrb());
		map.put("Griseous Orb", new GriseousOrb());
		map.put("Iron Ball", new IronBall());
		map.put("Lagging Tail", new LaggingTail());
		map.put("Life Orb", new LifeOrb());
		map.put("Light Ball", new LightBall());
		map.put("Light Clay", new LightClay());
		map.put("Lucky Egg", new LuckyEgg());
		map.put("Lucky Punch", new LuckyPunch());
		map.put("Luminous Moss", new LuminousMoss());
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
		map.put("Pixie Plate", new PixiePlate());
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
		map.put("Fairy Gem", new FairyGem());
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
		map.put("Deep Sea Scale", new DeepSeaScale());
		map.put("Deep Sea Tooth", new DeepSeaTooth());
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
		map.put("Sachet", new Sachet());
		map.put("Shiny Stone", new ShinyStone());
		map.put("Sun Stone", new SunStone());
		map.put("Thunder Stone", new ThunderStone());
		map.put("Up-Grade", new UpGrade());
		map.put("Water Stone", new WaterStone());
		map.put("Whipped Dream", new WhippedDream());
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
		map.put("X Special", new XSpecial());
		map.put("X Sp. Def", new XSpDef());
		map.put("X Speed", new XSpeed());
		map.put("HP Up", new HPUp());
		map.put("Protein", new Protein());
		map.put("Iron", new Iron());
		map.put("Calcium", new Calcium());
		map.put("Zinc", new Zinc());
		map.put("Carbos", new Carbos());
		map.put("Health Wing", new HealthWing());
		map.put("Muscle Wing", new MuscleWing());
		map.put("Resist Wing", new ResistWing());
		map.put("Genius Wing", new GeniusWing());
		map.put("Clever Wing", new CleverWing());
		map.put("Swift Wing", new SwiftWing());
		map.put("PP Max", new PPMax());
		map.put("PP Up", new PPUp());
		map.put("Rare Candy", new RareCandy());
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
		map.put("Roseli Berry", new RoseliBerry());
		map.put("Liechi Berry", new LiechiBerry());
		map.put("Ganlon Berry", new GanlonBerry());
		map.put("Salac Berry", new SalacBerry());
		map.put("Petaya Berry", new PetayaBerry());
		map.put("Apicot Berry", new ApicotBerry());
		map.put("Micle Berry", new MicleBerry());
		map.put("Kee Berry", new KeeBerry());
		map.put("Maranga Berry", new MarangaBerry());
		map.put("Jaboca Berry", new JabocaBerry());
		map.put("Rowap Berry", new RowapBerry());
		map.put("Custap Berry", new CustapBerry());
		map.put("Enigma Berry", new EnigmaBerry());
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
		map.put("Ability Capsule", new AbilityCapsule());
		map.put("Assault Vest", new AssaultVest());
		map.put("Power Herb", new PowerHerb());
		map.put("Hone Claws TM", new HoneClawsTM());
		map.put("Dragon Claw TM", new DragonClawTM());
		map.put("Psyshock TM", new PsyshockTM());
		map.put("Calm Mind TM", new CalmMindTM());
		map.put("Roar TM", new RoarTM());
		map.put("Toxic TM", new ToxicTM());
		map.put("Hail TM", new HailTM());
		map.put("Bulk Up TM", new BulkUpTM());
		map.put("Venoshock TM", new VenoshockTM());
		map.put("Hidden Power TM", new HiddenPowerTM());
		map.put("Sunny Day TM", new SunnyDayTM());
		map.put("Taunt TM", new TauntTM());
		map.put("Ice Beam TM", new IceBeamTM());
		map.put("Blizzard TM", new BlizzardTM());
		map.put("Hyper Beam TM", new HyperBeamTM());
		map.put("Light Screen TM", new LightScreenTM());
		map.put("Protect TM", new ProtectTM());
		map.put("Rain Dance TM", new RainDanceTM());
		map.put("Roost TM", new RoostTM());
		map.put("Safeguard TM", new SafeguardTM());
		map.put("Solar Beam TM", new SolarBeamTM());
		map.put("Smack Down TM", new SmackDownTM());
		map.put("Thunderbolt TM", new ThunderboltTM());
		map.put("Thunder TM", new ThunderTM());
		map.put("Earthquake TM", new EarthquakeTM());
		map.put("Dig TM", new DigTM());
		map.put("Psychic TM", new PsychicTM());
		map.put("Shadow Ball TM", new ShadowBallTM());
		map.put("Brick Break TM", new BrickBreakTM());
		map.put("Double Team TM", new DoubleTeamTM());
		map.put("Reflect TM", new ReflectTM());
		map.put("Sludge Wave TM", new SludgeWaveTM());
		map.put("Flamethrower TM", new FlamethrowerTM());
		map.put("Sludge Bomb TM", new SludgeBombTM());
		map.put("Sandstorm TM", new SandstormTM());
		map.put("Fire Blast TM", new FireBlastTM());
		map.put("Rock Tomb TM", new RockTombTM());
		map.put("Aerial Ace TM", new AerialAceTM());
		map.put("Torment TM", new TormentTM());
		map.put("Facade TM", new FacadeTM());
		map.put("Flame Charge TM", new FlameChargeTM());
		map.put("Rest TM", new RestTM());
		map.put("Attract TM", new AttractTM());
		map.put("Thief TM", new ThiefTM());
		map.put("Low Sweep TM", new LowSweepTM());
		map.put("Round TM", new RoundTM());
		map.put("Echoed Voice TM", new EchoedVoiceTM());
		map.put("Overheat TM", new OverheatTM());
		map.put("Steel Wing TM", new SteelWingTM());
		map.put("Focus Blast TM", new FocusBlastTM());
		map.put("Energy Ball TM", new EnergyBallTM());
		map.put("False Swipe TM", new FalseSwipeTM());
		map.put("Scald TM", new ScaldTM());
		map.put("Fling TM", new FlingTM());
		map.put("Charge Beam TM", new ChargeBeamTM());
		map.put("Sky Drop TM", new SkyDropTM());
		map.put("Incinerate TM", new IncinerateTM());
		map.put("Will-O-Wisp TM", new WillOWispTM());
		map.put("Acrobatics TM", new AcrobaticsTM());
		map.put("Embargo TM", new EmbargoTM());
		map.put("Explosion TM", new ExplosionTM());
		map.put("Shadow Claw TM", new ShadowClawTM());
		map.put("Payback TM", new PaybackTM());
		map.put("Retaliate TM", new RetaliateTM());
		map.put("Giga Impact TM", new GigaImpactTM());
		map.put("Rock Polish TM", new RockPolishTM());
		map.put("Flash TM", new FlashTM());
		map.put("Stone Edge TM", new StoneEdgeTM());
		map.put("Volt Switch TM", new VoltSwitchTM());
		map.put("Thunder Wave TM", new ThunderWaveTM());
		map.put("Gyro Ball TM", new GyroBallTM());
		map.put("Swords Dance TM", new SwordsDanceTM());
		map.put("Struggle Bug TM", new StruggleBugTM());
		map.put("Psych Up TM", new PsychUpTM());
		map.put("Bulldoze TM", new BulldozeTM());
		map.put("Frost Breath TM", new FrostBreathTM());
		map.put("Rock Slide TM", new RockSlideTM());
		map.put("X-Scissor TM", new XScissorTM());
		map.put("Dragon Tail TM", new DragonTailTM());
		map.put("Infestation TM", new InfestationTM());
		map.put("Poison Jab TM", new PoisonJabTM());
		map.put("Dream Eater TM", new DreamEaterTM());
		map.put("Grass Knot TM", new GrassKnotTM());
		map.put("Swagger TM", new SwaggerTM());
		map.put("Sleep Talk TM", new SleepTalkTM());
		map.put("U-turn TM", new UTurnTM());
		map.put("Substitute TM", new SubstituteTM());
		map.put("Flash Cannon TM", new FlashCannonTM());
		map.put("Trick Room TM", new TrickRoomTM());
		map.put("Wild Charge TM", new WildChargeTM());
		map.put("Rock Smash TM", new RockSmashTM());
		map.put("Snarl TM", new SnarlTM());
		map.put("Nature Power TM", new NaturePowerTM());
		map.put("Dark Pulse TM", new DarkPulseTM());
		map.put("Power-Up Punch TM", new PowerUpPunchTM());
		map.put("Dazzling Gleam TM", new DazzlingGleamTM());
		map.put("Confide TM", new ConfideTM());
		map.put("Cut TM", new CutTM());
		map.put("Fly TM", new FlyTM());
		map.put("Surf TM", new SurfTM());
		map.put("Strength TM", new StrengthTM());
		map.put("Waterfall TM", new WaterfallTM());

		processIncenseItems();
	}

	/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

	private static class NoItem extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		NoItem() {
			super(ItemNamesies.NO_ITEM, "YOU SHUOLDN'T SEE THIS", BagCategory.MISC, 0);
			super.price = -1;
		}

		public int flingDamage() {
			return 9001;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class Syrup extends Item implements TrainerUseItem {
		private static final long serialVersionUID = 1L;

		Syrup() {
			super(ItemNamesies.SYRUP, "A mysterious bottle of syrup. Maybe it will be useful some day.", BagCategory.KEY_ITEM, 1);
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

		Bicycle() {
			super(ItemNamesies.BICYCLE, "A folding Bicycle that enables much faster movement than the Running Shoes.", BagCategory.KEY_ITEM, 2);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public boolean use(Trainer t) {
			// TODO: if (Can ride bike) Set the bike as a 'currentlyUsing' item
			// May need to make this take in info on the route
			return false;
		}
	}

	private static class Surfboard extends Item implements TrainerUseItem {
		private static final long serialVersionUID = 1L;

		Surfboard() {
			super(ItemNamesies.SURFBOARD, "A fancy shmancy surfboard that lets you be RADICAL DUDE!", BagCategory.KEY_ITEM, 3);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public boolean use(Trainer t) {
			// TODO: DOESN'T DO SHIT
			return false;
		}
	}

	private static class FishingRod extends Item implements TrainerUseItem {
		private static final long serialVersionUID = 1L;

		FishingRod() {
			super(ItemNamesies.FISHING_ROD, "A multi-purpose, do-it-all kind of fishing rod. The kind you can use wherever you want. Except on land.", BagCategory.KEY_ITEM, 4);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "Oh! A bite!";
		}

		public boolean use(Trainer t) {
			// TODO: if (spot in front of player is a fishing spot) Set as 'currentlyUsing'
			// May need to make this take in info on the route
			return false;
		}
	}

	private static class AbsorbBulb extends Item implements HoldItem, ConsumableItem, TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		AbsorbBulb() {
			super(ItemNamesies.ABSORB_BULB, "A consumable bulb. If the holder is hit by a Water-type move, its Sp. Atk will rise.", BagCategory.MISC, 5);
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.WATER && victim.getAttributes().modifyStage(victim, victim, 1, Stat.SP_ATTACK, b, CastSource.HELD_ITEM)) {
				victim.consumeItem(b);
			}
		}
	}

	private static class AirBalloon extends Item implements HoldItem, ConsumableItem, LevitationEffect, TakeDamageEffect, EntryEffect {
		private static final long serialVersionUID = 1L;

		AirBalloon() {
			super(ItemNamesies.AIR_BALLOON, "When held by a Pok\u00e9mon, the Pok\u00e9mon will float into the air. When the holder is attacked, this item will burst.", BagCategory.MISC, 6);
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public void fall(Battle b, ActivePokemon fallen) {
			b.addMessage(fallen.getName() + " is no longer floating with its " + this.name + "!");
			
			// TODO: Fix this it's broken
			// Effect.removeEffect(fallen.getEffects(), this.namesies());
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			b.addMessage(victim.getName() + "'s " + this.name + " popped!");
			victim.consumeItem(b);
		}

		public void enter(Battle b, ActivePokemon victim) {
			b.addMessage(victim.getName() + " floats with its " + this.name + "!");
		}
	}

	private static class AmuletCoin extends Item implements HoldItem, EntryEffect {
		private static final long serialVersionUID = 1L;

		AmuletCoin() {
			super(ItemNamesies.AMULET_COIN, "An item to be held by a Pok\u00e9mon. It doubles a battle's prize money if the holding Pok\u00e9mon joins in.", BagCategory.MISC, 7);
			super.price = 100;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public void enter(Battle b, ActivePokemon victim) {
			TeamEffect.getEffect(EffectNamesies.GET_DAT_CASH_MONEY_TWICE).cast(b, victim, victim, CastSource.HELD_ITEM, false);
		}
	}

	private static class BigRoot extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		BigRoot() {
			super(ItemNamesies.BIG_ROOT, "A Pok\u00e9mon held item that boosts the power of HP-stealing moves to let the holder recover more HP.", BagCategory.MISC, 8);
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class BindingBand extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		BindingBand() {
			super(ItemNamesies.BINDING_BAND, "This item, when attached to a Pok\u00e9mon, increases damage caused by moves that constrict the opponent.", BagCategory.MISC, 9);
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class BlackSludge extends Item implements HoldItem, EndTurnEffect {
		private static final long serialVersionUID = 1L;

		BlackSludge() {
			super(ItemNamesies.BLACK_SLUDGE, "A held item that gradually restores the HP of Poison-type Pok\u00e9mon. It inflicts damage on all other types.", BagCategory.MISC, 10);
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.isType(b, Type.POISON)) {
				// Don't heal if at full health
				if (victim.fullHealth()) {
					return;
				}
				
				victim.healHealthFraction(1/16.0);
				b.addMessage(victim.getName() + "'s HP was restored by its " + this.name + "!", victim);
			}
			else if (!victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				b.addMessage(victim.getName() + " lost some of its HP due to its " + this.name + "!");
				victim.reduceHealthFraction(b, 1/8.0);
			}
		}
	}

	private static class BrightPowder extends Item implements HoldItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		BrightPowder() {
			super(ItemNamesies.BRIGHT_POWDER, "An item to be held by a Pok\u00e9mon. It casts a tricky glare that lowers the opponent's accuracy.", BagCategory.MISC, 11);
			super.price = 100;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.EVASION;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && true) {
				stat *= 1.1;
			}
			
			return stat;
		}
	}

	private static class CellBattery extends Item implements HoldItem, ConsumableItem, TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		CellBattery() {
			super(ItemNamesies.CELL_BATTERY, "A consumable battery. If the holder is hit by an Electric-type move, its Attack will rise.", BagCategory.MISC, 12);
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.ELECTRIC && victim.getAttributes().modifyStage(victim, victim, 1, Stat.ATTACK, b, CastSource.HELD_ITEM)) {
				victim.consumeItem(b);
			}
		}
	}

	private static class ChoiceBand extends Item implements AttackSelectionEffect, HoldItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		ChoiceBand() {
			super(ItemNamesies.CHOICE_BAND, "An item to be held by a Pok\u00e9mon. This headband ups Attack, but allows the use of only one of its moves.", BagCategory.MISC, 13);
			super.price = 100;
		}

		public boolean usable(ActivePokemon p, Move m) {
			Move last = p.getAttributes().getLastMoveUsed();
			if (last == null || m == last) {
				return true;
			}
			
			return false;
		}

		public String getUnusableMessage(ActivePokemon p) {
			return p.getName() + "'s " + super.name + " only allows " + p.getAttributes().getLastMoveUsed().getAttack().getName() + " to be used!";
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.ATTACK;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && true) {
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class ChoiceScarf extends Item implements AttackSelectionEffect, HoldItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		ChoiceScarf() {
			super(ItemNamesies.CHOICE_SCARF, "An item to be held by a Pok\u00e9mon. This scarf boosts Speed, but allows the use of only one of its moves.", BagCategory.MISC, 14);
			super.price = 200;
		}

		public boolean usable(ActivePokemon p, Move m) {
			Move last = p.getAttributes().getLastMoveUsed();
			if (last == null || m == last) {
				return true;
			}
			
			return false;
		}

		public String getUnusableMessage(ActivePokemon p) {
			return p.getName() + "'s " + super.name + " only allows " + p.getAttributes().getLastMoveUsed().getAttack().getName() + " to be used!";
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SPEED;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && true) {
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class ChoiceSpecs extends Item implements AttackSelectionEffect, HoldItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		ChoiceSpecs() {
			super(ItemNamesies.CHOICE_SPECS, "An item to be held by a Pok\u00e9mon. These distinctive glasses boost Sp. Atk but allow the use of only one of its moves.", BagCategory.MISC, 15);
			super.price = 200;
		}

		public boolean usable(ActivePokemon p, Move m) {
			Move last = p.getAttributes().getLastMoveUsed();
			if (last == null || m == last) {
				return true;
			}
			
			return false;
		}

		public String getUnusableMessage(ActivePokemon p) {
			return p.getName() + "'s " + super.name + " only allows " + p.getAttributes().getLastMoveUsed().getAttack().getName() + " to be used!";
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SP_ATTACK;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && true) {
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class CleanseTag extends Item implements HoldItem, RepellingEffect {
		private static final long serialVersionUID = 1L;

		CleanseTag() {
			super(ItemNamesies.CLEANSE_TAG, "An item to be held by a Pok\u00e9mon. It helps keep wild Pok\u00e9mon away if the holder is the first one in the party.", BagCategory.MISC, 16);
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double chance() {
			return .33;
		}
	}

	private static class DampRock extends Item implements HoldItem, WeatherExtendingEffect {
		private static final long serialVersionUID = 1L;

		DampRock() {
			super(ItemNamesies.DAMP_ROCK, "A Pok\u00e9mon held item that extends the duration of the move Rain Dance used by the holder.", BagCategory.MISC, 17);
			super.price = 200;
		}

		public int flingDamage() {
			return 60;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public EffectNamesies getWeatherType() {
			return EffectNamesies.RAINING;
		}
	}

	private static class HeatRock extends Item implements HoldItem, WeatherExtendingEffect {
		private static final long serialVersionUID = 1L;

		HeatRock() {
			super(ItemNamesies.HEAT_ROCK, "A Pok\u00e9mon held item that extends the duration of the move Sunny Day used by the holder.", BagCategory.MISC, 18);
			super.price = 200;
		}

		public int flingDamage() {
			return 60;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public EffectNamesies getWeatherType() {
			return EffectNamesies.SUNNY;
		}
	}

	private static class IcyRock extends Item implements HoldItem, WeatherExtendingEffect {
		private static final long serialVersionUID = 1L;

		IcyRock() {
			super(ItemNamesies.ICY_ROCK, "A Pok\u00e9mon held item that extends the duration of the move Hail used by the holder.", BagCategory.MISC, 19);
			super.price = 200;
		}

		public int flingDamage() {
			return 40;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public EffectNamesies getWeatherType() {
			return EffectNamesies.HAILING;
		}
	}

	private static class SmoothRock extends Item implements HoldItem, WeatherExtendingEffect {
		private static final long serialVersionUID = 1L;

		SmoothRock() {
			super(ItemNamesies.SMOOTH_ROCK, "A Pok\u00e9mon held item that extends the duration of the move Sandstorm used by the holder.", BagCategory.MISC, 20);
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public EffectNamesies getWeatherType() {
			return EffectNamesies.SANDSTORM;
		}
	}

	private static class EjectButton extends Item implements HoldItem, TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		EjectButton() {
			super(ItemNamesies.EJECT_BUTTON, "If the holder is hit by an attack, it will switch with another Pok\u00e9mon in your party.", BagCategory.MISC, 21);
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			// TODO: Try to generalize with self-switching moves
			Team t = b.getTrainer(victim.user());
			if (t instanceof WildPokemon) {
				return;
			}
			
			Trainer trainer = (Trainer)t;
			if (!trainer.hasRemainingPokemon()) {
				return;
			}
			
			b.addMessage(victim.getName() + "'s " + this.name + " sent it back to " + trainer.getName() + "!");
			victim.consumeItem(b);
			trainer.switchToRandom();
			trainer.setAction(Action.SWITCH);
			victim = trainer.front();
			b.enterBattle(victim, victim.getName() + " was sent out!");
		}
	}

	private static class DestinyKnot extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		DestinyKnot() {
			super(ItemNamesies.DESTINY_KNOT, "A long, thin, bright-red string to be held by a Pok\u00e9mon. If the holder becomes infatuated, the foe does too.", BagCategory.MISC, 22);
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class ExpertBelt extends Item implements HoldItem, PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		ExpertBelt() {
			super(ItemNamesies.EXPERT_BELT, "An item to be held by a Pok\u00e9mon. It is a well-worn belt that slightly boosts the power of supereffective moves.", BagCategory.MISC, 23);
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return Type.getAdvantage(user, victim, b) > 1 ? 1.2 : 1;
		}
	}

	private static class FlameOrb extends Item implements HoldItem, EndTurnEffect {
		private static final long serialVersionUID = 1L;

		FlameOrb() {
			super(ItemNamesies.FLAME_ORB, "An item to be held by a Pok\u00e9mon. It is a bizarre orb that inflicts a burn on the holder in battle.", BagCategory.MISC, 24);
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			Status.giveStatus(b, pelted, pelted, StatusCondition.BURNED, pelted.getName() + " was burned by the " + this.name + "!");
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			Status.giveStatus(b, victim, victim, StatusCondition.BURNED, victim.getName() + " was burned by its " + this.name + "!");
		}
	}

	private static class ToxicOrb extends Item implements HoldItem, EndTurnEffect {
		private static final long serialVersionUID = 1L;

		ToxicOrb() {
			super(ItemNamesies.TOXIC_ORB, "An item to be held by a Pok\u00e9mon. It is a bizarre orb that inflicts a burn on the holder in battle.", BagCategory.MISC, 25);
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			// Badly poisons the pelted
			applyEndTurn(pelted, b);
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			// Badly poisons the holder at the end of the turn
			if (Status.applies(StatusCondition.POISONED, b, victim, victim)) {
				victim.addEffect(PokemonEffect.getEffect(EffectNamesies.BAD_POISON).newInstance());
				Status.giveStatus(b, victim, victim, StatusCondition.POISONED, victim.getName() + " was badly poisoned by its " + this.name + "!");
			}
		}
	}

	private static class FloatStone extends Item implements HoldItem, HalfWeightEffect {
		private static final long serialVersionUID = 1L;

		FloatStone() {
			super(ItemNamesies.FLOAT_STONE, "This item, when attached to a Pok\u00e9mon, halves the Pok\u00e9mon's weight for use with attacks that deal with weight", BagCategory.MISC, 26);
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public int getHalfAmount(Integer halfAmount) {
			return halfAmount + 1;
		}
	}

	private static class FocusBand extends Item implements HoldItem, BracingEffect {
		private static final long serialVersionUID = 1L;

		FocusBand() {
			super(ItemNamesies.FOCUS_BAND, "An item to be held by a Pok\u00e9mon. The holder may endure a potential KO attack, leaving it with just 1 HP.", BagCategory.MISC, 27);
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean isBracing(Battle b, ActivePokemon bracer, Boolean fullHealth) {
			return Math.random()*100 < 10;
		}

		public String braceMessage(ActivePokemon bracer) {
			return bracer.getName() + " held on with its " + this.name + "!";
		}
	}

	private static class FocusSash extends Item implements HoldItem, ConsumableItem, BracingEffect {
		private static final long serialVersionUID = 1L;

		FocusSash() {
			super(ItemNamesies.FOCUS_SASH, "An item to be held by a Pok\u00e9mon. If it has full HP, the holder will endure one potential KO attack, leaving 1 HP.", BagCategory.MISC, 28);
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean isBracing(Battle b, ActivePokemon bracer, Boolean fullHealth) {
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

		GripClaw() {
			super(ItemNamesies.GRIP_CLAW, "A Pok\u00e9mon held item that extends the duration of multiturn attacks like Bind and Wrap.", BagCategory.MISC, 29);
			super.price = 200;
		}

		public int flingDamage() {
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class AdamantOrb extends Item implements HoldItem, PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		AdamantOrb() {
			super(ItemNamesies.ADAMANT_ORB, "A brightly gleaming orb to be held by Dialga. It boosts the power of Dragon- and Steel-type moves.", BagCategory.MISC, 30);
			super.price = 10000;
		}

		public boolean canUseOrb(ActivePokemon user) {
			if (!user.isPokemon(PokemonNamesies.DIALGA))
			{
				return false;
			}
			
			return user.isAttackType(Type.DRAGON) || user.isAttackType(Type.STEEL);
		}

		public int flingDamage() {
			return 60;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (canUseOrb(user))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class LustrousOrb extends Item implements HoldItem, PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		LustrousOrb() {
			super(ItemNamesies.LUSTROUS_ORB, "A beautifully glowing orb to be held by Palkia. It boosts the power of Dragon- and Water-type moves.", BagCategory.MISC, 31);
			super.price = 10000;
		}

		public boolean canUseOrb(ActivePokemon user) {
			if (!user.isPokemon(PokemonNamesies.PALKIA))
			{
				return false;
			}
			
			return user.isAttackType(Type.DRAGON) || user.isAttackType(Type.WATER);
		}

		public int flingDamage() {
			return 60;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (canUseOrb(user))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class GriseousOrb extends Item implements HoldItem, PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		GriseousOrb() {
			super(ItemNamesies.GRISEOUS_ORB, "A glowing orb to be held by Giratina. It boosts the power of Dragon- and Ghost-type moves.", BagCategory.MISC, 32);
			super.price = 10000;
		}

		public boolean canUseOrb(ActivePokemon user) {
			if (!user.isPokemon(PokemonNamesies.GIRATINA))
			{
				return false;
			}
			
			return user.isAttackType(Type.DRAGON) || user.isAttackType(Type.GHOST);
		}

		public int flingDamage() {
			return 60;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (canUseOrb(user))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class IronBall extends Item implements HoldItem, GroundedEffect, StatChangingEffect, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		IronBall() {
			super(ItemNamesies.IRON_BALL, "A Pok\u00e9mon held item that cuts Speed. It makes Flying-type and levitating holders susceptible to Ground moves.", BagCategory.MISC, 33);
			super.price = 200;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SPEED;
		}

		public int flingDamage() {
			return 130;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			// Technically the Iron Ball doesn't do this as a fling effect, but it almost makes sense so I'm doing it
			removeLevitation(b, pelted);
		}

		private void removeLevitation(Battle b, ActivePokemon p) {
			if (p.isSemiInvulnerableFlying()) {
				p.getMove().switchReady(b, p);
				b.addMessage(p.getName() + " fell to the ground!");
			}
			
			Battle.invoke(b.getEffectsList(p), LevitationEffect.class, "fall", b, p);
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && true) {
				stat *= .5;
			}
			
			return stat;
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (p.getAttack().isMoveType(MoveType.AIRBORNE)) {
				b.printAttacking(p);
				b.addMessage(Effect.DEFAULT_FAIL_MESSAGE);
				return false;
			}
			
			return true;
		}
	}

	private static class LaggingTail extends Item implements HoldItem, StallingEffect {
		private static final long serialVersionUID = 1L;

		LaggingTail() {
			super(ItemNamesies.LAGGING_TAIL, "An item to be held by a Pok\u00e9mon. It is tremendously heavy and makes the holder move slower than usual.", BagCategory.MISC, 34);
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class LifeOrb extends Item implements HoldItem, PowerChangeEffect, ApplyDamageEffect {
		private static final long serialVersionUID = 1L;

		LifeOrb() {
			super(ItemNamesies.LIFE_ORB, "An item to be held by a Pok\u00e9mon. It boosts the power of moves, but at the cost of some HP on each hit.", BagCategory.MISC, 35);
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return 5324.0/4096.0;
		}

		public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
			if (user.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			b.addMessage(user.getName() + " was hurt by its " + this.name + "!");
			user.reduceHealthFraction(b, .1);
		}
	}

	private static class LightBall extends Item implements HoldItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		LightBall() {
			super(ItemNamesies.LIGHT_BALL, "An item to be held by Pikachu. It is a puzzling orb that raises the Attack and Sp. Atk stat.", BagCategory.MISC, 36);
			super.price = 100;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.ATTACK || s == Stat.SP_ATTACK;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			Status.giveStatus(b, pelted, pelted, StatusCondition.PARALYZED, pelted.getName() + " was paralyzed by the " + this.name + "!");
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && p.isPokemon(PokemonNamesies.PIKACHU)) {
				stat *= 2;
			}
			
			return stat;
		}
	}

	private static class LightClay extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		LightClay() {
			super(ItemNamesies.LIGHT_CLAY, "A Pok\u00e9mon held item that extends the duration of barrier moves like Light Screen and Reflect used by the holder.", BagCategory.MISC, 37);
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class LuckyEgg extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		LuckyEgg() {
			super(ItemNamesies.LUCKY_EGG, "An item to be held by a Pok\u00e9mon. It is an egg filled with happiness that earns extra Exp. Points in battle.", BagCategory.MISC, 38);
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class LuckyPunch extends Item implements HoldItem, CritStageEffect {
		private static final long serialVersionUID = 1L;

		LuckyPunch() {
			super(ItemNamesies.LUCKY_PUNCH, "An item to be held by Chansey. It is a pair of gloves that boosts Chansey's critical-hit ratio.", BagCategory.MISC, 39);
			super.price = 10;
		}

		public int flingDamage() {
			return 40;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public int increaseCritStage(Integer stage, ActivePokemon p) {
			if (p.isPokemon(PokemonNamesies.CHANSEY)) {
				return stage + 2;
			}
			
			return stage;
		}
	}

	private static class LuminousMoss extends Item implements HoldItem, ConsumableItem, TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		LuminousMoss() {
			super(ItemNamesies.LUMINOUS_MOSS, "If the holder is hit by an Water-type attack, the holder's Special Defense stat is increased one stage. The item is consumed.", BagCategory.MISC, 40);
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.WATER && victim.getAttributes().modifyStage(victim, victim, 1, Stat.SP_DEFENSE, b, CastSource.HELD_ITEM)) {
				victim.consumeItem(b);
			}
		}
	}

	private static class MachoBrace extends Item implements EVItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		MachoBrace() {
			super(ItemNamesies.MACHO_BRACE, "An item to be held by a Pok\u00e9mon. It is a stiff and heavy brace that promotes strong growth but lowers Speed.", BagCategory.MISC, 41);
			super.price = 3000;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SPEED;
		}

		public int[] getEVs(int[] vals) {
			for (int i = 0; i < vals.length; i++) {
				vals[i] *= 2;
			}
			
			return vals;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && true) {
				stat *= .5;
			}
			
			return stat;
		}

		public int flingDamage() {
			return 60;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class MentalHerb extends Item implements HoldItem, EndTurnEffect {
		private static final long serialVersionUID = 1L;
		// TODO: This should be an object array
		EffectNamesies[] effects = {
			EffectNamesies.INFATUATED,
			EffectNamesies.DISABLE,
			EffectNamesies.TAUNT,
			EffectNamesies.ENCORE,
			EffectNamesies.TORMENT,
			EffectNamesies.CONFUSION,
			EffectNamesies.HEAL_BLOCK
		};
		String[] messages = {"infatuated", "disabled", "under the effects of taunt", "under the effects of encore", "under the effects of torment", "confused", "under the effects of heal block"};

		MentalHerb() {
			super(ItemNamesies.MENTAL_HERB, "An item to be held by a Pok\u00e9mon. It snaps the holder out of infatuation. It can be used only once.", BagCategory.MISC, 42);
			super.price = 100;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			if (pelted.hasEffect(EffectNamesies.INFATUATED)) {
				pelted.getAttributes().removeEffect(EffectNamesies.INFATUATED);
				b.addMessage(pelted.getName() + " is no longer infatuated to to the " + this.name + "!");
			}
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			boolean used = false;
			for (int i = 0; i < effects.length; i++) {
				EffectNamesies s = effects[i];
				if (victim.hasEffect(s)) {
					used = true;
					victim.getAttributes().removeEffect(s);
					b.addMessage(victim.getName() + " is no longer " + messages[i] + " due to its " + this.name + "!");
				}
			}
			
			if (used) {
				victim.consumeItem(b);
			}
		}
	}

	private static class MetalPowder extends Item implements HoldItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		MetalPowder() {
			super(ItemNamesies.METAL_POWDER, "When this item is held by a Ditto, the holder's initial Defence & Special Defence stats are increased by 50%", BagCategory.MISC, 43);
			super.price = 10;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.DEFENSE || s == Stat.SP_DEFENSE;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && p.isPokemon(PokemonNamesies.DITTO)) {
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class Metronome extends Item implements HoldItem, PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Metronome() {
			super(ItemNamesies.METRONOME, "A Pok\u00e9mon held item that boosts a move used consecutively. Its effect is reset if another move is used.", BagCategory.MISC, 44);
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return Math.min(2, 1 + .2*(user.getAttributes().getCount() - 1));
		}
	}

	private static class MuscleBand extends Item implements HoldItem, PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		MuscleBand() {
			super(ItemNamesies.MUSCLE_BAND, "An item to be held by a Pok\u00e9mon. It is a headband that slightly boosts the power of physical moves.", BagCategory.MISC, 45);
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttack().getCategory() == MoveCategory.PHYSICAL ? 1.1 : 1;
		}
	}

	private static class PowerAnklet extends Item implements PowerItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		PowerAnklet() {
			super(ItemNamesies.POWER_ANKLET, "A Pok\u00e9mon held item that promotes Speed gain on leveling, but reduces the Speed stat.", BagCategory.MISC, 46);
			super.price = 3000;
		}

		public Stat powerStat() {
			return Stat.SPEED;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SPEED;
		}

		public int[] getEVs(int[] vals) {
			vals[powerStat().index()] += 4;
			return vals;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && true) {
				stat *= .5;
			}
			
			return stat;
		}

		public int flingDamage() {
			return 70;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class PowerBand extends Item implements PowerItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		PowerBand() {
			super(ItemNamesies.POWER_BAND, "A Pok\u00e9mon held item that promotes Sp. Def gain on leveling, but reduces the Speed stat.", BagCategory.MISC, 47);
			super.price = 3000;
		}

		public Stat powerStat() {
			return Stat.SP_DEFENSE;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SPEED;
		}

		public int[] getEVs(int[] vals) {
			vals[powerStat().index()] += 4;
			return vals;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && true) {
				stat *= .5;
			}
			
			return stat;
		}

		public int flingDamage() {
			return 70;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class PowerBelt extends Item implements PowerItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		PowerBelt() {
			super(ItemNamesies.POWER_BELT, "A Pok\u00e9mon held item that promotes Def gain on leveling, but reduces the Speed stat.", BagCategory.MISC, 48);
			super.price = 3000;
		}

		public Stat powerStat() {
			return Stat.DEFENSE;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SPEED;
		}

		public int[] getEVs(int[] vals) {
			vals[powerStat().index()] += 4;
			return vals;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && true) {
				stat *= .5;
			}
			
			return stat;
		}

		public int flingDamage() {
			return 70;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class PowerBracer extends Item implements PowerItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		PowerBracer() {
			super(ItemNamesies.POWER_BRACER, "A Pok\u00e9mon held item that promotes Att gain on leveling, but reduces the Speed stat.", BagCategory.MISC, 49);
			super.price = 3000;
		}

		public Stat powerStat() {
			return Stat.ATTACK;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SPEED;
		}

		public int[] getEVs(int[] vals) {
			vals[powerStat().index()] += 4;
			return vals;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && true) {
				stat *= .5;
			}
			
			return stat;
		}

		public int flingDamage() {
			return 70;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class PowerLens extends Item implements PowerItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		PowerLens() {
			super(ItemNamesies.POWER_LENS, "A Pok\u00e9mon held item that promotes Sp. Att gain on leveling, but reduces the Speed stat.", BagCategory.MISC, 50);
			super.price = 3000;
		}

		public Stat powerStat() {
			return Stat.SP_ATTACK;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SPEED;
		}

		public int[] getEVs(int[] vals) {
			vals[powerStat().index()] += 4;
			return vals;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && true) {
				stat *= .5;
			}
			
			return stat;
		}

		public int flingDamage() {
			return 70;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class PowerWeight extends Item implements PowerItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		PowerWeight() {
			super(ItemNamesies.POWER_WEIGHT, "A Pok\u00e9mon held item that promotes HP gain on leveling, but reduces the Speed stat.", BagCategory.MISC, 51);
			super.price = 3000;
		}

		public Stat powerStat() {
			return Stat.HP;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SPEED;
		}

		public int[] getEVs(int[] vals) {
			vals[powerStat().index()] += 4;
			return vals;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && true) {
				stat *= .5;
			}
			
			return stat;
		}

		public int flingDamage() {
			return 70;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class QuickClaw extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		QuickClaw() {
			super(ItemNamesies.QUICK_CLAW, "An item to be held by a Pok\u00e9mon. A light, sharp claw that lets the bearer move first occasionally.", BagCategory.MISC, 52);
			super.price = 100;
		}

		public int flingDamage() {
			return 80;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class QuickPowder extends Item implements HoldItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		QuickPowder() {
			super(ItemNamesies.QUICK_POWDER, "An item to be held by Ditto. Extremely fine yet hard, this odd powder boosts the Speed stat.", BagCategory.MISC, 53);
			super.price = 10;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SPEED;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && p.isPokemon(PokemonNamesies.DITTO)) {
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class RedCard extends Item implements HoldItem, TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		RedCard() {
			super(ItemNamesies.RED_CARD, "A card with a mysterious power. When the holder is struck by a foe, the attacker is removed from battle.", BagCategory.MISC, 54);
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			// TODO: Generalize this code with that of moves like U-Turn
			Team t = b.getTrainer(user.user());
			if (t instanceof WildPokemon) {
				return;
			}
			
			Trainer trainer = (Trainer)t;
			if (!trainer.hasRemainingPokemon()) {
				return;
			}
			
			b.addMessage(victim.getName() + "'s " + this.name + " sent " + user.getName() + " back to " + trainer.getName() + "!");
			victim.consumeItem(b);
			trainer.switchToRandom();
			trainer.setAction(Action.SWITCH);
			user = trainer.front();
			b.enterBattle(user, user.getName() + " was sent out!");
		}
	}

	private static class RingTarget extends Item implements HoldItem, AdvantageChanger {
		private static final long serialVersionUID = 1L;

		RingTarget() {
			super(ItemNamesies.RING_TARGET, "Moves that would otherwise have no effect will land on the Pok\u00e9mon that holds it.", BagCategory.MISC, 55);
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public Type[] getAdvantageChange(Type attacking, Type[] defending) {
			for (int i = 0; i < 2; i++) {
				if (Type.getBasicAdvantage(attacking, defending[i]) == 0) {
					defending[i] = Type.NO_TYPE;
				}
			}
			
			return defending;
		}
	}

	private static class RockyHelmet extends Item implements HoldItem, PhysicalContactEffect {
		private static final long serialVersionUID = 1L;

		RockyHelmet() {
			super(ItemNamesies.ROCKY_HELMET, "If the holder of this item takes damage, the attacker will also be damaged upon contact.", BagCategory.MISC, 56);
			super.price = 200;
		}

		public int flingDamage() {
			return 60;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			b.addMessage(user.getName() + " was hurt by " + victim.getName() + "'s " + this.name + "!");
			user.reduceHealthFraction(b, 1/8.0);
		}
	}

	private static class SafetyGoggles extends Item implements HoldItem, WeatherBlockerEffect, EffectBlockerEffect {
		private static final long serialVersionUID = 1L;

		SafetyGoggles() {
			super(ItemNamesies.SAFETY_GOGGLES, "An item to be held by a Pok\u00e9mon. These goggles protect the holder from both weather-related damage and powder.", BagCategory.MISC, 57);
			super.price = 200;
		}

		public String getPreventMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + this.getName() + " protects it from powder moves!";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean block(EffectNamesies weather) {
			return true;
		}

		public boolean validMove(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (!user.getAttack().isMoveType(MoveType.POWDER)) {
				return true;
			}
			
			if (user.getAttack().getCategory() == MoveCategory.STATUS) {
				b.addMessage(getPreventMessage(victim));
			}
			
			return false;
		}
	}

	private static class ScopeLens extends Item implements HoldItem, CritStageEffect {
		private static final long serialVersionUID = 1L;

		ScopeLens() {
			super(ItemNamesies.SCOPE_LENS, "An item to be held by a Pok\u00e9mon. It is a lens that boosts the holder's critical-hit ratio.", BagCategory.MISC, 58);
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public int increaseCritStage(Integer stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	private static class ShedShell extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		ShedShell() {
			super(ItemNamesies.SHED_SHELL, "A tough, discarded carapace to be held by a Pok\u00e9mon. It enables the holder to switch with a waiting Pok\u00e9mon in battle.", BagCategory.MISC, 59);
			super.price = 100;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class ShellBell extends Item implements HoldItem, ApplyDamageEffect {
		private static final long serialVersionUID = 1L;

		ShellBell() {
			super(ItemNamesies.SHELL_BELL, "An item to be held by a Pok\u00e9mon. The holder's HP is restored a little every time it inflicts damage.", BagCategory.MISC, 60);
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
			if (user.fullHealth()) {
				return;
			}
			
			user.heal((int)Math.ceil(damage/8.0));
			// TODO: This looks really bad when paired with Explosion
			b.addMessage(user.getName() + " restored some HP due to its " + this.name + "!", user);
		}
	}

	private static class SmokeBall extends Item implements HoldItem, DefiniteEscape {
		private static final long serialVersionUID = 1L;

		SmokeBall() {
			super(ItemNamesies.SMOKE_BALL, "An item to be held by a Pok\u00e9mon. It enables the holder to flee from any wild Pok\u00e9mon without fail.", BagCategory.MISC, 61);
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class Snowball extends Item implements HoldItem, ConsumableItem, TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		Snowball() {
			super(ItemNamesies.SNOWBALL, "An item to be held by a Pok\u00e9mon. It boosts Attack if hit with an Ice-type attack. It can only be used once.", BagCategory.MISC, 62);
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.ICE && victim.getAttributes().modifyStage(victim, victim, 1, Stat.ATTACK, b, CastSource.HELD_ITEM)) {
				victim.consumeItem(b);
			}
		}
	}

	private static class SoulDew extends Item implements HoldItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		SoulDew() {
			super(ItemNamesies.SOUL_DEW, "If the Soul Dew is attached to Latios or Latias, the holder's Special Attack and Special Defence is increased by 50%.", BagCategory.MISC, 63);
			super.price = 10;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SP_ATTACK || s == Stat.SP_DEFENSE;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && p.isPokemon(PokemonNamesies.LATIOS) || p.isPokemon(PokemonNamesies.LATIAS)) {
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class Stick extends Item implements HoldItem, CritStageEffect {
		private static final long serialVersionUID = 1L;

		Stick() {
			super(ItemNamesies.STICK, "An item to be held by Farfetch'd. It is a very long and stiff stalk of leek that boosts the critical-hit ratio.", BagCategory.MISC, 64);
			super.price = 200;
		}

		public int flingDamage() {
			return 60;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public int increaseCritStage(Integer stage, ActivePokemon p) {
			if (p.isPokemon(PokemonNamesies.FARFETCHD)) {
				return stage + 2;
			}
			
			return stage;
		}
	}

	private static class StickyBarb extends Item implements HoldItem, EndTurnEffect, PhysicalContactEffect, ItemHolder {
		private static final long serialVersionUID = 1L;
		private Item item;

		StickyBarb() {
			super(ItemNamesies.STICKY_BARB, "A held item that damages the holder on every turn. It may latch on to foes and allies that touch the holder.", BagCategory.MISC, 65);
			super.price = 200;
		}

		public int flingDamage() {
			return 80;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			b.addMessage(victim.getName() + " was hurt by its " + this.name + "!");
			victim.reduceHealthFraction(b, 1/8.0);
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (!user.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				b.addMessage(user.getName() + " was hurt by " + victim.getName() + "'s " + this.name + "!");
				user.reduceHealthFraction(b, 1/8.0);
			}
			
			if (user.isHoldingItem(b) || user.isFainted(b)) {
				return;
			}
			
			b.addMessage(victim.getName() + "s " + this.name + " latched onto " + user.getName() + "!");
			
			if (b.isWildBattle()) {
				victim.removeItem();
				user.giveItem(this);
				return;
			}
			
			// TODO: Generalize this with other item stealing effects
			item = this;
			PokemonEffect.getEffect(EffectNamesies.CHANGE_ITEM).cast(b, victim, user, CastSource.HELD_ITEM, false);
			
			item = Item.noneItem();
			PokemonEffect.getEffect(EffectNamesies.CHANGE_ITEM).cast(b, victim, victim, CastSource.HELD_ITEM, false);
		}

		public Item getItem() {
			return item;
		}
	}

	private static class ThickClub extends Item implements HoldItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		ThickClub() {
			super(ItemNamesies.THICK_CLUB, "An item to be held by Cubone or Marowak. It is a hard bone of some sort that boosts the Attack stat.", BagCategory.MISC, 66);
			super.price = 500;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.ATTACK;
		}

		public int flingDamage() {
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && p.isPokemon(PokemonNamesies.CUBONE) || p.isPokemon(PokemonNamesies.MAROWAK)) {
				stat *= 2;
			}
			
			return stat;
		}
	}

	private static class WeaknessPolicy extends Item implements HoldItem, TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		WeaknessPolicy() {
			super(ItemNamesies.WEAKNESS_POLICY, "An item to be held by a Pok\u00e9mon. Attack and Sp. Atk sharply increase if the holder is hit with a move it's weak to.", BagCategory.MISC, 67);
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (Type.getAdvantage(user, victim, b) > 1) {
				victim.getAttributes().modifyStage(victim, victim, 2, Stat.ATTACK, b, CastSource.HELD_ITEM);
				victim.getAttributes().modifyStage(victim, victim, 2, Stat.SP_ATTACK, b, CastSource.HELD_ITEM);
			}
		}
	}

	private static class WhiteHerb extends Item implements HoldItem, StatProtectingEffect {
		private static final long serialVersionUID = 1L;

		WhiteHerb() {
			super(ItemNamesies.WHITE_HERB, "An item to be held by a Pok\u00e9mon. It restores any lowered stat in battle. It can be used only once.", BagCategory.MISC, 68);
			super.price = 100;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			// Restores negative stat changes to the pelted
			for (int i = 0; i < Stat.NUM_BATTLE_STATS; i++) {
				if (pelted.getStage(i) < 0) {
					pelted.getAttributes().setStage(i, 0);
				}
			}
			
			b.addMessage("The " + this.name + " restored " + pelted.getName() + "'s negative stat changes!");
		}

		public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
			// NOTE: Works like Clear Body, since ain't nobody want to keep track of stats.
			return true;
		}

		public String preventionMessage(ActivePokemon p, Stat s) {
			return p.getName() + "'s " + this.getName() + " prevents its " + s.getName().toLowerCase() + " from being lowered!";
		}
	}

	private static class WideLens extends Item implements HoldItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		WideLens() {
			super(ItemNamesies.WIDE_LENS, "An item to be held by a Pok\u00e9mon. It is a magnifying lens that slightly boosts the accuracy of moves.", BagCategory.MISC, 69);
			super.price = 200;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.ACCURACY;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && true) {
				stat *= 1.1;
			}
			
			return stat;
		}
	}

	private static class WiseGlasses extends Item implements HoldItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		WiseGlasses() {
			super(ItemNamesies.WISE_GLASSES, "An item to be held by a Pok\u00e9mon. It is a thick pair of glasses that slightly boosts the power of special moves.", BagCategory.MISC, 70);
			super.price = 200;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SP_ATTACK;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && true) {
				stat *= 1.1;
			}
			
			return stat;
		}
	}

	private static class ZoomLens extends Item implements HoldItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		ZoomLens() {
			super(ItemNamesies.ZOOM_LENS, "An item to be held by a Pok\u00e9mon. If the holder moves after its target, its accuracy will be boosted.", BagCategory.MISC, 71);
			super.price = 200;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.ACCURACY;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && !b.isFirstAttack()) {
				stat *= 1.2;
			}
			
			return stat;
		}
	}

	private static class FullIncense extends Item implements HoldItem, StallingEffect, IncenseItem {
		private static final long serialVersionUID = 1L;

		FullIncense() {
			super(ItemNamesies.FULL_INCENSE, "An item to be held by a Pok\u00e9mon. It is an exotic-smelling incense that makes the holder bloated and slow moving.", BagCategory.MISC, 72);
			super.price = 9600;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public PokemonNamesies getBaby() {
			return PokemonNamesies.MUNCHLAX;
		}
	}

	private static class LaxIncense extends Item implements HoldItem, IncenseItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		LaxIncense() {
			super(ItemNamesies.LAX_INCENSE, "An item to be held by a Pok\u00e9mon. The tricky aroma of this incense may make attacks miss the holder.", BagCategory.MISC, 73);
			super.price = 9600;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.EVASION;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public PokemonNamesies getBaby() {
			return PokemonNamesies.WYNAUT;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && true) {
				stat *= 1.1;
			}
			
			return stat;
		}
	}

	private static class LuckIncense extends Item implements HoldItem, EntryEffect, IncenseItem {
		private static final long serialVersionUID = 1L;

		LuckIncense() {
			super(ItemNamesies.LUCK_INCENSE, "An item to be held by a Pok\u00e9mon. It doubles a battle's prize money if the holding Pok\u00e9mon joins in.", BagCategory.MISC, 74);
			super.price = 9600;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public void enter(Battle b, ActivePokemon victim) {
			TeamEffect.getEffect(EffectNamesies.GET_DAT_CASH_MONEY_TWICE).cast(b, victim, victim, CastSource.HELD_ITEM, false);
		}

		public PokemonNamesies getBaby() {
			return PokemonNamesies.HAPPINY;
		}
	}

	private static class OddIncense extends Item implements IncenseItem, PowerChangeEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		OddIncense() {
			super(ItemNamesies.ODD_INCENSE, "An item to be held by a Pok\u00e9mon. It is an exotic-smelling incense that boosts the power of Psychic-type moves.", BagCategory.MISC, 75);
			super.price = 9600;
		}

		public PokemonNamesies getBaby() {
			return PokemonNamesies.MIME_JR;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.PSYCHIC)) {
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class PureIncense extends Item implements HoldItem, RepellingEffect, IncenseItem {
		private static final long serialVersionUID = 1L;

		PureIncense() {
			super(ItemNamesies.PURE_INCENSE, "An item to be held by a Pok\u00e9mon. It helps keep wild Pok\u00e9mon away if the holder is the first one in the party.", BagCategory.MISC, 76);
			super.price = 9600;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double chance() {
			return .33;
		}

		public PokemonNamesies getBaby() {
			return PokemonNamesies.CHINGLING;
		}
	}

	private static class RockIncense extends Item implements IncenseItem, PowerChangeEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		RockIncense() {
			super(ItemNamesies.ROCK_INCENSE, "An item to be held by a Pok\u00e9mon. It is an exotic-smelling incense that boosts the power of Rock-type moves.", BagCategory.MISC, 77);
			super.price = 9600;
		}

		public PokemonNamesies getBaby() {
			return PokemonNamesies.BONSLY;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.ROCK)) {
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class RoseIncense extends Item implements IncenseItem, PowerChangeEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		RoseIncense() {
			super(ItemNamesies.ROSE_INCENSE, "An item to be held by a Pok\u00e9mon. It is an exotic-smelling incense that boosts the power of Grass-type moves.", BagCategory.MISC, 78);
			super.price = 9600;
		}

		public PokemonNamesies getBaby() {
			return PokemonNamesies.BUDEW;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.GRASS)) {
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class SeaIncense extends Item implements IncenseItem, PowerChangeEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		SeaIncense() {
			super(ItemNamesies.SEA_INCENSE, "An item to be held by a Pok\u00e9mon. It is incense with a curious aroma that boosts the power of Water-type moves.", BagCategory.MISC, 79);
			super.price = 9600;
		}

		public PokemonNamesies getBaby() {
			return PokemonNamesies.AZURILL;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.WATER)) {
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class WaveIncense extends Item implements IncenseItem, PowerChangeEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		WaveIncense() {
			super(ItemNamesies.WAVE_INCENSE, "An item to be held by a Pok\u00e9mon. It is incense with a curious aroma that boosts the power of Water-type moves.", BagCategory.MISC, 80);
			super.price = 9600;
		}

		public PokemonNamesies getBaby() {
			return PokemonNamesies.MANTYKE;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.WATER)) {
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class DracoPlate extends Item implements PlateItem {
		private static final long serialVersionUID = 1L;

		DracoPlate() {
			super(ItemNamesies.DRACO_PLATE, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Dragon-type moves.", BagCategory.MISC, 81);
			super.price = 1000;
		}

		public Type getType() {
			return Type.DRAGON;
		}

		public int flingDamage() {
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class DreadPlate extends Item implements PlateItem {
		private static final long serialVersionUID = 1L;

		DreadPlate() {
			super(ItemNamesies.DREAD_PLATE, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Dark-type moves.", BagCategory.MISC, 82);
			super.price = 1000;
		}

		public Type getType() {
			return Type.DARK;
		}

		public int flingDamage() {
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class EarthPlate extends Item implements PlateItem {
		private static final long serialVersionUID = 1L;

		EarthPlate() {
			super(ItemNamesies.EARTH_PLATE, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Ground-type moves.", BagCategory.MISC, 83);
			super.price = 1000;
		}

		public Type getType() {
			return Type.GROUND;
		}

		public int flingDamage() {
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class FistPlate extends Item implements PlateItem {
		private static final long serialVersionUID = 1L;

		FistPlate() {
			super(ItemNamesies.FIST_PLATE, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Fighting-type moves.", BagCategory.MISC, 84);
			super.price = 1000;
		}

		public Type getType() {
			return Type.FIGHTING;
		}

		public int flingDamage() {
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class FlamePlate extends Item implements PlateItem {
		private static final long serialVersionUID = 1L;

		FlamePlate() {
			super(ItemNamesies.FLAME_PLATE, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Fire-type moves.", BagCategory.MISC, 85);
			super.price = 1000;
		}

		public Type getType() {
			return Type.FIRE;
		}

		public int flingDamage() {
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class IciclePlate extends Item implements PlateItem {
		private static final long serialVersionUID = 1L;

		IciclePlate() {
			super(ItemNamesies.ICICLE_PLATE, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Ice-type moves.", BagCategory.MISC, 86);
			super.price = 1000;
		}

		public Type getType() {
			return Type.ICE;
		}

		public int flingDamage() {
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class InsectPlate extends Item implements PlateItem {
		private static final long serialVersionUID = 1L;

		InsectPlate() {
			super(ItemNamesies.INSECT_PLATE, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Bug-type moves.", BagCategory.MISC, 87);
			super.price = 1000;
		}

		public Type getType() {
			return Type.BUG;
		}

		public int flingDamage() {
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class IronPlate extends Item implements PlateItem {
		private static final long serialVersionUID = 1L;

		IronPlate() {
			super(ItemNamesies.IRON_PLATE, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Steel-type moves.", BagCategory.MISC, 88);
			super.price = 1000;
		}

		public Type getType() {
			return Type.STEEL;
		}

		public int flingDamage() {
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class MeadowPlate extends Item implements PlateItem {
		private static final long serialVersionUID = 1L;

		MeadowPlate() {
			super(ItemNamesies.MEADOW_PLATE, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Grass-type moves.", BagCategory.MISC, 89);
			super.price = 1000;
		}

		public Type getType() {
			return Type.GRASS;
		}

		public int flingDamage() {
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class MindPlate extends Item implements PlateItem {
		private static final long serialVersionUID = 1L;

		MindPlate() {
			super(ItemNamesies.MIND_PLATE, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Psychic-type moves.", BagCategory.MISC, 90);
			super.price = 1000;
		}

		public Type getType() {
			return Type.PSYCHIC;
		}

		public int flingDamage() {
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class PixiePlate extends Item implements PlateItem {
		private static final long serialVersionUID = 1L;

		PixiePlate() {
			super(ItemNamesies.PIXIE_PLATE, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Fairy-type moves.", BagCategory.MISC, 91);
			super.price = 1000;
		}

		public Type getType() {
			return Type.FAIRY;
		}

		public int flingDamage() {
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class SkyPlate extends Item implements PlateItem {
		private static final long serialVersionUID = 1L;

		SkyPlate() {
			super(ItemNamesies.SKY_PLATE, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Flying-type moves.", BagCategory.MISC, 92);
			super.price = 1000;
		}

		public Type getType() {
			return Type.FLYING;
		}

		public int flingDamage() {
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class SplashPlate extends Item implements PlateItem {
		private static final long serialVersionUID = 1L;

		SplashPlate() {
			super(ItemNamesies.SPLASH_PLATE, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Water-type moves.", BagCategory.MISC, 93);
			super.price = 1000;
		}

		public Type getType() {
			return Type.WATER;
		}

		public int flingDamage() {
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class SpookyPlate extends Item implements PlateItem {
		private static final long serialVersionUID = 1L;

		SpookyPlate() {
			super(ItemNamesies.SPOOKY_PLATE, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Ghost-type moves.", BagCategory.MISC, 94);
			super.price = 1000;
		}

		public Type getType() {
			return Type.GHOST;
		}

		public int flingDamage() {
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class StonePlate extends Item implements PlateItem {
		private static final long serialVersionUID = 1L;

		StonePlate() {
			super(ItemNamesies.STONE_PLATE, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Rock-type moves.", BagCategory.MISC, 95);
			super.price = 1000;
		}

		public Type getType() {
			return Type.ROCK;
		}

		public int flingDamage() {
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class ToxicPlate extends Item implements PlateItem {
		private static final long serialVersionUID = 1L;

		ToxicPlate() {
			super(ItemNamesies.TOXIC_PLATE, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Poison-type moves.", BagCategory.MISC, 96);
			super.price = 1000;
		}

		public Type getType() {
			return Type.POISON;
		}

		public int flingDamage() {
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class ZapPlate extends Item implements PlateItem {
		private static final long serialVersionUID = 1L;

		ZapPlate() {
			super(ItemNamesies.ZAP_PLATE, "An item to be held by a Pok\u00e9mon. It is a stone tablet that boosts the power of Electric-type moves.", BagCategory.MISC, 97);
			super.price = 1000;
		}

		public Type getType() {
			return Type.ELECTRIC;
		}

		public int flingDamage() {
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType()))
			{
				return 1.2;
			}
			
			return 1;
		}
	}

	private static class BurnDrive extends Item implements DriveItem {
		private static final long serialVersionUID = 1L;

		BurnDrive() {
			super(ItemNamesies.BURN_DRIVE, "A cassette to be held by Genesect. It changes Techno Blast to a Fire-type move.", BagCategory.MISC, 98);
			super.price = 1000;
		}

		public Type getType() {
			return Type.FIRE;
		}

		public int flingDamage() {
			return 70;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class ChillDrive extends Item implements DriveItem {
		private static final long serialVersionUID = 1L;

		ChillDrive() {
			super(ItemNamesies.CHILL_DRIVE, "A cassette to be held by Genesect. It changes Techno Blast to an Ice-type move.", BagCategory.MISC, 99);
			super.price = 1000;
		}

		public Type getType() {
			return Type.ICE;
		}

		public int flingDamage() {
			return 70;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class DouseDrive extends Item implements DriveItem {
		private static final long serialVersionUID = 1L;

		DouseDrive() {
			super(ItemNamesies.DOUSE_DRIVE, "A cassette to be held by Genesect. It changes Techno Blast to a Water-type move.", BagCategory.MISC, 100);
			super.price = 1000;
		}

		public Type getType() {
			return Type.WATER;
		}

		public int flingDamage() {
			return 70;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class ShockDrive extends Item implements DriveItem {
		private static final long serialVersionUID = 1L;

		ShockDrive() {
			super(ItemNamesies.SHOCK_DRIVE, "A cassette to be held by Genesect. It changes Techno Blast to an Electric-type move.", BagCategory.MISC, 101);
			super.price = 1000;
		}

		public Type getType() {
			return Type.ELECTRIC;
		}

		public int flingDamage() {
			return 70;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class FireGem extends Item implements GemItem {
		private static final long serialVersionUID = 1L;

		FireGem() {
			super(ItemNamesies.FIRE_GEM, "A gem with an essence of fire. When held, it strengthens the power of a Fire-type move only once.", BagCategory.MISC, 102);
			super.price = 100;
		}

		public Type getType() {
			return Type.FIRE;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType())) {
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class WaterGem extends Item implements GemItem {
		private static final long serialVersionUID = 1L;

		WaterGem() {
			super(ItemNamesies.WATER_GEM, "A gem with an essence of water. When held, it strengthens the power of a Water-type move only once.", BagCategory.MISC, 103);
			super.price = 100;
		}

		public Type getType() {
			return Type.WATER;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType())) {
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class ElectricGem extends Item implements GemItem {
		private static final long serialVersionUID = 1L;

		ElectricGem() {
			super(ItemNamesies.ELECTRIC_GEM, "A gem with an essence of electricity. When held, it strengthens the power of an Electric-type move only once.", BagCategory.MISC, 104);
			super.price = 100;
		}

		public Type getType() {
			return Type.ELECTRIC;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType())) {
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class GrassGem extends Item implements GemItem {
		private static final long serialVersionUID = 1L;

		GrassGem() {
			super(ItemNamesies.GRASS_GEM, "A gem with an essence of nature. When held, it strengthens the power of a Grass-type move only once.", BagCategory.MISC, 105);
			super.price = 100;
		}

		public Type getType() {
			return Type.GRASS;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType())) {
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class IceGem extends Item implements GemItem {
		private static final long serialVersionUID = 1L;

		IceGem() {
			super(ItemNamesies.ICE_GEM, "A gem with an essence of ice. When held, it strengthens the power of an Ice-type move only once", BagCategory.MISC, 106);
			super.price = 100;
		}

		public Type getType() {
			return Type.ICE;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType())) {
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class FightingGem extends Item implements GemItem {
		private static final long serialVersionUID = 1L;

		FightingGem() {
			super(ItemNamesies.FIGHTING_GEM, "A gem with an essence of combat. When held, it strengthens the power of a Fighting-type move only once.", BagCategory.MISC, 107);
			super.price = 100;
		}

		public Type getType() {
			return Type.FIGHTING;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType())) {
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class PoisonGem extends Item implements GemItem {
		private static final long serialVersionUID = 1L;

		PoisonGem() {
			super(ItemNamesies.POISON_GEM, "A gem with an essence of poison. When held, it strengthens the power of a Poison-type move only once.", BagCategory.MISC, 108);
			super.price = 100;
		}

		public Type getType() {
			return Type.POISON;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType())) {
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class GroundGem extends Item implements GemItem {
		private static final long serialVersionUID = 1L;

		GroundGem() {
			super(ItemNamesies.GROUND_GEM, "A gem with an essence of land. When held, it strengthens the power of a Ground-type move only once.", BagCategory.MISC, 109);
			super.price = 100;
		}

		public Type getType() {
			return Type.GROUND;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType())) {
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class FlyingGem extends Item implements GemItem {
		private static final long serialVersionUID = 1L;

		FlyingGem() {
			super(ItemNamesies.FLYING_GEM, "A gem with an essence of air. When held, it strengthens the power of a Flying-type move only once.", BagCategory.MISC, 110);
			super.price = 100;
		}

		public Type getType() {
			return Type.FLYING;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType())) {
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class PsychicGem extends Item implements GemItem {
		private static final long serialVersionUID = 1L;

		PsychicGem() {
			super(ItemNamesies.PSYCHIC_GEM, "A gem with an essence of the mind. When held, it strengthens the power of a Psychic-type move only once.", BagCategory.MISC, 111);
			super.price = 100;
		}

		public Type getType() {
			return Type.PSYCHIC;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType())) {
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class BugGem extends Item implements GemItem {
		private static final long serialVersionUID = 1L;

		BugGem() {
			super(ItemNamesies.BUG_GEM, "A gem with an insect-like essence. When held, it strengthens the power of a Bug-type move only once.", BagCategory.MISC, 112);
			super.price = 100;
		}

		public Type getType() {
			return Type.BUG;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType())) {
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class RockGem extends Item implements GemItem {
		private static final long serialVersionUID = 1L;

		RockGem() {
			super(ItemNamesies.ROCK_GEM, "A gem with an essence of rock. When held, it strengthens the power of a Rock-type move only once.", BagCategory.MISC, 113);
			super.price = 100;
		}

		public Type getType() {
			return Type.ROCK;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType())) {
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class GhostGem extends Item implements GemItem {
		private static final long serialVersionUID = 1L;

		GhostGem() {
			super(ItemNamesies.GHOST_GEM, "A gem with a spectral essence. When held, it strengthens the power of a Ghost-type move only once.", BagCategory.MISC, 114);
			super.price = 100;
		}

		public Type getType() {
			return Type.GHOST;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType())) {
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class DragonGem extends Item implements GemItem {
		private static final long serialVersionUID = 1L;

		DragonGem() {
			super(ItemNamesies.DRAGON_GEM, "A gem with a draconic essence. When held, it strengthens the power of a Dragon-type move only once.", BagCategory.MISC, 115);
			super.price = 100;
		}

		public Type getType() {
			return Type.DRAGON;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType())) {
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class DarkGem extends Item implements GemItem {
		private static final long serialVersionUID = 1L;

		DarkGem() {
			super(ItemNamesies.DARK_GEM, "A gem with an essence of darkness. When held, it strengthens the power of a Dark-type move only once.", BagCategory.MISC, 116);
			super.price = 100;
		}

		public Type getType() {
			return Type.DARK;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType())) {
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class SteelGem extends Item implements GemItem {
		private static final long serialVersionUID = 1L;

		SteelGem() {
			super(ItemNamesies.STEEL_GEM, "A gem with an essence of steel. When held, it strengthens the power of a Steel-type move only once.", BagCategory.MISC, 117);
			super.price = 100;
		}

		public Type getType() {
			return Type.STEEL;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType())) {
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class NormalGem extends Item implements GemItem {
		private static final long serialVersionUID = 1L;

		NormalGem() {
			super(ItemNamesies.NORMAL_GEM, "A gem with an ordinary essence. When held, it strengthens the power of a Normal-type move only once.", BagCategory.MISC, 118);
			super.price = 100;
		}

		public Type getType() {
			return Type.NORMAL;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType())) {
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class FairyGem extends Item implements GemItem {
		private static final long serialVersionUID = 1L;

		FairyGem() {
			super(ItemNamesies.FAIRY_GEM, "A gem with an ordinary essence. When held, it strengthens the power of a Fairy-type move only once.", BagCategory.MISC, 119);
			super.price = 100;
		}

		public Type getType() {
			return Type.FAIRY;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(getType())) {
				// Consume the item
				b.addMessage(user.getName() + "'s " + this.getName() + " enhanced " + user.getAttack().getName() + "'s power!");
				user.consumeItem(b);
				
				// Gems increase the power of the move by 50% -- technically 30% in Gen 6 but they suck enough as is being a consumed item and all
				return 1.5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class Leftovers extends Item implements HoldItem, EndTurnEffect {
		private static final long serialVersionUID = 1L;

		Leftovers() {
			super(ItemNamesies.LEFTOVERS, "An item to be held by a Pok\u00e9mon. The holder's HP is gradually restored during battle.", BagCategory.MISC, 120);
			super.price = 200;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				return;
			}
			
			victim.healHealthFraction(1/16.0);
			b.addMessage(victim.getName() + "'s HP was restored by its " + this.name + "!", victim);
		}
	}

	private static class BlackBelt extends Item implements PowerChangeEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		BlackBelt() {
			super(ItemNamesies.BLACK_BELT, "An item to be held by a Pok\u00e9mon. It is a belt that boosts determination and Fighting-type moves.", BagCategory.MISC, 121);
			super.price = 9800;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.FIGHTING)) {
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class BlackGlasses extends Item implements PowerChangeEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		BlackGlasses() {
			super(ItemNamesies.BLACK_GLASSES, "An item to be held by a Pok\u00e9mon. It is a shady-looking pair of glasses that boosts Dark-type moves.", BagCategory.MISC, 122);
			super.price = 9800;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.DARK)) {
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class Charcoal extends Item implements PowerChangeEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		Charcoal() {
			super(ItemNamesies.CHARCOAL, "An item to be held by a Pok\u00e9mon. It is a combustible fuel that boosts the power of Fire-type moves.", BagCategory.MISC, 123);
			super.price = 9800;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.FIRE)) {
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class DragonFang extends Item implements PowerChangeEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		DragonFang() {
			super(ItemNamesies.DRAGON_FANG, "An item to be held by a Pok\u00e9mon. It is a hard and sharp fang that ups the power of Dragon-type moves.", BagCategory.MISC, 124);
			super.price = 9800;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.DRAGON)) {
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 70;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class HardStone extends Item implements PowerChangeEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		HardStone() {
			super(ItemNamesies.HARD_STONE, "An item to be held by a Pok\u00e9mon. It is an unbreakable stone that ups the power of Rock-type moves.", BagCategory.MISC, 125);
			super.price = 9800;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.ROCK)) {
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 100;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class Magnet extends Item implements PowerChangeEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		Magnet() {
			super(ItemNamesies.MAGNET, "An item to be held by a Pok\u00e9mon. It is a powerful magnet that boosts the power of Electric-type moves.", BagCategory.MISC, 126);
			super.price = 9800;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.ELECTRIC)) {
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class MetalCoat extends Item implements PowerChangeEffect, HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		MetalCoat() {
			super(ItemNamesies.METAL_COAT, "A mysterious substance full of a special filmy metal. It allows certain kinds of Pok\u00e9mon to evolve.", BagCategory.MISC, 127);
			super.price = 9800;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.STEEL)) {
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class MiracleSeed extends Item implements PowerChangeEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		MiracleSeed() {
			super(ItemNamesies.MIRACLE_SEED, "An item to be held by a Pok\u00e9mon. It is a seed imbued with life that ups the power of Grass-type moves.", BagCategory.MISC, 128);
			super.price = 9800;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.GRASS)) {
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class MysticWater extends Item implements PowerChangeEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		MysticWater() {
			super(ItemNamesies.MYSTIC_WATER, "An item to be held by a Pok\u00e9mon. It is a teardrop-shaped gem that ups the power of Water-type moves.", BagCategory.MISC, 129);
			super.price = 9800;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.WATER)) {
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class NeverMeltIce extends Item implements PowerChangeEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		NeverMeltIce() {
			super(ItemNamesies.NEVER_MELT_ICE, "An item to be held by a Pok\u00e9mon. It is a piece of ice that repels heat and boosts Ice-type moves.", BagCategory.MISC, 130);
			super.price = 9800;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.ICE)) {
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class PoisonBarb extends Item implements PowerChangeEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		PoisonBarb() {
			super(ItemNamesies.POISON_BARB, "An item to be held by a Pok\u00e9mon. It is a small, poisonous barb that ups the power of Poison-type moves.", BagCategory.MISC, 131);
			super.price = 9800;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.POISON)) {
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 70;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			Status.giveStatus(b, pelted, pelted, StatusCondition.POISONED, pelted.getName() + " was poisoned by the " + this.name + "!");
		}
	}

	private static class SharpBeak extends Item implements PowerChangeEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		SharpBeak() {
			super(ItemNamesies.SHARP_BEAK, "An item to be held by a Pok\u00e9mon. It is a long, sharp beak that boosts the power of Flying-type moves.", BagCategory.MISC, 132);
			super.price = 9800;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.FLYING)) {
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 50;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class SilkScarf extends Item implements PowerChangeEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		SilkScarf() {
			super(ItemNamesies.SILK_SCARF, "An item to be held by a Pok\u00e9mon. It is a sumptuous scarf that boosts the power of Normal-type moves.", BagCategory.MISC, 133);
			super.price = 9800;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.NORMAL)) {
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class SilverPowder extends Item implements PowerChangeEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		SilverPowder() {
			super(ItemNamesies.SILVER_POWDER, "An item to be held by a Pok\u00e9mon. It is a shiny, silver powder that ups the power of Bug-type moves.", BagCategory.MISC, 134);
			super.price = 9800;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.BUG)) {
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class SoftSand extends Item implements PowerChangeEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		SoftSand() {
			super(ItemNamesies.SOFT_SAND, "An item to be held by a Pok\u00e9mon. It is a loose, silky sand that boosts the power of Ground-type moves.", BagCategory.MISC, 135);
			super.price = 9800;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.GROUND)) {
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class SpellTag extends Item implements PowerChangeEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		SpellTag() {
			super(ItemNamesies.SPELL_TAG, "An item to be held by a Pok\u00e9mon. It is a sinister, eerie tag that boosts the power of Ghost-type moves.", BagCategory.MISC, 136);
			super.price = 9800;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.GHOST)) {
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class TwistedSpoon extends Item implements PowerChangeEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		TwistedSpoon() {
			super(ItemNamesies.TWISTED_SPOON, "An item to be held by a Pok\u00e9mon. It is a spoon imbued with telekinetic power that boosts Psychic-type moves.", BagCategory.MISC, 137);
			super.price = 9800;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.PSYCHIC)) {
				return 1.2;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class DawnStone extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		DawnStone() {
			super(ItemNamesies.DAWN_STONE, "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It sparkles like eyes.", BagCategory.MISC, 138);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 80;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class DeepSeaScale extends Item implements HoldItem, StatChangingEffect, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		DeepSeaScale() {
			super(ItemNamesies.DEEP_SEA_SCALE, "An item to be held by Clamperl, Chinchou, or Lanturn. A scale that shines a faint pink, it raises the Sp. Def stat.", BagCategory.MISC, 139);
			super.price = 200;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SP_DEFENSE;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && p.isPokemon(PokemonNamesies.CLAMPERL) || p.isPokemon(PokemonNamesies.CHINCHOU) || p.isPokemon(PokemonNamesies.LANTURN)) {
				stat *= 2;
			}
			
			return stat;
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class DeepSeaTooth extends Item implements HoldItem, StatChangingEffect, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		DeepSeaTooth() {
			super(ItemNamesies.DEEP_SEA_TOOTH, "An item to be held by Clamperl. A fang that gleams a sharp silver, it raises the Sp. Atk stat.", BagCategory.MISC, 140);
			super.price = 200;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SP_ATTACK;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 90;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && p.isPokemon(PokemonNamesies.CLAMPERL)) {
				stat *= 2;
			}
			
			return stat;
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class DragonScale extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		DragonScale() {
			super(ItemNamesies.DRAGON_SCALE, "A thick and tough scale. Dragon-type Pok\u00e9mon may be holding this item when caught.", BagCategory.MISC, 141);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class DubiousDisc extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		DubiousDisc() {
			super(ItemNamesies.DUBIOUS_DISC, "A transparent device overflowing with dubious data. Its producer is unknown.", BagCategory.MISC, 142);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 50;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class DuskStone extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		DuskStone() {
			super(ItemNamesies.DUSK_STONE, "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It is as dark as dark can be.", BagCategory.MISC, 143);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 80;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class Electirizer extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		Electirizer() {
			super(ItemNamesies.ELECTIRIZER, "A box packed with a tremendous amount of electric energy. It is loved by a certain Pok\u00e9mon.", BagCategory.MISC, 144);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 80;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class FireStone extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		FireStone() {
			super(ItemNamesies.FIRE_STONE, "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It is colored orange.", BagCategory.MISC, 145);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class KingsRock extends Item implements PokemonUseItem, ApplyDamageEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		KingsRock() {
			super(ItemNamesies.KINGS_ROCK, "An item to be held by a Pok\u00e9mon. When the holder inflicts damage, the target may flinch.", BagCategory.MISC, 146);
			super.price = 100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}

		public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
			if (Math.random()*100 < 10) {
				PokemonEffect flinch = PokemonEffect.getEffect(EffectNamesies.FLINCH);
				if (flinch.applies(b, user, victim, CastSource.HELD_ITEM)) {
					flinch.cast(b, user, victim, CastSource.HELD_ITEM, false);
					b.addMessage(user.getName() + "'s " + this.name + " caused " + victim.getName() + " to flinch!");
				}
			}
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			PokemonEffect flinch = PokemonEffect.getEffect(EffectNamesies.FLINCH);
			if (flinch.applies(b, pelted, pelted, CastSource.USE_ITEM)) {
				flinch.cast(b, pelted, pelted, CastSource.USE_ITEM, false);
				b.addMessage("The " + this.name + " caused " + pelted.getName() + " to flinch!");
			}
		}
	}

	private static class LeafStone extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		LeafStone() {
			super(ItemNamesies.LEAF_STONE, "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It has a leaf pattern.", BagCategory.MISC, 147);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class Magmarizer extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		Magmarizer() {
			super(ItemNamesies.MAGMARIZER, "A box packed with a tremendous amount of magma energy. It is loved by a certain Pok\u00e9mon.", BagCategory.MISC, 148);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 80;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class MoonStone extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		MoonStone() {
			super(ItemNamesies.MOON_STONE, "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It is as black as the night sky.", BagCategory.MISC, 149);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class OvalStone extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		OvalStone() {
			super(ItemNamesies.OVAL_STONE, "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It is shaped like an egg.", BagCategory.MISC, 150);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 80;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class Everstone extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		Everstone() {
			super(ItemNamesies.EVERSTONE, "An item to be held by a Pok\u00e9mon. The Pok\u00e9mon holding this peculiar stone is prevented from evolving.", BagCategory.MISC, 151);
			super.price = 200;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class PrismScale extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		PrismScale() {
			super(ItemNamesies.PRISM_SCALE, "A mysterious scale that evolves certain Pok\u00e9mon. It shines in rainbow colors.", BagCategory.MISC, 152);
			super.price = 500;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class Protector extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		Protector() {
			super(ItemNamesies.PROTECTOR, "A protective item of some sort. It is extremely stiff and heavy. It is loved by a certain Pok\u00e9mon.", BagCategory.MISC, 153);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 80;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class RazorClaw extends Item implements HoldItem, CritStageEffect, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		RazorClaw() {
			super(ItemNamesies.RAZOR_CLAW, "An item to be held by a Pok\u00e9mon. It is a sharply hooked claw that ups the holder's critical-hit ratio.", BagCategory.MISC, 154);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 80;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public int increaseCritStage(Integer stage, ActivePokemon p) {
			return stage + 1;
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class RazorFang extends Item implements PokemonUseItem, ApplyDamageEffect, HoldItem {
		private static final long serialVersionUID = 1L;

		RazorFang() {
			super(ItemNamesies.RAZOR_FANG, "An item to be held by a Pok\u00e9mon. It may make foes and allies flinch when the holder inflicts damage.`", BagCategory.MISC, 155);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}

		public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
			if (Math.random()*100 < 10) {
				PokemonEffect flinch = PokemonEffect.getEffect(EffectNamesies.FLINCH);
				if (flinch.applies(b, user, victim, CastSource.HELD_ITEM)) {
					flinch.cast(b, user, victim, CastSource.HELD_ITEM, false);
					b.addMessage(user.getName() + "'s " + this.name + " caused " + victim.getName() + " to flinch!");
				}
			}
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			PokemonEffect flinch = PokemonEffect.getEffect(EffectNamesies.FLINCH);
			if (flinch.applies(b, pelted, pelted, CastSource.USE_ITEM)) {
				flinch.cast(b, pelted, pelted, CastSource.USE_ITEM, false);
				b.addMessage("The " + this.name + " caused " + pelted.getName() + " to flinch!");
			}
		}
	}

	private static class ReaperCloth extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		ReaperCloth() {
			super(ItemNamesies.REAPER_CLOTH, "A cloth imbued with horrifyingly strong spiritual energy. It is loved by a certain Pok\u00e9mon.", BagCategory.MISC, 156);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class Sachet extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		Sachet() {
			super(ItemNamesies.SACHET, "A sachet filled with fragrant perfumes that are just slightly too overwhelming. Yet it's loved by a certain Pok\u00e9mon.", BagCategory.MISC, 157);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class ShinyStone extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		ShinyStone() {
			super(ItemNamesies.SHINY_STONE, "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It shines with a dazzling light.", BagCategory.MISC, 158);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 80;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class SunStone extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		SunStone() {
			super(ItemNamesies.SUN_STONE, "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It is as red as the sun.", BagCategory.MISC, 159);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class ThunderStone extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		ThunderStone() {
			super(ItemNamesies.THUNDER_STONE, "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It has a thunderbolt pattern.", BagCategory.MISC, 160);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class UpGrade extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		UpGrade() {
			super(ItemNamesies.UP_GRADE, "A transparent device filled with all sorts of data. It was produced by Silph Co.", BagCategory.MISC, 161);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class WaterStone extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		WaterStone() {
			super(ItemNamesies.WATER_STONE, "A peculiar stone that makes certain species of Pok\u00e9mon evolve. It is a clear, light blue.", BagCategory.MISC, 162);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class WhippedDream extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		WhippedDream() {
			super(ItemNamesies.WHIPPED_DREAM, "A soft and sweet treat made of fluffy, puffy, whipped and whirled cream. It is loved by a certain Pok\u00e9mon.", BagCategory.MISC, 163);
			super.price = 2100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Evolution ev = p.getPokemonInfo().getEvolution();
			BaseEvolution base = (BaseEvolution) ev.getEvolution(EvolutionCheck.ITEM, p, this.namesies);
			if (base == null) {
				return false;
			}
			
			player.setEvolution(p, base);
			return true;
		}
	}

	private static class Antidote extends Item implements HoldItem, PokemonUseItem, BattleUseItem {
		private static final long serialVersionUID = 1L;
		private String message;

		Antidote() {
			super(ItemNamesies.ANTIDOTE, "A spray-type medicine. It lifts the effect of poison from one Pok\u00e9mon.", BagCategory.MEDICINE, 164);
			super.price = 100;
			super.battleBagCategories.add(BattleBagCategory.STATUS);
		}

		public StatusCondition toRemove() {
			return StatusCondition.POISONED;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return message;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			if (!p.hasStatus(toRemove())) {
				return false;
			}
			
			message = Status.getRemoveStatus(null, p, CastSource.USE_ITEM);
			return true;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}
	}

	private static class Awakening extends Item implements HoldItem, PokemonUseItem, BattleUseItem {
		private static final long serialVersionUID = 1L;
		private String message;

		Awakening() {
			super(ItemNamesies.AWAKENING, "A spray-type medicine. It awakens a Pok\u00e9mon from the clutches of sleep.", BagCategory.MEDICINE, 165);
			super.price = 250;
			super.battleBagCategories.add(BattleBagCategory.STATUS);
		}

		public StatusCondition toRemove() {
			return StatusCondition.ASLEEP;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return message;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			if (!p.hasStatus(toRemove())) {
				return false;
			}
			
			message = Status.getRemoveStatus(null, p, CastSource.USE_ITEM);
			return true;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}
	}

	private static class BurnHeal extends Item implements HoldItem, PokemonUseItem, BattleUseItem {
		private static final long serialVersionUID = 1L;
		private String message;

		BurnHeal() {
			super(ItemNamesies.BURN_HEAL, "A spray-type medicine. It heals a single Pok\u00e9mon that is suffering from a burn.", BagCategory.MEDICINE, 166);
			super.price = 250;
			super.battleBagCategories.add(BattleBagCategory.STATUS);
		}

		public StatusCondition toRemove() {
			return StatusCondition.BURNED;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return message;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			if (!p.hasStatus(toRemove())) {
				return false;
			}
			
			message = Status.getRemoveStatus(null, p, CastSource.USE_ITEM);
			return true;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}
	}

	private static class IceHeal extends Item implements HoldItem, PokemonUseItem, BattleUseItem {
		private static final long serialVersionUID = 1L;
		private String message;

		IceHeal() {
			super(ItemNamesies.ICE_HEAL, "A spray-type medicine. It defrosts a Pok\u00e9mon that has been frozen solid.", BagCategory.MEDICINE, 167);
			super.price = 250;
			super.battleBagCategories.add(BattleBagCategory.STATUS);
		}

		public StatusCondition toRemove() {
			return StatusCondition.FROZEN;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return message;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			if (!p.hasStatus(toRemove())) {
				return false;
			}
			
			message = Status.getRemoveStatus(null, p, CastSource.USE_ITEM);
			return true;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}
	}

	private static class ParalyzeHeal extends Item implements HoldItem, PokemonUseItem, BattleUseItem {
		private static final long serialVersionUID = 1L;
		private String message;

		ParalyzeHeal() {
			super(ItemNamesies.PARALYZE_HEAL, "A spray-type medicine. It eliminates paralysis from a single Pok\u00e9mon.", BagCategory.MEDICINE, 168);
			super.price = 200;
			super.battleBagCategories.add(BattleBagCategory.STATUS);
		}

		public StatusCondition toRemove() {
			return StatusCondition.PARALYZED;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return message;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			if (!p.hasStatus(toRemove())) {
				return false;
			}
			
			message = Status.getRemoveStatus(null, p, CastSource.USE_ITEM);
			return true;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}
	}

	private static class FullHeal extends Item implements HoldItem, PokemonUseItem, BattleUseItem {
		private static final long serialVersionUID = 1L;

		FullHeal() {
			super(ItemNamesies.FULL_HEAL, "A spray-type medicine. It heals all the status problems of a single Pok\u00e9mon.", BagCategory.MEDICINE, 169);
			super.price = 250;
			super.battleBagCategories.add(BattleBagCategory.STATUS);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " was cured of its status condition!";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			// Does not apply to the dead
			if (p.hasStatus(StatusCondition.FAINTED)) {
				return false;
			}
			
			// YOU'RE FINE
			if (!p.hasStatus()) {
				return false;
			}
			
			p.removeStatus();
			return true;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}
	}

	private static class FullRestore extends Item implements PokemonUseItem, HoldItem, BattleUseItem {
		private static final long serialVersionUID = 1L;

		FullRestore() {
			super(ItemNamesies.FULL_RESTORE, "A medicine that fully restores the HP and heals any status problems of a single Pok\u00e9mon.", BagCategory.MEDICINE, 170);
			super.price = 3000;
			super.battleBagCategories.add(BattleBagCategory.HP_PP);
			super.battleBagCategories.add(BattleBagCategory.STATUS);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " was fully healed!";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			// Does not apply to the dead
			if (p.hasStatus(StatusCondition.FAINTED)) {
				return false;
			}
			
			// Does not apply to the fully healed -- status condition or otherwise
			if (!p.hasStatus() && p.fullHealth()) {
				return false;
			}
			
			p.removeStatus();
			p.healHealthFraction(1);
			return true;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}
	}

	private static class Elixir extends Item implements PokemonUseItem, BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;
		private boolean use(List<Move> moves)
		{
			boolean changed = false;
			for (Move m : moves)
			{
				changed |= m.increasePP(increaseAmount(m));
			}
			
			return changed;
		}

		Elixir() {
			super(ItemNamesies.ELIXIR, "It restores the PP of all the moves learned by the targeted Pok\u00e9mon by 10 points each.", BagCategory.MEDICINE, 171);
			super.price = 3000;
			super.battleBagCategories.add(BattleBagCategory.HP_PP);
		}

		public int increaseAmount(Move m) {
			return 10;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s PP was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			return use(p.getActualMoves());
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(p.getMoves(b));
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class MaxElixir extends Item implements PokemonUseItem, BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;
		private boolean use(List<Move> moves)
		{
			boolean changed = false;
			for (Move m : moves)
			{
				changed |= m.increasePP(increaseAmount(m));
			}
			
			return changed;
		}

		MaxElixir() {
			super(ItemNamesies.MAX_ELIXIR, "It restores the PP of all the moves learned by the targeted Pok\u00e9mon by 10 points each.", BagCategory.MEDICINE, 172);
			super.price = 4500;
			super.battleBagCategories.add(BattleBagCategory.HP_PP);
		}

		public int increaseAmount(Move m) {
			return m.getMaxPP();
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s PP was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			return use(p.getActualMoves());
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(p.getMoves(b));
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class Ether extends Item implements HoldItem, MoveUseItem {
		private static final long serialVersionUID = 1L;
		private String restore;

		Ether() {
			super(ItemNamesies.ETHER, "It restores the PP of a Pok\u00e9mon's selected move by a maximum of 10 points.", BagCategory.MEDICINE, 173);
			super.price = 1200;
			super.battleBagCategories.add(BattleBagCategory.HP_PP);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s PP for " + restore + " PP was restored!";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(ActivePokemon p, Move m) {
			// TODO: Need to be able to call these from the battle! (BattleMoveUse? yuck) -- Test messages once completed
			restore = m.getAttack().getName();
			return m.increasePP(10);
		}
	}

	private static class MaxEther extends Item implements HoldItem, MoveUseItem {
		private static final long serialVersionUID = 1L;
		private String restore;

		MaxEther() {
			super(ItemNamesies.MAX_ETHER, "It fully restores the PP of a single selected move that has been learned by the target Pok\u00e9mon.", BagCategory.MEDICINE, 174);
			super.price = 2000;
			super.battleBagCategories.add(BattleBagCategory.HP_PP);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s PP for " + restore + " PP was restored!";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(ActivePokemon p, Move m) {
			// TODO: Need to be able to call these from the battle! (BattleMoveUse? yuck) -- Test messages once completed
			restore = m.getAttack().getName();
			return m.increasePP(m.getMaxPP());
		}
	}

	private static class BerryJuice extends Item implements PokemonUseItem, BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;

		BerryJuice() {
			super(ItemNamesies.BERRY_JUICE, "A 100% pure juice made of Berries. It restores the HP of one Pok\u00e9mon by just 20 points.", BagCategory.MEDICINE, 175);
			super.price = 100;
			super.battleBagCategories.add(BattleBagCategory.HP_PP);
		}

		public int healAmount() {
			return 20;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class SweetHeart extends Item implements PokemonUseItem, BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;

		SweetHeart() {
			super(ItemNamesies.SWEET_HEART, "Very sweet chocolate. It restores the HP of one Pok\u00e9mon by only 20 points.", BagCategory.MEDICINE, 176);
			super.price = 100;
			super.battleBagCategories.add(BattleBagCategory.HP_PP);
		}

		public int healAmount() {
			return 20;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class Potion extends Item implements PokemonUseItem, BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;

		Potion() {
			super(ItemNamesies.POTION, "A spray-type medicine for wounds. It restores the HP of one Pok\u00e9mon by just 20 points.", BagCategory.MEDICINE, 177);
			super.price = 100;
			super.battleBagCategories.add(BattleBagCategory.HP_PP);
		}

		public int healAmount() {
			return 20;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class EnergyPowder extends Item implements PokemonUseItem, BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;

		EnergyPowder() {
			super(ItemNamesies.ENERGY_POWDER, "A very bitter medicine powder. It restores the HP of one Pok\u00e9mon by 50 points.", BagCategory.MEDICINE, 178);
			super.price = 500;
			super.battleBagCategories.add(BattleBagCategory.HP_PP);
		}

		public int healAmount() {
			return 50;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class FreshWater extends Item implements PokemonUseItem, BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;

		FreshWater() {
			super(ItemNamesies.FRESH_WATER, "Water with a high mineral content. It restores the HP of one Pok\u00e9mon by 50 points.", BagCategory.MEDICINE, 179);
			super.price = 200;
			super.battleBagCategories.add(BattleBagCategory.HP_PP);
		}

		public int healAmount() {
			return 50;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class SuperPotion extends Item implements PokemonUseItem, BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;

		SuperPotion() {
			super(ItemNamesies.SUPER_POTION, "A spray-type medicine for wounds. It restores the HP of one Pok\u00e9mon by 50 points.", BagCategory.MEDICINE, 180);
			super.price = 700;
			super.battleBagCategories.add(BattleBagCategory.HP_PP);
		}

		public int healAmount() {
			return 50;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class SodaPop extends Item implements PokemonUseItem, BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;

		SodaPop() {
			super(ItemNamesies.SODA_POP, "A fizzy soda drink. It restores the HP of one Pok\u00e9mon by 60 points.", BagCategory.MEDICINE, 181);
			super.price = 300;
			super.battleBagCategories.add(BattleBagCategory.HP_PP);
		}

		public int healAmount() {
			return 60;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class Lemonade extends Item implements PokemonUseItem, BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;

		Lemonade() {
			super(ItemNamesies.LEMONADE, "A very sweet drink. It restores the HP of one Pok\u00e9mon by 80 points.", BagCategory.MEDICINE, 182);
			super.price = 350;
			super.battleBagCategories.add(BattleBagCategory.HP_PP);
		}

		public int healAmount() {
			return 80;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class MoomooMilk extends Item implements PokemonUseItem, BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;

		MoomooMilk() {
			super(ItemNamesies.MOOMOO_MILK, "Milk with a very high nutrition content. It restores the HP of one Pok\u00e9mon by 100 points.", BagCategory.MEDICINE, 183);
			super.price = 500;
			super.battleBagCategories.add(BattleBagCategory.HP_PP);
		}

		public int healAmount() {
			return 100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class EnergyRoot extends Item implements PokemonUseItem, BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;

		EnergyRoot() {
			super(ItemNamesies.ENERGY_ROOT, "A very bitter root. It restores the HP of one Pok\u00e9mon by 200 points.", BagCategory.MEDICINE, 184);
			super.price = 800;
			super.battleBagCategories.add(BattleBagCategory.HP_PP);
		}

		public int healAmount() {
			return 200;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class HyperPotion extends Item implements PokemonUseItem, BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;

		HyperPotion() {
			super(ItemNamesies.HYPER_POTION, "A spray-type medicine for wounds. It restores the HP of one Pok\u00e9mon by 200 points.", BagCategory.MEDICINE, 185);
			super.price = 1200;
			super.battleBagCategories.add(BattleBagCategory.HP_PP);
		}

		public int healAmount() {
			return 200;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			return p.heal(healAmount()) != 0;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class MaxPotion extends Item implements PokemonUseItem, BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;

		MaxPotion() {
			super(ItemNamesies.MAX_POTION, "A spray-type medicine for wounds. It completely restores the HP of a single Pok\u00e9mon.", BagCategory.MEDICINE, 186);
			super.price = 2500;
			super.battleBagCategories.add(BattleBagCategory.HP_PP);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s health was restored!";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			return p.healHealthFraction(1) != 0;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class Revive extends Item implements PokemonUseItem, BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;

		Revive() {
			super(ItemNamesies.REVIVE, "A medicine that revives a fainted Pok\u00e9mon. It restores half the Pok\u00e9mon's maximum HP.", BagCategory.MEDICINE, 187);
			super.price = 1500;
			super.battleBagCategories.add(BattleBagCategory.STATUS);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " was partially revived!";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			// Only applies to the dead
			if (!p.hasStatus(StatusCondition.FAINTED)) {
				return false;
			}
			
			p.removeStatus();
			p.healHealthFraction(.5);
			
			return true;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class MaxRevive extends Item implements PokemonUseItem, BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;

		MaxRevive() {
			super(ItemNamesies.MAX_REVIVE, "A medicine that revives a fainted Pok\u00e9mon. It fully restores the Pok\u00e9mon's HP.", BagCategory.MEDICINE, 188);
			super.price = 4000;
			super.battleBagCategories.add(BattleBagCategory.STATUS);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " was fully revived!";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			// Only applies to the dead
			if (!p.hasStatus(StatusCondition.FAINTED)) {
				return false;
			}
			
			p.removeStatus();
			p.healHealthFraction(1);
			
			return true;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class RevivalHerb extends Item implements PokemonUseItem, BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;

		RevivalHerb() {
			super(ItemNamesies.REVIVAL_HERB, "A very bitter medicinal herb. It revives a fainted Pok\u00e9mon, fully restoring its HP.", BagCategory.MEDICINE, 189);
			super.price = 2800;
			super.battleBagCategories.add(BattleBagCategory.STATUS);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " was fully revived!";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			// Only applies to the dead
			if (!p.hasStatus(StatusCondition.FAINTED)) {
				return false;
			}
			
			p.removeStatus();
			p.healHealthFraction(1);
			
			return true;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class SacredAsh extends Item implements TrainerUseItem, HoldItem, BattleUseItem {
		private static final long serialVersionUID = 1L;

		SacredAsh() {
			super(ItemNamesies.SACRED_ASH, "It revives all fainted Pok\u00e9mon. In doing so, it also fully restores their HP.", BagCategory.MEDICINE, 190);
			super.price = 4000;
			super.battleBagCategories.add(BattleBagCategory.STATUS);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "All fainted Pok\u00e9mon were fully revived!";
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

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use((Trainer)b.getTrainer(p.user()));
		}
	}

	private static class DireHit extends Item implements BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;

		DireHit() {
			super(ItemNamesies.DIRE_HIT, "It raises the critical-hit ratio greatly. It can be used only once and wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT, 191);
			super.price = 650;
			super.battleBagCategories.add(BattleBagCategory.BATTLE);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " is getting pumped!";
		}

		public boolean use(ActivePokemon p, Battle b) {
			PokemonEffect crits = PokemonEffect.getEffect(EffectNamesies.RAISE_CRITS);
			if (!crits.applies(b, p, p, CastSource.USE_ITEM)) {
				return false;
			}
			
			crits.cast(b, p, p, CastSource.USE_ITEM, false);
			return true;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class GuardSpec extends Item implements BattleUseItem, HoldItem {
		private static final long serialVersionUID = 1L;

		GuardSpec() {
			super(ItemNamesies.GUARD_SPEC, "An item that prevents stat reduction among the Trainer's party Pok\u00e9mon for five turns after use.", BagCategory.STAT, 192);
			super.price = 700;
			super.battleBagCategories.add(BattleBagCategory.BATTLE);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " is covered by a veil!";
		}

		public boolean use(ActivePokemon p, Battle b) {
			PokemonEffect gSpesh = PokemonEffect.getEffect(EffectNamesies.GUARD_SPECIAL);
			if (!gSpesh.applies(b, p, p, CastSource.USE_ITEM)) {
				return false;
			}
			
			gSpesh.cast(b, p, p, CastSource.USE_ITEM, false);
			return true;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class XAccuracy extends Item implements HoldItem, BattleUseItem {
		private static final long serialVersionUID = 1L;

		XAccuracy() {
			super(ItemNamesies.XACCURACY, "An item that raises the accuracy of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT, 193);
			super.price = 950;
			super.battleBagCategories.add(BattleBagCategory.BATTLE);
		}

		public Stat toIncrease() {
			return Stat.ACCURACY;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(ActivePokemon p, Battle b) {
			return p.getAttributes().modifyStage(p, p, 1, toIncrease(), b, CastSource.USE_ITEM);
		}
	}

	private static class XAttack extends Item implements HoldItem, BattleUseItem {
		private static final long serialVersionUID = 1L;

		XAttack() {
			super(ItemNamesies.XATTACK, "An item that raises the Attack stat of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT, 194);
			super.price = 500;
			super.battleBagCategories.add(BattleBagCategory.BATTLE);
		}

		public Stat toIncrease() {
			return Stat.ATTACK;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(ActivePokemon p, Battle b) {
			return p.getAttributes().modifyStage(p, p, 1, toIncrease(), b, CastSource.USE_ITEM);
		}
	}

	private static class XDefend extends Item implements HoldItem, BattleUseItem {
		private static final long serialVersionUID = 1L;

		XDefend() {
			super(ItemNamesies.XDEFEND, "An item that raises the Defense stat of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT, 195);
			super.price = 550;
			super.battleBagCategories.add(BattleBagCategory.BATTLE);
		}

		public Stat toIncrease() {
			return Stat.DEFENSE;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(ActivePokemon p, Battle b) {
			return p.getAttributes().modifyStage(p, p, 1, toIncrease(), b, CastSource.USE_ITEM);
		}
	}

	private static class XSpecial extends Item implements HoldItem, BattleUseItem {
		private static final long serialVersionUID = 1L;

		XSpecial() {
			super(ItemNamesies.XSPECIAL, "An item that raises the Sp. Atk stat of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT, 196);
			super.price = 350;
			super.battleBagCategories.add(BattleBagCategory.BATTLE);
		}

		public Stat toIncrease() {
			return Stat.SP_ATTACK;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(ActivePokemon p, Battle b) {
			return p.getAttributes().modifyStage(p, p, 1, toIncrease(), b, CastSource.USE_ITEM);
		}
	}

	private static class XSpDef extends Item implements HoldItem, BattleUseItem {
		private static final long serialVersionUID = 1L;

		XSpDef() {
			super(ItemNamesies.XSP_DEF, "An item that raises the Sp. Def stat of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT, 197);
			super.price = 350;
			super.battleBagCategories.add(BattleBagCategory.BATTLE);
		}

		public Stat toIncrease() {
			return Stat.SP_DEFENSE;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(ActivePokemon p, Battle b) {
			return p.getAttributes().modifyStage(p, p, 1, toIncrease(), b, CastSource.USE_ITEM);
		}
	}

	private static class XSpeed extends Item implements HoldItem, BattleUseItem {
		private static final long serialVersionUID = 1L;

		XSpeed() {
			super(ItemNamesies.XSPEED, "An item that raises the Speed stat of a Pok\u00e9mon in battle. It wears off if the Pok\u00e9mon is withdrawn.", BagCategory.STAT, 198);
			super.price = 350;
			super.battleBagCategories.add(BattleBagCategory.BATTLE);
		}

		public Stat toIncrease() {
			return Stat.SPEED;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(ActivePokemon p, Battle b) {
			return p.getAttributes().modifyStage(p, p, 1, toIncrease(), b, CastSource.USE_ITEM);
		}
	}

	private static class HPUp extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		HPUp() {
			super(ItemNamesies.HPUP, "A nutritious drink for Pok\u00e9mon. It raises the base HP of a single Pok\u00e9mon.", BagCategory.STAT, 199);
			super.price = 9800;
		}

		public Stat toIncrease() {
			return Stat.HP;
		}

		public int increaseAmount() {
			return 10;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class Protein extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		Protein() {
			super(ItemNamesies.PROTEIN, "A nutritious drink for Pok\u00e9mon. It raises the base Attack stat of a single Pok\u00e9mon.", BagCategory.STAT, 200);
			super.price = 9800;
		}

		public Stat toIncrease() {
			return Stat.ATTACK;
		}

		public int increaseAmount() {
			return 10;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class Iron extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		Iron() {
			super(ItemNamesies.IRON, "A nutritious drink for Pok\u00e9mon. It raises the base Defense stat of a single Pok\u00e9mon.", BagCategory.STAT, 201);
			super.price = 9800;
		}

		public Stat toIncrease() {
			return Stat.DEFENSE;
		}

		public int increaseAmount() {
			return 10;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class Calcium extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		Calcium() {
			super(ItemNamesies.CALCIUM, "A nutritious drink for Pok\u00e9mon. It raises the base Sp. Atk (Special Attack) stat of a single Pok\u00e9mon.", BagCategory.STAT, 202);
			super.price = 9800;
		}

		public Stat toIncrease() {
			return Stat.SP_ATTACK;
		}

		public int increaseAmount() {
			return 10;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class Zinc extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		Zinc() {
			super(ItemNamesies.ZINC, "A nutritious drink for Pok\u00e9mon. It raises the base Sp. Def (Special Defense) stat of a single Pok\u00e9mon.", BagCategory.STAT, 203);
			super.price = 9800;
		}

		public Stat toIncrease() {
			return Stat.SP_DEFENSE;
		}

		public int increaseAmount() {
			return 10;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class Carbos extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		Carbos() {
			super(ItemNamesies.CARBOS, "A nutritious drink for Pok\u00e9mon. It raises the base Speed stat of a single Pok\u00e9mon.", BagCategory.STAT, 204);
			super.price = 9800;
		}

		public Stat toIncrease() {
			return Stat.SPEED;
		}

		public int increaseAmount() {
			return 10;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class HealthWing extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		HealthWing() {
			super(ItemNamesies.HEALTH_WING, "An item for use on a Pok\u00e9mon. It slightly increases the base HP of a single Pok\u00e9mon.", BagCategory.STAT, 205);
			super.price = 3000;
		}

		public Stat toIncrease() {
			return Stat.HP;
		}

		public int increaseAmount() {
			return 1;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public int flingDamage() {
			return 20;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class MuscleWing extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		MuscleWing() {
			super(ItemNamesies.MUSCLE_WING, "An item for use on a Pok\u00e9mon. It slightly increases the base Attack stat of a single Pok\u00e9mon.", BagCategory.STAT, 206);
			super.price = 3000;
		}

		public Stat toIncrease() {
			return Stat.ATTACK;
		}

		public int increaseAmount() {
			return 1;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public int flingDamage() {
			return 20;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class ResistWing extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		ResistWing() {
			super(ItemNamesies.RESIST_WING, "An item for use on a Pok\u00e9mon. It slightly increases the base Defense stat of a single Pok\u00e9mon.", BagCategory.STAT, 207);
			super.price = 3000;
		}

		public Stat toIncrease() {
			return Stat.DEFENSE;
		}

		public int increaseAmount() {
			return 1;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public int flingDamage() {
			return 20;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class GeniusWing extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		GeniusWing() {
			super(ItemNamesies.GENIUS_WING, "An item for use on a Pok\u00e9mon. It slightly increases the base Sp. Atk stat of a single Pok\u00e9mon.", BagCategory.STAT, 208);
			super.price = 3000;
		}

		public Stat toIncrease() {
			return Stat.SP_ATTACK;
		}

		public int increaseAmount() {
			return 1;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public int flingDamage() {
			return 20;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class CleverWing extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		CleverWing() {
			super(ItemNamesies.CLEVER_WING, "An item for use on a Pok\u00e9mon. It slightly increases the base Sp. Def stat of a single Pok\u00e9mon.", BagCategory.STAT, 209);
			super.price = 3000;
		}

		public Stat toIncrease() {
			return Stat.SP_DEFENSE;
		}

		public int increaseAmount() {
			return 1;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public int flingDamage() {
			return 20;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class SwiftWing extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		SwiftWing() {
			super(ItemNamesies.SWIFT_WING, "An item for use on a Pok\u00e9mon. It slightly increases the base Speed stat of a single Pok\u00e9mon.", BagCategory.STAT, 210);
			super.price = 3000;
		}

		public Stat toIncrease() {
			return Stat.SPEED;
		}

		public int increaseAmount() {
			return 1;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toIncrease().getName() + " was raised!";
		}

		public int flingDamage() {
			return 20;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			int[] toAdd = new int[Stat.NUM_STATS];
			toAdd[toIncrease().index()] += increaseAmount();
			
			return p.addEVs(toAdd);
		}
	}

	private static class PPMax extends Item implements MoveUseItem, HoldItem {
		private static final long serialVersionUID = 1L;
		private String increase;

		PPMax() {
			super(ItemNamesies.PPMAX, "It maximally raises the top PP of a selected move that has been learned by the target Pok\u00e9mon.", BagCategory.STAT, 211);
			super.price = 9800;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + increase + "'s Max PP was increased!";
		}

		public boolean use(ActivePokemon p, Move m) {
			increase = m.getAttack().getName();
			return m.increaseMaxPP(3);
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class PPUp extends Item implements MoveUseItem, HoldItem {
		private static final long serialVersionUID = 1L;
		private String increase;

		PPUp() {
			super(ItemNamesies.PPUP, "It slightly raises the maximum PP of a selected move that has been learned by the target Pok\u00e9mon.", BagCategory.STAT, 212);
			super.price = 9800;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + increase + "'s Max PP was increased!";
		}

		public boolean use(ActivePokemon p, Move m) {
			increase = m.getAttack().getName();
			return m.increaseMaxPP(1);
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class RareCandy extends Item implements HoldItem, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		RareCandy() {
			super(ItemNamesies.RARE_CANDY, "A candy that is packed with energy. It raises the level of a single Pok\u00e9mon by one.", BagCategory.STAT, 213);
			super.price = 4800;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " gained a level!";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			// TODO: Doesn't show animation if it causes an evolution
			return p.levelUp(null);
		}
	}

	private static class CherishBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		CherishBall() {
			super(ItemNamesies.CHERISH_BALL, "A quite rare Pok\u00e9 Ball that has been specially crafted to commemorate an occasion of some sort.", BagCategory.BALL, 214);
			super.price = 200;
			super.battleBagCategories.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p) {
		}
	}

	private static class DiveBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		DiveBall() {
			super(ItemNamesies.DIVE_BALL, "A somewhat different Pok\u00e9 Ball that works especially well on Pok\u00e9mon that live underwater.", BagCategory.BALL, 215);
			super.price = 1000;
			super.battleBagCategories.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			// TODO: Not sure yet if this will cover fishing
			if (b.getTerrainType() == TerrainType.WATER) {
				return new double[] {3.5, 0};
			}
			
			return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p) {
		}
	}

	private static class DuskBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		DuskBall() {
			super(ItemNamesies.DUSK_BALL, "A somewhat different Pok\u00e9 Ball that makes it easier to catch wild Pok\u00e9mon at night or in dark places like caves.", BagCategory.BALL, 216);
			super.price = 1000;
			super.battleBagCategories.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			if (b.getTerrainType() == TerrainType.CAVE) {
				return new double[] {3.5, 0};
			}
			
			return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p) {
		}
	}

	private static class FastBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		FastBall() {
			super(ItemNamesies.FAST_BALL, "A Pok\u00e9 Ball that makes it easier to catch Pok\u00e9mon which are quick to run away.", BagCategory.BALL, 217);
			super.price = 200;
			super.battleBagCategories.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			// If the opponent has a base speed of 100 or higher, multiplier is 4
			if (o.getPokemonInfo().getStat(Stat.SPEED.index()) >= 100) {
				return new double[] {4, 0};
			}
			
			return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p) {
		}
	}

	private static class GreatBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		GreatBall() {
			super(ItemNamesies.GREAT_BALL, "A good, high-performance Ball that provides a higher Pok\u00e9mon catch rate than a standard Pok\u00e9 Ball.", BagCategory.BALL, 218);
			super.price = 600;
			super.battleBagCategories.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			return new double[] {1.5, 0};
		}

		public void afterCaught(ActivePokemon p) {
		}
	}

	private static class HealBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		HealBall() {
			super(ItemNamesies.HEAL_BALL, "A remedial Pok\u00e9 Ball that restores the caught Pok\u00e9mon's HP and eliminates any status problem.", BagCategory.BALL, 219);
			super.price = 300;
			super.battleBagCategories.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p) {
			p.fullyHeal();
		}
	}

	private static class HeavyBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		HeavyBall() {
			super(ItemNamesies.HEAVY_BALL, "A Pok\u00e9 Ball for catching very heavy Pok\u00e9mon.", BagCategory.BALL, 220);
			super.price = 200;
			super.battleBagCategories.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			double weight = o.getWeight(b);
			
			double[] res = new double[2];
			res[0] = 1;
			
			// TODO: Rewrite this with a loop
			if (weight <= 451.5) res[1] = -20;
			else if (weight <= 661.5) res[1] = 20;
			else if (weight <= 903.0) res[1] = 30;
			else res[1] = 40;
			
			return res;
		}

		public void afterCaught(ActivePokemon p) {
		}
	}

	private static class LevelBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		LevelBall() {
			super(ItemNamesies.LEVEL_BALL, "A Pok\u00e9 Ball for catching Pok\u00e9mon that are a lower level than your own.", BagCategory.BALL, 221);
			super.price = 200;
			super.battleBagCategories.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			// TODO: Rewrite this in a loop
			if (me.getLevel()/4 > o.getLevel()) return new double[] {8, 0};
			else if (me.getLevel()/2 > o.getLevel()) return new double[] {4, 0};
			else if (me.getLevel() > o.getLevel()) return new double[] {2, 0};
			else return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p) {
		}
	}

	private static class LoveBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		LoveBall() {
			super(ItemNamesies.LOVE_BALL, "Pok\u00e9 Ball for catching Pok\u00e9mon that are the opposite gender of your Pok\u00e9mon.", BagCategory.BALL, 222);
			super.price = 200;
			super.battleBagCategories.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			if (me.getGender() == o.getGender()) {
				return new double[] {8, 0};
			}
			
			return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p) {
		}
	}

	private static class LureBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		LureBall() {
			super(ItemNamesies.LURE_BALL, "A Pok\u00e9 Ball for catching Pok\u00e9mon hooked by a Rod when fishing.", BagCategory.BALL, 223);
			super.price = 200;
			super.battleBagCategories.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			// TODO: Fishing
			if (false) {
				return new double[] {3, 0};
			}
			
			return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p) {
		}
	}

	private static class LuxuryBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		LuxuryBall() {
			super(ItemNamesies.LUXURY_BALL, "A comfortable Pok\u00e9 Ball that makes a caught wild Pok\u00e9mon quickly grow friendly.", BagCategory.BALL, 224);
			super.price = 1000;
			super.battleBagCategories.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p) {
			// TODO: Make this item do something more interesting
		}
	}

	private static class MasterBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		MasterBall() {
			super(ItemNamesies.MASTER_BALL, "The best Ball with the ultimate level of performance. It will catch any wild Pok\u00e9mon without fail.", BagCategory.BALL, 225);
			super.price = 0;
			super.battleBagCategories.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			return new double[] {255, 0};
		}

		public void afterCaught(ActivePokemon p) {
		}
	}

	private static class MoonBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		MoonBall() {
			super(ItemNamesies.MOON_BALL, "A Pok\u00e9 Ball for catching Pok\u00e9mon that evolve using the Moon Stone.", BagCategory.BALL, 226);
			super.price = 200;
			super.battleBagCategories.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			Evolution ev = o.getPokemonInfo().getEvolution();
			if (ev.getEvolution(EvolutionCheck.ITEM, o, ItemNamesies.MOON_STONE) != null) {
				return new double[] {4, 0};
			}
			
			return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p) {
		}
	}

	private static class NestBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		NestBall() {
			super(ItemNamesies.NEST_BALL, "A somewhat different Pok\u00e9 Ball that works especially well on weaker Pok\u00e9mon in the wild.", BagCategory.BALL, 227);
			super.price = 1000;
			super.battleBagCategories.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			// TODO: Loopy and make it general with the others
			if (o.getLevel() <= 19) return new double[] {3, 0};
			else if (o.getLevel() <= 29) return new double[] {2, 0};
			else return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p) {
		}
	}

	private static class NetBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		NetBall() {
			super(ItemNamesies.NET_BALL, "A somewhat different Pok\u00e9 Ball that works especially well on Water- and Bug-type Pok\u00e9mon.", BagCategory.BALL, 228);
			super.price = 1000;
			super.battleBagCategories.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			if (o.isType(b, Type.WATER) || o.isType(b, Type.BUG)) {
				return new double[] {3, 0};
			}
			
			return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p) {
		}
	}

	private static class PokeBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		PokeBall() {
			super(ItemNamesies.POKE_BALL, "A device for catching wild Pok\u00e9mon. It is thrown like a ball at the target. It is designed as a capsule system.", BagCategory.BALL, 229);
			super.price = 200;
			super.battleBagCategories.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p) {
		}
	}

	private static class PremierBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		PremierBall() {
			super(ItemNamesies.PREMIER_BALL, "A somewhat rare Pok\u00e9 Ball that has been specially made to commemorate an event of some sort.", BagCategory.BALL, 230);
			super.price = 200;
			super.battleBagCategories.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p) {
		}
	}

	private static class QuickBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		QuickBall() {
			super(ItemNamesies.QUICK_BALL, "A somewhat different Pok\u00e9 Ball that provides a better catch rate if it is used at the start of a wild encounter.", BagCategory.BALL, 231);
			super.price = 1000;
			super.battleBagCategories.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			// TODO: Generalize this
			if (b.getTurn() == 1) {
				return new double[] {3, 0};
			}
			
			return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p) {
		}
	}

	private static class RepeatBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		RepeatBall() {
			super(ItemNamesies.REPEAT_BALL, "A somewhat different Pok\u00e9 Ball that works especially well on Pok\u00e9mon species that were previously caught.", BagCategory.BALL, 232);
			super.price = 1000;
			super.battleBagCategories.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			if (b.getPlayer().getPokedex().caught(o.getPokemonInfo().namesies())) {
				return new double[] {3, 0};
			}
			
			return new double[] {1, 0};
		}

		public void afterCaught(ActivePokemon p) {
		}
	}

	private static class SafariBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		SafariBall() {
			super(ItemNamesies.SAFARI_BALL, "A special Pok\u00e9 Ball that is used only in the Safari Zone. It is decorated in a camouflage pattern.", BagCategory.BALL, 233);
			super.battleBagCategories.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			return new double[] {1.5, 0};
		}

		public void afterCaught(ActivePokemon p) {
		}
	}

	private static class TimerBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		TimerBall() {
			super(ItemNamesies.TIMER_BALL, "A somewhat different Ball that becomes progressively better the more turns there are in a battle.", BagCategory.BALL, 234);
			super.price = 1000;
			super.battleBagCategories.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			if (b.getTurn() <= 10) return new double[] {1, 0};
			else if (b.getTurn() <= 20) return new double[] {2, 0};
			else if (b.getTurn() <= 30) return new double[] {3, 0};
			else return new double[] {4, 0};
		}

		public void afterCaught(ActivePokemon p) {
		}
	}

	private static class UltraBall extends Item implements BallItem {
		private static final long serialVersionUID = 1L;

		UltraBall() {
			super(ItemNamesies.ULTRA_BALL, "An ultra-performance Ball that provides a higher Pok\u00e9mon catch rate than a Great Ball.", BagCategory.BALL, 235);
			super.price = 1200;
			super.battleBagCategories.add(BattleBagCategory.BALL);
		}

		public double[] catchRate(ActivePokemon me, ActivePokemon o, Battle b) {
			return new double[] {2, 0};
		}

		public void afterCaught(ActivePokemon p) {
		}
	}

	private static class CheriBerry extends Item implements StatusBerry, PokemonUseItem, BattleUseItem {
		private static final long serialVersionUID = 1L;
		private String message;
		private String holdMessage;

		CheriBerry() {
			super(ItemNamesies.CHERI_BERRY, "If held by a Pok\u00e9mon, it recovers from paralysis.", BagCategory.BERRY, 236);
			super.price = 20;
			super.battleBagCategories.add(BattleBagCategory.STATUS);
		}

		public StatusCondition toRemove() {
			return StatusCondition.PARALYZED;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return message;
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			if (!p.hasStatus(toRemove())) {
				return false;
			}
			
			message = Status.getRemoveStatus(null, p, CastSource.USE_ITEM);
			return true;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public String getHoldSuccessMessage(Battle b, ActivePokemon p) {
			return holdMessage;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
			if (!user.hasStatus(toRemove())) {
				return false;
			}
			
			holdMessage = Status.getRemoveStatus(b, user, source);
			
			String message = "";
			switch (source) {
				case USE_ITEM:
					message = getSuccessMessage(user);
					break;
				case HELD_ITEM:
					message = getHoldSuccessMessage(b, user);
					break;
				default:
					Global.error("Use item and held item are the only valid cast sources for berries.");
				}
				
				b.addMessage(message, user);
				
				if (user.hasAbility(AbilityNamesies.CHEEK_POUCH) && !user.fullHealth()) {
					b.addMessage(user.getName() + "'s " + AbilityNamesies.CHEEK_POUCH.getName() + " restored its health!");
					user.healHealthFraction(1/3.0);
					b.addMessage("", user);
				}
				
				// Eat dat berry!!
				PokemonEffect.getEffect(EffectNamesies.EATEN_BERRY).cast(b, user, user, source, false);
				
				return true;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.FIRE;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class ChestoBerry extends Item implements StatusBerry, PokemonUseItem, BattleUseItem {
		private static final long serialVersionUID = 1L;
		private String message;
		private String holdMessage;

		ChestoBerry() {
			super(ItemNamesies.CHESTO_BERRY, "If held by a Pok\u00e9mon, it recovers from sleep.", BagCategory.BERRY, 237);
			super.price = 20;
			super.battleBagCategories.add(BattleBagCategory.STATUS);
		}

		public StatusCondition toRemove() {
			return StatusCondition.ASLEEP;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return message;
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			if (!p.hasStatus(toRemove())) {
				return false;
			}
			
			message = Status.getRemoveStatus(null, p, CastSource.USE_ITEM);
			return true;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public String getHoldSuccessMessage(Battle b, ActivePokemon p) {
			return holdMessage;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
			if (!user.hasStatus(toRemove())) {
				return false;
			}
			
			holdMessage = Status.getRemoveStatus(b, user, source);
			
			String message = "";
			switch (source) {
				case USE_ITEM:
					message = getSuccessMessage(user);
					break;
				case HELD_ITEM:
					message = getHoldSuccessMessage(b, user);
					break;
				default:
					Global.error("Use item and held item are the only valid cast sources for berries.");
				}
				
				b.addMessage(message, user);
				
				if (user.hasAbility(AbilityNamesies.CHEEK_POUCH) && !user.fullHealth()) {
					b.addMessage(user.getName() + "'s " + AbilityNamesies.CHEEK_POUCH.getName() + " restored its health!");
					user.healHealthFraction(1/3.0);
					b.addMessage("", user);
				}
				
				// Eat dat berry!!
				PokemonEffect.getEffect(EffectNamesies.EATEN_BERRY).cast(b, user, user, source, false);
				
				return true;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.WATER;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class PechaBerry extends Item implements StatusBerry, PokemonUseItem, BattleUseItem {
		private static final long serialVersionUID = 1L;
		private String message;
		private String holdMessage;

		PechaBerry() {
			super(ItemNamesies.PECHA_BERRY, "If held by a Pok\u00e9mon, it recovers from poison.", BagCategory.BERRY, 238);
			super.price = 20;
			super.battleBagCategories.add(BattleBagCategory.STATUS);
		}

		public StatusCondition toRemove() {
			return StatusCondition.POISONED;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return message;
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			if (!p.hasStatus(toRemove())) {
				return false;
			}
			
			message = Status.getRemoveStatus(null, p, CastSource.USE_ITEM);
			return true;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public String getHoldSuccessMessage(Battle b, ActivePokemon p) {
			return holdMessage;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
			if (!user.hasStatus(toRemove())) {
				return false;
			}
			
			holdMessage = Status.getRemoveStatus(b, user, source);
			
			String message = "";
			switch (source) {
				case USE_ITEM:
					message = getSuccessMessage(user);
					break;
				case HELD_ITEM:
					message = getHoldSuccessMessage(b, user);
					break;
				default:
					Global.error("Use item and held item are the only valid cast sources for berries.");
				}
				
				b.addMessage(message, user);
				
				if (user.hasAbility(AbilityNamesies.CHEEK_POUCH) && !user.fullHealth()) {
					b.addMessage(user.getName() + "'s " + AbilityNamesies.CHEEK_POUCH.getName() + " restored its health!");
					user.healHealthFraction(1/3.0);
					b.addMessage("", user);
				}
				
				// Eat dat berry!!
				PokemonEffect.getEffect(EffectNamesies.EATEN_BERRY).cast(b, user, user, source, false);
				
				return true;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.ELECTRIC;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class RawstBerry extends Item implements StatusBerry, PokemonUseItem, BattleUseItem {
		private static final long serialVersionUID = 1L;
		private String message;
		private String holdMessage;

		RawstBerry() {
			super(ItemNamesies.RAWST_BERRY, "If held by a Pok\u00e9mon, it recovers from a burn.", BagCategory.BERRY, 239);
			super.price = 20;
			super.battleBagCategories.add(BattleBagCategory.STATUS);
		}

		public StatusCondition toRemove() {
			return StatusCondition.BURNED;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return message;
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			if (!p.hasStatus(toRemove())) {
				return false;
			}
			
			message = Status.getRemoveStatus(null, p, CastSource.USE_ITEM);
			return true;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public String getHoldSuccessMessage(Battle b, ActivePokemon p) {
			return holdMessage;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
			if (!user.hasStatus(toRemove())) {
				return false;
			}
			
			holdMessage = Status.getRemoveStatus(b, user, source);
			
			String message = "";
			switch (source) {
				case USE_ITEM:
					message = getSuccessMessage(user);
					break;
				case HELD_ITEM:
					message = getHoldSuccessMessage(b, user);
					break;
				default:
					Global.error("Use item and held item are the only valid cast sources for berries.");
				}
				
				b.addMessage(message, user);
				
				if (user.hasAbility(AbilityNamesies.CHEEK_POUCH) && !user.fullHealth()) {
					b.addMessage(user.getName() + "'s " + AbilityNamesies.CHEEK_POUCH.getName() + " restored its health!");
					user.healHealthFraction(1/3.0);
					b.addMessage("", user);
				}
				
				// Eat dat berry!!
				PokemonEffect.getEffect(EffectNamesies.EATEN_BERRY).cast(b, user, user, source, false);
				
				return true;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.GRASS;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class AspearBerry extends Item implements StatusBerry, PokemonUseItem, BattleUseItem {
		private static final long serialVersionUID = 1L;
		private String message;
		private String holdMessage;

		AspearBerry() {
			super(ItemNamesies.ASPEAR_BERRY, "If held by a Pok\u00e9mon, it defrosts it.", BagCategory.BERRY, 240);
			super.price = 20;
			super.battleBagCategories.add(BattleBagCategory.STATUS);
		}

		public StatusCondition toRemove() {
			return StatusCondition.FROZEN;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return message;
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			if (!p.hasStatus(toRemove())) {
				return false;
			}
			
			message = Status.getRemoveStatus(null, p, CastSource.USE_ITEM);
			return true;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public String getHoldSuccessMessage(Battle b, ActivePokemon p) {
			return holdMessage;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
			if (!user.hasStatus(toRemove())) {
				return false;
			}
			
			holdMessage = Status.getRemoveStatus(b, user, source);
			
			String message = "";
			switch (source) {
				case USE_ITEM:
					message = getSuccessMessage(user);
					break;
				case HELD_ITEM:
					message = getHoldSuccessMessage(b, user);
					break;
				default:
					Global.error("Use item and held item are the only valid cast sources for berries.");
				}
				
				b.addMessage(message, user);
				
				if (user.hasAbility(AbilityNamesies.CHEEK_POUCH) && !user.fullHealth()) {
					b.addMessage(user.getName() + "'s " + AbilityNamesies.CHEEK_POUCH.getName() + " restored its health!");
					user.healHealthFraction(1/3.0);
					b.addMessage("", user);
				}
				
				// Eat dat berry!!
				PokemonEffect.getEffect(EffectNamesies.EATEN_BERRY).cast(b, user, user, source, false);
				
				return true;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.ICE;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class LeppaBerry extends Item implements EndTurnEffect, GainableEffectBerry, MoveUseItem {
		private static final long serialVersionUID = 1L;
		private String restore;

		LeppaBerry() {
			super(ItemNamesies.LEPPA_BERRY, "If held by a Pok\u00e9mon, it restores a move's PP by 10.", BagCategory.BERRY, 241);
			super.price = 20;
			super.battleBagCategories.add(BattleBagCategory.HP_PP);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s PP for " + restore + " PP was restored!";
		}

		public String getHoldSuccessMessage(Battle b, ActivePokemon p) {
			return p.getName() + "'s " + this.getName() + " restored " + restore + "'s PP!";
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			for (Move m : victim.getMoves(b)) {
				if (m.getPP() == 0) {
					use(victim, m);
					b.addMessage(getHoldSuccessMessage(b, victim));
					victim.consumeItem(b);
					break;
				}
			}
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
			Move lowestPPMove = null;
			double lowestPPRatio = 1;
			
			for (Move m : user.getMoves(b)) {
				double ratio = (double)m.getPP()/m.getMaxPP();
				if (ratio < lowestPPRatio) {
					lowestPPRatio = ratio;
					lowestPPMove = m;
				}
			}
			
			// All moves have full PP
			if (lowestPPMove == null) {
				return false;
			}
			
			use(user, lowestPPMove);
			
			String message = "";
			switch (source) {
				case USE_ITEM:
					message = getSuccessMessage(user);
					break;
				case HELD_ITEM:
					message = getHoldSuccessMessage(b, user);
					break;
				default:
					Global.error("Use item and held item are the only valid cast sources for berries.");
				}
				
				b.addMessage(message, user);
				
				if (user.hasAbility(AbilityNamesies.CHEEK_POUCH) && !user.fullHealth()) {
					b.addMessage(user.getName() + "'s " + AbilityNamesies.CHEEK_POUCH.getName() + " restored its health!");
					user.healHealthFraction(1/3.0);
					b.addMessage("", user);
				}
				
				// Eat dat berry!!
				PokemonEffect.getEffect(EffectNamesies.EATEN_BERRY).cast(b, user, user, source, false);
				
				return true;
		}

		public boolean use(ActivePokemon p, Move m) {
			// TODO: Need to be able to call these from the battle! (BattleMoveUse? yuck) -- Test messages once completed
			restore = m.getAttack().getName();
			return m.increasePP(10);
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.FIGHTING;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class OranBerry extends Item implements HealthTriggeredBerry, PokemonUseItem, BattleUseItem {
		private static final long serialVersionUID = 1L;

		OranBerry() {
			super(ItemNamesies.ORAN_BERRY, "If held by a Pok\u00e9mon, it heals the user by just 10 HP.", BagCategory.BERRY, 242);
			super.price = 20;
			super.battleBagCategories.add(BattleBagCategory.HP_PP);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s health was restored!";
		}

		public String getHoldSuccessMessage(Battle b, ActivePokemon p) {
			return p.getName() + " was healed by its " + this.name + "!";
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user, CastSource source) {
			return use(user, b);
		}

		public double healthTriggerRatio() {
			return 1/3.0;
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			return p.heal(10) != 0;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
			if (!useHealthTriggerBerry(b, user, source)) {
				return false;
			}
			
			String message = "";
			switch (source) {
				case USE_ITEM:
					message = getSuccessMessage(user);
					break;
				case HELD_ITEM:
					message = getHoldSuccessMessage(b, user);
					break;
				default:
					Global.error("Use item and held item are the only valid cast sources for berries.");
				}
				
				b.addMessage(message, user);
				
				if (user.hasAbility(AbilityNamesies.CHEEK_POUCH) && !user.fullHealth()) {
					b.addMessage(user.getName() + "'s " + AbilityNamesies.CHEEK_POUCH.getName() + " restored its health!");
					user.healHealthFraction(1/3.0);
					b.addMessage("", user);
				}
				
				// Eat dat berry!!
				PokemonEffect.getEffect(EffectNamesies.EATEN_BERRY).cast(b, user, user, source, false);
				
				return true;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.POISON;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class PersimBerry extends Item implements BattleUseItem, GainableEffectBerry {
		private static final long serialVersionUID = 1L;

		PersimBerry() {
			super(ItemNamesies.PERSIM_BERRY, "If held by a Pok\u00e9mon, it recovers from confusion.", BagCategory.BERRY, 243);
			super.price = 20;
			super.battleBagCategories.add(BattleBagCategory.HP_PP);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " snapped out of its confusion!";
		}

		public String getHoldSuccessMessage(Battle b, ActivePokemon p) {
			return p.getName() + "'s " + this.getName() + " snapped it out of confusion!";
		}

		public boolean use(ActivePokemon p, Battle b) {
			if (p.hasEffect(EffectNamesies.CONFUSION)) {
				p.getAttributes().removeEffect(EffectNamesies.CONFUSION);
				return true;
			}
			
			return false;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
			if (!use(user, b)) {
				return false;
			}
			
			String message = "";
			switch (source) {
				case USE_ITEM:
					message = getSuccessMessage(user);
					break;
				case HELD_ITEM:
					message = getHoldSuccessMessage(b, user);
					break;
				default:
					Global.error("Use item and held item are the only valid cast sources for berries.");
				}
				
				b.addMessage(message, user);
				
				if (user.hasAbility(AbilityNamesies.CHEEK_POUCH) && !user.fullHealth()) {
					b.addMessage(user.getName() + "'s " + AbilityNamesies.CHEEK_POUCH.getName() + " restored its health!");
					user.healHealthFraction(1/3.0);
					b.addMessage("", user);
				}
				
				// Eat dat berry!!
				PokemonEffect.getEffect(EffectNamesies.EATEN_BERRY).cast(b, user, user, source, false);
				
				return true;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.GROUND;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class LumBerry extends Item implements StatusBerry, PokemonUseItem, BattleUseItem {
		private static final long serialVersionUID = 1L;
		private String holdMessage;

		LumBerry() {
			super(ItemNamesies.LUM_BERRY, "If held by a Pok\u00e9mon, it recovers from any status problem.", BagCategory.BERRY, 244);
			super.price = 20;
			super.battleBagCategories.add(BattleBagCategory.STATUS);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " was cured of its status condition!";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			// Does not apply to the dead
			if (p.hasStatus(StatusCondition.FAINTED)) {
				return false;
			}
			
			// YOU'RE FINE
			if (!p.hasStatus()) {
				return false;
			}
			
			p.removeStatus();
			return true;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public String getHoldSuccessMessage(Battle b, ActivePokemon p) {
			return holdMessage;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
			// Does not apply to the dead
			if (user.hasStatus(StatusCondition.FAINTED)) {
				return false;
			}
			
			// YOU'RE FINE
			if (!user.hasStatus()) {
				return false;
			}
			
			holdMessage = Status.getRemoveStatus(b, user, source);
			
			String message = "";
			switch (source) {
				case USE_ITEM:
					message = getSuccessMessage(user);
					break;
				case HELD_ITEM:
					message = getHoldSuccessMessage(b, user);
					break;
				default:
					Global.error("Use item and held item are the only valid cast sources for berries.");
				}
				
				b.addMessage(message, user);
				
				if (user.hasAbility(AbilityNamesies.CHEEK_POUCH) && !user.fullHealth()) {
					b.addMessage(user.getName() + "'s " + AbilityNamesies.CHEEK_POUCH.getName() + " restored its health!");
					user.healHealthFraction(1/3.0);
					b.addMessage("", user);
				}
				
				// Eat dat berry!!
				PokemonEffect.getEffect(EffectNamesies.EATEN_BERRY).cast(b, user, user, source, false);
				
				return true;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.FLYING;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class SitrusBerry extends Item implements PokemonUseItem, BattleUseItem, HealthTriggeredBerry {
		private static final long serialVersionUID = 1L;

		SitrusBerry() {
			super(ItemNamesies.SITRUS_BERRY, "If held by a Pok\u00e9mon, it heals the user by a little.", BagCategory.BERRY, 245);
			super.price = 20;
			super.battleBagCategories.add(BattleBagCategory.HP_PP);
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s health was restored!";
		}

		public String getHoldSuccessMessage(Battle b, ActivePokemon p) {
			return p.getName() + " was healed by its " + this.name + "!";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			return p.healHealthFraction(1/4.0) != 0;
		}

		public boolean use(ActivePokemon p, Battle b) {
			return use(b.getPlayer(), p);
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user, CastSource source) {
			return use(user, b);
		}

		public double healthTriggerRatio() {
			return 1/2.0;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
			if (!useHealthTriggerBerry(b, user, source)) {
				return false;
			}
			
			String message = "";
			switch (source) {
				case USE_ITEM:
					message = getSuccessMessage(user);
					break;
				case HELD_ITEM:
					message = getHoldSuccessMessage(b, user);
					break;
				default:
					Global.error("Use item and held item are the only valid cast sources for berries.");
				}
				
				b.addMessage(message, user);
				
				if (user.hasAbility(AbilityNamesies.CHEEK_POUCH) && !user.fullHealth()) {
					b.addMessage(user.getName() + "'s " + AbilityNamesies.CHEEK_POUCH.getName() + " restored its health!");
					user.healHealthFraction(1/3.0);
					b.addMessage("", user);
				}
				
				// Eat dat berry!!
				PokemonEffect.getEffect(EffectNamesies.EATEN_BERRY).cast(b, user, user, source, false);
				
				return true;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.PSYCHIC;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class RazzBerry extends Item implements Berry {
		private static final long serialVersionUID = 1L;

		RazzBerry() {
			super(ItemNamesies.RAZZ_BERRY, "A very valuable berry. Useful for aquiring value.", BagCategory.BERRY, 246);
			super.price = 60000;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.STEEL;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class PomegBerry extends Item implements Berry, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		PomegBerry() {
			super(ItemNamesies.POMEG_BERRY, "Using it on a Pok\u00e9mon lowers its base HP.", BagCategory.BERRY, 247);
			super.price = 20;
		}

		public Stat toDecrease() {
			return Stat.HP;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toDecrease().getName() + " was lowered!";
		}

		public int naturalGiftPower() {
			return 90;
		}

		public Type naturalGiftType() {
			return Type.ICE;
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			int[] vals = new int[Stat.NUM_STATS];
			if (p.getEV(toDecrease().index()) > 110) {
				vals[toDecrease().index()] = 100 - p.getEV(toDecrease().index());
			}
			else {
				vals[toDecrease().index()] -= 10;
			}
			
			return p.addEVs(vals);
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class KelpsyBerry extends Item implements Berry, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		KelpsyBerry() {
			super(ItemNamesies.KELPSY_BERRY, "Using it on a Pok\u00e9mon lowers its base Attack stat.", BagCategory.BERRY, 248);
			super.price = 20;
		}

		public Stat toDecrease() {
			return Stat.ATTACK;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toDecrease().getName() + " was lowered!";
		}

		public int naturalGiftPower() {
			return 90;
		}

		public Type naturalGiftType() {
			return Type.FIGHTING;
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			int[] vals = new int[Stat.NUM_STATS];
			if (p.getEV(toDecrease().index()) > 110) {
				vals[toDecrease().index()] = 100 - p.getEV(toDecrease().index());
			}
			else {
				vals[toDecrease().index()] -= 10;
			}
			
			return p.addEVs(vals);
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class QualotBerry extends Item implements Berry, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		QualotBerry() {
			super(ItemNamesies.QUALOT_BERRY, "Using it on a Pok\u00e9mon lowers its base Defense stat.", BagCategory.BERRY, 249);
			super.price = 20;
		}

		public Stat toDecrease() {
			return Stat.DEFENSE;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toDecrease().getName() + " was lowered!";
		}

		public int naturalGiftPower() {
			return 90;
		}

		public Type naturalGiftType() {
			return Type.POISON;
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			int[] vals = new int[Stat.NUM_STATS];
			if (p.getEV(toDecrease().index()) > 110) {
				vals[toDecrease().index()] = 100 - p.getEV(toDecrease().index());
			}
			else {
				vals[toDecrease().index()] -= 10;
			}
			
			return p.addEVs(vals);
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class HondewBerry extends Item implements Berry, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		HondewBerry() {
			super(ItemNamesies.HONDEW_BERRY, "Using it on a Pok\u00e9mon lowers its base Sp. Atk stat.", BagCategory.BERRY, 250);
			super.price = 20;
		}

		public Stat toDecrease() {
			return Stat.SP_ATTACK;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toDecrease().getName() + " was lowered!";
		}

		public int naturalGiftPower() {
			return 90;
		}

		public Type naturalGiftType() {
			return Type.GROUND;
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			int[] vals = new int[Stat.NUM_STATS];
			if (p.getEV(toDecrease().index()) > 110) {
				vals[toDecrease().index()] = 100 - p.getEV(toDecrease().index());
			}
			else {
				vals[toDecrease().index()] -= 10;
			}
			
			return p.addEVs(vals);
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class GrepaBerry extends Item implements Berry, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		GrepaBerry() {
			super(ItemNamesies.GREPA_BERRY, "Using it on a Pok\u00e9mon lowers its base Sp. Def stat.", BagCategory.BERRY, 251);
			super.price = 20;
		}

		public Stat toDecrease() {
			return Stat.SP_DEFENSE;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toDecrease().getName() + " was lowered!";
		}

		public int naturalGiftPower() {
			return 90;
		}

		public Type naturalGiftType() {
			return Type.FLYING;
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			int[] vals = new int[Stat.NUM_STATS];
			if (p.getEV(toDecrease().index()) > 110) {
				vals[toDecrease().index()] = 100 - p.getEV(toDecrease().index());
			}
			else {
				vals[toDecrease().index()] -= 10;
			}
			
			return p.addEVs(vals);
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class TamatoBerry extends Item implements Berry, PokemonUseItem {
		private static final long serialVersionUID = 1L;

		TamatoBerry() {
			super(ItemNamesies.TAMATO_BERRY, "Using it on a Pok\u00e9mon lowers its base Speed.", BagCategory.BERRY, 252);
			super.price = 20;
		}

		public Stat toDecrease() {
			return Stat.SPEED;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + toDecrease().getName() + " was lowered!";
		}

		public int naturalGiftPower() {
			return 90;
		}

		public Type naturalGiftType() {
			return Type.PSYCHIC;
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			int[] vals = new int[Stat.NUM_STATS];
			if (p.getEV(toDecrease().index()) > 110) {
				vals[toDecrease().index()] = 100 - p.getEV(toDecrease().index());
			}
			else {
				vals[toDecrease().index()] -= 10;
			}
			
			return p.addEVs(vals);
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class OccaBerry extends Item implements Berry, OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		OccaBerry() {
			super(ItemNamesies.OCCA_BERRY, "Weakens a supereffective Fire-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 253);
			super.price = 20;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.FIRE;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.FIRE && Type.getAdvantage(user, victim, b) > 1) {
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class PasshoBerry extends Item implements Berry, OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		PasshoBerry() {
			super(ItemNamesies.PASSHO_BERRY, "Weakens a supereffective Water-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 254);
			super.price = 20;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.WATER;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.WATER && Type.getAdvantage(user, victim, b) > 1) {
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class WacanBerry extends Item implements Berry, OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		WacanBerry() {
			super(ItemNamesies.WACAN_BERRY, "Weakens a supereffective Electric-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 255);
			super.price = 20;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.ELECTRIC;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.ELECTRIC && Type.getAdvantage(user, victim, b) > 1) {
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class RindoBerry extends Item implements Berry, OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		RindoBerry() {
			super(ItemNamesies.RINDO_BERRY, "Weakens a supereffective Grass-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 256);
			super.price = 20;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.GRASS;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.GRASS && Type.getAdvantage(user, victim, b) > 1) {
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class YacheBerry extends Item implements Berry, OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		YacheBerry() {
			super(ItemNamesies.YACHE_BERRY, "Weakens a supereffective Ice-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 257);
			super.price = 20;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.ICE;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.ICE && Type.getAdvantage(user, victim, b) > 1) {
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class ChopleBerry extends Item implements Berry, OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		ChopleBerry() {
			super(ItemNamesies.CHOPLE_BERRY, "Weakens a supereffective Fighting-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 258);
			super.price = 20;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.FIGHTING;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.FIGHTING && Type.getAdvantage(user, victim, b) > 1) {
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class KebiaBerry extends Item implements Berry, OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		KebiaBerry() {
			super(ItemNamesies.KEBIA_BERRY, "Weakens a supereffective Poison-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 259);
			super.price = 20;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.POISON;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.POISON && Type.getAdvantage(user, victim, b) > 1) {
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class ShucaBerry extends Item implements Berry, OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		ShucaBerry() {
			super(ItemNamesies.SHUCA_BERRY, "Weakens a supereffective Ground-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 260);
			super.price = 20;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.GROUND;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.GROUND && Type.getAdvantage(user, victim, b) > 1) {
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class CobaBerry extends Item implements Berry, OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		CobaBerry() {
			super(ItemNamesies.COBA_BERRY, "Weakens a supereffective Flying-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 261);
			super.price = 20;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.FLYING;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.FLYING && Type.getAdvantage(user, victim, b) > 1) {
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class PayapaBerry extends Item implements Berry, OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		PayapaBerry() {
			super(ItemNamesies.PAYAPA_BERRY, "Weakens a supereffective Psychic-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 262);
			super.price = 20;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.PSYCHIC;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.PSYCHIC && Type.getAdvantage(user, victim, b) > 1) {
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class TangaBerry extends Item implements Berry, OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		TangaBerry() {
			super(ItemNamesies.TANGA_BERRY, "Weakens a supereffective Bug-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 263);
			super.price = 20;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.BUG;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.BUG && Type.getAdvantage(user, victim, b) > 1) {
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class ChartiBerry extends Item implements Berry, OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		ChartiBerry() {
			super(ItemNamesies.CHARTI_BERRY, "Weakens a supereffective Rock-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 264);
			super.price = 20;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.ROCK;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.ROCK && Type.getAdvantage(user, victim, b) > 1) {
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class KasibBerry extends Item implements Berry, OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		KasibBerry() {
			super(ItemNamesies.KASIB_BERRY, "Weakens a supereffective Ghost-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 265);
			super.price = 20;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.GHOST;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.GHOST && Type.getAdvantage(user, victim, b) > 1) {
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class HabanBerry extends Item implements Berry, OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		HabanBerry() {
			super(ItemNamesies.HABAN_BERRY, "Weakens a supereffective Dragon-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 266);
			super.price = 20;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.DRAGON;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.DRAGON && Type.getAdvantage(user, victim, b) > 1) {
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class ColburBerry extends Item implements Berry, OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		ColburBerry() {
			super(ItemNamesies.COLBUR_BERRY, "Weakens a supereffective Dark-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 267);
			super.price = 20;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.DARK;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.DARK && Type.getAdvantage(user, victim, b) > 1) {
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class BabiriBerry extends Item implements Berry, OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		BabiriBerry() {
			super(ItemNamesies.BABIRI_BERRY, "Weakens a supereffective Steel-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 268);
			super.price = 20;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.STEEL;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.STEEL && Type.getAdvantage(user, victim, b) > 1) {
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class ChilanBerry extends Item implements Berry, OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		ChilanBerry() {
			super(ItemNamesies.CHILAN_BERRY, "Weakens a supereffective Normal-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 269);
			super.price = 20;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.NORMAL;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.NORMAL && Type.getAdvantage(user, victim, b) > 1) {
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class RoseliBerry extends Item implements Berry, OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		RoseliBerry() {
			super(ItemNamesies.ROSELI_BERRY, "Weakens a supereffective Fairy-type attack against the holding Pok\u00e9mon.", BagCategory.BERRY, 270);
			super.price = 20;
		}

		public int naturalGiftPower() {
			return 80;
		}

		public Type naturalGiftType() {
			return Type.FAIRY;
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.FAIRY && Type.getAdvantage(user, victim, b) > 1) {
				b.addMessage(victim.getName() + "'s " + this.name + " decreased " + user.getName() + "'s attack!");
				victim.consumeItem(b);
				return .5;
			}
			
			return 1;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class LiechiBerry extends Item implements HealthTriggeredBerry {
		private static final long serialVersionUID = 1L;

		LiechiBerry() {
			super(ItemNamesies.LIECHI_BERRY, "If held by a Pok\u00e9mon, it raises its Attack stat in a pinch.", BagCategory.BERRY, 271);
			super.price = 20;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + Stat.ATTACK.getName() + " increased!";
		}

		public String getHoldSuccessMessage(Battle b, ActivePokemon p) {
			return "";
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user, CastSource source) {
			if (user.getAttributes().modifyStage(user, user, 1, Stat.ATTACK, b, source)) {
				return true;
			}
			
			return false;
		}

		public double healthTriggerRatio() {
			return 1/4.0;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
			if (!useHealthTriggerBerry(b, user, source)) {
				return false;
			}
			
			String message = "";
			switch (source) {
				case USE_ITEM:
					message = getSuccessMessage(user);
					break;
				case HELD_ITEM:
					message = getHoldSuccessMessage(b, user);
					break;
				default:
					Global.error("Use item and held item are the only valid cast sources for berries.");
				}
				
				b.addMessage(message, user);
				
				if (user.hasAbility(AbilityNamesies.CHEEK_POUCH) && !user.fullHealth()) {
					b.addMessage(user.getName() + "'s " + AbilityNamesies.CHEEK_POUCH.getName() + " restored its health!");
					user.healHealthFraction(1/3.0);
					b.addMessage("", user);
				}
				
				// Eat dat berry!!
				PokemonEffect.getEffect(EffectNamesies.EATEN_BERRY).cast(b, user, user, source, false);
				
				return true;
		}

		public int naturalGiftPower() {
			return 100;
		}

		public Type naturalGiftType() {
			return Type.GRASS;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class GanlonBerry extends Item implements HealthTriggeredBerry {
		private static final long serialVersionUID = 1L;

		GanlonBerry() {
			super(ItemNamesies.GANLON_BERRY, "If held by a Pok\u00e9mon, it raises its Defense stat in a pinch.", BagCategory.BERRY, 272);
			super.price = 20;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + Stat.DEFENSE.getName() + " increased!";
		}

		public String getHoldSuccessMessage(Battle b, ActivePokemon p) {
			return "";
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user, CastSource source) {
			if (user.getAttributes().modifyStage(user, user, 1, Stat.DEFENSE, b, source)) {
				return true;
			}
			
			return false;
		}

		public double healthTriggerRatio() {
			return 1/4.0;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
			if (!useHealthTriggerBerry(b, user, source)) {
				return false;
			}
			
			String message = "";
			switch (source) {
				case USE_ITEM:
					message = getSuccessMessage(user);
					break;
				case HELD_ITEM:
					message = getHoldSuccessMessage(b, user);
					break;
				default:
					Global.error("Use item and held item are the only valid cast sources for berries.");
				}
				
				b.addMessage(message, user);
				
				if (user.hasAbility(AbilityNamesies.CHEEK_POUCH) && !user.fullHealth()) {
					b.addMessage(user.getName() + "'s " + AbilityNamesies.CHEEK_POUCH.getName() + " restored its health!");
					user.healHealthFraction(1/3.0);
					b.addMessage("", user);
				}
				
				// Eat dat berry!!
				PokemonEffect.getEffect(EffectNamesies.EATEN_BERRY).cast(b, user, user, source, false);
				
				return true;
		}

		public int naturalGiftPower() {
			return 100;
		}

		public Type naturalGiftType() {
			return Type.ICE;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class SalacBerry extends Item implements HealthTriggeredBerry {
		private static final long serialVersionUID = 1L;

		SalacBerry() {
			super(ItemNamesies.SALAC_BERRY, "If held by a Pok\u00e9mon, it raises its Speed stat in a pinch.", BagCategory.BERRY, 273);
			super.price = 20;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + Stat.SPEED.getName() + " increased!";
		}

		public String getHoldSuccessMessage(Battle b, ActivePokemon p) {
			return "";
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user, CastSource source) {
			if (user.getAttributes().modifyStage(user, user, 1, Stat.SPEED, b, source)) {
				return true;
			}
			
			return false;
		}

		public double healthTriggerRatio() {
			return 1/4.0;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
			if (!useHealthTriggerBerry(b, user, source)) {
				return false;
			}
			
			String message = "";
			switch (source) {
				case USE_ITEM:
					message = getSuccessMessage(user);
					break;
				case HELD_ITEM:
					message = getHoldSuccessMessage(b, user);
					break;
				default:
					Global.error("Use item and held item are the only valid cast sources for berries.");
				}
				
				b.addMessage(message, user);
				
				if (user.hasAbility(AbilityNamesies.CHEEK_POUCH) && !user.fullHealth()) {
					b.addMessage(user.getName() + "'s " + AbilityNamesies.CHEEK_POUCH.getName() + " restored its health!");
					user.healHealthFraction(1/3.0);
					b.addMessage("", user);
				}
				
				// Eat dat berry!!
				PokemonEffect.getEffect(EffectNamesies.EATEN_BERRY).cast(b, user, user, source, false);
				
				return true;
		}

		public int naturalGiftPower() {
			return 100;
		}

		public Type naturalGiftType() {
			return Type.FIGHTING;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class PetayaBerry extends Item implements HealthTriggeredBerry {
		private static final long serialVersionUID = 1L;

		PetayaBerry() {
			super(ItemNamesies.PETAYA_BERRY, "If held by a Pok\u00e9mon, it raises its Speed stat in a pinch.", BagCategory.BERRY, 274);
			super.price = 20;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + Stat.SP_ATTACK.getName() + " increased!";
		}

		public String getHoldSuccessMessage(Battle b, ActivePokemon p) {
			return "";
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user, CastSource source) {
			if (user.getAttributes().modifyStage(user, user, 1, Stat.SP_ATTACK, b, source)) {
				return true;
			}
			
			return false;
		}

		public double healthTriggerRatio() {
			return 1/4.0;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
			if (!useHealthTriggerBerry(b, user, source)) {
				return false;
			}
			
			String message = "";
			switch (source) {
				case USE_ITEM:
					message = getSuccessMessage(user);
					break;
				case HELD_ITEM:
					message = getHoldSuccessMessage(b, user);
					break;
				default:
					Global.error("Use item and held item are the only valid cast sources for berries.");
				}
				
				b.addMessage(message, user);
				
				if (user.hasAbility(AbilityNamesies.CHEEK_POUCH) && !user.fullHealth()) {
					b.addMessage(user.getName() + "'s " + AbilityNamesies.CHEEK_POUCH.getName() + " restored its health!");
					user.healHealthFraction(1/3.0);
					b.addMessage("", user);
				}
				
				// Eat dat berry!!
				PokemonEffect.getEffect(EffectNamesies.EATEN_BERRY).cast(b, user, user, source, false);
				
				return true;
		}

		public int naturalGiftPower() {
			return 100;
		}

		public Type naturalGiftType() {
			return Type.POISON;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class ApicotBerry extends Item implements HealthTriggeredBerry {
		private static final long serialVersionUID = 1L;

		ApicotBerry() {
			super(ItemNamesies.APICOT_BERRY, "If held by a Pok\u00e9mon, it raises its Speed stat in a pinch.", BagCategory.BERRY, 275);
			super.price = 20;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + Stat.SP_DEFENSE.getName() + " increased!";
		}

		public String getHoldSuccessMessage(Battle b, ActivePokemon p) {
			return "";
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user, CastSource source) {
			if (user.getAttributes().modifyStage(user, user, 1, Stat.SP_DEFENSE, b, source)) {
				return true;
			}
			
			return false;
		}

		public double healthTriggerRatio() {
			return 1/4.0;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
			if (!useHealthTriggerBerry(b, user, source)) {
				return false;
			}
			
			String message = "";
			switch (source) {
				case USE_ITEM:
					message = getSuccessMessage(user);
					break;
				case HELD_ITEM:
					message = getHoldSuccessMessage(b, user);
					break;
				default:
					Global.error("Use item and held item are the only valid cast sources for berries.");
				}
				
				b.addMessage(message, user);
				
				if (user.hasAbility(AbilityNamesies.CHEEK_POUCH) && !user.fullHealth()) {
					b.addMessage(user.getName() + "'s " + AbilityNamesies.CHEEK_POUCH.getName() + " restored its health!");
					user.healHealthFraction(1/3.0);
					b.addMessage("", user);
				}
				
				// Eat dat berry!!
				PokemonEffect.getEffect(EffectNamesies.EATEN_BERRY).cast(b, user, user, source, false);
				
				return true;
		}

		public int naturalGiftPower() {
			return 100;
		}

		public Type naturalGiftType() {
			return Type.GROUND;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class MicleBerry extends Item implements HealthTriggeredBerry {
		private static final long serialVersionUID = 1L;

		MicleBerry() {
			super(ItemNamesies.MICLE_BERRY, "If held by a Pok\u00e9mon, it raises its Accuracy stat in a pinch.", BagCategory.BERRY, 276);
			super.price = 20;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s " + Stat.ACCURACY.getName() + " increased!";
		}

		public String getHoldSuccessMessage(Battle b, ActivePokemon p) {
			return "";
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user, CastSource source) {
			if (user.getAttributes().modifyStage(user, user, 1, Stat.ACCURACY, b, source)) {
				return true;
			}
			
			return false;
		}

		public double healthTriggerRatio() {
			return 1/4.0;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
			if (!useHealthTriggerBerry(b, user, source)) {
				return false;
			}
			
			String message = "";
			switch (source) {
				case USE_ITEM:
					message = getSuccessMessage(user);
					break;
				case HELD_ITEM:
					message = getHoldSuccessMessage(b, user);
					break;
				default:
					Global.error("Use item and held item are the only valid cast sources for berries.");
				}
				
				b.addMessage(message, user);
				
				if (user.hasAbility(AbilityNamesies.CHEEK_POUCH) && !user.fullHealth()) {
					b.addMessage(user.getName() + "'s " + AbilityNamesies.CHEEK_POUCH.getName() + " restored its health!");
					user.healHealthFraction(1/3.0);
					b.addMessage("", user);
				}
				
				// Eat dat berry!!
				PokemonEffect.getEffect(EffectNamesies.EATEN_BERRY).cast(b, user, user, source, false);
				
				return true;
		}

		public int naturalGiftPower() {
			return 100;
		}

		public Type naturalGiftType() {
			return Type.ROCK;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class KeeBerry extends Item implements Berry, TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		KeeBerry() {
			super(ItemNamesies.KEE_BERRY, "If held by a Pok\u00e9mon, this Berry will increase the Pok\u00e9mon's Defense stat when hit by a physical attack.", BagCategory.BERRY, 277);
			super.price = 20;
		}

		public boolean checkModify(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttack().getCategory() == MoveCategory.PHYSICAL && victim.getAttributes().modifyStage(victim, victim, 1, Stat.DEFENSE, b, CastSource.HELD_ITEM);
		}

		public int naturalGiftPower() {
			return 100;
		}

		public Type naturalGiftType() {
			return Type.FAIRY;
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (checkModify(b, user, victim)) {
				victim.consumeItem(b);
			}
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class MarangaBerry extends Item implements Berry, TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		MarangaBerry() {
			super(ItemNamesies.MARANGA_BERRY, "If held by a Pok\u00e9mon, this Berry will increase the Pok\u00e9mon's Sp. Defense stat when hit by a special attack.", BagCategory.BERRY, 278);
			super.price = 20;
		}

		public boolean checkModify(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttack().getCategory() == MoveCategory.SPECIAL && victim.getAttributes().modifyStage(victim, victim, 1, Stat.SP_DEFENSE, b, CastSource.HELD_ITEM);
		}

		public int naturalGiftPower() {
			return 100;
		}

		public Type naturalGiftType() {
			return Type.DARK;
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (checkModify(b, user, victim)) {
				victim.consumeItem(b);
			}
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class JabocaBerry extends Item implements Berry, TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		JabocaBerry() {
			super(ItemNamesies.JABOCA_BERRY, "If held by a Pok\u00e9mon and a physical attack lands, the attacker also takes damage.", BagCategory.BERRY, 279);
			super.price = 20;
		}

		public MoveCategory getCategory() {
			return MoveCategory.PHYSICAL;
		}

		public int naturalGiftPower() {
			return 100;
		}

		public Type naturalGiftType() {
			return Type.DRAGON;
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttack().getCategory() == getCategory()) {
				b.addMessage(user.getName() + " was hurt by " + victim.getName() + "'s " + this.name + "!");
				user.reduceHealthFraction(b, 1/8.0);
				victim.consumeItem(b);
			}
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class RowapBerry extends Item implements Berry, TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		RowapBerry() {
			super(ItemNamesies.ROWAP_BERRY, "If held by a Pok\u00e9mon and a special attack lands, the attacker also takes damage.", BagCategory.BERRY, 280);
			super.price = 20;
		}

		public MoveCategory getCategory() {
			return MoveCategory.SPECIAL;
		}

		public int naturalGiftPower() {
			return 100;
		}

		public Type naturalGiftType() {
			return Type.DARK;
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttack().getCategory() == getCategory()) {
				b.addMessage(user.getName() + " was hurt by " + victim.getName() + "'s " + this.name + "!");
				user.reduceHealthFraction(b, 1/8.0);
				victim.consumeItem(b);
			}
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class CustapBerry extends Item implements Berry, PriorityChangeEffect {
		private static final long serialVersionUID = 1L;

		CustapBerry() {
			super(ItemNamesies.CUSTAP_BERRY, "If held by a Pok\u00e9mon, it gets to move first just once in a pinch.", BagCategory.BERRY, 281);
			super.price = 20;
		}

		public int naturalGiftPower() {
			return 100;
		}

		public Type naturalGiftType() {
			return Type.GHOST;
		}

		public int changePriority(Battle b, ActivePokemon user, Integer priority) {
			if (user.getHPRatio() < 1/3.0) {
				if (this instanceof ConsumableItem) {
					user.consumeItem(b);
				}
				
				priority++;
			}
			
			return priority;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class EnigmaBerry extends Item implements Berry, TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		EnigmaBerry() {
			super(ItemNamesies.ENIGMA_BERRY, "If held by a Pok\u00e9mon, it restores its HP if it is hit by any supereffective attack.", BagCategory.BERRY, 282);
			super.price = 20;
		}

		public int naturalGiftPower() {
			return 100;
		}

		public Type naturalGiftType() {
			return Type.BUG;
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (!victim.fullHealth() && Type.getAdvantage(user, victim, b) > 1) {
				b.addMessage(victim.getName() + "'s " + this.name + " restored its health!");
				victim.healHealthFraction(.25);
				b.addMessage("", victim);
				victim.consumeItem(b);
			}
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class LansatBerry extends Item implements HealthTriggeredBerry {
		private static final long serialVersionUID = 1L;

		LansatBerry() {
			super(ItemNamesies.LANSAT_BERRY, "If held by a Pok\u00e9mon, it raises its critical-hit ratio in a pinch.", BagCategory.BERRY, 283);
			super.price = 20;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " is getting pumped!";
		}

		public String getHoldSuccessMessage(Battle b, ActivePokemon p) {
			return p.getName() + " is getting pumped due to its " + this.name + "!";
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user, CastSource source) {
			PokemonEffect.getEffect(EffectNamesies.RAISE_CRITS).cast(b, user, user, source, false);
			return true;
		}

		public double healthTriggerRatio() {
			return 1/4.0;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
			if (!useHealthTriggerBerry(b, user, source)) {
				return false;
			}
			
			String message = "";
			switch (source) {
				case USE_ITEM:
					message = getSuccessMessage(user);
					break;
				case HELD_ITEM:
					message = getHoldSuccessMessage(b, user);
					break;
				default:
					Global.error("Use item and held item are the only valid cast sources for berries.");
				}
				
				b.addMessage(message, user);
				
				if (user.hasAbility(AbilityNamesies.CHEEK_POUCH) && !user.fullHealth()) {
					b.addMessage(user.getName() + "'s " + AbilityNamesies.CHEEK_POUCH.getName() + " restored its health!");
					user.healHealthFraction(1/3.0);
					b.addMessage("", user);
				}
				
				// Eat dat berry!!
				PokemonEffect.getEffect(EffectNamesies.EATEN_BERRY).cast(b, user, user, source, false);
				
				return true;
		}

		public int naturalGiftPower() {
			return 100;
		}

		public Type naturalGiftType() {
			return Type.FLYING;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class StarfBerry extends Item implements HealthTriggeredBerry {
		private static final long serialVersionUID = 1L;
		private String holdMessage;
		private String useMessage;

		StarfBerry() {
			super(ItemNamesies.STARF_BERRY, "If held by a Pok\u00e9mon, it raises a random stat in a pinch.", BagCategory.BERRY, 284);
			super.price = 20;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return useMessage;
		}

		public String getHoldSuccessMessage(Battle b, ActivePokemon p) {
			return holdMessage;
		}

		public boolean useHealthTriggerBerry(Battle b, ActivePokemon user, CastSource source) {
			holdMessage = "";
			useMessage = "";
			
			int rand = (int)(Math.random()*(Stat.NUM_BATTLE_STATS + 1));
			
			// Raise crit
			if (rand == Stat.NUM_BATTLE_STATS) {
				PokemonEffect.getEffect(EffectNamesies.RAISE_CRITS).cast(b, user, user, source, false);
				holdMessage = user.getName() + " is getting pumped due to its " + this.name + "!";
				useMessage = user.getName() + " is getting pumped!";
				return true;
			}
			
			// Raise random battle stat
			Stat stat = Stat.getStat(rand, true);
			if (user.getAttributes().modifyStage(user, user, 1, stat, b, source)) {
				useMessage = user.getName() + "'s " + stat.getName() + " increased!";
				return true;
			}
			
			return false;
		}

		public double healthTriggerRatio() {
			return 1/4.0;
		}

		public boolean gainBerryEffect(Battle b, ActivePokemon user, CastSource source) {
			if (!useHealthTriggerBerry(b, user, source)) {
				return false;
			}
			
			String message = "";
			switch (source) {
				case USE_ITEM:
					message = getSuccessMessage(user);
					break;
				case HELD_ITEM:
					message = getHoldSuccessMessage(b, user);
					break;
				default:
					Global.error("Use item and held item are the only valid cast sources for berries.");
				}
				
				b.addMessage(message, user);
				
				if (user.hasAbility(AbilityNamesies.CHEEK_POUCH) && !user.fullHealth()) {
					b.addMessage(user.getName() + "'s " + AbilityNamesies.CHEEK_POUCH.getName() + " restored its health!");
					user.healHealthFraction(1/3.0);
					b.addMessage("", user);
				}
				
				// Eat dat berry!!
				PokemonEffect.getEffect(EffectNamesies.EATEN_BERRY).cast(b, user, user, source, false);
				
				return true;
		}

		public int naturalGiftPower() {
			return 100;
		}

		public Type naturalGiftType() {
			return Type.PSYCHIC;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			gainBerryEffect(b, pelted, CastSource.USE_ITEM);
		}
	}

	private static class CometShard extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		CometShard() {
			super(ItemNamesies.COMET_SHARD, "A shard which fell to the ground when a comet approached. A maniac will buy it for a high price.", BagCategory.MISC, 285);
			super.price = 120000;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class TinyMushroom extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		TinyMushroom() {
			super(ItemNamesies.TINY_MUSHROOM, "A small and rare mushroom. It is sought after by collectors.", BagCategory.MISC, 286);
			super.price = 500;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class BigMushroom extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		BigMushroom() {
			super(ItemNamesies.BIG_MUSHROOM, "A large and rare mushroom. It is sought after by collectors.", BagCategory.MISC, 287);
			super.price = 5000;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class BalmMushroom extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		BalmMushroom() {
			super(ItemNamesies.BALM_MUSHROOM, "A rare mushroom which gives off a nice fragrance. A maniac will buy it for a high price.", BagCategory.MISC, 288);
			super.price = 50000;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class Nugget extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		Nugget() {
			super(ItemNamesies.NUGGET, "A nugget of pure gold that gives off a lustrous gleam. It can be sold at a high price to shops.", BagCategory.MISC, 289);
			super.price = 10000;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class BigNugget extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		BigNugget() {
			super(ItemNamesies.BIG_NUGGET, "A big nugget of pure gold that gives off a lustrous gleam. A maniac will buy it for a high price.", BagCategory.MISC, 290);
			super.price = 60000;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class Pearl extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		Pearl() {
			super(ItemNamesies.PEARL, "A somewhat-small pearl that sparkles in a pretty silver color. It can be sold cheaply to shops.", BagCategory.MISC, 291);
			super.price = 1400;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class BigPearl extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		BigPearl() {
			super(ItemNamesies.BIG_PEARL, "A quite-large pearl that sparkles in a pretty silver color. It can be sold at a high price to shops.", BagCategory.MISC, 292);
			super.price = 7500;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class Stardust extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		Stardust() {
			super(ItemNamesies.STARDUST, "Lovely, red-colored sand with a loose, silky feel. It can be sold at a high price to shops.", BagCategory.MISC, 293);
			super.price = 2000;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class StarPiece extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		StarPiece() {
			super(ItemNamesies.STAR_PIECE, "A shard of a pretty gem that sparkles in a red color. It can be sold at a high price to shops.", BagCategory.MISC, 294);
			super.price = 9800;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class RareBone extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		RareBone() {
			super(ItemNamesies.RARE_BONE, "A bone that is extremely valuable for Pok\u00e9mon archeology. It can be sold for a high price to shops.", BagCategory.MISC, 295);
			super.price = 10000;
		}

		public int flingDamage() {
			return 100;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class Honey extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		Honey() {
			super(ItemNamesies.HONEY, "A sweet honey with a lush aroma that attracts wild Pok\u00e9mon when it is used in grass, caves, or on special trees.", BagCategory.MISC, 296);
			super.price = 100;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
			// TODO: We need this item to do something (not in the fling effect, that's only there so I can put this todo in from le generator)
		}
	}

	private static class Eviolite extends Item implements HoldItem, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		Eviolite() {
			super(ItemNamesies.EVIOLITE, "A mysterious evolutionary lump. When held, it raises the Defense and Sp. Def of a Pok\u00e9mon that can still evolve.", BagCategory.MISC, 297);
			super.price = 200;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.DEFENSE || s == Stat.SP_DEFENSE;
		}

		public int flingDamage() {
			return 40;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && p.getPokemonInfo().getEvolution().canEvolve()) {
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class HeartScale extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		HeartScale() {
			super(ItemNamesies.HEART_SCALE, "A pretty, heart-shaped scale that is extremely rare. It glows faintly in the colors of the rainbow.", BagCategory.MISC, 298);
			super.price = 100;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class Repel extends Item implements HoldItem, TrainerUseItem {
		private static final long serialVersionUID = 1L;

		Repel() {
			super(ItemNamesies.REPEL, "An item that prevents weak wild Pok\u00e9mon from appearing for 100 steps after its use.", BagCategory.MISC, 299);
			super.price = 350;
		}

		public int repelSteps() {
			return 100;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "Weak wild Pok\u00e9mon will not appear for " + repelSteps() + " steps!";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(Trainer t) {
			if (!(t instanceof CharacterData)) {
				Global.error("Only the character should be using a Repel item");
			}
			
			CharacterData player = (CharacterData) t;
			if (player.isUsingRepel()) {
				return false;
			}
			
			player.addRepelSteps(repelSteps());
			return true;
		}
	}

	private static class SuperRepel extends Item implements HoldItem, TrainerUseItem {
		private static final long serialVersionUID = 1L;

		SuperRepel() {
			super(ItemNamesies.SUPER_REPEL, "An item that prevents weak wild Pok\u00e9mon from appearing for 200 steps after its use.", BagCategory.MISC, 300);
			super.price = 500;
		}

		public int repelSteps() {
			return 200;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "Weak wild Pok\u00e9mon will not appear for " + repelSteps() + " steps!";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(Trainer t) {
			if (!(t instanceof CharacterData)) {
				Global.error("Only the character should be using a Repel item");
			}
			
			CharacterData player = (CharacterData) t;
			if (player.isUsingRepel()) {
				return false;
			}
			
			player.addRepelSteps(repelSteps());
			return true;
		}
	}

	private static class MaxRepel extends Item implements HoldItem, TrainerUseItem {
		private static final long serialVersionUID = 1L;

		MaxRepel() {
			super(ItemNamesies.MAX_REPEL, "An item that prevents weak wild Pok\u00e9mon from appearing for 250 steps after its use.", BagCategory.MISC, 301);
			super.price = 700;
		}

		public int repelSteps() {
			return 250;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return "Weak wild Pok\u00e9mon will not appear for " + repelSteps() + " steps!";
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean use(Trainer t) {
			if (!(t instanceof CharacterData)) {
				Global.error("Only the character should be using a Repel item");
			}
			
			CharacterData player = (CharacterData) t;
			if (player.isUsingRepel()) {
				return false;
			}
			
			player.addRepelSteps(repelSteps());
			return true;
		}
	}

	private static class AbilityCapsule extends Item implements PokemonUseItem {
		private static final long serialVersionUID = 1L;

		AbilityCapsule() {
			super(ItemNamesies.ABILITY_CAPSULE, "A capsule that allows a Pok\u00e9mon with two Abilities to switch between these Abilities when it is used.", BagCategory.MISC, 302);
			super.price = 1000;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + "'s ability was changed to " + p.getAbility().getName() + "!";
		}

		public boolean use(CharacterData player, ActivePokemon p) {
			Ability other = Ability.getOtherAbility(p);
			if (other.namesies() == AbilityNamesies.NO_ABILITY) {
				return false;
			}
			
			p.assignAbility(other);
			return true;
		}
	}

	private static class AssaultVest extends Item implements HoldItem, AttackSelectionEffect, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		AssaultVest() {
			super(ItemNamesies.ASSAULT_VEST, "An item to be held by a Pok\u00e9mon. This offensive vest raises Sp. Def but prevents the use of status moves.", BagCategory.MISC, 303);
			super.price = 1000;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SP_DEFENSE;
		}

		public int flingDamage() {
			return 30;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}

		public boolean usable(ActivePokemon p, Move m) {
			return m.getAttack().getCategory() != MoveCategory.STATUS;
		}

		public String getUnusableMessage(ActivePokemon p) {
			return p.getName() + "'s " + this.name + " prevents the use of status moves!";
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && true) {
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	private static class PowerHerb extends Item implements HoldItem {
		private static final long serialVersionUID = 1L;

		PowerHerb() {
			super(ItemNamesies.POWER_HERB, "A single-use item to be held by a Pokémon. It allows the immediate use of a move that charges on the first turn.", BagCategory.MISC, 304);
			super.price = 100;
		}

		public int flingDamage() {
			return 10;
		}

		public void flingEffect(Battle b, ActivePokemon pelted) {
		}
	}

	private static class HoneClawsTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		HoneClawsTM() {
			super(ItemNamesies.HONE_CLAWS_TM, "The user sharpens its claws to boost its Attack stat and accuracy.", BagCategory.TM, 2015);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Hone Claws"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class DragonClawTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		DragonClawTM() {
			super(ItemNamesies.DRAGON_CLAW_TM, "The user slashes the target with huge, sharp claws.", BagCategory.TM, 2014);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Dragon Claw"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class PsyshockTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		PsyshockTM() {
			super(ItemNamesies.PSYSHOCK_TM, "The user materializes an odd psychic wave to attack the target. This attack does physical damage.", BagCategory.TM, 2010);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Psyshock"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class CalmMindTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		CalmMindTM() {
			super(ItemNamesies.CALM_MIND_TM, "The user quietly focuses its mind and calms its spirit to raise its Sp. Atk and Sp. Def stats.", BagCategory.TM, 2010);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Calm Mind"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class RoarTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		RoarTM() {
			super(ItemNamesies.ROAR_TM, "The target is scared off and replaced by another Pokémon in its party. In the wild, the battle ends.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Roar"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ToxicTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		ToxicTM() {
			super(ItemNamesies.TOXIC_TM, "A move that leaves the target badly poisoned. Its poison damage worsens every turn.", BagCategory.TM, 2007);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Toxic"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class HailTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		HailTM() {
			super(ItemNamesies.HAIL_TM, "The user summons a hailstorm lasting five turns. It damages all Pokémon except the Ice type.", BagCategory.TM, 2005);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Hail"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class BulkUpTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		BulkUpTM() {
			super(ItemNamesies.BULK_UP_TM, "The user tenses its muscles to bulk up its body, boosting both its Attack and Defense stats.", BagCategory.TM, 2006);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Bulk Up"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class VenoshockTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		VenoshockTM() {
			super(ItemNamesies.VENOSHOCK_TM, "The user drenches the target in a special poisonous liquid. Its power is doubled if the target is poisoned.", BagCategory.TM, 2007);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Venoshock"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class HiddenPowerTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		HiddenPowerTM() {
			super(ItemNamesies.HIDDEN_POWER_TM, "A unique attack that varies in type and intensity depending on the Pokémon using it.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Hidden Power"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SunnyDayTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		SunnyDayTM() {
			super(ItemNamesies.SUNNY_DAY_TM, "The user intensifies the sun for five turns, powering up Fire-type moves.", BagCategory.TM, 2001);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Sunny Day"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class TauntTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		TauntTM() {
			super(ItemNamesies.TAUNT_TM, "The target is taunted into a rage that allows it to use only attack moves for three turns.", BagCategory.TM, 2015);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Taunt"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class IceBeamTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		IceBeamTM() {
			super(ItemNamesies.ICE_BEAM_TM, "The target is struck with an icy-cold beam of energy. It may also freeze the target solid.", BagCategory.TM, 2005);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Ice Beam"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class BlizzardTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		BlizzardTM() {
			super(ItemNamesies.BLIZZARD_TM, "A howling blizzard is summoned to strike the opposing team. It may also freeze them solid.", BagCategory.TM, 2005);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Blizzard"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class HyperBeamTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		HyperBeamTM() {
			super(ItemNamesies.HYPER_BEAM_TM, "The target is attacked with a powerful beam. The user must rest on the next turn to regain its energy.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Hyper Beam"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class LightScreenTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		LightScreenTM() {
			super(ItemNamesies.LIGHT_SCREEN_TM, "A wondrous wall of light is put up to suppress damage from special attacks for five turns.", BagCategory.TM, 2010);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Light Screen"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ProtectTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		ProtectTM() {
			super(ItemNamesies.PROTECT_TM, "It enables the user to evade all attacks. Its chance of failing rises if it is used in succession.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Protect"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class RainDanceTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		RainDanceTM() {
			super(ItemNamesies.RAIN_DANCE_TM, "The user summons a heavy rain that falls for five turns, powering up Water-type moves.", BagCategory.TM, 2002);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Rain Dance"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class RoostTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		RoostTM() {
			super(ItemNamesies.ROOST_TM, "The user lands and rests its body. It restores the user's HP by up to half of its max HP.", BagCategory.TM, 2009);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Roost"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SafeguardTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		SafeguardTM() {
			super(ItemNamesies.SAFEGUARD_TM, "The user creates a protective field that prevents status problems for five turns.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Safeguard"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SolarBeamTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		SolarBeamTM() {
			super(ItemNamesies.SOLAR_BEAM_TM, "A two-turn attack. The user gathers light, then blasts a bundled beam on the second turn.", BagCategory.TM, 2004);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Solar Beam"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SmackDownTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		SmackDownTM() {
			super(ItemNamesies.SMACK_DOWN_TM, "The user throws a stone or projectile to attack an opponent. A flying Pokémon will fall to the ground when hit.", BagCategory.TM, 2012);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Smack Down"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ThunderboltTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		ThunderboltTM() {
			super(ItemNamesies.THUNDERBOLT_TM, "A strong electric blast is loosed at the target. It may also leave the target with paralysis.", BagCategory.TM, 2003);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Thunderbolt"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ThunderTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		ThunderTM() {
			super(ItemNamesies.THUNDER_TM, "A wicked thunderbolt is dropped on the target to inflict damage. It may also leave the target with paralysis.", BagCategory.TM, 2003);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Thunder"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class EarthquakeTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		EarthquakeTM() {
			super(ItemNamesies.EARTHQUAKE_TM, "The user sets off an earthquake that strikes those around it.", BagCategory.TM, 2008);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Earthquake"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class DigTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		DigTM() {
			super(ItemNamesies.DIG_TM, "The user burrows, then attacks on the second turn. It can also be used to exit dungeons.", BagCategory.TM, 2008);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Dig"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class PsychicTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		PsychicTM() {
			super(ItemNamesies.PSYCHIC_TM, "The target is hit by a strong telekinetic force. It may also reduce the target's Sp. Def stat.", BagCategory.TM, 2010);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Psychic"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ShadowBallTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		ShadowBallTM() {
			super(ItemNamesies.SHADOW_BALL_TM, "The user hurls a shadowy blob at the target. It may also lower the target's Sp. Def stat.", BagCategory.TM, 2013);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Shadow Ball"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class BrickBreakTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		BrickBreakTM() {
			super(ItemNamesies.BRICK_BREAK_TM, "The user attacks with a swift chop. It can also break any barrier such as Light Screen and Reflect.", BagCategory.TM, 2006);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Brick Break"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class DoubleTeamTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		DoubleTeamTM() {
			super(ItemNamesies.DOUBLE_TEAM_TM, "By moving rapidly, the user makes illusory copies of itself to raise its evasiveness.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Double Team"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ReflectTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		ReflectTM() {
			super(ItemNamesies.REFLECT_TM, "A wondrous wall of light is put up to suppress damage from physical attacks for five turns.", BagCategory.TM, 2010);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Reflect"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SludgeWaveTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		SludgeWaveTM() {
			super(ItemNamesies.SLUDGE_WAVE_TM, "It swamps the area around the user with a giant sludge wave. It may also poison those hit.", BagCategory.TM, 2007);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Sludge Wave"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class FlamethrowerTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		FlamethrowerTM() {
			super(ItemNamesies.FLAMETHROWER_TM, "The target is scorched with an intense blast of fire. It may also leave the target with a burn.", BagCategory.TM, 2001);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Flamethrower"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SludgeBombTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		SludgeBombTM() {
			super(ItemNamesies.SLUDGE_BOMB_TM, "Unsanitary sludge is hurled at the target. It may also poison the target.", BagCategory.TM, 2007);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Sludge Bomb"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SandstormTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		SandstormTM() {
			super(ItemNamesies.SANDSTORM_TM, "A five-turn sandstorm is summoned to hurt all combatants except the Rock, Ground, and Steel types.", BagCategory.TM, 2012);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Sandstorm"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class FireBlastTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		FireBlastTM() {
			super(ItemNamesies.FIRE_BLAST_TM, "The target is attacked with an intense blast of all-consuming fire. It may also leave the target with a burn.", BagCategory.TM, 2001);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Fire Blast"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class RockTombTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		RockTombTM() {
			super(ItemNamesies.ROCK_TOMB_TM, "Boulders are hurled at the target. It also lowers the target's Speed by preventing its movement.", BagCategory.TM, 2012);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Rock Tomb"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class AerialAceTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		AerialAceTM() {
			super(ItemNamesies.AERIAL_ACE_TM, "The user confounds the foe with speed, then slashes. The attack lands without fail.", BagCategory.TM, 2009);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Aerial Ace"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class TormentTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		TormentTM() {
			super(ItemNamesies.TORMENT_TM, "The user torments and enrages the target, making it incapable of using the same move twice in a row.", BagCategory.TM, 2015);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Torment"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class FacadeTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		FacadeTM() {
			super(ItemNamesies.FACADE_TM, "An attack move that doubles its power if the user is poisoned, burned, or has paralysis.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Facade"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class FlameChargeTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		FlameChargeTM() {
			super(ItemNamesies.FLAME_CHARGE_TM, "The user cloaks itself with flame and attacks. Building up more power, it raises the user's Speed stat.", BagCategory.TM, 2001);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Flame Charge"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class RestTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		RestTM() {
			super(ItemNamesies.REST_TM, "The user goes to sleep for two turns. It fully restores the user's HP and heals any status problem.", BagCategory.TM, 2010);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Rest"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class AttractTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		AttractTM() {
			super(ItemNamesies.ATTRACT_TM, "If it is the opposite gender of the user, the target becomes infatuated and less likely to attack.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Attract"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ThiefTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		ThiefTM() {
			super(ItemNamesies.THIEF_TM, "The user attacks and steals the target's held item simultaneously. It can't steal if the user holds an item.", BagCategory.TM, 2015);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Thief"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class LowSweepTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		LowSweepTM() {
			super(ItemNamesies.LOW_SWEEP_TM, "The user attacks the target's legs swiftly, reducing the target's Speed stat.", BagCategory.TM, 2006);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Low Sweep"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class RoundTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		RoundTM() {
			super(ItemNamesies.ROUND_TM, "The user attacks the target with a song.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Round"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class EchoedVoiceTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		EchoedVoiceTM() {
			super(ItemNamesies.ECHOED_VOICE_TM, "The user attacks the target with an echoing voice. If this move is used every turn, it does greater damage.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Echoed Voice"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class OverheatTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		OverheatTM() {
			super(ItemNamesies.OVERHEAT_TM, "The user attacks the target at full power. The attack's recoil harshly reduces the user's Sp. Atk stat.", BagCategory.TM, 2001);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Overheat"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SteelWingTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		SteelWingTM() {
			super(ItemNamesies.STEEL_WING_TM, "The target is hit with wings of steel. It may also raise the user's Defense stat.", BagCategory.TM, 2016);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Steel Wing"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class FocusBlastTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		FocusBlastTM() {
			super(ItemNamesies.FOCUS_BLAST_TM, "The user heightens its mental focus and unleashes its power. It may also lower the target's Sp. Def.", BagCategory.TM, 2006);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Focus Blast"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class EnergyBallTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		EnergyBallTM() {
			super(ItemNamesies.ENERGY_BALL_TM, "The user draws power from nature and fires it at the target. It may also lower the target's Sp. Def.", BagCategory.TM, 2004);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Energy Ball"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class FalseSwipeTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		FalseSwipeTM() {
			super(ItemNamesies.FALSE_SWIPE_TM, "A restrained attack that prevents the target from fainting. The target is left with at least 1 HP.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("False Swipe"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ScaldTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		ScaldTM() {
			super(ItemNamesies.SCALD_TM, "The user shoots boiling hot water at its target. It may also leave the target with a burn.", BagCategory.TM, 2002);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Scald"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class FlingTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		FlingTM() {
			super(ItemNamesies.FLING_TM, "The user flings its held item at the target to attack. Its power and effects depend on the item.", BagCategory.TM, 2015);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Fling"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ChargeBeamTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		ChargeBeamTM() {
			super(ItemNamesies.CHARGE_BEAM_TM, "The user attacks with an electric charge. The user may use any remaining electricity to raise its Sp. Atk stat.", BagCategory.TM, 2003);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Charge Beam"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SkyDropTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		SkyDropTM() {
			super(ItemNamesies.SKY_DROP_TM, "The user takes the target into the sky, then slams it into the ground.", BagCategory.TM, 2009);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Sky Drop"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class IncinerateTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		IncinerateTM() {
			super(ItemNamesies.INCINERATE_TM, "The user attacks the target with fire. If the target is holding a Berry, the Berry becomes burnt up and unusable.", BagCategory.TM, 2001);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Incinerate"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class WillOWispTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		WillOWispTM() {
			super(ItemNamesies.WILL_O_WISP_TM, "The user shoots a sinister, bluish-white flame at the target to inflict a burn.", BagCategory.TM, 2001);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Will-O-Wisp"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class AcrobaticsTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		AcrobaticsTM() {
			super(ItemNamesies.ACROBATICS_TM, "The user nimbly strikes the target. If the user is not holding an item, this attack inflicts massive damage.", BagCategory.TM, 2009);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Acrobatics"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class EmbargoTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		EmbargoTM() {
			super(ItemNamesies.EMBARGO_TM, "It prevents the target from using its held item. Its Trainer is also prevented from using items on it.", BagCategory.TM, 2015);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Embargo"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ExplosionTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		ExplosionTM() {
			super(ItemNamesies.EXPLOSION_TM, "The user explodes to inflict damage on those around it. The user faints upon using this move.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Explosion"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ShadowClawTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		ShadowClawTM() {
			super(ItemNamesies.SHADOW_CLAW_TM, "The user slashes with a sharp claw made from shadows. Critical hits land more easily.", BagCategory.TM, 2013);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Shadow Claw"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class PaybackTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		PaybackTM() {
			super(ItemNamesies.PAYBACK_TM, "If the user moves after the target, this attack's power will be doubled.", BagCategory.TM, 2015);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Payback"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class RetaliateTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		RetaliateTM() {
			super(ItemNamesies.RETALIATE_TM, "The user gets revenge for a fainted ally. If an ally fainted in the previous turn, this attack's damage increases.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Retaliate"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class GigaImpactTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		GigaImpactTM() {
			super(ItemNamesies.GIGA_IMPACT_TM, "The user charges at the target using every bit of its power. The user must rest on the next turn.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Giga Impact"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class RockPolishTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		RockPolishTM() {
			super(ItemNamesies.ROCK_POLISH_TM, "The user polishes its body to reduce drag. It can sharply raise the Speed stat.", BagCategory.TM, 2012);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Rock Polish"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class FlashTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		FlashTM() {
			super(ItemNamesies.FLASH_TM, "The user flashes a bright light that cuts the target's accuracy. It can also be used to illuminate caves.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Flash"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class StoneEdgeTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		StoneEdgeTM() {
			super(ItemNamesies.STONE_EDGE_TM, "The user stabs the foe with sharpened stones from below. It has a high critical-hit ratio.", BagCategory.TM, 2012);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Stone Edge"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class VoltSwitchTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		VoltSwitchTM() {
			super(ItemNamesies.VOLT_SWITCH_TM, "After making its attack, the user rushes back to switch places with a party Pokémon in waiting.", BagCategory.TM, 2003);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Volt Switch"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ThunderWaveTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		ThunderWaveTM() {
			super(ItemNamesies.THUNDER_WAVE_TM, "A weak electric charge is launched at the target. It causes paralysis if it hits.", BagCategory.TM, 2003);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Thunder Wave"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class GyroBallTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		GyroBallTM() {
			super(ItemNamesies.GYRO_BALL_TM, "The user tackles the target with a high-speed spin. The slower the user, the greater the damage.", BagCategory.TM, 2016);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Gyro Ball"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SwordsDanceTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		SwordsDanceTM() {
			super(ItemNamesies.SWORDS_DANCE_TM, "A frenetic dance to uplift the fighting spirit. It sharply raises the user's Attack stat.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Swords Dance"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class StruggleBugTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		StruggleBugTM() {
			super(ItemNamesies.STRUGGLE_BUG_TM, "While resisting, the user attacks the opposing Pokémon. The targets' Sp. Atk stat is reduced.", BagCategory.TM, 2011);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Struggle Bug"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class PsychUpTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		PsychUpTM() {
			super(ItemNamesies.PSYCH_UP_TM, "The user hypnotizes itself into copying any stat change made by the target.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Psych Up"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class BulldozeTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		BulldozeTM() {
			super(ItemNamesies.BULLDOZE_TM, "The user stomps down on the ground and attacks everything in the area. Hit Pokémon's Speed stat is reduced.", BagCategory.TM, 2008);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Bulldoze"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class FrostBreathTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		FrostBreathTM() {
			super(ItemNamesies.FROST_BREATH_TM, "The user blows a cold breath on the target. This attack always results in a critical hit.", BagCategory.TM, 2005);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Frost Breath"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class RockSlideTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		RockSlideTM() {
			super(ItemNamesies.ROCK_SLIDE_TM, "Large boulders are hurled at the opposing team to inflict damage. It may also make the targets flinch.", BagCategory.TM, 2012);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Rock Slide"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class XScissorTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		XScissorTM() {
			super(ItemNamesies.X_SCISSOR_TM, "The user slashes at the target by crossing its scythes or claws as if they were a pair of scissors.", BagCategory.TM, 2011);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("X-Scissor"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class DragonTailTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		DragonTailTM() {
			super(ItemNamesies.DRAGON_TAIL_TM, "The user knocks away the target and drags out another Pokémon in its party. In the wild, the battle ends.", BagCategory.TM, 2014);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Dragon Tail"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class InfestationTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		InfestationTM() {
			super(ItemNamesies.INFESTATION_TM, "The target is infested and attacked for four to five turns. The target can't flee during this time.", BagCategory.TM, 2011);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Infestation"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class PoisonJabTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		PoisonJabTM() {
			super(ItemNamesies.POISON_JAB_TM, "The target is stabbed with a tentacle or arm steeped in poison. It may also poison the target.", BagCategory.TM, 2007);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Poison Jab"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class DreamEaterTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		DreamEaterTM() {
			super(ItemNamesies.DREAM_EATER_TM, "The user eats the dreams of a sleeping target. It absorbs half the damage caused to heal the user's HP.", BagCategory.TM, 2010);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Dream Eater"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class GrassKnotTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		GrassKnotTM() {
			super(ItemNamesies.GRASS_KNOT_TM, "The user snares the target with grass and trips it. The heavier the target, the greater the damage.", BagCategory.TM, 2004);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Grass Knot"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SwaggerTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		SwaggerTM() {
			super(ItemNamesies.SWAGGER_TM, "The user enrages and confuses the target. However, it also sharply raises the target's Attack stat.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Swagger"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SleepTalkTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		SleepTalkTM() {
			super(ItemNamesies.SLEEP_TALK_TM, "While it is asleep, the user randomly uses one of the moves it knows.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Sleep Talk"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class UTurnTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		UTurnTM() {
			super(ItemNamesies.U_TURN_TM, "After making its attack, the user rushes back to switch places with a party Pokémon in waiting.", BagCategory.TM, 2011);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("U-turn"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SubstituteTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		SubstituteTM() {
			super(ItemNamesies.SUBSTITUTE_TM, "The user makes a copy of itself using some of its HP. The copy serves as the user's decoy.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Substitute"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class FlashCannonTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		FlashCannonTM() {
			super(ItemNamesies.FLASH_CANNON_TM, "The user gathers all its light energy and releases it at once. It may also lower the target's Sp. Def stat.", BagCategory.TM, 2016);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Flash Cannon"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class TrickRoomTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		TrickRoomTM() {
			super(ItemNamesies.TRICK_ROOM_TM, "The user creates a bizarre area in which slower Pokémon get to move first for five turns.", BagCategory.TM, 2010);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Trick Room"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class WildChargeTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		WildChargeTM() {
			super(ItemNamesies.WILD_CHARGE_TM, "The user shrouds itself in electricity and smashes into its target. It also damages the user a little.", BagCategory.TM, 2003);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Wild Charge"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class RockSmashTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		RockSmashTM() {
			super(ItemNamesies.ROCK_SMASH_TM, "The user attacks with a punch that can shatter a rock. It may also lower the target's Defense stat.", BagCategory.TM, 2006);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Rock Smash"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SnarlTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		SnarlTM() {
			super(ItemNamesies.SNARL_TM, "The user yells as if it is ranting about something, making the target's Sp. Atk stat decrease.", BagCategory.TM, 2015);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Snarl"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class NaturePowerTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		NaturePowerTM() {
			super(ItemNamesies.NATURE_POWER_TM, "An attack that makes use of nature's power. Its effects vary depending on the user's environment.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Nature Power"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class DarkPulseTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		DarkPulseTM() {
			super(ItemNamesies.DARK_PULSE_TM, "The user releases a horrible aura imbued with dark thoughts. It may also make the target flinch.", BagCategory.TM, 2015);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Dark Pulse"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class PowerUpPunchTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		PowerUpPunchTM() {
			super(ItemNamesies.POWER_UP_PUNCH_TM, "Striking opponents over and over makes the user's fists harder. Hitting a target raises the Attack stat.", BagCategory.TM, 2006);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Power-Up Punch"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class DazzlingGleamTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		DazzlingGleamTM() {
			super(ItemNamesies.DAZZLING_GLEAM_TM, "The user damages opposing Pokémon by emitting a powerful flash.", BagCategory.TM, 2017);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Dazzling Gleam"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class ConfideTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		ConfideTM() {
			super(ItemNamesies.CONFIDE_TM, "The user tells the target a secret, and the target loses its ability to concentrate. This lowers the target's Sp. Atk stat.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Confide"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class CutTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		CutTM() {
			super(ItemNamesies.CUT_TM, "The target is cut with a scythe or a claw. It can also be used to cut down thin trees.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Cut"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class FlyTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		FlyTM() {
			super(ItemNamesies.FLY_TM, "The user soars, then strikes its target on the second turn. It can also be used for flying to any familiar town.", BagCategory.TM, 2009);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Fly"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class SurfTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		SurfTM() {
			super(ItemNamesies.SURF_TM, "It swamps the area around the user with a giant wave. It can also be used for crossing water.", BagCategory.TM, 2002);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Surf"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class StrengthTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		StrengthTM() {
			super(ItemNamesies.STRENGTH_TM, "The target is slugged with a punch thrown at maximum power. It can also be used to move heavy boulders.", BagCategory.TM, 2000);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Strength"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}

	private static class WaterfallTM extends Item implements MoveUseItem {
		private static final long serialVersionUID = 1L;

		WaterfallTM() {
			super(ItemNamesies.WATERFALL_TM, "The user charges at the target and may make it flinch. It can also be used to climb a waterfall.", BagCategory.TM, 2002);
		}

		public Attack getAttack() {
			return Attack.getAttack(AttackNamesies.getValueOf("Waterfall"));
		}

		public boolean use(ActivePokemon p, Move m) {
			Attack attack = getAttack();
			
			// Cannot learn if you already know the move
			if (p.hasActualMove(attack.namesies())) {
				return false;
			}
			
			// Cannot learn if the TM is not compatible with the Pokemon
			if (!p.getPokemonInfo().canLearnMove(attack.namesies())) {
				return false;
			}
			
			Move tmMove = new Move(attack);
			List<Move> moveList = p.getActualMoves();
			
			// If they don't have a full move list, append to the end
			if (moveList.size() < Move.MAX_MOVES) {
				p.addMove(null, tmMove, moveList.size());
				return true;
			}
			
			// Otherwise, go through their moves and find the one that matches and replace with the TM move
			for (int i = 0; i < moveList.size(); i++) {
				if (moveList.get(i).getAttack().namesies() == m.getAttack().namesies()) {
					p.addMove(null, tmMove, i);
					return true;
				}
			}
			
			// Did not find move to replace -- throw error
			Global.error("ActivePokemon " + p.getName() + " does not have move to replace " + m.getAttack().getName());
			return false;
		}

		public String getSuccessMessage(ActivePokemon p) {
			return p.getName() + " learned " + getAttack().getName() + "!";
		}
	}
}
