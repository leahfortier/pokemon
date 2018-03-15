package pokemon.active;

import battle.ActivePokemon;
import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.effect.status.StatusCondition;
import battle.effect.status.StatusNamesies;
import draw.DrawUtils;
import item.Item;
import item.ItemNamesies;
import item.hold.HoldItem;
import main.Game;
import main.Global;
import pattern.PokemonMatcher;
import pokemon.Stat;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import pokemon.breeding.Eggy;
import pokemon.evolution.BaseEvolution;
import pokemon.species.LevelUpMove;
import pokemon.species.PokemonInfo;
import pokemon.species.PokemonNamesies;
import trainer.player.medal.Medal;
import trainer.player.medal.MedalCase;
import trainer.player.medal.MedalTheme;
import type.PokeType;
import type.Type;
import util.RandomUtils;
import util.serialization.Serializable;
import util.string.StringUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public abstract class PartyPokemon implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MAX_LEVEL = 100;
    public static final int MAX_NAME_LENGTH = 10;

    private PokemonNamesies pokemon;
    private String nickname;
    private int[] stats;
    private IndividualValues IVs;
    private List<Move> moves;
    private int hp;
    private int level;
    private boolean isPlayer;
    private StatusCondition status;
    private int totalEXP;
    private EffortValues EVs;
    private HoldItem heldItem;
    private Ability ability;
    private Gender gender;
    private Nature nature;
    private boolean shiny;

    // General constructor for an active Pokemon (isPlayer is true if it is the player's pokemon and false if it is wild, enemy trainer, etc.)
    protected PartyPokemon(PokemonNamesies pokemonNamesies, int level, boolean isWild, boolean isPlayer) {
        this.pokemon = pokemonNamesies;
        PokemonInfo pokemon = this.getPokemonInfo();

        this.nickname = this.pokemon.getName();
        this.level = level;

        this.nature = new Nature();
        this.stats = new int[Stat.NUM_STATS];
        this.IVs = new IndividualValues();
        this.EVs = new EffortValues();

        this.isPlayer = isPlayer;
        this.shiny = (isPlayer || isWild) && RandomUtils.chanceTest(1, 8192);

        this.setMoves();
        this.setGender(Gender.getGender(pokemon.getMaleRatio()));
        this.setAbility(Ability.assign(pokemon));

        this.heldItem = (HoldItem)ItemNamesies.NO_ITEM.getItem();

        this.totalEXP = pokemon.getGrowthRate().getEXP(this.level);
        this.totalEXP += RandomUtils.getRandomInt(expToNextLevel());

        this.setStats();
        this.fullyHeal();
        this.resetAttributes();
    }

    protected PartyPokemon(Eggy eggy) {
        PokemonInfo pokemonInfo = eggy.getPokemonInfo();
        this.pokemon = pokemonInfo.namesies();

        this.nickname = pokemon.getName();
        this.level = 1;

        this.nature = eggy.getNature();
        this.stats = new int[Stat.NUM_STATS];
        this.IVs = new IndividualValues(((PartyPokemon)eggy).IVs);
        this.EVs = new EffortValues();

        this.isPlayer = true;
        this.shiny = eggy.isShiny();

        this.setMoves(eggy.getActualMoves());
        this.setGender(eggy.getGender());
        this.setAbility(eggy.getActualAbility().namesies());

        this.heldItem = (HoldItem)ItemNamesies.NO_ITEM.getItem();

        // Don't add randomness for eggs
        this.totalEXP = pokemonInfo.getGrowthRate().getEXP(this.level);

        this.setStats();
        this.fullyHeal();
        this.resetAttributes();
    }

    public abstract boolean canFight();
    public abstract void resetAttributes();
    public abstract void setUsed(boolean used);
    public abstract boolean isUsed();
    public abstract boolean isBattleUsed();

    // Removes status, restores PP for all moves, restores to full health
    public void fullyHeal() {
        removeStatus();
        getActualMoves().forEach(Move::resetPP);
        healHealthFraction(1);
    }

    public PokemonNamesies namesies() {
        return this.pokemon;
    }

    public PokemonInfo getPokemonInfo() {
        return pokemon.getInfo();
    }

    protected void setIVs(int[] IVs) {
        this.IVs.setIVs(IVs);
        this.setStats();
    }

    private int[] setStats() {
        int[] prevStats = stats.clone();

        PokemonInfo pokemon = this.getPokemonInfo();

        stats = new int[Stat.NUM_STATS];
        int[] gain = new int[Stat.NUM_STATS];
        for (int i = 0; i < stats.length; i++) {
            stats[i] = Stat.getStat(i, level, pokemon.getStat(i), this.getIV(i), this.getEV(i), nature.getNatureVal(i));
            gain[i] = stats[i] - prevStats[i];
        }

        setHP(hp + stats[Stat.HP.index()] - prevStats[Stat.HP.index()]);

        return gain;
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

    protected void setMoves(List<Move> list) {
        if (list.isEmpty() || list.size() > Move.MAX_MOVES) {
            Global.error("Invalid move list: " + list);
        }

        moves = list;
    }

    // Handles actual level up, but does not handle messages or evolution or anything else like that
    // Everything else should be handled in the subclass
    protected int[] levelUp() {
        level = Math.min(level + 1, MAX_LEVEL);
        if (level == MAX_LEVEL) {
            Game.getPlayer().getMedalCase().increase(MedalTheme.LEVEL_100_POKEMON);
        }

        // Update stats and return gain
        return this.setStats();
    }

    // Handles actual evolution, but doesn't set any messages or anything or try to learn new moves or shit like that
    // Everything else should be handles in the subclass
    // Returns stats gains
    protected int[] evolve(BaseEvolution evolution) {
        Game.getPlayer().getMedalCase().increase(MedalTheme.POKEMON_EVOLVED);

        boolean sameName = nickname.equals(pokemon.getName());
        PokemonInfo evolutionInfo = evolution.getEvolution();

        this.setAbility(Ability.evolutionAssign(this, evolutionInfo));
        pokemon = evolutionInfo.namesies();

        // Set name if it was not given a nickname
        if (sameName) {
            nickname = pokemon.getName();
        }

        // Update stats and return gain
        return this.setStats();
    }

    // Adds the move at the specified index if full and to the end othwerwise
    // Does not handle evolution or anything else like that -- should be handled in subclasses
    protected void addMove(Move m, int index) {
        if (moves.size() < Move.MAX_MOVES) {
            moves.add(m);
        } else {
            moves.set(index, m);
        }
    }

    protected int numMoves() {
        return this.moves.size();
    }

    private void setShiny() {
        shiny = true;
    }

    public String getGenderString() {
        return this.getGender().getCharacter();
    }

    public Gender getGender() {
        return gender;
    }

    protected void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getCharacteristic() {
        return this.IVs.getCharacteristic();
    }

    public int[] getClonedStats() {
        return this.stats.clone();
    }

    public int getStat(int index) {
        return stats[index];
    }

    public int getStat(Stat stat) {
        return this.getStat(stat.index());
    }

    public int getIV(int index) {
        return this.IVs.getIV(index);
    }

    public int getIV(Stat stat) {
        return this.getIV(stat.index());
    }

    public int getEV(int index) {
        return this.EVs.getEV(index);
    }

    public int getEV(Stat stat) {
        return this.getEV(stat.index());
    }

    public Nature getNature() {
        return nature;
    }

    protected void setNature(Nature nature) {
        this.nature = nature;
        this.setStats();
    }

    // Returns whether or not the Pokemon is afflicted with a status condition
    public boolean hasStatus() {
        return !this.hasStatus(StatusNamesies.NO_STATUS);
    }

    public boolean hasStatus(StatusNamesies type) {
        return status.isType(type);
    }

    public StatusCondition getStatus() {
        return status;
    }

    public void setStatus(StatusCondition s) {
        status = s;
    }

    // Sets the Pokemon's status condition to be None
    public void removeStatus() {
        this.setStatus(StatusNamesies.NO_STATUS.getStatus());
    }

    public Ability getActualAbility() {
        return ability;
    }

    public void setAbility(AbilityNamesies ability) {
        this.ability = ability.getNewAbility();
    }

    public boolean canBreed() {
        return this.getPokemonInfo().canBreed();
    }

    public String getHpString() {
        return this.getHP() + "/" + this.getMaxHP();
    }

    public int getMaxHP() {
        return stats[Stat.HP.index()];
    }

    public int getHP() {
        return hp;
    }

    public void setHP(int amount) {
        hp = Math.min(this.getMaxHP(), Math.max(0, amount));
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

    // Restores hp by amount, returns the actual amount of hp that was restored
    public int heal(int amount) {

        // Dead Pokemon can't heal
        if (hasStatus(StatusNamesies.FAINTED)) {
            return 0;
        }

        int prev = hp;
        setHP(hp + amount);
        return hp - prev;
    }

    // Restores the amount of health that corresponds to the fraction of the pokemon's total health and returns this amount
    public int healHealthFraction(double fraction) {
        return heal((int)Math.max(this.getMaxHP()*fraction, 1));
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

    // Only handles adding EXP, but MUST be called in unison with a level upper
    protected void gainEXP(int gain) {
        this.totalEXP += gain;
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
        return this.getPokemonInfo().getTinyImageName();
    }

    // Does not include shiny -- this is for the pokedex tiles (in new pokemon view)
    public String getBaseImageName() {
        return this.getPokemonInfo().getBaseImageName();
    }

    public String getImageName() {
        return this.getImageName(true);
    }

    // Larger image index
    public String getImageName(boolean front) {
        return this.getPokemonInfo().getImageName(this.isShiny(), front);
    }

    public boolean isEgg() {
        return false;
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

    // Adds Effort Values to a Pokemon, returns true if they were successfully added
    public boolean addEVs(int[] vals) {
        if (!this.canFight()) {
            return false;
        }

        boolean added = this.EVs.addEVs(vals);

        if (added) {
            this.setStats();
            if (this.EVs.totalEVs() == EffortValues.MAX_EVS) {
                MedalCase medalCase = Game.getPlayer().getMedalCase();
                medalCase.earnMedal(Medal.TRAINED_TO_MAX_POTENTIAL);
            }
        }

        return added;
    }

    public PokeType getActualType() {
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

    public HoldItem getActualHeldItem() {
        return heldItem;
    }

    public Type computeHiddenPowerType() {
        return Type.getHiddenType(((
                this.getIV(Stat.HP)%2 +
                        2*(this.getIV(Stat.ATTACK)%2) +
                        4*(this.getIV(Stat.DEFENSE)%2) +
                        8*(this.getIV(Stat.SPEED)%2) +
                        16*(this.getIV(Stat.SP_ATTACK)%2) +
                        32*(this.getIV(Stat.SP_DEFENSE)%2)
        )*15)/63);
    }

    // Constructor for matchers
    public static PartyPokemon createActivePokemon(PokemonMatcher pokemonMatcher, boolean isPlayer) {
        final PokemonNamesies namesies = pokemonMatcher.getNamesies();

        PartyPokemon pokemon;
        if (pokemonMatcher.isEgg()) {
            if (!isPlayer) {
                Global.error("Trainers cannot have eggs.");
            }

            pokemon = new Eggy(namesies);
        } else {
            pokemon = new ActivePokemon(namesies, pokemonMatcher.getLevel(), false, isPlayer);
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

        if (pokemonMatcher.hasGender()) {
            pokemon.setGender(pokemonMatcher.getGender());
        }

        if (pokemonMatcher.hasNature()) {
            pokemon.setNature(pokemonMatcher.getNature());
        }

        return pokemon;
    }
}
