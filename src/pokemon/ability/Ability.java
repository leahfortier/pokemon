package pokemon.ability;

import battle.Battle;
import battle.attack.Attack;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.attack.MoveCategory;
import battle.attack.MoveType;
import battle.effect.DamageBlocker;
import battle.effect.DefiniteEscape;
import battle.effect.ModifyStageValueEffect;
import battle.effect.StallingEffect;
import battle.effect.SwitchOutEffect;
import battle.effect.attack.ChangeAbilityMove;
import battle.effect.attack.ChangeTypeSource;
import battle.effect.generic.CastSource;
import battle.effect.generic.EffectInterfaces.AccuracyBypassEffect;
import battle.effect.generic.EffectInterfaces.AdvantageChanger;
import battle.effect.generic.EffectInterfaces.ApplyDamageEffect;
import battle.effect.generic.EffectInterfaces.BeforeTurnEffect;
import battle.effect.generic.EffectInterfaces.BracingEffect;
import battle.effect.generic.EffectInterfaces.ChangeAttackTypeEffect;
import battle.effect.generic.EffectInterfaces.ChangeTypeEffect;
import battle.effect.generic.EffectInterfaces.CrashDamageMove;
import battle.effect.generic.EffectInterfaces.CritBlockerEffect;
import battle.effect.generic.EffectInterfaces.CritStageEffect;
import battle.effect.generic.EffectInterfaces.DifferentStatEffect;
import battle.effect.generic.EffectInterfaces.EffectBlockerEffect;
import battle.effect.generic.EffectInterfaces.EndBattleEffect;
import battle.effect.generic.EffectInterfaces.EndTurnEffect;
import battle.effect.generic.EffectInterfaces.EntryEffect;
import battle.effect.generic.EffectInterfaces.HalfWeightEffect;
import battle.effect.generic.EffectInterfaces.LevitationEffect;
import battle.effect.generic.EffectInterfaces.MurderEffect;
import battle.effect.generic.EffectInterfaces.NameChanger;
import battle.effect.generic.EffectInterfaces.OpponentAccuracyBypassEffect;
import battle.effect.generic.EffectInterfaces.OpponentBeforeTurnEffect;
import battle.effect.generic.EffectInterfaces.OpponentIgnoreStageEffect;
import battle.effect.generic.EffectInterfaces.OpponentPowerChangeEffect;
import battle.effect.generic.EffectInterfaces.OpponentTrappingEffect;
import battle.effect.generic.EffectInterfaces.PhysicalContactEffect;
import battle.effect.generic.EffectInterfaces.PowerChangeEffect;
import battle.effect.generic.EffectInterfaces.PriorityChangeEffect;
import battle.effect.generic.EffectInterfaces.RecoilMove;
import battle.effect.generic.EffectInterfaces.StageChangingEffect;
import battle.effect.generic.EffectInterfaces.StatChangingEffect;
import battle.effect.generic.EffectInterfaces.StatLoweredEffect;
import battle.effect.generic.EffectInterfaces.StatProtectingEffect;
import battle.effect.generic.EffectInterfaces.StatusPreventionEffect;
import battle.effect.generic.EffectInterfaces.StatusReceivedEffect;
import battle.effect.generic.EffectInterfaces.TakeDamageEffect;
import battle.effect.generic.EffectInterfaces.TargetSwapperEffect;
import battle.effect.generic.EffectInterfaces.WeatherBlockerEffect;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.PokemonEffect;
import battle.effect.generic.Weather;
import battle.effect.holder.ItemHolder;
import battle.effect.status.Status;
import battle.effect.status.StatusCondition;
import item.Item;
import item.ItemNamesies;
import item.berry.Berry;
import item.hold.ConsumableItem;
import item.hold.HoldItem;
import item.hold.PlateItem;
import main.Global;
import main.Type;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.Gender;
import pokemon.PokemonInfo;
import pokemon.PokemonNamesies;
import pokemon.Stat;
import trainer.Trainer;
import trainer.WildPokemon;
import util.RandomUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Ability implements Serializable {
	private static final long serialVersionUID = 1L;

	protected final AbilityNamesies namesies;
	private final String description;
	
	public Ability(AbilityNamesies namesies, String description) {
		this.namesies = namesies;
		this.description = description;
	}
	
	public AbilityNamesies namesies() {
		return this.namesies;
	}
	
	public String getName() {
		return namesies.getName();
	}
	
	public String getDescription() {
		return description;
	}

	public boolean isActive() {
		return true;
	}
	
	// Called when this ability is going to changed to a different ability -- can be overidden as necessary
	public void deactivate(Battle b, ActivePokemon victim) {}
	
	// Abilities that block damage
	public static boolean blockAttack(Battle b, ActivePokemon user, ActivePokemon victim) {
		if (user.breaksTheMold()) {
			return false;
		}
		
		Ability a = victim.getAbility();
		if (a instanceof DamageBlocker) {
			DamageBlocker blockityBlock = (DamageBlocker)a;
			if (blockityBlock.block(user.getAttackType(), victim)) {
				blockityBlock.alternateEffect(b, victim);
				return true;
			}
		}
		
		return false;
	}
	
	public static Ability assign(PokemonInfo p) {
		AbilityNamesies[] abilities = p.getAbilities();
		
		if (abilities[0] == AbilityNamesies.NO_ABILITY) {
			Global.error("First ability should not be none (Pokemon " + p.getName() + ")");
		}
		
		// Only has one ability -- return the first one
		if (abilities[1] == AbilityNamesies.NO_ABILITY) {
			return abilities[0].getNewAbility();
		}

		// Has two abilties -- return a random one
		return RandomUtils.getRandomValue(abilities).getNewAbility();
	}
	
	public static Ability evolutionAssign(ActivePokemon p, PokemonInfo ev) {
		AbilityNamesies prev = p.getAbility().namesies();

		// Evolution has current ability
		if (ev.hasAbility(prev)) {
			return p.getAbility();
		}

		// Evolution only has a single ability
		AbilityNamesies[] abilities = ev.getAbilities();
		if (abilities[1] == AbilityNamesies.NO_ABILITY) {
			return abilities[0].getNewAbility();
		}

		// Evolution has the alternative
		AbilityNamesies other = getOtherAbility(p.getPokemonInfo(), prev).namesies();
		if (ev.hasAbility(other)) {
			return getOtherAbility(ev, other);
		}
		
		return RandomUtils.getRandomValue(abilities).getNewAbility();
	}
	
	public static Ability getOtherAbility(ActivePokemon p) {
		return getOtherAbility(p.getPokemonInfo(), p.getAbility().namesies());
	}
	
	private static Ability getOtherAbility(PokemonInfo p, AbilityNamesies ability) {
		if (!p.hasAbility(ability)) {
			Global.error("Incorrect ability " + ability + " for " + p.getName() + ".");
		}

		AbilityNamesies[] abilities = p.getAbilities();
		return (abilities[0] == ability ? abilities[1] : abilities[0]).getNewAbility();
	}
	
	// EVERYTHING BELOW IS GENERATED ###
	/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

	static class NoAbility extends Ability {
		private static final long serialVersionUID = 1L;

		NoAbility() {
			super(AbilityNamesies.NO_ABILITY, "None");
		}
	}

	static class Overgrow extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Overgrow() {
			super(AbilityNamesies.OVERGROW, "Powers up Grass-type moves in a pinch.");
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getHPRatio() < 1/3.0 && user.getAttackType() == Type.GRASS ? 1.5 : 1;
		}
	}

	static class Chlorophyll extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		Chlorophyll() {
			super(AbilityNamesies.CHLOROPHYLL, "Boosts the Pok\u00e9mon's Speed in sunshine.");
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SPEED;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && b.getWeather().namesies() == EffectNamesies.SUNNY) {
				stat *= 2;
			}
			
			return stat;
		}
	}

	static class Blaze extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Blaze() {
			super(AbilityNamesies.BLAZE, "Powers up Fire-type moves in a pinch.");
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getHPRatio() < 1/3.0 && user.getAttackType() == Type.FIRE ? 1.5 : 1;
		}
	}

	static class SolarPower extends Ability implements PowerChangeEffect, EndTurnEffect {
		private static final long serialVersionUID = 1L;

		SolarPower() {
			super(AbilityNamesies.SOLAR_POWER, "Boosts Sp. Atk, but lowers HP in sunshine.");
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttack().getCategory() == MoveCategory.SPECIAL && b.getWeather().namesies() == EffectNamesies.SUNNY ? 1.5 : 1;
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (b.getWeather().namesies() == EffectNamesies.SUNNY) {
				Messages.add(new MessageUpdate(victim.getName() + " lost some of its HP due to its " + this.getName() + "!"));
				victim.reduceHealthFraction(b, 1/8.0);
			}
		}
	}

	static class Torrent extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Torrent() {
			super(AbilityNamesies.TORRENT, "Powers up Water-type moves in a pinch.");
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getHPRatio() < 1/3.0 && user.getAttackType() == Type.WATER ? 1.5 : 1;
		}
	}

	static class RainDish extends Ability implements EndTurnEffect {
		private static final long serialVersionUID = 1L;

		RainDish() {
			super(AbilityNamesies.RAIN_DISH, "The Pok\u00e9mon gradually recovers HP in rain.");
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (b.getWeather().namesies() == EffectNamesies.RAINING) {
				victim.healHealthFraction(1/16.0);
				Messages.add(new MessageUpdate(victim.getName() + "'s HP was restored due to its " + this.getName() + "!").updatePokemon(b, victim));
			}
		}
	}

	static class ShieldDust extends Ability implements EffectBlockerEffect {
		private static final long serialVersionUID = 1L;

		ShieldDust() {
			super(AbilityNamesies.SHIELD_DUST, "Blocks the added effects of attacks taken.");
		}

		public boolean validMove(Battle b, ActivePokemon user, ActivePokemon victim) {
			return !user.getAttack().hasSecondaryEffects();
		}
	}

	static class ShedSkin extends Ability implements EndTurnEffect {
		private static final long serialVersionUID = 1L;

		ShedSkin() {
			super(AbilityNamesies.SHED_SKIN, "The Pok\u00e9mon may heal its own status problems.");
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasStatus() && RandomUtils.chanceTest(1, 3)) {
				Status.removeStatus(b, victim, CastSource.ABILITY);
			}
		}
	}

	static class Compoundeyes extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		Compoundeyes() {
			super(AbilityNamesies.COMPOUNDEYES, "The Pok\u00e9mon's accuracy is boosted.");
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.ACCURACY;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && true) {
				stat *= 1.3;
			}
			
			return stat;
		}
	}

	static class TintedLens extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		TintedLens() {
			super(AbilityNamesies.TINTED_LENS, "Powers up \"not very effective\" moves.");
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return Type.getAdvantage(user, victim, b) < 1 ? 2 : 1;
		}
	}

	static class Swarm extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Swarm() {
			super(AbilityNamesies.SWARM, "Powers up Bug-type moves in a pinch.");
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getHPRatio() < 1/3.0 && user.getAttackType() == Type.BUG ? 1.5 : 1;
		}
	}

	static class Sniper extends Ability {
		private static final long serialVersionUID = 1L;

		Sniper() {
			super(AbilityNamesies.SNIPER, "Powers up moves if they become critical hits.");
		}
	}

	static class KeenEye extends Ability implements StatProtectingEffect, OpponentIgnoreStageEffect {
		private static final long serialVersionUID = 1L;

		KeenEye() {
			super(AbilityNamesies.KEEN_EYE, "Prevents the Pok\u00e9mon from losing accuracy.");
		}

		public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
			return stat == Stat.ACCURACY;
		}

		public String preventionMessage(ActivePokemon p, Stat s) {
			return p.getName() + "'s " + this.getName() + " prevents its " + s.getName().toLowerCase() + " from being lowered!";
		}

		public boolean ignoreStage(Stat s) {
			return s == Stat.EVASION;
		}
	}

	static class TangledFeet extends Ability implements StageChangingEffect {
		private static final long serialVersionUID = 1L;

		TangledFeet() {
			super(AbilityNamesies.TANGLED_FEET, "Raises evasion if the Pok\u00e9mon is confused.");
		}

		public int adjustStage(Battle b,  ActivePokemon p, ActivePokemon opp, Stat s, int stage) {
			return s == Stat.EVASION && p.hasEffect(EffectNamesies.CONFUSION) ? stage + 1 : stage;
		}
	}

	static class Guts extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		Guts() {
			super(AbilityNamesies.GUTS, "Boosts Attack if there is a status problem.");
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.ATTACK;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && p.hasStatus()) {
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	static class Intimidate extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Intimidate() {
			super(AbilityNamesies.INTIMIDATE, "Lowers the foe's Attack stat.");
		}

		public void enter(Battle b, ActivePokemon enterer) {
			ActivePokemon other = b.getOtherPokemon(enterer.isPlayer());
			other.getAttributes().modifyStage(enterer, other, -1, Stat.ATTACK, b, CastSource.ABILITY);
		}
	}

	static class Static extends Ability implements PhysicalContactEffect {
		private static final long serialVersionUID = 1L;

		Static() {
			super(AbilityNamesies.STATIC, "Contact with the Pok\u00e9mon may cause paralysis.");
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (RandomUtils.chanceTest(30)) {
				Status.giveStatus(b, victim, user, StatusCondition.PARALYZED, true);
			}
		}
	}

	static class Lightningrod extends Ability implements DamageBlocker {
		private static final long serialVersionUID = 1L;

		Lightningrod() {
			super(AbilityNamesies.LIGHTNINGROD, "The Pok\u00e9mon draws in all Electric-type moves.");
		}

		public Stat toIncrease() {
			return Stat.SP_ATTACK;
		}

		public boolean block(Type attacking, ActivePokemon victim) {
			return attacking == Type.ELECTRIC;
		}

		public void alternateEffect(Battle b, ActivePokemon victim) {
			Messages.add(new MessageUpdate(victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.ELECTRIC.getName() + " type moves!"));
			victim.getAttributes().modifyStage(victim, victim, 1, toIncrease(), b, CastSource.ABILITY);
		}
	}

	static class SandVeil extends Ability implements StageChangingEffect {
		private static final long serialVersionUID = 1L;

		SandVeil() {
			super(AbilityNamesies.SAND_VEIL, "Raises the Pok\u00e9mon's evasion during a sandstorm by one level.");
		}

		public int adjustStage(Battle b,  ActivePokemon p, ActivePokemon opp, Stat s, int stage) {
			return s == Stat.EVASION && b.getWeather().namesies() == EffectNamesies.SANDSTORM ? stage + 1 : stage;
		}
	}

	static class SandRush extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		SandRush() {
			super(AbilityNamesies.SAND_RUSH, "Speed rises in a Sandstorm.");
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SPEED;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && b.getWeather().namesies() == EffectNamesies.SANDSTORM) {
				stat *= 2;
			}
			
			return stat;
		}
	}

	static class PoisonPoint extends Ability implements PhysicalContactEffect {
		private static final long serialVersionUID = 1L;

		PoisonPoint() {
			super(AbilityNamesies.POISON_POINT, "Contact with the Pok\u00e9mon may poison the foe.");
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (RandomUtils.chanceTest(30)) {
				Status.giveStatus(b, victim, user, StatusCondition.POISONED, true);
			}
		}
	}

	static class Rivalry extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Rivalry() {
			super(AbilityNamesies.RIVALRY, "Raises Attack if the foe is of the same gender.");
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getGender() == Gender.GENDERLESS) return 1;
			if (Gender.oppositeGenders(user, victim)) return .75;
			if (user.getGender() == victim.getGender()) return 1.25;
			return 1;
		}
	}

	static class CuteCharm extends Ability implements PhysicalContactEffect {
		private static final long serialVersionUID = 1L;

		CuteCharm() {
			super(AbilityNamesies.CUTE_CHARM, "Contact with the Pok\u00e9mon may cause infatuation.");
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (RandomUtils.chanceTest(30)) {
				PokemonEffect infatuated = (PokemonEffect)EffectNamesies.INFATUATED.getEffect();
				if (infatuated.applies(b, victim, user, CastSource.ABILITY)) {
					user.addEffect(infatuated);
					Messages.add(new MessageUpdate(victim.getName() + "'s " + this.getName() + " infatuated " + user.getName() + "!"));
				}
			}
		}
	}

	static class MagicGuard extends Ability implements WeatherBlockerEffect {
		private static final long serialVersionUID = 1L;

		MagicGuard() {
			super(AbilityNamesies.MAGIC_GUARD, "The Pok\u00e9mon only takes damage from attacks.");
		}

		public boolean block(EffectNamesies weather) {
			return true;
		}
	}

	static class FlashFire extends Ability implements DamageBlocker, PowerChangeEffect {
		private static final long serialVersionUID = 1L;
		private boolean activated;

		FlashFire() {
			super(AbilityNamesies.FLASH_FIRE, "Powers up Fire-type moves if hit by a fire move.");
			this.activated = false;
		}

		public boolean isActive() {
			return activated;
		}

		public boolean block(Type attacking, ActivePokemon victim) {
			return attacking == Type.FIRE;
		}

		public void alternateEffect(Battle b, ActivePokemon victim) {
			Messages.add(new MessageUpdate(victim.getName() + "'s " + this.getName() + " makes it immune to Fire type moves!"));
			activated = true;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return activated && user.getAttackType() == Type.FIRE ? 1.5 : 1;
		}
	}

	static class Drought extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Drought() {
			super(AbilityNamesies.DROUGHT, "The Pok\u00e9mon makes it sunny if it is in battle.");
		}

		public void enter(Battle b, ActivePokemon enterer) {
			b.addEffect((Weather)EffectNamesies.SUNNY.getEffect());
			Messages.add(new MessageUpdate(enterer.getName() + "'s " + this.getName() + " made the sunlight turn harsh!"));
		}
	}

	static class Frisk extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Frisk() {
			super(AbilityNamesies.FRISK, "The Pok\u00e9mon can check the foe's held item.");
		}

		public void enter(Battle b, ActivePokemon enterer) {
			ActivePokemon other = b.getOtherPokemon(enterer.isPlayer());
			if (other.isHoldingItem(b)) Messages.add(new MessageUpdate(enterer.getName() + "'s " + this.getName() + " alerted it to " + other.getName() + "'s " + other.getHeldItem(b).getName() + "!"));
		}
	}

	static class InnerFocus extends Ability {
		private static final long serialVersionUID = 1L;

		InnerFocus() {
			super(AbilityNamesies.INNER_FOCUS, "The Pok\u00e9mon is protected from flinching.");
		}
	}

	static class Infiltrator extends Ability {
		private static final long serialVersionUID = 1L;

		Infiltrator() {
			super(AbilityNamesies.INFILTRATOR, "You slip through the opponents walls and attack.");
		}
	}

	static class Stench extends Ability implements ApplyDamageEffect {
		private static final long serialVersionUID = 1L;

		Stench() {
			super(AbilityNamesies.STENCH, "The stench may cause the target to flinch.");
		}

		public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
			if (RandomUtils.chanceTest(10)) {
				PokemonEffect flinch = (PokemonEffect)EffectNamesies.FLINCH.getEffect();
				if (flinch.applies(b, user, victim, CastSource.ABILITY)) {
					flinch.cast(b, user, victim, CastSource.ABILITY, false);
					Messages.add(new MessageUpdate(user.getName() + "'s " + this.getName() + " caused " + victim.getName() + " to flinch!"));
				}
			}
		}
	}

	static class EffectSpore extends Ability implements PhysicalContactEffect {
		private static final long serialVersionUID = 1L;
		private static StatusCondition[] statuses = new StatusCondition[] {
			StatusCondition.PARALYZED,
			StatusCondition.POISONED,
			StatusCondition.ASLEEP
		};

		EffectSpore() {
			super(AbilityNamesies.EFFECT_SPORE, "Contact may paralyze, poison, or cause sleep.");
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			// Grass-type Pokemon, Pokemon with Overcoat, and Pokemon holding the Safety Goggles are immune to Effect Spore
			if (user.isType(b, Type.GRASS) || user.hasAbility(AbilityNamesies.OVERCOAT) || user.isHoldingItem(b, ItemNamesies.SAFETY_GOGGLES)) {
				return;
			}
			
			// 30% chance to Paralyze, Poison, or induce Sleep
			if (RandomUtils.chanceTest(30)) {
				Status.giveStatus(b, victim, user, RandomUtils.getRandomValue(statuses), true);
			}
		}
	}

	static class DrySkin extends Ability implements EndTurnEffect, OpponentPowerChangeEffect, DamageBlocker {
		private static final long serialVersionUID = 1L;

		DrySkin() {
			super(AbilityNamesies.DRY_SKIN, "Reduces HP if it is hot. Water restores HP.");
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (b.getWeather().namesies() == EffectNamesies.SUNNY) {
				Messages.add(new MessageUpdate(victim.getName() + " lost some of its HP due to its " + this.getName() + "!"));
				victim.reduceHealthFraction(b, 1/8.0);
			}
			else if (b.getWeather().namesies() == EffectNamesies.RAINING && !victim.fullHealth()) {
				victim.healHealthFraction(1/8.0);
				Messages.add(new MessageUpdate(victim.getName() + "'s HP was restored due to its " + this.getName() + "!").updatePokemon(b, victim));
			}
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttackType() == Type.FIRE ? 1.25 : 1;
		}

		public boolean block(Type attacking, ActivePokemon victim) {
			return attacking == Type.WATER;
		}

		public void alternateEffect(Battle b, ActivePokemon victim) {
			Messages.add(new MessageUpdate(victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.WATER.getName() + " moves!"));
			
			// Technically, according to the description, Heal Block prevents the prevention entirely (meaning this should be in Block), but that makes no sense, they shouldn't take damage, this way makes more sense
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				return;
			}
			
			victim.healHealthFraction(1/4.0);
			Messages.add(new MessageUpdate(victim.getName() + "'s HP was restored instead!").updatePokemon(b, victim));
		}
	}

	static class ArenaTrap extends Ability implements OpponentTrappingEffect {
		private static final long serialVersionUID = 1L;

		ArenaTrap() {
			super(AbilityNamesies.ARENA_TRAP, "Prevents the foe from fleeing.");
		}

		public boolean trapOpponent(Battle b, ActivePokemon escaper, ActivePokemon trapper) {
			return !escaper.isLevitating(b) && !escaper.isType(b, Type.GHOST);
		}

		public String opponentTrappingMessage(ActivePokemon escaper, ActivePokemon trapper) {
			return trapper.getName() + "'s " + this.getName() + " prevents " + escaper.getName() + " from escaping!";
		}
	}

	static class Technician extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Technician() {
			super(AbilityNamesies.TECHNICIAN, "Powers up the Pok\u00e9mon's weaker moves.");
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttackPower() <= 60 ? 1.5 : 1;
		}
	}

	static class Limber extends Ability implements StatusPreventionEffect {
		private static final long serialVersionUID = 1L;

		Limber() {
			super(AbilityNamesies.LIMBER, "The Pok\u00e9mon is protected from paralysis.");
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return status == StatusCondition.PARALYZED;
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + this.getName() + " prevents paralysis!";
		}
	}

	static class Damp extends Ability implements BeforeTurnEffect, OpponentBeforeTurnEffect {
		private static final long serialVersionUID = 1L;
		private boolean checkeroo(Battle b, ActivePokemon attacking, ActivePokemon abilify) {
			if (attacking.getAttack().namesies() == AttackNamesies.SELF_DESTRUCT || attacking.getAttack().namesies() == AttackNamesies.EXPLOSION) {
				b.printAttacking(attacking);
				Messages.add(new MessageUpdate(abilify.getName() + "'s " + this.getName() + " prevents " + attacking.getAttack().getName() + " from being used!"));
				return false;
			}
			
			return true;
		}

		Damp() {
			super(AbilityNamesies.DAMP, "Prevents combatants from self destructing.");
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			return checkeroo(b, p, p);
		}

		public boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			return checkeroo(b, p, opp);
		}
	}

	static class CloudNine extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		CloudNine() {
			super(AbilityNamesies.CLOUD_NINE, "Eliminates the effects of weather.");
		}

		public void enter(Battle b, ActivePokemon enterer) {
			// TODO: I think this isn't the intended effect of this ability
			b.addEffect((Weather)EffectNamesies.CLEAR_SKIES.getEffect());
			Messages.add(new MessageUpdate(enterer.getName() + "'s " + this.getName() + " eliminated the weather!"));
		}
	}

	static class VitalSpirit extends Ability implements StatusPreventionEffect {
		private static final long serialVersionUID = 1L;

		VitalSpirit() {
			super(AbilityNamesies.VITAL_SPIRIT, "Prevents the Pok\u00e9mon from falling asleep.");
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return status == StatusCondition.ASLEEP;
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + this.getName() + " prevents sleep!";
		}
	}

	static class Insomnia extends Ability implements StatusPreventionEffect {
		private static final long serialVersionUID = 1L;

		Insomnia() {
			super(AbilityNamesies.INSOMNIA, "Prevents the Pok\u00e9mon from falling asleep.");
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return status == StatusCondition.ASLEEP;
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + this.getName() + " prevents sleep!";
		}
	}

	static class AngerPoint extends Ability {
		private static final long serialVersionUID = 1L;

		AngerPoint() {
			super(AbilityNamesies.ANGER_POINT, "Raises Attack upon taking a critical hit.");
		}
	}

	static class Synchronize extends Ability implements StatusReceivedEffect {
		private static final long serialVersionUID = 1L;

		Synchronize() {
			super(AbilityNamesies.SYNCHRONIZE, "Passes on a burn, poison, or paralysis to the foe.");
		}

		public void receiveStatus(Battle b, ActivePokemon caster, ActivePokemon victim) {
			StatusCondition statusType = victim.getStatus().getType();
			if ((statusType == StatusCondition.BURNED || statusType == StatusCondition.POISONED || statusType == StatusCondition.PARALYZED)
			&& caster.getAttributes().isAttacking()
			&& Status.giveStatus(b, victim, caster, statusType, true)
			&& victim.hasEffect(EffectNamesies.BAD_POISON)) {
				caster.addEffect((PokemonEffect)EffectNamesies.BAD_POISON.getEffect());
			}
		}
	}

	static class NoGuard extends Ability implements AccuracyBypassEffect, OpponentAccuracyBypassEffect {
		private static final long serialVersionUID = 1L;

		NoGuard() {
			super(AbilityNamesies.NO_GUARD, "Ensures the Pok\u00e9mon and its foe's attacks land.");
		}

		public boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			// Moves always hit unless they are OHKO moves
			return !attacking.getAttack().isMoveType(MoveType.ONE_HIT_KO);
		}

		public boolean opponentBypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			// Moves always hit unless they are OHKO moves
			return !attacking.getAttack().isMoveType(MoveType.ONE_HIT_KO);
		}
	}

	static class OwnTempo extends Ability {
		private static final long serialVersionUID = 1L;

		OwnTempo() {
			super(AbilityNamesies.OWN_TEMPO, "Prevents the Pok\u00e9mon from becoming confused.");
		}
	}

	static class ClearBody extends Ability implements StatProtectingEffect {
		private static final long serialVersionUID = 1L;

		ClearBody() {
			super(AbilityNamesies.CLEAR_BODY, "Prevents the Pok\u00e9mon's stats from being lowered.");
		}

		public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
			return true;
		}

		public String preventionMessage(ActivePokemon p, Stat s) {
			return p.getName() + "'s " + this.getName() + " prevents its " + s.getName().toLowerCase() + " from being lowered!";
		}
	}

	static class LiquidOoze extends Ability {
		private static final long serialVersionUID = 1L;

		LiquidOoze() {
			super(AbilityNamesies.LIQUID_OOZE, "Inflicts damage on foes using any draining move.");
		}
	}

	static class RockHead extends Ability {
		private static final long serialVersionUID = 1L;

		RockHead() {
			super(AbilityNamesies.ROCK_HEAD, "Protects the Pok\u00e9mon from recoil damage.");
		}
	}

	static class Sturdy extends Ability implements BracingEffect {
		private static final long serialVersionUID = 1L;

		Sturdy() {
			super(AbilityNamesies.STURDY, "The Pok\u00e9mon is protected against 1-hit KO attacks.");
		}

		public boolean isBracing(Battle b, ActivePokemon bracer, boolean fullHealth) {
			return fullHealth;
		}

		public String braceMessage(ActivePokemon bracer) {
			return bracer.getName() + "'s " + this.getName() + " endured the hit!";
		}
	}

	static class Oblivious extends Ability {
		private static final long serialVersionUID = 1L;

		Oblivious() {
			super(AbilityNamesies.OBLIVIOUS, "Prevents the Pok\u00e9mon from becoming infatuated.");
		}
	}

	static class MagnetPull extends Ability implements OpponentTrappingEffect {
		private static final long serialVersionUID = 1L;

		MagnetPull() {
			super(AbilityNamesies.MAGNET_PULL, "Prevents Steel-type Pok\u00e9mon from escaping.");
		}

		public boolean trapOpponent(Battle b, ActivePokemon escaper, ActivePokemon trapper) {
			return escaper.isType(b, Type.STEEL) && !escaper.isType(b, Type.GHOST);
		}

		public String opponentTrappingMessage(ActivePokemon escaper, ActivePokemon trapper) {
			return trapper.getName() + "'s " + this.getName() + " prevents " + escaper.getName() + " from escaping!";
		}
	}

	static class Unaware extends Ability implements OpponentIgnoreStageEffect {
		private static final long serialVersionUID = 1L;

		Unaware() {
			super(AbilityNamesies.UNAWARE, "Ignores any change in ability by the foe.");
		}

		public boolean ignoreStage(Stat s) {
			return s != Stat.SPEED;
		}
	}

	static class Simple extends Ability implements ModifyStageValueEffect {
		private static final long serialVersionUID = 1L;

		Simple() {
			super(AbilityNamesies.SIMPLE, "The Pok\u00e9mon is prone to wild stat changes.");
		}

		public int modifyStageValue(int modVal) {
			return modVal*2;
		}
	}

	static class EarlyBird extends Ability {
		private static final long serialVersionUID = 1L;

		EarlyBird() {
			super(AbilityNamesies.EARLY_BIRD, "The Pok\u00e9mon awakens quickly from sleep.");
		}
	}

	static class ThickFat extends Ability implements OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		ThickFat() {
			super(AbilityNamesies.THICK_FAT, "Raises resistance to Fire-and Ice-type moves.");
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttackType() == Type.FIRE || user.getAttackType() == Type.ICE ? .5 : 1;
		}
	}

	static class Hydration extends Ability implements EndTurnEffect {
		private static final long serialVersionUID = 1L;

		Hydration() {
			super(AbilityNamesies.HYDRATION, "Heals status problems if it is raining.");
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasStatus() && b.getWeather().namesies() == EffectNamesies.RAINING) {
				Status.removeStatus(b, victim, CastSource.ABILITY);
			}
		}
	}

	static class ShellArmor extends Ability implements CritBlockerEffect {
		private static final long serialVersionUID = 1L;

		ShellArmor() {
			super(AbilityNamesies.SHELL_ARMOR, "The Pok\u00e9mon is protected against critical hits.");
		}

		public boolean blockCrits() {
			return true;
		}
	}

	static class BattleArmor extends Ability implements CritBlockerEffect {
		private static final long serialVersionUID = 1L;

		BattleArmor() {
			super(AbilityNamesies.BATTLE_ARMOR, "The Pok\u00e9mon is protected against critical hits.");
		}

		public boolean blockCrits() {
			return true;
		}
	}

	static class SkillLink extends Ability {
		private static final long serialVersionUID = 1L;

		SkillLink() {
			super(AbilityNamesies.SKILL_LINK, "Increases the frequency of multi-strike moves.");
		}
	}

	static class Levitate extends Ability implements LevitationEffect {
		private static final long serialVersionUID = 1L;

		Levitate() {
			super(AbilityNamesies.LEVITATE, "Gives full immunity to all Ground-type moves.");
		}

		public void fall(Battle b, ActivePokemon fallen) {
			Messages.add(new MessageUpdate(fallen.getName() + " is no longer levitating!"));
			
			// TODO: Fix this it's broken
			// Effect.removeEffect(fallen.getEffects(), this.namesies());
		}
	}

	static class Forewarn extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Forewarn() {
			super(AbilityNamesies.FOREWARN, "Determines what moves the foe has.");
		}

		public void enter(Battle b, ActivePokemon enterer) {
			ActivePokemon other = b.getOtherPokemon(enterer.isPlayer());
			List<Move> otherMoves = other.getMoves(b);
			
			List<AttackNamesies> besties = new ArrayList<>();
			int highestPower = -1;
			
			for (Move move : otherMoves) {
				if (move.getAttack().getCategory() == MoveCategory.STATUS) {
					continue;
				}
				
				int power = move.getAttack().getPower();
				if (power > highestPower) {
					highestPower = power;
					besties = new ArrayList<>();
					besties.add(move.getAttack().namesies());
				}
				else if (power == highestPower) {
					besties.add(move.getAttack().namesies());
				}
			}
			
			AttackNamesies warn;
			if (highestPower == -1) {
				warn = RandomUtils.getRandomValue(otherMoves).getAttack().namesies();
			}
			else {
				warn = RandomUtils.getRandomValue(besties);
			}
			
			Messages.add(new MessageUpdate(enterer.getName() + "'s " + this.getName() + " alerted it to " + other.getName() + "'s " + warn.getName() + "!"));
		}
	}

	static class HyperCutter extends Ability implements StatProtectingEffect {
		private static final long serialVersionUID = 1L;

		HyperCutter() {
			super(AbilityNamesies.HYPER_CUTTER, "Prevents the Attack stat from being lowered.");
		}

		public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
			return stat == Stat.ATTACK;
		}

		public String preventionMessage(ActivePokemon p, Stat s) {
			return p.getName() + "'s " + this.getName() + " prevents its " + s.getName().toLowerCase() + " from being lowered!";
		}
	}

	static class Soundproof extends Ability implements OpponentBeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Soundproof() {
			super(AbilityNamesies.SOUNDPROOF, "Gives full immunity to all sound-based moves.");
		}

		public boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (p.getAttack().isMoveType(MoveType.SOUND_BASED)) {
				b.printAttacking(p);
				Messages.add(new MessageUpdate(opp.getName() + "'s " + this.getName() + " prevents " + p.getAttack().getName() + " from being used!"));
				return false;
			}
			
			return true;
		}
	}

	static class Reckless extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Reckless() {
			super(AbilityNamesies.RECKLESS, "Powers up moves that have recoil damage.");
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttack() instanceof RecoilMove || user.getAttack() instanceof CrashDamageMove ? 1.2 : 1;
		}
	}

	static class IronFist extends Ability implements PowerChangeEffect, EntryEffect {
		private static final long serialVersionUID = 1L;

		IronFist() {
			super(AbilityNamesies.IRON_FIST, "Boosts the power of punching moves.");
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttack().isMoveType(MoveType.PUNCHING) ? 1.2 : 1;
		}

		public void enter(Battle b, ActivePokemon enterer) {
			if (enterer.getPokemonInfo().namesies() == PokemonNamesies.PANGORO) {
				Messages.add(new MessageUpdate(enterer.getName() + " does not break the mold!!!!!!!"));
			}
		}
	}

	static class NaturalCure extends Ability implements SwitchOutEffect {
		private static final long serialVersionUID = 1L;

		NaturalCure() {
			super(AbilityNamesies.NATURAL_CURE, "All status problems are healed upon switching out.");
		}

		public void switchOut(ActivePokemon switchee) {
			if (!switchee.hasStatus(StatusCondition.FAINTED)) {
				switchee.removeStatus();
			}
		}
	}

	static class SereneGrace extends Ability {
		private static final long serialVersionUID = 1L;

		SereneGrace() {
			super(AbilityNamesies.SERENE_GRACE, "Boosts the likelihood of added effects appearing.");
		}
	}

	static class LeafGuard extends Ability implements StatusPreventionEffect {
		private static final long serialVersionUID = 1L;

		LeafGuard() {
			super(AbilityNamesies.LEAF_GUARD, "Prevents status problems in sunny weather.");
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return b.getWeather().namesies() == EffectNamesies.SUNNY;
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + this.getName() + " prevents status conditions!";
		}
	}

	static class Scrappy extends Ability implements AdvantageChanger {
		private static final long serialVersionUID = 1L;

		Scrappy() {
			super(AbilityNamesies.SCRAPPY, "Enables moves to hit Ghost-type foes.");
		}

		public Type[] getAdvantageChange(Type attacking, Type[] defending) {
			for (int i = 0; i < 2; i++) {
				if ((attacking == Type.NORMAL || attacking == Type.FIGHTING) && defending[i] == Type.GHOST) {
					defending[i] = Type.NO_TYPE;
				}
			}
			
			return defending;
		}
	}

	static class SwiftSwim extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		SwiftSwim() {
			super(AbilityNamesies.SWIFT_SWIM, "Boosts the Pok\u00e9mon's Speed in rain.");
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SPEED;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && b.getWeather().namesies() == EffectNamesies.RAINING) {
				stat *= 2;
			}
			
			return stat;
		}
	}

	static class WaterVeil extends Ability implements StatusPreventionEffect {
		private static final long serialVersionUID = 1L;

		WaterVeil() {
			super(AbilityNamesies.WATER_VEIL, "Prevents the Pok\u00e9mon from getting a burn.");
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return status == StatusCondition.BURNED;
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + this.getName() + " prevents burns!";
		}
	}

	static class Filter extends Ability implements OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Filter() {
			super(AbilityNamesies.FILTER, "Powers down super-effective moves.");
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return Type.getAdvantage(user, victim, b) > 1 ? .75 : 1;
		}
	}

	static class FlameBody extends Ability implements PhysicalContactEffect {
		private static final long serialVersionUID = 1L;

		FlameBody() {
			super(AbilityNamesies.FLAME_BODY, "Contact with the Pok\u00e9mon may burn the foe.");
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (RandomUtils.chanceTest(30)) {
				Status.giveStatus(b, victim, user, StatusCondition.BURNED, true);
			}
		}
	}

	static class Rattled extends Ability implements TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		Rattled() {
			super(AbilityNamesies.RATTLED, "Some move types scare it and boost its Speed.");
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			Type type = user.getAttackType();
			if (type == Type.BUG || type == Type.DARK || type == Type.GHOST) {
				victim.getAttributes().modifyStage(victim, victim, 1, Stat.SPEED, b, CastSource.ABILITY);
			}
		}
	}

	static class Moxie extends Ability implements MurderEffect {
		private static final long serialVersionUID = 1L;

		Moxie() {
			super(AbilityNamesies.MOXIE, "Attack rises when you knock out an opponent.");
		}

		public void killWish(Battle b, ActivePokemon dead, ActivePokemon murderer) {
			murderer.getAttributes().modifyStage(murderer, murderer, 1, Stat.ATTACK, b, CastSource.ABILITY);
		}
	}

	static class Imposter extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Imposter() {
			super(AbilityNamesies.IMPOSTER, "It transforms itself into the Pok\u00e9mon it is facing.");
		}

		public void enter(Battle b, ActivePokemon enterer) {
			EffectNamesies.TRANSFORMED.getEffect().cast(b, enterer, enterer, CastSource.ABILITY, false);
		}
	}

	static class Adaptability extends Ability {
		private static final long serialVersionUID = 1L;

		Adaptability() {
			super(AbilityNamesies.ADAPTABILITY, "Powers up moves of the same type.");
		}
	}

	static class WaterAbsorb extends Ability implements DamageBlocker {
		private static final long serialVersionUID = 1L;

		WaterAbsorb() {
			super(AbilityNamesies.WATER_ABSORB, "Restores HP if hit by a Water-type move.");
		}

		public boolean block(Type attacking, ActivePokemon victim) {
			return attacking == Type.WATER;
		}

		public void alternateEffect(Battle b, ActivePokemon victim) {
			Messages.add(new MessageUpdate(victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.WATER.getName() + " moves!"));
			
			// Technically, according to the description, Heal Block prevents the prevention entirely (meaning this should be in Block), but that makes no sense, they shouldn't take damage, this way makes more sense
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				return;
			}
			
			victim.healHealthFraction(1/4.0);
			Messages.add(new MessageUpdate(victim.getName() + "'s HP was restored instead!").updatePokemon(b, victim));
		}
	}

	static class VoltAbsorb extends Ability implements DamageBlocker {
		private static final long serialVersionUID = 1L;

		VoltAbsorb() {
			super(AbilityNamesies.VOLT_ABSORB, "Restores HP if hit by an Electric-type move.");
		}

		public boolean block(Type attacking, ActivePokemon victim) {
			return attacking == Type.ELECTRIC;
		}

		public void alternateEffect(Battle b, ActivePokemon victim) {
			Messages.add(new MessageUpdate(victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.ELECTRIC.getName() + " moves!"));
			
			// Technically, according to the description, Heal Block prevents the prevention entirely (meaning this should be in Block), but that makes no sense, they shouldn't take damage, this way makes more sense
			if (victim.fullHealth() || victim.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				return;
			}
			
			victim.healHealthFraction(1/4.0);
			Messages.add(new MessageUpdate(victim.getName() + "'s HP was restored instead!").updatePokemon(b, victim));
		}
	}

	static class QuickFeet extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		QuickFeet() {
			super(AbilityNamesies.QUICK_FEET, "Boosts Speed if there is a status problem.");
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SPEED;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && p.hasStatus()) {
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	static class Trace extends Ability implements EntryEffect, ChangeAbilityMove {
		private static final long serialVersionUID = 1L;

		Trace() {
			super(AbilityNamesies.TRACE, "The Pok\u00e9mon copies the foe's ability.");
		}

		public void enter(Battle b, ActivePokemon enterer) {
			ActivePokemon other = b.getOtherPokemon(enterer.isPlayer());
			if (other.hasAbility(AbilityNamesies.MULTITYPE) || other.hasAbility(AbilityNamesies.ILLUSION) || other.hasAbility(AbilityNamesies.STANCE_CHANGE) || other.hasAbility(AbilityNamesies.IMPOSTER) || other.hasAbility(this.namesies)) {
				return;
			}
			
			EffectNamesies.CHANGE_ABILITY.getEffect().cast(b, enterer, enterer, CastSource.ABILITY, true);
		}

		public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim) {
			Ability otherAbility = b.getOtherPokemon(victim.isPlayer()).getAbility();
			return otherAbility.namesies().getNewAbility();
		}

		public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
			ActivePokemon other = b.getOtherPokemon(victim.isPlayer());
			return victim.getName() + " traced " + other.getName() + "'s " + other.getAbility().getName() + "!";
		}
	}

	static class Download extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Download() {
			super(AbilityNamesies.DOWNLOAD, "Adjusts power according to a foe's defenses.");
		}

		public void enter(Battle b, ActivePokemon enterer) {
			ActivePokemon other = b.getOtherPokemon(enterer.isPlayer());
			PokemonInfo otherInfo = PokemonInfo.getPokemonInfo(other.getPokemonInfo().namesies());
			
			int baseDefense = otherInfo.getStat(Stat.DEFENSE.index());
			int baseSpecialDefense = otherInfo.getStat(Stat.SP_DEFENSE.index());
			
			Stat toRaise = baseDefense < baseSpecialDefense ? Stat.ATTACK : Stat.SP_ATTACK;
			
			enterer.getAttributes().modifyStage(enterer, enterer, 1, toRaise, b, CastSource.ABILITY);
		}
	}

	static class Pressure extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Pressure() {
			super(AbilityNamesies.PRESSURE, "The Pok\u00e9mon raises the foe's PP usage.");
		}

		public void enter(Battle b, ActivePokemon enterer) {
			Messages.add(new MessageUpdate(enterer.getName() + " is exerting pressure!"));
		}
	}

	static class Immunity extends Ability implements StatusPreventionEffect {
		private static final long serialVersionUID = 1L;

		Immunity() {
			super(AbilityNamesies.IMMUNITY, "Prevents the Pok\u00e9mon from getting poisoned.");
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return status == StatusCondition.POISONED;
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + this.getName() + " prevents poison!";
		}
	}

	static class SnowCloak extends Ability implements StageChangingEffect {
		private static final long serialVersionUID = 1L;

		SnowCloak() {
			super(AbilityNamesies.SNOW_CLOAK, "Raises the Pok\u00e9mon's evasion during a hailstorm by one level.");
		}

		public int adjustStage(Battle b,  ActivePokemon p, ActivePokemon opp, Stat s, int stage) {
			return s == Stat.EVASION && b.getWeather().namesies() == EffectNamesies.HAILING ? stage + 1 : stage;
		}
	}

	static class MarvelScale extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		MarvelScale() {
			super(AbilityNamesies.MARVEL_SCALE, "Boosts Defense if there is a status problem.");
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.DEFENSE;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && p.hasStatus()) {
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	static class Multiscale extends Ability implements OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Multiscale() {
			super(AbilityNamesies.MULTISCALE, "When this Pok\u00e9mon is at full HP, damage is lessened.");
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return victim.fullHealth() ? .5 : 1;
		}
	}

	static class SheerForce extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		SheerForce() {
			super(AbilityNamesies.SHEER_FORCE, "Attacks gain power, but lose their secondary effect.");
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttack().hasSecondaryEffects() ? 1.3 : 1;
		}
	}

	static class Hustle extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		Hustle() {
			super(AbilityNamesies.HUSTLE, "Boosts the Attack stat, but lowers accuracy.");
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (s == Stat.ATTACK) {
				stat *= 1.5;
			}
			else if (s == Stat.ACCURACY) {
				stat *= .8;
			}
			
			return stat;
		}
	}

	static class HugePower extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		HugePower() {
			super(AbilityNamesies.HUGE_POWER, "Raises the Pok\u00e9mon's Attack stat.");
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.ATTACK;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && true) {
				stat *= 2;
			}
			
			return stat;
		}
	}

	static class SpeedBoost extends Ability implements EndTurnEffect {
		private static final long serialVersionUID = 1L;

		SpeedBoost() {
			super(AbilityNamesies.SPEED_BOOST, "The Pok\u00e9mon's Speed stat is gradually boosted.");
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			victim.getAttributes().modifyStage(victim, victim, 1, Stat.SPEED, b, CastSource.ABILITY);
		}
	}

	static class MagicBounce extends Ability implements TargetSwapperEffect {
		private static final long serialVersionUID = 1L;

		MagicBounce() {
			super(AbilityNamesies.MAGIC_BOUNCE, "Reflects status-changing moves.");
		}

		public boolean swapTarget(Battle b, ActivePokemon user, ActivePokemon opponent) {
			Attack attack = user.getAttack();
			if (!attack.isSelfTarget() && attack.getCategory() == MoveCategory.STATUS && !attack.isMoveType(MoveType.NO_MAGIC_COAT)) {
				Messages.add(new MessageUpdate(opponent.getName() + "'s " + this.getName() + " reflected " + user.getName() + "'s move!"));
				return true;
			}
			
			return false;
		}
	}

	static class SuperLuck extends Ability implements CritStageEffect {
		private static final long serialVersionUID = 1L;

		SuperLuck() {
			super(AbilityNamesies.SUPER_LUCK, "Heightens the critical-hit ratios of moves.");
		}

		public int increaseCritStage(int stage, ActivePokemon p) {
			return stage + 1;
		}
	}

	static class ShadowTag extends Ability implements OpponentTrappingEffect {
		private static final long serialVersionUID = 1L;

		ShadowTag() {
			super(AbilityNamesies.SHADOW_TAG, "Prevents the foe from escaping.");
		}

		public boolean trapOpponent(Battle b, ActivePokemon escaper, ActivePokemon trapper) {
			return !escaper.hasAbility(this.namesies) && !escaper.isType(b, Type.GHOST);
		}

		public String opponentTrappingMessage(ActivePokemon escaper, ActivePokemon trapper) {
			return trapper.getName() + "'s " + this.getName() + " prevents " + escaper.getName() + " from escaping!";
		}
	}

	static class Overcoat extends Ability implements WeatherBlockerEffect, EffectBlockerEffect {
		private static final long serialVersionUID = 1L;

		Overcoat() {
			super(AbilityNamesies.OVERCOAT, "Protects the Pok\u00e9mon from damage from weather.");
		}

		public String getPreventMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + this.getName() + " protects it from powder moves!";
		}

		public boolean block(EffectNamesies weather) {
			return true;
		}

		public boolean validMove(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (!user.getAttack().isMoveType(MoveType.POWDER)) {
				return true;
			}
			
			if (user.getAttack().getCategory() == MoveCategory.STATUS) {
				Messages.add(new MessageUpdate(getPreventMessage(victim)));
			}
			
			return false;
		}
	}

	static class MagmaArmor extends Ability implements StatusPreventionEffect {
		private static final long serialVersionUID = 1L;

		MagmaArmor() {
			super(AbilityNamesies.MAGMA_ARMOR, "Prevents the Pok\u00e9mon from becoming frozen.");
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return status == StatusCondition.FROZEN;
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + this.getName() + " prevents freezing!";
		}
	}

	static class SuctionCups extends Ability {
		private static final long serialVersionUID = 1L;

		SuctionCups() {
			super(AbilityNamesies.SUCTION_CUPS, "Negates all moves that force switching out.");
		}
	}

	static class Steadfast extends Ability {
		private static final long serialVersionUID = 1L;

		Steadfast() {
			super(AbilityNamesies.STEADFAST, "Raises Speed each time the Pok\u00e9mon flinches.");
		}
	}

	static class SandStream extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		SandStream() {
			super(AbilityNamesies.SAND_STREAM, "The Pok\u00e9mon summons a sandstorm in battle.");
		}

		public void enter(Battle b, ActivePokemon enterer) {
			b.addEffect((Weather)EffectNamesies.SANDSTORM.getEffect());
			Messages.add(new MessageUpdate(enterer.getName() + "'s " + this.getName() + " whipped up a sandstorm!"));
		}
	}

	static class Regenerator extends Ability implements SwitchOutEffect {
		private static final long serialVersionUID = 1L;

		Regenerator() {
			super(AbilityNamesies.REGENERATOR, "Restores a little HP when withdrawn from battle.");
		}

		public void switchOut(ActivePokemon switchee) {
			if (!switchee.hasStatus(StatusCondition.FAINTED)) {
				switchee.healHealthFraction(1/3.0);
			}
		}
	}

	static class PoisonHeal extends Ability {
		private static final long serialVersionUID = 1L;

		PoisonHeal() {
			super(AbilityNamesies.POISON_HEAL, "Restores HP if the Pok\u00e9mon is poisoned.");
		}
	}

	static class Truant extends Ability implements EndTurnEffect, BeforeTurnEffect {
		private static final long serialVersionUID = 1L;
		private boolean lazyface;

		Truant() {
			super(AbilityNamesies.TRUANT, "Pok\u00e9mon can't attack on consecutive turns.");
			this.lazyface = false;
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasStatus(StatusCondition.ASLEEP)) {
				lazyface = false;
			}
			else {
				lazyface = !lazyface;
			}
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (lazyface) {
				Messages.add(new MessageUpdate(p.getName() + " is loafing around!"));
				return false;
			}
			
			return true;
		}
	}

	static class WonderGuard extends Ability implements OpponentBeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		WonderGuard() {
			super(AbilityNamesies.WONDER_GUARD, "Only supereffective moves will hit.");
		}

		public boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			// Status moves always hit
			if (p.getAttack().getCategory() == MoveCategory.STATUS) {
				return true;
			}
			
			// Super effective moves hit
			if (Type.getAdvantage(p, opp, b) > 1) {
				return true;
			}
			
			// None-type moves always hit
			if (p.isAttackType(Type.NO_TYPE)) {
				return true;
			}
			
			// Immunity Solunity
			b.printAttacking(p);
			Messages.add(new MessageUpdate(opp.getName() + "'s " + this.getName() + " makes it immune to " + p.getAttack().getName() + "!"));
			return false;
		}
	}

	static class Normalize extends Ability implements ChangeAttackTypeEffect {
		private static final long serialVersionUID = 1L;

		Normalize() {
			super(AbilityNamesies.NORMALIZE, "All the Pok\u00e9mon's moves become the Normal type.");
		}

		public Type changeAttackType(Type original) {
			return Type.NORMAL;
		}
	}

	static class Stall extends Ability implements StallingEffect {
		private static final long serialVersionUID = 1L;

		Stall() {
			super(AbilityNamesies.STALL, "The Pok\u00e9mon moves after even slower foes.");
		}
	}

	static class PurePower extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		PurePower() {
			super(AbilityNamesies.PURE_POWER, "Raises the Pok\u00e9mon's Attack stat.");
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.ATTACK;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && true) {
				stat *= 2;
			}
			
			return stat;
		}
	}

	static class RoughSkin extends Ability implements PhysicalContactEffect {
		private static final long serialVersionUID = 1L;

		RoughSkin() {
			super(AbilityNamesies.ROUGH_SKIN, "Inflicts damage to the foe on contact.");
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			Messages.add(new MessageUpdate(user.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!"));
			user.reduceHealthFraction(b, 1/8.0);
		}
	}

	static class SolidRock extends Ability implements OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		SolidRock() {
			super(AbilityNamesies.SOLID_ROCK, "Reduces damage from supereffective attacks.");
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return Type.getAdvantage(user, victim, b) < 1 ? .75 : 1;
		}
	}

	static class WhiteSmoke extends Ability implements StatProtectingEffect {
		private static final long serialVersionUID = 1L;

		WhiteSmoke() {
			super(AbilityNamesies.WHITE_SMOKE, "Prevents other Pok\u00e9mon from lowering its stats.");
		}

		public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
			return true;
		}

		public String preventionMessage(ActivePokemon p, Stat s) {
			return p.getName() + "'s " + this.getName() + " prevents its " + s.getName().toLowerCase() + " from being lowered!";
		}
	}

	static class ToxicBoost extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		ToxicBoost() {
			super(AbilityNamesies.TOXIC_BOOST, "Powers up physical attacks when poisoned.");
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.ATTACK;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && p.hasStatus(StatusCondition.POISONED)) {
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	static class Anticipation extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Anticipation() {
			super(AbilityNamesies.ANTICIPATION, "Senses a foe's dangerous moves.");
		}

		public void enter(Battle b, ActivePokemon enterer) {
			ActivePokemon other = b.getOtherPokemon(enterer.isPlayer());
			for (Move m : other.getMoves(b)) {
				Attack attack = m.getAttack();
				if (Type.getBasicAdvantage(attack.getActualType(), enterer, b) > 1 || attack.isMoveType(MoveType.ONE_HIT_KO)) {
					// TODO: Shouldn't this be for a random move?
					Messages.add(new MessageUpdate(enterer.getName() + "'s " + this.getName() + " made it shudder!"));
					break;
				}
			}
		}
	}

	static class StormDrain extends Ability implements DamageBlocker {
		private static final long serialVersionUID = 1L;

		StormDrain() {
			super(AbilityNamesies.STORM_DRAIN, "Draws in all Water-type moves to up Sp. Attack.");
		}

		public Stat toIncrease() {
			return Stat.SP_ATTACK;
		}

		public boolean block(Type attacking, ActivePokemon victim) {
			return attacking == Type.WATER;
		}

		public void alternateEffect(Battle b, ActivePokemon victim) {
			Messages.add(new MessageUpdate(victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.WATER.getName() + " type moves!"));
			victim.getAttributes().modifyStage(victim, victim, 1, toIncrease(), b, CastSource.ABILITY);
		}
	}

	static class ColorChange extends Ability implements TakeDamageEffect, ChangeTypeSource {
		private static final long serialVersionUID = 1L;
		private Type type;

		ColorChange() {
			super(AbilityNamesies.COLOR_CHANGE, "Changes the Pok\u00e9mon's type to the foe's move.");
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			Type t = user.getAttackType();
			if (!victim.isType(b, t)) {
				type = t;
				EffectNamesies.CHANGE_TYPE.getEffect().cast(b, victim, victim, CastSource.ABILITY, true);
			}
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
			return new Type[] { type, Type.NO_TYPE };
		}
	}

	static class IceBody extends Ability implements EndTurnEffect, WeatherBlockerEffect {
		private static final long serialVersionUID = 1L;

		IceBody() {
			super(AbilityNamesies.ICE_BODY, "The Pok\u00e9mon gradually regains HP in a hailstorm.");
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (b.getWeather().namesies() == EffectNamesies.HAILING) {
				victim.healHealthFraction(1/16.0);
				Messages.add(new MessageUpdate(victim.getName() + "'s HP was restored due to its " + this.getName() + "!").updatePokemon(b, victim));
			}
		}

		public boolean block(EffectNamesies weather) {
			return weather == EffectNamesies.HAILING;
		}
	}

	static class LightMetal extends Ability implements HalfWeightEffect {
		private static final long serialVersionUID = 1L;

		LightMetal() {
			super(AbilityNamesies.LIGHT_METAL, "Halves the Pok\u00e9mon's weight.");
		}

		public int getHalfAmount(int halfAmount) {
			return halfAmount + 1;
		}
	}

	static class Drizzle extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Drizzle() {
			super(AbilityNamesies.DRIZZLE, "The Pok\u00e9mon makes it rain if it appears in battle.");
		}

		public void enter(Battle b, ActivePokemon enterer) {
			b.addEffect((Weather)EffectNamesies.RAINING.getEffect());
			Messages.add(new MessageUpdate(enterer.getName() + "'s " + this.getName() + " started a downpour!"));
		}
	}

	static class AirLock extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		AirLock() {
			super(AbilityNamesies.AIR_LOCK, "Eliminates the effects of weather.");
		}

		public void enter(Battle b, ActivePokemon enterer) {
			// TODO: I think this isn't the intended effect of this ability
			b.addEffect((Weather)EffectNamesies.CLEAR_SKIES.getEffect());
			Messages.add(new MessageUpdate(enterer.getName() + "'s " + this.getName() + " eliminated the weather!"));
		}
	}

	static class Defiant extends Ability implements StatLoweredEffect {
		private static final long serialVersionUID = 1L;

		Defiant() {
			super(AbilityNamesies.DEFIANT, "Boosts the Attack stat when a stat is lowered.");
		}

		public void takeItToTheNextLevel(Battle b, ActivePokemon caster, ActivePokemon victim) {
			victim.getAttributes().modifyStage(victim, victim, 2, Stat.ATTACK, b, CastSource.ABILITY);
		}
	}

	static class Competitive extends Ability implements StatLoweredEffect {
		private static final long serialVersionUID = 1L;

		Competitive() {
			super(AbilityNamesies.COMPETITIVE, "Boosts the Sp. Atk stat when a stat is lowered.");
		}

		public void takeItToTheNextLevel(Battle b, ActivePokemon caster, ActivePokemon victim) {
			victim.getAttributes().modifyStage(victim, victim, 2, Stat.SP_ATTACK, b, CastSource.ABILITY);
		}
	}

	static class FlowerGift extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		FlowerGift() {
			super(AbilityNamesies.FLOWER_GIFT, "Powers up party Pok\u00e9mon when it is sunny.");
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.ATTACK || s == Stat.SP_DEFENSE;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && b.getWeather().namesies() == EffectNamesies.SUNNY) {
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	static class Aftermath extends Ability implements PhysicalContactEffect {
		private static final long serialVersionUID = 1L;

		Aftermath() {
			super(AbilityNamesies.AFTERMATH, "Damages the attacker landing the finishing hit.");
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			// TODO: Pretty sure this doesn't work anymore
			if (victim.isFainted(b)) {
				Messages.add(new MessageUpdate(user.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!"));
				user.reduceHealthFraction(b, 1/4.0);
			}
		}
	}

	static class Heatproof extends Ability implements OpponentPowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Heatproof() {
			super(AbilityNamesies.HEATPROOF, "Weakens the power of Fire-type moves.");
		}

		public double getOpponentMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttackType() == Type.FIRE ? .5 : 1;
		}
	}

	static class SandForce extends Ability implements PowerChangeEffect, WeatherBlockerEffect {
		private static final long serialVersionUID = 1L;

		SandForce() {
			super(AbilityNamesies.SAND_FORCE, "Boosts certain moves' power in a sandstorm.");
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			Type type = user.getAttackType();
			return (type == Type.ROCK || type == Type.STEEL || type == Type.GROUND) && b.getWeather().namesies() == EffectNamesies.SANDSTORM ? 1.3 : 1;
		}

		public boolean block(EffectNamesies weather) {
			return weather == EffectNamesies.SANDSTORM;
		}
	}

	static class SnowWarning extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		SnowWarning() {
			super(AbilityNamesies.SNOW_WARNING, "The Pok\u00e9mon summons a hailstorm in battle.");
		}

		public void enter(Battle b, ActivePokemon enterer) {
			b.addEffect((Weather)EffectNamesies.HAILING.getEffect());
			Messages.add(new MessageUpdate(enterer.getName() + "'s " + this.getName() + " caused it to hail!"));
		}
	}

	static class MotorDrive extends Ability implements DamageBlocker {
		private static final long serialVersionUID = 1L;

		MotorDrive() {
			super(AbilityNamesies.MOTOR_DRIVE, "Raises Speed if hit by an Electric-type move.");
		}

		public Stat toIncrease() {
			return Stat.SPEED;
		}

		public boolean block(Type attacking, ActivePokemon victim) {
			return attacking == Type.ELECTRIC;
		}

		public void alternateEffect(Battle b, ActivePokemon victim) {
			Messages.add(new MessageUpdate(victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.ELECTRIC.getName() + " type moves!"));
			victim.getAttributes().modifyStage(victim, victim, 1, toIncrease(), b, CastSource.ABILITY);
		}
	}

	static class Justified extends Ability implements TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		Justified() {
			super(AbilityNamesies.JUSTIFIED, "Raises Attack when hit by a Dark-type move.");
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == Type.DARK) {
				victim.getAttributes().modifyStage(victim, victim, 1, Stat.ATTACK, b, CastSource.ABILITY);
			}
		}
	}

	static class CursedBody extends Ability implements PhysicalContactEffect {
		private static final long serialVersionUID = 1L;

		CursedBody() {
			super(AbilityNamesies.CURSED_BODY, "May disable a move used on the Pok\u00e9mon.");
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (RandomUtils.chanceTest(30)) {
				user.getAttributes().setLastMoveUsed();
				PokemonEffect disable = (PokemonEffect)EffectNamesies.DISABLE.getEffect();
				if (disable.applies(b, victim, user, CastSource.ABILITY)) {
					disable.cast(b, victim, user, CastSource.ABILITY, false);
					Messages.add(new MessageUpdate(victim.getName() + "'s " + this.getName() + " disabled " + user.getName() + "'s " + user.getAttack().getName()));
				}
			}
		}
	}

	static class SlowStart extends Ability implements EndTurnEffect, EntryEffect, StatChangingEffect {
		private static final long serialVersionUID = 1L;
		int count;

		SlowStart() {
			super(AbilityNamesies.SLOW_START, "Temporarily halves Attack and Speed.");
			this.count = 0;
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.ATTACK || s == Stat.SPEED;
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			count++;
		}

		public void enter(Battle b, ActivePokemon enterer) {
			count = 0;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && count < 5) {
				stat *= .5;
			}
			
			return stat;
		}
	}

	static class BadDreams extends Ability implements EndTurnEffect {
		private static final long serialVersionUID = 1L;

		BadDreams() {
			super(AbilityNamesies.BAD_DREAMS, "Reduces a sleeping foe's HP.");
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			ActivePokemon other = b.getOtherPokemon(victim.isPlayer());
			if (other.hasStatus(StatusCondition.ASLEEP)) {
				Messages.add(new MessageUpdate(other.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!"));
				other.reduceHealthFraction(b, 1/8.0);
			}
		}
	}

	static class VictoryStar extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		VictoryStar() {
			super(AbilityNamesies.VICTORY_STAR, "Boosts the accuracy of its allies and itself.");
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.ACCURACY;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && true) {
				stat *= 1.1;
			}
			
			return stat;
		}
	}

	static class Contrary extends Ability implements ModifyStageValueEffect {
		private static final long serialVersionUID = 1L;

		Contrary() {
			super(AbilityNamesies.CONTRARY, "Makes stat changes have an opposite effect.");
		}

		public int modifyStageValue(int modVal) {
			return modVal*(modVal < 0 ? -1 : 1);
		}
	}

	static class BigPecks extends Ability implements StatProtectingEffect {
		private static final long serialVersionUID = 1L;

		BigPecks() {
			super(AbilityNamesies.BIG_PECKS, "Protects the Pok\u00e9mon from Defense-lowering attacks.");
		}

		public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
			return stat == Stat.DEFENSE;
		}

		public String preventionMessage(ActivePokemon p, Stat s) {
			return p.getName() + "'s " + this.getName() + " prevents its " + s.getName().toLowerCase() + " from being lowered!";
		}
	}

	static class PoisonTouch extends Ability implements ApplyDamageEffect {
		private static final long serialVersionUID = 1L;

		PoisonTouch() {
			super(AbilityNamesies.POISON_TOUCH, "May poison targets when a Pok\u00e9mon makes contact.");
		}

		public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
			if (RandomUtils.chanceTest(30)) {
				Status.giveStatus(b, user, victim, StatusCondition.POISONED, true);
			}
		}
	}

	static class Prankster extends Ability implements PriorityChangeEffect {
		private static final long serialVersionUID = 1L;

		Prankster() {
			super(AbilityNamesies.PRANKSTER, "Gives priority to a status move.");
		}

		public int changePriority(Battle b, ActivePokemon user, int priority) {
			if (user.getAttack().getCategory() == MoveCategory.STATUS) {
				if (this instanceof ConsumableItem) {
					user.consumeItem(b);
				}
				
				priority++;
			}
			
			return priority;
		}
	}

	static class WonderSkin extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		WonderSkin() {
			super(AbilityNamesies.WONDER_SKIN, "Makes status-changing moves more likely to miss.");
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.EVASION;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && opp.getAttack().getCategory() == MoveCategory.STATUS) {
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	static class Mummy extends Ability implements PhysicalContactEffect, ChangeAbilityMove {
		private static final long serialVersionUID = 1L;

		Mummy() {
			super(AbilityNamesies.MUMMY, "Contact with this Pok\u00e9mon spreads this Ability.");
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.hasAbility(this.namesies) || user.hasAbility(AbilityNamesies.MULTITYPE) || user.hasAbility(AbilityNamesies.STANCE_CHANGE)) {
				return;
			}
			
			// Cast the change ability effect onto the user
			EffectNamesies.CHANGE_ABILITY.getEffect().cast(b, victim, user, CastSource.ABILITY, true);
		}

		public Ability getAbility(Battle b, ActivePokemon caster, ActivePokemon victim) {
			return AbilityNamesies.MUMMY.getNewAbility();
		}

		public String getMessage(Battle b, ActivePokemon caster, ActivePokemon victim) {
			return victim.getName() + "'s ability was changed to " + this.namesies().getName() + "!";
		}
	}

	static class Defeatist extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Defeatist() {
			super(AbilityNamesies.DEFEATIST, "Lowers stats when HP becomes half or less.");
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getHPRatio() < 1/2.0 ? .5 : 1;
		}
	}

	static class WeakArmor extends Ability implements TakeDamageEffect {
		private static final long serialVersionUID = 1L;

		WeakArmor() {
			super(AbilityNamesies.WEAK_ARMOR, "Physical attacks lower Defense and raise Speed.");
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttack().getCategory() == MoveCategory.PHYSICAL) {
				victim.getAttributes().modifyStage(victim, victim, -1, Stat.DEFENSE, b, CastSource.ABILITY);
				victim.getAttributes().modifyStage(victim, victim, 1, Stat.SPEED, b, CastSource.ABILITY);
			}
		}
	}

	static class Illusion extends Ability implements EntryEffect, SwitchOutEffect, TakeDamageEffect, ChangeTypeEffect, NameChanger {
		private static final long serialVersionUID = 1L;
		private boolean activated;
		private String illusionName;
		private Type[] illusionType;
		private PokemonInfo illusionSpecies;
		private boolean illusionShiny;
		
		private void breakIllusion(Battle b, ActivePokemon victim) {
			// If the Illusion is already broken, no worries
			if (!activated) {
				return;
			}
			
			activated = false;
			Messages.add(new MessageUpdate(victim.getName() + "'s Illusion was broken!"));
			
			Messages.add(new MessageUpdate().withNewPokemon(victim.getPokemonInfo(), victim.isShiny(), true, victim.isPlayer()));
			Messages.add(new MessageUpdate().updatePokemon(b, victim));
		}

		Illusion() {
			super(AbilityNamesies.ILLUSION, "Comes out disguised as the Pok\u00e9mon in back.");
			this.activated = false;
		}

		public boolean isActive() {
			return activated;
		}

		public void deactivate(Battle b, ActivePokemon victim) {
			breakIllusion(b, victim);
		}

		public void enter(Battle b, ActivePokemon enterer) {
			// No Illusion today...
			if (!activated) {
				return;
			}
			
			// Display the Illusion changes
			Messages.add(new MessageUpdate().withNewPokemon(illusionSpecies, illusionShiny, false, enterer.isPlayer()));
			Messages.add(new MessageUpdate().updatePokemon(b, enterer));
		}

		public void switchOut(ActivePokemon switchee) {
			activated = false;
		}

		public void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim) {
			breakIllusion(b, victim);
		}

		public Type[] getType(Battle b, ActivePokemon p, boolean display) {
			if (display && activated) {
				return illusionType;
			}
			
			return p.getActualType();
		}

		public String getNameChange() {
			return activated ? illusionName : null;
		}

		public void setNameChange(Battle b, ActivePokemon victim) {
			List<ActivePokemon> team = b.getTrainer(victim.isPlayer()).getTeam();
			ActivePokemon illusion = null;
			
			// Starting from the back of the party, locate the first conscious Pokemon that is of a different species to be the illusion
			for (int i = team.size() - 1; i > 0; i--) {
				ActivePokemon temp = team.get(i);
				
				// If the Pokemon in back cannot fight for any reason -- do nothing
				if (!temp.canFight()) {
					continue;
				}
				
				// If the Pokemon in back is the same species at the current Pokemon -- do nothing
				if (temp.getPokemonInfo().getNumber() == victim.getPokemonInfo().getNumber()) {
					continue;
				}
				
				// Otherwise, we've found our Illusion!
				illusion = temp;
				break;
			}
			
			// No valid Pokemon to be as an illusion -- do not activate
			if (illusion == null) {
				return;
			}
			
			// Otherwise, we're in the illusion
			activated = true;
			
			illusionName = illusion.getName();
			illusionType = illusion.getActualType();
			illusionSpecies = illusion.getPokemonInfo();
			illusionShiny = illusion.isShiny();
		}
	}

	static class Analytic extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Analytic() {
			super(AbilityNamesies.ANALYTIC, "Boosts move power when the Pok\u00e9mon moves last.");
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return !b.isFirstAttack() ? 1.3 : 1;
		}
	}

	static class SapSipper extends Ability implements DamageBlocker {
		private static final long serialVersionUID = 1L;

		SapSipper() {
			super(AbilityNamesies.SAP_SIPPER, "Boosts Attack when hit by a Grass-type move.");
		}

		public Stat toIncrease() {
			return Stat.ATTACK;
		}

		public boolean block(Type attacking, ActivePokemon victim) {
			return attacking == Type.GRASS;
		}

		public void alternateEffect(Battle b, ActivePokemon victim) {
			Messages.add(new MessageUpdate(victim.getName() + "'s " + this.getName() + " makes it immune to " + Type.GRASS.getName() + " type moves!"));
			victim.getAttributes().modifyStage(victim, victim, 1, toIncrease(), b, CastSource.ABILITY);
		}
	}

	static class IronBarbs extends Ability implements PhysicalContactEffect {
		private static final long serialVersionUID = 1L;

		IronBarbs() {
			super(AbilityNamesies.IRON_BARBS, "Inflicts damage to the Pok\u00e9mon on contact.");
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			Messages.add(new MessageUpdate(user.getName() + " was hurt by " + victim.getName() + "'s " + this.getName() + "!"));
			user.reduceHealthFraction(b, 1/8.0);
		}
	}

	static class MoldBreaker extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		MoldBreaker() {
			super(AbilityNamesies.MOLD_BREAKER, "Moves can be used regardless of Abilities.");
		}

		public void enter(Battle b, ActivePokemon enterer) {
			Messages.add(new MessageUpdate(enterer.getName() + " breaks the mold!"));
		}
	}

	static class Teravolt extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Teravolt() {
			super(AbilityNamesies.TERAVOLT, "Moves can be used regardless of Abilities.");
		}

		public void enter(Battle b, ActivePokemon enterer) {
			Messages.add(new MessageUpdate(enterer.getName() + " is radiating a bursting aura!"));
		}
	}

	static class Turboblaze extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Turboblaze() {
			super(AbilityNamesies.TURBOBLAZE, "Moves can be used regardless of Abilities.");
		}

		public void enter(Battle b, ActivePokemon enterer) {
			Messages.add(new MessageUpdate(enterer.getName() + " is radiating a blazing aura!"));
		}
	}

	static class RunAway extends Ability implements DefiniteEscape {
		private static final long serialVersionUID = 1L;

		RunAway() {
			super(AbilityNamesies.RUN_AWAY, "Enables a sure getaway from wild Pok\u00e9mon.");
		}
	}

	static class StickyHold extends Ability {
		private static final long serialVersionUID = 1L;

		StickyHold() {
			super(AbilityNamesies.STICKY_HOLD, "Protects the Pok\u00e9mon from item theft.");
		}
	}

	static class Klutz extends Ability {
		private static final long serialVersionUID = 1L;

		Klutz() {
			super(AbilityNamesies.KLUTZ, "The Pok\u00e9mon can't use any held items.");
		}
	}

	static class Unburden extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		Unburden() {
			super(AbilityNamesies.UNBURDEN, "Raises Speed if a held item is used.");
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SPEED;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && p.hasEffect(EffectNamesies.CONSUMED_ITEM)) {
				stat *= 2;
			}
			
			return stat;
		}
	}

	static class Pickpocket extends Ability implements PhysicalContactEffect, ItemHolder {
		private static final long serialVersionUID = 1L;
		private Item item;

		Pickpocket() {
			super(AbilityNamesies.PICKPOCKET, "Steals an item when hit by another Pok\u00e9mon.");
		}

		public void steal(Battle b, ActivePokemon thief, ActivePokemon victim) {
			// Dead Pokemon and wild Pokemon cannot steal;
			// Cannot steal if victim is not holding an item or thief is already holding an item;
			// Cannot steal from a Pokemon with the Sticky Hold ability
			if (thief.isFainted(b) || !victim.isHoldingItem(b) || thief.isHoldingItem(b) || b.getTrainer(thief.isPlayer()) instanceof WildPokemon || victim.hasAbility(AbilityNamesies.STICKY_HOLD)) {
				return;
			}
			
			// Stealers gon' steal
			Item stolen = victim.getHeldItem(b);
			Messages.add(new MessageUpdate(thief.getName() + " stole " + victim.getName() + "'s " + stolen.getName() + "!"));
			
			if (b.isWildBattle()) {
				victim.removeItem();
				thief.giveItem((HoldItem)stolen);
				return;
			}
			
			item = stolen;
			EffectNamesies.CHANGE_ITEM.getEffect().cast(b, thief, thief, CastSource.ABILITY, false);
			
			item = ItemNamesies.NO_ITEM.getItem();
			EffectNamesies.CHANGE_ITEM.getEffect().cast(b, thief, victim, CastSource.ABILITY, false);
		}

		public void contact(Battle b, ActivePokemon user, ActivePokemon victim) {
			// Steal from the Pokemon who made physical contact with you
			steal(b, victim, user);
		}

		public Item getItem() {
			return item;
		}
	}

	static class Harvest extends Ability implements EndTurnEffect {
		private static final long serialVersionUID = 1L;

		Harvest() {
			super(AbilityNamesies.HARVEST, "May create another Berry after one is used.");
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			PokemonEffect consumed = victim.getEffect(EffectNamesies.CONSUMED_ITEM);
			if (consumed == null || victim.isHoldingItem(b)) {
				return;
			}
			
			Item restored = ((ItemHolder)consumed).getItem();
			if (restored instanceof Berry && (b.getWeather().namesies() == EffectNamesies.SUNNY || RandomUtils.chanceTest(50))) {
				victim.giveItem((HoldItem)restored);
				Messages.add(new MessageUpdate(victim.getName() + "'s " + this.getName() + " restored its " + restored.getName() + "!"));
			}
		}
	}

	static class Pickup extends Ability implements EndBattleEffect {
		private static final long serialVersionUID = 1L;

		Pickup() {
			super(AbilityNamesies.PICKUP, "The Pok\u00e9mon may pick up items.");
		}

		public void afterBattle(Trainer player, Battle b, ActivePokemon p) {
			if (!p.isHoldingItem(b) && RandomUtils.chanceTest(10)) {
				// TODO: THIS SHOULDN'T JUST BE LEFTOVERS IT SHOULD BE MORE FUN STUFF
				p.giveItem(ItemNamesies.LEFTOVERS);
			}
		}
	}

	static class Unnerve extends Ability implements EntryEffect {
		private static final long serialVersionUID = 1L;

		Unnerve() {
			super(AbilityNamesies.UNNERVE, "Makes the foe nervous and unable to eat Berries.");
		}

		public void enter(Battle b, ActivePokemon enterer) {
			Messages.add(new MessageUpdate(enterer.getName() + "'s " + this.getName() + " made " + b.getOtherPokemon(enterer.isPlayer()).getName() + " too nervous to eat berries!"));
		}
	}

	static class HoneyGather extends Ability implements EndBattleEffect {
		private static final long serialVersionUID = 1L;

		HoneyGather() {
			super(AbilityNamesies.HONEY_GATHER, "The Pok\u00e9mon may gather Honey from somewhere.");
		}

		public void afterBattle(Trainer player, Battle b, ActivePokemon p) {
			if (!p.isHoldingItem(b) && RandomUtils.chanceTest(5*(int)Math.ceil(p.getLevel()/10.0))) {
				// TODO: Should give the item Honey, but this item has no purpose in our game so we'll see what this ability should actually do also something about Syrup Gather
				p.giveItem(ItemNamesies.LEFTOVERS);
			}
		}
	}

	static class Gluttony extends Ability {
		private static final long serialVersionUID = 1L;

		Gluttony() {
			super(AbilityNamesies.GLUTTONY, "Makes the Pok\u00e9mon use a held Berry earlier than usual.");
		}
	}

	static class Multitype extends Ability implements ChangeTypeEffect {
		private static final long serialVersionUID = 1L;

		Multitype() {
			super(AbilityNamesies.MULTITYPE, "Changes type to match the held Plate.");
		}

		public Type[] getType(Battle b, ActivePokemon p, boolean display) {
			Item item = p.getHeldItem(b);
			if (item instanceof PlateItem) {
				return new Type[] { ((PlateItem)item).getType(), Type.NO_TYPE };
			}
			
			return p.getActualType();
		}
	}

	static class Forecast extends Ability implements ChangeTypeEffect {
		private static final long serialVersionUID = 1L;

		Forecast() {
			super(AbilityNamesies.FORECAST, "Changes with the weather.");
		}

		public Type[] getType(Battle b, ActivePokemon p, boolean display) {
			return new Type[] { b.getWeather().getElement(), Type.NO_TYPE };
		}
	}

	static class Bulletproof extends Ability implements OpponentBeforeTurnEffect {
		private static final long serialVersionUID = 1L;

		Bulletproof() {
			super(AbilityNamesies.BULLETPROOF, "Protects the Pok\u00e9mon from some ball and bomb moves.");
		}

		public boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (p.getAttack().isMoveType(MoveType.BOMB_BALL)) {
				b.printAttacking(p);
				Messages.add(new MessageUpdate(opp.getName() + "'s " + this.getName() + " prevents " + p.getAttack().getName() + " from being used!"));
				return false;
			}
			
			return true;
		}
	}

	static class AuraBreak extends Ability {
		private static final long serialVersionUID = 1L;

		AuraBreak() {
			super(AbilityNamesies.AURA_BREAK, "The effects of Aura Abilities are reversed.");
		}
	}

	static class FairyAura extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		FairyAura() {
			super(AbilityNamesies.FAIRY_AURA, "Powers up each Pok\u00e9mon's Fairy-type moves.");
		}

		public Type getAuraType() {
			return Type.FAIRY;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == getAuraType()) {
				return 1 + .33*(victim.hasAbility(AbilityNamesies.AURA_BREAK) ? -1 : 1);
			}
			
			return 1;
		}
	}

	static class DarkAura extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		DarkAura() {
			super(AbilityNamesies.DARK_AURA, "Powers up each Pok\u00e9mon's Dark-type moves.");
		}

		public Type getAuraType() {
			return Type.DARK;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.getAttackType() == getAuraType()) {
				return 1 + .33*(victim.hasAbility(AbilityNamesies.AURA_BREAK) ? -1 : 1);
			}
			
			return 1;
		}
	}

	static class Magician extends Ability implements ApplyDamageEffect, ItemHolder {
		private static final long serialVersionUID = 1L;
		private Item item;

		Magician() {
			super(AbilityNamesies.MAGICIAN, "The Pok\u00e9mon steals the held item of a Pok\u00e9mon it hits with a move.");
		}

		public void steal(Battle b, ActivePokemon thief, ActivePokemon victim) {
			// Dead Pokemon and wild Pokemon cannot steal;
			// Cannot steal if victim is not holding an item or thief is already holding an item;
			// Cannot steal from a Pokemon with the Sticky Hold ability
			if (thief.isFainted(b) || !victim.isHoldingItem(b) || thief.isHoldingItem(b) || b.getTrainer(thief.isPlayer()) instanceof WildPokemon || victim.hasAbility(AbilityNamesies.STICKY_HOLD)) {
				return;
			}
			
			// Stealers gon' steal
			Item stolen = victim.getHeldItem(b);
			Messages.add(new MessageUpdate(thief.getName() + " stole " + victim.getName() + "'s " + stolen.getName() + "!"));
			
			if (b.isWildBattle()) {
				victim.removeItem();
				thief.giveItem((HoldItem)stolen);
				return;
			}
			
			item = stolen;
			EffectNamesies.CHANGE_ITEM.getEffect().cast(b, thief, thief, CastSource.ABILITY, false);
			
			item = ItemNamesies.NO_ITEM.getItem();
			EffectNamesies.CHANGE_ITEM.getEffect().cast(b, thief, victim, CastSource.ABILITY, false);
		}

		public void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
			// Steal the victim's item when damage is dealt
			steal(b, user, victim);
		}

		public Item getItem() {
			return item;
		}
	}

	static class CheekPouch extends Ability {
		private static final long serialVersionUID = 1L;

		CheekPouch() {
			super(AbilityNamesies.CHEEK_POUCH, "Restores HP as well when the Pok\u00e9mon eats a Berry.");
		}
	}

	static class StrongJaw extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		StrongJaw() {
			super(AbilityNamesies.STRONG_JAW, "The Pok\u00e9mon's strong jaw gives it tremendous biting power.");
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttack().isMoveType(MoveType.BITING) ? 1.5 : 1;
		}
	}

	static class MegaLauncher extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		MegaLauncher() {
			super(AbilityNamesies.MEGA_LAUNCHER, "Powers up aura and pulse moves.");
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttack().isMoveType(MoveType.AURA_PULSE) ? 1.5 : 1;
		}
	}

	static class ToughClaws extends Ability implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		ToughClaws() {
			super(AbilityNamesies.TOUGH_CLAWS, "Powers up moves that make direct contact.");
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getAttack().isMoveType(MoveType.PHYSICAL_CONTACT) ? 1.33 : 1;
		}
	}

	static class SweetVeil extends Ability implements StatusPreventionEffect {
		private static final long serialVersionUID = 1L;

		SweetVeil() {
			super(AbilityNamesies.SWEET_VEIL, "Prevents itself and ally Pok\u00e9mon from falling asleep.");
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return status == StatusCondition.ASLEEP;
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + this.getName() + " prevents sleep!";
		}
	}

	static class AromaVeil extends Ability {
		private static final long serialVersionUID = 1L;

		AromaVeil() {
			super(AbilityNamesies.AROMA_VEIL, "Protects allies from attacks that effect their mental state.");
		}
	}

	static class Healer extends Ability implements EndTurnEffect {
		private static final long serialVersionUID = 1L;

		Healer() {
			super(AbilityNamesies.HEALER, "The Pok\u00e9mon may heal its own status problems.");
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			if (victim.hasStatus() && RandomUtils.chanceTest(1, 3)) {
				Status.removeStatus(b, victim, CastSource.ABILITY);
			}
		}
	}

	static class Pixilate extends Ability implements ChangeAttackTypeEffect, EndTurnEffect, PowerChangeEffect {
		private static final long serialVersionUID = 1L;
		private boolean activated;

		Pixilate() {
			super(AbilityNamesies.PIXILATE, "Normal-type moves become Fairy-type moves.");
			this.activated = false;
		}

		public Type getType() {
			return Type.FAIRY;
		}

		public Type changeAttackType(Type original) {
			if (original == Type.NORMAL) {
				this.activated = true;
				return getType();
			}
			
			return original;
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			this.activated = false;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return activated ? 1.3 : 1;
		}
	}

	static class Refrigerate extends Ability implements ChangeAttackTypeEffect, EndTurnEffect, PowerChangeEffect {
		private static final long serialVersionUID = 1L;
		private boolean activated;

		Refrigerate() {
			super(AbilityNamesies.REFRIGERATE, "Normal-type moves become Ice-type moves.");
			this.activated = false;
		}

		public Type getType() {
			return Type.ICE;
		}

		public Type changeAttackType(Type original) {
			if (original == Type.NORMAL) {
				this.activated = true;
				return getType();
			}
			
			return original;
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			this.activated = false;
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			return activated ? 1.3 : 1;
		}
	}

	static class StanceChange extends Ability implements BeforeTurnEffect, EntryEffect, DifferentStatEffect {
		private static final long serialVersionUID = 1L;
		private static final int[] BLADE_STATS = new int[] {60, 150, 50, 150, 50, 60};
		private static final int[] SHIELD_STATS = new int[] {60, 50, 150, 50, 150, 60};
		
		private boolean shieldForm;

		StanceChange() {
			super(AbilityNamesies.STANCE_CHANGE, "The Pok\u00e9mon changes form depending on how it battles.");
			this.shieldForm = true;
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (shieldForm && p.getAttack().getCategory() != MoveCategory.STATUS) {
				shieldForm = false;
				Messages.add(new MessageUpdate(p.getName() + " changed into Blade Forme!"));
			}
			else if (!shieldForm && p.getAttack().namesies() == AttackNamesies.KINGS_SHIELD) {
				shieldForm = true;
				Messages.add(new MessageUpdate(p.getName() + " changed into Shield Forme!"));
			}
			
			return true;
		}

		public void enter(Battle b, ActivePokemon enterer) {
			Messages.add(new MessageUpdate(enterer.getName() + " is in Shield Forme!"));
			shieldForm = true;
		}

		public Integer getStat(ActivePokemon user, Stat stat) {
			// Need to calculate the new stat -- yes, I realize this is super inefficient and whatever whatever whatever
			int index = stat.index();
			return Stat.getStat(index, user.getLevel(), (shieldForm ? SHIELD_STATS : BLADE_STATS)[index], user.getIV(index), user.getEV(index), user.getNature().getNatureVal(index));
		}
	}

	static class FurCoat extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		FurCoat() {
			super(AbilityNamesies.FUR_COAT, "Halves damage from physical moves.");
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.DEFENSE;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && true) {
				stat *= 2;
			}
			
			return stat;
		}
	}

	static class GrassPelt extends Ability implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		GrassPelt() {
			super(AbilityNamesies.GRASS_PELT, "Boosts the Defense stat in Grassy Terrain.");
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.DEFENSE;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && b.hasEffect(EffectNamesies.GRASSY_TERRAIN)) {
				stat *= 1.5;
			}
			
			return stat;
		}
	}

	static class FlowerVeil extends Ability implements StatusPreventionEffect, StatProtectingEffect {
		private static final long serialVersionUID = 1L;

		FlowerVeil() {
			super(AbilityNamesies.FLOWER_VEIL, "Prevents lowering of Grass-type Pok\u00e9mon's stats.");
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return victim.isType(b, Type.GRASS);
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return victim.getName() + "'s " + this.getName() + " prevents status conditions!";
		}

		public boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
			return victim.isType(b, Type.GRASS);
		}

		public String preventionMessage(ActivePokemon p, Stat s) {
			return p.getName() + "'s " + this.getName() + " prevents its " + s.getName().toLowerCase() + " from being lowered!";
		}
	}

	static class GaleWings extends Ability implements PriorityChangeEffect {
		private static final long serialVersionUID = 1L;

		GaleWings() {
			super(AbilityNamesies.GALE_WINGS, "Gives priority to Flying-type moves.");
		}

		public int changePriority(Battle b, ActivePokemon user, int priority) {
			if (user.getAttack().getActualType() == Type.FLYING) {
				if (this instanceof ConsumableItem) {
					user.consumeItem(b);
				}
				
				priority++;
			}
			
			return priority;
		}
	}

	static class Protean extends Ability implements BeforeTurnEffect, ChangeTypeSource {
		private static final long serialVersionUID = 1L;
		private Type type;

		Protean() {
			super(AbilityNamesies.PROTEAN, "Changes the Pok\u00e9mon's type to the type of the move it's using.");
		}

		public boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			// Protean activates for all moves except for Struggle
			if (p.getAttack().namesies() != AttackNamesies.STRUGGLE) {
				type = p.getAttackType();
				EffectNamesies.CHANGE_TYPE.getEffect().cast(b, p, p, CastSource.ABILITY, true);
			}
			
			return true;
		}

		public Type[] getType(Battle b, ActivePokemon caster, ActivePokemon victim) {
			return new Type[] { type, Type.NO_TYPE };
		}
	}
}
