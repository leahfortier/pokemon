package map.triggers.battle;

import battle.Battle;
import main.Game;
import map.entity.EntityAction.BattleAction;
import map.triggers.Trigger;
import map.triggers.TriggerType;
import pattern.PokemonMatcher;
import pattern.action.BattleMatcher;
import pattern.action.UpdateMatcher;
import pokemon.ActivePokemon;
import trainer.EnemyTrainer;
import util.RandomUtils;
import util.SerializationUtils;

public class TrainerBattleTrigger extends Trigger {
    private final EnemyTrainer trainer;
    private final UpdateMatcher npcUpdateInteraction;
    
    public TrainerBattleTrigger(String contents, String condition) {
        super(TriggerType.TRAINER_BATTLE, contents, condition);
        
        BattleAction battleAction = SerializationUtils.deserializeJson(contents, BattleAction.class);
        BattleMatcher battleMatcher = battleAction.getBattleMatcher();
        
        String trainerName = battleMatcher.getName();
        int cash = battleMatcher.getDatCashMoney();
        int maxPokemonAllowed = battleMatcher.getMaxPokemonAllowed();
        
        this.trainer = new EnemyTrainer(trainerName, cash, maxPokemonAllowed);
        
        RandomUtils.setTempRandomSeed(contents.hashCode());
        for (PokemonMatcher matcher : battleMatcher.getPokemon()) {
            trainer.addPokemon(ActivePokemon.createActivePokemon(matcher, false));
        }
        RandomUtils.resetRandomSeedToInitial();
        
        this.npcUpdateInteraction = new UpdateMatcher(battleAction.getEntityName(), battleMatcher.getUpdateInteraction());
    }
    
    protected void executeTrigger() {
        trainer.healAll();
        
        Battle b = new Battle(trainer, this.npcUpdateInteraction);
        Game.instance().setBattleViews(b, true);
    }
}
