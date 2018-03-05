package battle.effect.generic;

import battle.effect.generic.TeamEffect.AuroraVeil;
import battle.effect.generic.TeamEffect.DeadAlly;
import battle.effect.generic.TeamEffect.DoomDesire;
import battle.effect.generic.TeamEffect.FutureSight;
import battle.effect.generic.TeamEffect.GetDatCashMoneyTwice;
import battle.effect.generic.TeamEffect.HealSwitch;
import battle.effect.generic.TeamEffect.LightScreen;
import battle.effect.generic.TeamEffect.LuckyChant;
import battle.effect.generic.TeamEffect.PayDay;
import battle.effect.generic.TeamEffect.Reflect;
import battle.effect.generic.TeamEffect.Spikes;
import battle.effect.generic.TeamEffect.StealthRock;
import battle.effect.generic.TeamEffect.StickyWeb;
import battle.effect.generic.TeamEffect.Tailwind;
import battle.effect.generic.TeamEffect.ToxicSpikes;
import battle.effect.generic.TeamEffect.Wish;

import java.util.function.Supplier;

public enum TeamEffectNamesies implements EffectNamesies<TeamEffect> {
    // EVERYTHING BELOW IS GENERATED ###
    REFLECT(Reflect::new),
    LIGHT_SCREEN(LightScreen::new),
    TAILWIND(Tailwind::new),
    AURORA_VEIL(AuroraVeil::new),
    STICKY_WEB(StickyWeb::new),
    STEALTH_ROCK(StealthRock::new),
    TOXIC_SPIKES(ToxicSpikes::new),
    SPIKES(Spikes::new),
    WISH(Wish::new),
    LUCKY_CHANT(LuckyChant::new),
    FUTURE_SIGHT(FutureSight::new),
    DOOM_DESIRE(DoomDesire::new),
    HEAL_SWITCH(HealSwitch::new),
    DEAD_ALLY(DeadAlly::new),
    PAY_DAY(PayDay::new),
    GET_DAT_CASH_MONEY_TWICE(GetDatCashMoneyTwice::new);

    // EVERYTHING ABOVE IS GENERATED ###

    private final Supplier<TeamEffect> effectCreator;

    TeamEffectNamesies(Supplier<TeamEffect> effectCreator) {
        this.effectCreator = effectCreator;
    }

    @Override
    public TeamEffect getEffect() {
        return this.effectCreator.get();
    }
}

