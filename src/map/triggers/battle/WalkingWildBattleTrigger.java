package map.triggers.battle;

import battle.ActivePokemon;
import battle.effect.generic.EffectInterfaces.EncounterRateMultiplier;
import battle.effect.generic.EffectInterfaces.RepellingEffect;
import main.Game;
import map.condition.Condition;
import map.condition.ConditionHolder.AndCondition;
import map.overworld.EncounterRate;
import map.overworld.WildEncounter;
import map.overworld.WildEncounterInfo;
import map.triggers.Trigger;
import message.MessageUpdate;
import message.Messages;
import pattern.map.WildBattleMatcher;
import pokemon.PokemonNamesies;
import util.RandomUtils;

public class WalkingWildBattleTrigger extends Trigger {
    private final WildEncounterInfo[] wildEncounters;
    private final EncounterRate encounterRate;

    public WalkingWildBattleTrigger(WildBattleMatcher matcher, Condition condition) {
        super(matcher.getJson(), new AndCondition(condition, matcher.getCondition()));

        this.wildEncounters = matcher.getWildEncounters();
        this.encounterRate = matcher.getEncounterRate();
    }

    @Override
    protected void executeTrigger() {
        ActivePokemon front = Game.getPlayer().front();

        // TODO: What's going on with this random stuff also maybe this formula should be in the EncounterRate class
        double rand = Math.random()*187.5/encounterRate.getRate()*EncounterRateMultiplier.getModifier(front);
        if (rand < 1) {
            WildEncounter wildPokemon = getWildEncounter(front);

            // Maybe you won't actually fight this Pokemon after all (due to repel, cleanse tag, etc.)
            if (RepellingEffect.checkRepellingEffect(front, wildPokemon)) {
                return;
            }

            Trigger wildBattle = new WildBattleTrigger(wildPokemon, null);
            Messages.add(new MessageUpdate().withTrigger(wildBattle.getName()));
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
