package trainer;

import battle.ActivePokemon;
import battle.Battle;
import battle.Battle.EnterBattleMessageGetter;
import battle.effect.EffectList;
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

    EffectList<TeamEffect> getEffects();

    EnterBattleMessageGetter getEnterBattleMessage();
    void enterBattle();

    TrainerAction getAction();
    boolean blackout(Battle b);
    void resetUsed();

    default boolean hasEffect(EffectNamesies effect) {
        return this.getEffects().hasEffect(effect);
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
