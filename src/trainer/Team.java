package trainer;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.generic.EffectNamesies;
import battle.effect.generic.TeamEffect;
import pokemon.PartyPokemon;

import java.util.List;
import java.util.stream.Collectors;

// THIS IS A DUMB NAME SOMEONE HELP ME RENAME IT
public interface Team {
    List<PartyPokemon> getTeam();
    ActivePokemon front();
    int getTeamIndex(ActivePokemon teamMember);

    List<TeamEffect> getEffects();
    void addEffect(TeamEffect e);
    boolean hasEffect(EffectNamesies effect);
    void resetEffects();

    String getEnterBattleMessage(ActivePokemon enterer);
    void enterBattle();

    TrainerAction getAction();
    boolean blackout(Battle b);
    void resetUsed();

    default boolean removeEffect(TeamEffect effect) {
        return this.getEffects().remove(effect);
    }

    // Returns ONLY the active pokemon on the team (AKA NOT EGGS)
    // Note: Still returns fainted Pokemon
    default List<ActivePokemon> getActiveTeam() {
        return this.getTeam()
                   .stream()
                   .filter(p -> p instanceof ActivePokemon)
                   .map(p -> (ActivePokemon)p)
                   .collect(Collectors.toList());
    }
}
