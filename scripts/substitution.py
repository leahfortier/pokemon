#!/usr/bin/env python3
# -*- coding: utf-8 -*-
from typing import List

from scripts.forms import AddedPokes, Stat
from scripts.move import LevelUpMoves
from scripts.util import index_swap


# Attack replacement rules that always apply independent of pokemon and learn method
def _attack_substitution(attack: str) -> str:
    if attack is None or attack == '':
        raise Exception()

    # Ion Deluge was combined into Electrify
    if attack == 'ION_DELUGE':
        return 'ELECTRIFY'
    # Intentional spelling change
    elif attack == 'JUDGMENT':
        return 'JUDGEMENT'

    return attack


# Replaces the learnable move with an alternative if applicable
# Will return the empty string to indicate removing the move
def learnable_attack_substitution(attack: str) -> str:
    attack = _attack_substitution(attack)
    if is_unsupported_attack(attack):
        return ''

    return attack


# Returns true if the input namesies attack is a move which was removed from the game
def is_unsupported_attack(attack: str) -> bool:
    return attack in ['AFTER_YOU', 'ALLY_SWITCH', 'FOLLOW_ME', 'FRUSTRATION', 'HELPING_HAND', 'INSTRUCT', 'QUASH',
                      'RAGE_POWDER', 'RETURN', 'SPOTLIGHT', 'WIDE_GUARD']


# Other learnable moves that were manually added (because I think it makes sense for Butterfree to Fly)
def learnable_attack_additions(num: int) -> List[str]:
    additions = []  # type: List[str]

    # Manually add Fly for:
    # Butterfree, Beedrill, Venomoth, Scyther, Dragonair, Ledyba line,
    # Natu, Yanma, Gligar, Beautifly, Dustox, Masquerain, Ninjask,
    # Shedinja, Volbeat, Illumise, Mothim, Vespiquen, Garchomp, Yanmega,
    # Gliscor, Emolga, Vivillon, Rowlet line, Vikavolt, Cutiefly line
    if num in [12, 15, 49, 123, 148, 165, 166,
               177, 193, 207, 267, 269, 284, 291,
               292, 313, 314, 414, 416, 445, 469,
               472, 587, 666, 722, 723, 724, 738, 742, 743]:
        additions.append("FLY")

    return additions


def level_up_attack_additions(num: int, moves: LevelUpMoves) -> None:
    # Bulbasaur -- not sure why the fuck it doesn't learn Solar Beam by level up unless it evolves but that's DUMB
    # Note: If Bulby's level-up moves change in general the level might need tweaking
    if num == 1:
        moves.add(39, 'SOLAR_BEAM')
    # Rapidash -- it should definitely learn Blaze Kick, right???
    elif num == 78:
        moves.add_default('BLAZE_KICK')
    # Luxray -- I added Dark-type to it and it should learn some Dark moves
    elif num == 405:
        moves.add_evolution('FOUL_PLAY')
    # Budew -- really wanted to add Leech Seed in earlier (in attack_substitution)
    # Note: This should be edited if Budew's move list changes
    elif num == 406:
        moves.add(19, 'WORRY_SEED')
    # Togekiss -- like come on it should know this plus already changed Togetic to learn it by level up
    elif num == 468:
        moves.add_default('MOONBLAST')
    # Asbel -- needs some Fairy moves and people to notice its lovely wings
    elif num == AddedPokes.MEGA_ABSOL.value:
        moves.add_evolution('PLAY_ROUGH')
        moves.add_default('MOONLIGHT')
        moves.add_default('WING_ATTACK') # BECAUSE IT HAS WINGS
        moves.add_default('TAILWIND')


# Replaces value with alternative if it is the same as to_replace
def _replace(value: str, to_replace: str, alternative: str) -> str:
    if value == to_replace:
        return alternative
    return value


# Will swap the moves if the attack is either of them
# This is to avoid overwriting by using _replace_move twice in a row
def _swap_move(attack: str, first: str, second: str) -> str:
    if attack == first:
        return second
    elif attack == second:
        return first
    return attack


