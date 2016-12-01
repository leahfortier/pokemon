package gui.view.battle;

import battle.effect.status.StatusCondition;
import main.Type;
import pokemon.Gender;

// TODO: Should those set methods up there be inside this class and should it be moved to its own file?
// A class to hold the state of a Pokemon
class PokemonState { // todo
    public int maxHp, hp, imageNumber, level;
    public String name;
    public StatusCondition status;
    public Type[] type;
    public float expRatio;
    public boolean shiny;
    public boolean caught;
    public Gender gender;

    PokemonState() {
        type = new Type[2];
    }
}
