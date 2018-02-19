package map.triggers;

import gui.GameData;
import main.Game;
import map.condition.Condition;
import map.triggers.battle.FishingTrigger;
import map.triggers.battle.TrainerBattleTrigger;
import map.triggers.battle.WalkingWildBattleTrigger;
import map.triggers.battle.WildBattleTrigger;
import map.triggers.map.MapTransitionTrigger;
import map.triggers.map.MoveNPCTrigger;
import map.triggers.map.MovePlayerTrigger;
import map.triggers.map.ReloadMapTrigger;
import util.StringUtils;

public enum TriggerType {
    BADGE(BadgeTrigger.class, BadgeTrigger::new),
    CHANGE_VIEW(ChangeViewTrigger.class, ChangeViewTrigger::new),
    CHOICE(ChoiceTrigger.class, ChoiceTrigger::new),
    DAY_CARE(DayCareTrigger.class, DayCareTrigger::new),
    DIALOGUE(DialogueTrigger.class, DialogueTrigger::new),
    FISHING(FishingTrigger.class, FishingTrigger::new),
    GIVE_ITEM(GiveItemTrigger.class, GiveItemTrigger::new),
    GIVE_POKEMON(GivePokemonTrigger.class, GivePokemonTrigger::new),
    GLOBAL(GlobalTrigger.class, GlobalTrigger::new),
    GROUP(GroupTrigger.class, GroupTrigger::new),
    HALT(HaltTrigger.class, HaltTrigger::new),
    HEAL_PARTY(HealPartyTrigger.class, HealPartyTrigger::new),
    MAP_TRANSITION(MapTransitionTrigger.class, MapTransitionTrigger::new),
    MEDAL_COUNT(MedalCountTrigger.class, MedalCountTrigger::new),
    MOVE_NPC(MoveNPCTrigger.class, MoveNPCTrigger::new),
    MOVE_PLAYER(MovePlayerTrigger.class, MovePlayerTrigger::new),
    RELOAD_MAP(ReloadMapTrigger.class, ReloadMapTrigger::new),
    SOUND(SoundTrigger.class, SoundTrigger::new),
    TRADE_POKEMON(TradePokemonTrigger.class, TradePokemonTrigger::new),
    TRAINER_BATTLE(TrainerBattleTrigger.class, TrainerBattleTrigger::new),
    UPDATE(UpdateTrigger.class, UpdateTrigger::new),
    USE_ITEM(UseItemTrigger.class, UseItemTrigger::new),
    WALKING_WILD_BATTLE(WalkingWildBattleTrigger.class, WalkingWildBattleTrigger::new),
    WILD_BATTLE(WildBattleTrigger.class, WildBattleTrigger::new);

    private final String triggerPrefix;
    private final TriggerSuffixGetter triggerSuffixGetter;
    private final TriggerCreator triggerCreator;

    TriggerType(Class<? extends Trigger> triggerClass, TriggerCreator triggerCreator) {
        this(triggerClass, triggerCreator, contents -> contents);
    }

    TriggerType(Class<? extends Trigger> triggerClass, TriggerCreator triggerCreator, TriggerSuffixGetter triggerSuffixGetter) {
        this.triggerPrefix = triggerClass.getSimpleName();
        this.triggerSuffixGetter = triggerSuffixGetter;
        this.triggerCreator = triggerCreator;
    }

    public String getTriggerName(String contents) {
        return this.getTriggerNameFromSuffix(this.triggerSuffixGetter.getTriggerSuffix(contents));
    }

    public String getTriggerNameFromSuffix(String suffix) {
        return this.triggerPrefix + (StringUtils.isNullOrEmpty(suffix) ? StringUtils.empty() : "_" + suffix);
    }

    public Trigger createTrigger2(final String contents) {
        return this.createTrigger2(contents, null);
    }

    public Trigger createTrigger2(final String contents, final Condition condition) {
        GameData data = Game.getData();
        String triggerName = this.getTriggerName(contents);

        if (data.hasTrigger(triggerName)) {
            return data.getTrigger(triggerName);
        }

        Trigger trigger = this.triggerCreator.createTrigger(contents, condition);
        data.addTrigger(trigger);

        return trigger;
    }

    @FunctionalInterface
    private interface TriggerSuffixGetter {
        String getTriggerSuffix(final String contents);
    }

    @FunctionalInterface
    private interface TriggerCreator {
        Trigger createTrigger(final String contents, final Condition condition);
    }
}