# Replaces the level-up move with an alternative if applicable for the corresponding Pokemon
# Will return the empty string to indicate removing the move altogether
# The input attack string is expected to be in namesies format
def attack_substitution(num: int, attack: str) -> str:
    attack = _attack_substitution(attack)

    # Butterfree
    if num == 12:
        attack = _replace(attack, 'RAGE_POWDER', 'MORNING_SUN')
    # Nidoran line
    elif 29 <= num <= 33:
        attack = _replace(attack, 'HELPING_HAND', 'POISON_TAIL')
    # Clefairy/Clefable (Clefable does not learn After You/Follow Me technically)
    elif num in [35, 36]:
        # Note: I know it looks weird that multiple moves are substituting for Wish, but one is the last move
        # learned and the other is a default move and tbh it kind of works (if changes test will catch also)
        attack = _replace(attack, 'AFTER_YOU', 'WISH')
        attack = _replace(attack, 'FOLLOW_ME', 'MIMIC')
        attack = _replace(attack, 'SPOTLIGHT', 'WISH')
    # Paras/Parasect
    elif num in [46, 47]:
        attack = _replace(attack, 'RAGE_POWDER', 'LEECH_LIFE')
    # Growlithe
    elif num == 58:
        attack = _replace(attack, 'HELPING_HAND', 'HOWL')
    # Kadabra/Alakazam
    elif num in [64, 65]:
        attack = _replace(attack, 'ALLY_SWITCH', 'BARRIER')
    # Hitmonlee
    elif num == 106:
        attack = _replace(attack, 'WIDE_GUARD', 'LOW_KICK')
    # Sentret/Furret
    elif num in [161, 162]:
        attack = _replace(attack, 'FOLLOW_ME', 'COVET')
        attack = _replace(attack, 'HELPING_HAND', 'CHARM')
    # Togepi
    elif num == 175:
        attack = _replace(attack, 'AFTER_YOU', 'SOFT_BOILED')
        attack = _replace(attack, 'FOLLOW_ME', 'DRAINING_KISS')
    # Togetic
    elif num == 176:
        attack = _replace(attack, 'AFTER_YOU', 'MOONBLAST')
        attack = _replace(attack, 'FOLLOW_ME', 'DRAINING_KISS')
    # Marill/Azumarill/Azurill
    elif num in [183, 184, 298]:
        attack = _replace(attack, 'HELPING_HAND', 'DRAINING_KISS')
    # Hoppip line
    elif num in [187, 188, 189]:
        attack = _replace(attack, 'RAGE_POWDER', 'SILVER_WIND')
    # Murkrow/Honchkrow
    elif num in [198, 430]:
        attack = _replace(attack, 'QUASH', 'ROOST')
    # Mantine/Mantyke
    elif num in [226, 458]:
        attack = _replace(attack, 'WIDE_GUARD', 'DIVE')
    # Tyrogue
    elif num == 236:
        # Did you know that Tyrogue's only level-up moves are four default ones?
        attack = _replace(attack, 'HELPING_HAND', 'ENDURE')
    # Hitmontop
    elif num == 237:
        attack = _replace(attack, 'WIDE_GUARD', 'DRILL_RUN')
    # Sableye/Yamirami
    elif num in [302, AddedPokes.MEGA_SABLEYE.value]:
        attack = _replace(attack, 'QUASH', 'NIGHT_SLASH')
    # Plusle/Minun
    elif num in [311, 312]:
        attack = _replace(attack, 'HELPING_HAND', 'SWEET_KISS')
    # Volbeat/Illumise
    elif num in [313, 314]:
        attack = _replace(attack, 'HELPING_HAND', 'DIZZY_PUNCH')
    # Jirachi
    elif num == 385:
        attack = _replace(attack, 'HELPING_HAND', 'CALM_MIND')
    # Budew
    elif num == 406:
        # You can get Budew early in this game and Leech Seed is super helpful and it should definitely learn it early
        # This is in correspondence with level_up_attack_additions where it readds the Worry Seed later
        # Note: This definitely needs to be looked at if it's move list ever changes
        attack = _replace(attack, 'WORRY_SEED', 'LEECH_SEED')
    # Cherubi/Cherrim
    elif num in [420, 421]:
        attack = _replace(attack, 'HELPING_HAND', 'TICKLE')
    # Buneary
    elif num == 427:
        attack = _replace(attack, 'AFTER_YOU', 'SWEET_KISS')
    # Lopunny
    elif num == 428:
        attack = _replace(attack, 'AFTER_YOU', 'DRAINING_KISS')
        attack = _replace(attack, 'RETURN', 'CAPTIVATE')
    # Gallade
    elif num == 475:
        attack = _replace(attack, 'HELPING_HAND', 'DUAL_CHOP')
        attack = _replace(attack, 'WIDE_GUARD', 'SACRED_SWORD')
    # Regigagas
    elif num == 486:
        attack = _replace(attack, 'WIDE_GUARD', 'MEGA_PUNCH')
    # Patrat/Watchog
    elif num in [504, 505]:
        attack = _replace(attack, 'AFTER_YOU', 'COVET')
    # Lillipup line
    elif num in [506, 507, 508]:
        attack = _replace(attack, 'HELPING_HAND', 'HOWL')
    # Audino
    elif num == 531:
        attack = _replace(attack, 'AFTER_YOU', 'WISH')
    # Throh
    elif num == 538:
        attack = _replace(attack, 'WIDE_GUARD', 'BRICK_BREAK')
    # Leavanny
    elif num == 542:
        attack = _replace(attack, 'HELPING_HAND', 'ME_FIRST')
    # Cottonee
    elif num == 546:
        attack = _replace(attack, 'HELPING_HAND', 'TAILWIND')
    # Petilil
    elif num == 548:
        attack = _replace(attack, 'AFTER_YOU', 'HEAL_BELL')
        attack = _replace(attack, 'HELPING_HAND', 'FAIRY_WIND')
    # Maractus
    elif num == 556:
        attack = _replace(attack, 'AFTER_YOU', 'WOOD_HAMMER')
    # Tirtouga/Carracosta
    elif num in [564, 565]:
        attack = _replace(attack, 'WIDE_GUARD', 'IRON_DEFENSE')
    # Minccino
    elif num == 572:
        attack = _replace(attack, 'AFTER_YOU', 'IRON_TAIL')
        attack = _replace(attack, 'HELPING_HAND', 'COVET')
    # Foongus/Amoonguss
    elif num in [590, 591]:
        attack = _replace(attack, 'RAGE_POWDER', 'GASTRO_ACID')
    # Alomomola
    elif num == 594:
        attack = _replace(attack, 'HELPING_HAND', 'REFRESH')
        attack = _replace(attack, 'WIDE_GUARD', 'MIST')
    # Mienshao
    elif num == 620:
        attack = _replace(attack, 'WIDE_GUARD', 'DUAL_CHOP')
    # Volcarona
    elif num == 637:
        attack = _replace(attack, 'RAGE_POWDER', 'MORNING_SUN')
    # Cobalion
    elif num == 638:
        attack = _replace(attack, 'HELPING_HAND', 'IRON_DEFENSE')
    # Terrakion
    elif num == 639:
        attack = _replace(attack, 'HELPING_HAND', 'STEALTH_ROCK')
    # Virizion
    elif num == 640:
        attack = _replace(attack, 'HELPING_HAND', 'SYNTHESIS')
    # Keldeo
    elif num == 647:
        attack = _replace(attack, 'HELPING_HAND', 'ICY_WIND')
    # Oricorio
    elif num == 741:
        attack = _replace(attack, 'HELPING_HAND', 'TAILWIND')
    # Wishiwashi
    elif num == 746:
        attack = _replace(attack, 'HELPING_HAND', 'WATER_SPORT')
    # Mareanie/Toxapex
    elif num in [747, 748]:
        attack = _replace(attack, 'WIDE_GUARD', 'ACID_ARMOR')
    # Morelull/Shiinotic
    elif num in [755, 756]:
        attack = _replace(attack, 'SPOTLIGHT', 'AROMATHERAPY')
    # Oranguru
    elif num == 765:
        attack = _replace(attack, 'AFTER_YOU', 'WONDER_ROOM')
        attack = _replace(attack, 'INSTRUCT', 'LIGHT_SCREEN')
        attack = _replace(attack, 'QUASH', 'PSYCHIC_TERRAIN')
    # Pykumuku
    elif num == 771:
        attack = _replace(attack, 'HELPING_HAND', 'TICKLE')
    # Solgaleo
    elif num == 791:
        attack = _replace(attack, 'WIDE_GUARD', 'REFLECT')
    # Lunala
    elif num == 792:
        attack = _replace(attack, 'WIDE_GUARD', 'LIGHT_SCREEN')
    # Stakataka
    elif num == 805:
        attack = _replace(attack, 'WIDE_GUARD', 'STONE_EDGE')
    # Orbeetle
    elif num == 826:
        attack = _replace(attack, 'ALLY_SWITCH', 'REFLECT')
        attack = _replace(attack, 'AFTER_YOU', 'LIGHT_SCREEN')
    # Mr. Rime
    elif num == 866:
        attack = _replace(attack, 'ALLY_SWITCH', 'SAFEGUARD')
    # Frosmoth
    elif num == 873:
        attack = _replace(attack, 'WIDE_GUARD', 'MOONLIGHT')
    # Stonjourner
    elif num == 874:
        attack = _replace(attack, 'WIDE_GUARD', 'IRON_DEFENSE')
    # Indeedee
    elif num == 876:
        attack = _replace(attack, 'HELPING_HAND', 'WONDER_ROOM')
        attack = _replace(attack, 'AFTER_YOU', 'PSYCHIC_TERRAIN')
    # Rizardon
    elif num == AddedPokes.MEGA_CHARIZARD.value:
        # These correspond with the level_up_attack_additions for Rizardon
        # Basically Dragon Claw changes from default to evolution
        # Wing Attack changes from evolution to default
        # Inferno (level 62) changes to Outrage
        attack = _swap_move(attack, 'WING_ATTACK', 'DRAGON_CLAW')
        attack = _replace(attack, 'INFERNO', 'OUTRAGE')
    # Mr. Rime Jr.
    elif num == AddedPokes.GALARIAN_MR_MIME.value:
        attack = _replace(attack, 'ALLY_SWITCH', 'ROLE_PLAY')

    if is_unsupported_attack(attack):
        return ''
    else:
        return attack


