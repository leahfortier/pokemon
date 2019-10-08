package test.pokemon;

import org.junit.Assert;
import org.junit.Test;
import pokemon.active.Gender;
import pokemon.species.PokemonInfo;
import pokemon.species.PokemonNamesies;
import test.general.BaseTest;

public class GenderTest extends BaseTest {
    @Test
    public void genderStringTest()  {
        genderStringTest(PokemonNamesies.MAGNEMITE, Gender.GENDERLESS_VALUE, "Genderless");
        genderStringTest(PokemonNamesies.HITMONLEE, 0, "100% Male");
        genderStringTest(PokemonNamesies.BULBASAUR, 1, "87.5% Male, 12.5% Female");
        genderStringTest(PokemonNamesies.ALAKAZAM, 2, "75% Male, 25% Female");
        genderStringTest(PokemonNamesies.MILOTIC, 4, "50% Male, 50% Female");
        genderStringTest(PokemonNamesies.GOTHITA, 6, "25% Male, 75% Female");
        genderStringTest(PokemonNamesies.LITLEO, 7, "12.5% Male, 87.5% Female");
        genderStringTest(PokemonNamesies.JYNX, 8, "100% Female");
    }

    private void genderStringTest(PokemonNamesies pokemonNamesies, int femaleRatio, String expected) {
        PokemonInfo pokemonInfo = pokemonNamesies.getInfo();
        Assert.assertEquals(femaleRatio, pokemonInfo.getFemaleRatio());
        Assert.assertEquals(expected, Gender.getGenderString(pokemonInfo));
    }

    @Test
    public void genderAppliesTest()  {
        // Magnemite is Genderless
        genderAppliesTest(PokemonNamesies.MAGNEMITE, Gender.MALE, false);
        genderAppliesTest(PokemonNamesies.MAGNEMITE, Gender.FEMALE, false);
        genderAppliesTest(PokemonNamesies.MAGNEMITE, Gender.GENDERLESS, true);

        // Jynx is always Female
        genderAppliesTest(PokemonNamesies.JYNX, Gender.MALE, false);
        genderAppliesTest(PokemonNamesies.JYNX, Gender.FEMALE, true);
        genderAppliesTest(PokemonNamesies.JYNX, Gender.GENDERLESS, false);

        // Hitmonlee is always Male
        genderAppliesTest(PokemonNamesies.HITMONLEE, Gender.MALE, true);
        genderAppliesTest(PokemonNamesies.HITMONLEE, Gender.FEMALE, false);
        genderAppliesTest(PokemonNamesies.HITMONLEE, Gender.GENDERLESS, false);

        // Pokemon that can be male or female (of different ratios)
        genderAppliesTest(PokemonNamesies.BULBASAUR);
        genderAppliesTest(PokemonNamesies.ALAKAZAM);
        genderAppliesTest(PokemonNamesies.MILOTIC);
        genderAppliesTest(PokemonNamesies.GOTHITA);
        genderAppliesTest(PokemonNamesies.LITLEO);
    }

    // Used for Pokemon that can be either male or female
    private void genderAppliesTest(PokemonNamesies pokemonNamesies) {
        genderAppliesTest(pokemonNamesies, Gender.MALE, true);
        genderAppliesTest(pokemonNamesies, Gender.FEMALE, true);
        genderAppliesTest(pokemonNamesies, Gender.GENDERLESS, false);
    }

    private void genderAppliesTest(PokemonNamesies pokemonNamesies, Gender gender, boolean applies) {
        Assert.assertEquals(applies, gender.genderApplies(pokemonNamesies.getInfo()));
    }

    @Test
    public void oppositeGenderTest() {
        Assert.assertEquals(Gender.FEMALE, Gender.MALE.getOppositeGender());
        Assert.assertEquals(Gender.MALE, Gender.FEMALE.getOppositeGender());
        Assert.assertEquals(Gender.GENDERLESS, Gender.GENDERLESS.getOppositeGender());

        TestPokemon magnemite = genderTestPokemon(PokemonNamesies.MAGNEMITE, Gender.GENDERLESS);
        TestPokemon voltorb = genderTestPokemon(PokemonNamesies.VOLTORB, Gender.GENDERLESS);
        TestPokemon jynx = genderTestPokemon(PokemonNamesies.JYNX, Gender.FEMALE);
        TestPokemon miltank = genderTestPokemon(PokemonNamesies.MILTANK, Gender.FEMALE);
        TestPokemon hitmonlee = genderTestPokemon(PokemonNamesies.HITMONLEE, Gender.MALE);
        TestPokemon hitmonchan = genderTestPokemon(PokemonNamesies.HITMONCHAN, Gender.MALE);

        // Genderless vs genderless
        Assert.assertFalse(Gender.oppositeGenders(magnemite, voltorb));

        // Male vs female
        Assert.assertTrue(Gender.oppositeGenders(hitmonchan, jynx));

        // Female vs male
        Assert.assertTrue(Gender.oppositeGenders(miltank, hitmonlee));

        // Male vs male
        Assert.assertFalse(Gender.oppositeGenders(hitmonchan, hitmonlee));

        // Female vs female
        Assert.assertFalse(Gender.oppositeGenders(jynx, miltank));

        // Male vs genderless
        Assert.assertFalse(Gender.oppositeGenders(hitmonchan, magnemite));

        // Genderless vs male
        Assert.assertFalse(Gender.oppositeGenders(voltorb, hitmonlee));

        // Female vs genderless
        Assert.assertFalse(Gender.oppositeGenders(jynx, magnemite));

        // Genderless vs female
        Assert.assertFalse(Gender.oppositeGenders(voltorb, miltank));
    }

    private TestPokemon genderTestPokemon(PokemonNamesies name, Gender gender) {
        TestPokemon pokemon = TestPokemon.newPlayerPokemon(name);
        Assert.assertEquals(gender, pokemon.getGender());
        return pokemon;
    }
}
