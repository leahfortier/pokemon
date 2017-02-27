package util.save;

import java.util.Scanner;

public class SavePreviewInfo {
    private final String name;
    private final long time;
    private final int badges;
    private final int pokemonSeen;

    SavePreviewInfo(Scanner in) {
        this.name = in.next();
        this.time = in.nextLong();
        this.badges = in.nextInt();
        this.pokemonSeen = in.nextInt();
    }

    public String getName() {
        return this.name;
    }

    public long getTime() {
        return this.time;
    }

    public int getBadges() {
        return this.badges;
    }

    public int getPokemonSeen() {
        return this.pokemonSeen;
    }
}
