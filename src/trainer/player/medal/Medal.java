package trainer.player.medal;

import main.Global;

public enum Medal {
    // Steps Taken
    LIGHT_WALKER(1, 5000, "Light Walker", "A Medal to praise light walkers who took 5,000 steps."),
    MIDDLE_WALKER(2, 10000, "Middle Walker", "A Medal to praise middle walkers who took 10,000 steps."),
    HEAVY_WALKER(3, 25000, "Heavy Walker", "A Medal to praise heavy walkers who took 25,000 steps."),
    HONORED_FOOTPRINTS(4, 100000, "Honored Footprints", "A Medal to praise ultimate walkers who took 100,000 steps."),

    // Times Saved
    STEP_BY_STEP_SAVER(1, 10, "Step-by-Step Saver", "A Medal to recognize a newly started journey that has been recorded 10 times."),
    BUSY_SAVER(2, 20, "Busy Saver", "A Medal to recognize an energetic journey that has been recorded 20 times."),
    EXPERIENCED_SAVER(3, 50, "Experienced Saver", "A Medal to recognize a smooth journey that has been recorded 50 times."),
    WONDER_WRITER(4, 100, "Wonder Writer", "A Medal to recognize an astonishing journey that has been recorded 100 times"),

    // Pokecenter visits
    POKEMON_CENTER_FAN(1, 10, "Pokémon Center Fan", "A Medal given to kind Trainers who let their Pokémon rest at Pokémon Centers 10 times."),
    POKEMON_CENTER_SUPER_FAN(3, 100, "Pokémon Center Super Fan", "A Medal given to kind Trainers who let their Pokémon rest at Pokémon Centers 100 times."),

    // Bicycling riding
    STARTER_CYCLING(1, 1, "Starter Cycling", "A Medal given to beginning cyclists who rode a Bicycle for the first time."),
    EASY_CYCLING(2, 30, "Easy Cycling", "A Medal given to casual cyclists who have ridden a Bicycle 30 times."),
    HARD_CYCLING(3, 100, "Hard Cycling", "A Medal given to outstanding cyclists who have ridden a Bicycle 100 times."),
    PEDALING_LEGEND(4, 500, "Pedaling Legend", "A Medal given to earthshaking cyclists who have ridden a Bicycle 500 times."),

    // Fishing
    OLD_ROD_FISHERMAN(1, 1, "Old Rod Fisherman", "A Medal given to beginning fishers who reeled in a Pokémon for the first time."),
    GOOD_ROD_FISHERMAN(2, 10, "Good Rod Fisherman", "A Medal given to leisure fishers who reeled in 10 Pokémon."),
    SUPER_ROD_FISHERMAN(3, 50, "Super Rod Fisherman", "A Medal given to very experienced fishers who reeled in 50 Pokémon."),
    MIGHTY_FISHER(4, 100, "Mighty Fisher", "A Medal given to legendary fishers who reeled in 100 Pokémon."),

    // Eggs Hatched
    EGG_BEGINNER(1, 1, "Egg Beginner", "A Medal to prove the fresh parental instincts of people who hatched a Pokémon Egg for the first time."),
    EGG_BREEDER(2, 10, "Egg Breeder", "A Medal to prove the decent parental instincts of people who hatched 10 Pokémon Eggs."),
    EGG_ELITE(3, 50, "Egg Elite", "A Medal to prove the outstanding parental instincts of people who hatched 50 Pokémon Eggs."),
    HATCHING_AFICIONADO(4, 100, "Hatching Aficionado", "A Medal to prove the endless parental instincts of people who hatched 100 Pokémon Eggs."),

    // Pokemon Deposited at Day Care
    DAY_CARE_FAITHFUL(1, 10, "Day-Care Faithful", "A Medal for those who love to raise Pokémon and left 10 Pokémon at the Pokémon Day Care."),
    DAY_CARE_SUPER_FAITHFUL(2, 50, "Day-Care Super Faithful", "A Medal for those who love to raise Pokémon and left 50 Pokémon at the Pokémon Day Care."),
    DAY_CARE_EXTRAORDINARY_FAITHFUL(4, 100, "Day-Care Extraordinary Faithful", "A Medal for those who love to raise Pokémon and left 100 Pokémon at the Pokémon Day Care."),

