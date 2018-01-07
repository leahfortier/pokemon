package test;

import main.Game;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import pokemon.Stat;
import trainer.player.Player;
import trainer.player.medal.Medal;
import util.save.Save;

public class MedalTest extends BaseTest {
    @Test
    public void evsTest() {
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

    @Test
    public void saveTest() {
        Player player = Game.getPlayer();
        player.setFileNum(-1);

        for (int i = 0; i < 10; i++) {
            // The real test is to make sure this doesn't crash because of serialization problems
            Save.save();
        }

        Assert.assertTrue(player.getMedalCase().hasMedal(Medal.STEP_BY_STEP_SAVER));
        Save.deleteSave(-1);
    }
}
