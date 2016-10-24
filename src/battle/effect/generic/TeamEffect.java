package battle.effect.generic;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import battle.effect.BarrierEffect;
import battle.effect.CritBlockerEffect;
import battle.effect.DefogRelease;
import battle.effect.EndBattleEffect;
import battle.effect.EntryEffect;
import battle.effect.RapidSpinRelease;
import battle.effect.StatChangingEffect;
import battle.effect.status.Status;
import battle.effect.status.StatusCondition;
import main.Global;
import namesies.Namesies;
import main.Type;
import pokemon.ActivePokemon;
import pokemon.Stat;
import trainer.Trainer;
import battle.Attack;
import battle.Battle;
import battle.Move;

// Class to handle effects that are specific to one side of the battle
public abstract class TeamEffect extends Effect implements Serializable {
	private static final long serialVersionUID = 1L;
	private static Map<String, TeamEffect> map;
	
	public TeamEffect(Namesies name, int minTurns, int maxTurns, boolean nextTurnSubside) {
		super(name, minTurns, maxTurns, nextTurnSubside);
	}

	public abstract TeamEffect newInstance();
	
	public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
		if (printCast) b.addMessage(getCastMessage(b, caster, victim));
		b.getTrainer(victim.user()).addEffect(this);
		
		b.addMessage("", caster);
		b.addMessage("", victim);
	}

	public static TeamEffect getEffect(Namesies name) {
		String e = name.getName();
		if (map == null) {
			loadEffects();
		}
		
		if (map.containsKey(e)) {
			return map.get(e);
		}

		Global.error("No such Effect " + e);
		return null;
	}

	// Create and load the effects map if it doesn't already exist
	public static void loadEffects() {
		if (map != null) return;
		map = new HashMap<>();
		
		// EVERYTHING BELOW IS GENERATED ###

		// List all of the classes we are loading
		map.put("Reflect", new Reflect());
		map.put("LightScreen", new LightScreen());
		map.put("Tailwind", new Tailwind());
		map.put("StickyWeb", new StickyWeb());
		map.put("StealthRock", new StealthRock());
		map.put("ToxicSpikes", new ToxicSpikes());
		map.put("Spikes", new Spikes());
		map.put("Wish", new Wish());
		map.put("LuckyChant", new LuckyChant());
		map.put("FutureSight", new FutureSight());
		map.put("DoomDesire", new DoomDesire());
		map.put("HealSwitch", new HealSwitch());
		map.put("DeadAlly", new DeadAlly());
		map.put("PayDay", new PayDay());
		map.put("GetDatCashMoneyTwice", new GetDatCashMoneyTwice());
	}

	/**** WARNING DO NOT PUT ANY VALUABLE CODE HERE IT WILL BE DELETED *****/

	private static class Reflect extends TeamEffect implements BarrierEffect, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		Reflect() {
			super(Namesies.REFLECT_EFFECT, 5, 5, false);
		}

		public Reflect newInstance() {
			return (Reflect)(new Reflect().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim.user()), this.namesies));
		}

		public void breakBarrier(Battle b, ActivePokemon breaker) {
			b.addMessage(breaker.getName() + " broke the reflect barrier!");
			b.getEffects(!breaker.user()).remove(this);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, Namesies.LIGHT_CLAY_ITEM)) {
				Effect.getEffect(b.getEffects(victim.user()), this.namesies).setTurns(8);
			}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " raised the " + Stat.DEFENSE.getName().toLowerCase() + " of its team!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The effects of reflect faded.";
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.DEFENSE;
		}

		public void releaseDefog(Battle b, ActivePokemon victim) {
			b.addMessage("The effects of reflect faded.");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim.user()).remove(this);
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && !opp.hasAbility(Namesies.INFILTRATOR_ABILITY)) {
				stat *= 2;
			}
			
			return stat;
		}
	}

	private static class LightScreen extends TeamEffect implements BarrierEffect, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		LightScreen() {
			super(Namesies.LIGHT_SCREEN_EFFECT, 5, 5, false);
		}

		public LightScreen newInstance() {
			return (LightScreen)(new LightScreen().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim.user()), this.namesies));
		}

		public void breakBarrier(Battle b, ActivePokemon breaker) {
			b.addMessage(breaker.getName() + " broke the light screen barrier!");
			b.getEffects(!breaker.user()).remove(this);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, Namesies.LIGHT_CLAY_ITEM)) {
				Effect.getEffect(b.getEffects(victim.user()), this.namesies).setTurns(8);
			}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " raised the " + Stat.SP_DEFENSE.getName().toLowerCase() + " of its team!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The effects of light screen faded.";
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SP_DEFENSE;
		}

		public void releaseDefog(Battle b, ActivePokemon victim) {
			b.addMessage("The effects of light screen faded.");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim.user()).remove(this);
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && !opp.hasAbility(Namesies.INFILTRATOR_ABILITY)) {
				stat *= 2;
			}
			
			return stat;
		}
	}

	private static class Tailwind extends TeamEffect implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		Tailwind() {
			super(Namesies.TAILWIND_EFFECT, 4, 4, false);
		}

		public Tailwind newInstance() {
			return (Tailwind)(new Tailwind().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim.user()), this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return user.getName() + " raised the speed of its team!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The effects of tailwind faded.";
		}

		public boolean isModifyStat(Stat s) {
			return s == Stat.SPEED;
		}

		public int modify(Integer statValue, ActivePokemon p, ActivePokemon opp, Stat s, Battle b) {
			int stat = statValue;
			if (isModifyStat(s) && true) {
				stat *= 2;
			}
			
			return stat;
		}
	}

	private static class StickyWeb extends TeamEffect implements EntryEffect, RapidSpinRelease, DefogRelease {
		private static final long serialVersionUID = 1L;

		StickyWeb() {
			super(Namesies.STICKY_WEB_EFFECT, -1, -1, false);
		}

		public StickyWeb newInstance() {
			return (StickyWeb)(new StickyWeb().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim.user()), this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return "Sticky web covers everything!";
		}

		public void enter(Battle b, ActivePokemon victim) {
			if (victim.isLevitating(b)) {
				return;
			}
			
			victim.getAttributes().modifyStage(b.getOtherPokemon(victim.user()), victim, -1, Stat.SPEED, b, CastSource.EFFECT, "The sticky web {change} " + victim.getName() + "'s {statName}!");
		}

		public void releaseRapidSpin(Battle b, ActivePokemon user) {
			b.addMessage("The sticky web spun away!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			user.getEffects().remove(this);
			b.getEffects(user.user()).remove(this);
		}

		public void releaseDefog(Battle b, ActivePokemon victim) {
			b.addMessage("The sticky web dispersed!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim.user()).remove(this);
		}
	}

	private static class StealthRock extends TeamEffect implements EntryEffect, RapidSpinRelease, DefogRelease {
		private static final long serialVersionUID = 1L;

		StealthRock() {
			super(Namesies.STEALTH_ROCK_EFFECT, -1, -1, false);
		}

		public StealthRock newInstance() {
			return (StealthRock)(new StealthRock().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim.user()), this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return "Floating rocks were scattered all around!";
		}

		public void enter(Battle b, ActivePokemon victim) {
			if (victim.hasAbility(Namesies.MAGIC_GUARD_ABILITY)) {
				return;
			}
			
			b.addMessage(victim.getName() + " was hurt by stealth rock!");
			victim.reduceHealthFraction(b, Type.getBasicAdvantage(Type.ROCK, victim, b)/8.0);
		}

		public void releaseRapidSpin(Battle b, ActivePokemon user) {
			b.addMessage("The floating rocks spun away!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			user.getEffects().remove(this);
			b.getEffects(user.user()).remove(this);
		}

		public void releaseDefog(Battle b, ActivePokemon victim) {
			b.addMessage("The floating rocks dispersed!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim.user()).remove(this);
		}
	}

	private static class ToxicSpikes extends TeamEffect implements EntryEffect, RapidSpinRelease, DefogRelease {
		private static final long serialVersionUID = 1L;
		private int layers;

		ToxicSpikes() {
			super(Namesies.TOXIC_SPIKES_EFFECT, -1, -1, false);
		}

		public ToxicSpikes newInstance() {
			ToxicSpikes x = (ToxicSpikes)(new ToxicSpikes().activate());
			x.layers = 1;
			return x;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			Effect spikesies = Effect.getEffect(b.getEffects(victim.user()), this.namesies);
			if (spikesies == null) {
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			
			((ToxicSpikes)spikesies).layers++;
			b.addMessage(getCastMessage(b, caster, victim));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return "Toxic spikes were scattered all around!";
		}

		public void enter(Battle b, ActivePokemon victim) {
			if (victim.isLevitating(b)) {
				return;
			}
			
			if (victim.isType(b, Type.POISON)) {
				b.addMessage(victim.getName() + " absorbed the Toxic Spikes!");
				super.active = false;
				return;
			}
			
			ActivePokemon theOtherPokemon = b.getOtherPokemon(victim.user());
			if (Status.applies(StatusCondition.POISONED, b, theOtherPokemon, victim)) {
				if (layers >= 2) {
					PokemonEffect.getEffect(Namesies.BAD_POISON_EFFECT).cast(b, theOtherPokemon, victim, CastSource.EFFECT, false);
				}
				else {
					Status.giveStatus(b, theOtherPokemon, victim, StatusCondition.POISONED);
				}
			}
		}

		public void releaseRapidSpin(Battle b, ActivePokemon user) {
			b.addMessage("The toxic spikes dispersed!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			user.getEffects().remove(this);
			b.getEffects(user.user()).remove(this);
		}

		public void releaseDefog(Battle b, ActivePokemon victim) {
			b.addMessage("The toxic spikes dispersed!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim.user()).remove(this);
		}
	}

	private static class Spikes extends TeamEffect implements EntryEffect, RapidSpinRelease, DefogRelease {
		private static final long serialVersionUID = 1L;
		private int layers;

		Spikes() {
			super(Namesies.SPIKES_EFFECT, -1, -1, false);
		}

		public Spikes newInstance() {
			Spikes x = (Spikes)(new Spikes().activate());
			x.layers = 1;
			return x;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			Effect spikesies = Effect.getEffect(b.getEffects(victim.user()), this.namesies);
			if (spikesies == null) {
				super.cast(b, caster, victim, source, printCast);
				return;
			}
			
			((Spikes)spikesies).layers++;
			b.addMessage(getCastMessage(b, caster, victim));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return "Spikes were scattered all around!";
		}

		public void enter(Battle b, ActivePokemon victim) {
			if (victim.isLevitating(b) || victim.hasAbility(Namesies.MAGIC_GUARD_ABILITY)) {
				return;
			}
			
			b.addMessage(victim.getName() + " was hurt by spikes!");
			
			// TODO: Generalize this type of statement
			if (layers == 1) victim.reduceHealthFraction(b, 1/8.0);
			else if (layers == 2) victim.reduceHealthFraction(b, 1/6.0);
			else victim.reduceHealthFraction(b, 1/4.0);
		}

		public void releaseRapidSpin(Battle b, ActivePokemon user) {
			b.addMessage("The spikes dispersed!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			user.getEffects().remove(this);
			b.getEffects(user.user()).remove(this);
		}

		public void releaseDefog(Battle b, ActivePokemon victim) {
			b.addMessage("The spikes dispersed!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim.user()).remove(this);
		}
	}

	private static class Wish extends TeamEffect {
		private static final long serialVersionUID = 1L;
		private String casterName;

		Wish() {
			super(Namesies.WISH_EFFECT, 1, 1, true);
		}

		public Wish newInstance() {
			Wish x = (Wish)(new Wish().activate());
			x.casterName = casterName;
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim.user()), this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			casterName = caster.getName();
			super.cast(b, caster, victim, source, printCast);
		}

		public void subside(Battle b, ActivePokemon p) {
			if (p.hasEffect(Namesies.HEAL_BLOCK_EFFECT)) {
				return;
			}
			
			p.healHealthFraction(1/2.0);
			b.addMessage(casterName + "'s wish came true!", p);
		}
	}

	private static class LuckyChant extends TeamEffect implements CritBlockerEffect {
		private static final long serialVersionUID = 1L;

		LuckyChant() {
			super(Namesies.LUCKY_CHANT_EFFECT, 5, 5, false);
		}

		public LuckyChant newInstance() {
			return (LuckyChant)(new LuckyChant().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim.user()), this.namesies));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return "The lucky chant shielded " + victim.getName() + "'s team from critical hits!";
		}

		public String getSubsideMessage(ActivePokemon victim) {
			return "The effects of lucky chant wore off.";
		}

		public boolean blockCrits() {
			return true;
		}
	}

	private static class FutureSight extends TeamEffect {
		private static final long serialVersionUID = 1L;
		private ActivePokemon theSeeer;

		FutureSight() {
			super(Namesies.FUTURE_SIGHT_EFFECT, 2, 2, true);
		}

		public FutureSight newInstance() {
			FutureSight x = (FutureSight)(new FutureSight().activate());
			x.theSeeer = theSeeer;
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim.user()), this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			theSeeer = caster;
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return theSeeer.getName() + " foresaw an attack!";
		}

		public void subside(Battle b, ActivePokemon p) {
			b.addMessage(p.getName() + " took " + theSeeer.getName() + "'s attack!");
			
			Attack attack = Attack.getAttack(Namesies.FUTURE_SIGHT_ATTACK);
			
			// Don't do anything for moves that are uneffective
			if (!attack.effective(b, theSeeer, p)) {
				return;
			}
			
			theSeeer.setMove(new Move(attack));
			theSeeer.getAttack().applyDamage(theSeeer, p, b);
		}
	}

	private static class DoomDesire extends TeamEffect {
		private static final long serialVersionUID = 1L;
		private ActivePokemon theSeeer;

		DoomDesire() {
			super(Namesies.DOOM_DESIRE_EFFECT, 2, 2, true);
		}

		public DoomDesire newInstance() {
			DoomDesire x = (DoomDesire)(new DoomDesire().activate());
			x.theSeeer = theSeeer;
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim.user()), this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			theSeeer = caster;
			super.cast(b, caster, victim, source, printCast);
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return theSeeer.getName() + " foresaw an attack!";
		}

		public void subside(Battle b, ActivePokemon p) {
			b.addMessage(p.getName() + " took " + theSeeer.getName() + "'s attack!");
			
			Attack attack = Attack.getAttack(Namesies.DOOM_DESIRE_ATTACK);
			
			// Don't do anything for moves that are uneffective
			if (!attack.effective(b, theSeeer, p)) {
				return;
			}
			
			theSeeer.setMove(new Move(attack));
			theSeeer.getAttack().applyDamage(theSeeer, p, b);
		}
	}

	private static class HealSwitch extends TeamEffect implements EntryEffect {
		private static final long serialVersionUID = 1L;
		private String wish;

		HealSwitch() {
			super(Namesies.HEAL_SWITCH_EFFECT, -1, -1, false);
		}

		public HealSwitch newInstance() {
			HealSwitch x = (HealSwitch)(new HealSwitch().activate());
			x.wish = wish;
			return x;
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim.user()), this.namesies));
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			wish = caster.getAttack().namesies() == Namesies.LUNAR_DANCE_ATTACK ? "lunar dance" : "healing wish";
			super.cast(b, caster, victim, source, printCast);
		}

		public void enter(Battle b, ActivePokemon victim) {
			victim.healHealthFraction(1);
			victim.removeStatus();
			
			b.addMessage(victim.getName() + " health was restored due to the " + wish + "!", victim);
			super.active = false;
		}
	}

	private static class DeadAlly extends TeamEffect {
		private static final long serialVersionUID = 1L;

		DeadAlly() {
			super(Namesies.DEAD_ALLY_EFFECT, 2, 2, false);
		}

		public DeadAlly newInstance() {
			return (DeadAlly)(new DeadAlly().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim.user()), this.namesies));
		}
	}

	private static class PayDay extends TeamEffect implements EndBattleEffect {
		private static final long serialVersionUID = 1L;
		private int coins;

		PayDay() {
			super(Namesies.PAY_DAY_EFFECT, -1, -1, false);
		}

		public PayDay newInstance() {
			PayDay x = (PayDay)(new PayDay().activate());
			x.coins = coins;
			return x;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			PayDay payday = (PayDay)Effect.getEffect(b.getEffects(true), this.namesies);
			b.addMessage(getCastMessage(b, caster, victim));
			coins = 5*caster.getLevel();
			if (payday == null) {
				b.getPlayer().addEffect(this);
			}
			else {
				payday.coins += coins;
			}
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return "Coins scattered everywhere!";
		}

		public void afterBattle(Trainer player, Battle b, ActivePokemon p) {
			b.addMessage(player.getName() + " picked up " + coins + " pokedollars!");
			player.getDatCashMoney(coins);
		}
	}

	private static class GetDatCashMoneyTwice extends TeamEffect {
		private static final long serialVersionUID = 1L;

		GetDatCashMoneyTwice() {
			super(Namesies.GET_DAT_CASH_MONEY_TWICE_EFFECT, -1, -1, false);
		}

		public GetDatCashMoneyTwice newInstance() {
			return (GetDatCashMoneyTwice)(new GetDatCashMoneyTwice().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim.user()), this.namesies));
		}
	}
}
