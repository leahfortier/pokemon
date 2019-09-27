package map.triggers.battle;

import battle.Battle;
import main.Game;
import map.triggers.Trigger;
import pattern.PokemonMatcher;
import pattern.action.EntityActionMatcher.BattleActionMatcher;
import pattern.action.UpdateMatcher;
import trainer.EnemyTrainer;
import trainer.TrainerType;
import util.RandomUtils;

public class TrainerBattleTrigger extends Trigger {
    private final EnemyTrainer trainer;
    private final UpdateMatcher npcUpdateInteraction;

    public TrainerBattleTrigger(BattleActionMatcher matcher) {
        String trainerName = matcher.getName();
        int cash = matcher.getDatCashMoney();
        int maxPokemonAllowed = matcher.getMaxPokemonAllowed();

        this.trainer = new EnemyTrainer(trainerName, cash, maxPokemonAllowed);

        RandomUtils.setTempRandomSeed(matcher.getJson().hashCode());
        for (PokemonMatcher pokemonMatcher : matcher.getPokemon()) {
            trainer.addPokemon(pokemonMatcher.createPokemon(TrainerType.OPPONENT));
        }
        RandomUtils.resetRandomSeedToInitial();

        this.npcUpdateInteraction = new UpdateMatcher(matcher.getEntityName(), matcher.getUpdateInteraction());
    }

    @Override
    public void execute() {
        trainer.healAll();

        Battle b = new Battle(trainer, this.npcUpdateInteraction);
        Game.instance().setBattleViews(b, true);
    }
}
