package battle.effect.generic;

import battle.Attack;
import battle.Battle;
import battle.Move;
import battle.effect.generic.EffectInterfaces.BarrierEffect;
import battle.effect.generic.EffectInterfaces.CritBlockerEffect;
import battle.effect.generic.EffectInterfaces.DefogRelease;
import battle.effect.generic.EffectInterfaces.EndBattleEffect;
import battle.effect.generic.EffectInterfaces.EntryEffect;
import battle.effect.generic.EffectInterfaces.RapidSpinRelease;
import battle.effect.generic.EffectInterfaces.StatChangingEffect;
import battle.effect.status.Status;
import battle.effect.status.StatusCondition;
import main.Global;
import main.Type;
import message.Messages;
import namesies.AbilityNamesies;
import namesies.AttackNamesies;
import namesies.EffectNamesies;
import namesies.ItemNamesies;
import pokemon.ActivePokemon;
import pokemon.Stat;
import trainer.Trainer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

// Class to handle effects that are specific to one side of the battle
public abstract class TeamEffect extends Effect implements Serializable {
	private static final long serialVersionUID = 1L;
	private static Map<String, TeamEffect> map;
	
	public TeamEffect(EffectNamesies name, int minTurns, int maxTurns, boolean nextTurnSubside) {
		super(name, minTurns, maxTurns, nextTurnSubside);
	}

	public abstract TeamEffect newInstance();
	
	public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
		if (printCast) Messages.addMessage(getCastMessage(b, caster, victim));
		b.getTrainer(victim.user()).addEffect(this);
		
