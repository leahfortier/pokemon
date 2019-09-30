#!/usr/bin/env python3
# -*- coding: utf-8 -*-
from typing import List

from scripts.serebii.forms import AddedPokes


# Attack replacement rules that always apply independent of pokemon and learn method
def _attack_substitution(attack: str) -> str:
    if attack is None or attack == '':
        raise Exception()
    # Ion Deluge was combined into Electrify
    elif attack == 'ION_DELUGE':
        return 'ELECTRIFY'
    # Intentional spelling change
    elif attack == 'JUDGMENT':
        return 'JUDGEMENT'
    else:
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


# Replaces the level-up move with an alternative if applicable
# Can potentially return the empty string to indicate removing the move altogether
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


def ability_substitution(num, ability):
    if ability == 'BATTERY':
        # Charjabug
        if num == 737:
            return 'STATIC'
    elif ability == 'EARLY_BIRD':
        # Natu/Xatu -- I just love this ability and I want more Pokemon to have it
        if num == 177 or num == 178:
            return 'MAGIC_BOUNCE'
    elif ability == 'FRIEND_GUARD':
        # Spewpa
        if num == 665:
            return 'NO_ABILITY'
    elif ability == 'FLOWER_VEIL':
        # Comfey -- this ability was changed and doesn't make as much sense anymore for Comfey
        if num == 764:
            return 'NATURAL_CURE'
    elif ability == 'ILLUMINATE':
        # Staryu/Starmie and Watchog
        if num == 120 or num == 121 or num == 505:
            return 'ANALYTIC'
        # Volbeat
        elif num == 313:
            return 'PRANKSTER'
        elif num == 755 or num == 756:
            return 'RAIN_DISH'
        # Chinchou/Lanturn
        elif num == 170 or num == 171:
            return 'WATER_ABSORB'
    elif ability == 'MINUS':
        # Klink line
        if 599 <= num <= 601:
            return 'NO_ABILITY'
        # Minun
        elif num == 312:
            return 'STATIC'
    elif ability == 'POWER_CONSTRUCT':
        # Zygarde -- this should always be true if inside this if
        if num == 718:
            return 'NO_ABILITY'
    elif ability == 'PLUS':
        # Klink line
        if 599 <= num <= 601:
            return 'CLEAR_BODY'
        # Mareep line
        elif 179 <= num <= 181:
            return 'NO_ABILITY'
        # Plusle
        elif num == 311:
            return 'STATIC'
    elif ability == 'RECEIVER':
        # Passimian
        if num == 766:
            return 'NO_ABILITY'
    elif ability == 'RUN_AWAY':
        # Ponyta/Rapidash should really have this ability
        if num == 77 or num == 78:
            return 'FLAME_BODY'
    elif ability == 'STALL':
        # Sableye -- Prankster is way cooler
        if num == 302:
            return 'PRANKSTER'
    elif ability == 'SYMBIOSIS':
        # Flabebe line
        if 669 <= num <= 671:
            return 'FLOWER_GIFT'
    elif ability == 'TELEPATHY':
        # Elgyem/Beheeyem
        if num == 605 or num == 606:
            return 'ANALYTIC'
        # Wobbuffet/Wynaut and Meditite/Medicham and Dialga/Palkia/Giratina
        # and Oranguru and the Tapus
        elif num == 202 or num == 360 \
                or num == 307 or num == 308 \
                or num == 483 or num == 484 or num == 487 \
                or num == 765 \
                or 785 <= num <= 788:
            return 'NO_ABILITY'
    elif ability == 'ZEN_MODE':
        # Darmanitan
        if num == 555:
            return 'NO_ABILITY'

    return ability


# My personal type changes
def type_substitution(num, types):
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
