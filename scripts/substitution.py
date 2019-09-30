#!/usr/bin/env python3
# -*- coding: utf-8 -*-
from typing import List

from scripts.serebii.forms import AddedPokes


# Attack replacement rules that always apply independent of pokemon and learn method
def _attack_substitution(attack: str) -> str:
    if attack is None or attack == '':
        raise Exception()
    # Ion Deluge was combined into Electrify
    elif attack == 'Ion Deluge':
        return 'Electrify'
    # Intentional spelling change
    elif attack == 'Judgment':
        return 'Judgement'
    else:
        return attack


# Replaces the learnable move with an alternative if applicable
# Will return the empty string to indicate removing the move
def learnable_attack_substitution(attack: str) -> str:
    attack = _attack_substitution(attack)
    if attack in ['After You', 'Ally Switch', 'Follow Me', 'Frustration', 'Helping Hand', 'Instruct', 'Quash',
                  'Rage Powder', 'Return', 'Spotlight', 'Wide Guard']:
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
        additions.append("Fly")

    return additions


# Replaces the level-up move with an alternative if applicable
# Can potentially return the empty string to indicate removing the move altogether
def attack_substitution(num: int, attack: str) -> str:
    attack = _attack_substitution(attack)

    if attack == 'After You':
        # Patrat/Watchog
        if num == 504 or num == 505:
            return 'Covet'
        # Lopunny
        elif num == 428:
            return 'Draining Kiss'
        # Petilil
        elif num == 548:
            return 'Heal Bell'
        # Minccino
        elif num == 572:
            return 'Iron Tail'
        # Togetic/Togekiss
        elif num == 176 or num == 468:
            return 'Moonblast'
        # Togepi
        elif num == 175:
            return 'Soft Boiled'
        # Buneary
        elif num == 427:
            return 'Sweet Kiss'
        # Clefairy and Audino
        elif num == 35 or num == 531:
            return 'Wish'
        # Oranguru
        elif num == 765:
            return 'Wonder Room'
        # Maractus
        elif num == 556:
            return 'Wood Hammer'
    elif attack == 'Ally Switch':
        # Kadabra/Alakazam
        if num == 64 or num == 65:
            return 'Barrier'
        # Hoopa
        elif num == 720:
            return 'Magic Room'
    elif attack == 'Follow Me':
        # Sentret/Furret
        if num == 161 or num == 162:
            return 'Covet'
        # Togepi/Togetic
        elif num == 175 or num == 176:
            return 'Draining Kiss'
        # Clefairy
        elif num == 35:
            return 'Mimic'
    elif attack == 'Frustration':
        # Buneary
        if num == 427:
            return 'Fake Tears'
    elif attack == 'Helping Hand':
        # Meowstic
        if num == 678:
            return 'Assist'
        # Jirachi
        elif num == 385:
            return 'Calm Mind'
        # Sentret/Furret
        elif num == 161 or num == 162:
            return 'Charm'
        # Tyrogue and Minccino
        elif num == 236 or num == 572:
            return 'Covet'
        # Volbeat/Illumise
        elif num == 313 or num == 314:
            return 'Dizzy Punch'
        # Marill/Azumarill/Azurill
        elif num == 183 or num == 184 or num == 298:
            return 'Draining Kiss'
        # Gallade
        elif num == 475:
            return 'Dual Chop'
        # Petilil
        elif num == 548:
            return 'Fairy Wind'
        # Audino
        elif num == 531:
            return 'Heal Bell'
        # Growlithe and Lillipup line
        elif num == 58 or 506 <= num <= 508:
            return 'Howl'
        # Keldeo
        elif num == 647:
            return 'Icy Wind'
        # Cobalion
        elif num == 638:
            return 'Iron Defense'
        # Cinccino
        elif num == 573:
            return 'Iron Tail'
        # Magearna
        elif num == 801:
            return 'Light Screen'
        # Comfey
        elif num == 764:
            return 'Lucky Chant'
        # Leavanny
        elif num == 542:
            return 'Me First'
        # Nidoran line
        elif 29 <= num <= 33:
            return 'Poison Tail'
        # Alomomola
        elif num == 594:
            return 'Refresh'
        # Terrakion
        elif num == 639:
            return 'Stealth Rock'
        # Plusle/Minun
        elif num == 311 or num == 312:
            return 'Sweet Kiss'
        # Virizion
        elif num == 640:
            return 'Synthesis'
        # Latias/Latios and Cottonee and Oricorio
        elif num in [380, 381, 546, 741]:
            return 'Tailwind'
        # Cherubi/Cherrim and Pykumuku
        elif num == 420 or num == 421 or num == 771:
            return 'Tickle'
        # Wishiwashi
        elif num == 746:
            return 'Water Sport'
        # Eeveelutions all start with this
        # Also Poipole and Naganadel -- TODO: too lazy to come up with an alternative right now
        elif num in [133, 134, 135, 136, 196, 197, 470, 471, 700, 803, 804]:
            return ''
    elif attack == 'Instruct':
        # Oranguru
        if num == 765:
            return 'Light Screen'
    elif attack == 'Quash':
        # Sableye
        if num == 302 or num == AddedPokes.MEGA_SABLEYE.value:
            return 'Night Slash'
        # Oranguru
        elif num == 765:
            return 'Psychic Terrain'
        # Murkrow/Honchkrow
        elif num == 198 or num == 430:
            return 'Roost'
    elif attack == 'RAGE_POWDER':
        # Foongus/Amoonguss
        if num == 590 or num == 591:
            return 'Gastro Acid'
        # Paras/Parasect
        elif num == 46 or num == 47:
            return 'Leech Life'
        # Butterfree and Volcarona
        elif num == 12 or num == 637:
            return 'MORNING_SUN'
        # Hoppip line
        elif 187 <= num <= 189:
            return 'Silver Wind'
    elif attack == 'Return':
        # Lopunny
        if num == 428:
            return 'Captivate'
    elif attack == 'Spotlight':
        # Morelull/Shiinotic
        if num == 755 or num == 756:
            return 'Aromatherapy'
        # Starmie
        elif num == 121:
            return 'Cosmic Power'
        # Lanturn
        elif num == 171:
            return 'Soak'
        # Clefairy/Clefable
        elif num == 35 or num == 36:
            return 'Wish'
    elif attack == 'Wide Guard':
        # Mareanie/Toxapex
        if num == 747 or num == 748:
            return 'Acid Armor'
        # Throh
        elif num == 538:
            return 'Brick Break'
        # Mantine/Mantyke
        elif num == 226 or num == 458:
            return 'Dive'
        # Hitmontop
        elif num == 237:
            return 'Drill Run'
        # Mienshao
        elif num == 620:
            return 'Dual Chop'
        # Kingler
        elif num == 99:
            return 'Fury Cutter'
        # Tirtouga/Carracosta
        elif num == 564 or num == 565:
            return 'Iron Defense'
        # Lunala
        elif num == 792:
            return 'Light Screen'
        # Hitmonlee
        elif num == 106:
            return 'Low Kick'
        # Regigagas
        elif num == 486:
            return 'Mega Punch'
        # Alomomola and Avalugg
        elif num == 594 or num == 713:
            return 'Mist'
        # Solgaleo
        elif num == 791:
            return 'Reflect'
        # Gallade
        elif num == 475:
            return 'Sacred Sword'
        # Probopass
        elif num == 476:
            return 'Stealth Rock'
        # Araquanid
        elif num == 752:
            return 'Sticky Web'
        # Stakataka
        elif num == 805:
            return 'Stone Edge'
        # Machamp
        elif num == 68:
            return 'Superpower'
        # Mr. Mime
        elif num == 122:
            return 'Teeter Dance'
        # Celesteela and Guzzlord -- TODO: too lazy to come up with an alternative right now
        elif num == 797 or num == 799:
            return ''

    return attack