# Removes or replaces the ability with an alternative if applicable for the corresponding Pokemon
# Will return the empty string to indicate removing ability (never 'NO_ABILITY')
# The input ability string is expected to be in namesies format
def ability_substitution(num: int, ability: str) -> str:
    # Clefable -- Pre-evolutions have Friend Guard and causes terrible confusion
    if num == 36:
        ability = _replace(ability, 'UNAWARE', '')
    # Wigglytuff -- same deal as Clefable
    elif num == 40:
        ability = _replace(ability, 'FRISK', '')
    # Happiny -- similar but backwards to the Wigglyfable sitution
    elif num == 440:
        ability = _replace(ability, 'FRIEND_GUARD', 'HEALER')
    # Ferrothorn -- so this is incredible dumb even in the main game that Ferrothorn has a HA, but Ferroseed
    # doesn't?? that doesn't make any sense at all and I'm not condoning that type of behavior
    elif num == 598:
        ability = _replace(ability, 'ANTICIPATION', '')
    # Comfey -- this ability was changed and doesn't make as much sense anymore for Comfey
    elif num == 764:
        ability = _replace(ability, 'FLOWER_VEIL', '')

    # All abilities which were removed
    # Needs to be at the bottom for Pokemon with substitutions instead of removals
    if ability in ['FRIEND_GUARD', 'ILLUMINATE', 'MINUS', 'POWER_CONSTRUCT', 'PLUS', 'PROPELLER_TAIL',
                   'RECEIVER', 'STALWART', 'SYMBIOSIS', 'TELEPATHY', 'ZEN_MODE']:
        return ''

    return ability


