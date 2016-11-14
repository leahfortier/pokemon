package trainer;

import battle.Battle;
import battle.effect.generic.EffectInterfaces.EndBattleEffect;
import item.Item;
import item.use.BallItem;
import main.Game;
import main.Game.ViewMode;
import main.Global;
import map.Direction;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import message.MessageUpdate;
import message.MessageUpdate.Update;
import message.Messages;
import namesies.EffectNamesies;
import namesies.ItemNamesies;
import pattern.ActionMatcher.UpdateMatcher;
import pattern.GroupTriggerMatcher;
import pokemon.ActivePokemon;
import pokemon.BaseEvolution;
import pokemon.PC;
import trainer.Pokedex.PokedexStatus;
import util.JsonUtils;
import util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CharacterData extends Trainer implements Serializable {
	private static final long serialVersionUID = 4283479774388652604L;

	public static final int NUM_BADGES = 12;
	public static final int CATCH_SHAKES = 3;
	public static final int MAX_NAME_LENGTH = 10;
	public static final String DEFAULT_NAME = "Red";
	private static final int START_MONEY = 3000;

	// TODO: Look into most of these to check if they really do need to be public
	public int locationX, locationY;
	public Direction direction;
	
	public boolean mapReset;
	public String mapName;
	public String areaName;

	private Set<String> definedGlobals;
	private Map<String, String> npcInteractions;

	public String mapEntranceName;
	
	private int fileNum;
	private long seconds;
	private int numBadges;

	private transient long timeSinceUpdate;
	
	private String lastPCMap;
	private String lastPCMapEntrance;
	
	private Pokedex pokedex;
	private PC pc;
	private boolean[] badges;
	private int repelSteps;

	// TODO: Make private
	public ActivePokemon evolvingPokemon;
	public BaseEvolution evolution;
	
	private List<String> logMessages;

	public CharacterData() {
		super(DEFAULT_NAME, START_MONEY);
		this.initialize();

		definedGlobals = new HashSet<>();
		npcInteractions = new HashMap<>();
		
		pokedex = new Pokedex();
		pc = new PC();
		
		badges = new boolean[NUM_BADGES];
		Arrays.fill(badges, false);
		
		repelSteps = 0;
		seconds = 0;
		
		direction = Direction.DOWN;
		areaName = "";
		mapReset = false;
	}
	
	// Initializes the character with the current game -- used when recovering a save file as well as the generic constructor
	public void initialize() {
		this.logMessages = new ArrayList<>();
		this.timeSinceUpdate = System.currentTimeMillis();
	}
	
	public void setName(String playerName) {
		this.name = playerName;
	}

	// TODO: n??? srsly??? did I fucking write this??????
	public void giveBadge(int n) {
		if (!badges[n]) {
			numBadges++;
			badges[n] = true;
		}
	}
	
	public int getNumBadges() {
		return numBadges;
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
	
	public void setLocation(int x, int y) {
		locationX = x;
		locationY = y;
	}
	
	public void setMap(String name, String mapEntrance) {
		mapName = name;
		mapEntranceName = mapEntrance;
	}
	
	// Called when a character steps once in any given direction
	public void step() {
		// Decrease repel steps
		if (repelSteps > 0) {
			repelSteps--;
			if (repelSteps == 0) {
				// TODO: Give choice if you want to use another. 
				// Game variable needed
				Messages.addMessage("The effects of repel have worn off.");
			}
		}
		else {
			repelSteps = 0;
		}
		
		// Hatch eggs
		for (ActivePokemon p : team) {
			if (p.isEgg() && p.hatch()) {
				evolvingPokemon = p;

				Trigger dialogue = TriggerType.DIALOGUE.createTrigger("Huh?", null);
				Trigger evolutionView = TriggerType.CHANGE_VIEW.createTrigger(ViewMode.EVOLUTION_VIEW.name(), null);

				GroupTriggerMatcher matcher = new GroupTriggerMatcher(dialogue.getName(), evolutionView.getName());
				Trigger group = TriggerType.GROUP.createTrigger(JsonUtils.getJson(matcher), null);
				Messages.addMessage(new MessageUpdate("", group.getName(), Update.TRIGGER));
				
				// Only one hatch per step
				break;
			}
		}
	}
	
	public boolean isUsingRepel() {
		return repelSteps > 0;
	}
	
	public void addRepelSteps(int steps) {
		repelSteps += steps;
	}
	
	public String getAreaName() {
		return areaName;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}
	
	public void setPokeCenter() {
		lastPCMap = mapName;
		lastPCMapEntrance = mapEntranceName;
	}
	
	public void teleportToPokeCenter() {
		setMap(lastPCMap, lastPCMapEntrance);

		direction = Direction.DOWN;
		mapReset = true;
	}

	public void setNpcInteraction(final UpdateMatcher npcUpdateInteraction) {
		if (npcUpdateInteraction != null) {
			this.setNpcInteraction(npcUpdateInteraction.npcEntityName, npcUpdateInteraction.interactionName);
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

	public boolean globalsContain(String s) {
		return definedGlobals.contains(s);
	}
	
	public void addGlobal(String s) {
		if (s == null) {
			return;
		}

		System.out.println("ADD GLOBAL: " + s);

		definedGlobals.add(s);
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
			Messages.addMessage(getName() + " defeated " + opp.getName() + "!", Update.WIN_BATTLE);
			this.setNpcInteraction(b.getNpcUpdateInteraction());
			
			// I've decided that the next line of code is the best line in this entire codebase
			int datCash = opp.getDatCashMoney()*(hasEffect(EffectNamesies.GET_DAT_CASH_MONEY_TWICE) ? 2 : 1);
			Messages.addMessage(getName() + " received " + datCash + " pokedollars for winning! Woo!");
			getDatCashMoney(datCash);
		}
		else {
			Messages.addMessage("", Update.WIN_BATTLE);
		}

		EndBattleEffect.invokeEndBattleEffect(this.getEffects(), this, b, front());
		for (ActivePokemon p : team) {
			EndBattleEffect.invokeEndBattleEffect(p.getAllEffects(b), this, b, p);
		}
		
		setFront();
	}
	
	public Pokedex getPokedex() {
		return pokedex;
	}
	
	public void addPokemon(Battle b, ActivePokemon p) {
		p.setCaught();
		if (!pokedex.caught(p.getPokemonInfo().namesies())) {
			if (b != null) {
				Messages.addMessage(p.getPokemonInfo().getName() + " was registered in the Pok\u00e9dex!");
			}

			if (!p.isEgg()) {
				pokedex.setStatus(p.getPokemonInfo(), PokedexStatus.CAUGHT);
			}
		}
		
		if (team.size() < MAX_POKEMON) {
			team.add(p);
		}
		else {
			if (b != null) {
				Messages.addMessage(p.getActualName() + " was sent to Box " + (pc.getBoxNum() + 1) + " of your PC!");
			}

			pc.depositPokemon(p);
		}
	}
	
	// Determines whether or not a Pokemon can be deposited
	public boolean canDeposit(ActivePokemon p) {

		// You can't deposit a Pokemon that you don't have
		if (!team.contains(p)) {
			return false;
		}
		
		// Eggs can always be deposited
		if (p.isEgg()) {
			return true;
		}
		
		// Otherwise you can if you have at least one other Pokemon that is not an egg
		return team.size() - totalEggs() > 1;
	}
	
	public int totalEggs() {
		return (int)team.stream()
				.filter(ActivePokemon::isEgg)
				.count();
	}
	
	// OH MY GOD CATCH A POKEMON OH MY GOD
	public boolean catchPokemon(Battle b, BallItem ball) {
		if (!b.isWildBattle()) {
			Messages.addMessage("You can't try and catch a trainer's Pokemon! That's just rude!!!");
			return false;
		}
		
		Messages.addMessage(name + " threw the " + ((Item)ball).getName() + "!");
		
		ActivePokemon catchPokemon = b.getOtherPokemon(true);
		int maxHP = catchPokemon.getMaxHP();
		int hp = catchPokemon.getHP();

		int catchRate = catchPokemon.getPokemonInfo().getCatchRate();
		double statusMod = catchPokemon.getStatus().getType().getCatchModifier();

		double[] ballInfo = ball.catchRate(front(), catchPokemon, b);
		double ballMod = ballInfo[0];
		double ballAdd = ballInfo[1];

		double catchVal = (3*maxHP - 2*hp)*catchRate*ballMod*statusMod/(3*maxHP) + ballAdd;
		int shakeVal = (int)Math.ceil(65536/Math.pow(255/catchVal, .25));
				
		for (int i = 0; i < CATCH_SHAKES + 1; i++) {
			if (!Global.chanceTest(shakeVal, 65536)) {
				Messages.addMessage("", i);
				Messages.addMessage("Oh no! " + catchPokemon.getName() + " broke free!");
				return true;
			}
		}
		
		Messages.addMessage("", -1);
		Messages.addMessage("Gotcha! " + catchPokemon.getName() + " was caught!");
		gainEXP(catchPokemon, b);
		addPokemon(b, catchPokemon);
		
		Messages.addMessage(" ", Update.EXIT_BATTLE);
		return true;
	}
	
	public void setEvolution(ActivePokemon pokemon, BaseEvolution evolution) {
		this.evolvingPokemon = pokemon;
		this.evolution = evolution;
		
		Game.setViewMode(ViewMode.EVOLUTION_VIEW);
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
