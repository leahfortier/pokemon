package battle.effect.interfaces;

import battle.ActivePokemon;
import battle.Battle;
import battle.effect.pokemon.PokemonEffect;
import battle.effect.team.TeamEffect;
import main.Global;
import message.Messages;

public interface EffectReleaser {

    default void release(Battle b, ActivePokemon released, String releaseMessage) {
        Messages.add(releaseMessage);

        if (this instanceof PokemonEffect) {
            released.getEffects().remove((PokemonEffect)this);
        } else if (this instanceof TeamEffect) {
            b.getTrainer(released).getEffects().remove((TeamEffect)this);
        } else {
            Global.error("Invalid release object " + this.getClass().getSimpleName());
        }
    }
}
