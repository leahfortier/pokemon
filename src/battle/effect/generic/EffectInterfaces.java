package battle.effect.generic;

import battle.Battle;
import main.Global;
import pokemon.ActivePokemon;
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
				if (invokee instanceof ApplyDamageEffect) {
					if (Effect.isInactiveEffect(invokee)) {
						continue;
					}
					
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
				if (invokee instanceof EndTurnEffect) {
					if (Effect.isInactiveEffect(invokee)) {
						continue;
					}
					
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
				if (invokee instanceof RecoilMove) {
					if (Effect.isInactiveEffect(invokee)) {
						continue;
					}
					
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
				if (invokee instanceof PhysicalContactEffect) {
					if (Effect.isInactiveEffect(invokee)) {
						continue;
					}
					
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
				if (invokee instanceof TakeDamageEffect) {
					if (Effect.isInactiveEffect(invokee)) {
						continue;
					}
					
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
				if (invokee instanceof CrashDamageMove) {
					if (Effect.isInactiveEffect(invokee)) {
						continue;
					}
					
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
				if (invokee instanceof BarrierEffect) {
					if (Effect.isInactiveEffect(invokee)) {
						continue;
					}
					
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
				if (invokee instanceof DefogRelease) {
					if (Effect.isInactiveEffect(invokee)) {
						continue;
					}
					
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
				if (invokee instanceof RapidSpinRelease) {
					if (Effect.isInactiveEffect(invokee)) {
						continue;
					}
					
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
				if (invokee instanceof NameChanger) {
					if (Effect.isInactiveEffect(invokee)) {
						continue;
					}
					
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
				if (invokee instanceof EntryEffect) {
					if (Effect.isInactiveEffect(invokee)) {
						continue;
					}
					
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
				if (invokee instanceof StatLoweredEffect) {
					if (Effect.isInactiveEffect(invokee)) {
						continue;
					}
					
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
				if (invokee instanceof LevitationEffect) {
					if (Effect.isInactiveEffect(invokee)) {
						continue;
					}
					
					LevitationEffect effect = (LevitationEffect)invokee;
					effect.fall(b, fallen);
				}
			}
		}

		static boolean containsLevitationEffect(Battle b, ActivePokemon p) {
			List<Object> invokees = b.getEffectsList(p);
			for (Object invokee : invokees) {
				if (invokee instanceof LevitationEffect) {
					if (Effect.isInactiveEffect(invokee)) {
						continue;
					}
					
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
				if (invokee instanceof FaintEffect) {
					if (Effect.isInactiveEffect(invokee)) {
						continue;
					}
					
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
				if (invokee instanceof MurderEffect) {
					if (Effect.isInactiveEffect(invokee)) {
						continue;
					}
					
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
				if (invokee instanceof EndBattleEffect) {
					if (Effect.isInactiveEffect(invokee)) {
						continue;
					}
					
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
				if (invokee instanceof GroundedEffect) {
					if (Effect.isInactiveEffect(invokee)) {
						continue;
					}
					
					return true;
				}
			}
			
			return false;
		}
	}
}
