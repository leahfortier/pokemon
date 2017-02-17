package map.triggers;

import map.overworld.WildEncounter;
import message.MessageUpdate;
import message.Messages;
import pattern.GroupTriggerMatcher;
import pattern.map.FishingMatcher;
import util.JsonUtils;
import util.RandomUtils;

public class FishingTrigger extends Trigger {
    public static final String FISHING_GLOBAL = "isFishing";

    private final WildEncounter[] wildEncounters;

    FishingTrigger(String matcherJson, String condition) {
        super(TriggerType.FISHING, matcherJson, condition);

        FishingMatcher matcher = JsonUtils.deserialize(matcherJson, FishingMatcher.class);
        this.wildEncounters = matcher.getWildEncounters();
    }

    protected void executeTrigger() {
        if (RandomUtils.chanceTest(50)) {
            WildEncounter wildPokemon = WildEncounter.getWildEncounter(this.wildEncounters);
            String pokemonJson = JsonUtils.getJson(wildPokemon);

            GroupTriggerMatcher matcher = new GroupTriggerMatcher(
                    "FishingBite_" + pokemonJson,
                    TriggerType.DIALOGUE.createTrigger("Oh! A bite!", null).getName(),
                    TriggerType.GLOBAL.createTrigger(FISHING_GLOBAL, null).getName(),
                    TriggerType.WILD_BATTLE.createTrigger(pokemonJson, null).getName(),
                    TriggerType.GLOBAL.createTrigger("!" + FISHING_GLOBAL, null).getName()
            );

            Trigger group = TriggerType.GROUP.createTrigger(JsonUtils.getJson(matcher), null);
            Messages.add(new MessageUpdate().withTrigger(group.getName()));
        }
        else {
            Messages.add(new MessageUpdate().withTrigger(TriggerType.DIALOGUE.createTrigger("No dice.", null).getName()));
        }
    }
}
