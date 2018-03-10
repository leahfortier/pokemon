package trainer;

import battle.ActivePokemon;
import battle.Battle;
import battle.Battle.EnterBattleMessageGetter;
import main.Global;
import pokemon.PartyPokemon;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class WildPokemon implements Opponent, Serializable {
    private final ActivePokemon wildPokemon;
    private TeamEffectList effects;

    public WildPokemon(ActivePokemon wildPokemon) {
        if (wildPokemon.isPlayer()) {
            Global.error("Wild pokemon cannot be a player Pokemon.");
        }

        this.wildPokemon = wildPokemon;
        this.effects = new TeamEffectList();
    }

    @Override
    public ActivePokemon front() {
        return wildPokemon;
    }

    @Override
    public int getTeamIndex(ActivePokemon teamMember) {
        if (teamMember != wildPokemon) {
            Global.error("Wild pokemon only has one team member, and you are not it.");
        }

        return 0;
    }

    @Override
    public TeamEffectList getEffects() {
        return effects;
    }

    @Override
    public TrainerAction getAction() {
        return TrainerAction.FIGHT;
    }

    @Override
    public void resetUsed() {
        wildPokemon.setUsed(true);
    }

    @Override
    public List<PartyPokemon> getTeam() {
        return Collections.singletonList(wildPokemon);
    }

    @Override
    public boolean blackout(Battle b) {
        return !wildPokemon.canFight();
    }

    @Override
    public String getStartBattleMessage() {
        return "Wild " + wildPokemon.getName() + " appeared!";
    }

    @Override
    public EnterBattleMessageGetter getEnterBattleMessage() {
        // No additional message when the Pokemon 'enters' battle, it's just the start when they appear
        return enterer -> "";
    }

    @Override
    public void enterBattle() {
        // Just in case
        this.wildPokemon.resetAttributes();
        this.effects.reset();
    }

    @Override
    public int maxPokemonAllowed() {
        return Trainer.MAX_POKEMON;
    }
}
