package trainer.player.medal;

import main.Game;

import java.util.EnumMap;
import java.util.Map;

public enum MedalTheme {
    STEPS_WALKED(
            Medal.LIGHT_WALKER,
            Medal.MIDDLE_WALKER,
            Medal.HEAVY_WALKER,
            Medal.HONORED_FOOTPRINTS
    ),
    TIMES_SAVED(
            Medal.STEP_BY_STEP_SAVER,
            Medal.BUSY_SAVER,
            Medal.EXPERIENCED_SAVER,
            Medal.WONDER_WRITER
    ),
    POKE_CENTER_HEALS(
            Medal.POKEMON_CENTER_FAN,
            Medal.POKEMON_CENTER_SUPER_FAN
    ),
    BICYCLE_COUNT(
            Medal.STARTER_CYCLING,
            Medal.EASY_CYCLING,
            Medal.HARD_CYCLING,
            Medal.PEDALING_LEGEND
    ),
    FISH_REELED_IN(
            Medal.OLD_ROD_FISHERMAN,
            Medal.GOOD_ROD_FISHERMAN,
            Medal.SUPER_ROD_FISHERMAN,
            Medal.MIGHTY_FISHER
    ),
    BABIES_HATCHED(
            Medal.BABY_CAKES
    ),
    EGGS_HATCHED(
            Medal.EGG_BEGINNER,
            Medal.EGG_BREEDER,
            Medal.EGG_ELITE,
            Medal.HATCHING_AFICIONADO
    ),
    DAY_CARE_DEPOSITED(
            Medal.DAY_CARE_FAITHFUL,
            Medal.DAY_CARE_SUPER_FAITHFUL,
            Medal.DAY_CARE_EXTRAORDINARY_FAITHFUL
    ),
    HIDDEN_ITEMS_FOUND(
            Medal.DOWSING_BEGINNER,
            Medal.DOWSING_SPECIALIST,
            Medal.DOWSING_COLLECTOR,
            Medal.DOWSING_WIZARD
    ),
    ITEMS_BOUGHT(
            Medal.REGULAR_CUSTOMER
    ),
    CASH_MONEY_SPENT(
            Medal.MODERATE_CUSTOMER,
            Medal.GREAT_CUSTOMER,
            Medal.INDULGENT_CUSTOMER,
            Medal.SUPER_RICH
    ),
    POKEMON_EVOLVED(
            Medal.EVOLUTION_HOPEFUL,
            Medal.EVOLUTION_TECH,
            Medal.EVOLUTION_EXPERT,
            Medal.EVOLUTION_AUTHORITY
    ),
    NICKNAMES_GIVEN(
            Medal.NAMING_CHAMP
    ),
    SUPER_EFFECTIVE_MOVES_USED(
            Medal.SUPEREFFECTIVE_SAVANT
    ),
    SHINIES_FOUND(
            Medal.LUCKY_COLOR,
            Medal.LUCKIER_COLOR,
            Medal.LUCKIEST_COLOR,
            Medal.SUPER_DUPER_LUCKIEST_COLOR
    ),
    LEVEL_100_POKEMON(
            Medal.THREE_WHOLE_DIGITS,
            Medal.I_LIKE_TO_GRIND,
            Medal.GRINDING_EXTRAORDINAIRE
    ),
    BATTLES_BATTLED(
            Medal.BATTLE_LEARNER,
            Medal.BATTLE_TEACHER,
            Medal.BATTLE_VETERAN,
            Medal.BATTLE_VIRTUOSO
    ),
    DEMON_POKEMON_DEFEATED(
            Medal.RILEY,
            Medal.KENDRA,
            Medal.FAITH,
            Medal.BUFFY
    ),
    POKEMON_SEEN(
            Medal.GWENDOLYN_POST,
            Medal.MERRICK,
            Medal.WESLEY_WYNDAM_PRYCE,
            Medal.GILES
    ),
    POKEMON_CAUGHT(
            Medal.PROFESSOR_MAPLE,
            Medal.PROFESSOR_BIRCH,
            Medal.PROFESSOR_ELM,
            Medal.PROFESSOR_OAK
    ),
    NORMAL_CATCHER(
            Medal.NORMAL_TYPE_BEGINNER,
            Medal.NORMAL_TYPE_SPECIALIST,
            Medal.NORMAL_TYPE_COLLECTOR,
            Medal.NORMAL_TYPE_MASTERFUL_WIZARD
    ),
    FIRE_CATCHER(
            Medal.FIRE_TYPE_BEGINNER,
            Medal.FIRE_TYPE_SPECIALIST,
            Medal.FIRE_TYPE_COLLECTOR,
            Medal.FIRE_TYPE_MASTERFUL_WIZARD
    ),
    WATER_CATCHER(
            Medal.WATER_TYPE_BEGINNER,
            Medal.WATER_TYPE_SPECIALIST,
            Medal.WATER_TYPE_COLLECTOR,
            Medal.WATER_TYPE_MASTERFUL_WIZARD
    ),
    ELECTRIC_CATCHER(
            Medal.ELECTRIC_TYPE_BEGINNER,
            Medal.ELECTRIC_TYPE_SPECIALIST,
            Medal.ELECTRIC_TYPE_COLLECTOR,
            Medal.ELECTRIC_TYPE_MASTERFUL_WIZARD
    ),
    GRASS_CATCHER(
            Medal.GRASS_TYPE_BEGINNER,
            Medal.GRASS_TYPE_SPECIALIST,
            Medal.GRASS_TYPE_COLLECTOR,
            Medal.GRASS_TYPE_MASTERFUL_WIZARD
    ),
    ICE_CATCHER(
            Medal.ICE_TYPE_BEGINNER,
            Medal.ICE_TYPE_SPECIALIST,
            Medal.ICE_TYPE_COLLECTOR,
            Medal.ICE_TYPE_MASTERFUL_WIZARD
    ),
    FIGHTING_CATCHER(
            Medal.FIGHTING_TYPE_BEGINNER,
            Medal.FIGHTING_TYPE_SPECIALIST,
            Medal.FIGHTING_TYPE_COLLECTOR,
            Medal.FIGHTING_TYPE_MASTERFUL_WIZARD
    ),
    POISON_CATCHER(
            Medal.POISON_TYPE_BEGINNER,
            Medal.POISON_TYPE_SPECIALIST,
            Medal.POISON_TYPE_COLLECTOR,
            Medal.POISON_TYPE_MASTERFUL_WIZARD
    ),
    GROUND_CATCHER(
            Medal.GROUND_TYPE_BEGINNER,
            Medal.GROUND_TYPE_SPECIALIST,
            Medal.GROUND_TYPE_COLLECTOR,
            Medal.GROUND_TYPE_MASTERFUL_WIZARD
    ),
    FLYING_CATCHER(
            Medal.FLYING_TYPE_BEGINNER,
            Medal.FLYING_TYPE_SPECIALIST,
            Medal.FLYING_TYPE_COLLECTOR,
            Medal.FLYING_TYPE_MASTERFUL_WIZARD
    ),
    PSYCHIC_CATCHER(
            Medal.PSYCHIC_TYPE_BEGINNER,
            Medal.PSYCHIC_TYPE_SPECIALIST,
            Medal.PSYCHIC_TYPE_COLLECTOR,
            Medal.PSYCHIC_TYPE_MASTERFUL_WIZARD
    ),
    BUG_CATCHER(
            Medal.BUG_TYPE_BEGINNER,
            Medal.BUG_TYPE_SPECIALIST,
            Medal.BUG_TYPE_COLLECTOR,
            Medal.BUG_TYPE_MASTERFUL_WIZARD
    ),
    ROCK_CATCHER(
            Medal.ROCK_TYPE_BEGINNER,
            Medal.ROCK_TYPE_SPECIALIST,
            Medal.ROCK_TYPE_COLLECTOR,
            Medal.ROCK_TYPE_MASTERFUL_WIZARD
    ),
    GHOST_CATCHER(
            Medal.GHOST_TYPE_BEGINNER,
            Medal.GHOST_TYPE_SPECIALIST,
            Medal.GHOST_TYPE_COLLECTOR,
            Medal.GHOST_TYPE_MASTERFUL_WIZARD
    ),
    DRAGON_CATCHER(
            Medal.DRAGON_TYPE_BEGINNER,
            Medal.DRAGON_TYPE_SPECIALIST,
            Medal.DRAGON_TYPE_COLLECTOR,
            Medal.DRAGON_TYPE_MASTERFUL_WIZARD
    ),
    DARK_CATCHER(
            Medal.DARK_TYPE_BEGINNER,
            Medal.DARK_TYPE_SPECIALIST,
            Medal.DARK_TYPE_COLLECTOR,
            Medal.DARK_TYPE_MASTERFUL_WIZARD
    ),
    STEEL_CATCHER(
            Medal.STEEL_TYPE_BEGINNER,
            Medal.STEEL_TYPE_SPECIALIST,
            Medal.STEEL_TYPE_COLLECTOR,
            Medal.STEEL_TYPE_MASTERFUL_WIZARD
    ),
    FAIRY_CATCHER(
            Medal.FAIRY_TYPE_BEGINNER,
            Medal.FAIRY_TYPE_SPECIALIST,
            Medal.FAIRY_TYPE_COLLECTOR,
            Medal.FAIRY_TYPE_MASTERFUL_WIZARD
    ),
    NO_TYPE_CATCHER(),
    MEDALS_COLLECTED(
            Medal.ROOKIE_MEDALIST,
            Medal.ELITE_MEDALIST,
            Medal.MASTER_MEDALIST,
            Medal.LEGEND_MEDALIST,
            Medal.TOP_MEDALIST
    );
    
    private static final Map<Medal, MedalTheme> MEDAL_THEME_MAP;
    
    static {
        MEDAL_THEME_MAP = new EnumMap<>(Medal.class);
        for (MedalTheme medalTheme : MedalTheme.values()) {
            for (Medal medal : medalTheme.medals) {
                // TODO: Test for this -- make sure this is for all thresholds exactly once
                MEDAL_THEME_MAP.put(medal, medalTheme);
            }
        }
    }
    
    private Medal[] medals;
    
    MedalTheme(Medal... medals) {
        this.medals = medals;
    }
    
    public void checkThreshold(int count) {
        for (Medal medal : medals) {
            if (count >= medal.getThreshold()) {
                Game.getPlayer().getMedalCase().earnMedal(medal);
            }
        }
    }
    
    public static MedalTheme getMedalTheme(Medal medal) {
        return MEDAL_THEME_MAP.get(medal);
    }
}
