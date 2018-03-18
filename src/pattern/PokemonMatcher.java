package pattern;

import battle.ActivePokemon;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import item.ItemNamesies;
import main.Global;
import pokemon.active.Gender;
import pokemon.active.Nature;
import pokemon.active.PartyPokemon;
import pokemon.breeding.Eggy;
import pokemon.species.PokemonInfo;
import pokemon.species.PokemonNamesies;
import util.GeneralUtils;
import util.serialization.JsonMatcher;
import util.string.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PokemonMatcher implements JsonMatcher {
    private PokemonNamesies namesies;
    private String nickname;
    private Integer level;
    private Boolean isStarter;
    private Boolean isShiny;
    private List<AttackNamesies> moves;
    private Boolean isEgg;
    private ItemNamesies holdItem;
    private Gender gender;
    private Nature nature;

    public PokemonMatcher(PokemonNamesies namesies, int level) {
        this(namesies, null, level, false, null, null);
    }

    public PokemonMatcher(PokemonNamesies namesies, String nickname, int level, boolean isShiny, List<AttackNamesies> moves, ItemNamesies holdItem) {
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
            this.moves = moves.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (this.moves.isEmpty()) {
                this.moves = null;
            }
        }
    }

    private PokemonMatcher() {}

    public PokemonNamesies getNamesies() {
        if (GeneralUtils.getBooleanValue(this.isStarter)) {
            if (this.namesies != null) {
                Global.error("Cannot set namesies for random starter pokemon.");
            }

            return PokemonInfo.getRandomStarterPokemon();
        }

        return this.namesies;
    }

    public String getNickname() {
        return this.nickname == null ? "" : this.nickname;
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

    public boolean isShiny() {
        return GeneralUtils.getBooleanValue(this.isShiny);
    }

    public boolean hasMoves() {
        return this.moves != null && !this.moves.isEmpty();
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

    public Gender getGender() {
        return this.gender;
    }

    public void setNature(Nature nature) {
        this.nature = nature;
    }

    public Nature getNature() {
        return this.nature;
    }

    public PartyPokemon createPokemon(boolean isWild, boolean isPlayer) {
        PokemonNamesies namesies = this.getNamesies();
        List<Move> moves = this.moves == null ? null : this.moves.stream().map(Move::new).collect(Collectors.toList());

        if (this.isEgg()) {
            if (!isPlayer) {
                Global.error("Enemy trainers cannot have eggs.");
            } else if (isWild) {
                Global.error("Eggs cannot be wild.");
            } else if (this.hasHoldItem()) {
                Global.error("Eggs cannot hold items.");
            } else if (!StringUtils.isNullOrEmpty(this.getNickname())) {
                Global.error("Eggs cannot have nicknames.");
            } else if (this.getLevel() != 1) {
                Global.error("Eggs can only be level 1.");
            }

            return new Eggy(namesies, this.isShiny, moves, this.gender, this.nature);
        } else {
            ActivePokemon pokemon = new ActivePokemon(
                    namesies, this.getLevel(), isWild, isPlayer, this.getNickname(),
                    this.isShiny, moves, this.gender, this.nature
            );

            if (this.hasHoldItem()) {
                pokemon.giveItem(this.holdItem);
            }

            return pokemon;
        }
    }

    public static PokemonMatcher createEggMatcher(PokemonNamesies eggy) {
        PokemonMatcher matcher = new PokemonMatcher();
        matcher.namesies = eggy;
        matcher.isEgg = true;

        return matcher;
    }
}