# My personal type changes
# Types should be a size 2 list of non-empty namesies types (should use 'NO_TYPE' instead of empty)
def type_substitution(num: int, types: List[str]) -> List[str]:
    # Ninetales is now Psychic type
    if num == 38:
        assert types == ['FIRE', 'NO_TYPE']
        return ['FIRE', 'PSYCHIC']
    # Psyduck/Golduck are now Psychic type
    elif num == 54 or num == 55:
        assert types == ['WATER', 'NO_TYPE']
        return ['WATER', 'PSYCHIC']
    # Gyarados is now Water/Dragon instead of Water/Flying
    elif num == 130:
        assert types == ['WATER', 'FLYING']
        return ['WATER', 'DRAGON']
    # Noctowl is now Psychic/Flying
    elif num == 164:
        assert types == ['NORMAL', 'FLYING']
        return ['PSYCHIC', 'FLYING']
    # Luxray is now Dark type
    elif num == 405:
        assert types == ['ELECTRIC', 'NO_TYPE']
        return ['ELECTRIC', 'DARK']
    # Flabebe line is now Grass type
    elif 669 <= num <= 671:
        assert types == ['FAIRY', 'NO_TYPE']
        return ['FAIRY', 'GRASS']
    # Goomy line is now Water type
    elif 704 <= num <= 706:
        assert types == ['DRAGON', 'NO_TYPE']
        return ['DRAGON', 'WATER']
    # Mega Absol (Asbel) is Fairy type :)
    elif num == AddedPokes.MEGA_ABSOL.value:
        assert types == ['DARK', 'NO_TYPE']
        return ['DARK', 'FAIRY']

    return types


