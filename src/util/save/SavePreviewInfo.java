package util.save;

import java.util.Scanner;

public class SavePreviewInfo {
    private final String name;
    private final long seconds;
    private final int badges;
    private final int pokemonSeen;
    
    SavePreviewInfo(Scanner in) {
        this.name = in.next();
        this.seconds = in.nextLong();
        this.badges = in.nextInt();
        this.pokemonSeen = in.nextInt();
    }
    
    public String getName() {
        return this.name;
    }
    
    public long getSeconds() {
        return this.seconds;
    }
    
    public int getBadges() {
        return this.badges;
    }
    
    public int getPokemonSeen() {
        return this.pokemonSeen;
    }
}
