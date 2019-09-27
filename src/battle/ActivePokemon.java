package battle;

import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.attack.MoveType;
import battle.effect.Effect;
import battle.effect.EffectInterfaces.AbilityHolder;
import battle.effect.EffectInterfaces.ItemHolder;
import battle.effect.EffectInterfaces.PokemonHolder;
import battle.effect.EffectList;
import battle.effect.InvokeEffect;
import battle.effect.InvokeInterfaces.AbsorbDamageEffect;
import battle.effect.InvokeInterfaces.BracingEffect;
import battle.effect.InvokeInterfaces.ChangeMoveListEffect;
import battle.effect.InvokeInterfaces.ChangeTypeEffect;
import battle.effect.InvokeInterfaces.DamageTakenEffect;
import battle.effect.InvokeInterfaces.DifferentStatEffect;
import battle.effect.InvokeInterfaces.FaintEffect;
import battle.effect.InvokeInterfaces.GroundedEffect;
import battle.effect.InvokeInterfaces.HalfWeightEffect;
import battle.effect.InvokeInterfaces.ItemBlockerEffect;
import battle.effect.InvokeInterfaces.LevitationEffect;
import battle.effect.InvokeInterfaces.MurderEffect;
import battle.effect.InvokeInterfaces.NameChanger;
import battle.effect.InvokeInterfaces.NoSwapEffect;
import battle.effect.InvokeInterfaces.OpponentItemBlockerEffect;
import battle.effect.InvokeInterfaces.OpponentTrappingEffect;
import battle.effect.InvokeInterfaces.StickyHoldEffect;
import battle.effect.InvokeInterfaces.TrappingEffect;
import battle.effect.attack.MultiTurnMove;
import battle.effect.attack.MultiTurnMove.SemiInvulnerableMove;
import battle.effect.pokemon.PokemonEffect;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.source.CastSource;
import battle.effect.status.StatusCondition;
import battle.effect.status.StatusNamesies;
import battle.effect.team.TeamEffectNamesies;
import item.ItemNamesies;
import item.hold.EVItem;
import item.hold.HoldItem;
import main.Game;
import main.Global;
import message.MessageUpdate;
import message.MessageUpdateType;
import message.Messages;
import pokemon.Stat;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import pokemon.active.Gender;
import pokemon.active.MoveList;
import pokemon.active.Nature;
import pokemon.active.PartyPokemon;
import pokemon.breeding.Eggy;
import pokemon.evolution.BaseEvolution;
import pokemon.evolution.EvolutionMethod;
import pokemon.species.PokemonInfo;
import pokemon.species.PokemonNamesies;
import sound.SoundTitle;
import trainer.Team;
import trainer.Trainer;
import trainer.TrainerType;
import trainer.WildPokemon;
import trainer.player.medal.MedalTheme;
import type.PokeType;
import type.Type;
import util.Action;
import util.GeneralUtils;
import util.serialization.Serializable;
import util.string.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ActivePokemon extends PartyPokemon {
    private static final long serialVersionUID = 1L;

    private PokemonEffectList effects;
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
    public ActivePokemon(PokemonNamesies pokemonNamesies, int level, TrainerType trainerType) {
        super(pokemonNamesies, level, trainerType);
    }

    // Constructor for matchers
    public ActivePokemon(PokemonNamesies pokemonNamesies, int level, TrainerType trainerType,
                         String nickname, Boolean shiny, List<Move> moves, Gender gender, Nature nature) {
        super(pokemonNamesies, level, trainerType, nickname, shiny, moves, gender, nature);
    }

    // Constructor for eggys
    public ActivePokemon(Eggy eggy) {
        super(eggy);
    }

    public Ability getAbility() {
        // Check if the Pokemon has had its ability changed during the battle
        PokemonEffect effect = this.getEffect(PokemonEffectNamesies.CHANGE_ABILITY);
        if (effect != null) {
            return ((AbilityHolder)effect).getAbility();
        }

        return this.getActualAbility();
    }

    public int getStage(Stat stat) {
        return this.getStages().getStage(stat);
    }

    // Returns the straight up stat value
    // This does NOT include any battle modifiers like stages or anything like that
    public int getStat(Battle b, Stat stat) {
        Integer statValue = DifferentStatEffect.getStat(b, this, stat);
        if (statValue != null) {
            return statValue;
        }

        return this.getStat(stat);
    }

    public MoveList getMoves(Battle b) {
        MoveList actualMoves = this.getActualMoves();
        if (b == null) {
            return actualMoves;
        }

        MoveList changedMoveList = ChangeMoveListEffect.getMoveList(b, this, actualMoves);
        if (changedMoveList != null) {
            return changedMoveList;
        }

        return actualMoves;
    }

    public void gainEXP(Battle b, int gain, PartyPokemon dead) {
        boolean front = b.isFront(this);

        // Add EXP
        super.gainEXP(gain);
        Messages.add(getActualName() + " gained " + gain + " EXP points!");
        if (front) {
            Messages.add(new MessageUpdate().withExpGain(b, this, Math.min(1, expRatio()), false));
        }

        // Add EVs
        HoldItem item = this.getHeldItem(b);
        int[] vals = dead.getPokemonInfo().getGivenEVs();
        if (item instanceof EVItem) {
            vals = ((EVItem)item).getEVs(vals);
        }
        this.stats().addEVs(vals);

        // Level up if applicable
        while (this.getTotalEXP() >= this.getPokemonInfo().getGrowthRate().getEXP(this.getLevel() + 1)) {
            levelUp(b);
        }
    }

    public boolean levelUp(Battle b) {
        if (this.getLevel() == MAX_LEVEL || !this.canFight()) {
            return false;
        }

        boolean inBattle = b != null;
        boolean front = inBattle && b.isFront(this);

        // Grow to the next level
        int[] gain = super.levelUp();
        Messages.add(new MessageUpdate(this.getActualName() + " grew to level " + this.getLevel() + "!").withSoundEffect(SoundTitle.LEVEL_UP));

        if (front) {
            Messages.add(new MessageUpdate().withExpGain(b, this, Math.min(1, expRatio()), true));
            Messages.add(new MessageUpdate().updatePokemon(b, this));
        }

        Messages.add(new MessageUpdate().withStatGains(gain, this.stats().getClonedStats()));

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
        MoveList actualMoves = this.getActualMoves();

        // Don't want to learn a move you already know!
        if (actualMoves.hasMove(attackName)) {
            return;
        }

        Move move = new Move(attackName);
        if (actualMoves.size() < MoveList.MAX_MOVES) {
            addMove(move, actualMoves.size() - 1, inBattle);
        } else {
            // Need a non-empty message so that it doesn't get absorbed
            Messages.add(new MessageUpdate(" ").withLearnMove(this, move));
        }
    }

    public void addMove(Move m, int index, boolean inBattle) {
        Messages.add(getActualName() + " learned " + m.getAttack().getName() + "!");
        this.getActualMoves().add(m, index);

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
                && !StickyHoldEffect.containsStickyHoldEffect(b, this, swapster);
    }

    public boolean canSwapOpponent(Battle b, ActivePokemon victim) {
        // Can't swap on the first turn
        if (b.isFirstAttack() || NoSwapEffect.containsNoSwapEffect(b, this, victim)) {
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

        return this.hasEffect(PokemonEffectNamesies.BREAKS_THE_MOLD);
    }

    // Returns true if the move currently using makes physical contact
    public boolean isMakingContact() {
        return this.getAttack().isMoveType(MoveType.PHYSICAL_CONTACT) && !this.hasAbility(AbilityNamesies.LONG_REACH);
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

    public boolean isAttackType(Type... types) {
        for (Type t : types) {
            if (this.getAttackType() == t) {
                return true;
            }
        }
        return false;
    }

    public Type getAttackType() {
        return getMove().getType();
    }

    public boolean hasMove(Battle b, AttackNamesies name) {
        return this.getMoves(b).hasMove(name);
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

    // Returns true if the Pokemon is semi-invulnerable and not above ground (digging, diving, phantom forcing, etc.)
    // Returns false if semi-invulnerable flying (or something similar) or if not semi-invulnerable at all
    public boolean isSemiInvulnerableNotOverground() {
        if (!isSemiInvulnerable()) {
            return false;
        }

        final Attack attack = this.getAttack();
        if (attack instanceof SemiInvulnerableMove) {
            SemiInvulnerableMove semiInvulnerableMove = (SemiInvulnerableMove)attack;
            return !semiInvulnerableMove.isOverground();
        }

        Global.error("isSemiInvulnerable() is true, but attack (" + attack.getName() + ") is not a SemiInvulnerableMove.");
        return false;
    }

    public PokeType getDisplayType(Battle b) {
        return getType(b, true);
    }

    public PokeType getType(Battle b) {
        return getType(b, false);
    }

    private PokeType getType(Battle b, boolean displayOnly) {
        PokeType changeType = ChangeTypeEffect.getChangedType(b, this, displayOnly);
        if (changeType != null) {
            return changeType;
        }

        return getActualType();
    }

    public boolean isType(Battle b, Type type) {
        return this.getType(b).isType(type);
    }

    public boolean isPokemon(PokemonNamesies... names) {
        PokemonEffect transformed = this.getEffect(PokemonEffectNamesies.TRANSFORMED);
        if (transformed != null) {
            return GeneralUtils.contains(((PokemonHolder)transformed).getPokemon(), names);
        }

        return this.isActualPokemon(names);
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
        this.getActualMoves().resetAttacks();

        effects = new PokemonEffectList();
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

    // Removes status and adds the remove message
    // For no message -- just use removeStatus()
    public void removeStatus(Battle b, CastSource source) {
        StatusCondition status = this.getStatus();
        this.removeStatus();

        Messages.add(new MessageUpdate(status.getRemoveMessage(b, this, source)).updatePokemon(b, this));
    }

    // Returns whether or not the Pokemon is afflicted with a status condition (does not include fainted)
    public boolean hasStatus() {
        return !this.hasStatus(StatusNamesies.NO_STATUS) && !this.isActuallyDead();
    }

    public boolean isActuallyDead() {
        return this.hasStatus(StatusNamesies.FAINTED);
    }

    public boolean isFainted(Battle b) {
        return isFainted(b, false);
    }

    private boolean isFainted(Battle b, boolean directDamage) {
        // We have already checked that this Pokemon is fainted -- don't print/apply effects more than once
        if (isActuallyDead()) {
            if (this.getHP() != 0) {
                Global.error("Pokemon should only have the Fainted Status Condition when HP is zero.");
            }

            return true;
        }

        // Deady
        if (this.getHP() == 0) {
            Messages.add(new MessageUpdate().updatePokemon(b, this));

            ActivePokemon murderer = b.getOtherPokemon(this);
            StatusNamesies.FAINTED.getStatus().apply(b, murderer, this, CastSource.EFFECT);

            // If the pokemon fainted via murder (by direct result of an attack) -- apply kill and death wishes
            if (murderer.isAttacking() && directDamage) {
                FaintEffect.grantDeathWish(b, this, murderer);
                MurderEffect.killKillKillMurderMurderMurder(b, this, murderer);
            }

            Effect.cast(TeamEffectNamesies.DEAD_ALLY, b, this, this, CastSource.EFFECT, false);

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
        list.addAll(this.getEffects().asList());
        list.add(this.getStatus());
        list.add(this.getAbility());

        if (includeItem) {
            list.add(this.getHeldItem(b));
        }

        return list;
    }

    // Checks effects (namely Heal Block) by default
    public boolean canHeal() {
        return this.canHeal(true);
    }

    public boolean canHeal(boolean checkEffects) {
        if (this.isActuallyDead() || this.fullHealth()) {
            // Fainted Pokemon and Pokemon already at full health cannot heal
            return false;
        } else if (checkEffects && this.hasEffect(PokemonEffectNamesies.HEAL_BLOCK)) {
            // Cannot heal while heal blocked
            return false;
        } else {
            // Otherwise good to go
            return true;
        }
    }

    private int heal(boolean checkEffects, Battle b, String message, Supplier<Integer> healAction) {
        if (!this.canHeal(checkEffects)) {
            return 0;
        }

        Messages.add(message);

        int healAmount = healAction.get();
        if (healAmount <= 0) {
            Global.error("Heal amount should be positive.");
        }

        Messages.add(new MessageUpdate().updatePokemon(b, this));
        return healAmount;
    }

    public int heal(int amount, boolean checkEffects, Battle b, String message) {
        return this.heal(checkEffects, b, message, () -> super.heal(amount));
    }

    public int heal(int amount, Battle b, String message) {
        return this.heal(amount, true, b, message);
    }

    public int healHealthFraction(double fraction, Battle b, String message) {
        return this.heal(true, b, message, () -> super.healHealthFraction(fraction));
    }

    // Don't think you'll make it out alive
    public void killKillKillMurderMurderMurder(Battle b, String murderMessage) {
        this.forceReduceHealthFraction(b, 1, murderMessage);
    }

    // Reduces the amount of health that corresponds to fraction of the pokemon's max health
    // Returns the total amount of health reduced
    // Will always reduce damage regardless of effects like Magic Guard
    public int forceReduceHealthFraction(Battle b, double fraction, String message) {
        return this.reduceHealthFraction(b, fraction, true, message);
    }

    // Reduces the amount of health that corresponds to fraction of the pokemon's max health
    // Returns the total amount of health reduced
    // Will consider effects like Magic Guard and potentially not reduce damage
    public int reduceHealthFraction(Battle b, double fraction, String message) {
        return this.reduceHealthFraction(b, fraction, false, message);
    }

    // Fraction reduction is always indirect damage
    private int reduceHealthFraction(Battle b, double fraction, boolean forced, String message) {
        return indirectReduceHealth(b, (int)Math.max(this.getMaxHP()*fraction, 1), forced, message);
    }

    // General rule of thumb: forced is true if it should still reduce damage for Magical Guardians
    public int indirectReduceHealth(Battle b, int amount, boolean forced, String message) {
        return this.reduceHealth(b, amount, forced ? DamageType.FORCED_INDIRECT : DamageType.CASUAL_INDIRECT, message);
    }

    // No message for direct damage
    public int reduceHealth(Battle b, int amount) {
        return this.reduceHealth(b, amount, DamageType.DIRECT, "");
    }

    // Reduces hp by amount, returns the actual amount of hp that was reduced
    // Only checks effects like bracing and absorb damage is damageType is DIRECT
    // Only prints the message if damage is actually reduced (or absorbed)
    private int reduceHealth(Battle b, int amount, DamageType damageType, String message) {

        // Not actually reducing health...
        if (amount == 0) {
            return 0;
        }

        // Magic Guard make the user immune to indirect damage
        if (damageType == DamageType.CASUAL_INDIRECT && this.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
            return 0;
        }

        boolean directDamage = damageType == DamageType.DIRECT;
        Messages.add(message);

        // Check if the damage will be absorbed by an effect
        if (directDamage && AbsorbDamageEffect.checkAbsorbDamageEffect(b, this, amount)) {
            return 0;
        }

        boolean fullHealth = fullHealth();

        // Reduce HP and record damage
        int prev = this.getHP();
        this.setHP(prev - amount);
        int taken = prev - this.getHP();
        this.takeDamage(taken);

        // Enduring the hit
        if (this.getHP() == 0 && directDamage) {
            BracingEffect brace = BracingEffect.getBracingEffect(b, this, fullHealth);
            if (brace != null) {
                taken -= heal(1);

                Messages.add(new MessageUpdate().updatePokemon(b, this));
                Messages.add(brace.braceMessage(this));
            }
        }

        Messages.add(new MessageUpdate().updatePokemon(b, this));

        if (!isFainted(b, directDamage)) {
            DamageTakenEffect.invokeDamageTakenEffect(b, this);
        }

        return taken;
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
        return isLevitating(b, null, true);
    }

    // Returns true if the Pokemon is currently levitating for any reason
    // Will only return true for being a flying type is includeFlyingType is explicitly set to true
    // Grounded effect take precedence over levitation effects
    // Obvs levitating if you have a levitation effect
    // Also considered to be levitating if in the semi-invulnerable stage of Fly or Bounce
    public boolean isLevitating(Battle b, ActivePokemon moldBreaker, boolean includeFlyingType) {
        // Technically this shouldn't ever be possible if the Pokemon is grounded,
        // however if that were to happen for whatever reason it would make sense for this to take precedence I think
        if (this.isSemiInvulnerableFlying()) {
            return true;
        }

        // Gravity reigns supreme
        if (isGrounded(b)) {
            return false;
        }

        // Flyahs gon' Fly
        if (includeFlyingType && this.isType(b, Type.FLYING)) {
            return true;
        }

        return LevitationEffect.containsLevitationEffect(b, this, moldBreaker);
    }

    @Override
    public void removeItem() {
        super.removeItem();
        this.effects.remove(PokemonEffectNamesies.CHANGE_ITEM);
    }

    public HoldItem getHeldItem(Battle b) {
        if (b == null) {
            return getActualHeldItem();
        }

        if (ItemBlockerEffect.containsItemBlockerEffect(b, this)) {
            return (HoldItem)ItemNamesies.NO_ITEM.getItem();
        }

        // Check if the Pokemon has had its item changed during the battle
        PokemonEffect changeItem = getEffect(PokemonEffectNamesies.CHANGE_ITEM);
        HoldItem item = changeItem == null ? getActualHeldItem() : ((ItemHolder)changeItem).getItem();

        if (OpponentItemBlockerEffect.checkOpponentItemBlockerEffect(b, b.getOtherPokemon(this), item.namesies())) {
            return (HoldItem)ItemNamesies.NO_ITEM.getItem();
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

    void endTurn() {
        // No longer the first turn anymore
        firstTurn = false;
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

    public PokemonEffectList getEffects() {
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
    public PokemonEffect getEffect(PokemonEffectNamesies effect) {
        return effects.get(effect);
    }

    public boolean hasEffect(PokemonEffectNamesies effect) {
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
            this.effects.remove(PokemonEffectNamesies.SELF_CONFUSION);
            this.resetCount();
        }

        if (this.reducePP) {
            this.getMove().reducePP(opp.hasAbility(AbilityNamesies.PRESSURE) ? 2 : 1);
        }

        this.setAttacking(false);
    }

    public static class PokemonEffectList extends EffectList<PokemonEffectNamesies, PokemonEffect> {
        private static final long serialVersionUID = 1L;
    }

    private enum DamageType {
        // Damage caused by a straight-up attack (like Tackle)
        DIRECT,

        // Indirect damage that MUST be taken (like the user fainting from Explosion)
        FORCED_INDIRECT,

        // Indirect damage that doesn't necessarily need to be taken (like poison damage and Magic Guard)
        CASUAL_INDIRECT
    }
}
