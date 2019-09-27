package pokemon.active;

import battle.attack.AttackNamesies;
import battle.attack.Move;
import battle.effect.status.StatusCondition;
import battle.effect.status.StatusNamesies;
import draw.DrawUtils;
import item.Item;
import item.ItemNamesies;
import item.hold.HoldItem;
import main.Game;
import pokemon.Stat;
import pokemon.ability.Ability;
import pokemon.ability.AbilityNamesies;
import pokemon.breeding.Eggy;
import pokemon.evolution.BaseEvolution;
import pokemon.species.LevelUpMove;
import pokemon.species.PokemonInfo;
import pokemon.species.PokemonNamesies;
import trainer.TrainerType;
import trainer.player.medal.MedalTheme;
import type.PokeType;
import type.Type;
import util.GeneralUtils;
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
    private StatValues stats;
    private MoveList moves;
    private int hp;
    private int level;
    private boolean isPlayer;
    private StatusCondition status;
    private int totalEXP;
    private HoldItem heldItem;
    private Ability ability;
    private Gender gender;
    private boolean shiny;

    // General constructor for an active Pokemon (isPlayer is true if it is the player's pokemon and false if it is wild, enemy trainer, etc.)
    protected PartyPokemon(PokemonNamesies pokemonNamesies, int level, TrainerType trainerType) {
        this.pokemon = pokemonNamesies;

        this.nickname = this.pokemon.getName();
        this.level = level;
        this.isPlayer = trainerType.isPlayer();
        this.shiny = (trainerType.isWild()) && RandomUtils.chanceTest(1, 8192);

        this.stats = new StatValues(this);
        this.moves = new MoveList(this);

        PokemonInfo pokemon = this.getPokemonInfo();
        this.setGender(Gender.getGender(pokemon.getMaleRatio()));
        this.setAbility(Ability.assign(pokemon));

        this.heldItem = (HoldItem)ItemNamesies.NO_ITEM.getItem();

        this.totalEXP = pokemon.getGrowthRate().getEXP(this.level);
        this.totalEXP += RandomUtils.getRandomInt(expToNextLevel());

        this.setStats();
        this.fullyHeal();
        this.resetAttributes();
    }

    // Constructor for matchers
    protected PartyPokemon(PokemonNamesies pokemonNamesies, int level, TrainerType trainerType,
                           String nickname, Boolean shiny, List<Move> moves, Gender gender, Nature nature) {
        this(pokemonNamesies, level, trainerType);

        if (!StringUtils.isNullOrEmpty(nickname)) {
            this.setNickname(nickname);
        }

        if (GeneralUtils.getBooleanValue(shiny)) {
            this.setShiny();
        }

        if (moves != null) {
            this.setMoves(moves);
        }

        if (gender != null) {
            this.setGender(gender);
        }

        if (nature != null) {
            this.setNature(nature);
        }
    }

    protected PartyPokemon(Eggy eggy) {
        PokemonInfo pokemonInfo = eggy.getPokemonInfo();
        this.pokemon = pokemonInfo.namesies();

        this.nickname = pokemon.getName();
        this.level = 1;
        this.isPlayer = true;

        this.shiny = eggy.isShiny();
        this.stats = new StatValues(this);
        this.moves = new MoveList(eggy.getActualMoves());

        this.setNature(eggy.getNature());
        this.setIVs(eggy.getIVs());
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
        getActualMoves().restoreAllPP();
        healHealthFraction(1);
    }

    public PokemonNamesies namesies() {
        return this.pokemon;
    }

    public PokemonInfo getPokemonInfo() {
        return pokemon.getInfo();
    }

    // This method should always be used and the field should never be used directly
    public StatValues stats() {
        this.stats.setStatsHolder(this);
        return stats;
    }

    private int[] setStats() {
        return this.stats().setStats();
    }

    // Returns whether or not this Pokemon knows this move already
    public boolean hasActualMove(AttackNamesies name) {
        return this.moves.hasMove(name);
    }

    protected void setMoves(List<Move> list) {
        moves.setMoves(list);
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
        return this.getIVs().getCharacteristic();
    }

    public int getStat(int index) {
        return this.stats().get(index);
    }

    public int getStat(Stat stat) {
        return this.getStat(stat.index());
    }

    public IndividualValues getIVs() {
        return this.stats().getIVs();
    }

    protected void setIVs(IndividualValues IVs) {
        this.stats().setIVs(IVs);
    }

    public EffortValues getEVs() {
        return this.stats().getEVs();
    }

    public Nature getNature() {
        return this.stats().getNature();
    }

    protected void setNature(Nature nature) {
        this.stats().setNature(nature);
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
        return this.getStat(Stat.HP);
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

    public int getHealHealthFractionAmount(double fraction) {
        int prevHp = this.getHP();
        int healAmount = this.healHealthFraction(fraction);
        this.setHP(prevHp);
        return healAmount;
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
            if (this.isPlayer() && !this.isEgg()) {
                Game.getPlayer().getMedalCase().increase(MedalTheme.NICKNAMES_GIVEN);
            }
        }
    }

    public boolean isPlayer() {
        return this.isPlayer;
    }

    public MoveList getActualMoves() {
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

    public boolean isActualPokemon(PokemonNamesies... names) {
        return GeneralUtils.contains(pokemon, names);
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

    public PokeType getActualType() {
        return this.getPokemonInfo().getType();
    }

    public void setCaught() {
        this.isPlayer = true;
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
        IndividualValues IVs = this.getIVs();
        return Type.getHiddenType(((
                IVs.get(Stat.HP)%2 +
                        2*(IVs.get(Stat.ATTACK)%2) +
                        4*(IVs.get(Stat.DEFENSE)%2) +
                        8*(IVs.get(Stat.SPEED)%2) +
                        16*(IVs.get(Stat.SP_ATTACK)%2) +
                        32*(IVs.get(Stat.SP_DEFENSE)%2)
        )*15)/63);
    }
}