def ability_substitution(num, ability):
    if ability == 'Battery':
        # Charjabug
        if num == 737:
            return 'Static'
    elif ability == 'Early Bird':
        # Natu/Xatu -- I just love this ability and I want more Pokemon to have it
        if num == 177 or num == 178:
            return 'Magic Bounce'
    elif ability == 'Friend Guard':
        # Spewpa
        if num == 665:
            return 'No_Ability'
    elif ability == 'Flower Veil':
        # Comfey -- this ability was changed and doesn't make as much sense anymore for Comfey
        if num == 764:
            return 'Natural Cure'
    elif ability == 'Illuminate':
        # Staryu/Starmie and Watchog
        if num == 120 or num == 121 or num == 505:
            return 'Analytic'
        # Volbeat
        elif num == 313:
            return 'Prankster'
        elif num == 755 or num == 756:
            return 'Rain Dish'
        # Chinchou/Lanturn
        elif num == 170 or num == 171:
            return 'Water Absorb'
    elif ability == 'Minus':
        # Klink line
        if 599 <= num <= 601:
            return 'No_Ability'
        # Minun
        elif num == 312:
            return 'Static'
    elif ability == 'Power Construct':
        # Zygarde -- this should always be true if inside this if
        if num == 718:
            return 'No_Ability'
    elif ability == 'Plus':
        # Klink line
        if 599 <= num <= 601:
            return 'Clear Body'
        # Mareep line
        elif 179 <= num <= 181:
            return 'No_Ability'
        # Plusle
        elif num == 311:
            return 'Static'
    elif ability == 'Receiver':
        # Passimian
        if num == 766:
            return 'No_Ability'
    elif ability == 'Run Away':
        # Ponyta/Rapidash should really have this ability
        if num == 77 or num == 78:
            return 'Flame Body'
    elif ability == 'Stall':
        # Sableye -- Prankster is way cooler
        if num == 302:
            return 'Prankster'
    elif ability == 'Symbiosis':
        # Flabebe line
        if 669 <= num <= 671:
            return 'Flower Gift'
    elif ability == 'Telepathy':
        # Elgyem/Beheeyem
        if num == 605 or num == 606:
            return 'Analytic'
        # Wobbuffet/Wynaut and Meditite/Medicham and Dialga/Palkia/Giratina
        # and Oranguru and the Tapus
        elif num == 202 or num == 360 \
                or num == 307 or num == 308 \
                or num == 483 or num == 484 or num == 487 \
                or num == 765 \
                or 785 <= num <= 788:
            return 'No_Ability'
    elif ability == 'Zen Mode':
        if num == 555:
            return 'No_Ability'

    return ability


