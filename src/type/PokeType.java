package type;

import pokemon.active.PartyPokemon;
import pokemon.species.PokemonNamesies;
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

    // Returns relevant types as an array
    public Type[] getTypes() {
        if (this.isDualTyped()) {
            return new Type[] { firstType, secondType };
        } else {
            return new Type[] { firstType };
        }
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
        return Arrays.asList(this.getTypes()).iterator();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PokeType) {
            PokeType type = (PokeType)obj;
            return this.firstType == type.firstType && this.secondType == type.secondType;
        }
        return false;
    }

    public static Color[] getColors(PokemonNamesies pokemonNamesies) {
        return pokemonNamesies.getInfo().getType().getColors();
    }

    public static Color[] getColors(PartyPokemon p) {
        return (p.isEgg() ? new PokeType(Type.NORMAL) : p.getActualType()).getColors();
    }
}
