package battle.effect.generic.team;

import battle.effect.generic.EffectNamesies;
import battle.effect.generic.team.TeamEffect.AuroraVeil;
import battle.effect.generic.team.TeamEffect.DeadAlly;
import battle.effect.generic.team.TeamEffect.DoomDesire;
import battle.effect.generic.team.TeamEffect.FutureSight;
import battle.effect.generic.team.TeamEffect.GetDatCashMoneyTwice;
import battle.effect.generic.team.TeamEffect.HealSwitch;
import battle.effect.generic.team.TeamEffect.LightScreen;
import battle.effect.generic.team.TeamEffect.LuckyChant;
import battle.effect.generic.team.TeamEffect.PayDay;
import battle.effect.generic.team.TeamEffect.Reflect;
import battle.effect.generic.team.TeamEffect.Spikes;
import battle.effect.generic.team.TeamEffect.StealthRock;
import battle.effect.generic.team.TeamEffect.StickyWeb;
import battle.effect.generic.team.TeamEffect.Tailwind;
import battle.effect.generic.team.TeamEffect.ToxicSpikes;
import battle.effect.generic.team.TeamEffect.Wish;

import java.util.function.Supplier;

public enum TeamEffectNamesies implements EffectNamesies {
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

