package pokemon.evolution;

import java.util.Scanner;

public enum EvolutionType {
    NONE(in -> new NoEvolution()),
    GENDER(in -> new GenderEvolution(in.next(), getBaseEvolution(in))),
    TYPE_PARTY(in -> new TypePartyEvolution(in.next(), getBaseEvolution(in))),
    TIME(in -> new TimeEvolution(in.next(), getBaseEvolution(in))),
    EXTRA(in -> new ExtraEvolution(in.next(), in.next(), getBaseEvolution(in))),
    STAT(in -> new StatEvolution(in.next(), in.next(), in.next(), getBaseEvolution(in))),
    LEVEL(in -> new LevelUpEvolution(in.next(), in.nextInt())),
    ITEM(in -> new ItemEvolution(in.next(), in.next())),
    MOVE(in -> new MoveEvolution(in.next(), in.next())),
    MULTI(in -> {
        Evolution[] evolutions = new Evolution[in.nextInt()];
        for (int i = 0; i < evolutions.length; i++) {
            evolutions[i] = getEvolution(in);
        }

        return new MultipleEvolution(evolutions);
    });

    private final EvolutionReader evolutionReader;

    EvolutionType(EvolutionReader evolutionReader) {
        this.evolutionReader = evolutionReader;
    }

    private Evolution readEvolution(Scanner in) {
        return this.evolutionReader.readEvolution(in);
    }

    private interface EvolutionReader {
        Evolution readEvolution(Scanner in);
    }

    private static BaseEvolution getBaseEvolution(Scanner in) {
        return (BaseEvolution)getEvolution(in);
    }

    public static Evolution getEvolution(Scanner in) {
        return valueOf(in.next().toUpperCase()).readEvolution(in);
    }
}
