package pattern;

import battle.Attack;
import battle.Move;
import item.Item;
import item.hold.HoldItem;
import main.Global;
import namesies.PokemonNamesies;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PokemonMatcher {
    private static final Pattern pokemonPattern =
            Pattern.compile(
                    "(?:" +
                        "([\\w ]+ )" +  // Group 1: Pokemon name
                        "(\\d+)" +      // Group 2: Pokemon level
                        "(.*)" +        // Group 3: Additional parameters
                    ")|" +
                    "(RandomEgg)"       // Group 4: Jk it's just a random egg
            );

    private static final Pattern pokemonParameterPattern =
            Pattern.compile(
                    "(Shiny)|" +                // Group 1: Pokemon is shiny
                    "(?:(Moves:) " +            // Group 2: Pokemon has specific moves
                            "([\\w ]+), " +     // Group 3: First specified move
                            "([\\w ]+), " +     // Group 4: Second specified move
                            "([\\w ]+), " +     // Group 5: Third specified move
                            "([\\w ]+)" +       // Group 6: Fourth specified move
                    ")|" +
                    "(Egg)|" +                  // Group 7: Pokemon is an egg
                    "(?:(Item:)" +              // Group 8: Pokemon is holding an item
                            " " +
                            "([\\w \\-'.]+)" +  // Group 9: Item name
                    ")",
                    Pattern.UNICODE_CHARACTER_CLASS
            );

    private PokemonNamesies namesies;
    private int level;
    private boolean isRandomEgg;
    private boolean isShiny;
    private List<Move> moves;
    private boolean isEgg;
    private HoldItem holdItem;

    public static PokemonMatcher matchPokemonDescription(final String pokemonDescription) {
        Matcher matcher = pokemonPattern.matcher(pokemonDescription);
        if (!matcher.matches()) {
            Global.error("Pokemon description " + pokemonDescription + " does not match regex.");
        }

        // Random egg
        if (matcher.group(4) != null) {
            final PokemonMatcher pokemonMatcher = new PokemonMatcher();
            pokemonMatcher.isRandomEgg = true;
            return pokemonMatcher;
        }

        final String pokemonName = matcher.group(1);
        final String level = matcher.group(2);
        final String parameters = matcher.group(3);

        return new PokemonMatcher(pokemonName, level, parameters);
    }

    public static PokemonMatcher matchPokemonParameters(final String pokemonName, final String level, final String parameters) {
        return new PokemonMatcher(pokemonName, level, parameters);
    }

    private PokemonMatcher() {}

    private PokemonMatcher(final String pokemonName, final String level, final String parameters) {

        this.namesies = PokemonNamesies.getValueOf(pokemonName);
        this.level = Integer.parseInt(level);

        Matcher params = pokemonParameterPattern.matcher(parameters);
        while (params.find()) {
            if (params.group(1) != null) {
                this.isShiny = true;
            }

            if (params.group(2) != null) {
                this.moves = new ArrayList<>();
                for (int i = 0; i < Move.MAX_MOVES; i++) {
                    String attackName = params.group(3 + i);
                    if (!attackName.equals("None")) { // TODO: Use constant
                        moves.add(new Move(Attack.getAttackFromName(attackName)));
                    }
                }
            }

            if (params.group(7) != null) {
                this.isEgg = true;
            }

            if (params.group(8) != null) {
                String itemName = params.group(9);
                Item item = Item.getItemFromName(itemName);
                if (item.isHoldable()) {
                    this.holdItem = (HoldItem)item;
                }
                else {
                    Global.error(itemName +" is not a hold item. Pokemon: " + this.namesies.getName());
                }
            }
        }
    }

    public PokemonNamesies getNamesies() {
        return this.namesies;
    }

    public int getLevel() {
        return this.level;
    }

    public boolean isRandomEgg() {
        return this.isRandomEgg;
    }

    public boolean isShiny() {
        return this.isShiny;
    }

    public boolean hasMoves() {
        return this.moves != null;
    }

    public List<Move> getMoves() {
        return this.moves;
    }

    public List<String> getMoveNames() {
        return this.moves.stream()
                .map(move -> move.getAttack().getName())
                .collect(Collectors.toList());
    }

    public boolean isEgg() {
        return this.isEgg;
    }

    public boolean hasHoldItem() {
        return this.holdItem != null;
    }

    public HoldItem getHoldItem() {
        return this.holdItem;
    }
}
