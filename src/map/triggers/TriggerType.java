package map.triggers;

import gui.GameData;
import main.Game;
import util.PokeString;
import util.StringUtils;

public enum TriggerType {
    BADGE(BadgeTrigger.class, BadgeTrigger::new),
    CHANGE_VIEW(ChangeViewTrigger.class, ChangeViewTrigger::new),
    DIALOGUE(DialogueTrigger.class, DialogueTrigger::new),
    GIVE_ITEM(GiveItemTrigger.class, GiveItemTrigger::new),
    GIVE_POKEMON(GivePokemonTrigger.class, GivePokemonTrigger::new),
    GROUP(GroupTrigger.class, GroupTrigger::new, GroupTrigger::getTriggerSuffix),
    HEAL_PARTY(HealPartyTrigger.class, HealPartyTrigger::new),
    SET_LAST_POKE_CENTER(SetLastPokeCenterTrigger.class, SetLastPokeCenterTrigger::new),
    MAP_TRANSITION(MapTransitionTrigger.class, MapTransitionTrigger::new, MapTransitionTrigger::getTriggerSuffix),
    SOUND(SoundTrigger.class, SoundTrigger::new),
    TRAINER_BATTLE(TrainerBattleTrigger.class, TrainerBattleTrigger::new),
    UPDATE(UpdateTrigger.class, UpdateTrigger::new),
    WILD_BATTLE(WildBattleTrigger.class, WildBattleTrigger::new);

    private final TriggerPrefixGetter triggerPrefixGetter;
    private final TriggerSuffixGetter triggerSuffixGetter;
    private final TriggerCreator triggerCreator;

    TriggerType(Class<? extends Trigger> triggerClass, TriggerCreator triggerCreator) {
        this(triggerClass, triggerCreator, contents -> contents);
    }

    TriggerType(Class<? extends Trigger> triggerClass, TriggerCreator triggerCreator, TriggerSuffixGetter triggerSuffixGetter) {
        this.triggerPrefixGetter = triggerClass::getSimpleName;
        this.triggerSuffixGetter = triggerSuffixGetter;
        this.triggerCreator = triggerCreator;
    }

    private interface TriggerSuffixGetter {
        String getTriggerSuffix(final String contents);
    }

    private interface TriggerPrefixGetter {
        String getTriggerPrefix();
    }

    private interface TriggerCreator {
        Trigger createTrigger(final String contents);
    }

    public String getTriggerName(String contents) {
        return this.getTriggerNameFromSuffix(this.triggerSuffixGetter.getTriggerSuffix(contents));
    }

    public String getTriggerNameFromSuffix(String suffix) {
        final String prefix = this.triggerPrefixGetter.getTriggerPrefix();
        return prefix + (StringUtils.isNullOrEmpty(suffix) ? StringUtils.empty() : "_" + suffix);
    }

    public Trigger createTrigger(final String contents) {
        GameData data = Game.getData();
        String triggerName = this.getTriggerName(contents);

        if (data.hasTrigger(triggerName)) {
            return data.getTrigger(triggerName);
        }

        return this.triggerCreator.createTrigger(contents);
    }

    public static TriggerType getTriggerType(final String type) {
        return TriggerType.valueOf(PokeString.getNamesiesString(type));
    }
}
