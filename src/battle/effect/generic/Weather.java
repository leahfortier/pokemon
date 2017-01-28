package battle.effect.generic;

import battle.Battle;
import battle.effect.WeatherExtendingEffect;
import battle.effect.generic.EffectInterfaces.EndTurnEffect;
import battle.effect.generic.EffectInterfaces.PowerChangeEffect;
import battle.effect.generic.EffectInterfaces.StatChangingEffect;
import battle.effect.generic.EffectInterfaces.StatusPreventionEffect;
import battle.effect.generic.EffectInterfaces.WeatherBlockerEffect;
import battle.effect.status.StatusCondition;
import item.Item;
import type.Type;
import message.MessageUpdate;
import message.Messages;
import pokemon.ActivePokemon;
import pokemon.Stat;

public abstract class Weather extends BattleEffect implements EndTurnEffect {
	private static final long serialVersionUID = 1L;

	private Type weatherElement;
	
	public Weather(EffectNamesies namesies, Type weatherElement) {
		super(namesies, -1, -1, true);
		this.weatherElement = weatherElement;
	}
	
	public Type getElement() {
		return weatherElement;
	}
	
	public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
		super.cast(b, caster, victim, source, printCast);
		b.getWeather().setTurns(getTurns(b, caster));
		
		Messages.add(new MessageUpdate().updatePokemon(b, caster));
		Messages.add(new MessageUpdate().updatePokemon(b, victim));
	}
	
	private int getTurns(Battle b, ActivePokemon caster) {
		Item item = caster.getHeldItem(b);
		if (item instanceof WeatherExtendingEffect && this.namesies == ((WeatherExtendingEffect)item).getWeatherType()) {
			return 8;
		}
		
		return 5;
	}
	
	// EVERYTHING BELOW IS GENERATED ###
	/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

	static class ClearSkies extends Weather {
		private static final long serialVersionUID = 1L;

		ClearSkies() {
			super(EffectNamesies.CLEAR_SKIES, Type.NORMAL);
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
		}
	}

	static class Raining extends Weather implements PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Raining() {
			super(EffectNamesies.RAINING, Type.WATER);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(b.getWeather().namesies() == this.namesies);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return "It started to rain!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The rain stopped.";
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			Messages.add(new MessageUpdate("The rain continues to pour."));
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.WATER)) {
				// Water is fiddy percent stronger in tha weathz
				return 1.5;
			}
			else if (user.isAttackType(Type.FIRE)) {
				// Fire is fiddy percent weaker in tha weathz
				return .5;
			}
			else {
				return 1;
			}
		}
	}

	static class Sunny extends Weather implements StatusPreventionEffect, PowerChangeEffect {
		private static final long serialVersionUID = 1L;

		Sunny() {
			super(EffectNamesies.SUNNY, Type.FIRE);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(b.getWeather().namesies() == this.namesies);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return "The sunlight turned harsh!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The sunlight faded.";
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			Messages.add(new MessageUpdate("The sunlight is strong."));
		}

		public boolean preventStatus(Battle b, ActivePokemon caster, ActivePokemon victim, StatusCondition status) {
			return status == StatusCondition.FROZEN;
		}

		public String statusPreventionMessage(ActivePokemon victim) {
			return "Too sunny to freeze!!";
		}

		public double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
			if (user.isAttackType(Type.FIRE)) {
				// Fire is fiddy percent stronger in tha weathz
				return 1.5;
			}
			else if (user.isAttackType(Type.WATER)) {
				// Water is fiddy percent weaker in tha weathz
				return .5;
			}
			else {
				return 1;
			}
		}
	}

	static class Sandstorm extends Weather implements StatChangingEffect {
		private static final long serialVersionUID = 1L;
		private static final Type[] immunees = new Type[] { Type.ROCK, Type.GROUND, Type.STEEL };
		private void buffet(Battle b, ActivePokemon p) {
			// Don't buffet the immune!
			for (Type type : immunees) {
				if (p.isType(b, type)) {
					return;
				}
			}
			
			// Srsly don't buffet the immune!!
			if (WeatherBlockerEffect.checkBlocked(b, p, this.namesies)) {
				return;
			}
			
			// Buffety buffety buffet
			Messages.add(new MessageUpdate(p.getName() + " is buffeted by the sandstorm!"));
			p.reduceHealthFraction(b, 1/16.0);
		}

		Sandstorm() {
			super(EffectNamesies.SANDSTORM, Type.ROCK);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(b.getWeather().namesies() == this.namesies);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return "A sandstorm kicked up!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The sandstorm subsided.";
		}

		private boolean canModifyStat(Battle b, ActivePokemon p, ActivePokemon opp) {
			return p.isType(b, Type.ROCK);
		}

		private boolean isModifyStat(Stat s) {
			return s == Stat.SP_DEFENSE;
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && canModifyStat(b, p, opp)) {
				stat *= 1.5;
			}
			
			return stat;
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			Messages.add(new MessageUpdate("The sandstorm rages."));
			
			ActivePokemon other = b.getOtherPokemon(victim.isPlayer());
			buffet(b, victim);
			buffet(b, other);
		}
	}

	static class Hailing extends Weather {
		private static final long serialVersionUID = 1L;
		private static final Type[] immunees = new Type[] { Type.ICE };
		private void buffet(Battle b, ActivePokemon p) {
			// Don't buffet the immune!
			for (Type type : immunees) {
				if (p.isType(b, type)) {
					return;
				}
			}
			
			// Srsly don't buffet the immune!!
			if (WeatherBlockerEffect.checkBlocked(b, p, this.namesies)) {
				return;
			}
			
			// Buffety buffety buffet
			Messages.add(new MessageUpdate(p.getName() + " is buffeted by the hail!"));
			p.reduceHealthFraction(b, 1/16.0);
		}

		Hailing() {
			super(EffectNamesies.HAILING, Type.ICE);
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(b.getWeather().namesies() == this.namesies);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return "It started to hail!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The hail stopped.";
		}

		public void applyEndTurn(ActivePokemon victim, Battle b) {
			Messages.add(new MessageUpdate("The hail continues to fall."));
			
			ActivePokemon other = b.getOtherPokemon(victim.isPlayer());
			buffet(b, victim);
			buffet(b, other);
		}
	}
}
