package pattern;

import battle.attack.AttackNamesies;
import battle.attack.Move;
import item.ItemNamesies;
import main.Global;
import pokemon.ActivePokemon;
import pokemon.PokemonNamesies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PokemonMatcher {
    private PokemonNamesies namesies;
    private Integer level;
    private Boolean isRandomEgg;
    private Boolean isShiny;
    private List<AttackNamesies> moves;
    private Boolean isEgg;
    private ItemNamesies holdItem;

    public PokemonMatcher(PokemonNamesies namesies, int level, boolean isShiny, AttackNamesies[] moves) {
        if (namesies == null) {
            Global.error("Pokemon namesies cannot be null for a Pokemon Matcher");
        }

        if (level < 1 || level > ActivePokemon.MAX_LEVEL) {
            Global.error("Invalid level " + level + " for pokemon " + namesies);
        }

        this.namesies = namesies;
        this.level = level;

        if (isShiny) {
            this.isShiny = true;
        }

        this.moves = Arrays.stream(moves).filter(move -> move != null).collect(Collectors.toList());
        if (this.moves.isEmpty()) {
            this.moves = null;
        }
    }

    public PokemonNamesies getNamesies() {
        return this.namesies;
    }

    public int getLevel() {
        return this.level == null ? 0 : this.level;
    }

    public boolean isRandomEgg() {
        // TODO: Should make a util method for this
        return this.isRandomEgg == null ? false : this.isRandomEgg;
    }

    public boolean isShiny() {
        return this.isShiny == null ? false : this.isShiny;
    }

    public boolean hasMoves() {
        return this.moves != null;
    }

    public List<Move> getMoves() {
        List<Move> moves = new ArrayList<>();
        if (this.moves != null) {
            moves.addAll(this.moves.stream().map(Move::new).collect(Collectors.toList()));
        }

        return moves;
    }

    public List<AttackNamesies> getMoveNames() {
        return this.moves;
    }

    public boolean isEgg() {
        return this.isEgg == null ? false : this.isEgg;
    }

    public boolean hasHoldItem() {
        return this.holdItem != null;
    }

    public ItemNamesies getHoldItem() {
        return this.holdItem;
    }
}