    // Spend Dat Cash Money
    REGULAR_CUSTOMER(3, 100, "Regular Customer", "A Medal for Trainers who kept going to various shops and became regular customers.	Make 100 purchases at shops"),
    SMART_SHOPPER(1, "Smart Shopper", "A Medal for thrifty shoppers who made bulk purchases and got bonus Premier Balls."),
    MODERATE_CUSTOMER(1, 10000, "Moderate Customer", "A Medal for rich people who spent " + Global.MONEY_SYMBOL + "10,000 at various shops."),
    GREAT_CUSTOMER(2, 100000, "Great Customer", "A Medal for rich people who spent " + Global.MONEY_SYMBOL + "100,000 at various shops."),
    INDULGENT_CUSTOMER(3, 1000000, "Indulgent Customer", "A Medal for rich people who spent " + Global.MONEY_SYMBOL + "1,000,000 at various shops."),
    SUPER_RICH(4, 10000000, "Super Rich", "A Medal for super-rich people who spent " + Global.MONEY_SYMBOL + "10,000,000 at various shops."),

    // Evolution Solution
    EVOLUTION_HOPEFUL(1, 1, "Evolution Hopeful", "A Medal for promising Trainers who evolved a Pokémon for the first time."),
    EVOLUTION_TECH(2, 10, "Evolution Tech", "A Medal for skilled Trainers who evolved Pokémon 10 times."),
    EVOLUTION_EXPERT(3, 50, "Evolution Expert", "A Medal for great Trainers who evolved Pokémon 50 times."),
    EVOLUTION_AUTHORITY(4, 100, "Evolution Authority", "A Medal for exceptional Trainers who evolved Pokémon 100 times."),

    // Hidden Items Found
    DOWSING_BEGINNER(1, 1, "Dowsing Beginner", "A Medal to praise sharp-eyed Trainers who found a hidden item for the first time."),
    DOWSING_SPECIALIST(2, 10, "Dowsing Specialist", "A Medal to praise sharp-eyed Trainers who found 10 hidden items."),
    DOWSING_COLLECTOR(3, 50, "Dowsing Collector", "A Medal to praise sharp-eyed Trainers who found 50 hidden items."),
    DOWSING_WIZARD(4, 150, "Dowsing Wizard", "A Medal to praise peerlessly sharp-eyed Trainers who found 150 hidden items."),

    // Misc Medals
    MAGIKARP_AWARD(3, "Magikarp Award", "A Medal to praise the guts of Trainers who kept using Splash no matter what."),
    NEVER_GIVE_UP(3, "Never Give Up", "A Medal for those who don't know when to quit even when there's nothing they can do."),
    NONEFFECTIVE_ARTIST(3, "Noneffective Artist", "A consolation Medal for Trainers who made the cute mistake of using noneffective moves."),
    SUPEREFFECTIVE_SAVANT(4, 1000, "Supereffective Savant", "A Medal for Trainers who saw through many foes' weak points and battle to their best advantage."),
    NAMING_CHAMP(1, 10, "Naming Champ", "A Medal given to those who gave many nicknames to Pokémon."),

    // Medals Collected
    ROOKIE_MEDALIST(1, 50, "Rookie Medalist", "A Medal commemorating the advance to the Rookie Rank as the result of constant efforts in the Medal Rally."),
    ELITE_MEDALIST(2, 100, "Elite Medalist", "A Medal commemorating the advance to the Elite Rank as the result of constant efforts in the Medal Rally."),
    MASTER_MEDALIST(3, 150, "Master Medalist", "A Medal commemorating the advance to the Master Rank as the result of constant efforts in the Medal Rally."),
    LEGEND_MEDALIST(4, 200, "Legend Medalist", "A Medal commemorating the advance to the Legend Rank as the result of constant efforts in the Medal Rally."),
    TOP_MEDALIST(5, -1, "Top Medalist", "An honorable Medal for those who collected all the Medals.");

    static {
        TOP_MEDALIST.threshold = values().length - 1;
    }

    private final int imageIndex;
    private final String medalName;
    private final String description;
    private int threshold;

    Medal(int imageIndex, String medalName, String description) {
        this(imageIndex, -1, medalName, description);
    }

    Medal(int imageIndex, int threshold, String medalName, String description) {
        this.imageIndex = imageIndex;
        this.threshold = threshold;
        this.medalName = medalName;
        this.description = description;
    }

    public String getMedalName() {
        return this.medalName;
    }

    public String getDescription() {
        return this.description;
    }

    public int getThreshold() {
        return this.threshold;
    }

    public String getImageName() {
        return "medal" + imageIndex;
    }
}
