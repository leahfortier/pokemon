package map.triggers;

import util.PokeString;

public enum TriggerType {
    BADGE(BadgeTrigger::new),
    CHANGE_VIEW(ChangeViewTrigger::new),
    DIALOGUE(DialogueTrigger::new),
    GIVE_ITEM(GiveItemTrigger::new),
    GIVE_POKEMON(GivePokemonTrigger::new),
    GROUP(GroupTrigger::new),
    HEAL_PARTY(HealPartyTrigger::new),
    LAST_POKE_CENTER(LastPokeCenterTrigger::new),
    MAP_TRANSITION(MapTransitionTrigger::new),
    SOUND(SoundTrigger::new),
    TRAINER_BATTLE(TrainerBattleTrigger::new),
    UPDATE(UpdateTrigger::new),
    WILD_BATTLE(WildBattleTrigger::new);

    private final GetTrigger getTrigger;

    TriggerType(final GetTrigger getTrigger) {
        this.getTrigger = getTrigger;
    }

    private interface GetTrigger {
        Trigger getTrigger(final String name, final String contents);
    }

    public Trigger getTrigger(final String name, final String contents) {
        return this.getTrigger.getTrigger(name, contents);
    }

    public static TriggerType getTriggerType(final String type) {
        return TriggerType.valueOf(PokeString.getNamesiesString(type));
    }
}
