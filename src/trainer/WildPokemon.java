package trainer;

import battle.ActivePokemon;
import battle.Battle;
import battle.Battle.EnterBattleMessageGetter;
import battle.effect.generic.Effect;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.TeamEffect;
import main.Global;
import pokemon.PartyPokemon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WildPokemon implements Opponent, Serializable {
    private final ActivePokemon wildPokemon;
    private List<TeamEffect> effects;

    public WildPokemon(ActivePokemon wildPokemon) {
        if (wildPokemon.isPlayer()) {
            Global.error("Wild pokemon cannot be a player Pokemon.");
        }

        this.wildPokemon = wildPokemon;
        this.effects = new ArrayList<>();
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
    public List<TeamEffect> getEffects() {
        return effects;
    }

    @Override
    public void resetEffects() {
        effects = new ArrayList<>();
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
    public boolean hasEffect(EffectNamesies effect) {
        return Effect.hasEffect(effects, effect);
    }

    @Override
    public void addEffect(TeamEffect e) {
        effects.add(e);
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
        this.resetEffects();
    }

    @Override
    public int maxPokemonAllowed() {
        return Trainer.MAX_POKEMON;
    }
}
