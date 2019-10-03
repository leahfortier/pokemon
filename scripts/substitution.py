#!/usr/bin/env python3
# -*- coding: utf-8 -*-
from typing import List

from scripts.forms import AddedPokes, Stat
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
    if attack in ['AFTER_YOU', 'ALLY_SWITCH', 'FOLLOW_ME', 'FRUSTRATION', 'HELPING_HAND', 'INSTRUCT', 'QUASH',
                  'RAGE_POWDER', 'RETURN', 'SPOTLIGHT', 'WIDE_GUARD']:
        return ''

    return attack


# Other learnable moves that were manually added (because I think it makes sense for Butterfree to Fly)
def learnable_attack_additions(num:int) -> List[str]:
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


# Replaces the level-up move with an alternative if applicable for the corresponding Pokemon
# Will return the empty string to indicate removing the move altogether
# The input attack string is expected to be in namesies format
def attack_substitution(num: int, attack: str) -> str:
    attack = _attack_substitution(attack)

    if attack == 'AFTER_YOU':
        # Patrat/Watchog
        if num == 504 or num == 505:
            return 'COVET'
        # Lopunny
        elif num == 428:
            return 'DRAINING_KISS'
        # Petilil
        elif num == 548:
            return 'HEAL_BELL'
        # Minccino
        elif num == 572:
            return 'IRON_TAIL'
        # Togetic/Togekiss
        elif num == 176 or num == 468:
            return 'MOONBLAST'
        # Togepi
        elif num == 175:
            return 'SOFT_BOILED'
        # Buneary
        elif num == 427:
            return 'SWEET_KISS'
        # Clefairy and Audino
        elif num == 35 or num == 531:
            return 'WISH'
        # Oranguru
        elif num == 765:
            return 'WONDER_ROOM'
        # Maractus
        elif num == 556:
            return 'WOOD_HAMMER'
    elif attack == 'ALLY_SWITCH':
        # Kadabra/Alakazam
        if num == 64 or num == 65:
            return 'BARRIER'
        # Hoopa
        elif num == 720:
            return 'MAGIC_ROOM'
    elif attack == 'FOLLOW_ME':
        # Sentret/Furret
        if num == 161 or num == 162:
            return 'COVET'
        # Togepi/Togetic
        elif num == 175 or num == 176:
            return 'DRAINING_KISS'
        # Clefairy
        elif num == 35:
            return 'MIMIC'
    elif attack == 'FRUSTRATION':
        # Buneary
        if num == 427:
            return 'FAKE_TEARS'
    elif attack == 'HELPING_HAND':
        # Meowstic
        if num == 678:
            return 'ASSIST'
        # Jirachi
        elif num == 385:
            return 'CALM_MIND'
        # Sentret/Furret
        elif num == 161 or num == 162:
            return 'CHARM'
        # Tyrogue and Minccino
        elif num == 236 or num == 572:
            return 'COVET'
        # Volbeat/Illumise
        elif num == 313 or num == 314:
            return 'DIZZY_PUNCH'
        # Marill/Azumarill/Azurill
        elif num == 183 or num == 184 or num == 298:
            return 'DRAINING_KISS'
        # Gallade
        elif num == 475:
            return 'DUAL_CHOP'
        # Petilil
        elif num == 548:
            return 'FAIRY_WIND'
        # Audino
        elif num == 531:
            return 'HEAL_BELL'
        # Growlithe and Lillipup line
        elif num == 58 or 506 <= num <= 508:
            return 'HOWL'
        # Keldeo
        elif num == 647:
            return 'ICY_WIND'
        # Cobalion
        elif num == 638:
            return 'IRON_DEFENSE'
        # Cinccino
        elif num == 573:
            return 'IRON_TAIL'
        # Magearna
        elif num == 801:
            return 'LIGHT_SCREEN'
        # Comfey
        elif num == 764:
            return 'LUCKY_CHANT'
        # Leavanny
        elif num == 542:
            return 'ME_FIRST'
        # Nidoran line
        elif 29 <= num <= 33:
            return 'POISON_TAIL'
        # Alomomola
        elif num == 594:
            return 'REFRESH'
        # Terrakion
        elif num == 639:
            return 'STEALTH_ROCK'
        # Plusle/Minun
        elif num == 311 or num == 312:
            return 'SWEET_KISS'
        # Virizion
        elif num == 640:
            return 'SYNTHESIS'
        # Latias/Latios and Cottonee and Oricorio
        elif num in [380, 381, 546, 741]:
            return 'TAILWIND'
        # Cherubi/Cherrim and Pykumuku
        elif num == 420 or num == 421 or num == 771:
            return 'TICKLE'
        # WISHiwashi
        elif num == 746:
            return 'WATER_SPORT'
        # Eeveelutions all start with this
        # Also Poipole and Naganadel -- TODO: too lazy to come up with an alternative right now
        elif num in [133, 134, 135, 136, 196, 197, 470, 471, 700, 803, 804]:
            return ''
    elif attack == 'INSTRUCT':
        # Oranguru
        if num == 765:
            return 'LIGHT_SCREEN'
    elif attack == 'QUASH':
        # Sableye
        if num == 302 or num == AddedPokes.MEGA_SABLEYE.value:
            return 'NIGHT_SLASH'
        # Oranguru
        elif num == 765:
            return 'PSYCHIC_TERRAIN'
        # Murkrow/Honchkrow
        elif num == 198 or num == 430:
            return 'ROOST'
    elif attack == 'RAGE_POWDER':
        # Foongus/Amoonguss
        if num == 590 or num == 591:
            return 'GASTRO_ACID'
        # Paras/Parasect
        elif num == 46 or num == 47:
            return 'LEECH_LIFE'
        # Butterfree and Volcarona
        elif num == 12 or num == 637:
            return 'MORNING_SUN'
        # Hoppip line
        elif 187 <= num <= 189:
            return 'SILVER_WIND'
    elif attack == 'RETURN':
        # Lopunny
        if num == 428:
            return 'CAPTIVATE'
    elif attack == 'SPOTLIGHT':
        # Morelull/Shiinotic
        if num == 755 or num == 756:
            return 'AROMATHERAPY'
        # Starmie
        elif num == 121:
            return 'COSMIC_POWER'
        # Lanturn
        elif num == 171:
            return 'SOAK'
        # Clefairy/Clefable
        elif num == 35 or num == 36:
            return 'WISH'
    elif attack == 'WIDE_GUARD':
        # Mareanie/Toxapex
        if num == 747 or num == 748:
            return 'ACID_ARMOR'
        # Throh
        elif num == 538:
            return 'BRICK_BREAK'
        # Mantine/Mantyke
        elif num == 226 or num == 458:
            return 'DIVE'
        # Hitmontop
        elif num == 237:
            return 'DRILL_RUN'
        # Mienshao
        elif num == 620:
            return 'DUAL_CHOP'
        # Kingler
        elif num == 99:
            return 'FURY_CUTTER'
        # Tirtouga/Carracosta
        elif num == 564 or num == 565:
            return 'IRON_DEFENSE'
        # Lunala
        elif num == 792:
            return 'LIGHT_SCREEN'
        # Hitmonlee
        elif num == 106:
            return 'LOW_KICK'
        # Regigagas
        elif num == 486:
            return 'MEGA_PUNCH'
        # Alomomola and Avalugg
        elif num == 594 or num == 713:
            return 'MIST'
        # Solgaleo
        elif num == 791:
            return 'REFLECT'
        # Gallade
        elif num == 475:
            return 'SACRED_SWORD'
        # Probopass
        elif num == 476:
            return 'STEALTH_ROCK'
        # Araquanid
        elif num == 752:
            return 'STICKY_WEB'
        # Stakataka
        elif num == 805:
            return 'STONE_EDGE'
        # Machamp
        elif num == 68:
            return 'SUPERPOWER'
        # Mr. Mime
        elif num == 122:
            return 'TEETER_DANCE'
        # Celesteela and Guzzlord -- TODO: too lazy to come up with an alternative right now
        elif num == 797 or num == 799:
            return ''

    return attack


# Removes or replaces the ability with an alternative if applicable for the corresponding Pokemon
# Will return the empty string to indicate removing ability (never 'NO_ABILITY')
# The input ability string is expected to be in namesies format
def ability_substitution(num: int, ability: str) -> str:
    if ability == 'FLOWER_VEIL':
        # Comfey -- this ability was changed and doesn't make as much sense anymore for Comfey
        if num == 764:
            return ''
    elif ability == 'RUN_AWAY':
        # Ponyta/Rapidash should really have this ability
        # Also I know that this is their HA and if this is blank then they'll get it, but this case would exist
        # regardless of the fact that its their HA so I think I'll just leave as is
        if num == 77 or num == 78:
            return 'FLAME_BODY'

    # All abilities which were removed
    # Needs to be at the bottom for Pokemon with substitutions instead of removals
    if ability in ['FRIEND_GUARD', 'ILLUMINATE', 'MINUS', 'POWER_CONSTRUCT', 'PLUS', 'RECEIVER',
                   'SYMBIOSIS', 'TELEPATHY', 'ZEN_MODE']:
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
