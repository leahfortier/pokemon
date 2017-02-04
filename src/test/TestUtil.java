package test;

class TestUtil {
    static boolean healthRatioMatch(TestPokemon pokemon, double fraction) {
        return (int)(Math.ceil(fraction*pokemon.getMaxHP())) == pokemon.getHP();
    }
}
