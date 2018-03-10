package battle.effect.team;

import battle.effect.EffectNamesies;
import battle.effect.team.TeamEffect.AuroraVeil;
import battle.effect.team.TeamEffect.DeadAlly;
import battle.effect.team.TeamEffect.DoomDesire;
import battle.effect.team.TeamEffect.FutureSight;
import battle.effect.team.TeamEffect.GetDatCashMoneyTwice;
import battle.effect.team.TeamEffect.HealSwitch;
import battle.effect.team.TeamEffect.LightScreen;
import battle.effect.team.TeamEffect.LuckyChant;
import battle.effect.team.TeamEffect.PayDay;
import battle.effect.team.TeamEffect.Reflect;
import battle.effect.team.TeamEffect.Spikes;
import battle.effect.team.TeamEffect.StealthRock;
import battle.effect.team.TeamEffect.StickyWeb;
import battle.effect.team.TeamEffect.Tailwind;
import battle.effect.team.TeamEffect.ToxicSpikes;
import battle.effect.team.TeamEffect.Wish;

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

