package battle;

import battle.attack.Attack;
import battle.attack.MoveType;
import battle.effect.EffectNamesies.BattleEffectNamesies;
import battle.effect.InvokeEffect;
import battle.effect.attack.MultiTurnMove;
import battle.effect.battle.BattleEffect;
import battle.effect.battle.StandardBattleEffectNamesies;
import battle.effect.battle.weather.WeatherEffect;
import battle.effect.interfaces.InvokeInterfaces.AlwaysCritEffect;
import battle.effect.interfaces.InvokeInterfaces.BasicAccuracyBypassEffect;
import battle.effect.interfaces.InvokeInterfaces.BeforeTurnEffect;
import battle.effect.interfaces.InvokeInterfaces.CrashDamageMove;
import battle.effect.interfaces.InvokeInterfaces.CritBlockerEffect;
import battle.effect.interfaces.InvokeInterfaces.CritStageEffect;
import battle.effect.interfaces.InvokeInterfaces.DefiniteEscape;
import battle.effect.interfaces.InvokeInterfaces.EntryEffect;
import battle.effect.interfaces.InvokeInterfaces.NameChanger;
import battle.effect.interfaces.InvokeInterfaces.OpponentAccuracyBypassEffect;
import battle.effect.interfaces.InvokeInterfaces.OpponentPowerChangeEffect;
import battle.effect.interfaces.InvokeInterfaces.PowerChangeEffect;
import battle.effect.interfaces.InvokeInterfaces.PriorityChangeEffect;
import battle.effect.interfaces.InvokeInterfaces.SemiInvulnerableBypasser;
import battle.effect.interfaces.InvokeInterfaces.StallingEffect;
import battle.effect.interfaces.InvokeInterfaces.StrikeFirstEffect;
import main.Game;
import main.Global;
import map.overworld.TerrainType;
import message.MessageUpdate;
import message.MessageUpdateType;
import message.Messages;
import message.Messages.MessageState;
import pattern.action.UpdateMatcher;
import pokemon.Stat;
import trainer.EnemyTrainer;
import trainer.Opponent;
import trainer.PlayerTrainer;
import trainer.SimulatedPlayer;
import trainer.Team;
import trainer.Team.TeamEffectList;
import trainer.Trainer;
import trainer.TrainerAction;
import trainer.WildPokemon;
import trainer.player.Player;
import trainer.player.medal.MedalTheme;
import type.TypeAdvantage;
import util.RandomUtils;
import util.serialization.Serializable;
import util.string.PokeString;
import util.string.StringAppender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Battle implements Serializable {
    private static final long serialVersionUID = 1L;

    // Crit yo pants
    private static final int[] CRITSICLES = { 16, 8, 4, 3, 2 };

    private final Opponent opponent; // SO OBJECT-ORIENTED
    private PlayerTrainer player;

    private BattleEffectList effects;

    private int turn;
    private boolean firstAttacking;
    private int escapeAttempts;

    private transient UpdateMatcher npcUpdateInteraction;

    public Battle(EnemyTrainer npcTrainer, UpdateMatcher npcUpdateInteraction) {
        this(npcTrainer);
        this.npcUpdateInteraction = npcUpdateInteraction;
    }

    public Battle(Opponent opponent) {
        Messages.clearMessages(MessageState.FIGHTY_FIGHT);
        Messages.setMessageState(MessageState.FIGHTY_FIGHT);
        Messages.add(new MessageUpdate().withUpdate(MessageUpdateType.ENTER_BATTLE));

        Player player = Game.getPlayer();
        player.getMedalCase().increase(MedalTheme.BATTLES_BATTLED);

        this.player = player;
        this.opponent = opponent;

        this.player.enterBattle();
        this.opponent.enterBattle();

        this.effects = new BattleEffectList();
        this.effects.initialize(this);

        turn = 0;
        escapeAttempts = 0;
        firstAttacking = false;

        int maxPokemonAllowed = opponent.maxPokemonAllowed();
        if (maxPokemonAllowed < Trainer.MAX_POKEMON) {
            boolean onlyOne = maxPokemonAllowed == 1;
            Messages.add(String.format(
                    "%s %d %s %s allowed for this battle!!",
                    onlyOne ? "Only" : "At most",
                    maxPokemonAllowed,
                    PokeString.POKEMON,
                    onlyOne ? "is" : "are"
            ));
        }

        Messages.add(this.opponent.getStartBattleMessage());
        enterBattle(this.opponent.front());
        enterBattle(this.player.front());
    }

    public PlayerTrainer getPlayer() {
        return player;
    }

    public Battle getSimulated() {
        Battle simulated = this.getSerializedCopy(Battle.class);
        simulated.player = new SimulatedPlayer(this.getPlayer());
        simulated.effects.setBattle(simulated);

        return simulated;
    }

    public Opponent getOpponent() {
        return opponent;
    }

    public WeatherEffect getWeather() {
        return effects.getWeather();
    }

    public int getTurn() {
        return turn;
    }

    public UpdateMatcher getNpcUpdateInteraction() {
        return this.npcUpdateInteraction;
    }

    public TerrainType getTerrainType() {
        return effects.getTerrainType();
    }

    public boolean hasEffect(BattleEffectNamesies effect) {
        return effects.hasEffect(effect);
    }

    public void fight() {
        startTurn();

        boolean playerFirst = speedPriority();
        fight(playerFirst);
    }

    private void fight(boolean playerFirst) {
        // First turn
        executionSolution(true, playerFirst);

        // Second turn
        executionSolution(false, playerFirst);

        effects.endTurn();

        if (!isSimulating()) {
            deadUser();
            deadOpponent();
        }

        printShit();
    }

    public boolean isSimulating() {
        return !(this.player instanceof Player);
    }

    protected void printShit() {
        if (this.isSimulating()) {
            return;
        }

        System.out.println(getTeamEffectsString("Player:", player));
        System.out.println(getTeamEffectsString("Opponent:", opponent));

        this.effects.printShit();

        System.out.println();
    }

    private String getTeamEffectsString(String prefix, Team team) {
        ActivePokemon p = team.front();
        return new StringAppender(prefix)
                .appendLine()
                .append(p.getActualName() + " ")
                .appendJoin(" ", Stat.BATTLE_STATS, stat -> String.valueOf(p.getStage(stat)))
                .append(" " + p.getAbility().getName())
                .append(" " + p.getHeldItem(this).getName() + " ")
                .appendJoin(" ", Stat.STATS, stat -> String.valueOf(p.getStat(this, stat)))
                .appendLine()
                .appendIf(p.hasStatus(), p.getStatus() + "\n")
                .appendJoin("\n", p.getEffects().asList())
                .appendIfLastNonempty("\n")
                .appendJoin("\n", team.getEffects().asList())
                .toString()
                .replaceAll("\n", "\n\t")
                .trim();
    }

    // Handles events that occur at the beginning of each turn
    private void startTurn() {
        ActivePokemon plyr = player.front();
        ActivePokemon opp = opponent.front();

        turn++;
        plyr.resetTurn();
        opp.resetTurn();

        // Fucking focus punch
        if (isFighting(true)) {
            plyr.getAttack().startTurn(this, plyr);
        }

        if (isFighting(false)) {
            opp.getAttack().startTurn(this, opp);
        }
    }

    public boolean isFirstAttack() {
        return firstAttacking;
    }

    // If the trainer selected an attack, this will return true - Wild Pokemon will always return true
    // It will return false if the trainer tried to run, switch Pokemon, or used an item
    private boolean isFighting(boolean isPlayer) {
        return this.getTrainer(isPlayer).getAction() == TrainerAction.FIGHT;
    }

    // If the trainer selected Switch, this will return true -- Wild Pokemon will always return false
    // It will return false if the trainer tried to run, use an attack, or use an item
    public boolean isSwitching(boolean isPlayer) {
        return this.getTrainer(isPlayer).getAction() == TrainerAction.SWITCH;
    }

    private boolean deadUser() {
        // Front Pokemon is still functioning
        if (!player.front().isFainted(this)) {
            return false;
        }

        // Dead Front Pokemon, but you still have others to spare -- force a switch
        if (!player.blackout(this)) {
            Messages.add(new MessageUpdate("What Pokemon would you like to switch to?").withUpdate(MessageUpdateType.FORCE_SWITCH));
            return false;
        }

        // Blackout -- you're fucked
        Messages.add(player.getName() + " is out of usable " + PokeString.POKEMON + "! " + player.getName() + " blacked out!");

        // Sucks to suck
        if (opponent instanceof Trainer) {
            Trainer opp = (Trainer)opponent;
            int cashMoney = player.sucksToSuck(opp.getDatCashMoney());
            Messages.add(opp.getName() + " rummaged through the pockets of your passed out body and stole " + cashMoney + " pokedollars!!!");
        }

        player.healAll();
        ((Player)player).teleportToPokeCenter();

        Messages.clearMessages(MessageState.MAPPITY_MAP);
        Messages.add(new MessageUpdate().withUpdate(MessageUpdateType.EXIT_BATTLE));

        return true;
    }

    private boolean deadOpponent() {
        ActivePokemon dead = opponent.front();
        Player player = (Player)this.player;

        // YOU'RE FINE
        if (!dead.isFainted(this)) {
            return false;
        }

        // Gain dat EXP
        player.gainEXP(dead, this);

        // You have achieved total victory
        if (opponent.blackout(this)) {
            player.winBattle(this, opponent);
            return true;
        }

        // We know this is not a wild battle anymore and I don't feel like casting so much
        Trainer opp = (Trainer)opponent;

        // They still have some Pokes left
        opp.switchToRandom(this);
        enterBattle(opp.front());

        return false;
    }

    public void enterBattle(ActivePokemon enterer) {
        enterBattle(enterer, this.getTrainer(enterer).getEnterBattleMessage());
    }

    // Need to use EnterBattleMessageGetter instead of String in case of NameChanger effect
    public void enterBattle(ActivePokemon enterer, EnterBattleMessageGetter enterMessage) {
        if (!enterer.canFight()) {
            Global.error(enterer.getName() + " cannot fight!!!!");
        }

        // Document sighting in the Pokedex
        if (!enterer.isPlayer() && !isSimulating()) {
            ((Player)player).getPokedex().setSeen(enterer, isWildBattle());
        }

        enterer.resetAttributes();
        NameChanger.setNameChanges(this, enterer);

        Messages.add(new MessageUpdate(enterMessage.enterBattleMessage(enterer)).withSwitch(this, enterer));

        EntryEffect.invokeEntryEffect(this, enterer);

        getTrainer(!enterer.isPlayer()).resetUsed();
        enterer.setUsed(true);
    }

    public boolean runAway() {
        if (opponent instanceof Trainer) {
            Messages.add("There's no running from a trainer battle!");
            return false;
        }

        escapeAttempts++;

        ActivePokemon plyr = player.front();
        ActivePokemon opp = opponent.front();

        if (DefiniteEscape.canDefinitelyEscape(this, plyr)) {
            return true;
        } else if (!plyr.canEscape(this)) {
            return false;
        }

        int pSpeed = Stat.getStat(Stat.SPEED, plyr, opp, this);
        int oSpeed = Stat.getStat(Stat.SPEED, opp, plyr, this);

        int val = (int)((pSpeed*32.0)/(oSpeed/4.0) + 30.0*escapeAttempts);
        if (RandomUtils.chanceTest(val, 256)) {
            Messages.add("Got away safely!");
            return true;
        }

        Messages.add("Can't escape!");
        ((Player)player).performAction(this, TrainerAction.RUN);
        return false;
    }

    private boolean isFront(ActivePokemon p) {
        return p == getTrainer(p).front();
    }

    private void executionSolution(boolean firstAttacking, boolean playerFirst) {
        this.firstAttacking = firstAttacking;

        ActivePokemon me = firstAttacking == playerFirst ? player.front() : opponent.front();
        ActivePokemon o = this.getOtherPokemon(me);

        if (isSwitching(me.isPlayer())) {
            Trainer trainer = (Trainer)getTrainer(me);
            trainer.performSwitch(this);
            return;
        }

        // Don't do anything if they're not actually attacking
        if (!isFighting(me.isPlayer()) || !isFront(me)) {
            return;
        }

        boolean success = false;

        me.startAttack(this);

        // HOLD IT RIGHT THERE! YOU MAY NOT BE ABLE TO ATTACK!
        if (ableToAttack(me, o)) {
            // Made it, suckah!
            success = executeAttack(me, o);
        } else {
            me.getAttack().totalAndCompleteFailure(this, me, o);
        }

        me.endAttack(o, success);

        // Can't use me and o in case there was a switch mid-turn
        Messages.add(new MessageUpdate().updatePokemon(this, player.front()));
        Messages.add(new MessageUpdate().updatePokemon(this, opponent.front()));
    }

    public void printAttacking(ActivePokemon p) {
        Attack attack = p.getAttack();

        // Display the charge message instead
        if (attack instanceof MultiTurnMove) {
            MultiTurnMove multiTurnMove = (MultiTurnMove)attack;
            if (multiTurnMove.isCharging()) {
                Messages.add(multiTurnMove.getChargeMessage(p));
                return;
            }
        }

        Messages.add((p.isPlayer() ? "" : "Enemy ") + p.getName() + " used " + attack.getName() + "!");
        p.setReducePP(true);
    }

    // Executes the attack including accuracy checks, returns whether or not the move hit
    public boolean executeAttack(ActivePokemon me, ActivePokemon o) {
        Attack attack = me.getAttack();
        attack.beginAttack(this, me, o);

        printAttacking(me);

        // Check if the move actually hits!
        boolean attackHit = accuracyCheck(me, o);
        boolean success = false;
        if (attackHit) {
            if (me.isPlayer() && !this.isSimulating()) {
                Game.getPlayer().getMedalCase().useMove(attack.namesies());
            }

            me.count();

            success = attack.apply(me, o, this);
            me.setLastMoveSucceeded(success);

            me.getMove().setUsed();
            me.decay();
        } else {
            Messages.add(me.getName() + "'s attack missed!");
            CrashDamageMove.invokeCrashDamageMove(this, me);
        }

        if (!success) {
            attack.totalAndCompleteFailure(this, me, o);
        }

        attack.endAttack(this, me, o);
        return attackHit;
    }

    public void addEffect(BattleEffect<? extends BattleEffectNamesies> effect) {
        this.effects.add(effect);
    }

    public BattleEffectList getEffects() {
        return effects;
    }

    public TeamEffectList getEffects(ActivePokemon teamMember) {
        return getEffects(teamMember.isPlayer());
    }

    public TeamEffectList getEffects(boolean isPlayer) {
        return isPlayer ? player.getEffects() : opponent.getEffects();
    }

    public List<InvokeEffect> getEffectsList(ActivePokemon p, InvokeEffect... additionalItems) {
        return this.getEffectsList(p, true, additionalItems);
    }

    public List<InvokeEffect> getEffectsList(ActivePokemon p, boolean includeItem, InvokeEffect... additionalItems) {
        List<InvokeEffect> list = new ArrayList<>();
        Collections.addAll(list, additionalItems);

        list.addAll(p.getAllEffects(this, includeItem));
        list.addAll(this.getEffects(p).asList());
        list.addAll(this.getEffects().asList());

        return list;
    }

    public Team getTrainer(ActivePokemon pokemon) {
        return getTrainer(pokemon.isPlayer());
    }

    public Team getTrainer(boolean isPlayer) {
        return isPlayer ? player : opponent;
    }

    public ActivePokemon getOtherPokemon(ActivePokemon pokemon) {
        return getOtherPokemon(pokemon.isPlayer());
    }

    // Returns the current Pokemon that is out on the team opposite to the one passed in
    public ActivePokemon getOtherPokemon(boolean isPlayer) {
        return isPlayer ? opponent.front() : player.front();
    }

    public boolean isWildBattle() {
        return opponent instanceof WildPokemon;
    }

    public int calculateDamage(ActivePokemon me, ActivePokemon o) {
        final Stat attacking;
        final Stat defending;
        switch (me.getAttack().getCategory()) {
            case PHYSICAL:
                attacking = Stat.ATTACK;
                defending = Stat.DEFENSE;
                break;
            case SPECIAL:
                attacking = Stat.SP_ATTACK;
                defending = Stat.SP_DEFENSE;
                break;
            default:
                Global.error("Invalid category " + me.getAttack().getCategory() + " for calculating damage");
                return -1;
        }

        int level = me.getLevel();
        int random = RandomUtils.getRandomInt(16) + 85;

        int power = me.getAttack().getPower(this, me, o);
        power *= getDamageModifier(me, o);

        int attackStat = Stat.getStat(attacking, me, o, this);
        int defenseStat = Stat.getStat(defending, o, me, this);

        double stab = TypeAdvantage.getSTAB(this, me);
        double adv = TypeAdvantage.getAdvantage(me, o, this);

//                System.out.printf("%s %s %d %d %d %d %d %f %f %d%n",
//                me.getActualName(),
//                me.getAttack().getName(),
//                level,
//                power,
//                random,
//                attackStat,
//                defenseStat,
//                stab,
//                adv,
//                damage);

        return (int)Math.ceil(((((2*level/5.0 + 2)*attackStat*power/defenseStat)/50.0) + 2)*stab*adv*random/100.0);
    }

    protected double getDamageModifier(ActivePokemon me, ActivePokemon o) {
        return PowerChangeEffect.getModifier(this, me, o)*OpponentPowerChangeEffect.getModifier(this, me, o);
    }

    public boolean criticalHit(ActivePokemon me, ActivePokemon o) {
        if (CritBlockerEffect.checkBlocked(this, me, o)) {
            return false;
        }

        if (AlwaysCritEffect.defCritsies(this, me, o)) {
            return true;
        }

        // Increase crit stage and such
        int stage = getCritStage(me);

        return RandomUtils.chanceTest(1, CRITSICLES[stage - 1]);
    }

    public int getCritStage(ActivePokemon me) {
        int stage = 1;
        stage = CritStageEffect.updateCritStage(this, stage, me);
        stage = Math.min(stage, CRITSICLES.length); // Max it out, yo
        return stage;
    }

    protected Boolean bypassAccuracy(ActivePokemon me, ActivePokemon o) {
        Attack attack = me.getAttack();

        // Self-Target moves don't miss
        if (attack.isSelfTargetStatusMove()) {
            return true;
        }

        // Neither do field moves
        if (attack.isMoveType(MoveType.FIELD)) {
            return true;
        }

        // Cannot miss the charge
        if (attack instanceof MultiTurnMove && ((MultiTurnMove)attack).isCharging()) {
            return true;
        }

        // Effects that allow the user to bypass the accuracy check before the semi-invulnerable check
        if (SemiInvulnerableBypasser.bypassAccuracyCheck(this, me, o)) {
            return true;
        }

        // Opponent effects that always allow the user to hit them
        if (OpponentAccuracyBypassEffect.bypassAccuracyCheck(this, me, o)) {
            return true;
        }

        // Semi-invulnerable target -- automatic miss (unless a previous condition was triggered)
        if (o.isSemiInvulnerable()) {
            return false;
        }

        // Effects that allow the user to bypass the accuracy check after the semi-invulnerable check
        if (BasicAccuracyBypassEffect.bypassAccuracyCheck(this, me, o)) {
            return true;
        }

        // Just use plain old random numbers
        return null;
    }

    protected boolean accuracyCheck(ActivePokemon me, ActivePokemon o) {
        Boolean bypass = bypassAccuracy(me, o);
        if (bypass != null) {
            return bypass;
        }

        int moveAccuracy = me.getAttack().getAccuracy(this, me, o);
        int accuracy = Stat.getStat(Stat.ACCURACY, me, o, this);
        int evasion = Stat.getStat(Stat.EVASION, o, me, this);

        return RandomUtils.chanceTest((int)(moveAccuracy*((double)accuracy/(double)evasion)));
    }

    // Returns true if the Pokemon is able to execute their turn by checking effects that have been casted upon them
    // This is where BeforeTurnEffects are handled
    private boolean ableToAttack(ActivePokemon p, ActivePokemon opp) {
        // Dead Pokemon can't attack and it's not nice to attack a deady
        if (p.isFainted(this) || opp.isFainted(this)) {
            return false;
        }

        // Loop through all tha effects and do them checks
        if (BeforeTurnEffect.checkCannotAttack(p, opp, this)) {
            return false;
        }

        // WOOOOOOOOOO
        return true;
    }

    public int getAttackPriority(ActivePokemon p) {
        return p.getAttack().getPriority(this, p) + PriorityChangeEffect.getModifier(this, p);
    }

    // Returns the priority of the current action the trainer of the pokemon is performing
    private int getPriority(ActivePokemon p) {
        return this.getTrainer(p).getAction().getPriority(this, p);
    }

    // Returns true if the player will be attacking first, and false if the opponent will be
    public boolean speedPriority() {
        ActivePokemon plyr = player.front();
        ActivePokemon opp = opponent.front();

        // Higher priority always goes first
        int pPriority = getPriority(plyr);
        int oPriority = getPriority(opp);

        if (pPriority != oPriority) {
            return pPriority > oPriority;
        }

        // Slight bias towards the player here
        if (StrikeFirstEffect.checkStrikeFirst(this, plyr)) {
            return true;
        } else if (StrikeFirstEffect.checkStrikeFirst(this, opp)) {
            return false;
        }

        // Trick Room makes the slower Pokemon go first
        boolean reverse = hasEffect(StandardBattleEffectNamesies.TRICK_ROOM);

        // Pokemon that are stalling go last, if both are stalling, the slower one goes first
        boolean pStall = StallingEffect.containsStallingEffect(this, plyr);
        boolean oStall = StallingEffect.containsStallingEffect(this, opp);

        if (pStall && oStall) {
            reverse = true;
        } else if (pStall) {
            return false;
        } else if (oStall) {
            return true;
        }

        // Get the speeds of the Pokemon
        int pSpeed = getSpeedStat(plyr, opp);
        int oSpeed = getSpeedStat(opp, plyr);

        // Speeds are equal -- alternate
        if (pSpeed == oSpeed) {
            return turn%2 == 0;
        }

        // Return the faster Pokemon (or slower if reversed)
        return reverse ? oSpeed > pSpeed : oSpeed < pSpeed;
    }

    protected int getSpeedStat(ActivePokemon statPokemon, ActivePokemon otherPokemon) {
        return Stat.getStat(Stat.SPEED, statPokemon, otherPokemon, this);
    }

    @FunctionalInterface
    public interface EnterBattleMessageGetter {
        String enterBattleMessage(ActivePokemon enterer);
    }
}
