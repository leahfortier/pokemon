package pattern;

import battle.attack.AttackNamesies;
import battle.attack.Move;
import item.ItemNamesies;
import main.Global;
import pokemon.Gender;
import pokemon.Nature;
import pokemon.PartyPokemon;
import pokemon.PokemonNamesies;
import util.GeneralUtils;
import util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PokemonMatcher implements JsonMatcher {
    private PokemonNamesies namesies;
    private String nickname;
    private Integer level;
    private Boolean isStarterEgg;
    private Boolean isShiny;
    private List<AttackNamesies> moves;
    private Boolean isEgg;
    private ItemNamesies holdItem;
    private Gender gender;
    private Nature nature;

    public PokemonMatcher(PokemonNamesies namesies, int level) {
        this(namesies, null, level, false, null, null);
    }

    public PokemonMatcher(PokemonNamesies namesies, String nickname, int level, boolean isShiny, AttackNamesies[] moves, ItemNamesies holdItem) {
        if (namesies == null) {
            Global.error("Pokemon namesies cannot be null for a Pokemon Matcher");
        }

        if (level < 1 || level > PartyPokemon.MAX_LEVEL) {
            Global.error("Invalid level " + level + " for pokemon " + namesies);
        }

        this.namesies = namesies;
        this.nickname = nickname;
        this.level = level;
        this.isShiny = isShiny;
        this.holdItem = holdItem;

        if (moves != null) {
            this.moves = Arrays.stream(moves).filter(Objects::nonNull).collect(Collectors.toList());
            if (this.moves.isEmpty()) {
                this.moves = null;
            }
        }
    }

    private PokemonMatcher() {}

    public PokemonNamesies getNamesies() {
        return this.namesies;
    }

    public String getNickname() {
        return this.nickname == null ? StringUtils.empty() : this.nickname;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return GeneralUtils.getIntegerValue(this.level);
    }

    public boolean isEgg() {
        return GeneralUtils.getBooleanValue(this.isEgg);
    }

    public boolean isStarterEgg() {
        return GeneralUtils.getBooleanValue(this.isStarterEgg);
    }

    public boolean isShiny() {
        return GeneralUtils.getBooleanValue(this.isShiny);
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

    public void setHoldItem(ItemNamesies holdItem) {
        this.holdItem = holdItem;
    }

    public boolean hasHoldItem() {
        return this.holdItem != null;
    }

    public ItemNamesies getHoldItem() {
        return this.holdItem;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public boolean hasGender() {
        return this.gender != null;
    }

    public Gender getGender() {
        return this.gender;
    }

    public void setNature(Nature nature) {
        this.nature = nature;
    }

    public boolean hasNature() {
        return this.nature != null;
    }

    public Nature getNature() {
        return this.nature;
    }

    public static PokemonMatcher createEggMatcher(PokemonNamesies eggy) {
        PokemonMatcher matcher = new PokemonMatcher();
        matcher.namesies = eggy;
        matcher.isEgg = true;

        return matcher;
    }
}
