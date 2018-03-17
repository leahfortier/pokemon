package battle.effect.attack;

import battle.ActivePokemon;
import battle.Battle;
import battle.attack.AttackInterface;
import battle.effect.InvokeInterfaces.PowerChangeEffect;
import battle.effect.pokemon.PokemonEffectNamesies;

public interface PowerCountMove extends AttackInterface, PowerChangeEffect {
    boolean doubleDefenseCurled();

    @Override
    default double getMultiplier(Battle b, ActivePokemon user, ActivePokemon victim) {
        return Math.min(user.getCount(), 5)*(this.doubleDefenseCurled() && user.hasEffect(PokemonEffectNamesies.USED_DEFENSE_CURL) ? 2 : 1);
    }
}
