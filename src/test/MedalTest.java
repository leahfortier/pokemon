package test;

import main.Game;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import pokemon.Stat;
import trainer.player.Player;
import trainer.player.medal.Medal;

public class MedalTest {

    @Test
    public void evsTest() {
        TestGame.setNewPlayer(new Player());
        Player player = Game.getPlayer();

        ActivePokemon bulby = new ActivePokemon(PokemonNamesies.BULBASAUR, 5, false, true);
        Assert.assertFalse(player.getMedalCase().hasMedal(Medal.TRAINED_TO_MAX_POTENTIAL));

        bulby.addEVs(new int[] { 1, 1, 1, 1, 1, 1 });
        Assert.assertFalse(player.getMedalCase().hasMedal(Medal.TRAINED_TO_MAX_POTENTIAL));

        bulby.addEVs(new int[] { 1, 1, Stat.MAX_EVS, 1, 1, 1 });
        Assert.assertFalse(player.getMedalCase().hasMedal(Medal.TRAINED_TO_MAX_POTENTIAL));

        bulby.addEVs(new int[] { Stat.MAX_EVS, Stat.MAX_EVS, Stat.MAX_EVS, Stat.MAX_EVS, Stat.MAX_EVS, Stat.MAX_EVS });
        Assert.assertTrue(player.getMedalCase().hasMedal(Medal.TRAINED_TO_MAX_POTENTIAL));
    }
}
