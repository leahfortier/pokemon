package test;

import battle.ActivePokemon;
import main.Game;
import org.junit.Assert;
import org.junit.Test;
import pokemon.active.EffortValues;
import pokemon.species.PokemonNamesies;
import save.Save;
import trainer.player.Player;
import trainer.player.medal.Medal;
import trainer.player.medal.MedalCase;

import java.util.EnumSet;
import java.util.Set;

public class MedalTest extends BaseTest {
    @Test
    public void lastMedalTest() {
        MedalCase medalCase = Game.getPlayer().getMedalCase();

        Set<Medal> otherMedals = EnumSet.complementOf(EnumSet.of(Medal.TOP_MEDALIST));
        Assert.assertEquals(Medal.values().length - 1, otherMedals.size());

        for (Medal medal : otherMedals) {
            Assert.assertFalse(medalCase.hasMedal(Medal.TOP_MEDALIST));
            medalCase.earnMedal(medal);
        }

        // Awarded when the player has every medal other than this one
        Assert.assertTrue(medalCase.hasMedal(Medal.TOP_MEDALIST));
    }

    @Test
    public void evsTest() {
        Player player = Game.getPlayer();

        ActivePokemon bulby = new ActivePokemon(PokemonNamesies.BULBASAUR, 5, false, true);
        Assert.assertFalse(player.getMedalCase().hasMedal(Medal.TRAINED_TO_MAX_POTENTIAL));

        bulby.addEVs(new int[] { 1, 1, 1, 1, 1, 1 });
        Assert.assertFalse(player.getMedalCase().hasMedal(Medal.TRAINED_TO_MAX_POTENTIAL));

        bulby.addEVs(new int[] { 1, 1, EffortValues.MAX_EVS, 1, 1, 1 });
        Assert.assertFalse(player.getMedalCase().hasMedal(Medal.TRAINED_TO_MAX_POTENTIAL));

        bulby.addEVs(new int[] { EffortValues.MAX_EVS, EffortValues.MAX_EVS, EffortValues.MAX_EVS, EffortValues.MAX_EVS, EffortValues.MAX_EVS, EffortValues.MAX_EVS });
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
