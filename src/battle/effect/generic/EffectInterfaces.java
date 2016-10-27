package battle.effect.generic;

import battle.Attack;
import battle.Battle;
import battle.Move;
import battle.effect.status.StatusCondition;
import main.Global;
import namesies.EffectNamesies;
import pokemon.Ability;
import pokemon.ActivePokemon;
import pokemon.Stat;
import trainer.Trainer;

import java.util.Collections;
import java.util.List;

public final class EffectInterfaces {

	// Class to hold interfaces -- should not be instantiated
	private EffectInterfaces() {
		Global.error("EffectInterfaces class cannot be instantiated.");
	}

	// EVERYTHING BELOW IS GENERATED ###

	// This is used when the user applies direct damage to an opponent, and has special effects associated
	public interface ApplyDamageEffect {

		// b: The current battle
		// user: The user of that attack, the one who is probably implementing this effect
		// victim: The Pokemon that received the attack
		// damage: The amount of damage that was dealt to victim by the user
		void applyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage);

		static void invokeApplyDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim, int damage) {
			List<Object> invokees = b.getEffectsList(user);
			for (Object invokee : invokees) {
				if (invokee instanceof ApplyDamageEffect && !Effect.isInactiveEffect(invokee)) {
					
					ApplyDamageEffect effect = (ApplyDamageEffect)invokee;
					effect.applyDamageEffect(b, user, victim, damage);
				}
			}
		}
	}

	public interface EndTurnEffect {
		void applyEndTurn(ActivePokemon victim, Battle b);

		static void invokeEndTurnEffect(ActivePokemon victim, Battle b) {
			if (victim.isFainted(b)) {
				return;
			}
			
			// Weather is handled separately
			List<Object> invokees = b.getEffectsList(victim);
			invokees.remove(b.getWeather());
			
			for (Object invokee : invokees) {
				if (invokee instanceof EndTurnEffect && !Effect.isInactiveEffect(invokee)) {
					
					EndTurnEffect effect = (EndTurnEffect)invokee;
					effect.applyEndTurn(victim, b);
					
					if (victim.isFainted(b)) {
						return;
					}
				}
			}
		}
	}

	public interface RecoilMove {
		void applyRecoil(Battle b, ActivePokemon user, int damage);

		static void invokeRecoilMove(Battle b, ActivePokemon user, int damage) {
			List<Object> invokees = Collections.singletonList(user.getAttack());
			for (Object invokee : invokees) {
				if (invokee instanceof RecoilMove && !Effect.isInactiveEffect(invokee)) {
					
					RecoilMove effect = (RecoilMove)invokee;
					effect.applyRecoil(b, user, damage);
				}
			}
		}
	}

	public interface PhysicalContactEffect {

		// b: The current battle
		// user: The user of the attack that caused the physical contact
		// victim: The Pokemon that received the physical contact attack
		void contact(Battle b, ActivePokemon user, ActivePokemon victim);

		static void invokePhysicalContactEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isFainted(b)) {
				return;
			}
			
			List<Object> invokees = b.getEffectsList(victim);
			for (Object invokee : invokees) {
				if (invokee instanceof PhysicalContactEffect && !Effect.isInactiveEffect(invokee)) {
					
					PhysicalContactEffect effect = (PhysicalContactEffect)invokee;
					effect.contact(b, user, victim);
					
					if (user.isFainted(b)) {
						return;
					}
				}
			}
		}
	}

	public interface TakeDamageEffect {

		// b: The current battle
		// user: The user of the attack
		// victim: The Pokemon who is taking damage, they are the one's probably implementing this
		void takeDamage(Battle b, ActivePokemon user, ActivePokemon victim);

		static void invokeTakeDamageEffect(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (victim.isFainted(b)) {
				return;
			}
			
			List<Object> invokees = b.getEffectsList(victim);
			for (Object invokee : invokees) {
				if (invokee instanceof TakeDamageEffect && !Effect.isInactiveEffect(invokee)) {
					
					TakeDamageEffect effect = (TakeDamageEffect)invokee;
					effect.takeDamage(b, user, victim);
					
					if (victim.isFainted(b)) {
						return;
					}
				}
			}
		}
	}

	public interface CrashDamageMove {
		void crash(Battle b, ActivePokemon user);

		static void invokeCrashDamageMove(Battle b, ActivePokemon user) {
			List<Object> invokees = Collections.singletonList(user.getAttack());
			for (Object invokee : invokees) {
				if (invokee instanceof CrashDamageMove && !Effect.isInactiveEffect(invokee)) {
					
					CrashDamageMove effect = (CrashDamageMove)invokee;
					effect.crash(b, user);
				}
			}
		}
	}

	public interface BarrierEffect {
		void breakBarrier(Battle b, ActivePokemon breaker);

		static void breakBarriers(Battle b, ActivePokemon breaker) {
			List<Object> invokees = b.getEffectsList(b.getOtherPokemon(breaker.user()));
			
			for (Object invokee : invokees) {
				if (invokee instanceof BarrierEffect && !Effect.isInactiveEffect(invokee)) {
					
					BarrierEffect effect = (BarrierEffect)invokee;
					effect.breakBarrier(b, breaker);
				}
			}
		}
	}

	public interface DefogRelease {
		void releaseDefog(Battle b, ActivePokemon victim);

		static void release(Battle b, ActivePokemon victim) {
			List<Object> invokees = b.getEffectsList(victim);
			for (Object invokee : invokees) {
				if (invokee instanceof DefogRelease && !Effect.isInactiveEffect(invokee)) {
					
					DefogRelease effect = (DefogRelease)invokee;
					effect.releaseDefog(b, victim);
				}
			}
		}
	}

	public interface RapidSpinRelease {
		void releaseRapidSpin(Battle b, ActivePokemon releaser);

		static void release(Battle b, ActivePokemon releaser) {
			List<Object> invokees = b.getEffectsList(releaser);
			for (Object invokee : invokees) {
				if (invokee instanceof RapidSpinRelease && !Effect.isInactiveEffect(invokee)) {
					
					RapidSpinRelease effect = (RapidSpinRelease)invokee;
					effect.releaseRapidSpin(b, releaser);
				}
			}
		}
	}

	public interface NameChanger {

		// TODO: This one
		String getNameChange();
		void setNameChange(Battle b, ActivePokemon victim);

		static void setNameChanges(Battle b, ActivePokemon victim) {
			List<Object> invokees = b.getEffectsList(victim);
			for (Object invokee : invokees) {
				if (invokee instanceof NameChanger && !Effect.isInactiveEffect(invokee)) {
					
					NameChanger effect = (NameChanger)invokee;
					effect.setNameChange(b, victim);
				}
			}
		}
	}

	public interface EntryEffect {
		void enter(Battle b, ActivePokemon enterer);

		static void invokeEntryEffect(Battle b, ActivePokemon enterer) {
			List<Object> invokees = b.getEffectsList(enterer);
			for (Object invokee : invokees) {
				if (invokee instanceof EntryEffect && !Effect.isInactiveEffect(invokee)) {
					
					EntryEffect effect = (EntryEffect)invokee;
					effect.enter(b, enterer);
				}
			}
		}
	}

	public interface StatLoweredEffect {

		// b: The current battle
		// caster: The Pokemon responsible for causing the stat to be lowered
		// victim: The Pokemon who's stat is being lowered
		void takeItToTheNextLevel(Battle b, ActivePokemon caster, ActivePokemon victim);

		static void invokeStatLoweredEffect(Battle b, ActivePokemon caster, ActivePokemon victim) {
			List<Object> invokees = b.getEffectsList(victim);
			for (Object invokee : invokees) {
				if (invokee instanceof StatLoweredEffect && !Effect.isInactiveEffect(invokee)) {
					
					StatLoweredEffect effect = (StatLoweredEffect)invokee;
					effect.takeItToTheNextLevel(b, caster, victim);
				}
			}
		}
	}

	public interface LevitationEffect {
		void fall(Battle b, ActivePokemon fallen);

		static void falllllllll(Battle b, ActivePokemon fallen) {
			List<Object> invokees = b.getEffectsList(fallen);
			for (Object invokee : invokees) {
				if (invokee instanceof LevitationEffect && !Effect.isInactiveEffect(invokee)) {
					
					LevitationEffect effect = (LevitationEffect)invokee;
					effect.fall(b, fallen);
				}
			}
		}

		static boolean containsLevitationEffect(Battle b, ActivePokemon p) {
			List<Object> invokees = b.getEffectsList(p);
			for (Object invokee : invokees) {
				if (invokee instanceof LevitationEffect && !Effect.isInactiveEffect(invokee)) {
					
					return true;
				}
			}
			
			return false;
		}
	}

	public interface FaintEffect {
		void deathWish(Battle b, ActivePokemon dead, ActivePokemon murderer);

		static void grantDeathWish(Battle b, ActivePokemon dead, ActivePokemon murderer) {
			List<Object> invokees = b.getEffectsList(dead);
			for (Object invokee : invokees) {
				if (invokee instanceof FaintEffect && !Effect.isInactiveEffect(invokee)) {
					
					FaintEffect effect = (FaintEffect)invokee;
					effect.deathWish(b, dead, murderer);
				}
			}
		}
	}

	// KILL KILL KILL MURDER MURDER MURDER
	public interface MurderEffect {
		void killWish(Battle b, ActivePokemon dead, ActivePokemon murderer);

		static void killKillKillMurderMurderMurder(Battle b, ActivePokemon dead, ActivePokemon murderer) {
			List<Object> invokees = b.getEffectsList(murderer);
			for (Object invokee : invokees) {
				if (invokee instanceof MurderEffect && !Effect.isInactiveEffect(invokee)) {
					
					MurderEffect effect = (MurderEffect)invokee;
					effect.killWish(b, dead, murderer);
				}
			}
		}
	}

	public interface EndBattleEffect {
		void afterBattle(Trainer player, Battle b, ActivePokemon p);

		static void invokeEndBattleEffect(List<?> invokees, Trainer player, Battle b, ActivePokemon p) {
			for (Object invokee : invokees) {
				if (invokee instanceof EndBattleEffect
						&& !Effect.isInactiveEffect(invokee)) {
					
					EndBattleEffect effect = (EndBattleEffect)invokee;
					effect.afterBattle(player, b, p);
				}
			}
		}
	}

	public interface GroundedEffect {

		static boolean containsGroundedEffect(Battle b, ActivePokemon p) {
			List<Object> invokees = b.getEffectsList(p);
			for (Object invokee : invokees) {
				if (invokee instanceof GroundedEffect
						&& !Effect.isInactiveEffect(invokee)) {
					
					return true;
				}
			}
			
			return false;
		}
	}

	public interface AccuracyBypassEffect {

		// Attacker is the Pokemon whose accuracy is being evaluated and is the Pokemon on which this effect is attached to
		boolean bypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending);

		static boolean bypassAccuracyCheck(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			List<Object> invokees = b.getEffectsList(attacking);
			invokees.add(attacking.getAttack());
			
			for (Object invokee : invokees) {
				if (invokee instanceof AccuracyBypassEffect && !Effect.isInactiveEffect(invokee)) {
					
					AccuracyBypassEffect effect = (AccuracyBypassEffect)invokee;
					if (effect.bypassAccuracy(b, attacking, defending)) {
						return true;
					}
				}
			}
			
			return false;
		}
	}

	public interface OpponentAccuracyBypassEffect {

		// Attacker is the Pokemon whose accuracy is being evaluated, defender is the Pokemon on which this effect is attached to
		boolean opponentBypassAccuracy(Battle b, ActivePokemon attacking, ActivePokemon defending);

		static boolean bypassAccuracyCheck(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			List<Object> invokees = b.getEffectsList(defending);
			for (Object invokee : invokees) {
				if (invokee instanceof OpponentAccuracyBypassEffect && !Effect.isInactiveEffect(invokee)) {
					
					OpponentAccuracyBypassEffect effect = (OpponentAccuracyBypassEffect)invokee;
					if (effect.opponentBypassAccuracy(b, attacking, defending)) {
						return true;
					}
				}
			}
			
			return false;
		}
	}

	public interface AttackSelectionEffect {
		boolean usable(ActivePokemon p, Move m);
		String getUnusableMessage(ActivePokemon p);

		static AttackSelectionEffect getUnusableEffect(Battle b, ActivePokemon p, Move m) {
			List<Object> invokees = b.getEffectsList(p);
			for (Object invokee : invokees) {
				if (invokee instanceof AttackSelectionEffect && !Effect.isInactiveEffect(invokee)) {
					
					AttackSelectionEffect effect = (AttackSelectionEffect)invokee;
					if (!effect.usable(p, m)) {
						return effect;
					}
				}
			}
			
			return null;
		}
	}

	public interface WeatherBlockerEffect {
		boolean block(EffectNamesies weather);

		static boolean checkBlocked(Battle b, ActivePokemon p, EffectNamesies weather) {
			List<Object> invokees = b.getEffectsList(p);
			for (Object invokee : invokees) {
				if (invokee instanceof WeatherBlockerEffect && !Effect.isInactiveEffect(invokee)) {
					
					WeatherBlockerEffect effect = (WeatherBlockerEffect)invokee;
					if (effect.block(weather)) {
						return true;
					}
				}
			}
			
			return false;
		}
	}

	// Any effect that implements this will prevent a Pokemon with said effect from escaping battle
	public interface TrappingEffect {
		boolean isTrapped(Battle b, ActivePokemon escaper);
		String trappingMessage(ActivePokemon trapped);

		static TrappingEffect getTrapped(Battle b, ActivePokemon escaper) {
			List<Object> invokees = b.getEffectsList(escaper);
			for (Object invokee : invokees) {
				if (invokee instanceof TrappingEffect && !Effect.isInactiveEffect(invokee)) {
					
					TrappingEffect effect = (TrappingEffect)invokee;
					if (effect.isTrapped(b, escaper)) {
						return effect;
					}
				}
			}
			
			return null;
		}
	}

	public interface OpponentTrappingEffect {
		boolean trapOpponent(Battle b, ActivePokemon escaper, ActivePokemon trapper);
		String opponentTrappingMessage(ActivePokemon escaper, ActivePokemon trapper);

		static OpponentTrappingEffect getTrapped(Battle b, ActivePokemon escaper, ActivePokemon trapper) {
			List<Object> invokees = b.getEffectsList(escaper);
			for (Object invokee : invokees) {
				if (invokee instanceof OpponentTrappingEffect && !Effect.isInactiveEffect(invokee)) {
					
					OpponentTrappingEffect effect = (OpponentTrappingEffect)invokee;
					if (effect.trapOpponent(b, escaper, trapper)) {
						return effect;
					}
				}
			}
			
			return null;
		}
	}

	public interface BeforeTurnEffect {

		// TODO: Rename these to attacking and defending
		boolean canAttack(ActivePokemon p, ActivePokemon opp, Battle b);

		static boolean checkCannotAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (p.isFainted(b)) {
				return false;
			}
			
			if (opp.isFainted(b)) {
				return false;
			}
			
			List<Object> invokees = b.getEffectsList(p);
			for (Object invokee : invokees) {
				if (invokee instanceof BeforeTurnEffect && !Effect.isInactiveEffect(invokee)) {
					
					BeforeTurnEffect effect = (BeforeTurnEffect)invokee;
					if (!effect.canAttack(p, opp, b)) {
						return true;
					}
					
					if (p.isFainted(b)) {
						return false;
					}
					
					if (opp.isFainted(b)) {
						return false;
					}
				}
			}
			
			return false;
		}
	}

	public interface OpponentBeforeTurnEffect {
		boolean opposingCanAttack(ActivePokemon p, ActivePokemon opp, Battle b);

		static boolean checkCannotAttack(ActivePokemon p, ActivePokemon opp, Battle b) {
			if (p.isFainted(b)) {
				return false;
			}
			
			if (opp.isFainted(b)) {
				return false;
			}
			
			List<Object> invokees = b.getEffectsList(opp);
			for (Object invokee : invokees) {
				if (invokee instanceof OpponentBeforeTurnEffect && !Effect.isInactiveEffect(invokee)) {
					
					// If this is an ability that is being affected by mold breaker, we don't want to do anything with it
					if (invokee instanceof Ability && p.breaksTheMold()) {
						continue;
					}
					
					OpponentBeforeTurnEffect effect = (OpponentBeforeTurnEffect)invokee;
					if (!effect.opposingCanAttack(p, opp, b)) {
						return true;
					}
					
					if (p.isFainted(b)) {
						return false;
					}
					
					if (opp.isFainted(b)) {
						return false;
					}
				}
			}
			
			return false;
		}
	}

	public interface EffectBlockerEffect {
		boolean validMove(Battle b, ActivePokemon user, ActivePokemon victim);

		static boolean checkBlocked(Battle b, ActivePokemon user, ActivePokemon victim) {
			List<Object> invokees = b.getEffectsList(victim);
			for (Object invokee : invokees) {
				if (invokee instanceof EffectBlockerEffect && !Effect.isInactiveEffect(invokee)) {
					
					// If this is an ability that is being affected by mold breaker, we don't want to do anything with it
					if (invokee instanceof Ability && user.breaksTheMold()) {
						continue;
					}
					
					EffectBlockerEffect effect = (EffectBlockerEffect)invokee;
					if (!effect.validMove(b, user, victim)) {
						return true;
					}
				}
			}
			
			return false;
		}
	}

	public interface TargetSwapperEffect {
		boolean swapTarget(Battle b, ActivePokemon user, ActivePokemon opponent);

		static boolean checkSwapTarget(Battle b, ActivePokemon user, ActivePokemon opponent) {
			List<Object> invokees = b.getEffectsList(opponent);
			for (Object invokee : invokees) {
				if (invokee instanceof TargetSwapperEffect && !Effect.isInactiveEffect(invokee)) {
					
					// If this is an ability that is being affected by mold breaker, we don't want to do anything with it
					if (invokee instanceof Ability && user.breaksTheMold()) {
						continue;
					}
					
					TargetSwapperEffect effect = (TargetSwapperEffect)invokee;
					if (effect.swapTarget(b, user, opponent)) {
						return true;
					}
				}
			}
			
			return false;
		}
	}

	public interface CritBlockerEffect {
		boolean blockCrits();

		static boolean checkBlocked(Battle b, ActivePokemon attacking, ActivePokemon defending) {
			List<Object> invokees = b.getEffectsList(defending);
			invokees.add(attacking.getAttack());
			
			for (Object invokee : invokees) {
				if (invokee instanceof CritBlockerEffect && !Effect.isInactiveEffect(invokee)) {
					
					// If this is an ability that is being affected by mold breaker, we don't want to do anything with it
					if (invokee instanceof Ability && attacking.breaksTheMold()) {
						continue;
					}
					
					CritBlockerEffect effect = (CritBlockerEffect)invokee;
					if (effect.blockCrits()) {
						return true;
					}
				}
			}
			
			return false;
		}
	}

	public interface StatProtectingEffect {
		boolean prevent(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat);
		String preventionMessage(ActivePokemon p, Stat s);

		static StatProtectingEffect getPreventEffect(Battle b, ActivePokemon caster, ActivePokemon victim, Stat stat) {
			List<Object> invokees = b.getEffectsList(victim);
			for (Object invokee : invokees) {
				if (invokee instanceof StatProtectingEffect && !Effect.isInactiveEffect(invokee)) {
					
					// If this is an ability that is being affected by mold breaker, we don't want to do anything with it
					if (invokee instanceof Ability && caster.breaksTheMold()) {
						continue;
					}
					
					StatProtectingEffect effect = (StatProtectingEffect)invokee;
					if (effect.prevent(b, caster, victim, stat)) {
						return effect;
					}
				}
			}
			
			return null;
		}
	}

	public interface StatusPreventionEffect {

		// TODO: Would be nice in the future if I am able to implement multiple invoke methods for the same interface method since this could also use a basic check invoke as well
		boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status);
		String statusPreventionMessage(ActivePokemon victim);

		static StatusPreventionEffect getPreventEffect(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			List<Object> invokees = b.getEffectsList(victim);
			for (Object invokee : invokees) {
				if (invokee instanceof StatusPreventionEffect && !Effect.isInactiveEffect(invokee)) {
					
					// If this is an ability that is being affected by mold breaker, we don't want to do anything with it
					if (invokee instanceof Ability && caster.breaksTheMold()) {
						continue;
					}
					
					StatusPreventionEffect effect = (StatusPreventionEffect)invokee;
					if (effect.preventStatus(b, caster, victim, status)) {
						return effect;
					}
				}
			}
			
			return null;
		}
	}

	public interface BracingEffect {
		boolean isBracing(Battle b, ActivePokemon bracer, boolean fullHealth);
		String braceMessage(ActivePokemon bracer);

		static BracingEffect getBracingEffect(Battle b, ActivePokemon bracer, boolean fullHealth) {
			List<Object> invokees = b.getEffectsList(bracer);
			for (Object invokee : invokees) {
				if (invokee instanceof BracingEffect && !Effect.isInactiveEffect(invokee)) {
					
					// If this is an ability that is being affected by mold breaker, we don't want to do anything with it
					if (invokee instanceof Ability && b.getOtherPokemon(bracer.user()).breaksTheMold()) {
						continue;
					}
					
					BracingEffect effect = (BracingEffect)invokee;
					if (effect.isBracing(b, bracer, fullHealth)) {
						return effect;
					}
				}
			}
			
			return null;
		}
	}
}
