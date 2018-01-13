package pokemon;

import battle.ActivePokemon;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.effect.status.Status;
import draw.DrawUtils;
import item.Item;
import item.ItemNamesies;
import item.hold.HoldItem;
import main.Game;
import main.Global;
import pattern.PokemonMatcher;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import trainer.player.medal.Medal;
import trainer.player.medal.MedalCase;
import trainer.player.medal.MedalTheme;
import type.Type;
import util.RandomUtils;
import util.StringUtils;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class PartyPokemon implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MAX_LEVEL = 100;
    public static final int MAX_NAME_LENGTH = 10;

    public static final String TINY_EGG_IMAGE_NAME = "egg-small";
    public static final String BASE_EGG_IMAGE_NAME = "egg";
    public static final String SPRITE_EGG_IMAGE_NAME = "EggSprite";

    private static final String[][] characteristics =
        {{"Loves to eat",            "Proud of its power",      "Sturdy body",            "Highly curious",        "Strong willed",     "Likes to run"},
         {"Takes plenty of siestas", "Likes to thrash about",   "Capable of taking hits", "Mischievous",           "Somewhat vain",     "Alert to sounds"},
         {"Nods off a lot",          "A little quick tempered", "Highly persistent",      "Thoroughly cunning",    "Strongly defiant",  "Impetuous and silly"},
         {"Scatters things often",   "Likes to fight",          "Good endurance",         "Often lost in thought", "Hates to lose",     "Somewhat of a clown"},
         {"Likes to relax",          "Quick tempered",          "Good perseverance",      "Very finicky",          "Somewhat stubborn", "Quick to flee"}};

    protected PokemonNamesies pokemon;
    protected String nickname;
    protected int[] stats;
    protected int[] IVs;
    protected List<Move> moves;
    protected int hp;
    protected int level;
    protected boolean isPlayer;
    protected Status status;
    protected int totalEXP;
    protected int[] EVs;
    protected HoldItem heldItem;
    protected Ability ability;
    protected Gender gender;
    protected Nature nature;
    protected String characteristic;
    protected boolean shiny;
    protected boolean isEgg;
    protected int eggSteps;

    // General constructor for an active Pokemon (isPlayer is true if it is the player's pokemon and false if it is wild, enemy trainer, etc.)
    protected PartyPokemon(PokemonNamesies pokemonNamesies, int level, boolean isWild, boolean isPlayer) {
        this.pokemon = pokemonNamesies;
        PokemonInfo pokemon = this.getPokemonInfo();

        this.nickname = this.pokemon.getName();
        this.level = level;

        this.nature = new Nature();
        this.EVs = new int[Stat.NUM_STATS];
        this.stats = new int[Stat.NUM_STATS];
        this.setIVs();

        this.isPlayer = isPlayer;
        this.shiny = (isPlayer || isWild) && RandomUtils.chanceTest(1, 8192);

        this.setMoves();
        this.setGender(Gender.getGender(pokemon.getMaleRatio()));
        this.setAbility(Ability.assign(pokemon));

        this.heldItem = (HoldItem)ItemNamesies.NO_ITEM.getItem();

        this.isEgg = false;
        this.eggSteps = 0;

        this.totalEXP = pokemon.getGrowthRate().getEXP(this.level);
        this.totalEXP += RandomUtils.getRandomInt(expToNextLevel());

        this.fullyHeal();
        this.resetAttributes();
    }

    public abstract boolean canFight();
    public abstract void fullyHeal();
    public abstract void resetAttributes();

    public PokemonInfo getPokemonInfo() {
        return pokemon.getInfo();
    }

    // Random value between 0 and 31
    private void setIVs() {
        int[] IVs = new int[Stat.NUM_STATS];
        for (int i = 0; i < IVs.length; i++) {
            IVs[i] = Stat.getRandomIv();
        }

        this.setIVs(IVs);
    }

    // Values between 0 and 31
    protected void setIVs(int[] IVs) {
        this.IVs = IVs;

        int maxIndex = 0;
        for (int i = 0; i < this.IVs.length; i++) {
            if (this.IVs[i] > this.IVs[maxIndex]) {
                maxIndex = i;
            }
        }

        this.characteristic = characteristics[this.IVs[maxIndex]%5][maxIndex];
        this.setStats();
    }

    protected void setStats() {
        int prevHP = stats[Stat.HP.index()];
        PokemonInfo pokemon = this.getPokemonInfo();

        stats = new int[Stat.NUM_STATS];
        for (int i = 0; i < stats.length; i++) {
            stats[i] = Stat.getStat(i, level, pokemon.getStat(i), IVs[i], EVs[i], nature.getNatureVal(i));
        }

        setHP(hp + stats[Stat.HP.index()] - prevHP);
    }

    private void setMoves() {
        moves = new ArrayList<>();
        List<LevelUpMove> levelUpMoves = this.getPokemonInfo().getLevelUpMoves();
        for (LevelUpMove levelUpMove : levelUpMoves) {
            AttackNamesies attackNamesies = levelUpMove.getMove();
            if (levelUpMove.getLevel() > level) {
                break;
            }

            if (this.hasActualMove(attackNamesies)) {
                continue;
            }

            moves.add(new Move(attackNamesies));

            // This can be an 'if' statement, but just to be safe...
            while (moves.size() > Move.MAX_MOVES) {
                moves.remove(0);
            }
        }
    }

    // Returns whether or not this Pokemon knows this move already
    public boolean hasActualMove(AttackNamesies name) {
        return hasMove(getActualMoves(), name);
    }

    protected boolean hasMove(List<Move> moveList, AttackNamesies name) {
        for (Move m : moveList) {
            if (m.getAttack().namesies() == name) {
                return true;
            }
        }

        return false;
    }

    public void setMoves(List<Move> list) {
        moves = list;
    }

    public void setShiny() {
        shiny = true;
    }

    // TODO: Override for Eggy
    public String getGenderString() {
        if (this.isEgg()) {
            return StringUtils.empty();
        }

        return this.getGender().getCharacter();
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getCharacteristic() {
        return characteristic;
    }

    public int[] getStats() {
        return stats;
    }

    public int[] getIVs() {
        return IVs;
    }

    public int[] getEVs() {
        return EVs;
    }

    public int getIV(int index) {
        return IVs[index];
    }

    public int getEV(int index) {
        return EVs[index];
    }

    public Nature getNature() {
        return nature;
    }

    public void setNature(Nature nature) {
        this.nature = nature;
        this.setStats();
    }

    public Ability getActualAbility() {
        return ability;
    }

    public void setAbility(AbilityNamesies ability) {
        this.ability = ability.getNewAbility();
    }

    // TODO: Abstract
    public boolean canBreed() {
        return !isEgg && this.getPokemonInfo().canBreed();
    }

    public int getMaxHP() {
        return stats[Stat.HP.index()];
    }

    public int getHP() {
        return hp;
    }

    public void setHP(int amount) {
        hp = Math.min(getMaxHP(), Math.max(0, amount));
    }

    public boolean fullHealth() {
        return hp == getMaxHP();
    }

    public double getHPRatio() {
        return (double)hp/getMaxHP();
    }

    public Color getHPColor() {
        return DrawUtils.getHPColor(getHPRatio());
    }

    public String getActualName() {
        return nickname;
    }

    public void setNickname(String nickname) {
        if (!StringUtils.isNullOrEmpty(nickname) && !nickname.equals(this.nickname)) {
            this.nickname = nickname;
            if (this.isPlayer()) {
                Game.getPlayer().getMedalCase().increase(MedalTheme.NICKNAMES_GIVEN);
            }
        }
    }

    public boolean isPlayer() {
        return this.isPlayer;
    }

    // TODO: This should really be immutable
    public List<Move> getActualMoves() {
        return moves;
    }

    public int getTotalEXP() {
        return totalEXP;
    }

    public int expToNextLevel() {
        if (level == MAX_LEVEL) {
            return 0;
        }

        return this.getPokemonInfo().getGrowthRate().getEXP(level + 1) - totalEXP;
    }

    public float expRatio() {
        if (level == MAX_LEVEL) {
            return 0;
        }

        PokemonInfo pokemon = this.getPokemonInfo();

        int totalNextLevel = pokemon.getGrowthRate().getEXP(level + 1);
        int totalCurrentLevel = pokemon.getGrowthRate().getEXP(level);

        int currentToNextLevel = expToNextLevel();
        int totalToNextLevel = totalNextLevel - totalCurrentLevel;

        return 1.0f - (float)currentToNextLevel/totalToNextLevel;
    }

    public boolean isShiny() {
        return shiny;
    }

    public boolean isPokemon(PokemonNamesies name) {
        return pokemon == name;
    }

    // Does not include shiny -- this is for the small party tiles
    public String getTinyImageName() {
        return this.isEgg ? TINY_EGG_IMAGE_NAME : this.getPokemonInfo().getTinyImageName();
    }

    // Does not include shiny -- this is for the small party tiles
    public String getBaseImageName() {
        return this.isEgg ? BASE_EGG_IMAGE_NAME : this.getPokemonInfo().getBaseImageName();
    }

    public String getImageName() {
        return this.getImageName(true);
    }

    // Larger image index
    public String getImageName(boolean front) {
        return this.isEgg() ? SPRITE_EGG_IMAGE_NAME : this.getPokemonInfo().getImageName(this.isShiny(), front);
    }

    public boolean isEgg() {
        return isEgg;
    }

    public boolean hatch() {
        if (!isEgg()) {
            Global.error("Only eggs can hatch!");
        }

        eggSteps--;

        if (eggSteps > 0) {
            return false;
        }

        this.isEgg = false;
        this.nickname = pokemon.getName();

        return true;
    }

    public String getEggMessage() {
        if (!isEgg()) {
            Global.error("Only Eggs can have egg messages.");
        }

        if (eggSteps > 10*255) {
            return "Wonder what's inside? It needs more time though.";
        } else if (eggSteps > 5*255) {
            return "It moves around inside sometimes. It must be close to hatching.";
        } else {
            return "It's making sounds inside! It's going to hatch soon!";
        }
    }

    // Returns the moves this Pokemon could have learned up to its current level
    public List<AttackNamesies> getLearnableMoves() {
        List<AttackNamesies> moves = new ArrayList<>();
        List<LevelUpMove> levelUpMoves = this.getPokemonInfo().getLevelUpMoves();
        for (LevelUpMove levelUpMove : levelUpMoves) {
            if (levelUpMove.getLevel() > level) {
                break;
            }

            if (!this.hasActualMove(levelUpMove.getMove())) {
                moves.add(levelUpMove.getMove());
            }
        }

        return moves;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    private int totalEVs() {
        int sum = 0;
        for (int EV : EVs) {
            sum += EV;
        }

        return sum;
    }

    // Adds Effort Values to a Pokemon, returns true if they were successfully added
    public boolean addEVs(int[] vals) {
        if (totalEVs() == Stat.MAX_EVS) {
            return false;
        }

        boolean added = false;
        for (int i = 0; i < EVs.length; i++) {
            if (vals[i] > 0 && EVs[i] < Stat.MAX_STAT_EVS) {
                added = true;
                EVs[i] = Math.min(Stat.MAX_STAT_EVS, EVs[i] + vals[i]); // Don't exceed stat EV amount

                // Don't exceed total EV amount
                if (totalEVs() > Stat.MAX_EVS) {
                    EVs[i] -= (totalEVs() - Stat.MAX_EVS);
                    break;
                }
            } else if (vals[i] < 0 && EVs[i] > 0) {
                added = true;
                EVs[i] = Math.max(0, EVs[i] + vals[i]); // Don't drop below zero
            }
        }

        if (added) {
            setStats();
            if (totalEVs() == Stat.MAX_EVS) {
                MedalCase medalCase = Game.getPlayer().getMedalCase();
                medalCase.earnMedal(Medal.TRAINED_TO_MAX_POTENTIAL);
            }
        }

        return added;
    }

    public Type[] getActualType() {
        return this.getPokemonInfo().getType();
    }

    public void setCaught() {
        isPlayer = true;
    }

    public void giveItem(ItemNamesies itemName) {
        Item item = itemName.getItem();
        if (item.isHoldable()) {
            this.giveItem((HoldItem)item);
        }
    }

    public void giveItem(HoldItem item) {
        heldItem = item;
    }

    public void removeItem() {
        heldItem = (HoldItem)ItemNamesies.NO_ITEM.getItem();
    }

    public Item getActualHeldItem() {
        return (Item)heldItem;
    }

    /*
         * Format: Name Level Parameters
         * Possible parameters:
         *         Moves: Move1, Move2, Move3, Move4*
         *         Shiny
         *         Egg
         *         Item: item name*
         */
    // Constructor for triggers
    public static ActivePokemon createActivePokemon(PokemonMatcher pokemonMatcher, boolean user) {

        // Random Starter Egg
        if (pokemonMatcher.isStarterEgg()) {
            if (!user) {
                Global.error("Trainers cannot have eggs.");
            }

            return new ActivePokemon(PokemonInfo.getRandomStarterPokemon());
        }

        final PokemonNamesies namesies = pokemonMatcher.getNamesies();

        ActivePokemon pokemon;
        if (pokemonMatcher.isEgg()) {
            if (!user) {
                Global.error("Trainers cannot have eggs.");
            }

            pokemon = new ActivePokemon(namesies);
        } else {
            pokemon = new ActivePokemon(namesies, pokemonMatcher.getLevel(), false, user);
            String nickname = pokemonMatcher.getNickname();
            if (!StringUtils.isNullOrEmpty(nickname)) {
                pokemon.setNickname(nickname);
            }
        }

        if (pokemonMatcher.isShiny()) {
            pokemon.setShiny();
        }

        if (pokemonMatcher.hasMoves()) {
            pokemon.setMoves(pokemonMatcher.getMoves());
        }

        if (pokemonMatcher.hasHoldItem()) {
            pokemon.giveItem(pokemonMatcher.getHoldItem());
        }

        return pokemon;
    }
}
