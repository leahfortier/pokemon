package map.triggers;

import gui.view.ViewMode;
import main.Global;
import map.WildEncounter;
import message.MessageUpdate;
import message.Messages;
import pattern.GroupTriggerMatcher;
import pattern.map.FishingMatcher;
import util.JsonUtils;
import util.RandomUtils;

public class FishingTrigger extends Trigger {
    private final WildEncounter[] wildEncounters;

    FishingTrigger(String matcherJson, String condition) {
        super(TriggerType.FISHING, matcherJson, condition);

        FishingMatcher matcher = JsonUtils.deserialize(matcherJson, FishingMatcher.class);
        this.wildEncounters = matcher.getWildEncounters();

        // TODO: Move to test
        int totalProbability = 0;
        for (WildEncounter wildEncounter : this.wildEncounters) {
            totalProbability = totalProbability + wildEncounter.getProbability();
        }

        if (totalProbability != 100) {
            Global.error("Wild battle trigger probabilities add up to " + totalProbability + ", not 100.");
        }
    }

    protected void executeTrigger() {
        if (RandomUtils.chanceTest(50)) {
            WildEncounter wildPokemon = WildEncounter.getWildEncounter(this.wildEncounters);
            Trigger dialogue = TriggerType.DIALOGUE.createTrigger("Oh! A bite!", null);
            Trigger wildBattle = TriggerType.WILD_BATTLE.createTrigger(JsonUtils.getJson(wildPokemon), null);

            GroupTriggerMatcher matcher = new GroupTriggerMatcher("FishingBite", dialogue.getName(), wildBattle.getName());
            Trigger group = TriggerType.GROUP.createTrigger(JsonUtils.getJson(matcher), null);
            Messages.add(new MessageUpdate().withTrigger(group.getName()));
        }
        else {
            Messages.add(new MessageUpdate().withTrigger(TriggerType.DIALOGUE.createTrigger("No dice.", null).getName()));
        }
    }
}
