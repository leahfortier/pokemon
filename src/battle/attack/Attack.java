package battle.attack;

import battle.Battle;
import battle.effect.PassableEffect;
import battle.effect.SapHealthEffect;
import battle.effect.attack.ChangeAbilityMove;
import battle.effect.attack.ChangeTypeSource;
import battle.effect.attack.MultiStrikeMove;
import battle.effect.attack.MultiTurnMove;
import battle.effect.attack.SelfHealingMove;
import battle.effect.generic.CastSource;
import battle.effect.generic.Effect;
import battle.effect.generic.EffectInterfaces.AccuracyBypassEffect;
import battle.effect.generic.EffectInterfaces.AdvantageMultiplierMove;
import battle.effect.generic.EffectInterfaces.AlwaysCritEffect;
import battle.effect.generic.EffectInterfaces.ApplyDamageEffect;
import battle.effect.generic.EffectInterfaces.BarrierEffect;
import battle.effect.generic.EffectInterfaces.CrashDamageMove;
import battle.effect.generic.EffectInterfaces.CritBlockerEffect;
import battle.effect.generic.EffectInterfaces.CritStageEffect;
import battle.effect.generic.EffectInterfaces.DefogRelease;
import battle.effect.generic.EffectInterfaces.EffectBlockerEffect;
import battle.effect.generic.EffectInterfaces.MurderEffect;
import battle.effect.generic.EffectInterfaces.OpponentIgnoreStageEffect;
import battle.effect.generic.EffectInterfaces.OpponentStatSwitchingEffect;
import battle.effect.generic.EffectInterfaces.PhysicalContactEffect;
import battle.effect.generic.EffectInterfaces.RapidSpinRelease;
import battle.effect.generic.EffectInterfaces.RecoilMove;
import battle.effect.generic.EffectInterfaces.TakeDamageEffect;
import battle.effect.generic.EffectInterfaces.TargetSwapperEffect;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.PokemonEffect;
import battle.effect.holder.ItemHolder;
import battle.effect.status.Status;
import battle.effect.status.StatusCondition;
import item.Item;
import item.ItemNamesies;
import item.berry.Berry;
import item.berry.GainableEffectBerry;
import item.hold.DriveItem;
import item.hold.GemItem;
import item.hold.HoldItem;
import item.hold.PlateItem;
import main.Global;
import main.Type;
import map.TerrainType;
import message.MessageUpdate;
import message.MessageUpdate.Update;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.Gender;
import pokemon.Stat;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import trainer.Team;
import trainer.Trainer;
import trainer.Trainer.Action;
import trainer.WildPokemon;
import util.GeneralUtils;
import util.RandomUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Attack implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private AttackNamesies namesies;
	private String name;
	private String description;
	private int power;
	private int accuracy;
	private int pp;
	private Type type;
	private MoveCategory category;
	private List<EffectNamesies> effects;
	private int effectChance;
	private StatusCondition status;
	private List<MoveType> moveTypes;
	private boolean selfTarget;
	private int priority;
	private int[] statChanges;
	private boolean printCast;

	public Attack(AttackNamesies namesies, String description, int pp, Type type, MoveCategory category) {
		this.namesies = namesies;
		this.name = namesies.getName();
		this.description = description;
		this.pp = pp;
		this.type = type;
		this.category = category;
		this.effects = new ArrayList<>();
		this.moveTypes = new ArrayList<>();
		this.power = 0;
		this.accuracy = 10000;
		this.selfTarget = false;
		this.priority = 0;
		this.status = StatusCondition.NO_STATUS;
		this.statChanges = new int[Stat.NUM_BATTLE_STATS];
		this.effectChance = 100;
		this.printCast = true;
	}

	public int getPriority(Battle b, ActivePokemon me) {
		return this.priority;
	}
	
	public boolean isSelfTarget() {
		return this.selfTarget;
	}
	
	// Returns true if an attack has secondary effects -- this only applies to physical and special moves
	// Secondary effects include status conditions, confusing, flinching, and stat changes (unless the stat changes are negative for the user)
	public boolean hasSecondaryEffects()
	{
		// Effects are primary for status moves
		if (category == MoveCategory.STATUS) {
			return false;
		}
		
		// If the effect may not necessarily occur, then it is secondary
		if (effectChance < 100) {
			return true;
		}
		
		// Giving the target a status condition is a secondary effect
		if (status != StatusCondition.NO_STATUS) {
			return true;
		}
		
		// Confusion and flinching count as secondary effects -- but I don't think anything else does?
		for (EffectNamesies effect : effects) {
			if (effect == EffectNamesies.CONFUSION || effect == EffectNamesies.FLINCH) {
				return true;
			}
		}
		
		// Stat changes are considered to be secondary effects unless they are negative for the user
		for (int val : this.statChanges) {
			if (val < 0) {
				if (this.selfTarget) {
					continue;
				}
				
				return true;
			}
			
			if (val > 0) {
				return true;
			}
		}
		
		return false;
	}
	
	public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o) {
		return this.accuracy;
	}
	
	public String getAccuracyString() {
		if (this.accuracy > 100) {
			return "--";
		}
		
		return this.accuracy + "";
	}
	
	public MoveCategory getCategory() {
		return this.category;
	}
	
	public int getPP() {
		return this.pp;
	}
	
	public AttackNamesies namesies() {
		return this.namesies;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public boolean isMoveType(MoveType moveType) {
		// TODO: Make sure this is still working -- I might be super dumb
		return this.moveTypes.contains(moveType);
	}
	
	public boolean isMultiTurn(Battle b, ActivePokemon user) {
		if (this instanceof MultiTurnMove) {
			// The Power Herb item allows multi-turn moves that charge first to skip the charge turn -- BUT ONLY ONCE
			if (((MultiTurnMove)this).chargesFirst() && user.isHoldingItem(b, ItemNamesies.POWER_HERB)) {
				user.consumeItem(b);
				return false;
			}
			
			return true;
		}
		
		return false;
	}
	
	public Type getActualType() {
		return this.type;
	}
	
	public Type setType(Battle b, ActivePokemon user) {
		return this.type;
	}
	
	public int getPower() {
		return this.power;
	}
	
	public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
		return this.power;
	}
	
	public String getPowerString() {
		return this.power == 0 ? "--" : this.power + "";
	}
	
	public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
		ActivePokemon target = getTarget(b, me, o);
		
		// Don't do anything for moves that are uneffective
		if (!effective(b, me, target)) {
			return;
		}
		
		// Physical and special attacks -- apply dat damage
		if (category != MoveCategory.STATUS) {
			applyDamage(me, o, b);
		}
		
		// If you got it, flaunt it
		if (canApplyEffects(b, me, target)) {
			applyEffects(b, me, target);
		}
	}
	
	private ActivePokemon getTarget(Battle b, ActivePokemon user, ActivePokemon opponent) {
		if (TargetSwapperEffect.checkSwapTarget(b, user, opponent)) {
			return selfTarget ? opponent : user;
		}
		
		return selfTarget ? user : opponent;
	}
	
	private boolean canApplyEffects(Battle b, ActivePokemon me, ActivePokemon o) {
		int chance = effectChance*(me.hasAbility(AbilityNamesies.SERENE_GRACE) ? 2 : 1);
		if (!RandomUtils.chanceTest(chance)) {
			return false;
		}
		
		// Check the opponents effects and see if it will prevent effects from occurring
		if (EffectBlockerEffect.checkBlocked(b, me, o)) {
			return false;
		}
		
		// Sheer Force prevents the user from having secondary effects for its moves
		if (me.hasAbility(AbilityNamesies.SHEER_FORCE) && me.getAttack().hasSecondaryEffects()) {
			return false;
		}
		
		return true;
	}
	
	private boolean zeroAdvantage(Battle b, ActivePokemon p, ActivePokemon opp) {
		if (Type.getAdvantage(p, opp, b) > 0) {
			return false;
		}
		
		Messages.add(new MessageUpdate("It doesn't affect " + opp.getName() + "!"));
		CrashDamageMove.invokeCrashDamageMove(b, p);
		
		return true;
	}
	
	// Takes type advantage, victim ability, and victim type into account to determine if the attack is effective
	public boolean effective(Battle b, ActivePokemon me, ActivePokemon o) {
		// Self-target moves and field moves don't need to take type advantage always work
		if (this.isSelfTarget() || this.isMoveType(MoveType.FIELD)) {
			return true;
		}
		
		// Non-status moves (AND FUCKING THUNDER WAVE) -- need to check the type chart
		if ((this.category != MoveCategory.STATUS || this.namesies == AttackNamesies.THUNDER_WAVE) && this.zeroAdvantage(b, me, o)) {
			return false;
		}

		// TODO: Should generalize this to extend to more than just type and ability
		// Check if type or ability will block the attack
		if (Ability.blockAttack(b, me, o) || Type.blockAttack(b, me, o)) {
			return false;
		}
		
		// You passed!!
		return true;
	}
	
	// Physical and Special moves -- do dat damage!
	public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b)
	{	
		// Print Advantage
		double adv = Type.getAdvantage(me, o, b);
		if (adv < 1) Messages.add(new MessageUpdate("It's not very effective..."));
		else if (adv > 1) Messages.add(new MessageUpdate("It's super effective!"));
		
		// Deal damage
		int damage = o.reduceHealth(b, b.calculateDamage(me, o));
		
		// Check if target is fainted
		o.isFainted(b);
		
		if (me.isFainted(b)) {
			return;
		}
		
		// Apply a damage effect
		ApplyDamageEffect.invokeApplyDamageEffect(b, me, o, damage);
		
		if (me.isFainted(b)) {
			return;
		}
		
		// Take Recoil Damage
		RecoilMove.invokeRecoilMove(b, me, damage);
	
		if (me.isFainted(b)) {
			return;
		}

		// Sap the Health
		if (this instanceof SapHealthEffect) {
			((SapHealthEffect)this).sapHealth(b, me, o, damage, true);
		}
		
		// Effects that apply when a Pokemon makes physical contact with them
		if (isMoveType(MoveType.PHYSICAL_CONTACT)) {
			PhysicalContactEffect.invokePhysicalContactEffect(b, me, o);
		}

		// TODO: This might need to be moved higher like before the recoil stuff so it gets activated even if the attacker dies
		// Effects that apply to the opponent when they take damage
		TakeDamageEffect.invokeTakeDamageEffect(b, me, o);
	}

	public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
		// Kill yourself!!
		if (isMoveType(MoveType.USER_FAINTS)) {
			user.killKillKillMurderMurderMurder(b);
		}
		
		// Don't apply effects to a fainted Pokemon
		if (victim.isFainted(b)) {
			return;
		}
		
		// Give Status Condition
		if (status != StatusCondition.NO_STATUS) {
			boolean success = Status.giveStatus(b, user, victim, status);
			if (!success && canPrintFail()) {
				Messages.add(new MessageUpdate(Status.getFailMessage(b, user, victim, status)));
			}
		}
		
		// Give Stat Changes
		victim.modifyStages(b, user, statChanges, CastSource.ATTACK);
		
		// Give additional effects
		for (EffectNamesies effectNamesies : effects) {
            Effect effect = effectNamesies.getEffect();
			if (effect.applies(b, user, victim, CastSource.ATTACK)) {
				effect.cast(b, user, victim, CastSource.ATTACK, canPrintCast());
			}
			else if (canPrintFail()) {
				Messages.add(new MessageUpdate(effect.getFailMessage(b, user, victim)));
			}
		}
		
		// Heal yourself!!
		if (this instanceof SelfHealingMove) {
			((SelfHealingMove)this).heal(user, victim, b);
		}
	}
	
	public boolean canPrintFail() {
		return this.effectChance == 100 && this.category == MoveCategory.STATUS;
	}
	
	public boolean canPrintCast() {
		return this.printCast;
	}
	
	// To be overridden if necessary
	public void startTurn(Battle b, ActivePokemon me) {}

	public static boolean isAttack(String name) {
		return AttackNamesies.tryValueOf(name) != null;
	}

	// EVERYTHING BELOW IS GENERATED ###
	/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

	static class Tackle extends Attack {
		private static final long serialVersionUID = 1L;

		Tackle() {
			super(AttackNamesies.TACKLE, "A physical attack in which the user charges and slams into the target with its whole body.", 35, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 35;
			super.accuracy = 95;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class LeechSeed extends Attack {
		private static final long serialVersionUID = 1L;

		LeechSeed() {
			super(AttackNamesies.LEECH_SEED, "A seed is planted on the target. It steals some HP from the target every turn.", 10, Type.GRASS, MoveCategory.STATUS);
			super.accuracy = 90;
			super.effects.add(EffectNamesies.LEECH_SEED);
		}
	}

	static class ThunderWave extends Attack {
		private static final long serialVersionUID = 1L;

		ThunderWave() {
			super(AttackNamesies.THUNDER_WAVE, "A weak electric charge is launched at the target. It causes paralysis if it hits.", 20, Type.ELECTRIC, MoveCategory.STATUS);
			super.accuracy = 100;
			super.status = StatusCondition.PARALYZED;
		}
	}

	static class PoisonPowder extends Attack {
		private static final long serialVersionUID = 1L;

		PoisonPowder() {
			super(AttackNamesies.POISON_POWDER, "The user scatters a cloud of poisonous dust on the target. It may poison the target.", 35, Type.POISON, MoveCategory.STATUS);
			super.accuracy = 75;
			super.status = StatusCondition.POISONED;
			super.moveTypes.add(MoveType.POWDER);
		}
	}

	static class SleepPowder extends Attack {
		private static final long serialVersionUID = 1L;

		SleepPowder() {
			super(AttackNamesies.SLEEP_POWDER, "The user scatters a big cloud of sleep-inducing dust around the target.", 15, Type.GRASS, MoveCategory.STATUS);
			super.accuracy = 75;
			super.status = StatusCondition.ASLEEP;
			super.moveTypes.add(MoveType.POWDER);
		}
	}

	static class Toxic extends Attack implements AccuracyBypassEffect {
		private static final long serialVersionUID = 1L;

		Toxic() {
			super(AttackNamesies.TOXIC, "A move that leaves the target badly poisoned. Its poison damage worsens every turn.", 10, Type.POISON, MoveCategory.STATUS);
			super.accuracy = 90;
			super.effects.add(EffectNamesies.BAD_POISON);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			// Poison-type Pokemon bypass accuracy
			return attacking.isType(b, Type.POISON);
		}
	}

	static class Ember extends Attack {
		private static final long serialVersionUID = 1L;

		Ember() {
			super(AttackNamesies.EMBER, "The target is attacked with small flames. It may also leave the target with a burn.", 25, Type.FIRE, MoveCategory.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.BURNED;
		}
	}

	static class Growl extends Attack {
		private static final long serialVersionUID = 1L;

		Growl() {
			super(AttackNamesies.GROWL, "The user growls in an endearing way, making the opposing team less wary. The foes' Attack stats are lowered.", 40, Type.NORMAL, MoveCategory.STATUS);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.statChanges[Stat.ATTACK.index()] = -1;
		}
	}

	static class Scratch extends Attack {
		private static final long serialVersionUID = 1L;

		Scratch() {
			super(AttackNamesies.SCRATCH, "Hard, pointed, and sharp claws rake the target to inflict damage.", 35, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class VineWhip extends Attack {
		private static final long serialVersionUID = 1L;

		VineWhip() {
			super(AttackNamesies.VINE_WHIP, "The target is struck with slender, whiplike vines to inflict damage.", 25, Type.GRASS, MoveCategory.PHYSICAL);
			super.power = 45;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class SonicBoom extends Attack {
		private static final long serialVersionUID = 1L;

		SonicBoom() {
			super(AttackNamesies.SONIC_BOOM, "The target is hit with a destructive shock wave that always inflicts 20 HP damage.", 20, Type.NORMAL, MoveCategory.SPECIAL);
			super.accuracy = 90;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			o.reduceHealth(b, 20);
		}
	}

	static class Smokescreen extends Attack {
		private static final long serialVersionUID = 1L;

		Smokescreen() {
			super(AttackNamesies.SMOKESCREEN, "The user releases an obscuring cloud of smoke or ink. It reduces the target's accuracy.", 20, Type.NORMAL, MoveCategory.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	static class TakeDown extends Attack implements RecoilMove {
		private static final long serialVersionUID = 1L;

		TakeDown() {
			super(AttackNamesies.TAKE_DOWN, "A reckless, full-body charge attack for slamming into the target. It also damages the user a little.", 20, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 90;
			super.accuracy = 85;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyRecoil(Battle b, ActivePokemon user, int damage) {
			if (user.hasAbility(AbilityNamesies.ROCK_HEAD) || user.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(new MessageUpdate(user.getName() + " was hurt by recoil!"));
			user.reduceHealth(b, (int)Math.ceil(damage/4.0), false);
		}
	}

	static class Struggle extends Attack implements RecoilMove {
		private static final long serialVersionUID = 1L;

		Struggle() {
			super(AttackNamesies.STRUGGLE, "An attack that is used in desperation only if the user has no PP. It also hurts the user slightly.", 1, Type.NO_TYPE, MoveCategory.PHYSICAL);
			super.power = 50;
			super.moveTypes.add(MoveType.ENCORELESS);
			super.moveTypes.add(MoveType.MIMICLESS);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyRecoil(Battle b, ActivePokemon user, int damage) {
			Messages.add(new MessageUpdate(user.getName() + " was hurt by recoil!"));
			user.reduceHealth(b, user.getMaxHP()/4, false);
		}
	}

	static class RazorLeaf extends Attack implements CritStageEffect {
		private static final long serialVersionUID = 1L;

		RazorLeaf() {
			super(AttackNamesies.RAZOR_LEAF, "Sharp-edged leaves are launched to slash at the opposing team. Critical hits land more easily.", 25, Type.GRASS, MoveCategory.PHYSICAL);
			super.power = 55;
			super.accuracy = 95;
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	static class SweetScent extends Attack {
		private static final long serialVersionUID = 1L;

		SweetScent() {
			super(AttackNamesies.SWEET_SCENT, "A sweet scent that lowers the opposing team's evasiveness. It also lures wild Pok\u00e9mon if used in grass, etc.", 20, Type.NORMAL, MoveCategory.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.EVASION.index()] = -2;
		}
	}

	static class Growth extends Attack {
		private static final long serialVersionUID = 1L;

		Growth() {
			super(AttackNamesies.GROWTH, "The user's body grows all at once, raising the Attack and Sp. Atk stats.", 20, Type.NORMAL, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			// Doubles stat changes in the sunlight
			if (b.getWeather().namesies() == EffectNamesies.SUNNY) {
				int[] statChanges = super.statChanges.clone();
				for (int i = 0; i < super.statChanges.length; i++) {
					super.statChanges[i] *= 2;
				}
				
				super.applyEffects(b, user, victim);
				super.statChanges = statChanges;
				return;
			}
			
			super.applyEffects(b, user, victim);
		}
	}

	static class DoubleEdge extends Attack implements RecoilMove {
		private static final long serialVersionUID = 1L;

		DoubleEdge() {
			super(AttackNamesies.DOUBLE_EDGE, "A reckless, life-risking tackle. It also damages the user by a fairly large amount, however.", 15, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyRecoil(Battle b, ActivePokemon user, int damage) {
			if (user.hasAbility(AbilityNamesies.ROCK_HEAD) || user.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(new MessageUpdate(user.getName() + " was hurt by recoil!"));
			user.reduceHealth(b, (int)Math.ceil(damage/3.0), false);
		}
	}

	static class SeedBomb extends Attack {
		private static final long serialVersionUID = 1L;

		SeedBomb() {
			super(AttackNamesies.SEED_BOMB, "The user slams a barrage of hard-shelled seeds down on the target from above.", 15, Type.GRASS, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.BOMB_BALL);
		}
	}

	static class Synthesis extends Attack implements SelfHealingMove {
		private static final long serialVersionUID = 1L;

		Synthesis() {
			super(AttackNamesies.SYNTHESIS, "The user restores its own HP. The amount of HP regained varies with the weather.", 5, Type.GRASS, MoveCategory.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b) {
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			switch (b.getWeather().namesies()) {
				case CLEAR_SKIES:
					victim.healHealthFraction(1/2.0);
					break;
				case SUNNY:
					victim.healHealthFraction(2/3.0);
					break;
				case HAILING:
				case RAINING:
				case SANDSTORM:
					victim.healHealthFraction(1/4.0);
					break;
				default:
					Global.error("Funky weather problems!!!!");
					break;
			}
			
			Messages.add(new MessageUpdate(victim.getName() + "'s health was restored!").updatePokemon(b, victim));
		}
	}

	static class Recover extends Attack implements SelfHealingMove {
		private static final long serialVersionUID = 1L;

		Recover() {
			super(AttackNamesies.RECOVER, "Restoring its own cells, the user restores its own HP by half of its max HP.", 10, Type.NORMAL, MoveCategory.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b) {
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			victim.healHealthFraction(1/2.0);
			
			Messages.add(new MessageUpdate(victim.getName() + "'s health was restored!").updatePokemon(b, victim));
		}
	}

	static class DragonRage extends Attack {
		private static final long serialVersionUID = 1L;

		DragonRage() {
			super(AttackNamesies.DRAGON_RAGE, "This attack hits the target with a shock wave of pure rage. This attack always inflicts 40 HP damage.", 10, Type.DRAGON, MoveCategory.SPECIAL);
			super.accuracy = 100;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			o.reduceHealth(b, 40);
		}
	}

	static class ScaryFace extends Attack {
		private static final long serialVersionUID = 1L;

		ScaryFace() {
			super(AttackNamesies.SCARY_FACE, "The user frightens the target with a scary face to harshly reduce its Speed stat.", 10, Type.NORMAL, MoveCategory.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.SPEED.index()] = -2;
		}
	}

	static class FireFang extends Attack {
		private static final long serialVersionUID = 1L;

		FireFang() {
			super(AttackNamesies.FIRE_FANG, "The user bites with flame-cloaked fangs. It may also make the target flinch or leave it burned.", 15, Type.FIRE, MoveCategory.PHYSICAL);
			super.power = 65;
			super.accuracy = 95;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 20;
			super.moveTypes.add(MoveType.BITING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			// If the effect is being applied, 50/50 chance to give a status condition vs. flinching
			if (RandomUtils.chanceTest(50)) {
				Status.giveStatus(b, user, victim, StatusCondition.BURNED);
				return;
			}
			
			super.applyEffects(b, user, victim);
		}
	}

	static class FlameBurst extends Attack {
		private static final long serialVersionUID = 1L;

		FlameBurst() {
			super(AttackNamesies.FLAME_BURST, "The user attacks the target with a bursting flame.", 15, Type.FIRE, MoveCategory.SPECIAL);
			super.power = 70;
			super.accuracy = 100;
		}
	}

	static class Bite extends Attack {
		private static final long serialVersionUID = 1L;

		Bite() {
			super(AttackNamesies.BITE, "The target is bitten with viciously sharp fangs. It may make the target flinch.", 25, Type.DARK, MoveCategory.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 30;
			super.moveTypes.add(MoveType.BITING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Slash extends Attack implements CritStageEffect {
		private static final long serialVersionUID = 1L;

		Slash() {
			super(AttackNamesies.SLASH, "The target is attacked with a slash of claws or blades. Critical hits land more easily.", 20, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	static class TailWhip extends Attack {
		private static final long serialVersionUID = 1L;

		TailWhip() {
			super(AttackNamesies.TAIL_WHIP, "The user wags its tail cutely, making opposing Pok\u00e9mon less wary and lowering their Defense stat.", 30, Type.NORMAL, MoveCategory.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.DEFENSE.index()] = -1;
		}
	}

	static class SolarBeam extends Attack implements MultiTurnMove {
		private static final long serialVersionUID = 1L;

		SolarBeam() {
			super(AttackNamesies.SOLAR_BEAM, "A two-turn attack. The user gathers light, then blasts a bundled beam on the second turn.", 10, Type.GRASS, MoveCategory.SPECIAL);
			super.power = 120;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
		}

		public boolean isMultiTurn(Battle b, ActivePokemon user) {
			return super.isMultiTurn(b, user) && b.getWeather().namesies() != EffectNamesies.SUNNY;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			switch (b.getWeather().namesies()) {
				case HAILING:
				case RAINING:
				case SANDSTORM:
					return super.power/2;
				default:
					return super.power;
			}
		}

		public boolean chargesFirst() {
			return true;
		}

		public boolean semiInvulnerability() {
			return false;
		}

		public void charge(ActivePokemon user, Battle b) {
			Messages.add(new MessageUpdate(getChargeMessage(user)));
		}

		private String getChargeMessage(ActivePokemon user) {
			return user.getName() + " began taking in sunlight!";
		}
	}

	static class SolarBlade extends Attack implements MultiTurnMove {
		private static final long serialVersionUID = 1L;

		SolarBlade() {
			super(AttackNamesies.SOLAR_BLADE, "In this two-turn attack, the user gathers light and fills a blade with the light's energy, attacking the target on the next turn.", 10, Type.GRASS, MoveCategory.PHYSICAL);
			super.power = 125;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public boolean isMultiTurn(Battle b, ActivePokemon user) {
			return super.isMultiTurn(b, user) && b.getWeather().namesies() != EffectNamesies.SUNNY;
		}

		public boolean chargesFirst() {
			return true;
		}

		public boolean semiInvulnerability() {
			return false;
		}

		public void charge(ActivePokemon user, Battle b) {
			Messages.add(new MessageUpdate(getChargeMessage(user)));
		}

		private String getChargeMessage(ActivePokemon user) {
			return user.getName() + " began taking in sunlight!";
		}
	}

	static class Flamethrower extends Attack {
		private static final long serialVersionUID = 1L;

		Flamethrower() {
			super(AttackNamesies.FLAMETHROWER, "The target is scorched with an intense blast of fire. It may also leave the target with a burn.", 15, Type.FIRE, MoveCategory.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.BURNED;
		}
	}

	static class Fly extends Attack implements MultiTurnMove {
		private static final long serialVersionUID = 1L;

		Fly() {
			super(AttackNamesies.FLY, "The user soars, then strikes its target on the second turn. It can also be used for flying to any familiar town.", 15, Type.FLYING, MoveCategory.PHYSICAL);
			super.power = 90;
			super.accuracy = 95;
			super.moveTypes.add(MoveType.AIRBORNE);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public boolean chargesFirst() {
			return true;
		}

		public boolean semiInvulnerability() {
			return true;
		}

		public void charge(ActivePokemon user, Battle b) {
			Messages.add(new MessageUpdate(getChargeMessage(user)));
		}

		private String getChargeMessage(ActivePokemon user) {
			return user.getName() + " flew up high!";
		}
	}

	static class FireSpin extends Attack {
		private static final long serialVersionUID = 1L;

		FireSpin() {
			super(AttackNamesies.FIRE_SPIN, "The target becomes trapped within a fierce vortex of fire that rages for four to five turns.", 15, Type.FIRE, MoveCategory.SPECIAL);
			super.power = 35;
			super.accuracy = 85;
			super.effects.add(EffectNamesies.FIRE_SPIN);
		}
	}

	static class Inferno extends Attack {
		private static final long serialVersionUID = 1L;

		Inferno() {
			super(AttackNamesies.INFERNO, "The user attacks by engulfing the target in an intense fire. It leaves the target with a burn.", 5, Type.FIRE, MoveCategory.SPECIAL);
			super.power = 100;
			super.accuracy = 50;
			super.status = StatusCondition.BURNED;
		}
	}

	static class DragonClaw extends Attack {
		private static final long serialVersionUID = 1L;

		DragonClaw() {
			super(AttackNamesies.DRAGON_CLAW, "The user slashes the target with huge, sharp claws.", 15, Type.DRAGON, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class ShadowClaw extends Attack implements CritStageEffect {
		private static final long serialVersionUID = 1L;

		ShadowClaw() {
			super(AttackNamesies.SHADOW_CLAW, "The user slashes with a sharp claw made from shadows. Critical hits land more easily.", 15, Type.GHOST, MoveCategory.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	static class AirSlash extends Attack {
		private static final long serialVersionUID = 1L;

		AirSlash() {
			super(AttackNamesies.AIR_SLASH, "The user attacks with a blade of air that slices even the sky. It may also make the target flinch.", 15, Type.FLYING, MoveCategory.SPECIAL);
			super.power = 75;
			super.accuracy = 95;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 30;
		}
	}

	static class WingAttack extends Attack {
		private static final long serialVersionUID = 1L;

		WingAttack() {
			super(AttackNamesies.WING_ATTACK, "The target is struck with large, imposing wings spread wide to inflict damage.", 35, Type.FLYING, MoveCategory.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class HeatWave extends Attack {
		private static final long serialVersionUID = 1L;

		HeatWave() {
			super(AttackNamesies.HEAT_WAVE, "The user attacks by exhaling hot breath on the opposing team. It may also leave targets with a burn.", 10, Type.FIRE, MoveCategory.SPECIAL);
			super.power = 95;
			super.accuracy = 90;
			super.effectChance = 10;
			super.status = StatusCondition.BURNED;
		}
	}

	static class FlareBlitz extends Attack implements RecoilMove {
		private static final long serialVersionUID = 1L;

		FlareBlitz() {
			super(AttackNamesies.FLARE_BLITZ, "The user cloaks itself in fire and charges at the target. The user sustains serious damage and may leave the target burned.", 15, Type.FIRE, MoveCategory.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.BURNED;
			super.moveTypes.add(MoveType.DEFROST);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyRecoil(Battle b, ActivePokemon user, int damage) {
			if (user.hasAbility(AbilityNamesies.ROCK_HEAD) || user.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(new MessageUpdate(user.getName() + " was hurt by recoil!"));
			user.reduceHealth(b, (int)Math.ceil(damage/3.0), false);
		}
	}

	static class FlashCannon extends Attack {
		private static final long serialVersionUID = 1L;

		FlashCannon() {
			super(AttackNamesies.FLASH_CANNON, "The user gathers all its light energy and releases it at once. It may also lower the target's Sp. Def stat.", 10, Type.STEEL, MoveCategory.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.effectChance = 10;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
		}
	}

	static class Bubble extends Attack {
		private static final long serialVersionUID = 1L;

		Bubble() {
			super(AttackNamesies.BUBBLE, "A spray of countless bubbles is jetted at the opposing team. It may also lower the targets' Speed stats.", 30, Type.WATER, MoveCategory.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.effectChance = 10;
			super.statChanges[Stat.SPEED.index()] = -1;
		}
	}

	static class Withdraw extends Attack {
		private static final long serialVersionUID = 1L;

		Withdraw() {
			super(AttackNamesies.WITHDRAW, "The user withdraws its body into its hard shell, raising its Defense stat.", 40, Type.WATER, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 1;
		}
	}

	static class WaterGun extends Attack {
		private static final long serialVersionUID = 1L;

		WaterGun() {
			super(AttackNamesies.WATER_GUN, "The target is blasted with a forceful shot of water.", 25, Type.WATER, MoveCategory.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
		}
	}

	static class RapidSpin extends Attack {
		private static final long serialVersionUID = 1L;

		RapidSpin() {
			super(AttackNamesies.RAPID_SPIN, "A spin attack that can also eliminate such moves as Bind, Wrap, Leech Seed, and Spikes.", 40, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 20;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			RapidSpinRelease.release(b, user);
		}
	}

	static class Reflect extends Attack {
		private static final long serialVersionUID = 1L;

		Reflect() {
			super(AttackNamesies.REFLECT, "A wondrous wall of light is put up to suppress damage from physical attacks for five turns.", 20, Type.PSYCHIC, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.REFLECT);
			super.selfTarget = true;
		}
	}

	static class SpikyShield extends Attack {
		private static final long serialVersionUID = 1L;

		SpikyShield() {
			super(AttackNamesies.SPIKY_SHIELD, "In addition to protecting the user from attacks, this move also damages any attacker who makes direct contact.", 10, Type.GRASS, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.SPIKY_SHIELD);
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
			super(AttackNamesies.BANEFUL_BUNKER, "In addition to protecting the user from attacks, this move also poisons any attacker that makes direct contact.", 10, Type.POISON, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.BANEFUL_BUNKER);
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
			super(AttackNamesies.KINGS_SHIELD, "The user takes a defensive stance while it protects itself from damage. It also harshly lowers the Attack stat of any attacker who makes direct contact.", 10, Type.STEEL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.KINGS_SHIELD);
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
			super(AttackNamesies.PROTECT, "It enables the user to evade all attacks. Its chance of failing rises if it is used in succession.", 10, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.PROTECTING);
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
			super(AttackNamesies.DETECT, "It enables the user to evade all attacks. Its chance of failing rises if it is used in succession.", 5, Type.FIGHTING, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.PROTECTING);
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
			super(AttackNamesies.QUICK_GUARD, "The user protects itself and its allies from priority moves. If used in succession, its chance of failing rises.", 15, Type.FIGHTING, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.QUICK_GUARD);
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
			super(AttackNamesies.ENDURE, "The user endures any attack with at least 1 HP. Its chance of failing rises if it is used in succession.", 10, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.BRACING);
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
			super(AttackNamesies.WATER_PULSE, "The user attacks the target with a pulsing blast of water. It may also confuse the target.", 20, Type.WATER, MoveCategory.SPECIAL);
			super.power = 60;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.CONFUSION);
			super.effectChance = 20;
			super.moveTypes.add(MoveType.AURA_PULSE);
		}
	}

	static class ConfusionDamage extends Attack implements CritBlockerEffect {
		private static final long serialVersionUID = 1L;

		ConfusionDamage() {
			super(AttackNamesies.CONFUSION_DAMAGE, "None", 1, Type.NO_TYPE, MoveCategory.PHYSICAL);
			super.power = 40;
		}

		public boolean blockCrits() {
			return true;
		}
	}

	static class ConfuseRay extends Attack {
		private static final long serialVersionUID = 1L;

		ConfuseRay() {
			super(AttackNamesies.CONFUSE_RAY, "The target is exposed to a sinister ray that triggers confusion.", 10, Type.GHOST, MoveCategory.STATUS);
			super.accuracy = 100;
			super.effects.add(EffectNamesies.CONFUSION);
		}
	}

	static class AquaTail extends Attack {
		private static final long serialVersionUID = 1L;

		AquaTail() {
			super(AttackNamesies.AQUA_TAIL, "The user attacks by swinging its tail as if it were a vicious wave in a raging storm.", 10, Type.WATER, MoveCategory.PHYSICAL);
			super.power = 90;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class SkullBash extends Attack implements MultiTurnMove {
		private static final long serialVersionUID = 1L;

		SkullBash() {
			super(AttackNamesies.SKULL_BASH, "The user tucks in its head to raise its Defense in the first turn, then rams the target on the next turn.", 10, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 130;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public boolean chargesFirst() {
			return true;
		}

		public boolean semiInvulnerability() {
			return false;
		}

		public void charge(ActivePokemon user, Battle b) {
			Messages.add(new MessageUpdate(getChargeMessage(user)));
			user.getAttributes().modifyStage(user, user, 1, Stat.DEFENSE, b, CastSource.ATTACK);
		}

		private String getChargeMessage(ActivePokemon user) {
			return user.getName() + " lowered its head!";
		}
	}

	static class IronDefense extends Attack {
		private static final long serialVersionUID = 1L;

		IronDefense() {
			super(AttackNamesies.IRON_DEFENSE, "The user hardens its body's surface like iron, sharply raising its Defense stat.", 15, Type.STEEL, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 2;
		}
	}

	static class HydroPump extends Attack {
		private static final long serialVersionUID = 1L;

		HydroPump() {
			super(AttackNamesies.HYDRO_PUMP, "The target is blasted by a huge volume of water launched under great pressure.", 5, Type.WATER, MoveCategory.SPECIAL);
			super.power = 110;
			super.accuracy = 80;
		}
	}

	static class RainDance extends Attack {
		private static final long serialVersionUID = 1L;

		RainDance() {
			super(AttackNamesies.RAIN_DANCE, "The user summons a heavy rain that falls for five turns, powering up Water-type moves.", 5, Type.WATER, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.RAINING);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}
	}

	static class SunnyDay extends Attack {
		private static final long serialVersionUID = 1L;

		SunnyDay() {
			super(AttackNamesies.SUNNY_DAY, "The user intensifies the sun for five turns, powering up Fire-type moves.", 5, Type.FIRE, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.SUNNY);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}
	}

	static class Sandstorm extends Attack {
		private static final long serialVersionUID = 1L;

		Sandstorm() {
			super(AttackNamesies.SANDSTORM, "A five-turn sandstorm is summoned to hurt all combatants except the Rock, Ground, and Steel types.", 10, Type.ROCK, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.SANDSTORM);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}
	}

	static class Hail extends Attack {
		private static final long serialVersionUID = 1L;

		Hail() {
			super(AttackNamesies.HAIL, "The user summons a hailstorm lasting five turns. It damages all Pok\u00e9mon except the Ice type.", 10, Type.ICE, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.HAILING);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}
	}

	static class PetalDance extends Attack {
		private static final long serialVersionUID = 1L;

		PetalDance() {
			super(AttackNamesies.PETAL_DANCE, "The user attacks the target by scattering petals for two to three turns. The user then becomes confused.", 10, Type.GRASS, MoveCategory.SPECIAL);
			super.power = 120;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.SELF_CONFUSION);
			super.selfTarget = true;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Thrash extends Attack {
		private static final long serialVersionUID = 1L;

		Thrash() {
			super(AttackNamesies.THRASH, "The user rampages and attacks for two to three turns. It then becomes confused, however.", 10, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.SELF_CONFUSION);
			super.selfTarget = true;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class HyperBeam extends Attack implements MultiTurnMove {
		private static final long serialVersionUID = 1L;

		HyperBeam() {
			super(AttackNamesies.HYPER_BEAM, "The target is attacked with a powerful beam. The user must rest on the next turn to regain its energy.", 5, Type.NORMAL, MoveCategory.SPECIAL);
			super.power = 150;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
		}

		public boolean chargesFirst() {
			return false;
		}

		public boolean semiInvulnerability() {
			return false;
		}

		public void charge(ActivePokemon user, Battle b) {
			Messages.add(new MessageUpdate(getChargeMessage(user)));
		}

		private String getChargeMessage(ActivePokemon user) {
			return user.getName() + " must recharge!";
		}
	}

	static class StringShot extends Attack {
		private static final long serialVersionUID = 1L;

		StringShot() {
			super(AttackNamesies.STRING_SHOT, "The targets are bound with silk blown from the user's mouth. This silk reduces the targets' Speed stat.", 40, Type.BUG, MoveCategory.STATUS);
			super.accuracy = 95;
			super.statChanges[Stat.SPEED.index()] = -2;
		}
	}

	static class BugBite extends Attack {
		private static final long serialVersionUID = 1L;

		BugBite() {
			super(AttackNamesies.BUG_BITE, "The user bites the target. If the target is holding a Berry, the user eats it and gains its effect.", 20, Type.BUG, MoveCategory.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			Item i = victim.getHeldItem(b);
			if (i instanceof Berry) {
				Messages.add(new MessageUpdate(user.getName() + " ate " + victim.getName() + "'s " + i.getName() + "!"));
				victim.consumeItem(b);
				
				if (i instanceof GainableEffectBerry) {
					((GainableEffectBerry)i).gainBerryEffect(b, user, CastSource.USE_ITEM);
				}
			}
		}
	}

	static class Harden extends Attack {
		private static final long serialVersionUID = 1L;

		Harden() {
			super(AttackNamesies.HARDEN, "The user stiffens all the muscles in its body to raise its Defense stat.", 30, Type.NORMAL, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 1;
		}
	}

	static class Confusion extends Attack {
		private static final long serialVersionUID = 1L;

		Confusion() {
			super(AttackNamesies.CONFUSION, "The target is hit by a weak telekinetic force. It may also leave the target confused.", 25, Type.PSYCHIC, MoveCategory.SPECIAL);
			super.power = 50;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.CONFUSION);
			super.effectChance = 10;
		}
	}

	static class StunSpore extends Attack {
		private static final long serialVersionUID = 1L;

		StunSpore() {
			super(AttackNamesies.STUN_SPORE, "The user scatters a cloud of paralyzing powder. It may leave the target with paralysis.", 30, Type.GRASS, MoveCategory.STATUS);
			super.accuracy = 75;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.POWDER);
		}
	}

	static class Gust extends Attack implements AccuracyBypassEffect {
		private static final long serialVersionUID = 1L;

		Gust() {
			super(AttackNamesies.GUST, "A gust of wind is whipped up by wings and launched at the target to inflict damage.", 35, Type.FLYING, MoveCategory.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			// Twice as strong when the opponent is flying
			return super.power*(o.isSemiInvulnerableFlying() ? 2 : 1);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			// Always hit when the opponent is flying
			return defending.isSemiInvulnerableFlying();
		}
	}

	static class Supersonic extends Attack {
		private static final long serialVersionUID = 1L;

		Supersonic() {
			super(AttackNamesies.SUPERSONIC, "The user generates odd sound waves from its body. It may confuse the target.", 20, Type.NORMAL, MoveCategory.STATUS);
			super.accuracy = 55;
			super.effects.add(EffectNamesies.CONFUSION);
			super.moveTypes.add(MoveType.SOUND_BASED);
		}
	}

	static class Psybeam extends Attack {
		private static final long serialVersionUID = 1L;

		Psybeam() {
			super(AttackNamesies.PSYBEAM, "The target is attacked with a peculiar ray. It may also cause confusion.", 20, Type.PSYCHIC, MoveCategory.SPECIAL);
			super.power = 65;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.CONFUSION);
			super.effectChance = 10;
		}
	}

	static class SilverWind extends Attack {
		private static final long serialVersionUID = 1L;

		SilverWind() {
			super(AttackNamesies.SILVER_WIND, "The target is attacked with powdery scales blown by wind. It may also raise all the user's stats.", 5, Type.BUG, MoveCategory.SPECIAL);
			super.power = 60;
			super.accuracy = 100;
			super.effectChance = 10;
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
			super.statChanges[Stat.SPEED.index()] = 1;
			super.statChanges[Stat.ACCURACY.index()] = 1;
			super.statChanges[Stat.EVASION.index()] = 1;
		}
	}

	static class Tailwind extends Attack {
		private static final long serialVersionUID = 1L;

		Tailwind() {
			super(AttackNamesies.TAILWIND, "The user whips up a turbulent whirlwind that ups the Speed of all party Pok\u00e9mon for four turns.", 30, Type.FLYING, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.TAILWIND);
			super.selfTarget = true;
		}
	}

	static class MorningSun extends Attack implements SelfHealingMove {
		private static final long serialVersionUID = 1L;

		MorningSun() {
			super(AttackNamesies.MORNING_SUN, "The user restores its own HP. The amount of HP regained varies with the weather.", 5, Type.NORMAL, MoveCategory.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b) {
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			switch (b.getWeather().namesies()) {
				case CLEAR_SKIES:
					victim.healHealthFraction(1/2.0);
					break;
				case SUNNY:
					victim.healHealthFraction(2/3.0);
					break;
				case HAILING:
				case RAINING:
				case SANDSTORM:
					victim.healHealthFraction(1/4.0);
					break;
				default:
					Global.error("Funky weather problems!!!!");
					break;
			}
			
			Messages.add(new MessageUpdate(victim.getName() + "'s health was restored!").updatePokemon(b, victim));
		}
	}

	static class Safeguard extends Attack {
		private static final long serialVersionUID = 1L;

		Safeguard() {
			super(AttackNamesies.SAFEGUARD, "The user creates a protective field that prevents status problems for five turns.", 25, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.SAFEGUARD);
			super.selfTarget = true;
		}
	}

	static class Captivate extends Attack {
		private static final long serialVersionUID = 1L;

		Captivate() {
			super(AttackNamesies.CAPTIVATE, "If it is the opposite gender of the user, the target is charmed into harshly lowering its Sp. Atk stat.", 20, Type.NORMAL, MoveCategory.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.SP_ATTACK.index()] = -2;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			// Captivate does not work on members of the same sex or on victims with the Oblivious ability
			if (victim.hasAbility(AbilityNamesies.OBLIVIOUS)) {
				Messages.add(new MessageUpdate(victim.getName() + "'s " + victim.getAbility().getName() + " prevents it from being captivated!"));
			}
			else if (!Gender.oppositeGenders(user, victim)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
			}
			else {
				super.applyEffects(b, user, victim);
			}
		}
	}

	static class BugBuzz extends Attack {
		private static final long serialVersionUID = 1L;

		BugBuzz() {
			super(AttackNamesies.BUG_BUZZ, "The user vibrates its wings to generate a damaging sound wave. It may also lower the target's Sp. Def stat.", 10, Type.BUG, MoveCategory.SPECIAL);
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
			super(AttackNamesies.QUIVER_DANCE, "The user lightly performs a beautiful, mystic dance. It boosts the user's Sp. Atk, Sp. Def, and Speed stats.", 20, Type.BUG, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
			super.statChanges[Stat.SPEED.index()] = 1;
		}
	}

	static class Encore extends Attack {
		private static final long serialVersionUID = 1L;

		Encore() {
			super(AttackNamesies.ENCORE, "The user compels the target to keep using only the move it last used for three turns.", 5, Type.NORMAL, MoveCategory.STATUS);
			super.accuracy = 100;
			super.effects.add(EffectNamesies.ENCORE);
			super.moveTypes.add(MoveType.ENCORELESS);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
		}
	}

	static class PoisonSting extends Attack {
		private static final long serialVersionUID = 1L;

		PoisonSting() {
			super(AttackNamesies.POISON_STING, "The user stabs the target with a poisonous stinger. This may also poison the target.", 35, Type.POISON, MoveCategory.PHYSICAL);
			super.power = 15;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.POISONED;
		}
	}

	static class FuryAttack extends Attack implements MultiStrikeMove {
		private static final long serialVersionUID = 1L;

		FuryAttack() {
			super(AttackNamesies.FURY_ATTACK, "The target is jabbed repeatedly with a horn or beak two to five times in a row.", 20, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 15;
			super.accuracy = 85;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int hits = this.getNumHits(me);
			
			int hit = 1;
			for (; hit <= hits; hit++) {
				Messages.add(new MessageUpdate("Hit " + hit + "!"));
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b)) {
					break;
				}
			}
			
			hit--;
			
			// Print hits and gtfo
			Messages.add(new MessageUpdate("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!"));
		}

		public int getMinHits() {
			return 2;
		}

		public int getMaxHits() {
			return 5;
		}
	}

	static class FalseSwipe extends Attack {
		private static final long serialVersionUID = 1L;

		FalseSwipe() {
			super(AttackNamesies.FALSE_SWIPE, "A restrained attack that prevents the target from fainting. The target is left with at least 1 HP.", 40, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			o.addEffect((PokemonEffect)EffectNamesies.BRACING.getEffect());
			super.applyDamage(me, o, b);
			o.getAttributes().removeEffect(EffectNamesies.BRACING);
		}
	}

	static class Disable extends Attack {
		private static final long serialVersionUID = 1L;

		Disable() {
			super(AttackNamesies.DISABLE, "For four turns, this move prevents the target from using the move it last used.", 20, Type.NORMAL, MoveCategory.STATUS);
			super.accuracy = 100;
			super.effects.add(EffectNamesies.DISABLE);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
		}
	}

	static class FocusEnergy extends Attack {
		private static final long serialVersionUID = 1L;

		FocusEnergy() {
			super(AttackNamesies.FOCUS_ENERGY, "The user takes a deep breath and focuses so that critical hits land more easily.", 30, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.RAISE_CRITS);
			super.selfTarget = true;
		}
	}

	static class Twineedle extends Attack implements MultiStrikeMove {
		private static final long serialVersionUID = 1L;

		Twineedle() {
			super(AttackNamesies.TWINEEDLE, "The user damages the target twice in succession by jabbing it with two spikes. It may also poison the target.", 20, Type.BUG, MoveCategory.PHYSICAL);
			super.power = 25;
			super.accuracy = 100;
			super.effectChance = 20;
			super.status = StatusCondition.POISONED;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int hits = this.getNumHits(me);
			
			int hit = 1;
			for (; hit <= hits; hit++) {
				Messages.add(new MessageUpdate("Hit " + hit + "!"));
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b)) {
					break;
				}
			}
			
			hit--;
			
			// Print hits and gtfo
			Messages.add(new MessageUpdate("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!"));
		}

		public int getMinHits() {
			return 2;
		}

		public int getMaxHits() {
			return 2;
		}
	}

	static class Rage extends Attack {
		private static final long serialVersionUID = 1L;

		Rage() {
			super(AttackNamesies.RAGE, "As long as this move is in use, the power of rage raises the Attack stat each time the user is hit in battle.", 20, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 20;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			// TODO: Update for Gen 5 -- Increase attack when hit while using Rage
			return super.power*me.getAttributes().getCount();
		}
	}

	static class Pursuit extends Attack {
		private static final long serialVersionUID = 1L;

		Pursuit() {
			super(AttackNamesies.PURSUIT, "An attack move that inflicts double damage if used on a target that is switching out of battle.", 20, Type.DARK, MoveCategory.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int getPriority(Battle b, ActivePokemon me) {
			Team trainer = b.getTrainer(!me.isPlayer()); // TODO: Make switching occur at its priority
			if (trainer instanceof Trainer && ((Trainer)trainer).getAction() == Action.SWITCH) return 7;
			return super.priority;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			// TODO: Once the Begin- shit is resolved, then this should be combined there
			Team trainer = b.getTrainer(o.isPlayer());
			if (trainer instanceof Trainer && ((Trainer)trainer).getAction() == Action.SWITCH) {
				return super.power*2;
			}
			
			return super.power;
		}
	}

	static class ToxicSpikes extends Attack {
		private static final long serialVersionUID = 1L;

		ToxicSpikes() {
			super(AttackNamesies.TOXIC_SPIKES, "The user lays a trap of poison spikes at the opponent's feet. They poison opponents that switch into battle.", 20, Type.POISON, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.TOXIC_SPIKES);
			super.moveTypes.add(MoveType.FIELD);
		}
	}

	static class PinMissile extends Attack implements MultiStrikeMove {
		private static final long serialVersionUID = 1L;

		PinMissile() {
			super(AttackNamesies.PIN_MISSILE, "Sharp spikes are shot at the target in rapid succession. They hit two to five times in a row.", 20, Type.BUG, MoveCategory.PHYSICAL);
			super.power = 25;
			super.accuracy = 95;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int hits = this.getNumHits(me);
			
			int hit = 1;
			for (; hit <= hits; hit++) {
				Messages.add(new MessageUpdate("Hit " + hit + "!"));
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b)) {
					break;
				}
			}
			
			hit--;
			
			// Print hits and gtfo
			Messages.add(new MessageUpdate("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!"));
		}

		public int getMinHits() {
			return 2;
		}

		public int getMaxHits() {
			return 5;
		}
	}

	static class Agility extends Attack {
		private static final long serialVersionUID = 1L;

		Agility() {
			super(AttackNamesies.AGILITY, "The user relaxes and lightens its body to move faster. It sharply boosts the Speed stat.", 30, Type.PSYCHIC, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.SPEED.index()] = 2;
		}
	}

	static class Assurance extends Attack {
		private static final long serialVersionUID = 1L;

		Assurance() {
			super(AttackNamesies.ASSURANCE, "If the target has already taken some damage in the same turn, this attack's power is doubled.", 10, Type.DARK, MoveCategory.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(me.getAttributes().hasTakenDamage() ? 2 : 1);
		}
	}

	static class PoisonJab extends Attack {
		private static final long serialVersionUID = 1L;

		PoisonJab() {
			super(AttackNamesies.POISON_JAB, "The target is stabbed with a tentacle or arm steeped in poison. It may also poison the target.", 20, Type.POISON, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.POISONED;
			super.moveTypes.add(MoveType.PUNCHING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Endeavor extends Attack {
		private static final long serialVersionUID = 1L;

		Endeavor() {
			super(AttackNamesies.ENDEAVOR, "An attack move that cuts down the target's HP to equal the user's HP.", 5, Type.NORMAL, MoveCategory.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			o.reduceHealth(b, o.getHP() - me.getHP());
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			if (me.getHP() >= o.getHP()) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			super.apply(me, o, b);
		}
	}

	static class SandAttack extends Attack {
		private static final long serialVersionUID = 1L;

		SandAttack() {
			super(AttackNamesies.SAND_ATTACK, "Sand is hurled in the target's face, reducing its accuracy.", 15, Type.GROUND, MoveCategory.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	static class QuickAttack extends Attack {
		private static final long serialVersionUID = 1L;

		QuickAttack() {
			super(AttackNamesies.QUICK_ATTACK, "The user lunges at the target at a speed that makes it almost invisible. It is sure to strike first.", 30, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.priority = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Twister extends Attack implements AccuracyBypassEffect {
		private static final long serialVersionUID = 1L;

		Twister() {
			super(AttackNamesies.TWISTER, "The user whips up a vicious tornado to tear at the opposing team. It may also make targets flinch.", 20, Type.DRAGON, MoveCategory.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 20;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			// Twice as strong when the opponent is flying
			return super.power*(o.isSemiInvulnerableFlying() ? 2 : 1);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			// Always hit when the opponent is flying
			return defending.isSemiInvulnerableFlying();
		}
	}

	static class FeatherDance extends Attack {
		private static final long serialVersionUID = 1L;

		FeatherDance() {
			super(AttackNamesies.FEATHER_DANCE, "The user covers the target's body with a mass of down that harshly lowers its Attack stat.", 15, Type.FLYING, MoveCategory.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.ATTACK.index()] = -2;
		}
	}

	static class Roost extends Attack implements SelfHealingMove, ChangeTypeSource {
		private static final long serialVersionUID = 1L;
		private boolean healFail;

		Roost() {
			super(AttackNamesies.ROOST, "The user lands and rests its body. It restores the user's HP by up to half of its max HP.", 10, Type.FLYING, MoveCategory.STATUS);
			super.selfTarget = true;
			super.printCast = false;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			healFail = true;
			super.applyEffects(b, user, victim);
			if (!healFail && getType(b, user, victim) != null) {
				EffectNamesies.CHANGE_TYPE.getEffect().cast(b, user, victim, CastSource.ATTACK, super.printCast);
				user.getEffect(EffectNamesies.CHANGE_TYPE).setTurns(1);
			}
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b) {
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			healFail = false;
			victim.healHealthFraction(1/2.0);
			
			Messages.add(new MessageUpdate(victim.getName() + "'s health was restored!").updatePokemon(b, victim));
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
			Type[] type = victim.getType(b);
			
			// TODO: Rewrite this because it looks stupid
			if (type[0] == Type.FLYING) {
				return new Type[] { type[1], Type.NO_TYPE };
			}
			
			if (type[1] == Type.FLYING) {
				return new Type[] { type[0], Type.NO_TYPE };
			}
			
			return null;
		}
	}

	static class ThunderShock extends Attack {
		private static final long serialVersionUID = 1L;

		ThunderShock() {
			super(AttackNamesies.THUNDER_SHOCK, "A jolt of electricity is hurled at the target to inflict damage. It may also leave the target with paralysis.", 30, Type.ELECTRIC, MoveCategory.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.PARALYZED;
		}
	}

	static class MirrorMove extends Attack {
		private static final long serialVersionUID = 1L;

		MirrorMove() {
			super(AttackNamesies.MIRROR_MOVE, "The user counters the target by mimicking the target's last move.", 20, Type.FLYING, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.ENCORELESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.MIRRORLESS);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			Move mirror = o.getAttributes().getLastMoveUsed();
			if (mirror == null || mirror.getAttack().isMoveType(MoveType.MIRRORLESS)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			me.callNewMove(b, o, new Move(mirror.getAttack()));
		}
	}

	static class Hurricane extends Attack implements AccuracyBypassEffect {
		private static final long serialVersionUID = 1L;

		Hurricane() {
			super(AttackNamesies.HURRICANE, "The user attacks by wrapping its opponent in a fierce wind that flies up into the sky. It may also confuse the target.", 10, Type.FLYING, MoveCategory.SPECIAL);
			super.power = 110;
			super.accuracy = 70;
			super.effects.add(EffectNamesies.CONFUSION);
			super.effectChance = 30;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			// Twice as strong when the opponent is flying
			return super.power*(o.isSemiInvulnerableFlying() ? 2 : 1);
		}

		public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o) {
			// Accuracy is only 50% when sunny
			if (b.getWeather().namesies() == EffectNamesies.SUNNY) {
				return 50;
			}
			
			return super.accuracy;
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			// Always hits when the opponent is flying or it is raining (unless they're non-flying semi-invulnerable)
			return defending.isSemiInvulnerableFlying() || (b.getWeather().namesies() == EffectNamesies.RAINING && defending.isSemiInvulnerable());
		}
	}

	static class HyperFang extends Attack {
		private static final long serialVersionUID = 1L;

		HyperFang() {
			super(AttackNamesies.HYPER_FANG, "The user bites hard on the target with its sharp front fangs. It may also make the target flinch.", 15, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 90;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 10;
			super.moveTypes.add(MoveType.BITING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class SuckerPunch extends Attack {
		private static final long serialVersionUID = 1L;

		SuckerPunch() {
			super(AttackNamesies.SUCKER_PUNCH, "This move enables the user to attack first. It fails if the foe is not readying an attack, however.", 5, Type.DARK, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.priority = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			if (o.getMove().getAttack().getCategory() == MoveCategory.STATUS) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			super.apply(me, o, b);
		}
	}

	static class Crunch extends Attack {
		private static final long serialVersionUID = 1L;

		Crunch() {
			super(AttackNamesies.CRUNCH, "The user crunches up the foe with sharp fangs. It may also lower the target's Defense stat.", 15, Type.DARK, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.effectChance = 20;
			super.moveTypes.add(MoveType.BITING);
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class SuperFang extends Attack {
		private static final long serialVersionUID = 1L;

		SuperFang() {
			super(AttackNamesies.SUPER_FANG, "The user chomps hard on the foe with its sharp front fangs. It cuts the target's HP to half.", 10, Type.NORMAL, MoveCategory.PHYSICAL);
			super.accuracy = 90;
			super.moveTypes.add(MoveType.BITING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			o.reduceHealth(b, (int)Math.ceil(o.getHP()/2.0));
		}
	}

	static class SwordsDance extends Attack {
		private static final long serialVersionUID = 1L;

		SwordsDance() {
			super(AttackNamesies.SWORDS_DANCE, "A frenetic dance to uplift the fighting spirit. It sharply raises the user's Attack stat.", 30, Type.NORMAL, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 2;
		}
	}

	static class Peck extends Attack {
		private static final long serialVersionUID = 1L;

		Peck() {
			super(AttackNamesies.PECK, "The foe is jabbed with a sharply pointed beak or horn.", 35, Type.FLYING, MoveCategory.PHYSICAL);
			super.power = 35;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Leer extends Attack {
		private static final long serialVersionUID = 1L;

		Leer() {
			super(AttackNamesies.LEER, "The foe is given an intimidating leer with sharp eyes. The target's Defense stat is reduced.", 30, Type.NORMAL, MoveCategory.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.DEFENSE.index()] = -1;
		}
	}

	static class AerialAce extends Attack {
		private static final long serialVersionUID = 1L;

		AerialAce() {
			super(AttackNamesies.AERIAL_ACE, "The user confounds the foe with speed, then slashes. The attack lands without fail.", 20, Type.FLYING, MoveCategory.PHYSICAL);
			super.power = 60;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class DrillPeck extends Attack {
		private static final long serialVersionUID = 1L;

		DrillPeck() {
			super(AttackNamesies.DRILL_PECK, "A corkscrewing attack with the sharp beak acting as a drill.", 20, Type.FLYING, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Pluck extends Attack {
		private static final long serialVersionUID = 1L;

		Pluck() {
			super(AttackNamesies.PLUCK, "The user pecks the foe. If the foe is holding a Berry, the user plucks it and gains its effect.", 20, Type.FLYING, MoveCategory.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			Item i = victim.getHeldItem(b);
			if (i instanceof Berry) {
				Messages.add(new MessageUpdate(user.getName() + " ate " + victim.getName() + "'s " + i.getName() + "!"));
				victim.consumeItem(b);
				
				if (i instanceof GainableEffectBerry) {
					((GainableEffectBerry)i).gainBerryEffect(b, user, CastSource.USE_ITEM);
				}
			}
		}
	}

	static class DrillRun extends Attack implements CritStageEffect {
		private static final long serialVersionUID = 1L;

		DrillRun() {
			super(AttackNamesies.DRILL_RUN, "The user crashes into its target while rotating its body like a drill. Critical hits land more easily.", 10, Type.GROUND, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 95;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	static class Wrap extends Attack {
		private static final long serialVersionUID = 1L;

		Wrap() {
			super(AttackNamesies.WRAP, "A long body or vines are used to wrap and squeeze the target for four to five turns.", 20, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 15;
			super.accuracy = 90;
			super.effects.add(EffectNamesies.WRAPPED);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Glare extends Attack {
		private static final long serialVersionUID = 1L;

		Glare() {
			super(AttackNamesies.GLARE, "The user intimidates the target with the pattern on its belly to cause paralysis.", 30, Type.NORMAL, MoveCategory.STATUS);
			super.accuracy = 100;
			super.status = StatusCondition.PARALYZED;
		}
	}

	static class Screech extends Attack {
		private static final long serialVersionUID = 1L;

		Screech() {
			super(AttackNamesies.SCREECH, "An earsplitting screech harshly reduces the target's Defense stat.", 40, Type.NORMAL, MoveCategory.STATUS);
			super.accuracy = 85;
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.statChanges[Stat.DEFENSE.index()] = -2;
		}
	}

	static class Acid extends Attack {
		private static final long serialVersionUID = 1L;

		Acid() {
			super(AttackNamesies.ACID, "The opposing team is attacked with a spray of harsh acid. The acid may also lower the targets' Sp. Def stats.", 30, Type.POISON, MoveCategory.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.effectChance = 10;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
		}
	}

	static class Stockpile extends Attack {
		private static final long serialVersionUID = 1L;

		Stockpile() {
			super(AttackNamesies.STOCKPILE, "The user charges up power and raises both its Defense and Sp. Def. The move can be used three times.", 20, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.STOCKPILE);
			super.selfTarget = true;
		}
	}

	static class SpitUp extends Attack {
		private static final long serialVersionUID = 1L;

		SpitUp() {
			super(AttackNamesies.SPIT_UP, "The power stored using the move Stockpile is released at once in an attack. The more power is stored, the greater the damage.", 10, Type.NORMAL, MoveCategory.SPECIAL);
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			PokemonEffect stockpile = me.getEffect(EffectNamesies.STOCKPILE);
			
			// TODO: Look at this again
			// I really don't like this, because there's no way this value should actually be getting used -- but it's getting set now always even if the attack isn't going to work and we don't want a NullPointerException
			if (stockpile == null) {
				return super.setPower(b, me, o);
			}
			
			int turns = stockpile.getTurns();
			if (turns <= 0) {
				Global.error("Stockpile turns should never be nonpositive");
			}
			
			// Max power is 300
			return (int)Math.min(turns, 3)*100;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			PokemonEffect stockpile = me.getEffect(EffectNamesies.STOCKPILE);
			if (stockpile == null) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			super.apply(me, o, b);
			
			// Stockpile ends after Spit up is used
			stockpile.deactivate();
		}
	}

	static class Swallow extends Attack implements SelfHealingMove {
		private static final long serialVersionUID = 1L;

		Swallow() {
			super(AttackNamesies.SWALLOW, "The power stored using the move Stockpile is absorbed by the user to heal its HP. Storing more power heals more HP.", 10, Type.NORMAL, MoveCategory.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b) {
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			PokemonEffect stockpile = user.getEffect(EffectNamesies.STOCKPILE);
			if (stockpile == null) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			if (stockpile.getTurns() <= 0) {
				Global.error("Stockpile turns should never be nonpositive");
			}
			
			// TODO: This doesn't need to be in a switch statement
			// Heals differently based on number of stockpile turns
			switch (stockpile.getTurns()) {
				case 1:
					victim.healHealthFraction(1/4.0);
					break;
				case 2:
					victim.healHealthFraction(1/2.0);
					break;
				default:
					victim.healHealthFraction(1);
					break;
			}
			
			// Stockpile ends after Swallow is used
			stockpile.deactivate();
			
			Messages.add(new MessageUpdate(victim.getName() + "'s health was restored!").updatePokemon(b, victim));
		}
	}

	static class AcidSpray extends Attack {
		private static final long serialVersionUID = 1L;

		AcidSpray() {
			super(AttackNamesies.ACID_SPRAY, "The user spits fluid that works to melt the target. This harshly reduces the target's Sp. Def stat.", 20, Type.POISON, MoveCategory.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.BOMB_BALL);
			super.statChanges[Stat.SP_DEFENSE.index()] = -2;
		}
	}

	static class MudBomb extends Attack {
		private static final long serialVersionUID = 1L;

		MudBomb() {
			super(AttackNamesies.MUD_BOMB, "The user launches a hard-packed mud ball to attack. It may also lower the target's accuracy.", 10, Type.GROUND, MoveCategory.SPECIAL);
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
			super(AttackNamesies.HAZE, "The user creates a haze that eliminates every stat change among all the Pok\u00e9mon engaged in battle.", 30, Type.ICE, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			user.getAttributes().resetStages();
			victim.getAttributes().resetStages();
			Messages.add(new MessageUpdate("All stat changes were eliminated!"));
		}
	}

	static class Coil extends Attack {
		private static final long serialVersionUID = 1L;

		Coil() {
			super(AttackNamesies.COIL, "The user coils up and concentrates. This raises its Attack and Defense stats as well as its accuracy.", 20, Type.POISON, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.statChanges[Stat.ACCURACY.index()] = 1;
		}
	}

	static class GunkShot extends Attack {
		private static final long serialVersionUID = 1L;

		GunkShot() {
			super(AttackNamesies.GUNK_SHOT, "The user shoots filthy garbage at the target to attack. It may also poison the target.", 5, Type.POISON, MoveCategory.PHYSICAL);
			super.power = 120;
			super.accuracy = 80;
			super.effectChance = 30;
			super.status = StatusCondition.POISONED;
		}
	}

	static class IceFang extends Attack {
		private static final long serialVersionUID = 1L;

		IceFang() {
			super(AttackNamesies.ICE_FANG, "The user bites with cold-infused fangs. It may also make the target flinch or leave it frozen.", 15, Type.ICE, MoveCategory.PHYSICAL);
			super.power = 65;
			super.accuracy = 95;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 20;
			super.moveTypes.add(MoveType.BITING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			// If the effect is being applied, 50/50 chance to give a status condition vs. flinching
			if (RandomUtils.chanceTest(50)) {
				Status.giveStatus(b, user, victim, StatusCondition.FROZEN);
				return;
			}
			
			super.applyEffects(b, user, victim);
		}
	}

	static class ThunderFang extends Attack {
		private static final long serialVersionUID = 1L;

		ThunderFang() {
			super(AttackNamesies.THUNDER_FANG, "The user bites with electrified fangs. It may also make the target flinch or leave it with paralysis.", 15, Type.ELECTRIC, MoveCategory.PHYSICAL);
			super.power = 65;
			super.accuracy = 95;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 20;
			super.moveTypes.add(MoveType.BITING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			// If the effect is being applied, 50/50 chance to give a status condition vs. flinching
			if (RandomUtils.chanceTest(50)) {
				Status.giveStatus(b, user, victim, StatusCondition.PARALYZED);
				return;
			}
			
			super.applyEffects(b, user, victim);
		}
	}

	static class ElectroBall extends Attack {
		private static final long serialVersionUID = 1L;

		ElectroBall() {
			super(AttackNamesies.ELECTRO_BALL, "The user hurls an electric orb at the target. The faster the user is than the target, the greater the damage.", 10, Type.ELECTRIC, MoveCategory.SPECIAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.BOMB_BALL);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			double ratio = (double)Stat.getStat(Stat.SPEED, o, me, b)/Stat.getStat(Stat.SPEED, me, o, b);
			if (ratio > .5) return 60;
			if (ratio > .33) return 80;
			if (ratio > .25) return 120;
			return 150;
		}
	}

	static class DoubleTeam extends Attack {
		private static final long serialVersionUID = 1L;

		DoubleTeam() {
			super(AttackNamesies.DOUBLE_TEAM, "By moving rapidly, the user makes illusory copies of itself to raise its evasiveness.", 15, Type.NORMAL, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.EVASION.index()] = 1;
		}
	}

	static class Slam extends Attack {
		private static final long serialVersionUID = 1L;

		Slam() {
			super(AttackNamesies.SLAM, "The target is slammed with a long tail, vines, etc., to inflict damage.", 20, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 75;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Thunderbolt extends Attack {
		private static final long serialVersionUID = 1L;

		Thunderbolt() {
			super(AttackNamesies.THUNDERBOLT, "A strong electric blast is loosed at the target. It may also leave the target with paralysis.", 15, Type.ELECTRIC, MoveCategory.SPECIAL);
			super.power = 95;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.PARALYZED;
		}
	}

	static class Feint extends Attack {
		private static final long serialVersionUID = 1L;

		Feint() {
			super(AttackNamesies.FEINT, "An attack that hits a target using Protect or Detect. It also lifts the effects of those moves.", 10, Type.NORMAL, MoveCategory.PHYSICAL);
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
			super(AttackNamesies.DISCHARGE, "A flare of electricity is loosed to strike the area around the user. It may also cause paralysis.", 15, Type.ELECTRIC, MoveCategory.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.PARALYZED;
		}
	}

	static class LightScreen extends Attack {
		private static final long serialVersionUID = 1L;

		LightScreen() {
			super(AttackNamesies.LIGHT_SCREEN, "A wondrous wall of light is put up to suppress damage from special attacks for five turns.", 30, Type.PSYCHIC, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.LIGHT_SCREEN);
			super.selfTarget = true;
		}
	}

	static class Thunder extends Attack implements AccuracyBypassEffect {
		private static final long serialVersionUID = 1L;

		Thunder() {
			super(AttackNamesies.THUNDER, "A wicked thunderbolt is dropped on the target to inflict damage. It may also leave the target with paralysis.", 10, Type.ELECTRIC, MoveCategory.SPECIAL);
			super.power = 110;
			super.accuracy = 70;
			super.effectChance = 30;
			super.status = StatusCondition.PARALYZED;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			// Twice as strong when the opponent is flying
			return super.power*(o.isSemiInvulnerableFlying() ? 2 : 1);
		}

		public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o) {
			// Accuracy is only 50% when sunny
			if (b.getWeather().namesies() == EffectNamesies.SUNNY) {
				return 50;
			}
			
			return super.accuracy;
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			// Always hits when the opponent is flying or it is raining (unless they're non-flying semi-invulnerable)
			return defending.isSemiInvulnerableFlying() || (b.getWeather().namesies() == EffectNamesies.RAINING && defending.isSemiInvulnerable());
		}
	}

	static class DefenseCurl extends Attack {
		private static final long serialVersionUID = 1L;

		DefenseCurl() {
			super(AttackNamesies.DEFENSE_CURL, "The user curls up to conceal weak spots and raise its Defense stat.", 40, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.USED_DEFENSE_CURL);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 1;
		}
	}

	static class Swift extends Attack {
		private static final long serialVersionUID = 1L;

		Swift() {
			super(AttackNamesies.SWIFT, "Star-shaped rays are shot at the opposing team. This attack never misses.", 20, Type.NORMAL, MoveCategory.SPECIAL);
			super.power = 60;
		}
	}

	static class FurySwipes extends Attack implements MultiStrikeMove {
		private static final long serialVersionUID = 1L;

		FurySwipes() {
			super(AttackNamesies.FURY_SWIPES, "The target is raked with sharp claws or scythes for two to five times in quick succession.", 15, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 18;
			super.accuracy = 80;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int hits = this.getNumHits(me);
			
			int hit = 1;
			for (; hit <= hits; hit++) {
				Messages.add(new MessageUpdate("Hit " + hit + "!"));
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b)) {
					break;
				}
			}
			
			hit--;
			
			// Print hits and gtfo
			Messages.add(new MessageUpdate("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!"));
		}

		public int getMinHits() {
			return 2;
		}

		public int getMaxHits() {
			return 5;
		}
	}

	static class Rollout extends Attack {
		private static final long serialVersionUID = 1L;

		Rollout() {
			super(AttackNamesies.ROLLOUT, "The user continually rolls into the target over five turns. It becomes stronger each time it hits.", 20, Type.ROCK, MoveCategory.PHYSICAL);
			super.power = 30;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			// TODO: Combine these types of moves
			return super.power*(int)Math.min(me.getAttributes().getCount(), 5)*(me.hasEffect(EffectNamesies.USED_DEFENSE_CURL) ? 2 : 1);
		}
	}

	static class FuryCutter extends Attack {
		private static final long serialVersionUID = 1L;

		FuryCutter() {
			super(AttackNamesies.FURY_CUTTER, "The target is slashed with scythes or claws. Its power increases if it hits in succession.", 20, Type.BUG, MoveCategory.PHYSICAL);
			super.power = 40;
			super.accuracy = 95;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(int)Math.min(5, me.getAttributes().getCount());
		}
	}

	static class SandTomb extends Attack {
		private static final long serialVersionUID = 1L;

		SandTomb() {
			super(AttackNamesies.SAND_TOMB, "The user traps the target inside a harshly raging sandstorm for four to five turns.", 15, Type.GROUND, MoveCategory.PHYSICAL);
			super.power = 35;
			super.accuracy = 85;
			super.effects.add(EffectNamesies.SAND_TOMB);
		}
	}

	static class GyroBall extends Attack {
		private static final long serialVersionUID = 1L;

		GyroBall() {
			super(AttackNamesies.GYRO_BALL, "The user tackles the target with a high-speed spin. The slower the user, the greater the damage.", 5, Type.STEEL, MoveCategory.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.BOMB_BALL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return (int)Math.min(150, 25.0*Stat.getStat(Stat.SPEED, o, me, b)/Stat.getStat(Stat.SPEED, me, o, b));
		}
	}

	static class CrushClaw extends Attack {
		private static final long serialVersionUID = 1L;

		CrushClaw() {
			super(AttackNamesies.CRUSH_CLAW, "The user slashes the target with hard and sharp claws. It may also lower the target's Defense.", 10, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 75;
			super.accuracy = 95;
			super.effectChance = 50;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class DoubleKick extends Attack implements MultiStrikeMove {
		private static final long serialVersionUID = 1L;

		DoubleKick() {
			super(AttackNamesies.DOUBLE_KICK, "The target is quickly kicked twice in succession using both feet.", 30, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 30;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int hits = this.getNumHits(me);
			
			int hit = 1;
			for (; hit <= hits; hit++) {
				Messages.add(new MessageUpdate("Hit " + hit + "!"));
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b)) {
					break;
				}
			}
			
			hit--;
			
			// Print hits and gtfo
			Messages.add(new MessageUpdate("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!"));
		}

		public int getMinHits() {
			return 2;
		}

		public int getMaxHits() {
			return 2;
		}
	}

	static class PoisonTail extends Attack implements CritStageEffect {
		private static final long serialVersionUID = 1L;

		PoisonTail() {
			super(AttackNamesies.POISON_TAIL, "The user hits the target with its tail. It may also poison the target. Critical hits land more easily.", 25, Type.POISON, MoveCategory.PHYSICAL);
			super.power = 50;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.POISONED;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	static class Flatter extends Attack {
		private static final long serialVersionUID = 1L;

		Flatter() {
			super(AttackNamesies.FLATTER, "Flattery is used to confuse the target. However, it also raises the target's Sp. Atk stat.", 15, Type.DARK, MoveCategory.STATUS);
			super.accuracy = 100;
			super.effects.add(EffectNamesies.CONFUSION);
			super.statChanges[Stat.SP_ATTACK.index()] = 2;
		}
	}

	static class PoisonFang extends Attack {
		private static final long serialVersionUID = 1L;

		PoisonFang() {
			super(AttackNamesies.POISON_FANG, "The user bites the target with toxic fangs. It may also leave the target badly poisoned.", 15, Type.POISON, MoveCategory.PHYSICAL);
			super.power = 50;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.BAD_POISON);
			super.effectChance = 50;
			super.moveTypes.add(MoveType.BITING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class ChipAway extends Attack implements OpponentIgnoreStageEffect {
		private static final long serialVersionUID = 1L;

		ChipAway() {
			super(AttackNamesies.CHIP_AWAY, "Looking for an opening, the user strikes continually. The target's stat changes don't affect this attack's damage.", 20, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public boolean ignoreStage(Stat s) {
			return !s.user();
		}
	}

	static class BodySlam extends Attack implements AccuracyBypassEffect {
		private static final long serialVersionUID = 1L;

		BodySlam() {
			super(AttackNamesies.BODY_SLAM, "The user drops onto the target with its full body weight. It may also leave the target with paralysis.", 15, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 85;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(o.hasEffect(EffectNamesies.USED_MINIMIZE) ? 2 : 1);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			return !defending.isSemiInvulnerable() && defending.hasEffect(EffectNamesies.USED_MINIMIZE);
		}
	}

	static class EarthPower extends Attack {
		private static final long serialVersionUID = 1L;

		EarthPower() {
			super(AttackNamesies.EARTH_POWER, "The user makes the ground under the target erupt with power. It may also lower the target's Sp. Def.", 10, Type.GROUND, MoveCategory.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
			super.effectChance = 10;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
		}
	}

	static class Superpower extends Attack {
		private static final long serialVersionUID = 1L;

		Superpower() {
			super(AttackNamesies.SUPERPOWER, "The user attacks the target with great power. However, it also lowers the user's Attack and Defense.", 5, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = -1;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class HornAttack extends Attack {
		private static final long serialVersionUID = 1L;

		HornAttack() {
			super(AttackNamesies.HORN_ATTACK, "The target is jabbed with a sharply pointed horn to inflict damage.", 25, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 65;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class HornDrill extends Attack {
		private static final long serialVersionUID = 1L;

		HornDrill() {
			super(AttackNamesies.HORN_DRILL, "The user stabs the target with a horn that rotates like a drill. If it hits, the target faints instantly.", 5, Type.NORMAL, MoveCategory.PHYSICAL);
			super.accuracy = 30;
			super.moveTypes.add(MoveType.ONE_HIT_KO);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			// Fails when the opponent is at a higher level than the user
			if (me.getLevel() < o.getLevel()) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			// Sturdy prevents OHKO moves if the user is not a mold breaker
			if (o.hasAbility(AbilityNamesies.STURDY) && !me.breaksTheMold()) {
				Messages.add(new MessageUpdate(o.getName() + "'s " + AbilityNamesies.STURDY.getName() + " prevents OHKO moves!"));
				return;
			}
			
			// Certain death
			o.reduceHealth(b, o.getHP());
			Messages.add(new MessageUpdate("It's a One-Hit KO!"));
		}

		public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.accuracy + (me.getLevel() - o.getLevel());
		}
	}

	static class Megahorn extends Attack {
		private static final long serialVersionUID = 1L;

		Megahorn() {
			super(AttackNamesies.MEGAHORN, "Using its tough and impressive horn, the user rams into the target with no letup.", 10, Type.BUG, MoveCategory.PHYSICAL);
			super.power = 120;
			super.accuracy = 85;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Pound extends Attack {
		private static final long serialVersionUID = 1L;

		Pound() {
			super(AttackNamesies.POUND, "The target is physically pounded with a long tail or a foreleg, etc.", 35, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Sing extends Attack {
		private static final long serialVersionUID = 1L;

		Sing() {
			super(AttackNamesies.SING, "A soothing lullaby is sung in a calming voice that puts the target into a deep slumber.", 15, Type.NORMAL, MoveCategory.STATUS);
			super.accuracy = 55;
			super.status = StatusCondition.ASLEEP;
			super.moveTypes.add(MoveType.SOUND_BASED);
		}
	}

	static class DoubleSlap extends Attack implements MultiStrikeMove {
		private static final long serialVersionUID = 1L;

		DoubleSlap() {
			super(AttackNamesies.DOUBLE_SLAP, "The target is slapped repeatedly, back and forth, two to five times in a row.", 10, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 15;
			super.accuracy = 85;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int hits = this.getNumHits(me);
			
			int hit = 1;
			for (; hit <= hits; hit++) {
				Messages.add(new MessageUpdate("Hit " + hit + "!"));
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b)) {
					break;
				}
			}
			
			hit--;
			
			// Print hits and gtfo
			Messages.add(new MessageUpdate("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!"));
		}

		public int getMinHits() {
			return 2;
		}

		public int getMaxHits() {
			return 5;
		}
	}

	static class Wish extends Attack {
		private static final long serialVersionUID = 1L;

		Wish() {
			super(AttackNamesies.WISH, "One turn after this move is used, the target's HP is restored by half the user's maximum HP.", 10, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.WISH);
			super.selfTarget = true;
		}
	}

	static class Minimize extends Attack {
		private static final long serialVersionUID = 1L;

		Minimize() {
			super(AttackNamesies.MINIMIZE, "The user compresses its body to make itself look smaller, which sharply raises its evasiveness.", 20, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.USED_MINIMIZE);
			super.selfTarget = true;
			super.statChanges[Stat.EVASION.index()] = 2;
		}
	}

	static class WakeUpSlap extends Attack {
		private static final long serialVersionUID = 1L;

		WakeUpSlap() {
			super(AttackNamesies.WAKE_UP_SLAP, "This attack inflicts big damage on a sleeping target. It also wakes the target up, however.", 10, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(o.hasStatus(StatusCondition.ASLEEP) ? 2 : 1);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (victim.hasStatus(StatusCondition.ASLEEP)) {
				Status.removeStatus(b, victim, CastSource.ATTACK);
			}
		}
	}

	static class CosmicPower extends Attack {
		private static final long serialVersionUID = 1L;

		CosmicPower() {
			super(AttackNamesies.COSMIC_POWER, "The user absorbs a mystical power from space to raise its Defense and Sp. Def stats.", 20, Type.PSYCHIC, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
		}
	}

	static class LuckyChant extends Attack {
		private static final long serialVersionUID = 1L;

		LuckyChant() {
			super(AttackNamesies.LUCKY_CHANT, "The user chants an incantation toward the sky, preventing opposing Pok\u00e9mon from landing critical hits.", 30, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.LUCKY_CHANT);
			super.selfTarget = true;
		}
	}

	static class Metronome extends Attack {
		private static final long serialVersionUID = 1L;

		Metronome() {
			super(AttackNamesies.METRONOME, "The user waggles a finger and stimulates its brain into randomly using nearly any move.", 10, Type.NORMAL, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.MIMICLESS);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			AttackNamesies[] attackNames = AttackNamesies.values();
			
			int index;
			Attack metronomeMove;
			
			do {
				index = RandomUtils.getRandomIndex(attackNames);
				metronomeMove = attackNames[index].getAttack();
			} while (metronomeMove.isMoveType(MoveType.METRONOMELESS));
			
			me.callNewMove(b, o, new Move(metronomeMove));
		}
	}

	static class Gravity extends Attack {
		private static final long serialVersionUID = 1L;

		Gravity() {
			super(AttackNamesies.GRAVITY, "Gravity is intensified for five turns, making moves involving flying unusable and negating Levitate.", 5, Type.PSYCHIC, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.GRAVITY);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}
	}

	static class Moonlight extends Attack implements SelfHealingMove {
		private static final long serialVersionUID = 1L;

		Moonlight() {
			super(AttackNamesies.MOONLIGHT, "The user restores its own HP. The amount of HP regained varies with the weather.", 5, Type.FAIRY, MoveCategory.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b) {
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			switch (b.getWeather().namesies()) {
				case CLEAR_SKIES:
					victim.healHealthFraction(1/2.0);
					break;
				case SUNNY:
					victim.healHealthFraction(2/3.0);
					break;
				case HAILING:
				case RAINING:
				case SANDSTORM:
					victim.healHealthFraction(1/4.0);
					break;
				default:
					Global.error("Funky weather problems!!!!");
					break;
			}
			
			Messages.add(new MessageUpdate(victim.getName() + "'s health was restored!").updatePokemon(b, victim));
		}
	}

	static class StoredPower extends Attack {
		private static final long serialVersionUID = 1L;

		StoredPower() {
			super(AttackNamesies.STORED_POWER, "The user attacks the target with stored power. The more the user's stats are raised, the greater the damage.", 10, Type.PSYCHIC, MoveCategory.SPECIAL);
			super.power = 20;
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*me.getAttributes().totalStatIncreases();
		}
	}

	static class PowerTrip extends Attack {
		private static final long serialVersionUID = 1L;

		PowerTrip() {
			super(AttackNamesies.POWER_TRIP, "The user boasts its strength and attacks the target. The more the user's stats are raised, the greater the move's power.", 10, Type.DARK, MoveCategory.PHYSICAL);
			super.power = 20;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*me.getAttributes().totalStatIncreases();
		}
	}

	static class Mimic extends Attack {
		private static final long serialVersionUID = 1L;

		Mimic() {
			super(AttackNamesies.MIMIC, "The user copies the target's last move. The move can be used during battle until the Pok\u00e9mon is switched out.", 10, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.MIMIC);
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
			super(AttackNamesies.METEOR_MASH, "The target is hit with a hard punch fired like a meteor. It may also raise the user's Attack.", 10, Type.STEEL, MoveCategory.PHYSICAL);
			super.power = 90;
			super.accuracy = 90;
			super.effectChance = 20;
			super.moveTypes.add(MoveType.PUNCHING);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Imprison extends Attack {
		private static final long serialVersionUID = 1L;

		Imprison() {
			super(AttackNamesies.IMPRISON, "If the opponents know any move also known by the user, the opponents are prevented from using it.", 10, Type.PSYCHIC, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.IMPRISON);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
		}
	}

	static class WillOWisp extends Attack {
		private static final long serialVersionUID = 1L;

		WillOWisp() {
			super(AttackNamesies.WILL_O_WISP, "The user shoots a sinister, bluish-white flame at the target to inflict a burn.", 15, Type.FIRE, MoveCategory.STATUS);
			super.accuracy = 85;
			super.status = StatusCondition.BURNED;
		}
	}

	static class Payback extends Attack {
		private static final long serialVersionUID = 1L;

		Payback() {
			super(AttackNamesies.PAYBACK, "If the user moves after the target, this attack's power will be doubled.", 10, Type.DARK, MoveCategory.PHYSICAL);
			super.power = 50;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(!b.isFirstAttack() ? 2 : 1);
		}
	}

	static class Extrasensory extends Attack {
		private static final long serialVersionUID = 1L;

		Extrasensory() {
			super(AttackNamesies.EXTRASENSORY, "The user attacks with an odd, unseeable power. It may also make the target flinch.", 20, Type.PSYCHIC, MoveCategory.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 10;
		}
	}

	static class FireBlast extends Attack {
		private static final long serialVersionUID = 1L;

		FireBlast() {
			super(AttackNamesies.FIRE_BLAST, "The target is attacked with an intense blast of all-consuming fire. It may also leave the target with a burn.", 5, Type.FIRE, MoveCategory.SPECIAL);
			super.power = 110;
			super.accuracy = 85;
			super.effectChance = 10;
			super.status = StatusCondition.BURNED;
		}
	}

	static class NastyPlot extends Attack {
		private static final long serialVersionUID = 1L;

		NastyPlot() {
			super(AttackNamesies.NASTY_PLOT, "The user stimulates its brain by thinking bad thoughts. It sharply raises the user's Sp. Atk.", 20, Type.DARK, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = 2;
		}
	}

	static class Round extends Attack {
		private static final long serialVersionUID = 1L;

		Round() {
			super(AttackNamesies.ROUND, "The user attacks the target with a song.", 15, Type.NORMAL, MoveCategory.SPECIAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SOUND_BASED);
		}
	}

	static class Rest extends Attack implements SelfHealingMove {
		private static final long serialVersionUID = 1L;

		Rest() {
			super(AttackNamesies.REST, "The user goes to sleep for two turns. It fully restores the user's HP and heals any status problem.", 10, Type.PSYCHIC, MoveCategory.STATUS);
			super.status = StatusCondition.ASLEEP;
			super.selfTarget = true;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (victim.fullHealth() || !Status.applies(StatusCondition.ASLEEP, b, victim, victim)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			victim.removeStatus();
			super.applyEffects(b, user, victim);
			victim.getStatus().setTurns(3);
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b) {
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			victim.healHealthFraction(1/1.0);
			
			Messages.add(new MessageUpdate(victim.getName() + "'s health was restored!").updatePokemon(b, victim));
		}
	}

	static class HyperVoice extends Attack {
		private static final long serialVersionUID = 1L;

		HyperVoice() {
			super(AttackNamesies.HYPER_VOICE, "The user lets loose a horribly echoing shout with the power to inflict damage.", 10, Type.NORMAL, MoveCategory.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SOUND_BASED);
		}
	}

	static class LeechLife extends Attack implements SapHealthEffect {
		private static final long serialVersionUID = 1L;

		LeechLife() {
			super(AttackNamesies.LEECH_LIFE, "The user drains the target's blood. The user's HP is restored by half the damage taken by the target.", 15, Type.BUG, MoveCategory.PHYSICAL);
			super.power = 20;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Astonish extends Attack {
		private static final long serialVersionUID = 1L;

		Astonish() {
			super(AttackNamesies.ASTONISH, "The user attacks the target while shouting in a startling fashion. It may also make the target flinch.", 15, Type.GHOST, MoveCategory.PHYSICAL);
			super.power = 30;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 30;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class AirCutter extends Attack implements CritStageEffect {
		private static final long serialVersionUID = 1L;

		AirCutter() {
			super(AttackNamesies.AIR_CUTTER, "The user launches razor-like wind to slash the opposing team. Critical hits land more easily.", 25, Type.FLYING, MoveCategory.SPECIAL);
			super.power = 60;
			super.accuracy = 95;
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	static class MeanLook extends Attack {
		private static final long serialVersionUID = 1L;

		MeanLook() {
			super(AttackNamesies.MEAN_LOOK, "The user pins the target with a dark, arresting look. The target becomes unable to flee.", 5, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.TRAPPED);
		}
	}

	static class Acrobatics extends Attack {
		private static final long serialVersionUID = 1L;

		Acrobatics() {
			super(AttackNamesies.ACROBATICS, "The user nimbly strikes the target. If the user is not holding an item, this attack inflicts massive damage.", 15, Type.FLYING, MoveCategory.PHYSICAL);
			super.power = 55;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(!me.isHoldingItem(b) ? 2 : 1);
		}
	}

	static class Absorb extends Attack implements SapHealthEffect {
		private static final long serialVersionUID = 1L;

		Absorb() {
			super(AttackNamesies.ABSORB, "A nutrient-draining attack. The user's HP is restored by half the damage taken by the target.", 25, Type.GRASS, MoveCategory.SPECIAL);
			super.power = 20;
			super.accuracy = 100;
		}
	}

	static class MegaDrain extends Attack implements SapHealthEffect {
		private static final long serialVersionUID = 1L;

		MegaDrain() {
			super(AttackNamesies.MEGA_DRAIN, "A nutrient-draining attack. The user's HP is restored by half the damage taken by the target.", 15, Type.GRASS, MoveCategory.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
		}
	}

	static class NaturalGift extends Attack {
		private static final long serialVersionUID = 1L;

		NaturalGift() {
			super(AttackNamesies.NATURAL_GIFT, "The user draws power to attack by using its held Berry. The Berry determines its type and power.", 15, Type.NORMAL, MoveCategory.PHYSICAL);
			super.accuracy = 100;
		}

		public Type setType(Battle b, ActivePokemon user) {
			Item i = user.getHeldItem(b);
			if (i instanceof Berry) {
				return ((Berry)i).naturalGiftType();
			}
			
			return super.type;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return ((Berry)me.getHeldItem(b)).naturalGiftPower();
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			if (!(me.getHeldItem(b) instanceof Berry)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			super.apply(me, o, b);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			// This is so fucking stupid that it consumes the Berry upon use, like srsly what the fuck is the fucking point of this move
			if (user.getHeldItem(b) instanceof Berry) {
				user.consumeItem(b);
			}
		}
	}

	static class GigaDrain extends Attack implements SapHealthEffect {
		private static final long serialVersionUID = 1L;

		GigaDrain() {
			super(AttackNamesies.GIGA_DRAIN, "A nutrient-draining attack. The user's HP is restored by half the damage taken by the target.", 10, Type.GRASS, MoveCategory.SPECIAL);
			super.power = 75;
			super.accuracy = 100;
		}
	}

	static class Aromatherapy extends Attack {
		private static final long serialVersionUID = 1L;

		Aromatherapy() {
			super(AttackNamesies.AROMATHERAPY, "The user releases a soothing scent that heals all status problems affecting the user's party.", 5, Type.GRASS, MoveCategory.STATUS);
			super.selfTarget = true;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			for (ActivePokemon p : b.getTrainer(user.isPlayer()).getTeam()) {
				if (!p.hasStatus(StatusCondition.FAINTED)) {
					p.removeStatus();
				}
			}
			
			Messages.add(new MessageUpdate("All status problems were cured!"));
		}
	}

	static class Spore extends Attack {
		private static final long serialVersionUID = 1L;

		Spore() {
			super(AttackNamesies.SPORE, "The user scatters bursts of spores that induce sleep.", 15, Type.GRASS, MoveCategory.STATUS);
			super.accuracy = 100;
			super.status = StatusCondition.ASLEEP;
			super.moveTypes.add(MoveType.POWDER);
		}
	}

	static class CrossPoison extends Attack implements CritStageEffect {
		private static final long serialVersionUID = 1L;

		CrossPoison() {
			super(AttackNamesies.CROSS_POISON, "A slashing attack with a poisonous blade that may also leave the target poisoned. Critical hits land more easily.", 20, Type.POISON, MoveCategory.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.POISONED;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	static class XScissor extends Attack {
		private static final long serialVersionUID = 1L;

		XScissor() {
			super(AttackNamesies.X_SCISSOR, "The user slashes at the target by crossing its scythes or claws as if they were a pair of scissors.", 15, Type.BUG, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Foresight extends Attack {
		private static final long serialVersionUID = 1L;

		Foresight() {
			super(AttackNamesies.FORESIGHT, "Enables a Ghost-type target to be hit by Normal and Fighting type attacks. It also enables an evasive target to be hit.", 40, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.FORESIGHT);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			super.applyEffects(b, user, victim);
			victim.getAttributes().resetStage(Stat.EVASION);
		}
	}

	static class OdorSleuth extends Attack {
		private static final long serialVersionUID = 1L;

		OdorSleuth() {
			super(AttackNamesies.ODOR_SLEUTH, "Enables a Ghost-type target to be hit with Normal- and Fighting-type attacks. It also enables an evasive target to be hit.", 40, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.FORESIGHT);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			super.applyEffects(b, user, victim);
			victim.getAttributes().resetStage(Stat.EVASION);
		}
	}

	static class MiracleEye extends Attack {
		private static final long serialVersionUID = 1L;

		MiracleEye() {
			super(AttackNamesies.MIRACLE_EYE, "Enables a Dark-type target to be hit by Psychic-type attacks. It also enables an evasive target to be hit.", 40, Type.PSYCHIC, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.MIRACLE_EYE);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			super.applyEffects(b, user, victim);
			victim.getAttributes().resetStage(Stat.EVASION);
		}
	}

	static class Howl extends Attack {
		private static final long serialVersionUID = 1L;

		Howl() {
			super(AttackNamesies.HOWL, "The user howls loudly to raise its spirit, boosting its Attack stat.", 40, Type.NORMAL, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
		}
	}

	static class SignalBeam extends Attack {
		private static final long serialVersionUID = 1L;

		SignalBeam() {
			super(AttackNamesies.SIGNAL_BEAM, "The user attacks with a sinister beam of light. It may also confuse the target.", 15, Type.BUG, MoveCategory.SPECIAL);
			super.power = 75;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.CONFUSION);
			super.effectChance = 10;
		}
	}

	static class ZenHeadbutt extends Attack {
		private static final long serialVersionUID = 1L;

		ZenHeadbutt() {
			super(AttackNamesies.ZEN_HEADBUTT, "The user focuses its willpower to its head and attacks the target. It may also make the target flinch.", 15, Type.PSYCHIC, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 90;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 20;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Psychic extends Attack {
		private static final long serialVersionUID = 1L;

		Psychic() {
			super(AttackNamesies.PSYCHIC, "The target is hit by a strong telekinetic force. It may also reduce the target's Sp. Def stat.", 10, Type.PSYCHIC, MoveCategory.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
			super.effectChance = 10;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
		}
	}

	static class MudSlap extends Attack {
		private static final long serialVersionUID = 1L;

		MudSlap() {
			super(AttackNamesies.MUD_SLAP, "The user hurls mud in the target's face to inflict damage and lower its accuracy.", 10, Type.GROUND, MoveCategory.SPECIAL);
			super.power = 20;
			super.accuracy = 100;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	static class Magnitude extends Attack implements AccuracyBypassEffect {
		private static final long serialVersionUID = 1L;
		private static final int[] CHANCES = {5, 10, 20, 30, 20, 10, 5};
		private static final int[] POWERS = {10, 30, 50, 70, 90, 110, 150};
		
		private int index;

		Magnitude() {
			super(AttackNamesies.MAGNITUDE, "The user looses a ground-shaking quake affecting everyone around the user. Its power varies.", 30, Type.GROUND, MoveCategory.PHYSICAL);
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			int power = POWERS[index = GeneralUtils.getPercentageIndex(CHANCES)];
			
			// Power is halved during Grassy Terrain
			if (b.hasEffect(EffectNamesies.GRASSY_TERRAIN)) {
				power *= .5;
			}
			
			// Power is double when the opponent is underground
			if (o.isSemiInvulnerableDigging()) {
				power *= 2;
			}
			
			return power;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			Messages.add(new MessageUpdate("Magnitude " + (index + 4) + "!"));
			super.apply(me, o, b);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			// Always hit when the opponent is underground
			return defending.isSemiInvulnerableDigging();
		}
	}

	static class Bulldoze extends Attack {
		private static final long serialVersionUID = 1L;

		Bulldoze() {
			super(AttackNamesies.BULLDOZE, "The user stomps down on the ground and attacks everything in the area. Hit Pok\u00e9mon's Speed stat is reduced.", 20, Type.GROUND, MoveCategory.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.statChanges[Stat.SPEED.index()] = -1;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			// Power is halved during Grassy Terrain
			return (int)(super.power*(b.hasEffect(EffectNamesies.GRASSY_TERRAIN) ? .5 : 1));
		}
	}

	static class Dig extends Attack implements MultiTurnMove {
		private static final long serialVersionUID = 1L;

		Dig() {
			super(AttackNamesies.DIG, "The user burrows, then attacks on the second turn. It can also be used to exit dungeons.", 10, Type.GROUND, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public boolean chargesFirst() {
			return true;
		}

		public boolean semiInvulnerability() {
			return true;
		}

		public void charge(ActivePokemon user, Battle b) {
			Messages.add(new MessageUpdate(getChargeMessage(user)));
		}

		private String getChargeMessage(ActivePokemon user) {
			return user.getName() + " went underground!";
		}
	}

	static class Earthquake extends Attack implements AccuracyBypassEffect {
		private static final long serialVersionUID = 1L;

		Earthquake() {
			super(AttackNamesies.EARTHQUAKE, "The user sets off an earthquake that strikes those around it.", 10, Type.GROUND, MoveCategory.PHYSICAL);
			super.power = 100;
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			int power = super.power;
			
			// Power is halved during Grassy Terrain
			if (b.hasEffect(EffectNamesies.GRASSY_TERRAIN)) {
				power *= .5;
			}
			
			// Power is double when the opponent is underground
			if (o.isSemiInvulnerableDigging()) {
				power *= 2;
			}
			
			return power;
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			// Always hit when the opponent is underground
			return defending.isSemiInvulnerableDigging();
		}
	}

	static class Fissure extends Attack {
		private static final long serialVersionUID = 1L;

		Fissure() {
			super(AttackNamesies.FISSURE, "The user opens up a fissure in the ground and drops the target in. The target instantly faints if it hits.", 5, Type.GROUND, MoveCategory.PHYSICAL);
			super.accuracy = 30;
			super.moveTypes.add(MoveType.ONE_HIT_KO);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			// Fails when the opponent is at a higher level than the user
			if (me.getLevel() < o.getLevel()) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			// Sturdy prevents OHKO moves if the user is not a mold breaker
			if (o.hasAbility(AbilityNamesies.STURDY) && !me.breaksTheMold()) {
				Messages.add(new MessageUpdate(o.getName() + "'s " + AbilityNamesies.STURDY.getName() + " prevents OHKO moves!"));
				return;
			}
			
			// Certain death
			o.reduceHealth(b, o.getHP());
			Messages.add(new MessageUpdate("It's a One-Hit KO!"));
		}

		public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.accuracy + (me.getLevel() - o.getLevel());
		}
	}

	static class NightSlash extends Attack implements CritStageEffect {
		private static final long serialVersionUID = 1L;

		NightSlash() {
			super(AttackNamesies.NIGHT_SLASH, "The user slashes the target the instant an opportunity arises. Critical hits land more easily.", 15, Type.DARK, MoveCategory.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	static class TriAttack extends Attack {
		private static final long serialVersionUID = 1L;
		private static StatusCondition[] statusConditions = {
			StatusCondition.PARALYZED,
			StatusCondition.BURNED,
			StatusCondition.FROZEN
		};

		TriAttack() {
			super(AttackNamesies.TRI_ATTACK, "The user strikes with a simultaneous three-beam attack. May also burn, freeze, or leave the target with paralysis.", 10, Type.NORMAL, MoveCategory.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.effectChance = 20;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			super.status = RandomUtils.getRandomValue(statusConditions);
			super.applyEffects(b, user, victim);
		}
	}

	static class FakeOut extends Attack {
		private static final long serialVersionUID = 1L;

		FakeOut() {
			super(AttackNamesies.FAKE_OUT, "An attack that hits first and makes the target flinch. It only works the first turn the user is in battle.", 10, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.FLINCH);
			super.priority = 3;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			if (!me.getAttributes().isFirstTurn()) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			super.apply(me, o, b);
		}
	}

	static class FeintAttack extends Attack {
		private static final long serialVersionUID = 1L;

		FeintAttack() {
			super(AttackNamesies.FEINT_ATTACK, "The user approaches the target disarmingly, then throws a sucker punch. It hits without fail.", 20, Type.DARK, MoveCategory.PHYSICAL);
			super.power = 60;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Taunt extends Attack {
		private static final long serialVersionUID = 1L;

		Taunt() {
			super(AttackNamesies.TAUNT, "The target is taunted into a rage that allows it to use only attack moves for three turns.", 20, Type.DARK, MoveCategory.STATUS);
			super.accuracy = 100;
			super.effects.add(EffectNamesies.TAUNT);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
		}
	}

	static class PayDay extends Attack {
		private static final long serialVersionUID = 1L;

		PayDay() {
			super(AttackNamesies.PAY_DAY, "Numerous coins are hurled at the target to inflict damage. Money is earned after the battle.", 20, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.PAY_DAY);
			super.selfTarget = true;
		}
	}

	static class PowerGem extends Attack {
		private static final long serialVersionUID = 1L;

		PowerGem() {
			super(AttackNamesies.POWER_GEM, "The user attacks with a ray of light that sparkles as if it were made of gemstones.", 20, Type.ROCK, MoveCategory.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
		}
	}

	static class WaterSport extends Attack {
		private static final long serialVersionUID = 1L;

		WaterSport() {
			super(AttackNamesies.WATER_SPORT, "The user soaks itself with water. The move weakens Fire-type moves while the user is in the battle.", 15, Type.WATER, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.WATER_SPORT);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}
	}

	static class Soak extends Attack implements ChangeTypeSource {
		private static final long serialVersionUID = 1L;

		Soak() {
			super(AttackNamesies.SOAK, "The user shoots a torrent of water at the target and changes the target's type to Water.", 20, Type.WATER, MoveCategory.STATUS);
			super.accuracy = 100;
			super.effects.add(EffectNamesies.CHANGE_TYPE);
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
			return new Type[] { Type.WATER, Type.NO_TYPE };
		}
	}

	static class TrickOrTreat extends Attack implements ChangeTypeSource {
		private static final long serialVersionUID = 1L;

		TrickOrTreat() {
			super(AttackNamesies.TRICK_OR_TREAT, "The user takes the target trick-or-treating. This adds Ghost type to the target's type.", 20, Type.GHOST, MoveCategory.STATUS);
			super.accuracy = 100;
			super.effects.add(EffectNamesies.CHANGE_TYPE);
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
			Type primary = victim.getType(b)[0];
			
			return new Type[] { primary, primary == Type.GHOST ? Type.NO_TYPE : Type.GHOST };
		}
	}

	static class ForestsCurse extends Attack implements ChangeTypeSource {
		private static final long serialVersionUID = 1L;

		ForestsCurse() {
			super(AttackNamesies.FORESTS_CURSE, "The user puts a forest curse on the target. Afflicted targets are now Grass type as well.", 20, Type.GRASS, MoveCategory.STATUS);
			super.accuracy = 100;
			super.effects.add(EffectNamesies.CHANGE_TYPE);
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
			Type primary = victim.getType(b)[0];
			
			return new Type[] { primary, primary == Type.GRASS ? Type.NO_TYPE : Type.GRASS };
		}
	}

	static class PsychUp extends Attack {
		private static final long serialVersionUID = 1L;

		PsychUp() {
			super(AttackNamesies.PSYCH_UP, "The user hypnotizes itself into copying any stat change made by the target.", 10, Type.NORMAL, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			for (int i = 0; i < Stat.NUM_BATTLE_STATS; i++)  {
				user.getAttributes().setStage(i, victim.getStage(i));
			}
			
			Messages.add(new MessageUpdate(user.getName() + " copied " + victim.getName() + "'s stat changes!"));
		}
	}

	static class Amnesia extends Attack {
		private static final long serialVersionUID = 1L;

		Amnesia() {
			super(AttackNamesies.AMNESIA, "The user temporarily empties its mind to forget its concerns. It sharply raises the user's Sp. Def stat.", 20, Type.PSYCHIC, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.SP_DEFENSE.index()] = 2;
		}
	}

	static class WonderRoom extends Attack {
		private static final long serialVersionUID = 1L;

		WonderRoom() {
			super(AttackNamesies.WONDER_ROOM, "The user creates a bizarre area in which Pok\u00e9mon's Defense and Sp. Def stats are swapped for five turns.", 10, Type.PSYCHIC, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.WONDER_ROOM);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}
	}

	static class AquaJet extends Attack {
		private static final long serialVersionUID = 1L;

		AquaJet() {
			super(AttackNamesies.AQUA_JET, "The user lunges at the target at a speed that makes it almost invisible. It is sure to strike first.", 20, Type.WATER, MoveCategory.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.priority = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Covet extends Attack implements ItemHolder {
		private static final long serialVersionUID = 1L;
		private Item item;

		Covet() {
			super(AttackNamesies.COVET, "The user endearingly approaches the target, then steals the target's held item.", 25, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.CHANGE_ITEM);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		private String getSwitchMessage(ActivePokemon user, Item userItem, ActivePokemon victim, Item victimItem) {
			return user.getName() + " stole " + victim.getName() + "'s " + victimItem.getName() + "!";
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isHoldingItem(b) || !victim.isHoldingItem(b) || b.getTrainer(user.isPlayer()) instanceof WildPokemon || victim.hasAbility(AbilityNamesies.STICKY_HOLD)) {
				if (super.category == MoveCategory.STATUS) {
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				}
				
				return;
			}
			
			Item userItem = user.getHeldItem(b), victimItem = victim.getHeldItem(b);
			Messages.add(new MessageUpdate(getSwitchMessage(user, userItem, victim, victimItem)));
			
			if (b.isWildBattle()) {
				user.giveItem((HoldItem)victimItem);
				victim.giveItem((HoldItem)userItem);
				return;
			}
			
			item = userItem;
			super.applyEffects(b, user, victim);
			
			item = victimItem;
			super.applyEffects(b, user, user);
		}

		public Item getItem() {
			return item;
		}
	}

	static class LowKick extends Attack {
		private static final long serialVersionUID = 1L;

		LowKick() {
			super(AttackNamesies.LOW_KICK, "A powerful low kick that makes the target fall over. It inflicts greater damage on heavier targets.", 20, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			double weight = o.getWeight(b);
			if (weight < 22) return 20;
			if (weight < 55) return 40;
			if (weight < 110) return 60;
			if (weight < 220) return 80;
			if (weight < 440) return 100;
			return 120;
		}
	}

	static class KarateChop extends Attack implements CritStageEffect {
		private static final long serialVersionUID = 1L;

		KarateChop() {
			super(AttackNamesies.KARATE_CHOP, "The target is attacked with a sharp chop. Critical hits land more easily.", 25, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 50;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	static class SeismicToss extends Attack {
		private static final long serialVersionUID = 1L;

		SeismicToss() {
			super(AttackNamesies.SEISMIC_TOSS, "The target is thrown using the power of gravity. It inflicts damage equal to the user's level.", 20, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			o.reduceHealth(b, me.getLevel());
		}
	}

	static class Swagger extends Attack {
		private static final long serialVersionUID = 1L;

		Swagger() {
			super(AttackNamesies.SWAGGER, "The user enrages and confuses the target. However, it also sharply raises the target's Attack stat.", 15, Type.NORMAL, MoveCategory.STATUS);
			super.accuracy = 90;
			super.effects.add(EffectNamesies.CONFUSION);
			super.statChanges[Stat.ATTACK.index()] = 2;
		}
	}

	static class CrossChop extends Attack implements CritStageEffect {
		private static final long serialVersionUID = 1L;

		CrossChop() {
			super(AttackNamesies.CROSS_CHOP, "The user delivers a double chop with its forearms crossed. Critical hits land more easily.", 5, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 100;
			super.accuracy = 80;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	static class Punishment extends Attack {
		private static final long serialVersionUID = 1L;

		Punishment() {
			super(AttackNamesies.PUNISHMENT, "This attack's power increases the more the target has powered up with stat changes.", 5, Type.DARK, MoveCategory.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return (int)Math.min(super.power + 20*o.getAttributes().totalStatIncreases(), 200);
		}
	}

	static class CloseCombat extends Attack {
		private static final long serialVersionUID = 1L;

		CloseCombat() {
			super(AttackNamesies.CLOSE_COMBAT, "The user fights the target up close without guarding itself. It also cuts the user's Defense and Sp. Def.", 5, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class DragonAscent extends Attack {
		private static final long serialVersionUID = 1L;

		DragonAscent() {
			super(AttackNamesies.DRAGON_ASCENT, "After soaring upward, the user attacks its target by dropping out of the sky at high speeds, although it lowers its own Defense and Sp. Def in the process.", 5, Type.FLYING, MoveCategory.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class FlameWheel extends Attack {
		private static final long serialVersionUID = 1L;

		FlameWheel() {
			super(AttackNamesies.FLAME_WHEEL, "The user cloaks itself in fire and charges at the target. It may also leave the target with a burn.", 25, Type.FIRE, MoveCategory.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.BURNED;
			super.moveTypes.add(MoveType.DEFROST);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Reversal extends Attack {
		private static final long serialVersionUID = 1L;

		Reversal() {
			super(AttackNamesies.REVERSAL, "An all-out attack that becomes more powerful the less HP the user has.", 15, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			double ratio = me.getHPRatio();
			if (ratio > .7) return 20;
			if (ratio > .35) return 40;
			if (ratio > .2) return 80;
			if (ratio > .1) return 100;
			if (ratio > .04) return 150;
			return 200;
		}
	}

	static class ExtremeSpeed extends Attack {
		private static final long serialVersionUID = 1L;

		ExtremeSpeed() {
			super(AttackNamesies.EXTREME_SPEED, "The user charges the target at blinding speed. This attack always goes before any other move.", 5, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.priority = 2;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Hypnosis extends Attack {
		private static final long serialVersionUID = 1L;

		Hypnosis() {
			super(AttackNamesies.HYPNOSIS, "The user employs hypnotic suggestion to make the target fall into a deep sleep.", 20, Type.PSYCHIC, MoveCategory.STATUS);
			super.accuracy = 60;
			super.status = StatusCondition.ASLEEP;
		}
	}

	static class BubbleBeam extends Attack {
		private static final long serialVersionUID = 1L;

		BubbleBeam() {
			super(AttackNamesies.BUBBLE_BEAM, "A spray of bubbles is forcefully ejected at the opposing team. It may also lower their Speed stats.", 20, Type.WATER, MoveCategory.SPECIAL);
			super.power = 65;
			super.accuracy = 100;
			super.effectChance = 10;
			super.statChanges[Stat.SPEED.index()] = -1;
		}
	}

	static class MudShot extends Attack {
		private static final long serialVersionUID = 1L;

		MudShot() {
			super(AttackNamesies.MUD_SHOT, "The user attacks by hurling a blob of mud at the target. It also reduces the target's Speed.", 15, Type.GROUND, MoveCategory.SPECIAL);
			super.power = 55;
			super.accuracy = 95;
			super.statChanges[Stat.SPEED.index()] = -1;
		}
	}

	static class BellyDrum extends Attack {
		private static final long serialVersionUID = 1L;

		BellyDrum() {
			super(AttackNamesies.BELLY_DRUM, "The user maximizes its Attack stat in exchange for HP equal to half its max HP.", 10, Type.NORMAL, MoveCategory.STATUS);
			super.selfTarget = true;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			// Fails if attack is already maxed or if you have less than half your health to give up
			if (user.getStage(Stat.ATTACK.index()) == Stat.MAX_STAT_CHANGES || user.getHPRatio() < .5) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			// Maximization station
			Messages.add(new MessageUpdate(user.getName() + " cut its own HP and maximized its attack!"));
			user.reduceHealthFraction(b, 1/2.0);
			user.getAttributes().setStage(Stat.ATTACK.index(), Stat.MAX_STAT_CHANGES);
		}
	}

	static class Submission extends Attack implements RecoilMove {
		private static final long serialVersionUID = 1L;

		Submission() {
			super(AttackNamesies.SUBMISSION, "The user grabs the target and recklessly dives for the ground. It also hurts the user slightly.", 25, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 80;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyRecoil(Battle b, ActivePokemon user, int damage) {
			if (user.hasAbility(AbilityNamesies.ROCK_HEAD) || user.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(new MessageUpdate(user.getName() + " was hurt by recoil!"));
			user.reduceHealth(b, (int)Math.ceil(damage/4.0), false);
		}
	}

	static class DynamicPunch extends Attack {
		private static final long serialVersionUID = 1L;

		DynamicPunch() {
			super(AttackNamesies.DYNAMIC_PUNCH, "The user punches the target with full, concentrated power. It confuses the target if it hits.", 5, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 100;
			super.accuracy = 50;
			super.effects.add(EffectNamesies.CONFUSION);
			super.moveTypes.add(MoveType.PUNCHING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class MindReader extends Attack {
		private static final long serialVersionUID = 1L;

		MindReader() {
			super(AttackNamesies.MIND_READER, "The user senses the target's movements with its mind to ensure its next attack does not miss the target.", 5, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.LOCK_ON);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}
	}

	static class LockOn extends Attack {
		private static final long serialVersionUID = 1L;

		LockOn() {
			super(AttackNamesies.LOCK_ON, "The user takes sure aim at the target. It ensures the next attack does not fail to hit the target.", 5, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.LOCK_ON);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}
	}

	static class Kinesis extends Attack {
		private static final long serialVersionUID = 1L;

		Kinesis() {
			super(AttackNamesies.KINESIS, "The user distracts the target by bending a spoon. It lowers the target's accuracy.", 15, Type.PSYCHIC, MoveCategory.STATUS);
			super.accuracy = 80;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	static class Barrier extends Attack {
		private static final long serialVersionUID = 1L;

		Barrier() {
			super(AttackNamesies.BARRIER, "The user throws up a sturdy wall that sharply raises its Defense stat.", 20, Type.PSYCHIC, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 2;
		}
	}

	static class Telekinesis extends Attack {
		private static final long serialVersionUID = 1L;

		Telekinesis() {
			super(AttackNamesies.TELEKINESIS, "The user makes the target float with its psychic power. The target is easier to hit for three turns.", 15, Type.PSYCHIC, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.TELEKINESIS);
			super.moveTypes.add(MoveType.AIRBORNE);
		}
	}

	static class Ingrain extends Attack {
		private static final long serialVersionUID = 1L;

		Ingrain() {
			super(AttackNamesies.INGRAIN, "The user lays roots that restore its HP on every turn. Because it is rooted, it can't switch out.", 20, Type.GRASS, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.INGRAIN);
			super.selfTarget = true;
		}
	}

	static class PsychoCut extends Attack implements CritStageEffect {
		private static final long serialVersionUID = 1L;

		PsychoCut() {
			super(AttackNamesies.PSYCHO_CUT, "The user tears at the target with blades formed by psychic power. Critical hits land more easily.", 20, Type.PSYCHIC, MoveCategory.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	static class FutureSight extends Attack {
		private static final long serialVersionUID = 1L;

		FutureSight() {
			super(AttackNamesies.FUTURE_SIGHT, "Two turns after this move is used, a hunk of psychic energy attacks the target.", 10, Type.PSYCHIC, MoveCategory.SPECIAL);
			super.power = 120;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.FUTURE_SIGHT);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			super.applyEffects(b, me, o); // Don't apply damage just yet!!
		}

		public boolean canPrintFail() {
			return true;
		}
	}

	static class DoomDesire extends Attack {
		private static final long serialVersionUID = 1L;

		DoomDesire() {
			super(AttackNamesies.DOOM_DESIRE, "Two turns after this move is used, the user blasts the target with a concentrated bundle of light.", 5, Type.STEEL, MoveCategory.SPECIAL);
			super.power = 140;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.DOOM_DESIRE);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			super.applyEffects(b, me, o); // Don't apply damage just yet!!
		}

		public boolean canPrintFail() {
			return true;
		}
	}

	static class CalmMind extends Attack {
		private static final long serialVersionUID = 1L;

		CalmMind() {
			super(AttackNamesies.CALM_MIND, "The user quietly focuses its mind and calms its spirit to raise its Sp. Atk and Sp. Def stats.", 20, Type.PSYCHIC, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
		}
	}

	static class LowSweep extends Attack {
		private static final long serialVersionUID = 1L;

		LowSweep() {
			super(AttackNamesies.LOW_SWEEP, "The user attacks the target's legs swiftly, reducing the target's Speed stat.", 20, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 65;
			super.accuracy = 100;
			super.statChanges[Stat.SPEED.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Revenge extends Attack {
		private static final long serialVersionUID = 1L;

		Revenge() {
			super(AttackNamesies.REVENGE, "An attack move that inflicts double the damage if the user has been hurt by the opponent in the same turn.", 10, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.priority = -4;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(me.getAttributes().hasTakenDamage() ? 2 : 1);
		}
	}

	static class VitalThrow extends Attack {
		private static final long serialVersionUID = 1L;

		VitalThrow() {
			super(AttackNamesies.VITAL_THROW, "The user attacks last. In return, this throw move is guaranteed not to miss.", 10, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 70;
			super.priority = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class WringOut extends Attack {
		private static final long serialVersionUID = 1L;

		WringOut() {
			super(AttackNamesies.WRING_OUT, "The user powerfully wrings the target. The more HP the target has, the greater this attack's power.", 5, Type.NORMAL, MoveCategory.SPECIAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return (int)Math.min(1, (120*o.getHPRatio()));
		}
	}

	static class LeafTornado extends Attack {
		private static final long serialVersionUID = 1L;

		LeafTornado() {
			super(AttackNamesies.LEAF_TORNADO, "The user attacks its target by encircling it in sharp leaves. This attack may also lower the target's accuracy.", 10, Type.GRASS, MoveCategory.SPECIAL);
			super.power = 65;
			super.accuracy = 90;
			super.effectChance = 30;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	static class LeafStorm extends Attack {
		private static final long serialVersionUID = 1L;

		LeafStorm() {
			super(AttackNamesies.LEAF_STORM, "The user whips up a storm of leaves around the target. The attack's recoil harshly reduces the user's Sp. Atk stat.", 5, Type.GRASS, MoveCategory.SPECIAL);
			super.power = 130;
			super.accuracy = 90;
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = -2;
		}
	}

	static class LeafBlade extends Attack implements CritStageEffect {
		private static final long serialVersionUID = 1L;

		LeafBlade() {
			super(AttackNamesies.LEAF_BLADE, "The user handles a sharp leaf like a sword and attacks by cutting its target. Critical hits land more easily.", 15, Type.GRASS, MoveCategory.PHYSICAL);
			super.power = 90;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	static class Constrict extends Attack {
		private static final long serialVersionUID = 1L;

		Constrict() {
			super(AttackNamesies.CONSTRICT, "The target is attacked with long, creeping tentacles or vines. It may also lower the target's Speed stat.", 35, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 10;
			super.accuracy = 100;
			super.effectChance = 10;
			super.statChanges[Stat.SPEED.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Hex extends Attack {
		private static final long serialVersionUID = 1L;

		Hex() {
			super(AttackNamesies.HEX, "This relentless attack does massive damage to a target affected by status problems.", 10, Type.GHOST, MoveCategory.SPECIAL);
			super.power = 65;
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(o.hasStatus() ? 2 : 1);
		}
	}

	static class SludgeWave extends Attack {
		private static final long serialVersionUID = 1L;

		SludgeWave() {
			super(AttackNamesies.SLUDGE_WAVE, "It swamps the area around the user with a giant sludge wave. It may also poison those hit.", 10, Type.POISON, MoveCategory.SPECIAL);
			super.power = 95;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.POISONED;
		}
	}

	static class MudSport extends Attack {
		private static final long serialVersionUID = 1L;

		MudSport() {
			super(AttackNamesies.MUD_SPORT, "The user covers itself with mud. It weakens Electric-type moves while the user is in the battle.", 15, Type.GROUND, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.MUD_SPORT);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}
	}

	static class RockPolish extends Attack {
		private static final long serialVersionUID = 1L;

		RockPolish() {
			super(AttackNamesies.ROCK_POLISH, "The user polishes its body to reduce drag. It can sharply raise the Speed stat.", 20, Type.ROCK, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.SPEED.index()] = 2;
		}
	}

	static class RockThrow extends Attack {
		private static final long serialVersionUID = 1L;

		RockThrow() {
			super(AttackNamesies.ROCK_THROW, "The user picks up and throws a small rock at the target to attack.", 15, Type.ROCK, MoveCategory.PHYSICAL);
			super.power = 50;
			super.accuracy = 90;
		}
	}

	static class RockBlast extends Attack implements MultiStrikeMove {
		private static final long serialVersionUID = 1L;

		RockBlast() {
			super(AttackNamesies.ROCK_BLAST, "The user hurls hard rocks at the target. Two to five rocks are launched in quick succession.", 10, Type.ROCK, MoveCategory.PHYSICAL);
			super.power = 25;
			super.accuracy = 90;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int hits = this.getNumHits(me);
			
			int hit = 1;
			for (; hit <= hits; hit++) {
				Messages.add(new MessageUpdate("Hit " + hit + "!"));
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b)) {
					break;
				}
			}
			
			hit--;
			
			// Print hits and gtfo
			Messages.add(new MessageUpdate("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!"));
		}

		public int getMinHits() {
			return 2;
		}

		public int getMaxHits() {
			return 5;
		}
	}

	static class SmackDown extends Attack implements AccuracyBypassEffect {
		private static final long serialVersionUID = 1L;

		SmackDown() {
			super(AttackNamesies.SMACK_DOWN, "The user throws a stone or projectile to attack an opponent. A flying Pok\u00e9mon will fall to the ground when hit.", 15, Type.ROCK, MoveCategory.PHYSICAL);
			super.power = 50;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.GROUNDED);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			// Twice as strong when the opponent is flying
			return super.power*(o.isSemiInvulnerableFlying() ? 2 : 1);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			// Always hit when the opponent is flying
			return defending.isSemiInvulnerableFlying();
		}
	}

	static class StealthRock extends Attack {
		private static final long serialVersionUID = 1L;

		StealthRock() {
			super(AttackNamesies.STEALTH_ROCK, "The user lays a trap of levitating stones around the opponent's team. The trap hurts opponents that switch into battle.", 20, Type.ROCK, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.STEALTH_ROCK);
			super.moveTypes.add(MoveType.FIELD);
		}
	}

	static class StoneEdge extends Attack implements CritStageEffect {
		private static final long serialVersionUID = 1L;

		StoneEdge() {
			super(AttackNamesies.STONE_EDGE, "The user stabs the foe with sharpened stones from below. It has a high critical-hit ratio.", 5, Type.ROCK, MoveCategory.PHYSICAL);
			super.power = 100;
			super.accuracy = 80;
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	static class Steamroller extends Attack implements AccuracyBypassEffect {
		private static final long serialVersionUID = 1L;

		Steamroller() {
			super(AttackNamesies.STEAMROLLER, "The user crushes its targets by rolling over them with its rolled-up body. This attack may make the target flinch.", 20, Type.BUG, MoveCategory.PHYSICAL);
			super.power = 65;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 30;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(o.hasEffect(EffectNamesies.USED_MINIMIZE) ? 2 : 1);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			return !defending.isSemiInvulnerable() && defending.hasEffect(EffectNamesies.USED_MINIMIZE);
		}
	}

	static class HeavySlam extends Attack {
		private static final long serialVersionUID = 1L;

		HeavySlam() {
			super(AttackNamesies.HEAVY_SLAM, "The user slams into the target with its heavy body. The more the user outweighs the target, the greater its damage.", 10, Type.STEEL, MoveCategory.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			double ratio = o.getWeight(b)/me.getWeight(b);
			if (ratio > .5) return 40;
			if (ratio > .33) return 60;
			if (ratio > .25) return 80;
			if (ratio > .2) return 100;
			return 120;
		}
	}

	static class Stomp extends Attack implements AccuracyBypassEffect {
		private static final long serialVersionUID = 1L;

		Stomp() {
			super(AttackNamesies.STOMP, "The target is stomped with a big foot. It may also make the target flinch.", 20, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 65;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 30;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(o.hasEffect(EffectNamesies.USED_MINIMIZE) ? 2 : 1);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			return !defending.isSemiInvulnerable() && defending.hasEffect(EffectNamesies.USED_MINIMIZE);
		}
	}

	static class FlameCharge extends Attack {
		private static final long serialVersionUID = 1L;

		FlameCharge() {
			super(AttackNamesies.FLAME_CHARGE, "The user cloaks itself with flame and attacks. Building up more power, it raises the user's Speed stat.", 20, Type.FIRE, MoveCategory.PHYSICAL);
			super.power = 50;
			super.accuracy = 100;
			super.selfTarget = true;
			super.statChanges[Stat.SPEED.index()] = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Bounce extends Attack implements MultiTurnMove {
		private static final long serialVersionUID = 1L;

		Bounce() {
			super(AttackNamesies.BOUNCE, "The user bounces up high, then drops on the target on the second turn. It may also leave the target with paralysis.", 5, Type.FLYING, MoveCategory.PHYSICAL);
			super.power = 85;
			super.accuracy = 85;
			super.effectChance = 30;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.AIRBORNE);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public boolean chargesFirst() {
			return true;
		}

		public boolean semiInvulnerability() {
			return true;
		}

		public void charge(ActivePokemon user, Battle b) {
			Messages.add(new MessageUpdate(getChargeMessage(user)));
		}

		private String getChargeMessage(ActivePokemon user) {
			return user.getName() + " sprang up!";
		}
	}

	static class Curse extends Attack {
		private static final long serialVersionUID = 1L;

		Curse() {
			super(AttackNamesies.CURSE, "A move that works differently for the Ghost type than for all other types.", 10, Type.GHOST, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.statChanges[Stat.SPEED.index()] = -1;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			// Different effects based on the type of the user
			if (me.isType(b, Type.GHOST)) {
				Effect curse = EffectNamesies.CURSE.getEffect();
				
				// Manually apply the effect if it applies
				if (curse.applies(b, me, o, CastSource.ATTACK)) {
					curse.cast(b, me, o, CastSource.ATTACK, super.printCast);
				}
				else {
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				}
			}
			else {
				super.apply(me, o, b);
			}
		}
	}

	static class Yawn extends Attack {
		private static final long serialVersionUID = 1L;

		Yawn() {
			super(AttackNamesies.YAWN, "The user lets loose a huge yawn that lulls the target into falling asleep on the next turn.", 10, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.YAWN);
		}
	}

	static class Headbutt extends Attack {
		private static final long serialVersionUID = 1L;

		Headbutt() {
			super(AttackNamesies.HEADBUTT, "The user sticks out its head and attacks by charging straight into the target. It may also make the target flinch.", 15, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 30;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class SlackOff extends Attack implements SelfHealingMove {
		private static final long serialVersionUID = 1L;

		SlackOff() {
			super(AttackNamesies.SLACK_OFF, "The user slacks off, restoring its own HP by up to half of its maximum HP.", 10, Type.NORMAL, MoveCategory.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b) {
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			victim.healHealthFraction(1/2.0);
			
			Messages.add(new MessageUpdate(victim.getName() + "'s health was restored!").updatePokemon(b, victim));
		}
	}

	static class HealPulse extends Attack {
		private static final long serialVersionUID = 1L;

		HealPulse() {
			super(AttackNamesies.HEAL_PULSE, "The user emits a healing pulse which restores the target's HP by up to half of its max HP.", 10, Type.PSYCHIC, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.AURA_PULSE);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			// Heal by 50% unless the user has Mega Launcher -- then heal by 75%
			double fraction = user.hasAbility(AbilityNamesies.MEGA_LAUNCHER) ? .75 : .5;
			
			victim.healHealthFraction(fraction);
			Messages.add(new MessageUpdate(victim.getName() + "'s health was restored!").updatePokemon(b, victim));
		}
	}

	static class MetalSound extends Attack {
		private static final long serialVersionUID = 1L;

		MetalSound() {
			super(AttackNamesies.METAL_SOUND, "A horrible sound like scraping metal harshly reduces the target's Sp. Def stat.", 40, Type.STEEL, MoveCategory.STATUS);
			super.accuracy = 85;
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.statChanges[Stat.SP_DEFENSE.index()] = -2;
		}
	}

	static class Spark extends Attack {
		private static final long serialVersionUID = 1L;

		Spark() {
			super(AttackNamesies.SPARK, "The user throws an electrically charged tackle at the target. It may also leave the target with paralysis.", 20, Type.ELECTRIC, MoveCategory.PHYSICAL);
			super.power = 65;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class MagnetBomb extends Attack {
		private static final long serialVersionUID = 1L;

		MagnetBomb() {
			super(AttackNamesies.MAGNET_BOMB, "The user launches steel bombs that stick to the target. This attack will not miss.", 20, Type.STEEL, MoveCategory.PHYSICAL);
			super.power = 60;
			super.moveTypes.add(MoveType.BOMB_BALL);
		}
	}

	static class MirrorShot extends Attack {
		private static final long serialVersionUID = 1L;

		MirrorShot() {
			super(AttackNamesies.MIRROR_SHOT, "The user looses a flash of energy at the target from its polished body. It may also lower the target's accuracy.", 10, Type.STEEL, MoveCategory.SPECIAL);
			super.power = 65;
			super.accuracy = 85;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	static class MagnetRise extends Attack {
		private static final long serialVersionUID = 1L;

		MagnetRise() {
			super(AttackNamesies.MAGNET_RISE, "The user levitates using electrically generated magnetism for five turns.", 10, Type.ELECTRIC, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.MAGNET_RISE);
			super.moveTypes.add(MoveType.AIRBORNE);
			super.selfTarget = true;
		}
	}

	static class ZapCannon extends Attack {
		private static final long serialVersionUID = 1L;

		ZapCannon() {
			super(AttackNamesies.ZAP_CANNON, "The user fires an electric blast like a cannon to inflict damage and cause paralysis.", 5, Type.ELECTRIC, MoveCategory.SPECIAL);
			super.power = 120;
			super.accuracy = 50;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.BOMB_BALL);
		}
	}

	static class BraveBird extends Attack implements RecoilMove {
		private static final long serialVersionUID = 1L;

		BraveBird() {
			super(AttackNamesies.BRAVE_BIRD, "The user tucks in its wings and charges from a low altitude. The user also takes serious damage.", 15, Type.FLYING, MoveCategory.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyRecoil(Battle b, ActivePokemon user, int damage) {
			if (user.hasAbility(AbilityNamesies.ROCK_HEAD) || user.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(new MessageUpdate(user.getName() + " was hurt by recoil!"));
			user.reduceHealth(b, (int)Math.ceil(damage/3.0), false);
		}
	}

	static class Uproar extends Attack {
		private static final long serialVersionUID = 1L;

		Uproar() {
			super(AttackNamesies.UPROAR, "The user attacks in an uproar for three turns. Over that time, no one can fall asleep.", 10, Type.NORMAL, MoveCategory.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.UPROAR);
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.selfTarget = true;
		}
	}

	static class Acupressure extends Attack {
		private static final long serialVersionUID = 1L;

		Acupressure() {
			super(AttackNamesies.ACUPRESSURE, "The user applies pressure to stress points, sharply boosting one of its stats.", 30, Type.NORMAL, MoveCategory.STATUS);
			super.selfTarget = true;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			// TODO: Test to make sure this works when both parties use during the same turn -- will it always be the same stat?
			super.statChanges = new int[Stat.NUM_BATTLE_STATS];
			super.statChanges[RandomUtils.getRandomInt(super.statChanges.length)] = 2;
			
			super.applyEffects(b, user, victim);
		}
	}

	static class DoubleHit extends Attack implements MultiStrikeMove {
		private static final long serialVersionUID = 1L;

		DoubleHit() {
			super(AttackNamesies.DOUBLE_HIT, "The user slams the target with a long tail, vines, or tentacle. The target is hit twice in a row.", 10, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 35;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int hits = this.getNumHits(me);
			
			int hit = 1;
			for (; hit <= hits; hit++) {
				Messages.add(new MessageUpdate("Hit " + hit + "!"));
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b)) {
					break;
				}
			}
			
			hit--;
			
			// Print hits and gtfo
			Messages.add(new MessageUpdate("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!"));
		}

		public int getMinHits() {
			return 2;
		}

		public int getMaxHits() {
			return 2;
		}
	}

	static class IcyWind extends Attack {
		private static final long serialVersionUID = 1L;

		IcyWind() {
			super(AttackNamesies.ICY_WIND, "The user attacks with a gust of chilled air. It also reduces the targets' Speed stat.", 15, Type.ICE, MoveCategory.SPECIAL);
			super.power = 55;
			super.accuracy = 95;
			super.statChanges[Stat.SPEED.index()] = -1;
		}
	}

	static class IceShard extends Attack {
		private static final long serialVersionUID = 1L;

		IceShard() {
			super(AttackNamesies.ICE_SHARD, "The user flash freezes chunks of ice and hurls them at the target. This move always goes first.", 30, Type.ICE, MoveCategory.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.priority = 1;
		}
	}

	static class AquaRing extends Attack {
		private static final long serialVersionUID = 1L;

		AquaRing() {
			super(AttackNamesies.AQUA_RING, "The user envelops itself in a veil made of water. It regains some HP on every turn.", 20, Type.WATER, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.AQUA_RING);
			super.selfTarget = true;
		}
	}

	static class AuroraBeam extends Attack {
		private static final long serialVersionUID = 1L;

		AuroraBeam() {
			super(AttackNamesies.AURORA_BEAM, "The target is hit with a rainbow-colored beam. This may also lower the target's Attack stat.", 20, Type.ICE, MoveCategory.SPECIAL);
			super.power = 65;
			super.accuracy = 100;
			super.effectChance = 10;
			super.statChanges[Stat.ATTACK.index()] = -1;
		}
	}

	static class Brine extends Attack {
		private static final long serialVersionUID = 1L;

		Brine() {
			super(AttackNamesies.BRINE, "If the target's HP is down to about half, this attack will hit with double the power.", 10, Type.WATER, MoveCategory.SPECIAL);
			super.power = 65;
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(me.getHPRatio() < .5 ? 2 : 1);
		}
	}

	static class Dive extends Attack implements MultiTurnMove {
		private static final long serialVersionUID = 1L;

		Dive() {
			super(AttackNamesies.DIVE, "Diving on the first turn, the user floats up and attacks on the second turn. It can be used to dive deep in the ocean.", 10, Type.WATER, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public boolean chargesFirst() {
			return true;
		}

		public boolean semiInvulnerability() {
			return true;
		}

		public void charge(ActivePokemon user, Battle b) {
			Messages.add(new MessageUpdate(getChargeMessage(user)));
		}

		private String getChargeMessage(ActivePokemon user) {
			return user.getName() + " hid underwater!";
		}
	}

	static class IceBeam extends Attack {
		private static final long serialVersionUID = 1L;

		IceBeam() {
			super(AttackNamesies.ICE_BEAM, "The target is struck with an icy-cold beam of energy. It may also freeze the target solid.", 10, Type.ICE, MoveCategory.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.FROZEN;
		}
	}

	static class SheerCold extends Attack {
		private static final long serialVersionUID = 1L;

		SheerCold() {
			super(AttackNamesies.SHEER_COLD, "The target is attacked with a blast of absolute-zero cold. The target instantly faints if it hits.", 5, Type.ICE, MoveCategory.SPECIAL);
			super.accuracy = 30;
			super.moveTypes.add(MoveType.ONE_HIT_KO);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			// Fails when the opponent is at a higher level than the user
			if (me.getLevel() < o.getLevel()) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			// Sturdy prevents OHKO moves if the user is not a mold breaker
			if (o.hasAbility(AbilityNamesies.STURDY) && !me.breaksTheMold()) {
				Messages.add(new MessageUpdate(o.getName() + "'s " + AbilityNamesies.STURDY.getName() + " prevents OHKO moves!"));
				return;
			}
			
			// Certain death
			o.reduceHealth(b, o.getHP());
			Messages.add(new MessageUpdate("It's a One-Hit KO!"));
		}

		public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.accuracy + (me.getLevel() - o.getLevel());
		}
	}

	static class PoisonGas extends Attack {
		private static final long serialVersionUID = 1L;

		PoisonGas() {
			super(AttackNamesies.POISON_GAS, "A cloud of poison gas is sprayed in the face of opposing Pok\u00e9mon. It may poison those hit.", 40, Type.POISON, MoveCategory.STATUS);
			super.accuracy = 90;
			super.status = StatusCondition.POISONED;
		}
	}

	static class Sludge extends Attack {
		private static final long serialVersionUID = 1L;

		Sludge() {
			super(AttackNamesies.SLUDGE, "Unsanitary sludge is hurled at the target. It may also poison the target.", 20, Type.POISON, MoveCategory.SPECIAL);
			super.power = 65;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.POISONED;
		}
	}

	static class SludgeBomb extends Attack {
		private static final long serialVersionUID = 1L;

		SludgeBomb() {
			super(AttackNamesies.SLUDGE_BOMB, "Unsanitary sludge is hurled at the target. It may also poison the target.", 10, Type.POISON, MoveCategory.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.POISONED;
			super.moveTypes.add(MoveType.BOMB_BALL);
		}
	}

	static class AcidArmor extends Attack {
		private static final long serialVersionUID = 1L;

		AcidArmor() {
			super(AttackNamesies.ACID_ARMOR, "The user alters its cellular structure to liquefy itself, sharply raising its Defense stat.", 20, Type.POISON, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 2;
		}
	}

	static class IcicleSpear extends Attack implements MultiStrikeMove {
		private static final long serialVersionUID = 1L;

		IcicleSpear() {
			super(AttackNamesies.ICICLE_SPEAR, "The user launches sharp icicles at the target. It strikes two to five times in a row.", 30, Type.ICE, MoveCategory.PHYSICAL);
			super.power = 25;
			super.accuracy = 100;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int hits = this.getNumHits(me);
			
			int hit = 1;
			for (; hit <= hits; hit++) {
				Messages.add(new MessageUpdate("Hit " + hit + "!"));
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b)) {
					break;
				}
			}
			
			hit--;
			
			// Print hits and gtfo
			Messages.add(new MessageUpdate("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!"));
		}

		public int getMinHits() {
			return 2;
		}

		public int getMaxHits() {
			return 5;
		}
	}

	static class Clamp extends Attack {
		private static final long serialVersionUID = 1L;

		Clamp() {
			super(AttackNamesies.CLAMP, "The target is clamped and squeezed by the user's very thick and sturdy shell for four to five turns.", 15, Type.WATER, MoveCategory.PHYSICAL);
			super.power = 35;
			super.accuracy = 85;
			super.effects.add(EffectNamesies.CLAMPED);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class RazorShell extends Attack {
		private static final long serialVersionUID = 1L;

		RazorShell() {
			super(AttackNamesies.RAZOR_SHELL, "The user cuts its target with sharp shells. This attack may also lower the target's Defense stat.", 10, Type.WATER, MoveCategory.PHYSICAL);
			super.power = 75;
			super.accuracy = 95;
			super.effectChance = 50;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Whirlpool extends Attack {
		private static final long serialVersionUID = 1L;

		Whirlpool() {
			super(AttackNamesies.WHIRLPOOL, "Traps foes in a violent swirling whirlpool for four to five turns.", 15, Type.WATER, MoveCategory.SPECIAL);
			super.power = 35;
			super.accuracy = 85;
			super.effects.add(EffectNamesies.WHIRLPOOLED);
		}
	}

	static class ShellSmash extends Attack {
		private static final long serialVersionUID = 1L;

		ShellSmash() {
			super(AttackNamesies.SHELL_SMASH, "The user breaks its shell, lowering its Defense and Sp. Def stats but sharply raising Attack, Sp. Atk, and Speed stats.", 15, Type.NORMAL, MoveCategory.STATUS);
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
			super(AttackNamesies.SPIKE_CANNON, "Sharp spikes are shot at the target in rapid succession. They hit two to five times in a row.", 15, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 20;
			super.accuracy = 100;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int hits = this.getNumHits(me);
			
			int hit = 1;
			for (; hit <= hits; hit++) {
				Messages.add(new MessageUpdate("Hit " + hit + "!"));
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b)) {
					break;
				}
			}
			
			hit--;
			
			// Print hits and gtfo
			Messages.add(new MessageUpdate("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!"));
		}

		public int getMinHits() {
			return 2;
		}

		public int getMaxHits() {
			return 5;
		}
	}

	static class Spikes extends Attack {
		private static final long serialVersionUID = 1L;

		Spikes() {
			super(AttackNamesies.SPIKES, "The user lays a trap of spikes at the opposing team's feet. The trap hurts Pok\u00e9mon that switch into battle.", 20, Type.GROUND, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.SPIKES);
			super.moveTypes.add(MoveType.FIELD);
		}
	}

	static class IcicleCrash extends Attack {
		private static final long serialVersionUID = 1L;

		IcicleCrash() {
			super(AttackNamesies.ICICLE_CRASH, "The user attacks by harshly dropping an icicle onto the target. It may also make the target flinch.", 10, Type.ICE, MoveCategory.PHYSICAL);
			super.power = 85;
			super.accuracy = 90;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 30;
		}
	}

	static class Lick extends Attack {
		private static final long serialVersionUID = 1L;

		Lick() {
			super(AttackNamesies.LICK, "The target is licked with a long tongue, causing damage. It may also leave the target with paralysis.", 30, Type.GHOST, MoveCategory.PHYSICAL);
			super.power = 30;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Spite extends Attack {
		private static final long serialVersionUID = 1L;

		Spite() {
			super(AttackNamesies.SPITE, "The user unleashes its grudge on the move last used by the target by cutting 4 PP from it.", 10, Type.GHOST, MoveCategory.STATUS);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			Move last = victim.getAttributes().getLastMoveUsed();
			
			// Fails if the victim hasn't attacked yet, their last move already has 0 PP, or they don't actually know the last move they used
			if (last == null || last.getPP() == 0 || !victim.hasMove(b, last.getAttack().namesies())) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			Messages.add(new MessageUpdate(victim.getName() + "'s " + last.getAttack().getName() + "'s PP was reduced by " + last.reducePP(4) + "!"));
		}
	}

	static class NightShade extends Attack {
		private static final long serialVersionUID = 1L;

		NightShade() {
			super(AttackNamesies.NIGHT_SHADE, "The user makes the target see a frightening mirage. It inflicts damage matching the user's level.", 15, Type.GHOST, MoveCategory.SPECIAL);
			super.accuracy = 100;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			o.reduceHealth(b, me.getLevel());
		}
	}

	static class ShadowBall extends Attack {
		private static final long serialVersionUID = 1L;

		ShadowBall() {
			super(AttackNamesies.SHADOW_BALL, "The user hurls a shadowy blob at the target. It may also lower the target's Sp. Def stat.", 15, Type.GHOST, MoveCategory.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.effectChance = 20;
			super.moveTypes.add(MoveType.BOMB_BALL);
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
		}
	}

	static class DreamEater extends Attack implements SapHealthEffect {
		private static final long serialVersionUID = 1L;

		DreamEater() {
			super(AttackNamesies.DREAM_EATER, "The user eats the dreams of a sleeping target. It absorbs half the damage caused to heal the user's HP.", 15, Type.PSYCHIC, MoveCategory.SPECIAL);
			super.power = 100;
			super.accuracy = 100;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			if (!o.hasStatus(StatusCondition.ASLEEP)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			super.apply(me, o, b);
		}

		public String getSapMessage(ActivePokemon victim) {
			return victim.getName() + "'s dream was eaten!";
		}
	}

	static class DarkPulse extends Attack {
		private static final long serialVersionUID = 1L;

		DarkPulse() {
			super(AttackNamesies.DARK_PULSE, "The user releases a horrible aura imbued with dark thoughts. It may also make the target flinch.", 15, Type.DARK, MoveCategory.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 20;
			super.moveTypes.add(MoveType.AURA_PULSE);
		}
	}

	static class Nightmare extends Attack {
		private static final long serialVersionUID = 1L;

		Nightmare() {
			super(AttackNamesies.NIGHTMARE, "A sleeping target sees a nightmare that inflicts some damage every turn.", 15, Type.GHOST, MoveCategory.STATUS);
			super.accuracy = 100;
			super.effects.add(EffectNamesies.NIGHTMARE);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}
	}

	static class ShadowPunch extends Attack {
		private static final long serialVersionUID = 1L;

		ShadowPunch() {
			super(AttackNamesies.SHADOW_PUNCH, "The user throws a punch from the shadows. The punch lands without fail.", 20, Type.GHOST, MoveCategory.PHYSICAL);
			super.power = 60;
			super.moveTypes.add(MoveType.PUNCHING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Bind extends Attack {
		private static final long serialVersionUID = 1L;

		Bind() {
			super(AttackNamesies.BIND, "Things such as long bodies or tentacles are used to bind and squeeze the target for four to five turns.", 20, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 15;
			super.accuracy = 85;
			super.effects.add(EffectNamesies.BINDED);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class RockTomb extends Attack {
		private static final long serialVersionUID = 1L;

		RockTomb() {
			super(AttackNamesies.ROCK_TOMB, "Boulders are hurled at the target. It also lowers the target's Speed by preventing its movement.", 10, Type.ROCK, MoveCategory.PHYSICAL);
			super.power = 60;
			super.accuracy = 95;
			super.statChanges[Stat.SPEED.index()] = -1;
		}
	}

	static class DragonBreath extends Attack {
		private static final long serialVersionUID = 1L;

		DragonBreath() {
			super(AttackNamesies.DRAGON_BREATH, "The user exhales a mighty gust that inflicts damage. It may also leave the target with paralysis.", 20, Type.DRAGON, MoveCategory.SPECIAL);
			super.power = 60;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.PARALYZED;
		}
	}

	static class IronTail extends Attack {
		private static final long serialVersionUID = 1L;

		IronTail() {
			super(AttackNamesies.IRON_TAIL, "The target is slammed with a steel-hard tail. It may also lower the target's Defense stat.", 15, Type.STEEL, MoveCategory.PHYSICAL);
			super.power = 100;
			super.accuracy = 75;
			super.effectChance = 30;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Meditate extends Attack {
		private static final long serialVersionUID = 1L;

		Meditate() {
			super(AttackNamesies.MEDITATE, "The user meditates to awaken the power deep within its body and raise its Attack stat.", 40, Type.PSYCHIC, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
		}
	}

	static class Synchronoise extends Attack {
		private static final long serialVersionUID = 1L;

		Synchronoise() {
			super(AttackNamesies.SYNCHRONOISE, "Using an odd shock wave, the user inflicts damage on any Pok\u00e9mon of the same type in the area around it.", 15, Type.PSYCHIC, MoveCategory.SPECIAL);
			super.power = 120;
			super.accuracy = 100;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			Type[] type = me.getType(b);
			
			// Like this is literally the stupidest move ever like srsly what is wrong with the creators
			if (o.isType(b, type[0]) || (type[1] != Type.NO_TYPE && o.isType(b, type[1]))) {
				super.apply(me, o, b);
			}
			else {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
			}
		}
	}

	static class Psyshock extends Attack implements OpponentStatSwitchingEffect {
		private static final long serialVersionUID = 1L;

		Psyshock() {
			super(AttackNamesies.PSYSHOCK, "The user materializes an odd psychic wave to attack the target. This attack does physical damage.", 10, Type.PSYCHIC, MoveCategory.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
		}

		public Stat switchStat(Stat s) {
			return s == Stat.SP_DEFENSE ? Stat.DEFENSE : s;
		}
	}

	static class ViceGrip extends Attack {
		private static final long serialVersionUID = 1L;

		ViceGrip() {
			super(AttackNamesies.VICE_GRIP, "The target is gripped and squeezed from both sides to inflict damage.", 30, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 55;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class MetalClaw extends Attack {
		private static final long serialVersionUID = 1L;

		MetalClaw() {
			super(AttackNamesies.METAL_CLAW, "The target is raked with steel claws. It may also raise the user's Attack stat.", 35, Type.STEEL, MoveCategory.PHYSICAL);
			super.power = 50;
			super.accuracy = 95;
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Guillotine extends Attack {
		private static final long serialVersionUID = 1L;

		Guillotine() {
			super(AttackNamesies.GUILLOTINE, "A vicious, tearing attack with big pincers. The target will faint instantly if this attack hits.", 5, Type.NORMAL, MoveCategory.PHYSICAL);
			super.accuracy = 30;
			super.moveTypes.add(MoveType.ONE_HIT_KO);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			// Fails when the opponent is at a higher level than the user
			if (me.getLevel() < o.getLevel()) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			// Sturdy prevents OHKO moves if the user is not a mold breaker
			if (o.hasAbility(AbilityNamesies.STURDY) && !me.breaksTheMold()) {
				Messages.add(new MessageUpdate(o.getName() + "'s " + AbilityNamesies.STURDY.getName() + " prevents OHKO moves!"));
				return;
			}
			
			// Certain death
			o.reduceHealth(b, o.getHP());
			Messages.add(new MessageUpdate("It's a One-Hit KO!"));
		}

		public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.accuracy + (me.getLevel() - o.getLevel());
		}
	}

	static class Crabhammer extends Attack implements CritStageEffect {
		private static final long serialVersionUID = 1L;

		Crabhammer() {
			super(AttackNamesies.CRABHAMMER, "The target is hammered with a large pincer. Critical hits land more easily.", 10, Type.WATER, MoveCategory.PHYSICAL);
			super.power = 100;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	static class Flail extends Attack {
		private static final long serialVersionUID = 1L;

		Flail() {
			super(AttackNamesies.FLAIL, "The user flails about aimlessly to attack. It becomes more powerful the less HP the user has.", 15, Type.NORMAL, MoveCategory.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			double ratio = me.getHPRatio();
			if (ratio > .7) return 20;
			if (ratio > .35) return 40;
			if (ratio > .2) return 80;
			if (ratio > .1) return 100;
			if (ratio > .04) return 150;
			return 200;
		}
	}

	static class Charge extends Attack {
		private static final long serialVersionUID = 1L;

		Charge() {
			super(AttackNamesies.CHARGE, "The user boosts the power of the Electric move it uses on the next turn. It also raises the user's Sp. Def stat.", 20, Type.ELECTRIC, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.CHARGE);
			super.selfTarget = true;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
		}
	}

	static class ChargeBeam extends Attack {
		private static final long serialVersionUID = 1L;

		ChargeBeam() {
			super(AttackNamesies.CHARGE_BEAM, "The user attacks with an electric charge. The user may use any remaining electricity to raise its Sp. Atk stat.", 10, Type.ELECTRIC, MoveCategory.SPECIAL);
			super.power = 50;
			super.accuracy = 90;
			super.effectChance = 70;
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
		}
	}

	static class MirrorCoat extends Attack {
		private static final long serialVersionUID = 1L;

		MirrorCoat() {
			super(AttackNamesies.MIRROR_COAT, "A retaliation move that counters any special attack, inflicting double the damage taken.", 20, Type.PSYCHIC, MoveCategory.SPECIAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.priority = -5;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int damageTaken = me.getAttributes().getDamageTaken();
			
			// Fails if no damage to reflect or if the opponent isn't using an attack of the proper category
			if (damageTaken == 0 || o.getMove() == null || o.getAttack().getCategory() != MoveCategory.SPECIAL || b.isFirstAttack()) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			o.reduceHealth(b, damageTaken*2);
		}
	}

	static class Counter extends Attack {
		private static final long serialVersionUID = 1L;

		Counter() {
			super(AttackNamesies.COUNTER, "A retaliation move that counters any physical attack, inflicting double the damage taken.", 20, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.priority = -5;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int damageTaken = me.getAttributes().getDamageTaken();
			
			// Fails if no damage to reflect or if the opponent isn't using an attack of the proper category
			if (damageTaken == 0 || o.getMove() == null || o.getAttack().getCategory() != MoveCategory.PHYSICAL || b.isFirstAttack()) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			o.reduceHealth(b, damageTaken*2);
		}
	}

	static class Barrage extends Attack implements MultiStrikeMove {
		private static final long serialVersionUID = 1L;

		Barrage() {
			super(AttackNamesies.BARRAGE, "Round objects are hurled at the target to strike two to five times in a row.", 20, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 15;
			super.accuracy = 85;
			super.moveTypes.add(MoveType.BOMB_BALL);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int hits = this.getNumHits(me);
			
			int hit = 1;
			for (; hit <= hits; hit++) {
				Messages.add(new MessageUpdate("Hit " + hit + "!"));
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b)) {
					break;
				}
			}
			
			hit--;
			
			// Print hits and gtfo
			Messages.add(new MessageUpdate("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!"));
		}

		public int getMinHits() {
			return 2;
		}

		public int getMaxHits() {
			return 5;
		}
	}

	static class BulletSeed extends Attack implements MultiStrikeMove {
		private static final long serialVersionUID = 1L;

		BulletSeed() {
			super(AttackNamesies.BULLET_SEED, "The user forcefully shoots seeds at the target. Two to five seeds are shot in rapid succession.", 30, Type.GRASS, MoveCategory.PHYSICAL);
			super.power = 25;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.BOMB_BALL);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int hits = this.getNumHits(me);
			
			int hit = 1;
			for (; hit <= hits; hit++) {
				Messages.add(new MessageUpdate("Hit " + hit + "!"));
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b)) {
					break;
				}
			}
			
			hit--;
			
			// Print hits and gtfo
			Messages.add(new MessageUpdate("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!"));
		}

		public int getMinHits() {
			return 2;
		}

		public int getMaxHits() {
			return 5;
		}
	}

	static class EggBomb extends Attack {
		private static final long serialVersionUID = 1L;

		EggBomb() {
			super(AttackNamesies.EGG_BOMB, "A large egg is hurled at the target with maximum force to inflict damage.", 10, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 100;
			super.accuracy = 75;
			super.moveTypes.add(MoveType.BOMB_BALL);
		}
	}

	static class WoodHammer extends Attack implements RecoilMove {
		private static final long serialVersionUID = 1L;

		WoodHammer() {
			super(AttackNamesies.WOOD_HAMMER, "The user slams its rugged body into the target to attack. The user also sustains serious damage.", 15, Type.GRASS, MoveCategory.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyRecoil(Battle b, ActivePokemon user, int damage) {
			if (user.hasAbility(AbilityNamesies.ROCK_HEAD) || user.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(new MessageUpdate(user.getName() + " was hurt by recoil!"));
			user.reduceHealth(b, (int)Math.ceil(damage/3.0), false);
		}
	}

	static class BoneClub extends Attack {
		private static final long serialVersionUID = 1L;

		BoneClub() {
			super(AttackNamesies.BONE_CLUB, "The user clubs the target with a bone. It may also make the target flinch.", 20, Type.GROUND, MoveCategory.PHYSICAL);
			super.power = 65;
			super.accuracy = 85;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 10;
		}
	}

	static class Bonemerang extends Attack implements MultiStrikeMove {
		private static final long serialVersionUID = 1L;

		Bonemerang() {
			super(AttackNamesies.BONEMERANG, "The user throws the bone it holds. The bone loops to hit the target twice, coming and going.", 10, Type.GROUND, MoveCategory.PHYSICAL);
			super.power = 50;
			super.accuracy = 90;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int hits = this.getNumHits(me);
			
			int hit = 1;
			for (; hit <= hits; hit++) {
				Messages.add(new MessageUpdate("Hit " + hit + "!"));
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b)) {
					break;
				}
			}
			
			hit--;
			
			// Print hits and gtfo
			Messages.add(new MessageUpdate("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!"));
		}

		public int getMinHits() {
			return 2;
		}

		public int getMaxHits() {
			return 2;
		}
	}

	static class BoneRush extends Attack implements MultiStrikeMove {
		private static final long serialVersionUID = 1L;

		BoneRush() {
			super(AttackNamesies.BONE_RUSH, "The user strikes the target with a hard bone two to five times in a row.", 10, Type.GROUND, MoveCategory.PHYSICAL);
			super.power = 25;
			super.accuracy = 90;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int hits = this.getNumHits(me);
			
			int hit = 1;
			for (; hit <= hits; hit++) {
				Messages.add(new MessageUpdate("Hit " + hit + "!"));
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b)) {
					break;
				}
			}
			
			hit--;
			
			// Print hits and gtfo
			Messages.add(new MessageUpdate("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!"));
		}

		public int getMinHits() {
			return 2;
		}

		public int getMaxHits() {
			return 5;
		}
	}

	static class RollingKick extends Attack {
		private static final long serialVersionUID = 1L;

		RollingKick() {
			super(AttackNamesies.ROLLING_KICK, "The user lashes out with a quick, spinning kick. It may also make the target flinch.", 15, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 60;
			super.accuracy = 85;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 30;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class JumpKick extends Attack implements CrashDamageMove {
		private static final long serialVersionUID = 1L;

		JumpKick() {
			super(AttackNamesies.JUMP_KICK, "The user jumps up high, then strikes with a kick. If the kick misses, the user hurts itself.", 10, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 100;
			super.accuracy = 95;
			super.moveTypes.add(MoveType.AIRBORNE);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void crash(Battle b, ActivePokemon user) {
			Messages.add(new MessageUpdate(user.getName() + " kept going and crashed!"));
			user.reduceHealth(b, user.getMaxHP()/3);
		}
	}

	static class BrickBreak extends Attack {
		private static final long serialVersionUID = 1L;

		BrickBreak() {
			super(AttackNamesies.BRICK_BREAK, "The user attacks with a swift chop. It can also break any barrier such as Light Screen and Reflect.", 15, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 75;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			BarrierEffect.breakBarriers(b, user);
		}
	}

	static class HighJumpKick extends Attack implements CrashDamageMove {
		private static final long serialVersionUID = 1L;

		HighJumpKick() {
			super(AttackNamesies.HIGH_JUMP_KICK, "The target is attacked with a knee kick from a jump. If it misses, the user is hurt instead.", 10, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 130;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.AIRBORNE);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void crash(Battle b, ActivePokemon user) {
			Messages.add(new MessageUpdate(user.getName() + " kept going and crashed!"));
			user.reduceHealth(b, user.getMaxHP()/2);
		}
	}

	static class BlazeKick extends Attack implements CritStageEffect {
		private static final long serialVersionUID = 1L;

		BlazeKick() {
			super(AttackNamesies.BLAZE_KICK, "The user launches a kick that lands a critical hit more easily. It may also leave the target with a burn.", 10, Type.FIRE, MoveCategory.PHYSICAL);
			super.power = 85;
			super.accuracy = 90;
			super.effectChance = 10;
			super.status = StatusCondition.BURNED;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	static class MegaKick extends Attack {
		private static final long serialVersionUID = 1L;

		MegaKick() {
			super(AttackNamesies.MEGA_KICK, "The target is attacked by a kick launched with muscle-packed power.", 5, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 120;
			super.accuracy = 75;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class CometPunch extends Attack implements MultiStrikeMove {
		private static final long serialVersionUID = 1L;

		CometPunch() {
			super(AttackNamesies.COMET_PUNCH, "The target is hit with a flurry of punches that strike two to five times in a row.", 15, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 18;
			super.accuracy = 85;
			super.moveTypes.add(MoveType.PUNCHING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int hits = this.getNumHits(me);
			
			int hit = 1;
			for (; hit <= hits; hit++) {
				Messages.add(new MessageUpdate("Hit " + hit + "!"));
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b)) {
					break;
				}
			}
			
			hit--;
			
			// Print hits and gtfo
			Messages.add(new MessageUpdate("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!"));
		}

		public int getMinHits() {
			return 2;
		}

		public int getMaxHits() {
			return 5;
		}
	}

	static class MachPunch extends Attack {
		private static final long serialVersionUID = 1L;

		MachPunch() {
			super(AttackNamesies.MACH_PUNCH, "The user throws a punch at blinding speed. It is certain to strike first.", 30, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PUNCHING);
			super.priority = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class BulletPunch extends Attack {
		private static final long serialVersionUID = 1L;

		BulletPunch() {
			super(AttackNamesies.BULLET_PUNCH, "The user strikes the target with tough punches as fast as bullets. This move always goes first.", 30, Type.STEEL, MoveCategory.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PUNCHING);
			super.priority = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class VacuumWave extends Attack {
		private static final long serialVersionUID = 1L;

		VacuumWave() {
			super(AttackNamesies.VACUUM_WAVE, "The user whirls its fists to send a wave of pure vacuum at the target. This move always goes first.", 30, Type.FIGHTING, MoveCategory.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.priority = 1;
		}
	}

	static class ThunderPunch extends Attack {
		private static final long serialVersionUID = 1L;

		ThunderPunch() {
			super(AttackNamesies.THUNDER_PUNCH, "The target is punched with an electrified fist. It may also leave the target with paralysis.", 15, Type.ELECTRIC, MoveCategory.PHYSICAL);
			super.power = 75;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.PUNCHING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class IcePunch extends Attack {
		private static final long serialVersionUID = 1L;

		IcePunch() {
			super(AttackNamesies.ICE_PUNCH, "The target is punched with an icy fist. It may also leave the target frozen.", 15, Type.ICE, MoveCategory.PHYSICAL);
			super.power = 75;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.FROZEN;
			super.moveTypes.add(MoveType.PUNCHING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class FirePunch extends Attack {
		private static final long serialVersionUID = 1L;

		FirePunch() {
			super(AttackNamesies.FIRE_PUNCH, "The target is punched with a fiery fist. It may also leave the target with a burn.", 15, Type.FIRE, MoveCategory.PHYSICAL);
			super.power = 75;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.BURNED;
			super.moveTypes.add(MoveType.PUNCHING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class SkyUppercut extends Attack implements AccuracyBypassEffect {
		private static final long serialVersionUID = 1L;

		SkyUppercut() {
			super(AttackNamesies.SKY_UPPERCUT, "The user attacks the target with an uppercut thrown skyward with force.", 15, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 85;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.PUNCHING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			// Does not deal double damage when opponent is flying
			return super.power;
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			// Always hit when the opponent is flying
			return defending.isSemiInvulnerableFlying();
		}
	}

	static class MegaPunch extends Attack {
		private static final long serialVersionUID = 1L;

		MegaPunch() {
			super(AttackNamesies.MEGA_PUNCH, "The target is slugged by a punch thrown with muscle-packed power.", 20, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 85;
			super.moveTypes.add(MoveType.PUNCHING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class FocusPunch extends Attack {
		private static final long serialVersionUID = 1L;

		FocusPunch() {
			super(AttackNamesies.FOCUS_PUNCH, "The user focuses its mind before launching a punch. It will fail if the user is hit before it is used.", 20, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 150;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.FOCUSING);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.PUNCHING);
			super.selfTarget = true;
			super.priority = -3;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void startTurn(Battle b, ActivePokemon me) {
			super.applyEffects(b, me, me);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			if (me.hasEffect(EffectNamesies.FOCUSING)) {
				super.applyDamage(me, o, b);
			}
		}
	}

	static class MeFirst extends Attack {
		private static final long serialVersionUID = 1L;

		MeFirst() {
			super(AttackNamesies.ME_FIRST, "The user tries to cut ahead of the target to steal and use the target's intended move with greater power.", 20, Type.NORMAL, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.METRONOMELESS);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			// Fails if it is the second turn or the opponent is using a status move
			if (!b.isFirstAttack() || o.getMove() == null || o.getAttack().getCategory() == MoveCategory.STATUS) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			me.addEffect((PokemonEffect)EffectNamesies.FIDDY_PERCENT_STRONGER.getEffect());
			me.callNewMove(b, o, new Move(o.getAttack()));
			me.getAttributes().removeEffect(EffectNamesies.FIDDY_PERCENT_STRONGER);
		}
	}

	static class Refresh extends Attack {
		private static final long serialVersionUID = 1L;

		Refresh() {
			super(AttackNamesies.REFRESH, "The user rests to cure itself of a poisoning, burn, or paralysis.", 20, Type.NORMAL, MoveCategory.STATUS);
			super.selfTarget = true;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			if (!me.hasStatus()) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			super.apply(me, o, b);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			Status.removeStatus(b, user, CastSource.ATTACK);
		}
	}

	static class PowerWhip extends Attack {
		private static final long serialVersionUID = 1L;

		PowerWhip() {
			super(AttackNamesies.POWER_WHIP, "The user violently whirls its vines or tentacles to harshly lash the target.", 10, Type.GRASS, MoveCategory.PHYSICAL);
			super.power = 120;
			super.accuracy = 85;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Smog extends Attack {
		private static final long serialVersionUID = 1L;

		Smog() {
			super(AttackNamesies.SMOG, "The target is attacked with a discharge of filthy gases. It may also poison the target.", 20, Type.POISON, MoveCategory.SPECIAL);
			super.power = 30;
			super.accuracy = 70;
			super.effectChance = 40;
			super.status = StatusCondition.POISONED;
		}
	}

	static class ClearSmog extends Attack {
		private static final long serialVersionUID = 1L;

		ClearSmog() {
			super(AttackNamesies.CLEAR_SMOG, "The user attacks by throwing a clump of special mud. All status changes are returned to normal.", 15, Type.POISON, MoveCategory.SPECIAL);
			super.power = 50;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			user.getAttributes().resetStages();
			victim.getAttributes().resetStages();
			Messages.add(new MessageUpdate("All stat changes were eliminated!"));
		}
	}

	static class HammerArm extends Attack {
		private static final long serialVersionUID = 1L;

		HammerArm() {
			super(AttackNamesies.HAMMER_ARM, "The user swings and hits with its strong and heavy fist. It lowers the user's Speed, however.", 10, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 100;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.PUNCHING);
			super.selfTarget = true;
			super.statChanges[Stat.SPEED.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class IceHammer extends Attack {
		private static final long serialVersionUID = 1L;

		IceHammer() {
			super(AttackNamesies.ICE_HAMMER, "The user swings and hits with its strong, heavy fist. It lowers the user's Speed, however.", 10, Type.ICE, MoveCategory.PHYSICAL);
			super.power = 100;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.PUNCHING);
			super.selfTarget = true;
			super.statChanges[Stat.SPEED.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class SoftBoiled extends Attack implements SelfHealingMove {
		private static final long serialVersionUID = 1L;

		SoftBoiled() {
			super(AttackNamesies.SOFT_BOILED, "The user restores its own HP by up to half of its maximum HP. May also be used in the field to heal HP.", 10, Type.NORMAL, MoveCategory.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b) {
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			victim.healHealthFraction(1/2.0);
			
			Messages.add(new MessageUpdate(victim.getName() + "'s health was restored!").updatePokemon(b, victim));
		}
	}

	static class AncientPower extends Attack {
		private static final long serialVersionUID = 1L;

		AncientPower() {
			super(AttackNamesies.ANCIENT_POWER, "The user attacks with a prehistoric power. It may also raise all the user's stats at once.", 5, Type.ROCK, MoveCategory.SPECIAL);
			super.power = 60;
			super.accuracy = 100;
			super.effectChance = 10;
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
			super.statChanges[Stat.SPEED.index()] = 1;
			super.statChanges[Stat.ACCURACY.index()] = 1;
			super.statChanges[Stat.EVASION.index()] = 1;
		}
	}

	static class Tickle extends Attack {
		private static final long serialVersionUID = 1L;

		Tickle() {
			super(AttackNamesies.TICKLE, "The user tickles the target into laughing, reducing its Attack and Defense stats.", 20, Type.NORMAL, MoveCategory.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.ATTACK.index()] = -1;
			super.statChanges[Stat.DEFENSE.index()] = -1;
		}
	}

	static class DizzyPunch extends Attack {
		private static final long serialVersionUID = 1L;

		DizzyPunch() {
			super(AttackNamesies.DIZZY_PUNCH, "The target is hit with rhythmically launched punches that may also leave it confused.", 10, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.CONFUSION);
			super.effectChance = 20;
			super.moveTypes.add(MoveType.PUNCHING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Outrage extends Attack {
		private static final long serialVersionUID = 1L;

		Outrage() {
			super(AttackNamesies.OUTRAGE, "The user rampages and attacks for two to three turns. It then becomes confused, however.", 10, Type.DRAGON, MoveCategory.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.SELF_CONFUSION);
			super.selfTarget = true;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class DragonDance extends Attack {
		private static final long serialVersionUID = 1L;

		DragonDance() {
			super(AttackNamesies.DRAGON_DANCE, "The user vigorously performs a mystic, powerful dance that boosts its Attack and Speed stats.", 20, Type.DRAGON, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.SPEED.index()] = 1;
		}
	}

	static class DragonPulse extends Attack {
		private static final long serialVersionUID = 1L;

		DragonPulse() {
			super(AttackNamesies.DRAGON_PULSE, "The target is attacked with a shock wave generated by the user's gaping mouth.", 10, Type.DRAGON, MoveCategory.SPECIAL);
			super.power = 85;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.AURA_PULSE);
		}
	}

	static class DracoMeteor extends Attack {
		private static final long serialVersionUID = 1L;

		DracoMeteor() {
			super(AttackNamesies.DRACO_METEOR, "Comets are summoned down from the sky onto the target. The attack's recoil harshly reduces the user's Sp. Atk stat.", 5, Type.DRAGON, MoveCategory.SPECIAL);
			super.power = 130;
			super.accuracy = 90;
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = -2;
		}
	}

	static class Waterfall extends Attack {
		private static final long serialVersionUID = 1L;

		Waterfall() {
			super(AttackNamesies.WATERFALL, "The user charges at the target and may make it flinch. It can also be used to climb a waterfall.", 15, Type.WATER, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 20;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class ReflectType extends Attack implements ChangeTypeSource {
		private static final long serialVersionUID = 1L;

		ReflectType() {
			super(AttackNamesies.REFLECT_TYPE, "The user reflects the target's type, making it the same type as the target.", 15, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.CHANGE_TYPE);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
			return b.getOtherPokemon(caster.isPlayer()).getType(b).clone();
		}
	}

	static class MagicalLeaf extends Attack {
		private static final long serialVersionUID = 1L;

		MagicalLeaf() {
			super(AttackNamesies.MAGICAL_LEAF, "The user scatters curious leaves that chase the target. This attack will not miss.", 20, Type.GRASS, MoveCategory.SPECIAL);
			super.power = 60;
		}
	}

	static class PowerSwap extends Attack {
		private static final long serialVersionUID = 1L;
		private static final Stat[] swapStats = { Stat.ATTACK, Stat.SP_ATTACK };

		PowerSwap() {
			super(AttackNamesies.POWER_SWAP, "The user employs its psychic power to switch changes to its Attack and Sp. Atk with the target.", 10, Type.PSYCHIC, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			for (Stat s : swapStats) {
				user.getAttributes().swapStages(s, victim);
			}
			
			Messages.add(new MessageUpdate(user.getName() + " swapped its stats with " + victim.getName() + "!"));
		}
	}

	static class GuardSwap extends Attack {
		private static final long serialVersionUID = 1L;
		private static final Stat[] swapStats = { Stat.DEFENSE, Stat.SP_DEFENSE };

		GuardSwap() {
			super(AttackNamesies.GUARD_SWAP, "The user employs its psychic power to switch changes to its Defense and Sp. Def with the target.", 10, Type.PSYCHIC, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			for (Stat s : swapStats) {
				user.getAttributes().swapStages(s, victim);
			}
			
			Messages.add(new MessageUpdate(user.getName() + " swapped its stats with " + victim.getName() + "!"));
		}
	}

	static class SpeedSwap extends Attack {
		private static final long serialVersionUID = 1L;

		SpeedSwap() {
			super(AttackNamesies.SPEED_SWAP, "The user exchanges Speed stats with the target.", 10, Type.PSYCHIC, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			// NOTE: Looks like this is supposed to actually swap the stats and not just the stages but I don't really care it should do the same thing as power and guard swap because that makes more sense sue me
			user.getAttributes().swapStages(Stat.SPEED, victim);
			Messages.add(new MessageUpdate(user.getName() + " swapped its stats with " + victim.getName() + "!"));
		}
	}

	static class Copycat extends Attack {
		private static final long serialVersionUID = 1L;

		Copycat() {
			super(AttackNamesies.COPYCAT, "The user mimics the move used immediately before it. The move fails if no other move has been used yet.", 20, Type.NORMAL, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.ENCORELESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.MIRRORLESS);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			Move mirror = o.getAttributes().getLastMoveUsed();
			if (mirror == null || mirror.getAttack().isMoveType(MoveType.MIRRORLESS)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			me.callNewMove(b, o, new Move(mirror.getAttack()));
		}
	}

	static class Transform extends Attack {
		private static final long serialVersionUID = 1L;

		Transform() {
			super(AttackNamesies.TRANSFORM, "The user transforms into a copy of the target right down to having the same move set.", 10, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.TRANSFORMED);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.ENCORELESS);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.selfTarget = true;
		}
	}

	static class Substitute extends Attack {
		private static final long serialVersionUID = 1L;

		Substitute() {
			super(AttackNamesies.SUBSTITUTE, "The user makes a copy of itself using some of its HP. The copy serves as the user's decoy.", 10, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.SUBSTITUTE);
			super.selfTarget = true;
		}
	}

	static class RazorWind extends Attack implements MultiTurnMove, CritStageEffect {
		private static final long serialVersionUID = 1L;

		RazorWind() {
			super(AttackNamesies.RAZOR_WIND, "A two-turn attack. Blades of wind hit opposing Pok\u00e9mon on the second turn. Critical hits land more easily.", 10, Type.NORMAL, MoveCategory.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
		}

		public boolean chargesFirst() {
			return true;
		}

		public boolean semiInvulnerability() {
			return false;
		}

		public void charge(ActivePokemon user, Battle b) {
			Messages.add(new MessageUpdate(getChargeMessage(user)));
		}

		private String getChargeMessage(ActivePokemon user) {
			return user.getName() + " whipped up a whirlwind!";
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	static class LovelyKiss extends Attack {
		private static final long serialVersionUID = 1L;

		LovelyKiss() {
			super(AttackNamesies.LOVELY_KISS, "With a scary face, the user tries to force a kiss on the target. If it succeeds, the target falls asleep.", 10, Type.NORMAL, MoveCategory.STATUS);
			super.accuracy = 75;
			super.status = StatusCondition.ASLEEP;
		}
	}

	static class PowderSnow extends Attack {
		private static final long serialVersionUID = 1L;

		PowderSnow() {
			super(AttackNamesies.POWDER_SNOW, "The user attacks with a chilling gust of powdery snow. It may also freeze the targets.", 25, Type.ICE, MoveCategory.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.FROZEN;
		}
	}

	static class HeartStamp extends Attack {
		private static final long serialVersionUID = 1L;

		HeartStamp() {
			super(AttackNamesies.HEART_STAMP, "The user unleashes a vicious blow after its cute act makes the target less wary. It may also make the target flinch.", 25, Type.PSYCHIC, MoveCategory.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 30;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class FakeTears extends Attack {
		private static final long serialVersionUID = 1L;

		FakeTears() {
			super(AttackNamesies.FAKE_TEARS, "The user feigns crying to fluster the target, harshly lowering its Sp. Def stat.", 20, Type.DARK, MoveCategory.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.SP_DEFENSE.index()] = -2;
		}
	}

	static class Avalanche extends Attack {
		private static final long serialVersionUID = 1L;

		Avalanche() {
			super(AttackNamesies.AVALANCHE, "An attack move that inflicts double the damage if the user has been hurt by the target in the same turn.", 10, Type.ICE, MoveCategory.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.priority = -4;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(me.getAttributes().hasTakenDamage() ? 2 : 1);
		}
	}

	static class Blizzard extends Attack implements AccuracyBypassEffect {
		private static final long serialVersionUID = 1L;

		Blizzard() {
			super(AttackNamesies.BLIZZARD, "A howling blizzard is summoned to strike the opposing team. It may also freeze them solid.", 5, Type.ICE, MoveCategory.SPECIAL);
			super.power = 110;
			super.accuracy = 70;
			super.effectChance = 10;
			super.status = StatusCondition.FROZEN;
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			// Always hits when it's hailing unless the opponent is hiding (I think -- the hiding part is not specified on Bulbapedia)
			return b.getWeather().namesies() == EffectNamesies.HAILING && !defending.isSemiInvulnerable();
		}
	}

	static class ShockWave extends Attack {
		private static final long serialVersionUID = 1L;

		ShockWave() {
			super(AttackNamesies.SHOCK_WAVE, "The user strikes the target with a quick jolt of electricity. This attack cannot be evaded.", 20, Type.ELECTRIC, MoveCategory.SPECIAL);
			super.power = 60;
		}
	}

	static class LavaPlume extends Attack {
		private static final long serialVersionUID = 1L;

		LavaPlume() {
			super(AttackNamesies.LAVA_PLUME, "An inferno of scarlet flames torches everything around the user. It may leave targets with a burn.", 15, Type.FIRE, MoveCategory.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.BURNED;
		}
	}

	static class WorkUp extends Attack {
		private static final long serialVersionUID = 1L;

		WorkUp() {
			super(AttackNamesies.WORK_UP, "The user is roused, and its Attack and Sp. Atk stats increase.", 30, Type.NORMAL, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
		}
	}

	static class GigaImpact extends Attack implements MultiTurnMove {
		private static final long serialVersionUID = 1L;

		GigaImpact() {
			super(AttackNamesies.GIGA_IMPACT, "The user charges at the target using every bit of its power. The user must rest on the next turn.", 5, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 150;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public boolean chargesFirst() {
			return false;
		}

		public boolean semiInvulnerability() {
			return false;
		}

		public void charge(ActivePokemon user, Battle b) {
			Messages.add(new MessageUpdate(getChargeMessage(user)));
		}

		private String getChargeMessage(ActivePokemon user) {
			return user.getName() + " must recharge!";
		}
	}

	static class Splash extends Attack {
		private static final long serialVersionUID = 1L;

		Splash() {
			super(AttackNamesies.SPLASH, "The user just flops and splashes around to no effect at all...", 40, Type.NORMAL, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.AIRBORNE);
			super.selfTarget = true;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			Messages.add(new MessageUpdate("But nothing happened..."));
		}
	}

	static class Mist extends Attack {
		private static final long serialVersionUID = 1L;

		Mist() {
			super(AttackNamesies.MIST, "The user cloaks its body with a white mist that prevents any of its stats from being cut for five turns.", 30, Type.ICE, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.MIST);
			super.selfTarget = true;
		}
	}

	static class LastResort extends Attack {
		private static final long serialVersionUID = 1L;

		LastResort() {
			super(AttackNamesies.LAST_RESORT, "This move can be used only after the user has used all the other moves it knows in the battle.", 5, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 140;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			for (Move m : me.getMoves(b)) {
				if (m.getAttack().namesies() == super.namesies) {
					continue;
				}
				
				if (!m.used()) {
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
					return;
				}
			}
			
			super.apply(me, o, b);
		}
	}

	static class TrumpCard extends Attack {
		private static final long serialVersionUID = 1L;

		TrumpCard() {
			super(AttackNamesies.TRUMP_CARD, "The fewer PP this move has, the greater its attack power.", 5, Type.NORMAL, MoveCategory.SPECIAL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
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
			super(AttackNamesies.MUDDY_WATER, "The user attacks by shooting muddy water at the opposing team. It may also lower the targets' accuracy.", 10, Type.WATER, MoveCategory.SPECIAL);
			super.power = 90;
			super.accuracy = 85;
			super.effectChance = 30;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	static class Conversion extends Attack implements ChangeTypeSource {
		private static final long serialVersionUID = 1L;

		Conversion() {
			super(AttackNamesies.CONVERSION, "The user changes its type to become the same type as one of its moves.", 30, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.CHANGE_TYPE);
			super.selfTarget = true;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			for (Move m : me.getMoves(b)) {
				if (!me.isType(b, m.getAttack().getActualType())) {
					super.apply(me, o, b);
					return;
				}
			}
			
			Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
			List<Type> types = new ArrayList<>();
			for (Move m : victim.getMoves(b)) {
				Type t = m.getAttack().getActualType();
				if (!victim.isType(b, t)) {
					types.add(t);
				}
			}
			
			return new Type[] { RandomUtils.getRandomValue(types), Type.NO_TYPE };
		}
	}

	static class Conversion2 extends Attack implements ChangeTypeSource {
		private static final long serialVersionUID = 1L;
		private List<Type> getResistances(ActivePokemon victim, Type attacking, Battle b) {
			List<Type> types = new ArrayList<>();
			for (Type t : Type.values()) {
				if (Type.getBasicAdvantage(attacking, t) < 1 && !victim.isType(b, t)) {
					types.add(t);
				}
			}
			
			return types;
		}

		Conversion2() {
			super(AttackNamesies.CONVERSION2, "The user changes its type to make itself resistant to the type of the attack the opponent used last.", 30, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.CHANGE_TYPE);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			Move m = o.getAttributes().getLastMoveUsed();
			if (m == null || getResistances(me, m.getType(), b).size() == 0) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			super.apply(me, o, b);
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
			ActivePokemon other = b.getOtherPokemon(victim.isPlayer());
			List<Type> types = getResistances(victim, other.getAttributes().getLastMoveUsed().getType(), b);
			return new Type[] { RandomUtils.getRandomValue(types), Type.NO_TYPE };
		}
	}

	static class Sharpen extends Attack {
		private static final long serialVersionUID = 1L;

		Sharpen() {
			super(AttackNamesies.SHARPEN, "The user reduces its polygon count to make itself more jagged, raising the Attack stat.", 30, Type.NORMAL, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
		}
	}

	static class MagicCoat extends Attack {
		private static final long serialVersionUID = 1L;

		MagicCoat() {
			super(AttackNamesies.MAGIC_COAT, "A barrier reflects back to the target moves like Leech Seed and moves that damage status.", 15, Type.PSYCHIC, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.MAGIC_COAT);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
			super.priority = 4;
		}
	}

	static class SkyDrop extends Attack {
		private static final long serialVersionUID = 1L;

		SkyDrop() {
			super(AttackNamesies.SKY_DROP, "The user takes the target into the sky, then slams it into the ground.", 10, Type.FLYING, MoveCategory.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.AIRBORNE);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			if (o.getWeight(b) > 440) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			super.apply(me, o, b);
		}
	}

	static class IronHead extends Attack {
		private static final long serialVersionUID = 1L;

		IronHead() {
			super(AttackNamesies.IRON_HEAD, "The user slams the target with its steel-hard head. It may also make the target flinch.", 15, Type.STEEL, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 30;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class RockSlide extends Attack {
		private static final long serialVersionUID = 1L;

		RockSlide() {
			super(AttackNamesies.ROCK_SLIDE, "Large boulders are hurled at the opposing team to inflict damage. It may also make the targets flinch.", 10, Type.ROCK, MoveCategory.PHYSICAL);
			super.power = 75;
			super.accuracy = 90;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 30;
		}
	}

	static class Snore extends Attack {
		private static final long serialVersionUID = 1L;

		Snore() {
			super(AttackNamesies.SNORE, "An attack that can be used only if the user is asleep. The harsh noise may also make the target flinch.", 15, Type.NORMAL, MoveCategory.SPECIAL);
			super.power = 50;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 30;
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.ASLEEP_USER);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			if (!me.hasStatus(StatusCondition.ASLEEP)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			super.apply(me, o, b);
		}
	}

	static class SleepTalk extends Attack {
		private static final long serialVersionUID = 1L;

		SleepTalk() {
			super(AttackNamesies.SLEEP_TALK, "While it is asleep, the user randomly uses one of the moves it knows.", 10, Type.NORMAL, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.ASLEEP_USER);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			if (!me.hasStatus(StatusCondition.ASLEEP)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			List<Move> moves = new ArrayList<>();
			for (Move m : me.getMoves(b)) {
				if (!m.getAttack().isMoveType(MoveType.SLEEP_TALK_FAIL)) {
					moves.add(m);
				}
			}
			
			if (moves.size() == 0) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			me.callNewMove(b, o, RandomUtils.getRandomValue(moves));
		}
	}

	static class Block extends Attack {
		private static final long serialVersionUID = 1L;

		Block() {
			super(AttackNamesies.BLOCK, "The user blocks the target's way with arms spread wide to prevent escape.", 5, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.TRAPPED);
		}
	}

	static class SkyAttack extends Attack implements MultiTurnMove, CritStageEffect {
		private static final long serialVersionUID = 1L;

		SkyAttack() {
			super(AttackNamesies.SKY_ATTACK, "A second-turn attack move where critical hits land more easily. It may also make the target flinch.", 5, Type.FLYING, MoveCategory.PHYSICAL);
			super.power = 140;
			super.accuracy = 90;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 30;
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
		}

		public boolean chargesFirst() {
			return true;
		}

		public boolean semiInvulnerability() {
			return false;
		}

		public void charge(ActivePokemon user, Battle b) {
			Messages.add(new MessageUpdate(getChargeMessage(user)));
		}

		private String getChargeMessage(ActivePokemon user) {
			return user.getName() + " started glowing!";
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	static class DragonRush extends Attack implements AccuracyBypassEffect {
		private static final long serialVersionUID = 1L;

		DragonRush() {
			super(AttackNamesies.DRAGON_RUSH, "The user tackles the target while exhibiting overwhelming menace. It may also make the target flinch.", 10, Type.DRAGON, MoveCategory.PHYSICAL);
			super.power = 100;
			super.accuracy = 75;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 20;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(o.hasEffect(EffectNamesies.USED_MINIMIZE) ? 2 : 1);
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			return !defending.isSemiInvulnerable() && defending.hasEffect(EffectNamesies.USED_MINIMIZE);
		}
	}

	static class AuraSphere extends Attack {
		private static final long serialVersionUID = 1L;

		AuraSphere() {
			super(AttackNamesies.AURA_SPHERE, "The user looses a blast of aura power from deep within its body at the target. This move is certain to hit.", 20, Type.FIGHTING, MoveCategory.SPECIAL);
			super.power = 80;
			super.moveTypes.add(MoveType.BOMB_BALL);
			super.moveTypes.add(MoveType.AURA_PULSE);
		}
	}

	static class Psystrike extends Attack implements OpponentStatSwitchingEffect {
		private static final long serialVersionUID = 1L;

		Psystrike() {
			super(AttackNamesies.PSYSTRIKE, "The user materializes an odd psychic wave to attack the target. This attack does physical damage.", 10, Type.PSYCHIC, MoveCategory.SPECIAL);
			super.power = 100;
			super.accuracy = 100;
		}

		public Stat switchStat(Stat s) {
			return s == Stat.SP_DEFENSE ? Stat.DEFENSE : s;
		}
	}

	static class Eruption extends Attack {
		private static final long serialVersionUID = 1L;

		Eruption() {
			super(AttackNamesies.ERUPTION, "The user attacks the opposing team with explosive fury. The lower the user's HP, the less powerful this attack becomes.", 5, Type.FIRE, MoveCategory.SPECIAL);
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return (int)Math.min(1, (150*me.getHPRatio()));
		}
	}

	static class Charm extends Attack {
		private static final long serialVersionUID = 1L;

		Charm() {
			super(AttackNamesies.CHARM, "The user gazes at the target rather charmingly, making it less wary. The target's Attack is harshly lowered.", 20, Type.FAIRY, MoveCategory.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.ATTACK.index()] = -2;
		}
	}

	static class EchoedVoice extends Attack {
		private static final long serialVersionUID = 1L;

		EchoedVoice() {
			super(AttackNamesies.ECHOED_VOICE, "The user attacks the target with an echoing voice. If this move is used every turn, it does greater damage.", 15, Type.NORMAL, MoveCategory.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SOUND_BASED);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*Math.min(5, me.getAttributes().getCount());
		}
	}

	static class PsychoShift extends Attack {
		private static final long serialVersionUID = 1L;

		PsychoShift() {
			super(AttackNamesies.PSYCHO_SHIFT, "Using its psychic power of suggestion, the user transfers its status problems to the target.", 10, Type.PSYCHIC, MoveCategory.STATUS);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			String message = user.getName() + " transferred its status condition to " + victim.getName() + "!";
			if (!user.hasStatus() || !Status.giveStatus(b, user, victim, user.getStatus().getType(), message)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			user.removeStatus();
			Messages.add(new MessageUpdate().updatePokemon(b, user));
		}
	}

	static class ShadowSneak extends Attack {
		private static final long serialVersionUID = 1L;

		ShadowSneak() {
			super(AttackNamesies.SHADOW_SNEAK, "The user extends its shadow and attacks the target from behind. This move always goes first.", 30, Type.GHOST, MoveCategory.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.priority = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class SpiderWeb extends Attack {
		private static final long serialVersionUID = 1L;

		SpiderWeb() {
			super(AttackNamesies.SPIDER_WEB, "The user ensnares the target with thin, gooey silk so it can't flee from battle.", 10, Type.BUG, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.TRAPPED);
		}
	}

	static class SweetKiss extends Attack {
		private static final long serialVersionUID = 1L;

		SweetKiss() {
			super(AttackNamesies.SWEET_KISS, "The user kisses the target with a sweet, angelic cuteness that causes confusion.", 10, Type.FAIRY, MoveCategory.STATUS);
			super.accuracy = 75;
			super.effects.add(EffectNamesies.CONFUSION);
		}
	}

	static class OminousWind extends Attack {
		private static final long serialVersionUID = 1L;

		OminousWind() {
			super(AttackNamesies.OMINOUS_WIND, "The user blasts the target with a gust of repulsive wind. It may also raise all the user's stats at once.", 5, Type.GHOST, MoveCategory.SPECIAL);
			super.power = 60;
			super.accuracy = 100;
			super.effectChance = 10;
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
			super.statChanges[Stat.SPEED.index()] = 1;
			super.statChanges[Stat.ACCURACY.index()] = 1;
			super.statChanges[Stat.EVASION.index()] = 1;
		}
	}

	static class CottonSpore extends Attack {
		private static final long serialVersionUID = 1L;

		CottonSpore() {
			super(AttackNamesies.COTTON_SPORE, "The user releases cotton-like spores that cling to the target, harshly reducing its Speed stat.", 40, Type.GRASS, MoveCategory.STATUS);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.POWDER);
			super.statChanges[Stat.SPEED.index()] = -2;
		}
	}

	static class CottonGuard extends Attack {
		private static final long serialVersionUID = 1L;

		CottonGuard() {
			super(AttackNamesies.COTTON_GUARD, "The user protects itself by wrapping its body in soft cotton, drastically raising the user's Defense stat.", 10, Type.GRASS, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 3;
		}
	}

	static class GrassWhistle extends Attack {
		private static final long serialVersionUID = 1L;

		GrassWhistle() {
			super(AttackNamesies.GRASS_WHISTLE, "The user plays a pleasant melody that lulls the target into a deep sleep.", 15, Type.GRASS, MoveCategory.STATUS);
			super.accuracy = 55;
			super.status = StatusCondition.ASLEEP;
			super.moveTypes.add(MoveType.SOUND_BASED);
		}
	}

	static class Torment extends Attack {
		private static final long serialVersionUID = 1L;

		Torment() {
			super(AttackNamesies.TORMENT, "The user torments and enrages the target, making it incapable of using the same move twice in a row.", 15, Type.DARK, MoveCategory.STATUS);
			super.accuracy = 100;
			super.effects.add(EffectNamesies.TORMENT);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
		}
	}

	static class HiddenPower extends Attack {
		private static final long serialVersionUID = 1L;

		HiddenPower() {
			super(AttackNamesies.HIDDEN_POWER, "A unique attack that varies in type and intensity depending on the Pok\u00e9mon using it.", 15, Type.NORMAL, MoveCategory.SPECIAL);
			super.power = 60;
			super.accuracy = 100;
		}

		public Type setType(Battle b, ActivePokemon user) {
			return user.getHiddenPowerType();
		}
	}

	static class Psywave extends Attack {
		private static final long serialVersionUID = 1L;

		Psywave() {
			super(AttackNamesies.PSYWAVE, "The target is attacked with an odd psychic wave. The attack varies in intensity.", 15, Type.PSYCHIC, MoveCategory.SPECIAL);
			super.accuracy = 100;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			o.reduceHealth(b, (int)Math.max(1, (RandomUtils.getRandomInt(11) + 5)*me.getLevel()/10.0));
		}
	}

	static class PainSplit extends Attack {
		private static final long serialVersionUID = 1L;

		PainSplit() {
			super(AttackNamesies.PAIN_SPLIT, "The user adds its HP to the target's HP, then equally shares the combined HP with the target.", 20, Type.NORMAL, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
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
			super(AttackNamesies.BIDE, "The user endures attacks for two turns, then strikes back to cause double the damage taken.", 10, Type.NORMAL, MoveCategory.PHYSICAL);
			super.effects.add(EffectNamesies.BIDE);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.selfTarget = true;
			super.priority = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			super.applyEffects(b, me, me);
		}
	}

	static class Autotomize extends Attack {
		private static final long serialVersionUID = 1L;

		Autotomize() {
			super(AttackNamesies.AUTOTOMIZE, "The user sheds part of its body to make itself lighter and sharply raise its Speed stat.", 15, Type.STEEL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.HALF_WEIGHT);
			super.selfTarget = true;
			super.statChanges[Stat.SPEED.index()] = 2;
		}
	}

	static class StruggleBug extends Attack {
		private static final long serialVersionUID = 1L;

		StruggleBug() {
			super(AttackNamesies.STRUGGLE_BUG, "While resisting, the user attacks the opposing Pok\u00e9mon. The targets' Sp. Atk stat is reduced.", 20, Type.BUG, MoveCategory.SPECIAL);
			super.power = 50;
			super.accuracy = 100;
			super.statChanges[Stat.SP_ATTACK.index()] = -1;
		}
	}

	static class PowerTrick extends Attack {
		private static final long serialVersionUID = 1L;

		PowerTrick() {
			super(AttackNamesies.POWER_TRICK, "The user employs its psychic power to switch its Attack with its Defense stat.", 10, Type.PSYCHIC, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.POWER_TRICK);
			super.selfTarget = true;
		}
	}

	static class PowerSplit extends Attack {
		private static final long serialVersionUID = 1L;

		PowerSplit() {
			super(AttackNamesies.POWER_SPLIT, "The user employs its psychic power to average its Attack and Sp. Atk stats with those of the target's.", 10, Type.PSYCHIC, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.POWER_SPLIT);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}
	}

	static class GuardSplit extends Attack {
		private static final long serialVersionUID = 1L;

		GuardSplit() {
			super(AttackNamesies.GUARD_SPLIT, "The user employs its psychic power to average its Defense and Sp. Def stats with those of its target's.", 10, Type.PSYCHIC, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.GUARD_SPLIT);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}
	}

	static class HoneClaws extends Attack {
		private static final long serialVersionUID = 1L;

		HoneClaws() {
			super(AttackNamesies.HONE_CLAWS, "The user sharpens its claws to boost its Attack stat and accuracy.", 15, Type.DARK, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.ACCURACY.index()] = 1;
		}
	}

	static class BeatUp extends Attack {
		private static final long serialVersionUID = 1L;

		BeatUp() {
			super(AttackNamesies.BEAT_UP, "The user gets all party Pok\u00e9mon to attack the target. The more party Pok\u00e9mon, the greater the number of attacks.", 10, Type.DARK, MoveCategory.PHYSICAL);
			super.power = 10;
			super.accuracy = 100;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			for (ActivePokemon p : b.getTrainer(me.isPlayer()).getTeam()) {
				// Only healthy Pokemon get to attack
				if (!p.canFight() || p.hasStatus()) {
					continue;
				}
				
				// Stop killing the dead
				if (o.isFainted(b)) {
					break;
				}
				
				Move temp = p.getMove();
				p.setMove(new Move(super.namesies.getAttack()));
				Messages.add(new MessageUpdate(p.getName() + "'s attack!"));
				super.applyDamage(p, o, b);
				p.setMove(temp);
			}
		}
	}

	static class Octazooka extends Attack {
		private static final long serialVersionUID = 1L;

		Octazooka() {
			super(AttackNamesies.OCTAZOOKA, "The user attacks by spraying ink in the target's face or eyes. It may also lower the target's accuracy.", 10, Type.WATER, MoveCategory.SPECIAL);
			super.power = 65;
			super.accuracy = 85;
			super.effectChance = 50;
			super.moveTypes.add(MoveType.BOMB_BALL);
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	static class Present extends Attack {
		private static final long serialVersionUID = 1L;

		Present() {
			super(AttackNamesies.PRESENT, "The user attacks by giving the target a gift with a hidden trap. It restores HP sometimes, however.", 15, Type.NORMAL, MoveCategory.PHYSICAL);
			super.accuracy = 90;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			double random = RandomUtils.getRandomInt(80);
			if (random < 40) {
				return 40;
			}
			else if (random < 70) {
				return 80;
			}
			else {
				return 120;
			}
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			if (!effective(b, me, o)) {
				return;
			}
			
			if (RandomUtils.chanceTest(80)) {
				super.applyDamage(me, o, b);
				return;
			}
			
			if (o.fullHealth() || o.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			o.healHealthFraction(1/4.0);
			Messages.add(new MessageUpdate(o.getName() + "'s health was restored!").updatePokemon(b, o));
		}
	}

	static class SteelWing extends Attack {
		private static final long serialVersionUID = 1L;

		SteelWing() {
			super(AttackNamesies.STEEL_WING, "The target is hit with wings of steel. It may also raise the user's Defense stat.", 25, Type.STEEL, MoveCategory.PHYSICAL);
			super.power = 70;
			super.accuracy = 90;
			super.effectChance = 10;
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Sketch extends Attack {
		private static final long serialVersionUID = 1L;

		Sketch() {
			super(AttackNamesies.SKETCH, "It enables the user to permanently learn the move last used by the target. Once used, Sketch disappears.", 1, Type.NORMAL, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.ENCORELESS);
			super.moveTypes.add(MoveType.MIMICLESS);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			Move copy = victim.getAttributes().getLastMoveUsed();
			if (copy == null || copy.getAttack().namesies() == AttackNamesies.STRUGGLE || user.hasEffect(EffectNamesies.TRANSFORMED)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			List<Move> moves = user.getMoves(b);
			for (int i = 0; i < moves.size(); i++) {
				if (moves.get(i).getAttack().namesies() == super.namesies) {
					moves.add(i, new Move(copy.getAttack()));
					moves.remove(i + 1);
					Messages.add(new MessageUpdate(user.getName() + " learned " + moves.get(i).getAttack().getName() + "!"));
					break;
				}
			}
		}
	}

	static class TripleKick extends Attack implements MultiStrikeMove {
		private static final long serialVersionUID = 1L;

		TripleKick() {
			super(AttackNamesies.TRIPLE_KICK, "A consecutive three-kick attack that becomes more powerful with each successive hit.", 10, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 20;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int hits = this.getNumHits(me);
			
			int hit = 1;
			for (; hit <= hits; hit++) {
				Messages.add(new MessageUpdate("Hit " + hit + "!"));
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b)) {
					break;
				}
			}
			
			hit--;
			
			// Print hits and gtfo
			Messages.add(new MessageUpdate("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!"));
		}

		public int getMinHits() {
			return 3;
		}

		public int getMaxHits() {
			return 3;
		}
	}

	static class MilkDrink extends Attack implements SelfHealingMove {
		private static final long serialVersionUID = 1L;

		MilkDrink() {
			super(AttackNamesies.MILK_DRINK, "The user restores its own HP by up to half of its maximum HP. May also be used in the field to heal HP.", 10, Type.NORMAL, MoveCategory.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b) {
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			victim.healHealthFraction(1/2.0);
			
			Messages.add(new MessageUpdate(victim.getName() + "'s health was restored!").updatePokemon(b, victim));
		}
	}

	static class HealBell extends Attack {
		private static final long serialVersionUID = 1L;

		HealBell() {
			super(AttackNamesies.HEAL_BELL, "The user makes a soothing bell chime to heal the status problems of all the party Pok\u00e9mon.", 5, Type.NORMAL, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.selfTarget = true;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			for (ActivePokemon p : b.getTrainer(user.isPlayer()).getTeam()) {
				if (!p.hasStatus(StatusCondition.FAINTED)) {
					p.removeStatus();
				}
			}
			
			Messages.add(new MessageUpdate("All status problems were cured!"));
		}
	}

	static class WeatherBall extends Attack {
		private static final long serialVersionUID = 1L;

		WeatherBall() {
			super(AttackNamesies.WEATHER_BALL, "An attack move that varies in power and type depending on the weather.", 10, Type.NORMAL, MoveCategory.SPECIAL);
			super.power = 50;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.BOMB_BALL);
		}

		public Type setType(Battle b, ActivePokemon user) {
			return b.getWeather().getElement();
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(b.getWeather().namesies() != EffectNamesies.CLEAR_SKIES ? 2 : 1);
		}
	}

	static class Aeroblast extends Attack implements CritStageEffect {
		private static final long serialVersionUID = 1L;

		Aeroblast() {
			super(AttackNamesies.AEROBLAST, "A vortex of air is shot at the target to inflict damage. Critical hits land more easily.", 5, Type.FLYING, MoveCategory.SPECIAL);
			super.power = 100;
			super.accuracy = 95;
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	static class SacredFire extends Attack {
		private static final long serialVersionUID = 1L;

		SacredFire() {
			super(AttackNamesies.SACRED_FIRE, "The target is razed with a mystical fire of great intensity. It may also leave the target with a burn.", 5, Type.FIRE, MoveCategory.PHYSICAL);
			super.power = 100;
			super.accuracy = 95;
			super.effectChance = 50;
			super.status = StatusCondition.BURNED;
			super.moveTypes.add(MoveType.DEFROST);
		}
	}

	static class HealBlock extends Attack {
		private static final long serialVersionUID = 1L;

		HealBlock() {
			super(AttackNamesies.HEAL_BLOCK, "For five turns, the user prevents the opposing team from using any moves, Abilities, or held items that recover HP.", 15, Type.PSYCHIC, MoveCategory.STATUS);
			super.accuracy = 100;
			super.effects.add(EffectNamesies.HEAL_BLOCK);
		}
	}

	static class EnergyBall extends Attack {
		private static final long serialVersionUID = 1L;

		EnergyBall() {
			super(AttackNamesies.ENERGY_BALL, "The user draws power from nature and fires it at the target. It may also lower the target's Sp. Def.", 10, Type.GRASS, MoveCategory.SPECIAL);
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
			super(AttackNamesies.BULK_UP, "The user tenses its muscles to bulk up its body, boosting both its Attack and Defense stats.", 20, Type.FIGHTING, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.DEFENSE.index()] = 1;
		}
	}

	static class Thief extends Attack implements ItemHolder {
		private static final long serialVersionUID = 1L;
		private Item item;

		Thief() {
			super(AttackNamesies.THIEF, "The user attacks and steals the target's held item simultaneously. It can't steal if the user holds an item.", 25, Type.DARK, MoveCategory.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.CHANGE_ITEM);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		private String getSwitchMessage(ActivePokemon user, Item userItem, ActivePokemon victim, Item victimItem) {
			return user.getName() + " stole " + victim.getName() + "'s " + victimItem.getName() + "!";
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isHoldingItem(b) || !victim.isHoldingItem(b) || b.getTrainer(user.isPlayer()) instanceof WildPokemon || victim.hasAbility(AbilityNamesies.STICKY_HOLD)) {
				if (super.category == MoveCategory.STATUS) {
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				}
				
				return;
			}
			
			Item userItem = user.getHeldItem(b), victimItem = victim.getHeldItem(b);
			Messages.add(new MessageUpdate(getSwitchMessage(user, userItem, victim, victimItem)));
			
			if (b.isWildBattle()) {
				user.giveItem((HoldItem)victimItem);
				victim.giveItem((HoldItem)userItem);
				return;
			}
			
			item = userItem;
			super.applyEffects(b, user, victim);
			
			item = victimItem;
			super.applyEffects(b, user, user);
		}

		public Item getItem() {
			return item;
		}
	}

	static class Attract extends Attack {
		private static final long serialVersionUID = 1L;

		Attract() {
			super(AttackNamesies.ATTRACT, "If it is the opposite gender of the user, the target becomes infatuated and less likely to attack.", 15, Type.NORMAL, MoveCategory.STATUS);
			super.accuracy = 100;
			super.effects.add(EffectNamesies.INFATUATED);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
		}
	}

	static class ForcePalm extends Attack {
		private static final long serialVersionUID = 1L;

		ForcePalm() {
			super(AttackNamesies.FORCE_PALM, "The target is attacked with a shock wave. It may also leave the target with paralysis.", 10, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class ArmThrust extends Attack implements MultiStrikeMove {
		private static final long serialVersionUID = 1L;

		ArmThrust() {
			super(AttackNamesies.ARM_THRUST, "The user looses a flurry of open-palmed arm thrusts that hit two to five times in a row.", 20, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 15;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int hits = this.getNumHits(me);
			
			int hit = 1;
			for (; hit <= hits; hit++) {
				Messages.add(new MessageUpdate("Hit " + hit + "!"));
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b)) {
					break;
				}
			}
			
			hit--;
			
			// Print hits and gtfo
			Messages.add(new MessageUpdate("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!"));
		}

		public int getMinHits() {
			return 2;
		}

		public int getMaxHits() {
			return 5;
		}
	}

	static class SmellingSalts extends Attack {
		private static final long serialVersionUID = 1L;

		SmellingSalts() {
			super(AttackNamesies.SMELLING_SALTS, "This attack inflicts double damage on a target with paralysis. It also cures the target's paralysis, however.", 10, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(o.hasStatus(StatusCondition.PARALYZED) ? 2 : 1);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (victim.hasStatus(StatusCondition.PARALYZED)) {
				Status.removeStatus(b, victim, CastSource.ATTACK);
			}
		}
	}

	static class Assist extends Attack {
		private static final long serialVersionUID = 1L;

		Assist() {
			super(AttackNamesies.ASSIST, "The user hurriedly and randomly uses a move among those known by other Pok\u00e9mon in the party.", 20, Type.NORMAL, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.ASSISTLESS);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			List<Attack> attacks = new ArrayList<>();
			for (ActivePokemon p : b.getTrainer(me.isPlayer()).getTeam()) {
				if (p == me) {
					continue;
				}
				
				for (Move move : p.getMoves(b)) {
					if (!move.getAttack().isMoveType(MoveType.ASSISTLESS)) {
						attacks.add(move.getAttack());
					}
				}
			}
			
			if (attacks.isEmpty()) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			me.callNewMove(b, o, new Move(RandomUtils.getRandomValue(attacks)));
		}
	}

	static class MetalBurst extends Attack {
		private static final long serialVersionUID = 1L;

		MetalBurst() {
			super(AttackNamesies.METAL_BURST, "The user retaliates with much greater power against the target that last inflicted damage on it.", 10, Type.STEEL, MoveCategory.PHYSICAL);
			super.accuracy = 100;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int damageTaken = me.getAttributes().getDamageTaken();
			if (damageTaken == 0 || b.isFirstAttack()) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			o.reduceHealth(b, (int)(damageTaken*1.5));
		}
	}

	static class WildCharge extends Attack implements RecoilMove {
		private static final long serialVersionUID = 1L;

		WildCharge() {
			super(AttackNamesies.WILD_CHARGE, "The user shrouds itself in electricity and smashes into its target. It also damages the user a little.", 15, Type.ELECTRIC, MoveCategory.PHYSICAL);
			super.power = 90;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyRecoil(Battle b, ActivePokemon user, int damage) {
			if (user.hasAbility(AbilityNamesies.ROCK_HEAD) || user.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(new MessageUpdate(user.getName() + " was hurt by recoil!"));
			user.reduceHealth(b, (int)Math.ceil(damage/4.0), false);
		}
	}

	static class Flash extends Attack {
		private static final long serialVersionUID = 1L;

		Flash() {
			super(AttackNamesies.FLASH, "The user flashes a bright light that cuts the target's accuracy. It can also be used to illuminate caves.", 20, Type.NORMAL, MoveCategory.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	static class TailGlow extends Attack {
		private static final long serialVersionUID = 1L;

		TailGlow() {
			super(AttackNamesies.TAIL_GLOW, "The user stares at flashing lights to focus its mind, drastically raising its Sp. Atk stat.", 20, Type.BUG, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = 3;
		}
	}

	static class WaterSpout extends Attack {
		private static final long serialVersionUID = 1L;

		WaterSpout() {
			super(AttackNamesies.WATER_SPOUT, "The user spouts water to damage the opposing team. The lower the user's HP, the less powerful it becomes.", 5, Type.WATER, MoveCategory.SPECIAL);
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return (int)Math.min(1, (150*me.getHPRatio()));
		}
	}

	static class TeeterDance extends Attack {
		private static final long serialVersionUID = 1L;

		TeeterDance() {
			super(AttackNamesies.TEETER_DANCE, "The user performs a wobbly dance that confuses the Pok\u00e9mon around it.", 20, Type.NORMAL, MoveCategory.STATUS);
			super.accuracy = 100;
			super.effects.add(EffectNamesies.CONFUSION);
		}
	}

	static class NeedleArm extends Attack {
		private static final long serialVersionUID = 1L;

		NeedleArm() {
			super(AttackNamesies.NEEDLE_ARM, "The user attacks by wildly swinging its thorny arms. It may also make the target flinch.", 15, Type.GRASS, MoveCategory.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.FLINCH);
			super.effectChance = 30;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Venoshock extends Attack {
		private static final long serialVersionUID = 1L;

		Venoshock() {
			super(AttackNamesies.VENOSHOCK, "The user drenches the target in a special poisonous liquid. Its power is doubled if the target is poisoned.", 10, Type.POISON, MoveCategory.SPECIAL);
			super.power = 65;
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(o.hasStatus(StatusCondition.POISONED) ? 2 : 1);
		}
	}

	static class Snatch extends Attack {
		private static final long serialVersionUID = 1L;

		Snatch() {
			super(AttackNamesies.SNATCH, "The user steals the effects of any healing or stat-changing move the opponent attempts to use.", 10, Type.DARK, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.SNATCH);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
			super.priority = 4;
		}
	}

	static class IceBall extends Attack {
		private static final long serialVersionUID = 1L;

		IceBall() {
			super(AttackNamesies.ICE_BALL, "The user continually rolls into the target over five turns. It becomes stronger each time it hits.", 20, Type.ICE, MoveCategory.PHYSICAL);
			super.power = 30;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.BOMB_BALL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(int)Math.min(me.getAttributes().getCount(), 5)*(me.hasEffect(EffectNamesies.USED_DEFENSE_CURL) ? 2 : 1);
		}
	}

	static class HeadSmash extends Attack implements RecoilMove {
		private static final long serialVersionUID = 1L;

		HeadSmash() {
			super(AttackNamesies.HEAD_SMASH, "The user attacks the target with a hazardous, full-power headbutt. The user also takes terrible damage.", 5, Type.ROCK, MoveCategory.PHYSICAL);
			super.power = 150;
			super.accuracy = 80;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyRecoil(Battle b, ActivePokemon user, int damage) {
			if (user.hasAbility(AbilityNamesies.ROCK_HEAD) || user.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(new MessageUpdate(user.getName() + " was hurt by recoil!"));
			user.reduceHealth(b, (int)Math.ceil(damage/2.0), false);
		}
	}

	static class MistBall extends Attack {
		private static final long serialVersionUID = 1L;

		MistBall() {
			super(AttackNamesies.MIST_BALL, "A mistlike flurry of down envelops and damages the target. It may also lower the target's Sp. Atk.", 5, Type.PSYCHIC, MoveCategory.SPECIAL);
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
			super(AttackNamesies.LUSTER_PURGE, "The user lets loose a damaging burst of light. It may also reduce the target's Sp. Def stat.", 5, Type.PSYCHIC, MoveCategory.SPECIAL);
			super.power = 70;
			super.accuracy = 100;
			super.effectChance = 50;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
		}
	}

	static class PsychoBoost extends Attack {
		private static final long serialVersionUID = 1L;

		PsychoBoost() {
			super(AttackNamesies.PSYCHO_BOOST, "The user attacks the target at full power. The attack's recoil harshly reduces the user's Sp. Atk stat.", 5, Type.PSYCHIC, MoveCategory.SPECIAL);
			super.power = 140;
			super.accuracy = 90;
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = -2;
		}
	}

	static class Facade extends Attack {
		private static final long serialVersionUID = 1L;

		Facade() {
			super(AttackNamesies.FACADE, "An attack move that doubles its power if the user is poisoned, burned, or has paralysis.", 20, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void startTurn(Battle b, ActivePokemon me) {
			// TODO: Should not take the attack reduction from burn
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(me.hasStatus() ? 2 : 1);
		}
	}

	static class DefendOrder extends Attack {
		private static final long serialVersionUID = 1L;

		DefendOrder() {
			super(AttackNamesies.DEFEND_ORDER, "The user calls out its underlings to shield its body, raising its Defense and Sp. Def stats.", 10, Type.BUG, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
		}
	}

	static class HealOrder extends Attack implements SelfHealingMove {
		private static final long serialVersionUID = 1L;

		HealOrder() {
			super(AttackNamesies.HEAL_ORDER, "The user calls out its underlings to heal it. The user regains up to half of its max HP.", 10, Type.BUG, MoveCategory.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b) {
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			victim.healHealthFraction(1/2.0);
			
			Messages.add(new MessageUpdate(victim.getName() + "'s health was restored!").updatePokemon(b, victim));
		}
	}

	static class AttackOrder extends Attack implements CritStageEffect {
		private static final long serialVersionUID = 1L;

		AttackOrder() {
			super(AttackNamesies.ATTACK_ORDER, "The user calls out its underlings to pummel the target. Critical hits land more easily.", 15, Type.BUG, MoveCategory.PHYSICAL);
			super.power = 90;
			super.accuracy = 100;
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	static class Chatter extends Attack {
		private static final long serialVersionUID = 1L;

		Chatter() {
			super(AttackNamesies.CHATTER, "The user attacks using a sound wave based on words it has learned. It may also confuse the target.", 20, Type.FLYING, MoveCategory.SPECIAL);
			super.power = 65;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.CONFUSION);
			super.moveTypes.add(MoveType.SOUND_BASED);
		}
	}

	static class DualChop extends Attack implements MultiStrikeMove {
		private static final long serialVersionUID = 1L;

		DualChop() {
			super(AttackNamesies.DUAL_CHOP, "The user attacks its target by hitting it with brutal strikes. The target is hit twice in a row.", 15, Type.DRAGON, MoveCategory.PHYSICAL);
			super.power = 40;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int hits = this.getNumHits(me);
			
			int hit = 1;
			for (; hit <= hits; hit++) {
				Messages.add(new MessageUpdate("Hit " + hit + "!"));
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b)) {
					break;
				}
			}
			
			hit--;
			
			// Print hits and gtfo
			Messages.add(new MessageUpdate("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!"));
		}

		public int getMinHits() {
			return 2;
		}

		public int getMaxHits() {
			return 2;
		}
	}

	static class RockWrecker extends Attack implements MultiTurnMove {
		private static final long serialVersionUID = 1L;

		RockWrecker() {
			super(AttackNamesies.ROCK_WRECKER, "The user launches a huge boulder at the target to attack. It must rest on the next turn, however.", 5, Type.ROCK, MoveCategory.PHYSICAL);
			super.power = 150;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.BOMB_BALL);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
		}

		public boolean chargesFirst() {
			return false;
		}

		public boolean semiInvulnerability() {
			return false;
		}

		public void charge(ActivePokemon user, Battle b) {
			Messages.add(new MessageUpdate(getChargeMessage(user)));
		}

		private String getChargeMessage(ActivePokemon user) {
			return user.getName() + " must recharge!";
		}
	}

	static class TrickRoom extends Attack {
		private static final long serialVersionUID = 1L;

		TrickRoom() {
			super(AttackNamesies.TRICK_ROOM, "The user creates a bizarre area in which slower Pok\u00e9mon get to move first for five turns.", 5, Type.PSYCHIC, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.TRICK_ROOM);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
			super.priority = -7;
		}
	}

	static class RoarOfTime extends Attack implements MultiTurnMove {
		private static final long serialVersionUID = 1L;

		RoarOfTime() {
			super(AttackNamesies.ROAR_OF_TIME, "The user blasts the target with power that distorts even time. The user must rest on the next turn.", 5, Type.DRAGON, MoveCategory.SPECIAL);
			super.power = 150;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
		}

		public boolean chargesFirst() {
			return false;
		}

		public boolean semiInvulnerability() {
			return false;
		}

		public void charge(ActivePokemon user, Battle b) {
			Messages.add(new MessageUpdate(getChargeMessage(user)));
		}

		private String getChargeMessage(ActivePokemon user) {
			return user.getName() + " must recharge!";
		}
	}

	static class SpacialRend extends Attack implements CritStageEffect {
		private static final long serialVersionUID = 1L;

		SpacialRend() {
			super(AttackNamesies.SPACIAL_REND, "The user tears the target along with the space around it. Critical hits land more easily.", 5, Type.DRAGON, MoveCategory.SPECIAL);
			super.power = 100;
			super.accuracy = 95;
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	static class MagmaStorm extends Attack {
		private static final long serialVersionUID = 1L;

		MagmaStorm() {
			super(AttackNamesies.MAGMA_STORM, "The target becomes trapped within a maelstrom of fire that rages for four to five turns.", 5, Type.FIRE, MoveCategory.SPECIAL);
			super.power = 100;
			super.accuracy = 75;
			super.effects.add(EffectNamesies.MAGMA_STORM);
		}
	}

	static class CrushGrip extends Attack {
		private static final long serialVersionUID = 1L;

		CrushGrip() {
			super(AttackNamesies.CRUSH_GRIP, "The target is crushed with great force. The attack is more powerful the more HP the target has left.", 5, Type.NORMAL, MoveCategory.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return (int)Math.min(1, (120*o.getHPRatio()));
		}
	}

	static class ShadowForce extends Attack implements MultiTurnMove {
		private static final long serialVersionUID = 1L;

		ShadowForce() {
			super(AttackNamesies.SHADOW_FORCE, "The user disappears, then strikes the target on the second turn. It hits even if the target protects itself.", 5, Type.GHOST, MoveCategory.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public boolean chargesFirst() {
			return true;
		}

		public boolean semiInvulnerability() {
			return true;
		}

		public void charge(ActivePokemon user, Battle b) {
			Messages.add(new MessageUpdate(getChargeMessage(user)));
		}

		private String getChargeMessage(ActivePokemon user) {
			return user.getName() + " disappeared!";
		}
	}

	static class HeartSwap extends Attack {
		private static final long serialVersionUID = 1L;

		HeartSwap() {
			super(AttackNamesies.HEART_SWAP, "The user employs its psychic power to switch stat changes with the target.", 10, Type.PSYCHIC, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			for (int i = 0; i < Stat.NUM_BATTLE_STATS; i++)
			{
				int temp = user.getAttributes().getStage(i);
				user.getAttributes().setStage(i, victim.getAttributes().getStage(i));
				victim.getAttributes().setStage(i, temp);
			}
			
			Messages.add(new MessageUpdate(user.getName() + " swapped its stats with " + victim.getName() + "!"));
		}
	}

	static class DarkVoid extends Attack {
		private static final long serialVersionUID = 1L;

		DarkVoid() {
			super(AttackNamesies.DARK_VOID, "Opposing Pok\u00e9mon are dragged into a world of total darkness that makes them sleep.", 10, Type.DARK, MoveCategory.STATUS);
			super.accuracy = 80;
			super.status = StatusCondition.ASLEEP;
		}
	}

	static class SeedFlare extends Attack {
		private static final long serialVersionUID = 1L;

		SeedFlare() {
			super(AttackNamesies.SEED_FLARE, "The user emits a shock wave from its body to attack its target. It may harshly lower the target's Sp. Def.", 5, Type.GRASS, MoveCategory.SPECIAL);
			super.power = 120;
			super.accuracy = 85;
			super.effectChance = 40;
			super.statChanges[Stat.SP_DEFENSE.index()] = -2;
		}
	}

	static class Judgement extends Attack {
		private static final long serialVersionUID = 1L;

		Judgement() {
			super(AttackNamesies.JUDGEMENT, "The user releases countless shots of light at the target. Its type varies with the kind of Plate the user is holding.", 10, Type.NORMAL, MoveCategory.SPECIAL);
			super.power = 100;
			super.accuracy = 100;
		}

		public Type setType(Battle b, ActivePokemon user) {
			Item i = user.getHeldItem(b);
			if (i instanceof PlateItem) {
				return ((PlateItem)i).getType();
			}
			
			return super.type;
		}
	}

	static class SearingShot extends Attack {
		private static final long serialVersionUID = 1L;

		SearingShot() {
			super(AttackNamesies.SEARING_SHOT, "An inferno of scarlet flames torches everything around the user. It may leave targets with a burn.", 5, Type.FIRE, MoveCategory.SPECIAL);
			super.power = 100;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.BURNED;
			super.moveTypes.add(MoveType.BOMB_BALL);
		}
	}

	static class Incinerate extends Attack {
		private static final long serialVersionUID = 1L;

		Incinerate() {
			super(AttackNamesies.INCINERATE, "The user attacks the target with fire. If the target is holding a Berry, the Berry becomes burnt up and unusable.", 15, Type.FIRE, MoveCategory.SPECIAL);
			super.power = 60;
			super.accuracy = 100;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			Item heldItem = victim.getHeldItem(b);
			
			if (heldItem instanceof Berry || heldItem instanceof GemItem) {
				Messages.add(new MessageUpdate(victim.getName() + "'s " + heldItem.getName() + " was burned!"));
				victim.consumeItem(b);
			}
		}
	}

	static class Overheat extends Attack {
		private static final long serialVersionUID = 1L;

		Overheat() {
			super(AttackNamesies.OVERHEAT, "The user attacks the target at full power. The attack's recoil harshly reduces the user's Sp. Atk stat.", 5, Type.FIRE, MoveCategory.SPECIAL);
			super.power = 130;
			super.accuracy = 90;
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = -2;
		}
	}

	static class HeatCrash extends Attack {
		private static final long serialVersionUID = 1L;

		HeatCrash() {
			super(AttackNamesies.HEAT_CRASH, "The user slams its target with its flame- covered body. The more the user outweighs the target, the greater the damage.", 10, Type.FIRE, MoveCategory.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			double ratio = o.getWeight(b)/me.getWeight(b);
			if (ratio > .5) return 40;
			if (ratio > .33) return 60;
			if (ratio > .25) return 80;
			if (ratio > .2) return 100;
			return 120;
		}
	}

	static class GrassKnot extends Attack {
		private static final long serialVersionUID = 1L;

		GrassKnot() {
			super(AttackNamesies.GRASS_KNOT, "The user snares the target with grass and trips it. The heavier the target, the greater the damage.", 20, Type.GRASS, MoveCategory.SPECIAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			double weight = o.getWeight(b);
			if (weight < 22) return 20;
			if (weight < 55) return 40;
			if (weight < 110) return 60;
			if (weight < 220) return 80;
			if (weight < 440) return 100;
			return 120;
		}
	}

	static class Scald extends Attack {
		private static final long serialVersionUID = 1L;

		Scald() {
			super(AttackNamesies.SCALD, "The user shoots boiling hot water at its target. It may also leave the target with a burn.", 15, Type.WATER, MoveCategory.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.effectChance = 30;
			super.status = StatusCondition.BURNED;
			super.moveTypes.add(MoveType.DEFROST);
		}
	}

	static class DrainPunch extends Attack implements SapHealthEffect {
		private static final long serialVersionUID = 1L;

		DrainPunch() {
			super(AttackNamesies.DRAIN_PUNCH, "An energy-draining punch. The user's HP is restored by half the damage taken by the target.", 10, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 75;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PUNCHING);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class StormThrow extends Attack implements AlwaysCritEffect {
		private static final long serialVersionUID = 1L;

		StormThrow() {
			super(AttackNamesies.STORM_THROW, "The user strikes the target with a fierce blow. This attack always results in a critical hit.", 10, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 60;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class FrostBreath extends Attack implements AlwaysCritEffect {
		private static final long serialVersionUID = 1L;

		FrostBreath() {
			super(AttackNamesies.FROST_BREATH, "The user blows a cold breath on the target. This attack always results in a critical hit.", 10, Type.ICE, MoveCategory.SPECIAL);
			super.power = 60;
			super.accuracy = 90;
		}
	}

	static class RockSmash extends Attack {
		private static final long serialVersionUID = 1L;

		RockSmash() {
			super(AttackNamesies.ROCK_SMASH, "The user attacks with a punch that can shatter a rock. It may also lower the target's Defense stat.", 15, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.effectChance = 50;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class RockClimb extends Attack {
		private static final long serialVersionUID = 1L;

		RockClimb() {
			super(AttackNamesies.ROCK_CLIMB, "The user attacks the target by smashing into it with incredible force. It may also confuse the target.", 20, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 90;
			super.accuracy = 85;
			super.effects.add(EffectNamesies.CONFUSION);
			super.effectChance = 20;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class NightDaze extends Attack {
		private static final long serialVersionUID = 1L;

		NightDaze() {
			super(AttackNamesies.NIGHT_DAZE, "The user lets loose a pitch-black shock wave at its target. It may also lower the target's accuracy.", 10, Type.DARK, MoveCategory.SPECIAL);
			super.power = 85;
			super.accuracy = 95;
			super.effectChance = 40;
			super.statChanges[Stat.ACCURACY.index()] = -1;
		}
	}

	static class TailSlap extends Attack implements MultiStrikeMove {
		private static final long serialVersionUID = 1L;

		TailSlap() {
			super(AttackNamesies.TAIL_SLAP, "The user attacks by striking the target with its hard tail. It hits the target two to five times in a row.", 10, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 25;
			super.accuracy = 85;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int hits = this.getNumHits(me);
			
			int hit = 1;
			for (; hit <= hits; hit++) {
				Messages.add(new MessageUpdate("Hit " + hit + "!"));
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b)) {
					break;
				}
			}
			
			hit--;
			
			// Print hits and gtfo
			Messages.add(new MessageUpdate("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!"));
		}

		public int getMinHits() {
			return 2;
		}

		public int getMaxHits() {
			return 5;
		}
	}

	static class Defog extends Attack {
		private static final long serialVersionUID = 1L;

		Defog() {
			super(AttackNamesies.DEFOG, "A strong wind blows away the target's obstacles such as Reflect or Light Screen. It also lowers the target's evasiveness.", 15, Type.FLYING, MoveCategory.STATUS);
			super.statChanges[Stat.EVASION.index()] = -1;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			super.applyEffects(b, user, victim);
			DefogRelease.release(b, victim);
		}
	}

	static class HornLeech extends Attack implements SapHealthEffect {
		private static final long serialVersionUID = 1L;

		HornLeech() {
			super(AttackNamesies.HORN_LEECH, "The user drains the target's energy with its horns. The user's HP is restored by half the damage taken by the target.", 10, Type.GRASS, MoveCategory.PHYSICAL);
			super.power = 75;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Electroweb extends Attack {
		private static final long serialVersionUID = 1L;

		Electroweb() {
			super(AttackNamesies.ELECTROWEB, "The user captures and attacks opposing Pok\u00e9mon by using an electric net. It reduces the targets' Speed stat.", 15, Type.ELECTRIC, MoveCategory.SPECIAL);
			super.power = 55;
			super.accuracy = 95;
			super.statChanges[Stat.SPEED.index()] = -1;
		}
	}

	static class GearGrind extends Attack implements MultiStrikeMove {
		private static final long serialVersionUID = 1L;

		GearGrind() {
			super(AttackNamesies.GEAR_GRIND, "The user attacks by throwing two steel gears at its target.", 15, Type.STEEL, MoveCategory.PHYSICAL);
			super.power = 50;
			super.accuracy = 85;
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int hits = this.getNumHits(me);
			
			int hit = 1;
			for (; hit <= hits; hit++) {
				Messages.add(new MessageUpdate("Hit " + hit + "!"));
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b)) {
					break;
				}
			}
			
			hit--;
			
			// Print hits and gtfo
			Messages.add(new MessageUpdate("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!"));
		}

		public int getMinHits() {
			return 2;
		}

		public int getMaxHits() {
			return 2;
		}
	}

	static class ShiftGear extends Attack {
		private static final long serialVersionUID = 1L;

		ShiftGear() {
			super(AttackNamesies.SHIFT_GEAR, "The user rotates its gears, raising its Attack and sharply raising its Speed.", 10, Type.STEEL, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.SPEED.index()] = 2;
		}
	}

	static class HeadCharge extends Attack implements RecoilMove {
		private static final long serialVersionUID = 1L;

		HeadCharge() {
			super(AttackNamesies.HEAD_CHARGE, "The user charges its head into its target, using its powerful guard hair. It also damages the user a little.", 15, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyRecoil(Battle b, ActivePokemon user, int damage) {
			if (user.hasAbility(AbilityNamesies.ROCK_HEAD) || user.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(new MessageUpdate(user.getName() + " was hurt by recoil!"));
			user.reduceHealth(b, (int)Math.ceil(damage/4.0), false);
		}
	}

	static class FieryDance extends Attack {
		private static final long serialVersionUID = 1L;

		FieryDance() {
			super(AttackNamesies.FIERY_DANCE, "Cloaked in flames, the user dances and flaps its wings. It may also raise the user's Sp. Atk stat.", 10, Type.FIRE, MoveCategory.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
			super.effectChance = 50;
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
		}
	}

	static class SacredSword extends Attack implements OpponentIgnoreStageEffect {
		private static final long serialVersionUID = 1L;

		SacredSword() {
			super(AttackNamesies.SACRED_SWORD, "The user attacks by slicing with its long horns. The target's stat changes don't affect this attack's damage.", 20, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 90;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public boolean ignoreStage(Stat s) {
			return !s.user();
		}
	}

	static class SecretSword extends Attack implements OpponentStatSwitchingEffect {
		private static final long serialVersionUID = 1L;

		SecretSword() {
			super(AttackNamesies.SECRET_SWORD, "The user cuts with its long horn. The odd power contained in the horn does physical damage to the target.", 10, Type.FIGHTING, MoveCategory.SPECIAL);
			super.power = 85;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.METRONOMELESS);
		}

		public Stat switchStat(Stat s) {
			// TODO: Can combine this with Psystrike
			return s == Stat.SP_DEFENSE ? Stat.DEFENSE : s;
		}
	}

	static class FusionFlare extends Attack {
		private static final long serialVersionUID = 1L;

		FusionFlare() {
			super(AttackNamesies.FUSION_FLARE, "The user brings down a giant flame. This attack does greater damage when influenced by an enormous thunderbolt.", 5, Type.FIRE, MoveCategory.SPECIAL);
			super.power = 100;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.DEFROST);
		}

		public void startTurn(Battle b, ActivePokemon me) {
			// TODO: Can combine power condition with Fusion Bolt
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(!b.isFirstAttack() && o.getAttack().namesies() == AttackNamesies.FUSION_BOLT ? 2 : 1);
		}
	}

	static class FusionBolt extends Attack {
		private static final long serialVersionUID = 1L;

		FusionBolt() {
			super(AttackNamesies.FUSION_BOLT, "The user throws down a giant thunderbolt. This attack does greater damage when influenced by an enormous flame.", 5, Type.ELECTRIC, MoveCategory.PHYSICAL);
			super.power = 100;
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(!b.isFirstAttack() && o.getAttack().namesies() == AttackNamesies.FUSION_FLARE ? 2 : 1);
		}
	}

	static class BlueFlare extends Attack {
		private static final long serialVersionUID = 1L;

		BlueFlare() {
			super(AttackNamesies.BLUE_FLARE, "The user attacks by engulfing the target in an intense, yet beautiful, blue flame. It may leave the target with a burn.", 5, Type.FIRE, MoveCategory.SPECIAL);
			super.power = 130;
			super.accuracy = 85;
			super.effectChance = 20;
			super.status = StatusCondition.BURNED;
		}
	}

	static class BoltStrike extends Attack {
		private static final long serialVersionUID = 1L;

		BoltStrike() {
			super(AttackNamesies.BOLT_STRIKE, "The user charges its target, surrounding itself with a great amount of electricity. It may leave the target with paralysis.", 5, Type.ELECTRIC, MoveCategory.PHYSICAL);
			super.power = 130;
			super.accuracy = 85;
			super.effectChance = 20;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Glaciate extends Attack {
		private static final long serialVersionUID = 1L;

		Glaciate() {
			super(AttackNamesies.GLACIATE, "The user attacks by blowing freezing cold air at opposing Pok\u00e9mon. This attack reduces the targets' Speed stat.", 10, Type.ICE, MoveCategory.SPECIAL);
			super.power = 65;
			super.accuracy = 95;
			super.statChanges[Stat.SPEED.index()] = -1;
		}
	}

	static class TechnoBlast extends Attack {
		private static final long serialVersionUID = 1L;

		TechnoBlast() {
			super(AttackNamesies.TECHNO_BLAST, "The user fires a beam of light at its target. The type changes depending on the Drive the user holds.", 5, Type.NORMAL, MoveCategory.SPECIAL);
			super.power = 120;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.METRONOMELESS);
		}

		public Type setType(Battle b, ActivePokemon user) {
			// TODO: Can combine this with Judgement
			Item i = user.getHeldItem(b);
			if (i instanceof DriveItem) {
				return ((DriveItem)i).getType();
			}
			
			return super.type;
		}
	}

	static class Explosion extends Attack {
		private static final long serialVersionUID = 1L;

		Explosion() {
			super(AttackNamesies.EXPLOSION, "The user explodes to inflict damage on those around it. The user faints upon using this move.", 5, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 250;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.USER_FAINTS);
		}
	}

	static class SelfDestruct extends Attack {
		private static final long serialVersionUID = 1L;

		SelfDestruct() {
			super(AttackNamesies.SELF_DESTRUCT, "The user attacks everything around it by causing an explosion. The user faints upon using this move.", 5, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 200;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.USER_FAINTS);
		}
	}

	static class Fling extends Attack {
		private static final long serialVersionUID = 1L;

		Fling() {
			super(AttackNamesies.FLING, "The user flings its held item at the target to attack. Its power and effects depend on the item.", 10, Type.DARK, MoveCategory.PHYSICAL);
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			if (me.isHoldingItem(b)) {
				return ((HoldItem)me.getHeldItem(b)).flingDamage();
			}
			
			return super.power;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			if (!me.isHoldingItem(b)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			Messages.add(new MessageUpdate(me.getName() + " flung its " + me.getHeldItem(b).getName() + "!"));
			super.apply(me, o, b);
			me.consumeItem(b);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			((HoldItem)user.getHeldItem(b)).flingEffect(b, victim);
		}
	}

	static class FreezeShock extends Attack implements MultiTurnMove {
		private static final long serialVersionUID = 1L;

		FreezeShock() {
			super(AttackNamesies.FREEZE_SHOCK, "On the second turn, the user hits the target with electrically charged ice. It may leave the target with paralysis.", 5, Type.ICE, MoveCategory.PHYSICAL);
			super.power = 140;
			super.accuracy = 90;
			super.effectChance = 30;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
		}

		public boolean chargesFirst() {
			return true;
		}

		public boolean semiInvulnerability() {
			return false;
		}

		public void charge(ActivePokemon user, Battle b) {
			Messages.add(new MessageUpdate(getChargeMessage(user)));
		}

		private String getChargeMessage(ActivePokemon user) {
			return user.getName() + " is charging!";
		}
	}

	static class SecretPower extends Attack {
		private static final long serialVersionUID = 1L;

		SecretPower() {
			super(AttackNamesies.SECRET_POWER, "The user attacks the target with a secret power. Its added effects vary depending on the user's environment.", 20, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.effectChance = 30;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			TerrainType terrain = b.getTerrainType();
			
			super.status = terrain.getStatusCondition();
			super.statChanges = terrain.getStatChanges();
			super.effects = terrain.getEffects();
			
			super.applyEffects(b, user, victim);
			
			super.status = StatusCondition.NO_STATUS;
			super.statChanges = new int[Stat.NUM_BATTLE_STATS];
			super.effects = new ArrayList<>();
		}
	}

	static class FinalGambit extends Attack {
		private static final long serialVersionUID = 1L;

		FinalGambit() {
			super(AttackNamesies.FINAL_GAMBIT, "The user risks everything to attack its target. The user faints but does damage equal to the user's HP.", 5, Type.FIGHTING, MoveCategory.SPECIAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.USER_FAINTS);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			o.reduceHealth(b, me.getHP());
		}
	}

	static class GastroAcid extends Attack implements ChangeAbilityMove {
		private static final long serialVersionUID = 1L;

		GastroAcid() {
			super(AttackNamesies.GASTRO_ACID, "The user hurls up its stomach acids on the target. The fluid eliminates the effect of the target's Ability.", 10, Type.POISON, MoveCategory.STATUS);
			super.accuracy = 100;
			super.effects.add(EffectNamesies.CHANGE_ABILITY);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			switch (o.getAbility().namesies())
			{
				case MULTITYPE:
				case STANCE_CHANGE:
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
					return;
				default:
					super.apply(me, o, b);
				}
		}

		public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim) {
			return AbilityNamesies.NO_ABILITY.getNewAbility();
		}

		public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
			return caster.getName() + " suppressed " + victim.getName() + "'s ability!";
		}
	}

	static class HealingWish extends Attack {
		private static final long serialVersionUID = 1L;

		HealingWish() {
			super(AttackNamesies.HEALING_WISH, "The user faints. In return, the Pok\u00e9mon taking its place will have its HP restored and status cured.", 10, Type.PSYCHIC, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.HEAL_SWITCH);
			super.moveTypes.add(MoveType.USER_FAINTS);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}
	}

	static class LunarDance extends Attack {
		private static final long serialVersionUID = 1L;

		LunarDance() {
			super(AttackNamesies.LUNAR_DANCE, "The user faints. In return, the Pok\u00e9mon taking its place will have its status and HP fully restored.", 10, Type.PSYCHIC, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.HEAL_SWITCH);
			super.moveTypes.add(MoveType.USER_FAINTS);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}
	}

	static class Roar extends Attack {
		private static final long serialVersionUID = 1L;

		Roar() {
			super(AttackNamesies.ROAR, "The target is scared off and replaced by another Pok\u00e9mon in its party. In the wild, the battle ends.", 20, Type.NORMAL, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.priority = -6;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			// Fails against the Suction Cups ability
			if (victim.hasAbility(AbilityNamesies.SUCTION_CUPS) && !user.breaksTheMold()) {
				Messages.add(new MessageUpdate(victim.getName() + "'s " + AbilityNamesies.SUCTION_CUPS.getName() + " prevents it from switching!"));
				return;
			}
			
			// Fails if this is the first attack of the turn, or if the victim is rooted by Ingrain
			if (b.isFirstAttack() || victim.hasEffect(EffectNamesies.INGRAIN)) {
				if (super.category == MoveCategory.STATUS)  {
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				}
				
				return;
			}
			
			Team opponent = b.getTrainer(victim.isPlayer());
			if (opponent instanceof WildPokemon) {
				// Fails against wild Pokemon of higher levels
				if (victim.getLevel() > user.getLevel()) {
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
					return;
				}
				
				// End the battle against a wild Pokemon
				Messages.add(new MessageUpdate(victim.getName() + " fled in fear!"));
				Messages.add(new MessageUpdate().withUpdate(Update.EXIT_BATTLE));
				return;
			}
			
			Trainer trainer = (Trainer)opponent;
			if (!trainer.hasRemainingPokemon()) {
				// Fails against trainers on their last Pokemon
				if (super.category == MoveCategory.STATUS) {
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				}
				
				return;
			}
			
			// Swap to a random Pokemon!
			Messages.add(new MessageUpdate(victim.getName() + " fled in fear!"));
			trainer.switchToRandom();
			victim = trainer.front();
			b.enterBattle(victim, "...and " + victim.getName() + " was dragged out!");
		}
	}

	static class Grudge extends Attack {
		private static final long serialVersionUID = 1L;

		Grudge() {
			super(AttackNamesies.GRUDGE, "If the user faints, the user's grudge fully depletes the PP of the opponent's move that knocked it out.", 5, Type.GHOST, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.GRUDGE);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}
	}

	static class Retaliate extends Attack {
		private static final long serialVersionUID = 1L;

		Retaliate() {
			super(AttackNamesies.RETALIATE, "The user gets revenge for a fainted ally. If an ally fainted in the previous turn, this attack's damage increases.", 5, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(Effect.hasEffect(b.getEffects(me.isPlayer()), EffectNamesies.DEAD_ALLY) ? 2 : 1);
		}
	}

	static class CircleThrow extends Attack {
		private static final long serialVersionUID = 1L;

		CircleThrow() {
			super(AttackNamesies.CIRCLE_THROW, "The user throws the target and drags out another Pok\u00e9mon in its party. In the wild, the battle ends.", 10, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 60;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.priority = -6;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			// Fails against the Suction Cups ability
			if (victim.hasAbility(AbilityNamesies.SUCTION_CUPS) && !user.breaksTheMold()) {
				Messages.add(new MessageUpdate(victim.getName() + "'s " + AbilityNamesies.SUCTION_CUPS.getName() + " prevents it from switching!"));
				return;
			}
			
			// Fails if this is the first attack of the turn, or if the victim is rooted by Ingrain
			if (b.isFirstAttack() || victim.hasEffect(EffectNamesies.INGRAIN)) {
				if (super.category == MoveCategory.STATUS)  {
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				}
				
				return;
			}
			
			Team opponent = b.getTrainer(victim.isPlayer());
			if (opponent instanceof WildPokemon) {
				// Fails against wild Pokemon of higher levels
				if (victim.getLevel() > user.getLevel()) {
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
					return;
				}
				
				// End the battle against a wild Pokemon
				Messages.add(new MessageUpdate(victim.getName() + " was thrown away!"));
				Messages.add(new MessageUpdate().withUpdate(Update.EXIT_BATTLE));
				return;
			}
			
			Trainer trainer = (Trainer)opponent;
			if (!trainer.hasRemainingPokemon()) {
				// Fails against trainers on their last Pokemon
				if (super.category == MoveCategory.STATUS) {
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				}
				
				return;
			}
			
			// Swap to a random Pokemon!
			Messages.add(new MessageUpdate(victim.getName() + " was thrown away!"));
			trainer.switchToRandom();
			victim = trainer.front();
			b.enterBattle(victim, "...and " + victim.getName() + " was dragged out!");
		}
	}

	static class Teleport extends Attack {
		private static final long serialVersionUID = 1L;

		Teleport() {
			super(AttackNamesies.TELEPORT, "Use it to flee from any wild Pok\u00e9mon. It can also warp to the last Pok\u00e9mon Center visited.", 20, Type.PSYCHIC, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (!b.isWildBattle()) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			Messages.add(new MessageUpdate(user.getName() + " teleported out of battle!"));
			Messages.add(new MessageUpdate().withUpdate(Update.EXIT_BATTLE));
		}
	}

	static class RolePlay extends Attack implements ChangeAbilityMove {
		private static final long serialVersionUID = 1L;

		RolePlay() {
			super(AttackNamesies.ROLE_PLAY, "The user mimics the target completely, copying the target's natural Ability.", 10, Type.PSYCHIC, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.CHANGE_ABILITY);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			switch (o.getAbility().namesies()) {
				case WONDER_GUARD:
				case MULTITYPE:
				case STANCE_CHANGE:
				case IMPOSTER:
				case ILLUSION:
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
					return;
				default:
					super.apply(me, o, b);
				}
		}

		public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim) {
			// TODO: Combine with Trace
			Ability otherAbility = b.getOtherPokemon(victim.isPlayer()).getAbility();
			return otherAbility.namesies().getNewAbility();
		}

		public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
			ActivePokemon other = b.getOtherPokemon(victim.isPlayer());
			return victim.getName() + " copied " + other.getName() + "'s " + other.getAbility().getName() + "!";
		}
	}

	static class KnockOff extends Attack implements ItemHolder {
		private static final long serialVersionUID = 1L;

		KnockOff() {
			super(AttackNamesies.KNOCK_OFF, "The user slaps down the target's held item, preventing that item from being used in the battle.", 25, Type.DARK, MoveCategory.PHYSICAL);
			super.power = 65;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.CHANGE_ITEM);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return (int)(super.power*(o.isHoldingItem(b) ? 1.5 : 1));
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (!victim.isHoldingItem(b) || victim.hasAbility(AbilityNamesies.STICKY_HOLD)) {
				return;
			}
			
			Messages.add(new MessageUpdate(user.getName() + " knocked off " + victim.getName() + "'s " + victim.getHeldItem(b).getName() + "!"));
			super.applyEffects(b, user, victim);
		}

		public Item getItem() {
			return ItemNamesies.NO_ITEM.getItem();
		}
	}

	static class Whirlwind extends Attack {
		private static final long serialVersionUID = 1L;

		Whirlwind() {
			super(AttackNamesies.WHIRLWIND, "The target is blown away, to be replaced by another Pok\u00e9mon in its party. In the wild, the battle ends.", 20, Type.NORMAL, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.priority = -6;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			// Fails against the Suction Cups ability
			if (victim.hasAbility(AbilityNamesies.SUCTION_CUPS) && !user.breaksTheMold()) {
				Messages.add(new MessageUpdate(victim.getName() + "'s " + AbilityNamesies.SUCTION_CUPS.getName() + " prevents it from switching!"));
				return;
			}
			
			// Fails if this is the first attack of the turn, or if the victim is rooted by Ingrain
			if (b.isFirstAttack() || victim.hasEffect(EffectNamesies.INGRAIN)) {
				if (super.category == MoveCategory.STATUS)  {
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				}
				
				return;
			}
			
			Team opponent = b.getTrainer(victim.isPlayer());
			if (opponent instanceof WildPokemon) {
				// Fails against wild Pokemon of higher levels
				if (victim.getLevel() > user.getLevel()) {
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
					return;
				}
				
				// End the battle against a wild Pokemon
				Messages.add(new MessageUpdate(victim.getName() + " blew away!"));
				Messages.add(new MessageUpdate().withUpdate(Update.EXIT_BATTLE));
				return;
			}
			
			Trainer trainer = (Trainer)opponent;
			if (!trainer.hasRemainingPokemon()) {
				// Fails against trainers on their last Pokemon
				if (super.category == MoveCategory.STATUS) {
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				}
				
				return;
			}
			
			// Swap to a random Pokemon!
			Messages.add(new MessageUpdate(victim.getName() + " blew away!"));
			trainer.switchToRandom();
			victim = trainer.front();
			b.enterBattle(victim, "...and " + victim.getName() + " was dragged out!");
		}
	}

	static class Bestow extends Attack implements ItemHolder {
		private static final long serialVersionUID = 1L;
		private Item item;

		Bestow() {
			super(AttackNamesies.BESTOW, "The user passes its held item to the target when the target isn't holding an item.", 15, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.CHANGE_ITEM);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		private String getSwitchMessage(ActivePokemon user, Item userItem, ActivePokemon victim, Item victimItem) {
			return user.getName() + " gave " + victim.getName() + " its " + userItem.getName() + "!";
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (!user.isHoldingItem(b) || victim.isHoldingItem(b)) {
				if (super.category == MoveCategory.STATUS) {
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				}
				
				return;
			}
			
			Item userItem = user.getHeldItem(b), victimItem = victim.getHeldItem(b);
			Messages.add(new MessageUpdate(getSwitchMessage(user, userItem, victim, victimItem)));
			
			if (b.isWildBattle()) {
				user.giveItem((HoldItem)victimItem);
				victim.giveItem((HoldItem)userItem);
				return;
			}
			
			item = userItem;
			super.applyEffects(b, user, victim);
			
			item = victimItem;
			super.applyEffects(b, user, user);
		}

		public Item getItem() {
			return item;
		}
	}

	static class Switcheroo extends Attack implements ItemHolder {
		private static final long serialVersionUID = 1L;
		private Item item;

		Switcheroo() {
			super(AttackNamesies.SWITCHEROO, "The user passes its held item to the target when the target isn't holding an item.", 10, Type.DARK, MoveCategory.STATUS);
			super.accuracy = 100;
			super.effects.add(EffectNamesies.CHANGE_ITEM);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		private String getSwitchMessage(ActivePokemon user, Item userItem, ActivePokemon victim, Item victimItem) {
			return user.getName() + " switched its " + userItem.getName() + " with " + victim.getName() + "'s " + victimItem.getName() + "!";
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			if ((!user.isHoldingItem(b) && !victim.isHoldingItem(b)) || user.hasAbility(AbilityNamesies.STICKY_HOLD) || victim.hasAbility(AbilityNamesies.STICKY_HOLD)) {
				if (super.category == MoveCategory.STATUS) {
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				}
				
				return;
			}
			
			Item userItem = user.getHeldItem(b), victimItem = victim.getHeldItem(b);
			Messages.add(new MessageUpdate(getSwitchMessage(user, userItem, victim, victimItem)));
			
			if (b.isWildBattle()) {
				user.giveItem((HoldItem)victimItem);
				victim.giveItem((HoldItem)userItem);
				return;
			}
			
			item = userItem;
			super.applyEffects(b, user, victim);
			
			item = victimItem;
			super.applyEffects(b, user, user);
		}

		public Item getItem() {
			return item;
		}
	}

	static class Trick extends Attack implements ItemHolder {
		private static final long serialVersionUID = 1L;
		private Item item;

		Trick() {
			super(AttackNamesies.TRICK, "The user catches the target off guard and swaps its held item with its own.", 10, Type.PSYCHIC, MoveCategory.STATUS);
			super.accuracy = 100;
			super.effects.add(EffectNamesies.CHANGE_ITEM);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		private String getSwitchMessage(ActivePokemon user, Item userItem, ActivePokemon victim, Item victimItem) {
			return user.getName() + " switched its " + userItem.getName() + " with " + victim.getName() + "'s " + victimItem.getName() + "!";
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			if ((!user.isHoldingItem(b) && !victim.isHoldingItem(b)) || user.hasAbility(AbilityNamesies.STICKY_HOLD) || victim.hasAbility(AbilityNamesies.STICKY_HOLD)) {
				if (super.category == MoveCategory.STATUS) {
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				}
				
				return;
			}
			
			Item userItem = user.getHeldItem(b), victimItem = victim.getHeldItem(b);
			Messages.add(new MessageUpdate(getSwitchMessage(user, userItem, victim, victimItem)));
			
			if (b.isWildBattle()) {
				user.giveItem((HoldItem)victimItem);
				victim.giveItem((HoldItem)userItem);
				return;
			}
			
			item = userItem;
			super.applyEffects(b, user, victim);
			
			item = victimItem;
			super.applyEffects(b, user, user);
		}

		public Item getItem() {
			return item;
		}
	}

	static class Memento extends Attack {
		private static final long serialVersionUID = 1L;

		Memento() {
			super(AttackNamesies.MEMENTO, "The user faints when using this move. In return, it harshly lowers the target's Attack and Sp. Atk.", 10, Type.DARK, MoveCategory.STATUS);
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
			super(AttackNamesies.DESTINY_BOND, "When this move is used, if the user faints, the Pok\u00e9mon that landed the knockout hit also faints.", 5, Type.GHOST, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.DESTINY_BOND);
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
			super(AttackNamesies.CAMOUFLAGE, "The user's type is changed depending on its environment, such as at water's edge, in grass, or in a cave.", 20, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.CHANGE_TYPE);
			super.selfTarget = true;
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
			return new Type[] { b.getTerrainType().getType(), Type.NO_TYPE };
		}
	}

	static class Recycle extends Attack {
		private static final long serialVersionUID = 1L;

		Recycle() {
			super(AttackNamesies.RECYCLE, "The user recycles a held item that has been used in battle so it can be used again.", 10, Type.NORMAL, MoveCategory.STATUS);
			super.selfTarget = true;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			PokemonEffect consumed = victim.getEffect(EffectNamesies.CONSUMED_ITEM);
			if (consumed == null || victim.isHoldingItem(b)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			Item restored = ((ItemHolder)consumed).getItem();
			victim.giveItem((HoldItem)restored);
			Messages.add(new MessageUpdate(victim.getName() + "'s " + restored.getName() + " was restored!"));
		}
	}

	static class PartingShot extends Attack {
		private static final long serialVersionUID = 1L;

		PartingShot() {
			super(AttackNamesies.PARTING_SHOT, "With a parting threat, the user lowers the target's Attack and Sp. Atk stats. Then it switches with a party Pok\u00e9mon.", 20, Type.DARK, MoveCategory.STATUS);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.statChanges[Stat.ATTACK.index()] = -1;
			super.statChanges[Stat.SP_ATTACK.index()] = -1;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			// First execute the move as normal
			super.apply(me, o, b);
			
			Team t = b.getTrainer(me.isPlayer());
			if (t instanceof WildPokemon) {
				// End the battle against a wild Pokemon
				Messages.add(new MessageUpdate(me.getName() + " left the battle!"));
				Messages.add(new MessageUpdate().withUpdate(Update.EXIT_BATTLE));
				return;
			}
			
			Trainer trainer = (Trainer)t;
			if (!trainer.hasRemainingPokemon())
			{
				// Don't switch if no one to switch to
				return;
			}
			
			// Send this Pokemon back to the trainer and send out the next one
			Messages.add(new MessageUpdate(me.getName() + " went back to " + trainer.getName() + "!"));
			trainer.switchToRandom(); // TODO: Prompt a legit switch fo user
			me = trainer.front();
			b.enterBattle(me, trainer.getName() + " sent out " + me.getName() + "!");
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			Messages.add(new MessageUpdate(user.getName() + " called " + victim.getName() + " a chump!!"));
			super.applyEffects(b, user, victim);
		}
	}

	static class UTurn extends Attack {
		private static final long serialVersionUID = 1L;

		UTurn() {
			super(AttackNamesies.U_TURN, "After making its attack, the user rushes back to switch places with a party Pok\u00e9mon in waiting.", 20, Type.BUG, MoveCategory.PHYSICAL);
			super.power = 70;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			// First execute the move as normal
			super.apply(me, o, b);
			
			Team t = b.getTrainer(me.isPlayer());
			if (t instanceof WildPokemon) {
				// End the battle against a wild Pokemon
				Messages.add(new MessageUpdate(me.getName() + " left the battle!"));
				Messages.add(new MessageUpdate().withUpdate(Update.EXIT_BATTLE));
				return;
			}
			
			Trainer trainer = (Trainer)t;
			if (!trainer.hasRemainingPokemon())
			{
				// Don't switch if no one to switch to
				return;
			}
			
			// Send this Pokemon back to the trainer and send out the next one
			Messages.add(new MessageUpdate(me.getName() + " went back to " + trainer.getName() + "!"));
			trainer.switchToRandom(); // TODO: Prompt a legit switch fo user
			me = trainer.front();
			b.enterBattle(me, trainer.getName() + " sent out " + me.getName() + "!");
		}
	}

	static class BatonPass extends Attack {
		private static final long serialVersionUID = 1L;

		BatonPass() {
			super(AttackNamesies.BATON_PASS, "The user switches places with a party Pok\u00e9mon in waiting, passing along any stat changes.", 40, Type.NORMAL, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			Team t = b.getTrainer(user.isPlayer());
			if (t instanceof WildPokemon) {
				Messages.add(new MessageUpdate(user.getName() + " left the battle!"));
				Messages.add(new MessageUpdate().withUpdate(Update.EXIT_BATTLE));
				return;
			}
			
			Trainer trainer = (Trainer)t;
			if (!trainer.hasRemainingPokemon()) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			Messages.add(new MessageUpdate(user.getName() + " went back to " + trainer.getName() + "!"));
			trainer.switchToRandom(); // TODO: Prompt a legit switch fo user
			
			ActivePokemon next = trainer.front();
			next.resetAttributes();
			for (int i = 0; i < Stat.NUM_BATTLE_STATS; i++) {
				next.getAttributes().setStage(i, user.getStage(i));
			}
			
			for (PokemonEffect e : user.getEffects()) {
				if (e instanceof PassableEffect) {
					next.addEffect(e);
				}
			}
			
			user = next;
			b.enterBattle(user, trainer.getName() + " sent out " + user.getName() + "!", false);
		}
	}

	static class PerishSong extends Attack {
		private static final long serialVersionUID = 1L;

		PerishSong() {
			super(AttackNamesies.PERISH_SONG, "Any Pok\u00e9mon that hears this song faints in three turns, unless it switches out of battle.", 5, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.PERISH_SONG);
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			Messages.add(new MessageUpdate("All Pokemon hearing this song will faint in three turns!"));
			
			if (!victim.hasEffect(EffectNamesies.PERISH_SONG)) {
				super.applyEffects(b, user, victim);
			}
			
			if (!user.hasEffect(EffectNamesies.PERISH_SONG)) {
				super.applyEffects(b, user, user);
			}
		}
	}

	static class DragonTail extends Attack {
		private static final long serialVersionUID = 1L;

		DragonTail() {
			super(AttackNamesies.DRAGON_TAIL, "The user knocks away the target and drags out another Pok\u00e9mon in its party. In the wild, the battle ends.", 10, Type.DRAGON, MoveCategory.PHYSICAL);
			super.power = 60;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.priority = -6;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			// Fails against the Suction Cups ability
			if (victim.hasAbility(AbilityNamesies.SUCTION_CUPS) && !user.breaksTheMold()) {
				Messages.add(new MessageUpdate(victim.getName() + "'s " + AbilityNamesies.SUCTION_CUPS.getName() + " prevents it from switching!"));
				return;
			}
			
			// Fails if this is the first attack of the turn, or if the victim is rooted by Ingrain
			if (b.isFirstAttack() || victim.hasEffect(EffectNamesies.INGRAIN)) {
				if (super.category == MoveCategory.STATUS)  {
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				}
				
				return;
			}
			
			Team opponent = b.getTrainer(victim.isPlayer());
			if (opponent instanceof WildPokemon) {
				// Fails against wild Pokemon of higher levels
				if (victim.getLevel() > user.getLevel()) {
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
					return;
				}
				
				// End the battle against a wild Pokemon
				Messages.add(new MessageUpdate(victim.getName() + " was slapped away!"));
				Messages.add(new MessageUpdate().withUpdate(Update.EXIT_BATTLE));
				return;
			}
			
			Trainer trainer = (Trainer)opponent;
			if (!trainer.hasRemainingPokemon()) {
				// Fails against trainers on their last Pokemon
				if (super.category == MoveCategory.STATUS) {
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				}
				
				return;
			}
			
			// Swap to a random Pokemon!
			Messages.add(new MessageUpdate(victim.getName() + " was slapped away!"));
			trainer.switchToRandom();
			victim = trainer.front();
			b.enterBattle(victim, "...and " + victim.getName() + " was dragged out!");
		}
	}

	static class FoulPlay extends Attack {
		private static final long serialVersionUID = 1L;

		FoulPlay() {
			super(AttackNamesies.FOUL_PLAY, "The user turns the target's power against it. The higher the target's Attack stat, the greater the damage.", 15, Type.DARK, MoveCategory.PHYSICAL);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			double ratio = (double)Stat.getStat(Stat.ATTACK, me, o, b)/Stat.getStat(Stat.ATTACK, o, me, b);
			if (ratio > .5) return 60;
			if (ratio > .33) return 80;
			if (ratio > .25) return 120;
			return 150;
		}
	}

	static class Embargo extends Attack {
		private static final long serialVersionUID = 1L;

		Embargo() {
			super(AttackNamesies.EMBARGO, "It prevents the target from using its held item. Its Trainer is also prevented from using items on it.", 15, Type.DARK, MoveCategory.STATUS);
			super.accuracy = 100;
			super.effects.add(EffectNamesies.EMBARGO);
		}
	}

	static class NaturePower extends Attack {
		private static final long serialVersionUID = 1L;

		NaturePower() {
			super(AttackNamesies.NATURE_POWER, "An attack that makes use of nature's power. Its effects vary depending on the user's environment.", 20, Type.NORMAL, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public int getAccuracy(Battle b, ActivePokemon me, ActivePokemon o) {
			return b.getTerrainType().getAttack().getAccuracy(b, me, o);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			me.callNewMove(b, o, new Move(b.getTerrainType().getAttack()));
		}
	}

	static class Entrainment extends Attack implements ChangeAbilityMove {
		private static final long serialVersionUID = 1L;

		Entrainment() {
			super(AttackNamesies.ENTRAINMENT, "The user dances with an odd rhythm that compels the target to mimic it, making the target's Ability the same as the user's.", 15, Type.NORMAL, MoveCategory.STATUS);
			super.accuracy = 100;
			super.effects.add(EffectNamesies.CHANGE_ABILITY);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			switch (me.getAbility().namesies())
			{
				case TRUANT:
				case MULTITYPE:
				case STANCE_CHANGE:
				case ILLUSION:
				case TRACE:
				case IMPOSTER:
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
					return;
				default:
					super.apply(me, o, b);
				}
		}

		public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim) {
			// TODO: Combine with Trace/Role Play
			return caster.getAbility().namesies().getNewAbility();
		}

		public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
			return victim.getName() + " copied " + caster.getName() + "'s " + caster.getAbility().getName() + "!";
		}
	}

	static class MagicRoom extends Attack {
		private static final long serialVersionUID = 1L;

		MagicRoom() {
			super(AttackNamesies.MAGIC_ROOM, "The user creates a bizarre area in which Pok\u00e9mon's held items lose their effects for five turns.", 10, Type.PSYCHIC, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.MAGIC_ROOM);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}
	}

	static class WorrySeed extends Attack implements ChangeAbilityMove {
		private static final long serialVersionUID = 1L;

		WorrySeed() {
			super(AttackNamesies.WORRY_SEED, "A seed that causes worry is planted on the target. It prevents sleep by making its Ability Insomnia.", 10, Type.GRASS, MoveCategory.STATUS);
			super.accuracy = 100;
			super.effects.add(EffectNamesies.CHANGE_ABILITY);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			switch (o.getAbility().namesies()) {
				case TRUANT:
				case MULTITYPE:
				case STANCE_CHANGE:
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
					return;
				default:
					super.apply(me, o, b);
				}
		}

		public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim) {
			return AbilityNamesies.INSOMNIA.getNewAbility();
		}

		public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
			return victim.getName() + "'s ability was changed to " + AbilityNamesies.INSOMNIA.getName() + "!";
		}
	}

	static class SimpleBeam extends Attack implements ChangeAbilityMove {
		private static final long serialVersionUID = 1L;

		SimpleBeam() {
			super(AttackNamesies.SIMPLE_BEAM, "The user's mysterious psychic wave changes the target's Ability to Simple.", 15, Type.NORMAL, MoveCategory.STATUS);
			super.accuracy = 100;
			super.effects.add(EffectNamesies.CHANGE_ABILITY);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			switch (o.getAbility().namesies()) {
				case TRUANT:
				case MULTITYPE:
				case STANCE_CHANGE:
					Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
					return;
				default:
					super.apply(me, o, b);
				}
		}

		public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim) {
			return AbilityNamesies.SIMPLE.getNewAbility();
		}

		public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
			return victim.getName() + "'s ability was changed to " + AbilityNamesies.SIMPLE.getName() + "!";
		}
	}

	static class SkillSwap extends Attack implements ChangeAbilityMove {
		private static final long serialVersionUID = 1L;
		private Ability ability;
		
		private static boolean canSkillSwap(ActivePokemon p) {
			switch (p.getAbility().namesies()) {
				case WONDER_GUARD:
				case MULTITYPE:
				case ILLUSION:
				case STANCE_CHANGE:
					return false;
				default:
					return true;
			}
		}

		SkillSwap() {
			super(AttackNamesies.SKILL_SWAP, "The user employs its psychic power to exchange Abilities with the target.", 10, Type.PSYCHIC, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (!canSkillSwap(user) || !canSkillSwap(victim)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			ability = user.getAbility();
			EffectNamesies.CHANGE_ABILITY.getEffect().cast(b, user, victim, CastSource.ATTACK, super.printCast);
			
			ability = victim.getAbility();
			EffectNamesies.CHANGE_ABILITY.getEffect().cast(b, user, user, CastSource.ATTACK, super.printCast);
		}

		public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim) {
			return ability;
		}

		public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
			return victim.getName() + "'s ability was changed to " + ability.getName() + "!";
		}
	}

	static class VoltSwitch extends Attack {
		private static final long serialVersionUID = 1L;

		VoltSwitch() {
			super(AttackNamesies.VOLT_SWITCH, "After making its attack, the user rushes back to switch places with a party Pok\u00e9mon in waiting.", 20, Type.ELECTRIC, MoveCategory.SPECIAL);
			super.power = 70;
			super.accuracy = 100;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			// First execute the move as normal
			super.apply(me, o, b);
			
			Team t = b.getTrainer(me.isPlayer());
			if (t instanceof WildPokemon) {
				// End the battle against a wild Pokemon
				Messages.add(new MessageUpdate(me.getName() + " left the battle!"));
				Messages.add(new MessageUpdate().withUpdate(Update.EXIT_BATTLE));
				return;
			}
			
			Trainer trainer = (Trainer)t;
			if (!trainer.hasRemainingPokemon())
			{
				// Don't switch if no one to switch to
				return;
			}
			
			// Send this Pokemon back to the trainer and send out the next one
			Messages.add(new MessageUpdate(me.getName() + " went back to " + trainer.getName() + "!"));
			trainer.switchToRandom(); // TODO: Prompt a legit switch fo user
			me = trainer.front();
			b.enterBattle(me, trainer.getName() + " sent out " + me.getName() + "!");
		}
	}

	static class RelicSong extends Attack {
		private static final long serialVersionUID = 1L;

		RelicSong() {
			super(AttackNamesies.RELIC_SONG, "The user sings an ancient song and attacks by appealing to the hearts of those listening. It may also induce sleep.", 10, Type.NORMAL, MoveCategory.SPECIAL);
			super.power = 75;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.ASLEEP;
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.SOUND_BASED);
		}
	}

	static class Snarl extends Attack {
		private static final long serialVersionUID = 1L;

		Snarl() {
			super(AttackNamesies.SNARL, "The user yells as if it is ranting about something, making the target's Sp. Atk stat decrease.", 15, Type.DARK, MoveCategory.SPECIAL);
			super.power = 55;
			super.accuracy = 95;
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.statChanges[Stat.SP_ATTACK.index()] = -1;
		}
	}

	static class IceBurn extends Attack implements MultiTurnMove {
		private static final long serialVersionUID = 1L;

		IceBurn() {
			super(AttackNamesies.ICE_BURN, "On the second turn, an ultracold, freezing wind surrounds the target. This may leave the target with a burn.", 5, Type.ICE, MoveCategory.SPECIAL);
			super.power = 140;
			super.accuracy = 90;
			super.effectChance = 30;
			super.status = StatusCondition.BURNED;
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
		}

		public boolean chargesFirst() {
			return true;
		}

		public boolean semiInvulnerability() {
			return false;
		}

		public void charge(ActivePokemon user, Battle b) {
			Messages.add(new MessageUpdate(getChargeMessage(user)));
		}

		private String getChargeMessage(ActivePokemon user) {
			return user.getName() + " is charging!";
		}
	}

	static class VCreate extends Attack {
		private static final long serialVersionUID = 1L;

		VCreate() {
			super(AttackNamesies.V_CREATE, "With a hot flame on its forehead, the user hurls itself at its target. It lowers the user's Defense, Sp. Def, and Speed stats.", 5, Type.FIRE, MoveCategory.PHYSICAL);
			super.power = 180;
			super.accuracy = 95;
			super.moveTypes.add(MoveType.METRONOMELESS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.statChanges[Stat.SP_DEFENSE.index()] = -1;
			super.statChanges[Stat.SPEED.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Surf extends Attack {
		private static final long serialVersionUID = 1L;

		Surf() {
			super(AttackNamesies.SURF, "It swamps the area around the user with a giant wave. It can also be used for crossing water.", 15, Type.WATER, MoveCategory.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
		}
	}

	static class VoltTackle extends Attack implements RecoilMove {
		private static final long serialVersionUID = 1L;

		VoltTackle() {
			super(AttackNamesies.VOLT_TACKLE, "The user electrifies itself, then charges. It causes considerable damage to the user and may leave the target with paralysis.", 15, Type.ELECTRIC, MoveCategory.PHYSICAL);
			super.power = 120;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyRecoil(Battle b, ActivePokemon user, int damage) {
			if (user.hasAbility(AbilityNamesies.ROCK_HEAD) || user.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.add(new MessageUpdate(user.getName() + " was hurt by recoil!"));
			user.reduceHealth(b, (int)Math.ceil(damage/3.0), false);
		}
	}

	static class FocusBlast extends Attack {
		private static final long serialVersionUID = 1L;

		FocusBlast() {
			super(AttackNamesies.FOCUS_BLAST, "The user heightens its mental focus and unleashes its power. It may also lower the target's Sp. Def.", 5, Type.FIGHTING, MoveCategory.SPECIAL);
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
			super(AttackNamesies.DIAMOND_STORM, "The user whips up a storm of diamonds to damage opposing Pok\u00e9mon. This may also raise the user's Defense stat.", 5, Type.ROCK, MoveCategory.PHYSICAL);
			super.power = 100;
			super.accuracy = 95;
			super.effectChance = 50;
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Moonblast extends Attack {
		private static final long serialVersionUID = 1L;

		Moonblast() {
			super(AttackNamesies.MOONBLAST, "Borrowing the power of the moon, the user attacks the target. This may also lower the target's Sp. Atk stat.", 15, Type.FAIRY, MoveCategory.PHYSICAL);
			super.power = 95;
			super.accuracy = 100;
			super.effectChance = 30;
			super.statChanges[Stat.SP_ATTACK.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class LandsWrath extends Attack {
		private static final long serialVersionUID = 1L;

		LandsWrath() {
			super(AttackNamesies.LANDS_WRATH, "The user gathers the energy of the land and focuses that power on opposing Pok\u00e9mon to damage them.", 10, Type.GROUND, MoveCategory.PHYSICAL);
			super.power = 90;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class PhantomForce extends Attack implements MultiTurnMove, AccuracyBypassEffect {
		private static final long serialVersionUID = 1L;

		PhantomForce() {
			super(AttackNamesies.PHANTOM_FORCE, "The user vanishes somewhere, then strikes the target on the next turn. This move hits even if the target protects itself.", 10, Type.GHOST, MoveCategory.PHYSICAL);
			super.power = 90;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(o.hasEffect(EffectNamesies.USED_MINIMIZE) ? 2 : 1);
		}

		public boolean chargesFirst() {
			return true;
		}

		public boolean semiInvulnerability() {
			return true;
		}

		public void charge(ActivePokemon user, Battle b) {
			Messages.add(new MessageUpdate(getChargeMessage(user)));
		}

		private String getChargeMessage(ActivePokemon user) {
			return user.getName() + " vanished suddenly!";
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			return !defending.isSemiInvulnerable() && defending.hasEffect(EffectNamesies.USED_MINIMIZE);
		}
	}

	static class OblivionWing extends Attack implements SapHealthEffect {
		private static final long serialVersionUID = 1L;

		OblivionWing() {
			super(AttackNamesies.OBLIVION_WING, "The user absorbs its target's HP. The user's HP is restored by over half of the damage taken by the target.", 10, Type.FLYING, MoveCategory.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
		}

		public double sapPercentage() {
			return .75;
		}
	}

	static class Geomancy extends Attack implements MultiTurnMove {
		private static final long serialVersionUID = 1L;

		Geomancy() {
			super(AttackNamesies.GEOMANCY, "The user absorbs energy and sharply raises its Sp. Atk, Sp. Def, and Speed stats on the next turn.", 10, Type.FAIRY, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.SLEEP_TALK_FAIL);
			super.selfTarget = true;
			super.statChanges[Stat.SP_ATTACK.index()] = 2;
			super.statChanges[Stat.SP_DEFENSE.index()] = 2;
			super.statChanges[Stat.SPEED.index()] = 2;
		}

		public boolean chargesFirst() {
			return true;
		}

		public boolean semiInvulnerability() {
			return false;
		}

		public void charge(ActivePokemon user, Battle b) {
			Messages.add(new MessageUpdate(getChargeMessage(user)));
		}

		private String getChargeMessage(ActivePokemon user) {
			return user.getName() + " is absorbing power!";
		}
	}

	static class Boomburst extends Attack {
		private static final long serialVersionUID = 1L;

		Boomburst() {
			super(AttackNamesies.BOOMBURST, "The user attacks everything around it with the destructive power of a terrible, explosive sound.", 10, Type.NORMAL, MoveCategory.SPECIAL);
			super.power = 140;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SOUND_BASED);
		}
	}

	static class PlayRough extends Attack {
		private static final long serialVersionUID = 1L;

		PlayRough() {
			super(AttackNamesies.PLAY_ROUGH, "The user plays rough with the target and attacks it. This may also lower the target's Attack stat.", 10, Type.FAIRY, MoveCategory.PHYSICAL);
			super.power = 90;
			super.accuracy = 90;
			super.effectChance = 10;
			super.statChanges[Stat.ATTACK.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class CraftyShield extends Attack {
		private static final long serialVersionUID = 1L;

		CraftyShield() {
			super(AttackNamesies.CRAFTY_SHIELD, "The user protects itself and its allies from status moves with a mysterious power. This does not stop moves that do damage.", 10, Type.FAIRY, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.CRAFTY_SHIELD);
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
			super(AttackNamesies.NUZZLE, "The user attacks by nuzzling its electrified cheeks against the target. This also leaves the target with paralysis.", 20, Type.ELECTRIC, MoveCategory.PHYSICAL);
			super.power = 20;
			super.accuracy = 100;
			super.status = StatusCondition.PARALYZED;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class DrainingKiss extends Attack implements SapHealthEffect {
		private static final long serialVersionUID = 1L;

		DrainingKiss() {
			super(AttackNamesies.DRAINING_KISS, "The user steals the target's energy with a kiss. The user's HP is restored by over half of the damage taken by the target.", 10, Type.FAIRY, MoveCategory.SPECIAL);
			super.power = 50;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public double sapPercentage() {
			return .75;
		}
	}

	static class FairyWind extends Attack {
		private static final long serialVersionUID = 1L;

		FairyWind() {
			super(AttackNamesies.FAIRY_WIND, "The user stirs up a fairy wind and strikes the target with it.", 30, Type.FAIRY, MoveCategory.SPECIAL);
			super.power = 40;
			super.accuracy = 100;
		}
	}

	static class ParabolicCharge extends Attack implements SapHealthEffect {
		private static final long serialVersionUID = 1L;

		ParabolicCharge() {
			super(AttackNamesies.PARABOLIC_CHARGE, "The user attacks everything around it. The user's HP is restored by half the damage taken by those hit.", 20, Type.ELECTRIC, MoveCategory.SPECIAL);
			super.power = 50;
			super.accuracy = 100;
		}
	}

	static class DisarmingVoice extends Attack {
		private static final long serialVersionUID = 1L;

		DisarmingVoice() {
			super(AttackNamesies.DISARMING_VOICE, "Letting out a charming cry, the user does emotional damage to opposing Pok\u00e9mon. This attack never misses.", 15, Type.FAIRY, MoveCategory.SPECIAL);
			super.power = 40;
			super.moveTypes.add(MoveType.SOUND_BASED);
		}
	}

	static class FreezeDry extends Attack implements AdvantageMultiplierMove {
		private static final long serialVersionUID = 1L;

		FreezeDry() {
			super(AttackNamesies.FREEZE_DRY, "The user rapidly cools the target. This may also leave the target frozen. This move is super effective on Water types.", 20, Type.ICE, MoveCategory.SPECIAL);
			super.power = 70;
			super.accuracy = 100;
			super.effectChance = 10;
			super.status = StatusCondition.FROZEN;
		}

		public double multiplyAdvantage(Type moveType, Type[] defendingType) {
			double multiplier = 1;
			for (int i = 0; i < 2; i++) {
				if (defendingType[i] == Type.WATER) {
					multiplier *= 2/Type.getBasicAdvantage(moveType, defendingType[i]);
				}
			}
			
			return multiplier;
		}
	}

	static class FlyingPress extends Attack implements AdvantageMultiplierMove {
		private static final long serialVersionUID = 1L;

		FlyingPress() {
			super(AttackNamesies.FLYING_PRESS, "The user dives down onto the target from the sky. This move is Fighting and Flying type simultaneously.", 10, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 95;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public double multiplyAdvantage(Type moveType, Type[] defendingType) {
			return Type.getBasicAdvantage(Type.FLYING, defendingType[0])*Type.getBasicAdvantage(Type.FLYING, defendingType[1]);
		}
	}

	static class TopsyTurvy extends Attack {
		private static final long serialVersionUID = 1L;

		TopsyTurvy() {
			super(AttackNamesies.TOPSY_TURVY, "All stat changes affecting the target turn topsy-turvy and become the opposite of what they were.", 20, Type.DARK, MoveCategory.STATUS);
			super.accuracy = 100;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			for (int i = 0; i < Stat.NUM_BATTLE_STATS; i++) {
				victim.getAttributes().setStage(i, -victim.getStage(i));
			}
			
			Messages.add(new MessageUpdate(victim.getName() + "'s stat changes were all reversed!"));
		}
	}

	static class PlayNice extends Attack {
		private static final long serialVersionUID = 1L;

		PlayNice() {
			super(AttackNamesies.PLAY_NICE, "The user and the target become friends, and the target loses its will to fight. This lowers the target's Attack stat.", 20, Type.NORMAL, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.moveTypes.add(MoveType.SUBSTITUTE_PIERCING);
			super.statChanges[Stat.ATTACK.index()] = -1;
		}
	}

	static class EerieImpulse extends Attack {
		private static final long serialVersionUID = 1L;

		EerieImpulse() {
			super(AttackNamesies.EERIE_IMPULSE, "The user's body generates an eerie impulse. Exposing the target to it harshly lowers the target's Sp. Atk stat.", 15, Type.ELECTRIC, MoveCategory.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.SP_ATTACK.index()] = -2;
		}
	}

	static class MistyTerrain extends Attack {
		private static final long serialVersionUID = 1L;

		MistyTerrain() {
			super(AttackNamesies.MISTY_TERRAIN, "The user covers the ground under everyone's feet with mist for five turns. This protects Pok\u00e9mon on the ground from status conditions.", 10, Type.FAIRY, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.MISTY_TERRAIN);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}
	}

	static class FairyLock extends Attack {
		private static final long serialVersionUID = 1L;

		FairyLock() {
			super(AttackNamesies.FAIRY_LOCK, "By locking down the battlefield, the user keeps all Pok\u00e9mon from fleeing during the next turn.", 10, Type.FAIRY, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.FAIRY_LOCK);
			super.moveTypes.add(MoveType.NON_SNATCHABLE);
			super.selfTarget = true;
		}
	}

	static class AromaticMist extends Attack {
		private static final long serialVersionUID = 1L;

		AromaticMist() {
			super(AttackNamesies.AROMATIC_MIST, "The user its Sp. Def stat with a mysterious aroma.", 20, Type.FAIRY, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
		}
	}

	static class BabyDollEyes extends Attack {
		private static final long serialVersionUID = 1L;

		BabyDollEyes() {
			super(AttackNamesies.BABY_DOLL_EYES, "The user stares at the target with its baby-doll eyes, which lowers its Attack stat. This move always goes first.", 30, Type.FAIRY, MoveCategory.STATUS);
			super.accuracy = 100;
			super.priority = 1;
			super.statChanges[Stat.ATTACK.index()] = -1;
		}
	}

	static class PetalBlizzard extends Attack {
		private static final long serialVersionUID = 1L;

		PetalBlizzard() {
			super(AttackNamesies.PETAL_BLIZZARD, "The user stirs up a violent petal blizzard and attacks everything around it.", 15, Type.GRASS, MoveCategory.PHYSICAL);
			super.power = 90;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class GrassyTerrain extends Attack {
		private static final long serialVersionUID = 1L;

		GrassyTerrain() {
			super(AttackNamesies.GRASSY_TERRAIN, "The user turns the ground under everyone's feet to grass for five turns. This restores the HP of Pok\u00e9mon on the ground a little every turn.", 10, Type.GRASS, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.GRASSY_TERRAIN);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}
	}

	static class FlowerShield extends Attack {
		private static final long serialVersionUID = 1L;

		FlowerShield() {
			super(AttackNamesies.FLOWER_SHIELD, "The user raises its Defense stat with a mysterious power.", 10, Type.FAIRY, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 1;
		}
	}

	static class NobleRoar extends Attack {
		private static final long serialVersionUID = 1L;

		NobleRoar() {
			super(AttackNamesies.NOBLE_ROAR, "Letting out a noble roar, the user intimidates the target and lowers its Attack and Sp. Atk stats.", 30, Type.NORMAL, MoveCategory.STATUS);
			super.accuracy = 100;
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.statChanges[Stat.ATTACK.index()] = -1;
			super.statChanges[Stat.SP_ATTACK.index()] = -1;
		}
	}

	static class Powder extends Attack {
		private static final long serialVersionUID = 1L;

		Powder() {
			super(AttackNamesies.POWDER, "The user covers the target in a powder that explodes and damages the target if it uses a Fire-type move.", 20, Type.BUG, MoveCategory.STATUS);
			super.accuracy = 100;
			super.effects.add(EffectNamesies.POWDER);
			super.moveTypes.add(MoveType.POWDER);
			super.priority = 1;
		}
	}

	static class Rototiller extends Attack {
		private static final long serialVersionUID = 1L;

		Rototiller() {
			super(AttackNamesies.ROTOTILLER, "Tilling the soil, the user makes it easier for plants to grow. This raises its Attack and Sp. Atk stats.", 10, Type.GROUND, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.SP_ATTACK.index()] = 1;
		}
	}

	static class WaterShuriken extends Attack implements MultiStrikeMove {
		private static final long serialVersionUID = 1L;

		WaterShuriken() {
			super(AttackNamesies.WATER_SHURIKEN, "The user hits the target with throwing stars two to five times in a row. This move always goes first.", 20, Type.WATER, MoveCategory.PHYSICAL);
			super.power = 15;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void applyDamage(ActivePokemon me, ActivePokemon o, Battle b) {
			int hits = this.getNumHits(me);
			
			int hit = 1;
			for (; hit <= hits; hit++) {
				Messages.add(new MessageUpdate("Hit " + hit + "!"));
				super.applyDamage(me, o, b);
				
				// Stop attacking the dead
				if (o.isFainted(b)) {
					break;
				}
			}
			
			hit--;
			
			// Print hits and gtfo
			Messages.add(new MessageUpdate("Hit " + hit + " time" + (hit == 1 ? "" : "s") + "!"));
		}

		public int getMinHits() {
			return 2;
		}

		public int getMaxHits() {
			return 5;
		}
	}

	static class MatBlock extends Attack {
		private static final long serialVersionUID = 1L;

		MatBlock() {
			super(AttackNamesies.MAT_BLOCK, "Using a pulled-up mat as a shield, the user protects itself and its allies from damaging moves. This does not stop status moves.", 15, Type.FIGHTING, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.MAT_BLOCK);
			super.moveTypes.add(MoveType.ASSISTLESS);
			super.selfTarget = true;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			if (!me.getAttributes().isFirstTurn()) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			super.apply(me, o, b);
		}
	}

	static class MysticalFire extends Attack {
		private static final long serialVersionUID = 1L;

		MysticalFire() {
			super(AttackNamesies.MYSTICAL_FIRE, "The user attacks by breathing a special, hot fire. This also lowers the target's Sp. Atk stat.", 10, Type.FIRE, MoveCategory.SPECIAL);
			super.power = 65;
			super.accuracy = 100;
			super.statChanges[Stat.SP_ATTACK.index()] = -1;
		}
	}

	static class Infestation extends Attack {
		private static final long serialVersionUID = 1L;

		Infestation() {
			super(AttackNamesies.INFESTATION, "The target is infested and attacked for four to five turns. The target can't flee during this time.", 20, Type.BUG, MoveCategory.SPECIAL);
			super.power = 20;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.INFESTATION);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Electrify extends Attack {
		private static final long serialVersionUID = 1L;

		Electrify() {
			super(AttackNamesies.ELECTRIFY, "If the target is electrified before it uses a move during that turn, the target's move becomes Electric type.", 20, Type.ELECTRIC, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.ELECTRIFIED);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.priority = 1;
		}
	}

	static class FellStinger extends Attack implements MurderEffect {
		private static final long serialVersionUID = 1L;

		FellStinger() {
			super(AttackNamesies.FELL_STINGER, "When the user knocks out a target with this move, the user's Attack stat rises sharply.", 25, Type.BUG, MoveCategory.PHYSICAL);
			super.power = 30;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void killWish(Battle b, ActivePokemon dead, ActivePokemon murderer) {
			murderer.getAttributes().modifyStage(murderer, murderer, 2, Stat.ATTACK, b, CastSource.ATTACK);
		}
	}

	static class MagneticFlux extends Attack {
		private static final long serialVersionUID = 1L;

		MagneticFlux() {
			super(AttackNamesies.MAGNETIC_FLUX, "The user manipulates magnetic fields which raises its Defense and Sp. Def stats.", 20, Type.ELECTRIC, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.DEFENSE.index()] = 1;
			super.statChanges[Stat.SP_DEFENSE.index()] = 1;
		}
	}

	static class StickyWeb extends Attack {
		private static final long serialVersionUID = 1L;

		StickyWeb() {
			super(AttackNamesies.STICKY_WEB, "The user weaves a sticky net around the opposing team, which lowers their Speed stat upon switching into battle.", 20, Type.BUG, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.STICKY_WEB);
			super.moveTypes.add(MoveType.FIELD);
		}
	}

	static class Belch extends Attack {
		private static final long serialVersionUID = 1L;

		Belch() {
			super(AttackNamesies.BELCH, "The user lets out a damaging belch on the target. The user must eat a Berry to use this move.", 10, Type.POISON, MoveCategory.SPECIAL);
			super.power = 120;
			super.accuracy = 90;
			super.moveTypes.add(MoveType.ASSISTLESS);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			if (!me.hasEffect(EffectNamesies.EATEN_BERRY)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			super.apply(me, o, b);
		}
	}

	static class VenomDrench extends Attack {
		private static final long serialVersionUID = 1L;

		VenomDrench() {
			super(AttackNamesies.VENOM_DRENCH, "Opposing Pok\u00e9mon are drenched in an odd poisonous liquid. This lowers the Attack, Sp. Atk, and Speed stats of a poisoned target.", 20, Type.POISON, MoveCategory.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.ATTACK.index()] = -1;
			super.statChanges[Stat.SP_ATTACK.index()] = -1;
			super.statChanges[Stat.SPEED.index()] = -1;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			if (!o.hasStatus(StatusCondition.POISONED)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			super.apply(me, o, b);
		}
	}

	static class ElectricTerrain extends Attack {
		private static final long serialVersionUID = 1L;

		ElectricTerrain() {
			super(AttackNamesies.ELECTRIC_TERRAIN, "The user electrifies the ground under everyone's feet for five turns. Pok\u00e9mon on the ground no longer fall asleep.", 10, Type.ELECTRIC, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.ELECTRIC_TERRAIN);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}
	}

	static class PsychicTerrain extends Attack {
		private static final long serialVersionUID = 1L;

		PsychicTerrain() {
			super(AttackNamesies.PSYCHIC_TERRAIN, "This protects Pokémon on the ground from priority moves and powers up Psychic-type moves for five turns.", 10, Type.PSYCHIC, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.PSYCHIC_TERRAIN);
			super.moveTypes.add(MoveType.NO_MAGIC_COAT);
			super.moveTypes.add(MoveType.FIELD);
		}
	}

	static class PowerUpPunch extends Attack {
		private static final long serialVersionUID = 1L;

		PowerUpPunch() {
			super(AttackNamesies.POWER_UP_PUNCH, "Striking opponents over and over makes the user's fists harder. Hitting a target raises the Attack stat.", 20, Type.FIGHTING, MoveCategory.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PUNCHING);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Confide extends Attack {
		private static final long serialVersionUID = 1L;

		Confide() {
			super(AttackNamesies.CONFIDE, "The user tells the target a secret, and the target loses its ability to concentrate. This lowers the target's Sp. Atk stat.", 20, Type.NORMAL, MoveCategory.STATUS);
			super.moveTypes.add(MoveType.SOUND_BASED);
			super.moveTypes.add(MoveType.PROTECT_PIERCING);
			super.statChanges[Stat.SP_ATTACK.index()] = -1;
		}
	}

	static class Cut extends Attack {
		private static final long serialVersionUID = 1L;

		Cut() {
			super(AttackNamesies.CUT, "The target is cut with a scythe or a claw. It can also be used to cut down thin trees.", 30, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 50;
			super.accuracy = 95;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class DazzlingGleam extends Attack {
		private static final long serialVersionUID = 1L;

		DazzlingGleam() {
			super(AttackNamesies.DAZZLING_GLEAM, "The user damages opposing Pok\u00e9mon by emitting a powerful flash.", 10, Type.FAIRY, MoveCategory.SPECIAL);
			super.power = 80;
			super.accuracy = 100;
		}
	}

	static class Strength extends Attack {
		private static final long serialVersionUID = 1L;

		Strength() {
			super(AttackNamesies.STRENGTH, "The target is slugged with a punch thrown at maximum power. It can also be used to move heavy boulders.", 15, Type.NORMAL, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class OriginPulse extends Attack {
		private static final long serialVersionUID = 1L;

		OriginPulse() {
			super(AttackNamesies.ORIGIN_PULSE, "The user attacks opposing Pokémon with countless beams of light that glow a deep and brilliant blue.", 10, Type.WATER, MoveCategory.SPECIAL);
			super.power = 110;
			super.accuracy = 85;
		}
	}

	static class PrecipiceBlades extends Attack {
		private static final long serialVersionUID = 1L;

		PrecipiceBlades() {
			super(AttackNamesies.PRECIPICE_BLADES, "The user attacks opposing Pokémon by manifesting the power of the land in fearsome blades of stone.", 10, Type.GROUND, MoveCategory.PHYSICAL);
			super.power = 120;
			super.accuracy = 85;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class ShoreUp extends Attack implements SelfHealingMove {
		private static final long serialVersionUID = 1L;

		ShoreUp() {
			super(AttackNamesies.SHORE_UP, "The user regains up to half of its max HP. It restores more HP in a sandstorm.", 10, Type.GROUND, MoveCategory.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b) {
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			if (b.getWeather().namesies() == EffectNamesies.SANDSTORM) {
				victim.healHealthFraction(1);
			}
			else {
				victim.healHealthFraction(1/2.0);
			}
			
			Messages.add(new MessageUpdate(victim.getName() + "'s health was restored!").updatePokemon(b, victim));
		}
	}

	static class FloralHealing extends Attack implements SelfHealingMove {
		private static final long serialVersionUID = 1L;

		FloralHealing() {
			super(AttackNamesies.FLORAL_HEALING, "The user restores the target's HP by up to half of its max HP. It restores more HP when the terrain is grass.", 10, Type.FAIRY, MoveCategory.STATUS);
			super.selfTarget = true;
		}

		public void heal(ActivePokemon user, ActivePokemon victim, Battle b) {
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			if (b.hasEffect(EffectNamesies.GRASSY_TERRAIN)) {
				victim.healHealthFraction(1);
			}
			else {
				victim.healHealthFraction(1/2.0);
			}
			
			Messages.add(new MessageUpdate(victim.getName() + "'s health was restored!").updatePokemon(b, victim));
		}
	}

	static class FirstImpression extends Attack {
		private static final long serialVersionUID = 1L;

		FirstImpression() {
			super(AttackNamesies.FIRST_IMPRESSION, "Although this move has great power, it only works the first turn the user is in battle.", 10, Type.BUG, MoveCategory.PHYSICAL);
			super.power = 90;
			super.accuracy = 100;
			super.priority = 2;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			if (!me.getAttributes().isFirstTurn()) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			super.apply(me, o, b);
		}
	}

	static class AnchorShot extends Attack {
		private static final long serialVersionUID = 1L;

		AnchorShot() {
			super(AttackNamesies.ANCHOR_SHOT, "The user entangles the target with its anchor chain while attacking. The target becomes unable to flee.", 20, Type.STEEL, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.TRAPPED);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class SpiritShackle extends Attack {
		private static final long serialVersionUID = 1L;

		SpiritShackle() {
			super(AttackNamesies.SPIRIT_SHACKLE, "The user attacks while simultaneously stitching the target's shadow to the ground to prevent the target from escaping.", 10, Type.GHOST, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.TRAPPED);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class DarkestLariat extends Attack implements OpponentIgnoreStageEffect {
		private static final long serialVersionUID = 1L;

		DarkestLariat() {
			super(AttackNamesies.DARKEST_LARIAT, "The user swings both arms and hits the target. The target's stat changes don't affect this attack's damage.", 10, Type.DARK, MoveCategory.PHYSICAL);
			super.power = 85;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}

		public boolean ignoreStage(Stat s) {
			return !s.user();
		}
	}

	static class SparklingAria extends Attack {
		private static final long serialVersionUID = 1L;

		SparklingAria() {
			super(AttackNamesies.SPARKLING_ARIA, "The user bursts into song, emitting many bubbles. Any Pokémon suffering from a burn will be healed by the touch of these bubbles.", 10, Type.WATER, MoveCategory.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
		}

		public int setPower(Battle b, ActivePokemon me, ActivePokemon o) {
			return super.power*(o.hasStatus(StatusCondition.ASLEEP) ? 2 : 1);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (victim.hasStatus(StatusCondition.ASLEEP)) {
				Status.removeStatus(b, victim, CastSource.ATTACK);
			}
		}
	}

	static class HighHorsepower extends Attack {
		private static final long serialVersionUID = 1L;

		HighHorsepower() {
			super(AttackNamesies.HIGH_HORSEPOWER, "The user fiercely attacks the target using its entire body.", 10, Type.GROUND, MoveCategory.PHYSICAL);
			super.power = 95;
			super.accuracy = 95;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class StrengthSap extends Attack implements SapHealthEffect {
		private static final long serialVersionUID = 1L;

		StrengthSap() {
			super(AttackNamesies.STRENGTH_SAP, "The user restores its HP by the same amount as the target's Attack stat. It also lowers the target's Attack stat.", 10, Type.GRASS, MoveCategory.STATUS);
			super.accuracy = 100;
			super.statChanges[Stat.ATTACK.index()] = -1;
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			int victimAttackStat = Stat.getStat(Stat.ATTACK, victim, user, b);
			super.applyEffects(b, user, victim);
			
			this.sapHealth(b, user, victim, victimAttackStat, true);
		}

		public double sapPercentage() {
			return 1;
		}
	}

	static class Leafage extends Attack {
		private static final long serialVersionUID = 1L;

		Leafage() {
			super(AttackNamesies.LEAFAGE, "The user attacks by pelting the target with leaves.", 40, Type.GRASS, MoveCategory.PHYSICAL);
			super.power = 40;
			super.accuracy = 100;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class ToxicThread extends Attack {
		private static final long serialVersionUID = 1L;

		ToxicThread() {
			super(AttackNamesies.TOXIC_THREAD, "The user shoots poisonous threads to poison the target and lower the target's Speed stat.", 20, Type.POISON, MoveCategory.STATUS);
			super.accuracy = 100;
			super.status = StatusCondition.POISONED;
			super.statChanges[Stat.SPEED.index()] = -1;
		}
	}

	static class LaserFocus extends Attack {
		private static final long serialVersionUID = 1L;

		LaserFocus() {
			super(AttackNamesies.LASER_FOCUS, "The user concentrates intensely. The attack on the next turn always results in a critical hit.", 30, Type.NORMAL, MoveCategory.STATUS);
			super.effects.add(EffectNamesies.LASER_FOCUS);
			super.selfTarget = true;
		}
	}

	static class GearUp extends Attack {
		private static final long serialVersionUID = 1L;

		GearUp() {
			super(AttackNamesies.GEAR_UP, "The user engages its gears to raise its Attack and Sp. Atk stats.", 20, Type.STEEL, MoveCategory.STATUS);
			super.selfTarget = true;
			super.statChanges[Stat.ATTACK.index()] = 1;
			super.statChanges[Stat.SP_ATTACK.index()] = 1/2;
		}
	}

	static class ThroatChop extends Attack {
		private static final long serialVersionUID = 1L;

		ThroatChop() {
			super(AttackNamesies.THROAT_CHOP, "The user attacks the target's throat, and the resultant suffering prevents the target from using moves that emit sound for two turns.", 15, Type.DARK, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.SOUND_BLOCK);
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class PollenPuff extends Attack {
		private static final long serialVersionUID = 1L;

		PollenPuff() {
			super(AttackNamesies.POLLEN_PUFF, "The user attacks the enemy with a pollen puff that explodes. If the target is an ally, it gives the ally a pollen puff that restores its HP instead.", 15, Type.BUG, MoveCategory.SPECIAL);
			super.power = 90;
			super.accuracy = 100;
		}
	}

	static class Lunge extends Attack {
		private static final long serialVersionUID = 1L;

		Lunge() {
			super(AttackNamesies.LUNGE, "The user makes a lunge at the target, attacking with full force. This also lowers the target's Attack stat.", 15, Type.BUG, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.statChanges[Stat.ATTACK.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class FireLash extends Attack {
		private static final long serialVersionUID = 1L;

		FireLash() {
			super(AttackNamesies.FIRE_LASH, "The user strikes the target with a burning lash. This also lowers the target's Defense stat.", 15, Type.FIRE, MoveCategory.PHYSICAL);
			super.power = 80;
			super.accuracy = 100;
			super.statChanges[Stat.DEFENSE.index()] = -1;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class BurnUp extends Attack implements ChangeTypeSource {
		private static final long serialVersionUID = 1L;

		BurnUp() {
			super(AttackNamesies.BURN_UP, "To inflict massive damage, the user burns itself out. After using this move, the user will no longer be Fire type.", 5, Type.FIRE, MoveCategory.SPECIAL);
			super.power = 130;
			super.accuracy = 100;
			super.effects.add(EffectNamesies.CHANGE_TYPE);
			super.selfTarget = true;
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			if (!me.isType(b, Type.FIRE)) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			super.apply(me, o, b);
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
			Type[] type = victim.getType(b);
			
			// TODO: Rewrite this because it looks stupid
			if (type[0] == Type.FIRE) {
				return new Type[] { type[1], Type.NO_TYPE };
			}
			
			if (type[1] == Type.FIRE) {
				return new Type[] { type[0], Type.NO_TYPE };
			}
			
			return null;
		}
	}

	static class SmartStrike extends Attack {
		private static final long serialVersionUID = 1L;

		SmartStrike() {
			super(AttackNamesies.SMART_STRIKE, "The user stabs the target with a sharp horn. This attack never misses.", 10, Type.STEEL, MoveCategory.PHYSICAL);
			super.power = 70;
			super.moveTypes.add(MoveType.PHYSICAL_CONTACT);
		}
	}

	static class Purify extends Attack {
		private static final long serialVersionUID = 1L;

		Purify() {
			super(AttackNamesies.PURIFY, "The user heals the target's status condition. If the move succeeds, it also restores the user's own HP.", 20, Type.POISON, MoveCategory.STATUS);
		}

		public void apply(ActivePokemon me, ActivePokemon o, Battle b) {
			if (!me.hasStatus()) {
				Messages.add(new MessageUpdate(Effect.DEFAULT_FAIL_MESSAGE));
				return;
			}
			
			super.apply(me, o, b);
		}

		public void applyEffects(Battle b, ActivePokemon user, ActivePokemon victim) {
			Status.removeStatus(b, user, CastSource.ATTACK);
			if (!user.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				Messages.add(new MessageUpdate(user.getName() + "'s health was restored!"));
				user.healHealthFraction(.5);
				Messages.add(new MessageUpdate().updatePokemon(b, user));
			}
		}
	}
}
