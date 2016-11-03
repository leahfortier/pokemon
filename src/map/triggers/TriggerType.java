package map.triggers;

import main.Global;

public enum TriggerType {
    BADGE("Badge", BadgeTrigger::new),
    CHANGE_VIEW("ChangeView", ChangeViewTrigger::new),
    EVENT("Event", EventTrigger::new),
    GIVE("Give", GiveTrigger::new),
    GROUP("Group", GroupTrigger::new),
    HEAL_PARTY("HealParty", HealPartyTrigger::new),
    LAST_POKE_CENTER("LastPokeCenter", LastPokeCenterTrigger::new),
    MAP_TRANSITION("MapTransition", MapTransitionTrigger::new),
    SOUND("Sound", SoundTrigger::new),
    TRAINER_BATTLE("TrainerBattle", TrainerBattleTrigger::new),
    UPDATE("Update", UpdateTrigger::new),
    WILD_BATTLE("WildBattle", WildBattleTrigger::new);

    final String typeName;
    final GetTrigger getTrigger;

    TriggerType(final String typeName, final GetTrigger getTrigger) {
        this.typeName = typeName;
        this.getTrigger = getTrigger;
    }

    private interface GetTrigger {
        Trigger getTrigger(final String name, final String contents);
    }

    public Trigger getTrigger(final String name, final String contents) {
        return this.getTrigger.getTrigger(name, contents);
    }

    public static TriggerType getTriggerType(final String type) {
        for (final TriggerType triggerType : TriggerType.values()) {
            if (triggerType.typeName.equals(type)) {
                return triggerType;
            }
        }

        Global.error("Could not find a trigger with type " + type);
        return null;
    }
}
