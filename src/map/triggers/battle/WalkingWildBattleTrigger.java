package map.triggers.battle;

import battle.ActivePokemon;
import battle.effect.generic.EffectInterfaces.EncounterRateMultiplier;
import battle.effect.generic.EffectInterfaces.RepellingEffect;
import battle.effect.generic.EffectInterfaces.WildEncounterAlterer;
import main.Game;
import map.overworld.EncounterRate;
import map.overworld.WildEncounter;
import map.overworld.WildEncounterInfo;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import message.MessageUpdate;
import message.Messages;
import pattern.map.WildBattleMatcher;
import pokemon.PokemonNamesies;
import trainer.player.Player;
import util.RandomUtils;
import util.SerializationUtils;

public class WalkingWildBattleTrigger extends Trigger {
    private final WildEncounterInfo[] wildEncounters;
    private final EncounterRate encounterRate;

    public WalkingWildBattleTrigger(String matcherJson, String condition) {
        super(TriggerType.WALKING_WILD_BATTLE, matcherJson, condition);

        WildBattleMatcher matcher = SerializationUtils.deserializeJson(matcherJson, WildBattleMatcher.class);
        this.wildEncounters = matcher.getWildEncounters();
        this.encounterRate = matcher.getEncounterRate();
    }

    @Override
    protected void executeTrigger() {
        Player player = Game.getPlayer();
        ActivePokemon front = player.front();

        // TODO: What's going on with this random stuff also maybe this formula should be in the EncounterRate class
        double rand = Math.random()*187.5/encounterRate.getRate()*EncounterRateMultiplier.getModifier(front);
        if (rand < 1) {
            WildEncounter wildPokemon = getWildEncounter(front);

            // Maybe you won't actually fight this Pokemon after all (due to repel, cleanse tag, etc.)
            if ((wildPokemon.getLevel() <= front.getLevel() && player.getRepelInfo().isUsingRepel())
                    || RepellingEffect.checkRepellingEffect(front, wildPokemon)) {
                return;
            }

            Trigger wildBattle = TriggerType.WILD_BATTLE.createTrigger(SerializationUtils.getJson(wildPokemon), null);
            Messages.add(new MessageUpdate().withTrigger(wildBattle.getName()));
        }
    }

    private WildEncounter getWildEncounter(ActivePokemon playerFront) {
        final WildEncounter legendaryEncounter = this.getLegendaryEncounter();
        if (legendaryEncounter != null) {
            return legendaryEncounter;
        }

        WildEncounterInfo encounterInfo = WildEncounterInfo.getWildEncounterInfo(this.wildEncounters);
        WildEncounter encounter = new WildEncounter(encounterInfo);
        WildEncounterAlterer.invokeWildEncounterAlterer(playerFront, encounterInfo, encounter);

        return encounter;
    }

    // Returns a legendary encounter if applicable and null otherwise
    private WildEncounter getLegendaryEncounter() {
        if (RandomUtils.chanceTest(1, 1024) && !Game.getPlayer().getPokedex().isCaught(PokemonNamesies.MEW)) {
            return new WildEncounter(PokemonNamesies.MEW, 5);
        }

        return null;
    }
}
