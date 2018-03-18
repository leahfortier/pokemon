package type;

import pokemon.active.PartyPokemon;
import pokemon.species.PokemonInfo;
import util.serialization.Serializable;

import java.awt.Color;
import java.util.Arrays;
import java.util.Iterator;

public class PokeType implements Iterable<Type>, Serializable {
    private static final long serialVersionUID = 1L;

    private final Type firstType;
    private final Type secondType;

    public PokeType(Type type) {
        this(type, Type.NO_TYPE);
    }

    public PokeType(Type firstType, Type secondType) {
        this.firstType = firstType;
        this.secondType = secondType;
    }

    public boolean isType(Type type) {
        return this.firstType == type || this.secondType == type;
    }

    public Type getFirstType() {
        return firstType;
    }

    public Type getSecondType() {
        return secondType;
    }

    public boolean isDualTyped() {
        return this.getSecondType() != Type.NO_TYPE;
    }

    public Color[] getColors() {
        return new Color[] {
                firstType.getColor(),
                (this.isDualTyped() ? secondType : firstType).getColor()
        };
    }

    // If single type, just returns the type name, Ex: Fire
    // If dual typed, return both names separated by a '/', Ex: Fire/Flying
    @Override
    public String toString() {
        return firstType.getName() + (this.isDualTyped() ? "/" + this.secondType.getName() : "");
    }

    @Override
    public Iterator<Type> iterator() {
        return Arrays.asList(firstType, secondType).iterator();
    }

    public static Color[] getColors(PokemonInfo p) {
        return p.getType().getColors();
    }

    public static Color[] getColors(PartyPokemon p) {
        return (p.isEgg() ? new PokeType(Type.NORMAL) : p.getActualType()).getColors();
    }
}
