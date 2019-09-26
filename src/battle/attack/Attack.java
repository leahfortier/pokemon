package battle.attack;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.ApplyResult;
import battle.effect.Effect;
import battle.effect.EffectInterfaces.DoubleMinimizerMove;
import battle.effect.EffectInterfaces.ItemHolder;
import battle.effect.EffectInterfaces.ItemSwapperEffect;
import battle.effect.EffectInterfaces.PassableEffect;
import battle.effect.EffectInterfaces.PowderMove;
import battle.effect.EffectInterfaces.PowerStatusBoosterEffect;
import battle.effect.EffectInterfaces.SapHealthEffect;
import battle.effect.EffectInterfaces.SwapOpponentEffect;
import battle.effect.EffectNamesies;
import battle.effect.InvokeInterfaces.AdvantageMultiplierMove;
import battle.effect.InvokeInterfaces.AlwaysCritEffect;
import battle.effect.InvokeInterfaces.AttackBlocker;
import battle.effect.InvokeInterfaces.BarrierEffect;
import battle.effect.InvokeInterfaces.BasicAccuracyBypassEffect;
import battle.effect.InvokeInterfaces.ChangeAttackTypeEffect;
import battle.effect.InvokeInterfaces.CrashDamageMove;
import battle.effect.InvokeInterfaces.CritBlockerEffect;
import battle.effect.InvokeInterfaces.CritStageEffect;
import battle.effect.InvokeInterfaces.DefogRelease;
import battle.effect.InvokeInterfaces.EffectChanceMultiplierEffect;
import battle.effect.InvokeInterfaces.MurderEffect;
import battle.effect.InvokeInterfaces.OpponentEndAttackEffect;
import battle.effect.InvokeInterfaces.OpponentIgnoreStageEffect;
import battle.effect.InvokeInterfaces.OpponentStatSwitchingEffect;
import battle.effect.InvokeInterfaces.PowerChangeEffect;
import battle.effect.InvokeInterfaces.RapidSpinRelease;
import battle.effect.InvokeInterfaces.SelfAttackBlocker;
import battle.effect.InvokeInterfaces.SemiInvulnerableBypasser;
import battle.effect.InvokeInterfaces.SleepyFightsterEffect;
import battle.effect.InvokeInterfaces.StickyHoldEffect;
import battle.effect.InvokeInterfaces.TargetSwapperEffect;
import battle.effect.attack.FixedDamageMove;
import battle.effect.attack.MultiStrikeMove;
import battle.effect.attack.MultiTurnMove.ChargingMove;
import battle.effect.attack.MultiTurnMove.RechargingMove;
import battle.effect.attack.MultiTurnMove.SemiInvulnerableMove;
import battle.effect.attack.OhkoMove;
import battle.effect.attack.PowerCountMove;
import battle.effect.attack.RecoilMove;
import battle.effect.attack.RecoilMove.RecoilPercentageMove;
import battle.effect.attack.SapHealthMove;
import battle.effect.attack.SelfHealingMove;
import battle.effect.battle.StandardBattleEffectNamesies;
import battle.effect.battle.terrain.TerrainNamesies;
import battle.effect.battle.weather.WeatherNamesies;
import battle.effect.pokemon.PokemonEffect;
import battle.effect.pokemon.PokemonEffectNamesies;
import battle.effect.source.CastSource;
import battle.effect.source.ChangeAbilitySource;
import battle.effect.source.ChangeAttackTypeSource;
import battle.effect.source.ChangeTypeSource;
import battle.effect.status.StatusCondition;
import battle.effect.status.StatusNamesies;
import battle.effect.team.TeamEffectNamesies;
import item.ItemNamesies;
import item.berry.Berry;
import item.hold.HoldItem;
import item.hold.SpecialTypeItem.DriveItem;
import item.hold.SpecialTypeItem.GemItem;
import item.hold.SpecialTypeItem.MemoryItem;
import item.hold.SpecialTypeItem.PlateItem;
import main.Global;
import map.overworld.TerrainType;
import message.MessageUpdate;
import message.MessageUpdateType;
import message.Messages;
import pokemon.Stat;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import pokemon.active.Gender;
import pokemon.active.MoveList;
import pokemon.species.PokemonNamesies;
import trainer.Team;
import trainer.Trainer;
import type.PokeType;
import type.Type;
import type.TypeAdvantage;
import util.GeneralUtils;
import util.RandomUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class Attack implements AttackInterface {
    private static final long serialVersionUID = 1L;

    private AttackNamesies namesies;
    private String description;
    private int power;
    private int accuracy;
    private int pp;
    private Type type;
    private MoveCategory category;
    private EffectNamesies effect;
    private int effectChance;
    private StatusNamesies status;
    private List<MoveType> moveTypes;
    private boolean selfTarget;
    private int priority;
    private int[] statChanges;
    private boolean printCast;

    public Attack(AttackNamesies namesies, Type type, MoveCategory category, int pp, String description) {
        this.namesies = namesies;
        this.description = description;
        this.pp = pp;
        this.type = type;
        this.category = category;
        this.effect = null;
        this.moveTypes = new ArrayList<>();
        this.power = 0;
        this.accuracy = 10000;
        this.selfTarget = false;
        this.priority = 0;
        this.status = StatusNamesies.NO_STATUS;
        this.statChanges = new int[Stat.NUM_BATTLE_STATS];
        this.effectChance = 100;
        this.printCast = true;
    }

    public EffectNamesies getEffect() {
        return this.effect;
    }

    public int[] getStatChangesCopy() {
        return this.statChanges.clone();
    }

    public StatusNamesies getStatus() {
        return this.status;
    }

    public int getActualPriority() {
        return this.priority;
    }

    public int getPriority(Battle b, ActivePokemon me) {
        return this.getActualPriority();
    }

    @Override
    public boolean isSelfTarget() {
        return this.selfTarget;
    }

    // Returns true if an attack has secondary effects -- this only applies to physical and special moves
    // Secondary effects include status conditions, confusing, flinching, and stat changes (unless the stat changes are negative for the user)
    public boolean hasSecondaryEffects() {

        // Effects are primary for status moves
        if (this.isStatusMove()) {
            return false;
        }

        // If the effect may not necessarily occur, then it is secondary
        if (effectChance < 100) {
            return true;
        }

        // Giving the target a status condition is a secondary effect
        if (status != StatusNamesies.NO_STATUS) {
            return true;
        }

        // Confusion and flinching count as secondary effects -- but I don't think anything else does?
        if (effect == PokemonEffectNamesies.CONFUSION || effect == PokemonEffectNamesies.FLINCH) {
            return true;
        }

        // Stat changes are considered to be secondary effects unless they are negative for the user
        for (int val : this.statChanges) {
            if (val > 0 || (val < 0 && !this.isSelfTarget())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int getBaseAccuracy() {
        return this.accuracy;
    }

    public String getAccuracyString() {
        if (this.accuracy > 100) {
            return "--";
        }

        return this.accuracy + "";
    }

    public int getEffectChance() {
        return this.effectChance;
    }

    @Override
    public MoveCategory getCategory() {
        return this.category;
    }

    public int getPP() {
        return this.pp;
    }

    @Override
    public AttackNamesies namesies() {
        return this.namesies;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isMoveType(MoveType moveType) {
        return this.moveTypes.contains(moveType);
    }

    public Type getActualType() {
        return this.type;
    }

    public Type getBattleType(Battle b, ActivePokemon user) {
        Type type = this.getType(b, user);

        // Check if there is an effect that changes the type of the user
        Type changeType = ChangeAttackTypeEffect.getAttackType(b, user, this, type);
        if (changeType != null) {
            return changeType;
        }

        return type;
    }

    protected Type getType(Battle b, ActivePokemon user) {
        return this.type;
    }

    public int getPower(Battle b, ActivePokemon me, ActivePokemon o) {
        return this.power;
    }

    public String getPowerString() {
        return this.power == 0 ? "--" : this.power + "";
    }

    // To be overridden as necessary
    protected void afterApplyCheck(Battle b, ActivePokemon attacking, ActivePokemon defending) {}
    protected void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {}

    public final boolean apply(ActivePokemon me, ActivePokemon o, Battle b) {
        ActivePokemon target = getTarget(b, me, o);

        // Don't do anything for moves that are uneffective
        if (!effective(b, me, this.isStatusMove() ? target : o)) {
            return false;
        }

        if (!applies(b, me, o)) {
            Messages.add(Effect.DEFAULT_FAIL_MESSAGE);
            return false;
        }

        this.afterApplyCheck(b, me, target);

        // Physical and special attacks -- apply dat damage
        if (shouldApplyDamage(b, me)) {
            applyDamage(me, o, b);
        }

        // If you got it, flaunt it
        if (canApplyEffects(b, me, target)) {
            applyBasicEffects(b, me, target);
        }

        this.applyUniqueEffects(b, me, target);

        return true;
    }

    private ActivePokemon getTarget(Battle b, ActivePokemon user, ActivePokemon opponent) {
        if (TargetSwapperEffect.checkSwapTarget(b, user, opponent)) {
            return selfTarget ? opponent : user;
        }

        return selfTarget ? user : opponent;
    }

    private boolean canApplyEffects(Battle b, ActivePokemon me, ActivePokemon target) {
        int chance = (int)(effectChance*EffectChanceMultiplierEffect.getModifier(b, me));
        if (!RandomUtils.chanceTest(chance)) {
            return false;
        }

        // Check the opponents effects and see if it will prevent effects from occurring
        if (target.hasAbility(AbilityNamesies.SHIELD_DUST) && this.hasSecondaryEffects()) {
            return false;
        }

        return this.shouldApplyEffects(b, me, target);
    }

    private boolean zeroAdvantage(Battle b, ActivePokemon p, ActivePokemon opp) {
        if (TypeAdvantage.doesNotEffect(p, opp, b)) {
            Messages.add(TypeAdvantage.getDoesNotEffectMessage(opp));
            CrashDamageMove.invokeCrashDamageMove(b, p);

            return true;
        }

        return false;
    }

    // Takes type advantage, victim ability, and victim type into account to determine if the attack is effective
    public boolean effective(Battle b, ActivePokemon me, ActivePokemon o) {
        SelfAttackBlocker selfAttackBlocker = SelfAttackBlocker.checkBlocked(b, me);
        if (selfAttackBlocker != null) {
            Messages.add(selfAttackBlocker.getBlockMessage(b, me));
            selfAttackBlocker.alternateEffect(b, me);
            return false;
        }

        // Self-target moves and field moves don't need to take type advantage always work
        if (this.isSelfTargetStatusMove() || this.isMoveType(MoveType.FIELD)) {
            return true;
        }

        // Non-status moves (AND FUCKING THUNDER WAVE) -- need to check the type chart
        if ((!isStatusMove() || this.namesies() == AttackNamesies.THUNDER_WAVE) && this.zeroAdvantage(b, me, o)) {
            return false;
        }

        AttackBlocker attackBlocker = AttackBlocker.checkBlocked(b, me, o);
        if (attackBlocker != null) {
            Messages.add(attackBlocker.getBlockMessage(b, me, o));
            attackBlocker.alternateEffect(b, me, o);
            return false;
        }

        // You passed!!
        return true;
    }

    private void applyUniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
        // Kill yourself!!
        if (isMoveType(MoveType.USER_FAINTS)) {
            user.killKillKillMurderMurderMurder(b, "");
        }

        this.uniqueEffects(b, user, victim);

        OpponentEndAttackEffect.invokeOpponentEndAttackEffect(b, user);
    }

    private void applyBasicEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
        // Don't apply effects to a fainted Pokemon
        if (victim.isFainted(b)) {
            return;
        }

        // Give Status Condition
        if (status != StatusNamesies.NO_STATUS) {
            StatusCondition statusCondition = status.getStatus();
            ApplyResult result = statusCondition.apply(b, user, victim, CastSource.ATTACK);
            if (result.isFailure() && this.canPrintFail()) {
                Messages.add(result.getMessage());
            }
        }

        // Give Stat Changes
        victim.getStages().modifyStages(b, user, statChanges, CastSource.ATTACK);

        // Give additional effects
        if (effect != null) {
            ApplyResult result = Effect.apply(effect, b, user, victim, CastSource.ATTACK, this.canPrintCast());
            if (result.isFailure() && this.canPrintFail()) {
                Messages.add(result.getMessage());
            }
        }
    }

    public boolean canPrintFail() {
        return this.effectChance == 100 && this.isStatusMove();
    }

    private boolean canPrintCast() {
        return this.printCast;
    }

    // Returns true if this move can be Snatched
    public final boolean isSnatchable() {
        return this.isSelfTargetStatusMove() && !this.isMoveType(MoveType.NON_SNATCHABLE);
    }

    // Returns true if this move can be reflected by Magic Coat/Magic Bounce
    public final boolean isMagicReflectable() {
        return !this.isSelfTarget() && this.isStatusMove() && !this.isMoveType(MoveType.NO_MAGIC_COAT);
    }

    // Returns true if affected by Protect-like moves
    public final boolean isProtectAffected() {
        return !this.isSelfTargetStatusMove() && !this.isMoveType(MoveType.FIELD) && !this.isMoveType(MoveType.PROTECT_PIERCING);
    }

    // To be overridden if necessary
    public void startTurn(Battle b, ActivePokemon me) {}

    @Override
    public String toString() {
        return this.getName();
    }

    // EVERYTHING BELOW IS GENERATED ###

    /**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

    static class Tackle extends Attack {
        private static final long serialVersionUID = 1L;

        Tackle() {
            super(AttackNamesies.TACKLE, Type.NORMAL, MoveCategory.PHYSICAL, 35, "A physical attack in which the user charges and slams into the target with its whole body.");
            super.power = 40;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class LeechSeed extends Attack {
        private static final long serialVersionUID = 1L;

        LeechSeed() {
            super(AttackNamesies.LEECH_SEED, Type.GRASS, MoveCategory.STATUS, 10, "A seed is planted on the target. It steals some HP from the target every turn.");
            super.accuracy = 90;
            super.effect = PokemonEffectNamesies.LEECH_SEED;
        }
    }

    static class ThunderWave extends Attack {
        private static final long serialVersionUID = 1L;

        ThunderWave() {
            super(AttackNamesies.THUNDER_WAVE, Type.ELECTRIC, MoveCategory.STATUS, 20, "The user launches a weak jolt of electricity that paralyzes the target.");
            super.accuracy = 90;
            super.status = StatusNamesies.PARALYZED;
        }
    }

    static class PoisonPowder extends Attack implements PowderMove {
        private static final long serialVersionUID = 1L;

        PoisonPowder() {
            super(AttackNamesies.POISON_POWDER, Type.POISON, MoveCategory.STATUS, 35, "The user scatters a cloud of poisonous dust that poisons the target.");
            super.accuracy = 75;
            super.status = StatusNamesies.POISONED;
        }
    }

    static class SleepPowder extends Attack implements PowderMove {
        private static final long serialVersionUID = 1L;

        SleepPowder() {
            super(AttackNamesies.SLEEP_POWDER, Type.GRASS, MoveCategory.STATUS, 15, "The user scatters a big cloud of sleep-inducing dust around the target.");
            super.accuracy = 75;
            super.status = StatusNamesies.ASLEEP;
        }
    }

    static class Toxic extends Attack implements SemiInvulnerableBypasser {
        private static final long serialVersionUID = 1L;

        Toxic() {
            super(AttackNamesies.TOXIC, Type.POISON, MoveCategory.STATUS, 10, "A move that leaves the target badly poisoned. Its poison damage worsens every turn.");
            super.accuracy = 90;
            super.status = StatusNamesies.BADLY_POISONED;
        }

        @Override
        public boolean semiInvulnerableBypass(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Poison-type Pokemon bypass accuracy -- even if target is semi-invulnerable
            return attacking.isType(b, Type.POISON);
        }
    }

    static class Ember extends Attack {
        private static final long serialVersionUID = 1L;

        Ember() {
            super(AttackNamesies.EMBER, Type.FIRE, MoveCategory.SPECIAL, 25, "The target is attacked with small flames. This may also leave the target with a burn.");
            super.power = 40;
            super.accuracy = 100;
            super.effectChance = 10;
            super.status = StatusNamesies.BURNED;
        }
    }

    static class Growl extends Attack {
        private static final long serialVersionUID = 1L;

        Growl() {
            super(AttackNamesies.GROWL, Type.NORMAL, MoveCategory.STATUS, 40, "The user growls in an endearing way, making opposing Pok\u00e9mon less wary. This lowers their Attack stat.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.SOUND_BASED);
            super.statChanges[Stat.ATTACK.index()] = -1;
        }
    }

    static class Scratch extends Attack {
        private static final long serialVersionUID = 1L;

        Scratch() {
            super(AttackNamesies.SCRATCH, Type.NORMAL, MoveCategory.PHYSICAL, 35, "Hard, pointed, sharp claws rake the target to inflict damage.");
            super.power = 40;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class VineWhip extends Attack {
        private static final long serialVersionUID = 1L;

        VineWhip() {
            super(AttackNamesies.VINE_WHIP, Type.GRASS, MoveCategory.PHYSICAL, 25, "The target is struck with slender, whiplike vines to inflict damage.");
            super.power = 45;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class SonicBoom extends Attack implements FixedDamageMove {
        private static final long serialVersionUID = 1L;

        SonicBoom() {
            super(AttackNamesies.SONIC_BOOM, Type.NORMAL, MoveCategory.SPECIAL, 20, "The target is hit with a destructive shock wave that always inflicts 20 HP damage.");
            super.accuracy = 90;
        }

        @Override
        public int getFixedDamageAmount(ActivePokemon me, ActivePokemon o) {
            return 20;
        }
    }

    static class Smokescreen extends Attack {
        private static final long serialVersionUID = 1L;

        Smokescreen() {
            super(AttackNamesies.SMOKESCREEN, Type.NORMAL, MoveCategory.STATUS, 20, "The user releases an obscuring cloud of smoke or ink. This lowers the target's accuracy.");
            super.accuracy = 100;
            super.statChanges[Stat.ACCURACY.index()] = -1;
        }
    }

    static class TakeDown extends Attack implements RecoilPercentageMove {
        private static final long serialVersionUID = 1L;

        TakeDown() {
            super(AttackNamesies.TAKE_DOWN, Type.NORMAL, MoveCategory.PHYSICAL, 20, "A reckless, full-body charge attack for slamming into the target. This also damages the user a little.");
            super.power = 90;
            super.accuracy = 85;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getDamagePercentageDenominator() {
            return 4;
        }
    }

    static class Struggle extends Attack implements RecoilMove {
        private static final long serialVersionUID = 1L;

        Struggle() {
            super(AttackNamesies.STRUGGLE, Type.NO_TYPE, MoveCategory.PHYSICAL, 1, "This attack is used in desperation only if the user has no PP. It also damages the user a little.");
            super.power = 50;
            super.moveTypes.add(MoveType.ENCORELESS);
            super.moveTypes.add(MoveType.MIMICLESS);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.MIRRORLESS);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public void applyRecoil(Battle b, ActivePokemon user, int damage) {
            user.forceReduceHealthFraction(b, 1/4.0, user.getName() + " was hurt by recoil!");
        }
    }

    static class RazorLeaf extends Attack implements CritStageEffect {
        private static final long serialVersionUID = 1L;

        RazorLeaf() {
            super(AttackNamesies.RAZOR_LEAF, Type.GRASS, MoveCategory.PHYSICAL, 25, "Sharp-edged leaves are launched to slash at the opposing Pok\u00e9mon. Critical hits land more easily.");
            super.power = 55;
            super.accuracy = 95;
        }
    }

    static class SweetScent extends Attack {
        private static final long serialVersionUID = 1L;

        SweetScent() {
            super(AttackNamesies.SWEET_SCENT, Type.NORMAL, MoveCategory.STATUS, 20, "A sweet scent that harshly lowers opposing Pok\u00e9mon's evasiveness.");
            super.accuracy = 100;
            super.statChanges[Stat.EVASION.index()] = -2;
        }
    }

    static class Growth extends Attack {
        private static final long serialVersionUID = 1L;

        Growth() {
            super(AttackNamesies.GROWTH, Type.NORMAL, MoveCategory.STATUS, 20, "The user's body grows all at once, raising the Attack and Sp. Atk stats.");
            super.selfTarget = true;
        }

        @Override
        public void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            super.statChanges = new int[Stat.NUM_BATTLE_STATS];
            super.statChanges[Stat.ATTACK.index()] = 1;
            super.statChanges[Stat.SP_ATTACK.index()] = 1;

            // Doubles stat changes in the sunlight
            if (b.getWeather().namesies() == WeatherNamesies.SUNNY) {
                for (int i = 0; i < super.statChanges.length; i++) {
                    super.statChanges[i] *= 2;
                }
            }
        }
    }

    static class DoubleEdge extends Attack implements RecoilPercentageMove {
        private static final long serialVersionUID = 1L;

        DoubleEdge() {
            super(AttackNamesies.DOUBLE_EDGE, Type.NORMAL, MoveCategory.PHYSICAL, 15, "A reckless, life-risking tackle. This also damages the user quite a lot.");
            super.power = 120;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getDamagePercentageDenominator() {
            return 3;
        }
    }

    static class SeedBomb extends Attack {
        private static final long serialVersionUID = 1L;

        SeedBomb() {
            super(AttackNamesies.SEED_BOMB, Type.GRASS, MoveCategory.PHYSICAL, 15, "The user slams a barrage of hard-shelled seeds down on the target from above.");
            super.power = 80;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.BOMB_BALL);
        }
    }

    static class Synthesis extends Attack implements SelfHealingMove {
        private static final long serialVersionUID = 1L;

        Synthesis() {
            super(AttackNamesies.SYNTHESIS, Type.GRASS, MoveCategory.STATUS, 5, "The user restores its own HP. The amount of HP regained varies with the weather.");
            super.moveTypes.add(MoveType.HEALING);
            super.selfTarget = true;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            this.heal(b, victim);
        }

        @Override
        public double getHealFraction(Battle b, ActivePokemon victim) {
            switch (b.getWeather().namesies()) {
                case SUNNY:
                    return 2/3.0;
                case HAILING:
                case RAINING:
                case SANDSTORM:
                    return 1/4.0;
                default:
                    return 1/2.0;
            }
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.canHeal();
        }
    }

    static class Recover extends Attack implements SelfHealingMove {
        private static final long serialVersionUID = 1L;

        Recover() {
            super(AttackNamesies.RECOVER, Type.NORMAL, MoveCategory.STATUS, 10, "Restoring its own cells, the user restores its own HP by half of its max HP.");
            super.moveTypes.add(MoveType.HEALING);
            super.selfTarget = true;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            this.heal(b, victim);
        }

        @Override
        public double getHealFraction(Battle b, ActivePokemon victim) {
            return .5;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.canHeal();
        }
    }

    static class DragonRage extends Attack implements FixedDamageMove {
        private static final long serialVersionUID = 1L;

        DragonRage() {
            super(AttackNamesies.DRAGON_RAGE, Type.DRAGON, MoveCategory.SPECIAL, 10, "This attack hits the target with a shock wave of pure rage. This attack always inflicts 40 HP damage.");
            super.accuracy = 100;
        }

        @Override
        public int getFixedDamageAmount(ActivePokemon me, ActivePokemon o) {
            return 40;
        }
    }

    static class ScaryFace extends Attack {
        private static final long serialVersionUID = 1L;

        ScaryFace() {
            super(AttackNamesies.SCARY_FACE, Type.NORMAL, MoveCategory.STATUS, 10, "The user frightens the target with a scary face to harshly lower its Speed stat.");
            super.accuracy = 100;
            super.statChanges[Stat.SPEED.index()] = -2;
        }
    }

    static class FireFang extends Attack {
        private static final long serialVersionUID = 1L;

        FireFang() {
            super(AttackNamesies.FIRE_FANG, Type.FIRE, MoveCategory.PHYSICAL, 15, "The user bites with flame-cloaked fangs. This may also make the target flinch or leave it with a burn.");
            super.power = 65;
            super.accuracy = 95;
            super.effectChance = 20;
            super.moveTypes.add(MoveType.BITING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            super.status = StatusNamesies.NO_STATUS;
            super.effect = null;

            // If the effect is being applied, 50/50 chance to give a status condition vs. flinching
            if (RandomUtils.chanceTest(50)) {
                super.status = StatusNamesies.BURNED;
            } else {
                super.effect = PokemonEffectNamesies.FLINCH;
            }
        }
    }

    static class FlameBurst extends Attack {
        private static final long serialVersionUID = 1L;

        FlameBurst() {
            super(AttackNamesies.FLAME_BURST, Type.FIRE, MoveCategory.SPECIAL, 15, "The user attacks the target with a bursting flame. The bursting flame damages Pok\u00e9mon next to the target as well.");
            super.power = 70;
            super.accuracy = 100;
        }
    }

    static class Bite extends Attack {
        private static final long serialVersionUID = 1L;

        Bite() {
            super(AttackNamesies.BITE, Type.DARK, MoveCategory.PHYSICAL, 25, "The target is bitten with viciously sharp fangs. This may also make the target flinch.");
            super.power = 60;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 30;
            super.moveTypes.add(MoveType.BITING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Slash extends Attack implements CritStageEffect {
        private static final long serialVersionUID = 1L;

        Slash() {
            super(AttackNamesies.SLASH, Type.NORMAL, MoveCategory.PHYSICAL, 20, "The target is attacked with a slash of claws or blades. Critical hits land more easily.");
            super.power = 70;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class TailWhip extends Attack {
        private static final long serialVersionUID = 1L;

        TailWhip() {
            super(AttackNamesies.TAIL_WHIP, Type.NORMAL, MoveCategory.STATUS, 30, "The user wags its tail cutely, making opposing Pok\u00e9mon less wary and lowering their Defense stat.");
            super.accuracy = 100;
            super.statChanges[Stat.DEFENSE.index()] = -1;
        }
    }

    static class SolarBeam extends Attack implements ChargingMove, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        private boolean isCharging;

        SolarBeam() {
            super(AttackNamesies.SOLAR_BEAM, Type.GRASS, MoveCategory.SPECIAL, 10, "In this two-turn attack, the user gathers light, then blasts a bundled beam on the next turn.");
            super.power = 120;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            this.resetReady();
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            switch (b.getWeather().namesies()) {
                case HAILING:
                case RAINING:
                case SANDSTORM:
                    return .5;
                default:
                    return 1;
            }
        }

        @Override
        public boolean requiresCharge(Battle b) {
            // Does not need to charge during harsh sunlight
            return b.getWeather().namesies() != WeatherNamesies.SUNNY;
        }

        @Override
        public String getChargeMessage(ActivePokemon user) {
            return user.getName() + " began taking in sunlight!";
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void resetReady() {
            this.isCharging = !this.chargesFirst();
        }

        @Override
        public void switchReady() {
            this.isCharging = !this.isCharging;
        }
    }

    static class SolarBlade extends Attack implements ChargingMove, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        private boolean isCharging;

        SolarBlade() {
            super(AttackNamesies.SOLAR_BLADE, Type.GRASS, MoveCategory.PHYSICAL, 10, "In this two-turn attack, the user gathers light and fills a blade with the light's energy, attacking the target on the next turn.");
            super.power = 125;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            this.resetReady();
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            switch (b.getWeather().namesies()) {
                case HAILING:
                case RAINING:
                case SANDSTORM:
                    return .5;
                default:
                    return 1;
            }
        }

        @Override
        public boolean requiresCharge(Battle b) {
            // Does not need to charge during harsh sunlight
            return b.getWeather().namesies() != WeatherNamesies.SUNNY;
        }

        @Override
        public String getChargeMessage(ActivePokemon user) {
            return user.getName() + " began taking in sunlight!";
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void resetReady() {
            this.isCharging = !this.chargesFirst();
        }

        @Override
        public void switchReady() {
            this.isCharging = !this.isCharging;
        }
    }

    static class Flamethrower extends Attack {
        private static final long serialVersionUID = 1L;

        Flamethrower() {
            super(AttackNamesies.FLAMETHROWER, Type.FIRE, MoveCategory.SPECIAL, 15, "The target is scorched with an intense blast of fire. This may also leave the target with a burn.");
            super.power = 90;
            super.accuracy = 100;
            super.effectChance = 10;
            super.status = StatusNamesies.BURNED;
        }
    }

    static class Fly extends Attack implements SemiInvulnerableMove {
        private static final long serialVersionUID = 1L;

        private boolean isCharging;

        Fly() {
            super(AttackNamesies.FLY, Type.FLYING, MoveCategory.PHYSICAL, 15, "The user soars and then strikes its target on the next turn.");
            super.power = 90;
            super.accuracy = 95;
            super.moveTypes.add(MoveType.AIRBORNE);
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            this.resetReady();
        }

        @Override
        public boolean isOverground() {
            return true;
        }

        @Override
        public String getChargeMessage(ActivePokemon user) {
            return user.getName() + " flew up high!";
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void resetReady() {
            this.isCharging = !this.chargesFirst();
        }

        @Override
        public void switchReady() {
            this.isCharging = !this.isCharging;
        }
    }

    static class FireSpin extends Attack {
        private static final long serialVersionUID = 1L;

        FireSpin() {
            super(AttackNamesies.FIRE_SPIN, Type.FIRE, MoveCategory.SPECIAL, 15, "The target becomes trapped within a fierce vortex of fire that rages for four to five turns.");
            super.power = 35;
            super.accuracy = 85;
            super.effect = PokemonEffectNamesies.FIRE_SPIN;
        }
    }

    static class Inferno extends Attack {
        private static final long serialVersionUID = 1L;

        Inferno() {
            super(AttackNamesies.INFERNO, Type.FIRE, MoveCategory.SPECIAL, 5, "The user attacks by engulfing the target in an intense fire. This leaves the target with a burn.");
            super.power = 100;
            super.accuracy = 50;
            super.status = StatusNamesies.BURNED;
        }
    }

    static class DragonClaw extends Attack {
        private static final long serialVersionUID = 1L;

        DragonClaw() {
            super(AttackNamesies.DRAGON_CLAW, Type.DRAGON, MoveCategory.PHYSICAL, 15, "The user slashes the target with huge sharp claws.");
            super.power = 80;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class ShadowClaw extends Attack implements CritStageEffect {
        private static final long serialVersionUID = 1L;

        ShadowClaw() {
            super(AttackNamesies.SHADOW_CLAW, Type.GHOST, MoveCategory.PHYSICAL, 15, "The user slashes with a sharp claw made from shadows. Critical hits land more easily.");
            super.power = 70;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class AirSlash extends Attack {
        private static final long serialVersionUID = 1L;

        AirSlash() {
            super(AttackNamesies.AIR_SLASH, Type.FLYING, MoveCategory.SPECIAL, 15, "The user attacks with a blade of air that slices even the sky. This may also make the target flinch.");
            super.power = 75;
            super.accuracy = 95;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 30;
        }
    }

    static class WingAttack extends Attack {
        private static final long serialVersionUID = 1L;

        WingAttack() {
            super(AttackNamesies.WING_ATTACK, Type.FLYING, MoveCategory.PHYSICAL, 35, "The target is struck with large, imposing wings spread wide to inflict damage.");
            super.power = 60;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class HeatWave extends Attack {
        private static final long serialVersionUID = 1L;

        HeatWave() {
            super(AttackNamesies.HEAT_WAVE, Type.FIRE, MoveCategory.SPECIAL, 10, "The user attacks by exhaling hot breath on the opposing Pok\u00e9mon. This may also leave those Pok\u00e9mon with a burn.");
            super.power = 95;
            super.accuracy = 90;
            super.effectChance = 10;
            super.status = StatusNamesies.BURNED;
        }
    }

    static class FlareBlitz extends Attack implements RecoilPercentageMove {
        private static final long serialVersionUID = 1L;

        FlareBlitz() {
            super(AttackNamesies.FLARE_BLITZ, Type.FIRE, MoveCategory.PHYSICAL, 15, "The user cloaks itself in fire and charges the target. This also damages the user quite a lot. This attack may leave the target with a burn.");
            super.power = 120;
            super.accuracy = 100;
            super.effectChance = 10;
            super.status = StatusNamesies.BURNED;
            super.moveTypes.add(MoveType.DEFROST);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getDamagePercentageDenominator() {
            return 3;
        }
    }

    static class FlashCannon extends Attack {
        private static final long serialVersionUID = 1L;

        FlashCannon() {
            super(AttackNamesies.FLASH_CANNON, Type.STEEL, MoveCategory.SPECIAL, 10, "The user gathers all its light energy and releases it all at once. This may also lower the target's Sp. Def stat.");
            super.power = 80;
            super.accuracy = 100;
            super.effectChance = 10;
            super.statChanges[Stat.SP_DEFENSE.index()] = -1;
        }
    }

    static class Bubble extends Attack {
        private static final long serialVersionUID = 1L;

        Bubble() {
            super(AttackNamesies.BUBBLE, Type.WATER, MoveCategory.SPECIAL, 30, "A spray of countless bubbles is jetted at the opposing Pok\u00e9mon. This may also lower their Speed stat.");
            super.power = 40;
            super.accuracy = 100;
            super.effectChance = 10;
            super.statChanges[Stat.SPEED.index()] = -1;
        }
    }

    static class Withdraw extends Attack {
        private static final long serialVersionUID = 1L;

        Withdraw() {
            super(AttackNamesies.WITHDRAW, Type.WATER, MoveCategory.STATUS, 40, "The user withdraws its body into its hard shell, raising its Defense stat.");
            super.selfTarget = true;
            super.statChanges[Stat.DEFENSE.index()] = 1;
        }
    }

    static class WaterGun extends Attack {
        private static final long serialVersionUID = 1L;

        WaterGun() {
            super(AttackNamesies.WATER_GUN, Type.WATER, MoveCategory.SPECIAL, 25, "The target is blasted with a forceful shot of water.");
            super.power = 40;
            super.accuracy = 100;
        }
    }

    static class RapidSpin extends Attack {
        private static final long serialVersionUID = 1L;

        RapidSpin() {
            super(AttackNamesies.RAPID_SPIN, Type.NORMAL, MoveCategory.PHYSICAL, 40, "A spin attack that can also eliminate such moves as Bind, Wrap, Leech Seed, and Spikes.");
            super.power = 20;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.selfTarget = true;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            RapidSpinRelease.release(b, victim);
        }
    }

    static class Reflect extends Attack {
        private static final long serialVersionUID = 1L;

        Reflect() {
            super(AttackNamesies.REFLECT, Type.PSYCHIC, MoveCategory.STATUS, 20, "A wondrous wall of light is put up to reduce damage from physical attacks for five turns.");
            super.effect = TeamEffectNamesies.REFLECT;
            super.selfTarget = true;
        }
    }

    static class AuroraVeil extends Attack {
        private static final long serialVersionUID = 1L;

        AuroraVeil() {
            super(AttackNamesies.AURORA_VEIL, Type.ICE, MoveCategory.STATUS, 20, "This move reduces damage from physical and special moves for five turns. This can be used only in a hailstorm.");
            super.effect = TeamEffectNamesies.AURORA_VEIL;
            super.selfTarget = true;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return b.getWeather().namesies() == WeatherNamesies.HAILING;
        }
    }

    static class SpikyShield extends Attack {
        private static final long serialVersionUID = 1L;

        SpikyShield() {
            super(AttackNamesies.SPIKY_SHIELD, Type.GRASS, MoveCategory.STATUS, 10, "In addition to protecting the user from attacks, this move also damages any attacker who makes direct contact.");
            super.effect = PokemonEffectNamesies.SPIKY_SHIELD;
            super.moveTypes.add(MoveType.SUCCESSIVE_DECAY);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.selfTarget = true;
            super.priority = 4;
        }
    }

    static class BanefulBunker extends Attack {
        private static final long serialVersionUID = 1L;

        BanefulBunker() {
            super(AttackNamesies.BANEFUL_BUNKER, Type.POISON, MoveCategory.STATUS, 10, "In addition to protecting the user from attacks, this move also poisons any attacker that makes direct contact.");
            super.effect = PokemonEffectNamesies.BANEFUL_BUNKER;
            super.moveTypes.add(MoveType.SUCCESSIVE_DECAY);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.selfTarget = true;
            super.priority = 4;
        }
    }

    static class KingsShield extends Attack {
        private static final long serialVersionUID = 1L;

        KingsShield() {
            super(AttackNamesies.KINGS_SHIELD, Type.STEEL, MoveCategory.STATUS, 10, "The user takes a defensive stance while it protects itself from damage. It also harshly lowers the Attack stat of any attacker who makes direct contact.");
            super.effect = PokemonEffectNamesies.KINGS_SHIELD;
            super.moveTypes.add(MoveType.SUCCESSIVE_DECAY);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.selfTarget = true;
            super.priority = 4;
        }
    }

    static class Protect extends Attack {
        private static final long serialVersionUID = 1L;

        Protect() {
            super(AttackNamesies.PROTECT, Type.NORMAL, MoveCategory.STATUS, 10, "Enables the user to evade all attacks. Its chance of failing rises if it is used in succession.");
            super.effect = PokemonEffectNamesies.PROTECT;
            super.moveTypes.add(MoveType.SUCCESSIVE_DECAY);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.selfTarget = true;
            super.priority = 4;
        }
    }

    static class Detect extends Attack {
        private static final long serialVersionUID = 1L;

        Detect() {
            super(AttackNamesies.DETECT, Type.FIGHTING, MoveCategory.STATUS, 5, "Enables the user to evade all attacks. Its chance of failing rises if it is used in succession.");
            super.effect = PokemonEffectNamesies.PROTECT;
            super.moveTypes.add(MoveType.SUCCESSIVE_DECAY);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.selfTarget = true;
            super.priority = 4;
        }
    }

    static class QuickGuard extends Attack {
        private static final long serialVersionUID = 1L;

        QuickGuard() {
            super(AttackNamesies.QUICK_GUARD, Type.FIGHTING, MoveCategory.STATUS, 15, "The user protects itself and its allies from priority moves.");
            super.effect = PokemonEffectNamesies.QUICK_GUARD;
            super.moveTypes.add(MoveType.SUCCESSIVE_DECAY);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.selfTarget = true;
            super.priority = 4;
        }
    }

    static class Endure extends Attack {
        private static final long serialVersionUID = 1L;

        Endure() {
            super(AttackNamesies.ENDURE, Type.NORMAL, MoveCategory.STATUS, 10, "The user endures any attack with at least 1 HP. Its chance of failing rises if it is used in succession.");
            super.effect = PokemonEffectNamesies.BRACING;
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.SUCCESSIVE_DECAY);
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.selfTarget = true;
            super.priority = 4;
        }
    }

    static class WaterPulse extends Attack {
        private static final long serialVersionUID = 1L;

        WaterPulse() {
            super(AttackNamesies.WATER_PULSE, Type.WATER, MoveCategory.SPECIAL, 20, "The user attacks the target with a pulsing blast of water. This may also confuse the target.");
            super.power = 60;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.CONFUSION;
            super.effectChance = 20;
            super.moveTypes.add(MoveType.AURA_PULSE);
        }
    }

    static class ConfusionDamage extends Attack implements CritBlockerEffect {
        private static final long serialVersionUID = 1L;

        ConfusionDamage() {
            super(AttackNamesies.CONFUSION_DAMAGE, Type.NO_TYPE, MoveCategory.PHYSICAL, 1, "None");
            super.power = 40;
        }
    }

    static class ConfuseRay extends Attack {
        private static final long serialVersionUID = 1L;

        ConfuseRay() {
            super(AttackNamesies.CONFUSE_RAY, Type.GHOST, MoveCategory.STATUS, 10, "The target is exposed to a sinister ray that triggers confusion.");
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.CONFUSION;
        }
    }

    static class AquaTail extends Attack {
        private static final long serialVersionUID = 1L;

        AquaTail() {
            super(AttackNamesies.AQUA_TAIL, Type.WATER, MoveCategory.PHYSICAL, 10, "The user attacks by swinging its tail as if it were a vicious wave in a raging storm.");
            super.power = 90;
            super.accuracy = 90;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class SkullBash extends Attack implements ChargingMove {
        private static final long serialVersionUID = 1L;

        private boolean isCharging;

        SkullBash() {
            super(AttackNamesies.SKULL_BASH, Type.NORMAL, MoveCategory.PHYSICAL, 10, "The user tucks in its head to raise its Defense stat on the first turn, then rams the target on the next turn.");
            super.power = 130;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.selfTarget = true;
            super.statChanges[Stat.DEFENSE.index()] = 1;
            this.resetReady();
        }

        @Override
        public boolean shouldApplyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            return this.isCharging();
        }

        @Override
        public String getChargeMessage(ActivePokemon user) {
            return user.getName() + " lowered its head!";
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void resetReady() {
            this.isCharging = !this.chargesFirst();
        }

        @Override
        public void switchReady() {
            this.isCharging = !this.isCharging;
        }
    }

    static class IronDefense extends Attack {
        private static final long serialVersionUID = 1L;

        IronDefense() {
            super(AttackNamesies.IRON_DEFENSE, Type.STEEL, MoveCategory.STATUS, 15, "The user hardens its body's surface like iron, sharply raising its Defense stat.");
            super.selfTarget = true;
            super.statChanges[Stat.DEFENSE.index()] = 2;
        }
    }

    static class HydroPump extends Attack {
        private static final long serialVersionUID = 1L;

        HydroPump() {
            super(AttackNamesies.HYDRO_PUMP, Type.WATER, MoveCategory.SPECIAL, 5, "The target is blasted by a huge volume of water launched under great pressure.");
            super.power = 110;
            super.accuracy = 80;
        }
    }

    static class RainDance extends Attack {
        private static final long serialVersionUID = 1L;

        RainDance() {
            super(AttackNamesies.RAIN_DANCE, Type.WATER, MoveCategory.STATUS, 5, "The user summons a heavy rain that falls for five turns, powering up Water-type moves. It lowers the power of Fire-type moves.");
            super.effect = WeatherNamesies.RAINING;
            super.moveTypes.add(MoveType.DANCE);
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.FIELD);
        }
    }

    static class SunnyDay extends Attack {
        private static final long serialVersionUID = 1L;

        SunnyDay() {
            super(AttackNamesies.SUNNY_DAY, Type.FIRE, MoveCategory.STATUS, 5, "The user intensifies the sun for five turns, powering up Fire-type moves. It lowers the power of Water-type moves.");
            super.effect = WeatherNamesies.SUNNY;
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.FIELD);
        }
    }

    static class Sandstorm extends Attack {
        private static final long serialVersionUID = 1L;

        Sandstorm() {
            super(AttackNamesies.SANDSTORM, Type.ROCK, MoveCategory.STATUS, 10, "A five-turn sandstorm is summoned to hurt all combatants except the Rock, Ground, and Steel types. It raises the Sp. Def stat of Rock types.");
            super.effect = WeatherNamesies.SANDSTORM;
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.FIELD);
        }
    }

    static class Hail extends Attack {
        private static final long serialVersionUID = 1L;

        Hail() {
            super(AttackNamesies.HAIL, Type.ICE, MoveCategory.STATUS, 10, "The user summons a hailstorm lasting five turns. It damages all Pok\u00e9mon except the Ice type.");
            super.effect = WeatherNamesies.HAILING;
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.FIELD);
        }
    }

    static class PetalDance extends Attack {
        private static final long serialVersionUID = 1L;

        PetalDance() {
            super(AttackNamesies.PETAL_DANCE, Type.GRASS, MoveCategory.SPECIAL, 10, "The user attacks the target by scattering petals for two to three turns. The user then becomes confused.");
            super.power = 120;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.SELF_CONFUSION;
            super.moveTypes.add(MoveType.DANCE);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.selfTarget = true;
        }
    }

    static class Thrash extends Attack {
        private static final long serialVersionUID = 1L;

        Thrash() {
            super(AttackNamesies.THRASH, Type.NORMAL, MoveCategory.PHYSICAL, 10, "The user rampages and attacks for two to three turns. The user then becomes confused.");
            super.power = 120;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.SELF_CONFUSION;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.selfTarget = true;
        }
    }

    static class HyperBeam extends Attack implements RechargingMove {
        private static final long serialVersionUID = 1L;

        private boolean isCharging;

        HyperBeam() {
            super(AttackNamesies.HYPER_BEAM, Type.NORMAL, MoveCategory.SPECIAL, 5, "The target is attacked with a powerful beam. The user can't move on the next turn.");
            super.power = 150;
            super.accuracy = 90;
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            this.resetReady();
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void resetReady() {
            this.isCharging = !this.chargesFirst();
        }

        @Override
        public void switchReady() {
            this.isCharging = !this.isCharging;
        }
    }

    static class FrenzyPlant extends Attack implements RechargingMove {
        private static final long serialVersionUID = 1L;

        private boolean isCharging;

        FrenzyPlant() {
            super(AttackNamesies.FRENZY_PLANT, Type.GRASS, MoveCategory.SPECIAL, 5, "The user slams the target with an enormous tree. The user can't move on the next turn.");
            super.power = 150;
            super.accuracy = 90;
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            this.resetReady();
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void resetReady() {
            this.isCharging = !this.chargesFirst();
        }

        @Override
        public void switchReady() {
            this.isCharging = !this.isCharging;
        }
    }

    static class BlastBurn extends Attack implements RechargingMove {
        private static final long serialVersionUID = 1L;

        private boolean isCharging;

        BlastBurn() {
            super(AttackNamesies.BLAST_BURN, Type.FIRE, MoveCategory.SPECIAL, 5, "The target is razed by a fiery explosion. The user can't move on the next turn.");
            super.power = 150;
            super.accuracy = 90;
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            this.resetReady();
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void resetReady() {
            this.isCharging = !this.chargesFirst();
        }

        @Override
        public void switchReady() {
            this.isCharging = !this.isCharging;
        }
    }

    static class HydroCannon extends Attack implements RechargingMove {
        private static final long serialVersionUID = 1L;

        private boolean isCharging;

        HydroCannon() {
            super(AttackNamesies.HYDRO_CANNON, Type.WATER, MoveCategory.SPECIAL, 5, "The target is hit with a watery blast. The user can't move on the next turn.");
            super.power = 150;
            super.accuracy = 90;
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            this.resetReady();
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void resetReady() {
            this.isCharging = !this.chargesFirst();
        }

        @Override
        public void switchReady() {
            this.isCharging = !this.isCharging;
        }
    }

    static class PrismaticLaser extends Attack implements RechargingMove {
        private static final long serialVersionUID = 1L;

        private boolean isCharging;

        PrismaticLaser() {
            super(AttackNamesies.PRISMATIC_LASER, Type.PSYCHIC, MoveCategory.SPECIAL, 10, "The user shoots powerful lasers using the power of a prism. The user can't move on the next turn.");
            super.power = 160;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            this.resetReady();
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void resetReady() {
            this.isCharging = !this.chargesFirst();
        }

        @Override
        public void switchReady() {
            this.isCharging = !this.isCharging;
        }
    }

    static class StringShot extends Attack {
        private static final long serialVersionUID = 1L;

        StringShot() {
            super(AttackNamesies.STRING_SHOT, Type.BUG, MoveCategory.STATUS, 40, "The opposing Pok\u00e9mon are bound with silk blown from the user's mouth that harshly lowers the Speed stat.");
            super.accuracy = 95;
            super.statChanges[Stat.SPEED.index()] = -2;
        }
    }

    static class BugBite extends Attack {
        private static final long serialVersionUID = 1L;

        BugBite() {
            super(AttackNamesies.BUG_BITE, Type.BUG, MoveCategory.PHYSICAL, 20, "The user bites the target. If the target is holding a Berry, the user eats it and gains its effect.");
            super.power = 60;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            HoldItem item = victim.getHeldItem(b);
            if (item instanceof Berry) {
                ((Berry)item).stealBerry(b, user, victim);
            }
        }
    }

    static class Harden extends Attack {
        private static final long serialVersionUID = 1L;

        Harden() {
            super(AttackNamesies.HARDEN, Type.NORMAL, MoveCategory.STATUS, 30, "The user stiffens all the muscles in its body to raise its Defense stat.");
            super.selfTarget = true;
            super.statChanges[Stat.DEFENSE.index()] = 1;
        }
    }

    static class Confusion extends Attack {
        private static final long serialVersionUID = 1L;

        Confusion() {
            super(AttackNamesies.CONFUSION, Type.PSYCHIC, MoveCategory.SPECIAL, 25, "The target is hit by a weak telekinetic force. This may also confuse the target.");
            super.power = 50;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.CONFUSION;
            super.effectChance = 10;
        }
    }

    static class StunSpore extends Attack implements PowderMove {
        private static final long serialVersionUID = 1L;

        StunSpore() {
            super(AttackNamesies.STUN_SPORE, Type.GRASS, MoveCategory.STATUS, 30, "The user scatters a cloud of numbing powder that paralyzes the target.");
            super.accuracy = 75;
            super.status = StatusNamesies.PARALYZED;
        }
    }

    // Twice as strong when the opponent is flying
    static class Gust extends Attack implements SemiInvulnerableBypasser, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Gust() {
            super(AttackNamesies.GUST, Type.FLYING, MoveCategory.SPECIAL, 35, "A gust of wind is whipped up by wings and launched at the target to inflict damage.");
            super.power = 40;
            super.accuracy = 100;
        }

        @Override
        public boolean semiInvulnerableBypass(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Always hit when the opponent is flying
            return defending.isSemiInvulnerableFlying();
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.isSemiInvulnerableFlying() ? 2 : 1;
        }
    }

    static class Supersonic extends Attack {
        private static final long serialVersionUID = 1L;

        Supersonic() {
            super(AttackNamesies.SUPERSONIC, Type.NORMAL, MoveCategory.STATUS, 20, "The user generates odd sound waves from its body that confuse the target.");
            super.accuracy = 55;
            super.effect = PokemonEffectNamesies.CONFUSION;
            super.moveTypes.add(MoveType.SOUND_BASED);
        }
    }

    static class Psybeam extends Attack {
        private static final long serialVersionUID = 1L;

        Psybeam() {
            super(AttackNamesies.PSYBEAM, Type.PSYCHIC, MoveCategory.SPECIAL, 20, "The target is attacked with a peculiar ray. This may also leave the target confused.");
            super.power = 65;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.CONFUSION;
            super.effectChance = 10;
        }
    }

    static class SilverWind extends Attack {
        private static final long serialVersionUID = 1L;

        SilverWind() {
            super(AttackNamesies.SILVER_WIND, Type.BUG, MoveCategory.SPECIAL, 5, "The target is attacked with powdery scales blown by the wind. This may also raise all the user's stats.");
            super.power = 60;
            super.accuracy = 100;
            super.effectChance = 10;
            super.selfTarget = true;
            super.statChanges[Stat.ATTACK.index()] = 1;
            super.statChanges[Stat.DEFENSE.index()] = 1;
            super.statChanges[Stat.SP_ATTACK.index()] = 1;
            super.statChanges[Stat.SP_DEFENSE.index()] = 1;
            super.statChanges[Stat.SPEED.index()] = 1;
        }
    }

    static class Tailwind extends Attack {
        private static final long serialVersionUID = 1L;

        Tailwind() {
            super(AttackNamesies.TAILWIND, Type.FLYING, MoveCategory.STATUS, 15, "The user whips up a turbulent whirlwind that ups the Speed stat of the user and its allies for four turns.");
            super.effect = TeamEffectNamesies.TAILWIND;
            super.selfTarget = true;
        }
    }

    static class MorningSun extends Attack implements SelfHealingMove {
        private static final long serialVersionUID = 1L;

        MorningSun() {
            super(AttackNamesies.MORNING_SUN, Type.NORMAL, MoveCategory.STATUS, 5, "The user restores its own HP. The amount of HP regained varies with the weather.");
            super.moveTypes.add(MoveType.HEALING);
            super.selfTarget = true;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            this.heal(b, victim);
        }

        @Override
        public double getHealFraction(Battle b, ActivePokemon victim) {
            switch (b.getWeather().namesies()) {
                case SUNNY:
                    return 2/3.0;
                case HAILING:
                case RAINING:
                case SANDSTORM:
                    return 1/4.0;
                default:
                    return 1/2.0;
            }
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.canHeal();
        }
    }

    static class Safeguard extends Attack {
        private static final long serialVersionUID = 1L;

        Safeguard() {
            super(AttackNamesies.SAFEGUARD, Type.NORMAL, MoveCategory.STATUS, 25, "The user creates a protective field that prevents status conditions for five turns.");
            super.effect = TeamEffectNamesies.SAFEGUARD;
            super.selfTarget = true;
        }
    }

    static class Captivate extends Attack {
        private static final long serialVersionUID = 1L;

        Captivate() {
            super(AttackNamesies.CAPTIVATE, Type.NORMAL, MoveCategory.STATUS, 20, "If any opposing Pok\u00e9mon is the opposite gender of the user, it is charmed, which harshly lowers its Sp. Atk stat.");
            super.accuracy = 100;
            super.statChanges[Stat.SP_ATTACK.index()] = -2;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return Gender.oppositeGenders(user, victim);
        }
    }

    static class BugBuzz extends Attack {
        private static final long serialVersionUID = 1L;

        BugBuzz() {
            super(AttackNamesies.BUG_BUZZ, Type.BUG, MoveCategory.SPECIAL, 10, "The user generates a damaging sound wave by vibration. This may also lower the target's Sp. Def stat.");
            super.power = 90;
            super.accuracy = 100;
            super.effectChance = 10;
            super.moveTypes.add(MoveType.SOUND_BASED);
            super.statChanges[Stat.SP_DEFENSE.index()] = -1;
        }
    }

    static class QuiverDance extends Attack {
        private static final long serialVersionUID = 1L;

        QuiverDance() {
            super(AttackNamesies.QUIVER_DANCE, Type.BUG, MoveCategory.STATUS, 20, "The user lightly performs a beautiful, mystic dance. This boosts the user's Sp. Atk, Sp. Def, and Speed stats.");
            super.moveTypes.add(MoveType.DANCE);
            super.selfTarget = true;
            super.statChanges[Stat.SP_ATTACK.index()] = 1;
            super.statChanges[Stat.SP_DEFENSE.index()] = 1;
            super.statChanges[Stat.SPEED.index()] = 1;
        }
    }

    static class Encore extends Attack {
        private static final long serialVersionUID = 1L;

        Encore() {
            super(AttackNamesies.ENCORE, Type.NORMAL, MoveCategory.STATUS, 5, "The user compels the target to keep using the move it encored for three turns.");
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.ENCORE;
            super.moveTypes.add(MoveType.ENCORELESS);
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
        }
    }

    static class PoisonSting extends Attack {
        private static final long serialVersionUID = 1L;

        PoisonSting() {
            super(AttackNamesies.POISON_STING, Type.POISON, MoveCategory.PHYSICAL, 35, "The user stabs the target with a poisonous stinger. This may also poison the target.");
            super.power = 15;
            super.accuracy = 100;
            super.effectChance = 30;
            super.status = StatusNamesies.POISONED;
        }
    }

    static class FuryAttack extends Attack implements MultiStrikeMove {
        private static final long serialVersionUID = 1L;

        FuryAttack() {
            super(AttackNamesies.FURY_ATTACK, Type.NORMAL, MoveCategory.PHYSICAL, 20, "The target is jabbed repeatedly with a horn or beak two to five times in a row.");
            super.power = 15;
            super.accuracy = 85;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getMinHits() {
            return 2;
        }

        @Override
        public int getMaxHits() {
            return 5;
        }
    }

    static class FalseSwipe extends Attack {
        private static final long serialVersionUID = 1L;

        FalseSwipe() {
            super(AttackNamesies.FALSE_SWIPE, Type.NORMAL, MoveCategory.PHYSICAL, 40, "A restrained attack that prevents the target from fainting. The target is left with at least 1 HP.");
            super.power = 40;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
            // Give bracing effect for damage application (should only affect direct damage, and not any Apply/TakeDamageEffects)
            // Don't use the cast method here since we don't want it to be affected by successive decay
            PokemonEffect bracing = PokemonEffectNamesies.BRACING.getEffect();
            o.getEffects().add(bracing);
            super.applyDamage(me, o, b);
            bracing.deactivate();
        }
    }

    static class Disable extends Attack {
        private static final long serialVersionUID = 1L;

        Disable() {
            super(AttackNamesies.DISABLE, Type.NORMAL, MoveCategory.STATUS, 20, "For four turns, this move prevents the target from using the move it last used.");
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.DISABLE;
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
        }
    }

    static class FocusEnergy extends Attack {
        private static final long serialVersionUID = 1L;

        FocusEnergy() {
            super(AttackNamesies.FOCUS_ENERGY, Type.NORMAL, MoveCategory.STATUS, 30, "The user takes a deep breath and focuses so that critical hits land more easily.");
            super.effect = PokemonEffectNamesies.RAISE_CRITS;
            super.selfTarget = true;
        }
    }

    static class Twineedle extends Attack implements MultiStrikeMove {
        private static final long serialVersionUID = 1L;

        Twineedle() {
            super(AttackNamesies.TWINEEDLE, Type.BUG, MoveCategory.PHYSICAL, 20, "The user damages the target twice in succession by jabbing it with two spikes. This may also poison the target.");
            super.power = 25;
            super.accuracy = 100;
            super.effectChance = 20;
            super.status = StatusNamesies.POISONED;
        }

        @Override
        public int getMinHits() {
            return 2;
        }

        @Override
        public int getMaxHits() {
            return 2;
        }
    }

    static class Rage extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Rage() {
            super(AttackNamesies.RAGE, Type.NORMAL, MoveCategory.PHYSICAL, 20, "As long as this move is in use, the power of rage raises the Attack stat each time the user is hit in battle.");
            super.power = 20;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.RAGING;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.selfTarget = true;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getCount();
        }
    }

    static class Pursuit extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Pursuit() {
            super(AttackNamesies.PURSUIT, Type.DARK, MoveCategory.PHYSICAL, 20, "The power of this attack move is doubled if it's used on a target that's switching out of battle.");
            super.power = 40;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getPriority(Battle b, ActivePokemon me) {
            return b.isSwitching(!me.isPlayer()) ? 7 : super.priority;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return b.isSwitching(victim.isPlayer()) ? 2 : 1;
        }
    }

    static class ToxicSpikes extends Attack {
        private static final long serialVersionUID = 1L;

        ToxicSpikes() {
            super(AttackNamesies.TOXIC_SPIKES, Type.POISON, MoveCategory.STATUS, 20, "The user lays a trap of poison spikes at the feet of the opposing team. The spikes will poison opposing Pok\u00e9mon that switch into battle.");
            super.effect = TeamEffectNamesies.TOXIC_SPIKES;
            super.moveTypes.add(MoveType.FIELD);
        }
    }

    static class PinMissile extends Attack implements MultiStrikeMove {
        private static final long serialVersionUID = 1L;

        PinMissile() {
            super(AttackNamesies.PIN_MISSILE, Type.BUG, MoveCategory.PHYSICAL, 20, "Sharp spikes are shot at the target in rapid succession. They hit two to five times in a row.");
            super.power = 25;
            super.accuracy = 95;
        }

        @Override
        public int getMinHits() {
            return 2;
        }

        @Override
        public int getMaxHits() {
            return 5;
        }
    }

    static class Agility extends Attack {
        private static final long serialVersionUID = 1L;

        Agility() {
            super(AttackNamesies.AGILITY, Type.PSYCHIC, MoveCategory.STATUS, 30, "The user relaxes and lightens its body to move faster. This sharply raises the Speed stat.");
            super.selfTarget = true;
            super.statChanges[Stat.SPEED.index()] = 2;
        }
    }

    static class Assurance extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Assurance() {
            super(AttackNamesies.ASSURANCE, Type.DARK, MoveCategory.PHYSICAL, 10, "If the target has already taken some damage in the same turn, this attack's power is doubled.");
            super.power = 60;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.hasTakenDamage() ? 2 : 1;
        }
    }

    static class PoisonJab extends Attack {
        private static final long serialVersionUID = 1L;

        PoisonJab() {
            super(AttackNamesies.POISON_JAB, Type.POISON, MoveCategory.PHYSICAL, 20, "The target is stabbed with a tentacle or arm steeped in poison. This may also poison the target.");
            super.power = 80;
            super.accuracy = 100;
            super.effectChance = 30;
            super.status = StatusNamesies.POISONED;
            super.moveTypes.add(MoveType.PUNCHING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Endeavor extends Attack implements FixedDamageMove {
        private static final long serialVersionUID = 1L;

        Endeavor() {
            super(AttackNamesies.ENDEAVOR, Type.NORMAL, MoveCategory.PHYSICAL, 5, "This attack move cuts down the target's HP to equal the user's HP.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getFixedDamageAmount(ActivePokemon me, ActivePokemon o) {
            return o.getHP() - me.getHP();
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getHP() < victim.getHP();
        }
    }

    static class SandAttack extends Attack {
        private static final long serialVersionUID = 1L;

        SandAttack() {
            super(AttackNamesies.SAND_ATTACK, Type.GROUND, MoveCategory.STATUS, 15, "Sand is hurled in the target's face, reducing the target's accuracy.");
            super.accuracy = 100;
            super.statChanges[Stat.ACCURACY.index()] = -1;
        }
    }

    static class QuickAttack extends Attack {
        private static final long serialVersionUID = 1L;

        QuickAttack() {
            super(AttackNamesies.QUICK_ATTACK, Type.NORMAL, MoveCategory.PHYSICAL, 30, "The user lunges at the target at a speed that makes it almost invisible. This move always goes first.");
            super.power = 40;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.priority = 1;
        }
    }

    // Twice as strong when the opponent is flying
    static class Twister extends Attack implements SemiInvulnerableBypasser, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Twister() {
            super(AttackNamesies.TWISTER, Type.DRAGON, MoveCategory.SPECIAL, 20, "The user whips up a vicious tornado to tear at the opposing Pok\u00e9mon. This may also make them flinch.");
            super.power = 40;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 20;
        }

        @Override
        public boolean semiInvulnerableBypass(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Always hit when the opponent is flying
            return defending.isSemiInvulnerableFlying();
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.isSemiInvulnerableFlying() ? 2 : 1;
        }
    }

    static class FeatherDance extends Attack {
        private static final long serialVersionUID = 1L;

        FeatherDance() {
            super(AttackNamesies.FEATHER_DANCE, Type.FLYING, MoveCategory.STATUS, 15, "The user covers the target's body with a mass of down that harshly lowers its Attack stat.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.DANCE);
            super.statChanges[Stat.ATTACK.index()] = -2;
        }
    }

    static class Roost extends Attack implements ChangeTypeSource, SelfHealingMove {
        private static final long serialVersionUID = 1L;

        Roost() {
            super(AttackNamesies.ROOST, Type.FLYING, MoveCategory.STATUS, 10, "The user lands and rests its body. It restores the user's HP by up to half of its max HP.");
            super.moveTypes.add(MoveType.HEALING);
            super.selfTarget = true;
            super.printCast = false;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            this.heal(b, victim);

            if (getType(b, user, victim) != null) {
                Effect.cast(PokemonEffectNamesies.CHANGE_TYPE, b, user, victim, CastSource.ATTACK, super.printCast);
                user.getEffect(PokemonEffectNamesies.CHANGE_TYPE).setTurns(1);
            }
        }

        @Override
        public PokeType getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
            PokeType type = victim.getType(b);
            if (type.getFirstType() == Type.FLYING) {
                return new PokeType(type.getSecondType());
            }

            if (type.getSecondType() == Type.FLYING) {
                return new PokeType(type.getFirstType());
            }

            return null;
        }

        @Override
        public double getHealFraction(Battle b, ActivePokemon victim) {
            return .5;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.canHeal();
        }
    }

    static class ThunderShock extends Attack {
        private static final long serialVersionUID = 1L;

        ThunderShock() {
            super(AttackNamesies.THUNDER_SHOCK, Type.ELECTRIC, MoveCategory.SPECIAL, 30, "A jolt of electricity crashes down on the target to inflict damage. This may also leave the target with paralysis.");
            super.power = 40;
            super.accuracy = 100;
            super.effectChance = 10;
            super.status = StatusNamesies.PARALYZED;
        }
    }

    static class MirrorMove extends Attack {
        private static final long serialVersionUID = 1L;

        private AttackNamesies mirror;

        MirrorMove() {
            super(AttackNamesies.MIRROR_MOVE, Type.FLYING, MoveCategory.STATUS, 20, "The user counters the target by mimicking the target's last move.");
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            super.moveTypes.add(MoveType.ENCORELESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.MIRRORLESS);
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
        }

        @Override
        public void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            Move lastMoveUsed = defending.getLastMoveUsed();
            this.mirror = lastMoveUsed == null ? null : lastMoveUsed.getAttack().namesies();
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return mirror != null && !mirror.getNewAttack().isMoveType(MoveType.MIRRORLESS);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            user.callNewMove(b, victim, this.mirror);
        }
    }

    // Twice as strong when the opponent is flying
    static class Hurricane extends Attack implements BasicAccuracyBypassEffect, SemiInvulnerableBypasser, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Hurricane() {
            super(AttackNamesies.HURRICANE, Type.FLYING, MoveCategory.SPECIAL, 10, "The user attacks by wrapping its opponent in a fierce wind that flies up into the sky. This may also confuse the target.");
            super.power = 110;
            super.accuracy = 70;
            super.effect = PokemonEffectNamesies.CONFUSION;
            super.effectChance = 30;
        }

        @Override
        public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o) {
            // Accuracy is only 50% when sunny
            if (b.getWeather().namesies() == WeatherNamesies.SUNNY) {
                return 50;
            }

            return super.accuracy;
        }

        @Override
        public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Always hits in the rain
            return b.getWeather().namesies() == WeatherNamesies.RAINING;
        }

        @Override
        public boolean semiInvulnerableBypass(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Always hit when the opponent is flying
            return defending.isSemiInvulnerableFlying();
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.isSemiInvulnerableFlying() ? 2 : 1;
        }
    }

    static class HyperFang extends Attack {
        private static final long serialVersionUID = 1L;

        HyperFang() {
            super(AttackNamesies.HYPER_FANG, Type.NORMAL, MoveCategory.PHYSICAL, 15, "The user bites hard on the target with its sharp front fangs. This may also make the target flinch.");
            super.power = 80;
            super.accuracy = 90;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 10;
            super.moveTypes.add(MoveType.BITING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class SuckerPunch extends Attack {
        private static final long serialVersionUID = 1L;

        SuckerPunch() {
            super(AttackNamesies.SUCKER_PUNCH, Type.DARK, MoveCategory.PHYSICAL, 5, "This move enables the user to attack first. This move fails if the target is not readying an attack.");
            super.power = 70;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.priority = 1;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return !victim.getMove().getAttack().isStatusMove();
        }
    }

    static class Crunch extends Attack {
        private static final long serialVersionUID = 1L;

        Crunch() {
            super(AttackNamesies.CRUNCH, Type.DARK, MoveCategory.PHYSICAL, 15, "The user crunches up the target with sharp fangs. This may also lower the target's Defense stat.");
            super.power = 80;
            super.accuracy = 100;
            super.effectChance = 20;
            super.moveTypes.add(MoveType.BITING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.statChanges[Stat.DEFENSE.index()] = -1;
        }
    }

    static class SuperFang extends Attack implements FixedDamageMove {
        private static final long serialVersionUID = 1L;

        SuperFang() {
            super(AttackNamesies.SUPER_FANG, Type.NORMAL, MoveCategory.PHYSICAL, 10, "The user chomps hard on the target with its sharp front fangs. This cuts the target's HP in half.");
            super.accuracy = 90;
            super.moveTypes.add(MoveType.BITING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getFixedDamageAmount(ActivePokemon me, ActivePokemon o) {
            return (int)Math.ceil(o.getHP()/2.0);
        }
    }

    static class NaturesMadness extends Attack implements FixedDamageMove {
        private static final long serialVersionUID = 1L;

        NaturesMadness() {
            super(AttackNamesies.NATURES_MADNESS, Type.FAIRY, MoveCategory.SPECIAL, 10, "The user hits the target with the force of nature. It halves the target's HP.");
            super.accuracy = 90;
        }

        @Override
        public int getFixedDamageAmount(ActivePokemon me, ActivePokemon o) {
            return (int)Math.ceil(o.getHP()/2.0);
        }
    }

    static class SwordsDance extends Attack {
        private static final long serialVersionUID = 1L;

        SwordsDance() {
            super(AttackNamesies.SWORDS_DANCE, Type.NORMAL, MoveCategory.STATUS, 20, "A frenetic dance to uplift the fighting spirit. This sharply raises the user's Attack stat.");
            super.moveTypes.add(MoveType.DANCE);
            super.selfTarget = true;
            super.statChanges[Stat.ATTACK.index()] = 2;
        }
    }

    static class Peck extends Attack {
        private static final long serialVersionUID = 1L;

        Peck() {
            super(AttackNamesies.PECK, Type.FLYING, MoveCategory.PHYSICAL, 35, "The target is jabbed with a sharply pointed beak or horn.");
            super.power = 35;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Leer extends Attack {
        private static final long serialVersionUID = 1L;

        Leer() {
            super(AttackNamesies.LEER, Type.NORMAL, MoveCategory.STATUS, 30, "The user gives opposing Pok\u00e9mon an intimidating leer that lowers the Defense stat.");
            super.accuracy = 100;
            super.statChanges[Stat.DEFENSE.index()] = -1;
        }
    }

    static class AerialAce extends Attack {
        private static final long serialVersionUID = 1L;

        AerialAce() {
            super(AttackNamesies.AERIAL_ACE, Type.FLYING, MoveCategory.PHYSICAL, 20, "The user confounds the target with speed, then slashes. This attack never misses.");
            super.power = 60;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class DrillPeck extends Attack {
        private static final long serialVersionUID = 1L;

        DrillPeck() {
            super(AttackNamesies.DRILL_PECK, Type.FLYING, MoveCategory.PHYSICAL, 20, "A corkscrewing attack with a sharp beak acting as a drill.");
            super.power = 80;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Pluck extends Attack {
        private static final long serialVersionUID = 1L;

        Pluck() {
            super(AttackNamesies.PLUCK, Type.FLYING, MoveCategory.PHYSICAL, 20, "The user pecks the target. If the target is holding a Berry, the user eats it and gains its effect.");
            super.power = 60;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            HoldItem item = victim.getHeldItem(b);
            if (item instanceof Berry) {
                ((Berry)item).stealBerry(b, user, victim);
            }
        }
    }

    static class DrillRun extends Attack implements CritStageEffect {
        private static final long serialVersionUID = 1L;

        DrillRun() {
            super(AttackNamesies.DRILL_RUN, Type.GROUND, MoveCategory.PHYSICAL, 10, "The user crashes into its target while rotating its body like a drill. Critical hits land more easily.");
            super.power = 80;
            super.accuracy = 95;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Wrap extends Attack {
        private static final long serialVersionUID = 1L;

        Wrap() {
            super(AttackNamesies.WRAP, Type.NORMAL, MoveCategory.PHYSICAL, 20, "A long body, vines, or the like are used to wrap and squeeze the target for four to five turns.");
            super.power = 15;
            super.accuracy = 90;
            super.effect = PokemonEffectNamesies.WRAPPED;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Glare extends Attack {
        private static final long serialVersionUID = 1L;

        Glare() {
            super(AttackNamesies.GLARE, Type.NORMAL, MoveCategory.STATUS, 30, "The user intimidates the target with the pattern on its belly to cause paralysis.");
            super.accuracy = 100;
            super.status = StatusNamesies.PARALYZED;
        }
    }

    static class Screech extends Attack {
        private static final long serialVersionUID = 1L;

        Screech() {
            super(AttackNamesies.SCREECH, Type.NORMAL, MoveCategory.STATUS, 40, "An earsplitting screech harshly lowers the target's Defense stat.");
            super.accuracy = 85;
            super.moveTypes.add(MoveType.SOUND_BASED);
            super.statChanges[Stat.DEFENSE.index()] = -2;
        }
    }

    static class Acid extends Attack {
        private static final long serialVersionUID = 1L;

        Acid() {
            super(AttackNamesies.ACID, Type.POISON, MoveCategory.SPECIAL, 30, "The opposing Pok\u00e9mon are attacked with a spray of harsh acid. This may also lower their Sp. Def stat.");
            super.power = 40;
            super.accuracy = 100;
            super.effectChance = 10;
            super.statChanges[Stat.SP_DEFENSE.index()] = -1;
        }
    }

    static class Stockpile extends Attack {
        private static final long serialVersionUID = 1L;

        Stockpile() {
            super(AttackNamesies.STOCKPILE, Type.NORMAL, MoveCategory.STATUS, 20, "The user charges up power and raises both its Defense and Sp. Def stats. The move can be used three times.");
            super.effect = PokemonEffectNamesies.STOCKPILE;
            super.selfTarget = true;
        }
    }

    static class SpitUp extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        SpitUp() {
            super(AttackNamesies.SPIT_UP, Type.NORMAL, MoveCategory.SPECIAL, 10, "The power stored using the move Stockpile is released at once in an attack. The more power is stored, the greater the move's power.");
            super.power = 100;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.MIRRORLESS);
        }

        @Override
        public String getPowerString() {
            return "--";
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Stockpile ends after Spit up is used
            user.getEffect(PokemonEffectNamesies.STOCKPILE).deactivate();
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            PokemonEffect stockpile = user.getEffect(PokemonEffectNamesies.STOCKPILE);
            int turns = stockpile.getTurns();
            if (turns <= 0) {
                Global.error("Stockpile turns should never be nonpositive");
            }

            // Max power is 300
            return Math.min(turns, 3);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.hasEffect(PokemonEffectNamesies.STOCKPILE);
        }
    }

    static class Swallow extends Attack implements SelfHealingMove {
        private static final long serialVersionUID = 1L;

        Swallow() {
            super(AttackNamesies.SWALLOW, Type.NORMAL, MoveCategory.STATUS, 10, "The power stored using the move Stockpile is absorbed by the user to heal its HP. Storing more power heals more HP.");
            super.moveTypes.add(MoveType.HEALING);
            super.selfTarget = true;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            this.heal(b, victim);

            // Stockpile ends after Swallow is used
            user.getEffect(PokemonEffectNamesies.STOCKPILE).deactivate();
        }

        @Override
        public double getHealFraction(Battle b, ActivePokemon victim) {
            PokemonEffect stockpile = victim.getEffect(PokemonEffectNamesies.STOCKPILE);
            int turns = stockpile.getTurns();
            if (turns <= 0) {
                Global.error("Stockpile turns should never be nonpositive");
            }

            // Heals differently based on number of stockpile turns
            if (turns == 1) {
                return .25;
            } else if (turns == 2) {
                return .5;
            } else {
                return 1;
            }
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.hasEffect(PokemonEffectNamesies.STOCKPILE) && user.canHeal();
        }
    }

    static class AcidSpray extends Attack {
        private static final long serialVersionUID = 1L;

        AcidSpray() {
            super(AttackNamesies.ACID_SPRAY, Type.POISON, MoveCategory.SPECIAL, 20, "The user spits fluid that works to melt the target. This harshly lowers the target's Sp. Def stat.");
            super.power = 40;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.BOMB_BALL);
            super.statChanges[Stat.SP_DEFENSE.index()] = -2;
        }
    }

    static class MudBomb extends Attack {
        private static final long serialVersionUID = 1L;

        MudBomb() {
            super(AttackNamesies.MUD_BOMB, Type.GROUND, MoveCategory.SPECIAL, 10, "The user launches a hard-packed mud ball to attack. This may also lower the target's accuracy.");
            super.power = 65;
            super.accuracy = 85;
            super.effectChance = 30;
            super.moveTypes.add(MoveType.BOMB_BALL);
            super.statChanges[Stat.ACCURACY.index()] = -1;
        }
    }

    static class Haze extends Attack {
        private static final long serialVersionUID = 1L;

        Haze() {
            super(AttackNamesies.HAZE, Type.ICE, MoveCategory.STATUS, 30, "The user creates a haze that eliminates every stat change among all the Pok\u00e9mon engaged in battle.");
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.FIELD);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            user.getStages().reset();
            victim.getStages().reset();
            Messages.add("All stat changes were eliminated!");
        }
    }

    static class Coil extends Attack {
        private static final long serialVersionUID = 1L;

        Coil() {
            super(AttackNamesies.COIL, Type.POISON, MoveCategory.STATUS, 20, "The user coils up and concentrates. This raises its Attack and Defense stats as well as its accuracy.");
            super.selfTarget = true;
            super.statChanges[Stat.ATTACK.index()] = 1;
            super.statChanges[Stat.DEFENSE.index()] = 1;
            super.statChanges[Stat.ACCURACY.index()] = 1;
        }
    }

    static class GunkShot extends Attack {
        private static final long serialVersionUID = 1L;

        GunkShot() {
            super(AttackNamesies.GUNK_SHOT, Type.POISON, MoveCategory.PHYSICAL, 5, "The user shoots filthy garbage at the target to attack. This may also poison the target.");
            super.power = 120;
            super.accuracy = 80;
            super.effectChance = 30;
            super.status = StatusNamesies.POISONED;
        }
    }

    static class IceFang extends Attack {
        private static final long serialVersionUID = 1L;

        IceFang() {
            super(AttackNamesies.ICE_FANG, Type.ICE, MoveCategory.PHYSICAL, 15, "The user bites with cold-infused fangs. This may also make the target flinch or leave it frozen.");
            super.power = 65;
            super.accuracy = 95;
            super.effectChance = 20;
            super.moveTypes.add(MoveType.BITING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            super.status = StatusNamesies.NO_STATUS;
            super.effect = null;

            // If the effect is being applied, 50/50 chance to give a status condition vs. flinching
            if (RandomUtils.chanceTest(50)) {
                super.status = StatusNamesies.FROZEN;
            } else {
                super.effect = PokemonEffectNamesies.FLINCH;
            }
        }
    }

    static class ThunderFang extends Attack {
        private static final long serialVersionUID = 1L;

        ThunderFang() {
            super(AttackNamesies.THUNDER_FANG, Type.ELECTRIC, MoveCategory.PHYSICAL, 15, "The user bites with electrified fangs. This may also make the target flinch or leave it with paralysis.");
            super.power = 65;
            super.accuracy = 95;
            super.effectChance = 20;
            super.moveTypes.add(MoveType.BITING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            super.status = StatusNamesies.NO_STATUS;
            super.effect = null;

            // If the effect is being applied, 50/50 chance to give a status condition vs. flinching
            if (RandomUtils.chanceTest(50)) {
                super.status = StatusNamesies.PARALYZED;
            } else {
                super.effect = PokemonEffectNamesies.FLINCH;
            }
        }
    }

    static class ElectroBall extends Attack {
        private static final long serialVersionUID = 1L;

        ElectroBall() {
            super(AttackNamesies.ELECTRO_BALL, Type.ELECTRIC, MoveCategory.SPECIAL, 10, "The user hurls an electric orb at the target. The faster the user is than the target, the greater the move's power.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.BOMB_BALL);
        }

        @Override
        public int getPower(Battle b, ActivePokemon me, ActivePokemon o) {
            double ratio = (double)Stat.SPEED.getBasicStat(b, o)/Stat.SPEED.getBasicStat(b, me);
            if (ratio > .5) {
                return 60;
            } else if (ratio > .33) {
                return 80;
            } else if (ratio > .25) {
                return 120;
            } else {
                return 150;
            }
        }
    }

    static class DoubleTeam extends Attack {
        private static final long serialVersionUID = 1L;

        DoubleTeam() {
            super(AttackNamesies.DOUBLE_TEAM, Type.NORMAL, MoveCategory.STATUS, 15, "By moving rapidly, the user makes illusory copies of itself to raise its evasiveness.");
            super.selfTarget = true;
            super.statChanges[Stat.EVASION.index()] = 1;
        }
    }

    static class Slam extends Attack {
        private static final long serialVersionUID = 1L;

        Slam() {
            super(AttackNamesies.SLAM, Type.NORMAL, MoveCategory.PHYSICAL, 20, "The target is slammed with a long tail, vines, or the like to inflict damage.");
            super.power = 80;
            super.accuracy = 75;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Thunderbolt extends Attack {
        private static final long serialVersionUID = 1L;

        Thunderbolt() {
            super(AttackNamesies.THUNDERBOLT, Type.ELECTRIC, MoveCategory.SPECIAL, 15, "A strong electric blast crashes down on the target. This may also leave the target with paralysis.");
            super.power = 90;
            super.accuracy = 100;
            super.effectChance = 10;
            super.status = StatusNamesies.PARALYZED;
        }
    }

    static class Feint extends Attack {
        private static final long serialVersionUID = 1L;

        Feint() {
            super(AttackNamesies.FEINT, Type.NORMAL, MoveCategory.PHYSICAL, 10, "This attack hits a target using a move such as Protect or Detect. This also lifts the effects of those moves.");
            super.power = 30;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.priority = 2;
        }
    }

    static class Discharge extends Attack {
        private static final long serialVersionUID = 1L;

        Discharge() {
            super(AttackNamesies.DISCHARGE, Type.ELECTRIC, MoveCategory.SPECIAL, 15, "The user strikes everything around it by letting loose a flare of electricity. This may also cause paralysis.");
            super.power = 80;
            super.accuracy = 100;
            super.effectChance = 30;
            super.status = StatusNamesies.PARALYZED;
        }
    }

    static class LightScreen extends Attack {
        private static final long serialVersionUID = 1L;

        LightScreen() {
            super(AttackNamesies.LIGHT_SCREEN, Type.PSYCHIC, MoveCategory.STATUS, 30, "A wondrous wall of light is put up to reduce damage from special attacks for five turns.");
            super.effect = TeamEffectNamesies.LIGHT_SCREEN;
            super.selfTarget = true;
        }
    }

    // Twice as strong when the opponent is flying
    static class Thunder extends Attack implements BasicAccuracyBypassEffect, SemiInvulnerableBypasser, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Thunder() {
            super(AttackNamesies.THUNDER, Type.ELECTRIC, MoveCategory.SPECIAL, 10, "A wicked thunderbolt is dropped on the target to inflict damage. This may also leave the target with paralysis.");
            super.power = 110;
            super.accuracy = 70;
            super.effectChance = 30;
            super.status = StatusNamesies.PARALYZED;
        }

        @Override
        public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o) {
            // Accuracy is only 50% when sunny
            if (b.getWeather().namesies() == WeatherNamesies.SUNNY) {
                return 50;
            }

            return super.accuracy;
        }

        @Override
        public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Always hits in the rain
            return b.getWeather().namesies() == WeatherNamesies.RAINING;
        }

        @Override
        public boolean semiInvulnerableBypass(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Always hit when the opponent is flying
            return defending.isSemiInvulnerableFlying();
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.isSemiInvulnerableFlying() ? 2 : 1;
        }
    }

    static class DefenseCurl extends Attack {
        private static final long serialVersionUID = 1L;

        DefenseCurl() {
            super(AttackNamesies.DEFENSE_CURL, Type.NORMAL, MoveCategory.STATUS, 40, "The user curls up to conceal weak spots and raise its Defense stat.");
            super.effect = PokemonEffectNamesies.USED_DEFENSE_CURL;
            super.selfTarget = true;
            super.statChanges[Stat.DEFENSE.index()] = 1;
        }
    }

    static class Swift extends Attack {
        private static final long serialVersionUID = 1L;

        Swift() {
            super(AttackNamesies.SWIFT, Type.NORMAL, MoveCategory.SPECIAL, 20, "Star-shaped rays are shot at the opposing Pok\u00e9mon. This attack never misses.");
            super.power = 60;
        }
    }

    static class FurySwipes extends Attack implements MultiStrikeMove {
        private static final long serialVersionUID = 1L;

        FurySwipes() {
            super(AttackNamesies.FURY_SWIPES, Type.NORMAL, MoveCategory.PHYSICAL, 15, "The target is raked with sharp claws or scythes quickly two to five times in a row.");
            super.power = 18;
            super.accuracy = 80;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getMinHits() {
            return 2;
        }

        @Override
        public int getMaxHits() {
            return 5;
        }
    }

    static class Rollout extends Attack implements PowerCountMove {
        private static final long serialVersionUID = 1L;

        Rollout() {
            super(AttackNamesies.ROLLOUT, Type.ROCK, MoveCategory.PHYSICAL, 20, "The user continually rolls into the target over five turns. It becomes more powerful each time it hits.");
            super.power = 30;
            super.accuracy = 90;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public boolean doubleDefenseCurled() {
            return true;
        }
    }

    static class FuryCutter extends Attack implements PowerCountMove {
        private static final long serialVersionUID = 1L;

        FuryCutter() {
            super(AttackNamesies.FURY_CUTTER, Type.BUG, MoveCategory.PHYSICAL, 20, "The target is slashed with scythes or claws. This attack becomes more powerful if it hits in succession.");
            super.power = 40;
            super.accuracy = 95;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public boolean doubleDefenseCurled() {
            return false;
        }
    }

    static class SandTomb extends Attack {
        private static final long serialVersionUID = 1L;

        SandTomb() {
            super(AttackNamesies.SAND_TOMB, Type.GROUND, MoveCategory.PHYSICAL, 15, "The user traps the target inside a harshly raging sandstorm for four to five turns.");
            super.power = 35;
            super.accuracy = 85;
            super.effect = PokemonEffectNamesies.SAND_TOMB;
        }
    }

    static class GyroBall extends Attack {
        private static final long serialVersionUID = 1L;

        GyroBall() {
            super(AttackNamesies.GYRO_BALL, Type.STEEL, MoveCategory.PHYSICAL, 5, "The user tackles the target with a high-speed spin. The slower the user compared to the target, the greater the move's power.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.BOMB_BALL);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getPower(Battle b, ActivePokemon me, ActivePokemon o) {
            return (int)Math.min(150, 25.0*Stat.SPEED.getBasicStat(b, o)/Stat.SPEED.getBasicStat(b, me));
        }
    }

    static class CrushClaw extends Attack {
        private static final long serialVersionUID = 1L;

        CrushClaw() {
            super(AttackNamesies.CRUSH_CLAW, Type.NORMAL, MoveCategory.PHYSICAL, 10, "The user slashes the target with hard and sharp claws. This may also lower the target's Defense stat.");
            super.power = 75;
            super.accuracy = 95;
            super.effectChance = 50;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.statChanges[Stat.DEFENSE.index()] = -1;
        }
    }

    static class DoubleKick extends Attack implements MultiStrikeMove {
        private static final long serialVersionUID = 1L;

        DoubleKick() {
            super(AttackNamesies.DOUBLE_KICK, Type.FIGHTING, MoveCategory.PHYSICAL, 30, "The target is quickly kicked twice in succession using both feet.");
            super.power = 30;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getMinHits() {
            return 2;
        }

        @Override
        public int getMaxHits() {
            return 2;
        }
    }

    static class PoisonTail extends Attack implements CritStageEffect {
        private static final long serialVersionUID = 1L;

        PoisonTail() {
            super(AttackNamesies.POISON_TAIL, Type.POISON, MoveCategory.PHYSICAL, 25, "The user hits the target with its tail. This may also poison the target. Critical hits land more easily.");
            super.power = 50;
            super.accuracy = 100;
            super.effectChance = 10;
            super.status = StatusNamesies.POISONED;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Flatter extends Attack {
        private static final long serialVersionUID = 1L;

        Flatter() {
            super(AttackNamesies.FLATTER, Type.DARK, MoveCategory.STATUS, 15, "Flattery is used to confuse the target. However, this also raises the target's Sp. Atk stat.");
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.CONFUSION;
            super.statChanges[Stat.SP_ATTACK.index()] = 1;
        }
    }

    static class PoisonFang extends Attack {
        private static final long serialVersionUID = 1L;

        PoisonFang() {
            super(AttackNamesies.POISON_FANG, Type.POISON, MoveCategory.PHYSICAL, 15, "The user bites the target with toxic fangs. This may also leave the target badly poisoned.");
            super.power = 50;
            super.accuracy = 100;
            super.effectChance = 50;
            super.status = StatusNamesies.BADLY_POISONED;
            super.moveTypes.add(MoveType.BITING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class ChipAway extends Attack implements OpponentIgnoreStageEffect {
        private static final long serialVersionUID = 1L;

        ChipAway() {
            super(AttackNamesies.CHIP_AWAY, Type.NORMAL, MoveCategory.PHYSICAL, 20, "Looking for an opening, the user strikes consistently. The target's stat changes don't affect this attack's damage.");
            super.power = 70;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public boolean ignoreStage(Stat s) {
            return !s.user();
        }
    }

    static class BodySlam extends Attack implements DoubleMinimizerMove {
        private static final long serialVersionUID = 1L;

        BodySlam() {
            super(AttackNamesies.BODY_SLAM, Type.NORMAL, MoveCategory.PHYSICAL, 15, "The user drops onto the target with its full body weight. This may also leave the target with paralysis.");
            super.power = 85;
            super.accuracy = 100;
            super.effectChance = 30;
            super.status = StatusNamesies.PARALYZED;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class EarthPower extends Attack {
        private static final long serialVersionUID = 1L;

        EarthPower() {
            super(AttackNamesies.EARTH_POWER, Type.GROUND, MoveCategory.SPECIAL, 10, "The user makes the ground under the target erupt with power. This may also lower the target's Sp. Def stat.");
            super.power = 90;
            super.accuracy = 100;
            super.effectChance = 10;
            super.statChanges[Stat.SP_DEFENSE.index()] = -1;
        }
    }

    static class Superpower extends Attack {
        private static final long serialVersionUID = 1L;

        Superpower() {
            super(AttackNamesies.SUPERPOWER, Type.FIGHTING, MoveCategory.PHYSICAL, 5, "The user attacks the target with great power. However, this also lowers the user's Attack and Defense stats.");
            super.power = 120;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.selfTarget = true;
            super.statChanges[Stat.ATTACK.index()] = -1;
            super.statChanges[Stat.DEFENSE.index()] = -1;
        }
    }

    static class HornAttack extends Attack {
        private static final long serialVersionUID = 1L;

        HornAttack() {
            super(AttackNamesies.HORN_ATTACK, Type.NORMAL, MoveCategory.PHYSICAL, 25, "The target is jabbed with a sharply pointed horn to inflict damage.");
            super.power = 65;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class HornDrill extends Attack implements OhkoMove {
        private static final long serialVersionUID = 1L;

        HornDrill() {
            super(AttackNamesies.HORN_DRILL, Type.NORMAL, MoveCategory.PHYSICAL, 5, "The user stabs the target with a horn that rotates like a drill. The target faints instantly if this attack hits.");
            super.accuracy = 30;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Megahorn extends Attack {
        private static final long serialVersionUID = 1L;

        Megahorn() {
            super(AttackNamesies.MEGAHORN, Type.BUG, MoveCategory.PHYSICAL, 10, "Using its tough and impressive horn, the user rams into the target with no letup.");
            super.power = 120;
            super.accuracy = 85;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Pound extends Attack {
        private static final long serialVersionUID = 1L;

        Pound() {
            super(AttackNamesies.POUND, Type.NORMAL, MoveCategory.PHYSICAL, 35, "The target is physically pounded with a long tail, a foreleg, or the like.");
            super.power = 40;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Sing extends Attack {
        private static final long serialVersionUID = 1L;

        Sing() {
            super(AttackNamesies.SING, Type.NORMAL, MoveCategory.STATUS, 15, "A soothing lullaby is sung in a calming voice that puts the target into a deep slumber.");
            super.accuracy = 55;
            super.status = StatusNamesies.ASLEEP;
            super.moveTypes.add(MoveType.SOUND_BASED);
        }
    }

    static class DoubleSlap extends Attack implements MultiStrikeMove {
        private static final long serialVersionUID = 1L;

        DoubleSlap() {
            super(AttackNamesies.DOUBLE_SLAP, Type.NORMAL, MoveCategory.PHYSICAL, 10, "The target is slapped repeatedly, back and forth, two to five times in a row.");
            super.power = 15;
            super.accuracy = 85;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getMinHits() {
            return 2;
        }

        @Override
        public int getMaxHits() {
            return 5;
        }
    }

    static class Wish extends Attack {
        private static final long serialVersionUID = 1L;

        Wish() {
            super(AttackNamesies.WISH, Type.NORMAL, MoveCategory.STATUS, 10, "One turn after this move is used, the user's or its replacement's HP is restored by half the user's max HP.");
            super.effect = TeamEffectNamesies.WISH;
            super.moveTypes.add(MoveType.HEALING);
            super.selfTarget = true;
        }
    }

    static class Minimize extends Attack {
        private static final long serialVersionUID = 1L;

        Minimize() {
            super(AttackNamesies.MINIMIZE, Type.NORMAL, MoveCategory.STATUS, 10, "The user compresses its body to make itself look smaller, which sharply raises its evasiveness.");
            super.effect = PokemonEffectNamesies.USED_MINIMIZE;
            super.selfTarget = true;
            super.statChanges[Stat.EVASION.index()] = 2;
        }
    }

    static class WakeUpSlap extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        WakeUpSlap() {
            super(AttackNamesies.WAKE_UP_SLAP, Type.FIGHTING, MoveCategory.PHYSICAL, 10, "This attack inflicts big damage on a sleeping target. This also wakes the target up, however.");
            super.power = 70;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (victim.hasStatus(StatusNamesies.ASLEEP)) {
                victim.removeStatus(b, CastSource.ATTACK);
            }
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.hasStatus(StatusNamesies.ASLEEP) ? 2 : 1;
        }
    }

    static class CosmicPower extends Attack {
        private static final long serialVersionUID = 1L;

        CosmicPower() {
            super(AttackNamesies.COSMIC_POWER, Type.PSYCHIC, MoveCategory.STATUS, 20, "The user absorbs a mystical power from space to raise its Defense and Sp. Def stats.");
            super.selfTarget = true;
            super.statChanges[Stat.DEFENSE.index()] = 1;
            super.statChanges[Stat.SP_DEFENSE.index()] = 1;
        }
    }

    static class LuckyChant extends Attack {
        private static final long serialVersionUID = 1L;

        LuckyChant() {
            super(AttackNamesies.LUCKY_CHANT, Type.NORMAL, MoveCategory.STATUS, 30, "The user chants an incantation toward the sky, preventing opposing Pok\u00e9mon from landing critical hits for five turns.");
            super.effect = TeamEffectNamesies.LUCKY_CHANT;
            super.selfTarget = true;
        }
    }

    static class Metronome extends Attack {
        private static final long serialVersionUID = 1L;

        private Attack metronomeMove;

        Metronome() {
            super(AttackNamesies.METRONOME, Type.NORMAL, MoveCategory.STATUS, 10, "The user waggles a finger and stimulates its brain into randomly using nearly any move.");
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            super.moveTypes.add(MoveType.MIMICLESS);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
            super.moveTypes.add(MoveType.MIRRORLESS);
        }

        @Override
        public void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            AttackNamesies[] attackNames = AttackNamesies.values();
            do {
                int index = RandomUtils.getRandomIndex(attackNames);
                metronomeMove = attackNames[index].getNewAttack();
            } while (metronomeMove.isMoveType(MoveType.METRONOMELESS));
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            user.callFullNewMove(b, victim, metronomeMove.namesies());
        }
    }

    static class Gravity extends Attack {
        private static final long serialVersionUID = 1L;

        Gravity() {
            super(AttackNamesies.GRAVITY, Type.PSYCHIC, MoveCategory.STATUS, 5, "Enables Flying-type Pok\u00e9mon or Pok\u00e9mon with the Levitate Ability to be hit by Ground-type moves. Flying moves can't be used.");
            super.effect = StandardBattleEffectNamesies.GRAVITY;
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.FIELD);
        }
    }

    static class Moonlight extends Attack implements SelfHealingMove {
        private static final long serialVersionUID = 1L;

        Moonlight() {
            super(AttackNamesies.MOONLIGHT, Type.FAIRY, MoveCategory.STATUS, 5, "The user restores its own HP. The amount of HP regained varies with the weather.");
            super.moveTypes.add(MoveType.HEALING);
            super.selfTarget = true;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            this.heal(b, victim);
        }

        @Override
        public double getHealFraction(Battle b, ActivePokemon victim) {
            switch (b.getWeather().namesies()) {
                case SUNNY:
                    return 2/3.0;
                case HAILING:
                case RAINING:
                case SANDSTORM:
                    return 1/4.0;
                default:
                    return 1/2.0;
            }
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.canHeal();
        }
    }

    static class StoredPower extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        StoredPower() {
            super(AttackNamesies.STORED_POWER, Type.PSYCHIC, MoveCategory.SPECIAL, 10, "The user attacks the target with stored power. The more the user's stats are raised, the greater the move's power.");
            super.power = 20;
            super.accuracy = 100;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return 1 + user.getStages().totalStatIncreases();
        }
    }

    static class PowerTrip extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        PowerTrip() {
            super(AttackNamesies.POWER_TRIP, Type.DARK, MoveCategory.PHYSICAL, 10, "The user boasts its strength and attacks the target. The more the user's stats are raised, the greater the move's power.");
            super.power = 20;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return 1 + user.getStages().totalStatIncreases();
        }
    }

    static class Mimic extends Attack {
        private static final long serialVersionUID = 1L;

        Mimic() {
            super(AttackNamesies.MIMIC, Type.NORMAL, MoveCategory.STATUS, 10, "The user copies the target's last move. The move can be used during battle until the Pok\u00e9mon is switched out.");
            super.effect = PokemonEffectNamesies.MIMIC;
            super.moveTypes.add(MoveType.ENCORELESS);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.selfTarget = true;
        }
    }

    static class MeteorMash extends Attack {
        private static final long serialVersionUID = 1L;

        MeteorMash() {
            super(AttackNamesies.METEOR_MASH, Type.STEEL, MoveCategory.PHYSICAL, 10, "The target is hit with a hard punch fired like a meteor. This may also raise the user's Attack stat.");
            super.power = 90;
            super.accuracy = 90;
            super.effectChance = 20;
            super.moveTypes.add(MoveType.PUNCHING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.selfTarget = true;
            super.statChanges[Stat.ATTACK.index()] = 1;
        }
    }

    static class Imprison extends Attack {
        private static final long serialVersionUID = 1L;

        Imprison() {
            super(AttackNamesies.IMPRISON, Type.PSYCHIC, MoveCategory.STATUS, 10, "If opposing Pok\u00e9mon know any move also known by the user, they are prevented from using it.");
            super.effect = PokemonEffectNamesies.IMPRISON;
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
        }
    }

    static class WillOWisp extends Attack {
        private static final long serialVersionUID = 1L;

        WillOWisp() {
            super(AttackNamesies.WILL_O_WISP, Type.FIRE, MoveCategory.STATUS, 15, "The user shoots a sinister, bluish-white flame at the target to inflict a burn.");
            super.accuracy = 85;
            super.status = StatusNamesies.BURNED;
        }
    }

    static class Payback extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Payback() {
            super(AttackNamesies.PAYBACK, Type.DARK, MoveCategory.PHYSICAL, 10, "The user stores power, then attacks. If the user moves after the target, this attack's power will be doubled.");
            super.power = 50;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return !b.isFirstAttack() ? 2 : 1;
        }
    }

    static class Extrasensory extends Attack {
        private static final long serialVersionUID = 1L;

        Extrasensory() {
            super(AttackNamesies.EXTRASENSORY, Type.PSYCHIC, MoveCategory.SPECIAL, 20, "The user attacks with an odd, unseeable power. This may also make the target flinch.");
            super.power = 80;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 10;
        }
    }

    static class FireBlast extends Attack {
        private static final long serialVersionUID = 1L;

        FireBlast() {
            super(AttackNamesies.FIRE_BLAST, Type.FIRE, MoveCategory.SPECIAL, 5, "The target is attacked with an intense blast of all-consuming fire. This may also leave the target with a burn.");
            super.power = 110;
            super.accuracy = 85;
            super.effectChance = 10;
            super.status = StatusNamesies.BURNED;
        }
    }

    static class NastyPlot extends Attack {
        private static final long serialVersionUID = 1L;

        NastyPlot() {
            super(AttackNamesies.NASTY_PLOT, Type.DARK, MoveCategory.STATUS, 20, "The user stimulates its brain by thinking bad thoughts. This sharply raises the user's Sp. Atk stat.");
            super.selfTarget = true;
            super.statChanges[Stat.SP_ATTACK.index()] = 2;
        }
    }

    static class Round extends Attack {
        private static final long serialVersionUID = 1L;

        Round() {
            super(AttackNamesies.ROUND, Type.NORMAL, MoveCategory.SPECIAL, 15, "The user attacks the target with a song. Others can join in the Round to increase the power of the attack.");
            super.power = 60;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.SOUND_BASED);
        }
    }

    // Need the sleep check in case called from Sleep Talk, Comatose, etc.
    static class Rest extends Attack implements SelfHealingMove {
        private static final long serialVersionUID = 1L;

        Rest() {
            super(AttackNamesies.REST, Type.PSYCHIC, MoveCategory.STATUS, 10, "The user goes to sleep for two turns. This fully restores the user's HP and heals any status conditions.");
            super.status = StatusNamesies.ASLEEP;
            super.moveTypes.add(MoveType.HEALING);
            super.selfTarget = true;
        }

        @Override
        public void afterApplyCheck(Battle b, ActivePokemon user, ActivePokemon victim) {
            victim.removeStatus();
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            this.heal(b, victim);

            victim.getStatus().setTurns(2);
        }

        @Override
        public double getHealFraction(Battle b, ActivePokemon victim) {
            return 1;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return !user.hasStatus(StatusNamesies.ASLEEP) && StatusNamesies.ASLEEP.getStatus().appliesWithoutStatusCheck(b, user, user).isSuccess() && user.canHeal();
        }
    }

    static class HyperVoice extends Attack {
        private static final long serialVersionUID = 1L;

        HyperVoice() {
            super(AttackNamesies.HYPER_VOICE, Type.NORMAL, MoveCategory.SPECIAL, 10, "The user lets loose a horribly echoing shout with the power to inflict damage.");
            super.power = 90;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.SOUND_BASED);
        }
    }

    static class LeechLife extends Attack implements SapHealthMove {
        private static final long serialVersionUID = 1L;

        LeechLife() {
            super(AttackNamesies.LEECH_LIFE, Type.BUG, MoveCategory.PHYSICAL, 10, "The user drains the target's blood. The user's HP is restored by half the damage taken by the target.");
            super.power = 80;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.HEALING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Astonish extends Attack {
        private static final long serialVersionUID = 1L;

        Astonish() {
            super(AttackNamesies.ASTONISH, Type.GHOST, MoveCategory.PHYSICAL, 15, "The user attacks the target while shouting in a startling fashion. This may also make the target flinch.");
            super.power = 30;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 30;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class AirCutter extends Attack implements CritStageEffect {
        private static final long serialVersionUID = 1L;

        AirCutter() {
            super(AttackNamesies.AIR_CUTTER, Type.FLYING, MoveCategory.SPECIAL, 25, "The user launches razor-like wind to slash the opposing Pok\u00e9mon. Critical hits land more easily.");
            super.power = 60;
            super.accuracy = 95;
        }
    }

    static class MeanLook extends Attack {
        private static final long serialVersionUID = 1L;

        MeanLook() {
            super(AttackNamesies.MEAN_LOOK, Type.NORMAL, MoveCategory.STATUS, 5, "The user pins the target with a dark, arresting look. The target becomes unable to flee.");
            super.effect = PokemonEffectNamesies.TRAPPED;
        }
    }

    static class Acrobatics extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Acrobatics() {
            super(AttackNamesies.ACROBATICS, Type.FLYING, MoveCategory.PHYSICAL, 15, "The user nimbly strikes the target. If the user is not holding an item, this attack inflicts massive damage.");
            super.power = 55;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return !user.isHoldingItem(b) ? 2 : 1;
        }
    }

    static class Absorb extends Attack implements SapHealthMove {
        private static final long serialVersionUID = 1L;

        Absorb() {
            super(AttackNamesies.ABSORB, Type.GRASS, MoveCategory.SPECIAL, 25, "A nutrient-draining attack. The user's HP is restored by half the damage taken by the target.");
            super.power = 20;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.HEALING);
        }
    }

    static class MegaDrain extends Attack implements SapHealthMove {
        private static final long serialVersionUID = 1L;

        MegaDrain() {
            super(AttackNamesies.MEGA_DRAIN, Type.GRASS, MoveCategory.SPECIAL, 15, "A nutrient-draining attack. The user's HP is restored by half the damage taken by the target.");
            super.power = 40;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.HEALING);
        }
    }

    static class NaturalGift extends Attack {
        private static final long serialVersionUID = 1L;

        NaturalGift() {
            super(AttackNamesies.NATURAL_GIFT, Type.NORMAL, MoveCategory.PHYSICAL, 15, "The user draws power to attack by using its held Berry. The Berry determines the move's type and power.");
            super.accuracy = 100;
        }

        @Override
        public int getPower(Battle b, ActivePokemon me, ActivePokemon o) {
            return ((Berry)me.getHeldItem(b)).naturalGiftPower();
        }

        @Override
        public Type getType(Battle b, ActivePokemon user) {
            HoldItem item = user.getHeldItem(b);
            if (item instanceof Berry) {
                return ((Berry)item).naturalGiftType();
            }

            return super.type;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            // This is so fucking stupid that it consumes the Berry upon use, like srsly what the fuck is the fucking point of this move
            HoldItem item = user.getHeldItem(b);
            if (item instanceof Berry) {
                item.consumeItemWithoutEffects(b, user);
            }
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getHeldItem(b) instanceof Berry;
        }
    }

    static class GigaDrain extends Attack implements SapHealthMove {
        private static final long serialVersionUID = 1L;

        GigaDrain() {
            super(AttackNamesies.GIGA_DRAIN, Type.GRASS, MoveCategory.SPECIAL, 10, "A nutrient-draining attack. The user's HP is restored by half the damage taken by the target.");
            super.power = 75;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.HEALING);
        }
    }

    static class Aromatherapy extends Attack {
        private static final long serialVersionUID = 1L;

        Aromatherapy() {
            super(AttackNamesies.AROMATHERAPY, Type.GRASS, MoveCategory.STATUS, 5, "The user releases a soothing scent that heals all status conditions affecting the user's party.");
            super.moveTypes.add(MoveType.HEALING);
            super.selfTarget = true;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            for (ActivePokemon p : b.getTrainer(user).getActiveTeam()) {
                if (!p.isActuallyDead()) {
                    p.removeStatus();
                }
            }

            Messages.add("All status problems were cured!");
        }
    }

    static class Spore extends Attack implements PowderMove {
        private static final long serialVersionUID = 1L;

        Spore() {
            super(AttackNamesies.SPORE, Type.GRASS, MoveCategory.STATUS, 15, "The user scatters bursts of spores that induce sleep.");
            super.accuracy = 100;
            super.status = StatusNamesies.ASLEEP;
        }
    }

    static class CrossPoison extends Attack implements CritStageEffect {
        private static final long serialVersionUID = 1L;

        CrossPoison() {
            super(AttackNamesies.CROSS_POISON, Type.POISON, MoveCategory.PHYSICAL, 20, "A slashing attack with a poisonous blade that may also poison the target. Critical hits land more easily.");
            super.power = 70;
            super.accuracy = 100;
            super.effectChance = 10;
            super.status = StatusNamesies.POISONED;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class XScissor extends Attack {
        private static final long serialVersionUID = 1L;

        XScissor() {
            super(AttackNamesies.X_SCISSOR, Type.BUG, MoveCategory.PHYSICAL, 15, "The user slashes at the target by crossing its scythes or claws as if they were a pair of scissors.");
            super.power = 80;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Foresight extends Attack {
        private static final long serialVersionUID = 1L;

        Foresight() {
            super(AttackNamesies.FORESIGHT, Type.NORMAL, MoveCategory.STATUS, 40, "Enables a Ghost-type target to be hit by Normal- and Fighting-type attacks. This also enables an evasive target to be hit.");
            super.effect = PokemonEffectNamesies.FORESIGHT;
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            victim.getStages().resetStage(Stat.EVASION);
        }
    }

    static class OdorSleuth extends Attack {
        private static final long serialVersionUID = 1L;

        OdorSleuth() {
            super(AttackNamesies.ODOR_SLEUTH, Type.NORMAL, MoveCategory.STATUS, 40, "Enables a Ghost-type target to be hit by Normal- and Fighting-type attacks. This also enables an evasive target to be hit.");
            super.effect = PokemonEffectNamesies.FORESIGHT;
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            victim.getStages().resetStage(Stat.EVASION);
        }
    }

    static class MiracleEye extends Attack {
        private static final long serialVersionUID = 1L;

        MiracleEye() {
            super(AttackNamesies.MIRACLE_EYE, Type.PSYCHIC, MoveCategory.STATUS, 40, "Enables a Dark-type target to be hit by Psychic-type attacks. This also enables an evasive target to be hit.");
            super.effect = PokemonEffectNamesies.MIRACLE_EYE;
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            victim.getStages().resetStage(Stat.EVASION);
        }
    }

    static class Howl extends Attack {
        private static final long serialVersionUID = 1L;

        Howl() {
            super(AttackNamesies.HOWL, Type.NORMAL, MoveCategory.STATUS, 40, "The user howls loudly to raise its spirit, which raises its Attack stat.");
            super.selfTarget = true;
            super.statChanges[Stat.ATTACK.index()] = 1;
        }
    }

    static class SignalBeam extends Attack {
        private static final long serialVersionUID = 1L;

        SignalBeam() {
            super(AttackNamesies.SIGNAL_BEAM, Type.BUG, MoveCategory.SPECIAL, 15, "The user attacks with a sinister beam of light. This may also confuse the target.");
            super.power = 75;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.CONFUSION;
            super.effectChance = 10;
        }
    }

    static class ZenHeadbutt extends Attack {
        private static final long serialVersionUID = 1L;

        ZenHeadbutt() {
            super(AttackNamesies.ZEN_HEADBUTT, Type.PSYCHIC, MoveCategory.PHYSICAL, 15, "The user focuses its willpower to its head and attacks the target. This may also make the target flinch.");
            super.power = 80;
            super.accuracy = 90;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 20;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Psychic extends Attack {
        private static final long serialVersionUID = 1L;

        Psychic() {
            super(AttackNamesies.PSYCHIC, Type.PSYCHIC, MoveCategory.SPECIAL, 10, "The target is hit by a strong telekinetic force. This may also lower the target's Sp. Def stat.");
            super.power = 90;
            super.accuracy = 100;
            super.effectChance = 10;
            super.statChanges[Stat.SP_DEFENSE.index()] = -1;
        }
    }

    static class MudSlap extends Attack {
        private static final long serialVersionUID = 1L;

        MudSlap() {
            super(AttackNamesies.MUD_SLAP, Type.GROUND, MoveCategory.SPECIAL, 10, "The user hurls mud in the target's face to inflict damage and lower its accuracy.");
            super.power = 20;
            super.accuracy = 100;
            super.statChanges[Stat.ACCURACY.index()] = -1;
        }
    }

    static class Magnitude extends Attack implements PowerChangeEffect, SemiInvulnerableBypasser {
        private static final long serialVersionUID = 1L;

        private static final int[] CHANCES = { 5, 10, 20, 30, 20, 10, 5 };
        private static final int[] POWERS = { 10, 30, 50, 70, 90, 110, 150 };

        private int index;

        Magnitude() {
            super(AttackNamesies.MAGNITUDE, Type.GROUND, MoveCategory.PHYSICAL, 30, "The user attacks everything around it with a ground-shaking quake. Its power varies.");
            super.accuracy = 100;
        }

        @Override
        public void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            index = GeneralUtils.getPercentageIndex(CHANCES);
        }

        @Override
        public int getPower(Battle b, ActivePokemon me, ActivePokemon o) {
            return POWERS[index];
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            Messages.add("Magnitude " + (index + 4) + "!");
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            double multiplier = 1;

            // Power is halved during Grassy Terrain
            if (b.hasEffect(TerrainNamesies.GRASSY_TERRAIN)) {
                multiplier *= .5;
            }

            // Power is doubled when the opponent is underground
            if (victim.isSemiInvulnerableDigging()) {
                multiplier *= 2;
            }

            return multiplier;
        }

        @Override
        public boolean semiInvulnerableBypass(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Always hit when the opponent is underground
            return defending.isSemiInvulnerableDigging();
        }
    }

    // Power is halved during Grassy Terrain
    static class Bulldoze extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Bulldoze() {
            super(AttackNamesies.BULLDOZE, Type.GROUND, MoveCategory.PHYSICAL, 20, "The user strikes everything around it by stomping down on the ground. This lowers the Speed stat of those hit.");
            super.power = 60;
            super.accuracy = 100;
            super.statChanges[Stat.SPEED.index()] = -1;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return b.hasEffect(TerrainNamesies.GRASSY_TERRAIN) ? .5 : 1;
        }
    }

    static class Dig extends Attack implements SemiInvulnerableMove {
        private static final long serialVersionUID = 1L;

        private boolean isCharging;

        Dig() {
            super(AttackNamesies.DIG, Type.GROUND, MoveCategory.PHYSICAL, 10, "The user burrows, then attacks on the next turn.");
            super.power = 80;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            this.resetReady();
        }

        @Override
        public boolean isOverground() {
            return false;
        }

        @Override
        public String getChargeMessage(ActivePokemon user) {
            return user.getName() + " went underground!";
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void resetReady() {
            this.isCharging = !this.chargesFirst();
        }

        @Override
        public void switchReady() {
            this.isCharging = !this.isCharging;
        }
    }

    static class Earthquake extends Attack implements PowerChangeEffect, SemiInvulnerableBypasser {
        private static final long serialVersionUID = 1L;

        Earthquake() {
            super(AttackNamesies.EARTHQUAKE, Type.GROUND, MoveCategory.PHYSICAL, 10, "The user sets off an earthquake that strikes every Pok\u00e9mon around it.");
            super.power = 100;
            super.accuracy = 100;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            double multiplier = 1;

            // Power is halved during Grassy Terrain
            if (b.hasEffect(TerrainNamesies.GRASSY_TERRAIN)) {
                multiplier *= .5;
            }

            // Power is doubled when the opponent is underground
            if (victim.isSemiInvulnerableDigging()) {
                multiplier *= 2;
            }

            return multiplier;
        }

        @Override
        public boolean semiInvulnerableBypass(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Always hit when the opponent is underground
            return defending.isSemiInvulnerableDigging();
        }
    }

    static class Fissure extends Attack implements OhkoMove, SemiInvulnerableBypasser {
        private static final long serialVersionUID = 1L;

        Fissure() {
            super(AttackNamesies.FISSURE, Type.GROUND, MoveCategory.PHYSICAL, 5, "The user opens up a fissure in the ground and drops the target in. The target faints instantly if this attack hits.");
            super.accuracy = 30;
        }

        @Override
        public boolean semiInvulnerableBypass(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Always hit when the opponent is underground
            return defending.isSemiInvulnerableDigging();
        }
    }

    static class NightSlash extends Attack implements CritStageEffect {
        private static final long serialVersionUID = 1L;

        NightSlash() {
            super(AttackNamesies.NIGHT_SLASH, Type.DARK, MoveCategory.PHYSICAL, 15, "The user slashes the target the instant an opportunity arises. Critical hits land more easily.");
            super.power = 70;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class TriAttack extends Attack {
        private static final long serialVersionUID = 1L;

        private static final StatusNamesies[] statusConditions = {
                StatusNamesies.PARALYZED,
                StatusNamesies.BURNED,
                StatusNamesies.FROZEN
        };

        TriAttack() {
            super(AttackNamesies.TRI_ATTACK, Type.NORMAL, MoveCategory.SPECIAL, 10, "The user strikes with a simultaneous three-beam attack. May also burn, freeze, or paralyze the target.");
            super.power = 80;
            super.accuracy = 100;
            super.effectChance = 20;
        }

        @Override
        public void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            super.status = RandomUtils.getRandomValue(statusConditions);
        }
    }

    static class FakeOut extends Attack {
        private static final long serialVersionUID = 1L;

        FakeOut() {
            super(AttackNamesies.FAKE_OUT, Type.NORMAL, MoveCategory.PHYSICAL, 10, "This attack hits first and makes the target flinch. It only works the first turn the user is in battle.");
            super.power = 40;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.priority = 3;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isFirstTurn();
        }
    }

    static class FeintAttack extends Attack {
        private static final long serialVersionUID = 1L;

        FeintAttack() {
            super(AttackNamesies.FEINT_ATTACK, Type.DARK, MoveCategory.PHYSICAL, 20, "The user approaches the target disarmingly, then throws a sucker punch. This attack never misses.");
            super.power = 60;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Taunt extends Attack {
        private static final long serialVersionUID = 1L;

        Taunt() {
            super(AttackNamesies.TAUNT, Type.DARK, MoveCategory.STATUS, 20, "The target is taunted into a rage that allows it to use only attack moves for three turns.");
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.TAUNT;
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
        }
    }

    static class PayDay extends Attack {
        private static final long serialVersionUID = 1L;

        PayDay() {
            super(AttackNamesies.PAY_DAY, Type.NORMAL, MoveCategory.PHYSICAL, 20, "Numerous coins are hurled at the target to inflict damage. Money is earned after the battle.");
            super.power = 40;
            super.accuracy = 100;
            super.effect = TeamEffectNamesies.PAY_DAY;
            super.selfTarget = true;
        }
    }

    static class PowerGem extends Attack {
        private static final long serialVersionUID = 1L;

        PowerGem() {
            super(AttackNamesies.POWER_GEM, Type.ROCK, MoveCategory.SPECIAL, 20, "The user attacks with a ray of light that sparkles as if it were made of gemstones.");
            super.power = 80;
            super.accuracy = 100;
        }
    }

    static class WaterSport extends Attack {
        private static final long serialVersionUID = 1L;

        WaterSport() {
            super(AttackNamesies.WATER_SPORT, Type.WATER, MoveCategory.STATUS, 15, "The user soaks the battlefield with water. This weakens Fire-type moves for five turns.");
            super.effect = StandardBattleEffectNamesies.WATER_SPORT;
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.FIELD);
        }
    }

    static class Soak extends Attack implements ChangeTypeSource {
        private static final long serialVersionUID = 1L;

        Soak() {
            super(AttackNamesies.SOAK, Type.WATER, MoveCategory.STATUS, 20, "The user shoots a torrent of water at the target and changes the target's type to Water.");
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.CHANGE_TYPE;
        }

        @Override
        public PokeType getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return new PokeType(Type.WATER);
        }
    }

    static class TrickOrTreat extends Attack implements ChangeTypeSource {
        private static final long serialVersionUID = 1L;

        TrickOrTreat() {
            super(AttackNamesies.TRICK_OR_TREAT, Type.GHOST, MoveCategory.STATUS, 20, "The user takes the target trick-or-treating. This adds Ghost type to the target's type.");
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.CHANGE_TYPE;
        }

        @Override
        public PokeType getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
            Type primary = victim.getType(b).getFirstType();
            return new PokeType(primary, primary == Type.GHOST ? Type.NO_TYPE : Type.GHOST);
        }
    }

    static class ForestsCurse extends Attack implements ChangeTypeSource {
        private static final long serialVersionUID = 1L;

        ForestsCurse() {
            super(AttackNamesies.FORESTS_CURSE, Type.GRASS, MoveCategory.STATUS, 20, "The user puts a forest curse on the target. The target is now Grass type as well.");
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.CHANGE_TYPE;
        }

        @Override
        public PokeType getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
            Type primary = victim.getType(b).getFirstType();
            return new PokeType(primary, primary == Type.GRASS ? Type.NO_TYPE : Type.GRASS);
        }
    }

    static class PsychUp extends Attack {
        private static final long serialVersionUID = 1L;

        PsychUp() {
            super(AttackNamesies.PSYCH_UP, Type.NORMAL, MoveCategory.STATUS, 10, "The user hypnotizes itself into copying any stat change made by the target.");
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            for (Stat stat : Stat.BATTLE_STATS) {
                user.getStages().setStage(stat, victim.getStage(stat));
            }

            Messages.add(user.getName() + " copied " + victim.getName() + "'s stat changes!");
        }
    }

    static class Amnesia extends Attack {
        private static final long serialVersionUID = 1L;

        Amnesia() {
            super(AttackNamesies.AMNESIA, Type.PSYCHIC, MoveCategory.STATUS, 20, "The user temporarily empties its mind to forget its concerns. This sharply raises the user's Sp. Def stat.");
            super.selfTarget = true;
            super.statChanges[Stat.SP_DEFENSE.index()] = 2;
        }
    }

    static class WonderRoom extends Attack {
        private static final long serialVersionUID = 1L;

        WonderRoom() {
            super(AttackNamesies.WONDER_ROOM, Type.PSYCHIC, MoveCategory.STATUS, 10, "The user creates a bizarre area in which Pok\u00e9mon's Defense and Sp. Def stats are swapped for five turns.");
            super.effect = StandardBattleEffectNamesies.WONDER_ROOM;
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.FIELD);
        }
    }

    static class AquaJet extends Attack {
        private static final long serialVersionUID = 1L;

        AquaJet() {
            super(AttackNamesies.AQUA_JET, Type.WATER, MoveCategory.PHYSICAL, 20, "The user lunges at the target at a speed that makes it almost invisible. This move always goes first.");
            super.power = 40;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.priority = 1;
        }
    }

    static class Covet extends Attack implements ItemSwapperEffect {
        private static final long serialVersionUID = 1L;

        Covet() {
            super(AttackNamesies.COVET, Type.NORMAL, MoveCategory.PHYSICAL, 25, "The user endearingly approaches the target, then steals the target's held item.");
            super.power = 60;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.canStealItem(b, victim)) {
                this.swapItems(b, user, victim);
            }
        }

        @Override
        public String getSwitchMessage(ActivePokemon user, HoldItem userItem, ActivePokemon victim, HoldItem victimItem) {
            return user.getName() + " stole " + victim.getName() + "'s " + victimItem.getName() + "!";
        }
    }

    static class LowKick extends Attack {
        private static final long serialVersionUID = 1L;

        LowKick() {
            super(AttackNamesies.LOW_KICK, Type.FIGHTING, MoveCategory.PHYSICAL, 20, "A powerful low kick that makes the target fall over. The heavier the target, the greater the move's power.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getPower(Battle b, ActivePokemon me, ActivePokemon o) {
            double weight = o.getWeight(b);
            if (weight < 22) {
                return 20;
            } else if (weight < 55) {
                return 40;
            } else if (weight < 110) {
                return 60;
            } else if (weight < 220) {
                return 80;
            } else if (weight < 440) {
                return 100;
            } else {
                return 120;
            }
        }
    }

    static class KarateChop extends Attack implements CritStageEffect {
        private static final long serialVersionUID = 1L;

        KarateChop() {
            super(AttackNamesies.KARATE_CHOP, Type.FIGHTING, MoveCategory.PHYSICAL, 25, "The target is attacked with a sharp chop. Critical hits land more easily.");
            super.power = 50;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class SeismicToss extends Attack implements FixedDamageMove {
        private static final long serialVersionUID = 1L;

        SeismicToss() {
            super(AttackNamesies.SEISMIC_TOSS, Type.FIGHTING, MoveCategory.PHYSICAL, 20, "The target is thrown using the power of gravity. It inflicts damage equal to the user's level.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getFixedDamageAmount(ActivePokemon me, ActivePokemon o) {
            return me.getLevel();
        }
    }

    static class Swagger extends Attack {
        private static final long serialVersionUID = 1L;

        Swagger() {
            super(AttackNamesies.SWAGGER, Type.NORMAL, MoveCategory.STATUS, 15, "The user enrages and confuses the target. However, this also sharply raises the target's Attack stat.");
            super.accuracy = 85;
            super.effect = PokemonEffectNamesies.CONFUSION;
            super.statChanges[Stat.ATTACK.index()] = 2;
        }
    }

    static class CrossChop extends Attack implements CritStageEffect {
        private static final long serialVersionUID = 1L;

        CrossChop() {
            super(AttackNamesies.CROSS_CHOP, Type.FIGHTING, MoveCategory.PHYSICAL, 5, "The user delivers a double chop with its forearms crossed. Critical hits land more easily.");
            super.power = 100;
            super.accuracy = 80;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Punishment extends Attack {
        private static final long serialVersionUID = 1L;

        Punishment() {
            super(AttackNamesies.PUNISHMENT, Type.DARK, MoveCategory.PHYSICAL, 5, "The more the target has powered up with stat changes, the greater the move's power.");
            super.power = 60;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public String getPowerString() {
            return "--";
        }

        @Override
        public int getPower(Battle b, ActivePokemon me, ActivePokemon o) {
            return Math.min(super.power + 20*o.getStages().totalStatIncreases(), 200);
        }
    }

    static class CloseCombat extends Attack {
        private static final long serialVersionUID = 1L;

        CloseCombat() {
            super(AttackNamesies.CLOSE_COMBAT, Type.FIGHTING, MoveCategory.PHYSICAL, 5, "The user fights the target up close without guarding itself. This also lowers the user's Defense and Sp. Def stats.");
            super.power = 120;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.selfTarget = true;
            super.statChanges[Stat.DEFENSE.index()] = -1;
            super.statChanges[Stat.SP_DEFENSE.index()] = -1;
        }
    }

    static class DragonAscent extends Attack {
        private static final long serialVersionUID = 1L;

        DragonAscent() {
            super(AttackNamesies.DRAGON_ASCENT, Type.FLYING, MoveCategory.PHYSICAL, 5, "After soaring upward, the user attacks its target by dropping out of the sky at high speeds. But it lowers its own Defense and Sp. Def stats in the process.");
            super.power = 120;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.selfTarget = true;
            super.statChanges[Stat.DEFENSE.index()] = -1;
            super.statChanges[Stat.SP_DEFENSE.index()] = -1;
        }
    }

    static class FlameWheel extends Attack {
        private static final long serialVersionUID = 1L;

        FlameWheel() {
            super(AttackNamesies.FLAME_WHEEL, Type.FIRE, MoveCategory.PHYSICAL, 25, "The user cloaks itself in fire and charges at the target. This may also leave the target with a burn.");
            super.power = 60;
            super.accuracy = 100;
            super.effectChance = 10;
            super.status = StatusNamesies.BURNED;
            super.moveTypes.add(MoveType.DEFROST);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Reversal extends Attack {
        private static final long serialVersionUID = 1L;

        Reversal() {
            super(AttackNamesies.REVERSAL, Type.FIGHTING, MoveCategory.PHYSICAL, 15, "An all-out attack that becomes more powerful the less HP the user has.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getPower(Battle b, ActivePokemon me, ActivePokemon o) {
            double ratio = me.getHPRatio();
            if (ratio > .7) {
                return 20;
            } else if (ratio > .35) {
                return 40;
            } else if (ratio > .2) {
                return 80;
            } else if (ratio > .1) {
                return 100;
            } else if (ratio > .04) {
                return 150;
            } else {
                return 200;
            }
        }
    }

    static class ExtremeSpeed extends Attack {
        private static final long serialVersionUID = 1L;

        ExtremeSpeed() {
            super(AttackNamesies.EXTREME_SPEED, Type.NORMAL, MoveCategory.PHYSICAL, 5, "The user charges the target at blinding speed. This move always goes first.");
            super.power = 80;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.priority = 2;
        }
    }

    static class Hypnosis extends Attack {
        private static final long serialVersionUID = 1L;

        Hypnosis() {
            super(AttackNamesies.HYPNOSIS, Type.PSYCHIC, MoveCategory.STATUS, 20, "The user employs hypnotic suggestion to make the target fall into a deep sleep.");
            super.accuracy = 60;
            super.status = StatusNamesies.ASLEEP;
        }
    }

    static class BubbleBeam extends Attack {
        private static final long serialVersionUID = 1L;

        BubbleBeam() {
            super(AttackNamesies.BUBBLE_BEAM, Type.WATER, MoveCategory.SPECIAL, 20, "A spray of bubbles is forcefully ejected at the target. This may also lower its Speed stat.");
            super.power = 65;
            super.accuracy = 100;
            super.effectChance = 10;
            super.statChanges[Stat.SPEED.index()] = -1;
        }
    }

    static class MudShot extends Attack {
        private static final long serialVersionUID = 1L;

        MudShot() {
            super(AttackNamesies.MUD_SHOT, Type.GROUND, MoveCategory.SPECIAL, 15, "The user attacks by hurling a blob of mud at the target. This also lowers the target's Speed stat.");
            super.power = 55;
            super.accuracy = 95;
            super.statChanges[Stat.SPEED.index()] = -1;
        }
    }

    static class BellyDrum extends Attack {
        private static final long serialVersionUID = 1L;

        BellyDrum() {
            super(AttackNamesies.BELLY_DRUM, Type.NORMAL, MoveCategory.STATUS, 10, "The user maximizes its Attack stat in exchange for HP equal to half its max HP.");
            super.selfTarget = true;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Fails if attack is already maxed or if you have less than half your health to give up
            return user.getStage(Stat.ATTACK) < Stat.MAX_STAT_CHANGES && user.getHPRatio() > .5;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Maximization station
            user.getStages().modifyStage(
                    user, 2*Stat.MAX_STAT_CHANGES, Stat.ATTACK, b, CastSource.ATTACK,
                    (victimName, statName, changed) -> user.getName() + " cut its own HP and maximized " + victimName + " " + statName + "!"
            );
            user.forceReduceHealthFraction(b, 1/2.0, "");
        }
    }

    static class Submission extends Attack implements RecoilPercentageMove {
        private static final long serialVersionUID = 1L;

        Submission() {
            super(AttackNamesies.SUBMISSION, Type.FIGHTING, MoveCategory.PHYSICAL, 20, "The user grabs the target and recklessly dives for the ground. This also damages the user a little.");
            super.power = 80;
            super.accuracy = 80;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getDamagePercentageDenominator() {
            return 4;
        }
    }

    static class DynamicPunch extends Attack {
        private static final long serialVersionUID = 1L;

        DynamicPunch() {
            super(AttackNamesies.DYNAMIC_PUNCH, Type.FIGHTING, MoveCategory.PHYSICAL, 5, "The user punches the target with full, concentrated power. This confuses the target if it hits.");
            super.power = 100;
            super.accuracy = 50;
            super.effect = PokemonEffectNamesies.CONFUSION;
            super.moveTypes.add(MoveType.PUNCHING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class MindReader extends Attack {
        private static final long serialVersionUID = 1L;

        MindReader() {
            super(AttackNamesies.MIND_READER, Type.NORMAL, MoveCategory.STATUS, 5, "The user senses the target's movements with its mind to ensure its next attack does not miss the target.");
            super.effect = PokemonEffectNamesies.LOCK_ON;
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.selfTarget = true;
        }
    }

    static class LockOn extends Attack {
        private static final long serialVersionUID = 1L;

        LockOn() {
            super(AttackNamesies.LOCK_ON, Type.NORMAL, MoveCategory.STATUS, 5, "The user takes sure aim at the target. This ensures the next attack does not miss the target.");
            super.effect = PokemonEffectNamesies.LOCK_ON;
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.selfTarget = true;
        }
    }

    static class Kinesis extends Attack {
        private static final long serialVersionUID = 1L;

        Kinesis() {
            super(AttackNamesies.KINESIS, Type.PSYCHIC, MoveCategory.STATUS, 15, "The user distracts the target by bending a spoon. This lowers the target's accuracy.");
            super.accuracy = 80;
            super.statChanges[Stat.ACCURACY.index()] = -1;
        }
    }

    static class Barrier extends Attack {
        private static final long serialVersionUID = 1L;

        Barrier() {
            super(AttackNamesies.BARRIER, Type.PSYCHIC, MoveCategory.STATUS, 20, "The user throws up a sturdy wall that sharply raises its Defense stat.");
            super.selfTarget = true;
            super.statChanges[Stat.DEFENSE.index()] = 2;
        }
    }

    static class Telekinesis extends Attack {
        private static final long serialVersionUID = 1L;

        Telekinesis() {
            super(AttackNamesies.TELEKINESIS, Type.PSYCHIC, MoveCategory.STATUS, 15, "The user makes the target float with its psychic power. The target is easier to hit for three turns.");
            super.effect = PokemonEffectNamesies.TELEKINESIS;
            super.moveTypes.add(MoveType.AIRBORNE);
        }
    }

    static class Ingrain extends Attack {
        private static final long serialVersionUID = 1L;

        Ingrain() {
            super(AttackNamesies.INGRAIN, Type.GRASS, MoveCategory.STATUS, 20, "The user lays roots that restore its HP on every turn. Because it's rooted, it can't switch out.");
            super.effect = PokemonEffectNamesies.INGRAIN;
            super.selfTarget = true;
        }
    }

    static class PsychoCut extends Attack implements CritStageEffect {
        private static final long serialVersionUID = 1L;

        PsychoCut() {
            super(AttackNamesies.PSYCHO_CUT, Type.PSYCHIC, MoveCategory.PHYSICAL, 20, "The user tears at the target with blades formed by psychic power. Critical hits land more easily.");
            super.power = 70;
            super.accuracy = 100;
        }
    }

    static class FutureSight extends Attack {
        private static final long serialVersionUID = 1L;

        FutureSight() {
            super(AttackNamesies.FUTURE_SIGHT, Type.PSYCHIC, MoveCategory.SPECIAL, 10, "Two turns after this move is used, a hunk of psychic energy attacks the target.");
            super.power = 120;
            super.accuracy = 100;
            super.effect = TeamEffectNamesies.FUTURE_SIGHT;
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
            super.moveTypes.add(MoveType.MIRRORLESS);
        }

        @Override
        public boolean shouldApplyDamage(Battle b, ActivePokemon user) {
            // Don't apply damage just yet!!
            return false;
        }

        @Override
        public boolean canPrintFail() {
            return true;
        }
    }

    static class DoomDesire extends Attack {
        private static final long serialVersionUID = 1L;

        DoomDesire() {
            super(AttackNamesies.DOOM_DESIRE, Type.STEEL, MoveCategory.SPECIAL, 5, "Two turns after this move is used, a concentrated bundle of light blasts the target.");
            super.power = 140;
            super.accuracy = 100;
            super.effect = TeamEffectNamesies.DOOM_DESIRE;
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
            super.moveTypes.add(MoveType.MIRRORLESS);
        }

        @Override
        public boolean shouldApplyDamage(Battle b, ActivePokemon user) {
            // Don't apply damage just yet!!
            return false;
        }

        @Override
        public boolean canPrintFail() {
            return true;
        }
    }

    static class CalmMind extends Attack {
        private static final long serialVersionUID = 1L;

        CalmMind() {
            super(AttackNamesies.CALM_MIND, Type.PSYCHIC, MoveCategory.STATUS, 20, "The user quietly focuses its mind and calms its spirit to raise its Sp. Atk and Sp. Def stats.");
            super.selfTarget = true;
            super.statChanges[Stat.SP_ATTACK.index()] = 1;
            super.statChanges[Stat.SP_DEFENSE.index()] = 1;
        }
    }

    static class LowSweep extends Attack {
        private static final long serialVersionUID = 1L;

        LowSweep() {
            super(AttackNamesies.LOW_SWEEP, Type.FIGHTING, MoveCategory.PHYSICAL, 20, "The user makes a swift attack on the target's legs, which lowers the target's Speed stat.");
            super.power = 65;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.statChanges[Stat.SPEED.index()] = -1;
        }
    }

    static class Revenge extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Revenge() {
            super(AttackNamesies.REVENGE, Type.FIGHTING, MoveCategory.PHYSICAL, 10, "This attack move's power is doubled if the user has been hurt by the opponent in the same turn.");
            super.power = 60;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.priority = -4;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.hasTakenDamage() ? 2 : 1;
        }
    }

    static class VitalThrow extends Attack {
        private static final long serialVersionUID = 1L;

        VitalThrow() {
            super(AttackNamesies.VITAL_THROW, Type.FIGHTING, MoveCategory.PHYSICAL, 10, "The user attacks last. In return, this throw move never misses.");
            super.power = 70;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.priority = -1;
        }
    }

    static class WringOut extends Attack {
        private static final long serialVersionUID = 1L;

        WringOut() {
            super(AttackNamesies.WRING_OUT, Type.NORMAL, MoveCategory.SPECIAL, 5, "The user powerfully wrings the target. The more HP the target has, the greater the move's power.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getPower(Battle b, ActivePokemon me, ActivePokemon o) {
            return (int)Math.min(1, (120*o.getHPRatio()));
        }
    }

    static class LeafTornado extends Attack {
        private static final long serialVersionUID = 1L;

        LeafTornado() {
            super(AttackNamesies.LEAF_TORNADO, Type.GRASS, MoveCategory.SPECIAL, 10, "The user attacks its target by encircling it in sharp leaves. This attack may also lower the target's accuracy.");
            super.power = 65;
            super.accuracy = 90;
            super.effectChance = 50;
            super.statChanges[Stat.ACCURACY.index()] = -1;
        }
    }

    static class LeafStorm extends Attack {
        private static final long serialVersionUID = 1L;

        LeafStorm() {
            super(AttackNamesies.LEAF_STORM, Type.GRASS, MoveCategory.SPECIAL, 5, "The user whips up a storm of leaves around the target. The attack's recoil harshly lowers the user's Sp. Atk stat.");
            super.power = 130;
            super.accuracy = 90;
            super.selfTarget = true;
            super.statChanges[Stat.SP_ATTACK.index()] = -2;
        }
    }

    static class LeafBlade extends Attack implements CritStageEffect {
        private static final long serialVersionUID = 1L;

        LeafBlade() {
            super(AttackNamesies.LEAF_BLADE, Type.GRASS, MoveCategory.PHYSICAL, 15, "The user handles a sharp leaf like a sword and attacks by cutting its target. Critical hits land more easily.");
            super.power = 90;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Constrict extends Attack {
        private static final long serialVersionUID = 1L;

        Constrict() {
            super(AttackNamesies.CONSTRICT, Type.NORMAL, MoveCategory.PHYSICAL, 35, "The target is attacked with long, creeping tentacles, vines, or the like. This may also lower the target's Speed stat.");
            super.power = 10;
            super.accuracy = 100;
            super.effectChance = 10;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.statChanges[Stat.SPEED.index()] = -1;
        }
    }

    static class Hex extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Hex() {
            super(AttackNamesies.HEX, Type.GHOST, MoveCategory.SPECIAL, 10, "This relentless attack does massive damage to a target affected by status conditions.");
            super.power = 65;
            super.accuracy = 100;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.hasStatus() ? 2 : 1;
        }
    }

    static class SludgeWave extends Attack {
        private static final long serialVersionUID = 1L;

        SludgeWave() {
            super(AttackNamesies.SLUDGE_WAVE, Type.POISON, MoveCategory.SPECIAL, 10, "The user strikes everything around it by swamping the area with a giant sludge wave. This may also poison those hit.");
            super.power = 95;
            super.accuracy = 100;
            super.effectChance = 10;
            super.status = StatusNamesies.POISONED;
        }
    }

    static class MudSport extends Attack {
        private static final long serialVersionUID = 1L;

        MudSport() {
            super(AttackNamesies.MUD_SPORT, Type.GROUND, MoveCategory.STATUS, 15, "The user kicks up mud on the battlefield. This weakens Electric-type moves for five turns.");
            super.effect = StandardBattleEffectNamesies.MUD_SPORT;
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.FIELD);
        }
    }

    static class RockPolish extends Attack {
        private static final long serialVersionUID = 1L;

        RockPolish() {
            super(AttackNamesies.ROCK_POLISH, Type.ROCK, MoveCategory.STATUS, 20, "The user polishes its body to reduce drag. This can sharply raise the Speed stat.");
            super.selfTarget = true;
            super.statChanges[Stat.SPEED.index()] = 2;
        }
    }

    static class RockThrow extends Attack {
        private static final long serialVersionUID = 1L;

        RockThrow() {
            super(AttackNamesies.ROCK_THROW, Type.ROCK, MoveCategory.PHYSICAL, 15, "The user picks up and throws a small rock at the target to attack.");
            super.power = 50;
            super.accuracy = 90;
        }
    }

    static class RockBlast extends Attack implements MultiStrikeMove {
        private static final long serialVersionUID = 1L;

        RockBlast() {
            super(AttackNamesies.ROCK_BLAST, Type.ROCK, MoveCategory.PHYSICAL, 10, "The user hurls hard rocks at the target. Two to five rocks are launched in a row.");
            super.power = 25;
            super.accuracy = 90;
            super.moveTypes.add(MoveType.BOMB_BALL);
        }

        @Override
        public int getMinHits() {
            return 2;
        }

        @Override
        public int getMaxHits() {
            return 5;
        }
    }

    // Twice as strong when the opponent is flying
    static class SmackDown extends Attack implements SemiInvulnerableBypasser, PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        SmackDown() {
            super(AttackNamesies.SMACK_DOWN, Type.ROCK, MoveCategory.PHYSICAL, 15, "The user throws a stone or similar projectile to attack an opponent. A flying Pok\u00e9mon will fall to the ground when it's hit.");
            super.power = 50;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.GROUNDED;
        }

        @Override
        public boolean semiInvulnerableBypass(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Always hit when the opponent is flying
            return defending.isSemiInvulnerableFlying();
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.isSemiInvulnerableFlying() ? 2 : 1;
        }
    }

    static class StealthRock extends Attack {
        private static final long serialVersionUID = 1L;

        StealthRock() {
            super(AttackNamesies.STEALTH_ROCK, Type.ROCK, MoveCategory.STATUS, 20, "The user lays a trap of levitating stones around the opposing team. The trap hurts opposing Pok\u00e9mon that switch into battle.");
            super.effect = TeamEffectNamesies.STEALTH_ROCK;
            super.moveTypes.add(MoveType.FIELD);
        }
    }

    static class StoneEdge extends Attack implements CritStageEffect {
        private static final long serialVersionUID = 1L;

        StoneEdge() {
            super(AttackNamesies.STONE_EDGE, Type.ROCK, MoveCategory.PHYSICAL, 5, "The user stabs the target from below with sharpened stones. Critical hits land more easily.");
            super.power = 100;
            super.accuracy = 80;
        }
    }

    static class Steamroller extends Attack implements DoubleMinimizerMove {
        private static final long serialVersionUID = 1L;

        Steamroller() {
            super(AttackNamesies.STEAMROLLER, Type.BUG, MoveCategory.PHYSICAL, 20, "The user crushes its target by rolling over the target with its rolled-up body. This may also make the target flinch.");
            super.power = 65;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 30;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class HeavySlam extends Attack implements DoubleMinimizerMove {
        private static final long serialVersionUID = 1L;

        HeavySlam() {
            super(AttackNamesies.HEAVY_SLAM, Type.STEEL, MoveCategory.PHYSICAL, 10, "The user slams into the target with its heavy body. The more the user outweighs the target, the greater the move's power.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getPower(Battle b, ActivePokemon me, ActivePokemon o) {
            double ratio = o.getWeight(b)/me.getWeight(b);
            if (ratio > .5) {
                return 40;
            } else if (ratio > .33) {
                return 60;
            } else if (ratio > .25) {
                return 80;
            } else if (ratio > .2) {
                return 100;
            } else {
                return 120;
            }
        }
    }

    static class Stomp extends Attack implements DoubleMinimizerMove {
        private static final long serialVersionUID = 1L;

        Stomp() {
            super(AttackNamesies.STOMP, Type.NORMAL, MoveCategory.PHYSICAL, 20, "The target is stomped with a big foot. This may also make the target flinch.");
            super.power = 65;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 30;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class FlameCharge extends Attack {
        private static final long serialVersionUID = 1L;

        FlameCharge() {
            super(AttackNamesies.FLAME_CHARGE, Type.FIRE, MoveCategory.PHYSICAL, 20, "Cloaking itself in flame, the user attacks. Then, building up more power, the user raises its Speed stat.");
            super.power = 50;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.selfTarget = true;
            super.statChanges[Stat.SPEED.index()] = 1;
        }
    }

    static class Bounce extends Attack implements SemiInvulnerableMove {
        private static final long serialVersionUID = 1L;

        private boolean isCharging;

        Bounce() {
            super(AttackNamesies.BOUNCE, Type.FLYING, MoveCategory.PHYSICAL, 5, "The user bounces up high, then drops on the target on the second turn. This may also leave the target with paralysis.");
            super.power = 85;
            super.accuracy = 85;
            super.effectChance = 30;
            super.status = StatusNamesies.PARALYZED;
            super.moveTypes.add(MoveType.AIRBORNE);
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            this.resetReady();
        }

        @Override
        public boolean isOverground() {
            return true;
        }

        @Override
        public String getChargeMessage(ActivePokemon user) {
            return user.getName() + " sprang up!";
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void resetReady() {
            this.isCharging = !this.chargesFirst();
        }

        @Override
        public void switchReady() {
            this.isCharging = !this.isCharging;
        }
    }

    static class Curse extends Attack {
        private static final long serialVersionUID = 1L;

        Curse() {
            super(AttackNamesies.CURSE, Type.GHOST, MoveCategory.STATUS, 10, "A move that works differently for the Ghost type than for all other types.");
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.MIRRORLESS);
        }

        @Override
        public void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            super.statChanges = new int[Stat.NUM_BATTLE_STATS];
            super.effect = null;

            // Different effects based on the type of the user
            if (attacking.isType(b, Type.GHOST)) {
                super.effect = PokemonEffectNamesies.CURSE;
                super.selfTarget = false;
            } else {
                super.statChanges[Stat.ATTACK.index()] = 1;
                super.statChanges[Stat.DEFENSE.index()] = 1;
                super.statChanges[Stat.SPEED.index()] = -1;
                super.selfTarget = true;
            }
        }
    }

    static class Yawn extends Attack {
        private static final long serialVersionUID = 1L;

        Yawn() {
            super(AttackNamesies.YAWN, Type.NORMAL, MoveCategory.STATUS, 10, "The user lets loose a huge yawn that lulls the target into falling asleep on the next turn.");
            super.effect = PokemonEffectNamesies.YAWN;
        }
    }

    static class Headbutt extends Attack {
        private static final long serialVersionUID = 1L;

        Headbutt() {
            super(AttackNamesies.HEADBUTT, Type.NORMAL, MoveCategory.PHYSICAL, 15, "The user sticks out its head and attacks by charging straight into the target. This may also make the target flinch.");
            super.power = 70;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 30;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class SlackOff extends Attack implements SelfHealingMove {
        private static final long serialVersionUID = 1L;

        SlackOff() {
            super(AttackNamesies.SLACK_OFF, Type.NORMAL, MoveCategory.STATUS, 10, "The user slacks off, restoring its own HP by up to half of its max HP.");
            super.moveTypes.add(MoveType.HEALING);
            super.selfTarget = true;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            this.heal(b, victim);
        }

        @Override
        public double getHealFraction(Battle b, ActivePokemon victim) {
            return .5;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.canHeal();
        }
    }

    static class HealPulse extends Attack {
        private static final long serialVersionUID = 1L;

        HealPulse() {
            super(AttackNamesies.HEAL_PULSE, Type.PSYCHIC, MoveCategory.STATUS, 10, "The user emits a healing pulse that restores the target's HP by up to half of its max HP.");
            super.moveTypes.add(MoveType.AURA_PULSE);
            super.moveTypes.add(MoveType.HEALING);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Heal by 50% unless the user has Mega Launcher -- then heal by 75%
            double fraction = user.hasAbility(AbilityNamesies.MEGA_LAUNCHER) ? .75 : .5;
            victim.healHealthFraction(fraction, b, victim.getName() + "'s health was restored!");
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.canHeal();
        }
    }

    static class MetalSound extends Attack {
        private static final long serialVersionUID = 1L;

        MetalSound() {
            super(AttackNamesies.METAL_SOUND, Type.STEEL, MoveCategory.STATUS, 40, "A horrible sound like scraping metal harshly lowers the target's Sp. Def stat.");
            super.accuracy = 85;
            super.moveTypes.add(MoveType.SOUND_BASED);
            super.statChanges[Stat.SP_DEFENSE.index()] = -2;
        }
    }

    static class Spark extends Attack {
        private static final long serialVersionUID = 1L;

        Spark() {
            super(AttackNamesies.SPARK, Type.ELECTRIC, MoveCategory.PHYSICAL, 20, "The user throws an electrically charged tackle at the target. This may also leave the target with paralysis.");
            super.power = 65;
            super.accuracy = 100;
            super.effectChance = 30;
            super.status = StatusNamesies.PARALYZED;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class MagnetBomb extends Attack {
        private static final long serialVersionUID = 1L;

        MagnetBomb() {
            super(AttackNamesies.MAGNET_BOMB, Type.STEEL, MoveCategory.PHYSICAL, 20, "The user launches steel bombs that stick to the target. This attack never misses.");
            super.power = 60;
            super.moveTypes.add(MoveType.BOMB_BALL);
        }
    }

    static class MirrorShot extends Attack {
        private static final long serialVersionUID = 1L;

        MirrorShot() {
            super(AttackNamesies.MIRROR_SHOT, Type.STEEL, MoveCategory.SPECIAL, 10, "The user lets loose a flash of energy at the target from its polished body. This may also lower the target's accuracy.");
            super.power = 65;
            super.accuracy = 85;
            super.effectChance = 30;
            super.statChanges[Stat.ACCURACY.index()] = -1;
        }
    }

    static class MagnetRise extends Attack {
        private static final long serialVersionUID = 1L;

        MagnetRise() {
            super(AttackNamesies.MAGNET_RISE, Type.ELECTRIC, MoveCategory.STATUS, 10, "The user levitates using electrically generated magnetism for five turns.");
            super.effect = PokemonEffectNamesies.MAGNET_RISE;
            super.moveTypes.add(MoveType.AIRBORNE);
            super.selfTarget = true;
        }
    }

    static class ZapCannon extends Attack {
        private static final long serialVersionUID = 1L;

        ZapCannon() {
            super(AttackNamesies.ZAP_CANNON, Type.ELECTRIC, MoveCategory.SPECIAL, 5, "The user fires an electric blast like a cannon to inflict damage and cause paralysis.");
            super.power = 120;
            super.accuracy = 50;
            super.status = StatusNamesies.PARALYZED;
            super.moveTypes.add(MoveType.BOMB_BALL);
        }
    }

    static class BraveBird extends Attack implements RecoilPercentageMove {
        private static final long serialVersionUID = 1L;

        BraveBird() {
            super(AttackNamesies.BRAVE_BIRD, Type.FLYING, MoveCategory.PHYSICAL, 15, "The user tucks in its wings and charges from a low altitude. This also damages the user quite a lot.");
            super.power = 120;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getDamagePercentageDenominator() {
            return 3;
        }
    }

    static class Uproar extends Attack {
        private static final long serialVersionUID = 1L;

        Uproar() {
            super(AttackNamesies.UPROAR, Type.NORMAL, MoveCategory.SPECIAL, 10, "The user attacks in an uproar for three turns. During that time, no Pok\u00e9mon can fall asleep.");
            super.power = 90;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.UPROAR;
            super.moveTypes.add(MoveType.SOUND_BASED);
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            super.selfTarget = true;
        }
    }

    static class Acupressure extends Attack {
        private static final long serialVersionUID = 1L;

        Acupressure() {
            super(AttackNamesies.ACUPRESSURE, Type.NORMAL, MoveCategory.STATUS, 30, "The user applies pressure to stress points, sharply boosting one of its or its allies' stats.");
            super.selfTarget = true;
        }

        @Override
        public void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            super.statChanges = new int[Stat.NUM_BATTLE_STATS];
            super.statChanges[RandomUtils.getRandomInt(super.statChanges.length)] = 2;
        }
    }

    static class DoubleHit extends Attack implements MultiStrikeMove {
        private static final long serialVersionUID = 1L;

        DoubleHit() {
            super(AttackNamesies.DOUBLE_HIT, Type.NORMAL, MoveCategory.PHYSICAL, 10, "The user slams the target with a long tail, vines, or a tentacle. The target is hit twice in a row.");
            super.power = 35;
            super.accuracy = 90;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getMinHits() {
            return 2;
        }

        @Override
        public int getMaxHits() {
            return 2;
        }
    }

    static class IcyWind extends Attack {
        private static final long serialVersionUID = 1L;

        IcyWind() {
            super(AttackNamesies.ICY_WIND, Type.ICE, MoveCategory.SPECIAL, 15, "The user attacks with a gust of chilled air. This also lowers the opposing Pok\u00e9mon's Speed stats.");
            super.power = 55;
            super.accuracy = 95;
            super.statChanges[Stat.SPEED.index()] = -1;
        }
    }

    static class IceShard extends Attack {
        private static final long serialVersionUID = 1L;

        IceShard() {
            super(AttackNamesies.ICE_SHARD, Type.ICE, MoveCategory.PHYSICAL, 30, "The user flash-freezes chunks of ice and hurls them at the target. This move always goes first.");
            super.power = 40;
            super.accuracy = 100;
            super.priority = 1;
        }
    }

    static class AquaRing extends Attack {
        private static final long serialVersionUID = 1L;

        AquaRing() {
            super(AttackNamesies.AQUA_RING, Type.WATER, MoveCategory.STATUS, 20, "The user envelops itself in a veil made of water. It regains some HP every turn.");
            super.effect = PokemonEffectNamesies.AQUA_RING;
            super.selfTarget = true;
        }
    }

    static class AuroraBeam extends Attack {
        private static final long serialVersionUID = 1L;

        AuroraBeam() {
            super(AttackNamesies.AURORA_BEAM, Type.ICE, MoveCategory.SPECIAL, 20, "The target is hit with a rainbow-colored beam. This may also lower the target's Attack stat.");
            super.power = 65;
            super.accuracy = 100;
            super.effectChance = 10;
            super.statChanges[Stat.ATTACK.index()] = -1;
        }
    }

    static class Brine extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Brine() {
            super(AttackNamesies.BRINE, Type.WATER, MoveCategory.SPECIAL, 10, "If the target's HP is half or less, this attack will hit with double the power.");
            super.power = 65;
            super.accuracy = 100;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getHPRatio() < .5 ? 2 : 1;
        }
    }

    static class Dive extends Attack implements SemiInvulnerableMove {
        private static final long serialVersionUID = 1L;

        private boolean isCharging;

        Dive() {
            super(AttackNamesies.DIVE, Type.WATER, MoveCategory.PHYSICAL, 10, "Diving on the first turn, the user floats up and attacks on the next turn.");
            super.power = 80;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            this.resetReady();
        }

        @Override
        public boolean isOverground() {
            return false;
        }

        @Override
        public String getChargeMessage(ActivePokemon user) {
            return user.getName() + " hid underwater!";
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void resetReady() {
            this.isCharging = !this.chargesFirst();
        }

        @Override
        public void switchReady() {
            this.isCharging = !this.isCharging;
        }
    }

    static class IceBeam extends Attack {
        private static final long serialVersionUID = 1L;

        IceBeam() {
            super(AttackNamesies.ICE_BEAM, Type.ICE, MoveCategory.SPECIAL, 10, "The target is struck with an icy-cold beam of energy. This may also leave the target frozen.");
            super.power = 90;
            super.accuracy = 100;
            super.effectChance = 10;
            super.status = StatusNamesies.FROZEN;
        }
    }

    static class SheerCold extends Attack implements OhkoMove {
        private static final long serialVersionUID = 1L;

        SheerCold() {
            super(AttackNamesies.SHEER_COLD, Type.ICE, MoveCategory.SPECIAL, 5, "The target faints instantly. It's less likely to hit the target if it's used by Pok\u00e9mon other than Ice types.");
            super.accuracy = 30;
        }

        @Override
        public int baseAccuracy(Battle b, ActivePokemon user) {
            // Accuracy is 10% lower for non-ice types
            return OhkoMove.super.baseAccuracy(b, user) - (user.isType(b, Type.ICE) ? 0 : 10);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return super.applies(b, user, victim) && !victim.isType(b, Type.ICE);
        }
    }

    static class PoisonGas extends Attack {
        private static final long serialVersionUID = 1L;

        PoisonGas() {
            super(AttackNamesies.POISON_GAS, Type.POISON, MoveCategory.STATUS, 40, "A cloud of poison gas is sprayed in the face of opposing Pok\u00e9mon, poisoning those hit.");
            super.accuracy = 90;
            super.status = StatusNamesies.POISONED;
        }
    }

    static class Sludge extends Attack {
        private static final long serialVersionUID = 1L;

        Sludge() {
            super(AttackNamesies.SLUDGE, Type.POISON, MoveCategory.SPECIAL, 20, "Unsanitary sludge is hurled at the target. This may also poison the target.");
            super.power = 65;
            super.accuracy = 100;
            super.effectChance = 30;
            super.status = StatusNamesies.POISONED;
        }
    }

    static class SludgeBomb extends Attack {
        private static final long serialVersionUID = 1L;

        SludgeBomb() {
            super(AttackNamesies.SLUDGE_BOMB, Type.POISON, MoveCategory.SPECIAL, 10, "Unsanitary sludge is hurled at the target. This may also poison the target.");
            super.power = 90;
            super.accuracy = 100;
            super.effectChance = 30;
            super.status = StatusNamesies.POISONED;
            super.moveTypes.add(MoveType.BOMB_BALL);
        }
    }

    static class AcidArmor extends Attack {
        private static final long serialVersionUID = 1L;

        AcidArmor() {
            super(AttackNamesies.ACID_ARMOR, Type.POISON, MoveCategory.STATUS, 20, "The user alters its cellular structure to liquefy itself, sharply raising its Defense stat.");
            super.selfTarget = true;
            super.statChanges[Stat.DEFENSE.index()] = 2;
        }
    }

    static class IcicleSpear extends Attack implements MultiStrikeMove {
        private static final long serialVersionUID = 1L;

        IcicleSpear() {
            super(AttackNamesies.ICICLE_SPEAR, Type.ICE, MoveCategory.PHYSICAL, 30, "The user launches sharp icicles at the target two to five times in a row.");
            super.power = 25;
            super.accuracy = 100;
        }

        @Override
        public int getMinHits() {
            return 2;
        }

        @Override
        public int getMaxHits() {
            return 5;
        }
    }

    static class Clamp extends Attack {
        private static final long serialVersionUID = 1L;

        Clamp() {
            super(AttackNamesies.CLAMP, Type.WATER, MoveCategory.PHYSICAL, 15, "The target is clamped and squeezed by the user's very thick and sturdy shell for four to five turns.");
            super.power = 35;
            super.accuracy = 85;
            super.effect = PokemonEffectNamesies.CLAMPED;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class RazorShell extends Attack {
        private static final long serialVersionUID = 1L;

        RazorShell() {
            super(AttackNamesies.RAZOR_SHELL, Type.WATER, MoveCategory.PHYSICAL, 10, "The user cuts its target with sharp shells. This may also lower the target's Defense stat.");
            super.power = 75;
            super.accuracy = 95;
            super.effectChance = 50;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.statChanges[Stat.DEFENSE.index()] = -1;
        }
    }

    static class Whirlpool extends Attack {
        private static final long serialVersionUID = 1L;

        Whirlpool() {
            super(AttackNamesies.WHIRLPOOL, Type.WATER, MoveCategory.SPECIAL, 15, "The user traps the target in a violent swirling whirlpool for four to five turns.");
            super.power = 35;
            super.accuracy = 85;
            super.effect = PokemonEffectNamesies.WHIRLPOOLED;
        }
    }

    static class ShellSmash extends Attack {
        private static final long serialVersionUID = 1L;

        ShellSmash() {
            super(AttackNamesies.SHELL_SMASH, Type.NORMAL, MoveCategory.STATUS, 15, "The user breaks its shell, which lowers Defense and Sp. Def stats but sharply raises its Attack, Sp. Atk, and Speed stats.");
            super.selfTarget = true;
            super.statChanges[Stat.DEFENSE.index()] = -1;
            super.statChanges[Stat.SP_DEFENSE.index()] = -1;
            super.statChanges[Stat.ATTACK.index()] = 2;
            super.statChanges[Stat.SP_ATTACK.index()] = 2;
            super.statChanges[Stat.SPEED.index()] = 2;
        }
    }

    static class SpikeCannon extends Attack implements MultiStrikeMove {
        private static final long serialVersionUID = 1L;

        SpikeCannon() {
            super(AttackNamesies.SPIKE_CANNON, Type.NORMAL, MoveCategory.PHYSICAL, 15, "Sharp spikes are shot at the target in rapid succession. They hit two to five times in a row.");
            super.power = 20;
            super.accuracy = 100;
        }

        @Override
        public int getMinHits() {
            return 2;
        }

        @Override
        public int getMaxHits() {
            return 5;
        }
    }

    static class Spikes extends Attack {
        private static final long serialVersionUID = 1L;

        Spikes() {
            super(AttackNamesies.SPIKES, Type.GROUND, MoveCategory.STATUS, 20, "The user lays a trap of spikes at the opposing team's feet. The trap hurts Pok\u00e9mon that switch into battle.");
            super.effect = TeamEffectNamesies.SPIKES;
            super.moveTypes.add(MoveType.FIELD);
        }
    }

    static class IcicleCrash extends Attack {
        private static final long serialVersionUID = 1L;

        IcicleCrash() {
            super(AttackNamesies.ICICLE_CRASH, Type.ICE, MoveCategory.PHYSICAL, 10, "The user attacks by harshly dropping large icicles onto the target. This may also make the target flinch.");
            super.power = 85;
            super.accuracy = 90;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 30;
        }
    }

    static class Lick extends Attack {
        private static final long serialVersionUID = 1L;

        Lick() {
            super(AttackNamesies.LICK, Type.GHOST, MoveCategory.PHYSICAL, 30, "The target is licked with a long tongue, causing damage. This may also leave the target with paralysis.");
            super.power = 30;
            super.accuracy = 100;
            super.effectChance = 30;
            super.status = StatusNamesies.PARALYZED;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Spite extends Attack {
        private static final long serialVersionUID = 1L;

        Spite() {
            super(AttackNamesies.SPITE, Type.GHOST, MoveCategory.STATUS, 10, "The user unleashes its grudge on the move last used by the target by cutting 4 PP from it.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Fails if the victim hasn't attacked yet, their last move already has 0 PP, or they don't actually know the last move they used
            Move last = victim.getLastMoveUsed();
            return last != null && last.getPP() > 0 && victim.hasMove(b, last.getAttack().namesies());
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            Move last = victim.getLastMoveUsed();
            Messages.add(victim.getName() + "'s " + last.getAttack().getName() + "'s PP was reduced by " + last.reducePP(4) + "!");
        }
    }

    static class NightShade extends Attack implements FixedDamageMove {
        private static final long serialVersionUID = 1L;

        NightShade() {
            super(AttackNamesies.NIGHT_SHADE, Type.GHOST, MoveCategory.SPECIAL, 15, "The user makes the target see a frightening mirage. It inflicts damage equal to the user's level.");
            super.accuracy = 100;
        }

        @Override
        public int getFixedDamageAmount(ActivePokemon me, ActivePokemon o) {
            return me.getLevel();
        }
    }

    static class ShadowBall extends Attack {
        private static final long serialVersionUID = 1L;

        ShadowBall() {
            super(AttackNamesies.SHADOW_BALL, Type.GHOST, MoveCategory.SPECIAL, 15, "The user hurls a shadowy blob at the target. This may also lower the target's Sp. Def stat.");
            super.power = 80;
            super.accuracy = 100;
            super.effectChance = 20;
            super.moveTypes.add(MoveType.BOMB_BALL);
            super.statChanges[Stat.SP_DEFENSE.index()] = -1;
        }
    }

    static class DreamEater extends Attack implements SapHealthMove {
        private static final long serialVersionUID = 1L;

        DreamEater() {
            super(AttackNamesies.DREAM_EATER, Type.PSYCHIC, MoveCategory.SPECIAL, 15, "The user eats the dreams of a sleeping target. It absorbs half the damage caused to heal its own HP.");
            super.power = 100;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.HEALING);
        }

        @Override
        public String getSapMessage(ActivePokemon victim) {
            return victim.getName() + "'s dream was eaten!";
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.hasStatus(StatusNamesies.ASLEEP);
        }
    }

    static class DarkPulse extends Attack {
        private static final long serialVersionUID = 1L;

        DarkPulse() {
            super(AttackNamesies.DARK_PULSE, Type.DARK, MoveCategory.SPECIAL, 15, "The user releases a horrible aura imbued with dark thoughts. This may also make the target flinch.");
            super.power = 80;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 20;
            super.moveTypes.add(MoveType.AURA_PULSE);
        }
    }

    static class Nightmare extends Attack {
        private static final long serialVersionUID = 1L;

        Nightmare() {
            super(AttackNamesies.NIGHTMARE, Type.GHOST, MoveCategory.STATUS, 15, "A sleeping target sees a nightmare that inflicts some damage every turn.");
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.NIGHTMARE;
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
        }
    }

    static class ShadowPunch extends Attack {
        private static final long serialVersionUID = 1L;

        ShadowPunch() {
            super(AttackNamesies.SHADOW_PUNCH, Type.GHOST, MoveCategory.PHYSICAL, 20, "The user throws a punch from the shadows. This attack never misses.");
            super.power = 60;
            super.moveTypes.add(MoveType.PUNCHING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Bind extends Attack {
        private static final long serialVersionUID = 1L;

        Bind() {
            super(AttackNamesies.BIND, Type.NORMAL, MoveCategory.PHYSICAL, 20, "Things such as long bodies or tentacles are used to bind and squeeze the target for four to five turns.");
            super.power = 15;
            super.accuracy = 85;
            super.effect = PokemonEffectNamesies.BINDED;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class RockTomb extends Attack {
        private static final long serialVersionUID = 1L;

        RockTomb() {
            super(AttackNamesies.ROCK_TOMB, Type.ROCK, MoveCategory.PHYSICAL, 15, "Boulders are hurled at the target. This also lowers the target's Speed stat by preventing its movement.");
            super.power = 60;
            super.accuracy = 95;
            super.statChanges[Stat.SPEED.index()] = -1;
        }
    }

    static class DragonBreath extends Attack {
        private static final long serialVersionUID = 1L;

        DragonBreath() {
            super(AttackNamesies.DRAGON_BREATH, Type.DRAGON, MoveCategory.SPECIAL, 20, "The user exhales a mighty gust that inflicts damage. This may also leave the target with paralysis.");
            super.power = 60;
            super.accuracy = 100;
            super.effectChance = 30;
            super.status = StatusNamesies.PARALYZED;
        }
    }

    static class IronTail extends Attack {
        private static final long serialVersionUID = 1L;

        IronTail() {
            super(AttackNamesies.IRON_TAIL, Type.STEEL, MoveCategory.PHYSICAL, 15, "The target is slammed with a steel-hard tail. This may also lower the target's Defense stat.");
            super.power = 100;
            super.accuracy = 75;
            super.effectChance = 30;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.statChanges[Stat.DEFENSE.index()] = -1;
        }
    }

    static class Meditate extends Attack {
        private static final long serialVersionUID = 1L;

        Meditate() {
            super(AttackNamesies.MEDITATE, Type.PSYCHIC, MoveCategory.STATUS, 40, "The user meditates to awaken the power deep within its body and raise its Attack stat.");
            super.selfTarget = true;
            super.statChanges[Stat.ATTACK.index()] = 1;
        }
    }

    static class Synchronoise extends Attack {
        private static final long serialVersionUID = 1L;

        Synchronoise() {
            super(AttackNamesies.SYNCHRONOISE, Type.PSYCHIC, MoveCategory.SPECIAL, 10, "Using an odd shock wave, the user inflicts damage on any Pok\u00e9mon of the same type in the area around it.");
            super.power = 120;
            super.accuracy = 100;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Like this is literally the stupidest move ever like srsly what is wrong with the creators
            PokeType type = user.getType(b);
            return victim.isType(b, type.getFirstType()) || (type.isDualTyped() && victim.isType(b, type.getSecondType()));
        }
    }

    static class Psyshock extends Attack implements OpponentStatSwitchingEffect {
        private static final long serialVersionUID = 1L;

        Psyshock() {
            super(AttackNamesies.PSYSHOCK, Type.PSYCHIC, MoveCategory.SPECIAL, 10, "The user materializes an odd psychic wave to attack the target. This attack does physical damage.");
            super.power = 80;
            super.accuracy = 100;
        }

        @Override
        public Stat getSwitchStat(Stat s) {
            return s == Stat.SP_DEFENSE ? Stat.DEFENSE : s;
        }
    }

    static class ViceGrip extends Attack {
        private static final long serialVersionUID = 1L;

        ViceGrip() {
            super(AttackNamesies.VICE_GRIP, Type.NORMAL, MoveCategory.PHYSICAL, 30, "The target is gripped and squeezed from both sides to inflict damage.");
            super.power = 55;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class MetalClaw extends Attack {
        private static final long serialVersionUID = 1L;

        MetalClaw() {
            super(AttackNamesies.METAL_CLAW, Type.STEEL, MoveCategory.PHYSICAL, 35, "The target is raked with steel claws. This may also raise the user's Attack stat.");
            super.power = 50;
            super.accuracy = 95;
            super.effectChance = 10;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.selfTarget = true;
            super.statChanges[Stat.ATTACK.index()] = 1;
        }
    }

    static class Guillotine extends Attack implements OhkoMove {
        private static final long serialVersionUID = 1L;

        Guillotine() {
            super(AttackNamesies.GUILLOTINE, Type.NORMAL, MoveCategory.PHYSICAL, 5, "A vicious, tearing attack with big pincers. The target faints instantly if this attack hits.");
            super.accuracy = 30;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Crabhammer extends Attack implements CritStageEffect {
        private static final long serialVersionUID = 1L;

        Crabhammer() {
            super(AttackNamesies.CRABHAMMER, Type.WATER, MoveCategory.PHYSICAL, 10, "The target is hammered with a large pincer. Critical hits land more easily.");
            super.power = 100;
            super.accuracy = 90;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Flail extends Attack {
        private static final long serialVersionUID = 1L;

        Flail() {
            super(AttackNamesies.FLAIL, Type.NORMAL, MoveCategory.PHYSICAL, 15, "The user flails about aimlessly to attack. The less HP the user has, the greater the move's power.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getPower(Battle b, ActivePokemon me, ActivePokemon o) {
            double ratio = me.getHPRatio();
            if (ratio > .7) {
                return 20;
            } else if (ratio > .35) {
                return 40;
            } else if (ratio > .2) {
                return 80;
            } else if (ratio > .1) {
                return 100;
            } else if (ratio > .04) {
                return 150;
            } else {
                return 200;
            }
        }
    }

    static class Charge extends Attack {
        private static final long serialVersionUID = 1L;

        Charge() {
            super(AttackNamesies.CHARGE, Type.ELECTRIC, MoveCategory.STATUS, 20, "The user boosts the power of the Electric move it uses on the next turn. This also raises the user's Sp. Def stat.");
            super.effect = PokemonEffectNamesies.CHARGE;
            super.selfTarget = true;
            super.statChanges[Stat.SP_DEFENSE.index()] = 1;
        }
    }

    static class ChargeBeam extends Attack {
        private static final long serialVersionUID = 1L;

        ChargeBeam() {
            super(AttackNamesies.CHARGE_BEAM, Type.ELECTRIC, MoveCategory.SPECIAL, 10, "The user attacks with an electric charge. The user may use any remaining electricity to raise its Sp. Atk stat.");
            super.power = 50;
            super.accuracy = 90;
            super.effectChance = 70;
            super.selfTarget = true;
            super.statChanges[Stat.SP_ATTACK.index()] = 1;
        }
    }

    // Fails if no damage to reflect or if the opponent isn't using an attack of the proper category
    static class MirrorCoat extends Attack implements FixedDamageMove {
        private static final long serialVersionUID = 1L;

        MirrorCoat() {
            super(AttackNamesies.MIRROR_COAT, Type.PSYCHIC, MoveCategory.SPECIAL, 20, "A retaliation move that counters any special attack, inflicting double the damage taken.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.MIRRORLESS);
            super.priority = -5;
        }

        @Override
        public int getFixedDamageAmount(ActivePokemon me, ActivePokemon o) {
            return me.getDamageTaken();
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getDamageTaken() > 0 && victim.getMove() != null && victim.getAttack().getCategory() == MoveCategory.SPECIAL && !b.isFirstAttack();
        }
    }

    // Fails if no damage to reflect or if the opponent isn't using an attack of the proper category
    static class Counter extends Attack implements FixedDamageMove {
        private static final long serialVersionUID = 1L;

        Counter() {
            super(AttackNamesies.COUNTER, Type.FIGHTING, MoveCategory.PHYSICAL, 20, "A retaliation move that counters any physical attack, inflicting double the damage taken.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.MIRRORLESS);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.priority = -5;
        }

        @Override
        public int getFixedDamageAmount(ActivePokemon me, ActivePokemon o) {
            return me.getDamageTaken();
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getDamageTaken() > 0 && victim.getMove() != null && victim.getAttack().getCategory() == MoveCategory.PHYSICAL && !b.isFirstAttack();
        }
    }

    static class Barrage extends Attack implements MultiStrikeMove {
        private static final long serialVersionUID = 1L;

        Barrage() {
            super(AttackNamesies.BARRAGE, Type.NORMAL, MoveCategory.PHYSICAL, 20, "Round objects are hurled at the target to strike two to five times in a row.");
            super.power = 15;
            super.accuracy = 85;
            super.moveTypes.add(MoveType.BOMB_BALL);
        }

        @Override
        public int getMinHits() {
            return 2;
        }

        @Override
        public int getMaxHits() {
            return 5;
        }
    }

    static class BulletSeed extends Attack implements MultiStrikeMove {
        private static final long serialVersionUID = 1L;

        BulletSeed() {
            super(AttackNamesies.BULLET_SEED, Type.GRASS, MoveCategory.PHYSICAL, 30, "The user forcefully shoots seeds at the target two to five times in a row.");
            super.power = 25;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.BOMB_BALL);
        }

        @Override
        public int getMinHits() {
            return 2;
        }

        @Override
        public int getMaxHits() {
            return 5;
        }
    }

    static class EggBomb extends Attack {
        private static final long serialVersionUID = 1L;

        EggBomb() {
            super(AttackNamesies.EGG_BOMB, Type.NORMAL, MoveCategory.PHYSICAL, 10, "A large egg is hurled at the target with maximum force to inflict damage.");
            super.power = 100;
            super.accuracy = 75;
            super.moveTypes.add(MoveType.BOMB_BALL);
        }
    }

    static class WoodHammer extends Attack implements RecoilPercentageMove {
        private static final long serialVersionUID = 1L;

        WoodHammer() {
            super(AttackNamesies.WOOD_HAMMER, Type.GRASS, MoveCategory.PHYSICAL, 15, "The user slams its rugged body into the target to attack. This also damages the user quite a lot.");
            super.power = 120;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getDamagePercentageDenominator() {
            return 3;
        }
    }

    static class BoneClub extends Attack {
        private static final long serialVersionUID = 1L;

        BoneClub() {
            super(AttackNamesies.BONE_CLUB, Type.GROUND, MoveCategory.PHYSICAL, 20, "The user clubs the target with a bone. This may also make the target flinch.");
            super.power = 65;
            super.accuracy = 85;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 10;
        }
    }

    static class Bonemerang extends Attack implements MultiStrikeMove {
        private static final long serialVersionUID = 1L;

        Bonemerang() {
            super(AttackNamesies.BONEMERANG, Type.GROUND, MoveCategory.PHYSICAL, 10, "The user throws the bone it holds. The bone loops around to hit the target twice -- coming and going.");
            super.power = 50;
            super.accuracy = 90;
        }

        @Override
        public int getMinHits() {
            return 2;
        }

        @Override
        public int getMaxHits() {
            return 2;
        }
    }

    static class BoneRush extends Attack implements MultiStrikeMove {
        private static final long serialVersionUID = 1L;

        BoneRush() {
            super(AttackNamesies.BONE_RUSH, Type.GROUND, MoveCategory.PHYSICAL, 10, "The user strikes the target with a hard bone two to five times in a row.");
            super.power = 25;
            super.accuracy = 90;
        }

        @Override
        public int getMinHits() {
            return 2;
        }

        @Override
        public int getMaxHits() {
            return 5;
        }
    }

    static class RollingKick extends Attack {
        private static final long serialVersionUID = 1L;

        RollingKick() {
            super(AttackNamesies.ROLLING_KICK, Type.FIGHTING, MoveCategory.PHYSICAL, 15, "The user lashes out with a quick, spinning kick. This may also make the target flinch.");
            super.power = 60;
            super.accuracy = 85;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 30;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class JumpKick extends Attack implements CrashDamageMove {
        private static final long serialVersionUID = 1L;

        JumpKick() {
            super(AttackNamesies.JUMP_KICK, Type.FIGHTING, MoveCategory.PHYSICAL, 10, "The user jumps up high, then strikes with a kick. If the kick misses, the user hurts itself.");
            super.power = 100;
            super.accuracy = 95;
            super.moveTypes.add(MoveType.AIRBORNE);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getMaxHealthPercentageDenominator() {
            return 3;
        }
    }

    static class BrickBreak extends Attack {
        private static final long serialVersionUID = 1L;

        BrickBreak() {
            super(AttackNamesies.BRICK_BREAK, Type.FIGHTING, MoveCategory.PHYSICAL, 15, "The user attacks with a swift chop. It can also break barriers, such as Light Screen and Reflect.");
            super.power = 75;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            BarrierEffect.breakBarriers(b, victim, user);
        }
    }

    static class PsychicFangs extends Attack {
        private static final long serialVersionUID = 1L;

        PsychicFangs() {
            super(AttackNamesies.PSYCHIC_FANGS, Type.PSYCHIC, MoveCategory.PHYSICAL, 10, "The user bites the target with its psychic capabilities. This can also destroy Light Screen and Reflect.");
            super.power = 85;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.BITING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            BarrierEffect.breakBarriers(b, victim, user);
        }
    }

    static class HighJumpKick extends Attack implements CrashDamageMove {
        private static final long serialVersionUID = 1L;

        HighJumpKick() {
            super(AttackNamesies.HIGH_JUMP_KICK, Type.FIGHTING, MoveCategory.PHYSICAL, 10, "The target is attacked with a knee kick from a jump. If it misses, the user is hurt instead.");
            super.power = 130;
            super.accuracy = 90;
            super.moveTypes.add(MoveType.AIRBORNE);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getMaxHealthPercentageDenominator() {
            return 2;
        }
    }

    static class BlazeKick extends Attack implements CritStageEffect {
        private static final long serialVersionUID = 1L;

        BlazeKick() {
            super(AttackNamesies.BLAZE_KICK, Type.FIRE, MoveCategory.PHYSICAL, 10, "The user launches a kick that lands a critical hit more easily. This may also leave the target with a burn.");
            super.power = 85;
            super.accuracy = 90;
            super.effectChance = 10;
            super.status = StatusNamesies.BURNED;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class MegaKick extends Attack {
        private static final long serialVersionUID = 1L;

        MegaKick() {
            super(AttackNamesies.MEGA_KICK, Type.NORMAL, MoveCategory.PHYSICAL, 5, "The target is attacked by a kick launched with muscle-packed power.");
            super.power = 120;
            super.accuracy = 75;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class CometPunch extends Attack implements MultiStrikeMove {
        private static final long serialVersionUID = 1L;

        CometPunch() {
            super(AttackNamesies.COMET_PUNCH, Type.NORMAL, MoveCategory.PHYSICAL, 15, "The target is hit with a flurry of punches that strike two to five times in a row.");
            super.power = 18;
            super.accuracy = 85;
            super.moveTypes.add(MoveType.PUNCHING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getMinHits() {
            return 2;
        }

        @Override
        public int getMaxHits() {
            return 5;
        }
    }

    static class MachPunch extends Attack {
        private static final long serialVersionUID = 1L;

        MachPunch() {
            super(AttackNamesies.MACH_PUNCH, Type.FIGHTING, MoveCategory.PHYSICAL, 30, "The user throws a punch at blinding speed. This move always goes first.");
            super.power = 40;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PUNCHING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.priority = 1;
        }
    }

    static class BulletPunch extends Attack {
        private static final long serialVersionUID = 1L;

        BulletPunch() {
            super(AttackNamesies.BULLET_PUNCH, Type.STEEL, MoveCategory.PHYSICAL, 30, "The user strikes the target with tough punches as fast as bullets. This move always goes first.");
            super.power = 40;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PUNCHING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.priority = 1;
        }
    }

    static class VacuumWave extends Attack {
        private static final long serialVersionUID = 1L;

        VacuumWave() {
            super(AttackNamesies.VACUUM_WAVE, Type.FIGHTING, MoveCategory.SPECIAL, 30, "The user whirls its fists to send a wave of pure vacuum at the target. This move always goes first.");
            super.power = 40;
            super.accuracy = 100;
            super.priority = 1;
        }
    }

    static class ThunderPunch extends Attack {
        private static final long serialVersionUID = 1L;

        ThunderPunch() {
            super(AttackNamesies.THUNDER_PUNCH, Type.ELECTRIC, MoveCategory.PHYSICAL, 15, "The target is punched with an electrified fist. This may also leave the target with paralysis.");
            super.power = 75;
            super.accuracy = 100;
            super.effectChance = 10;
            super.status = StatusNamesies.PARALYZED;
            super.moveTypes.add(MoveType.PUNCHING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class IcePunch extends Attack {
        private static final long serialVersionUID = 1L;

        IcePunch() {
            super(AttackNamesies.ICE_PUNCH, Type.ICE, MoveCategory.PHYSICAL, 15, "The target is punched with an icy fist. This may also leave the target frozen.");
            super.power = 75;
            super.accuracy = 100;
            super.effectChance = 10;
            super.status = StatusNamesies.FROZEN;
            super.moveTypes.add(MoveType.PUNCHING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class FirePunch extends Attack {
        private static final long serialVersionUID = 1L;

        FirePunch() {
            super(AttackNamesies.FIRE_PUNCH, Type.FIRE, MoveCategory.PHYSICAL, 15, "The target is punched with a fiery fist. This may also leave the target with a burn.");
            super.power = 75;
            super.accuracy = 100;
            super.effectChance = 10;
            super.status = StatusNamesies.BURNED;
            super.moveTypes.add(MoveType.PUNCHING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class SkyUppercut extends Attack implements SemiInvulnerableBypasser {
        private static final long serialVersionUID = 1L;

        SkyUppercut() {
            super(AttackNamesies.SKY_UPPERCUT, Type.FIGHTING, MoveCategory.PHYSICAL, 15, "The user attacks the target with an uppercut thrown skyward with force.");
            super.power = 85;
            super.accuracy = 90;
            super.moveTypes.add(MoveType.PUNCHING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public boolean semiInvulnerableBypass(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Always hit when the opponent is flying
            return defending.isSemiInvulnerableFlying();
        }
    }

    static class MegaPunch extends Attack {
        private static final long serialVersionUID = 1L;

        MegaPunch() {
            super(AttackNamesies.MEGA_PUNCH, Type.NORMAL, MoveCategory.PHYSICAL, 20, "The target is slugged by a punch thrown with muscle-packed power.");
            super.power = 80;
            super.accuracy = 85;
            super.moveTypes.add(MoveType.PUNCHING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class FocusPunch extends Attack {
        private static final long serialVersionUID = 1L;

        FocusPunch() {
            super(AttackNamesies.FOCUS_PUNCH, Type.FIGHTING, MoveCategory.PHYSICAL, 20, "The user focuses its mind before launching a punch. This move fails if the user is hit before it is used.");
            super.power = 150;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.FOCUSING;
            super.moveTypes.add(MoveType.PUNCHING);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.MIRRORLESS);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.selfTarget = true;
            super.priority = -3;
        }

        @Override
        public void startTurn(Battle b, ActivePokemon me) {
            super.applyBasicEffects(b, me, me);
            me.setReducePP(true);
        }

        @Override
        public boolean shouldApplyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            return false;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.hasEffect(PokemonEffectNamesies.FOCUSING);
        }
    }

    static class ShellTrap extends Attack {
        private static final long serialVersionUID = 1L;

        ShellTrap() {
            super(AttackNamesies.SHELL_TRAP, Type.FIRE, MoveCategory.SPECIAL, 5, "The user sets a shell trap. If the user is hit by a physical move, the trap will explode and inflict damage on the opposing Pok\u00e9mon.");
            super.power = 150;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.SHELL_TRAP;
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.MIRRORLESS);
            super.selfTarget = true;
            super.priority = -3;
        }

        @Override
        public void startTurn(Battle b, ActivePokemon me) {
            super.applyBasicEffects(b, me, me);
            me.setReducePP(true);
        }

        @Override
        public boolean shouldApplyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            return false;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return !user.hasEffect(PokemonEffectNamesies.SHELL_TRAP);
        }
    }

    static class BeakBlast extends Attack {
        private static final long serialVersionUID = 1L;

        BeakBlast() {
            super(AttackNamesies.BEAK_BLAST, Type.FLYING, MoveCategory.PHYSICAL, 15, "The user first heats up its beak, and then it attacks the target. Making direct contact with the Pok\u00e9mon while it's heating up its beak results in a burn.");
            super.power = 100;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.BEAK_BLAST;
            super.moveTypes.add(MoveType.BOMB_BALL);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.MIRRORLESS);
            super.selfTarget = true;
            super.priority = -3;
        }

        @Override
        public void startTurn(Battle b, ActivePokemon me) {
            super.applyBasicEffects(b, me, me);
            me.setReducePP(true);
        }

        @Override
        public boolean shouldApplyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            return false;
        }
    }

    static class MeFirst extends Attack {
        private static final long serialVersionUID = 1L;

        private Effect effect;

        MeFirst() {
            super(AttackNamesies.ME_FIRST, Type.NORMAL, MoveCategory.STATUS, 20, "The user cuts ahead of the target to copy and use the target's intended move with greater power. This move fails if it isn't used first.");
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.MIRRORLESS);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Fails if it is the second turn or the opponent is using a status move
            return b.isFirstAttack() && victim.getMove() != null && !victim.getAttack().isStatusMove();
        }

        @Override
        public void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            this.effect = Effect.cast(PokemonEffectNamesies.FIDDY_PERCENT_STRONGER, b, attacking, attacking, CastSource.ATTACK, false);
        }

        @Override
        public void endAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            this.effect.deactivate();
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            user.callNewMove(b, victim, victim.getAttack().namesies());
        }
    }

    static class Refresh extends Attack {
        private static final long serialVersionUID = 1L;

        Refresh() {
            super(AttackNamesies.REFRESH, Type.NORMAL, MoveCategory.STATUS, 20, "The user rests to cure itself of poisoning, a burn, or paralysis.");
            super.moveTypes.add(MoveType.HEALING);
            super.selfTarget = true;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            user.removeStatus(b, CastSource.ATTACK);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.hasStatus();
        }
    }

    static class PowerWhip extends Attack {
        private static final long serialVersionUID = 1L;

        PowerWhip() {
            super(AttackNamesies.POWER_WHIP, Type.GRASS, MoveCategory.PHYSICAL, 10, "The user violently whirls its vines, tentacles, or the like to harshly lash the target.");
            super.power = 120;
            super.accuracy = 85;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Smog extends Attack {
        private static final long serialVersionUID = 1L;

        Smog() {
            super(AttackNamesies.SMOG, Type.POISON, MoveCategory.SPECIAL, 20, "The target is attacked with a discharge of filthy gases. This may also poison the target.");
            super.power = 30;
            super.accuracy = 70;
            super.effectChance = 40;
            super.status = StatusNamesies.POISONED;
        }
    }

    static class ClearSmog extends Attack {
        private static final long serialVersionUID = 1L;

        ClearSmog() {
            super(AttackNamesies.CLEAR_SMOG, Type.POISON, MoveCategory.SPECIAL, 15, "The user attacks by throwing a clump of special mud. All stat changes are returned to normal.");
            super.power = 50;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            user.getStages().reset();
            victim.getStages().reset();
            Messages.add("All stat changes were eliminated!");
        }
    }

    static class HammerArm extends Attack {
        private static final long serialVersionUID = 1L;

        HammerArm() {
            super(AttackNamesies.HAMMER_ARM, Type.FIGHTING, MoveCategory.PHYSICAL, 10, "The user swings and hits with its strong, heavy fist. It lowers the user's Speed, however.");
            super.power = 100;
            super.accuracy = 90;
            super.moveTypes.add(MoveType.PUNCHING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.selfTarget = true;
            super.statChanges[Stat.SPEED.index()] = -1;
        }
    }

    static class IceHammer extends Attack {
        private static final long serialVersionUID = 1L;

        IceHammer() {
            super(AttackNamesies.ICE_HAMMER, Type.ICE, MoveCategory.PHYSICAL, 10, "The user swings and hits with its strong, heavy fist. It lowers the user's Speed, however.");
            super.power = 100;
            super.accuracy = 90;
            super.moveTypes.add(MoveType.PUNCHING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.selfTarget = true;
            super.statChanges[Stat.SPEED.index()] = -1;
        }
    }

    static class SoftBoiled extends Attack implements SelfHealingMove {
        private static final long serialVersionUID = 1L;

        SoftBoiled() {
            super(AttackNamesies.SOFT_BOILED, Type.NORMAL, MoveCategory.STATUS, 10, "The user restores its own HP by up to half of its max HP.");
            super.moveTypes.add(MoveType.HEALING);
            super.selfTarget = true;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            this.heal(b, victim);
        }

        @Override
        public double getHealFraction(Battle b, ActivePokemon victim) {
            return .5;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.canHeal();
        }
    }

    static class AncientPower extends Attack {
        private static final long serialVersionUID = 1L;

        AncientPower() {
            super(AttackNamesies.ANCIENT_POWER, Type.ROCK, MoveCategory.SPECIAL, 5, "The user attacks with a prehistoric power. This may also raise all the user's stats at once.");
            super.power = 60;
            super.accuracy = 100;
            super.effectChance = 10;
            super.selfTarget = true;
            super.statChanges[Stat.ATTACK.index()] = 1;
            super.statChanges[Stat.DEFENSE.index()] = 1;
            super.statChanges[Stat.SP_ATTACK.index()] = 1;
            super.statChanges[Stat.SP_DEFENSE.index()] = 1;
            super.statChanges[Stat.SPEED.index()] = 1;
        }
    }

    static class Tickle extends Attack {
        private static final long serialVersionUID = 1L;

        Tickle() {
            super(AttackNamesies.TICKLE, Type.NORMAL, MoveCategory.STATUS, 20, "The user tickles the target into laughing, reducing its Attack and Defense stats.");
            super.accuracy = 100;
            super.statChanges[Stat.ATTACK.index()] = -1;
            super.statChanges[Stat.DEFENSE.index()] = -1;
        }
    }

    static class DizzyPunch extends Attack {
        private static final long serialVersionUID = 1L;

        DizzyPunch() {
            super(AttackNamesies.DIZZY_PUNCH, Type.NORMAL, MoveCategory.PHYSICAL, 10, "The target is hit with rhythmically launched punches. This may also leave the target confused.");
            super.power = 70;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.CONFUSION;
            super.effectChance = 20;
            super.moveTypes.add(MoveType.PUNCHING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Outrage extends Attack {
        private static final long serialVersionUID = 1L;

        Outrage() {
            super(AttackNamesies.OUTRAGE, Type.DRAGON, MoveCategory.PHYSICAL, 10, "The user rampages and attacks for two to three turns. The user then becomes confused.");
            super.power = 120;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.SELF_CONFUSION;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.selfTarget = true;
        }
    }

    static class DragonDance extends Attack {
        private static final long serialVersionUID = 1L;

        DragonDance() {
            super(AttackNamesies.DRAGON_DANCE, Type.DRAGON, MoveCategory.STATUS, 20, "The user vigorously performs a mystic, powerful dance that raises its Attack and Speed stats.");
            super.moveTypes.add(MoveType.DANCE);
            super.selfTarget = true;
            super.statChanges[Stat.ATTACK.index()] = 1;
            super.statChanges[Stat.SPEED.index()] = 1;
        }
    }

    static class DragonPulse extends Attack {
        private static final long serialVersionUID = 1L;

        DragonPulse() {
            super(AttackNamesies.DRAGON_PULSE, Type.DRAGON, MoveCategory.SPECIAL, 10, "The target is attacked with a shock wave generated by the user's gaping mouth.");
            super.power = 85;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.AURA_PULSE);
        }
    }

    static class DracoMeteor extends Attack {
        private static final long serialVersionUID = 1L;

        DracoMeteor() {
            super(AttackNamesies.DRACO_METEOR, Type.DRAGON, MoveCategory.SPECIAL, 5, "Comets are summoned down from the sky onto the target. The attack's recoil harshly lowers the user's Sp. Atk stat.");
            super.power = 130;
            super.accuracy = 90;
            super.selfTarget = true;
            super.statChanges[Stat.SP_ATTACK.index()] = -2;
        }
    }

    static class Waterfall extends Attack {
        private static final long serialVersionUID = 1L;

        Waterfall() {
            super(AttackNamesies.WATERFALL, Type.WATER, MoveCategory.PHYSICAL, 15, "The user charges at the target and may make it flinch.");
            super.power = 80;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 20;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class ReflectType extends Attack implements ChangeTypeSource {
        private static final long serialVersionUID = 1L;

        ReflectType() {
            super(AttackNamesies.REFLECT_TYPE, Type.NORMAL, MoveCategory.STATUS, 15, "The user reflects the target's type, making it the same type as the target.");
            super.effect = PokemonEffectNamesies.CHANGE_TYPE;
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.selfTarget = true;
        }

        @Override
        public PokeType getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
            PokeType type = b.getOtherPokemon(caster).getType(b);
            return new PokeType(type.getFirstType(), type.getSecondType());
        }
    }

    static class MagicalLeaf extends Attack {
        private static final long serialVersionUID = 1L;

        MagicalLeaf() {
            super(AttackNamesies.MAGICAL_LEAF, Type.GRASS, MoveCategory.SPECIAL, 20, "The user scatters curious leaves that chase the target. This attack never misses.");
            super.power = 60;
        }
    }

    static class PowerSwap extends Attack {
        private static final long serialVersionUID = 1L;

        private static final Stat[] swapStats = { Stat.ATTACK, Stat.SP_ATTACK };

        PowerSwap() {
            super(AttackNamesies.POWER_SWAP, Type.PSYCHIC, MoveCategory.STATUS, 10, "The user employs its psychic power to switch changes to its Attack and Sp. Atk stats with the target.");
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            for (Stat s : swapStats) {
                user.getStages().swapStages(s, victim);
            }

            Messages.add(user.getName() + " swapped its stats with " + victim.getName() + "!");
        }
    }

    static class GuardSwap extends Attack {
        private static final long serialVersionUID = 1L;

        private static final Stat[] swapStats = { Stat.DEFENSE, Stat.SP_DEFENSE };

        GuardSwap() {
            super(AttackNamesies.GUARD_SWAP, Type.PSYCHIC, MoveCategory.STATUS, 10, "The user employs its psychic power to switch changes to its Defense and Sp. Def stats with the target.");
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            for (Stat s : swapStats) {
                user.getStages().swapStages(s, victim);
            }

            Messages.add(user.getName() + " swapped its stats with " + victim.getName() + "!");
        }
    }

    static class SpeedSwap extends Attack {
        private static final long serialVersionUID = 1L;

        SpeedSwap() {
            super(AttackNamesies.SPEED_SWAP, Type.PSYCHIC, MoveCategory.STATUS, 10, "The user exchanges Speed stats with the target.");
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            // NOTE: Looks like this is supposed to actually swap the stats and not just the stages but I don't really care it should do the same thing as power and guard swap because that makes more sense sue me
            user.getStages().swapStages(Stat.SPEED, victim);
            Messages.add(user.getName() + " swapped its stats with " + victim.getName() + "!");
        }
    }

    static class Copycat extends Attack {
        private static final long serialVersionUID = 1L;

        private AttackNamesies mirror;

        Copycat() {
            super(AttackNamesies.COPYCAT, Type.NORMAL, MoveCategory.STATUS, 20, "The user mimics the move used immediately before it. The move fails if no other move has been used yet.");
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            super.moveTypes.add(MoveType.ENCORELESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.MIRRORLESS);
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
        }

        @Override
        public void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            Move lastMoveUsed = defending.getLastMoveUsed();
            this.mirror = lastMoveUsed == null ? null : lastMoveUsed.getAttack().namesies();
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return mirror != null && !mirror.getNewAttack().isMoveType(MoveType.MIRRORLESS);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            user.callNewMove(b, victim, this.mirror);
        }
    }

    static class Transform extends Attack {
        private static final long serialVersionUID = 1L;

        Transform() {
            super(AttackNamesies.TRANSFORM, Type.NORMAL, MoveCategory.STATUS, 10, "The user transforms into a copy of the target right down to having the same move set.");
            super.effect = PokemonEffectNamesies.TRANSFORMED;
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.moveTypes.add(MoveType.ENCORELESS);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.selfTarget = true;
        }
    }

    static class Substitute extends Attack {
        private static final long serialVersionUID = 1L;

        Substitute() {
            super(AttackNamesies.SUBSTITUTE, Type.NORMAL, MoveCategory.STATUS, 10, "The user makes a copy of itself using some of its HP. The copy serves as the user's decoy.");
            super.effect = PokemonEffectNamesies.SUBSTITUTE;
            super.selfTarget = true;
        }
    }

    static class RazorWind extends Attack implements CritStageEffect, ChargingMove {
        private static final long serialVersionUID = 1L;

        private boolean isCharging;

        RazorWind() {
            super(AttackNamesies.RAZOR_WIND, Type.NORMAL, MoveCategory.SPECIAL, 10, "In this two-turn attack, blades of wind hit opposing Pok\u00e9mon on the second turn. Critical hits land more easily.");
            super.power = 80;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            this.resetReady();
        }

        @Override
        public String getChargeMessage(ActivePokemon user) {
            return user.getName() + " whipped up a whirlwind!";
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void resetReady() {
            this.isCharging = !this.chargesFirst();
        }

        @Override
        public void switchReady() {
            this.isCharging = !this.isCharging;
        }
    }

    static class LovelyKiss extends Attack {
        private static final long serialVersionUID = 1L;

        LovelyKiss() {
            super(AttackNamesies.LOVELY_KISS, Type.NORMAL, MoveCategory.STATUS, 10, "With a scary face, the user tries to force a kiss on the target. If it succeeds, the target falls asleep.");
            super.accuracy = 75;
            super.status = StatusNamesies.ASLEEP;
        }
    }

    static class PowderSnow extends Attack {
        private static final long serialVersionUID = 1L;

        PowderSnow() {
            super(AttackNamesies.POWDER_SNOW, Type.ICE, MoveCategory.SPECIAL, 25, "The user attacks with a chilling gust of powdery snow. This may also freeze the opposing Pok\u00e9mon.");
            super.power = 40;
            super.accuracy = 100;
            super.effectChance = 10;
            super.status = StatusNamesies.FROZEN;
        }
    }

    static class HeartStamp extends Attack {
        private static final long serialVersionUID = 1L;

        HeartStamp() {
            super(AttackNamesies.HEART_STAMP, Type.PSYCHIC, MoveCategory.PHYSICAL, 25, "The user unleashes a vicious blow after its cute act makes the target less wary. This may also make the target flinch.");
            super.power = 60;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 30;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class FakeTears extends Attack {
        private static final long serialVersionUID = 1L;

        FakeTears() {
            super(AttackNamesies.FAKE_TEARS, Type.DARK, MoveCategory.STATUS, 20, "The user feigns crying to fluster the target, harshly lowering its Sp. Def stat.");
            super.accuracy = 100;
            super.statChanges[Stat.SP_DEFENSE.index()] = -2;
        }
    }

    static class Avalanche extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Avalanche() {
            super(AttackNamesies.AVALANCHE, Type.ICE, MoveCategory.PHYSICAL, 10, "The power of this attack move is doubled if the user has been hurt by the target in the same turn.");
            super.power = 60;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.priority = -4;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.hasTakenDamage() ? 2 : 1;
        }
    }

    static class Blizzard extends Attack implements BasicAccuracyBypassEffect {
        private static final long serialVersionUID = 1L;

        Blizzard() {
            super(AttackNamesies.BLIZZARD, Type.ICE, MoveCategory.SPECIAL, 5, "A howling blizzard is summoned to strike opposing Pok\u00e9mon. This may also leave the opposing Pok\u00e9mon frozen.");
            super.power = 110;
            super.accuracy = 70;
            super.effectChance = 10;
            super.status = StatusNamesies.FROZEN;
        }

        @Override
        public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Always hits when it's hailing unless the opponent is hiding
            return b.getWeather().namesies() == WeatherNamesies.HAILING;
        }
    }

    static class ShockWave extends Attack {
        private static final long serialVersionUID = 1L;

        ShockWave() {
            super(AttackNamesies.SHOCK_WAVE, Type.ELECTRIC, MoveCategory.SPECIAL, 20, "The user strikes the target with a quick jolt of electricity. This attack never misses.");
            super.power = 60;
        }
    }

    static class LavaPlume extends Attack {
        private static final long serialVersionUID = 1L;

        LavaPlume() {
            super(AttackNamesies.LAVA_PLUME, Type.FIRE, MoveCategory.SPECIAL, 15, "The user torches everything around it in an inferno of scarlet flames. This may also leave those it hits with a burn.");
            super.power = 80;
            super.accuracy = 100;
            super.effectChance = 30;
            super.status = StatusNamesies.BURNED;
        }
    }

    static class WorkUp extends Attack {
        private static final long serialVersionUID = 1L;

        WorkUp() {
            super(AttackNamesies.WORK_UP, Type.NORMAL, MoveCategory.STATUS, 30, "The user is roused, and its Attack and Sp. Atk stats increase.");
            super.selfTarget = true;
            super.statChanges[Stat.ATTACK.index()] = 1;
            super.statChanges[Stat.SP_ATTACK.index()] = 1;
        }
    }

    static class GigaImpact extends Attack implements RechargingMove {
        private static final long serialVersionUID = 1L;

        private boolean isCharging;

        GigaImpact() {
            super(AttackNamesies.GIGA_IMPACT, Type.NORMAL, MoveCategory.PHYSICAL, 5, "The user charges at the target using every bit of its power. The user can't move on the next turn.");
            super.power = 150;
            super.accuracy = 90;
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            this.resetReady();
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void resetReady() {
            this.isCharging = !this.chargesFirst();
        }

        @Override
        public void switchReady() {
            this.isCharging = !this.isCharging;
        }
    }

    static class Splash extends Attack {
        private static final long serialVersionUID = 1L;

        Splash() {
            super(AttackNamesies.SPLASH, Type.NORMAL, MoveCategory.STATUS, 40, "The user just flops and splashes around to no effect at all...");
            super.moveTypes.add(MoveType.AIRBORNE);
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.selfTarget = true;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            Messages.add("But nothing happened...");
        }
    }

    static class Mist extends Attack {
        private static final long serialVersionUID = 1L;

        Mist() {
            super(AttackNamesies.MIST, Type.ICE, MoveCategory.STATUS, 30, "The user cloaks itself and its allies in a white mist that prevents any of their stats from being lowered for five turns.");
            super.effect = TeamEffectNamesies.MIST;
            super.selfTarget = true;
        }
    }

    static class LastResort extends Attack {
        private static final long serialVersionUID = 1L;

        LastResort() {
            super(AttackNamesies.LAST_RESORT, Type.NORMAL, MoveCategory.PHYSICAL, 5, "This move can be used only after the user has used all the other moves it knows in the battle.");
            super.power = 140;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            for (Move move : user.getMoves(b)) {
                if (move.getAttack().namesies() != this.namesies() && !move.used()) {
                    return false;
                }
            }

            return true;
        }
    }

    static class TrumpCard extends Attack {
        private static final long serialVersionUID = 1L;

        TrumpCard() {
            super(AttackNamesies.TRUMP_CARD, Type.NORMAL, MoveCategory.SPECIAL, 5, "The fewer PP this move has, the greater its power.");
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getPower(Battle b, ActivePokemon me, ActivePokemon o) {
            int pp = me.getMove().getPP();

            switch (pp) {
                case 1:
                    return 190;
                case 2:
                    return 75;
                case 3:
                    return 60;
                case 4:
                    return 50;
                default:
                    return 40;
            }
        }
    }

    static class MuddyWater extends Attack {
        private static final long serialVersionUID = 1L;

        MuddyWater() {
            super(AttackNamesies.MUDDY_WATER, Type.WATER, MoveCategory.SPECIAL, 10, "The user attacks by shooting muddy water at the opposing Pok\u00e9mon. This may also lower their accuracy.");
            super.power = 90;
            super.accuracy = 85;
            super.effectChance = 30;
            super.statChanges[Stat.ACCURACY.index()] = -1;
        }
    }

    static class Conversion extends Attack implements ChangeTypeSource {
        private static final long serialVersionUID = 1L;

        private Type type;

        Conversion() {
            super(AttackNamesies.CONVERSION, Type.NORMAL, MoveCategory.STATUS, 30, "The user changes its type to become the same type as the move at the top of the list of moves it knows.");
            super.effect = PokemonEffectNamesies.CHANGE_TYPE;
            super.selfTarget = true;
        }

        @Override
        public void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Change to the type of the first in the first slot
            this.type = attacking.getMoves(b).get(0).getAttack().getActualType();
        }

        @Override
        public PokeType getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return new PokeType(this.type);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return !user.isType(b, this.type);
        }
    }

    static class Conversion2 extends Attack implements ChangeTypeSource {
        private static final long serialVersionUID = 1L;

        private List<Type> types;

        Conversion2() {
            super(AttackNamesies.CONVERSION_2, Type.NORMAL, MoveCategory.STATUS, 30, "The user changes its type to make itself resistant to the type of the attack the opponent used last.");
            super.effect = PokemonEffectNamesies.CHANGE_TYPE;
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.selfTarget = true;
        }

        @Override
        public void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            this.types = new ArrayList<>();

            Move lastMoveUsed = b.getOtherPokemon(attacking).getLastMoveUsed();
            if (lastMoveUsed != null) {
                Type attackingType = lastMoveUsed.getType();
                for (Type type : Type.values()) {
                    if (attackingType.getAdvantage().isNotVeryEffective(type) && !attacking.isType(b, type)) {
                        this.types.add(type);
                    }
                }
            }
        }

        @Override
        public PokeType getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return new PokeType(RandomUtils.getRandomValue(this.types));
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return !this.types.isEmpty();
        }
    }

    static class Sharpen extends Attack {
        private static final long serialVersionUID = 1L;

        Sharpen() {
            super(AttackNamesies.SHARPEN, Type.NORMAL, MoveCategory.STATUS, 30, "The user makes its edges more jagged, which raises its Attack stat.");
            super.selfTarget = true;
            super.statChanges[Stat.ATTACK.index()] = 1;
        }
    }

    static class MagicCoat extends Attack {
        private static final long serialVersionUID = 1L;

        MagicCoat() {
            super(AttackNamesies.MAGIC_COAT, Type.PSYCHIC, MoveCategory.STATUS, 15, "A barrier reflects back to the target moves like Leech Seed and moves that damage status.");
            super.effect = PokemonEffectNamesies.MAGIC_COAT;
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.selfTarget = true;
            super.priority = 4;
        }
    }

    static class SkyDrop extends Attack {
        private static final long serialVersionUID = 1L;

        SkyDrop() {
            super(AttackNamesies.SKY_DROP, Type.FLYING, MoveCategory.PHYSICAL, 10, "The user takes the target into the sky, then slams it into the ground.");
            super.power = 60;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.AIRBORNE);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.getWeight(b) < 441;
        }
    }

    static class IronHead extends Attack {
        private static final long serialVersionUID = 1L;

        IronHead() {
            super(AttackNamesies.IRON_HEAD, Type.STEEL, MoveCategory.PHYSICAL, 15, "The user slams the target with its steel-hard head. This may also make the target flinch.");
            super.power = 80;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 30;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class RockSlide extends Attack {
        private static final long serialVersionUID = 1L;

        RockSlide() {
            super(AttackNamesies.ROCK_SLIDE, Type.ROCK, MoveCategory.PHYSICAL, 10, "Large boulders are hurled at the opposing Pok\u00e9mon to inflict damage. This may also make the opposing Pok\u00e9mon flinch.");
            super.power = 75;
            super.accuracy = 90;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 30;
        }
    }

    static class Snore extends Attack implements SleepyFightsterEffect {
        private static final long serialVersionUID = 1L;

        Snore() {
            super(AttackNamesies.SNORE, Type.NORMAL, MoveCategory.SPECIAL, 15, "This attack can be used only if the user is asleep. The harsh noise may also make the target flinch.");
            super.power = 50;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 30;
            super.moveTypes.add(MoveType.SOUND_BASED);
            super.moveTypes.add(MoveType.METRONOMELESS);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.hasStatus(StatusNamesies.ASLEEP);
        }
    }

    static class SleepTalk extends Attack implements SleepyFightsterEffect {
        private static final long serialVersionUID = 1L;

        private List<Move> moves;

        SleepTalk() {
            super(AttackNamesies.SLEEP_TALK, Type.NORMAL, MoveCategory.STATUS, 10, "While it is asleep, the user randomly uses one of the moves it knows.");
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
            super.moveTypes.add(MoveType.MIRRORLESS);
        }

        @Override
        public void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            this.moves = attacking.getMoves(b).filter(move -> !move.getAttack().isMoveType(MoveType.SLEEP_TALK_FAIL));
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.hasStatus(StatusNamesies.ASLEEP) && !this.moves.isEmpty();
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            user.callFullNewMove(b, victim, RandomUtils.getRandomValue(this.moves));
        }
    }

    static class Block extends Attack {
        private static final long serialVersionUID = 1L;

        Block() {
            super(AttackNamesies.BLOCK, Type.NORMAL, MoveCategory.STATUS, 5, "The user blocks the target's way with arms spread wide to prevent escape.");
            super.effect = PokemonEffectNamesies.TRAPPED;
        }
    }

    static class SkyAttack extends Attack implements CritStageEffect, ChargingMove {
        private static final long serialVersionUID = 1L;

        private boolean isCharging;

        SkyAttack() {
            super(AttackNamesies.SKY_ATTACK, Type.FLYING, MoveCategory.PHYSICAL, 5, "A second-turn attack move where critical hits land more easily. This may also make the target flinch.");
            super.power = 140;
            super.accuracy = 90;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 30;
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            this.resetReady();
        }

        @Override
        public String getChargeMessage(ActivePokemon user) {
            return user.getName() + " started glowing!";
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void resetReady() {
            this.isCharging = !this.chargesFirst();
        }

        @Override
        public void switchReady() {
            this.isCharging = !this.isCharging;
        }
    }

    static class DragonRush extends Attack implements DoubleMinimizerMove {
        private static final long serialVersionUID = 1L;

        DragonRush() {
            super(AttackNamesies.DRAGON_RUSH, Type.DRAGON, MoveCategory.PHYSICAL, 10, "The user tackles the target while exhibiting overwhelming menace. This may also make the target flinch.");
            super.power = 100;
            super.accuracy = 75;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 20;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class AuraSphere extends Attack {
        private static final long serialVersionUID = 1L;

        AuraSphere() {
            super(AttackNamesies.AURA_SPHERE, Type.FIGHTING, MoveCategory.SPECIAL, 20, "The user lets loose a blast of aura power from deep within its body at the target. This attack never misses.");
            super.power = 80;
            super.moveTypes.add(MoveType.BOMB_BALL);
            super.moveTypes.add(MoveType.AURA_PULSE);
        }
    }

    static class Psystrike extends Attack implements OpponentStatSwitchingEffect {
        private static final long serialVersionUID = 1L;

        Psystrike() {
            super(AttackNamesies.PSYSTRIKE, Type.PSYCHIC, MoveCategory.SPECIAL, 10, "The user materializes an odd psychic wave to attack the target. This attack does physical damage.");
            super.power = 100;
            super.accuracy = 100;
        }

        @Override
        public Stat getSwitchStat(Stat s) {
            return s == Stat.SP_DEFENSE ? Stat.DEFENSE : s;
        }
    }

    static class Eruption extends Attack {
        private static final long serialVersionUID = 1L;

        Eruption() {
            super(AttackNamesies.ERUPTION, Type.FIRE, MoveCategory.SPECIAL, 5, "The user attacks opposing Pok\u00e9mon with explosive fury. The lower the user's HP, the lower the move's power.");
            super.power = 150;
            super.accuracy = 100;
        }

        @Override
        public int getPower(Battle b, ActivePokemon me, ActivePokemon o) {
            return (int)Math.min(1, (super.power*me.getHPRatio()));
        }
    }

    static class Charm extends Attack {
        private static final long serialVersionUID = 1L;

        Charm() {
            super(AttackNamesies.CHARM, Type.FAIRY, MoveCategory.STATUS, 20, "The user gazes at the target rather charmingly, making it less wary. This harshly lowers its Attack stat.");
            super.accuracy = 100;
            super.statChanges[Stat.ATTACK.index()] = -2;
        }
    }

    static class EchoedVoice extends Attack implements PowerCountMove {
        private static final long serialVersionUID = 1L;

        EchoedVoice() {
            super(AttackNamesies.ECHOED_VOICE, Type.NORMAL, MoveCategory.SPECIAL, 15, "The user attacks the target with an echoing voice. If this move is used every turn, its power is increased.");
            super.power = 40;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.SOUND_BASED);
        }

        @Override
        public boolean doubleDefenseCurled() {
            return false;
        }
    }

    static class PsychoShift extends Attack {
        private static final long serialVersionUID = 1L;

        PsychoShift() {
            super(AttackNamesies.PSYCHO_SHIFT, Type.PSYCHIC, MoveCategory.STATUS, 10, "Using its psychic power of suggestion, the user transfers its status conditions to the target.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            // NOTE: The messaging looks off when combined with Lum Berry but I don't know if this can be easily fixed
            StatusNamesies toTransfer = user.getStatus().namesies();
            String transferMessage = user.getName() + " transferred its status condition to " + victim.getName() + "!";
            toTransfer.getStatus().apply(b, user, victim, transferMessage);
            user.removeStatus();
            Messages.add(new MessageUpdate().updatePokemon(b, user));
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.hasStatus() && user.getStatus().applies(b, user, victim).isSuccess();
        }
    }

    static class ShadowSneak extends Attack {
        private static final long serialVersionUID = 1L;

        ShadowSneak() {
            super(AttackNamesies.SHADOW_SNEAK, Type.GHOST, MoveCategory.PHYSICAL, 30, "The user extends its shadow and attacks the target from behind. This move always goes first.");
            super.power = 40;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.priority = 1;
        }
    }

    static class SpiderWeb extends Attack {
        private static final long serialVersionUID = 1L;

        SpiderWeb() {
            super(AttackNamesies.SPIDER_WEB, Type.BUG, MoveCategory.STATUS, 10, "The user ensnares the target with thin, gooey silk so it can't flee from battle.");
            super.effect = PokemonEffectNamesies.TRAPPED;
        }
    }

    static class SweetKiss extends Attack {
        private static final long serialVersionUID = 1L;

        SweetKiss() {
            super(AttackNamesies.SWEET_KISS, Type.FAIRY, MoveCategory.STATUS, 10, "The user kisses the target with a sweet, angelic cuteness that causes confusion.");
            super.accuracy = 75;
            super.effect = PokemonEffectNamesies.CONFUSION;
        }
    }

    static class OminousWind extends Attack {
        private static final long serialVersionUID = 1L;

        OminousWind() {
            super(AttackNamesies.OMINOUS_WIND, Type.GHOST, MoveCategory.SPECIAL, 5, "The user blasts the target with a gust of repulsive wind. This may also raise all the user's stats at once.");
            super.power = 60;
            super.accuracy = 100;
            super.effectChance = 10;
            super.selfTarget = true;
            super.statChanges[Stat.ATTACK.index()] = 1;
            super.statChanges[Stat.DEFENSE.index()] = 1;
            super.statChanges[Stat.SP_ATTACK.index()] = 1;
            super.statChanges[Stat.SP_DEFENSE.index()] = 1;
            super.statChanges[Stat.SPEED.index()] = 1;
        }
    }

    static class CottonSpore extends Attack implements PowderMove {
        private static final long serialVersionUID = 1L;

        CottonSpore() {
            super(AttackNamesies.COTTON_SPORE, Type.GRASS, MoveCategory.STATUS, 40, "The user releases cotton-like spores that cling to the opposing Pok\u00e9mon, which harshly lowers their Speed stat.");
            super.accuracy = 100;
            super.statChanges[Stat.SPEED.index()] = -2;
        }
    }

    static class CottonGuard extends Attack {
        private static final long serialVersionUID = 1L;

        CottonGuard() {
            super(AttackNamesies.COTTON_GUARD, Type.GRASS, MoveCategory.STATUS, 10, "The user protects itself by wrapping its body in soft cotton, which drastically raises the user's Defense stat.");
            super.selfTarget = true;
            super.statChanges[Stat.DEFENSE.index()] = 3;
        }
    }

    static class GrassWhistle extends Attack {
        private static final long serialVersionUID = 1L;

        GrassWhistle() {
            super(AttackNamesies.GRASS_WHISTLE, Type.GRASS, MoveCategory.STATUS, 15, "The user plays a pleasant melody that lulls the target into a deep sleep.");
            super.accuracy = 55;
            super.status = StatusNamesies.ASLEEP;
            super.moveTypes.add(MoveType.SOUND_BASED);
        }
    }

    static class Torment extends Attack {
        private static final long serialVersionUID = 1L;

        Torment() {
            super(AttackNamesies.TORMENT, Type.DARK, MoveCategory.STATUS, 15, "The user torments and enrages the target, making it incapable of using the same move twice in a row.");
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.TORMENT;
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
        }
    }

    static class HiddenPower extends Attack {
        private static final long serialVersionUID = 1L;

        HiddenPower() {
            super(AttackNamesies.HIDDEN_POWER, Type.NORMAL, MoveCategory.SPECIAL, 15, "A unique attack that varies in type depending on the Pok\u00e9mon using it.");
            super.power = 60;
            super.accuracy = 100;
        }

        @Override
        public Type getType(Battle b, ActivePokemon user) {
            return user.computeHiddenPowerType();
        }
    }

    static class Psywave extends Attack implements FixedDamageMove {
        private static final long serialVersionUID = 1L;

        Psywave() {
            super(AttackNamesies.PSYWAVE, Type.PSYCHIC, MoveCategory.SPECIAL, 15, "The target is attacked with an odd psychic wave. The attack varies in intensity.");
            super.accuracy = 100;
        }

        @Override
        public int getFixedDamageAmount(ActivePokemon me, ActivePokemon o) {
            return (int)Math.max(1, (RandomUtils.getRandomInt(11) + 5)*me.getLevel()/10.0);
        }
    }

    static class PainSplit extends Attack {
        private static final long serialVersionUID = 1L;

        PainSplit() {
            super(AttackNamesies.PAIN_SPLIT, Type.NORMAL, MoveCategory.STATUS, 20, "The user adds its HP to the target's HP, then equally shares the combined HP with the target.");
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            int share = (user.getHP() + victim.getHP())/2;
            user.setHP(share);
            victim.setHP(share);

            Messages.add(new MessageUpdate(user.getName() + " and " + victim.getName() + " split their pain!").updatePokemon(b, user));
            Messages.add(new MessageUpdate().updatePokemon(b, victim));
        }
    }

    static class Bide extends Attack {
        private static final long serialVersionUID = 1L;

        Bide() {
            super(AttackNamesies.BIDE, Type.NORMAL, MoveCategory.PHYSICAL, 10, "The user endures attacks for two turns, then strikes back to cause double the damage taken.");
            super.effect = PokemonEffectNamesies.BIDE;
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            super.moveTypes.add(MoveType.MIRRORLESS);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.selfTarget = true;
            super.priority = 1;
        }

        @Override
        public boolean shouldApplyDamage(Battle b, ActivePokemon user) {
            return false;
        }
    }

    static class Autotomize extends Attack {
        private static final long serialVersionUID = 1L;

        Autotomize() {
            super(AttackNamesies.AUTOTOMIZE, Type.STEEL, MoveCategory.STATUS, 15, "The user sheds part of its body to make itself lighter and sharply raise its Speed stat.");
            super.effect = PokemonEffectNamesies.HALF_WEIGHT;
            super.selfTarget = true;
            super.statChanges[Stat.SPEED.index()] = 2;
        }
    }

    static class StruggleBug extends Attack {
        private static final long serialVersionUID = 1L;

        StruggleBug() {
            super(AttackNamesies.STRUGGLE_BUG, Type.BUG, MoveCategory.SPECIAL, 20, "While resisting, the user attacks the opposing Pok\u00e9mon. This lowers the Sp. Atk stat of those hit.");
            super.power = 50;
            super.accuracy = 100;
            super.statChanges[Stat.SP_ATTACK.index()] = -1;
        }
    }

    static class PowerTrick extends Attack {
        private static final long serialVersionUID = 1L;

        PowerTrick() {
            super(AttackNamesies.POWER_TRICK, Type.PSYCHIC, MoveCategory.STATUS, 10, "The user employs its psychic power to switch its Attack stat with its Defense stat.");
            super.effect = PokemonEffectNamesies.POWER_TRICK;
            super.selfTarget = true;
        }
    }

    static class PowerSplit extends Attack {
        private static final long serialVersionUID = 1L;

        PowerSplit() {
            super(AttackNamesies.POWER_SPLIT, Type.PSYCHIC, MoveCategory.STATUS, 10, "The user employs its psychic power to average its Attack and Sp. Atk stats with those of the target.");
            super.effect = StandardBattleEffectNamesies.POWER_SPLIT;
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.FIELD);
        }
    }

    static class GuardSplit extends Attack {
        private static final long serialVersionUID = 1L;

        GuardSplit() {
            super(AttackNamesies.GUARD_SPLIT, Type.PSYCHIC, MoveCategory.STATUS, 10, "The user employs its psychic power to average its Defense and Sp. Def stats with those of the target.");
            super.effect = StandardBattleEffectNamesies.GUARD_SPLIT;
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.FIELD);
        }
    }

    static class HoneClaws extends Attack {
        private static final long serialVersionUID = 1L;

        HoneClaws() {
            super(AttackNamesies.HONE_CLAWS, Type.DARK, MoveCategory.STATUS, 15, "The user sharpens its claws to boost its Attack stat and accuracy.");
            super.selfTarget = true;
            super.statChanges[Stat.ATTACK.index()] = 1;
            super.statChanges[Stat.ACCURACY.index()] = 1;
        }
    }

    static class BeatUp extends Attack {
        private static final long serialVersionUID = 1L;

        BeatUp() {
            super(AttackNamesies.BEAT_UP, Type.DARK, MoveCategory.PHYSICAL, 10, "The user gets all party Pok\u00e9mon to attack the target. The more party Pok\u00e9mon, the greater the number of attacks.");
            super.power = 10;
            super.accuracy = 100;
        }

        @Override
        public String getPowerString() {
            return "--";
        }

        @Override
        public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
            for (ActivePokemon p : b.getTrainer(me).getActiveTeam()) {
                // Only healthy Pokemon get to attack
                if (!p.canFight() || p.hasStatus()) {
                    continue;
                }

                // Stop killing the dead
                if (o.isFainted(b)) {
                    break;
                }

                p.callTempMove(this.namesies(), () -> {
                    Messages.add(p.getName() + "'s attack!");
                    super.applyDamage(p, o, b);
                });
            }
        }
    }

    static class Octazooka extends Attack {
        private static final long serialVersionUID = 1L;

        Octazooka() {
            super(AttackNamesies.OCTAZOOKA, Type.WATER, MoveCategory.SPECIAL, 10, "The user attacks by spraying ink in the target's face or eyes. This may also lower the target's accuracy.");
            super.power = 65;
            super.accuracy = 85;
            super.effectChance = 50;
            super.moveTypes.add(MoveType.BOMB_BALL);
            super.statChanges[Stat.ACCURACY.index()] = -1;
        }
    }

    static class Present extends Attack {
        private static final long serialVersionUID = 1L;

        private boolean applyDamage;

        Present() {
            super(AttackNamesies.PRESENT, Type.NORMAL, MoveCategory.PHYSICAL, 15, "The user attacks by giving the target a gift with a hidden trap. It restores HP sometimes, however.");
            super.accuracy = 90;
        }

        @Override
        public int getPower(Battle b, ActivePokemon me, ActivePokemon o) {
            double random = RandomUtils.getRandomInt(80);
            if (random < 40) {
                return 40;
            } else if (random < 70) {
                return 80;
            } else {
                return 120;
            }
        }

        @Override
        public void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            this.applyDamage = RandomUtils.chanceTest(80);
        }

        @Override
        public boolean shouldApplyDamage(Battle b, ActivePokemon user) {
            return this.applyDamage;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (!applyDamage) {
                victim.healHealthFraction(1/4.0, b, victim.getName() + "'s health was restored!");
            }
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return this.applyDamage || victim.canHeal();
        }
    }

    static class SteelWing extends Attack {
        private static final long serialVersionUID = 1L;

        SteelWing() {
            super(AttackNamesies.STEEL_WING, Type.STEEL, MoveCategory.PHYSICAL, 25, "The target is hit with wings of steel. This may also raise the user's Defense stat.");
            super.power = 70;
            super.accuracy = 90;
            super.effectChance = 10;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.selfTarget = true;
            super.statChanges[Stat.DEFENSE.index()] = 1;
        }
    }

    // Fails if trying to copy Sketch, Struggle, or a move you already know or if used by a Transformed Pokemon or you don't actually know Sketch
    static class Sketch extends Attack {
        private static final long serialVersionUID = 1L;

        private AttackNamesies copy;

        Sketch() {
            super(AttackNamesies.SKETCH, Type.NORMAL, MoveCategory.STATUS, 1, "It enables the user to permanently learn the move last used by the target. Once used, Sketch disappears.");
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            super.moveTypes.add(MoveType.ENCORELESS);
            super.moveTypes.add(MoveType.MIMICLESS);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.MIRRORLESS);
        }

        @Override
        public void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            Move lastMoveUsed = defending.getLastMoveUsed();
            this.copy = lastMoveUsed == null ? null : lastMoveUsed.getAttack().namesies();
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            MoveList moves = user.getMoves(b);
            for (int i = 0; i < moves.size(); i++) {
                if (moves.get(i).getAttack().namesies() == this.namesies()) {
                    moves.set(new Move(this.copy), i);
                    Messages.add(user.getName() + " learned " + moves.get(i).getAttack().getName() + "!");
                    break;
                }
            }
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return copy != null && copy != this.namesies() && copy != AttackNamesies.STRUGGLE && !user.hasActualMove(copy) && user.hasActualMove(this.namesies()) && !user.hasEffect(PokemonEffectNamesies.TRANSFORMED);
        }
    }

    static class TripleKick extends Attack implements MultiStrikeMove {
        private static final long serialVersionUID = 1L;

        TripleKick() {
            super(AttackNamesies.TRIPLE_KICK, Type.FIGHTING, MoveCategory.PHYSICAL, 10, "A consecutive three-kick attack that becomes more powerful with each successive hit.");
            super.power = 20;
            super.accuracy = 90;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getMinHits() {
            return 3;
        }

        @Override
        public int getMaxHits() {
            return 3;
        }
    }

    static class MilkDrink extends Attack implements SelfHealingMove {
        private static final long serialVersionUID = 1L;

        MilkDrink() {
            super(AttackNamesies.MILK_DRINK, Type.NORMAL, MoveCategory.STATUS, 10, "The user restores its own HP by up to half of its max HP.");
            super.moveTypes.add(MoveType.HEALING);
            super.selfTarget = true;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            this.heal(b, victim);
        }

        @Override
        public double getHealFraction(Battle b, ActivePokemon victim) {
            return .5;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.canHeal();
        }
    }

    static class HealBell extends Attack {
        private static final long serialVersionUID = 1L;

        HealBell() {
            super(AttackNamesies.HEAL_BELL, Type.NORMAL, MoveCategory.STATUS, 5, "The user makes a soothing bell chime to heal the status conditions of all the party Pok\u00e9mon.");
            super.moveTypes.add(MoveType.SOUND_BASED);
            super.moveTypes.add(MoveType.HEALING);
            super.selfTarget = true;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            for (ActivePokemon p : b.getTrainer(user).getActiveTeam()) {
                if (!p.isActuallyDead()) {
                    p.removeStatus();
                }
            }

            Messages.add("All status problems were cured!");
        }
    }

    static class WeatherBall extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        WeatherBall() {
            super(AttackNamesies.WEATHER_BALL, Type.NORMAL, MoveCategory.SPECIAL, 10, "This attack move varies in power and type depending on the weather.");
            super.power = 50;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.BOMB_BALL);
        }

        @Override
        public Type getType(Battle b, ActivePokemon user) {
            return b.getWeather().getElement();
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return b.getWeather().namesies() != WeatherNamesies.CLEAR_SKIES ? 2 : 1;
        }
    }

    static class Aeroblast extends Attack implements CritStageEffect {
        private static final long serialVersionUID = 1L;

        Aeroblast() {
            super(AttackNamesies.AEROBLAST, Type.FLYING, MoveCategory.SPECIAL, 5, "A vortex of air is shot at the target to inflict damage. Critical hits land more easily.");
            super.power = 100;
            super.accuracy = 95;
        }
    }

    static class SacredFire extends Attack {
        private static final long serialVersionUID = 1L;

        SacredFire() {
            super(AttackNamesies.SACRED_FIRE, Type.FIRE, MoveCategory.PHYSICAL, 5, "The target is razed with a mystical fire of great intensity. This may also leave the target with a burn.");
            super.power = 100;
            super.accuracy = 95;
            super.effectChance = 50;
            super.status = StatusNamesies.BURNED;
            super.moveTypes.add(MoveType.DEFROST);
        }
    }

    static class HealBlock extends Attack {
        private static final long serialVersionUID = 1L;

        HealBlock() {
            super(AttackNamesies.HEAL_BLOCK, Type.PSYCHIC, MoveCategory.STATUS, 15, "For five turns, the user prevents the opposing team from using any moves, Abilities, or held items that recover HP.");
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.HEAL_BLOCK;
        }
    }

    static class EnergyBall extends Attack {
        private static final long serialVersionUID = 1L;

        EnergyBall() {
            super(AttackNamesies.ENERGY_BALL, Type.GRASS, MoveCategory.SPECIAL, 10, "The user draws power from nature and fires it at the target. This may also lower the target's Sp. Def stat.");
            super.power = 90;
            super.accuracy = 100;
            super.effectChance = 10;
            super.moveTypes.add(MoveType.BOMB_BALL);
            super.statChanges[Stat.SP_DEFENSE.index()] = -1;
        }
    }

    static class BulkUp extends Attack {
        private static final long serialVersionUID = 1L;

        BulkUp() {
            super(AttackNamesies.BULK_UP, Type.FIGHTING, MoveCategory.STATUS, 20, "The user tenses its muscles to bulk up its body, raising both its Attack and Defense stats.");
            super.selfTarget = true;
            super.statChanges[Stat.ATTACK.index()] = 1;
            super.statChanges[Stat.DEFENSE.index()] = 1;
        }
    }

    static class Thief extends Attack implements ItemSwapperEffect {
        private static final long serialVersionUID = 1L;

        Thief() {
            super(AttackNamesies.THIEF, Type.DARK, MoveCategory.PHYSICAL, 25, "The user attacks and steals the target's held item simultaneously. The user can't steal anything if it already holds an item.");
            super.power = 60;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.canStealItem(b, victim)) {
                this.swapItems(b, user, victim);
            }
        }

        @Override
        public String getSwitchMessage(ActivePokemon user, HoldItem userItem, ActivePokemon victim, HoldItem victimItem) {
            return user.getName() + " stole " + victim.getName() + "'s " + victimItem.getName() + "!";
        }
    }

    static class Attract extends Attack {
        private static final long serialVersionUID = 1L;

        Attract() {
            super(AttackNamesies.ATTRACT, Type.NORMAL, MoveCategory.STATUS, 15, "If it is the opposite gender of the user, the target becomes infatuated and less likely to attack.");
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.INFATUATION;
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
        }
    }

    static class ForcePalm extends Attack {
        private static final long serialVersionUID = 1L;

        ForcePalm() {
            super(AttackNamesies.FORCE_PALM, Type.FIGHTING, MoveCategory.PHYSICAL, 10, "The target is attacked with a shock wave. This may also leave the target with paralysis.");
            super.power = 60;
            super.accuracy = 100;
            super.effectChance = 30;
            super.status = StatusNamesies.PARALYZED;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class ArmThrust extends Attack implements MultiStrikeMove {
        private static final long serialVersionUID = 1L;

        ArmThrust() {
            super(AttackNamesies.ARM_THRUST, Type.FIGHTING, MoveCategory.PHYSICAL, 20, "The user lets loose a flurry of open-palmed arm thrusts that hit two to five times in a row.");
            super.power = 15;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getMinHits() {
            return 2;
        }

        @Override
        public int getMaxHits() {
            return 5;
        }
    }

    static class SmellingSalts extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        SmellingSalts() {
            super(AttackNamesies.SMELLING_SALTS, Type.NORMAL, MoveCategory.PHYSICAL, 10, "This attack's power is doubled when used on a target with paralysis. This also cures the target's paralysis, however.");
            super.power = 70;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (victim.hasStatus(StatusNamesies.PARALYZED)) {
                victim.removeStatus(b, CastSource.ATTACK);
            }
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.hasStatus(StatusNamesies.PARALYZED) ? 2 : 1;
        }
    }

    static class Assist extends Attack {
        private static final long serialVersionUID = 1L;

        private List<AttackNamesies> attacks;

        Assist() {
            super(AttackNamesies.ASSIST, Type.NORMAL, MoveCategory.STATUS, 20, "The user hurriedly and randomly uses a move among those known by ally Pok\u00e9mon.");
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
            super.moveTypes.add(MoveType.MIRRORLESS);
        }

        @Override
        public void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            this.attacks = new ArrayList<>();
            for (ActivePokemon p : b.getTrainer(attacking).getActiveTeam()) {
                if (p != attacking) {
                    for (Move move : p.getMoves(b)) {
                        Attack attack = move.getAttack();
                        if (!attack.isMoveType(MoveType.ASSISTLESS)) {
                            attacks.add(attack.namesies());
                        }
                    }
                }
            }
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return !this.attacks.isEmpty();
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            user.callFullNewMove(b, victim, RandomUtils.getRandomValue(attacks));
        }
    }

    static class MetalBurst extends Attack implements FixedDamageMove {
        private static final long serialVersionUID = 1L;

        MetalBurst() {
            super(AttackNamesies.METAL_BURST, Type.STEEL, MoveCategory.PHYSICAL, 10, "The user retaliates with much greater force against the opponent that last inflicted damage on it.");
            super.accuracy = 100;
        }

        @Override
        public int getFixedDamageAmount(ActivePokemon me, ActivePokemon o) {
            return (int)(me.getDamageTaken()*1.5);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.getDamageTaken() > 0 && !b.isFirstAttack();
        }
    }

    static class WildCharge extends Attack implements RecoilPercentageMove {
        private static final long serialVersionUID = 1L;

        WildCharge() {
            super(AttackNamesies.WILD_CHARGE, Type.ELECTRIC, MoveCategory.PHYSICAL, 15, "The user shrouds itself in electricity and smashes into its target. This also damages the user a little.");
            super.power = 90;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getDamagePercentageDenominator() {
            return 4;
        }
    }

    static class Flash extends Attack {
        private static final long serialVersionUID = 1L;

        Flash() {
            super(AttackNamesies.FLASH, Type.NORMAL, MoveCategory.STATUS, 20, "The user flashes a bright light that cuts the target's accuracy.");
            super.accuracy = 100;
            super.statChanges[Stat.ACCURACY.index()] = -1;
        }
    }

    static class TailGlow extends Attack {
        private static final long serialVersionUID = 1L;

        TailGlow() {
            super(AttackNamesies.TAIL_GLOW, Type.BUG, MoveCategory.STATUS, 20, "The user stares at flashing lights to focus its mind, drastically raising its Sp. Atk stat.");
            super.selfTarget = true;
            super.statChanges[Stat.SP_ATTACK.index()] = 3;
        }
    }

    static class WaterSpout extends Attack {
        private static final long serialVersionUID = 1L;

        WaterSpout() {
            super(AttackNamesies.WATER_SPOUT, Type.WATER, MoveCategory.SPECIAL, 5, "The user spouts water to damage opposing Pok\u00e9mon. The lower the user's HP, the lower the move's power.");
            super.power = 150;
            super.accuracy = 100;
        }

        @Override
        public int getPower(Battle b, ActivePokemon me, ActivePokemon o) {
            return (int)Math.min(1, (super.power*me.getHPRatio()));
        }
    }

    static class TeeterDance extends Attack {
        private static final long serialVersionUID = 1L;

        TeeterDance() {
            super(AttackNamesies.TEETER_DANCE, Type.NORMAL, MoveCategory.STATUS, 20, "The user performs a wobbly dance that confuses the Pok\u00e9mon around it.");
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.CONFUSION;
            super.moveTypes.add(MoveType.DANCE);
        }
    }

    static class NeedleArm extends Attack {
        private static final long serialVersionUID = 1L;

        NeedleArm() {
            super(AttackNamesies.NEEDLE_ARM, Type.GRASS, MoveCategory.PHYSICAL, 15, "The user attacks by wildly swinging its thorny arms. This may also make the target flinch.");
            super.power = 60;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 30;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Venoshock extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Venoshock() {
            super(AttackNamesies.VENOSHOCK, Type.POISON, MoveCategory.SPECIAL, 10, "The user drenches the target in a special poisonous liquid. This move's power is doubled if the target is poisoned.");
            super.power = 65;
            super.accuracy = 100;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.hasStatus(StatusNamesies.POISONED) ? 2 : 1;
        }
    }

    static class Snatch extends Attack {
        private static final long serialVersionUID = 1L;

        Snatch() {
            super(AttackNamesies.SNATCH, Type.DARK, MoveCategory.STATUS, 10, "The user steals the effects of any attempts to use a healing or stat-changing move.");
            super.effect = PokemonEffectNamesies.SNATCH;
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.selfTarget = true;
            super.priority = 4;
        }
    }

    static class IceBall extends Attack implements PowerCountMove {
        private static final long serialVersionUID = 1L;

        IceBall() {
            super(AttackNamesies.ICE_BALL, Type.ICE, MoveCategory.PHYSICAL, 20, "The user attacks the target for five turns. The move's power increases each time it hits.");
            super.power = 30;
            super.accuracy = 90;
            super.moveTypes.add(MoveType.BOMB_BALL);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public boolean doubleDefenseCurled() {
            return true;
        }
    }

    static class HeadSmash extends Attack implements RecoilPercentageMove {
        private static final long serialVersionUID = 1L;

        HeadSmash() {
            super(AttackNamesies.HEAD_SMASH, Type.ROCK, MoveCategory.PHYSICAL, 5, "The user attacks the target with a hazardous, full-power headbutt. This also damages the user terribly.");
            super.power = 150;
            super.accuracy = 80;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getDamagePercentageDenominator() {
            return 2;
        }
    }

    static class MistBall extends Attack {
        private static final long serialVersionUID = 1L;

        MistBall() {
            super(AttackNamesies.MIST_BALL, Type.PSYCHIC, MoveCategory.SPECIAL, 5, "A mist-like flurry of down envelops and damages the target. This may also lower the target's Sp. Atk stat.");
            super.power = 70;
            super.accuracy = 100;
            super.effectChance = 50;
            super.moveTypes.add(MoveType.BOMB_BALL);
            super.statChanges[Stat.SP_ATTACK.index()] = -1;
        }
    }

    static class LusterPurge extends Attack {
        private static final long serialVersionUID = 1L;

        LusterPurge() {
            super(AttackNamesies.LUSTER_PURGE, Type.PSYCHIC, MoveCategory.SPECIAL, 5, "The user lets loose a damaging burst of light. This may also lower the target's Sp. Def stat.");
            super.power = 70;
            super.accuracy = 100;
            super.effectChance = 50;
            super.statChanges[Stat.SP_DEFENSE.index()] = -1;
        }
    }

    static class PsychoBoost extends Attack {
        private static final long serialVersionUID = 1L;

        PsychoBoost() {
            super(AttackNamesies.PSYCHO_BOOST, Type.PSYCHIC, MoveCategory.SPECIAL, 5, "The user attacks the target at full power. The attack's recoil harshly lowers the user's Sp. Atk stat.");
            super.power = 140;
            super.accuracy = 90;
            super.selfTarget = true;
            super.statChanges[Stat.SP_ATTACK.index()] = -2;
        }
    }

    static class Facade extends Attack implements PowerStatusBoosterEffect {
        private static final long serialVersionUID = 1L;

        Facade() {
            super(AttackNamesies.FACADE, Type.NORMAL, MoveCategory.PHYSICAL, 20, "This attack move doubles its power if the user is poisoned, burned, or paralyzed.");
            super.power = 70;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public double getStatusBoost() {
            return 2;
        }
    }

    static class DefendOrder extends Attack {
        private static final long serialVersionUID = 1L;

        DefendOrder() {
            super(AttackNamesies.DEFEND_ORDER, Type.BUG, MoveCategory.STATUS, 10, "The user calls out its underlings to shield its body, raising its Defense and Sp. Def stats.");
            super.selfTarget = true;
            super.statChanges[Stat.DEFENSE.index()] = 1;
            super.statChanges[Stat.SP_DEFENSE.index()] = 1;
        }
    }

    static class HealOrder extends Attack implements SelfHealingMove {
        private static final long serialVersionUID = 1L;

        HealOrder() {
            super(AttackNamesies.HEAL_ORDER, Type.BUG, MoveCategory.STATUS, 10, "The user calls out its underlings to heal it. The user regains up to half of its max HP.");
            super.moveTypes.add(MoveType.HEALING);
            super.selfTarget = true;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            this.heal(b, victim);
        }

        @Override
        public double getHealFraction(Battle b, ActivePokemon victim) {
            return .5;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.canHeal();
        }
    }

    static class AttackOrder extends Attack implements CritStageEffect {
        private static final long serialVersionUID = 1L;

        AttackOrder() {
            super(AttackNamesies.ATTACK_ORDER, Type.BUG, MoveCategory.PHYSICAL, 15, "The user calls out its underlings to pummel the target. Critical hits land more easily.");
            super.power = 90;
            super.accuracy = 100;
        }
    }

    static class Chatter extends Attack {
        private static final long serialVersionUID = 1L;

        Chatter() {
            super(AttackNamesies.CHATTER, Type.FLYING, MoveCategory.SPECIAL, 20, "The user attacks the target with sound waves of deafening chatter. This confuses the target.");
            super.power = 65;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.CONFUSION;
            super.moveTypes.add(MoveType.SOUND_BASED);
        }
    }

    static class DualChop extends Attack implements MultiStrikeMove {
        private static final long serialVersionUID = 1L;

        DualChop() {
            super(AttackNamesies.DUAL_CHOP, Type.DRAGON, MoveCategory.PHYSICAL, 15, "The user attacks its target by hitting it with brutal strikes. The target is hit twice in a row.");
            super.power = 40;
            super.accuracy = 90;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getMinHits() {
            return 2;
        }

        @Override
        public int getMaxHits() {
            return 2;
        }
    }

    static class RockWrecker extends Attack implements RechargingMove {
        private static final long serialVersionUID = 1L;

        private boolean isCharging;

        RockWrecker() {
            super(AttackNamesies.ROCK_WRECKER, Type.ROCK, MoveCategory.PHYSICAL, 5, "The user launches a huge boulder at the target to attack. The user can't move on the next turn.");
            super.power = 150;
            super.accuracy = 90;
            super.moveTypes.add(MoveType.BOMB_BALL);
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            this.resetReady();
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void resetReady() {
            this.isCharging = !this.chargesFirst();
        }

        @Override
        public void switchReady() {
            this.isCharging = !this.isCharging;
        }
    }

    static class TrickRoom extends Attack {
        private static final long serialVersionUID = 1L;

        TrickRoom() {
            super(AttackNamesies.TRICK_ROOM, Type.PSYCHIC, MoveCategory.STATUS, 5, "The user creates a bizarre area in which slower Pok\u00e9mon get to move first for five turns.");
            super.effect = StandardBattleEffectNamesies.TRICK_ROOM;
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.FIELD);
            super.priority = -7;
        }
    }

    static class RoarOfTime extends Attack implements RechargingMove {
        private static final long serialVersionUID = 1L;

        private boolean isCharging;

        RoarOfTime() {
            super(AttackNamesies.ROAR_OF_TIME, Type.DRAGON, MoveCategory.SPECIAL, 5, "The user blasts the target with power that distorts even time. The user can't move on the next turn.");
            super.power = 150;
            super.accuracy = 90;
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            this.resetReady();
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void resetReady() {
            this.isCharging = !this.chargesFirst();
        }

        @Override
        public void switchReady() {
            this.isCharging = !this.isCharging;
        }
    }

    static class SpacialRend extends Attack implements CritStageEffect {
        private static final long serialVersionUID = 1L;

        SpacialRend() {
            super(AttackNamesies.SPACIAL_REND, Type.DRAGON, MoveCategory.SPECIAL, 5, "The user tears the target along with the space around it. Critical hits land more easily.");
            super.power = 100;
            super.accuracy = 95;
        }
    }

    static class MagmaStorm extends Attack {
        private static final long serialVersionUID = 1L;

        MagmaStorm() {
            super(AttackNamesies.MAGMA_STORM, Type.FIRE, MoveCategory.SPECIAL, 5, "The target becomes trapped within a maelstrom of fire that rages for four to five turns.");
            super.power = 100;
            super.accuracy = 75;
            super.effect = PokemonEffectNamesies.MAGMA_STORM;
        }
    }

    static class CrushGrip extends Attack {
        private static final long serialVersionUID = 1L;

        CrushGrip() {
            super(AttackNamesies.CRUSH_GRIP, Type.NORMAL, MoveCategory.PHYSICAL, 5, "The target is crushed with great force. The more HP the target has left, the greater this move's power.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getPower(Battle b, ActivePokemon me, ActivePokemon o) {
            return (int)Math.min(1, (120*o.getHPRatio()));
        }
    }

    static class ShadowForce extends Attack implements SemiInvulnerableMove {
        private static final long serialVersionUID = 1L;

        private boolean isCharging;

        ShadowForce() {
            super(AttackNamesies.SHADOW_FORCE, Type.GHOST, MoveCategory.PHYSICAL, 5, "The user disappears, then strikes the target on the next turn. This move hits even if the target protects itself.");
            super.power = 120;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            this.resetReady();
        }

        @Override
        public boolean isOverground() {
            return false;
        }

        @Override
        public String getChargeMessage(ActivePokemon user) {
            return user.getName() + " disappeared!";
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void resetReady() {
            this.isCharging = !this.chargesFirst();
        }

        @Override
        public void switchReady() {
            this.isCharging = !this.isCharging;
        }
    }

    static class HeartSwap extends Attack {
        private static final long serialVersionUID = 1L;

        HeartSwap() {
            super(AttackNamesies.HEART_SWAP, Type.PSYCHIC, MoveCategory.STATUS, 10, "The user employs its psychic power to switch stat changes with the target.");
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            for (Stat stat : Stat.BATTLE_STATS) {
                user.getStages().swapStages(stat, victim);
            }

            Messages.add(user.getName() + " swapped its stats with " + victim.getName() + "!");
        }
    }

    static class DarkVoid extends Attack {
        private static final long serialVersionUID = 1L;

        DarkVoid() {
            super(AttackNamesies.DARK_VOID, Type.DARK, MoveCategory.STATUS, 10, "Opposing Pok\u00e9mon are dragged into a world of total darkness that makes them sleep.");
            super.accuracy = 50;
            super.status = StatusNamesies.ASLEEP;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isPokemon(PokemonNamesies.DARKRAI);
        }
    }

    static class SeedFlare extends Attack {
        private static final long serialVersionUID = 1L;

        SeedFlare() {
            super(AttackNamesies.SEED_FLARE, Type.GRASS, MoveCategory.SPECIAL, 5, "The user emits a shock wave from its body to attack its target. This may also harshly lower the target's Sp. Def stat.");
            super.power = 120;
            super.accuracy = 85;
            super.effectChance = 40;
            super.statChanges[Stat.SP_DEFENSE.index()] = -2;
        }
    }

    static class Judgement extends Attack {
        private static final long serialVersionUID = 1L;

        Judgement() {
            super(AttackNamesies.JUDGEMENT, Type.NORMAL, MoveCategory.SPECIAL, 10, "The user releases countless shots of light at the target. This move's type varies depending on the kind of Plate the user is holding.");
            super.power = 100;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.METRONOMELESS);
        }

        @Override
        public Type getType(Battle b, ActivePokemon user) {
            HoldItem item = user.getHeldItem(b);
            if (item instanceof PlateItem) {
                return ((PlateItem)item).getType();
            }

            return super.type;
        }
    }

    static class SearingShot extends Attack {
        private static final long serialVersionUID = 1L;

        SearingShot() {
            super(AttackNamesies.SEARING_SHOT, Type.FIRE, MoveCategory.SPECIAL, 5, "The user torches everything around it in an inferno of scarlet flames. This may also leave those it hits with a burn.");
            super.power = 100;
            super.accuracy = 100;
            super.effectChance = 30;
            super.status = StatusNamesies.BURNED;
            super.moveTypes.add(MoveType.BOMB_BALL);
        }
    }

    static class Incinerate extends Attack {
        private static final long serialVersionUID = 1L;

        Incinerate() {
            super(AttackNamesies.INCINERATE, Type.FIRE, MoveCategory.SPECIAL, 15, "The user attacks opposing Pok\u00e9mon with fire. If a Pok\u00e9mon is holding a certain item, such as a Berry, the item becomes burned up and unusable.");
            super.power = 60;
            super.accuracy = 100;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            HoldItem heldItem = victim.getHeldItem(b);
            if ((heldItem instanceof Berry || heldItem instanceof GemItem) && !StickyHoldEffect.containsStickyHoldEffect(b, user, victim)) {
                Messages.add(victim.getName() + "'s " + heldItem.getName() + " was burned!");
                heldItem.consumeItemWithoutEffects(b, victim);
            }
        }
    }

    static class Overheat extends Attack {
        private static final long serialVersionUID = 1L;

        Overheat() {
            super(AttackNamesies.OVERHEAT, Type.FIRE, MoveCategory.SPECIAL, 5, "The user attacks the target at full power. The attack's recoil harshly lowers the user's Sp. Atk stat.");
            super.power = 130;
            super.accuracy = 90;
            super.selfTarget = true;
            super.statChanges[Stat.SP_ATTACK.index()] = -2;
        }
    }

    static class HeatCrash extends Attack implements DoubleMinimizerMove {
        private static final long serialVersionUID = 1L;

        HeatCrash() {
            super(AttackNamesies.HEAT_CRASH, Type.FIRE, MoveCategory.PHYSICAL, 10, "The user slams its target with its flame-covered body. The more the user outweighs the target, the greater the move's power.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getPower(Battle b, ActivePokemon me, ActivePokemon o) {
            double ratio = o.getWeight(b)/me.getWeight(b);
            if (ratio > .5) {
                return 40;
            } else if (ratio > .33) {
                return 60;
            } else if (ratio > .25) {
                return 80;
            } else if (ratio > .2) {
                return 100;
            } else {
                return 120;
            }
        }
    }

    static class GrassKnot extends Attack {
        private static final long serialVersionUID = 1L;

        GrassKnot() {
            super(AttackNamesies.GRASS_KNOT, Type.GRASS, MoveCategory.SPECIAL, 20, "The user snares the target with grass and trips it. The heavier the target, the greater the move's power.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getPower(Battle b, ActivePokemon me, ActivePokemon o) {
            double weight = o.getWeight(b);
            if (weight < 22) {
                return 20;
            } else if (weight < 55) {
                return 40;
            } else if (weight < 110) {
                return 60;
            } else if (weight < 220) {
                return 80;
            } else if (weight < 440) {
                return 100;
            } else {
                return 120;
            }
        }
    }

    static class Scald extends Attack {
        private static final long serialVersionUID = 1L;

        Scald() {
            super(AttackNamesies.SCALD, Type.WATER, MoveCategory.SPECIAL, 15, "The user shoots boiling hot water at its target. This may also leave the target with a burn.");
            super.power = 80;
            super.accuracy = 100;
            super.effectChance = 30;
            super.status = StatusNamesies.BURNED;
            super.moveTypes.add(MoveType.DEFROST);
        }
    }

    static class DrainPunch extends Attack implements SapHealthMove {
        private static final long serialVersionUID = 1L;

        DrainPunch() {
            super(AttackNamesies.DRAIN_PUNCH, Type.FIGHTING, MoveCategory.PHYSICAL, 10, "An energy-draining punch. The user's HP is restored by half the damage taken by the target.");
            super.power = 75;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PUNCHING);
            super.moveTypes.add(MoveType.HEALING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class StormThrow extends Attack implements AlwaysCritEffect {
        private static final long serialVersionUID = 1L;

        StormThrow() {
            super(AttackNamesies.STORM_THROW, Type.FIGHTING, MoveCategory.PHYSICAL, 10, "The user strikes the target with a fierce blow. This attack always results in a critical hit.");
            super.power = 60;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class FrostBreath extends Attack implements AlwaysCritEffect {
        private static final long serialVersionUID = 1L;

        FrostBreath() {
            super(AttackNamesies.FROST_BREATH, Type.ICE, MoveCategory.SPECIAL, 10, "The user blows its cold breath on the target. This attack always results in a critical hit.");
            super.power = 60;
            super.accuracy = 90;
        }
    }

    static class RockSmash extends Attack {
        private static final long serialVersionUID = 1L;

        RockSmash() {
            super(AttackNamesies.ROCK_SMASH, Type.FIGHTING, MoveCategory.PHYSICAL, 15, "The user attacks with a punch. This may also lower the target's Defense stat.");
            super.power = 40;
            super.accuracy = 100;
            super.effectChance = 50;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.statChanges[Stat.DEFENSE.index()] = -1;
        }
    }

    static class RockClimb extends Attack {
        private static final long serialVersionUID = 1L;

        RockClimb() {
            super(AttackNamesies.ROCK_CLIMB, Type.NORMAL, MoveCategory.PHYSICAL, 20, "The user attacks the target by smashing into it with incredible force. This may also confuse the target.");
            super.power = 90;
            super.accuracy = 85;
            super.effect = PokemonEffectNamesies.CONFUSION;
            super.effectChance = 20;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class NightDaze extends Attack {
        private static final long serialVersionUID = 1L;

        NightDaze() {
            super(AttackNamesies.NIGHT_DAZE, Type.DARK, MoveCategory.SPECIAL, 10, "The user lets loose a pitch-black shock wave at its target. This may also lower the target's accuracy.");
            super.power = 85;
            super.accuracy = 95;
            super.effectChance = 40;
            super.statChanges[Stat.ACCURACY.index()] = -1;
        }
    }

    static class TailSlap extends Attack implements MultiStrikeMove {
        private static final long serialVersionUID = 1L;

        TailSlap() {
            super(AttackNamesies.TAIL_SLAP, Type.NORMAL, MoveCategory.PHYSICAL, 10, "The user attacks by striking the target with its hard tail. It hits the target two to five times in a row.");
            super.power = 25;
            super.accuracy = 85;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getMinHits() {
            return 2;
        }

        @Override
        public int getMaxHits() {
            return 5;
        }
    }

    static class Defog extends Attack {
        private static final long serialVersionUID = 1L;

        Defog() {
            super(AttackNamesies.DEFOG, Type.FLYING, MoveCategory.STATUS, 15, "A strong wind blows away the target's barriers such as Reflect or Light Screen. This also lowers the target's evasiveness.");
            super.statChanges[Stat.EVASION.index()] = -1;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            DefogRelease.release(b, victim);
        }
    }

    static class HornLeech extends Attack implements SapHealthMove {
        private static final long serialVersionUID = 1L;

        HornLeech() {
            super(AttackNamesies.HORN_LEECH, Type.GRASS, MoveCategory.PHYSICAL, 10, "The user drains the target's energy with its horns. The user's HP is restored by half the damage taken by the target.");
            super.power = 75;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.HEALING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Electroweb extends Attack {
        private static final long serialVersionUID = 1L;

        Electroweb() {
            super(AttackNamesies.ELECTROWEB, Type.ELECTRIC, MoveCategory.SPECIAL, 15, "The user attacks and captures opposing Pok\u00e9mon using an electric net. This lowers their Speed stat.");
            super.power = 55;
            super.accuracy = 95;
            super.statChanges[Stat.SPEED.index()] = -1;
        }
    }

    static class GearGrind extends Attack implements MultiStrikeMove {
        private static final long serialVersionUID = 1L;

        GearGrind() {
            super(AttackNamesies.GEAR_GRIND, Type.STEEL, MoveCategory.PHYSICAL, 15, "The user attacks by throwing steel gears at its target twice.");
            super.power = 50;
            super.accuracy = 85;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getMinHits() {
            return 2;
        }

        @Override
        public int getMaxHits() {
            return 2;
        }
    }

    static class ShiftGear extends Attack {
        private static final long serialVersionUID = 1L;

        ShiftGear() {
            super(AttackNamesies.SHIFT_GEAR, Type.STEEL, MoveCategory.STATUS, 10, "The user rotates its gears, raising its Attack stat and sharply raising its Speed stat.");
            super.selfTarget = true;
            super.statChanges[Stat.ATTACK.index()] = 1;
            super.statChanges[Stat.SPEED.index()] = 2;
        }
    }

    static class HeadCharge extends Attack implements RecoilPercentageMove {
        private static final long serialVersionUID = 1L;

        HeadCharge() {
            super(AttackNamesies.HEAD_CHARGE, Type.NORMAL, MoveCategory.PHYSICAL, 15, "The user charges its head into its target, using its powerful guard hair. This also damages the user a little.");
            super.power = 120;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getDamagePercentageDenominator() {
            return 4;
        }
    }

    static class FieryDance extends Attack {
        private static final long serialVersionUID = 1L;

        FieryDance() {
            super(AttackNamesies.FIERY_DANCE, Type.FIRE, MoveCategory.SPECIAL, 10, "Cloaked in flames, the user dances and flaps its wings. This may also raise the user's Sp. Atk stat.");
            super.power = 80;
            super.accuracy = 100;
            super.effectChance = 50;
            super.moveTypes.add(MoveType.DANCE);
            super.selfTarget = true;
            super.statChanges[Stat.SP_ATTACK.index()] = 1;
        }
    }

    static class SacredSword extends Attack implements OpponentIgnoreStageEffect {
        private static final long serialVersionUID = 1L;

        SacredSword() {
            super(AttackNamesies.SACRED_SWORD, Type.FIGHTING, MoveCategory.PHYSICAL, 15, "The user attacks by slicing with a long horn. The target's stat changes don't affect this attack's damage.");
            super.power = 90;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public boolean ignoreStage(Stat s) {
            return !s.user();
        }
    }

    static class SecretSword extends Attack implements OpponentStatSwitchingEffect {
        private static final long serialVersionUID = 1L;

        SecretSword() {
            super(AttackNamesies.SECRET_SWORD, Type.FIGHTING, MoveCategory.SPECIAL, 10, "The user cuts with its long horn. The odd power contained in the horn does physical damage to the target.");
            super.power = 85;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.METRONOMELESS);
        }

        @Override
        public Stat getSwitchStat(Stat s) {
            return s == Stat.SP_DEFENSE ? Stat.DEFENSE : s;
        }
    }

    static class FusionFlare extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        FusionFlare() {
            super(AttackNamesies.FUSION_FLARE, Type.FIRE, MoveCategory.SPECIAL, 5, "The user brings down a giant flame. This move's power is increased when influenced by an enormous lightning bolt.");
            super.power = 100;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.DEFROST);
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return !b.isFirstAttack() && victim.getAttack().namesies() == AttackNamesies.FUSION_BOLT ? 2 : 1;
        }
    }

    static class FusionBolt extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        FusionBolt() {
            super(AttackNamesies.FUSION_BOLT, Type.ELECTRIC, MoveCategory.PHYSICAL, 5, "The user throws down a giant lightning bolt. This move's power is increased when influenced by an enormous flame.");
            super.power = 100;
            super.accuracy = 100;
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return !b.isFirstAttack() && victim.getAttack().namesies() == AttackNamesies.FUSION_FLARE ? 2 : 1;
        }
    }

    static class BlueFlare extends Attack {
        private static final long serialVersionUID = 1L;

        BlueFlare() {
            super(AttackNamesies.BLUE_FLARE, Type.FIRE, MoveCategory.SPECIAL, 5, "The user attacks by engulfing the target in an intense, yet beautiful, blue flame. This may also leave the target with a burn.");
            super.power = 130;
            super.accuracy = 85;
            super.effectChance = 20;
            super.status = StatusNamesies.BURNED;
        }
    }

    static class BoltStrike extends Attack {
        private static final long serialVersionUID = 1L;

        BoltStrike() {
            super(AttackNamesies.BOLT_STRIKE, Type.ELECTRIC, MoveCategory.PHYSICAL, 5, "The user surrounds itself with a great amount of electricity and charges its target. This may also leave the target with paralysis.");
            super.power = 130;
            super.accuracy = 85;
            super.effectChance = 20;
            super.status = StatusNamesies.PARALYZED;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Glaciate extends Attack {
        private static final long serialVersionUID = 1L;

        Glaciate() {
            super(AttackNamesies.GLACIATE, Type.ICE, MoveCategory.SPECIAL, 10, "The user attacks by blowing freezing cold air at opposing Pok\u00e9mon. This lowers their Speed stat.");
            super.power = 65;
            super.accuracy = 95;
            super.statChanges[Stat.SPEED.index()] = -1;
        }
    }

    static class TechnoBlast extends Attack {
        private static final long serialVersionUID = 1L;

        TechnoBlast() {
            super(AttackNamesies.TECHNO_BLAST, Type.NORMAL, MoveCategory.SPECIAL, 5, "The user fires a beam of light at its target. The move's type changes depending on the Drive the user holds.");
            super.power = 120;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.METRONOMELESS);
        }

        @Override
        public Type getType(Battle b, ActivePokemon user) {
            HoldItem item = user.getHeldItem(b);
            if (item instanceof DriveItem) {
                return ((DriveItem)item).getType();
            }

            return super.type;
        }
    }

    static class MultiAttack extends Attack {
        private static final long serialVersionUID = 1L;

        MultiAttack() {
            super(AttackNamesies.MULTI_ATTACK, Type.NORMAL, MoveCategory.PHYSICAL, 10, "Cloaking itself in high energy, the user slams into the target. The memory held determines the move's type.");
            super.power = 90;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public Type getType(Battle b, ActivePokemon user) {
            HoldItem item = user.getHeldItem(b);
            if (item instanceof MemoryItem) {
                return ((MemoryItem)item).getType();
            }

            return super.type;
        }
    }

    static class Explosion extends Attack {
        private static final long serialVersionUID = 1L;

        Explosion() {
            super(AttackNamesies.EXPLOSION, Type.NORMAL, MoveCategory.PHYSICAL, 5, "The user attacks everything around it by causing a tremendous explosion. The user faints upon using this move.");
            super.power = 250;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.USER_FAINTS);
            super.moveTypes.add(MoveType.EXPLODING);
        }
    }

    static class SelfDestruct extends Attack {
        private static final long serialVersionUID = 1L;

        SelfDestruct() {
            super(AttackNamesies.SELF_DESTRUCT, Type.NORMAL, MoveCategory.PHYSICAL, 5, "The user attacks everything around it by causing an explosion. The user faints upon using this move.");
            super.power = 200;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.USER_FAINTS);
            super.moveTypes.add(MoveType.EXPLODING);
        }
    }

    static class Fling extends Attack {
        private static final long serialVersionUID = 1L;

        Fling() {
            super(AttackNamesies.FLING, Type.DARK, MoveCategory.PHYSICAL, 10, "The user flings its held item at the target to attack. This move's power and effects depend on the item.");
            super.accuracy = 100;
        }

        @Override
        public int getPower(Battle b, ActivePokemon me, ActivePokemon o) {
            return me.getHeldItem(b).flingDamage();
        }

        @Override
        public void afterApplyCheck(Battle b, ActivePokemon user, ActivePokemon victim) {
            Messages.add(user.getName() + " flung its " + user.getHeldItem(b).getName() + "!");
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            HoldItem item = user.getHeldItem(b);
            item.flingEffect(b, victim);
            item.consumeItemWithoutEffects(b, user);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isHoldingItem(b);
        }
    }

    static class FreezeShock extends Attack implements ChargingMove {
        private static final long serialVersionUID = 1L;

        private boolean isCharging;

        FreezeShock() {
            super(AttackNamesies.FREEZE_SHOCK, Type.ICE, MoveCategory.PHYSICAL, 5, "On the second turn, the user hits the target with electrically charged ice. This may also leave the target with paralysis.");
            super.power = 140;
            super.accuracy = 90;
            super.effectChance = 30;
            super.status = StatusNamesies.PARALYZED;
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            this.resetReady();
        }

        @Override
        public String getChargeMessage(ActivePokemon user) {
            return user.getName() + " is charging!";
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void resetReady() {
            this.isCharging = !this.chargesFirst();
        }

        @Override
        public void switchReady() {
            this.isCharging = !this.isCharging;
        }
    }

    static class SecretPower extends Attack {
        private static final long serialVersionUID = 1L;

        SecretPower() {
            super(AttackNamesies.SECRET_POWER, Type.NORMAL, MoveCategory.PHYSICAL, 20, "The additional effects of this attack depend upon where it was used.");
            super.power = 70;
            super.accuracy = 100;
            super.effectChance = 30;
        }

        @Override
        public void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            TerrainType terrain = b.getTerrainType();

            super.status = terrain.getStatusCondition();
            super.statChanges = terrain.getStatChanges();
            super.effect = terrain.getEffect();
        }
    }

    static class FinalGambit extends Attack implements FixedDamageMove {
        private static final long serialVersionUID = 1L;

        FinalGambit() {
            super(AttackNamesies.FINAL_GAMBIT, Type.FIGHTING, MoveCategory.SPECIAL, 5, "The user risks everything to attack its target. The user faints but does damage equal to its HP.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.USER_FAINTS);
            super.moveTypes.add(MoveType.MIRRORLESS);
        }

        @Override
        public int getFixedDamageAmount(ActivePokemon me, ActivePokemon o) {
            return me.getHP();
        }
    }

    static class GastroAcid extends Attack implements ChangeAbilitySource {
        private static final long serialVersionUID = 1L;

        GastroAcid() {
            super(AttackNamesies.GASTRO_ACID, Type.POISON, MoveCategory.STATUS, 10, "The user hurls up its stomach acids on the target. The fluid eliminates the effect of the target's Ability.");
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.CHANGE_ABILITY;
        }

        @Override
        public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return caster.getName() + " suppressed " + victim.getName() + "'s ability!";
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.getAbility().isReplaceable();
        }

        @Override
        public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return AbilityNamesies.NO_ABILITY.getNewAbility();
        }
    }

    static class HealingWish extends Attack {
        private static final long serialVersionUID = 1L;

        HealingWish() {
            super(AttackNamesies.HEALING_WISH, Type.PSYCHIC, MoveCategory.STATUS, 10, "The user faints. In return, the Pok\u00e9mon taking its place will have its HP restored and status conditions cured.");
            super.effect = TeamEffectNamesies.HEAL_SWITCH;
            super.moveTypes.add(MoveType.USER_FAINTS);
            super.moveTypes.add(MoveType.HEALING);
            super.selfTarget = true;
        }
    }

    static class LunarDance extends Attack {
        private static final long serialVersionUID = 1L;

        LunarDance() {
            super(AttackNamesies.LUNAR_DANCE, Type.PSYCHIC, MoveCategory.STATUS, 10, "The user faints. In return, the Pok\u00e9mon taking its place will have its status and HP fully restored.");
            super.effect = TeamEffectNamesies.HEAL_SWITCH;
            super.moveTypes.add(MoveType.DANCE);
            super.moveTypes.add(MoveType.USER_FAINTS);
            super.moveTypes.add(MoveType.HEALING);
            super.selfTarget = true;
        }
    }

    static class Roar extends Attack implements SwapOpponentEffect {
        private static final long serialVersionUID = 1L;

        Roar() {
            super(AttackNamesies.ROAR, Type.NORMAL, MoveCategory.STATUS, 20, "The target is scared off, and a different Pok\u00e9mon is dragged out. In the wild, this ends a battle against a single Pok\u00e9mon.");
            super.moveTypes.add(MoveType.SOUND_BASED);
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.priority = -6;
        }

        @Override
        public String getSwapMessage(ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + " fled in fear!";
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            this.swapOpponent(b, user, victim);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.canSwapOpponent(b, victim);
        }
    }

    static class Grudge extends Attack {
        private static final long serialVersionUID = 1L;

        Grudge() {
            super(AttackNamesies.GRUDGE, Type.GHOST, MoveCategory.STATUS, 5, "If the user faints, the user's grudge fully depletes the PP of the opponent's move that knocked it out.");
            super.effect = PokemonEffectNamesies.GRUDGE;
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.selfTarget = true;
        }
    }

    static class Retaliate extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        Retaliate() {
            super(AttackNamesies.RETALIATE, Type.NORMAL, MoveCategory.PHYSICAL, 5, "The user gets revenge for a fainted ally. If an ally fainted in the previous turn, this move's power is increased.");
            super.power = 70;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return b.getEffects(user).hasEffect(TeamEffectNamesies.DEAD_ALLY) ? 2 : 1;
        }
    }

    static class CircleThrow extends Attack implements SwapOpponentEffect {
        private static final long serialVersionUID = 1L;

        CircleThrow() {
            super(AttackNamesies.CIRCLE_THROW, Type.FIGHTING, MoveCategory.PHYSICAL, 10, "The target is thrown, and a different Pok\u00e9mon is dragged out. In the wild, this ends a battle against a single Pok\u00e9mon.");
            super.power = 60;
            super.accuracy = 90;
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.priority = -6;
        }

        @Override
        public String getSwapMessage(ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + " was thrown away!";
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            this.swapOpponent(b, user, victim);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.canSwapOpponent(b, victim);
        }
    }

    static class Teleport extends Attack {
        private static final long serialVersionUID = 1L;

        Teleport() {
            super(AttackNamesies.TELEPORT, Type.PSYCHIC, MoveCategory.STATUS, 20, "Use it to flee from any wild Pok\u00e9mon.");
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.selfTarget = true;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            Messages.add(user.getName() + " teleported out of battle!");
            Messages.add(new MessageUpdate().withUpdate(MessageUpdateType.EXIT_BATTLE));
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return b.isWildBattle();
        }
    }

    static class RolePlay extends Attack implements ChangeAbilitySource {
        private static final long serialVersionUID = 1L;

        RolePlay() {
            super(AttackNamesies.ROLE_PLAY, Type.PSYCHIC, MoveCategory.STATUS, 10, "The user mimics the target completely, copying the target's natural Ability.");
            super.effect = PokemonEffectNamesies.CHANGE_ABILITY;
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.selfTarget = true;
        }

        @Override
        public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim) {
            Ability otherAbility = b.getOtherPokemon(victim).getAbility();
            return otherAbility.namesies().getNewAbility();
        }

        @Override
        public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
            ActivePokemon other = b.getOtherPokemon(victim);
            return victim.getName() + " copied " + other.getName() + "'s " + other.getAbility().getName() + "!";
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.getAbility().isStealable();
        }
    }

    static class KnockOff extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        KnockOff() {
            super(AttackNamesies.KNOCK_OFF, Type.DARK, MoveCategory.PHYSICAL, 20, "The user slaps down the target's held item, and that item can't be used in that battle. The move does more damage if the target has a held item.");
            super.power = 65;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (user.canRemoveItem(b, victim)) {
                Messages.add(user.getName() + " knocked off " + victim.getName() + "'s " + victim.getHeldItem(b).getName() + "!");
                if (b.isWildBattle()) {
                    victim.removeItem();
                } else {
                    user.setCastSource(ItemNamesies.NO_ITEM.getItem());
                    Effect.apply(PokemonEffectNamesies.CHANGE_ITEM, b, user, victim, CastSource.CAST_SOURCE, false);
                }
            }
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.isHoldingItem(b) ? 1.5 : 1;
        }
    }

    // Apparently this isn't supposed to hit fly anymore, but that's stupid
    static class Whirlwind extends Attack implements SwapOpponentEffect, SemiInvulnerableBypasser {
        private static final long serialVersionUID = 1L;

        Whirlwind() {
            super(AttackNamesies.WHIRLWIND, Type.NORMAL, MoveCategory.STATUS, 20, "The target is blown away, and a different Pok\u00e9mon is dragged out. In the wild, this ends a battle against a single Pok\u00e9mon.");
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.priority = -6;
        }

        @Override
        public String getSwapMessage(ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + " blew away!";
        }

        @Override
        public boolean semiInvulnerableBypass(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Always hit when the opponent is flying
            return defending.isSemiInvulnerableFlying();
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            this.swapOpponent(b, user, victim);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.canSwapOpponent(b, victim);
        }
    }

    static class Bestow extends Attack implements ItemSwapperEffect {
        private static final long serialVersionUID = 1L;

        Bestow() {
            super(AttackNamesies.BESTOW, Type.NORMAL, MoveCategory.STATUS, 15, "The user passes its held item to the target when the target isn't holding an item.");
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            this.swapItems(b, user, victim);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.canGiftItem(b, victim);
        }

        @Override
        public String getSwitchMessage(ActivePokemon user, HoldItem userItem, ActivePokemon victim, HoldItem victimItem) {
            return user.getName() + " gave " + victim.getName() + " its " + userItem.getName() + "!";
        }
    }

    static class Switcheroo extends Attack implements ItemSwapperEffect {
        private static final long serialVersionUID = 1L;

        Switcheroo() {
            super(AttackNamesies.SWITCHEROO, Type.DARK, MoveCategory.STATUS, 10, "The user trades held items with the target faster than the eye can follow.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            this.swapItems(b, user, victim);
        }

        @Override
        public String getSwitchMessage(ActivePokemon user, HoldItem userItem, ActivePokemon victim, HoldItem victimItem) {
            if (userItem.namesies() == ItemNamesies.NO_ITEM) {
                return user.getName() + " switched with " + victim.getName() + "'s " + victimItem.getName() + "!";
            } else if (victimItem.namesies() == ItemNamesies.NO_ITEM) {
                return user.getName() + " switched its " + userItem.getName() + " with " + victim.getName() + "!";
            } else {
                return user.getName() + " switched its " + userItem.getName() + " with " + victim.getName() + "'s " + victimItem.getName() + "!";
            }
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.canSwapItems(b, victim);
        }
    }

    static class Trick extends Attack implements ItemSwapperEffect {
        private static final long serialVersionUID = 1L;

        Trick() {
            super(AttackNamesies.TRICK, Type.PSYCHIC, MoveCategory.STATUS, 10, "The user catches the target off guard and swaps its held item with its own.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            this.swapItems(b, user, victim);
        }

        @Override
        public String getSwitchMessage(ActivePokemon user, HoldItem userItem, ActivePokemon victim, HoldItem victimItem) {
            if (userItem.namesies() == ItemNamesies.NO_ITEM) {
                return user.getName() + " switched with " + victim.getName() + "'s " + victimItem.getName() + "!";
            } else if (victimItem.namesies() == ItemNamesies.NO_ITEM) {
                return user.getName() + " switched its " + userItem.getName() + " with " + victim.getName() + "!";
            } else {
                return user.getName() + " switched its " + userItem.getName() + " with " + victim.getName() + "'s " + victimItem.getName() + "!";
            }
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.canSwapItems(b, victim);
        }
    }

    static class Memento extends Attack {
        private static final long serialVersionUID = 1L;

        Memento() {
            super(AttackNamesies.MEMENTO, Type.DARK, MoveCategory.STATUS, 10, "The user faints when using this move. In return, this harshly lowers the target's Attack and Sp. Atk stats.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.USER_FAINTS);
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.statChanges[Stat.ATTACK.index()] = -2;
            super.statChanges[Stat.SP_ATTACK.index()] = -2;
        }
    }

    static class DestinyBond extends Attack {
        private static final long serialVersionUID = 1L;

        DestinyBond() {
            super(AttackNamesies.DESTINY_BOND, Type.GHOST, MoveCategory.STATUS, 5, "After using this move, if the user faints, the Pok\u00e9mon that landed the knockout hit also faints. Its chance of failing rises if it is used in succession.");
            super.effect = PokemonEffectNamesies.DESTINY_BOND;
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.selfTarget = true;
        }
    }

    static class Camouflage extends Attack implements ChangeTypeSource {
        private static final long serialVersionUID = 1L;

        Camouflage() {
            super(AttackNamesies.CAMOUFLAGE, Type.NORMAL, MoveCategory.STATUS, 20, "The user's type is changed depending on its environment, such as at water's edge, in grass, or in a cave.");
            super.effect = PokemonEffectNamesies.CHANGE_TYPE;
            super.selfTarget = true;
        }

        @Override
        public PokeType getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return new PokeType(b.getTerrainType().getType());
        }
    }

    static class Recycle extends Attack {
        private static final long serialVersionUID = 1L;

        Recycle() {
            super(AttackNamesies.RECYCLE, Type.NORMAL, MoveCategory.STATUS, 10, "The user recycles a held item that has been used in battle so it can be used again.");
            super.selfTarget = true;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            HoldItem restored = ((ItemHolder)victim.getEffect(PokemonEffectNamesies.CONSUMED_ITEM)).getItem();
            victim.giveItem(restored);
            Messages.add(victim.getName() + "'s " + restored.getName() + " was restored!");
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.hasEffect(PokemonEffectNamesies.CONSUMED_ITEM) && !user.isHoldingItem(b);
        }
    }

    static class PartingShot extends Attack {
        private static final long serialVersionUID = 1L;

        PartingShot() {
            super(AttackNamesies.PARTING_SHOT, Type.DARK, MoveCategory.STATUS, 20, "With a parting threat, the user lowers the target's Attack and Sp. Atk stats. Then it switches with a party Pok\u00e9mon.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.SOUND_BASED);
            super.statChanges[Stat.ATTACK.index()] = -1;
            super.statChanges[Stat.SP_ATTACK.index()] = -1;
        }

        @Override
        public void afterApplyCheck(Battle b, ActivePokemon user, ActivePokemon victim) {
            Messages.add(user.getName() + " called " + victim.getName() + " a chump!!");
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            user.switcheroo(b, user, CastSource.ATTACK, true);
        }
    }

    static class UTurn extends Attack {
        private static final long serialVersionUID = 1L;

        UTurn() {
            super(AttackNamesies.U_TURN, Type.BUG, MoveCategory.PHYSICAL, 20, "After making its attack, the user rushes back to switch places with a party Pok\u00e9mon in waiting.");
            super.power = 70;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            user.switcheroo(b, user, CastSource.ATTACK, true);
        }
    }

    static class BatonPass extends Attack {
        private static final long serialVersionUID = 1L;

        BatonPass() {
            super(AttackNamesies.BATON_PASS, Type.NORMAL, MoveCategory.STATUS, 40, "The user switches places with a party Pok\u00e9mon in waiting and passes along any stat changes.");
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.selfTarget = true;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Fails if used by a wild pokemon or if the trainer does not have anyone to switch to
            Team team = b.getTrainer(user);
            return !(team instanceof Trainer) || ((Trainer)team).hasRemainingPokemon(b);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            // TODO: Hardcore test this shit not in the mood right now but this is one of the most complicated moves so lots of tests tests tests
            user.switcheroo(b, user, CastSource.ATTACK, true);

            ActivePokemon next = b.getTrainer(user).front();
            for (Stat stat : Stat.BATTLE_STATS) {
                next.getStages().setStage(stat, user.getStage(stat));
            }

            // Add each passable effect to the new Pokemon out front
            user.getEffects()
                .asList()
                .stream()
                .filter(effect -> effect instanceof PassableEffect)
                .forEach(effect -> next.getEffects().add(effect));
        }
    }

    static class PerishSong extends Attack {
        private static final long serialVersionUID = 1L;

        PerishSong() {
            super(AttackNamesies.PERISH_SONG, Type.NORMAL, MoveCategory.STATUS, 5, "Any Pok\u00e9mon that hears this song faints in three turns, unless it switches out of battle.");
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
            super.moveTypes.add(MoveType.SOUND_BASED);
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.MIRRORLESS);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            Messages.add("All Pokemon hearing this song will faint in three turns!");

            // TODO: Test and also this used to check if they didn't have the effect before casting just in case that's relevant later
            Effect.apply(PokemonEffectNamesies.PERISH_SONG, b, user, victim, CastSource.ATTACK, false);
            Effect.apply(PokemonEffectNamesies.PERISH_SONG, b, user, user, CastSource.ATTACK, false);
        }
    }

    static class DragonTail extends Attack implements SwapOpponentEffect {
        private static final long serialVersionUID = 1L;

        DragonTail() {
            super(AttackNamesies.DRAGON_TAIL, Type.DRAGON, MoveCategory.PHYSICAL, 10, "The target is knocked away, and a different Pok\u00e9mon is dragged out. In the wild, this ends a battle against a single Pok\u00e9mon.");
            super.power = 60;
            super.accuracy = 90;
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.priority = -6;
        }

        @Override
        public String getSwapMessage(ActivePokemon user, ActivePokemon victim) {
            return victim.getName() + " was slapped away!";
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            this.swapOpponent(b, user, victim);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.canSwapOpponent(b, victim);
        }
    }

    static class FoulPlay extends Attack {
        private static final long serialVersionUID = 1L;

        FoulPlay() {
            super(AttackNamesies.FOUL_PLAY, Type.DARK, MoveCategory.PHYSICAL, 15, "The user turns the target's power against it. The higher the target's Attack stat, the greater the move's power.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getPower(Battle b, ActivePokemon me, ActivePokemon o) {
            double ratio = (double)Stat.ATTACK.getBasicStat(b, me)/Stat.ATTACK.getBasicStat(b, o);
            if (ratio > .5) {
                return 60;
            } else if (ratio > .33) {
                return 80;
            } else if (ratio > .25) {
                return 120;
            } else {
                return 150;
            }
        }
    }

    static class Embargo extends Attack {
        private static final long serialVersionUID = 1L;

        Embargo() {
            super(AttackNamesies.EMBARGO, Type.DARK, MoveCategory.STATUS, 15, "This move prevents the target from using its held item for five turns. Its Trainer is also prevented from using items on it.");
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.EMBARGO;
        }
    }

    static class NaturePower extends Attack {
        private static final long serialVersionUID = 1L;

        NaturePower() {
            super(AttackNamesies.NATURE_POWER, Type.NORMAL, MoveCategory.STATUS, 20, "This attack makes use of nature's power. Its effects vary depending on the user's environment.");
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
            super.moveTypes.add(MoveType.MIRRORLESS);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            user.callFullNewMove(b, victim, b.getTerrainType().getAttack());
        }
    }

    static class Entrainment extends Attack implements ChangeAbilitySource {
        private static final long serialVersionUID = 1L;

        Entrainment() {
            super(AttackNamesies.ENTRAINMENT, Type.NORMAL, MoveCategory.STATUS, 15, "The user dances with an odd rhythm that compels the target to mimic it, making the target's Ability the same as the user's.");
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.CHANGE_ABILITY;
        }

        @Override
        public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return caster.getAbility().namesies().getNewAbility();
        }

        @Override
        public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return victim.getName() + " copied " + caster.getName() + "'s " + caster.getAbility().getName() + "!";
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.getAbility().isReplaceable() && user.getAbility().isStealable();
        }
    }

    static class MagicRoom extends Attack {
        private static final long serialVersionUID = 1L;

        MagicRoom() {
            super(AttackNamesies.MAGIC_ROOM, Type.PSYCHIC, MoveCategory.STATUS, 10, "The user creates a bizarre area in which Pok\u00e9mon's held items lose their effects for five turns.");
            super.effect = StandardBattleEffectNamesies.MAGIC_ROOM;
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.FIELD);
        }
    }

    static class WorrySeed extends Attack implements ChangeAbilitySource {
        private static final long serialVersionUID = 1L;

        WorrySeed() {
            super(AttackNamesies.WORRY_SEED, Type.GRASS, MoveCategory.STATUS, 10, "A seed that causes worry is planted on the target. It prevents sleep by making the target's Ability Insomnia.");
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.CHANGE_ABILITY;
        }

        @Override
        public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return victim.getName() + "'s ability was changed to " + AbilityNamesies.INSOMNIA.getName() + "!";
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.getAbility().isReplaceable();
        }

        @Override
        public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return AbilityNamesies.INSOMNIA.getNewAbility();
        }
    }

    static class SimpleBeam extends Attack implements ChangeAbilitySource {
        private static final long serialVersionUID = 1L;

        SimpleBeam() {
            super(AttackNamesies.SIMPLE_BEAM, Type.NORMAL, MoveCategory.STATUS, 15, "The user's mysterious psychic wave changes the target's Ability to Simple.");
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.CHANGE_ABILITY;
        }

        @Override
        public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return victim.getName() + "'s ability was changed to " + AbilityNamesies.SIMPLE.getName() + "!";
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.getAbility().isReplaceable();
        }

        @Override
        public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return AbilityNamesies.SIMPLE.getNewAbility();
        }
    }

    static class SkillSwap extends Attack implements ChangeAbilitySource {
        private static final long serialVersionUID = 1L;

        private Ability ability;

        private static boolean canSkillSwap(ActivePokemon p) {
            return p.getAbility().isReplaceable() && p.getAbility().isStealable();
        }

        SkillSwap() {
            super(AttackNamesies.SKILL_SWAP, Type.PSYCHIC, MoveCategory.STATUS, 10, "The user employs its psychic power to exchange Abilities with the target.");
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
        }

        @Override
        public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return ability;
        }

        @Override
        public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return victim.getName() + "'s ability was changed to " + ability.getName() + "!";
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            Ability userAbility = user.getAbility();
            Ability victimAbility = victim.getAbility();

            ability = userAbility;
            Effect.cast(PokemonEffectNamesies.CHANGE_ABILITY, b, user, victim, CastSource.ATTACK, super.printCast);

            ability = victimAbility;
            Effect.cast(PokemonEffectNamesies.CHANGE_ABILITY, b, user, user, CastSource.ATTACK, super.printCast);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return canSkillSwap(user) && canSkillSwap(victim);
        }
    }

    static class VoltSwitch extends Attack {
        private static final long serialVersionUID = 1L;

        VoltSwitch() {
            super(AttackNamesies.VOLT_SWITCH, Type.ELECTRIC, MoveCategory.SPECIAL, 20, "After making its attack, the user rushes back to switch places with a party Pok\u00e9mon in waiting.");
            super.power = 70;
            super.accuracy = 100;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            user.switcheroo(b, user, CastSource.ATTACK, true);
        }
    }

    static class RelicSong extends Attack {
        private static final long serialVersionUID = 1L;

        RelicSong() {
            super(AttackNamesies.RELIC_SONG, Type.NORMAL, MoveCategory.SPECIAL, 10, "The user sings an ancient song and attacks by appealing to the hearts of the listening opposing Pok\u00e9mon. This may also induce sleep.");
            super.power = 75;
            super.accuracy = 100;
            super.effectChance = 10;
            super.status = StatusNamesies.ASLEEP;
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.SOUND_BASED);
        }
    }

    static class Snarl extends Attack {
        private static final long serialVersionUID = 1L;

        Snarl() {
            super(AttackNamesies.SNARL, Type.DARK, MoveCategory.SPECIAL, 15, "The user yells as if it's ranting about something, which lowers the Sp. Atk stat of opposing Pok\u00e9mon.");
            super.power = 55;
            super.accuracy = 95;
            super.moveTypes.add(MoveType.SOUND_BASED);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.statChanges[Stat.SP_ATTACK.index()] = -1;
        }
    }

    static class IceBurn extends Attack implements ChargingMove {
        private static final long serialVersionUID = 1L;

        private boolean isCharging;

        IceBurn() {
            super(AttackNamesies.ICE_BURN, Type.ICE, MoveCategory.SPECIAL, 5, "On the second turn, an ultracold, freezing wind surrounds the target. This may leave the target with a burn.");
            super.power = 140;
            super.accuracy = 90;
            super.effectChance = 30;
            super.status = StatusNamesies.BURNED;
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            this.resetReady();
        }

        @Override
        public String getChargeMessage(ActivePokemon user) {
            return user.getName() + " is charging!";
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void resetReady() {
            this.isCharging = !this.chargesFirst();
        }

        @Override
        public void switchReady() {
            this.isCharging = !this.isCharging;
        }
    }

    static class VCreate extends Attack {
        private static final long serialVersionUID = 1L;

        VCreate() {
            super(AttackNamesies.V_CREATE, Type.FIRE, MoveCategory.PHYSICAL, 5, "With a hot flame on its forehead, the user hurls itself at its target. This lowers the user's Defense, Sp. Def, and Speed stats.");
            super.power = 180;
            super.accuracy = 95;
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.selfTarget = true;
            super.statChanges[Stat.DEFENSE.index()] = -1;
            super.statChanges[Stat.SP_DEFENSE.index()] = -1;
            super.statChanges[Stat.SPEED.index()] = -1;
        }
    }

    static class Surf extends Attack {
        private static final long serialVersionUID = 1L;

        Surf() {
            super(AttackNamesies.SURF, Type.WATER, MoveCategory.SPECIAL, 15, "The user attacks everything around it by swamping its surroundings with a giant wave.");
            super.power = 90;
            super.accuracy = 100;
        }
    }

    static class VoltTackle extends Attack implements RecoilPercentageMove {
        private static final long serialVersionUID = 1L;

        VoltTackle() {
            super(AttackNamesies.VOLT_TACKLE, Type.ELECTRIC, MoveCategory.PHYSICAL, 15, "The user electrifies itself and charges the target. This also damages the user quite a lot. This attack may leave the target with paralysis.");
            super.power = 120;
            super.accuracy = 100;
            super.effectChance = 10;
            super.status = StatusNamesies.PARALYZED;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getDamagePercentageDenominator() {
            return 3;
        }
    }

    static class FocusBlast extends Attack {
        private static final long serialVersionUID = 1L;

        FocusBlast() {
            super(AttackNamesies.FOCUS_BLAST, Type.FIGHTING, MoveCategory.SPECIAL, 5, "The user heightens its mental focus and unleashes its power. This may also lower the target's Sp. Def stat.");
            super.power = 120;
            super.accuracy = 70;
            super.effectChance = 10;
            super.moveTypes.add(MoveType.BOMB_BALL);
            super.statChanges[Stat.SP_DEFENSE.index()] = -1;
        }
    }

    static class DiamondStorm extends Attack {
        private static final long serialVersionUID = 1L;

        DiamondStorm() {
            super(AttackNamesies.DIAMOND_STORM, Type.ROCK, MoveCategory.PHYSICAL, 5, "The user whips up a storm of diamonds to damage opposing Pok\u00e9mon. This may also sharply raise the user's Defense stat.");
            super.power = 100;
            super.accuracy = 95;
            super.effectChance = 50;
            super.selfTarget = true;
            super.statChanges[Stat.DEFENSE.index()] = 2;
        }
    }

    static class Moonblast extends Attack {
        private static final long serialVersionUID = 1L;

        Moonblast() {
            super(AttackNamesies.MOONBLAST, Type.FAIRY, MoveCategory.SPECIAL, 15, "Borrowing the power of the moon, the user attacks the target. This may also lower the target's Sp. Atk stat.");
            super.power = 95;
            super.accuracy = 100;
            super.effectChance = 30;
            super.statChanges[Stat.SP_ATTACK.index()] = -1;
        }
    }

    static class LandsWrath extends Attack {
        private static final long serialVersionUID = 1L;

        LandsWrath() {
            super(AttackNamesies.LANDS_WRATH, Type.GROUND, MoveCategory.PHYSICAL, 10, "The user gathers the energy of the land and focuses that power on opposing Pok\u00e9mon to damage them.");
            super.power = 90;
            super.accuracy = 100;
        }
    }

    static class PhantomForce extends Attack implements SemiInvulnerableMove {
        private static final long serialVersionUID = 1L;

        private boolean isCharging;

        PhantomForce() {
            super(AttackNamesies.PHANTOM_FORCE, Type.GHOST, MoveCategory.PHYSICAL, 10, "The user vanishes somewhere, then strikes the target on the next turn. This move hits even if the target protects itself.");
            super.power = 90;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            this.resetReady();
        }

        @Override
        public boolean isOverground() {
            return false;
        }

        @Override
        public String getChargeMessage(ActivePokemon user) {
            return user.getName() + " vanished suddenly!";
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void resetReady() {
            this.isCharging = !this.chargesFirst();
        }

        @Override
        public void switchReady() {
            this.isCharging = !this.isCharging;
        }
    }

    static class OblivionWing extends Attack implements SapHealthMove {
        private static final long serialVersionUID = 1L;

        OblivionWing() {
            super(AttackNamesies.OBLIVION_WING, Type.FLYING, MoveCategory.SPECIAL, 10, "The user absorbs its target's HP. The user's HP is restored by over half of the damage taken by the target.");
            super.power = 80;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.HEALING);
        }

        @Override
        public double sapPercentage() {
            return .75;
        }
    }

    static class Geomancy extends Attack implements ChargingMove {
        private static final long serialVersionUID = 1L;

        private boolean isCharging;

        Geomancy() {
            super(AttackNamesies.GEOMANCY, Type.FAIRY, MoveCategory.STATUS, 10, "The user absorbs energy and sharply raises its Sp. Atk, Sp. Def, and Speed stats on the next turn.");
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
            super.selfTarget = true;
            super.statChanges[Stat.SP_ATTACK.index()] = 2;
            super.statChanges[Stat.SP_DEFENSE.index()] = 2;
            super.statChanges[Stat.SPEED.index()] = 2;
            this.resetReady();
        }

        @Override
        public String getChargeMessage(ActivePokemon user) {
            return user.getName() + " is absorbing power!";
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void resetReady() {
            this.isCharging = !this.chargesFirst();
        }

        @Override
        public void switchReady() {
            this.isCharging = !this.isCharging;
        }
    }

    static class Boomburst extends Attack {
        private static final long serialVersionUID = 1L;

        Boomburst() {
            super(AttackNamesies.BOOMBURST, Type.NORMAL, MoveCategory.SPECIAL, 10, "The user attacks everything around it with the destructive power of a terrible, explosive sound.");
            super.power = 140;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.SOUND_BASED);
        }
    }

    static class PlayRough extends Attack {
        private static final long serialVersionUID = 1L;

        PlayRough() {
            super(AttackNamesies.PLAY_ROUGH, Type.FAIRY, MoveCategory.PHYSICAL, 10, "The user plays rough with the target and attacks it. This may also lower the target's Attack stat.");
            super.power = 90;
            super.accuracy = 90;
            super.effectChance = 10;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.statChanges[Stat.ATTACK.index()] = -1;
        }
    }

    static class CraftyShield extends Attack {
        private static final long serialVersionUID = 1L;

        CraftyShield() {
            super(AttackNamesies.CRAFTY_SHIELD, Type.FAIRY, MoveCategory.STATUS, 10, "The user protects itself and its allies from status moves with a mysterious power. This does not stop moves that do damage.");
            super.effect = PokemonEffectNamesies.CRAFTY_SHIELD;
            super.moveTypes.add(MoveType.SUCCESSIVE_DECAY);
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.moveTypes.add(MoveType.NON_SNATCHABLE);
            super.selfTarget = true;
            super.priority = 4;
        }
    }

    static class Nuzzle extends Attack {
        private static final long serialVersionUID = 1L;

        Nuzzle() {
            super(AttackNamesies.NUZZLE, Type.ELECTRIC, MoveCategory.PHYSICAL, 20, "The user attacks by nuzzling its electrified cheeks against the target. This also leaves the target with paralysis.");
            super.power = 20;
            super.accuracy = 100;
            super.status = StatusNamesies.PARALYZED;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class DrainingKiss extends Attack implements SapHealthMove {
        private static final long serialVersionUID = 1L;

        DrainingKiss() {
            super(AttackNamesies.DRAINING_KISS, Type.FAIRY, MoveCategory.SPECIAL, 10, "The user steals the target's HP with a kiss. The user's HP is restored by over half of the damage taken by the target.");
            super.power = 50;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.HEALING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public double sapPercentage() {
            return .75;
        }
    }

    static class FairyWind extends Attack {
        private static final long serialVersionUID = 1L;

        FairyWind() {
            super(AttackNamesies.FAIRY_WIND, Type.FAIRY, MoveCategory.SPECIAL, 30, "The user stirs up a fairy wind and strikes the target with it.");
            super.power = 40;
            super.accuracy = 100;
        }
    }

    static class ParabolicCharge extends Attack implements SapHealthMove {
        private static final long serialVersionUID = 1L;

        ParabolicCharge() {
            super(AttackNamesies.PARABOLIC_CHARGE, Type.ELECTRIC, MoveCategory.SPECIAL, 20, "The user attacks everything around it. The user's HP is restored by half the damage taken by those hit.");
            super.power = 65;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.HEALING);
        }
    }

    static class DisarmingVoice extends Attack {
        private static final long serialVersionUID = 1L;

        DisarmingVoice() {
            super(AttackNamesies.DISARMING_VOICE, Type.FAIRY, MoveCategory.SPECIAL, 15, "Letting out a charming cry, the user does emotional damage to opposing Pok\u00e9mon. This attack never misses.");
            super.power = 40;
            super.moveTypes.add(MoveType.SOUND_BASED);
        }
    }

    static class FreezeDry extends Attack implements AdvantageMultiplierMove {
        private static final long serialVersionUID = 1L;

        FreezeDry() {
            super(AttackNamesies.FREEZE_DRY, Type.ICE, MoveCategory.SPECIAL, 20, "The user rapidly cools the target. This may also leave the target frozen. This move is super effective on Water types.");
            super.power = 70;
            super.accuracy = 100;
            super.effectChance = 10;
            super.status = StatusNamesies.FROZEN;
        }

        @Override
        public double multiplyAdvantage(Type attackingType, PokeType defendingTypes) {
            double multiplier = 1;
            for (Type defendingType : defendingTypes) {
                if (defendingType == Type.WATER) {
                    multiplier *= 2/attackingType.getAdvantage().getAdvantage(defendingType);
                }
            }

            return multiplier;
        }
    }

    static class FlyingPress extends Attack implements AdvantageMultiplierMove, DoubleMinimizerMove {
        private static final long serialVersionUID = 1L;

        FlyingPress() {
            super(AttackNamesies.FLYING_PRESS, Type.FIGHTING, MoveCategory.PHYSICAL, 10, "The user dives down onto the target from the sky. This move is Fighting and Flying type simultaneously.");
            super.power = 100;
            super.accuracy = 95;
            super.moveTypes.add(MoveType.AIRBORNE);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public double multiplyAdvantage(Type attackingType, PokeType defendingTypes) {
            return TypeAdvantage.FLYING.getAdvantage(defendingTypes);
        }
    }

    static class TopsyTurvy extends Attack {
        private static final long serialVersionUID = 1L;

        TopsyTurvy() {
            super(AttackNamesies.TOPSY_TURVY, Type.DARK, MoveCategory.STATUS, 20, "All stat changes affecting the target turn topsy-turvy and become the opposite of what they were.");
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            for (Stat stat : Stat.BATTLE_STATS) {
                victim.getStages().setStage(stat, -victim.getStage(stat));
            }

            Messages.add(victim.getName() + "'s stat changes were all reversed!");
        }
    }

    static class PlayNice extends Attack {
        private static final long serialVersionUID = 1L;

        PlayNice() {
            super(AttackNamesies.PLAY_NICE, Type.NORMAL, MoveCategory.STATUS, 20, "The user and the target become friends, and the target loses its will to fight. This lowers the target's Attack stat.");
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
            super.statChanges[Stat.ATTACK.index()] = -1;
        }
    }

    static class EerieImpulse extends Attack {
        private static final long serialVersionUID = 1L;

        EerieImpulse() {
            super(AttackNamesies.EERIE_IMPULSE, Type.ELECTRIC, MoveCategory.STATUS, 15, "The user's body generates an eerie impulse. Exposing the target to it harshly lowers the target's Sp. Atk stat.");
            super.accuracy = 100;
            super.statChanges[Stat.SP_ATTACK.index()] = -2;
        }
    }

    static class MistyTerrain extends Attack {
        private static final long serialVersionUID = 1L;

        MistyTerrain() {
            super(AttackNamesies.MISTY_TERRAIN, Type.FAIRY, MoveCategory.STATUS, 10, "This protects Pok\u00e9mon on the ground from status conditions and halves damage from Dragon-type moves for five turns.");
            super.effect = TerrainNamesies.MISTY_TERRAIN;
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.FIELD);
        }
    }

    // Note: Changed to work for the remainder of combat and not just the following turn
    static class FairyLock extends Attack {
        private static final long serialVersionUID = 1L;

        FairyLock() {
            super(AttackNamesies.FAIRY_LOCK, Type.FAIRY, MoveCategory.STATUS, 10, "By locking down the battlefield, the user keeps all Pok\u00e9mon from fleeing.");
            super.effect = PokemonEffectNamesies.TRAPPED;
        }
    }

    static class AromaticMist extends Attack {
        private static final long serialVersionUID = 1L;

        AromaticMist() {
            super(AttackNamesies.AROMATIC_MIST, Type.FAIRY, MoveCategory.STATUS, 20, "The user raises its Sp. Def stat by using a mysterious aroma.");
            super.selfTarget = true;
            super.statChanges[Stat.SP_DEFENSE.index()] = 1;
        }
    }

    static class BabyDollEyes extends Attack {
        private static final long serialVersionUID = 1L;

        BabyDollEyes() {
            super(AttackNamesies.BABY_DOLL_EYES, Type.FAIRY, MoveCategory.STATUS, 30, "The user stares at the target with its baby-doll eyes, which lowers its Attack stat. This move always goes first.");
            super.accuracy = 100;
            super.priority = 1;
            super.statChanges[Stat.ATTACK.index()] = -1;
        }
    }

    static class PetalBlizzard extends Attack {
        private static final long serialVersionUID = 1L;

        PetalBlizzard() {
            super(AttackNamesies.PETAL_BLIZZARD, Type.GRASS, MoveCategory.PHYSICAL, 15, "The user stirs up a violent petal blizzard and attacks everything around it.");
            super.power = 90;
            super.accuracy = 100;
        }
    }

    static class GrassyTerrain extends Attack {
        private static final long serialVersionUID = 1L;

        GrassyTerrain() {
            super(AttackNamesies.GRASSY_TERRAIN, Type.GRASS, MoveCategory.STATUS, 10, "The user turns the ground to grass for five turns. This restores the HP of Pok\u00e9mon on the ground a little every turn and powers up Grass type-moves.");
            super.effect = TerrainNamesies.GRASSY_TERRAIN;
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.FIELD);
        }
    }

    static class FlowerShield extends Attack {
        private static final long serialVersionUID = 1L;

        FlowerShield() {
            super(AttackNamesies.FLOWER_SHIELD, Type.FAIRY, MoveCategory.STATUS, 10, "The user raises its Defense stat with a mysterious power.");
            super.selfTarget = true;
            super.statChanges[Stat.DEFENSE.index()] = 1;
        }
    }

    static class NobleRoar extends Attack {
        private static final long serialVersionUID = 1L;

        NobleRoar() {
            super(AttackNamesies.NOBLE_ROAR, Type.NORMAL, MoveCategory.STATUS, 30, "Letting out a noble roar, the user intimidates the target and lowers its Attack and Sp. Atk stats.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.SOUND_BASED);
            super.statChanges[Stat.ATTACK.index()] = -1;
            super.statChanges[Stat.SP_ATTACK.index()] = -1;
        }
    }

    static class Powder extends Attack implements PowderMove {
        private static final long serialVersionUID = 1L;

        Powder() {
            super(AttackNamesies.POWDER, Type.BUG, MoveCategory.STATUS, 20, "The user covers the target in a combustible powder. If the target uses a Fire-type move, the powder explodes and damages the target.");
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.POWDER;
            super.priority = 1;
        }
    }

    static class Rototiller extends Attack {
        private static final long serialVersionUID = 1L;

        Rototiller() {
            super(AttackNamesies.ROTOTILLER, Type.GROUND, MoveCategory.STATUS, 10, "Tilling the soil, the user makes it easier for plants to grow. This raises its Attack and Sp. Atk stats.");
            super.selfTarget = true;
            super.statChanges[Stat.ATTACK.index()] = 1;
            super.statChanges[Stat.SP_ATTACK.index()] = 1;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isGrounded(b);
        }
    }

    static class WaterShuriken extends Attack implements MultiStrikeMove {
        private static final long serialVersionUID = 1L;

        WaterShuriken() {
            super(AttackNamesies.WATER_SHURIKEN, Type.WATER, MoveCategory.SPECIAL, 20, "The user hits the target with throwing stars two to five times in a row. This move always goes first.");
            super.power = 15;
            super.accuracy = 100;
            super.priority = 1;
        }

        @Override
        public int getMinHits() {
            return 2;
        }

        @Override
        public int getMaxHits() {
            return 5;
        }
    }

    static class MatBlock extends Attack {
        private static final long serialVersionUID = 1L;

        MatBlock() {
            super(AttackNamesies.MAT_BLOCK, Type.FIGHTING, MoveCategory.STATUS, 10, "Using a pulled-up mat as a shield, the user protects itself and its allies from damaging moves. This does not stop status moves.");
            super.effect = PokemonEffectNamesies.MAT_BLOCK;
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.selfTarget = true;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isFirstTurn();
        }
    }

    static class MysticalFire extends Attack {
        private static final long serialVersionUID = 1L;

        MysticalFire() {
            super(AttackNamesies.MYSTICAL_FIRE, Type.FIRE, MoveCategory.SPECIAL, 10, "The user attacks by breathing a special, hot fire. This also lowers the target's Sp. Atk stat.");
            super.power = 75;
            super.accuracy = 100;
            super.statChanges[Stat.SP_ATTACK.index()] = -1;
        }
    }

    static class Infestation extends Attack {
        private static final long serialVersionUID = 1L;

        Infestation() {
            super(AttackNamesies.INFESTATION, Type.BUG, MoveCategory.SPECIAL, 20, "The target is infested and attacked for four to five turns. The target can't flee during this time.");
            super.power = 20;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.INFESTATION;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Electrify extends Attack implements ChangeAttackTypeSource {
        private static final long serialVersionUID = 1L;

        Electrify() {
            super(AttackNamesies.ELECTRIFY, Type.ELECTRIC, MoveCategory.STATUS, 20, "If the target is electrified before it uses a move during that turn, the target's move becomes Electric type.");
            super.effect = PokemonEffectNamesies.CHANGE_ATTACK_TYPE;
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.priority = 1;
        }

        @Override
        public Type getAttackType(Type original) {
            return Type.ELECTRIC;
        }

        @Override
        public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return caster.getName() + " electrified " + victim.getName() + "!";
        }
    }

    static class FellStinger extends Attack implements MurderEffect {
        private static final long serialVersionUID = 1L;

        FellStinger() {
            super(AttackNamesies.FELL_STINGER, Type.BUG, MoveCategory.PHYSICAL, 25, "When the user knocks out a target with this move, the user's Attack stat rises drastically.");
            super.power = 50;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public void killWish(Battle b, ActivePokemon dead, ActivePokemon murderer) {
            murderer.getStages().modifyStage(murderer, 3, Stat.ATTACK, b, CastSource.ATTACK);
        }
    }

    static class MagneticFlux extends Attack {
        private static final long serialVersionUID = 1L;

        MagneticFlux() {
            super(AttackNamesies.MAGNETIC_FLUX, Type.ELECTRIC, MoveCategory.STATUS, 20, "The user manipulates magnetic fields, which raises its Defense and Sp. Def stats.");
            super.selfTarget = true;
            super.statChanges[Stat.DEFENSE.index()] = 1;
            super.statChanges[Stat.SP_DEFENSE.index()] = 1;
        }
    }

    static class StickyWeb extends Attack {
        private static final long serialVersionUID = 1L;

        StickyWeb() {
            super(AttackNamesies.STICKY_WEB, Type.BUG, MoveCategory.STATUS, 20, "The user weaves a sticky net around the opposing team, which lowers their Speed stat upon switching into battle.");
            super.effect = TeamEffectNamesies.STICKY_WEB;
            super.moveTypes.add(MoveType.FIELD);
        }
    }

    static class Belch extends Attack {
        private static final long serialVersionUID = 1L;

        Belch() {
            super(AttackNamesies.BELCH, Type.POISON, MoveCategory.SPECIAL, 10, "The user lets out a damaging belch at the target. The user must eat a held Berry to use this move.");
            super.power = 120;
            super.accuracy = 90;
            super.moveTypes.add(MoveType.ASSISTLESS);
            super.moveTypes.add(MoveType.MIRRORLESS);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.hasEffect(PokemonEffectNamesies.EATEN_BERRY);
        }
    }

    static class VenomDrench extends Attack {
        private static final long serialVersionUID = 1L;

        VenomDrench() {
            super(AttackNamesies.VENOM_DRENCH, Type.POISON, MoveCategory.STATUS, 20, "Opposing Pok\u00e9mon are drenched in an odd poisonous liquid. This lowers the Attack, Sp. Atk, and Speed stats of a poisoned target.");
            super.accuracy = 100;
            super.statChanges[Stat.ATTACK.index()] = -1;
            super.statChanges[Stat.SP_ATTACK.index()] = -1;
            super.statChanges[Stat.SPEED.index()] = -1;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.hasStatus(StatusNamesies.POISONED);
        }
    }

    static class ElectricTerrain extends Attack {
        private static final long serialVersionUID = 1L;

        ElectricTerrain() {
            super(AttackNamesies.ELECTRIC_TERRAIN, Type.ELECTRIC, MoveCategory.STATUS, 10, "The user electrifies the ground for five turns, powering up Electric-type moves. Pok\u00e9mon on the ground no longer fall asleep.");
            super.effect = TerrainNamesies.ELECTRIC_TERRAIN;
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.FIELD);
        }
    }

    static class PsychicTerrain extends Attack {
        private static final long serialVersionUID = 1L;

        PsychicTerrain() {
            super(AttackNamesies.PSYCHIC_TERRAIN, Type.PSYCHIC, MoveCategory.STATUS, 10, "This protects Pok\u00e9mon on the ground from priority moves and powers up Psychic-type moves for five turns.");
            super.effect = TerrainNamesies.PSYCHIC_TERRAIN;
            super.moveTypes.add(MoveType.NO_MAGIC_COAT);
            super.moveTypes.add(MoveType.FIELD);
        }
    }

    static class PowerUpPunch extends Attack {
        private static final long serialVersionUID = 1L;

        PowerUpPunch() {
            super(AttackNamesies.POWER_UP_PUNCH, Type.FIGHTING, MoveCategory.PHYSICAL, 20, "Striking opponents over and over makes the user's fists harder. Hitting a target raises the Attack stat.");
            super.power = 40;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PUNCHING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.selfTarget = true;
            super.statChanges[Stat.ATTACK.index()] = 1;
        }
    }

    static class Confide extends Attack {
        private static final long serialVersionUID = 1L;

        Confide() {
            super(AttackNamesies.CONFIDE, Type.NORMAL, MoveCategory.STATUS, 20, "The user tells the target a secret, and the target loses its ability to concentrate. This lowers the target's Sp. Atk stat.");
            super.moveTypes.add(MoveType.SOUND_BASED);
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
            super.statChanges[Stat.SP_ATTACK.index()] = -1;
        }
    }

    static class Cut extends Attack {
        private static final long serialVersionUID = 1L;

        Cut() {
            super(AttackNamesies.CUT, Type.NORMAL, MoveCategory.PHYSICAL, 30, "The target is cut with a scythe or claw.");
            super.power = 50;
            super.accuracy = 95;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class DazzlingGleam extends Attack {
        private static final long serialVersionUID = 1L;

        DazzlingGleam() {
            super(AttackNamesies.DAZZLING_GLEAM, Type.FAIRY, MoveCategory.SPECIAL, 10, "The user damages opposing Pok\u00e9mon by emitting a powerful flash.");
            super.power = 80;
            super.accuracy = 100;
        }
    }

    static class Strength extends Attack {
        private static final long serialVersionUID = 1L;

        Strength() {
            super(AttackNamesies.STRENGTH, Type.NORMAL, MoveCategory.PHYSICAL, 15, "The target is slugged with a punch thrown at maximum power.");
            super.power = 80;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class OriginPulse extends Attack {
        private static final long serialVersionUID = 1L;

        OriginPulse() {
            super(AttackNamesies.ORIGIN_PULSE, Type.WATER, MoveCategory.SPECIAL, 10, "The user attacks opposing Pok\u00e9mon with countless beams of light that glow a deep and brilliant blue.");
            super.power = 110;
            super.accuracy = 85;
            super.moveTypes.add(MoveType.AURA_PULSE);
        }
    }

    static class PrecipiceBlades extends Attack {
        private static final long serialVersionUID = 1L;

        PrecipiceBlades() {
            super(AttackNamesies.PRECIPICE_BLADES, Type.GROUND, MoveCategory.PHYSICAL, 10, "The user attacks opposing Pok\u00e9mon by manifesting the power of the land in fearsome blades of stone.");
            super.power = 120;
            super.accuracy = 85;
        }
    }

    static class ShoreUp extends Attack implements SelfHealingMove {
        private static final long serialVersionUID = 1L;

        ShoreUp() {
            super(AttackNamesies.SHORE_UP, Type.GROUND, MoveCategory.STATUS, 10, "The user regains up to half of its max HP. It restores more HP in a sandstorm.");
            super.moveTypes.add(MoveType.HEALING);
            super.selfTarget = true;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            this.heal(b, victim);
        }

        @Override
        public double getHealFraction(Battle b, ActivePokemon victim) {
            // Fully heals in a sandstorm
            return b.getWeather().namesies() == WeatherNamesies.SANDSTORM ? 1 : .5;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.canHeal();
        }
    }

    static class FloralHealing extends Attack implements SelfHealingMove {
        private static final long serialVersionUID = 1L;

        FloralHealing() {
            super(AttackNamesies.FLORAL_HEALING, Type.FAIRY, MoveCategory.STATUS, 10, "The user restores the target's HP by up to half of its max HP. It restores more HP when the terrain is grass.");
            super.moveTypes.add(MoveType.HEALING);
            super.selfTarget = true;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            this.heal(b, victim);
        }

        @Override
        public double getHealFraction(Battle b, ActivePokemon victim) {
            // Fully heals in Grassy Terrain
            return b.hasEffect(TerrainNamesies.GRASSY_TERRAIN) ? 1 : .5;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.canHeal();
        }
    }

    static class FirstImpression extends Attack {
        private static final long serialVersionUID = 1L;

        FirstImpression() {
            super(AttackNamesies.FIRST_IMPRESSION, Type.BUG, MoveCategory.PHYSICAL, 10, "Although this move has great power, it only works the first turn the user is in battle.");
            super.power = 90;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.priority = 2;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isFirstTurn();
        }
    }

    static class AnchorShot extends Attack {
        private static final long serialVersionUID = 1L;

        AnchorShot() {
            super(AttackNamesies.ANCHOR_SHOT, Type.STEEL, MoveCategory.PHYSICAL, 20, "The user entangles the target with its anchor chain while attacking. The target becomes unable to flee.");
            super.power = 80;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.TRAPPED;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class SpiritShackle extends Attack {
        private static final long serialVersionUID = 1L;

        SpiritShackle() {
            super(AttackNamesies.SPIRIT_SHACKLE, Type.GHOST, MoveCategory.PHYSICAL, 10, "The user attacks while simultaneously stitching the target's shadow to the ground to prevent the target from escaping.");
            super.power = 80;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.TRAPPED;
        }
    }

    static class DarkestLariat extends Attack implements OpponentIgnoreStageEffect {
        private static final long serialVersionUID = 1L;

        DarkestLariat() {
            super(AttackNamesies.DARKEST_LARIAT, Type.DARK, MoveCategory.PHYSICAL, 10, "The user swings both arms and hits the target. The target's stat changes don't affect this attack's damage.");
            super.power = 85;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public boolean ignoreStage(Stat s) {
            return !s.user();
        }
    }

    static class SparklingAria extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        SparklingAria() {
            super(AttackNamesies.SPARKLING_ARIA, Type.WATER, MoveCategory.SPECIAL, 10, "The user bursts into song, emitting many bubbles. Any Pok\u00e9mon suffering from a burn will be healed by the touch of these bubbles.");
            super.power = 90;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.SOUND_BASED);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            if (victim.hasStatus(StatusNamesies.BURNED)) {
                victim.removeStatus(b, CastSource.ATTACK);
            }
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return victim.hasStatus(StatusNamesies.BURNED) ? 2 : 1;
        }
    }

    static class HighHorsepower extends Attack {
        private static final long serialVersionUID = 1L;

        HighHorsepower() {
            super(AttackNamesies.HIGH_HORSEPOWER, Type.GROUND, MoveCategory.PHYSICAL, 10, "The user fiercely attacks the target using its entire body.");
            super.power = 95;
            super.accuracy = 95;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class StrengthSap extends Attack implements SapHealthEffect {
        private static final long serialVersionUID = 1L;

        StrengthSap() {
            super(AttackNamesies.STRENGTH_SAP, Type.GRASS, MoveCategory.STATUS, 10, "The user restores its HP by the same amount as the target's Attack stat. It also lowers the target's Attack stat.");
            super.accuracy = 100;
            super.moveTypes.add(MoveType.HEALING);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Only heal if stat actually lowers
            int victimAttackStat = Stat.ATTACK.getBasicStat(b, victim);
            boolean reduced = victim.getStages().modifyStage(user, -1, Stat.ATTACK, b, CastSource.ATTACK);
            if (reduced) {
                this.sapHealth(b, user, victim, victimAttackStat, true);
            }
        }

        @Override
        public double sapPercentage() {
            return 1;
        }
    }

    static class Leafage extends Attack {
        private static final long serialVersionUID = 1L;

        Leafage() {
            super(AttackNamesies.LEAFAGE, Type.GRASS, MoveCategory.PHYSICAL, 40, "The user attacks by pelting the target with leaves.");
            super.power = 40;
            super.accuracy = 100;
        }
    }

    static class ToxicThread extends Attack {
        private static final long serialVersionUID = 1L;

        ToxicThread() {
            super(AttackNamesies.TOXIC_THREAD, Type.POISON, MoveCategory.STATUS, 20, "The user shoots poisonous threads to poison the target and lower the target's Speed stat.");
            super.accuracy = 100;
            super.status = StatusNamesies.POISONED;
            super.statChanges[Stat.SPEED.index()] = -1;
        }
    }

    static class LaserFocus extends Attack {
        private static final long serialVersionUID = 1L;

        LaserFocus() {
            super(AttackNamesies.LASER_FOCUS, Type.NORMAL, MoveCategory.STATUS, 30, "The user concentrates intensely. The attack on the next turn always results in a critical hit.");
            super.effect = PokemonEffectNamesies.LASER_FOCUS;
            super.selfTarget = true;
        }
    }

    static class GearUp extends Attack {
        private static final long serialVersionUID = 1L;

        GearUp() {
            super(AttackNamesies.GEAR_UP, Type.STEEL, MoveCategory.STATUS, 20, "The user engages its gears to raise its Attack and Sp. Atk stats.");
            super.selfTarget = true;
            super.statChanges[Stat.ATTACK.index()] = 1;
            super.statChanges[Stat.SP_ATTACK.index()] = 1;
        }
    }

    static class ThroatChop extends Attack {
        private static final long serialVersionUID = 1L;

        ThroatChop() {
            super(AttackNamesies.THROAT_CHOP, Type.DARK, MoveCategory.PHYSICAL, 15, "The user attacks the target's throat, and the resultant suffering prevents the target from using moves that emit sound for two turns.");
            super.power = 80;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.SILENCE;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    // TODO: Had to remove the special effects of this move because double battle things, but would like to replace it with something else
    static class PollenPuff extends Attack {
        private static final long serialVersionUID = 1L;

        PollenPuff() {
            super(AttackNamesies.POLLEN_PUFF, Type.BUG, MoveCategory.SPECIAL, 15, "The user attacks the enemy with a pollen puff that explodes.");
            super.power = 90;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.BOMB_BALL);
        }
    }

    static class Lunge extends Attack {
        private static final long serialVersionUID = 1L;

        Lunge() {
            super(AttackNamesies.LUNGE, Type.BUG, MoveCategory.PHYSICAL, 15, "The user makes a lunge at the target, attacking with full force. This also lowers the target's Attack stat.");
            super.power = 80;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.statChanges[Stat.ATTACK.index()] = -1;
        }
    }

    static class FireLash extends Attack {
        private static final long serialVersionUID = 1L;

        FireLash() {
            super(AttackNamesies.FIRE_LASH, Type.FIRE, MoveCategory.PHYSICAL, 15, "The user strikes the target with a burning lash. This also lowers the target's Defense stat.");
            super.power = 80;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.statChanges[Stat.DEFENSE.index()] = -1;
        }
    }

    static class BurnUp extends Attack implements ChangeTypeSource {
        private static final long serialVersionUID = 1L;

        BurnUp() {
            super(AttackNamesies.BURN_UP, Type.FIRE, MoveCategory.SPECIAL, 5, "To inflict massive damage, the user burns itself out. After using this move, the user will no longer be Fire type.");
            super.power = 130;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.CHANGE_TYPE;
            super.moveTypes.add(MoveType.DEFROST);
            super.selfTarget = true;
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.isType(b, Type.FIRE);
        }

        @Override
        public PokeType getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
            PokeType type = victim.getType(b);
            if (type.getFirstType() == Type.FIRE) {
                return new PokeType(type.getSecondType());
            }

            if (type.getSecondType() == Type.FIRE) {
                return new PokeType(type.getFirstType());
            }

            return null;
        }
    }

    static class SmartStrike extends Attack {
        private static final long serialVersionUID = 1L;

        SmartStrike() {
            super(AttackNamesies.SMART_STRIKE, Type.STEEL, MoveCategory.PHYSICAL, 10, "The user stabs the target with a sharp horn. This attack never misses.");
            super.power = 70;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class Purify extends Attack implements SelfHealingMove {
        private static final long serialVersionUID = 1L;

        Purify() {
            super(AttackNamesies.PURIFY, Type.POISON, MoveCategory.STATUS, 20, "The user heals the target's status condition. If the move succeeds, it also restores the user's own HP.");
            super.moveTypes.add(MoveType.HEALING);
            super.selfTarget = true;
        }

        @Override
        public double getHealFraction(Battle b, ActivePokemon victim) {
            return .5;
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            // Okay so this is a little different than in the games
            // So normally you heal a target's (presumably ally's) status condition and then yourself
            // But not gonna implement to heal the opponent's status condition because that's stupid
            // So basically it's just a better Refresh that heals when successful
            user.removeStatus(b, CastSource.ATTACK);
            this.heal(b, user);
        }

        @Override
        public boolean applies(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.hasStatus();
        }
    }

    static class RevelationDance extends Attack {
        private static final long serialVersionUID = 1L;

        RevelationDance() {
            super(AttackNamesies.REVELATION_DANCE, Type.NORMAL, MoveCategory.SPECIAL, 15, "The user attacks the target by dancing very hard. The user's type determines the type of this move.");
            super.power = 90;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.DANCE);
        }

        @Override
        public Type getType(Battle b, ActivePokemon user) {
            return user.getType(b).getFirstType();
        }
    }

    static class CoreEnforcer extends Attack implements ChangeAbilitySource {
        private static final long serialVersionUID = 1L;

        CoreEnforcer() {
            super(AttackNamesies.CORE_ENFORCER, Type.DRAGON, MoveCategory.SPECIAL, 10, "If the Pok\u00e9mon the user has inflicted damage on have already used their moves, this move eliminates the effect of the target's Ability.");
            super.power = 100;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.CHANGE_ABILITY;
        }

        @Override
        public boolean shouldApplyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            return !b.isFirstAttack() && victim.getAbility().isReplaceable();
        }

        @Override
        public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return caster.getName() + " suppressed " + victim.getName() + "'s ability!";
        }

        @Override
        public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return AbilityNamesies.NO_ABILITY.getNewAbility();
        }
    }

    static class TropKick extends Attack {
        private static final long serialVersionUID = 1L;

        TropKick() {
            super(AttackNamesies.TROP_KICK, Type.GRASS, MoveCategory.PHYSICAL, 15, "The user lands an intense kick of tropical origins on the target. This also lowers the target's Attack stat.");
            super.power = 70;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.statChanges[Stat.ATTACK.index()] = -1;
        }
    }

    static class ClangingScales extends Attack {
        private static final long serialVersionUID = 1L;

        ClangingScales() {
            super(AttackNamesies.CLANGING_SCALES, Type.DRAGON, MoveCategory.SPECIAL, 5, "The user rubs the scales on its entire body and makes a huge noise to attack the opposing Pok\u00e9mon. The user's Defense stat goes down after the attack.");
            super.power = 110;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.SOUND_BASED);
            super.selfTarget = true;
            super.statChanges[Stat.DEFENSE.index()] = -1;
        }
    }

    static class DragonHammer extends Attack {
        private static final long serialVersionUID = 1L;

        DragonHammer() {
            super(AttackNamesies.DRAGON_HAMMER, Type.DRAGON, MoveCategory.PHYSICAL, 15, "The user uses its body like a hammer to attack the target and inflict damage.");
            super.power = 90;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class BrutalSwing extends Attack {
        private static final long serialVersionUID = 1L;

        BrutalSwing() {
            super(AttackNamesies.BRUTAL_SWING, Type.DARK, MoveCategory.PHYSICAL, 20, "The user swings its body around violently to inflict damage on everything in its vicinity.");
            super.power = 60;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class FleurCannon extends Attack {
        private static final long serialVersionUID = 1L;

        FleurCannon() {
            super(AttackNamesies.FLEUR_CANNON, Type.FAIRY, MoveCategory.SPECIAL, 5, "The user unleashes a strong beam. The attack's recoil harshly lowers the user's Sp. Atk stat.");
            super.power = 130;
            super.accuracy = 90;
            super.selfTarget = true;
            super.statChanges[Stat.SP_ATTACK.index()] = -2;
        }
    }

    static class ShadowBone extends Attack {
        private static final long serialVersionUID = 1L;

        ShadowBone() {
            super(AttackNamesies.SHADOW_BONE, Type.GHOST, MoveCategory.PHYSICAL, 10, "The user attacks by beating the target with a bone that contains a spirit. This may also lower the target's Defense stat.");
            super.power = 85;
            super.accuracy = 100;
            super.effectChance = 20;
            super.statChanges[Stat.DEFENSE.index()] = -1;
        }
    }

    static class Accelerock extends Attack {
        private static final long serialVersionUID = 1L;

        Accelerock() {
            super(AttackNamesies.ACCELEROCK, Type.ROCK, MoveCategory.PHYSICAL, 20, "The user smashes into the target at high speed. This move always goes first.");
            super.power = 40;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.priority = 1;
        }
    }

    static class Liquidation extends Attack {
        private static final long serialVersionUID = 1L;

        Liquidation() {
            super(AttackNamesies.LIQUIDATION, Type.WATER, MoveCategory.PHYSICAL, 10, "The user slams into the target using a full-force blast of water. This may also lower the target's Defense stat.");
            super.power = 85;
            super.accuracy = 100;
            super.effectChance = 20;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
            super.statChanges[Stat.DEFENSE.index()] = -1;
        }
    }

    static class SpectralThief extends Attack {
        private static final long serialVersionUID = 1L;

        SpectralThief() {
            super(AttackNamesies.SPECTRAL_THIEF, Type.GHOST, MoveCategory.PHYSICAL, 10, "The user hides in the target's shadow, steals the target's stat boosts, and then attacks.");
            super.power = 90;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public void afterApplyCheck(Battle b, ActivePokemon user, ActivePokemon victim) {
            for (Stat stat : Stat.BATTLE_STATS) {
                int stage = victim.getStages().getStage(stat);
                if (stage > 0) {
                    victim.getStages().resetStage(stat);
                    user.getStages().modifyStage(user, stage, stat, b, CastSource.ATTACK);
                }
            }
        }
    }

    static class SunsteelStrike extends Attack {
        private static final long serialVersionUID = 1L;

        private Effect effect;

        SunsteelStrike() {
            super(AttackNamesies.SUNSTEEL_STRIKE, Type.STEEL, MoveCategory.PHYSICAL, 5, "The user slams into the target with the force of a meteor. This move can be used on the target regardless of its Abilities.");
            super.power = 100;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            this.effect = Effect.cast(PokemonEffectNamesies.BREAKS_THE_MOLD, b, attacking, attacking, CastSource.ATTACK, false);
        }

        @Override
        public void endAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            this.effect.deactivate();
        }
    }

    static class MoongeistBeam extends Attack {
        private static final long serialVersionUID = 1L;

        private Effect effect;

        MoongeistBeam() {
            super(AttackNamesies.MOONGEIST_BEAM, Type.GHOST, MoveCategory.SPECIAL, 5, "The user emits a sinister ray to attack the target. This move can be used on the target regardless of its Abilities.");
            super.power = 100;
            super.accuracy = 100;
        }

        @Override
        public void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            this.effect = Effect.cast(PokemonEffectNamesies.BREAKS_THE_MOLD, b, attacking, attacking, CastSource.ATTACK, false);
        }

        @Override
        public void endAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            this.effect.deactivate();
        }
    }

    static class TearfulLook extends Attack {
        private static final long serialVersionUID = 1L;

        TearfulLook() {
            super(AttackNamesies.TEARFUL_LOOK, Type.NORMAL, MoveCategory.STATUS, 20, "The user gets teary eyed to make the target lose its combative spirit. This lowers the target's Attack and Sp. Atk stats.");
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
            super.statChanges[Stat.ATTACK.index()] = -1;
            super.statChanges[Stat.SP_ATTACK.index()] = -1;
        }
    }

    static class ZingZap extends Attack {
        private static final long serialVersionUID = 1L;

        ZingZap() {
            super(AttackNamesies.ZING_ZAP, Type.ELECTRIC, MoveCategory.PHYSICAL, 10, "A strong electric blast crashes down on the target, giving it an electric shock. This may also make the target flinch.");
            super.power = 80;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 30;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }
    }

    static class GrassPledge extends Attack {
        private static final long serialVersionUID = 1L;

        GrassPledge() {
            super(AttackNamesies.GRASS_PLEDGE, Type.GRASS, MoveCategory.SPECIAL, 10, "A column of grass hits the target. When used with its water equivalent, its power increases and a vast swamp appears.");
            super.power = 80;
            super.accuracy = 100;
        }
    }

    static class FirePledge extends Attack {
        private static final long serialVersionUID = 1L;

        FirePledge() {
            super(AttackNamesies.FIRE_PLEDGE, Type.FIRE, MoveCategory.SPECIAL, 10, "A column of fire hits the target. When used with its grass equivalent, its power increases and a vast sea of fire appears.");
            super.power = 80;
            super.accuracy = 100;
        }
    }

    static class WaterPledge extends Attack {
        private static final long serialVersionUID = 1L;

        WaterPledge() {
            super(AttackNamesies.WATER_PLEDGE, Type.WATER, MoveCategory.SPECIAL, 10, "A column of water strikes the target. When combined with its fire equivalent, its power increases and a rainbow appears.");
            super.power = 80;
            super.accuracy = 100;
        }
    }

    // TODO: Test
    static class StompingTantrum extends Attack implements PowerChangeEffect {
        private static final long serialVersionUID = 1L;

        StompingTantrum() {
            super(AttackNamesies.STOMPING_TANTRUM, Type.GROUND, MoveCategory.PHYSICAL, 10, "Driven by frustration, the user attacks the target. If the user's previous move has failed, the power of this move doubles.");
            super.power = 75;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
            return user.lastMoveSucceeded() ? 2 : 1;
        }
    }

    static class HyperspaceHole extends Attack {
        private static final long serialVersionUID = 1L;

        HyperspaceHole() {
            super(AttackNamesies.HYPERSPACE_HOLE, Type.PSYCHIC, MoveCategory.SPECIAL, 5, "Using a hyperspace hole, the user appears right next to the target and strikes. This also hits a target using a move such as Protect or Detect.");
            super.power = 80;
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
            super.moveTypes.add(MoveType.METRONOMELESS);
        }
    }

    static class HyperspaceFury extends Attack {
        private static final long serialVersionUID = 1L;

        HyperspaceFury() {
            super(AttackNamesies.HYPERSPACE_FURY, Type.DARK, MoveCategory.PHYSICAL, 5, "Using its many arms, the user unleashes a barrage of attacks that ignore the effects of moves like Protect and Detect. But the user’s Defense stat falls.");
            super.power = 100;
            super.moveTypes.add(MoveType.PROTECT_PIERCING);
            super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
            super.moveTypes.add(MoveType.METRONOMELESS);
            super.selfTarget = true;
            super.statChanges[Stat.DEFENSE.index()] = -1;
        }
    }

    static class SteamEruption extends Attack {
        private static final long serialVersionUID = 1L;

        SteamEruption() {
            super(AttackNamesies.STEAM_ERUPTION, Type.WATER, MoveCategory.SPECIAL, 5, "The user immerses the target in superheated steam. This may also leave the target with a burn.");
            super.power = 110;
            super.accuracy = 95;
            super.effectChance = 30;
            super.status = StatusNamesies.BURNED;
            super.moveTypes.add(MoveType.DEFROST);
        }
    }

    static class PhotonGeyser extends Attack {
        private static final long serialVersionUID = 1L;

        private Effect effect;

        PhotonGeyser() {
            super(AttackNamesies.PHOTON_GEYSER, Type.PSYCHIC, MoveCategory.SPECIAL, 5, "The user attacks a target with a pillar of light. This move inflicts Attack or Sp. Atk damage -- whichever stat is higher for the user.");
            super.power = 100;
            super.accuracy = 100;
        }

        @Override
        public void beginAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // If attack stat is higher, switch to physical move
            if (Stat.ATTACK.getBasicStat(b, attacking) > Stat.SP_ATTACK.getBasicStat(b, attacking)) {
                super.category = MoveCategory.PHYSICAL;
            }
            this.effect = Effect.cast(PokemonEffectNamesies.BREAKS_THE_MOLD, b, attacking, attacking, CastSource.ATTACK, false);
        }

        @Override
        public void endAttack(Battle b, ActivePokemon attacking, ActivePokemon defending) {
            // Reset category to special
            super.category = MoveCategory.SPECIAL;
            this.effect.deactivate();
        }
    }

    static class MindBlown extends Attack {
        private static final long serialVersionUID = 1L;

        MindBlown() {
            super(AttackNamesies.MIND_BLOWN, Type.FIRE, MoveCategory.SPECIAL, 5, "The user attacks everything around it by causing its own head to explode. This also damages the user.");
            super.power = 150;
            super.accuracy = 100;
            super.moveTypes.add(MoveType.EXPLODING);
        }

        @Override
        public void uniqueEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
            user.reduceHealthFraction(b, 1/2.0, user.getName() + " blew its mind!!!");
        }
    }

    static class PlasmaFists extends Attack implements ChangeAttackTypeSource {
        private static final long serialVersionUID = 1L;

        PlasmaFists() {
            super(AttackNamesies.PLASMA_FISTS, Type.ELECTRIC, MoveCategory.PHYSICAL, 15, "The user attacks with electrically charged fists. This move changes Normal-type moves to Electric-type moves.");
            super.power = 100;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.CHANGE_ATTACK_TYPE;
            super.moveTypes.add(MoveType.PUNCHING);
        }

        @Override
        public Type getAttackType(Type original) {
            if (original == Type.NORMAL) {
                return Type.ELECTRIC;
            }

            return original;
        }

        @Override
        public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
            return caster.getName() + " electrified " + victim.getName() + "!";
        }
    }

    static class DoubleIronBash extends Attack implements MultiStrikeMove {
        private static final long serialVersionUID = 1L;

        DoubleIronBash() {
            super(AttackNamesies.DOUBLE_IRON_BASH, Type.STEEL, MoveCategory.PHYSICAL, 5, "The user rotates, centering the hex nut in its chest, and then strikes with its arms twice in a row. This may also make the target flinch.");
            super.power = 60;
            super.accuracy = 100;
            super.effect = PokemonEffectNamesies.FLINCH;
            super.effectChance = 30;
            super.moveTypes.add(MoveType.PUNCHING);
            super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
        }

        @Override
        public int getMinHits() {
            return 2;
        }

        @Override
        public int getMaxHits() {
            return 2;
        }
    }
}
