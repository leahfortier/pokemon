package battle;

import battle.DamageCalculator.DamageCalculation;
import battle.attack.Attack;
import battle.effect.EffectNamesies.BattleEffectNamesies;
import battle.effect.InvokeEffect;
import battle.effect.InvokeInterfaces.AttackMissedEffect;
import battle.effect.InvokeInterfaces.BasicAccuracyBypassEffect;
import battle.effect.InvokeInterfaces.BeforeAttackPreventingEffect;
import battle.effect.InvokeInterfaces.DefiniteEscape;
import battle.effect.InvokeInterfaces.EntryEffect;
import battle.effect.InvokeInterfaces.NameChanger;
import battle.effect.InvokeInterfaces.OpponentAccuracyBypassEffect;
import battle.effect.InvokeInterfaces.SemiInvulnerableBypasser;
import battle.effect.InvokeInterfaces.StallingEffect;
import battle.effect.InvokeInterfaces.StartAttackEffect;
import battle.effect.InvokeInterfaces.StrikeFirstEffect;
import battle.effect.attack.MultiTurnMove;
import battle.effect.battle.BattleEffect;
import battle.effect.battle.StandardBattleEffectNamesies;
import main.Game;
import main.Global;
import map.overworld.TerrainType;
import message.MessageUpdate;
import message.MessageUpdateType;
import message.Messages;
import message.Messages.MessageState;
import pattern.action.UpdateMatcher;
import pokemon.stat.Stat;
import trainer.EnemyTrainer;
import trainer.Opponent;
import trainer.PlayerTrainer;
import trainer.SimulatedPlayer;
import trainer.Team;
import trainer.Trainer;
import trainer.TrainerAction;
import trainer.WildPokemon;
import trainer.player.Player;
import trainer.player.medal.MedalTheme;
import util.RandomUtils;
import util.serialization.Serializable;
import util.string.PokeString;
import util.string.StringAppender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Battle implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final DamageCalculator damageCalculator;

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
        this(opponent, new DamageCalculator());
    }

    protected Battle(Opponent opponent, DamageCalculator damageCalculator) {
        Messages.clearMessages(MessageState.FIGHTY_FIGHT);
        Messages.setMessageState(MessageState.FIGHTY_FIGHT);
        Messages.add(new MessageUpdate().withUpdate(MessageUpdateType.ENTER_BATTLE));

        Player player = Game.getPlayer();
        player.getMedalCase().increase(MedalTheme.BATTLES_BATTLED);

        this.damageCalculator = damageCalculator;

        this.player = player;
        this.opponent = opponent;

        this.player.enterBattle(this);
        this.opponent.enterBattle(this);

        this.effects = new BattleEffectList();
        this.effects.initialize(this);

        this.turn = 0;
        this.escapeAttempts = 0;
        this.firstAttacking = false;

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
        ActivePokemon first = playerFirst ? player.front() : opponent.front();
        ActivePokemon second = this.getOtherPokemon(first);

        // First turn
        executionSolution(true, first);

        // Second turn
        executionSolution(false, second);

        effects.endTurn();

        // Is this the end?
        checkExit();

        printShit();
    }

    public boolean isSimulating() {
        return this.player instanceof SimulatedPlayer;
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
                .append(" " + p.getHeldItem().getName() + " ")
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
        if (isFighting(plyr)) {
            plyr.getMove().startTurn(this, plyr);
        }

        if (isFighting(opp)) {
            opp.getMove().startTurn(this, opp);
        }
    }

    public boolean isFirstAttack() {
        return firstAttacking;
    }

    // If the trainer selected an attack, this will return true - Wild Pokemon will always return true
    // It will return false if the trainer tried to run, switch Pokemon, or used an item
    private boolean isFighting(ActivePokemon fighter) {
        return this.isAction(fighter, TrainerAction.FIGHT);
    }

    // If the trainer selected Switch, this will return true -- Wild Pokemon will always return false
    // It will return false if the trainer tried to run, use an attack, or use an item
    public boolean isSwitching(ActivePokemon switchPokemon) {
        return this.isAction(switchPokemon, TrainerAction.SWITCH);
    }

    // Returns true if the trainer has selected the specified action for the current turn (fight/switch/item/run)
    private boolean isAction(ActivePokemon p, TrainerAction action) {
        return this.getTrainer(p).getAction() == action;
    }

    // Handles exiting the battle and swapping dead Pokemon if relevant
    private void checkExit() {
        if (this.isSimulating()) {
            return;
        }

        boolean playerDead = player.front().isFainted(this);
        boolean opponentDead = opponent.front().isFainted(this);

        // Both front Pokemon are still functioning
        if (!playerDead && !opponentDead) {
            return;
        }

        // At least one Pokemon is dead
        boolean playerBlackout = player.blackout(this);
        boolean opponentBlackout = opponent.blackout(this);

        Player player = (Player)this.player;

        // There's no escape from this one...
        if (playerBlackout) {
            player.loseBattle(opponent);
            return;
        }

        // They dead (not necessarily completely but still)
        if (opponentDead) {
            // Gain dat EXP
            player.gainEXP(opponent.front(), this);

            // You have achieved total victory
            if (opponentBlackout) {
                player.winBattle(this, opponent);
                return;
            }
        }

        // Dead Front Pokemon, but you still have others to spare -- force a switch
        if (playerDead) {
            Messages.add(new MessageUpdate("What " + PokeString.POKEMON + " would you like to switch to?")
                                 .withUpdate(MessageUpdateType.FORCE_SWITCH));
        }

        // We know this is not a wild battle anymore and they still have some Pokes left so send them out
        if (opponentDead) {
            ((Trainer)opponent).switchToRandom(this);
            enterBattle(opponent.front());
        }
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

        getOtherTrainer(enterer).resetUsed();
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

        // Only use the stage for the speed estimate
        int pSpeed = Stat.SPEED.getBasicStat(this, plyr);
        int oSpeed = Stat.SPEED.getBasicStat(this, opp);

        int val = (int)((pSpeed*32.0)/(oSpeed/4.0) + 30.0*escapeAttempts);
        if (RandomUtils.chanceTest(val, 256)) {
            Messages.add("Got away safely!");
            return true;
        }

        Messages.add("Can't escape!");
        ((Player)player).performAction(this, TrainerAction.RUN);
        return false;
    }

    public boolean isFront(ActivePokemon p) {
        return p == getTrainer(p).front();
    }

    private void executionSolution(boolean firstAttacking, ActivePokemon me) {
        this.firstAttacking = firstAttacking;

        // Kind of hacky solution to check if the battle is ended via catching the opponent Pokemon
        // Anyways in that case they shouldn't execute their turn because we're all friends now...
        // Also the isPlayer field being set like this fucks a lot of shit up soooo
        if (opponent.front().isPlayer()) {
            return;
        }

        ActivePokemon o = this.getOtherPokemon(me);

        if (isSwitching(me)) {
            Trainer trainer = (Trainer)getTrainer(me);
            trainer.performSwitch(this);
            return;
        } else if (this.isAction(me, TrainerAction.ITEM)) {
            Trainer trainer = (Trainer)getTrainer(me);
            trainer.getBag().battleUseItem(this, trainer);
            return;
        }

        // Don't do anything if they're not actually attacking
        if (!isFighting(me) || !isFront(me)) {
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
        // Setup attack effects
        // Note: important to be before printing for things like MultiTurn moves
        Attack attack = me.getAttack();
        attack.beginAttack(this, me, o);

        printAttacking(me);
        StartAttackEffect.checkBeforeAttack(this, me, o);

        // Check if the move actually hits!
        boolean attackHit = accuracyCheck(me, o);
        boolean success = false;
        if (attackHit) {
            if (me.isPlayer()) {
                Game.getPlayer().getMedalCase().useMove(attack.namesies());
            }

            me.count();

            success = attack.apply(me, o, this);
            me.setLastMoveSucceeded(success);

            me.getMove().setUsed();
            me.decay();
        } else {
            Messages.add(me.getName() + "'s attack missed!");
            AttackMissedEffect.invokeAttackMissedEffect(this, me);
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

    public List<InvokeEffect> getEffectsList(ActivePokemon p, InvokeEffect... additionalItems) {
        List<InvokeEffect> list = this.getIndividualAndTeamEffects(p, additionalItems);
        list.addAll(this.getEffects().asListNoWeather());
        list.add(p.getWeather(this));
        return list;
    }

    // Only includes PokemonEffects and TeamEffects, but not BattleEffects (including Weather and Terrain)
    public List<InvokeEffect> getIndividualAndTeamEffects(ActivePokemon p, InvokeEffect... additionalItems) {
        List<InvokeEffect> list = new ArrayList<>();
        Collections.addAll(list, additionalItems);

        list.addAll(p.getAllEffects());
        list.addAll(this.getTrainer(p).getEffects().asList());

        return list;
    }

    public Team getOtherTrainer(ActivePokemon pokemon) {
        return this.getTrainer(this.getOtherPokemon(pokemon));
    }

    public Team getTrainer(ActivePokemon pokemon) {
        return getTrainer(pokemon.isPlayer());
    }

    public Team getTrainer(boolean isPlayer) {
        return isPlayer ? player : opponent;
    }

    // Returns the current Pokemon that is out on the team opposite to the one passed in
    public ActivePokemon getOtherPokemon(ActivePokemon pokemon) {
        return pokemon.isPlayer() ? opponent.front() : player.front();
    }

    public boolean isWildBattle() {
        return opponent instanceof WildPokemon;
    }

    public DamageCalculation calculateDamage(ActivePokemon me, ActivePokemon o) {
        return this.damageCalculator.calculateDamage(this, me, o);
    }

    protected Boolean bypassAccuracy(ActivePokemon me, ActivePokemon o) {
        Attack attack = me.getAttack();

        // Self-Target moves, field moves, charging Multi-turn moves, etc
        if (attack.ignoreAccuracyCheck()) {
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

    // Returns true if the move should hit based on normal things like accuracy and evasion
    // Does not take things like semi-invulnerable or similar effects into account (should be handled first)
    protected boolean naturalAccuracyCheck(ActivePokemon me, ActivePokemon o) {
        int moveAccuracy = me.getAttack().getAccuracy(this, me, o);
        int accuracy = Stat.getStat(Stat.ACCURACY, me, o, this);
        int evasion = Stat.getStat(Stat.EVASION, o, me, this);

        return RandomUtils.chanceTest((int)(moveAccuracy*((double)accuracy/(double)evasion)));
    }

    public boolean accuracyCheck(ActivePokemon me, ActivePokemon o) {
        Boolean bypass = bypassAccuracy(me, o);
        boolean attackHit = bypass != null ? bypass : this.naturalAccuracyCheck(me, o);

        me.getMoveData().setAccuracyCheck(bypass, attackHit);
        return attackHit;
    }

    // Returns true if the Pokemon is able to execute their turn by checking effects that have been casted upon them
    // This is where BeforeTurnEffects are handled
    private boolean ableToAttack(ActivePokemon p, ActivePokemon opp) {
        // Dead Pokemon can't attack and it's not nice to attack a deady
        if (p.isFainted(this) || opp.isFainted(this)) {
            return false;
        }

        // Loop through all tha effects and do them checks
        if (BeforeAttackPreventingEffect.checkCannotAttack(this, p, opp)) {
            return false;
        }

        // WOOOOOOOOOO
        return true;
    }

    // Returns the priority of the current action the trainer of the pokemon is performing
    private int getPriority(ActivePokemon p) {
        return this.getTrainer(p).getAction().getPriority(p);
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
