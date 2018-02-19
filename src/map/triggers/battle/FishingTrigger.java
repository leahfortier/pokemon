package map.triggers.battle;

import battle.ActivePokemon;
import item.ItemNamesies;
import main.Game;
import map.condition.Condition.ItemCondition;
import map.overworld.WildEncounter;
import map.overworld.WildEncounterInfo;
import map.triggers.DialogueTrigger;
import map.triggers.GlobalTrigger;
import map.triggers.GroupTrigger;
import map.triggers.Trigger;
import message.MessageUpdate;
import message.Messages;
import pattern.GroupTriggerMatcher;
import pattern.map.FishingMatcher;
import pokemon.ability.AbilityNamesies;
import trainer.player.Player;
import trainer.player.medal.MedalTheme;
import util.RandomUtils;

public class FishingTrigger extends Trigger {
    public static final String FISHING_GLOBAL = "isFishing";

    private final WildEncounterInfo[] wildEncounters;

    public FishingTrigger(FishingMatcher matcher) {
        super(matcher.getJson(), new ItemCondition(ItemNamesies.FISHING_ROD));

        this.wildEncounters = matcher.getWildEncounters();
    }

    @Override
    public void execute() {
        Player player = Game.getPlayer();

        ActivePokemon front = player.front();
        int chance = front.hasAbility(AbilityNamesies.SUCTION_CUPS) || front.hasAbility(AbilityNamesies.STICKY_HOLD)
                ? 75 // I made up this number since I couldn't find it
                : 50;

        if (RandomUtils.chanceTest(chance)) {
            WildEncounter wildPokemon = WildEncounterInfo.getWildEncounter(front, this.wildEncounters);
            String pokemonJson = wildPokemon.getJson();

            GroupTriggerMatcher matcher = new GroupTriggerMatcher(
                    "FishingBite_" + pokemonJson,
                    new DialogueTrigger("Oh! A bite!"),
                    new GlobalTrigger(FISHING_GLOBAL),
                    new WildBattleTrigger(wildPokemon),
                    new GlobalTrigger("!" + FISHING_GLOBAL)
            );

            Trigger group = new GroupTrigger(matcher, null);
            Messages.add(new MessageUpdate().withTrigger(group));

            player.getMedalCase().increase(MedalTheme.FISH_REELED_IN);
        } else {
            Messages.add(new MessageUpdate().withTrigger(new DialogueTrigger("No dice.")));
        }
    }
}
