package map.triggers.battle;

import battle.Battle;
import main.Game;
import map.condition.Condition;
import map.triggers.Trigger;
import pattern.PokemonMatcher;
import pattern.action.ActionMatcher.BattleActionMatcher;
import pattern.action.UpdateMatcher;
import pokemon.PartyPokemon;
import trainer.EnemyTrainer;
import util.RandomUtils;
import util.SerializationUtils;

public class TrainerBattleTrigger extends Trigger {
    private final EnemyTrainer trainer;
    private final UpdateMatcher npcUpdateInteraction;

    public TrainerBattleTrigger(String contents, Condition condition) {
        this(SerializationUtils.deserializeJson(contents, BattleActionMatcher.class), condition);
    }

    public TrainerBattleTrigger(BattleActionMatcher matcher, Condition condition) {
        super(matcher.getJson(), condition);

        String trainerName = matcher.getName();
        int cash = matcher.getDatCashMoney();
        int maxPokemonAllowed = matcher.getMaxPokemonAllowed();

        this.trainer = new EnemyTrainer(trainerName, cash, maxPokemonAllowed);

        RandomUtils.setTempRandomSeed(matcher.getJson().hashCode());
        for (PokemonMatcher pokemonMatcher : matcher.getPokemon()) {
            trainer.addPokemon(PartyPokemon.createActivePokemon(pokemonMatcher, false));
        }
        RandomUtils.resetRandomSeedToInitial();

        this.npcUpdateInteraction = new UpdateMatcher(matcher.getEntityName(), matcher.getUpdateInteraction());
    }

    @Override
    protected void executeTrigger() {
        trainer.healAll();

        Battle b = new Battle(trainer, this.npcUpdateInteraction);
        Game.instance().setBattleViews(b, true);
    }
}
