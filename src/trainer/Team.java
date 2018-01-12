package trainer;

import battle.Battle;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.TeamEffect;
import pokemon.ActivePokemon;

import java.util.List;

// THIS IS A DUMB NAME SOMEONE HELP ME RENAME IT
public interface Team {
    ActivePokemon front();
    int getTeamIndex(ActivePokemon teamMember);
    List<TeamEffect> getEffects();
    boolean hasEffect(EffectNamesies effect);
    void addEffect(TeamEffect e);
    List<ActivePokemon> getTeam();
    boolean blackout(Battle b);
    void resetEffects();
    void resetUsed();

    default boolean removeEffect(TeamEffect effect) {
        return this.getEffects().remove(effect);
    }
}