# Returns the replacement name of the Pokemon if applicable
# Returns the empty string if the Pokemon's name does not need to be replaced and should be looked up normally
def name_substitution(num: int) -> str:
    if num == 29:
        return "Nidoran F"
    elif num == 32:
        return "Nidoran M"
    # Flabebe has a stupid name with stupid special characters
    elif num == 669:
        return "Flabebe"
    # Meltan/Melmetal are not currently in PokeAPI and are just using placeholder information (figured would be less
    # confusing if I included their names though)
    elif num == 808:
        return "Meltan"
    elif num == 809:
        return "Melmetal"
    elif num == AddedPokes.MEGA_CHARIZARD.value:
        return "Rizardon"
    elif num == AddedPokes.MEGA_MAWILE.value:
        return "Kuchiito"
    elif num == AddedPokes.MEGA_ABSOL.value:
        return "Asbel"
    elif num == AddedPokes.MEGA_SABLEYE.value:
        return "Yamirami"
    elif num == AddedPokes.ALOLAN_RAICHU.value:
        return "Silph Surfer"
    elif num == AddedPokes.ALOLAN_SANDSHREW.value:
        return "Snowshrew"
    elif num == AddedPokes.ALOLAN_SANDSLASH.value:
        return "Snowslash"
    elif num == AddedPokes.ALOLAN_VULPIX.value:
        return "Yukikon"
    elif num == AddedPokes.ALOLAN_NINETALES.value:
        return "Kyukon"
    elif num == AddedPokes.ALOLAN_GRIMER.value:
        return "Sleima"
    elif num == AddedPokes.ALOLAN_MUK.value:
        return "Sleimok"
    elif num == AddedPokes.ALOLAN_EXEGGUTOR.value:
        return "Kokonatsu"
    elif num == AddedPokes.ALOLAN_MAROWAK.value:
        return "GaraGara"
    elif num == AddedPokes.MEGA_BANETTE.value:
        return "Jupetta"
    elif num == AddedPokes.MIDNIGHT_LYCANROC.value:
        return "Lougaroc"
    elif num == AddedPokes.DUSK_LYCANROC.value:
        return "Lugarugan"
    elif num == AddedPokes.GALARIAN_MEOWTH.value:
        return "Nyarth"
    elif num == AddedPokes.GALARIAN_PONTYA.value:
        return "Unita"
    elif num == AddedPokes.GALARIAN_RAPIDASH.value:
        return "Wisteridash"
    elif num == AddedPokes.GALARIAN_FARFETCHD.value:
        return "Squirfetch'd"
    elif num == AddedPokes.GALARIAN_WEEZING.value:
        return "Smogogo"
    elif num == AddedPokes.GALARIAN_MR_MIME.value:
        return "Mr. Rime Jr."
    elif num == AddedPokes.GALARIAN_CORSOLA.value:
        return "Cursayon"
    elif num == AddedPokes.GALARIAN_ZIGZAGOON.value:
        return "Zigzaton"
    elif num == AddedPokes.GALARIAN_LINOONE.value:
        return "Massuguma"
    elif num == AddedPokes.GALARIAN_DARUMAKA.value:
        return "Darumakice"
    elif num == AddedPokes.GALARIAN_DARMANITAN.value:
        return "Darmaniyeti"
    elif num == AddedPokes.GALARIAN_YAMASK.value:
        return "Yarune"

    return ''


