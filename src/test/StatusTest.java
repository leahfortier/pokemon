package test;

import battle.Battle;
import battle.effect.status.Status;
import battle.effect.status.StatusCondition;
import org.junit.Assert;
import org.junit.Test;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;
import pokemon.Stat;

/*
TODO:
    Give each status to a raticate
    fail for two statuses -- maybe have a loop that tests all combos
    fail for fainted on a nonzero health poke
    rapidash can't be burned
    change dragonair's in-battle type to fire and then try to burn it -- should fail
    bulbasaur can't be poisoned
    jolteon can't be paralyzed

    speed is decreased from paralysis
    attack is halved from burn

    damage works correctly -- assert hp change is in range or deadsies
    deadsies -- set hp to 1, then murder -- make sure its's dead
 */
public class StatusTest {
    @Test
    public void testGiveStatus() {
        ActivePokemon uglyFace = new TestPokemon(PokemonNamesies.RATICATE);
    }

    @Test
    public void testStatChanges() {
        testStatChange(StatusCondition.PARALYZED, Stat.SPEED, .25);
        testStatChange(StatusCondition.BURNED, Stat.ATTACK, .5);

        // TODO: Test Guts
    }

    private void testStatChange(StatusCondition statusCondition, Stat stat, double ratio) {
        ActivePokemon mahBoi = new TestPokemon(PokemonNamesies.RAPIDASH);
        ActivePokemon uglyFace = new TestPokemon(PokemonNamesies.WATCHOG);

        Battle b = TestBattle.create(mahBoi, uglyFace);

        int original = Stat.getStat(stat, uglyFace, mahBoi, b);

        Status.giveStatus(b, uglyFace, uglyFace, statusCondition);
        int afterStatus = Stat.getStat(stat, uglyFace, mahBoi, b);
        Assert.assertTrue((int)(original*ratio) == afterStatus);

        uglyFace.removeStatus();
        int afterRemoved = Stat.getStat(stat, uglyFace, mahBoi, b);
        Assert.assertTrue(original == afterRemoved);
    }
}
