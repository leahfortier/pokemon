package trainer;

import battle.Battle;
import battle.effect.generic.EffectInterfaces.EndBattleEffect;
import battle.effect.generic.EffectNamesies;
import gui.view.ViewMode;
import item.ItemNamesies;
import item.use.BallItem;
import map.AreaData;
import map.Direction;
import map.MapName;
import map.entity.movable.PlayerEntity;
import map.overworld.OverworldTool;
import map.triggers.battle.FishingTrigger;
import map.triggers.TriggerType;
import message.MessageUpdate;
import message.MessageUpdate.Update;
import message.Messages;
import pattern.SimpleMapTransition;
import pattern.action.UpdateMatcher;
import pokemon.ActivePokemon;
import pokemon.PC;
import pokemon.ability.AbilityNamesies;
import pokemon.breeding.DayCareCenter;
import pokemon.evolution.BaseEvolution;
import trainer.pokedex.Pokedex;
import util.Point;
import util.RandomUtils;
import util.StringUtils;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class Player extends Trainer implements Serializable {
	private static final long serialVersionUID = 4283479774388652604L;

	public static final int CATCH_SHAKES = 3;
	public static final int MAX_NAME_LENGTH = 10;
	public static final String DEFAULT_NAME = "Red";
	private static final int START_MONEY = 3000;

	private Point location;
	private Direction direction;

	private boolean mapReset;
	private MapName mapName;
	private SimpleMapTransition mapTransition;
	private String areaName;
	private Set<Entry<MapName, String>> flyLocations;

	private boolean isBiking;

	private transient PlayerEntity entity;

	private Set<String> definedGlobals;
	private Map<String, String> npcInteractions;

	private int fileNum;
	private long seconds;

	private transient long timeSinceUpdate;

	private SimpleMapTransition lastPCMapEntrance;

	private Pokedex pokedex;
	private PC pc;
	private Set<Badge> badges;
	private int repelSteps;

	private DayCareCenter dayCareCenter;

	private ActivePokemon evolvingPokemon;
	private BaseEvolution evolution;

	private ActivePokemon newPokemon;
	private Integer newPokemonBox;
	private boolean isFirstNewPokemon;

	private transient List<String> logMessages;

	public Player() {
		super(DEFAULT_NAME, START_MONEY);
		this.initialize();

		definedGlobals = new HashSet<>();
		npcInteractions = new HashMap<>();

		pokedex = new Pokedex();
		pc = new PC();

        badges = EnumSet.noneOf(Badge.class);

		repelSteps = 0;
		seconds = 0;

		direction = Direction.DOWN;
		areaName = StringUtils.empty();
		mapReset = false;
		flyLocations = new HashSet<>();

		dayCareCenter = new DayCareCenter();
	}

	// Initializes the character with the current game -- used when recovering a save file as well as the generic constructor
	public void initialize() {
		this.logMessages = new ArrayList<>();
		this.timeSinceUpdate = System.currentTimeMillis();
		this.entity = new PlayerEntity(this.location);
	}

	public void setName(String playerName) {
		this.name = playerName;
	}

	public void giveBadge(Badge badge) {
        this.badges.add(badge);
	}

	public boolean hasBadge(Badge badge) {
		return this.badges.contains(badge);
	}

	public PlayerEntity getEntity() {
		return this.entity;
	}

	public boolean hasTool(OverworldTool tool) {
		return this.globalsContain(tool.getGlobalName());
	}

	public DayCareCenter getDayCareCenter() {
		return this.dayCareCenter;
	}

	public boolean isBiking() {
		return this.isBiking;
	}

	public void toggleBicycle() {
		this.isBiking = !this.isBiking && this.hasTool(OverworldTool.BIKE);

	}

	public int getNumBadges() {
		return this.badges.size();
	}

	public void updateTimePlayed() {
		seconds += (System.currentTimeMillis() - timeSinceUpdate)/1000;
		timeSinceUpdate = System.currentTimeMillis();
	}

	public long getTimePlayed() {
		return seconds + (System.currentTimeMillis() - timeSinceUpdate)/1000;
	}

	public long getSeconds() {
		return this.seconds;
	}

	public int getFileNum() {
		return fileNum;
	}

	public void setFileNum(int n) {
		fileNum = n;
	}

	public Point getLocation() {
		return this.location;
	}

	public void setLocation(Point newLocation) {
		this.location = newLocation;
	}

	public Direction getDirection() {
		return this.direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}
	
	public void setMap(SimpleMapTransition mapTransitionMatcher) {
		mapName = mapTransitionMatcher.getNextMap();
		mapTransition = mapTransitionMatcher;
	}

	public void setArea(MapName mapName, AreaData area) {
		this.areaName = area.getAreaName();

		if (area.isFlyLocation()) {
			this.flyLocations.add(new SimpleEntry<>(mapName, this.areaName));
		}
	}

	public List<Entry<MapName, String>> getFlyLocations() {
		return this.flyLocations.stream().collect(Collectors.toList());
	}

	public ActivePokemon getEvolvingPokemon() {
		return evolvingPokemon;
	}

	public BaseEvolution getEvolution() {
		return evolution;
	}

	public ActivePokemon getNewPokemon() {
		return this.newPokemon;
	}

	public Integer getNewPokemonBox() {
		return this.newPokemonBox;
	}

	public boolean isFirstNewPokemon() {
		return this.isFirstNewPokemon;
	}

	// Called when a character steps once in any given direction
	public void step() {

		// Decrease repel steps
		if (repelSteps > 0) {
			repelSteps--;
			if (repelSteps == 0) {
				// TODO: Give choice if you want to use another. 
				// Game variable needed
				Messages.add("The effects of repel have worn off.");
			}
		}
		else {
			repelSteps = 0;
		}
		
		// Hatch eggs
        boolean doubleHatch = front().hasAbility(AbilityNamesies.FLAME_BODY) || front().hasAbility(AbilityNamesies.MAGMA_ARMOR);
		for (ActivePokemon p : team) {
			if (p.isEgg() && (p.hatch() || (doubleHatch && p.hatch()))) {
				evolvingPokemon = p;
				evolution = null;

				Messages.add(new MessageUpdate().withTrigger(
						TriggerType.GROUP.getTriggerNameFromSuffix("EggHatching"))
				);
				
				// Only one hatch per step
				break;
			}
		}

		// Check day care eggs
		dayCareCenter.step();
	}

	public boolean isFishing() {
		return this.globalsContain(FishingTrigger.FISHING_GLOBAL);
	}

	public boolean isUsingRepel() {
		return repelSteps > 0;
	}
	
	public void addRepelSteps(int steps) {
		repelSteps += steps;
	}

	public MapName getMapName() {
		return this.mapName;
	}

	public boolean mapReset() {
		return mapReset;
	}

	public void setMapReset(boolean mapReset) {
		this.mapReset = mapReset;
	}

	public SimpleMapTransition getMapTransition() {
		return this.mapTransition;
	}

	public String getAreaName() {
		return areaName;
	}
	
	public void setPokeCenter(SimpleMapTransition entranceName) {
		lastPCMapEntrance = entranceName;
	}
	
	public void teleportToPokeCenter() {
		setMap(lastPCMapEntrance);

		direction = Direction.DOWN;
		mapReset = true;
	}

	public void setNpcInteraction(final UpdateMatcher npcUpdateInteraction) {
		if (npcUpdateInteraction != null) {
			this.setNpcInteraction(npcUpdateInteraction.getNpcEntityName(), npcUpdateInteraction.getInteractionName());
		}
	}

	public void setNpcInteraction(final String npcEntityName, final String interactionName) {
		if (!StringUtils.isNullOrEmpty(interactionName)) {
			this.npcInteractions.put(npcEntityName, interactionName);
			System.out.println(npcEntityName + " -> " + npcInteractions.get(npcEntityName));
		}
	}

	public boolean hasNpcInteraction(final String npcEntityName) {
		return this.npcInteractions.containsKey(npcEntityName);
	}

	public String getNpcInteractionName(final String npcEntityName) {
		return this.npcInteractions.get(npcEntityName);
	}

	public boolean isNpcInteraction(final String npcEntityName, final String interactionName) {
		if (StringUtils.isNullOrEmpty(interactionName)) {
			return !this.hasNpcInteraction(npcEntityName);
		} else {
			return interactionName.equals(getNpcInteractionName(npcEntityName));
		}
	}

	public boolean globalsContain(String s) {
		return definedGlobals.contains(s);
	}
	
	public void addGlobal(String s) {
		if (!StringUtils.isNullOrEmpty(s)) {
			System.out.println("ADD GLOBAL: " + s);
			definedGlobals.add(s);
		}
	}
	
	public void removeGlobal(String s) {
		if (definedGlobals.contains(s)) {
			definedGlobals.remove(s);
		}
	}
	
	public PC getPC() {
		return pc;
	}
	
	// Gives EXP to all Pokemon who participated in battle
	public void gainEXP(ActivePokemon dead, Battle b) {
		int numUsed = 0;
		for (ActivePokemon p : team) {
			if (!p.canFight()) {
				continue;
			}

			if (p.getLevel() == ActivePokemon.MAX_LEVEL) {
				continue;
			}

			if (p.getAttributes().isUsed()) {
				numUsed++;
			}
		}
		
		// Everyone died at the same time! Or only Level 100 Pokemon were used!
		if (numUsed == 0) {
			return;
		}
		
		double wild = b.isWildBattle() ? 1 : 1.5;
		int lev = dead.getLevel(), base = dead.getPokemonInfo().getBaseEXP();
		for (ActivePokemon p : team) {
			if (p.canFight() && p.getAttributes().isUsed()) {
				double gain = wild * base * lev * Math.pow(2 * lev + 10, 2.5);
				gain /= 5 * Math.pow(lev + p.getLevel() + 10, 2.5);
				gain++;
				gain *= p.isHoldingItem(b, ItemNamesies.LUCKY_EGG) ? 1.5 : 1;

				p.gainEXP(b, (int) Math.max(1, gain / numUsed), dead);
			}
		}
	}
	
	public void winBattle(Battle b, Opponent opponent) {
		
		// Trainers pay up!
		if (opponent instanceof Trainer) {
			Trainer opp = (Trainer)opponent;
			Messages.add(new MessageUpdate(getName() + " defeated " + opp.getName() + "!").withUpdate(Update.WIN_BATTLE));
			this.setNpcInteraction(b.getNpcUpdateInteraction());
			
			// I've decided that the next line of code is the best line in this entire codebase
			int datCash = opp.getDatCashMoney()*(hasEffect(EffectNamesies.GET_DAT_CASH_MONEY_TWICE) ? 2 : 1);
			Messages.add(getName() + " received " + datCash + " pokedollars for winning! Woo!");
			getDatCashMoney(datCash);
		}
		else {
			Messages.add(new MessageUpdate().withUpdate(Update.WIN_BATTLE));
		}

		EndBattleEffect.invokeEndBattleEffect(this.getEffects(), this, b, front());
		for (ActivePokemon p : team) {
			EndBattleEffect.invokeEndBattleEffect(p.getAllEffects(b), this, b, p);
		}

		setFront();

		// WE'RE DONE HERE
		Messages.add(new MessageUpdate().withUpdate(Update.EXIT_BATTLE));
	}

	public void checkEvolution() {
		team.stream()
				.filter(pokemon -> pokemon.canFight())
				.forEach(pokemon -> pokemon.checkEvolution());
	}
	
	public Pokedex getPokedex() {
		return pokedex;
	}

	@Override
	public void addPokemon(ActivePokemon p) {
		this.addPokemon(p, true);
	}

	public void addPokemon(ActivePokemon p, boolean viewChange) {
		this.newPokemon = p;
		if (viewChange) {
			Messages.add(new MessageUpdate().withViewChange(ViewMode.NEW_POKEMON_VIEW));
		}

		p.setCaught();

		if (team.size() < MAX_POKEMON) {
			team.add(p);
			this.newPokemonBox = null;
		}
		else {
			pc.depositPokemon(p);
			this.newPokemonBox = pc.getBoxNum() + 1;
		}

		if (!p.isEgg() && !pokedex.isCaught(newPokemon)) {
			pokedex.setCaught(newPokemon.getPokemonInfo());
			this.isFirstNewPokemon = true;
		} else {
			this.isFirstNewPokemon = false;
		}
	}

	public void pokemonEvolved(ActivePokemon p) {
		this.newPokemon = p;
		Messages.add(new MessageUpdate().withViewChange(ViewMode.NEW_POKEMON_VIEW));

		// Should already be in party if evolving/hatching
		this.newPokemonBox = null;

		// Show pokedex info if we don't already have this pokemon
		isFirstNewPokemon = !pokedex.isCaught(p);
		pokedex.setCaught(p);
		p.setCaught();
	}

	public boolean fullParty() {
		return this.team.size() == MAX_POKEMON;
	}

	// TODO: this looks like you can deposit your last not fainted pokemon
	// Determines whether or not a Pokemon can be deposited
	public boolean canDeposit(ActivePokemon p) {

		// You can't deposit a Pokemon that you don't have
		if (!team.contains(p)) {
			return false;
		}
		
		// Eggs and deadies can always be deposited
		if (!p.canFight()) {
			return true;
		}

		// Otherwise you can if you have at least one other Pokemon that is not dead or an egg
		for (ActivePokemon pokemon : team) {
			if (pokemon != p && pokemon.canFight()) {
				return true;
			}
		}

		return false;
	}
	
	public int totalEggs() {
		return (int)team.stream()
				.filter(ActivePokemon::isEgg)
				.count();
	}

	private BallItem pokeball;
	public BallItem getPokeball() {
		return this.pokeball;
	}

	// OH MY GOD CATCH A POKEMON OH MY GOD
	public boolean catchPokemon(Battle b, BallItem ball) {
		if (!b.isWildBattle()) {
			Messages.add("You can't try and catch a trainer's Pokemon! That's just rude!!!");
			return false;
		}

		Messages.add(name + " threw the " + ball.getName() + "!");
		this.pokeball = ball;
		
		ActivePokemon catchPokemon = b.getOtherPokemon(true);
		int maxHP = catchPokemon.getMaxHP();
		int hp = catchPokemon.getHP();

		int catchRate = catchPokemon.getPokemonInfo().getCatchRate();
		double statusMod = catchPokemon.getStatus().getType().getCatchModifier();

		double ballMod = ball.getModifier(front(), catchPokemon, b);
		int ballAdd = ball.getAdditive(front(), catchPokemon, b);

		double catchVal = (3*maxHP - 2*hp)*catchRate*ballMod*statusMod/(3*maxHP) + ballAdd;
		int shakeVal = (int)Math.ceil(65536/Math.pow(255/catchVal, .25));
				
		for (int i = 0; i < CATCH_SHAKES + 1; i++) {
			if (!RandomUtils.chanceTest(shakeVal, 65536)) {
				Messages.add(new MessageUpdate().withCatchPokemon(i));
				Messages.add("Oh no! " + catchPokemon.getName() + " broke free!");
				return true;
			}
		}

		Messages.add(new MessageUpdate().withCatchPokemon(-1));
		Messages.add("Gotcha! " + catchPokemon.getName() + " was caught!");
		gainEXP(catchPokemon, b);
		addPokemon(catchPokemon);
		ball.afterCaught(catchPokemon);

		Messages.add(new MessageUpdate().withUpdate(Update.CATCH_POKEMON));
		return true;
	}
	
	public void setEvolution(ActivePokemon pokemon, BaseEvolution evolution) {
		this.evolvingPokemon = pokemon;
		this.evolution = evolution;

		Messages.add(new MessageUpdate().withViewChange(ViewMode.EVOLUTION_VIEW));
	}
	
	public void addLogMessage(MessageUpdate messageUpdate) {
		String messageString = messageUpdate.getMessage().trim();
		if (messageString.isEmpty()) {
			return;
		}
		
		logMessages.add("-" + messageString);
	}
	
	public void clearLogMessages() {
		logMessages.clear();
	}
	
	public List<String> getLogMessages() {
		return logMessages;
	}
}
