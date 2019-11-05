package trainer.player;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.Move;
import battle.effect.InvokeInterfaces.EndBattleEffect;
import battle.effect.team.TeamEffectNamesies;
import gui.view.ViewMode;
import item.ItemNamesies;
import item.berry.farm.BerryFarm;
import item.use.BallItem;
import main.Game;
import map.Direction;
import map.MapName;
import map.area.AreaData;
import map.area.FlyLocation;
import map.entity.movable.PlayerEntity;
import map.overworld.OverworldTool;
import map.triggers.battle.FishingTrigger;
import message.MessageUpdate;
import message.MessageUpdateType;
import message.Messages;
import message.Messages.MessageState;
import pattern.SimpleMapTransition;
import pattern.action.UpdateMatcher;
import pokemon.ability.AbilityNamesies;
import pokemon.active.PartyPokemon;
import pokemon.breeding.DayCareCenter;
import pokemon.breeding.Eggy;
import pokemon.evolution.EvolutionMethod;
import trainer.Opponent;
import trainer.PlayerTrainer;
import trainer.Trainer;
import trainer.TrainerAction;
import trainer.player.medal.MedalCase;
import trainer.player.medal.MedalTheme;
import trainer.player.pokedex.Pokedex;
import util.Point;
import util.RandomUtils;
import util.TimeUtils;
import util.serialization.Serializable;
import util.string.PokeString;
import util.string.StringUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Player extends PlayerTrainer implements Serializable {
    private static final long serialVersionUID = 4283479774388652604L;

    public static final String DEFAULT_NAME = "Red";
    public static final int MAX_NAME_LENGTH = 10;
    public static final int CATCH_SHAKES = 3;
    private static final int START_MONEY = 3000;

    private Point location;
    private Direction direction;

    private boolean mapReset;
    private MapName mapName;
    private SimpleMapTransition mapTransition;
    private String areaName;
    private Set<FlyLocation> flyLocations;

    private boolean isBiking;

    private transient PlayerEntity entity;

    private Set<String> definedGlobals;
    private Map<String, String> entityInteractions;

    private int fileNum;
    private long seconds;

    private transient long timeSinceUpdate;

    private SimpleMapTransition lastPCMapEntrance;

    private Set<Badge> badges;
    private Pokedex pokedex;
    private PC pc;
    private MedalCase medalCase;

    private DayCareCenter dayCareCenter;
    private BerryFarm berryFarm;

    private EvolutionInfo evolutionInfo;
    private NewPokemonInfo newPokemonInfo;
    private RepelInfo repelInfo;
    private BallItem pokeball;

    private transient List<String> logMessages;

    public Player() {
        super(DEFAULT_NAME, START_MONEY);
        this.initialize();

        definedGlobals = new HashSet<>();
        entityInteractions = new HashMap<>();

        pokedex = new Pokedex();
        pc = new PC();
        medalCase = new MedalCase();

        badges = EnumSet.noneOf(Badge.class);

        seconds = 0;

        direction = Direction.DOWN;
        areaName = "";
        mapReset = false;
        flyLocations = new HashSet<>();

        dayCareCenter = new DayCareCenter();
        berryFarm = new BerryFarm();

        evolutionInfo = new EvolutionInfo();
        newPokemonInfo = new NewPokemonInfo();
        repelInfo = new RepelInfo();
    }

    // Initializes the character with the current game -- used when recovering a save file as well as the generic constructor
    public void initialize() {
        this.logMessages = new ArrayList<>();
        this.timeSinceUpdate = TimeUtils.getCurrentTimestamp();
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
        return this.hasGlobal(tool.getGlobalName());
    }

    public DayCareCenter getDayCareCenter() {
        return this.dayCareCenter;
    }

    public BerryFarm getBerryFarm() {
        return this.berryFarm;
    }

    public boolean isBiking() {
        return this.isBiking;
    }

    public void toggleBicycle() {
        this.isBiking = !this.isBiking && this.getBag().hasItem(ItemNamesies.BICYCLE);

        if (this.isBiking) {
            this.medalCase.increase(MedalTheme.BICYCLE_COUNT);
        }
    }

    public int getNumBadges() {
        return this.badges.size();
    }

    public void updateTimePlayed() {
        seconds += TimeUtils.secondsSince(timeSinceUpdate);
        timeSinceUpdate = TimeUtils.getCurrentTimestamp();
    }

    public long getTimePlayed() {
        return seconds + TimeUtils.secondsSince(timeSinceUpdate);
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
            this.flyLocations.add(new FlyLocation(mapName, this.areaName));
        }
    }

    public List<FlyLocation> getFlyLocations() {
        return new ArrayList<>(this.flyLocations);
    }

    public EvolutionInfo getEvolutionInfo() {
        return this.evolutionInfo;
    }

    public NewPokemonInfo getNewPokemonInfo() {
        return this.newPokemonInfo;
    }

    // Called when a character steps once in any given direction
    public void step() {

        // Decrease repel steps
        repelInfo.step();

        // Check day care eggs
        dayCareCenter.step();

        // Gotta get dat gold
        medalCase.increase(MedalTheme.STEPS_WALKED);

        // Hatch eggs
        boolean doubleHatch = front().hasAbility(AbilityNamesies.FLAME_BODY) || front().hasAbility(AbilityNamesies.MAGMA_ARMOR);
        for (int i = 0; i < team.size(); i++) {
            PartyPokemon p = team.get(i);
            if (p.isEgg()) {
                Eggy eggy = (Eggy)p;
                ActivePokemon hatched = eggy.hatch(doubleHatch);
                if (hatched != null) {
                    this.evolutionInfo.setEgg(hatched);
                    this.team.set(i, hatched);

                    // Only one hatch per step
                    break;
                }
            }
        }
    }

    public boolean isFishing() {
        return this.hasGlobal(FishingTrigger.FISHING_GLOBAL);
    }

    public RepelInfo getRepelInfo() {
        return this.repelInfo;
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

    public AreaData getArea() {
        return Game.getData().getMap(this.getMapName()).getArea(this.getAreaName());
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
            this.entityInteractions.put(npcEntityName, interactionName);
            System.out.println(npcEntityName + " -> " + entityInteractions.get(npcEntityName));
        }
    }

    public boolean hasEntityInteraction(final String npcEntityName) {
        return this.entityInteractions.containsKey(npcEntityName);
    }

    public String getEntityInteractionName(final String npcEntityName) {
        return this.entityInteractions.get(npcEntityName);
    }

    public boolean isEntityInteraction(final String entityName, final String interactionName) {
        if (StringUtils.isNullOrEmpty(interactionName)) {
            return !this.hasEntityInteraction(entityName);
        } else {
            return interactionName.equals(getEntityInteractionName(entityName));
        }
    }

    public boolean hasGlobal(String s) {
        return definedGlobals.contains(s);
    }

    public void addGlobal(String s) {
        if (!StringUtils.isNullOrEmpty(s)) {
            System.out.println("ADD GLOBAL: " + s);
            definedGlobals.add(s);
        }
    }

    public void removeGlobal(String s) {
        definedGlobals.remove(s);
    }

    public PC getPC() {
        return pc;
    }

    public MedalCase getMedalCase() {
        return this.medalCase;
    }

    @Override
    public int sucksToSuck(int datCash) {
        this.medalCase.increase(MedalTheme.CASH_MONEY_SPENT, datCash);

        return super.sucksToSuck(datCash);
    }

    // Gives EXP to all Pokemon who participated in battle
    public void gainEXP(ActivePokemon dead, Battle b) {
        int numUsed = 0;
        for (PartyPokemon p : team) {
            if (!p.canFight()) {
                continue;
            }

            if (p.getLevel() == PartyPokemon.MAX_LEVEL) {
                continue;
            }

            if (p.isUsed()) {
                numUsed++;
            }
        }

        // Everyone died at the same time! Or only Level 100 Pokemon were used!
        if (numUsed == 0) {
            return;
        }

        double wild = b.isWildBattle() ? 1 : 1.5;
        int lev = dead.getLevel();
        int base = dead.getPokemonInfo().getBaseEXP();
        for (PartyPokemon p : team) {
            if (p.canFight() && p.isUsed()) {
                ActivePokemon active = (ActivePokemon)p;

                double gain = wild*base*lev*Math.pow(2*lev + 10, 2.5);
                gain /= 5*Math.pow(lev + active.getLevel() + 10, 2.5);
                gain++;
                gain *= active.isHoldingItem(b, ItemNamesies.LUCKY_EGG) ? 1.5 : 1;

                active.gainEXP(b, (int)Math.max(1, gain/numUsed), dead);
            }
        }
    }

    public void winBattle(Battle b, Opponent opponent) {
        // Trainers pay up!
        if (opponent instanceof Trainer) {
            Trainer opp = (Trainer)opponent;
            Messages.add(new MessageUpdate(getName() + " defeated " + opp.getName() + "!").withUpdate(MessageUpdateType.WIN_BATTLE));
            this.setNpcInteraction(b.getNpcUpdateInteraction());

            // I've decided that the next line of code is the best line in this entire codebase
            int datCash = opp.getDatCashMoney()*(hasEffect(TeamEffectNamesies.GET_DAT_CASH_MONEY_TWICE) ? 2 : 1);
            Messages.add(getName() + " received " + datCash + " " + PokeString.POKEDOLLARS + " for winning! Woo!");
            this.getDatCashMoney(datCash);
        } else {
            Messages.add(new MessageUpdate().withUpdate(MessageUpdateType.WIN_BATTLE));
        }

        EndBattleEffect.invokeEndBattleEffect(this.getEffects().asList(), this, b, front());
        for (ActivePokemon p : this.getActiveTeam()) {
            EndBattleEffect.invokeEndBattleEffect(p.getAllEffects(b), this, b, p);
        }

        setFront();

        // WE'RE DONE HERE
        Messages.add(new MessageUpdate().withUpdate(MessageUpdateType.EXIT_BATTLE));
    }

    public void loseBattle(Opponent opponent) {
        // Blackout -- you're fucked
        Messages.add(this.getName() + " is out of usable " + PokeString.POKEMON + "! " + this.getName() + " blacked out!");

        // Sucks to suck
        if (opponent instanceof Trainer) {
            Trainer opp = (Trainer)opponent;
            int cashMoney = this.sucksToSuck(opp.getDatCashMoney());
            Messages.add(opp.getName() + " rummaged through the pockets of your passed out body and stole " + cashMoney + " " + PokeString.POKEDOLLARS + "!!!");
        }

        this.healAll();
        this.teleportToPokeCenter();

        Messages.clearMessages(MessageState.MAPPITY_MAP);
        Messages.add(new MessageUpdate().withUpdate(MessageUpdateType.EXIT_BATTLE));
    }

    public void checkEvolution() {
        for (PartyPokemon p : team) {
            if (p.canFight() && p.isBattleUsed()) {
                // Evolution information will be set in this method
                boolean canEvolve = EvolutionMethod.LEVEL.checkEvolution(p) || EvolutionMethod.MOVE.checkEvolution(p);
                if (canEvolve) {
                    // Only one evolution per battle
                    break;
                }
            }
        }
    }

    public Pokedex getPokedex() {
        return pokedex;
    }

    @Override
    public void addPokemon(PartyPokemon p) {
        this.addPokemon(p, true);
    }

    public void addPokemon(PartyPokemon p, boolean viewChange) {
        this.newPokemonInfo.setNewPokemon(p);
        if (viewChange) {
            Messages.add(new MessageUpdate().withViewChange(ViewMode.NEW_POKEMON_VIEW));
        }

        p.setCaught();

        if (team.size() < MAX_POKEMON) {
            team.add(p);
            newPokemonInfo.inTeam();
        } else {
            pc.depositPokemon(p);
            newPokemonInfo.inBox(pc.getBoxNum() + 1);
        }

        boolean addToPokedex = !p.isEgg() && !pokedex.isCaught((ActivePokemon)p);
        if (addToPokedex) {
            pokedex.setCaught(p.getPokemonInfo());
        }

        newPokemonInfo.setFirstNewPokemon(addToPokedex);
    }

    public void pokemonEvolved(ActivePokemon p) {
        newPokemonInfo.setNewPokemon(p);
        Messages.add(new MessageUpdate().withViewChange(ViewMode.NEW_POKEMON_VIEW));

        // Should already be in party if evolving/hatching
        newPokemonInfo.inTeam();

        // Show pokedex info if we don't already have this pokemon
        newPokemonInfo.setFirstNewPokemon(!pokedex.isCaught(p));
        pokedex.setCaught(p);
        p.setCaught();
    }

    public boolean fullParty() {
        return this.team.size() == MAX_POKEMON;
    }

    // Determines whether or not a Pokemon can be deposited
    public boolean canDeposit(PartyPokemon p) {

        // You can't deposit a Pokemon that you don't have
        if (!team.contains(p)) {
            return false;
        }

        // Eggs and deadies can always be deposited
        if (!p.canFight()) {
            return true;
        }

        // Otherwise you can if you have at least one other Pokemon that is not dead or an egg
        for (PartyPokemon pokemon : team) {
            if (pokemon != p && pokemon.canFight()) {
                return true;
            }
        }

        return false;
    }

    public int totalEggs() {
        return (int)team.stream()
                        .filter(PartyPokemon::isEgg)
                        .count();
    }

    public BallItem getPokeball() {
        return this.pokeball;
    }

    private double getCriticalCaptureModifier() {
        int numPokemonCaught = this.getPokedex().numCaught();
        if (numPokemonCaught > 600) {
            return 2.5;
        } else if (numPokemonCaught > 450) {
            return 2;
        } else if (numPokemonCaught > 300) {
            return 1.5;
        } else if (numPokemonCaught > 150) {
            return 1;
        } else if (numPokemonCaught > 30) {
            return .5;
        } else {
            return 0;
        }
    }

    // OH MY GOD CATCH A POKEMON OH MY GOD
    public boolean catchPokemon(Battle b, BallItem ball) {
        ActivePokemon catchPokemon = b.getOpponent().front();
        if (!b.isWildBattle()) {
            Messages.add("You can't try and catch a trainer's " + PokeString.POKEMON + "! That's just rude!!!");
            return false;
        } else if (catchPokemon.isSemiInvulnerable()) {
            Messages.add(catchPokemon.getName() + " is out of range!!");
            return false;
        }

        Messages.add(name + " threw the " + ball.getName() + "!");
        this.pokeball = ball;

        int maxHP = catchPokemon.getMaxHP();
        int hp = catchPokemon.getHP();

        int catchRate = catchPokemon.getPokemonInfo().getCatchRate();
        double statusMod = catchPokemon.getStatus().getCatchModifier();

        double ballMod = ball.getModifier(front(), catchPokemon, b);
        int ballAdd = ball.getAdditive(front(), catchPokemon, b);

        double catchVal = (3*maxHP - 2*hp)*catchRate*ballMod*statusMod/(3*maxHP) + ballAdd;
        int shakeVal = (int)Math.ceil(65536/Math.pow(255/catchVal, .1875));

        int criticalCaptureVal = (int)(catchVal*this.getCriticalCaptureModifier()/6);
        boolean criticalCapture = RandomUtils.chanceTest(criticalCaptureVal, 255);
        if (criticalCapture) {
            Messages.add("It's a critical capture!!!");
        }

        for (int i = 0; i < CATCH_SHAKES + 1; i++) {
            if (!RandomUtils.chanceTest(shakeVal, 65536)) {
                Messages.add(new MessageUpdate().withCatchPokemon(i));
                Messages.add("Oh no! " + catchPokemon.getName() + " broke free!");
                return true;
            }

            // Critical captures only have to pass one test
            if (criticalCapture) {
                break;
            }
        }

        Messages.add(new MessageUpdate().withCatchPokemon(-1));
        Messages.add("Gotcha! " + catchPokemon.getName() + " was caught!");
        gainEXP(catchPokemon, b);
        addPokemon(catchPokemon);
        ball.afterCaught(catchPokemon);

        Messages.add(new MessageUpdate().withUpdate(MessageUpdateType.CATCH_POKEMON));
        return true;
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

    public void performAction(Battle b, TrainerAction action) {
        setAction(action);

        Opponent opponent = b.getOpponent();
        ActivePokemon opponentPokemon = opponent.front();
        opponentPokemon.setMove(Move.selectOpponentMove(b, opponentPokemon));

        if (opponent instanceof Trainer) {
            ((Trainer)opponent).setAction(TrainerAction.FIGHT);
        }

        b.fight();
    }
}
