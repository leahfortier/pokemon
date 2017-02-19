package map.triggers;

import battle.effect.RepellingEffect;
import item.Item;
import main.Game;
import map.overworld.EncounterRate;
import map.overworld.WildEncounter;
import message.MessageUpdate;
import message.Messages;
import pattern.map.WildBattleMatcher;
import pokemon.PokemonNamesies;
import trainer.CharacterData;
import util.JsonUtils;
import util.RandomUtils;

public class WalkingWildBattleTrigger extends Trigger {
    private final WildEncounter[] wildEncounters;
    private final EncounterRate encounterRate;

    WalkingWildBattleTrigger(String matcherJson, String condition) {
        super(TriggerType.WALKING_WILD_BATTLE, matcherJson, condition);

        WildBattleMatcher matcher = JsonUtils.deserialize(matcherJson, WildBattleMatcher.class);
        this.wildEncounters = matcher.getWildEncounters();
        this.encounterRate = matcher.getEncounterRate();
    }

    protected void executeTrigger() {
        // TODO: What's going on with this random stuff also maybe this formula should be in the EncounterRate class
        double rand = Math.random()*187.5/encounterRate.getRate();

        if (rand < 1) {
            WildEncounter wildPokemon = getWildEncounter();
            if (repelCheck(wildPokemon)) {
                return;
            }

            Trigger wildBattle = TriggerType.WILD_BATTLE.createTrigger(JsonUtils.getJson(wildPokemon), null);
            Messages.add(new MessageUpdate().withTrigger(wildBattle.getName()));
        }
    }

    private WildEncounter getWildEncounter() {
        final WildEncounter legendaryEncounter = this.getLegendaryEncounter();
        if (legendaryEncounter != null) {
            return legendaryEncounter;
        }

        return WildEncounter.getWildEncounter(this.wildEncounters);
    }

    // Returns a legendary encounter if applicable and null otherwise
    private WildEncounter getLegendaryEncounter() {
        if (RandomUtils.chanceTest(1, 1024) && !Game.getPlayer().getPokedex().isCaught(PokemonNamesies.MEW)) {
            return new WildEncounter(PokemonNamesies.MEW, 5);
        }

        return null;
    }

    private boolean repelCheck(WildEncounter wildPokemon) {
        CharacterData player = Game.getPlayer();

        // Maybe you won't actually fight this Pokemon after all (due to repel, cleanse tag, etc.)
        if (player.front().getLevel() >= wildPokemon.getLevel()) {
            if (player.isUsingRepel()) {
                return true;
            }

            // TODO: Make the chance method return an int instead of a double
            Item item = player.front().getActualHeldItem();
            if (item instanceof RepellingEffect && RandomUtils.chanceTest((int)(100*((RepellingEffect)item).chance()))) {
                return true;
            }
        }

        return false;
    }
}
