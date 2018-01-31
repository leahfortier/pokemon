package trainer.player.medal;

import main.Global;
import pokemon.PokemonInfo;
import type.Type;
import util.PokeString;

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
    WONDER_WRITER(4, 100, "Wonder Writer", "A Medal to recognize an astonishing journey that has been recorded 100 times."),

    // Pokecenter visits
    POKEMON_CENTER_FAN(1, 10, "Pokémon Center Fan", "A Medal given to kind Trainers who let their " + PokeString.POKEMON + " rest at " + PokeString.POKEMON + " Centers 10 times."),
    POKEMON_CENTER_SUPER_FAN(3, 100, "Pokémon Center Super Fan", "A Medal given to kind Trainers who let their " + PokeString.POKEMON + " rest at " + PokeString.POKEMON + " Centers 100 times."),

    // Bicycling riding
    STARTER_CYCLING(1, 1, "Starter Cycling", "A Medal given to beginning cyclists who rode a Bicycle for the first time."),
    EASY_CYCLING(2, 30, "Easy Cycling", "A Medal given to casual cyclists who have ridden a Bicycle 30 times."),
    HARD_CYCLING(3, 100, "Hard Cycling", "A Medal given to outstanding cyclists who have ridden a Bicycle 100 times."),
    PEDALING_LEGEND(4, 500, "Pedaling Legend", "A Medal given to earthshaking cyclists who have ridden a Bicycle 500 times."),

    // Fishing
    OLD_ROD_FISHERMAN(1, 1, "Old Rod Fisherman", "A Medal given to beginning fishers who reeled in a " + PokeString.POKEMON + " for the first time."),
    GOOD_ROD_FISHERMAN(2, 10, "Good Rod Fisherman", "A Medal given to leisure fishers who reeled in 10 " + PokeString.POKEMON + "."),
    SUPER_ROD_FISHERMAN(3, 50, "Super Rod Fisherman", "A Medal given to very experienced fishers who reeled in 50 " + PokeString.POKEMON + "."),
    MIGHTY_FISHER(4, 100, "Mighty Fisher", "A Medal given to legendary fishers who reeled in 100 " + PokeString.POKEMON + "."),

    // Eggy
    BABY_CAKES(4, PokemonInfo.getNumBabyPokemon(), "Baby Cakes", "A Medal to prove the parental instincts of people who hatched every single baby " + PokeString.POKEMON + "."),
    CHAMPION_OF_GENETICS(4, "Champion of Genetics", "A Medal for patient trainers who crave only the best of the best."),
    EGG_BEGINNER(1, 1, "Egg Beginner", "A Medal to prove the fresh parental instincts of people who hatched a " + PokeString.POKEMON + " Egg for the first time."),
    EGG_BREEDER(2, 10, "Egg Breeder", "A Medal to prove the decent parental instincts of people who hatched 10 " + PokeString.POKEMON + " Eggs."),
    EGG_ELITE(3, 50, "Egg Elite", "A Medal to prove the outstanding parental instincts of people who hatched 50 " + PokeString.POKEMON + " Eggs."),
    HATCHING_AFICIONADO(4, 100, "Hatching Aficionado", "A Medal to prove the endless parental instincts of people who hatched 100 " + PokeString.POKEMON + " Eggs."),
    DAY_CARE_FAITHFUL(1, 10, "Day-Care Faithful", "A Medal for those who love to raise " + PokeString.POKEMON + " and left 10 " + PokeString.POKEMON + " at the " + PokeString.POKEMON + " Day Care."),
    DAY_CARE_SUPER_FAITHFUL(2, 50, "Day-Care Super Faithful", "A Medal for those who love to raise " + PokeString.POKEMON + " and left 50 " + PokeString.POKEMON + " at the " + PokeString.POKEMON + " Day Care."),
    DAY_CARE_EXTRAORDINARY_FAITHFUL(4, 100, "Day-Care Extraordinary Faithful", "A Medal for those who love to raise Pokémon and left 100 " + PokeString.POKEMON + " at the " + PokeString.POKEMON + " Day Care."),

    // Spend Dat Cash Money
    REGULAR_CUSTOMER(3, 100, "Regular Customer", "A Medal for Trainers who kept going to various shops and became regular customers."),
    SMART_SHOPPER(1, "Smart Shopper", "A Medal for thrifty shoppers who made bulk purchases and got bonus Premier Balls."),
    MODERATE_CUSTOMER(1, 10000, "Moderate Customer", "A Medal for rich people who spent " + Global.MONEY_SYMBOL + "10,000 at various shops."),
    GREAT_CUSTOMER(2, 100000, "Great Customer", "A Medal for rich people who spent " + Global.MONEY_SYMBOL + "100,000 at various shops."),
    INDULGENT_CUSTOMER(3, 1000000, "Indulgent Customer", "A Medal for rich people who spent " + Global.MONEY_SYMBOL + "1,000,000 at various shops."),
    SUPER_RICH(4, 10000000, "Super Rich", "A Medal for super-rich people who spent " + Global.MONEY_SYMBOL + "10,000,000 at various shops."),

    // Evolution Solution
    EVOLUTION_HOPEFUL(1, 1, "Evolution Hopeful", "A Medal for promising Trainers who evolved a " + PokeString.POKEMON + " for the first time."),
    EVOLUTION_TECH(2, 10, "Evolution Tech", "A Medal for skilled Trainers who evolved " + PokeString.POKEMON + " 10 times."),
    EVOLUTION_EXPERT(3, 50, "Evolution Expert", "A Medal for great Trainers who evolved " + PokeString.POKEMON + " 50 times."),
    EVOLUTION_AUTHORITY(4, 100, "Evolution Authority", "A Medal for exceptional Trainers who evolved " + PokeString.POKEMON + " 100 times."),

    // Shiiiiinnnnnnnnyyyyyyyy :)
    LUCKY_COLOR(1, 1, "Lucky Color", "A Medal to praise the luck of people who have witnessed a Shiny " + PokeString.POKEMON + "."),
    LUCKIER_COLOR(2, 3, "Luckier Color", "A Medal to praise the luck of people who have witnessed 3 Shiny " + PokeString.POKEMON + "."),
    LUCKIEST_COLOR(3, 5, "Luckiest Color", "A Medal to praise the luck of people who have witnessed 5 Shiny " + PokeString.POKEMON + "."),
    SUPER_DUPER_LUCKIEST_COLOR(4, 10, "Super Duper Luckiest Color", "A Medal to praise the luck of people who have witnessed 10 Shiny " + PokeString.POKEMON + "."),

    // Hidden Items Found
    DOWSING_BEGINNER(1, 1, "Dowsing Beginner", "A Medal to praise sharp-eyed Trainers who found a hidden item for the first time."),
    DOWSING_SPECIALIST(2, 10, "Dowsing Specialist", "A Medal to praise sharp-eyed Trainers who found 10 hidden items."),
    DOWSING_COLLECTOR(3, 50, "Dowsing Collector", "A Medal to praise sharp-eyed Trainers who found 50 hidden items."),
    DOWSING_WIZARD(4, 150, "Dowsing Wizard", "A Medal to praise peerlessly sharp-eyed Trainers who found 150 hidden items."),

    // Battles battled
    BATTLE_LEARNER(1, 100, "Battle Learner", "A Medal for courageous young Trainers who went through 100 battles."),
    BATTLE_TEACHER(2, 200, "Battle Teacher", "A Medal for ferociously courageous Trainers who went through 200 battles."),
    BATTLE_VETERAN(3, 400, "Battle Veteran", "A Medal for tremendously courageous Trainers who went through 400 battles."),
    BATTLE_VIRTUOSO(4, 2000, "Battle Virtuoso", "A Medal for peerlessly courageous Trainers who went through 2,000 battles."),

    // Pokemon murdered
    RILEY(1, 17, "Riley", "A Medal for whiny, insecure Trainers who only killed 17 demons."),
    KENDRA(2, 100, "Kendra", "A Medal for skilled Trainers with bad accents who killed 100 demons."),
    FAITH(3, 500, "Faith", "A Medal for psycho-killer Trainers who killed 500 demons for pleasure."),
    BUFFY(4, 5000, "Buffy", "A Medal for Trainers who killed 5000 demons and are now recognized as the Chosen One."),

    // Pokedexter
    GWENDOLYN_POST(1, 151, "Gwendolyn Post", "A Medal for Trainers who have encountered 151 different species of " + PokeString.POKEMON + "."),
    MERRICK(2, 386, "Merrick", "A Medal for Trainers who have encountered 386 different species of " + PokeString.POKEMON + "."),
    WESLEY_WYNDAM_PRYCE(3, 649, "Wesley Wyndam-Pryce", "A Medal for Trainers who have encountered 649 different species of " + PokeString.POKEMON + "."),
    GILES(4, PokemonInfo.NUM_POKEMON, "Giles", "A Medal for Trainers who have encountered every species of " + PokeString.POKEMON + "."),
    PROFESSOR_MAPLE(1, 151, "Professor Maple", "A Medal for Trainers who have captured 151 different species of " + PokeString.POKEMON + "."),
    PROFESSOR_BIRCH(2, 386, "Professor Birch", "A Medal for Trainers who have captured 386 different species of " + PokeString.POKEMON + "."),
    PROFESSOR_ELM(3, 649, "Professor Elm", "A Medal for Trainers who have captured 649 different species of " + PokeString.POKEMON + "."),
    PROFESSOR_OAK(4, PokemonInfo.NUM_POKEMON, "Professor Oak", "A Medal for Trainers who have captured every species of " + PokeString.POKEMON + "."),
    MEWRE_THE_WINNER(4, "Mew're the Winner", "A Medal given to those worthy enough to encounter the legendary Mew."),

    // Typesters
    NORMAL_TYPE_BEGINNER(1, PokemonInfo.getNumTypedPokemon(Type.NORMAL)/4, "Normal-Type Beginner", "A Medal in commemoration of catching a quarter of all the Normal-type Pokémon."),
    NORMAL_TYPE_SPECIALIST(2, PokemonInfo.getNumTypedPokemon(Type.NORMAL)/2, "Normal-Type Specialist", "A Medal in commemoration of catching half of all the Normal-type Pokémon."),
    NORMAL_TYPE_COLLECTOR(3, 3*PokemonInfo.getNumTypedPokemon(Type.NORMAL)/4, "Normal-Type Collector", "A Medal in commemoration of catching three-quarters of all the Normal-type Pokémon."),
    NORMAL_TYPE_MASTERFUL_WIZARD(4, PokemonInfo.getNumTypedPokemon(Type.NORMAL), "Normal-Type Masterful Wizard", "A Medal in commemoration of catching all the Normal-type Pokémon."),
    FIRE_TYPE_BEGINNER(1, PokemonInfo.getNumTypedPokemon(Type.FIRE)/4, "Fire-Type Beginner", "A Medal in commemoration of catching a quarter of all the Fire-type Pokémon."),
    FIRE_TYPE_SPECIALIST(2, PokemonInfo.getNumTypedPokemon(Type.FIRE)/2, "Fire-Type Specialist", "A Medal in commemoration of catching half of all the Fire-type Pokémon."),
    FIRE_TYPE_COLLECTOR(3, 3*PokemonInfo.getNumTypedPokemon(Type.FIRE)/4, "Fire-Type Collector", "A Medal in commemoration of catching three-quarters of all the Fire-type Pokémon."),
    FIRE_TYPE_MASTERFUL_WIZARD(4, PokemonInfo.getNumTypedPokemon(Type.FIRE), "Fire-Type Masterful Wizard", "A Medal in commemoration of catching all the Fire-type Pokémon."),
    WATER_TYPE_BEGINNER(1, PokemonInfo.getNumTypedPokemon(Type.WATER)/4, "Water-Type Beginner", "A Medal in commemoration of catching a quarter of all the Water-type Pokémon."),
    WATER_TYPE_SPECIALIST(2, PokemonInfo.getNumTypedPokemon(Type.WATER)/2, "Water-Type Specialist", "A Medal in commemoration of catching half of all the Water-type Pokémon."),
    WATER_TYPE_COLLECTOR(3, 3*PokemonInfo.getNumTypedPokemon(Type.WATER)/4, "Water-Type Collector", "A Medal in commemoration of catching three-quarters of all the Water-type Pokémon."),
    WATER_TYPE_MASTERFUL_WIZARD(4, PokemonInfo.getNumTypedPokemon(Type.WATER), "Water-Type Masterful Wizard", "A Medal in commemoration of catching all the Water-type Pokémon."),
    ELECTRIC_TYPE_BEGINNER(1, PokemonInfo.getNumTypedPokemon(Type.ELECTRIC)/4, "Electric-Type Beginner", "A Medal in commemoration of catching a quarter of all the Electric-type Pokémon."),
    ELECTRIC_TYPE_SPECIALIST(2, PokemonInfo.getNumTypedPokemon(Type.ELECTRIC)/2, "Electric-Type Specialist", "A Medal in commemoration of catching half of all the Electric-type Pokémon."),
    ELECTRIC_TYPE_COLLECTOR(3, 3*PokemonInfo.getNumTypedPokemon(Type.ELECTRIC)/4, "Electric-Type Collector", "A Medal in commemoration of catching three-quarters of all the Electric-type Pokémon."),
    ELECTRIC_TYPE_MASTERFUL_WIZARD(4, PokemonInfo.getNumTypedPokemon(Type.ELECTRIC), "Electric-Type Masterful Wizard", "A Medal in commemoration of catching all the Electric-type Pokémon."),
    GRASS_TYPE_BEGINNER(1, PokemonInfo.getNumTypedPokemon(Type.GRASS)/4, "Grass-Type Beginner", "A Medal in commemoration of catching a quarter of all the Grass-type Pokémon."),
    GRASS_TYPE_SPECIALIST(2, PokemonInfo.getNumTypedPokemon(Type.GRASS)/2, "Grass-Type Specialist", "A Medal in commemoration of catching half of all the Grass-type Pokémon."),
    GRASS_TYPE_COLLECTOR(3, 3*PokemonInfo.getNumTypedPokemon(Type.GRASS)/4, "Grass-Type Collector", "A Medal in commemoration of catching three-quarters of all the Grass-type Pokémon."),
    GRASS_TYPE_MASTERFUL_WIZARD(4, PokemonInfo.getNumTypedPokemon(Type.GRASS), "Grass-Type Masterful Wizard", "A Medal in commemoration of catching all the Grass-type Pokémon."),
    ICE_TYPE_BEGINNER(1, PokemonInfo.getNumTypedPokemon(Type.ICE)/4, "Ice-Type Beginner", "A Medal in commemoration of catching a quarter of all the Ice-type Pokémon."),
    ICE_TYPE_SPECIALIST(2, PokemonInfo.getNumTypedPokemon(Type.ICE)/2, "Ice-Type Specialist", "A Medal in commemoration of catching half of all the Ice-type Pokémon."),
    ICE_TYPE_COLLECTOR(3, 3*PokemonInfo.getNumTypedPokemon(Type.ICE)/4, "Ice-Type Collector", "A Medal in commemoration of catching three-quarters of all the Ice-type Pokémon."),
    ICE_TYPE_MASTERFUL_WIZARD(4, PokemonInfo.getNumTypedPokemon(Type.ICE), "Ice-Type Masterful Wizard", "A Medal in commemoration of catching all the Ice-type Pokémon."),
    FIGHTING_TYPE_BEGINNER(1, PokemonInfo.getNumTypedPokemon(Type.FIGHTING)/4, "Fighting-Type Beginner", "A Medal in commemoration of catching a quarter of all the Fighting-type Pokémon."),
    FIGHTING_TYPE_SPECIALIST(2, PokemonInfo.getNumTypedPokemon(Type.FIGHTING)/2, "Fighting-Type Specialist", "A Medal in commemoration of catching half of all the Fighting-type Pokémon."),
    FIGHTING_TYPE_COLLECTOR(3, 3*PokemonInfo.getNumTypedPokemon(Type.FIGHTING)/4, "Fighting-Type Collector", "A Medal in commemoration of catching three-quarters of all the Fighting-type Pokémon."),
    FIGHTING_TYPE_MASTERFUL_WIZARD(4, PokemonInfo.getNumTypedPokemon(Type.FIGHTING), "Fighting-Type Masterful Wizard", "A Medal in commemoration of catching all the Fighting-type Pokémon."),
    POISON_TYPE_BEGINNER(1, PokemonInfo.getNumTypedPokemon(Type.POISON)/4, "Poison-Type Beginner", "A Medal in commemoration of catching a quarter of all the Poison-type Pokémon."),
    POISON_TYPE_SPECIALIST(2, PokemonInfo.getNumTypedPokemon(Type.POISON)/2, "Poison-Type Specialist", "A Medal in commemoration of catching half of all the Poison-type Pokémon."),
    POISON_TYPE_COLLECTOR(3, 3*PokemonInfo.getNumTypedPokemon(Type.POISON)/4, "Poison-Type Collector", "A Medal in commemoration of catching three-quarters of all the Poison-type Pokémon."),
    POISON_TYPE_MASTERFUL_WIZARD(4, PokemonInfo.getNumTypedPokemon(Type.POISON), "Poison-Type Masterful Wizard", "A Medal in commemoration of catching all the Poison-type Pokémon."),
    GROUND_TYPE_BEGINNER(1, PokemonInfo.getNumTypedPokemon(Type.GROUND)/4, "Ground-Type Beginner", "A Medal in commemoration of catching a quarter of all the Ground-type Pokémon."),
    GROUND_TYPE_SPECIALIST(2, PokemonInfo.getNumTypedPokemon(Type.GROUND)/2, "Ground-Type Specialist", "A Medal in commemoration of catching half of all the Ground-type Pokémon."),
    GROUND_TYPE_COLLECTOR(3, 3*PokemonInfo.getNumTypedPokemon(Type.GROUND)/4, "Ground-Type Collector", "A Medal in commemoration of catching three-quarters of all the Ground-type Pokémon."),
    GROUND_TYPE_MASTERFUL_WIZARD(4, PokemonInfo.getNumTypedPokemon(Type.GROUND), "Ground-Type Masterful Wizard", "A Medal in commemoration of catching all the Ground-type Pokémon."),
    FLYING_TYPE_BEGINNER(1, PokemonInfo.getNumTypedPokemon(Type.FLYING)/4, "Flying-Type Beginner", "A Medal in commemoration of catching a quarter of all the Flying-type Pokémon."),
    FLYING_TYPE_SPECIALIST(2, PokemonInfo.getNumTypedPokemon(Type.FLYING)/2, "Flying-Type Specialist", "A Medal in commemoration of catching half of all the Flying-type Pokémon."),
    FLYING_TYPE_COLLECTOR(3, 3*PokemonInfo.getNumTypedPokemon(Type.FLYING)/4, "Flying-Type Collector", "A Medal in commemoration of catching three-quarters of all the Flying-type Pokémon."),
    FLYING_TYPE_MASTERFUL_WIZARD(4, PokemonInfo.getNumTypedPokemon(Type.FLYING), "Flying-Type Masterful Wizard", "A Medal in commemoration of catching all the Flying-type Pokémon."),
    PSYCHIC_TYPE_BEGINNER(1, PokemonInfo.getNumTypedPokemon(Type.PSYCHIC)/4, "Psychic-Type Beginner", "A Medal in commemoration of catching a quarter of all the Psychic-type Pokémon."),
    PSYCHIC_TYPE_SPECIALIST(2, PokemonInfo.getNumTypedPokemon(Type.PSYCHIC)/2, "Psychic-Type Specialist", "A Medal in commemoration of catching half of all the Psychic-type Pokémon."),
    PSYCHIC_TYPE_COLLECTOR(3, 3*PokemonInfo.getNumTypedPokemon(Type.PSYCHIC)/4, "Psychic-Type Collector", "A Medal in commemoration of catching three-quarters of all the Psychic-type Pokémon."),
    PSYCHIC_TYPE_MASTERFUL_WIZARD(4, PokemonInfo.getNumTypedPokemon(Type.PSYCHIC), "Psychic-Type Masterful Wizard", "A Medal in commemoration of catching all the Psychic-type Pokémon."),
    BUG_TYPE_BEGINNER(1, PokemonInfo.getNumTypedPokemon(Type.BUG)/4, "Bug-Type Beginner", "A Medal in commemoration of catching a quarter of all the Bug-type Pokémon."),
    BUG_TYPE_SPECIALIST(2, PokemonInfo.getNumTypedPokemon(Type.BUG)/2, "Bug-Type Specialist", "A Medal in commemoration of catching half of all the Bug-type Pokémon."),
    BUG_TYPE_COLLECTOR(3, 3*PokemonInfo.getNumTypedPokemon(Type.BUG)/4, "Bug-Type Collector", "A Medal in commemoration of catching three-quarters of all the Bug-type Pokémon."),
    BUG_TYPE_MASTERFUL_WIZARD(4, PokemonInfo.getNumTypedPokemon(Type.BUG), "Bug-Type Masterful Wizard", "A Medal in commemoration of catching all the Bug-type Pokémon."),
    ROCK_TYPE_BEGINNER(1, PokemonInfo.getNumTypedPokemon(Type.ROCK)/4, "Rock-Type Beginner", "A Medal in commemoration of catching a quarter of all the Rock-type Pokémon."),
    ROCK_TYPE_SPECIALIST(2, PokemonInfo.getNumTypedPokemon(Type.ROCK)/2, "Rock-Type Specialist", "A Medal in commemoration of catching half of all the Rock-type Pokémon."),
    ROCK_TYPE_COLLECTOR(3, 3*PokemonInfo.getNumTypedPokemon(Type.ROCK)/4, "Rock-Type Collector", "A Medal in commemoration of catching three-quarters of all the Rock-type Pokémon."),
    ROCK_TYPE_MASTERFUL_WIZARD(4, PokemonInfo.getNumTypedPokemon(Type.ROCK), "Rock-Type Masterful Wizard", "A Medal in commemoration of catching all the Rock-type Pokémon."),
    GHOST_TYPE_BEGINNER(1, PokemonInfo.getNumTypedPokemon(Type.GHOST)/4, "Ghost-Type Beginner", "A Medal in commemoration of catching a quarter of all the Ghost-type Pokémon."),
    GHOST_TYPE_SPECIALIST(2, PokemonInfo.getNumTypedPokemon(Type.GHOST)/2, "Ghost-Type Specialist", "A Medal in commemoration of catching half of all the Ghost-type Pokémon."),
    GHOST_TYPE_COLLECTOR(3, 3*PokemonInfo.getNumTypedPokemon(Type.GHOST)/4, "Ghost-Type Collector", "A Medal in commemoration of catching three-quarters of all the Ghost-type Pokémon."),
    GHOST_TYPE_MASTERFUL_WIZARD(4, PokemonInfo.getNumTypedPokemon(Type.GHOST), "Ghost-Type Masterful Wizard", "A Medal in commemoration of catching all the Ghost-type Pokémon."),
    DRAGON_TYPE_BEGINNER(1, PokemonInfo.getNumTypedPokemon(Type.DRAGON)/4, "Dragon-Type Beginner", "A Medal in commemoration of catching a quarter of all the Dragon-type Pokémon."),
    DRAGON_TYPE_SPECIALIST(2, PokemonInfo.getNumTypedPokemon(Type.DRAGON)/2, "Dragon-Type Specialist", "A Medal in commemoration of catching half of all the Dragon-type Pokémon."),
    DRAGON_TYPE_COLLECTOR(3, 3*PokemonInfo.getNumTypedPokemon(Type.DRAGON)/4, "Dragon-Type Collector", "A Medal in commemoration of catching three-quarters of all the Dragon-type Pokémon."),
    DRAGON_TYPE_MASTERFUL_WIZARD(4, PokemonInfo.getNumTypedPokemon(Type.DRAGON), "Dragon-Type Masterful Wizard", "A Medal in commemoration of catching all the Dragon-type Pokémon."),
    DARK_TYPE_BEGINNER(1, PokemonInfo.getNumTypedPokemon(Type.DARK)/4, "Dark-Type Beginner", "A Medal in commemoration of catching a quarter of all the Dark-type Pokémon."),
    DARK_TYPE_SPECIALIST(2, PokemonInfo.getNumTypedPokemon(Type.DARK)/2, "Dark-Type Specialist", "A Medal in commemoration of catching half of all the Dark-type Pokémon."),
    DARK_TYPE_COLLECTOR(3, 3*PokemonInfo.getNumTypedPokemon(Type.DARK)/4, "Dark-Type Collector", "A Medal in commemoration of catching three-quarters of all the Dark-type Pokémon."),
    DARK_TYPE_MASTERFUL_WIZARD(4, PokemonInfo.getNumTypedPokemon(Type.DARK), "Dark-Type Masterful Wizard", "A Medal in commemoration of catching all the Dark-type Pokémon."),
    STEEL_TYPE_BEGINNER(1, PokemonInfo.getNumTypedPokemon(Type.STEEL)/4, "Steel-Type Beginner", "A Medal in commemoration of catching a quarter of all the Steel-type Pokémon."),
    STEEL_TYPE_SPECIALIST(2, PokemonInfo.getNumTypedPokemon(Type.STEEL)/2, "Steel-Type Specialist", "A Medal in commemoration of catching half of all the Steel-type Pokémon."),
    STEEL_TYPE_COLLECTOR(3, 3*PokemonInfo.getNumTypedPokemon(Type.STEEL)/4, "Steel-Type Collector", "A Medal in commemoration of catching three-quarters of all the Steel-type Pokémon."),
    STEEL_TYPE_MASTERFUL_WIZARD(4, PokemonInfo.getNumTypedPokemon(Type.STEEL), "Steel-Type Masterful Wizard", "A Medal in commemoration of catching all the Steel-type Pokémon."),
    FAIRY_TYPE_BEGINNER(1, PokemonInfo.getNumTypedPokemon(Type.FAIRY)/4, "Fairy-Type Beginner", "A Medal in commemoration of catching a quarter of all the Fairy-type Pokémon."),
    FAIRY_TYPE_SPECIALIST(2, PokemonInfo.getNumTypedPokemon(Type.FAIRY)/2, "Fairy-Type Specialist", "A Medal in commemoration of catching half of all the Fairy-type Pokémon."),
    FAIRY_TYPE_COLLECTOR(3, 3*PokemonInfo.getNumTypedPokemon(Type.FAIRY)/4, "Fairy-Type Collector", "A Medal in commemoration of catching three-quarters of all the Fairy-type Pokémon."),
    FAIRY_TYPE_MASTERFUL_WIZARD(4, PokemonInfo.getNumTypedPokemon(Type.FAIRY), "Fairy-Type Masterful Wizard", "A Medal in commemoration of catching all the Fairy-type Pokémon."),

    // Grindy
    THREE_WHOLE_DIGITS(1, 1, "Three Whole Digits", "A Medal for Trainers who leveled a Pokémon up to level 100."),
    I_LIKE_TO_GRIND(2, 10, "I Like to Grind", "A Medal for Trainers who leveled 10 Pokémon up to level 100."),
    GRINDING_EXTRAORDINAIRE(4, 100, "Grinding Extraordinaire", "A Medal for Trainers who leveled 100 Pokémon up to level 100."),

    // Misc Medals
    MAGIKARP_AWARD(3, "Magikarp Award", "A Medal to praise the guts of Trainers who kept using Splash no matter what."),
    NEVER_GIVE_UP(3, "Never Give Up", "A Medal for those who don't know when to quit even when there's nothing they can do."),
    NONEFFECTIVE_ARTIST(3, "Noneffective Artist", "A consolation Medal for Trainers who made the cute mistake of using noneffective moves."),
    SUPEREFFECTIVE_SAVANT(4, 1000, "Supereffective Savant", "A Medal for Trainers who saw through many foes' weak points and battle to their best advantage."),
    NAMING_CHAMP(1, 10, "Naming Champ", "A Medal given to those who gave many nicknames to " + PokeString.POKEMON + "."),
    TRAINED_TO_MAX_POTENTIAL(3, "Trained to Maximum Potential", "A Medal for diligent trainers who have trained a " + PokeString.POKEMON + " to their limits."),

    // Medals Collected
    ROOKIE_MEDALIST(1, 50, "Rookie Medalist", "A Medal commemorating the advance to the Rookie Rank as the result of constant efforts in the Medal Rally."),
    ELITE_MEDALIST(2, 100, "Elite Medalist", "A Medal commemorating the advance to the Elite Rank as the result of constant efforts in the Medal Rally."),
    MASTER_MEDALIST(3, 150, "Master Medalist", "A Medal commemorating the advance to the Master Rank as the result of constant efforts in the Medal Rally."),
    LEGEND_MEDALIST(4, 200, "Legend Medalist", "A Medal commemorating the advance to the Legend Rank as the result of constant efforts in the Medal Rally."),
    TOP_MEDALIST(5, "Top Medalist", "An honorable Medal for those who collected all the Medals.");

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

    public boolean hasThreshold() {
        return this.threshold > 0;
    }

    public int getThreshold() {
        return this.threshold;
    }

    public String getImageName() {
        return getImageName(this.imageIndex);
    }

    private static String getImageName(int imageIndex) {
        return "medal" + imageIndex;
    }

    public static String getUnknownMedalImageName() {
        return getImageName(0);
    }
}
