package trainer;

import battle.Battle;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.TeamEffect;
import battle.ActivePokemon;
import pokemon.PartyPokemon;

import java.util.List;
import java.util.stream.Collectors;

// THIS IS A DUMB NAME SOMEONE HELP ME RENAME IT
public interface Team {
    ActivePokemon front();
    int getTeamIndex(ActivePokemon teamMember);
    List<TeamEffect> getEffects();
    boolean hasEffect(EffectNamesies effect);
    void addEffect(TeamEffect e);
    List<PartyPokemon> getTeam();
    boolean blackout(Battle b);
    void resetEffects();
    void resetUsed();

    default boolean removeEffect(TeamEffect effect) {
        return this.getEffects().remove(effect);
    }

    default List<ActivePokemon> getActiveTeam() {
        return this.getTeam()
                   .stream()
                   .filter(p -> p instanceof ActivePokemon)
                   .map(p -> (ActivePokemon)p)
                   .collect(Collectors.toList());
    }
}
