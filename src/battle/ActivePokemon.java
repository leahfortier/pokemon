package battle;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.attack.MoveType;
import battle.effect.CastSource;
import battle.effect.EffectList;
import battle.effect.InvokeEffect;
import battle.effect.attack.MultiTurnMove;
import battle.effect.generic.EffectInterfaces.AbsorbDamageEffect;
import battle.effect.generic.EffectInterfaces.BracingEffect;
import battle.effect.generic.EffectInterfaces.ChangeMoveListEffect;
import battle.effect.generic.EffectInterfaces.ChangeTypeEffect;
import battle.effect.generic.EffectInterfaces.DamageTakenEffect;
import battle.effect.generic.EffectInterfaces.DifferentStatEffect;
import battle.effect.generic.EffectInterfaces.GroundedEffect;
import battle.effect.generic.EffectInterfaces.HalfWeightEffect;
import battle.effect.generic.EffectInterfaces.ItemBlockerEffect;
import battle.effect.generic.EffectInterfaces.ItemSwapperEffect;
import battle.effect.generic.EffectInterfaces.LevitationEffect;
import battle.effect.generic.EffectInterfaces.MurderEffect;
import battle.effect.generic.EffectInterfaces.NameChanger;
import battle.effect.generic.EffectInterfaces.OpponentItemBlockerEffect;
import battle.effect.generic.EffectInterfaces.OpponentTrappingEffect;
import battle.effect.generic.EffectInterfaces.SwapOpponentEffect;
import battle.effect.generic.EffectInterfaces.TrappingEffect;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.PokemonEffect;
import battle.effect.holder.AbilityHolder;
import battle.effect.holder.ItemHolder;
import battle.effect.status.Status;
import battle.effect.status.StatusCondition;
import item.Item;
import item.ItemNamesies;
import item.berry.Berry;
import item.berry.GainableEffectBerry;
import item.hold.EVItem;
import item.hold.HoldItem;
import main.Game;
import main.Global;
import message.MessageUpdate;
import message.MessageUpdateType;
import message.Messages;
import pokemon.PartyPokemon;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;
import pokemon.Stat;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import pokemon.breeding.Eggy;
import pokemon.evolution.BaseEvolution;
import pokemon.evolution.EvolutionMethod;
import sound.SoundTitle;
import trainer.Team;
import trainer.Trainer;
import trainer.WildPokemon;
import trainer.player.medal.MedalTheme;
import type.Type;
import util.Action;
import util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ActivePokemon extends PartyPokemon {
    private static final long serialVersionUID = 1L;

    private EffectList<PokemonEffect> effects;
    private Stages stages;
    private Move selected;
    private Move lastMoveUsed;
    private Serializable castSource;
    private int counter;
    private int damageTaken;
    private double successionDecayRate;
    private boolean firstTurn;
    private boolean attacking;
    private boolean reducePP;
    private boolean used;
    private boolean battleUsed;
    private boolean lastMoveSucceeded;

    // General constructor for an active Pokemon (isPlayer is true if it is the player's pokemon and false if it is wild, enemy trainer, etc.)
    public ActivePokemon(PokemonNamesies pokemonNamesies, int level, boolean isWild, boolean isPlayer) {
        super(pokemonNamesies, level, isWild, isPlayer);
    }

    public ActivePokemon(Eggy eggy) {
        super(eggy);
    }

    public Type computeHiddenPowerType() {
        return Type.getHiddenType(((
                this.getIV(Stat.HP)%2 +
                        2*(this.getIV(Stat.ATTACK)%2) +
                        4*(this.getIV(Stat.DEFENSE)%2) +
                        8*(this.getIV(Stat.SPEED)%2) +
                        16*(this.getIV(Stat.SP_ATTACK)%2) +
                        32*(this.getIV(Stat.SP_DEFENSE)%2)
        )*15)/63);
    }

    public Ability getAbility() {

        // Check if the Pokemon has had its ability changed during the battle
        PokemonEffect effect = getEffect(EffectNamesies.CHANGE_ABILITY);
        if (effect != null) {
            return ((AbilityHolder)effect).getAbility();
        }

        return this.getActualAbility();
    }

    public int getStage(Stat stat) {
        return this.getStages().getStage(stat);
    }

    public int getStat(Battle b, Stat s) {
        Integer stat = DifferentStatEffect.getStat(b, this, s);
        if (stat != null) {
            return stat;
        }

        return this.getStat(s);
    }

    public List<Move> getMoves(Battle b) {
        List<Move> actualMoves = this.getActualMoves();
        if (b == null) {
            return actualMoves;
        }

        List<Move> changedMoveList = ChangeMoveListEffect.getMoveList(b, this, actualMoves);
        if (changedMoveList != null) {
            return changedMoveList;
        }

        return actualMoves;
    }

    public void gainEXP(Battle b, int gain, PartyPokemon dead) {
        boolean front = b.getPlayer().front() == this;

        // Add EXP
        super.gainEXP(gain);
        Messages.add(getActualName() + " gained " + gain + " EXP points!");
        if (front) {
            Messages.add(new MessageUpdate().withExpGain(b, this, Math.min(1, expRatio()), false));
        }

        // Add EVs
        Item item = this.getHeldItem(b);
        int[] vals = dead.getPokemonInfo().getGivenEVs();
        if (item instanceof EVItem) {
            vals = ((EVItem)item).getEVs(vals);
        }
        this.addEVs(vals);

        // Level up if applicable
        while (this.getTotalEXP() >= this.getPokemonInfo().getGrowthRate().getEXP(this.getLevel() + 1)) {
            levelUp(b);
        }
    }

    public boolean levelUp(Battle b) {
        if (this.getLevel() == MAX_LEVEL) {
            return false;
        }

        boolean inBattle = b != null;
        boolean front = inBattle && b.getPlayer().front() == this;

        // Grow to the next level
        int[] gain = super.levelUp();
        Messages.add(new MessageUpdate(this.getActualName() + " grew to level " + this.getLevel() + "!").withSoundEffect(SoundTitle.LEVEL_UP));

        if (front) {
            Messages.add(new MessageUpdate().withExpGain(b, this, Math.min(1, expRatio()), true));
            Messages.add(new MessageUpdate().updatePokemon(b, this));
        }

        Messages.add(new MessageUpdate().withStatGains(gain, this.getClonedStats()));

        // Learn new moves
        this.getPokemonInfo().getMoves(this.getLevel()).forEach(attackNamesies -> learnMove(attackNamesies, inBattle));

        // Maybe you'll evolve?!
        // Can only evolve outside of battle
        if (!inBattle) {
            EvolutionMethod.LEVEL.checkEvolution(this);
        }

        return true;
    }

    // Returns stat gains
    @Override
    public int[] evolve(BaseEvolution evolution) {
        int[] gain = super.evolve(evolution);

        // Learn new moves
        List<AttackNamesies> levelMoves = this.getPokemonInfo().getMoves(PokemonInfo.EVOLUTION_LEVEL_LEARNED);
        levelMoves.forEach(attack -> learnMove(attack, false));

        levelMoves = this.getPokemonInfo().getMoves(this.getLevel());
        levelMoves.forEach(attack -> learnMove(attack, false));

        return gain;
    }

    private void learnMove(AttackNamesies attackName, boolean inBattle) {
        // Don't want to learn a move you already know!
        if (hasActualMove(attackName)) {
            return;
        }

        Move move = new Move(attackName);
        if (this.numMoves() < Move.MAX_MOVES) {
            addMove(move, this.numMoves() - 1, inBattle);
        } else {
            // Need a non-empty message so that it doesn't get absorbed
            Messages.add(new MessageUpdate(" ").withLearnMove(this, move));
        }
    }

    public void addMove(Move m, int index, boolean inBattle) {
        Messages.add(getActualName() + " learned " + m.getAttack().getName() + "!");
        super.addMove(m, index);

        if (!inBattle) {
            EvolutionMethod.MOVE.checkEvolution(this);
        }
    }

    public void callFullNewMove(Battle b, ActivePokemon opp, AttackNamesies attack) {
        this.callFullNewMove(b, opp, new Move(attack));
    }

    // Calls a new move -- wil reperform the accuracy check for this method and print attacking
    public void callFullNewMove(Battle b, ActivePokemon opp, Move newMove) {
        this.callTempMove(newMove, () -> {
            this.startAttack(b);
            b.executeAttack(this, opp);
        });
    }

    // Calls a new move -- prints attacking but does not reperform accuracy check
    public void callNewMove(Battle b, ActivePokemon opp, AttackNamesies attack) {
        this.callTempMove(attack, () -> {
            b.printAttacking(this);
            this.startAttack(b);
            this.getAttack().apply(this, opp, b);
        });
    }

    public void callTempMove(AttackNamesies tempMove, Action moveAction) {
        this.callTempMove(new Move(tempMove), moveAction);
    }

    // Sets the temporary move and performs the action with that move, then resets back to the original move
    private void callTempMove(Move tempMove, Action moveAction) {
        Move currentMove = getMove();
        setMove(tempMove);
        moveAction.performAction();
        setMove(currentMove);
    }

    // Wild Pokemon if in a wild battle and not the player's pokemon
    public boolean isWildPokemon(Battle b) {
        return b.isWildBattle() && !this.isPlayer();
    }

    public boolean canStealItem(Battle b, ActivePokemon victim) {
        return !this.isHoldingItem(b)
                && victim.isHoldingItem(b)
                && this.canSwapItems(b, victim);
    }

    public boolean canGiftItem(Battle b, ActivePokemon receiver) {
        return this.isHoldingItem(b) && !receiver.isHoldingItem(b);
    }

    public boolean canRemoveItem(Battle b, ActivePokemon victim) {
        return victim.isHoldingItem(b) && canSwapItems(b, victim);
    }

    public boolean canSwapItems(Battle b, ActivePokemon swapster) {
        return (this.isHoldingItem(b) || swapster.isHoldingItem(b))
                && !this.isWildPokemon(b)
                && !(swapster.hasAbility(AbilityNamesies.STICKY_HOLD) && !this.breaksTheMold());
    }

    public void swapItems(Battle b, ActivePokemon swapster, ItemSwapperEffect swapsicles) {
        Item userItem = this.getHeldItem(b);
        Item victimItem = swapster.getHeldItem(b);

        Messages.add(swapsicles.getSwitchMessage(this, userItem, swapster, victimItem));

        // For wild battles, an actual switch occurs
        if (b.isWildBattle()) {
            this.giveItem((HoldItem)victimItem);
            swapster.giveItem((HoldItem)userItem);
        } else {
            this.setCastSource(victimItem);
            EffectNamesies.CHANGE_ITEM.getEffect().apply(b, this, this, CastSource.CAST_SOURCE, false);

            this.setCastSource(userItem);
            EffectNamesies.CHANGE_ITEM.getEffect().apply(b, this, swapster, CastSource.CAST_SOURCE, false);
        }
    }

    public boolean canSwapOpponent(Battle b, ActivePokemon victim) {
        if (b.isFirstAttack() || victim.hasEffect(EffectNamesies.INGRAIN)) {
            return false;
        }

        if (victim.hasAbility(AbilityNamesies.SUCTION_CUPS) && !this.breaksTheMold()) {
            return false;
        }

        Team opponent = b.getTrainer(victim);
        if (opponent instanceof WildPokemon) {
            // Fails against wild Pokemon of higher levels
            return victim.getLevel() <= this.getLevel();
        } else {
            // Fails against trainers on their last Pokemon
            Trainer trainer = (Trainer)opponent;
            return trainer.hasRemainingPokemon(b);
        }
    }

    public void swapOpponent(Battle b, ActivePokemon victim, SwapOpponentEffect swapster) {
        if (!canSwapOpponent(b, victim)) {
            return;
        }

        Messages.add(swapster.getSwapMessage(this, victim));

        Team opponent = b.getTrainer(victim);
        if (opponent instanceof WildPokemon) {
            // End the battle against a wild Pokemon
            Messages.add(new MessageUpdate().withUpdate(MessageUpdateType.EXIT_BATTLE));
        } else {
            Trainer trainer = (Trainer)opponent;

            // Swap to a random Pokemon!
            trainer.switchToRandom(b);
            b.enterBattle(trainer.front(), enterer -> "...and " + enterer.getName() + " was dragged out!");
        }
    }

    public boolean switcheroo(Battle b, ActivePokemon caster, CastSource source, boolean wildExit) {
        Team team = b.getTrainer(this);
        String sourceName = source.getSourceName(b, this);
        String selfReference = caster == this ? "it" : this.getName();

        // End the battle against a wild Pokemon
        if (team instanceof WildPokemon) {
            if (!wildExit) {
                return false;
            }

            final String message;
            if (!StringUtils.isNullOrEmpty(sourceName)) {
                message = caster.getName() + "'s " + sourceName + " caused " + selfReference + " to leave the battle!";
            } else {
                message = this.getName() + " left the battle!";
            }

            Messages.add(message);
            Messages.add(new MessageUpdate().withUpdate(MessageUpdateType.EXIT_BATTLE));
            return true;
        }

        Trainer trainer = (Trainer)team;
        if (!trainer.hasRemainingPokemon(b)) {
            // Don't switch if no one to switch to
            return false;
        }

        // Send this Pokemon back to the trainer and send out the next one
        final String message;
        if (!StringUtils.isNullOrEmpty(sourceName)) {
            message = caster.getName() + "'s " + sourceName + " sent " + selfReference + " back to " + trainer.getName() + "!";
        } else {
            message = this.getName() + " went back to " + trainer.getName() + "!";
        }

        Messages.add(message);

        // TODO: Prompt a legit switch fo user
        // TODO: Once this happens, this should take in a random parameter since this is still correct for Red Card, I believe and should have the message "name was sent out!"
        // TODO: Check if trainer action needs to be set to Switch
        trainer.switchToRandom(b);
        b.enterBattle(trainer.front(), enterer -> trainer.getName() + " sent out " + enterer.getName() + "!");

        return true;
    }

    // Pangoro breaks the mold!
    public boolean breaksTheMold() {
        switch (getAbility().namesies()) {
            case MOLD_BREAKER:
            case TURBOBLAZE:
            case TERAVOLT:
                return true;
        }

        return this.hasEffect(EffectNamesies.BREAKS_THE_MOLD);
    }

    @Override
    public boolean canFight() {
        return !this.isActuallyDead();
    }

    public boolean hasAbility(AbilityNamesies a) {
        return getAbility().namesies() == a;
    }

    public Attack getAttack() {
        Move m = this.getMove();
        if (m == null) {
            return null;
        }

        return m.getAttack();
    }

    public boolean isAttackType(Type t) {
        return this.getAttackType() == t;
    }

    public Type getAttackType() {
        return getMove().getType();
    }

    public boolean hasMove(Battle b, AttackNamesies name) {
        return hasMove(getMoves(b), name);
    }

    public boolean isSemiInvulnerable() {
        final Attack attack = this.getAttack();
        if (attack instanceof MultiTurnMove) {
            MultiTurnMove multiTurnMove = (MultiTurnMove)attack;
            return multiTurnMove.isCharging() && multiTurnMove.semiInvulnerability();
        }

        return false;
    }

    public boolean isSemiInvulnerableFlying() {
        return isSemiInvulnerable() && (getAttack().namesies() == AttackNamesies.FLY || getAttack().namesies() == AttackNamesies.BOUNCE);
    }

    public boolean isSemiInvulnerableDigging() {
        return isSemiInvulnerable() && getAttack().namesies() == AttackNamesies.DIG;
    }

    public Type[] getDisplayType(Battle b) {
        return getType(b, true);
    }

    public Type[] getType(Battle b) {
        return getType(b, false);
    }

    private Type[] getType(Battle b, boolean displayOnly) {
        Type[] changeType = ChangeTypeEffect.getChangedType(b, this, displayOnly);
        if (changeType != null) {
            return changeType;
        }

        return getActualType();
    }

    public boolean isType(Battle b, Type type) {
        Type[] types = getType(b);
        return types[0] == type || types[1] == type;
    }

    public String getName() {
        String changedName = NameChanger.getChangedName(this);
        if (changedName != null) {
            return changedName;
        }

        return getActualName();
    }

    @Override
    public void resetAttributes() {
        // Reset ability and each move
        this.setAbility(this.getActualAbility().namesies());
        this.getActualMoves().forEach(Move::resetAttack);

        effects = new EffectList<>();
        stages = new Stages(this);

        selected = null;
        lastMoveUsed = null;
        castSource = null;

        this.resetCount();
        this.resetDamageTaken();
        this.resetDecay();

        used = false;
        battleUsed = false;
        firstTurn = true;
        attacking = false;
        reducePP = false;
        lastMoveSucceeded = true;
    }

    public boolean isActuallyDead() {
        return this.hasStatus(StatusCondition.FAINTED);
    }

    public boolean isFainted(Battle b) {
        // We have already checked that this Pokemon is fainted -- don't print/apply effects more than once
        if (isActuallyDead()) {
            if (this.getHP() == 0) {
                return true;
            }

            Global.error("Pokemon should only have the Fainted Status Condition when HP is zero.");
        }

        // Deady
        if (this.getHP() == 0) {
            Messages.add(new MessageUpdate().updatePokemon(b, this));

            ActivePokemon murderer = b.getOtherPokemon(this);
            Status.die(b, murderer, this);

            // If the pokemon fainted via murder (by direct result of an attack) -- apply kill wishes
            if (murderer.isAttacking()) {
                MurderEffect.killKillKillMurderMurderMurder(b, this, murderer);
            }

            EffectNamesies.DEAD_ALLY.getEffect().cast(b, this, this, CastSource.EFFECT, false);

            // If the player slayed a Dark or Ghost type Pokemon, then they are on their way to becoming the Chosen One
            if (!isPlayer() && (isType(b, Type.DARK) || isType(b, Type.GHOST))) {
                Game.getPlayer().getMedalCase().increase(MedalTheme.DEMON_POKEMON_DEFEATED);
            }

            return true;
        }

        // Still kickin' it
        return false;
    }

    public boolean canEscape(Battle b) {
        // Shed Shell always allows escape
        if (isHoldingItem(b, ItemNamesies.SHED_SHELL)) {
            return true;
        }

        // Check if the user is under an effect that prevents escape
        if (TrappingEffect.isTrapped(b, this)) {
            return false;
        }

        // The opponent has an effect that prevents escape
        ActivePokemon other = b.getOtherPokemon(this);
        if (OpponentTrappingEffect.isTrapped(b, this, other)) {
            return false;
        }

        // Safe and sound
        return true;
    }

    public List<InvokeEffect> getAllEffects(final Battle b) {
        return this.getAllEffects(b, true);
    }

    public List<InvokeEffect> getAllEffects(final Battle b, final boolean includeItem) {
        List<InvokeEffect> list = new ArrayList<>();
        list.addAll(this.getEffects().listEffects());
        list.add(this.getStatus());
        list.add(this.getAbility());

        if (includeItem) {
            list.add(this.getHeldItem(b));
        }

        return list;
    }

    // Don't think you'll make it out alive
    public void killKillKillMurderMurderMurder(Battle b) {
        reduceHealth(b, this.getHP(), false);
    }

    public int reduceHealth(Battle b, int amount) {
        return this.reduceHealth(b, amount, true);
    }

    // Reduces hp by amount, returns the actual amount of hp that was reduced
    public int reduceHealth(Battle b, int amount, boolean checkEffects) {

        // Not actually reducing health...
        if (amount == 0) {
            return 0;
        }

        // Check if the damage will be absorbed by an effect
        if (checkEffects && AbsorbDamageEffect.checkAbsorbDamageEffect(b, this, amount)) {
            return 0;
        }

        boolean fullHealth = fullHealth();

        // Reduce HP and record damage
        int prev = this.getHP();
        this.setHP(prev - amount);
        int taken = prev - this.getHP();
        this.takeDamage(taken);

        // Enduring the hit
        if (this.getHP() == 0 && checkEffects) {
            BracingEffect brace = BracingEffect.getBracingEffect(b, this, fullHealth);
            if (brace != null) {
                taken -= heal(1);

                Messages.add(new MessageUpdate().updatePokemon(b, this));
                Messages.add(brace.braceMessage(this));
            }
        }

        if (!isFainted(b)) {
            Messages.add(new MessageUpdate().updatePokemon(b, this));
            DamageTakenEffect.invokeDamageTakenEffect(b, this);
        }

        return taken;
    }

    // Reduces the amount of health that corresponds to fraction of the pokemon's total health and returns this amount
    public int reduceHealthFraction(Battle b, double fraction) {
        return reduceHealth(b, (int)Math.max(this.getMaxHP()*fraction, 1));
    }

    public Stat getBestBattleStat() {
        Stat bestStat = Stat.ATTACK;
        for (Stat stat : Stat.STATS) {
            if (stat == Stat.HP) {
                continue;
            }

            if (this.getStat(stat) > this.getStat(bestStat)) {
                bestStat = stat;
            }
        }

        return bestStat;
    }

    public boolean isGrounded(Battle b) {
        return GroundedEffect.containsGroundedEffect(b, this);
    }

    public boolean isLevitating(Battle b) {
        return isLevitating(b, null);
    }

    // Returns true if the Pokemon is currently levitating for any reason
    public boolean isLevitating(Battle b, ActivePokemon moldBreaker) {
        if (isGrounded(b)) {
            return false;
        }

        // Flyahs gon' Fly
        return isLevitatingWithoutTypeCheck(b, moldBreaker) || isType(b, Type.FLYING);
    }

    // Returns true if the Pokemon is currently levitating for any reason besides being a flying type pokemon
    // Grounded effect take precedence over levitation effects
    // Obvs levitating if you have a levitation effect
    // Stupid motherfucking Mold Breaker not allowing me to make Levitate a Levitation effect, fuck you Mold Breaker. -- NOT ANYMORE NOW WE HAVE Battle.hasInvoke FUCK YES YOU GO GLENN COCO
    public boolean isLevitatingWithoutTypeCheck(Battle b, ActivePokemon moldBreaker) {
        return !isGrounded(b) && LevitationEffect.containsLevitationEffect(b, this, moldBreaker);
    }

    public void stealBerry(Battle b, ActivePokemon victim) {
        Item item = victim.getHeldItem(b);
        if (item instanceof Berry && !victim.hasAbility(AbilityNamesies.STICKY_HOLD)) {
            Messages.add(this.getName() + " ate " + victim.getName() + "'s " + item.getName() + "!");
            victim.consumeItemWithoutEffects(b);

            if (item instanceof GainableEffectBerry) {
                ((GainableEffectBerry)item).gainBerryEffect(b, this, CastSource.USE_ITEM);
                this.consumeBerry((Berry)item, b);
            }
        }
    }

    private void consumeBerry(Berry consumed, Battle b) {
        // Eat dat berry!!
        EffectNamesies.EATEN_BERRY.getEffect().cast(b, this, this, CastSource.HELD_ITEM, false);

        if (consumed instanceof GainableEffectBerry
                && this.hasAbility(AbilityNamesies.CHEEK_POUCH)
                && !this.fullHealth()) {
            Messages.add(this.getName() + "'s " + this.getAbility().getName() + " restored its health!");
            this.healHealthFraction(1/3.0);
            Messages.add(new MessageUpdate().updatePokemon(b, this));
        }
    }

    private Item consumeItemWithoutEffects(Battle b) {
        Item consumed = getHeldItem(b);
        EffectNamesies.CONSUMED_ITEM.getEffect().cast(b, this, this, CastSource.HELD_ITEM, false);

        return consumed;
    }

    public void consumeItem(Battle b) {
        Item consumed = this.consumeItemWithoutEffects(b);

        if (consumed instanceof Berry) {
            this.consumeBerry((Berry)consumed, b);
        }

        ActivePokemon other = b.getOtherPokemon(this);
        if (other.hasAbility(AbilityNamesies.PICKUP) && !other.isHoldingItem(b)) {
            other.giveItem((HoldItem)consumed);
            Messages.add(other.getName() + " picked up " + getName() + "'s " + consumed.getName() + "!");
        }
    }

    @Override
    public void removeItem() {
        super.removeItem();
        this.effects.remove(EffectNamesies.CHANGE_ITEM);
    }

    public Item getHeldItem(Battle b) {
        if (b == null) {
            return getActualHeldItem();
        }

        if (ItemBlockerEffect.containsItemBlockerEffect(b, this)) {
            return ItemNamesies.NO_ITEM.getItem();
        }

        // Check if the Pokemon has had its item changed during the battle
        PokemonEffect changeItem = getEffect(EffectNamesies.CHANGE_ITEM);
        Item item = changeItem == null ? getActualHeldItem() : ((ItemHolder)changeItem).getItem();

        if (OpponentItemBlockerEffect.checkOpponentItemBlockerEffect(b, b.getOtherPokemon(this), item.namesies())) {
            return ItemNamesies.NO_ITEM.getItem();
        }

        return item;
    }

    public boolean isHoldingItem(Battle b, ItemNamesies itemName) {
        return getHeldItem(b).namesies() == itemName;
    }

    public boolean isHoldingItem(Battle b) {
        return getHeldItem(b).namesies() != ItemNamesies.NO_ITEM;
    }

    public double getWeight(Battle b) {
        int halfAmount = 0;
        halfAmount = HalfWeightEffect.updateHalfAmount(b, this, halfAmount);

        return this.getPokemonInfo().getWeight()/Math.pow(2, halfAmount);
    }

    public void setReducePP(boolean reduce) {
        reducePP = reduce;
    }

    public Object getCastSource() {
        return this.castSource;
    }

    public void setCastSource(Serializable castSource) {
        this.castSource = castSource;
    }

    public boolean isAttacking() {
        return attacking;
    }

    private void setAttacking(boolean isAttacking) {
        attacking = isAttacking;
    }

    void setLastMoveSucceeded(boolean lastMoveSucceeded) {
        this.lastMoveSucceeded = lastMoveSucceeded;
    }

    public boolean lastMoveSucceeded() {
        return this.lastMoveSucceeded;
    }

    @Override
    public boolean isUsed() {
        return used;
    }

    @Override
    public void setUsed(boolean u) {
        used = u;
        if (used) {
            battleUsed = true;
        }
    }

    @Override
    public boolean isBattleUsed() {
        return this.battleUsed;
    }

    public boolean isFirstTurn() {
        return firstTurn;
    }

    void setFirstTurn(boolean isFirstTurn) {
        firstTurn = isFirstTurn;
    }

    public void takeDamage(int damage) {
        damageTaken = damage;
    }

    public int getDamageTaken() {
        return damageTaken;
    }

    public boolean hasTakenDamage() {
        return damageTaken > 0;
    }

    public void resetTurn() {
        resetDamageTaken();
        setReducePP(false);
    }

    private void resetDamageTaken() {
        damageTaken = 0;
    }

    public void setLastMoveUsed() {
        lastMoveUsed = selected;
    }

    public Move getLastMoveUsed() {
        return lastMoveUsed;
    }

    // Increment count if the pokemon uses the same move twice in a row
    public void count() {
        if (lastMoveUsed == null || selected.getAttack().namesies() != lastMoveUsed.getAttack().namesies()) {
            resetCount();
        } else {
            counter++;
        }
    }

    private void resetCount() {
        counter = 1;
    }

    public int getCount() {
        return counter;
    }

    public EffectList<PokemonEffect> getEffects() {
        return effects;
    }

    public double getSuccessionDecayRate() {
        return successionDecayRate;
    }

    public void decay() {
        if (selected.getAttack().isMoveType(MoveType.SUCCESSIVE_DECAY)) {
            successionDecayRate *= .5;
        } else {
            this.resetDecay();
        }
    }

    private void resetDecay() {
        successionDecayRate = 1;
    }

    public Move getMove() {
        return selected;
    }

    public void setMove(Move move) {
        this.selected = move;
    }

    // Returns null if the Pokemon is not under the effects of the input effect, otherwise returns the Effect
    public PokemonEffect getEffect(EffectNamesies effect) {
        return effects.get(effect);
    }

    public boolean hasEffect(EffectNamesies effect) {
        return effects.hasEffect(effect);
    }

    public Stages getStages() {
        return this.stages;
    }

    public void startAttack(Battle b) {
        this.setAttacking(true);
        this.getMove().setAttributes(b, this);
    }

    public void endAttack(ActivePokemon opp, boolean success) {
        if (success) {
            this.setLastMoveUsed();
        } else {
            this.effects.remove(EffectNamesies.SELF_CONFUSION);
            this.resetCount();
        }

        if (this.reducePP) {
            this.getMove().reducePP(opp.hasAbility(AbilityNamesies.PRESSURE) ? 2 : 1);
        }

        this.setAttacking(false);
    }
}
