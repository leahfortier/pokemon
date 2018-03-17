package map.triggers.battle;

import battle.ActivePokemon;
import battle.effect.InvokeInterfaces.EncounterRateMultiplier;
import battle.effect.InvokeInterfaces.RepellingEffect;
import main.Game;
import map.overworld.wild.EncounterRate;
import map.overworld.wild.WildEncounter;
import map.overworld.wild.WildEncounterInfo;
import map.triggers.Trigger;
import message.MessageUpdate;
import message.Messages;
import pattern.map.WildBattleMatcher;
import pokemon.species.PokemonNamesies;
import util.RandomUtils;

public class WalkingWildBattleTrigger extends Trigger {
    private final WildEncounterInfo[] wildEncounters;
    private final EncounterRate encounterRate;

    public WalkingWildBattleTrigger(WildBattleMatcher matcher) {
        super(matcher.getCondition());

        this.wildEncounters = matcher.getWildEncounters();
        this.encounterRate = matcher.getEncounterRate();
    }

    @Override
    public void execute() {
        ActivePokemon front = Game.getPlayer().front();

        // TODO: What's going on with this random stuff also maybe this formula should be in the EncounterRate class
        double rand = Math.random()*187.5/encounterRate.getRate()*EncounterRateMultiplier.getModifier(front);
        if (rand < 1) {
            WildEncounter wildPokemon = getWildEncounter(front);

            // Maybe you won't actually fight this Pokemon after all (due to repel, cleanse tag, etc.)
            if (RepellingEffect.checkRepellingEffect(front, wildPokemon)) {
                return;
            }

            Trigger wildBattle = new WildBattleTrigger(wildPokemon);
            Messages.add(new MessageUpdate().withTrigger(wildBattle));
        }
    }

    private WildEncounter getWildEncounter(ActivePokemon playerFront) {
        final WildEncounter legendaryEncounter = this.getLegendaryEncounter();
        if (legendaryEncounter != null) {
            return legendaryEncounter;
        }

        return WildEncounterInfo.getWildEncounter(playerFront, this.wildEncounters);
    }

    // Returns a legendary encounter if applicable and null otherwise
    private WildEncounter getLegendaryEncounter() {
        if (RandomUtils.chanceTest(1, 1024) && !Game.getPlayer().getPokedex().isCaught(PokemonNamesies.MEW)) {
            return new WildEncounter(PokemonNamesies.MEW, 5);
        }

        return null;
    }
}