		Messages.addMessage("", b, caster);
		Messages.addMessage("", b, victim);
	}

	public static TeamEffect getEffect(EffectNamesies name) {
		String effectName = name.getName();
		if (map == null) {
			loadEffects();
		}
		
		if (!map.containsKey(effectName)) {
			Global.error("No such Effect " + effectName);
		}

		return map.get(effectName);
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

	static class Reflect extends TeamEffect implements BarrierEffect, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		Reflect() {
			super(EffectNamesies.REFLECT, 5, 5, false);
		}

		public Reflect newInstance() {
			return (Reflect)(new Reflect().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim.user()), this.namesies));
		}

		public void breakBarrier(Battle b, ActivePokemon breaker) {
			Messages.addMessage(breaker.getName() + " broke the reflect barrier!");
			b.getEffects(!breaker.user()).remove(this);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, ItemNamesies.LIGHT_CLAY)) {
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
			Messages.addMessage("The effects of reflect faded.");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim.user()).remove(this);
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && !opp.hasAbility(AbilityNamesies.INFILTRATOR)) {
				stat *= 2;
			}
			
			return stat;
		}
	}

	static class LightScreen extends TeamEffect implements BarrierEffect, StatChangingEffect {
		private static final long serialVersionUID = 1L;

		LightScreen() {
			super(EffectNamesies.LIGHT_SCREEN, 5, 5, false);
		}

		public LightScreen newInstance() {
			return (LightScreen)(new LightScreen().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim.user()), this.namesies));
		}

		public void breakBarrier(Battle b, ActivePokemon breaker) {
			Messages.addMessage(breaker.getName() + " broke the light screen barrier!");
			b.getEffects(!breaker.user()).remove(this);
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			super.cast(b, caster, victim, source, printCast);
			if (caster.isHoldingItem(b, ItemNamesies.LIGHT_CLAY)) {
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
			Messages.addMessage("The effects of light screen faded.");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim.user()).remove(this);
		}

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && !opp.hasAbility(AbilityNamesies.INFILTRATOR)) {
				stat *= 2;
			}
			
			return stat;
		}
	}

	static class Tailwind extends TeamEffect implements StatChangingEffect {
		private static final long serialVersionUID = 1L;

		Tailwind() {
			super(EffectNamesies.TAILWIND, 4, 4, false);
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

		public int modify(Battle b, ActivePokemon p, ActivePokemon opp, Stat s, int stat) {
			if (isModifyStat(s) && true) {
				stat *= 2;
			}
			
			return stat;
		}
	}

	static class StickyWeb extends TeamEffect implements EntryEffect, RapidSpinRelease, DefogRelease {
		private static final long serialVersionUID = 1L;

		StickyWeb() {
			super(EffectNamesies.STICKY_WEB, -1, -1, false);
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

		public void enter(Battle b, ActivePokemon enterer) {
			if (enterer.isLevitating(b)) {
				return;
			}
			
			enterer.getAttributes().modifyStage(b.getOtherPokemon(enterer.user()), enterer, -1, Stat.SPEED, b, CastSource.EFFECT, "The sticky web {change} " + enterer.getName() + "'s {statName}!");
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.addMessage("The sticky web spun away!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser.user()).remove(this);
		}

		public void releaseDefog(Battle b, ActivePokemon victim) {
			Messages.addMessage("The sticky web dispersed!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim.user()).remove(this);
		}
	}

	static class StealthRock extends TeamEffect implements EntryEffect, RapidSpinRelease, DefogRelease {
		private static final long serialVersionUID = 1L;

		StealthRock() {
			super(EffectNamesies.STEALTH_ROCK, -1, -1, false);
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

		public void enter(Battle b, ActivePokemon enterer) {
			if (enterer.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.addMessage(enterer.getName() + " was hurt by stealth rock!");
			enterer.reduceHealthFraction(b, Type.getBasicAdvantage(Type.ROCK, enterer, b)/8.0);
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.addMessage("The floating rocks spun away!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser.user()).remove(this);
		}

		public void releaseDefog(Battle b, ActivePokemon victim) {
			Messages.addMessage("The floating rocks dispersed!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim.user()).remove(this);
		}
	}

	static class ToxicSpikes extends TeamEffect implements EntryEffect, RapidSpinRelease, DefogRelease {
		private static final long serialVersionUID = 1L;
		private int layers;

		ToxicSpikes() {
			super(EffectNamesies.TOXIC_SPIKES, -1, -1, false);
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
			Messages.addMessage(getCastMessage(b, caster, victim));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return "Toxic spikes were scattered all around!";
		}

		public void enter(Battle b, ActivePokemon enterer) {
			if (enterer.isLevitating(b)) {
				return;
			}
			
			if (enterer.isType(b, Type.POISON)) {
				Messages.addMessage(enterer.getName() + " absorbed the Toxic Spikes!");
				super.active = false;
				return;
			}
			
			ActivePokemon theOtherPokemon = b.getOtherPokemon(enterer.user());
			if (Status.applies(StatusCondition.POISONED, b, theOtherPokemon, enterer)) {
				if (layers >= 2) {
					PokemonEffect.getEffect(EffectNamesies.BAD_POISON).cast(b, theOtherPokemon, enterer, CastSource.EFFECT, false);
				}
				else {
					Status.giveStatus(b, theOtherPokemon, enterer, StatusCondition.POISONED);
				}
			}
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.addMessage("The toxic spikes dispersed!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser.user()).remove(this);
		}

		public void releaseDefog(Battle b, ActivePokemon victim) {
			Messages.addMessage("The toxic spikes dispersed!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim.user()).remove(this);
		}
	}

	static class Spikes extends TeamEffect implements EntryEffect, RapidSpinRelease, DefogRelease {
		private static final long serialVersionUID = 1L;
		private int layers;

		Spikes() {
			super(EffectNamesies.SPIKES, -1, -1, false);
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
			Messages.addMessage(getCastMessage(b, caster, victim));
		}

		public String getCastMessage(Battle b, ActivePokemon user, ActivePokemon victim) {
			return "Spikes were scattered all around!";
		}

		public void enter(Battle b, ActivePokemon enterer) {
			if (enterer.isLevitating(b) || enterer.hasAbility(AbilityNamesies.MAGIC_GUARD)) {
				return;
			}
			
			Messages.addMessage(enterer.getName() + " was hurt by spikes!");
			
			// TODO: Generalize this type of statement
			if (layers == 1) enterer.reduceHealthFraction(b, 1/8.0);
			else if (layers == 2) enterer.reduceHealthFraction(b, 1/6.0);
			else enterer.reduceHealthFraction(b, 1/4.0);
		}

		public void releaseRapidSpin(Battle b, ActivePokemon releaser) {
			Messages.addMessage("The spikes dispersed!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both list
			releaser.getEffects().remove(this);
			b.getEffects(releaser.user()).remove(this);
		}

		public void releaseDefog(Battle b, ActivePokemon victim) {
			Messages.addMessage("The spikes dispersed!");
			
			// This is a little hacky and I'm not a super fan but I don't feel like distinguishing in the generator if this a PokemonEffect or a TeamEffect, so just try to remove from both lists
			victim.getEffects().remove(this);
			b.getEffects(victim.user()).remove(this);
		}
	}

	static class Wish extends TeamEffect {
		private static final long serialVersionUID = 1L;
		private String casterName;

		Wish() {
			super(EffectNamesies.WISH, 1, 1, true);
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
			if (p.hasEffect(EffectNamesies.HEAL_BLOCK)) {
				return;
			}
			
			p.healHealthFraction(1/2.0);
			Messages.addMessage(casterName + "'s wish came true!", b, p);
		}
	}

	static class LuckyChant extends TeamEffect implements CritBlockerEffect {
		private static final long serialVersionUID = 1L;

		LuckyChant() {
			super(EffectNamesies.LUCKY_CHANT, 5, 5, false);
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

	static class FutureSight extends TeamEffect {
		private static final long serialVersionUID = 1L;
		private ActivePokemon theSeeer;

		FutureSight() {
			super(EffectNamesies.FUTURE_SIGHT, 2, 2, true);
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
			Messages.addMessage(p.getName() + " took " + theSeeer.getName() + "'s attack!");
			
			Attack attack = Attack.getAttack(AttackNamesies.FUTURE_SIGHT);
			
			// Don't do anything for moves that are uneffective
			if (!attack.effective(b, theSeeer, p)) {
				return;
			}
			
			theSeeer.setMove(new Move(attack));
			theSeeer.getAttack().applyDamage(theSeeer, p, b);
		}
	}

	static class DoomDesire extends TeamEffect {
		private static final long serialVersionUID = 1L;
		private ActivePokemon theSeeer;

		DoomDesire() {
			super(EffectNamesies.DOOM_DESIRE, 2, 2, true);
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
			Messages.addMessage(p.getName() + " took " + theSeeer.getName() + "'s attack!");
			
			Attack attack = Attack.getAttack(AttackNamesies.DOOM_DESIRE);
			
			// Don't do anything for moves that are uneffective
			if (!attack.effective(b, theSeeer, p)) {
				return;
			}
			
			theSeeer.setMove(new Move(attack));
			theSeeer.getAttack().applyDamage(theSeeer, p, b);
		}
	}

	static class HealSwitch extends TeamEffect implements EntryEffect {
		private static final long serialVersionUID = 1L;
		private String wish;

		HealSwitch() {
			super(EffectNamesies.HEAL_SWITCH, -1, -1, false);
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
			// TODO: This should be passes in the generator instead of being hardcoded
			wish = caster.getAttack().namesies() == AttackNamesies.LUNAR_DANCE ? "lunar dance" : "healing wish";
			super.cast(b, caster, victim, source, printCast);
		}

		public void enter(Battle b, ActivePokemon enterer) {
			enterer.healHealthFraction(1);
			enterer.removeStatus();
			
			Messages.addMessage(enterer.getName() + " health was restored due to the " + wish + "!", b, enterer);
			super.active = false;
		}
	}

	static class DeadAlly extends TeamEffect {
		private static final long serialVersionUID = 1L;

		DeadAlly() {
			super(EffectNamesies.DEAD_ALLY, 2, 2, false);
		}

		public DeadAlly newInstance() {
			return (DeadAlly)(new DeadAlly().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim.user()), this.namesies));
		}
	}

	static class PayDay extends TeamEffect implements EndBattleEffect {
		private static final long serialVersionUID = 1L;
		private int coins;

		PayDay() {
			super(EffectNamesies.PAY_DAY, -1, -1, false);
		}

		public PayDay newInstance() {
			PayDay x = (PayDay)(new PayDay().activate());
			x.coins = coins;
			return x;
		}

		public void cast(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source, boolean printCast) {
			PayDay payday = (PayDay)Effect.getEffect(b.getEffects(true), this.namesies);
			Messages.addMessage(getCastMessage(b, caster, victim));
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
			Messages.addMessage(player.getName() + " picked up " + coins + " pokedollars!");
			player.getDatCashMoney(coins);
		}
	}

	static class GetDatCashMoneyTwice extends TeamEffect {
		private static final long serialVersionUID = 1L;

		GetDatCashMoneyTwice() {
			super(EffectNamesies.GET_DAT_CASH_MONEY_TWICE, -1, -1, false);
		}

		public GetDatCashMoneyTwice newInstance() {
			return (GetDatCashMoneyTwice)(new GetDatCashMoneyTwice().activate());
		}

		public boolean applies(Battle b, ActivePokemon caster, ActivePokemon victim, CastSource source) {
			return !(Effect.hasEffect(b.getEffects(victim.user()), this.namesies));
		}
	}
}
