package trainer;

import battle.Battle;
import battle.effect.generic.Effect;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.TeamEffect;
import main.Global;
import pokemon.ActivePokemon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WildPokemon implements Opponent, Serializable {
    private final ActivePokemon wildPokemon;
    private List<TeamEffect> effects;
    
    public WildPokemon(ActivePokemon wildPokemon) {
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
    public void resetUsed() {
        wildPokemon.getAttributes().setUsed(true);
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
    public List<ActivePokemon> getTeam() {
        List<ActivePokemon> list = new ArrayList<>();
        list.add(wildPokemon);
        return list;
    }

    @Override
    public boolean blackout(Battle b) {
        return !wildPokemon.canFight();
    }

    @Override
    public int maxPokemonAllowed() {
        return Trainer.MAX_POKEMON;
    }
}