# Replaces the gender ratio of the Pokemon if applicable
# female_ratio (and return value) is the chance of this Pokémon being female, in eighths; or -1 for genderless
def gender_substitution(num: int, female_ratio: int) -> int:
    # Silcoon/Beautifly, Gardevoir are 100% female now
    if num in [266, 267, 282]:
        assert female_ratio == 4
        return 8
    # Cascoon/Dustox, Glalie are 100% male now
    elif num in [268, 269, 362]:
        assert female_ratio == 4
        return 0

    return female_ratio


# Replaces the egg group of the Pokemon if applicable
def egg_group_substitution(num: int, egg_groups: List[str]) -> List[str]:
    # Nidorina and Nidoqueen for some super mysterious reason cannot breed in the games and it makes no sense
    if num in [30, 31]:
        assert egg_groups == ['NO_EGGS']
        return ['GROUND', 'MONSTER']

    return egg_groups


# Replaces the capture rate of the Pokemon if applicable
def capture_rate_substitution(num: int, capture_rate: int) -> int:
    # Necrozma -- 255 just seems really unreasonable
    if num == 800:
        assert capture_rate == 255
        return 45

    return capture_rate


# Replaces the stats of the Pokemon if applicable
# Directly edits the contents of stats which should be a size 6 list of the base stats
def stat_substitution(num: int, stats: List[int]) -> None:
    # Decrease Absol's attack since it has an evolution now
    if num == 359:
        stats[Stat.ATTACK.value] -= 30
    # Use Rotom's alternate form stats
    elif num == 479:
        stats[Stat.ATTACK.value] += 15
        stats[Stat.DEFENSE.value] += 30
        stats[Stat.SP_ATTACK.value] += 10
        stats[Stat.SP_DEFENSE.value] += 30
        stats[Stat.SPEED.value] -= 5
    # Use Charizard's stats with modifications
    elif num == AddedPokes.MEGA_CHARIZARD.value:
        index_swap(stats, Stat.ATTACK.value, Stat.SP_ATTACK.value)
        index_swap(stats, Stat.DEFENSE.value, Stat.SP_DEFENSE.value)
        stats[Stat.ATTACK.value] += 10
        stats[Stat.SPEED.value] -= 10
    # Use Absol's stats with increase speed
    elif num == AddedPokes.MEGA_ABSOL.value:
        stats[Stat.SPEED.value] += 20
    # Decrease mega attack stats
    elif num == AddedPokes.MEGA_BANETTE.value:
        stats[Stat.ATTACK.value] -= 35
        stats[Stat.SP_ATTACK.value] -= 10


# Replaces the effort values of the Pokemon if applicable
# Directly edits the contents of evs which should be a size 6 list of the effort values
def effort_substitution(num: int, evs: List[int]) -> None:
    if num == AddedPokes.MEGA_CHARIZARD.value:
        # Swap Attack and Sp. Attack for Rizardon
        index_swap(evs, Stat.ATTACK.value, Stat.SP_ATTACK.value)
    elif num == AddedPokes.MEGA_MAWILE.value:
        evs[Stat.DEFENSE.value] += 1
    elif num == AddedPokes.MEGA_ABSOL.value:
        evs[Stat.ATTACK.value] += 1
    elif num == AddedPokes.MEGA_SABLEYE.value:
        evs[Stat.ATTACK.value] -= 1
        evs[Stat.DEFENSE.value] += 1
        evs[Stat.SP_DEFENSE.value] += 1
    elif num == AddedPokes.MEGA_BANETTE.value:
        evs[Stat.ATTACK.value] += 1
