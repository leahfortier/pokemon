package trainer.player.medal;

import main.Game;

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
    // TODO
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
    // TODO
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
    // TODO
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
    MEDALS_COLLECTED(
            Medal.ROOKIE_MEDALIST,
            Medal.ELITE_MEDALIST,
            Medal.MASTER_MEDALIST,
            Medal.LEGEND_MEDALIST,
            Medal.TOP_MEDALIST
    );

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
}
