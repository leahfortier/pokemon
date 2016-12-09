package pokemon.evolution;

import java.util.Scanner;

public enum EvolutionType {
    NONE(in -> new NoEvolution()),
    GENDER(in -> new GenderEvolution(in.next(), getEvolution(in))),
    STAT(in -> new StatEvolution(in.next(), in.next(), in.next(), getEvolution(in))),
    LEVEL(in -> new LevelUpEvolution(in.nextInt(), in.nextInt())),
    ITEM(in -> new ItemEvolution(in.nextInt(), in.nextLine().trim())),
    MOVE(in -> new MoveEvolution(in.nextInt(), in.nextLine().trim())),
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

    public static Evolution getEvolution(Scanner in) {
        return valueOf(in.next().toUpperCase()).readEvolution(in);
    }
}
