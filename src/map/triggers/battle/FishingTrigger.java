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
import map.triggers.MedalCountTrigger;
import map.triggers.Trigger;
import message.MessageUpdate;
import message.Messages;
import pokemon.ability.AbilityNamesies;
import trainer.player.Player;
import trainer.player.medal.MedalTheme;
import util.RandomUtils;

public class FishingTrigger extends Trigger {
    public static final String FISHING_GLOBAL = "isFishing";

    private final WildEncounterInfo[] wildEncounters;

    public FishingTrigger(WildEncounterInfo[] wildEncounters) {
        super(new ItemCondition(ItemNamesies.FISHING_ROD));

        this.wildEncounters = wildEncounters;
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

            Trigger trigger = new GroupTrigger(
                    new DialogueTrigger("Oh! A bite!"),
                    new GlobalTrigger(FISHING_GLOBAL),
                    new WildBattleTrigger(wildPokemon),
                    new GlobalTrigger("!" + FISHING_GLOBAL),
                    new MedalCountTrigger(MedalTheme.FISH_REELED_IN)
            );

            Messages.add(new MessageUpdate().withTrigger(trigger));
        } else {
            Messages.add(new MessageUpdate().withTrigger(new DialogueTrigger("No dice.")));
        }
    }
}