# My personal type changes
def type_substitution(num, types):
    # Ninetales is now Psychic type
    if num == 38:
        assert types == ['Fire', 'No_Type']
        return ['Fire', 'Psychic']
    # Psyduck/Golduck are now Psychic type
    elif num == 54 or num == 55:
        assert types == ['Water', 'No_Type']
        return ['Water', 'Psychic']
    # Gyarados is now Water/Dragon instead of Water/Flying
    elif num == 130:
        assert types == ['Water', 'Flying']
        return ['Water', 'Dragon']
    # Noctowl is now Psychic/Flying
    elif num == 164:
        assert types == ['Normal', 'Flying']
        return ['Psychic', 'Flying']
    # Luxray is now Dark type
    elif num == 405:
        assert types == ['Electric', 'No_Type']
        return ['Electric', 'Dark']
    # Flabebe line is now Grass type
    elif 669 <= num <= 671:
        assert types == ['Fairy', 'No_Type']
        return ['Fairy', 'Grass']
    # Goomy line is now Water type
    elif 704 <= num <= 706:
        assert types == ['Dragon', 'No_Type']
        return ['Dragon', 'Water']
    # Mega Absol (Asbel) is Fairy type :)
    elif num == AddedPokes.MEGA_ABSOL.value:
        assert types == ['Dark', 'No_Type']
        return ['Dark', 'Fairy']

    return types
