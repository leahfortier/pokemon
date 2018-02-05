package map.triggers.battle;

import battle.ActivePokemon;
import item.ItemNamesies;
import main.Game;
import map.condition.ConditionHolder.AndCondition;
import map.condition.Condition;
import map.condition.Condition.ItemCondition;
import map.overworld.WildEncounter;
import map.overworld.WildEncounterInfo;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import message.MessageUpdate;
import message.Messages;
import pattern.GroupTriggerMatcher;
import pattern.map.FishingMatcher;
import pokemon.ability.AbilityNamesies;
import trainer.player.Player;
import trainer.player.medal.MedalTheme;
import util.RandomUtils;
import util.SerializationUtils;

public class FishingTrigger extends Trigger {
    public static final String FISHING_GLOBAL = "isFishing";

    private final WildEncounterInfo[] wildEncounters;

    public FishingTrigger(String matcherJson, Condition condition) {
        super(TriggerType.FISHING, matcherJson, new AndCondition(condition, new ItemCondition(ItemNamesies.FISHING_ROD)));

        FishingMatcher matcher = SerializationUtils.deserializeJson(matcherJson, FishingMatcher.class);
        this.wildEncounters = matcher.getWildEncounters();
    }

    @Override
    protected void executeTrigger() {
        Player player = Game.getPlayer();

        ActivePokemon front = player.front();
        int chance = front.hasAbility(AbilityNamesies.SUCTION_CUPS) || front.hasAbility(AbilityNamesies.STICKY_HOLD)
                ? 75 // I made up this number since I couldn't find it
                : 50;

        if (RandomUtils.chanceTest(chance)) {
            WildEncounter wildPokemon = WildEncounterInfo.getWildEncounter(front, this.wildEncounters);
            String pokemonJson = SerializationUtils.getJson(wildPokemon);

            GroupTriggerMatcher matcher = new GroupTriggerMatcher(
                    "FishingBite_" + pokemonJson,
                    TriggerType.DIALOGUE.createTrigger("Oh! A bite!", null).getName(),
                    TriggerType.GLOBAL.createTrigger(FISHING_GLOBAL, null).getName(),
                    TriggerType.WILD_BATTLE.createTrigger(pokemonJson, null).getName(),
                    TriggerType.GLOBAL.createTrigger("!" + FISHING_GLOBAL, null).getName()
            );

            Trigger group = TriggerType.GROUP.createTrigger(SerializationUtils.getJson(matcher), null);
            Messages.add(new MessageUpdate().withTrigger(group.getName()));

            player.getMedalCase().increase(MedalTheme.FISH_REELED_IN);
        } else {
            Messages.add(new MessageUpdate().withTrigger(TriggerType.DIALOGUE.createTrigger("No dice.", null).getName()));
        }
    }
}
